/*
 * Copyright (c) 2008-2015 Geode Systems LLC
 * This Software is licensed under the Geode Systems RAMADDA License available in the source distribution in the file 
 * ramadda_license.txt. The above copyright notice shall be included in all copies or substantial portions of the Software.
 */

package org.ramadda.data.tools;


import org.ramadda.data.point.*;


import org.ramadda.data.record.*;
import org.ramadda.util.grid.LatLonGrid;
import org.ramadda.util.grid.ObjectGrid;

import ucar.unidata.util.IOUtil;

import java.io.*;

import java.util.ArrayList;
import java.util.List;


/**
 * Class description
 *
 *
 * @version        $version$, Wed, Feb 15, '12
 * @author         Enter your name here...
 */
public class CsvFileInfo extends PointFileInfo {

    /** _more_ */
    boolean wroteHeader = false;

    /** _more_ */
    VisitInfo visitInfo;

    /**
     * _more_
     *
     * @param outputFile _more_
     * @param pointFile _more_
     *
     * @throws Exception _more_
     */
    public CsvFileInfo(String outputFile, PointFile pointFile)
            throws Exception {
        super(outputFile, pointFile);
        visitInfo = new VisitInfo();
    }

    /**
     * _more_
     *
     * @throws Exception _more_
     */
    public void writeHeader() throws Exception {}

    /**
     * _more_
     *
     * @param record _more_
     *
     * @throws Exception _more_
     */
    public void writeRecord(Record record) throws Exception {
        if ( !wroteHeader) {
            record.printCsvHeader(visitInfo, recordOutput.getPrintWriter());
            wroteHeader = true;
        }
        record.printCsv(visitInfo, recordOutput.getPrintWriter());
    }
}
