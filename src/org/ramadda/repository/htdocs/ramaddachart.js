/**
Copyright 2008-2014 Geode Systems LLC

This package supports charting and mapping of georeferenced time series data
It requires pointdata.js

Use:
<div id="example"></div>
...
var recordFields  = [new RecordField(...), ... see below]
var data  = [new Record(...), ...]
var pointData = new  PointData("Example data set",  recordFields, data);
var chart = new  RamaddaChart("example" , pointData);
*/


/*
Create a chart
id - the id of this chart. Has to correspond to a div tag id 
pointData - A PointData object (see below)
 */
function RamaddaLineChart(id, pointDataArg, properties) {
    this.id = id;
    this.pointData  = null;
    this.properties = properties;
    init_RamaddaLineChart(this);
    this.createHtml();
    var testUrl = null;
    //Uncomment to test using test.json
    //testUrl = "/repository/test.json";
    //    testUrl = "http://localhost:8080/repository/entry/show/gfs80_point?output=data.gridaspoint&entryid=633bc909-079f-429a-b873-5744a3b04770&location.latitude=34.6&location.longitude=-101.1&calendar=gregorian&fromdate=&todate=&format=json&level=-1&variable.Temperature=true"

    if(testUrl!=null) {
        pointDataArg = new PointData("Test",null,null,testUrl);
    }
    this.setPointData(pointDataArg, true);
}


function init_RamaddaLineChart(theChart) {
    init_RamaddaChart(theChart);
    theChart.createHtml = function() {
        var html = "";
        html += this.getHeaderDiv();
        html += "<table width=100%>";
        html += "<tr valign=top><td>";
        html += this.getFieldsDiv();
        html += "</td><td>";
        html += this.getChartDiv();
        html += "</td></tr></table>";
        this.setHtml(html);
    }

    theChart.addFieldsLegend = function() {
        if(!this.hasData()) {
            $("#" + this.fieldsDivId).html("No fields selected");
            return;
        }
        if(this.getProperty("fields",null)!=null) {
            return;
        }
        this.setTitle(pointData.getTitle());
        var html =  "<div class=chart-fields-inner>";
        var fields = this.pointData.getChartableFields();
        var checkboxClass = this.id +"_checkbox";
        for(i=0;i<fields.length;i++) { 
            var field = fields[i];
            field.checkboxId  = this.id +"_cbx" + i;
            html += htmlUtil.checkbox(field.checkboxId, checkboxClass,
                                      field ==fields[0]);
            html += "&nbsp;<span title=\"" + field.getId() +"\">" + field.label+"</span><br>";
        }
        html+= "</div>";
        $("#" + this.fieldsDivId).html(html);

        //Listen for changes to the checkboxes
        $("." + checkboxClass).click(function() {
                theChart.loadData();
          });
    }


    theChart.getDisplayedFields = function() {
        var df = [];
        var fields = this.pointData.getChartableFields();
        var fixedFields = this.getProperty("fields");
        if(fixedFields !=null) {
            for(i=0;i<fields.length;i++) { 
                var field = fields[i];
                if(fixedFields.indexOf(field.getId())>=0) {
                    df.push(field);
                }
            }
            return df;
        }

        for(i=0;i<fields.length;i++) { 
            var field = fields[i];
            if($("#" + field.checkboxId).is(':checked')) {
                df.push(field);
            }
        }

        if(df.length==0) {
            if(fields.length==0) {
                return df;
            }
            df.push(fields[0]);
        }
        return df;
    }


    theChart.loadData = function() {
        if(!this.hasData()) {
            if(this.chart !=null) {
                this.chart.clearChart();
            }
            return;
        }

        var displayedFields = this.getDisplayedFields();
        if(displayedFields.length==0) {
            $("#" + this.chartDivId).html("No fields selected");
            return;
        }

        var dataList = this.getStandardData(displayedFields);

        var dataTable = google.visualization.arrayToDataTable(dataList);
        var options = {
            series: [
        {targetAxisIndex:0},
        {targetAxisIndex:1},
                     ],
            title: this.pointData.getTitle(),
            chartArea:{left:30,top:30,height:"75%"}
        };
        this.chart = new google.visualization.LineChart(document.getElementById(this.chartDivId));
        this.chart.draw(dataTable, options);
    }
}




function init_RamaddaChart(theChart) {
    theChart.chartHeaderId =theChart.id +"_header";
    theChart.fieldsDivId =theChart.id +"_fields";
    theChart.chartDivId =theChart.id +"_chart";


    theChart.setHtml = function(html) {
        $("#" + this.id).html(html);
    }

    theChart.setTitle  = function(title) {
        $("#" + this.chartHeaderId).html(title);
    }

    theChart.getId = function() {
        return this.id;
    }

    theChart.getProperty = function(key, dflt) {
        if(typeof this.properties == 'undefined') return dflt;
        var value = this.properties[key];
        if(value == null) return dflt;
        return value;
    }

    theChart.hasData = function() {
        return this.pointData!=null && this.pointData.hasData();
    }

    theChart.setPointData = function(pointData, checkUrl) {
        this.pointData = pointData;
        if(this.hasData()) {
            this.addFieldsLegend();
            this.loadData();
        } else if(checkUrl) {
            var jsonUrl = pointData.getUrl();
            if(jsonUrl!=null) {
                loadPointJson(jsonUrl, this);
            }
        }
    }

    theChart.getHeaderDiv = function() {
        return   "<div id=\"" + this.chartHeaderId +"\" class=chart-header></div>";
    }
    theChart.getFieldsDiv = function() {
        return   "<div id=\"" + this.fieldsDivId +"\" class=chart-fields></div>";
    }
    theChart.getChartDiv = function() {
        return   "<div id=\"" + this.chartDivId +"\" style=\"width: " + this.getProperty("width","400px") +"; height: " + this.getProperty("height","400px") +";\"></div>\n";
    }

    theChart.getStandardData = function(fields) {
        var dataList = [];
        //The first entry in the dataList is the array of names
        //The first field is the domain, e.g., time or index
        //        var fieldNames = ["domain","depth"];
        var fieldNames = ["domain"];
        for(i=0;i<fields.length;i++) { 
            var field = fields[i];
            var name  = field.getLabel();
            if(field.getUnit()!=null) {
                name += " (" + field.getUnit()+")";
            }
            fieldNames.push(name);
        }
        dataList.push(fieldNames);

        //These are Record objects 
        var records = this.pointData.getData();
        for(j=0;j<records.length;j++) { 
            var record = records[j];
            var values = [];
            var date = record.getDate();
            //Add the date or index field
            if(date!=null) {
                values.push(date);
            } else {
                values.push(j);
            }
            //            values.push(record.getElevation());
            var allNull  = true;
            for(var i=0;i<fields.length;i++) { 
                var field = fields[i];
                var value = record.getValue(field.getIndex());
                if(value!=null) {
                    allNull = false;
                }
                values.push(value);
            }
            //TODO: when its all null values we get some errors
            dataList.push(values);
        }
        return dataList;
    }
}
