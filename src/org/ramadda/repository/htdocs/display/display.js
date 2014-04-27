/**
* Copyright 2008-2014 Geode Systems LLC
*
* Permission is hereby granted, free of charge, to any person obtaining a copy of this 
* software and associated documentation files (the "Software"), to deal in the Software 
* without restriction, including without limitation the rights to use, copy, modify, 
* merge, publish, distribute, sublicense, and/or sell copies of the Software, and to 
* permit persons to whom the Software is furnished to do so, subject to the following conditions:
* 
* The above copyright notice and this permission notice shall be included in all copies 
* or substantial portions of the Software.
* 
* THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, 
* INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR 
* PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE 
* FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR 
* OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER 
* DEALINGS IN THE SOFTWARE.
*/


//Ids of DOM components
var ID_FIELDS = "fields";
var ID_HEADER = "header";
var ID_TITLE = ATTR_TITLE;
var ID_DISPLAY_CONTENTS = "contents";
var ID_DIALOG = "dialog";
var ID_DIALOG_BUTTON = "dialog_button";
var ID_FOOTER = "footer";
var ID_FOOTER_LEFT = "footer_left";
var ID_FOOTER_RIGHT = "footer_right";
var ID_MENU_BUTTON = "menu_button";
var ID_MENU_OUTER =  "menu_outer";
var ID_MENU_INNER =  "menu_inner";



var ID_REPOSITORY = "repository";

var  displayDebug = false;


var PROP_DISPLAY_FILTER = "displayFilter";


var PROP_DIVID = "divid";
var PROP_FIELDS = "fields";
var PROP_LAYOUT_HERE = "layoutHere";
var PROP_HEIGHT  = "height";
var PROP_WIDTH  = "width";



function addRamaddaDisplay(display) {
    if(window.globalDisplays == null) {
        window.globalDisplays = {};
    }
    window.globalDisplays[display.getId()] = display;
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


function DisplayThing(argId, argProperties) {
    if(argProperties == null) {
       argProperties = {};
    }
    
    //check for booleans as strings
    for(var i in argProperties) {
        if(typeof  argProperties[i]  == "string") {
            if(argProperties[i] == "true") argProperties[i] =true;
            else if(argProperties[i] == "false") argProperties[i] =false;
        }
    }


    //Now look for the structured foo.bar=value
    for(var key  in argProperties) {
        var toks = key.split(".");
        if(toks.length<=1) {
            continue;
        }
        var map = argProperties;
        var topMap  = map;
        //graph.axis.foo=bar
        var v = argProperties[key];
        if(v == "true") v = true;
        else if(v == "false") v = false;
        for(var i=0;i<toks.length;i++) {
            var tok = toks[i];
            if(i == toks.length-1) {
                map[tok] = v;
                break;
            }
            var nextMap = map[tok];
            if(nextMap == null) {
                map[tok] = {};
                map = map[tok];
            }  else {
                map = nextMap;
            }
        }
    }

    $.extend(this, argProperties);

    RamaddaUtil.defineMembers(this, {
            objectId: argId,
            cnt: 0,
            properties:argProperties,
            displayParent: null,
            getId: function() {
                return this.objectId;
            },
            setId: function(id) {
                this.objectId = id;
            },
        getUniqueId: function(base) {
                var uid =  base +"_" +(this.cnt++);
                return uid;

        },
        toString: function() {
                return "DisplayThing:" + this.getId();
         },
       getDomId:function(suffix) {
                return this.getId() +"_" + suffix;
       },
       jq: function(componentId) {
             return $("#"+ this.getDomId(componentId));
       },
       writeHtml: function(idSuffix, html) {
                $("#" + this.getDomId(idSuffix)).html(html);
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
            return this.getFormValue("eventSource","");
       },
       setDisplayParent:  function (parent) {
             this.displayParent = parent;
       },
       getDisplayParent:  function () {
            return this.displayParent;
       },
       removeProperty: function(key) {
                this.properties[key] = null;
       },
       setProperty: function(key, value) {
           this.properties[key] = value;
        },

        


       getProperty: function(key, dflt) {
            var value = this.properties[key];
            if(value != null) {
                return value;
            }
            if(this.displayParent!=null) {
                return this.displayParent.getProperty(key, dflt);
             }
            if(this.getDisplayManager) {
                return this.getDisplayManager().getProperty(key, dflt);

            }
            return dflt;
         }

        });
}





function RamaddaDisplay(argDisplayManager, argId, argType, argProperties) {

    RamaddaUtil.initMembers(this, {
            orientation: "horizontal",
        });

    var SUPER;
    RamaddaUtil.inherit(this,SUPER = new DisplayThing(argId, argProperties));

    RamaddaUtil.defineMembers(this, {
            type: argType,
            displayManager:argDisplayManager,
            filters: [],
            dataCollection: new DataCollection(),
            selectedCbx: [],
            entries: [],
            getDisplayManager: function() {
               return this.displayManager;
            },
            getLayoutManager: function() {
                return this.getDisplayManager().getLayoutManager();
            },
            notifyEvent:function(func, source, data) {
                if(this[func] == null) { return;}
                this[func].apply(this, [source,data]);
            },
            toString: function() {
                 return "RamaddaDisplay:" + this.type +" - " + this.getId();
            },
            getType: function() {
                return this.type;
            },
            setDisplayManager: function(cm) {
                this.displayManager = cm;
                this.setDisplayParent(cm.getLayoutManager());
            },
            setContents: function(contents) {
                contents = HtmlUtil.div([ATTR_CLASS,"display-contents-inner display-" + this.getType() +"-inner"], contents);
                this.writeHtml(ID_DISPLAY_CONTENTS, contents);
            },
            addEntry: function(entry) {
                this.entries.push(entry);
            },
            handleEventRecordSelection: function(source, args) {
                if(!source.getEntries) {
                    return;
                }
                var entries = source.getEntries();
                for(var i =0;i<entries.length;i++) {
                    var entry = entries[i];
                    var containsEntry = this.getEntries().indexOf(entry) >=0;
                    if(containsEntry) {
                        this.highlightEntry(entry);
                        break;
                    }
                }
            },
            handleEventEntrySelection: function(source, args) {
                var containsEntry = this.getEntries().indexOf(args.entry) >=0;
                if(!containsEntry) {
                    return;
                }
                if(args.selected) {
                    $("#" + this.getDomId(ID_TITLE)).addClass("display-title-select");
                } else {
                    $("#" + this.getDomId(ID_TITLE)).removeClass("display-title-select");
                }
            },
            highlightEntry: function(entry) {
                $("#" + this.getDomId(ID_TITLE)).addClass("display-title-select");
            },
            getEntries: function() {
                return this.entries;
            },
            hasEntries: function() {
                return this.entries.length>0;
            },
            getWaitImage: function() {
                return HtmlUtil.image(ramaddaBaseUrl + "/icons/progress.gif");
            },
            getLoadingMessage: function() {
                return this.getMessage("&nbsp;Loading...");
            },
            getMessage: function(msg) {
                return HtmlUtil.div([ATTR_CLASS,"display-message"], msg);
            },
            getFieldValue: function(id, dflt) {
                var jq = $("#" + id);
                if(jq.size()>0) {
                    return jq.val();
                } 
                return dflt;
            },
            getFooter: function() {
                return  HtmlUtil.div([ATTR_ID,this.getDomId(ID_FOOTER),ATTR_CLASS,"display-footer"], 
                                           HtmlUtil.leftRight(HtmlUtil.div([ATTR_ID,this.getDomId(ID_FOOTER_LEFT),ATTR_CLASS,"display-footer-left"],""),
                                                              HtmlUtil.div([ATTR_ID,this.getDomId(ID_FOOTER_RIGHT),ATTR_CLASS,"display-footer-right"],"")));
            },
           checkFixedLayout: function() {
                if(this.getIsLayoutFixed()) {
                    var divid = this.getProperty(PROP_DIVID);
                    if(divid!=null) {
                        var html = this.getHtml();
                        $("#" + divid).html(html);
                    }
                }
            },
            addFieldsCheckboxes: function() {
                if(!this.hasData()) {
                    return;
                }
                var fixedFields = this.getProperty(PROP_FIELDS);
                if(fixedFields!=null) {
                    if(fixedFields.length==0) {
                        fixedFields = null;
                    } 
                }


                
                var html =  null;
                var checkboxClass = this.getId() +"_checkbox";
                var dataList =  this.dataCollection.getList();


                for(var collectionIdx=0;collectionIdx<dataList.length;collectionIdx++) {             
                    var pointData = dataList[collectionIdx];
                    
                    var fields =this.getFieldsToSelect(pointData);
                    if(html == null) {
                        html = HtmlUtil.tag(TAG_B, [],  "Fields");
                        html += HtmlUtil.openTag(TAG_DIV, [ATTR_CLASS, "display-fields"]);
                    } else {
                        html+= "<br>";
                    }


                    for(i=0;i<fields.length;i++) { 
                        var field = fields[i];
                        var idBase = "cbx_" + collectionIdx +"_" +i;
                        field.checkboxId  = this.getDomId(idBase);
                        var on = false;
                        if(fixedFields!=null) {
                            on = (fixedFields.indexOf(field.getId())>=0);
                        } else {
                            if(this.selectedCbx.indexOf(idBase)>=0) {
                                on = true;
                            }  else if(this.selectedCbx.length==0) {
                                on = (i==0);
                            }
                        }
                        html += HtmlUtil.tag(TAG_DIV, [ATTR_TITLE, field.getId()],
                                             HtmlUtil.checkbox(field.checkboxId, checkboxClass,
                                                               on) +" " +field.getLabel()
                                             );
                    }
                }
                if(html == null) {
                    html = "";
                } else {
                    html+= HtmlUtil.closeTag(TAG_DIV);
                }

                this.writeHtml(ID_FIELDS,html);

                var theDisplay = this;
                //Listen for changes to the checkboxes
                $("." + checkboxClass).click(function(event) {
                        theDisplay.removeProperty(PROP_FIELDS);
                        theDisplay.fieldSelectionChanged();
                    });
            },
            fieldSelectionChanged: function() {
            },
            getSelectedFields:function() {
                var df = [];
                var dataList =  this.dataCollection.getList();
                //If we have fixed fields then clear them after the first time
                var fixedFields = this.getProperty(PROP_FIELDS);
                if(fixedFields!=null) {
                    if(fixedFields.length==0) {
                        fixedFields = null;
                    } 
                }

                for(var collectionIdx=0;collectionIdx<dataList.length;collectionIdx++) {             
                    var pointData = dataList[collectionIdx];
                    var fields = this.getFieldsToSelect(pointData);
                    if(fixedFields !=null) {
                        for(i=0;i<fields.length;i++) { 
                            var field = fields[i];
                            if(fixedFields.indexOf(field.getId())>=0) {
                                df.push(field);
                            }
                        }
                    }
                }

                if(fixedFields !=null && fixedFields.length>0) {
                    return df;
                }

                var firstField = null;
                this.selectedCbx = [];
                for(var collectionIdx=0;collectionIdx<dataList.length;collectionIdx++) {             
                    var pointData = dataList[collectionIdx];
                    var fields = this.getFieldsToSelect(pointData);
                    for(i=0;i<fields.length;i++) { 
                        var field = fields[i];
                        if(firstField==null) firstField = field;

                        var idBase = "cbx_" + collectionIdx +"_" +i;
                        var cbxId =  this.getDomId(idBase)
                        if($("#" + cbxId).is(':checked')) {
                            this.selectedCbx.push(idBase);
                            df.push(field);
                        }
                    }
                }

                if(df.length==0 && firstField!=null) {
                    df.push(firstField);
                }
                return df;
            },
            getFieldsToSelect: function(pointData) {
                return  pointData.getChartableFields();
            },
            getGet: function() {
                return  "getRamaddaDisplay('" + this.getId() +"')";
            },
            getEntryHtml: function(entry, props) {
                var dfltProps = {
                    showHeader: true,
                    headerRight: false
                };
                $.extend(dfltProps, props);
                props = dfltProps;
                var menu = this.getEntryMenuButton(entry);
                var html = "";
                if(props.showHeader) {
                    var left = menu +" " + entry.getLink(entry.getIconImage() +" " + entry.getName());
                    if(props.headerRight) html += HtmlUtil.leftRight(left,props.headerRight);
                    else html += left;
                    html += "<hr>";
                }
                html += entry.getDescription();
                html += HtmlUtil.formTable();


                var columns = entry.getColumns();

                if(entry.getFilesize()>0) {
                    html+= HtmlUtil.formEntry("File:", entry.getFilename() +" " +
                                              HtmlUtil.href(entry.getResourceUrl(), HtmlUtil.image(ramaddaBaseUrl +"/icons/download.png")) + " " +
                                              entry.getFormattedFilesize());
                }
                for(var colIdx =0;colIdx< columns.length;colIdx++) {
                    var column = columns[colIdx];
                    var columnValue = entry.getColumnValue(column.getName());
                    if(column.isUrl()) {
                        var tmp = "";
                        var toks = columnValue.split("\n");
                        for(var i=0;i<toks.length;i++) {
                            var url = toks[i].trim();
                            if(url.length==0) continue;
                            tmp += HtmlUtil.href(url, url);
                            tmp += "<br>";
                        }
                        columnValue = tmp;
                    }

                    html+= HtmlUtil.formEntry(column.getLabel()+":", columnValue);
                }

                html += HtmlUtil.closeTag(TAG_TABLE);
                return html;
        },
        getEntryMenuButton: function(entry) {
             var menuButton = HtmlUtil.onClick(this.getGet()+".showEntryMenu(event, '" + entry.getId() +"');", 
                                               HtmlUtil.image(ramaddaBaseUrl+"/icons/downdart.png", 
                                                              [ATTR_CLASS, "display-dialog-button", ATTR_ID,  this.getDomId(ID_MENU_BUTTON + entry.getId())]));
             return menuButton;
         },
         setRamadda: function(e) {
                this.ramadda = e;
         },
         getRamadda: function() {
                if(this.ramadda!=null) {
                    return this.ramadda;
                }
                if(this.ramaddaBaseUrl !=null) {
                    this.ramadda =  getRamadda(this.ramaddaBaseUrl);
                    return this.ramadda;
                }
                return getGlobalRamadda();
        },
        getEntry: function(entryId) {
                var ramadda = this.getRamadda();
                var toks = entryId.split(",");
                if(toks.length==2) {
                    entryId = toks[1];
                    ramadda = getRamadda(toks[0]);
                }
                var entry = null;
                if(this.entryList!=null) {
                    entry = this.entryList.getEntry(entryId);
                }
                if(entry == null) {
                    entry = ramadda.getEntry(entryId);
                }
                if(entry == null) {
                    console.log("Display.getEntry: entry not found id=" + entryId +" repository=" + ramadda.getRoot());
                    entry = this.getRamadda().getEntry(entryId);
                }
                return entry;
            },
            addMapLayer: function(entryId) {
                var entry = this.getEntry(entryId);
                if(entry == null) {
                    console.log("No entry:" + entryId);
                    return;
                }
                this.getDisplayManager().addMapLayer(this, entry);
              
            },
            createDisplay: function(entryId, displayType, jsonUrl) {
                var entry = this.getEntry(entryId);
                if(entry == null) {
                    console.log("No entry:" + entryId);
                    return null;
                }
                var props = {
                    showMenu: true,
                    sourceEntry: entry
                };

                //TODO: figure out when to create data, check for grids, etc
                if(displayType != DISPLAY_ENTRYLIST) {
                    if(jsonUrl == null) {
                        jsonUrl = this.getPointUrl(entry);
                    }
                    var pointDataProps = {
                        entry: entry,
                        entryId: entry.getId()
                    };
                    props.data = new PointData(entry.getName(), null, null, jsonUrl,pointDataProps);
                }
                if(this.lastDisplay!=null) {
                    props.column = this.lastDisplay.getColumn();
                    props.row = this.lastDisplay.getRow();
                } else {
                    props.column = this.getProperty("newColumn",this.getColumn());
                    props.row = this.getProperty("newRow",this.getRow());
                }
                this.lastDisplay = this.getDisplayManager().createDisplay(displayType, props);
            },
            getPointUrl: function(entry) {
                //check if it has point data
                var service = entry.getService("points.json");
                if(service!=null) {
                    return  service.url;
                }
                service = entry.getService("grid.point.json");
                if(service!=null) {
                    return  service.url;
                }
                return null;
            },
            getEntryMenu: function(entryId) {
                var entry = this.getEntry(entryId);
                if(entry == null) {
                    return "null entry";
                }


                var get = this.getGet();
                var menus = [];
                var fileMenuItems = [];
                var viewMenuItems = [];
                var newMenuItems = [];
                viewMenuItems.push(HtmlUtil.tag(TAG_LI,[], HtmlUtil.tag(TAG_A, ["href", entry.getEntryUrl(),"target","_"], "View Entry")));
                if(entry.getFilesize()>0) {
                    fileMenuItems.push(HtmlUtil.tag(TAG_LI,[], HtmlUtil.tag(TAG_A, ["href", entry.getResourceUrl()], "Download " + entry.getFilename() + " (" + entry.getFormattedFilesize() +")")));
                }

                if(this.jsonUrl!=null) {
                    fileMenuItems.push(HtmlUtil.tag(TAG_LI,[], "Data: " + HtmlUtil.onClick(get+".fetchUrl('json');", "JSON")
                                                    + HtmlUtil.onClick(get+".fetchUrl('csv');", "CSV")));
                }

                newMenuItems.push(HtmlUtil.tag(TAG_LI,[], HtmlUtil.onClick(get+".createDisplay('" + entry.getFullId() +"','entrydisplay');", "New Entry Display")));

                if(fileMenuItems.length>0)
                    menus.push("<a>File</a>" + HtmlUtil.tag(TAG_UL,[], HtmlUtil.join(fileMenuItems)));
                if(viewMenuItems.length>0)
                    menus.push("<a>View</a>" + HtmlUtil.tag(TAG_UL,[], HtmlUtil.join(viewMenuItems)));
                if(newMenuItems.length>0)
                    menus.push("<a>New</a>" + HtmlUtil.tag(TAG_UL,[], HtmlUtil.join(newMenuItems)));

                //check if it has point data
                var pointUrl = this.getPointUrl(entry);
                if(pointUrl!=null) {
                    var newMenu = "";
                    for(var i=0;i<this.getDisplayManager().displayTypes.length;i++) {
                        var type = this.getDisplayManager().displayTypes[i];
                        if(!type.requiresData) continue;
                        
                        newMenu+= HtmlUtil.tag(TAG_LI,[], HtmlUtil.tag(TAG_A, ["onclick", get+".createDisplay(" + HtmlUtil.sqt(entry.getFullId()) +"," + HtmlUtil.sqt(type.type)+"," +HtmlUtil.sqt(pointUrl) +");"], type.label));
                    }
                    menus.push("<a>New Chart</a>" + HtmlUtil.tag(TAG_UL,[], newMenu));
                }

                var topMenus = "";
                for(var i=0;i<menus.length;i++) {
                    topMenus += HtmlUtil.tag(TAG_LI,[], menus[i]);
                }

                var menu = HtmlUtil.tag(TAG_UL, [ATTR_ID, this.getDomId(ID_MENU_INNER+entry.getId()),ATTR_CLASS, "sf-menu"], 
                                        topMenus);
                return menu;
            },
            showEntryMenu: function(event, entryId) {
                var menu = this.getEntryMenu(entryId);               
                this.writeHtml(ID_MENU_OUTER, menu);
                var srcId = this.getDomId(ID_MENU_BUTTON + entryId);

                showPopup(event, srcId, this.getDomId(ID_MENU_OUTER), false,null,"left bottom");
                $("#"+  this.getDomId(ID_MENU_INNER+entryId)).superfish({
                        animation: {height:'show'},
                            delay: 1200
                            });
           },
           fetchUrl: function(as, url) {
                if(url == null) {
                    url = this.jsonUrl;
                }
                url =  this.getDisplayManager().getJsonUrl(url, this);
                if(url == null) return;
                if(as !=null && as != "json") {
                    url = url.replace("points.json","points." + as);
                }
                window.open(url,'_blank');
            },
            getMenuItems: function(menuItems) {
            },
            getDisplayMenuSettings: function() {
                var get = "getRamaddaDisplay('" + this.getId() +"')";
                var moveRight = HtmlUtil.onClick(get +".moveDisplayRight();", "Right");
                var moveLeft = HtmlUtil.onClick(get +".moveDisplayLeft();", "Left");
                var moveUp = HtmlUtil.onClick(get +".moveDisplayUp();", "Up");
                var moveDown = HtmlUtil.onClick(get +".moveDisplayDown();", "Down");

                var menu =  "<table>" +
                    "<tr style=\"border:1px #000 solid;\"><td align=right><b>Move:</b></td><td>" + moveUp + " " +moveDown+  " " +moveRight+ " " + moveLeft +"</td></tr>"  +
                    "<tr><td align=right><b>Name:</b></td><td> " + HtmlUtil.input("", this.getProperty("name",""), ["size","7",ATTR_ID,  this.getDomId("name")]) + "</td></tr>" +
                    "<tr><td align=right><b>Source:</b></td><td>"  + 
                    HtmlUtil.input("", this.getProperty("eventsource",""), ["size","7",ATTR_ID,  this.getDomId("eventsource")]) +
                    "</td></tr>" +
                    "<tr><td align=right><b>Width:</b></td><td> " + HtmlUtil.input("", this.getProperty("width",""), ["size","7",ATTR_ID,  this.getDomId("width")]) + "</td></tr>" +
                    "<tr><td align=right><b>Height:</b></td><td> " + HtmlUtil.input("", this.getProperty("height",""), ["size","7",ATTR_ID,  this.getDomId("height")]) + "</td></tr>" +
                    "<tr><td align=right><b>Row:</b></td><td> " + HtmlUtil.input("", this.getProperty("row",""), ["size","7",ATTR_ID,  this.getDomId("row")]) + "</td></tr>" +
                    "<tr><td align=right><b>Column:</b></td><td> " + HtmlUtil.input("", this.getProperty("column",""), ["size","7",ATTR_ID,  this.getDomId("column")]) + "</td></tr>" +
                    "</table>";
                return menu;
           },
           isLayoutHorizontal: function(){ 
                return this.orientation == "horizontal";
            },
            loadInitialData: function() {
                if(!this.needsData() || this.properties.data==null) {
                    return;
                } 
                if(this.properties.data.hasData()) {
                    this.addData(this.properties.data);
                    return;
                } 
                this.properties.data.loadData(this);
            },
            getData: function() {
                if(!this.hasData()) return null;
                var dataList =  this.dataCollection.getList();
                return dataList[0];
            },
            hasData: function() {
                return this.dataCollection.hasData();
            },
            needsData: function() {
                return false;
            },
            getShowMenu: function() {
                return this.getProperty(PROP_SHOW_MENU, true);
            },
            getShowTitle: function() {
                return this.getProperty(PROP_SHOW_TITLE, true);
            },
            setDisplayProperty: function(key,value) {
                this.setProperty(key, value);
                $("#" + this.getDomId(key)).val(value);
            },
            deltaColumn: function(delta) {
                var column = parseInt(this.getProperty("column",0));
                column += delta;
                if(column<0) column = 0;
                this.setDisplayProperty("column",column);
                this.getLayoutManager().layoutChanged(this);
            },
            deltaRow: function(delta) {
                var row = parseInt(this.getProperty("row",0));
                row += delta;
                if(row<0) row = 0;
                this.setDisplayProperty("row",row);
                this.getLayoutManager().layoutChanged(this);
            },
            moveDisplayRight: function() {
                if(this.getLayoutManager.isLayoutColumns()) {
                    this.deltaColumn(1);
                } else {
                    this.getLayoutManager().moveDisplayDown(this);
                }
            },
            moveDisplayLeft: function() {
                if(this.getLayoutManager().isLayoutColumns()) {
                    this.deltaColumn(-1);
                } else {
                    this.getLayoutManager().moveDisplayUp(this);
                }
            },
            moveDisplayUp: function() {
                if(this.getLayoutManager().isLayoutRows()) {
                    this.deltaRow(-1);
                } else {
                    this.getLayoutManager().moveDisplayUp(this);
                }
            },
            moveDisplayDown: function() {
                if(this.getLayoutManager().isLayoutRows()) {
                    this.deltaRow(1);
                } else {
                    this.getLayoutManager().moveDisplayDown(this);
                }
            },
            getDialogContents: function() {
                var get = this.getGet();
                var menuItems = [];

                this.getMenuItems(menuItems);
                menuItems.push(HtmlUtil.onClick(get+".copyDisplay();", "Copy Display"));
                if(!this.getIsLayoutFixed()) {
                    menuItems.push(HtmlUtil.onClick("removeRamaddaDisplay('" + this.getId() +"')", "Remove Display"));
                }

                if(this.jsonUrl!=null) {
                    menuItems.push("Data: " + HtmlUtil.onClick(get+".fetchUrl('json');", "JSON")
                                   + HtmlUtil.onClick(get+".fetchUrl('csv');", "CSV"));
                }
                var form = "<form>";

                form += this.getDisplayMenuSettings();
                for(var i=0;i<menuItems.length;i++) {
                    form += HtmlUtil.div([ATTR_CLASS,"display-menu-item"], menuItems[i]);
                }
                form += "</form>";
                return HtmlUtil.div([], form);
            },
           popup: function(srcId, popupId) {
                var popup = GuiUtils.getDomObject(popupId);
                var srcObj = GuiUtils.getDomObject(srcId);
                if(!popup || !srcObj) return;
                var myalign = 'right top';
                var atalign = 'right bottom';
                showObject(popup);
                jQuery("#"+popupId ).position({
                        of: jQuery( "#" + srcId ),
                            my: myalign,
                            at: atalign,
                            collision: "none none"
                            });
                //Do it again to fix a bug on safari
                jQuery("#"+popupId ).position({
                        of: jQuery( "#" + srcId ),
                            my: myalign,
                            at: atalign,
                            collision: "none none"
                            });

                $("#" + popupId).draggable();
            },
            initUI:function() {
                this.checkFixedLayout();
            },
            initDisplay:function() {
                this.initUI();
                this.setContents("<p>default html<p>");
            },
            updateUI: function(data) {
            },

            /*
              This creates the default layout for a display
              Its a table:
              <td>title id=ID_HEADER</td><td>align-right popup menu</td>
              <td colspan=2><div id=ID_DISPLAY_CONTENTS></div></td>
              the getDisplayContents method by default returns:
              <div id=ID_DISPLAY_CONTENTS></div>
              but can be overwritten by sub classes
              After getHtml is called the DisplayManager will add the html to the DOM then call
              initDisplay
              That needs to call setContents with the html contents of the display
            */
            getHtml: function() {
                var html = "";
                html+= HtmlUtil.div([ATTR_CLASS,"ramadda-popup", ATTR_ID, this.getDomId(ID_MENU_OUTER)], "");
                var menu = HtmlUtil.div([ATTR_CLASS, "display-dialog", ATTR_ID, this.getDomId(ID_DIALOG)], "");
                var width = this.getWidth();
                var tableWidth = "100%";
                if(width>0) {
                    tableWidth = width+"px";
                }
                html += HtmlUtil.openTag(TAG_TABLE, [ATTR_CLASS, "display", "border","0", "width",tableWidth, "cellpadding","0", "cellspacing","0"]);
                html += HtmlUtil.openTag(TAG_TR, ["valign", "bottom"]);
                if(this.getShowTitle()) {
                    html += HtmlUtil.td([], HtmlUtil.div([ATTR_CLASS,"display-title",ATTR_ID,this.getDomId(ID_TITLE)], this.getTitle()));
                } else {
                    html += HtmlUtil.td([], "");
                }

                var get = this.getGet();

                if(this.getShowMenu()) {
                    var menuButton = HtmlUtil.onClick(get+".showDialog();", 
                                                      HtmlUtil.image(ramaddaBaseUrl+"/icons/downdart.png", 
                                                                     [ATTR_CLASS, "display-dialog-button", ATTR_ID,  this.getDomId(ID_DIALOG_BUTTON)]));
                    html += HtmlUtil.td(["align", "right"], menuButton);
                } else {
                    html += HtmlUtil.td(["align", "right"], "");
                }
                html += HtmlUtil.closeTag(TAG_TR);

                var contents = this.getContentsDiv();
                html += HtmlUtil.tr([], HtmlUtil.td(["colspan", "2"],contents));
                html += HtmlUtil.closeTag(TAG_TABLE)
                html += menu;
                return html;
            },
             makeDialog: function() {
                var html = "";
                html +=   HtmlUtil.div([ATTR_ID, this.getDomId(ID_HEADER),ATTR_CLASS, "display-header"]);
                var get = this.getGet();

                var header = HtmlUtil.div([ATTR_CLASS,"display-dialog-header"], HtmlUtil.onClick("$('#" +this.getDomId(ID_DIALOG) +"').hide();",HtmlUtil.image(ramaddaBaseUrl +"/icons/close.gif",[ATTR_CLASS,"display-dialog-close"])));

                var dialogContents = HtmlUtil.div([ATTR_CLASS, "display-dialog-contents"], this.getDialogContents());
                dialogContents  = header + dialogContents;
                return dialogContents;
            },
            initDialog: function() {
            },
            showDialog: function() {
                var dialog =this.getDomId(ID_DIALOG); 
                this.writeHtml(ID_DIALOG, this.makeDialog());
                this.initDialog();
                this.popup(this.getDomId(ID_DIALOG_BUTTON), dialog);
            },
            getContentsDiv: function() {
                var extraStyle = "";
                var width = this.getWidth();
                if(width>0) {
                    extraStyle += "width:" + width +"px; overflow-x: auto;";
                }
                var height = this.getProperty("height",-1);
                if(height>0) {
                    extraStyle += " height:" + height +"px; " + " max-height:" + height +"px; overflow-y: auto;";
                }
                return  HtmlUtil.div([ATTR_CLASS,"display-contents-inner display-" +this.type, "style", extraStyle, ATTR_ID, this.getDomId(ID_DISPLAY_CONTENTS)],"");
            },
            copyDisplay: function() {
                var newOne = {};
                $.extend(true, newOne, this);
                newOne.setId(newOne.getId() +this.getUniqueId("display"));
                addRamaddaDisplay(newOne);
                this.getDisplayManager().addDisplay(newOne);
            },
            removeDisplay: function() {
                this.getDisplayManager().removeDisplay(this);
            },
            //Gets called before the displays are laid out
            prepareToLayout:function() {
                //Force setting the property from the input dom (which is about to go away)
                this.getColumn();
                this.getWidth();
                this.getHeight();
                this.getName();
                this.getEventSource();
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
                this.writeHtml(ID_TITLE,title);
            },
            getTitle: function () {
                var prefix  = "";
                if(this.hasEntries()) {
                    prefix = this.getEntryMenuButton(this.getEntries()[0])+" ";
                }
                var title = this.getProperty(ATTR_TITLE);
                if(title!=null) {
                    return prefix +title;
                }
                if(this.dataCollection == null) {
                    return prefix;
                }
                var dataList =  this.dataCollection.getList();
                title = "";
                for(var collectionIdx=0;collectionIdx<dataList.length;collectionIdx++) {             
                    var pointData = dataList[collectionIdx];
                    if(collectionIdx>0) title+="/";
                    title += pointData.getName();
                }

                return prefix+title;
            },
            getIsLayoutFixed: function() {
                return this.getProperty(PROP_LAYOUT_HERE,false);
            },
            doingQuickEntrySearch: false,
            doQuickEntrySearch: function(request, callback) {
                if(this.doingQuickEntrySearch) return;
                var text = request.term;
                if(text == null || text.length<=1) return;
                this.doingQuickEntrySearch = true;
                var searchSettings = new EntrySearchSettings({
                        name: text,
                        max: 10,
                    });
                if(this.searchSettings) {
                    searchSettings.clearAndAddType(this.searchSettings.entryType);
                }
                var theDisplay = this;
                var jsonUrl = this.getRamadda().getSearchUrl(searchSettings, OUTPUT_JSON);
                var handler = {
                    entryListChanged: function(entryList) {
                        theDisplay.doneQuickEntrySearch(entryList, callback);
                    }
                };
                var entryList =  new EntryList(jsonUrl, handler);
            },
            doneQuickEntrySearch: function(entryList, callback) {
                var names = [];
                var entries = entryList.getEntries();
                for(var i=0;i<entries.length;i++) {
                    names.push(entries[i].getName());
                }
                callback(names);
                this.doingQuickEntrySearch = false;

            },
            hasData: function() {
                return this.dataCollection.hasData();
            },
            addData: function(pointData) { 
                var records = pointData.getRecords();
                if(records.length>0) {
                    this.hasElevation = records[0].hasElevation();
                } else {
                    this.hasElevation = false;
                }

                this.dataCollection.addData(pointData);
                var entry  =  pointData.entry;
                if(entry == null) {
                    entry  = this.getRamadda().getEntry(pointData.entryId);
                } 
                if(entry) {
                    pointData.entry = entry;
                    this.addEntry(entry);
                }
            },
            //callback from the pointData.loadData call
            pointDataLoaded: function(pointData, url, reload)  {
                if(!reload) {
                    this.addData(pointData);
                }
                this.updateUI(pointData);
                if(!reload) {
                    this.getDisplayManager().handleEventPointDataLoaded(this, pointData);
                }
                if(url!=null) {
                    this.jsonUrl = url;
                } else {
                    this.jsonUrl = null;
                }
            },
            //get an array of arrays of data 
            getStandardData : function(fields, props) {
                if(props == null) {
                    props = {
                        includeIndex: true,
                    };
                }
                
                var dataList = [];
                //The first entry in the dataList is the array of names
                //The first field is the domain, e.g., time or index
                //        var fieldNames = ["domain","depth"];
                var fieldNames = [];


                if(props.includeIndex) {
                    fieldNames.push("Date");
                }

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
                var pointData = this.dataCollection.getList()[0];




                var nonNullRecords = 0;
                var indexField = this.indexField;
                var allFields = this.allFields;
                var records = pointData.getRecords();

                //Check if there are dates and if they are different
                this.hasDate = false;
                var lastDate = null;
                for(j=0;j<records.length;j++) { 
                    var record = records[j];
                    var date = record.getDate();
                    if(date==null) {
                        continue;
                    }
                    if(lastDate!=null && lastDate.getTime()!=date.getTime()) {
                        this.hasDate = true;
                        break
                    }
                    lastDate = date;
                }




                for(j=0;j<records.length;j++) { 
                    var record = records[j];
                    var values = [];
                    if(props.includeIndex) {
                        var date = record.getDate();
                        if(indexField>=0) {
                            var field = allFields[indexField];
                            var value = record.getValue(indexField);
                            if(j==0) {
                                fieldNames[0] = field.getLabel();
                            }
                            values.push(value);
                        } else {
                            if(this.hasDate) {
                                date = new Date(date);
                                values.push(date);
                            } else {
                                if(j==0) {
                                    fieldNames[0] = "Index";
                                }
                                values.push(j);
                            }
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
                    //                    console.log("values:" + values);
                    dataList.push(values);
                    if(!allNull) {
                        nonNullRecords++;
                    }
                }
                if(nonNullRecords==0) {
                    //                    console.log("Num non null:" + nonNullRecords);
                    return [];
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

        var filter = this.getProperty(PROP_DISPLAY_FILTER);
        if(filter!=null) {
            //semi-colon delimited list of filter definitions
            //display.filter="filtertype:params;filtertype:params;
            //display.filter="month:0-11;
            var filters = filter.split(";");
            for(var i=0;i<filters.length;i++) {
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



function DisplayGroup(argDisplayManager, argId, argProperties) {
    var LAYOUT_TABLE = TAG_TABLE;
    var LAYOUT_TABS = "tabs";
    var LAYOUT_COLUMNS = "columns";
    var LAYOUT_ROWS = "rows";

    var SUPER;
    RamaddaUtil.inherit(this, SUPER = new RamaddaDisplay(argDisplayManager, argId, "group", argProperties));

    RamaddaUtil.defineMembers(this, {
            layout:this.getProperty(PROP_LAYOUT_TYPE, LAYOUT_TABLE)});


    RamaddaUtil.defineMembers(this, {
            displays : [],
            layout:this.getProperty(PROP_LAYOUT_TYPE, LAYOUT_TABLE),
            columns:this.getProperty(PROP_LAYOUT_COLUMNS, 1),
            isLayoutColumns: function() {
                return this.layout == LAYOUT_COLUMNS;
            },
            walkTree: function(func, data) {
                for(var i=0;i<this.displays.length;i++) {
                    var display  = this.displays[i];
                    if(display.walkTree!=null) {
                        display.walkTree(func, data);
                    } else {
                        func.call(data, display);
                    }
                }
            }, 
            collectEntries: function(entries) {
                if(entries == null) entries = [];
                for(var i=0;i<this.displays.length;i++) {
                    var display  = this.displays[i];
                    if(display.collectEntries!=null) {
                        display.collectEntries(entries);
                    } else {
                        var displayEntries = display.getEntries();
                        if(displayEntries!=null && displayEntries.length>0) {
                            entries.push({source: display, entries: displayEntries});
                        }
                    }
                }
                return entries;
            },
            isLayoutRows: function() {
                return this.layout == LAYOUT_ROWS;
            },

            getPosition:function() {
                for(var i=0;i<this.displays.length;i++) {
                    var display  = this.displays[i];
                    if(display.getPosition) {
                        return display.getPosition();
                    }
                }
            },
            getDisplays:function() {
                return this.display;
            },
            notifyEvent:function(func, source, data) {
               var displays  = this.getDisplays();
               for(var i=0;i<this.displays.length;i++) {
                   var display = this.displays[i];
                   if(display == source) {
                       continue;
                   }
                   var eventSource  = display.getEventSource();
                   if(eventSource!=null && eventSource.length>0) {
                       if(eventSource!= source.getId() && eventSource!= source.getName()) {
                           continue;
                        }
                   }
                   display.notifyEvent(func, source, data);
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
            addDisplay: function(display) {
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
                var displaysToPrepare = this.displays;

                for(var i=0;i<displaysToPrepare.length;i++) {
                    var display = displaysToPrepare[i];
                    if(display.prepareToLayout!=null) {
                        display.prepareToLayout();
                    }
                }

                if(this.layout == LAYOUT_TABLE) {
                    if(displaysToLayout.length == 1) {
                        html+= displaysToLayout[0].getHtml();
                    } else {
                        var width = Math.round(100/this.columns)+"%";
                        html+=HtmlUtil.openTag(TAG_TABLE, ["border","0","width", "100%", "cellpadding", "5",  "cellspacing", "5"]);
                        for(var i=0;i<displaysToLayout.length;i++) {
                            colCnt++;
                            if(colCnt>=this.columns) {
                                if(i>0) {
                                    html+= HtmlUtil.closeTag(TAG_TR);
                                }
                                html+= HtmlUtil.openTag(TAG_TR,["valign", "top"]);
                                colCnt=0;
                            }
                            html+=HtmlUtil.tag(TAG_TD, ["width", width], HtmlUtil.div([], displaysToLayout[i].getHtml()));
                        }
                        html+= HtmlUtil.closeTag(TAG_TR);
                        html+= HtmlUtil.closeTag(TAG_TABLE);
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
                        html+=HtmlUtil.openTag(TAG_TABLE, ["border","0","width", "100%", "cellpadding", "5",  "cellspacing", "0"]);
                        html+=HtmlUtil.openTag(TAG_TR, ["valign","top"]);
                        for(var col=0;col<cols.length;col++) {
                            html+=HtmlUtil.tag(TAG_TD,["width", width], cols[col]);
                        }
                        html+= HtmlUtil.closeTag(TAG_TR);
                        html+= HtmlUtil.closeTag(TAG_TABLE);
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
                    html+=HtmlUtil.openTag(TAG_TABLE, ["border","0","width", "100%", "cellpadding", "5",  "cellspacing", "0"]);
                    html+=HtmlUtil.openTag(TAG_TR, ["valign","top"]);
                    var width = Math.round(100/cols.length)+"%";
                    for(var i=0;i<cols.length;i++) {
                        var rows = cols[i];
                        var contents = "";
                        for(var j=0;j<rows.length;j++) {
                            contents+= rows[j];
                        }
                        html+=HtmlUtil.tag(TAG_TD, ["width", width, "valign","top"], contents);
                    }
                    html+= HtmlUtil.closeTag(TAG_TR);
                    html+= HtmlUtil.closeTag(TAG_TABLE);
                } else {
                    html+="Unknown layout:" + this.layout;
                }
                this.writeHtml(ID_DISPLAYS, html);
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



