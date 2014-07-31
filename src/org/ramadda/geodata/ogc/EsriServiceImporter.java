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


import org.json.*;

import org.ramadda.repository.*;
import org.ramadda.repository.metadata.*;
import org.ramadda.repository.metadata.Metadata;
import org.ramadda.util.Utils;

import ucar.unidata.util.IOUtil;
import ucar.unidata.util.TwoFacedObject;

import java.io.InputStream;

import java.util.ArrayList;

import java.util.Date;
import java.util.List;


/**
 * Class description
 *
 *
 * @version        $version$, Thu, Jul 31, '14
 * @author         Enter your name here...
 */
public class EsriServiceImporter extends ImportHandler {

    /** _more_ */
    public static final String TYPE_ESRI = "esriservice";


    /**
     * ctor
     *
     * @param repository _more_
     */
    public EsriServiceImporter(Repository repository) {
        super(repository);
    }


    /**
     * _more_
     *
     * @param importTypes _more_
     * @param formBuffer _more_
     */
    @Override
    public void addImportTypes(List<TwoFacedObject> importTypes,
                               Appendable formBuffer) {
        super.addImportTypes(importTypes, formBuffer);
        importTypes.add(new TwoFacedObject("Esri Rest Service Import",
                                           TYPE_ESRI));
    }


    /**
     * _more_
     *
     * @param request _more_
     * @param repository _more_
     * @param uploadedFile _more_
     * @param url _more_
     * @param parentEntry _more_
     *
     * @return _more_
     *
     * @throws Exception _more_
     */
    @Override
    public Result handleUrlRequest(Request request, Repository repository,
                                   String url, Entry parentEntry)
            throws Exception {
        if ( !request.getString(ARG_IMPORT_TYPE, "").equals(TYPE_ESRI)) {
            return null;
        }
        StringBuffer sb      = new StringBuffer();
        List<Entry>  entries = new ArrayList<Entry>();
        processServiceList(request, parentEntry, entries, url);
        for (Entry entry : entries) {
            entry.setUser(request.getUser());
        }
        getEntryManager().addNewEntries(request, entries);

        return new Result(request.entryUrl(getRepository().URL_ENTRY_SHOW, parentEntry));

    }



    /**
     * _more_
     *
     *
     * @param request _more_
     * @param parentEntry _more_
     * @param entries _more_
     * @param esriServiceUrl _more_
     *
     * @throws Exception _more_
     */
    private void processServiceList(Request request, Entry parentEntry,
                                    List<Entry> entries,
                                    String esriServiceUrl)
            throws Exception {
        InputStream is        = IOUtil.getInputStream(esriServiceUrl);
        JSONTokener tokenizer = new JSONTokener(is);
        JSONObject  obj       = new JSONObject(tokenizer);
        JSONArray   services  = obj.getJSONArray("services");
        for (int i = 0; i < services.length(); i++) {
            JSONObject service = services.getJSONObject(i);
            processService(request, parentEntry, entries, service);
        }
        IOUtil.close(is);
    }

    /**
     * _more_
     *
     *
     * @param request _more_
     * @param parentEntry _more_
     * @param entries _more_
     * @param service _more_
     *
     * @throws Exception _more_
     */
    private void processService(Request request, Entry parentEntry,
                                List<Entry> entries, JSONObject service)
            throws Exception {
        String      id        = service.getString("name");
        String      type      = service.getString("type");
        String      url       = service.getString("url");

        InputStream is        = IOUtil.getInputStream(url + "?f=pjson");
        JSONTokener tokenizer = new JSONTokener(is);
        JSONObject  obj       = new JSONObject(tokenizer);
        String      name      = obj.getString("serviceDescription");
        if ( !Utils.stringDefined(name)) {
            name = id;
        }
        String description = obj.getString("description");
        String wkid        = null;
        if (obj.has("fullExtent")) {
            JSONObject extent = obj.getJSONObject("fullExtent");
            if (extent.has("spatialReference")) {
                JSONObject spatialReference =
                    extent.getJSONObject("spatialReference");
                if (spatialReference.has("wkid")) {
                    wkid = spatialReference.get("wkid") + "";
                }
            }
        }

        /*        System.err.println(id + " " + name + " " + type + " " + url + "\n"
                           + description);
        */

        IOUtil.close(is);

        Entry entry = getRepository().getTypeHandler(
                          "type_esri_restservice").createEntry(
                          getRepository().getGUID());
        Object[] values = entry.getTypeHandler().getEntryValues(entry);

        values[0] = id;


        Date now = new Date();
        entry.setResource(new Resource(url, Resource.TYPE_URL));
        entry.setCreateDate(now.getTime());
        entry.setChangeDate(now.getTime());
        entry.setStartDate(now.getTime());
        entry.setEndDate(now.getTime());
        entry.setDescription(description);
        entry.setName(name);
        entry.setParentEntryId(parentEntry.getId());
        entries.add(entry);



    }


}
