/**
Copyright 2008-2014 Geode Systems LLC

This package supports charting and mapping of georeferenced time series data
It requires displaymanager.js pointdata.js
*/

//Ids of DOM components
var ID_FIELDS = "fields";
var ID_HEADER = "header";
var ID_TITLE = "title";
var ID_DISPLAY_CONTENTS = "contents";
var ID_DIALOG = "dialog";
var ID_DIALOG_BUTTON = "dialog_button";

var PROP_DISPLAY_FILTER = "displayFilter";

var PROP_CHART_MIN = "chartMin";
var PROP_CHART_MAX = "chartMax";
var PROP_CHART_TYPE = "chartType";
var PROP_DIVID = "divid";
var PROP_FIELDS = "fields";
var PROP_LAYOUT_FIXED = "layoutFixed";
var PROP_HEIGHT  = "height";
var PROP_WIDTH  = "width";

var DFLT_WIDTH = "600px";
var DFLT_HEIGHT = "200px";


var DISPLAY_LINECHART = "linechart";
var DISPLAY_BARCHART = "barchart";
var DISPLAY_TABLE = "table";
var DISPLAY_MAP = "map";
var DISPLAY_TEXT = "text";
var DISPLAY_ANIMATION = "animation";
var DISPLAY_OPERANDS = "operands";


function addRamaddaDisplay(display) {
    if(window.globalDisplays == null) {
        window.globalDisplays = {};
    }
    window.globalDisplays[display.id] = display;
}


function getRamaddaDisplay(id) {
    if(window.globalDisplays == null) {
        return null;
    }
    return window.globalDisplays[id];
}


function removeRamaddaDisplay(id) {
    var display =getRamaddaDisplay(id);
    if(display) {
        display.removeDisplay();
    }
}


function DisplayThing(id, properties) {
    if(properties == null) {
       properties = {};
    }
    $.extend(this, properties);
    $.extend(this, {
            id: id,
            properties:properties,
            displayParent: null,
            getId: function() {
            return this.id;
        },
       getDomId:function(suffix) {
                return this.getId() +"_" + suffix;
       },
       getFormValue: function(what, dflt) {
           var fromForm = $("#" + this.getDomId(what)).val();
           if(fromForm!=null) {
               if(fromForm.length>0) {
                   this.setProperty(what,fromForm);
               }
               if(fromForm == "none") {
                   this.setProperty(what,null);
               }
               return fromForm;
           }
             return this.getProperty(what,dflt);
        },

       getName: function() {
         return this.getFormValue("name",this.getId());
       },
       getEventSource: function() {
            return this.getFormValue("eventSource","");
       },
       setDisplayParent:  function (parent) {
             this.displayParent = parent;
       },
       getDisplayParent:  function () {
                return this.displayParent;
       },
       removeProperty: function(key) {
                this.properties[key] = null;
       },
       setProperty: function(key, value) {
           this.properties[key] = value;
        },
       getProperty: function(key, dflt) {
            var value = this.properties[key];
            if(value != null) return value;
            if(this.displayParent!=null) {
                return this.displayParent.getProperty(key, dflt);
             }
             return dflt;
         }

        });
}





function RamaddaDisplay(displayManager, id, type, propertiesArg) {
    $.extend(this, new DisplayThing(id, propertiesArg));
    $.extend(this, {
            type: type,
            displayManager:displayManager,
            filters: [],
            dataCollection: new DataCollection(),
            selectedCbx: [],
            getType: function() {
                return this.type;
            },
            setDisplayManager: function(cm) {
                this.displayManager = cm;
                this.setDisplayParent(cm);
            },
            setContents: function(contents) {
                contents = htmlUtil.div(["class","display-" + this.getType() +"-inner"], contents);
                $("#" + this.getDomId(ID_DISPLAY_CONTENTS)).html(htmlUtil.div(["class","display-" +this.type], contents));
            },
           checkFixedLayout: function() {
                if(this.getIsLayoutFixed()) {
                    var divid = this.getProperty(PROP_DIVID);
                    if(divid!=null) {
                        var html = this.getHtml();
                        $("#" + divid).html(html);
                    }
                }
            },
            addFieldsCheckboxes: function() {
                if(!this.hasData()) {
                    $("#" + this.getDomId(ID_FIELDS)).html("No data");
                    return;
                }
                if(this.getProperty(PROP_FIELDS,null)!=null) {
                    //            return;
                }
                var html =  null;
                var checkboxClass = this.id +"_checkbox";
                var dataList =  this.dataCollection.getList();
                for(var collectionIdx=0;collectionIdx<dataList.length;collectionIdx++) {             
                    var pointData = dataList[collectionIdx];
                    var fields =pointData.getChartableFields();
                    fields = RecordFieldSort(fields);
                    if(html == null) {
                        html = htmlUtil.tag("b", [],  "Fields");
                        html += htmlUtil.openTag("div", ["class", "display-fields"]);
                    } else {
                        html+= "<br>";
                    }



                    for(i=0;i<fields.length;i++) { 
                        var field = fields[i];
                        var idBase = "cbx_" + collectionIdx +"_" +i;
                        field.checkboxId  = this.getDomId(idBase);
                        var on = false;
                        if(this.selectedCbx.indexOf(idBase)>=0) {
                            on = true;
                        }  else if(this.selectedCbx.length==0) {
                            on = (i==0);
                        }
                        html += htmlUtil.tag("div", ["title", field.getId()],
                                             htmlUtil.checkbox(field.checkboxId, checkboxClass,
                                                               on) +" " +field.getLabel()
                                             );
                    }
                }
                if(html == null) {
                    html = "";
                } else {
                    html+= htmlUtil.closeTag("div");
                }

                $("#" + this.getDomId(ID_FIELDS)).html(html);

                var theChart = this;
                //Listen for changes to the checkboxes
                $("." + checkboxClass).click(function(event) {
                        theChart.fieldSelectionChanged();
                    });
            },
            fieldSelectionChanged: function() {
            },
            getSelectedFields:function() {

                var df = [];
                var dataList =  this.dataCollection.getList();
                //If we have fixed fields then clear them after the first time
                var fixedFields = this.getProperty(PROP_FIELDS);
                if(fixedFields!=null) {
                    this.removeProperty(PROP_FIELDS);
                    if(fixedFields.length==0) {
                        fixedFields = null;
                    } 
                }
                for(var collectionIdx=0;collectionIdx<dataList.length;collectionIdx++) {             
                    var pointData = dataList[collectionIdx];
                    var fields = pointData.getChartableFields();
                    if(fixedFields !=null) {
                        for(i=0;i<fields.length;i++) { 
                            var field = fields[i];
                            if(fixedFields.indexOf(field.getId())>=0) {
                                df.push(field);
                            }
                        }
                    }
                }

                if(fixedFields !=null) {
                    return df;
                }

                var firstField = null;
                this.selectedCbx = [];
                for(var collectionIdx=0;collectionIdx<dataList.length;collectionIdx++) {             
                    var pointData = dataList[collectionIdx];
                    var fields = pointData.getChartableFields();
                    for(i=0;i<fields.length;i++) { 
                        var field = fields[i];
                        if(firstField==null) firstField = field;

                        var idBase = "cbx_" + collectionIdx +"_" +i;
                        var cbxId =  this.getDomId(idBase)
                        if($("#" + cbxId).is(':checked')) {
                            this.selectedCbx.push(idBase);
                            df.push(field);
                        }
                    }
                }

                if(df.length==0 && firstField!=null) {
                    df.push(firstField);
                }
                return df;
            },
            getDisplayMenuContents: function() {
                var get = "getRamaddaDisplay('" + this.id +"')";
                var moveRight = htmlUtil.onClick(get +".moveDisplayRight();", "Right");
                var moveLeft = htmlUtil.onClick(get +".moveDisplayLeft();", "Left");
                var moveUp = htmlUtil.onClick(get +".moveDisplayUp();", "Up");
                var moveDown = htmlUtil.onClick(get +".moveDisplayDown();", "Down");
                var deleteMe = htmlUtil.onClick("removeRamaddaDisplay('" + this.id +"')", "Remove Display");
                var form = "<form><table>" +
                    "<tr><td align=right><b>Move:</b></td><td>" + moveUp + " " +moveDown+  " " +moveRight+ " " + moveLeft +"</td></tr>"  +
                    "<tr><td align=right><b>Name:</b></td><td> " + htmlUtil.input("", this.getProperty("name",""), ["size","7","id",  this.getDomId("name")]) + "</td></tr>" +
                    "<tr><td align=right><b>Source:</b></td><td>"  + 
                    htmlUtil.input("", this.getProperty("eventsource",""), ["size","7","id",  this.getDomId("eventsource")]) +
                    "</td></tr>" +
                    "<tr><td align=right><b>Width:</b></td><td> " + htmlUtil.input("", this.getProperty("width",""), ["size","7","id",  this.getDomId("width")]) + "</td></tr>" +
                    "<tr><td align=right><b>Height:</b></td><td> " + htmlUtil.input("", this.getProperty("height",""), ["size","7","id",  this.getDomId("height")]) + "</td></tr>" +
                    "<tr><td align=right><b>Row:</b></td><td> " + htmlUtil.input("", this.getProperty("row",""), ["size","7","id",  this.getDomId("row")]) + "</td></tr>" +
                    "<tr><td align=right><b>Column:</b></td><td> " + htmlUtil.input("", this.getProperty("column",""), ["size","7","id",  this.getDomId("column")]) + "</td></tr>" +
                    "<tr style=\"border-top:1px #ccc solid;\"><td align=right></td><td> " + deleteMe+ "</td></tr>" +
                    "</table>" +
                    "</form>";
                return htmlUtil.div([], form);
            },
            loadInitialData: function() {
                if(!this.needsData() || this.properties.data==null) {
                    return;
                } 
                if(this.properties.data.hasData()) {
                    this.addData(this.properties.data);
                    return;
                } 
                this.properties.data.loadData(this);
            },
            getData: function() {
                if(!this.hasData()) return null;
                var dataList =  this.dataCollection.getList();
                return dataList[0];
            },
            hasData: function() {
                return this.dataCollection.hasData();
            },
            needsData: function() {
                return false;
            },
            getShowMenu: function() {
                return this.getProperty(PROP_SHOW_MENU, true);
            },
            getShowTitle: function() {
                return this.getProperty(PROP_SHOW_TITLE, true);
            },
            setDisplayProperty: function(key,value) {
                this.setProperty(key, value);
                $("#" + this.getDomId(key)).val(value);
            },
            deltaColumn: function(delta) {
                var column = parseInt(this.getProperty("column",0));
                column += delta;
                if(column<0) column = 0;
                this.setDisplayProperty("column",column);
                this.displayManager.doLayout();
            },
            deltaRow: function(delta) {
                var row = parseInt(this.getProperty("row",0));
                row += delta;
                if(row<0) row = 0;
                this.setDisplayProperty("row",row);
                this.displayManager.doLayout();
            },
            moveDisplayRight: function() {
                if(this.displayManager.layout == LAYOUT_COLUMNS) {
                    this.deltaColumn(1);
                } else {
                    this.displayManager.moveDisplayDown(this);
                }
            },
            moveDisplayLeft: function() {
                if(this.displayManager.layout == LAYOUT_COLUMNS) {
                    this.deltaColumn(-1);
                } else {
                    this.displayManager.moveDisplayUp(this);
                }
            },
            moveDisplayUp: function() {
                if(this.displayManager.layout == LAYOUT_ROWS) {
                    this.deltaRow(-1);
                } else {
                    this.displayManager.moveDisplayUp(this);
                }
            },
            moveDisplayDown: function() {
                if(this.displayManager.layout == LAYOUT_ROWS) {
                    this.deltaRow(1);
                } else {
                    this.displayManager.moveDisplayDown(this);
                }
            },
            getMenuContents: function() {
                return this.getDisplayMenuContents();
             },
           initMenu: function() {
           },
           popup: function(srcId, popupId) {
                var popup = ramaddaUtil.getDomObject(popupId);
                var srcObj = ramaddaUtil.getDomObject(srcId);
                if(!popup || !srcObj) return;
                var myalign = 'right top';
                var atalign = 'right bottom';
                showObject(popup);
                jQuery("#"+popupId ).position({
                        of: jQuery( "#" + srcId ),
                            my: myalign,
                            at: atalign,
                            collision: "none none"
                            });
                //Do it again to fix a bug on safari
                jQuery("#"+popupId ).position({
                        of: jQuery( "#" + srcId ),
                            my: myalign,
                            at: atalign,
                            collision: "none none"
                            });

                $("#" + popupId).draggable();
            },
            initUI:function() {
                this.checkFixedLayout();
                this.initMenu();
            },
            initDisplay:function() {
                this.initUI();
                this.setContents("<p>default html<p>");
            },
            updateUI: function(data) {
            },

            /*
              This creates the default layout for a display
              Its a table:
              <td>title id=ID_HEADER</td><td>align-right popup menu</td>
              <td colspan=2><div id=ID_DISPLAY_CONTENTS></div></td>
              the getDisplayContents method by default returns:
              <div id=ID_DISPLAY_CONTENTS></div>
              but can be overwritten by sub classes
              After getHtml is called the DisplayManager will add the html to the DOM then call
              initDisplay
              That needs to call setContents with the html contents of the display
            */
            getHtml: function() {
                var html = "";
                html +=   htmlUtil.div(["id", this.getDomId(ID_HEADER),"class", "display-header"]);
                var get = "getRamaddaDisplay('" + this.id +"')";
                var menuButton = htmlUtil.onClick(get+".showDialog();", 
                                                  htmlUtil.image(root+"/icons/downdart.png", 
                                                                 ["class", "display-dialog-button", "id",  this.getDomId(ID_DIALOG_BUTTON)]));

                var header = htmlUtil.div(["class","display-dialog-header"], htmlUtil.onClick("$('#" +this.getDomId(ID_DIALOG) +"').hide();",htmlUtil.image(root +"/icons/close.gif",["class","display-dialog-close"])));

                var menuContents = htmlUtil.div(["class", "display-dialog-contents"], this.getMenuContents());
                menuContents  = header + menuContents;
                var menu = htmlUtil.div(["class", "display-dialog", "id", this.getDomId(ID_DIALOG)], menuContents);
                var width = this.getWidth();
                var tableWidth = "100%";
                if(width>0) {
                    tableWidth = width+"px";
                }
                html += htmlUtil.openTag("table", ["border","0", "width",tableWidth, "cellpadding","0", "cellspacing","0"]);
                html += htmlUtil.openTag("tr", ["valign", "bottom"]);
                if(this.getShowTitle()) {
                    html += htmlUtil.td([], htmlUtil.div(["class","display-title","id",this.getDomId(ID_TITLE)], this.getTitle()));
                } else {
                    html += htmlUtil.td([], "");
                }
                if(this.getShowMenu()) {
                    html += htmlUtil.td(["align", "right"], menuButton);
                } else {
                    html += htmlUtil.td(["align", "right"], "");
                }
                html += htmlUtil.closeTag("tr");

                var contents = this.getContentsDiv();
                html += htmlUtil.tr([], htmlUtil.td(["colspan", "2"],contents));
                html += htmlUtil.closeTag("table")
                html += menu;
                return html;
            },
            showDialog: function() {
                var dialog =this.getDomId(ID_DIALOG); 
                this.popup(this.getDomId(ID_DIALOG_BUTTON), dialog);
            },

            getContentsDiv: function() {
                var extraStyle = "";
                var width = this.getWidth();
                if(width>0) {
                    extraStyle += "width:" + width +"px; ";
                }
                var height = this.getProperty("height",-1);
                if(height>0) {
                    extraStyle += " height:" + height +"px; ";
                }
                return  htmlUtil.div(["class","display-contents", "style", extraStyle, "id", this.getDomId(ID_DISPLAY_CONTENTS)],"");
            },
            removeDisplay: function() {
                this.displayManager.removeDisplay(this);
            },
            //Gets called before the displays are laid out
            prepareToLayout:function() {
                //Force setting the property from the input dom (which is about to go away)
                this.getColumn();
                this.getWidth();
                this.getHeight();
                this.getName();
                this.getEventSource();
            },
            getColumn: function() {
                return this.getFormValue("column",0);
            },
            getRow: function() {
                return this.getFormValue("row",0);
            },
            getWidth: function() {
                return this.getFormValue("width",0);
            },
            getHeight: function() {
                return this.getFormValue("height",0);
            },
            setTitle: function(title) {
                console.log("settitle:" + title);
                $("#" + this.getDomId(ID_TITLE)).html(title);
            },
            getTitle: function () {
                var title = this.getProperty("title");
                if(title!=null) {
                    return title;
                }
                if(this.dataCollection == null) {
                    return "";
                }
                var dataList =  this.dataCollection.getList();
                title = "";
                for(var collectionIdx=0;collectionIdx<dataList.length;collectionIdx++) {             
                    var pointData = dataList[collectionIdx];
                    if(collectionIdx>0) title+="/";
                    title += pointData.getName();
                }
                return title;
            },
            getIsLayoutFixed: function() {
                return this.getProperty(PROP_LAYOUT_FIXED,false);
            },
            hasData: function() {
                return this.dataCollection.hasData();
            },
            addData: function(pointData) { 
                this.dataCollection.addData(pointData);
            },
            //callback from the pointData.loadData call
            pointDataLoaded: function(pointData) {
                this.addData(pointData);
                this.updateUI(pointData);
                this.displayManager.pointDataLoaded(this, pointData);
            },
            //get an array of arrays of data 
            getStandardData : function(fields) {
                var dataList = [];
                //The first entry in the dataList is the array of names
                //The first field is the domain, e.g., time or index
                //        var fieldNames = ["domain","depth"];
                var fieldNames = ["Date"];
                for(i=0;i<fields.length;i++) { 
                    var field = fields[i];
                    var name  = field.getLabel();
                    if(field.getUnit()!=null) {
                        name += " (" + field.getUnit()+")";
                    }
                    fieldNames.push(name);
                }
                dataList.push(fieldNames);

                //These are Record objects 
                //TODO: handle multiple data sources (or not?)
                var pointData = this.dataCollection.getList()[0];
                var nonNullRecords = 0;
                var indexField = this.indexField;
                var allFields = this.allFields;
                var records = pointData.getRecords();

                //Check if there are dates and if they are different
                this.hasDate = false;
                var lastDate = null;
                for(j=0;j<records.length;j++) { 
                    var record = records[j];
                    var date = record.getDate();
                    if(date==null) {
                        continue;
                    }
                    if(lastDate!=null && lastDate.getTime()!=date.getTime()) {
                        this.hasDate = true;
                        break
                    }
                    lastDate = date;
                }




                for(j=0;j<records.length;j++) { 
                    var record = records[j];
                    var values = [];
                    var date = record.getDate();
                    if(indexField>=0) {
                        var field = allFields[indexField];
                        var value = record.getValue(indexField);
                        if(j==0) {
                            fieldNames[0] = field.getLabel();
                        }
                        values.push(value);
                    } else {
                        if(this.hasDate) {
                            date = new Date(date);
                            values.push(date);
                        } else {
                            if(j==0) {
                                fieldNames[0] = "Index";
                            }
                            values.push(j);
                        }
                    }

                    //            values.push(record.getElevation());
                    var allNull  = true;
                    for(var i=0;i<fields.length;i++) { 
                        var field = fields[i];
                        var value = record.getValue(field.getIndex());
                        if(value!=null) {
                            allNull = false;
                        }
                        values.push(value);
                    }
                    if(this.filters!=null) {
                        if(!this.applyFilters(record, values)) {
                            continue;
                        }
                    }
                    //TODO: when its all null values we get some errors
                    //                    console.log("values:" + values);
                    dataList.push(values);
                    if(!allNull) {
                        nonNullRecords++;
                    }
                }
                if(nonNullRecords==0) {
                    //                    console.log("Num non null:" + nonNullRecords);
                    return [];
                }
                return dataList;
            },
            applyFilters: function(record, values) {
                for(var i=0;i<this.filters.length;i++) {
                    if(!this.filters[i].recordOk(this, record, values)) {
                        return false;
                    }
                }
                return true;
            }
        }
        );

        var filter = this.getProperty(PROP_DISPLAY_FILTER);
        if(filter!=null) {
            //semi-colon delimited list of filter definitions
            //display.filter="filtertype:params;filtertype:params;
            //display.filter="month:0-11;
            var filters = filter.split(";");
            for(i in filters) {
                filter = filters[i];
                var toks = filter.split(":");
                var type  = toks[0];
                if(type == "month") {
                    this.filters.push(new MonthFilter(toks[1]));
                } else {
                    console.log("unknown filter:" + type);
                }
            }
        }
}



/*
Create a chart
id - the id of this chart. Has to correspond to a div tag id 
pointData - A PointData object (see below)
 */
function RamaddaMultiChart(displayManager, id, properties) {
    //Init the defaults first
    $.extend(this, {
            indexField: -1,
            colors: ['blue', 'red', 'green'],
            curveType: 'none',
            fontSize: 0,
            vAxisMinValue:NaN,
            vAxisMaxValue:NaN
           });

    RamaddaSuper(this, new RamaddaDisplay(displayManager, id, properties.chartType, properties));

    $.extend(this, {
            getType: function () {
                return this.getProperty(PROP_CHART_TYPE,DISPLAY_LINECHART);
            },
            needsData: function() {
                return true;
            },
            initDisplay:function() {
                this.initUI();
                this.updateUI();
            },
            updateUI: function() {
                this.addFieldsCheckboxes();
                this.displayData();
            },
            fieldSelectionChanged: function() {
                this.displayData();
            },
            getMenuContents: function() {
                var height = this.getProperty(PROP_HEIGHT,"400");
                var html  =  htmlUtil.div(["id",  this.getDomId(ID_FIELDS),"class", "display-fields","style","overflow-y: auto;    max-height:" + height +"px;"]);
                html +=  this.getDisplayMenuContents();
                return html;
            },
            handleRecordSelection: function(source, index, record, html) {
                var chartType = this.getProperty(PROP_CHART_TYPE,DISPLAY_LINECHART);
                if(source==this) {
                    return;
                }
                this.setChartSelection(index);
            },
            displayData: function() {
                if(this.getShowTitle()) {
                    this.setTitle(this.getTitle());
                }
                if(!this.hasData()) {
                    this.clearChart();
                    return;
                }

                this.allFields =  this.dataCollection.getList()[0].getRecordFields();

                var selectedFields = this.getSelectedFields();
                if(selectedFields.length==0) {
                    this.setContents("No fields selected");
                    return;
                }
                var dataList = this.getStandardData(selectedFields);
                if(dataList.length==0) {
                    this.setContents(htmlUtil.div(["class","display-message"],
                                                  "No data available"));
                    return;
                }

                var chartType = this.getProperty(PROP_CHART_TYPE,DISPLAY_LINECHART);
                this.makeChart(chartType, dataList, selectedFields);
            },
            clearChart: function() {
                if(this.chart !=null) {
                    this.chart.clearChart();
                }
            },
            setChartSelection: function(index) {
                if(this.chart!=null) {
                    this.chart.setSelection([{row:index, column:null}]); 
                }
            },

            makeGoogleChart: function(chartType, dataList, selectedFields) {
                if(typeof google == 'undefined') {
                    this.setContents("No google");
                    return;
                }
                var dataTable = google.visualization.arrayToDataTable(dataList);
                var   chartOptions = {};
                $.extend(chartOptions, {
                        lineWidth: 1,
                        colors: this.colors,
                        curveType:this.curveType,
                        vAxis: {}});


                if(this.fontSize>0) {
                    chartOptions.fontSize = this.fontSize;
                }
                if(!isNaN(this.vAxisMinValue)) {
                    chartOptions.vAxis.minValue =parseFloat(this.vAxisMinValue);
                }
                if(!isNaN(this.vAxisMaxValue)) {
                    chartOptions.vAxis.maxValue =parseFloat(this.vAxisMaxValue);
                }
                var width = "95%";
                if(selectedFields.length>1) {
                    width = "80%";
                }
                $.extend(chartOptions, {
                    series: [{targetAxisIndex:0},{targetAxisIndex:1},],
                    legend: { position: 'bottom' },
                    chartArea:{left:75,top:10,height:"60%",width:width}
                 });
                if(chartType == DISPLAY_BARCHART) {
                    chartOptions.orientation =  "horizontal";
                    this.chart = new google.visualization.BarChart(document.getElementById(this.getDomId(ID_DISPLAY_CONTENTS)));
                } else  if(chartType == DISPLAY_TABLE) {
                    this.chart = new google.visualization.Table(document.getElementById(this.getDomId(ID_DISPLAY_CONTENTS)));
                } else {
                    //                    this.chart =  new Dygraph.GVizChart(
                    //                    document.getElementById(this.getDomId(ID_DISPLAY_CONTENTS)));
                    this.chart = new google.visualization.LineChart(document.getElementById(this.getDomId(ID_DISPLAY_CONTENTS)));
                }
                if(this.chart!=null) {
                    this.chart.draw(dataTable, chartOptions);
                    var theDisplay = this;
                    google.visualization.events.addListener(this.chart, 'select', function() {
                            var index = theDisplay.chart.getSelection()[0].row;
                            theDisplay.displayManager.handleRecordSelection(theDisplay, 
                                                                            theDisplay.dataCollection.getList()[0], index);
                        });
                }
            }
        });


    this.makeChart = this.makeGoogleChart;
}



function LinechartDisplay(displayManager, id, properties) {
    properties = $.extend({"chartType": DISPLAY_LINECHART}, properties);
    RamaddaSuper(this, new RamaddaMultiChart(displayManager, id, properties));
    addRamaddaDisplay(this);
}

function BarchartDisplay(displayManager, id, properties) {
    properties = $.extend({"chartType": DISPLAY_BARCHART}, properties);
    RamaddaSuper(this, new RamaddaMultiChart(displayManager, id, properties));
    addRamaddaDisplay(this);
}


function TableDisplay(displayManager, id, properties) {
    properties = $.extend({"chartType": DISPLAY_TABLE}, properties);
    RamaddaSuper(this, new RamaddaMultiChart(displayManager, id, properties));
    addRamaddaDisplay(this);
}


function RamaddaTextDisplay(displayManager, id, properties) {
    $.extend(this, new RamaddaDisplay(displayManager, id, DISPLAY_TEXT, properties));
    addRamaddaDisplay(this);
    $.extend(this, {
            lastHtml:"<p>&nbsp;<p>&nbsp;<p>",
            initDisplay: function() {
                this.initUI();
                this.setContents(this.lastHtml);
            },
            handleRecordSelection: function(source, index, record, html) {
                this.lastHtml = html;
                this.setContents(html);
            }
        });
}



function RamaddaMapDisplay(displayManager, id, properties) {
    var ID_LATFIELD  = "latfield";
    var ID_LONFIELD  = "lonfield";
    var ID_MAP = "map";
    RamaddaSuper(this, new RamaddaDisplay(displayManager, id, DISPLAY_MAP, properties));
    addRamaddaDisplay(this);
    $.extend(this, {
            initBounds:displayManager.initMapBounds,
            initPoints:displayManager.initMapPoints,
            mapBoundsSet:false,
            polygons:[],
            markers: {},
            initDisplay: function() {
                this.initUI();
                var html = "";
                var extraStyle = "min-height:200px;";
                var width = this.getWidth();
                if(width>0) {
                    extraStyle += "width:" + width +"px; ";
                }
                var height = this.getProperty("height",300);
                //                var height = this.getProperty("height",-1);
                if(height>0) {
                    extraStyle += " height:" + height +"px; ";
                }

                html+=htmlUtil.div(["class", "display-map-map", "style",extraStyle, "id", this.getDomId(ID_MAP)]);
                html+="<br>";
                html+= htmlUtil.openTag("div",["class","display-map-latlon"]);
                html+= htmlUtil.openTag("form");
                html+= "Latitude: " + htmlUtil.input(this.getDomId(ID_LATFIELD), "", ["size","7","id",  this.getDomId(ID_LATFIELD)]);
                html+= "  ";
                html+= "Longitude: " + htmlUtil.input(this.getDomId(ID_LONFIELD), "", ["size","7","id",  this.getDomId(ID_LONFIELD)]);
                html+= htmlUtil.closeTag("form");
                html+= htmlUtil.closeTag("div");
                this.setContents(html);

                var currentPolygons = this.polygons;
                this.polygons = [];


                var params = {
                    "defaultMapLayer": this.getProperty("defaultMapLayer", map_default_layer)
                };
                this.map = new RepositoryMap(this.getDomId(ID_MAP), params);
                this.map.initMap(false);
                this.map.addClickHandler(this.getDomId(ID_LONFIELD), this.getDomId(ID_LATFIELD), null, this);
                if(this.initBounds!=null) {
                    var b  = this.initBounds;
                    this.setInitMapBounds(b[0],b[1],b[2],b[3]);
                }


                if(this.initPoints!=null && this.initPoints.length>1) {
                    this.polygons.push(this.initPoints);
                    this.map.addPolygon("basemap", clonePoints(this.initPoints), null);
                }
                
                if(currentPolygons!=null) {
                    for(var i=0;i<currentPolygons.length;i++)  {
                        this.polygons.push(currentPolygons[i]);
                        this.map.addPolygon("basemap", clonePoints(currentPolygons[i]), null);
                    }
                }
            },
            loadInitialData: function() {
                if(this.displayManager.getData().length>0) {
                    this.handlePointDataLoaded(this, this.displayManager.getData()[0]);
                }
            },

            getContentsDiv: function() {
                return  htmlUtil.div(["class","display-contents", "id", this.getDomId(ID_DISPLAY_CONTENTS)],"");
            },

            handleClick: function (theMap, lon,lat) {
                this.displayManager.handleMapClick(this, lon, lat);
            },

            getPosition:function() {
                var lat = $("#" + this.getDomId(ID_LATFIELD)).val();
                var lon = $("#" + this.getDomId(ID_LONFIELD)).val();
                if(lat == null) return null;
                return [lat,lon];
            },

           setInitMapBounds: function(north, west, south, east) {
                if(!this.map) return;
                this.map.centerOnMarkers(new OpenLayers.Bounds(west,south,east, north));
            },

            handlePointDataLoaded: function(source, pointData) {
                var bounds = [NaN,NaN,NaN,NaN];
                var records = pointData.getRecords();
                var points =RecordGetPoints(records, bounds);
                if(!isNaN(bounds[0])) {
                    this.initBounds = bounds;
                    this.initPoints = points;
                    this.displayManager.setMapState(points, bounds);
                    this.setInitMapBounds(bounds[0],bounds[1],bounds[2], bounds[3]);
                    if(this.map!=null && points.length>1) {
                        this.polygons.push(points);
                        this.map.addPolygon("basemap", clonePoints(points), null);
                    }
                }

            },
             handleDisplayDelete: function(source) {
                var marker  = this.markers[source];
                if(marker!=null) {
                    this.map.removeMarker(marker);
                }
            },
            handleRecordSelection: function(source, index, record, html) {
                if(record.hasLocation()) {
                    var latitude = record.getLatitude();
                    var longitude = record.getLongitude();
                    var point = new OpenLayers.LonLat(longitude, latitude);
                    var marker  = this.markers[source];
                    if(marker!=null) {
                        this.map.removeMarker(marker);
                    }
                    this.markers[source] =  this.map.addMarker(source.getId(), point, null,html);
                }}
        });
}


function RamaddaAnimationDisplay(displayManager, id, properties) {
    var ID_START = "start";
    var ID_STOP = "stop";
    var ID_TIME = "time";
    $.extend(this, new RamaddaDisplay(displayManager, id, DISPLAY_ANIMATION, properties));
    addRamaddaDisplay(this);
    $.extend(this, {
            running: false,
            timestamp: 0,
            index: 0,              
            sleepTime: 500,
            iconStart: root+"/icons/display/control.png",
            iconStop: root+"/icons/display/control-stop-square.png",
            iconBack: root+"/icons/display/control-stop-180.png",
            iconForward: root+"/icons/display/control-stop.png",
            iconFaster: root+"/icons/display/plus.png",
            iconSlower: root+"/icons/display/minus.png",
            iconBegin: root+"/icons/display/control-double-180.png",
            iconEnd: root+"/icons/display/control-double.png",
            deltaIndex: function(i) {
                this.stop();
                this.setIndex(this.index+i);
            }, 
            setIndex: function(i) {
                if(i<0) i=0;
                this.index = i;
                this.applyStep();
            },
            toggle: function() {
                if(this.running) {
                    this.stop();
                } else {
                    this.start();
                }
            },
            tick: function() {
                if(!this.running) return;
                this.index++;
                this.applyStep();
                var theAnimation = this;
                setTimeout(function() {theAnimation.tick();}, this.sleepTime);
            },
           applyStep: function() {
                var data = this.displayManager.getDefaultData();
                if(data == null) return;
                var records = data.getRecords();
                if(records == null) {
                    $("#" + this.getDomId(ID_TIME)).html("no records");
                    return;
                }
                if(this.index>=records.length) {
                    this.index = records.length-1;
                }
                var record = records[this.index];
                var label = "";
                if(record.getDate()!=null) {
                    label += htmlUtil.b("Date:") + " "  + record.getDate();
                } else {
                    label += htmlUtil.b("Index:") +" " + this.index;
                }
                $("#" + this.getDomId(ID_TIME)).html(label);
                this.displayManager.handleRecordSelection(this, null, this.index);
            },
            faster: function() {
                this.sleepTime = this.sleepTime/2;
                if(this.sleepTime==0) this.sleepTime  = 100;
            },
            slower: function() {
                this.sleepTime = this.sleepTime*1.5;
            },
            start: function() {
                if(this.running) return;
                this.running = true;
                this.timestamp++;
                $("#"+this.getDomId(ID_START)).attr("src",this.iconStop);
                this.tick();
            },
            stop: function() {
                if(!this.running) return;
                this.running = false;
                this.timestamp++;
                $("#"+this.getDomId(ID_START)).attr("src",this.iconStart);
            },
            initDisplay: function() {
                this.initUI();
                this.stop();

                var get = "getRamaddaDisplay('" + this.id +"')";
                var html =  "";
                html+=  htmlUtil.onClick(get +".setIndex(0);", htmlUtil.image(this.iconBegin,["title","beginning", "class", "display-animation-button", "xwidth","32"]));
                html +="  ";
                html+=  htmlUtil.onClick(get +".deltaIndex(-1);", htmlUtil.image(this.iconBack,["title","back 1", "class", "display-animation-button", "xwidth","32"]));
                html +="  ";
                html+=  htmlUtil.onClick(get +".toggle();", htmlUtil.image(this.iconStart,["title","play/stop", "class", "display-animation-button", "xwidth","32", "id", this.getDomId(ID_START)]));
                html +="  ";
                html+=  htmlUtil.onClick(get +".deltaIndex(1);", htmlUtil.image(this.iconForward,["title","forward 1", "class", "display-animation-button", "xwidth","32"]));
                html +="  ";
                html+=  htmlUtil.onClick(get +".faster();", htmlUtil.image(this.iconFaster,["class", "display-animation-button", "title","faster", "xwidth","32"]));
                html +="  ";
                html+=  htmlUtil.onClick(get +".slower();", htmlUtil.image(this.iconSlower,["class", "display-animation-button", "title","slower", "xwidth","32"]));
                html+=  htmlUtil.div(["id", this.getDomId(ID_TIME)],"&nbsp;");
                this.setTitle("Animation");
                this.setContents(html);
            },
        });
}


function RamaddaOperandsDisplay(displayManager, id, properties) {
    var ID_SELECT = "select";
    var ID_SELECT1 = "select1";
    var ID_SELECT2 = "select2";
    var ID_NEWDISPLAY = "newdisplay";
    var ID_CHARTTYPE = "charttype";

    $.extend(this, {
            entryType: null,
                entryParent: null});
    $.extend(this, new RamaddaDisplay(displayManager, id, DISPLAY_OPERANDS, properties));
    addRamaddaDisplay(this);
    $.extend(this, {
            initDisplay: function() {
                this.initUI();
                var jsonUrl = null;
                if(this.entryType!=null) {
                    jsonUrl = root +"/search/type/" + this.entryType +"?max=50&output=json&type=" + this.entryType;
                }
                if(jsonUrl == null) {
                    this.setContents("<p>No entry type given");
                    return;
                }
                this.entryList = new EntryList(jsonUrl, this);
                this.setContents("<p>Loading<p>");
            },
            entryListChanged: function(entryList) {
                var html = "<form>";
                html += "<p>";
                html += htmlUtil.openTag("table",["class","formtable","cellspacing","0","cellspacing","0"]);
                var entries = this.entryList.getEntries();
                var get = "getRamaddaDisplay('" + this.id +"')";
                for(var j=1;j<=2;j++) {
                    var select= htmlUtil.openTag("select",["id", this.getDomId(ID_SELECT +j)]);
                    select += htmlUtil.tag("option",["title","","value",""],
                                         "-- Select data --");
                    for(var i=0;i<entries.length;i++) {
                        var entry = entries[i];
                        var label = entry.getIconImage() +" " + entry.getName();
                        select += htmlUtil.tag("option",["title",entry.getName(),"value",entry.getId()],
                                             entry.getName());
                        
                    }
                    select += htmlUtil.closeTag("select");
                    html += htmlUtil.formEntry("Data:",select);
                }

                var select  = htmlUtil.openTag("select",["id", this.getDomId(ID_CHARTTYPE)]);
                select += htmlUtil.tag("option",["title","","value","linechart"],
                                     "Line chart");
                select += htmlUtil.tag("option",["title","","value","barchart"],
                                     "Bar chart");
                select += htmlUtil.closeTag("select");
                html += htmlUtil.formEntry("Chart Type:",select);

                html += htmlUtil.closeTag("table");
                html += "<p>";
                html +=  htmlUtil.tag("div", ["class", "display-button", "id",  this.getDomId(ID_NEWDISPLAY)],"New Chart");
                html += "<p>";
                html += "</form>";
                this.setContents(html);
                var theDisplay = this;
                $("#"+this.getDomId(ID_NEWDISPLAY)).button().click(function(event) {
                       theDisplay.createDisplay();
                   });
            },
            createDisplay: function() {
                var entry1 = this.entryList.getEntry($("#" + this.getDomId(ID_SELECT1)).val());
                var entry2 = this.entryList.getEntry($("#" + this.getDomId(ID_SELECT2)).val());
                if(entry1 == null) {
                    alert("No data selected");
                    return;
                }
                var pointDataList = [];

                pointDataList.push(new PointData(entry1.getName(), null, null, root +"/entry/show?&output=points.product&product=points.json&numpoints=1000&entryid=" +entry1.getId()));
                if(entry2!=null) {
                    pointDataList.push(new PointData(entry2.getName(), null, null, root +"/entry/show?&output=points.product&product=points.json&numpoints=1000&entryid=" +entry2.getId()));
                }

                //Make up some functions
                var operation = "average";
                var derivedData = new  DerivedPointData(this.displayManager, "Derived Data", pointDataList,operation);
                var pointData = derivedData;
                var chartType = $("#" + this.getDomId(ID_CHARTTYPE)).val();
                displayManager.createDisplay(chartType, {
                        "layoutFixed": false,
                        "data": pointData
                   });
            }

        });
}
