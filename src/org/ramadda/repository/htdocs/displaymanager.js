
//There are the DOM IDs for various components of the UI
var ID_ENTRIES = "entries";
var ID_DISPLAYS = "displays";
var ID_MENU_BUTTON = "menu_button";
var ID_MENU_POPUP =  "menu_popup";
var ID_MENU_INNER =  "menu_inner";

var LAYOUT_TABLE = "table";
var LAYOUT_TABS = "tabs";
var LAYOUT_COLUMNS = "columns";
var LAYOUT_ROWS = "rows";

var PROP_CHART_TYPE = "chart.type";
var PROP_LAYOUT_TYPE = "layout.type";
var PROP_LAYOUT_COLUMNS = "layout.columns";
var PROP_SHOW_MAP = "showmap";
var PROP_SHOW_MENU  = "showmenu";
var PROP_FROMDATE = "fromdate";
var PROP_TODATE = "todate";

//
//adds the display manager to the list of global display managers
//
function addDisplayManager(displayManager) {
    if(window.globalDisplayManagers == null) {
        window.globalDisplayManagers =  {'foo':'bar'};
        window.globalDisplayManager = null;
    }
    window.globalDisplayManagers[displayManager.getId()] = displayManager;
    window.globalDisplayManager = displayManager;
}


//
//This will get the currently created global displaymanager or will create a new one
//
function getOrCreateDisplayManager(id, properties, force) {
    if(!force) {
        var displayManager = getDisplayManager(id);
        if(displayManager != null) {
            return displayManager;
        }
        if(window.globalDisplayManager!=null) {
            return window.globalDisplayManager;
        }
    }
    window.globalDisplayManager =  new DisplayManager(id, properties);
    return window.globalDisplayManager;
}

//
//return the global display manager with the given id, null if not found
//
function getDisplayManager(id) {
    if(window.globalDisplayManagers==null) {
        return null;
    }
    var manager =  window.globalDisplayManagers[id];
    return manager;
}


//
//DisplayManager constructor
//
function DisplayManager(id,properties) {
    var theDisplayManager = this;
    if(properties == null) {
       properties == {};
    }
    $.extend(this, new DisplayThing(id, properties));
    $.extend(this, {
                displays : [],
                data : [],
                cnt : 0,
                eventListeners: [],
                layout:this.getProperty(PROP_LAYOUT_TYPE, LAYOUT_TABLE),
                columns:this.getProperty(PROP_LAYOUT_COLUMNS, 1),
                showmap : this.getProperty(PROP_SHOW_MAP,null),
                initMapBounds : null,
                initMapPoints : null,
                setMapState : function(points, bounds) {
                this.initMapPoints = points;
                this.initMapBounds = bounds;
            },

                addDisplayEventListener: function(listener) {
                this.eventListeners.push(listener);
            },
            handleMapClick: function (mapDisplay, lon, lat) {
                var indexObj = [];
                var records = null;
                for(var i=0;i<this.data.length;i++) {
                    var pointData = this.data[i];
                    records = pointData.getData();
                    if(records!=null) break;
                }
                var indexObj = [];
                var closest =  RecordFindClosest(records, lon, lat, indexObj);
                if(closest!=null) {
                    this.handleRecordSelection(mapDisplay, pointData, indexObj.index);
                }
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
                    var eventSource  = eventListener.getEventSource();
                    if(eventSource!=null && eventSource.length>0) {
                        if(eventSource!= source.getId() && eventSource!= source.getName()) {
                            continue;
                        }
                    }
                    eventListener.handleRecordSelection(source, index, record, values);
                }
            },
            makeMainMenu: function() {
                if(!this.getProperty(PROP_SHOW_MENU, true))  {
                    return "";
                }
                //How else do I refer to this object in the html that I add 
                var get = "getDisplayManager('" + this.getId() +"')";
                var html = "";
                var wider = htmlUtil.onClick(get +".changeChartWidth(1);","Chart width");
                var narrower = htmlUtil.onClick(get +".changeChartWidth(-1);","Chart width");
                var chartNames = ["Map", "Time Series","Bar Chart","Scatter Plot", "Table", "Text"];
                var chartCalls = ["createMapDisplay();", "createTimeseries();","createBarchart();","createScatterPlot();", "createTableDisplay();","createTextDisplay();"];
                var newMenu = "";
                for(var i=0;i<chartNames.length;i++) {
                    newMenu+= htmlUtil.tag("li",[], htmlUtil.tag("a", ["onclick", get+"." + chartCalls[i]], chartNames[i]));
                }

                var layoutMenu = 
                    htmlUtil.tag("li",[], htmlUtil.onClick(get +".setLayout('table',1);", "Table - 1 column")) +"\n" +
                    htmlUtil.tag("li",[], htmlUtil.onClick(get +".setLayout('table',2);", "Table - 2 column")) +"\n" +
                    htmlUtil.tag("li",[], htmlUtil.onClick(get +".setLayout('table',3);", "Table - 3 column")) +"\n" +
                    htmlUtil.tag("li",[], htmlUtil.onClick(get +".setLayout('rows');", "Rows")) +"\n" +
                    htmlUtil.tag("li",[], htmlUtil.onClick(get +".setLayout('columns');", "Columns")) +"\n" +
                    htmlUtil.tag("li",[], htmlUtil.onClick(get +".setLayout('tabs');", "Tabs"));


                var menu = htmlUtil.div(["class","ramadda-popup", "id", this.getDomId(ID_MENU_POPUP)], 
                                        htmlUtil.tag("ul", ["id", this.getDomId(ID_MENU_INNER),"class", "sf-menu"], 
                                                     htmlUtil.tag("li",[],"<a>New</a>" + htmlUtil.tag("ul",[], newMenu)) +
                                                     htmlUtil.tag("li",[],"<a>Layout</a>" + htmlUtil.tag("ul", [], layoutMenu))));



                html += menu;
                html += htmlUtil.tag("a", ["class", "display-menu-button", "id", this.getDomId(ID_MENU_BUTTON)]);
                html+="<br>";

                return html;
            },
           getPosition:function() {
                for(var i=0;i<this.displays.length;i++) {
                    var display  = this.displays[i];
                    if(display.getPosition) {
                        return display.getPosition();
                    }
                }
            },
            getJsonUrl:function(jsonUrl, display) {
                var hasGeoMacro = jsonUrl.match(/(\${latitude})/g);
                var fromDate  = display.getProperty(PROP_FROMDATE);
                if(fromDate!=null) {
                    jsonUrl += "&fromdate=" + fromDate;
                }
                var toDate  = display.getProperty(PROP_TODATE);
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
            getDisplaysToLayout:function() {
                var result = [];
                for(var i=0;i<this.displays.length;i++) {
                    if(this.displays[i].getIsLayoutFixed()) continue;
                    result.push(this.displays[i]);
                }
                return result;
            },
            doLayout:function() {
                var html = "";
                var colCnt=100;
                var displaysToLayout = this.getDisplaysToLayout();

                for(var i=0;i<displaysToLayout.length;i++) {
                    var display = displaysToLayout[i];
                    if(display.prepareToLayout!=null) {
                        display.prepareToLayout();
                    }
                }



                if(this.layout == LAYOUT_TABLE) {
                    if(displaysToLayout.length == 1) {
                        html+= displaysToLayout[0].getDisplay();
                    } else {
                        var width = Math.round(100/this.columns)+"%";
                        html+=htmlUtil.openTag("table", ["border","0","width", "100%", "cellpadding", "5",  "cellspacing", "0"]);
                        for(var i=0;i<displaysToLayout.length;i++) {
                            colCnt++;
                            if(colCnt>=this.columns) {
                                if(i>0) {
                                    html+= htmlUtil.closeTag("tr");
                                }
                                html+= htmlUtil.openTag("tr",["valign", "top"]);
                                colCnt=0;
                            }
                            html+=htmlUtil.tag("td", ["width", width], htmlUtil.div([], displaysToLayout[i].getDisplay()));
                        }
                        html+= htmlUtil.closeTag("tr");
                        html+= htmlUtil.closeTag("table");
                    }
                } else if(this.layout==LAYOUT_TABS) {
                    //TODO
                } else if(this.layout==LAYOUT_ROWS) {
                    var rows = [];
                    for(var i=0;i<displaysToLayout.length;i++) {
                        var display =displaysToLayout[i];
                        var row = display.getRow();
                        if((""+row).length==0) row = 0;
                        while(rows.length<=row) {
                            rows.push("");
                        }
                        rows[row] += "<td>" + display.getDisplay() +"</td>";
                    }
                    for(var i=0;i<rows.length;i++) {
                        html+=htmlUtil.openTag("table", ["border","0","width", "100%", "cellpadding", "5",  "cellspacing", "0"]);
                        html+=htmlUtil.openTag("tr", ["valign","top"]);
                        html+=rows[i];
                        html+= htmlUtil.closeTag("tr");
                        html+= htmlUtil.closeTag("table");
                    }
                } else if(this.layout==LAYOUT_COLUMNS) {
                    var cols = [];
                    for(var i=0;i<displaysToLayout.length;i++) {
                        var display =displaysToLayout[i];
                        var column = display.getColumn();
                        if((""+column).length==0) column = 0;
                        while(cols.length<=column) {
                            cols.push("");
                        }
                        cols[column] += display.getDisplay();
                    }
                    html+=htmlUtil.openTag("table", ["border","0","width", "100%", "cellpadding", "5",  "cellspacing", "0"]);
                    html+=htmlUtil.openTag("tr", ["valign","top"]);
                    for(var i=0;i<cols.length;i++) {
                        html+=htmlUtil.tag("td", ["valign","top"], cols[i]);
                    }
                    html+= htmlUtil.closeTag("tr");
                    html+= htmlUtil.closeTag("table");
                } else {
                    html+="Unknown layout:" + this.layout;
                }
                $("#" + this.getDomId(ID_DISPLAYS)).html(html);

                for(var i=0;i<this.displays.length;i++) {
                    this.displays[i].initDisplay();
                }

            },
            setLayout:function(layout, columns) {
                this.layout  = layout;
                if(columns) {
                    this.columns  = columns;
                }
                this.doLayout();
            },
            createDisplay: function(data, type, props) {
                if(type == DISPLAY_MAP) {
                    this.data.push(data);
                    return this.createMapDisplay(props);
                }
                if(type == DISPLAY_TEXT) {
                    this.data.push(data);
                    return this.createTextDisplay(props);
                }
                return this.createChart(data,type, props);
            },
            createTimeseries: function(data) {
                if(data == null) {
                    data = this.data[0];
                }
                this.createChart(data,DISPLAY_LINECHART);
            },
            createBarchart:function(data) {
                if(data == null) {
                    data = this.data[0];
                }
                this.createChart(data,DISPLAY_BARCHART);
            },
            createScatterPlot:function(data) {
                if(data == null) {
                    data = this.data[0];
                }
                this.createChart(data,'scatterplot');
            },
            createTableDisplay: function(data) {
                if(data == null) {
                    data = this.data[0];
                }
                this.createChart(data,DISPLAY_TABLE);
            },
            createChart:function(pointData, chartType, props) {
                var chartId = this.id +"_chart_" + (this.cnt++);
                var myProps = {
                    "xwidth":600,
                    "xheight":200,
                    PROP_CHART_TYPE: chartType};
                myProps[PROP_CHART_TYPE] = chartType;
                $.extend(myProps, props);
                var chart =  new RamaddaMultiChart(this, chartId, pointData, myProps);
                this.data.push(pointData);
                this.displays.push(chart);
                this.addDisplayEventListener(chart);
                this.doLayout();
            },
            createTextDisplay:function(props) {
                var displayId = this.id +"_display_" + (this.cnt++);
                var display =  new RamaddaTextDisplay(this, displayId, props);
                this.displays.push(display);
                this.addDisplayEventListener(display);
                this.doLayout();
            },
            createMapDisplay:function(props) {
                var displayId = this.id +"_display_" + (this.cnt++);
                var myProps = {};
                $.extend(myProps, props);
                var display =  new RamaddaMapDisplay(this, displayId, props);
                this.displays.push(display);
                this.addDisplayEventListener(display);
                if(this.data.length>0) {
                    var data = this.data[0];
                    display.handlePointDataLoaded(pointData);
                }
                this.doLayout();
            },
            moveDisplayUp: function(display) {
                var index = this.displays.indexOf(display);
                if(index <= 0) { 
                    return;
                }
                this.displays.splice(index, 1);
                this.displays.splice(index-1, 0,display);
                this.doLayout();
            },
            moveDisplayDown: function(display) {
                var index = this.displays.indexOf(display);
                if(index >=this.displays.length) { 
                    return;
                }
                this.displays.splice(index, 1);
                this.displays.splice(index+1, 0,display);
                this.doLayout();
           },
            removeDisplay:function(display) {
                var index = this.displays.indexOf(display);
                if(index >= 0) { 
                    this.displays.splice(index, 1);
                }   

                for(var i=0;i< this.eventListeners.length;i++) {
                    eventListener = this.eventListeners[i];
                    if(eventListener.handleDisplayDelete!=null) {
                        eventListener.handleDisplayDelete(display);
                    }
                }

                var displaymanager = this;
                setTimeout(function(){displayManager.doLayout();},1);
            },
            pointDataLoaded: function(pointData) {
                for(var i=0;i< this.eventListeners.length;i++) {
                    eventListener = this.eventListeners[i];
                    if(eventListener.handlePointDataLoaded!=null) {
                        eventListener.handlePointDataLoaded(pointData);
                    }
                }

            },
            addLineChart: function(pointDataArg, properties) {
                var chartId = this.id +"_chart_" + (this.cnt++);
                var chart  = new RamaddaMultiChart(chartId, pointDataArg, properties);
                display.setDisplayManager(this);
                this.displays.add(chart); 
           }
        });

    addDisplayManager(this);

    var html = htmlUtil.openTag("div", ["class","display-container"]);
    html += this.makeMainMenu();

    //
    //Main layout is defined here
    // Right now it is a table:
    // menu
    // | displays div |  map & settings form | 
    //


    //    html += htmlUtil.openTag("table",["cellspacing","0","cellpadding","0","width","100%","border","0"]);
    //    html += htmlUtil.openTag("tr", ["valign","top"]);

    //    html+="<td>";
    //    html+="<b>Entries</b>";
    //    html+="<div class=chart-entry-list-wrapper><div id=" + this.getDomId(ID_ENTRIES) +"  class=chart-entry-list></div></div>";
    //    html+="</td>";


    var theDiv =  htmlUtil.div(["id", this.getDomId(ID_DISPLAYS)]);
    html += theDiv;

    /*
    html+=htmlUtil.tag("td", [], theDiv);
    html+=htmlUtil.openTag("td", ["width", "300"]);
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
    */

    $("#"+ this.getId()).html(html);

    if(this.showmap) {
        this.createMapDisplay(null);
    }

    if(this.entryList) {
        //this.entryList.initDisplay(this.getDomId(ID_ENTRIES));
    }


    $("#"+this.getDomId(ID_MENU_BUTTON)).button({ icons: { primary: "ui-icon-gear", secondary: "ui-icon-triangle-1-s"}}).click(function(event) {
            var id =theDisplayManager.getDomId(ID_MENU_POPUP); 
            showPopup(event, theDisplayManager.getDomId(ID_MENU_BUTTON), id, false,null,"left bottom");
            $("#"+  theDisplayManager.getDomId(ID_MENU_INNER)).superfish({
                    animation: {height:'show'},
                        delay: 1200
                        });
        });

}


