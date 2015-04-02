/**
* Copyright (c) 2008-2015 Geode Systems LLC
* This Software is licensed under the Geode Systems RAMADDA License available in the source distribution in the file 
* ramadda_license.txt. The above copyright notice shall be included in all copies or substantial portions of the Software.
*/

/**
 * (C) 1999-2002  WTS Systems, L.L.C.
 *   All rights reserved
 */



package com.infocetera.util;


/**
 * Class ObjectPair _more_
 *
 *
 * @author IDV Development Team
 * @version $Revision: 1.3 $
 */
public class ObjectPair {

    /** _more_ */
    public Object o1;

    /** _more_ */
    public Object o2;


    /**
     * _more_
     *
     * @param o1 _more_
     * @param o2 _more_
     */
    public ObjectPair(Object o1, Object o2) {
        this.o1 = o1;
        this.o2 = o2;
    }

    /**
     *  For now just show the first object
     *
     * @return _more_
     */
    public String toString() {
        return o1.toString();
    }

    /**
     * _more_
     *
     * @param o _more_
     *
     * @return _more_
     */
    public boolean equals(Object o) {
        if ( !(o instanceof ObjectPair)) {
            return false;
        }
        ObjectPair other = (ObjectPair) o;

        return o1.equals(other.o1) && o2.equals(other.o2);
    }


}
