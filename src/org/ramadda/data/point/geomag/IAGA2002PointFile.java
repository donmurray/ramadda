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

package org.ramadda.data.point.geomag;


import org.ramadda.data.point.*;
import org.ramadda.data.point.text.*;
import org.ramadda.data.record.*;

import ucar.unidata.util.StringUtil;

import java.io.*;

import java.util.List;



/**
 * A file reader for IAGA2002 formatted geomagnetic data
 * run:
 * java org.ramadda.data.point.geomag.IAGA2002PointFile  examples/abk20130725vmin.min
 */
public class IAGA2002PointFile extends CsvFile {


    /**
     * The constructor
     *
     * @param filename file
     * @throws IOException On badness
     */
    public IAGA2002PointFile(String filename) throws IOException {
        super(filename);
    }

    /**
     * Tell the base class to read the header the standard way  - until a non header line is reached
     *
     * @return true
     */
    public boolean isHeaderStandard() {
        return true;
    }

    /**
     * Overwrite base class method to determine if the given line is a header line
     *
     * @param line line
     *
     * @return is header line
     */
    @Override
    public boolean isHeaderLine(String line) {
        return line.endsWith("|");
    }


    /**
     * This  gets called before the file is visited.
     * It reads the header and defines the fields
     *
     * @param visitInfo visit info
     * @return possible new visitinfo
     *
     * @throws Exception _more_
     */
    @Override
    public VisitInfo prepareToVisit(VisitInfo visitInfo) throws Exception {

        //Set the delimiter and the date format
        putProperty(PROP_DELIMITER, "");
        putProperty(PROP_DATEFORMAT, "yyyy-MM-dd HH:mm:ss");
        super.prepareToVisit(visitInfo);

        List<String> headerLines = getHeaderLines();

        /*
Format                 IAGA-2002                                    |
 Source of Data         Sveriges geologiska undersokning             |
 Station Name           Abisko                                       |
 IAGA CODE              ABK                                          |
 Geodetic Latitude      68.400                                       |
 Geodetic Longitude     18.800                                       |
 Elevation                                                           |
 Reported               XYZF                                         |
 Sensor Orientation                                                  |
 Digital Sampling                                                    |
 Data Interval Type     1-minute                                     |
 Data Type              variation
        */

        //Read the metadata from the header
        int    idx       = 0;
        String format    = getHeaderValue(headerLines.get(idx++));
        String source    = getHeaderValue(headerLines.get(idx++));
        String station   = getHeaderValue(headerLines.get(idx++));
        String iagaCode  = getHeaderValue(headerLines.get(idx++));
        String latitude  = getHeaderValue(headerLines.get(idx++));
        String longitude = getHeaderValue(headerLines.get(idx++));
        String elevation = getHeaderValue(headerLines.get(idx++));
        if (elevation.length() == 0) {
            elevation = "0";
        }

        String reported    = getHeaderValue(headerLines.get(idx++));
        String orientation = getHeaderValue(headerLines.get(idx++));
        String sampling    = getHeaderValue(headerLines.get(idx++));
        String interval    = getHeaderValue(headerLines.get(idx++));
        String dataType    = getHeaderValue(headerLines.get(idx++));

        //This matches up with the entry definition in the point plugin in
        //org/ramadda/geodata/point/geomagtypes.xml

        setFileMetadata(new Object[] {
            iagaCode, station, source, orientation, sampling, interval,
            dataType
        });


        //The  date and time fields are defined as string. The isdate and istime attributes
        //cause the 2 fields to be concatenated together and the above date format is applied
        //The attrValue(latitude), etc., is the way the georeferencing is passed back
        StringBuffer sb = new StringBuffer();
        sb.append(makeFields(new String[] {
            makeField(FIELD_DATE, attrType("string"), attr("isdate", "true")),
            makeField(FIELD_TIME, attrType("string"), attr("istime", "true")),
            makeField(FIELD_DOY),
            makeField(FIELD_LATITUDE, attrValue(latitude)),
            makeField(FIELD_LONGITUDE, attrValue(longitude)),
            makeField(FIELD_ELEVATION, attrValue(elevation)),
        }));

        //The IAGA has different value columns depending on the reported in the header
        for (int i = 0; i < reported.length(); i++) {
            char c = reported.charAt(i);
            sb.append(",");
            sb.append(makeField("" + c, attrChartable(), attrSearchable(),
                                attrMissing(99999)));
        }

        putProperty(PROP_FIELDS, sb.toString());

        return visitInfo;
    }

    /**
     * Utility to read the IAGA2002 header and gets the value
     *
     * @param line header  line
     *
     * @return value
     */
    private String getHeaderValue(String line) {
        String s = line.substring(23).trim();
        s = s.substring(0, s.length() - 1);
        s = s.trim();

        return s;
    }


    /**
     * _more_
     *
     * @param args _more_
     */
    public static void main(String[] args) {
        PointFile.test(args, IAGA2002PointFile.class);
    }

}
