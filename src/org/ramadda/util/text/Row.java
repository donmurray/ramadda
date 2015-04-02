/**
* Copyright (c) 2008-2015 Geode Systems LLC
* This Software is licensed under the Geode Systems RAMADDA License available in the source distribution in the file 
* ramadda_license.txt. The above copyright notice shall be included in all copies or substantial portions of the Software.
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
    public Row(List values) {
        this.values = values;
    }


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
