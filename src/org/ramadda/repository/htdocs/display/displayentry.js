
var DISPLAY_ENTRYLIST = "entrylist";
var DISPLAY_OPERANDS = "operands";


addGlobalDisplayType({type: DISPLAY_ENTRYLIST, label:"Entry List"});
addGlobalDisplayType({type: DISPLAY_OPERANDS, label:"Operands"});

function RamaddaEntryDisplay(displayManager, id, type, properties) {
     $.extend(this, new RamaddaDisplay(displayManager, id, type, properties));
     $.extend(this, {
             settings: new EntrySearchSettings({
                     type: properties.entryType,
                     parent: properties.entryParent,
             }),
             entryList: null,
             entryMap: {},
             getSettings: function() {
                 return this.settings;
             },
            initDisplay: function() {
                this.initUI();
                var jsonUrl = getEntryManager().getSearchUrl("json",this.settings);
                console.log("json:" + jsonUrl);
                this.entryList = new EntryList(jsonUrl, this);
                this.setContents("<p>Loading...<p>");
            }
        });
}


function RamaddaEntrylistDisplay(displayManager, id, properties) {

    $.extend(this, new RamaddaEntryDisplay(displayManager, id, DISPLAY_ENTRYLIST, properties));
    addRamaddaDisplay(this);
    $.extend(this, {
            selectedEntries: [],            
            handleEntrySelection: function(source, entry, selected) {
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
                //todo: what to do on a change?
            },
            entryListChanged: function(entryList) {
                this.entryList = entryList;
                var html = "";
                html += htmlUtil.openTag("ol",["class","display-entrylist-list", "id",this.getDomId("list")]);
                html  += "\n";
                var entries = this.entryList.getEntries();
                for(var i=0;i<entries.length;i++) {
                    var entry = entries[i];
                    var label = entry.getIconImage() +" " + entry.getName();
                    var right ="";
                    var hasLocation = entry.hasLocation();
                    if(hasLocation) {
                        right += htmlUtil.image(root+"/icons/map.png",["title","Location:" +entry.getLocationLabel()]);
                    }

                    var line = htmlUtil.leftRight(label,right);
                    html  += htmlUtil.tag("li",["id",
                                                this.getDomId("entry_" + entry.getId()),
                                                "entryid",entry.getId(), "class","ui-widget-content"], line);
                    html  += "\n";
                }
                html += htmlUtil.closeTag("ol");
                this.setContents(html);
                var theDisplay   =this;
                $("#" + this.getDomId("list")).selectable({
                        selected: function( event, ui ) {
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

            }
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
                var jsonUrl = getEntryManager().getSearchUrl("json",this.settings);
                this.entryList = new EntryList(jsonUrl, this);
                this.setContents("<p>Loading<p>");
            },
            entryListChanged: function(entryList) {
                var html = "<form>";
                html += "<p>";
                html += htmlUtil.openTag("table",["class","formtable","cellspacing","0","cellspacing","0"]);
                var entries = this.entryList.getEntries();
                var get = "getRamaddaDisplay('" + this.id +"')";
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
                this.setContents(html);
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



