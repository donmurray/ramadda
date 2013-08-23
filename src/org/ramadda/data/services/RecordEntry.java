/*
* Copyright 2008-2013 Geode Systems LLC
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

import org.ramadda.data.record.*;
import org.ramadda.data.record.filter.*;


import org.ramadda.repository.*;

import ucar.unidata.util.IOUtil;

import java.io.File;

import java.util.concurrent.*;


/**
 * This is a wrapper around  ramadda Entry and a RecordFile
 *
 *
 */
public class RecordEntry implements Runnable, Callable<Boolean> {


    /** the output handler */
    private RecordOutputHandler recordOutputHandler;

    /** the ramadda entry */
    private Entry entry;

    /** the record file */
    private RecordFile recordFile;

    /** the initial user request */
    private Request request;

    /** the visitor */
    private RecordVisitor visitor;

    /** the visit info */
    private VisitInfo visitInfo;

    /** the job id */
    private Object processId;

    /**
     * ctor
     *
     *
     * @param recordOutputHandler output handler
     * @param request the request
     * @param entry the entry
     */
    public RecordEntry(RecordOutputHandler recordOutputHandler,
                       Request request, Entry entry) {
        this.recordOutputHandler = recordOutputHandler;
        this.request             = request;
        this.entry               = entry;

    }

    /**
     * _more_
     *
     * @param index _more_
     *
     * @return _more_
     *
     * @throws Exception _more_
     */
    public Record getRecord(int index) throws Exception {
        return getRecordFile().getRecord(index);
    }


    /**
     * create the record filter from the request
     *
     * @return the filter
     *
     * @throws Exception on badness
     */
    public RecordFilter getFilter() throws Exception {
        return recordOutputHandler.getFilter(request, entry, getRecordFile());
    }

    /**
     *  Set the ProcessId property.
     *
     *  @param value The new value for ProcessId
     */
    public void setProcessId(Object value) {
        processId = value;
    }

    /**
     *  Get the ProcessId property.
     *
     *  @return The ProcessId
     */
    public Object getProcessId() {
        return processId;
    }


    /**
     * How many records in the record file
     *
     * @return number of records
     *
     * @throws Exception on badness
     */
    public long getNumRecords() throws Exception {
        long records = getNumRecordsFromEntry(-1);
        if (records < 0) {
            records = getRecordFile().getNumRecords();
        }

        return records;
    }

    /**
     * get the number of points in the record file
     *
     * @param dflt default value
     *
     * @return number of records
     *
     * @throws Exception On badness
     */
    public long getNumRecordsFromEntry(long dflt) throws Exception {
        Object[] values = entry.getValues();
        if ((values != null) && (values.length > 0) && (values[0] != null)) {
            return ((Integer) values[0]).intValue();
        }

        return dflt;
    }

    /**
     * create if needed and return the RecordFile
     *
     * @return the record file
     *
     * @throws Exception on badness
     */
    public RecordFile getRecordFile() throws Exception {
        return recordFile;
    }

    /**
     * _more_
     *
     * @param file _more_
     */
    public void setRecordFile(RecordFile file) {
        this.recordFile = file;
    }


    /**
     * _more_
     *
     * @return _more_
     */
    public RecordOutputHandler getOutputHandler() {
        return recordOutputHandler;
    }


    /*
     * get the entry
     *
     * @return the entry
     */

    /**
     * _more_
     *
     * @return _more_
     */
    public Entry getEntry() {
        return entry;
    }


    /**
     * implement the callable interface
     *
     * @return OK
     *
     * @throws Exception _more_
     */
    public Boolean call() throws Exception {
        try {
            visit(visitor, visitInfo);
        } catch (Exception exc) {
            System.err.println("RecordEntry: ERROR:" + exc);

            throw exc;
        }

        return Boolean.TRUE;
    }


    /**
     * apply the visitor to the record file
     */
    public void run() {
        try {
            visit(visitor, visitInfo);
        } catch (Exception exc) {
            throw new RuntimeException(exc);
        }
    }

    /**
     * set the visit info
     *
     * @param visitor the visitor
     * @param visitInfo the visit info
     */
    public void setVisitInfo(RecordVisitor visitor, VisitInfo visitInfo) {
        this.visitor   = visitor;
        this.visitInfo = visitInfo;
    }

    /**
     * apply the visitor to the recordfile
     *
     * @param visitor visitor
     * @param visitInfo visit info
     *
     * @throws Exception On badness
     */
    public void visit(RecordVisitor visitor, VisitInfo visitInfo)
            throws Exception {
        getRecordFile().visit(visitor, visitInfo, getFilter());
    }



    /**
     * _more_
     *
     * @return _more_
     */
    public Request getRequest() {
        return request;
    }


}
