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
public class FredTimeSeriesTypeHandler extends PointTypeHandler {


    //NOTE: This starts at 1 because the point type has a number of points field

    /** _more_          */
    public static final int IDX_SERIES_ID = 1;


    /**
     * _more_
     *
     * @param repository _more_
     * @param entryNode _more_
     *
     * @throws Exception _more_
     */
    public FredTimeSeriesTypeHandler(Repository repository, Element entryNode)
            throws Exception {
        super(repository, entryNode);
    }



    public void initializeNewEntry(Request request, Entry entry)
            throws Exception {
        super.initializeNewEntry(request, entry);

        FredCategoryTypeHandler fcth =
            (FredCategoryTypeHandler) getRepository().getTypeHandler(
                                                                     Fred.TYPE_CATEGORY);
        String seriesId  = (String) entry.getValue(IDX_SERIES_ID, null);
        if(seriesId == null) return;
        List<String> args = new ArrayList<String>();
        args.add(Fred.ARG_SERIES_ID);
        args.add(seriesId);
        Element         root     = fcth.call(Fred.URL_CATEGORY_CHILDREN, args);
        System.err.println(XmlUtil.toString(root));
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
        String url = fcth.makeUrl(Fred.URL_FRED_SERIES_OBSERVATIONS, args);
        System.err.println("URL:" + url);

        return url;
    }

}
