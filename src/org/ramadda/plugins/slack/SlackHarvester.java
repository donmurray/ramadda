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
    private String tokens;

    /** _more_ */
    private String webHook;


    /** _more_ */
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
        tokens = Utils.getAttributeOrTag(element, SlackUtil.ATTR_TOKENS,
                                         tokens);
        webHook = XmlUtil.getAttribute(element, SlackUtil.ATTR_WEBHOOK,
                                       webHook);
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
            Element node = XmlUtil.create(SlackUtil.ATTR_TOKENS, element);
            node.appendChild(XmlUtil.makeCDataNode(node.getOwnerDocument(),
                    tokens, false));
            //            element.setAttribute(SlackUtil.ATTR_TOKENS, tokens);
        }
        if (webHook != null) {
            element.setAttribute(SlackUtil.ATTR_WEBHOOK, webHook);
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
        tokens  = request.getString(SlackUtil.ATTR_TOKENS, tokens);
        webHook = request.getString(SlackUtil.ATTR_WEBHOOK, webHook);
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
        sb.append(
            HtmlUtils.formEntry(
                msgLabel("Slack Tokens"),
                HtmlUtils.textArea(SlackUtil.ATTR_TOKENS, (tokens == null)
                ? ""
                : tokens, 4, 60) + " " + "Tokens from Slack. One per line"));

        sb.append(HtmlUtils.formEntry(msgLabel("Slack Web Hook URL"),
                                      HtmlUtils.input(SlackUtil.ATTR_WEBHOOK,
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

        String tokenFromSlack = request.getString(SlackUtil.SLACK_TOKEN,
                                    "none");
        boolean ok = false;
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
        String       text = SlackUtil.getSlackText(request);
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
        if (cmd.equals(SlackUtil.CMD_SEARCH)) {
            return processSearch(request, rest);
        } else if (cmd.equals(SlackUtil.CMD_LS)) {
            return processLs(request, rest);
        } else if (cmd.equals(SlackUtil.CMD_PWD)) {
            return processPwd(request, rest);
        } else if (cmd.equals(SlackUtil.CMD_DESC)) {
            return processDesc(request, rest);
        } else if (cmd.equals(SlackUtil.CMD_NEW)) {
            return processNew(request, rest);
        } else if (cmd.equals(SlackUtil.CMD_CD)) {
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

        return SlackUtil.makeEntryResult(getRepository(), request,
                                         "Search Results",
                                         (List<Entry>) pair[0], webHook);
    }


    private Result processDesc(Request request, String text)
            throws Exception {
        Entry        entry = getCurrentEntry(request);
        if (entry == null) {
            return getUsage(request, "No current entry");
        }
        return SlackUtil.makeEntryResult(getRepository(), request,
                                         entry.getDescription(),
                                         null, webHook);
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

        String currentId = cwd.get(SlackUtil.getSlackUserId(request));
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
        String attach = SlackUtil.makeEntryLinks(getRepository(), request,
                            sb, children);
        if (children.size() == 0) {
            return new Result("", new StringBuffer("No children entries"));
        }

        return SlackUtil.makeEntryResult(getRepository(), request, "Listing",
                                         children, webHook);
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
        Entry newEntry = getEntryManager().getRelativeEntry(request, getBaseGroup(), parent,  text);
        if (newEntry == null) {
            return new Result("", new StringBuffer("No such entry"));
        }
        if (text.length() == 0) {
            newEntry = getBaseGroup();
        }

        cwd.put(SlackUtil.getSlackUserId(request), newEntry.getId());
        return SlackUtil.makeEntryResult(getRepository(), request,
                                         "Current entry:", toList(newEntry), webHook);
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
        Entry        entry   = getCurrentEntry(request);
        return SlackUtil.makeEntryResult(getRepository(), request,
                                         "Current entry:", toList(entry), webHook);
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
        desc = desc.replace("\\n","\n");
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



        StringBuffer msg      = new StringBuffer();
        Entry entry = addEntry(request, parent, theType, name, desc, msg);
        if (entry == null) {
            return getUsage(request, msg.toString());
        }
        cwd.put(SlackUtil.getSlackUserId(request), entry.getId());
        return SlackUtil.makeEntryResult(getRepository(), request,
                                         "New entry:", toList(entry), webHook);
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
            "New:\n/ramadda new (folder|wiki|blog|note) Name of entry; Optional description\n");

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

    private List<Entry> toList(Entry entry) {
        List<Entry>  l = new ArrayList<Entry>();
        l.add(entry);
        return l;
    }

}
