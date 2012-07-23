/*
* Copyright 2008-2011 Jeff McWhirter/ramadda.org
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

package org.ramadda.geodata.gps;


import org.ramadda.repository.*;
import org.ramadda.repository.type.*;

import org.w3c.dom.*;

import ucar.unidata.util.IOUtil;
import ucar.unidata.util.Misc;
import ucar.unidata.util.StringUtil;

import java.io.File;

import java.util.List;


/**
 *
 *
 * @author Jeff McWhirter
 */
public class OpusTypeHandler extends GenericTypeHandler {

    /** _more_ */
    public static final String TYPE_OPUS = "project_gps_opus";

    /** _more_ */
    private static int COLCNT = 0;

    /** _more_ */
    public static final int IDX_SITE_CODE = COLCNT++;

    /** _more_ */
    public static final int IDX_UTM_X = COLCNT++;

    /** _more_ */
    public static final int IDX_UTM_Y = COLCNT++;

    /** _more_ */
    public static final int IDX_ITRF_X = COLCNT++;

    /** _more_ */
    public static final int IDX_ITRF_Y = COLCNT++;

    /** _more_ */
    public static final int IDX_ITRF_Z = COLCNT++;





    /**
     * _more_
     *
     * @param repository _more_
     * @param node _more_
     * @throws Exception On badness
     */
    public OpusTypeHandler(Repository repository, Element node)
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
        initializeOpusEntry(entry);
    }


    /**
     * _more_
     *
     * @param entry _more_
     *
     * @throws Exception On badness
     */
    @Override
    public void initializeNewEntry(Entry entry) throws Exception {
        super.initializeNewEntry(entry);
        initializeOpusEntry(entry);
    }



    /**
     * _more_
     *
     * @param entry _more_
     *
     * @throws Exception _more_
     */
    private void initializeOpusEntry(Entry entry) throws Exception {
        String opus = new String(
                          IOUtil.readBytes(
                              getStorageManager().getFileInputStream(
                                  entry.getFile())));
        /*
      LAT:   40 6 46.56819      0.003(m)        40 6 46.58791      0.003(m)
    E LON:  253 35  8.56821      0.010(m)       253 35  8.52089      0.010(m)
    W LON:  106 24 51.43179      0.010(m)       106 24 51.47911      0.010(m)
   EL HGT:         2275.608(m)   0.009(m)              2274.768(m)   0.009(m)
Northing (Y) [meters]     4441227.340           391737.791
Easting (X)  [meters]      379359.228           836346.070

  X:     -1380903.608(m)   0.015(m)          -1380904.391(m)   0.015(m)
        Y:     -4687187.453(m)   0.012(m)          -4687186.144(m)   0.012(m)
        Z:      4089011.143(m)   0.010(m)           4089011.067(m)   0.010(m)
         */
        //        List<String> 
        Object[] values = entry.getTypeHandler().getValues(entry);
        String[] patterns = { "Northing\\s*\\(Y\\)\\s*\\[meters\\]\\s*([-\\.\\d]+)\\s+",
                              "Easting\\s*\\(X\\)\\s*\\[meters\\]\\s*([-\\.\\d]+)\\s+",
                              "X:\\s*([-\\.\\d]+)\\(",
                              "Y:\\s*([-\\.\\d]+)\\(",
                              "Z:\\s*([-\\.\\d]+)\\(", };
        for (int i = 0; i < patterns.length; i++) {
            String value = StringUtil.findPattern(opus, patterns[i]);
            if (value != null) {
                values[i + IDX_UTM_X] = new Double(value);
            }
        }
        String latLine    = StringUtil.findPattern(opus, "LAT: *([^\n]+)\n");
        String lonLine    = StringUtil.findPattern(opus, "LON: *([^\n]+)\n");
        String heightLine = StringUtil.findPattern(opus,
                                "HGT: *([^\\(]+)\\(");
        double altitude = 0.0;
        if (heightLine != null) {
            //            System.err.println ("hgt: " + heightLine);
            altitude = Double.parseDouble(heightLine.trim());
        }
        if ((latLine != null) && (lonLine != null)) {
            List<String> latToks = StringUtil.split(latLine.trim(), " ",
                                       true, true);
            List<String> lonToks = StringUtil.split(lonLine.trim(), " ",
                                       true, true);
            double lat = Misc.decodeLatLon(latToks.get(0) + ":"
                                           + latToks.get(1) + ":"
                                           + latToks.get(2));
            double lon =
                Misc.normalizeLongitude(Misc.decodeLatLon(lonToks.get(0)
                    + ":" + lonToks.get(1) + ":" + lonToks.get(2)));
            //            System.err.println ("lat: " + lat + " " + lon +" alt:" + altitude);
            entry.setLocation(lat, lon, altitude);
        }
    }

    public static void main(String[]args) {
        String opus = "Northing (Y) [meters]     3634840.411\n" +
            "Easting (X)  [meters]      734737.791\n";
        String[] patterns = { "Northing\\s*\\(Y\\)\\s*\\[meters\\]\\s*([-\\.\\d]+)\\s+",
                              "Easting\\s*\\(X\\)\\s*\\[meters\\]\\s*([-\\.\\d]+)\\s+",
                              "X:\\s*([-\\.\\d]+)\\(",
                              "Y:\\s*([-\\.\\d]+)\\(",
                              "Z:\\s*([-\\.\\d]+)\\(", };
        for (int i = 0; i < patterns.length; i++) {
            String value = StringUtil.findPattern(opus, patterns[i]);
            if (value != null) {
                System.err.println("i:" + i +" " + patterns[i] +" " + value);
            } else {
                System.err.println("i:" + i +" " + patterns[i] +" BAD" );
            }
        }





    }

}
