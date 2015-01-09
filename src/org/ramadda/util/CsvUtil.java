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

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import java.util.regex.*;


/**
 *
 * @author Jeff McWhirter
 */

public class CsvUtil {


    /** _more_          */
    static String theLine;

    /**
     * _more_
     *
     * @param files _more_
     * @param out _more_
     *
     * @throws Exception _more_
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
     * @param input _more_
     * @param out _more_
     * @param filter _more_
     * @param converter _more_
     * @param processor _more_
     * @param skip _more_
     *
     * @throws Exception _more_
     */
    public static void process(InputStream input, OutputStream out,
                               Filter filter, Converter converter,
                               Processor processor, int skip)
            throws Exception {

        input = new BufferedInputStream(input);

        String delimiter = null;
        //        BufferedReader br =      new BufferedReader(new InputStreamReader(input));
        PrintWriter   writer = new PrintWriter(out);
        String        line;
        int           rowIdx = 0;

        StringBuilder lb     = new StringBuilder();
        int           c;

        boolean       inQuote = false;
        while ((c = input.read()) != -1) {
            //(line = br.readLine()) != null) {
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
            if (rowIdx <= skip) {
                if (processor == null) {
                    //                    writer.println(line);
                }

                continue;
            }

            if (line.startsWith("#")) {
                //                writer.println(line);
                continue;
            }

            if (delimiter == null) {
                delimiter = ",";
                int i1 = line.indexOf(",");
                int i2 = line.indexOf("|");
                if ((i2 >= 0) && ((i1 < 0) || (i2 < i1))) {
                    delimiter = "|";
                }
            }


            List<String> cols = Utils.tokenizeColumns(line, delimiter);

            if ((filter != null) && !filter.ok(cols)) {
                continue;
            }


            if (converter != null) {
                cols = converter.convert(cols);
            }

            if (processor != null) {
                processor.process(cols);
            } else {
                writer.println(StringUtil.join(",", cols));
                writer.flush();
            }
        }

        if (processor != null) {
            processor.finish(writer);
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
        boolean       doMerge = false;


        List<String>  files   = new ArrayList<String>();
        List<Integer> cols    = null;
        Summer        summer  = null;
        FilterGroup   filter  = new FilterGroup();

        for (int i = 0; i < args.length; i++) {
            String arg = args[i];
            if (arg.equals("-merge")) {
                doMerge = true;

                continue;
            }



            if (arg.equals("-sum")) {
                summer = new Summer();

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
            files.add(arg);
        }
        if (doMerge) {
            merge(files, System.out);
        } else {
            for (String file : files) {
                if (summer != null) {
                    summer.reset();
                }
                process(new FileInputStream(file), System.out, filter,
                        new ColumnSelector(cols), summer, 1);
            }
        }
    }


    /**
     * Class description
     *
     *
     * @version        $version$, Fri, Jan 9, '15
     * @author         Enter your name here...    
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
     * @author         Enter your name here...    
     */
    public static class ColumnSelector extends Converter {

        /** _more_          */
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
     * @author         Enter your name here...    
     */
    public static abstract class Processor {

        /**
         * _more_
         *
         * @param toks _more_
         */
        public abstract void process(List<String> toks);

        /**
         * _more_
         *
         * @param writer _more_
         *
         * @throws Exception _more_
         */
        public abstract void finish(PrintWriter writer) throws Exception;

        /**
         * _more_
         */
        public void reset() {}
    }

    /**
     * Class description
     *
     *
     * @version        $version$, Fri, Jan 9, '15
     * @author         Enter your name here...    
     */
    public static class Summer extends Processor {

        /** _more_          */
        List<Double> values;

        /**
         * _more_
         */
        public Summer() {}

        /**
         * _more_
         */
        public void reset() {
            values = null;
        }

        /**
         * _more_
         *
         * @param toks _more_
         */
        public void process(List<String> toks) {
            if (values == null) {
                values = new ArrayList<Double>();
            }
            for (int i = 0; i < toks.size(); i++) {
                if (i >= values.size()) {
                    values.add(new Double(0));
                }
                try {
                    String s     = toks.get(i).trim();
                    double value = (s.length() == 0)
                                   ? 0
                                   : new Double(s).doubleValue();
                    double sum   = values.get(i).doubleValue() + value;
                    values.set(i, sum);
                } catch (Exception exc) {
                    //                    System.err.println("err:" + exc);
                    //                    System.err.println("line:" + theLine);
                }
            }
        }

        /**
         * _more_
         *
         * @param writer _more_
         *
         * @throws Exception _more_
         */
        public void finish(PrintWriter writer) throws Exception {
            if (values == null) {
                writer.println("no values");
            } else {
                writer.println(StringUtil.join(",", values));
            }
            writer.flush();
        }

    }



    /**
     * Class description
     *
     *
     * @version        $version$, Fri, Jan 9, '15
     * @author         Enter your name here...    
     */
    public abstract static class Filter {

        /**
         * _more_
         */
        public Filter() {}

        /**
         * _more_
         *
         * @param toks _more_
         *
         * @return _more_
         */
        public abstract boolean ok(List<String> toks);

    }

    /**
     * Class description
     *
     *
     * @version        $version$, Fri, Jan 9, '15
     * @author         Enter your name here...    
     */
    public abstract static class ColumnFilter extends Filter {

        /** _more_          */
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
     * @author         Enter your name here...    
     */
    public static class FilterGroup extends Filter {

        /** _more_          */
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
         * @param toks _more_
         *
         * @return _more_
         */
        public boolean ok(List<String> toks) {
            if (filters.size() == 0) {
                return true;
            }
            for (Filter filter : filters) {
                if ( !filter.ok(toks)) {
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
     * @author         Enter your name here...    
     */
    public static class PatternFilter extends ColumnFilter {

        /** _more_          */
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
         * @param toks _more_
         *
         * @return _more_
         */
        public boolean ok(List<String> toks) {
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


}
