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
package org.ramadda.data.point.netcdf;


import org.ramadda.data.point.*;

import org.ramadda.data.record.*;

import ucar.unidata.util.StringUtil;

import java.io.*;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;


/**
 * Class description
 *
 *
 * @version        $version$, Fri, Aug 23, '13
 * @author         Enter your name here...    
 */
public class NetcdfRecord extends PointRecord {

    /** _more_ */
    public static final int ATTR_FIRST =
        org.ramadda.data.point.PointRecord.ATTR_LAST;

    /** _more_ */
    private List<RecordField> fields;

    /** _more_ */
    private double[] values;

    /** _more_          */
    private Object[] objectValues;

    /** _more_ */
    private int idxX;

    /** _more_ */
    private int idxY;

    /** _more_ */
    private int idxZ;


    /** _more_ */
    private boolean convertedXYZToLatLonAlt = false;

    /**
     * _more_
     *
     * @param that _more_
     */
    public NetcdfRecord(NetcdfRecord that) {
        super(that);
        this.fields  = that.fields;
        values       = null;
        objectValues = null;
    }


    /**
     * _more_
     *
     * @param file _more_
     * @param fields _more_
     */
    public NetcdfRecord(RecordFile file, List<RecordField> fields) {
        super(file);
        initFields(fields);
    }


    /**
     * _more_
     *
     * @param file _more_
     */
    public NetcdfRecord(RecordFile file) {
        super(file);
    }

    /**
     * _more_
     *
     * @param file _more_
     * @param bigEndian _more_
     */
    public NetcdfRecord(RecordFile file, boolean bigEndian) {
        super(file, bigEndian);
    }




    /**
     * _more_
     *
     * @param fields _more_
     */
    private void initFields(List<RecordField> fields) {
        this.fields  = fields;
        values       = new double[fields.size()];
        objectValues = new Object[fields.size()];
        idxX         = idxY = idxZ = -1;
        for (int i = 0; i < fields.size(); i++) {
            RecordField field = fields.get(i);
            String      name  = field.getName().toLowerCase();
        }

        if (idxX == -1) {
            throw new IllegalArgumentException(
                "Could not find x index, e.g., longitude, lon, x, etc.");
        }
        if (idxY == -1) {
            throw new IllegalArgumentException(
                "Could not find y index, e.g., latitude, lat, y, etc.");
        }
    }


    /**
     * _more_
     *
     * @return _more_
     */
    public int getLastAttribute() {
        return fields.get(fields.size() - 1).getParamId();
    }


    /**
     * _more_
     *
     * @param fields _more_
     */
    protected void addFields(List<RecordField> fields) {
        super.addFields(fields);
        fields.addAll(this.fields);
    }


    /**
     * _more_
     *
     * @param attrId _more_
     *
     * @return _more_
     */
    public double getValue(int attrId) {
        int idx = attrId - ATTR_FIRST;
        //Offset since the  field ids are 1 based not 0 based
        idx = idx - 1;
        if ((idx >= 0) && (idx < values.length)) {
            return values[idx];
        }

        return super.getValue(attrId);
    }


    /**
     * _more_
     *
     * @param attrId _more_
     *
     * @return _more_
     */
    public String getStringValue(int attrId) {
        int idx = attrId - ATTR_FIRST;
        //Offset since the  field ids are 1 based not 0 based
        idx = idx - 1;
        if ((idx >= 0) && (idx < values.length)) {
            return objectValues[idx].toString();
        }

        return super.getStringValue(attrId);
    }



    /** _more_          */
    private int skipCnt = 0;

    /**
     * _more_
     *
     * @param recordIO _more_
     *
     * @return _more_
     *
     * @throws IOException _more_
     */
    public ReadStatus read(RecordIO recordIO) throws IOException {
        ReadStatus status = ReadStatus.OK;
        //        setLocation(values[idxX], values[idxY], ((idxZ >= 0)
        //                ? values[idxZ]
        //                : 0));
        convertedXYZToLatLonAlt = true;

        return status;
    }


    /**
     * _more_
     */
    public void convertXYZToLatLonAlt() {
        convertedXYZToLatLonAlt = true;
        if (idxX >= 0) {
            values[idxX] = getLongitude();
        }
        if (idxY >= 0) {
            values[idxY] = getLatitude();
        }
        if (idxZ >= 0) {
            values[idxZ] = getAltitude();
        }
    }

    /**
     * _more_
     *
     * @param visitInfo _more_
     * @param pw _more_
     *
     * @return _more_
     */
    public int doPrintCsv(VisitInfo visitInfo, PrintWriter pw) {
        int superCnt = super.doPrintCsv(visitInfo, pw);
        if (superCnt > 0) {
            pw.print(',');
        }
        int cnt = 0;
        for (int fieldCnt = 0; fieldCnt < values.length; fieldCnt++) {
            RecordField recordField = fields.get(fieldCnt);
            if (recordField.getSkip()) {
                continue;
            }

            if (cnt > 0) {
                pw.print(',');
            }
            cnt++;

            if (recordField.isTypeString()) {
                pw.print(getStringValue(recordField.getParamId()));

                continue;
            }

            double value = values[fieldCnt];
            if (fieldCnt == idxX) {
                value = getLongitude();
            } else if (fieldCnt == idxY) {
                value = getLatitude();
            } else if (fieldCnt == idxZ) {
                value = getAltitude();
            }
            pw.print(value);
        }

        return fields.size() + superCnt;
    }


    /**
     * _more_
     *
     * @param visitInfo _more_
     * @param pw _more_
     *
     * @return _more_
     */
    public int doPrintCsvHeader(VisitInfo visitInfo, PrintWriter pw) {
        int superCnt = super.doPrintCsvHeader(visitInfo, pw);
        int myCnt    = 0;
        if (superCnt > 0) {
            pw.print(',');
        }
        for (int i = 0; i < fields.size(); i++) {
            int         cnt         = 0;
            RecordField recordField = fields.get(i);
            if (recordField.getSkip()) {
                continue;
            }
            if (cnt > 0) {
                pw.print(',');
            }
            cnt++;
            if (convertedXYZToLatLonAlt) {
                if (i == idxX) {
                    pw.append("longitude[unit=\"degrees\"]");

                    continue;
                }
                if (i == idxY) {
                    pw.append("latitude[unit=\"degrees\"]");

                    continue;
                }
                if (i == idxZ) {
                    pw.append("altitude[unit=\"m\"]");

                    continue;
                }
            }
            fields.get(i).printCsvHeader(visitInfo, pw);
        }

        return fields.size() + superCnt;
    }



    /**
     * _more_
     *
     * @param buff _more_
     *
     * @throws Exception _more_
     */
    public void print(Appendable buff) throws Exception {
        super.print(buff);
        for (int i = 0; i < fields.size(); i++) {
            if (fields.get(i).getSkip()) {
                continue;
            }
            System.out.println(fields.get(i).getName() + ":" + values[i]
                               + " ");
        }
    }




}
