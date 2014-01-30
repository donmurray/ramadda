/**
Copyright 2008-2014 Geode Systems LLC
*/



/*
  This gets created by the displayManager.createDisplay('example')
 */
function RamaddaExampleDisplay(displayManager, id, properties) {
    //Create the base class
    $.extend(this, new RamaddaDisplay(displayManager, id, "example", properties));

    //Add this display to the list of global displays
    addRamaddaDisplay(this);

    //Define my methods
    $.extend(this, {
            //gets called by displaymanager after the displays are layed out
            initDisplay: function() {
                this.checkFixedLayout();
                this.initMenu();
                this.setInnerContents("<p>the example html display<p>");
            },
           //I've been calling back to this display with the following
            var get = "getRamaddaDisplay('" + this.id +"')";

                html+=  "&nbsp;&nbsp;";
                html+=  htmlUtil.onClick(get +".setIndex(0);", htmlUtil.image(this.iconBegin,["title","beginning", "class", "display-animation-button", "xwidth","32"]));

            handleRecordSelection: function(source, index, record, html) {
                //this gets called when an event source has selected a record
                //this.setInnerContents(html);
            },
        });
}

