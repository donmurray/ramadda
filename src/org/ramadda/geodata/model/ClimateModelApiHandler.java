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

package org.ramadda.geodata.model;


import org.ramadda.repository.*;
import org.ramadda.repository.auth.*;
import org.ramadda.repository.search.*;
import org.ramadda.repository.type.*;

import org.ramadda.util.HtmlUtils;
import org.ramadda.util.HtmlUtils;

import org.w3c.dom.*;

import ucar.unidata.util.IOUtil;

import ucar.unidata.util.Misc;
import ucar.unidata.util.StringUtil;


import java.io.*;


import java.util.ArrayList;
import java.util.List;
import java.util.Hashtable;

import java.util.regex.*;


/**
 * Provides a top-level API
 *
 */
public class ClimateModelApiHandler extends RepositoryManager implements RequestHandler {

    private String collectionType;


    /**
     * ctor
     *
     * @param repository the repository
     * @param node xml from api.xml
     * @param props properties
     *
     * @throws Exception on badness
     */

    public ClimateModelApiHandler(Repository repository, Element node, Hashtable props)
            throws Exception {
        super(repository);
        collectionType  = Misc.getProperty(props, "collectiontype", "climate_collection");
    }


    /**
     * handle the request
     *
     * @param request request
     *
     * @return result
     *
     * @throws Exception on badness
     */
    public Result processClimateModelRequest(Request request) throws Exception {

        TypeHandler collectionTypeHandler  =getRepository().getTypeHandler(collectionType);
        if(collectionTypeHandler == null) {
            throw new IllegalStateException("Unknown collection type:" +collectionType);
        }

        StringBuffer sb = new StringBuffer();
        
        List<Entry> collections =  getCollections(request);
        sb.append("<ul>");
        for(Entry collection: collections) {
            sb.append("<li>" + getEntryManager().getEntryLink(request,collection));
        }
        sb.append("</ul>");
        if(collections.size()==0) {
            sb.append(getPageHandler().showDialogWarning(msg("No climate collections found")));
        }
        return new Result("Climate Model Analysis", sb);
    }




    private List<Entry> getCollections(Request request) throws Exception {
        Request tmpRequest = new  Request(getRepository(), request.getUser());
      
        tmpRequest.put(ARG_TYPE, collectionType);

        List<Entry> collections = (List<Entry>) getEntryManager().getEntries(tmpRequest)[0];
        return collections;
    }


}
