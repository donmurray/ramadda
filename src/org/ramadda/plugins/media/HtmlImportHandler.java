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

package org.ramadda.plugins.media;


import org.ramadda.repository.*;
import org.ramadda.repository.metadata.Metadata;
import org.ramadda.repository.type.TypeHandler;
import org.ramadda.util.HtmlUtils;

import org.w3c.dom.*;

import ucar.unidata.util.IOUtil;
import ucar.unidata.util.StringUtil;
import ucar.unidata.util.TwoFacedObject;


import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;

import java.net.URL;

import java.util.ArrayList;
import java.util.List;

import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;


/**
 */
public class HtmlImportHandler extends ImportHandler {

    /** _more_          */
    public static final String ARG_IMPORT_PATTERN = "import.pattern";

    /** _more_          */
    public static final String ARG_IMPORT_DOIT = "import.doit";

    /** _more_          */
    public static final String ARG_IMPORT_PROVENANCE = "import.addprovenance";

    /** _more_          */
    public static final String ARG_IMPORT_UNCOMPRESS = "import.uncompress";

    /** _more_          */
    public static final String ARG_IMPORT_HANDLE = "import.handle";

    /** _more_          */
    public static final String TYPE_HTML = "html";

    /**
     * _more_
     */
    public HtmlImportHandler() {
        super(null);
    }

    /**
     * _more_
     *
     * @param repository _more_
     */
    public HtmlImportHandler(Repository repository) {
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
        importTypes.add(new TwoFacedObject("Links in an HTML Page", TYPE_HTML));
    }


    /**
     * _more_
     *
     * @param request _more_
     * @param repository _more_
     * @param url _more_
     * @param parentEntry _more_
     * @param links _more_
     *
     * @return _more_
     *
     * @throws Exception _more_
     */
    public Result importHtml(final Request request,
                             final Repository repository, String url,
                             final Entry parentEntry,
                             final List<HtmlUtils.Link> links)
            throws Exception {

        //IMPORTANT!
        request.ensureAuthToken();

        ActionManager.Action action = new ActionManager.Action() {

            public void run(Object actionId) throws Exception {

                List<String>  errors = new ArrayList<String>();
                StringBuilder sb     = new StringBuilder();
                boolean addFile = request.getString(ARG_IMPORT_HANDLE,
                                      "").equals("file");
                boolean uncompress = request.get(ARG_IMPORT_UNCOMPRESS,
                                         false);
                boolean addProvenance = request.get(ARG_IMPORT_PROVENANCE,
                                            false);

                if ( !getActionManager().getActionOk(actionId)) {
                    return;
                }

                sb.append("<ul>");
                for (HtmlUtils.Link link : links) {
                    Resource    resource    = null;
                    TypeHandler typeHandler = null;
                    String      name        = link.getLabel();
                    //TODO: check if we have a entry already
                    Entry existing =
                        getEntryManager().findEntryWithName(request,
                            parentEntry, name);
                    if (existing == null) {
                        String tmp = IOUtil.stripExtension(name);
                        existing =
                            getEntryManager().findEntryWithName(request,
                                parentEntry, tmp);
                    }
                    if (existing != null) {
                        sb.append("<li> ");
                        sb.append(msgLabel("Entry already exists"));
                        sb.append(" ");
                        sb.append(link.getUrl());
                        sb.append("\n");

                        continue;
                    }


                    if (addFile) {
                        File tmpFile =
                            getStorageManager().getTmpFile(request,
                                IOUtil.getFileTail(link.getUrl().toString()));
                        FileOutputStream fos = new FileOutputStream(tmpFile);
                        if (IOUtil.writeTo(
                                IOUtil.getInputStream(
                                    link.getUrl().toString()), fos) == 0) {
                            errors.add("Failed to read url:" + link.getUrl());
                            IOUtil.close(fos);

                            continue;
                        }
                        IOUtil.close(fos);

                        if (request.get(ARG_IMPORT_UNCOMPRESS, false)) {
                            tmpFile = getStorageManager().uncompressIfNeeded(
                                request, tmpFile);
                            if (tmpFile == null) {
                                errors.add("Failed to uncompress file:"
                                           + tmpFile);

                                continue;
                            }
                            name = RepositoryUtil.getFileTail(
                                tmpFile.getName());
                            //                                System.err.println("NAME:" + name);
                        }
                        tmpFile = getStorageManager().moveToStorage(request,
                                tmpFile);
                        typeHandler =
                            getEntryManager().findDefaultTypeHandler(
                                tmpFile.toString());
                        resource = new Resource(tmpFile,
                                Resource.TYPE_STOREDFILE);
                    } else {
                        resource = new Resource(link.getUrl().toString(),
                                Resource.TYPE_URL);
                        getEntryManager().findDefaultTypeHandler(
                            link.getUrl().toString());
                    }

                    Entry entry = getEntryManager().makeEntry(request,
                                      resource, parentEntry, name, "",
                                      request.getUser(), typeHandler, null);

                    if (addProvenance) {
                        entry.addMetadata(
                            new Metadata(
                                getRepository().getGUID(), entry.getId(),
                                "metadata_source", false,
                                link.getUrl().toString(),
                                "RAMADDA entry import", null, null, null));
                    }


                    getEntryManager().addNewEntry(request, entry);
                    sb.append("<li> ");
                    sb.append(
                        HtmlUtils.href(
                            request.entryUrl(
                                getRepository().URL_ENTRY_SHOW,
                                entry), entry.getName()));
                    sb.append(HtmlUtils.br());
                    getActionManager().setActionMessage(actionId,
                            "<h2>Imported entries</h2>" + sb.toString());
                }
                sb.append("</ul>");

                if (errors.size() > 0) {
                    sb.append(
                        getPageHandler().showDialogError(
                            StringUtil.join("<br>", errors)));
                }

                getActionManager().setActionMessage(actionId, sb.toString());
                getActionManager().setContinueHtml(actionId, sb.toString());

            }

        };

        return getActionManager().doAction(request, action, "Importing HTML",
                                           "", parentEntry);

        //        return getEntryManager().makeEntryEditResult(request, parentEntry, "HTML Import", sb);

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
    public Result handleUrlRequest(Request request, Repository repository,
                                   String url, Entry parentEntry)
            throws Exception {


        if ( !request.getString(ARG_IMPORT_TYPE, "").equals(TYPE_HTML)) {
            return null;
        }
        StringBuilder sb = new StringBuilder();

        String pattern   = request.getString(ARG_IMPORT_PATTERN, "");

        List<HtmlUtils.Link> links = HtmlUtils.extractLinks(new URL(url),
                                         pattern);
        if (request.exists(ARG_IMPORT_DOIT)) {
            return importHtml(request, repository, url, parentEntry, links);
        }


        String buttons =
            HtmlUtils.buttons(HtmlUtils.submit(msg("Test it out")),
                              HtmlUtils.submit("Create entries",
                                  ARG_IMPORT_DOIT));
        sb.append(msgHeader("HTML Import"));
        request.uploadFormWithAuthToken(sb,
                                        getRepository().URL_ENTRY_XMLCREATE,
                                        makeFormSubmitDialog(sb,
                                            msg("Importing HTML")));
        sb.append(HtmlUtils.hidden(ARG_GROUP, parentEntry.getId()));
        sb.append(HtmlUtils.hidden(ARG_IMPORT_TYPE, TYPE_HTML));
        sb.append(HtmlUtils.formTable());
        sb.append(HtmlUtils.formEntry(msgLabel("URL"),
                                      HtmlUtils.input(ARG_URL, url,
                                          HtmlUtils.SIZE_70)));

        sb.append(
            HtmlUtils.formEntry(
                msgLabel("Pattern"),
                HtmlUtils.input(
                    ARG_IMPORT_PATTERN, pattern, HtmlUtils.SIZE_70) + " "
                        + msg("regular expression - add .*")));

        sb.append(
            HtmlUtils.formEntry(
                msgLabel("What to do"),
                HtmlUtils.radio(ARG_IMPORT_HANDLE, "file", true)
                + HtmlUtils.space(1) + msg("Download the file")
                + HtmlUtils.space(3)
                + HtmlUtils.radio(ARG_IMPORT_HANDLE, "url", false)
                + HtmlUtils.space(1) + msg("Add the link")));


        sb.append(
            HtmlUtils.formEntry(
                "",
                HtmlUtils.checkbox(
                    ARG_IMPORT_PROVENANCE, "true",
                    request.get(ARG_IMPORT_PROVENANCE, false)) + " "
                        + msg("Add the source URL as provenance metadata")));

        sb.append(
            HtmlUtils.formEntry(
                "",
                HtmlUtils.checkbox(
                    ARG_IMPORT_UNCOMPRESS, "true",
                    request.get(ARG_IMPORT_UNCOMPRESS, false)) + " "
                        + msg("Uncompress file")));

        sb.append(HtmlUtils.formEntry("", buttons));
        sb.append(HtmlUtils.formTableClose());

        sb.append(HtmlUtils.p());

        if (links.size() > 0) {
            sb.append(msgHeader("Links Import"));
            sb.append("<ul>");
            for (HtmlUtils.Link link : links) {
                sb.append("<li> ");
                sb.append(link.getHref());
                if (link.getSize() > 0) {
                    sb.append("  --  ");
                    sb.append(
                        " "
                        + RepositoryManager.formatFileLength(link.getSize()));
                }
                sb.append("  --  ");
                sb.append(link.getUrl());
                sb.append(HtmlUtils.br());
            }
            sb.append("</ul>");


        } else {
            sb.append(
                getPageHandler().showDialogNote(
                    "No links found. Maybe add \".*\" before and after pattern"));
        }
        sb.append(HtmlUtils.formClose());

        return getEntryManager().makeEntryEditResult(request, parentEntry,
                "HTML Import", sb);
        //        return getEntryManager().addEntryHeader(request, parentEntry, new Result("",sb));
    }


    /**
     * _more_
     *
     * @param args _more_
     *
     * @throws Exception _more_
     */
    public static void main(String[] args) throws Exception {
        String url =
            "https://www.aoncadis.org/download/fileDownload.htm?logicalFileId=71073a32-dbb1-11e3-85a2-00c0f03d5b7c";
        url = args[0];
        url = "ftp://n5eil01u.ecs.nsidc.org/SAN2/ICEBRIDGE/ILATM1B.002/2013.03.21";
        System.out.println(IOUtil.readContents(url, HtmlUtils.class));
        //        List<HtmlUtils.Link> links = HtmlUtils.extractLinks(new URL(url), args.length>1?args[1]:null);
        //        System.err.println ("Links:"  + links);
    }


    /**
     * _more_
     *
     * @param file _more_
     *
     * @throws Exception _more_
     */
    public static void test(String file) throws Exception {
        String html = IOUtil.readContents(file, HtmlUtils.class);
        //        String pattern = "(?i)<\\s*a href\\s*=\\s*\"?([^\">]+)\"?>(.+?)</a>";

        String pattern =
            "(?i)<\\s*a href\\s*=\\s*\"?([^\">]+)\"?[^>]*>(.+)</a>";
        Matcher matcher = Pattern.compile(pattern).matcher(html);
        while (matcher.find()) {
            String href = matcher.group(1);
            //            String label = matcher.group(2);
            System.err.println(href);
        }
    }



}
