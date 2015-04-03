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
public class SlackHarvester extends Harvester {


    /** _more_ */
    public static final String CMD_SEARCH = "search";

    /** _more_          */
    public static final String CMD_PWD = "pwd";

    /** _more_          */
    public static final String CMD_LS = "ls";

    /** _more_          */
    public static final String CMD_CD = "cd";

    /** _more_          */
    public static final String CMD_NEW = "new";

    /** _more_          */
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



    /** _more_ */
    public static final String ATTR_TOKENS = "tokens";

    /** _more_ */
    public static final String ATTR_WEBHOOK = "webhook";


    /** _more_ */
    private String tokens;

    /** _more_ */
    private String webHook;


    /** _more_          */
    private Hashtable<String, String> cwd = new Hashtable<String, String>();


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
                                          : webHook, HtmlUtils.SIZE_70)));

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
            if (Misc.equals(token, tokenFromSlack)) {
                ok = true;

                break;
            }
        }

        if ( !ok) {
            return null;
        }

        System.err.println("slack request: " + request);
        String       text = getSlackText(request);
        List<String> toks = StringUtil.splitUpTo(text, " ", 2);

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
        } else if (cmd.equals(CMD_LS)) {
            return processLs(request, rest);
        } else if (cmd.equals(CMD_PWD)) {
            return processPwd(request, rest);
        } else if (cmd.equals(CMD_NEW)) {
            return processNew(request, rest);
        } else if (cmd.equals(CMD_CD)) {
            return processCd(request, rest);
        } else {
            return getUsage(request, "Unknown command: " + cmd);
        }

        /*
        StringBuffer sb          = new StringBuffer();
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

        text = text.trim();
        if ( !Utils.stringDefined(text)) {
            return getUsage(request, "Need to specify search string");
        }
        request.put(ARG_TEXT, text);
        List[] pair = getEntryManager().getEntries(request);
        pair[0].addAll(pair[1]);
        StringBuffer sb = new StringBuffer();
        makeEntryLinks(request, sb, (List<Entry>) pair[0]);

        return makeResult(request, sb);
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
    private Entry getCurrentEntry(Request request) throws Exception {

        String currentId = cwd.get(getSlackUserId(request));
        if (currentId == null) {
            return getBaseGroup();
        }
        Entry entry = getEntryManager().getEntry(request, currentId);
        if (entry == null) {
            return getBaseGroup();
        }

        return entry;
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
    private Result processLs(Request request, String text) throws Exception {
        Entry        parent   = getCurrentEntry(request);
        StringBuffer sb       = new StringBuffer();
        List<Entry>  children = getEntryManager().getChildren(request,
                                    parent);
        makeEntryLinks(request, sb, children);
        if (children.size() == 0) {
            return new Result("", new StringBuffer("No children entries"));
        }

        return makeResult(request, sb);
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
    private Result processCd(Request request, String text) throws Exception {
        text = text.trim();
        Entry parent = getCurrentEntry(request);
        Entry newEntry = getEntryManager().getRelativeEntry(request, parent,
                             text);
        if (newEntry == null) {
            return new Result("", new StringBuffer("No such entry"));
        }
        if (text.length() == 0) {
            newEntry = getBaseGroup();
        }

        cwd.put(getSlackUserId(request), newEntry.getId());
        StringBuffer sb       = new StringBuffer();
        List<Entry>  children = new ArrayList<Entry>();
        children.add(newEntry);
        sb.append("ok\n");
        makeEntryLinks(request, sb, children);

        return makeResult(request, sb);
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
    private Result processPwd(Request request, String text) throws Exception {
        Entry        parent   = getCurrentEntry(request);
        StringBuffer sb       = new StringBuffer();
        List<Entry>  children = new ArrayList<Entry>();
        children.add(parent);
        sb.append("Current entry:\n");
        makeEntryLinks(request, sb, children);

        return makeResult(request, sb);
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
    private Result processNew(Request request, String text) throws Exception {
        Entry        parent = getCurrentEntry(request);
        StringBuffer sb     = new StringBuffer();

        //new folder name;

        List<String> toks = StringUtil.splitUpTo(text, " ", 2);
        if (toks.size() != 2) {
            return getUsage(request,
                            "new <folder|blog|wiki|note> name;description");
        }

        String type = toks.get(0);
        toks = StringUtil.splitUpTo(toks.get(1), ";", 2);
        String   name = toks.get(0);
        String   desc = (toks.size() > 1)
                        ? toks.get(1)
                        : "";
        String[] cmds = { "folder", "wiki", "blog", "note" };
        String[] types = { TypeHandler.TYPE_GROUP, "wikipage", "blogentry",
                           "notes_note" };
        String theType = null;
        for (int i = 0; i < cmds.length; i++) {
            if (type.equals(cmds[i])) {
                theType = types[i];

                break;
            }
        }

        if (theType == null) {
            return getUsage(request,
                            "new <folder|blog|wiki|note> name;description");
        }

        List<Entry>  children = new ArrayList<Entry>();

        StringBuffer msg      = new StringBuffer();
        Entry entry = addEntry(request, parent, theType, name, desc, msg);
        if (entry == null) {
            return getUsage(request, msg.toString());
        }
        cwd.put(getSlackUserId(request), entry.getId());
        children.add(entry);
        sb.append("New entry:\n");
        makeEntryLinks(request, sb, children);

        return makeResult(request, sb);
    }


    /**
     * _more_
     *
     * @param request _more_
     * @param sb _more_
     * @param entries _more_
     *
     * @return _more_
     *
     * @throws Exception _more_
     */
    private Appendable makeEntryLinks(Request request, Appendable sb,
                                      List<Entry> entries)
            throws Exception {
        for (Entry entry : entries) {
            String url = request.getAbsoluteUrl(
                             request.entryUrl(
                                 getRepository().URL_ENTRY_SHOW, entry));
            sb.append("<" + url + "|" + entry.getName() + ">\n");
        }

        return sb;
    }


    /**
     * _more_
     *
     * @param request _more_
     * @param sb _more_
     *
     * @return _more_
     *
     * @throws Exception _more_
     */
    private Result makeResult(Request request, Appendable sb)
            throws Exception {
        if (Utils.stringDefined(webHook)) {
            StringBuilder json = new StringBuilder();
            List<String>  map  = new ArrayList<String>();
            map.add("text");
            map.add(Json.quote(sb.toString()));
            //            map.add(Json.quote("results"));
            map.add("username");
            map.add(Json.quote("RAMADDA"));
            if (request.defined(SLACK_CHANNEL_ID)) {
                map.add("channel");
                map.add(Json.quote(request.getString(SLACK_CHANNEL_ID, "")));
            }
            json.append(Json.map(map));
            List<HttpFormEntry> entries = new ArrayList<HttpFormEntry>();
            entries.add(HttpFormEntry.hidden(SLACK_PAYLOAD, json.toString()));
            String[] result = HttpFormEntry.doPost(entries, webHook);
            System.err.println("results:" + result[0] + " " + result[1]);

            return new Result("", new StringBuffer(""));
        }

        return new Result("", new StringBuffer(sb.toString()));
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
        sb.append("/ramadda search <search text>\nls\npwd");

        return new Result("", sb);
    }

    /**
     * _more_
     *
     * @param request _more_
     *
     * @return _more_
     */
    private String getSlackChannelId(Request request) {
        return request.getString(SLACK_CHANNEL_ID, "");
    }

    /**
     * _more_
     *
     * @param request _more_
     *
     * @return _more_
     */
    private String getSlackChannelName(Request request) {
        return request.getString(SLACK_CHANNEL_NAME, "");
    }

    /**
     * _more_
     *
     * @param request _more_
     *
     * @return _more_
     */
    private String getSlackUserId(Request request) {
        return request.getString(SLACK_USER_ID, "");
    }

    /**
     * _more_
     *
     * @param request _more_
     *
     * @return _more_
     */
    private String getSlackUserName(Request request) {
        return request.getString(SLACK_USER_NAME, "");
    }

    /**
     * _more_
     *
     * @param request _more_
     *
     * @return _more_
     */
    private String getSlackText(Request request) {
        return request.getString(SLACK_TEXT, "");
    }

    /**
     * _more_
     *
     * @param request _more_
     * @param parent _more_
     * @param type _more_
     * @param name _more_
     * @param desc _more_
     * @param msg _more_
     *
     * @return _more_
     *
     * @throws Exception _more_
     */
    private Entry addEntry(Request request, Entry parent, String type,
                           String name, String desc, Appendable msg)
            throws Exception {
        if ( !parent.isGroup()) {
            msg.append("ERROR: Not a folder:\n" + parent.getName());

            return null;
        }

        TypeHandler typeHandler = null;
        if (type != null) {
            typeHandler = getRepository().getTypeHandler(type);
        } else {
            typeHandler = getRepository().getTypeHandler("file");
        }
        Entry    entry  = typeHandler.createEntry(getRepository().getGUID());
        Date     date   = new Date();
        Object[] values = typeHandler.makeEntryValues(new Hashtable());
        if (type.equals("wikipage")) {
            values[0] = desc;
            desc      = "";
        }
        entry.initEntry(name, desc, parent, getUser(), new Resource(""), "",
                        date.getTime(), date.getTime(), date.getTime(),
                        date.getTime(), values);
        List<Entry> entries = (List<Entry>) Misc.newList(entry);
        getEntryManager().addNewEntries(getRequest(), entries);

        return entry;
    }



}
