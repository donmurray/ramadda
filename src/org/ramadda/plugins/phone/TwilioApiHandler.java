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

package org.ramadda.plugins.phone;


import org.ramadda.repository.*;
import org.ramadda.repository.auth.*;
import org.ramadda.repository.harvester.*;
import org.ramadda.repository.search.*;
import org.ramadda.repository.type.*;

import org.ramadda.util.HtmlUtils;
import org.ramadda.util.HtmlUtils;

import org.w3c.dom.*;

import ucar.unidata.util.IOUtil;


import ucar.unidata.util.Misc;
import ucar.unidata.util.StringUtil;
import ucar.unidata.xml.XmlUtil;


import java.io.*;

import java.util.ArrayList;
import java.util.Hashtable;
import java.util.List;
import java.util.regex.*;


/**
 * Provides a top-level API
 *
 */
public class TwilioApiHandler extends RepositoryManager implements RequestHandler {

    /** _more_          */
    public static final String TAG_RESPONSE = "Response";

    /** _more_          */
    public static final String TAG_SMS = "Sms";


    /** _more_          */
    public static final String ARG_FROM = "From";

    /** _more_          */
    public static final String ARG_TO = "To";

    /** _more_          */
    public static final String ARG_BODY = "Body";

    /** _more_          */
    public static final String ARG_ = "";
    //    public static final String ARG_  = ""; 

    /**
     *     ctor
     *    
     *     @param repository the repository
     *     @param node xml from api.xml
     *     @param props propertiesn
     *    
     *     @throws Exception on badness
     */
    public TwilioApiHandler(Repository repository) throws Exception {
        super(repository);
    }



    /**
     * _more_
     *
     * @return _more_
     */
    public List<PhoneHarvester> getHarvesters() {
        List<PhoneHarvester> harvesters = new ArrayList<PhoneHarvester>();
        for (Harvester harvester : getHarvesterManager().getHarvesters()) {
            if (harvester.getActiveOnStart()
                    && (harvester instanceof PhoneHarvester)) {
                harvesters.add((PhoneHarvester) harvester);
            }
        }

        return harvesters;
    }



    /**
     * handle the request
     *
     * @param request request
     *
     * @return result
     *
     * @throws Exception on badness
     */
    public Result processSms(Request request) throws Exception {
        PhoneInfo info = new PhoneInfo(PhoneInfo.TYPE_SMS,
                                       request.getString(ARG_FROM, ""),
                                       request.getString(ARG_TO, ""), null);
        System.err.println("Phone: " + info);
        System.err.println("request: " + request);
        StringBuffer sb =
            new StringBuffer("<?xml version=\"1.0\" encoding=\"UTF-8\"?>");
        sb.append(XmlUtil.openTag(TAG_RESPONSE));
        info.setMessage(request.getString(ARG_BODY, ""));
        info.setFromZip(request.getString("FromZip", (String) null));
        boolean handledMessage = false;
        for (PhoneHarvester harvester : getHarvesters()) {
            if (harvester.handleMessage(request, info)) {
                sb.append(XmlUtil.tag(TAG_SMS, "", "Cool!"));
                handledMessage = true;

                break;
            }
        }

        if ( !handledMessage) {
            sb.append(XmlUtil.tag(TAG_SMS, "", "Sorry dude"));
        }
        sb.append(XmlUtil.closeTag(TAG_RESPONSE));

        return new Result("", sb, "text/xml");
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
    public Result processVoice(Request request) throws Exception {
        System.err.println("sms from:" + request.getString("From", "none"));
        System.err.println("sms body:" + request.getString("Body", "none"));
        String recordingUrl = request.getString("RecordingUrl", "none");
        System.err.println("sms url:" + recordingUrl);
        System.err.println("sms args:" + request.getUrlArgs());
        StringBuffer sb = new StringBuffer();
        sb.append("<?xml version=\"1.0\" encoding=\"UTF-8\"?>");
        sb.append("<Response>");
        sb.append(
            "<Say voice=\"woman\">This is RAMADDA. Please leave a message.</Say>");
        sb.append("<Record maxLength=\"20\" />");
        sb.append("</Response>");

        return new Result("", sb, "text/xml");
    }



}
