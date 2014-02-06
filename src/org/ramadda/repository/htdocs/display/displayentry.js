
var DISPLAY_ENTRYLIST = "entrylist";
var DISPLAY_ENTRYDISPLAY = "entrydisplay";
var DISPLAY_OPERANDS = "operands";


addGlobalDisplayType({type: DISPLAY_ENTRYLIST, label:"Entry List",requiresData:false});
addGlobalDisplayType({type: DISPLAY_ENTRYDISPLAY, label:"Entry Display",requiresData:false});
addGlobalDisplayType({type: DISPLAY_OPERANDS, label:"Operands",requiresData:false});

function RamaddaEntryDisplay(displayManager, id, type, properties) {
     $.extend(this, new RamaddaDisplay(displayManager, id, type, properties));
     $.extend(this, {
             settings: new EntrySearchSettings({
                     parent: properties.entryParent,
                     text: properties.entryText,
                     entryType: properties.entryType,
             }),
             entryList: null,
             entryMap: {},
             getSettings: function() {
                 return this.settings;
             }
        });
     if(properties.entryType!=null) {
         this.settings.addType(properties.entryType);
     }
}


function RamaddaEntrylistDisplay(displayManager, id, properties) {

    var NONE = "-- None --";

    var ID_ENTRIES = "entries";
    var ID_FOOTER = "footer";
    var ID_FOOTER_LEFT = "footer_left";
    var ID_FOOTER_RIGHT = "footer_right";


    var ID_TEXT_FIELD = "textfield";
    var ID_TYPE_FIELD = "typefield";
    var ID_SEARCH = "search";
    var ID_RESULTS = "results";
    var ID_FORM = "form";
    var ID_DATE_START = "date_start";
    var ID_DATE_END = "date_end";
    var ID_MENU_BUTTON = "menu_button";
    var ID_MENU_OUTER =  "menu_outer";
    var ID_MENU_INNER =  "menu_inner";





    $.extend(this, {
            showForm: true,            
            showTypes: true,           
            fullForm: true,            
            showEntries: true,
            share: true
    });            

    $.extend(this, new RamaddaEntryDisplay(displayManager, id, DISPLAY_ENTRYLIST, properties));
    //hack to bool
    this.showForm = (""+this.showForm) =="true";
    this.showEntries = (""+this.showEntries) == "true";
    this.share = (""+this.share) == "true";
    this.showTypes = (""+this.showTypes) == "true";
    this.fullForm = (""+this.fullForm) == "true";


    addRamaddaDisplay(this);
    $.extend(this, {
            haveSearched: false,
            haveTypes: false,
            selectedEntries: [],            
            getSelectedEntries: function() {return this.selectedEntries;},
            initDisplay: function() {
                this.initUI();
                var html = "";
                var horizontal = this.isLayoutHorizontal();

                var footer =  htmlUtil.div(["id",this.getDomId(ID_FOOTER),"class","display-entrylist-footer"], 
                                           htmlUtil.leftRight(htmlUtil.div(["id",this.getDomId(ID_FOOTER_LEFT),"class","display-entrylist-footer-left"],""),
                                                              htmlUtil.div(["id",this.getDomId(ID_FOOTER_RIGHT),"class","display-entrylist-footer-right"],"")));
                
                if(horizontal) {
                    html+= htmlUtil.openTag("table",["border","0", "width","100%", "cellpadding","0","cellpadding","5"]);
                    html += htmlUtil.openTag("tr",["valign","top"]);
                    if(this.showForm) {
                        html += htmlUtil.tag("td",[],this.makeSearchForm());
                    }
                    if(this.showEntries) {
                        html += htmlUtil.tag("td",[],htmlUtil.div(["id",this.getDomId(ID_ENTRIES),"class","display-entrylist-entries"], this.getLoadingMessage()));
                    }
                    html += htmlUtil.closeTag("tr");

                    html += htmlUtil.openTag("tr",["valign","top"]);
                    if(this.showForm) {
                        html += htmlUtil.tag("td",[],"");
                    }
                    if(this.showEntries) {
                        html += htmlUtil.tag("td",[],footer);
                    }
 
                    html += htmlUtil.closeTag("tr");
                    html += htmlUtil.closeTag("table");
                } else {
                    if(this.showForm) {
                        html += this.makeSearchForm();
                    }
                    if(this.showEntries) {
                        html += htmlUtil.div(["id",this.getDomId(ID_ENTRIES),"class","display-entrylist-entries"], this.getLoadingMessage());
                        html += footer;
                    }
                }

                this.setContents(html);



                var theDisplay  = this;
                $("#" + this.getDomId(ID_SEARCH)).button().click(function(event) {
                        theDisplay.submitSearchForm();
                        event.preventDefault();
                    });



                $( "#" + this.getDomId(ID_FORM)).submit(function( event ) {
                        theDisplay.submitSearchForm();
                        event.preventDefault();
                    });

                if(this.entryList!=null && this.entryList.haveLoaded) {
                    this.entryListChanged(this.entryList);
                }
                this.addTypes();
                if(!this.haveSearched) {
                    this.submitSearchForm();
                }
            },
            getFieldValue: function(id, dflt) {
                var jq = $("#" + this.getDomId(id));
                if(jq.size()>0) {
                    return jq.val();
                } 
                return dflt;
            },
            submitSearchForm: function() {
                this.haveSearched = true;
                this.settings.text = this.getFieldValue(ID_TEXT_FIELD, this.settings.text);
                if(this.haveTypes) {
                    this.settings.entryType  = this.getFieldValue(ID_TYPE_FIELD, this.settings.entryType);
                }
                this.settings.clearAndAddType(this.settings.entryType);
                
                var footer = "Links: ";
                var outputs = getEntryManager().getSearchLinks(this.settings);
                for(var i in outputs) {
                    if(i>0)
                        footer += " - ";
                    footer += outputs[i];
                }
                this.footerRight  = footer;
                $("#"  +this.getDomId(ID_FOOTER_RIGHT)).html(this.footerRight);

                var jsonUrl = getEntryManager().getSearchUrl(this.settings, OUTPUT_JSON);
                console.log("json:" + jsonUrl);
                this.entryList = new EntryList(jsonUrl, this);
                $("#"+this.getDomId(ID_ENTRIES)).html(this.getLoadingMessage());
            },
            makeSearchForm: function() {
                var html = "";
                html += htmlUtil.openTag("form",["id",this.getDomId(ID_FORM),"action","#"]);
                html += htmlUtil.formTable();
                var text = this.settings.text;
                if(text == null) text = "";
                html+= htmlUtil.formEntry("Text:", 
                                          htmlUtil.input("", text, ["class","input", "size","15","id",  this.getDomId(ID_TEXT_FIELD)]));
                if(this.showTypes) {
                    var typeSelect= htmlUtil.tag("select",["id", this.getDomId(ID_TYPE_FIELD),
                                                           "class","display-typelist"],

                                                 htmlUtil.tag("option",["title","","value",""],
                                                              NONE));

                    html+= htmlUtil.formEntry("Type:", typeSelect);
                }

                if(this.fullForm) {
                }


                html+= htmlUtil.formEntry("", 
                                          htmlUtil.div(["id", this.getDomId(ID_SEARCH)],"Search"));
                html+= htmlUtil.formEntry("", htmlUtil.div(["class","display-entrylist-results", "id",this.getDomId(ID_RESULTS)],"&nbsp;"));

                html+= htmlUtil.closeTag("table");

                //Hide the real submit button
                html += "<input type=\"submit\" style=\"position:absolute;left:-9999px;width:1px;height:1px;\"/>";
                html += htmlUtil.closeTag("form");
                return html;
            },
            addTypes: function(types) {
                if(types == null) {
                    var theDisplay = this;
                    types = getEntryManager().getEntryTypes(function(types) {theDisplay.addTypes(types);});
                }
                if(types == null) {
                    return;
                }
                this.haveTypes = true;
                var cats =[];
                var catMap = {};
                var select = htmlUtil.tag("option",["title","","value",""],NONE);
                for(var i in types) {
                    var type = types[i];
                    var map = catMap[type.getCategory()];
                    var style = " background: URL(" + type.getIcon() +") no-repeat;";
                    
                    var optionAttrs  = ["title",type.getLabel(),"value",type.getId(),"class", "display-typelist-type",
                                        "style", style];
                    var selected =  this.settings.hasType(type.getId());
                    if(selected) {
                        optionAttrs.push("selected");
                        optionAttrs.push(null);
                    }
                    var option = htmlUtil.tag("option",optionAttrs, type.getLabel() +" (" + type.getEntryCount() +")");
                    if(map == null) {
                        catMap[type.getCategory()] = htmlUtil.tag("option",["class", "display-typelist-category", "title","","value",""],type.getCategory());
                        cats.push(type.getCategory());
                    }
                    catMap[type.getCategory()] += option;

                }
                for(var i in cats) {
                    select += catMap[cats[i]];
                }
                $("#" + this.getDomId(ID_TYPE_FIELD)).html(select);
            },
            handleEntrySelection: function(source, entry, selected) {
                this.selectEntry(entry, selected);
            },
            selectEntry: function(entry, selected) {
                var changed  = false;
                if(selected) {
                    $("#"+ this.getDomId("entry_" + entry.getId())).addClass("ui-selected");
                    var index = this.selectedEntries.indexOf(entry);
                    if (index < 0) {
                        this.selectedEntries.push(entry);
                        changed = true;
                    }
                } else {
                    $("#"+ this.getDomId("entry_" + entry.getId())).removeClass("ui-selected");
                    var index = this.selectedEntries.indexOf(entry);
                    if (index >= 0) {
                        this.selectedEntries.splice(index, 1);
                        changed = true;
                    }
                }
            },
            createDisplay: function(entryId, displayType) {
                var entry = this.entryList.getEntry(entryId);
                if(entry == null) {
                    console.log("No entry:" + entryId);
                    return;
                }
                var url = root + "/entry/show?entryid=" + entryId +"&output=points.product&product=points.json&numpoints=1000";
                var props = {
                        "showMenu": true,
                        "showMap": "false",
                        "data": new PointData(entry.getName(), null, null, url)
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
            entryListChanged: function(entryList) {
                this.entryList = entryList;
                var rowClass = "entryrow_" + this.getId()
                var entries = this.entryList.getEntries();
                var html = "";
                if(entries.length==0) {
                    $("#" + this.getDomId(ID_ENTRIES)).html(this.getMessage("Nothing found"));
                    $("#"  +this.getDomId(ID_FOOTER_LEFT)).html("");
                    $("#" + this.getDomId(ID_RESULTS)).html("");
                    return;
                }

                var results = "Found: " + entries.length +" " ;
                if(entries.length == this.settings.getMax()) {
                    results += "more";
                } else {

                }
                $("#" + this.getDomId(ID_RESULTS)).html(results);


                html += htmlUtil.openTag("ol",["class","display-entrylist-list", "id",this.getDomId("list")]);
                html  += "\n";
                var get = this.getGet();
                $("#"  +this.getDomId(ID_FOOTER_LEFT)).html("");
                if(this.footerRight!=null) {
                    $("#"  +this.getDomId(ID_FOOTER_RIGHT)).html(this.footerRight);
                }
                html+= htmlUtil.div(["class","ramadda-popup", "id", this.getDomId(ID_MENU_OUTER)], "");
                for(var i=0;i<entries.length;i++) {
                    var entry = entries[i];
                    var right ="";
                    var hasLocation = entry.hasLocation();
                    if(hasLocation) {
                        //                        right += htmlUtil.image(root+"/icons/map.png",["title","Location:" +entry.getLocationLabel()]);
                    }
                    var menuButton = htmlUtil.onClick(get+".showEntryMenu(event, '" + entry.getId() +"');", 
                                                  htmlUtil.image(root+"/icons/downdart.png", 
                                                                 ["class", "display-dialog-button", "id",  this.getDomId(ID_MENU_BUTTON + entry.getId())]));

                    right += menuButton;
                    var newMenu = "";




                    var icon = entry.getIconImage(["title","View entry"]);
                    var link  =  htmlUtil.tag("a",["href", entry.getEntryUrl()],icon);
                    var entryName = entry.getName();
                    if(entryName.length>100) {
                        entryName = entryName.substring(0,99)+"...";
                    }
                    var left =  htmlUtil.div(["style"," white-space: nowrap;  overflow-x:none; max-width:300px;"],link +" " +  entryName);
                    var line = htmlUtil.leftRight(left,right);
                    html  += htmlUtil.tag("li",["id",
                                                this.getDomId("entry_" + entry.getId()),
                                                "entryid",entry.getId(), "class","display-entrylist-entry ui-widget-content " + rowClass], line);
                    html  += "\n";
                }
                html += htmlUtil.closeTag("ol");

                $("#"+this.getDomId(ID_ENTRIES)).html(html);
                var theDisplay   =this;
                $("#" + this.getDomId("list")).selectable({
                        cancel: 'a',
                        selected: function( event, ui ) {
                            //                            console.log("i:" + event.shiftKey);
                            var entryId = ui.selected.getAttribute('entryid');
                            var entry = theDisplay.entryList.getEntry(entryId);
                            //                            console.log("selected:" +  entry);
                            if(entry!=null) {
                                theDisplay.selectedEntries.push(entry);
                                theDisplay.getDisplayManager().handleEntrySelection(theDisplay, entry, true);
                            }
                        },
                        unselected: function( event, ui ) {
                            var entryId = ui.unselected.getAttribute('entryid');
                            var entry = theDisplay.entryList.getEntry(entryId);
                            var index = theDisplay.selectedEntries.indexOf(entry);
                            //                            console.log("remove:" +  index + " " + theDisplay.selectedEntries);
                            if (index > -1) {
                                theDisplay.selectedEntries.splice(index, 1);
                                theDisplay.getDisplayManager().handleEntrySelection(theDisplay, entry, false);
                            }
                        },

                    });


                //                var mouseEnter = function(event,ui) {console.log("in " +ui);};
                //                var mouseExit = function(event) {console.log("out " + event.ui);};
                //                $("." + rowClass ).mouseenter( mouseEnter).mouseleave( mouseExit );
            },


            showEntryMenu: function(event, entryId) {
                var entry = this.entryList.getEntry(entryId);
                var newMenu = "";
                var get = this.getGet();
                for(var i=0;i<this.displayManager.displayTypes.length;i++) {
                    var type = this.displayManager.displayTypes[i];
                    if(!type.requiresData) continue;
                    newMenu+= htmlUtil.tag("li",[], htmlUtil.tag("a", ["onclick", get+".createDisplay('" + entry.getId() +"','" + type.type+"');"], type.label));
                }

                var menu = htmlUtil.tag("ul", ["id", this.getDomId(ID_MENU_INNER+entry.getId()),"class", "sf-menu"], 
                                        htmlUtil.tag("li",[],"<a>New</a>" + htmlUtil.tag("ul",[], newMenu)));
                                        

                $("#" + this.getDomId(ID_MENU_OUTER)).html(menu);
                showPopup(event, this.getDomId(ID_MENU_BUTTON+entry.getId()), this.getDomId(ID_MENU_OUTER), false,null,"left bottom");
                $("#"+  this.getDomId(ID_MENU_INNER+entry.getId())).superfish({
                        animation: {height:'show'},
                            delay: 1200
                            });
            }
        });
}

function RamaddaEntrydisplayDisplay(displayManager, id, properties) {
    $.extend(this, new RamaddaDisplay(displayManager, id, DISPLAY_ENTRYDISPLAY, properties));
    addRamaddaDisplay(this);
    $.extend(this, {
            selectedEntry: null,
            initDisplay: function() {
                this.initUI();
                this.setTitle("Entry Display");
                this.addEntryHtml(this.selectedEntry);
            },
            handleEntrySelection: function(source, entry, selected) {
                if(!selected) {
                    if(this.selectedEntry != entry) {
                        //not mine
                        return;
                    }
                    this.selectedEntry = null;
                    this.setContents("");
                    return;
                }
                this.selectedEntry = entry;
                this.addEntryHtml(this.selectedEntry);
            },
            addEntryHtml: function(entry) {
                if(entry==null) {
                    this.setContents("");
                    return;
                }

                var html = "";
                html += htmlUtil.div(["class", "wiki-h2"], entry.getLink(entry.getIconImage() +" " + entry.getName()));
                html+= entry.getDescription();
                html += htmlUtil.formTable();
                var columnNames = entry.getColumnNames();
                var columnLabels = entry.getColumnLabels();
                if(entry.getFilesize()>0) {
                    html+= htmlUtil.formEntry("File:", entry.getFilename() +" " +
                                              htmlUtil.href(entry.getFileUrl(), htmlUtil.image(root +"/icons/download.png")) + " " +
                                              entry.getFormattedFilesize());
                }
                for(var i in columnNames) {
                    var columnName = columnNames[i];
                    var columnLabel = columnLabels[i];
                    var columnValue = entry.getColumnValue(columnName);
                    html+= htmlUtil.formEntry(columnLabel+":", columnValue);
                }

                html += htmlUtil.closeTag("table");
                this.setContents(html);
            },
        });
}





function RamaddaOperandsDisplay(displayManager, id, properties) {
    var ID_SELECT = "select";
    var ID_SELECT1 = "select1";
    var ID_SELECT2 = "select2";
    var ID_NEWDISPLAY = "newdisplay";


    $.extend(this, new RamaddaEntryDisplay(displayManager, id, DISPLAY_OPERANDS, properties));
    addRamaddaDisplay(this);
    $.extend(this, {
            initDisplay: function() {
                this.initUI();
                var jsonUrl = getEntryManager().getSearchUrl(this.settings, OUTPUT_JSON);
                this.entryList = new EntryList(jsonUrl, this);
                var html = "";
                html += htmlUtil.div(["id",this.getDomId(ID_ENTRIES),"class","display-entrylist-entries"], "");
                this.setContents(html);
            },
            entryListChanged: function(entryList) {
                var html = "<form>";
                html += "<p>";
                html += htmlUtil.openTag("table",["class","formtable","cellspacing","0","cellspacing","0"]);
                var entries = this.entryList.getEntries();
                var get = this.getGet();

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
                $("#"+this.getDomId(ID_ENTRIES)).html(html);
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
            }

        });
}



