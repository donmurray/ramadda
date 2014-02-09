/**
Copyright 2008-2014 Geode Systems LLC

This package supports charting and mapping of georeferenced time series data
It requires displaymanager.js pointdata.js
*/


var DISPLAY_MAP = "map";

addGlobalDisplayType({type: DISPLAY_MAP, label:"Map"});

function MapFeature(source, points) {
    this.source = source;
    this.points = points;
}



function RamaddaMapDisplay(displayManager, id, properties) {
    var ID_LATFIELD  = "latfield";
    var ID_LONFIELD  = "lonfield";
    var ID_MAP = "map";
    RamaddaUtil.inherit(this, new RamaddaDisplay(displayManager, id, DISPLAY_MAP, properties));
    addRamaddaDisplay(this);
    RamaddaUtil.defineMembers(this, {
            initBounds:displayManager.initMapBounds,
            mapBoundsSet:false,
            features: [],
            markers: {},
            sourceToLine: {},
            sourceToPoints: {},
            initDisplay: function() {
                this.initUI();
                var html = "";
                var extraStyle = "min-height:200px;";
                var width = this.getWidth();
                if(width>0) {
                    extraStyle += "width:" + width +"px; ";
                }
                var height = this.getProperty("height",300);
                //                var height = this.getProperty("height",-1);
                if(height>0) {
                    extraStyle += " height:" + height +"px; ";
                }

                html+=htmlUtil.div(["class", "display-map-map", "style",extraStyle, "id", this.getDomId(ID_MAP)]);
                html+="<br>";
                html+= htmlUtil.openTag("div",["class","display-map-latlon"]);
                html+= htmlUtil.openTag("form");
                html+= "Latitude: " + htmlUtil.input(this.getDomId(ID_LATFIELD), "", ["size","7","id",  this.getDomId(ID_LATFIELD)]);
                html+= "  ";
                html+= "Longitude: " + htmlUtil.input(this.getDomId(ID_LONFIELD), "", ["size","7","id",  this.getDomId(ID_LONFIELD)]);
                html+= htmlUtil.closeTag("form");
                html+= htmlUtil.closeTag("div");
                this.setContents(html);




                var params = {
                    "defaultMapLayer": this.getProperty("defaultMapLayer", map_default_layer)
                };

                this.map = new RepositoryMap(this.getDomId(ID_MAP), params);
                this.map.initMap(false);
                this.map.addClickHandler(this.getDomId(ID_LONFIELD), this.getDomId(ID_LATFIELD), null, this);
                if(this.initBounds!=null) {
                    var b  = this.initBounds;
                    this.setInitMapBounds(b[0],b[1],b[2],b[3]);
                }

                var currentFeatures = this.features;
                this.features = [];
                for(var i=0;i<currentFeatures.length;i++)  {
                    this.addFeature(currentFeatures[i]);
                }
            },
            addFeature: function(feature) {
                this.features.push(feature);
                feature.line = this.map.addPolygon("lines_" + feature.source.getId(), clonePoints(feature.points), null);
            },
            loadInitialData: function() {
                if(this.displayManager.getData().length>0) {
                    this.handlePointDataLoaded(this, this.displayManager.getData()[0]);
                }
            },

            getContentsDiv: function() {
                return  htmlUtil.div(["class","display-contents", "id", this.getDomId(ID_DISPLAY_CONTENTS)],"");
            },

            handleClick: function (theMap, lon,lat) {
                this.displayManager.handleMapClick(this, lon, lat);
            },

            getPosition:function() {
                var lat = $("#" + this.getDomId(ID_LATFIELD)).val();
                var lon = $("#" + this.getDomId(ID_LONFIELD)).val();
                if(lat == null) return null;
                return [lat,lon];
            },

           setInitMapBounds: function(north, west, south, east) {
                if(!this.map) return;
                this.map.centerOnMarkers(new OpenLayers.Bounds(west,south,east, north));
            },

            handleEntrySelection: function(source, entry, selected) {
                //                this.super.handleEntrySelection.apply(this, [source,entry,selected]);
                if(!entry.hasLocation()) {
                    return;
                }
                var id = source.getId() +"_" + entry.getId();
                var marker  = this.markers[id];
                if(!selected) {
                    if(marker!=null) {
                        this.map.removeMarker(marker);
                        this.markers[id] = null;
                    }  
                } else {
                    if(marker==null) {
                        var latitude = entry.getLatitude();
                        var longitude = entry.getLongitude();
                        var point = new OpenLayers.LonLat(longitude, latitude);
                        marker =  this.map.addMarker(id, point, entry.getIconUrl(),entry.getName());

                        var numMarkers = this.markers.length;
                        this.markers[id] = marker;
                        if(this.handledMarkers == null) {
                            this.map.centerToMarkers();
                            this.handledMarkers = true;
                        }
                    }  else {
                        //                        console.log("already have marker");
                    }
                }
            },
            handlePointDataLoaded: function(source, pointData) {
                var bounds = [NaN,NaN,NaN,NaN];
                var records = pointData.getRecords();
                var points =RecordGetPoints(records, bounds);
                if(!isNaN(bounds[0])) {
                    this.initBounds  = bounds;
                    this.setInitMapBounds(bounds[0],bounds[1],bounds[2], bounds[3]);
                    if(this.map!=null && points.length>1) {
                        this.addFeature(new MapFeature(source, points));
                    }
                }
            },
            handleDisplayDelete: function(source) {
                var marker  = this.markers[source];
                if(marker!=null) {
                    this.map.removeMarker(marker);
                }
                var feature = this.findFeature(source, true);
                if(feature!=null) {
                    if(feature.line!=null) {
                        this.map.removePolygon(feature.line);
                    }
                }
            },
            findFeature: function(source, andDelete) {
                for(var i in this.features) {
                    var feature = this.features[i];
                    if(feature.source == source) {
                        if(andDelete) {
                            this.features.splice(i,1);
                        }
                        return feature;
                    }
                }
                return null;
            },

            handleRecordSelection: function(source, index, record, html) {
                if(record.hasLocation()) {
                    var latitude = record.getLatitude();
                    var longitude = record.getLongitude();
                    var point = new OpenLayers.LonLat(longitude, latitude);
                    var marker  = this.markers[source];
                    if(marker!=null) {
                        this.map.removeMarker(marker);
                    }
                    this.markers[source] =  this.map.addMarker(source.getId(), point, null,html);
                }}
        });
}


