/*
 * Copyright 1997-2010 Unidata Program Center/University Corporation for
 * Atmospheric Research, P.O. Box 3000, Boulder, CO 80307,
 * support@unidata.ucar.edu.
 * 
 * This library is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation; either version 2.1 of the License, or (at
 * your option) any later version.
 * 
 * This library is distributed in the hope that it will be useful, but
 * WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU Lesser
 * General Public License for more details.
 * 
 * You should have received a copy of the GNU Lesser General Public License
 * along with this library; if not, write to the Free Software Foundation,
 * Inc., 59 Temple Place, Suite 330, Boston, MA 02111-1307 USA
 */

package org.ramadda.repository.idv;


import org.w3c.dom.*;

import org.ramadda.repository.*;
import org.ramadda.repository.data.*;
import org.ramadda.repository.metadata.*;
import org.ramadda.repository.output.*;


import ucar.unidata.sql.SqlUtil;
import ucar.unidata.ui.ImageUtils;
import ucar.unidata.util.DateUtil;
import ucar.unidata.util.HtmlUtil;
import ucar.unidata.util.IOUtil;
import ucar.unidata.util.Misc;

import ucar.unidata.util.StringBufferCollection;
import ucar.unidata.util.StringUtil;
import ucar.unidata.xml.XmlUtil;


import java.io.*;

import java.io.File;
import java.io.InputStream;



import java.net.*;



import java.text.SimpleDateFormat;

import java.util.ArrayList;
import java.util.Date;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.List;
import java.util.Properties;



import java.util.regex.*;

import java.util.zip.*;


/**
 * Class SqlUtil _more_
 *
 *
 * @author IDV Development Team
 * @version $Revision: 1.3 $
 */
public class IdvWebstartOutputHandler extends OutputHandler {

    /** _more_ */
    private static String jnlpTemplate;



    /** _more_ */
    public static final OutputType OUTPUT_WEBSTART =
        new OutputType("View in IDV", "idv.webstart",
                       OutputType.TYPE_CATEGORY, "", "/icons/idv.gif",
                       DataOutputHandler.GROUP_DATA);



    /**
     * _more_
     *
     * @param repository _more_
     * @param element _more_
     * @throws Exception _more_
     */
    public IdvWebstartOutputHandler(Repository repository, Element element)
            throws Exception {
        super(repository, element);
        addType(OUTPUT_WEBSTART);
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

        Entry entry = state.getEntry();
        if (entry == null) {
            return;
        }
        if (entry.getResource().getPath().endsWith(".xidv")
                || entry.getResource().getPath().endsWith(".zidv")) {
            String fileTail = getStorageManager().getFileTail(entry);
            String suffix   = "/" + IOUtil.stripExtension(fileTail) + ".jnlp";
            //                suffix = java.net.URLEncoder.encode(suffix);
            links.add(makeLink(request, state.getEntry(), OUTPUT_WEBSTART,
                               suffix));
        } else {
            DataOutputHandler data =
                (DataOutputHandler) getRepository().getOutputHandler(
                    DataOutputHandler.OUTPUT_OPENDAP);
            if (data != null) {
                if (data.canLoadAsCdm(entry)) {
                    String suffix = "/" + entry.getId() + ".jnlp";
                    links.add(makeLink(request, state.getEntry(),
                                       OUTPUT_WEBSTART, suffix));
                }
            }

        }
    }





    public Result outputGroup(Request request, OutputType outputType,
                              Entry group, List<Entry> subGroups,
                              List<Entry> entries)
            throws Exception {
        return outputEntry(request, outputType, group);
    }


    public static String getJnlpTemplate(Repository repository) throws Exception {
        if (jnlpTemplate == null) {
            String localPath = repository.getStorageManager().localizePath(
                                   repository.getProperty(
                                       "ramadda.idv.jnlp.template",
                                       (String) null));
            if (localPath != null) {
                try {
                    jnlpTemplate = IOUtil.readContents(
                                                       repository.getStorageManager().getInputStream(localPath));
                    repository.getLogManager().logInfo("IdvWebstartOutputHandler: using jnlp template: "
                            + localPath);
                } catch (Exception ignoreThis) {}
            }
            if (jnlpTemplate == null) {
                jnlpTemplate = repository.getResource(
                    "/org/ramadda/repository/idv/template.jnlp");
            }
            //Replace the macros
            for (String macro :
                    new String[] { "codebase", "href", "title", "description",
                                   "maxheapsize" }) {
                jnlpTemplate = jnlpTemplate.replace("${" + macro + "}",
                                                    repository.getProperty("ramadda.idv.jnlp." + macro, ""));
            }
        }
        return jnlpTemplate;
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

        String       jnlp = getJnlpTemplate(getRepository());

        StringBuffer args = new StringBuffer();
        if (entry.getResource().getPath().endsWith(".xidv")
                || entry.getResource().getPath().endsWith(".zidv")) {

            String fileTail = getStorageManager().getFileTail(entry);
            String url =
                HtmlUtil.url(request.url(getRepository().URL_ENTRY_GET) + "/"
                             + fileTail, ARG_ENTRYID, entry.getId());
            url = getRepository().absoluteUrl(url);
            args.append("<argument>-bundle</argument>");
            args.append("<argument>" + url + "</argument>");
        } else {

            List<Metadata> metadataList =
                getMetadataManager().findMetadata(entry,
                    ContentMetadataHandler.TYPE_ATTACHMENT, true);

            DataOutputHandler data =
                (DataOutputHandler) getRepository().getOutputHandler(
                    DataOutputHandler.OUTPUT_OPENDAP);
            if ((data != null) && data.canLoadAsCdm(entry)) {
                String embeddedBundle = null;
                String opendapUrl     = data.getFullTdsUrl(entry);
                if (metadataList != null) {
                    for (Metadata metadata : metadataList) {
                        if (metadata.getAttr1().endsWith(".xidv")) {
                            File xidvFile =
                                new File(IOUtil
                                    .joinDir(getRepository()
                                        .getStorageManager()
                                        .getEntryDir(metadata.getEntryId(),
                                            false), metadata.getAttr1()));
                            embeddedBundle =
                                getStorageManager().readSystemResource(
                                    xidvFile);
                            embeddedBundle =
                                embeddedBundle.replace("${datasource}",
                                    opendapUrl);
                            embeddedBundle = XmlUtil.encodeBase64(
                                embeddedBundle.getBytes());
                            break;
                        }
                    }
                }


                if (embeddedBundle != null) {
                    args.append(
                        "<argument>-b64bundle</argument>\n<argument>");
                    args.append(embeddedBundle);
                    args.append("</argument>\n");
                } else {
                    args.append("<argument>-data</argument>\n<argument>");
                    String type = "OPENDAP.GRID";
                    if (entry.getDataType() != null) {
                        if (entry.getDataType().equals("point")) {
                            type = "NetCDF.POINT";
                        }
                    }
                    args.append("type:" + type + ":" + opendapUrl);
                    args.append("</argument>\n");
                }
            }
        }
        jnlp = jnlp.replace("${args}", args.toString());

        return new Result("", new StringBuffer(jnlp),
                          "application/x-java-jnlp-file");
        //        return new Result("",new StringBuffer(jnlp),"text/xml");
    }



}
