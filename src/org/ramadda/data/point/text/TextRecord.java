/*
 * Copyright (c) 2008-2015 Geode Systems LLC
 * This Software is licensed under the Geode Systems RAMADDA License available in the source distribution in the file 
 * ramadda_license.txt. The above copyright notice shall be included in all copies or substantial portions of the Software.
 */

package org.ramadda.data.point.text;


import org.ramadda.data.point.*;

import org.ramadda.data.record.*;

import org.ramadda.util.Station;
import org.ramadda.util.text.*;
import org.ramadda.util.Utils;

import ucar.unidata.util.StringUtil;

import java.io.*;

import java.text.SimpleDateFormat;


import java.util.ArrayList;
import java.util.Date;
import java.util.HashSet;
import java.util.List;




/** This is generated code from generate.tcl. Do not edit it! */
public class TextRecord extends DataRecord {

    /** _more_ */
    public static final int ATTR_FIRST =
        org.ramadda.data.point.PointRecord.ATTR_LAST;

    /** _more_ */
    private String delimiter = ",";

    /** _more_ */
    private boolean delimiterIsSpace = false;

    /** _more_ */
    protected String firstDataLine = null;


    /** _more_ */
    private String[] tokens;

    private Visitor visitor;

    /** _more_ */
    private int[] fixedWidth = null;

    /** _more_ */
    private String currentLine = "";

    /** _more_ */
    private boolean bePickyAboutTokens = true;


    /** _more_ */
    private int badCnt = 0;

    /**
     * _more_
     */
    public TextRecord() {}


    /**
     * _more_
     *
     * @param that _more_
     */
    public TextRecord(TextRecord that) {
        super(that);
        tokens = null;
    }


    /**
     * _more_
     *
     * @param file _more_
     * @param fields _more_
     */
    public TextRecord(RecordFile file, List<RecordField> fields) {
        super(file, fields);
        initFields(fields);

    }

    /**
     * _more_
     *
     * @param fields _more_
     */
    @Override
    public void initFields(List<RecordField> fields) {
        super.initFields(fields);
        tokens = new String[numDataFields];
        for (int i = 0; i < fields.size(); i++) {
            RecordField field = fields.get(i);
            if (field.getSynthetic() || field.hasDefaultValue()
                    || field.getSkip()) {
                continue;
            }
            if (field.getColumnWidth() > 0) {
                fixedWidth = new int[tokens.length];
                int widthIdx = 0;
                for (int j = 0; j < fields.size(); j++) {
                    field = fields.get(j);
                    if (field.getSynthetic() || field.hasDefaultValue()
                            || field.getSkip()) {
                        continue;
                    }
                    fixedWidth[widthIdx++] = field.getColumnWidth();
                }

                break;
            }
        }
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
     * @return _more_
     */
    public String getLine() {
        return currentLine;
    }

    /**
     *  Set the Delimiter property.
     *
     *  @param value The new value for Delimiter
     */
    public void setDelimiter(String value) {
        delimiter = value;
        if (( !delimiter.equals("\t") && (delimiter.trim().length() == 0))
                || delimiter.equals("space")) {
            delimiterIsSpace = true;
            delimiter        = " ";
        } else {
            delimiterIsSpace = false;
        }
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
     * @param recordIO _more_
     *
     * @return _more_
     *
     * @throws IOException _more_
     */
    public String readNextLine(RecordIO recordIO) throws IOException {
        try {
            if(visitor == null) {
                visitor = new Visitor();
                visitor.setReader(recordIO.getBufferedReader());
            }
            while (true) {
                if (firstDataLine != null) {
                    currentLine   = firstDataLine;
                    firstDataLine = null;
                } else {
                    //                    currentLine = recordIO.readLine();
                    currentLine = visitor.readLine();
                }
                if (currentLine == null) {
                    return null;
                }
                currentLine = currentLine.trim();
                if ( !lineOk(currentLine)) {
                    continue;
                }
                return currentLine;
            }
        } catch(Exception exc) {
            throw new RuntimeException(exc);
        }

    }

        /**
     * _more_
     *
     * @param line _more_
     *
     * @return _more_
     */
    public boolean isLineValidData(String line) {
        return ((TextFile) getRecordFile()).isLineValidData(line);
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
    @Override
    public ReadStatus read(RecordIO recordIO) throws Exception {
        String line = null;
        try {
            int fieldCnt;
            while (true) {
                line = readNextLine(recordIO);
                if (line == null) {
                    return ReadStatus.EOF;
                }
                //                System.err.println("LINE:" + line);
                if (isLineValidData(line)) {
                    break;
                }
            }

            //            System.err.println("LINE:" + line);

            for (int i = 0; i < tokens.length; i++) {
                tokens[i] = "";
            }

            if (fixedWidth != null) {
                if ( !split(recordIO, line, fields)) {
                    //throw new IllegalArgumentException("Could not tokenize line:" + line);
                    return ReadStatus.SKIP;
                }
            } else {
                List<String> toks = Utils.tokenizeColumns(line,",");
                if(bePickyAboutTokens && toks.size()!= tokens.length) {
                    throw new IllegalArgumentException("Bad token count:" + tokens.length +" toks:" + toks.size() +" " + toks);
                }
                for(int i=0;i<toks.size() && i < tokens.length;i++) {
                    tokens[i] = toks.get(i);
                }
            }



            String tok      = null;
            int    tokenCnt = 0;
            for (fieldCnt = 0; fieldCnt < fields.size(); fieldCnt++) {
                RecordField field = fields.get(fieldCnt);
                if (skip[fieldCnt]) {
                    continue;
                }
                if (hasDefault[fieldCnt]) {
                    if (field.isTypeString()) {
                        objectValues[fieldCnt] =
                            field.getDefaultStringValue();
                    } else if (field.isTypeDate()) {
                        String dttm = field.getDefaultStringValue();
                        objectValues[fieldCnt] = parseDate(field, dttm);
                    } else {
                        values[fieldCnt] = field.getDefaultDoubleValue();
                    }

                    continue;
                }

                //                System.err.println ("field:" + field +" " + tok);
                if (synthetic[fieldCnt]) {
                    continue;
                }


                tok = tokens[tokenCnt++];
                if (field.isTypeString()) {
                    objectValues[fieldCnt] = tok;

                    continue;
                }
                if (field.isTypeDate()) { 
                   tok                    = tok.replaceAll("\"", "");
                    objectValues[fieldCnt] = parseDate(field, tok);

                    continue;
                }
                if (tok == null) {
                    System.err.println("tok null: " + tokenCnt + " " + line);
                }
                //Check for the riscan NaN
                if (isMissingValue(field, tok)) {
                    values[fieldCnt] = Double.NaN;
                } else {
                    double dValue;
                    if ((idxX == fieldCnt) || (idxY == fieldCnt)) {
                        dValue = ucar.unidata.util.Misc.decodeLatLon(tok);
                    } else {
                        if(tok.endsWith("%")) {
                            tok = tok.substring(0,tok.length()-1);
                        } else if(tok.startsWith("$")) {
                            tok = tok.substring(1);
                        }
                        dValue = Double.parseDouble(tok);
                    }
                    values[fieldCnt] = field.convertValue(dValue);
                    if (isMissingValue(field, values[fieldCnt])) {
                        values[fieldCnt] = Double.NaN;
                    }
                }
                //                System.err.println ("value[ " + fieldCnt +"] = " + values[fieldCnt]);
            }

            if ((idxX >= 0) && (idxY >= 0)) {
                setLocation(values[idxX], values[idxY], ((idxZ >= 0)
                        ? values[idxZ]
                        : 0));
                convertedXYZToLatLonAlt = true;
            }

            if (idxTime >= 0) {
                setRecordTime(getRecordTime());
            }

            return ReadStatus.OK;
        } catch (Exception exc) {
            System.err.println("Line:" + line);

            throw exc;
        }

    }




    /**
     * _more_
     *
     * @param field _more_
     * @param tok _more_
     *
     * @return _more_
     *
     * @throws Exception _more_
     */
    private Date parseDate(RecordField field, String tok) throws Exception {
        tok = tok.trim();
        if(tok.equals("") || tok.equals("null")) {
            return null;
        }
        Date date   = null;
        int  offset = field.getUtcOffset();
        try {
            date = getDateFormat(field).parse(tok);
            //            System.err.println ("Date:" + tok +" parsed:" + date);
        } catch (java.text.ParseException ignore) {
            //Try tacking on UTC
            try {
                date = getDateFormat(field).parse(tok + " UTC");
            } catch (java.text.ParseException ignoreThisOne) {
                throw ignore;
            }
        }
        if (offset != 0) {
            long millis = date.getTime();
            millis += (-offset * 1000 * 3600);
            //            System.err.println ("date1:" + date);
            date = new Date(millis);
            //            System.err.println ("date2:" + date);
            //            System.exit(0);
        }

        return date;
    }



    /**
     * _more_
     *
     * @param field _more_
     *
     * @return _more_
     */
    private SimpleDateFormat getDateFormat(RecordField field) {
        SimpleDateFormat sdf = field.getDateFormat();
        if (sdf == null) {
            field.setDateFormat(sdf =
                getRecordFile().makeDateFormat(TextFile.DFLT_DATE_FORMAT));
        }

        return sdf;
    }



    /**
     * _more_
     *
     * @param station _more_
     */
    public void setLocation(Station station) {
        this.setLocation(station.getLongitude(), station.getLatitude(),
                         station.getElevation());
        if (idxX >= 0) {
            this.setValue(idxX, station.getLongitude());
        }
        if (idxY >= 0) {
            this.setValue(idxY, station.getLatitude());
        }
        if (idxZ >= 0) {
            this.setValue(idxZ, station.getElevation());
        }

    }



    /**
     * _more_
     *
     * @param line _more_
     *
     * @return _more_
     */
    public boolean lineOk(String line) {
        if ((line.length() == 0) || line.startsWith("#")) {
            return false;
        }

        return true;
    }



    /** _more_ */
    boolean testing = false;

    /**
     * _more_
     *
     * @param sourceString _more_
     * @param fields _more_
     *
     * @return _more_
     */
    public boolean split(RecordIO recordIO, String sourceString, List<RecordField> fields) throws Exception {

        if (tokens == null) {
            testing = true;
            tokens  = new String[10];
        }
        int delimLength        = 1;
        int fullTokenCnt  = 0;
        int numTokensRead = 0;
        int fromIndex     = 0;
        int sourceLength  = sourceString.length();
        //        System.err.println ("line:" + sourceString);
        boolean inQuotes = sourceString.startsWith("\"");

        /*
        //            10,"text column",20,"another text column"
        0
        */

        if (fixedWidth != null) {
            int lastIdx = 0;
            for (int i = 0; i < fixedWidth.length; i++) {
                //                System.err.println("last idx:" + lastIdx +" w:" + fixedWidth[i]);
                String theString = sourceString.substring(lastIdx,
                                       lastIdx + fixedWidth[i]);
                tokens[numTokensRead++] = theString;
                lastIdx                 += fixedWidth[i];
                //                System.err.println(" tok:" + theString);
            }
        } else {
            int     idx;
            while (true) {
                if (inQuotes) {
                    idx = sourceString.indexOf("\"", fromIndex + 1);
                    int maxLines = 10;
                    while(idx<0 && maxLines-->0) {
                        String extraLine = readNextLine(recordIO);
                        if(extraLine == null) {
                            break;
                        }
                        sourceString = sourceString + extraLine;
                        idx = sourceString.indexOf("\"", fromIndex + 1);
                    }
                    idx++;
                } else {
                    idx = sourceString.indexOf(delimiter, fromIndex);
                }
                System.err.println("inquote:" + inQuotes +" from:" + fromIndex +" idx:" + idx);
                String theString;
                if (idx < 0) {
                    theString = sourceString.substring(fromIndex);
                } else {
                    theString = sourceString.substring(fromIndex, idx);
                    if (delimiterIsSpace) {
                        while ((sourceString.charAt(idx) == ' ')
                                && (idx < sourceLength)) {
                            idx++;
                        }
                        fromIndex = idx;
                    } else {
                        fromIndex = idx + delimLength;
                    }
                }

                theString = theString.trim();
                if (inQuotes) {
                    theString = theString.substring(1,
                            theString.length() - 1);
                }
                System.err.println ("\ttokens[" + numTokensRead +"] = " + theString);
                tokens[numTokensRead++] = theString;

                if ((idx < 0) || (numTokensRead == tokens.length)) {
                    break;
                }
                if (fromIndex >= sourceLength) {
                    if(fromIndex == sourceLength && sourceString.endsWith(delimiter)) {
                        //pad out
                        tokens[numTokensRead++] = "";
                    }
                    break;
                }
                if (fromIndex < sourceLength) {
                    //                System.err.println("C:" + sourceString.charAt(fromIndex));
                    inQuotes = sourceString.charAt(fromIndex) == '\"';
                }
            }
        }

        if (testing) {
            return true;
        }


        if (bePickyAboutTokens && (numTokensRead != tokens.length)) {
            badCnt++;
            //Handle the goofy point cloud text file that occasionally has a single number
            if ((badCnt > 5) || (numTokensRead != 1)) {
                System.err.println("bad token cnt: expected:" + tokens.length
                                   + " read:" + numTokensRead + " delimiter:"
                                   + delimiter + ": is space:"
                                   + delimiterIsSpace + "\nLine:"
                                   + currentLine);

                throw new IllegalArgumentException(
                    "Could not tokenize line:\n" + currentLine + "\n");
            }

            return false;
        }
        badCnt = 0;

        return true;

        /*
        System.err.println("line:" + sourceString);
        for(String tok: tokens) {
            System.err.println("tok:" + tok);
        }
        */

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
     * @param args _more_
     */
    public static void main(String[] args) throws Exception {
        TextRecord record = new TextRecord();
        record.setDelimiter(",");
        record.testing = true;
        for (String line : args) {
            record.split(null, line, null);
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


    /**
     * _more_
     *
     * @param picky _more_
     */
    public void setBePickyAboutTokens(boolean picky) {
        bePickyAboutTokens = picky;
    }

    /**
     * _more_
     *
     * @param line _more_
     */
    public void setFirstDataLine(String line) {
        firstDataLine = line;
    }


}
