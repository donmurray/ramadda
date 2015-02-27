
function dbRowOver(rowId) {
    $("#"+ rowId).css("background-color",  "#edf5ff");
}

function dbRowOut(rowId) {
    $("#"+ rowId).css("background-color",  "#fff");
}

function dbRowClick(event, divId, url) {
    row = Utils.getDomObject(divId);
    if(!row) return;
    left = Utils.getLeft(row.obj);
    eventX = Utils.getEventX(event);
    //Don't pick up clicks on the left side
    if(eventX-left<50) return;
    Utils.loadXML( url, dbHandleXml,divId);
}


function dbHandleXml(request,divId) {
    row = Utils.getDomObject(divId);
    if(!row) return;
    div = Utils.getDomObject("tooltipdiv");
    if(!div) return;
    var xmlDoc=request.responseXML.documentElement;
    text = getChildText(xmlDoc);
    Utils.setPosition(div, Utils.getLeft(row.obj), Utils.getBottom(row.obj));

    div.obj.innerHTML = "<div class=tooltip-inner><div id=\"tooltipwrapper\" ><table><tr valign=top><img width=\"16\" onmousedown=\"hideEntryPopup();\" id=\"tooltipclose\"  src=" + icon_close +"></td><td>" + text+"</table></div></div>";
    showObject(div);
}


function stickyDragEnd(id, url) {
    div  = Utils.getDomObject(id);
    if(!div) return;
    url = url +"&posx=" + div.style.left +"&posy=" + div.style.top;
    Utils.loadXML( url, stickyNOOP,id);
}


function stickyNOOP(request,divId) {
}