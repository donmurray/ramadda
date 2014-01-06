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


function ChartManager(id,properties) {
    var theChart = this;
    this.id = id;
    this.properties = properties;
    this.charts = [];
    this.data = [];
    this.cnt = 0;
    init_ChartManager(this);
    var html = "";
    if(this.getProperty("shownew",false)) {
        html += "<span id=\"" + this.id + "_new\">New Chart</span>";
        html+="<br>";
    }

    for(var i=0;i<10;i++)  {
        var chartId = this.id +"_chart_" + i;
        html+= "<div id=\"" + chartId +"\"/>";
    }
    $("#"+ this.getId()).html(html);
    $("#" + this.id +"_new").button().click(function(event) {
            theChart.doNew();
        });
}



function init_ChartManager(chartManager) {
    chartManager.getId = function() {
        return this.id;
    }


    chartManager.getProperty = function(key, dflt) {
        if(typeof this.properties == 'undefined') return dflt;
        var value = this.properties[key];
        if(value == null) return dflt;
        return value;
    }

    chartManager.doNew = function() {
        this.addPointData(this.data[0]);
    }

    chartManager.addPointData = function(pointData) {
        var chartId = this.id +"_chart_" + (this.cnt++);
        var chart  = new RamaddaLineChart(chartId, pointData, pointData.getProperties());
        this.data.push(pointData);
        this.charts.push(chart);
    }

    chartManager.addLineChart = function(pointDataArg, properties) {
        var chartId = this.id +"_chart_" + (this.cnt++);
        var chart  = new RamaddaLineChart(chartId, pointDataArg, properties);
        this.charts.add(chart);
    }
}



/*
Create a chart
id - the id of this chart. Has to correspond to a div tag id 
pointData - A PointData object (see below)
 */
function RamaddaLineChart(id, pointDataArg, properties) {
    this.id = id;
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
    this.addOrLoadData(pointDataArg);
}


function init_RamaddaLineChart(theChart) {
    theChart.dataCollection = new DataCollection();
    init_RamaddaChart(theChart);
    theChart.createHtml = function() {
        var mapEnabled = this.getProperty("mapenabled",null);
        var theChart = this;
        var html = "";
        this.latFieldId = this.getId() +"_latfield";
        this.lonFieldId = this.getId() +"_lonfield";
        var reloadId = this.getId() +"_reload";
        html += this.getHeaderDiv();
        if (mapEnabled) {
            html+= "<form><input type=submit value=\"Reload\" id=\"" + reloadId +"\"> <input id=\"" + this.latFieldId +"\"> <input id=\"" +  this.lonFieldId+"\"></form><div id=\"mapdiv\" style=\"border:1px #888888 solid; background-color:#7391ad; width: 400px; height:200px;\"></div>";
        }
        html += "<table width=100% border=0>";
        html += "<tr valign=top>";
        if(this.getProperty("fields",null)==null) {
            html += "<td width=200>";
            html += this.getFieldsDiv();
            html += "</td>";
        }
        html += "<td>";
        html += this.getChartDiv();
        html += "</td></tr></table>";
        this.setHtml(html);

        $("#" + reloadId).button().click(function(event) {
                event.preventDefault();
                theChart.reload();
            });
        var mapProps = {"foo":"bar"};
        this.map = new RepositoryMap("mapdiv", mapProps);

        this.map.initMap(false);
        this.map.addClickHandler( this.lonFieldId, this.latFieldId);
    }

    theChart.addFieldsLegend = function() {
        if(!this.hasData()) {
            $("#" + this.fieldsDivId).html("No fields selected");
            return;
        }
        if(this.getProperty("fields",null)!=null) {
            return;
        }
        //        this.setTitle(this.getTitle());


        var html =  "<div class=chart-fields-inner>";
        var checkboxClass = this.id +"_checkbox";
        var dataList =  this.dataCollection.getData();
        for(var collectionIdx=0;collectionIdx<dataList.length;collectionIdx++) {             
            var pointData = dataList[collectionIdx];
            var fields =pointData.getChartableFields();
            html+= "<b>" + pointData.getName() + "</b><br>";
            for(i=0;i<fields.length;i++) { 
                var field = fields[i];
                field.checkboxId  = this.id +"_cbx_" + collectionIdx +"_" +i;
                html += "<span title=\"" + field.getId() +"\">";
                html += htmlUtil.checkbox(field.checkboxId, checkboxClass,
                                          field ==fields[0]);
                html += field.label+"</span><br>";
             }
        }
        html+= "</div>";
        $("#" + this.fieldsDivId).html(html);

        //Listen for changes to the checkboxes
        $("." + checkboxClass).click(function() {
                theChart.displayData();
          });
    }


    theChart.getSelectedFields = function() {
        var df = [];
        var dataList =  this.dataCollection.getData();
        var fixedFields = this.getProperty("fields");
        for(var collectionIdx=0;collectionIdx<dataList.length;collectionIdx++) {             
            var pointData = dataList[collectionIdx];
            var fields = pointData.getChartableFields();
            if(fixedFields !=null) {
                for(i=0;i<fields.length;i++) { 
                    var field = fields[i];
                    if(fixedFields.indexOf(field.getId())>=0) {
                        df.push(field);
                    }
                }
            }
        }

        if(fixedFields !=null) {
            return df;
        }

        var firstField = null;
        for(var collectionIdx=0;collectionIdx<dataList.length;collectionIdx++) {             
            var pointData = dataList[collectionIdx];
            var fields = pointData.getChartableFields();
            for(i=0;i<fields.length;i++) { 
                var field = fields[i];
                if(firstField==null) firstField = field;
                var cbxId =  this.id +"_cbx_" + collectionIdx +"_" +i;
                if($("#" + cbxId).is(':checked')) {
                    df.push(field);
                }
            }
        }

        if(df.length==0 && firstField!=null) {
            df.push(firstField);
        }

        return df;
    }


    theChart.displayData = function() {
        if(!this.hasData()) {
            if(this.chart !=null) {
                this.chart.clearChart();
            }
            return;
        }

        var selectedFields = this.getSelectedFields();
        if(selectedFields.length==0) {
            $("#" + this.chartDivId).html("No fields selected");
            return;
        }

        var dataList = this.getStandardData(selectedFields);

        var dataTable = google.visualization.arrayToDataTable(dataList);

        var options = {
            series: [{targetAxisIndex:0},{targetAxisIndex:1},],
            title: this.getTitle(),
            chartArea:{left:30,top:30,height:"75%"}
        };

        if(this.getProperty("chart.type","linechart") == "barchart") {
            options.orientation =  "horizontal";
                //            vAxis: {title: 'Year'}
            this.chart = new google.visualization.BarChart(document.getElementById(this.chartDivId));
            this.chart.draw(dataTable, options);
        } else {
            this.chart = new google.visualization.LineChart(document.getElementById(this.chartDivId));
            this.chart.draw(dataTable, options);

        }

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

    theChart.getTitle = function () {
        var title = "";
        var dataList =  this.dataCollection.getData();
        for(var collectionIdx=0;collectionIdx<dataList.length;collectionIdx++) {             
            var pointData = dataList[collectionIdx];
            if(collectionIdx>0) title+="/";
            title += pointData.getName();
        }
        return title;
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
        return this.dataCollection.hasData();
    }

    theChart.addData= function(pointData) { 
       this.dataCollection.addData(pointData);
    }

    theChart.pointDataLoaded = function(pointData) {
        this.addData(pointData);
        this.addFieldsLegend();
        this.displayData();
    }


    theChart.reload = function() {
        var dataList =  this.dataCollection.getData();
        this.dataCollection = new DataCollection();
        for(var collectionIdx=0;collectionIdx<dataList.length;collectionIdx++) {             
            var pointData = dataList[collectionIdx];
            pointData.clear();
            this.addOrLoadData(pointData);
        }
    }

    theChart.addOrLoadData = function(pointData) {
        if(pointData.hasData()) {
            this.addData(pointData);
            this.addFieldsLegend();
            this.displayData();
        } else {
            var jsonUrl = pointData.getUrl();
            var hasGeoMacro = jsonUrl.match(/(\${latitude})/g);
            var fromDate  = this.getProperty("fromdate");
            if(fromDate!=null) {
                jsonUrl += "&fromdate=" + fromDate;
            }
            var toDate  = this.getProperty("todate");
            if(toDate!=null) {
                jsonUrl += "&todate=" + toDate;
            }
            if(hasGeoMacro !=null) {
                var lat = "40.0";
                var lon = "-107";
                if(this.map!=null && this.latFieldId!=null) {
                    lat = $("#" + this.latFieldId).val();
                    lon = $("#" + this.lonFieldId).val();
                }
                if(lat!=null && lat.length>0) {
                    jsonUrl = jsonUrl.replace("${latitude}",lat);
                } else {
                    jsonUrl = jsonUrl.replace("${latitude}","40.0");
                }
                if(lon!=null && lon.length>0) {
                    jsonUrl = jsonUrl.replace("${longitude}",lon);
                } else {
                    jsonUrl = jsonUrl.replace("${longitude}","-107.0");
                }
            }
            if(jsonUrl!=null) {
                loadPointJson(jsonUrl, this, pointData);
            }
        }
    }

    theChart.getHeaderDiv = function() {
        return   "<div id=\"" + this.chartHeaderId +"\" class=chart-header></div>";
    }
    theChart.getFieldsDiv = function() {
        var height = this.getProperty("height","400");
        var style = " style=\"  overflow-y: auto;    max-height:" + height +"px;\" ";
        var div = "<div id=\"" + this.fieldsDivId +"\" class=\"chart-fields\" " + style +"></div>";
        return   div;
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
        //TODO: handle multiple data sources
        var pointData = this.dataCollection.getData()[0];

        var records = pointData.getData();
        for(j=0;j<records.length;j++) { 
            var record = records[j];
            var values = [];
            var date = record.getDate();
            //Add the date or index field
            if(date!=null) {
                date = new Date(date);
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
        //var js = "values[1] = 33;if(values[1]<360) ok= 'valuesxxx'; else ok= 'zzz';"
        return dataList;
    }
}
