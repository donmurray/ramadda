/*
* Copyright 2008-2012 Jeff McWhirter/ramadda.org
*                     Don Murray/CU-CIRES
*
* Permission is hereby granted, free of charge, to any person obtaining a copy of this 
* software and associated documentation files (the "Software"), to deal in the Software 
* without restriction, including without limitation the rights to use, copy, modify, 
* merge, publish, distribute, sublicense, and/or sell copies of the Software, and to 
* permit persons to whom the Software is furnished to do so, subject to the following conditions:
* 
* The above copyright notice and this permission notice shall be included in all copies 
* or substantial portions of the Software.
* 
* THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, 
* INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR 
* PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE 
* FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR 
* OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER 
* DEALINGS IN THE SOFTWARE.
*/

package org.ramadda.data.services;


import org.ramadda.data.record.*;
import org.ramadda.data.point.*;
import org.ramadda.data.point.binary.*;
import org.ramadda.data.record.filter.*;


import org.ramadda.repository.*;

import ucar.unidata.util.IOUtil;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.*;


/**
 * This is a wrapper around  ramadda Entry and a RecordFile
 *
 *
 */
public class PointEntry extends RecordEntry {

    public static final String SUFFIX_BINARY_DOUBLE = ".llab";    
    public static final String SUFFIX_BINARY_FLOAT = ".fllab";    

    public static final String FILE_BINARY_DOUBLE = "lightweight" + SUFFIX_BINARY_DOUBLE;
    public static final String FILE_BINARY_FLOAT = "lightweight" + SUFFIX_BINARY_FLOAT;



    /** This points to the  short lat/lon/alt binary file ramadda creates on the fly */
    private PointFile binaryPointFile;



    /**
     * ctor
     *
     * @param outputHandler _more_
     * @param request the request
     * @param entry the entry
     */
    public PointEntry(PointOutputHandler outputHandler, Request request,
                      Entry entry) {
        super(outputHandler, request, entry);
    }



    public static List<PointEntry> toPointEntryList(List l) {
        List<PointEntry> pointEntries = new ArrayList<PointEntry>();
        for(Object o: l) pointEntries.add((PointEntry)o);
        return pointEntries;
    }

    public PointOutputHandler getPointOutputHandler() {
        return (PointOutputHandler) getOutputHandler();
    }

    public PointFile getPointFile() throws Exception {
        return (PointFile) getRecordFile();
    }


    /**
     * _more_
     *
     * @return _more_
     *
     * @throws Exception _more_
     */
    @Override
    public RecordFile getRecordFile() throws Exception {
        RecordFile recordFile = (RecordFile) super.getRecordFile();
        if (recordFile == null) {
            long records = getNumRecordsFromEntry(-1);
            recordFile = getPointOutputHandler().createAndInitializeRecordFile(getRequest(),
                                                                               getEntry(), records);
            setRecordFile(recordFile);
        }
        return recordFile;
    }



    public static  boolean isDoubleBinaryFile(File f) {
        return f.toString().endsWith(SUFFIX_BINARY_DOUBLE);
    }


    /**
     * _more_
     *
     * @return _more_
     */
    public File getQuickScanFile() {
        File entryDir = getOutputHandler().getStorageManager().getEntryDir(
                            getEntry().getId(), true);
        //Look for one that exists
        for(String file: new String[]{FILE_BINARY_DOUBLE, FILE_BINARY_FLOAT}){
            File quickscanFile = new File(IOUtil.joinDir(entryDir,
                                                         file));
            if(quickscanFile.exists()) {
                return quickscanFile;
            }
        }
        //Default to the float
        return new File(IOUtil.joinDir(entryDir,FILE_BINARY_FLOAT));
        //return new File(IOUtil.joinDir(entryDir,FILE_BINARY_DOUBLE));
    }



    /**
     * apply the visitor to the point file
     *
     * @param visitor visitor
     * @param visitInfo visit info
     *
     * @throws Exception On badness
     */
    @Override
    public void visit(RecordVisitor visitor, VisitInfo visitInfo)
            throws Exception {
        if ((visitInfo != null) && visitInfo.getQuickScan()) {
            PointFile quickscanFile = getBinaryPointFile();
            System.err.println("POINT: Using quick scan file #records = "
                               + quickscanFile.getNumRecords());
            quickscanFile.visit(visitor, visitInfo, getFilter());
            return;
        }
        super.visit(visitor, visitInfo);
    }



    /**
     * get the Point File for the short lat/lon/alt binary file
     *
     * @return short binary file
     *
     * @throws Exception On badness
     */
    public PointFile getBinaryPointFile() throws Exception {
        PointFile pointFile = getPointFile();
        if (pointFile instanceof DoubleLatLonAltBinaryFile) {
            return pointFile;
        }
        if (pointFile instanceof FloatLatLonAltBinaryFile) {
            return pointFile;
        }
        if (binaryPointFile == null) {
            File entryDir =
                getOutputHandler().getStorageManager().getEntryDir(
                    getEntry().getId(), true);
            File quickscanFile = getQuickScanFile();
            if (!quickscanFile.exists()) {
                //Write to a tmp file and only move it over when we are done
                File tmpFile = new File(IOUtil.joinDir(entryDir,
                                                       "tmpfile.bin"));
                pointFile.setDefaultSkip(0);
                System.err.println("POINT: making quickscan file ");
                getPointOutputHandler().writeBinaryFile(tmpFile,
                                                        pointFile,isDoubleBinaryFile(quickscanFile));
                tmpFile.renameTo(quickscanFile);
                System.err.println("POINT: done making quickscan file");
            }

            if(isDoubleBinaryFile(quickscanFile)) {
                binaryPointFile =
                    new DoubleLatLonAltBinaryFile(quickscanFile.toString());
            } else{
                binaryPointFile =
                    new FloatLatLonAltBinaryFile(quickscanFile.toString());
            }
        }
        return binaryPointFile;
    }



}
