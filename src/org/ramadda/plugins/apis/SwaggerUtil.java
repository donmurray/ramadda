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


import org.ramadda.util.Json;


import ucar.unidata.util.Misc;

import java.util.ArrayList;
import java.util.List;


/**
 */
public class SwaggerUtil {


    /** _more_ */
    public static final String VERSION_API = "1.0.0";

    /** _more_ */
    public static final String VERSION_SWAGGER = "1.2";

    /** _more_ */
    public static final String ATTR_API_VERSION = "apiVersion";

    /** _more_ */
    public static final String ATTR_SWAGGER_VERSION = "swaggerVersion";


    /** _more_ */
    public static final String ATTR_PATH = "path";

    /** _more_ */
    public static final String ATTR_BASEPATH = "basePath";

    /** _more_ */
    public static final String ATTR_RESOURCEPATH = "resourcePath";

    /** _more_ */
    public static final String ATTR_DESCRIPTION = "description";

    /** _more_ */
    public static final String ATTR_SUMMARY = "summary";

    /** _more_ */
    public static final String ATTR_AUTHORIZATIONS = "authorizations";

    /** _more_ */
    public static final String ATTR_NOTES = "notes";

    /** _more_ */
    public static final String ATTR_APIS = "apis";

    /** _more_ */
    public static final String ATTR_OPERATIONS = "operations";

    /** _more_ */
    public static final String ATTR_METHOD = "method";

    /** _more_ */
    public static final String ATTR_NICKNAME = "nickname";

    /** _more_ */
    public static final String ATTR_PARAMETERS = "parameters";

    /** _more_ */
    public static final String ATTR_ = "";


    /** _more_ */
    public static final String ATTR_NAME = "name";

    /** _more_ */
    public static final String ATTR_REQUIRED = "required";

    /** _more_ */
    public static final String ATTR_TYPE = "type";

    /** _more_ */
    public static final String ATTR_PARAMTYPE = "paramType";

    /** _more_ */
    public static final String ATTR_RESPONSEMESSAGES = "responseMessages";

    /** _more_ */
    public static final String ATTR_CODE = "code";

    /** _more_ */
    public static final String ATTR_MESSAGE = "message";

    /** _more_ */
    public static final String ATTR_DEPRECATED = "deprecated";

    /** _more_ */
    public static final String ATTR_PRODUCES = "produces";


    /** _more_ */
    public static final String TYPE_INTEGER = "integer";

    /** _more_ */
    public static final String TYPE_LONG = "long";

    /** _more_ */
    public static final String TYPE_FLOAT = "float";

    /** _more_ */
    public static final String TYPE_DOUBLE = "double";

    /** _more_ */
    public static final String TYPE_STRING = "string";

    /** _more_ */
    public static final String TYPE_BYTE = "byte";

    /** _more_ */
    public static final String TYPE_BOOLEAN = "boolean";

    /** _more_ */
    public static final String TYPE_DATE = "date";

    /** _more_ */
    public static final String TYPE_DATETIME = "dateTime";



    /**
     * _more_
     *
     * @param name _more_
     * @param description _more_
     *
     * @return _more_
     */
    public static String getParameter(String name, String description) {
        return getParameter(name, description, false);
    }



    /**
     * _more_
     *
     * @param mapItems _more_
     */
    public static void initVersionItems(List<String> mapItems) {
        mapItems.add(ATTR_API_VERSION);
        mapItems.add(Json.quote(VERSION_API));
        mapItems.add(ATTR_SWAGGER_VERSION);
        mapItems.add(Json.quote(VERSION_SWAGGER));
    }


    /**
     * _more_
     *
     * @param basePath _more_
     * @param resourcePath _more_
     * @param produces _more_
     * @param apis _more_
     *
     * @return _more_
     */
    public static List<String> createDocument(String basePath,
            String resourcePath, String[] produces, List<String> apis) {
        List<String> doc = new ArrayList<String>();
        doc.add(ATTR_BASEPATH);
        doc.add(Json.quote(basePath));
        doc.add(ATTR_RESOURCEPATH);
        doc.add(Json.quote(resourcePath));
        doc.add(ATTR_PRODUCES);
        doc.add(Json.list(Misc.toList(produces), true));
        doc.add(ATTR_APIS);
        doc.add(Json.list(apis));

        return doc;
    }


    /**
     * _more_
     *
     * @param path _more_
     * @param operations _more_
     *
     * @return _more_
     */
    public static List<String> createApi(String path,
                                         List<String> operations) {
        List<String> api = new ArrayList<String>();
        api.add(ATTR_PATH);
        api.add(Json.quote(path));
        api.add(ATTR_OPERATIONS);
        api.add(Json.list(operations));

        return api;
    }





    /**
     * _more_
     *
     * @param summary _more_
     * @param notes _more_
     * @param nickname _more_
     * @param parameters _more_
     * @param responseMessages _more_
     *
     * @return _more_
     */
    public static List<String> createOperation(String summary, String notes,
            String nickname, List<String> parameters,
            List<String> responseMessages) {
        List<String> operation = new ArrayList<String>();
        initOperation(operation, summary, notes, nickname);
        operation.add(ATTR_RESPONSEMESSAGES);
        operation.add(Json.list(responseMessages));
        operation.add(ATTR_PARAMETERS);
        operation.add(Json.list(parameters));


        return operation;
    }



    /**
     * _more_
     *
     * @param operation _more_
     * @param summary _more_
     * @param notes _more_
     * @param nickname _more_
     *
     * @return _more_
     */
    public static List<String> initOperation(List<String> operation,
                                             String summary, String notes,
                                             String nickname) {
        operation.add(ATTR_METHOD);
        operation.add(Json.quote("GET"));
        operation.add(ATTR_SUMMARY);
        operation.add(Json.quote(summary));
        operation.add(ATTR_NOTES);
        operation.add(Json.quote(notes));
        operation.add(ATTR_NICKNAME);
        operation.add(Json.quote(nickname));

        //Add dummy auths
        operation.add(ATTR_AUTHORIZATIONS);
        operation.add(Json.map());

        return operation;
    }




    /**
     * _more_
     *
     * @param name _more_
     * @param description _more_
     * @param required _more_
     *
     * @return _more_
     */
    public static String getParameter(String name, String description,
                                      boolean required) {
        return getParameter(name, description, required, TYPE_STRING);
    }

    /**
     * _more_
     *
     * @param name _more_
     * @param description _more_
     * @param required _more_
     * @param type _more_
     *
     * @return _more_
     */
    public static String getParameter(String name, String description,
                                      boolean required, String type) {
        return Json.map(ATTR_NAME, Json.quote(name), ATTR_DESCRIPTION,
                        Json.quote(description), ATTR_REQUIRED,
                        "" + required, ATTR_TYPE, Json.quote(type),
                        ATTR_PARAMTYPE, Json.quote("query"));
    }


}
