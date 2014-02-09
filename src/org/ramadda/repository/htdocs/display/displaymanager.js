/**
Copyright 2008-2014 Geode Systems LLC
*/

//There are the DOM IDs for various components of the UI
var ID_DISPLAYS = "displays";
var ID_MENU_BUTTON = "menu_button";
var ID_MENU_OUTER =  "menu_outer";
var ID_MENU_INNER =  "menu_inner";



//Properties
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
        window.globalDisplayManagers =  {};
        // window.globalDisplayManager = null;
    }
    window.globalDisplayManagers[displayManager.getId()] = displayManager;
    window.globalDisplayManager = displayManager;
}


function addGlobalDisplayType(type) {
    if(window.globalDisplayTypes == null) {
        window.globalDisplayTypes=  [];
    }
    window.globalDisplayTypes.push(type);
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

function DisplayGroup(id,properties) {
    var LAYOUT_TABLE = "table";
    var LAYOUT_TABS = "tabs";
    var LAYOUT_COLUMNS = "columns";
    var LAYOUT_ROWS = "rows";

    RamaddaUtil.inherit(this, new DisplayThing(id, properties));
    RamaddaUtil.defineMembers(this, {
            displays : [],
            layout:this.getProperty(PROP_LAYOUT_TYPE, LAYOUT_TABLE),
            columns:this.getProperty(PROP_LAYOUT_COLUMNS, 1),
            getPosition:function() {
                for(var i=0;i<this.displays.length;i++) {
                    var display  = this.displays[i];
                    if(display.getPosition) {
                        return display.getPosition();
                    }
                }
            },
            getDisplaysToLayout:function() {
                var result = [];
                for(var i=0;i<this.displays.length;i++) {
                    if(this.displays[i].getIsLayoutFixed()) continue;
                    result.push(this.displays[i]);
                }
                return result;
            },
            addNewDisplay: function(display) {
//                display.setDisplayManager(this);
//                this.addDisplayEventListener(display);
//                display.loadInitialData();
                this.displays.push(display);
                this.doLayout();
            },
            removeDisplay:function(display) {
                var index = this.displays.indexOf(display);
                if(index >= 0) { 
                    this.displays.splice(index, 1);
                }   
                this.doLayout();
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
                        html+=HtmlUtil.openTag("table", ["border","0","width", "100%", "cellpadding", "5",  "cellspacing", "0"]);
                        for(var i=0;i<displaysToLayout.length;i++) {
                            colCnt++;
                            if(colCnt>=this.columns) {
                                if(i>0) {
                                    html+= HtmlUtil.closeTag("tr");
                                }
                                html+= HtmlUtil.openTag("tr",["valign", "top"]);
                                colCnt=0;
                            }
                            html+=HtmlUtil.tag("td", ["width", width], HtmlUtil.div([], displaysToLayout[i].getHtml()));
                        }
                        html+= HtmlUtil.closeTag("tr");
                        html+= HtmlUtil.closeTag("table");
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
                        html+=HtmlUtil.openTag("table", ["border","0","width", "100%", "cellpadding", "5",  "cellspacing", "0"]);
                        html+=HtmlUtil.openTag("tr", ["valign","top"]);
                        for(var col=0;col<cols.length;col++) {
                            html+=HtmlUtil.tag("td",["width", width], cols[col]);
                        }
                        html+= HtmlUtil.closeTag("tr");
                        html+= HtmlUtil.closeTag("table");
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
                    html+=HtmlUtil.openTag("table", ["border","0","width", "100%", "cellpadding", "5",  "cellspacing", "0"]);
                    html+=HtmlUtil.openTag("tr", ["valign","top"]);
                    var width = Math.round(100/cols.length)+"%";
                    for(var i=0;i<cols.length;i++) {
                        var rows = cols[i];
                        var contents = "";
                        for(var j=0;j<rows.length;j++) {
                            contents+= rows[j];
                        }
                        html+=HtmlUtil.tag("td", ["width", width, "valign","top"], contents);
                    }
                    html+= HtmlUtil.closeTag("tr");
                    html+= HtmlUtil.closeTag("table");
                } else {
                    html+="Unknown layout:" + this.layout;
                }
                $("#" + this.getDomId(ID_DISPLAYS)).html(html);


                this.initDisplay();

            },
            initDisplay: function() {
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


        });
}


//
//DisplayManager constructor
//id should correspond to a DOM element id

function DisplayManager(id,properties) {
    RamaddaUtil.inherit(this, new DisplayGroup(id, properties));
    RamaddaUtil.defineMembers(this, {
                dataList : [],
                displayTypes: [],
                cnt : 0,
                eventListeners: [],
                showmap : this.getProperty(PROP_SHOW_MAP,null),
                initMapBounds : null,
                });

    if(window.globalDisplayTypes!=null) {
        for(var i=0;i<window.globalDisplayTypes.length;i++) {
            this.displayTypes.push(window.globalDisplayTypes[i]);
        }
    }

   RamaddaUtil.defineMembers(this, {
           addDisplayType: function(type,label) {
               this.displayTypes.push({type:label});
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
                if(records == null) {
                    return;
                }
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
            handleEntrySelection: function(source, entry, selected) {
                for(var i=0;i< this.eventListeners.length;i++) {
                    eventListener = this.eventListeners[i];
                    if(eventListener == source) {
                        continue;
                    }
                    var eventSource  = eventListener.getEventSource();
                    if(eventSource!=null && eventSource.length>0) {
                        if(eventSource!= source.getId() && eventSource!= source.getName()) {
                            continue;
                        }
                    }
                    if(eventListener.handleEntrySelection) {
                        eventListener.handleEntrySelection(source, entry, selected);
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
                var newMenu = "";
                for(var i=0;i<this.displayTypes.length;i++) {
                    //The ids (.e.g., 'linechart' have to match up with some class function with the name 
                    var type = this.displayTypes[i];
                    newMenu+= HtmlUtil.tag("li",[], HtmlUtil.tag("a", ["onclick", get+".createDisplay('" + type.type+"');"], type.label));
                }

                var layoutMenu = 
                    HtmlUtil.tag("li",[], HtmlUtil.onClick(get +".setLayout('table',1);", "Table - 1 column")) +"\n" +
                    HtmlUtil.tag("li",[], HtmlUtil.onClick(get +".setLayout('table',2);", "Table - 2 column")) +"\n" +
                    HtmlUtil.tag("li",[], HtmlUtil.onClick(get +".setLayout('table',3);", "Table - 3 column")) +"\n" +
                    HtmlUtil.tag("li",[], HtmlUtil.onClick(get +".setLayout('rows');", "Rows")) +"\n" +
                    HtmlUtil.tag("li",[], HtmlUtil.onClick(get +".setLayout('columns');", "Columns")) +"\n" +
                    HtmlUtil.tag("li",[], HtmlUtil.onClick(get +".setLayout('tabs');", "Tabs"));


                var menu = HtmlUtil.div(["class","ramadda-popup", "id", this.getDomId(ID_MENU_OUTER)], 
                                        HtmlUtil.tag("ul", ["id", this.getDomId(ID_MENU_INNER),"class", "sf-menu"], 
                                                     HtmlUtil.tag("li",[],"<a>New</a>" + HtmlUtil.tag("ul",[], newMenu)) +
                                                     HtmlUtil.tag("li",[],"<a>Layout</a>" + HtmlUtil.tag("ul", [], layoutMenu))));



                html += menu;
                html += HtmlUtil.tag("a", ["class", "display-menu-button", "id", this.getDomId(ID_MENU_BUTTON)]);
                html+="<br>";

                return html;
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
            getDefaultData: function() {
                for(var i in this.dataList) {
                    var data = this.dataList[i];
                    var records = data.getRecords();
                    if(records!=null) {
                        return data;
                    }
                }
                if(this.dataList.length>0) {
                    return this.dataList[0];
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

                if(props.data==null && this.dataList.length>0) {
                    props.data =  this.dataList[0];
                }
                var display =  eval(" new " + funcName+"(this, displayId, props);");
                if(display == null) {
                    console.log("Error: could not create display using:" + funcName);
                    alert("Error: could not create display using:" + funcName);
                    return;
                }
                this.addNewDisplay(display);
                return display;
            },
            addNewDisplay: function(display) {
                display.setDisplayManager(this);
                this.addDisplayEventListener(display);
                display.loadInitialData();
                this.super.addNewDisplay.apply(this, [display]);
            },
            removeDisplay:function(display) {
                for(var i=0;i< this.eventListeners.length;i++) {
                    eventListener = this.eventListeners[i];
                    if(eventListener.handleDisplayDelete!=null) {
                        eventListener.handleDisplayDelete(display);
                    }
                }
                this.super.removeDisplay.apply(this,[display]);
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

    var html = "";
    html += HtmlUtil.openTag("div", ["class","display-container"]);
    html += this.makeMainMenu();
    html +=  HtmlUtil.div(["id", this.getDomId(ID_DISPLAYS)]);
    $("#"+ this.getId()).html(html)
    if(this.showmap) {
        this.createDisplay('map');
    }
    var theDisplayManager = this;

    $("#"+this.getDomId(ID_MENU_BUTTON)).button({ icons: { primary: "ui-icon-gear", secondary: "ui-icon-triangle-1-s"}}).click(function(event) {
            var id =theDisplayManager.getDomId(ID_MENU_OUTER); 
            showPopup(event, theDisplayManager.getDomId(ID_MENU_BUTTON), id, false,null,"left bottom");
            $("#"+  theDisplayManager.getDomId(ID_MENU_INNER)).superfish({
                    animation: {height:'show'},
                        delay: 1200
                        });
        });

}


