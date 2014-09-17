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
 * @version        $version$, Wed, Sep 17, '14
 * @author         Enter your name here...
 */
public class CommandOutput {

    /** _more_ */
    private boolean ok = true;

    /** _more_ */
    private boolean resultsShownAsText = false;

    /** _more_ */
    private StringBuffer results = new StringBuffer();

    /** _more_ */
    private List<Entry> entries = new ArrayList<Entry>();

    /**
     * _more_
     */
    public CommandOutput() {}

    /**
     * _more_
     *
     * @param ok _more_
     * @param message _more_
     */
    public CommandOutput(boolean ok, String message) {
        this.ok = ok;
        this.results.append(message);
    }

    /**
     * _more_
     *
     * @param entry _more_
     */
    public void addEntry(Entry entry) {
        entries.add(entry);
    }

    /**
     * _more_
     *
     * @return _more_
     */
    public List<Entry> getEntries() {
        return entries;
    }

    /**
     * _more_
     *
     * @return _more_
     */
    public boolean isOk() {
        return ok;
    }

    /**
     * _more_
     *
     * @param b _more_
     */
    public void setOk(boolean b) {
        this.ok = b;
    }

    /**
     * _more_
     *
     * @param s _more_
     */
    public void append(String s) {
        results.append(s);
    }

    /**
     * _more_
     *
     * @return _more_
     */
    public String getResults() {
        return results.toString();
    }



    /**
     * Set the ResultsShownAsText property.
     *
     * @param value The new value for ResultsShownAsText
     */
    public void setResultsShownAsText(boolean value) {
        resultsShownAsText = value;
    }

    /**
     * Get the ResultsShownAsText property.
     *
     * @return The ResultsShownAsText
     */
    public boolean getResultsShownAsText() {
        return resultsShownAsText;
    }


}
