/*
 * Copyright (c) 2008-2015 Geode Systems LLC
 * This Software is licensed under the Geode Systems RAMADDA License available in the source distribution in the file 
 * ramadda_license.txt. The above copyright notice shall be included in all copies or substantial portions of the Software.
 */

package org.ramadda.plugins.biz;


import org.ramadda.data.point.*;
import org.ramadda.data.point.text.*;
import org.ramadda.data.record.*;
import org.ramadda.repository.Entry;


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
public class EiaFile extends CsvFile {

    /** _more_ */
    private StringBuilder buffer;


    /**
     * ctor
     *
     * @param filename _more_
     *
     * @throws IOException _more_
     */
    public EiaFile(String filename) throws IOException {
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
            if (buffer == null) {
                //                System.err.println("Reading EIA time series");
                buffer = new StringBuilder();
                InputStream source = super.doMakeInputStream(buffered);
                Element     root   = XmlUtil.getRoot(source);
                Element     series = XmlUtil.findChild(root, Eia.TAG_SERIES);
                series = XmlUtil.findChild(series, Eia.TAG_ROW);

                Element data = XmlUtil.findChild(series, Eia.TAG_DATA);
                //            System.err.println("Root:" + XmlUtil.toString(root));
                Entry entry = (Entry) getProperty("entry");
                String name = XmlUtil.getGrandChildText(series, Eia.TAG_NAME,
                                  "").trim();
                String desc = XmlUtil.getGrandChildText(series,
                                  Eia.TAG_DESCRIPTION, "").trim();
                if (entry != null) {
                    entry.setName(name);
                    entry.setDescription(desc);
                }

                String format = "yyyyMMdd";
                String unit = XmlUtil.getGrandChildText(series,
                                  Eia.TAG_UNITS, "").trim();
                List nodes = XmlUtil.findChildren(data, Eia.TAG_ROW);
                for (int i = 0; i < nodes.size(); i++) {
                    Element node = (Element) nodes.get(i);
                    String dttm = XmlUtil.getGrandChildText(node,
                                      Eia.TAG_DATE, "").trim().toLowerCase();

                    if (dttm.indexOf("q") >= 0) {
                        dttm = dttm.replace("q1", "01").replace("q2",
                                            "04").replace("q3",
                                                "07").replace("q4", "10");
                    }

                    if (i == 0) {
                        if (dttm.length() == 4) {
                            format = "yyyy";
                        } else if (dttm.length() == 6) {
                            format = "yyyyMM";
                        } else if (dttm.length() == 8) {
                            format = "yyyyMMdd";
                        }
                    }
                    String value = XmlUtil.getGrandChildText(node,
                                       Eia.TAG_VALUE, "").trim();
                    if (value.equals("") || value.equals(".")) {
                        value = "-999999.99";
                    }
                    buffer.append(dttm).append(",").append(value).append(
                        "\n");
                }

                putFields(new String[] {
                    makeField(FIELD_DATE, attrType("date"),
                              attrFormat(format)),
                    makeField("value", attrUnit(unit), attrLabel("Value"),
                              attrChartable(), attrMissing(-999999.99)), });
            }

            ByteArrayInputStream bais =
                new ByteArrayInputStream(buffer.toString().getBytes());

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
        if (getProperty(PROP_FIELDS, (String) null) == null) {
            String format = "yyyy-MM-dd";
            putFields(new String[] {
                makeField(FIELD_DATE, attrType("date"), attrFormat(format)),
                makeField("value", attrLabel("Value"), attrChartable(),
                          attrMissing(-999999.99)), });
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
        PointFile.test(args, EiaFile.class);
    }

}
