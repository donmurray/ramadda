/*
 * Copyright 1997-2010 Unidata Program Center/University Corporation for
 * Atmospheric Research, P.O. Box 3000, Boulder, CO 80307,
 * support@unidata.ucar.edu.
 * Copyright 2010- ramadda.org
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
 * 
 */

package ucar.unidata.repository.harvester;


import org.apache.log4j.Logger;


import org.w3c.dom.*;

import ucar.unidata.repository.*;
import ucar.unidata.repository.auth.*;
import ucar.unidata.repository.output.OutputHandler;
import ucar.unidata.repository.type.*;

import ucar.unidata.util.StringUtil;
import ucar.unidata.util.TwoFacedObject;
import ucar.unidata.xml.XmlUtil;

import java.io.ByteArrayInputStream;

import java.io.File;
import java.io.InputStream;

import java.lang.reflect.*;



import java.net.*;

import java.text.SimpleDateFormat;

import java.util.ArrayList;
import java.util.Date;
import java.util.Enumeration;
import java.util.GregorianCalendar;
import java.util.Hashtable;
import java.util.List;
import java.util.Properties;



import java.util.regex.*;


/**
 * Class HarvesterEntry _more_
 *
 *
 * @author IDV Development Team
 * @version $Revision: 1.3 $
 */
public class HarvesterEntry {

    /** _more_ */
    public static final String TAG_URLENTRY = "urlentry";

    /** _more_          */
    public static final String ATTR_URL = "url";

    /** _more_          */
    public static final String ATTR_NAME = "name";

    /** _more_          */
    public static final String ATTR_DESCRIPTION = "description";

    /** _more_          */
    public static final String ATTR_GROUP = "group";

    /** _more_ */
    String url;

    /** _more_ */
    String name;

    /** _more_ */
    String description;

    /** _more_ */
    String baseGroupId;

    /** _more_ */
    String group;

    /** _more_          */
    private Hashtable properties = new Hashtable();

    public HarvesterEntry() {}

    /**
     * _more_
     *
     * @param url _more_
     * @param name _more_
     * @param description _more_
     * @param group _more_
     * @param baseGroupId _more_
     */
    public HarvesterEntry(String url, String name, String description,
                          String group, String baseGroupId) {
        this.url         = url;
        this.name        = name;
        this.description = description;
        this.group       = group;
        this.baseGroupId = baseGroupId;

    }


    /**
     * _more_
     *
     * @param node _more_
     */
    public HarvesterEntry(Element node) {
        this.url         = XmlUtil.getAttribute(node, ATTR_URL, "");
        this.name        = XmlUtil.getAttribute(node, ATTR_NAME, "");
        this.description = XmlUtil.getAttribute(node, ATTR_DESCRIPTION, "");
        this.group       = XmlUtil.getAttribute(node, ATTR_GROUP, "");
        this.baseGroupId = XmlUtil.getAttribute(node,
                Harvester.ATTR_BASEGROUP, "");
    }


    /**
     * _more_
     *
     * @param element _more_
     *
     * @return _more_
     *
     * @throws Exception _more_
     */
    public Element toXml(Element element) throws Exception {
        Element node = XmlUtil.create(element.getOwnerDocument(),
                                      TAG_URLENTRY, element, new String[] {
            ATTR_URL, url, ATTR_NAME, name, ATTR_DESCRIPTION, description,
            ATTR_GROUP, group, Harvester.ATTR_BASEGROUP, baseGroupId
        });

        return node;
    }


    /**
     * _more_
     *
     * @return _more_
     */
    public String toString() {
        return url;
    }



    /**
     *  Set the Url property.
     *
     *  @param value The new value for Url
     */
    public void setUrl(String value) {
        url = value;
    }

    /**
     *  Get the Url property.
     *
     *  @return The Url
     */
    public String getUrl() {
        return url;
    }

    /**
     *  Set the Name property.
     *
     *  @param value The new value for Name
     */
    public void setName(String value) {
        name = value;
    }

    /**
     *  Get the Name property.
     *
     *  @return The Name
     */
    public String getName() {
        return name;
    }

    /**
     *  Set the Description property.
     *
     *  @param value The new value for Description
     */
    public void setDescription(String value) {
        description = value;
    }

    /**
     *  Get the Description property.
     *
     *  @return The Description
     */
    public String getDescription() {
        return description;
    }

    /**
     *  Set the BaseGroupId property.
     *
     *  @param value The new value for BaseGroupId
     */
    public void setBaseGroupId(String value) {
        baseGroupId = value;
    }

    /**
     *  Get the BaseGroupId property.
     *
     *  @return The BaseGroupId
     */
    public String getBaseGroupId() {
        return baseGroupId;
    }

    /**
     *  Set the Group property.
     *
     *  @param value The new value for Group
     */
    public void setGroup(String value) {
        group = value;
    }

    /**
     *  Get the Group property.
     *
     *  @return The Group
     */
    public String getGroup() {
        return group;
    }

    /**
     *  Set the Properties property.
     *
     *  @param value The new value for Properties
     */
    public void setProperties(Hashtable value) {
        properties = value;
    }

    /**
     *  Get the Properties property.
     *
     *  @return The Properties
     */
    public Hashtable getProperties() {
        return properties;
    }



}
