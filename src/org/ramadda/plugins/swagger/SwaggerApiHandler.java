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

package org.ramadda.plugins.swagger;


import org.ramadda.repository.*;
import org.ramadda.repository.type.*;
import org.ramadda.util.Json;
import org.ramadda.util.HtmlUtils;
import org.ramadda.util.Utils;
import org.w3c.dom.Element;
import ucar.unidata.util.StringUtil;
import java.util.ArrayList;
import java.util.List;
import java.util.Hashtable;




/**
 */
public class SwaggerApiHandler extends RepositoryManager implements RequestHandler {


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
     * @param request _more_
     *
     * @return _more_
     *
     * @throws Exception _more_
     */
    public Result processApiRequest(Request request) throws Exception {
        StringBuffer sb = new StringBuffer();

        List<String> mapItems = new ArrayList<String>();

        mapItems.add(SwaggerUtil.ATTR_API_VERSION);
        mapItems.add(Json.quote(SwaggerUtil.VERSION_API));

        mapItems.add(SwaggerUtil.ATTR_SWAGGER_VERSION);
        mapItems.add(Json.quote(SwaggerUtil.VERSION_SWAGGER));
        List<String> paths = new ArrayList<String>();
        for (TypeHandler typeHandler : getRepository().getTypeHandlers()) {
            if ( !typeHandler.getForUser()) {
                continue;
            }
            String url = getRepository().getUrlBase() +"/swagger/type/" + typeHandler.getType();
            paths.add(Json.map(SwaggerUtil.ATTR_PATH, url,
                               SwaggerUtil.ATTR_DESCRIPTION,
                               "Search for " + typeHandler.getLabel()));
        }
        mapItems.add(SwaggerUtil.ATTR_APIS);
        mapItems.add(Json.list(paths));
        sb.append(Json.map(mapItems));
        return new Result("", sb,Json.MIMETYPE);
    }

    public Result processTypeRequest(Request request) throws Exception {
        StringBuffer sb = new StringBuffer();

        return new Result("", sb,Json.MIMETYPE);
    }

}
