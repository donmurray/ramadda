


function RamaddaXlsDisplay(displayManager, id, properties) {  

    var ID_TABLE = "table";
    var ID_TABLE_LABEL = "tablelabel";
    var ID_CHART = "chart";

    RamaddaUtil.inherit(this, new RamaddaDisplay(displayManager, id, "xls", properties));
    addRamaddaDisplay(this);


    this.url = properties.url;
    this.tableProps = {
        fixedRowsTop: 0,
        fixedColumnsLeft: 0,
        rowHeaders: true,
        colHeaders: true,
        headers: null,
        skipRows: 0,
        skipColumns: 0,
    };
    if(properties!=null) {
        $.extend(this.tableProps, properties);
    }


    RamaddaUtil.defineMembers(this, {
            initDisplay: function() {
                this.initUI();
                this.setTitle("Table Data");
                this.divId = HtmlUtil.getUniqueId();
                var html  = HtmlUtil.div(["id", this.divId],this.getLoadingMessage())
                this.setContents(html);
                this.loadTableData(this.url);
            },
         });


    RamaddaUtil.defineMembers(this, {
            currentSheet:  0,
            currentData: null,
            columnLabels: null,
            startRow:0,
            xAxisIndex: -1,
            yAxisIndex: 0,
            header: null,
            cellSelected: function(row, col) {
                this.startRow = row;
                if(this.jq("params-xaxis-select").attr("checked")) {
                    this.xAxisIndex = col;
                } else {
                    this.yAxisIndex = col;
                }
                var label = "";
                var p1 = "";
                var p2 = "";

                this.jq("params-xaxis-label").html(this.getHeading(this.xAxisIndex));
                this.jq("params-yaxis-label").html(this.getHeading(this.yAxisIndex));
            },

            loadSheet: function(sheetIdx) {
                this.currentSheet = sheetIdx;
                var sheet = this.sheets[sheetIdx];
                var rows =sheet.rows.slice(0);
                if(rows.length>0) {
                    this.header = rows[0];
                }

                var html = "";
                var _this = this;
                var args  = {
                    contextMenu: true,
                    stretchH: 'all',
                    useFirstRowAsHeader: false,
                    colHeaders: true,
                    rowHeaders: true,
                    minSpareRows: 1,
                    contextMenu: true,
                    afterSelection: function() {
                        if(arguments.length>2) {
                            for(var i=0;i<arguments.length;i++) {
                                //                                console.log("a[" + i +"]=" + arguments[i]);
                            }
                            var row = arguments[0];
                            var col = arguments[1];
                            _this.cellSelected(row, col);
                        }
                    },
                };
                $.extend(args, this.tableProps);


                if(args.useFirstRowAsHeader) {
                    var headers = rows[0];
                    args.colHeaders = headers;
                    rows = rows.splice(1);
                }
                for(var i=0;i<this.tableProps.skipRows;i++) {
                    rows = rows.splice(1);
                }

                args.data = rows;
                this.currentData = rows;

                if(this.tableProps.headers!=null) {
                    args.colHeaders = this.tableProps.headers;
                }

                if(this.getProperty("showTable",true)) {
                    this.jq(ID_TABLE_LABEL).html(sheet.name);
                    this.jq(ID_TABLE).handsontable(args);
                }

            },
            makeChart: function(chartType, props) {
                if(typeof google == 'undefined') {
                    this.jq(ID_CHART).html("No google chart available");
                    return;
                }
                if(this.currentData ==null) {
                    this.jq(ID_CHART).html("There is no data");
                    return;
                }

                if(props==null) props = {};
                var xAxisIndex = Utils.getDefined(props.xAxisIndex, this.xAxisIndex);
                var yAxisIndex = Utils.getDefined(props.yAxisIndex, this.yAxisIndex);

                //                console.log("y:" + yAxisIndex +" props:" + props.yAxisIndex);

                if(yAxisIndex<0) {
                    alert("You must select a y-axis field.\n\nSelect the desired axis with the radio button.\n\nClick the column in the table to chart.");
                    return;
                }

                var rows =this.currentData;

                //remove the first header row
                var rows =rows.slice(1);
                
                for(var i=0;i<this.startRow-1;i++) {
                    rows =rows.slice(1);
                }

                var subset = [];
                console.log("x:" + xAxisIndex +" " + " y:" + yAxisIndex);
                for(var rowIdx=0;rowIdx<rows.length;rowIdx++) {
                    var row = [];
                    var idx = 0;
                    if(xAxisIndex>=0) {
                        row.push(rows[rowIdx][xAxisIndex]);
                    }   else {
                        row.push(rowIdx);
                    }
                    if(yAxisIndex>=0) {
                        row.push(rows[rowIdx][yAxisIndex]);
                    }
                    subset.push(row);
                    if(rowIdx<2)
                        console.log("row:" + row);
                }
                rows = subset;

                for(var rowIdx=0;rowIdx<rows.length;rowIdx++) {
                    var cols = rows[rowIdx];


                    for(var colIdx=0;colIdx<cols.length;colIdx++) {
                        var value = cols[colIdx]+"";
                        cols[colIdx] = parseFloat(value.trim());
                    }
               }


                var lbl1 = this.getHeading(xAxisIndex,true);
                var lbl2 = this.getHeading(yAxisIndex, true);
                this.columnLabels = [lbl1,lbl2];


                var labels = this.columnLabels!=null?this.columnLabels:["Field 1","Field 2"];
                rows.splice(0,0,labels);
                /*
                for(var rowIdx=0;rowIdx<rows.length;rowIdx++) {
                    var cols = rows[rowIdx];
                    var s = "";
                    for(var colIdx=0;colIdx<cols.length;colIdx++) {
                        if(colIdx>0)
                            s += ", ";
                        s += "'" +cols[colIdx]+"'" + " (" + (typeof cols[colIdx]) +")";
                    }
                    console.log(s);
                    if(rowIdx>5) break;
                }
                */

                var dataTable = google.visualization.arrayToDataTable(rows);
                var   chartOptions = {};
                var width = "95%";
                $.extend(chartOptions, {
                      legend: { position: 'top' },
                 });

                if(this.header!=null) {
                    if(xAxisIndex>=0) {
                        chartOptions.hAxis =  {title: this.header[xAxisIndex]};
                    }
                    if(yAxisIndex>=0) {
                        chartOptions.vAxis =  {title: this.header[yAxisIndex]};
                    }
                }

                var chartDivId = HtmlUtil.getUniqueId();
                var divAttrs = ["id",chartDivId];
                if(chartType == "scatterplot") {
                    divAttrs.push("style");
                    divAttrs.push("width: 450px; height: 450px;");
                }
                this.jq(ID_CHART).append(HtmlUtil.div(divAttrs));

                if(chartType == "barchart") {
                    chartOptions.chartArea = {left:75,top:10,height:"60%",width:width};
                    chartOptions.orientation =  "horizontal";
                    this.chart = new google.visualization.BarChart(document.getElementById(chartDivId));
                } else  if(chartType == "table") {
                    this.chart = new google.visualization.Table(document.getElementById(chartDivId));
                } else  if(chartType == "motion") {
                    this.chart = new google.visualization.MotionChart(document.getElementById(chartDivId));
                } else  if(chartType == "scatterplot") {
                    chartOptions.chartArea = {left:50,top:30,height:400,width:400};
                    chartOptions.legend = 'none';
                    chartOptions.axisTitlesPosition = "in";
                    this.chart = new google.visualization.ScatterChart(document.getElementById(chartDivId));
                } else {
                    $.extend(chartOptions, {lineWidth: 1});
                    chartOptions.chartArea = {left:75,top:10,height:"60%",width:width};
                    this.chart = new google.visualization.LineChart(document.getElementById(chartDivId));
                }
                if(this.chart!=null) {
                    this.chart.draw(dataTable, chartOptions);
                }
            },

            addNewChartListener: function(makeChartId, chartType) {
                var _this = this;
                $("#" + makeChartId+"-" + chartType).button().click(function(event){
                        console.log("make chart:" + chartType);
                        _this.makeChart(chartType);
                    });
            },

            makeSheetButton: function(id, index) {
                var _this = this;
                $("#" + id).button().click(function(event){
                        _this.loadSheet(index);
                    });
            },
            clear: function() {
                this.jq(ID_CHART).html("");
                this.startRow = 0;
                this.xAxisIndex = -1;
                this.yAxisIndex = -1;
                this.jq("params-xaxis-label").html("");
                this.jq("params-yaxis-label").html("");
            },
             getHeading: function(index, doField) {
                if(this.header != null && index>=0 && index< this.header.length) {
                    var v=  this.header[index];
                    v  = v.trim();
                    if(v.length>0) return v;
                }
                if(doField)
                    return "Field " + (index+1);
                return "";
            },
            showTableData: function(data) {
                this.sheets = data;
                var buttons = "";
                for(var sheetIdx =0;sheetIdx<this.sheets.length;sheetIdx++) {
                    var id = this.getDomId("sheet_"+ sheetIdx);
                    buttons+=HtmlUtil.div(["id", id,"class","ramadda-xls-button-sheet"],
                                          this.sheets[sheetIdx].name);

                    buttons += "\n";
                }

                var weight = "12";

                var tableHtml =  "<table width=100%>";
                tableHtml +=  "<tr>";
                tableHtml+="\n";
                if(this.sheets.length>1) {
                    weight = "10";
                    //empty column
                    tableHtml += HtmlUtil.td(["width","120"],"");
                    tableHtml+="\n";
                }




                tableHtml += HtmlUtil.td([], HtmlUtil.div(["id",this.getDomId(ID_TABLE_LABEL),"class","ramadda-xls-label"]));
                tableHtml += "</tr>";
                tableHtml+="\n";

                tableHtml +=  "<tr valign=top>";

                if(this.sheets.length>1) {
                    //                    tableHtml += HtmlUtil.openTag(["class","col-md-2"]);
                    tableHtml += HtmlUtil.td(["width","110"],HtmlUtil.div(["class","ramadda-xls-buttons"], buttons));
                    weight = "10";
                }

                var _this = this;
                var makeChartId =  HtmlUtil.getUniqueId();

                //                tableHtml += HtmlUtil.openDiv(["class","col-md-" + weight]);
                tableHtml += HtmlUtil.td([],HtmlUtil.div(["id",this.getDomId(ID_TABLE),"class","ramadda-xls-table","style","width:900px; height: 300px; overflow: auto"]));


                tableHtml += "</tr>";
                tableHtml += "</table>";
                
                var chartToolbar  = "";
                var chartTypes = ["barchart","linechart","scatterplot"];
                for(var i=0;i<chartTypes.length;i++) {
                    chartToolbar+=HtmlUtil.div(["id", makeChartId+"-" + chartTypes[i],"class","ramadda-xls-button"],  "Make " + chartTypes[i]);
                    chartToolbar+= "&nbsp;";
                }

                chartToolbar+= "&nbsp;";
                chartToolbar+=HtmlUtil.div(["id", this.getDomId("removechart"),"class","ramadda-xls-button"],  "Clear Charts");


                chartToolbar += "<p>";
                chartToolbar += "<form>Fields: ";
                chartToolbar +=  "<input type=radio checked name=\"param\" id=\"" + this.getDomId("params-yaxis-select")+"\"> y-axis:&nbsp;" +
                    HtmlUtil.div(["id", this.getDomId("params-yaxis-label"), "style","border-bottom:1px #ccc dotted;min-width:20em;display:inline-block;"], "");

                chartToolbar += "&nbsp;&nbsp;&nbsp;";
                chartToolbar += "<input type=radio  name=\"param\" id=\"" + this.getDomId("params-xaxis-select")+"\"> x-axis:&nbsp;" +
                    HtmlUtil.div(["id", this.getDomId("params-xaxis-label"), "style","border-bottom:1px #ccc dotted;min-width:20em;display:inline-block;"], "");
 
               chartToolbar+= "</form>";



               var html = "";


               if(this.getProperty("showTable",true)) {
                   html+= tableHtml;
                   chartToolbar += "<br>";
                   if(this.getProperty("showChart",true)) {
                       html+= HtmlUtil.div(["class", "ramadda-xls-chart-toolbar"], chartToolbar);
                   }
               }
               html += HtmlUtil.div(["id",this.getDomId(ID_CHART),"class","ramadda-xls-chart"]);

                $("#" + this.divId).html(html);

                for(var i=0;i<chartTypes.length;i++) {
                    this.addNewChartListener(makeChartId, chartTypes[i]);
                }
                this.jq("removechart").button().click(function(event){
                        _this.clear();
                    });

              for(var sheetIdx =0;sheetIdx<this.sheets.length;sheetIdx++) {
                  var id = this.getDomId("sheet_"+ sheetIdx);
                  this.makeSheetButton(id, sheetIdx);
                }
                var sheetIdx = 0;
                var rx = /sheet=([^&]+)/g;
                var arr = rx.exec(window.location.search);
                if(arr) {
                    sheetIdx =  arr[1]; 
                }
                this.loadSheet(sheetIdx);


                if(this.defaultCharts) {
                    for(var i=0;i<this.defaultCharts.length;i++) {
                        var dflt  =this.defaultCharts[i];
                        this.makeChart(dflt.type,dflt);
                    }
                }


                this.jq("params-yaxis-label").html(this.getHeading(this.yAxisIndex));


            },




           loadTableData:  function(url) {
                var _this = this;
                console.log("url:" + this.url);
                var jqxhr = $.getJSON( this.url, function(data) {
                        if(GuiUtils.isJsonError(data)) {
                            $("#" + _this.divId).html("Error: " + data.error);
                            return;
                        }
                        _this.showTableData(data);
                    })
                    .fail(function(jqxhr, textStatus, error) {
                            var err = textStatus + ", " + error;
                            $("#" + this.divId).html("An error occurred: " + error);
                            console.log("JSON error:" +err);
                        });
            }
        });

     }
