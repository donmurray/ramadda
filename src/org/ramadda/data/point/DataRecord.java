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

package org.ramadda.data.point;


import org.ramadda.data.point.*;

import org.ramadda.data.record.*;


import ucar.unidata.util.StringUtil;

import java.io.*;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashSet;
import java.util.List;




/**
 * Class description
 *
 *
 * @version        $version$, Thu, Oct 31, '13
 * @author         Enter your name here...
 */
public class DataRecord extends PointRecord {

    /** _more_ */
    public static final int ATTR_FIRST =
        org.ramadda.data.point.PointRecord.ATTR_LAST;

    /** _more_ */
    protected List<RecordField> fields;

    /** _more_ */
    protected double[] values;

    /** _more_ */
    protected Object[] objectValues;

    /** _more_ */
    protected int numDataFields = 0;

    /** _more_ */
    protected boolean[] hasDefault;

    /** _more_ */
    protected boolean[] skip;

    /** _more_ */
    protected boolean[] synthetic;

    /** _more_ */
    private boolean positionRequired = true;

    /** _more_ */
    protected int idxX;

    /** _more_ */
    protected int idxY;

    /** _more_ */
    protected int idxZ;

    /** _more_ */
    protected int idxTime;

    /**
     * _more_
     */
    public DataRecord() {}


    /**
     * _more_
     *
     * @param that _more_
     */
    public DataRecord(DataRecord that) {
        super(that);
        this.fields  = that.fields;
        values       = null;
        objectValues = null;
    }

    /**
     * _more_
     *
     * @param file _more_
     */
    public DataRecord(RecordFile file) {
        super(file);
    }



    /**
     * _more_
     *
     * @param file _more_
     * @param fields _more_
     */
    public DataRecord(RecordFile file, List<RecordField> fields) {
        super(file);
        this.fields = fields;
    }




    /**
     * _more_
     *
     * @param fields _more_
     */
    public void initFields(List<RecordField> fields) {

        numDataFields = 0;
        String timeField = (String) getRecordFile().getProperty("field.time");
        String timeFormat =
            (String) getRecordFile().getProperty("field.time.format");

        String latField =
            (String) getRecordFile().getProperty("field.latitude");
        String lonField =
            (String) getRecordFile().getProperty("field.longitude");
        this.fields  = fields;
        values       = new double[fields.size()];
        objectValues = new Object[fields.size()];
        hasDefault   = new boolean[fields.size()];
        skip         = new boolean[fields.size()];
        synthetic    = new boolean[fields.size()];

        int        dateIndex     = -1;
        int        timeIndex     = -1;
        String     dateFormat    = null;

        int[]      ymdhmsIndices = {
            -1, -1, -1, -1, -1, -1
        };
        boolean    gotDateFields = false;
        String[][] timeFields    = {
            { "year", "yyyy", "yy" }, { "month", "MM" },
            { "day", "dom", "dd" }, { "hour", "hr", "hh" },
            { "minute", "mm" }, { "second" },
        };


        positionRequired = getRecordFile().getProperty("position.required",
                "true").equals("true");
        idxX = idxY = idxZ = idxTime = -1;
        boolean seenLon = false;
        boolean seenLat = false;

        for (int fieldIdx = 0; fieldIdx < fields.size(); fieldIdx++) {
            RecordField field = fields.get(fieldIdx);
            hasDefault[fieldIdx] = field.hasDefaultValue();
            skip[fieldIdx]       = field.getSkip();
            synthetic[fieldIdx]  = field.getSynthetic();
            if ( !synthetic[fieldIdx] && !skip[fieldIdx]
                    && !hasDefault[fieldIdx]) {
                numDataFields++;
            }
            if (field.isTypeDate() && (idxTime == -1)) {
                idxTime = fieldIdx;

                continue;
            }
            String casedName = field.getName();
            String name      = casedName.toLowerCase();

            if (field.getIsDate()) {
                dateIndex = fieldIdx + 1;
            }
            if (field.getIsTime()) {
                timeIndex = fieldIdx + 1;
            }

            for (int timeIdx = 0; timeIdx < timeFields.length; timeIdx++) {
                boolean gotOne = false;
                for (String timeFieldName : timeFields[timeIdx]) {
                    if (casedName.equals(timeFieldName)) {
                        gotDateFields          = true;
                        ymdhmsIndices[timeIdx] = fieldIdx + 1;
                        gotOne                 = true;

                        break;
                    }
                }
                if (gotOne) {
                    break;
                }
            }
            if ((latField != null) && latField.equalsIgnoreCase(name)) {
                idxY = fieldIdx;

                continue;
            }
            if ((lonField != null) && lonField.equalsIgnoreCase(name)) {
                idxX = fieldIdx;

                continue;
            }

            if (name.equals("x")) {
                if (idxX == -1) {
                    idxX = fieldIdx;
                }
            } else if (name.equals("longitude") || name.equals("long")
                       || name.equals("lon")) {
                if ( !seenLon) {
                    idxX    = fieldIdx;
                    seenLon = true;
                }
            } else if (name.equals("y")) {
                if (idxY == -1) {
                    idxY = fieldIdx;
                }

            } else if (name.equals("latitude") || name.equals("lat")) {
                if ( !seenLat) {
                    idxY    = fieldIdx;
                    seenLat = true;
                }
            } else if (name.equals("z") || name.equals("altitude")
                       || name.equals("elevation") || name.equals("elev")
                       || name.equals("alt")) {
                if (idxZ == -1) {
                    idxZ = fieldIdx;
                }
            }
        }

        //timeField

        if ((dateIndex >= 0) || (timeIndex >= 0)) {
            getRecordFile().setDateTimeIndex(dateIndex, timeIndex);
        }

        if (gotDateFields) {
            getRecordFile().setYMDHMSIndices(ymdhmsIndices);
        }


        checkIndices();




    }


    /**
     * _more_
     *
     * @param field _more_
     */
    public static void initField(RecordField field) {
        field.setValueGetter(new ValueGetter() {
            public double getValue(Record record, RecordField field,
                                   VisitInfo visitInfo) {
                DataRecord dataRecord = (DataRecord) record;

                return dataRecord.getValue(field.getParamId());
            }
            public String getStringValue(Record record, RecordField field,
                                         VisitInfo visitInfo) {
                DataRecord dataRecord = (DataRecord) record;

                return dataRecord.getStringValue(field.getParamId());
            }
        });
    }



    /**
     * _more_
     *
     * @return _more_
     */
    public boolean needsValidPosition() {
        return positionRequired;
    }


    /**
     * _more_
     */
    public void checkIndices() {
        if (positionRequired) {
            if (idxX == -1) {
                throw new IllegalArgumentException(
                    "Could not find x index, e.g., longitude, lon, x, etc.");
            }
            if (idxY == -1) {
                throw new IllegalArgumentException(
                    "Could not find y index, e.g., latitude, lat, y, etc.");
            }
        }
    }

    /**
     * _more_
     *
     * @return _more_
     */
    @Override
    public boolean hasRecordTime() {
        if (super.hasRecordTime()) {
            return true;
        }

        return idxTime >= 0;
    }


    /**
     * _more_
     *
     * @return _more_
     */
    @Override
    public long getRecordTime() {
        if (idxTime >= 0) {
            return ((Date) objectValues[idxTime]).getTime();
        }

        return super.getRecordTime();
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
     * @param value _more_
     */
    public void setValue(int attrId, double value) {
        int idx = attrId - ATTR_FIRST;
        //Offset since the  field ids are 1 based not 0 based
        idx         = idx - 1;
        values[idx] = value;
    }


    /**
     * _more_
     *
     * @param attrId _more_
     * @param value _more_
     */
    public void setValue(int attrId, Object value) {
        int idx = attrId - ATTR_FIRST;
        //Offset since the  field ids are 1 based not 0 based
        idx               = idx - 1;
        objectValues[idx] = value;
    }


    /**
     * _more_
     *
     * @param attrId _more_
     *
     * @return _more_
     */
    @Override
    public String getStringValue(int attrId) {
        int idx = attrId - ATTR_FIRST;
        //Offset since the  field ids are 1 based not 0 based
        idx = idx - 1;
        if ((idx >= 0) && (idx < values.length)) {
            //Maybe just a number
            if (objectValues[idx] == null) {
                return "" + values[idx];
            }

            return objectValues[idx].toString();
        }

        return super.getStringValue(attrId);
    }


    /**
     * _more_
     *
     * @param attrId _more_
     *
     * @return _more_
     */
    @Override
    public Object getObjectValue(int attrId) {
        int idx = attrId - ATTR_FIRST;
        //Offset since the  field ids are 1 based not 0 based
        idx = idx - 1;

        if ((idx >= 0) && (idx < values.length)) {
            return objectValues[idx];
        }

        return super.getObjectValue(attrId);
    }

    /**
     * _more_
     *
     * @param attrId _more_
     *
     * @return _more_
     */
    @Override
    public boolean hasObjectValue(int attrId) {
        int idx = attrId - ATTR_FIRST;
        //Offset since the  field ids are 1 based not 0 based
        idx = idx - 1;

        if ((idx >= 0) && (idx < values.length)) {
            return objectValues[idx] != null;
        }

        return false;
    }


    /** _more_ */
    private boolean convertedXYZToLatLonAlt = false;

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



            if (recordField.isTypeInteger()) {
                int v = (int) value;
                pw.print(v);

                continue;
            }


            if (fieldCnt == idxX) {
                value = getLongitude();
            } else if (fieldCnt == idxY) {
                value = getLatitude();
            } else if (fieldCnt == idxZ) {
                value = getAltitude();
            }


            double roundingFactor = recordField.getRoundingFactor();
            if (roundingFactor > 0) {
                double nv = Math.round(value * roundingFactor)
                            / roundingFactor;
                value = nv;
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
