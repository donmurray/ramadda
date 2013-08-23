/*
* Copyright 2008-2013 Geode Systems LLC
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

package org.ramadda.plugins.poll;


import ucar.unidata.xml.XmlEncoder;


import java.util.Date;
import java.util.Hashtable;
import java.util.UUID;


/**
 *
 *
 */
public class PollResponse {

    /** _more_ */
    private String id;

    /** _more_ */
    private String what;

    /** _more_ */
    private Date date;

    /** _more_ */
    private String comment = "";

    /** _more_ */
    private Hashtable<String, String> selected = new Hashtable<String,
                                                     String>();


    /**
     * _more_
     */
    public PollResponse() {}

    /**
     * _more_
     *
     * @param what _more_
     * @param comment _more_
     */
    public PollResponse(String what, String comment) {
        this.id      = UUID.randomUUID().toString();
        this.what    = what;
        this.comment = comment;
        this.date    = new Date();
    }

    /**
     * _more_
     *
     *
     * @param key _more_
     * @param value _more_
     */
    public void set(String key, String value) {
        selected.put(key, value);
    }

    /**
     * _more_
     *
     * @param key _more_
     *
     * @return _more_
     */
    public String get(String key) {
        return selected.get(key);
    }


    /**
     * _more_
     *
     * @param value _more_
     *
     * @return _more_
     */
    public boolean isSelected(String value) {
        return selected.get(value) != null;
    }

    /**
     *  Set the Comment property.
     *
     *  @param value The new value for Comment
     */
    public void setComment(String value) {
        comment = value;
    }

    /**
     *  Get the Comment property.
     *
     *  @return The Comment
     */
    public String getComment() {
        return comment;
    }


    /**
     *  Set the Id property.
     *
     *  @param value The new value for Id
     */
    public void setId(String value) {
        id = value;
    }

    /**
     *  Get the Id property.
     *
     *  @return The Id
     */
    public String getId() {
        return id;
    }


    /**
     *  Set the What property.
     *
     *  @param value The new value for What
     */
    public void setWhat(String value) {
        what = value;
    }

    /**
     *  Get the What property.
     *
     *  @return The What
     */
    public String getWhat() {
        return what;
    }

    /**
     *  Set the Selected property.
     *
     *  @param value The new value for Selected
     */
    public void setSelected(Hashtable<String, String> value) {
        selected = value;
    }

    /**
     *  Get the Selected property.
     *
     *  @return The Selected
     */
    public Hashtable<String, String> getSelected() {
        return selected;
    }

    /**
     *  Set the Date property.
     *
     *  @param value The new value for Date
     */
    public void setDate(Date value) {
        date = value;
    }

    /**
     *  Get the Date property.
     *
     *  @return The Date
     */
    public Date getDate() {
        return date;
    }



}
