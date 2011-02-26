/*
 * Copyright 2008-2011 Jeff McWhirter/ramadda.org
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
 * 
 */

package org.ramadda.geodata.echo;


import org.w3c.dom.*;

import org.ramadda.repository.*;
import org.ramadda.repository.database.Tables;
import org.ramadda.repository.database.DatabaseManager;
import org.ramadda.repository.harvester.*;
import org.ramadda.repository.auth.*;
import org.ramadda.repository.metadata.*;
import org.ramadda.repository.output.OutputHandler;
import org.ramadda.repository.output.OutputType;
import org.ramadda.repository.type.*;

import ucar.unidata.util.HtmlUtil;
import ucar.unidata.util.IOUtil;
import ucar.unidata.xml.XmlUtil;
import ucar.unidata.sql.Clause;
import ucar.unidata.sql.SqlUtil;

import java.text.SimpleDateFormat;
import java.util.zip.*;
import java.util.Date;
import java.io.*;

import java.sql.ResultSet;
import java.sql.Statement;




import ucar.unidata.xml.XmlUtil;

import java.util.ArrayList;
import java.util.List;


/**
 *
 */
public class EchoPublisher extends Harvester {

    public static final String ATTR_FTP_URL = "echo.ftp.url";

    public static final String ATTR_FTP_USER = "echo.ftp.user";

    private String ftpUrl="";

    private String ftpUser="";

    /** _more_ */
    private SimpleDateFormat sdf;

    /**
     * _more_
     *
     *
     * @throws Exception _more_
     */
    public EchoPublisher(Repository repository, String id)
        throws Exception {
        super(repository, id);
    }

    /**                                                                                                
     * _more_                                                                                          
     *                                                                                                 
     * @param repository _more_                                                                        
     * @param element _more_                                                                           
     *                                                                                                 
     * @throws Exception _more_                                                                        
     */
    public EchoPublisher(Repository repository, Element element)
        throws Exception {
        super(repository, element);
    }

    /**                                                                                                
     * _more_                                                                                          
     *                                                                                                 
     * @return _more_                                                                                  
     */
    public String getDescription() {
        return "ECHO Publisher";
    }



    protected void init(Element element) throws Exception {
        super.init(element);

        ftpUrl =  XmlUtil.getAttribute(element, ATTR_FTP_URL,ftpUrl);
        ftpUser =  XmlUtil.getAttribute(element, ATTR_FTP_USER,ftpUrl);


    }

    private String formatDate(long t) {
        if (sdf == null) {
            sdf = RepositoryUtil.makeDateFormat("yyyy-MM-dd'T'HH:mm:ss");
        }
        synchronized(sdf) {
            return sdf.format(new Date(t)) + "Z";
        }
    }


    public void applyState(Element element) throws Exception {
        super.applyState(element);
        element.setAttribute(ATTR_FTP_URL, ftpUrl);
        element.setAttribute(ATTR_FTP_USER, ftpUser);
    }


    public void applyEditForm(Request request) throws Exception {
        super.applyEditForm(request);
        ftpUrl = request.getString(ATTR_FTP_URL, ftpUrl);
        ftpUser = request.getString(ATTR_FTP_USER, ftpUser);
    }


    public void createEditForm(Request request, StringBuffer sb)
        throws Exception {

        super.createEditForm(request, sb);
        sb.append(HtmlUtil.formEntry(msg("ECHO FTP URL"), HtmlUtil.input(ATTR_FTP_URL, ftpUrl, 60)));
        sb.append(HtmlUtil.formEntry(msg("ECHO User"), HtmlUtil.input(ATTR_FTP_USER, ftpUser, 60)));
    }

    protected void runInner(int timestamp) throws Exception {
        if ( !canContinueRunning(timestamp)) {
            return;
        }

        status = new StringBuffer("");
        int cnt =0 ;
        logHarvesterInfo("EchoPublisher: starting");
        while (canContinueRunning(timestamp)) {
            doPublish();
            cnt++;
            if ( !getMonitor()) {
                status.append("Done<br>");
                logHarvesterInfo("Ran one time only. Exiting loop");
                break;
            }

            status.append("Done... sleeping for " + getSleepMinutes()
                          + " minutes<br>");
            logHarvesterInfo("Sleeping for " + getSleepMinutes()
                             + " minutes");
            doPause();
            status = new StringBuffer();
        }
        logHarvesterInfo("Done running");
    }


    protected void doPublish() throws Exception {
        //Find all entries that have attached echo collection data
        Statement statement = getDatabaseManager().select(
                                                          SqlUtil.comma(Tables.METADATA.COL_ID,
                                                                        Tables.METADATA.COL_ENTRY_ID),
                                                          Tables.METADATA.NAME,
                                                          Clause.eq(Tables.METADATA.COL_TYPE,
                                                                    "echo.collection"));
        SqlUtil.Iterator iterator = getDatabaseManager().getIterator(statement);
     
        ResultSet        results;
        List<Entry> collections = new ArrayList<Entry>();
        while ((results = iterator.getNext()) != null) {
            String metadataId = results.getString(1);
            String entryId = results.getString(2);
            Entry entry = getEntryManager().getEntry(getRequest(), entryId);
            if(entry!=null) collections.add(entry);
        }

       if(collections.size()==0) return;



        File zipFile  = getRepository().getStorageManager().getTmpFile(getRequest(), ".zip");
        OutputStream os = getStorageManager().getFileOutputStream(zipFile);
        writeCollections(collections, os, true);
        IOUtil.close(os);
    }


    public void writeCollections(List<Entry> collections, OutputStream os, boolean includeGranules) throws Exception {
        ZipOutputStream  zos = new ZipOutputStream(os);
        Document doc = XmlUtil.makeDocument();
        Element root = XmlUtil.create(doc, EchoUtil.TAG_COLLECTIONMETADATAFILE, null,
                                      new String[] {
                                          "xmlns:xsi", 
                                          "http://www.w3.org/2001/XMLSchema-instance",
                                          "xsi:noNamespaceSchemaLocation",
                                          "http://www.echo.nasa.gov/ingest/schemas/operations/Collection.xsd"
                                      });
        Element collectionsNode = XmlUtil.create(root.getOwnerDocument(), EchoUtil.TAG_COLLECTIONS, root);
        for(Entry entry: collections) {
            makeCollectionNode(entry, collectionsNode);
        }

        String xml = XmlUtil.toString(root);
        System.err.println(xml);
        zos.putNextEntry(new ZipEntry("collections.xml"));
        byte[] bytes = xml.getBytes();
        zos.write(bytes, 0, bytes.length);
        zos.closeEntry();
        IOUtil.close(zos);
    }



    private void makeCollectionNode(Entry entry, Element collectionsNode) throws Exception {
        Document doc = collectionsNode.getOwnerDocument();
        Element collectionNode = XmlUtil.create(doc, EchoUtil.TAG_COLLECTION, collectionsNode);

        /*
      <ShortName>${entry.name}</ShortName>
      <InsertTime>${entry.publishdate}</InsertTime>
      <LastUpdate>${entry.changedate}</LastUpdate>
      <DataSetId>${entry.id}</DataSetId>
      <Description>${entry.description.cdata}</Description>
        */

        XmlUtil.create(doc, EchoUtil.TAG_SHORTNAME, collectionNode).appendChild(XmlUtil.makeCDataNode(doc, entry.getName(), false));
        XmlUtil.create(doc, EchoUtil.TAG_DESCRIPTION, collectionNode).appendChild(XmlUtil.makeCDataNode(doc, entry.getDescription(), false));


        XmlUtil.create(doc, EchoUtil.TAG_DATASETID, collectionNode, entry.getId(),null);
        XmlUtil.create(doc, EchoUtil.TAG_INSERTTIME, collectionNode, formatDate(entry.getCreateDate()),null);
        XmlUtil.create(doc, EchoUtil.TAG_LASTUPDATE, collectionNode, formatDate(entry.getChangeDate()),null);

        /*
      <Temporal>
        <RangeDateTime>
          <BeginningDateTime>2007-04-01T01:00:00Z</BeginningDateTime>
            <EndingDateTime>2007-05-01T01:00:00Z</EndingDateTime>
        </RangeDateTime>
      </Temporal>
        */

        Element temporalNode = XmlUtil.create(doc, EchoUtil.TAG_TEMPORAL, collectionNode);
        Element rangeNode = XmlUtil.create(doc, EchoUtil.TAG_RANGEDATETIME, temporalNode);
        XmlUtil.create(doc, EchoUtil.TAG_BEGINNINGDATETIME, rangeNode, formatDate(entry.getStartDate()), null);
        XmlUtil.create(doc, EchoUtil.TAG_ENDINGDATETIME, rangeNode, formatDate(entry.getEndDate()), null);



        List<Metadata> metadataList = getMetadataManager().getMetadata(entry);
        List<MetadataHandler> metadataHandlers =
            repository.getMetadataManager().getMetadataHandlers();
        for (Metadata metadata : metadataList) {
            for (MetadataHandler metadataHandler : metadataHandlers) {
                if (metadataHandler.canHandle(metadata)) {
                    metadataHandler.addMetadataToXml(getRequest(),
                                                     "echo", entry,
                                                     metadata, doc, collectionNode);
                    break;
                }
            }
        }
    }
}
