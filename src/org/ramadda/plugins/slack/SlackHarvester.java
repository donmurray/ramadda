/**
* Copyright (c) 2008-2015 Geode Systems LLC
* This Software is licensed under the Geode Systems RAMADDA License available in the source distribution in the file 
* ramadda_license.txt. The above copyright notice shall be included in all copies or substantial portions of the Software.
*/

package org.ramadda.plugins.slack;


import org.ramadda.repository.*;
import org.ramadda.repository.auth.*;
import org.ramadda.repository.harvester.*;
import org.ramadda.repository.metadata.*;
import org.ramadda.repository.type.*;
import org.ramadda.util.HtmlUtils;


import org.ramadda.util.Utils;


import org.w3c.dom.*;

import ucar.unidata.util.IOUtil;

import ucar.unidata.util.Misc;
import ucar.unidata.util.StringUtil;
import ucar.unidata.util.TwoFacedObject;

import ucar.unidata.xml.XmlUtil;
import ucar.unidata.xml.XmlUtil;

import java.io.*;

import java.net.*;



import java.util.ArrayList;
import java.util.Date;
import java.util.Enumeration;
import java.util.GregorianCalendar;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.List;
import java.util.Properties;




/**
 */
public class SlackHarvester extends Harvester {


    /** _more_ */
    public static final String SLACK_TOKEN = "token";

    /** _more_ */
    public static final String SLACK_TEAM_ID = "team_id";

    /** _more_ */
    public static final String SLACK_TEAM_DOMAIN = "team_domain";

    /** _more_ */
    public static final String SLACK_CHANNEL_ID = "channel_id";

    /** _more_ */
    public static final String SLACK_CHANNEL_NAME = "channel_name";

    /** _more_ */
    public static final String SLACK_TIMESTAMP = "timestamp";

    /** _more_ */
    public static final String SLACK_USER_ID = "user_id";

    /** _more_ */
    public static final String SLACK_USER_NAME = "user_name";

    /** _more_ */
    public static final String SLACK_TEXT = "text";

    /** _more_ */
    public static final String SLACK_TRIGGER_WORD = "trigger_word";



    /** _more_ */
    public static final String ATTR_TOKEN = "token";


    /** _more_ */
    private String token;



    /**
     * _more_
     *
     * @param repository _more_
     * @param id _more_
     *
     * @throws Exception _more_
     */
    public SlackHarvester(Repository repository, String id) throws Exception {
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
    public SlackHarvester(Repository repository, Element node)
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
        token = XmlUtil.getAttribute(element, ATTR_TOKEN, token);
    }



    /**
     * _more_
     *
     * @return _more_
     */
    public String getDescription() {
        return "Slack Harvester";
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
        if (token != null) {
            element.setAttribute(ATTR_TOKEN, token);
        }
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
        token = request.getString(ATTR_TOKEN, token);
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
        sb.append(HtmlUtils.formEntry(msgLabel("Slack Token"),
                                      HtmlUtils.input(ATTR_TOKEN, token,
                                          HtmlUtils.SIZE_60) + " "
                                              + "Token from Slack"));
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
    public Result handleRequest(Request request) throws Exception {
        if ( !Utils.stringDefined(token)
                || !Misc.equals(token,
                                request.getString(SLACK_TOKEN, "none"))) {
            return null;
        }

        String       channelId   = request.getString(SLACK_CHANNEL_ID, "");
        String       channelName = request.getString(SLACK_CHANNEL_NAME, "");
        String       userId      = request.getString(SLACK_USER_ID, "");
        String       userName    = request.getString(SLACK_USER_NAME, "");
        String       text        = request.getString(SLACK_TEXT, "");
        StringBuffer sb          = new StringBuffer();
        sb.append("Channel: " + channelName);

        return new Result("", sb);
    }

}
