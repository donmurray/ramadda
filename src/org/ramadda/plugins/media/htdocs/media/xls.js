



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
            loadSheet: function(sheetIdx) {
                this.currentSheet = sheetIdx;
                var sheet = this.sheets[sheetIdx];
                var rows =sheet.rows.slice(0);
                var html = "";
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
                            console.log('column:' + col);
                        }
                    },
                };
                $.extend(args, this.props);



                /*
                var hooks = Handsontable.hooks.getRegistered();
                hooks.forEach(function(hook) {
                        args[hook] = function() {
                            console.log(hook + " " +  arguments);
                        }
                    });
                */
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
                $.extend(chartOptions, {
                        lineWidth: 1,
                        vAxis: {}});
                var width = "95%";
                $.extend(chartOptions, {
                    series: [{targetAxisIndex:0},{targetAxisIndex:1},],
                    legend: { position: 'bottom' },
                    chartArea:{left:75,top:10,height:"60%",width:width}
                 });
                if(chartType == "barchart") {
                    chartOptions.orientation =  "horizontal";
                    this.chart = new google.visualization.BarChart(document.getElementById(this.chartId));
                } else  if(chartType == "table") {
                    this.chart = new google.visualization.Table(document.getElementById(this.chartId));
                } else {
                    this.chart = new google.visualization.LineChart(document.getElementById(this.chartId));
                }
                if(this.chart!=null) {
                    this.chart.draw(dataTable, chartOptions);
                }
            },

            makeSheetButton: function(id, index) {
                var _this = this;
                $("#" + id).button().click(function(event){
                        _this.loadSheet(index);
                    });
            },
            loadData: function(data) {
                this.sheets = data;
                var buttons = "";
                var html = "";
                for(var sheetIdx =0;sheetIdx<this.sheets.length;sheetIdx++) {
                    var id = this.baseId+"_"+ sheetIdx;
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
                html+=HtmlUtil.div(["id", makeChartId,"class","ramadda-xls-button"],  "Make Chart");
                html += HtmlUtil.div(["id",this.chartId,"class","ramadda-xls-chart"]);

                html += HtmlUtil.closeDiv();
                html += HtmlUtil.closeDiv();

                $("#" + divId).html(html);

                $("#" + makeChartId).button().click(function(event){
                        console.log("make chart");
                        _this.makeChart("");
                    });

              for(var sheetIdx =0;sheetIdx<this.sheets.length;sheetIdx++) {
                    var id = this.baseId+"_"+ sheetIdx;
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
