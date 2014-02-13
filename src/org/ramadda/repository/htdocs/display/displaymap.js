/**
Copyright 2008-2014 Geode Systems LLC
*/


var DISPLAY_MAP = "map";

addGlobalDisplayType({type: DISPLAY_MAP, label:"Map"});

function MapFeature(source, points) {
    RamaddaUtil.defineMembers(this, {
            source: source,
                points: points});
}



function RamaddaMapDisplay(displayManager, id, properties) {
    var ID_LATFIELD  = "latfield";
    var ID_LONFIELD  = "lonfield";
    var ID_MAP = "map";
    var SUPER;
    RamaddaUtil.inherit(this, SUPER = new RamaddaDisplay(displayManager, id, DISPLAY_MAP, properties));
    addRamaddaDisplay(this);
    RamaddaUtil.defineMembers(this, {
            initBounds:displayManager.initMapBounds,
            mapBoundsSet:false,
            features: [],
            myMarkers: {},
            sourceToLine: {},
            sourceToPoints: {},
            snarf:true,
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

                html+=HtmlUtil.div(["class", "display-map-map", "style",extraStyle, "id", this.getDomId(ID_MAP)]);
                html+="<br>";
                html+= HtmlUtil.openTag("div",["class","display-map-latlon"]);
                html+= HtmlUtil.openTag("form");
                html+= "Latitude: " + HtmlUtil.input(this.getDomId(ID_LATFIELD), "", ["size","7","id",  this.getDomId(ID_LATFIELD)]);
                html+= "  ";
                html+= "Longitude: " + HtmlUtil.input(this.getDomId(ID_LONFIELD), "", ["size","7","id",  this.getDomId(ID_LONFIELD)]);
                html+= HtmlUtil.closeTag("form");
                html+= HtmlUtil.closeTag("div");
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
                var entries  = this.getDisplayManager().collectEntries();
                for(var i=0;i<entries.length;i++) {
                    var pair = entries[i];
                    this.handleEventEntriesChanged(pair.source, pair.entries);
                }

            },
            addFeature: function(feature) {
                this.features.push(feature);
                feature.line = this.map.addPolygon("lines_" + feature.source.getId(), RecordUtil.clonePoints(feature.points), null);
            },
            loadInitialData: function() {
                if(this.displayManager.getData().length>0) {
                    this.handleEventPointDataLoaded(this, this.displayManager.getData()[0]);
                }
            },

            getContentsDiv: function() {
                return  HtmlUtil.div(["class","display-contents", "id", this.getDomId(ID_DISPLAY_CONTENTS)],"");
            },

            handleClick: function (theMap, lon,lat) {
                this.displayManager.handleEventMapClick(this, lon, lat);
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

           sourceToEntries: {},
           handleEventEntriesChanged: function (source, entries) {
                var oldEntries = this.sourceToEntries[source.getId()];
                if(oldEntries!=null) {
                    for(var i=0;i<oldEntries.length;i++) {
                        var id = source.getId() +"_" + oldEntries[i].getId();
                        this.addOrRemoveEntryMarker(id, oldEntries[i],false);
                    }
                }

                this.sourceToEntries[source.getId()] = entries;
                for(var i=0;i<entries.length;i++) {
                    var id = source.getId() +"_" + entries[i].getId();
                    this.addOrRemoveEntryMarker(id, entries[i],true);
                }
                this.map.zoomToMarkers();
            },
            handleEventEntrySelection: function(source, args) {
                var entry = args.entry;
                var selected = args.selected;
                if(!entry.hasLocation()) {
                    return;
                }
                if(selected) {
                    this.map.setSelectionMarker(entry.getLongitude(), entry.getLatitude());
                }
            },
            addOrRemoveEntryMarker: function(id, entry, add) {
                var marker  = this.myMarkers[id];
                if(!add) {
                    if(marker!=null) {
                        this.map.removeMarker(marker);
                        this.myMarkers[id] = null;
                    }  
                } else {
                    if(marker==null) {
                        var latitude = entry.getLatitude();
                        var longitude = entry.getLongitude();
                        var point = new OpenLayers.LonLat(longitude, latitude);
                        marker =  this.map.addMarker(id, point, entry.getIconUrl(),this.getEntryHtml(entry));
                        var theDisplay =this;
                        marker.entry = entry;
                        marker.ramaddaClickHandler = function(marker) {theDisplay.handleMapClick(marker);};
                        this.myMarkers[id] = marker;
                        if(this.handledMarkers == null) {
                            this.map.centerToMarkers();
                            this.handledMarkers = true;
                        }
                    } 
                }
            },
            handleMapClick: function(marker) {
                if(this.selectedMarker!=null) {
                    this.getDisplayManager().handleEventEntrySelection(this, this.selectedMarker.entry, false);
                }
                this.getDisplayManager().handleEventEntrySelection(this, marker.entry, true);
                this.selectedMarker = marker;
            },
            handleEventPointDataLoaded: function(source, pointData) {
                var bounds = [NaN,NaN,NaN,NaN];
                var records = pointData.getRecords();
                var points =RecordUtil.getPoints(records, bounds);
                if(!isNaN(bounds[0])) {
                    this.initBounds  = bounds;
                    this.setInitMapBounds(bounds[0],bounds[1],bounds[2], bounds[3]);
                    if(this.map!=null && points.length>1) {
                        this.addFeature(new MapFeature(source, points));
                    }
                }
            },
            handleEventRemoveDisplay: function(source, display) {
                var marker  = this.myMarkers[display];
                if(marker!=null) {
                    this.map.removeMarker(marker);
                }
                var feature = this.findFeature(display, true);
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

            handleEventRecordSelection: function(source, args) {
                var record  = args.record;
                if(record.hasLocation()) {
                    var latitude = record.getLatitude();
                    var longitude = record.getLongitude();
                    var point = new OpenLayers.LonLat(longitude, latitude);
                    var marker  = this.myMarkers[source];
                    if(marker!=null) {
                        this.map.removeMarker(marker);
                    }
                    this.myMarkers[source] =  this.map.addMarker(source.getId(), point, null,args.html);
                }}
        });
}


