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

package org.ramadda.plugins.swagger;


import org.ramadda.repository.*;
import org.ramadda.repository.type.*;
import org.ramadda.repository.util.DateArgument;
import org.ramadda.util.HtmlUtils;
import org.ramadda.util.Json;
import org.ramadda.util.Utils;

import org.w3c.dom.Element;

import ucar.unidata.util.StringUtil;

import java.util.ArrayList;
import java.util.Hashtable;
import java.util.List;




/**
 */
public class SwaggerApiHandler extends RepositoryManager implements RequestHandler {


    /** _more_          */
    private static final SwaggerUtil SU = null;

    /** _more_          */
    public static final String BASE_PATH = "/swagger/api-docs";


    /**
     * _more_
     *
     * @param repository _more_
     * @param node _more_
     * @param props _more_
     *
     * @throws Exception _more_
     */
    public SwaggerApiHandler(Repository repository, Element node,
                             Hashtable props)
            throws Exception {
        super(repository);
    }


    /**
     * _more_
     *
     * @param mapItems _more_
     */
    private void initVersionItems(List<String> mapItems) {
        mapItems.add(SU.ATTR_API_VERSION);
        mapItems.add(Json.quote(SU.VERSION_API));
        mapItems.add(SU.ATTR_SWAGGER_VERSION);
        mapItems.add(Json.quote(SU.VERSION_SWAGGER));
    }

    /**
     * _more_
     *
     * @param request _more_
     * @param json _more_
     *
     * @return _more_
     *
     * @throws Exception _more_
     */
    private Result returnJson(Request request, StringBuffer json)
            throws Exception {
        //        request.setResultFilename("ramaddaswagger.json");
        Result result = new Result("", json, Json.MIMETYPE);
        result.addHttpHeader("Access-Control-Allow-Methods",
                             "POST, GET, OPTIONS , PUT");
        result.addHttpHeader("Access-Control-Allow-Origin", "*");

        return result;
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
    public Result processApiRequest(Request request) throws Exception {
        List<String> mapItems = new ArrayList<String>();
        initVersionItems(mapItems);

        List<String> apis = new ArrayList<String>();
        int          cnt  = 0;
        for (TypeHandler typeHandler : getRepository().getTypeHandlers()) {
            if ( !typeHandler.getForUser()) {
                continue;
            }
            int entryCnt = getEntryUtil().getEntryCount(typeHandler);

            //Only show the types we have ??
            if(entryCnt == 0) {
                continue;
            }
            String url = "/type/" + typeHandler.getType();
            String api = Json.map(SU.ATTR_PATH, Json.quote(url),
                                  SU.ATTR_DESCRIPTION,
                                  Json.quote("Search for "
                                             + typeHandler.getLabel()));
            apis.add(api);
        }
        mapItems.add(SU.ATTR_APIS);
        mapItems.add(Json.list(apis));
        StringBuffer sb = new StringBuffer();
        sb.append(Json.map(mapItems));

        return returnJson(request, sb);
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
    public Result processTypeRequest(Request request) throws Exception {
        List<String> toks = StringUtil.split(request.getRequestPath(), "/",
                                             true, true);
        String       type        = toks.get(toks.size() - 1);
        TypeHandler  typeHandler = getRepository().getTypeHandler(type);

        List<String> mapItems    = new ArrayList<String>();
        initVersionItems(mapItems);

        mapItems.add(SU.ATTR_BASEPATH);
        mapItems.add(Json.quote(request.getAbsoluteUrl("")));
        mapItems.add(SU.ATTR_RESOURCEPATH);
        mapItems.add(Json.quote(getRepository().getUrlBase() +"/search/type/" + type));
        mapItems.add(SU.ATTR_PRODUCES);
        mapItems.add(Json.map(new String[] { "application/json",
                                             "application/xml", "text/plain",
                                             "text/html" }, true));


        //         "path":"/pet/findByTags",
        List<String> apis = new ArrayList<String>();
        apis.add(getSearchApi(request, typeHandler));
        mapItems.add(SU.ATTR_APIS);
        mapItems.add(Json.list(apis));


        StringBuffer sb = new StringBuffer();
        sb.append(Json.map(mapItems));

        return returnJson(request, sb);
    }

    /**
     * _more_
     *
     * @param request _more_
     * @param typeHandler _more_
     *
     * @return _more_
     *
     * @throws Exception _more_
     */
    private String getSearchApi(Request request, TypeHandler typeHandler)
            throws Exception {

        List<String> api              = new ArrayList<String>();
        List<String> operations       = new ArrayList<String>();

        List<String> operation        = new ArrayList<String>();
        List<String> parameters       = new ArrayList<String>();
        List<String> responseMessages = new ArrayList<String>();


        addBaseSearchParameters(request, parameters);


        operation.add(SU.ATTR_METHOD);
        operation.add(Json.quote("GET"));
        operation.add(SU.ATTR_SUMMARY);
        operation.add(Json.quote("Search for " + typeHandler.getLabel()));
        operation.add(SU.ATTR_NOTES);
        operation.add(Json.quote("API to search for entries of type "
                                 + typeHandler.getLabel()));
        operation.add(SU.ATTR_AUTHORIZATIONS);
        operation.add(Json.map());
        operation.add(SU.ATTR_NICKNAME);
        operation.add(Json.quote("search_" + typeHandler.getType()));

        List<Column> columns = typeHandler.getColumns();
        if (columns != null) {
            for (Column column : columns) {
                if ( !column.getCanSearch()) {
                    continue;
                }
                String type = SU.TYPE_STRING;
                if (column.isEnumeration()) {
                    //TODO: list the enums
                } else if (column.isBoolean()) {
                    type = SU.TYPE_BOOLEAN;
                } else if (column.isDouble()) {
                    type = SU.TYPE_DOUBLE;
                } else if (column.isNumeric()) {
                    type = SU.TYPE_INTEGER;
                }
                parameters.add(SU.getParameter(column.getSearchArg(),
                        column.getLabel(), false, type));
            }
        }


        operation.add(SU.ATTR_PARAMETERS);
        operation.add(Json.list(parameters));


        operation.add(SU.ATTR_RESPONSEMESSAGES);
        operation.add(Json.list(responseMessages));

        operations.add(Json.map(operation));


        api.add(SU.ATTR_PATH);
        api.add(Json.quote(getRepository().getUrlBase() +"/search/type/" + typeHandler.getType()));
        api.add(SU.ATTR_OPERATIONS);
        api.add(Json.list(operations));

        return Json.map(api);
    }

    /**
     * _more_
     *
     * @param request _more_
     * @param parameters _more_
     *
     * @throws Exception _more_
     */
    private void addBaseSearchParameters(Request request,
                                         List<String> parameters)
            throws Exception {
        parameters.add(SU.getParameter(ARG_TEXT, "Search text"));
        parameters.add(SU.getParameter(ARG_NAME, "Search name"));
        parameters.add(SU.getParameter(ARG_DESCRIPTION,
                                       "Search description"));

        parameters.add(SU.getParameter(ARG_FROMDATE, "From date", false,
                                       SU.TYPE_DATETIME));
        parameters.add(SU.getParameter(ARG_TODATE, "To date", false,
                                       SU.TYPE_DATETIME));

        parameters.add(SU.getParameter(DateArgument.ARG_CREATE.getFromArg(),
                                       "Archive create date from", false,
                                       SU.TYPE_DATETIME));
        parameters.add(SU.getParameter(DateArgument.ARG_CREATE.getToArg(),
                                       "Archive create date to", false,
                                       SU.TYPE_DATETIME));

        parameters.add(SU.getParameter(DateArgument.ARG_CHANGE.getFromArg(),
                                       "Archive change date from", false,
                                       SU.TYPE_DATETIME));
        parameters.add(SU.getParameter(DateArgument.ARG_CHANGE.getToArg(),
                                       "Archive change date to", false,
                                       SU.TYPE_DATETIME));


        parameters.add(SU.getParameter(ARG_GROUP, "Parent entry"));
        parameters.add(SU.getParameter(ARG_FILESUFFIX, "File suffix"));


        parameters.add(SU.getParameter(ARG_AREA_NORTH,
                                       "Northern bounds of search"));
        parameters.add(SU.getParameter(ARG_AREA_WEST,
                                       "Western bounds of search"));
        parameters.add(SU.getParameter(ARG_AREA_SOUTH,
                                       "Southern bounds of search"));
        parameters.add(SU.getParameter(ARG_AREA_EAST,
                                       "Eastern bounds of search"));

        parameters.add(SU.getParameter(ARG_MAX, "Max number of results",
                                       false, SU.TYPE_INTEGER));
        parameters.add(SU.getParameter(ARG_SKIP, "Number to skip", false,
                                       SU.TYPE_INTEGER));
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
    public Result processUIRequest(Request request) throws Exception {
        StringBuffer sb = new StringBuffer();

        return returnJson(request, sb);
    }



}
