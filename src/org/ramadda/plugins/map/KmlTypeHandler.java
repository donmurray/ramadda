/**
 * Copyright 2008-2012 Jeff McWhirter/ramadda.org
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

package org.ramadda.plugins.map;


import org.ramadda.repository.*;
import org.ramadda.repository.metadata.*;
import org.ramadda.repository.output.*;
import org.ramadda.repository.map.*;
import org.ramadda.repository.type.*;


import java.awt.geom.Rectangle2D;

import org.w3c.dom.*;

import ucar.unidata.util.DateUtil;
import ucar.unidata.util.IOUtil;
import org.ramadda.util.HtmlUtils;
import ucar.unidata.util.Misc;
import ucar.unidata.data.gis.KmlUtil;

import ucar.unidata.util.StringUtil;
import ucar.unidata.util.TwoFacedObject;
import ucar.unidata.xml.XmlUtil;
import org.w3c.dom.Element;



import java.text.SimpleDateFormat;


import java.io.File;

import java.util.ArrayList;
import java.util.List;
import java.util.Date;


import ucar.unidata.gis.*;
import java.util.zip.*;

import ucar.unidata.data.gis.KmlUtil;


/**
 */
public class KmlTypeHandler extends GenericTypeHandler {



    /**
     * _more_
     *
     * @param repository _more_
     * @param node _more_
     * @throws Exception _more_
     */
    public KmlTypeHandler(Repository repository, Element node)
        throws Exception {
        super(repository, node);
    }


    public void initializeEntryFromForm(Request request, Entry entry,
                                        Entry parent, boolean newEntry)
        throws Exception {
        if(!entry.isFile()) return;
        Element kmlRoot = readKml(getRepository(), entry);
        if(kmlRoot==null) return;
        double[] nwse = new double[]{
            Entry.NONGEO,
            Entry.NONGEO,
            Entry.NONGEO,
            Entry.NONGEO,
        };

        List<Element> lats  = ( List<Element>)XmlUtil.findDescendants(kmlRoot, KmlUtil.TAG_LATITUDE);
        for(Element latNode: lats) {
            setLat(nwse,Double.parseDouble(XmlUtil.getChildText(latNode)));
        }
        List<Element> lons  = ( List<Element>)XmlUtil.findDescendants(kmlRoot, KmlUtil.TAG_LONGITUDE);
        for(Element lonNode: lons) {
            setLon(nwse,Double.parseDouble(XmlUtil.getChildText(lonNode)));
        }



        initializeEntry(entry, kmlRoot, nwse);


        if(nwse[0] != Entry.NONGEO)
            entry.setNorth(nwse[0]);
        if(nwse[1] != Entry.NONGEO)
            entry.setWest(nwse[1]);
        if(nwse[2] != Entry.NONGEO)
            entry.setSouth(nwse[2]);
        if(nwse[3] != Entry.NONGEO)
            entry.setEast(nwse[3]);
    }


    public static Element readKml(Repository repository, Entry entry) 
        throws Exception {
        Element kmlRoot = null;
        String path = entry.getFile().toString();
        if (path.toLowerCase().endsWith(".kmz")) {
            ZipInputStream zin = new ZipInputStream(repository.getStorageManager().getFileInputStream(path));
            ZipEntry       ze  = null;
            while ((ze = zin.getNextEntry()) != null) {
                String name = ze.getName().toLowerCase();
                if(name.toLowerCase().endsWith(".kml")) {
                    kmlRoot  = XmlUtil.getRoot(new String(IOUtil.readBytes(zin)));
                    break;
                }
            }
            IOUtil.close(zin);
        } else {
            kmlRoot = XmlUtil.getRoot(repository.getStorageManager().readSystemResource(entry.getFile()));
        }
        return kmlRoot;
    } 


    private void initializeEntry(Entry entry, Element node, double[]nwse) {
        String tagName = node.getTagName();
        if(tagName.equals(KmlUtil.TAG_FOLDER) || tagName.equals(KmlUtil.TAG_KML) || tagName.equals(KmlUtil.TAG_DOCUMENT)) {
            NodeList    children = XmlUtil.getElements(node);
            for (int i = 0; i < children.getLength(); i++) {
                Element child = (Element)  children.item(i);
                initializeEntry(entry, child, nwse);
            }
            return;
        }


        if(tagName.equals(KmlUtil.TAG_GROUNDOVERLAY)) {
            Element llbox  = XmlUtil.findChild(node, KmlUtil.TAG_LATLONBOX);
            if(llbox!=null) {
                setNorth(nwse,convert(XmlUtil.getGrandChildText(llbox, KmlUtil.TAG_NORTH,null),Entry.NONGEO));
                setWest(nwse, convert(XmlUtil.getGrandChildText(llbox, KmlUtil.TAG_WEST,null),Entry.NONGEO));
                setSouth(nwse, convert(XmlUtil.getGrandChildText(llbox, KmlUtil.TAG_SOUTH,null),Entry.NONGEO));
                setEast(nwse,convert(XmlUtil.getGrandChildText(llbox, KmlUtil.TAG_EAST,null),Entry.NONGEO));
            } else {
                System.err.println("no  latlonbox:" + XmlUtil.toString(node));
            }
            return;
        }

        if(tagName.equals(KmlUtil.TAG_PLACEMARK)) {
            List<Element> coords  = ( List<Element>)XmlUtil.findDescendants(node, KmlUtil.TAG_COORDINATES);
            for(Element coordNode: coords) {
                setBounds(nwse, XmlUtil.getChildText(coordNode));
            }
            if(coords.size()>0) return;
            System.err.println("no  coords:" + XmlUtil.toString(node));
            return;
        } 

        //        System.err.println("Unknown:" + tagName);
    }

    private void setBounds(double[]nwse, String coordString) {
        if(coordString!=null) {
            double[][]coords = KmlUtil.parseCoordinates(coordString);
            for(int i=0;i<coords[0].length;i++) {
                double lat = coords[1][i];
                double lon = coords[0][i];
                setBounds(nwse, lat, lon);
            }
        }
    }



    private double convert(String value, double dflt) {
        if(value == null) return dflt;
        return Double.parseDouble(value);
    }

    private void setBounds(double[]nwse, double lat, double lon) {
        setLat(nwse,lat);
        setLon(nwse,lon);
    }

    private void setLon(double[]nwse, double lon) {
        nwse[1] = nwse[1]==Entry.NONGEO?lon:Math.min(nwse[1],lon);
        nwse[3] = nwse[3]==Entry.NONGEO?lon:Math.max(nwse[3],lon);
    }


    private void setLat(double[]nwse, double lat) {
        nwse[0] = nwse[0]==Entry.NONGEO?lat:Math.max(nwse[0],lat);
        nwse[2] = nwse[2]==Entry.NONGEO?lat:Math.min(nwse[2],lat);
    }


    private void setNorth(double[]nwse, double lat) {
        nwse[0] = nwse[0]==Entry.NONGEO?lat:Math.max(nwse[0],lat);
    }

    private void setSouth(double[]nwse, double lat) {
        nwse[2] = nwse[2]==Entry.NONGEO?lat:Math.min(nwse[2],lat);
    }


    private void setWest(double[]nwse, double lon) {
        nwse[1] = nwse[1]==Entry.NONGEO?lon:Math.min(nwse[1],lon);
    }

    private void setEast(double[]nwse, double lon) {
        nwse[3] = nwse[3]==Entry.NONGEO?lon:Math.max(nwse[3],lon);
    }

    /**
     */
    @Override
        public boolean addToMap(Request request, Entry entry, MapInfo map)     {
        map.addKmlUrl(getEntryManager().getEntryResourceUrl(request, entry, false));
        return true;
    }




}
