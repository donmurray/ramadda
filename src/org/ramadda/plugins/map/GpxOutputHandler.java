/*
* Copyright 2008-2012 Jeff McWhirter/ramadda.org
*                     Don Murray/CU-CIRES
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

package org.ramadda.plugins.map;


import org.ramadda.repository.*;
import org.ramadda.repository.output.*;
import org.w3c.dom.*;

import ucar.unidata.xml.XmlUtil;
import ucar.unidata.util.DateUtil;
import ucar.unidata.util.IOUtil;
import ucar.unidata.util.HtmlUtil;


import java.text.SimpleDateFormat;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;


/**
 *
 *
 */
public class GpxOutputHandler extends OutputHandler {


    /** Map output type */
    public static final OutputType OUTPUT_GPX =
        new OutputType("GPS GPX File", "gpx",
                       OutputType.TYPE_FEEDS | OutputType.TYPE_FORSEARCH, "",
                       ICON_MAP);


    /**
     * Create a MapOutputHandler
     *
     *
     * @param repository  the repository
     * @param element     the Element
     * @throws Exception  problem generating handler
     */
    public GpxOutputHandler(Repository repository, Element element)
            throws Exception {
        super(repository, element);
        addType(OUTPUT_GPX);
    }



    /**
     * Get the entry links
     *
     * @param request  the Request
     * @param state    the repository State
     * @param links    the links
     *
     * @throws Exception  problem creating links
     */
    public void getEntryLinks(Request request, State state, List<Link> links)
            throws Exception {
        for (Entry entry : state.getAllEntries()) {
            if (entry.hasLocationDefined() || entry.hasAreaDefined()) {
                links.add(makeLink(request, state.getEntry(), OUTPUT_GPX));
                break;
            }
        }
    }


    /**
     * Output the entry
     *
     * @param request      the Request
     * @param outputType   the type of output
     * @param entry        the Entry to output
     *
     * @return  the Result
     *
     * @throws Exception  problem outputting entry
     */
    public Result outputEntry(Request request, OutputType outputType,
                              Entry entry)
            throws Exception {
        List<Entry> entriesToUse = new ArrayList<Entry>();
        entriesToUse.add(entry);
        return outputGpx(request, entry, entriesToUse);
    }


    /**
     * Output a group
     *
     * @param request      The Request
     * @param outputType   the type of output
     * @param group        the group Entry
     * @param subGroups    the subgroups
     * @param entries      The list of Entrys
     *
     * @return  the resule
     *
     * @throws Exception    problem on output
     */
    public Result outputGroup(Request request, OutputType outputType,
                              Entry group, List<Entry> subGroups,
                              List<Entry> entries)
            throws Exception {
        List<Entry> entriesToUse = new ArrayList<Entry>(subGroups);
        entriesToUse.addAll(entries);

        return outputGpx(request, group, entriesToUse);
    }

    public Result outputGpx(Request request, Entry entry,
                            List<Entry> entries)
            throws Exception {
        StringBuffer sb  = new StringBuffer();
        sb.append(XmlUtil.openTag(GpxUtil.TAG_GPX,XmlUtil.attrs(new String[]{
                    GpxUtil.ATTR_VERSION,"1.1",
                    GpxUtil.ATTR_CREATOR,"RAMADDA"
                    })));

        for (Entry child : entries) {
            if (!(child.hasLocationDefined() || child.hasAreaDefined())) {
                continue;
            }
            if(child.hasAreaDefined()) {
            } else {
                sb.append(XmlUtil.tag(GpxUtil.TAG_WPT, XmlUtil.attrs(new String[]{GpxUtil.ATTR_LAT, ""+child.getLatitude(),
                                                                                 GpxUtil.ATTR_LON,""+ child.getLongitude()})));
            }
        }

        Result result = new Result("", sb, "application/gpx+xml");
        result.setReturnFilename(IOUtil.stripExtension(entry.getName())+".gpx");
        return result;
    }



}
