/*
* Copyright 2008-2014 Geode Systems LLC
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

package org.ramadda.plugins.map;


import org.ramadda.repository.*;
import org.ramadda.repository.map.*;
import org.ramadda.repository.metadata.*;
import org.ramadda.repository.output.*;
import org.ramadda.repository.type.*;
import org.ramadda.util.HtmlUtils;


import org.w3c.dom.*;

import ucar.unidata.data.gis.KmlUtil;

import ucar.unidata.util.DateUtil;
import ucar.unidata.util.IOUtil;
import ucar.unidata.util.Misc;

import ucar.unidata.util.StringUtil;
import ucar.unidata.util.TwoFacedObject;
import ucar.unidata.xml.XmlUtil;


import java.io.File;


import java.text.SimpleDateFormat;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;



/**
 */
public class LatLonImageTypeHandler extends GenericTypeHandler {



    /**
     * _more_
     *
     * @param repository _more_
     * @param node _more_
     * @throws Exception _more_
     */
    public LatLonImageTypeHandler(Repository repository, Element node)
            throws Exception {
        super(repository, node);
    }


    /**
     * _more_
     *
     * @param request _more_
     * @param entry _more_
     * @param map _more_
     *
     * @return _more_
     */
    @Override
    public boolean addToMap(Request request, Entry entry, MapInfo map) {

        if (!entry.hasAreaDefined()) {
            return false;
        }

        //Only set the width if the latlonentry is the main displayed entry

        if(entry.getId().equals(request.getString(ARG_ENTRYID,""))) {
            int width  = (int) entry.getValue(0, -1);
            int height = (int) entry.getValue(1, -1);
            if ((width > 0) && (height > 0)) {
                map.setWidth(width);
                map.setHeight(height);
            }
        }



        String url =
            getRepository().getHtmlOutputHandler().getImageUrl(request,
                entry);

        boolean visible = false;
        if(request.getExtraProperty("wmslayershow") == null) {
            request.putExtraProperty("wmslayershow","true");
            visible = true;
        }

        map.addJS(HtmlUtils.call("theMap.addImageLayer",
                                 HtmlUtils.jsMakeArgs(new String[] {
                                         HtmlUtils.squote(entry.getId()),
                                         HtmlUtils.squote(entry.getName()), HtmlUtils.squote(url),
            ""+visible,
            "" + entry.getNorth(), "" + entry.getWest(),
            "" + entry.getSouth(), "" + entry.getEast(), "400", "400"
        }, false)));

        return true;
    }



}
