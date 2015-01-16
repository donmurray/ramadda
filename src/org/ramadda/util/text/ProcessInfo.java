/*
* Copyright 2008-2014 Geode Systems LLC
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

package org.ramadda.util.text;


import org.ramadda.util.Utils;


import ucar.unidata.util.IOUtil;
import ucar.unidata.util.Misc;
import ucar.unidata.util.StringUtil;

import java.io.*;

import java.text.DateFormat;

import java.text.DecimalFormat;
import java.text.SimpleDateFormat;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.List;

import java.util.regex.*;


/**
 *
 * @author Jeff McWhirter
 */


public class ProcessInfo {

    /** _more_ */
    private PrintWriter writer;

    /** _more_ */
    private InputStream input;

    /** _more_ */
    private OutputStream output;

    /** _more_ */
    private String delimiter = null;

    /** _more_ */
    private int skip = 0;

    /** _more_ */
    private int maxRows = -1;

    /** _more_ */
    private int row = 0;


    /** _more_ */
    private List<String> headerLines = new ArrayList<String>();

    /** _more_ */
    private Hashtable<String, Integer> columnMap;

    /** _more_ */
    private List<String> columnNames;

    /** _more_ */
    private DecimalFormat format;

    /** _more_ */
    private Converter.ColumnSelector selector;


    /**
     * _more_
     *
     * @param input _more_
     * @param output _more_
     */
    public ProcessInfo(InputStream input, OutputStream output) {
        this(input, output, null);
    }

    /**
     *  Set the Selector property.
     *
     *  @param value The new value for Selector
     */
    public void setSelector(Converter.ColumnSelector value) {
        selector = value;
    }

    /**
     *  Get the Selector property.
     *
     *  @return The Selector
     */
    public Converter.ColumnSelector getSelector() {
        return selector;
    }



    /**
     * _more_
     *
     * @param s _more_
     *
     * @return _more_
     */
    public int getColumnIndex(String s) {
        try {
            return Integer.parseInt(s);
        } catch (NumberFormatException exc) {}
        if (columnNames == null) {
            if (headerLines.size() == 0) {
                return -1;
            }
            columnMap   = new Hashtable<String, Integer>();
            columnNames = StringUtil.split(headerLines.get(0), delimiter);
        }
        Integer iv = columnMap.get(s);
        if (iv != null) {
            return iv.intValue();
        }
        for (int i = 0; i < columnNames.size(); i++) {
            String v = columnNames.get(i);
            if (v.startsWith(s)) {
                columnMap.put(v, i);

                return i;
            }
        }

        return -1;
    }


    /**
     * _more_
     */
    public void incrRow() {
        row++;
    }


    /**
     *  Get the Row property.
     *
     *  @return The Row
     */
    public int getRow() {
        return row;
    }



    /**
     * _more_
     *
     * @param input _more_
     * @param output _more_
     * @param delimiter _more_
     */
    public ProcessInfo(InputStream input, OutputStream output,
                       String delimiter) {
        this.input     = input;
        this.output    = output;
        this.writer    = new PrintWriter(this.output);
        this.delimiter = delimiter;
    }

    /**
     * _more_
     *
     * @return _more_
     */
    public PrintWriter getWriter() {
        return writer;
    }


    /**
     * _more_
     *
     * @param line _more_
     */
    public void addHeaderLine(String line) {
        headerLines.add(line);
    }

    /**
     * _more_
     *
     * @return _more_
     */
    public List<String> getHeaderLines() {
        return headerLines;
    }

    /**
     * _more_
     *
     * @param value _more_
     *
     * @return _more_
     */
    public String formatValue(double value) {
        if (format != null) {
            return format.format(value);
        }

        return "" + value;
    }


    /**
     * Set the Format property.
     *
     * @param value The new value for Format
     */
    public void setFormat(String value) {
        if (value == null) {
            format = null;
        } else {
            format = new DecimalFormat(value);
        }
    }




    /**
     * Set the Input property.
     *
     * @param value The new value for Input
     */
    public void setInput(InputStream value) {
        input = value;
    }

    /**
     * Get the Input property.
     *
     * @return The Input
     */
    public InputStream getInput() {
        return input;
    }

    /**
     * Set the Output property.
     *
     * @param value The new value for Output
     */
    public void setOutput(OutputStream value) {
        output = value;
    }

    /**
     * Get the Output property.
     *
     * @return The Output
     */
    public OutputStream getOutput() {
        return output;
    }



    /**
     * Set the MaxRows property.
     *
     * @param value The new value for MaxRows
     */
    public void setMaxRows(int value) {
        maxRows = value;
    }

    /**
     * Get the MaxRows property.
     *
     * @return The MaxRows
     */
    public int getMaxRows() {
        return maxRows;
    }




    /**
     * Set the Skip property.
     *
     * @param value The new value for Skip
     */
    public void setSkip(int value) {
        skip = value;
    }

    /**
     * Get the Skip property.
     *
     * @return The Skip
     */
    public int getSkip() {
        return skip;
    }



    /**
     * Set the Delimiter property.
     *
     * @param value The new value for Delimiter
     */
    public void setDelimiter(String value) {
        delimiter = value;
    }

    /**
     * Get the Delimiter property.
     *
     * @return The Delimiter
     */
    public String getDelimiter() {
        return delimiter;
    }





}
