/*
 * Copyright (c) 2008-2015 Geode Systems LLC
 * This Software is licensed under the Geode Systems RAMADDA License available in the source distribution in the file 
 * ramadda_license.txt. The above copyright notice shall be included in all copies or substantial portions of the Software.
 */

package org.ramadda.data.record.filter;


import org.ramadda.data.record.*;

import java.io.*;

import java.util.ArrayList;
import java.util.List;


/**
 * Class description
 *
 *
 * @version        Enter version here..., Fri, May 21, '10
 * @author         Enter your name here...
 */
public class LatLonBoundsFilter implements RecordFilter {

    /** _more_ */
    public double north;

    /** _more_ */
    public double south;

    /** _more_ */
    public double east;

    /** _more_ */
    public double west;


    /**
     * _more_
     *
     * @param north _more_
     * @param south _more_
     * @param east _more_
     * @param west _more_
     */
    public LatLonBoundsFilter(double north, double west, double south,
                              double east) {
        this.north = north;
        this.west  = west;
        this.south = south;
        this.east  = east;
    }



    /**
     * _more_
     *
     * @param record _more_
     * @param visitInfo _more_
     *
     * @return _more_
     */
    public boolean isRecordOk(Record record, VisitInfo visitInfo) {
        GeoRecord geoRecord = (GeoRecord) record;
        if ( !geoRecord.isValidPosition()) {
            return false;
        }
        double lat = geoRecord.getLatitude();
        double lon = geoRecord.getLongitude();

        boolean contains = (lat >= south) && (lat <= north) && (lon >= west)
                           && (lon <= east);

        return contains;

    }


    /**
     * _more_
     *
     * @return _more_
     */
    public String toString() {
        return "llb filter:" + " N:" + north + " W:" + west + " S:" + south
               + " E:" + east;
    }


}
