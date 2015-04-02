/*
* Copyright 2008-2015 Geode Systems LLC
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

package org.ramadda.plugins.slack;


import org.ramadda.repository.*;
import org.ramadda.repository.auth.*;
import org.ramadda.repository.harvester.*;
import org.ramadda.repository.search.*;
import org.ramadda.repository.type.*;

import org.ramadda.util.HtmlUtils;
import org.ramadda.util.HtmlUtils;

import org.w3c.dom.*;

import ucar.unidata.ui.HttpFormEntry;

import ucar.unidata.util.IOUtil;


import ucar.unidata.util.Misc;
import ucar.unidata.util.StringUtil;
import ucar.unidata.xml.XmlUtil;

import java.io.*;


import java.net.*;

import java.util.ArrayList;

import java.util.Collections;
import java.util.Comparator;
import java.util.Hashtable;
import java.util.List;
import java.util.regex.*;


/**
 * Provides a top-level API
 *
 */
public class SlackApiHandler extends RepositoryManager implements RequestHandler {

    /**
     *     ctor
     *
     *     @param repository the repository
     *     @param node xml from api.xml
     *     @param props propertiesn
     *
     *     @throws Exception on badness
     */
    public SlackApiHandler(Repository repository) throws Exception {
        super(repository);
    }



    /**
     * _more_
     *
     * @return _more_
     */
    public List<SlackHarvester> getHarvesters() {
        List<SlackHarvester> harvesters = new ArrayList<SlackHarvester>();
        for (Harvester harvester : getHarvesterManager().getHarvesters()) {
            if (harvester.getActiveOnStart()
                    && (harvester instanceof SlackHarvester)) {
                harvesters.add((SlackHarvester) harvester);
            }
        }
        return harvesters;
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
    public Result processSlackApi(Request request) throws Exception {
        Result result = null;
        for(SlackHarvester harvester: getHarvesters()) {
            result = harvester.handleRequest(request);
            if(result!=null) {
                break;
            }
            
        }
        if(result == null) {
            result =  new Result("", new StringBuffer("nothing defined"));
        }
        result.setShouldDecorate(false);
        return result;
    }


}
