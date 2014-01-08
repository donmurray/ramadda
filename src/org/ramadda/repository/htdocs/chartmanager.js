
function ChartManager(id,properties) {
    var theChart = this;
    this.id = id;
    this.properties = properties;
    this.charts = [];
    this.data = [];
    this.cnt = 0;
    init_ChartManager(this);
    var html = "";
    if(this.getProperty("shownew",true)) {
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
        var props = pointData.getProperties();
        //        props.width = 400;
        //        props.height = 200;
        var chart  = new RamaddaLineChart(chartId, pointData, props);
        this.data.push(pointData);
        this.charts.push(chart);
    }

    chartManager.addLineChart = function(pointDataArg, properties) {
        var chartId = this.id +"_chart_" + (this.cnt++);
        var chart  = new RamaddaLineChart(chartId, pointDataArg, properties);
        this.charts.add(chart);
    }
}


