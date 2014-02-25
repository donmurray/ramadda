/**
Copyright 2008-2014 Geode Systems LLC

This package supports charting and mapping of georeferenced time series data
It requires displaymanager.js pointdata.js
*/

var DISPLAY_LINECHART = "linechart";
var DISPLAY_BARCHART = "barchart";
var DISPLAY_TABLE = TAG_TABLE;
var DISPLAY_TEXT = "text";



addGlobalDisplayType({type: DISPLAY_LINECHART, label:"Line chart",requiresData:true});
addGlobalDisplayType({type:DISPLAY_BARCHART,label: "Bar chart",requiresData:true});
addGlobalDisplayType({type:DISPLAY_TABLE , label: "Table",requiresData:true});

addGlobalDisplayType({type:DISPLAY_TEXT , label: "Text Readout",requiresData:false});




var PROP_CHART_MIN = "chartMin";
var PROP_CHART_MAX = "chartMax";
var PROP_CHART_TYPE = "chartType";
var DFLT_WIDTH = "600px";
var DFLT_HEIGHT = "200px";




/*
Create a chart
id - the id of this chart. Has to correspond to a div tag id 
pointData - A PointData object (see below)
 */
function RamaddaMultiChart(displayManager, id, properties) {
    var SUPER; 
    var ID_CHART = "chart";
    //Init the defaults first
    $.extend(this, {
            indexField: -1,
            colors: ['blue', 'red', 'green'],
            curveType: 'none',
            fontSize: 0,
            vAxisMinValue:NaN,
            vAxisMaxValue:NaN
           });

    RamaddaUtil.inherit(this, SUPER = new RamaddaDisplay(displayManager, id, properties.chartType, properties));

    RamaddaUtil.defineMembers(this, {
            getType: function () {
                return this.getProperty(PROP_CHART_TYPE,DISPLAY_LINECHART);
            },
            needsData: function() {
                return true;
            },
            initDisplay:function() {
                this.initUI();
                this.updateUI();
            },
            updateUI: function() {
                this.addFieldsCheckboxes();
                this.displayData();
            },
            fieldSelectionChanged: function() {
                this.displayData();
            },
            initDialog: function() {
                this.addFieldsCheckboxes();
            },
            getDialogContents: function() {
                var height = this.getProperty(PROP_HEIGHT,"400");
                var html  =  HtmlUtil.div([ATTR_ID,  this.getDomId(ID_FIELDS),"style","overflow-y: auto;    max-height:" + height +"px;"],"FIELDS");
                html +=  SUPER.getDialogContents.apply(this);
                return html;
            },
            handleEventMapClick: function (source,args) {
                this.data.handleEventMapClick(this, source, args.lon,args.lat);
            },
            handleEventRecordSelection: function(source, args) {
                var chartType = this.getProperty(PROP_CHART_TYPE,DISPLAY_LINECHART);
                if(source==this) {
                    return;
                }
                this.setChartSelection(args.index);
            },
            displayData: function() {
                if(this.getShowTitle()) {
                    this.setTitle(this.getTitle());
                }
                if(!this.hasData()) {
                    this.clearChart();
                    this.setContents(HtmlUtil.div([ATTR_CLASS,"display-message"],
                                                  this.getLoadingMessage()));
                    return;
                }

                this.allFields =  this.dataCollection.getList()[0].getRecordFields();

                var selectedFields = this.getSelectedFields();
                if(selectedFields.length==0) {
                    this.setContents("No fields selected");
                    return;
                }
                var dataList = this.getStandardData(selectedFields);
                if(dataList.length==0) {
                    this.setContents(HtmlUtil.div([ATTR_CLASS,"display-message"],
                                                  "No data available"));
                    return;
                }

                var chartType = this.getProperty(PROP_CHART_TYPE,DISPLAY_LINECHART);
                this.makeChart(chartType, dataList, selectedFields);
            },
            clearChart: function() {
                if(this.chart !=null) {
                    this.chart.clearChart();
                }
            },
            setChartSelection: function(index) {
                if(this.chart!=null) {
                    this.chart.setSelection([{row:index, column:null}]); 
                }
            },
            makeGoogleChart: function(chartType, dataList, selectedFields) {
                if(typeof google == 'undefined') {
                    this.setContents("No google");
                    return;
                }
                var dataTable = google.visualization.arrayToDataTable(dataList);
                var   chartOptions = {};
                $.extend(chartOptions, {
                        lineWidth: 1,
                        colors: this.colors,
                        curveType:this.curveType,
                        vAxis: {}});


                if(this.fontSize>0) {
                    chartOptions.fontSize = this.fontSize;
                }
                if(!isNaN(this.vAxisMinValue)) {
                    chartOptions.vAxis.minValue =parseFloat(this.vAxisMinValue);
                }
                if(!isNaN(this.vAxisMaxValue)) {
                    chartOptions.vAxis.maxValue =parseFloat(this.vAxisMaxValue);
                }
                var width = "95%";
                if(selectedFields.length>1) {
                    width = "80%";
                }
                $.extend(chartOptions, {
                    series: [{targetAxisIndex:0},{targetAxisIndex:1},],
                    legend: { position: 'bottom' },
                    chartArea:{left:75,top:10,height:"60%",width:width}
                 });
                var chartId = this.getDomId(ID_CHART);
                this.setContents(HtmlUtil.div([ATTR_ID, chartId],""));

                if(chartType == DISPLAY_BARCHART) {
                    chartOptions.orientation =  "horizontal";
                    this.chart = new google.visualization.BarChart(document.getElementById(chartId));
                } else  if(chartType == DISPLAY_TABLE) {
                    this.chart = new google.visualization.Table(document.getElementById(chartId));
                } else {
                    //                    this.chart =  new Dygraph.GVizChart(
                    //                    document.getElementById(chartId));
                    this.chart = new google.visualization.LineChart(document.getElementById(chartId));
                }
                if(this.chart!=null) {
                    this.chart.draw(dataTable, chartOptions);
                    var theDisplay = this;
                    google.visualization.events.addListener(this.chart, 'select', function() {
                            var index = theDisplay.chart.getSelection()[0].row;
                            theDisplay.displayManager.handleEventRecordSelection(theDisplay, 
                                                                                 theDisplay.dataCollection.getList()[0], index);
                        });
                }
            }
        });


    this.makeChart = this.makeGoogleChart;
}



function LinechartDisplay(displayManager, id, properties) {
    properties = $.extend({"chartType": DISPLAY_LINECHART}, properties);
    RamaddaUtil.inherit(this, new RamaddaMultiChart(displayManager, id, properties));
    addRamaddaDisplay(this);
}

function BarchartDisplay(displayManager, id, properties) {
    properties = $.extend({"chartType": DISPLAY_BARCHART}, properties);
    RamaddaUtil.inherit(this, new RamaddaMultiChart(displayManager, id, properties));
    addRamaddaDisplay(this);
}


function TableDisplay(displayManager, id, properties) {
    properties = $.extend({"chartType": DISPLAY_TABLE}, properties);
    RamaddaUtil.inherit(this, new RamaddaMultiChart(displayManager, id, properties));
    addRamaddaDisplay(this);
}


function RamaddaTextDisplay(displayManager, id, properties) {
    var SUPER;
    $.extend(this, SUPER = new RamaddaDisplay(displayManager, id, DISPLAY_TEXT, properties));
    addRamaddaDisplay(this);
    RamaddaUtil.defineMembers(this, {
            lastHtml:"<p>&nbsp;<p>&nbsp;<p>",
            initDisplay: function() {
                this.initUI();
                this.setContents(this.lastHtml);
            },
            handleEventRecordSelection: function(source,  args) {
                this.lastHtml = args.html;
                this.setContents(args.html);
            }
        });
}


