



function RamaddaXls(divId, url, props) {
    $("#" + divId).html("Loading...");
    this.url = url;
    this.divId = divId;
    this.baseId = HtmlUtil.getUniqueId();
    this.ssId = HtmlUtil.getUniqueId();
    this.ssLabelId = HtmlUtil.getUniqueId();
    this.chartId = HtmlUtil.getUniqueId();

    if(props == null) {
        props = {};
    }
    this.props = {
        fixedRowsTop: 0,
        fixedColumnsLeft: 0,
        rowHeaders: true,
        colHeaders: true,
    };
    $.extend(this.props, props);

    RamaddaUtil.defineMembers(this, {
            currentSheet:  0,
            flip: true,
            col1: -1,
            col2: -1,
            header: null,
            columnSelected: function(col) {
                if(this.flip) {
                    this.col1 = col;
                } else {
                    this.col2 = col;
                }
                this.flip = !this.flip;
                var label = "";
                var p1 = "";
                var p2 = "";

                var lbl1 = this.col1;
                var lbl2 = this.col2;
                if(this.flip) p1  = "<b>&gt;</b>";
                else p2  = "<b>&gt;</b>";
                if(this.header != null) {
                    if(this.col1>=0 && this.col1<this.header.length) {
                        lbl1 = this.header[this.col1];
                    }
                    if(this.col2>=0 && this.col2<this.header.length) {
                        lbl2 = this.header[this.col2];
                    }
                }
                label = "Field 1: " +  p1 + this.header[this.col1] +" Field 2: " + p2 + this.header[this.col2];
                $("#" + this.getId("params")).html("Selected: " + label);
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
                    data: rows,
                    contextMenu: true,
                    stretchH: 'last',
                    useFirstRowAsHeader: false,
                    colHeaders: true,
                    rowHeaders: true,
                    minSpareRows: 1,
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
                args.contextMenu= true;
                if(args.useFirstRowAsHeader) {
                    var headers = rows[0];
                    args.colHeaders = headers;
                    rows = rows.splice(1);
                    args.data = rows;
                }

                $("#" + this.ssLabelId).html(sheet.name);
                $("#" + this.ssId).handsontable(args);

            },
            makeChart: function(chartType) {
                if(typeof google == 'undefined') {
                    $("#" + this.chartId).html("No google");
                    return;
                }
                var sheet = this.sheets[this.currentSheet];

                //remove the first header row
                var rows =sheet.rows.slice(0);

                if(this.col1>=0 || this.col2>=0) {
                    var subset = [];
                    for(var rowIdx=0;rowIdx<rows.length;rowIdx++) {
                        var row = [];
                        var idx = 0;
                        if(this.col1>0) row.push(rows[rowIdx][this.col1]);
                        if(this.col2>0) row.push(rows[rowIdx][this.col2]);
                        subset.push(row);
                    }
                    console.log("setting rows to subset");
                    rows = subset;
                }



                for(var rowIdx=0;rowIdx<rows.length;rowIdx++) {
                    var cols = rows[rowIdx];
                    var s = "";
                    for(var colIdx=0;colIdx<cols.length;colIdx++) {
                        if(rowIdx>0) {
                            cols[colIdx] = Number(cols[colIdx]);
                        }
                        s += " " + cols[colIdx];
                    }
                    console.log(s);
                }
                var dataTable = google.visualization.arrayToDataTable(rows);
                var   chartOptions = {};
                var width = "95%";
                $.extend(chartOptions, {
                          series: [{targetAxisIndex:0},{targetAxisIndex:1},],
                          legend: { position: 'top' },
                 });

                if(this.header!=null) {
                    if(this.col1>=0) {
                        chartOptions.hAxis =  {title: this.header[this.col1]};
                    }
                    if(this.col2>=0) {
                        chartOptions.vAxis =  {title: this.header[this.col2]};
                    }
                }

                if(chartType == "barchart") {
                    chartOptions.chartArea = {left:75,top:10,height:"60%",width:width};
                    chartOptions.orientation =  "horizontal";
                    this.chart = new google.visualization.BarChart(document.getElementById(this.chartId));
                } else  if(chartType == "table") {
                    this.chart = new google.visualization.Table(document.getElementById(this.chartId));
                } else  if(chartType == "scatterplot") {
                    chartOptions.chartArea = {left:20,top:20,height:350,width:350};
                    chartOptions.legend = 'none';
                    chartOptions.axisTitlesPosition = "in";
                    var newDivId= HtmlUtil.getUniqueId();
                    $("#" + this.chartId).html(HtmlUtil.div(["id", newDivId,"style","width: 400px; height: 400px;"],""));
                    this.chart = new google.visualization.ScatterChart(document.getElementById(newDivId));

                } else {
                    $.extend(chartOptions, {lineWidth: 1,vAxis: {}});
                    chartOptions.chartArea = {left:75,top:10,height:"60%",width:width};
                    this.chart = new google.visualization.LineChart(document.getElementById(this.chartId));
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
                }

                html += "<br>";
                html += HtmlUtil.div(["id", this.getId("params")], "&nbsp;");
                html += HtmlUtil.div(["id",this.chartId,"class","ramadda-xls-chart"]);

                html += HtmlUtil.closeDiv();
                html += HtmlUtil.closeDiv();

                $("#" + divId).append(html);

                for(var i=0;i<chartTypes.length;i++) {
                    this.addNewChartListener(makeChartId, chartTypes[i]);
                }

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
