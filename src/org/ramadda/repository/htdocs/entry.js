
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
function getRamadda(id) {

    /*
      OpenSearch(http://asdasdsadsds);sdsadasdsa,...
     */

    //check for the embed label
    var toks = id.split(";");
    var name = null;
    if(toks.length>1) {
        id = toks[0].trim();
        name = toks[1].trim();
    }

    var extraArgs = null;
    var regexp = new RegExp("^(.*)\\((.+)\\).*$");
    var args = regexp.exec(id);
    if(args) {
        id = args[1].trim();
        extraArgs = args[2];
    }

    if(id == "this") {
        return getGlobalRamadda();
    }

    if(window.globalRamaddas==null) {
        window.globalRamaddas = {};
    }
    var repo =  window.globalRamaddas[id];
    if(repo!=null) {
        return repo;
    }


    //See if its a js class
    var func = window[id];
    if(func == null) {
        func = window[id+"Repository"];
    }

    if(func) {
        repo =   new Object();
        func.call(repo,name, extraArgs);
        //eval(" new " + id+"();");
    }


    if(repo == null) {
        repo = new Ramadda(id);
        if(name!=null) {
            repo.name = name;
        }
    }



    addRepository(repo);
    return repo;
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



function Repository(repositoryRoot) {
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
            getSearchMessage: function() {
                return "Searching " + this.getName();
            },
            getSearchLinks: function(searchSettings) {
                return null;
            },
            getSearchUrl: function(settings) {
                return null;
            },
            getId: function() {
                return this.repositoryRoot;
            },
            getIconUrl: function(entry) {
                return ramaddaBaseUrl +"/icons/page.png";
            },
            getEntryTypes: function(callback) {
                return new Array();
            },
            getMetadataCount: function(type, callback) {
                //                console.log("getMetatataCount:" + type.name);
                return 0;
            },
            getEntryUrl : function (entry, extraArgs) {
                return null;
            },

            getRoot: function() {
                return this.repositoryRoot;
            },
            getHostname: function() {
                return this.hostname;
            },
            canSearch: function() {
                return true;
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

        });


}


function RepositoryContainer(id, name) {
    this._id = "CONTAINER";
    var SUPER;
    RamaddaUtil.inherit(this, SUPER = new Repository(id));
    RamaddaUtil.defineMembers(this, {
            name: name,
            children: [],
            canSearch: function() {
                return false;
            },
            getSearchMessage: function() {
                return "Searching " + this.children.length +" repositories";
            },
            addRepository: function(repository) {
                this.children.push(repository);
            },
            getEntryTypes: function(callback) {
                if(this.entryTypes != null) {
                    return this.entryTypes;
                }
                this.entryTypes = [];
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

        });

}

function Ramadda(repositoryRoot) {
    if(repositoryRoot == null) {
        repositoryRoot = ramaddaBaseUrl;
    }
    var SUPER;
    RamaddaUtil.inherit(this, SUPER = new Repository(repositoryRoot));

    RamaddaUtil.defineMembers(this, {
            entryCache: {},
            entryTypes: null,
            entryTypeMap: {},
            canSearch: function() {
                return true;
            },
            getJsonUrl: function(entryId) {
                return this.repositoryRoot + "/entry/show?entryid=" + entryId +"&output=json";
            },
            createEntriesFromJson: function(data) {
                var entries = new Array();
                for(var i=0;i<data.length;i++)  {
                    var entryData = data[i];
                    entryData.baseUrl = this.getRoot();
                    var entry = new Entry(entryData);
                    this.addEntry(entry);
                    entries.push(entry);
                }
                return entries;
            },    
            getEntryType: function(typeId) {
                return this.entryTypeMap[typeId];
            },
            getEntryTypes: function(callback) {
                if(this.entryTypes == null) {
                    var theRamadda = this;
                    var url = this.repositoryRoot +"/entry/types";
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
                            GuiUtils.handleError("Error reading entry types:" + err, "URL:" + url);
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
            getEntryUrl : function (entry, extraArgs) {
                var url =   this.getRoot() + "/entry/show?entryid=" + entry.id;
                if(extraArgs!=null) {
                    if (!StringUtil.startsWith(extraArgs, "&")) {
                        url += "&";
                    }
                    url += extraArgs;
                }
                return url;
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

    this.getEntryTypes();
}

/**
This creates a list of Entry objects from the given JSON data. 
If the given ramadda is null then use the global
*/
function createEntriesFromJson(data, ramadda) {
    if(ramadda==null) {
        ramadda = getGlobalRamadda();
    }
    return ramadda.createEntriesFromJson(data);
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
    if(props.repositoryId == null) {
        props.repositoryId = props.baseUrl;
    }
    if(props.repositoryId == null) {
        props.repositoryId = ramaddaBaseUrl;
    }


    var NONGEO = -9999;
    if(props.type) props.type = new EntryType(props.type);
    $.extend(this, {
            id:null,
            name:null,
            description:null,
                //xxx
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
                return getRamadda(this.repositoryId);
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
                    return this.getRamadda().getIconUrl(this);
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
                if(this.type.getColumns() == null) {
                    return new Array();
                }
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
                if(this.url) return this.url;
                return this.getRamadda().getEntryUrl(this, extraArgs);
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
                var jqxhr = $.getJSON( this.url, function(data, status, jqxhr) {
                        if(GuiUtils.isJsonError(data)) {
                            return;
                        }
                        _this.haveLoaded = true;
                        _this.createEntries(data, listener);
                    })
                    .fail(function(jqxhr, textStatus, error) {
                            //                            console.log("ERROR:" + jqxhr.responseText);
                            GuiUtils.handleError("error doing search:" +error, _this.url);
                            console.log("listener:" + listener.handleSearchError);
                            if(listener.handleSearchError) {
                                listener.handleSearchError(_this.url,error);
                            }
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


/**
   {"id":"0fdc0daa-2535-4f89-9a36-6295ea8279f4",
"name":"Top",
"description":"<wiki>\r\nThis is an example RAMADDA repository highlighting some of its science data management facilities.\r\n\r\n\r\n\r\n\r\n\r\n{{tree details=false}}\r\n\r\n{{import entry=df59f025-7f30-494e-a8c8-3dd317a956ff }}\r\n\r\n\r\n\r\n\r\n\r\n\r\n\r\n\r\n",
"type":{"id":"group",
"name":"Folder"},
"isGroup":true,
"icon":"/repository/repos/data/icons/folderclosed.png",
"parent":null,
"user":"default",
"createDate":"2013-06-11 19:13:00",
"startDate":"2013-06-11 19:13:00",
"endDate":"2013-06-11 19:13:00",
"north":-9999,
"south":-9999,
"east":-9999,
"west":-9999,
"altitudeTop":-9999,
"altitudeBottom":-9999,
"services":[],
"columnNames":[],
"columnLabels":[],
"extraColumns":[],
"metadata":[{"id":"44ea1893-e433-490d-9b0c-5fd77af0ee6c",
"type":"content.sort",
"label":"Sort Order",
"attr1":"name",
"attr2":true,
"attr3":"-1",
"attr4":""}]}

*/