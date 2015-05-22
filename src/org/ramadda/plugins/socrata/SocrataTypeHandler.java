/*
 * Copyright (c) 2008-2015 Geode Systems LLC
 * This Software is licensed under the Geode Systems RAMADDA License available in the source distribution in the file 
 * ramadda_license.txt. The above copyright notice shall be included in all copies or substantial portions of the Software.
 */

package org.ramadda.plugins.socrata;


import org.json.*;


import org.ramadda.repository.*;
import org.ramadda.repository.metadata.*;
import org.ramadda.repository.type.*;
import org.ramadda.util.HtmlUtils;
import org.ramadda.util.Json;
import org.ramadda.util.Utils;

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
public class SocrataTypeHandler extends ExtensibleGroupTypeHandler {


    /** _more_ */
    public static final int IDX_CATEGORY_ID = 0;



    /**
     * _more_
     *
     * @param repository _more_
     * @param entryNode _more_
     *
     * @throws Exception _more_
     */
    public SocrataTypeHandler(Repository repository, Element entryNode)
            throws Exception {
        super(repository, entryNode);
    }



    /**
     * _more_
     *
     * @return _more_
     */
    public boolean isSynthType() {
        return true;
    }



    /**
     * _more_
     *
     * @param request _more_
     * @param mainEntry _more_
     * @param parentEntry _more_
     * @param synthId _more_
     *
     * @return _more_
     *
     * @throws Exception _more_
     */
    public List<String> getSynthIds(Request request, Entry mainEntry,
                                    Entry parentEntry, String synthId)
            throws Exception {

        List<String> ids = parentEntry.getChildIds();
        if (ids != null) {
            return ids;
        }

        ids = new ArrayList<String>();

        if (Utils.stringDefined(synthId)) {
            List<String> toks = StringUtil.split(synthId, ":", true, true);
            if (toks.size() <= 1) {
                return null;
            }
            //            if ( !toks.get(0).equals(PREFIX_CATEGORY)) {
            //                return null;
            //            }
            //            categoryId = toks.get(1);
        }


        Object[] categoryValues = null;
        if ((parentEntry != null)
                && parentEntry.getTypeHandler().isType("type_socrata_view")) {
            categoryValues =
                parentEntry.getTypeHandler().getEntryValues(mainEntry);
        }
        if (categoryValues == null) {
            categoryValues =
                mainEntry.getTypeHandler().getEntryValues(mainEntry);
        }


        return ids;

    }


    /**
     * _more_
     *
     * @param mainEntry _more_
     * @param categoryEntry _more_
     * @param categoryId _more_
     * @param name _more_
     *
     * @return _more_
     *
     * @throws Exception _more_
     */
    private Entry createCategoryEntry(Entry mainEntry, Entry categoryEntry,
                                      String categoryId, String name)
            throws Exception {
        return  null;
        /*
        Date   dttm  = new Date();
        String id = createSynthId(mainEntry, Eia.PREFIX_CATEGORY, categoryId);
        Entry  entry = getEntryManager().getEntryFromCache(id);
        if (entry != null) {
            return entry;
        }


        String desc = "";
        entry = new Entry(id, this);
        String eiaUrl = "http://www.eia.gov/beta/api/qb.cfm?category="
                        + categoryId;
        Resource resource = new Resource(new URL(eiaUrl));
        Object[] values   = this.makeEntryValues(null);
        values[IDX_CATEGORY_ID] = categoryId;
        entry.initEntry(name, desc, categoryEntry, categoryEntry.getUser(),
                        resource, "", dttm.getTime(), dttm.getTime(),
                        dttm.getTime(), dttm.getTime(), values);

        getEntryManager().cacheEntry(entry);

        return entry;
        */
    }




    /**
     * _more_
     *
     * @param request _more_
     * @param mainEntry _more_
     * @param id _more_
     *
     * @return _more_
     *
     * @throws Exception _more_
     */
    @Override
    public Entry makeSynthEntry(Request request, Entry mainEntry, String id)
            throws Exception {
        Entry entry = getEntryManager().getEntryFromCache(id);
        if (entry != null) {
            return entry;
        }

        List<String> toks = StringUtil.split(id, ":", true, true);
        if (toks.size() <= 1) {
            return null;
        }
        String type = toks.get(0);
        id = toks.get(1);
        return null;
        //        if (type.equals(Eia.PREFIX_CATEGORY)) {
        //            return createCategoryEntry(mainEntry, mainEntry, id, null);
        //        } else {
        //            return createSeriesEntry(mainEntry, mainEntry, id, null);
        //        }
    }




}