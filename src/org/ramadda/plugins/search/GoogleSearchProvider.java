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

import org.ramadda.repository.util.ServerInfo;
import org.ramadda.util.HtmlUtils;
import org.ramadda.util.Utils;

import ucar.unidata.util.IOUtil;

import java.net.URL;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;


/**
 * Class description
 *
 *
 * @version        $version$, Sat, Nov 8, '14
 * @author         Enter your name here...
 */
public class GoogleSearchProvider extends SearchProvider {

    public static final String URL = "http://ajax.googleapis.com/ajax/services/search/web?v=1.0";
    public static final String SEARCH_ID ="google";

    /**
     * _more_
     *
     * @param repository _more_
     */
    public GoogleSearchProvider(Repository repository) {
        super(repository,SEARCH_ID,"Google");
    }

    /**
     * _more_
     *
     * @param repository _more_
     * @param args _more_
     */
    public GoogleSearchProvider(Repository repository, List<String> args) {
        super(repository, SEARCH_ID, "Google");
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
                             request.getString(ARG_TEXT, ""));
        System.err.println("google search url:" + url);
        String json = IOUtil.readContents(url);
        //        System.err.println("Json:" + json);
        JSONObject obj = new JSONObject(new JSONTokener(json));
        if ( !obj.has("responseData")) {
            System.err.println("GoogleSearchProvider: no response field in json:" + json);
            return entries;
        }

        JSONObject response = obj.getJSONObject("responseData");
        JSONArray searchResults = response.getJSONArray("results");
        TypeHandler typeHandler = getRepository().getTypeHandler("link");
        Entry parent = getRepository().getEntryManager().getTopGroup();
        for (int i = 0; i < searchResults.length(); i++) {
            JSONObject result = searchResults.getJSONObject(i);
            String     name    = result.getString("titleNoFormatting");
            String     desc    = result.getString("content");
            String     resultUrl    = result.getString("url");
            Entry        newEntry = new Entry(Repository.ID_PREFIX_SYNTH+SEARCH_ID+":" + resultUrl, typeHandler);
            entries.add(newEntry);
            newEntry.setIcon("/search/google-icon.png");
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



}