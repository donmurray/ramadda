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

package org.ramadda.geodata.earthdata;


import org.ramadda.repository.*;
import org.ramadda.repository.metadata.Metadata;
import org.ramadda.repository.type.*;



import org.ramadda.service.Service;
import org.ramadda.service.ServiceOutput;
import org.ramadda.util.Utils;



import org.w3c.dom.*;

import ucar.nc2.units.DateUnit;

import ucar.unidata.util.IOUtil;
import ucar.unidata.util.Misc;
import ucar.unidata.util.StringUtil;
import ucar.unidata.xml.XmlUtil;

import java.io.File;


import java.util.Date;
import java.util.List;


/**
 *
 *
 * @author Jeff McWhirter
 */
public class GdalTypeHandler extends GenericTypeHandler {

    /**
     * _more_
     *
     * @param repository _more_
     * @param node _more_
     * @throws Exception On badness
     */
    public GdalTypeHandler(Repository repository, Element node)
            throws Exception {
        super(repository, node);
    }



    /**
     * _more_
     *
     *
     * @param request _more_
     * @param entry _more_
     * @param service _more_
     * @param output _more_
     *
     * @throws Exception _more_
     */
    @Override
    public void handleServiceResults(Request request, Entry entry,
                                     Service service, ServiceOutput output)
            throws Exception {

        super.handleServiceResults(request, entry, service, output);
        List<Entry> entries = output.getEntries();
        if (entries.size() != 0) {
            return;
        }
        String results = output.getResults();
        System.err.println("r:" + results);
        /*
Upper Left  (  -28493.167, 4255884.544) (117d38'27.05"W, 33d56'37.74"N)
Lower Left  (  -28493.167, 4224973.143) (117d38'27.05"W, 33d39'53.81"N)
Upper Right (    2358.212, 4255884.544) (117d18'28.38"W, 33d56'37.74"N)
Lower Right (    2358.212, 4224973.143) (117d18'28.38"W, 33d39'53.81"N)
        */

        for (String line : StringUtil.split(results, "\n", true, true)) {
            double[] latlon;
            if (line.indexOf("Upper Left") >= 0) {
                latlon = getLatLon(line);
                if (latlon != null) {
                    entry.setNorth(latlon[0]);
                    entry.setWest(latlon[1]);
                }
            } else if (line.indexOf("Lower Right") >= 0) {
                latlon = getLatLon(line);
                if (latlon != null) {
                    entry.setSouth(latlon[0]);
                    entry.setEast(latlon[1]);
                }
            } else {}

        }
    }

    /**
     * _more_
     *
     * @param line _more_
     *
     * @return _more_
     */
    private double[] getLatLon(String line) {
        line = line.replaceAll("\\(", "");
        List<String> toks = StringUtil.split(line, " ", true, true);
        if (toks.size() != 6) {
            return null;
        }

        return new double[] { decodeLatLon(toks.get(5)),
                              decodeLatLon(toks.get(4)) };
    }

    /**
     * _more_
     *
     * @param s _more_
     *
     * @return _more_
     */
    private double decodeLatLon(String s) {
        s = s.replace("d", ":");
        s = s.replace("'", ":");
        s = s.replace("\"", "");

        return Misc.decodeLatLon(s);
    }



}
