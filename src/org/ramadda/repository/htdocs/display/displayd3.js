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
                var html  =  htmlUtil.div(["id",  this.getDomId(ID_FIELDS),"class", "display-fields","style","overflow-y: auto;    max-height:" + height +"px;"]);
                html +=  this.getDisplayMenuContents();
                return html;
            },
            fieldSelectionChanged: function() {
                this.updateUI();
            },
            updateUI: function() {
                var displayHeight = this.getProperty("height",300);
                var displayWidth = this.getProperty("width",300);
                var html = htmlUtil.div(["id", this.getDomId(ID_SVG),"style","border:1px #000 solid; min-height:" + displayHeight +"px;"], "");
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

                /*
                var xAxis = d3.svg.axis();
                xAxis.scale(xScale);
                xAxis.orient("bottom");
                svg.append("g")
                    .call(xAxis);

                //The axis isn't working right now
                var yAxis = d3.svg.axis()
                    .scale(yScale)
                    .orient("left");
                svg.append("g")
                    .attr("class", "axis")
                    .call(yAxis);
                */

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
                this.updateUI();
            },
            needsData: function() {return true;},
            getMenuContents: function() {
                var height = this.getProperty(PROP_HEIGHT,"400");
                var html  =  htmlUtil.div(["id",  this.getDomId(ID_FIELDS),"class", "display-fields","style","overflow-y: auto;    max-height:" + height +"px;"]);
                html +=  this.getDisplayMenuContents();
                return html;
            },
            fieldSelectionChanged: function() {
                this.updateUI();
            },
            updateUI: function() {
				
                var displayHeight = this.getProperty("height",300);
                var displayWidth = this.getProperty("width",600);
                var margin = {top: 20, left: 50, bottom: 20, right: 20};

                var html = htmlUtil.div(["id", this.getDomId(ID_SVG),"style","border:1px #000 solid; min-height:" + displayHeight +"px;"], "");
                this.setContents(html);

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

				
				// 100 pixels for the legend... lets see if we keep it
				var x = d3.time.scale()
					.range([0, displayWidth-100]);

				var y = d3.scale.linear()
					.range([displayHeight, 0]);

			    /*var zoom = d3.behavior.zoom()
							.x(x)
							.y(y)
							.on("zoom", this.zoomBehaviours);*/
							
				var xAxis = d3.svg.axis()
					.scale(x)
					.orient("bottom");
					//.ticks(5);

				var yAxis = d3.svg.axis()
					.scale(y)
					.orient("left");

				var svg = d3.select("#" + this.getDomId(ID_SVG)).append("svg")
							.attr("width", displayWidth + margin.left + margin.right)
							.attr("height", displayHeight + margin.top + margin.bottom)
						  .append("g")
							.attr("transform", "translate(" + margin.left + "," + margin.top + ")");

				x.domain(d3.extent(records, function(d) { return new Date(d.getDate()); }));
				// the y domain depends on the first selected element I have to think about it.
				y.domain(d3.extent(records, function(d) { return d.getData()[selectedFields[0].getIndex()]; }));
				
				
				
				svg.append("g")
				  .attr("class", "x axis")
				  .attr("transform", "translate(0," + displayHeight + ")")
				  .attr("fill","none")
				  .attr("stroke","#555555")
				  .attr("shape-rendering","crispEdges")
				  .call(xAxis);
				  

				svg.append("g")
				  .attr("class", "y axis")
				  .call(yAxis)
				  .attr("fill","none")
				  .attr("stroke","#555555")
				  .attr("shape-rendering","crispEdges");
				  // This will be the label text
				  /*.append("text")
					  .attr("transform", "rotate(-90)")
					  .attr("y", 6)
					  .attr("dy", ".71em")
					  .style("text-anchor", "end")
					  .text(selectedFields[0].getLabel()); // again lets see the Y axis 
				  */
				
				//svg.call(zoom);
				
				color = d3.scale.category20();
				
				for(var fieldIdx=0;fieldIdx<selectedFields.length;fieldIdx++) {
                    /*var dataIndex = selectedFields[fieldIdx].getIndex();
                    var range = ranges[dataIndex];
					*/
					var line = d3.svg.line()
						.x(function(d) { return x(new Date(d.getDate())); })
						.y(function(d) { return y(d.getData()[selectedFields[fieldIdx].getIndex()]); });
						
					svg.append("path")
					  .datum(records)
					  .attr("class", "line")
					  .attr("d", line)
					  .attr("fill","none")
					  .attr("stroke",function(d){return color(fieldIdx);})
					  .attr("stroke-width","1.5px");
					
					// Legend element
				    svg.append("svg:rect")
					   .attr("x", displayWidth-100)
					   .attr("y", (50+50*fieldIdx))
					   .attr("stroke", function(d){return color(fieldIdx);})
					   .attr("height", 2)
					   .attr("width", 40);
					   
					svg.append("svg:text")
						   .attr("x", displayWidth-100+40+10) // position+color rect+padding
						   .attr("y", (55+55*fieldIdx))
						   .attr("stroke", function(d){return color(fieldIdx);})
						   .text(selectedFields[fieldIdx].getLabel());
				}
				test=this;

            },
			zoomBehaviour: function(){
				// I think we will have to do this nightmare...
				console.log(d3.event.translate);
				console.log(d3.event.scale);
				
			},
            //this gets called when an event source has selected a record
            handleRecordSelection: function(source, index, record, html) {
                //                  this.setContents(html);
            },
            click: function() {
                $("#"+this.getDomId(ID_CLICK)).html("Click again");
            }
        });
}


//Add this type to the global list
addGlobalDisplayType({type: "D3LineChart", label:"D3 LineChart"});