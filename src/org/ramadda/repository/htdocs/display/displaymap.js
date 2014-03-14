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
            mapEntryInfos: {},
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

                html+=HtmlUtil.div([ATTR_CLASS, "display-map-map", "style",extraStyle, ATTR_ID, this.getDomId(ID_MAP)]);
                html+="<br>";
                html+= HtmlUtil.openTag(TAG_DIV,[ATTR_CLASS,"display-map-latlon"]);
                html+= HtmlUtil.openTag("form");
                html+= "Latitude: " + HtmlUtil.input(this.getDomId(ID_LATFIELD), "", ["size","7",ATTR_ID,  this.getDomId(ID_LATFIELD)]);
                html+= "  ";
                html+= "Longitude: " + HtmlUtil.input(this.getDomId(ID_LONFIELD), "", ["size","7",ATTR_ID,  this.getDomId(ID_LONFIELD)]);
                html+= HtmlUtil.closeTag("form");
                html+= HtmlUtil.closeTag(TAG_DIV);
                this.setContents(html);

                var params = {
                    "defaultMapLayer": this.getProperty("defaultMapLayer", map_default_layer)
                };

                this.map = new RepositoryMap(this.getDomId(ID_MAP), params);
                this.map.initMap(false);
                this.map.addClickHandler(this.getDomId(ID_LONFIELD), this.getDomId(ID_LATFIELD), null, this);
                var theDisplay = this;
                this.map.map.events.register("zoomend","", function() {
                        theDisplay.mapBoundsChanged();
                    });
                this.map.map.events.register("moveend","", function() {
                        theDisplay.mapBoundsChanged();
                    });

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
            addMapLayer: function(source, props) {
                var entry = props.entry;
                console.log("addMapLayer:" + entry.getName());
                if(entry["column.base_url"] == null) {
                    console.log("No base url:" + entryId);
                    return;
                }
                this.map.addWMSLayer(entry.getName(),       
                                     entry["column.base_url"],
                                     entry["column.layer_name"],
                                     false);
            },
            mapBoundsChanged: function() {
                var bounds = this.map.map.calculateBounds();
                bounds =  bounds.transform(this.map.sourceProjection, this.map.displayProjection);
                this.getDisplayManager().handleEventMapBoundsChanged(this, bounds);
            },
            addFeature: function(feature) {
                this.features.push(feature);
                feature.line = this.map.addPolygon("lines_" + feature.source.getId(), RecordUtil.clonePoints(feature.points), null);
            },
            loadInitialData: function() {
                if(this.getDisplayManager().getData().length>0) {
                    this.handleEventPointDataLoaded(this, this.getDisplayManager().getData()[0]);
                }
            },

            getContentsDiv: function() {
                return  HtmlUtil.div([ATTR_CLASS,"display-contents", ATTR_ID, this.getDomId(ID_DISPLAY_CONTENTS)],"");
            },

            handleClick: function (theMap, lon,lat) {
                this.getDisplayManager().handleEventMapClick(this, lon, lat);
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
                if(source == this.lastSource) {
                    this.map.clearSelectionMarker();
                }

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
                if(entry == null) {
                    console.log("no entry");
                    this.map.clearSelectionMarker();
                    return;
                }
                var selected = args.selected;
                if(!entry.hasLocation()) {
                    return;
                }
                if(selected) {
                    this.lastSource = source;
                    //                    console.log("set selection marker:" +entry.getLongitude()+" " +  entry.getLatitude());
                    this.map.setSelectionMarker(entry.getLongitude(), entry.getLatitude(), true, args.zoom);
                }  else if(source == this.lastSource) {
                    this.map.clearSelectionMarker();
                }
            },
            addOrRemoveEntryMarker: function(id, entry, add) {
                var mapEntryInfo  = this.mapEntryInfos[id];
                if(!add) {
                    if(mapEntryInfo!=null) {
                        mapEntryInfo.removeFromMap(this.map);
                        this.mapEntryInfos[id] = null;
                    }  
                } else {
                    if(mapEntryInfo==null) {
                        mapEntryInfo = new MapEntryInfo(entry);
                        if(entry.hasBounds()) {
                            var attrs ={};
                            mapEntryInfo.rectangle = this.map.addRectangle (id, entry.getNorth(), entry.getWest(), entry.getSouth(), entry.getEast(), attrs);
                        }

                        var latitude = entry.getLatitude();
                        var longitude = entry.getLongitude();
                        var point = new OpenLayers.LonLat(longitude, latitude);
                        mapEntryInfo.marker =  this.map.addMarker(id, point, entry.getIconUrl(),this.getEntryHtml(entry));
                        var theDisplay =this;
                        mapEntryInfo.marker.entry = entry;
                        mapEntryInfo.marker.ramaddaClickHandler = function(marker) {theDisplay.handleMapClick(marker);};
                        this.mapEntryInfos[id] = mapEntryInfo;
                        if(this.handledMarkers == null) {
                            this.map.centerToMarkers();
                            this.handledMarkers = true;
                        }
                    } 
                }
            },
            handleMapClick: function(marker) {
                if(this.selectedMarker!=null) {
                    this.getDisplayManager().handleEventEntrySelection(this, {entry:this.selectedMarker.entry, selected:false});
                }
                this.getDisplayManager().handleEventEntrySelection(this, {entry:marker.entry, selected:true});
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
                var mapEntryInfo =  this.mapEntryInfos[display];
                if(mapEntryInfo!=null) {
                    mapEntryInfo.removeFromMap(this.map);
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



function MapEntryInfo(entry) {
    RamaddaUtil.defineMembers(this,{
            entry: entry,
            marker: null,
            rectangle: null,
            removeFromMap: function(map) {
                if(this.marker != null) {
                    map.removeMarker(this.marker);
                }
                if(this.rectangle != null) {
                    map.removePolygon(this.rectangle);
                }
            }

        });
}
