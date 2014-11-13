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

package org.ramadda.plugins.media;


import org.ramadda.repository.*;
import org.ramadda.repository.metadata.*;
import org.ramadda.repository.type.*;
import org.ramadda.util.HtmlUtils;

import org.ramadda.util.Json;
import org.ramadda.util.XlsUtil;


import org.w3c.dom.*;

import ucar.unidata.util.IOUtil;

import ucar.unidata.util.Misc;
import ucar.unidata.util.StringUtil;


import ucar.unidata.xml.XmlUtil;

import java.io.File;
import java.io.InputStream;
import java.io.OutputStream;

import java.util.ArrayList;

import java.util.Date;
import java.util.Hashtable;
import java.util.List;

import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;


/**
 *
 *
 */
public class XlsTypeHandler extends MsDocTypeHandler {


    /**
     * _more_
     *
     * @param repository _more_
     * @param entryNode _more_
     *
     * @throws Exception _more_
     */
    public XlsTypeHandler(Repository repository, Element entryNode)
            throws Exception {
        super(repository, entryNode);
    }


    /**
     * _more_
     *
     * @param request _more_
     * @param requestProps _more_
     * @param entry _more_
     *
     * @return _more_
     *
     * @throws Exception _more_
     */
    @Override
    public String getSimpleDisplay(Request request, Hashtable requestProps,
                                   Entry entry)
            throws Exception {
        if ( !Misc.equals(entry.getValue(0, "false"), "true")) {
            return null;
        }
                                               
        boolean useFirstRowAsHeader = Misc.equals("true",
                                          entry.getValue(1, "true"));


        boolean colHeader = Misc.equals("true", entry.getValue(2, "false"));
        boolean rowHeader = Misc.equals("true", entry.getValue(3, "false"));
        List<String> widths = StringUtil.split(entry.getValue(4,""),",",true,true);
        List    propsList = new ArrayList();

        propsList.add("useFirstRowAsHeader");
        propsList.add("" + useFirstRowAsHeader);
        propsList.add("colHeaders");
        propsList.add("" + colHeader);
        propsList.add("rowHeaders");
        propsList.add("" + rowHeader);


        if(widths.size()>0) {
            propsList.add("colWidths");
            propsList.add(Json.list(widths));
        }

        String        props = Json.map(propsList);

        StringBuilder sb    = new StringBuilder();
        String jsonUrl =
            request.entryUrl(getRepository().URL_ENTRY_SHOW, entry,
                             ARG_OUTPUT,
                             XlsOutputHandler.OUTPUT_XLS_JSON.getId());



        sb.append(HtmlUtils.importJS(getRepository().getUrlBase()
                                     + "/media/jquery.handsontable.full.js"));
        sb.append(HtmlUtils.cssLink(getRepository().getUrlBase()
                                    + "/media/jquery.handsontable.full.css"));

        sb.append(HtmlUtils.cssLink(getRepository().getUrlBase()
                                    + "/media/xls.css"));
        sb.append(HtmlUtils.importJS(getRepository().getUrlBase()
                                     + "/media/xls.js"));

        sb.append("\n");
        sb.append(header(entry.getName()));
        String divId = HtmlUtils.getUniqueId("div_");
        sb.append(HtmlUtils.div("", HtmlUtils.id(divId)));
        String js = "var ramaddaXls  = new RamaddaXls("
                    + HtmlUtils.quote(divId) + "," + HtmlUtils.quote(jsonUrl)
                    + "," + props + ");";
        sb.append(HtmlUtils.script("$( document ).ready(function() {\n" + js
                                   + "\n});\n"));

        return sb.toString();
    }


    /**
     * _more_
     *
     * @param request _more_
     * @param entry _more_
     * @param wikiTemplate _more_
     *
     * @return _more_
     *
     * @throws Exception _more_
     */
    @Override
    public String getInnerWikiContent(Request request, Entry entry,
                                      String wikiTemplate)
            throws Exception {
        return getSimpleDisplay(request, null, entry);
    }


}
