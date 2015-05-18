/**
Copyright 2008-2015 Geode Systems LLC
*/



var DISPLAY_ENTRYLIST = "entrylist";
var DISPLAY_ENTRYDISPLAY = "entrydisplay";
var DISPLAY_ENTRY_GALLERY = "entrygallery";
var DISPLAY_OPERANDS = "operands";
var DISPLAY_METADATA = "metadata";
var DISPLAY_TIMELINE = "timeline";
var DISPLAY_REPOSITORIES = "repositories";

var ID_RESULTS = "results";
var ID_ENTRIES = "entries";
var ID_DETAILS = "details";
var ID_DETAILS_INNER = "detailsinner";
var ID_DETAILS_MAIN = "detailsmain";


var ID_TREE_LINK = "treelink";

addGlobalDisplayType({type: DISPLAY_ENTRYLIST, label:"Entry List",requiresData:false,category:"Entry Displays"});
addGlobalDisplayType({type: DISPLAY_ENTRYDISPLAY, label:"Entry Display",requiresData:false,category:"Entry Displays"});
addGlobalDisplayType({type: DISPLAY_ENTRY_GALLERY, label:"Entry Gallery",requiresData:false,category:"Entry Displays"});
//addGlobalDisplayType({type: DISPLAY_OPERANDS, label:"Operands",requiresData:false,category:"Entry Displays"});
addGlobalDisplayType({type: DISPLAY_METADATA, label:"Metadata Table",requiresData:false,category:"Entry Displays"});

addGlobalDisplayType({type: DISPLAY_TIMELINE, label:"Timeline",requiresData:false,category:"Test"});


function RamaddaEntryDisplay(displayManager, id, type, properties) {
     var SUPER;
     var ID_TOOLBAR = "toolbar";
     var ID_TOOLBAR_INNER = "toolbarinner";
     RamaddaUtil.inherit(this, SUPER = new RamaddaDisplay(displayManager, id, type, properties));

     this.ramaddas = new Array();
     var repos = this.getProperty("repositories",this.getProperty("repos",null));
     if(repos != null) {
         var toks = repos.split(",");
         //OpenSearch;http://adasd..asdasdas.dasdas.,
         for(var i=0;i<toks.length;i++) {
             var tok = toks[i];
             tok = tok.trim();
             this.ramaddas.push(getRamadda(tok));
         }
         if(this.ramaddas.length>0) {
             var container = new RepositoryContainer("all", "All entries");
             addRepository(container);
             for(var i=0;i<this.ramaddas.length;i++) {
                 container.addRepository(this.ramaddas[i]);
             }
             this.ramaddas.push(container);
             this.setRamadda(this.ramaddas[0]);
         }
     }



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
                     toolbarItems.push(HtmlUtil.tag(TAG_A, ["onclick", get+".createDisplay(" + HtmlUtil.sqt(entry.getFullId()) +"," +
                                                            HtmlUtil.sqt("linechart") +"," + HtmlUtil.sqt(jsonUrl)+");"],
                                                            HtmlUtil.image(ramaddaBaseUrl +"/icons/chart_line_add.png",["border",0,ATTR_TITLE,"Create Chart"])));
                 }
                 if(entry.getFilesize()>0) {
                     toolbarItems.push(HtmlUtil.tag(TAG_A, [ATTR_HREF, entry.getResourceUrl()], 
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
                 var id = entryId.replace(/:/g,"_");
                 id = id.replace(/=/g,"_");
                 return this.getDomId(ID_TOOLBAR +"_" + id);
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
            showSearchSettings: true,            
            showEntries: true,
            showType: true,           
            formOpen: true,
            fullForm: true,            
            showMetadata: true,
            showToggle:true,
            showArea: true,
            showText: true,
            showDate: true,
            fields: null,
            formWidth: 0,
            entriesWidth: 0,
            //List of type names from user
            types: null,
            entryTypes: null,
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
                var entriesDivAttrs = [ATTR_ID,this.getDomId(ID_ENTRIES),ATTR_CLASS,this.getClass("content")];
                var innerHeight = this.getProperty("innerHeight",null);
                if(innerHeight == null) {
                    innerHeight = this.getProperty("entriesHeight",null);
                }
                if(innerHeight!=null) {
                    entriesDivAttrs.push(ATTR_STYLE);
                    entriesDivAttrs.push("margin: 0px; padding: 0px;  min-height:" + innerHeight +"px; max-height:" + innerHeight +"px; overflow-y: auto;");
                }
                var resultsDiv = "";
                if(this.getProperty("showHeader", true)) {
                    resultsDiv = HtmlUtil.div([ATTR_CLASS,"display-entries-results", ATTR_ID,this.getDomId(ID_RESULTS)],"&nbsp;"); 
                }

                var entriesDiv = 
                    resultsDiv +
                    HtmlUtil.div(entriesDivAttrs, this.getLoadingMessage());
                


                if(horizontal) {
                    html+= HtmlUtil.openTag(TAG_TABLE,["border","0", ATTR_WIDTH,"100%", "cellpadding","0","cellpadding","5"]);
                    html += HtmlUtil.openTag(TAG_TR,["valign","top"]);
                    if(this.showForm) {
                        var attrs = [];
                        if(this.formWidth === "") {
                            attrs = [];
                        } else if(this.formWidth!=0) {
                            attrs = [ATTR_WIDTH,this.formWidth];
                        }
                        html += HtmlUtil.tag(TAG_TD,attrs,this.makeSearchForm());
                    }
                    if(this.showEntries) {
                        var attrs = [ATTR_WIDTH,"75%"];
                        if(this.entriesWidth === "") {
                            attrs = [];
                        }  else if(this.entriesWidth!=0) {
                            attrs = [ATTR_WIDTH,this.entriesWidth];
                        }
                        html += HtmlUtil.tag(TAG_TD,attrs,entriesDiv);
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
                            //                            theDisplay.doQuickEntrySearch(request, callback);
                            }
                            });

                this.jq(ID_REPOSITORY).selectBoxIt({});
                this.jq(ID_REPOSITORY).change(function() {
                        var v = theDisplay.jq(ID_REPOSITORY).val();
                        var ramadda = getRamadda(v);
                        theDisplay.setRamadda(ramadda);
                        theDisplay.addTypes(null);
                        theDisplay.typeChanged();
                    });

                this.jq(ID_FORM).submit(function( event ) {
                        theDisplay.submitSearchForm();
                        event.preventDefault();
                    });


                this.addTypes(this.entryTypes);
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
                var entry = this.getEntry(entryId);
                var link = this.jq(ID_TREE_LINK+entry.getIdForDom());
                var details = this.jq(ID_DETAILS + entry.getIdForDom());
                var detailsInner = this.jq(ID_DETAILS_INNER + entry.getIdForDom());
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
                    //                    detailsInner.html(HtmlUtil.image(icon_progress));
                } else {
                    if(entry.getIsGroup() && !entry.isRemote) {
                        detailsInner.html(HtmlUtil.image(icon_progress));
                        var theDisplay = this;
                        var callback = function(entries) {
                            theDisplay.displayChildren(entry, entries);
                        };
                        var entries = entry.getChildrenEntries(callback);
                    } else {
                        detailsInner.html(this.getEntryHtml(entry,{showHeader:false}));
                    }
                }


                if(open) {
                    details.hide();
                } else {
                    details.show();
                }

            },
            displayChildren: function(entry, entries) {
                var detailsInner = this.jq(ID_DETAILS_INNER + entry.getIdForDom());
                if(entries.length==0) {
                    detailsInner.html(this.getEntryHtml(entry,{showHeader:false}));
                } else {
                    var entriesHtml  = this.getEntriesTree(entries);
                    detailsInner.html(entriesHtml);
                    this.addEntrySelect();
                }
            },
            showEntryDetails: function(event, entryId, src,leftAlign) {
                if(true) return;
                var entry = this.getEntry(entryId);
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
                if(src == null) src =  this.getDomId("entry_" + entry.getIdForDom());
                var close  = HtmlUtil.onClick(this.getGet()+ ".hideEntryDetails('" + entryId +"');",
                                              HtmlUtil.image(ramaddaBaseUrl +"/icons/close.gif"));
                
                var contents = this.getEntryHtml(entry, {headerRight:close});
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
                var nextPrev = [];
                var lessMore = [];
                if(this.searchSettings.skip>0) {
                    nextPrev.push(HtmlUtil.onClick(this.getGet()+".loadPrevUrl();", "Previous",[ATTR_CLASS,"display-link"]));
                }
                var addMore = false;
                if(entries.length == this.searchSettings.getMax()) {
                    nextPrev.push(HtmlUtil.onClick(this.getGet()+".loadNextUrl();", "Next",[ATTR_CLASS,"display-link"]));
                    addMore = true;
                }

                lessMore.push(HtmlUtil.onClick(this.getGet()+".loadLess();", HtmlUtil.image(ramaddaBaseUrl +"/icons/minus-small-white.png",[ATTR_ALT, "View less", ATTR_TITLE, "View less", "border","0"]),[ATTR_CLASS,"display-link"]));
                if(addMore) {
                    lessMore.push(HtmlUtil.onClick(this.getGet()+".loadMore();", HtmlUtil.image(ramaddaBaseUrl +"/icons/plus-small-white.png",[ATTR_ALT, "View more", ATTR_TITLE, "View more","border","0"]),[ATTR_CLASS,"display-link"]));
                }
                var results = "";
                results = HtmlUtil.join(nextPrev,  "") + "&nbsp;&nbsp;" +
                    left + "&nbsp;&nbsp;" +
                    HtmlUtil.join(lessMore,  "") ;
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


              var theRepository= this.getRamadda()

              if(theRepository.children) {
                  console.log("Searching  multiple ramaddas");
                  this.entryList = new EntryListHolder(theRepository, this);
                  this.multiSearch  = {
                      count: 0,
                  };

                  for(var i =0;i<theRepository.children.length;i++) {
                      var ramadda = theRepository.children[i];
                      var jsonUrl = this.makeSearchUrl(ramadda);
                      this.updateForSearching(jsonUrl);
                      this.entryList.addEntryList(new EntryList(ramadda, jsonUrl, null, false));
                      this.multiSearch.count++;
                  }
                  this.entryList.doSearch(this);
              } else {
                  this.multiSearch  = null;
                  var jsonUrl = this.makeSearchUrl(this.getRamadda());
                  this.entryList = new EntryList(this.getRamadda(), jsonUrl, this, true);
                  this.updateForSearching(jsonUrl);
              }


            },
            handleSearchError: function(url, msg) {
                this.writeHtml(ID_ENTRIES, "");
                this.writeHtml(ID_RESULTS, "");
                console.log("Error performing search:" + msg);
                //alert("There was an error performing the search\n" + msg);
            },
            updateForSearching: function(jsonUrl) {
                var outputs = this.getRamadda().getSearchLinks(this.searchSettings);
                this.footerRight  = outputs == null?"":"Links: " + HtmlUtil.join(outputs," - "); 
                this.writeHtml(ID_FOOTER_RIGHT, this.footerRight);
                this.writeHtml(ID_RESULTS, this.getRamadda().getSearchMessage());
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
            makeSearchUrl: function(repository) {
                var extra = "";
                var cols  = this.getSearchableColumns();
                for(var i =0;i<cols.length;i++) {
                    var col = cols[i];
                    var value = this.jq(ID_COLUMN+col.getName()).val();
                    if(value == null || value.length == 0)continue;
                    extra+= "&" + col.getSearchArg() +"=" + encodeURI(value);
                }
                this.searchSettings.setExtra(extra);
                var jsonUrl = repository.getSearchUrl(this.searchSettings, OUTPUT_JSON);
                return jsonUrl;
            },
            makeSearchForm: function() {
                var form =  HtmlUtil.openTag("form",[ATTR_ID,this.getDomId(ID_FORM),"action","#"]);
                var extra = "";
                var text = this.searchSettings.text;
                if(text == null) text = "";
                var textField =  HtmlUtil.input("", text, ["placeholder","search text",ATTR_CLASS, "display-search-input", ATTR_SIZE,"8",ATTR_ID,  this.getDomId(ID_TEXT_FIELD)]);

                var buttonLabel =  HtmlUtil.image(ramaddaBaseUrl +"/icons/magnifier.png",[ATTR_BORDER,"0",ATTR_TITLE,"Search"]);
                var topItems = [];
                var extra = "";

                extra +=   HtmlUtil.div([ATTR_ID,this.getDomId(ID_FIELDS)],"");
                extra +=   HtmlUtil.formTable();

                topItems.push(HtmlUtil.div([ATTR_ID, this.getDomId(ID_SEARCH),ATTR_CLASS,"display-button"],buttonLabel));

                if(this.ramaddas.length>0) {
                    var select  = HtmlUtil.openTag(TAG_SELECT,[ATTR_ID, this.getDomId(ID_REPOSITORY), ATTR_CLASS,"display-repositories-select"]);
                    var icon = ramaddaBaseUrl +"/icons/favicon.png";
                    for(var i=0;i<this.ramaddas.length;i++) {
                        var ramadda = this.ramaddas[i];
                        var attrs = [ATTR_TITLE,"",ATTR_VALUE,ramadda.getId(),
                                     "data-iconurl",icon];
                        if(this.getRamadda().getId() == ramadda.getId()) {
                            attrs.push("selected");
                            attrs.push(null);
                        }
                        var label = 
                            select += HtmlUtil.tag(TAG_OPTION,attrs,
                                                   ramadda.getName());
                    }
                    select += HtmlUtil.closeTag(TAG_SELECT);
                    topItems.push(select);
                }


                if(this.showType) {
                    topItems.push(HtmlUtil.span([ATTR_ID, this.getDomId(ID_TYPE_DIV)],HtmlUtil.span([ATTR_CLASS, "display-loading"], "Loading types...")));
                } 

                if(this.showText) {
                    topItems.push(textField);
                }
                form += HtmlUtil.join(topItems,"&nbsp;");

                if(this.showArea) {
                    this.areaWidget = new AreaWidget(this);
                    extra += HtmlUtil.formEntry("Area:",this.areaWidget.getHtml());
                }


                if(this.showDate) {
                    this.dateRangeWidget  = new DateRangeWidget(this);
                    extra += HtmlUtil.formEntry("Date Range:",this.dateRangeWidget.getHtml());
                }

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



                if(this.showSearchSettings) {
                    if(this.showToggle) {
                        form += HtmlUtil.div([ATTR_CLASS, "display-search-extra"],
                                             HtmlUtil.toggleBlock("Search Settings", HtmlUtil.div([ATTR_CLASS, "display-search-extra-inner"], extra), this.formOpen));
                    } else {
                        form += HtmlUtil.div([ATTR_CLASS, "display-search-extra"],
                                             HtmlUtil.div([ATTR_CLASS, "display-search-extra-inner"], extra));
                    }
                }

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
                        metadata = this.getRamadda().getMetadataCount(metadataType, function(metadataType, metadata) {theDisplay.addMetadata(metadataType, metadata);});
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
                if(this.entryTypes == null) return null;
                for(var i = 0;i< this.entryTypes.length;i++) {
                    var type = this.entryTypes[i];
                    if(type.getId() == typeName) return type;
                }
                return null;
            },
            addTypes: function(newTypes) {
                if(newTypes == null) {
                    var theDisplay = this;
                    newTypes = this.getRamadda().getEntryTypes(function(ramadda, types) {theDisplay.addTypes(types);});
                }
                if(newTypes == null) {
                    return;
                }
                this.entryTypes = newTypes;

                if(this.types) {
                    var showType = {};
                    this.types = this.types.split(",");
                    for(var i=0;i<this.types.length;i++) {
                        var type  = this.types[i];
                        showType[type] = true;
                    }
                    var tmp = [];
                    for(var i=0;i<this.entryTypes.length;i++) {
                        var type  = this.entryTypes[i];
                        if(showType[type.getId()]) {
                            tmp.push(type);
                        } else if(type.getCategory()!=null && showType[type.getCategory()]) {
                            tmp.push(type);
                        }
                    }
                    this.entryTypes = tmp;
                    this.types = null;
                }

                this.haveTypes = true;
                var cats =[];
                var catMap = {}; 
                var select =  HtmlUtil.openTag(TAG_SELECT,[ATTR_ID, this.getDomId(ID_TYPE_FIELD),
                                                           ATTR_CLASS,"display-typelist",
                                                           "onchange", this.getGet()+".typeChanged();"]);
                //                HtmlUtil.tag(TAG_OPTION,[ATTR_TITLE,"",ATTR_VALUE,""], " Choose Type "));
                select += HtmlUtil.tag(TAG_OPTION,[ATTR_TITLE,"",ATTR_VALUE,""],"Any Type");

                for(var i = 0;i< this.entryTypes.length;i++) {
                    var type = this.entryTypes[i];
                    //                    var style = " background: URL(" + type.getIcon() +") no-repeat;";
                    var icon =                     type.getIcon();
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
                //                this.writeHtml(ID_TYPE_FIELD, "# " + entryTypes.length);
                //                this.writeHtml(ID_TYPE_FIELD, select);
                this.writeHtml(ID_TYPE_DIV, select);
                this.jq(ID_TYPE_FIELD).selectBoxIt({});
                this.addExtraForm();
           },
           getSelectedType: function() {
                if(this.entryTypes == null) return null;
                for(var i = 0;i< this.entryTypes.length;i++) {
                    var type = this.entryTypes[i];
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
                    if(this.fields!=null && this.fields.indexOf(col.getName())<0) {
                        continue;
                    }


                    if(extra.length==0) {
                        extra+=HtmlUtil.formTable();
                    }
                    var field  ="";
                    var id = this.getDomId(ID_COLUMN+col.getName());
                    var savedValue  = this.savedValues[id];
                    if(savedValue == null) {
                        savedValue = this.jq(ID_COLUMN+col.getName()).val();
                    }
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
                        field = HtmlUtil.input("", savedValue, [ATTR_CLASS,"input", ATTR_SIZE,"15",ATTR_ID,  id]);
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
                    this.jq("entry_" + entry.getIdForDom()).addClass("ui-selected");
                    var index = this.selectedEntries.indexOf(entry);
                    if (index < 0) {
                        this.selectedEntries.push(entry);
                        changed = true;
                    }
                } else {
                    this.jq("entry_" + entry.getIdForDom()).removeClass("ui-selected");
                    var index = this.selectedEntries.indexOf(entry);
                    if (index >= 0) {
                        this.selectedEntries.splice(index, 1);
                        changed = true;
                    }
                }
            },

            entryListChanged: function(entryList) {
                if(this.multiSearch) {
                    this.multiSearch.count--;
                }
                SUPER.entryListChanged.apply(this,[entryList]);
                var entries = this.entryList.getEntries();

                if(entries.length==0) {
                    this.searchSettings.skip=0;
                    this.searchSettings.max=50;
                    var msg = "Nothing found";
                    if(this.multiSearch) {
                        if(this.multiSearch.count>0) {
                            msg = "Nothing found so far. Still searching " + this.multiSearch.count +" repositories";
                        } else {
                        }
                    }
                    this.writeHtml(ID_ENTRIES, this.getMessage(msg));
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
                html += HtmlUtil.openTag(TAG_OL,[ATTR_CLASS,this.getClass("list"), ATTR_ID,this.getDomId(ID_LIST)]);
                html += entriesHtml;
                html += HtmlUtil.closeTag(TAG_OL);
                this.writeHtml(ID_ENTRIES, html);
                this.addEntrySelect();

                this.getDisplayManager().handleEventEntriesChanged(this, entries);
            },
            addEntrySelect: function() {
                var theDisplay   =this;
                var entryRows = $("#" + this.getDomId(ID_DISPLAY_CONTENTS) +"  ." + this.getClass("entry-main"));

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
                            var entry = theDisplay.getEntry(entryId);
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
                            var entry = theDisplay.getEntry(entryId);
                            var index = theDisplay.selectedEntries.indexOf(entry);
                            //                            console.log("remove:" +  index + " " + theDisplay.selectedEntries);
                            if (index > -1) {
                                theDisplay.selectedEntries.splice(index, 1);
                                theDisplay.getDisplayManager().handleEventEntrySelection(theDisplay, {entry:entry, selected:false});
                            }
                        },
                            
                    });
            },
            getEntriesTable:function (entries, columns, columnNames) {
                var columnWidths = this.getProperty("columnWidths",null);
                if(columnWidths!=null) {
                    columnWidths  = columnWidths.split(",");
                }
                var html = HtmlUtil.openTag(TAG_TABLE,[ATTR_WIDTH,"100%","cellpadding", "0","cellspacing","0"]);
                html += HtmlUtil.openTag(TAG_TR,["valign","top"]);
                for(var i=0;i<columnNames.length;i++) {
                    html += HtmlUtil.td([ATTR_ALIGN,"center", ATTR_CLASS, "display-entrytable-header"],columnNames[i]);
                }
                html += HtmlUtil.closeTag(TAG_TR);

                for(var i=0;i<entries.length;i++) {
                    html += HtmlUtil.openTag(TAG_TR,["valign","top"]);
                    var entry = entries[i];
                    for(var j=0;j<columns.length;j++) {
                        var columnWidth = null;
                        if(columnWidths!=null) {
                            columnWidth= columnWidths[j];
                        }
                        var column = columns[j];
                        var value = "";
                        if(column == "name") {
                            value =   HtmlUtil.tag(TAG_A,[ATTR_HREF, entry.getEntryUrl()],entry.getName());
                        } else if(column == "description") {
                            value = entry.getDescription();
                        } else {
                            value = entry.getAttributeValue(column);
                        }
                        var attrs = [ATTR_CLASS, "display-entrytable-cell"];
                        if(columnWidth!=null) {
                            attrs.push(ATTR_WIDTH);
                            attrs.push(columnWidth);
                        }

                        html += HtmlUtil.td(attrs,value);
                    }
                    html += HtmlUtil.closeTag(TAG_TR);
                }
                html += HtmlUtil.closeTag(TAG_TABLE);
                return html;
            },
            getEntriesTree:function (entries) {
                var columns = this.getProperty("columns",null);
                if(columns!=null) {
                    columns = columns.split(",");
                    var columnNames = this.getProperty("columnNames",null);
                    if(columnNames == null) {
                        columnNames = columns;
                    } else {
                        columnNames = columnNames.split(",");
                    }
                    return this.getEntriesTable(entries, columns, columnNames);
                }

                
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
                                                                  this.getDomId(ID_TREE_LINK+entry.getIdForDom())]);

                    //                    console.log("ID:" + ID_TREE_LINK+entry.getIdForDom());

                    var open =  HtmlUtil.onClick(this.getGet()+".toggleEntryDetails('" + entry.getId()+ "');", 
                                                 arrow);




                    entryName = HtmlUtil.tag(TAG_A,[ATTR_HREF, entry.getEntryUrl()],entryName);
                    var left =   HtmlUtil.div([ATTR_STYLE," white-space: nowrap;  overflow-x:none; max-width:300px;"],open +" " + link +" " +  entryName);

                    var details = HtmlUtil.div([ATTR_ID,this.getDomId(ID_DETAILS+entry.getIdForDom()), ATTR_CLASS,this.getClass("details")],HtmlUtil.div([ATTR_CLASS,this.getClass("details-inner"),ATTR_ID,this.getDomId(ID_DETAILS_INNER+entry.getIdForDom())],""));

                    var line = HtmlUtil.leftRight(left,toolbar,"10","2");
                    //                    line = HtmlUtil.leftRight(left,toolbar,"60%","30%");

                    var mainLine = HtmlUtil.div([ATTR_ID, this.getDomId(ID_DETAILS_MAIN+ entry.getIdForDom()), ATTR_CLASS,this.getClass("entry-main"), "entryid",entry.getIdForDom()], line);
                    var line = HtmlUtil.div([ATTR_ID, this.getDomId("entryinner_" + entry.getIdForDom())], mainLine + details);
                    html  += HtmlUtil.tag(TAG_LI,[ATTR_ID,
                                                this.getDomId("entry_" + entry.getIdForDom()),
                                                  "entryid",entry.getIdForDom(), ATTR_CLASS,this.getClass("entry")+" ui-widget-content " + rowClass], line);
                    html  += "\n";
                }
                return html;
            }
        });
}


var  RamaddaListDisplay = RamaddaEntrylistDisplay;


function RamaddaGalleryDisplay(displayManager, id, properties) {
    var SUPER;
    RamaddaUtil.inherit(this, SUPER = new RamaddaSearcher(displayManager, id, DISPLAY_ENTRY_GALLERY, properties));
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
            entryListChanged: function(entryList) {
                if(this.multiSearch) {
                    this.multiSearch.count--;
                }
                SUPER.entryListChanged.apply(this,[entryList]);
                var entries = this.entryList.getEntries();

                if(entries.length==0) {
                    this.searchSettings.skip=0;
                    this.searchSettings.max=50;
                    var msg = "Nothing found";
                    if(this.multiSearch) {
                        if(this.multiSearch.count>0) {
                            msg = "Nothing found so far. Still searching " + this.multiSearch.count +" repositories";
                        } else {
                        }
                    }
                    this.writeHtml(ID_ENTRIES, this.getMessage(msg));
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

                //Don: this gets the gallery html then writes it out. 
                var html  = this.getEntriesGallery(entries);
                this.writeHtml(ID_ENTRIES, html);
                //Don: Do any final fancy box things here after the html is written out

                this.getDisplayManager().handleEventEntriesChanged(this, entries);
            },
            getEntriesGallery:function (entries) {
                var nonImageHtml = "";
                var html = "";
                var imageCnt = 0;
                var imageEntries = [];
                for(var i=0;i<entries.length;i++) {
                    var entry = entries[i];
                    //Don: Right now this just shows all of the images one after the other.
                    //If there is just one image we should just display it
                    //We should do a gallery here if more than 1
                    if(entry.isImage()) {
                        imageEntries.push(entry);
                        var link  =  HtmlUtil.tag(TAG_A,[ATTR_HREF, entry.getEntryUrl()],entry.getName());
                        imageCnt++;
                        html  += HtmlUtil.tag(TAG_IMG,["src", entry.getResourceUrl(), ATTR_WIDTH,"500",ATTR_ID,
                                                  this.getDomId("entry_" + entry.getIdForDom()),
                                                       "entryid",entry.getIdForDom(), ATTR_CLASS,"display-entrygallery-entry"]) +"<br>" +
                            link+"<p>";
                    } else  {
                        var icon = entry.getIconImage([ATTR_TITLE,"View entry"]);
                        var link  =  HtmlUtil.tag(TAG_A,[ATTR_HREF, entry.getEntryUrl()],icon+" "+ entry.getName());
                        nonImageHtml += link +"<br>";
                    }
                }


                if(imageCnt>1) {
                    //Show a  gallery instead
                    var newHtml = "";
                    for(var i=0;i<imageEntries.length;i++) {
                        var entry = imageEntries[i];
                        var link  =  HtmlUtil.tag(TAG_A,[ATTR_HREF, entry.getEntryUrl()],entry.getName());
                        //Don: right now I just replicate what I do above
                        newHtml  += HtmlUtil.tag(TAG_IMG,["src", entry.getResourceUrl(), ATTR_WIDTH,"500",ATTR_ID,
                                                          this.getDomId("entry_" + entry.getIdForDom()),
                                                          "entryid",entry.getIdForDom(), ATTR_CLASS,"display-entrygallery-entry"]) +"<br>" +
                            link+"<p>";

                    }
                    html = newHtml;
                }

                //append the links to the non image entries
                if(nonImageHtml!="") {
                    if(imageCnt>0) {
                        html += "<hr>";
                    }
                    html += nonImageHtml;
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
                html += HtmlUtil.openTag(TAG_TABLE,[ATTR_CLASS,"display-metadata-table",ATTR_WIDTH,"100%","cellpadding", "5","cellspacing","0"]);
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

                var skip ={
                    "content.pagestyle": true,
                    "content.pagetemplate": true,
                    "content.sort": true,
                    "spatial.polygon":true,
                };
                var headerItems = [];
                headerItems.push(HtmlUtil.th([ATTR_CLASS, "display-metadata-table-cell"],HtmlUtil.b(typeName)));
                for(var i=0;i<mdts.length;i++) {
                    var type = mdts[i];
                    if(skip[type]) {
                        continue;
                    }
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
                    var buttonId = this.getDomId("entrylink" + entry.getIdForDom());
                    //                    var link =  HtmlUtil.onClick(this.getGet()+".showEntryDetails(event, '" + entry.getIdForDom() +"','" + buttonId +"',true);", 
                    //                                                 entry.getIconImage() +" " + entry.getName(),[ATTR_ID,buttonId,ATTR_CLASS,"display-metadata-link"]);
                    var link =  entry.getLink(entry.getIconImage() +" " + entry.getName());
                    row.push(HtmlUtil.td([ATTR_CLASS, "display-metadata-table-cell"],HtmlUtil.div([ATTR_CLASS,"display-metadata-entrylink"], link)));
                    for(var mdtIdx=0;mdtIdx<mdts.length;mdtIdx++) {
                        var mdt = mdts[mdtIdx];
                        if(skip[mdt]) {
                            continue;
                        }
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
                                if(m.type == "content.thumbnail" || m.type == "content.logo") {
                                    var url =this.getRamadda().getRoot() +"/metadata/view/" + m.attr1 +"?element=1&entryid=" + entry.getId() +"&metadata.id=" + m.id;
                                    item =  HtmlUtil.image(url,[ATTR_WIDTH,"100"]);
                                } else if(m.type == "content.url") {
                                    var label = m.attr2;
                                    if(label == null || label == "") {
                                        label = m.attr1;
                                    }
                                    item =  HtmlUtil.href(m.attr1,label);
                                } else if(m.type == "content.attachment") {
                                    var toks = m.attr1.split("_file_");
                                    var filename = toks[1];
                                    var url =this.getRamadda().getRoot()+"/metadata/view/" + m.attr1 +"?element=1&entryid=" + entry.getId() +"&metadata.id=" + m.id;
                                    item =  HtmlUtil.href(url,filename);
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
                        var add = HtmlUtil.tag(TAG_A, [ATTR_STYLE,"color:#000;", ATTR_HREF, this.getRamadda().getRoot() + "/metadata/addform?entryid=" + entry.getId() +"&metadata.type=" + mdt,
                                                     "target","_blank","alt","Add metadata",ATTR_TITLE,"Add metadata"],"+");
                        var cellContents = add;
                        if(cell.length>0) 
                            cellContents = cell+divider + add;
                        row.push(HtmlUtil.td([ATTR_CLASS, "display-metadata-table-cell"],HtmlUtil.div([ATTR_CLASS,"display-metadata-table-cell-contents"], cellContents)));
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
    $.extend(this, {sourceEntry: properties.sourceEntry});
    RamaddaUtil.inherit(this, SUPER = new RamaddaDisplay(displayManager, id, DISPLAY_ENTRYDISPLAY, properties));
    if(properties.sourceEntry == null && properties.entryId!=null) {
        var _this = this;
        var callback  = function(entries) {
            var entry = entries[0];
            _this.sourceEntry = entry;
            _this.initDisplay();
        }
        properties.sourceEntry = this.getEntry(properties.entryId, callback);
    }


    addRamaddaDisplay(this);
    $.extend(this, {
            selectedEntry: null,
            initDisplay: function() {
                this.initUI();
                if(this.sourceEntry!=null) {
                    this.addEntryHtml(this.sourceEntry);
                    var url = this.sourceEntry.getEntryUrl();
                    var title = HtmlUtil.href(url, this.sourceEntry.getName());
                    console.log("set title:" + title);
                    this.setTitle(title);
                } else {
                    this.addEntryHtml(this.selectedEntry);
                    this.setTitle("Entry Display");
                }
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
                this.baseUrl = this.getRamadda().getSearchUrl(this.searchSettings, OUTPUT_JSON);
                this.entryList = new EntryList(this.getRamadda(), jsonUrl, this);
                var html = "";
                html += HtmlUtil.div([ATTR_ID,this.getDomId(ID_ENTRIES),ATTR_CLASS,this.getClass("entries")], "");
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
                var entry1 = this.getEntry(this.jq(ID_SELECT1).val());
                var entry2 = this.getEntry(this.jq(ID_SELECT2).val());
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


function RamaddaRepositoriesDisplay(displayManager, id, properties) {
    RamaddaUtil.inherit(this, new RamaddaEntryDisplay(displayManager, id, DISPLAY_REPOSITORIES, properties));
    addRamaddaDisplay(this);
    RamaddaUtil.defineMembers(this, {
            initDisplay: function() {
                var theDisplay = this;
                this.initUI();
                var html = "";
                if(this.ramaddas.length==0) {
                    html += this.getMessage("No repositories specified");
                } else {
                    html += this.getMessage("Loading repository listing");
                }
                this.numberWithTypes = 0;
                this.finishedInitDisplay = false;
                for(var i=0;i<this.ramaddas.length;i++) {
                    if(i == 0) {
                    }
                    var ramadda = this.ramaddas[i];
                    var types = ramadda.getEntryTypes(function(ramadda, types) {theDisplay.gotTypes(ramadda, types);});
                    if(types !=null) {
                        this.numberWithTypes++;
                    }
                }
                this.setTitle("Repositories");
                this.setContents(html);
                this.finishedInitDisplay = true;
                this.displayRepositories();
            },
            displayRepositories: function() {
                //                console.log("displayRepositories " + this.numberWithTypes + " " + this.ramaddas.length);
                if(!this.finishedInitDisplay || this.numberWithTypes != this.ramaddas.length) {
                    return;
                }
                var typeMap = {};
                var allTypes = [];
                var html = "";
                html += HtmlUtil.openTag(TAG_TABLE, [ATTR_CLASS, "display-repositories-table",ATTR_WIDTH,"100%",ATTR_BORDER,"1","cellspacing","0","cellpadding","5"]);
                for(var i=0;i<this.ramaddas.length;i++) {
                    var ramadda = this.ramaddas[i]; 
                   var types = ramadda.getEntryTypes();
                    for(var typeIdx=0;typeIdx<types.length;typeIdx++) {
                        var type = types[typeIdx];
                        if(typeMap[type.getId()] == null) {
                            typeMap[type.getId()] = type;
                            allTypes.push(type);
                        }
                    }
                }

                html += HtmlUtil.openTag(TAG_TR, ["valign", "bottom"]);
                html += HtmlUtil.th([ATTR_CLASS,"display-repositories-table-header"],"Type");
                for(var i=0;i<this.ramaddas.length;i++) {
                    var ramadda = this.ramaddas[i];
                    var link = HtmlUtil.href(ramadda.getRoot(),ramadda.getName());
                    html += HtmlUtil.th([ATTR_CLASS,"display-repositories-table-header"],link);
                }
                html += "</tr>";

                var onlyCats = [];
                if(this.categories!=null) {
                    onlyCats = this.categories.split(",");
                }



                var catMap = {};
                var cats = [];
                for(var typeIdx =0;typeIdx<allTypes.length;typeIdx++) {
                    var type = allTypes[typeIdx];
                    

                    var row = "";


                    row += "<tr>";
                    row += HtmlUtil.td([],HtmlUtil.image(type.getIcon()) +" " + type.getLabel());
                    for(var i=0;i<this.ramaddas.length;i++) {
                        var ramadda = this.ramaddas[i];
                        var repoType = ramadda.getEntryType(type.getId());
                        var col = "";
                        if(repoType == null) {
                            row += HtmlUtil.td([ATTR_CLASS,"display-repositories-table-type-hasnot"],"");
                        } else {
                            var label  =
                                HtmlUtil.tag(TAG_A, ["href", ramadda.getRoot()+"/search/type/" + repoType.getId(),"target","_blank"],
                                             repoType.getEntryCount());
                            row += HtmlUtil.td([ATTR_ALIGN, "right", ATTR_CLASS,"display-repositories-table-type-has"],label);
                        }

                    }
                    row += "</tr>";

                    var catRows = catMap[type.getCategory()];
                    if(catRows == null) {
                        catRows = [];
                        catMap[type.getCategory()] = catRows;
                        cats.push(type.getCategory());
                    }
                    catRows.push(row);
                }

                for(var i=0;i<cats.length;i++) {
                    var cat = cats[i];
                    if(onlyCats.length>0) {
                        var ok = false;
                        for(var patternIdx=0;patternIdx<onlyCats.length;patternIdx++) {
                            if(cat == onlyCats[patternIdx]) {
                                ok = true;
                                break;
                            }
                            if(cat.match(onlyCats[patternIdx])) {
                                ok = true;
                                break;
                                
                            }
                        }
                        if(!ok) continue;

                    }
                    var rows = catMap[cat];
                    html +=  "<tr>";
                    html += HtmlUtil.th(["colspan", ""+(1 + this.ramaddas.length)], cat);
                    html +=  "</tr>";
                    for(var row=0;row<rows.length;row++) {
                        html += rows[row];
                    }

                }


                html += HtmlUtil.closeTag(HtmlUtil.TAG_TABLE);
                this.setContents(html);
            },
            gotTypes: function(ramadda,  types) {
                this.numberWithTypes++;
                this.displayRepositories();
            }
        });
}

