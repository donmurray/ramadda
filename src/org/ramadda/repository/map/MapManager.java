/*
* Copyright 2008-2012 Jeff McWhirter/ramadda.org
*                     Don Murray/CU-CIRES
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

package org.ramadda.repository.map;


import org.ramadda.repository.*;
import org.ramadda.repository.metadata.*;
import org.ramadda.repository.output.KmlOutputHandler;
import org.ramadda.repository.output.MapOutputHandler;
import org.ramadda.repository.output.OutputHandler;


import ucar.unidata.geoloc.LatLonRect;
import ucar.unidata.geoloc.Bearing;
import ucar.unidata.geoloc.LatLonPointImpl;
import java.awt.geom.Rectangle2D;


import ucar.unidata.util.Counter;
import ucar.unidata.util.DateUtil;
import ucar.unidata.util.HtmlUtil;

import ucar.unidata.util.IOUtil;
import ucar.unidata.util.LogUtil;
import ucar.unidata.util.Misc;
import ucar.unidata.util.PatternFileFilter;
import ucar.unidata.util.PluginClassLoader;

import ucar.unidata.util.StringUtil;
import ucar.unidata.util.TwoFacedObject;

import ucar.unidata.xml.XmlUtil;

import java.util.ArrayList;
import java.util.Hashtable;
import java.util.List;





/**
 * This class provides a variety of mapping services, e.g., map display and map form selector
 *
 * @author Jeff McWhirter
 * @version $Revision: 1.3 $
 */
public class MapManager extends RepositoryManager {


    /** default height for GE plugin */
    public static int DFLT_EARTH_HEIGHT = 500;

    /** default width for the entries list */
    public static final int EARTH_ENTRIES_WIDTH = 150;

    /** GoogleEarth keys */
    private List<List<String>> geKeys;

    /**
     * Create a new MapManager for the repository
     *
     * @param repository  the repository
     */
    public MapManager(Repository repository) {
        super(repository);
    }


    /**
     * Should maps be shown?
     *
     * @return  true if okay to show maps
     * @deprecated
     */
    public boolean shouldShowMaps() {
        return showMaps();
    }

    /**
     * Should maps be shown?
     *
     * @return the show maps property value
     */
    public boolean showMaps() {
        return getRepository().getProperty(PROP_SHOWMAP, true);
    }





    /**
     * Create a map
     *
     * @param request      the Request
     * @param forSelection true if map used for selection
     *
     * @return a map information holder
     */
    public MapInfo createMap(Request request, boolean forSelection) {
        return createMap(request, MapInfo.DFLT_WIDTH, MapInfo.DFLT_HEIGHT,
                         forSelection);
    }


    /**
     * Create a map
     *
     * @param request      the Request
     * @param width        map width
     * @param height       map height
     * @param forSelection true if map used for selection
     *
     * @return a map information holder
     */
    public MapInfo createMap(Request request, int width, int height,
                             boolean forSelection) {


        MapInfo mapInfo = new MapInfo(getRepository(), width, height,
                                      forSelection);

        if ( !showMaps()) {
            return mapInfo;
        }

        if (request.getExtraProperty("initmap") == null) {
            mapInfo.addHtml(
                HtmlUtil.cssLink(
                    fileUrl("/openlayers/theme/default/style.css")));
            mapInfo.addHtml("\n");
            mapInfo.addHtml("\n");
            mapInfo.addHtml(
                HtmlUtil.importJS(fileUrl("/openlayers/OpenLayers.js")));
            mapInfo.addHtml("\n");
            /*
            mapInfo.addHtml(
                HtmlUtil.importJS(
                    "http://api.maps.yahoo.com/ajaxymap?v=3.0&appid=euzuro-openlayers"));
            */
            mapInfo.addHtml(
                HtmlUtil.importJS(
                    "http://maps.google.com/maps/api/js?v=3.5&amp;sensor=false"));
            mapInfo.addHtml("\n");
            //            mapInfo.addHtml(HtmlUtil.importJS("http://dev.virtualearth.net/mapcontrol/mapcontrol.ashx?v=6.1"));
            //            mapInfo.addHtml(HtmlUtil.importJS("http://maps.google.com/maps/api/js?v=3.2&amp;sensor=false"));
            mapInfo.addHtml(HtmlUtil.importJS(fileUrl("/ramaddamap.js")));
            mapInfo.addHtml("\n");
            mapInfo.addHtml(HtmlUtil.cssLink(fileUrl("/ramaddamap.css")));
            mapInfo.addHtml("\n");
            request.putExtraProperty("initmap", "");
        }

        return mapInfo;
    }


    /**
     * Apply the admin configuration
     *
     * @param request the Request
     */
    public void applyAdminConfig(Request request) {
        geKeys = null;
    }

    /**
     * Is GoogleEarth plugin enabled?
     *
     * @param request  the Request
     *
     * @return true if enabled
     */
    public boolean isGoogleEarthEnabled(Request request) {
        //Exclude iphone, android and linux
        if (request.isMobile()) {
            return false;
        }
        String userAgent = request.getUserAgent("").toLowerCase();
        if (userAgent.indexOf("linux") >= 0) {
            return false;
        }

        return getGoogleMapsKey(request) != null;
    }

    /**
     * Get the GoogleMaps key for the Request
     *
     * @param request the Request
     *
     * @return  a list of GE keys
     */
    public String[] getGoogleMapsKey(Request request) {
        if (geKeys == null) {
            String geAPIKeys = getProperty(PROP_GOOGLEAPIKEYS, null);
            if ((geAPIKeys == null) || (geAPIKeys.trim().length() == 0)) {
                return null;
            }
            List<List<String>> tmpKeys = new ArrayList<List<String>>();
            for (String line :
                    StringUtil.split(geAPIKeys, "\n", true, true)) {
                List<String> toks = StringUtil.split(line, ";", true, false);
                if (toks.size() > 1) {
                    tmpKeys.add(toks);
                }
            }
            geKeys = tmpKeys;
        }
        String hostname         = request.getServerName();
        int    port             = request.getServerPort();
        String hostnameWithPort = hostname + ":" + port;
        for (String h : new String[] { hostnameWithPort, hostname }) {
            // System.err.println("hostname:" + hostname);
            for (List<String> tuple : geKeys) {
                String server = tuple.get(0);
                // check to see if this matches me 
                //            System.err.println("    server:" + server);
                if (server.equals("*") || hostname.endsWith(h)) {
                    String mapsKey = tuple.get(1);
                    if (tuple.size() > 2) {
                        return new String[] { mapsKey, tuple.get(2) };
                    } else {
                        return new String[] { mapsKey, null };
                    }
                }
            }
        }

        return null;
    }




    /**
     * Get the GoogleEarth plugin html
     *
     * @param request   The Request
     * @param sb        StringBuffer for passing strings
     * @param width     width of GE plugin
     * @param height    height of GE plugin
     * @param url       the KML URL
     *
     * @return  the html for the plugin
     *
     * @throws Exception  problem creating the HTML
     */
    public String getGoogleEarthPlugin(Request request, Appendable sb,
                                       int width, int height, String url)
            throws Exception {

        String[] keyAndOther = getGoogleMapsKey(request);
        if (keyAndOther == null) {
            sb.append("Google Earth is not enabled");

            return null;
        }

        String otherOpts = "";
        String mapsKey   = "";
        if ( !keyAndOther[0].isEmpty()) {
            mapsKey = "?key=" + keyAndOther[0];
        }
        if (keyAndOther[1] != null) {
            otherOpts = ", {\"other_params\":\"" + keyAndOther[1] + "\"}";
        }
        Integer currentId = (Integer) request.getExtraProperty("ge.id");
        int     nextNum   = 1;
        if (currentId != null) {
            nextNum = currentId.intValue() + 1;
        }
        request.putExtraProperty("ge.id", new Integer(nextNum));
        String id = "map3d" + nextNum;

        if (request.getExtraProperty("initgooglearth") == null) {
            sb.append(HtmlUtil.importJS("http://www.google.com/jsapi"
                                        + mapsKey));
            sb.append(HtmlUtil.importJS(fileUrl("/google/googleearth.js")));
            sb.append(
                HtmlUtil.importJS(
                    fileUrl("/google/extensions-0.2.1.pack.js")));
            sb.append(HtmlUtil.script("google.load(\"earth\", \"1\""
                                      + otherOpts + ");"));
            request.putExtraProperty("initgooglearth", "");
        }



        String style = "";
        if (width > 0) {
            style += "width:" + width + "px; ";
        }
        if (height <= 0) {
            height = DFLT_EARTH_HEIGHT;
        }
        style += "height:" + height + "px; ";

        String earthHtml =
            HtmlUtil.div("",
                         HtmlUtil.id(id) + HtmlUtil.style(style)
                         + HtmlUtil.cssClass(CSS_CLASS_EARTH_CONTAINER));
        sb.append("\n");
        sb.append(earthHtml);
        sb.append(HtmlUtil.italics(msgLabel("On click")));
        sb.append(HtmlUtil.space(2));
        sb.append(HtmlUtil.checkbox("tmp", "true", true,
                                    HtmlUtil.id("googleearth.showdetails")));
        sb.append("\n");
        sb.append(HtmlUtil.space(1));
        sb.append(HtmlUtil.italics(msg("Show details")));
        sb.append(HtmlUtil.space(2));
        sb.append(HtmlUtil.checkbox("tmp", "true", true,
                                    HtmlUtil.id("googleearth.zoomonclick")));
        sb.append(HtmlUtil.space(1));
        sb.append(HtmlUtil.italics(msg("Zoom")));


        sb.append(HtmlUtil.script("var  " + id + " = new RamaddaEarth("
                                  + HtmlUtil.squote(id) + ", "
                                  + ((url == null)
                                     ? "null"
                                     : HtmlUtil.squote(url)) + ");\n"));

        return id;
    }


    /**
     * Get the HTML for the entries
     *
     * @param request      the Request
     * @param entries      the Entrys
     * @param sb           StringBuffer to pass info back
     * @param width        width of GE plugin
     * @param height       height of GE plugin
     * @param includeList  true to include a list of entries
     * @param justPoints   true to just make the points
     *
     * @throws Exception problem creating the plugin
     */
    public void getGoogleEarth(Request request, List<Entry> entries,
                               StringBuffer sb, int width, int height,
                               boolean includeList, boolean justPoints)
            throws Exception {



        StringBuffer mapSB = new StringBuffer();

        String       id = getMapManager().getGoogleEarthPlugin(request, mapSB,
                        width, height, null);

        StringBuffer                    js         = new StringBuffer();
        List<String>                    categories = new ArrayList<String>();
        Hashtable<String, StringBuffer> catMap     = new Hashtable<String,
                                                     StringBuffer>();
        int kmlCnt = 0;
        for (Entry entry : entries) {
            String kmlUrl = KmlOutputHandler.getKmlUrl(request, entry);
            if ((kmlUrl == null)
                    && !(entry.hasLocationDefined()
                         || entry.hasAreaDefined())) {
                continue;
            }

            String       category = entry.getTypeHandler().getCategory(entry);
            StringBuffer catSB    = catMap.get(category);
            if (catSB == null) {
                catMap.put(category, catSB = new StringBuffer());
                categories.add(category);
            }
            String call = id + ".entryClicked("
                          + HtmlUtil.squote(entry.getId()) + ");";
            catSB.append(HtmlUtil.open(HtmlUtil.TAG_DIV,
                                       HtmlUtil.cssClass(CSS_CLASS_EARTH_NAV)
                                       + "" /*HtmlUtil.onMouseClick(call)*/));
            boolean visible = true;
            //If there are lots of kmls then don't load all of them
            if (kmlUrl != null) {
                visible = (kmlCnt++ < 3);
            }
            catSB.append(
                "<table cellspacing=0 cellpadding=0  width=100%><tr><td>");
            catSB.append(HtmlUtil.checkbox("tmp", "true", visible,
                    HtmlUtil.style("margin:0px;padding:0px;margin-right:5px;padding-bottom:10px;")
                    + HtmlUtil.id("googleearth.visibility." + entry.getId())
                    + HtmlUtil.onMouseClick(id + ".togglePlacemarkVisible("
                        + HtmlUtil.squote(entry.getId()) + ")")));

            String iconUrl = getEntryManager().getIconUrl(request, entry);
            catSB.append(
                HtmlUtil.href(
                    getEntryManager().getEntryURL(request, entry),
                    HtmlUtil.img(iconUrl, msg("Click to view entry details"))
                    + " " + entry.getName()));
            catSB.append("</td><td align=right>");
            catSB.append(HtmlUtil.space(2));
            double lat = entry.getSouth();
            double lon = entry.getEast();
            //            catSB.append("<a href=\"javascript:" + call +"\">"
            //                         + entry.getName() + "</a><br>");
            //HtmlUtil.onMouseClick(call);

            /*
            catSB.append(
                         HtmlUtil.href(
                                       "javascript:" + call, entry.getName(),
                                       HtmlUtil.cssClass(CSS_CLASS_EARTH_LINK)));
            */
            catSB.append(
                HtmlUtil.href(
                    "javascript:" + call,
                    HtmlUtil.img(
                        getRepository().iconUrl(ICON_MAP_NAV),
                        "View entry"), HtmlUtil.cssClass(
                            CSS_CLASS_EARTH_LINK)));
            catSB.append("</td></tr></table>");
            catSB.append(HtmlUtil.close(HtmlUtil.TAG_DIV));

            String         pointsString = "null";
            boolean        hasPolygon   = false;
            List<Metadata> metadataList =
                getMetadataManager().getMetadata(entry);
            for (Metadata metadata : metadataList) {
                if ( !metadata.getType().equals(
                        MetadataHandler.TYPE_SPATIAL_POLYGON)) {
                    continue;
                }
                List<double[]> points   = new ArrayList<double[]>();
                String         s        = metadata.getAttr1();
                StringBuffer   pointsSB = new StringBuffer();
                for (String pair : StringUtil.split(s, ";", true, true)) {
                    List<String> toks = StringUtil.splitUpTo(pair, ",", 2);
                    if (toks.size() != 2) {
                        continue;
                    }
                    double polyLat = Misc.decodeLatLon(toks.get(0));
                    double polyLon = Misc.decodeLatLon(toks.get(1));
                    if (pointsSB.length() == 0) {
                        pointsSB.append("new Array(");
                    } else {
                        pointsSB.append(",");
                    }
                    pointsSB.append(polyLat);
                    pointsSB.append(",");
                    pointsSB.append(polyLon);
                }
                hasPolygon = true;
                pointsSB.append(")");
                pointsString = pointsSB.toString();
            }

            //            hasPolygon = false;
            if ((kmlUrl == null) && !hasPolygon && entry.hasAreaDefined()
                    && !justPoints) {
                pointsString = "new Array(" + entry.getNorth() + ","
                               + entry.getWest() + "," + entry.getNorth()
                               + "," + entry.getEast() + ","
                               + entry.getSouth() + "," + entry.getEast()
                               + "," + entry.getSouth() + ","
                               + entry.getWest() + "," + entry.getNorth()
                               + "," + entry.getWest() + ")";
            }

            String name = entry.getName();

            name = name.replace("\r", " ");
            name = name.replace("\n", " ");
            name = name.replace("\"", "\\\"");
            name = name.replace("'", "\\'");

            String desc = HtmlUtil.img(iconUrl)
                          + getEntryManager().getEntryLink(request, entry);
            desc = desc.replace("\r", " ");
            desc = desc.replace("\n", " ");
            desc = desc.replace("\"", "\\\"");
            desc = desc.replace("'", "\\'");

            if (kmlUrl == null) {
                kmlUrl = "null";
            } else {
                kmlUrl = HtmlUtil.squote(kmlUrl);
            }

            String detailsUrl =
                HtmlUtil.url(getRepository().URL_ENTRY_SHOW.getUrlPath(),
                             new String[] { ARG_ENTRYID,
                                            entry.getId(), ARG_OUTPUT,
                                            "mapinfo" });

            String fromTime = "null";
            String toTime   = "null";
            if (entry.getCreateDate() != entry.getStartDate()) {
                fromTime = HtmlUtil.squote(
                    DateUtil.getTimeAsISO8601(entry.getStartDate()));
                if (entry.getStartDate() != entry.getEndDate()) {
                    toTime = HtmlUtil.squote(
                        DateUtil.getTimeAsISO8601(entry.getEndDate()));
                }
            }


            js.append(
                HtmlUtil.call(
                    id + ".addPlacemark",
                    HtmlUtil.comma(
                        HtmlUtil.squote(entry.getId()),
                            HtmlUtil.squote(name), HtmlUtil.squote(desc),
                                "" + lat, "" + lon) + ","
                                    + HtmlUtil.squote(detailsUrl) + ","
                                        + HtmlUtil.squote(
                                            request.getAbsoluteUrl(
                                                iconUrl)) + ","
                                                    + pointsString + ","
                                                        + kmlUrl + ","
                                                            + fromTime + ","
                                                                + toTime));
            js.append("\n");
        }

        if (height <= 0) {
            height = DFLT_EARTH_HEIGHT;
        }


        if (includeList) {
            sb.append(
                "<table border=\"0\" width=\"100%\"><tr valign=\"top\">");
            sb.append("<td width=\"250\" style=\"max-width:250px;\">");
            sb.append(
                HtmlUtil.open(
                    HtmlUtil.TAG_DIV,
                    HtmlUtil.cssClass(CSS_CLASS_EARTH_ENTRIES)
                    + HtmlUtil.style(
                        "max-height:" + height + "px; overflow-y: auto;")));

            boolean doToggle = (entries.size() > 5)
                               && (categories.size() > 1);
            for (String category : categories) {
                StringBuffer catSB = catMap.get(category);
                if (doToggle) {
                    sb.append(HtmlUtil.makeShowHideBlock(category,
                            catSB.toString(), true));
                } else {
                    sb.append(HtmlUtil.b(category));
                    sb.append(HtmlUtil.br());
                    sb.append(catSB);
                }
            }
            sb.append(HtmlUtil.close(HtmlUtil.TAG_DIV));
            sb.append("</td>");
            sb.append("<td align=left>");
        }
        sb.append(mapSB);
        sb.append(HtmlUtil.script(js.toString()));
        if (includeList) {
            sb.append("</td></tr></table>");
        }

    }


    /**
     * Make the info bubble for the map popups
     *
     * @param request   the Request
     * @param entry     the Entry
     *
     * @return  the HTML for the popup
     *
     * @throws Exception   problem getting the entry info
     */
    public String makeInfoBubble(Request request, Entry entry)
            throws Exception {
        String fromEntry = entry.getTypeHandler().getMapInfoBubble(request,
                               entry);
        if (fromEntry != null) {
            return fromEntry;
        }
        StringBuffer info = new StringBuffer("<table>");
        info.append(entry.getTypeHandler().getInnerEntryContent(entry,
                request, OutputHandler.OUTPUT_HTML, true, false, false));

        List<String> urls = new ArrayList<String>();
        getMetadataManager().getThumbnailUrls(request, entry, urls);
        boolean isImage = entry.getResource().isImage();
        if ( !isImage && (urls.size() > 0)) {
            info.append("<tr><td colspan=2>"
                        + HtmlUtil.img(urls.get(0), "", " width=300 ")
                        + "</td></tr>");
        }
        info.append("</table>");

        if (isImage) {
            String thumbUrl = request.getAbsoluteUrl(
                                  HtmlUtil.url(
                                      request.url(repository.URL_ENTRY_GET)
                                      + "/"
                                      + getStorageManager().getFileTail(
                                          entry), ARG_ENTRYID, entry.getId(),
                                              ARG_IMAGEWIDTH, "300"));
            info.append(HtmlUtil.center(HtmlUtil.img(thumbUrl, "", "")));

        }


        return info.toString();


    }



    /**
     * Get the map information
     *
     * @param request       the Request
     * @param entriesToUse  the list of Entrys
     * @param sb            StringBuffer to pass back html
     * @param width         width of the map
     * @param height        height of the map
     * @param detailed      detailed or not
     * @param haveBearingLines   true if plot bearing lines
     *
     * @return MapInfo (not really used)
     *
     * @throws Exception  problem creating map
     */
    public MapInfo getMap(Request request, List<Entry> entriesToUse,
                          StringBuffer sb, int width, int height,
                          boolean detailed, boolean[] haveBearingLines)
            throws Exception {
        return getMap(request, entriesToUse, sb, width, height, detailed,
                      haveBearingLines, false);
    }


    /**
     * Get the map information
     *
     * @param request       the Request
     * @param entriesToUse  the list of Entrys
     * @param sb            StringBuffer to pass back html
     * @param width         width of the map
     * @param height        height of the map
     * @param detailed      detailed or not
     * @param haveBearingLines   true if plot bearing lines
     * @param listentries  if true, include the entries on the side
     *
     * @return MapInfo (not really used)
     *
     * @throws Exception  problem creating map
     */
    public MapInfo getMap(Request request, List<Entry> entriesToUse,
                          StringBuffer sb, int width, int height,
                          boolean detailed, boolean[] haveBearingLines,
                          boolean listentries)
            throws Exception {
        MapInfo map = getRepository().getMapManager().createMap(request,
                          width, height, false);
        if (map == null) {
            return map;
        }
        addToMap(request, map, entriesToUse, detailed, haveBearingLines,
                 true);

        Rectangle2D.Double bounds = getEntryManager().getBounds(entriesToUse);
        map.centerOn(bounds);
        if (listentries) {
            StringBuffer entryBuff = new StringBuffer();
            for (Entry entry : entriesToUse) {
                if (entry.hasLocationDefined() || entry.hasAreaDefined()) {
                    entryBuff.append(
                        "<table cellspacing=0 cellpadding=0  width=100%><tr><td>");
                    entryBuff.append("<td>");
                    String iconUrl = getEntryManager().getIconUrl(request,
                                         entry);
                    entryBuff.append(
                        HtmlUtil.href(
                            getEntryManager().getEntryURL(request, entry),
                            HtmlUtil.img(
                                iconUrl,
                                msg("Click to view entry details")) + " "
                                    + entry.getName()));
                    entryBuff.append("</td><td align=right>");
                    entryBuff.append(
                        HtmlUtil.href(
                            "javascript:" + map.getVariableName()
                            + ".hiliteMarker(" + sqt(entry.getId())
                            + ");", HtmlUtil.img(
                                getRepository().iconUrl(
                                    ICON_MAP_NAV), "View entry")));
                    entryBuff.append("</td></tr></table>");
                }
            }



            sb.append(
                "<table border=\"0\" width=\"100%\"><tr valign=\"top\">");
            sb.append("<td width=\"250\">");
            sb.append(
                HtmlUtil.open(
                    HtmlUtil.TAG_DIV,
                    HtmlUtil.style(
                        "max-width:250px; overflow-x: auto;  overflow-y: auto; max-height:"
                        + map.getHeight())));
            sb.append(entryBuff);
            sb.append(HtmlUtil.close(HtmlUtil.TAG_DIV));
            sb.append("</td>");
            sb.append("<td>");
            sb.append(map.getHtml());
            sb.append("</td>");
            sb.append("</tr></table>");
        } else {
            sb.append(map.getHtml());
        }

        return map;
    }

    /**
     * Add the entry to the map
     *
     * @param request      The request
     * @param map          the map information
     * @param entriesToUse the Entrys to use
     * @param detailed     deatiled or not
     * @param haveBearingLines   have bearing lines flag
     * @param screenBigRects     handle big rectangles
     *
     * @throws Exception  problem adding entries to map
     */
    public void addToMap(Request request, MapInfo map,
                         List<Entry> entriesToUse, boolean detailed,
                         boolean[] haveBearingLines, boolean screenBigRects)
            throws Exception {
        screenBigRects = false;
        int cnt = 0;
        for (Entry entry : entriesToUse) {
            if (entry.hasAreaDefined()) {
                cnt++;
            }
        }


        boolean       makeRectangles = cnt <= 100;
        MapProperties mapProperties  = new MapProperties("blue", true);
        makeRectangles = true;

        for (Entry entry : entriesToUse) {
            String         idBase       = entry.getId();
            List<Metadata> metadataList =
                getMetadataManager().getMetadata(entry);

            if (makeRectangles) {
                boolean didMetadata = map.addSpatialMetadata(entry,
                                          metadataList);
                if (detailed) {
                    entry.getTypeHandler().addToMap(request, entry, map);
                }
                if (entry.hasAreaDefined() && !didMetadata) {
                    if ( !screenBigRects
                            || (Math.abs(entry.getEast() - entry.getWest())
                                < 90)) {
                        map.addBox(entry, mapProperties);
                    }
                }
            }


            if (entry.hasLocationDefined() || entry.hasAreaDefined()) {

                double[] location;
                if (makeRectangles || !entry.hasAreaDefined()) {
                    location = entry.getLocation();
                } else {
                    location = entry.getCenter();
                }

                for (Metadata metadata : metadataList) {
                    if (metadata.getType().equals(
                            JpegMetadataHandler.TYPE_CAMERA_DIRECTION)) {
                        double dir = Double.parseDouble(metadata.getAttr1());
                        LatLonPointImpl fromPt =
                            new LatLonPointImpl(location[0], location[1]);
                        LatLonPointImpl pt = Bearing.findPoint(fromPt, dir,
                                                 0.25, null);
                        map.addLine(entry.getId(), fromPt, pt);
                        if (haveBearingLines != null) {
                            haveBearingLines[0] = true;
                        }

                        break;
                    }
                }
                String infoHtml = getMapManager().makeInfoBubble(request,
                                      entry);
                infoHtml = infoHtml.replace("\r", " ");
                infoHtml = infoHtml.replace("\n", " ");
                infoHtml = infoHtml.replace("\"", "\\\"");
                infoHtml = infoHtml.replace("'", "\\'");
                infoHtml = getRepository().translate(request, infoHtml);
                String icon = getEntryManager().getIconUrl(request, entry);
                map.addMarker(entry.getId(),
                              new LatLonPointImpl(Math.max(-80,
                                  Math.min(80,
                                           location[0])), location[1]), icon,
                                               infoHtml);
            }
        }
    }


    /**
     * Quote a String with double quotes (&quot;)
     *
     * @param s  the String
     *
     * @return  the quoted String
     */
    private static String qt(String s) {
        return "\"" + s + "\"";
    }

    /**
     * Quote a String with single quotes (')
     *
     * @param s  the String
     *
     * @return  the quoted String
     */
    private static String sqt(String s) {
        return "'" + s + "'";
    }



    /**
     * Create a lat/lon point string for OpenLayers
     *
     * @param lat  the latitude
     * @param lon  the longitude
     *
     * @return  the OpenLayers String
     */
    public static String llp(double lat, double lon) {
        if (lat < -90) {
            lat = -90;
        }
        if (lat > 90) {
            lat = 90;
        }
        if (lon < -180) {
            lon = -180;
        }
        if (lon > 180) {
            lon = 180;
        }

        return "new OpenLayers.LonLat(" + lon + "," + lat + ")";
    }



}
