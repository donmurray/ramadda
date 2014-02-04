/**
Copyright 2008-2014 Geode Systems LLC
*/


var DISPLAY_FILTER = "filter";
var DISPLAY_ANIMATION = "animation";

addGlobalDisplayType({type:DISPLAY_FILTER , label: "Filter"});
addGlobalDisplayType({type:DISPLAY_ANIMATION , label: "Animation"});

function RamaddaFilterDisplay(displayManager, id, properties) {
    $.extend(this, new RamaddaDisplay(displayManager, id, properties));
    addRamaddaDisplay(this);
    $.extend(this, {
            html: "<p>&nbsp;&nbsp;&nbsp;Nothing selected&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;<p>",
            initDisplay: function() {
                this.initUI();
                this.setContents(this.html);
            },
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


