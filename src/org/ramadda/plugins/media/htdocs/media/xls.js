


function RamaddaXls(divId, url, props) {
    $("#" + divId).html("Loading...");
    this.url = url;
    this.divId = divId;
    this.baseId = HtmlUtil.getUniqueId();
    this.ssId = HtmlUtil.getUniqueId();
    this.ssLabelId = HtmlUtil.getUniqueId();

    if(props == null) {
        props = {};
    }
    this.props = {
        fixedRowsTop: 0,
        fixedColumnsLeft: 0,
        rowHeaders: true,
        colHeaders: true,
        colWidths:   null,
    };
    $.extend(this.props, props);

    RamaddaUtil.defineMembers(this, {
            loadSheet: function(sheetIdx) {
                var sheet = this.sheets[sheetIdx];
                var rows =sheet.rows.slice(0);
                var html = "";

                var args  = {
                    data: rows,
                    contextMenu: false,
                    stretchH: 'last',
                    useFirstRowAsHeader: false,
                    colHeaders: true,
                    rowHeaders: false,
                    minSpareRows: 1,
                };
                $.extend(args, this.props);


                if(args.useFirstRowAsHeader) {
                    var headers = rows[0];
                    args.colHeaders = headers;
                    rows = rows.splice(1);
                    args.data = rows;
                }

                $("#" + this.ssLabelId).html(sheet.name);
                $("#" + this.ssId).handsontable(args);

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

                html += HtmlUtil.openDiv(["class","col-md-" + weight]);
                html += HtmlUtil.div(["id",this.ssId,"class","ramadda-xls-table"]);
                html += HtmlUtil.closeDiv();
                html += HtmlUtil.closeDiv();

                $("#" + divId).html(html);


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
