/*
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
import org.ramadda.util.FileInfo;
import org.ramadda.util.Utils;

import org.json.*;


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


import org.apache.http.HttpEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.mime.MultipartEntityBuilder;
import org.apache.http.entity.mime.content.FileBody;
import org.apache.http.entity.mime.content.StringBody;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;



/**
 */
public class SlackHarvester extends Harvester {

    /** _more_ */
    public static final String ATTR_TOKENS = "tokens";

    /** _more_ */
    public static final String ATTR_WEBHOOK = "webhook";

    /** _more_ */
    public static final String ATTR_APITOKEN = "apitokenk";

    /** _more_ */
    public static final String ATTR_ALLOW_CREATE = "allow_create";


    /** _more_ */
    public static final String[] CMDS_SEARCH = { "search", "find" };

    /** _more_ */
    public static final String[] CMDS_PWD = { "pwd", "dir" };


    /** _more_ */
    public static final String[] CMDS_DESC = { "desc" };

    /** _more_ */
    public static final String[] CMDS_APPEND = { "append" };

    /** _more_ */
    public static final String[] CMDS_LS = { "ls", "dir" };

    /** _more_ */
    public static final String[] CMDS_CD = { "cd", "go" };

    /** _more_ */
    public static final String[] CMDS_NEW = { "new", "create" };

    /** _more_ */
    public static final String[] CMDS_GET = { "get" };

    /** _more_ */
    public static final String[] CMDS_VIEW = { "view" };


    /** plain old command is a slack argument so we use ramadda_command */
    public static final String ARG_COMMAND = "ramadda_command";

    /** _more_ */
    public static final String ARG_TYPE = "ramadda_type";

    /** _more_ */
    private String tokens;

    /** _more_ */
    private String webHook;

    /** _more_ */
    private String apiToken;


    /** _more_ */
    private boolean allowCreate = false;


    /** _more_ */
    private Hashtable<String, SlackState> slackStates = new Hashtable<String,
                                                            SlackState>();



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
     * @param command _more_
     * @param commands _more_
     *
     * @return _more_
     */
    public boolean isCommand(String command, String[] commands) {
        if (command == null) {
            return false;
        }
        for (String s : commands) {
            if (s.equals(command)) {
                return true;
            }
        }

        return false;
    }

    /**
     * _more_
     *
     * @param msg _more_
     */
    public void debug(String msg) {
        System.err.println("SlackHarvester: " + msg);
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
        tokens      = Utils.getAttributeOrTag(element, ATTR_TOKENS, tokens);
        webHook     = XmlUtil.getAttribute(element, ATTR_WEBHOOK, webHook);
        apiToken    = XmlUtil.getAttribute(element, ATTR_APITOKEN, apiToken);
        allowCreate = XmlUtil.getAttribute(element, ATTR_ALLOW_CREATE, false);
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
        element.setAttribute(ATTR_ALLOW_CREATE, allowCreate + "");
        if (webHook != null) {
            element.setAttribute(ATTR_WEBHOOK, webHook);
        }
        if (apiToken != null) {
            element.setAttribute(ATTR_APITOKEN, apiToken);
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
        tokens      = request.getString(ATTR_TOKENS, tokens);
        webHook     = request.getString(ATTR_WEBHOOK, webHook);
        apiToken    = request.getString(ATTR_APITOKEN, apiToken);
        allowCreate = request.get(ATTR_ALLOW_CREATE, allowCreate);
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
                                          + "Slack /slash command tokens. One per line"));

        sb.append(
            HtmlUtils.formEntry(
                msgLabel("Slack Incoming Web Hook URL"),
                HtmlUtils.input(ATTR_WEBHOOK, (webHook == null)
                ? ""
                : webHook, HtmlUtils.SIZE_70)));

        sb.append(HtmlUtils.formEntry(msgLabel("Slack API Token"),
                                      HtmlUtils.password(ATTR_APITOKEN,
                                          (apiToken == null)
                                          ? ""
                                          : apiToken, HtmlUtils.SIZE_70)));

        sb.append(
            HtmlUtils.formEntry(
                "",
                HtmlUtils.checkbox(ATTR_ALLOW_CREATE, "true", allowCreate)
                + " "
                + msgLabel(
                    "Allow creating wiki pages, notes, blog posts, etc")));
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

        String tokenFromSlack = request.getString(Slack.SLACK_TOKEN, "none");
        //        debug("handleRequest");
        boolean ok = false;


        for (String token : StringUtil.split(tokens, "\n", true, true)) {
            if (Misc.equals(token, tokenFromSlack)) {
                //                debug("slack token:" +tokenFromSlack +" my:" + token);
                ok = true;

                break;
            }
        }

        if ( !ok) {
            debug("tokens did not match");

            return null;
        }

        String textFromSlack = Slack.getSlackText(request);
        List<String> commandToks = StringUtil.split(textFromSlack, ";",
                                       false, false);
        if (commandToks.size() == 0) {
            commandToks.add("");
        }
        debug("slack text:" + textFromSlack);
        debug("command toks:" + commandToks);
        // /cd foo; ls; new wiki name|description
        Result result = null;
        //debug("request:" + request);
        for (String commandTok : commandToks) {
            debug("command tok:" + commandTok);
            commandTok = commandTok.trim();
            if (commandTok.startsWith("/")) {
                commandTok = commandTok.substring(1);
            }
            String       text = commandTok;
            String       cmd  = request.getString(ARG_COMMAND, (String) null);
            List<String> toks;
            if (cmd == null) {
                toks = StringUtil.splitUpTo(text, " ", 2);
                if (toks.size() == 0) {
                    return getUsage(request, "No command given");
                }
                cmd = toks.get(0);
                toks.remove(0);
                if (toks.size() > 0) {
                    text = toks.get(0);
                } else {
                    text = "";
                }
            }

            debug("checking command:" + cmd);
            if (isCommand(cmd, CMDS_SEARCH)) {
                result = processSearch(request, text);
            } else if (isCommand(cmd, CMDS_LS)) {
                result = processLs(request, text);
            } else if (isCommand(cmd, CMDS_PWD)) {
                result = processPwd(request, text);
            } else if (isCommand(cmd, CMDS_DESC)) {
                result = processDesc(request, text);
            } else if (isCommand(cmd, CMDS_GET)) {
                result = processGet(request, text);
            } else if (isCommand(cmd, CMDS_VIEW)) {
                result = processView(request, text);
            } else if (isCommand(cmd, CMDS_APPEND)) {
                result = processAppend(request, text);
            } else if (isCommand(cmd, CMDS_NEW)) {
                if ( !allowCreate) {
                    return message(
                        "Sorry, but creating new entries is not allowed");
                }
                result = processNew(request, text);
            } else if (isCommand(cmd, CMDS_CD)) {
                result = processCd(request, text);
            } else {
                result = getUsage(request, "Unknown command: " + cmd);
            }
            //Remove any default args
            request.remove(ARG_COMMAND);
            request.remove(ARG_TYPE);
        }

        //TODO: 
        if (result != null) {
            return result;
        }

        return getUsage(request, "Unknown command: " + textFromSlack);

    }


    /**
     * _more_
     *
     * @param msg _more_
     *
     * @return _more_
     */
    private static Result message(String msg) {
        return new Result("", new StringBuilder(msg));
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

        return Slack.makeEntryResult(getRepository(), request,
                                     "Search Results", (List<Entry>) pair[0],
                                     webHook, false);
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
    public Slack.Args parseArgs(Request request, String text)
            throws Exception {
        System.err.println("ARGS: " + text);
        List<String> toks    = StringUtil.split(text, " ", true, true);
        Slack.Args   args    = new Slack.Args(toks, null);
        String       entryId = null;
        for (String tok : args.getArgs()) {
            System.err.println("TOK: " + tok);
            if (tok.equals("-l")) {
                args.setFlag(tok);
            } else {
                entryId = tok;
            }
        }
        if (entryId != null) {
            Entry entry = getEntryToUse(request, entryId);
            args.setEntry(entry);
        }

        return args;

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
    private Result processDesc(Request request, String text)
            throws Exception {
        Slack.Args args  = parseArgs(request, text);
        Entry      entry = args.getEntry();
        if (entry == null) {
            return getUsage(request, "No current entry");
        }

        String desc = "Description:" + entry.getDescription();
        if ( !Utils.stringDefined(desc)) {
            desc = "    ";
        }

        return Slack.makeEntryResult(getRepository(), request, desc, null,
                                     webHook, false);
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
    private Result processGet(Request request, String text) throws Exception {
        Slack.Args args  = parseArgs(request, text);
        Entry      entry = args.getEntry();
        if (entry == null) {
            return getUsage(request, "No current entry");
        }
        if ( !Utils.stringDefined(apiToken)) {
            return message("RAMADDA get command not enabled.");
        }
        if ( !entry.isFile()) {
            return message("RAMADDA entry not a file");
        }
        File file = new File(entry.getResource().getPath());
        //Cap at 10MB
        if (file.length() > 1000000 * 10) {
            return message("Sorry, for now too big of a file");
        }

        return sendFile(getRepository(), file, apiToken,
                        request.getString(Slack.SLACK_CHANNEL_ID, ""),
                        entry.getName(), entry.getDescription());

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
    private Result processView(Request request, String text)
            throws Exception {
        Slack.Args args  = parseArgs(request, text);
        Entry      entry = args.getEntry();
        if (entry == null) {
            return getUsage(request, "No current entry");
        }
        StringBuilder sb = new StringBuilder();
        sb.append("```");
        sb.append("Entry: " + entry.getName() + "\n");
        int len = sb.length();
        List<FileInfo> files = new ArrayList<FileInfo>();
        entry.getTypeHandler().addEncoding(request, entry, "slack.view", args.getArgs(), sb, files);
        if (sb.length() == len) {
            if (entry.getResource().isImage()) {
                File file = new File(entry.getResource().getPath());

                return sendFile(getRepository(), file, apiToken,
                                request.getString(Slack.SLACK_CHANNEL_ID,
                                    ""), entry.getName(),
                                         entry.getDescription());
            }
        }

        sb.append("\n```");
        System.err.println(sb);

        return Slack.makeEntryResult(getRepository(), request, sb.toString(),
                                     null, webHook, true);

        //        return message(sb.toString());


    }


    /**
     * _more_
     *
     * @param s _more_
     * @param cnt _more_
     *
     * @return _more_
     */
    private String pad(String s, int cnt) {
        return StringUtil.padLeft(s, cnt);
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
    private Result processAppend(Request request, String text)
            throws Exception {
        Entry entry = getCurrentEntry(request);
        if (entry == null) {
            return getUsage(request, "No current entry");
        }

        getEntryManager().appendText(getRequest(), entry, text);

        return Slack.makeEntryResult(getRepository(), request,
                                     "Text appended", null, webHook, false);
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
        SlackState state = slackStates.get(getStateKey(request));
        if (state == null) {
            return getBaseGroup();
        }

        return state.entry;
    }

    /**
     * _more_
     *
     * @param request _more_
     *
     * @return _more_
     */
    private String getStateKey(Request request) {
        return Slack.getSlackUserId(request) + "_"
               + Slack.getSlackChannelName(request);
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
        Slack.Args  args     = parseArgs(request, text);
        Entry       parent   = args.getEntry();

        List<Entry> children = getEntryManager().getChildren(request, parent);
        if (children.size() == 0) {
            return message("No children entries");
        }

        return Slack.makeEntryResult(getRepository(), request, "Listing",
                                     children, webHook, false);
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
    private Entry getEntryToUse(Request request, String text)
            throws Exception {
        text = text.trim();
        //Check for an ID
        Entry newEntry = getEntryManager().getEntry(request, text);
        if (newEntry != null) {
            return newEntry;
        }

        Entry currentEntry = getCurrentEntry(request);
        newEntry = getEntryManager().getRelativeEntry(request,
                getBaseGroup(), currentEntry, text);
        if (newEntry != null) {
            return newEntry;
        }

        return currentEntry;
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
        Entry parent = getCurrentEntry(request);
        text = text.trim();
        Entry theEntry = getEntryManager().getRelativeEntry(request,
                             getBaseGroup(), parent, text);
        if (theEntry == null) {
            return message("No such entry");
        }
        if (text.length() == 0) {
            theEntry = getBaseGroup();
        }

        slackStates.put(getStateKey(request), new SlackState(theEntry));


        return Slack.makeEntryResult(getRepository(), request,
                                     "Current entry:", toList(theEntry),
                                     webHook, true);
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
        Entry entry = getCurrentEntry(request);

        return Slack.makeEntryResult(getRepository(), request,
                                     "Current entry:", toList(entry),
                                     webHook, true);
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

        List<String> toks;
        //new <type> <name>|<description>
        String type = request.getString(ARG_TYPE, (String) null);
        if (type == null) {
            toks = StringUtil.splitUpTo(text, " ", 2);
            if (toks.size() == 0) {
                return getUsage(
                    request,
                    "new <folder or blog or wiki or note> name|description");
            }
            type = toks.get(0);
            toks.remove(0);
            if (toks.size() > 0) {
                text = toks.get(0);
            } else {
                text = "";
            }
        }
        toks = StringUtil.splitUpTo(text, "|", 2);
        String name = toks.get(0);
        String desc = (toks.size() > 1)
                      ? toks.get(1)
                      : "";
        desc = desc.replace("\\n", "\n");
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



        StringBuffer msg = new StringBuffer();
        Entry entry      = addEntry(request, parent, theType, name, desc,
                                    msg);
        if (entry == null) {
            return getUsage(request, msg.toString());
        }
        slackStates.put(getStateKey(request), new SlackState(entry));

        return Slack.makeEntryResult(getRepository(), request, "New entry:",
                                     toList(entry), webHook, true);
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

        sb.append("Navigation:\n/ramadda ls or pwd or cd\n");
        sb.append("Search:\n/ramadda search <search text>\n");
        sb.append(
            "New:\n/ramadda new (folder or wiki or blog ornote) Name of entry | Optional description\n");

        return new Result("", sb);
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


    /**
     * _more_
     *
     * @param request _more_
     * @param entry _more_
     *
     * @return _more_
     *
     * @throws Exception _more_
     */
    private String getEntryUrl(Request request, Entry entry)
            throws Exception {
        return request.getAbsoluteUrl(
            request.entryUrl(getRepository().URL_ENTRY_SHOW, entry));
    }


    /**
     * Class description
     *
     *
     * @version        $version$, Fri, Apr 3, '15
     * @author         Enter your name here...
     */
    public static class SlackInfo {}

    /**
     * _more_
     *
     * @param entry _more_
     *
     * @return _more_
     */
    private List<Entry> toList(Entry entry) {
        List<Entry> l = new ArrayList<Entry>();
        l.add(entry);

        return l;
    }

    /**
     * Class description
     *
     *
     * @version        $version$, Sat, May 2, '15
     * @author         Enter your name here...
     */
    private static class SlackState {

        /** _more_ */
        private Entry entry;

        /**
         * _more_
         *
         * @param entry _more_
         */
        public SlackState(Entry entry) {
            this.entry = entry;
        }
    }

    /**
     * _more_
     *
     * @param repository _more_
     * @param file _more_
     * @param apiToken _more_
     * @param channel _more_
     * @param title _more_
     * @param desc _more_
     *
     * @return _more_
     *
     * @throws Exception _more_
     */
    public static Result sendFile(Repository repository, File file,
                                  String apiToken, String channel,
                                  String title, String desc)
            throws Exception {
        CloseableHttpClient   client   = HttpClients.createDefault();
        CloseableHttpResponse response = null;

        try {
            HttpPost post =
                new HttpPost(Slack.getSlackApiUrl(Slack.API_FILES_UPLOAD));
            FileBody               filePart = new FileBody(file);
            MultipartEntityBuilder mpe      = MultipartEntityBuilder.create();

            mpe.addPart(Slack.ARG_FILE, filePart);
            mpe.addPart(
                Slack.ARG_FILENAME,
                new StringBody(
                    repository.getStorageManager().getOriginalFilename(
                        file.getName()), ContentType.TEXT_PLAIN));
            mpe.addPart(Slack.ARG_TOKEN,
                        new StringBody(apiToken, ContentType.TEXT_PLAIN));
            mpe.addPart(Slack.ARG_TITLE,
                        new StringBody(title, ContentType.TEXT_PLAIN));
            mpe.addPart(Slack.ARG_INITIAL_COMMENT,
                        new StringBody(desc, ContentType.TEXT_PLAIN));
            mpe.addPart(Slack.ARG_CHANNELS, new StringBody(channel));

            HttpEntity requestEntity = mpe.build();
            post.setEntity(requestEntity);
            //            System.out.println("executing request " + post.getRequestLine());
            response = client.execute(post);
            System.out.println(response.getStatusLine());
            HttpEntity entity = response.getEntity();
            if (entity == null) {
                return message("Error: no http entity?");
            }
            String json = EntityUtils.toString(entity);
            EntityUtils.consume(entity);
            System.err.println("post response:" + json);
            try {
                JSONObject obj = new JSONObject(json);
                if ( !Json.readValue(obj, Slack.JSON_OK,
                                     "false").equals("true")) {
                    String error = Json.readValue(obj, Slack.JSON_ERROR, "");

                    return message("Oops, got an error posting the file:"
                                   + error);
                }

                return message("Ok, file is on its way");
            } catch (Exception exc) {
                return message("Error:" + json);
            }
        } finally {
            if (response != null) {
                response.close();
            }
            client.close();
        }
    }



}
