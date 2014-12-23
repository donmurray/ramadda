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

package org.ramadda.geodata.model;


import org.ramadda.repository.Entry;
import org.ramadda.repository.Repository;
import org.ramadda.repository.Request;
import org.ramadda.repository.type.GenericTypeHandler;
import org.ramadda.repository.type.GranuleTypeHandler;
import org.ramadda.repository.type.TypeHandler;

import org.w3c.dom.Element;

import ucar.unidata.util.IOUtil;

import java.util.Date;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;


/**
 * A class for handling climate model files
 */
public class ClimateModelFileTypeHandler extends GranuleTypeHandler {

    /** the default file regex */
    public static final String FILE_REGEX =
        "([^_]+)_([^_]+)_(.*)_(ens..|mean|sprd|clim)(_([^_]+))?.nc";

    /** local regex */
    private String myRegex = FILE_REGEX;

    /** the regex property */
    public static final String PROP_FILE_PATTERN = "model.file.pattern";

    /** pattern for file names */
    //public static final Pattern pattern = Pattern.compile(FILE_REGEX);
    protected Pattern pattern = null;

    /** ClimateModelFile type */
    public static final String TYPE_CLIMATE_MODELFILE = "climate_modelfile";

    /**
     * _more_
     *
     * @param repository _more_
     * @param entryNode _more_
     *
     * @throws Exception _more_
     */
    public ClimateModelFileTypeHandler(Repository repository,
                                       Element entryNode)
            throws Exception {
        this(repository, entryNode, null);
    }

    /**
     * Create a ClimateModelFileTypeHandler with the given pattern
     *
     * @param repository  the repository
     * @param entryNode   the node
     * @param regex_pattern   the pattern for file names
     *
     * @throws Exception  the pattern
     */
    public ClimateModelFileTypeHandler(Repository repository,
                                       Element entryNode,
                                       String regex_pattern)
            throws Exception {
        super(repository, entryNode);
        if (regex_pattern == null) {
            myRegex = getProperty(PROP_FILE_PATTERN, FILE_REGEX);
        }
        pattern = Pattern.compile(myRegex);
    }

    /**
     * Initialize the entry
     *
     *
     * @param request _more_
     * @param entry the Entry
     *
     * @throws Exception  problems during initialization
     */
    @Override
    public void initializeNewEntry(Request request, Entry entry)
            throws Exception {
        super.initializeNewEntry(request, entry);
        Object[] values = getEntryValues(entry);
        if ((values[1] != null) && !values[1].toString().isEmpty()) {
            //System.err.println("already have  values set");
            return;
        }
        //System.err.println("no values set");
        String filepath = entry.getFile().toString();
        // strip off entry.das in case it's an opendap link from RAMADDA
        filepath = filepath.replaceAll("/entry.das", "");
        String filename = IOUtil.getFileTail(filepath);
        // Filename looks like  var_model_scenario_ens??_<date>.nc
        Matcher m = pattern.matcher(filename);
        if ( !m.find()) {
            System.err.println("no match");

            return;
        }
        String var        = m.group(1);
        String model      = m.group(2);
        String experiment = m.group(3);
        String member     = m.group(4);
        String date       = m.group(6);
        String frequency  = "Monthly";
        if (filepath.indexOf("Daily") >= 0) {
            frequency = "Daily";
        }

        /*
     <column name="collection_id" type="string"  label="Collection ID" showinhtml="false" showinform="false"/>
     <column name="model" type="enumerationplus"  label="Model"  showinhtml="true" xxxxvalues="file:/org/ramadda/data/model/models.txt"/>
     <column name="experiment" type="enumerationplus"  label="Experiment" xxxxvalues="file:/org/ramadda/data/model/experiments.txt" showinhtml="true" />
     <column name="ensemble" type="string"  label="Ensemble"/>
     <column name="variable" type="enumerationplus"  label="Variable"  xxxxxvalues="file:/org/ramadda/data/model/vars.txt"/>
        */

        int idx = 1;
        values[idx++] = model;
        values[idx++] = experiment;
        values[idx++] = member;
        values[idx++] = var;

    }



    /**
     * This gets called from the wiki chart displays
     * we route the request to the cdm_grid type handler
     *
     * @param request The request
     * @param entry The entry
     * @param tag The wiki tag being used
     *
     * @return The point time series url
     */
    @Override
    public String getUrlForWiki(Request request, Entry entry, String tag) {
        try {
            TypeHandler gridType = getRepository().getTypeHandler("cdm_grid");
            if (gridType != null) {
                return gridType.getUrlForWiki(request, entry, tag);
            }
        } catch (Exception exc) {
            throw new RuntimeException(exc);
        }

        return super.getUrlForWiki(request, entry, tag);

    }



    /**
     * Test it
     *
     * @param args  the arguments
     * public static void main(String[] args) {
     *   for (String arg : args) {
     *       Matcher m = pattern.matcher(arg);
     *       if ( !m.find()) {
     *           System.err.println("no match x");
     *       } else {
     *           System.err.println("match");
     *           String var = m.group(1);
     *           System.err.println("var:" + var);
     *       }
     *   }
     * }
     *
     * @param request _more_
     * @param entry _more_
     * @param tag _more_
     *
     * @return _more_
     */



}
