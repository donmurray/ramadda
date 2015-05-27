/*
 * Copyright (c) 2008-2015 Geode Systems LLC
 * This Software is licensed under the Geode Systems RAMADDA License available in the source distribution in the file 
 * ramadda_license.txt. The above copyright notice shall be included in all copies or substantial portions of the Software.
 */

package org.ramadda.plugins.search;


import org.json.*;

import org.ramadda.repository.*;
import org.ramadda.repository.search.*;
import org.ramadda.repository.type.*;
import org.ramadda.util.AtomUtil;

import org.ramadda.util.HtmlUtils;
import org.ramadda.util.Utils;



import org.w3c.dom.*;

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
 * Proxy that searches google
 *
 */
public class OpenSearchProvider extends SearchProvider {

    /** _more_ */
    private String baseUrl;


    /**
     * _more_
     *
     * @param repository _more_
     * @param args _more_
     */
    public OpenSearchProvider(Repository repository, List<String> args) {
        super(repository, args.get(0), args.get(2));
        baseUrl = args.get(1);
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
        String      url     = baseUrl;
        url = url.replace("${searchterms}", request.getString(ARG_TEXT, ""));
        //TODO:
        url = url.replace("${time:start}", "");
        url = url.replace("${time:end}", "");
        url = url.replace("${geo:box}", "");

        System.err.println(getName() + " search url:" + url);
        URLConnection connection = new URL(url).openConnection();
        connection.setRequestProperty("User-Agent", "ramadda");
        InputStream is  = connection.getInputStream();
        String      xml = IOUtil.readContents(is);
        //        System.out.println("xml:" + xml);

        Element     root        = XmlUtil.getRoot(xml);
        Entry       parent      = getSynthTopLevelEntry();
        TypeHandler typeHandler = getLinkTypeHandler();
        NodeList    children = XmlUtil.getElements(root, AtomUtil.TAG_ENTRY);
        for (int childIdx = 0; childIdx < children.getLength(); childIdx++) {
            Element item = (Element) children.item(childIdx);
            Element link = XmlUtil.findChild(item, AtomUtil.TAG_LINK);
            String name = XmlUtil.getGrandChildText(item, AtomUtil.TAG_TITLE,
                              "");
            String desc = XmlUtil.getGrandChildText(item,
                              AtomUtil.TAG_SUMMARY, "");
            String id = XmlUtil.getGrandChildText(item, AtomUtil.TAG_ID, "");
            Date   dttm     = new Date();
            Date   fromDate = null,
                   toDate   = null;
            String dateString;


            dateString = XmlUtil.getGrandChildText(item, "dc:date",
                    (String) null);
            if (Utils.stringDefined(dateString)) {
                List<String> toks = StringUtil.splitUpTo(dateString, "/", 2);
                fromDate = DateUtil.parse(toks.get(0));
                if (toks.size() > 1) {
                    toDate = DateUtil.parse(toks.get(1));
                } else {
                    toDate = fromDate;
                }
            }


            if (fromDate == null) {
                dateString = XmlUtil.getGrandChildText(item,
                        AtomUtil.TAG_TIME_START, (String) null);
                if (Utils.stringDefined(dateString)) {
                    fromDate = DateUtil.parse(dateString);
                }
            }


            if (toDate == null) {
                dateString = XmlUtil.getGrandChildText(item,
                        AtomUtil.TAG_TIME_END, (String) null);
                if (Utils.stringDefined(dateString)) {
                    toDate = DateUtil.parse(dateString);
                }
            }

            if (fromDate == null) {
                fromDate = dttm;
            }
            if (toDate == null) {
                toDate = dttm;
            }

            String itemUrl = XmlUtil.getAttribute(link, AtomUtil.ATTR_HREF,
                                 "");
            Entry newEntry = new Entry(Repository.ID_PREFIX_SYNTH + getId()
                                       + ":" + id, typeHandler);
            entries.add(newEntry);

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
