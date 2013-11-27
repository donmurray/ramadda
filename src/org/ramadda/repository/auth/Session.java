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

package org.ramadda.repository.auth;


import org.ramadda.repository.*;


import org.ramadda.repository.database.*;


import org.ramadda.sql.Clause;


import org.ramadda.sql.SqlUtil;
import org.ramadda.util.HtmlUtils;
import org.ramadda.util.TTLCache;

import ucar.unidata.util.Cache;
import ucar.unidata.util.DateUtil;

import ucar.unidata.util.IOUtil;
import ucar.unidata.util.LogUtil;
import ucar.unidata.util.Misc;
import ucar.unidata.util.StringUtil;
import ucar.unidata.util.TwoFacedObject;
import ucar.unidata.xml.XmlUtil;

import java.io.File;


import java.io.UnsupportedEncodingException;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;


import java.sql.ResultSet;
import java.sql.Statement;


import java.util.ArrayList;
import java.util.Date;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.List;
import java.util.Properties;






/**
 * Class Session _more_
 *
 *
 * @author RAMADDA Development Team
 * @version $Revision: 1.3 $
 */
public class Session {

    /** _more_ */
    private String id;

    /** _more_ */
    private User user;

    /** _more_ */
    private Date createDate;

    /** _more_ */
    private Date lastActivity;

    /** _more_ */
    private Hashtable properties = new Hashtable();

    /**
     * _more_
     *
     * @param id _more_
     * @param user _more_
     * @param createDate _more_
     */
    public Session(String id, User user, Date createDate) {
        this(id, user, createDate, new Date());
    }

    /**
     * _more_
     *
     * @param id _more_
     * @param user _more_
     * @param createDate _more_
     * @param lastActivity _more_
     */
    public Session(String id, User user, Date createDate, Date lastActivity) {
        this.id           = id;
        this.user         = user;
        this.createDate   = createDate;
        this.lastActivity = lastActivity;
    }

    /**
     * _more_
     *
     * @return _more_
     */
    public String toString() {
        return "session:" + user + " id:" + id;
    }

    /**
     * _more_
     *
     * @param key _more_
     * @param value _more_
     */
    public void putProperty(Object key, Object value) {
        properties.put(key, value);
    }

    /**
     * _more_
     *
     * @param key _more_
     *
     * @return _more_
     */
    public Object getProperty(Object key) {
        return properties.get(key);
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
     *  Set the User property.
     *
     *  @param value The new value for User
     */
    public void setUser(User value) {
        user = value;
    }

    /**
     *  Get the User property.
     *
     *  @return The User
     */
    public User getUser() {
        return user;
    }

    /**
     * _more_
     *
     * @return _more_
     */
    public String getUserId() {
        if (user == null) {
            return "";
        }

        return user.getId();
    }

    /**
     *  Set the CreateDate property.
     *
     *  @param value The new value for CreateDate
     */
    public void setCreateDate(Date value) {
        createDate = value;
    }

    /**
     *  Get the CreateDate property.
     *
     *  @return The CreateDate
     */
    public Date getCreateDate() {
        return createDate;
    }

    /**
     *  Set the LastActivity property.
     *
     *  @param value The new value for LastActivity
     */
    public void setLastActivity(Date value) {
        lastActivity = value;
    }

    /**
     *  Get the LastActivity property.
     *
     *  @return The LastActivity
     */
    public Date getLastActivity() {
        return lastActivity;
    }



}
