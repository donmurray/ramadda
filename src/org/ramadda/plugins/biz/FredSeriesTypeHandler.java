/*
 * Copyright (c) 2008-2015 Geode Systems LLC
 * This Software is licensed under the Geode Systems RAMADDA License available in the source distribution in the file 
 * ramadda_license.txt. The above copyright notice shall be included in all copies or substantial portions of the Software.
 */

package org.ramadda.plugins.biz;


import org.json.*;

import org.ramadda.data.services.PointTypeHandler;


import org.ramadda.repository.*;
import org.ramadda.repository.metadata.*;
import org.ramadda.repository.type.*;
import org.ramadda.util.HtmlUtils;
import org.ramadda.util.Json;
import org.ramadda.util.TTLCache;
import org.ramadda.util.Utils;

import org.w3c.dom.*;
import org.w3c.dom.*;

import ucar.unidata.util.DateUtil;
import ucar.unidata.util.IOUtil;
import ucar.unidata.util.StringUtil;
import ucar.unidata.xml.XmlUtil;

import java.net.URL;

import java.text.SimpleDateFormat;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.List;


/**
 */
public class FredSeriesTypeHandler extends PointTypeHandler {


    //NOTE: This starts at 2 because the point type has a number of points field

    /** _more_          */
    public static final int IDX_SERIES_ID = 2;
    public static final int IDX_FREQUENCY = 3;
    public static final int IDX_UNITS = 4;
    public static final int IDX_SEASONAL_ADJUSTMENT = 5;


    /**
     * _more_
     *
     * @param repository _more_
     * @param entryNode _more_
     *
     * @throws Exception _more_
     */
    public FredSeriesTypeHandler(Repository repository, Element entryNode)
            throws Exception {
        super(repository, entryNode);
    }



    public void initializeNewEntry(Request request, Entry entry)
            throws Exception {
        super.initializeNewEntry(request, entry);
        System.err.println("FredSeries.init");
        initializeSeries(entry);
    }


    public void initializeSeries(Entry entry)
            throws Exception {
        //        super.initializeNewEntry(request, entry);


        FredCategoryTypeHandler fcth =
            (FredCategoryTypeHandler) getRepository().getTypeHandler(
                                                                     Fred.TYPE_CATEGORY);
        String seriesId  = (String) entry.getValue(IDX_SERIES_ID, null);
        if(seriesId == null) {
            System.err.println("No series id");
            return;
        }
        entry.setResource(new Resource(new URL("https://research.stlouisfed.org/fred2/series/" + seriesId)));

        List<String> args = new ArrayList<String>();
        args.add(Fred.ARG_SERIES_ID);
        args.add(seriesId);
        Element         root     = fcth.call(Fred.URL_SERIES, args);
        System.err.println(XmlUtil.toString(root));

        Object[] values =getEntryValues(entry);
        Element node = XmlUtil.findChild(root, Fred.TAG_SERIES);

        if(node!=null) {
            entry.setName(XmlUtil.getAttribute(node, Fred.ATTR_TITLE, entry.getName()));
            entry.setDescription(XmlUtil.getAttribute(node, Fred.ATTR_NOTES, ""));
            values[IDX_FREQUENCY] =  XmlUtil.getAttribute(node, Fred.ATTR_FREQUENCY_SHORT, "");
            values[IDX_UNITS] = XmlUtil.getAttribute(node, Fred.ATTR_UNITS, "");
            values[IDX_SEASONAL_ADJUSTMENT] = XmlUtil.getAttribute(node, Fred.ATTR_SEASONAL_ADJUSTMENT, "");

        }

    //ATTR_OBSERVATION_START





    }

    /**
     * _more_
     *
     * @param entry _more_
     *
     * @return _more_
     *
     * @throws Exception _more_
     */
    @Override
    public String getPathForEntry(Entry entry) throws Exception {
        String id = entry.getValue(IDX_SERIES_ID, (String) null);
        if (id == null) {
            return null;
        }
        FredCategoryTypeHandler fcth =
            (FredCategoryTypeHandler) getRepository().getTypeHandler(
                Fred.TYPE_CATEGORY);
        List<String> args = new ArrayList<String>();
        args.add(Fred.ARG_SERIES_ID);
        args.add(id);
        String url = fcth.makeUrl(Fred.URL_SERIES_OBSERVATIONS, args);
        System.err.println("URL:" + url);

        return url;
    }


}
