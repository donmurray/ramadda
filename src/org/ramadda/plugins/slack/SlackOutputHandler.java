/*
 * Copyright (c) 2008-2015 Geode Systems LLC
 * This Software is licensed under the Geode Systems RAMADDA License available in the source distribution in the file 
 * ramadda_license.txt. The above copyright notice shall be included in all copies or substantial portions of the Software.
 */

package org.ramadda.plugins.slack;


import org.ramadda.repository.*;
import org.ramadda.repository.auth.*;
import org.ramadda.repository.metadata.Metadata;
import org.ramadda.repository.output.*;

import org.ramadda.sql.SqlUtil;
import org.ramadda.util.HtmlUtils;
import org.ramadda.util.Utils;


import org.w3c.dom.*;

import ucar.unidata.ui.ImageUtils;
import ucar.unidata.util.DateUtil;
import ucar.unidata.util.IOUtil;
import ucar.unidata.util.Misc;


import ucar.unidata.util.StringUtil;
import ucar.unidata.util.WmsUtil;
import ucar.unidata.xml.XmlUtil;

import java.io.*;


import java.io.File;


import java.net.*;


import java.text.SimpleDateFormat;

import java.util.ArrayList;
import java.util.Date;
import java.util.Date;
import java.util.Enumeration;


import java.util.GregorianCalendar;
import java.util.Hashtable;
import java.util.List;
import java.util.Properties;
import java.util.TimeZone;



import java.util.regex.*;

import java.util.zip.*;


/**
 *
 *
 * @author IDV Development Team
 * @version $Revision: 1.3 $
 */
public class SlackOutputHandler extends OutputHandler {


    public static final String PROP_SLACK_API_TOKEN = "slack.api.token";

    /** _more_ */
    public static final OutputType OUTPUT_SLACK_PUBLISH =
        new OutputType("Publish to Slack", "slack_publish",
                       OutputType.TYPE_VIEW, "", "/slack/slack.png");


    /**
     * _more_
     */
    public SlackOutputHandler() {}

    /**
     * _more_
     *
     *
     * @param repository _more_
     * @param element _more_
     * @throws Exception _more_
     */
    public SlackOutputHandler(Repository repository, Element element)
            throws Exception {
        super(repository, element);
        addType(OUTPUT_SLACK_PUBLISH);
    }

    /**
     * _more_
     *
     * @param request _more_
     * @param state _more_
     * @param links _more_
     *
     *
     * @throws Exception _more_
     */
    public void getEntryLinks(Request request, State state, List<Link> links)
            throws Exception {
        if(!request.isAnonymous() &&
           state.getEntry().isFile() &&  
           getAccessManager().canDoAction(request, state.getEntry(), Permission.ACTION_EDIT) &&
           getRepository().getProperty(PROP_SLACK_API_TOKEN, (String) null) !=null) {
            links.add(makeLink(request, state.getEntry(),  OUTPUT_SLACK_PUBLISH));
        }
    }


    /**
     * _more_
     *
     * @return _more_
     *
     * @throws Exception _more_
     */
    @Override
    public Result outputEntry(Request request, OutputType outputType,
                              Entry entry)
            throws Exception {
        if(!getAccessManager().canDoAction(request, entry, Permission.ACTION_EDIT)) {
            throw new IllegalArgumentException("No access");
        }
        if(getRepository().getProperty(PROP_SLACK_API_TOKEN, (String) null) ==null) {
            return new Result("", new StringBuilder("No Slack API token defined"));
        }

        StringBuilder sb = new StringBuilder("slack publish stuff here");

        return new Result("", sb);

    }


}
