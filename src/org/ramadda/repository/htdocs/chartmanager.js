
var globalChartManagers = {'foo':'bar'};
var globalChartManager = null;

function addChartManager(chartManager) {
    globalChartManagers[chartManager.getId()] = chartManager;
    globalChartManager = chartManager;
}


function getOrCreateChartManager(id, properties) {
    var chartManager = getChartManager(id);
    if(chartManager != null) {
        return chartManager;
    }
    if(globalChartManager!=null) {
        return globalChartManager;
    }
    globalChartManager =  new ChartManager(id, properties);
    return globalChartManager;
}

function getChartManager(id) {
    return globalChartManagers[id];
}


function ChartManager(id,properties) {
    var theChart = this;
    this.id = id;
    this.properties = properties;
    this.charts = [];
    this.data = [];
    this.cnt = 0;
    this.layout="table";
    this.columns=1;
    var entryUrl = "/repository/search/type/type_point_noaa_carbon?type=type_point_noaa_carbon&search.type_point_noaa_carbon.site_id=MLO&datadate.mode=overlaps&output=json&max=3";
    this.entryList = new  EntryList(entryUrl, this);
    init_ChartManager(this);
    addChartManager(this);
    //How else do I refer to this object in the html that I add 
    var get = "getChartManager('" + id +"')";
    var html = "<div class=\"chart-container\">";
    var new1= "<a onclick=\"" + get +".newTimeseries()\">Timeseries</a>";
    var new2= "<a onclick=\"" + get +".newBarchart()\">Barchart</a>";
    var layout = "<li><a onclick=\"" + get +".setLayout('table',1)\">Table - 1 column</a></li><li><a onclick=\"" + get +".setLayout('table',2)\">Table - 2 column</a><li><a onclick=\"" + get +".setLayout('table',3)\">Table - 3 column</a></li><li><a onclick=\"" + get +".setLayout('tabs')\">Tabs</a></li>"
    var menu = "<div class=ramadda-popup id=" + this.id+"_menu_popup><ul id=" + this.id+"_menu_inner sample-menu class=sf-menu><li><a>New</a><ul><li>" + new1 +"</li><li>" + new2 +"</li></ul></li><li><a>Layout</a><ul>" + layout +"</ul></li></ul></div>";

    html+= menu;
    html += "<a class=chart-menu-button id=\"" + this.id +"_menu_button\"></a><br>";

    html+= "<table width=100%><tr valign=top>";
    //    html+="<td>";
    //    html+="<b>Entries</b>";
    //    html+="<div class=chart-entry-list-wrapper><div id=" + this.id+"_entries class=chart-entry-list></div></div>";
    //    html+="</td>";
    html+="<td>";
    html+= "<div id=\"" + this.id +"_charts\"></div>";

    html+="<td width=300>";

    $("#"+ this.getId()).html(html);

    if(this.entryList) {
        //this.entryList.initDisplay(this.id+"_entries");
    }


    $("#"+this.id +"_menu_button").button({ icons: { primary: "ui-icon-gear", secondary: "ui-icon-triangle-1-s"}}).click(function(event) {
            var id =theChart.getId()+"_menu_popup"; 
            showPopup(event, theChart.id +"_menu_button", id, false,null,"left bottom");
            $("#"+  this.id+"_menu_inner").superfish({
                    animation: {height:'show'},
                        delay: 1200
                        });
        });
}


function init_ChartManager(chartManager) {
    chartManager.getId = function() {
        return this.id;
    }

    chartManager.getPosition = function() {
        var lat = "40";
        var lon = "-105";
        return [lat,lon];
    }
    
    /*
        var mapProps = {"foo":"bar"};
        this.map = new RepositoryMap("mapdiv", mapProps);
        this.map.initMap(false);
        this.map.addClickHandler( this.lonFieldId, this.latFieldId);
    */


    chartManager.getJsonUrl = function(jsonUrl, chart) {
        var hasGeoMacro = jsonUrl.match(/(\${latitude})/g);
        var fromDate  = chart.getProperty("fromdate");
        if(fromDate!=null) {
            jsonUrl += "&fromdate=" + fromDate;
        }
        var toDate  = chart.getProperty("todate");
        if(toDate!=null) {
            jsonUrl += "&todate=" + toDate;
        }
        if(hasGeoMacro !=null) {
            var tuple = this.getPosition();
            var lat = tuple[0];
            var lon = tuple[1];
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
        return jsonUrl;
    }

    chartManager.entryListChanged = function(entryList) {
        entryList.setHtml(entryList.getHtml());
    }

    chartManager.getProperty = function(key, dflt) {
        if(typeof this.properties == 'undefined') return dflt;
        var value = this.properties[key];
        if(value == null) return dflt;
        return value;
    }

    chartManager.doLayout = function() {
        var html = "";
        var colCnt=100;
        if(this.layout == "table") {
            html+="<table width=100% cellpadding=5 cellspacing=5>";
            for(var i=0;i<this.charts.length;i++) {
                colCnt++;
                if(colCnt>=this.columns) {
                    if(i>0) {
                        html+= "</tr>";
                    }
                    html+= "<tr valign=top>";
                    colCnt=0;
                }
                html+="<td><div>";
                html+=this.charts[i].getDisplay();
                html+="</div></td>";
            }
            html+= "</tr></table>";
        } else if(this.layout=="tabs") {
        } else {
            html+="Unknown layout:" + this.layout;
        }
        $("#" + this.getId() +"_charts").html(html);
        for(var i=0;i<this.charts.length;i++) {
            this.charts[i].initDisplay();
        }
    }


    chartManager.setLayout = function(layout, columns) {
        this.layout  = layout;
        if(columns) {
            this.columns  = columns;
        }
        this.doLayout();
    }

    chartManager.newTimeseries= function(data) {
        if(data == null) {
            data = this.data[0];
        }
        var chartManager = this;
        setTimeout(function(){chartManager.addPointData(data);},1);
    }

    chartManager.newBarchart = function(data) {
        if(data == null) {
            data = this.data[0];
        }
        var chartManager = this;
        setTimeout(function(){chartManager.addPointData(data,'barchart');},1);
    }


    chartManager.addPointData = function(pointData, type) {
        var chartId = this.id +"_chart_" + (this.cnt++);
        var props = pointData.getProperties();
        var newProps = {};
        for (var i in props) {
            newProps[i] = props[i];
        }
        props = newProps;
        //        props.width = 400;
        props.height = 200;
        props["chart.type"] = type;
        var chart  = new RamaddaLineChart(chartId, pointData, props);
        chart.setChartManager(this);
        this.data.push(pointData);
        this.charts.push(chart);
        this.doLayout();
    }

    chartManager.removeChart = function(chart) {
        var index = this.charts.indexOf(chart);
        if(index >= 0) { 
            this.charts.splice(index, 1);
        }   
        var chartmanager = this;
        setTimeout(function(){chartManager.doLayout();},1);
    }


    chartManager.addLineChart = function(pointDataArg, properties) {
        var chartId = this.id +"_chart_" + (this.cnt++);
        var chart  = new RamaddaLineChart(chartId, pointDataArg, properties);
        chart.setChartManager(this);
        this.charts.add(chart);
    }
}


