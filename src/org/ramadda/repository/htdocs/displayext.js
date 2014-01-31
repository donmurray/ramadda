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

    //Create the base class
    $.extend(this, new RamaddaDisplay(displayManager, id, "example", properties));

    //Add this display to the list of global displays
    addRamaddaDisplay(this);

    //Define my methods
    $.extend(this, {

            //gets called by displaymanager after the displays are layed out
            initDisplay: function() {
                //Call base class to init menu, etc
                this.initUI();

                //I've been calling back to this display with the following
                var get = "getRamaddaDisplay('" + this.id +"')";
                var html =  "<p>";
                html +=htmlUtil.onClick(get +".click();", htmlUtil.div(["id", this.getDomId(ID_CLICK)], "Click me"));
                html +=  "<p>";

                //Set the contents
                this.setContents(html);
            },
            click: function() {
                $("#"+this.getDomId(ID_CLICK)).html("Click again");
            },
            //this gets called when an event source has selected a record
            handleRecordSelection: function(source, index, record, html) {
                //this.setContents(html);
            },
        });
}

