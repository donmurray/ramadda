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

package org.ramadda.repository.metadata;


import org.ramadda.repository.*;
import org.ramadda.repository.auth.*;
import org.ramadda.repository.database.*;


import org.w3c.dom.*;




import ucar.unidata.sql.Clause;

import ucar.unidata.sql.SqlUtil;

import ucar.unidata.ui.ImageUtils;
import ucar.unidata.util.DateUtil;
import org.ramadda.util.HtmlUtils;

import ucar.unidata.util.IOUtil;
import ucar.unidata.util.LogUtil;
import ucar.unidata.util.Misc;


import ucar.unidata.util.StringUtil;
import ucar.unidata.util.TwoFacedObject;

import ucar.unidata.xml.XmlUtil;



import java.io.*;

import java.io.File;
import java.io.InputStream;

import java.lang.reflect.*;



import java.net.*;

import java.sql.ResultSet;
import java.sql.Statement;

import java.text.SimpleDateFormat;

import java.util.ArrayList;
import java.util.Date;
import java.util.Enumeration;
import java.util.GregorianCalendar;
import java.util.Hashtable;
import java.util.List;
import java.util.Properties;
import java.util.TimeZone;

import java.util.regex.*;
import java.util.zip.*;




/**
 *
 *
 * @author RAMADDA Development Team
 * @version $Revision: 1.3 $
 */
public class MetadataManager extends RepositoryManager {

    /** _more_ */
    private static final String SUFFIX_SELECT = ".select.";


    /** _more_ */
    private Object MUTEX_METADATA = new Object();


    /** _more_ */
    public RequestUrl URL_METADATA_FORM = new RequestUrl(getRepository(),
                                              "/metadata/form",
                                              "Edit Properties");

    /** _more_ */
    public RequestUrl URL_METADATA_LIST = new RequestUrl(getRepository(),
                                              "/metadata/list",
                                              "Property Listing");

    /** _more_ */
    public RequestUrl URL_METADATA_VIEW = new RequestUrl(getRepository(),
                                              "/metadata/view",
                                              "Property View");

    /** _more_ */
    public RequestUrl URL_METADATA_ADDFORM = new RequestUrl(getRepository(),
                                                 "/metadata/addform",
                                                 "Add Property");

    /** _more_ */
    public RequestUrl URL_METADATA_ADD = new RequestUrl(getRepository(),
                                             "/metadata/add");

    /** _more_ */
    public RequestUrl URL_METADATA_CHANGE = new RequestUrl(getRepository(),
                                                "/metadata/change");




    /** _more_ */
    private Hashtable<String, Hashtable<String, String>> metadataTypeToTemplate =
        new Hashtable<String, Hashtable<String, String>>();

    /** _more_ */
    protected Hashtable distinctMap = new Hashtable();

    /** _more_ */
    private List<MetadataHandler> metadataHandlers =
        new ArrayList<MetadataHandler>();

    /** _more_ */
    private Hashtable<Class, MetadataHandler> metadataHandlerMap =
        new Hashtable<Class, MetadataHandler>();


    /** _more_ */
    protected Hashtable<String, MetadataType> typeMap = new Hashtable<String,
                                                            MetadataType>();


    /** _more_ */
    private List<MetadataType> metadataTypes = new ArrayList<MetadataType>();


    /** _more_ */
    private List<String> tableNames = new ArrayList<String>();


    /**
     * _more_
     *
     *
     * @param repository _more_
     *
     */
    public MetadataManager(Repository repository) {
        super(repository);
    }


    /** _more_ */
    MetadataHandler dfltMetadataHandler;


    /**
     * _more_
     *
     * @param stringType _more_
     *
     * @return _more_
     */
    public MetadataType findType(String stringType) {
        return typeMap.get(stringType);
    }

    /**
     * _more_
     *
     * @param type _more_
     */
    public void addMetadataType(MetadataType type) {
        metadataTypes.add(type);
        typeMap.put(type.getId(), type);
        if (type.getHasDatabaseTable()) {
            tableNames.add(type.getTableName());
        }
    }


    /**
     * _more_
     *
     * @param metadataType _more_
     * @param templateType _more_
     * @param templateContents _more_
     */
    public void addTemplate(String metadataType, String templateType,
                            String templateContents) {
        Hashtable<String, String> templatesForType =
            metadataTypeToTemplate.get(metadataType);
        if (templatesForType == null) {
            templatesForType = new Hashtable<String, String>();
            metadataTypeToTemplate.put(metadataType, templatesForType);

        }
        templatesForType.put(templateType, templateContents);
    }

    /**
     * _more_
     *
     * @param metadataType _more_
     * @param templateType _more_
     *
     * @return _more_
     */
    public String getTemplate(String metadataType, String templateType) {
        Hashtable<String, String> templatesForType =
            metadataTypeToTemplate.get(metadataType);
        if (templatesForType == null) {
            return null;
        }
        return templatesForType.get(templateType);
    }


    /**
     * _more_
     *
     * @param request _more_
     * @param entry _more_
     * @param sb _more_
     * @param forLink _more_
     *
     * @throws Exception _more_
     */
    public void decorateEntry(Request request, Entry entry, StringBuffer sb,
                              boolean forLink)
            throws Exception {
        for (Metadata metadata : getMetadata(entry)) {
            MetadataHandler handler = findMetadataHandler(metadata.getType());
            handler.decorateEntry(request, entry, sb, metadata, forLink);
        }
    }


    /**
     * _more_
     *
     * @param entry _more_
     * @param sb _more_
     *
     * @throws Exception _more_
     */
    public void getTextCorpus(Entry entry, StringBuffer sb) throws Exception {
        for (Metadata metadata : getMetadata(entry)) {
            MetadataHandler handler = findMetadataHandler(metadata.getType());
            handler.getTextCorpus(entry, sb, metadata);
        }
    }


    /**
     * _more_
     *
     * @param oldEntry _more_
     * @param newEntry _more_
     * @param oldMetadata _more_
     *
     * @return _more_
     *
     * @throws Exception _more_
     */
    public Metadata copyMetadata(Entry oldEntry, Entry newEntry,
                                 Metadata oldMetadata)
            throws Exception {
        MetadataHandler handler = findMetadataHandler(oldMetadata.getType());
        return handler.copyMetadata(oldEntry, newEntry, oldMetadata);
    }



    /**
     * _more_
     *
     * @param request _more_
     * @param entry _more_
     * @param urls _more_
     *
     * @throws Exception _more_
     */
    public void getThumbnailUrls(Request request, Entry entry,
                                 List<String> urls)
            throws Exception {
        for (Metadata metadata : getMetadata(entry)) {
            MetadataHandler handler = findMetadataHandler(metadata.getType());
            handler.getThumbnailUrls(request, entry, urls, metadata);
        }
    }


    /**
     * _more_
     *
     * @param entry _more_
     * @param type _more_
     * @param checkInherited _more_
     *
     * @return _more_
     *
     * @throws Exception _more_
     */
    public List<Metadata> findMetadata(Entry entry, String type,
                                       boolean checkInherited)
            throws Exception {
        return findMetadata(entry, type, checkInherited, true);
    }


    /**
     * _more_
     *
     * @param entry _more_
     * @param type _more_
     * @param checkInherited _more_
     * @param firstOk _more_
     *
     * @return _more_
     *
     * @throws Exception _more_
     */
    public List<Metadata> findMetadata(Entry entry, String type,
                                       boolean checkInherited,
                                       boolean firstOk)
            throws Exception {
        List<Metadata> result = new ArrayList<Metadata>();
        findMetadata(entry, type, result, checkInherited, firstOk);
        if (result.size() == 0) {
            return null;
        }
        return result;
    }

    /**
     * _more_
     *
     * @param entry _more_
     *
     * @return _more_
     *
     * @throws Exception _more_
     */
    public List<Metadata> getInheritedMetadata(Entry entry) throws Exception {
        List<Metadata> result = new ArrayList<Metadata>();
        findInheritedMetadata(getEntryManager().getParent(null, entry),
                              result);
        return result;
    }

    /**
     * _more_
     *
     * @param entry _more_
     * @param result _more_
     *
     * @throws Exception _more_
     */
    private void findInheritedMetadata(Entry entry, List<Metadata> result)
            throws Exception {

        if (entry == null) {
            return;
        }
        for (Metadata metadata : getMetadata(entry)) {
            if ( !metadata.getInherited()) {
                continue;
            }

            result.add(metadata);
        }
        findInheritedMetadata(getEntryManager().getParent(null, entry),
                              result);
    }




    /**
     * _more_
     *
     * @param entry _more_
     * @param type _more_
     * @param result _more_
     * @param checkInherited _more_
     * @param firstTime _more_
     *
     * @throws Exception _more_
     */
    private void findMetadata(Entry entry, String type,
                              List<Metadata> result, boolean checkInherited,
                              boolean firstTime)
            throws Exception {

        if (entry == null) {
            return;
        }
        for (Metadata metadata : getMetadata(entry)) {
            if ( !firstTime && !metadata.getInherited()) {
                continue;
            }
            if (type != null) {
                if (metadata.getType().equals(type)) {
                    result.add(metadata);
                }
            } else {
                result.add(metadata);
            }
        }
        if (checkInherited) {
            findMetadata(getEntryManager().getParent(null, entry), type,
                         result, checkInherited, false);
        }
    }


    /**
     * _more_
     *
     * @param entry _more_
     * @param id _more_
     *
     * @return _more_
     *
     * @throws Exception _more_
     */
    public Metadata findMetadata(Entry entry, String id) throws Exception {
        if (entry == null) {
            return null;
        }
        for (Metadata metadata : getMetadata(entry)) {
            if (metadata.getId().equals(id)) {
                return metadata;
            }
        }
        return null;
    }


    /**
     * _more_
     *
     *
     * @param entry _more_
     *
     * @return _more_
     *
     * @throws Exception _more_
     */
    public List<Metadata> getMetadata(Entry entry) throws Exception {
        if (entry.isDummy()) {
            return new ArrayList<Metadata>();
        }
        List<Metadata> metadataList = entry.getMetadata();
        if (metadataList != null) {
            return metadataList;
        }

        metadataList = new ArrayList();
        Statement stmt =
            getDatabaseManager().select(
                Tables.METADATA.COLUMNS, Tables.METADATA.NAME,
                Clause.eq(Tables.METADATA.COL_ENTRY_ID, entry.getId()),
                " order by " + Tables.METADATA.COL_TYPE);
        SqlUtil.Iterator iter = getDatabaseManager().getIterator(stmt);
        ResultSet        results;
        while ((results = iter.getNext()) != null) {
            int             col     = 1;
            String          type    = results.getString(3);
            MetadataHandler handler = findMetadataHandler(type);

            metadataList.add(handler.makeMetadata(results.getString(col++),
                    results.getString(col++), results.getString(col++),
                    results.getInt(col++) == 1, results.getString(col++),
                    results.getString(col++), results.getString(col++),
                    results.getString(col++), results.getString(col++)));
        }

        entry.setMetadata(metadataList);
        return metadataList;
    }



    /**
     * _more_
     *
     * @param request _more_
     * @param entry _more_
     * @param extra _more_
     * @param shortForm _more_
     *
     * @return _more_
     */
    public List<Metadata> getInitialMetadata(Request request, Entry entry,
                                             Hashtable extra,
                                             boolean shortForm) {
        List<Metadata> metadataList = new ArrayList<Metadata>();
        for (MetadataHandler handler : getMetadataHandlers()) {
            handler.getInitialMetadata(request, entry, metadataList, extra,
                                       shortForm);
        }
        return metadataList;
    }


    /**
     * _more_
     *
     * @param request _more_
     * @param entry _more_
     * @param extra _more_
     * @param shortForm _more_
     *
     * @return _more_
     */
    public boolean addInitialMetadata(Request request, Entry entry,
                                      Hashtable extra, boolean shortForm) {
        boolean changed = false;
        for (Metadata metadata :
                getInitialMetadata(request, entry, extra, shortForm)) {
            if (entry.addMetadata(metadata, true)) {
                changed = true;
            }
        }
        if (extra.size() > 0) {
            changed = true;
        }
        return changed;
    }


    /**
     * _more_
     *
     * @param request _more_
     * @param entry _more_
     * @param zos _more_
     * @param doc _more_
     * @param parent _more_
     *
     * @throws Exception _more_
     */
    public void addMetadata(Request request, Entry entry,
                            ZipOutputStream zos, Document doc, Element parent)
            throws Exception {
        List<Metadata> metadataList = getMetadata(entry);
        for (Metadata metadata : metadataList) {
            MetadataHandler metadataHandler = findMetadataHandler(metadata);
            if (metadataHandler == null) {
                continue;
            }
            metadataHandler.addMetadata(request, entry, zos, metadata,
                                        parent);

        }
    }









    /**
     * _more_
     *
     * @return _more_
     */
    public List<MetadataHandler> getMetadataHandlers() {
        return metadataHandlers;
    }




    /**
     * _more_
     *
     * @param metadata _more_
     *
     * @return _more_
     *
     * @throws Exception _more_
     */
    public MetadataHandler findMetadataHandler(Metadata metadata)
            throws Exception {
        for (MetadataHandler handler : metadataHandlers) {
            if (handler.canHandle(metadata)) {
                return handler;
            }
        }
        if (dfltMetadataHandler == null) {
            dfltMetadataHandler = new MetadataHandler(getRepository(), null);
        }
        return dfltMetadataHandler;
    }



    /**
     * _more_
     *
     * @param type _more_
     *
     * @return _more_
     *
     * @throws Exception _more_
     */
    public MetadataHandler findMetadataHandler(String type) throws Exception {
        for (MetadataHandler handler : metadataHandlers) {
            if (handler.canHandle(type)) {
                return handler;
            }
        }
        if (dfltMetadataHandler == null) {
            dfltMetadataHandler = new MetadataHandler(getRepository(), null);
        }
        return dfltMetadataHandler;
    }


    /**
     * _more_
     *
     * @param c _more_
     *
     * @return _more_
     *
     * @throws Exception _more_
     */
    public MetadataHandler getHandler(Class c) throws Exception {
        MetadataHandler handler = metadataHandlerMap.get(c);
        if (handler == null) {
            Constructor ctor = Misc.findConstructor(c,
                                   new Class[] { Repository.class });
            if (ctor == null) {
                throw new IllegalStateException(
                    "Could not find constructor for MetadataHandler:"
                    + c.getName());
            }

            handler = (MetadataHandler) ctor.newInstance(new Object[] {
                getRepository() });

            metadataHandlers.add(handler);
            metadataHandlerMap.put(c, handler);
        }
        return handler;
    }


    /**
     * _more_
     *
     *
     * @param metadataDefFiles _more_
     *
     * @param pluginManager _more_
     * @throws Exception _more_
     */
    public void loadMetadataHandlers(PluginManager pluginManager)
            throws Exception {
        List<String> metadataDefFiles =
            getRepository().getPluginManager().getMetadataDefFiles();
        for (String file : metadataDefFiles) {
            try {
                file = getStorageManager().localizePath(file);
                if (pluginManager.haveSeen(file)) {
                    continue;
                }
                Element root = XmlUtil.getRoot(file, getClass());
                if (root == null) {
                    continue;
                }
                MetadataType.parse(root, this);
            } catch (Exception exc) {
                logError("Error loading metadata handler file:" + file, exc);
                throw exc;
            }

        }
    }



    /**
     * _more_
     *
     * @param request _more_
     * @param sb _more_
     *
     *
     * @return _more_
     * @throws Exception _more_
     */
    public StringBuffer addToSearchForm(Request request, StringBuffer sb)
            throws Exception {
        for (MetadataType type : metadataTypes) {
            if ( !type.getSearchable()) {
                continue;
            }
            type.getHandler().addToSearchForm(request, sb, type);
        }
        return sb;
    }


    public List<Metadata> getMetadataFromClipboard(Request request) throws Exception {
        List<Metadata> metadata = (List<Metadata> ) getSessionManager().getSessionProperty(request, PROP_METADATA);
        return metadata;
    }

    public void copyMetadataToClipboard(Request request, List<Metadata> metadataList) throws Exception {
        List<Metadata> copies = new ArrayList<Metadata>();
        for(Metadata metadata: metadataList) {
            copies.add(new Metadata(metadata));
        }
        getSessionManager().putSessionProperty(request, PROP_METADATA, copies);
    }


    /**
     * _more_
     *
     * @param request _more_
     * @param sb _more_
     *
     * @return _more_
     *
     * @throws Exception _more_
     */
    public StringBuffer addToBrowseSearchForm(Request request,
            StringBuffer sb)
            throws Exception {
        for (MetadataType type : metadataTypes) {
            if ( !type.getBrowsable()) {
                continue;
            }
            type.getHandler().addToBrowseSearchForm(request, sb, type);
        }
        return sb;
    }



    /**
     * _more_
     *
     * @param entry _more_
     * @param entryChild _more_
     * @param fileMap _more_
     * @param internal _more_
     *
     * @throws Exception _more_
     */
    public void processMetadataXml(Entry entry, Element entryChild,
                                   Hashtable fileMap, boolean internal)
            throws Exception {
        String          type    = XmlUtil.getAttribute(entryChild, ATTR_TYPE);
        MetadataHandler handler = findMetadataHandler(type);
        handler.processMetadataXml(entry, entryChild, fileMap, internal);
    }

    /**
     * _more_
     *
     * @param entry _more_
     *
     * @throws Exception _more_
     */
    public void newEntry(Entry entry) throws Exception {
        for (Metadata metadata : getMetadata(entry)) {
            MetadataHandler handler = findMetadataHandler(metadata.getType());
            handler.newEntry(metadata, entry);
        }
    }



    /**
     * _more_
     *
     * @param request _more_
     *
     * @return _more_
     *
     * @throws Exception _more_
     */
    public Result processMetadataChange(Request request) throws Exception {
        synchronized (MUTEX_METADATA) {
            Entry entry = getEntryManager().getEntry(request);
            Entry parent = getEntryManager().getParent(request, entry);
            boolean canEditParent =
                (parent != null)
                && getAccessManager().canDoAction(request, parent,
                    Permission.ACTION_EDIT);


            if (request.exists(ARG_METADATA_DELETE)) {
                Hashtable args = request.getArgs();
                for (Enumeration keys =
                        args.keys(); keys.hasMoreElements(); ) {
                    String arg = (String) keys.nextElement();
                    if ( !arg.startsWith(ARG_METADATA_ID + SUFFIX_SELECT)) {
                        continue;
                    }
                    getDatabaseManager().delete(Tables.METADATA.NAME,
                            Clause.eq(Tables.METADATA.COL_ID,
                                      request.getString(arg, BLANK)));
                }
            } else {
                List<Metadata> newMetadataList  = new ArrayList<Metadata>();
                List<Metadata> existingMetadata = getMetadata(entry);
                Hashtable<String, Metadata> map = new Hashtable<String,
                                                      Metadata>();
                for (Metadata metadata : existingMetadata) {
                    map.put(metadata.getId(), metadata);
                }

                for (MetadataHandler handler : metadataHandlers) {
                    handler.handleFormSubmit(request, entry, map,
                                             newMetadataList);
                }

                if(!request.isAnonymous()
                   && request.exists(ARG_METADATA_CLIPBOARD_COPY)) {
                    List<Metadata> toCopy = new ArrayList<Metadata>();
                    for (Metadata metadata : newMetadataList) {
                        if (request.defined(ARG_METADATA_ID + SUFFIX_SELECT
                                            + metadata.getId())) {
                            toCopy.add(metadata);
                        }
                    }
                    copyMetadataToClipboard(request, toCopy);
                }


                if (canEditParent
                        && request.exists(ARG_METADATA_ADDTOPARENT)) {
                    List<Metadata> parentMetadataList = getMetadata(parent);
                    int            cnt                = 0;

                    for (Metadata metadata : newMetadataList) {
                        if (request.defined(ARG_METADATA_ID + SUFFIX_SELECT
                                            + metadata.getId())) {
                            Metadata newMetadata =
                                new Metadata(getRepository().getGUID(),
                                             parent.getId(), metadata);

                            if ( !parentMetadataList.contains(newMetadata)) {
                                insertMetadata(newMetadata);
                                cnt++;
                            }
                        }
                    }
                    parent.setMetadata(null);
                    return new Result(request.url(URL_METADATA_FORM,
                            ARG_ENTRYID, parent.getId(), ARG_MESSAGE,
                            cnt + " "
                            + getRepository().translate(request,
                                "metadata items added")));

                }


                for (Metadata metadata : newMetadataList) {
                    getDatabaseManager().delete(Tables.METADATA.NAME,
                            Clause.eq(Tables.METADATA.COL_ID,
                                      metadata.getId()));
                    insertMetadata(metadata);
                }
            }
            entry.setMetadata(null);
            Misc.run(getRepository(), "checkModifiedEntries",
                     Misc.newList(entry));
            return new Result(request.url(URL_METADATA_FORM, ARG_ENTRYID,
                                          entry.getId()));
        }
    }



    /**
     * _more_
     *
     * @param request _more_
     *
     * @return _more_
     *
     * @throws Exception _more_
     */
    public Result processMetadataList(Request request) throws Exception {

        boolean doCloud = request.getString(ARG_TYPE, "list").equals("cloud");
        StringBuffer sb = new StringBuffer();
        String       header;
        if (doCloud) {
            request.put(ARG_TYPE, "list");
            header = HtmlUtils.href(request.getUrl(), msg("List"))
                     + HtmlUtils.span(
                         "&nbsp;|&nbsp;",
                         HtmlUtils.cssClass(CSS_CLASS_SEPARATOR)) + HtmlUtils.b(
                             msg("Cloud"));
        } else {
            request.put(ARG_TYPE, "cloud");
            header = HtmlUtils.b(msg("List"))
                     + HtmlUtils.span(
                         "&nbsp;|&nbsp;",
                         HtmlUtils.cssClass(
                             CSS_CLASS_SEPARATOR)) + HtmlUtils.href(
                                 request.getUrl(), msg("Cloud"));
        }
        sb.append(HtmlUtils.center(HtmlUtils.span(header,
                HtmlUtils.cssClass(CSS_CLASS_HEADING_2))));
        sb.append(HtmlUtils.hr());
        MetadataHandler handler =
            findMetadataHandler(request.getString(ARG_METADATA_TYPE, ""));
        MetadataType type =
            handler.findType(request.getString(ARG_METADATA_TYPE, ""));
        String[] values = getDistinctValues(request, handler, type);
        int[]    cnt    = new int[values.length];
        int      max    = -1;
        int      min    = 10000;
        for (int i = 0; i < values.length; i++) {
            String value = values[i];
            cnt[i] = 0;
            Statement stmt = getDatabaseManager().select(
                                 SqlUtil.count("*"), Tables.METADATA.NAME,
                                 Clause.and(
                                     Clause.eq(
                                         Tables.METADATA.COL_TYPE,
                                         type.getId()), Clause.eq(
                                             Tables.METADATA.COL_ATTR1,
                                             value)));
            ResultSet results = stmt.getResultSet();
            if ( !results.next()) {
                continue;
            }
            cnt[i] = results.getInt(1);
            max    = Math.max(cnt[i], max);
            min    = Math.min(cnt[i], min);
            getDatabaseManager().closeAndReleaseConnection(stmt);
        }
        int    diff         = max - min;
        double distribution = diff / 5.0;
        if ( !doCloud) {
            List tuples = new ArrayList();
            for (int i = 0; i < values.length; i++) {
                tuples.add(new Object[] { new Integer(cnt[i]), values[i] });
            }
            tuples = Misc.sortTuples(tuples, false);
            sb.append(HtmlUtils.formTable());
            sb.append(HtmlUtils.row(HtmlUtils.cols(HtmlUtils.b("Count"),
                    HtmlUtils.b(type.getLabel()))));
            for (int i = 0; i < tuples.size(); i++) {
                Object[] tuple = (Object[]) tuples.get(i);
                sb.append("<tr><td width=\"1%\" align=right>");
                sb.append(tuple[0]);
                sb.append("</td><td>");
                String value = (String) tuple[1];
                sb.append(HtmlUtils.href(handler.getSearchUrl(request, type,
                        value), value));
                sb.append("</td></tr>");
            }
            sb.append(HtmlUtils.formTableClose());
        } else {
            for (int i = 0; i < values.length; i++) {
                if (cnt[i] == 0) {
                    continue;
                }
                double percent = cnt[i] / distribution;
                int    bin     = (int) (percent * 5);
                String css     = "font-size:" + (12 + bin * 2);
                String value   = values[i];
                String ttValue = value.replace("\"", "'");
                if (value.length() > 30) {
                    value = value.substring(0, 29) + "...";
                }
                sb.append("<span style=\"" + css + "\">");
                String extra = XmlUtil.attrs("alt",
                                             "Count:" + cnt[i] + " "
                                             + ttValue, "title",
                                                 "Count:" + cnt[i] + " "
                                                 + ttValue);
                sb.append(HtmlUtils.href(handler.getSearchUrl(request, type,
                        values[i]), value, extra));
                sb.append("</span>");
                sb.append(" &nbsp; ");
            }
        }

        return getSearchManager().makeResult(request,
                                             msg(type.getLabel() + " Cloud"),
                                             sb);
    }


    /**
     * _more_
     *
     * @param request _more_
     *
     * @return _more_
     *
     * @throws Exception _more_
     */
    public Result processMetadataView(Request request) throws Exception {
        Entry          entry        = getEntryManager().getEntry(request);
        List<Metadata> metadataList = getMetadata(entry);
        Metadata metadata = findMetadata(entry,
                                         request.getString(ARG_METADATA_ID,
                                             ""));
        if (metadata == null) {
            return new Result("", "Could not find metadata");
        }
        MetadataHandler handler = findMetadataHandler(metadata.getType());
        Result          result = handler.processView(request, entry,
                                     metadata);
        return getEntryManager().addEntryHeader(request,
                getEntryManager().getTopGroup(), result);
    }



    /**
     * _more_
     *
     * @param request _more_
     *
     * @return _more_
     *
     * @throws Exception _more_
     */
    public Result processMetadataForm(Request request) throws Exception {
        Entry entry = getEntryManager().getEntry(request);
        StringBuffer sb = new StringBuffer();
        request.appendMessage(sb);
        return processMetadataForm(request, entry, sb);
    }

    public Result processMetadataForm(Request request, Entry entry, StringBuffer sb) throws Exception {
        boolean canEditParent = getAccessManager().canDoAction(request,
                                    getEntryManager().getParent(request,
                                        entry), Permission.ACTION_EDIT);

        //        sb.append(getEntryManager().makeEntryHeader(request, entry));

        List<Metadata> metadataList = getMetadata(entry);
        sb.append(HtmlUtils.p());
        if (metadataList.size() == 0) {
            sb.append(
                getRepository().showDialogNote(
                    msg("No metadata defined for entry")));
            sb.append(msgLabel("Add new metadata"));
            makeAddList(request, entry, sb);
        } else {
            request.uploadFormWithAuthToken(sb, URL_METADATA_CHANGE);
            sb.append(HtmlUtils.hidden(ARG_ENTRYID, entry.getId()));
            sb.append(HtmlUtils.submit(msg("Change")));
            sb.append(HtmlUtils.space(2));
            sb.append(HtmlUtils.submit(msg("Delete selected"),
                                      ARG_METADATA_DELETE));
            sb.append(HtmlUtils.space(2));
            sb.append(
                      HtmlUtils.submit(
                                      msg("Copy selected to clipboard"),
                                      ARG_METADATA_CLIPBOARD_COPY));
            //            sb.append(HtmlUtils.formTable());
            sb.append(HtmlUtils.br());
            for (Metadata metadata : metadataList) {
                metadata.setEntry(entry);
                MetadataHandler metadataHandler =
                    findMetadataHandler(metadata);
                if (metadataHandler == null) {
                    continue;
                }
                String[] html = metadataHandler.getForm(request, entry,
                                    metadata, true);
                if (html == null) {
                    continue;
                }

                String cbxId = "cbx_" + metadata.getId();
                String cbx =
                    HtmlUtils.checkbox(
                        ARG_METADATA_ID + SUFFIX_SELECT + metadata.getId(),
                        metadata.getId(), false,
                        HtmlUtils.id(cbxId) + " "
                        + HtmlUtils.attr(
                            HtmlUtils.ATTR_TITLE,
                            msg(
                            "Shift-click: select range; Control-click: toggle all")) + HtmlUtils.attr(
                                HtmlUtils.ATTR_ONCLICK,
                                HtmlUtils.call(
                                    "checkboxClicked",
                                    HtmlUtils.comma(
                                        "event", HtmlUtils.squote("cbx_"),
                                        HtmlUtils.squote(cbxId)))));

                StringBuffer metadataEntry = new StringBuffer();
                metadataEntry.append(HtmlUtils.formTable());
                metadataEntry.append(html[1]);
                metadataEntry.append(HtmlUtils.formTableClose());
                sb.append(
                    HtmlUtils.makeShowHideBlock(
                        cbx + html[0],
                        HtmlUtils.div(
                            metadataEntry.toString(),
                            HtmlUtils.cssClass("metadatagroup")), false));
            }
            sb.append(HtmlUtils.p());
            sb.append(HtmlUtils.submit(msg("Change")));
            sb.append(HtmlUtils.space(2));
            sb.append(HtmlUtils.submit(msg("Delete Selected"),
                                      ARG_METADATA_DELETE));
            sb.append(
                      HtmlUtils.submit(
                                      msg("Copy selected to clipboard"),
                                      ARG_METADATA_CLIPBOARD_COPY));
            sb.append(HtmlUtils.formClose());
        }

        return getEntryManager().makeEntryEditResult(request, entry,
                msg("Edit Properties"), sb);

    }



    /**
     * _more_
     *
     * @param request _more_
     *
     * @return _more_
     *
     * @throws Exception _more_
     */
    public Result processMetadataAddForm(Request request) throws Exception {
        StringBuffer sb    = new StringBuffer();
        Entry        entry = getEntryManager().getEntry(request);
        sb.append(HtmlUtils.p());

        if(request.get(ARG_METADATA_CLIPBOARD_PASTE, false)) {
            List<Metadata>clipboard = getMetadataFromClipboard(request);
            if(clipboard==null || clipboard.size()==0) {
                sb.append(getRepository().showDialogError("Clipboard empty"));
            } else {
                //TODO: file attachments
                for(Metadata copiedMetadata: clipboard) {
                    Metadata newMetadata =
                        new Metadata(getRepository().getGUID(),
                                     entry.getId(), copiedMetadata);
                    insertMetadata(newMetadata);
                }
                entry.setMetadata(null);
                sb.append(getRepository().showDialogNote("Metadata pasted from clipboard"));
            }
            return processMetadataForm(request, entry, sb);
        }

        if ( !request.exists(ARG_METADATA_TYPE)) {
            makeAddList(request, entry, sb);
        } else {
            String type = request.getString(ARG_METADATA_TYPE, BLANK);
            sb.append(HtmlUtils.formTable());
            for (MetadataHandler handler : metadataHandlers) {
                if (handler.canHandle(type)) {
                    handler.makeAddForm(request, entry,
                                        handler.findType(type), sb);
                    break;
                }
            }
            sb.append(HtmlUtils.formTableClose());
        }
        return getEntryManager().makeEntryEditResult(request, entry,
                msg("Add Property"), sb);
    }

    /**
     * _more_
     *
     * @param request _more_
     * @param entry _more_
     * @param sb _more_
     *
     * @throws Exception _more_
     */
    private void makeAddList(Request request, Entry entry, StringBuffer sb)
            throws Exception {
        List<String> groups   = new ArrayList<String>();
        Hashtable    groupMap = new Hashtable();

        List<Metadata>clipboard = getMetadataFromClipboard(request);
        if(clipboard!=null && clipboard.size()>0) {
            StringBuffer clipboardSB = new StringBuffer();
            Entry dummyEntry = new Entry();
            int cnt = 0;
            for(Metadata copied: clipboard) {
                MetadataHandler handler = findMetadataHandler(copied.getType());
                MetadataType type = handler.getType(copied.getType());
                String label = type.getTypeLabel(copied);
                String row = label;
                clipboardSB.append(row);
                clipboardSB.append("<br>");
                cnt++;
            }
            
            request.uploadFormWithAuthToken(sb, URL_METADATA_ADDFORM);
            sb.append(HtmlUtils.hidden(ARG_ENTRYID, entry.getId()));
            sb.append(HtmlUtils.hidden(ARG_METADATA_CLIPBOARD_PASTE, "true"));
            sb.append(HtmlUtils.submit(msg("Copy from Clipboard")));
            sb.append(HtmlUtils.formClose());
            sb.append(HtmlUtils.makeShowHideBlock("Clipboard", clipboardSB.toString(),
                    false));
            sb.append(HtmlUtils.p());
        }

        for (MetadataType type : metadataTypes) {
            if (type.getAdminOnly() && !request.getUser().getAdmin()) {
                continue;
            }
            if ( !type.getForUser()) {
                continue;
            }
            if(!type.isForEntry(entry)) {
                continue;
            }
            String       name    = type.getCategory();
            StringBuffer groupSB = (StringBuffer) groupMap.get(name);
            if (groupSB == null) {
                groupMap.put(name, groupSB = new StringBuffer());
                groups.add(name);
            }
            request.uploadFormWithAuthToken(groupSB, URL_METADATA_ADDFORM);
            groupSB.append(HtmlUtils.hidden(ARG_ENTRYID, entry.getId()));
            groupSB.append(HtmlUtils.hidden(ARG_METADATA_TYPE, type.getId()));
            groupSB.append(HtmlUtils.submit(msg("Add")));
            groupSB.append(HtmlUtils.space(1)
                           + HtmlUtils.bold(type.getLabel()));
            groupSB.append(HtmlUtils.formClose());
            groupSB.append(HtmlUtils.p());
            groupSB.append(NEWLINE);
        }

        for (String name : groups) {
            //                sb.append(header(name));
            StringBuffer tmp = new StringBuffer();
            tmp.append("<ul>");
            tmp.append(groupMap.get(name));
            tmp.append("</ul>");
            sb.append(HtmlUtils.makeShowHideBlock(name, tmp.toString(),
                    false));

        }

    }



    /**
     * _more_
     *
     * @param request _more_
     *
     * @return _more_
     *
     * @throws Exception _more_
     */
    public Result processMetadataAdd(Request request) throws Exception {
        synchronized (MUTEX_METADATA) {
            Entry entry = getEntryManager().getEntry(request);
            if (request.exists(ARG_CANCEL)) {
                return new Result(request.url(URL_METADATA_ADDFORM,
                        ARG_ENTRYID, entry.getId()));
            }
            List<Metadata> newMetadata = new ArrayList<Metadata>();
            for (MetadataHandler handler : metadataHandlers) {
                handler.handleAddSubmit(request, entry, newMetadata);
            }

            for (Metadata metadata : newMetadata) {
                insertMetadata(metadata);
            }
            entry.setMetadata(null);
            Misc.run(getRepository(), "checkModifiedEntries",
                     Misc.newList(entry));
            return new Result(request.url(URL_METADATA_FORM, ARG_ENTRYID,
                                          entry.getId()));

        }
    }



    /**
     * _more_
     *
     * @param request _more_
     * @param handler _more_
     * @param type _more_
     *
     * @return _more_
     *
     * @throws Exception _more_
     */
    public String[] getDistinctValues(Request request,
                                      MetadataHandler handler,
                                      MetadataType type)
            throws Exception {
        Hashtable myDistinctMap = distinctMap;
        String[]  values        = (String[]) ((myDistinctMap == null)
                ? null
                : myDistinctMap.get(type.getId()));

        if (values == null) {
            Statement stmt = getDatabaseManager().select(
                                 SqlUtil.distinct(Tables.METADATA.COL_ATTR1),
                                 Tables.METADATA.NAME,
                                 Clause.eq(
                                     Tables.METADATA.COL_TYPE, type.getId()));
            values =
                SqlUtil.readString(getDatabaseManager().getIterator(stmt), 1);

            if (myDistinctMap != null) {
                myDistinctMap.put(type.getId(), values);
            }
        }
        return values;
    }


    /**
     * _more_
     *
     * @param metadata _more_
     *
     * @throws Exception _more_
     */
    public void insertMetadata(Metadata metadata) throws Exception {
        distinctMap = null;

        getDatabaseManager().executeInsert(Tables.METADATA.INSERT,
                                           new Object[] {
            metadata.getId(), metadata.getEntryId(), metadata.getType(),
            new Integer(metadata.getInherited()
                        ? 1
                        : 0), metadata.getAttr1(), metadata.getAttr2(),
            metadata.getAttr3(), metadata.getAttr4(), metadata.getExtra()
        });
    }

    /**
     * _more_
     *
     * @param metadata _more_
     *
     * @throws Exception _more_
     */
    public void deleteMetadata(Metadata metadata) throws Exception {
        getDatabaseManager().delete(Tables.METADATA.NAME,
                                    Clause.eq(Tables.METADATA.COL_ID,
                                        metadata.getId()));
    }




}
