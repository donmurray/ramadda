
//There are the DOM IDs for various components of the UI
var ID_ENTRIES = "entries";
var ID_CHARTS = "charts";
var ID_LATFIELD  = "latfield";
var ID_LONFIELD  = "lonfield";
var ID_MAP = "map";
var ID_MENU_BUTTON = "menu_button";
var ID_MENU_POPUP =  "menu_popup";
var ID_MENU_INNER =  "menu_inner";

var LAYOUT_TABLE = "table";
var LAYOUT_TABS = "tabs";

var PROP_LAYOUT_TYPE = "layout.type";
var PROP_LAYOUT_COLUMNS = "layout.columns";
var PROP_SHOW_MAP = "show.map";
var PROP_SHOW_MENU  = "show.menu";
var PROP_SHOW_TEXT = "show.text";
var PROP_FROMDATE = "fromdate";
var PROP_TODATE = "todate";

//
//adds the chartmanager to the list of global chartmanagers
//
function addChartManager(chartManager) {
    if(window.globalChartManagers == null) {
        window.globalChartManagers =  {'foo':'bar'};
        window.globalChartManager = null;
    }
    window.globalChartManagers[chartManager.getId()] = chartManager;
    window.globalChartManager = chartManager;
}


//
//This will get the currently created global chartmanager or will create a new one
//
function getOrCreateChartManager(id, properties, force) {
    if(!force) {
        var chartManager = getChartManager(id);
        if(chartManager != null) {
            return chartManager;
        }
        if(window.globalChartManager!=null) {
            return window.globalChartManager;
        }
    }
    window.globalChartManager =  new ChartManager(id, properties);
    return window.globalChartManager;
}

//
//return the global chart manager with the given id, null if not found
//
function getChartManager(id) {
    if(window.globalChartManagers==null) {
        return null;
    }
    var manager =  window.globalChartManagers[id];
    return manager;
}


//
//ChartManager constructor
//
function ChartManager(id,properties) {
    var theChart = this;
    this.id = id;
    this.properties = properties;
    if(this.properties == null) {
        this.properties == {'':''};
    }
    this.charts = [];
    this.data = [];
    this.cnt = 0;

    //This is test for listing a set of entries
    //    var entryUrl = "/repository/search/type/type_point_noaa_carbon?type=type_point_noaa_carbon&search.type_point_noaa_carbon.site_id=MLO&datadate.mode=overlaps&output=json&max=3";
    //    this.entryList = new  EntryList(entryUrl, this);

    init_ChartManager(this);

    this.layout=this.getProperty(PROP_LAYOUT_TYPE, LAYOUT_TABLE);
    this.columns=this.getProperty(PROP_LAYOUT_COLUMNS, 1);
    this.showmap = this.getProperty(PROP_SHOW_MAP,null);
    this.showtext = this.getProperty(PROP_SHOW_TEXT,null);
    this.mapBoundsSet  = false;
    addChartManager(this);

    var html = htmlUtil.openTag("div", ["class","chart-container"]);

    html += this.makeMainMenu();

    //
    //Main layout is defined here
    // Right now it is a table:
    // menu
    // | charts div |  map & settings form | 
    //


    html += htmlUtil.openTag("table",["width","100%","border","0"]);
    html += htmlUtil.openTag("tr", ["valign","top"]);

    //    html+="<td>";
    //    html+="<b>Entries</b>";
    //    html+="<div class=chart-entry-list-wrapper><div id=" + this.getDomId(ID_ENTRIES) +"  class=chart-entry-list></div></div>";
    //    html+="</td>";


    //this is the div where the charts go
    html+=htmlUtil.tag("td", [],  htmlUtil.div(["id", this.getDomId(ID_CHARTS)]));

    html+=htmlUtil.openTag("td", ["width", "300"]);

    //Add the map
    if(this.showmap) {
        html+=htmlUtil.div(["style", "width:300px; height:300px;",
                            "class", "chart-map",
                            "id", this.getDomId(ID_MAP)]);
        html+= htmlUtil.openTag("form");
        html+= "Latitude: " + htmlUtil.input(this.getDomId(ID_LATFIELD), "", ["size","7","id",  this.getDomId(ID_LATFIELD)]);
        html+= "  ";
        html+= "Longitude: " + htmlUtil.input(this.getDomId(ID_LONFIELD), "", ["size","7","id",  this.getDomId(ID_LONFIELD)]);
        html+= htmlUtil.closeTag("form");

    }

    if(this.showtext) {
        html+= htmlUtil.div(["id", this.getDomId("text")],"");
        this.addChartEventListener(new TextListener(this.getDomId("text")));
    }

    if(this.getProperty(PROP_SHOW_MENU, true))  {
        //This is where we can put time selectors, etc
        html+= "<br>";
        html+= htmlUtil.tag("b",[],"Selection");
        html+=htmlUtil.openTag("form");
        html+=" Put selection form here";
        html+=htmlUtil.closeTag("form");
    }

    html+=htmlUtil.closeTag("td");
    html+=htmlUtil.closeTag("table");

    $("#"+ this.getId()).html(html);

    this.mapCentered = false;
    if(this.showmap) {
        var params = {};
        this.map = new RepositoryMap(this.getDomId(ID_MAP), params);
        this.addChartEventListener(new MapListener(this));
        this.map.initMap(false);
        this.map.addClickHandler(this.getDomId(ID_LONFIELD), this.getDomId(ID_LATFIELD));
        //        this.map.addLine('0e9d5f64-823a-4bdf-813b-bb3f4d80f6e2_polygon', 59.772422500000005, -151.10694375000003, 59.7614675, -151.14459375);
    }

    if(this.entryList) {
        //this.entryList.initDisplay(this.getDomId(ID_ENTRIES));
    }


    $("#"+this.getDomId(ID_MENU_BUTTON)).button({ icons: { primary: "ui-icon-gear", secondary: "ui-icon-triangle-1-s"}}).click(function(event) {

            var id =theChart.getDomId(ID_MENU_POPUP); 
            showPopup(event, theChart.getDomId(ID_MENU_BUTTON), id, false,null,"left bottom");
            $("#"+  theChart.getDomId(ID_MENU_INNER)).superfish({
                    animation: {height:'show'},
                        delay: 1200
                        });
        });
}


function init_ChartManager(chartManager) {

    init_ChartThing(chartManager);

    $.extend(chartManager, {
          eventListeners: [],
          addChartEventListener: function(listener) {
                this.eventListeners.push(listener);
            },

            handleRecordSelection: function(source, pointData, index) {
                var fields =  pointData.getRecordFields();
                var records = pointData.getData();
                if(index<0 || index>= records.length) {
                    console.log("handleRecordSelection: bad index= " + index);
                    return;
                 }
                var record = records[index];
                var values = "<table>";
                if(record.hasLocation()) {
                    var latitude = record.getLatitude();
                    var longitude = record.getLongitude();
                    values+= "<tr><td align=right><b>Latitude:</b></td><td>" + latitude + "</td></tr>";
                    values+= "<tr><td align=right><b>Longitude:</b></td><td>" + longitude + "</td></tr>";
                }
                if(record.hasElevation()) {
                    values+= "<tr><td  align=right><b>Elevation:</b></td><td>" + record.getElevation() + "</td></tr>";
                }
                for(var i=0;i<record.getData().length;i++) {
                    var label = fields[i].getLabel();
                    values+= "<tr><td align=right><b>" + label +":</b></td><td>" + record.getValue(i) + "</td></tr>";
                }
                values += "</table>";



                for(var i=0;i< this.eventListeners.length;i++) {
                    eventListener = this.eventListeners[i];
                    eventListener.handleRecordSelection(source, index, record, values);
                }
            },
            makeMainMenu: function() {
                if(!this.getProperty(PROP_SHOW_MENU, true))  {
                    return "";
                }
                //How else do I refer to this object in the html that I add 
                var get = "getChartManager('" + this.getId() +"')";
                var html = "";
                var wider = htmlUtil.onClick(get +".changeChartWidth(1);","Chart width");
                var narrower = htmlUtil.onClick(get +".changeChartWidth(-1);","Chart width");
                var chartNames = ["Time Series","Bar Chart","Scatter Plot", "Table", "Text"];
                var chartCalls = ["newTimeseries();","newBarchart();","newScatterPlot();", "newTable();","newText();"];
                var newMenu = "";
                for(var i=0;i<chartNames.length;i++) {
                    newMenu+= htmlUtil.tag("li",[], htmlUtil.tag("a", ["onclick", get+"." + chartCalls[i]], chartNames[i]));
                }


                var layoutMenu = 
                    htmlUtil.tag("li",[], htmlUtil.onClick(get +".setLayout('table',1);", "Table - 1 column")) +"\n" +
                    htmlUtil.tag("li",[], htmlUtil.onClick(get +".setLayout('table',2);", "Table - 2 column")) +"\n" +
                    htmlUtil.tag("li",[], htmlUtil.onClick(get +".setLayout('table',3);", "Table - 3 column")) +"\n" +
                    htmlUtil.tag("li",[], htmlUtil.onClick(get +".setLayout('tabs');", "Tabs"));


                var menu = htmlUtil.div(["class","ramadda-popup", "id", this.getDomId(ID_MENU_POPUP)], 
                                        htmlUtil.tag("ul", ["id", this.getDomId(ID_MENU_INNER),"class", "sf-menu"], 
                                                     htmlUtil.tag("li",[],"<a>New</a>" + htmlUtil.tag("ul",[], newMenu)) +
                                                     htmlUtil.tag("li",[],"<a>Layout</a>" + htmlUtil.tag("ul", [], layoutMenu))));

                html += menu;
                html += htmlUtil.tag("a", ["class", "chart-menu-button", "id", this.getDomId(ID_MENU_BUTTON)]);
                html+="<br>";

                return html;
            },
                hasMap: function() {
                return this.map!=null;
            },
            addPolygon:function(id, points, props) {
                if(!this.map) return;
                this.map.addPolygon(id, points, props);
            },
                setInitMapBounds: function(north, west, south, east) {
                if(!this.map) return;
                if(this.mapBoundsSet) return;
                this.mapBoundsSet = true;
                var bounds = new OpenLayers.Bounds(west,south,east, north);
                this.map.centerOnMarkers(bounds);
            },
           getPosition:function() {
                var lat = $("#" + this.getDomId(ID_LATFIELD)).val();
                var lon = $("#" + this.getDomId(ID_LONFIELD)).val();
                if(lat == null) return null;
                return [lat,lon];
            },
                getJsonUrl:function(jsonUrl, chart) {
                var hasGeoMacro = jsonUrl.match(/(\${latitude})/g);
                var fromDate  = chart.getProperty(PROP_FROMDATE);
                if(fromDate!=null) {
                    jsonUrl += "&fromdate=" + fromDate;
                }
                var toDate  = chart.getProperty(PROP_TODATE);
                if(toDate!=null) {
                    jsonUrl += "&todate=" + toDate;
                }
                if(hasGeoMacro !=null) {
                    if(this.map!=null) {
                        var tuple = this.getPosition();
                        if(tuple!=null) {
                            var lat = tuple[0];
                            var lon = tuple[1];
                            jsonUrl = jsonUrl.replace("${latitude}",lat);
                            jsonUrl = jsonUrl.replace("${longitude}",lon);
                        }
                    } 
                    jsonUrl = jsonUrl.replace("${latitude}","40.0");
                    jsonUrl = jsonUrl.replace("${longitude}","-107.0");
                }
                return jsonUrl;
            },
                entryListChanged:function(entryList) {
                entryList.setHtml(entryList.getHtml());
            },
            changeChartWidth:function(w) {
            },
            getChartsToLayout:function() {
                var result = [];
                for(var i=0;i<this.charts.length;i++) {
                    if(this.charts[i].getIsLayoutFixed()) continue;
                    result.push(this.charts[i]);
                }
                return result;
            },
                doLayout:function() {
                var html = "";
                var colCnt=100;
                var chartsToLayout = this.getChartsToLayout();
                if(this.layout == LAYOUT_TABLE) {
                    html+=htmlUtil.openTag("table", ["border","0","width", "100%", "cellpadding", "5",  "cellspacing", "5"]);
                    for(var i=0;i<chartsToLayout.length;i++) {
                        colCnt++;
                        if(colCnt>=this.columns) {
                            if(i>0) {
                                html+= htmlUtil.closeTag("tr");
                            }
                            html+= htmlUtil.openTag("tr",["valign", "top"]);
                            colCnt=0;
                        }
                        html+=htmlUtil.tag("td", [], htmlUtil.div([], chartsToLayout[i].getDisplay()));
                    }
                    html+= htmlUtil.closeTag("tr");
                    html+= htmlUtil.closeTag("table");
                } else if(this.layout==LAYOUT_TABS) {
                    //TODO
                } else {
                    html+="Unknown layout:" + this.layout;
                }
                $("#" + this.getDomId(ID_CHARTS)).html(html);

                for(var i=0;i<this.charts.length;i++) {
                    this.charts[i].initDisplay();
                }

            },
                setLayout:function(layout, columns) {
                this.layout  = layout;
                if(columns) {
                    this.columns  = columns;
                }
                this.doLayout();
            },
            newTimeseries: function(data) {
                if(data == null) {
                    data = this.data[0];
                }
                var chartManager = this;
                setTimeout(function(){chartManager.createChart(data,CHART_LINECHART);},1);
            },
             newBarchart:function(data) {
                if(data == null) {
                    data = this.data[0];
                }
                var chartManager = this;
                setTimeout(function(){chartManager.createChart(data,CHART_BARCHART);},1);
            },
            newScatterPlot:function(data) {
                if(data == null) {
                    data = this.data[0];
                }
                var chartManager = this;
                setTimeout(function(){chartManager.createChart(data,'scatterplot');},1);
            },
           newTable: function(data) {
                if(data == null) {
                    data = this.data[0];
                }
                var chartManager = this;
                setTimeout(function(){chartManager.createChart(data,CHART_TABLE);},1);
            },
           newText: function(data) {
                this.createChart(data,CHART_TEXT);
            },

            createChart:function(pointData, chartType, props) {
                var chartId = this.id +"_chart_" + (this.cnt++);
                var myProps = {
                    PROP_WIDTH:600,
                    PROP_HEIGHT:200,
                    PROP_CHART_TYPE: chartType};
                myProps[PROP_CHART_TYPE] = chartType;
                $.extend(myProps, props);
                var chart =  new RamaddaLineChart(chartId, pointData, myProps);
                chart.setChartManager(this);
                this.data.push(pointData);
                this.charts.push(chart);
                this.addChartEventListener(chart);
                this.doLayout();
            },
            removeChart:function(chart) {
                var index = this.charts.indexOf(chart);
                if(index >= 0) { 
                    this.charts.splice(index, 1);
                }   

                for(var i=0;i< this.eventListeners.length;i++) {
                    eventListener = this.eventListeners[i];
                    if(eventListener.handleChartDelete!=null) {
                        eventListener.handleChartDelete(chart);
                    }
                }

                var chartmanager = this;
                setTimeout(function(){chartManager.doLayout();},1);
            },
            pointDataLoaded: function(pointData) {
                for(var i=0;i< this.eventListeners.length;i++) {
                    eventListener = this.eventListeners[i];
                    if(eventListener.handleChartData!=null) {
                        eventListener.handleChartData(pointData);
                    }
                }

            },
            addLineChart: function(pointDataArg, properties) {
                var chartId = this.id +"_chart_" + (this.cnt++);
                var chart  = new RamaddaLineChart(chartId, pointDataArg, properties);
                chart.setChartManager(this);
                this.charts.add(chart);
            }
        });
}



function init_ChartThing(chartThing) {
    $.extend(chartThing, {
            parent: null,
        getId: function() {
            return this.id;
        },
        getDomId:function(suffix) {
                return this.getId() +"_" + suffix;
        },
       setParent:  function (parent) {
                this.parent = parent;
            },
       removeProperty: function(key) {
                this.properties[key] = null;
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


function MapListener(chartManager) {
    this.chartManager  = chartManager;
    $.extend(this, {
            markers: {'foo':'bar'},
            handleChartData: function(pointData) {
                var points =[];
                var records = pointData.getData();
                var north=NaN,west=NaN,south=NaN,east=NaN;
                for(j=0;j<records.length;j++) { 
                    var record = records[j];
                    if(!isNaN(record.getLatitude())) { 
                        if(j == 0) {
                            north  =  record.getLatitude();
                            south  = record.getLatitude();
                            west  =  record.getLongitude();
                            east  = record.getLongitude();
                        } else {
                            north  = Math.max(north, record.getLatitude());
                            south  = Math.min(south, record.getLatitude());
                            west  = Math.min(west, record.getLongitude());
                            east  = Math.min(east, record.getLongitude());
                        }
                        points.push(new OpenLayers.Geometry.Point(record.getLongitude(),record.getLatitude()));
                    }
                }
                if(!isNaN(north)) {
                    this.chartManager.setInitMapBounds(north, west, south, east);
                    if(points.length>1) {
                        this.chartManager.addPolygon("id",points, null);
                    }
                }

            },
            handleChartDelete: function(source) {
                var marker  = this.markers[source];
                if(marker!=null) {
                    this.chartManager.map.removeMarker(marker);
                }
            },
                handleRecordSelection: function(source, index, record, html) {
                if(record.hasLocation()) {
                    var latitude = record.getLatitude();
                    var longitude = record.getLongitude();
                    var point = new OpenLayers.LonLat(longitude, latitude);
                    var marker  = this.markers[source];
                    if(marker!=null) {
                        this.chartManager.map.removeMarker(marker);
                    }
                    this.markers[source] =  this.chartManager.map.addMarker(source.getId(), point, null,html);
                }}
        });
}


function TextListener(domId) {
    this.domId = domId;
    $.extend(this, {
            handleRecordSelection: function(source, index, record, html) {
                $("#"+this.domId).html(html);
            }});
}
