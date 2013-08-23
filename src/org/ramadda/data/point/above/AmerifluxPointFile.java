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

package org.ramadda.data.point.above;


import org.ramadda.data.point.*;
import org.ramadda.data.point.text.*;

import org.ramadda.data.record.*;
import org.ramadda.util.Utils;

import ucar.unidata.util.Misc;
import ucar.unidata.util.StringUtil;

import java.io.*;

import java.text.SimpleDateFormat;

import java.util.ArrayList;
import java.util.Date;
import java.util.Hashtable;
import java.util.List;


/**
 */
public class AmerifluxPointFile extends SingleSiteTextFile {

    /** _more_          */
    private SimpleDateFormat sdf = makeDateFormat("yyyy-D HHmm");



    /**
     * ctor
     *
     *
     * @param filename _more_
     * @throws Exception On badness
     *
     * @throws IOException _more_
     */
    public AmerifluxPointFile(String filename) throws IOException {
        super(filename);
    }





    /*
Sitename: UCI 1930 Canada
Location: Latitude: 55.9058  Longitude: -98.5247  Elevation (masl): 257
Principal investigator: Michael Goulden
Ecosystem type: ENF Evergreen Needleleaf Forest
File creation date: 15NOV2010
Data Policy -- The AmeriFlux data provided on this site are freely available and were furnished by individual AmeriFlux scientists who encourage their use.
Please kindly inform in writing (or e-mail) the appropriate AmeriFlux scientist(s) of how you intend to use the data and of any publication plans.
It is also important to contact the AmeriFlux investigator to assure you are downloading the latest revision of the data and to prevent potential misuse or misinterpretation of the data.
Please acknowledge the data source as a citation or in the acknowledgments if no citation is available.
If the AmeriFlux Principal Investigators (PIs) feel that they should be acknowledged or offered participation as authors they will let you know.
And we assume that an agreement on such matters will be reached before publishing and/or use of the data for publication.
If your work directly competes with the PIs analysis they may ask that they have the opportunity to submit a manuscript before you submit one that uses unpublished data.
In addition when publishing please acknowledge the agency that supported the research. --
File Origin - This file was created at Oak Ridge National Laboratory by the AmeriFlux and FLUXNET data management groups.
These groups are supported by the U.S. Department of Energy and National Aeronautics and Space Administration.
This standardized file is identical in format to other standardized files provided here with a goal of aiding intersite comparisons  multi-site syntheses and modeling activities.
Questions about these standardized files should be addressed to Tom Boden (bodenta@ornl.gov) .
YEAR, GAP, DTIME, DOY, HRMIN, UST, TA, WD, WS, NEE, FC, SFC, H, SH, LE, SLE, FG, TS1, TSdepth1, TS2, TSdepth2, PREC, RH, PRESS, CO2, VPD, SWC1, SWC2, Rn, PAR, Rg, Rgdif, PARout, RgOut, Rgl, RglOut, H2O, RE, GPP, CO2top, CO2height, APAR, PARdif, APARpct, ZL
YEAR, GAP, DTIME, DOY, HRMIN, m/s, deg C, deg, m/s, umol/m2/s, umol/m2/s, umol/m2/s, W/m2, W/m2, W/m2, W/m2, W/m2, deg C, cm, deg C, cm, mm, %, kPa, umol/mol, kPa, %, %, W/m2, umol/m2/s, W/m2, W/m2, umol/m2/s, W/m2, W/m2, W/m2, mmol/mol, umol/m2/s, umol/m2/s, umol/mol, m, umol/m2/s, umol/m2/s, %, unitless
100....
    */


    /**
     *
     * @param visitInfo holds visit info
     *
     * @return visit info
     *
     * @throws IOException on badness
     */
    public VisitInfo prepareToVisit(VisitInfo visitInfo) throws IOException {
        putProperty(PROP_DELIMITER, ",");
        putProperty(PROP_SKIPLINES, "20");
        super.prepareToVisit(visitInfo);
        List<String> header = getHeaderLines();

        //        Sitename: UCI 1930 Canada
        List<String> toks   = StringUtil.splitUpTo(header.get(0), ":", 2);
        String       siteId = toks.get(1);


        //        Location: Latitude: 55.9058  Longitude: -98.5247  Elevation (masl): 257
        String locationLine = header.get(1);
        String latString = StringUtil.findPattern(locationLine,
                               "Latitude:\\s*([\\-0-9\\.]+)\\s+");
        String lonString = StringUtil.findPattern(locationLine,
                               ".*Longitude:\\s*([\\-0-9\\.]+)\\s+");
        String elevationString = StringUtil.findPattern(locationLine,
                                     "Elevation\\s*\\(.*\\):\\s+(\\d+)");

        if (latString == null) {
            throw new IllegalArgumentException("Could not read latitude:"
                    + locationLine);
        }

        if (lonString == null) {
            throw new IllegalArgumentException("Could not read longitude:"
                    + locationLine);
        }
        if (elevationString == null) {
            throw new IllegalArgumentException("Could not read elevation:"
                    + locationLine);
        }


        double lat       = Misc.decodeLatLon(latString);
        double lon       = Misc.decodeLatLon(lonString);
        double elevation = Double.parseDouble(elevationString);
        setLocation(lat, lon, elevation);

        //        Principal investigator: Marc Fischer and Margaret Torn
        String contact = StringUtil.splitUpTo(header.get(2), ":", 2).get(1);
        String ecosystemType = StringUtil.splitUpTo(header.get(3), ":",
                                   2).get(1);
        //LOOK: this needs to be in the same order as the aontypes.xml defines in the point plugin
        setFileMetadata(new Object[] { siteId, contact, ecosystemType });



        String baseAttrs = attrChartable() + attrSearchable();
        String fields = makeFields(new String[] {
                            makeField(FIELD_SITE_ID, attrType(TYPE_STRING),
                                      attrValue(siteId.trim())),
                            makeField("Ecosystem_Type",
                                      attrType(TYPE_STRING),
                                      attrValue(ecosystemType)),
                            makeField(FIELD_LATITUDE, attrValue(lat)),
                            makeField(FIELD_LONGITUDE, attrValue(lon)),
                            makeField(FIELD_ELEVATION,
                                      attrValue(elevation)), });

        List<String> fieldsFromFile = StringUtil.split(header.get(17), ",");
        List<String> unitsFromFile  = StringUtil.split(header.get(18), ",");
        for (int fieldIdx = 0; fieldIdx < fieldsFromFile.size(); fieldIdx++) {
            String field = fieldsFromFile.get(fieldIdx);
            String unit  = unitsFromFile.get(fieldIdx).trim();
            String attr  = baseAttrs;
            if (Utils.stringDefined(unit)) {
                attr += attrUnit(unit);
            }
            fields += "," + makeField(field, attr);
        }

        putProperty(PROP_FIELDS, fields);

        return visitInfo;
    }


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
    @Override
    public boolean processAfterReading(VisitInfo visitInfo, Record record)
            throws Exception {
        int        offset     = 5;
        TextRecord textRecord = (TextRecord) record;
        int        year       = (int) textRecord.getValue(offset + 1);
        int        doy        = (int) textRecord.getValue(offset + 4);
        String     hhmm       = "" + (int) textRecord.getValue(offset + 5);
        hhmm = StringUtil.padLeft(hhmm, 4, "0");
        String dttm = year + "-" + doy + " " + hhmm;
        Date   date = sdf.parse(dttm);
        record.setRecordTime(date.getTime());

        return super.processAfterReading(visitInfo, record);
    }

    /**
     * _more_
     *
     * @param record _more_
     * @param field _more_
     * @param v _more_
     *
     * @return _more_
     */
    public boolean isMissingValue(Record record, RecordField field,
                                  double v) {
        if ((v == -9999) || (v == -6999)) {
            return true;
        }

        return false;
    }


    /**
     * _more_
     *
     * @param args _more_
     */
    public static void main(String[] args) {
        PointFile.test(args, AmerifluxPointFile.class);
    }

}
