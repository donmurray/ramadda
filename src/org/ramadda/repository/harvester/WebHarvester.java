/*
* Copyright 2008-2011 Jeff McWhirter/ramadda.org
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

package org.ramadda.repository.harvester;


import org.ramadda.repository.*;
import org.ramadda.repository.auth.*;
import org.ramadda.repository.metadata.*;
import org.ramadda.repository.output.*;
import org.ramadda.repository.type.*;


import org.w3c.dom.*;


import ucar.unidata.sql.SqlUtil;
import ucar.unidata.util.DateUtil;
import ucar.unidata.util.HtmlUtil;

import ucar.unidata.util.IOUtil;
import ucar.unidata.util.LogUtil;
import ucar.unidata.util.Misc;


import ucar.unidata.util.StringUtil;
import ucar.unidata.util.TwoFacedObject;
import ucar.unidata.xml.XmlUtil;

import java.io.File;

import java.lang.reflect.*;



import java.net.*;



import java.util.ArrayList;
import java.util.Date;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.List;
import java.util.Properties;



import java.util.regex.*;


/**
 *
 *
 * @author RAMADDA Development Team
 * @version $Revision: 1.3 $
 */
public class WebHarvester extends Harvester {

    /** _more_ */
    public static final String TAG_URLS = "urls";

    /** _more_ */
    public static final String ATTR_URLS = "urls";


    /** _more_ */
    private List<String> patternNames = new ArrayList<String>();


    /** _more_ */
    private List<HarvesterEntry> urlEntries = new ArrayList<HarvesterEntry>();

    /** _more_ */
    User user;


    /** _more_ */
    List<String> statusMessages = new ArrayList<String>();

    /** _more_ */
    private int entryCnt = 0;

    /** _more_ */
    private int newEntryCnt = 0;


    /**
     * _more_
     *
     * @param repository _more_
     * @param id _more_
     *
     * @throws Exception _more_
     */
    public WebHarvester(Repository repository, String id) throws Exception {
        super(repository, id);
    }

    /**
     * _more_
     *
     * @param repository _more_
     * @param element _more_
     *
     * @throws Exception _more_
     */
    public WebHarvester(Repository repository, Element element)
            throws Exception {
        super(repository, element);
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
        rootDir = new File(XmlUtil.getAttribute(element, ATTR_ROOTDIR, ""));
        List children = XmlUtil.findChildren(element,
                                             HarvesterEntry.TAG_URLENTRY);
        urlEntries = new ArrayList<HarvesterEntry>();
        for (int i = 0; i < children.size(); i++) {
            Element node = (Element) children.get(i);
            urlEntries.add(new HarvesterEntry(node));
        }
    }


    /**
     * _more_
     *
     * @return _more_
     */
    public String getDescription() {
        return "URL";
    }

    /**
     * _more_
     *
     * @return _more_
     *
     * @throws Exception _more_
     */
    protected User getUser() throws Exception {
        if (user == null) {
            user = repository.getUserManager().getDefaultUser();
        }
        return user;
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
        for (HarvesterEntry urlEntry : urlEntries) {
            urlEntry.toXml(element);
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
        StringBuffer sb  = new StringBuffer();
        int          cnt = 1;
        urlEntries = new ArrayList<HarvesterEntry>();
        HarvesterEntry lastEntry = null;
        while (true) {
            System.err.println("loop:" + cnt);
            String urlArg = ATTR_URL + cnt;
            if ( !request.exists(urlArg)) {
                System.err.println("done cnt:" + cnt);
                break;
            }
            if ( !request.defined(urlArg)) {
                System.err.println(urlArg + " not defined cnt = " + cnt);
                cnt++;
                continue;
            }
            String baseGroupId = request.getUnsafeString(ATTR_BASEGROUP + cnt
                                     + "_hidden", "");

            String groupName = request.getUnsafeString(ATTR_GROUP + cnt, "");
            groupName = groupName.replace(" > ", "/");
            groupName = groupName.replace(">", "/");

            System.err.println("cnt:" + cnt);
            System.err.println(groupName + " " + baseGroupId);
            lastEntry = new HarvesterEntry(request.getUnsafeString(urlArg,
                    ""), request.getUnsafeString(ATTR_NAME + cnt, ""),
                         request.getUnsafeString(ATTR_DESCRIPTION + cnt, ""),
                         groupName, baseGroupId);
            urlEntries.add(lastEntry);
            cnt++;
        }

    }



    /**
     * _more_
     *
     * @param request _more_
     * @param superSB _more_
     *
     * @throws Exception _more_
     */
    public void addToEditForm(Request request, StringBuffer superSB)
            throws Exception {}

    /**
     * _more_
     *
     * @param request _more_
     * @param formSB _more_
     *
     * @throws Exception _more_
     */
    public void createEditForm(Request request, StringBuffer formSB)
            throws Exception {

        StringBuffer sb      = new StringBuffer();
        StringBuffer superSB = new StringBuffer();

        formSB.append(HtmlUtil.formTableClose());
        superSB.append(HtmlUtil.formTable());
        super.createEditForm(request, superSB);
        superSB.append(
            HtmlUtil.formEntry(
                msgLabel("Entry type"),
                repository.makeTypeSelect(
                    request, false, getTypeHandler().getType(), false,
                    null)));


        superSB.append(HtmlUtil.formEntry(msgLabel("User"),
                                          HtmlUtil.input(ATTR_USER,
                                              (getUserName() != null)
                ? getUserName().trim()
                : "", HtmlUtil.SIZE_30)));




        addToEditForm(request, superSB);


        superSB.append(HtmlUtil.formTableClose());
        formSB.append(HtmlUtil.makeShowHideBlock("Basic Information",
                superSB.toString(), true));

        sb.append(HtmlUtil.hr());
        sb.append("Enter urls and the folders to add them to.");


        int          cnt = 1;

        StringBuffer entrySB;
        for (HarvesterEntry urlEntry : urlEntries) {
            entrySB = new StringBuffer();
            entrySB.append(HtmlUtil.formTable());
            String link = "";
            if ((urlEntry.url != null) && (urlEntry.url.length() > 0)) {
                link = HtmlUtil.href(
                    urlEntry.url,
                    HtmlUtil.img(getRepository().iconUrl(ICON_LINK)),
                    HtmlUtil.attr("target", "_linkpage"));
            }
            String urlInput = HtmlUtil.input(ATTR_URL + cnt, urlEntry.url,
                                             HtmlUtil.SIZE_80) + link;

            entrySB.append(HtmlUtil.formEntry(msgLabel("Fetch URL"),
                    urlInput));
            addEntryToForm(request, entrySB, urlEntry, cnt);
            entrySB.append(HtmlUtil.formTableClose());
            sb.append(HtmlUtil.makeShowHideBlock("URL #" + cnt,
                    entrySB.toString(), true));
            sb.append(HtmlUtil.hr());
            cnt++;
        }

        entrySB = new StringBuffer();
        entrySB.append(HtmlUtil.formTable());
        entrySB.append(HtmlUtil.formEntry(msgLabel("Fetch URL"),
                                          HtmlUtil.input(ATTR_URL + cnt, "",
                                              HtmlUtil.SIZE_80)));
        /*
        entrySB.append(
            RepositoryManager.tableSubHeader("Then create an entry with"));
        entrySB.append(
            HtmlUtil.formEntry(
                msgLabel("Name"),
                HtmlUtil.input(
                    ATTR_NAME + cnt, "",
                    HtmlUtil.SIZE_80 + HtmlUtil.title(templateHelp))));
        entrySB.append(
            HtmlUtil.formEntry(
                msgLabel("Description"),
                HtmlUtil.input(
                    ATTR_DESCRIPTION + cnt, "",
                    HtmlUtil.SIZE_80 + HtmlUtil.title(templateHelp))));
        entrySB.append(
            HtmlUtil.formEntry(
                msgLabel("Sub-Folder"),
                HtmlUtil.input(
                    ATTR_GROUP + cnt, "",
                    HtmlUtil.SIZE_80 + HtmlUtil.title(templateHelp))));
        */

        entrySB.append(HtmlUtil.formTableClose());

        sb.append(HtmlUtil.makeShowHideBlock("New URL", entrySB.toString(),
                                             true));


        sb.append(HtmlUtil.p());

        formSB.append(sb);
        formSB.append(HtmlUtil.formTable());

    }


    /** _more_ */
    public static final String templateHelp =
        "Use macros: ${filename}, ${fromdate}, ${todate}, etc.";



    /**
     * _more_
     *
     * @param request _more_
     * @param entrySB _more_
     * @param urlEntry _more_
     * @param cnt _more_
     *
     * @throws Exception _more_
     */
    protected void addEntryToForm(Request request, StringBuffer entrySB,
                                  HarvesterEntry urlEntry, int cnt)
            throws Exception {
        entrySB.append(
            RepositoryManager.tableSubHeader("Then create an entry with"));

        entrySB.append(
            HtmlUtil.formEntry(
                msgLabel("Name"),
                HtmlUtil.input(
                    ATTR_NAME + cnt, urlEntry.name,
                    HtmlUtil.SIZE_80 + HtmlUtil.title(templateHelp))));
        entrySB.append(
            HtmlUtil.formEntry(
                msgLabel("Description"),
                HtmlUtil.input(
                    ATTR_DESCRIPTION + cnt, urlEntry.description,
                    HtmlUtil.SIZE_80 + HtmlUtil.title(templateHelp))));

        addBaseFolderToForm(request, entrySB, urlEntry, cnt);

        String fieldId = ATTR_GROUP + cnt;
        entrySB.append(
            HtmlUtil.formEntry(
                msgLabel("Sub-Folder Template"),
                HtmlUtil.input(
                    fieldId, urlEntry.group,
                    HtmlUtil.SIZE_80 + HtmlUtil.id(fieldId)
                    + HtmlUtil.title(templateHelp))));

    }


    /**
     * _more_
     *
     * @param request _more_
     * @param entrySB _more_
     * @param urlEntry _more_
     * @param cnt _more_
     *
     * @throws Exception _more_
     */
    protected void addBaseFolderToForm(Request request, StringBuffer entrySB,
                                       HarvesterEntry urlEntry, int cnt)
            throws Exception {
        String baseGroupFieldId = ATTR_BASEGROUP + cnt;
        Entry  baseGroup        = ((urlEntry.baseGroupId.length() == 0)
                                   ? null
                                   : getEntryManager().findGroup(request,
                                       urlEntry.baseGroupId));
        String baseSelect = OutputHandler.getGroupSelect(request,
                                baseGroupFieldId);
        entrySB.append(HtmlUtil.hidden(baseGroupFieldId + "_hidden",
                                       urlEntry.baseGroupId,
                                       HtmlUtil.id(baseGroupFieldId
                                           + "_hidden")));
        entrySB.append(
            HtmlUtil.formEntry(
                msgLabel("Base Folder"),
                HtmlUtil.disabledInput(baseGroupFieldId, ((baseGroup != null)
                ? baseGroup.getFullName()
                : ""), HtmlUtil.id(baseGroupFieldId)
                       + HtmlUtil.SIZE_60) + baseSelect));

    }







    /**
     * _more_
     *
     * @return _more_
     *
     * @throws Exception _more_
     */
    public String getExtraInfo() throws Exception {
        String error = getError();
        if (error != null) {
            return super.getExtraInfo();
        }

        String messages = StringUtil.join("", statusMessages);
        return status.toString() + ((messages.length() == 0)
                                    ? ""
                                    : HtmlUtil.makeShowHideBlock("Entries",
                                    messages, false));
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
            return;
        }
        entryCnt       = 0;
        newEntryCnt    = 0;
        statusMessages = new ArrayList<String>();
        status         = new StringBuffer("Fetching URLS<br>");
        int cnt = 0;

        if (getTestMode()) {
            collectEntries((cnt == 0));
            return;
        }

        while (canContinueRunning(timestamp)) {
            long t1 = System.currentTimeMillis();
            collectEntries((cnt == 0));
            long t2 = System.currentTimeMillis();
            cnt++;
            if ( !getMonitor()) {
                status = new StringBuffer("Done<br>");
                break;
            }

            status = new StringBuffer();
            doPause();
            if ( !canContinueRunning(timestamp)) {
                return;
            }
        }
    }




    /**
     * _more_
     *
     * @param firstTime _more_
     *
     *
     * @throws Exception _more_
     */
    private void collectEntries(boolean firstTime) throws Exception {
        List<Entry> entries = new ArrayList<Entry>();
        for (HarvesterEntry urlEntry : urlEntries) {
            if ( !getActive()) {
                return;
            }
            if ( !processEntry(urlEntry, entries)) {
                break;
            }
        }
        newEntryCnt += entries.size();
        if (entries.size() > 0) {
            getEntryManager().insertEntries(entries, true, true);
        }
    }


    /**
     * _more_
     *
     * @param urlEntry _more_
     * @param entries _more_
     *
     * @return _more_
     *
     * @throws Exception _more_
     */
    protected boolean processEntry(HarvesterEntry urlEntry,
                                   List<Entry> entries)
            throws Exception {

        Entry baseGroup = ((urlEntry.baseGroupId.length() == 0)
                           ? null
                           : getEntryManager().findGroup(null,
                               urlEntry.baseGroupId));
        Entry entry = processUrl(urlEntry.url, urlEntry.name,
                                 urlEntry.description, baseGroup,
                                 urlEntry.group);
        if (entry != null) {
            entries.add(entry);
            if (statusMessages.size() > 100) {
                statusMessages = new ArrayList<String>();
            }
            String crumbs = getEntryManager().getBreadCrumbs(null, entry,
                                true)[1];
            crumbs = crumbs.replace("class=", "xclass=");
            statusMessages.add(crumbs);
            entryCnt++;
        }
        return true;
    }



    /**
     * _more_
     *
     * @param name _more_
     * @param desc _more_
     * @param baseGroup _more_
     * @param groupName _more_
     *
     * @param url _more_
     *
     * @return _more_
     *
     * @throws Exception _more_
     */
    private Entry processUrl(String url, String name, String desc,
                             Entry baseGroup, String groupName)
            throws Exception {


        String fileName = url;
        String tail     = IOUtil.getFileTail(url);
        File   tmpFile  = getStorageManager().getTmpFile(null, tail);
        //System.err.println ("WebHarvester: " + getName() +" fetching URL: " + url);

        try {
            IOUtil.writeTo(new URL(url), tmpFile, null);
        } catch (Exception exc) {
            statusMessages.add("Unable to fetch URL: " + url);
            return null;
        }

        File newFile = getStorageManager().moveToStorage(null, tmpFile);
        //                           getRepository().getGUID() + "_");
        //        System.err.println ("got it " + newFile);
        String tag        = tagTemplate;

        Date   createDate = new Date();
        Date   fromDate   = createDate;
        Date   toDate     = createDate;
        String ext        = IOUtil.getFileExtension(url);
        tag = tag.replace("${extension}", ext);

        //        groupName = groupName.replace("${dirgroup}", dirGroup);


        groupName = applyMacros(groupName, createDate, fromDate, toDate,
                                fileName);
        name = applyMacros(name, createDate, fromDate, toDate, fileName);
        desc = applyMacros(desc, createDate, fromDate, toDate, fileName);


        Entry group = ((baseGroup != null)
                       ? getEntryManager().findGroupUnder(getRequest(),
                           baseGroup, groupName, getUser())
                       : getEntryManager().findGroupFromName(groupName,
                           getUser(), true));
        //        System.err.println("Group:" + group.getFullName());
        Entry entry = getTypeHandler().createEntry(repository.getGUID());
        Resource resource = new Resource(newFile.toString(),
                                         Resource.TYPE_STOREDFILE);


        //        System.err.println ("WebHarvester: " + getName() +" adding entry: " + name);
        entry.initEntry(name, desc, group, getUser(), resource, "",
                        createDate.getTime(), createDate.getTime(),
                        fromDate.getTime(), toDate.getTime(), null);
        if (tag.length() > 0) {
            List tags = StringUtil.split(tag, ",", true, true);
            for (int i = 0; i < tags.size(); i++) {
                entry.addMetadata(new Metadata(repository.getGUID(),
                        entry.getId(), EnumeratedMetadataHandler.TYPE_TAG,
                        DFLT_INHERITED, (String) tags.get(i),
                        Metadata.DFLT_ATTR, Metadata.DFLT_ATTR,
                        Metadata.DFLT_ATTR, Metadata.DFLT_EXTRA));
            }

        }
        getTypeHandler().initializeNewEntry(entry);
        return entry;

    }



}
