
var imageDoFirst = 1;
function editImageClick(event, imgId, pt1x,pt1y,pt2x,pt2y) {
    var obj = GuiUtils.getDomObject(imgId);

    if(obj) {
	var ex  = GuiUtils.getEventX(event);
	var ey  = GuiUtils.getEventY(event);
        var  idx =  GuiUtils.getLeft(obj.obj);
        var  idy =  GuiUtils.getTop(obj.obj);
	var ix = ex-idx;
	var iy = ey-idy;

	var    fldx1= GuiUtils.getDomObject(pt1x);
	var    fldy1= GuiUtils.getDomObject(pt1y);
	var    fldx2= GuiUtils.getDomObject(pt2x);
	var    fldy2= GuiUtils.getDomObject(pt2y);
        var fldx;
	var fldy;
	if(imageDoFirst) {
            imageDoFirst	 =0;
	    fldx= fldx1;
	    fldy= fldy1;
	    fldx2.obj.value	 = ""+0
            fldy2.obj.value	 = ""+0;
        } else {
  	     imageDoFirst = 1;
	     fldx= fldx2;
	     fldy= fldy2;
        }
	if(fldx) {
		fldx.obj.value	 = ""+ix;
		fldy.obj.value	 = ""+iy;
	}
	var box = GuiUtils.getDomObject("image_edit_box");
	if(box) {
		var style = GuiUtils.getStyle(box);
		style.visibility =  "visible";
                style.left = idx+parseInt(fldx1.obj.value);
		style.top =  idy+parseInt(fldy1.obj.value);
		var x2 = parseInt(fldx2.obj.value);
		var y2 = parseInt(fldy2.obj.value);
	        if(x2>0) {
 	            style.width = x2-parseInt(fldx1.obj.value);
	            style.height = y2-parseInt(fldy1.obj.value);
		}  else {
 	            style.width = 0;
	            style.height = 0;
		}
//		alert(style.top + " " + style.left + " w:" + style.width + " " + style.height);
	}
    }
}

