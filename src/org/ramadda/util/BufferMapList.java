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

package org.ramadda.util;


import java.util.ArrayList;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.List;


/**
 *
 * @param <T>
 */
public class BufferMapList<T> extends MapList<T, Appendable> {


    /**
     * _more_
     *
     * @param key _more_
     *
     * @return _more_
     */
    public Appendable get(T key) {
        Appendable sb = super.get(key);
        if (sb == null) {
            sb = new StringBuilder();
            super.put(key, sb);
        }

        return sb;
    }

}
