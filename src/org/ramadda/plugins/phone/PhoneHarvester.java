/*
* Copyright 2008-2012 Jeff McWhirter/ramadda.org
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

import ucar.unidata.xml.XmlUtil;


import org.w3c.dom.*;
import ucar.unidata.util.Misc;
import ucar.unidata.util.StringUtil;
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
    public static final String ATTR_TYPE = "type";

    public static final String ATTR_FROMPHONE = "fromphone";
    public static final String ATTR_TOPHONE = "tophone";
    public static final String ATTR_PASSCODE = "passcode";
    public static final String ATTR_ = "";


    private String type = PhoneInfo.TYPE_SMS;
    private String fromPhone;
    private String toPhone;
    private String passCode;


    /**
     * _more_
     *
     * @param repository _more_
     * @param id _more_
     *
     * @throws Exception _more_
     */
    public PhoneHarvester(Repository repository, String id)
            throws Exception {
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
        fromPhone   = XmlUtil.getAttribute(element, ATTR_FROMPHONE, fromPhone);
        toPhone   = XmlUtil.getAttribute(element, ATTR_TOPHONE, toPhone);
        passCode   = XmlUtil.getAttribute(element, ATTR_PASSCODE, passCode);
        type   = XmlUtil.getAttribute(element, ATTR_TYPE, type);
    }


    public boolean handleMessage(Request request, PhoneInfo info) throws Exception {

        if(fromPhone.length()>0) {
            if(!Misc.equals(fromPhone, info.getFromPhone())) return false;
        }

        if(toPhone.length()>0) {
            if(!Misc.equals(toPhone, info.getToPhone())) return false;
        }

        TypeHandler typeHandler = getRepository().getTypeHandler("phone_sms");
        Entry baseGroup = getBaseGroup();
        Entry parent = baseGroup;
        Entry entry = typeHandler.createEntry(getRepository().getGUID());
        String name = "sms";
        String desc = info.getMessage();
        Date date = new Date();
        Object[] values = typeHandler.makeValues(new Hashtable());
        values[0] = info.getFromPhone();
        values[1] = info.getToPhone();
        entry.initEntry(name, desc, parent, getUser(), new Resource(), "",
                        date.getTime(), date.getTime(),
                        date.getTime(), date.getTime(), values);


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


    public void  makeRunSettings(Request request, StringBuffer sb) {
        StringBuffer runWidgets = new StringBuffer();
        runWidgets.append(HtmlUtils.checkbox(ATTR_ACTIVEONSTART, "true", getActiveOnStart()) + HtmlUtils.space(1) + msg("Active"));
        sb.append(
                  HtmlUtils.formEntryTop("",runWidgets.toString()));
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
        element.setAttribute( ATTR_FROMPHONE, fromPhone);
        element.setAttribute( ATTR_TOPHONE, toPhone);
        element.setAttribute( ATTR_PASSCODE, passCode);
        element.setAttribute( ATTR_TYPE, type);
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
        fromPhone   = request.getString(ATTR_FROMPHONE, fromPhone);
        toPhone   = request.getString(ATTR_TOPHONE, toPhone);
        passCode   = request.getString(ATTR_PASSCODE, passCode);
        type   = request.getString(ATTR_TYPE, type);
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
                                      HtmlUtils.input(ATTR_FROMPHONE, fromPhone,
                                                      HtmlUtils.SIZE_60)));
        sb.append(HtmlUtils.formEntry(msgLabel("To Phone"),
                                      HtmlUtils.input(ATTR_TOPHONE, toPhone,
                                                      HtmlUtils.SIZE_60)));
        sb.append(HtmlUtils.formEntry(msgLabel("Pass Code"),
                                      HtmlUtils.input(ATTR_PASSCODE, passCode,
                                                      HtmlUtils.SIZE_60)));
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


}
