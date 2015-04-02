/**
* Copyright (c) 2008-2015 Geode Systems LLC
* This Software is licensed under the Geode Systems RAMADDA License available in the source distribution in the file 
* ramadda_license.txt. The above copyright notice shall be included in all copies or substantial portions of the Software.
*/

package org.ramadda.data.point.eol;


import org.ramadda.data.point.*;
import org.ramadda.data.point.text.*;
import org.ramadda.data.record.*;

import java.io.*;

import java.text.SimpleDateFormat;

import java.util.Date;


/**
 */
public class CeopSfcPointFile extends CsvFile {

    /**
     * The constructor
     *
     * @param filename file
     * @throws IOException On badness
     */
    public CeopSfcPointFile(String filename) throws IOException {
        super(filename);
    }


    /**
     * This  gets called before the file is visited. It reads the header and defines the fields
     *
     * @param visitInfo visit info
     * @return possible new visitinfo
     *
     * @throws Exception _more_
     */
    @Override
    public VisitInfo prepareToVisit(VisitInfo visitInfo) throws Exception {
        //Set the delimiter
        putProperty(PROP_DELIMITER, " ");
        //Set the fields. the method reads the file CeopSfcPointFile.fields.txt
        putProperty(PROP_FIELDS, getFieldsFileContents());
        super.prepareToVisit(visitInfo);

        return visitInfo;
    }


    /** _more_ */
    private SimpleDateFormat sdf = makeDateFormat("yyyy/MM/dd HH:mm");

    /**
     * This gets called after a record has been read.
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
        //concatenate the year and hour fields and set the date
        String dttm = textRecord.getStringValue(1) + " "
                      + textRecord.getStringValue(2);
        Date date = sdf.parse(dttm);
        record.setRecordTime(date.getTime());

        return true;
    }

    /**
     * _more_
     *
     * @param args _more_
     */
    public static void main(String[] args) {
        PointFile.test(args, CeopSfcPointFile.class);
    }

}
