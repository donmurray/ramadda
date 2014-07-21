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
*/


function DataCollection() {
    RamaddaUtil.defineMembers(this,{ 
            data: [],
            hasData: function() {
                for(var i=0;i<this.data.length;i++) {
                    if(this.data[i].hasData()) return true;
                }
                return false;
            },
            getList: function() {
                return this.data;
            },
            addData: function(data) {
                this.data.push(data);
            },
            handleEventMapClick: function (myDisplay, source, lon, lat) {
                for(var i=0;i<this.data.length;i++ ) {
                    this.data[i].handleEventMapClick(myDisplay, source, lon, lat);
                }

            },


});
    
}

function BasePointData(name, properties) {
    if(properties == null) properties = {};

    RamaddaUtil.defineMembers(this, {
            recordFields : null,
                records : null,
                entryId: null,
                entry: null});

    $.extend(this, properties);

    RamaddaUtil.defineMembers(this, {
            name : name,
            properties : properties,
            initWith : function(thatPointData) {
                this.recordFields = thatPointData.recordFields;
                this.records = thatPointData.records;
            },
            handleEventMapClick: function (myDisplay, source, lon, lat) {
            },
            hasData : function() {
                return this.records!=null;
            },
            clear: function() {
                this.records = null;
                this.recordFields = null;
            },
            getProperties : function() {
                return this.properties;
            },
            getProperty : function(key, dflt) {
                var value = this.properties[key];
                if(value == null) return dflt;
                return value;
            },
            getRecordFields : function() {
                return this.recordFields;
            },
            getRecords : function() {
                return this.records;
            },
            getNumericFields : function() {
                var recordFields = this.getRecordFields();
                var numericFields = [];
                for(var i=0;i<recordFields.length;i++) {
                    var field = recordFields[i];
                    if(field.isNumeric) numericFields.push(field);
                }
                return numericFields;
            },
            getChartableFields : function() {
                var recordFields = this.getRecordFields();
                var numericFields = [];
                var skip = /(TIME|HOUR|MINUTE|SECOND|YEAR|MONTH|DAY|LATITUDE|LONGITUDE|ELEVATION)/g;
                for(var i=0;i<recordFields.length;i++) {
                    var field = recordFields[i];
                    if(!field.isNumeric || !field.isChartable()) {
                        continue;
                    }
                    var ID = field.getId().toUpperCase() ;
                    if(ID.match(skip)) {
                        continue;
                    }
                    numericFields.push(field);
                }

                return RecordUtil.sort(numericFields);
            },
            loadData: function(display) {
            },
            getName : function() {
                return this.name;
            },
            getTitle : function() {
                if(this.records !=null && this.records.length>0)
                    return this.name +" - " + this.records.length +" points";
                return this.name;
            }
        });
}




/*
This encapsulates some instance of point data. 
name - the name of this data
recordFields - array of RecordField objects that define the metadata
data - array of Record objects holding the data
*/
function PointData(name, recordFields, records, url, properties) {
    RamaddaUtil.inherit(this, new  BasePointData(name, properties));
    RamaddaUtil.defineMembers(this, {
            recordFields : recordFields,
            records : records,
            url : url,
            loadingCnt: 0,
            equals : function(that) {
                return this.url == that.url;
            },
            getIsLoading: function() {
                return this.loadingCnt>0;
            },
            handleEventMapClick: function (myDisplay, source, lon, lat) {
                this.lon = lon;
                this.lat = lat;
                if(myDisplay.getDisplayManager().hasGeoMacro(this.url)) {
                    this.loadData(myDisplay, true);
                }
            },
            startLoading: function() {
                this.loadingCnt++;
            },
            stopLoading: function() {
                this.loadingCnt--;
            },
            loadData: function(display, reload) {
                if(this.url==null) {
                    console.log("No URL");
                    return;
                }
                var props = {
                    lat:this.lat,                    
                    lon:this.lon,
                };
                var jsonUrl = display.displayManager.getJsonUrl(this.url, display, props);
                this.loadPointJson(jsonUrl, display, reload);
            },
            loadPointJson: function(url, display, reload) {
                var pointData = this;
                console.log("loadPointJson url:" + url);
                this.startLoading();
                var jqxhr = $.getJSON( url, function(data) {
                        if(data.error!=null) {
                            var code = data.errorcode;
                            if(code == null) code = "error";
                            display.handleError(code, data.error);
                            return;
                        }
                        var newPointData =    makePointData(data);
                        pointData.initWith(newPointData);
                        display.pointDataLoaded(pointData, url, reload);
                        pointData.stopLoading();
                    })
                    .fail(function(jqxhr, textStatus, error) {
                            var err = textStatus + ", " + error;
                            console.log("JSON error:" +err);
                            pointData.stopLoading();
                        });
            }

        });
}


function DerivedPointData(displayManager, name, pointDataList, operation) {
    RamaddaUtil.inherit(this, new  BasePointData(name));
    RamaddaUtil.defineMembers(this, {
            displayManager: displayManager,
            operation: operation,
            pointDataList: pointDataList,
            loadDataCalls: 0,
            display: null,
            pointDataLoaded: function(pointData) {
                this.loadDataCalls--;
                if(this.loadDataCalls<=0) {
                    this.initData();
                }
            },
            equals : function(that) {
                if(that.pointDataList == null) return false;
                if(this.pointDataList.length!=that.pointDataList.length) return false;
                for(var i in this.pointDataList) {
                    if(!this.pointDataList[i].equals(that.pointDataList[i])) {
                        return false;
                    }
                }
                return true;
            },
            initData: function() {
                var pointData1 = this.pointDataList[0];
                if(this.pointDataList.length == 1) {
                    this.records = pointData1.getRecords();
                    this.recordFields = pointData1.getRecordFields();
                } else if(this.pointDataList.length > 1) {
                    var results = this.combineData(pointData1, this.pointDataList[1]);
                    this.records = results.records;
                    this.recordFields = results.recordFields;
                }
                this.display.pointDataLoaded(this);
            },
            combineData: function(pointData1, pointData2) {
                var records1 = pointData1.getRecords();
                var records2 = pointData2.getRecords();
                var newRecords = [];
                var newRecordFields;

                //TODO:  we really need visad here to sample

                if(records1.length!=records2.length) {
                    console.log("bad records:" + records1.length +" " + records2.length);
                }

                if(this.operation == "average") {
                    for(var recordIdx=0;recordIdx<records1.length;recordIdx++) {
                        var record1 = records1[recordIdx];
                        var record2 = records2[recordIdx];
                        if(record1.getDate()!=record2.getDate()) {
                            console.log("Bad record date:" + record1.getDate() + " " + record2.getDate());
                            break;
                        }
                        var newRecord = $.extend(true, {}, record1);
                        var data1 = newRecord.getData();
                        var data2 = record2.getData();
                        for(var colIdx=0;colIdx<data1.length;colIdx++) {
                            data1[colIdx]= (data1[colIdx]+data2[colIdx])/2;
                        }
                        newRecords.push(newRecord);
                    }
                    newRecordFields = pointData1.getRecordFields();
                } else  if(this.operation == "other func") {
                }
                if(newRecordFields==null) {
                    //for now just use the first operand
                    newRecords = records1;
                    newRecordFields = pointData1.getRecordFields();
                }
                return {records: newRecords,
                        recordFields: newRecordFields};
            },
            loadData: function(display) {
                this.display = display;
                this.loadDataCalls=0;
                for(var i in this.pointDataList) {
                    var pointData = this.pointDataList[i];
                    if(!pointData.hasData()) {
                        this.loadDataCalls++;
                        pointData.loadData(this);
                    }
                    if(this.loadDataCalls==0) {
                        this.initData();
                    }
                }
                //TODO: notify display
            }
        });
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
function RecordField(props) {
    $.extend(this, props);
    $.extend(this, {
             isNumeric: props.type == "double",
             properties: props
             });
 
   RamaddaUtil.defineMembers(this, {
             getIndex: function() {
                 return this.index;
             },
             isChartable: function() {
               return this.chartable;
             },
             getSortOrder: function() {
               return this.sortorder;
             },
             getId: function() {
                 return this.id;
             },
             getLabel: function() {
                 if(this.label == null || this.label.length==0) return this.id;
                 return this.label;
             },
             getType: function() {
                 return this.type;
             },
             getMissing: function() {
                 return this.missing;
             },
             getUnit: function() {
                 return this.unit;
             }
        });

}


/*
The main data record. This holds a lat/lon/elevation, time and an array of data
The data array corresponds to the RecordField fields
 */
function PointRecord(lat, lon, elevation, time, data) {
    RamaddaUtil.defineMembers(this, {
            latitude : lat,
            longitude : lon,
            elevation : elevation,
            recordTime : time,
            data : data,
            getData : function() {
                return this.data;
            }, 
            getValue : function(index) {
                return this.data[index];
            }, 
            hasLocation : function() {
                return ! isNaN(this.latitude);
            }, 
            hasElevation : function() {
                return ! isNaN(this.elevation);
            }, 
            getLatitude : function() {
                return this.latitude;
            }, 
            getLongitude : function() {
                return this.longitude;
            }, 
            getTime : function() {
            	return this.time;
            },
            getElevation : function() {
                return this.elevation;
            }, 
            getDate : function() {
                return this.recordTime;
            }
        });
}



function makePointData(json) {
    var fields = [];
    for(var i=0;i<json.fields.length;i++) {
        var field  = json.fields[i];
        fields.push(new RecordField(field));
    }

    var data =[];

    for(var i=0;i<json.data.length;i++) {
        var tuple = json.data[i];
        //lat,lon,alt,time,data values
        var date  = tuple.date;
        if(date!=0) {
            date = new Date(date);
        }
        if ((typeof tuple.latitude === 'undefined')) {
            tuple.latitude = NaN;
            tuple.longitude = NaN;
        }

        if ((typeof tuple.elevation === 'undefined')) {
            tuple.elevation = NaN;
        }
        var record = new PointRecord(tuple.latitude, tuple.longitude,tuple.elevation, date, tuple.values);
        data.push(record);
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









/*
function InteractiveDataWidget (theChart) {
    this.jsTextArea =  id + "_js_textarea";
    this.jsSubmit =  id + "_js_submit";
    this.jsOutputId =  id + "_js_output";
        var jsInput = "<textarea rows=10 cols=80 id=\"" + this.jsTextArea +"\"/><br><input value=\"Try it out\" type=submit id=\"" + this.jsSubmit +"\">";

        var jsOutput = "<div id=\"" + this.jsOutputId +"\"/>";
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
    if(properties == null) properties = {};
    RamaddaUtil.defineMembers(this, {
            properties: properties,
            recordOk:function(display, record, values) {
                return true;
            }
        });
}


function MonthFilter(param) {
    RamaddaUtil.inherit(this,new RecordFilter());
    RamaddaUtil.defineMembers(this,{
            months: param.split(","),
            recordOk: function(display, record, values) {
                for(i in this.months) {
                    var month = this.months[i];
                    var date = record.getDate();
                    if(date == null) return false;
                    if(date.getMonth == null) {
                        //console.log("bad date:" + date);
                        return false;
                    }
                    if(date.getMonth()==month) return true;
                }
                return false;
            }
        });
}


var RecordUtil = {
    getRanges: function(fields,records) {
        var maxValues = [];
        var minValues = [];
        for(var i=0;i<fields.length;i++) {
            maxValues.push(NaN);
            minValues.push(NaN);
        }

        for(var row=0;row<records.length;row++) {
            for(var col=0;col<fields.length;col++) {
                var value  = records[row].getValue(col);
                if(isNaN(value)) continue;    
                maxValues[col] = (isNaN(maxValues[col])?value:Math.max(value, maxValues[col]));
                minValues[col] = (isNaN(minValues[col])?value:Math.min(value, minValues[col]));
            }
        }

        var ranges = [];
        for(var col=0;col<fields.length;col++) {
            ranges.push([minValues[col],maxValues[col]]);
        }
        return ranges;
    },



    getElevationRange: function(fields,records) {
        var maxValue =NaN;
        var minValue = NaN;

        for(var row=0;row<records.length;row++) {
            if(records[row].hasElevation()) { 
                var value = records[row].getElevation();
                maxValue = (isNaN(maxValue)?value:Math.max(value, maxValue));
                minValue = (isNaN(minValue)?value:Math.min(value, minValue));
            }
        }
        return [minValue,maxValue];
    },


    slice: function(records,index) {
        var values = [];
        for(var row=0;row<records.length;row++) {
            values.push(records[row].getValue(index));
        }
        return values;
    },


    sort : function(fields) {
        fields = fields.slice(0);
        fields.sort(function(a,b){
                var s1 = a.getSortOrder();
                var s2 = b.getSortOrder();
                return s1<s2;
            });
        return fields;
    },
    getPoints: function (records, bounds) {
        var points =[];
        var north=NaN,west=NaN,south=NaN,east=NaN;
        if(records!=null) {
            for(j=0;j<records.length;j++) { 
                var record = records[j];
                if(!isNaN(record.getLatitude())) { 
                    if(j == 0) {
                        north  =  record.getLatitude();
                        south  = record.getLatitude();
                        west  =  record.getLongitude();
                        east  = record.getLongitude();
                    } else {
                        north  = Math.max(north, record.getLatitude());
                        south  = Math.min(south, record.getLatitude());
                        west  = Math.min(west, record.getLongitude());
                        east  = Math.min(east, record.getLongitude());
                    }
                    points.push(new OpenLayers.Geometry.Point(record.getLongitude(),record.getLatitude()));
                }
            }
        }
        bounds[0] = north;
        bounds[1] = west;
        bounds[2] = south;
        bounds[3] = east;
        return points;
    },
    findClosest: function(records, lon, lat, indexObj) {
        if(records == null) return null;
        var closestRecord = null;
        var minDistance = 1000000000;
        var index = -1;
        for(j=0;j<records.length;j++) { 
            var record = records[j];
            if(isNaN(record.getLatitude())) { 
                continue;
            }
            var distance = Math.sqrt((lon-record.getLongitude())*(lon-record.getLongitude()) + (lat-record.getLatitude())*(lat-record.getLatitude()));
            if(distance<minDistance) {
                minDistance = distance;
                closestRecord = record;
                index = j;
            }
        }
        if(indexObj!=null) {
            indexObj.index = index;
        }
        return closestRecord;
    },
    clonePoints: function(points) {
        var result = [];
        for(var i=0;i<points.length;i++) {
            var point = points[i];
            result.push(new OpenLayers.Geometry.Point(point.x,point.y));
        }
        return result;
    }
};

