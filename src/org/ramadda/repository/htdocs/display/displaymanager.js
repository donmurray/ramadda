
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

var PROP_CHART_TYPE = "chartType";
var PROP_LAYOUT_TYPE = "layoutType";
var PROP_LAYOUT_COLUMNS = "layoutColumns";
var PROP_SHOW_MAP = "showMap";
var PROP_SHOW_MENU  = "showMenu";
var PROP_SHOW_TITLE  = "showTitle";
var PROP_FROMDATE = "fromDate";
var PROP_TODATE = "toDate";

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
                dataList : [],
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
            getData: function() {
                return this.dataList;
            },
           addDisplayEventListener: function(listener) {
                this.eventListeners.push(listener);
            },
            handleMapClick: function (mapDisplay, lon, lat) {
                var indexObj = [];
                var records = null;
                for(var i=0;i<this.dataList.length;i++) {
                    var pointData = this.dataList[i];
                    records = pointData.getRecords();
                    if(records!=null) break;
                }
                var indexObj = [];
                var closest =  RecordFindClosest(records, lon, lat, indexObj);
                if(closest!=null) {
                    this.handleRecordSelection(mapDisplay, pointData, indexObj.index);
                }
            },
            handleRecordSelection: function(source, pointData, index) {
                if(pointData ==null && this.dataList.length>0) {
                    pointData = this.dataList[0];
                }
                var fields =  pointData.getRecordFields();
                var records = pointData.getRecords();
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
                    if(eventListener == source) continue;
                    var eventSource  = eventListener.getEventSource();
                    if(eventSource!=null && eventSource.length>0) {
                        if(eventSource!= source.getId() && eventSource!= source.getName()) {
                            //                            console.log("skipping:" + eventSource);
                            continue;
                        }
                    }
                    if(eventListener.handleRecordSelection) {
                        eventListener.handleRecordSelection(source, index, record, values);
                    } else {
                        //                        console.log("no handle func : " + eventListener.getId());
                    }
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
                //The ids (.e.g., 'linechart' have to match up with some class function with the name 
                //as defined in the createDisplay method
                var displayNames = ["Map", "Line Chart","Bar Chart", "Table", "Text","Animation", "Filter", "Scatter Plot","Example"];
                var displayCalls = ["createDisplay('map');", "createDisplay('linechart');","createDisplay('barchart');", "createDisplay('table');","createDisplay('text');","createDisplay('animation');","createDisplay('RamaddaFilterDisplay');", "createDisplay('scatterplot');","createDisplay('example');"];
                var newMenu = "";
                for(var i=0;i<displayNames.length;i++) {
                    newMenu+= htmlUtil.tag("li",[], htmlUtil.tag("a", ["onclick", get+"." + displayCalls[i]], displayNames[i]));
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
                        html+= displaysToLayout[0].getHtml();
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
                            html+=htmlUtil.tag("td", ["width", width], htmlUtil.div([], displaysToLayout[i].getHtml()));
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
                            rows.push([]);
                        }
                        rows[row].push(display.getHtml());
                    }
                    for(var i=0;i<rows.length;i++) {
                        var cols = rows[i];
                        var width = Math.round(100/cols.length)+"%";
                        html+=htmlUtil.openTag("table", ["border","0","width", "100%", "cellpadding", "5",  "cellspacing", "0"]);
                        html+=htmlUtil.openTag("tr", ["valign","top"]);
                        for(var col=0;col<cols.length;col++) {
                            html+=htmlUtil.tag("td",["width", width], cols[col]);
                        }
                        html+= htmlUtil.closeTag("tr");
                        html+= htmlUtil.closeTag("table");
                    }
                } else if(this.layout==LAYOUT_COLUMNS) {
                    var cols = [];
                    var weights = [];
                    for(var i=0;i<displaysToLayout.length;i++) {
                        var display =displaysToLayout[i];
                        var column = display.getColumn();
                        if((""+column).length==0) column = 0;
                        while(cols.length<=column) {
                            cols.push([]);
                            weights.push(0);
                        }
                        cols[column].push(display.getHtml());
                    }
                    html+=htmlUtil.openTag("table", ["border","0","width", "100%", "cellpadding", "5",  "cellspacing", "0"]);
                    html+=htmlUtil.openTag("tr", ["valign","top"]);
                    var width = Math.round(100/cols.length)+"%";
                    for(var i=0;i<cols.length;i++) {
                        var rows = cols[i];
                        var contents = "";
                        for(var j=0;j<rows.length;j++) {
                            contents+= rows[j];
                            contents+= "<br>";
                        }
                        html+=htmlUtil.tag("td", ["width", width, "valign","top"], contents);
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
            getDefaultData: function() {
                if(this.dataList.length>0) {
                    return  this.dataList[0];
                }
                return null;
            },

            createDisplay: function(type, props) {
                if(props == null) props ={};
                if(props.data!=null) {
                    var haveItAlready = false;
                    for(var i=0;i<this.dataList.length;i++) {
                        var existingData = this.dataList[i];
                        if(existingData.equals(props.data)) {
                            props.data = existingData;
                            haveItAlready = true;
                            break;
                        }
                    }
                    if(!haveItAlready) {
                        this.dataList.push(props.data);
                    }
                }

                //Upper case the type name, e.g., linechart->Linechart
                var proc = type.substring(0,1).toUpperCase() + type.substring(1);

                //Look for global functions  Ramadda<Type>Display, <Type>Display, <Type> 
                //e.g. - RamaddaLinechartDisplay, LinechartDisplay, Linechart 
                var classname = null;
                var names = ["Ramadda" +proc + "Display",
                            proc +"Display",
                            proc];
                var func = null;
                var funcName = null;
                for(var i=0;i<names.length;i++) {
                    if(window[names[i]]!=null) {
                        funcName = names[i];
                        func = window[names[i]];
                        break;
                    }
                }
                if(func==null) {
                    console.log("Error: could not find display function:" + type);
                    alert("Error: could not find display function:" + type);
                    return;
                }
                var displayId = this.id +"_display_" + (this.cnt++);
                var myProps = {};
                $.extend(myProps, props);

                if(props.data==null && this.dataList.length>0) {
                    props.data =  this.dataList[0];
                }
                //                console.log("Calling:" + funcName);
                var display =  eval(" new " + funcName+"(this, displayId, props);");
                if(display == null) {
                    console.log("Error: could not create display using:" + funcName);
                    alert("Error: could not create display using:" + funcName);
                    return;
                }
                this.displays.push(display);
                display.setDisplayManager(this);
                this.addDisplayEventListener(display);
                display.loadInitialData();
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
            pointDataLoaded: function(source, pointData) {
                for(var i=0;i< this.eventListeners.length;i++) {
                    eventListener = this.eventListeners[i];
                    if(eventListener.handlePointDataLoaded!=null) {
                        eventListener.handlePointDataLoaded(source, pointData);
                    }
                }

            },
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
        this.createDisplay('map');
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




function RamaddaFilterDisplay(displayManager, id, properties) {
    $.extend(this, new RamaddaDisplay(displayManager, id, properties));
    addRamaddaDisplay(this);
    $.extend(this, {
            html: "<p>&nbsp;&nbsp;&nbsp;Nothing selected&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;<p>",
            initDisplay: function() {
                this.initUI();
                this.setContents(this.html);
            },
        });
}


function RamaddaSuper(object, parent) {
    $.extend(object, parent);
    return object;
}