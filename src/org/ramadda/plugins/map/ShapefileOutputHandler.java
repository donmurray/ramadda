/**
* Copyright (c) 2008-2015 Geode Systems LLC
* This Software is licensed under the Geode Systems RAMADDA License available in the source distribution in the file 
* ramadda_license.txt. The above copyright notice shall be included in all copies or substantial portions of the Software.
*/

/**
 * Copyright (c) 2008-2015 Geode Systems LLC
 * This Software is licensed under the Geode Systems RAMADDA License available in the source distribution in the file
 * ramadda_license.txt. The above copyright notice shall be included in all copies or substantial portions of the Software.
 */

package org.ramadda.plugins.map;


import org.ramadda.repository.*;
import org.ramadda.repository.output.*;
import org.ramadda.util.HtmlUtils;

import org.w3c.dom.*;

import ucar.unidata.data.gis.KmlUtil;

import ucar.unidata.data.gis.KmlUtil;
import ucar.unidata.gis.*;
import ucar.unidata.gis.shapefile.*;
import ucar.unidata.gis.shapefile.EsriShapefile;
import ucar.unidata.util.DateUtil;
import ucar.unidata.util.IOUtil;

import ucar.unidata.xml.XmlUtil;

import java.text.SimpleDateFormat;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;


/**
 *
 *
 */
public class ShapefileOutputHandler extends OutputHandler {


    /** Map output type */
    public static final OutputType OUTPUT_KML =
        new OutputType("Convert Shapefile to KML", "shapefile.kml",
                       OutputType.TYPE_FILE, "", ICON_KML);



    /**
     * Create a MapOutputHandler
     *
     *
     * @param repository  the repository
     * @param element     the Element
     * @throws Exception  problem generating handler
     */
    public ShapefileOutputHandler(Repository repository, Element element)
            throws Exception {
        super(repository, element);
        addType(OUTPUT_KML);
    }



    /**
     * Get the entry links
     *
     * @param request  the Request
     * @param state    the repository State
     * @param links    the links
     *
     * @throws Exception  problem creating links
     */
    public void getEntryLinks(Request request, State state, List<Link> links)
            throws Exception {
        if ((state.entry != null)
                && state.entry.getTypeHandler().isType("geo_shapefile")) {
            links.add(makeLink(request, state.entry, OUTPUT_KML));
        }
    }


    /**
     * Output the entry
     *
     * @param request      the Request
     * @param outputType   the type of output
     * @param entry        the Entry to output
     *
     * @return  the Result
     *
     * @throws Exception  problem outputting entry
     */
    public Result outputEntry(Request request, OutputType outputType,
                              Entry entry)
            throws Exception {
        if (outputType.equals(OUTPUT_KML)) {
            return outputKml(request, entry);
        }

        return null;
    }



    /**
     * _more_
     *
     * @param request _more_
     * @param entry _more_
     *
     * @return _more_
     *
     * @throws Exception _more_
     */
    private Result outputKml(Request request, Entry entry) throws Exception {
        Element root   = KmlUtil.kml(entry.getName());
        Element folder = KmlUtil.folder(root, entry.getName(), true);
        KmlUtil.open(folder, false);
        if (entry.getDescription().length() > 0) {
            KmlUtil.description(folder, entry.getDescription());
        }


        EsriShapefile shapefile =
            new EsriShapefile(entry.getFile().toString());
        List features = shapefile.getFeatures();
        for (int i = 0; i < features.size(); i++) {
            EsriShapefile.EsriFeature gf =
                (EsriShapefile.EsriFeature) features.get(i);
            java.util.Iterator pi = gf.getGisParts();
            while (pi.hasNext()) {
                GisPart  gp = (GisPart) pi.next();
                double[] xx = gp.getX();
                double[] yy = gp.getY();
                if (xx.length == 1) {
                    KmlUtil.placemark(folder, "", null, yy[0], xx[0], 0,
                                      null);
                } else if (xx.length > 1) {
                    float[][] pts = new float[2][xx.length];
                    for (int ptIdx = 0; ptIdx < xx.length; ptIdx++) {
                        pts[0][ptIdx] = (float) yy[ptIdx];
                        pts[1][ptIdx] = (float) xx[ptIdx];
                    }
                    KmlUtil.placemark(folder, "", null, pts,
                                      java.awt.Color.red, 1);
                }
            }
        }
        StringBuffer sb = new StringBuffer(XmlUtil.XML_HEADER);
        sb.append(XmlUtil.toString(root));
        Result result = new Result("", sb, KmlOutputHandler.MIME_KML);
        result.setReturnFilename(
            IOUtil.stripExtension(getStorageManager().getFileTail(entry))
            + ".kml");

        return result;
    }



}
