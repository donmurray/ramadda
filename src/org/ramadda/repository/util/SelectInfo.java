/**
* Copyright (c) 2008-2015 Geode Systems LLC
* This Software is licensed under the Geode Systems RAMADDA License available in the source distribution in the file 
* ramadda_license.txt. The above copyright notice shall be included in all copies or substantial portions of the Software.
*/

package org.ramadda.repository.util;


import org.ramadda.sql.Clause;
import org.ramadda.sql.SqlUtil;




import java.util.ArrayList;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.List;


/**
 * Holds information for generating entries in entries.xml
 */
public class SelectInfo {

    /** _more_ */
    private List<Clause> where;

    /** _more_ */
    private int max = -1;

    /**
     * _more_
     */
    public SelectInfo() {}


    /**
     * _more_
     *
     * @param where _more_
     */
    public SelectInfo(List<Clause> where) {
        this(where, -1);
    }

    /**
     * _more_
     *
     * @param where _more_
     * @param max _more_
     */
    public SelectInfo(List<Clause> where, int max) {
        this.where = where;
        this.max   = max;
    }

    /**
     * _more_
     *
     * @return _more_
     */
    public List<Clause> getWhere() {
        return where;
    }

    /**
     * _more_
     *
     * @return _more_
     */
    public int getMaxCount() {
        return max;
    }

    /**
     * _more_
     *
     * @return _more_
     */
    public String toString() {
        return " max:" + max;
    }
}
