
var DISPLAY_ENTRYLIST = "entrylist";
var DISPLAY_ENTRYDISPLAY = "entrydisplay";
var DISPLAY_OPERANDS = "operands";
var DISPLAY_METADATA = "metadata";


var ID_RESULTS = "results";
var ID_ENTRIES = "entries";

addGlobalDisplayType({type: DISPLAY_ENTRYLIST, label:"Entry List",requiresData:false});
addGlobalDisplayType({type: DISPLAY_ENTRYDISPLAY, label:"Entry Display",requiresData:false});
addGlobalDisplayType({type: DISPLAY_OPERANDS, label:"Operands",requiresData:false});
addGlobalDisplayType({type: DISPLAY_METADATA, label:"Metadata Table",requiresData:false});


function RamaddaEntryDisplay(displayManager, id, type, properties) {
     var SUPER;
     var ID_TOOLBAR = "toolbar";
     var ID_TOOLBAR_INNER = "toolbarinner";
     RamaddaUtil.inherit(this, SUPER = new RamaddaDisplay(displayManager, id, type, properties));
     RamaddaUtil.defineMembers(this, {
             searchSettings: new EntrySearchSettings({
                         parent: properties.entryParent,
                         text: properties.entryText,
                         entryType: properties.entryType,
             }),
             entryList: null,
             entryMap: {},
             getSearchSettings: function() {
                 return this.searchSettings;
             },
            getEntries: function() {
                if(this.entryList == null) return [];
                return  this.entryList.getEntries();
            },
             makeEntryToolbar: function(entry) {
                 var get = this.getGet();
                 var toolbarItems = [];
                 toolbarItems.push(HtmlUtil.tag("a", ["href", entry.getEntryUrl(),"target","_"], 
                                                HtmlUtil.image(root +"/icons/application-home.png",["border",0,"title","View Entry"])));
                 if(entry.getService("points.latlonaltcsv")) {
                     toolbarItems.push(HtmlUtil.tag("a", ["onclick", get+".createDisplay('" + entry.getId() +"','linechart');"], 
                                                    HtmlUtil.image(root +"/icons/chart_line_add.png",["border",0,"title","Create Chart"])));
                 }
                 if(entry.getFilesize()>0) {
                     toolbarItems.push(HtmlUtil.tag("a", ["href", entry.getFileUrl()], 
                                                    HtmlUtil.image(root +"/icons/download.png",["border",0,"title","Download (" + entry.getFormattedFilesize() +")"])));
                     
                 }
                 var entryMenuButton = this.getEntryMenuButton(entry);
                 entryMenuButton =  HtmlUtil.onClick(this.getGet()+".showEntryPopup(event, '" + entry.getId() +"');", 
                                               HtmlUtil.image(root+"/icons/downdart.png", 
                                                              ["class", "display-dialog-button", "id",  this.getDomId(ID_MENU_BUTTON + entry.getId())]));



                 toolbarItems.push(entryMenuButton);
                 return HtmlUtil.div(["class","display-entry-toolbar","id",
                                      this.getEntryToolbarId(entry.getId())],
                                     HtmlUtil.join(toolbarItems,""));
             },
             getEntryToolbarId: function(entryId) {
                 return this.getDomId(ID_TOOLBAR +"_" + entryId);
             }

        });
     if(properties.entryType!=null) {
         this.searchSettings.addType(properties.entryType);
     }
}


function RamaddaSearcher(displayManager, id, type, properties) {
    var NONE = "-- None --";
    var ID_TEXT_FIELD = "textfield";
    var ID_TYPE_FIELD = "typefield";
    var ID_TYPE_FIELDS = "typefields";
    var ID_METADATA_FIELD = "metadatafield";
    var ID_SEARCH = "search";
    var ID_FORM = "form";
    var ID_COLUMN = "column";

    $.extend(this, {
            showForm: true,            
            showType: true,           
            formOpen: true,
            fullForm: true,            
            showMetadata: true,
            showArea: true,
            types: null,
            metadataTypeList: [],
    });            

    var SUPER;
    RamaddaUtil.inherit(this, SUPER = new RamaddaEntryDisplay(displayManager, id, type, properties));

    var metadataTypesAttr= this.getProperty("metadataTypes","enum_tag:Tag");
    //look for type:value:label, or type:label,
    var toks  = metadataTypesAttr.split(",");
    for(var i=0;i<toks.length;i++) {
        var type = toks[i];
        var label = type;
        var value = null;
        var subToks  = type.split(":");
        if(subToks.length>1) {
            type = subToks[0];
            if(subToks.length>=3) {
                value = subToks[1];
                label  = subToks[2];
            } else {
                label  = subToks[1];
            }
        }
        this.metadataTypeList.push(new MetadataType(type, label,value));
    }

    RamaddaUtil.defineMembers(this, {
            haveSearched: false,
            haveTypes: false,
            metadata: {},
            metadataLoading: {},
            initDisplay: function() {
                var theDisplay  = this;
                this.jq(ID_SEARCH).button().click(function(event) {
                        theDisplay.submitSearchForm();
                        event.preventDefault();
                    });

                this.jq(ID_TEXT_FIELD).autocomplete({
                        source: function(request, callback) {
                            theDisplay.doQuickEntrySearch(request, callback);
                            }
                            });

                this.jq(ID_FORM).submit(function( event ) {
                        theDisplay.submitSearchForm();
                        event.preventDefault();
                    });

                this.addTypes(this.types);
                for(var i=0;i<this.metadataTypeList.length;i++) {
                    var type  = this.metadataTypeList[i];
                    this.addMetadata(type, null);
                }
                if(!this.haveSearched) {
                    this.submitSearchForm();
                }
            },
            submitSearchForm: function() {
                this.haveSearched = true;
                this.searchSettings.text = this.getFieldValue(this.getDomId(ID_TEXT_FIELD), this.searchSettings.text);
                if(this.haveTypes) {
                    this.searchSettings.entryType  = this.getFieldValue(this.getDomId(ID_TYPE_FIELD), this.searchSettings.entryType);
                }
                this.searchSettings.clearAndAddType(this.searchSettings.entryType);
                
                if(this.areaWidget) {
                    this.areaWidget.setSearchSettings(this.searchSettings);
                }
                if(this.dateRangeWidget) {
                    this.dateRangeWidget.setSearchSettings(this.searchSettings);
                }
                this.searchSettings.metadata = [];
                for(var i=0;i<this.metadataTypeList.length;i++) {
                    var metadataType  = this.metadataTypeList[i];
                    var value = metadataType.getValue();
                    if(value == null) {
                        value = this.getFieldValue(this.getMetadataFieldId(metadataType), null);
                    }
                    if(value!=null) {
                        this.searchSettings.metadata.push({type:metadataType.getType(),value:value});
                    }
                }

                //Call this now because it sets settings
                var jsonUrl = this.makeSearchUrl();
                this.entryList = new EntryList(jsonUrl, this, this.entryList);
                this.updateForSearching(jsonUrl);
            },
            updateForSearching: function(jsonUrl) {
                var outputs = getEntryManager().getSearchLinks(this.searchSettings);
                this.footerRight  = "Links: " + HtmlUtil.join(outputs," - "); 
                this.writeHtml(ID_FOOTER_RIGHT, this.footerRight);
                this.writeHtml(ID_RESULTS, "Searching...");
            },
            prepareToLayout:function() {
                SUPER.prepareToLayout.apply(this);
                this.savedValues = {};
                var cols  = this.getSearchableColumns();
                for(var i =0;i<cols.length;i++) {
                    var col = cols[i];
                    var id = this.getDomId(ID_COLUMN+col.getName());
                    var value = $("#" + id).val();
                    if(value == null || value.length == 0) continue;
                    this.savedValues[id] = value;
                }
            },
            makeSearchUrl: function() {
                var extra = "";
                var cols  = this.getSearchableColumns();
                for(var i =0;i<cols.length;i++) {
                    var col = cols[i];
                    var value = this.jq(ID_COLUMN+col.getName()).val();
                    if(value == null || value.length == 0)continue;
                    extra+= "&" + col.getSearchArg() +"=" + encodeURI(value);
                }
                this.searchSettings.setExtra(extra);
                var jsonUrl = getEntryManager().getSearchUrl(this.searchSettings, OUTPUT_JSON);
                return jsonUrl;
            },
            makeSearchForm: function() {
                var form =  HtmlUtil.openTag("form",["id",this.getDomId(ID_FORM),"action","#"]);
                var extra = "";
                var text = this.searchSettings.text;
                if(text == null) text = "";

                var textField =  HtmlUtil.input("", text, ["placeholder","search text","class", "display-search-input ui-widget ui-button-text", "size","20","id",  this.getDomId(ID_TEXT_FIELD)]);

                form += HtmlUtil.div(["id", this.getDomId(ID_SEARCH),"class","display-button"],"Search for:");
                if(this.showType) {
                    form += "&nbsp;&nbsp;";
                    form += HtmlUtil.tag("select",["id", this.getDomId(ID_TYPE_FIELD),
                                                   "class","display-typelist",
                                                   "onchange", this.getGet()+".typeChanged();"],
                                                 HtmlUtil.tag("option",["title","","value",""],
                                                              " Choose Type "));
                } 
                form += "&nbsp;&nbsp;";
                form += textField;

                var extra = "";

                extra+= HtmlUtil.formTable();
                if(this.showArea) {
                    this.areaWidget = new AreaWidget(this);
                    extra += HtmlUtil.formEntry("Area:",this.areaWidget.getHtml());
                }

                this.dateRangeWidget  = new DateRangeWidget(this);
                extra += HtmlUtil.formEntry("Date Range:",this.dateRangeWidget.getHtml());

                if(this.showMetadata) {
                    for(var i =0;i<this.metadataTypeList.length;i++) {
                        var type  = this.metadataTypeList[i];
                        var value = type.getValue();
                        var metadataSelect;
                        if(value!=null) {
                            metadataSelect= value;
                        } else {
                            metadataSelect= HtmlUtil.tag("select",["id", this.getMetadataFieldId(type),
                                                                   "class","display-metadatalist"],
                                                         HtmlUtil.tag("option",["title","","value",""],
                                                                      NONE));
                        }
                        extra+= HtmlUtil.formEntry(type.getLabel() +":", metadataSelect);
                    }
                }
                extra+= HtmlUtil.closeTag("table");
                extra+=    HtmlUtil.div(["id",this.getDomId(ID_TYPE_FIELDS)],"");

                form += HtmlUtil.div(["class", "display-search-extra"],
                                     HtmlUtil.toggleBlock("Search Settings", HtmlUtil.div(["class", "display-search-extra-inner"], extra), this.formOpen));
                //Hide the real submit button
                form += "<input type=\"submit\" style=\"position:absolute;left:-9999px;width:1px;height:1px;\"/>";
                form += HtmlUtil.closeTag("form");

                return form;

            },
            handleEventMapBoundsChanged: function (source,  bounds) {
                if(this.areaWidget) this.areaWidget.handleEventMapBoundsChanged (source,  bounds);
            },
            typeChanged: function() {
                this.searchSettings.skip=0;
                this.searchSettings.max=50;
                this.searchSettings.entryType  = this.getFieldValue(this.getDomId(ID_TYPE_FIELD), this.searchSettings.entryType);
                this.searchSettings.clearAndAddType(this.searchSettings.entryType);
                this.addExtraForm();
                this.submitSearchForm();
            },
            addMetadata: function(metadataType, metadata) {
                if(metadata == null) {
                    metadata = this.metadata[metadataType.getType()];
                }
                if(metadata == null) {
                    var theDisplay = this;
                    if(!this.metadataLoading[metadataType.getType()]) {
                        this.metadataLoading[metadataType.getType()] = true;
                        metadata = getEntryManager().getMetadataCount(metadataType, function(metadataType, metadata) {theDisplay.addMetadata(metadataType, metadata);});
                    }
                }
                if(metadata == null) {
                    return;
                }

                this.metadata[metadataType.getType()] = metadata;


                var select = HtmlUtil.tag("option",["title","","value",""],NONE);
                for(var i =0;i<metadata.length;i++) {
                    var count = metadata[i].count;
                    var value = metadata[i].value;
                    var optionAttrs  = ["value",value,"class", "display-metadata-item"];
                    var selected =  false;
                    if(selected) {
                        optionAttrs.push("selected");
                        optionAttrs.push(null);
                    }
                    select +=  HtmlUtil.tag("option",optionAttrs, value +" (" + count +")");
                }
                $("#" + this.getMetadataFieldId(metadataType)).html(select);
            },
                
                
            getMetadataFieldId: function(metadataType) {
                var id = metadataType.getType();
                id = id.replace(".","_");
                return this.getDomId(ID_METADATA_FIELD +id);
            },

            addTypes: function(types) {
                if(types == null) {
                    var theDisplay = this;
                    types = getEntryManager().getEntryTypes(function(types) {theDisplay.addTypes(types);});
                }
                if(types == null) {
                    return;
                }
                this.types = types;
                this.haveTypes = true;
                var cats =[];
                var catMap = {}; 
                var select = HtmlUtil.tag("option",["title","","value",""],"Any Type");
                for(var i = 0;i< types.length;i++) {
                    var type = types[i];
                    var map = catMap[type.getCategory()];
                    //                    var style = " background: URL(" + type.getIcon() +") no-repeat;";
                    
                    var optionAttrs  = ["title",type.getLabel(),"value",type.getId(),"class", "display-typelist-type",
                                        //                                        "style", style,
                                        "data-iconurl",type.getIcon()];
                    var selected =  this.searchSettings.hasType(type.getId());
                    if(selected) {
                        optionAttrs.push("selected");
                        optionAttrs.push(null);
                    }
                    var option = HtmlUtil.tag("option",optionAttrs, type.getLabel() +" (" + type.getEntryCount() +")");
                    if(map == null) {
                        catMap[type.getCategory()] = HtmlUtil.tag("option",["class", "display-typelist-category", "title","","value",""],type.getCategory());
                        cats.push(type.getCategory());
                    }
                    catMap[type.getCategory()] += option;

                }
                for(var i in cats) {
                    select += catMap[cats[i]];
                }
                this.writeHtml(ID_TYPE_FIELD, select);
                this.jq(ID_TYPE_FIELD).selectBoxIt({});
                this.addExtraForm();
           },
           getSelectedType: function() {
                if(this.types == null) return null;
                for(var i = 0;i< this.types.length;i++) {
                    var type = this.types[i];
                    if(this.searchSettings.hasType(type.getId())) {
                        return type;
                    }
                }
                return null;
            },
            getSearchableColumns: function() {
                var searchable = [];
                var type = this.getSelectedType();
                if(type==null) {
                    return searchable;
                }
                var cols = type.getColumns();
                if(cols == null) {
                    return searchable;
                }
                for(var i = 0;i< cols.length;i++) {
                    var col = cols[i];
                    if(!col.getCanSearch()) continue;
                    searchable.push(col);
                }
                return searchable;
           },
           addExtraForm: function() {
                if(this.savedValues == null) this.savedValues = {};
                var extra   = "";
                var cols = this.getSearchableColumns();
                for(var i = 0;i< cols.length;i++) {
                    var col = cols[i];
                    if(extra.length==0) {
                        extra+=HtmlUtil.formTable();
                    }
                    var field  ="";
                    var id = this.getDomId(ID_COLUMN+col.getName());
                    var savedValue  = this.savedValues[id];
                    if(savedValue == null) savedValue = "";
                    if(col.isEnumeration()) {
                        field  = HtmlUtil.openTag("select",["id", id]);
                        field += HtmlUtil.tag("option",["title","","value",""],
                                              "-- Select --");
                        var values = col.getValues();
                        for(var vidx in values) {
                            var value = values[vidx].value;
                            var label = values[vidx].label;
                            var extraAttr = "";
                            if(value == savedValue) {
                                extraAttr =  " selected ";
                            }
                            field += HtmlUtil.tag("option",["title",label,"value",value, extraAttr,  null],
                                                  label);
                        }
                        field  += HtmlUtil.closeTag("select");
                    } else {
                        field = HtmlUtil.input("", savedValue, ["class","input", "size","15","id",  id]);
                    }
                    extra+= HtmlUtil.formEntry(col.getLabel() +":" ,field + " " + col.getSuffix());

                }
                if(extra.length>0) {
                    extra+=HtmlUtil.closeTag("table");
                }
                
                this.writeHtml(ID_TYPE_FIELDS, extra);

                
           },
            getEntries: function() {
                if(this.entryList == null) return [];
                return  this.entryList.getEntries();
            },
            loadNextUrl: function() {
                this.searchSettings.skip+= this.searchSettings.max;
                this.submitSearchForm();
            },
            loadMore: function() {
                this.searchSettings.max = this.searchSettings.max+=50;
                this.submitSearchForm();
            },
            loadLess: function() {
                var max = this.searchSettings.max;
                max = parseInt(0.75*max);
                this.searchSettings.max = Math.max(1, max);
                this.submitSearchForm();
            },
            loadPrevUrl: function() {
                this.searchSettings.skip = Math.max(0, this.searchSettings.skip-this.searchSettings.max);
                this.submitSearchForm();
            },
            entryListChanged: function(entryList) {
                this.entryList = entryList;
            }
        });
}




function RamaddaEntrylistDisplay(displayManager, id, properties) {
    var ID_LIST = "list";
    var ID_DETAILS = "details";

    $.extend(this, {
            showForm: true,            
            showEntries: true,
    });            

    var SUPER;
    RamaddaUtil.inherit(this, SUPER = new RamaddaSearcher(displayManager, id, DISPLAY_ENTRYLIST, properties));
    addRamaddaDisplay(this);

    RamaddaUtil.defineMembers(this, {
            haveDisplayed: false,
            selectedEntries: [],            
            getSelectedEntries: function() {return this.selectedEntries;},
            initDisplay: function() {
                if(this.getIsLayoutFixed() && this.haveDisplayed) {
                    return;
                }
                this.haveDisplayed =true;
                this.initUI();
                var html = "";
                var horizontal = this.isLayoutHorizontal();
                var footer =  this.getFooter();
                var entriesDivAttrs = ["id",this.getDomId(ID_ENTRIES),"class","display-entrylist-entries"];
                var innerHeight = this.getProperty("innerHeight",null);
                if(innerHeight!=null) {
                    entriesDivAttrs.push("style");
                    entriesDivAttrs.push("margin: 0px; padding: 0px;  min-height:" + innerHeight +"px; max-height:" + innerHeight +"px; overflow-y: none;");
                }
                var entriesDiv = 
                    HtmlUtil.div(["class","display-entrylist-results", "id",this.getDomId(ID_RESULTS)],"&nbsp;") +
                    HtmlUtil.div(entriesDivAttrs, this.getLoadingMessage());
                
                html += HtmlUtil.div(["class","display-entry-popup", "id",this.getDomId(ID_DETAILS)],"&nbsp;");


                if(horizontal) {
                    html+= HtmlUtil.openTag("table",["border","0", "width","100%", "cellpadding","0","cellpadding","5"]);
                    html += HtmlUtil.openTag("tr",["valign","top"]);
                    if(this.showForm) {
                        html += HtmlUtil.tag("td",[],this.makeSearchForm());
                    }
                    if(this.showEntries) {
                        html += HtmlUtil.tag("td",["width","60%"],entriesDiv);
                    }
                    html += HtmlUtil.closeTag("tr");

                    html += HtmlUtil.openTag("tr",["valign","top"]);
                    if(this.showForm) {
                        html += HtmlUtil.tag("td",[],"");
                    }
                    if(this.showEntries) {
                        html += HtmlUtil.tag("td",[],footer);
                    }
                    html += HtmlUtil.closeTag("tr");
                    html += HtmlUtil.closeTag("table");
                } else {
                    if(this.showForm) {
                        html += this.makeSearchForm();
                    }
                    if(this.showEntries) {
                        html += entriesDiv;
                        html += footer;
                    }
                }

                this.setContents(html);

                if(this.dateRangeWidget) this.dateRangeWidget.initHtml();


                SUPER.initDisplay.apply(this);
                if(this.entryList!=null && this.entryList.haveLoaded) {
                    this.entryListChanged(this.entryList);
                }
            },
            updateForSearching: function(jsonUrl) {
                SUPER.updateForSearching.apply(this,[jsonUrl]);
                this.writeHtml(ID_ENTRIES, HtmlUtil.div(["style","margin:20px;"], this.getWaitImage()));
            },
            highlightEntry: function(entry) {
                this.jq("entryinner_" + entry.getId()).addClass("display-entrylist-highlight");
            },
            handleEventEntrySelection: function(source, args) {
                this.selectEntry(args.entry, args.selected);
            },
            selectEntry: function(entry, selected) {
                var changed  = false;
                if(selected) {
                    this.jq("entry_" + entry.getId()).addClass("ui-selected");
                    var index = this.selectedEntries.indexOf(entry);
                    if (index < 0) {
                        this.selectedEntries.push(entry);
                        changed = true;
                    }
                } else {
                    this.jq("entry_" + entry.getId()).removeClass("ui-selected");
                    var index = this.selectedEntries.indexOf(entry);
                    if (index >= 0) {
                        this.selectedEntries.splice(index, 1);
                        changed = true;
                    }
                }
            },

            hideEntryPopup: function() {
                var popupId = "#"+ this.getDomId(ID_DETAILS);
                $(popupId).hide();
                this.currentPopupEntry = null;
            },
            showEntryPopup: function(event, entryId) {
                var entry = this.entryList.getEntry(entryId);
                var popupId = "#"+ this.getDomId(ID_DETAILS);
                if(this.currentPopupEntry ==  entry) {
                    this.hideEntryPopup();
                    return;
                }
                this.currentPopupEntry = entry;
                var src =  this.getDomId("entry_" + entry.getId());
                $(popupId).html(this.getEntryHtml(entry));
                $(popupId).show();
                $(popupId).position({
                        of: jQuery( "#" +src),
                            my: 'left top',
                            at: 'left bottom',
                            collision: "none none"
                            });
            },
            entryListChanged: function(entryList) {
                SUPER.entryListChanged.apply(this,[entryList]);
                var rowClass = "entryrow_" + this.getId()
                var entries = this.entryList.getEntries();
                var html = "";
                if(entries.length==0) {
                    this.searchSettings.skip=0;
                    this.searchSettings.max=50;
                    this.writeHtml(ID_ENTRIES, this.getMessage("Nothing found"));
                    this.writeHtml(ID_FOOTER_LEFT,"");
                    this.writeHtml(ID_RESULTS,"&nbsp;");
                    this.getDisplayManager().handleEventEntriesChanged(this, []);
                    return;
                }
                var left = "Showing " + (this.searchSettings.skip+1) +"-" +(this.searchSettings.skip+Math.min(this.searchSettings.max, entries.length));
                var right = [];
                if(this.searchSettings.skip>0) {
                    right.push(HtmlUtil.onClick(this.getGet()+".loadPrevUrl();", "Previous",["class","display-link"]));
                }
                var addMore = false;
                if(entries.length == this.searchSettings.getMax()) {
                    right.push(HtmlUtil.onClick(this.getGet()+".loadNextUrl();", "Next",["class","display-link"]));
                    addMore = true;
                }
                right.push(HtmlUtil.onClick(this.getGet()+".loadLess();", HtmlUtil.image(root +"/icons/minus-small-white.png","View less"["border","0"]),["class","display-link"]));
                if(addMore) {
                    right.push(HtmlUtil.onClick(this.getGet()+".loadMore();", HtmlUtil.image(root +"/icons/plus-small-white.png","View more"["border","0"]),["class","display-link"]));
                }
                var results = "";
                if(right.length>0)
                    results = HtmlUtil.leftRight(left, HtmlUtil.join(right,  ""));
                else
                    results  =left;
                this.writeHtml(ID_RESULTS, results);

                html += HtmlUtil.openTag("ol",["class","display-entrylist-list", "id",this.getDomId(ID_LIST)]);
                html  += "\n";
                var get = this.getGet();
                this.writeHtml(ID_FOOTER_LEFT,"");
                if(this.footerRight!=null) {
                    this.writeHtml(ID_FOOTER_RIGHT, this.footerRight);
                }


                for(var i=0;i<entries.length;i++) {
                    var entry = entries[i];
                    right = this.makeEntryToolbar(entry);
                    var icon = entry.getIconImage(["title","View entry"]);
                    var link  =  HtmlUtil.tag("a",["href", entry.getEntryUrl()],icon);
                    var entryName = entry.getName();
                    if(entryName.length>100) {
                        entryName = entryName.substring(0,99)+"...";
                    }
                    var left =  HtmlUtil.div(["style"," white-space: nowrap;  overflow-x:none; max-width:300px;"],link +" " +  entryName);

                    var line = HtmlUtil.div(["id",this.getDomId("entryinner_" + entry.getId())], HtmlUtil.leftRight(left,right,"60%","30%"));
                    html  += HtmlUtil.tag("li",["id",
                                                this.getDomId("entry_" + entry.getId()),
                                                "entryid",entry.getId(), "class","display-entrylist-entry ui-widget-content " + rowClass], line);
                    html  += "\n";
                }
                html += HtmlUtil.closeTag("ol");

                this.writeHtml(ID_ENTRIES, html);
                var theDisplay   =this;
                var entryRows = $("#" + this.getDomId(ID_LIST) +"  .display-entrylist-entry");
                entryRows.mouseover(function(event){
                        var entryId = $( this ).attr('entryid');
                        var toolbarId = theDisplay.getEntryToolbarId(entryId);
                        var toolbar = $("#" + toolbarId);
                        toolbar.show();
                        var myalign = 'right center';
                        var atalign = 'right center';
                        var srcId = theDisplay.getDomId("entry_" + entryId);
                        toolbar.position({
                                of: $( "#" +srcId ),
                                    my: myalign,
                                    at: atalign,
                                    collision: "none none"
                                    });

                    });
                entryRows.mouseout(function(event){
                        var entryId = $( this ).attr('entryid');
                        var toolbarId = theDisplay.getEntryToolbarId(entryId);
                        var toolbar = $("#" + toolbarId);
                        toolbar.hide();
                    });

                this.jq(ID_LIST).selectable({
                        delay: 0,
                        cancel: 'a',
                        selected: function( event, ui ) {
                            theDisplay.hideEntryPopup();
                            var entryId = ui.selected.getAttribute('entryid');
                            var entry = theDisplay.entryList.getEntry(entryId);
                            //                            console.log("selected:" +  entry);
                            if(entry == null) return;

                            var zoom = null;
                            if(event.shiftKey) {
                                zoom = {zoomIn:true};
                            }
                            theDisplay.selectedEntries.push(entry);
                            theDisplay.getDisplayManager().handleEventEntrySelection(theDisplay, {entry:entry, selected:true, zoom:zoom});
                            theDisplay.lastSelectedEntry = entry;
                        },
                        unselected: function( event, ui ) {
                            var entryId = ui.unselected.getAttribute('entryid');
                            var entry = theDisplay.entryList.getEntry(entryId);
                            var index = theDisplay.selectedEntries.indexOf(entry);
                            //                            console.log("remove:" +  index + " " + theDisplay.selectedEntries);
                            if (index > -1) {
                                theDisplay.selectedEntries.splice(index, 1);
                                theDisplay.getDisplayManager().handleEventEntrySelection(theDisplay, {entry:entry, selected:false});
                            }
                        },
                            
                    });

                this.getDisplayManager().handleEventEntriesChanged(this, entries);
            }
        });
}








function RamaddaEntrydisplayDisplay(displayManager, id, properties) {
    var SUPER;
    $.extend(this, {
            sourceEntry: properties.sourceEntry});
    RamaddaUtil.inherit(this, SUPER = new RamaddaDisplay(displayManager, id, DISPLAY_ENTRYDISPLAY, properties));
    addRamaddaDisplay(this);
    $.extend(this, {
            selectedEntry: null,
            initDisplay: function() {
                this.initUI();
                if(this.sourceEntry!=null) {
                    this.addEntryHtml(this.sourceEntry);
                } else {
                    this.addEntryHtml(this.selectedEntry);
                }
                this.setTitle("Entry Display");
            },
            handleEventEntrySelection: function(source, args) {
                //Ignore select events
                if(this.sourceEntry !=null) return;
                var selected = args.selected;
                var entry = args.entry;
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
                    this.setContents("&nbsp;");
                    return;
                }
                this.setContents(this.getEntryHtml(entry));
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
            baseUrl: null,
            initDisplay: function() {
                this.initUI();
                this.baseUrl = getEntryManager().getSearchUrl(this.searchSettings, OUTPUT_JSON);
                this.entryList = new EntryList(jsonUrl, this, this.entryList);
                var html = "";
                html += HtmlUtil.div(["id",this.getDomId(ID_ENTRIES),"class","display-entrylist-entries"], "");
                this.setContents(html);
            },
            entryListChanged: function(entryList) {
                var html = "<form>";
                html += "<p>";
                html += HtmlUtil.openTag("table",["class","formtable","cellspacing","0","cellspacing","0"]);
                var entries = this.entryList.getEntries();
                var get = this.getGet();

                for(var j=1;j<=2;j++) {
                    var select= HtmlUtil.openTag("select",["id", this.getDomId(ID_SELECT +j)]);
                    select += HtmlUtil.tag("option",["title","","value",""],
                                         "-- Select --");
                    for(var i=0;i<entries.length;i++) {
                        var entry = entries[i];
                        var label = entry.getIconImage() +" " + entry.getName();
                        select += HtmlUtil.tag("option",["title",entry.getName(),"value",entry.getId()],
                                             entry.getName());
                        
                    }
                    select += HtmlUtil.closeTag("select");
                    html += HtmlUtil.formEntry("Data:",select);
                }

                var select  = HtmlUtil.openTag("select",["id", this.getDomId(ID_CHARTTYPE)]);
                select += HtmlUtil.tag("option",["title","","value","linechart"],
                                     "Line chart");
                select += HtmlUtil.tag("option",["title","","value","barchart"],
                                     "Bar chart");
                select += HtmlUtil.closeTag("select");
                html += HtmlUtil.formEntry("Chart Type:",select);

                html += HtmlUtil.closeTag("table");
                html += "<p>";
                html +=  HtmlUtil.tag("div", ["class", "display-button", "id",  this.getDomId(ID_NEWDISPLAY)],"New Chart");
                html += "<p>";
                html += "</form>";
                this.writeHtml(ID_ENTRIES, html);
                var theDisplay = this;
                this.jq(ID_NEWDISPLAY).button().click(function(event) {
                       theDisplay.createDisplay();
                   });
            },
            createDisplay: function() {
                var entry1 = this.entryList.getEntry(this.jq(ID_SELECT1).val());
                var entry2 = this.entryList.getEntry(this.jq(ID_SELECT2).val());
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
                var chartType = this.jq(ID_CHARTTYPE).val();
                displayManager.createDisplay(chartType, {
                        "layoutFixed": false,
                        "data": pointData
                   });
            }

        });
}



function RamaddaMetadataDisplay(displayManager, id, properties) {
    var ID_TABLE = "table";
    $.extend(this, {
            type: null,            
    });            
    var SUPER;
    RamaddaUtil.inherit(this, SUPER = new RamaddaSearcher(displayManager, id, DISPLAY_METADATA, properties));
    addRamaddaDisplay(this);
    RamaddaUtil.defineMembers(this, {
            initDisplay: function() {
                var html = HtmlUtil.div(["id", this.getDomId(ID_TABLE)],"Loading");
                this.setContents(html);
                var theDisplay  = this;
            },
            entryListChanged: function(entryList) {
                this.entryList = entryList;
                var rowClass = "entryrow_" + this.getId()
                var entries = this.entryList.getEntries();
                var html = "";
                if(entries.length==0) {
                    this.setHtml(ID_TABLE,"Nothing found");
                    return;
                }
                var html = HtmlUtil.openTag("table",["width","100%","cellpadding", "5"]);
                for(var i=0;i<entries.length;i++) {
                    var entry = entries[i];
                    html += HtmlUtil.tr([],
                                        HtmlUtil.td([],entry.getName()));
                }
                html += HtmlUtil.closeTag("table");
                this.setHtml(ID_TABLE,html);
            },
                });

}

