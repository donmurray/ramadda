/*
* Copyright 2008-2012 Jeff McWhirter/ramadda.org
*                     Don Murray/CU-CIRES
*
* Permission is hereby granted, free of charge, to any person obtaining a copy of this 
* software and associated documentation files (the "Software"), to deal in the Software 
* without restriction, including without limitation the rights to use, copy, modify, 
* merge, publish, distribute, sublicense, and/or sell copies of the Software, and to 
* permit persons to whom the Software is furnished to do so, subject to the following conditions:
* 
* The above copyright notice and this permission notice shall be included in all copies 
* or substantial portions of the Software.
* 
* THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, 
* INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR 
* PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE 
* FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR 
* OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER 
* DEALINGS IN THE SOFTWARE.
*/

package org.ramadda.geodata.cdmdata;


import org.ramadda.repository.*;

import org.ramadda.repository.type.*;
import ucar.nc2.units.DateUnit;

import ucar.unidata.util.Misc;
import org.ramadda.util.HtmlUtils;
import org.ramadda.util.Utils;


import org.w3c.dom.*;

import java.io.File;
import java.util.Date;
import java.util.Hashtable;
import java.util.List;

import ucar.nc2.NetcdfFile;
import ucar.nc2.Attribute;


/**
 *
 *
 * @author RAMADDA Development Team
 * @version $Revision: 1.3 $
 */
public class RadarTypeHandler extends GenericTypeHandler {
    public static final String TYPE_RADAR = "cdm_radar";
    public static final String TYPE_RADAR_LEVEL2 = "cdm_radar_level2";
    public static final String TYPE_RADAR_LEVEL3 = "cdm_radar_level3";
    public static final int IDX_STATION_ID = 0;
    public static final int IDX_STATION_NAME = 1;
    public static final int IDX_STATION_LAT = 2;
    public static final int IDX_STATION_LON = 3;
    public static final int IDX_STATION_PRODUCT = 4;

    /**
     * _more_
     *
     * @param repository _more_
     * @param entryNode _more_
     *
     * @throws Exception _more_
     */
    public RadarTypeHandler(Repository repository, Element entryNode)
            throws Exception {
        super(repository, entryNode);
    }

    /**
     * _more_
     *
     * @param entry _more_
     * @param request _more_
     * @param links _more_
     *
     *
     * @throws Exception _more_
     */
    public void getEntryLinks(Request request, Entry entry, List<Link> links)
            throws Exception {
        super.getEntryLinks(request, entry, links);
        /*
        if (entry.getValues() == null) {
            return;
        }
        Object[] values = entry.getValues();
        if ((values.length >= 2) && (values[0] != null)
                && (values[1] != null)) {
            links.add(
                new Link(
                    HtmlUtils.url(
                        "http://radar.weather.gov/radar.php", "rid",
                        (String) entry.getValues()[0], "product",
                        (String) entry.getValues()[1]), iconUrl(
                            "/icons/radar.gif"), "Show NWS Radar Site"));
        }
        */
    }


@Override
    public void initializeEntryFromForm(Request request, Entry entry,
                                        Entry parent, boolean newEntry) throws Exception {
        initializeNewEntry(entry);
    }



    public void initializeNewEntry(Entry entry) throws Exception {
        Object[] values = entry.getTypeHandler().getValues(entry);
        File f = entry.getFile();
        NetcdfFile ncf =  NetcdfFile.open(f.toString());

        //Merge all of the attributes into one map
        Hashtable<String,String> attrMap = new Hashtable<String,String>();
        List<Attribute> attrs      = ncf.getGlobalAttributes();
        for (Attribute attr : attrs) {
            String name  = attr.getName();
            String value = attr.getStringValue();
            if (value == null) {
                value = "" + attr.getNumericValue();
            }
            attrMap.put(name, value);
        }


        
        String stationId = Misc.getProperty(attrMap, CdmUtil.ATTR_RADAR_STATIONID, 
                                            Misc.getProperty(attrMap, CdmUtil.ATTR_STATIONID, ""));
        String stationName =  Misc.getProperty(attrMap, CdmUtil.ATTR_RADAR_STATIONNAME, 
                                               Misc.getProperty(attrMap, CdmUtil.ATTR_STATIONNAME, ""));
        double radarLat = Double.parseDouble(Misc.getProperty(attrMap, CdmUtil.ATTR_RADAR_LATITUDE, 
                                                              Misc.getProperty(attrMap,CdmUtil.ATTR_STATION_LATITUDE, "0.0")));
        double radarLon = Double.parseDouble(Misc.getProperty(attrMap, CdmUtil.ATTR_RADAR_LONGITUDE, 
                                                              Misc.getProperty(attrMap, CdmUtil.ATTR_STATION_LONGITUDE, "0.0")));
        String product = Misc.getProperty(attrMap, CdmUtil.ATTR_KEYWORDS_VOCABULARY, "");

        String sdate = Misc.getProperty(attrMap, CdmUtil.ATTR_TIME_START, (String)null);
        Date  startDate = sdate==null?new Date():DateUnit.getStandardOrISO(sdate);

        float latMin = Float.parseFloat(Misc.getProperty(attrMap, CdmUtil.ATTR_MINLAT, "0.0f"));
        float latMax = Float.parseFloat(Misc.getProperty(attrMap, CdmUtil.ATTR_MAXLAT, "0.0f"));
        float lonMin = Float.parseFloat(Misc.getProperty(attrMap, CdmUtil.ATTR_MINLON, "0.0f"));
        float lonMax = Float.parseFloat(Misc.getProperty(attrMap, CdmUtil.ATTR_MAXLON, "0.0f"));

        //Flip them
        if(lonMin>lonMax) {
            float tmp = lonMin;
            lonMin = lonMax;
            lonMax = tmp;
        }
        double altitude = Double.parseDouble(Misc.getProperty(attrMap, CdmUtil.ATTR_RADAR_ALTITUDE, 
                                                              Misc.getProperty(attrMap, CdmUtil.ATTR_STATION_ALTITUDE, "0.0")));


        if(!Utils.stringDefined(entry.getDescription())) {
            entry.setDescription(Misc.getProperty(attrMap, CdmUtil.ATTR_SUMMARY, ""));
        }

        values[IDX_STATION_ID] = stationId;
        values[IDX_STATION_NAME] = stationName;
        values[IDX_STATION_LAT] = radarLat;
        values[IDX_STATION_LON] = radarLon;
        values[IDX_STATION_PRODUCT] = product;

        entry.setStartDate(startDate.getTime());
        entry.setEndDate(startDate.getTime());
        entry.setAltitude(altitude);
        entry.setSouth(latMin);
        entry.setNorth(latMax);
        entry.setEast(lonMax);
        entry.setWest(lonMin);

    }


}
