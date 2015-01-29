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


public class Visitor implements Cloneable {


    /** _more_ */
    private PrintWriter writer;

    /** _more_ */
    private InputStream input;

    /** _more_ */
    private OutputStream output;

    /** _more_ */
    private int nextChar = -1;


    /** _more_ */
    private String delimiter = null;

    /** _more_ */
    private String outputDelimiter = ",";

    /** _more_ */
    private int skip = 0;

    /** _more_ */
    private int maxRows = -1;

    /** _more_ */
    private int row = 0;


    /** _more_ */
    private Converter.ColumnSelector selector;

    /** _more_          */
    private Filter.FilterGroup filter = new Filter.FilterGroup();

    /** _more_ */
    private Converter.ConverterGroup converter =
        new Converter.ConverterGroup();

    /** _more_ */
    private Processor.ProcessorGroup processor =
        new Processor.ProcessorGroup();

    /** _more_ */
    private DecimalFormat format;

    /** _more_ */
    private List<String> headerLines = new ArrayList<String>();

    /** _more_ */
    private Hashtable<String, Integer> columnMap;

    /** _more_ */
    private List<String> columnNames;

    /** _more_ */
    private List props = new ArrayList();

    /** _more_ */
    private HashSet<Integer> sheetsToShow;

    /** _more_ */
    private List<SearchField> searchFields = new ArrayList<SearchField>();


    /**
     * _more_
     */
    public Visitor() {}



    /**
     * _more_
     *
     * @return _more_
     *
     * @throws CloneNotSupportedException _more_
     */
    public Object clone() throws CloneNotSupportedException {
        Object that = super.clone();

        return that;
    }

    /**
     * _more_
     *
     * @param input _more_
     * @param output _more_
     *
     * @return _more_
     *
     * @throws CloneNotSupportedException _more_
     */
    public Visitor cloneMe(InputStream input, OutputStream output)
            throws CloneNotSupportedException {
        Visitor that = (Visitor) super.clone();
        that.input  = input;
        that.output = output;
        that.writer = new PrintWriter(that.output);

        return that;
    }


    /**
     * _more_
     *
     * @param info _more_
     *
     * @return _more_
     *
     * @throws Exception _more_
     */
    public String readLine() throws Exception {
        //        System.err.println("readline");
        StringBuilder lb = new StringBuilder();
        int           c;
        boolean       inQuote         = false;
        StringBuilder sb              = new StringBuilder();
        char          NEWLINE         = '\n';
        char          CARRIAGE_RETURN = '\r';
        while (true) {

            /*
            if (lb.length() > 750) {
                System.err.println("Whoa:" + lb);
                System.err.println(sb);
                System.exit(0);
            }
            */

            if (nextChar >= 0) {
                c        = nextChar;
                nextChar = -1;
            } else {
                c = getInput().read();
            }
            if (c == -1) {
                break;
            }

            if (c == NEWLINE) {
                sb.append("\t************** new line:" + inQuote + "\n");
                if ( !inQuote) {
                    break;
                }
            } else if (c == CARRIAGE_RETURN) {
                sb.append("\tcr:" + inQuote + "\n");
                if ( !inQuote) {
                    nextChar = getInput().read();
                    if (nextChar == -1) {
                        break;
                    }
                    if (nextChar == NEWLINE) {
                        nextChar = -1;
                    }

                    break;
                }
            } else {
                sb.append("\tchar:" + (char) c + "  " + inQuote + "\n");
            }
            lb.append((char) c);
            if (c == '"') {
                //                sb.append("\tquote: "  + inQuote+"\n");
                if ( !inQuote) {
                    //                    sb.append("\tinto quote\n");
                    inQuote = true;
                } else {
                    nextChar = getInput().read();
                    if (nextChar == -1) {
                        break;
                    }
                    if (nextChar != '"') {
                        //                        sb.append("\tout quote\n");
                        inQuote = false;
                        if (nextChar == NEWLINE) {
                            //                            sb.append("\t************** new line:" + inQuote+"\n");
                            break;
                        }
                    }
                    //                    sb.append("\tnext char:" +(char) nextChar+"\n");
                    lb.append((char) nextChar);
                    nextChar = -1;
                }
            }

        }
        //        System.out.println(sb);


        String line = lb.toString();
        if (line.length() == 0) {
            return null;
        }

        //        System.err.println("LINE:" + line);
        return line;
    }



    /**
     * _more_
     *
     * @param sheetIdx _more_
     *
     * @return _more_
     */
    public boolean okToShowSheet(int sheetIdx) {
        if ((sheetsToShow != null) && !sheetsToShow.contains(sheetIdx)) {
            return false;
        }

        return true;
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
     * @return _more_
     */
    public PrintWriter getWriter() {
        return writer;
    }


    /**
     * _more_
     *
     * @param s _more_
     */
    public void print(String s) {
        if (writer != null) {
            writer.print(s);
        }
    }


    /**
     * _more_
     */
    public void flush() {
        if (writer != null) {
            writer.flush();
        }
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


    /**
     * Set the Converter property.
     *
     * @param value The new value for Converter
     */
    public void setConverter(Converter.ConverterGroup value) {
        converter = value;
    }

    /**
     * Get the Converter property.
     *
     * @return The Converter
     */
    public Converter.ConverterGroup getConverter() {
        return converter;
    }

    /**
     * _more_
     *
     * @return _more_
     */
    public Filter.FilterGroup getFilter() {
        return filter;
    }


    /**
     * Set the Processor property.
     *
     * @param value The new value for Processor
     */
    public void setProcessor(Processor.ProcessorGroup value) {
        processor = value;
    }

    /**
     * Get the Processor property.
     *
     * @return The Processor
     */
    public Processor.ProcessorGroup getProcessor() {
        return processor;
    }


    /**
     * _more_
     *
     * @param name _more_
     * @param value _more_
     */
    public void addTableProperty(String name, String value) {
        props.add(name);
        props.add(value);
    }

    /**
     * _more_
     *
     * @return _more_
     */
    public List getTableProperties() {
        return props;
    }

    /**
     *  Set the SheetsToShow property.
     *
     *  @param value The new value for SheetsToShow
     */
    public void setSheetsToShow(HashSet<Integer> value) {
        sheetsToShow = value;
    }

    /**
     *  Get the SheetsToShow property.
     *
     *  @return The SheetsToShow
     */
    public HashSet<Integer> getSheetsToShow() {
        return sheetsToShow;
    }


    /**
     * Set the SearchFields property.
     *
     * @param value The new value for SearchFields
     */
    public void setSearchFields(List<SearchField> value) {
        searchFields = value;
    }

    /**
     * Get the SearchFields property.
     *
     * @return The SearchFields
     */
    public List<SearchField> getSearchFields() {
        return searchFields;
    }

    /**
     * Set the OutputDelimiter property.
     *
     * @param value The new value for OutputDelimiter
     */
    public void setOutputDelimiter(String value) {
        outputDelimiter = value;
    }

    /**
     * Get the OutputDelimiter property.
     *
     * @return The OutputDelimiter
     */
    public String getOutputDelimiter() {
        return outputDelimiter;
    }




}
