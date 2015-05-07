/*
 * Copyright (c) 2008-2015 Geode Systems LLC
 * This Software is licensed under the Geode Systems RAMADDA License available in the source distribution in the file 
 * ramadda_license.txt. The above copyright notice shall be included in all copies or substantial portions of the Software.
 */

package org.ramadda.plugins.search;


import org.json.*;

import org.ramadda.repository.*;
import org.ramadda.repository.search.*;
import org.ramadda.repository.type.*;

import org.ramadda.util.HtmlUtils;
import org.ramadda.util.Utils;
import ucar.unidata.util.IOUtil;

import java.net.URL;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;


/**
 * Proxy that searches google
 *
 */
public class DuckDuckGoSearchProvider extends SearchProvider {

    
    public static final String URL = "https://api.duckduckgo.com?format=json";
    public static final String SEARCH_ID ="duckduckgo";

    /**
     * _more_
     *
     * @param repository _more_
     */
    public DuckDuckGoSearchProvider(Repository repository) {
        super(repository,SEARCH_ID,"Duck Duck Go");
    }

    /**
     * _more_
     *
     * @param repository _more_
     * @param args _more_
     */
    public DuckDuckGoSearchProvider(Repository repository, List<String> args) {
        super(repository, SEARCH_ID, "Duck Duck Go");
    }



    /**
     * _more_
     *
     * @param request _more_
     * @param searchCriteriaSB _more_
     *
     * @return _more_
     *
     * @throws Exception _more_
     */
    @Override
    public List<Entry> getEntries(Request request,
                                  Appendable searchCriteriaSB)
            throws Exception {

        List<Entry> entries = new ArrayList<Entry>();
        String url = URL;
        url += "&";
        url += HtmlUtils.arg("q",
                             HtmlUtils.urlEncode(request.getString(ARG_TEXT, "")));
        System.err.println(getName() +" search url:" + url);
        String json = IOUtil.readContents(url);
        System.err.println("Json:" + json);
        JSONObject obj = new JSONObject(new JSONTokener(json));
        if ( !obj.has("RelatedTopics")) {
            System.err.println("DuckDuckGo SearchProvider: no RelatedTopics field in json:" + json);
            return entries;
        }

        JSONArray searchResults = obj.getJSONArray("RelatedTopics");
        TypeHandler typeHandler = getRepository().getTypeHandler("link");
        Entry parent = getRepository().getEntryManager().getTopGroup();
        for (int i = 0; i < searchResults.length(); i++) {
            JSONObject result = searchResults.getJSONObject(i);
            String     name    = result.getString("Text");
            String     desc    = result.getString("Result");
            String     resultUrl    = result.getString("FirstURL");
            Entry        newEntry = new Entry(Repository.ID_PREFIX_SYNTH+getId()+":" + resultUrl, typeHandler);
            entries.add(newEntry);
            newEntry.setIcon("/search/duckduckgo.png");
            Date dttm  = new Date();
            newEntry.initEntry(name, desc, parent,
                               getUserManager().getLocalFileUser(),
                               new Resource(new URL(resultUrl)), "", dttm.getTime(),
                               dttm.getTime(), dttm.getTime(),
                               dttm.getTime(), null);
            getEntryManager().cacheEntry(newEntry);
        }
        return entries;
    }

    public static void main(String[]args) throws Exception {
        String url = "https://api.duckduckgo.com/?format=json&t=ramadda&q=zoom";
        String json = IOUtil.readContents(new URL(url));
        System.err.println(json);
    }


}