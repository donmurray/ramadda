

var OUTPUT_JSON = "json";
var OUTPUT_CSV = "default.csv";
var OUTPUT_ZIP = "zip.tree";
var OUTPUT_EXPORT = "zip.export";

var OUTPUTS = [
               {id: OUTPUT_JSON, name:  "JSON"},
               {id: OUTPUT_CSV, name:  "CSV"},
               {id: OUTPUT_ZIP, name:  "Zip Tree"},
               {id: OUTPUT_EXPORT, name:  "Export"}];

function EntryManager(repositoryRoot) {
    if(repositoryRoot == null) {
        repositoryRoot = root;
    }
    $.extend(this, {
            repositoryRoot:repositoryRoot,
            entryCache: {},
            entryTypes: null,
            getJsonUrl: function(entryId) {
                return this.repositoryRoot + "/entry/show?entryid=" + id +"&output=json";
            },
            getEntryTypes: function(callback) {
                if(this.entryTypes == null) {
                    var jqxhr = $.getJSON(this.repositoryRoot +"/entry/types", function(data) {
                            this.entryTypes = [];
                            for(var i in data) {
                                this.entryTypes.push(new EntryType(data[i]));
                            }
                            if(callback!=null) {
                                callback(this.entryTypes);
                            }
                        });
                }
                return this.entryTypes;
            },
            getMetadataCount: function(type, callback) {
                var url  = this.repositoryRoot +"/metadata/list?metadata.type=" + type.getType() +"&response=json";
                //                console.log("getMetadata:" + type.getType() + " URL:" + url);
                var jqxhr = $.getJSON(url, function(data) {
                        callback(type, data);
                    });
                    return null;
            },
            getSearchLinks: function(searchSettings) {
                var urls = [];
                for(var i in OUTPUTS) {
                    urls.push(htmlUtil.href(this.getSearchUrl(searchSettings, OUTPUTS[i].id),
                                            OUTPUTS[i].name));
                }
                return urls;
            },
           getSearchUrl: function(searchSettings, output) {
                var url =  this.repositoryRoot +"/search/do?output=" +output;
                for(var i in searchSettings.types) {
                    var type = searchSettings.types[i];
                    url += "&type=" + type;
                }
                if(searchSettings.parent!=null&& searchSettings.parent.length>0) 
                    url += "&group=" + searchSettings.parent;
                if(searchSettings.text!=null&& searchSettings.text.length>0) 
                    url += "&text=" + searchSettings.text;

                for(var i in searchSettings.metadata) {
                    var metadata = searchSettings.metadata[i];
                    url += "&metadata.attr1." + metadata.type + "=" + metadata.value;
                }
                url += "&max=" + searchSettings.getMax();
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
                if(callback==null) {
                    return null;
                }
                var jsonUrl = this.getJsonUrl(id);
                var jqxhr = $.getJSON( jsonUrl, function(data) {
                        var entryList =  createEntriesFromJson(data);
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



function getEntryManager() {
    if(window.globalEntryManager == null) {
        window.globalEntryManager = new EntryManager();
    }
    return window.globalEntryManager;
}




function createEntriesFromJson(data) {
    var entries = new Array();
    for(var i=0;i<data.length;i++)  {
        var entryData = data[i];
        var entry = new Entry(entryData);
        getEntryManager().addEntry(entry);
        entries.push(entry);
    }
    return entries;
}


function MetadataType(type, label) {
    $.extend(this, {type:type,label:label});
    $.extend(this, {
            getType: function() {return this.type;},
            getLabel: function() {if(this.label!=null) return this.label; return this.type;}
        });
}


function EntryType(props) {
    $.extend(this, props);
    $.extend(this, {
            getIsGroup: function() {return this.isgroup;},
            getIcon: function() {return this.icon;},
            getLabel: function() {return this.label;},
            getId: function() {return this.type;},
            getCategory: function() {return this.category;},
            getEntryCount: function() {return this.entryCount;},
        });
}

function Entry (props) {
    var NONGEO = -9999;
    $.extend(this, {
            latitude: NaN,
            longitude: NaN,
            north: NaN,
            west: NaN,
            south: NaN,
            east: NaN,
        });

    $.extend(this, props);
    $.extend(this, {
            getId : function () {
                return  this.id;
            },
            getLocationLabel: function() {
                return "n: " + this.north + " w:" + this.west + " s:" + this.south +" e:" + this.east;
            },
            hasLocation: function() {
                return !isNaN(this.north) && this.north != NONGEO;
            },
            getLatitude: function() {
                return this.north;
            },
            getLongitude: function() {
                return this.west;
            },
            getIconUrl : function () {
                if(this.icon==null)
                    return root + "/icons/page.png";
                return this.icon;
            },
            getIconImage : function (attrs) {
                return htmlUtil.image(this.getIconUrl(),attrs);
            },
            getColumnValue : function (name) {
                var value = this["column." + name];
                return value;
            },
            getColumnNames : function () {
                var names =  this.columnNames;
                if (!names) names = new Array();
                return names;
            },

            getColumnLabels : function () {
                var names =  this.columnLabels;
                if (!names) names = new Array();
                return names;
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
                return size_format(this.getFilesize());
            },
            toString: function() {
                return "entry:" + this.getName();
            },
            getEntryUrl : function () {
                return  root + "/entry/show?entryid=" + this.id;
            },
            getFilename : function () {
                return this.filename;
            }, 
            getFileUrl : function () {
                return  root + "/entry/get?entryid=" + this.id;
            },
            getLink : function (label) {
                if(!label) label = this.getName();
                return  htmlUtil.tag("a",["href", this.getEntryUrl()],label);
            },

            toString: function() {
                return "entry:" + this.getName();
            }
        });
}



function EntryList(jsonUrl, listener) {
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
                this.entries =         createEntriesFromJson(data);
                for(var i in this.entries) {
                    var entry = this.entries[i];
                    this.map[entry.getId()] = entry;
                }
                if(this.listener) {
                    this.listener.entryListChanged(this);
                }
            }
            });

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
            metadata: [],
            getMax: function() {
                return this.max;
            },
            hasType:function(type) {
                return this.types.indexOf(type)>=0;
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
