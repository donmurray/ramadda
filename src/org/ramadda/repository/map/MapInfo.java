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

package org.ramadda.repository.map;


import org.ramadda.repository.Entry;
import org.ramadda.repository.PageDecorator;
import org.ramadda.repository.Repository;
import org.ramadda.repository.Request;
import org.ramadda.repository.metadata.Metadata;
import org.ramadda.repository.metadata.MetadataHandler;
import org.ramadda.util.HtmlUtils;
import org.ramadda.util.MapRegion;
import org.ramadda.util.Utils;

import ucar.unidata.geoloc.LatLonPointImpl;
import ucar.unidata.geoloc.LatLonRect;
import ucar.unidata.util.Misc;
import ucar.unidata.util.StringUtil;
import ucar.unidata.util.TwoFacedObject;


import java.awt.geom.Rectangle2D;

import java.util.ArrayList;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.List;



/**
 * A MapInfo class to hold map info
 */
public class MapInfo {

    /** default box color */
    public static final String DFLT_BOX_COLOR = "blue";

    /** default map width */
    public static final int DFLT_WIDTH = 600;

    /** default map height */
    public static final int DFLT_HEIGHT = 300;

    /** The associated repository */
    private Repository repository;

    /** the map count */
    private static int cnt = 0;

    /** the map variable name */
    private String mapVarName;

    /** the width */
    private int width = DFLT_WIDTH;

    /** the height */
    private int height = DFLT_HEIGHT;

    /** is the map for selection */
    private boolean forSelection = false;

    /** list of map regions */
    private List<MapRegion> mapRegions = null;

    /** default map region */
    private String defaultMapRegion = null;

    /** the javascript buffer? */
    private StringBuilder jsBuffer = null;

    /** right side of widget */
    private StringBuilder rightSide = new StringBuilder();

    /** the html */
    private StringBuilder html = new StringBuilder();

    /** map properties */
    private Hashtable mapProps;

    /** selection label */
    private String selectionLabel;

    /** the request */
    private Request request;

    /**
     * Create a MapInfo for the associated repository
     *
     *
     * @param request    the Request
     * @param repository the associated repository
     */
    public MapInfo(Request request, Repository repository) {
        this(request, repository, DFLT_WIDTH, DFLT_HEIGHT);
    }

    /**
     * Create a MapInfo for the associated repository
     *
     *
     * @param request    the Request
     * @param repository the associated repository
     * @param width  the width of the map
     * @param height  the height of the map
     */
    public MapInfo(Request request, Repository repository, int width,
                   int height) {
        this(request, repository, width, height, false);
    }

    /**
     * Create a MapInfo for the associated repository
     *
     *
     * @param request    the Request
     * @param repository the associated repository
     * @param width  the width of the map
     * @param height  the height of the map
     * @param forSelection  true if for selecting something
     */
    public MapInfo(Request request, Repository repository, int width,
                   int height, boolean forSelection) {
        this.request      = request;
        this.repository   = repository;

        this.mapVarName   = "ramaddaMap" + (cnt++);
        this.width        = width;
        this.height       = height;
        this.forSelection = forSelection;
    }


    /**
     * Shortcut to repository.msg
     *
     * @param s  the string
     *
     * @return  the translated message
     */
    public String msg(String s) {
        return repository.msg(s);
    }


    /**
     * Shortcut to repository.msgLabel
     *
     * @param s  the string
     *
     * @return  the label
     */
    public String msgLabel(String s) {
        return repository.msgLabel(s);
    }

    /**
     * Show the maps
     *
     * @return  true if successful
     */
    public boolean showMaps() {
        return repository.getMapManager().showMaps();
    }


    /**
     * Add the html to the output
     *
     * @param s  the string
     */
    public void addHtml(String s) {
        html.append(s);
    }

    /**
     * Add JavaSript code to the output
     *
     * @param s  the JavaSript
     */
    public void addJS(String s) {
        getJS().append(s);
    }

    /**
     * Add the right side of the widget
     *
     * @param s  the HTML
     */
    public void addRightSide(String s) {
        rightSide.append(s);
    }



    /**
     * Add spatial metadata
     *
     * @param entry  the entry
     * @param metadataList  the list of metatdata
     *
     * @return true if we added some metadata
     */
    public boolean addSpatialMetadata(Entry entry,
                                      List<Metadata> metadataList) {
        boolean didone = false;
        for (Metadata metadata : metadataList) {
            if (metadata.getType().equals(
                    MetadataHandler.TYPE_SPATIAL_POLYGON)) {
                List<double[]> points = new ArrayList<double[]>();
                String         s      = metadata.getAttr1();
                for (String pair : StringUtil.split(s, ";", true, true)) {
                    List<String> toks = StringUtil.splitUpTo(pair, ",", 2);
                    if (toks.size() != 2) {
                        continue;
                    }
                    double lat = Utils.decodeLatLon(toks.get(0));
                    double lon = Utils.decodeLatLon(toks.get(1));
                    points.add(new double[] { lat, lon });
                }
                this.addLines(entry.getId() + "_polygon", points);
                didone = true;
            }
        }

        return didone;
    }


    /**
     * Get the map div
     *
     * @param contents the contents
     *
     * @return  the div tag
     */
    private String getMapDiv(String contents) {
        StringBuilder result = new StringBuilder();
        String readout =
            HtmlUtils.div("&nbsp;",
                          HtmlUtils.id("ramadda-map-latlonreadout")
                          + HtmlUtils.style("font-style:italic; width:"
                                            + width + "px;"));
        String styles =
            "border:1px #888888 solid; background-color:#7391ad; height:"
            + height + "px; ";
        if (width > 0) {
            styles += " width:" + width + "px; ";
        } else {
            styles += " width: 100%;";
        }
        result.append(HtmlUtils.div(contents,
                                    HtmlUtils.style(styles) + " "
                                    + HtmlUtils.id(mapVarName)));
        result.append("\n");
        result.append(readout);
        result.append("\n");

        return result.toString();
    }


    /**
     * Get the HTML for this map
     *
     * @return  the HTML
     */
    public String getHtml() {
        if ( !showMaps()) {
            return getMapDiv("&nbsp;Maps not available");
        }

        repository.getPageHandler().addToMap(request, this);
        for (PageDecorator pageDecorator :
                repository.getPluginManager().getPageDecorators()) {
            pageDecorator.addToMap(request, this);
        }

        StringBuilder result = new StringBuilder();
        result.append(html);


        //For now don't decorate with the WMS legend popup
        /*if (rightSide.length() > 0) {
            result.append("<table width=\"100%\"><tr valign=top><td>");
        }
        */


        result.append(getMapDiv(""));
        //For now don't decorate with the WMS legend popup
        /*
        if (rightSide.length() > 0) {
            result.append("</td><td width=10%>");
            result.append(rightSide);
            result.append("</td></tr></table>");
        }
        */
        result.append(HtmlUtils.script(getJS().toString()));
        result.append("\n");

        return result.toString();
    }


    /**
     * Get the JavaScript for this map
     *
     * @return  the JavaScript
     */
    private StringBuilder getJS() {
        if (jsBuffer == null) {
            jsBuffer = new StringBuilder();
            jsBuffer.append("//mapjs\n");
            jsBuffer.append("var params = " + formatProps() + ";\n");
            jsBuffer.append("var " + mapVarName + " = new RepositoryMap("
                            + HtmlUtils.squote(mapVarName) + ", params);\n");
            jsBuffer.append("var theMap = " + mapVarName + ";\n");
            // TODO: why is this here?
            if ( !forSelection) {
                jsBuffer.append("theMap.initMap(" + forSelection + ");\n");
            }
        }

        return jsBuffer;
    }

    /**
     * Add a property for the map
     *
     * @param name   the property name
     * @param value  the value
     */
    public void addProperty(String name, Object value) {
        if (mapProps == null) {
            mapProps = new Hashtable();
        }
        mapProps.put(name, value);
    }

    /**
     * Format the properties
     *
     * @return  the properties as a Javascript string
     */
    private String formatProps() {
        StringBuilder props = new StringBuilder("{");
        if ((mapProps != null) && !mapProps.isEmpty()) {
            for (Enumeration<String> e =
                    mapProps.keys(); e.hasMoreElements(); ) {
                String key   = e.nextElement();
                Object value = mapProps.get(key);
                props.append("\n");
                props.append(key);
                props.append(":");
                if (value instanceof List) {
                    props.append("[ \n");
                    List vals = (List) value;
                    for (int i = 0; i < vals.size(); i++) {
                        props.append(
                            HtmlUtils.squote(vals.get(i).toString()));
                        if (i < vals.size() - 1) {
                            props.append(",");
                        }
                    }
                    props.append("\n]");
                } else {
                    props.append(value.toString());
                }
                if (e.hasMoreElements()) {
                    props.append(",");
                }
            }
            props.append("\n");
        }
        props.append("}");

        return props.toString();
    }

    /**
     * Make a selector
     *
     * @param arg  the argument
     * @param popup  true to make a popup
     * @param nwse  the north, south, east and west ids
     *
     * @return  the corresponding code
     */
    public String makeSelector(String arg, boolean popup, String[] nwse) {
        return makeSelector(arg, popup, nwse, "", "");
    }

    /**
     * Make a selector
     *
     * @param arg  the argument
     * @param popup  true to make a popup
     * @param nwse  the north, south, east and west ids
     * @param extraLeft  extra left text
     * @param extraTop  extra top text
     *
     * @return  the corresponding code
     */
    public String makeSelector(String arg, boolean popup, String[] nwse,
                               String extraLeft, String extraTop) {
        boolean doRegion = true;
        if (nwse == null) {
            nwse = new String[] { "", "", "", "" };
        }

        if (nwse.length == 2) {
            doRegion = false;
        }
        StringBuilder widget  = new StringBuilder();
        String        regions = "";
        if (doRegion) {
            regions = getRegionSelectorWidget(arg);
        }
        widget.append(getSelectorWidget(arg, nwse));
        if ( !showMaps()) {
            return widget.toString();
        }

        String        msg       = HtmlUtils.italics(doRegion
                ? msg("Shift-drag to select region")
                : msg("Click to select point"));

        StringBuilder sb        = new StringBuilder();
        String        clearLink = getSelectorClearLink(msg("Clear"));
        sb.append(HtmlUtils.leftRight(msg, clearLink));
        //        sb.append(HtmlUtils.br());
        //        sb.append(clearLink);
        sb.append(getMapDiv(""));
        if ((extraLeft != null) && (extraLeft.length() > 0)) {
            widget.append(HtmlUtils.br() + extraLeft);
        }

        String rightSide = null;
        String initParams = HtmlUtils.squote(arg) + "," + doRegion + ","
                            + (popup
                               ? "1"
                               : "0");

        if (popup) {
            String popupLabel = (selectionLabel != null)
                                ? selectionLabel
                                : HtmlUtils.img(
                                    repository.iconUrl("/icons/map.png"),
                                    msg("Show Map"));
            rightSide =
                HtmlUtils.space(2)
                + repository.getPageHandler().makeStickyPopup(popupLabel,
                    sb.toString(),
                    getVariableName() + ".selectionPopupInit();") +
            //                                              HtmlUtils.space(2) + clearLink
            HtmlUtils.space(2) + extraTop;
        } else {
            //rightSide = clearLink + HtmlUtils.space(2) + HtmlUtils.br()
            //            + sb.toString();
            rightSide = sb.toString();
        }

        addJS(getVariableName() + ".setSelection(" + initParams + ");\n");

        String mapStuff = HtmlUtils.table(new Object[] { widget.toString(),
                rightSide });
        StringBuilder retBuf = new StringBuilder();
        if ((regions != null) && !regions.isEmpty()) {
            retBuf.append(regions);
            retBuf.append("<div id=\"" + getVariableName() + "_mapToggle\">");
            retBuf.append(mapStuff);
            retBuf.append("</div>");
            // Hack to hide the maps if they haven't selected a custom region.
            addJS("if ($('#" + getVariableName()
                  + "_regions option:selected').val() != \"CUSTOM\") {"
                  + "$('#" + getVariableName()
                  + "_mapToggle').hide(); } else {" + getVariableName()
                  + ".initMap(" + forSelection + ");}\n");
            // Fire the map selection change to pick up the current map params
            addJS("$('#" + getVariableName() + "_regions').change();");
        } else {
            retBuf.append(mapStuff);
            // this wasn't done in the initial making of the JS
            if (forSelection && !popup) {
                addJS(getVariableName() + ".initMap(" + forSelection
                      + ");\n");
            }
        }
        retBuf.append(html);
        retBuf.append(HtmlUtils.script(getJS().toString()));

        return retBuf.toString();

        //return HtmlUtils.table(new Object[] { widget.toString(), rightSide }) + html
        //       + HtmlUtils.script(getJS().toString());
    }

    /**
     * Get the selector clear link
     *
     * @param msg  name for the clear link
     *
     * @return  the link
     */
    public String getSelectorClearLink(String msg) {
        return HtmlUtils.mouseClickHref(getVariableName()
                                        + ".selectionClear();", msg);
    }

    /**
     * _more_
     *
     *
     * @param arg _more_
     * @return _more_
     */
    public String getRegionSelectorWidget(String arg) {
        StringBuilder widget = new StringBuilder();
        if ((mapRegions != null) && (mapRegions.size() > 0)) {
            List values = new ArrayList<String>();
            //values.add(new TwoFacedObject("Select Region", ""));
            for (MapRegion region : mapRegions) {
                String value = region.getId() + "," + region.getNorth() + ","
                               + region.getWest() + "," + region.getSouth()
                               + "," + region.getEast();
                values.add(new TwoFacedObject(region.getName(), value));
            }
            values.add(new TwoFacedObject("Custom", "CUSTOM"));
            String regionSelectId = getVariableName() + "_regions";
            widget.append(HtmlUtils.hidden(arg + "_regionid", "",
                                           HtmlUtils.id(getVariableName()
                                               + "_regionid")));
            widget.append(
                HtmlUtils.select(
                    "mapregion", values, getDefaultMapRegion(),
                    HtmlUtils.id(regionSelectId)
                    + HtmlUtils.attr(
                        HtmlUtils.ATTR_ONCHANGE,
                        HtmlUtils.call(
                            "MapUtils.mapRegionSelected",
                            HtmlUtils.squote(regionSelectId),
                            HtmlUtils.squote(mapVarName)))));
        }

        return widget.toString();

    }

    /**
     * GEt the selector widget
     *
     * @param arg  the argument
     * @param nwse the N,S,E and W labels
     *
     * @return  the widget
     */
    public String getSelectorWidget(String arg, String[] nwse) {
        boolean doRegion = true;
        if (nwse == null) {
            nwse = new String[] { "", "", "", "" };
        }
        StringBuilder widget = new StringBuilder();
        if (nwse.length == 2) {
            doRegion = false;
        }

        if (doRegion) {
            widget.append(HtmlUtils.makeLatLonBox(mapVarName, arg, nwse[2],
                    nwse[0], nwse[3], nwse[1]));

        } else {
            widget.append(" ");
            widget.append(msgLabel("Latitude"));
            widget.append(" ");
            widget.append(
                HtmlUtils.input(
                    arg + ".latitude", nwse[0],
                    HtmlUtils.SIZE_5 + " "
                    + HtmlUtils.id(arg + ".latitude")) + " "
                        + msgLabel("Longitude") + " "
                        + HtmlUtils.input(
                            arg + ".longitude", nwse[1],
                            HtmlUtils.SIZE_5 + " "
                            + HtmlUtils.id(arg + ".longitude")) + " ");

        }

        return widget.toString();
    }


    /**
     * Add a box to the map
     *
     * @param entry  the map entry
     * @param properties  the properties for the box
     */
    public void addBox(Entry entry, MapBoxProperties properties) {
        addBox(entry.getId(), properties, entry.getNorth(), entry.getWest(),
               entry.getSouth(), entry.getEast());
    }

    /**
     * Add a box to the map
     *
     * @param id  the id
     * @param llr  the bounds
     * @param properties the box properties
     */
    public void addBox(String id, LatLonRect llr,
                       MapBoxProperties properties) {
        addBox(id, properties, llr.getLatMax(), llr.getLonMin(),
               llr.getLatMin(), llr.getLonMax());
    }


    /**
     * Add a box to the map
     *
     * @param id  the id
     * @param properties the box properties
     * @param north  north value
     * @param west   west value
     * @param south  south value
     * @param east   east value
     */
    public void addBox(String id, MapBoxProperties properties, double north,
                       double west, double south, double east) {
        getJS().append("var mapBoxAttributes = {\"color\":\""
                       + properties.getColor() + "\",\"selectable\": "
                       + properties.getSelectable() + ",\"zoomToExtent\": "
                       + properties.getZoomToExtent() + "};\n");
        getJS().append(mapVarName + ".addBox(" + HtmlUtils.squote(id) + ","
                       + north + "," + west + "," + south + "," + east
                       + ", mapBoxAttributes);\n");
    }


    /**
     * Add a line to the map
     *
     * @param id  the line id
     * @param fromPt  starting point
     * @param toPt    ending point
     */
    public void addLine(String id, LatLonPointImpl fromPt,
                        LatLonPointImpl toPt) {
        addLine(id, fromPt.getLatitude(), fromPt.getLongitude(),
                toPt.getLatitude(), toPt.getLongitude());
    }

    /**
     * Add a set of lines
     *
     * @param id  the lines id
     * @param pts  the points
     */
    public void addLines(String id, double[][] pts) {
        for (int i = 1; i < pts.length; i++) {
            addLine(id, pts[i - 1][0], pts[i - 1][1], pts[i][0], pts[i][1]);
        }
    }

    /**
     * Add a set of lines
     *
     * @param id  the lines id
     * @param pts  the points
     */
    public void addLines(String id, List<double[]> pts) {
        if (pts.size() == 0) {
            return;
        }
        double[] lastGoodPoint = pts.get(0);
        for (int i = 1; i < pts.size(); i++) {
            double[] currentPoint = pts.get(i);
            if (Double.isNaN(currentPoint[0])
                    || Double.isNaN(currentPoint[0])) {
                lastGoodPoint = null;

                continue;
            }
            if (lastGoodPoint != null) {
                addLine(id, lastGoodPoint[0], lastGoodPoint[1],
                        currentPoint[0], currentPoint[1]);
            }
            lastGoodPoint = currentPoint;
        }
    }


    /**
     * Add a line
     *
     * @param id  the line id
     * @param fromLat  starting lat
     * @param fromLon  starting lon
     * @param toLat    ending lat
     * @param toLon    ending lon
     */
    public void addLine(String id, double fromLat, double fromLon,
                        double toLat, double toLon) {
        getJS().append(mapVarName + ".addLine(" + HtmlUtils.squote(id) + ","
                       + fromLat + "," + fromLon + "," + toLat + "," + toLon
                       + ");\n");
    }

    /**
     * Add a marker
     *
     * @param id  the marker id
     * @param pt  the position
     * @param icon  the icon
     * @param info  the associated text
     */
    public void addMarker(String id, LatLonPointImpl pt, String icon,
                          String info) {
        addMarker(id, pt.getLatitude(), pt.getLongitude(), icon, info);
    }

    /**
     * Add a marker
     *
     * @param id  the marker id
     * @param lat  the latitude
     * @param lon  the longitude
     * @param icon  the icon
     * @param info  the associated text
     */
    public void addMarker(String id, double lat, double lon, String icon,
                          String info) {
        getJS().append(mapVarName + ".addMarker(" + HtmlUtils.squote(id)
                       + "," + llp(lat, lon) + "," + ((icon == null)
                ? "null"
                : HtmlUtils.squote(icon)) + "," + HtmlUtils.squote(info)
                                          + ");\n");
    }


    /**
     * Add a KML Url
     *
     * @param url  the URL
     */
    public void addKmlUrl(String url) {
        //TODO:
        //This doesn't work now in the js
        //        getJS().append(mapVarName + ".addKMLLayer(" + HtmlUtils.squote("TEST")+","+ HtmlUtils.squote(url)+");\n");
    }




    /**
     * Center the map on the bounds
     *
     * @param bounds  the bounds
     */
    public void centerOn(Rectangle2D.Double bounds) {
        if (bounds != null) {
            getJS().append("var bounds = new OpenLayers.Bounds("
                           + bounds.getX() + "," + bounds.getY() + ","
                           + (bounds.getX() + bounds.getWidth()) + ","
                           + (bounds.getY() + bounds.getHeight()) + ");\n");
            getJS().append(mapVarName + ".centerOnMarkers(bounds);\n");
        } else {
            center();
        }

    }


    /**
     * Center the map
     */
    public void center() {
        getJS().append(mapVarName + ".centerOnMarkers(null);\n");
    }

    /**
     * Center on the entry bounds
     *
     * @param entry  the entry
     */
    public void centerOn(Entry entry) {
        if (entry == null) {
            center();

            return;
        }
        if (entry.hasAreaDefined()) {
            centerOn(entry.getNorth(), entry.getWest(), entry.getSouth(),
                     entry.getEast());
        } else {
            center();
        }
    }


    /**
     * Center on the box defined by the N,S,E,W coords
     *
     * @param north  north edge
     * @param west   west edge
     * @param south  south edge
     * @param east   east edge
     */
    public void centerOn(double north, double west, double south,
                         double east) {
        getJS().append("var bounds = new OpenLayers.Bounds(" + west + ","
                       + south + "," + east + "," + north + ");\n");
        getJS().append(mapVarName + ".centerOnMarkers(bounds);\n");
    }


    /**
     * Get the highlight  href tag
     *
     * @param id  the id
     * @param label  the label
     *
     * @return  the href tag
     */
    public String getHiliteHref(String id, String label) {
        return "<a href=\"javascript:" + getVariableName() + ".hiliteMarker("
               + HtmlUtils.squote(id) + ");\">" + label + "</a>";
    }

    /**
     * Create a OpenLayers.LonLat string representation from a lat/lon point
     *
     * @param lat  the latitude
     * @param lon  the longitude
     *
     * @return the OpenLayer.LonLat
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


    /**
     *  Set the Width property.
     *
     *  @param value The new value for Width
     */
    public void setWidth(int value) {
        width = value;
    }

    /**
     *  Get the Width property.
     *
     *  @return The Width
     */
    public int getWidth() {
        return width;
    }

    /**
     *  Set the Height property.
     *
     *  @param value The new value for Height
     */
    public void setHeight(int value) {
        height = value;
    }

    /**
     *  Get the Height property.
     *
     *  @return The Height
     */
    public int getHeight() {
        return height;
    }





    /**
     *  Set the MapVarName property.
     *
     *  @param value The new value for MapVarName
     */
    public void setMapVarName(String value) {
        mapVarName = value;
    }

    /**
     *  Get the MapVarName property.
     *
     *  @return The MapVarName
     */
    public String getVariableName() {
        return mapVarName;
    }

    /**
     * Set the selection label
     *
     * @param l  the label
     */
    public void setSelectionLabel(String l) {
        selectionLabel = l;
    }

    /**
     * Is this for selection?
     *
     * @return  true if for selection
     */
    public boolean forSelection() {
        return forSelection;
    }


    /**
     *  Set the MapRegions property.
     *
     *  @param value The new value for MapRegions
     */
    public void setMapRegions(List<MapRegion> value) {
        mapRegions = value;
    }

    /**
     *  Get the MapRegions property.
     *
     *  @return The MapRegions
     */
    public List<MapRegion> getMapRegions() {
        return mapRegions;
    }

    /**
     *  Set the DefaultMapRegion property.
     *
     *  @param The DefaultMapRegion
     *
     * @param mapRegion _more_
     */
    public void setDefaultMapRegion(String mapRegion) {
        defaultMapRegion = mapRegion;
    }

    /**
     *  Get the DefaultMapRegion property.
     *
     *  @return The DefaultMapRegion
     */
    public String getDefaultMapRegion() {
        return defaultMapRegion;
    }


}
