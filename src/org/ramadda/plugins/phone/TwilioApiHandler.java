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
import ucar.unidata.ui.HttpFormEntry;


import ucar.unidata.util.Misc;
import ucar.unidata.util.StringUtil;
import ucar.unidata.xml.XmlUtil;


import java.net.*;
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

    public static final String PROP_AUTHTOKEN = "twilio.authtoken";
    public static final String PROP_APPID = "twilio.appid";
    public static final String PROP_TRANSCRIBE = "twilio.transcribe";


    public static final String ARG_ACCOUNTSID = "AccountSid";
    public static final String ARG_RECORDINGSID = "RecordingSid";
    public static final String ARG_RECORDINGURL = "RecordingUrl";
    public static final String ARG_FROMZIP = "FromZip";


    public static final String XML_HEADER = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>";

    /** _more_          */
    public static final String TAG_RESPONSE = "Response";
    public static final String TAG_RECORD = "Record";
    public static final String TAG_TRANSCRIPTIONTEXT  = "TranscriptionText";
    public static final String TAG_STATUS = "Status";

    /** _more_          */
    public static final String TAG_SMS = "Sms";

    public static final String TAG_SAY = "Say";

    public static final String ATTR_VOICE = "voice";
    public static final String ATTR_TRANSCRIBE = "transcribe";
    public static final String ATTR_TO = "to";
    public static final String ATTR_FROM = "from";

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


    private boolean callOK(Request request) {
        String appId = getRepository().getProperty(PROP_APPID,null);
        if(appId == null) return false;
        return request.getString(ARG_ACCOUNTSID, "").equals(appId);
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
        //        System.err.println("TwilioApiHandler: request: " + request);
        StringBuffer sb =
            new StringBuffer(XML_HEADER);
        sb.append(XmlUtil.openTag(TAG_RESPONSE));
        info.setMessage(request.getString(ARG_BODY, ""));
        info.setFromZip(request.getString(ARG_FROMZIP, (String) null));
        boolean handledMessage = false;
        if(!callOK(request)) {
            sb.append(XmlUtil.tag(TAG_SMS, "", "Sorry, bad APPID property defined"));
        } else {
            StringBuffer msg = new StringBuffer();
            for (PhoneHarvester harvester : getHarvesters()) {
                if (harvester.handleMessage(request, info, msg)) {
                    String response = msg.toString();
                    if(response.length()==0) {
                        response = "Message handled";
                    } 

                    int cnt = 0;
                    System.err.println("************");
                    while(true) {
                        if(cnt++>5) break;
                        if(response.length()<160) {
                            sb.append(XmlUtil.tag(TAG_SMS, "", response));
                            break;
                        }
                        String prefix =response.substring(0,159);
                        sb.append(XmlUtil.tag(TAG_SMS, "", prefix));
                        response = response.substring(159);
                        System.err.println("prefix:" + prefix);
                        System.err.println("rest:" + response);
                    }
                    handledMessage = true;
                    break;
                }
            }


            if ( !handledMessage) {
                String response = msg.toString();
                sb.append(XmlUtil.tag(TAG_SMS, "", "Sorry, RAMADDA was not able to process your message.\n" + response));
            }
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
        String authToken = getRepository().getProperty(PROP_AUTHTOKEN, null);
        String recordingUrl = request.getString(ARG_RECORDINGURL, null);
        System.err.println("processVoice:" + request.getUrlArgs());
        StringBuffer sb = new StringBuffer();
        sb.append(XML_HEADER);
        sb.append(XmlUtil.openTag(TAG_RESPONSE));
        try {
            PhoneInfo info = new PhoneInfo(PhoneInfo.TYPE_SMS,
                                           request.getString(ARG_FROM, ""),
                                           request.getString(ARG_TO, ""), null);
            if(!callOK(request)) {
                sb.append(XmlUtil.tag(TAG_SAY,XmlUtil.attr(ATTR_VOICE,"woman"),"Sorry, bad application identifier"));
            } else {
                if(recordingUrl==null) {
                    String voiceResponse = null;
                    boolean canEdit = true;
                    for (PhoneHarvester harvester : getHarvesters()) {
                        String response = harvester.getVoiceResponse(info);
                        if(response!=null && response.trim().length()>0) {
                            voiceResponse = response;
                            canEdit = harvester.canEdit(info);
                            break;
                        }
                    }
                    if(voiceResponse==null) {
                        sb.append(XmlUtil.tag(TAG_SAY,XmlUtil.attr(ATTR_VOICE,"woman"),"Sorry, this ramadda repository does not accept voice messages"));
                    } else if(!canEdit) {
                        sb.append(XmlUtil.tag(TAG_SAY,XmlUtil.attr(ATTR_VOICE,"woman"),"Sorry, you need to login through a text message first"));
                    } else {
                        sb.append(XmlUtil.tag(TAG_SAY,XmlUtil.attr(ATTR_VOICE,"woman"),voiceResponse));
                        String recordAttrs = XmlUtil.attrs(new String[]{
                                "maxLength", "30",
                            });

                        if(getRepository().getProperty(PROP_TRANSCRIBE, false)) {
                            recordAttrs+= XmlUtil.attr(ATTR_TRANSCRIBE,"true");
                        }
                        sb.append(XmlUtil.tag(TAG_RECORD, recordAttrs));
                    }
                } else {
                    info.setRecordingUrl(recordingUrl);
                    if(getRepository().getProperty(PROP_TRANSCRIBE, false)) {
                        int cnt =0;
                        String text = null;
                        while(cnt++<5) {
                            text = getTranscriptionText(request, authToken);
                            if(text!=null) break;
                            Misc.sleepSeconds(3);
                        }
                        if(text!=null) {
                            info.setTranscription(text);
                        } else {
                            System.err.println("processVoice: failed to get transcription text");
                        }
                    }

                    for (PhoneHarvester harvester : getHarvesters()) {
                        StringBuffer msg = new StringBuffer();
                        if (harvester.handleVoice(request, info,msg)) {
                            if(msg.length()>0) {
                                String smsUrl = getApiPrefix()+"/SMS/Messages";
                                List<HttpFormEntry> postEntries = new ArrayList<HttpFormEntry>();
                                postEntries.add(HttpFormEntry.hidden("From", info.getToPhone()));
                                postEntries.add(HttpFormEntry.hidden("To", info.getFromPhone()));
                                postEntries.add(HttpFormEntry.hidden("Body", msg.toString()));
                                String[]result =  HttpFormEntry.doPost(postEntries, smsUrl);
                                if(result[0]!=null) {
                                    System.err.println ("Error posting to " + smsUrl);
                                    System.err.println (result[0]);
                                } else {
                                    System.err.println ("OK:" + result[1]);
                                }
                                /*
                                sb.append(XmlUtil.tag(TAG_SMS, XmlUtil.attrs(new String[]{
                                            ATTR_FROM, info.getToPhone(),
                                            ATTR_TO, info.getFromPhone(),
                                            }), msg.toString()));
                                */
                            }
                            break;
                        }
                    }

                }
            }
        } catch(Exception exc) {
            sb = new StringBuffer();
            sb.append(XML_HEADER);
            sb.append(XmlUtil.openTag(TAG_RESPONSE));
            sb.append(XmlUtil.tag(TAG_SAY,XmlUtil.attr(ATTR_VOICE,"woman"),"Sorry, an error occurred"));
            exc.printStackTrace();
            getLogManager().logError("Error handling twilio voice message", exc);
        }
        sb.append(XmlUtil.closeTag(TAG_RESPONSE));
        System.err.println("voice response:" + sb);
        return new Result("", sb, "text/xml");
    }


    private String getApiPrefix() {
        return  "https://api.twilio.com/2010-04-01/Accounts/" +
            getRepository().getProperty(PROP_APPID,null);
            
    }


    private String getTranscriptionText(Request request, String authToken) throws Exception {
        String transcriptionUrl = getApiPrefix()+"/Recordings/"+ 
            request.getString(ARG_RECORDINGSID,null)+"/Transcriptions";
        URL url        = new URL(transcriptionUrl);
        HttpURLConnection     huc = (HttpURLConnection) url.openConnection();            
        String auth  = request.getString(ARG_ACCOUNTSID, null)+":" + authToken;
        String encoding = RepositoryUtil.encodeBase64 (auth.getBytes());
        huc.addRequestProperty("Authorization",
                               "Basic " + encoding);
        String transcription = new String(IOUtil.readBytes(huc.getInputStream()));
        Element root = XmlUtil.getRoot(transcription);
        Element statusNode = XmlUtil.findDescendant(root,TAG_STATUS);
        if(statusNode==null) return null;
        if(!XmlUtil.getChildText(statusNode).equals("completed")) {
            return null;

        }

        Element textNode = XmlUtil.findDescendant(root,TAG_TRANSCRIPTIONTEXT);
        if(textNode!=null) {
            return  XmlUtil.getChildText(textNode);
        }
        return null;
    }


}
