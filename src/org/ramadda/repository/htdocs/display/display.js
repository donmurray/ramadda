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
    //    console.log("new display:" + display.getId());
    window.globalDisplays[display.getId()] = display;
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


function DisplayThing(argId, argProperties) {
    if(argProperties == null) {
       argProperties = {};
    }

    //check for booleans as strings
    for(var i in argProperties) {
        if(typeof  argProperties[i]  == "string") {
            if(argProperties[i] == "true") argProperties[i] =true;
            else if(argProperties[i] == "false") argProperties[i] =false;
            else continue;
            //            console.log("Changed type" + i);
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
            if(value != null) return value;
            if(this.displayParent!=null) {
                return this.displayParent.getProperty(key, dflt);
             }
             return dflt;
         }

        });
}





function RamaddaDisplay(argDisplayManager, argId, argType, argProperties) {
    $.extend(this, {
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
                contents = HtmlUtil.div(["class","display-contents-inner display-" + this.getType() +"-inner"], contents);
                this.writeHtml(ID_DISPLAY_CONTENTS, contents);
            },
            addEntry: function(entry) {
                this.entries.push(entry);
            },
            handleEventRecordSelection: function(source, args) {
                if(!source.getEntries) {
                    return;
                }
                for(var i in source.getEntries()) {
                    var entry = source.getEntries()[i];
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
            getLoadingMessage: function() {
                return this.getMessage("&nbsp;Loading...");
            },
            getMessage: function(msg) {
                return HtmlUtil.div(["class","display-message"], msg);
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
            fieldsHtml:"",
            addFieldsCheckboxes: function() {
                if(!this.hasData()) {
                    this.fieldsHtml = "No data";
                    return;
                }
                if(this.getProperty(PROP_FIELDS,null)!=null) {
                    //            return;
                }
                var html =  null;
                var checkboxClass = this.getId() +"_checkbox";
                var dataList =  this.dataCollection.getList();
                for(var collectionIdx=0;collectionIdx<dataList.length;collectionIdx++) {             
                    var pointData = dataList[collectionIdx];
                    var fields =pointData.getChartableFields();
                    fields = RecordUtil.sort(fields);
                    if(html == null) {
                        html = HtmlUtil.tag("b", [],  "Fields");
                        html += HtmlUtil.openTag("div", ["class", "display-fields"]);
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
                        html += HtmlUtil.tag("div", ["title", field.getId()],
                                             HtmlUtil.checkbox(field.checkboxId, checkboxClass,
                                                               on) +" " +field.getLabel()
                                             );
                    }
                }
                if(html == null) {
                    html = "";
                } else {
                    html+= HtmlUtil.closeTag("div");
                }

                this.fieldsHtml = html;
                this.writeHtml(ID_FIELDS,html);

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
                return  "getRamaddaDisplay('" + this.getId() +"')";
            },
            getEntryHtml: function(entry) {
                var menu = this.getEntryMenuButton(entry);
                var html = "";
                html += menu +" " + entry.getLink(entry.getIconImage() +" " + entry.getName());
                html += "<hr>";
                html += entry.getDescription();
                html += HtmlUtil.formTable();
                var columnNames = entry.getColumnNames();
                var columnLabels = entry.getColumnLabels();
                if(entry.getFilesize()>0) {
                    html+= HtmlUtil.formEntry("File:", entry.getFilename() +" " +
                                              HtmlUtil.href(entry.getFileUrl(), HtmlUtil.image(root +"/icons/download.png")) + " " +
                                              entry.getFormattedFilesize());
                }
                for(var i in columnNames) {
                    var columnName = columnNames[i];
                    var columnLabel = columnLabels[i];
                    var columnValue = entry.getColumnValue(columnName);
                    html+= HtmlUtil.formEntry(columnLabel+":", columnValue);
                }

                html += HtmlUtil.closeTag("table");
                return html;
        },
        getEntryMenuButton: function(entry) {
             var menuButton = HtmlUtil.onClick(this.getGet()+".showEntryMenu(event, '" + entry.getId() +"');", 
                                               HtmlUtil.image(root+"/icons/downdart.png", 
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
                var props = {
                    showMenu: true,
                    sourceEntry: entry
                };

                //TODO: figure out when to create data, check for grids, etc
                if(displayType != DISPLAY_ENTRYLIST) {
                    var url = root + "/entry/show?entryid=" + entryId +"&output=points.product&product=points.json&numpoints=1000";
                    var pointDataProps = {
                        entry: entry,
                        entryId: entry.getId()
                    };
                    props.data = new PointData(entry.getName(), null, null, url,pointDataProps);
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
                viewMenuItems.push(HtmlUtil.tag("li",[], HtmlUtil.tag("a", ["href", entry.getEntryUrl(),"target","_"], "View Entry")));
                if(entry.getFilesize()>0) {
                    fileMenuItems.push(HtmlUtil.tag("li",[], HtmlUtil.tag("a", ["href", entry.getFileUrl()], "Download " + entry.getFilename() + " (" + entry.getFormattedFilesize() +")")));
                }

                if(this.jsonUrl!=null) {
                    fileMenuItems.push(HtmlUtil.tag("li",[], "Data: " + HtmlUtil.onClick(get+".fetchUrl('json');", "JSON")
                                                    + HtmlUtil.onClick(get+".fetchUrl('csv');", "CSV")));
                }

                newMenuItems.push(HtmlUtil.tag("li",[], HtmlUtil.onClick(get+".createDisplay('" + entry.getId() +"','entrydisplay');", "New Entry Display")));

                if(fileMenuItems.length>0)
                    menus.push("<a>File</a>" + HtmlUtil.tag("ul",[], HtmlUtil.join(fileMenuItems)));
                if(viewMenuItems.length>0)
                    menus.push("<a>View</a>" + HtmlUtil.tag("ul",[], HtmlUtil.join(viewMenuItems)));
                if(newMenuItems.length>0)
                    menus.push("<a>New</a>" + HtmlUtil.tag("ul",[], HtmlUtil.join(newMenuItems)));

                //check if it has point data
                if(entry.getService("points.latlonaltcsv")) {
                    var newMenu = "";
                    for(var i=0;i<this.getDisplayManager().displayTypes.length;i++) {
                        var type = this.getDisplayManager().displayTypes[i];
                        if(!type.requiresData) continue;
                        
                        newMenu+= HtmlUtil.tag("li",[], HtmlUtil.tag("a", ["onclick", get+".createDisplay('" + entry.getId() +"','" + type.type+"');"], type.label));
                    }
                    menus.push("<a>New Chart</a>" + HtmlUtil.tag("ul",[], newMenu));
                }

                var topMenus = "";
                for(var i in menus) {
                    topMenus += HtmlUtil.tag("li",[], menus[i]);
                }

                var menu = HtmlUtil.tag("ul", ["id", this.getDomId(ID_MENU_INNER+entry.getId()),"class", "sf-menu"], 
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
                    console.log("url:" + url);
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
                    "<tr><td align=right><b>Name:</b></td><td> " + HtmlUtil.input("", this.getProperty("name",""), ["size","7","id",  this.getDomId("name")]) + "</td></tr>" +
                    "<tr><td align=right><b>Source:</b></td><td>"  + 
                    HtmlUtil.input("", this.getProperty("eventsource",""), ["size","7","id",  this.getDomId("eventsource")]) +
                    "</td></tr>" +
                    "<tr><td align=right><b>Width:</b></td><td> " + HtmlUtil.input("", this.getProperty("width",""), ["size","7","id",  this.getDomId("width")]) + "</td></tr>" +
                    "<tr><td align=right><b>Height:</b></td><td> " + HtmlUtil.input("", this.getProperty("height",""), ["size","7","id",  this.getDomId("height")]) + "</td></tr>" +
                    "<tr><td align=right><b>Row:</b></td><td> " + HtmlUtil.input("", this.getProperty("row",""), ["size","7","id",  this.getDomId("row")]) + "</td></tr>" +
                    "<tr><td align=right><b>Column:</b></td><td> " + HtmlUtil.input("", this.getProperty("column",""), ["size","7","id",  this.getDomId("column")]) + "</td></tr>" +
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
                var copyMe = HtmlUtil.onClick(get+".copyDisplay();", "Copy Display");
                var deleteMe = HtmlUtil.onClick("removeRamaddaDisplay('" + this.getId() +"')", "Remove Display");
                var menuItems = [];
                this.getMenuItems(menuItems);
                menuItems.push(copyMe);
                menuItems.push(deleteMe);

                if(this.jsonUrl!=null) {
                    menuItems.push("Data: " + HtmlUtil.onClick(get+".fetchUrl('json');", "JSON")
                                   + HtmlUtil.onClick(get+".fetchUrl('csv');", "CSV"));
                }
                var form = "<form>";

                form += this.getDisplayMenuSettings();
                for(var i in menuItems) {
                    form += HtmlUtil.div(["class","display-menu-item"], menuItems[i]);
                }
                form += "</form>";
                return HtmlUtil.div([], form);
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
                html+= HtmlUtil.div(["class","ramadda-popup", "id", this.getDomId(ID_MENU_OUTER)], "");
                var menu = HtmlUtil.div(["class", "display-dialog", "id", this.getDomId(ID_DIALOG)], "");
                var width = this.getWidth();
                var tableWidth = "100%";
                if(width>0) {
                    tableWidth = width+"px";
                }
                html += HtmlUtil.openTag("table", ["border","0", "width",tableWidth, "cellpadding","0", "cellspacing","0"]);
                html += HtmlUtil.openTag("tr", ["valign", "bottom"]);
                if(this.getShowTitle()) {
                    html += HtmlUtil.td([], HtmlUtil.div(["class","display-title","id",this.getDomId(ID_TITLE)], this.getTitle()));
                } else {
                    html += HtmlUtil.td([], "");
                }

                var get = this.getGet();

                if(this.getShowMenu()) {
                    var menuButton = HtmlUtil.onClick(get+".showDialog();", 
                                                      HtmlUtil.image(root+"/icons/downdart.png", 
                                                                     ["class", "display-dialog-button", "id",  this.getDomId(ID_DIALOG_BUTTON)]));
                    html += HtmlUtil.td(["align", "right"], menuButton);
                } else {
                    html += HtmlUtil.td(["align", "right"], "");
                }
                html += HtmlUtil.closeTag("tr");

                var contents = this.getContentsDiv();
                html += HtmlUtil.tr([], HtmlUtil.td(["colspan", "2"],contents));
                html += HtmlUtil.closeTag("table")
                html += menu;
                return html;
            },
             makeDialog: function() {
                var html = "";
                html +=   HtmlUtil.div(["id", this.getDomId(ID_HEADER),"class", "display-header"]);
                var get = this.getGet();

                var header = HtmlUtil.div(["class","display-dialog-header"], HtmlUtil.onClick("$('#" +this.getDomId(ID_DIALOG) +"').hide();",HtmlUtil.image(root +"/icons/close.gif",["class","display-dialog-close"])));

                var dialogContents = HtmlUtil.div(["class", "display-dialog-contents"], this.getDialogContents());
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
                return  HtmlUtil.div(["class","display-contents-inner display-" +this.type, "style", extraStyle, "id", this.getDomId(ID_DISPLAY_CONTENTS)],"");
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
            pointDataLoaded: function(pointData, url) {
                this.addData(pointData);
                this.updateUI(pointData);
                this.getDisplayManager().handleEventPointDataLoaded(this, pointData);
                if(url!=null) {
                    this.jsonUrl = url;
                } else {
                    this.jsonUrl = null;
                }
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



function DisplayGroup(argDisplayManager, argId, argProperties) {
    var LAYOUT_TABLE = "table";
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
                   if(display == source) continue;
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



