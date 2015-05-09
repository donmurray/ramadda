/*
 * Copyright (c) 2008-2015 Geode Systems LLC
 * This Software is licensed under the Geode Systems RAMADDA License available in the source distribution in the file 
 * ramadda_license.txt. The above copyright notice shall be included in all copies or substantial portions of the Software.
 */

package org.ramadda.plugins.biz;


import org.json.*;


import org.ramadda.repository.*;
import org.ramadda.repository.metadata.*;
import org.ramadda.repository.type.*;
import org.ramadda.util.HtmlUtils;
import org.ramadda.util.Json;
import org.ramadda.util.TTLCache;
import org.ramadda.util.Utils;

import org.w3c.dom.*;

import ucar.unidata.util.DateUtil;
import ucar.unidata.util.IOUtil;
import ucar.unidata.util.StringUtil;

import java.net.URL;

import java.text.SimpleDateFormat;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.List;


/**
 */
public class FredTypeHandler extends ExtensibleGroupTypeHandler {

    public static final String URL_BASE = "http://api.stlouisfed.org/fred";
 
    public static final String URL_CATEGORY = URL_BASE + "/category";
    public static final String URL_CATEGORY_CHILDREN = URL_BASE + "/category/children";
    public static final String URL_CATEGORY_SERIES = URL_BASE + "/category/series";
    public static final String URL_CATEGORY_RELATED = URL_BASE + "/category/related";

    public static final String URL_FRED_SERIES = URL_BASE + "/fred/series";
    public static final String URL_FRED_SERIES_CATEGORIES = URL_BASE + "/fred/series/categories";
    public static final String URL_FRED_SERIES_OBSERVATIONS = URL_BASE + "/fred/series/observations";
    public static final String URL_FRED_SERIES_RELEASE = URL_BASE + "/fred/series/release";
    public static final String URL_FRED_SERIES_SEARCH = URL_BASE + "/fred/series/search";

    //    http://api.stlouisfed.org/fred/category/children?category_id=13&api_key=abcdefghijklmnopqrstuvwxyz123456&file_type=json
    public static final String ARG_API_KEY = "api_key";
    public static final String ARG_CATEGORY_ID = "category_id";
    public static final String ARG_FILE_TYPE = "file_type";


    public static final String PROP_API_KEY = "fred.api.key";




    private String apiKey;


    /**
     * _more_
     *
     * @param repository _more_
     * @param entryNode _more_
     *
     * @throws Exception _more_
     */
    public FredTypeHandler(Repository repository, Element entryNode)
            throws Exception {
        super(repository, entryNode);
    }


    public String getApiKey() {
        if(apiKey == null) {
            apiKey = getRepository().getProperty(PROP_API_KEY,(String) null);
        }
        return apiKey;
    }


    /**
     * _more_
     *
     * @param request _more_
     * @param entry _more_
     * @param parent _more_
     * @param newEntry _more_
     *
     * @throws Exception _more_
     */
    public void initializeEntryFromForm(Request request, Entry entry,
                                        Entry parent, boolean newEntry)
            throws Exception {
        super.initializeEntryFromForm(request, entry, parent, newEntry);

        /*
        Object[] values = entry.getTypeHandler().getEntryValues(entry);
        values[IDX_TEAM_ID] = Json.readValue(result, "team.id", "");
        String domain = Json.readValue(result, "team.domain", "");
        values[IDX_TEAM_DOMAIN] = domain;
        entry.setResource(new Resource(Slack.getTeamUrl(domain)));
        */
    }





    /**
     * _more_
     *
     * @param request _more_
     * @param teamEntry _more_
     * @param parentEntry _more_
     * @param synthId _more_
     *
     * @return _more_
     *
     * @throws Exception _more_
     */
    public List<String> getSynthIds(Request request, Entry mainEntry,
                                    Entry parentEntry, String synthId)
            throws Exception {
        //        System.err.println("FredTypeHandler.getSynthIds:" + synthId +" parent:" + parentEntry.getName()  +" team: " + teamEntry.getName());
        List<String> ids = parentEntry.getChildIds();
        if (ids != null) {
            return ids;
        }
        

        List<String> args = new ArrayList<String>();
        args.add(ARG_API_KEY);
        args.add(getApiKey());

        String url = HtmlUtils.url(URL_CATEGORY_CHILDREN, args);

        System.err.println ("Fred URL:" + url);
        String xml = IOUtil.readContents(new URL(url));
        System.err.println ("xml:" + xml);

        ids = new ArrayList<String>();
        parentEntry.setChildIds(ids);
        return ids;
    }



    /**
     * _more_
     *
     * @param teamEntry _more_
     * @param channel _more_
     * @param channelsToShow _more_
     *
     * @return _more_
     *
     * @throws Exception _more_
     */
    private Entry createCategoryEntry(Entry teamEntry, JSONObject channel) 
            throws Exception {
        return null;
        /*

        String channelId = Json.readValue(channel, "id", "");
        String name      = Json.readValue(channel, "name", "");
        if (channelsToShow != null) {
            if ( !(channelsToShow.contains(channelId)
                    || channelsToShow.contains(name))) {
                return null;
            }
        }
        Date dttm = Slack.getDate(Json.readValue(channel, "created", ""));
        String id = getEntryManager().createSynthId(teamEntry, channelId);

        String topic = Json.readValue(channel, "topic.value", "");
        String purpose = Json.readValue(channel, "purpose.value", "");
        TypeHandler channelTypeHandler =
            getRepository().getTypeHandler("slack_channel");
        Entry channelEntry = new Entry(id, channelTypeHandler);
        //        https://geodesystems.slack.com/messages/general/

        String desc       = "";
        String teamDomain = (String) teamEntry.getValue(IDX_TEAM_DOMAIN, "");
        Resource resource =
            new Resource(new Resource(Slack.getChannelUrl(teamDomain, name)));
        Object[] values = channelTypeHandler.makeEntryValues(null);

        values[SlackChannelTypeHandler.IDX_CHANNEL_ID]      = channelId;
        values[SlackChannelTypeHandler.IDX_CHANNEL_PURPOSE] = purpose;

        channelEntry.initEntry(name, desc, teamEntry, teamEntry.getUser(),
                               resource, "", dttm.getTime(), dttm.getTime(),
                               dttm.getTime(), dttm.getTime(), values);

        channelEntry.setMasterTypeHandler(this);

        return channelEntry;
        */
    }



    /**
     * _more_
     *
     * @param request _more_
     * @param teamEntry _more_
     * @param id _more_
     *
     * @return _more_
     *
     * @throws Exception _more_
     */
    @Override
    public Entry makeSynthEntry(Request request, Entry mainEntry, String id)
            throws Exception {
        return null;
            //        return createMessageEntry(request, teamEntry, channelId, message);
    }


    /**
     * _more_
     *
     * @return _more_
     */
    public boolean isSynthType() {
        return true;
    }



    /**
     * _more_
     *
     * @param id _more_
     *
     * @return _more_
     */
    public Entry createEntry(String id) {
        return new Entry(id, this, true);
    }



}
