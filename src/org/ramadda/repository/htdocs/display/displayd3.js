/**
Copyright 2008-2015 Geode Systems LLC
*/



//Note: I put all of the chart definitions together at the top so one can see everything that is available here
var DISPLAY_D3_GLIDER_CROSS_SECTION = "GliderCrossSection";
var DISPLAY_D3_PROFILE = "profile";
var DISPLAY_D3_LINECHART = "D3LineChart";

//Note: Added requiresData and category
addGlobalDisplayType({type: DISPLAY_D3_LINECHART, label:"D3 LineChart",requiresData:true,category:"Charts"});
addGlobalDisplayType({type: DISPLAY_D3_PROFILE, label:"Profile",requiresData:true,category:"Charts"});
addGlobalDisplayType({type:DISPLAY_D3_GLIDER_CROSS_SECTION , label:"Glider cross section",requiresData:true,category:"Charts"});

//Note: define meaningful things as variables not as string literals
var FIELD_TIME = "time";
var FIELD_DEPTH = "depth";
var FIELD_VALUE = "value";
var FIELD_SELECTEDFIELD = "selectedfield";

var TYPE_LATITUDE = "latitude";
var TYPE_LONGITUDE = "longitude";
var TYPE_TIME = "time";
var TYPE_VALUE = "value";
var TYPE_ELEVATION = "elevation";


var FUNC_MOVINGAVERAGE = "movingAverage";

var D3Util = {
    foo:"bar",
    getAxis: function(axisType,range) {
        var axis;
        if (axisType == FIELD_TIME) {
            axis = d3.time.scale().range(range);
        } else {
            axis = d3.scale.linear().range(range);
        }
        return axis;
    }, 
    getDataValue: function(axis,record,index) {
        var data;
        if(axis.fieldIdx>=0){
            data = record.getData()[axis.fieldIdx];
        } else {	
            switch(axis.type) {
            case TYPE_TIME: 
            data = new Date(record.getDate());
            break;
            case TYPE_ELEVATION:
            //console.log(record.getElevation());
            data = record.getElevation();
            break;
            case TYPE_LATITUDE:
            data = record.getLatitude();
            case TYPE_LONGITUDE:
            data = record.getLongitude();
            default:
            data = record.getData()[index];
            }
        }		


        if(axis.reverse==true) {
            return -1*data;
        } else {
            return data;
        }
    }, 
// This will be the default but we can add more colorscales
    getColorFromColorBar: function(value,range) {
	var colors = ["#00008B","#0000CD","#0000FF","#00FFFF","#7CFC00","#FFFF00","#FFA500","#FF4500","#FF0000","#8B0000"];
	var colorScale = d3.scale.linear()
        .domain([0, colors.length-1])
        .range(range);
	
	var colorScaler = d3.scale.linear()
        .range(colors)
        .domain(d3.range(0,colors.length).map(colorScale));
	
	color = colorScaler(value);
	return color;
    },
// This is for the path lines the previous function for generic ones. 
    addColorBar: function(svg,colors,colorSpacing, displayWidth) {
        //Note: this originally had this.displayWidth which was undefined
        var colorBar = svg.append("g")
        .attr({
                "id"        : "colorBarG",
                "transform" : "translate(" + (displayWidth-40) + ",0)"
            });

        colorBar.append("g")
        .append("defs")
        .append("linearGradient")
        .attr({
                id : "colorBarGradient",
                    x1 : "0%",
                    y1 : "100%",
                    x2 : "0%",
                    y2 : "0%"
                    })
        .selectAll("stop")
        .data(colors)
        .enter()
        .append("stop")
        .attr({
                "offset": function(d,i){return colorSpacing * (i) + "%"},
                    "stop-color":function(d,i){return colors[i]},
			"stop-opacity":1
                            });
	
        return colorBar;
    }
}


function RamaddaD3Display(displayManager, id, properties) {
    // To get it to the console
    testProperties = properties;

    var ID_SVG = "svg";
    var SUPER; 
    RamaddaUtil.inherit(this, SUPER = new RamaddaDisplay(displayManager, id, "d3", properties));
    addRamaddaDisplay(this);

    RamaddaUtil.defineMembers(this, {
            initDisplay: function() {
                this.initUI();
                this.setTitle(properties.graph.title);

                //Note: use innerHeight/innerWidth wiki attributes
                var width = this.getProperty("innerWidth", 600);
                var height = this.getProperty("innerHeight",300);
                var margin = {top: 20, right: 50, bottom: 30, left: 50};
                var divStyle = 
                    "height:" + height +"px;" +
                    "width:" + width +"px;";
                var html = HtmlUtil.div([ATTR_ID, this.getDomId(ID_SVG),ATTR_STYLE,divStyle],"");
                this.setContents(html);

                // To create dynamic size of the div
                this.displayHeight = parseInt((d3.select("#"+this.getDomId(ID_SVG)).style("height")).split("px")[0])-margin.top-margin.bottom;//this.getProperty("height",300);//d3.select("#"+this.getDomId(ID_SVG)).style("height");//
                this.displayWidth  = parseInt((d3.select("#"+this.getDomId(ID_SVG)).style("width")).split("px")[0])-margin.left-margin.right;//this.getProperty("width",600);//d3.select("#"+this.getDomId(ID_SVG)).style("width");//
                	
                //                console.log("WxH:" + this.displayHeight +" " + this.displayWidth);

                // To solve the problem with the classess within the class
                var myThis = this;
                var zoom = d3.behavior.zoom()
                    .on("zoom", function() {myThis.zoomBehaviour()});
                this.zoom = zoom;
                this.svg = d3.select("#" + this.getDomId(ID_SVG)).append("svg")
                    .attr("width", this.displayWidth + margin.left + margin.right)
                    .attr("height", this.displayHeight + margin.top + margin.bottom)
                    .attr(ATTR_CLASS,"D3graph")
                    .call(zoom)
                    .on("click", function(){myThis.click(d3.event)})
                    .on("dblclick", function(){myThis.dbclick(d3.event)})
                    .append("g")
                    .attr("transform", "translate(" + margin.left + "," + margin.top + ")");
                
                // Define the Axis
                // 100 pixels for the legend... lets see if we keep it
                this.x = D3Util.getAxis(properties.graph.axis.x.type,[0, this.displayWidth-100]);
                this.y = D3Util.getAxis(properties.graph.axis.y.type,[this.displayHeight,0]);
		
                this.xAxis = d3.svg.axis()
                    .scale(this.x)
                    .orient("bottom");

                this.yAxis = d3.svg.axis()
                    .scale(this.y)
                    .orient("left");
			
                // Add Axis to the plot
                this.svg.append("g")
                    .attr(ATTR_CLASS, "x axis")
                    .attr("transform", "translate(0," + this.displayHeight + ")")
                    .call(this.xAxis);
				  
                this.svg.append("g")
                    .attr(ATTR_CLASS, "y axis")
                    .call(this.yAxis);
				
				
                // Color Bar
                var colors = ["#00008B","#0000CD","#0000FF","#00FFFF","#7CFC00","#FFFF00","#FFA500","#FF4500","#FF0000","#8B0000"];

                var colorSpacing = 100 / ( colors.length - 1 );

                var colorBar = D3Util.addColorBar(this.svg,colors,colorSpacing,this.displayWidth);
                this.color = d3.scale.category10();
                this.updateUI();
            },
            needsData: function() {return true;},
            initDialog: function() {
                this.addFieldsCheckboxes();
            },
            getDialogContents: function() {
                var height = this.getProperty(PROP_HEIGHT,"400");
                var html  =  HtmlUtil.div([ATTR_ID,  this.getDomId(ID_FIELDS),ATTR_CLASS, "display-fields",]);
                html +=  SUPER.getDialogContents.apply(this);
                return html;
            },
            fieldSelectionChanged: function() {
                this.updateUI();
            },
            // onlyZoom is not updating the axis
            updateUI: function(pointData) {
                //Note: Not sure why onlyZoom was a function param. The pointData gets passes in 
                //when the json is loaded
                //            updateUI: function(onlyZoom) {
                var onlyZoom = false;

                //Note: if we write to the SVG dom element then we lose the svg object that got created in initDisplay
                //Not sure how to show a message to the user
                if(!this.hasData()) {
                    //this.writeHtml(ID_SVG, HtmlUtil.div([ATTR_CLASS,"display-message"], this.getLoadingMessage()));
                    return;
                }
                test= this;
                var selectedFields = this.getSelectedFields();
                if(selectedFields.length==0) {
                    //this.writeHtml(ID_SVG, "No fields selected");
                    return;
                }
                this.addFieldsCheckboxes();
                pointData = this.getData();
                if(pointData == null) {
                    //this.writeHtml(ID_SVG, "No data");
                    console.log("no data");
                    return;
                }
				
                var fields = pointData.getNumericFields();
                var records = pointData.getRecords();
                var ranges =  RecordUtil.getRanges(fields,records);
                var elevationRange =  RecordUtil.getElevationRange(fields,records);
                var offset = (elevationRange[1] - elevationRange[0])*0.05;
                // To be used inside a function we can use this.x inside them so we extract as variables. 
                var x = this.x;
                var y = this.y;
                var color = this.color;
                var axis = properties.graph.axis;

                if(onlyZoom){
                    this.zoom.x(this.x);
                    this.zoom.y(this.y);
                } else {
                    // Update axis for the zoom and other changes
                    this.x.domain(d3.extent(records, function(d) { return D3Util.getDataValue(axis.x,d,selectedFields[0].getIndex()); }));
                    // the y domain depends on the first selected element I have to think about it.
                    this.y.domain(d3.extent(records, function(d) { return D3Util.getDataValue(axis.y,d,selectedFields[0].getIndex()); }));
                    
                    this.zoom.x(this.x);
                    this.zoom.y(this.y);
                }
				
                this.svg.selectAll(".y.axis").call(this.yAxis);
                this.svg.selectAll(".x.axis").call(this.xAxis);
				
                // Remove previous lines
                this.svg.selectAll(".line").remove();
                this.svg.selectAll(".legendElement").remove();

                var myThis=this;
                for(var fieldIdx=0;fieldIdx<selectedFields.length;fieldIdx++) {
                    var dataIndex=selectedFields[fieldIdx].getIndex();
                    var range = ranges[dataIndex];
                    // Plot line for the values
                    var line = d3.svg.line()
                        .x(function(d) { return x( D3Util.getDataValue(axis.x,d,selectedFields[fieldIdx].getIndex())); })
                        .y(function(d) { return y( D3Util.getDataValue(axis.y,d,selectedFields[fieldIdx].getIndex())); });
                    
                    displayLine = this.svg.append("path")
                        .datum(records)
                        .attr(ATTR_CLASS, "line")
                        .attr("d", line)
                        .on("mousemove", function(){myThis.mouseover(d3.event)})
                        .attr("fill", "none")
                        .attr("stroke",function(d){return color(fieldIdx);})
                        .attr("stroke-width","0.5px");

                    if(properties.graph.axis.z==FIELD_SELECTEDFIELD) {
                        displayLine.attr("stroke", "url(#colorBarGradient)");
                    }
					
                    if(properties.graph.derived !=null) {
                        var funcs = properties.graph.derived.split(",");
                        for(funcIdx=0;funcIdx<funcs.length;funcIdx++) {
                            var func  = funcs[funcIdx];
                            if(func==FUNC_MOVINGAVERAGE) {
                                // Plot moving average Line
                                var movingAverageLine = d3.svg.line()
                                    .x(function(d) { return x(D3Util.getDataValue(axis.x,d,selectedFields[fieldIdx].getIndex())); })
                                    .y(function(d,i) {
                                            if (i == 0) {
                                                return _movingSum = D3Util.getDataValue(axis.y,d,selectedFields[fieldIdx].getIndex());
                                            } else {
                                                _movingSum +=  D3Util.getDataValue(axis.y,d,selectedFields[fieldIdx].getIndex());
                                            }
                                            return y(_movingSum / i);
                                        })
                                    .interpolate("basis");
                                this.svg.append("path")
                                    .attr(ATTR_CLASS, "line")
                                    .attr("d", movingAverageLine(records))
                                    .attr("fill","none")
                                    .attr("stroke",function(d){return color(fieldIdx);})
                                    .attr("stroke-width","1.5px")
                                    .attr("viewBox", "50 50 100 100 ")
                                    .style("stroke-dasharray", ("3, 3"));
                            } else {
                                console.log("Error: Unknown derived function:" + func);
                            }                            

                        }
                    }

                    // Legend element Maybe create a function or see how we implement the legend
                    this.svg.append("svg:rect")
                        .attr(ATTR_CLASS,"legendElement")
                        .attr("x", this.displayWidth-100)
                        .attr("y", (50+50*fieldIdx))
                        .attr("stroke", function(d){return color(fieldIdx);})
                        .attr("height", 2)
                        .attr("width", 40);
					   
                    this.svg.append("svg:text")
                        .attr(ATTR_CLASS,"legendElement")
                        .attr("x", this.displayWidth-100+40+10) // position+color rect+padding
                        .attr("y", (55+55*fieldIdx))
                        .attr("stroke", function(d){return color(fieldIdx);})
                        .attr("style","font-size:50%")
                        .text(selectedFields[fieldIdx].getLabel());
                }
            },

            zoomBehaviour: function(){
                // Call redraw with only zoom don't update extent of the data.
                this.updateUI(true);
            },
            //this gets called when an event source has selected a record
            handleEventRecordSelection: function(source, args) {
                //this.setContents(args.html);
            },
            mouseover: function() {
                // TO DO
                testing=d3.event;
                console.log("mouseover");
            },
            click: function(event) {
                // TO DO
                console.log("click:" + event);
            },
            dbclick:function(event) {
                // Unzoom
                this.zoom();
                this.updateUI();
            },
            getSVG: function() {
                return this.svg;
            }
        });
}


function RamaddaD3LineChartDisplay(displayManager, id, properties) {
    var dfltProperties = {};
    //Note: use json structures to define the props
    dfltProperties.graph = {
        title: "Line chart",
        //Note: changed this to "derived" from "extraLine".
        //This is a comma separated list of functions (for now just one)
        derived: FUNC_MOVINGAVERAGE,
        axis: {
            y: {
                type: TYPE_VALUE,
                fieldname: FIELD_SELECTEDFIELD
                },
            x: {
                type: TYPE_TIME,
                fieldname: FIELD_TIME,
            }
        }};

    properties = $.extend(dfltProperties, properties);
    return new RamaddaD3Display(displayManager,id,properties);
}


function RamaddaProfileDisplay(displayManager, id, properties) {
    var dfltProperties = {};
    //Note: use json structures to define the props
    dfltProperties.graph = {
        title: "Profile chart",
        derived: null,
        axis: {
            y: {
                type: TYPE_ELEVATION,
                fieldname: FIELD_DEPTH,
                fieldIdx: 3,
                reverse: true},
            x: {
                type: TYPE_VALUE,
                fieldname: FIELD_VALUE,
            },
        }};
    //Note: now set the properties
    properties = $.extend(dfltProperties, properties);
    return new RamaddaD3Display(displayManager, id, properties);
}




function RamaddaGliderCrossSectionDisplay(displayManager, id, properties) {
    var dfltProperties = {};
    //Note: use json structures to define the props
    dfltProperties.graph = {
        title: "Glider cross section",
        derived: null,
        axis: {
            y: {
                type: TYPE_ELEVATION,
                fieldname: FIELD_DEPTH,
                reverse: true},
            x: {
                type: TYPE_TIME,
                fieldname: FIELD_TIME,
            },
            z: FIELD_SELECTEDFIELD,
        }};
    properties = $.extend(dfltProperties, properties);

    return new RamaddaD3Display(displayManager,id,properties);
}


