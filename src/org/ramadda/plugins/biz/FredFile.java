/*
 * Copyright (c) 2008-2015 Geode Systems LLC
 * This Software is licensed under the Geode Systems RAMADDA License available in the source distribution in the file 
 * ramadda_license.txt. The above copyright notice shall be included in all copies or substantial portions of the Software.
 */

package org.ramadda.plugins.biz;


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
public class FredFile extends CsvFile {

    /** _more_ */
    private boolean windForecast = false;


    /**
     * ctor
     *
     * @param filename _more_
     *
     * @throws IOException _more_
     */
    public FredFile(String filename) throws IOException {
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
            System.err.println("Reading FRED time series"); 
            StringBuilder sb =  new StringBuilder();
            InputStream source = super.doMakeInputStream(buffered);
            Element     root   = XmlUtil.getRoot(source);

            //            System.err.println("Root:" + XmlUtil.toString(root));

            String format   = "yyyy-MM-dd";
            String unit = XmlUtil.getAttribute(root, Fred.ATTR_UNITS,"");
            putFields(new String[] {
                    makeField(FIELD_DATE, attrType("date"), attrFormat(format)),
                    makeField("value", attrUnit(unit), attrLabel("Value"), attrChartable(), attrMissing(-999999.99)), });


            List         nodes = XmlUtil.findChildren(root, Fred.TAG_OBSERVATION);
            for (int i = 0; i < nodes.size(); i++) {
                Element node = (Element) nodes.get(i);
                String value = XmlUtil.getAttribute(node, Fred.ATTR_VALUE,"").trim();
                String dttm = XmlUtil.getAttribute(node, Fred.ATTR_DATE, (String) null);
                if(value.equals("") || value.equals(".")) {
                    value = "-999999.99";
                }
                sb.append(dttm +  "," + value + "\n");
            }

            ByteArrayInputStream bais =
                new ByteArrayInputStream(sb.toString().getBytes());

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
        if(getProperty(PROP_FIELDS, (String)null) == null) {
            String format   = "yyyy-MM-dd";
            putFields(new String[] {
                    makeField(FIELD_DATE, attrType("date"), attrFormat(format)),
                    makeField("value", attrLabel("Value"), attrChartable(), attrMissing(-999999.99)), });
        }
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
        PointFile.test(args, FredFile.class);
    }

}
