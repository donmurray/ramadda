/*
* Copyright 2008-2013 Geode Systems LLC
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

import ucar.unidata.util.WmsUtil;
import ucar.unidata.util.IOUtil;
import ucar.unidata.xml.XmlUtil;
import java.io.*;

import org.w3c.dom.*;

import java.util.List;
import java.util.ArrayList;

/**
 * A place holder class that provides services for WMS URL entry types.
 * Right now this does nothing but we could use it to provide a new defalt html display
 */
public class WmsTypeHandler extends ExtensibleGroupTypeHandler {


    /**
     * ctor
     *
     * @param repository the repository
     * @param node the types.xml node
     * @throws Exception On badness
     */
    public WmsTypeHandler(Repository repository, Element node)
            throws Exception {
        super(repository, node);
    }



    public void initializeNewEntry(Entry entry)
            throws Exception {
        super.initializeNewEntry(entry);
        String url = entry.getResource().getPath();
        InputStream fis =  getStorageManager().getFileInputStream(url);
        Element root = XmlUtil.getRoot(fis);
        IOUtil.close(fis);
        Element service = XmlUtil.findChild(root, "Service");
        if(service == null) return;
        entry.setName(XmlUtil.getGrandChildText(service, WmsUtil.TAG_TITLE,entry.getName()));
        if(entry.getDescription().length()==0) {
            entry.setDescription(XmlUtil.getGrandChildText(service, "Abstract",entry.getDescription()));
        }
        addKeywords(entry, service);
        List<Entry> children = new ArrayList<Entry>();
        entry.putProperty("entries", children);
        List layers =   XmlUtil.findDescendants(root, "Layer");
        TypeHandler layerTypeHandler = getRepository().getTypeHandler("type_wms_layer");

        for(int i=0;i<layers.size();i++) {
            Element layer = (Element) layers.get(i);
            
            
        }

    }

    @Override
    public void doFinalEntryInitialization(Request request, Entry entry)  {
        try {
            super.doFinalEntryInitialization(request, entry);
            List<Entry> childrenEntries = (List<Entry>) entry.getProperty("entries");
            if(childrenEntries == null) return;
            
        
        } catch (Exception exc) {
            throw new RuntimeException(exc);
        }
    }



    private void addKeywords(Entry entry, Element service) throws Exception {
        Element keyWords = XmlUtil.findChild(service, "KeywordList");
        if(keyWords!=null) {
            List children = XmlUtil.findChildren(keyWords, "Keyword");
            for(int i=0;i<children.size();i++) {
                String text = XmlUtil.getChildText((Element) children.get(i));
                entry.addMetadata(new Metadata(getRepository().getGUID(),
                                               entry.getId(), "content.keyword", false, text,
                                               "", "", "", ""));
                    
            }
        }
    }




}
