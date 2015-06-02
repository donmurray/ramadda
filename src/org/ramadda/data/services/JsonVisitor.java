/*
 * Copyright (c) 2008-2015 Geode Systems LLC
 * This Software is licensed under the Geode Systems RAMADDA License available in the source distribution in the file 
 * ramadda_license.txt. The above copyright notice shall be included in all copies or substantial portions of the Software.
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
import ucar.unidata.util.DateUtil;
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
        boolean addGeo = file.getProperty("output.latlon", true) && pointRecord.isValidPosition();
        boolean addTime = file.getProperty("output.time", true) && pointRecord.hasRecordTime();
        if (fields == null) {
            pw     = getThePrintWriter();
            fields = record.getFields();
            RecordField.addJsonHeader(
                pw, mainEntry.getName(), fields,
                addGeo,
                addTime);
        }

        int fieldCnt = 0;
        if (cnt > 0) {
            pw.append(COMMA);
        }

        pw.append(Json.mapOpen());

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
                    svalue = record.getStringValue(field.getParamId());
                    svalue = Json.quote(svalue);
                } else {
                    double value = record.getValue(field.getParamId());
                    svalue = Json.formatNumber(value);
                }
            } else {
                if (field.isTypeString()) {
                    svalue = getter.getStringValue(record, field, visitInfo);
                    svalue = Json.quote(svalue);
                } else {
                    svalue = Json.formatNumber(getter.getValue(record, field, visitInfo));
                }
            }
            if (fieldCnt > 0) {
                pw.append(COMMA);
            }
            pw.append(svalue);
            fieldCnt++;
        }


            if (addGeo) {
                pw.append(COMMA);
                pw.append(Json.formatNumber(pointRecord.getLatitude()));
                pw.append(COMMA);
                pw.append(Json.formatNumber(pointRecord.getLongitude()));
                pw.append(COMMA);
                pw.append(Json.formatNumber(pointRecord.getAltitude()));
            }
            if (addTime) {
                pw.append(COMMA);
                //                pw.append(Json.quote(DateUtil.getTimeAsISO8601(pointRecord.getRecordTime())));
                //Just use the milliseconds
                pw.append(Json.formatNumber(pointRecord.getRecordTime()));
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
