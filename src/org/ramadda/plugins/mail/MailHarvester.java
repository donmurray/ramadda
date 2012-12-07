/*
* Copyright 2008-2012 Jeff McWhirter/ramadda.org
*                     Don Murray/CU-CIRES
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

package org.ramadda.plugins.mail;


import org.ramadda.repository.*;
import org.ramadda.repository.auth.*;
import org.ramadda.repository.harvester.*;
import org.ramadda.repository.metadata.*;
import org.ramadda.repository.type.*;

import org.ramadda.util.MailUtil;
import org.ramadda.util.HtmlUtils;


import org.w3c.dom.*;

import ucar.unidata.util.Misc;
import ucar.unidata.util.IOUtil;
import ucar.unidata.util.StringUtil;

import ucar.unidata.xml.XmlUtil;
import ucar.unidata.xml.XmlUtil;

import java.io.*;

import javax.mail.*;
import javax.mail.internet.*;

import java.net.*;



import java.util.ArrayList;
import java.util.Date;
import java.util.Enumeration;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.List;
import java.util.Properties;



/**
 */
public class MailHarvester extends Harvester {

    /** _more_          */

    public static final String ATTR_ACTION = "action";
    public static final String ATTR_IMAP_URL = "imapurl";
    public static final String ATTR_FOLDER = "folder";
    public static final String ATTR_FROM = "from";
    public static final String ATTR_SUBJECT = "subject";
    public static final String ATTR_BODY = "body";

    public static final String ATTR_RESPONSE = "response";


    private String imapUrl;
    private String folder = "Inbox";

    /** _more_          */
    private String from;

    /** _more_          */
    private String subject;

    /** _more_          */
    private String body;

    /** _more_          */
    private String response;




    /**
     * _more_
     *
     * @param repository _more_
     * @param id _more_
     *
     * @throws Exception _more_
     */
    public MailHarvester(Repository repository, String id) throws Exception {
        super(repository, id);
    }


    /**
     * _more_
     *
     * @param repository _more_
     * @param node _more_
     *
     * @throws Exception _more_
     */
    public MailHarvester(Repository repository, Element node)
            throws Exception {
        super(repository, node);
    }



    /**
     * _more_
     *
     * @param element _more_
     *
     * @throws Exception _more_
     */
    protected void init(Element element) throws Exception {
        super.init(element);
        imapUrl = XmlUtil.getAttribute(element, ATTR_IMAP_URL, imapUrl);
        folder = XmlUtil.getAttribute(element, ATTR_FOLDER, folder);
        from = XmlUtil.getAttribute(element, ATTR_FROM, from);
        subject = XmlUtil.getAttribute(element, ATTR_SUBJECT, subject);
        body= XmlUtil.getAttribute(element, ATTR_BODY, body);
        response  = XmlUtil.getAttribute(element, ATTR_RESPONSE, response);
    }



    /**
     * _more_
     *
     * @return _more_
     */
    public String getDescription() {
        return "Email Harvester";
    }



    /**
     * _more_
     *
     * @param element _more_
     *
     * @throws Exception _more_
     */
    public void applyState(Element element) throws Exception {
        super.applyState(element);
        element.setAttribute(ATTR_IMAP_URL, imapUrl);
        element.setAttribute(ATTR_FOLDER, folder);
        element.setAttribute(ATTR_FROM, from);
        element.setAttribute(ATTR_SUBJECT, subject);
        element.setAttribute(ATTR_BODY, body);
        element.setAttribute(ATTR_RESPONSE, response);
    }


    /**
     * _more_
     *
     * @param request _more_
     *
     * @throws Exception _more_
     */
    public void applyEditForm(Request request) throws Exception {
        super.applyEditForm(request);
        imapUrl = request.getString(ATTR_IMAP_URL, imapUrl);
        folder = request.getString(ATTR_FOLDER, folder);
        from = request.getString(ATTR_FROM, from);
        subject   = request.getString(ATTR_SUBJECT, subject);
        body   = request.getString(ATTR_BODY, body);
        response  = request.getString(ATTR_RESPONSE, response);
    }


    /**
     * _more_
     *
     * @param request _more_
     * @param sb _more_
     *
     * @throws Exception _more_
     */
    public void createEditForm(Request request, StringBuffer sb)
            throws Exception {
        super.createEditForm(request, sb);
        addBaseGroupSelect(ATTR_BASEGROUP, sb);
        


        sb.append(HtmlUtils.formEntry(msgLabel("Email URL"),
                                      HtmlUtils.input(ATTR_IMAP_URL,
                                                      imapUrl, HtmlUtils.SIZE_60)
                                      + " " + "e.g. <i>imaps://&lt;email address&gt;:&lt;email password&gt;@&lt;email server&gt;</i>"));


        sb.append(HtmlUtils.formEntry(msgLabel("Folder"),
                                      HtmlUtils.input(ATTR_FOLDER,
                                                      folder, HtmlUtils.SIZE_60)));

        sb.append(HtmlUtils.formEntry(msgLabel("Sender Contains"),
                                      HtmlUtils.input(ATTR_FROM,
                                                      from, HtmlUtils.SIZE_60)));
        sb.append(HtmlUtils.formEntry(msgLabel("Subject Contains"),
                                      HtmlUtils.input(ATTR_SUBJECT, subject,
                                          HtmlUtils.SIZE_60)));
        sb.append(HtmlUtils.formEntry(msgLabel("Body Contains"),
                                      HtmlUtils.input(ATTR_BODY, body,
                                          HtmlUtils.SIZE_60)));
        sb.append(HtmlUtils.formEntryTop(msgLabel("Email Response"),
                                      HtmlUtils.textArea(ATTR_RESPONSE,
                                                         response==null?"":response,5,60) +"<br>" + "Use ${url} for the URL to the created entry"));

    }



    /**
     * _more_
     *
     *
     * @param timestamp _more_
     * @throws Exception _more_
     */
    protected void runInner(int timestamp) throws Exception {
        while (canContinueRunning(timestamp)) {
            long t1 = System.currentTimeMillis();
            logHarvesterInfo("Checking email");
            try {
                status = new StringBuffer();
                checkEmail();
            } catch(Exception exc) {
                logHarvesterError("Error in checkEmail", exc);
                return;
            }
            currentStatus =  "Done";
            if ( !getMonitor()) {
                logHarvesterInfo("Ran one time only. Exiting loop");
                break;
            }

            logHarvesterInfo("Sleeping for " + getSleepMinutes()
                             + " minutes");
            doPause();
        }
    }



    /**
     * _more_
     *
     * @param request _more_
     * @param info _more_
     *
     * @return _more_
     *
     * @throws Exception _more_
     */
    public void checkEmail() throws Exception {
        currentStatus = "Checking mail";
        Properties props = System.getProperties();
        Session session = Session.getDefaultInstance(props);
        URLName urlName = new URLName(imapUrl);
        Store   store   = session.getStore(urlName);
        if ( !store.isConnected()) {
            store.connect();
        }

        if ( !store.isConnected()) {
            logHarvesterError("Could not connect to email server",null);
            return;
        }

        Folder emailFolder = store.getFolder(folder);
        if ((emailFolder == null) || !emailFolder.exists()) {
            status.append("Invalid folder:" + folder);
            logHarvesterError("Invalid folder:" + folder, null);
            return;
        }
        emailFolder.open(Folder.READ_WRITE);
        int numMessages = emailFolder.getMessageCount();
        int cnt = 0;
        //Go backwards to get the newest first
        for (int i = numMessages; i >0;i--) {
            //Only read 100 messages
            if(cnt++>100) break;
            Message message = emailFolder.getMessage(i);
            String messageSubject = message.getSubject();
            String messageFrom     = InternetAddress.toString(message.getFrom());

            if(!matches(subject, messageSubject)) {
                continue;
            }
            if(!matches(from, messageFrom)) {
                    continue;
            }
            if(message.isSet(Flags.Flag.SEEN)) {
                logHarvesterInfo ("message has been read:"  + message.getSubject());
                continue;
            }
            Object       content = message.getContent();
            StringBuffer sb      = new StringBuffer();
            MailUtil.extractText(content, sb);
            String messageBody = sb.toString();
            if(!matches(body, messageBody)) {
                continue;
            }
            System.err.println("message: " + messageSubject);
            List<Entry> newEntries = new ArrayList<Entry>();
            processMessage(getBaseGroup(), content, messageBody, newEntries);

            if(newEntries.size()>0) {
                StringBuffer result = new StringBuffer();
                if(defined(response)) {
                    result.append(response);
                    result.append("\n");
                }
                if(newEntries.size()>1)
                    status.append("Harvested " + newEntries.size() +" entries from email<br>"); 
                else
                    status.append("Harvested 1 entry from email<br>"); 

                for(Entry newEntry: newEntries) {
                    String fullEntryUrl = 
                        HtmlUtils.url(getRepository().URL_ENTRY_SHOW.getFullUrl(),
                                      ARG_ENTRYID, newEntry.getId());

                    String entryUrl = 
                        HtmlUtils.url(getRepository().URL_ENTRY_SHOW.toString(),
                                      ARG_ENTRYID, newEntry.getId());
                    
                    status.append(HtmlUtils.href(entryUrl, newEntry.getName()));
                    status.append("<br>");
                    result.append(fullEntryUrl);
                    result.append("\n");
                }
                if(defined(response) && getAdmin().isEmailCapable()) {
                    String     to     = InternetAddress.toString(message.getFrom());
                    getRepository().getAdmin().sendEmail(to, "harvested emails", result.toString(), false);
                }
            }
            
        }
    }


    private void processMessage(Entry parentEntry, Object content,
                                String desc, List<Entry> newEntries)
        throws Exception {

        if (content instanceof MimeMultipart) {
            MimeMultipart multipart = (MimeMultipart) content;
            for (int i = 0; i < multipart.getCount(); i++) {
                MimeBodyPart part = (MimeBodyPart) multipart.getBodyPart(i);
                String       disposition = part.getDisposition();
                if (disposition == null) {
                    Object partContent = part.getContent();
                    if (partContent instanceof MimeMultipart) {
                        processMessage(parentEntry, partContent, desc, newEntries);
                    } else {
                    }
                    continue;

                }
                if (disposition.equalsIgnoreCase(Part.ATTACHMENT)
                    || disposition.equalsIgnoreCase(Part.INLINE)) {
                    if (part.getFileName() != null) {
                        InputStream inputStream = part.getInputStream();
                        File        f = getStorageManager().getTmpFile(getRequest(),
                                                                       part.getFileName());
                        OutputStream outputStream =
                            getStorageManager().getFileOutputStream(f);
                        IOUtil.writeTo(inputStream, outputStream);
                        IOUtil.close(inputStream);
                        IOUtil.close(outputStream);
                        f =     getStorageManager().moveToStorage(getRequest(), f);
                        TypeHandler typeHandler  = getEntryManager().findDefaultTypeHandler(f.toString());
                        if(typeHandler == null) {
                            typeHandler = getRepository().getTypeHandler(TypeHandler.TYPE_FILE);
                        }
                        Resource resource = new Resource(f.toString(), Resource.TYPE_STOREDFILE);
                        Date        date        = new Date();
                        Object[]    values      = typeHandler.makeValues(new Hashtable());
                        Entry       entry = typeHandler.createEntry(getRepository().getGUID());
                        entry.initEntry(part.getFileName(), desc.toString(), parentEntry, getUser(), resource, "",
                                        date.getTime(), date.getTime(), date.getTime(),
                                        date.getTime(), values);

                        List<Entry> entries = (List<Entry>) Misc.newList(entry);
                        getEntryManager().addInitialMetadata(getRequest(),
                                                             entries,
                                                             true, false);
                        getEntryManager().insertEntries(entries, true, true);
                        newEntries.add(entry);
                    }
                }
            }
        } 
    }





    public static void main(String[]args) throws Exception {
        //        imap://MYUSERNAME@gmail.com:MYPASSWORD@imap.gmail.com

        if(args.length != 1) {
            System.err.println("usage: username:password");
            return;
        }

        String url = "imaps://" + args[0] +"@imap.gmail.com:993";
        System.err.println ("url:" + url);

        Properties props = System.getProperties();
        Session session = Session.getDefaultInstance(props);
        URLName urlName = new URLName(url);
        System.err.println ("protocol:" + urlName.getProtocol() +" port:" + urlName.getPort() +" password:" + urlName.getPassword());

       Store   store   = session.getStore(urlName);

        if ( !store.isConnected()) {
            store.connect();
        }
        Folder emailFolder = store.getFolder("Inbox");
        emailFolder.open(Folder.READ_WRITE);
        int numMessages = emailFolder.getMessageCount();
        int cnt = 0;
        for (int i = numMessages; i >0;i--) {
            Message message =  emailFolder.getMessage(i);
            if(message==null) continue;
            String subject  =message.getSubject();
            if(!subject.equals("for ramadda")) {
                continue;
            }
            if(message.isSet(Flags.Flag.SEEN)) {
                System.err.println ("Have seen:"  + message.getSubject());
                continue;
            }


            System.err.println ("subject:" + message.getSubject());
            Object       content = message.getContent();
            StringBuffer sb      = new StringBuffer();
            MailUtil.extractText(content, sb);
            System.err.println("text:" + sb);

            if(true) return;
            /*
            FileOutputStream fos = new FileOutputStream("test.eml");
            message.writeTo(fos);
            fos.close();
            if(true) break;
            */
            if(cnt++>100) break;
        }
    } 

    private boolean matches(String pattern, String text) {
        if(defined(pattern) && text.toLowerCase().indexOf(pattern.toLowerCase())<0) {
            return false;
        }
        return true;
    }
}
