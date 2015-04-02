/**
* Copyright (c) 2008-2015 Geode Systems LLC
* This Software is licensed under the Geode Systems RAMADDA License available in the source distribution in the file 
* ramadda_license.txt. The above copyright notice shall be included in all copies or substantial portions of the Software.
*/

/**
 * Copyright (c) 2008-2015 Geode Systems LLC
 * This Software is licensed under the Geode Systems RAMADDA License available in the source distribution in the file
 * ramadda_license.txt. The above copyright notice shall be included in all copies or substantial portions of the Software.
 */

package org.ramadda.data.record.filter;


import org.ramadda.data.record.*;

import java.io.*;

import java.util.ArrayList;
import java.util.List;


/**
 * Class description
 *
 *
 * @version        Enter version here..., Fri, May 21, '10
 * @author         Enter your name here...
 */
public class CollectionRecordFilter implements RecordFilter {

    /** _more_ */
    public static final int OP_AND = 0;

    /** _more_ */
    public static final int OP_OR = 1;

    /** _more_ */
    private int operator = OP_AND;

    /** _more_ */
    private List<RecordFilter> filters = new ArrayList<RecordFilter>();

    /**
     * _more_
     *
     * @param operator _more_
     */
    public CollectionRecordFilter(int operator) {
        this.operator = operator;
    }

    /**
     * _more_
     *
     * @param filters _more_
     */
    public CollectionRecordFilter(List<RecordFilter> filters) {
        this(OP_AND, filters);
    }

    /**
     * _more_
     *
     * @param operator _more_
     * @param filters _more_
     */
    public CollectionRecordFilter(int operator, List<RecordFilter> filters) {
        this(operator);
        for (RecordFilter filter : filters) {
            addFilter(filter);
        }
    }

    /**
     * _more_
     *
     * @param operator _more_
     * @param filters _more_
     */
    public CollectionRecordFilter(int operator, RecordFilter[] filters) {
        this(operator);
        for (RecordFilter filter : filters) {
            addFilter(filter);
        }
    }


    /**
     * _more_
     *
     * @param filters _more_
     *
     * @return _more_
     */
    public static RecordFilter and(RecordFilter[] filters) {
        return new CollectionRecordFilter(OP_AND, filters);
    }

    /**
     * _more_
     *
     * @param filters _more_
     *
     * @return _more_
     */
    public static RecordFilter or(RecordFilter[] filters) {
        return new CollectionRecordFilter(OP_OR, filters);
    }

    /**
     * _more_
     *
     * @param filter _more_
     */
    public void addFilter(RecordFilter filter) {
        filters.add(filter);
    }

    /**
     * _more_
     *
     * @param record _more_
     * @param visitInfo _more_
     *
     * @return _more_
     */
    public boolean isRecordOk(Record record, VisitInfo visitInfo) {
        boolean anyTrue = false;
        for (RecordFilter filter : filters) {
            boolean v = filter.isRecordOk(record, visitInfo);
            if (v) {
                anyTrue = true;
            } else {
                if (operator == OP_AND) {
                    return false;
                }
            }
        }

        return anyTrue;
    }




}
