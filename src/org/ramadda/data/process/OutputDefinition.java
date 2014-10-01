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
import org.ramadda.repository.output.OutputHandler;
import org.ramadda.repository.type.*;
import org.ramadda.util.HtmlUtils;
import org.ramadda.util.Utils;




import org.w3c.dom.*;

import ucar.unidata.util.IOUtil;
import ucar.unidata.util.Misc;
import ucar.unidata.util.StringUtil;
import ucar.unidata.util.TwoFacedObject;
import ucar.unidata.xml.XmlUtil;

import java.io.*;

import java.lang.reflect.*;

import java.net.*;

import java.util.ArrayList;
import java.util.Date;
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
 * @version        $version$, Thu, Sep 4, '14
 * @author         Enter your name here...
 */
public class OutputDefinition {

    /** _more_ */
    private String entryType;

    /** _more_ */
    private String pattern;

    private String depends;
    private boolean  notDepends;

    /** _more_ */
    private boolean useStdout = false;

    /** _more_ */
    private String filename;

    /** _more_ */
    private boolean showResults = false;



    /**
     * _more_
     *
     * @param node _more_
     */
    public OutputDefinition(Element node) {
        entryType = XmlUtil.getAttribute(node, Service.ATTR_TYPE,
                                         TypeHandler.TYPE_FILE);
        pattern     = XmlUtil.getAttribute(node, "pattern", (String) null);
        useStdout   = XmlUtil.getAttribute(node, "stdout", useStdout);
        filename    = XmlUtil.getAttribute(node, "filename", (String) null);
        depends     = XmlUtil.getAttribute(node, "depends", (String) null);
        showResults = XmlUtil.getAttribute(node, "showResults", showResults);
    }


    /**
     *  Set the EntryType property.
     *
     *  @param value The new value for EntryType
     */
    public void setEntryType(String value) {
        entryType = value;
    }

    /**
     *  Get the EntryType property.
     *
     *  @return The EntryType
     */
    public String getEntryType() {
        return entryType;
    }

    /**
     *  Set the Pattern property.
     *
     *  @param value The new value for Pattern
     */
    public void setPattern(String value) {
        pattern = value;
    }

    /**
     * _more_
     *
     * @return _more_
     */
    public String getFilename() {
        return filename;
    }


    /**
     *  Get the Pattern property.
     *
     *  @return The Pattern
     */
    public String getPattern() {
        return pattern;
    }

    /**
     * _more_
     *
     * @return _more_
     */
    public boolean getShowResults() {
        return showResults;
    }

    /**
     * _more_
     *
     * @return _more_
     */
    public boolean getUseStdout() {
        return useStdout;
    }

    /**
       Set the Depends property.

       @param value The new value for Depends
    **/
    public void setDepends (String value) {
	depends = value;
    }

    /**
       Get the Depends property.

       @return The Depends
    **/
    public String getDepends () {
	return depends;
    }


}
