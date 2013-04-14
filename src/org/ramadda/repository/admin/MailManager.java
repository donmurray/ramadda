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

package org.ramadda.repository.admin;


import org.ramadda.repository.*;

import org.ramadda.repository.auth.*;
import org.ramadda.repository.database.*;
import org.ramadda.repository.ftp.FtpManager;

import org.ramadda.repository.harvester.*;

import org.ramadda.repository.output.*;
import org.ramadda.util.HtmlUtils;
import org.ramadda.util.Utils;

import org.w3c.dom.*;



import ucar.unidata.util.IOUtil;
import ucar.unidata.util.LogUtil;
import ucar.unidata.util.Misc;

import ucar.unidata.util.StringUtil;

import java.io.*;


import java.util.ArrayList;
import java.util.Date;
import java.util.Enumeration;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.List;
import java.util.Properties;

import javax.mail.Address;

import javax.mail.Message;
import javax.mail.Transport;
import javax.mail.internet.InternetAddress;


import javax.mail.internet.MimeMessage;

import java.util.Properties;
import javax.mail.*;
import javax.mail.internet.*;


/**
 * Class Admin
 *
 *
 * @author RAMADDA Development Team
 * @version $Revision: 1.3 $
 */
public class MailManager extends RepositoryManager {


    /**
     * _more_
     *
     * @param repository _more_
     */
    public MailManager(Repository repository) {
        super(repository);

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
    public void addAdminSettings(Request request, StringBuffer sb) throws Exception {
        sb.append(
                  HtmlUtils.formEntry(
                                      msgLabel("Mail Server"), HtmlUtils.input(
                                                                               PROP_ADMIN_SMTP, getProperty(
                                                                                                            PROP_ADMIN_SMTP, ""), HtmlUtils.SIZE_40) + " "
                                      + msg("For sending password reset messages")));
    }

    public void applyAdminConfig(Request request) throws Exception {
        getRepository().writeGlobal(request, PROP_ADMIN_SMTP, true);
    }

    /**
     * _more_
     *
     * @return _more_
     */
    public boolean isEmailCapable() {
        String smtpServer = getRepository().getProperty(PROP_ADMIN_SMTP,
                                "").trim();
        String serverAdmin = getRepository().getProperty(PROP_ADMIN_EMAIL,
                                 "").trim();
        if ((serverAdmin.length() == 0) || (smtpServer.length() == 0)) {
            return false;
        }

        return true;
    }

    /**
     * _more_
     *
     * @param to _more_
     * @param subject _more_
     * @param contents _more_
     * @param asHtml _more_
     *
     * @throws Exception _more_
     */
    public void sendEmail(String to, String subject, String contents,
                          boolean asHtml)
            throws Exception {
        String from = getRepository().getProperty(PROP_ADMIN_EMAIL,
                          "").trim();
        sendEmail(to, from, subject, contents, asHtml);
    }


    /**
     * _more_
     *
     * @param to _more_
     * @param from _more_
     * @param subject _more_
     * @param contents _more_
     * @param asHtml _more_
     *
     * @throws Exception _more_
     */
    public void sendEmail(String to, String from, String subject,
                          String contents, boolean asHtml)
            throws Exception {
        sendEmail((List<Address>) Misc.newList(new InternetAddress(to)),
                  new InternetAddress(from), subject, contents, false,
                  asHtml);
    }


    /**
     * _more_
     *
     * @param to _more_
     * @param from _more_
     * @param subject _more_
     * @param contents _more_
     * @param bcc _more_
     * @param asHtml _more_
     *
     * @throws Exception _more_
     */
    public void sendEmail(List<Address> to, InternetAddress from,
                          String subject, String contents, boolean bcc,
                          boolean asHtml)
            throws Exception {
        if ( !isEmailCapable()) {
            throw new IllegalStateException(
                "This RAMADDA server has not been configured to send email");
        }

        //        System.err.println("subject:" + subject);
        //        System.err.println("contents:" + contents);
        String smtpServer = getRepository().getProperty(PROP_ADMIN_SMTP,
                                "").trim();

        System.err.println("sending mail from:" + from.getAddress());

        Properties props = new Properties();
        props.put("mail.smtp.host", smtpServer);
        props.put("mail.from", from.getAddress());
        javax.mail.Session session = javax.mail.Session.getInstance(props,
                                                                    null);
        String smtpUser = getRepository().getProperty(PROP_SMTP_USER, (String) null);
        String smtpPassword = getRepository().getProperty(PROP_SMTP_PASSWORD, (String) null);
        if(smtpUser!=null) {
            System.err.println("smtp user:" + smtpUser);
            props.put("mail.smtp.user", smtpUser);
        }


        MimeMessage msg = new MimeMessage(session);
        msg.setFrom(from);
        Address[] array = new Address[to.size()];
        for (int i = 0; i < to.size(); i++) {
            array[i] = to.get(i);
        }
        msg.setRecipients((bcc
                           ? Message.RecipientType.BCC
                           : Message.RecipientType.TO), array);
        msg.setSubject(subject);
        msg.setSentDate(new Date());
        msg.setContent(contents, (asHtml
                                  ? "text/html"
                                  : "text/plain"));

        if(smtpPassword!=null) {
            System.err.println("password:" + smtpPassword);
            Transport tr = session.getTransport();
            tr.connect(null, smtpPassword);
            tr.send(msg);
        } else {
            Transport.send(msg);
        }
    }


    static final String HOST = "email-smtp.us-east-1.amazonaws.com";    
    static final String SMTP_USERNAME = "username";  // Replace with your SMTP username credential.
    static final String SMTP_PASSWORD = "password";  // Replace with your SMTP password.

    public void sendMailNew(String to, String subject, String body) throws Exception {


        Properties props = System.getProperties();
        props.put("mail.transport.protocol", "smtp");

        // Port we will connect to on the Amazon SES SMTP endpoint. We are choosing port 25 because we will use
        // STARTTLS to encrypt the connection.
        props.put("mail.smtp.port", 25); 

        
        // Set properties indicating that we want to use STARTTLS to encrypt the connection.
        // The SMTP session will begin on an unencrypted connection, and then the client
        // will issue a STARTTLS command to upgrade to an encrypted connection.
        props.put("mail.smtp.auth", "true");
        props.put("mail.smtp.starttls.enable", "true");
        props.put("mail.smtp.starttls.required", "true");

        // Create a Session object to represent a mail session with the specified properties. 
        Session session = Session.getDefaultInstance(props);

        // Create a message with the specified information. 
        MimeMessage msg = new MimeMessage(session);
        msg.setFrom(new InternetAddress("jeff.mcwhirter@gmail.com"));
        msg.setRecipient(Message.RecipientType.TO, new InternetAddress("jeff.mcwhirter@gmail.com"));
        msg.setSubject(subject);
        msg.setContent(body,"text/plain");
            
        // Create a transport.        
        Transport transport = session.getTransport();
                    
        // Send the message.
        try    {
            System.out.println("Attempting to send an email");
            
            String smtpServer = getRepository().getProperty(PROP_ADMIN_SMTP,
                                                            "").trim();
            String smtpUser = getRepository().getProperty(PROP_SMTP_USER, (String) null);
            String smtpPassword = getRepository().getProperty(PROP_SMTP_PASSWORD, (String) null);
            if(smtpUser!=null) {
                // Connect to Amazon SES using the SMTP username and password you specified above.
                transport.connect(smtpServer, smtpUser, smtpPassword);
            } else  {
                //TODO.....                transport.connect(smtpServer);
            }
            // Send the email.
            transport.sendMessage(msg, msg.getAllRecipients());
        }  catch (Exception ex) {
            System.out.println("The email was not sent.");
            System.out.println("Error message: " + ex.getMessage());
        } finally {
            // Close and terminate the connection.
            transport.close();         
        }
    }




}
