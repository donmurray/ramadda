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

package org.ramadda.geodata.cdmdata;


import org.ramadda.repository.Constants;


/**
 */
public interface CdmConstants {

    /** _more_          */
    public static final String ICON_OPENDAP = "/cdmdata/opendap.gif";

    /** CSV format */
    public static final String FORMAT_CSV = "csv";

    /** KML format */
    public static final String FORMAT_KML = "kml";

    /** NCML format */
    public static final String FORMAT_NCML = "ncml";

    /** _more_ */
    public static final String FORMAT_JSON = "json";

    /** NCML suffix */
    public static final String SUFFIX_NCML = ".ncml";

    /** netcdf suffix */
    public static final String SUFFIX_NC = ".nc";

    /** netcdf4 suffix */
    public static final String SUFFIX_NC4 = ".nc4";

    /** GrADS CTL suffix */
    public static final String SUFFIX_CTL = ".ctl";

    /** CSV suffix */
    public static final String SUFFIX_CSV = ".csv";

    /** _more_ */
    public static final String SUFFIX_JSON = ".json";

    /** CSV suffix */
    public static final String SUFFIX_XML = ".xml";

    /** bounding box argument */
    public static final String ARG_POINT_BBOX = "bbox";

    /** Variable prefix */
    public static final String VAR_PREFIX = Constants.ARG_VARIABLE + ".";

    /** add lat lon argument */
    public static final String ARG_ADDLATLON = "addlatlon";


    /** horizontal stride */
    public static final String ARG_HSTRIDE = "hstride";

    /** level */
    public static final String ARG_LEVEL = "level";

    /** format */
    public static final String ARG_FORMAT = "format";

    /** format */
    public static final String ARG_IMAGE_WIDTH = "image_width";

    /** format */
    public static final String ARG_IMAGE_HEIGHT = "image_height";

    /** calendar */
    public static final String ARG_CALENDAR = "calendar";

    /** spatial arguments */
    public static final String[] SPATIALARGS = new String[] {
                                                   Constants.ARG_AREA_NORTH,
            Constants.ARG_AREA_WEST, Constants.ARG_AREA_SOUTH,
            Constants.ARG_AREA_EAST, };

    /** chart format */
    public static final String FORMAT_TIMESERIES = "timeseries";

    /** chart format */
    public static final String FORMAT_TIMESERIES_CHART = "timeserieschart";


    /** chart format */
    public static final String FORMAT_TIMESERIES_CHART_DATA =
        "timeserieschartdata";

    /** chart image format */
    public static final String FORMAT_TIMESERIES_IMAGE = "timeseriesimage";

    /** Data group */
    public static final String GROUP_DATA = "Data";

    /** CDM Type */
    public static final String TYPE_CDM = "cdm";

    /** GRID type */
    public static final String TYPE_GRID = "grid";

    /** TRAJECTORY type */
    public static final String TYPE_TRAJECTORY = "trajectory";

    /** POINT_TYPE */
    public static final String TYPE_POINT = "point";

    /** GrADS type */
    public static final String TYPE_GRADS = "gradsbinary";


}