/*
 * Copyright 2008-2011 Jeff McWhirter/ramadda.org
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

package org.ramadda.util;


import org.ramadda.repository.*;

import org.ramadda.repository.monitor.*;


import org.w3c.dom.*;



import ucar.unidata.sql.Clause;

import ucar.unidata.sql.SqlUtil;

import ucar.unidata.ui.ImageUtils;
import ucar.unidata.util.DateUtil;
import ucar.unidata.util.HtmlUtil;
import ucar.unidata.util.HttpServer;
import ucar.unidata.util.IOUtil;
import ucar.unidata.util.JobManager;
import ucar.unidata.util.LogUtil;
import ucar.unidata.util.Misc;
import ucar.unidata.util.PatternFileFilter;
import ucar.unidata.util.PluginClassLoader;

import ucar.unidata.util.StringUtil;
import ucar.unidata.util.TwoFacedObject;

import ucar.unidata.xml.XmlUtil;



import java.io.*;

import java.io.File;
import java.io.InputStream;

import java.lang.reflect.*;



import java.net.*;

import java.util.Hashtable;
import java.util.List;
import java.util.Map;
import java.util.Properties;


import java.util.jar.*;



import java.util.regex.*;
import java.util.zip.*;


/**
 * Class HtmlTemplate _more_
 *
 *
 * @author IDV Development Team
 */
public class HtmlTemplate {

    /** _more_ */
    private PropertyProvider propertyProvider;

    /** _more_ */
    private String name;

    /** _more_ */
    private String id;

    /** _more_ */
    private String template;

    /** _more_ */
    private String path;

    /** _more_ */
    private Hashtable properties = new Hashtable();


    /**
     * _more_
     *
     *
     *
     * @param propertyProvider _more_
     * @param path _more_
     * @param t _more_
     */
    public HtmlTemplate(PropertyProvider propertyProvider, String path,
                        String t) {
        try {

            this.propertyProvider = propertyProvider;
            this.path             = path;
            Pattern pattern =
                Pattern.compile("(?s)(.*)<properties>(.*)</properties>(.*)");
            Matcher matcher = pattern.matcher(t);
            if (matcher.find()) {
                template = matcher.group(1) + matcher.group(3);
                Properties p = new Properties();
                p.load(new ByteArrayInputStream(matcher.group(2).getBytes()));
                properties.putAll(p);
                //                System.err.println ("got props " + properties);
            } else {
                template = t;
            }
            name = (String) properties.get("name");
            id   = (String) properties.get("id");
            if (name == null) {
                name = IOUtil.stripExtension(IOUtil.getFileTail(path));
            }
            if (id == null) {
                //                id = IOUtil.stripExtension(IOUtil.getFileTail(path));
            }
        } catch (Exception exc) {
            throw new RuntimeException(exc);
        }

    }

    /**
     * _more_
     *
     * @return _more_
     */
    public String getTemplate() {
        return template;
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
    public String getName() {
        return name;
    }


    /**
     * _more_
     *
     * @return _more_
     */
    public String toString() {
        return name;
    }

    /**
     * _more_
     *
     * @param request _more_
     *
     * @return _more_
     */
    public boolean isTemplateFor(Request request) {
        if (request.getUser() == null) {
            return false;
        }
        String templateId = request.getUser().getTemplate();
        if (templateId == null) {
            return false;
        }
        if (Misc.equals(id, templateId)) {
            return true;
        }
        return false;
    }

    /**
     * _more_
     *
     * @param name _more_
     * @param dflt _more_
     *
     * @return _more_
     */
    public String getTemplateProperty(String name, String dflt) {
        String value = (String) properties.get(name);
        if (value != null) {
            return value;
        }
        return propertyProvider.getProperty(name, dflt);

    }

}
