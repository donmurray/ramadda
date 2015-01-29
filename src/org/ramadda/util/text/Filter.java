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

public class Filter extends Converter {

    /** _more_ */
    private String commentPrefix = "#";


    /**
     * _more_
     */
    public Filter() {}

    /**
     * _more_
     *
     * @param info _more_
     * @param row _more_
     *
     * @return _more_
     */
    @Override
    public Row convert(Visitor info, Row row) {
        if (rowOk(info, row)) {
            return row;
        } else {
            return null;
        }
    }

    /**
     * _more_
     *
     *
     * @param info _more_
     * @param row _more_
     *
     * @return _more_
     */
    public boolean rowOk(Visitor info, Row row) {
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
    public boolean lineOk(Visitor info, String line) {
        if ((commentPrefix != null) && line.startsWith(commentPrefix)) {
            return false;
        }

        return true;
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

        /** _more_ */
        private String scol;

        /** _more_ */
        private boolean negate = false;

        /**
         * _more_
         *
         * @param col _more_
         */
        public ColumnFilter(int col) {
            this.col = col;
        }


        /**
         * _more_
         *
         * @param scol _more_
         */
        public ColumnFilter(String scol) {
            this.scol = scol;
        }

        /**
         * _more_
         *
         * @param scol _more_
         * @param negate _more_
         */
        public ColumnFilter(String scol, boolean negate) {
            this(scol);
            this.negate = negate;
        }

        /**
         * _more_
         *
         * @param b _more_
         *
         * @return _more_
         */
        public boolean doNegate(boolean b) {
            if (negate) {
                return !b;
            }

            return b;
        }

        /**
         * _more_
         *
         * @param info _more_
         *
         * @return _more_
         */
        public int getIndex(Visitor info) {
            if (col >= 0) {
                return col;
            }
            if (scol == null) {
                return -11;
            }
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

        /** _more_ */
        private boolean andLogic = true;


        /**
         * _more_
         */
        public FilterGroup() {}

        /**
         * _more_
         *
         * @param andLogic _more_
         */
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
         * @param row _more_
         *
         * @return _more_
         */
        @Override
        public boolean rowOk(Visitor info, Row row) {
            if (filters.size() == 0) {
                return true;
            }
            for (Filter filter : filters) {
                if ( !filter.rowOk(info, row)) {
                    if (andLogic) {
                        return false;
                    }
                } else {
                    if ( !andLogic) {
                        return true;
                    }
                }
            }

            if ( !andLogic) {
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
         * @param col _more_
         * @param pattern _more_
         * @param negate _more_
         */
        public PatternFilter(String col, String pattern, boolean negate) {
            super(col, negate);
            setPattern(pattern);
        }


        /**
         * _more_
         *
         * @param col _more_
         * @param pattern _more_
         */
        public PatternFilter(String col, String pattern) {
            super(col);
            setPattern(pattern);
        }

        /**
         * _more_
         *
         * @param pattern _more_
         */
        public void setPattern(String pattern) {
            this.pattern = Pattern.compile(pattern);
        }


        /**
         * _more_
         *
         *
         * @param info _more_
         * @param row _more_
         *
         * @return _more_
         */
        public boolean rowOk(Visitor info, Row row) {
            int idx = getIndex(info);
            if (idx >= row.size()) {
                return doNegate(false);
            }
            if (idx < 0) {
                for (int i = 0; i < row.size(); i++) {
                    String v = row.getString(i);
                    if (pattern.matcher(v).find()) {
                        return doNegate(true);
                    }
                }

                return doNegate(false);
            }

            String v = row.getString(idx);
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
         * @param op _more_
         * @param value _more_
         */
        public ValueFilter(int col, int op, double value) {
            super(col);
            this.op    = op;
            this.value = value;
        }



        /**
         * _more_
         *
         * @param col _more_
         * @param op _more_
         * @param value _more_
         */
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
            s = s.trim();
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
         * @param row _more_
         *
         * @return _more_
         */
        public boolean rowOk(Visitor info, Row row) {
            int idx = getIndex(info);
            if (idx >= row.size()) {
                return false;
            }
            try {
                String v     = row.getString(idx);
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
     * @version        $version$, Fri, Jan 9, '15
     * @author         Jeff McWhirter
     */
    public static class Cutter extends Filter {

        /** _more_ */
        private int currentRow = -1;

        /** _more_ */
        private int start = -1;

        /** _more_ */
        private int end = -1;


        /**
         * _more_
         *
         * @param op _more_
         *
         * @param start _more_
         * @param end _more_
         */
        public Cutter(int start, int end) {
            this.start = start;
            this.end   = end;
        }

        /**
         * _more_
         *
         * @param info _more_
         * @param row _more_
         *
         * @return _more_
         */
        @Override
        public boolean rowOk(Visitor info, Row row) {
            currentRow++;
            if ((start >= 0) && (currentRow < start)) {
                return false;
            }
            if ((end >= 0) && (currentRow > end)) {
                return false;
            }

            return true;
        }



    }




}
