/*
* Copyright 2008-2014 Geode Systems LLC
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

package org.ramadda.data.point.text;


import org.ramadda.data.point.*;

import org.ramadda.data.record.*;

import ucar.unidata.util.Misc;

import ucar.unidata.util.StringUtil;

import java.awt.*;
import java.awt.image.*;

import java.io.*;

import java.text.SimpleDateFormat;

import java.util.ArrayList;
import java.util.Hashtable;
import java.util.List;

import javax.swing.*;


/**
 * CSV file supports any form of column delimited files - comma, tab, space, etc
 *
 */
public class CsvFile extends TextFile {

    /** column delimiter */
    private String delimiter = null;


    /**
     * ctor
     */
    public CsvFile() {}


    /**
     * ctor
     *
     *
     * @param filename _more_
     *
     * @throws IOException on badness
     */
    public CsvFile(String filename) throws IOException {
        super(filename);
    }

    /**
     * ctor
     *
     * @param filename _more_
     * @param properties _more_
     *
     * @throws IOException on badness
     */
    public CsvFile(String filename, Hashtable properties) throws IOException {
        super(filename, properties);
    }


    /**
     * _more_
     *
     * @param filename _more_
     *
     * @return _more_
     */
    public boolean canLoad(String filename) {
        String f = filename.toLowerCase();
        //A hack to not include lidar coordinate txt files
        if ((f.indexOf("coords") >= 0) || (f.indexOf("coordinates") >= 0)) {
            return false;
        }
        if (f.indexOf("target") >= 0) {
            return false;
        }

        return (f.endsWith(".csv") || f.endsWith(".txt")
                || f.endsWith(".xyz") || f.endsWith(".tsv"));
    }

    /**
     * is this file capable of certain actions - gridding, decimation, etc
     *
     * @param action action type
     *
     * @return is capable
     */
    public boolean isCapable(String action) {
        if (action.equals(ACTION_GRID)) {
            return true;
        }
        if (action.equals(ACTION_DECIMATE)) {
            return true;
        }

        return super.isCapable(action);
    }



    /**
     * _more_
     *
     * @return _more_
     */
    public String getDelimiter() {
        if (delimiter == null) {
            delimiter = getProperty(PROP_DELIMITER, ",");
            if (delimiter.length() == 0) {
                delimiter = " ";
            } else if (delimiter.equals("\\t")) {
                delimiter = "\t";
            } else if (delimiter.equals("tab")) {
                delimiter = "\t";
            }
        }

        return delimiter;
    }


    /**
     * _more_
     */
    public void initAfterClone() {
        super.initAfterClone();
        delimiter = null;
    }




    /**
     * _more_
     *
     * @return _more_
     */
    public List<RecordField> doMakeFields() {
        String fieldString = getProperty(PROP_FIELDS, null);
        if (fieldString == null) {
            doQuickVisit();
            fieldString = getProperty(PROP_FIELDS, null);
        } else {
            //Read the header because there are properties
            if(getProperty(PROP_HEADER_STANDARD, false)) {
                doQuickVisit();
            }
        }


        if (fieldString == null) {
            setIsHeaderStandard(true);
            doQuickVisit();
            fieldString = getProperty(PROP_FIELDS, null);
        }

        if (fieldString == null) {
            throw new IllegalArgumentException("Properties must have a "
                    + PROP_FIELDS + " value");
        }

        return doMakeFields(fieldString);
    }




    /**
     * _more_
     *
     *
     * @param visitInfo _more_
     * @return _more_
     */
    public Record doMakeRecord(VisitInfo visitInfo) {
        TextRecord record = new TextRecord(this, getFields());
        record.setFirstDataLine(firstDataLine);
        record.setDelimiter(getDelimiter());

        return record;
    }

    /**
     * _more_
     *
     * @param args _more_
     *
     * @throws Exception on badness
     */
    public static void main(String[] args) throws Exception {
        if (true) {
            PointFile.test(args, CsvFile.class);

            return;
        }


        for (int argIdx = 0; argIdx < args.length; argIdx++) {
            String arg = args[argIdx];
            try {
                long                t1       = System.currentTimeMillis();
                final int[]         cnt      = { 0 };
                CsvFile             file     = new CsvFile(arg);
                final RecordVisitor metadata = new RecordVisitor() {
                    public boolean visitRecord(RecordFile file,
                            VisitInfo visitInfo, Record record) {
                        cnt[0]++;
                        PointRecord pointRecord = (PointRecord) record;
                        if ((pointRecord.getLatitude() < -90)
                                || (pointRecord.getLatitude() > 90)) {
                            System.err.println("Bad lat:"
                                    + pointRecord.getLatitude());
                        }
                        if ((cnt[0] % 100000) == 0) {
                            System.err.println(cnt[0] + " lat:"
                                    + pointRecord.getLatitude() + " "
                                    + pointRecord.getLongitude() + " "
                                    + pointRecord.getAltitude());

                        }

                        return true;
                    }
                };
                file.visit(metadata);
                long t2 = System.currentTimeMillis();
                System.err.println("time:" + (t2 - t1) / 1000.0
                                   + " # record:" + cnt[0]);
            } catch (Exception exc) {
                System.err.println("Error:" + exc);
                exc.printStackTrace();
            }
        }
    }



}
