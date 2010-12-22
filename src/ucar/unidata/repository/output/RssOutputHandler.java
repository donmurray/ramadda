/*
 * Copyright 1997-2010 Unidata Program Center/University Corporation for
 * Atmospheric Research, P.O. Box 3000, Boulder, CO 80307,
 * support@unidata.ucar.edu.
 * 
 * This library is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation; either version 2.1 of the License, or (at
 * your option) any later version.
 * 
 * This library is distributed in the hope that it will be useful, but
 * WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU Lesser
 * General Public License for more details.
 * 
 * You should have received a copy of the GNU Lesser General Public License
 * along with this library; if not, write to the Free Software Foundation,
 * Inc., 59 Temple Place, Suite 330, Boston, MA 02111-1307 USA
 */

package ucar.unidata.repository.output;


import org.w3c.dom.*;

import ucar.unidata.ui.ImageUtils;
import ucar.unidata.repository.*;
import ucar.unidata.repository.auth.*;
import ucar.unidata.repository.type.*;

import ucar.unidata.sql.SqlUtil;
import ucar.unidata.util.DateUtil;
import ucar.unidata.util.HtmlUtil;
import ucar.unidata.util.IOUtil;
import ucar.unidata.util.Misc;


import ucar.unidata.util.StringBufferCollection;


import ucar.unidata.util.StringUtil;
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
 * Class SqlUtil _more_
 *
 *
 * @author IDV Development Team
 * @version $Revision: 1.3 $
 */
public class RssOutputHandler extends OutputHandler {

    /** _more_ */
    public static final String TAG_RSS_RSS = "rss";

    /** _more_ */
    public static final String TAG_RSS_GEOLAT = "georss:lat";

    /** _more_ */
    public static final String TAG_RSS_GEOLON = "georss:lon";

    public static final String TAG_RSS_GEOBOX = "georss:box";


    /** _more_ */
    public static final String TAG_RSS_LINK = "link";

    /** _more_ */
    public static final String TAG_RSS_GUID = "guid";

    /** _more_ */
    public static final String TAG_RSS_CHANNEL = "channel";

    /** _more_ */
    public static final String TAG_RSS_ITEM = "item";

    /** _more_ */
    public static final String TAG_RSS_TITLE = "title";

    /** _more_ */
    public static final String TAG_RSS_PUBDATE = "pubDate";

    /** _more_ */
    public static final String TAG_RSS_DESCRIPTION = "description";


    /** _more_          */
    SimpleDateFormat rssSdf =
        new SimpleDateFormat("EEE dd, MMM yyyy HH:mm:ss Z");

    /** _more_ */
    public static final OutputType OUTPUT_RSS_FULL =
        new OutputType("Full RSS Feed", "rss.full", OutputType.TYPE_NONHTML,
                       "", ICON_RSS);

    /** _more_ */
    public static final OutputType OUTPUT_RSS_SUMMARY =
        new OutputType("RSS Feed", "rss.summary",
                       OutputType.TYPE_NONHTML | OutputType.TYPE_FORSEARCH,
                       "", ICON_RSS);




    /**
     * _more_
     *
     * @param repository _more_
     * @param element _more_
     * @throws Exception _more_
     */
    public RssOutputHandler(Repository repository, Element element)
            throws Exception {
        super(repository, element);
        addType(OUTPUT_RSS_FULL);
        addType(OUTPUT_RSS_SUMMARY);
    }




    /**
     * _more_
     *
     * @param request _more_
     * @param state _more_
     * @param links _more_
     *
     * @throws Exception _more_
     */
    public void getEntryLinks(Request request, State state, List<Link> links)
            throws Exception {

        if (state.getEntry() != null) {
            links.add(
                makeLink(
                    request, state.getEntry(), OUTPUT_RSS_SUMMARY,
                    "/" + IOUtil.stripExtension(state.getEntry().getName())
                    + ".rss"));
        }
    }





    /**
     * _more_
     *
     * @param output _more_
     *
     * @return _more_
     */
    public String getMimeType(OutputType output) {
        if (output.equals(OUTPUT_RSS_FULL)
                || output.equals(OUTPUT_RSS_SUMMARY)) {
            return repository.getMimeTypeFromSuffix(".rss");
        } else {
            return super.getMimeType(output);
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
                              Group group, List<Group> subGroups,
                              List<Entry> entries)
            throws Exception {
        entries.addAll(subGroups);
        return outputEntries(request, group, entries);
    }



    public Result outputEntry(Request request, OutputType outputType,
                              Entry entry)
            throws Exception {
        List<Entry> entries = new ArrayList<Entry>();
        entries.add(entry);
        return outputEntries(request, entry, entries);
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
    private Result outputEntries(Request request, Entry parentEntry, List<Entry> entries)
            throws Exception {

        StringBuffer sb = new StringBuffer();
        sb.append(XmlUtil.XML_HEADER + "\n");
        sb.append(XmlUtil.openTag(TAG_RSS_RSS,
                                  XmlUtil.attrs(ATTR_RSS_VERSION, "2.0")));
        sb.append(XmlUtil.openTag(TAG_RSS_CHANNEL));
        sb.append(XmlUtil.tag(TAG_RSS_TITLE, "", parentEntry.getName()));
        StringBufferCollection sbc    = new StringBufferCollection();
        OutputType             output = request.getOutput();
        request.put(ARG_OUTPUT, OutputHandler.OUTPUT_HTML);
        for (Entry entry : entries) {
            StringBuffer extra = new StringBuffer();
            String resource = entry.getResource().getPath();
            if(ImageUtils.isImage(resource)) {
                String imageUrl = repository.absoluteUrl(HtmlUtil.url(
                                                                      getRepository().URL_ENTRY_GET + entry.getId()
                                                                      + IOUtil.getFileExtension(
                                                                                                resource), ARG_ENTRYID, entry.getId()
                                                                      /*,                                                                    ARG_IMAGEWIDTH, "75"*/));
                extra.append(HtmlUtil.br());
                extra.append(HtmlUtil.img(imageUrl));
            }

            sb.append(XmlUtil.openTag(TAG_RSS_ITEM));
            sb.append(
                XmlUtil.tag(
                    TAG_RSS_PUBDATE, "",
                    rssSdf.format(new Date(entry.getStartDate()))));
            sb.append(XmlUtil.tag(TAG_RSS_TITLE, "", entry.getName()));
            String url =
                repository.absoluteUrl(request.url(repository.URL_ENTRY_SHOW,
                    ARG_ENTRYID, entry.getId()));
            sb.append(XmlUtil.tag(TAG_RSS_LINK, "", url));
            sb.append(XmlUtil.tag(TAG_RSS_GUID, "", url));

            sb.append(XmlUtil.openTag(TAG_RSS_DESCRIPTION, ""));
            if (output.equals(OUTPUT_RSS_FULL)) {
                XmlUtil.appendCdata(
                    sb,
                    entry.getTypeHandler().getEntryContent(
                                                           entry, request, true, false).toString());
            } else {
                XmlUtil.appendCdata(sb, entry.getDescription()+extra);
            }

            sb.append(XmlUtil.closeTag(TAG_RSS_DESCRIPTION));
            if (entry.hasLocationDefined()) {
                sb.append(XmlUtil.tag(TAG_RSS_GEOLAT, "",
                                      "" + entry.getSouth()));
                sb.append(XmlUtil.tag(TAG_RSS_GEOLON, "",
                                      "" + entry.getEast()));
            } else if (entry.hasAreaDefined()) {
                //For now just include the southeast point
                sb.append(XmlUtil.tag(TAG_RSS_GEOBOX, "",
                                      entry.getSouth()+","+entry.getWest()+"," +
                                      entry.getNorth() +"," +
                                      entry.getEast()));
            }


            sb.append(XmlUtil.closeTag(TAG_RSS_ITEM));
        }

        request.put(ARG_OUTPUT, output);
        sb.append(XmlUtil.closeTag(TAG_RSS_CHANNEL));
        sb.append(XmlUtil.closeTag(TAG_RSS_RSS));
        Result result = new Result("Query Results", sb,
                                   getMimeType(OUTPUT_RSS_SUMMARY));




        return result;

    }


}
