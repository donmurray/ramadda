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

package org.ramadda.bio.genomics;


import org.ramadda.repository.*;
import org.ramadda.repository.auth.*;
import org.ramadda.repository.metadata.Metadata;
import org.ramadda.repository.output.*;

import org.ramadda.sql.SqlUtil;
import org.ramadda.util.HtmlUtils;
import org.ramadda.util.Utils;


import org.w3c.dom.*;

import ucar.unidata.ui.ImageUtils;
import ucar.unidata.util.DateUtil;
import ucar.unidata.util.IOUtil;
import ucar.unidata.util.Misc;


import ucar.unidata.util.StringUtil;
import ucar.unidata.util.WmsUtil;
import ucar.unidata.xml.XmlUtil;

import java.io.*;


import java.io.File;


import java.net.*;


import java.text.SimpleDateFormat;

import java.util.ArrayList;
import java.util.Date;
import java.util.Date;
import java.util.Enumeration;


import java.util.GregorianCalendar;
import java.util.Hashtable;
import java.util.List;
import java.util.Properties;
import java.util.TimeZone;



import java.util.regex.*;

import java.util.zip.*;


/**
 *
 *
 * @author IDV Development Team
 * @version $Revision: 1.3 $
 */
public class GenomicsOutputHandler extends OutputHandler {


    /** _more_ */
    public static final OutputType OUTPUT_GENOMICS_TEST1 =
        new OutputType("Genomics Test 1", "genomics_test1",
                       OutputType.TYPE_VIEW, "", "/genomics/dna.png");

    /** _more_ */
    public static final OutputType OUTPUT_GENOMICS_TEST2 =
        new OutputType("Genomics Test 2", "genomics_test2",
                       OutputType.TYPE_VIEW, "", "/genomics/dna.png");


    /**
     * _more_
     */
    public GenomicsOutputHandler() {}

    /**
     * _more_
     *
     *
     * @param repository _more_
     * @param element _more_
     * @throws Exception _more_
     */
    public GenomicsOutputHandler(Repository repository, Element element)
            throws Exception {
        super(repository, element);
        //        addType(OUTPUT_GENOMICS_TEST1);
        //        addType(OUTPUT_GENOMICS_TEST2);
    }

    /**
     * _more_
     *
     * @param request _more_
     * @param state _more_
     * @param links _more_
     *
     *
     * @throws Exception _more_
     */
    public void getEntryLinks(Request request, State state, List<Link> links)
            throws Exception {
        if (true) {
            return;
        }
        for (Entry entry : state.getAllEntries()) {
            if (entry.getTypeHandler().isType("bio_genomics")) {
                links.add(makeLink(request, state.getEntry(),
                                   OUTPUT_GENOMICS_TEST1));
                links.add(makeLink(request, state.getEntry(),
                                   OUTPUT_GENOMICS_TEST2));

                return;
            }
        }

    }


    /**
     * _more_
     *
     * @param request _more_
     * @param outputType _more_
     * @param entry _more_
     *
     * @return _more_
     *
     * @throws Exception _more_
     */
    @Override
    public Result outputEntry(Request request, OutputType outputType,
                              Entry entry)
            throws Exception {
        List<Entry> entries = new ArrayList<Entry>();
        entries.add(entry);

        return outputEntries(request, outputType, entries);
    }

    /**
     * _more_
     *
     * @param request _more_
     * @param outputType _more_
     * @param group _more_
     * @param subGroups _more_
     * @param entries _more_
     *
     * @return _more_
     *
     * @throws Exception _more_
     */
    public Result outputGroup(Request request, OutputType outputType,
                              Entry group, List<Entry> subGroups,
                              List<Entry> entries)
            throws Exception {
        return outputEntries(request, outputType, entries);
    }


    /**
     * _more_
     *
     * @param request _more_
     * @param outputType _more_
     * @param entries _more_
     *
     * @return _more_
     *
     * @throws Exception _more_
     */
    public Result outputEntries(Request request, OutputType outputType,
                                List<Entry> entries)
            throws Exception {

        StringBuffer sb =
            new StringBuffer("Test output handler for genomics data");
        if (outputType.equals(OUTPUT_GENOMICS_TEST1)) {
            sb.append("Test 1<br>");
        } else if (outputType.equals(OUTPUT_GENOMICS_TEST2)) {
            sb.append("Test 2<br>");
        }
        for (Entry entry : entries) {
            if ( !entry.getTypeHandler().isType("bio_genomics")) {
                continue;
            }
            sb.append("File:" + entry.getName());
            sb.append(HtmlUtils.br());
        }
        Result result = new Result("", sb);

        return result;
    }




}
