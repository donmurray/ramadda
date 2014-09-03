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

package org.ramadda.geodata.ogc;


import org.ramadda.repository.*;
import org.ramadda.repository.output.*;

import org.ramadda.util.CswUtil;
import org.ramadda.util.HtmlUtils;


import org.w3c.dom.*;

import ucar.unidata.data.GeoLocationInfo;
import ucar.unidata.xml.XmlUtil;


import java.util.ArrayList;
import java.util.Hashtable;
import java.util.List;


/**
 */
public class CswOutputHandler extends OutputHandler {

    /** output type for viewing map */
    public static final OutputType OUTPUT_CSW = new OutputType("CSW", "csw",
                                                    OutputType.TYPE_FEEDS,
                                                    "", "/icons/globe.jpg");

    /**
     * Constructor
     *
     * @param repository The repository
     * @param element The xml element from outputhandlers.xml
     * @throws Exception On badness
     */
    public CswOutputHandler(Repository repository, Element element)
            throws Exception {
        super(repository, element);
        //add in the output types
        addType(OUTPUT_CSW);
    }



    /**
     * This method gets called to add to the types list the OutputTypes that are applicable
     * to the given State.  The State can be viewing a single Entry (state.entry non-null),
     * viewing a Group (state.group non-null).
     *
     * @param request The request
     * @param state The state
     * @param links _more_
     *
     *
     * @throws Exception On badness
     */
    @Override
    public void getEntryLinks(Request request, State state, List<Link> links)
            throws Exception {
        if (state.getEntry() != null) {
            links.add(makeLink(request, state.getEntry(), OUTPUT_CSW));
        }
    }



    /**
     * Output the html for the given entry
     *
     * @param request the request
     * @param outputType type of output
     * @param entry the entry
     *
     * @return the result
     *
     * @throws Exception on badness
     */
    @Override
    public Result outputEntry(Request request, OutputType outputType,
                              Entry entry)
            throws Exception {
        StringBuffer sb = new StringBuffer();

        return new Result("", sb);
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
    @Override
    public Result outputGroup(Request request, OutputType outputType,
                              Entry group, List<Entry> subGroups,
                              List<Entry> entries)
            throws Exception {
        StringBuffer sb = new StringBuffer();

        return new Result("", sb);

    }

}
