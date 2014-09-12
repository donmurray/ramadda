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

package org.ramadda.repository.output;


import org.ramadda.repository.*;
import org.ramadda.repository.auth.*;
import org.ramadda.repository.metadata.*;
import org.ramadda.repository.type.*;


import org.ramadda.sql.SqlUtil;
import org.ramadda.util.CategoryBuffer;
import org.ramadda.util.HtmlUtils;
import org.ramadda.util.JQuery;
import org.ramadda.util.Json;
import org.ramadda.util.TTLCache;


import org.ramadda.util.Utils;


import org.w3c.dom.*;

import ucar.unidata.util.DateUtil;
import ucar.unidata.util.IOUtil;
import ucar.unidata.util.Misc;


import ucar.unidata.util.StringUtil;
import ucar.unidata.util.TwoFacedObject;
import ucar.unidata.xml.XmlUtil;


import java.io.*;

import java.io.File;


import java.net.*;



import java.text.SimpleDateFormat;

import java.util.ArrayList;
import java.util.Date;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.List;
import java.util.Properties;



import java.util.regex.*;

import java.util.zip.*;


/**
 *
 *
 *
 * @author RAMADDA Development Team
 * @version $Revision: 1.3 $
 */
public class HtmlOutputHandler extends OutputHandler {

    /** _more_ */
    public static final OutputType OUTPUT_TEST =
        new OutputType("test", "html.test",
                       OutputType.TYPE_VIEW | OutputType.TYPE_FORSEARCH, "",
                       ICON_DATA);


    /** _more_ */
    public static final OutputType OUTPUT_GRID =
        new OutputType("Grid Layout", "html.grid",
                       OutputType.TYPE_VIEW | OutputType.TYPE_FORSEARCH, "",
                       ICON_DATA);

    /** _more_ */
    public static final OutputType OUTPUT_TREEVIEW =
        new OutputType("Tree View", "html.treeview", OutputType.TYPE_VIEW,
                       "", "/icons/application_side_tree.png");

    /** _more_ */
    public static final OutputType OUTPUT_INFO =
        new OutputType("Information", "html.info", OutputType.TYPE_VIEW, "",
                       ICON_INFORMATION);



    /*
    public static final OutputType OUTPUT_LISTING =
        new OutputType("Tree View", "html.listing",
                       OutputType.TYPE_VIEW , "",
                       "/icons/application_side_tree.png");
    */


    /** _more_ */
    public static final OutputType OUTPUT_GRAPH =
        new OutputType("Graph", "default.graph",
                       OutputType.TYPE_VIEW | OutputType.TYPE_FORSEARCH, "",
                       ICON_GRAPH);

    /** _more_ */
    public static final OutputType OUTPUT_TABLE =
        new OutputType("Tabular Layout", "html.table",
                       OutputType.TYPE_VIEW | OutputType.TYPE_FORSEARCH, "",
                       ICON_TABLE);

    /** _more_ */
    public static final OutputType OUTPUT_CLOUD = new OutputType("Cloud",
                                                      "default.cloud",
                                                      OutputType.TYPE_VIEW);

    /** _more_ */
    public static final OutputType OUTPUT_INLINE =
        new OutputType("inline", OutputType.TYPE_INTERNAL);

    /** _more_ */
    public static final OutputType OUTPUT_MAPINFO =
        new OutputType("mapinfo", OutputType.TYPE_INTERNAL);

    /** _more_ */
    public static final OutputType OUTPUT_SELECTXML =
        new OutputType("selectxml", OutputType.TYPE_INTERNAL);


    /** _more_ */
    public static final OutputType OUTPUT_METADATAXML =
        new OutputType("metadataxml", OutputType.TYPE_INTERNAL);

    /** _more_ */
    public static final OutputType OUTPUT_LINKSXML =
        new OutputType("linksxml", OutputType.TYPE_INTERNAL);


    /** _more_ */
    public static final String ATTR_WIKI_SECTION = "wiki-section";

    /** _more_ */
    public static final String ATTR_WIKI_URL = "wiki-url";



    /** _more_ */
    public static final String ASSOCIATION_LABEL = "Connections";




    /**
     * _more_
     *
     * @param repository _more_
     * @param element _more_
     * @throws Exception _more_
     */
    public HtmlOutputHandler(Repository repository, Element element)
            throws Exception {
        super(repository, element);
        addType(OUTPUT_HTML);
        addType(OUTPUT_INFO);
        addType(OUTPUT_TABLE);
        addType(OUTPUT_GRID);
        addType(OUTPUT_TREEVIEW);
        addType(OUTPUT_GRAPH);
        addType(OUTPUT_INLINE);
        addType(OUTPUT_MAPINFO);
        addType(OUTPUT_SELECTXML);
        addType(OUTPUT_METADATAXML);
        addType(OUTPUT_LINKSXML);
        //        addType(OUTPUT_TEST);
    }




    /**
     * _more_
     *
     * @return _more_
     */
    @Override
    public boolean allowRobots() {
        return true;
    }

    /**
     * _more_
     *
     * @param request _more_
     * @param entry _more_
     *
     * @return _more_
     */
    public String getHtmlHeader(Request request, Entry entry) {
        if (entry.isDummy() || !entry.isGroup()) {
            return "";
        }

        //        return makeHtmlHeader(request, entry, "Layout");
        return makeHtmlHeader(request, entry, "");
    }


    /**
     * _more_
     *
     * @param request _more_
     * @param entry _more_
     * @param title _more_
     *
     * @return _more_
     */
    public String makeHtmlHeader(Request request, Entry entry, String title) {
        OutputType[] types = new OutputType[] { OUTPUT_INFO, OUTPUT_TABLE,
        /*OUTPUT_GRID,*/
        OUTPUT_TREEVIEW, CalendarOutputHandler.OUTPUT_TIMELINE,
                CalendarOutputHandler.OUTPUT_CALENDAR };
        StringBuffer sb =
            new StringBuffer(
                "<table border=0 cellspacing=0 cellpadding=0><tr>");
        String selected = request.getString(ARG_OUTPUT, OUTPUT_INFO.getId());
        if (title.length() > 0) {
            sb.append("<td align=center>" + msgLabel(title) + "</td>");
        }
        for (OutputType output : types) {
            String link = HtmlUtils.href(
                              request.entryUrl(
                                  getRepository().URL_ENTRY_SHOW, entry,
                                  ARG_OUTPUT, output), HtmlUtils.img(
                                      iconUrl(output.getIcon()),
                                      output.getLabel()));
            sb.append("<td align=center>");
            if (output.getId().equals(selected)) {
                sb.append(
                    HtmlUtils.div(
                        link, HtmlUtils.cssClass("toolbar-selected")));
            } else {
                sb.append(HtmlUtils.div(link, HtmlUtils.cssClass("toolbar")));
            }
            sb.append(" ");
            sb.append("</td>");
        }
        sb.append("</table>");

        return "<table border=0 cellspacing=0 cellpadding=0 width=100%><tr><td align=right>"
               + sb.toString() + "</td></tr></table>";
    }


    /**
     * _more_
     *
     * @param request _more_
     * @param state _more_
     * @param links _more_
     *
     *
     * @throws Exception _more_
     */
    public void getEntryLinks(Request request, State state, List<Link> links)
            throws Exception {
        List<Entry> entries = state.getAllEntries();
        if (state.getEntry() != null) {
            links.add(makeLink(request, state.getEntry(), OUTPUT_HTML));
            links.add(makeLink(request, state.getEntry(), OUTPUT_INFO));
            if (entries.size() > 1) {
                links.add(makeLink(request, state.getEntry(), OUTPUT_TABLE));
                links.add(makeLink(request, state.getEntry(),
                                   OUTPUT_TREEVIEW));
                //                links.add(makeLink(request, state.getEntry(), OUTPUT_TEST));
                //                links.add(makeLink(request, state.getEntry(), OUTPUT_GRID));
            }
        }
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
    public Result getMapInfo(Request request, Entry entry) throws Exception {
        String html = null;
        Result typeResult = entry.getTypeHandler().getHtmlDisplay(request,
                                entry);
        if (typeResult != null) {
            byte[] content = typeResult.getContent();
            if (content != null) {
                html = new String(content);
            }
        }

        if (html == null) {
            String wikiTemplate = getWikiText(request, entry);
            if (wikiTemplate != null) {
                String wiki = getWikiManager().wikifyEntry(request, entry,
                                  wikiTemplate);
                html = getRepository().translate(request, wiki);
            } else {
                html = getMapManager().makeInfoBubble(request, entry);
            }
        }
        html = getRepository().translate(request, html);

        StringBuffer xml = new StringBuffer(XmlUtil.XML_HEADER);
        xml.append("\n<content>\n");
        XmlUtil.appendCdata(xml, html);
        xml.append("\n</content>");

        return new Result("", xml, "text/xml");
    }


    /**
     * _more_
     *
     * @param request _more_
     * @param entry _more_
     * @param showLinks _more_
     *
     * @return _more_
     *
     * @throws Exception _more_
     */
    private Result getMetadataXml(Request request, Entry entry,
                                  boolean showLinks)
            throws Exception {
        String contents;
        if (showLinks) {
            int menuType = OutputType.TYPE_VIEW | OutputType.TYPE_FILE
                           | OutputType.TYPE_EDIT | OutputType.TYPE_OTHER;
            contents = getEntryManager().getEntryActionsTable(request, entry,
                    menuType);
        } else {
            contents = getInformationTabs(request, entry, true);
        }
        StringBuffer xml = new StringBuffer("<content>\n");
        XmlUtil.appendCdata(xml,
                            getRepository().translate(request, contents));
        xml.append("\n</content>");

        return new Result("", xml, "text/xml");

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
    public Result getLinksXml(Request request, Entry entry) throws Exception {
        StringBuffer sb = new StringBuffer("<content>\n");
        String links = getEntryManager().getEntryActionsTable(request, entry,
                           OutputType.TYPE_ALL);
        StringBuffer inner = new StringBuffer();
        String cLink =
            HtmlUtils.jsLink(HtmlUtils.onMouseClick("hidePopupObject();"),
                             HtmlUtils.img(iconUrl(ICON_CLOSE)), "");
        inner.append(cLink);
        inner.append(HtmlUtils.br());
        inner.append(links);
        XmlUtil.appendCdata(sb, inner.toString());
        sb.append("\n</content>");

        return new Result("", sb, "text/xml");
    }




    /**
     * _more_
     *
     * @param request _more_
     * @param outputType _more_
     * @param entry _more_
     *
     * @return _more_
     *
     * @throws Exception _more_
     */
    public Result outputEntry(Request request, OutputType outputType,
                              Entry entry)
            throws Exception {
        TypeHandler typeHandler =
            getRepository().getTypeHandler(entry.getType());
        if (outputType.equals(OUTPUT_METADATAXML)) {
            return getMetadataXml(request, entry, true);
        }
        if (outputType.equals(OUTPUT_TEST)) {
            return outputTest(request, entry);
        }

        if (outputType.equals(OUTPUT_LINKSXML)) {
            return getLinksXml(request, entry);
        }
        if (outputType.equals(OUTPUT_MAPINFO)) {
            return getMapInfo(request, entry);
        }
        if (outputType.equals(OUTPUT_INLINE)) {
            String inline = typeHandler.getInlineHtml(request, entry);

            if (inline != null) {
                inline = getRepository().translate(request, inline);
                StringBuffer xml = new StringBuffer("<content>\n");
                XmlUtil.appendCdata(xml,
                                    "<div class=inline>" + inline + "</div>");
                xml.append("\n</content>");

                return new Result("", xml, "text/xml");
            }

            /**
             * String wikiTemplate = getWikiText(request, entry);
             * if (wikiTemplate == null) {
             *   wikiTemplate = getPageHandler().getWikiTemplate(request,
             *           entry, PageHandler.TEMPLATE_DEFAULT);
             * }
             *
             * if (wikiTemplate != null) {
             *   String wiki = getWikiManager().wikifyEntry(request, entry,
             *                     wikiTemplate);
             *   wiki = getRepository().translate(request, wiki);
             *   StringBuffer xml = new StringBuffer("<content>\n");
             *   XmlUtil.appendCdata(xml,
             *                       "<div class=entry-inline>" + wiki
             *                       + "</div>");
             *   xml.append("\n</content>");
             *
             *   return new Result("", xml, "text/xml");
             * }
             */
            return getMetadataXml(request, entry, false);
        }

        return getHtmlResult(request, outputType, entry);
    }


    /**
     * _more_
     *
     * @param request _more_
     * @param outputType _more_
     * @param entry _more_
     *
     * @return _more_
     *
     * @throws Exception _more_
     */
    public Result getHtmlResult(Request request, OutputType outputType,
                                Entry entry)
            throws Exception {
        return getHtmlResult(request, outputType, entry, true);
    }


    /**
     * _more_
     *
     * @param request _more_
     * @param outputType _more_
     * @param entry _more_
     * @param checkType _more_
     *
     * @return _more_
     *
     * @throws Exception _more_
     */
    public Result getHtmlResult(Request request, OutputType outputType,
                                Entry entry, boolean checkType)
            throws Exception {

        TypeHandler typeHandler = entry.getTypeHandler();
        if (checkType) {
            Result typeResult = typeHandler.getHtmlDisplay(request, entry);
            if (typeResult != null) {
                return typeResult;
            }
        }

        StringBuffer sb        = new StringBuffer();
        boolean      doingInfo = outputType.equals(OUTPUT_INFO);
        if (doingInfo) {
            StringBuffer suffix = new StringBuffer();
            addDescription(request, entry, sb, true, suffix);
            String informationBlock = getInformationTabs(request, entry,
                                          false);
            sb.append(informationBlock);
            sb.append(suffix);
        } else {
            handleDefaultWiki(request, entry, sb, null, null);
        }

        return makeLinksResult(request, msg("Entry"), sb, new State(entry));
    }


    /**
     * _more_
     *
     * @param request _more_
     * @param entry _more_
     * @param sb _more_
     * @param folders _more_
     * @param files _more_
     *
     * @throws Exception _more_
     */
    private void handleDefaultWiki(Request request, Entry entry,
                                   StringBuffer sb, List<Entry> folders,
                                   List<Entry> files)
            throws Exception {
        String wikiTemplate = getWikiText(request, entry);

        String innerContent = null;


        if ((wikiTemplate != null)
                && wikiTemplate.startsWith("<wiki_inner>")) {
            innerContent = wikiTemplate;
            wikiTemplate = null;
        }

        if (innerContent == null) {
            innerContent = entry.getTypeHandler().getWikiTemplateInner();
        }


        if (wikiTemplate == null) {
            wikiTemplate = getPageHandler().getWikiTemplate(request, entry,
                    PageHandler.TEMPLATE_DEFAULT);
        }
        if (wikiTemplate == null) {
            wikiTemplate = "=={{name}}=={{description}}{{information}}";
        }
        if (innerContent == null) {
            innerContent =
                entry.getTypeHandler().getInnerWikiContent(request, entry,
                    wikiTemplate);
        }

        //Check for 
        if (innerContent == null) {}

        if (innerContent == null) {
            innerContent = getPageHandler().getWikiTemplate(request, entry,
                    PageHandler.TEMPLATE_CONTENT);
        }
        if (innerContent == null) {}

        if (innerContent != null) {
            wikiTemplate = wikiTemplate.replace("${innercontent}",
                    innerContent);
        }


        if (files != null) {
            sb.append(getWikiManager().wikifyEntry(request, entry,
                    wikiTemplate, true, files, folders));
        } else {
            sb.append(getWikiManager().wikifyEntry(request, entry,
                    wikiTemplate));
        }
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
    public String getAttachmentsHtml(Request request, Entry entry)
            throws Exception {
        StringBuffer metadataSB = new StringBuffer();
        getMetadataManager().decorateEntry(request, entry, metadataSB, false);
        String metataDataHtml = metadataSB.toString();
        if (metataDataHtml.length() > 0) {
            return HtmlUtils.makeShowHideBlock(msg("Attachments"),
                    "<div class=\"description\">" + metadataSB + "</div>",
                    false);
        }

        return "";
    }

    /**
     * _more_
     *
     *
     * @param request _more_
     * @param entry _more_
     *
     * @return _more_
     */
    public String getEntryLink(Request request, Entry entry) {
        return getEntryManager().getEntryLink(request, entry);
    }







    /**
     * _more_
     *
     * @param output _more_
     *
     * @return _more_
     */
    public String getMimeType(OutputType output) {
        if (output.equals(OUTPUT_GRID) || output.equals(OUTPUT_TREEVIEW)
                || output.equals(OUTPUT_TABLE)) {
            return getRepository().getMimeTypeFromSuffix(".html");
        } else if (output.equals(OUTPUT_GRAPH)) {
            return getRepository().getMimeTypeFromSuffix(".xml");
        } else if (output.equals(OUTPUT_HTML) || output.equals(OUTPUT_INFO)) {
            return getRepository().getMimeTypeFromSuffix(".html");
        } else {
            return super.getMimeType(output);
        }
    }





    /**
     * _more_
     *
     * @param request _more_
     * @param entry _more_
     * @param onlyTheseTypes _more_
     * @param notTheseTypes _more_
     *
     * @return _more_
     *
     * @throws Exception _more_
     */
    public List<TwoFacedObject> getMetadataHtml(Request request, Entry entry,
            List<String> onlyTheseTypes, List<String> notTheseTypes)
            throws Exception {

        List<TwoFacedObject> result = new ArrayList<TwoFacedObject>();
        boolean showMetadata        = request.get(ARG_SHOWMETADATA, false);
        List<Metadata> metadataList = getMetadataManager().getMetadata(entry);
        if (metadataList.size() == 0) {
            return result;
        }

        CategoryBuffer catBuff = new CategoryBuffer();
        Hashtable      catMap  = new Hashtable();
        List<String>   cats    = new ArrayList<String>();
        List<MetadataHandler> metadataHandlers =
            getMetadataManager().getMetadataHandlers();

        boolean canEdit = getAccessManager().canDoAction(request, entry,
                              Permission.ACTION_EDIT);

        boolean smallDisplay = request.getString(ARG_DISPLAY,
                                   "").equals(DISPLAY_SMALL);
        boolean                    didone  = false;
        Hashtable<String, Boolean> typeRow = new Hashtable<String, Boolean>();

        for (Metadata metadata : metadataList) {
            if ((onlyTheseTypes != null) && (onlyTheseTypes.size() > 0)) {
                if ( !onlyTheseTypes.contains(metadata.getType())) {
                    continue;
                }
            }

            if ((notTheseTypes != null) && (notTheseTypes.size() > 0)) {
                if (notTheseTypes.contains(metadata.getType())) {
                    continue;
                }
            }

            MetadataType type = getRepository().getMetadataManager().findType(
                                    metadata.getType());
            if (type == null) {
                continue;
            }
            MetadataHandler metadataHandler = type.getHandler();
            String[] html = metadataHandler.getHtml(request, entry, metadata);
            if (html == null) {
                continue;
            }
            String         cat = type.getDisplayCategory();

            CategoryBuffer cb  = (CategoryBuffer) catMap.get(cat);
            if (cb == null) {
                cb = new CategoryBuffer();
                catMap.put(cat, cb);
                cats.add(cat);
            }



            String       group   = type.getDisplayGroup();
            StringBuffer sb      = cb.get(group);
            Boolean      rowFlag = typeRow.get(group);
            if (rowFlag == null) {
                rowFlag = new Boolean(true);
                typeRow.put(group, rowFlag);
            }
            boolean even = rowFlag.booleanValue();
            typeRow.put(group, new Boolean( !even));
            String  rowClass = "metadata-row-" + (even
                    ? "even"
                    : "odd");

            boolean first    = sb.length() == 0;

            if (smallDisplay) {
                sb.append(HtmlUtils.open("tr",
                                         " valign=\"top\" "
                                         + HtmlUtils.cssClass(rowClass)));
                sb.append(HtmlUtils.open("td"));
                sb.append(
                    HtmlUtils.tag(
                        "div", HtmlUtils.cssClass("metadata-small-label"),
                        html[0]));
                sb.append(
                    HtmlUtils.tag(
                        "div", HtmlUtils.cssClass("metadata-small-content"),
                        html[1]));
                sb.append(HtmlUtils.close("td"));
                sb.append(HtmlUtils.close("tr"));
            } else {
                if ( !first) {
                    sb.append("<div class=\"metadata-row-divider\"></div>");
                }
                sb.append(HtmlUtils.div(html[1],
                                        HtmlUtils.cssClass(rowClass)));
            }
            sb.append("\n");
        }


        for (String cat : cats) {
            CategoryBuffer cb = (CategoryBuffer) catMap.get(cat);
            StringBuffer   sb = new StringBuffer();
            for (String category : cb.getCategories()) {
                String header = HtmlUtils.div(category,
                                    HtmlUtils.cssClass("wiki-h2"));
                sb.append(header);
                sb.append(cb.get(category));
            }
            result.add(new TwoFacedObject(cat, sb));
        }

        return result;
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
    public Result getActionXml(Request request, Entry entry)
            throws Exception {
        StringBuffer sb = new StringBuffer();
        sb.append(getEntryManager().getEntryActionsTable(request, entry,
                OutputType.TYPE_ALL));

        StringBuffer xml = new StringBuffer("<content>\n");
        XmlUtil.appendCdata(xml,
                            getRepository().translate(request,
                                sb.toString()));
        xml.append("\n</content>");

        return new Result("", xml, "text/xml");
    }



    /**
     * _more_
     *
     * @param request _more_
     * @param parent _more_
     * @param subGroups _more_
     * @param entries _more_
     *
     * @return _more_
     *
     * @throws Exception _more_
     */
    public Result getChildrenXml(Request request, Entry parent,
                                 List<Entry> subGroups, List<Entry> entries)
            throws Exception {
        StringBuffer sb         = new StringBuffer();
        String       folder     = iconUrl(ICON_FOLDER_CLOSED);
        boolean      showLink   = request.get(ARG_SHOWLINK, true);
        boolean      onlyGroups = request.get(ARG_ONLYGROUPS, false);

        int          cnt        = 0;
        StringBuffer jsSB       = new StringBuffer();
        String       rowId;
        String       cbxId;
        String       cbxWrapperId;

        if ( !showingAll(request, subGroups, entries)) {
            sb.append(msgLabel("Showing") + " 1.."
                      + (subGroups.size() + entries.size()));
            sb.append(HtmlUtils.space(2));
            String url = request.getEntryUrl(
                             getRepository().URL_ENTRY_SHOW.toString(),
                             parent);
            url = HtmlUtils.url(url, ARG_ENTRYID, parent.getId());
            sb.append(HtmlUtils.href(url, msg("More...")));
            sb.append(HtmlUtils.br());
        }
        boolean showDetails = request.get(ARG_DETAILS, true);


        for (Entry subGroup : subGroups) {
            cnt++;
            addEntryTableRow(request, subGroup, sb, jsSB, showDetails);
        }


        if ( !onlyGroups) {
            for (Entry entry : entries) {
                cnt++;
                addEntryTableRow(request, entry, sb, jsSB, showDetails);
            }
        }


        if (cnt == 0) {
            parent.getTypeHandler().handleNoEntriesHtml(request, parent, sb);
            String tabs = getInformationTabs(request, parent, true);
            sb.append(tabs);
            //            sb.append(entry.getDescription());
            if (getAccessManager().hasPermissionSet(parent,
                    Permission.ACTION_VIEWCHILDREN)) {
                if ( !getAccessManager().canDoAction(request, parent,
                        Permission.ACTION_VIEWCHILDREN)) {
                    sb.append(HtmlUtils.space(1));
                    sb.append(
                        msg(
                        "You do not have permission to view the sub-folders of this entry"));
                }
            }
        }

        StringBuffer xml = new StringBuffer("<response><content>\n");
        XmlUtil.appendCdata(xml,
                            getRepository().translate(request,
                                sb.toString()));
        xml.append("\n</content>");

        xml.append("<javascript>");
        XmlUtil.appendCdata(xml,
                            getRepository().translate(request,
                                jsSB.toString()));
        xml.append("</javascript>");
        xml.append("\n</response>");

        return new Result("", xml, "text/xml");
    }


    /**
     * _more_
     *
     * @param request _more_
     * @param subGroups _more_
     * @param entries _more_
     *
     * @return _more_
     *
     * @throws Exception _more_
     */
    public Result getSelectXml(Request request, List<Entry> subGroups,
                               List<Entry> entries)
            throws Exception {

        String       localeId = request.getString(ARG_LOCALEID, null);

        String       target   = request.getString(ATTR_TARGET, "");
        StringBuffer sb       = new StringBuffer();
        boolean      didExtra = false;
        String sectionDivider =
            HtmlUtils.tag(
                "hr",
                HtmlUtils.style("padding:0px;margin:0px;margin-bottom:0px;"));


        boolean addExtra = false;
        //If we have a localeid that means this is the first call
        if (localeId != null) {
            addExtra = true;
            Entry localeEntry = getEntryManager().getEntry(request, localeId);
            if (localeEntry != null) {
                if ( !localeEntry.isGroup()) {
                    localeEntry = getEntryManager().getParent(request,
                            localeEntry);
                }
                if (localeEntry != null) {
                    sb.append(
                        HtmlUtils.open(
                            "div",
                            HtmlUtils.cssClass("ramadda-select-inner")));
                    Entry grandParent = getEntryManager().getParent(request,
                                            localeEntry);
                    String indent = "";
                    if (grandParent != null) {
                        sb.append(getSelectLink(request, grandParent,
                                target));
                        //indent = HtmlUtils.space(2);
                    }
                    sb.append(indent);
                    sb.append(getSelectLink(request, localeEntry, target));
                    localeId = localeEntry.getId();
                    sb.append(HtmlUtils.close("div"));
                    sb.append(sectionDivider);
                }
            }


        }



        if (request.get("firstclick", false)) {
            addExtra = true;
        }

        if (addExtra) {
            List<FavoriteEntry> favoritesList =
                getUserManager().getFavorites(request, request.getUser());


            if (favoritesList.size() > 0) {
                sb.append(
                    HtmlUtils.open(
                        "div", HtmlUtils.cssClass("ramadda-select-inner")));
                sb.append(HtmlUtils.center(HtmlUtils.b(msg("Favorites"))));
                List favoriteLinks = new ArrayList();
                for (FavoriteEntry favorite : favoritesList) {
                    Entry favEntry = favorite.getEntry();
                    sb.append(getSelectLink(request, favEntry, target));
                }
                sb.append(HtmlUtils.close("div"));
                sb.append(sectionDivider);
            }


            List<Entry> recents =
                getEntryManager().getSessionFolders(request);
            if (recents.size() > 0) {
                sb.append(
                    HtmlUtils.open(
                        "div", HtmlUtils.cssClass("ramadda-select-inner")));
                sb.append(HtmlUtils.center(HtmlUtils.b(msg("Recent"))));
                List favoriteLinks = new ArrayList();
                for (Entry recent : recents) {
                    sb.append(getSelectLink(request, recent, target));
                }
                sb.append(HtmlUtils.close("div"));
                sb.append(sectionDivider);


            }


            List<Entry> cartEntries = getUserManager().getCart(request);
            if (cartEntries.size() > 0) {
                sb.append(
                    HtmlUtils.open(
                        "div", HtmlUtils.cssClass("ramadda-select-inner")));
                sb.append(HtmlUtils.b(msg("Cart")));
                sb.append(HtmlUtils.br());
                for (Entry cartEntry : cartEntries) {
                    sb.append(getSelectLink(request, cartEntry, target));
                }
                sb.append(HtmlUtils.close("div"));
                sb.append(sectionDivider);
            }
        }


        for (Entry subGroup : subGroups) {
            if (Misc.equals(localeId, subGroup.getId())) {
                continue;
            }
            sb.append(getSelectLink(request, subGroup, target));
        }

        if (request.get(ARG_ALLENTRIES, false)) {
            String entryType = request.getString(ARG_ENTRYTYPE,(String) null);
            for (Entry entry : entries) {
                if(Utils.stringDefined(entryType) && !entry.getTypeHandler().isType(entryType)) {
                    continue;
                }
                sb.append(getSelectLink(request, entry, target));
            }
        }

        sb.append(HtmlUtils.close("div"));

        return makeAjaxResult(request,
                              getRepository().translate(request,
                                  sb.toString()));

    }



    /**
     * _more_
     *
     * @param request _more_
     * @param entry _more_
     * @param sb _more_
     * @param open _more_
     * @param suffix _more_
     */
    private void addDescription(Request request, Entry entry,
                                StringBuffer sb, boolean open,
                                StringBuffer suffix) {
        String  desc   = entry.getDescription().trim();
        boolean isWiki = TypeHandler.isWikiText(desc);
        if ((desc.length() > 0) && !isWiki && !desc.equals("<nolinks>")) {
            desc = processText(request, entry, desc);
            StringBuffer descSB =
                new StringBuffer("\n<div class=\"description\">\n");
            descSB.append(desc);
            descSB.append("</div>\n");

            //            sb.append(HtmlUtils.makeShowHideBlock(msg("Description"),
            //                    descSB.toString(), open));

            //            sb.append(HtmlUtils.makeToggleInline("",
            //                                                desc, true));
            sb.append(desc);
        }
        if (isWiki) {
            suffix.append(HtmlUtils.p());
            suffix.append(subHeader(msg("Wiki Text")));
            suffix.append(HtmlUtils.textArea("", desc, 20, 80));
            suffix.append(HtmlUtils.p());
        }

    }


    /**
     * _more_
     *
     * @param request _more_
     * @param entry _more_
     * @param includeDescription _more_
     *
     * @return _more_
     *
     * @throws Exception _more_
     */
    public String getInformationTabs(Request request, Entry entry,
                                     boolean includeDescription)
            throws Exception {
        List         tabTitles   = new ArrayList<String>();
        List         tabContents = new ArrayList<String>();
        StringBuffer basicSB     = new StringBuffer();
        String       desc        = entry.getDescription();
        if (includeDescription && (desc.length() > 0)) {
            desc = processText(request, entry, desc);
            basicSB.append(desc);
            basicSB.append("<br>");
        }
        basicSB.append(entry.getTypeHandler().getEntryContent(request, entry,
                false, true));

        tabTitles.add("Information");
        tabContents.add(basicSB.toString());

        for (TwoFacedObject tfo :
                getMetadataHtml(request, entry, null, null)) {
            tabTitles.add(tfo.toString());
            tabContents.add(tfo.getId());
        }
        entry.getTypeHandler().addToInformationTabs(request, entry,
                tabTitles, tabContents);


        StringBuilder comments = getCommentBlock(request, entry, true);
        if (comments.length() > 0) {
            tabTitles.add(msg("Comments"));
            //        System.out.println (comments);
            tabContents.add(comments);
        }

        String attachments = getAttachmentsHtml(request, entry);
        if (attachments.length() > 0) {
            tabTitles.add(msg("Attachments"));
            tabContents.add(attachments);
        }

        StringBuffer associationBlock =
            getAssociationManager().getAssociationBlock(request, entry);
        if (associationBlock.length() > 0) {
            if (request.get(ARG_SHOW_ASSOCIATIONS, false)) {
                tabTitles.add(0, msg(ASSOCIATION_LABEL));
                tabContents.add(0, associationBlock);
            } else {
                tabTitles.add(msg(ASSOCIATION_LABEL));
                tabContents.add(associationBlock);
            }
        }


        //        tabTitles.add(msg(LABEL_LINKS));
        //        tabContents.add(getEntryManager().getEntryActionsTable(request, entry,
        //                OutputType.TYPE_ALL));

        if (tabContents.size() == 1) {
            return tabContents.get(0).toString();
        }


        return OutputHandler.makeTabs(tabTitles, tabContents, true);
    }







    /**
     * _more_
     *
     * @param request _more_
     * @param group _more_
     * @param subGroups _more_
     * @param entries _more_
     *
     * @return _more_
     *
     * @throws Exception _more_
     */
    public Result outputGrid(Request request, Entry group,
                             List<Entry> subGroups, List<Entry> entries)
            throws Exception {
        StringBuffer sb         = new StringBuffer();
        List<Entry>  allEntries = new ArrayList<Entry>();
        allEntries.addAll(subGroups);
        allEntries.addAll(entries);
        makeGrid(request, allEntries, sb);

        return makeLinksResult(request, msg("Grid"), sb,
                               new State(group, subGroups, entries));
    }


    /**
     * _more_
     *
     * @param request _more_
     * @param group _more_
     * @param subGroups _more_
     * @param entries _more_
     *
     * @return _more_
     *
     * @throws Exception _more_
     */
    public Result outputTreeView(Request request, Entry group,
                                 List<Entry> subGroups, List<Entry> entries)
            throws Exception {
        StringBuffer sb         = new StringBuffer();
        List<Entry>  allEntries = new ArrayList<Entry>();
        allEntries.addAll(subGroups);
        allEntries.addAll(entries);
        makeTreeView(request, allEntries, sb);

        return makeLinksResult(request, msg("Tree View"), sb,
                               new State(group, subGroups, entries));
    }


    /**
     * _more_
     *
     * @param request _more_
     * @param group _more_
     * @param subGroups _more_
     * @param entries _more_
     *
     * @return _more_
     *
     * @throws Exception _more_
     */
    public Result outputTest(Request request, Entry group,
                             List<Entry> subGroups, List<Entry> entries)
            throws Exception {
        return outputTest(request, group);
    }

    /** _more_ */
    private TTLCache<String, StringBuffer> testCache = new TTLCache<String,
                                                           StringBuffer>(60
                                                               * 60 * 1000);


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
    public Result outputTest(Request request, Entry entry) throws Exception {
        StringBuffer sb        = new StringBuffer();
        String       selectArg = "select";

        if (request.exists(selectArg + "1")) {
            List<String> values = new ArrayList<String>();
            for (int i = 1; i < 5; i++) {
                if ( !request.exists(selectArg + i)) {
                    break;
                }
                values.add(request.getString(selectArg + i, ""));
            }
            String valueKey = entry.getId() + "::"
                              + StringUtil.join("::", values);
            StringBuffer json = testCache.get(valueKey);
            if (json == null) {
                json = new StringBuffer();
                String lastValue = values.get(values.size() - 1);
                json.append(Json.map(new String[] { "values",
                        Json.list(new String[] { "--", lastValue + "-v1",
                        lastValue + "-v2", lastValue + "-v3" }) }, false));
                //                System.err.println(json);
                testCache.put(valueKey, json);
            }

            return new Result(BLANK, json,
                              getRepository().getMimeTypeFromSuffix(".json"));
        }

        String       formId = "form" + HtmlUtils.blockCnt++;
        StringBuffer js     = new StringBuffer();
        js.append("var " + formId + " = new Form(" + HtmlUtils.squote(formId)
                  + "," + HtmlUtils.squote(entry.getId()) + ");\n");
        sb.append(request.form(getRepository().URL_ENTRY_FORM,
                               HtmlUtils.attr("id", formId)));
        sb.append(HtmlUtils.hidden(ARG_ENTRYID, entry.getId()));
        sb.append(HtmlUtils.input("value", "",
                                  HtmlUtils.attr("id", formId + "_value")));
        js.append(JQ.submit(JQ.id(formId),
                            "return "
                            + HtmlUtils.call(formId + ".submit", "")));
        for (int selectIdx = 1; selectIdx < 10; selectIdx++) {
            sb.append(HtmlUtils.p());
            List values = new ArrayList();
            values.add(new TwoFacedObject("--", ""));
            if (selectIdx == 1) {
                values.add(new TwoFacedObject("Apple", "apple"));
                values.add(new TwoFacedObject("Banana", "banana"));
                values.add(new TwoFacedObject("Orange", "orange"));
            }
            sb.append(HtmlUtils.select(selectArg + +selectIdx, values,
                                       (String) null,
                                       HtmlUtils.attr("id",
                                           formId + "_" + selectArg
                                           + selectIdx)));
            js.append(JQ.change(JQ.id(formId + "_" + selectArg + selectIdx),
                                "return "
                                + HtmlUtils.call(formId + ".select",
                                    HtmlUtils.squote("" + selectIdx))));
        }
        sb.append(HtmlUtils.p());
        sb.append(HtmlUtils.submit("submit", "Submit"));

        sb.append(HtmlUtils.hr());
        sb.append(HtmlUtils.img(iconUrl("/icons/arrow.gif"), "",
                                HtmlUtils.attr("id", formId + "_image")));

        sb.append(HtmlUtils.script(js.toString()));
        //        System.err.println(sb);

        return new Result("test", sb);
    }





    /**
     * _more_
     *
     * @param request _more_
     * @param group _more_
     * @param subGroups _more_
     * @param entries _more_
     *
     * @return _more_
     *
     * @throws Exception _more_
     */
    public Result outputTable(Request request, Entry group,
                              List<Entry> subGroups, List<Entry> entries)
            throws Exception {
        StringBuffer sb         = new StringBuffer();
        List<Entry>  allEntries = new ArrayList<Entry>();
        allEntries.addAll(subGroups);
        allEntries.addAll(entries);
        makeTable(request, allEntries, sb);

        return makeLinksResult(request, msg("Table"), sb,
                               new State(group, subGroups, entries));
    }



    /**
     * _more_
     *
     * @param request _more_
     * @param allEntries _more_
     * @param sb _more_
     *
     * @throws Exception _more_
     */
    public void makeGrid(Request request, List<Entry> allEntries,
                         Appendable sb)
            throws Exception {
        int cols = request.get(ARG_COLUMNS, 4);
        sb.append("<table width=100% border=0 cellpadding=10>");
        int     col           = 0;
        boolean needToOpenRow = true;
        int     width         = (int) (100 * 1.0 / (float) cols);
        for (Entry entry : allEntries) {
            if (col >= cols) {
                sb.append("</tr>");
                needToOpenRow = true;
                col           = 0;
            }
            if (needToOpenRow) {
                sb.append("<tr align=bottom>");
                needToOpenRow = false;
            }
            col++;
            sb.append("<td valign=bottom align=center width=" + width
                      + "% >");
            String url = request.entryUrl(getRepository().URL_ENTRY_SHOW,
                                          entry, ARG_OUTPUT, OUTPUT_GRID);
            List<String> urls = new ArrayList<String>();
            getMetadataManager().getThumbnailUrls(request, entry, urls);
            if (urls.size() > 0) {
                sb.append(
                    HtmlUtils.href(
                        url,
                        HtmlUtils.img(
                            urls.get(0), "",
                            HtmlUtils.attr(HtmlUtils.ATTR_WIDTH, "100"))));
                sb.append(HtmlUtils.br());
            } else if (entry.getResource().isImage()) {
                String thumburl = HtmlUtils.url(
                                      request.url(repository.URL_ENTRY_GET)
                                      + "/"
                                      + getStorageManager().getFileTail(
                                          entry), ARG_ENTRYID, entry.getId(),
                                              ARG_IMAGEWIDTH, "" + 100);

                sb.append(HtmlUtils.href(url, HtmlUtils.img(thumburl)));
                sb.append(HtmlUtils.br());
            } else {
                sb.append(HtmlUtils.br());
                sb.append(HtmlUtils.space(1));
                sb.append(HtmlUtils.br());
            }

            String icon = getPageHandler().getIconUrl(request, entry);
            sb.append(HtmlUtils.href(url, HtmlUtils.img(icon)));
            sb.append(HtmlUtils.space(1));
            sb.append(getEntryManager().getTooltipLink(request, entry,
                    getEntryDisplayName(entry), url));
            sb.append(HtmlUtils.br());
            sb.append(entry.getTypeHandler().formatDate(request, entry,
                    new Date(entry.getStartDate()), ""));


            //            sb.append (getEntryManager().getAjaxLink( request,  entry,
            //                                                      "<br>"+getEntryDisplayName(entry),null, false));

            sb.append("</td>");
        }



        sb.append("</table>");

    }



    /**
     * _more_
     *
     * @param request _more_
     * @param children _more_
     * @param sb _more_
     *
     * @throws Exception _more_
     */
    public void makeTreeView(Request request, List<Entry> children,
                             Appendable sb)
            throws Exception {
        request.put(ARG_TREEVIEW, "true");
        StringBuffer listSB = new StringBuffer();
        sb.append("<table width=\"100%\"><tr valign=\"top\">");
        String link = getEntriesList(request, listSB, children, true, false,
                                     false);
        sb.append(HtmlUtils.col(link,
                                HtmlUtils.attr(HtmlUtils.ATTR_WIDTH, "350")));
        String gotoHtml = HtmlUtils.mouseClickHref("treeViewGoTo();",
                              "Go to", "");
        sb.append(HtmlUtils.col(HtmlUtils.leftRight(HtmlUtils.div("&nbsp;",
                HtmlUtils.id("treeview_header")), gotoHtml)));
        sb.append("</tr><tr valign=\"top\">");
        sb.append(HtmlUtils.col(listSB.toString()));
        sb.append(
            HtmlUtils.col(
                "<iframe id=\"treeview_view\" src=\"" + getRepository().getUrlBase()+"/blank\" width=\"750\" height=\"500\"></iframe>"));
        sb.append("</tr></table>");
        request.remove(ARG_TREEVIEW);
    }


    /**
     * _more_
     *
     * @param request _more_
     * @param allEntries _more_
     * @param sb _more_
     *
     * @throws Exception _more_
     */
    public void makeTable(Request request, List<Entry> allEntries,
                          Appendable sb)
            throws Exception {

        boolean showCategories = request.get(ARG_SHOWCATEGORIES, true);
        Hashtable<String, List<Entry>> map = new Hashtable<String,
                                                 List<Entry>>();
        List<String> types = new ArrayList<String>();
        for (Entry entry : allEntries) {
            TypeHandler  typeHandler = entry.getTypeHandler();
            String       type        = typeHandler.getType();
            List<Column> columns     = typeHandler.getColumns();
            boolean      hasFields   = false;
            if (columns != null) {
                for (Column column : columns) {
                    if (column.getCanList() && column.getCanShow()
                            && (column.getRows() <= 1)) {
                        hasFields = true;
                    }
                }
            }
            if ( !hasFields) {
                if (typeHandler.isGroup()) {
                    type = "Folders";
                } else if (entry.isFile()) {
                    type = "Files";
                }
            }

            if ( !showCategories) {
                type = "entries";
            }


            List<Entry> entries = map.get(type);
            if (entries == null) {
                entries = new ArrayList<Entry>();
                map.put(type, entries);
                types.add(type);
            }
            entries.add(entry);
        }
        List<String> contents = new ArrayList<String>();
        List<String> titles   = new ArrayList<String>();

        int          typeCnt  = 0;
        for (String type : types) {
            typeCnt++;
            int          numCols     = 0;
            List<Entry>  entries     = map.get(type);
            TypeHandler  typeHandler = entries.get(0).getTypeHandler();
            String       typeLabel   = type.equals("File")
                                       ? "File"
                                       : typeHandler.getLabel();
            List<Column> columns     = typeHandler.getColumns();
            StringBuffer tableSB     = new StringBuffer();
            tableSB.append("<div class=\"entry-table-wrapper\">");
            String tableId = HtmlUtils.getUniqueId("entrytable_");
            tableSB.append(HtmlUtils.open(HtmlUtils.TAG_TABLE,
                                          HtmlUtils.attrs(new String[] {
                "class", "entry-table", "width", "100%", "cellspacing", "0",
                "cellpadding", "0", "border", "0", HtmlUtils.ATTR_ID, tableId
            })));
            tableSB.append("<thead>");
            tableSB.append("<tr valign=bottom>");
            numCols++;

            tableSB.append(HtmlUtils.th(HtmlUtils.b(msg("Name"))));
            numCols++;
            tableSB.append(HtmlUtils.th(HtmlUtils.b(msg("Date"))));
            boolean haveFiles = false;
            for (Entry entry : entries) {
                if (entry.isFile()) {
                    haveFiles = true;

                    break;
                }
            }
            if (haveFiles) {
                numCols++;
                tableSB.append(HtmlUtils.th(HtmlUtils.b(msg("Size")),
                                            " align=right "));
            }
            if (columns != null) {
                for (Column column : columns) {
                    if (column.getCanList() && column.getCanShow()
                            && (column.getRows() <= 1)) {
                        numCols++;
                        tableSB.append(
                            HtmlUtils.th(HtmlUtils.b(column.getLabel())));
                    }
                }
            }
            tableSB.append("</tr>");
            tableSB.append("</thead>");
            tableSB.append("<tbody>");

            String  blank =
                HtmlUtils.img(getRepository().iconUrl(ICON_BLANK));
            boolean odd   = true;
            for (Entry entry : entries) {
                tableSB.append(HtmlUtils.open(HtmlUtils.TAG_TR,
                        HtmlUtils.attrs(new String[] { "class", odd
                        ? "odd"
                        : "even", "valign", "top" })));

                EntryLink entryLink = getEntryManager().getAjaxLink(request,
                                          entry, getEntryDisplayName(entry));

                tableSB.append(
                    HtmlUtils.col(
                        entryLink.getLink(),
                        " nowrap " + HtmlUtils.cssClass("entry-table-name")));
                String date = entry.getTypeHandler().formatDate(request,
                                  entry, new Date(entry.getStartDate()), "");
                tableSB.append(
                    HtmlUtils.col(
                        date,
                        " class=\"entry-table-date\" width=10% align=right "));

                if (haveFiles) {
                    String downloadLink =
                        HtmlUtils.href(
                            entry.getTypeHandler().getEntryResourceUrl(
                                request, entry), HtmlUtils.img(
                                iconUrl(ICON_DOWNLOAD), msg("Download"), ""));

                    if (entry.isFile()) {
                        tableSB.append(HtmlUtils
                            .col(formatFileLength(entry.getResource()
                                .getFileSize()) + " "
                                    + downloadLink, " align=right nowrap "));
                    } else {
                        tableSB.append(HtmlUtils.col("NA",
                                " align=right nowrap "));
                    }

                }
                Object[] values = entry.getValues();

                if (columns != null) {
                    for (Column column : columns) {
                        if (column.getCanList() && column.getCanShow()
                                && (column.getRows() <= 1)) {
                            String s = column.getString(values);
                            if (s == null) {
                                s = "NA";
                            }
                            if (column.isNumeric()) {
                                tableSB.append(HtmlUtils.colRight(s));
                            } else {
                                tableSB.append(HtmlUtils.col(s));
                            }
                        }
                    }
                }
                tableSB.append("</tr>");
                tableSB.append("<tr class=" + (odd
                        ? "odd"
                        : "even") + "><td class=entry-table-block colspan="
                                  + numCols + " >"
                                  + entryLink.getFolderBlock()
                                  + "</td></tr>");

                odd = !odd;
            }
            tableSB.append("</tbody>");
            tableSB.append("</table>");

            //            String script = JQuery.ready(JQuery.select(JQuery.id(tableId)) +".dataTable();\n");
            //            sb.append(HtmlUtils.script(script));

            if (typeCnt > 1) {
                sb.append("<p>");
            }

            tableSB.append("</div>");

            contents.add(tableSB.toString());
            titles.add(typeLabel);
        }
        if (types.size() == 1) {
            sb.append(contents.get(0));
        } else {
            HtmlUtils.makeAccordian(sb, titles, contents);
            /*            for(int i=0;i<titles.size();i++) {
                String title = titles.get(i);
                String content = contents.get(i);
                sb.append(HtmlUtils.makeShowHideBlock(title,
                                                      HtmlUtils.insetLeft(content, 10), true));
                                                      }*/
        }
    }


    /**
     * _more_
     *
     * @param request _more_
     * @param outputType _more_
     * @param group _more_
     * @param subGroups _more_
     * @param entries _more_
     *
     * @return _more_
     *
     * @throws Exception _more_
     */
    public Result outputGroup(Request request, OutputType outputType,
                              Entry group, List<Entry> subGroups,
                              List<Entry> entries)
            throws Exception {


        //This is a terrible hack but check if the request is for the timeline xml. If it is let the 
        //CalendarOutputHandler handle it.
        if (request.get("timelinexml", false)) {
            Result timelineResult =
                getCalendarOutputHandler().handleIfTimelineXml(request,
                    group, subGroups, entries);

            return timelineResult;
        }



        boolean isSearchResults = group.isDummy();
        TypeHandler typeHandler =
            getRepository().getTypeHandler(group.getType());

        if (outputType.equals(OUTPUT_INLINE)) {
            /*
            String wikiTemplate = getWikiText(request, group);
            if (wikiTemplate != null) {
                String wiki = getWikiManager().wikifyEntry(request, group, wikiTemplate, true, subGroups,
                                          entries);
                wiki = getRepository().translate(request, wiki);
                StringBuffer xml = new StringBuffer("<content>\n");
                XmlUtil.appendCdata(xml,
                                    "<div class=inline>" +wiki+"</div>");
                xml.append("\n</content>");
                return new Result("", xml, "text/xml");
                }*/

            return getChildrenXml(request, group, subGroups, entries);
        }

        if (outputType.equals(OUTPUT_SELECTXML)) {
            return getSelectXml(request, subGroups, entries);
        }
        if (outputType.equals(OUTPUT_METADATAXML)) {
            return getMetadataXml(request, group, true);
        }
        if (outputType.equals(OUTPUT_MAPINFO)) {
            return getMapInfo(request, group);
        }

        if (outputType.equals(OUTPUT_LINKSXML)) {
            return getLinksXml(request, group);
        }

        if (outputType.equals(OUTPUT_GRID)) {
            return outputGrid(request, group, subGroups, entries);
        }
        if (outputType.equals(OUTPUT_TREEVIEW)) {
            return outputTreeView(request, group, subGroups, entries);
        }

        if (outputType.equals(OUTPUT_TEST)) {
            return outputTest(request, group, subGroups, entries);
        }

        if (outputType.equals(OUTPUT_TABLE)) {
            return outputTable(request, group, subGroups, entries);
        }



        //        Result typeResult = typeHandler.getHtmlDisplay(request, group, subGroups, entries);
        //        if (typeResult != null) {
        //            return typeResult;
        //        }


        boolean doSimpleListing = !request.exists(ARG_OUTPUT);

        //If no children then show the details of this group
        if ((subGroups.size() == 0) && (entries.size() == 0)) {
            //            doSimpleListing = false;
        }
        boolean doingInfo = outputType.equals(OUTPUT_INFO);

        if ( !doingInfo) {
            if (typeHandler != null) {
                Result typeResult = typeHandler.getHtmlDisplay(request,
                                        group, subGroups, entries);

                if (typeResult != null) {
                    return typeResult;
                }
            }
        }

        StringBuffer sb = new StringBuffer("");
        request.appendMessage(sb);

        String messageLeft = request.getLeftMessage();
        if (messageLeft != null) {
            sb.append(messageLeft);
        }




        boolean hasChildren = ((subGroups.size() != 0)
                               || (entries.size() != 0));


        if (isSearchResults) {
            if ( !hasChildren) {
                //                sb.append(
                //                    getPageHandler().showDialogNote(msg("No entries found")));
            }
        }


        String       wikiTemplate = null;
        StringBuffer suffix       = new StringBuffer();
        if ( !doingInfo && !group.isDummy()) {
            handleDefaultWiki(request, group, sb, subGroups, entries);
        } else {
            if ( !group.isDummy()) {
                addDescription(request, group, sb, true, suffix);
                if ( !doSimpleListing) {
                    String informationBlock = getInformationTabs(request,
                                                  group, false);
                    if (hasChildren) {
                        sb.append(
                            HtmlUtils.makeShowHideBlock(
                                msg("Information"), informationBlock,
                                request.get(ARG_SHOW_ASSOCIATIONS, doingInfo
                                ? true
                                : !hasChildren)));
                    } else {
                        sb.append(informationBlock);
                    }
                }


                StringBuffer metadataSB = new StringBuffer();
                getMetadataManager().decorateEntry(request, group,
                        metadataSB, false);
                String metataDataHtml = metadataSB.toString();
                if (metataDataHtml.length() > 0) {
                    sb.append(HtmlUtils.makeShowHideBlock(msg("Attachments"),
                            "<div class=\"description\">" + metadataSB
                            + "</div>", false));
                }
            }

            showNext(request, subGroups, entries, sb);
            List<Entry> allEntries = new ArrayList<Entry>();
            allEntries.addAll(subGroups);
            allEntries.addAll(entries);
            if (allEntries.size() > 0) {
                getEntriesList(request, sb, allEntries, true,
                               group.isDummy(), true);
            } else {

                if ( !isSearchResults
                        && !Utils.stringDefined(group.getDescription())) {
                    sb.append(
                        getPageHandler().showDialogNote(
                            msg(LABEL_EMPTY_FOLDER)));
                }
            }

            if ( !group.isDummy() && (subGroups.size() == 0)
                    && (entries.size() == 0)) {
                if (getAccessManager().hasPermissionSet(group,
                        Permission.ACTION_VIEWCHILDREN)) {
                    if ( !getAccessManager().canDoAction(request, group,
                            Permission.ACTION_VIEWCHILDREN)) {
                        sb.append(
                            getPageHandler().showDialogWarning(
                                "You do not have permission to view the sub-folders of this entry"));
                    }
                }
            }
        }

        sb.append(suffix);

        Result result = makeLinksResult(request, msg("Folder"), sb,
                                        new State(group, subGroups, entries));

        return result;
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
    private String getWikiText(Request request, Entry entry)
            throws Exception {
        String description = entry.getDescription();

        String wikiInner   = null;
        //If it begins with <wiki> then it overrides anything else

        if (TypeHandler.isWikiText(description)) {
            if (description.startsWith("<wiki_inner>")) {
                wikiInner = description;
            } else {
                return description;
            }
        }

        String wikiTemplate = entry.getTypeHandler().getWikiTemplate(request,
                                  entry);
        if (wikiTemplate == null) {
            PageStyle pageStyle = request.getPageStyle(entry);
            wikiTemplate = pageStyle.getWikiTemplate(entry);
        }

        if (wikiInner != null) {
            if (wikiTemplate == null) {
                return wikiInner;
            }
            wikiTemplate = wikiTemplate.replace("${innercontent}", wikiInner);
        }

        return wikiTemplate;
    }




    /**
     * _more_
     *
     * @param request _more_
     * @param entries _more_
     *
     * @return _more_
     *
     * @throws Exception _more_
     */
    public String getTimelineApplet(Request request, List<Entry> entries)
            throws Exception {
        String timelineAppletTemplate =
            getRepository().getResource(PROP_HTML_TIMELINEAPPLET);
        List times  = new ArrayList();
        List labels = new ArrayList();
        List ids    = new ArrayList();
        for (Entry entry : entries) {
            String label = getEntryDisplayName(entry);
            label = label.replaceAll(",", " ");
            times.add(SqlUtil.format(new Date(entry.getStartDate())));
            labels.add(label);
            ids.add(entry.getId());
        }
        String tmp = StringUtil.replace(timelineAppletTemplate, "${times}",
                                        StringUtil.join(",", times));
        tmp = StringUtil.replace(tmp, "${root}",
                                 getRepository().getUrlBase());
        tmp = StringUtil.replace(tmp, "${labels}",
                                 StringUtil.join(",", labels));
        tmp = StringUtil.replace(tmp, "${ids}", StringUtil.join(",", ids));
        tmp = StringUtil.replace(
            tmp, "${loadurl}",
            request.url(
                getRepository().URL_ENTRY_GETENTRIES, ARG_ENTRYIDS, "%ids%",
                ARG_OUTPUT, OUTPUT_HTML));

        return tmp;

    }





    /**
     * _more_
     *
     * @param request _more_
     * @param entry _more_
     * @param text _more_
     *
     * @return _more_
     */
    public String processText(Request request, Entry entry, String text) {
        int idx = text.indexOf("<more>");
        if (idx >= 0) {
            String first  = text.substring(0, idx);
            String base   = "" + (HtmlUtils.blockCnt++);
            String divId  = "morediv_" + base;
            String linkId = "morelink_" + base;
            String second = text.substring(idx + "<more>".length());
            String moreLink = "javascript:showMore(" + HtmlUtils.squote(base)
                              + ")";
            String lessLink = "javascript:hideMore(" + HtmlUtils.squote(base)
                              + ")";
            text = first + "<br><a " + HtmlUtils.id(linkId) + " href="
                   + HtmlUtils.quote(moreLink)
                   + ">More...</a><div style=\"\" class=\"moreblock\" "
                   + HtmlUtils.id(divId) + ">" + second + "<br>" + "<a href="
                   + HtmlUtils.quote(lessLink) + ">...Less</a>" + "</div>";
        }
        text = text.replaceAll("\r\n\r\n", "<p>");
        text = text.replace("\n\n", "<p>");

        return text;
    }




}
