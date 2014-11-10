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

package org.ramadda.plugins.blog;


import org.ramadda.repository.*;
import org.ramadda.repository.auth.*;
import org.ramadda.repository.output.*;

import org.ramadda.sql.SqlUtil;
import org.ramadda.util.HtmlUtils;


import org.w3c.dom.*;

import ucar.unidata.ui.ImageUtils;
import ucar.unidata.util.DateUtil;
import ucar.unidata.util.IOUtil;
import ucar.unidata.util.Misc;


import ucar.unidata.util.StringUtil;
import ucar.unidata.util.WmsUtil;
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
 * @author IDV Development Team
 * @version $Revision: 1.3 $
 */
public class WeblogOutputHandler extends OutputHandler {

    /** _more_ */
    private SimpleDateFormat sdf;


    /** _more_ */
    public static final OutputType OUTPUT_BLOG = new OutputType("Weblog",
                                                     "blog",
                                                     OutputType.TYPE_VIEW,
                                                     "", "blog.image");


    /**
     * _more_
     *
     *
     * @param repository _more_
     * @param element _more_
     * @throws Exception _more_
     */
    public WeblogOutputHandler(Repository repository, Element element)
            throws Exception {
        super(repository, element);
        addType(OUTPUT_BLOG);
        sdf = new SimpleDateFormat("MMMMM d, yyyy");
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
        if (true) {
            return;
        }
        if (state.group == null) {
            return;
        }
        List<Entry> entries = state.getAllEntries();
        if (entries.size() == 0) {
            return;
        }
        boolean ok = false;
        for (Entry entry : entries) {
            if (entry.getType().equals("blogentry")) {
                ok = true;

                break;
            }
        }
        if ( !ok) {
            return;
        }
        links.add(makeLink(request, state.getEntry(), OUTPUT_BLOG));
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

        StringBuilder sb          = new StringBuilder();
        StringBuilder blogEntries = new StringBuilder();
        for (Entry entry : entries) {
            if ( !entry.getTypeHandler().isType("blogentry")) {
                continue;
            }
            String blogEntry = getBlogEntry(request, entry, false);
            blogEntries.append(
                HtmlUtils.div(blogEntry, HtmlUtils.cssClass("blog-entry")));
        }

        boolean embedded = request.get(ARG_EMBEDDED, false);

        wrapContent(request, group, sb,
                    HtmlUtils.div(blogEntries.toString(),
                                  HtmlUtils.cssClass("blog-entries"
                                      + (embedded
                                         ? "-embed"
                                         : ""))));

        return new Result("", sb);
    }


    /**
     * _more_
     *
     * @param request _more_
     * @param group _more_
     * @param sb _more_
     * @param content _more_
     *
     * @throws Exception _more_
     */
    public void wrapContent(Request request, Entry group, StringBuilder sb,
                            String content)
            throws Exception {
        sb.append(HtmlUtils.cssLink(getRepository().getUrlBase()
                                    + "/blog/blogstyle.css"));
        boolean embedded = request.get(ARG_EMBEDDED, false);
        if (embedded) {
            sb.append(content);

            return;
        }
        List<String> links = new ArrayList<String>();
        if (group != null) {
            String headerValue = group.getValue(0, "");
            if (headerValue.length() == 0) {
                headerValue =
                    "\n<br>"
                    + HtmlUtils.div(
                        HtmlUtils.img(
                            getRepository().fileUrl(
                                "/blog/header.png")), HtmlUtils.attrs(
                                    "style",
                                    "text-align:center;")) + "\n<p>\n";
            }
            String header = getWikiManager().wikifyEntry(request, group,
                                headerValue);

            sb.append(header);
            boolean canAdd = getAccessManager().canDoAction(request, group,
                                 Permission.ACTION_NEW);

            if (canAdd && !embedded) {
                links.add(
                    HtmlUtils
                        .href(HtmlUtils
                            .url(request
                                .entryUrl(
                                    getRepository().URL_ENTRY_FORM, group,
                                    ARG_GROUP), ARG_TYPE,
                                        BlogEntryTypeHandler
                                            .TYPE_BLOGENTRY), HtmlUtils
                                                .img(getRepository()
                                                    .iconUrl(ICON_NEW), msg(
                                                        "New Weblog Entry"))));
            }
        }

        sb.append(HtmlUtils.open("div", HtmlUtils.cssClass("row")));
        sb.append(HtmlUtils.open("div", HtmlUtils.cssClass("col-md-9")));
        sb.append(content);
        sb.append(HtmlUtils.close("div"));

        sb.append(HtmlUtils.open("div", HtmlUtils.cssClass("col-md-3")));
        if (group != null) {
            String rightSide = getWikiManager().wikifyEntry(request, group,
                                   group.getValue(1, ""));
            String rssLink = request.entryUrl(getRepository().URL_ENTRY_SHOW,
                                 group, ARG_OUTPUT,
                                 RssOutputHandler.OUTPUT_RSS_FULL.toString());


            links.add(
                HtmlUtils.href(
                    rssLink,
                    HtmlUtils.img(iconUrl(RssOutputHandler.ICON_RSS))));


            sb.append(StringUtil.join(" ", links));
            sb.append(HtmlUtils.br());
            sb.append(rightSide);
            sb.append(HtmlUtils.close("div"));
        }


        sb.append(HtmlUtils.close("div"));
    }


    /**
     * _more_
     *
     * @param date _more_
     *
     * @return _more_
     */
    private String formatDate(Date date) {
        synchronized (sdf) {
            return sdf.format(date);
        }
    }

    /**
     * _more_
     *
     * @param request _more_
     * @param entry _more_
     * @param single _more_
     *
     * @return _more_
     *
     * @throws Exception _more_
     */
    public String getBlogEntry(Request request, Entry entry, boolean single)
            throws Exception {
        boolean       embedded  = request.get(ARG_EMBEDDED, false);
        StringBuilder blogEntry = new StringBuilder();
        String entryUrl = request.entryUrl(getRepository().URL_ENTRY_SHOW,
                                           entry);
        String subject;
        if (single) {
            subject = HtmlUtils.div(entry.getLabel(),
                                    HtmlUtils.cssClass("blog-subject"));
        } else {
            subject = HtmlUtils.div(
                HtmlUtils.href(
                    entryUrl, entry.getLabel(), HtmlUtils.cssClass(
                        "blog-subject")), HtmlUtils.cssClass("blog-subject"));
        }
        String postingInfo = "";
        String header;
        if ( !embedded) {
            String posted = msg("Posted on") + " "
                            + formatDate(new Date(entry.getStartDate()))
                            + " " + msg("by") + " "
                            + entry.getUser().getName();
            postingInfo = HtmlUtils.div(posted,
                                        HtmlUtils.cssClass("blog-posted"));
        } else {
            postingInfo = formatDate(new Date(entry.getStartDate()));
        }
        header = HtmlUtils.leftRightBottom(subject, postingInfo, "");

        blogEntry.append(HtmlUtils.div(header,
                                       HtmlUtils.cssClass("blog-header")));
        String desc = entry.getDescription();
        if (desc.startsWith("<p>")) {
            desc = desc.substring(3);
            if (desc.endsWith("</p>")) {
                desc = desc.substring(0, desc.length() - 4);
            }
        }
        desc = getWikiManager().wikifyEntry(request, entry, desc);


        StringBuilder blogBody = new StringBuilder(desc);
        Object[]      values   = entry.getValues();
        if (values[0] != null) {
            String extra = ((String) values[0]).trim();
            if (extra.length() > 0) {
                extra = getWikiManager().wikifyEntry(request, entry, extra);
                if (single) {
                    blogBody.append(extra);
                } else {
                    if ( !embedded) {
                        blogBody.append(
                            HtmlUtils.makeShowHideBlock(
                                msg("More..."), extra, false));
                    }
                }
            }
        }

        if ( !embedded) {
            StringBuilder comments = getCommentBlock(request, entry, false);
            String commentsBlock = HtmlUtils.makeShowHideBlock(
                                       msg("Comments"),
                                       HtmlUtils.insetDiv(
                                           comments.toString(), 0, 30, 0,
                                           0), false);

            blogBody.append(commentsBlock);
        }



        blogEntry.append(HtmlUtils.div(blogBody.toString(),
                                       HtmlUtils.cssClass("blog-body")));

        return blogEntry.toString();
    }

}
