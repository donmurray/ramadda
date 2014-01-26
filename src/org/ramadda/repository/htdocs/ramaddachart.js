/**
Copyright 2008-2014 Geode Systems LLC

This package supports charting and mapping of georeferenced time series data
It requires displaymanager.js pointdata.js
*/

//Ids of DOM components
var ID_FIELDS = "fields";
var ID_HEADER = "header";
var ID_DISPLAY_CONTENTS = "contents";
var ID_MENU_BUTTON = "menu_button";
var ID_MENU_POPUP = "menu_popup";
var ID_MENU_INNER = "menu_inner";
var ID_RELOAD = "reload";

var PROP_CHART_FILTER = "chart.filter";
var PROP_CHART_MIN = "chart.min";
var PROP_CHART_MAX = "chart.max";
var PROP_CHART_TYPE = "chart.type";
var PROP_DIVID = "divid";
var PROP_FIELDS = "fields";
var PROP_LAYOUT_FIXED = "layout.fixed";
var PROP_HEIGHT  = "height";
var PROP_WIDTH  = "width";

var DFLT_WIDTH = "600px";
var DFLT_HEIGHT = "200px";


var DISPLAY_LINECHART = "linechart";
var DISPLAY_BARCHART = "barchart";
var DISPLAY_TABLE = "table";
var DISPLAY_MAP = "map";
var DISPLAY_TEXT = "text";


function addRamaddaDisplay(display) {
    if(window.globalDisplays == null) {
        window.globalDisplays = {};
    }
    window.globalDisplays[display.id] = display;
}


function getRamaddaDisplay(id) {
    if(window.globalDisplays == null) {
        return null;
    }
    return window.globalDisplays[id];
}


function removeRamaddaDisplay(id) {
    var display =getRamaddaDisplay(id);
    if(display) {
        display.removeDisplay();
    }
}


function DisplayThing(id, properties) {
    if(properties == null) {
       properties = {};
    }
    $.extend(this, {
            id: id,
            properties:properties,
            parent: null,
            getId: function() {
            return this.id;
        },
       getDomId:function(suffix) {
                return this.getId() +"_" + suffix;
       },
       getFormValue: function(what, dflt) {
           var fromForm = $("#" + this.getDomId(what)).val();
           if(fromForm!=null) {
               if(fromForm.length>0) {
                   this.setProperty(what,fromForm);
               }
               if(fromForm == "none") {
                   this.setProperty(what,null);
               }
               return fromForm;
           }
             return this.getProperty(what,dflt);
        },

       getName: function() {
         return this.getFormValue("name",this.getId());
       },
       getEventSource: function() {
         return this.getFormValue("eventsource",this.getId());
       },
       setParent:  function (parent) {
                this.parent = parent;
       },
       removeProperty: function(key) {
                this.properties[key] = null;
       },
       setProperty: function(key, value) {
           this.properties[key] = value;
        },
       getProperty: function(key, dflt) {
            var value = this.properties[key];
            if(value != null) return value;
            if(this.parent!=null) {
                    return this.parent.getProperty(key, dflt);
                }
                return dflt;
            }
        });
}





function RamaddaDisplay(displayManager, id, propertiesArg) {
    $.extend(this, new DisplayThing(id, propertiesArg));
    $.extend(this, {
            displayManager:displayManager,
            filters: [],
            setDisplayManager: function(cm) {
                this.displayManager = cm;
                this.setParent(cm);
            },
           checkFixedLayout: function() {
                if(this.getIsLayoutFixed()) {
                    var divid = this.getProperty(PROP_DIVID);
                    if(divid!=null) {
                        var html = this.getDisplay();
                        $("#" + divid).html(html);
                    }
                }
            },
            getDisplayMenuContents: function() {
                var get = "getRamaddaDisplay('" + this.id +"')";
                var moveRight = htmlUtil.onClick(get +".moveDisplayRight();", "Right");
                var moveLeft = htmlUtil.onClick(get +".moveDisplayLeft();", "Left");
                var moveUp = htmlUtil.onClick(get +".moveDisplayUp();", "Up");
                var moveDown = htmlUtil.onClick(get +".moveDisplayDown();", "Down");
                var deleteMe = htmlUtil.onClick("removeRamaddaDisplay('" + this.id +"')", "Remove Display");
                var form = "<form><table>" +
                    "<tr><td align=right><b>Move:</b></td><td>" + moveUp + " " +moveDown+  " " +moveRight+ " " + moveLeft +"</td></tr>"  +
                    "<tr><td align=right><b>Name:</b></td><td> " + htmlUtil.input("", this.getProperty("name",""), ["size","7","id",  this.getDomId("name")]) + "</td></tr>" +
                    "<tr><td align=right><b>Source:</b></td><td>"  + 
                    htmlUtil.input("", this.getProperty("eventsource",""), ["size","7","id",  this.getDomId("eventsource")]) +
                    "</td></tr>" +
                    "<tr><td align=right><b>Width:</b></td><td> " + htmlUtil.input("", this.getProperty("width",""), ["size","7","id",  this.getDomId("width")]) + "</td></tr>" +
                    "<tr><td align=right><b>Height:</b></td><td> " + htmlUtil.input("", this.getProperty("height",""), ["size","7","id",  this.getDomId("height")]) + "</td></tr>" +
                    "<tr><td align=right><b>Row:</b></td><td> " + htmlUtil.input("", this.getProperty("row",""), ["size","7","id",  this.getDomId("row")]) + "</td></tr>" +
                    "<tr><td align=right><b>Column:</b></td><td> " + htmlUtil.input("", this.getProperty("column",""), ["size","7","id",  this.getDomId("column")]) + "</td></tr>" +
                    "<tr><td align=right></td><td> " + deleteMe+ "</td></tr>" +
                    "</table>" +
                    "</form>";
                return htmlUtil.div(["class","display-fields"], form);
            },
            deltaColumn: function(delta) {
                var column = this.getProperty("column",0);
                column += delta;
                if(column<0) column = 0;
                this.setProperty("column",column);
                this.displayManager.doLayout();
            },
            moveDisplayRight: function() {
                if(this.displayManager.layout == LAYOUT_COLUMNS) {
                    this.deltaColumn(1);
                } else {
                    this.moveDisplayUp();
                }
            },
            moveDisplayLeft: function() {
                if(this.displayManager.layout == LAYOUT_COLUMNS) {
                    this.deltaColumn(-1);
                } else {
                    this.moveDisplayDown();
                }
            },
            moveDisplayUp: function() {
                this.displayManager.moveDisplayUp(this);
            },
            moveDisplayDown: function() {
                this.displayManager.moveDisplayDown(this);
            },
            getMenuContents: function() {
                return this.getDisplayMenuContents();
             },
             initMenu: function() {
                var theDisplay = this;
                $("#"+this.getDomId(ID_MENU_BUTTON)).button({ icons: { primary:  "ui-icon-triangle-1-s"}}).click(function(event) {
                        var id =theDisplay.getDomId(ID_MENU_POPUP); 
                        //function showStickyPopup(event, srcId, popupId, alignLeft) {
                        //                        showPopup(event, theDisplay.getDomId(ID_MENU_BUTTON), id, false,null,"left bottom");
                        showStickyPopup(event, theDisplay.getDomId(ID_MENU_BUTTON), id, false);
                        $("#"+  theDisplay.getDomId(ID_MENU_INNER)).superfish({
                                animation: {height:'show'},
                                    delay: 1200
                                    });
                    });
            },
            getDisplay: function() {
                var html = "";
                html +=   htmlUtil.div(["id", this.getDomId(ID_HEADER),"class", "chart-header"]);
                var menuButton =  htmlUtil.tag("a", ["class", "chart-menu-button", "id",  this.getDomId(ID_MENU_BUTTON)]);



                var close = htmlUtil.onClick("$('#" +this.getDomId(ID_MENU_POPUP) +"').hide();","<table width=100%><tr><td class=display-menu-close align=right><img src=" + root +"/icons/close.gif></td></tr></table>");

                var menuContents = this.getMenuContents();
                menuContents  = close + menuContents;
                var menu = htmlUtil.div(["class", "ramadda-popup", "id", this.getDomId(ID_MENU_POPUP)], menuContents);

                var width = this.getWidth();
                var tableWidth = "100%";
                if(width>0) {
                    tableWidth = width+"px";
                }
                html += htmlUtil.openTag("table", ["width",tableWidth, "cellpadding","0", "cellspacing","0"]);
                html += htmlUtil.openTag("tr", ["valign", "bottom"]);
                html += htmlUtil.td([], htmlUtil.b(this.getTitle()));
                html += htmlUtil.td(["align", "right"],
                                    menuButton);
                html += htmlUtil.closeTag("tr");
                var contents = this.getDisplayContents();
                html += htmlUtil.tr(["valign", "top"], htmlUtil.td(["colspan", "2","id"],contents));
                html += htmlUtil.closeTag("table")
                html += menu;
                return html;
            },
            getDisplayContents: function() {
                return htmlUtil.div(["id", this.getDomId(ID_DISPLAY_CONTENTS)]);
            },
            removeDisplay: function() {
                this.displayManager.removeDisplay(this);
            },
            setHtml: function(html) {
                $("#" + this.id).html(html);
            },
            prepareToLayout:function() {
                this.getColumn();
                this.getWidth();
                this.getHeight();
            },
            getColumn: function() {
                return this.getFormValue("column",0);
            },
            getRow: function() {
                return this.getFormValue("row",0);
            },
            getWidth: function() {
                return this.getFormValue("width",0);
            },
            getHeight: function() {
                return this.getFormValue("height",0);
            },
            setTitle: function(title) {
                $("#" +  this.getDomId(ID_HEADER)).html(title);
            },
            getTitle: function () {
                var title = this.getProperty("title");
                if(title!=null) return title;
                title = "";
                if(this.dataCollection == null) {
                    return "";
                }
                var dataList =  this.dataCollection.getData();
                for(var collectionIdx=0;collectionIdx<dataList.length;collectionIdx++) {             
                    var pointData = dataList[collectionIdx];
                    if(collectionIdx>0) title+="/";
                    title += pointData.getName();
                }
                return title;
            },
            getIsLayoutFixed: function() {
                return this.getProperty(PROP_LAYOUT_FIXED,false);
            },
            hasData: function() {
                return this.dataCollection.hasData();
            },
            addData: function(pointData) { 
                this.dataCollection.addData(pointData);
            },
            pointDataLoaded: function(pointData) {
                this.addData(pointData);
                this.displayData();
                this.addFieldsLegend();
                this.displayManager.pointDataLoaded(pointData);
            },
            reload: function() {
                var dataList =  this.dataCollection.getData();
                this.dataCollection = new DataCollection();
                for(var collectionIdx=0;collectionIdx<dataList.length;collectionIdx++) {             
                    var pointData = dataList[collectionIdx];
                    pointData.clear();
                    this.addOrLoadData(pointData);
                }
            },
            addOrLoadData: function(pointData) {
                if(pointData.hasData()) {
                    this.addData(pointData);
                } else {
                    var jsonUrl = pointData.getUrl();
                    if(jsonUrl!=null) {
                        jsonUrl = this.displayManager.getJsonUrl(jsonUrl, this);
                        loadPointJson(jsonUrl, this, pointData);
                    }
                }
            },
           getFieldsDiv: function() {
                var height = this.getProperty(PROP_HEIGHT,"400");
                return htmlUtil.div(["id",  this.getDomId(ID_FIELDS),"class", "display-fields","style","overflow-y: auto;    max-height:" + height +"px;"]);
            },
            getStandardData : function(fields) {
                var dataList = [];
                //The first entry in the dataList is the array of names
                //The first field is the domain, e.g., time or index
                //        var fieldNames = ["domain","depth"];
                var fieldNames = ["Date"];
                for(i=0;i<fields.length;i++) { 
                    var field = fields[i];
                    var name  = field.getLabel();
                    if(field.getUnit()!=null) {
                        name += " (" + field.getUnit()+")";
                    }
                    fieldNames.push(name);
                }
                dataList.push(fieldNames);

                //These are Record objects 
                //TODO: handle multiple data sources (or not?)
                var pointData = this.dataCollection.getData()[0];
                this.hasDate = true;

                var indexField = this.indexField;
                var allFields = this.allFields;
                var records = pointData.getData();
                for(j=0;j<records.length;j++) { 
                    var record = records[j];
                    var values = [];
                    var date = record.getDate();
                    if(indexField>=0) {
                        var field = allFields[indexField];
                        var value = record.getValue(indexField);
                        if(j==0) {
                            fieldNames[0] = field.getLabel();
                        }
                        values.push(value);
                    } else {
                        if(date!=null) {
                            date = new Date(date);
                            values.push(date);
                        } else {
                            if(j==0) {
                                this.hasDate = false;
                                fieldNames[0] = "Index";
                            }
                            values.push(j);
                        }
                    }

                    //            values.push(record.getElevation());
                    var allNull  = true;
                    for(var i=0;i<fields.length;i++) { 
                        var field = fields[i];
                        var value = record.getValue(field.getIndex());
                        if(value!=null) {
                            allNull = false;
                        }
                        values.push(value);
                    }
                    if(this.filters!=null) {
                        if(!this.applyFilters(record, values)) {
                            continue;
                        }
                    }
                    //TODO: when its all null values we get some errors
                    dataList.push(values);
                }
                return dataList;
            },
            applyFilters: function(record, values) {
                for(var i=0;i<this.filters.length;i++) {
                    if(!this.filters[i].recordOk(this, record, values)) {
                        return false;
                    }
                }
                return true;
            }
        }
        );

        var filter = this.getProperty(PROP_CHART_FILTER);
        if(filter!=null) {
            //semi-colon delimited list of filter definitions
            //display.filter="filtertype:params;filtertype:params;
            //display.filter="month:0-11;
            var filters = filter.split(";");
            for(i in filters) {
                filter = filters[i];
                var toks = filter.split(":");
                var type  = toks[0];
                if(type == "month") {
                    this.filters.push(new MonthFilter(toks[1]));
                } else {
                    console.log("unknown filter:" + type);
                }
            }
        }
}


/*
Create a chart
id - the id of this chart. Has to correspond to a div tag id 
pointData - A PointData object (see below)
 */
function RamaddaMultiChart(displayManager, id, pointDataArg, properties) {
    $.extend(this, new RamaddaDisplay(displayManager, id, properties));
    addRamaddaDisplay(this);
    $.extend(this, {
            dataCollection: new DataCollection(),
            indexField: -1,
            getMenuContents: function() {
                return this.getFieldsDiv()+ this.getDisplayMenuContents();
            },
            getDisplayContents: function() {
                var extraStyle = "";
                var width = this.getWidth();
                if(width>0) {
                    extraStyle += "width:" + width +"px; ";
                }
                var height = this.getProperty("height",-1);
                if(height>0) {
                    extraStyle += " height:" + height +"px; ";
                }
                var html =  htmlUtil.div(["style", extraStyle, "id", this.getDomId(ID_DISPLAY_CONTENTS)],"");
                return html;
            },
            initDisplay:function() {
                //If we are a fixed layout then there should be a div id property
                this.checkFixedLayout();
                var theChart = this;
                var reloadId = this.getDomId(ID_RELOAD);
                $("#" + reloadId).button().click(function(event) {
                        event.preventDefault();
                        theChart.reload();
                    });

                this.initMenu();
                this.addFieldsLegend();
                this.displayData();
            },
            addFieldsLegend: function() {
                if(!this.hasData()) {
                    $("#" + this.getDomId(ID_FIELDS)).html("No data");
                    return;
                }
                if(this.getProperty(PROP_FIELDS,null)!=null) {
                    //            return;
                }
                //        this.setTitle(this.getTitle());

                var html =  htmlUtil.openTag("div", ["class", "chart-fields-inner"]);
                var checkboxClass = this.id +"_checkbox";
                var dataList =  this.dataCollection.getData();
                for(var collectionIdx=0;collectionIdx<dataList.length;collectionIdx++) {             
                    var pointData = dataList[collectionIdx];
                    var fields =pointData.getChartableFields();

                    fields = RecordFieldSort(fields);
                    html+= htmlUtil.tag("b", [],  "Fields");
                    html+= "<br>";
                    for(i=0;i<fields.length;i++) { 
                        var field = fields[i];
                        field.checkboxId  = this.getDomId("cbx_" + collectionIdx +"_" +i);
                        html += htmlUtil.tag("div", ["title", field.getId()],
                                             htmlUtil.checkbox(field.checkboxId, checkboxClass,
                                                               field ==fields[0]) +" " +field.getLabel()
                                             );
                    }
                }
                html+= htmlUtil.closeTag("div");

                $("#" + this.getDomId(ID_FIELDS)).html(html);

                var theChart = this;
                //Listen for changes to the checkboxes
                $("." + checkboxClass).click(function(event) {
                        theChart.displayData();
                    });
            },
            getSelectedFields:function() {
                var df = [];
                var dataList =  this.dataCollection.getData();

                //If we have fixed fields then clear them after the first time
                var fixedFields = this.getProperty(PROP_FIELDS);
                if(fixedFields!=null) {
                    this.removeProperty(PROP_FIELDS);
                    if(fixedFields.length==0) {
                        fixedFields = null;
                    } 
                }
                for(var collectionIdx=0;collectionIdx<dataList.length;collectionIdx++) {             
                    var pointData = dataList[collectionIdx];
                    var fields = pointData.getChartableFields();
                    if(fixedFields !=null) {
                        for(i=0;i<fields.length;i++) { 
                            var field = fields[i];
                            if(fixedFields.indexOf(field.getId())>=0) {
                                df.push(field);
                            }
                        }
                    }
                }

                if(fixedFields !=null) {
                    return df;
                }

                var firstField = null;
                for(var collectionIdx=0;collectionIdx<dataList.length;collectionIdx++) {             
                    var pointData = dataList[collectionIdx];
                    var fields = pointData.getChartableFields();
                    for(i=0;i<fields.length;i++) { 
                        var field = fields[i];
                        if(firstField==null) firstField = field;
                        var cbxId =  this.getDomId("cbx_" + collectionIdx +"_" +i);
                        if($("#" + cbxId).is(':checked')) {
                            df.push(field);
                        }
                    }
                }

                if(df.length==0 && firstField!=null) {
                    df.push(firstField);
                }

                return df;
            },
            handleRecordSelection: function(source, index, record, html) {
                if(source==this) return;
                var chartType = this.getProperty(PROP_CHART_TYPE,DISPLAY_LINECHART);
                if(this.chart!=null) {
                    this.chart.setSelection([{row:index, column:null}]);
                }
            },
            displayData: function() {
                var theChart = this;
                if(!this.hasData()) {
                    if(this.chart !=null) {
                        this.chart.clearChart();
                    }
                    return;
                }

                this.allFields =  this.dataCollection.getData()[0].getRecordFields();

                var selectedFields = this.getSelectedFields();
                if(selectedFields.length==0) {
                    $("#" + this.getDomId(ID_DISPLAY_CONTENTS)).html("No fields selected");
                    return;
                }

                var dataList = this.getStandardData(selectedFields);

                var chartType = this.getProperty(PROP_CHART_TYPE,DISPLAY_LINECHART);
                //
                //Keep all of the google chart specific code here
                //
                if(typeof google == 'undefined') {
                    $("#"+this.getDomId(ID_DISPLAY_CONTENTS)).html("No google");
                    return;
                }

                var dataTable = google.visualization.arrayToDataTable(dataList);
                var options = {
                    series: [{targetAxisIndex:0},{targetAxisIndex:1},],
                    legend: { position: 'bottom' },
                    chartArea:{left:50,top:10,height:"75%",width:"85%"}
                };

                var min = this.getProperty(PROP_CHART_MIN,"");
                if(min!="") {
                    options.vAxis = {
                        minValue:min,
                    };
                }

                if(chartType == DISPLAY_BARCHART) {
                    options.orientation =  "horizontal";
                    this.chart = new google.visualization.BarChart(document.getElementById(this.getDomId(ID_DISPLAY_CONTENTS)));
                } else  if(chartType == DISPLAY_TABLE) {
                    this.chart = new google.visualization.Table(document.getElementById(this.getDomId(ID_DISPLAY_CONTENTS)));
                } else {
                    this.chart = new google.visualization.LineChart(document.getElementById(this.getDomId(ID_DISPLAY_CONTENTS)));
                }
                if(this.chart!=null) {
                    this.chart.draw(dataTable, options);
                    google.visualization.events.addListener(this.chart, 'select', function() {
                           var index = theChart.chart.getSelection()[0].row
                                theChart.displayManager.handleRecordSelection(theChart, 
                                                                              theChart.dataCollection.getData()[0], index);
                        });
                }

            }
        });
    var testUrl = null;
    //Uncomment to test using  "/repository/test.json";
    if(pointDataArg!=null) {
        this.title = pointDataArg.getName();
        if(testUrl!=null) {
            pointDataArg = new PointData("Test",null,null,testUrl);
        }
        this.addOrLoadData(pointDataArg);
    }
}



function RamaddaTextDisplay(displayManager, id, properties) {
    $.extend(this, new RamaddaDisplay(displayManager, id, properties));
    addRamaddaDisplay(this);
    $.extend(this, {
            html: "<p>&nbsp;&nbsp;&nbsp;Nothing selected&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;<p>",
            initDisplay: function() {
                this.checkFixedLayout();
                this.initMenu();
                $("#" + this.getDomId(ID_DISPLAY_CONTENTS)).html(htmlUtil.div(["class","display-text"], this.html));
            },
            handleRecordSelection: function(source, index, record, html) {
                this.html  = html;
                $("#" + this.getDomId(ID_DISPLAY_CONTENTS)).html(htmlUtil.div(["class","display-text"], this.html));
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
                    "defaultLayer": "google.streets"
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
                //                console.log("click: " + lon  +" " + lat);
            },
           getPosition:function() {
                var lat = $("#" + this.getDomId(ID_LATFIELD)).val();
                var lon = $("#" + this.getDomId(ID_LONFIELD)).val();
                if(lat == null) return null;
                return [lat,lon];
            },
           setInitMapBounds: function(north, west, south, east) {
                if(!this.map) return;
                //                if(this.mapBoundsSet) return;
                //                this.mapBoundsSet = true;
                this.map.centerOnMarkers(new OpenLayers.Bounds(west,south,east, north));
            },
            handlePointDataLoaded: function(pointData) {
                var bounds = [NaN,NaN,NaN,NaN];
                var records = pointData.getData();
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
