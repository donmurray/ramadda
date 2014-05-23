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


import java.util.ArrayList;
import java.util.Hashtable;
import java.util.List;



/**
 * A collection of utilities for rss feeds xml.
 *
 * @author Jeff McWhirter
 *
 * @param <T>
 */

public class CategoryList<T> {

    /** _more_ */
    List<String> categories = new ArrayList<String>();

    /** _more_ */
    Hashtable<String, List<T>> map = new Hashtable<String, List<T>>();

    /**
     * _more_
     */
    public CategoryList() {}

    /**
     * _more_
     *
     * @param category _more_
     *
     * @return _more_
     */
    public List<T> get(String category) {
        if (category == null) {
            category = "";
        }
        List<T> sb = map.get(category);
        if (sb == null) {
            sb = new ArrayList<T>();
            map.put(category, sb);
            categories.add(category);
        }

        return sb;
    }

    /**
     * _more_
     *
     * @param category _more_
     * @param object _more_
     */
    public void add(String category, T object) {
        get(category).add(object);
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
