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



//some globals


var ID_FIELDS = "fields";
var ID_HEADER = "header";
var ID_CHART = "chart";
var ID_MENU_BUTTON = "menu_button";
var ID_MENU_POPUP = "menu_popup";
var ID_MENU_INNER = "menu_inner";
var ID_RELOAD = "reload";

var PROP_CHART_FILTER = "chart.filter";
var PROP_CHART_MIN = "chart.min";
var PROP_CHART_MAX = "chart.max";
var PROP_CHART_TYPE = "chart.type";
var PROP_DIVID = "divid";
var PROP_FIELDS = "fields";
var PROP_LAYOUT_FIXED = "layout.fixed";
var PROP_HEIGHT  = "height";
var PROP_WIDTH  = "width";

var DFLT_WIDTH = "600px";
var DFLT_HEIGHT = "200px";

var CHART_LINECHART = "linechart";
var CHART_BARCHART = "barchart";
var CHART_TABLE = "table";

function addChart(chart) {
    if(window.globalCharts == null) {
        window.globalCharts = {'foo':'bar'};
    }
    window.globalCharts[chart.id] = chart;
}


function getChart(id) {
    if(window.globalCharts == null) {
        return null;
    }
    return window.globalCharts[id];
}


function removeChart(id) {
    var chart =getChart(id);
    if(chart) {
        chart.removeChart();
    }
}




/*
Create a chart
id - the id of this chart. Has to correspond to a div tag id 
pointData - A PointData object (see below)
 */
function RamaddaLineChart(id, pointDataArg, properties) {
    this.id = id;
    init_RamaddaLineChart(this, properties);
    addChart(this);
    var testUrl = null;
    //Uncomment to test using test.json
    //testUrl = "/repository/test.json";
    this.title = pointDataArg.getName();
    if(testUrl!=null) {
        pointDataArg = new PointData("Test",null,null,testUrl);
    }
    this.addOrLoadData(pointDataArg);
}




function init_RamaddaLineChart(theChart, properties) {
    theChart.dataCollection = new DataCollection();
    theChart.indexField = -1;
    init_RamaddaChart(theChart, properties);
    theChart.initDisplay = function() {
        //If we are a fixed layout then there should be a div id property
        if(this.getIsLayoutFixed()) {
            var divid = this.getProperty(PROP_DIVID);
            if(divid!=null) {
                var html = this.getDisplay();
                $("#" + divid).html(html);
            }
        }



        var theChart = this;
        var reloadId = this.getDomId(ID_RELOAD);
        $("#" + reloadId).button().click(function(event) {
                event.preventDefault();
                theChart.reload();
            });

        $("#"+this.getDomId(ID_MENU_BUTTON)).button({ icons: { primary:  "ui-icon-triangle-1-s"}}).click(function(event) {
                var id =theChart.getDomId(ID_MENU_POPUP); 
                showPopup(event, theChart.getDomId(ID_MENU_BUTTON), id, false,null,"left bottom");
                $("#"+  theChart.getDomId(ID_MENU_INNER)).superfish({
                        animation: {height:'show'},
                            delay: 1200
                            });
            });

        this.addFieldsLegend();
        this.displayData();
    }



    theChart.addFieldsLegend = function() {
        if(!this.hasData()) {
            $("#" + this.getDomId(ID_FIELDS)).html("No data");
            return;
        }
        if(this.getProperty(PROP_FIELDS,null)!=null) {
            //            return;
        }
        //        this.setTitle(this.getTitle());

        var html =  htmlUtil.openTag("div", ["class", "chart-fields-inner"]);
        var checkboxClass = this.id +"_checkbox";
        var dataList =  this.dataCollection.getData();
        for(var collectionIdx=0;collectionIdx<dataList.length;collectionIdx++) {             
            var pointData = dataList[collectionIdx];
            var fields =pointData.getChartableFields();
            html+= htmlUtil.tag("b", [],  "Fields");
            html+= "<br>";
            for(i=0;i<fields.length;i++) { 
                var field = fields[i];
                field.checkboxId  = this.getDomId("cbx_" + collectionIdx +"_" +i);
                html += htmlUtil.tag("div", ["title", field.getId()],
                                     htmlUtil.checkbox(field.checkboxId, checkboxClass,
                                                       field ==fields[0]) +" " +field.getLabel()
);
             }
        }
        html+= htmlUtil.closeTag("div");

        $("#" + this.getDomId(ID_FIELDS)).html(html);

        //Listen for changes to the checkboxes
        $("." + checkboxClass).click(function(event) {
                theChart.displayData();
          });
    }


    theChart.getSelectedFields = function() {
        var df = [];
        var dataList =  this.dataCollection.getData();

        //If we have fixed fields then clear them after the first time
        var fixedFields = this.getProperty(PROP_FIELDS);
        if(fixedFields!=null) {
            this.removeProperty(PROP_FIELDS);
            if(fixedFields.length==0) {
                fixedFields = null;
            } 
        }
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
                var cbxId =  this.getDomId("cbx_" + collectionIdx +"_" +i);
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
        var theChart = this;
        if(!this.hasData()) {
            if(this.chart !=null) {
                this.chart.clearChart();
            }
            return;
        }

        this.allFields =  this.dataCollection.getData()[0].getRecordFields();

        var selectedFields = this.getSelectedFields();
        if(selectedFields.length==0) {
            $("#" + this.getDomId(ID_CHART)).html("No fields selected");
            return;
        }

        var dataList = this.getStandardData(selectedFields);

        //Keep all of the google chart specific code here
        if(typeof google == 'undefined') {
            $("#"+this.getDomId(ID_CHART)).html("No google");
            return;
        }

        var dataTable = google.visualization.arrayToDataTable(dataList);
        var options = {
            series: [{targetAxisIndex:0},{targetAxisIndex:1},],
            //            title: this.getTitle(),
            legend: { position: 'bottom' },
            //chartArea:{left:0,top:0,height:"75%",width:"85%"}
            chartArea:{left:50,top:10,height:"75%",width:"85%"}
        };


        var min = this.getProperty(PROP_CHART_MIN,"");
        if(min!="") {
            options.vAxis = {
                minValue:min,
            };
        }

        var chartType = this.getProperty(PROP_CHART_TYPE,CHART_LINECHART);
        if(chartType == CHART_BARCHART) {
            options.orientation =  "horizontal";
            this.chart = new google.visualization.BarChart(document.getElementById(this.getDomId(ID_CHART)));
            this.chart.draw(dataTable, options);
        } else  if(chartType == CHART_TABLE) {
            this.chart = new google.visualization.Table(document.getElementById(this.getDomId(ID_CHART)));
            this.chart.draw(dataTable, options);

        } else {
            if(false && this.hasDate) {
                this.chart = new google.visualization.AnnotatedTimeLine(document.getElementById(this.getDomId(ID_CHART)));
            } else {
                this.chart = new google.visualization.LineChart(document.getElementById(this.getDomId(ID_CHART)));
            }
            google.visualization.events.addListener(this.chart, 'select', function() {
                    var item = theChart.chart.getSelection()[0];
                    var index = item.row;
                    var records = pointData.getData();
                    var dataList =  theChart.dataCollection.getData()[0].getData();
                    var fields =  theChart.dataCollection.getData()[0].getRecordFields();
                    if(index>=0 && index<records.length) {
                        var record = records[index];
                        var values = "<table>";
                        if(!record.hasLocation()) return;
                        
                        var latitude = record.getLatitude();
                        var longitude = record.getLongitude();
                        values+= "<tr><td align=right><b>Latitude:</b></td><td>" + latitude + "</td></tr>";
                        values+= "<tr><td align=right><b>Longitude:</b></td><td>" + longitude + "</td></tr>";
                        if(record.hasElevation()) {
                            values+= "<tr><td  align=right><b>Elevation:</b></td><td>" + record.getElevation() + "</td></tr>";
                        }
                        for(var i=0;i<record.getData().length;i++) {
                            var label = fields[i].getLabel();
                            values+= "<tr><td align=right><b>" + label +":</b></td><td>" + record.getValue(i) + "</td></tr>";
                        }
                        values += "</table>";
                        if(theChart.chartManager.hasMap()) {
                            var point = new OpenLayers.LonLat(longitude, latitude);
                            if(theChart.lastMarker!=null) {
                                theChart.chartManager.map.removeMarker(theChart.lastMarker);
                            }
                            //                        theChart.chartManager.map.setCenter(point);
                            theChart.lastMarker =  theChart.chartManager.map.addMarker(theChart.getId(), point, null,values);
                        }
                    }

             });

            this.chart.draw(dataTable, options);
        }
    }
}



/*
Create a chart
id - the id of this chart. Has to correspond to a div tag id 
pointData - A PointData object (see below)
 */
function RamaddaScatterChart(id, pointDataArg, properties) {
    this.id = id;
    init_RamaddaScatterChart(this, properties);
    addChart(this);
    this.title = pointDataArg.getName();
    this.addOrLoadData(pointDataArg);
}



function init_RamaddaScatterChart(theChart, properties) {
    theChart.dataCollection = new DataCollection();
    init_RamaddaChart(theChart,properties);
    theChart.initDisplay = function() {
        var theChart = this;
        var reloadId = this.getDomId(ID_RELOAD);
        $("#" + reloadId).button().click(function(event) {
                event.preventDefault();
                theChart.reload();
            });

        $("#"+this.getDomId(ID_MENU_BUTTON)).button({ icons: { primary:  "ui-icon-triangle-1-s"}}).click(function(event) {
                var id =this.getDomId(ID_MENU_POPUP); 
                showPopup(event, this.getDomId(ID_MENU_BUTTON), id, false,null,"left bottom");
                $("#"+  this.getDomId(ID_MENU_INNER)).superfish({
                        animation: {height:'show'},
                            delay: 1200
                            });
            });

        this.addFieldsLegend();
        this.displayData();
    }



    theChart.addFieldsLegend = function() {
        if(!this.hasData()) {
            $("#" + this.getDomId(ID_FIELDS)).html("No data");
            return;
        }
        if(this.getProperty(PROP_FIELDS,null)!=null) {
            return;
        }
        //        this.setTitle(this.getTitle());


        var html =  htmlUtil.openTag("div", ["class", "chart-fields-inner"]);
        var checkboxClass = this.id +"_checkbox";
        var dataList =  this.dataCollection.getData();
        for(var collectionIdx=0;collectionIdx<dataList.length;collectionIdx++) {             
            var pointData = dataList[collectionIdx];
            var fields =pointData.getChartableFields();
            html+= "<b>" + pointData.getName() + "</b><br>";
            for(i=0;i<fields.length;i++) { 
                var field = fields[i];
                field.checkboxId  = this.getDomId("cbx_" + collectionIdx +"_" +i);
                html += htmlUtil.tag("span", ["title",  field.getId()], 
                                     htmlUtil.checkbox(field.checkboxId, checkboxClass,
                                                       field ==fields[0]));
                html += "<br>";
             }
        }
        html+= htmlUtil.closeTag("div");
        $("#" + this.getDomId(ID_FIELDS)).html(html);

        //Listen for changes to the checkboxes
        $("." + checkboxClass).click(function() {
                theChart.displayData();
          });
    }


    theChart.getSelectedFields = function() {
        var df = [];
        var dataList =  this.dataCollection.getData();
        var fixedFields = this.getProperty(PROP_FIELDS);


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
                var cbxId =  this.getDomId("_cbx_" + collectionIdx +"_" +i);
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
            $("#" + this.getDomId(ID_CHART)).html("No fields selected");
            return;
        }

        var dataList = this.getStandardData(selectedFields);

        //Keep all of the google chart specific code here
        if(typeof google == 'undefined') {
            return;
        }

        var dataTable = google.visualization.arrayToDataTable(dataList);
        var options = {
            series: [{targetAxisIndex:0},{targetAxisIndex:1},],
            legend: { position: 'bottom' },
            chartArea:{left:0,top:0,height:"75%",width:"85%"}
        };


        //this.chart = new google.visualization.BarChart(document.getElementById(this.getDomId(ID_CHART)));
        //this.chart.draw(dataTable, options);
    }
}




function init_RamaddaChart(theChart, propertiesArg) {
    if(propertiesArg == null) {
       propertiesArg = {'':''};
    }
    $.extend(theChart, {
            properties: propertiesArg,
            chartManager:null,
                });
    init_ChartThing(theChart);

    $.extend(theChart, {
            filters: [],
           setChartManager: function(cm) {
                this.chartManager = cm;
                this.setParent(cm);
            },
            getDisplay: function() {
                var theChart = this;
                var reloadId = this.getDomId(ID_RELOAD);
                var html = "";
                html +=   htmlUtil.div(["id", this.getDomId(ID_HEADER),"class", "chart-header"]);
                var get = "getChart('" + this.id +"')";
                var deleteMe = htmlUtil.onClick("removeChart('" + this.id +"')", "<img src=" + root +"/icons/close.gif> Remove Chart");
                var menuButton =  htmlUtil.tag("a", ["class", "chart-menu-button", "id",  this.getDomId(ID_MENU_BUTTON)]);
                var menu = htmlUtil.div(["class", "ramadda-popup", "id", this.getDomId(ID_MENU_POPUP)], this.getFieldsDiv()+"<hr>" + deleteMe);

                html += htmlUtil.openTag("table", ["border", "0", "cellpadding","0", "cellspacing","0"]);
                html += htmlUtil.openTag("tr", ["valign", "bottom"]);
                html += htmlUtil.td([], htmlUtil.b(this.getTitle()));
                html += htmlUtil.td(["align", "right"],
                                    menuButton);
                html += htmlUtil.closeTag("tr");

                var chartDiv = htmlUtil.div(["id", this.getDomId(ID_CHART),"style", "border:0px red solid; width: " + this.getProperty(PROP_WIDTH,DFLT_WIDTH) +"; height: " + this.getProperty(PROP_HEIGHT,DFLT_HEIGHT) +";"]);

                html += htmlUtil.tr(["valign", "top"], htmlUtil.td(["colspan", "2","id"],chartDiv));
                html += htmlUtil.closeTag("table")
                html += menu;
                return html;
            },
            removeChart: function() {
                this.chartManager.removeChart(this);
                if(theChart.lastMarker!=null) {
                    this.chartManager.map.removeMarker(theChart.lastMarker);
                }
            },
            setHtml: function(html) {
                $("#" + this.id).html(html);
            },
            setTitle: function(title) {
                $("#" +  this.getDomId(ID_HEADER)).html(title);
            },
            getTitle: function () {
                if(this.title!=null) return this.title;
                var title = "";
                var dataList =  this.dataCollection.getData();
                for(var collectionIdx=0;collectionIdx<dataList.length;collectionIdx++) {             
                    var pointData = dataList[collectionIdx];
                    if(collectionIdx>0) title+="/";
                    title += pointData.getName();
                }
                return title;
            },
            getIsLayoutFixed: function() {
                return this.getProperty(PROP_LAYOUT_FIXED,false);
            },
            hasData: function() {
                return this.dataCollection.hasData();
            },
            addData: function(pointData) { 
                this.dataCollection.addData(pointData);
            },
            pointDataLoaded: function(pointData) {
                this.addData(pointData);
                this.displayData();
                this.addFieldsLegend();
            },
            reload: function() {
                var dataList =  this.dataCollection.getData();
                this.dataCollection = new DataCollection();
                for(var collectionIdx=0;collectionIdx<dataList.length;collectionIdx++) {             
                    var pointData = dataList[collectionIdx];
                    pointData.clear();
                    this.addOrLoadData(pointData);
                }
            },
            addOrLoadData: function(pointData) {
                if(pointData.hasData()) {
                    this.addData(pointData);
                } else {
                    var jsonUrl = pointData.getUrl();
                         
                    if(jsonUrl!=null) {
                        loadPointJson(jsonUrl, this, pointData);
                    }
                }
            },
           getFieldsDiv: function() {
                var height = this.getProperty(PROP_HEIGHT,"400");
                return htmlUtil.div(["id",  this.getDomId(ID_FIELDS),"class", "chart-fields","style","overflow-y: auto;    max-height:" + height +"px;"]);
            },
            getStandardData : function(fields) {
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
                this.hasDate = true;

                var indexField = this.indexField;
                var allFields = this.allFields;

                var north=NaN,west=NaN,south=NaN,east=NaN;
                //        var bounds = new OpenLayers.Bounds(-151.827, 59.3397, -151.074, 59.7779);
                //        this.map.centerOnMarkers(bounds);

                var points =[];
                var records = pointData.getData();
                for(j=0;j<records.length;j++) { 
                    var record = records[j];
                    var values = [];
                    var date = record.getDate();
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
                    //Add the date or index field
           
                    if(indexField>=0) {
                        var field = allFields[indexField];
                        var value = record.getValue(indexField);
                        if(j==0) {
                            console.log("index field:" +  field.getLabel() + " ex:" + value);
                            fieldNames[0] = field.getLabel();
                        }
                        values.push(value);
                    } else {
                        if(date!=null) {
                            date = new Date(date);
                            values.push(date);
                        } else {
                            if(j==0) {
                                this.hasDate = false;
                                fieldNames[0] = "Index";
                            }
                            values.push(j);
                        }
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
                if(!isNaN(north)) {
                    this.chartManager.setInitMapBounds(north, west, south, east);
                    if(points.length>1) {
                        this.chartManager.addPolygon("id",points, null);
                    }
                }

                return dataList;
            },
            applyFilters: function(record, values) {
                for(var i=0;i<this.filters.length;i++) {
                    if(!this.filters[i].recordOk(record, values)) {
                        return false;
                    }
                }
                return true;
            }
        }
        );



        var filter = theChart.getProperty(PROP_CHART_FILTER);
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


