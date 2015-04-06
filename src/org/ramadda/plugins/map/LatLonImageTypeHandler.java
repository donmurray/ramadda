/*
 * Copyright (c) 2008-2015 Geode Systems LLC
 * This Software is licensed under the Geode Systems RAMADDA License available in the source distribution in the file 
 * ramadda_license.txt. The above copyright notice shall be included in all copies or substantial portions of the Software.
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

        if ( !entry.hasAreaDefined()) {
            return false;
        }

        //Only set the width if the latlonentry is the main displayed entry

        if (entry.getId().equals(request.getString(ARG_ENTRYID, ""))) {
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
        if (request.getExtraProperty("wmslayershow") == null) {
            request.putExtraProperty("wmslayershow", "true");
            visible = true;
        }

        map.addJS(HtmlUtils.call("theMap.addImageLayer",
                                 HtmlUtils.jsMakeArgs(new String[] {
            HtmlUtils.squote(entry.getId()),
            HtmlUtils.squote(entry.getName()), HtmlUtils.squote(url),
            "" + visible, "" + entry.getNorth(), "" + entry.getWest(),
            "" + entry.getSouth(), "" + entry.getEast(), "400", "400"
        }, false)));

        return true;
    }



}
