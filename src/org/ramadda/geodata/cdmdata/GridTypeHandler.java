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

package org.ramadda.geodata.cdmdata;



import org.ramadda.repository.*;
import org.ramadda.repository.output.*;
import org.ramadda.repository.output.WikiConstants;
import org.ramadda.repository.type.*;
import org.ramadda.util.HtmlUtils;


import org.w3c.dom.*;

import ucar.unidata.util.DateUtil;

import ucar.unidata.util.IOUtil;
import ucar.unidata.util.Misc;
import ucar.unidata.util.PatternFileFilter;


import ucar.unidata.util.StringUtil;
import ucar.unidata.util.TwoFacedObject;
import ucar.unidata.xml.XmlUtil;

import java.awt.geom.Rectangle2D;

import java.io.File;


import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.Enumeration;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.List;


/**
 *
 */

public class GridTypeHandler extends TypeHandler {


    /**
     * Construct a new GridAggregationTypeHandler
     *
     * @param repository   the Repository
     * @param node         the defining Element
     * @throws Exception   problems
     */
    public GridTypeHandler(Repository repository, Element node)
            throws Exception {
        super(repository, node);
    }


    /**
     * _more_
     *
     * @param request _more_
     * @param entry _more_
     * @param tag _more_
     *
     * @return _more_
     */
    @Override
    public String getUrlForWiki(Request request, Entry entry, String tag) {
        if (tag.equals(WikiConstants.WIKI_TAG_CHART)
                || tag.equals(WikiConstants.WIKI_TAG_DISPLAY)) {
            StringBuilder jsonbuf = new StringBuilder();
            jsonbuf.append(getRepository().getUrlBase() + "/grid/json?"
                           + HtmlUtils.args(new String[] {
                ARG_ENTRYID, entry.getId(), ARG_LOCATION_LATITUDE,
                "${latitude}", ARG_LOCATION_LONGITUDE, "${longitude}"
            }, false));
            // add in the list of selected variables as well
            String    VAR_PREFIX = Constants.ARG_VARIABLE + ".";
            Hashtable args       = request.getArgs();
            for (Enumeration keys = args.keys(); keys.hasMoreElements(); ) {
                String arg = (String) keys.nextElement();
                if (arg.startsWith(VAR_PREFIX) && request.get(arg, false)) {
                    jsonbuf.append("&");
                    jsonbuf.append(VAR_PREFIX);
                    jsonbuf.append(arg.substring(VAR_PREFIX.length()));
                    jsonbuf.append("=true");
                }
            }


            if (request.defined(ARG_FROMDATE)) {
                jsonbuf.append("&");
                jsonbuf.append(
                    HtmlUtils.arg(
                        ARG_FROMDATE, request.getString(ARG_FROMDATE)));
            }

            if (request.defined(ARG_TODATE)) {
                jsonbuf.append("&");
                jsonbuf.append(HtmlUtils.arg(ARG_TODATE,
                                             request.getString(ARG_TODATE)));
            }


            return jsonbuf.toString();
        }

        return super.getUrlForWiki(request, entry, tag);
    }





}
