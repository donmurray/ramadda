/*
 * Copyright 2010 UNAVCO, 6350 Nautilus Drive, Boulder, CO 80301
 * http://www.unavco.org
 *
 * This library is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation; either version 2.1 of the License, or (at
 * your option) any later version.
 *
 * This library is distributed in the hope that it will be useful, but
 * WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU Lesser
 * General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this library; if not, write to the Free Software Foundation,
 * Inc., 59 Temple Place, Suite 330, Boston, MA 02111-1307 USA
 * 
 */

package org.ramadda.data.point.text;

import org.ramadda.data.record.*;
import org.ramadda.data.point.*;

import org.ramadda.util.Station;
import java.io.*;
import java.util.Date;

import ucar.unidata.util.StringUtil;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;




/** This is generated code from generate.tcl. Do not edit it! */
public class TextRecord extends PointRecord {

    /** _more_          */
    public static final int ATTR_FIRST =
        org.ramadda.data.point.PointRecord.ATTR_LAST;

    /** _more_          */
    private String delimiter = ",";

    private boolean delimiterIsSpace = false;

    /** _more_          */
    private List<RecordField> fields;

    /** _more_          */
    private double[] values;

    private Object[] objectValues;

    private String[] tokens;
    private boolean[] hasDefault;
    private boolean[] skip;
    private boolean[] synthetic;

    private String     line   = "";

    
    /** _more_          */
    private int idxX;

    /** _more_          */
    private int idxY;

    /** _more_          */
    private int idxZ;

    private int idxTime;

    private int idxRed=-1;
    private int idxGreen=-1;
    private int idxBlue=-1;

    private int badCnt = 0;
    private int goodCnt = 0;

    /**
     * _more_
     *
     * @param that _more_
     */
    public TextRecord(TextRecord that) {
        super(that);
        this.fields = that.fields;
        values      = null;
        objectValues = null;
        tokens      = null;
    }


    /**
     * _more_
     *
     * @param file _more_
     * @param fields _more_
     */
    public TextRecord(RecordFile file, List<RecordField> fields) {
        super(file);
        initFields(fields);
    }


    /**
     * _more_
     *
     * @param file _more_
     */
    public TextRecord(RecordFile file) {
        super(file);
    }

    /**
     * _more_
     *
     * @param file _more_
     * @param bigEndian _more_
     */
    public TextRecord(RecordFile file, boolean bigEndian) {
        super(file, bigEndian);
    }


    public String getLine() {
        return line;
    }

    /**
     *  Set the Delimiter property.
     *
     *  @param value The new value for Delimiter
     */
    public void setDelimiter(String value) {
        delimiter = value;
        if(!delimiter.equals("\t") && delimiter.trim().length()==0) delimiterIsSpace = true;
        else delimiterIsSpace = false;
    }

    /**
     * _more_
     *
     * @return _more_
     */
    public String getFileDelimiter() {
        return delimiter;
    }


    /**
     *  Get the Delimiter property.
     *
     *  @return The Delimiter
     */
    public String getDelimiter() {
        return delimiter;
    }

    /**
     * _more_
     *
     * @param fields _more_
     */
    private void initFields(List<RecordField> fields) {
        String timeField = (String) getRecordFile().getProperty("field.time");
        String timeFormat = (String) getRecordFile().getProperty("field.time.format");

        String latField = (String) getRecordFile().getProperty("field.latitude");
        String lonField = (String) getRecordFile().getProperty("field.longitude");
        this.fields = fields;
        values      = new double[fields.size()];
        objectValues      = new Object[fields.size()];
        hasDefault = new boolean[fields.size()];
        skip = new boolean[fields.size()];
        synthetic = new boolean[fields.size()];
        int [] timeIndices = {-1,-1,-1,-1,-1,-1};
        boolean gotDateFields = false;
        String[][] timeFields = {{"year","yyyy"},
                                 {"month"},
                                 {"day","dom"},
                                 {"hour","hr"},
                                 {"minute"},
                                 {"second"},};

        idxX    = idxY = idxZ = idxTime = -1;
        int numFields = 0;
        boolean seenLon = false;
        boolean seenLat = false;
        for (int i = 0; i < fields.size(); i++) {
            RecordField field = fields.get(i);
            hasDefault[i] = field.hasDefaultValue();
            skip[i] = field.getSkip();
            synthetic[i] = field.getSynthetic();
            if(!synthetic[i] && !skip[i] && !hasDefault[i]) {
                numFields++;
            }
            if(field.isTypeDate() && idxTime==-1) {
                idxTime = i;
                continue;
            }
            String      name  = field.getName().toLowerCase();
            for(int timeIdx=0;timeIdx<timeFields.length;timeIdx++) {
                boolean gotOne = false;
                for(String timeFieldName: timeFields[timeIdx]) {
                    if(name.equals(timeFieldName)) {
                        gotDateFields = true;
                        //                        System.err.println("got time:" + name + " idx:" + i);
                        timeIndices[timeIdx] =  i+1;
                        gotOne = true;
                        break;
                    }
                    if(gotOne) break;
                }
            }
            if(latField!=null && latField.equalsIgnoreCase(name)) {
                idxY = i;
                continue;
            }
            if(lonField!=null && lonField.equalsIgnoreCase(name)) {
                idxX = i;
                continue;
            }
            if (name.equals("red") || name.equals("r")) {
                idxRed = i;
            } else   if (name.equals("green") || name.equals("g")) {
                idxGreen = i;
            } else   if (name.equals("blue") || name.equals("b")) {
                idxBlue = i;
            } else if (name.equals("x")) {
                if (idxX == -1) {
                    idxX = i;
                }
            } else if (name.equals("longitude")
                    || name.equals("long") || name.equals("lon")) {
                if (!seenLon) {
                    idxX = i;
                    seenLon = true;
                }
            } else if (name.equals("y")) {
                if (idxY == -1) {
                    idxY = i;
                }

            } else if (name.equals("latitude")
                       || name.equals("lat")) {
                if (!seenLat) {
                    idxY = i;
                    seenLat = true;
                }
            } else if (name.equals("z") || name.equals("altitude")
                       || name.equals("elevation") || name.equals("elev")
                       || name.equals("alt")) {
                if (idxZ == -1) {
                    idxZ = i;
                }
            }
        }

        //timeField

        if(gotDateFields) {
            getRecordFile().setDateIndices(timeIndices);
        }

        tokens      = new String[numFields];

        if (idxX == -1) {
            throw new IllegalArgumentException(
                "Could not find x index, e.g., longitude, lon, x, etc.");
        }
        if (idxY == -1) {
            throw new IllegalArgumentException(
                "Could not find y index, e.g., latitude, lat, y, etc.");
        }
    }


    @Override
    public boolean hasRecordTime() {
        if(super.hasRecordTime()) return true;
        return idxTime>=0;
    }


    @Override
    public long getRecordTime() {
        if(idxTime>=0) {
            return ((Date)objectValues[idxTime]).getTime();
        }
        return super.getRecordTime();
    }

    public short[] getRgb() {
        if(idxRed>=0 && idxGreen>=0 && idxBlue>=0) {
            return new short[]{(short)values[idxRed],
                               (short)values[idxGreen],
                               (short)values[idxBlue]};
        }
        return null;
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
        idx = idx-1;
        if ((idx >= 0) && (idx < values.length)) {
            return values[idx];
        }
        return super.getValue(attrId);
    }


    public void setValue(int attrId, double value) {
        int idx = attrId - ATTR_FIRST;
        //Offset since the  field ids are 1 based not 0 based
        idx = idx-1;
        values[idx] = value;
    }


    @Override
    public String getStringValue(int attrId) {
        int idx = attrId - ATTR_FIRST;
        //Offset since the  field ids are 1 based not 0 based
        idx = idx-1;
        if ((idx >= 0) && (idx < values.length)) {
            //Maybe just a number
            if(objectValues[idx] == null) {
                return ""+values[idx];
            }

            return objectValues[idx].toString();
        }
        return super.getStringValue(attrId);
    }


    @Override
    public Object getObjectValue(int attrId) {
        int idx = attrId - ATTR_FIRST;
        //Offset since the  field ids are 1 based not 0 based
        idx = idx-1;
        if ((idx >= 0) && (idx < values.length)) {
            return objectValues[idx];
        }
        return super.getObjectValue(attrId);
    }





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
        try {
            while (true) {
                line = recordIO.readLine();
                if (line == null) {
                    return ReadStatus.EOF;
                }
                line = line.trim();
                if(!lineOk(line)) {
                    continue;
                }
                break;
            }
            if(!split(line)) {
                //throw new IllegalArgumentException("Could not tokenize line:" + line);
                return ReadStatus.SKIP;
            }
            int tokenCnt = 0;
            for (int fieldCnt = 0; fieldCnt < fields.size(); fieldCnt++) {
                RecordField field = fields.get(fieldCnt);
                if(skip[fieldCnt]) {
                    continue;
                }
                if(hasDefault[fieldCnt]) {
                    if(field.isTypeString()) {
                        objectValues[fieldCnt] = field.getDefaultStringValue();
                    } else if(field.isTypeDate()) {
                        String dttm = field.getDefaultStringValue();
                        objectValues[fieldCnt] = field.getDateFormat().parse(dttm);
                    } else {
                        values[fieldCnt] = field.getDefaultDoubleValue();
                    }
                    continue;
                }

                //                System.err.println ("field:" + field +" " + tok);
                if(synthetic[fieldCnt]) {
                    continue;
                }


                String tok = tokens[tokenCnt++];
                if(field.isTypeString()) {
                    objectValues[fieldCnt] = tok;
                    continue;
                }
                if(field.isTypeDate()) {
                    tok = tok.replaceAll("\"", "");
                    try {
                        objectValues[fieldCnt] = field.getDateFormat().parse(tok);
                    } catch(java.text.ParseException ignore) {
                        objectValues[fieldCnt] = field.getDateFormat().parse(tok+" UTC");
                    }
                    continue;
                }
                if(tok == null)  {
                    System.err.println("tok null: " +tokenCnt +" " +line);
                }
                //Check for the riscan NaN
                if(isMissingValue(field, tok)) {
                    values[fieldCnt] = Double.NaN;
                } else {
                    values[fieldCnt] = (double) Double.parseDouble(tok);
                    if(isMissingValue(field, values[fieldCnt])) {
                        values[fieldCnt] = Double.NaN;
                    } 
                    
                }
            }

            setLocation(values[idxX], values[idxY], ((idxZ >= 0)
                                                     ? values[idxZ]
                                                     : 0));
            convertedXYZToLatLonAlt = true;

            if(idxTime>=0) {
                setRecordTime(getRecordTime());
            }

            return ReadStatus.OK;
        } catch (Exception exc) {
            System.err.println("Line:" + line);
            throw new RuntimeException(exc);
        }

    }


    public void setLocation(Station station) {
        this.setLocation(station.getLongitude(),
                         station.getLatitude(),
                         station.getElevation());
        if(idxX>=0) 
            this.setValue(idxX, station.getLongitude());
        if(idxY>=0)
            this.setValue(idxY, station.getLatitude());
        if(idxZ>=0) 
            this.setValue(idxZ, station.getElevation());

    }



    public boolean lineOk(String line) {
        if (line.length() == 0 || line.startsWith("#")) {
            return false;
        }
        return true;
    }



    public boolean split(String sourceString) {
        int length   = 1;
        int fullTokenCnt = 0;
        int tokenCnt= 0;
        int fromIndex=0;
        int sourceLength = sourceString.length();
        //        System.err.println ("line:" + sourceString);
        while (true) {
            int    idx = sourceString.indexOf(delimiter, fromIndex);
            //            System.err.println ("\tidx:" + idx +" delimiter:" + delimiter +":  str:" + sourceString);
            String theString;
            if (idx < 0) {
                theString    = sourceString.substring(fromIndex);
            } else {
                theString    = sourceString.substring(fromIndex, idx);
                if(delimiterIsSpace) {
                    while(sourceString.charAt(idx)==' ' && idx<sourceLength) {
                        idx++;
                    }
                    fromIndex = idx;
                } else {
                    fromIndex = idx+length;
                }
            }
            //            System.err.println ("\ttokens[" + tokenCnt +"] = " + theString);
            tokens[tokenCnt++] = theString.trim();
            if (idx < 0 || tokenCnt == tokens.length) {
                break;
            }
        }
        if(tokenCnt!=tokens.length) {
            badCnt++;
            //            System.exit(0);
            if(badCnt>10 && goodCnt<=0) {
                System.err.println ("bad token cnt: expected:" + tokens.length + " read:" + tokenCnt +" delimiter:" + delimiter +" is space:" + delimiterIsSpace +"\nLine:" + line);

                throw new IllegalArgumentException("Could not tokenize line:" + line);
                //                return false;
            }
            return false;
        }
        badCnt = 0;
        goodCnt++;
        return true;
        
        /*
        System.err.println("line:" + sourceString);
        for(String tok: tokens) {
            System.err.println("tok:" + tok);
        }
        */
    }


    /** _more_          */
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
            if(recordField.getSkip()) continue;

            if (cnt > 0) {
                pw.print(',');
            }
            cnt++;

            if(recordField.isTypeString()) {
                pw.print(getStringValue(recordField.getParamId()));
                continue;
            } 

            double  value  = values[fieldCnt];



            if(recordField.isTypeInteger()) {
                int v = (int) value;
                pw.print(v);
                continue;
            } 


            if(fieldCnt == idxX)
                value = getLongitude();
            else  if(fieldCnt == idxY)
                value = getLatitude();
            else  if(fieldCnt == idxZ)
                value = getAltitude();


            double roundingFactor = recordField.getRoundingFactor();
            if(roundingFactor>0) {
                double nv = Math.round(value*roundingFactor)/roundingFactor;
                value = nv;
            }


            pw.print(value);
        }
        return fields.size() + superCnt;
    }


    public static void main(String[]args) {
        int precision = 4;
        double value = 1.23456789;
        //        double nv = Math.round(value * factor) / factor;
        double factor =  Math.pow(10,precision);
        double nv = Math.round(value*factor)/factor;
        System.err.println(factor);
        System.err.println(nv);
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
            int cnt = 0;
            RecordField recordField = fields.get(i);
            if(recordField.getSkip()) continue;
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
     */
    public void print(Appendable buff) throws Exception {
        super.print(buff);
        for (int i = 0; i < fields.size(); i++) {
            if(fields.get(i).getSkip()) continue;
            System.out.println(fields.get(i).getName() + ":" + values[i]
                               + " ");
        }
    }




}
