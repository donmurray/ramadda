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

package org.ramadda.data.point.binary;


import org.ramadda.data.record.*;

import java.io.*;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;





/** This is generated code from generate.tcl. Do not edit it! */
public class FloatLatLonRecord extends org.ramadda.data.point.PointRecord {

    /** _more_ */
    public static final int ATTR_FIRST =
        org.ramadda.data.point.PointRecord.ATTR_LAST;

    /** _more_ */
    public static final List<RecordField> FIELDS =
        new ArrayList<RecordField>();

    /** _more_ */
    public static final int ATTR_LAT = ATTR_FIRST + 1;

    /** _more_ */
    public static final RecordField RECORDATTR_LAT;

    /** _more_ */
    public static final int ATTR_LON = ATTR_FIRST + 2;

    /** _more_ */
    public static final RecordField RECORDATTR_LON;

    /** _more_ */
    public static final int ATTR_LAST = ATTR_FIRST + 3;


    static {
        FIELDS.add(RECORDATTR_LAT = new RecordField("lat", "lat", "",
                ATTR_LAT, "", "float", "float", 0, SEARCHABLE_NO,
                CHARTABLE_NO));
        RECORDATTR_LAT.setValueGetter(new ValueGetter() {
            public double getValue(Record record, RecordField field,
                                   VisitInfo visitInfo) {
                return (double) ((FloatLatLonRecord) record).lat;
            }
            public String getStringValue(Record record, RecordField field,
                                         VisitInfo visitInfo) {
                return "" + ((FloatLatLonRecord) record).lat;
            }
        });
        FIELDS.add(RECORDATTR_LON = new RecordField("lon", "lon", "",
                ATTR_LON, "", "float", "float", 0, SEARCHABLE_NO,
                CHARTABLE_NO));
        RECORDATTR_LON.setValueGetter(new ValueGetter() {
            public double getValue(Record record, RecordField field,
                                   VisitInfo visitInfo) {
                return (double) ((FloatLatLonRecord) record).lon;
            }
            public String getStringValue(Record record, RecordField field,
                                         VisitInfo visitInfo) {
                return "" + ((FloatLatLonRecord) record).lon;
            }
        });

    }


    /** _more_ */
    float lat;

    /** _more_ */
    float lon;


    /**
     * _more_
     *
     * @param that _more_
     */
    public FloatLatLonRecord(FloatLatLonRecord that) {
        super(that);
        this.lat = that.lat;
        this.lon = that.lon;


    }



    /**
     * _more_
     *
     * @param file _more_
     */
    public FloatLatLonRecord(RecordFile file) {
        super(file);
    }



    /**
     * _more_
     *
     * @param file _more_
     * @param bigEndian _more_
     */
    public FloatLatLonRecord(RecordFile file, boolean bigEndian) {
        super(file, bigEndian);
    }



    /**
     * _more_
     *
     * @return _more_
     */
    public int getLastAttribute() {
        return ATTR_LAST;
    }



    /**
     * _more_
     *
     * @param object _more_
     *
     * @return _more_
     */
    public boolean equals(Object object) {
        if ( !super.equals(object)) {
            System.err.println("bad super");

            return false;
        }
        if ( !(object instanceof FloatLatLonRecord)) {
            return false;
        }
        FloatLatLonRecord that = (FloatLatLonRecord) object;
        if (this.lat != that.lat) {
            System.err.println("bad lat");

            return false;
        }
        if (this.lon != that.lon) {
            System.err.println("bad lon");

            return false;
        }

        return true;
    }




    /**
     * _more_
     *
     * @return _more_
     */
    public double getLatitude() {
        return (double) lat;
    }

    /**
     * _more_
     *
     * @return _more_
     */
    public double getLongitude() {
        return (double) lon;
    }

    /**
     * _more_
     *
     * @param fields _more_
     */
    protected void addFields(List<RecordField> fields) {
        super.addFields(fields);
        fields.addAll(FIELDS);
    }



    /**
     * _more_
     *
     * @param attrId _more_
     *
     * @return _more_
     */
    public double getValue(int attrId) {
        if (attrId == ATTR_LAT) {
            return lat;
        }
        if (attrId == ATTR_LON) {
            return lon;
        }

        return super.getValue(attrId);

    }



    /**
     * _more_
     *
     * @return _more_
     */
    public int getRecordSize() {
        return super.getRecordSize() + 8;
    }



    /**
     * _more_
     *
     * @param recordIO _more_
     *
     * @return _more_
     *
     * @throws Exception _more_
     */
    public ReadStatus read(RecordIO recordIO) throws Exception {
        DataInputStream dis    = recordIO.getDataInputStream();
        ReadStatus      status = super.read(recordIO);
        if (status != ReadStatus.OK) {
            return status;
        }
        lat = readFloat(dis);
        lon = readFloat(dis);


        return ReadStatus.OK;
    }



    /**
     * _more_
     *
     * @param recordIO _more_
     *
     * @throws IOException _more_
     */
    public void write(RecordIO recordIO) throws IOException {
        DataOutputStream dos = recordIO.getDataOutputStream();
        super.write(recordIO);
        writeFloat(dos, lat);
        writeFloat(dos, lon);

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
        boolean includeVector = visitInfo.getProperty(PROP_INCLUDEVECTOR,
                                    false);
        int superCnt = super.doPrintCsv(visitInfo, pw);
        int myCnt    = 0;
        if (superCnt > 0) {
            pw.print(',');
        }
        pw.print(lat);
        myCnt++;
        pw.print(',');
        pw.print(lon);
        myCnt++;

        return myCnt + superCnt;

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
        boolean includeVector = visitInfo.getProperty(PROP_INCLUDEVECTOR,
                                    false);
        if (superCnt > 0) {
            pw.print(',');
        }
        RECORDATTR_LAT.printCsvHeader(visitInfo, pw);
        myCnt++;
        pw.print(',');
        RECORDATTR_LON.printCsvHeader(visitInfo, pw);
        myCnt++;

        return myCnt + superCnt;

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
        buff.append(" lat: " + lat + " \n");
        buff.append(" lon: " + lon + " \n");

    }



    /**
     * _more_
     *
     * @return _more_
     */
    public float getLat() {
        return lat;
    }


    /**
     * _more_
     *
     * @param newValue _more_
     */
    public void setLat(float newValue) {
        lat = newValue;
    }


    /**
     * _more_
     *
     * @return _more_
     */
    public float getLon() {
        return lon;
    }


    /**
     * _more_
     *
     * @param newValue _more_
     */
    public void setLon(float newValue) {
        lon = newValue;
    }



}
