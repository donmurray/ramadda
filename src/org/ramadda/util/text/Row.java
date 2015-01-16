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

public class Row {

    /** _more_ */
    private List values;

    /**
     * _more_
     *
     * @param values _more_
     */
    public Row(List values) {}


    /**
     * _more_
     *
     * @param line _more_
     * @param delimiter _more_
     */
    public Row(String line, String delimiter) {
        this(Utils.tokenizeColumns(line, delimiter));
    }


    /**
     * Set the Values property.
     *
     * @param value The new value for Values
     */
    public void setValues(List value) {
        values = value;
    }

    /**
     * Get the Values property.
     *
     * @return The Values
     */
    public List getValues() {
        return values;
    }

    /**
     * _more_
     *
     * @param index _more_
     *
     * @return _more_
     */
    public Object get(int index) {
        return values.get(index);
    }

    /**
     * _more_
     *
     * @param index _more_
     *
     * @return _more_
     */
    public String getString(int index) {
        return values.get(index).toString();
    }

    /**
     * _more_
     *
     * @param index _more_
     * @param object _more_
     */
    public void set(int index, Object object) {
        values.set(index, object);
    }

    /**
     * _more_
     *
     * @param object _more_
     */
    public void add(Object object) {
        values.add(object);
    }

    /**
     * _more_
     *
     * @param index _more_
     * @param object _more_
     */
    public void add(int index, Object object) {
        values.add(index, object);
    }

    /**
     * _more_
     *
     * @param index _more_
     */
    public void remove(int index) {
        values.remove(index);
    }


    /**
     * _more_
     *
     * @return _more_
     */
    public int size() {
        return values.size();
    }




}
