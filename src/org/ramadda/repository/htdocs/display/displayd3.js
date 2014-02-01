/**
Copyright 2008-2014 Geode Systems LLC
*/



function RamaddaD3Display(displayManager, id, properties) {

    var ID_SVG = "svg";
    $.extend(this, new RamaddaDisplay(displayManager, id, "d3", properties));
    addRamaddaDisplay(this);

    $.extend(this, {

            initDisplay: function() {
                this.initUI();
                //I've been calling back to this display with the following
                var get = "getRamaddaDisplay('" + this.id +"')";
                var height = this.getProperty("height",300);
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
                if(selectedFields.length==0) return;
                this.addFieldsCheckboxes();
                var pointData = this.getData();
                if(pointData == null) return;
                var svg = d3.select("#" + this.getDomId(ID_SVG));
                this.svg = svg;
                if(svg == null) return;


                var margin = {top: 0, left: 40, bottom: 20, right: 0};
                var width = displayWidth - margin.left - margin.right;
                var height = displayHeight - margin.top - margin.bottom;

                var fields = pointData.getNumericFields();
                var records = pointData.getRecords();
                var ranges =  RecordGetRanges(fields,records);
                var dataIndex = selectedFields[0].getIndex();
                var range = ranges[dataIndex];
                var elevationRange =  RecordGetElevationRange(fields,records);
                var barWidth = width/records.length;
                var barHeight = 10;

                var xScale = d3.scale.linear().domain([0, records.length]).range([0, width]);
                var yScale = d3.scale.linear().domain([elevationRange[0], elevationRange[1]]).range([0, height]);

                svg.attr("width", width + margin.left + margin.right)
                    .attr("height", height + margin.top + margin.bottom);


                // add the canvas to the DOM
                var rects = svg.
                    append("svg:svg").
                    attr("width", width).
                    attr("height", height).
                    attr("transform", "translate(" + margin.left + "," + margin.top + ")");
                
                rects.selectAll("rect").
                    data(records).
                    enter().
                    append("svg:rect").
                    attr("x", function(record, index) { 
                            return xScale(index); 
                        }).
                    attr("y", function(record) { 
                            return height - yScale(record.getElevation()); 
                        }).
                    attr("height", barHeight).
                    attr("width", barWidth).
                    attr("fill", function(record) {
                            var value = record.getValue(dataIndex);
                            var scale = (value-range[0])/(range[1]-range[0]);
                            var v = parseInt(scale*256);
                            return "rgb(" + v + ", " + v +", " + v+ ")";
                        });


                //The axis isn't working right now
                var yAxis = d3.svg.axis()
                    .scale(yScale)
                    .orient("left");

                svg.append("g")
                    .attr("class", "axis")
                    .call(yAxis);


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
addGlobalDisplayType({type: "d3", label:"D3 Example"});