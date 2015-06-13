/*
 * Copyright (c) 2008-2015 Geode Systems LLC
 * This Software is licensed under the Geode Systems RAMADDA License available in the source distribution in the file 
 * ramadda_license.txt. The above copyright notice shall be included in all copies or substantial portions of the Software.
 */

package org.ramadda.plugins.search;


import org.apache.commons.httpclient.params.HttpClientParams;



import org.json.*;

import org.ramadda.repository.*;
import org.ramadda.repository.metadata.*;
import org.ramadda.repository.search.*;
import org.ramadda.repository.type.*;
import org.ramadda.util.HtmlUtils;

import org.ramadda.util.HttpFormField;

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
 * Proxy that searches twitter
 *
 */
public class TwitterSearchProvider extends SearchProvider {

    /** _more_ */
    private static final String ID = "twitter";

    /** _more_ */
    private static final String URL =
        "https://www.googleapis.com/youtube/v3/search?part=snippet";


    /** _more_ */
    private static final String ARG_API_KEY = "key";

    /** _more_ */
    private static final String ARG_Q = "q";

    /** _more_ */
    private static final String ARG_MIN_TAKEN_DATE = "min_taken_date";

    /** _more_ */
    private static final String ARG_MAX_TAKEN_DATE = "max_taken_date";

    /** _more_ */
    private static final String ARG_TAGS = "tags";

    /** _more_ */
    private static final String ARG_BBOX = "bbox";





    /**
     * _more_
     *
     * @param repository _more_
     */
    public TwitterSearchProvider(Repository repository) {
        super(repository, ID, "Twitter Search");
    }


    /**
     * _more_
     *
     * @return _more_
     */
    public boolean isEnabled() {
        return getApiKey() != null;
    }


    /**
     * _more_
     *
     * @throws Exception _more_
     */
    private static void getCredentials() throws Exception {

        String cred = "todo:todo";
        //getRepository().getProperty("twitter.api.key","")+TypeHandler.ID_DELIMITER + getRepository().getProperty("twitter.api.secret","");
        cred = Utils.encodeBase64(cred.getBytes());
        String           url    = "https://api.twitter.com/oauth2/token";
        HttpClientParams params = new HttpClientParams();
        System.err.println("Basic " + cred);
        params.setParameter("Authorization", "Basic " + cred);
        params.setParameter("Content-Type",
                            "application/x-www-form-urlencoded");

        List entries = new ArrayList();
        entries.add(HttpFormField.hidden("grant_type", "client_credentials"));
        String[] result = HttpFormField.doPost(entries, url, params, true);
        System.err.println("result:" + result[0] + " " + result[1]);
    }

    /**
     * _more_
     *
     * @param args _more_
     *
     * @throws Exception _more_
     */
    public static void main(String[] args) throws Exception {
        getCredentials();
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
        String      url     = URL;
        url = HtmlUtils.url(url, ARG_API_KEY, getApiKey(), ARG_Q,
                            request.getString(ARG_TEXT, ""));
        System.err.println(getName() + " search url:" + url);
        URLConnection connection = new URL(url).openConnection();
        connection.setRequestProperty("User-Agent", "ramadda");
        InputStream is   = connection.getInputStream();
        String      json = IOUtil.readContents(is);
        //        System.out.println("xml:" + json);
        JSONObject obj = new JSONObject(new JSONTokener(json));
        if ( !obj.has("items")) {
            System.err.println(
                "YouTube SearchProvider: no items field in json:" + json);

            return entries;
        }

        JSONArray searchResults = obj.getJSONArray("items");
        Entry     parent        = getSynthTopLevelEntry();
        TypeHandler typeHandler =
            getRepository().getTypeHandler("media_youtubevideo");

        for (int i = 0; i < searchResults.length(); i++) {
            JSONObject item    = searchResults.getJSONObject(i);
            JSONObject snippet = item.getJSONObject("snippet");
            String     kind    = Json.readValue(item, "id.kind", "");
            if ( !kind.equals("youtube#video")) {
                System.err.println("? Youtube kind:" + kind);

                continue;
            }
            String id   = Json.readValue(item, "id.videoId", "");
            String name = snippet.getString("title");
            String desc = snippet.getString("description");

            Date   dttm = new Date();
            Date fromDate = DateUtil.parse(Json.readValue(snippet,
                                "publishedAt", null));
            Date   toDate  = fromDate;

            String itemUrl = "https://www.youtube.com/watch?v=" + id;

            Entry newEntry = new Entry(Repository.ID_PREFIX_SYNTH + getId()
                                       + TypeHandler.ID_DELIMITER + id, typeHandler);
            entries.add(newEntry);

            String thumb = Json.readValue(snippet, "thumbnails.default.url",
                                          null);

            if (thumb != null) {
                Metadata thumbnailMetadata =
                    new Metadata(getRepository().getGUID(), newEntry.getId(),
                                 ContentMetadataHandler.TYPE_THUMBNAIL,
                                 false, thumb, null, null, null, null);
                newEntry.addMetadata(thumbnailMetadata);
            }


            newEntry.initEntry(name, desc, parent,
                               getUserManager().getLocalFileUser(),
                               new Resource(new URL(itemUrl)), "",
                               dttm.getTime(), dttm.getTime(),
                               fromDate.getTime(), toDate.getTime(), null);
            getEntryManager().cacheEntry(newEntry);
        }

        return entries;
    }



}
