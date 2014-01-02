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
function RamaddaLineChart(id, pointDataArg) {
    var theChart  = this;
    this.id = id;
    this.pointData  = null;
    this.fieldsDivId =id +"_fields";
    this.chartDivId =id +"_chart";
    //Init methods
    init_RamaddaLineChart(this);
    this.createHtml();
    this.chart = new google.visualization.LineChart(document.getElementById(this.chartDivId));
    this.pointData  = null;
    //Uncomment to test using test.json
    //    pointDataArg = new PointData("Test",null,null,"/repository/test.json");
    this.setPointData(pointDataArg, true);
}

function init_RamaddaLineChart(theChart) {

    theChart.getId = function() {
        return this.id;
    }

    theChart.loadJson = function(url) {
        var theChart = this;
        var jqxhr = $.getJSON( url, function(data) {
                theChart.setPointData(makePointData(data),false);
            })
        .fail(function(jqxhr, textStatus, error) {
                var err = textStatus + ", " + error;
                alert("Error:" + err);
                console.log(err);
            });


    }

    theChart.setPointData = function(pointData, checkUrl) {
        var theChart = this;
        this.pointData = pointData;
        if(this.hasData()) {
            this.addFields();
            this.loadData();
        } else if(checkUrl) {
            var jsonUrl = pointData.getUrl();
            if(jsonUrl!=null) {
                this.loadJson(jsonUrl);
            }
        }
    }


    theChart.createHtml = function() {
        var fieldsDiv =  "<div id=\"" + this.fieldsDivId +"\" class=chart-fields>";
        var chartDiv =  "<div id=\"" + this.chartDivId +"\" style=\"width: 900px; height: 500px;\"></div>\n";

        var html = "";
        html += "<table width=100%>";
        html += "<tr valign=top><td>";
        html += fieldsDiv;
        html += "</td><td>";
        html += chartDiv;
        html += "</td></tr></table>";
        $("#" + this.id).html(html);
    }


    theChart.addFields = function() {
        if(!this.hasData()) {
            $("#" + this.fieldsDivId).html("No fields selected");
            return;
        }
        var html = "";
        var fields = this.pointData.getChartableFields();
        this.displayedFields = [fields[0]];
        var checkboxClass = this.id +"_checkbox";
        for(i=0;i<fields.length;i++) { 
            var field = fields[i];
            if(!field.isNumeric) {
                continue;
            }
            field.checkboxId  = this.id +"_cbx" + i;
            html += "<input id=\"" + field.checkboxId +"\" class=\""  + checkboxClass +"\"  type=checkbox value=true ";
            if(this.displayedFields.indexOf(field)>=0) {
                html+= " checked ";
            }
            html += "/> ";
            html += field.label;
            html+= "<br>";
        }
        $("#" + this.fieldsDivId).html(html);
        //Listen for changes to the checkboxes
        $("." + checkboxClass).change(function() {
                theChart.loadData();
            });

    }

    theChart.setDisplayedFields = function() {
        this.displayedFields = [];
        var fields = this.pointData.getChartableFields();
        for(i=0;i<fields.length;i++) { 
            var field = fields[i];
            if($("#" + field.checkboxId).is(':checked')) {
                this.displayedFields.push(field);
            }
        }

        if(this.displayedFields.length==0) {
            if(fields.length==0) return;
            this.displayedFields.push(fields[0]);
        }
    }


    theChart.loadData = function() {
        if(!this.hasData()) {
            this.chart.clearChart();
            return;
        }
        
        this.setDisplayedFields();

        if(this.displayedFields.length==0) {
            $("#" + this.chartDivId).html("No fields selected");
            return;
        }
        var dataList = [];
       
        //The first entry in the dataList is the array of names
        //The first field is the domain, e.g., time or index
        //        var fieldNames = ["domain","depth"];
        var fieldNames = ["domain"];
        for(i=0;i<this.displayedFields.length;i++) { 
            var field = this.displayedFields[i];
            if(!field.isNumeric) {
                console.log("skipping:" + field.label + " " + field.type);
                continue;
            }
            //            console.log("using:" + field.label + " " + field.type);
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
            for(var i=0;i<this.displayedFields.length;i++) { 
                var field = this.displayedFields[i];
                if(!field.isNumeric) {
                    //                    console.log("skipping:" + field.label + " " + field.type);
                    continue;
                }
                var value = record.getValue(field.getIndex());
                //                console.log(j+" value:" + value);
                if(value!=null) {
                    allNull = false;
                }
                values.push(value);
            }
            //TODO: when its all null values we get some errors
            dataList.push(values);
        }

        var dataTable = google.visualization.arrayToDataTable(dataList);

        //Not quite sure about the axis settings
        var options = {
            series: [
        {targetAxisIndex:0},
        {targetAxisIndex:1},
        {targetAxisIndex:0},
        {targetAxisIndex:1},
        {targetAxisIndex:0},
                     ],
            title: this.pointData.getTitle(),
            chartArea:{xxleft:20,xxtop:0,height:"85%"}
        };
        this.chart.draw(dataTable, options);
    }

    theChart.hasData = function() {
        return this.pointData!=null && this.pointData.hasData();
    }
}

