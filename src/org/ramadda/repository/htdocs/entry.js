


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
//return the global display manager with the given id, null if not found
//
function getEntryManager(baseUrl) {
    if(window.globalEntryManagers==null) {
        window.globalEntryManagers = {};
    }
    var manager =  window.globalEntryManagers[baseUrl];
    if(manager == null) {
        manager = new EntryManager(baseUrl);
        window.globalEntryManagers[baseUrl] = manager;
    }
    return manager;
}

function getGlobalEntryManager() {
    return getEntryManager(ramaddaBaseUrl);
}



function EntryManager(repositoryRoot) {
    if(repositoryRoot == null) {
        repositoryRoot = ramaddaBaseUrl;
    }

    console.log("root:" + repositoryRoot);
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
            entryCache: {},
            entryTypes: null,
            getHostname: function() {
                return this.hostname;
            },
            getRoot: function() {
                return this.repositoryRoot;
            },
            getJsonUrl: function(entryId) {
                return this.repositoryRoot + "/entry/show?entryid=" + id +"&output=json";
            },
            getEntryTypes: function(callback) {
                if(this.entryTypes == null) {
                    var jqxhr = $.getJSON(this.repositoryRoot +"/entry/types", function(data) {
                            this.entryTypes = [];
                            for(var i =0;i<data.length;i++) {
                                this.entryTypes.push(new EntryType(data[i]));
                            }
                            if(callback!=null) {
                                callback(this.entryTypes);
                            }
                        }).done(function(jqxhr, textStatus, error) {
                                //                                console.log("JSON done:" +textStatus);
                        }).always(function(jqxhr, textStatus, error) {
                                //                                console.log("Always:" +textStatus);
                        }).fail(function(jqxhr, textStatus, error) {
                            var err = textStatus + " --  " + error;
                            console.log("JSON error:" +err);
                            });
                }
                return this.entryTypes;
            },
            getMetadataCount: function(type, callback) {
                var url  = this.repositoryRoot +"/metadata/list?metadata.type=" + type.getType() +"&response=json";
                console.log("getMetadata:" + type.getType() + " URL:" + url);
                var jqxhr = $.getJSON(url, function(data) {
                        callback(type, data);
                    })
                    .fail(function(jqxhr, textStatus, error) {
                            var err = textStatus + ", " + error;
                            //                            alert("JSON error:" + err);
                            console.log("JSON error:" +err);
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
                    url += "&datadata.from=" + settings.startDate;
                }
                if(settings.endDate && settings.endDate.length>0) {
                    url += "&datadata.to=" + settings.endDate;
                }
                if(!isNaN(settings.getNorth())) 
                   url += "&area_north=" + settings.getNorth();
                if(!isNaN(settings.getWest())) 
                   url += "&area_west=" + settings.getWest();
                if(!isNaN(settings.getSouth())) 
                   url += "&area_south=" + settings.getSouth();
                if(!isNaN(settings.getEast())) 
                   url += "&area_east=" + settings.getEast();

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
                if(window.globalEntryManagers) {
                    for(var i=0;i<window.globalEntryManagers.length;i++) {
                        var em = window.globalEntryManagers[i];
                        var entry = em.entryCache[id];
                        if(entry!=null)  {
                            return entry;
                        }
                    }
                }


                if(callback==null) {
                    return null;
                }
                var entryManager = this;
                var jsonUrl = this.getJsonUrl(id);
                var jqxhr = $.getJSON( jsonUrl, function(data) {
                        var entryList =  createEntriesFromJson(data, entryManager);
                        console.log("got entry:" + entryList);
                        callback.call(data);
                    })
                    .fail(function(jqxhr, textStatus, error) {
                            var err = textStatus + ", " + error;
                            alert("JSON error:" + err);
                            console.log("JSON error:" +err);
                        });
                return null;
            }
        });

    this.getEntryTypes();
}










function createEntriesFromJson(data, entryManager) {
    var entries = new Array();
    for(var i=0;i<data.length;i++)  {
        var entryData = data[i];
        if(entryManager!=null) {
            entryData.baseUrl = entryManager.getRoot();
        }
        var entry = new Entry(entryData);
        getGlobalEntryManager().addEntry(entry);
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
    if(columns == null) colunms = [];
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
            getId: function() {return this.type;},
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
        });

    RamaddaUtil.inherit(this,  props);
    RamaddaUtil.defineMembers(this, {
            getId : function () {
                return  this.id;
            },
            getType: function() {
                return this.type;
            },
            getMetadata: function() {
                return this.metadata;
            },
            getEntryManager: function() {
                return getEntryManager(this.baseUrl);
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
                    return this.getEntryManager().getRoot() + "/icons/page.png";
                }
                var url;
                var hostname = this.getEntryManager().getHostname();
                if(hostname)
                    url =  hostname + this.icon;
                else 
                    url =  this.getEntryManager().getRoot() + this.icon;
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
                for(var i=0;i<this.columns.length;i++) {
                    names.push(this.columns[i].getName());
                }
                return names;
            },
            getColumnLabels : function () {
                var labels =  [];
                for(var i=0;i<this.columns.length;i++) {
                    labels.push(this.columns[i].getLabel());
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
            toString: function() {
                return "entry:" + this.getName();
            },
            getEntryUrl : function () {
                return  this.getEntryManager().getRoot() + "/entry/show?entryid=" + this.id;
            },
            getFilename : function () {
                return this.filename;
            }, 
            getFileUrl : function () {
                return  this.getEntryManager().getRoot() + "/entry/get?entryid=" + this.id;
            },
            getLink : function (label) {
                if(!label) label = this.getName();
                return  HtmlUtil.tag("a",["href", this.getEntryUrl()],label);
            },

            toString: function() {
                return "entry:" + this.getName();
            }
        });
}



function EntryList(entryManager, jsonUrl, listener) {
    this.entryManager = entryManager;
    var entryList = this;

    $.extend(this, {
            haveLoaded : false,
            divId : null,
            entries :[],
            map: {},
            listener : listener,
            getEntry : function(id) {
                return this.map[id];
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
                    if(entryList.haveLoaded) {
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
            createEntries: function(data) {
                this.entries =         createEntriesFromJson(data, this.entryManager);
                for(var i =0;i<this.entries.length;i++) {
                    var entry = this.entries[i];
                    this.map[entry.getId()] = entry;
                }
                if(this.listener) {
                    this.listener.entryListChanged(this);
                }
            }
            });

    this.url = jsonUrl;
    console.log("json:" + jsonUrl);
    var jqxhr = $.getJSON( jsonUrl, function(data) {
            entryList.haveLoaded = true;
            entryList.createEntries(data);
        })
        .fail(function(jqxhr, textStatus, error) {
                var err = textStatus + ", " + error;
                alert("JSON error:" + err);
                console.log("JSON error:" +err);
            });
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
