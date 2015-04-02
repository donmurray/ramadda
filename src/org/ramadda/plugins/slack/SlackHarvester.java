/**
* Copyright (c) 2008-2015 Geode Systems LLC
* This Software is licensed under the Geode Systems RAMADDA License available in the source distribution in the file 
* ramadda_license.txt. The above copyright notice shall be included in all copies or substantial portions of the Software.
*/

package org.ramadda.plugins.slack;


import org.ramadda.repository.*;
import org.ramadda.repository.auth.*;
import org.ramadda.repository.harvester.*;
import org.ramadda.repository.metadata.*;
import org.ramadda.repository.type.*;

import org.ramadda.util.HtmlUtils;
import org.ramadda.util.Json;
import org.ramadda.util.Utils;


import org.w3c.dom.*;

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
public class SlackHarvester extends Harvester {


    /** _more_          */
    public static final String CMD_SEARCH = "search";

    /** _more_          */
    public static final String SLACK_TOKEN = "token";

    /** _more_          */
    public static final String SLACK_TEAM_ID = "team_id";

    /** _more_          */
    public static final String SLACK_TEAM_DOMAIN = "team_domain";

    /** _more_          */
    public static final String SLACK_CHANNEL_ID = "channel_id";

    /** _more_          */
    public static final String SLACK_CHANNEL_NAME = "channel_name";

    /** _more_          */
    public static final String SLACK_TIMESTAMP = "timestamp";

    /** _more_          */
    public static final String SLACK_USER_ID = "user_id";

    /** _more_          */
    public static final String SLACK_USER_NAME = "user_name";

    /** _more_          */
    public static final String SLACK_TEXT = "text";

    /** _more_          */
    public static final String SLACK_TRIGGER_WORD = "trigger_word";



    /** _more_ */
    public static final String ATTR_TOKENS = "tokens";

    /** _more_          */
    public static final String ATTR_WEBHOOK = "webhook";


    /** _more_ */
    private String tokens;

    /** _more_          */
    private String webHook;



    /**
     * _more_
     *
     * @param repository _more_
     * @param id _more_
     *
     * @throws Exception _more_
     */
    public SlackHarvester(Repository repository, String id) throws Exception {
        super(repository, id);
    }


    /**
     * _more_
     *
     * @param repository _more_
     * @param node _more_
     *
     * @throws Exception _more_
     */
    public SlackHarvester(Repository repository, Element node)
            throws Exception {
        super(repository, node);
    }



    /**
     * _more_
     *
     * @param element _more_
     *
     * @throws Exception _more_
     */
    protected void init(Element element) throws Exception {
        super.init(element);
        tokens  = Utils.getAttributeOrTag(element, ATTR_TOKENS, tokens);
        webHook = XmlUtil.getAttribute(element, ATTR_WEBHOOK, webHook);
    }



    /**
     * _more_
     *
     * @return _more_
     */
    public String getDescription() {
        return "Slack Harvester";
    }



    /**
     * _more_
     *
     * @param element _more_
     *
     * @throws Exception _more_
     */
    public void applyState(Element element) throws Exception {
        super.applyState(element);
        if (tokens != null) {
            Element node = XmlUtil.create(ATTR_TOKENS, element);
            node.appendChild(XmlUtil.makeCDataNode(node.getOwnerDocument(),
                    tokens, false));
            //            element.setAttribute(ATTR_TOKENS, tokens);
        }
        if (webHook != null) {
            element.setAttribute(ATTR_WEBHOOK, webHook);
        }
    }


    /**
     * _more_
     *
     * @param request _more_
     *
     * @throws Exception _more_
     */
    public void applyEditForm(Request request) throws Exception {
        super.applyEditForm(request);
        tokens  = request.getString(ATTR_TOKENS, tokens);
        webHook = request.getString(ATTR_WEBHOOK, webHook);
    }


    /**
     * _more_
     *
     * @param request _more_
     * @param sb _more_
     *
     * @throws Exception _more_
     */
    public void createEditForm(Request request, StringBuffer sb)
            throws Exception {
        super.createEditForm(request, sb);

        addBaseGroupSelect(ATTR_BASEGROUP, sb);
        sb.append(HtmlUtils.formEntry(msgLabel("Slack Tokens"),
                                      HtmlUtils.textArea(ATTR_TOKENS,
                                          (tokens == null)
                                          ? ""
                                          : tokens, 4, 60) + " "
                                          + "Tokens from Slack. One per line"));

        sb.append(HtmlUtils.formEntry(msgLabel("Slack Web Hook URL"),
                                      HtmlUtils.input(ATTR_WEBHOOK,
                                          (webHook == null)
                                          ? ""
                                          : webHook, HtmlUtils.SIZE_60)));

    }


    /**
     * _more_
     *
     * @param request _more_
     *
     * @return _more_
     *
     * @throws Exception _more_
     */
    public Result handleRequest(Request request) throws Exception {
        if ( !Utils.stringDefined(tokens)) {
            return null;
        }

        String  tokenFromSlack = request.getString(SLACK_TOKEN, "none");
        boolean ok             = false;
        for (String token : StringUtil.split(tokens, "\n", true, true)) {
            System.err.println("token:" + token + " " + tokenFromSlack);
            if (Misc.equals(token, tokenFromSlack)) {
                ok = true;

                break;
            }
        }

        if ( !ok) {
            return null;
        }

        System.err.println("slack request: " + request);
        String       channelId   = request.getString(SLACK_CHANNEL_ID, "");
        String       channelName = request.getString(SLACK_CHANNEL_NAME, "");
        String       userId      = request.getString(SLACK_USER_ID, "");
        String       userName    = request.getString(SLACK_USER_NAME, "");
        String       text        = request.getString(SLACK_TEXT, "");
        StringBuffer sb          = new StringBuffer();
        List<String> toks        = StringUtil.splitUpTo(text, " ", 2);

        if (toks.size() < 1) {
            return getUsage(request, "No command given");
        }

        String rest = "";
        if (toks.size() == 2) {
            rest = toks.get(1);
        }
        String cmd = toks.get(0);
        System.err.println("command:" + cmd);
        if (cmd.equals(CMD_SEARCH)) {
            return processSearch(request, rest);
        } else {
            return getUsage(request, "Unknown command: " + cmd);
        }

        /*
        if(Utils.stringDefined(webHook)) {
        }

        return new Result("", sb);
        */
    }

    /**
     * _more_
     *
     * @param request _more_
     * @param text _more_
     *
     * @return _more_
     *
     * @throws Exception _more_
     */
    private Result processSearch(Request request, String text)
            throws Exception {
        StringBuffer sb = new StringBuffer();
        text = text.trim();
        if ( !Utils.stringDefined(text)) {
            return getUsage(request, "Need to specify search string");
        }
        request.put(ARG_TEXT, text);
        List[] pair = getEntryManager().getEntries(request);
        pair[0].addAll(pair[1]);
        for (Entry entry : ((List<Entry>) pair[0])) {
            String url = request.getAbsoluteUrl(
                             request.entryUrl(
                                 getRepository().URL_ENTRY_SHOW, entry));
            sb.append("<" + url + "|" + entry.getName() + ">\n");
        }
        sb.append(Json.map("text", Json.quote("results")));

        return new Result("", sb);
    }

    /**
     * _more_
     *
     * @param request _more_
     * @param msg _more_
     *
     * @return _more_
     *
     * @throws Exception _more_
     */
    private Result getUsage(Request request, String msg) throws Exception {
        StringBuffer sb = new StringBuffer();
        if (Utils.stringDefined(msg)) {
            sb.append(msg);
            sb.append("\n");
        }
        sb.append(
            "/ramadda [search <search text> or new]\n<http://ramaddaorg|go to ramadda>\nmore stuff");

        return new Result("", sb);
    }


}
