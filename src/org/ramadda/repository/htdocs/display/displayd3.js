/**
Copyright 2008-2014 Geode Systems LLC
*/

// Lets see if we create a support class for D3
// This will be the default but we can add more colorscales


					
function getColorFromColorBar(value,range){
	
	console.log(value);
	console.log(range);
	
	var colors = ["#00008B","#0000CD","#0000FF","#00FFFF","#7CFC00","#FFFF00","#FFA500","#FF4500","#FF0000","#8B0000"];
	
	var colorScale = d3.scale.linear()
		.domain([0, colors.length-1])
		.range(range);
	
	var colorScaler = d3.scale.linear()
						.range(colors)
						.domain(d3.range(0,colors.length).map(colorScale));
	
	color = colorScaler(value);
	console.log(color);
	return color;
}

function RamaddaD3Display(displayManager, id, properties) {
    var ID_SVG = "svg";
    var SUPER; 
    RamaddaUtil.inherit(this, SUPER = new RamaddaDisplay(displayManager, id, "d3", properties));
    addRamaddaDisplay(this);

    $.extend(this, {
            initDisplay: function() {
                this.initUI();
                this.setTitle("D3 Example");
                this.updateUI();
            },
            needsData: function() {return true;},
            initDialog: function() {
                this.addFieldsCheckboxes();
            },
            getDialogContents: function() {
                var height = this.getProperty(PROP_HEIGHT,"400");
                var html  =  HtmlUtil.div([ATTR_ID,  this.getDomId(ID_FIELDS),ATTR_CLASS, "display-fields","style","overflow-y: auto;    max-height:" + height +"px;"]);
                html +=  SUPER.getDialogContents.apply(this);
                return html;
            },
            fieldSelectionChanged: function() {
                this.updateUI();
            },
            updateUI: function() {
                var displayHeight = this.getProperty("height",300);
                var displayWidth = this.getProperty("width",300);
                var html = HtmlUtil.div([ATTR_ID, this.getDomId(ID_SVG),"style","border:1px #000 solid; min-height:" + displayHeight +"px;"], "");
                this.setContents(html);

                var selectedFields = this.getSelectedFields();
                if(selectedFields.length==0) {
                    $("#" + this.getDomId(ID_SVG)).html("No fields");
                    return;
                }
                this.addFieldsCheckboxes();
                var pointData = this.getData();
                if(pointData == null) {
                    $("#" + this.getDomId(ID_SVG)).html("No data");
                    console.log("no data");
                    return;
                }

                var svg = d3.select("#" + this.getDomId(ID_SVG));
                this.svg = svg;
                if(svg == null) {
                    console.log("no svg");
                    return;
                }

                var fields = pointData.getNumericFields();
                var records = pointData.getRecords();
                var ranges =  RecordUtil.getRanges(fields,records);
                var elevationRange =  RecordUtil.getElevationRange(fields,records);
                var offset = (elevationRange[1] - elevationRange[0])*0.05;


                var heightPerField = displayHeight/selectedFields.length;
                var margin = {top: 20, left: 20, bottom: 20, right: 20};

                svg.attr("width", width + margin.left + margin.right)
                    .attr("height", displayHeight);

					
               for(var fieldIdx=0;fieldIdx<selectedFields.length;fieldIdx++) {
                    var dataIndex = selectedFields[fieldIdx].getIndex();
                    var range = ranges[dataIndex];
                    var width = displayWidth - margin.left - margin.right;

                    //displayHeight
                    var height = heightPerField - margin.top - margin.bottom;

                    var barWidth = width/records.length+1;
                    var barHeight = height/20+1;

                    var xScale = d3.scale.linear().domain([0, records.length]).range([0, width]);
                    var yScale = d3.scale.linear().domain([elevationRange[0]-offset, elevationRange[1]+offset]).range([0, height]);
					
					var extentZ = d3.extent(records, function (d) { return records;} )
					
                    var rects = svg.
                        append("svg:svg").
                        attr("width", width).
                        attr("height", height).
                        attr("transform", "translate(" + margin.left + "," + margin.top + ")");
                    
					//Draw the rects
                    rects.selectAll("rect").
                        data(records).
                        enter().
                        append("svg:rect").
                        attr("x", function(record, index) { 
                                return xScale(index); 
                            }).
                        attr("y", function(record) { 
								return  yScale(record.getElevation())+barHeight/2; 
                            }).
                        attr("height", barHeight).
                        attr("width", barWidth).
                        attr("fill", function(record) {
						
                                var value = record.getValue(dataIndex);
                                //var scale = (value-range[0])/(range[1]-range[0]);
                                //var v = parseInt(scale*255);
                                return getColorFromColorBar(value,range);
                            });
                }

            },
            //this gets called when an event source has selected a record
            handleEventRecordSelection: function(source, args) {
                //args: index, record, html
                console.log(source);
                console.log(index);
                console.log(record);
            },
            click: function() {
                $("#"+this.getDomId(ID_CLICK)).html("Click again");
            }
        });
}


//Add this type to the global list
addGlobalDisplayType({type: "d3", label:"D3 Example"});

function RamaddaD3LineChartDisplay(displayManager, id, properties) {
    var ID_SVG = "svg";
    var SUPER; 
    RamaddaUtil.inherit(this, SUPER = new RamaddaDisplay(displayManager, id, "d3", properties));
    addRamaddaDisplay(this);
    $.extend(this, {
            initDisplay: function() {
                this.initUI();
								
                this.setTitle("D3 LineChart");
				
				var height = this.getProperty("height",300);
				var margin = {top: 20, right: 50, bottom: 30, left: 50};
				
                var html = HtmlUtil.div([ATTR_ID, this.getDomId(ID_SVG),"style","height:" + height +"px;"],"");
                this.setContents(html);

				// To create dinamic size of the div
				var displayHeight = parseInt((d3.select("#"+this.getDomId(ID_SVG)).style("height")).split("px")[0])-margin.top-margin.bottom;//this.getProperty("height",300);//d3.select("#"+this.getDomId(ID_SVG)).style("height");//
                var displayWidth  = parseInt((d3.select("#"+this.getDomId(ID_SVG)).style("width")).split("px")[0])-margin.left-margin.right;//this.getProperty("width",600);//d3.select("#"+this.getDomId(ID_SVG)).style("width");//
                
                // 100 pixels for the legend... lets see if we keep it
				this.x = d3.time.scale()
					.range([0, displayWidth-100]);

				this.y = d3.scale.linear()
					.range([displayHeight, 0]);
			
				this.xAxis = d3.svg.axis()
					.scale(this.x)
					.orient("bottom");
					//.ticks(5);

				this.yAxis = d3.svg.axis()
					.scale(this.y)
					.orient("left");
					
				// To solve the problem with the classess within the class
				var myThis = this;
				
				
				var zoom = d3.behavior.zoom()
							.on("zoom", function() {myThis.zoomBehaviour()});
				
				this.zoom = zoom;
				
				this.svg = d3.select("#" + this.getDomId(ID_SVG)).append("svg")
								.attr("width", displayWidth + margin.left + margin.right)
								.attr("height", displayHeight + margin.top + margin.bottom)
								.call(zoom)
								.on("click", function(){myThis.click(event)})
								.on("dblclick", function(){myThis.dbclick(event)})
						    .append("g")
								.attr("transform", "translate(" + margin.left + "," + margin.top + ")");
				
				// Add the axis
				this.svg.append("g")
				  .attr(ATTR_CLASS, "x axis")
				  .attr("transform", "translate(0," + displayHeight + ")")
				  .attr("fill","none")
				  .attr("stroke","#555555")
				  .attr("shape-rendering","crispEdges")
				  .call(this.xAxis);
				  

				this.svg.append("g")
				  .attr(ATTR_CLASS, "y axis")
				  .call(this.yAxis)
				  .attr("fill","none")
				  .attr("stroke","#555555")
				  .attr("shape-rendering","crispEdges");
				
				
				this.displayWidth=displayWidth;
				this.displayHeight=displayHeight;
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
            updateUI: function(onlyZoom) {
			
				test = this;
				
				var selectedFields = this.getSelectedFields();
                if(selectedFields.length==0) {
                    $("#" + this.getDomId(ID_SVG)).html("No fields");
                    return;
                }
                this.addFieldsCheckboxes();
                pointData = this.getData();
				if(pointData == null) {
                    $("#" + this.getDomId(ID_SVG)).html("No data");
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
				
				if(onlyZoom){
					this.zoom.x(this.x);
					this.zoom.y(this.y);
				}else{
					// Update axis for the zoom and other changes
					this.x.domain(d3.extent(records, function(d) { return new Date(d.getDate()); }));
					// the y domain depends on the first selected element I have to think about it.
					this.y.domain(d3.extent(records, function(d) { return d.getData()[selectedFields[0].getIndex()]; }));
					this.zoom.x(this.x);
					this.zoom.y(this.y);
				}
				
				this.svg.selectAll(".y.axis")
					.call(this.yAxis);
			
				this.svg.selectAll(".x.axis")
					.call(this.xAxis);
				
				// Remove previous lines
				this.svg.selectAll(".line").remove();
				this.svg.selectAll(".legendElement").remove();
				
				
				
				for(var fieldIdx=0;fieldIdx<selectedFields.length;fieldIdx++) {
                    var dataIndex=selectedFields[fieldIdx].getIndex()
					var range = ranges[dataIndex];
					
					
					// Plot line for the values
					var line = d3.svg.line()
						.x(function(d) { return x(new Date(d.getDate())); })
						.y(function(d) { return y(d.getData()[dataIndex]); });
					
					// For the color
					var extentZ = d3.extent(records, function(d) { return y(d.getData()[dataIndex]); })
					var colors = ["#00008B","#0000CD","#0000FF","#00FFFF","#7CFC00","#FFFF00","#FFA500","#FF4500","#FF0000","#8B0000"];

					var colorSpacing = 100 / ( colors.length - 1 );

					var colorBarScale = d3.scale.linear()
							.range([ 0, this.displayHeight])
							.domain(extentZ);

					var colorBarAxisY = d3.svg.axis()
							.scale( colorBarScale  )
							.ticks( 10 )
							.tickSubdivide( true )
							.orient("right");

					
					this.svg.append("path")
					  .datum(records)
					  .attr(ATTR_CLASS, "line")
					  .attr("d", line)
					  .attr("fill", "none")
					  .attr("stroke", "url(#colorBarGradient)")
					  .attr("stroke-width","0.5px");
                      
					// Plot moving average Line
					var movingAverageLine = d3.svg.line()
							.x(function(d) { return x(new Date(d.getDate())); })
							.y(function(d,i) {
								if (i == 0) {
									return _movingSum = 0;
								} else {
									_movingSum += d.getData()[selectedFields[fieldIdx].getIndex()];
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
					  
					  
					
					// Legend element Maybe create a function or see how we implement the legend
				    this.svg.append("svg:rect")
					   .attr("class","legendElement")
					   .attr("x", this.displayWidth-100)
					   .attr("y", (50+50*fieldIdx))
					   .attr("stroke", function(d){return color(fieldIdx);})
					   .attr("height", 2)
					   .attr("width", 40);
					   
					this.svg.append("svg:text")
						    .attr("class","legendElement")
						   .attr("x", this.displayWidth-100+40+10) // position+color rect+padding
						   .attr("y", (55+55*fieldIdx))
						   .attr("stroke", function(d){return color(fieldIdx);})
						   .attr("style","font-size:8px")
						   .text(selectedFields[fieldIdx].getLabel());
				}

				// Plot Color 
				var colorBar = this.svg.append("g")
						.attr({
							"id"        : "colorBarG",
							"transform" : "translate(" + (this.displayWidth+40) + ",0)"
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

            },
			zoomBehaviour: function(){
				// I think we will have to do this nightmare...
				
				// This will zoom the entire graph I will keep it here for other visualizations
				/* this.svg.attr("transform",
					  "translate(" + d3.event.translate + ")"
					  + " scale(" + d3.event.scale + ")"); */
				
				// Call redraw with only zoom don't update extent of the data.
				this.updateUI(true);
				
			},
            //this gets called when an event source has selected a record
            handleEventRecordSelection: function(source, args) {
                //this.setContents(args.html);
            },
            click: function(event) {
				// TO DO
				console.log(event);
            },
			dbclick:function(event) {
				// Unzoom
				this.zoom
				this.updateUI();
            },
			getSVG: function() {
				return this.svg;
			}
        });
}


//Add this type to the global list
addGlobalDisplayType({type: "D3LineChart", label:"D3 LineChart"});