
var OUTPUT_JSON = "json";
var OUTPUT_CSV = "default.csv";
var OUTPUT_ZIP = "zip.tree";
var OUTPUT_EXPORT = "zip.export";

var OUTPUTS = [
               {id: OUTPUT_ZIP, name:  "Download Zip"},
               {id: OUTPUT_EXPORT, name:  "Export"},
               {id: OUTPUT_JSON, name:  "JSON"},
               {id: OUTPUT_CSV, name:  "CSV"},
               ];

//
//return the global entry manager with the given id, null if not found
//
function getRamadda(baseUrl) {
    //check for the embed label
    var toks = baseUrl.split(";");
    var name = null;
    if(toks.length>1) {
        baseUrl = toks[0];
        name = toks[1];
    }

    if(baseUrl == "this") {
        return getGlobalRamadda();
    }

    if(window.globalRamaddas==null) {
        window.globalRamaddas = {};
    }
    var manager =  window.globalRamaddas[baseUrl];
    if(manager == null) {
        //        console.log("new ramadda:" + baseUrl);
        manager = new Ramadda(baseUrl);
        if(name!=null) {
            manager.name = name;
        }
        window.globalRamaddas[baseUrl] = manager;
    }
    return manager;
}

function addRepository(repository) {
    if(window.globalRamaddas==null) {
        window.globalRamaddas = {};
    }
    window.globalRamaddas[repository.repositoryRoot] = repository;
}

function getGlobalRamadda() {
    return getRamadda(ramaddaBaseUrl);
}



function Ramadda(repositoryRoot, isContainer) {
    if(repositoryRoot == "all" && !isContainer) {
    }

    if(repositoryRoot == null) {
        repositoryRoot = ramaddaBaseUrl;
    }

    //    console.log("root:" + repositoryRoot);
    var hostname = null;
    var match =  repositoryRoot.match("^(http.?://[^/]+)/");
    if(match && match.length>0) {
        hostname = match[1];
    } else {
        //        console.log("no match");
    }
    //    console.log("hostname:" + hostname);

    RamaddaUtil.defineMembers(this, {
            repositoryRoot:repositoryRoot,
            hostname: hostname,
            name: null,
            entryCache: {},
            entryTypes: null,
            entryTypeMap: {},
            addRepository: function(repository) {
                if(this.children==null) {
                    this.children = [];
                }
                this.children.push(repository);
            },
            getHostname: function() {
                return this.hostname;
            },
            canSearch: function() {
                return this.children==null;
            },
            getName: function() {
                if(this.children) { return "Search all repositories";}
                if(this.name!=null) return this.name;
                if(this.repositoryRoot.indexOf("/") == 0)  {
                    return this.name  = "This RAMADDA";
                }
                var url  = this.repositoryRoot;
                //Do the a trick
                var parser = document.createElement('a');
                parser.href = url;
                var host   = parser.hostname;
                var path = parser.pathname;
                //if its the default then just return the host;
                if(path == "/repository") return host;
                return this.name = host+": " + path;
            },
            getId: function() {
                return this.repositoryRoot;
            },
            getRoot: function() {
                return this.repositoryRoot;
            },
            getJsonUrl: function(entryId) {
                return this.repositoryRoot + "/entry/show?entryid=" + entryId +"&output=json";
            },
            getEntryType: function(typeId) {
                return this.entryTypeMap[typeId];
            },
            getEntryTypes: function(callback) {
                if(this.entryTypes != null) {
                    return this.entryTypes;
                }
                if(this.children!=null) {
                    if(this.entryTypes == null) {
                        this.entryTypes = [];
                    }
                    var seen = {};
                    for(var i =0;i<this.children.length;i++) {
                        var types = this.children[i].getEntryTypes();
                        if(types == null) continue;
                        for(var j =0;j<types.length;j++) {
                            var type = types[j];
                            if(seen[type.getId()] == null) {
                                var newType = {};
                                newType = $.extend(newType, type);
                                seen[type.getId()] = newType;
                                this.entryTypes.push(newType);
                            } else {
                                seen[type.getId()].entryCount+= type.getEntryCount();
                            }
                        }
                    }
                    return this.entryTypes;
                }


                if(this.entryTypes == null) {
                    var theRamadda = this;
                    var url = this.repositoryRoot +"/entry/types";
                    //                    console.log(this.repositoryRoot +" fetching: " + url +" children:" + this.children);
                    var jqxhr = $.getJSON(url, function(data) {
                            if(GuiUtils.isJsonError(data)) {
                                return;
                            }
                            theRamadda.entryTypes = [];
                            for(var i =0;i<data.length;i++) {
                                var type = new EntryType(data[i]);
                                theRamadda.entryTypeMap[type.getId()] = type;
                                theRamadda.entryTypes.push(type);
                            }
                            if(callback!=null) {
                                callback(theRamadda, theRamadda.entryTypes);
                            }
                        }).done(function(jqxhr, textStatus, error) {
                                //                                console.log("JSON done:" +textStatus);
                        }).always(function(jqxhr, textStatus, error) {
                                //                                console.log("Always:" +textStatus);
                        }).fail(function(jqxhr, textStatus, error) {
                            var err = textStatus + " --  " + error;
                            GuiUtils.handleError("entry error 1:" + err, url);
                            });
                }
                return this.entryTypes;
            },
            getMetadataCount: function(type, callback) {
                var url  = this.repositoryRoot +"/metadata/list?metadata.type=" + type.getType() +"&response=json";
                //                console.log("getMetadata:" + type.getType() + " URL:" + url);
                var jqxhr = $.getJSON(url, function(data) {
                        if(GuiUtils.isJsonError(data)) {
                            return;
                        }
                        callback(type, data);
                    })
                    .fail(function(jqxhr, textStatus, error) {
                            var err = textStatus + ", " + error;
                            GuiUtils.handleError("entry error 2:" +err, url);
                        });
                return null;
            },
            getSearchLinks: function(searchSettings) {
                var urls = [];
                for(var i =0;i<OUTPUTS.length;i++) {
                    urls.push(HtmlUtil.href(this.getSearchUrl(searchSettings, OUTPUTS[i].id),
                                            OUTPUTS[i].name));
                }
                return urls;
            },
           getSearchUrl: function(settings, output) {
                var url =  this.repositoryRoot +"/search/do?output=" +output;
                for(var i =0;i<settings.types.length;i++) {
                    var type = settings.types[i];
                    url += "&type=" + type;
                }
                if(settings.parent!=null&& settings.parent.length>0) 
                    url += "&group=" + settings.parent;
                if(settings.text!=null&& settings.text.length>0) 
                    url += "&text=" + settings.text;
                if(settings.name!=null&& settings.name.length>0) 
                    url += "&name=" + settings.name;
                if(settings.startDate && settings.startDate.length>0) {
                    url += "&starttime=" + settings.startDate;
                }
                if(settings.endDate && settings.endDate.length>0) {
                    url += "&endtime=" + settings.endDate;
                }
                if(!isNaN(settings.getNorth())) 
                   url += "&maxlatitude=" + settings.getNorth();
                if(!isNaN(settings.getWest())) 
                   url += "&minlongitude=" + settings.getWest();
                if(!isNaN(settings.getSouth())) 
                   url += "&minlatitude=" + settings.getSouth();
                if(!isNaN(settings.getEast())) 
                   url += "&maxlongitude=" + settings.getEast();

                for(var i =0;i<settings.metadata.length;i++) {
                    var metadata = settings.metadata[i];
                    url += "&metadata.attr1." + metadata.type + "=" + metadata.value;
                }
                url += "&max=" + settings.getMax();
                url += "&skip=" + settings.getSkip();
                url += settings.getExtra();
                return url;
            },

            addEntry: function(entry) {
                this.entryCache[entry.getId()] = entry;
            },
            getEntry: function(id, callback) {
                var entry = this.entryCache[id];
                if(entry!=null)  {
                    return entry;
                }
                //Check any others
                if(window.globalRamaddas) {
                    for(var i=0;i<window.globalRamaddas.length;i++) {
                        var em = window.globalRamaddas[i];
                        var entry = em.entryCache[id];
                        if(entry!=null)  {
                            return entry;
                        }
                    }
                }

                if(callback==null) {
                    return null;
                }
                var ramadda = this;
                var jsonUrl = this.getJsonUrl(id);
                var jqxhr = $.getJSON( jsonUrl, function(data) {
                        if(GuiUtils.isJsonError(data)) {
                            return;
                        }
                        var entryList =  createEntriesFromJson(data, ramadda);
                        callback.call(null, entryList);
                    })
                    .fail(function(jqxhr, textStatus, error) {
                            var err = textStatus + ", " + error;
                            GuiUtils.handleError("entry error 3:" +err, jsonUrl);
                        });
                return null;
            }
        });

    if(!isContainer) {
        this.getEntryTypes();
    }
}










function createEntriesFromJson(data, ramadda) {
    var entries = new Array();
    if(ramadda==null) {
        ramadda = getGlobalRamadda();
    }
    for(var i=0;i<data.length;i++)  {
        var entryData = data[i];
        entryData.baseUrl = ramadda.getRoot();
        var entry = new Entry(entryData);
        ramadda.addEntry(entry);
        entries.push(entry);
    }
    return entries;
}


function MetadataType(type, label,value) {
    $.extend(this, {type:type,label:label,value:value});
    $.extend(this, {
            getType: function() {return this.type;},
            getLabel: function() {if(this.label!=null) return this.label; return this.type;},
            getValue: function() {return this.value;},
        });
}


function EntryTypeColumn(props) {
    $.extend(this, props);
    RamaddaUtil.defineMembers(this, {
            getName: function() {return this.name;},
            getLabel: function() {return this.label;},
            getType: function() {return this.type;},
            getValues: function() {return this.values;},
            getSuffix: function() {return this.suffix;},
            getSearchArg: function() {return "search." + this.namespace +"." + this.name;},
            getCanSearch: function() {return this.cansearch;},
            getCanShow: function() {return this.canshow;},
            isEnumeration: function() {return this.getType() == "enumeration" || this.getType() == "enumerationplus";},
            isUrl: function() {return this.getType() == "url"},
        });
}

function EntryType(props) {
    //Make the Columns
    var columns = props.columns;
    if(columns == null) columns = [];
    var myColumns = [];
    for(var i=0;i<columns.length;i++) {
        myColumns.push(new EntryTypeColumn(columns[i]));
    }
    props.columns = myColumns;
    $.extend(this, props);

    RamaddaUtil.defineMembers(this, {
            getIsGroup: function() {return this.isgroup;},
            getIcon: function() {return this.icon;},
            getLabel: function() {return this.label;},
                getId: function() {
                if(this.type!=null) return this.type;
                return this.id;
            },
            getCategory: function() {return this.category;},
            getEntryCount: function() {return this.entryCount;},
            getColumns: function() {return this.columns;},
        });
}

function Entry(props) {
    if(props.baseUrl == null) props.baseUrl = ramaddaBaseUrl;
    var NONGEO = -9999;
    if(props.type) props.type = new EntryType(props.type);
    $.extend(this, {
            latitude: NaN,
            longitude: NaN,
            north: NaN,
            west: NaN,
            south: NaN,
            east: NaN,
            services: [],
            metadata: [],
            childrenEntries: null,
        });

    RamaddaUtil.inherit(this,  props);
    RamaddaUtil.defineMembers(this, {
            getId : function () {
                return  this.id;
            },
            getFullId: function() {
                return this.getRamadda().getRoot() +"," + this.id;
            },
            getIsGroup: function() {return this.isGroup;},
            getChildrenEntries: function(callback, extraArgs) {
                if(this.childrenEntries !=null) {
                    return this.childrenEntries;
                }
                var theEntry =this;

                var settings = new  EntrySearchSettings({parent: this.getId()});
                var jsonUrl = this.getRamadda().getSearchUrl(settings, OUTPUT_JSON);
                var jsonUrl =  this.getRamadda().getJsonUrl(this.getId()) +"&justchildren=true";
                if(extraArgs!=null) {
                    jsonUrl += "&" + extraArgs;
                }

                console.log(jsonUrl);

                var myCallback = {
                    entryListChanged: function(list) {

                        callback(list.getEntries());
                    }
                };
                var entryList = new EntryList(this.getRamadda(), jsonUrl, myCallback, true);
                return null;
            },
            getType: function() {
                return this.type;
            },
            getMetadata: function() {
                return this.metadata;
            },
            getRamadda: function() {
                return getRamadda(this.baseUrl);
            },
            getLocationLabel: function() {
                return "n: " + this.north + " w:" + this.west + " s:" + this.south +" e:" + this.east;
            },
            getServices: function() {
                return this.services;
            },
            getService: function(relType) {
                for(var i =0;i<this.services.length;i++) {
                    if(this.services[i].relType == relType) return this.services[i];
                }
                return null;
            },
            goodLoc: function(v) {
                return !isNaN(v) && v != NONGEO;
            },
            hasBounds: function() {
                return this.goodLoc(this.north) && this.goodLoc(this.west) && this.goodLoc(this.south) && this.goodLoc(this.east);
            },
            hasLocation: function() {
                return this.goodLoc(this.north);
            },
            getNorth: function() {
                return this.north;
            },
            getWest: function() {
                return this.west;
            },
            getSouth: function() {
                return this.south;
            },
            getEast: function() {
                return this.east;
            },
            getLatitude: function() {
                return this.north;
            },
            getLongitude: function() {
                return this.west;
            },
            getIconUrl : function () {
                if(this.icon==null) {
                    return this.getRamadda().getRoot() + "/icons/page.png";
                }
                var url;
                var hostname = this.getRamadda().getHostname();
                if(hostname)
                    url =  hostname + this.icon;
                else 
                    url =  this.icon;
                //this.getRamadda().getRoot() + 
                return url;
            },
            getIconImage : function (attrs) {
                return HtmlUtil.image(this.getIconUrl(),attrs);
            },
            getColumns : function () {
                return this.type.getColumns();
            },
            getColumnValue : function (name) {
                var value = this["column." + name];
                return value;
            },
            getColumnNames : function () {
                var names =  [];
                for(var i=0;i<this.type.columns.length;i++) {
                    names.push(this.type.columns[i].getName());
                }
                return names;
            },
            getColumnLabels : function () {
                var labels =  [];
                for(var i=0;i<this.type.columns.length;i++) {
                    labels.push(this.type.columns[i].getLabel());
                }
                return labels;
            },
            getName : function () {
                if(this.name ==null || this.name == "") {
                    return "no name";
                }
                return this.name;
            },
            getDescription : function (dflt) {
                if(this.description == null) return dflt;
                return this.description;
            },
            getFilesize : function () {
                var size =  parseInt(this.filesize);
                if(size == size) return size;
                return 0;
            },
            getFormattedFilesize : function () {
                return GuiUtils.size_format(this.getFilesize());
            },
            getEntryUrl : function (extraArgs) {
                var url =   this.getRamadda().getRoot() + "/entry/show?entryid=" + this.id;
                if(extraArgs!=null) {
                    if (!StringUtil.startsWith(extraArgs, "&")) {
                      url += "&";
                    }
                    url += extraArgs;
                }
                return url;
            },
            getFilename : function () {
                return this.filename;
            }, 
            getResourceUrl : function () {
                var rurl = this.getRamadda().getRoot() + "/entry/get";
                if (this.getFilename() != null) {
                    rurl += "/"+ this.getFilename();
                }
                return  rurl + "?entryid=" + this.id;
            },
            getLink : function (label) {
                if(!label) label = this.getName();
                return  HtmlUtil.tag("a",["href", this.getEntryUrl()],label);
            },
            getResourceLink : function (label) {
                if(!label) label = this.getName();
                return  HtmlUtil.tag("a",["href", this.getResourceUrl()],label);
            },
            toString: function() {
                return "entry:" + this.getName();
            }
        });
}



function EntryList(repository, jsonUrl, listener, doSearch) {
    $.extend(this, {
            repository: repository,
            url: jsonUrl,
            listener : listener,
            haveLoaded : false,
            divId : null,
            entries :[],
            map: {},
            getRepository: function() {
                return this.repository;
            },
            getEntry : function(id) {
                var entry =  this.map[id];
                if(entry!=null) return entry;
                return this.getRepository().getEntry(id);
            },
            getEntries : function() {
                return this.entries;
            },
            setHtml: function(html) {
                if(this.divId == null) return;
                $("#" + this.divId).html(html);
            },
            initDisplay: function(divId) {
                var html;
                this.divId = divId;
                if(this.entries.length==0)  {
                    if(this.haveLoaded) {
                        html = "No entries";
                    } else {
                        html = "Loading...";
                    }
                } else {
                    html = getHtml();
                }
                this.setHtml(html);
            },
            getHtml: function() {
                var html = "";
                for(var i=0;i<this.entries.length;i++) {
                    var entry = this.entries[i];
                    html += "<div class=entry-list-entry>";
                    html+= entry.getName();
                    html += "</div>"; 
               }
                return html;
            },
            createEntries: function(data,listener ) {
                this.entries =   createEntriesFromJson(data, this.getRepository());
                for(var i =0;i<this.entries.length;i++) {
                    var entry = this.entries[i];
                    this.map[entry.getId()] = entry;
                }
                if(listener == null) {
                    listener = this.listener;
                }
                if(listener) {
                   listener.entryListChanged(this);
                }
            },
            doSearch: function(listener) {
                if(listener == null) {
                    listener = this.listener;
                }
                var _this = this;
                console.log("json:" + this.url);
                var jqxhr = $.getJSON( this.url, function(data) {
                        if(GuiUtils.isJsonError(data)) {
                            return;
                        }
                        _this.haveLoaded = true;
                        _this.createEntries(data, listener);
                    })
            .fail(function(jqxhr, textStatus, error) {
                    console.log(textStatus + ", " + error);
                    var err = "Unable to complete request.";
                    GuiUtils.handleError("entry error 4:" +err, _this.url);
                });
            }
        });

    if(doSearch) {
        this.doSearch();
    }
}




function EntrySearchSettings(props) {
    $.extend(this, {
            types: [],
            parent: null,
            max: 50,
            skip: 0,
            metadata: [],
            extra:"",
            startDate: null,
                endDate: null,
                north: NaN,
                west: NaN,
                north: NaN,
                east: NaN,
            getMax: function() {
                return this.max;
            },
            getSkip: function() {
                return this.skip;
            },
            toString: function() { return "n:" + this.north +" w:" + this.west +" s:" + this.south +" e:"  + this.east;},
            getNorth: function() {return this.north;},
            getSouth: function() {return this.south;},
            getWest: function() {return this.west;},
            getEast: function() {return this.east;},
                setDateRange: function(start, end) {
                this.startDate = start;
                this.endDate = end;
            },

            setBounds: function(north, west, south, east) {
                this.north = (north==null || north.toString().length==0?NaN:parseFloat(north));
                this.west = (west==null || west.toString().length==0?NaN:parseFloat(west));
                this.south = (south==null || south.toString().length==0?NaN:parseFloat(south));
                this.east = (east==null || east.toString().length==0?NaN:parseFloat(east));
            },
            getTypes: function() {
                return this.types;
            },
            hasType:function(type) {
                return this.types.indexOf(type)>=0;
            },
            getExtra: function() {
                return this.extra;
            },
            setExtra: function(extra) {
                this.extra = extra;
            },
            clearAndAddType:function(type) {
                this.types = [];
                this.addType(type);
                return this;
            },
            addType:function(type) {
                if(type == null || type.length ==0) return;
                if(this.hasType(type)) return;
                this.types.push(type);
                return this;
            }
     });
    if(props!=null) {
        $.extend(this,props);
    }
}


function EntryListHolder(ramadda) {
    var SUPER;
    RamaddaUtil.inherit(this, SUPER = new EntryList(ramadda, null));

    $.extend(this, {
            entryLists: [],
            addEntryList: function(e) {
                this.entryLists.push(e);
            },
            doSearch: function(listener) {
                var _this  = this;
                if(listener == null) {
                    listener = this.listener;
                }
                for(var i =0;i<this.entryLists.length;i++) {
                    var entryList = this.entryLists[i];
                    if(!entryList.getRepository().canSearch()) continue;
                    var callback = {
                        entryListChanged: function(entryList) {
                            if(listener) {
                                listener.entryListChanged(_this, entryList);
                            }
                        }
                    };
                    entryList.doSearch(callback);
                }
            },
            getEntry : function(id) {
                for(var i =0;i<this.entryLists.length;i++) {
                    var entryList = this.entryLists[i];
                    var entry =  entryList.getEntry(id);
                    if(entry!=null) {
                        return entry;
                    }
                }
                return  null;
            },
            getEntries : function() {
                var entries = [];
                for(var i =0;i<this.entryLists.length;i++) {
                    var sub = this.entryLists[i].getEntries();
                    for(var j =0;j<sub.length;j++) {
                        entries.push(sub[j]);
                    }
                }
                return entries;
            },});

}