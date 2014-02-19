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

import org.ramadda.util.HtmlUtils;
import org.ramadda.util.WfsUtil;

import org.w3c.dom.Element;

import ucar.unidata.util.StringUtil;
import ucar.unidata.xml.XmlUtil;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.List;


/**
 * Implements a OGC WFS endpoint for search
 *
 */

public class WfsApiHandler extends RepositoryManager {

    /**
     * ctor
     *
     * @param repository the main ramadda repository
     * @param node xml node from nlasapi.xml
     * @param props extra properties
     *
     * @throws Exception On badness
     */
    public WfsApiHandler(Repository repository, Element node, Hashtable props)
            throws Exception {
        super(repository);
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
    public Result processWfsRequest(Request request) throws Exception {
        //        /ogc/wfs/<type>
        String wfsRequest = request.getString(WfsUtil.ARG_REQUEST,
                                WfsUtil.REQUEST_GETCAPABILITIES);
        if (wfsRequest.equals(WfsUtil.REQUEST_GETCAPABILITIES)) {
            return processGetCapabilitiesRequest(request);
        } else if (wfsRequest.equals(WfsUtil.REQUEST_DESCRIBEFEATURETYPE)) {
            return processDescribeFeatureTypeRequest(request);
        } else {
            return handleError(request, "Unknown request:" + wfsRequest);
        }
    }

    /**
     * _more_
     *
     * @param request _more_
     * @param message _more_
     *
     * @return _more_
     *
     * @throws Exception _more_
     */
    public Result handleError(Request request, String message)
            throws Exception {
        request.setReturnFilename("error.xml");
        StringBuffer xml = new StringBuffer();

        return new Result("", xml, "application/xml");
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
    public Result processGetCapabilitiesRequest(Request request)
            throws Exception {
        request.setReturnFilename("wfs.xml");
        String xml = getRepository().getResource(
                         "/org/ramadda/geodata/ogc/capabilities.xml");
        xml = xml.replaceAll("${wfs.onlineresource}",
                             getRepository().getUrlBase() + "/ogc/wfs");

        return new Result("", new StringBuffer(xml), "application/xml");
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
    public Result processDescribeFeatureTypeRequest(Request request)
            throws Exception {
        request.setReturnFilename("wfs.xml");
        StringBuffer xml = new StringBuffer();

        return new Result("", xml, "application/xml");
    }

}
