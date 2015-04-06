/*
 * Copyright (c) 2008-2015 Geode Systems LLC
 * This Software is licensed under the Geode Systems RAMADDA License available in the source distribution in the file 
 * ramadda_license.txt. The above copyright notice shall be included in all copies or substantial portions of the Software.
 */

package org.ramadda.data.point.gcnet;


import org.ramadda.data.point.*;
import org.ramadda.data.point.text.*;
import org.ramadda.data.record.*;


import org.ramadda.util.Station;

import java.io.*;


/**
 */

public class GCNetPointFile extends CsvFile {


    /**
     * ctor
     *
     * @param filename filename
     * @throws IOException On badness
     */
    public GCNetPointFile(String filename) throws IOException {
        super(filename);
    }


    /**
     * _more_
     *
     * @param visitInfo _more_
     *
     * @return _more_
     *
     * @throws Exception _more_
     */
    @Override
    public VisitInfo prepareToVisit(VisitInfo visitInfo) throws Exception {
        //Put the delimiter first so we can read the header in the parent method
        putProperty(PROP_HEADER_DELIMITER, "");
        putProperty(PROP_DELIMITER, " ");
        super.prepareToVisit(visitInfo);
        putProperty(PROP_FIELDS, getFieldsFileContents());

        return visitInfo;
    }


    /*
     * This gets called after a record has been read
     */

    /**
     * _more_
     *
     * @param visitInfo _more_
     * @param record _more_
     *
     * @return _more_
     *
     * @throws Exception _more_
     */
    public boolean processAfterReading(VisitInfo visitInfo, Record record)
            throws Exception {
        if ( !super.processAfterReading(visitInfo, record)) {
            return false;
        }
        TextRecord textRecord = (TextRecord) record;
        setLocation("" + (int) textRecord.getValue(1), textRecord);
        int    year      = (int) textRecord.getValue(5);
        double julianDay = textRecord.getValue(6);
        record.setRecordTime(getDateFromJulianDay(year, julianDay).getTime());

        return true;
    }


    /**
     * _more_
     *
     * @param args _more_
     */
    public static void main(String[] args) {
        PointFile.test(args, GCNetPointFile.class);
    }

}
