/**
Copyright 2008-2014 Geode Systems LLC
*/



/*
  This gets created by the displayManager.createDisplay('example')
 */
function RamaddaExampleDisplay(displayManager, id, properties) {
    //Create the base class
    $.extend(this, new RamaddaDisplay(displayManager, id, properties));

    //Add me to the list of global displays
    addRamaddaDisplay(this);

    //Define my methods
    $.extend(this, {
            //gets called by displaymanager after the displays are layed out
            initDisplay: function() {
                this.checkFixedLayout();
                this.initMenu();
                this.setInnerContents("<p>&nbsp;<p>&nbsp;<p>");
            },
            handleRecordSelection: function(source, index, record, html) {
                this.setInnerContents(html);
            },
            setInnerContents: function(contents) {
                contents = htmlUtil.div(["class","display-text-inner"], contents);
                $("#" + this.getDomId(ID_DISPLAY_CONTENTS)).html(htmlUtil.div(["class","display-text"], contents));
                
            }
        });
}



function RamaddaMapDisplay(displayManager, id, properties) {
    var ID_LATFIELD  = "latfield";
    var ID_LONFIELD  = "lonfield";
    var ID_MAP = "map";
    $.extend(this, new RamaddaDisplay(displayManager, id, properties));
    addRamaddaDisplay(this);
    $.extend(this, {
            initBounds:displayManager.initMapBounds,
            initPoints:displayManager.initMapPoints,
            mapBoundsSet:false,
            polygons:[],
            markers: {},
            initDisplay: function() {
                this.checkFixedLayout();
                var currentPolygons = this.polygons;
                this.polygons = [];
                var params = {
                    "defaultMapLayer": this.getProperty("defaultMapLayer", map_default_layer)
                };
                this.initMenu();
                this.map = new RepositoryMap(this.getDomId(ID_MAP), params);
                this.map.initMap(false);
                this.map.addClickHandler(this.getDomId(ID_LONFIELD), this.getDomId(ID_LATFIELD), null, this);
                if(this.initBounds!=null) {
                    var b  = this.initBounds;
                    this.setInitMapBounds(b[0],b[1],b[2],b[3]);
                }

                if(this.initPoints!=null && this.initPoints.length>1) {
                    this.polygons.push(this.initPoints);
                    this.map.addPolygon("basemap", clonePoints(this.initPoints), null);
                }
                
                if(currentPolygons!=null) {
                    for(var i=0;i<currentPolygons.length;i++)  {
                        this.polygons.push(currentPolygons[i]);
                        this.map.addPolygon("basemap", clonePoints(currentPolygons[i]), null);
                    }
                }
            },
            getDisplayContents: function() {
                var html = "";
                var extraStyle = "";
                var width = this.getWidth();
                if(width>0) {
                    extraStyle += "width:" + width +"px; ";
                }
                var height = this.getProperty("height",-1);
                if(height>0) {
                    extraStyle += " height:" + height +"px; ";
                }

                html+=htmlUtil.div(["style", "min-width:200px; min-height:200px; " + extraStyle,
                                    "class", "display-map",
                                    "id", this.getDomId(ID_MAP)]);

                html+= htmlUtil.openTag("form");
                html+= "Latitude: " + htmlUtil.input(this.getDomId(ID_LATFIELD), "", ["size","7","id",  this.getDomId(ID_LATFIELD)]);
                html+= "  ";
                html+= "Longitude: " + htmlUtil.input(this.getDomId(ID_LONFIELD), "", ["size","7","id",  this.getDomId(ID_LONFIELD)]);
                html+= htmlUtil.closeTag("form");
                return html;
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
            handlePointDataLoaded: function(pointData) {
                var bounds = [NaN,NaN,NaN,NaN];
                var records = pointData.getRecords();
                var points =RecordGetPoints(records, bounds);
                if(!isNaN(bounds[0])) {
                    this.initBounds = bounds;
                    this.initPoints = points;
                    this.displayManager.setMapState(points, bounds);
                    this.setInitMapBounds(bounds[0],bounds[1],bounds[2], bounds[3]);
                    if(this.map!=null && points.length>1) {
                        this.polygons.push(points);
                        this.map.addPolygon("basemap", clonePoints(points), null);
                    }
                }

            },
             handleDisplayDelete: function(source) {
                var marker  = this.markers[source];
                if(marker!=null) {
                    this.map.removeMarker(marker);
                }
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


function RamaddaAnimationDisplay(displayManager, id, properties) {
    var ID_START = "start";
    var ID_STOP = "stop";
    var ID_TIME = "time";
    $.extend(this, new RamaddaDisplay(displayManager, id, properties));
    addRamaddaDisplay(this);
    $.extend(this, {
            running: false,
            timestamp: 0,
            index: 0,              
            sleepTime: 500,
            iconStart: root+"/icons/display/control.png",
            iconStop: root+"/icons/display/control-stop-square.png",
            iconBack: root+"/icons/display/control-stop-180.png",
            iconForward: root+"/icons/display/control-stop.png",
            iconFaster: root+"/icons/display/plus.png",
            iconSlower: root+"/icons/display/minus.png",
            iconBegin: root+"/icons/display/control-double-180.png",
            iconEnd: root+"/icons/display/control-double.png",
            deltaIndex: function(i) {
                this.stop();
                this.setIndex(this.index+i);
            }, 
            setIndex: function(i) {
                if(i<0) i=0;
                this.index = i;
                this.applyStep();
            },
            toggle: function() {
                if(this.running) {
                    this.stop();
                } else {
                    this.start();
                }
            },
            tick: function() {
                if(!this.running) return;
                this.index++;
                this.applyStep();
                var theAnimation = this;
                setTimeout(function() {theAnimation.tick();}, this.sleepTime);
            },
           applyStep: function() {
                var data = this.displayManager.getDefaultData();
                if(data == null) return;
                var records = data.getRecords();
                if(records == null) {
                    $("#" + this.getDomId(ID_TIME)).html("no records");
                    return;
                }
                if(this.index>=records.length) {
                    this.index = records.length-1;
                }
                var record = records[this.index];
                var label = "";
                if(record.getDate()!=null) {
                    label += htmlUtil.b("Date:") + " "  + record.getDate();
                } else {
                    label += htmlUtil.b("Index:") +" " + this.index;
                }
                $("#" + this.getDomId(ID_TIME)).html(label);
                this.displayManager.handleRecordSelection(this, null, this.index);
            },
            faster: function() {
                this.sleepTime = this.sleepTime/2;
                if(this.sleepTime==0) this.sleepTime  = 100;
            },
            slower: function() {
                this.sleepTime = this.sleepTime*1.5;
            },
            start: function() {
                if(this.running) return;
                this.running = true;
                this.timestamp++;
                $("#"+this.getDomId(ID_START)).attr("src",this.iconStop);
                this.tick();
            },
            stop: function() {
                if(!this.running) return;
                this.running = false;
                this.timestamp++;
                $("#"+this.getDomId(ID_START)).attr("src",this.iconStart);
            },
            initDisplay: function() {
                this.stop();
                this.checkFixedLayout();
                this.initMenu();
                var html =  htmlUtil.div(["class","wiki-h2"],"Animation");
                var get = "getRamaddaDisplay('" + this.id +"')";

                html+=  "&nbsp;&nbsp;";
                html+=  htmlUtil.onClick(get +".setIndex(0);", htmlUtil.image(this.iconBegin,["title","beginning", "class", "display-animation-button", "xwidth","32"]));
                html +="  ";
                html+=  htmlUtil.onClick(get +".deltaIndex(-1);", htmlUtil.image(this.iconBack,["title","back 1", "class", "display-animation-button", "xwidth","32"]));
                html +="  ";
                html+=  htmlUtil.onClick(get +".toggle();", htmlUtil.image(this.iconStart,["title","play/stop", "class", "display-animation-button", "xwidth","32", "id", this.getDomId(ID_START)]));
                html +="  ";
                html+=  htmlUtil.onClick(get +".deltaIndex(1);", htmlUtil.image(this.iconForward,["title","forward 1", "class", "display-animation-button", "xwidth","32"]));
                html +="  ";
                html+=  htmlUtil.onClick(get +".faster();", htmlUtil.image(this.iconFaster,["class", "display-animation-button", "title","faster", "xwidth","32"]));
                html +="  ";
                html+=  htmlUtil.onClick(get +".slower();", htmlUtil.image(this.iconSlower,["class", "display-animation-button", "title","slower", "xwidth","32"]));
                html += "<p>";
                html+=  htmlUtil.div(["id", this.getDomId(ID_TIME)],"&nbsp;");
                $("#" + this.getDomId(ID_TITLE)).html(this.getTitle());
                $("#" + this.getDomId(ID_DISPLAY_CONTENTS)).html(htmlUtil.div(["class","display-text"], html));
            },
        });
}


function RamaddaOperandsDisplay(displayManager, id, properties) {
    var ID_SELECT = "select";
    var ID_SELECT1 = "select1";
    var ID_SELECT2 = "select2";
    var ID_NEWDISPLAY = "newdisplay";
    var ID_CHARTTYPE = "charttype";

    $.extend(this, {
            entryType: null,
                entryParent: null});
    $.extend(this, new RamaddaDisplay(displayManager, id, properties));
    addRamaddaDisplay(this);
    $.extend(this, {
            initDisplay: function() {
             this.checkFixedLayout();
                this.initMenu();
                var jsonUrl = null;
                if(this.entryType!=null) {
                    jsonUrl = root +"/search/type/" + this.entryType +"?max=50&output=json&type=" + this.entryType;
                }
                if(jsonUrl == null) {
                    this.setInnerContents("<p>No entry type given");
                    return;
                }
                this.entryList = new EntryList(jsonUrl, this);
                this.setInnerContents("<p>Loading<p>");
            },
            entryListChanged: function(entryList) {
                var html = "<form>";
                html += "<p>";
                html += htmlUtil.openTag("table",["class","formtable","cellspacing","0","cellspacing","0"]);
                var entries = this.entryList.getEntries();
                var get = "getRamaddaDisplay('" + this.id +"')";
                for(var j=1;j<=2;j++) {
                    var select= htmlUtil.openTag("select",["id", this.getDomId(ID_SELECT +j)]);
                    select += htmlUtil.tag("option",["title","","value",""],
                                         "-- Select data --");
                    for(var i=0;i<entries.length;i++) {
                        var entry = entries[i];
                        var label = entry.getIconImage() +" " + entry.getName();
                        select += htmlUtil.tag("option",["title",entry.getName(),"value",entry.getId()],
                                             entry.getName());
                        
                    }
                    select += htmlUtil.closeTag("select");
                    html += htmlUtil.formEntry("Data:",select);
                }

                var select  = htmlUtil.openTag("select",["id", this.getDomId(ID_CHARTTYPE)]);
                select += htmlUtil.tag("option",["title","","value","linechart"],
                                     "Line chart");
                select += htmlUtil.tag("option",["title","","value","barchart"],
                                     "Bar chart");
                select += htmlUtil.closeTag("select");
                html += htmlUtil.formEntry("Chart Type:",select);

                html += htmlUtil.closeTag("table");
                html += "<p>";
                html +=  htmlUtil.tag("div", ["class", "display-button", "id",  this.getDomId(ID_NEWDISPLAY)],"New Chart");
                html += "<p>";
                html += "</form>";
                this.setInnerContents(htmlUtil.div(["class","display-operands-inner"], html));
                var theDisplay = this;
                $("#"+this.getDomId(ID_NEWDISPLAY)).button().click(function(event) {
                       theDisplay.createDisplay();
                   });
            },
            createDisplay: function() {
                var entry1 = this.entryList.getEntry($("#" + this.getDomId(ID_SELECT1)).val());
                var entry2 = this.entryList.getEntry($("#" + this.getDomId(ID_SELECT2)).val());
                if(entry1 == null) {
                    alert("No data selected");
                    return;
                }
                var pointDataList = [];

                pointDataList.push(new PointData(entry1.getName(), null, null, root +"/entry/show?&output=points.product&product=points.json&numpoints=1000&entryid=" +entry1.getId()));
                if(entry2!=null) {
                    pointDataList.push(new PointData(entry2.getName(), null, null, root +"/entry/show?&output=points.product&product=points.json&numpoints=1000&entryid=" +entry2.getId()));
                }

                //Make up some functions
                var operation = "average";
                var derivedData = new  DerivedPointData(this.displayManager, "Derived Data", pointDataList,operation);
                var pointData = derivedData;
                var chartType = $("#" + this.getDomId(ID_CHARTTYPE)).val();
                displayManager.createDisplay(chartType, {
                        "layoutFixed": false,
                        "data": pointData
                   });
            },
            setInnerContents: function(contents) {
                contents = htmlUtil.div(["class","display-operands"], contents);
                $("#" + this.getDomId(ID_DISPLAY_CONTENTS)).html(contents);
            }
        });
}
