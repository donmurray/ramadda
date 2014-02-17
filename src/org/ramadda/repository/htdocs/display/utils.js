/**
Copyright 2008-2014 Geode Systems LLC
*/



var ID_AREA_LINK = "arealink";
var ID_NORTH = "north";
var ID_SOUTH = "south";
var ID_EAST = "east";
var ID_WEST = "west";


function AreaForm(display) {
    RamaddaUtil.inherit(this, {
            display:display,
            getHtml: function() {
                var callback = this.display.getGet();
                var link = HtmlUtil.onClick(callback+".areaForm.areaLinkClick();", HtmlUtil.image(root +"/icons/link_break.png",["title","Set bounds from map", "class", "display-area-link", "border","0","id", this.display.getDomId(ID_AREA_LINK)]));
                var erase = HtmlUtil.onClick(callback+".areaForm.areaClear();", HtmlUtil.image(root +"/icons/eraser.png",["title","Clear form", "class", "display-area-link", "border","0"]));
                var areaForm = HtmlUtil.openTag("table",["class","display-area", "border","0","cellpadding","0","cellspacing","0"]);
                areaForm += HtmlUtil.tr([],
                                        HtmlUtil.td(["align","center","class"],
                                                    HtmlUtil.input(ID_NORTH,"",["placeholder","N","class","input display-area-input", "size", "5","id", 
                                                                                this.display.getDomId(ID_NORTH),  "title","North"])));

                areaForm += HtmlUtil.tr([],HtmlUtil.td([],
                                                       HtmlUtil.input(ID_WEST,"",["placeholder","W","class","input  display-area-input", "size", "5","id", 
                                                                                  this.display.getDomId(ID_WEST),  "title","West"]) +
                                                       HtmlUtil.input(ID_EAST,"",["placeholder","E","class","input  display-area-input", "size", "5","id", 
                                                                                  this.display.getDomId(ID_EAST),  "title","East"])));
                areaForm += HtmlUtil.tr([],HtmlUtil.td(["align","center"],
                                                       HtmlUtil.input(ID_SOUTH,"",["placeholder","S","class","input  display-area-input", "size", "5","id", 
                                                                                   this.display.getDomId(ID_SOUTH),  "title","South"])));

                areaForm += HtmlUtil.closeTag("table");
                return   HtmlUtil.hbox(areaForm, link, erase);
            },
            areaClear: function() {
                $("#" + this.display.getDomId(ID_NORTH)).val("");
                $("#" + this.display.getDomId(ID_WEST)).val("");
                $("#" + this.display.getDomId(ID_SOUTH)).val("");
                $("#" + this.display.getDomId(ID_EAST)).val("");
            },
            areaLinkClick: function() {
                this.linkArea = !this.linkArea;
                var image = root +( this.linkArea? "/icons/link.png":"/icons/link_break.png");
                $("#" + this.display.getDomId(ID_AREA_LINK)).attr("src", image);
                if(this.linkArea && this.lastBounds) {
                    var b  = this.lastBounds;
                    $("#" + this.display.getDomId(ID_NORTH)).val(formatLocationValue(b.top));
                    $("#" + this.display.getDomId(ID_WEST)).val(formatLocationValue(b.left));
                    $("#" + this.display.getDomId(ID_SOUTH)).val(formatLocationValue(b.bottom));
                    $("#" + this.display.getDomId(ID_EAST)).val(formatLocationValue(b.right));
                }
            },
           linkArea: false,
           lastBounds: null,
           handleEventMapBoundsChanged: function (source,  bounds) {
                this.lastBounds = bounds;
                if(!this.linkArea) return;
                $("#" + this.display.getDomId(ID_NORTH)).val(formatLocationValue(bounds.top));
                $("#" + this.display.getDomId(ID_WEST)).val(formatLocationValue(bounds.left));
                $("#" + this.display.getDomId(ID_SOUTH)).val(formatLocationValue(bounds.bottom));
                $("#" + this.display.getDomId(ID_EAST)).val(formatLocationValue(bounds.right));
            },
            setAreaSettings: function(settings) {
                settings.setBounds(this.display.getFieldValue(this.display.getDomId(ID_NORTH), null),
                                   this.display.getFieldValue(this.display.getDomId(ID_WEST), null),
                                   this.display.getFieldValue(this.display.getDomId(ID_SOUTH), null),
                                   this.display.getFieldValue(this.display.getDomId(ID_EAST), null));
            },
                });
}

