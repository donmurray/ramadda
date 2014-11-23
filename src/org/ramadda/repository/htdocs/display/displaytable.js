


function RamaddaXlsDisplay(displayManager, id, properties) {  

    var ID_SEARCH = "search";    
    var ID_SEARCH_PREFIX = "table";    
    var ID_SEARCH_EXTRA = "searchextra";    
    var ID_SEARCH_HEADER = "searchheader";    
    var ID_RESULTS = "results";    
    var ID_DOWNLOADURL = "downloadurl";    
    var ID_CONTENTS = "tablecontents";    
    var ID_SEARCH_DIV = "searchdiv";
    var ID_SEARCH_FORM = "searchform";
    var ID_SEARCH_TEXT = "searchtext";
    var ID_TABLE_HOLDER = "tableholder";
    var ID_TABLE = "table";
    var ID_CHARTTOOLBAR = "charttoolbar";
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
                var body = 
                    HtmlUtil.div(["id", this.getDomId(ID_SEARCH_HEADER)]) +
                    HtmlUtil.div(["id", this.getDomId(ID_TABLE_HOLDER)]) +
                    HtmlUtil.div(["id", this.getDomId(ID_CHARTTOOLBAR)]) +
                    HtmlUtil.div(["id", this.getDomId(ID_CHART)]);
                this.setContents(body);
                this.loadTableData(this.url);
            },
         });


    RamaddaUtil.defineMembers(this, {
            currentSheet:  0,
            currentData: null,
            columnLabels: null,
            startRow:0,
            groupIndex: -1,
            xAxisIndex: -1,
            yAxisIndex: 0,
            header: null,
            cellSelected: function(row, col) {
                this.startRow = row;
                if(this.jq("params-xaxis-select").attr("checked")) {
                    this.xAxisIndex = col;
                } if(this.jq("params-group-select").attr("checked")) {
                    this.groupIndex = col;
                } else {
                    this.yAxisIndex = col;
                }
                var label = "";
                var p1 = "";
                var p2 = "";

                this.setAxisLabel("params-xaxis-label", this.getHeading(this.xAxisIndex, true));
                this.setAxisLabel("params-group-label", this.getHeading(this.groupIndex, true));
                this.setAxisLabel("params-yaxis-label", this.getHeading(this.yAxisIndex, true));
            },
           setAxisLabel: function(fieldId, lbl) {
                var id= HtmlUtil.getUniqueId();
                if(lbl.length>25) {
                    lbl = lbl.substring(0,25) +"...";
                }
                if(lbl.trim()!="") {
                    lbl= HtmlUtil.span(["id",id,"class","ramadda-tag-box"], "&nbsp;&nbsp;" + lbl + "&nbsp;&nbsp;");
                }
                this.jq(fieldId).html(lbl);
            },
            loadSheet: function(sheetIdx) {
                
                var all = $("[id^=" + this.getDomId("sheet_")+"]");
                var sel = $("#" + this.getDomId("sheet_")+sheetIdx);

                all.css('font-weight','normal');
                sel.css('font-weight','bold');

                all.css('border','1px #ccc solid');
                sel.css('border','1px #666 solid');

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
                    colHeaders: true,
                    rowHeaders: true,
                    minSpareRows: 1,
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
                if(this.tableProps.useFirstRowAsHeader) {
                    var headers = rows[0];
                    args.colHeaders = headers;
                    rows = rows.splice(1);
                }
                for(var i=0;i<this.tableProps.skipRows;i++) {
                    rows = rows.splice(1);
                }

                if(rows.length==0) {
                    this.displayMessage("No data found");
                    this.jq(ID_RESULTS).html("");
                    return;
                }

                this.jq(ID_RESULTS).html("Found: " + rows.length);
                args.data = rows;
                this.currentData = rows;

                if(this.tableProps.headers!=null) {
                    args.colHeaders = this.tableProps.headers;
                }

                if(this.getProperty("showTable",true)) {
                    this.jq(ID_TABLE).handsontable(args);
                }

            },
            getDataForSheet: function(sheetIdx, args) {
                var sheet = this.sheets[sheetIdx];
                var rows =sheet.rows.slice(0);
                if(rows.length>0) {
                    this.header = rows[0];
                }

                if(this.tableProps.useFirstRowAsHeader) {
                    var headers = rows[0];
                    if(args) {
                        args.colHeaders = headers;
                    }
                    rows = rows.splice(1);
                }
                for(var i=0;i<this.tableProps.skipRows;i++) {
                    rows = rows.splice(1);
                }
                return rows;
            },

            makeChart: function(chartType, props) {
                if(typeof google == 'undefined') {
                    this.jq(ID_CHART).html("No google chart available");
                    return;
                }

                if(props==null) props = {};
                var xAxisIndex = Utils.getDefined(props.xAxisIndex, this.xAxisIndex);
                var groupIndex = Utils.getDefined(props.groupIndex, this.groupIndex);
                var yAxisIndex = Utils.getDefined(props.yAxisIndex, this.yAxisIndex);

                //                console.log("y:" + yAxisIndex +" props:" + props.yAxisIndex);

                if(yAxisIndex<0) {
                    alert("You must select a y-axis field.\n\nSelect the desired axis with the radio button.\n\nClick the column in the table to chart.");
                    return;
                }

                var sheetIdx  = this.currentSheet;
                if(!(typeof props.sheet == "undefined")) {
                    sheetIdx = props.sheet;
                }

                var rows = this.getDataForSheet(sheetIdx);
                if(rows ==null) {
                    this.jq(ID_CHART).html("There is no data");
                    return;
                }


                //remove the first header row
                var rows =rows.slice(1);
                
                for(var i=0;i<this.startRow-1;i++) {
                    rows =rows.slice(1);
                }

                var subset = [];
                console.log("x:" + xAxisIndex +" " + " y:" + yAxisIndex +" group:" + groupIndex);
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
                var lbl3 = this.getHeading(groupIndex,true);
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
                this.groupIndex = -1;
                this.xAxisIndex = -1;
                this.yAxisIndex = -1;
                this.setAxisLabel("params-group-label", "");
                this.setAxisLabel("params-xaxis-label", "");
                this.setAxisLabel("params-yaxis-label", "");


            },
             getHeading: function(index, doField) {
                if(index<0) return "";
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
                var _this = this;

                this.data = data;
                this.sheets = this.data.sheets;
                this.columns = data.columns;



                var buttons = "";
                for(var sheetIdx =0;sheetIdx<this.sheets.length;sheetIdx++) {
                    var id = this.getDomId("sheet_"+ sheetIdx);
                    buttons+=HtmlUtil.div(["id", id,"class","ramadda-xls-button-sheet"],
                                          this.sheets[sheetIdx].name);

                    buttons += "\n";
                }

                var weight = "12";

                var tableHtml =  "<table width=100% style=\"max-width:1000px;\" > ";
                if(this.sheets.length>1) {
                    weight = "10";
                }

                tableHtml +=  "<tr valign=top>";

                if(this.sheets.length>1) {
                    //                    tableHtml += HtmlUtil.openTag(["class","col-md-2"]);
                    tableHtml += HtmlUtil.td(["width","110"],HtmlUtil.div(["class","ramadda-xls-buttons"], buttons));
                    weight = "10";
                }


                var makeChartId =  HtmlUtil.getUniqueId();

                var tableWidth = this.getProperty("tableWidth", "");
                var tableHeight = this.getProperty("tableHeight", "300px");

                var style = "";
                if(tableWidth!="") {
                    style += " width:" + tableWidth + ";";
                }
                style += " height: "+ tableHeight +";";
                style += " overflow: auto;";
                tableHtml += HtmlUtil.td([],HtmlUtil.div(["id",this.getDomId(ID_TABLE),"class","ramadda-xls-table","style",style]));


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
                    HtmlUtil.div(["id", this.getDomId("params-yaxis-label"), "style","border-bottom:1px #ccc dotted;min-width:10em;display:inline-block;"], "");

                chartToolbar += "&nbsp;&nbsp;&nbsp;";
                chartToolbar += "<input type=radio  name=\"param\" id=\"" + this.getDomId("params-xaxis-select")+"\"> x-axis:&nbsp;" +
                    HtmlUtil.div(["id", this.getDomId("params-xaxis-label"), "style","border-bottom:1px #ccc dotted;min-width:10em;display:inline-block;"], "");

                chartToolbar += "&nbsp;&nbsp;&nbsp;";
                chartToolbar += "<input type=radio  name=\"param\" id=\"" + this.getDomId("params-group-select")+"\"> group:&nbsp;" +
                    HtmlUtil.div(["id", this.getDomId("params-group-label"), "style","border-bottom:1px #ccc dotted;min-width:10em;display:inline-block;"], "");
 
               chartToolbar+= "</form>";

                if(this.getProperty("showSearch",true)) {
                    var results = HtmlUtil.div(["style","display:inline-block;","id",this.getDomId(ID_RESULTS)],"");
                    var download = HtmlUtil.div(["style","display:inline-block;","id",this.getDomId(ID_DOWNLOADURL)]);
                    var searchDiv =  HtmlUtil.div(["id", this.getDomId(ID_SEARCH_DIV),"class","ramadda-xls-search-form"]);


                    var search = "";
                    search += HtmlUtil.openTag("form",["action","#","id",this.getDomId(ID_SEARCH_FORM)]);
                    search += HtmlUtil.image(icon_tree_closed,["id",this.getDomId(ID_SEARCH+"_open")]);
                    search += "\n";
                    search += HtmlUtil.input(ID_SEARCH_TEXT, this.jq(ID_SEARCH_TEXT).val(),["size","60", "id", this.getDomId(ID_SEARCH_TEXT),"placeholder","Search"]);
                    search += "<input type=submit name='' style='display:none;'>";

                    search += HtmlUtil.openTag("div", ["id", this.getDomId(ID_SEARCH_EXTRA),"class","ramadda-xls-search-extra"],"");                    
                    if(this.columns) {
                        var extra = HtmlUtil.openTag("table", ["class","formtable"]);
                        for(var i =0;i<this.columns.length;i++) {
                            var col = this.columns[i];
                            var id = ID_SEARCH_PREFIX+"_" + col.name;
                            var widget = HtmlUtil.input(id, this.jq(id).val(), ["id", this.getDomId(id),"placeholder","Search"]);
                            extra += HtmlUtil.formEntry(col.name.replace("_"," ")+":",widget);
                        }
                        extra += HtmlUtil.closeTag("table");
                        search += extra;
                    }

                    search += "\n";
                    search+= HtmlUtil.closeTag("div");
                    search += "\n";
                    search+= HtmlUtil.closeTag("form");

                    this.jq(ID_SEARCH_HEADER).html(HtmlUtil.leftRight(searchDiv,results+" " + download));

                    this.jq(ID_SEARCH_DIV).html(search);

                    if(!this.extraOpen) {
                        this.jq(ID_SEARCH_EXTRA).hide();
                    } 


                    this.jq(ID_SEARCH+"_open").button().click(function(event){
                            _this.jq(ID_SEARCH_EXTRA).toggle();
                            _this.extraOpen = !_this.extraOpen;
                            if(_this.extraOpen) {
                                _this.jq(ID_SEARCH+"_open").attr("src",icon_tree_open);
                            } else {
                                _this.jq(ID_SEARCH+"_open").attr("src",icon_tree_closed);
                            }
                        });
                
                }


               if(this.getProperty("showTable",true)) {
                   this.jq(ID_TABLE_HOLDER).html(tableHtml);
                   chartToolbar += "<br>";
                   if(this.getProperty("showChart",true)) {
                       this.jq(ID_CHARTTOOLBAR).html(chartToolbar);
                   }
               }

                if(this.getProperty("showSearch",true)) {
                    this.jq(ID_SEARCH_FORM).submit(function( event ) {
                            event.preventDefault();
                            _this.loadTableData(_this.url,"Searching...");
                        });
                    this.jq(ID_SEARCH_TEXT).focus();
                    this.jq(ID_SEARCH_TEXT).select();
                }


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
                this.setAxisLabel("params-yaxis-label", this.getHeading(this.yAxisIndex, true));

                this.displayDownloadUrl();

            },
             displayMessage: function(msg, icon) {
                if(!icon) {
                    icon = icon_information;
                }
                var html  = HtmlUtil.hbox(HtmlUtil.image(icon,["align","left"]),
                                          HtmlUtil.inset(msg,10,10,5,10));
                html = HtmlUtil.div(["class","note"],html);
                this.jq(ID_TABLE_HOLDER).html(html);
           },
           displayDownloadUrl: function() {
                var url = this.lastUrl;
                if(url == null) {
                    this.jq(ID_DOWNLOADURL).html("");
                    return
                }
                url = url.replace("xls_json","media_tabular_extractsheet");
                url += "&execute=true";
                var img = HtmlUtil.image(ramaddaBaseUrl +"/icons/xls.png",["title","Download XLSX"]);
                this.jq(ID_DOWNLOADURL).html(HtmlUtil.href(url,img));
            },
            loadTableData:  function(url, message) {
                this.url = url;
                if(!message) message = this.getLoadingMessage();
                this.displayMessage(message, icon_progress);
                var _this = this;

                var text = this.jq(ID_SEARCH_TEXT).val();
                if(text && text!="") {
                    url = url + "&table.text=" + encodeURIComponent(text);
                }
                if(this.columns) {
                    for(var i =0;i<this.columns.length;i++) {
                        var col = this.columns[i];
                        var id = ID_SEARCH_PREFIX+"_" + col.name;
                        var text = this.jq(id).val();
                        if(text) {
                            url = url + "&table." + col.name +"=" + encodeURIComponent(text);
                        }
                    }
                }



                console.log("url:" + url);
                this.lastUrl = url;

                var jqxhr = $.getJSON(url, function(data) {
                        if(GuiUtils.isJsonError(data)) {
                            this.displayMessage("Error: " + data.error);
                            return;
                        }
                        _this.showTableData(data);
                    })
                    .fail(function(jqxhr, textStatus, error) {
                            var err = textStatus + ", " + error;
                            this.displayMessage("An error occurred: " + error);
                            console.log("JSON error:" +err);
                        });
            }
        });

     }
