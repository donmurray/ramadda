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



var globalCharts = {'foo':'bar'};

function addChart(chart) {
    globalCharts[chart.id] = chart;
}


function getChart(id) {
    return globalCharts[id];
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
    addChart(this);
    //    this.createHtml();
    var testUrl = null;
    //Uncomment to test using test.json
    //testUrl = "/repository/test.json";
    //    testUrl = "http://localhost:8080/repository/entry/show/gfs80_point?output=data.gridaspoint&entryid=633bc909-079f-429a-b873-5744a3b04770&location.latitude=34.6&location.longitude=-101.1&calendar=gregorian&fromdate=&todate=&format=json&level=-1&variable.Temperature=true"

    this.title = pointDataArg.getName();
    if(testUrl!=null) {
        pointDataArg = new PointData("Test",null,null,testUrl);
    }
    this.addOrLoadData(pointDataArg);
}


function removeChart(id) {
    var chart =getChart(id);
    if(chart) {
        chart.removeChart();
    }
}


function init_RamaddaLineChart(theChart) {
    theChart.dataCollection = new DataCollection();
    init_RamaddaChart(theChart);
    theChart.initDisplay = function() {
        var theChart = this;
        var reloadId = this.getId() +"_reload";
        $("#" + reloadId).button().click(function(event) {
                event.preventDefault();
                theChart.reload();
            });

        $("#"+this.id +"_menu_button").button({ icons: { primary:  "ui-icon-triangle-1-s"}}).click(function(event) {
                var id =theChart.getId()+"_menu_popup"; 
                showPopup(event, theChart.id +"_menu_button", id, false,null,"left bottom");
                $("#"+  this.id+"_menu_inner").superfish({
                        animation: {height:'show'},
                            delay: 1200
                            });
            });

        var mapProps = {"foo":"bar"};
        this.map = new RepositoryMap("mapdiv", mapProps);
        this.map.initMap(false);
        this.map.addClickHandler( this.lonFieldId, this.latFieldId);
        this.addFieldsLegend();
        this.displayData();
    }


    theChart.getDisplay = function() {
        var mapEnabled = this.getProperty("mapenabled",null);
        var theChart = this;
        this.latFieldId = this.getId() +"_latfield";
        this.lonFieldId = this.getId() +"_lonfield";
        var reloadId = this.getId() +"_reload";

        var html = "";
        html +=   "<div id=\"" + this.chartHeaderId +"\" class=chart-header></div>";
        var get = "getChart('" + this.id +"')";
        var deleteMe = "<a href=# onclick=\"removeChart('" + this.id +"')\"><img src=" + root +"/icons/close.gif></a>";
        var menuButton =  "<a class=chart-menu-button id=\"" + theChart.getId() +"_menu_button\"></a>";
        var menu = "<div class=ramadda-popup id=" + this.id+"_menu_popup>" + this.getFieldsDiv() +"</div>";

        html +="<table border=0 cellpadding=0 cellspacing=0><tr valign=bottom><td><b>" +  this.getTitle() +"</b></td><td align=right>";
        html += menuButton;
        html += deleteMe;

        html += "</td></tr>"
        html += "<tr valign=top><td colspan=2>"
        html += this.getChartDiv();
        html += "</td></tr></table>"
        html += menu;
        return html;
    }

    theChart.addFieldsLegend = function() {
        if(!this.hasData()) {
            $("#" + this.fieldsDivId).html("No data");
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

        //Keep all of the google chart specific code here
        var dataTable = google.visualization.arrayToDataTable(dataList);
        var options = {
            series: [{targetAxisIndex:0},{targetAxisIndex:1},],
            //            title: this.getTitle(),
            legend: { position: 'bottom' },
            chartArea:{left:0,top:0,height:"75%",width:"100%"}
        };

        var min = this.getProperty("chart.min","");
        if(min!="") {
            options.vAxis = {
                minValue:min,
            };
        }

        var chartType = this.getProperty("chart.type","linechart");
        if(chartType == "barchart") {
            options.orientation =  "horizontal";
            this.chart = new google.visualization.BarChart(document.getElementById(this.chartDivId));
            this.chart.draw(dataTable, options);
        } else  if(chartType == "table") {
            this.chart = new google.visualization.Table(document.getElementById(this.chartDivId));
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


    theChart.chartManager = null;
    
    theChart.setChartManager = function(cm) {
        this.chartManager = cm;

    }

    theChart.removeChart = function() {
        this.chartManager.removeChart(this);
    }


    theChart.setHtml = function(html) {
        $("#" + this.id).html(html);
    }

    theChart.setTitle  = function(title) {
        $("#" + this.chartHeaderId).html(title);
    }

    theChart.getTitle = function () {
        if(this.title!=null) return this.title;
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
        this.displayData();
        this.addFieldsLegend();
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




    theChart.getFieldsDiv = function() {
        var height = this.getProperty("height","400");
        var style = " style=\"  overflow-y: auto;    max-height:" + height +"px;\" ";
        var div = "<div id=\"" + this.fieldsDivId +"\" class=\"chart-fields\" " + style +"></div>";
        return   div;
    }
    theChart.getChartDiv = function() {
        return   "<div id=\"" + this.chartDivId +"\" style=\"border:0px red solid; width: " + this.getProperty("width","400px") +"; height: " + this.getProperty("height","400px") +";\"></div>\n";
    }


    theChart.getStandardData = function(fields) {
        var dataList = [];
        //The first entry in the dataList is the array of names
        //The first field is the domain, e.g., time or index
        //        var fieldNames = ["domain","depth"];
        var fieldNames = ["Date"];
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
        //TODO: handle multiple data sources (or not?)
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
                if(j==0) fieldNames[0] = "Index";
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
            if(this.filters!=null) {
                if(!this.applyFilters(record, values)) {
                    continue;
                }
            }


            //TODO: when its all null values we get some errors
            dataList.push(values);
        }
        //var js = "values[1] = 33;if(values[1]<360) ok= 'valuesxxx'; else ok= 'zzz';"
        return dataList;
    }

    
    theChart.applyFilters = function(record, values) {
        for(var i=0;i<this.filters.length;i++) {
            if(!this.filters[i].recordOk(record, values)) {
                return false;
            }
        }
        return true;
    }

    theChart.filters = [];
    var filter = theChart.getProperty("chart.filter");
    if(filter!=null) {
        //chart.filter="month:0-11;
        var toks = filter.split(":");
        var type  = toks[0];
        if(type == "month") {
            theChart.filters.push(new MonthFilter(toks[1]));
        } else {
            console.log("unknown filter:" + type);
        }
    }


}


