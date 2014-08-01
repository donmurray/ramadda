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
import org.ramadda.util.HtmlUtils;
import org.ramadda.util.Utils;

import ucar.unidata.util.IOUtil;
import ucar.unidata.util.TwoFacedObject;

import java.awt.geom.*;

import java.io.InputStream;

import java.net.URL;

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
    public static final String TAG_SERVICES = "services";

    /** _more_ */
    public static final String TAG_FOLDERS = "folders";

    /** _more_ */
    public static final String TAG_URL = "url";

    /** _more_ */
    public static final String TAG_DESCRIPTION = "description";

    /** _more_ */
    public static final String TAG_SERVICEDESCRIPTION = "serviceDescription";

    /** _more_ */
    public static final String TAG_FULLEXTENT = "fullExtent";

    /** _more_ */
    public static final String TAG_SPATIALREFERENCE = "spatialReference";

    /** _more_ */
    public static final String TAG_WKID = "wkid";


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
     * @param theUrl _more_
     * @param parentEntry _more_
     * @return _more_
     *
     * @throws Exception _more_
     */
    @Override
    public Result handleUrlRequest(final Request request,
                                   Repository repository,
                                   final String theUrl,
                                   final Entry parentEntry)
            throws Exception {
        if ( !request.getString(ARG_IMPORT_TYPE, "").equals(TYPE_ESRI)) {
            return null;
        }

        ActionManager.Action action = new ActionManager.Action() {
            public void run(Object actionId) throws Exception {
                String       url     = theUrl;
                StringBuffer sb      = new StringBuffer();
                List<Entry>  entries = new ArrayList<Entry>();
                String[]     toks    = url.split("\\?");
                url = toks[0];
                if (url.endsWith("/")) {
                    url = url.substring(0, url.length() - 1);
                }

                //Make a top-level entry
                String topName = new URL(url).getHost() + " rest services";
                Entry topEntry = makeEntry(request, parentEntry,
                                           "type_esri_restfolder", topName,
                                           url);
                entries.add(topEntry);

                processServiceList(request, topEntry, actionId, entries, url,
                                   url);

                if ( !okToContinue(actionId,
                                   "Processed " + entries.size()
                                   + " entries")) {
                    return;
                }
                for (Entry entry : entries) {
                    entry.setUser(request.getUser());
                }
                getEntryManager().addNewEntries(request, entries);
                getActionManager().setContinueHtml(
                    actionId,
                    entries.size() + " entries created" + HtmlUtils.br()
                    + HtmlUtils.href(
                        request.url(
                            getRepository().URL_ENTRY_SHOW, ARG_ENTRYID,
                            topEntry.getId()), "Continue"));

            }
        };

        return getActionManager().doAction(request, action,
                                           "Importing the services", "",
                                           parentEntry);


    }


    /**
     * _more_
     *
     * @param url _more_
     *
     * @return _more_
     *
     * @throws Exception _more_
     */
    private JSONTokener getTokenizer(String url) throws Exception {
        url = url + "?f=pjson";
        String      json = IOUtil.readContents(url.toString(), getClass());
        JSONTokener tokenizer = new JSONTokener(json);

        return tokenizer;
    }

    /**
     * _more_
     *
     *
     *
     * @param request _more_
     * @param parentEntry _more_
     * @param actionId _more_
     * @param entries _more_
     * @param baseUrl _more_
     * @param serviceUrl _more_
     * @throws Exception _more_
     */
    private void processServiceList(Request request, Entry parentEntry,
                                    Object actionId, List<Entry> entries,
                                    String baseUrl, String serviceUrl)
            throws Exception {

        if ( !okToContinue(actionId,
                           "Processed " + entries.size() + " entries")) {
            return;
        }

        System.err.println("EsriServiceImporter: url: " + serviceUrl);
        JSONObject obj = new JSONObject(getTokenizer(serviceUrl));

        if ( !okToContinue(actionId,
                           "Processed " + entries.size() + " entries")) {
            return;
        }

        if (obj.has(TAG_FOLDERS)) {
            JSONArray folders = obj.getJSONArray(TAG_FOLDERS);
            for (int i = 0; i < folders.length(); i++) {
                if ( !okToContinue(actionId,
                                   "Processed " + entries.size()
                                   + " entries")) {
                    return;
                }

                String folder = folders.getString(i);
                System.err.println("EsriServiceImporter: making folder:"
                                   + folder);
                String url = baseUrl + "/" + folder;
                Entry folderEntry = makeEntry(request, parentEntry,
                                        "type_esri_restfolder",
                                        getNameFromId(folder), url);
                entries.add(folderEntry);
                processServiceList(request, folderEntry, actionId, entries,
                                   baseUrl, url);
            }
        }


        if (obj.has(TAG_SERVICES)) {
            JSONArray services = obj.getJSONArray(TAG_SERVICES);
            for (int i = 0; i < services.length(); i++) {
                JSONObject service = services.getJSONObject(i);
                if ( !okToContinue(actionId,
                                   "Processed " + entries.size()
                                   + " entries")) {
                    return;
                }
                processService(request, parentEntry, actionId, entries,
                               baseUrl, service);
            }
        }
    }

    /**
     * _more_
     *
     * @param request _more_
     * @param parentEntry _more_
     * @param type _more_
     * @param name _more_
     * @param url _more_
     *
     * @return _more_
     *
     * @throws Exception _more_
     */
    private Entry makeEntry(Request request, Entry parentEntry, String type,
                            String name, String url)
            throws Exception {
        Entry entry = getRepository().getTypeHandler(type).createEntry(
                          getRepository().getGUID());
        Date now = new Date();
        entry.setResource(new Resource(url, Resource.TYPE_URL));
        entry.setCreateDate(now.getTime());
        entry.setChangeDate(now.getTime());
        entry.setStartDate(now.getTime());
        entry.setEndDate(now.getTime());
        entry.setName(name);
        entry.setParentEntryId(parentEntry.getId());

        return entry;

    }


    /**
     * _more_
     *
     * @param actionId _more_
     * @param message _more_
     *
     * @return _more_
     */
    private boolean okToContinue(Object actionId, String message) {
        if ( !getRepository().getActionManager().getActionOk(actionId)) {
            return false;
        }
        if (message != null) {
            getActionManager().setActionMessage(actionId, message);
        }

        return true;
    }


    /**
     * _more_
     *
     * @param id _more_
     *
     * @return _more_
     */
    private String getNameFromId(String id) {
        String[] toks = id.split("/");

        return toks[toks.length - 1];
    }


    /**
     * _more_
     *
     *
     * @param request _more_
     * @param parentEntry _more_
     * @param actionId _more_
     * @param entries _more_
     * @param baseUrl _more_
     * @param service _more_
     *
     * @throws Exception _more_
     */
    private void processService(Request request, Entry parentEntry,
                                Object actionId, List<Entry> entries,
                                String baseUrl, JSONObject service)
            throws Exception {
        String id   = service.getString("name");
        String type = service.getString("type");
        String url  = null;

        //        http://services.nationalmap.gov/arcgis/rest/services?f=pjson
        if (service.has(TAG_URL)) {
            url = service.getString(TAG_URL);
        } else {
            url = baseUrl + "/" + id + "/" + type;
        }
        System.err.println("EsriServiceImporter.processService:" + url);

        JSONObject obj  = new JSONObject(getTokenizer(url));
        String     name = null;
        if (obj.has(TAG_SERVICEDESCRIPTION)) {
            obj.getString(TAG_SERVICEDESCRIPTION);
        }
        if ( !Utils.stringDefined(name)) {
            name = getNameFromId(id);
        }
        String description = "";
        if (obj.has(TAG_DESCRIPTION)) {
            description = obj.getString(TAG_DESCRIPTION);
        } else if (obj.has(TAG_SERVICEDESCRIPTION)) {
            description = obj.getString(TAG_SERVICEDESCRIPTION);
        }





        if (name.length() > Entry.MAX_NAME_LENGTH) {
            name = name.substring(0, 195) + "...";
        }


        String entryType = "type_esri_restservice";
        if (type.equals("FeatureServer")) {
            entryType = "type_esri_featureserver";
        } else if (type.equals("MapServer")) {
            entryType = "type_esri_mapserver";
        } else if (type.equals("ImageServer")) {
            entryType = "type_esri_imageserver";
        } else if (type.equals("GPServer")) {
            entryType = "type_esri_gpserver";
        } else if (type.equals("GeometryServer")) {
            entryType = "type_esri_geometryserver";
        }

        Entry entry = makeEntry(request, parentEntry, entryType, name, url);

        if (obj.has(TAG_FULLEXTENT)) {
            JSONObject extent = obj.getJSONObject(TAG_FULLEXTENT);
            if (extent.has(TAG_SPATIALREFERENCE)) {
                JSONObject spatialReference =
                    extent.getJSONObject(TAG_SPATIALREFERENCE);
                if (spatialReference.has(TAG_WKID)) {
                    String   wkid   = spatialReference.get(TAG_WKID) + "";
                    double[] bounds = getBounds(extent, wkid);
                    if ((bounds != null) && (Math.abs(bounds[0]) < 360)
                            && (Math.abs(bounds[1]) < 360)
                            && (Math.abs(bounds[2]) < 360)
                            && (Math.abs(bounds[3]) < 360)) {
                        entry.setNorth(bounds[0]);
                        entry.setWest(bounds[1]);
                        entry.setSouth(bounds[2]);
                        entry.setEast(bounds[3]);
                        //                        System.err.println("bounds:" + bounds[0] +" " + bounds[1] +" " + bounds[2] + " " + bounds[3]);
                    }
                }
            }
        }



        Object[] values = entry.getTypeHandler().getEntryValues(entry);
        values[0] = id;
        entries.add(entry);
    }


    /**
     * _more_
     *
     * @param extent _more_
     * @param wkid _more_
     *
     * @return _more_
     *
     * @throws Exception _more_
     */
    private double[] getBounds(JSONObject extent, String wkid)
            throws Exception {
        double                         xmin         =
            extent.getDouble("xmin");
        double                         xmax         =
            extent.getDouble("xmax");
        double                         ymin         =
            extent.getDouble("ymin");
        double                         ymax         =
            extent.getDouble("ymax");
        com.jhlabs.map.proj.Projection jhProjection = null;
        double                         scale        = 1.0;
        if (wkid.equals("102006")) {
            //            System.err.println ("Changed to proj4:" + wkid);
            //+proj=aea +lat_1=50 +lat_2=70 +lat_0=40 +lon_0=-96 +x_0=0 +y_0=0 +datum=NAD83 +units=m +no_defs 
            //            scale = 0.017453292519943295;
            jhProjection =
                com.jhlabs.map.proj.ProjectionFactory.fromPROJ4Specification(
                    new String[] {
                "+proj=aea", "+lat_1=55", "+lat_2=65", "+lat_0=50",
                "+lon_0=-154", "+x_0=0", "+y_0=0", "+ellps=GRS80",
                "+datum=NAD83", "+units=m", "+no_defs"
            });
        }

        if (wkid.equals("4326")) {}
        else if (jhProjection == null) {
            try {
                jhProjection =
                    com.jhlabs.map.proj.ProjectionFactory
                        .getNamedPROJ4CoordinateSystem(wkid);
                if (jhProjection != null) {
                    //                    System.err.println("Good projection:" + wkid);
                }
            } catch (Exception exc) {
                //                System.err.println("Bad projection:" + wkid+ " " + exc);
                return null;
            }
            if (jhProjection == null) {
                return null;
            }
        }

        if (jhProjection != null) {
            Point2D.Double dst = new Point2D.Double(0, 0);
            Point2D.Double src = new Point2D.Double(xmin, ymax);
            dst  = jhProjection.inverseTransform(src, dst);
            xmin = scale * src.getX();
            ymax = scale * src.getY();
            src  = new Point2D.Double(xmax, ymin);
            dst  = jhProjection.inverseTransform(src, dst);
            xmax = scale * src.getX();
            ymin = scale * src.getY();
        }
        double[] b = new double[] { ymax, xmin, ymin, xmax };
        if ((Math.abs(b[0]) < 360) && (Math.abs(b[1]) < 360)
                && (Math.abs(b[2]) < 360) && (Math.abs(b[3]) < 360)) {
            return b;
        }
        //        System.err.println("bad bounds:" + b[0] +" " + b[1] +" " + b[2] + " " + b[3]);

        return null;
    }
}
