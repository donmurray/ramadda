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

public abstract class Processor {

    /**
     * _more_
     *
     *
     * @param info _more_
     * @param row _more_
     * @param line _more_
     *
     * @return _more_
     */
    public boolean processRow(Visitor info, Row row, String line) {
        return true;
    }

    /**
     * _more_
     *
     * @param info _more_
     *
     * @throws Exception On badness
     */
    public void finish(Visitor info) throws Exception {}

    /**
     * _more_
     */
    public void reset() {}




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
         * @param row _more_
         * @param line _more_
         *
         * @return _more_
         */
        @Override
        public boolean processRow(Visitor info, Row row, String line) {
            if (processors.size() == 0) {
                if (info.getRow() == 0) {
                    //not now
                    for (String header : info.getHeaderLines()) {
                        //                        info.getWriter().println(header);
                    }
                }
                info.getWriter().println(
                    CsvUtil.columnsToString(
                        row.getValues(), info.getOutputDelimiter()));
                info.getWriter().flush();
            }
            for (Processor processor : processors) {
                if ( !processor.processRow(info, row, line)) {
                    return false;
                }
            }

            return true;
        }

        /**
         * _more_
         *
         * @param info _more_
         *
         * @throws Exception On badness
         */
        @Override
        public void finish(Visitor info) throws Exception {
            for (int i = 0; i < processors.size(); i++) {
                Processor processor = processors.get(i);
                if (i > 0) {
                    info.print(",");
                }
                processor.finish(info);
            }
            if (processors.size() > 0) {
                info.print("\n");
            }
            info.flush();
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
         * @param row _more_
         * @param line _more_
         *
         * @return _more_
         */
        @Override
        public boolean processRow(Visitor info, Row row, String line) {
            boolean first = false;
            if (values == null) {
                values = new ArrayList<Double>();
                counts = new ArrayList<Integer>();
                first  = true;
            }
            for (int i = 0; i < row.size(); i++) {
                if (i >= values.size()) {
                    values.add(new Double(0));
                    counts.add(new Integer(0));
                }
                try {
                    String s            = row.getString(i).trim();
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

            return true;
        }

        /**
         * _more_
         *
         * @param info _more_
         *
         * @throws Exception On badness
         */
        @Override
        public void finish(Visitor info) throws Exception {
            if (values == null) {
                Converter.ColumnSelector selector = info.getSelector();
                if ((selector != null)
                        && (selector.getIndices(info) != null)) {
                    for (int i = 0; i < selector.getIndices(info).size();
                            i++) {
                        if (i > 0) {
                            info.getWriter().print(",");
                        }
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
    public static class RowCollector extends Processor {

        /** _more_ */
        private List<Row> rows = new ArrayList<Row>();


        /**
         * _more_
         *
         */
        public RowCollector() {}

        /**
         * _more_
         */
        @Override
        public void reset() {
            rows = new ArrayList<Row>();
        }

        /**
         * _more_
         *
         *
         * @param info _more_
         * @param row _more_
         * @param line _more_
         *
         * @return _more_
         */
        @Override
        public boolean processRow(Visitor info, Row row, String line) {
            rows.add(row);

            return true;
        }

        /**
         *  Set the Rows property.
         *
         *  @param value The new value for Rows
         */
        public void setRows(List<Row> value) {
            rows = value;
        }

        /**
         *  Get the Rows property.
         *
         *  @return The Rows
         */
        public List<Row> getRows() {
            return rows;
        }


    }




    /**
     * Class description
     *
     *
     * @version        $version$, Fri, Jan 9, '15
     * @author         Jeff McWhirter
     */
    public static class Rotator extends RowCollector {

        /**
         * _more_
         *
         */
        public Rotator() {}

        /**
         * _more_
         *
         * @param info _more_
         *
         * @throws Exception On badness
         */
        @Override
        public void finish(Visitor info) throws Exception {
            int columnIndex = 0;
            while (true) {
                boolean stillHaveData = false;
                int     rowIndex      = 0;
                for (Row row : getRows()) {
                    List values = row.getValues();
                    if (columnIndex < values.size()) {
                        //info.getWriter().print(",");
                        if (rowIndex > 0) {
                            info.getWriter().print(",");
                        }
                        info.getWriter().print(values.get(columnIndex));
                        stillHaveData = true;
                    }
                    rowIndex++;
                }
                info.getWriter().print("\n");
                columnIndex++;
                if ( !stillHaveData) {
                    break;
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
    public static class Flipper extends RowCollector {

        /**
         * ctor
         */
        public Flipper() {}

        /**
         * _more_
         *
         * @param info _more_
         *
         * @throws Exception On badness
         */
        @Override
        public void finish(Visitor info) throws Exception {
            List<Row> rows = getRows();
            for (int i = rows.size() - 1; i >= 0; i--) {
                Row row = rows.get(i);
                info.getWriter().println(
                    CsvUtil.columnsToString(row.getValues(), ","));
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
         * @param row _more_
         * @param line _more_
         *
         * @return _more_
         */
        @Override
        public boolean processRow(Visitor info, Row row, String line) {
            boolean first = false;
            if (contains == null) {
                contains = new ArrayList<HashSet>();
                values   = new ArrayList<List>();
            }
            for (int i = 0; i < row.size(); i++) {
                if (i >= values.size()) {
                    contains.add(new HashSet());
                    values.add(new ArrayList());
                }
                String s = row.getString(i).trim();
                if (contains.get(i).contains(s)) {
                    continue;
                }
                contains.get(i).add(s);
                values.get(i).add(s);
            }

            return true;
        }

        /**
         *   _more_
         *
         *   @param info _more_
         *
         *   @throws Exception On badness
         */
        @Override
        public void finish(Visitor info) throws Exception {
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

        /** _more_ */
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
         * @param row _more_
         * @param line _more_
         *
         * @return _more_
         */
        @Override
        public boolean processRow(Visitor info, Row row, String line) {
            uniqueCounts.add(row.size());
            count++;

            return true;
        }

        /**
         *   _more_
         *
         *   @param info _more_
         *
         *   @throws Exception On badness
         */
        @Override
        public void finish(Visitor info) throws Exception {
            info.getWriter().print(count);
            //            System.err.println(uniqueCounts);
        }

    }





}
