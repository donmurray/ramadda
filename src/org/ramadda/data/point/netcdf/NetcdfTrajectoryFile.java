/*
 * Copyright (c) 2008-2015 Geode Systems LLC
 * This Software is licensed under the Geode Systems RAMADDA License available in the source distribution in the file 
 * ramadda_license.txt. The above copyright notice shall be included in all copies or substantial portions of the Software.
 */

package org.ramadda.data.point.netcdf;


import org.ramadda.data.point.*;


import org.ramadda.data.point.netcdf.*;


import org.ramadda.data.record.*;
import org.ramadda.util.Utils;

import ucar.ma2.DataType;

import ucar.nc2.*;
import ucar.nc2.dataset.NetcdfDataset;
import ucar.nc2.ft.*;
import ucar.nc2.jni.netcdf.Nc4Iosp;
import ucar.nc2.time.Calendar;
import ucar.nc2.time.CalendarDate;
import ucar.nc2.time.CalendarDateFormatter;
import ucar.nc2.time.CalendarDateRange;

import ucar.unidata.util.IOUtil;


import ucar.unidata.util.StringUtil;

import java.io.*;

import java.util.ArrayList;




import java.util.Formatter;
import java.util.List;



/**
 * Class description
 *
 *
 * @version        $version$, Fri, Aug 23, '13
 * @author         Enter your name here...
 */
public class NetcdfTrajectoryFile extends NetcdfPointFile {

    /**
     * ctor
     */
    public NetcdfTrajectoryFile() {}




    /**
     * ctor
     *
     *
     *
     *
     *
     * @param filename _more_
     * @throws IOException On badness
     */
    public NetcdfTrajectoryFile(String filename) throws IOException {
        super(filename);
    }


    /**
     * _more_
     *
     * @param action _more_
     *
     * @return _more_
     */
    @Override
    public boolean isCapable(String action) {

        if (action.equals(ACTION_MAPINCHART)) {
            return true;
        }
        if (action.equals(ACTION_TRAJECTORY)) {
            return true;
        }

        return super.isCapable(action);
    }


    /**
     * _more_
     *
     * @param visitInfo _more_
     *
     * @return _more_
     *
     *
     * @throws Exception _more_
     */
    public VisitInfo prepareToVisit(VisitInfo visitInfo) throws Exception {
        super.prepareToVisit(visitInfo);
        String platform = "";
        //LOOK: this needs to be in the same order as the oceantypes.xml defines in the point plugin
        setFileMetadata(new Object[] { platform });

        return visitInfo;
    }


    /**
     * _more_
     *
     * @param args _more_
     *
     * @throws Exception _more_
     */
    public static void main(String[] args) throws Exception {
        PointFile.test(args, NetcdfTrajectoryFile.class);
    }





}
