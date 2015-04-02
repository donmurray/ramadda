/**
* Copyright (c) 2008-2015 Geode Systems LLC
* This Software is licensed under the Geode Systems RAMADDA License available in the source distribution in the file 
* ramadda_license.txt. The above copyright notice shall be included in all copies or substantial portions of the Software.
*/

/**
 * Copyright (c) 2008-2015 Geode Systems LLC
 * This Software is licensed under the Geode Systems RAMADDA License available in the source distribution in the file
 * ramadda_license.txt. The above copyright notice shall be included in all copies or substantial portions of the Software.
 */

package org.ramadda.plugins.media;


import org.ramadda.repository.*;
import org.ramadda.repository.metadata.*;
import org.ramadda.repository.type.*;
import org.ramadda.util.HtmlUtils;

import org.ramadda.util.XlsUtil;


import org.w3c.dom.*;

import ucar.unidata.util.IOUtil;

import ucar.unidata.util.Misc;
import ucar.unidata.util.StringUtil;


import ucar.unidata.xml.XmlUtil;

import java.io.File;
import java.io.InputStream;
import java.io.OutputStream;

import java.util.Date;
import java.util.Hashtable;
import java.util.List;

import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;


/**
 *
 *
 */
public class MsDocTypeHandler extends GenericTypeHandler {


    /**
     * _more_
     *
     * @param repository _more_
     * @param entryNode _more_
     *
     * @throws Exception _more_
     */
    public MsDocTypeHandler(Repository repository, Element entryNode)
            throws Exception {
        super(repository, entryNode);
    }


    /**
     * _more_
     *
     *
     * @param request _more_
     * @param entry _more_
     *
     * @throws Exception _more_
     */
    @Override
    public void initializeNewEntry(Request request, Entry entry)
            throws Exception {
        super.initializeNewEntry(request, entry);
        initializeDocEntry(request, entry);
    }


    /**
     * _more_
     *
     * @param request _more_
     * @param entry _more_
     *
     * @throws Exception _more_
     */
    public void initializeDocEntry(Request request, Entry entry)
            throws Exception {
        File file = entry.getFile();
        if ( !file.exists()) {
            return;
        }
        String filename = file.toString().toLowerCase();
        if ( !(filename.endsWith(".pptx") || filename.endsWith(".docx")
                || filename.endsWith(".xlsx"))) {
            return;
        }
        try {
            InputStream    fis = getStorageManager().getFileInputStream(file);
            OutputStream   fos = null;
            ZipInputStream zin = new ZipInputStream(fis);
            ZipEntry       ze  = null;
            while ((ze = zin.getNextEntry()) != null) {
                if (ze.isDirectory()) {
                    continue;
                }
                String  path        = ze.getName();
                String  lcpath      = path.toLowerCase();
                boolean isImage     = false;
                boolean isThumbnail = false;

                if (lcpath.endsWith("thumbnail.jpeg")) {
                    isThumbnail = isImage = true;
                } else if (lcpath.endsWith(".jpeg")
                           || lcpath.endsWith(".jpg")
                           || lcpath.endsWith(".png")
                           || lcpath.endsWith(".gif")) {
                    isImage = true;
                }

                //For now just extract the thumbnails, not all of the images
                if (isThumbnail) {
                    String thumbFile = IOUtil.getFileTail(path);
                    File   f = getStorageManager().getTmpFile(null,
                                   thumbFile);
                    fos = getStorageManager().getFileOutputStream(f);
                    try {
                        IOUtil.writeTo(zin, fos);
                    } finally {
                        IOUtil.close(fos);
                    }
                    String fileName =
                        getStorageManager().copyToEntryDir(entry,
                            f).getName();
                    Metadata metadata =
                        new Metadata(getRepository().getGUID(),
                                     entry.getId(), (isThumbnail
                            ? ContentMetadataHandler.TYPE_THUMBNAIL
                            : ContentMetadataHandler.TYPE_ATTACHMENT), false,
                                fileName, null, null, null, null);

                    entry.addMetadata(metadata);
                }
            }
        } catch (Exception exc) {
            System.err.println("oops:" + exc);
        }
    }

}
