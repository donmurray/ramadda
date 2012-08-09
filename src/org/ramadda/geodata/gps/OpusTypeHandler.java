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

package org.ramadda.geodata.gps;


import org.ramadda.repository.*;
import org.ramadda.repository.type.*;
import org.ramadda.repository.auth.User;

import org.w3c.dom.*;

import ucar.unidata.util.IOUtil;
import ucar.unidata.util.Misc;
import ucar.unidata.util.StringUtil;

import java.io.*;
import java.io.File;

import java.util.List;
import java.util.Properties;
import javax.mail.*;
import javax.mail.internet.*;


/**
 *
 *
 * @author Jeff McWhirter
 */
public class OpusTypeHandler extends SolutionTypeHandler {

    /** _more_ */
    public static final String TYPE_OPUS = "gps_solution_opus";

    public static final String PROP_OPUS_MAIL_URL = "gps.opus.mail.url";
    public static final String PROP_OPUS_MAIL_USER = "gps.opus.mail.user";


    private boolean monitoringOpus = false;

    private String opusUser;

    /**
     * _more_
     *
     * @param repository _more_
     * @param node _more_
     * @throws Exception On badness
     */
    public OpusTypeHandler(Repository repository, Element node)
            throws Exception {
        super(repository, node);
        final String opusMailUrl = getRepository().getProperty(PROP_OPUS_MAIL_URL,(String)null);
        opusUser = getRepository().getProperty(PROP_OPUS_MAIL_USER,(String)null);
        if(opusMailUrl!=null) {
            if(opusUser == null) {
                getLogManager().logInfoAndPrint ("OPUS:  error: No user id defined by property: "+PROP_OPUS_MAIL_USER);
                return;
            }
            //Start up in 10 seconds
            Misc.runInABit(10000, new Runnable() {
                    public void run() {
                        //Make sure we have a user
                        try {
                            User user = getUserManager().findUser(opusUser);
                            if(user == null) {
                                getLogManager().logInfoAndPrint ("OPUS: could not find user:" + opusUser);
                                return;
                            }
                        } catch(Exception exc) {
                            getLogManager().logError ("OPUS: could not find user:" + opusUser,exc);
                            return;
                        }
                        //                        getLogManager().logInfoAndPrint ("OPUS:  monitoring email "+ opusMailUrl);
                        getLogManager().logInfoAndPrint ("OPUS: monitoring OPUS email");
                        monitorOpusEmail(opusMailUrl);
                    }});
        }
    }

    @Override
    public void shutdown() throws Exception {
        super.shutdown();
        monitoringOpus = false;
    }


    private void monitorOpusEmail(String opusMailUrl) {
        monitoringOpus = true;
        int errorCnt = 0;
        long time = System.currentTimeMillis();
        while(monitoringOpus) {
            if(errorCnt>5) {
                getLogManager().logErrorAndPrint("OPUS: Opus email monitoring failed",null);
                return;
            }
            try {
                //                System.err.println ("calling checkfor opusmail");
                //                System.err.println ("OPUS: checking mbox");
                if(!checkForOpusEmail(opusMailUrl)) {
                    errorCnt++;
                } else {
                    errorCnt = 0;
                }
                //                System.err.println ("OPUS: done checking mbox");
                //Sleep for  5 minutes
                Misc.sleepSeconds(60*5);
            } catch(Exception exc) {
                errorCnt++;
                //                if(errorCnt>5) {
                exc.printStackTrace();
                getLogManager().logError("OPUS: Opus email monitoring failed", exc);
                return;
                //                }
            }
        }
    }


    /**
     * _more_
     *
     * @param request _more_
     * @param entry _more_
     * @param parent _more_
     * @param newEntry _more_
     *
     * @throws Exception On badness
     */
    @Override
    public void initializeEntryFromForm(Request request, Entry entry,
                                        Entry parent, boolean newEntry)
            throws Exception {
        super.initializeEntryFromForm(request, entry, parent, newEntry);
        initializeOpusEntry(entry);
    }


    /**
     * _more_
     *
     * @param entry _more_
     *
     * @throws Exception On badness
     */
    @Override
    public void initializeNewEntry(Entry entry) throws Exception {
        super.initializeNewEntry(entry);
        initializeOpusEntry(entry);
    }



    /**
     * _more_
     *
     * @param entry _more_
     *
     * @throws Exception _more_
     */
    private void initializeOpusEntry(Entry entry) throws Exception {
        String opus = new String(
                          IOUtil.readBytes(
                              getStorageManager().getFileInputStream(
                                  entry.getFile())));
        /*
      LAT:   40 6 46.56819      0.003(m)        40 6 46.58791      0.003(m)
    E LON:  253 35  8.56821      0.010(m)       253 35  8.52089      0.010(m)
    W LON:  106 24 51.43179      0.010(m)       106 24 51.47911      0.010(m)
   EL HGT:         2275.608(m)   0.009(m)              2274.768(m)   0.009(m)
Northing (Y) [meters]     4441227.340           391737.791
Easting (X)  [meters]      379359.228           836346.070

  X:     -1380903.608(m)   0.015(m)          -1380904.391(m)   0.015(m)
        Y:     -4687187.453(m)   0.012(m)          -4687186.144(m)   0.012(m)
        Z:      4089011.143(m)   0.010(m)           4089011.067(m)   0.010(m)
         */
        //        List<String> 
        Object[] values = entry.getTypeHandler().getValues(entry);
        String[] patterns = { "Northing\\s*\\(Y\\)\\s*\\[meters\\]\\s*([-\\.\\d]+)\\s+",
                              "Easting\\s*\\(X\\)\\s*\\[meters\\]\\s*([-\\.\\d]+)\\s+",
                              "X:\\s*([-\\.\\d]+)\\(",
                              "Y:\\s*([-\\.\\d]+)\\(",
                              "Z:\\s*([-\\.\\d]+)\\(", };
        for (int i = 0; i < patterns.length; i++) {
            String value = StringUtil.findPattern(opus, patterns[i]);
            if (value != null) {
                values[i + IDX_UTM_X] = new Double(value);
            }
        }
        String latLine    = StringUtil.findPattern(opus, "LAT: *([^\n]+)\n");
        String lonLine    = StringUtil.findPattern(opus, "LON: *([^\n]+)\n");
        String heightLine = StringUtil.findPattern(opus,
                                "HGT: *([^\\(]+)\\(");
        double altitude = 0.0;
        if (heightLine != null) {
            //            System.err.println ("hgt: " + heightLine);
            altitude = Double.parseDouble(heightLine.trim());
        }
        if ((latLine != null) && (lonLine != null)) {
            List<String> latToks = StringUtil.split(latLine.trim(), " ",
                                       true, true);
            List<String> lonToks = StringUtil.split(lonLine.trim(), " ",
                                       true, true);
            double lat = Misc.decodeLatLon(latToks.get(0) + ":"
                                           + latToks.get(1) + ":"
                                           + latToks.get(2));
            double lon =
                Misc.normalizeLongitude(Misc.decodeLatLon(lonToks.get(0)
                    + ":" + lonToks.get(1) + ":" + lonToks.get(2)));
            //            System.err.println ("lat: " + lat + " " + lon +" alt:" + altitude);
            entry.setLocation(lat, lon, altitude);
        }
    }


    private boolean checkForOpusEmail(String opusMailUrl) throws Exception  {
        Properties props = System.getProperties();
        try {
            Session session = Session.getDefaultInstance(props);
            URLName urlName = new URLName(opusMailUrl);
            Store store = session.getStore(urlName);
            if (!store.isConnected()) {
                store.connect();
            }
            Folder folder = store.getFolder("Inbox");
            if (folder == null || !folder.exists()) {
                getLogManager().logError("OPUS: Invalid folder");
                return false;
            }

            folder.open(Folder.READ_WRITE);
            Message[] messages = folder.getMessages();
            for(int i=0;i<messages.length;i++) {
                String subject = messages[i].getSubject();
                if(subject.indexOf("OPUS solution") <0) {
                    System.err.println("OPUS: skipping: " + subject);
                    //                    messages[i].setFlag(Flags.Flag.DELETED, true);
                    continue;
                }
                System.err.println("OPUS: subject:" +  subject);
                Object content = messages[i].getContent();
                StringBuffer sb = new StringBuffer();
                processContent(content, sb);
                GpsOutputHandler gpsOutputHandler =
                    (GpsOutputHandler) getRepository().getOutputHandler(
                                                                        GpsOutputHandler.OUTPUT_GPS_TORINEX);
                //                System.err.println("deleting:" +  subject);
                //                messages[i].setFlag(Flags.Flag.DELETED, true);
                //                if(true) continue;

                StringBuffer msgBuff = new StringBuffer();
                Request tmpRequest = getRepository().getTmpRequest(opusUser);
                Entry newEntry = gpsOutputHandler.processAddOpus(tmpRequest, sb.toString(), msgBuff);
                if(newEntry==null) {
                    System.err.println("OPUS: Unable to process OPUS message:" + msgBuff);
                    getLogManager().logError("OPUS: Unable to process OPUS message:" + msgBuff);
                } else {
                    //                    monitoringOpus = false;
                    System.err.println("OPUS: added opus. deleting email: " + newEntry.getId());
                    messages[i].setFlag(Flags.Flag.DELETED, true);
                }
            }
        } catch (NoSuchProviderException e) {
            e.printStackTrace();
        } catch (MessagingException e) {
            e.printStackTrace();
        }
        return true;
    }

    private static void processContent(Object content, StringBuffer desc)  throws Exception {
        if(content instanceof MimeMultipart){
            MimeMultipart multipart= (MimeMultipart) content;
            for(int i=0;i<multipart.getCount();i++) {
                MimeBodyPart part = (MimeBodyPart)multipart.getBodyPart(i);
                String disposition = part.getDisposition();
                String contentType = part.getContentType();
                Object partContent = part.getContent();
                if (disposition == null) {
                    if(partContent instanceof MimeMultipart){
                        processContent(partContent, desc);
                    } else {
                        //Only ingest the text
                        if(contentType.indexOf("text/plain")>=0) {
                            desc.append(partContent);
                            desc.append("\n");
                        }
                    }
                    continue;
                }
                if (disposition.equals(Part.INLINE) && contentType.indexOf("text/plain")>=0) {
                    desc.append(partContent);
                    return;
                }

                //                System.err.println("disposition:" + disposition + " Type:" + contentType +" part:" + partContent.getClass().getName());
                if (disposition.equals(Part.ATTACHMENT) || 
                        disposition.equals(Part.INLINE)) {
                    if(part.getFileName()!=null) {
                        InputStream inputStream = part.getInputStream();
                    }
                }
            }
        } else if(content instanceof Part) {
            //            System.err.println("Part");
            //TODO
            Part part= (Part) content;
        } else {
            //            System.err.println ("xxx content:" + content.getClass().getName());
            //            System.err.println("Content");
            String contents = content.toString();
            desc.append(contents);
            desc.append("\n");
        }
    }



}
