


function RamaddaXls(divId, url, props) {
    $("#" + divId).html("Loading...");
    this.url = url;
    this.divId = divId;
    this.baseId = HtmlUtil.getUniqueId();
    this.ssId = HtmlUtil.getUniqueId();
    this.ssLabelId = HtmlUtil.getUniqueId();
    this.chartContainerId = HtmlUtil.getUniqueId();

    if(props == null) {
        props = {};
    }
    this.props = {
        fixedRowsTop: 0,
        fixedColumnsLeft: 0,
        rowHeaders: true,
        colHeaders: true,
        headers: null,
        skipRows: 0,
        skipColumns: 0,
    };
    $.extend(this.props, props);

    RamaddaUtil.defineMembers(this, {
            currentSheet:  0,
            currentData: null,
            columnLabels: null,
            xAxisIndex: -1,
            yAxisIndex: -1,
            header: null,
            columnSelected: function(col) {
                if($("#"+this.getId("params-xaxis-select")).attr("checked")) {
                    this.xAxisIndex = col;
                } else {
                    this.yAxisIndex = col;
                }
                var label = "";
                var p1 = "";
                var p2 = "";

                var lbl1 = this.getHeading(this.xAxisIndex);
                var lbl2 = this.getHeading(this.yAxisIndex);
                this.columnLabels = [lbl1,lbl2];
                $("#" + this.getId("params-xaxis-label")).html(lbl1);
                $("#" + this.getId("params-yaxis-label")).html(lbl2);
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
                    stretchH: 'last',

                    useFirstRowAsHeader: false,
                    colHeaders: true,
                    rowHeaders: true,
                    minSpareRows: 1,
                    contextMenu: true,
                    afterSelection: function() {
                        if(arguments.length>2) {
                            var col = arguments[1];
                            if(_this.lastColumnSelected == col) {
                                return;
                            }
                            _this.lastColumnSelected = col;
                            _this.columnSelected(col);
                        }
                    },
                };
                $.extend(args, this.props);

                if(args.useFirstRowAsHeader) {
                    var headers = rows[0];
                    args.colHeaders = headers;
                    rows = rows.splice(1);
                }
                for(var i=0;i<this.props.skipRows;i++) {
                    rows = rows.splice(1);
                }

                args.data = rows;
                this.currentData = rows;

                if(this.props.headers!=null) {
                    args.colHeaders = this.props.headers;
                }

                $("#" + this.ssLabelId).html(sheet.name);
                $("#" + this.ssId).handsontable(args);

            },
            makeChart: function(chartType) {
                if(typeof google == 'undefined') {
                    $("#" + this.chartContainerId).html("No google");
                    return;
                }
                if(this.currentData ==null) {
                    $("#" + this.chartContainerId).html("No data");
                    return;
                }


                //remove the first header row
                var rows =this.currentData.slice(0);


                if(this.xAxisIndex>=0 || this.yAxisIndex>=0) {
                    var subset = [];
                    for(var rowIdx=0;rowIdx<rows.length;rowIdx++) {
                        var row = [];
                        var idx = 0;
                        if(this.xAxisIndex>0) {
                            row.push(rows[rowIdx][this.xAxisIndex]);
                        }   else {
                            row.push(10+rowIdx);
                        }
                        if(this.yAxisIndex>0) {
                            row.push(rows[rowIdx][this.yAxisIndex]);
                        }
                        subset.push(row);
                        if(rowIdx>4) break;
                    }
                    console.log("setting rows to subset");
                    rows = subset;
                }



                for(var rowIdx=0;rowIdx<rows.length;rowIdx++) {
                    var cols = rows[rowIdx];
                    for(var colIdx=0;colIdx<cols.length;colIdx++) {
                        if(rowIdx>0) {
                            cols[colIdx] = Number(cols[colIdx]);
                        }
                    }
                }

                var labels = this.columnLabels!=null?this.columnLabels:["Field 1","Field 2"];
                labels = ["Field 1","Field 2"];
                
                console.log("labels:" + labels);
                rows.splice(0,0,labels);

                for(var rowIdx=0;rowIdx<rows.length;rowIdx++) {
                    var cols = rows[rowIdx];
                    var s = "";
                    for(var colIdx=0;colIdx<cols.length;colIdx++) {
                        if(colIdx>0)
                            s += ", ";
                        s += cols[colIdx];
                    }
                    console.log(s);
                    if(rowIdx>5) break;
                }



                var dataTable = google.visualization.arrayToDataTable(rows);
                var   chartOptions = {};
                var width = "95%";
                $.extend(chartOptions, {
                        //                          series: [{targetAxisIndex:0},{targetAxisIndex:1},],
                          legend: { position: 'top' },
                 });

                if(this.header!=null) {
                    if(this.xAxisIndex>=0) {
                        chartOptions.hAxis =  {title: this.header[this.xAxisIndex]};
                    }
                    if(this.yAxisIndex>=0) {
                        chartOptions.vAxis =  {title: this.header[this.yAxisIndex]};
                    }
                }

                var chartDivId = HtmlUtil.getUniqueId();
                var chartDiv = HtmlUtil.div(["id", chartDivId],"");

                $("#"+this.chartContainerId).append(chartDiv);


                if(chartType == "barchart") {
                    chartOptions.chartArea = {left:75,top:10,height:"60%",width:width};
                    chartOptions.orientation =  "horizontal";
                    this.chart = new google.visualization.BarChart(document.getElementById(chartDivId));
                } else  if(chartType == "table") {
                    this.chart = new google.visualization.Table(document.getElementById(chartDivId));
                } else  if(chartType == "scatterplot") {
                    chartOptions.chartArea = {left:50,top:30,height:400,width:400};
                    chartOptions.legend = 'none';
                    chartOptions.axisTitlesPosition = "in";
                    var newDivId= HtmlUtil.getUniqueId();
                    $("#" + chartDivId).html(HtmlUtil.div(["id", newDivId,"style","width: 450px; height: 450px;"],""));
                    this.chart = new google.visualization.ScatterChart(document.getElementById(newDivId));

                } else {
                    $.extend(chartOptions, {lineWidth: 1});
                    //                    $.extend(chartOptions, {lineWidth: 1,vAxis: {}});
                    chartOptions.chartArea = {left:75,top:10,height:"60%",width:width};
                    this.chart = new google.visualization.LineChart(document.getElementById(chartDivId));
                }









                if(this.chart!=null) {
                    this.chart.draw(dataTable, chartOptions);
                }
            },

                addNewChartListener: function(makeChartId, chartType) {
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
            getId: function(suffix) {
                return this.baseId +"_"+ suffix;
            },
            clear: function() {
                $("#" + this.chartContainerId).html("");
                this.xAxisIndex = -1;
                this.yAxisIndex = -1;
                $("#" + this.getId("params-xaxis-label")).html("");
                $("#" + this.getId("params-yaxis-label")).html("");
            },
             getHeading: function(index, doField) {
                if(this.header != null && index>=0 && index< this.header.length) {
                    return this.header[index];
                }
                if(doField)
                    return "Field " + (index+1);
                return "";
            },
            loadData: function(data) {
                this.sheets = data;
                var buttons = "";
                var html = "";
                for(var sheetIdx =0;sheetIdx<this.sheets.length;sheetIdx++) {
                    var id = this.getId(sheetIdx);
                    buttons+=HtmlUtil.div(["id", id,"class","ramadda-xls-button"],
                                          this.sheets[sheetIdx].name);

                    buttons += "<p>";
                }
                var weight = "12";
                if(this.sheets.length>1) {
                    weight = "10";
                }
                html += HtmlUtil.openDiv(["class","row"]);
                html += HtmlUtil.openDiv(["class","col-md-2"]);
                if(this.sheets.length>1) {
                    html+=HtmlUtil.div(["class","ramadda-xls-label"],"Sheets")
                    
                }

                html += HtmlUtil.closeDiv();
                html += HtmlUtil.openDiv(["class","col-md-" + weight]);
                html+=HtmlUtil.div(["id",this.ssLabelId,"class","ramadda-xls-label"])
                html += HtmlUtil.closeDiv();
                html += HtmlUtil.closeDiv();


                html += HtmlUtil.openDiv(["class","row"]);
                if(this.sheets.length>1) {
                    html += HtmlUtil.openDiv(["class","col-md-2"]);
                    html += buttons;
                    html += HtmlUtil.closeDiv();
                    weight = "10";
                }

                var _this = this;
                var makeChartId =  HtmlUtil.getUniqueId();

                html += HtmlUtil.openDiv(["class","col-md-" + weight]);
                html += HtmlUtil.div(["id",this.ssId,"class","ramadda-xls-table"]);
                html += "<p>";
                var chartTypes = ["barchart","linechart","scatterplot"];
                for(var i=0;i<chartTypes.length;i++) {
                    html+=HtmlUtil.div(["id", makeChartId+"-" + chartTypes[i],"class","ramadda-xls-button"],  "Make " + chartTypes[i]);
                    html+= "&nbsp;";
                }

                html+= "&nbsp;";
                html+=HtmlUtil.div(["id", this.getId("removechart"),"class","ramadda-xls-button"],  "Clear");


                html += "<br>";
                html += "<form>Fields: <input type=radio checked name=\"param\" id=\"" + this.getId("params-xaxis-select")+"\"> x-axis:&nbsp;" +
                    HtmlUtil.div(["id", this.getId("params-xaxis-label"), "style","border-bottom:1px #ccc dotted;min-width:20em;display:inline-block;"], "") +
                    "&nbsp;&nbsp;&nbsp;" +
                    "<input type=radio name=\"param\" id=\"" + this.getId("params-yaxis-select")+"\"> y-axis:&nbsp;" +
                    HtmlUtil.div(["id", this.getId("params-yaxis-label"), "style","border-bottom:1px #ccc dotted;min-width:20em;display:inline-block;"], "");
                html+= "</form>";

                html += HtmlUtil.div(["id",this.chartContainerId,"class","ramadda-xls-chart"]);

                html += HtmlUtil.closeDiv();
                html += HtmlUtil.closeDiv();

                $("#" + divId).append(html);

                for(var i=0;i<chartTypes.length;i++) {
                    this.addNewChartListener(makeChartId, chartTypes[i]);
                }
                $("#" + this.getId("removechart")).button().click(function(event){
                        _this.clear();
                    });

              for(var sheetIdx =0;sheetIdx<this.sheets.length;sheetIdx++) {
                  var id = this.getId(sheetIdx);
                    this.makeSheetButton(id, sheetIdx);
                }
                var sheetIdx = 0;
                var rx = /sheet=([^&]+)/g;
                var arr = rx.exec(window.location.search);
                if(arr) {
                    sheetIdx =  arr[1]; 
                }

                this.loadSheet(sheetIdx);
            }
        });

    var _this = this;
    console.log("url:" + url);
    var jqxhr = $.getJSON( url, function(data) {
            if(GuiUtils.isJsonError(data)) {
                $("#" + divId).html("Error:" + data.error);
                return;
            }
            _this.loadData(data);
        })
        .fail(function(jqxhr, textStatus, error) {
                var err = textStatus + ", " + error;
                $("#" + divId).html("An error occurred: " + error);
                console.log("JSON error:" +err);
            });

     }
