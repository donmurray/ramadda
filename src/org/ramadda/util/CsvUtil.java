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
import java.text.SimpleDateFormat;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import java.util.regex.*;


/**
 *
 * @author Jeff McWhirter
 */

public class CsvUtil {


    /** a hack for debugging  */
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
        String        line;
        int           rowIdx = 0;

        StringBuilder lb     = new StringBuilder();
        int           c;

        boolean       inQuote = false;
        int visitedRows = 0;
        while ((c = info.getInput().read()) != -1) {
            if (c != '\n') {
                if (c == '"') {
                    inQuote = !inQuote;
                }
                lb.append((char) c);

                continue;
            }
            if (inQuote) {
                lb.append((char) c);

                continue;
            }
            line    = lb.toString();
            lb      = new StringBuilder();
            theLine = line;
            rowIdx++;
            if (rowIdx <= info.getSkip()) {
                if (processor == null) {
                    //TODO: What to do with header lines
                    //                    info.getWriter().println(line);
                }
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

            List<String> cols = Utils.tokenizeColumns(line,
                                    info.getDelimiter());

            if ((filter != null) && !filter.rowOk(info, cols)) {
                continue;
            }


            visitedRows++;
            if(info.getMaxRows()>=0 && visitedRows>info.getMaxRows()) {
                break;
            }

            if (converter != null) {
                cols = converter.convert(cols);
            }

            if (processor != null) {
                processor.processRow(info, cols);
            } else {
                info.getWriter().println(StringUtil.join(",", cols));
                info.getWriter().flush();
            }
        }

        if (processor != null) {
            processor.finish(info);
        }

    }


    /**
     * _more_
     *
     * @param args _more_
     *
     * @throws Exception On badness
     */
    public static void main(String[] args) throws Exception {
        boolean        doMerge   = false;


        List<String>   files     = new ArrayList<String>();
        List<Integer>  cols      = null;
        ProcessorGroup processor = new ProcessorGroup();
        FilterGroup    filter    = new FilterGroup();
        String format = null;

        for (int i = 0; i < args.length; i++) {
            String arg = args[i];
            if (arg.equals("-merge")) {
                doMerge = true;

                continue;
            }

            if (arg.equals("-format")) {
                format = args[++i];
                continue;
            }

            if (arg.equals("-header")) {
                i++;
                System.out.println(args[i]);

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
                cols = new ArrayList<Integer>();
                for (String col :
                        StringUtil.split(args[i], ",", true, true)) {
                    cols.add(new Integer(col));
                }

                continue;
            }
            if (arg.equals("-pattern")) {
                int    col     = new Integer(args[++i]);
                String pattern = args[++i];
                filter.addFilter(new PatternFilter(col, pattern));
                continue;
            }


            if (arg.equals("-lt")) {
                int    col     = new Integer(args[++i]);
                filter.addFilter(new ValueFilter(col, ValueFilter.OP_LT,Double.parseDouble(args[++i])));
                continue;
            }

            if (arg.equals("-gt")) {
                int    col     = new Integer(args[++i]);
                filter.addFilter(new ValueFilter(col, ValueFilter.OP_GT,Double.parseDouble(args[++i])));
                continue;
            }


            if (arg.equals("-defined")) {
                int    col     = new Integer(args[++i]);
                filter.addFilter(new ValueFilter(col, ValueFilter.OP_DEFINED, 0));
                continue;
            }
            files.add(arg);
        }
        if (doMerge) {
            merge(files, System.out);
        } else {
            for (String file : files) {
                processor.reset();
                ProcessInfo info = new ProcessInfo(
                                       new BufferedInputStream(
                                           new FileInputStream(
                                               file)), System.out);
                info.setSkip(1);
                if(format!=null) {
                    info.setFormat(format);
                }
                process(info, filter, new ColumnSelector(cols), processor);
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

        /**
         * _more_
         *
         * @param cols _more_
         *
         * @return _more_
         */
        public abstract List<String> convert(List<String> cols);
    }

    /**
     * Class description
     *
     *
     * @version        $version$, Fri, Jan 9, '15
     * @author         Jeff McWhirter
     */
    public static class ColumnSelector extends Converter {

        /** _more_ */
        List<Integer> indices;

        /**
         * _more_
         *
         * @param indices _more_
         */
        public ColumnSelector(List<Integer> indices) {
            this.indices = indices;
        }

        /**
         * _more_
         *
         * @param cols _more_
         *
         * @return _more_
         */
        public List<String> convert(List<String> cols) {
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
         */
        public void processRow(ProcessInfo info, List<String> toks) {}

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
         */
@Override
        public void processRow(ProcessInfo info, List<String> columns) {
            if (processors.size() == 0) {
                info.getWriter().println(StringUtil.join(",", columns));
                info.getWriter().flush();
            }
            for (Processor processor : processors) {
                processor.processRow(info, columns);
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
            for (int i=0;i<processors.size();i++) {
                Processor processor =  processors.get(i);
                if(i>0)
                    info.getWriter().print(",");
                processor.finish(info);
            }
            info.getWriter().print("\n");
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

        public static final int OP_MIN = 1;

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
         */
@Override
        public void processRow(ProcessInfo info, List<String> toks) {
            boolean first = false;
            if (values == null) {
                values = new ArrayList<Double>();
                counts = new ArrayList<Integer>();
                first = true;
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
                        newValue = first? value:Math.min(value,currentValue);
                    } else if (op == OP_MAX) {
                        newValue = first? value:Math.max(value,currentValue);
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
                info.getWriter().print("-0");
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
        int col;

        /**
         * _more_
         *
         * @param col _more_
         */
        public ColumnFilter(int col) {
            this.col = col;
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

        /**
         * _more_
         */
        public FilterGroup() {}

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
                    //                    System.err.println ("filter not OK");
                    return false;
                }
            }

            //            System.err.println ("OK");
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
            if (col >= toks.size()) {
                return false;
            }
            String v = toks.get(col);
            if (pattern.matcher(v).find()) {
                return true;
            }

            return false;
        }

    }


    public static class ValueFilter extends ColumnFilter {

        
        public static int OP_LT = 0;
        public static int OP_LE = 1;
        public static int OP_GT = 2;
        public static int OP_GE = 3;
        public static int OP_EQUALS = 4;
        public static int OP_DEFINED = 5;

        private int op;

        private double value;

        /**
         * _more_
         *
         * @param col _more_
         * @param pattern _more_
         */
        public ValueFilter(int col, int op, double value) {
            super(col);
            this.op = op;
            this.value = value;
        }

        public static int getOperator(String s) {
            if(s.equals("<")) return OP_LT;
            if(s.equals("<=")) return OP_LE;
            if(s.equals(">")) return OP_GT;
            if(s.equals(">=")) return OP_GE;
            if(s.equals("=")) return OP_EQUALS;
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
            if (col >= toks.size()) {
                return false;
            }
            try {
                String v = toks.get(col);
                double value = Double.parseDouble(v);
                if(op == OP_LT)
                    return value < this.value;
                if(op == OP_LE)
                    return value <= this.value;
                if(op == OP_GT)
                    return value > this.value;
                if(op == OP_GE)
                    return value >= this.value;
                if(op == OP_EQUALS)
                    return value == this.value;
                if(op == OP_DEFINED)
                    return value == value;
                return false;
            } catch(Exception exc) {
            }
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

        private int maxRows = -1;

        private DecimalFormat format;

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


        public String formatValue(double value) {
            if(format!=null)  return format.format(value);
            return ""+value;
        }


        /**
Set the Format property.

@param value The new value for Format
**/
public void setFormat (String value) {
    if(value == null)
        format  =null;
    else
        format  = new DecimalFormat(value);
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
Set the MaxRows property.

@param value The new value for MaxRows
**/
public void setMaxRows (int value) {
	maxRows = value;
}

/**
Get the MaxRows property.

@return The MaxRows
**/
public int getMaxRows () {
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
