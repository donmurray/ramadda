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

import org.w3c.dom.*;

import ucar.unidata.util.IOUtil;


import java.util.Hashtable;
import java.util.List;


/**
 * Provides a top-level API /repository/opendap/<entry path>/entry.das
 *
 */
public class OpendapApiHandler extends RepositoryManager implements RequestHandler {

    /** My id. defined in resources/opendapapi.xml */
    public static final String API_ID = "opendap";

    /** Top-level path element */
    public static final String PATH_OPENDAP = "opendap";


    /** opendap suffix to use. The dodsC is from the TDS paths. The IDV uses it to recognize opendap grids */
    public static final String OPENDAP_SUFFIX = "entry.das";



    /** the output handler to pass opendap calls to */
    private CdmDataOutputHandler dataOutputHandler;

    /** _more_ */
    private GridPointOutputHandler gridPointOutputHandler;

    /** _more_ */
    private static boolean useApi = true;

    /**
     * ctor
     *
     * @param repository the repository
     * @param node xml from api.xml
     * @param props propertiesn
     *
     * @throws Exception on badness
     */
    public OpendapApiHandler(Repository repository, Element node,
                             Hashtable props)
            throws Exception {
        super(repository);
    }


    /**
     * _more_
     *
     * @param request _more_
     * @param entry _more_
     *
     * @return _more_
     */
    public String getAbsoluteOpendapUrl(Request request, Entry entry) {
        return request.getAbsoluteUrl(getOpendapUrl(entry));
    }

    /**
     * makes the opendap url for the entry
     *
     * @param entry the entry
     *
     * @return opendap url
     */
    public String getOpendapUrl(Entry entry) {
        return getOpendapPrefix(entry) + getOpendapSuffix(entry);
    }


    /**
     * _more_
     *
     * @param entry _more_
     *
     * @return _more_
     */
    public String getOpendapPrefix(Entry entry) {
        if (useApi) {
            return getRepository().getUrlBase() + "/" + PATH_OPENDAP;
        }

        return getRepository().URL_ENTRY_SHOW.toString();
    }


    /**
     * _more_
     *
     * @param entry _more_
     *
     * @return _more_
     */
    public String getOpendapSuffix(Entry entry) {
        String url;
        //Always use the full /entry/show/... url
        //        if(getEntryManager().isSynthEntry(entry.getId())) {
        if (useApi) {
            //entry.getFullName?
            //            url = getRepository().getUrlBase() + "/" + PATH_OPENDAP + "/"
            //                  + entry.getId() + "/" + OPENDAP_SUFFIX;
            url = "/" + entry.getId() + "/" + OPENDAP_SUFFIX;
        } else {
            url = "/" + ARG_OUTPUT + ":"
                  + Request.encodeEmbedded(
                      CdmDataOutputHandler.OUTPUT_OPENDAP) + "/"
                          + ARG_ENTRYID + ":"
                          + Request.encodeEmbedded(entry.getId()) + "/"
                          + getStorageManager().getFileTail(entry) + "/"
                          + OPENDAP_SUFFIX;
        }

        url = url.replaceAll(" ", "+");

        return url;
    }


    /**
     * _more_
     *
     * @return _more_
     *
     * @throws Exception _more_
     */
    private CdmDataOutputHandler getDataOutputHandler() throws Exception {
        if (dataOutputHandler == null) {
            dataOutputHandler =
                (CdmDataOutputHandler) getRepository().getOutputHandler(
                    CdmDataOutputHandler.OUTPUT_OPENDAP);
        }

        return dataOutputHandler;
    }

    /**
     * _more_
     *
     * @return _more_
     *
     * @throws Exception _more_
     */
    private GridPointOutputHandler getGridPointOutputHandler()
            throws Exception {
        if (gridPointOutputHandler == null) {
            gridPointOutputHandler =
                (GridPointOutputHandler) getRepository().getOutputHandler(
                    GridPointOutputHandler.class);
        }

        return gridPointOutputHandler;
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
    public Result processOpendapRequest(Request request) throws Exception {
        String prefix = getRepository().getUrlBase() + "/" + PATH_OPENDAP;
        Entry entry =
            getDataOutputHandler().getCdmManager().findEntryFromPath(request,
                prefix);

        return getDataOutputHandler().outputOpendap(request, entry);
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
        return getGridPointOutputHandler().processJsonRequest(request);
    }

}
