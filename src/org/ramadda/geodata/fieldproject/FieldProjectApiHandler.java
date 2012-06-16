/*
* Copyright 2008-2011 Jeff McWhirter/ramadda.org
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

package org.ramadda.geodata.fieldproject;


import org.ramadda.repository.*;
import org.ramadda.repository.auth.*;
import org.ramadda.repository.search.*;
import org.ramadda.repository.type.*;

import org.w3c.dom.*;

import org.ramadda.util.HtmlUtils;
import org.ramadda.util.HtmlUtils;
import ucar.unidata.util.IOUtil;

import ucar.unidata.util.Misc;
import ucar.unidata.util.StringUtil;


import java.io.*;

import java.util.Hashtable;
import java.util.regex.*;


/**
 * Provides a top-level API
 *
 */
public class FieldProjectApiHandler extends SpecialSearch implements RequestHandler {



    /**
     * ctor
     *
     * @param repository the repository
     * @param node xml from api.xml
     * @param props propertiesn
     *
     * @throws Exception on badness
     */
    public FieldProjectApiHandler(Repository repository, Element node,
                                  Hashtable props)
            throws Exception {
        super(repository, node, props);
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
    public Result processAddOpus(Request request) throws Exception {
        GpsOutputHandler gpsOutputHandler =
            (GpsOutputHandler) getRepository().getOutputHandler(
                GpsOutputHandler.OUTPUT_GPS_TORINEX);
        return gpsOutputHandler.processAddOpus(request);
    }

}
