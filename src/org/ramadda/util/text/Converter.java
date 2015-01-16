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
 * Class description
 *
 *
 * @version        $version$, Fri, Jan 9, '15
 * @author         Jeff McWhirter
 */
public abstract class Converter {

    /** _more_ */
    private int index = -1;

    /** _more_ */
    private String scol;

    /**
     * _more_
     */
    public Converter() {}

    /**
     * _more_
     *
     * @param col _more_
     */
    public Converter(String col) {
        scol = col;
    }

    /**
     * _more_
     *
     *
     * @param info _more_
     * @param cols _more_
     *
     * @return _more_
     */
    public abstract List<String> convert(ProcessInfo info, List<String> cols);

    /**
     * _more_
     *
     * @param info _more_
     *
     * @return _more_
     */
    public int getIndex(ProcessInfo info) {
        if (index < 0) {
            index = info.getColumnIndex(scol);
        }

        return index;
    }





    /**
     * Class description
     *
     *
     * @version        $version$, Fri, Jan 16, '15
     * @author         Enter your name here...
     */
    public static class ConverterGroup extends Converter {

        /** _more_ */
        private List<Converter> converters = new ArrayList<Converter>();

        /**
         * _more_
         */
        public ConverterGroup() {}

        /**
         * _more_
         *
         * @param converter _more_
         */
        public void addConverter(Converter converter) {
            converters.add(converter);
        }

        /**
         * _more_
         *
         *
         * @param info _more_
         * @param cols _more_
         *
         * @return _more_
         */
        public List<String> convert(ProcessInfo info, List<String> cols) {
            for (Converter child : converters) {
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

        /** _more_ */
        List<String> sindices;

        /** _more_ */
        List<Integer> indices;

        /**
         * _more_
         *
         * @param cols _more_
         */
        public ColumnSelector(List<String> cols) {
            this.sindices = cols;
        }

        /**
         * _more_
         *
         * @param info _more_
         *
         * @return _more_
         */
        public List<Integer> getIndices(ProcessInfo info) {
            if (indices == null) {
                indices = new ArrayList<Integer>();
                for (String s : sindices) {
                    int idx = info.getColumnIndex(s);
                    if (idx >= 0) {
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
         *
         * @param info _more_
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


    /**
     * Class description
     *
     *
     * @version        $version$, Fri, Jan 16, '15
     * @author         Enter your name here...
     */
    public static class ColumnChanger extends Converter {

        /** _more_ */
        private String pattern;

        /** _more_ */
        private String value;

        /**
         * _more_
         *
         *
         * @param col _more_
         * @param pattern _more_
         * @param value _more_
         */
        public ColumnChanger(String col, String pattern, String value) {
            super(col);
            this.pattern = pattern;
            this.value   = value;
        }

        /**
         * _more_
         *
         *
         * @param info _more_
         * @param cols _more_
         *
         * @return _more_
         */
        public List<String> convert(ProcessInfo info, List<String> cols) {
            int index = getIndex(info);
            if ((index < 0) || (index >= cols.size())) {
                return cols;
            }
            String s = cols.get(index);
            s = s.replaceAll(pattern, value);

            cols.set(index, s);

            return cols;
        }

    }




    /**
     * Class description
     *
     *
     * @version        $version$, Fri, Jan 16, '15
     * @author         Enter your name here...
     */
    public static class ColumnSplitter extends Converter {


        /** _more_ */
        private String delimiter;


        /**
         * _more_
         *
         *
         * @param col _more_
         * @param delimiter _more_
         */
        public ColumnSplitter(String col, String delimiter) {
            super(col);
            this.delimiter = delimiter;
        }

        /**
         * _more_
         *
         *
         * @param info _more_
         * @param cols _more_
         *
         * @return _more_
         */
        public List<String> convert(ProcessInfo info, List<String> cols) {
            int index = getIndex(info);
            if ((index < 0) || (index >= cols.size())) {
                return cols;
            }
            cols.remove(index);
            int colOffset = 0;
            for (String tok : StringUtil.split(cols.get(index), delimiter)) {
                cols.add(index + (colOffset++), tok);
            }

            return cols;
        }

    }



    /**
     * Class description
     *
     *
     * @version        $version$, Fri, Jan 16, '15
     * @author         Enter your name here...
     */
    public static class ColumnDeleter extends Converter {

        /**
         * _more_
         *
         * @param col _more_
         */
        public ColumnDeleter(String col) {
            super(col);
        }

        /**
         * _more_
         *
         *
         * @param info _more_
         * @param cols _more_
         *
         * @return _more_
         */
        public List<String> convert(ProcessInfo info, List<String> cols) {
            int index = getIndex(info);
            if ((index < 0) || (index >= cols.size())) {
                return cols;
            }
            cols.remove(index);

            return cols;
        }

    }


    /**
     * Class description
     *
     *
     * @version        $version$, Fri, Jan 16, '15
     * @author         Enter your name here...
     */
    public static class ColumnAdder extends Converter {

        /** _more_ */
        private String value;


        /**
         * _more_
         *
         * @param col _more_
         * @param value _more_
         */
        public ColumnAdder(String col, String value) {
            super(col);
            this.value = value;
        }

        /**
         * _more_
         *
         *
         * @param info _more_
         * @param cols _more_
         *
         * @return _more_
         */
        public List<String> convert(ProcessInfo info, List<String> cols) {
            int index = getIndex(info);
            if ((index < 0) || (index >= cols.size())) {
                return cols;
            }
            cols.add(index, value);

            return cols;
        }

    }

}
