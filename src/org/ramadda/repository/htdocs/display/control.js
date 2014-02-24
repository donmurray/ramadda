/**
Copyright 2008-2014 Geode Systems LLC
*/


var DISPLAY_FILTER = "filter";
var DISPLAY_ANIMATION = "animation";

addGlobalDisplayType({type:DISPLAY_FILTER , label: "Filter",requiresData:false});
addGlobalDisplayType({type:DISPLAY_ANIMATION , label: "Animation",requiresData:false});

function RamaddaFilterDisplay(displayManager, id, properties) {
    RamaddaUtil.inherit(this, new RamaddaDisplay(displayManager, id, properties));
    addRamaddaDisplay(this);
    RamaddaUtil.defineMembers(this, {
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
    RamaddaUtil.inherit(this, new RamaddaDisplay(displayManager, id, DISPLAY_ANIMATION, properties));
    addRamaddaDisplay(this);
    RamaddaUtil.defineMembers(this, {
            running: false,
            timestamp: 0,
            index: 0,              
            sleepTime: 500,
            iconStart: ramaddaBaseUrl+"/icons/display/control.png",
            iconStop: ramaddaBaseUrl+"/icons/display/control-stop-square.png",
            iconBack: ramaddaBaseUrl+"/icons/display/control-stop-180.png",
            iconForward: ramaddaBaseUrl+"/icons/display/control-stop.png",
            iconFaster: ramaddaBaseUrl+"/icons/display/plus.png",
            iconSlower: ramaddaBaseUrl+"/icons/display/minus.png",
            iconBegin: ramaddaBaseUrl+"/icons/display/control-double-180.png",
            iconEnd: ramaddaBaseUrl+"/icons/display/control-double.png",
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
                    label += HtmlUtil.b("Date:") + " "  + record.getDate();
                } else {
                    label += HtmlUtil.b("Index:") +" " + this.index;
                }
                $("#" + this.getDomId(ID_TIME)).html(label);
                this.displayManager.handleEventRecordSelection(this, null, this.index);
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

                var get = this.getGet();
                var html =  "";
                html+=  HtmlUtil.onClick(get +".setIndex(0);", HtmlUtil.image(this.iconBegin,[ATTR_TITLE,"beginning", ATTR_CLASS, "display-animation-button", "xwidth","32"]));
                html +="  ";
                html+=  HtmlUtil.onClick(get +".deltaIndex(-1);", HtmlUtil.image(this.iconBack,[ATTR_TITLE,"back 1", ATTR_CLASS, "display-animation-button", "xwidth","32"]));
                html +="  ";
                html+=  HtmlUtil.onClick(get +".toggle();", HtmlUtil.image(this.iconStart,[ATTR_TITLE,"play/stop", ATTR_CLASS, "display-animation-button", "xwidth","32", ATTR_ID, this.getDomId(ID_START)]));
                html +="  ";
                html+=  HtmlUtil.onClick(get +".deltaIndex(1);", HtmlUtil.image(this.iconForward,[ATTR_TITLE,"forward 1", ATTR_CLASS, "display-animation-button", "xwidth","32"]));
                html +="  ";
                html+=  HtmlUtil.onClick(get +".faster();", HtmlUtil.image(this.iconFaster,[ATTR_CLASS, "display-animation-button", ATTR_TITLE,"faster", "xwidth","32"]));
                html +="  ";
                html+=  HtmlUtil.onClick(get +".slower();", HtmlUtil.image(this.iconSlower,[ATTR_CLASS, "display-animation-button", ATTR_TITLE,"slower", "xwidth","32"]));
                html+=  HtmlUtil.div([ATTR_ID, this.getDomId(ID_TIME)],"&nbsp;");
                this.setTitle("Animation");
                this.setContents(html);
            },
        });
}


