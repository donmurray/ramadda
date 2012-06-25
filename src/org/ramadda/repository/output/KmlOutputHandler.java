/*
* Copyright 2008-2011 Jeff McWhirter/ramadda.org
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
import org.ramadda.repository.metadata.JpegMetadataHandler;
import org.ramadda.repository.metadata.Metadata;


import org.w3c.dom.*;

import ucar.unidata.data.gis.KmlUtil;

import ucar.unidata.geoloc.Bearing;
import ucar.unidata.geoloc.LatLonPointImpl;

import org.ramadda.util.HtmlUtils;
import ucar.unidata.util.IOUtil;
import ucar.unidata.util.Misc;
import ucar.unidata.util.StringUtil;
import ucar.unidata.xml.XmlUtil;

import java.awt.Color;


import java.io.*;

import java.io.File;

import java.util.ArrayList;
import java.util.Date;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.List;
import java.util.Properties;



/**
 *
 *
 *
 * @author RAMADDA Development Team
 * @version $Revision: 1.3 $
 */
public class KmlOutputHandler extends OutputHandler {

    /** _more_ */
    public static final String KML_ATTRS =
        "  xmlns:xlink=\"http://www.w3.org/1999/xlink\" ";

    /** _more_ */
    public static final OutputType OUTPUT_KML =
        new OutputType("Google Earth KML", "kml",
                       OutputType.TYPE_FEEDS | OutputType.TYPE_FORSEARCH, "",
                       ICON_KML);



    /**
     * _more_
     *
     * @param repository _more_
     * @param element _more_
     * @throws Exception _more_
     */
    public KmlOutputHandler(Repository repository, Element element)
            throws Exception {
        super(repository, element);
        addType(OUTPUT_KML);
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
        if (state.getEntry() != null) {
            if ( !state.getEntry().isGroup()) {
                if ( !isLatLonImage(state.getEntry())) {
                    if (true) {
                        return;
                    }
                }
            }
            links.add(
                makeLink(
                    request, state.getEntry(), OUTPUT_KML,
                    "/" + IOUtil.stripExtension(state.getEntry().getName())
                    + ".kml"));
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
        return repository.getMimeTypeFromSuffix(".kml");
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
        List<Entry> entries = new ArrayList<Entry>();
        entries.add(entry);
        return outputGroup(request, outputType, entry,
                           new ArrayList<Entry>(), entries);

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

        boolean justOneEntry = group.isDummy() && (entries.size() == 1)
                               && (subGroups.size() == 0);


        String  title = (justOneEntry
                         ? entries.get(0).getName()
                         : group.getFullName());
        Element root  = KmlUtil.kml(title);
        Element folder = KmlUtil.folder(root, title,
                                        request.get(ARG_VISIBLE, false));
        KmlUtil.open(folder, false);
        if (group.getDescription().length() > 0) {
            KmlUtil.description(folder, group.getDescription());
        }

        int cnt  = subGroups.size() + entries.size();
        int max  = request.get(ARG_MAX, DB_MAX_ROWS);
        int skip = Math.max(0, request.get(ARG_SKIP, 0));
        for (Entry childGroup : subGroups) {
            String url =
                request.getAbsoluteUrl(request.url(repository.URL_ENTRY_SHOW,
                    ARG_ENTRYID, childGroup.getId(), ARG_OUTPUT, OUTPUT_KML));
            Element link = KmlUtil.networkLink(folder, childGroup.getName(),
                               url);
            if (childGroup.getDescription().length() > 0) {
                KmlUtil.description(link, childGroup.getDescription());
            }

            KmlUtil.visible(link, request.get(ARG_VISIBLE, false));
            KmlUtil.open(link, false);
            link.setAttribute(KmlUtil.ATTR_ID, childGroup.getId());
        }

        if ((cnt > 0) && ((cnt == max) || request.defined(ARG_SKIP))) {
            if (cnt >= max) {
                String skipArg = request.getString(ARG_SKIP, null);
                request.remove(ARG_SKIP);
                String url = request.url(repository.URL_ENTRY_SHOW,
                                         ARG_ENTRYID, group.getId(),
                                         ARG_OUTPUT, OUTPUT_KML, ARG_SKIP,
                                         "" + (skip + max), ARG_MAX,
                                         "" + max);

                url = request.getAbsoluteUrl(url);
                Element link = KmlUtil.networkLink(folder, "More...", url);

                if (skipArg != null) {
                    request.put(ARG_SKIP, skipArg);
                }
            }
        }


        for (Entry entry : (List<Entry>) entries) {
            if (isLatLonImage(entry)) {
                String fileTail = getStorageManager().getFileTail(entry);
                String url =
                    HtmlUtils.url(request.url(getRepository().URL_ENTRY_GET)
                                 + "/" + fileTail, ARG_ENTRYID,
                                     entry.getId());
                url = request.getAbsoluteUrl(url);
                myGroundOverlay(folder, entry.getName(),
                                entry.getDescription(), url,
                                getLocation(entry.getNorth(), 90),
                                getLocation(entry.getSouth(), -90),
                                getLocation(entry.getEast(), 180),
                                getLocation(entry.getWest(), -180),
                                request.get(ARG_VISIBLE, false));
                continue;
            }

            List<Service> services = new ArrayList<Service>();
            entry.getTypeHandler().getServices(request, entry, services);

            for (Service service : services) {
                if (service.isType(Service.TYPE_KML)) {
                    KmlUtil.networkLink(folder, service.getName(),
                                        service.getUrl());
                }
            }

            String url = getKmlUrl(request, entry);
            if (url != null) {
                Element link = KmlUtil.networkLink(folder, entry.getName(),
                                   url);

                if (entry.getDescription().length() > 0) {
                    KmlUtil.description(link, entry.getDescription());
                }
                KmlUtil.visible(link, false);
                KmlUtil.open(link, false);
                link.setAttribute(KmlUtil.ATTR_ID, entry.getId());
            } else if (entry.hasLocationDefined() || entry.hasAreaDefined()) {
                double[] lonlat;
                if (entry.hasAreaDefined()) {
                    lonlat = entry.getCenter();
                } else {
                    lonlat = entry.getLocation();
                }
                String link = HtmlUtils.href(
                                  request.getAbsoluteUrl(
                                      request.entryUrl(
                                          getRepository().URL_ENTRY_SHOW,
                                          entry)), entry.getName());
                String  desc    = link + entry.getDescription();
                boolean isImage = entry.getResource().isImage();
                if (isImage) {
                    String thumbUrl =
                        request.getAbsoluteUrl(
                            HtmlUtils.url(
                                request.url(repository.URL_ENTRY_GET) + "/"
                                + getStorageManager().getFileTail(
                                    entry), ARG_ENTRYID, entry.getId(),
                                            ARG_IMAGEWIDTH, "500"));
                    desc = desc + "<br>" + HtmlUtils.img(thumbUrl, "", "");
                }
                Element placemark = KmlUtil.placemark(folder,
                                        entry.getName(), desc, lonlat[0],
                                        lonlat[1], entry.hasAltitudeTop()
                        ? entry.getAltitudeTop()
                        : (entry.hasAltitudeBottom()
                           ? entry.getAltitudeBottom()
                           : 0), null);

                KmlUtil.visible(placemark, true);

                if (isImage) {
                    List<Metadata> metadataList =
                        getMetadataManager().getMetadata(entry);
                    for (Metadata metadata : metadataList) {
                        if (metadata.getType().equals(
                                JpegMetadataHandler.TYPE_CAMERA_DIRECTION)) {
                            double dir =
                                Double.parseDouble(metadata.getAttr1());
                            LatLonPointImpl fromPt =
                                new LatLonPointImpl(lonlat[0], lonlat[1]);
                            LatLonPointImpl pt = Bearing.findPoint(fromPt,
                                                     dir, 0.25, null);
                            Element bearingPlacemark =
                                KmlUtil.placemark(folder, "Bearing", null,
                                    new float[][] {
                                { (float) fromPt.getLatitude(),
                                  (float) pt.getLatitude() },
                                { (float) fromPt.getLongitude(),
                                  (float) pt.getLongitude() }
                            }, Color.red, 2);
                            KmlUtil.visible(bearingPlacemark, false);
                            break;
                        }
                    }

                }


            }
        }

        StringBuffer sb = new StringBuffer(XmlUtil.XML_HEADER);
        sb.append(XmlUtil.toString(root));
        return new Result(title, sb, "application/vnd.google-earth.kml+xml");

    }



    /**
     * _more_
     *
     * @param parent _more_
     * @param name _more_
     * @param description _more_
     * @param url _more_
     * @param north _more_
     * @param south _more_
     * @param east _more_
     * @param west _more_
     * @param visible _more_
     *
     * @return _more_
     */
    public static Element myGroundOverlay(Element parent, String name,
                                          String description, String url,
                                          double north, double south,
                                          double east, double west,
                                          boolean visible) {
        Element node = KmlUtil.makeElement(parent, KmlUtil.TAG_GROUNDOVERLAY);
        KmlUtil.name(node, name);
        KmlUtil.description(node, description);
        KmlUtil.visible(node, visible);
        Element icon = KmlUtil.makeElement(node, KmlUtil.TAG_ICON);
        Element href = KmlUtil.makeText(icon, KmlUtil.TAG_HREF, url);
        Element llb  = KmlUtil.makeElement(node, KmlUtil.TAG_LATLONBOX);
        KmlUtil.makeText(llb, KmlUtil.TAG_NORTH, "" + north);
        KmlUtil.makeText(llb, KmlUtil.TAG_SOUTH, "" + south);
        KmlUtil.makeText(llb, KmlUtil.TAG_EAST, "" + east);
        KmlUtil.makeText(llb, KmlUtil.TAG_WEST, "" + west);
        return node;
    }



    /**
     * _more_
     *
     * @param request _more_
     * @param entry _more_
     *
     * @return _more_
     */
    public static String getKmlUrl(Request request, Entry entry) {
        if (isLatLonImage(entry)) {
            return request.getAbsoluteUrl(
                request.url(
                    request.getRepository().URL_ENTRY_SHOW, ARG_ENTRYID,
                    entry.getId(), ARG_OUTPUT, OUTPUT_KML, ARG_VISIBLE,
                    "true"));
        }
        if ( !isKml(entry)) {
            return null;
        }
        String url;
        if (entry.getResource().isFile()) {
            String fileTail =
                request.getRepository().getStorageManager().getFileTail(
                    entry);
            url = HtmlUtils.url(
                request.url(request.getRepository().URL_ENTRY_GET) + "/"
                + fileTail, ARG_ENTRYID, entry.getId());
            return request.getAbsoluteUrl(url);
        } else if (entry.getResource().isUrl()) {
            return entry.getResource().getPath();
        }
        return null;
    }


    /**
     * _more_
     *
     * @param entry _more_
     *
     * @return _more_
     */
    public static boolean isKml(Entry entry) {
        String resource = entry.getResource().getPath();
        if ((resource != null)
                && (IOUtil.hasSuffix(resource, "kml")
                    || IOUtil.hasSuffix(resource, "kmz"))) {
            return true;
        }
        return false;
    }

    /**
     * _more_
     *
     * @param l _more_
     * @param dflt _more_
     *
     * @return _more_
     */
    public static double getLocation(double l, double dflt) {
        if ((l == l) && (l != Entry.NONGEO)) {
            return l;
        }
        return dflt;
    }


    /**
     * _more_
     *
     * @param entry _more_
     *
     * @return _more_
     */
    public static boolean isLatLonImage(Entry entry) {
        return entry.getType().equals("latlonimage")
               && entry.getResource().isImage();
    }



}
