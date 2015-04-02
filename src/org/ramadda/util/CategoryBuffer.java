/**
* Copyright (c) 2008-2015 Geode Systems LLC
* This Software is licensed under the Geode Systems RAMADDA License available in the source distribution in the file 
* ramadda_license.txt. The above copyright notice shall be included in all copies or substantial portions of the Software.
*/

package org.ramadda.util;


import java.util.ArrayList;
import java.util.Hashtable;
import java.util.List;



/**
 * A collection of utilities for rss feeds xml.
 *
 * @author Jeff McWhirter
 */

public class CategoryBuffer {

    /** _more_ */
    List<String> categories = new ArrayList<String>();

    /** _more_ */
    Hashtable<String, StringBuffer> buffers = new Hashtable<String,
                                                  StringBuffer>();

    /**
     * _more_
     */
    public CategoryBuffer() {}

    /**
     * _more_
     *
     * @param category _more_
     *
     * @return _more_
     */
    public StringBuffer get(String category) {
        return get(category, false);
    }


    /**
     * _more_
     *
     * @param category _more_
     * @param addToFront _more_
     *
     * @return _more_
     */
    public StringBuffer get(String category, boolean addToFront) {
        if (category == null) {
            category = "";
        }
        StringBuffer sb = buffers.get(category);
        if (sb == null) {
            sb = new StringBuffer();
            buffers.put(category, sb);
            if (addToFront) {
                categories.add(0, category);
            } else {
                categories.add(category);
            }
        }

        return sb;
    }

    /**
     * _more_
     *
     * @param category _more_
     */
    public void moveToFront(String category) {
        categories.remove(category);
        categories.add(0, category);
    }


    /**
     * _more_
     *
     * @param category _more_
     * @param object _more_
     */
    public void append(String category, Object object) {
        get(category).append(object);
    }

    /**
     * _more_
     *
     * @param category _more_
     *
     * @return _more_
     */
    public boolean contains(String category) {
        return buffers.get(category) != null;
    }

    /**
     * _more_
     *
     * @return _more_
     */
    public List<String> getCategories() {
        return categories;
    }


}
