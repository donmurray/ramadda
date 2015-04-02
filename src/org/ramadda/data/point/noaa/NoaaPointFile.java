/**
* Copyright (c) 2008-2015 Geode Systems LLC
* This Software is licensed under the Geode Systems RAMADDA License available in the source distribution in the file 
* ramadda_license.txt. The above copyright notice shall be included in all copies or substantial portions of the Software.
*/

package org.ramadda.data.point.noaa;


import org.ramadda.data.point.*;
import org.ramadda.data.point.text.*;



import org.ramadda.data.record.*;

import org.ramadda.util.Station;

import ucar.unidata.util.StringUtil;

import java.io.*;

import java.util.List;



/**
 */

public class NoaaPointFile extends CsvFile {

    /** _more_ */
    public static final String FIELD_NUMBER_OF_MEASUREMENTS =
        "number_of_measurements";

    /** _more_ */
    public static final String FIELD_QC_FLAG = "qc_flag";

    /** _more_ */
    public static final String FIELD_INTAKE_HEIGHT = "intake_height";

    /** _more_ */
    public static final String FIELD_INSTRUMENT = "instrument";

    /**
     * ctor
     *
     * @param filename _more_
     *
     * @throws IOException On badness
     */
    public NoaaPointFile(String filename) throws IOException {
        super(filename);
    }


    /**
     * _more_
     *
     * @return _more_
     */
    public String getStationsPath() {
        return "/org/ramadda/data/point/noaa/stations.txt";
    }



    /**
     * This is used by RAMADDA to determine what kind of services are available for this type of point data
     *
     * @param action _more_
     * @return is this file capable of the action
     */
    public boolean isCapable(String action) {
        if (action.equals(ACTION_BOUNDINGPOLYGON)) {
            return false;
        }
        if (action.equals(ACTION_GRID)) {
            return false;
        }

        return super.isCapable(action);
    }


    /*
     * Get the delimiter (space)
     *      @return the column delimiter
     */

    /**
     * _more_
     *
     * @return _more_
     */
    public String getDelimiter() {
        return " ";
    }


    /**
     * There are  2 header lines
     *
     * @param visitInfo file visit info
     *
     * @return how many lines to skip
     */
    public int getSkipLines(VisitInfo visitInfo) {
        return 0;
    }

}
