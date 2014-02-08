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

package org.ramadda.geodata.point;


import org.ramadda.data.record.*;
import org.ramadda.data.services.*;
import org.ramadda.repository.*;
import org.ramadda.util.HtmlUtils;

import org.w3c.dom.Element;

import ucar.unidata.util.StringUtil;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.List;


/**
 *
 * @author Jeff McWhirter
 */

public class PointApiHandler extends RepositoryManager {


    /**
     * ctor
     *
     * @param repository the main ramadda repository
     * @param node xml node from nlasapi.xml
     * @param props extra properties
     *
     * @throws Exception On badness
     */
    public PointApiHandler(Repository repository, Element node,
                           Hashtable props)
            throws Exception {
        super(repository);
    }


    /**
     * _more_
     *
     * @return _more_
     */
    public PointOutputHandler getPointOutputHandler() {
        return (PointOutputHandler) getRepository().getOutputHandler(
            PointOutputHandler.class);
    }



    /**
     * _more_
     *
     * @param request _more_
     *
     * @return _more_
     *
     * @throws Exception _more_
     */
    public Result processJsonRequest(Request request) throws Exception {
        PointOutputHandler poh   = getPointOutputHandler();
        Entry              entry = getEntryManager().getEntry(request);
        request.getHttpServletResponse().setContentType("test/json");
        //Set a default of 500 for num points
        request.put(RecordFormHandler.ARG_NUMPOINTS, request.getString(RecordFormHandler.ARG_NUMPOINTS, "500"));

        request.put(ARG_OUTPUT, poh.OUTPUT_PRODUCT.getId());
        request.put(ARG_PRODUCT, poh.OUTPUT_JSON.toString());
        request.put(RecordConstants.ARG_ASYNCH, "false");
        request.setReturnFilename(entry.getName() + ".json");

        return poh.outputEntry(request, poh.OUTPUT_PRODUCT, entry);
    }


    public Result processDataRequest(Request request) throws Exception {
        PointOutputHandler poh   = getPointOutputHandler();
        Entry              entry = getEntryManager().getEntry(request);
        request.put(ARG_OUTPUT, poh.OUTPUT_PRODUCT.getId());
        request.put(ARG_PRODUCT, poh.OUTPUT_JSON.toString());
        request.put(RecordConstants.ARG_ASYNCH, "false");
        return poh.outputEntry(request, poh.OUTPUT_PRODUCT, entry);
    }

}
