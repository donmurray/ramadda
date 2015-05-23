/*
 * Copyright (c) 2008-2015 Geode Systems LLC
 * This Software is licensed under the Geode Systems RAMADDA License available in the source distribution in the file 
 * ramadda_license.txt. The above copyright notice shall be included in all copies or substantial portions of the Software.
 */

package org.ramadda.plugins.socrata;


import org.ramadda.data.point.*;
import org.ramadda.data.point.text.*;
import org.ramadda.data.record.*;
import org.ramadda.repository.Entry;


import org.ramadda.repository.RepositoryUtil;

import org.w3c.dom.Element;

import ucar.unidata.util.IOUtil;

import ucar.unidata.xml.XmlUtil;

import java.io.*;

import java.text.SimpleDateFormat;

import java.util.Date;
import java.util.List;

import java.util.TimeZone;


/**
 */
public class SocrataFile extends CsvFile {

    /** _more_ */
    private StringBuilder buffer;


    /**
     * ctor
     *
     * @param filename _more_
     *
     * @throws IOException _more_
     */
    public SocrataFile(String filename) throws IOException {
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
                System.err.println("Reading SOCRATA time series");
                buffer = new StringBuilder();

                InputStream source = super.doMakeInputStream(buffered);
                String      json   = IOUtil.readContents(source);
                /*
                putFields(new String[] {
                    makeField(FIELD_DATE, attrType("date"),
                              attrFormat(format)),
                    makeField("value", attrUnit(unit), attrLabel("Value"),
                              attrChartable(), attrMissing(-999999.99)), });
                */
            }
            ByteArrayInputStream bais =
                new ByteArrayInputStream(buffer.toString().getBytes());

            return bais;
        } catch (Exception exc) {
            throw new RuntimeException(exc);
        }
    }


    /**
     * _more_
     *
     * @param args _more_
     *
     * @throws Exception _more_
     */
    public static void main(String[] args) throws Exception {
        PointFile.test(args, SocrataFile.class);
    }

}
