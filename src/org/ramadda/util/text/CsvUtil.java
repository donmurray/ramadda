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
    public static void merge(List<String> files, OutputStream out)
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
     * Run through the csv file in the ProcessInfo
     *
     * @param info Holds input, output, skip, delimiter, etc
     * @param filter determines whether to process a row
     * @param converter Convert the columns
     * @param processor Process the columns
     *
     * @throws Exception On badness
     */
    public static void process(ProcessInfo info, Filter filter,
                               Converter converter, Processor processor)
            throws Exception {
        int rowIdx      = 0;
        int visitedRows = 0;
        int cnt         = 0;
        while (true) {
            String line = readLine(info);
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

            if ((filter != null) && !filter.lineOk(info, line)) {

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

            if ((filter != null) && !filter.rowOk(info, row)) {
                continue;
            }

            visitedRows++;
            if ((info.getMaxRows() >= 0)
                    && (visitedRows > info.getMaxRows())) {
                break;
            }

            if (converter != null) {
                row = converter.convert(info, row);
            }

            if (processor != null) {
                processor.processRow(info, row, line);
            } else {
                info.getWriter().println(columnsToString(row.getValues(),
                        ","));
                info.getWriter().flush();
            }

            info.incrRow();


        }

        if (processor != null) {
            processor.finish(info);
        }

    }


    /** _more_ */
    static int xcnt = 0;

    /**
     * _more_
     *
     * @param info _more_
     *
     * @return _more_
     *
     * @throws Exception _more_
     */
    private static String readLine(ProcessInfo info) throws Exception {
        //        System.err.println("readline");
        StringBuilder lb = new StringBuilder();
        int           c;
        boolean       inQuote    = false;
        int           nextChar   = -1;
        StringBuilder sb         = new StringBuilder();
        char          LINE_BREAK = '\n';
        while (true) {
            if (lb.length() > 1500) {
                System.err.println("Whoa:" + lb);
                System.err.println(sb);
                System.exit(0);
            }

            c = info.getInput().read();
            if (c == -1) {
                break;
            }

            if (c == LINE_BREAK) {
                //                sb.append("\t************** new line:" + inQuote+"\n");
                if ( !inQuote) {
                    break;
                }
            } else if (c == '\r') {
                //                sb.append("\tcr:" + inQuote+"\n");
            } else {
                //sb.append("\tchar:" + (char)c+"  " + inQuote +"\n");
            }
            lb.append((char) c);
            if (c == '"') {
                //                sb.append("\tquote: "  + inQuote+"\n");
                if ( !inQuote) {
                    //                    sb.append("\tinto quote\n");
                    inQuote = true;
                } else {
                    nextChar = info.getInput().read();
                    if (nextChar == -1) {
                        break;
                    }
                    if (nextChar != '"') {
                        //                        sb.append("\tout quote\n");
                        inQuote = false;
                        if (nextChar == LINE_BREAK) {
                            //                            sb.append("\t************** new line:" + inQuote+"\n");
                            break;
                        }
                    }
                    //                    sb.append("\tnext char:" +(char) nextChar+"\n");
                    lb.append((char) nextChar);
                }
            }

        }
        //        System.out.println(sb);


        String line = lb.toString();
        if (line.length() == 0) {
            return null;
        }

        return line;
    }


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
            + "\n\t-delete <col #>" + "\n\t-add <col #> <value>"
            + "\n\t-change <col #> <pattern> <substitution string>"
            + "\n\t-format <decimal format, e.g. '#'>\n\t-u (show unique values)\n\t-count (show count)"
            + "\n\t-header (pretty print the first line)\n\t-merge\n\t*.csv - one or more csv files");
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

        boolean                  doMerge       = false;
        boolean                  doHeader      = false;
        boolean                  printFileName = false;
        boolean                  reset         = true;
        int                      skip          = 1;
        String                   iterateColumn = null;
        List<String>             iterateValues = new ArrayList<String>();

        List<String>             files         = new ArrayList<String>();

        Converter.ColumnSelector selector      = null;
        Processor.ProcessorGroup processor = new Processor.ProcessorGroup();
        Converter.ConverterGroup converter = new Converter.ConverterGroup();
        Filter.FilterGroup       filter        = new Filter.FilterGroup();
        Filter.FilterGroup       subFilter     = null;
        Filter.FilterGroup       filterToAddTo = filter;

        String                   format        = null;
        for (int i = 0; i < args.length; i++) {
            String arg = args[i];

            if (arg.equals("-help")) {
                usage("");
            }

            if (arg.equals("-merge")) {
                doMerge = true;

                continue;
            }

            if (arg.equals("-noreset")) {
                reset = false;

                continue;
            }

            if (arg.equals("-printfile")) {
                printFileName = true;

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


            if (arg.equals("-format")) {
                format = args[++i];

                continue;
            }

            if (arg.equals("-skip")) {
                skip = Integer.parseInt(args[++i]);

                continue;
            }


            if (arg.equals("-prefix")) {
                i++;
                System.out.println(args[i]);

                continue;
            }

            if (arg.equals("-u")) {
                processor.addProcessor(new Processor.Uniquifier());

                continue;
            }

            if (arg.equals("-count")) {
                processor.addProcessor(new Processor.Counter());

                continue;
            }

            if (arg.equals("-sum")) {
                processor.addProcessor(
                    new Processor.Operator(Processor.Operator.OP_SUM));

                continue;
            }

            if (arg.equals("-max")) {
                processor.addProcessor(
                    new Processor.Operator(Processor.Operator.OP_MAX));

                continue;
            }

            if (arg.equals("-min")) {
                processor.addProcessor(
                    new Processor.Operator(Processor.Operator.OP_MIN));

                continue;
            }

            if (arg.equals("-average")) {
                processor.addProcessor(
                    new Processor.Operator(Processor.Operator.OP_AVERAGE));

                continue;
            }

            if (arg.equals("-columns")) {
                i++;
                List<String> cols = StringUtil.split(args[i], ",", true,
                                        true);
                selector = new Converter.ColumnSelector(cols);
                converter.addConverter(selector);

                continue;
            }

            if (arg.equals("-change")) {
                converter.addConverter(new Converter.ColumnChanger(args[++i],
                        args[++i], args[++i]));

                continue;
            }


            if (arg.equals("-split")) {
                converter.addConverter(
                    new Converter.ColumnSplitter(args[++i], args[++i]));

                continue;
            }

            if (arg.equals("-delete")) {
                converter.addConverter(
                    new Converter.ColumnDeleter(args[++i]));

                continue;
            }

            if (arg.equals("-add")) {
                converter.addConverter(new Converter.ColumnAdder(args[++i],
                        args[++i]));

                continue;
            }


            if (arg.equals("-or")) {
                subFilter = new Filter.FilterGroup(false);
                filter.addFilter(subFilter);
                filterToAddTo = subFilter;

                continue;
            }

            if (arg.equals("-and")) {
                subFilter = new Filter.FilterGroup(true);
                filter.addFilter(subFilter);
                filterToAddTo = subFilter;

                continue;
            }

            if (arg.equals("-pattern")) {
                String col     = args[++i];
                String pattern = args[++i];
                filterToAddTo.addFilter(new Filter.PatternFilter(col,
                        pattern));

                continue;
            }

            if (arg.equals("-lt")) {
                filterToAddTo.addFilter(new Filter.ValueFilter(args[++i],
                        Filter.ValueFilter.OP_LT,
                        Double.parseDouble(args[++i])));

                continue;
            }

            if (arg.equals("-gt")) {
                filterToAddTo.addFilter(new Filter.ValueFilter(args[++i],
                        Filter.ValueFilter.OP_GT,
                        Double.parseDouble(args[++i])));

                continue;
            }


            if (arg.equals("-defined")) {
                filterToAddTo.addFilter(new Filter.ValueFilter(args[++i],
                        Filter.ValueFilter.OP_DEFINED, 0));

                continue;
            }
            if (arg.startsWith("-")) {
                usage("Unknown arg:" + arg);
            }

            int idx;

            idx = arg.indexOf("!=");
            if (idx >= 0) {
                filterToAddTo.addFilter(
                    new Filter.PatternFilter(
                        arg.substring(0, idx).trim(),
                        arg.substring(idx + 2).trim(), true));

                continue;
            }


            idx = arg.indexOf("=~");
            if (idx >= 0) {
                filterToAddTo.addFilter(
                    new Filter.PatternFilter(
                        arg.substring(0, idx).trim(),
                        arg.substring(idx + 2).trim()));

                continue;
            }


            boolean didone = false;
            for (String op : new String[] { "<=", ">=", "<", ">", "=" }) {
                idx = arg.indexOf(op);
                if (idx >= 0) {
                    filterToAddTo.addFilter(
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


        if (doMerge) {
            merge(files, System.out);
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
                filter.addFilter(iteratePattern);
            }




            for (int i = 0; i < iterateValues.size(); i++) {
                String pattern = iterateValues.get(i);
                if (iteratePattern != null) {
                    iteratePattern.setPattern(pattern);
                }
                for (String file : files) {
                    if (reset) {
                        processor.reset();
                    }
                    InputStream is;
                    if (file.equals("stdin")) {
                        is = System.in;
                    } else {
                        is = new BufferedInputStream(
                            new FileInputStream(file));
                    }
                    ProcessInfo info = new ProcessInfo(is, System.out);
                    info.setSelector(selector);
                    info.setSkip(skip);
                    if (format != null) {
                        info.setFormat(format);
                    }
                    if (printFileName) {
                        System.out.println("");
                        System.out.println(file);
                    }




                    process(info, filter, converter, processor);
                }
            }
        }
    }



}
