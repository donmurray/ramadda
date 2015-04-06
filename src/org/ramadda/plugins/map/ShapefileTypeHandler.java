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


import ucar.unidata.gis.*;
import ucar.unidata.gis.shapefile.*;

import ucar.unidata.util.DateUtil;
import ucar.unidata.util.IOUtil;
import ucar.unidata.util.Misc;

import ucar.unidata.util.StringUtil;
import ucar.unidata.util.TwoFacedObject;
import ucar.unidata.xml.XmlUtil;


import java.awt.geom.Rectangle2D;


import java.io.File;


import java.text.SimpleDateFormat;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;


/**
 */
public class ShapefileTypeHandler extends GenericTypeHandler {

    /** _more_ */
    private static final int IDX_LON = 0;

    /** _more_ */
    private static final int IDX_LAT = 1;


    /**
     * _more_
     *
     * @param repository _more_
     * @param node _more_
     * @throws Exception _more_
     */
    public ShapefileTypeHandler(Repository repository, Element node)
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
     * @throws Exception _more_
     */
    public void initializeEntryFromForm(Request request, Entry entry,
                                        Entry parent, boolean newEntry)
            throws Exception {
        if ( !entry.isFile()) {
            return;
        }
        EsriShapefile shapefile = null;
        try {
            shapefile = new EsriShapefile(entry.getFile().toString());
        } catch (Exception exc) {
            return;
        }

        Rectangle2D bounds   = shapefile.getBoundingBox();
        double[][]  lonlat   = new double[][] {
            { bounds.getX() }, { bounds.getY() + bounds.getHeight() }
        };
        ProjFile    projFile = shapefile.getProjFile();
        if (projFile != null) {
            lonlat = projFile.convertToLonLat(lonlat);
        }
        entry.setNorth(lonlat[IDX_LAT][0]);
        entry.setWest(lonlat[IDX_LON][0]);
        lonlat[IDX_LAT][0] = bounds.getY();
        lonlat[IDX_LON][0] = bounds.getX() + bounds.getWidth();
        if (projFile != null) {
            lonlat = projFile.convertToLonLat(lonlat);
        }
        entry.setSouth(lonlat[IDX_LAT][0]);
        entry.setEast(lonlat[IDX_LON][0]);

    }



    /**
     *
     * @param request _more_
     * @param entry _more_
     * @param map _more_
     *
     * @return _more_
     */
    @Override
    public boolean addToMap(Request request, Entry entry, MapInfo map) {
        try {
            if ( !entry.isFile()) {
                return true;
            }
            //TODO: stream through the shapes
            EsriShapefile shapefile = null;
            try {
                shapefile = new EsriShapefile(entry.getFile().toString());
            } catch (Exception exc) {
                return true;
            }

            List features    = shapefile.getFeatures();
            int  totalPoints = 0;
            int  MAX_POINTS  = 10000;
            for (int i = 0; i < features.size(); i++) {
                if (totalPoints > MAX_POINTS) {
                    break;
                }
                EsriShapefile.EsriFeature gf =
                    (EsriShapefile.EsriFeature) features.get(i);
                java.util.Iterator pi = gf.getGisParts();
                while (pi.hasNext()) {
                    if (totalPoints > MAX_POINTS) {
                        break;
                    }
                    GisPart        gp     = (GisPart) pi.next();
                    double[]       xx     = gp.getX();
                    double[]       yy     = gp.getY();
                    List<double[]> points = new ArrayList<double[]>();
                    for (int ptIdx = 0; ptIdx < xx.length; ptIdx++) {
                        points.add(new double[] { yy[ptIdx], xx[ptIdx] });
                    }
                    totalPoints += points.size();
                    if (points.size() > 1) {
                        map.addLines("", points);
                    } else if (points.size() == 1) {
                        map.addMarker("id", points.get(0)[0],
                                      points.get(0)[1], null, "");
                    }
                }
            }
        } catch (Exception exc) {
            throw new RuntimeException(exc);
        }

        return false;
    }




}
