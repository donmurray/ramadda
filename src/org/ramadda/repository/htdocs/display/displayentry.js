
var DISPLAY_ENTRYLIST = "entrylist";
var DISPLAY_ENTRYDISPLAY = "entrydisplay";
var DISPLAY_OPERANDS = "operands";
var DISPLAY_METADATA = "metadata";
var DISPLAY_TIMELINE = "timeline";


var ID_RESULTS = "results";
var ID_ENTRIES = "entries";
var ID_DETAILS = "details";
var ID_DETAILS_INNER = "detailsinner";
var ID_DETAILS_MAIN = "detailsmain";


var ID_TREE_LINK = "treelink";

addGlobalDisplayType({type: DISPLAY_ENTRYLIST, label:"Entry List",requiresData:false,category:"Entry Displays"});
addGlobalDisplayType({type: DISPLAY_ENTRYDISPLAY, label:"Entry Display",requiresData:false,category:"Entry Displays"});
//addGlobalDisplayType({type: DISPLAY_OPERANDS, label:"Operands",requiresData:false,category:"Entry Displays"});
addGlobalDisplayType({type: DISPLAY_TIMELINE, label:"Timeline",requiresData:false,category:"Test"});
addGlobalDisplayType({type: DISPLAY_METADATA, label:"Metadata Table",requiresData:false,category:"Entry Displays"});


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
                 toolbarItems.push(HtmlUtil.tag(TAG_A, [ATTR_HREF, entry.getEntryUrl(),"target","_"], 
                                                HtmlUtil.image(ramaddaBaseUrl +"/icons/application-home.png",["border",0,ATTR_TITLE,"View Entry"])));
                 if(entry.getType().getId() == "type_wms_layer") {
                     toolbarItems.push(HtmlUtil.tag(TAG_A, ["onclick", get+".addMapLayer(" + HtmlUtil.sqt(entry.getId()) + ");"],
                                                            HtmlUtil.image(ramaddaBaseUrl +"/icons/map.png",["border",0,ATTR_TITLE,"Add Map Layer"])));

                 }


                 var jsonUrl = this.getPointUrl(entry);
                 if(jsonUrl!=null) {
                     toolbarItems.push(HtmlUtil.tag(TAG_A, ["onclick", get+".createDisplay(" + HtmlUtil.sqt(entry.getId()) +"," +
                                                            HtmlUtil.sqt("linechart") +"," + HtmlUtil.sqt(jsonUrl)+");"],
                                                            HtmlUtil.image(ramaddaBaseUrl +"/icons/chart_line_add.png",["border",0,ATTR_TITLE,"Create Chart"])));
                 }
                 if(entry.getFilesize()>0) {
                     toolbarItems.push(HtmlUtil.tag(TAG_A, [ATTR_HREF, entry.getFileUrl()], 
                                                    HtmlUtil.image(ramaddaBaseUrl +"/icons/download.png",["border",0,ATTR_TITLE,"Download (" + entry.getFormattedFilesize() +")"])));
                     
                 }
                 var entryMenuButton = this.getEntryMenuButton(entry);
                 /*
                 entryMenuButton =  HtmlUtil.onClick(this.getGet()+".showEntryDetails(event, '" + entry.getId() +"');", 
                                               HtmlUtil.image(ramaddaBaseUrl+"/icons/downdart.png", 
                                                              [ATTR_CLASS, "display-dialog-button", ATTR_ID,  this.getDomId(ID_MENU_BUTTON + entry.getId())]));

                 */

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
    var ID_TYPE_DIV = "typediv";
    var ID_FIELDS = "typefields";
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
                    entriesDivAttrs.push(ATTR_STYLE);
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

                this.jq(ID_REPOSITORY).selectBoxIt({});
                this.jq(ID_REPOSITORY).change(function() {
                        var entryManager = getEntryManager(theDisplay.jq(ID_REPOSITORY).val());
                        theDisplay.setEntryManager(entryManager);
                        theDisplay.addTypes(null);
                        theDisplay.typeChanged();
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
            hideEntryDetails: function(entryId) {
                //                var popupId = "#"+ this.getDomId(ID_DETAILS + entryId);
                //                $(popupId).hide();
                //                this.currentPopupEntry = null;
            },
            toggleEntryDetails: function(entryId) {
                var entry = this.entryList.getEntry(entryId);
                var link = this.jq(ID_TREE_LINK+entry.getId());
                var details = this.jq(ID_DETAILS + entry.getId());
                var detailsInner = this.jq(ID_DETAILS_INNER + entry.getId());
                var open = link.attr("tree-open")=="true";
                if(open) {
                    link.attr("src", icon_tree_closed);
                } else {
                    link.attr("src", icon_tree_open);
                }
                link.attr("tree-open", open?"false":"true");

                var hereBefore  =  details.attr("has-content") !=null;
                details.attr("has-content","true");
                if(hereBefore) {
                    //                    detailsInner.html(HtmlUtils.image(icon_progress));
                } else {
                    if(entry.getIsGroup()) {
                        detailsInner.html(HtmlUtil.image(icon_progress));
                        var theDisplay = this;
                        var callback = function(entries) {
                            theDisplay.displayChildren(entry, entries);
                        };
                        var entries = entry.getChildrenEntries(callback);
                    } else {
                        detailsInner.html(this.getEntryHtml(entry));
                    }
                }


                if(open) {
                    details.hide();
                } else {
                    details.show();
                }

            },
            displayChildren: function(entry, entries) {
                var detailsInner = this.jq(ID_DETAILS_INNER + entry.getId());
                if(entries.length==0) {
                    detailsInner.html(this.getEntryHtml(entry));
                } else {
                    var entriesHtml  = this.getEntriesTree(entries);
                    detailsInner.html(entriesHtml);
                    this.addEntrySelect();
                }
            },
            showEntryDetails: function(event, entryId, src,leftAlign) {
                if(true) return;
                var entry = this.entryList.getEntry(entryId);
                var popupId = "#"+ this.getDomId(ID_DETAILS+ entryId);
                if(this.currentPopupEntry ==  entry) {
                    this.hideEntryDetails(entryId);
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
                var close  = HtmlUtil.onClick(this.getGet()+ ".hideEntryDetails('" + entryId +"');",
                                              HtmlUtil.image(ramaddaBaseUrl +"/icons/close.gif"));
                
                var contents = this.getEntryHtml(entry, close);
                $(popupId).html(contents);
                $(popupId).show();
                /*
                $(popupId).position({
                        of: jQuery( "#" +src),
                            my: myloc,
                            at: atloc,
                            collision: "none none"
                            });
                */
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
                this.entryList = new EntryList(this.getEntryManager(), jsonUrl, this, this.entryList);
                this.updateForSearching(jsonUrl);
            },
            updateForSearching: function(jsonUrl) {
                var outputs = this.getEntryManager().getSearchLinks(this.searchSettings);
                this.footerRight  = "Links: " + HtmlUtil.join(outputs," - "); 
                this.writeHtml(ID_FOOTER_RIGHT, this.footerRight);
                this.writeHtml(ID_RESULTS, "Searching...");
                this.writeHtml(ID_ENTRIES, HtmlUtil.div([ATTR_STYLE,"margin:20px;"], this.getWaitImage()));
                this.hideEntryDetails();
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
                var jsonUrl = this.getEntryManager().getSearchUrl(this.searchSettings, OUTPUT_JSON);
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
                    form += HtmlUtil.span([ATTR_ID, this.getDomId(ID_TYPE_DIV)],HtmlUtil.span([ATTR_CLASS, "display-loading"], "Loading types..."));
                } 
                form += "&nbsp;&nbsp;";
                form += textField;

                var extra =   HtmlUtil.formTable();


                //["http://ramadda.org/repository;ramadda.org","http://community.ramadda.org/repository/repos/data;Community data site","http://community.ramadda.org/repository"];
                var repos = this.getProperty("repositories",this.getProperty("repos",null));
                if(repos != null) {
                    var ramaddas = repos.split(",");
                    if(ramaddas.length>0) {
                        ramaddas.unshift(this.getEntryManager().getId());

                        var select  = HtmlUtil.openTag(TAG_SELECT,[ATTR_ID, this.getDomId(ID_REPOSITORY), ATTR_CLASS,"display-repositories-select"]);
                        var icon = ramaddaBaseUrl +"/icons/favicon.png";
                        for(var i=0;i<ramaddas.length;i++) {
                            var entryManager = getEntryManager(ramaddas[i]);
                            var attrs = [ATTR_TITLE,"",ATTR_VALUE,entryManager.getId(),
                                         "data-iconurl",icon];
                            if(this.getEntryManager().getId() == entryManager.getId()) {
                                attrs.push("selected");
                                attrs.push(null);
                            }
                            var label = 
                                select += HtmlUtil.tag(TAG_OPTION,attrs,
                                                       entryManager.getName());
                        }
                        select += HtmlUtil.closeTag(TAG_SELECT);
                        extra += HtmlUtil.formEntry("Repository:",select);
                    }
                }


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
                            metadataSelect= HtmlUtil.tag(TAG_SELECT,[ATTR_ID, this.getMetadataFieldId(type),
                                                                   ATTR_CLASS,"display-metadatalist"],
                                                         HtmlUtil.tag(TAG_OPTION,[ATTR_TITLE,"",ATTR_VALUE,""],
                                                                      NONE));
                        }
                        extra+= HtmlUtil.formEntry(type.getLabel() +":", metadataSelect);
                    }
                }
                extra+= HtmlUtil.closeTag(TAG_TABLE);
                extra+=    HtmlUtil.div([ATTR_ID,this.getDomId(ID_FIELDS)],"");

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
                        metadata = this.getEntryManager().getMetadataCount(metadataType, function(metadataType, metadata) {theDisplay.addMetadata(metadataType, metadata);});
                    }
                }
                if(metadata == null) {
                    return;
                }

                this.metadata[metadataType.getType()] = metadata;


                var select = HtmlUtil.tag(TAG_OPTION,[ATTR_TITLE,"",ATTR_VALUE,""],NONE);
                for(var i =0;i<metadata.length;i++) {
                    var count = metadata[i].count;
                    var value = metadata[i].value;
                    var optionAttrs  = [ATTR_VALUE,value,ATTR_CLASS, "display-metadatalist-item"];
                    var selected =  false;
                    if(selected) {
                        optionAttrs.push("selected");
                        optionAttrs.push(null);
                    }
                    select +=  HtmlUtil.tag(TAG_OPTION,optionAttrs, value +" (" + count +")");
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
                    types = this.getEntryManager().getEntryTypes(function(types) {theDisplay.addTypes(types);});
                }
                if(types == null) {
                    return;
                }
                this.types = types;
                this.haveTypes = true;
                var cats =[];
                var catMap = {}; 
                var select =  HtmlUtil.openTag(TAG_SELECT,[ATTR_ID, this.getDomId(ID_TYPE_FIELD),
                                                           ATTR_CLASS,"display-typelist",
                                                           "onchange", this.getGet()+".typeChanged();"]);
                //                HtmlUtil.tag(TAG_OPTION,[ATTR_TITLE,"",ATTR_VALUE,""], " Choose Type "));
                select += HtmlUtil.tag(TAG_OPTION,[ATTR_TITLE,"",ATTR_VALUE,""],"Any Type");

                var hostname = this.getEntryManager().getHostname();
                for(var i = 0;i< types.length;i++) {
                    var type = types[i];
                    //                    var style = " background: URL(" + type.getIcon() +") no-repeat;";

                    var icon =                     type.getIcon();
                    if(hostname) icon = hostname + icon;
                    var optionAttrs  = [ATTR_TITLE,type.getLabel(),ATTR_VALUE,type.getId(),ATTR_CLASS, "display-typelist-type",
                                        //                                        ATTR_STYLE, style,
                                        "data-iconurl",icon];
                    var selected =  this.searchSettings.hasType(type.getId());
                    if(selected) {
                        optionAttrs.push("selected");
                        optionAttrs.push(null);
                    }
                    var option = HtmlUtil.tag(TAG_OPTION,optionAttrs,  type.getLabel() +" (" + type.getEntryCount() +")");
                    var map = catMap[type.getCategory()];
                    if(map == null) {
                        catMap[type.getCategory()] = HtmlUtil.tag(TAG_OPTION,[ATTR_CLASS, "display-typelist-category", ATTR_TITLE,"",ATTR_VALUE,""],type.getCategory());
                        cats.push(type.getCategory());
                    }
                    catMap[type.getCategory()] += option;

                }
                for(var i in cats) {
                    select += catMap[cats[i]];
                }

                select+=  HtmlUtil.closeTag(TAG_SELECT);
                //                this.writeHtml(ID_TYPE_FIELD, "# " + types.length);
                //                this.writeHtml(ID_TYPE_FIELD, select);
                this.writeHtml(ID_TYPE_DIV, select);
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
                        field  = HtmlUtil.openTag(TAG_SELECT,[ATTR_ID, id]);
                        field += HtmlUtil.tag(TAG_OPTION,[ATTR_TITLE,"",ATTR_VALUE,""],
                                              "-- Select --");
                        var values = col.getValues();
                        for(var vidx in values) {
                            var value = values[vidx].value;
                            var label = values[vidx].label;
                            var extraAttr = "";
                            if(value == savedValue) {
                                extraAttr =  " selected ";
                            }
                            field += HtmlUtil.tag(TAG_OPTION,[ATTR_TITLE,label,ATTR_VALUE,value, extraAttr,  null],
                                                  label);
                        }
                        field  += HtmlUtil.closeTag(TAG_SELECT);
                    } else {
                        field = HtmlUtil.input("", savedValue, [ATTR_CLASS,"input", "size","15",ATTR_ID,  id]);
                    }
                    extra+= HtmlUtil.formEntry(col.getLabel() +":" ,field + " " + col.getSuffix());

                }
                if(extra.length>0) {
                    extra+=HtmlUtil.closeTag(TAG_TABLE);
                }
                
                this.writeHtml(ID_FIELDS, extra);

                
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
                var entries = this.entryList.getEntries();

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


                var get = this.getGet();
                this.writeHtml(ID_FOOTER_LEFT,"");
                if(this.footerRight!=null) {
                    this.writeHtml(ID_FOOTER_RIGHT, this.footerRight);
                }

                var entriesHtml  = this.getEntriesTree(entries);

                var html = "";
                html += HtmlUtil.openTag(TAG_OL,[ATTR_CLASS,"display-entrylist-list", ATTR_ID,this.getDomId(ID_LIST)]);
                html += entriesHtml;
                html += HtmlUtil.closeTag(TAG_OL);
                this.writeHtml(ID_ENTRIES, html);
                this.addEntrySelect();

                this.getDisplayManager().handleEventEntriesChanged(this, entries);
            },
            addEntrySelect: function() {
                var theDisplay   =this;
                //                var entryRows = $("#" + this.getDomId(ID_LIST) +"  .display-entrylist-entry-main");
                var entryRows = $("#" + this.getDomId(ID_DISPLAY_CONTENTS) +"  .display-entrylist-entry-main");

                entryRows.unbind();
                entryRows.mouseover(function(event){
                        var entryId = $( this ).attr('entryid');
                        var toolbarId = theDisplay.getEntryToolbarId(entryId);

                        var toolbar = $("#" + toolbarId);
                        toolbar.show();
                        var myalign = 'right center';
                        var atalign = 'right center';
                        //                        var srcId = theDisplay.getDomId("entry_" + entryId);
                        var srcId =  theDisplay.getDomId(ID_DETAILS_MAIN + entryId);
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
                            var entryId = ui.selected.getAttribute('entryid');
                            theDisplay.hideEntryDetails(entryId);
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
            },
            getEntriesTree:function (entries) {
                var html = "";
                var rowClass = "entryrow_" + this.getId()
                for(var i=0;i<entries.length;i++) {
                    var entry = entries[i];
                    var toolbar = this.makeEntryToolbar(entry);
                    var icon = entry.getIconImage([ATTR_TITLE,"View entry"]);
                    var link  =  HtmlUtil.tag(TAG_A,[ATTR_HREF, entry.getEntryUrl()],icon);
                    var entryName = entry.getName();
                    if(entryName.length>100) {
                        entryName = entryName.substring(0,99)+"...";
                    }

                    var  arrow = HtmlUtil.image(icon_tree_closed,[ATTR_BORDER,"0",
                                                                  "tree-open","false",
                                                                  ATTR_ID,
                                                                  this.getDomId(ID_TREE_LINK+entry.getId())]);
                    var open =  HtmlUtil.onClick(this.getGet()+".toggleEntryDetails('" + entry.getId()+ "');", 
                                                 arrow);


                    var left =   HtmlUtil.div([ATTR_STYLE," white-space: nowrap;  overflow-x:none; max-width:300px;"],open +" " + link +" " +  entryName);

                    var details = HtmlUtil.div([ATTR_ID,this.getDomId(ID_DETAILS+entry.getId()), ATTR_CLASS,"display-entrylist-details"],HtmlUtil.div([ATTR_CLASS,"display-entrylist-details-inner",ATTR_ID,this.getDomId(ID_DETAILS_INNER+entry.getId())],""));

                    var mainLine = HtmlUtil.div([ATTR_ID, this.getDomId(ID_DETAILS_MAIN+ entry.getId()), ATTR_CLASS,"display-entrylist-entry-main", "entryid",entry.getId()], HtmlUtil.leftRight(left,toolbar,"60%","30%"));
                    var line = HtmlUtil.div([ATTR_ID, this.getDomId("entryinner_" + entry.getId())], mainLine + details);
                    html  += HtmlUtil.tag(TAG_LI,[ATTR_ID,
                                                this.getDomId("entry_" + entry.getId()),
                                                  "entryid",entry.getId(), ATTR_CLASS,"display-entrylist-entry ui-widget-content " + rowClass], line);
                    html  += "\n";
                }
                return html;
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
                    var link =  HtmlUtil.onClick(this.getGet()+".showEntryDetails(event, '" + entry.getId() +"','" + buttonId +"',true);", 
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
                        var add = HtmlUtil.tag(TAG_A, [ATTR_STYLE,"color:#000;", ATTR_HREF, ramaddaBaseUrl + "/metadata/addform?entryid=" + entry.getId() +"&metadata.type=" + mdt,
                                                     "target","_blank","alt","Add metadata",ATTR_TITLE,"Add metadata"],"+");
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
    var ID_SELECT = TAG_SELECT;
    var ID_SELECT1 = "select1";
    var ID_SELECT2 = "select2";
    var ID_NEWDISPLAY = "newdisplay";

    $.extend(this, new RamaddaEntryDisplay(displayManager, id, DISPLAY_OPERANDS, properties));
    addRamaddaDisplay(this);
    $.extend(this, {
            baseUrl: null,
            initDisplay: function() {
                this.initUI();
                this.baseUrl = this.getEntryManager().getSearchUrl(this.searchSettings, OUTPUT_JSON);
                this.entryList = new EntryList(this.getEntryManager(), jsonUrl, this, this.entryList);
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
                    var select= HtmlUtil.openTag(TAG_SELECT,[ATTR_ID, this.getDomId(ID_SELECT +j)]);
                    select += HtmlUtil.tag(TAG_OPTION,[ATTR_TITLE,"",ATTR_VALUE,""],
                                         "-- Select --");
                    for(var i=0;i<entries.length;i++) {
                        var entry = entries[i];
                        var label = entry.getIconImage() +" " + entry.getName();
                        select += HtmlUtil.tag(TAG_OPTION,[ATTR_TITLE,entry.getName(),ATTR_VALUE,entry.getId()],
                                             entry.getName());
                        
                    }
                    select += HtmlUtil.closeTag(TAG_SELECT);
                    html += HtmlUtil.formEntry("Data:",select);
                }

                var select  = HtmlUtil.openTag(TAG_SELECT,[ATTR_ID, this.getDomId(ID_CHARTTYPE)]);
                select += HtmlUtil.tag(TAG_OPTION,[ATTR_TITLE,"",ATTR_VALUE,"linechart"],
                                     "Line chart");
                select += HtmlUtil.tag(TAG_OPTION,[ATTR_TITLE,"",ATTR_VALUE,"barchart"],
                                     "Bar chart");
                select += HtmlUtil.closeTag(TAG_SELECT);
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
