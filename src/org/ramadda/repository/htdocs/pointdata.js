/**
* Copyright 2008-2013 Geode Systems LLC
*
* Permission is hereby granted, free of charge, to any person obtaining a copy of this 
* software and associated documentation files (the "Software"), to deal in the Software 
* without restriction, including without limitation the rights to use, copy, modify, 
* merge, publish, distribute, sublicense, and/or sell copies of the Software, and to 
* permit persons to whom the Software is furnished to do so, subject to the following conditions:
* 
* The above copyright notice and this permission notice shall be included in all copies 
* or substantial portions of the Software.
* 
* THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, 
* INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR 
* PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE 
* FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR 
* OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER 
* DEALINGS IN THE SOFTWARE.
*/


/*
This package supports charting and mapping of georeferenced time series data

It is used  in conjunction with the ramaddachart.js package

Use:
<div id="example"></div>
...
var recordFields  = [new RecordField(...), ... see below]
var data  = [new Record(...), ...]
var pointData = new  PointData("Example data set",  recordFields, data);
var chart = new  RamaddaChart("example" , pointData);

*/


function DataCollection() {
    this.data = [];

    this.hasData = function() {
        for(var i=0;i<this.data.length;i++) {
            if(this.data[i].hasData()) return true;
        }
        return false;
    }

    this.getData = function() {
        return this.data;
    }

    this.addData = function(data) {
        this.data.push(data);
    }

}

/*
This encapsulates some instance of point data. 
name - the name of this data
recordFields - array of RecordField objects that define the metadata
data - array of Record objects holding the data
*/
function PointData(name, recordFields, data, url, properties) {
    this.name = name;
    this.recordFields = recordFields;
    this.data = data;
    this.xurl = url;
    this.properties = properties;


    this.initWith = function(thatPointData) {
        this.recordFields = thatPointData.recordFields;
        this.data = thatPointData.data;
    }

    this.hasData = function() {
        return this.data!=null;
    }
    this.clear= function() {
        this.data = null;
    }

    this.getProperties = function() {
        return this.properties;
    }


    this.getProperty = function(key, dflt) {
        if(typeof this.properties == 'undefined') {
            return dflt;
        }
        var value = this.properties[key];
        if(value == null) return dflt;
        return value;
    }

    this.getRecordFields = function() {
        return this.recordFields;
    }
    this.getNumericFields = function() {
        var numericFields = [];
        for(var i=0;i<this.recordFields.length;i++) {
            var field = this.recordFields[i];
            if(field.isNumeric) numericFields.push(field);
        }
        return numericFields;
    }
    this.getChartableFields = function() {
        var numericFields = [];
        var skip = /(LATITUDE|LONGITUDE|ELEVATION)/g;
        for(var i=0;i<this.recordFields.length;i++) {
            var field = this.recordFields[i];
            if(!field.isNumeric || !field.isChartable()) {
                continue;
            }
            var ID = field.getId().toUpperCase() ;
            if(ID.match(skip)) {
                continue;
            }
            numericFields.push(field);
        }
        return numericFields;
    }

    this.getData = function() {
        return this.data;
    }
    this.getUrl = function() {
        return this.xurl;
    }
    this.getName = function() {
        return this.name;
    }

    this.getTitle = function() {
        if(this.data !=null && this.data.length>0)
            return this.name +" - " + this.data.length +" points";
        return this.name;
    }

}


/*
This class defines the metadata for a record column. 
index - the index i the data array
id - string id
label - string label to show to user
type - for now not used but once we have string or other column types we'll need it
missing - the missing value forthis field. Probably not needed and isn't used
as I think RAMADDA passes in NaN
unit - the unit of the value
 */
function RecordField(index, id, label, type, missing, unit, properties) {
    this.index = index;
    this.id = id;
    this.label = label;
    this.type = type;
    this.missing = missing;
    this.unit = unit;
    this.properties = properties;
    init_RecordField(this);
}

function init_RecordField(recordField) {
    recordField.isNumeric = recordField.type == "double";
    //    console.log(recordField.label+" type:" + recordField.type+": " + recordField.isNumeric);

    recordField.getIndex = function() {
        return this.index;
    }

    recordField.getProperty = function(key, dflt) {
        if(typeof this.properties == 'undefined') {
            return dflt;
        }
        var value = this.properties[key];
        if(value == null) return dflt;
        return value;
    }

    recordField.isChartable = function() {
        return this.getProperty("chartable",false);
    }

    recordField.getId = function() {
        return this.id;
    }
    recordField.getLabel = function() {
        return this.label;
    }
    recordField.getType = function() {
        return this.type;
    }
    recordField.getMissing = function() {
        return this.missing;
    }
    recordField.getUnit = function() {
        return recordField.unit;
    }
}


/*
The main data record. This holds a lat/lon/elevation, time and an array of data
The data array corresponds to the RecordField fields
 */
function Record(lat, lon, elevation, time, data) {
    this.latitude = lat;
    this.longitude = lon;
    this.elevation = elevation;
    this.time = time;
    this.data = data;
    init_Record(this);
}

//Add the Record class functions to the object
function init_Record(record) {
    record.getData = function() {
        return this.data;
    }
    record.getValue = function(index) {
        return this.data[index];
    }
    record.hasLocation = function() {
        return ! isNaN(this.latitude);
    }

    record.hasElevation = function() {
        return ! isNaN(this.elevation);
    }

    record.getLatitude = function() {
        return this.latitude;
    }
    record.getLongitude = function() {
        return this.longitude;
    }
    record.getElevation = function() {
        return this.elevation;
    }
    record.getDate = function() {
        return this.time;
    }
    return record;
}



function makePointData(json) {
    var fields = [];
    for(var i=0;i<json.fields.length;i++) {
        var field  = json.fields[i];
        init_RecordField(field);
        fields.push(field);
    }

    var data =[];

    for(var i=0;i<json.data.length;i++) {
        var tuple = json.data[i];
        //lat,lon,alt,time,data values
        var date  = tuple.date;
        if(date>0) {
            date = new Date(date);
        }
        if ((typeof tuple.latitude === 'undefined')) {
            tuple.latitude = NaN;
            tuple.longitude = NaN;
        }

        if ((typeof tuple.elevation === 'undefined')) {
            tuple.elevation = NaN;
        }

        data.push(new Record(tuple.latitude, tuple.longitude,tuple.elevation,date, tuple.values));
    }

    
    var name = data.name;

    if ((typeof name === 'undefined')) {
        name =  "Point Data";
    }
    return new  PointData(name,  fields, data);
}






function makeTestPointData() {
    var json = {
        fields:
        [{index:0,
               id:"field1",
               label:"Field 1",
               type:"double",
               missing:"-999.0",
               unit:"m"},

        {index:1,
               id:"field2",
               label:"Field 2",
               type:"double",
               missing:"-999.0",
               unit:"c"},
            ],
        data: [
               [-64.77,-64.06,45, null,[8.0,1000]],
               [-65.77,-64.06,45, null,[9.0,500]],
               [-65.77,-64.06,45, null,[10.0,250]],
               ]
    };

    return makePointData(json);

}



function getRanges(fields,data) {
    var ranges = [];
    var maxValues = [];
    var minValues = [];
    for(var i=0;i<fields.length;i++) {
        maxValues.push(NaN);
        minValues.push(NaN);
    }

    for(var row=0;row<data.length;row++) {
        for(var col=0;col<fields.length;col++) {
            var value  = data[row].getValue(col);
            if(isNaN(value)) continue;    
            maxValues[col] = (isNaN(maxValues[col])?value:Math.max(value, maxValues[col]));
            minValues[col] = (isNaN(minValues[col])?value:Math.min(value, minValues[col]));
        }
    }
    var tuple =[minValues,maxValues];
    return tuple;
}


function loadPointJson(url, theChart, pointData) {
    console.log("json url:" + url);
    var jqxhr = $.getJSON( url, function(data) {
            var newPointData =    makePointData(data);
            pointData.initWith(newPointData);
            theChart.pointDataLoaded(pointData);
        })
        .fail(function(jqxhr, textStatus, error) {
                var err = textStatus + ", " + error;
                alert("JSON error:" + err);
                console.log("JSON error:" +err);
            });
}




/*
function InteractiveDataWidget (theChart) {
    this.jsTextArea =  id + "_js_textarea";
    this.jsSubmit =  id + "_js_submit";
    this.jsOutputId =  id + "_js_output";
        var jsInput = "<textarea rows=10 cols=80 id=\"" + this.jsTextArea +"\"/><br><input value=\"Try it out\" type=submit id=\"" + this.jsSubmit +"\">";

        var jsOutput = "<div id=\"" + this.jsOutputId +"\"/>";


xxxx
        $("#" + this.jsSubmit).button().click(function(event){
                var js = "var chart = ramaddaGlobalChart;\n";
                js += "var data = chart.pointData.getData();\n";
                js += "var fields= chart.pointData.getRecordFields();\n";
                js += "var output= \"#" + theChart.jsOutputId  +"\";\n";
                js += $("#" + theChart.jsTextArea).val();
                eval(js);
            });
        html += "<table width=100%>";
        html += "<tr valign=top><td width=50%>";
        html += jsInput;
        html += "</td><td width=50%>";
        html += jsOutput;
        html += "</td></tr></table>";
*/



function RecordFilter(properties) {
    this.properties = properties;
    if(this.properties == null) this.properties = [];
    this.recordOk = function(record, values) {
        if(values[0].getMonth()!=0) return false;
        return true;
    }
}


function MonthFilter(month) {
    this.month = month;
    this.recordOk = function(record, values) {
        if(values[0].getMonth()!=this.month) return false;
        return true;
    }
}
