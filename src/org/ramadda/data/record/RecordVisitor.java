/*
 * Copyright (c) 2008-2015 Geode Systems LLC
 * This Software is licensed under the Geode Systems RAMADDA License available in the source distribution in the file 
 * ramadda_license.txt. The above copyright notice shall be included in all copies or substantial portions of the Software.
 */

package org.ramadda.data.record;


import java.io.*;


/**
 * Class description
 *
 *
 * @version        Enter version here..., Fri, May 21, '10
 * @author         Enter your name here...
 */
public abstract class RecordVisitor {

    /**
     * _more_
     *
     * @param file _more_
     * @param visitInfo _more_
     * @param record _more_
     *
     * @return _more_
     *
     * @throws Exception _more_
     */
    public abstract boolean visitRecord(RecordFile file, VisitInfo visitInfo,
                                        Record record)
     throws Exception;

    /**
     * This gets called when the visitor is done visiting the given record file
     *
     * @param file file we just visited
     * @param visitInfo visit info
     *
     * @throws Exception _more_
     */
    public void finished(RecordFile file, VisitInfo visitInfo)
            throws Exception {}

    /**
     * _more_
     *
     * @param visitInfo _more_
     */
    public void close(VisitInfo visitInfo) {}






}
