/*
 * Copyright (c) 2008-2015 Geode Systems LLC
 * This Software is licensed under the Geode Systems RAMADDA License available in the source distribution in the file 
 * ramadda_license.txt. The above copyright notice shall be included in all copies or substantial portions of the Software.
 */

package org.ramadda.plugins.power;


import org.ramadda.data.point.*;
import org.ramadda.data.point.text.*;
import org.ramadda.data.record.*;


import org.ramadda.repository.RepositoryUtil;

import org.w3c.dom.Element;

import ucar.unidata.xml.XmlUtil;

import java.io.*;

import java.text.SimpleDateFormat;

import java.util.Date;
import java.util.List;

import java.util.TimeZone;


/**
 */
public class MisoForecastFile extends CsvFile {

    /** _more_ */
    private boolean windForecast = false;


    /**
     * ctor
     *
     * @param filename _more_
     *
     * @throws IOException _more_
     */
    public MisoForecastFile(String filename) throws IOException {
        super(filename);
    }

    /**
     * _more_
     *
     * @param buffered _more_
     *
     * @return _more_
     *
     * @throws IOException _more_
     */
    @Override
    public InputStream doMakeInputStream(boolean buffered)
            throws IOException {
        try {
            InputStream source = super.doMakeInputStream(buffered);
            Element     root   = XmlUtil.getRoot(source);
            windForecast = root.getTagName().equals("WindForecastDayAhead");

            StringBuffer s     = new StringBuffer("#converted stream\n");

            List         nodes = XmlUtil.findChildren(root, windForecast
                    ? "Forecast"
                    : "instance");
            for (int i = 0; i < nodes.size(); i++) {
                Element node = (Element) nodes.get(i);
                String dttm = XmlUtil.getGrandChildText(node, "DateTimeEST",
                                  null);
                String hour = XmlUtil.getGrandChildText(node,
                                  "HourEndingEST", null);
                String value = XmlUtil.getGrandChildText(node, "Value", null);
                s.append(dttm + "," + hour + "," + value + "\n");
            }

            ByteArrayInputStream bais =
                new ByteArrayInputStream(s.toString().getBytes());

            return bais;
        } catch (Exception exc) {
            throw new RuntimeException(exc);
        }
    }

    /**
     * Gets called when first reading the file. Parses the header
     *
     * @param visitInfo visit info
     *
     * @return the visit info
     *
     *
     * @throws Exception _more_
     */
    @Override
    public VisitInfo prepareToVisit(VisitInfo visitInfo) throws Exception {
        super.prepareToVisit(visitInfo);
        String format;
        String varName;
        String varLabel;
        if (windForecast) {
            format   = "MM/dd/yyyy h:mm a";
            varName  = "wind_forecast";
            varLabel = "Wind Forecast";
        } else {
            format   = "MMM dd yyyy h:mma";
            varName  = "wind_generation";
            varLabel = "Wind Generation";
            //            <DateTimeEST>Feb 27 2014  8:00PM</DateTimeEST>
        }
        putFields(new String[] {
            makeField(FIELD_DATE, attr("timezone", "EST"), attrType("date"),
                      attrFormat(format)),
            makeField("hour_ending", attrType("string"),
                      attrLabel("Hour Ending"), attrChartable()),
            makeField(varName, attrLabel(varLabel), attrChartable()), });

        return visitInfo;
    }




    /**
     * _more_
     *
     * @param args _more_
     *
     * @throws Exception _more_
     */
    public static void main(String[] args) throws Exception {
        PointFile.test(args, MisoForecastFile.class);
    }

}
