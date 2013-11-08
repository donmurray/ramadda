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

package org.ramadda.repository.search;


import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.document.DateTools;

import org.apache.lucene.document.Field;

import org.apache.lucene.index.FilterIndexReader;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.IndexWriter;


import org.apache.lucene.index.Term;
import org.apache.lucene.queryParser.MultiFieldQueryParser;
import org.apache.lucene.queryParser.QueryParser;
import org.apache.lucene.search.Collector;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.ScoreDoc;
import org.apache.lucene.search.Scorer;
import org.apache.lucene.search.Searcher;
import org.apache.lucene.search.TopDocs;
import org.apache.lucene.search.TopScoreDocCollector;
import org.apache.lucene.store.FSDirectory;
import org.apache.lucene.util.Version;

import org.ramadda.repository.*;
import org.ramadda.repository.admin.*;

import org.ramadda.repository.auth.*;

import org.ramadda.repository.database.Tables;

import org.ramadda.repository.metadata.*;

import org.ramadda.repository.output.*;
import org.ramadda.repository.type.*;
import org.ramadda.repository.util.ServerInfo;

import org.ramadda.sql.Clause;

import org.ramadda.sql.SqlUtil;

import org.ramadda.util.CategoryBuffer;
import org.ramadda.util.HtmlUtils;
import org.ramadda.util.JQuery;
import org.ramadda.util.OpenSearchUtil;

import org.ramadda.util.TTLObject;


import org.w3c.dom.*;

import ucar.unidata.ui.ImageUtils;
import ucar.unidata.util.DateUtil;

import ucar.unidata.util.IOUtil;
import ucar.unidata.util.LogUtil;
import ucar.unidata.util.Misc;
import ucar.unidata.util.PatternFileFilter;
import ucar.unidata.util.PluginClassLoader;
import ucar.unidata.util.StringUtil;
import ucar.unidata.util.TwoFacedObject;
import ucar.unidata.util.TwoFacedObject;

import ucar.unidata.xml.XmlUtil;



import java.io.*;

import java.io.File;

import java.lang.reflect.*;



import java.net.*;



import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.Date;
import java.util.Enumeration;
import java.util.GregorianCalendar;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.Hashtable;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.TimeZone;

import java.util.jar.*;



import java.util.regex.*;
import java.util.zip.*;




/**
 *
 *
 * @author RAMADDA Development Team
 * @version $Revision: 1.3 $
 */
public class SearchManager extends RepositoryManager implements EntryChecker,
        AdminHandler {

    /** _more_ */
    public static final String ARG_SEARCH_SUBMIT = "search.submit";

    /** _more_ */
    public static final String ARG_SEARCH_SUBSET = "search.subset";

    /** _more_ */
    public static final String ARG_SEARCH_SERVERS = "search.servers";



    /** _more_ */
    public final RequestUrl URL_SEARCH_FORM = new RequestUrl(this,
                                                  "/search/form",
                                                  "Advanced Search");

    /** _more_ */
    public final RequestUrl URL_SEARCH_TYPE = new RequestUrl(this,
                                                  "/search/type",
                                                  "Search by Type");

    /** _more_ */
    public final RequestUrl URL_SEARCH_ASSOCIATIONS =
        new RequestUrl(this, "/search/associations/do",
                       "Search Associations");

    /** _more_ */
    public final RequestUrl URL_SEARCH_ASSOCIATIONS_FORM =
        new RequestUrl(this, "/search/associations/form",
                       "Search Associations");

    /** _more_ */
    public final RequestUrl URL_SEARCH_TEXTFORM = new RequestUrl(this,
                                                      "/search/textform",
                                                      "Text Search");

    /** _more_ */
    public final RequestUrl URL_SEARCH_BROWSE = new RequestUrl(this,
                                                    "/search/browse",
                                                    "Browse");



    /** _more_ */
    public final RequestUrl URL_SEARCH_REMOTE_DO =
        new RequestUrl(this, "/search/remote/do", "Search Remote Servers");

    /** _more_ */
    public final RequestUrl URL_ENTRY_SEARCH = new RequestUrl(this,
                                                   "/search/do", "Search");

    /** _more_ */
    public final List<RequestUrl> searchUrls =
        RequestUrl.toList(new RequestUrl[] { URL_SEARCH_TEXTFORM,
                                             URL_SEARCH_TYPE,
                                             URL_SEARCH_FORM,
                                             URL_SEARCH_BROWSE,
                                             URL_SEARCH_ASSOCIATIONS_FORM });

    /** _more_ */
    public final List<RequestUrl> remoteSearchUrls =
        RequestUrl.toList(new RequestUrl[] { URL_SEARCH_TEXTFORM,
                                             URL_SEARCH_TYPE,
                                             URL_SEARCH_FORM,
                                             URL_SEARCH_BROWSE,
                                             URL_SEARCH_ASSOCIATIONS_FORM });


    /** _more_ */
    private static final String FIELD_ENTRYID = "entryid";

    /** _more_ */
    private static final String FIELD_PATH = "path";

    /** _more_ */
    private static final String FIELD_CONTENTS = "contents";

    /** _more_ */
    private static final String FIELD_MODIFIED = "modified";

    /** _more_ */
    private static final String FIELD_DESCRIPTION = "description";

    /** _more_ */
    private static final String FIELD_METADATA = "metadata";

    /** _more_ */
    private IndexSearcher luceneSearcher;

    /** _more_ */
    private IndexReader luceneReader;

    /** _more_ */
    private boolean isLuceneEnabled = true;





    /**
     * _more_
     *
     * @param repository _more_
     */
    public SearchManager(Repository repository) {
        super(repository);
        repository.addEntryChecker(this);
        isLuceneEnabled = getProperty(PROP_SEARCH_LUCENE_ENABLED, false);
        getAdmin().addAdminHandler(this);
    }


    /**
     * _more_
     *
     * @return _more_
     */
    public boolean includeMetadata() {
        return getProperty(PROP_SEARCH_SHOW_METADATA, true);
    }

    /**
     * _more_
     *
     * @return _more_
     */
    public boolean isLuceneEnabled() {
        return isLuceneEnabled;
    }


    /**
     * _more_
     *
     * @return _more_
     *
     * @throws Exception _more_
     */
    private IndexWriter getLuceneWriter() throws Exception {
        File indexFile = new File(getStorageManager().getIndexDir());
        IndexWriter writer =
            new IndexWriter(FSDirectory.open(indexFile),
                            new StandardAnalyzer(Version.LUCENE_CURRENT),
                            IndexWriter.MaxFieldLength.LIMITED);

        return writer;
    }


    /**
     * _more_
     *
     * @return _more_
     */
    public List<RequestUrl> getAdminUrls() {
        return null;
    }

    /**
     * _more_
     *
     * @param block _more_
     * @param asb _more_
     */
    public void addToAdminSettingsForm(String block, StringBuffer asb) {
        if ( !block.equals(Admin.BLOCK_ACCESS)) {
            return;
        }
        asb.append(HtmlUtils.colspan(msgHeader("Search"), 2));
        asb.append(
            HtmlUtils
                .formEntry(
                    "",
                    HtmlUtils
                        .checkbox(
                            PROP_SEARCH_LUCENE_ENABLED, "true",
                            isLuceneEnabled()) + HtmlUtils.space(2)
                                + msg("Enable Lucene Indexing and Search")));
    }


    /**
     * _more_
     *
     * @param request _more_
     *
     * @throws Exception _more_
     */
    public void applyAdminSettingsForm(Request request) throws Exception {
        getRepository().writeGlobal(
            PROP_SEARCH_LUCENE_ENABLED,
            isLuceneEnabled = request.get(PROP_SEARCH_LUCENE_ENABLED, false));

    }

    /**
     * _more_
     *
     * @return _more_
     */
    public String getId() {
        return "searchmanager";
    }

    /**
     * _more_
     *
     * @param entries _more_
     *
     * @throws Exception _more_
     */
    public synchronized void indexEntries(List<Entry> entries)
            throws Exception {
        IndexWriter writer = getLuceneWriter();
        for (Entry entry : entries) {
            indexEntry(writer, entry);
        }
        writer.optimize();
        writer.close();
        luceneReader   = null;
        luceneSearcher = null;
    }


    /**
     * _more_
     *
     * @param writer _more_
     * @param entry _more_
     *
     * @throws Exception _more_
     */
    private void indexEntry(IndexWriter writer, Entry entry)
            throws Exception {

        org.apache.lucene.document.Document doc =
            new org.apache.lucene.document.Document();
        String path = entry.getResource().getPath();
        doc.add(new Field(FIELD_ENTRYID, entry.getId(), Field.Store.YES,
                          Field.Index.NOT_ANALYZED));
        if ((path != null) && (path.length() > 0)) {
            doc.add(new Field(FIELD_PATH, path, Field.Store.YES,
                              Field.Index.NOT_ANALYZED));
        }

        StringBuffer metadataSB = new StringBuffer();
        getRepository().getMetadataManager().getTextCorpus(entry, metadataSB);
        entry.getTypeHandler().getTextCorpus(entry, metadataSB);
        doc.add(new Field(FIELD_DESCRIPTION,
                          entry.getName() + " " + entry.getDescription(),
                          Field.Store.NO, Field.Index.ANALYZED));

        if (metadataSB.length() > 0) {
            doc.add(new Field(FIELD_METADATA, metadataSB.toString(),
                              Field.Store.NO, Field.Index.ANALYZED));
        }

        doc.add(new Field(FIELD_MODIFIED,
                          DateTools.timeToString(entry.getStartDate(),
                              DateTools.Resolution.MINUTE), Field.Store.YES,
                                  Field.Index.NOT_ANALYZED));

        if (entry.isFile()) {
            addContentField(entry, doc, new File(path));
        }
        writer.addDocument(doc);
    }


    /**
     * _more_
     *
     * @param entry _more_
     * @param doc _more_
     * @param f _more_
     *
     * @throws Exception _more_
     */
    private void addContentField(Entry entry,
                                 org.apache.lucene.document.Document doc,
                                 File f)
            throws Exception {
        //org.apache.lucene.document.Document doc
        InputStream stream = getStorageManager().getFileInputStream(f);
        try {
            org.apache.tika.metadata.Metadata metadata =
                new org.apache.tika.metadata.Metadata();
            org.apache.tika.parser.AutoDetectParser parser =
                new org.apache.tika.parser.AutoDetectParser();
            org.apache.tika.sax.BodyContentHandler handler =
                new org.apache.tika.sax.BodyContentHandler();
            parser.parse(stream, handler, metadata);
            String contents = handler.toString();
            if ((contents != null) && (contents.length() > 0)) {
                doc.add(new Field(FIELD_CONTENTS, contents, Field.Store.NO,
                                  Field.Index.ANALYZED));
            }

            /*
            String[] names = metadata.names();
            for (String name : names) {
                String value = metadata.get(name);
                System.err.println(name +"=" + value);
                doc.add(new Field(name, value, Field.Store.YES,
                                  Field.Index.ANALYZED));
            }
            */
        } catch (Exception exc) {
            System.err.println("error harvesting corpus from:" + f);
            exc.printStackTrace();
        } finally {
            IOUtil.close(stream);
        }
    }


    /**
     * _more_
     *
     * @return _more_
     *
     * @throws Exception _more_
     */
    private IndexReader getLuceneReader() throws Exception {
        if (true) {
            return IndexReader.open(
                FSDirectory.open(
                    new File(getStorageManager().getIndexDir())), false);
        }
        if (luceneReader == null) {
            luceneReader = IndexReader.open(
                FSDirectory.open(
                    new File(getStorageManager().getIndexDir())), false);
        }

        return luceneReader;
    }


    /**
     * _more_
     *
     * @return _more_
     *
     * @throws Exception _more_
     */
    private IndexSearcher getLuceneSearcher() throws Exception {
        if (luceneSearcher == null) {
            luceneSearcher = new IndexSearcher(getLuceneReader());
        }

        return luceneSearcher;
    }


    /**
     * _more_
     *
     * @param request _more_
     * @param groups _more_
     * @param entries _more_
     *
     * @throws Exception _more_
     */
    public void processLuceneSearch(Request request, List<Entry> groups,
                                    List<Entry> entries)
            throws Exception {
        StringBuffer sb = new StringBuffer();
        StandardAnalyzer analyzer =
            new StandardAnalyzer(Version.LUCENE_CURRENT);
        QueryParser qp = new MultiFieldQueryParser(Version.LUCENE_CURRENT,
                             new String[] { FIELD_DESCRIPTION,
                                            FIELD_METADATA,
                                            FIELD_CONTENTS }, analyzer);
        Query         query    = qp.parse(request.getString(ARG_TEXT, ""));
        IndexSearcher searcher = getLuceneSearcher();
        TopDocs       hits     = searcher.search(query, 100);
        ScoreDoc[]    docs     = hits.scoreDocs;
        for (int i = 0; i < docs.length; i++) {
            org.apache.lucene.document.Document doc =
                searcher.doc(docs[i].doc);
            String id = doc.get(FIELD_ENTRYID);
            if (id == null) {
                continue;
            }
            Entry entry = getEntryManager().getEntry(request, id);
            if (entry == null) {
                continue;
            }
            if (entry.isGroup()) {
                groups.add(entry);
            } else {
                entries.add(entry);
            }
        }
    }


    /**
     * _more_
     *
     * @param entries _more_
     */
    public void entriesCreated(List<Entry> entries) {
        if ( !isLuceneEnabled()) {
            return;
        }
        try {
            indexEntries(entries);
        } catch (Exception exc) {
            logError("Error indexing entries", exc);
        }
    }



    /**
     * _more_
     *
     * @param entries _more_
     */
    public void entriesModified(List<Entry> entries) {
        if ( !isLuceneEnabled()) {
            return;
        }
        try {
            List<String> ids = new ArrayList<String>();
            for (Entry entry : entries) {
                ids.add(entry.getId());

            }
            entriesDeleted(ids);
            indexEntries(entries);
        } catch (Exception exc) {
            logError("Error adding entries to Lucene index", exc);
        }
    }


    /**
     * _more_
     *
     * @param ids _more_
     */
    public synchronized void entriesDeleted(List<String> ids) {
        if ( !isLuceneEnabled()) {
            return;
        }
        try {
            IndexWriter writer = getLuceneWriter();
            for (String id : ids) {
                writer.deleteDocuments(new Term(FIELD_ENTRYID, id));
            }
            writer.close();
        } catch (Exception exc) {
            logError("Error deleting entries from Lucene index", exc);
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
    public Result processCapabilities(Request request) throws Exception {
        return new Result("", "text/xml");
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
    public Result processOpenSearch(Request request) throws Exception {

        Document doc  = XmlUtil.makeDocument();
        Element  root = OpenSearchUtil.getRoot();
        /*
   <ShortName>Web Search</ShortName>
   <Description>Use Example.com to search the Web.</Description>
   <Tags>example web</Tags>
   <Contact>admin@example.com</Contact>
        */
        OpenSearchUtil.addBasicTags(
            root, getRepository().getRepositoryName(),
            getRepository().getRepositoryDescription(),
            getRepository().getRepositoryEmail());
        ((Element) XmlUtil.create(
            OpenSearchUtil.TAG_IMAGE, root)).appendChild(
                XmlUtil.makeCDataNode(
                    root.getOwnerDocument(),
                    getRepository().getLogoImage(null), false));




        String url = request.getAbsoluteUrl(URL_ENTRY_SEARCH.toString());
        url = HtmlUtils.url(url, new String[] {
            ARG_OUTPUT, AtomOutputHandler.OUTPUT_ATOM.getId(), ARG_TEXT,
            OpenSearchUtil.MACRO_TEXT, ARG_BBOX, OpenSearchUtil.MACRO_BBOX,
            Constants.dataDate.getFromArg(), OpenSearchUtil.MACRO_TIME_START,
            Constants.dataDate.getToArg(), OpenSearchUtil.MACRO_TIME_END,
        }, false);


        XmlUtil.create(OpenSearchUtil.TAG_URL, root, "",
                       new String[] { OpenSearchUtil.ATTR_TYPE,
                                      "application/atom+xml",
                                      OpenSearchUtil.ATTR_TEMPLATE, url });

        return new Result(XmlUtil.toString(root), OpenSearchUtil.MIMETYPE);
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
    public Result processEntryTextSearchForm(Request request)
            throws Exception {
        return makeSearchForm(request, true, false);
    }


    /**
     * _more_
     *
     * @return _more_
     *
     * @throws Exception _more_
     */
    public List<RequestUrl> getSearchUrls() throws Exception {
        if (getRegistryManager().getSelectedRemoteServers().size() > 0) {
            //            return getRepository().remoteSearchUrls;
            return remoteSearchUrls;
        }

        return searchUrls;
    }




    /**
     * _more_
     *
     * @param request _more_
     * @param justText _more_
     * @param typeSpecific _more_
     *
     * @return _more_
     *
     * @throws Exception _more_
     */
    public Result makeSearchForm(Request request, boolean justText,
                                 boolean typeSpecific)
            throws Exception {
        StringBuffer sb = new StringBuffer();
        makeSearchForm(request, justText, typeSpecific, sb);

        return makeResult(request, msg("Search Form"), sb);
    }

    /**
     * _more_
     *
     * @param request _more_
     *
     * @return _more_
     */
    public String getSearchUrl(Request request) {
        return request.url(URL_ENTRY_SEARCH, ARG_NAME, WHAT_ENTRIES);
    }

    /**
     * _more_
     *
     * @param request _more_
     * @param justText _more_
     * @param typeSpecific _more_
     * @param sb _more_
     *
     * @throws Exception _more_
     */
    private void makeSearchForm(Request request, boolean justText,
                                boolean typeSpecific, StringBuffer sb)
            throws Exception {

        TypeHandler typeHandler = getRepository().getTypeHandler(request);
        sb.append(
            HtmlUtils.form(
                getSearchUrl(request),
                makeFormSubmitDialog(sb, msg("Searching..."))
                + " name=\"searchform\" "));

        if (justText) {
            sb.append(HtmlUtils.hidden(ARG_SEARCH_TYPE, SEARCH_TYPE_TEXT));
        }

        //Put in an empty submit button so when the user presses return 
        //it acts like a regular submit (not a submit to change the type)
        sb.append(HtmlUtils.submitImage(iconUrl(ICON_BLANK),
                                        ARG_SEARCH_SUBMIT));

        String what = (String) request.getWhat(BLANK);
        if (what.length() == 0) {
            what = WHAT_ENTRIES;
        }


        List<ServerInfo> servers =
            getRegistryManager().getSelectedRemoteServers();

        String buttons;


        if (servers.size() > 0) {
            buttons =
                HtmlUtils.buttons(
                    HtmlUtils.submit(
                        msg("Search this Repository"),
                        ARG_SEARCH_SUBMIT), HtmlUtils.submit(
                            msg("Search Remote Repositories"),
                            ARG_SEARCH_SERVERS));
        } else {
            buttons = HtmlUtils.submit(msg("Search"), ARG_SEARCH_SUBMIT);
        }
        sb.append(HtmlUtils.p());
        if ( !justText) {
            sb.append(buttons);
            sb.append(HtmlUtils.p());
        }

        if (justText) {
            String value = (String) request.getString(ARG_TEXT, "");
            String extra = "";
            /*            if (getDatabaseManager().supportsRegexp()) {
                extra = HtmlUtils.checkbox(
                                          ARG_ISREGEXP, "true", request.get(ARG_ISREGEXP, false)) + " "
                    + msg("Use regular expression");
                    }*/
            sb.append(HtmlUtils.span(msgLabel("Text"),
                                     HtmlUtils.cssClass("formlabel")) + " "
                                         + HtmlUtils.input(ARG_TEXT, value,
                                             HtmlUtils.SIZE_50
                                             + " autofocus ") + " " + extra
                                                 + " " + buttons);

            /*            sb.append(
                "<table width=\"100%\" border=\"0\"><tr><td width=\"60\">");
            typeHandler.addTextSearch(request, sb);
            sb.append("</table>");
            sb.append(HtmlUtils.p());
            */

            sb.append("<p>&nbsp;<p>&nbsp;<p>");
            sb.append(header(msg("Search by Type")));
            addSearchByTypeList(request, sb);


        } else {
            Object       oldValue = request.remove(ARG_RELATIVEDATE);
            List<Clause> where    = typeHandler.assembleWhereClause(request);
            if (oldValue != null) {
                request.put(ARG_RELATIVEDATE, oldValue);
            }

            typeHandler.addToSearchForm(request, sb, where, true);


            if (includeMetadata()) {
                StringBuffer metadataSB = new StringBuffer();
                metadataSB.append(HtmlUtils.formTable());
                getMetadataManager().addToSearchForm(request, metadataSB);
                metadataSB.append(HtmlUtils.formTableClose());
                sb.append(HtmlUtils.makeShowHideBlock(msg("Properties"),
                        metadataSB.toString(), false));
            }

            StringBuffer outputForm = new StringBuffer(HtmlUtils.formTable());
            /* Humm, we probably don't want to include this as it screws up setting the output in the form
            if (request.defined(ARG_OUTPUT)) {

                OutputType output = request.getOutput(BLANK);
                outputForm.append(HtmlUtils.hidden(ARG_OUTPUT,
                        output.getId().toString()));
            }
            */

            List orderByList = new ArrayList();
            orderByList.add(new TwoFacedObject(msg("None"), "none"));
            orderByList.add(new TwoFacedObject(msg("From Date"),
                    SORTBY_FROMDATE));
            orderByList.add(new TwoFacedObject(msg("To Date"),
                    SORTBY_TODATE));
            orderByList.add(new TwoFacedObject(msg("Create Date"),
                    SORTBY_CREATEDATE));
            orderByList.add(new TwoFacedObject(msg("Name"), SORTBY_NAME));

            String orderBy = HtmlUtils.select(
                                 ARG_ORDERBY, orderByList,
                                 request.getString(
                                     ARG_ORDERBY,
                                     "none")) + HtmlUtils.checkbox(
                                         ARG_ASCENDING, "true",
                                         request.get(
                                             ARG_ASCENDING,
                                             false)) + HtmlUtils.space(1)
                                                 + msg("ascending");
            outputForm.append(HtmlUtils.formEntry(msgLabel("Order By"),
                    orderBy));
            outputForm.append(HtmlUtils.formEntry(msgLabel("Output"),
                    HtmlUtils.select(ARG_OUTPUT,
                                     getOutputHandlerSelectList(),
                                     request.getString(ARG_OUTPUT, ""))));

            outputForm.append(HtmlUtils.formTableClose());




            sb.append(HtmlUtils.makeShowHideBlock(msg("Output"),
                    outputForm.toString(), false));

        }

        if (servers.size() > 0) {
            StringBuffer serverSB  = new StringBuffer();
            int          serverCnt = 0;
            String       cbxId;
            String       call;

            cbxId = ATTR_SERVER + (serverCnt++);
            call = HtmlUtils.attr(HtmlUtils.ATTR_ONCLICK,
                                  HtmlUtils.call("checkboxClicked",
                                      HtmlUtils.comma("event",
                                          HtmlUtils.squote(ATTR_SERVER),
                                          HtmlUtils.squote(cbxId))));

            serverSB.append(HtmlUtils.checkbox(ARG_DOFRAMES, "true",
                    request.get(ARG_DOFRAMES, false)));
            serverSB.append(msg("Do frames"));
            serverSB.append(HtmlUtils.br());
            serverSB.append(HtmlUtils.checkbox(ATTR_SERVER,
                    ServerInfo.ID_THIS, false, HtmlUtils.id(cbxId) + call));
            serverSB.append(msg("Include this repository"));
            serverSB.append(HtmlUtils.br());
            for (ServerInfo server : servers) {
                cbxId = ATTR_SERVER + (serverCnt++);
                call = HtmlUtils.attr(HtmlUtils.ATTR_ONCLICK,
                                      HtmlUtils.call("checkboxClicked",
                                          HtmlUtils.comma("event",
                                              HtmlUtils.squote(ATTR_SERVER),
                                                  HtmlUtils.squote(cbxId))));
                serverSB.append(HtmlUtils.checkbox(ATTR_SERVER,
                        server.getId(), false, HtmlUtils.id(cbxId) + call));
                serverSB.append(HtmlUtils.space(1));
                serverSB.append(server.getHref(" target=\"server\" "));
                serverSB.append(HtmlUtils.br());
            }
            sb.append(
                HtmlUtils.makeShowHideBlock(
                    msg("Remote Search Settings"),
                    HtmlUtils.div(
                        serverSB.toString(),
                        HtmlUtils.cssClass(CSS_CLASS_SERVER)), false));
        }




        if ( !justText) {
            sb.append(HtmlUtils.p());
            sb.append(buttons);
            sb.append(HtmlUtils.p());
        }
        sb.append(HtmlUtils.formClose());
    }



    /**
     * _more_
     *
     * @return _more_
     */
    public List getOutputHandlerSelectList() {
        List tfos = new ArrayList<TwoFacedObject>();
        for (OutputHandler outputHandler :
                getRepository().getOutputHandlers()) {
            for (OutputType type : outputHandler.getTypes()) {
                if (type.getIsForSearch()) {
                    tfos.add(new HtmlUtils.Selector(type.getLabel(),
                            type.getId(),
                            getRepository().iconUrl(type.getIcon())));
                }
            }
        }

        return tfos;
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
    public Result processSearchType(Request request) throws Exception {
        StringBuffer sb = new StringBuffer();




        List<String> toks = StringUtil.split(request.getRequestPath(), "/",
                                             true, true);
        String lastTok = toks.get(toks.size() - 1);
        if (lastTok.equals("type")) {

            addSearchByTypeList(request, sb);
        } else {
            String      type        = lastTok;
            TypeHandler typeHandler = getRepository().getTypeHandler(type);
            Result result =
                typeHandler.getSpecialSearch().processSearchRequest(request,
                    sb);
            //Is it non-html?
            if (result != null) {
                return result;
            }
        }

        return makeResult(request, msg("Search by Type"), sb);
    }


    /**
     * _more_
     *
     * @param request _more_
     * @param sb _more_
     *
     * @throws Exception _more_
     */
    private void addSearchByTypeList(Request request, StringBuffer sb)
            throws Exception {
        CategoryBuffer cb = new CategoryBuffer();
        for (TypeHandler typeHandler : getRepository().getTypeHandlers()) {
            if ( !typeHandler.getForUser()) {
                continue;
            }
            if (typeHandler.isAnyHandler()) {
                continue;
            }
            int cnt = getEntryUtil().getEntryCount(typeHandler);
            if (cnt == 0) {
                continue;
            }
            String icon = typeHandler.getProperty("icon", (String) null);
            String img;
            if (icon == null) {
                icon = ICON_BLANK;
                img = HtmlUtils.img(typeHandler.iconUrl(icon), "",
                                    HtmlUtils.attr(HtmlUtils.ATTR_WIDTH,
                                        "16"));
            } else {
                img = HtmlUtils.img(typeHandler.iconUrl(icon));
            }
            StringBuffer buff = new StringBuffer();

            buff.append("<li> ");
            buff.append(img);
            buff.append(" ");
            String label = typeHandler.getDescription() + " (" + cnt + ")";



            buff.append(HtmlUtils.href(getRepository().getUrlBase()
                                       + "/search/type/"
                                       + typeHandler.getType(), label));
            cb.append(typeHandler.getCategory(), buff);
        }
        sb.append("<table width=100%><tr valign=top>");
        int colCnt = 0;
        for (String cat : cb.getCategories()) {
            colCnt++;
            if (colCnt > 4) {
                sb.append("</tr><tr valign=top>");
                sb.append("<td colspan=4><hr></td>");
                sb.append("</tr><tr valign=top>");
                colCnt = 1;
            }

            sb.append("<td>");
            sb.append(HtmlUtils.b(msg(cat)));
            sb.append(
                "<div style=\"solid black; max-height: 150px; overflow-y: auto\";>");
            sb.append("<ul>");
            sb.append(cb.get(cat));
            sb.append("</ul>");
            sb.append("</div>");
            sb.append("</td>");

        }
        sb.append("</table>");
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
    public Result processEntrySearchForm(Request request) throws Exception {
        return makeSearchForm(request, false, false);
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
    public Result processSearchInfo(Request request) throws Exception {
        StringBuffer sb = new StringBuffer();
        sb.append(header(msg("Entry Types")));
        sb.append(HtmlUtils.formTable());
        for (TypeHandler typeHandler : getRepository().getTypeHandlers()) {
            String link =
                HtmlUtils.href(URL_SEARCH_TYPE + "/" + typeHandler.getType(),
                               typeHandler.getType());
            sb.append(HtmlUtils.row(HtmlUtils.cols(link,
                    typeHandler.getDescription())));
        }
        sb.append(HtmlUtils.formTableClose());


        sb.append(header(msg("Output Types")));
        sb.append(HtmlUtils.formTable());
        for (OutputHandler outputHandler :
                getRepository().getOutputHandlers()) {
            for (OutputType type : outputHandler.getTypes()) {
                sb.append(HtmlUtils.row(HtmlUtils.cols(type.getId(),
                        type.getLabel())));
            }
        }
        sb.append(HtmlUtils.formTableClose());


        sb.append(header(msg("Metadata Types")));
        sb.append(HtmlUtils.formTable());
        for (MetadataType type :
                getRepository().getMetadataManager().getMetadataTypes()) {
            if ( !type.getSearchable()) {
                continue;
            }
            sb.append(HtmlUtils.row(HtmlUtils.cols(type.getId(),
                    type.getName())));
        }
        sb.append(HtmlUtils.formTableClose());






        return makeResult(request, msg("Search Metadata"), sb);
    }



    /**
     * _more_
     *
     * @param request _more_
     * @param includeThis _more_
     *
     * @return _more_
     *
     * @throws Exception _more_
     */
    public List<ServerInfo> findServers(Request request, boolean includeThis)
            throws Exception {
        List<ServerInfo> servers = new ArrayList<ServerInfo>();
        for (String id :
                (List<String>) request.get(ATTR_SERVER, new ArrayList())) {
            if (id.equals(ServerInfo.ID_THIS) && !includeThis) {
                continue;
            }
            ServerInfo server = getRegistryManager().findRemoteServer(id);
            if (server == null) {
                continue;
            }
            servers.add(server);
        }

        return servers;
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
    public Result processRemoteSearch(Request request) throws Exception {
        StringBuffer sb = new StringBuffer();
        List<String> servers = (List<String>) request.get(ATTR_SERVER,
                                   new ArrayList());
        sb.append(HtmlUtils.p());
        request.remove(ATTR_SERVER);

        boolean      didone   = false;
        StringBuffer serverSB = new StringBuffer();
        for (String id : servers) {
            ServerInfo server = getRegistryManager().findRemoteServer(id);
            if (server == null) {
                continue;
            }
            if ( !didone) {
                sb.append(header(msg("Selected Servers")));
            }
            serverSB.append(server.getHref(" target=\"server\" "));
            serverSB.append(HtmlUtils.br());
            didone = true;
        }

        if ( !didone) {
            sb.append(
                getPageHandler().showDialogNote(msg("No servers selected")));
        } else {
            sb.append(
                HtmlUtils.div(
                    serverSB.toString(),
                    HtmlUtils.cssClass(CSS_CLASS_SERVER_BLOCK)));
            sb.append(HtmlUtils.p());
        }
        sb.append(HtmlUtils.p());
        sb.append(header(msg("Search Results")));

        return makeResult(request, msg("Remote Form"), sb);

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
    public Result processEntryBrowseSearchForm(Request request)
            throws Exception {

        StringBuffer sb = new StringBuffer();
        getMetadataManager().addToBrowseSearchForm(request, sb);

        return makeResult(request, msg("Search Form"), sb);
    }

    /**
     * _more_
     *
     * @param request _more_
     * @param title _more_
     * @param sb _more_
     *
     * @return _more_
     *
     * @throws Exception _more_
     */
    public Result makeResult(Request request, String title, StringBuffer sb)
            throws Exception {
        StringBuffer headerSB = new StringBuffer();
        headerSB.append(getPageHandler().makeHeader(request, getSearchUrls(),
                ""));
        headerSB.append(sb);
        sb = headerSB;
        Result result = new Result(title, sb);

        return addHeaderToAncillaryPage(request, result);
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
    public Result processEntrySearch(Request request) throws Exception {

        if (request.get(ARG_WAIT, false)) {
            return getRepository().getMonitorManager().processEntryListen(
                request);
        }

        //        System.err.println("submit:" + request.getString("submit","YYY"));
        if (request.defined("submit_type.x")
                || request.defined(ARG_SEARCH_SUBSET)) {
            request.remove(ARG_OUTPUT);

            return processEntrySearchForm(request);
        }


        boolean textSearch = isLuceneEnabled()
                             && request.getString(ARG_SEARCH_TYPE,
                                 "").equals(SEARCH_TYPE_TEXT);

        StringBuffer     searchCriteriaSB = new StringBuffer();
        boolean          searchThis       = true;
        List<ServerInfo> servers          = null;

        ServerInfo       thisServer       = getRepository().getServerInfo();
        boolean          doFrames         = request.get(ARG_DOFRAMES, false);

        if (request.exists(ARG_SEARCH_SERVERS)) {
            servers = findServers(request, true);
            if (servers.size() == 0) {
                servers = getRegistryManager().getSelectedRemoteServers();
            }
            if (request.defined(ATTR_SERVER)) {
                searchThis = servers.contains(thisServer);
                if ( !doFrames) {
                    servers.remove(thisServer);
                }
            }
            if (servers.size() > 100) {
                throw new IllegalArgumentException("Too many remote servers:"
                        + servers.size());
            }
        }

        List<Entry> groups  = new ArrayList<Entry>();
        List<Entry> entries = new ArrayList<Entry>();

        if (textSearch) {
            processLuceneSearch(request, groups, entries);
        } else if (searchThis) {
            List[] pair = getEntryManager().getEntries(request,
                              searchCriteriaSB);
            groups.addAll((List<Entry>) pair[0]);
            entries.addAll((List<Entry>) pair[1]);
        }



        if ((servers != null) && (servers.size() > 0)) {
            request.remove(ATTR_SERVER);
            request.remove(ARG_SEARCH_SERVERS);

            if (doFrames) {
                String linkUrl = request.getUrlArgs();
                request.put(ARG_DECORATE, "false");
                request.put(ATTR_TARGET, "_server");
                String       embeddedUrl = request.getUrlArgs();
                StringBuffer sb          = new StringBuffer();
                sb.append(msgHeader("Remote Server Search Results"));
                for (ServerInfo server : servers) {
                    String remoteSearchUrl = server.getUrl()
                                             + URL_ENTRY_SEARCH.getPath()
                                             + "?" + linkUrl;
                    sb.append("\n");
                    sb.append(HtmlUtils.p());
                    String link = HtmlUtils.href(remoteSearchUrl,
                                      server.getUrl());
                    String fullUrl = server.getUrl()
                                     + URL_ENTRY_SEARCH.getPath() + "?"
                                     + embeddedUrl;
                    String content =
                        HtmlUtils.tag(
                            HtmlUtils.TAG_IFRAME,
                            HtmlUtils.attrs(
                                HtmlUtils.ATTR_WIDTH, "100%",
                                HtmlUtils.ATTR_HEIGHT, "200",
                                HtmlUtils.ATTR_SRC,
                                fullUrl), "need to have iframe support");
                    sb.append(HtmlUtils.makeShowHideBlock(server.getLabel()
                            + HtmlUtils.space(2) + link, content, true));

                    sb.append("\n");
                }
                request.remove(ARG_DECORATE);
                request.remove(ARG_TARGET);

                return new Result("Remote Search Results", sb);
            }

            Entry tmpGroup = getEntryManager().getDummyGroup();
            doDistributedSearch(request, servers, tmpGroup, groups, entries);
            Result result = getRepository().getOutputHandler(
                                request).outputGroup(
                                request, request.getOutput(), tmpGroup,
                                groups, entries);

            return result;

        }

        Entry theGroup = null;

        if (request.defined(ARG_GROUP)) {
            String groupId = (String) request.getString(ARG_GROUP, "").trim();
            //            System.err.println("group:" + groupId);
            theGroup = getEntryManager().findGroup(request, groupId);
        }


        String s = searchCriteriaSB.toString();
        if (request.defined(ARG_TARGET)) {
            s = "";
        }


        //        if (s.length() > 0) {
        StringBuffer searchForm = new StringBuffer();
        request.remove(ARG_SEARCH_SUBMIT);
        String url = request.getUrl(URL_SEARCH_FORM);
        String searchLink =
            HtmlUtils.href(url,
                           HtmlUtils.img(iconUrl(ICON_SEARCH),
                                         "Search Again"));
        //            searchForm.append(searchLink);
        if (s.length() > 0) {
            searchForm.append(msg("Search Criteria") + "<br><table>" + s
                              + "</table>");
        }
        boolean foundAny = (groups.size() > 0) || (entries.size() > 0);
        if (foundAny) {
            String searchUrl = request.getUrl();
            searchForm.append(HtmlUtils.href(searchUrl, msg("Search URL")));
            searchForm.append(HtmlUtils.br());
        }



        makeSearchForm(request, textSearch, true, searchForm);


        String form = HtmlUtils.makeShowHideBlock(
                          searchLink + msg("Search Again"),
                          HtmlUtils.inset(
                              searchForm.toString(), 0, 20, 0, 0), false);
        StringBuffer header = new StringBuffer();
        header.append(getPageHandler().makeHeader(request, getSearchUrls(),
                ""));
        header.append(msgHeader("Search Results"));

        if (foundAny) {
            header.append(form);
        } else {
            header.append(searchForm);
        }

        request.setLeftMessage(header.toString());
        //        }
        if (theGroup == null) {
            theGroup = getEntryManager().getDummyGroup();
        }
        Result result =
            getRepository().getOutputHandler(request).outputGroup(request,
                                             request.getOutput(), theGroup,
                                             groups, entries);
        //        return makeResult(request, msg("Search Results"), sb);
        if (theGroup.isDummy()) {
            return addHeaderToAncillaryPage(request, result);
        }

        return getEntryManager().addEntryHeader(request, theGroup, result);
    }




    /**
     * _more_
     *
     * @param request _more_
     * @param servers _more_
     * @param tmpEntry _more_
     * @param groups _more_
     * @param entries _more_
     *
     * @throws Exception _more_
     */
    private void doDistributedSearch(final Request request,
                                     List<ServerInfo> servers,
                                     Entry tmpEntry,
                                     final List<Entry> groups,
                                     final List<Entry> entries)
            throws Exception {

        String output = request.getString(ARG_OUTPUT, "");
        request.put(ARG_OUTPUT, XmlOutputHandler.OUTPUT_XML);
        final String    linkUrl     = request.getUrlArgs();
        ServerInfo      thisServer  = getRepository().getServerInfo();
        final int[]     runnableCnt = { 0 };
        final boolean[] running     = { true };
        //TODO: We need to cap the number of servers we're searching on
        List<Runnable> runnables = new ArrayList<Runnable>();
        for (ServerInfo server : servers) {
            if (server.equals(thisServer)) {
                continue;
            }
            final Entry parentEntry =
                new Entry(getRepository().getGroupTypeHandler(), true);
            parentEntry.setId(
                getEntryManager().getRemoteEntryId(server.getUrl(), ""));
            getEntryManager().cacheEntry(parentEntry);
            parentEntry.setRemoteServer(server.getUrl());
            parentEntry.setIsRemoteEntry(true);
            parentEntry.setUser(getUserManager().getAnonymousUser());
            parentEntry.setParentEntry(tmpEntry);
            parentEntry.setName(server.getUrl());
            final ServerInfo theServer = server;
            Runnable         runnable  = new Runnable() {
                public void run() {
                    String remoteSearchUrl = theServer.getUrl()
                                             + URL_ENTRY_SEARCH.getPath()
                                             + "?" + linkUrl;

                    try {
                        String entriesXml =
                            getStorageManager().readSystemResource(
                                new URL(remoteSearchUrl));
                        //                            System.err.println(entriesXml);
                        if ( !running[0]) {
                            return;
                        }
                        Element  root     = XmlUtil.getRoot(entriesXml);
                        NodeList children = XmlUtil.getElements(root);
                        //Synchronize on the groups list so only one thread at  a time adds its entries to it
                        synchronized (groups) {
                            for (int i = 0; i < children.getLength(); i++) {
                                Element node = (Element) children.item(i);
                                //                    if (!node.getTagName().equals(TAG_ENTRY)) {continue;}
                                Entry entry =
                                    getEntryManager().createEntryFromXml(
                                        request, node, parentEntry,
                                        new Hashtable(), false, false);

                                entry.setResource(
                                    new Resource(
                                        "remote:"
                                        + XmlUtil.getAttribute(
                                            node, ATTR_RESOURCE,
                                            ""), Resource.TYPE_REMOTE_FILE));
                                entry.setId(
                                    getEntryManager().getRemoteEntryId(
                                        theServer.getUrl(),
                                        XmlUtil.getAttribute(node, ATTR_ID)));
                                entry.setIsRemoteEntry(true);
                                entry.setRemoteServer(theServer.getUrl());
                                getEntryManager().cacheEntry(entry);
                                if (entry.isGroup()) {
                                    groups.add((Entry) entry);
                                } else {
                                    entries.add((Entry) entry);
                                }
                            }
                        }
                    } catch (Exception exc) {
                        logException("Error doing search:" + remoteSearchUrl,
                                     exc);
                    } finally {
                        synchronized (runnableCnt) {
                            runnableCnt[0]--;
                        }
                    }
                }

                public String toString() {
                    return "Runnable:" + theServer.getUrl();
                }
            };
            runnables.add(runnable);
        }


        runnableCnt[0] = runnables.size();
        for (Runnable runnable : runnables) {
            Misc.runInABit(0, runnable);
        }


        //Wait at most 10 seconds for all of the thread to finish
        long t1 = System.currentTimeMillis();
        while (true) {
            synchronized (runnableCnt) {
                if (runnableCnt[0] <= 0) {
                    break;
                }
            }
            //Busy loop
            Misc.sleep(100);
            long t2 = System.currentTimeMillis();
            //Wait at most 10 seconds
            if ((t2 - t1) > 20000) {
                logInfo("Remote search waited too long");

                break;
            }
        }
        running[0] = false;



        request.put(ARG_OUTPUT, output);


    }

    /**
     * _more_
     *
     *
     * @param request _more_
     * @param what _more_
     * @return _more_
     *
     * @throws Exception _more_
     */
    protected List getSearchFormLinks(Request request, String what)
            throws Exception {
        TypeHandler typeHandler = getRepository().getTypeHandler(request);
        List        links       = new ArrayList();
        String      extra1      = " class=subnavnolink ";
        String      extra2      = " class=subnavlink ";
        String[]    whats       = { WHAT_ENTRIES, WHAT_TAG,
                                    WHAT_ASSOCIATION };
        String[]    names       = { LABEL_ENTRIES, "Tags", "Associations" };

        String      formType    = request.getString(ARG_FORM_TYPE, "basic");

        for (int i = 0; i < whats.length; i++) {
            String item;
            if (what.equals(whats[i])) {
                item = HtmlUtils.span(names[i], extra1);
            } else {
                item = HtmlUtils.href(request.url(URL_SEARCH_FORM, ARG_WHAT,
                        whats[i], ARG_FORM_TYPE, formType), names[i], extra2);
            }
            if (i == 0) {
                item = "<span " + extra1
                       + ">Search For:&nbsp;&nbsp;&nbsp; </span>" + item;
            }
            links.add(item);
        }

        List<TwoFacedObject> whatList = typeHandler.getListTypes(false);
        for (TwoFacedObject tfo : whatList) {
            if (tfo.getId().equals(what)) {
                links.add(HtmlUtils.span(tfo.toString(), extra1));
            } else {
                links.add(HtmlUtils.href(request.url(URL_SEARCH_FORM,
                        ARG_WHAT, BLANK + tfo.getId(), ARG_TYPE,
                        typeHandler.getType()), tfo.toString(), extra2));
            }
        }

        return links;
    }

    /**
     * _more_
     *
     * @param args _more_
     *
     * @throws Exception _more_
     */
    public static void main(String[] args) throws Exception {
        for (String f : args) {
            InputStream stream = new FileInputStream(f);
            org.apache.tika.metadata.Metadata metadata =
                new org.apache.tika.metadata.Metadata();
            org.apache.tika.parser.AutoDetectParser parser =
                new org.apache.tika.parser.AutoDetectParser();
            org.apache.tika.sax.BodyContentHandler handler =
                new org.apache.tika.sax.BodyContentHandler(100000000);
            parser.parse(stream, handler, metadata);
            String contents = handler.toString();
            //            System.out.println("contents: " + contents);
        }
    }

}
