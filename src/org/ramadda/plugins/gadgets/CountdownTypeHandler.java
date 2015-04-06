/*
 * Copyright (c) 2008-2015 Geode Systems LLC
 * This Software is licensed under the Geode Systems RAMADDA License available in the source distribution in the file 
 * ramadda_license.txt. The above copyright notice shall be included in all copies or substantial portions of the Software.
 */

package org.ramadda.plugins.gadgets;


import org.ramadda.repository.*;
import org.ramadda.repository.output.*;
import org.ramadda.repository.type.*;

import org.ramadda.util.HtmlUtils;

import org.w3c.dom.*;

import ucar.unidata.util.StringUtil;

import java.util.ArrayList;
import java.util.Date;

import java.util.List;


/**
 *
 *
 */
public class CountdownTypeHandler extends GenericTypeHandler {


    /**
     * _more_
     *
     * @param repository _more_
     * @param entryNode _more_
     *
     * @throws Exception _more_
     */
    public CountdownTypeHandler(Repository repository, Element entryNode)
            throws Exception {
        super(repository, entryNode);
    }

    /** _more_ */
    private int countdownCnt = 0;

    /** _more_ */
    private String countdownHtml;

    /**
     * _more_
     *
     * @param request _more_
     * @param group _more_
     * @param subGroups _more_
     * @param entries _more_
     * @param entry _more_
     *
     * @return _more_
     *
     * @throws Exception _more_
     */
    public Result getHtmlDisplay(Request request, Entry entry)
            throws Exception {
        if (countdownHtml == null) {
            countdownHtml = getRepository().getResource(
                "/org/ramadda/plugins/gadgets/countdown.html");
        }
        String orient  = entry.getValue(0, "");
        String howMany = entry.getValue(1, "");
        if (howMany.length() == 0) {
            howMany = "4";
        }
        StringBuffer sb = new StringBuffer(countdownHtml);
        sb.append("<table><tr><td><center>");
        sb.append(getPageHandler().formatDate(request, entry.getStartDate(),
                getEntryUtil().getTimezone(entry)));
        Date   to = new Date(entry.getStartDate());
        String id = "countdownid_" + (countdownCnt++);
        //        sb.append(HtmlUtils.cssBlock(".countdown-clock {font-size: 150%;}\n.countdown-number {color:#A94DEA;\n.countdown-label {color:#000;}\n"));
        String inner = HtmlUtils.div("",
                                     HtmlUtils.id(id)
                                     + HtmlUtils.cssClass("countdown-clock"));
        sb.append("<table><td><td>"
                  + HtmlUtils.div(inner, HtmlUtils.cssClass("countdown"))
                  + "</td></tr></table>");
        sb.append(
            HtmlUtils.script(
                "$(document).ready(function() {countdownStart("
                + HtmlUtils.squote(entry.getName()) + ","
                + HtmlUtils.squote(id) + "," + (to.getTime() / 1000) + ","
                + HtmlUtils.squote(orient) + "," + howMany + ");});\n"));
        sb.append("</center></td></tr></table>");

        return new Result("Countdown", sb);
    }




}
