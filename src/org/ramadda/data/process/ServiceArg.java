/*
* Copyright 2008-2014 Geode Systems LLC
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

package org.ramadda.data.process;


import org.ramadda.repository.*;
import org.ramadda.repository.auth.*;
import org.ramadda.repository.job.JobManager;
import org.ramadda.repository.output.OutputHandler;
import org.ramadda.repository.type.*;
import org.ramadda.util.HtmlUtils;
import org.ramadda.util.Utils;


import org.w3c.dom.*;

import ucar.unidata.util.IOUtil;
import ucar.unidata.util.Misc;
import ucar.unidata.util.PatternFileFilter;
import ucar.unidata.util.StringUtil;
import ucar.unidata.util.TwoFacedObject;
import ucar.unidata.xml.XmlUtil;

import java.io.*;

import java.lang.reflect.*;

import java.net.*;

import java.util.ArrayList;
import java.util.Date;

import java.util.Enumeration;
import java.util.Enumeration;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.List;
import java.util.Properties;


import java.util.regex.*;
import java.util.zip.*;




/**
 * Class description
 *
 *
 * @version        $version$, Fri, Oct 10, '14
 * @author         Enter your name here...
 */
public class ServiceArg implements Constants {

    /** _more_ */
    private static final String TYPE_STRING = "string";

    /** _more_ */
    private static final String TYPE_ENUMERATION = "enumeration";

    /** _more_ */
    private static final String TYPE_INT = "int";

    /** _more_ */
    private static final String TYPE_FLOAT = "float";

    /** _more_ */
    private static final String TYPE_ENTRY = "entry";

    /** _more_ */
    private static final String TYPE_FLAG = "flag";

    /** _more_ */
    private static final String TYPE_FILE = "file";

    /** _more_ */
    private static final String TYPE_CATEGORY = "category";

    /** _more_ */
    private Service service;

    /** _more_ */
    private String name;


    /** _more_ */
    private String value;

    /** _more_ */
    private String dflt;

    /** _more_ */
    private String prefix;

    /** _more_ */
    private String group;

    /** _more_ */
    private String file;

    /** _more_ */
    private String filePattern;

    /** _more_ */
    private boolean nameDefined = false;


    /** _more_ */
    private boolean ifDefined = true;


    /** _more_ */
    private boolean multiple = false;

    /** _more_ */
    private boolean first = false;

    /** _more_ */
    private String multipleJoin;

    /** _more_ */
    private boolean sameRow = false;


    /** _more_ */
    private String label;

    /** _more_ */
    private String help;

    /** _more_ */
    private String type;



    /** _more_ */
    private String depends;

    /** _more_ */
    private boolean addAll;

    /** _more_ */
    private boolean addNone;

    /** _more_ */
    private String placeHolder;

    /** _more_ */
    private String entryType;

    /** _more_ */
    private String entryPattern;

    /** _more_ */
    private boolean isPrimaryEntry = false;

    /** _more_ */
    private boolean required = false;

    /** _more_ */
    private boolean copy = false;

    /** _more_ */
    private String fileName;

    /** _more_ */
    private int size;


    /** _more_ */
    private List<TwoFacedObject> values = new ArrayList<TwoFacedObject>();

    /** _more_ */
    private String valuesProperty;


    /**
     * _more_
     *
     *
     * @param service _more_
     * @param node _more_
     * @param idx _more_
     *
     * @throws Exception _more_
     */
    public ServiceArg(Service service, Element node, int idx)
            throws Exception {
        this.service = service;

        type = XmlUtil.getAttribute(node, Service.ATTR_TYPE, (String) null);
        depends      = XmlUtil.getAttribute(node, "depends", (String) null);
        addAll       = XmlUtil.getAttribute(node, "addAll", false);
        addNone      = XmlUtil.getAttribute(node, "addNone", false);


        entryType = XmlUtil.getAttributeFromTree(node,
                Service.ATTR_ENTRY_TYPE, (String) null);

        entryPattern = XmlUtil.getAttributeFromTree(node,
                Service.ATTR_ENTRY_PATTERN, (String) null);

        placeHolder = XmlUtil.getAttribute(node, "placeHolder",
                                           (String) null);
        isPrimaryEntry = XmlUtil.getAttribute(node, Service.ATTR_PRIMARY,
                isPrimaryEntry);

        prefix = XmlUtil.getAttribute(node, "prefix", (String) null);
        value  = Utils.getAttributeOrTag(node, "value", "");
        dflt   = Utils.getAttributeOrTag(node, "default", "");
        name   = XmlUtil.getAttribute(node, Service.ATTR_NAME, (String) null);

        if ((name == null) && isEntry() && isPrimaryEntry) {
            name = "input_file";
        }

        nameDefined = name != null;
        if ((name == null) && (prefix != null)) {
            name = prefix.replaceAll("-", "");
        }
        if ((name == null) && Utils.stringDefined(value)) {
            name = value.replaceAll("-", "");
        }
        if (name == null) {
            name = "arg" + idx;
        }

        group = XmlUtil.getAttribute(node, Service.ATTR_GROUP, (String) null);
        file = XmlUtil.getAttribute(node, Service.ATTR_FILE, (String) null);
        filePattern = XmlUtil.getAttribute(node, "filePattern",
                                           (String) null);
        required = XmlUtil.getAttribute(node, "required", required);
        copy     = XmlUtil.getAttribute(node, "copy", false);
        valuesProperty = XmlUtil.getAttribute(node, "valuesProperty",
                (String) null);
        label    = XmlUtil.getAttribute(node, Service.ATTR_LABEL, name);
        help     = Utils.getAttributeOrTag(node, "help", "");
        fileName = XmlUtil.getAttribute(node, "filename", "${src}");
        if (isInt()) {
            size = 5;
        } else if (isFloat()) {
            size = 10;
        } else {
            size = 24;
        }
        size      = XmlUtil.getAttribute(node, Service.ATTR_SIZE, size);
        ifDefined = XmlUtil.getAttribute(node, "ifdefined", ifDefined);
        multiple  = XmlUtil.getAttribute(node, "multiple", multiple);
        first     = XmlUtil.getAttribute(node, "first", false);
        multipleJoin = XmlUtil.getAttribute(node, "multipleJoin",
                                            (String) null);
        sameRow = XmlUtil.getAttribute(node, "sameRow", sameRow);
        size    = XmlUtil.getAttribute(node, "size", size);
        for (String tok :
                StringUtil.split(Utils.getAttributeOrTag(node,
                    Service.ATTR_VALUES, ""), ",", true, true)) {
            List<String> toks  = StringUtil.splitUpTo(tok, ":", 2);

            String       value = toks.get(0);
            String       label;
            if (toks.size() > 1) {
                label = toks.get(1);
            } else {
                label = value;
            }
            values.add(new TwoFacedObject(label, value));
        }
    }



    /**
     * _more_
     *
     * @param entry _more_
     * @param debug _more_
     *
     * @return _more_
     */
    public boolean isApplicable(Entry entry, boolean debug) {
        boolean defaultReturn = true;
        //            debug  = true;

        if (debug) {
            System.err.println("Service.Arg.isApplicable:" + getName()
                               + " entry type:" + entryType + " pattern:"
                               + entryPattern);
        }
        if (entryType != null) {
            if ( !entry.getTypeHandler().isType(entryType)) {
                if (debug) {
                    System.err.println("\tentry is not type");
                }

                return false;
            }
            if (entryPattern == null) {
                if (debug) {
                    System.err.println("\thas entry type:" + entryType);
                }

                return true;
            }
        }
        if (entryPattern != null) {
            if (entry.getResource().getPath().toLowerCase().matches(
                    entryPattern)) {
                return true;
            }

            return false;
        }

        return defaultReturn;
    }


    /**
     * _more_
     *
     * @return _more_
     */
    public boolean isRequired() {
        return required;
    }

    /**
     * _more_
     *
     * @return _more_
     */
    public boolean isMultiple() {
        return multiple;
    }


    /**
     * _more_
     *
     * @return _more_
     */
    public String toString() {
        return getName() + " " + getLabel();
    }

    /**
     * _more_
     *
     * @return _more_
     */
    public boolean isValueArg() {
        return (type == null) && !isCategory() && !nameDefined
               && Utils.stringDefined(value);
    }

    /**
     * _more_
     *
     * @return _more_
     */
    public boolean getIfDefined() {
        return ifDefined;
    }


    /**
     * _more_
     *
     * @return _more_
     */
    public boolean isEnumeration() {
        return (type != null) && type.equals(TYPE_ENUMERATION);
    }

    /**
     * _more_
     *
     * @return _more_
     */
    public boolean isFlag() {
        return (type != null) && type.equals(TYPE_FLAG);
    }



    /**
     * _more_
     *
     * @return _more_
     */
    public boolean isFile() {
        return (type != null) && type.equals(TYPE_FILE);
    }

    /**
     * _more_
     *
     * @return _more_
     */
    public boolean isEntry() {
        return (type != null) && type.equals(TYPE_ENTRY);
    }

    /**
     * _more_
     *
     * @return _more_
     */
    public boolean isInt() {
        return (type != null) && type.equals(TYPE_INT);
    }

    /**
     * _more_
     *
     * @return _more_
     */
    public boolean isFloat() {
        return (type != null) && type.equals(TYPE_FLOAT);
    }

    /**
     * _more_
     *
     * @return _more_
     */
    public boolean isPrimaryEntry() {
        return isPrimaryEntry;
    }

    /**
     * _more_
     *
     * @return _more_
     */
    public boolean isCategory() {
        return (type != null) && type.equals(TYPE_CATEGORY);
    }


    /**
     * _more_
     *
     * @return _more_
     */
    public String getGroup() {
        return group;
    }


    /**
     * _more_
     *
     * @return _more_
     */
    public String getPrefix() {
        return prefix;
    }


    /**
     * _more_
     *
     * @return _more_
     */
    public String getEntryType() {
        return entryType;
    }




    /**
     * Set the Value property.
     *
     * @param value The new value for Value
     */
    public void setValue(String value) {
        value = value;
    }

    /**
     * Get the Value property.
     *
     * @return The Value
     */
    public String getValue() {
        return value;
    }


    /**
     * Get the Id property.
     *
     * @return The Id
     */
    public String getName() {
        return name;
    }

    /**
     * Set the Label property.
     *
     * @param value The new value for Label
     */
    public void setLabel(String value) {
        label = value;
    }

    /**
     * Get the Label property.
     *
     * @return The Label
     */
    public String getLabel() {
        return label;
    }


    /**
     * _more_
     *
     * @return _more_
     */
    public String getHelp() {
        return help;
    }

    /**
     * Set the Size property.
     *
     * @param value The new value for Size
     */
    public void setSize(int value) {
        size = value;
    }

    /**
     * Get the Size property.
     *
     * @return The Size
     */
    public int getSize() {
        return size;
    }

    /**
     * Set the FileName property.
     *
     * @param value The new value for FileName
     */
    public void setFileName(String value) {
        fileName = value;
    }

    /**
     * Get the FileName property.
     *
     * @return The FileName
     */
    public String getFileName() {
        return fileName;
    }


    /**
     * _more_
     *
     * @return _more_
     */
    public String getFile() {
        return file;
    }






    /**
     * _more_
     *
     * @return _more_
     */
    public String getFilePattern() {
        return filePattern;
    }

    /**
     * _more_
     *
     * @return _more_
     */
    public String getValuesProperty() {
        return valuesProperty;
    }




    /**
     * Set the Type property.
     *
     * @param value The new value for Type
     */
    public void setType(String value) {
        type = value;
    }

    /**
     * Get the Type property.
     *
     * @return The Type
     */
    public String getType() {
        return type;
    }

    /**
     * Set the Values property.
     *
     * @param value The new value for Values
     */
    public void setValues(List<TwoFacedObject> value) {
        values = value;
    }

    /**
     * Get the Values property.
     *
     * @return The Values
     */
    public List<TwoFacedObject> getValues() {
        return values;
    }


    /**
     *  Get the Category property.
     *
     *  @return The Category
     */
    public String getCategory() {
        if ( !isCategory()) {
            return null;
        }

        return label;
    }


    /**
     * _more_
     *
     * @return _more_
     */
    public String getDefault() {
        return dflt;
    }


    /**
     * _more_
     *
     * @return _more_
     */
    public boolean getSameRow() {
        return sameRow;
    }

    /**
     * _more_
     *
     * @return _more_
     */
    public boolean getFirst() {
        return first;
    }

    /**
     * _more_
     *
     * @return _more_
     */
    public boolean getAddAll() {
        return addAll;
    }

    /**
     * _more_
     *
     * @return _more_
     */
    public String getPlaceHolder() {
        return placeHolder;
    }

    /**
     * _more_
     *
     * @return _more_
     */
    public boolean getAddNone() {
        return addNone;
    }

    /**
     * _more_
     *
     * @return _more_
     */
    public boolean getCopy() {
        return copy;
    }

    /**
     * _more_
     *
     * @return _more_
     */
    public String getDepends() {
        return depends;
    }

    /**
     * _more_
     *
     * @return _more_
     */
    public String getMultipleJoin() {
        return multipleJoin;
    }

}