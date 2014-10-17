/**
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



// google maps
var map_google_terrain = "google.terrain";
var map_google_streets = "google.streets";
var map_google_hybrid = "google.hybrid";
var map_google_satellite = "google.satellite";

// ESRI Maps
var map_esri_topo = "esri.topo";


// Microsoft maps - only work for -180 to 180
var map_ms_shaded = "ms.shaded";
var map_ms_hybrid = "ms.hybrid";
var map_ms_aerial = "ms.aerial";



// WMS maps
var map_wms_openlayers = "wms:OpenLayers WMS,http://vmap0.tiles.osgeo.org/wms/vmap0,basic";

// doesn't support EPSG:900913
var map_wms_topographic = "wms:Topo Maps,http://terraservice.net/ogcmap.ashx,DRG";

var map_ol_openstreetmap = "ol.openstreetmap";


var map_default_layer =  map_google_terrain;

var defaultLocation = new OpenLayers.LonLat(-0, 0);
var defaultZoomLevel = 2;
var sphericalMercatorDefault = true;

var maxExtent = new OpenLayers.Bounds(-20037508, -20037508, 20037508, 20037508);

var earthCS = new OpenLayers.Projection("EPSG:4326");
var sphericalMercatorCS = new OpenLayers.Projection("EPSG:900913");


var maxLatValue = 85;

var initialExtent = new OpenLayers.Bounds(-180, -maxLatValue, 180, maxLatValue);

var positionMarkerID = "location";

var latlonReadoutID = "ramadda-map-latlonreadout";

//Global list of all maps on this page
var ramaddaMaps = new Array();

var wrapDatelineDefault = true;
var zoomLevelsDefault = 30;


function ramaddaAddMap(map) {
    ramaddaMaps.push(map);
}

function RepositoryMap(mapId, params) {
    ramaddaAddMap(this);
    var theMap = this;
    if (mapId == null) {
        mapId = "map";
    }

    $.extend(this, {
            sourceProjection:sphericalMercatorCS,
                //sourceProjection: earthCS,
                displayProjection: earthCS,
                mapId: mapId,
                mapDivId: mapId,
                showScaleLine : true,
                showLayerSwitcher : true,
                showZoomPanControl : true,
                showZoomOnlyControl : false,
                initialLocation : defaultLocation,
                initialZoom : defaultZoomLevel,
                latlonReadout : latlonReadoutID,
                map: null,
                defaultMapLayer: map_default_layer,
                haveAddedDefaultLayer: false,
                layer: null,
                markers: null,
                boxes: null,
                lines: null,
                selectorBox: null,
                selectorMarker: null,
                listeners: [],
                initialLayers: [],
                });
    $.extend(this, params);

    jQuery(document).ready(function($) {
            if(theMap.map) {
                theMap.map.updateSize();
            }
     });


    RamaddaUtil.defineMembers(this,  {
            addLayer: function(layer) {
                if(this.map!=null) {
                    this.map.addLayer(layer);
                } else {
                    this.initialLayers.push(layer);
                }
            }
        });

    this.addImageLayer = function(name, url, north,west,south,east, width,height) {
        //Things go blooeey with lat up to 90
        if(north>88) north = 88;
        if(south<-88) south = -88;
        var imageBounds = new OpenLayers.Bounds(west, south,east,north);
        imageBounds = this.transformLLBounds(imageBounds);
        var imageLayer = new OpenLayers.Layer.Image(
                                                    name,url,
                                                    imageBounds,
                                                    new OpenLayers.Size(width, height),
                                                    {numZoomLevels: 3, 
                                                            isBaseLayer: false,
                                                            resolutions:this.map.layers[0].resolutions,
                                                            maxResolution:this.map.layers[0].resolutions[0]}
                                                    );
        
        //        imageLayer.isBaseLayer = false;
        this.map.addLayer(imageLayer);
    }


    this.addWMSLayer = function(name, url, layer, isBaseLayer) {
        var layer = new OpenLayers.Layer.WMS(name, url, {
                layers : layer,
                format: "image/png"
        }, {
            wrapDateLine : wrapDatelineDefault
        });
        if(isBaseLayer) 
            layer.isBaseLayer = true;
        else
            layer.isBaseLayer = false;
        layer.visibility = false;
        layer.reproject = true;
        this.map.addLayer(layer);
    }



    this.addMapLayer = function(name, url, layer, isBaseLayer, isDefault) {
        var layer;
        if (/\/tile\//.exec(url)) {
            layer = new OpenLayers.Layer.XYZ(
                        name, url, {
                            sphericalMercator : sphericalMercatorDefault,
                            numZoomLevels : zoomLevelsDefault,
                            wrapDateLine : wrapDatelineDefault
                        });
        } else {
            layer = new OpenLayers.Layer.WMS(name, url, {
                    layers : layer,
                    format: "image/png"
                }, {
                    wrapDateLine : wrapDatelineDefault
                });
        }
        if(isBaseLayer) 
            layer.isBaseLayer = true;
        else
            layer.isBaseLayer = false;
        layer.visibility = false;
        layer.reproject = true;
        this.map.addLayer(layer);
        if(isDefault) {
            this.haveAddedDefaultLayer = true;
            this.map.setLayerIndex(layer, 0);
            this.map.setBaseLayer(layer);
        }
    }


    this.addKMLLayer = function(name, kmlUrl) {
        var layer = new OpenLayers.Layer.Vector(name, {
                strategies: [new OpenLayers.Strategy.Fixed()],
                protocol: new OpenLayers.Protocol.HTTP({
                        url: kmlUrl,
                        format: new OpenLayers.Format.KML({
                                extractStyles: true, 
                                extractAttributes: true,
                                maxDepth: 2
                            })
                    })});
        this.map.addLayer(layer);
    }

    this.addBaseLayers = function() {
        if (!this.mapLayers) {
            this.mapLayers = [ 
                              map_google_terrain,
                              map_google_streets, 
                              map_google_satellite,
                              map_google_hybrid,
                              map_wms_openlayers,
            ];
        }

        if(!this.haveAddedDefaultLayer && this.defaultMapLayer) {
            var index = this.mapLayers.indexOf(this.defaultMapLayer);
            if(index >= 0) { 
                this.mapLayers.splice(index, 1);
                this.mapLayers.splice(0, 0,this.defaultMapLayer);
            }
        }



        for (i = 0; i < this.mapLayers.length; i++) {
            mapLayer = this.mapLayers[i];
            if(mapLayer == null) {
                continue;
            }
            if (mapLayer == map_google_hybrid) {
            	this.map.addLayer(new OpenLayers.Layer.Google("Google Hybrid",
                        {
                            'type' : google.maps.MapTypeId.HYBRID,
                            numZoomLevels : zoomLevelsDefault,
                            sphericalMercator : sphericalMercatorDefault,
                            wrapDateLine : wrapDatelineDefault
                        }));
            } else if (mapLayer == map_google_streets) {
                this.map.addLayer(new OpenLayers.Layer.Google("Google Streets",
                        {
                            numZoomLevels :zoomLevelsDefault,
                            sphericalMercator : sphericalMercatorDefault,
                            wrapDateLine : wrapDatelineDefault
                        }));
            } else if (mapLayer == map_google_terrain) {
                this.map.addLayer(new OpenLayers.Layer.Google("Google Terrain",
                        {
        	 		numZoomLevels : zoomLevelsDefault,
                            'type' : google.maps.MapTypeId.TERRAIN,
                            sphericalMercator : sphericalMercatorDefault,
                            wrapDateLine : wrapDatelineDefault
                        }));
            } else if (mapLayer == map_google_satellite) {
                this.map.addLayer(new OpenLayers.Layer.Google(
                        "Google Satellite", {
                            'type' : google.maps.MapTypeId.SATELLITE,
                            numZoomLevels : zoomLevelsDefault,
                            sphericalMercator : sphericalMercatorDefault,
                            wrapDateLine : wrapDatelineDefault
                        }));
            } else if (mapLayer == map_ms_shaded) {
                this.map.addLayer(new OpenLayers.Layer.VirtualEarth(
                        "Virtual Earth - Shaded", {
                            'type' : VEMapStyle.Shaded,
                            sphericalMercator : sphericalMercatorDefault,
                            wrapDateLine : wrapDatelineDefault,
                            numZoomLevels : zoomLevelsDefault
                        }));
            } else if (mapLayer == map_ms_hybrid) {
                this.map.addLayer(new OpenLayers.Layer.VirtualEarth(
                        "Virtual Earth - Hybrid", {
                            'type' : VEMapStyle.Hybrid,
                            sphericalMercator : sphericalMercatorDefault,
                            numZoomLevels : zoomLevelsDefault,
                            wrapDateLine : wrapDatelineDefault
                        }));
            } else if (mapLayer == map_ms_aerial) {
                this.map.addLayer(new OpenLayers.Layer.VirtualEarth(
                        "Virtual Earth - Aerial", {
                            'type' : VEMapStyle.Aerial,
                            sphericalMercator : sphericalMercatorDefault,
                            numZoomLevels : zoomLevelsDefault,
                            wrapDateLine : wrapDatelineDefault
                        }));
            /* needs OpenLayers 2.12
            } else if (mapLayer == map_ol_openstreetmap) {
                this.map.addLayer(new OpenLayers.Layer.OSM("OpenStreetMap", null, {
                      transitionEffect: "resize",
                      attribution: "&copy; <a href='http://www.openstreetmap.org/copyright'>OpenStreetMap</a> contributors",
                      sphericalMercator : sphericalMercatorDefault,
                      wrapDateLine : wrapDatelineDefault
                  }));
            */
            } else if (mapLayer == map_esri_topo) {
                var layerURL = "http://server.arcgisonline.com/ArcGIS/rest/services/World_Topo_Map/MapServer/tile/${z}/${y}/${x}";
                this.map.addLayer(new OpenLayers.Layer.XYZ(
                        "ESRI - Topo", layerURL, {
                            sphericalMercator : sphericalMercatorDefault,
                            numZoomLevels : zoomLevelsDefault,
                            wrapDateLine : wrapDatelineDefault
                        }));
            } else if (/\/tile\//.exec(mapLayer)) {
                var layerURL = mapLayer;
                this.map.addLayer(new OpenLayers.Layer.XYZ(
                        "ESRI - China Map", layerURL, {
                            sphericalMercator : sphericalMercatorDefault,
                            numZoomLevels : zoomLevelsDefault,
                            wrapDateLine : wrapDatelineDefault
                        }));




            } else {
                var match = /wms:(.*),(.*),(.*)/.exec(mapLayer);
                if (!match) {
                    alert("no match for map layer:" + mapLayer);
                    continue;
                }
                this.addWMSLayer(match[1], match[2], match[3]);
            }
        }

        this.graticule = new OpenLayers.Control.Graticule( {
            layerName : "Lat/Lon Lines",
            numPoints : 2,
            labelled : true,
            visible : false
        });
        this.map.addControl(this.graticule);

        return;
    }



    this.setLatLonReadout = function(llr) {
        this.latlonReadout = llr;
    }

    this.getMap = function() {
        return this.map;
    }
    this.initMap = function(doRegion) {
        if (this.inited)
            return;
        this.inited = true;
        this.name = "map";
        var theMap = this;
        var options = {
            
        	projection : this.sourceProjection,
            displayProjection : this.displayProjection,
            units : "m",
            controls: [],
            maxResolution : 156543.0339,
            maxExtent : maxExtent
            
        };
        

        this.map = new OpenLayers.Map(this.mapDivId,options);


        this.addBaseLayers();

        //this.vectors = new OpenLayers.Layer.Vector("Drawing");
        //this.map.addLayer(this.vectors);

        

        this.map.addControl(new OpenLayers.Control.Navigation({
                    dragPanOptions: {
                        enableKinetic: true
                            }
        }));
        
        
        /*this.map.addControl(new OpenLayers.Control.TouchNavigation({
            dragPanOptions: {
                enableKinetic: true
            }
        }));*/
        

        if (this.showZoomPanControl && !this.showZoomOnlyControl) {
            this.map.addControl(new OpenLayers.Control.PanZoom());
        }
        if (this.showZoomOnlyControl && !this.showZoomPanControl) {
            this.map.addControl(new OpenLayers.Control.Zoom());
        }

        if (this.showScaleLine) {
            this.map.addControl(new OpenLayers.Control.ScaleLine());
        }
        //        this.map.addControl(new OpenLayers.Control.OverviewMap());
        this.map.addControl(new OpenLayers.Control.KeyboardDefaults());
        if (this.showLayerSwitcher) {
            this.map.addControl(new OpenLayers.Control.LayerSwitcher());
        }

        var latLonReadout = GuiUtils.getDomObject(this.latlonReadout);
        if(latLonReadout) {
            this.map.addControl(new OpenLayers.Control.MousePosition( {
                        numDigits : 3,
                            element: latLonReadout.obj,
                            prefix: "Position: "
                    }));
        } else {
            this.map.addControl(new OpenLayers.Control.MousePosition( {
                        numDigits : 3,
                        prefix: "Position: "
                    }));
        }

        this.map.setCenter(this.transformLLPoint(this.initialLocation),
                           this.initialZoom);

        if (this.initialBoxes) {
            this.initBoxes(this.initialBoxes);
            this.initialBoxes = null;
        }

        if (this.initialBounds) {
            var llPoint = this.initialBounds.getCenterLonLat();
            var projPoint = this.transformLLPoint(llPoint);
            this.map.setCenter(projPoint);
            this.map.zoomToExtent(this.transformLLBounds(this.initialBounds));
            // this.map.restrictedExtent = this.initialBounds;
            this.initialBounds = null;
        }  else { 
            this.map.zoomToMaxExtent(); 
        }

        if (this.initialCircles) {
            this.map.addLayer(this.initialCircles);
            this.initialCircles = null;
        }

        if (this.markers) {
            this.map.addLayer(this.markers);
            var sf = new OpenLayers.Control.SelectFeature(this.markers);
            this.map.addControl(sf);
            sf.activate();
        }

        for(var i=0;i<this.initialLayers.length;i++) {
            this.map.addLayer(this.initialLayers[i]);
        }
        this.initialLayers = [];

        if (doRegion) {
            this.addRegionSelectorControl();
        }
    }

    this.initForDrawing = function() {
        var theMap = this;
        if (!theMap.drawingLayer) {
            theMap.drawingLayer = new OpenLayers.Layer.Vector("Drawing");
            theMap.map.addLayer(theMap.drawingLayer);
        }
        theMap.drawControl = new OpenLayers.Control.DrawFeature(
                theMap.drawingLayer, OpenLayers.Handler.Point);
        // theMap.drawControl.activate();
        theMap.map.addControl(theMap.drawControl);
    }

    this.drawingFeatureAdded = function(feature) {
        // alert(feature);
    }


    this.addClickHandler = function(lonfld, latfld, zoomfld, object) {

        this.lonFldId = lonfld;
        this.latFldId = latfld;

        if (this.clickHandler)
            return;
        if (!this.map)
            return;
        this.clickHandler = new OpenLayers.Control.Click();
        this.clickHandler.setLatLonZoomFld(lonfld, latfld, zoomfld, object);
        this.clickHandler.setTheMap(this);
        this.map.addControl(this.clickHandler);
        this.clickHandler.activate();
    }

    this.setSelection = function(argBase, doRegion, absolute) {
    	this.selectRegion = doRegion;
        this.argBase = argBase;
        if (!GuiUtils) {
            return;
        }
        this.fldNorth = GuiUtils.getDomObject(this.argBase + "_north");
        if (this.fldNorth ==null)
            this.fldNorth = GuiUtils.getDomObject(this.argBase + ".north");
        if (this.fldNorth ==null)
            this.fldNorth = GuiUtils.getDomObject(this.mapId + "_north");




        this.fldSouth = GuiUtils.getDomObject(this.argBase + "_south");
        if (!this.fldSouth)
            this.fldSouth = GuiUtils.getDomObject(this.argBase + ".south");
        if (this.fldSouth ==null)
            this.fldSouth = GuiUtils.getDomObject(this.mapId + "_south");

        this.fldEast = GuiUtils.getDomObject(this.argBase + "_east");
        if (!this.fldEast)
            this.fldEast = GuiUtils.getDomObject(this.argBase + ".east");
        if (this.fldEast ==null)
            this.fldEast = GuiUtils.getDomObject(this.mapId + "_east");

        this.fldWest = GuiUtils.getDomObject(this.argBase + "_west");
        if (!this.fldWest)
            this.fldWest = GuiUtils.getDomObject(this.argBase + ".west");

        if (this.fldWest ==null)
            this.fldWest = GuiUtils.getDomObject(this.mapId + "_west");

        this.fldLat = GuiUtils.getDomObject(this.argBase + "_latitude");
        if (!this.fldLat)
            this.fldLat = GuiUtils.getDomObject(this.argBase + ".latitude");

        if (this.fldLat ==null)
            this.fldLat = GuiUtils.getDomObject(this.mapId + "_latitude");


        this.fldLon = GuiUtils.getDomObject(this.argBase + "_longitude");
        if (!this.fldLon)
            this.fldLon = GuiUtils.getDomObject(this.argBase + ".longitude");
        if (this.fldLon ==null)
            this.fldLon = GuiUtils.getDomObject(this.mapId + "_longitude");


        if (this.fldLon) {
            this.addClickHandler(this.fldLon.id, this.fldLat.id);
            this.setSelectionMarker(this.fldLon.obj.value, this.fldLat.obj.value);
        }
    }

    this.selectionPopupInit = function() {
        if (!this.inited) {
            this.initMap(this.selectRegion);
            if (this.argBase && !this.fldNorth) {
                this.setSelection(this.argBase);
            }

            if (this.fldNorth) {
                // alert("north = " + this.fldNorth.obj.value);
                this.setSelectionBox(this.fldNorth.obj.value,
                        this.fldWest.obj.value, this.fldSouth.obj.value,
                        this.fldEast.obj.value);
            }
            
            if (this.fldLon) {
                this.addClickHandler(this.fldLon.id, this.fldLat.id);
                this.setSelectionMarker(this.fldLon.obj.value, this.fldLat.obj.value);
            }
        }
    }

    this.setSelectionBoxFromFields = function(zoom) {
        if (this.fldNorth) {
            // alert("north = " + this.fldNorth.obj.value);
            this.setSelectionBox(this.fldNorth.obj.value,
                    this.fldWest.obj.value, this.fldSouth.obj.value,
                    this.fldEast.obj.value);
            if (this.selectorBox) {
                var boxBounds = this.selectorBox.bounds
                this.map.setCenter(boxBounds.getCenterLonLat());
                if (zoom) {
                    this.map.zoomToExtent(boxBounds);
                }
            }
        }
    }

    this.toggleSelectorBox = function(toggle) {
       if (this.selectorControl) {
          if (toggle) {
             this.selectorControl.activate();
             this.selectorControl.box.activate();
          } else {
             this.selectorControl.deactivate();
             this.selectorControl.box.deactivate();
          }
       }
    }

    this.resetExtent = function() {
       this.map.zoomToMaxExtent();
    }

    // Assume that north, south, east, and west are in degrees or
    // some variant thereof
    this.setSelectionBox = function(north, west, south, east) {
        if (north == "" || west == "" || south == "" || east == "")
            return;
        if (!this.selectorBox) {
            var args = {
                "color" : "red",
                "selectable" : false
            };
            this.selectorBox = this.addBox("", north, west, south, east, args);
        } else {
            var bounds = new OpenLayers.Bounds(west, Math.max(south,
                    -maxLatValue), east, Math.min(north, maxLatValue));
            this.selectorBox.bounds = this.transformLLBounds(bounds);
            // this.selectorBox.bounds = bounds;
        }
        this.boxes.redraw();
    }

    this.clearSelectionMarker = function() {
        if(this.selectorMarker!=null) {
            this.removeMarker(this.selectorMarker);
            this.selectorMarker = null;
        }
    }

    this.setSelectionMarker = function(lon, lat, andCenter, zoom) {
        if (!lon || !lat || lon == "" || lat == "")
            return;
        if(this.lonFldId!=null) {
            $("#" +this.lonFldId).val(formatLocationValue(lon));
            $("#" +this.latFldId).val(formatLocationValue(lat));
        }

        var lonlat = new OpenLayers.LonLat(lon,lat);
        if (this.selectorMarker == null) {
            this.selectorMarker = this.addMarker(positionMarkerID, lonlat, "", "", 20,10);
        } else {
            this.selectorMarker.lonlat = this.transformLLPoint(lonlat);
        }
        this.markers.redraw();
        if(andCenter) {
            this.map.setCenter(this.selectorMarker.lonlat);
        }
        if(zoom) {
            if(zoom.zoomOut) {
                var level = this.map.getZoom();
                level--;
                if(this.map.isValidZoomLevel(level)) {
                    this.map.zoomTo(level);
                }
                return;
            }
            if(zoom.zoomIn) {
                var level = this.map.getZoom();
                level++;
                if(this.map.isValidZoomLevel(level)) {
                    this.map.zoomTo(level);
                }
                return;
            }

            var offset = zoom.offset;
            if(offset) {
                var bounds = this.transformLLBounds(new OpenLayers.Bounds(lon-offset, lat-offset,lon+offset,lat+offset));
                this.map.zoomToExtent(bounds);
            }
            
        }
    }
    
    this.transformLLBounds = function(bounds) {
        if (!bounds)
            return;
        var llbounds = bounds.clone();
        return llbounds.transform(this.displayProjection, this.sourceProjection);
    }

    this.transformLLPoint = function(point) {
        if (!point)
            return;
        var llpoint = point.clone();
        return llpoint.transform(this.displayProjection, this.sourceProjection);
    }

    this.transformProjBounds = function(bounds) {
        if (!bounds)
            return;
        var projbounds = bounds.clone();
        return projbounds.transform(this.sourceProjection, this.displayProjection);
    }

    this.transformProjPoint = function(point) {
        if (!point)
            return;
        var projpoint = point.clone();
        return projpoint.transform(this.sourceProjection, this.displayProjection);
    }

    this.normalizeBounds = function(bounds) {
        if (!this.map) {
            return bounds;
        }
        var newBounds = bounds;
        var newLeft = bounds.left;
        var newRight = bounds.right;
        var extentBounds = this.map.restrictedExtent;
        if (!extentBounds) {
            extentBounds = maxExtent;
        }
        /*
         * if (extentBounds.left < 0) { // map is -180 to 180 if (bounds.right >
         * 180) { //bounds is 0 to 360 newLeft = bounds.left-360; newRight =
         * bounds.right-360; } } else { // map is 0 to 360
         */
        if (extentBounds.left >= 0) { // map is 0 to 360+
            if (bounds.left < 0) { // left edge is -180 to 180
                newLeft = bounds.left + 360;
            }
            if (bounds.right < 0) { // right edge is -180 to 180
                newRight = bounds.right + 360;
            }
        }
        // just account for crossing the dateline
        if (newLeft > newRight) {
            newRight = bounds.right + 360;
        }
        newLeft = Math.max(newLeft, extentBounds.left);
        newRight = Math.min(newRight, extentBounds.right);
        newBounds = new OpenLayers.Bounds(newLeft, bounds.bottom, newRight,
                bounds.top);
        return newBounds;
    }

    this.findSelectionFields = function() {
        if (this.argBase && !(this.fldNorth || this.fldLon)) {
            this.setSelection(this.argBase);
        }
    }

    this.selectionClear = function() {
        this.findSelectionFields();
        if (this.fldNorth) {
            this.fldNorth.obj.value = "";
            this.fldSouth.obj.value = "";
            this.fldWest.obj.value = "";
            this.fldEast.obj.value = "";
        } else if (this.fldLat) {
            this.fldLon.obj.value = "";
            this.fldLat.obj.value = "";
        }
        if (this.selectorBox && this.boxes) {
            this.boxes.removeMarker(this.selectorBox);
            this.selectorBox = null;
        }
        if (this.selectorMarker && this.markers) {
            this.markers.removeMarker(this.selectorMarker);
            this.selectorMarker = null;
        }
    }

    this.addRegionSelectorControl = function() {
        var theMap = this;
        if (theMap.selectorControl)
            return;
        theMap.selectorControl = new OpenLayers.Control();
        OpenLayers.Util.extend(theMap.selectorControl, {
            draw : function() {
                // this Handler.Box will intercept the shift-mousedown
                // before Control.MouseDefault gets to see it
                this.box = new OpenLayers.Handler.Box(theMap.selectorControl, 
                    { "done" : this.notice }, 
                    { keyMask : OpenLayers.Handler.MOD_SHIFT });
                this.box.activate();
            },

            notice : function(bounds) {
                var ll = this.map.getLonLatFromPixel(new OpenLayers.Pixel(
                        bounds.left, bounds.bottom));
                var ur = this.map.getLonLatFromPixel(new OpenLayers.Pixel(
                        bounds.right, bounds.top));
                ll = theMap.transformProjPoint(ll);
                ur = theMap.transformProjPoint(ur);
                var bounds = new OpenLayers.Bounds(ll.lon, ll.lat, ur.lon,
                        ur.lat);
                bounds = theMap.normalizeBounds(bounds);
                theMap.setSelectionBox(bounds.top, bounds.left, bounds.bottom, bounds.right);
                theMap.findSelectionFields();
                if (theMap.fldNorth) {
                    // theMap.fldNorth.obj.value = ur.lat;
                    // theMap.fldSouth.obj.value = ll.lat;
                    // theMap.fldWest.obj.value = ll.lon;
                    // theMap.fldEast.obj.value = ur.lon;
                    theMap.fldNorth.obj.value = formatLocationValue(bounds.top);
                    theMap.fldSouth.obj.value = formatLocationValue(bounds.bottom);
                    theMap.fldWest.obj.value = formatLocationValue(bounds.left);
                    theMap.fldEast.obj.value = formatLocationValue(bounds.right);
                }
                // OpenLayers.Event.stop(evt);
            }
        });
        theMap.map.addControl(theMap.selectorControl);
    }

    this.onPopupClose = function(evt) {
        if (this.currentPopup) {
            this.map.removePopup(this.currentPopup);
            this.currentPopup.destroy();
            this.currentPopup = null;
            this.hiliteBox('');
        }
    }

    this.findObject = function(id, array) {
        for (i = 0; i < array.length; i++) {
            if (array[i].id == id) {
                return array[i];
            }
        }
        return null;
    }

    this.findMarker = function(id) {
        if (!this.markers) {
            return null;
        }
        return this.findObject(id, this.markers.markers);
    }

    this.findBox = function(id) {
        if (!this.boxes) {
            return null;
        }
        return this.findObject(id, this.boxes.markers);
    }

    this.hiliteBox = function(id) {
        if (this.currentBox) {
            this.currentBox.setBorder("blue");
        }
        this.currentBox = this.findBox(id);
        if (this.currentBox) {
            this.currentBox.setBorder("red");
        }
    }



    this.hiliteMarker = function(id) {
        var mymarker = this.findMarker(id);
        if (!mymarker) {
            return;
        }
        this.map.setCenter(mymarker.lonlat);
        this.showMarkerPopup(mymarker);
    }

    // bounds are in lat/lon
    this.centerOnMarkers = function(bounds) {
        // bounds = this.boxes.getDataExtent();
        if (!bounds) {
            if (!this.markers) {
                return;
            }
            // markers are in projection coordinates
            var dataBounds = this.markers.getDataExtent();
            bounds = this.transformProjBounds(dataBounds);
        }
        if (!this.map) {
            this.initialBounds = bounds;
            return;
        }
        if(bounds.getHeight()>160) {
            bounds.top = 80;
            bounds.bottom=-80;
        }
        projBounds = this.transformLLBounds(bounds);
        this.map.setCenter(projBounds.getCenterLonLat());

        if(projBounds.getWidth() ==0) {
            this.map.zoomTo(8);
        } else {
            this.map.zoomToExtent(projBounds);
        }
    }

    this.setCenter = function(latLonPoint) {
        var projPoint =  this.transformLLPoint(latLonPoint);
        this.map.setCenter(projPoint);
    }


    this.zoomToMarkers = function() {
        if (!this.markers)
            return;
        bounds = this.markers.getDataExtent();
        if(bounds == null) return;
        this.map.setCenter(bounds.getCenterLonLat());
        this.map.zoomToExtent(bounds);
    }

    this.centerToMarkers = function() {
        if (!this.markers)
            return;
        bounds = this.markers.getDataExtent();
        this.map.setCenter(bounds.getCenterLonLat());
    }

    this.setInitialCenterAndZoom = function(lon, lat, zoomLevel) {
        this.initialLocation = new OpenLayers.LonLat(lon, lat);
        this.initialZoom = zoomLevel;
    }



    this.addMarker = function(id, location, iconUrl, text, size, voffset) {
        if(size == null) size = 18;
        if(voffset ==null) voffset = 0;

        if (!this.markers) {
            this.markers = new OpenLayers.Layer.Markers("Markers");
            // Added this because I was getting an unknown method error
            this.markers.getFeatureFromEvent = function(evt) {
                return null;
            };

            if (this.map) {
                this.map.addLayer(this.markers);
                var sf = new OpenLayers.Control.SelectFeature(this.markers);
                this.map.addControl(sf);
                sf.activate();
            }
        }
        if (!iconUrl) {
            iconUrl = ramaddaBaseUrl + '/icons/marker.png';
        }
        var sz = new OpenLayers.Size(size, size);
        var calculateOffset = function(size) {
            //            return new OpenLayers.Pixel(-(size.w / 2), -size.h);
            return new OpenLayers.Pixel(-(size.w / 2), -(size.h/2)-voffset);
        };
        var icon = new OpenLayers.Icon(iconUrl, sz, null, calculateOffset);
        projPoint = this.transformLLPoint(location);


        var marker = new OpenLayers.Marker(projPoint, icon);
        marker.id = id;
        marker.text = text;
        marker.location = location;


        var theMapD = this;
        marker.events.register('click', marker, function(evt) {
                theMap.showMarkerPopup(marker);
                if(marker.ramaddaClickHandler!=null) {
                    marker.ramaddaClickHandler.call(null, marker);
                }
                OpenLayers.Event.stop(evt);
        });

        this.markers.addMarker(marker);
        if(this.selectorMarker!=null && this.selectorMarker!=marker) {
            this.markers.removeMarker(this.selectorMarker);
            this.markers.addMarker(this.selectorMarker);
        }
        return marker;
    
    }

    this.initBoxes = function(theBoxes) {
        if (!this.map) {
            // alert('whoa, no map');
        }
        this.map.addLayer(theBoxes);
        // Added this because I was getting an unknown method error
        theBoxes.getFeatureFromEvent = function(evt) {
            return null;
        };
        var sf = new OpenLayers.Control.SelectFeature(theBoxes);
        this.map.addControl(sf);
        sf.activate();
    }

    this.addBox = function(id, north, west, south, east, params) {
        var args = {
            "color" : "blue",
            "selectable" : true,
            "zoomToExtent" : false
        };

        for(var i =0;i<params.length;i++) {
            args[i] = params[i];
        }

        if (!this.boxes) {
            this.boxes = new OpenLayers.Layer.Boxes("Boxes", {
                            wrapDateLine : wrapDatelineDefault
                        });
            if (!this.map) {
                this.initialBoxes = this.boxes;
            } else {
                this.initBoxes(this.boxes);
            }
        }

        var bounds = new OpenLayers.Bounds(west, Math.max(south, -maxLatValue),
                east, Math.min(north, maxLatValue));
        var projBounds = this.transformLLBounds(bounds);
        box = new OpenLayers.Marker.Box(projBounds);
        var theMap = this;

        if (args["selectable"]) {
            box.events.register("click", box, function(e) {
                theMap.showMarkerPopup(box);
                OpenLayers.Event.stop(evt);
            });
        }
        box.setBorder(args["color"]);
        box.id = id;
        this.boxes.addMarker(box);
        var attrs =   {
            fillColor: "red",
            fillOpacity:1.0,
            pointRadius : 5, 
            zIndex: 20,
        };

        /*
        this.addPoint(id, north, west, attrs);
        this.addPoint(id, south, west, attrs);
        this.addPoint(id, north, east, attrs);
        this.addPoint(id, south, east, attrs);
        */

        if (args["zoomToExtent"]) {
            this.centerOnMarkers(bounds);
        }
        return box;
    }


    this.addPoint = function(id, lat,lon, attrs) {

        if(this.circles == null) {
            this.circles =  new OpenLayers.Layer.Vector("Circles Layer");
            this.addLayer(this.circles);
        }

        var cstyle = OpenLayers.Util.extend( {},  OpenLayers.Feature.Vector.style['default']);
        $.extend(cstyle, {
                strokeColor : "red",
                strokeWidth : 0,
                pointRadius : 10, 
                fillColor: "green",
                fillOpacity: 0.5}
            );
        if (attrs) {
            $.extend(cstyle, attrs);
        }
        var center = new OpenLayers.Geometry.Point(lon, lat);
        center.transform(this.displayProjection, this.sourceProjection);
        var point = new OpenLayers.Feature.Vector(center, null, cstyle);
        return this.circles.addFeatures( [point]);
    }


    this.addRectangle = function(id, north, west, south, east, attrs) {
        var points = [ new OpenLayers.Geometry.Point(west, north),
                new OpenLayers.Geometry.Point(west, south),
                new OpenLayers.Geometry.Point(east, south),
                new OpenLayers.Geometry.Point(east, north),
                new OpenLayers.Geometry.Point(west, north) ];
        return this.addPolygon(id, points, attrs);
    }

    this.addLine = function(id, lat1, lon1, lat2, lon2, attrs) {
        var points = [ new OpenLayers.Geometry.Point(lon1, lat1),
                       new OpenLayers.Geometry.Point(lon2, lat2) ];
        return this.addPolygon(id, points, attrs);
    }

    this.removePolygon = function(line) {
        if (this.lines) {
            this.lines.removeAllFeatures();
            this.lines.removeFeatures([line]);
        }
    }

    var cnt = 0;

    this.addPolygon = function(id, points, attrs) {


        for(var i =0;i<points.length;i++) {
            points[i].transform(this.displayProjection, this.sourceProjection);
        }


        var base_style = OpenLayers.Util.extend( {},
                OpenLayers.Feature.Vector.style['default']);
        var style = OpenLayers.Util.extend( {}, base_style);
        style.strokeColor = "blue";
        style.strokeWidth = 3;
        if (attrs) {
            for (key in attrs) {
                style[key] = attrs[key];
            }
        }




        var pointLayer = new OpenLayers.Layer.Vector("Point Layer");


        if (!this.lines) {
            // this.lines = new OpenLayers.Layer.Vector("Lines", {style:
            // base_style});
            this.lines = new OpenLayers.Layer.PointTrack("Lines", {
                style : base_style
            });
            this.addLayer(this.lines);

            /*
             * var sf = new OpenLayers.Control.SelectFeature(this.lines,{
             * onSelect: function(o) { alert(o) } }); this.map.addControl(sf);
             * sf.activate();
             */
        }





        var lineString = new OpenLayers.Geometry.LineString(points);
        var line = new OpenLayers.Feature.Vector(lineString, null, style);
        var theMap = this;
        /*
         * line.events.register("click", line, function (e) { alert("box
         * click"); theMap.showMarkerPopup(box); OpenLayers.Event.stop(evt); });
         */
        
        this.lines.addFeatures( [ line ]);
        line.id = id;
        return line;
    }

    this.showMarkerPopup = function(marker) {
        if (this.currentPopup) {
            this.map.removePopup(this.currentPopup);
            this.currentPopup.destroy();
        }
        this.hiliteBox(marker.id);
        var theMap = this;
        var markertext = marker.text;
        // set marker text as the location
        if (!markertext || markertext == "") {
        	markerlocation = this.transformProjPoint(marker.lonlat);
        	markertext = "Lon: " + location.lat + "<br>" + "Lat: " + location.lon;
        }
        popup = new OpenLayers.Popup.FramedCloud("popup", marker.lonlat,
                null, markertext, null, true, function() {
                    theMap.onPopupClose()
                });
        marker.popup = popup;
        popup.marker = marker;
        this.map.addPopup(popup);
        this.currentPopup = popup;

    }

    this.removeMarker = function(marker) {
        if (this.markers) {
            this.markers.removeMarker(marker);
        }
    }

}

function formatLocationValue(value) {
   return number_format(value, 3, ".", "");
}

OpenLayers.Control.Click = OpenLayers.Class(OpenLayers.Control, {
    defaultHandlerOptions : {
        'single' : true,
        'double' : false,
        'pixelTolerance' : 0,
        'stopSingle' : false,
        'stopDouble' : false
    },

    initialize : function(options) {
        this.handlerOptions = OpenLayers.Util.extend( {},
                this.defaultHandlerOptions);
        OpenLayers.Control.prototype.initialize.apply(this, arguments);
        this.handler = new OpenLayers.Handler.Click(this, {
            'click' : this.trigger
        }, this.handlerOptions);
    },
    setLatLonZoomFld : function(lonFld, latFld, zoomFld, listener) {
        this.lonFldId = lonFld;
        this.latFldId = latFld;
        this.zoomFldId = zoomFld;
        this.clickListener = listener;
    },

    setTheMap : function(map) {
        this.theMap = map;
    },

    trigger : function(e) {
        var xy = this.theMap.getMap().getLonLatFromViewPortPx(e.xy);
        var lonlat = this.theMap.transformProjPoint(xy)
        if (!this.lonFldId) {
            this.lonFldId = "lonfld";
            this.latFldId = "latfld";
            this.zoomFldId = "zoomfld";
        }
        lonFld = GuiUtils.getDomObject(this.lonFldId);
        latFld = GuiUtils.getDomObject(this.latFldId);
        zoomFld = GuiUtils.getDomObject(this.zoomFldId);
        if (latFld && lonFld) {
            latFld.obj.value = formatLocationValue(lonlat.lat);
            lonFld.obj.value = formatLocationValue(lonlat.lon);
        }
        if (zoomFld) {
            zoomFld.obj.value = this.theMap.getMap().getZoom();
        }
        this.theMap.setSelectionMarker(lonlat.lon, lonlat.lat);

        if(this.clickListener!=null) {
            this.clickListener.handleClick(this, lonlat.lon,lonlat.lat);
        }


    }
    
});


var CUSTOM_MAP = "CUSTOM";

var MapUtils = {
    mapRegionSelected: function(selectId, baseId) {
        var value  = $( "#" + selectId).val();
        if(value == null) {
            console.log("Error: No map region value");
            return;
        }
        if( value == "") {
            this.toggleMapWidget(baseId, false);
            return;
        }
        var toks = value.split(",");

        if(toks.length == 1) {
            if (toks[0] != CUSTOM_MAP) {
                return;
            } else {
                this.setMapRegion(baseId, "", "", "", "", "");
                this.toggleMapWidget(baseId, true);
                return;
            }
        }
        if(toks.length != 5) {
            return;
        }
        this.toggleMapWidget(baseId, false);
        this.setMapRegion(baseId, toks[0], toks[1], toks[2], toks[3], toks[4]);
    },


    setMapRegion: function(baseId, regionid, north, west, south, east) {
        $("#"+ baseId +"_regionid").val(regionid);
        $("#"+ baseId +"_north").val(north);
        $("#"+ baseId +"_west").val(west);
        $("#"+ baseId +"_south").val(south);
        $("#"+ baseId +"_east").val(east);

    },
    
    toggleMapWidget: function(baseId, onOrOff) {
        if (onOrOff) {
            // check if the map has been initialized
            var mapVar = window[baseId];
            if (mapVar && !mapVar.inited) {
                mapVar.initMap(true);
            }
            $("#"+ baseId +"_mapToggle").show();
        } else {
            $("#"+ baseId +"_mapToggle").hide();
        }
     }

}