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

package org.ramadda.data.services;


import org.ramadda.data.point.*;
import org.ramadda.data.point.binary.*;

import org.ramadda.data.record.*;


import org.ramadda.data.record.*;
import org.ramadda.data.record.filter.*;

import org.ramadda.repository.*;
import org.ramadda.repository.auth.*;
import org.ramadda.repository.job.*;
import org.ramadda.repository.map.*;
import org.ramadda.repository.metadata.*;
import org.ramadda.repository.output.*;
import org.ramadda.repository.type.TypeHandler;
import org.ramadda.util.ColorTable;
import org.ramadda.util.HtmlUtils;
import org.ramadda.util.Json;
import org.ramadda.util.SelectionRectangle;

import org.ramadda.util.TempDir;
import org.ramadda.util.Utils;

import org.ramadda.util.grid.*;

import org.w3c.dom.*;


import ucar.ma2.DataType;

import ucar.nc2.Attribute;
//import ucar.nc2.ft.point.writer.CFPointObWriter;
//import ucar.nc2.ft.point.writer.PointObVar;
import ucar.nc2.dt.point.CFPointObWriter;
import ucar.nc2.dt.point.PointObVar;

import ucar.unidata.data.gis.KmlUtil;


import ucar.unidata.ui.ImageUtils;
import ucar.unidata.util.IOUtil;
import ucar.unidata.util.Misc;
import ucar.unidata.util.StringUtil;
import ucar.unidata.xml.XmlUtil;

import java.awt.*;
import java.awt.geom.Rectangle2D;

import java.awt.image.*;

import java.io.*;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.List;


import java.util.zip.*;


/**
 * Class description
 *
 *
 * @version        $version$, Thu, Jan 2, '14
 * @author         Enter your name here...
 */
public class JsonVisitor extends BridgeRecordVisitor {

    /** _more_ */
    private static final String COMMA = ",\n";

    /** _more_ */
    private int cnt = 0;

    /** _more_ */
    private List<RecordField> fields;

    /** _more_ */
    private PrintWriter pw;


    /**
     * _more_
     *
     * @param handler _more_
     * @param request _more_
     * @param processId _more_
     * @param mainEntry _more_
     * @param suffix _more_
     */
    public JsonVisitor(RecordOutputHandler handler, Request request,
                       Object processId, Entry mainEntry, String suffix) {
        super(handler, request, processId, mainEntry, suffix);
    }

    /**
     * _more_
     *
     * @param file _more_
     * @param visitInfo _more_
     * @param record _more_
     *
     * @return _more_
     *
     * @throws Exception _more_
     */
    public boolean doVisitRecord(RecordFile file, VisitInfo visitInfo,
                                 Record record)
            throws Exception {

        if ( !getHandler().jobOK(getProcessId())) {
            return false;
        }
        PointRecord pointRecord = (PointRecord) record;
        if (fields == null) {
            pw     = getThePrintWriter();
            fields = record.getFields();
            RecordField.addJsonHeader(pw, mainEntry.getName(), fields);
        }

        int fieldCnt = 0;
        if (cnt > 0) {
            pw.append(COMMA);
        }

        pw.append(Json.mapOpen());
        if (pointRecord.isValidPosition()) {
            Json.addGeolocation(pw, pointRecord.getLatitude(),
                                pointRecord.getLongitude(),
                                pointRecord.getAltitude());
        }
        pw.append(COMMA);
        if (pointRecord.hasRecordTime()) {
            pw.append(Json.attr(Json.FIELD_DATE,
                                pointRecord.getRecordTime()));
        } else {
            pw.append(Json.attr(Json.FIELD_DATE, Json.NULL, false));
        }
        pw.append(COMMA);

        pw.append(Json.mapKey(Json.FIELD_VALUES));
        pw.append(Json.listOpen());
        for (RecordField field : fields) {
            if (field.getSynthetic()) {
                continue;
            }
            if (field.getArity() > 1) {
                continue;
            }
            String      svalue;
            ValueGetter getter = field.getValueGetter();
            if (getter == null) {
                if (field.isTypeString()) {
                    svalue = HtmlUtils.quote(
                        record.getStringValue(field.getParamId()));
                } else {
                    double value = record.getValue(field.getParamId());
                    svalue = Json.formatNumber(value);
                }
            } else {
                if (field.isTypeString()) {
                    svalue = HtmlUtils.quote(getter.getStringValue(record,
                            field, visitInfo));
                } else {
                    svalue = Json.formatNumber(getter.getValue(record, field,
                            visitInfo));
                }
            }
            if (fieldCnt > 0) {
                pw.append(COMMA);
            }
            pw.append(svalue);
            fieldCnt++;
        }
        pw.append(Json.listClose());
        pw.append(Json.mapClose());
        cnt++;

        return true;
    }

    /**
     * _more_
     *
     * @param file _more_
     * @param visitInfo _more_
     *
     * @throws Exception _more_
     */
    @Override
    public void finished(RecordFile file, VisitInfo visitInfo)
            throws Exception {
        super.finished(file, visitInfo);
        if (pw != null) {
            RecordField.addJsonFooter(pw);
        }
    }

}
