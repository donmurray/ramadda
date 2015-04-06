/*
 * Copyright (c) 2008-2015 Geode Systems LLC
 * This Software is licensed under the Geode Systems RAMADDA License available in the source distribution in the file 
 * ramadda_license.txt. The above copyright notice shall be included in all copies or substantial portions of the Software.
 */

package org.ramadda.geodata.gps;


import org.ramadda.repository.*;
import org.ramadda.repository.type.*;

import org.w3c.dom.*;

import ucar.unidata.util.Misc;

import java.io.File;

import java.util.List;


/**
 *
 *
 * @author Jeff McWhirter
 */
public class GpsTypeHandler extends GenericTypeHandler {

    /** _more_ */
    public static final String TYPE_GPS = "project_gps";

    /** _more_ */
    public static final String TYPE_RINEX = "project_gps_rinex";

    /** _more_ */
    public static final String TYPE_RAW = "project_gps_raw";

    /** _more_ */
    public static final String TYPE_CONTROLPOINTS =
        "project_gps_controlpoints";

    /**
     * _more_
     *
     * @param repository _more_
     * @param node _more_
     * @throws Exception On badness
     */
    public GpsTypeHandler(Repository repository, Element node)
            throws Exception {
        super(repository, node);
    }



    /**
     * _more_
     *
     * @param request _more_
     * @param entry _more_
     * @param parent _more_
     * @param newEntry _more_
     *
     * @throws Exception On badness
     */
    @Override
    public void initializeEntryFromForm(Request request, Entry entry,
                                        Entry parent, boolean newEntry)
            throws Exception {
        super.initializeEntryFromForm(request, entry, parent, newEntry);
        if (newEntry) {
            initializeGpsEntry(entry);
        }
    }


    /**
     * _more_
     *
     *
     * @param request _more_
     * @param entry _more_
     *
     * @throws Exception On badness
     */
    @Override
    public void initializeNewEntry(Request request, Entry entry)
            throws Exception {
        super.initializeNewEntry(request, entry);
        //        Misc.printStack("GpsTypeHandler.initializeNewEntry",10,null);
        initializeGpsEntry(entry);
    }

    /**
     * _more_
     *
     * @param entry _more_
     *
     * @throws Exception _more_
     */
    private void initializeGpsEntry(Entry entry) throws Exception {
        //Get the output handler
        GpsOutputHandler gpsOutputHandler =
            (GpsOutputHandler) getRepository().getOutputHandler(
                GpsOutputHandler.OUTPUT_GPS_TORINEX);
        gpsOutputHandler.initializeGpsEntry(entry, this);
    }



}
