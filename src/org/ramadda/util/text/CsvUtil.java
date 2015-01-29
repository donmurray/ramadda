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


import ucar.unidata.xml.XmlUtil;

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

public class CsvUtil {


    /** a hack for debugging */
    private static String theLine;


    /**
     * Merge each row in the given files out. e.g., if file1 has
     *  1,2,3
     *  4,5,6
     * and file2 has
     * 8,9,10
     * 11,12,13
     * the result would be
     * 1,2,3,8,9,10
     * 4,5,6,11,12,13
     * Gotta figure out how to handle different numbers of rows
     *
     * @param files files
     * @param out output
     *
     * @throws Exception On badness
     */
    public static void concat(List<String> files, OutputStream out)
            throws Exception {
        PrintWriter          writer    = new PrintWriter(out);
        String               delimiter = ",";
        List<BufferedReader> readers   = new ArrayList<BufferedReader>();
        for (String file : files) {
            readers.add(
                new BufferedReader(
                    new InputStreamReader(new FileInputStream(file))));
        }
        while (true) {
            int nullCnt = 0;
            for (int i = 0; i < readers.size(); i++) {
                BufferedReader br   = readers.get(i);
                String         line = br.readLine();
                if (line == null) {
                    nullCnt++;

                    continue;
                }
                if (i > 0) {
                    writer.print(delimiter);
                }
                writer.print(line);
                writer.flush();
            }
            if (nullCnt == readers.size()) {
                break;
            }
            writer.println("");
        }

    }


    /**
     * _more_
     *
     * @param files _more_
     * @param out _more_
     *
     * @throws Exception _more_
     */
    public static void header(List<String> files, OutputStream out)
            throws Exception {
        PrintWriter          writer    = new PrintWriter(out);
        String               delimiter = ",";
        List<BufferedReader> readers   = new ArrayList<BufferedReader>();
        for (String file : files) {
            readers.add(
                new BufferedReader(
                    new InputStreamReader(new FileInputStream(file))));
        }
        for (BufferedReader br : readers) {
            String line = br.readLine();
            if (line == null) {
                continue;
            }
            List<String> cols = Utils.tokenizeColumns(line, ",");
            for (int i = 0; i < cols.size(); i++) {
                String col = cols.get(i);
                System.out.println("#" + i + " " + col);
            }
        }
    }


    /**
     * _more_
     *
     * @param files _more_
     * @param out _more_
     *
     * @throws Exception _more_
     */
    public static void doDbXml(List<String> files, OutputStream out)
            throws Exception {
        PrintWriter writer = new PrintWriter(out);
        writer.println("<tables>");
        for (String file : files) {
            String name = IOUtil.stripExtension(IOUtil.getFileTail(file));
            String id   = name.toLowerCase().replaceAll(" ", "_");
            writer.println(XmlUtil.openTag("table",
                                           XmlUtil.attrs("id", id, "name",
                                               name, "icon",
                                                   "/db/database.png")));
            ProcessInfo info = new ProcessInfo();
            info.setDelimiter(",");
            info.setSkip(0);
            info.setInput(new BufferedInputStream(new FileInputStream(file)));
            Row       row1    = new Row(info.readLine(), info.getDelimiter());
            List<Row> samples = new ArrayList<Row>();
            for (int i = 0; i < 50; i++) {
                String line = info.readLine();
                if (line == null) {
                    break;
                }
                samples.add(new Row(line, info.getDelimiter()));
            }
            boolean[] isNumeric = new boolean[row1.getValues().size()];
            for (int i = 0; i < isNumeric.length; i++) {
                isNumeric[i] = false;
            }

            for (Row sample : samples) {
                for (int colIdx = 0; colIdx < sample.getValues().size();
                        colIdx++) {
                    Object value = sample.getValues().get(colIdx);
                    try {
                        Double.parseDouble(value.toString());
                        //                        System.err.println("OK: " + row1.getValues().get(colIdx));
                        isNumeric[colIdx] = true;
                    } catch (Exception ignore) {}
                }
            }

            for (int colIdx = 0; colIdx < row1.getValues().size(); colIdx++) {
                Object col = row1.getValues().get(colIdx);
                String colId = col.toString().toLowerCase().replaceAll(" ",
                                   "_").replaceAll("[^a-z0-9]", "_");
                boolean isNumber = isNumeric[colIdx];
                writer.println(XmlUtil.tag("column",
                                           XmlUtil.attrs(new String[] {
                    "name", colId, "type", isNumber
                                           ? "double"
                                           : "string", "label",
                    col.toString(), "cansearch", "true", "canlist", "true"
                })));
            }

            writer.println(XmlUtil.closeTag("table"));
        }
        writer.println("</tables>");
        writer.flush();
    }



    /**
     * Run through the csv file in the ProcessInfo
     *
     * @param info Holds input, output, skip, delimiter, etc
     *
     * @throws Exception On badness
     */
    public static void process(ProcessInfo info) throws Exception {
        int rowIdx      = 0;
        int visitedRows = 0;
        int cnt         = 0;
        while (true) {
            String line = info.readLine();
            if (line == null) {
                break;
            }

            if (line.length() > 1000) {
                //                System.err.println("Whoa:" +line);
                //                System.exit(0);
            }
            //            if(true) continue;


            theLine = line;
            rowIdx++;


            if (rowIdx <= info.getSkip()) {
                info.addHeaderLine(line);

                continue;
            }

            if ((info.getFilter() != null)
                    && !info.getFilter().lineOk(info, line)) {
                continue;
            }

            if (info.getDelimiter() == null) {
                String delimiter = ",";
                //Check for the bad separator
                int i1 = line.indexOf(",");
                int i2 = line.indexOf("|");
                if ((i2 >= 0) && ((i1 < 0) || (i2 < i1))) {
                    delimiter = "|";
                }
                info.setDelimiter(delimiter);
            }

            Row row = new Row(line, info.getDelimiter());

            if ((info.getFilter() != null)
                    && !info.getFilter().rowOk(info, row)) {
                continue;
            }


            if (info.getConverter() != null) {
                row = info.getConverter().convert(info, row);
                if (row == null) {
                    continue;
                }
            }

            visitedRows++;
            if ((info.getMaxRows() >= 0)
                    && (visitedRows > info.getMaxRows())) {
                break;
            }


            if (info.getProcessor() != null) {
                info.getProcessor().processRow(info, row, line);
            } else {
                info.getWriter().println(columnsToString(row.getValues(),
                        info.getOutputDelimiter()));
                info.getWriter().flush();
            }

            info.incrRow();


        }

        if (info.getProcessor() != null) {
            info.getProcessor().finish(info);
        }

    }


    /** _more_ */
    static int xcnt = 0;


    /**
     * _more_
     *
     * @param cols _more_
     * @param delimiter _more_
     *
     * @return _more_
     */
    public static String columnsToString(List cols, String delimiter) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < cols.size(); i++) {
            String s = cols.get(i).toString();
            if (i > 0) {
                sb.append(delimiter);
            }
            boolean needToQuote = false;
            if (s.indexOf("\n") >= 0) {
                needToQuote = true;
            } else if (s.indexOf(delimiter) >= 0) {
                needToQuote = true;
            }

            if (s.indexOf("\"") >= 0) {
                s           = s.replaceAll("\"", "\"\"");
                needToQuote = true;
            }

            if (needToQuote) {
                sb.append('"');
                sb.append(s);
                sb.append('"');
            } else {
                sb.append(s);
            }
        }

        return sb.toString();

    }




    /**
     * _more_
     *
     * @param msg _more_
     */
    public static void usage(String msg) {
        if (msg.length() > 0) {
            System.err.println(msg);
        }
        System.err.println(
            "CsvUtil"
            + "\n\t-columns <comma separated list of columns #s. 0-based>"
            + "\n\t-pattern <col #> <regexp pattern>\n\t\"<column=~<value>\" pattern search"
            + "\n\t<-gt|-ge|-lt|-le> <col #> <value>\n\t-skip <how many lines to skip>"
            + "\n\t-rotate" + "\n\t-flip"
            + "\n\t-cut <start row> <end row (-1 for end)>"
            + "\n\t-delete <col #>" + "\n\t-add <col #> <value>"
            + "\n\t-change <col #> <pattern> <substitution string>"
            + "\n\t-format <decimal format, e.g. '#'>\n\t-u (show unique values)\n\t-count (show count)"
            + "\n\t-delimiter (specify an alternative delimiter)"
            + "\n\t-header (pretty print the first line)\n\t-concat\n\t*.csv - one or more csv files"
            + "\n\t-db (generate the RAMADDA db xml from the header)");
        System.exit(0);
    }

    /**
     * _more_
     *
     * @param args _more_
     *
     * @throws Exception On badness
     */
    public static void main(String[] args) throws Exception {

        boolean      doConcat      = false;
        boolean      doDbXml       = false;
        boolean      doHeader      = false;

        String       iterateColumn = null;
        List<String> iterateValues = new ArrayList<String>();
        List<String> files         = new ArrayList<String>();

        ProcessInfo  info          = new ProcessInfo();


        List<String> extra         = new ArrayList<String>();
        for (int i = 0; i < args.length; i++) {
            String arg = args[i];

            if (arg.equals("-help")) {
                usage("");
            }

            if (arg.equals("-concat")) {
                doConcat = true;

                continue;
            }

            if (arg.equals("-db")) {
                doDbXml = true;

                continue;
            }

            if (arg.equals("-header")) {
                doHeader = true;

                continue;
            }

            if (arg.startsWith("-iter")) {
                iterateColumn = args[++i];
                iterateValues = StringUtil.split(args[++i], ",");

                continue;
            }
            extra.add(arg);
        }

        parseArgs(extra, info, files);

        if (doConcat) {
            concat(files, System.out);
        } else if (doDbXml) {
            doDbXml(files, System.out);
        } else if (doHeader) {
            header(files, System.out);
        } else {
            if (files.size() == 0) {
                files.add("stdin");
            }

            Filter.PatternFilter iteratePattern = null;
            if (iterateColumn == null) {
                iterateValues.add("dummy");
            } else {
                iteratePattern = new Filter.PatternFilter(iterateColumn, "");
                info.getFilter().addFilter(iteratePattern);
            }


            for (int i = 0; i < iterateValues.size(); i++) {
                String pattern = iterateValues.get(i);
                if (iteratePattern != null) {
                    iteratePattern.setPattern(pattern);
                }
                for (String file : files) {
                    info.getProcessor().reset();
                    InputStream is;
                    if (file.equals("stdin")) {
                        is = System.in;
                    } else {
                        is = new BufferedInputStream(
                            new FileInputStream(file));
                    }
                    process(info.cloneMe(is, System.out));
                }
            }
        }
    }





    /**
     * _more_
     *
     * @param args _more_
     * @param info _more_
     * @param files _more_
     *
     * @throws Exception _more_
     */
    public static void parseArgs(List<String> args, ProcessInfo info,
                                 List<String> files)
            throws Exception {

        Filter.FilterGroup subFilter     = null;
        Filter.FilterGroup filterToAddTo = null;
        //info.getFilter();


        for (int i = 0; i < args.size(); i++) {
            String arg = args.get(i);
            if (arg.equals("-format")) {
                info.setFormat(args.get(++i));

                continue;
            }

            if (arg.equals("-skip")) {
                info.setSkip(Integer.parseInt(args.get(++i)));

                continue;
            }

            if (arg.equals("-prefix")) {
                System.out.println(args.get(++i));

                continue;
            }

            if (arg.equals("-u")) {
                info.getProcessor().addProcessor(new Processor.Uniquifier());

                continue;
            }

            if (arg.equals("-count")) {
                info.getProcessor().addProcessor(new Processor.Counter());

                continue;
            }

            if (arg.equals("-sum")) {
                info.getProcessor().addProcessor(
                    new Processor.Operator(Processor.Operator.OP_SUM));

                continue;
            }

            if (arg.equals("-rotate")) {
                info.getProcessor().addProcessor(new Processor.Rotator());

                continue;
            }

            if (arg.equals("-flip")) {
                info.getProcessor().addProcessor(new Processor.Flipper());

                continue;
            }

            if (arg.equals("-delimiter")) {
                info.setDelimiter(args.get(++i));

                continue;
            }

            if (arg.equals("-output")) {
                String s = args.get(++i);
                if (s.equals("tab")) {
                    s = "\t";
                }
                info.setOutputDelimiter(s);

                continue;
            }

            if (arg.equals("-cut")) {
                String r1 = args.get(++i);
                String r2;
                int    idx = r1.indexOf(";");
                if (idx > 0) {
                    List<String> toks = StringUtil.splitUpTo(r1, ";", 2);
                    r1 = toks.get(0);
                    r2 = toks.get(1);
                } else {
                    r2 = args.get(++i);
                }

                info.getConverter().addConverter(
                // info.getFilter().addFilter(
                new Filter.Cutter(Integer.parseInt(r1),
                                  Integer.parseInt(r2)));

                continue;
            }

            if (arg.equals("-max")) {
                info.getProcessor().addProcessor(
                    new Processor.Operator(Processor.Operator.OP_MAX));

                continue;
            }

            if (arg.equals("-min")) {
                info.getProcessor().addProcessor(
                    new Processor.Operator(Processor.Operator.OP_MIN));

                continue;
            }

            if (arg.equals("-average")) {
                info.getProcessor().addProcessor(
                    new Processor.Operator(Processor.Operator.OP_AVERAGE));

                continue;
            }

            if (arg.equals("-columns")) {
                i++;
                List<String> cols = StringUtil.split(args.get(i), ",", true,
                                        true);
                info.setSelector(new Converter.ColumnSelector(cols));
                info.getConverter().addConverter(info.getSelector());

                continue;
            }

            if (arg.equals("-change")) {
                info.getConverter().addConverter(
                    new Converter.ColumnChanger(
                        args.get(++i), args.get(++i), args.get(++i)));

                continue;
            }


            if (arg.equals("-split")) {
                info.getConverter().addConverter(
                    new Converter.ColumnSplitter(
                        args.get(++i), args.get(++i)));

                continue;
            }

            if (arg.equals("-delete")) {
                info.getConverter().addConverter(
                    new Converter.ColumnDeleter(args.get(++i)));

                continue;
            }

            if (arg.equals("-add")) {
                info.getConverter().addConverter(
                    new Converter.ColumnAdder(args.get(++i), args.get(++i)));

                continue;
            }


            if (arg.equals("-or")) {
                filterToAddTo = new Filter.FilterGroup(false);
                info.getConverter().addConverter(filterToAddTo);

                continue;
            }

            if (arg.equals("-and")) {
                filterToAddTo = new Filter.FilterGroup(true);
                info.getConverter().addConverter(filterToAddTo);

                continue;
            }

            if (arg.equals("-pattern")) {
                String col     = args.get(++i);
                String pattern = args.get(++i);
                handlePattern(info, filterToAddTo,
                              new Filter.PatternFilter(col, pattern));

                continue;
            }

            if (arg.equals("-lt")) {
                handlePattern(info, filterToAddTo,
                              new Filter.ValueFilter(args.get(++i),
                                  Filter.ValueFilter.OP_LT,
                                  Double.parseDouble(args.get(++i))));

                continue;
            }

            if (arg.equals("-gt")) {
                handlePattern(info, filterToAddTo,
                              new Filter.ValueFilter(args.get(++i),
                                  Filter.ValueFilter.OP_GT,
                                  Double.parseDouble(args.get(++i))));

                continue;
            }


            if (arg.equals("-defined")) {
                handlePattern(info, filterToAddTo,
                              new Filter.ValueFilter(args.get(++i),
                                  Filter.ValueFilter.OP_DEFINED, 0));

                continue;
            }
            if (arg.startsWith("-")) {
                throw new IllegalArgumentException("Unknown arg:" + arg);
            }

            int idx;

            idx = arg.indexOf("!=");
            if (idx >= 0) {
                handlePattern(info, filterToAddTo,
                              new Filter.PatternFilter(arg.substring(0,
                                  idx).trim(), arg.substring(idx + 2).trim(),
                                      true));

                continue;
            }


            idx = arg.indexOf("=~");
            if (idx >= 0) {
                handlePattern(info, filterToAddTo,
                              new Filter.PatternFilter(arg.substring(0,
                                  idx).trim(), arg.substring(idx
                                  + 2).trim()));

                continue;
            }


            boolean didone = false;
            for (String op : new String[] { "<=", ">=", "<", ">", "=" }) {
                idx = arg.indexOf(op);
                if (idx >= 0) {
                    handlePattern(
                        info, filterToAddTo,
                        new Filter.ValueFilter(
                            arg.substring(0, idx).trim(),
                            Filter.ValueFilter.getOperator(op),
                            Double.parseDouble(
                                arg.substring(idx + op.length()).trim())));
                    didone = true;

                    break;
                }
            }
            if (didone) {
                continue;
            }

            files.add(arg);
        }

    }

    /**
     * _more_
     *
     * @param info _more_
     * @param filterToAddTo _more_
     * @param converter _more_
     */
    private static void handlePattern(ProcessInfo info,
                                      Filter.FilterGroup filterToAddTo,
                                      Filter converter) {
        if (filterToAddTo != null) {
            filterToAddTo.addFilter(converter);
        } else {
            info.getConverter().addConverter(converter);
        }
    }


}
