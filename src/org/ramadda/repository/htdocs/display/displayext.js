/**
Copyright 2008-2014 Geode Systems LLC
*/



/*
  This gets created by the displayManager.createDisplay('example')
 */
function RamaddaExampleDisplay(displayManager, id, properties) {

    //Dom id for example
    //The displays use display.getDomId(ID_CLICK) to get a unique (based on the display id) id
    var ID_CLICK = "click";

    var ID_DATA = "data";

    //Create the base class
    RamaddaUtil.inherit(this, new RamaddaDisplay(displayManager, id, "example", properties));

    //Add this display to the list of global displays
    addRamaddaDisplay(this);

    //Define the methods
    RamaddaUtil.defineMembers(this, {
            //gets called by displaymanager after the displays are layed out
            initDisplay: function() {
                //Call base class to init menu, etc
                this.initUI();

                //I've been calling back to this display with the following
                //this returns "getRamaddaDisplay('" + this.getId() +"')";
                var get = this.getGet();
                var html =  "<p>";
                html +=HtmlUtil.onClick(get +".click();", HtmlUtil.div(["id", this.getDomId(ID_CLICK)], "Click me"));
                html +=  "<p>";
                html += HtmlUtil.div(["id", this.getDomId(ID_DATA)], "");

                //Set the contents
                this.setContents(html);

                //Add the data
                this.updateUI();
            },
            //this tells the base display class to loadInitialData
            needsData: function() {
                return true;
            },
            //this gets called after the data has been loaded
            updateUI: function() {
                var pointData = this.getData();
                if(pointData == null) return;
                var recordFields = pointData.getRecordFields();
                var records = pointData.getRecords();
                var html = "";
                html += "#records:" + records.length;
                $("#" + this.getDomId(ID_DATA)).html(html);
            },
            //this gets called when an event source has selected a record
            handleEventRecordSelection: function(source, args) {
                //args: index, record, html
                //this.setContents(args.html);
            },
            click: function() {
                $("#"+this.getDomId(ID_CLICK)).html("Click again");
            }
        });
}


//Add this type to the global list
addGlobalDisplayType({type: "example", label:"Example"});