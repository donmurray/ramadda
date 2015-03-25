/*
* Copyright 2008-2015 Geode Systems LLC
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


import java.util.ArrayList;
import java.util.List;


/**
 *
 *
 * @author RAMADDA Development Team
 * @version $Revision: 1.30 $
 */
public class TwitterAction extends PasswordAction {


    /**
     * _more_
     */
    public TwitterAction() {}

    /**
     * _more_
     *
     * @param id _more_
     */
    public TwitterAction(String id) {
        super(id);
    }



    /**
     * _more_
     *
     *
     * @param id _more_
     * @param remoteUserId _more_
     * @param password _more_
     */
    public TwitterAction(String id, String remoteUserId, String password) {
        super(id, remoteUserId, password);
    }


    /**
     * _more_
     *
     * @return _more_
     */
    public String getActionName() {
        return "twitter";
    }

    /**
     * _more_
     *
     * @return _more_
     */
    public String getActionLabel() {
        return "Twitter Action";
    }

    /**
     * _more_
     *
     *
     * @param entryMonitor _more_
     * @return _more_
     */
    public String getSummary(EntryMonitor entryMonitor) {
        return "Twitter to:" + getRemoteUserId();
    }

    /**
     * _more_
     *
     * @return _more_
     */
    protected String getInitialMessageTemplate() {
        return "New RAMADDA entry: ${name} ${url}";
    }

    /**
     * _more_
     *
     * @param monitor _more_
     * @param sb _more_
     */
    @Override
        public void addToEditForm(EntryMonitor monitor, Appendable sb) throws Exception  {
        sb.append(HtmlUtils.formTable());
        sb.append(HtmlUtils.colspan("Twitter Action", 2));
        sb.append(
            HtmlUtils.formEntry(
                "Twitter ID:",
                HtmlUtils.input(
                    getArgId(ARG_ACTION_ID), getRemoteUserId(),
                    HtmlUtils.SIZE_60)));
        sb.append(
            HtmlUtils.formEntry(
                "Twitter Password:",
                HtmlUtils.input(
                    getArgId(ARG_ACTION_PASSWORD), getPassword(),
                    HtmlUtils.SIZE_60)));
        sb.append(
            HtmlUtils.formEntryTop(
                "Message:",
                HtmlUtils.textArea(
                    getArgId(ARG_ACTION_MESSAGE), getMessageTemplate(), 5,
                    60)));
        sb.append(HtmlUtils.formTableClose());
    }



    /**
     * _more_
     *
     *
     * @param monitor _more_
     * @param entry _more_
     */
    protected void entryMatched(EntryMonitor monitor, Entry entry) {
        try {
            super.entryMatched(monitor, entry);

            twitter4j.Twitter twitter =
                new twitter4j.Twitter(getRemoteUserId(), getPassword());
            twitter4j.Status status = twitter.update(getMessage(monitor,
                                          entry));
            System.out.println("Successfully sent a twitter message: ["
                               + status.getText() + "]");
        } catch (Exception exc) {
            monitor.handleError("Error posting to Twitter", exc);
        }
    }



}
