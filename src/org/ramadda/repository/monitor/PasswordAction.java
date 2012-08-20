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

package org.ramadda.repository.monitor;


import org.ramadda.repository.*;
import org.ramadda.repository.auth.*;

import org.ramadda.util.HtmlUtils;

import ucar.unidata.xml.XmlUtil;


import java.util.ArrayList;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.List;


/**
 *
 *
 * @author RAMADDA Development Team
 * @version $Revision: 1.30 $
 */
public abstract class PasswordAction extends MonitorAction {

    /** _more_ */
    public static final String ARG_ACTION_ID = "action_id";

    /** _more_ */
    public static final String ARG_ACTION_PASSWORD = "action_password";

    /** _more_ */
    public static final String ARG_ACTION_MESSAGE = "action_message";


    /** _more_ */
    private String remoteUserId = "";

    /** _more_ */
    private String password = "";

    /** _more_ */
    protected String messageTemplate = null;


    /**
     * _more_
     */
    public PasswordAction() {}

    /**
     * _more_
     *
     * @param id _more_
     */
    public PasswordAction(String id) {
        super(id);
    }



    /**
     * _more_
     *
     * @param id _more_
     * @param remoteUserId _more_
     * @param password _more_
     */
    public PasswordAction(String id, String remoteUserId, String password) {
        super(id);
        this.remoteUserId = remoteUserId;
        this.password     = password;
    }


    /**
     * _more_
     *
     * @param monitor _more_
     * @param entry _more_
     *
     * @return _more_
     */
    public String getMessage(EntryMonitor monitor, Entry entry) {
        String message = getMessageTemplate().replace(
                             "${server}",
                             monitor.getRepository().absoluteUrl(
                                 monitor.getRepository().getUrlBase()));

        return monitor.getRepository().getEntryManager().replaceMacros(entry,
                message);
    }

    /**
     * _more_
     *
     * @param request _more_
     * @param monitor _more_
     */
    public void applyEditForm(Request request, EntryMonitor monitor) {
        super.applyEditForm(request, monitor);

        if (request.exists(getArgId(ARG_ACTION_ID))) {
            this.remoteUserId = request.getString(getArgId(ARG_ACTION_ID),
                    remoteUserId);
        }
        if (request.exists(getArgId(ARG_ACTION_PASSWORD))) {
            this.password = request.getString(getArgId(ARG_ACTION_PASSWORD),
                    password);
        }
        if (request.exists(getArgId(ARG_ACTION_MESSAGE))) {
            this.messageTemplate =
                request.getString(getArgId(ARG_ACTION_MESSAGE),
                                  getMessageTemplate());
        }
    }


    /**
     *  Method for encoding to xml the password. This simply obfuscates what is saved to disk
     *
     *  @param value The new value
     */
    public void setTmp(byte[] value) {
        if (value == null) {
            password = null;
        } else {
            password =
                new String(RepositoryUtil.decodeBase64(new String(value)));
        }
    }



    /**
     *  Method for encoding to xml the password. This simply obfuscates what is saved to disk
     *
     *  @return The Password
     */
    public byte[] getTmp() {
        if (password == null) {
            return null;
        }

        return RepositoryUtil.encodeBase64(password.getBytes()).getBytes();
    }

    /**
     * _more_
     *
     * @return _more_
     */
    protected String getPassword() {
        return password;
    }


    /**
     * Set the RemoteUserId property.
     *
     * @param value The new value for RemoteUserId
     */
    public void setRemoteUserId(String value) {
        remoteUserId = value;
    }

    /**
     * Get the RemoteUserId property.
     *
     * @return The RemoteUserId
     */
    public String getRemoteUserId() {
        return remoteUserId;
    }

    /**
     * Set the MessageTemplate property.
     *
     * @param value The new value for MessageTemplate
     */
    public void setMessageTemplate(String value) {
        messageTemplate = value;
    }

    /**
     * Get the MessageTemplate property.
     *
     * @return The MessageTemplate
     */
    public String getMessageTemplate() {
        if (messageTemplate == null) {
            messageTemplate = getInitialMessageTemplate();
        }

        return messageTemplate;
    }

    /**
     * _more_
     *
     * @return _more_
     */
    protected String getInitialMessageTemplate() {
        return "A new entry has been created on ${server} by ${user}\n${name} ${url}";
    }

}
