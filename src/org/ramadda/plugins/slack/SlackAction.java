/**
 * Copyright (c) 2008-2015 Geode Systems LLC
 * This Software is licensed under the Geode Systems RAMADDA License available in the source distribution in the file
 * ramadda_license.txt. The above copyright notice shall be included in all copies or substantial portions of the Software.
 */

package org.ramadda.plugins.slack;


import org.ramadda.repository.*;
import org.ramadda.repository.auth.*;
import org.ramadda.repository.monitor.*;

import org.ramadda.util.HtmlUtils;


import java.util.ArrayList;
import java.util.List;


/**
 *
 *
 * @author RAMADDA Development Team
 * @version $Revision: 1.30 $
 */
public class SlackAction extends MonitorAction {

    /** _more_          */
    public static final String ARG_WEBHOOK = "webhook";


    /** _more_          */
    private String webhook;


    /**
     * _more_
     */
    public SlackAction() {}

    /**
     * _more_
     *
     * @param id _more_
     */
    public SlackAction(String id) {
        super(id);
    }


    /**
     * _more_
     *
     * @return _more_
     */
    @Override
    public String getActionName() {
        return "Slack Action";
    }

    /**
     * _more_
     *
     * @return _more_
     */
    @Override
    public String getActionLabel() {
        return "Slack Action";
    }

    /**
     * _more_
     *
     *
     * @param entryMonitor _more_
     * @return _more_
     */
    @Override
    public String getSummary(EntryMonitor entryMonitor) {
        return "Post a link to Slack";
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
     *
     * @throws Exception _more_
     */
    @Override
    public void addToEditForm(EntryMonitor monitor, Appendable sb)
            throws Exception {
        sb.append(HtmlUtils.formTable());
        sb.append(HtmlUtils.colspan("Post a link to the entry in Slack", 2));
        sb.append(HtmlUtils.formEntry("Slack Web Hook URL:",
                                      HtmlUtils.input(ARG_WEBHOOK,
                                          getWebhook(), HtmlUtils.SIZE_60)));
        sb.append(HtmlUtils.formTableClose());
    }

    /**
     * _more_
     *
     * @param request _more_
     * @param monitor _more_
     */
    public void applyEditForm(Request request, EntryMonitor monitor) {
        super.applyEditForm(request, monitor);
        this.webhook = request.getString(ARG_WEBHOOK, webhook);
    }

    /**
     * _more_
     *
     *
     * @param monitor _more_
     * @param entry _more_
     */
    @Override
    public void entryMatched(EntryMonitor monitor, Entry entry, boolean isNew) {
        try {
            super.entryMatched(monitor, entry, isNew);
            List<Entry> entries = new ArrayList<Entry>();
            entries.add(entry);
            SlackUtil.makeEntryResult(
                monitor.getRepository(),
                new Request(monitor.getRepository(), null),
                (isNew?"New":"Modified") +" "  + entry.getTypeHandler().getLabel(), entries,
                getWebhook());
        } catch (Exception exc) {
            monitor.handleError("Error posting to Monitor   ", exc);
        }
    }


    /**
     *  Set the Webhook property.
     *
     *  @param value The new value for Webhook
     */
    public void setWebhook(String value) {
        webhook = value;
    }

    /**
     *  Get the Webhook property.
     *
     *  @return The Webhook
     */
    public String getWebhook() {
        return webhook;
    }




}
