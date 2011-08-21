/*
 * Copyright 2008-2011 Jeff McWhirter/ramadda.org
 * Copyright 2010-2011 Don Murray/NOAA
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
 * 
 */

package org.ramadda.repository.map;


import org.ramadda.repository.*;
import org.ramadda.repository.output.MapOutputHandler;

import ucar.unidata.geoloc.LatLonRect;


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
import java.util.List;





/**
 * This class provides a variety of mapping services, e.g., map display and map form selector
 *
 * @author Jeff McWhirter
 * @version $Revision: 1.3 $
 */
public class MapManager extends RepositoryManager {

    /**
     * _more_
     *
     * @param repository _more_
     */
    public MapManager(Repository repository) {
        super(repository);
    }


    /**
     * _more_
     *
     * @return _more_
     */
    public boolean shouldShowMaps() {
        return showMaps();
    }

    /**
     * _more_
     *
     * @return _more_
     */
    public boolean showMaps() {
        return getRepository().getProperty(PROP_SHOWMAP, true);
    }





    /**
     * _more_
     *
     * @param request _more_
     * @param forSelection _more_
     *
     * @return _more_
     */
    public MapInfo createMap(Request request, boolean forSelection) {
        return createMap(request, MapInfo.DFLT_WIDTH, MapInfo.DFLT_HEIGHT,
                         forSelection);
    }


    /**
     * _more_
     *
     * @param request _more_
     * @param width _more_
     * @param height _more_
     * @param forSelection _more_
     *
     * @return _more_
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
            mapInfo.addHtml(
                HtmlUtil.importJS(fileUrl("/openlayers/OpenLayers.js")));
            mapInfo.addHtml("\n");
            mapInfo.addHtml(
                HtmlUtil.importJS(
                    "http://api.maps.yahoo.com/ajaxymap?v=3.0&appid=euzuro-openlayers"));
            mapInfo.addHtml("\n");
            //            mapInfo.addHtml(HtmlUtil.importJS("http://dev.virtualearth.net/mapcontrol/mapcontrol.ashx?v=6.1"));
            //            mapInfo.addHtml(HtmlUtil.importJS("http://maps.google.com/maps/api/js?v=3.2&amp;sensor=false"));
            mapInfo.addHtml(HtmlUtil.importJS(fileUrl("/ramaddamap.js")));
            mapInfo.addHtml("\n");
            request.putExtraProperty("initmap", "");
        }

        return mapInfo;
    }


    /** _more_          */
    private List<List<String>> geKeys;

    /**
     * _more_
     *
     * @param request _more_
     */
    public void applyAdminConfig(Request request) {
        geKeys = null;
    }

    /**
     * _more_
     *
     * @param request _more_
     *
     * @return _more_
     */
    public boolean isGoogleEarthEnabled(Request request) {
        return getGoogleMapsKey(request) != null;
    }

    /**
     * _more_
     *
     * @param request _more_
     *
     * @return _more_
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
                List<String> toks = StringUtil.split(line, ":", true, true);
                if (toks.size() > 1) {
                    tmpKeys.add(toks);
                }
            }
            geKeys = tmpKeys;
        }
        String hostname = request.getServerName();
        //System.err.println(hostname);
        for (List<String> tuple : geKeys) {
            String server = tuple.get(0);
            // check to see if this matches me 
            if (server.equals("*") || (hostname.indexOf(server) >= 0)) {  // match
                String mapsKey = tuple.get(1);
                if (tuple.size() > 2) {
                    return new String[] { mapsKey, tuple.get(2) };
                } else {
                    return new String[] { mapsKey, null };
                }
            }
        }
        return null;
    }




    /**
     * _more_
     *
     * @param request _more_
     * @param width _more_
     * @param height _more_
     * @param url _more_
     *
     * @return _more_
     *
     * @throws Exception _more_
     */
    public String getGoogleEarthPlugin(Request request, StringBuffer sb,
                                       int width,
                                       int height, String url)
            throws Exception {
        String id = "map3d";
        // fill in the API keys for this machine
        String   mapsKey     = "";
        String   otherOpts   = "";
        String[] keyAndOther = getGoogleMapsKey(request);
        if (keyAndOther != null) {
            mapsKey = "?key=" + keyAndOther[0];
            if (keyAndOther[1] != null) {
                otherOpts = ", {\"other_params\":\"" + keyAndOther[1] + "\"}";
            }
        }
        Integer currentId = (Integer) request.getExtraProperty("ge.id");
        int nextNum  = 1;
        if(currentId != null) {
            nextNum = currentId.intValue()+1;
        }
        request.putExtraProperty("ge.id", new Integer(nextNum));
        id = "map3d" + nextNum;

        if (request.getExtraProperty("initgooglearth") == null) {
            sb.append(
                      HtmlUtil.importJS("http://www.google.com/jsapi" +mapsKey));
            sb.append(
                HtmlUtil.importJS(fileUrl("/googleearth.js")));
            sb.append(HtmlUtil.script(
                                      "google.load(\"earth\", \"1\"" + otherOpts+");"));
            request.putExtraProperty("initgooglearth", "");
        }


        String template =
            getRepository().getResource(
                "/org/ramadda/repository/resources/googleearth/geplugin.html");
        template = template.replace("${width}", width + "");
        template = template.replace("${height}", height + "");
        template = template.replace("${id}", id);
        sb.append(template);
        sb.append(HtmlUtil.script("var  " + id + " = new GoogleEarth(" + HtmlUtil.squote(id) +", " + (url==null?"null":HtmlUtil.squote(url))+");\n"));
        return id;
    }


    public void getGoogleEarth(Request request, 
                               List<Entry> entries, StringBuffer sb,
                               int width, int height)
            throws Exception {
        sb.append(
            "<table border=\"0\" width=\"100%\"><tr valign=\"top\"><td>");

        StringBuffer mapSB = new StringBuffer();
        String id = getMapManager().getGoogleEarthPlugin(request, mapSB, width, height, null);

        StringBuffer js  = new StringBuffer();
        for (Entry entry : entries) {
            if (entry.hasLocationDefined() || entry.hasAreaDefined()) {
                sb.append(HtmlUtil.img(getEntryManager().getIconUrl(request,
                        entry)));
                sb.append(HtmlUtil.space(1));
                double lat = entry.getSouth();
                double lon = entry.getEast();
                sb.append("<a href=\"javascript:" + id +".setLocation(" +lat+"," +
                          lon + ");\">"
                          + entry.getName() + "</a><br>");
                String icon = getRepository().absoluteUrl(getEntryManager().getIconUrl(request, entry));
                String points = "null";
                if(entry.hasAreaDefined()) {
                    points = "new Array(" + 
                        entry.getNorth() +"," +
                        entry.getWest() +"," +
                        entry.getNorth() +"," +
                        entry.getEast() +"," +
                        entry.getSouth() +"," +
                        entry.getEast() +"," +
                        entry.getSouth() +"," +
                        entry.getWest() +"," +
                        entry.getNorth() +"," +
                        entry.getWest()+")";
                }
                js.append(HtmlUtil.call(
                                        id +".addPlacemark",
                                        HtmlUtil.comma(HtmlUtil.squote(entry.getName()), ""+lat, ""+lon,
                                                       HtmlUtil.squote(icon),points)));
                js.append("\n");
            }
        }
        sb.append("</td><td>");
        sb.append(mapSB);
        sb.append(HtmlUtil.script(js.toString()));
        sb.append("</td></tr></table>");
    }



}
