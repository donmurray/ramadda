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
 * 
 */

package org.ramadda.repository;


import org.w3c.dom.*;




import ucar.unidata.sql.SqlUtil;


import ucar.unidata.util.GuiUtils;
import ucar.unidata.util.HtmlUtil;
import ucar.unidata.util.IOUtil;
import ucar.unidata.util.LogUtil;
import ucar.unidata.util.Misc;


import ucar.unidata.util.StringUtil;
import ucar.unidata.util.TwoFacedObject;

import ucar.unidata.xml.XmlUtil;





import java.util.ArrayList;
import java.util.Hashtable;
import java.util.List;



/**
 * 
 *
 *
 * @author RAMADDA Development Team
 * @version $Revision: 1.3 $
 */
public class EntryGroup {

    /** _more_ */
    private Object key;

    /** _more_ */
    private String name;

    /** _more_ */
    private List children = new ArrayList();


    /** _more_ */
    private List keys = new ArrayList();

    /** _more_ */
    private Hashtable map = new Hashtable();

    /**
     * _more_
     *
     * @param key _more_
     */
    public EntryGroup(Object key) {
        this.key = key;
    }




    /**
     * _more_
     *
     * @param key _more_
     *
     * @return _more_
     */
    public EntryGroup find(Object key) {
        EntryGroup group = (EntryGroup) map.get(key);
        if (group == null) {
            group = new EntryGroup(key);
            map.put(key, group);
            keys.add(key);
        }
        return group;
    }

    /**
     * _more_
     *
     * @param obj _more_
     */
    public void add(Object obj) {
        children.add(obj);
    }

    /**
     * _more_
     *
     * @return _more_
     */
    public List keys() {
        return keys;
    }


    /**
     * Set the Key property.
     *
     * @param value The new value for Key
     */
    public void setKey(Object value) {
        key = value;
    }

    /**
     * Get the Key property.
     *
     * @return The Key
     */
    public Object getKey() {
        return key;
    }

    /**
     * Set the Name property.
     *
     * @param value The new value for Name
     */
    public void setName(String value) {
        name = value;
    }

    /**
     * Get the Name property.
     *
     * @return The Name
     */
    public String getName() {
        return name;
    }

    /**
     * Set the Children property.
     *
     * @param value The new value for Children
     */
    public void setChildren(List value) {
        children = value;
    }

    /**
     * Get the Children property.
     *
     * @return The Children
     */
    public List getChildren() {
        return children;
    }


    /**
     * Set the Keys property.
     *
     * @param value The new value for Keys
     */
    public void setKeys(List value) {
        keys = value;
    }

    /**
     * Get the Keys property.
     *
     * @return The Keys
     */
    public List getKeys() {
        return keys;
    }

    /**
     * Set the Map property.
     *
     * @param value The new value for Map
     */
    public void setMap(Hashtable value) {
        map = value;
    }

    /**
     * Get the Map property.
     *
     * @return The Map
     */
    public Hashtable getMap() {
        return map;
    }


}
