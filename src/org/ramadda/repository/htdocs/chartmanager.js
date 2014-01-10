
var globalChartManagers = {'foo':'bar'};

function addChartManager(chartManager) {
    globalChartManagers[chartManager.getId()] = chartManager;
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
    init_ChartManager(this);
    addChartManager(this);
    var get = "getChartManager('" + id +"')";
    var html = "<div class=\"chart-container\">";
    var new1= "<a onclick=\"" + get +".newTimeseries()\">Timeseries</a>";
    var new2= "<a onclick=\"" + get +".newBarchart()\">Barchart</a>";
    var layout = "<li><a onclick=\"" + get +".setLayout('table',1)\">Table - 1 column</a></li><li><a onclick=\"" + get +".setLayout('table',2)\">Table - 2 column</a></li><li><a onclick=\"" + get +".setLayout('tabs')\">Tabs</a></li>"
    var menu = "<div class=ramadda-popup id=" + this.id+"_menu_popup><ul id=" + this.id+"_menu_inner sample-menu class=sf-menu><li><a>New</a><ul><li>" + new1 +"</li><li>" + new2 +"</li></ul></li><li><a>Layout</a><ul>" + layout +"</ul></li></ul></div>";

    html+= menu;
    html += "<a class=chart-menu-button id=\"" + this.id +"_menu_button\"></a><br>";

    html+= "<div id=\"" + this.id +"_charts\"></div>";
    $("#"+ this.getId()).html(html);


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

    chartManager.getProperty = function(key, dflt) {
        if(typeof this.properties == 'undefined') return dflt;
        var value = this.properties[key];
        if(value == null) return dflt;
        return value;
    }

    chartManager.doLayout = function() {
        var html = "";
        var colCnt=0;
        if(this.layout == "table") {
            html+="<table width=100%><tr valign=top>"
                for(var i=0;i<this.charts.length;i++) {
                    colCnt++;
                    if(colCnt>this.columns) {
                        html+= "</tr><tr valign=top>"
                            }
                    html+="<td><div>";
                    html+=this.charts[i].getDisplay();
                    html+="</div></td>"
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


