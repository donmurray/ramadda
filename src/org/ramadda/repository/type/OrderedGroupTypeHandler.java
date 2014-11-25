/*
* Copyright 2008-2014 Geode Systems LLC
*
* Permission is hereby granted, free of charge, to any person obtaining a copy of this 
* software and associated documentation files (the "Software"), to deal in the Software 
* without restriction, including without limitation the rights to use, copy, modify, 
* merge, publish, distribute, sublicense, and/or sell copies of the Software, and to 
* permit persons to whom the Software is furnished to do so, subject to the following conditions:
* 
* The above copyright notice and this permission notice shall be included in all copies 
* or substantial portions of the Software.
* 
* THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, 
* INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR 
* PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE 
* FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR 
* OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER 
* DEALINGS IN THE SOFTWARE.
*/

package org.ramadda.repository.type;


import org.ramadda.repository.*;
import org.ramadda.repository.type.*;
import org.ramadda.util.HtmlUtils;

import org.ramadda.util.Utils;
import org.ramadda.util.WikiUtil;


import org.w3c.dom.*;

import ucar.unidata.util.IOUtil;
import ucar.unidata.util.Misc;
import ucar.unidata.util.StringUtil;
import ucar.unidata.xml.XmlUtil;


import java.io.*;

import java.util.ArrayList;

import java.util.Arrays;
import java.util.Comparator;
import java.util.Hashtable;
import java.util.List;
import java.util.Properties;


/**
 */
public abstract class OrderedGroupTypeHandler extends ExtensibleGroupTypeHandler {


    /** _more_ */
    public static final String ARG_MOVE_UP = "move.up";

    /** _more_ */
    public static final String ARG_MOVE_DOWN = "move.down";



    /**
     * _more_
     *
     * @param repository _more_
     * @param entryNode _more_
     *
     * @throws Exception on badness
     */
    public OrderedGroupTypeHandler(Repository repository, Element entryNode)
            throws Exception {
        super(repository, entryNode);
    }



    /**
     * _more_
     *
     * @return _more_
     */
    public abstract String getChildType();

    /**
     * _more_
     *
     * @return _more_
     */
    public String getListTitle() {
        return "Entries";
    }

    /**
     * _more_
     *
     * @param request _more_
     * @param entries _more_
     *
     * @return _more_
     */
    @Override
    public List<Entry> postProcessEntries(Request request,
                                          List<Entry> entries) {
        List<Entry> sorted =
            getEntryManager().getEntryUtil().sortEntriesOnField(entries,
                false, getChildType(), getSortIndex());

        return sorted;
    }

    /**
     * _more_
     *
     * @return _more_
     */
    public int getSortIndex() {
        return 0;
    }


    /**
     * _more_
     *
     * @param request _more_
     * @param parent _more_
     * @param entries _more_
     * @param sb _more_
     *
     *
     * @throws Exception on badness
     */
    public void addListForm(Request request, Entry parent,
                            List<Entry> entries, Appendable sb)
            throws Exception {


        sb.append(request.form(getRepository().URL_ENTRY_ACCESS));
        sb.append(HtmlUtils.hidden(ARG_ENTRYID, parent.getId()));
        if (entries.size() > 0) {
            sb.append(msgHeader(getListTitle()));
        }
        sb.append(HtmlUtils.formTable());
        int cnt = 0;
        for (Entry entry : entries) {
            sb.append("<tr valign=top><td>");
            if (cnt > 0) {
                sb.append(HtmlUtils.submitImage(iconUrl(ICON_UPARROW),
                                                ARG_MOVE_UP + "." + entry.getId(), msg("Move up"),""));
            }
            sb.append("</td><td>");
            if (cnt < entries.size() - 1) {
                sb.append(HtmlUtils.submitImage(iconUrl(ICON_DOWNARROW),
                                                ARG_MOVE_DOWN + "." + entry.getId(), msg("Move down"),""));
            }
            sb.append("</td><td>");
            cnt++;

            String url = null;
            if(getAccessManager().canEditEntry(request, entry)) {
                url = request.entryUrl(getRepository().URL_ENTRY_FORM, entry);
            }
            EntryLink link = getEntryManager().getAjaxLink(request, entry,
                                                           getEntryDisplayName(entry), url);
            addListLink(request, entry, link, sb);
            sb.append("</td></tr>");
        }

        sb.append(HtmlUtils.formTableClose());
        sb.append(HtmlUtils.p());
        List<String> buttons = new ArrayList<String>();
        addEntryButtons(request, parent, buttons);
        sb.append(HtmlUtils.buttons(buttons));
        sb.append(HtmlUtils.formClose());
    }

    /**
     * _more_
     *
     * @param request _more_
     * @param entry _more_
     * @param link _more_
     * @param sb _more_
     *
     * @throws Exception _more_
     */
    public void addListLink(Request request, Entry entry, EntryLink link,
                            Appendable sb)
            throws Exception {
        sb.append(link.getLink());
        sb.append(link.getFolderBlock());
    }


    /**
     * _more_
     *
     * @param request _more_
     * @param entry _more_
     * @param buttons _more_
     */
    public void addEntryButtons(Request request, Entry entry,
                                List<String> buttons) {}


    /**
     * _more_
     *
     * @param request _more_
     * @param entry _more_
     *
     * @return _more_
     *
     * @throws Exception on badness
     */
    public List<Entry> getChildrenEntries(Request request, Entry entry)
            throws Exception {
        return getEntryUtil().getEntriesWithType(
            getEntryManager().getChildrenAll(request, entry), getChildType());

    }


    /**
     * _more_
     *
     * @param request _more_
     * @param entry _more_
     *
     * @return _more_
     *
     * @throws Exception on badness
     */
    @Override
    public Result processEntryAccess(Request request, Entry entry)
            throws Exception {


        if ( !getEntryManager().canAddTo(request, entry)) {
            return null;
        }

        List<Entry> children = getChildrenEntries(request, entry);
        boolean     didMove  = false;
        for (int i = 0; i < children.size(); i++) {
            Entry child = children.get(i);
            if ( !child.getTypeHandler().isType(getChildType())) {
                continue;
            }
            if (request.exists(ARG_MOVE_UP + "." + child.getId() + ".x")) {
                didMove = true;
                if (i > 0) {
                    children.remove(child);
                    children.add(i - 1, child);
                }

                break;
            }
            if (request.exists(ARG_MOVE_DOWN + "." + child.getId() + ".x")) {
                didMove = true;
                if (i < children.size() - 1) {
                    children.remove(child);
                    children.add(i + 1, child);
                }

                break;
            }
        }

        if (didMove) {
            int index = 0;
            for (int i = 0; i < children.size(); i++) {
                Entry child = children.get(i);
                index++;
                child.getTypeHandler().setEntryValue(child, 0,
                        new Integer(index));
            }
            getEntryManager().updateEntries(request, children);
        }


        String url = request.entryUrl(getRepository().URL_ENTRY_SHOW, entry);

        return new Result(url);
    }




}
