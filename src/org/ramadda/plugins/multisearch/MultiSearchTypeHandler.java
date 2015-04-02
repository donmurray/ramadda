/**
* Copyright (c) 2008-2015 Geode Systems LLC
* This Software is licensed under the Geode Systems RAMADDA License available in the source distribution in the file 
* ramadda_license.txt. The above copyright notice shall be included in all copies or substantial portions of the Software.
*/

/**
 * Copyright (c) 2008-2015 Geode Systems LLC
 * This Software is licensed under the Geode Systems RAMADDA License available in the source distribution in the file
 * ramadda_license.txt. The above copyright notice shall be included in all copies or substantial portions of the Software.
 */

package org.ramadda.plugins.multisearch;


import org.ramadda.repository.*;
import org.ramadda.repository.output.*;
import org.ramadda.repository.type.*;

import org.ramadda.util.HtmlUtils;

import org.w3c.dom.*;

import ucar.unidata.util.StringUtil;

import java.util.ArrayList;

import java.util.List;


/**
 *
 *
 */
public class MultiSearchTypeHandler extends GenericTypeHandler {

    /** _more_ */
    public static String ARG_QUERY = "query";

    /**
     * _more_
     *
     * @param repository _more_
     * @param entryNode _more_
     *
     * @throws Exception _more_
     */
    public MultiSearchTypeHandler(Repository repository, Element entryNode)
            throws Exception {
        super(repository, entryNode);
    }


    /**
     * _more_
     *
     * @param request _more_
     * @param group _more_
     * @param subGroups _more_
     * @param entries _more_
     * @param entry _more_
     *
     * @return _more_
     *
     * @throws Exception _more_
     */
    public Result getHtmlDisplay(Request request, Entry entry)
            throws Exception {
        StringBuffer sb = new StringBuffer();
        String formUrl = request.entryUrl(getRepository().URL_ENTRY_SHOW,
                                          entry);
        String query = request.getString(ARG_QUERY, "");
        sb.append(HtmlUtils.form(formUrl, ""));
        sb.append(HtmlUtils.hidden(ARG_ENTRYID, entry.getId()));
        sb.append(msg("Search across multiple search engines"));
        sb.append(HtmlUtils.br());
        sb.append(HtmlUtils.input(ARG_QUERY, query, 40));
        sb.append(HtmlUtils.submit("Search"));
        sb.append(HtmlUtils.formClose());
        if (request.defined(ARG_QUERY)) {
            List<String> tabTitles = new ArrayList<String>();
            List<String> tabs      = new ArrayList<String>();
            String[]     urls      = {
                "Google",
                "http://www.google.com/search?hl=en&ie=ISO-8859-1&q=${query}&btnG=Search",
                "Bing",
                "http://www.bing.com/search?q=${query}&go=&form=QBLH&qs=n&sk=&sc=8-5",
                "Yahoo",
                "http://search.yahoo.com/search?p=${query}&ei=UTF-8&fr=moz35",
                "DuckDuckGo", "http://duckduckgo.com/?q=${query}",
            };
            for (int i = 0; i < urls.length; i += 2) {
                String title = urls[i];
                String url   = urls[i + 1];
                tabTitles.add(title);
                url = url.replace("${query}", query);
                StringBuffer tmp = new StringBuffer();
                tmp.append(HtmlUtils.href(url, "Go to " + title));
                tmp.append(
                    HtmlUtils.tag(
                        HtmlUtils.TAG_IFRAME,
                        HtmlUtils.attr(HtmlUtils.ATTR_SRC, url)
                        + HtmlUtils.attr(HtmlUtils.ATTR_WIDTH, "100%")
                        + HtmlUtils.attr(
                            HtmlUtils.ATTR_HEIGHT, "300"), "Need frames"));
                tabs.add(tmp.toString());
            }
            sb.append(OutputHandler.makeTabs(tabTitles, tabs, true));
        }

        return new Result("MultiSearch", sb);
    }




}
