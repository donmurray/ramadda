/*
 * Copyright (c) 2008-2015 Geode Systems LLC
 * This Software is licensed under the Geode Systems RAMADDA License available in the source distribution in the file 
 * ramadda_license.txt. The above copyright notice shall be included in all copies or substantial portions of the Software.
 */

package org.ramadda.data.services;


import org.ramadda.data.point.*;

import org.ramadda.data.record.*;
import org.ramadda.data.record.filter.*;
import org.ramadda.data.services.*;


import org.ramadda.repository.*;
import org.ramadda.util.grid.IdwGrid;
import org.ramadda.util.grid.LatLonGrid;

/*
For netcdf export sometime
import ucar.nc2.Attribute;
import ucar.nc2.Dimension;
import ucar.nc2.NetcdfFile;
import ucar.nc2.NetcdfFileWriteable;
import ucar.nc2.Variable;
import ucar.nc2.ft.point.writer.CFPointObWriter;
*/






import ucar.unidata.util.Misc;

import java.awt.*;

import java.io.*;




/**
 * A Record visitor that holds the IdwGrid
 *
 *
 */
public class GridVisitor extends BridgeRecordVisitor {

    /** the request */
    Request request;

    /** the grid that does the real work */
    IdwGrid llg;

    /** number of columns */
    double gridWidth;

    /** number of rows */
    double gridHeight;

    /** are we gridding another attribute instead of altitude */
    int valueAttr = -1;

    /** are we using altitude as the gridded value */
    boolean usingAltitude;

    /** how big an image */
    int imageHeight;

    /** how big an image */
    int imageWidth;

    /**
     * ctor
     *
     *
     * @param handler the output handler
     * @param request the request
     * @param llg the grid
     */
    public GridVisitor(RecordOutputHandler handler, Request request,
                       IdwGrid llg) {
        super(handler);
        this.request = request;
        this.llg     = llg;
        gridWidth    = llg.getGridWidth();
        gridHeight   = llg.getGridHeight();
        imageHeight  = llg.getHeight();
        imageWidth   = llg.getWidth();
        if (request.defined(RecordOutputHandler.ARG_PARAMETER)) {
            valueAttr = request.get(RecordOutputHandler.ARG_PARAMETER, -1);
        }
        usingAltitude = valueAttr == -1;
    }

    /**
     * get the grid
     *
     * @return the grid
     */
    public IdwGrid getGrid() {
        return llg;
    }

    /**
     * visit the record
     *
     * @param file record file
     * @param visitInfo visit info
     * @param record the record
     *
     * @return should continue
     */
    int cnt = 0;

    /**
     * _more_
     *
     * @param file _more_
     * @param visitInfo _more_
     * @param record _more_
     *
     * @return _more_
     */
    public boolean doVisitRecord(RecordFile file, VisitInfo visitInfo,
                                 Record record) {

        PointRecord pointRecord = (PointRecord) record;
        double      value;
        if (valueAttr != -1) {
            value = (double) pointRecord.getValue(valueAttr);
        } else {
            value = (double) pointRecord.getAltitude();
        }
        double lat = pointRecord.getLatitude();
        double lon = pointRecord.getLongitude();
        synchronized (MUTEX) {
            //If first time then reset the grid
            if (cnt == 0) {
                llg.resetGrid();
            }
            cnt++;
            llg.addValue(lat, lon, value);
        }

        return true;
    }


    /**
     * Done. Tell the llg to average its values
     *
     */
    public void finishedWithAllFiles() {
        llg.doAverageValues();
        if (request.get(PointOutputHandler.ARG_FILLMISSING, false)) {
            llg.fillMissing();
        }
    }

}
