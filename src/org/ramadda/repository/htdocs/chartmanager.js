

//
//adds the chartmanager to the list of global chartmanagers
//
function addChartManager(chartManager) {
    if(window.globalChartManagers == null) {
        window.globalChartManagers =  {'foo':'bar'};
        window.globalChartManager = null;
    }
    window.globalChartManagers[chartManager.getId()] = chartManager;
    window.globalChartManager = chartManager;
}


//
//This will get the currently created global chartmanager or will create a new one
//
function getOrCreateChartManager(id, properties) {
    var chartManager = getChartManager(id);
    if(chartManager != null) {
        return chartManager;
    }
    if(window.globalChartManager!=null) {
        return window.globalChartManager;
    }
    window.globalChartManager =  new ChartManager(id, properties);
    return window.globalChartManager;
}

//
//return one the global chart manager with the given id
//
function getChartManager(id) {
    if(window.globalChartManagers==null) {
        return null;
    }
    return window.globalChartManagers[id];
}


//
//ChartManager constructor
//
function ChartManager(id,properties) {
    var theChart = this;
    this.id = id;
    this.properties = properties;
    if(this.properties == null) {
        this.properties == {'':''};
    }
    this.charts = [];
    this.data = [];
    this.cnt = 0;
    this.layout="table";
    this.columns=1;

    //This is test for listing a set of entries
    var entryUrl = "/repository/search/type/type_point_noaa_carbon?type=type_point_noaa_carbon&search.type_point_noaa_carbon.site_id=MLO&datadate.mode=overlaps&output=json&max=3";
    this.entryList = new  EntryList(entryUrl, this);

    init_ChartManager(this);

    this.mapEnabled = this.getProperty("mapenabled",null);
    this.mapBoundsSet  = false;


    if(this.mapEnabled) {
        this.latFieldId = this.getDomId("latfield");
        this.lonFieldId = this.getDomId("lonfield");
    }


    addChartManager(this);

    var html = htmlUtil.openTag("div", ["class","chart-container"]);

    html += this.makeMainMenu();


    html += htmlUtil.openTag("table",["width","100%","border","0"]);
    html += htmlUtil.openTag("tr", ["valign","top"]);

    //    html+="<td>";
    //    html+="<b>Entries</b>";
    //    html+="<div class=chart-entry-list-wrapper><div id=" + this.getDomId("entries") +"  class=chart-entry-list></div></div>";
    //    html+="</td>";


    //this is the div where the charts go
    html+=htmlUtil.tag("td", [],  htmlUtil.div(["id", this.getDomId("charts")]));

    html+=htmlUtil.openTag("td", ["width", "300"]);

    if(this.mapEnabled) {
        html+= htmlUtil.tag("h3",[],"Map");
        html+= htmlUtil.openTag("form");
        html+= "Latitude: " + htmlUtil.input(this.latFieldId, "", ["size","10","id",  this.latFieldId]);
        html+= "  ";
        html+= "Longitude: " + htmlUtil.input(this.lonFieldId, "", ["size","10","id",  this.lonFieldId]);
        html+= htmlUtil.closeTag("form");
        html+=htmlUtil.div(["style", "width:400px; height:400px;",
                            "class", "chart-map",
                            "id", this.getDomId("map")]);
    }


    //This is where we can put time selectors, etc
    html+= htmlUtil.tag("h3",[],"Selection");
    html+=htmlUtil.openTag("form");
    html+=" Put selection form here";
    html+=htmlUtil.closeTag("form");

    html+=htmlUtil.closeTag("td");
    html+=htmlUtil.closeTag("table");

    $("#"+ this.getId()).html(html);

    this.mapCentered = false;
    if(this.mapEnabled) {
        var params = {};
        this.map = new RepositoryMap(this.getDomId("map"), params);
        this.map.initMap(false);
        this.map.addClickHandler( this.lonFieldId, this.latFieldId);
        //        this.map.addLine('0e9d5f64-823a-4bdf-813b-bb3f4d80f6e2_polygon', 59.772422500000005, -151.10694375000003, 59.7614675, -151.14459375);
    }

    if(this.entryList) {
        //this.entryList.initDisplay(this.getDomId("entries"));
    }


    $("#"+this.getDomId("menu_button")).button({ icons: { primary: "ui-icon-gear", secondary: "ui-icon-triangle-1-s"}}).click(function(event) {
            var id =theChart.getDomId("menu_popup"); 
            showPopup(event, theChart.getDomId("menu_button"), id, false,null,"left bottom");
            $("#"+  theChart.getDomId("menu_inner")).superfish({
                    animation: {height:'show'},
                        delay: 1200
                        });
        });
}


function init_ChartManager(chartManager) {

    chartManager.makeMainMenu = function() {
        if(!this.getProperty("show.menu", true))  {
            return "";
        }
        //How else do I refer to this object in the html that I add 
        var get = "getChartManager('" + this.getId() +"')";
        var html = "";
        var wider = "<a onclick=\"" + get +".changeChartWidth(1)\">+ Chart width</a>";
        var narrower = "<a onclick=\"" + get +".changeChartWidth(-1)\">- Chart width</a>";
        var chartNames = ["Time Series","Bar Chart","Scatter Plot", "Table"];
        var chartCalls = ["newTimeseries();","newBarchart();","newScatterPlot();", "newTable();"];
        var newMenu = "";
        for(var i=0;i<chartNames.length;i++) {
            newMenu+= htmlUtil.tag("li",[], htmlUtil.tag("a", ["onclick", get+"." + chartCalls[i]], chartNames[i]));
        }

        var layoutMenu = 
        htmlUtil.tag("li",[], htmlUtil.tag("a", ["onclick",  get +".setLayout('table',1)"], "Table - 1 column")) +
        htmlUtil.tag("li",[], htmlUtil.tag("a", ["onclick",  get +".setLayout('table',2)"], "Table - 2 column")) +
        htmlUtil.tag("li",[], htmlUtil.tag("a", ["onclick",  get +".setLayout('table',3)"], "Table - 3 column")) +
        htmlUtil.tag("li",[], htmlUtil.tag("a", ["onclick",  get +".setLayout('tabs')"], "Tabs"));

        var menu = htmlUtil.div(["class","ramadda-popup", "id", this.getDomId("menu_popup")], 
                                htmlUtil.tag("ul", ["id", this.getDomId("menu_inner"),"class", "sf-menu"], 
                                        htmlUtil.tag("li",[],"<a>New</a>" + htmlUtil.tag("ul",[], newMenu)) +
                                        htmlUtil.tag("li",[],"<a>Layout</a>" + htmlUtil.tag("ul", [], layoutMenu))));

        html += menu;
        html += htmlUtil.tag("a", ["class", "chart-menu-button", "id", this.getDomId("menu_button")]);
        html+="<br>";

        return html;
    }

    chartManager.getId = function() {
        return this.id;
    }


    chartManager.getDomId = function(suffix) {
        return this.getId() +"_" + suffix;
    }




    chartManager.hasMap = function() {
        return this.map!=null;
    }


    chartManager.addPolygon = function(id, points, props) {
        if(!this.map) return;
        this.map.addPolygon(id, points, props);
    }


    chartManager.setMapBounds = function(north, west, south, east) {
        if(!this.map) return;
        if(this.mapBoundsSet) return;
        this.mapBoundsSet = true;
        var bounds = new OpenLayers.Bounds(west,south,east, north);
        this.map.centerOnMarkers(bounds);
    }

    chartManager.getPosition = function() {
        var lat = "40";
        var lon = "-105";
        return [lat,lon];
    }
    


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

    chartManager.changeChartWidth = function(w) {
        
    }

    chartManager.getChartsToLayout = function() {
        var result = [];
        for(var i=0;i<this.charts.length;i++) {
            if(this.charts[i].getIsLayoutFixed()) continue;
            result.push(this.charts[i]);
        }
        return result;
    }


    chartManager.doLayout = function() {
        var html = "";
        var colCnt=100;
        var chartsToLayout = this.getChartsToLayout();
        if(this.layout == "table") {
            html+=htmlUtil.openTag("table", ["width", "100%", "cellpadding", "5",  "cellspacing", "5"]);
            for(var i=0;i<chartsToLayout.length;i++) {
                colCnt++;
                if(colCnt>=this.columns) {
                    if(i>0) {
                        html+= htmlUtil.closeTag("tr");
                    }
                    html+= htmlUtil.openTag("tr",["valign", "top"]);
                    colCnt=0;
                }
                html+=htmlUtil.tag("td", [], htmlUtil.div([], chartsToLayout[i].getDisplay()));
            }
            html+= htmlUtil.closeTag("tr");
            html+= htmlUtil.closeTag("table");
        } else if(this.layout=="tabs") {
        } else {
            html+="Unknown layout:" + this.layout;
        }
        $("#" + this.getDomId("charts")).html(html);

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

    chartManager.newScatterPlot = function(data) {
        if(data == null) {
            data = this.data[0];
        }
        var chartManager = this;
        setTimeout(function(){chartManager.addPointData(data,'scatterplot');},1);
    }

    chartManager.newTable = function(data) {
        if(data == null) {
            data = this.data[0];
        }
        var chartManager = this;
        setTimeout(function(){chartManager.addPointData(data,'table');},1);
    }


    chartManager.addPointData = function(pointData, chartType) {
        var chartId = this.id +"_chart_" + (this.cnt++);
        var props = pointData.getProperties();
        var newProps = {};
        for (var i in props) {
            newProps[i] = props[i];
        }
        props = newProps;
        //        props.width = 400;
        props.height = 200;
        props["chart.type"] = chartType;
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

