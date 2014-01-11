
function createEntriesFromJson(data) {
    var entries = new Array();
    for(var i=0;i<data.length;i++)  {
        var entry = data[i];
        entries.push(createEntryFromJson(entry));
    }
    return entries;
}


function createEntryFromJson(data) {
    init_Entry(data);
    return data;
}

function Entry () {
    init_Entry(this);
}

function init_Entry (theEntry) {

    theEntry.getId = function () {
        return  this.id;
    }
    theEntry.getIconImage = function () {
        return "<img src=\"" + this.icon +"\">";
    }
    theEntry.getColumnValue = function (name) {
        var value = this["column." + name];
        return value;
    }

    theEntry.getColumnNames = function () {
        var names =  this.columnNames;
        if (!names) names = new Array();
        return names;
    }

    theEntry.getColumnLabels = function () {
        var names =  this.columnLabels;
        if (!names) names = new Array();
        return names;
    }

    theEntry.getName = function () {
        if(this.name ==null || this.name == "") {
            return "no name";
        }
        return this.name;
    }
    theEntry.getFilesize = function () {
        var size =  parseInt(this.filesize);
        if(size == size) return size;
        return 0;
    }
    theEntry.getFormattedFilesize = function () {
        return size_format(this.getFilesize());
    }
    theEntry.getLink = function (label) {
        if(!label) label = this.getName();
        return  "<a href=\"${urlroot}/entry/show?entryid=" + this.id +"\">" + label +"</a>";
    }

}

function EntryList(jsonUrl, listener) {
    var entryList = this;

    entryList.haveLoaded = false;
    entryList.divId = null;
    entryList.entries =[];
    entryList.listener = listener;

    entryList.getEntries = function() {
        return this.entries;
    }

    entryList.setHtml = function(html) {
        if(this.divId == null) return;
        $("#" + this.divId).html(html);
    }

    entryList.initDisplay = function(divId) {
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
    }

    entryList.getHtml = function() {
        var html = "";
        for(var i=0;i<this.entries.length;i++) {
            var entry = this.entries[i];
            html += "<div class=entry-list-entry>";
            html+= entry.getName();
            html += "</div>";
        }
        return html;
    }

    entryList.createEntries = function(data) {
        this.entries =         createEntriesFromJson(data);
        if(this.listener) {
            this.listener.entryListChanged(this);
        }
    }


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



