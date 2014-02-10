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




//
//DisplayManager constructor
//id should correspond to a DOM element id
//

function DisplayManager(argId,argProperties) {
    RamaddaUtil.inherit(this, new DisplayThing(argId, argProperties));
    RamaddaUtil.defineMembers(this, {
                dataList : [],
                displayTypes: [],
                eventListeners: [],
                group: new DisplayGroup(this, argId),
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
           getLayoutManager: function () {
               return this.group;
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
                var closest =  RecordUtil.findClosest(records, lon, lat, indexObj);
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
                var layout = "getDisplayManager('" + this.getId() +"').getLayoutManager()";
                var html = "";
                var newMenu = "";
                for(var i=0;i<this.displayTypes.length;i++) {
                    //The ids (.e.g., 'linechart' have to match up with some class function with the name 
                    var type = this.displayTypes[i];
                    newMenu+= HtmlUtil.tag("li",[], HtmlUtil.tag("a", ["onclick", get+".createDisplay('" + type.type+"');"], type.label));
                }

                var layoutMenu = 
                    HtmlUtil.tag("li",[], HtmlUtil.onClick(layout +".setLayout('table',1);", "Table - 1 column")) +"\n" +
                    HtmlUtil.tag("li",[], HtmlUtil.onClick(layout +".setLayout('table',2);", "Table - 2 column")) +"\n" +
                    HtmlUtil.tag("li",[], HtmlUtil.onClick(layout +".setLayout('table',3);", "Table - 3 column")) +"\n" +
                    HtmlUtil.tag("li",[], HtmlUtil.onClick(layout +".setLayout('rows');", "Rows")) +"\n" +
                    HtmlUtil.tag("li",[], HtmlUtil.onClick(layout +".setLayout('columns');", "Columns")) +"\n" +
                    HtmlUtil.tag("li",[], HtmlUtil.onClick(layout +".setLayout('tabs');", "Tabs"));


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
           layoutChanged: function(display) {
               this.doLayout();
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
                var displayId = this.getUniqueId("display");
                if(props.data==null && this.dataList.length>0) {
                    props.data =  this.dataList[0];
                }
                var display =  eval(" new " + funcName+"(this, displayId, props);");
                if(display == null) {
                    console.log("Error: could not create display using:" + funcName);
                    alert("Error: could not create display using:" + funcName);
                    return;
                }
                this.addDisplay(display);
                return display;
            },
            addDisplay: function(display) {
                display.setDisplayManager(this);
                this.addDisplayEventListener(display);
                display.loadInitialData();
                this.getLayoutManager().addDisplay(display);
            },
            removeDisplay:function(display) {
                for(var i=0;i< this.eventListeners.length;i++) {
                    eventListener = this.eventListeners[i];
                    if(eventListener.handleDisplayDelete!=null) {
                        eventListener.handleDisplayDelete(display);
                    }
                }
                this.getLayoutManager().removeDisplay(display);
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


