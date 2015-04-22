/*
 * Copyright (c) 2008-2015 Geode Systems LLC
 * This Software is licensed under the Geode Systems RAMADDA License available in the source distribution in the file 
 * ramadda_license.txt. The above copyright notice shall be included in all copies or substantial portions of the Software.
 */

package org.ramadda.plugins.slack;


import org.json.*;


import org.ramadda.repository.*;
import org.ramadda.repository.auth.*;
import org.ramadda.repository.harvester.*;
import org.ramadda.repository.metadata.*;
import org.ramadda.repository.type.*;

import org.ramadda.util.HtmlUtils;
import org.ramadda.util.Json;
import org.ramadda.util.Utils;

import ucar.unidata.ui.HttpFormEntry;

import ucar.unidata.util.IOUtil;

import ucar.unidata.util.Misc;

import ucar.unidata.util.StringUtil;
import ucar.unidata.util.StringUtil;
import ucar.unidata.util.TwoFacedObject;

import ucar.unidata.xml.XmlUtil;

import java.io.*;

import java.net.*;



import java.util.ArrayList;
import java.util.Date;
import java.util.Enumeration;
import java.util.GregorianCalendar;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.List;
import java.util.Properties;




/**
 */
public class Slack {

    /** _more_          */
    public static boolean debug = false;

    /** _more_          */
    public static final String URL_BASE = "https://slack.com/api";

    /** _more_          */
    public static final String URL_SEARCH = URL_BASE + "/search.all";

    /** _more_          */
    public static final String API_TEAM_INFO = "team.info";

    /** _more_          */
    public static final String API_CHANNELS_LIST = "channels.list";

    /** _more_          */
    public static final String API_CHANNELS_INFO = "channels.info";

    /** _more_          */
    public static final String API_CHANNELS_HISTORY = "channels.history";

    /** _more_          */
    public static final String API_SEARCH_ALL = "search.all";

    /** _more_          */
    public static final String API_SEARCH_MESSAGES = "search.messages";

    /** _more_          */
    public static final String API_SEARCH_FILES = "search.files";

    /** _more_          */
    public static final String API_USERS_INFO = "users.info";

    public static final String API_RTM_START = "rtm.start";


    /** _more_          */
    public static final String ARG_TOKEN = "token";

    /** _more_          */
    public static final String ARG_CHANNEL = "channel";

    /** _more_          */
    public static final String ARG_QUERY = "query";

    /** _more_          */
    public static final String ARG_SORT = "sort";

    /** _more_          */
    public static final String ARG_SORT_DIR = "sort_dir";

    /** _more_          */
    public static final String ARG_COUNT = "count";

    /** _more_          */
    public static final String ARG_PAGE = "page";

    /** _more_          */
    public static final String ARG_LATEST = "latest";

    /** _more_          */
    public static final String ARG_OLDEST = "oldest";

    /** _more_          */
    public static final String ARG_INCLUSIVE = "inclusive";

    /** _more_          */
    public static final String ARG_USER = "user";



    /** _more_ */
    public static final String SLACK_PAYLOAD = "payload";


    /** _more_ */
    public static final String SLACK_TOKEN = "token";

    /** _more_ */
    public static final String SLACK_TEAM_ID = "team_id";

    /** _more_ */
    public static final String SLACK_TEAM_DOMAIN = "team_domain";

    /** _more_ */
    public static final String SLACK_CHANNEL_ID = "channel_id";

    /** _more_ */
    public static final String SLACK_CHANNEL_NAME = "channel_name";

    /** _more_ */
    public static final String SLACK_TIMESTAMP = "timestamp";

    /** _more_ */
    public static final String SLACK_USER_ID = "user_id";

    /** _more_ */
    public static final String SLACK_USER_NAME = "user_name";

    /** _more_ */
    public static final String SLACK_TEXT = "text";

    /** _more_ */
    public static final String SLACK_TRIGGER_WORD = "trigger_word";



    /** _more_          */
    public static final String JSON_OK = "ok";

    /** _more_          */
    public static final String JSON_ERROR = "error";

    /**
     * _more_
     *
     * @param repository _more_
     * @param endPoint _more_
     * @param token _more_
     *
     * @return _more_
     */
    public static JSONObject call(Repository repository, String endPoint,
                                  String token) {
        return call(repository, endPoint, token, null);
    }


    /**
     * _more_
     *
     * @param repository _more_
     * @param endPoint _more_
     * @param token _more_
     * @param args _more_
     *
     * @return _more_
     */
    public static JSONObject call(Repository repository, String endPoint,
                                  String token, String args) {
        try {
            if ( !Utils.stringDefined(token)) {
                System.err.println("Slack.call:" + endPoint + " no token");

                return null;
            }

            String url = URL_BASE + "/" + endPoint;
            url += "?" + HtmlUtils.arg(ARG_TOKEN, token);
            if (Utils.stringDefined(args)) {
                url += "&" + args;
            }
            //            System.err.println ("Slack api call:" + url);
            System.err.println("Slack.call:" + url);
            String json = IOUtil.readContents(new URL(url));
            if (json == null) {
                return null;
            }
            JSONObject obj = new JSONObject(json);
            if (debug) {
                System.err.println("JSON:" + json);
            }
            if ( !Json.readValue(obj, Slack.JSON_OK,
                                 "false").equals("true")) {
                String error = Json.readValue(obj, Slack.JSON_ERROR, "");
                repository.getLogManager().logError(
                    "Error calling Slack API:" + endPoint + " error:"
                    + error, null);

                return null;
            }

            return obj;
        } catch (Exception exc) {
            repository.getLogManager().logError("Error calling Slack API:"
                    + endPoint, exc);

            return null;
        }
    }


    /**
     * _more_
     *
     *
     * @param repository _more_
     * @param request _more_
     * @param message _more_
     * @param entries _more_
     * @param webHook _more_
     *
     * @return _more_
     *
     * @throws Exception _more_
     */
    public static Result makeEntryResult(Repository repository,
                                         Request request, String message,
                                         List<Entry> entries, String webHook)
            throws Exception {
        StringBuffer sb = new StringBuffer();
        if ( !Utils.stringDefined(webHook)) {
            sb.append(message);
            sb.append("\n");
        }
        String attachments = makeEntryLinks(repository, request, sb, entries);
        if ( !Utils.stringDefined(webHook)) {
            return new Result("", sb);
        }

        StringBuilder json = new StringBuilder();
        List<String>  map  = new ArrayList<String>();
        if (attachments != null) {
            map.add("attachments");
            map.add(attachments);
        }
        map.add(SLACK_TEXT);
        map.add(Json.quote(message + "\n"));
        map.add("username");
        map.add(Json.quote("RAMADDA"));
        if ((request != null) && request.defined(SLACK_CHANNEL_ID)) {
            map.add("channel");
            map.add(Json.quote(request.getString(SLACK_CHANNEL_ID, "")));
        }
        json.append(Json.map(map));
        List<HttpFormEntry> formEntries = new ArrayList<HttpFormEntry>();
        formEntries.add(HttpFormEntry.hidden(SLACK_PAYLOAD, json.toString()));
        System.err.println("SlackHarvester: posting to slack");
        String[] result = HttpFormEntry.doPost(formEntries, webHook);
        System.err.println("SlackHarvester: results:" + result[0] + " "
                           + result[1]);

        return new Result("", new StringBuffer(""));
    }


    /**
     * _more_
     *
     *
     * @param repository _more_
     * @param request _more_
     * @param sb _more_
     * @param entries _more_
     *
     * @return _more_
     *
     * @throws Exception _more_
     */
    public static String makeEntryLinks(Repository repository,
                                        Request request, Appendable sb,
                                        List<Entry> entries)
            throws Exception {
        if (entries == null) {
            return null;
        }

        /*
        "attachments": [
                        {
                            "fallback": "Required plain-text summary of the attachment.",
                                "color": "#36a64f",
                                "pretext": "Optional text that appears above the attachment block",
                                "author_name": "Bobby Tables",
                                "author_link": "http://flickr.com/bobby/",
                                "author_icon": "http://flickr.com/icons/bobby.jpg",
                                "title": "Slack API Documentation",
                                "title_link": "https://api.slack.com/",
                                "text": "Optional text that appears within the attachment",
                                "fields": [
                                           {
                                               "title": "Priority",
                                                   "value": "High",
                                                   "short": false
                                                   }
                                           ],

                                "image_url": "http://my-website.com/path/to/image.jpg"
                                }
    ]
            */

        List<String> maps = new ArrayList<String>();

        int          cnt  = 0;
        for (Entry entry : entries) {
            cnt++;
            List<String> map = new ArrayList<String>();
            sb.append("<" + getEntryUrl(repository, request, entry) + "|"
                      + entry.getName() + ">\n");


            map.add("title");
            String name = entry.getName();
            if (entries.size() > 1) {
                name = "#" + cnt + " " + name;
            }
            map.add(Json.quote(name));
            map.add("title_link");
            map.add(Json.quote(getEntryUrl(repository, request, entry)));
            map.add("fallback");
            map.add(Json.quote(entry.getName()));
            map.add("color");
            map.add(Json.quote("#00FCF4"));


            StringBuffer desc = new StringBuffer();
            String snippet =
                request.getRepository().getWikiManager().getSnippet(request,
                    entry);
            if (Utils.stringDefined(snippet)) {
                desc.append(snippet);
                desc.append("\n");
            }
            Link downloadLink =
                entry.getTypeHandler().getEntryDownloadLink(request, entry);
            if (downloadLink != null) {
                desc.append(
                    "<" + downloadLink.getUrl() + "|"
                    + IOUtil.getFileTail(entry.getResource().getPath())
                    + ">\n");
                desc.append("\n");
            }


            map.add("text");
            map.add(Json.quote(desc.toString()));
            List<String> fields = new ArrayList<String>();
            /*
            fields.add(Json.map("title", Json.quote("From date"),
                                "value",Json.quote(getWikiManager().formatDate(request,  new Date(entry.getCreateDate()), entry))));
            */
            if ((request != null) && entry.getResource().isImage()) {
                map.add("image_url");
                map.add(Json
                    .quote(request
                        .getAbsoluteUrl(request.getRepository()
                            .getHtmlOutputHandler()
                            .getImageUrl(request, entry, true))));
            }
            map.add("fields");
            map.add(Json.list(fields));

            maps.add(Json.map(map));
        }
        String attachments = Json.list(maps);

        //        System.err.println("attachments:" + attachments);
        return attachments;
    }



    /**
     * _more_
     *
     * @param request _more_
     *
     * @return _more_
     */
    public static String getSlackChannelId(Request request) {
        return request.getString(SLACK_CHANNEL_ID, "");
    }

    /**
     * _more_
     *
     * @param request _more_
     *
     * @return _more_
     */
    public static String getSlackChannelName(Request request) {
        return request.getString(SLACK_CHANNEL_NAME, "");
    }

    /**
     * _more_
     *
     * @param request _more_
     *
     * @return _more_
     */
    public static String getSlackUserId(Request request) {
        return request.getString(SLACK_USER_ID, "");
    }

    /**
     * _more_
     *
     * @param request _more_
     *
     * @return _more_
     */
    public static String getSlackUserName(Request request) {
        return request.getString(SLACK_USER_NAME, "");
    }

    /**
     * _more_
     *
     * @param request _more_
     *
     * @return _more_
     */
    public static String getSlackText(Request request) {
        return request.getString(SLACK_TEXT, "").trim();
    }

    /**
     * _more_
     *
     *
     * @param repository _more_
     * @param request _more_
     * @param entry _more_
     *
     * @return _more_
     *
     * @throws Exception _more_
     */
    public static String getEntryUrl(Repository repository, Request request,
                                     Entry entry)
            throws Exception {
        return request.getAbsoluteUrl(
            request.entryUrl(request.getRepository().URL_ENTRY_SHOW, entry));
    }

    /**
     * _more_
     *
     * @param s _more_
     *
     * @return _more_
     *
     * @throws Exception _more_
     */
    public static Date getDate(String s) throws Exception {
        double milliseconds = 1000 * Double.parseDouble(s);

        return new Date((long) milliseconds);
    }


    /**
     * _more_
     *
     * @param teamDomain _more_
     *
     * @return _more_
     *
     * @throws Exception _more_
     */
    public static URL getTeamUrl(String teamDomain) throws Exception {
        return new URL("https://" + teamDomain + ".slack.com/messages");
    }

    /**
     * _more_
     *
     * @param teamDomain _more_
     * @param channelId _more_
     *
     * @return _more_
     *
     * @throws Exception _more_
     */
    public static URL getChannelUrl(String teamDomain, String channelId)
            throws Exception {
        return new URL(getTeamUrl(teamDomain) + "/" + channelId);
    }


    /**
     * _more_
     *
     * @param what _more_
     *
     * @return _more_
     */
    public static String in(String what) {
        return "in:" + what;
    }



}
