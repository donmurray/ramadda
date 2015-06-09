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

    /** _more_ */
    private static final String SOCRATA_ID = "socrata";

    /** _more_ */
    private static final String URL_ALL =
        "http://api.us.socrata.com/api/catalog/v1?";

    /** _more_ */
    private String hostname;


    /**
     * _more_
     *
     * @param repository _more_
     */
    public SocrataSearchProvider(Repository repository) {
        this(repository, new ArrayList<String>());
    }


    /**
     * _more_
     *
     * @param repository _more_
     * @param args _more_
     */
    public SocrataSearchProvider(Repository repository, List<String> args) {
        super(repository, ((args.size() == 0)
                           ? SOCRATA_ID
                           : args.get(0)), ((args.size() > 1)
                                            ? args.get(1)
                                            : ((args.size() == 0)
                ? "All Socrata Sites"
                : args.get(0))));
        if (args.size() > 0) {
            hostname = args.get(0);
        }
    }


    /**
     * _more_
     *
     * @return _more_
     */
    @Override
    public String getCategory() {
        return "Socrata Search Providers";
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

        if ((hostname == null) || true) {
            return doSearchAll(request, searchCriteriaSB);
        }

        List<Entry> entries = new ArrayList<Entry>();


        String      server  = "https://" + hostname;
        String url = server + "/api/search/views.json?q="
                     + HtmlUtils.urlEncodeSpace(request.getString(ARG_TEXT,
                         ""));
        System.err.println(getName() + " search url:" + url);

        String json = new String(IOUtil.readBytes(getInputStream(url)));
        //        System.out.println("json:" + json);
        JSONObject obj = new JSONObject(new JSONTokener(json));
        if ( !obj.has("results")) {
            System.err.println(
                "Socrata SearchProvider: no items field in json:" + json);

            return entries;
        }

        JSONArray searchResults = obj.getJSONArray("results");
        Entry     parent        = getSynthTopLevelEntry();
        TypeHandler seriesTypeHandler =
            getRepository().getTypeHandler(
                SocrataSeriesTypeHandler.TYPE_SERIES);
        TypeHandler fileTypeHandler =
            getRepository().getTypeHandler(TypeHandler.TYPE_FILE);

        for (int i = 0; i < searchResults.length(); i++) {
            JSONObject wrapper = searchResults.getJSONObject(i);
            JSONObject item    = wrapper.getJSONObject("view");
            String     id      = Json.readValue(item, "id", "");
            String     name    = Json.readValue(item, "name", "");
            StringBuilder desc = new StringBuilder(Json.readValue(item,
                                     "description", ""));
            String      displayType = Json.readValue(item, "displayType", "");
            String      viewType    = Json.readValue(item, "viewType", "");

            TypeHandler typeHandler = (viewType.equals("tabular")
                                       ? seriesTypeHandler
                                       : fileTypeHandler);


            Date        dttm        = new Date();
            Date        fromDate    = dttm,
                        toDate      = dttm;
            String      itemUrl = "https://" + hostname + "/dataset/-/" + id;
            Resource    resource    = new Resource(new URL(itemUrl));
            Entry newEntry = new Entry(Repository.ID_PREFIX_SYNTH + getId()
                                       + ":" + id, typeHandler);
            Object[] values = typeHandler.makeEntryValues(null);
            if (viewType.equals("tabular")) {
                values[SocrataSeriesTypeHandler.IDX_REPOSITORY] = server;
                values[SocrataSeriesTypeHandler.IDX_SERIES_ID]  = id;
            } else if (viewType.equals("blobby")) {
                String mimeType = StringUtil.splitUpTo(Json.readValue(item,
                                      "blobMimeType", ";"), ";",
                                          2).get(0).trim();
                String fileUrl = "https://" + hostname + "/download/" + id
                                 + "/" + mimeType;
                resource = new Resource(new URL(fileUrl));
                //            https://www.opendatanyc.com/download/ewq6-p8b6/application/pdf
                desc.append(HtmlUtils.br());
                desc.append(HtmlUtils.href(itemUrl, "View file at Socrata"));
            }

            newEntry.setIcon("/socrata/socrata.png");
            entries.add(newEntry);
            newEntry.initEntry(name, desc.toString(), parent,
                               getUserManager().getLocalFileUser(), resource,
                               "", dttm.getTime(), dttm.getTime(),
                               fromDate.getTime(), toDate.getTime(), values);
            getEntryManager().cacheEntry(newEntry);
        }

        return entries;
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
    private List<Entry> doSearchAll(Request request,
                                    Appendable searchCriteriaSB)
            throws Exception {

        List<Entry> entries = new ArrayList<Entry>();



        String url = URL_ALL + "&"
                     + HtmlUtils.arg(
                         "q",
                         HtmlUtils.urlEncodeSpace(
                             request.getString(ARG_TEXT, "")), false);
        if (hostname != null) {
            url += "&" + HtmlUtils.arg("domains", hostname);
        }

        System.err.println(getName() + " search url:" + url);
        String json = new String(IOUtil.readBytes(getInputStream(url)));
        //        System.out.println("json:" + json);
        JSONObject obj = new JSONObject(new JSONTokener(json));
        if ( !obj.has("results")) {
            System.err.println(
                "Socrata SearchProvider: no items field in json:" + json);

            return entries;
        }

        JSONArray searchResults = obj.getJSONArray("results");
        Entry     parent        = getSynthTopLevelEntry();
        TypeHandler seriesTypeHandler =
            getRepository().getTypeHandler(
                SocrataSeriesTypeHandler.TYPE_SERIES);
        TypeHandler fileTypeHandler =
            getRepository().getTypeHandler(TypeHandler.TYPE_FILE);

        for (int i = 0; i < searchResults.length(); i++) {
            JSONObject   wrapper    = searchResults.getJSONObject(i);
            JSONObject   item       = Json.readObject(wrapper, "resource");
            String       id         = Json.readValue(item, "id", "");
            String       name       = Json.readValue(item, "name", "");
            String       domain     = Json.readValue(item, "domain", "");
            String       server     = "https://" + domain;
            List<String> tmp        = StringUtil.splitUpTo(domain, ".", 2);
            String       domainName = (tmp.size() > 1)
                                      ? tmp.get(1)
                                      : domain;
            name += " - " + domainName;

            StringBuilder desc = new StringBuilder(Json.readValue(item,
                                     "description", ""));
            String      type        = Json.readValue(item, "type", "");

            String      itemUrl     = Json.readValue(wrapper, "link", "");
            TypeHandler typeHandler = (type.equals("dataset")
                                       ? seriesTypeHandler
                                       : fileTypeHandler);




            Date        dttm        = new Date();
            Date        fromDate    = dttm,
                        toDate      = dttm;
            Resource    resource    = new Resource(new URL(itemUrl));
            Entry newEntry = new Entry(Repository.ID_PREFIX_SYNTH + getId()
                                       + ":" + domain + ":"
                                       + id, typeHandler);
            Object[] values = typeHandler.makeEntryValues(null);
            if (type.equals("dataset")) {
                values[SocrataSeriesTypeHandler.IDX_REPOSITORY] = server;
                values[SocrataSeriesTypeHandler.IDX_SERIES_ID]  = id;
            } else if (true || type.equals("blobby")) {
                System.err.println("Socrata - new Type:" + type);
                /*
                String mimeType = StringUtil.splitUpTo(Json.readValue(item,
                                      "blobMimeType", ";"), ";",
                                          2).get(0).trim();
                String fileUrl = "https://" + domain + "/download/" + id
                                 + "/" + mimeType;
                resource = new Resource(new URL(fileUrl));
                //            https://www.opendatanyc.com/download/ewq6-p8b6/application/pdf
                desc.append(HtmlUtils.br());
                desc.append(HtmlUtils.href(itemUrl, "View file at Socrata"));
                */
            }

            newEntry.setIcon("/socrata/socrata.png");
            entries.add(newEntry);
            newEntry.initEntry(name, desc.toString(), parent,
                               getUserManager().getLocalFileUser(), resource,
                               "", dttm.getTime(), dttm.getTime(),
                               fromDate.getTime(), toDate.getTime(), values);
            getEntryManager().cacheEntry(newEntry);
        }

        return entries;
    }



}
