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

package org.ramadda.repository.job;


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
 * @version        $version$, Mon, Sep 15, '14
 * @author         Enter your name here...
 */
public class CommandInfo {

    /** _more_ */
    private File workDir;

    /** _more_ */
    private boolean forDisplay = false;

    /** _more_ */
    private boolean publish = false;


    /** _more_ */
    private Hashtable<String, String> params = new Hashtable<String,
                                                   String>();

    /**
     * _more_
     *
     * @param commandInfo _more_
     */
    public CommandInfo(CommandInfo commandInfo) {
        this.workDir    = commandInfo.workDir;
        this.forDisplay = commandInfo.forDisplay;
        this.publish    = commandInfo.publish;
    }

    /**
     * _more_
     *
     * @param workDir _more_
     * @param forDisplay _more_
     */
    public CommandInfo(File workDir, boolean forDisplay) {
        this.workDir    = workDir;
        this.forDisplay = forDisplay;
    }


    /**
     * _more_
     *
     * @param key _more_
     * @param value _more_
     */
    public void addParam(String key, String value) {
        params.put(key, value);
    }

    /**
     * _more_
     *
     * @return _more_
     */
    public Hashtable<String, String> getParams() {
        return params;
    }


    /**
     * Set the Publish property.
     *
     * @param value The new value for Publish
     */
    public void setPublish(boolean value) {
        publish = value;
    }

    /**
     * Get the Publish property.
     *
     * @return The Publish
     */
    public boolean getPublish() {
        return publish;
    }



    /**
     * _more_
     *
     * @return _more_
     */
    public File getWorkDir() {
        return workDir;
    }

    /**
     * _more_
     *
     * @return _more_
     */
    public boolean getForDisplay() {
        return forDisplay;
    }

}
