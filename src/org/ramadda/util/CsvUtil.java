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

package org.ramadda.util;


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
        int           rowIdx = 0;
        int           visitedRows = 0;
        int cnt = 0;
        while (true) {
            String line = readLine(info);
            if(line == null) break;

            if(line.length()>1000) {
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

            //            System.err.println("line:" + line);
                

            List<String> cols = Utils.tokenizeColumns(line,
                                                      info.getDelimiter());

            //            System.err.println("cols:" + cols);



            if ((filter != null) && !filter.rowOk(info, cols)) {
                continue;
            }

            visitedRows++;
            if ((info.getMaxRows() >= 0)
                    && (visitedRows > info.getMaxRows())) {
                break;
            }

            if (converter != null) {
                cols = converter.convert(info, cols);
            }

            if (processor != null) {
                processor.processRow(info, cols, line);
            } else {
                info.getWriter().println(columnsToString(cols, ","));
                info.getWriter().flush();
            }

            info.incrRow();


        }

        if (processor != null) {
            processor.finish(info);
        }

    }


    static int xcnt =0;
    private static String  readLine(ProcessInfo info) throws Exception {
        //        System.err.println("readline");
        StringBuilder lb     = new StringBuilder();
        int           c;
        boolean       inQuote     = false;
        int nextChar = -1;
        StringBuilder sb = new StringBuilder();
        char LINE_BREAK = '\n';
        while(true) {
            if(lb.length()>1500) {
                System.err.println("Whoa:" +lb);
                System.err.println(sb);
                System.exit(0);
            }

            c = info.getInput().read();
            if(c == -1) {
                break;
            }

            if (c == LINE_BREAK) {
                //                sb.append("\t************** new line:" + inQuote+"\n");
                if (!inQuote) {
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
                if(!inQuote) {
                    //                    sb.append("\tinto quote\n");
                    inQuote = true;
                } else {
                    nextChar = info.getInput().read();
                    if(nextChar == -1) {
                        break;
                    }
                    if(nextChar != '"') {
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


        String line    = lb.toString();
        if(line.length()==0) return null;
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

            if(s.indexOf("\"")>=0) {
                s = s.replaceAll("\"","\"\"");
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
            "CsvUtil" +
            "\n\t-columns <comma separated list of columns #s. 0-based>" +
            "\n\t-pattern <col #> <regexp pattern>\n\t\"<column=~<value>\" pattern search" +
            "\n\t<-gt|-ge|-lt|-le> <col #> <value>\n\t-skip <how many lines to skip>" +
            "\n\t-delete <col #>" +
            "\n\t-add <col #> <value>" +
            "\n\t-change <col #> <pattern> <substitution string>" +
            "\n\t-format <decimal format, e.g. '#'>\n\t-u (show unique values)\n\t-count (show count)" +
            "\n\t-header (pretty print the first line)\n\t-merge\n\t*.csv - one or more csv files"
                           );
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

        boolean        doMerge       = false;
        boolean        doHeader      = false;
        boolean        printFileName = false;
        boolean        reset         = true;
        int            skip          = 1;
        String iterateColumn = null;
        List<String> iterateValues = new ArrayList<String>();

        List<String>   files         = new ArrayList<String>();

        ColumnSelector selector = null;
        ProcessorGroup processor     = new ProcessorGroup();
        ConverterGroup converter     = new ConverterGroup();
        FilterGroup    filter        = new FilterGroup();
        String         format        = null;

        FilterGroup    subFilter     = null;

        FilterGroup    filterToAddTo = filter;

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

            if(arg.startsWith("-iter")) {
                iterateColumn = args[++i];
                iterateValues = StringUtil.split(args[++i],",");
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
                processor.addProcessor(new Uniquifier());
                continue;
            }

            if (arg.equals("-count")) {
                processor.addProcessor(new Counter());
                continue;
            }

            if (arg.equals("-sum")) {
                processor.addProcessor(new Operator(Operator.OP_SUM));
                continue;
            }

            if (arg.equals("-max")) {
                processor.addProcessor(new Operator(Operator.OP_MAX));
                continue;
            }

            if (arg.equals("-min")) {
                processor.addProcessor(new Operator(Operator.OP_MIN));
                continue;
            }

            if (arg.equals("-average")) {
                processor.addProcessor(new Operator(Operator.OP_AVERAGE));
                continue;
            }

            if (arg.equals("-columns")) {
                i++;
                List<String>   cols = StringUtil.split(args[i], ",", true, true);
                selector = new ColumnSelector(cols);
                converter.addConverter(selector);
                continue;
            }

            if (arg.equals("-change")) {
                converter.addConverter(new  ColumnChanger(args[++i],
                                                          args[++i],
                                                          args[++i]));
                continue;
            }


            if (arg.equals("-split")) {
                converter.addConverter(new  ColumnSplitter(args[++i],
                                                          args[++i]));
                continue;
            }

            if (arg.equals("-delete")) {
                converter.addConverter(new  ColumnDeleter(args[++i]));
                continue;
            }

            if (arg.equals("-add")) {
                converter.addConverter(new  ColumnAdder(args[++i],args[++i]));
                continue;
            }


            if(arg.equals("-or")) {
                subFilter = new FilterGroup(false);
                filter.addFilter(subFilter);
                filterToAddTo= subFilter;
                continue;
            }

            if(arg.equals("-and")) {
                subFilter = new FilterGroup(true);
                filter.addFilter(subFilter);
                filterToAddTo= subFilter;
                continue;
            }

            if (arg.equals("-pattern")) {
                String    col     = args[++i];
                String pattern = args[++i];
                filterToAddTo.addFilter(new PatternFilter(col, pattern));

                continue;
            }

            if (arg.equals("-lt")) {
                filterToAddTo.addFilter(new ValueFilter(args[++i], ValueFilter.OP_LT,
                                                 Double.parseDouble(args[++i])));

                continue;
            }

            if (arg.equals("-gt")) {
                filterToAddTo.addFilter(new ValueFilter(args[++i], ValueFilter.OP_GT,
                        Double.parseDouble(args[++i])));

                continue;
            }


            if (arg.equals("-defined")) {
                filterToAddTo.addFilter(new ValueFilter(args[++i], ValueFilter.OP_DEFINED,
                        0));

                continue;
            }
            if (arg.startsWith("-")) {
                usage("Unknown arg:" + arg);
            }

            int idx;

            idx = arg.indexOf("!=");
            if(idx>=0) {
                filterToAddTo.addFilter(new PatternFilter(arg.substring(0,idx).trim(), arg.substring(idx+2).trim(),true));
                continue;
            }


            idx = arg.indexOf("=~");
            if(idx>=0) {
                filterToAddTo.addFilter(new PatternFilter(arg.substring(0,idx).trim(), arg.substring(idx+2).trim()));
                continue;
            }


            boolean didone = false;
            for(String op: new String[]{"<=",">=","<",">","="}) {
                idx = arg.indexOf(op);
                if(idx>=0) {
                    filterToAddTo.addFilter(new ValueFilter(arg.substring(0,idx).trim(), ValueFilter.getOperator(op),
                                                            Double.parseDouble(arg.substring(idx+op.length()).trim())));
                    didone = true;
                    break;
                }
            }
            if(didone) continue;

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

            PatternFilter iteratePattern = null;
            if(iterateColumn==null) {
                iterateValues.add("dummy");
            } else {
                iteratePattern = new PatternFilter(iterateColumn, "");
                filter.addFilter(iteratePattern);
            }




            for(int i=0;i<iterateValues.size();i++) {
                String pattern = iterateValues.get(i);
                if(iteratePattern!=null) {
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
                        is = new BufferedInputStream(new FileInputStream(file));
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


    /**
     * Class description
     *
     *
     * @version        $version$, Fri, Jan 9, '15
     * @author         Jeff McWhirter
     */
    public abstract static class Converter {

        private int index =-1;
        private String scol;

        public Converter() {
        }

        public Converter(String col) {
            scol = col;
        }

        /**
         * _more_
         *
         * @param cols _more_
         *
         * @return _more_
         */
        public abstract List<String> convert(ProcessInfo info, List<String> cols);

        public int getIndex(ProcessInfo info) {
            if(index<0) {
                index=  info.getColumnIndex(scol);
            }
            return index;
        }


    }


    public static class ConverterGroup extends Converter {

        private List<Converter> converters = new ArrayList<Converter>();

        public ConverterGroup(){

        }

        public void addConverter(Converter converter) {
            converters.add(converter);
        }

        /**
         * _more_
         *
         * @param cols _more_
         *
         * @return _more_
         */
        public  List<String> convert(ProcessInfo info, List<String> cols) {
            for(Converter child: converters) {
                cols = child.convert(info, cols);
            }
            return cols;
        }

    }

    /**
     * Class description
     *
     *
     * @version        $version$, Fri, Jan 9, '15
     * @author         Jeff McWhirter
     */
    public static class ColumnSelector extends Converter {

        List<String> sindices;

        /** _more_ */
        List<Integer> indices;

        /**
         * _more_
         *
         * @param indices _more_
         */
        public ColumnSelector(List<String> cols) {
            this.sindices = cols;
        }

        public List<Integer>getIndices(ProcessInfo info) {
            if (indices == null) {
                indices = new ArrayList<Integer>();
                for(String s: sindices) {
                    int idx =  info.getColumnIndex(s);
                    if(idx>=0)  {
                        indices.add(idx);
                    } else {
                        throw new IllegalStateException("Bad column: " + s);
                    }
                }
            }
            return indices;
        }


        /**
         * _more_
         *
         * @param cols _more_
         *
         * @return _more_
         */
        public List<String> convert(ProcessInfo info, List<String> cols) {

            getIndices(info);
            if (indices == null) {
                return cols;
            }
            List<String> result = new ArrayList<String>();
            for (Integer idx : indices) {
                if (idx < cols.size()) {
                    String s = cols.get(idx);
                    //                    if(s.indexOf("ROOFING") >=0) {
                    //                        System.err.println("Line:" + theLine);
                    //                        System.err.println("Cols:" + cols);
                    //                    }
                    result.add(s);
                }
            }

            return result;
        }

    }


    public static class ColumnChanger extends Converter {

        private String pattern;
        private String value;

        /**
         * _more_
         *
         * @param indices _more_
         */
        public ColumnChanger(String col, String pattern, String value) {
            super(col);
            this.pattern = pattern;
            this.value = value;
        }

        /**
         * _more_
         *
         * @param cols _more_
         *
         * @return _more_
         */
        public List<String> convert(ProcessInfo info, List<String> cols) {
            int index = getIndex(info);
            if (index<0 || index>=cols.size()) {
                return cols;
            }
            String s = cols.get(index);
            s = s.replaceAll(pattern,value);

            cols.set(index,s);
            return cols;
        }

    }




    public static class ColumnSplitter extends Converter {


        private String delimiter;


        /**
         * _more_
         *
         * @param indices _more_
         */
        public ColumnSplitter(String col, String delimiter) {
            super(col);
            this.delimiter = delimiter;
        }

        /**
         * _more_
         *
         * @param cols _more_
         *
         * @return _more_
         */
        public List<String> convert(ProcessInfo info, List<String> cols) {
            int index = getIndex(info);            
            if (index<0 || index>=cols.size()) {
                return cols;
            }
            cols.remove(index);
            int colOffset =0;
            for(String tok: StringUtil.split(cols.get(index),delimiter)) {
                cols.add(index+(colOffset++),tok);
            }
            return cols;
        }

    }



    public static class ColumnDeleter extends Converter {

        /**
         * _more_
         *
         * @param indices _more_
         */
        public ColumnDeleter(String col) {
            super(col);
        }

        /**
         * _more_
         *
         * @param cols _more_
         *
         * @return _more_
         */
        public List<String> convert(ProcessInfo info, List<String> cols) {
            int index = getIndex(info);            
            if (index<0 || index>=cols.size()) {
                return cols;
            }
            cols.remove(index);
            return cols;
        }

    }


    public static class ColumnAdder extends Converter {

        private String value;


        /**
         * _more_
         *
         * @param indices _more_
         */
        public ColumnAdder(String col, String value) {
            super(col);
            this.value = value;
        }

        /**
         * _more_
         *
         * @param cols _more_
         *
         * @return _more_
         */
        public List<String> convert(ProcessInfo info, List<String> cols) {
            int index = getIndex(info);            
            if (index<0 || index>=cols.size()) {
                return cols;
            }
            cols.add(index, value);
            return cols;
        }

    }

    /**
     * Class description
     *
     *
     * @version        $version$, Fri, Jan 9, '15
     * @author         Jeff McWhirter
     */
    public static abstract class Processor {

        /**
         * _more_
         *
         *
         * @param info _more_
         * @param toks _more_
         * @param line _more_
         */
        public void processRow(ProcessInfo info, List<String> toks,
                               String line) {}

        /**
         * _more_
         *
         * @param info _more_
         *
         * @throws Exception On badness
         */
        public void finish(ProcessInfo info) throws Exception {}

        /**
         * _more_
         */
        public void reset() {}
    }

    /**
     * Class description
     *
     *
     * @version        $version$, Sat, Jan 10, '15
     * @author         Jeff McWhirter
     */
    public static class ProcessorGroup extends Processor {

        /** _more_ */
        private List<Processor> processors = new ArrayList<Processor>();

        /**
         * _more_
         */
        public ProcessorGroup() {}

        /**
         * _more_
         *
         * @param processor _more_
         */
        public void addProcessor(Processor processor) {
            processors.add(processor);
        }


        /**
         * _more_
         */
        public void reset() {
            for (Processor processor : processors) {
                processor.reset();
            }
        }

        /**
         * _more_
         *
         * @param info _more_
         * @param columns _more_
         * @param line _more_
         */
        @Override
        public void processRow(ProcessInfo info, List<String> columns,
                               String line) {
            if (processors.size() == 0) {
                if(info.getRow()==0) {
                    //not now
                    for(String header:info.getHeaderLines()) {
                        //                        info.getWriter().println(header);
                    }
                }
                //                System.err.println ("cols:" + columns);
                info.getWriter().println(columnsToString(columns, ","));
                info.getWriter().flush();
            }
            for (Processor processor : processors) {
                processor.processRow(info, columns, line);
            }
        }

        /**
         * _more_
         *
         * @param info _more_
         *
         * @throws Exception On badness
         */
        @Override
        public void finish(ProcessInfo info) throws Exception {
            for (int i = 0; i < processors.size(); i++) {
                Processor processor = processors.get(i);
                if (i > 0) {
                    info.getWriter().print(",");
                }
                processor.finish(info);
            }
            if (processors.size() > 0) {
                info.getWriter().print("\n");
            }
            info.getWriter().flush();
        }
    }



    /**
     * Class description
     *
     *
     * @version        $version$, Fri, Jan 9, '15
     * @author         Jeff McWhirter
     */
    public static class Operator extends Processor {

        /** _more_ */
        public static final int OP_SUM = 0;

        /** _more_ */
        public static final int OP_MIN = 1;

        /** _more_ */
        public static final int OP_MAX = 2;

        /** _more_ */
        public static final int OP_AVERAGE = 3;


        /** _more_ */
        private int op = OP_SUM;

        /** _more_ */
        private List<Double> values;

        /** _more_ */
        private List<Integer> counts;

        /**
         * _more_
         */
        public Operator() {}

        /**
         * _more_
         *
         * @param op _more_
         */
        public Operator(int op) {
            this.op = op;
        }

        /**
         * _more_
         */
        public void reset() {
            values = null;
            counts = null;
        }

        /**
         * _more_
         *
         *
         * @param info _more_
         * @param toks _more_
         * @param line _more_
         */
        @Override
        public void processRow(ProcessInfo info, List<String> toks,
                               String line) {
            boolean first = false;
            if (values == null) {
                values = new ArrayList<Double>();
                counts = new ArrayList<Integer>();
                first  = true;
            }
            for (int i = 0; i < toks.size(); i++) {
                if (i >= values.size()) {
                    values.add(new Double(0));
                    counts.add(new Integer(0));
                }
                try {
                    String s            = toks.get(i).trim();
                    double value        = (s.length() == 0)
                                          ? 0
                                          : new Double(s).doubleValue();
                    double currentValue = values.get(i).doubleValue();
                    double newValue     = 0;
                    if (op == OP_SUM) {
                        newValue = currentValue + value;
                    } else if (op == OP_MIN) {
                        newValue = first
                                   ? value
                                   : Math.min(value, currentValue);
                    } else if (op == OP_MAX) {
                        newValue = first
                                   ? value
                                   : Math.max(value, currentValue);
                    } else if (op == OP_AVERAGE) {
                        newValue = currentValue + value;
                    } else {
                        System.err.println("NA:" + op);
                    }
                    values.set(i, newValue);
                    counts.set(i, new Integer(counts.get(i).intValue() + 1));
                } catch (Exception exc) {
                    //                    System.err.println("err:" + exc);
                    //                    System.err.println("line:" + theLine);
                }
            }
        }

        /**
         * _more_
         *
         * @param info _more_
         *
         * @throws Exception On badness
         */
        @Override
        public void finish(ProcessInfo info) throws Exception {
            if (values == null) {
                ColumnSelector selector = info.getSelector ();
                if(selector!=null && selector.getIndices(info)!=null) {
                    for(int i=0;i<selector.getIndices(info).size();i++)  {
                        if(i>0) 
                            info.getWriter().print(",");
                        info.getWriter().print("-0");
                    }
                } else {
                    info.getWriter().print("-0");
                }
                //                System.err.println("no values");
            } else {
                for (int i = 0; i < values.size(); i++) {
                    double value = values.get(i);
                    if (op == OP_AVERAGE) {
                        value = value / counts.get(i);
                    }
                    if (i > 0) {
                        info.getWriter().print(",");
                    }
                    info.getWriter().print(info.formatValue(value));
                }
            }
            info.getWriter().flush();
        }

    }




    /**
     * Class description
     *
     *
     * @version        $version$, Fri, Jan 9, '15
     * @author         Jeff McWhirter
     */
    public static class Uniquifier extends Processor {

        /** _more_ */
        private List<HashSet> contains;

        /** _more_ */
        private List<List> values;

        /**
         * _more_
         */
        public Uniquifier() {}

        /**
         * _more_
         */
        public void reset() {
            contains = null;
            values   = null;
        }

        /**
         * _more_
         *
         *
         * @param info _more_
         * @param toks _more_
         * @param line _more_
         */
        @Override
        public void processRow(ProcessInfo info, List<String> toks,
                               String line) {
            boolean first = false;
            if (contains == null) {
                contains = new ArrayList<HashSet>();
                values   = new ArrayList<List>();
            }
            for (int i = 0; i < toks.size(); i++) {
                if (i >= values.size()) {
                    contains.add(new HashSet());
                    values.add(new ArrayList());
                }
                String s = toks.get(i).trim();
                if (contains.get(i).contains(s)) {
                    continue;
                }
                contains.get(i).add(s);
                values.get(i).add(s);
            }
        }

        /**
         *   _more_
         *
         *   @param info _more_
         *
         *   @throws Exception On badness
         */
        @Override
        public void finish(ProcessInfo info) throws Exception {
            if (contains == null) {
                info.getWriter().print("-0");
            } else {
                for (int i = 0; i < values.size(); i++) {
                    List uniqueValues = values.get(i);
                    for (int j = 0; j < uniqueValues.size(); j++) {
                        if (j > 0) {
                            //                            info.getWriter().print(",");
                        }
                        info.getWriter().println(uniqueValues.get(j));
                    }
                }
            }
            info.getWriter().flush();
        }

    }


    /**
     * Class description
     *
     *
     * @version        $version$, Mon, Jan 12, '15
     * @author         Enter your name here...    
     */
    public static class Counter extends Processor {

        /** _more_ */
        private int count;

        private HashSet<Integer> uniqueCounts = new HashSet<Integer>();

        /**
         * _more_
         */
        public Counter() {}

        /**
         * _more_
         */
        public void reset() {
            count = 0;
        }

        /**
         * _more_
         *
         *
         * @param info _more_
         * @param toks _more_
         * @param line _more_
         */
        @Override
        public void processRow(ProcessInfo info, List<String> toks,
                               String line) {
            uniqueCounts.add(toks.size());
            count++;
        }

        /**
         *   _more_
         *
         *   @param info _more_
         *
         *   @throws Exception On badness
         */
        @Override
        public void finish(ProcessInfo info) throws Exception {
            info.getWriter().print(count);
            //            System.err.println(uniqueCounts);
        }

    }





    /**
     * Class description
     *
     *
     * @version        $version$, Fri, Jan 9, '15
     * @author         Jeff McWhirter
     */
    public abstract static class Filter {

        /** _more_ */
        private String commentPrefix = "#";


        /**
         * _more_
         */
        public Filter() {}

        /**
         * _more_
         *
         *
         * @param info _more_
         * @param toks _more_
         *
         * @return _more_
         */
        public boolean rowOk(ProcessInfo info, List<String> toks) {
            return true;
        }

        /**
         * _more_
         *
         * @param info _more_
         * @param line _more_
         *
         * @return _more_
         */
        public boolean lineOk(ProcessInfo info, String line) {
            if ((commentPrefix != null) && line.startsWith(commentPrefix)) {
                return false;
            }

            return true;
        }

    }

    /**
     * Class description
     *
     *
     * @version        $version$, Fri, Jan 9, '15
     * @author         Jeff McWhirter
     */
    public abstract static class ColumnFilter extends Filter {

        /** _more_ */
        private int col = -1;

        private String scol;
        
        private boolean negate = false;

        /**
         * _more_
         *
         * @param col _more_
         */
        public ColumnFilter(int col) {
            this.col = col;
        }


        public ColumnFilter(String scol) {
            this.scol = scol;
        }

        public ColumnFilter(String scol, boolean negate) {
            this(scol);
            this.negate = negate;
        }

        public boolean doNegate(boolean b) {
            if(negate) return !b;
            return b;
        }

        public int getIndex(ProcessInfo info) {
            if(col>=0) return col;
            if(scol == null) return -11;
            col = info.getColumnIndex(scol);
            return col;
        }


    }

    /**
     * Class description
     *
     *
     * @version        $version$, Fri, Jan 9, '15
     * @author         Jeff McWhirter
     */
    public static class FilterGroup extends Filter {

        /** _more_ */
        List<Filter> filters = new ArrayList<Filter>();

        private boolean andLogic = true;


        /**
         * _more_
         */
        public FilterGroup() {}

        public FilterGroup(boolean andLogic) {
            this.andLogic = andLogic;
        }

        /**
         * _more_
         *
         * @param filter _more_
         */
        public void addFilter(Filter filter) {
            filters.add(filter);
        }

        /**
         * _more_
         *
         *
         * @param info _more_
         * @param toks _more_
         *
         * @return _more_
         */
        @Override
        public boolean rowOk(ProcessInfo info, List<String> toks) {
            if (filters.size() == 0) {
                return true;
            }
            for (Filter filter : filters) {
                if ( !filter.rowOk(info, toks)) {
                    if(andLogic) return false;
                } else {
                    if(!andLogic) return true;
                }
            }

            if(!andLogic) return false;
            return true;
        }
    }




    /**
     * Class description
     *
     *
     * @version        $version$, Fri, Jan 9, '15
     * @author         Jeff McWhirter
     */
    public static class PatternFilter extends ColumnFilter {

        /** _more_ */
        Pattern pattern;

        /**
         * _more_
         *
         * @param col _more_
         * @param pattern _more_
         */
        public PatternFilter(int col, String pattern) {
            super(col);
            this.pattern = Pattern.compile(pattern);
        }


        public PatternFilter(String col, String pattern, boolean negate) {
            super(col, negate);
            setPattern(pattern);
        }


        public PatternFilter(String col, String pattern) {
            super(col);
            setPattern(pattern);
        }

        public void setPattern(String pattern) {
            this.pattern = Pattern.compile(pattern);
        }


        /**
         * _more_
         *
         *
         * @param info _more_
         * @param toks _more_
         *
         * @return _more_
         */
        public boolean rowOk(ProcessInfo info, List<String> toks) {
            int idx = getIndex(info);
            if (idx >= toks.size()) {
                return doNegate(false);
            }
            String v = toks.get(idx);
            if (pattern.matcher(v).find()) {
                return doNegate(true);
            }

            return doNegate(false);
        }

    }


    /**
     * Class description
     *
     *
     * @version        $version$, Mon, Jan 12, '15
     * @author         Enter your name here...
     */
    public static class ValueFilter extends ColumnFilter {

        /** _more_ */
        public static int OP_LT = 0;

        /** _more_ */
        public static int OP_LE = 1;

        /** _more_ */
        public static int OP_GT = 2;

        /** _more_ */
        public static int OP_GE = 3;

        /** _more_ */
        public static int OP_EQUALS = 4;

        /** _more_ */
        public static int OP_DEFINED = 5;

        /** _more_ */
        private int op;

        /** _more_ */
        private double value;

        /**
         * _more_
         *
         * @param col _more_
         * @param pattern _more_
         * @param op _more_
         * @param value _more_
         */
        public ValueFilter(int col, int op, double value) {
            super(col);
            this.op    = op;
            this.value = value;
        }



        public ValueFilter(String col, int op, double value) {
            super(col);
            this.op    = op;
            this.value = value;
        }



        /**
         * _more_
         *
         * @param s _more_
         *
         * @return _more_
         */
        public static int getOperator(String s) {
            s =s.trim();
            if (s.equals("<")) {
                return OP_LT;
            }
            if (s.equals("<=")) {
                return OP_LE;
            }
            if (s.equals(">")) {
                return OP_GT;
            }
            if (s.equals(">=")) {
                return OP_GE;
            }
            if (s.equals("=")) {
                return OP_EQUALS;
            }

            return -1;
        }


        /**
         * _more_
         *
         *
         * @param info _more_
         * @param toks _more_
         *
         * @return _more_
         */
        public boolean rowOk(ProcessInfo info, List<String> toks) {
            int idx = getIndex(info);
            if (idx >= toks.size()) {
                return false;
            }
            try {
                String v     = toks.get(idx);
                double value = Double.parseDouble(v);
                if (op == OP_LT) {
                    return value < this.value;
                }
                if (op == OP_LE) {
                    return value <= this.value;
                }
                if (op == OP_GT) {
                    return value > this.value;
                }
                if (op == OP_GE) {
                    return value >= this.value;
                }
                if (op == OP_EQUALS) {
                    return value == this.value;
                }
                if (op == OP_DEFINED) {
                    return value == value;
                }

                return false;
            } catch (Exception exc) {}

            return false;

        }

    }

    /**
     * Class description
     *
     *
     * @version        $version$, Sat, Jan 10, '15
     * @author         Jeff McWhirter
     */
    public static class ProcessInfo {

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

        private int row=0;


        private List<String> headerLines = new ArrayList<String>();

        private  Hashtable<String,Integer> columnMap;

        private List<String> columnNames;

        /** _more_ */
        private DecimalFormat format;

        private ColumnSelector selector;


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
           Set the Selector property.

           @param value The new value for Selector
        **/
        public void setSelector (ColumnSelector value) {
            selector = value;
        }

        /**
           Get the Selector property.

           @return The Selector
        **/
        public ColumnSelector getSelector () {
            return selector;
        }



        public int getColumnIndex(String s) {
            try {
                return  Integer.parseInt(s);
            } catch(NumberFormatException exc) {
            }
            if(columnNames==null) {
                if(headerLines.size()==0) return -1;
                columnMap = new Hashtable<String,Integer>();
                columnNames = StringUtil.split(headerLines.get(0),delimiter);
            }
            Integer iv = columnMap.get(s);
            if(iv != null)  {
                return iv.intValue();
            }
            for(int i=0;i<columnNames.size();i++) {
                String v = columnNames.get(i);
                if(v.startsWith(s)) {
                    columnMap.put(v,i);
                    return i;
                }
            }

            return -1;
        }


        public void incrRow () {
            row++;
        }


        /**
           Get the Row property.

           @return The Row
        **/
        public int getRow () {
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


        public void  addHeaderLine(String line) {
            headerLines.add(line);
        }

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


}
