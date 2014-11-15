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

var icon_tree_open = ramaddaBaseUrl +"/icons/togglearrowdown.gif";
var icon_tree_closed = ramaddaBaseUrl +"/icons/togglearrowright.gif";


var icon_menuarrow = ramaddaBaseUrl +"/icons/downdart.gif";
var icon_blank = ramaddaBaseUrl +"/icons/blank.gif";
var uniqueCnt = 0;

function noop() {}

var Utils = {
    isDefined: function(v) {
        return  !(typeof v=== 'undefined');
    },
    getDefined: function(v1, v2) {
        if(Utils.isDefined(v1)) return v1;
        return v2;
    }
};

var GuiUtils = {
    handleError: function(error, extra) {
        console.log("Error: " +error);
        //        alert("An error has occurred: " + error);
        if(extra) {
            console.log(extra);
        }
        closeFormLoadingDialog ();
    },
    isJsonError: function(data) {
        if(data == null) {
            this.handleError("Null JSON data");
            return true;
        }
        if(data.error!=null) {
            var code = data.errorcode;
            if(code == null) code = "error";
            this.handleError(data.error);
            return true;
        }
        return false;
    },
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
    loadHtml: function (url, callback) {
        var jqxhr = $.get(url, function(data) {
                alert( "success" );
                console.log(data);
            })
        .done(function() {})
        .fail(function() {
                console.log("Failed to load url: "+ url);
            });
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


var TAG_A = "a";
var TAG_B = "b";
var TAG_DIV = "div";
var TAG_LI = "li";
var TAG_SELECT = "select";
var TAG_OPTION = "option";
var TAG_TABLE = "table";
var TAG_TR = "tr";
var TAG_TD = "td";
var TAG_UL= "ul";
var TAG_OL= "ol";

var ATTR_WIDTH = "width";
var ATTR_HREF = "href";
var ATTR_BORDER = "border";
var ATTR_VALUE = "value";
var ATTR_TITLE = "title";
var ATTR_ALT = "alt";
var ATTR_ID = "id";
var ATTR_CLASS = "class";
var ATTR_SIZE = "size";
var ATTR_STYLE = "style";
var ATTR_ALIGN = "align";
var ATTR_VALIGN = "valign";

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
    sqt : function (value) {
        return "\'" + value +"\'";
    },
    getUniqueId: function() {
        return "id_" + (uniqueCnt++);
    },
    hbox : function() {
        var row = HtmlUtil.openTag("tr",["valign","top"]);
        row += "<td>";
        row += HtmlUtil.join(arguments,"</td><td>");
        row += "</td></tr>";
        return this.tag("table",["border","0", "cellspacing","0","cellpadding","0"],
                        row);
    },
    leftRight : function(left,right,leftWeight, rightWeight) {
        if(leftWeight==null) leftWeight = "6";
        if(rightWeight==null) rightWeight = "6";
        return this.div(["class","row"],
                        this.div(["class", "col-md-" + leftWeight], left) +
                        this.div(["class", "col-md-" + rightWeight,"style","align:right;"], right));
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
    setFormValue: function(id,val) {
        $("#"+ id).val(val);
    },
    setHtml: function(id,val) {
        $("#"+ id).html(val);
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
        html += "</" + tagName +">";
        return html;
    },
    openTag : function(tagName, attrs) {
        var html = "<" + tagName +" " + this.attrs(attrs) +">";
        return html;
    },
    openDiv : function(attrs) {
        return this.openTag("div",attrs);
    },
    closeDiv : function() {
        return this.closeTag("div");
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

    handleFormChangeShowUrl: function(formId, outputId, skip) {
        if(skip == null) {
            skip = [".*OpenLayers_Control.*","authtoken"];
        }
        var url = $("#" + formId).attr("action")+"?";
        var inputs = $("#" + formId +" :input");
        var cnt = 0;
        inputs.each(function (i, item) {
                if(item.name == "" || item.value == null || item.value == "") return;
                if(item.type == "checkbox") {
                    if(!item.checked) {
                        return;
                    }
                } 
                if(skip!=null) {
                    for(var i=0;i<skip.length;i++) {
                        var pattern = skip[i];
                        if(item.name.match(pattern)) {
                            return;
                        }
                    }
                }
                if(cnt>0) url += "&";
                cnt++;
                url += encodeURIComponent(item.name) + "=" + encodeURIComponent(item.value);
            });
        var base = window.location.protocol+ "://" + window.location.host;
        url = base + url;                        
        $("#" + outputId).html(HtmlUtil.div(["class","ramadda-form-url"],  HtmlUtil.href(url, HtmlUtil.image(ramaddaBaseUrl +"/icons/link.png")) +" " + url));
    },
    makeUrlShowingForm: function(formId, outputId, skip) {
        $("#" + formId +" :input").change(function() {
                HtmlUtil.handleFormChangeShowUrl(formId, outputId, skip);
            });
        HtmlUtil.handleFormChangeShowUrl(formId, outputId, skip);
    },
    input :   function(name, value, attrs) {
        return "<input " + HtmlUtil.attrs(attrs) + HtmlUtil.attrs(["name", name, "value",value]) +"/>";
    },

    valueDefined: function(value) {
        if(value != "" && value.indexOf("--") != 0) {
            return true;
        }
        return false;
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


var StringUtil = {
    endsWith : function(str,suffix) {
        return (str.length >= suffix.length) && 
               (str.lastIndexOf(suffix) + suffix.length == str.length);
    },
    startsWith : function(str,prefix) {
        return (str.length >= prefix.length) && 
               (str.lastIndexOf(prefix, 0) === 0);
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



