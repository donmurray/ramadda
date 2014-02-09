/**
Copyright 2008-2014 Geode Systems LLC
*/

// Testing color


function RamaddaD3Display(displayManager, id, properties) {

    var ID_SVG = "svg";
	
    $.extend(this, new RamaddaDisplay(displayManager, id, "d3", properties));
    addRamaddaDisplay(this);

    $.extend(this, {

            initDisplay: function() {
                this.initUI();
                this.setTitle("D3 Example");
                this.updateUI();
            },
            needsData: function() {return true;},
            getMenuContents: function() {
                var height = this.getProperty(PROP_HEIGHT,"400");
                var html  =  HtmlUtil.div(["id",  this.getDomId(ID_FIELDS),"class", "display-fields","style","overflow-y: auto;    max-height:" + height +"px;"]);
                html +=  this.getDisplayMenuContents();
                return html;
            },
            fieldSelectionChanged: function() {
                this.updateUI();
            },
            updateUI: function() {
                var displayHeight = this.getProperty("height",300);
                var displayWidth = this.getProperty("width",300);
                var html = HtmlUtil.div(["id", this.getDomId(ID_SVG),"style","border:1px #000 solid; min-height:" + displayHeight +"px;"], "");
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
                var ranges =  RecordGetRanges(fields,records);
                var elevationRange =  RecordGetElevationRange(fields,records);
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
					
					var colors = ["#00008B","#0000CD","#0000FF","#00FFFF","#7CFC00","#FFFF00","#FFA500","#FF4500","#FF0000","#8B0000"];

					var colorScale = d3.scale.linear()
						.domain([0, colors.length-1])
						.range(range);
						
					var colorScaler = d3.scale.linear()
										.range(colors)
										.domain(d3.range(0,colors.length).map(colorScale));

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
                                return colorScaler(value);
                            });
                }

            },
            //this gets called when an event source has selected a record
            handleRecordSelection: function(source, index, record, html) {
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
    $.extend(this, new RamaddaDisplay(displayManager, id, "d3", properties));
    addRamaddaDisplay(this);
    $.extend(this, {
            initDisplay: function() {
                this.initUI();
                this.setTitle("D3 LineChart");
				
				var height = this.getProperty("height",300);
				var margin = {top: 20, left: 50, bottom: 20, right: 20};
				
                var html = HtmlUtil.div(["id", this.getDomId(ID_SVG),"style","height:" + height +"px;"],"");
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

				this.svg = d3.select("#" + this.getDomId(ID_SVG)).append("svg")
							.attr("width", displayWidth + margin.left + margin.right)
							.attr("height", displayHeight + margin.top + margin.bottom)
						  .append("g")
							.attr("transform", "translate(" + margin.left + "," + margin.top + ")");

				 
				
				
				// To solve the problem with the classess within the class
				var myThis = this;
				// This will be the event handler. In this case the zoom will be only inside the graph area nor the axis
				this.svg.append("svg:rect")
					.attr("width", displayWidth)
					.attr("height", displayHeight)
					.attr("id","rect_"+this.getDomId(ID_SVG))
					.call(d3.behavior.zoom().on("zoom", function(){myThis.zoomBehaviour()}))
					.on("click", function(){myThis.click(event)});
					
				this.displayWidth=displayWidth;
				this.displayHeight=displayHeight;
				this.color = d3.scale.category10();
				
                this.updateUI();
            },
            needsData: function() {return true;},
            getMenuContents: function() {
                var height = this.getProperty(PROP_HEIGHT,"400");
                var html  =  HtmlUtil.div(["id",  this.getDomId(ID_FIELDS),"class", "display-fields",]);
                html +=  this.getDisplayMenuContents();
                return html;
            },
            fieldSelectionChanged: function() {
                this.updateUI();
            },
            updateUI: function() {
			
				// To be used inside a function we can use this.x inside them so we extract as variables. 
				var x = this.x;
				var y = this.y;
				var color = this.color;
				
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
                var ranges =  RecordGetRanges(fields,records);
                var elevationRange =  RecordGetElevationRange(fields,records);
                var offset = (elevationRange[1] - elevationRange[0])*0.05;

				this.x.domain(d3.extent(records, function(d) { return new Date(d.getDate()); }));
				// the y domain depends on the first selected element I have to think about it.
				this.y.domain(d3.extent(records, function(d) { return d.getData()[selectedFields[0].getIndex()]; }));
				
				this.svg.append("g")
				  .attr("class", "x axis")
				  .attr("transform", "translate(0," + this.displayHeight + ")")
				  .attr("fill","none")
				  .attr("stroke","#555555")
				  .attr("shape-rendering","crispEdges")
				  .call(this.xAxis);
				  

				this.svg.append("g")
				  .attr("class", "y axis")
				  .call(this.yAxis)
				  .attr("fill","none")
				  .attr("stroke","#555555")
				  .attr("shape-rendering","crispEdges");
					
				for(var fieldIdx=0;fieldIdx<selectedFields.length;fieldIdx++) {
                    /*var dataIndex = selectedFields[fieldIdx].getIndex();
                    var range = ranges[dataIndex];
					*/
					
					// Plot line for the values
					var line = d3.svg.line()
						.x(function(d) { return x(new Date(d.getDate())); })
						.y(function(d) { return y(d.getData()[selectedFields[fieldIdx].getIndex()]); });
					
					this.svg.append("path")
					  .datum(records)
					  .attr("class", "line")
					  .attr("d", line)
					  .attr("fill","none")
					  .attr("stroke",function(d){return color(fieldIdx);})
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
					  .attr("class", "line")
					  .attr("d", movingAverageLine(records))
					  .attr("fill","none")
					  .attr("stroke",function(d){return color(fieldIdx);})
					  .attr("stroke-width","1.5px")
					  .style("stroke-dasharray", ("3, 3"));
					  
					
					// Legend element Maybe create a function or see how we implement the legend
				    this.svg.append("svg:rect")
					   .attr("x", this.displayWidth-100)
					   .attr("y", (50+50*fieldIdx))
					   .attr("stroke", function(d){return color(fieldIdx);})
					   .attr("height", 2)
					   .attr("width", 40);
					   
					this.svg.append("svg:text")
						   .attr("x", this.displayWidth-100+40+10) // position+color rect+padding
						   .attr("y", (55+55*fieldIdx))
						   .attr("stroke", function(d){return color(fieldIdx);})
						   .attr("style","font-size:8px")
						   .text(selectedFields[fieldIdx].getLabel());
				}


            },
			zoomBehaviour: function(){
				// I think we will have to do this nightmare...
				console.log(d3.event.translate);
				console.log(d3.event.scale);
				
				// This will zoom the entire graph I will keep it here for other visualizations
				this.svg.attr("transform",
					  "translate(" + d3.event.translate + ")"
					  + " scale(" + d3.event.scale + ")");
				
				testing=this;
				/*this.svg.select("g.x.axis").call(this.xAxis);
			    this.svg.select("g.y.axis").call(this.yAxis);*/
				
				this.updateUI();
				
			},
            //this gets called when an event source has selected a record
            handleRecordSelection: function(source, index, record, html) {
                //                  this.setContents(html);
            },
            click: function(event) {
                console.log("Clicked");
				console.log(event);
            },
			getSVG: function() {
				return this.svg;
			}
        });
}


//Add this type to the global list
addGlobalDisplayType({type: "D3LineChart", label:"D3 LineChart"});