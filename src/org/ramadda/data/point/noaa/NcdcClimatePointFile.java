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

package org.ramadda.data.point.noaa;


import org.ramadda.data.point.*;
import org.ramadda.data.point.text.*;
import org.ramadda.data.record.*;

import org.ramadda.util.Station;
import org.ramadda.util.Utils;

import ucar.unidata.util.StringUtil;

import java.io.*;



import java.util.ArrayList;
import java.util.Hashtable;
import java.util.List;


/**
 */
public class NcdcClimatePointFile extends CsvFile {

    /** _more_          */
    private Hashtable<String, String> idToLabel;


    /**
     * ctor
     *
     *
     * @param filename _more_
     *
     * @throws IOException On badness
     */
    public NcdcClimatePointFile(String filename) throws IOException {
        super(filename);
        //Gack, need to move these into a properties file
        idToLabel = Utils.makeMap(
            "cldd", "Cooling degree days", "dp10",
            "# of days in month precipitation >= 1.0 inch",
            "dp05",
            "# of days in month precipitation >=  0.5 inch",
            "dp01",
            "# of days in month precipitation >=  0.1 inch",
            "htdd", 
            "Heating degree days", 
            "dt00",
            "# days in month min temperature <= to 0.0 F ",
            "dt32",
            "# days in month min temperature <= to 32.0 F ",
            "dt90",
            "# days in month max temperature >= to 90.0 F ",
            "dx32",
            "# days in month with maximum temperature less than or equal to 32.0 F ",
            "emxp", "Extreme maximum daily precipitation total within month",
            "mxsd", "Maximum snow depth reported during month", "tpcp",
            "Total precipitation amount for the month", "tsnw",
            "Total snow fall amount for the month", "emnt",
            "Extreme minimum temperature", "emxt",
            "Extreme maximum temperature", "mmnt",
            "Monthly mean minimum temperature", "mmxt",
            "Monthly mean maximum temperature", "mntm",
            "Monthly mean temperature     ");



    }


    /**
     * _more_
     *
     * @param visitInfo _more_
     *
     * @return _more_
     *
     *
     * @throws Exception _more_
     */
    @Override
    public VisitInfo prepareToVisit(VisitInfo visitInfo) throws Exception {
        putProperty(PROP_SKIPLINES, "1");
        super.prepareToVisit(visitInfo);
        List<String> headerLines = getHeaderLines();

        //STATION,STATION_NAME,ELEVATION,LATITUDE,LONGITUDE,DATE,EMXP,Missing,Consecutive Missing,MXSD,Missing,Consecutive Missing,TPCP,Missing,Consecutive Missing,TSNW,Missing,Consecutive Missing,EMXT,Missing,Consecutive Missing,EMNT,Missing,Consecutive Missing,MMXT,Missing,Consecutive Missing,MMNT,Missing,Consecutive Missing,MNTM,Missing,Consecutive Missing

        List<String> fields         = new ArrayList<String>();
        String       lastFieldId    = "";
        String       lastFieldLabel = "";
        String[]     textFields     = { "station", "station_name", };


        for (String tok : StringUtil.split(headerLines.get(0), ",")) {
            String fieldId = tok.toLowerCase().replace(" ", "_");
            String label   = idToLabel.get(fieldId);

            if (label == null) {
                label = StringUtil.camelCase(tok);
            }

            boolean chartable = true;
            boolean searchable = true;

            if (fieldId.indexOf("missing") >= 0) {
                chartable = false;
                fieldId = lastFieldId + "_" + fieldId;
                label   = lastFieldLabel + " " + label;
            } else {
                lastFieldId    = fieldId;
                lastFieldLabel = label;
            }


            boolean didString = false;

            for (String textField : textFields) {
                if (fieldId.equals(textField)) {
                    fields.add(makeField(fieldId, attrType(TYPE_STRING),
                                         attrLabel(label)));
                    didString = true;

                    break;
                }
            }
            if (didString) {
                continue;
            }

            if (fieldId.equals("date")) {
                fields.add(makeField(fieldId, attrType(TYPE_DATE),
                                     attrFormat("yyyyMMdd"),
                                     attrMissing(9999), attrLabel(label)));
            } else {
                fields.add(makeField(fieldId, attrMissing(9999),
                                     attrLabel(label), chartable?attrChartable():"",
                                     attrSearchable()));
            }
        }


        putProperty(PROP_FIELDS, makeFields(fields));

        return visitInfo;
    }

    /**
     * _more_
     *
     * @param args _more_
     */
    public static void main(String[] args) {
        PointFile.test(args, NcdcClimatePointFile.class);
    }

}
