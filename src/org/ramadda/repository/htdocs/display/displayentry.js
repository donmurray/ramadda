
var DISPLAY_ENTRYLIST = "entrylist";
var DISPLAY_ENTRYDISPLAY = "entrydisplay";
var DISPLAY_OPERANDS = "operands";
var DISPLAY_METADATA = "metadata";
var DISPLAY_TIMELINE = "timeline";


var ID_RESULTS = "results";
var ID_ENTRIES = "entries";
var ID_DETAILS = "details";

addGlobalDisplayType({type: DISPLAY_ENTRYLIST, label:"Entry List",requiresData:false});
addGlobalDisplayType({type: DISPLAY_ENTRYDISPLAY, label:"Entry Display",requiresData:false});
//addGlobalDisplayType({type: DISPLAY_OPERANDS, label:"Operands",requiresData:false});
addGlobalDisplayType({type: DISPLAY_TIMELINE, label:"Timeline",requiresData:false});
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
                                                HtmlUtil.image(ramaddaBaseUrl +"/icons/application-home.png",["border",0,"title","View Entry"])));
                 if(entry.getService("points.latlonaltcsv")) {
                     toolbarItems.push(HtmlUtil.tag("a", ["onclick", get+".createDisplay('" + entry.getId() +"','linechart');"], 
                                                    HtmlUtil.image(ramaddaBaseUrl +"/icons/chart_line_add.png",["border",0,"title","Create Chart"])));
                 }
                 if(entry.getFilesize()>0) {
                     toolbarItems.push(HtmlUtil.tag("a", ["href", entry.getFileUrl()], 
                                                    HtmlUtil.image(ramaddaBaseUrl +"/icons/download.png",["border",0,"title","Download (" + entry.getFormattedFilesize() +")"])));
                     
                 }
                 var entryMenuButton = this.getEntryMenuButton(entry);
                 entryMenuButton =  HtmlUtil.onClick(this.getGet()+".showEntryPopup(event, '" + entry.getId() +"');", 
                                               HtmlUtil.image(ramaddaBaseUrl+"/icons/downdart.png", 
                                                              [ATTR_CLASS, "display-dialog-button", ATTR_ID,  this.getDomId(ID_MENU_BUTTON + entry.getId())]));



                 toolbarItems.push(entryMenuButton);
                 return HtmlUtil.div([ATTR_CLASS,"display-entry-toolbar",ATTR_ID,
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

    RamaddaUtil.initMembers(this, {
            showForm: true,            
            showEntries: true,
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
            getDefaultHtml: function() {
                var html = "";
                var horizontal = this.isLayoutHorizontal();
                var footer =  this.getFooter();
                if(!this.getProperty("showFooter", true)) {
                    footer = "";
                }
                displayDebug  =false;
                var entriesDivAttrs = [ATTR_ID,this.getDomId(ID_ENTRIES),ATTR_CLASS,"display-entries-content"];
                var innerHeight = this.getProperty("innerHeight",null);
                if(innerHeight!=null) {
                    entriesDivAttrs.push("style");
                    entriesDivAttrs.push("margin: 0px; padding: 0px;  min-height:" + innerHeight +"px; max-height:" + innerHeight +"px; overflow-y: none;");
                }
                var resultsDiv = "";
                if(this.getProperty("showHeader", true)) {
                    resultsDiv = HtmlUtil.div([ATTR_CLASS,"display-entries-results", ATTR_ID,this.getDomId(ID_RESULTS)],"&nbsp;"); 
                }

                var entriesDiv = 
                    resultsDiv +
                    HtmlUtil.div(entriesDivAttrs, this.getLoadingMessage());
                


                if(horizontal) {
                    html+= HtmlUtil.openTag(TAG_TABLE,["border","0", "width","100%", "cellpadding","0","cellpadding","5"]);
                    html += HtmlUtil.openTag(TAG_TR,["valign","top"]);
                    if(this.showForm) {
                        html += HtmlUtil.tag(TAG_TD,[],this.makeSearchForm());
                    }
                    if(this.showEntries) {
                        html += HtmlUtil.tag(TAG_TD,["width","60%"],entriesDiv);
                    }
                    html += HtmlUtil.closeTag(TAG_TR);

                    html += HtmlUtil.openTag(TAG_TR,["valign","top"]);
                    if(this.showForm) {
                        html += HtmlUtil.tag(TAG_TD,[],"");
                    }
                    if(this.showEntries) {
                        html += HtmlUtil.tag(TAG_TD,[],footer);
                    }
                    html += HtmlUtil.closeTag(TAG_TR);
                    html += HtmlUtil.closeTag(TAG_TABLE);
                } else {
                    if(this.showForm) {
                        html += this.makeSearchForm();
                    }
                    if(this.showEntries) {
                        html += entriesDiv;
                        html += footer;
                    }
                }
                html += HtmlUtil.div([ATTR_CLASS,"display-entry-popup", ATTR_ID,this.getDomId(ID_DETAILS)],"&nbsp;");
                return html;
            },
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
            hideEntryPopup: function() {
                var popupId = "#"+ this.getDomId(ID_DETAILS);
                $(popupId).hide();
                this.currentPopupEntry = null;
            },
             showEntryPopup: function(event, entryId, src,leftAlign) {
                var entry = this.entryList.getEntry(entryId);
                var popupId = "#"+ this.getDomId(ID_DETAILS);
                if(this.currentPopupEntry ==  entry) {
                    this.hideEntryPopup();
                    return;
                }
                var myloc = 'right top';
                var atloc = 'right bottom';
                if(leftAlign) {
                    myloc = 'left top';
                    atloc = 'left bottom';
                }
                this.currentPopupEntry = entry;
                if(src == null) src =  this.getDomId("entry_" + entry.getId());
                var close  = HtmlUtil.onClick(this.getGet()+ ".hideEntryPopup();",
                                              HtmlUtil.image(ramaddaBaseUrl +"/icons/close.gif"));
                
                var contents = this.getEntryHtml(entry, close);
                $(popupId).html(contents);
                $(popupId).show();
                $(popupId).position({
                        of: jQuery( "#" +src),
                            my: myloc,
                            at: atloc,
                            collision: "none none"
                            });
            },

             getResultsHeader: function(entries) {
                var left = "Showing " + (this.searchSettings.skip+1) +"-" +(this.searchSettings.skip+Math.min(this.searchSettings.max, entries.length));
                var right = [];
                if(this.searchSettings.skip>0) {
                    right.push(HtmlUtil.onClick(this.getGet()+".loadPrevUrl();", "Previous",[ATTR_CLASS,"display-link"]));
                }
                var addMore = false;
                if(entries.length == this.searchSettings.getMax()) {
                    right.push(HtmlUtil.onClick(this.getGet()+".loadNextUrl();", "Next",[ATTR_CLASS,"display-link"]));
                    addMore = true;
                }
                right.push(HtmlUtil.onClick(this.getGet()+".loadLess();", HtmlUtil.image(ramaddaBaseUrl +"/icons/minus-small-white.png","View less"["border","0"]),[ATTR_CLASS,"display-link"]));
                if(addMore) {
                    right.push(HtmlUtil.onClick(this.getGet()+".loadMore();", HtmlUtil.image(ramaddaBaseUrl +"/icons/plus-small-white.png","View more"["border","0"]),[ATTR_CLASS,"display-link"]));
                }
                var results = "";
                if(right.length>0)
                    results = HtmlUtil.leftRight(left, HtmlUtil.join(right,  ""));
                else
                    results  =left;
                return results;
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
                this.writeHtml(ID_ENTRIES, HtmlUtil.div(["style","margin:20px;"], this.getWaitImage()));
                this.hideEntryPopup();
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
                var form =  HtmlUtil.openTag("form",[ATTR_ID,this.getDomId(ID_FORM),"action","#"]);
                var extra = "";
                var text = this.searchSettings.text;
                if(text == null) text = "";
                var textField =  HtmlUtil.input("", text, ["placeholder","search text",ATTR_CLASS, "display-search-input ui-widget ui-button-text", "size","20",ATTR_ID,  this.getDomId(ID_TEXT_FIELD)]);

                form += HtmlUtil.div([ATTR_ID, this.getDomId(ID_SEARCH),ATTR_CLASS,"display-button"],"Search for:");
                if(this.showType) {
                    form += "&nbsp;&nbsp;";
                    form += HtmlUtil.tag("select",[ATTR_ID, this.getDomId(ID_TYPE_FIELD),
                                                   ATTR_CLASS,"display-typelist",
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
                            metadataSelect= HtmlUtil.tag("select",[ATTR_ID, this.getMetadataFieldId(type),
                                                                   ATTR_CLASS,"display-metadatalist"],
                                                         HtmlUtil.tag("option",["title","","value",""],
                                                                      NONE));
                        }
                        extra+= HtmlUtil.formEntry(type.getLabel() +":", metadataSelect);
                    }
                }
                extra+= HtmlUtil.closeTag(TAG_TABLE);
                extra+=    HtmlUtil.div([ATTR_ID,this.getDomId(ID_TYPE_FIELDS)],"");

                form += HtmlUtil.div([ATTR_CLASS, "display-search-extra"],
                                     HtmlUtil.toggleBlock("Search Settings", HtmlUtil.div([ATTR_CLASS, "display-search-extra-inner"], extra), this.formOpen));
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
                    var optionAttrs  = ["value",value,ATTR_CLASS, "display-metadatalist-item"];
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

            findEntryType: function(typeName) {
                if(this.types == null) return null;
                for(var i = 0;i< this.types.length;i++) {
                    var type = this.types[i];
                    if(type.getId() == typeName) return type;
                }
                return null;
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
                    
                    var optionAttrs  = ["title",type.getLabel(),"value",type.getId(),ATTR_CLASS, "display-typelist-type",
                                        //                                        "style", style,
                                        "data-iconurl",type.getIcon()];
                    var selected =  this.searchSettings.hasType(type.getId());
                    if(selected) {
                        optionAttrs.push("selected");
                        optionAttrs.push(null);
                    }
                    var option = HtmlUtil.tag("option",optionAttrs, type.getLabel() +" (" + type.getEntryCount() +")");
                    if(map == null) {
                        catMap[type.getCategory()] = HtmlUtil.tag("option",[ATTR_CLASS, "display-typelist-category", "title","","value",""],type.getCategory());
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
                        field  = HtmlUtil.openTag("select",[ATTR_ID, id]);
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
                        field = HtmlUtil.input("", savedValue, [ATTR_CLASS,"input", "size","15",ATTR_ID,  id]);
                    }
                    extra+= HtmlUtil.formEntry(col.getLabel() +":" ,field + " " + col.getSuffix());

                }
                if(extra.length>0) {
                    extra+=HtmlUtil.closeTag(TAG_TABLE);
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
                this.setContents(this.getDefaultHtml());
                if(this.dateRangeWidget) {
                    this.dateRangeWidget.initHtml();
                }
                SUPER.initDisplay.apply(this);
                if(this.entryList!=null && this.entryList.haveLoaded) {
                    this.entryListChanged(this.entryList);
                }
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
                this.writeHtml(ID_RESULTS, this.getResultsHeader(entries));

                html += HtmlUtil.openTag("ol",[ATTR_CLASS,"display-entrylist-list", ATTR_ID,this.getDomId(ID_LIST)]);
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

                    var line = HtmlUtil.div([ATTR_ID,this.getDomId("entryinner_" + entry.getId())], HtmlUtil.leftRight(left,right,"60%","30%"));
                    html  += HtmlUtil.tag("li",[ATTR_ID,
                                                this.getDomId("entry_" + entry.getId()),
                                                "entryid",entry.getId(), ATTR_CLASS,"display-entrylist-entry ui-widget-content " + rowClass], line);
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


function RamaddaMetadataDisplay(displayManager, id, properties) {
    if(properties.formOpen == null) {
       properties.formOpen = false;
    }
    var SUPER;
    RamaddaUtil.inherit(this, SUPER = new RamaddaSearcher(displayManager, id, DISPLAY_METADATA, properties));
    addRamaddaDisplay(this);
    RamaddaUtil.defineMembers(this, {
            haveDisplayed: false,
            initDisplay: function() {
                this.initUI();
                this.setContents(this.getDefaultHtml());
                SUPER.initDisplay.apply(this);
                if(this.haveDisplayed && this.entryList) {
                    this.entryListChanged(this.entryList);
                }
                this.haveDisplayed =true;
            },
            entryListChanged: function(entryList) {
                this.entryList = entryList;
                var entries = this.entryList.getEntries();
                if(entries.length==0) {
                    this.writeHtml(ID_ENTRIES, "Nothing found");
                    this.writeHtml(ID_RESULTS, "&nbsp;");
                    return;
                }
                var mdtsFromEntries = [];
                var mdtmap = {};
                var tmp = {};
                for(var i=0;i<entries.length;i++) {
                    var entry = entries[i];
                    var metadata = entry.getMetadata();
                    for(var j=0;j<metadata.length;j++) {
                        var m = metadata[j];
                        if(tmp[m.type] == null) {
                            tmp[m.type] = "";
                            mdtsFromEntries.push(m.type);
                        }
                        mdtmap[metadata[j].type] =metadata[j].label;
                    }
                }

                var html = "";
                html += HtmlUtil.openTag(TAG_TABLE,[ATTR_CLASS,"display-metadata-table","width","100%","cellpadding", "5","cellspacing","0"]);
                var type = this.findEntryType(this.searchSettings.entryType);
                var typeName = "Entry";
                if(type!=null) {
                    typeName  = type.getLabel();
                }
                this.writeHtml(ID_RESULTS, this.getResultsHeader(entries));



                var mdts =  null;
                //Get the metadata types to show from either a property or
                //gather them from all of the entries
                // e.g., "project_pi,project_person,project_funding"
                var prop = this.getProperty("metadataTypes",null);
                if(prop!=null) {
                    mdts = prop.split(",");
                } else {
                    mdts = mdtsFromEntries;
                    mdts.sort();
                }

                var headerItems = [];
                headerItems.push(HtmlUtil.th([ATTR_CLASS, "display-metadata-table-cell"],HtmlUtil.b(typeName)));
                for(var i=0;i<mdts.length;i++) {
                    var label = mdtmap[mdts[i]];
                    if(label == null) label = mdts[i];
                    headerItems.push(HtmlUtil.th([ATTR_CLASS, "display-metadata-table-cell"], HtmlUtil.b(label)));
                }
                var headerRow = HtmlUtil.tr(["valign", "bottom"],HtmlUtil.join(headerItems,""));
                html += headerRow;
                var divider = "<div class=display-metadata-divider></div>";
                var missing = this.missingMessage;
                if(missing = null) missing = "&nbsp;";
                for(var entryIdx=0;entryIdx<entries.length;entryIdx++) {
                    var entry = entries[entryIdx];
                    var metadata = entry.getMetadata();
                    var row = [];
                    var buttonId = this.getDomId("entrylink" + entry.getId());
                    var link =  HtmlUtil.onClick(this.getGet()+".showEntryPopup(event, '" + entry.getId() +"','" + buttonId +"',true);", 
                                                 entry.getIconImage() +" " + entry.getName(),[ATTR_ID,buttonId,ATTR_CLASS,"display-metadata-link"]);
                    row.push(HtmlUtil.td([ATTR_CLASS, "display-metadata-table-cell"],HtmlUtil.div([ATTR_CLASS,"display-metadata-entrylink"], link)));
                    for(var mdtIdx=0;mdtIdx<mdts.length;mdtIdx++) {
                        var mdt = mdts[mdtIdx];
                        var cell = null;
                        for(var j=0;j<metadata.length;j++) {
                            var m = metadata[j];
                            if(m.type == mdt) {
                                if(cell==null) {
                                    cell = "";
                                } else {
                                    cell += divider;
                                }
                                var item = null;
                                if(m.type == "content.thumbnail") {
                                    var url =ramaddaBaseUrl +"/metadata/view/" + m.attr1 +"?element=1&entryid=" + entry.getId() +"&metadata.id=" + m.id;
                                    item =  HtmlUtil.image(url,[]);
                                } else {
                                    item = m.attr1;
                                    if(m.attr2 && m.attr2.trim().length>0) {
                                        item += " - " + m.attr2;
                                    }
                                }
                                if(item!=null) {
                                    cell += HtmlUtil.div([ATTR_CLASS, "display-metadata-item"], item);
                                }
                                
                            }
                        }
                        if(cell ==null) {
                            cell = missing;
                        }
                        if(cell ==null) {
                            cell = "";
                        }
                        var add = HtmlUtil.tag("a", ["style","color:#000;", "href", ramaddaBaseUrl + "/metadata/addform?entryid=" + entry.getId() +"&metadata.type=" + mdt,
                                                     "target","_blank","alt","Add metadata","title","Add metadata"],"+");
                        var cellContents = add;
                        if(cell.length>0) 
                            cellContents = cell+divider + add;
                        row.push(HtmlUtil.td([ATTR_CLASS, "display-metadata-table-cell"],cellContents));
                    }
                    html += HtmlUtil.tr(["valign", "top"],HtmlUtil.join(row,""));
                    //Add in the header every 10 rows
                    if(((entryIdx+1) %10) == 0) html += headerRow;
                }
                html += HtmlUtil.closeTag(TAG_TABLE);
                this.jq(ID_ENTRIES).html(html);
            },
                });

}



function RamaddaTimelineDisplay(displayManager, id, properties) {
    if(properties.formOpen == null) {
       properties.formOpen = false;
    }
    var SUPER;
    RamaddaUtil.inherit(this, SUPER = new RamaddaSearcher(displayManager, id, DISPLAY_TIMELINE, properties));
    addRamaddaDisplay(this);
    RamaddaUtil.defineMembers(this, {
            initDisplay: function() {
                this.initUI();
                this.setContents(this.getDefaultHtml());
                SUPER.initDisplay.apply(this);
            },
            entryListChanged: function(entryList) {
                this.entryList = entryList;
                var entries = this.entryList.getEntries();
                var html = "";
                if(entries.length==0) {
                    this.writeHtml(ID_ENTRIES, "Nothing found");
                    this.writeHtml(ID_RESULTS, "&nbsp;");
                    return;
                }

                var data = {
                    "timeline":
                    {
                        "headline":"The Main Timeline Headline Goes here",
                        "type":"default",
                        "text":"<p>Intro body text goes here, some HTML is ok</p>",
                        "asset": {
                            "media":"http://yourdomain_or_socialmedialink_goes_here.jpg",
                            "credit":"Credit Name Goes Here",
                            "caption":"Caption text goes here"
                        },
                        "date": [
                {
                    "startDate":"2011,12,10",
                    "endDate":"2011,12,11",
                    "headline":"Headline Goes Here",
                    "text":"<p>Body text goes here, some HTML is OK</p>",
                    "tag":"This is Optional",
                    "classname":"optionaluniqueclassnamecanbeaddedhere",
                    "asset": {
                        "media":"http://twitter.com/ArjunaSoriano/status/164181156147900416",
                        "thumbnail":"optional-32x32px.jpg",
                        "credit":"Credit Name Goes Here",
                        "caption":"Caption text goes here"
                    }
                }
                ,
                {
                    "startDate":"2012,12,10",
                    "endDate":"2012,12,11",
                    "headline":"Headline Goes Here",
                    "text":"<p>Body text goes here, some HTML is OK</p>",
                    "tag":"This is Optional",
                    "classname":"optionaluniqueclassnamecanbeaddedhere",
                    "asset": {
                        "media":"http://twitter.com/ArjunaSoriano/status/164181156147900416",
                        "thumbnail":"optional-32x32px.jpg",
                        "credit":"Credit Name Goes Here",
                        "caption":"Caption text goes here"
                    }
                },
                {
                    "startDate":"2013,12,10",
                    "endDate":"2013,12,11",
                    "headline":"Headline Goes Here",
                    "text":"<p>Body text goes here, some HTML is OK</p>",
                    "tag":"This is Optional",
                    "classname":"optionaluniqueclassnamecanbeaddedhere",
                    "asset": {
                        "media":"http://twitter.com/ArjunaSoriano/status/164181156147900416",
                        "thumbnail":"optional-32x32px.jpg",
                        "credit":"Credit Name Goes Here",
                        "caption":"Caption text goes here"
                    }
                }

                                 ],
                        "era": [
                {
                    "startDate":"2011,12,10",
                    "endDate":"2011,12,11",
                    "headline":"Headline Goes Here",
                    "text":"<p>Body text goes here, some HTML is OK</p>",
                    "tag":"This is Optional"
                }

        ]
                    }
                };


                for(var i=0;i<entries.length;i++) {
                    var entry = entries[i];

                }
                createStoryJS({
                        type:       'timeline',
                            width:      '800',
                            height:     '600',
                            source:     data,
                            embed_id:   this.getDomId(ID_ENTRIES),  
                            });

            },
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
                html += HtmlUtil.div([ATTR_ID,this.getDomId(ID_ENTRIES),ATTR_CLASS,"display-entrylist-entries"], "");
                this.setContents(html);
            },
            entryListChanged: function(entryList) {
                var html = "<form>";
                html += "<p>";
                html += HtmlUtil.openTag(TAG_TABLE,[ATTR_CLASS,"formtable","cellspacing","0","cellspacing","0"]);
                var entries = this.entryList.getEntries();
                var get = this.getGet();

                for(var j=1;j<=2;j++) {
                    var select= HtmlUtil.openTag("select",[ATTR_ID, this.getDomId(ID_SELECT +j)]);
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

                var select  = HtmlUtil.openTag("select",[ATTR_ID, this.getDomId(ID_CHARTTYPE)]);
                select += HtmlUtil.tag("option",["title","","value","linechart"],
                                     "Line chart");
                select += HtmlUtil.tag("option",["title","","value","barchart"],
                                     "Bar chart");
                select += HtmlUtil.closeTag("select");
                html += HtmlUtil.formEntry("Chart Type:",select);

                html += HtmlUtil.closeTag(TAG_TABLE);
                html += "<p>";
                html +=  HtmlUtil.tag(TAG_DIV, [ATTR_CLASS, "display-button", ATTR_ID,  this.getDomId(ID_NEWDISPLAY)],"New Chart");
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

                pointDataList.push(new PointData(entry1.getName(), null, null, ramaddaBaseUrl +"/entry/show?&output=points.product&product=points.json&numpoints=1000&entryid=" +entry1.getId()));
                if(entry2!=null) {
                    pointDataList.push(new PointData(entry2.getName(), null, null, ramaddaBaseUrl +"/entry/show?&output=points.product&product=points.json&numpoints=1000&entryid=" +entry2.getId()));
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
