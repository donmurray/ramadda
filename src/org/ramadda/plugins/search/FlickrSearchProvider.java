/*
 * Copyright (c) 2008-2015 Geode Systems LLC
 * This Software is licensed under the Geode Systems RAMADDA License available in the source distribution in the file 
 * ramadda_license.txt. The above copyright notice shall be included in all copies or substantial portions of the Software.
 */

package org.ramadda.plugins.search;


import org.json.*;

import org.ramadda.repository.*;
import org.ramadda.repository.metadata.*;
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
public class FlickrSearchProvider extends SearchProvider {

    /** _more_ */
    private static final String ID = "flickr";

    /** _more_ */
    private static final String URL =
        "https://api.flickr.com/services/rest/?method=flickr.photos.search";


    /** _more_ */
    private static final String ARG_API_KEY = "api_key";

    /** _more_ */
    private static final String ARG_TEXT = "text";

    /** _more_ */
    private static final String ARG_MIN_TAKEN_DATE = "min_taken_date";

    /** _more_ */
    private static final String ARG_MAX_TAKEN_DATE = "max_taken_date";

    /** _more_ */
    private static final String ARG_TAGS = "tags";

    /** _more_ */
    private static final String ARG_BBOX = "bbox";



    /**
     * _more_
     *
     * @param repository _more_
     */
    public FlickrSearchProvider(Repository repository) {
        super(repository, ID, "Flickr Image Search");
    }



    /**
     * _more_
     *
     * @return _more_
     */
    public boolean isEnabled() {
        return getApiKey() != null;
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
        String      url     = URL;
        url = HtmlUtils.url(url, ARG_API_KEY, getApiKey(), ARG_TEXT,
                            request.getString(ARG_TEXT, ""));
        //        System.err.println(getName() + " search url:" + url);
        URLConnection connection = new URL(url).openConnection();
        connection.setRequestProperty("User-Agent", "ramadda");
        InputStream is  = connection.getInputStream();
        String      xml = IOUtil.readContents(is);
        //        System.out.println("xml:" + xml);

        Element root   = XmlUtil.getRoot(xml);
        Element photos = XmlUtil.findChild(root, "photos");
        if (photos == null) {
            return entries;
        }

        Entry       parent      = getSynthTopLevelEntry();
        TypeHandler typeHandler =
            getRepository().getTypeHandler("type_image");
        NodeList children = XmlUtil.getElements(photos, "photo");
        for (int childIdx = 0; childIdx < children.getLength(); childIdx++) {
            Element item     = (Element) children.item(childIdx);
            Element link     = XmlUtil.findChild(item, AtomUtil.TAG_LINK);
            String  name     = XmlUtil.getAttribute(item, "title", "");
            String  desc     = "";
            String  id       = XmlUtil.getAttribute(item, "id", "");
            String  server   = XmlUtil.getAttribute(item, "server", "");
            String  farm     = XmlUtil.getAttribute(item, "farm", "");
            String  secret   = XmlUtil.getAttribute(item, "secret", "");
            Date    dttm     = new Date();
            Date    fromDate = dttm,
                    toDate   = dttm;
            String urlTemplate =
                "https://farm${farm}.staticflickr.com/${server}/${id}_${secret}_${size}.jpg";
            String imageUrl = urlTemplate.replace("${farm}",
                                  farm).replace("${server}",
                                      server).replace("${id}",
                                          id).replace("${secret}", secret);

            String itemUrl = imageUrl.replace("${size}", "b");

            Entry newEntry = new Entry(Repository.ID_PREFIX_SYNTH + getId()
                                       + TypeHandler.ID_DELIMITER + id, typeHandler);
            newEntry.setIcon("/search/flickr.png");
            entries.add(newEntry);

            Metadata thumbnailMetadata =
                new Metadata(getRepository().getGUID(), newEntry.getId(),
                             ContentMetadataHandler.TYPE_THUMBNAIL, false,
                             imageUrl.replace("${size}", "t"), null, null,
                             null, null);
            newEntry.addMetadata(thumbnailMetadata);

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
