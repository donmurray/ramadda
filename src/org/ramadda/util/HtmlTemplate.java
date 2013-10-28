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

package org.ramadda.util;



import org.ramadda.util.HtmlUtils;


import org.w3c.dom.*;




import ucar.unidata.ui.ImageUtils;
import ucar.unidata.util.DateUtil;
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

import java.util.ArrayList;

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
    public static final String PROP_PROPERTIES =
        "ramadda.template.properties";


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


    /** _more_ */
    private List<String> propertyIds = new ArrayList<String>();

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
            String tmp = (String) properties.get(PROP_PROPERTIES);
            if (tmp != null) {
                propertyIds = StringUtil.split(tmp, ",", true, true);
            }

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
    public List<String> getPropertyIds() {
        return propertyIds;
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
