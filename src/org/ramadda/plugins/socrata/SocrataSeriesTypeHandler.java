/*
 * Copyright (c) 2008-2015 Geode Systems LLC
 * This Software is licensed under the Geode Systems RAMADDA License available in the source distribution in the file 
 * ramadda_license.txt. The above copyright notice shall be included in all copies or substantial portions of the Software.
 */

package org.ramadda.plugins.socrata;


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
public class SocrataSeriesTypeHandler extends PointTypeHandler {


    /** _more_          */
    public static final String URL_TEMPLATE =
        "${hostname}/api/views/${series_id}/rows.json?accessType=DOWNLOAD";

    /** _more_          */
    public static final String TYPE_SERIES = "type_socrata_series";

    //NOTE: This starts at 2 because the point type has a number of points field

    /** _more_ */
    public static final int IDX_REPOSITORY = 2;

    /** _more_          */
    public static final int IDX_SERIES_ID = 3;


    /**
     * _more_
     *
     * @param repository _more_
     * @param entryNode _more_
     *
     * @throws Exception _more_
     */
    public SocrataSeriesTypeHandler(Repository repository, Element entryNode)
            throws Exception {
        super(repository, entryNode);
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
        String repository = entry.getValue(IDX_REPOSITORY, (String) null);
        String id         = entry.getValue(IDX_SERIES_ID, (String) null);
        if ( !Utils.stringDefined(id) || !Utils.stringDefined(repository)) {
            return null;
        }
        String url = URL_TEMPLATE.replace("{$hostname}",
                                          repository).replace("${series_id}",
                                              id);

        return url;
    }


}
