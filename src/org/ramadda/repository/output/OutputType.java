/*
 * Copyright 1997-2010 Unidata Program Center/University Corporation for
 * Atmospheric Research, P.O. Box 3000, Boulder, CO 80307,
 * support@unidata.ucar.edu.
 * 
 * This library is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation; either version 2.1 of the License, or (at
 * your option) any later version.
 * 
 * This library is distributed in the hope that it will be useful, but
 * WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU Lesser
 * General Public License for more details.
 * 
 * You should have received a copy of the GNU Lesser General Public License
 * along with this library; if not, write to the Free Software Foundation,
 * Inc., 59 Temple Place, Suite 330, Boston, MA 02111-1307 USA
 */

package org.ramadda.repository.output;


import org.w3c.dom.*;

import org.ramadda.repository.*;
import org.ramadda.repository.auth.*;

import ucar.unidata.sql.SqlUtil;
import ucar.unidata.util.Counter;
import ucar.unidata.util.DateUtil;
import ucar.unidata.util.HtmlUtil;
import ucar.unidata.util.IOUtil;
import ucar.unidata.util.Misc;


import ucar.unidata.util.StringUtil;
import ucar.unidata.util.TwoFacedObject;
import ucar.unidata.xml.XmlUtil;


import java.io.*;

import java.io.File;


import java.net.*;




import java.util.ArrayList;
import java.util.Date;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.List;
import java.util.Properties;



import java.util.regex.*;

import java.util.zip.*;


/**
 * Class SqlUtil _more_
 *
 *
 * @author IDV Development Team
 * @version $Revision: 1.3 $
 */
public class OutputType {

    /** _more_ */
    public static final int TYPE_HTML = 1 << 0;

    /** _more_ */
    public static final int TYPE_NONHTML = 1 << 1;

    /** _more_ */
    public static final int TYPE_ACTION = 1 << 2;

    /** _more_ */
    public static final int TYPE_INTERNAL = 1 << 3;

    /** _more_ */
    public static final int TYPE_FILE = 1 << 4;

    /** _more_ */
    public static final int TYPE_EDIT = 1 << 5;

    /** _more_ */
    public static final int TYPE_VIEW = 1 << 6;

    /** _more_ */
    public static final int TYPE_TOOLBAR = 1 << 7;

    /** _more_ */
    public static final int TYPE_FORSEARCH = 1 << 8;


    /** _more_ */
    public static final int TYPE_CATEGORY = 1 << 9;
    
    /** _more_ */
    public static final int TYPE_CONNECT = 1 << 10;

    /** _more_ */
    public static final int TYPE_EXPORT = 1 << 11;
    
    /** _more_ */
    public static final int TYPE_ALL = TYPE_HTML | TYPE_ACTION | TYPE_NONHTML
                                       | TYPE_FILE | TYPE_EDIT | TYPE_VIEW
                                       | TYPE_TOOLBAR | TYPE_CATEGORY | TYPE_CONNECT;


    /** _more_ */
    public static String ICON_NULL = null;

    /** _more_ */
    public static String SUFFIX_NONE = "";

    /** _more_ */
    private String suffix = SUFFIX_NONE;

    /** _more_ */
    private String id;

    /** _more_ */
    private String label;

    /** _more_ */
    private boolean forUser = true;

    /** _more_ */
    private String groupName = "";

    private String category = "";



    /** _more_ */
    private String icon;

    /** _more_ */
    private int type = TYPE_HTML;

    /** _more_ */
    private Counter numberOfCalls = new Counter();


    /**
     * _more_
     *
     * @param id _more_
     * @param type _more_
     */
    public OutputType(String id, int type) {
        this(id, id, type);
    }



    /**
     * _more_
     *
     * @param label _more_
     * @param id _more_
     * @param type _more_
     */
    public OutputType(String label, String id, int type) {
        this(label, id, type, SUFFIX_NONE, ICON_NULL);
    }


    /**
     * _more_
     *
     * @param label _more_
     * @param id _more_
     * @param type _more_
     * @param suffix _more_
     * @param icon _more_
     */
    public OutputType(String label, String id, int type, String suffix,
                      String icon) {
        this(label,  id,  type,  suffix, icon,null);
    }

    public OutputType(String label, String id, int type, String suffix,
                      String icon, String category) {
        this.label  = label;
        this.id     = id;
        this.type   = type;
        this.suffix = suffix;
        this.icon   = icon;
        this.category = category;
    }




    /**
     * _more_
     *
     * @param that _more_
     */
    public OutputType(OutputType that) {
        this.icon   = that.icon;
        this.label  = that.label;
        this.id     = that.id;
        this.suffix = that.suffix;
        this.type   = that.type;
    }

    /**
     * _more_
     *
     * @param that _more_
     * @param suffix _more_
     */
    public OutputType(OutputType that, String suffix) {
        this(that);
        this.suffix = suffix;
    }

    /**
     * _more_
     *
     * @return _more_
     */
    public String getIcon() {
        return icon;
    }

    /**
     * _more_
     *
     * @return _more_
     */
    public String getId() {
        return id;
    }

    /**
     * _more_
     *
     * @return _more_
     */
    public String getLabel() {
        return label;
    }

    /**
     * _more_
     *
     * @param request _more_
     *
     * @return _more_
     */
    public String assembleUrl(Request request) {
        return request.getRequestPath() + getSuffix() + "?"
               + request.getUrlArgs();
    }


    /**
     * Set the Suffix property.
     *
     * @param value The new value for Suffix
     */
    public void setSuffix(String value) {
        suffix = value;
    }

    /**
     * Get the Suffix property.
     *
     * @return The Suffix
     */
    public String getSuffix() {
        return suffix;
    }


    /**
     * String representation of this object.
     * @return toString() method of label.
     */
    public String toString() {
        return id;
    }


    /**
     * _more_
     *
     * @param other _more_
     *
     * @return _more_
     */
    public boolean equals(Object other) {
        if ( !(other instanceof OutputType)) {
            return false;
        }
        OutputType that = (OutputType) other;
        return Misc.equals(id, that.id);
    }

    /**
     *  Set the ForUser property.
     *
     *  @param value The new value for ForUser
     */
    public void setForUser(boolean value) {
        forUser = value;
    }

    /**
     *  Get the ForUser property.
     *
     *  @return The ForUser
     */
    public boolean getForUser() {
        return forUser;
    }


    /**
     * Set the GroupName property.
     *
     * @param value The new value for GroupName
     */
    public void setGroupName(String value) {
        groupName = value;
    }

    /**
     * Get the GroupName property.
     *
     * @return The GroupName
     */
    public String getGroupName() {
        return groupName;
    }


    public String getCategory() {
        return category;
    }


    /**
     * _more_
     *
     * @return _more_
     */
    public int getType() {
        return type;
    }

    /**
     * _more_
     *
     * @param flag _more_
     *
     * @return _more_
     */
    public boolean isType(int flag) {
        return (flag & type) != 0;
    }

    /**
     *  Get the IsHtml property.
     *
     *  @return The IsHtml
     */
    public boolean getIsHtml() {
        return isType(TYPE_HTML);
    }


    /**
     *
     *  @return The IsHtml
     */
    public boolean getIsForSearch() {
        return isType(TYPE_FORSEARCH);
    }

    /**
     * _more_
     *
     * @return _more_
     */
    public boolean getIsAction() {
        return isType(TYPE_ACTION);
    }


    /**
     * _more_
     *
     * @return _more_
     */
    public boolean getIsNonHtml() {
        return isType(TYPE_NONHTML);
    }

    /**
     * _more_
     *
     * @return _more_
     */
    public boolean getIsInternal() {
        return isType(TYPE_INTERNAL);
    }

    /**
     * _more_
     *
     * @return _more_
     */
    public boolean getIsFile() {
        return isType(TYPE_FILE);
    }

    /**
     * _more_
     *
     * @return _more_
     */
    public boolean getIsEdit() {
        return isType(TYPE_EDIT);
    }

    /**
     * _more_
     *
     * @return _more_
     */
    public boolean getIsView() {
        return isType(TYPE_VIEW);
    }

    /**
     * _more_
     *
     * @return _more_
     */
    public int getNumberOfCalls() {
        return numberOfCalls.getCount();
    }

    /**
     * _more_
     */
    public void incrNumberOfCalls() {
        numberOfCalls.incr();
    }




}
