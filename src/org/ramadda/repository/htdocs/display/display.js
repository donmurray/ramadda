/**
Copyright 2008-2014 Geode Systems LLC
*/

//Ids of DOM components
var ID_FIELDS = "fields";
var ID_HEADER = "header";
var ID_TITLE = "title";
var ID_DISPLAY_CONTENTS = "contents";
var ID_DIALOG = "dialog";
var ID_DIALOG_BUTTON = "dialog_button";


var ID_MENU_BUTTON = "menu_button";
var ID_MENU_OUTER =  "menu_outer";
var ID_MENU_INNER =  "menu_inner";


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
    //    console.log("new display:" + display.id);
    window.globalDisplays[display.id] = display;
}


function getRamaddaDisplay(id) {
    if(window.globalDisplays == null) {
        return null;
    }
    //    console.log("get display:" + id);
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
    $.extend(this, properties);

    $.extend(this, {testFunction: function(){console.log("Parent Class:" + this.toString());}});

    RamaddaUtil.defineMembers(this, {
            id: id,
            properties:properties,
            displayParent: null,
            getId: function() {
            return this.id;
        },
        toString: function() {
                return "DisplayThing:" + this.getId();
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
            if(value != null) return value;
            if(this.displayParent!=null) {
                return this.displayParent.getProperty(key, dflt);
             }
             return dflt;
         }

        });
}





function RamaddaDisplay(displayManager, id, type, propertiesArg) {
    $.extend(this, {
            orientation: "horizontal",
        });

    RamaddaUtil.inherit(this,new DisplayThing(id, propertiesArg));

    $.extend(this, {testFunction: function(){console.log("Child Class:" + this.toString());}});


    RamaddaUtil.defineMembers(this, {
            type: type,
            displayManager:displayManager,
            filters: [],
            dataCollection: new DataCollection(),
            selectedCbx: [],
            entries: [],
            getDisplayManager: function() {
               return this.displayManager;
            },
            toString: function() {
                 return "RamaddaDisplay:" + this.getId();
             },
            getType: function() {
                return this.type;
            },
            setDisplayManager: function(cm) {
                this.displayManager = cm;
                this.setDisplayParent(cm);
            },
            setContents: function(contents) {
                contents = htmlUtil.div(["class","display-contents-inner display-" + this.getType() +"-inner"], contents);
                $("#" + this.getDomId(ID_DISPLAY_CONTENTS)).html(contents);
            },
            addEntry: function(entry) {
                this.entries.push(entry);
            },
            handleEntrySelection: function(source, entry, selected) {
                var containsEntry = this.entries.indexOf(entry) >=0;
                if(!containsEntry) {
                    return;
                }
                if(selected) {
                    $("#" + this.getDomId(ID_TITLE)).addClass("display-title-select");
                } else {
                    $("#" + this.getDomId(ID_TITLE)).removeClass("display-title-select");
                }
            },
            getEntries: function() {
                return this.entries;
            },
            hasEntries: function() {
                return this.entries.length>0;
            },
            getLoadingMessage: function() {
                return this.getMessage("&nbsp;Loading...");
            },
            getMessage: function(msg) {
                return htmlUtil.div(["class","display-message"], msg);
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
                    $("#" + this.getDomId(ID_FIELDS)).html("No data");
                    return;
                }
                if(this.getProperty(PROP_FIELDS,null)!=null) {
                    //            return;
                }
                var html =  null;
                var checkboxClass = this.id +"_checkbox";
                var dataList =  this.dataCollection.getList();
                for(var collectionIdx=0;collectionIdx<dataList.length;collectionIdx++) {             
                    var pointData = dataList[collectionIdx];
                    var fields =pointData.getChartableFields();
                    fields = RecordFieldSort(fields);
                    if(html == null) {
                        html = htmlUtil.tag("b", [],  "Fields");
                        html += htmlUtil.openTag("div", ["class", "display-fields"]);
                    } else {
                        html+= "<br>";
                    }



                    for(i=0;i<fields.length;i++) { 
                        var field = fields[i];
                        var idBase = "cbx_" + collectionIdx +"_" +i;
                        field.checkboxId  = this.getDomId(idBase);
                        var on = false;
                        if(this.selectedCbx.indexOf(idBase)>=0) {
                            on = true;
                        }  else if(this.selectedCbx.length==0) {
                            on = (i==0);
                        }
                        html += htmlUtil.tag("div", ["title", field.getId()],
                                             htmlUtil.checkbox(field.checkboxId, checkboxClass,
                                                               on) +" " +field.getLabel()
                                             );
                    }
                }
                if(html == null) {
                    html = "";
                } else {
                    html+= htmlUtil.closeTag("div");
                }

                $("#" + this.getDomId(ID_FIELDS)).html(html);

                var theDisplay = this;
                //Listen for changes to the checkboxes
                $("." + checkboxClass).click(function(event) {
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
                this.selectedCbx = [];
                for(var collectionIdx=0;collectionIdx<dataList.length;collectionIdx++) {             
                    var pointData = dataList[collectionIdx];
                    var fields = pointData.getChartableFields();
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

            getGet: function() {
                return  "getRamaddaDisplay('" + this.id +"')";
            },
            getEntryMenuButton: function(entry) {
                var menuButton = htmlUtil.onClick(this.getGet()+".showEntryMenu(event, '" + entry.getId() +"');", 
                                                  htmlUtil.image(root+"/icons/downdart.png", 
                                                                 ["class", "display-dialog-button", "id",  this.getDomId(ID_MENU_BUTTON + entry.getId())]));
                return menuButton;
            },
            getEntry: function(entryId) {
                var entry = null;
                if(this.entryList!=null) {
                    entry = this.entryList.getEntry(entryId);
                }
                if(entry == null) {
                    entry = getEntryManager().getEntry(entryId);
                }
                return entry;
            },
            createDisplay: function(entryId, displayType) {
                var entry = this.getEntry(entryId);
                if(entry == null) {
                    console.log("No entry:" + entryId);
                    return null;
                }
                //TODO: check for grids, etc
                var url = root + "/entry/show?entryid=" + entryId +"&output=points.product&product=points.json&numpoints=1000";
                var pointDataProps = {
                    entry: entry,
                    entryId: entry.getId()
                };
                var props = {
                        "showMenu": true,
                        "showMap": "false",
                        "data": new PointData(entry.getName(), null, null, url,pointDataProps)
                };
                if(this.lastDisplay!=null) {
                    props.column = this.lastDisplay.getColumn();
                    props.row = this.lastDisplay.getRow();
                } else {
                    props.column = this.getProperty("newColumn",this.getColumn());
                    props.row = this.getProperty("newRow",this.getRow());
                }
                this.lastDisplay = displayManager.createDisplay(displayType, props);
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
                viewMenuItems.push(htmlUtil.tag("li",[], htmlUtil.tag("a", ["href", entry.getEntryUrl()], "View Entry")));
                if(entry.getFilesize()>0) {
                    fileMenuItems.push(htmlUtil.tag("li",[], htmlUtil.tag("a", ["href", entry.getFileUrl()], "Download " + entry.getFilename() + " (" + entry.getFormattedFilesize() +")")));
                }

                menus.push("<a>File</a>" + htmlUtil.tag("ul",[], htmlUtil.join(fileMenuItems)));
                menus.push("<a>View</a>" + htmlUtil.tag("ul",[], htmlUtil.join(viewMenuItems)));

                //check if it has point data
                if(entry.getService("points.latlonaltcsv")) {
                    var newMenu = "";
                    for(var i=0;i<this.displayManager.displayTypes.length;i++) {
                        var type = this.displayManager.displayTypes[i];
                        if(!type.requiresData) continue;
                        
                        newMenu+= htmlUtil.tag("li",[], htmlUtil.tag("a", ["onclick", get+".createDisplay('" + entry.getId() +"','" + type.type+"');"], type.label));
                    }
                    menus.push("<a>New Chart</a>" + htmlUtil.tag("ul",[], newMenu));
                }

                var topMenus = "";
                for(var i in menus) {
                    topMenus += htmlUtil.tag("li",[], menus[i]);
                }

                var menu = htmlUtil.tag("ul", ["id", this.getDomId(ID_MENU_INNER+entry.getId()),"class", "sf-menu"], 
                                        topMenus);
                return menu;
            },
            showEntryMenu: function(event, entryId) {
                var menu = this.getEntryMenu(entryId);               
                $("#" + this.getDomId(ID_MENU_OUTER)).html(menu);
                showPopup(event, this.getDomId(ID_MENU_BUTTON+entryId), this.getDomId(ID_MENU_OUTER), false,null,"left bottom");
                $("#"+  this.getDomId(ID_MENU_INNER+entryId)).superfish({
                        animation: {height:'show'},
                            delay: 1200
                            });
            },
            getDisplayMenuContents: function() {
                var get = this.getGet();
                var copyMe = htmlUtil.onClick(get+".copyDisplay();", "Copy Display");
                var deleteMe = htmlUtil.onClick("removeRamaddaDisplay('" + this.id +"')", "Remove Display");
                var menuItems = [];
                this.getMenuItems(menuItems);
                menuItems.push(copyMe);
                menuItems.push(deleteMe);

                var form = "<form>";

                form += this.getDisplayMenuSettings();
                for(var i in menuItems) {
                    form += htmlUtil.div(["class","display-menu-item"], menuItems[i]);
                }
                form += "</form>";
                return htmlUtil.div([], form);
            },
            getMenuItems: function(menuItems) {
            },
            getDisplayMenuSettings: function() {
                var get = "getRamaddaDisplay('" + this.id +"')";
                var moveRight = htmlUtil.onClick(get +".moveDisplayRight();", "Right");
                var moveLeft = htmlUtil.onClick(get +".moveDisplayLeft();", "Left");
                var moveUp = htmlUtil.onClick(get +".moveDisplayUp();", "Up");
                var moveDown = htmlUtil.onClick(get +".moveDisplayDown();", "Down");

                var menu =  "<table>" +
                    "<tr style=\"border:1px #000 solid;\"><td align=right><b>Move:</b></td><td>" + moveUp + " " +moveDown+  " " +moveRight+ " " + moveLeft +"</td></tr>"  +
                    "<tr><td align=right><b>Name:</b></td><td> " + htmlUtil.input("", this.getProperty("name",""), ["size","7","id",  this.getDomId("name")]) + "</td></tr>" +
                    "<tr><td align=right><b>Source:</b></td><td>"  + 
                    htmlUtil.input("", this.getProperty("eventsource",""), ["size","7","id",  this.getDomId("eventsource")]) +
                    "</td></tr>" +
                    "<tr><td align=right><b>Width:</b></td><td> " + htmlUtil.input("", this.getProperty("width",""), ["size","7","id",  this.getDomId("width")]) + "</td></tr>" +
                    "<tr><td align=right><b>Height:</b></td><td> " + htmlUtil.input("", this.getProperty("height",""), ["size","7","id",  this.getDomId("height")]) + "</td></tr>" +
                    "<tr><td align=right><b>Row:</b></td><td> " + htmlUtil.input("", this.getProperty("row",""), ["size","7","id",  this.getDomId("row")]) + "</td></tr>" +
                    "<tr><td align=right><b>Column:</b></td><td> " + htmlUtil.input("", this.getProperty("column",""), ["size","7","id",  this.getDomId("column")]) + "</td></tr>" +
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
                this.displayManager.doLayout();
            },
            deltaRow: function(delta) {
                var row = parseInt(this.getProperty("row",0));
                row += delta;
                if(row<0) row = 0;
                this.setDisplayProperty("row",row);
                this.displayManager.doLayout();
            },
            moveDisplayRight: function() {
                if(this.displayManager.layout == LAYOUT_COLUMNS) {
                    this.deltaColumn(1);
                } else {
                    this.displayManager.moveDisplayDown(this);
                }
            },
            moveDisplayLeft: function() {
                if(this.displayManager.layout == LAYOUT_COLUMNS) {
                    this.deltaColumn(-1);
                } else {
                    this.displayManager.moveDisplayUp(this);
                }
            },
            moveDisplayUp: function() {
                if(this.displayManager.layout == LAYOUT_ROWS) {
                    this.deltaRow(-1);
                } else {
                    this.displayManager.moveDisplayUp(this);
                }
            },
            moveDisplayDown: function() {
                if(this.displayManager.layout == LAYOUT_ROWS) {
                    this.deltaRow(1);
                } else {
                    this.displayManager.moveDisplayDown(this);
                }
            },
            getMenuContents: function() {
                return this.getDisplayMenuContents();
             },
           initMenu: function() {
           },
           popup: function(srcId, popupId) {
                var popup = ramaddaUtil.getDomObject(popupId);
                var srcObj = ramaddaUtil.getDomObject(srcId);
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
                this.initMenu();
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

                //                console.log("getHtml:" + this.getId());
                //                this.testFunction();
                //                this.super.testFunction();
                //                this.super.testFunction.call(this);

                var html = "";
                html +=   htmlUtil.div(["id", this.getDomId(ID_HEADER),"class", "display-header"]);
                html+= htmlUtil.div(["class","ramadda-popup", "id", this.getDomId(ID_MENU_OUTER)], "");
                var get = "getRamaddaDisplay('" + this.id +"')";
                var menuButton = htmlUtil.onClick(get+".showDialog();", 
                                                  htmlUtil.image(root+"/icons/downdart.png", 
                                                                 ["class", "display-dialog-button", "id",  this.getDomId(ID_DIALOG_BUTTON)]));

                var header = htmlUtil.div(["class","display-dialog-header"], htmlUtil.onClick("$('#" +this.getDomId(ID_DIALOG) +"').hide();",htmlUtil.image(root +"/icons/close.gif",["class","display-dialog-close"])));

                var menuContents = htmlUtil.div(["class", "display-dialog-contents"], this.getMenuContents());
                menuContents  = header + menuContents;
                var menu = htmlUtil.div(["class", "display-dialog", "id", this.getDomId(ID_DIALOG)], menuContents);
                var width = this.getWidth();
                var tableWidth = "100%";
                if(width>0) {
                    tableWidth = width+"px";
                }
                html += htmlUtil.openTag("table", ["border","0", "width",tableWidth, "cellpadding","0", "cellspacing","0"]);
                html += htmlUtil.openTag("tr", ["valign", "bottom"]);
                if(this.getShowTitle()) {
                    html += htmlUtil.td([], htmlUtil.div(["class","display-title","id",this.getDomId(ID_TITLE)], this.getTitle()));
                } else {
                    html += htmlUtil.td([], "");
                }
                if(this.getShowMenu()) {
                    html += htmlUtil.td(["align", "right"], menuButton);
                } else {
                    html += htmlUtil.td(["align", "right"], "");
                }
                html += htmlUtil.closeTag("tr");

                var contents = this.getContentsDiv();
                html += htmlUtil.tr([], htmlUtil.td(["colspan", "2"],contents));
                html += htmlUtil.closeTag("table")
                html += menu;
                return html;
            },
            showDialog: function() {
                var dialog =this.getDomId(ID_DIALOG); 
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
                return  htmlUtil.div(["class","display-contents-inner display-" +this.type, "style", extraStyle, "id", this.getDomId(ID_DISPLAY_CONTENTS)],"");
            },
            copyDisplay: function() {
                var newOne = {};
                $.extend(true, newOne, this);
                newOne.id =   newOne.id +"_display_" + (this.displayManager.cnt++);
                addRamaddaDisplay(newOne);
                this.displayManager.addNewDisplay(newOne);
            },
            removeDisplay: function() {
                this.displayManager.removeDisplay(this);
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
                $("#" + this.getDomId(ID_TITLE)).html(title);
            },
            getTitle: function () {
                var prefix  = "";
                if(this.hasEntries()) {
                    prefix = this.getEntryMenuButton(this.getEntries()[0])+" ";
                }
                var title = this.getProperty("title");
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
            hasData: function() {
                return this.dataCollection.hasData();
            },
            addData: function(pointData) { 
                this.dataCollection.addData(pointData);
                var entry = pointData.getEntry();
                if(entry!=null) {
                    this.addEntry(entry);
                } else {
                    //console.log("no entry:" + pointData.entryId);
                }
            },
            //callback from the pointData.loadData call
            pointDataLoaded: function(pointData) {
                this.addData(pointData);
                this.updateUI(pointData);
                this.displayManager.pointDataLoaded(this, pointData);
            },
            //get an array of arrays of data 
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



