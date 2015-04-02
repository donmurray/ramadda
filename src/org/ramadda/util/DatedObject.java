/**
* Copyright (c) 2008-2015 Geode Systems LLC
* This Software is licensed under the Geode Systems RAMADDA License available in the source distribution in the file 
* ramadda_license.txt. The above copyright notice shall be included in all copies or substantial portions of the Software.
*/

package org.ramadda.util;


import java.util.ArrayList;
import java.util.Date;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.List;



/**
 * Provides a wrapper around an object and keeps track of a date.
 *
 * @param <ObjectType>
 */
public class DatedObject<ObjectType> {

    /** _more_ */
    private Date date;

    /** _more_ */
    private ObjectType object;


    /**
     * ctor
     *
     * @param date _more_
     * @param object _more_
     */
    public DatedObject(Date date, ObjectType object) {
        this.date   = date;
        this.object = object;
    }

    /**
     * Set the Date property.
     *
     * @param value The new value for Date
     */
    public void setDate(Date value) {
        date = value;
    }

    /**
     * Get the Date property.
     *
     * @return The Date
     */
    public Date getDate() {
        return date;
    }

    /**
     * Set the Object property.
     *
     * @param value The new value for Object
     */
    public void setObject(ObjectType value) {
        object = value;
    }

    /**
     * Get the Object property.
     *
     * @return The Object
     */
    public ObjectType getObject() {
        return object;
    }



}
