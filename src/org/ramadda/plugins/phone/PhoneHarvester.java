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
import org.ramadda.repository.metadata.*;
import org.ramadda.repository.type.*;

import org.ramadda.util.HtmlUtils;


import org.w3c.dom.*;

import ucar.unidata.util.Misc;
import ucar.unidata.util.IOUtil;
import ucar.unidata.util.StringUtil;

import ucar.unidata.xml.XmlUtil;
import ucar.unidata.xml.XmlUtil;

import java.io.*;



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
public class PhoneHarvester extends Harvester {

    public static final String CMD_APPEND = "append";
    public static final String CMD_CD = "cd";
    public static final String CMD_PWD= "pwd";
    public static final String CMD_LS = "ls";
    public static final String CMD_GET = "get";
    public static final String CMD_URL = "url";
    public static final String DELIMITER = "/";

    /** _more_          */
    public static final String ATTR_TYPE = "type";

    /** _more_          */
    public static final String ATTR_FROMPHONE = "fromphone";

    /** _more_          */
    public static final String ATTR_TOPHONE = "tophone";

    /** _more_          */
    public static final String ATTR_PASSCODE = "passcode";

    public static final String ATTR_RESPONSE = "response";
    public static final String ATTR_VOICEMESSAGE = "voicemessage";

    /** _more_          */
    public static final String ATTR_ = "";


    /** _more_          */
    private String type = PhoneInfo.TYPE_SMS;

    /** _more_          */
    private String fromPhone;

    /** _more_          */
    private String toPhone;

    /** _more_          */
    private String passCode;

    /** _more_          */
    private String response;


    /** _more_          */
    private String voiceMessage;


    /**
     * _more_
     *
     * @param repository _more_
     * @param id _more_
     *
     * @throws Exception _more_
     */
    public PhoneHarvester(Repository repository, String id) throws Exception {
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
    public PhoneHarvester(Repository repository, Element node)
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
        fromPhone = XmlUtil.getAttribute(element, ATTR_FROMPHONE, fromPhone);
        toPhone   = XmlUtil.getAttribute(element, ATTR_TOPHONE, toPhone);
        passCode  = XmlUtil.getAttribute(element, ATTR_PASSCODE, passCode);
        response  = XmlUtil.getAttribute(element, ATTR_RESPONSE, response);
        voiceMessage = XmlUtil.getAttribute(element, ATTR_VOICEMESSAGE,  voiceMessage);
        type      = XmlUtil.getAttribute(element, ATTR_TYPE, type);
    }


    private Hashtable<String,String> phoneToEntry = new Hashtable<String,String>();


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
    public boolean handleMessage(Request request, PhoneInfo info, StringBuffer returnMsg)
            throws Exception {

        System.err.println ("handleMessage:" + fromPhone +":" +info.getFromPhone() +": to phone:" + toPhone +":" +
                            info.getToPhone());
        if (fromPhone!=null && fromPhone.length() > 0) {
            if ( info.getFromPhone().indexOf(fromPhone)<0) {
                System.err.println ("handleMessage: skipping wrong from phone");
                return false;
            }
        }

        if (toPhone!= null && toPhone.length() > 0) {
            if ( info.getToPhone().indexOf(toPhone)<0) {
                System.err.println ("handleMessage: skipping wrong to phone");
                return false;
            }
        }

        Entry       baseGroup   = getBaseGroup();
        Entry currentEntry  = baseGroup;
        String pastEntry = phoneToEntry.get(info.getFromPhone());
        if(pastEntry!=null) {
            Entry entry = getEntryManager().getEntry(request, pastEntry, false);
            if (entry!=null) currentEntry = entry;
        }


        String      message        = info.getMessage();
        String      name        = "SMS Message";
        String toEntryName = null;
        int spaceIndex = message.indexOf(" ", 10);
        if(spaceIndex<0) {
            spaceIndex = message.indexOf("\n");
        }
        if(spaceIndex>0) {
            name = message.substring(0, spaceIndex);
        } else {
            //??
        }

        if(passCode!=null && passCode.length()>0) {
            if(message.indexOf(passCode)<0) {
                returnMsg.append("Message does not contain passcode");
                return true;
            }
            message = message.replace(passCode,"");
        }
        message = message.trim();
        if(message.equals("help") || message.equals("?")) {
            returnMsg.append("\n" + CMD_LS +  "," +   CMD_CD +  "," + CMD_URL +"," + CMD_GET + " &lt;path&gt;\n"+
                             CMD_APPEND+"\n" +
                             "create:\nfolder,wiki,note,sms &lt;name&gt;\n" +
                             "name &lt;entry name&gt;\n" +
                             "&lt;text&gt;");
            return true;
        }
        String      type = null;
        String tmp;
        StringBuffer desc = new StringBuffer();
        boolean doAppend = false;
        boolean processedACommand = false;
        for(String line: StringUtil.split(message,"\n")) {
            line = line.trim();
            String tline = line.toLowerCase();
            boolean skipLine = false;

            for(String prefix: new String[]{
                    "title","Title","name","Name","nm","Nm","Subject", "subject"
                }) {
                if((tmp =  StringUtil.findPattern(line,prefix+"\\s(.+)"))!=null) {
                    name = tmp;
                    skipLine = true;
                    break;
                }
            }

            if(skipLine) continue;

            if(tline.startsWith(CMD_LS)) {
                String remainder = line.substring(CMD_LS.length()).trim();
                for(Entry child: getEntryManager().getChildren(request, currentEntry)) {
                    String childName = child.getName();
                    if(remainder.length()>0) {
                        if(childName.indexOf(remainder)<0) {
                            continue;
                        }
                    }
                    if((returnMsg.length() + childName.length())>120) break;
                    returnMsg.append(childName.trim());
                    if(child.isGroup()) {
                        returnMsg.append(" (folder)");
                    }
                    returnMsg.append("\n");
                }
                return true;
            }

            if(tline.startsWith(CMD_URL)) {
                currentEntry =  getEntry(request, line, CMD_URL, currentEntry, returnMsg);
                if(currentEntry == null) return true;
                returnMsg.append("entry:\n" + getEntryInfo(currentEntry));
                return true;
            }

            if(tline.startsWith(CMD_GET)) {
                currentEntry =  getEntry(request, line, CMD_GET, currentEntry, returnMsg);
                if(currentEntry == null) return true;
                returnMsg.append(currentEntry.getName() +"\n" +currentEntry.getDescription());
                return true;
            }


            if(tline.equals(CMD_PWD)) {
                returnMsg.append(getEntryInfo(currentEntry));
                return true;
            }

            if(tline.startsWith(CMD_CD)) {
                if(line.substring(CMD_CD.length()).trim().length()==0) {
                    currentEntry = baseGroup;
                } else {
                    currentEntry =  getEntry(request, line, CMD_CD, currentEntry, returnMsg);
                    if(currentEntry == null) return true;
                }
                phoneToEntry.put(info.getFromPhone(), currentEntry.getId());
                processedACommand = true;
                continue;
            }

            if(tline.startsWith(CMD_APPEND)) {
                currentEntry =  getEntry(request, line, CMD_APPEND, currentEntry, returnMsg);
                if(currentEntry == null) return true;
                doAppend = true;
                processedACommand = true;
                continue;
            }

            if(type == null) {
                String[]cmds = {"folder","wiki","sms","note"};
                String[]types = {TypeHandler.TYPE_GROUP,"wikipage","phone_sms","notes_note"};
                boolean didOne = false;
                for(int i=0;i<cmds.length;i++) {
                    if(tline.startsWith(cmds[i] +" ")) {
                        type =types[i] ;
                        name  = line.substring(cmds[i].length()).trim();
                        didOne = true;
                        break;
                    }
                }
                if(didOne) continue;
            }

            if(desc.length()>0)
                desc.append("<br>");
            desc.append(line);
        }


        Entry  parent      = currentEntry;
        if(doAppend) {
            //TODO: handle wiki and update the entry better
            if(currentEntry.getTypeHandler().getType().equals("wikipage")) {
            } else {
                currentEntry.setDescription(currentEntry.getDescription() +"\n" + desc);
            }
            getEntryManager().updateEntry(currentEntry);
            returnMsg.append("entry appended to\n" + getEntryInfo(currentEntry));
            return true;
        }

        if(type == null) {
            if(!processedACommand) {
                returnMsg.append("Nothing much happened");
            } else {
                returnMsg.append("OK");
            }
            return true;
        }

        if(!defined(name)) { 
           name  = "SMS Entry";
        }
        name = name.trim();



        if(!currentEntry.isGroup()) {
            returnMsg.append("Not a folder:\n" +currentEntry.getName());
            return true;
        }

        TypeHandler typeHandler = getRepository().getTypeHandler(type);
        Entry       entry = typeHandler.createEntry(getRepository().getGUID());
        Date        date        = new Date();
        Object[]    values      = typeHandler.makeValues(new Hashtable());
        if(type.equals("phone_sms")) {
            values[0] = info.getFromPhone();
            values[1] = info.getToPhone();
        } else if (type.equals("wikipage")) {
            values[0] = desc.toString();
            desc = new StringBuffer();
        }

        entry.initEntry(name, desc.toString(), currentEntry, getUser(), new Resource(), "",
                        date.getTime(), date.getTime(), date.getTime(),
                        date.getTime(), values);


        double[] location = org.ramadda.util.GeoUtils.getLocationFromAddress(
                                info.getFromZip());
        if (location != null) {
            entry.setLocation(location[0], location[1], 0);
        }

        List<Entry> entries = (List<Entry>) Misc.newList(entry);
        getEntryManager().insertEntries(entries, true, true);
        String template = response;
        if(template == null || template.trim().length()==0) template = "Entry created:\n${url}";
        template = template.replace("${url}", getEntryInfo(entry));
        returnMsg.append(template);
        return true;
    }

    private String getEntryUrl(Entry entry) {
        return  HtmlUtils.url(getRepository().URL_ENTRY_SHOW.getFullUrl(),
                          ARG_ENTRYID, entry.getId());

    }

    private String getEntryInfo(Entry entry) {
        return  entry.getName() +"\n" + getEntryUrl(entry);
    }


    private boolean checkPhone(PhoneInfo info) {
        if (fromPhone!=null && fromPhone.length() > 0) {
            if ( info.getFromPhone().indexOf(fromPhone)<0) {
                System.err.println ("handleMessage: skipping wrong from phone");
                return false;
            }
        }

        if (toPhone!= null && toPhone.length() > 0) {
            if ( info.getToPhone().indexOf(toPhone)<0) {
                System.err.println ("handleMessage: skipping wrong to phone");
                return false;
            }
        }
        return true;
    }


    public boolean handleVoice(Request request, PhoneInfo info)
            throws Exception {
        //        if(passCode!=null && passCode.length()>0) {
        if(getVoiceResponse(info)==null) {
            return false;
        }
        System.err.println ("handleVoice:" + fromPhone +":" +info.getFromPhone() +": to phone:" + toPhone +":" +
                            info.getToPhone());
        Entry       baseGroup   = getBaseGroup();
        Entry       parent      = baseGroup;
        String      name        = "Voice Message  - " + getRepository().formatDate(request, new Date());
        String      type = "media_audiofile";
        TypeHandler typeHandler = getRepository().getTypeHandler(type);
        Entry       entry = typeHandler.createEntry(getRepository().getGUID());
        Date        date        = new Date();
        Object[]    values      = typeHandler.makeValues(new Hashtable());
        StringBuffer desc = new StringBuffer(info.getTranscription());
        File  voiceFile = fetchVoiceFile(request, new URL(info.getRecordingUrl()));
        if(voiceFile==null) {
            return false;
        }
        voiceFile =     getStorageManager().moveToStorage(request, voiceFile);
        Resource resource = new Resource(voiceFile.toString(), Resource.TYPE_STOREDFILE);
        entry.initEntry(name, desc.toString(), parent, getUser(), resource, "",
                        date.getTime(), date.getTime(), date.getTime(),
                        date.getTime(), values);


        double[] location = org.ramadda.util.GeoUtils.getLocationFromAddress(
                                info.getFromZip());
        if (location != null) {
            entry.setLocation(location[0], location[1], 0);
        }

        List<Entry> entries = (List<Entry>) Misc.newList(entry);
        getEntryManager().insertEntries(entries, true, true);
        return true;
    }




    /**
     * _more_
     *
     * @return _more_
     */
    public String getDescription() {
        return "Phone and SMS Harvester";
    }


    /**
     * _more_
     *
     * @param request _more_
     * @param sb _more_
     */
    public void makeRunSettings(Request request, StringBuffer sb) {
        StringBuffer runWidgets = new StringBuffer();
        runWidgets.append(
            HtmlUtils.checkbox(
                ATTR_ACTIVEONSTART, "true",
                getActiveOnStart()) + HtmlUtils.space(1) + msg("Active"));
        sb.append(HtmlUtils.formEntryTop("", runWidgets.toString()));
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
        element.setAttribute(ATTR_FROMPHONE, fromPhone);
        element.setAttribute(ATTR_TOPHONE, toPhone);
        element.setAttribute(ATTR_PASSCODE, passCode);
        element.setAttribute(ATTR_RESPONSE, response);
        element.setAttribute(ATTR_VOICEMESSAGE, voiceMessage);
        element.setAttribute(ATTR_TYPE, type);
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
        fromPhone = request.getString(ATTR_FROMPHONE, fromPhone);
        toPhone   = request.getString(ATTR_TOPHONE, toPhone);
        passCode  = request.getString(ATTR_PASSCODE, passCode);
        response  = request.getString(ATTR_RESPONSE, response);
        voiceMessage  = request.getString(ATTR_VOICEMESSAGE, voiceMessage);
        type      = request.getString(ATTR_TYPE, type);
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
        sb.append(HtmlUtils.formEntry(msgLabel("From Phone"),
                                      HtmlUtils.input(ATTR_FROMPHONE,
                                          fromPhone, HtmlUtils.SIZE_60)));
        sb.append(HtmlUtils.formEntry(msgLabel("To Phone"),
                                      HtmlUtils.input(ATTR_TOPHONE, toPhone,
                                          HtmlUtils.SIZE_60)));
        sb.append(HtmlUtils.formEntry(msgLabel("Pass Code"),
                                      HtmlUtils.input(ATTR_PASSCODE,
                                          passCode, HtmlUtils.SIZE_60)));

        sb.append(HtmlUtils.formEntryTop(msgLabel("SMS Response"),
                                      HtmlUtils.textArea(ATTR_RESPONSE,
                                                         response==null?"":response,5,60) +"<br>" + "Use ${url} for the URL to the created entry"));


        sb.append(HtmlUtils.formEntryTop(msgLabel("Voice Message"),
                                         HtmlUtils.input(ATTR_VOICEMESSAGE,voiceMessage,  HtmlUtils.SIZE_60) +"<br>" + "Specify a voice response to handle voice message"));
    }



    /**
     * _more_
     *
     *
     * @param timestamp _more_
     * @throws Exception _more_
     */
    protected void runInner(int timestamp) throws Exception {
        if ( !canContinueRunning(timestamp)) {
            //            return true;
        }
    }


    public String getVoiceResponse(PhoneInfo info) {
        if(!checkPhone(info)) {
            return null;
        }
        if(voiceMessage!=null && voiceMessage.trim().length()==0) return null;
        return voiceMessage;
    }


    private File fetchVoiceFile(Request request, URL url) throws Exception {
        String tail    = "voicemessage.wav";
        File   newFile = getStorageManager().getTmpFile(request,
                                                        tail);
        URLConnection connection = url.openConnection();
        InputStream   fromStream = connection.getInputStream();
        FileOutputStream toStream =
            getStorageManager().getFileOutputStream(newFile);
        try {
            int bytes = IOUtil.writeTo(fromStream, toStream);
            if (bytes < 0) {
                System.err.println("PhoneHarvester: failed to read voice URL:" + url);
                return null;
            }
        } finally {
            IOUtil.close(toStream);
            IOUtil.close(fromStream);
        }
        return newFile;
    }

    private Entry getEntry(Request request, String line, String cmd, Entry currentEntry, StringBuffer returnMsg) throws Exception {
        for(String newEntryName: StringUtil.split(line.substring(cmd.length()).trim(),DELIMITER, true, true)) {
            Entry childEntry  = getEntryManager().findEntryWithName(request, currentEntry, newEntryName);
            if(childEntry==null) {
                returnMsg.append("Could not find:\n" +newEntryName);
                return null;
            }
            currentEntry = childEntry;
        }
        return currentEntry;
    }


}
