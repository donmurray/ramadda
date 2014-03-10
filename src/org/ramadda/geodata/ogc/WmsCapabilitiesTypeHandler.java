/*
* Copyright 2008-2014 Geode Systems LLC
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

package org.ramadda.geodata.ogc;


import org.ramadda.repository.*;
import org.ramadda.repository.metadata.Metadata;
import org.ramadda.repository.output.*;
import org.ramadda.repository.type.*;
import org.ramadda.util.HtmlUtils;

import org.w3c.dom.*;

import ucar.unidata.util.IOUtil;

import ucar.unidata.util.WmsUtil;
import ucar.unidata.xml.XmlUtil;

import java.io.*;

import java.util.ArrayList;
import java.util.Hashtable;
import java.util.Date;


import java.util.List;


/**
 * A place holder class that provides services for WMS URL entry types.
 * Right now this does nothing but we could use it to provide a new defalt html display
 */
public class WmsCapabilitiesTypeHandler extends ExtensibleGroupTypeHandler {


    /**
     * ctor
     *
     * @param repository the repository
     * @param node the types.xml node
     * @throws Exception On badness
     */
    public WmsCapabilitiesTypeHandler(Repository repository, Element node)
            throws Exception {
        super(repository, node);
    }



    /**
     * _more_
     *
     * @param entry _more_
     *
     * @throws Exception _more_
     */
    public void initializeNewEntry(Entry entry) throws Exception {
        super.initializeNewEntry(entry);
        String      url  = entry.getResource().getPath();
        InputStream fis  = getStorageManager().getInputStream(url);
        Element     root = XmlUtil.getRoot(fis);
        IOUtil.close(fis);
        String version = XmlUtil.getAttribute(root, WmsUtil.ATTR_VERSION, "1.3.0");
        String format= "image/png";
        //Assume epsg4326
        String srs = "EPSG:4326";
        

        Element service = XmlUtil.findChild(root, "Service");
        if (service == null) {
            logError("WMS: No service node", null);
            return;
        }



        entry.setName(XmlUtil.getGrandChildText(service, WmsUtil.TAG_TITLE,
                entry.getName()));
        if (entry.getDescription().length() == 0) {
            entry.setDescription(XmlUtil.getGrandChildText(service,
                    "Abstract", entry.getDescription()));
        }
        addKeywords(entry, service);
        Element capabilityNode = XmlUtil.findChild(root, WmsUtil.TAG_CAPABILITY);
        if(capabilityNode == null) {
            logError("WMS: No capability node", null);
            return;
        }
        Element getMap = XmlUtil.findDescendantFromPath(capabilityNode, "Request.GetMap");
        if(getMap == null) {
            logError("WMS: No getMap node", null);
            return;
        }

        Element onlineResource = XmlUtil.findDescendantFromPath(getMap, "DCPType.HTTP.Get.OnlineResource");
        if(onlineResource == null) {
            logError("WMS: No onlineReslource node", null);
            return;
        }
                
        String getMapUrl = XmlUtil.getAttribute(onlineResource, "xlink:href");
        if(getMapUrl.indexOf("?") <0) {
            getMapUrl += "?";
        } else {
            getMapUrl += "&";
        }
        getMapUrl += "version=" + version;

        getMapUrl += "&request=GetMap";

        List<Entry> children = new ArrayList<Entry>();
        entry.putProperty("entries", children);

        List layers = XmlUtil.findDescendants(root, "Layer");
        TypeHandler layerTypeHandler =
            getRepository().getTypeHandler("type_wms_layer");

        for (int i = 0; i < layers.size(); i++) {
            Element layer = (Element) layers.get(i);
            Element nameNode = XmlUtil.findChild(layer, WmsUtil.TAG_NAME);
            if(nameNode == null) {
                continue;
            }
            
            String name = XmlUtil.getChildText(nameNode); 
            String title  = XmlUtil.getGrandChildText(layer, WmsUtil.TAG_TITLE, name);

            String imageUrl = getMapUrl;
            imageUrl += "&" + HtmlUtils.arg("layers", name, true);
            imageUrl += "&" + HtmlUtils.arg("format", format, true);
            imageUrl += "&" + HtmlUtils.arg("SRS", srs, true);

            //<BoundingBox CRS="EPSG:4326" minx="-180.0" miny="-90" maxx="180.0" maxy="90"/>
            Element bbox =     XmlUtil.findChildRecurseUp(layer,  WmsUtil.TAG_BOUNDINGBOX);
            //Must we always have a bbox
            if(bbox ==null) {
                //                logError("WMS: No BBOX specified: " + XmlUtil.toString(layer),  null);
                System.err.println("WMS: No BBOX specified: " + XmlUtil.toString(layer));
                continue;
            }
            String minx = XmlUtil.getAttribute(bbox, "minx");
            String maxx = XmlUtil.getAttribute(bbox, "maxx");
            String miny = XmlUtil.getAttribute(bbox, "miny");
            String maxy = XmlUtil.getAttribute(bbox, "maxy");
            //Don't encode the args
            imageUrl += "&" + HtmlUtils.arg("BBOX",minx +"," + miny +"," + maxx +"," + maxy, false);
            imageUrl += "&width=400&height=400";

            System.err.println("name:" + name + " title:" + title);            
            System.err.println("url:" + imageUrl);
            Resource resource = new Resource(imageUrl, Resource.TYPE_URL);
            Date now  = new Date();
            Date date  = now;

            //The  values array corresponds  to the column defs in wmstypes.xml
            Object[] values =
                layerTypeHandler.makeEntryValues(new Hashtable());
            values[0] = getMapUrl;
            values[1] = name;
            values[2] = version;
            values[3] = srs;
            values[4] = format;
            

            Entry layerEntry = layerTypeHandler.createEntry(
                                                            getRepository().getGUID());

            
            layerEntry.initEntry(title, "",
                                 entry, entry.getUser(),
                                 resource, "", now.getTime(),
                                 now.getTime(), date.getTime(),
                                 date.getTime(), values);
            children.add(layerEntry);
            
        }

    }

    /**
     * _more_
     *
     * @param request _more_
     * @param entry _more_
     */
    @Override
    public void doFinalEntryInitialization(Request request, Entry entry) {
        try {
            super.doFinalEntryInitialization(request, entry);
            List<Entry> childrenEntries =
                (List<Entry>) entry.getProperty("entries");
            if (childrenEntries == null) {
                return;
            }
            getEntryManager().addNewEntries(request,  childrenEntries);


        } catch (Exception exc) {
            throw new RuntimeException(exc);
        }
    }



    /**
     * _more_
     *
     * @param entry _more_
     * @param service _more_
     *
     * @throws Exception _more_
     */
    private void addKeywords(Entry entry, Element service) throws Exception {
        Element keyWords = XmlUtil.findChild(service, "KeywordList");
        if (keyWords != null) {
            List children = XmlUtil.findChildren(keyWords, "Keyword");
            for (int i = 0; i < children.size(); i++) {
                String text = XmlUtil.getChildText((Element) children.get(i));
                entry.addMetadata(new Metadata(getRepository().getGUID(),
                        entry.getId(), "content.keyword", false, text, "",
                        "", "", ""));

            }
        }
    }




}
