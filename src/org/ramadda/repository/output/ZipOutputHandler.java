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

package org.ramadda.repository.output;



import org.ramadda.repository.*;
import org.ramadda.repository.auth.*;


import org.ramadda.repository.util.FileWriter;


import org.ramadda.sql.SqlUtil;
import org.ramadda.util.HtmlUtils;

import org.w3c.dom.*;

import ucar.unidata.util.DateUtil;
import ucar.unidata.util.IOUtil;
import ucar.unidata.util.Misc;

import ucar.unidata.util.StringUtil;
import ucar.unidata.xml.XmlUtil;


import java.io.*;

import java.io.File;
import java.io.InputStream;



import java.net.*;



import java.util.ArrayList;
import java.util.Date;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.List;
import java.util.Properties;



import java.util.regex.*;

import java.util.zip.*;


/**
 *
 *
 *
 * @author RAMADDA Development Team
 * @version $Revision: 1.3 $
 */
public class ZipOutputHandler extends OutputHandler {

    /** _more_ */
    private static final String ARG_WRITETODISK = "writetodisk";

    /** _more_ */
    private final LogManager.LogId LOGID =
        new LogManager.LogId(
            "org.ramadda.repository.output.ZipOutputHandler");



    /** _more_ */
    public static final OutputType OUTPUT_ZIP =
        new OutputType("Zip and Download File", "zip.zip",
                       OutputType.TYPE_FILE, "", ICON_ZIP);


    /** _more_ */
    public static final OutputType OUTPUT_ZIPTREE =
        new OutputType("Zip and Download Tree", "zip.tree",
                       OutputType.TYPE_FILE, "", ICON_ZIP);


    /** _more_ */
    public static final OutputType OUTPUT_ZIPGROUP =
        new OutputType("Zip and Download Files", "zip.zipgroup",
                       OutputType.TYPE_FILE, "", ICON_ZIP);

    /** _more_          */
    public static final OutputType OUTPUT_EXPORT =
        new OutputType("Export Entries", "zip.export", OutputType.TYPE_FILE,
                       "", ICON_ZIP);


    /**
     * _more_
     *
     * @param repository _more_
     * @param element _more_
     * @throws Exception _more_
     */
    public ZipOutputHandler(Repository repository, Element element)
            throws Exception {
        super(repository, element);
        addType(OUTPUT_ZIP);
        addType(OUTPUT_ZIPGROUP);
        addType(OUTPUT_ZIPTREE);
        addType(OUTPUT_EXPORT);
    }


    /**
     * _more_
     *
     * @param request _more_
     *
     * @return _more_
     */
    public AuthorizationMethod getAuthorizationMethod(Request request) {
        return AuthorizationMethod.AUTH_HTTP;
    }


    /**
     * _more_
     *
     * @param request _more_
     * @param state _more_
     * @param links _more_
     *
     * @throws Exception _more_
     */
    public void getEntryLinks(Request request, State state, List<Link> links)
            throws Exception {

        if (state.entry != null) {
            if (getAccessManager().canDownload(request, state.entry)) {
                links.add(
                    makeLink(
                        request, state.entry, OUTPUT_ZIP,
                        "/" + IOUtil.stripExtension(state.entry.getName())
                        + ".zip"));
            }

            return;
        }

        boolean hasFile  = false;
        boolean hasGroup = false;
        for (Entry child : state.getAllEntries()) {
            if (getAccessManager().canDownload(request, child)) {
                hasFile = true;

                break;
            }
            if (child.isGroup()) {
                hasGroup = true;
            }
        }



        if (hasFile) {
            if (state.group != null) {
                links.add(
                    makeLink(
                        request, state.group, OUTPUT_ZIPGROUP,
                        "/" + IOUtil.stripExtension(state.group.getName())
                        + ".zip"));
            } else {
                links.add(makeLink(request, state.group, OUTPUT_ZIP));
            }
        }


        if ((state.group != null) && hasGroup
                && ( !state.group.isTopEntry() || state.group.isDummy())) {
            links.add(makeLink(request, state.group, OUTPUT_ZIPTREE,
                               "/"
                               + IOUtil.stripExtension(state.group.getName())
                               + ".zip"));
        }

    }




    /**
     * _more_
     *
     * @param request _more_
     * @param outputType _more_
     * @param entry _more_
     *
     * @return _more_
     *
     * @throws Exception _more_
     */
    public Result outputEntry(Request request, OutputType outputType,
                              Entry entry)
            throws Exception {
        List<Entry> entries = new ArrayList<Entry>();
        entries.add(entry);

        return toZip(request, "", entries, false, false);
    }


    /**
     * _more_
     *
     * @param request _more_
     * @param outputType _more_
     * @param group _more_
     * @param subGroups _more_
     * @param entries _more_
     *
     * @return _more_
     *
     * @throws Exception _more_
     */
    public Result outputGroup(Request request, OutputType outputType,
                              Entry group, List<Entry> subGroups,
                              List<Entry> entries)
            throws Exception {

        if (group.isDummy()) {
            request.setReturnFilename("Search_Results.zip");
        }

        OutputType output = request.getOutput();
        request.setReturnFilename(IOUtil.stripExtension(group.getName())+ ".zip");
        if (output.equals(OUTPUT_ZIPTREE)) {
            List<Entry> all = new ArrayList<Entry>();
            all.addAll(subGroups);
            all.addAll(entries);
            getLogManager().logInfo("Doing zip tree");

            return toZip(request, group.getName(), all, true, false);
        }
        if (output.equals(OUTPUT_EXPORT)) {
            List<Entry> all = new ArrayList<Entry>();
            all.addAll(subGroups);
            all.addAll(entries);

            return toZip(request, group.getName(), all, true, true);
        } else {
            return toZip(request, group.getName(), entries, false, false);
        }
    }



    /**
     * _more_
     *
     * @param output _more_
     *
     * @return _more_
     */
    public String getMimeType(OutputType output) {
        if (output.equals(OUTPUT_ZIP) || output.equals(OUTPUT_ZIPGROUP)) {
            return repository.getMimeTypeFromSuffix(".zip");
        } else {
            return super.getMimeType(output);
        }
    }





    /**
     * _more_
     *
     * @param request _more_
     * @param prefix _more_
     * @param entries _more_
     * @param recurse _more_
     * @param forExport _more_
     *
     * @return _more_
     *
     * @throws Exception _more_
     */
    public Result toZip(Request request, String prefix, List<Entry> entries,
                        boolean recurse, boolean forExport)
            throws Exception {
        OutputStream os        = null;
        boolean      doingFile = false;
        File         tmpFile   = null;



        Element      root      = null;
        boolean      ok        = true;
        //First recurse down without a zos to check the size
        try {
            processZip(request, entries, recurse, 0, null, prefix, 0,
                       new int[] { 0 }, forExport, null);
        } catch (IllegalArgumentException iae) {
            ok = false;
        }
        if ( !ok) {
            return new Result(
                "Error",
                new StringBuffer(
                    "Size of request has exceeded maximum size"));
        }


        Result     result         = new Result();
        FileWriter fileWriter     = null;



        boolean    writeToDisk    = request.get(ARG_WRITETODISK, false);
        File       writeToDiskDir = null;
        if (writeToDisk) {
            //IMPORTANT: Make sure that the user is an admin when handling the write to disk 
            request.ensureAdmin();
            forExport = true;
            writeToDiskDir =
                getStorageManager().makeTempDir(getRepository().getGUID(),
                    false).getDir();
            fileWriter = new FileWriter(writeToDiskDir);
        } else {
            if (request.getHttpServletResponse() != null) {
                os = request.getHttpServletResponse().getOutputStream();
                request.getHttpServletResponse().setContentType(
                    getMimeType(OUTPUT_ZIP));
            } else {
                tmpFile =
                    getRepository().getStorageManager().getTmpFile(request,
                        ".zip");
                os = getStorageManager().getUncheckedFileOutputStream(
                    tmpFile);
                doingFile = true;
            }
            fileWriter = new FileWriter(new ZipOutputStream(os));
            result.setNeedToWrite(false);
            if (request.get(ARG_COMPRESS, true) == false) {
                //You would think that setting the method to stored would work
                //but it throws an error wanting the crc to be set on the ZipEntry
                //            zos.setMethod(ZipOutputStream.STORED);
                fileWriter.setCompressionOn();
            }
        }

        Hashtable seen = new Hashtable();
        try {
            if (forExport) {
                Document doc = XmlUtil.makeDocument();
                root = XmlUtil.create(doc, TAG_ENTRIES, null,
                                      new String[] {});

            }
            processZip(request, entries, recurse, 0, fileWriter, prefix, 0,
                       new int[] { 0 }, forExport, root);

            if (root != null) {
                String xml = XmlUtil.toString(root);
                fileWriter.writeFile("entries.xml", xml.getBytes());
            }
        } finally {
            fileWriter.close();
        }
        if (doingFile) {
            IOUtil.close(os);

            return new Result(
                "", getStorageManager().getFileInputStream(tmpFile),
                getMimeType(OUTPUT_ZIP));

        }
        getLogManager().logInfo("Zip File ended");

        if (writeToDisk) {
            return new Result("Export",
                              new StringBuffer("<p>Exported to:<br>"
                                  + writeToDiskDir));
        }

        return result;
    }


    /**
     * _more_
     *
     * @param request _more_
     * @param entries _more_
     * @param recurse _more_
     * @param level _more_
     * @param fileWriter _more_
     * @param prefix _more_
     * @param sizeSoFar _more_
     * @param counter _more_
     * @param forExport _more_
     * @param entriesRoot _more_
     *
     * @return _more_
     *
     * @throws Exception _more_
     */
    protected long processZip(Request request, List<Entry> entries,
                              boolean recurse, int level,
                              FileWriter fileWriter, String prefix,
                              long sizeSoFar, int[] counter,
                              boolean forExport, Element entriesRoot)
            throws Exception {

        long      sizeProcessed = 0;
        Hashtable seen          = new Hashtable();
        long      sizeLimit;
        if (request.isAnonymous()) {
            sizeLimit = MEGA
                        * getRepository().getProperty(
                            request.PROP_ZIPOUTPUT_ANONYMOUS_MAXSIZEMB, 100);
        } else {
            sizeLimit = MEGA
                        * getRepository().getProperty(
                            request.PROP_ZIPOUTPUT_REGISTERED_MAXSIZEMB,
                            2000);
        }
        for (Entry entry : entries) {
            //Not sure why I wasn't dealing with synthetic entries here
            //if (getEntryManager().isSynthEntry(entry.getId())) {
            //                continue;
            //            }
            counter[0]++;
            //We are getting some weirdness in the database connections so lets
            //sleep a bit every 100 entries we see
            /*
              For now comment this out
            if (counter[0] % 100 == 0) {
//                System.err.println("zip count:" + counter[0] + " "
//                                   + new Date());
                Misc.sleep(10);
            }
            */

            //Don't get big files
            if (request.defined(ARG_MAXFILESIZE) && entry.isFile()) {
                if (entry.getFile().length()
                        >= request.get(ARG_MAXFILESIZE, 0)) {
                    continue;
                }
            }

            Element entryNode = null;
            if (forExport && (entriesRoot != null)) {
                entryNode =
                    getRepository().getXmlOutputHandler().getEntryTag(null,
                        entry, fileWriter, entriesRoot.getOwnerDocument(),
                        entriesRoot, true, level != 0);
            }

            if (entry.isGroup() && recurse) {
                Entry group = (Entry) entry;
                List<Entry> children = getEntryManager().getChildren(request,
                                           group);
                String path = group.getName();
                if (prefix.length() > 0) {
                    path = prefix + "/" + path;
                }
                sizeProcessed += processZip(request, children, recurse,
                                            level + 1, fileWriter, path,
                                            sizeProcessed + sizeSoFar,
                                            counter, forExport, entriesRoot);
            }


            //            getLogManager().logInfo("Zip generated size =" + sizeProcessed);
            if ( !getAccessManager().canDownload(request, entry)) {
                continue;
            }


            String path = entry.getResource().getPath();
            String name = getStorageManager().getFileTail(entry);
            int    cnt  = 1;
            if ( !forExport) {
                while (seen.get(name) != null) {
                    name = (cnt++) + "_" + name;
                }
                seen.put(name, name);
                if (prefix.length() > 0) {
                    name = prefix + "/" + name;
                }
            }
            File f = new File(path);
            sizeProcessed += f.length();

            //check for size limit
            if (sizeSoFar + sizeProcessed > sizeLimit) {
                throw new IllegalArgumentException(
                    "Size of request has exceeded maximum size");
            }


            if (fileWriter != null) {
                InputStream fis =
                    getStorageManager().getFileInputStream(path);
                if ((entryNode != null) && forExport) {
                    fileWriter.writeFile(entry.getId(), fis);
                    XmlUtil.setAttributes(entryNode, new String[] { ATTR_FILE,
                            entry.getId(), ATTR_FILENAME, name });

                } else {
                    fileWriter.writeFile(name, fis);
                }
            }
        }

        return sizeProcessed;

    }

}
