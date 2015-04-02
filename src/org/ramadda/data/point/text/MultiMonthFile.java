/**
* Copyright (c) 2008-2015 Geode Systems LLC
* This Software is licensed under the Geode Systems RAMADDA License available in the source distribution in the file 
* ramadda_license.txt. The above copyright notice shall be included in all copies or substantial portions of the Software.
*/

package org.ramadda.data.point.text;


import org.ramadda.data.point.*;
import org.ramadda.data.point.text.*;

import org.ramadda.data.record.*;

import ucar.unidata.util.Misc;
import ucar.unidata.util.StringUtil;

import java.io.*;

import java.text.SimpleDateFormat;

import java.util.Date;
import java.util.List;



/**
 */
public class MultiMonthFile extends CsvFile {

    /** _more_ */
    String varName = null;

    /** _more_ */
    String varDesc = null;

    /** _more_ */
    double missing = -99.9;

    /** _more_ */
    String unit = "";

    /**
     * The constructor
     *
     * @param filename file
     * @throws IOException On badness
     */
    public MultiMonthFile(String filename) throws IOException {
        this(filename, "index", "Index", "", -99.9);
    }

    /**
     * The constructor
     *
     * @param filename file
     * @param varName _more_
     * @param varDesc _more_
     * @param unit _more_
     * @param missing _more_
     * @throws IOException On badness
     */
    public MultiMonthFile(String filename, String varName, String varDesc,
                          String unit, double missing)
            throws IOException {
        super(filename);
        this.varName = varName;
        this.varDesc = varDesc;
        this.unit    = unit;
        this.missing = missing;
    }

    /**
     * _more_
     *
     * @return _more_
     */
    public List<RecordField> doMakeFields() {
        MultiMonthRecord record = new MultiMonthRecord(this, varName,
                                      varDesc, unit, missing);

        return record.getFields();
    }


    /**
     * _more_
     *
     * @param visitInfo _more_
     *
     * @return _more_
     */
    public Record doMakeRecord(VisitInfo visitInfo) {
        MultiMonthRecord record = new MultiMonthRecord(this, varName,
                                      varDesc, unit, missing);
        record.setFirstDataLine(firstDataLine);

        return record;
    }

    /**
     * _more_
     *
     * @param visitInfo _more_
     * @param record _more_
     * @param howMany _more_
     *
     * @return _more_
     *
     * @throws Exception _more_
     */
    @Override
    public boolean skip(VisitInfo visitInfo, Record record, int howMany)
            throws Exception {
        MultiMonthRecord mmr = (MultiMonthRecord) record;

        return mmr.skip(visitInfo, record, howMany);
    }

    /**
     * _more_
     *
     * @param args _more_
     */
    public static void main(String[] args) {
        PointFile.test(args, MultiMonthFile.class);
    }

}
