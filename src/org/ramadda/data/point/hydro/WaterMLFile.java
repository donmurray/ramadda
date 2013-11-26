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

package org.ramadda.data.point.hydro;


import org.ramadda.data.point.*;
import org.ramadda.data.point.text.*;
import org.ramadda.data.record.*;

import org.ramadda.util.WaterMLUtil;

import org.w3c.dom.Document;
import org.w3c.dom.Element;

import ucar.unidata.util.Misc;
import ucar.unidata.util.StringUtil;
import ucar.unidata.xml.XmlUtil;

import java.io.*;

import java.text.SimpleDateFormat;

import java.util.ArrayList;

import java.util.Date;
import java.util.List;


/**
 */
public class WaterMLFile extends PointFile {


    private double latitude=0, longitude=0, altitude=0;

    /** _more_ */
    private List<RecordField> fields;

    /** _more_ */
    private double[][] values;

    /**
     * The constructor
     *
     * @param filename file
     * @throws IOException On badness
     */
    public WaterMLFile(String filename) throws IOException {
        super(filename);
    }


    /**
     * _more_
     *
     * @param visitInfo _more_
     *
     * @return _more_
     */
    public Record doMakeRecord(VisitInfo visitInfo) {
        DataRecord dataRecord = new DataRecord(this) {
                public void checkIndices() {}
            };
        dataRecord.initFields(fields);
        return dataRecord;
    }


    /**
     * This  gets called before the file is visited. It reads the header and defines the fields
     *
     * @param visitInfo visit info
     * @return possible new visitinfo
     * @throws Exception On badness
     */
    @Override
    public VisitInfo prepareToVisit(VisitInfo visitInfo) throws Exception {
        fields = new ArrayList<RecordField>();
        Element root = XmlUtil.getRoot(getFilename(), getClass());

        List timeseriesNodes = XmlUtil.findChildren(root,
                              WaterMLUtil.TAG_TIMESERIES);
        if (timeseriesNodes.size() == 0) {
            throw new IllegalArgumentException("No timeseries found");
        }

        int                numParams  = timeseriesNodes.size();
        int                numValues  = 0;
        List<List<Double>> valuesList = new ArrayList<List<Double>>();
        


        /*
      <geoLocation>
        <geogLocation xsi:type="LatLonPointType" srs="EPSG:4269">
          <latitude>41.643457</latitude>
          <longitude>-111.917649</longitude>
        </geogLocation>
        */

        Object[] fileMetadata = null;

        String latitude = null;
        String longitude = null;

        List<List>  listOfValues = new ArrayList<List>();

        for (int i = 0; i < timeseriesNodes.size(); i++) {
            Element timeSeriesNode = (Element) timeseriesNodes.get(i);
            Element sourceInfo = XmlUtil.findChild(timeSeriesNode,
                                     WaterMLUtil.TAG_SOURCEINFO);
            if(latitude==null) {
                Element latitudeNode = XmlUtil.findDescendant(sourceInfo,
                                                              WaterMLUtil.TAG_LATITUDE);
                Element longitudeNode = XmlUtil.findDescendant(sourceInfo,
                                                               WaterMLUtil.TAG_LONGITUDE);
                if(latitudeNode!=null) {
                    latitude = XmlUtil.getChildText(latitudeNode);
                }
                if(longitudeNode!=null) {
                    longitude = XmlUtil.getChildText(longitudeNode);
                }
            }



            Element valuesNode = XmlUtil.findChild(timeSeriesNode,
                                     WaterMLUtil.TAG_VALUES);
            Element variable = XmlUtil.findChild(timeSeriesNode,
                                   WaterMLUtil.TAG_VARIABLE);

            //    <variable>
            //      <variableCode vocabulary="LBR" default="true" variableID="3">USU3</variableCode>
            //      <variableName>Battery voltage</variableName>
            //      <valueType>Field Observation</valueType>
            //      <dataType>Minimum</dataType>
            String variableCode = XmlUtil.getGrandChildText(variable,
                                      WaterMLUtil.TAG_VARIABLECODE, "");
            String variableName = XmlUtil.getGrandChildText(variable,
                                      WaterMLUtil.TAG_VARIABLENAME, "");
            String valueType = XmlUtil.getGrandChildText(variable,
                                   WaterMLUtil.TAG_VALUETYPE, "");
            String dataType = XmlUtil.getGrandChildText(variable,
                                  WaterMLUtil.TAG_DATATYPE, "");



            List values = XmlUtil.findChildren(valuesNode,
                                               WaterMLUtil.TAG_VALUE);

            listOfValues.add(values);
            //            <siteName>Little Bear River at Mendon Road near Mendon, Utah</siteName>
            //<siteCode network="LBR" siteID="1">USU-LBR-Mendon</siteCode>
            String siteName = XmlUtil.getGrandChildText(sourceInfo,
                                  WaterMLUtil.TAG_SITENAME, "");
            String siteCode = XmlUtil.getGrandChildText(sourceInfo,
                                  WaterMLUtil.TAG_SITECODE, "");

            if(fileMetadata==null) {
                fileMetadata = new Object[]{siteCode, siteName};
                setFileMetadata(fileMetadata);
            }

            String varName = variableCode +"-" + dataType;
            String var = variableName +"-" + dataType;
            RecordField field = new RecordField(varName, var, var,  i + 1+2, "");
            field.setType(RecordField.TYPE_NUMERIC);
            field.setChartable(true);
            field.setSearchable(true);
            fields.add(field);

            //            valuesList.add(values);
            numValues = Math.max(numValues, values.size());
        }

        RecordField lonField = new RecordField("longitude", "longitude","longitude",  2, "degrees");
        lonField.setDefaultDoubleValue(this.longitude = Double.parseDouble(longitude));
        lonField.setType(RecordField.TYPE_NUMERIC);
        lonField.setChartable(true);
        lonField.setSearchable(true);
        fields.add(0, lonField);

        RecordField latField = new RecordField("latitude", "latitude","latitude",  1, "degrees");
        latField.setDefaultDoubleValue(this.latitude = Double.parseDouble(latitude));
        latField.setType(RecordField.TYPE_NUMERIC);
        latField.setChartable(true);
        latField.setSearchable(true);
        fields.add(0, latField);


        for(RecordField recordField: fields) {
            DataRecord.initField(recordField);
        }
        setFields(fields);

        values = new double[numParams][];
        for (int i = 0; i < numParams; i++) {
            values[i] = new double[numValues];
        }

        for (int i = 0; i < listOfValues.size(); i++) {
            List valueNodes = listOfValues.get(i);
            for (int valueIdx = 0; valueIdx < valueNodes.size(); valueIdx++) {
                //<value censorCode="nc" dateTime="2007-11-05T14:30:00" timeOffset="-07:00" dateTimeUTC="2007-11-05T21:30:00" methodCode="4" sourceCode="2" qualityControlLevelCode="0">13.33616</value>
                Element valueNode = (Element) valueNodes.get(valueIdx);

                double value = Double.parseDouble(XmlUtil.getChildText(valueNode));
                values[i][valueIdx] = value;
            }
        }

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
     * @throws IOException _more_
     */
    public Record.ReadStatus readNextRecord(VisitInfo visitInfo,
                                            Record record)
            throws IOException {
        visitInfo.addRecordIndex(1);
        int currentIdx = visitInfo.getRecordIndex();
        DataRecord dataRecord = (DataRecord) record;
        if (currentIdx >= values[0].length) {
            return Record.ReadStatus.EOF;
        }
        for (int i = 0; i < values.length; i++) {
            dataRecord.setValue(i+1+2, values[i][currentIdx]);
        }

        dataRecord.setLatitude(latitude);
        dataRecord.setLongitude(longitude);
        dataRecord.setAltitude(altitude);
        //        convertedXYZToLatLonAlt = true;

        return Record.ReadStatus.OK;
    }


    /**
     * _more_
     *
     * @param args _more_
     */
    public static void main(String[] args) {
        PointFile.test(args, WaterMLFile.class);
    }

}
