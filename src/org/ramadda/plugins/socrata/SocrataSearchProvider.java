/*
 * Copyright (c) 2008-2015 Geode Systems LLC
 * This Software is licensed under the Geode Systems RAMADDA License available in the source distribution in the file 
 * ramadda_license.txt. The above copyright notice shall be included in all copies or substantial portions of the Software.
 */

package org.ramadda.plugins.socrata;


import org.json.*;

import org.ramadda.repository.*;
import org.ramadda.repository.metadata.*;
import org.ramadda.repository.search.*;
import org.ramadda.repository.type.*;
import org.ramadda.util.HtmlUtils;

import org.ramadda.util.Json;
import org.ramadda.util.Utils;



import ucar.unidata.util.DateUtil;
import ucar.unidata.util.IOUtil;
import ucar.unidata.util.StringUtil;
import ucar.unidata.xml.XmlUtil;

import java.io.*;

import java.net.URL;
import java.net.URLConnection;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;


/**
 * Proxy that searches youtube
 *
 */
public class SocrataSearchProvider extends SearchProvider {

    /** _more_          */
    private String hostname;


    /**
     * _more_
     *
     * @param repository _more_
     * @param args _more_
     */
    public SocrataSearchProvider(Repository repository, List<String> args) {
        super(repository, args.get(0), (args.size() > 1)
                                       ? args.get(1)
                                       : args.get(0));
        hostname = args.get(0);
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
        //TODO: handle limit better
        String server = "https://" + hostname;
        String url = server
                     + "/api/search/views.json?limit=50&q="
                     + HtmlUtils.urlEncode(request.getString(ARG_TEXT, ""));
        System.err.println(getName() + " search url:" + url);
        InputStream is   = getInputStream(url);
        String      json = IOUtil.readContents(is);
        IOUtil.close(is);
        //        System.out.println("json:" + json);
        JSONObject obj = new JSONObject(new JSONTokener(json));
        if ( !obj.has("results")) {
            System.err.println(
                "Socrata SearchProvider: no items field in json:" + json);

            return entries;
        }

        JSONArray   searchResults = obj.getJSONArray("results");
        Entry       parent        = getSynthTopLevelEntry();
        TypeHandler typeHandler   = getRepository().getTypeHandler(SocrataSeriesTypeHandler.TYPE_SERIES);

        for (int i = 0; i < searchResults.length(); i++) {
            JSONObject wrapper    = searchResults.getJSONObject(i);
            JSONObject item = wrapper.getJSONObject("view");
            String     id    = Json.readValue(item, "id", "");
            String     name    = Json.readValue(item, "name", "");
            String     desc    = Json.readValue(item, "description", "");
            String     displayType    = Json.readValue(item, "displayType", "");
            Date   dttm = new Date();
            Date fromDate = dttm, toDate = dttm;
            String itemUrl = "https://" + hostname +"/dataset/-/" + id;
            Entry newEntry = new Entry(Repository.ID_PREFIX_SYNTH + getId()
                                       + ":" + id, typeHandler);
            Object[] values = typeHandler.makeEntryValues(null);
            values[SocrataSeriesTypeHandler.IDX_REPOSITORY] = server;
            values[SocrataSeriesTypeHandler.IDX_SERIES_ID] = id;
            newEntry.setIcon("/socrata/socrata.png");
            entries.add(newEntry);
            newEntry.initEntry(name, desc, parent,
                               getUserManager().getLocalFileUser(),
                               new Resource(new URL(itemUrl)), "",
                               dttm.getTime(), dttm.getTime(),
                               fromDate.getTime(), toDate.getTime(), values);
            getEntryManager().cacheEntry(newEntry);
        }

        return entries;
    }



}