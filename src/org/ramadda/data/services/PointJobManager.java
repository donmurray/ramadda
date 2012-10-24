package org.ramadda.data.services;

import org.ramadda.data.record.*;
import org.ramadda.data.record.filter.*;
import org.ramadda.data.services.*;


import org.ramadda.repository.*;
import org.ramadda.repository.auth.*;
import org.ramadda.repository.job.JobInfo;

import org.ramadda.repository.job.JobManager;
import org.ramadda.repository.output.*;
import org.ramadda.repository.type.TypeHandler;

import org.w3c.dom.*;

import java.io.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Enumeration;



/**
 */
public class PointJobManager extends RecordJobManager  {


    /**
     * ctor
     *
     * @param lidarOutputHandler the output handler
     */
    public PointJobManager(PointOutputHandler pointOutputHandler) {
        super(pointOutputHandler);
    }

    public PointOutputHandler getPointOutputHandler() {
        return (PointOutputHandler) getRecordOutputHandler();
    }


    /**
     * _more_
     *
     * @param request _more_
     *
     * @return _more_
     */
    public String makeJobUrl(Request dummy) {
        dummy.remove(ARG_GETDATA);
        dummy.remove(ARG_RECORDENTRY);
        dummy.remove(ARG_RECORDENTRY_CHECK);
        dummy.remove("Boxes");
        List<String> products = (List<String>) dummy.get(ARG_PRODUCT,
                                    new ArrayList<String>());
        //If its just csv then get rid of the grid parameters
        if ((products.size() == 1)
                && (products.get(0).equals(getPointOutputHandler().OUTPUT_CSV.getId())
                    || products.get(0).equals(getPointOutputHandler().OUTPUT_LATLONALTCSV.getId()))) {
            for (String gridArg : new String[] {
                ARG_WIDTH, ARG_HEIGHT, ARG_COLORTABLE, ARG_GRID_RADIUS_CELLS,
                ARG_GRID_RADIUS_DEGREES, ARG_HILLSHADE_AZIMUTH,
                ARG_HILLSHADE_ANGLE, ARG_GRID_RADIUS_DEGREES_ORIG,
            }) {
                dummy.remove(gridArg);
            }
        }

        for (Enumeration keys = dummy.keys(); keys.hasMoreElements(); ) {
            String arg = (String) keys.nextElement();
            if (arg.startsWith("job.")) {
                dummy.remove(arg);
            }
            if (arg.startsWith("OpenLayers")) {
                dummy.remove(arg);
            }
        }
        return super.makeJobUrl(dummy);
    }



}
