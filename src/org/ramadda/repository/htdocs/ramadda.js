/**
* Copyright 2008-2013 Geode Systems LLC
*
* Permission is hereby granted, free of charge, to any person obtaining a copy of this 
* software and associated documentation files (the "Software"), to deal in the Software 
* without restriction, including without limitation the rights to use, copy, modify, 
* merge, publish, distribute, sublicense, and/or sell copies of the Software, and to 
* permit persons to whom the Software is furnished to do so, subject to the following conditions:
* 
* The above copyright notice and this permission notice shall be included in all copies 
* or substantial portions of the Software.
* 
* THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, 
* INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR 
* PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE 
* FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR 
* OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER 
* DEALINGS IN THE SOFTWARE.
*/

var ramaddaBaseUrl = "${urlroot}";
var root = ramaddaBaseUrl;
var urlroot = ramaddaBaseUrl;
var icon_close = ramaddaBaseUrl +"/icons/close.gif";
var icon_rightarrow = ramaddaBaseUrl +"/icons/grayrightarrow.gif";
var icon_downdart =ramaddaBaseUrl +"/icons/downdart.gif";
var icon_rightdart =ramaddaBaseUrl +"/icons/rightdart.gif";
var icon_downdart =ramaddaBaseUrl +"/icons/application_side_contract.png";
var icon_rightdart =ramaddaBaseUrl +"/icons/application_side_expand.png";
var icon_progress = ramaddaBaseUrl +"/icons/progress.gif";
var icon_information = ramaddaBaseUrl +"/icons/information.png";
var icon_folderclosed = ramaddaBaseUrl +"/icons/folderclosed.png";
var icon_folderopen = ramaddaBaseUrl +"/icons/togglearrowdown.gif";
var icon_menuarrow = ramaddaBaseUrl +"/icons/downdart.gif";
var icon_blank = ramaddaBaseUrl +"/icons/blank.gif";
var uniqueCnt = 0;

var GuiUtils = {
    loadXML: function (url, callback,arg) {
        var req = false;
        if(window.XMLHttpRequest) {
            try {
                req = new XMLHttpRequest();
            } catch(e) {
                req = false;
            }
        } else if(window.ActiveXObject)  {
            try {
                req = new ActiveXObject("Msxml2.XMLHTTP");
            } catch(e) {
                try {
                    req = new ActiveXObject("Microsoft.XMLHTTP");
                } catch(e) {
                    req = false;
                }
            }
        }
        if(req) {
            req.onreadystatechange = function () { 
                if (req.readyState == 4 && req.status == 200)   {
                    callback(req,arg); 
                }
            };
            req.open("GET", url, true);
            req.send("");
        }
    },
    loadUrl: function (url, callback,arg) {
        var req = false;
        if(window.XMLHttpRequest) {
            try {
                req = new XMLHttpRequest();
            } catch(e) {
                req = false;
            }
        } else if(window.ActiveXObject)  {
            try {
                req = new ActiveXObject("Msxml2.XMLHTTP");
            } catch(e) {
                try {
                    req = new ActiveXObject("Microsoft.XMLHTTP");
                } catch(e) {
                    req = false;
                }
            }
        }
        if(req) {
            req.onreadystatechange = function () { 
                if (req.readyState == 4 && req.status == 200)   {
                    callback(req,arg); 
                }
            };
            req.open("GET", url, true);
            req.send("");
        }
    },
    getUrlArg: function( name, dflt ) {
        name = name.replace(/[\[]/,"\\\[").replace(/[\]]/,"\\\]");
        var regexS = "[\\?&]"+name+"=([^&#]*)";
        var regex = new RegExp( regexS );
        var results = regex.exec( window.location.href );
        if( results == null || results=="" )
            return dflt;
        else
            return results[1];
    },
    setCursor: function(c) {
        var cursor = document.cursor;
        if(!cursor && document.getElementById) {
            cursor =  document.getElementById('cursor');
        }
        if(!cursor) {
            document.body.style.cursor = c;
        }
    },
    getDomObject : function(name) {
        obj = new DomObject(name);
        if(obj.obj) return obj;
        return null;
    },
    getEvent: function (event) {
        if(event) return event;
        return window.event;
    },
    getEventX:    function (event) {
        if (event.pageX) {
            return  event.pageX;
        }
        return  event.clientX + document.body.scrollLeft
        + document.documentElement.scrollLeft;
    },
    getEventY :function (event) {
        if (event.pageY) {
            return  event.pageY;
        }
        return  event.clientY + document.body.scrollTop
        + document.documentElement.scrollTop;

    },

    getTop : function (obj) {
        if(!obj) return 0;
        return obj.offsetTop+this.getTop(obj.offsetParent);
    },


    getBottom : function (obj) {
        if(!obj) return 0;
        return this.getTop(obj) + obj.offsetHeight;
    },


    setPosition : function(obj,x,y) {
        obj.style.top = y;
        obj.style.left = x;
    },

    getLeft :  function(obj) {
        if(!obj) return 0;
        return obj.offsetLeft+this.getLeft(obj.offsetParent);
    },
    getRight :  function(obj) {
        if(!obj) return 0;
        return obj.offsetRight+this.getRight(obj.offsetParent);
    },

    getStyle : function(obj) {
        if(obj.style) return obj.style;
        if (document.layers)  { 		
            return   document.layers[obj.name];
        }        
        return null;
    },
    //from http://snipplr.com/view.php?codeview&id=5949
    size_format: function (filesize) {
        if (filesize >= 1073741824) {
            filesize = number_format(filesize / 1073741824, 2, '.', '') + ' Gb';
        } else { 
            if (filesize >= 1048576) {
                filesize = number_format(filesize / 1048576, 2, '.', '') + ' Mb';
            } else { 
                if (filesize >= 1024) {
                    filesize = number_format(filesize / 1024, 0) + ' Kb';
                } else {
                    filesize = number_format(filesize, 0) + ' bytes';
                };
            };
        };
        return filesize;
    },
    inputLengthOk: function(domId, length) {
        var value = $("#"+ domId).val();
        if(value == null) return true;
        if(value.length>length) {
            closeFormLoadingDialog ();
            return false;
        }
        return true;
    },
    inputValueOk: function (domId, rangeValue, min) {
        var value = $("#"+ domId).val();
        if(value == null) return true;
        if(min && value<rangeValue) {
            closeFormLoadingDialog ();
            return false;
        }
        if(!min && value>rangeValue) {
            closeFormLoadingDialog ();
            return false;
        }
        return true;
    }


};

var TAG_B = "b";
var TAG_DIV = "div";
var TAG_LI = "li";
var TAG_TABLE = "table";
var TAG_TR = "tr";
var TAG_TD = "td";
var TAG_UL= "ul";

var ATTR_ID = "id";
var ATTR_CLASS = "class";

var HtmlUtil =  {
    join : function (items,separator) {
        var html = "";
        for(var i =0;i<items.length;i++) {
            if(i>0 & separator!=null) html+= separator;
            html += items[i];
        }
        return html;
    },
    qt : function (value) {
        return "\"" + value +"\"";
    },
    hbox : function() {
        var row = HtmlUtil.openTag("tr",["valign","top"]);
        row += "<td>";
        row += HtmlUtil.join(arguments,"</td><td>");
        row += "</td></tr>";
        return this.tag("table",["border","0", "cellspacing","0","cellpadding","0"],
                        row);
    },

    leftRight : function(left,right,leftWidth, rightWidth) {
        if(leftWidth==null) leftWidth = "50%";
        if(rightWidth==null) rightWidth = "50%";
        return this.tag("table",["border","0", "width","100%","cellspacing","0","cellpadding","0"],
                        this.tr(["valign","top"],
                                this.td(["align","left","width",leftWidth],left) +
                                this.td(["align","right","width", rightWidth],right)));
    },
    leftCenterRight : function(left,center, right,leftWidth, centerWidth, rightWidth) {
        if(leftWidth==null) leftWidth = "33%";
        if(centerWidth==null) centerWidth = "33%";
        if(rightWidth==null) rightWidth = "33%";

        return this.tag("table",["border","0", "width","100%","cellspacing","0","cellpadding","0"],
                        this.tr(["valign","top"],
                                this.td(["align","center","width",leftWidth],left) +
                                this.td(["align","center","width",centerWidth],center) +
                                this.td(["align","center","width", rightWidth],right)));
    },

    div : function(attrs, inner) {
        return this.tag("div", attrs, inner);
    },
    span : function(attrs, inner) {
        return this.tag("span", attrs, inner);
    },
    image : function(path, attrs) {
        return  "<img " + this.attrs(["src", path,"border","0"]) +" " + this.attrs(attrs) +">";
    },
    tr : function(attrs, inner) {
        return this.tag("tr", attrs, inner);
    },
    td : function(attrs, inner) {
        return this.tag("td", attrs, inner);
    },
    th : function(attrs, inner) {
        return this.tag("th", attrs, inner);
    },
    formTable : function() {
        return  this.openTag("table",["class","formtable","cellspacing","0","cellspacing","0"]);
    },
    formEntryTop : function(label, value) {
        return this.tag("tr", ["valign","top"],
                        this.tag("td",["class","formlabel","align","right"],
                                 label) +
                        this.tag("td",[],
                                 value));

    },
    formEntry : function(label, value) {
        return this.tag("tr", [],
                        this.tag("td",["class","formlabel","align","right"],
                                 label) +
                        this.tag("td",[],
                                 value));

    },

    b : function(inner) {
        return this.tag("b", [], inner);
    },

    td : function(attrs, inner) {
        return this.tag("td", attrs, inner);
    },

    tag : function(tagName, attrs, inner) {
        var html = "<" + tagName +" " + this.attrs(attrs) +">";
        if(inner!=null) {
            html += inner;
        }
        html += "</" + tagName +">\n";
        return html;
    },
    openTag : function(tagName, attrs) {
        var html = "<" + tagName +" " + this.attrs(attrs) +">";
        return html;
    },

    closeTag : function(tagName) {
        return  "</" + tagName +">\n";
    },

    attr : function(name, value) {
        return " " + name +"=" + this.qt(value) +" ";
    },

    attrs : function(list) {
        var html = "";
        if(list == null) return html;
        for(var i=0;i<list.length;i+=2) {
            var name = list[i];
            var value = list[i+1];
            if(value == null) {
                html += name;
            } else {
                html += this.attr(name,value);
            }

        }
        return html;
    },
    styleAttr : function(s) {
        return this.attr("style", s);
    },

    classAttr : function(s) {
        return this.attr("class", s);
    },

    idAttr : function(s) {
        return this.attr("id", s);
    },
    href: function(url, label) {
        return this.tag("a", ["href", url], label);
    },

    onClick : function(call, html, attrs) {
        var myAttrs = ["onclick", call, "style","color:black;"];
        if(attrs!=null) {
            for(var i=0;i<attrs.length;i++) {
                myAttrs.push(attrs[i]);
            }
        }
        return this.tag("a", myAttrs, html);
    },

    checkbox:  function(id,cbxclass,checked) {
        var html = "<input id=\"" + id +"\" class=\""  + cbxclass +"\"  type=checkbox value=true ";
        if(checked) {
            html+= " checked ";
        }
        html += "/>";
        return html;
    },

    input :   function(name, value, attrs) {
        return "<input " + HtmlUtil.attrs(attrs) + HtmlUtil.attrs(["name", name, "value",value]) +"/>";
    },


    squote: function(s) {return "'" + s +"'";},
    toggleBlock: function(label, contents, visible) {
        var id = "block_" + (uniqueCnt++);
        var imgid = id +"_img";

        var img1=ramaddaBaseUrl + "/icons/togglearrowdown.gif";
        var img2=ramaddaBaseUrl + "/icons/togglearrowright.gif";
        var args = HtmlUtil.join([HtmlUtil.squote(id),HtmlUtil.squote(imgid), HtmlUtil.squote(img1), HtmlUtil.squote(img2)],",");
        var click = "toggleBlockVisibility(" + args +");";

        var header = HtmlUtil.div(["class","entry-toggleblock-label","onClick", click],
                                  HtmlUtil.image((visible?img1:img2),  ["align","bottom","id",imgid]) +
                                  " " + label);
        var style = (visible?"display:block;visibility:visible":"display:none;");
        var body = HtmlUtil.div(["class","hideshowblock", "id",id, "style", style],
                                contents);
        return header + body;
    }
}







var blockCnt=0;
function DomObject(name) {
    this.obj = null;
    // DOM level 1 browsers: IE 5+, NN 6+
    if (document.getElementById)	{    	
        this.obj = document.getElementById(name);
        if(this.obj) 
            this.style = this.obj.style;
    }
    // IE 4
    else if (document.all)	{  			
        this.obj = document.all[name];
        if(this.obj) 
            this.style = this.obj.style;
    }
    // NN 4
    else if (document.layers)  { 		
        this.obj = document.layers[name];
        this.style = document.layers[name];
    }
   if(this.obj) {
      this.id = this.obj.id;
      if(!this.id) {
	this.id = "obj"+ (blockCnt++);
      }
   } 
   
}



function noop() {
}



var popupObject;
var popupSrcId;
var popupTime;

document.onmousemove = mouseMove;
document.onmousedown = mouseDown;
document.onmouseup   = mouseUp;


var mouseIsDown = 0;
var dragSource;
var draggedEntry;
var draggedEntryName;
var draggedEntryIcon;
var mouseMoveCnt =0;
var objectToHide;

function hidePopupObject() {
    if(objectToHide!=popupObject) {
        //	return;
    }
    if(popupObject) {
        hideObject(popupObject);
        popupObject = null;
        popupSrcId = null;
    }
}



function mouseDown(event) {
    if(popupObject) {
        if(checkToHidePopup()) {
            theObjectToHide = popupObject;
            thePopupSrcId  = popupSrcId;
            var callback = function() {
                var shouldClear = (popupObject == theObjectToHide);
                hideObject(theObjectToHide);
                if(shouldClear) {
                    popupSrcId = null;
                    popupObject = null;
                }
            }
            setTimeout(callback,250);
        }
    }
    event = GuiUtils.getEvent(event);
    mouseIsDown = 1;
    mouseMoveCnt =0;
    return true;
}



function mouseUp(event) {
    event = GuiUtils.getEvent(event);
    mouseIsDown = 0;
    draggedEntry   = null;
    GuiUtils.setCursor('default');
    var obj = GuiUtils.getDomObject('ramadda-floatdiv');
    if(obj) {
        var dragSourceObj= GuiUtils.getDomObject(dragSource);
        if(dragSourceObj) {
            var tox = GuiUtils.getLeft(dragSourceObj.obj);
            var toy = GuiUtils.getTop(dragSourceObj.obj);
            var fromx = parseInt(obj.style.left);
            var fromy = parseInt(obj.style.top);
            var steps = 10;
            var dx=(tox-fromx)/steps;
            var dy=(toy-fromy)/steps;
            flyBackAndHide('ramadda-floatdiv',0,steps,fromx,fromy,dx,dy);
        } else {
            hideObject(obj);
        }
    }
    return true;
}




function flyBackAndHide(id, step,steps,fromx,fromy,dx,dy) {
    var obj = GuiUtils.getDomObject(id);
    if(!obj) {
        return;
    }
    step=step+1;
    obj.style.left = fromx+dx*step+"px";
    obj.style.top = fromy+dy*step+"px";
    var opacity = 80*(steps-step)/steps;
    //    GuiUtils.print(opacity);
    //    obj.style.filter="alpha(opacity="+opacity+")";
    //    obj.style.opacity="0." + opacity;

    if(step<steps) {
        var callback = "flyBackAndHide('" + id +"'," + step+","+steps+","+fromx+","+fromy+","+dx+","+dy+");"
        setTimeout(callback,30);
    } else {
        setTimeout("finalHide('" + id+"')",150);
        //        hideObject(obj);
    }
}

function finalHide(id) {
    var obj = GuiUtils.getDomObject(id);
    if(!obj) {
        return;
    }
    hideObject(obj);
    obj.style.filter="alpha(opacity=80)";
    obj.style.opacity="0.8";
}

function mouseMove(event) {
    event = GuiUtils.getEvent(event);
    if(draggedEntry && mouseIsDown) {
        mouseMoveCnt++;
        var obj = GuiUtils.getDomObject('ramadda-floatdiv');
        if(mouseMoveCnt==6) {
            GuiUtils.setCursor('move');
        }
        if(mouseMoveCnt>=6&& obj) {
            moveFloatDiv(GuiUtils.getEventX(event),GuiUtils.getEventY(event));
        }
    }    
    return false;
}



function moveFloatDiv(x,y) {
    var obj = GuiUtils.getDomObject('ramadda-floatdiv');
    if(obj) {
        if(obj.style.visibility!="visible") {
            obj.style.visibility = "visible";
            obj.style.display = "block";
            var icon = "";
            if(draggedEntryIcon) {
                icon = "<img src=\"" +draggedEntryIcon+"\"/> ";
            }
            obj.obj.innerHTML = icon +draggedEntryName+"<br>Drag to a group to copy/move/associate";
        }
        obj.style.top = y;
        obj.style.left = x+10;
    }
}


function mouseOverOnEntry(event, entryId, targetId) {
    event = GuiUtils.getEvent(event);
    if(entryId == draggedEntry) return;
    if(mouseIsDown)  {
        var obj = GuiUtils.getDomObject(targetId);
        if(!obj)  return;
        //       if(obj.style && obj.style.borderBottom) {
        obj.style.borderBottom="2px black solid";
        //        }
    }
}

function mouseOutOnEntry(event, entryId,targetId) {
    event = GuiUtils.getEvent(event);
    if(entryId == draggedEntry) return;
    var obj = GuiUtils.getDomObject(targetId);
    if(!obj)  return;
    if(mouseIsDown)  {
        obj.style.borderBottom="";
    }
}




function mouseDownOnEntry(event, entryId, name, sourceIconId, icon) {
    event = GuiUtils.getEvent(event);
    dragSource  = sourceIconId;
    draggedEntry = entryId;
    draggedEntryName=name;
    draggedEntryIcon = icon;
    mouseIsDown = 1;
    if(event.preventDefault) {
        event.preventDefault();
    } else {
	event.returnValue = false;
        return false;
    }
}


function mouseUpOnEntry(event, entryId, targetId) {
    event = GuiUtils.getEvent(event);
    if(entryId == draggedEntry) {
        return;
    }
    var obj = GuiUtils.getDomObject(targetId);
    if(!obj)  {
        return;
    }
    if(mouseIsDown)  {
        obj.style.borderBottom="";
    }
    if(draggedEntry && draggedEntry!=entryId) {
        /*
        $("#ramadda-dialog").html("what to do....");
        $("#ramadda-dialog").dialog({
                resizable: false,
                modal: true,
                buttons: {
                   Cancel: function() {$( this ).dialog( "close" );}
                }}
        );
        */
        url = ramaddaBaseUrl +"/entry/copy?action=action.move&from=" + draggedEntry +"&to=" + entryId;
        //        alert(url);
        //        window.open(url,'move window','') ;
        document.location = url;
    }
}


function getTooltip() {
    return $("#ramadda-tooltipdiv");
}

function handleKeyPress(event) {
    getTooltip().hide();
}


document.onkeypress = handleKeyPress;

var groups = new Array();
var groupList = new Array();




function EntryFormList(formId,img,selectId, initialOn) {

    this.entryRows = new Array();
    this.lastEntryRowClicked=null;
    groups[formId] = this;
    groupList[groupList.length] = this;
    this.formId = formId;
    this.toggleImg  = img;
    this.on = initialOn;
    this.entries = new Array();

    this.groupAddEntry = function(entryId) {
        this.entries[this.entries.length] = entryId;
    }

    this.addEntryRow = function(entryRow) {
        this.groupAddEntry(entryRow.cbxWrapperId);
        this.entryRows[this.entryRows.length] = entryRow;
        if(!this.on) {
            entryRow.getCbx().hide();
        } else {
            entryRow.getCbx().show();
	}
    }

    this.groupAddEntry(selectId);
    if(!this.on) {
        hideObject(selectId);
    }

    this.groupToggleVisibility = function  () {
        this.on = !this.on;
        this.setVisibility();
    }


    this.findEntryRow =function(rowId) {
        var i;
        for (i = 0; i < this.entryRows.length; i++) {
            if(this.entryRows[i].rowId == rowId) {
                return  this.entryRows[i];
            }
        }
        return null;
    }



    this.checkboxClicked = function(event,cbxId) {
        if(!event) return;
        var entryRow;
        for (i = 0; i < this.entryRows.length; i++) {
            if(this.entryRows[i].cbxId ==cbxId) {
                entryRow = this.entryRows[i];
                break;
            }
        }

        if(!entryRow) return;


        var value = entryRow.isSelected();
        if(event.ctrlKey) {
            for (i = 0; i < this.entryRows.length; i++) {
                this.entryRows[i].setCheckbox(value);
            }
        }

        if(event.shiftKey) {
            if(this.lastEntryRowClicked) {
                var pos1 = this.lastEntryRowClicked.getCbx().offset().top;
                var pos2 = entryRow.getCbx().offset().top;
                if(pos1>pos2) {
		    var tmp = pos1;
		    pos1 =pos2;
		    pos2=tmp;
                }
                for (i = 0; i < this.entryRows.length; i++) {
                    var top = this.entryRows[i].getCbx().offset().top;
                    if(top>=pos1 && top<=pos2) {
                        this.entryRows[i].setCheckbox(value);
                    }
                }
            }
            return;
        }
        this.lastEntryRowClicked = entryRow;
    }

    this.setVisibility = function  () {
        if(this.toggleImg) {
            if(this.on) {
                $("#" + this.toggleImg).attr('src',  icon_downdart);
            } else {
                $("#" + this.toggleImg).attr('src',  icon_rightdart);
            }
        }

        var form = GuiUtils.getDomObject(this.formId);
        if(form) {
            form = form.obj;
            for(i=0;i<form.elements.length;i++) { 
                if(this.on) {
                    showObject(form.elements[i],"inline");
                } else {
                    hideObject(form.elements[i]);
                }
            }
        }

        for(i=0;i<this.entries.length;i++) {
            obj = GuiUtils.getDomObject(this.entries[i]);
            if(!obj) continue;
            if(this.on) {
                showObject(obj,"block");
            } else {
                hideObject(obj);
            }
        }
    }
}


function entryRowCheckboxClicked(event,cbxId) {

    var cbx = GuiUtils.getDomObject(cbxId);
    if(!cbx) return;
    cbx = cbx.obj;
    if(!cbx.form) return;
    var visibilityGroup = groups[cbx.form.id];
    if(visibilityGroup) {
        visibilityGroup.checkboxClicked(event,cbxId);
    }
}

function initEntryListForm(formId) {
    var visibilityGroup = groups[formId];
    if(visibilityGroup) {
        visibilityGroup.on = 0;
        visibilityGroup.setVisbility();
    }
}


function EntryRow (entryId, rowId, cbxId,cbxWrapperId, showDetails) {
    this.entryId = entryId;

    this.onColor = "#FFFFCC";
    this.overColor = "#f6f6f6";
    this.overColor = "#edf5ff";
    this.overColor = "#ffffee";
    this.overColor = "#f4f4f4";
    this.rowId = rowId;
    this.cbxId = cbxId;
    this.cbxWrapperId = cbxWrapperId;
    this.showDetails = showDetails;
    this.getRow = function() {
        return $("#" + this.rowId);
    }

    this.getCbx = function() {
        return $("#" + this.cbxId);
    }

    var form = this.getCbx().closest('form');
    if(form.size()) {
        var visibilityGroup = groups[form.attr('id')];
        if(visibilityGroup) {
            visibilityGroup.addEntryRow(this);
        }
    } else {
        this.getCbx().hide();
    }


    this.setCheckbox = function(value) {
        this.getCbx().attr('checked', value);
        this.setRowColor();
    }

    
    this.isSelected = function() {
        return this.getCbx().attr('checked');
    }

    this.setRowColor = function() {
        if(this.isSelected()) {
            this.getRow().css("background-color", this.onColor);
        } else {
            this.getRow().css("background-color", "#ffffff");
        }
    }


    this.mouseOver = function(event) {
        $("#" + "entrymenuarrow_" +rowId).attr('src',icon_menuarrow);
        this.getRow().css('background-color',  this.overColor);
    }

    this.mouseOut = function(event) {
        $("#entrymenuarrow_" +rowId).attr('src',icon_blank);
        this.setRowColor();
    }


    this.mouseClick = function(event) {
        eventX = GuiUtils.getEventX(event);
        var position = this.getRow().offset();
        //Don't pick up clicks on the left side
        if(eventX-position.left<150) return;
        this.lastClick = eventX;
        var url = ramaddaBaseUrl +"/entry/show?entryid=" + entryId +"&output=metadataxml";
        if(this.showDetails) {
            url+="&details=true";
        } else {
            url+="&details=false";
        }
	GuiUtils.loadXML( url, this.handleTooltip,this);
    }

    this.handleTooltip = function(request,entryRow) {
        var xmlDoc=request.responseXML.documentElement;
        text = getChildText(xmlDoc);
        var leftSide  = entryRow.getRow().offset().left;
        var offset = entryRow.lastClick-leftSide;
        getTooltip().html("<div class=tooltip-inner><div id=\"tooltipwrapper\" ><table><tr valign=top><img width=\"16\" onmousedown=\"hideEntryPopup();\" id=\"tooltipclose\"  src=" + icon_close +"></td><td>" + text+"</table></div></div>");
        checkTabs(text);

        var pos = entryRow.getRow().offset();    
        var eWidth = entryRow.getRow().outerWidth();
        var eHeight = entryRow.getRow().outerHeight();
        var mWidth = getTooltip().outerWidth();
        var left =  entryRow.lastClick + "px";
        var top = (3+pos.top+eHeight) + "px";
        //show the menu directly over the placeholder  
        getTooltip().css( { 
                position: 'absolute',
                    zIndex: 5000,
                    left: left, 
                    top: top
                    } );
        getTooltip().show();
    }



}


function checkTabs(html) {
    while(1) {
        var re = new RegExp("id=\"(tabId[^\"]+)\"");
        var m = re.exec(html);
        if(!m) {
            break;
        }
        var s =   m[1];
        if(s.indexOf("-")<0) {
            jQuery(function(){
                    jQuery('#'+ s).tabs();
                });
        }
        var idx = html.indexOf("id=\"tabId");
        if(idx<0) {
            break;
        }
        html = html.substring(idx+20);
    }
}


function hideEntryPopup() {
    getTooltip().hide();
}

function findEntryRow(rowId) {
    var idx;
    for(idx=0;idx<groupList.length;idx++) {
        var entryRow = groupList[idx].findEntryRow(rowId);
        if(entryRow) return entryRow;
    }
    return null;
}


function entryRowOver(rowId) {
    var entryRow = findEntryRow(rowId);
    if(entryRow) entryRow.mouseOver();
}


function entryRowOut(rowId) {
    var entryRow = findEntryRow(rowId);
    if(entryRow) entryRow.mouseOut();
}

function entryRowClick(event,rowId) {
    var entryRow = findEntryRow(rowId);
    if(entryRow) entryRow.mouseClick(event);
}





function indexOf(array,object) {
    for (i = 0; i <= array.length; i++) {
        if(array[i] == object) return i;
    }
    return -1;
}


var lastCbxClicked;

function checkboxClicked(event, cbxPrefix, id) {
    if(!event) return;
    var cbx = GuiUtils.getDomObject(id);
    if(!cbx) return;
    cbx = cbx.obj;

    var checkBoxes = new Array();
    if(!cbx.form) return;
    var elements = cbx.form.elements;
    for(i=0;i<elements.length;i++) {
        if(elements[i].name.indexOf(cbxPrefix)>=0 || elements[i].id.indexOf(cbxPrefix)>=0) {
            checkBoxes[checkBoxes.length] = elements[i];
        }
    }


    var value = cbx.checked;
    if(event.ctrlKey) {
        for (i = 0; i < checkBoxes.length; i++) {
	    checkBoxes[i].checked = value;
        }
    }


    if(event.shiftKey) {
        if(lastCbxClicked) {
	    var pos1 = GuiUtils.getTop(cbx);
	    var pos2 = GuiUtils.getTop(lastCbxClicked);
	    if(pos1>pos2) {
		var tmp = pos1;
		pos1 =pos2;
		pos2=tmp;
	    }
	    for (i = 0; i < checkBoxes.length; i++) {
		var top = GuiUtils.getTop(checkBoxes[i]);
		if(top>=pos1 && top<=pos2) {
	                checkBoxes[i].checked = value;
		}
            }
        }
        return;
    }
    lastCbxClicked = cbx;
}






function toggleBlockVisibility(id, imgid, showimg, hideimg) {
    if(toggleVisibility(id,'block')) {
        $("#"+imgid).attr('src', showimg);
    } else {
        $("#"+imgid).attr('src', hideimg);
    }
    ramaddaUpdateMaps();
}


function toggleInlineVisibility(id, imgid, showimg, hideimg) {
    var img = GuiUtils.getDomObject(imgid);
    if(toggleVisibility(id,'inline')) {
        if(img) img.obj.src = showimg;
    } else {
        if(img) img.obj.src = hideimg;
    }
    ramaddaUpdateMaps();
}







var originalImages = new Array();
var changeImages = new Array();

function folderClick(uid, url, changeImg) {
    changeImages[uid] = changeImg;
    var jqBlock = $("#"+uid);
    if(jqBlock.size() ==0) {
	return;
    }
    var jqImage = $("#img_" +uid);
    var showing = jqBlock.css('display') != "none";
    if(!showing) {
	originalImages[uid] = jqImage.attr('src');
        jqBlock.show();
        jqImage.attr('src', icon_progress);
	GuiUtils.loadXML( url, handleFolderList,uid);
    } else {
	if(changeImg) {
            if(originalImages[uid]) {
                jqImage.attr('src', originalImages[uid]);
            } else 
                jqImage.attr('src', icon_folderclosed);
        }
        jqBlock.hide();
    }
}



function  handleFolderList(request, uid) {
    if(request.responseXML!=null) {
        var xmlDoc=request.responseXML.documentElement;
	var script;
	var html;
	for(i=0;i<xmlDoc.childNodes.length;i++) {
            var childNode = xmlDoc.childNodes[i];
            if(childNode.tagName=="javascript") {
                script =getChildText(childNode);
            } else if(childNode.tagName=="content") {
                html = getChildText(childNode);
            }  else {
            }
	}
        if(!html) {
            html = getChildText(xmlDoc);
        }
	if(html) {
            $("#" + uid).html("<div>"+html+"</div>");
            checkTabs(html);
	}
	if(script) {
            eval(script);
	}
    }
    
    if(changeImages[uid]) {
        $("#img_" +uid).attr('src', icon_folderopen);
    } else {
        $("#img_" +uid).attr('src', originalImages[uid]);
    }
}


var selectors = new Array();

function Selector(event, selectorId, elementId, allEntries, selecttype, localeId) {
    this.id  = selectorId;
    this.elementId  = elementId;
    this.localeId = localeId;
    this.allEntries = allEntries;
    this.selecttype = selecttype;
    this.textComp = GuiUtils.getDomObject(this.elementId);

    this.getTextComponent = function() {
        var id = "#" + this.elementId;
        return $(id);
    }

    this.getHiddenComponent = function() {
        var id = "#" + this.elementId+"_hidden";
        return $(id);
    }

    this.clearInput = function() {	
        this.getHiddenComponent().val("");
        this.getTextComponent().val("");
    }


    this.handleClick = function(event) {
        var srcId = this.id+'_selectlink';
        this.div = GuiUtils.getDomObject('ramadda-selectdiv');
        hidePopupObject();
        $("#ramadda-selectdiv").show();
        $("#ramadda-selectdiv").position({
                of: $( "#" + srcId ),
                my: "left top",
                at: "left bottom",
                collision: "none none"
                });
        url = ramaddaBaseUrl +"/entry/show?output=selectxml&selecttype=" + this.selecttype+"&allentries=" + this.allEntries+"&target=" + this.id+"&noredirect=true&firstclick=true";
        if(localeId) {
            url = url+"&localeid=" + localeId;
        }
        GuiUtils.loadXML( url, handleSelect,this.id);
        return false;
    }
    this.handleClick(event);
}

function selectClick(id,entryId,value) {
    selector = selectors[id];
    if (selector.selecttype=="wikilink") {
        insertAtCursor(selector.textComp.obj,"[[" +entryId+"|"+value+"]]");
    } else if (selector.selecttype=="entryid") {
        //        insertTagsInner(selector.elementId, selector.textComp.obj, "" +entryId+"|"+value+" "," ","importtype");
        insertTagsInner(selector.elementId, selector.textComp.obj, entryId," ","importtype");
    } else { 
        selector.getHiddenComponent().val(entryId);
        selector.getTextComponent().val(value);
    }
    selectCancel();
}

function selectCancel() {
    $("#ramadda-selectdiv").hide();
}


function selectCreate(event, selectorId,elementId, allEntries,selecttype, localeId) {
    if(!selectors[selectorId]) {
        selectors[selectorId] = new Selector(event,selectorId, elementId,allEntries,selecttype,localeId);
    } else {
        //Don:  alert('have selector'):
        selectors[selectorId].handleClick(event);
    }
}


function selectInitialClick(event, selectorId, elementId, allEntries, selecttype, localeId) {
    selectCreate(event, selectorId, elementId, allEntries, selecttype, localeId);
    return false;
}


function clearSelect(id) {
    selector = selectors[id];
    if(selector) {
        selector.clearInput();
    } else {
        //In case the user never clicked select
        var textComp = GuiUtils.getDomObject(id);
        var hiddenComp = GuiUtils.getDomObject(id+"_hidden");
	if(hiddenComp) {
            hiddenComp.obj.value =""
        }
	if(textComp) {
            textComp.obj.value =""
        }
    }
}


function handleSelect(request, id) {
    selector = selectors[id];
    var xmlDoc=request.responseXML.documentElement;
    text = getChildText(xmlDoc);
    var close = "<a href=\"javascript:selectCancel();\"><img border=0 src=" + icon_close + "></a>";
    selector.div.obj.innerHTML = "<table width=100%><tr><td align=right>" + close +"</table>" +text;
}




function  getChildText(node) {
    var text = '';
    for(childIdx=0;childIdx<node.childNodes.length;childIdx++) {
        text = text  + node.childNodes[childIdx].nodeValue;
    }
    return text;
	
}


function toggleVisibility(id,style) {
    var display  = $("#" + id).css('display');
    $("#" + id).toggle();
    return display != 'block';
}


function hide(id) {
    $("#" + id).hide();
    //    hideElementById(id);
}

function hideElementById(id) {
    hideObject(GuiUtils.getDomObject(id));
}

function checkToHidePopup() {
    if(popupTime) {
        var now = new Date();
        timeDiff = now-popupTime;
        if(timeDiff>1000)  {
            return 1;
        }
    }
}

function showPopup(event, srcId, popupId, alignLeft, myalign, atalign) {
    if(popupSrcId == srcId) {
        if(checkToHidePopup()) {
            hidePopupObject();
            return;
        }
    }

    popupTime = new Date();
    hidePopupObject();
    var popup = GuiUtils.getDomObject(popupId);
    var srcObj = GuiUtils.getDomObject(srcId);
    if(!popup || !srcObj) return;
    popupObject = popup;
    popupSrcId = srcId;

    if(!myalign)
        myalign = 'left top';
    if(!atalign)
        atalign = 'left top';
    if(alignLeft) {
        myalign = 'right top';
        atalign =  'left top';
    }
    showObject(popup);
    jQuery("#"+popupId ).position({
                of: jQuery( "#" + srcId ),
                my: myalign,
                at: atalign,
                collision: "none none"
                });
    //Do it again to fix a bug on safari
    jQuery("#"+popupId ).position({
                of: jQuery( "#" + srcId ),
                my: myalign,
                at: atalign,
                collision: "none none"
                });
}


function showStickyPopup(event, srcId, popupId, alignLeft) {
    var myalign = 'left top';
    var atalign = 'left top';


    $("#"+popupId ).show("slow");
    $("#"+popupId ).position({
                of: jQuery( "#" + srcId ),
                my: myalign,
                at: atalign,
                collision: "none none"
                });
    //Do it again to fix a bug on safari
    $("#"+popupId ).position({
                of: jQuery( "#" + srcId ),
                my: myalign,
                at: atalign,
                collision: "none none"
                });
}



function hideObject(obj) {
    if(!obj) {
        return 0;
    }
    $("#" + obj.id).hide();
    return 1;
}


function hideMore(base) {
    var link = GuiUtils.getDomObject("morelink_" + base);
    var div = GuiUtils.getDomObject("morediv_" + base);
    hideObject(div);
    showObject(link);
}


function showMore(base) {
    var link = GuiUtils.getDomObject("morelink_" + base);
    var div = GuiUtils.getDomObject("morediv_" + base);
    hideObject(link);
    showObject(div);
}




function showObject(obj, display) {
    if(!obj) return 0;
    $("#" + obj.id).show();
    return;
}



function toggleVisibilityOnObject(obj, display) {
    if(!obj) return 0;
    $("#" + obj.id).toggle();
}






//This gets called from toggleBlockVisibility
//It updates any map on the page to fix some sort of offset problem
function ramaddaUpdateMaps() {
    if (!(typeof ramaddaMaps === 'undefined')) {
        for(i=0;i<ramaddaMaps.length;i++) {
            var ramaddaMap = ramaddaMaps[i];
            if(!ramaddaMap.map) continue;
            ramaddaMap.map.updateSize();
        }
    }
}  




var formDialogId;

function closeFormLoadingDialog () { 
    var dialog =   $(formDialogId);
    dialog.dialog('close');
}



function popupFormLoadingDialog (dialogId) { 
    formDialogId = dialogId;
    var dialog =   $(dialogId);
    dialog.dialog({
            resizable: false,
                height:100,
                modal: true
                }
        );
}


function submitEntryForm (dialogId) {
    popupFormLoadingDialog (dialogId);
    return true;
}




function treeViewClick (entryId, url, label) {
    var href="<a href='" + url +"'>" + label+"</a>";
    $("#treeview_header").html(href);
    url = url +"&template=empty";
    $('#treeview_view').attr("src",url); 
}


function treeViewGoTo () {
    var currentUrl =   $('#treeview_view').attr("src"); 
    if(currentUrl) {
        currentUrl = currentUrl.replace("template=","notemplate=");
        $(location).attr('href',currentUrl);
    }
}

function number_format( number, decimals, dec_point, thousands_sep ) {
    // http://kevin.vanzonneveld.net
    // +   original by: Jonas Raoni Soares Silva (http://www.jsfromhell.com)
    // +   improved by: Kevin van Zonneveld (http://kevin.vanzonneveld.net)
    // +     bugfix by: Michael White (http://crestidg.com)
    // +     bugfix by: Benjamin Lupton
    // +     bugfix by: Allan Jensen (http://www.winternet.no)
    // +    revised by: Jonas Raoni Soares Silva (http://www.jsfromhell.com)    
    // *     example 1: number_format(1234.5678, 2, '.', '');
    // *     returns 1: 1234.57     
 
    var n = number, c = isNaN(decimals = Math.abs(decimals)) ? 2 : decimals;
    var d = dec_point == undefined ? "," : dec_point;
    var t = thousands_sep == undefined ? "." : thousands_sep, s = n < 0 ? "-" : "";
    var i = parseInt(n = Math.abs(+n || 0).toFixed(c)) + "", j = (j = i.length) > 3 ? j % 3 : 0;
    
    return s + (j ? i.substr(0, j) + t : "") + i.substr(j).replace(/(\d{3})(?=\d)/g, "$1" + t) + (c ? d + Math.abs(n - i).toFixed(c).slice(2) : "");
}


var RamaddaUtil = {
    //applies extend to the given object
    //and sets a super member to the original object
    //you can call original super class methods with:
    //this.super.<method>.call(this,...);
    inherit: function(object, parent) {
        $.extend(object, parent);
        object.super = parent;
        return object;
    },
    //Just a wrapper around extend. We use this so it is easy to find 
    //class definitions
    initMembers: function(object, members) {
        $.extend(object, members);
        return object;
    },
    //Just a wrapper around extend. We use this so it is easy to find 
    //class definitions
    defineMembers: function(object, members) {
        $.extend(object, members);
        return object;
    }
}
