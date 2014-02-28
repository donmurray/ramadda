/**
Copyright 2008-2014 Geode Systems LLC
*/

// Lets see if we create a support class for D3
function getAxis(axisType,range){
	
	var axis;
	if ( axisType == "time"){
		axis = d3.time.scale().range(range);
	}else{
		axis = d3.scale.linear().range(range);
	}
	return axis;
}

function getDataValue(axis,value,index){

	var data;
	if(axis.fieldIdx>=0){
		data = value.getData()[axis.fieldIdx];
	}else{	
		switch(axis.type){
			case "time": 
				data = new Date(value.getDate());
				break
			case "elevation":
				console.log(value.getElevation());
				data = value.getElevation();
					break
			case "latitude":
				data = value.getLatitude();
			case "longitude":
				data = value.getLongitude();
			default:
				data = value.getData()[index];
		}
	}		

    if(axis.reverse=="true"){
		return -1*data;
	}else{
		return data;
	}
}

// This will be the default but we can add more colorscales
function getColorFromColorBar(value,range){
	
	var colors = ["#00008B","#0000CD","#0000FF","#00FFFF","#7CFC00","#FFFF00","#FFA500","#FF4500","#FF0000","#8B0000"];
	
	var colorScale = d3.scale.linear()
		.domain([0, colors.length-1])
		.range(range);
	
	var colorScaler = d3.scale.linear()
						.range(colors)
						.domain(d3.range(0,colors.length).map(colorScale));
	
	color = colorScaler(value);
	return color;
}

// This is for the path lines the previous function for generic ones. 
function addColorBar(svg,colors,colorSpacing){
	var colorBar = svg.append("g")
					.attr({
						"id"        : "colorBarG",
						"transform" : "translate(" + (this.displayWidth-40) + ",0)"
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


function RamaddaD3Display(displayManager, id, properties) {
    var ID_SVG = "svg";
	var SUPER; 
    // To get it to the console
	testProperties = properties;
	
	RamaddaUtil.inherit(this, SUPER = new RamaddaDisplay(displayManager, id, "d3", properties));
    addRamaddaDisplay(this);
				
	$.extend(this, {
            initDisplay: function() {
                this.initUI();
								
                this.setTitle(properties.graph.title);
				
				var height = this.getProperty("height",300);
				var margin = {top: 20, right: 50, bottom: 30, left: 50};
				
                var html = HtmlUtil.div([ATTR_ID, this.getDomId(ID_SVG),"style","height:" + height +"px;"],"");
                this.setContents(html);

				// To create dinamic size of the div
				var displayHeight = parseInt((d3.select("#"+this.getDomId(ID_SVG)).style("height")).split("px")[0])-margin.top-margin.bottom;//this.getProperty("height",300);//d3.select("#"+this.getDomId(ID_SVG)).style("height");//
                var displayWidth  = parseInt((d3.select("#"+this.getDomId(ID_SVG)).style("width")).split("px")[0])-margin.left-margin.right;//this.getProperty("width",600);//d3.select("#"+this.getDomId(ID_SVG)).style("width");//
                	
				// To solve the problem with the classess within the class
				var myThis = this;
				
				var zoom = d3.behavior.zoom()
							.on("zoom", function() {myThis.zoomBehaviour()});
				this.zoom = zoom;
				
				
				this.svg = d3.select("#" + this.getDomId(ID_SVG)).append("svg")
								.attr("width", displayWidth + margin.left + margin.right)
								.attr("height", displayHeight + margin.top + margin.bottom)
								.attr("class","D3graph")
								.call(zoom)
								.on("click", function(){myThis.click(d3.event)})
								.on("dblclick", function(){myThis.dbclick(d3.event)})
						    .append("g")
								.attr("transform", "translate(" + margin.left + "," + margin.top + ")");
				
				// Define the Axis
				// 100 pixels for the legend... lets see if we keep it
				this.x = getAxis(properties.graph.axis.x.type,[0,displayWidth-100]);
				this.y = getAxis(properties.graph.axis.y.type,[displayHeight,0]);
			
				this.xAxis = d3.svg.axis()
					.scale(this.x)
					.orient("bottom");

				
				this.yAxis = d3.svg.axis()
					.scale(this.y)
					.orient("left");
			
				// Add Axis to the plot
				this.svg.append("g")
				  .attr(ATTR_CLASS, "x axis")
				  .attr("transform", "translate(0," + displayHeight + ")")
				  .call(this.xAxis);
				  
				this.svg.append("g")
				  .attr(ATTR_CLASS, "y axis")
				  .call(this.yAxis);
				
				
				// Color Bar
				var colors = ["#00008B","#0000CD","#0000FF","#00FFFF","#7CFC00","#FFFF00","#FFA500","#FF4500","#FF0000","#8B0000"];

				var colorSpacing = 100 / ( colors.length - 1 );
				
				var colorBar = addColorBar(this.svg,colors,colorSpacing);
				
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
			// onlyZoom is not updating the axis
            updateUI: function(onlyZoom) {
			
				test= this;
				
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
				
				var axis = properties.graph.axis;
				if(onlyZoom){
					this.zoom.x(this.x);
					this.zoom.y(this.y);
				}else{
					
					// Update axis for the zoom and other changes
					this.x.domain(d3.extent(records, function(d) { return getDataValue(axis.x,d,selectedFields[0].getIndex()); }));
					// the y domain depends on the first selected element I have to think about it.
					this.y.domain(d3.extent(records, function(d) { return getDataValue(axis.y,d,selectedFields[0].getIndex()); }));
					
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
						.x(function(d) { return x( getDataValue(axis.x,d,selectedFields[fieldIdx].getIndex())); })
						.y(function(d) { return y( getDataValue(axis.y,d,selectedFields[fieldIdx].getIndex())); });
					
					myThis=this;
					displayLine = this.svg.append("path")
									  .datum(records)
									  .attr("class", "line")
									  .attr("d", line)
									  .on("mousemove", function(){myThis.mouseover(d3.event)})
									  .attr("fill", "none")
									  .attr("stroke",function(d){return color(fieldIdx);})
									  .attr("stroke-width","0.5px");

					if(properties.graph.axis.z=="selectedField"){
						displayLine.attr("stroke", "url(#colorBarGradient)")
									  
					}
					
					if(properties.graph.extraLine=="movingAverage"){
						// Plot moving average Line
						var movingAverageLine = d3.svg.line()
								.x(function(d) { return x(getDataValue(axis.x,d,selectedFields[fieldIdx].getIndex())); })
								.y(function(d,i) {
									if (i == 0) {
										return _movingSum = getDataValue(axis.y,d,selectedFields[fieldIdx].getIndex());
									} else {
										_movingSum +=  getDataValue(axis.y,d,selectedFields[fieldIdx].getIndex());
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
								  .attr("viewBox", "50 50 100 100 ")
								  .style("stroke-dasharray", ("3, 3"));
					}
					  
					
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


function RamaddaD3LineChartDisplay(displayManager, id, properties) {

	properties["graph"]={};
	properties["graph"]["title"]="Line Chart";
	properties["graph"]["axis"]={};
	properties["graph"]["axis"]["y"]={}
	properties["graph"]["axis"]["y"]["type"]="value";
	properties["graph"]["axis"]["y"]["fieldname"]="selectedField";
	properties["graph"]["axis"]["x"]={}
	properties["graph"]["axis"]["x"]["type"]="time";
	properties["graph"]["axis"]["x"]["fieldname"]="time";
	properties["graph"]["axis"]["z"]="none";
	properties["graph"]["extraLine"]="movingAverage";
	
	return new RamaddaD3Display(displayManager,id,properties);
	
}
//Add this type to the global list
addGlobalDisplayType({type: "D3LineChart", label:"D3 LineChart"});

function RamaddaProfileDisplay(displayManager, id, properties) {
	
	properties["graph"]={};
	properties["graph"]["title"]="Profile chart";
	properties["graph"]["axis"]={};
	properties["graph"]["axis"]["y"]={}
	properties["graph"]["axis"]["y"]["type"]="elevation";
	properties["graph"]["axis"]["y"]["fieldname"]="depth";
	properties["graph"]["axis"]["y"]["fieldIdx"]=3;
	properties["graph"]["axis"]["y"]["reverse"]="true";
	properties["graph"]["axis"]["x"]={}
	properties["graph"]["axis"]["x"]["type"]="value";
	properties["graph"]["axis"]["x"]["fieldname"]="value";
	properties["graph"]["axis"]["z"]="none";
	properties["graph"]["extraLine"]="none";
	
	return new RamaddaD3Display(displayManager,id,properties);

}

addGlobalDisplayType({type: "Profile", label:"Profile"});

function RamaddaGliderCrossSectionDisplay(displayManager, id, properties) {
	
	properties["graph"]={};
	properties["graph"]["title"]="Glider cross section";
	properties["graph"]["axis"]={};
	properties["graph"]["axis"]["y"]={}
	properties["graph"]["axis"]["y"]["type"]="elevation";
	properties["graph"]["axis"]["y"]["fieldname"]="depth";
	properties["graph"]["axis"]["y"]["reverse"]="true";
	properties["graph"]["axis"]["x"]={}
	properties["graph"]["axis"]["x"]["type"]="time";
	properties["graph"]["axis"]["x"]["fieldname"]="time";
	properties["graph"]["axis"]["z"]="selectedField";
	properties["graph"]["extraLine"]="none";

	return new RamaddaD3Display(displayManager,id,properties);

}

addGlobalDisplayType({type: "GliderCrossSection", label:"Glider cross section"});
