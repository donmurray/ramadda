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

package org.ramadda.repository.harvester;



import org.ramadda.repository.*;
import org.ramadda.repository.auth.*;
import org.ramadda.repository.database.*;
import org.ramadda.repository.type.*;

import org.ramadda.util.HtmlUtils;


import org.w3c.dom.*;

import ucar.unidata.sql.Clause;

import ucar.unidata.sql.SqlUtil;
import ucar.unidata.util.IOUtil;
import ucar.unidata.util.LogUtil;
import ucar.unidata.util.Misc;



import ucar.unidata.util.StringUtil;
import ucar.unidata.util.TwoFacedObject;


import ucar.unidata.xml.XmlUtil;

import java.io.File;

import java.lang.reflect.*;

import java.sql.ResultSet;


import java.util.ArrayList;
import java.util.Date;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.List;
import java.util.Properties;



/**
 *
 *
 *
 * @author RAMADDA Development Team
 * @version $Revision: 1.3 $
 */
public class HarvesterManager extends RepositoryManager {


    /** _more_ */
    public RequestUrl URL_HARVESTERS_LIST = new RequestUrl(this,
                                                "/harvester/list",
                                                "Harvesters");

    /** _more_ */
    public RequestUrl URL_HARVESTERS_NEW = new RequestUrl(this,
                                               "/harvester/new");


    /** _more_ */
    public RequestUrl URL_HARVESTERS_FORM = new RequestUrl(this,
                                                "/harvester/form");

    /** _more_ */
    public RequestUrl URL_HARVESTERS_CHANGE = new RequestUrl(this,
                                                  "/harvester/change");


    /** _more_ */
    private List<Harvester> harvesters = new ArrayList();

    /** _more_ */
    private Hashtable harvesterMap = new Hashtable();

    /** _more_ */
    List<TwoFacedObject> harvesterTypes = new ArrayList<TwoFacedObject>();


    /**
     * _more_
     *
     * @param repository _more_
     */
    public HarvesterManager(Repository repository) {
        super(repository);
        addHarvesterType(PatternHarvester.class);
        addHarvesterType(WebHarvester.class);
        addHarvesterType(DirectoryHarvester.class);
    }


    /**
     * _more_
     *
     * @param c _more_
     */
    public void addHarvesterType(Class c) {
        try {
            Constructor ctor = Misc.findConstructor(c,
                                   new Class[] { Repository.class,
                    String.class });
            if (ctor == null) {
                throw new IllegalArgumentException(
                    "Could not find constructor for harvester:"
                    + c.getName());
            }
            Harvester dummy = (Harvester) ctor.newInstance(new Object[] {
                                  getRepository(),
                                  "" });
            harvesterTypes.add(new TwoFacedObject(dummy.getDescription(),
                    c.getName()));
        } catch (Exception exc) {
            logError("Error creating harvester: " + c.getName(), exc);
        }
    }






    /**
     * _more_
     *
     * @param id _more_
     *
     * @return _more_
     */
    protected Harvester findHarvester(String id) {
        if (id == null) {
            return null;
        }
        for (Harvester harvester : harvesters) {
            if (harvester.getId().equals(id)) {
                return harvester;
            }
        }

        return null;
    }


    /**
     * _more_
     *
     * @return _more_
     */
    public List<Harvester> getHarvesters() {
        return harvesters;
    }




    /**
     * This starts up the harvesters in a thread
     *
     * @throws Exception On badness
     */
    public void initHarvesters() throws Exception {
        //If we are in read only mode then don't start the harvesters
        if (getRepository().isReadOnly()) {
            return;
        }
        Misc.run(this, "initHarvestersInThread");
    }

    /**
     * _more_
     *
     * @throws Exception _more_
     */
    public void initHarvestersInThread() throws Exception {
        List<String> harvesterFiles =
            getRepository().getResourcePaths(PROP_HARVESTERS);
        boolean okToStart =
            getRepository().getProperty(PROP_HARVESTERS_ACTIVE, true);

        harvesters = new ArrayList<Harvester>();


        SqlUtil.Iterator iter = getDatabaseManager().getIterator(
                                    getDatabaseManager().select(
                                        Tables.HARVESTERS.COLUMNS,
                                        Tables.HARVESTERS.NAME,
                                        new Clause()));;
        ResultSet results;
        while ((results = iter.getNext()) != null) {
            String id        = results.getString(1);
            String className = results.getString(2);
            String content   = results.getString(3);

            Class  c         = null;

            //Hack, hack... 
            //handle package changes 
            className = className.replace("ucar.unidata.repository",
                                          "org.ramadda.repository");
            className = className.replace("ucar/unidata/repository",
                                          "org/ramadda/repository");

            className = className.replace("org.ramadda.geodata.data",
                                          "org.ramadda.geodata.cdmdata");
            className = className.replace("org/ramadda/geodata/data",
                                          "org/ramadda/geodata/cdmdata");
            try {
                c = Misc.findClass(className);
            } catch (ClassNotFoundException cnfe1) {
                className = className.replace("repository.",
                        "repository.harvester.");
                try {
                    c = Misc.findClass(className);
                } catch (ClassNotFoundException cnfe2) {
                    getRepository().getLogManager().logError(
                        "HarvesterManager: Could not load harvester class: "
                        + className);

                    continue;
                }
            }
            Constructor ctor = Misc.findConstructor(c,
                                   new Class[] { Repository.class,
                    String.class });
            Harvester harvester = (Harvester) ctor.newInstance(new Object[] {
                                      getRepository(),
                                      id });

            harvester.initFromContent(content);
            harvesters.add(harvester);
            harvesterMap.put(harvester.getId(), harvester);
        }



        try {
            for (String file : harvesterFiles) {
                Element root = XmlUtil.getRoot(file, getClass());
                if (root == null) {
                    continue;
                }
                try {
                    List<Harvester> newHarvesters =
                        Harvester.createHarvesters(getRepository(), root);
                    harvesters.addAll(newHarvesters);
                    for (Harvester harvester : newHarvesters) {
                        harvesterMap.put(harvester.getId(), harvester);
                    }
                } catch (Exception exc) {
                    logError("Error loading harvester file:" + file, exc);
                }
            }
        } catch (Exception exc) {
            logError("Error loading harvester file", exc);
        }


        for (Harvester harvester : harvesters) {
            for (File rootDir : harvester.getRootDirs()) {
                getStorageManager().addOkToReadFromDirectory(rootDir);
            }
            if ( !okToStart) {
                harvester.setActive(false);
            } else if (harvester.getActiveOnStart()) {
                Misc.run(harvester, "run");
            }
        }
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
    public Result processNew(Request request) throws Exception {

        StringBuffer sb = new StringBuffer();
        if (request.exists(ARG_CANCEL)) {
            return new Result(request.url(URL_HARVESTERS_LIST));
        }

        request.ensureAuthToken();
        List<Harvester> harvestersBeingCreated = new ArrayList<Harvester>();
        if (request.exists(ARG_HARVESTER_XMLFILE)) {
            String file = request.getUploadedFile(ARG_HARVESTER_XMLFILE);
            if ((file == null) || !new File(file).exists()) {
                return getAdmin().makeResult(
                    request, msg("New Harvester"),
                    new StringBuffer("You must specify a file"));
            }
            String xml = getStorageManager().readSystemResource(file);
            harvestersBeingCreated =
                Harvester.createHarvesters(getRepository(),
                                           XmlUtil.getRoot(xml));
            if (harvestersBeingCreated.size() == 0) {
                return getAdmin().makeResult(
                    request, msg("New Harvester"),
                    new StringBuffer("No harvesters defined"));
            }

            for (Harvester harvester : harvestersBeingCreated) {
                harvester.setId(getRepository().getGUID());
            }

        } else if (request.exists(ARG_NAME)) {
            String      id   = getRepository().getGUID();
            Class c = Misc.findClass(request.getString(ARG_HARVESTER_CLASS));
            Constructor ctor = Misc.findConstructor(c,
                                   new Class[] { Repository.class,
                    String.class });
            Harvester harvester = (Harvester) ctor.newInstance(new Object[] {
                                      getRepository(),
                                      id });
            harvester.setName(request.getString(ARG_NAME, ""));
            harvester.setUser(request.getUser());
            harvestersBeingCreated.add(harvester);
        }


        if (harvestersBeingCreated.size() > 0) {
            for (Harvester harvester : harvestersBeingCreated) {
                harvester.setIsEditable(true);
                harvesters.add(harvester);
                harvesterMap.put(harvester.getId(), harvester);

                getDatabaseManager().executeInsert(Tables.HARVESTERS.INSERT,
                        new Object[] { harvester.getId(),
                                       harvester.getClass().getName(),
                                       harvester.getContent() });
            }

            return new Result(
                request.url(
                    URL_HARVESTERS_FORM, ARG_HARVESTER_ID,
                    harvestersBeingCreated.get(0).getId()));
        }


        sb.append(RepositoryUtil.header("Create new harvester"));
        request.formPostWithAuthToken(sb, URL_HARVESTERS_NEW);
        sb.append(HtmlUtils.formTable());
        sb.append(HtmlUtils.formEntry(msgLabel("Name"),
                                      HtmlUtils.input(ARG_NAME, "",
                                          HtmlUtils.SIZE_40)));
        String typeInput = HtmlUtils.select(ARG_HARVESTER_CLASS,
                                            harvesterTypes);
        sb.append(HtmlUtils.formEntry(msgLabel("Type"), typeInput));
        sb.append(HtmlUtils.formEntry("",
                                      HtmlUtils.submit(msg("Create"))
                                      + HtmlUtils.space(1)
                                      + HtmlUtils.submit(msg("Cancel"),
                                          ARG_CANCEL)));

        sb.append(HtmlUtils.formClose());

        sb.append(HtmlUtils.formEntry(HtmlUtils.p(), ""));
        request.uploadFormWithAuthToken(sb, URL_HARVESTERS_NEW);

        sb.append(
            HtmlUtils.row(
                HtmlUtils.colspan(HtmlUtils.b("Or upload xml file"), 2)));
        sb.append(
            HtmlUtils.formEntry(
                msgLabel("File"),
                HtmlUtils.fileInput(
                    ARG_HARVESTER_XMLFILE, HtmlUtils.SIZE_70)));

        sb.append(HtmlUtils.formEntry("",
                                      HtmlUtils.submit(msg("Upload"))
                                      + HtmlUtils.space(1)
                                      + HtmlUtils.submit(msg("Cancel"),
                                          ARG_CANCEL)));
        sb.append(HtmlUtils.formClose());
        sb.append(HtmlUtils.formTableClose());

        return getAdmin().makeResult(request, msg("New Harvester"), sb);

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
    public Result processForm(Request request) throws Exception {
        StringBuffer sb        = new StringBuffer();
        Harvester    harvester =
            findHarvester(request.getString(ARG_HARVESTER_ID));
        if (harvester == null) {
            throw new IllegalArgumentException("Could not find harvester");
        }

        if (request.get(ARG_HARVESTER_GETXML, false)) {
            String xml = harvester.getContent();
            xml = XmlUtil.tag(Harvester.TAG_HARVESTERS, "", xml);

            return new Result("",
                              new StringBuffer(XmlUtil.getHeader() + "\n"
                                  + xml), "text/xml");
        }

        if ( !harvester.getIsEditable()) {
            throw new IllegalArgumentException("Cannot edit harvester");
        }
        if (request.exists(ARG_CANCEL)) {
            return new Result(request.url(URL_HARVESTERS_LIST));
        }

        makeFormHeader(request, harvester, sb);


        String xmlLink = HtmlUtils.href(
                             HtmlUtils.url(
                                 URL_HARVESTERS_FORM.toString()
                                 + "/harvester.xml", ARG_HARVESTER_GETXML,
                                     "true", ARG_HARVESTER_ID,
                                     harvester.getId()), msg("Download"));

        String buttons = HtmlUtils.submit(msg("Change"), ARG_CHANGE)
                         + HtmlUtils.space(1)
                         + HtmlUtils.submit(msg("Delete"), ARG_DELETE)
                         + HtmlUtils.space(1)
                         + HtmlUtils.submit(msg("Cancel"), ARG_CANCEL);



        sb.append(buttons);
        sb.append(HtmlUtils.space(2));
        sb.append(xmlLink);
        StringBuffer formSB = new StringBuffer();
        formSB.append(HtmlUtils.formTable());
        harvester.createEditForm(request, formSB);
        formSB.append(HtmlUtils.formTableClose());

        sb.append(formSB);
        sb.append(buttons);
        sb.append(HtmlUtils.formClose());

        return getAdmin().makeResult(request, msg("Edit Harvester"), sb);
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
    public Result processChange(Request request) throws Exception {
        Harvester harvester =
            findHarvester(request.getString(ARG_HARVESTER_ID));
        if (harvester == null) {
            throw new IllegalArgumentException("Could not find harvester");
        }
        request.ensureAuthToken();
        if ( !harvester.getIsEditable()) {
            throw new IllegalArgumentException("Cannot edit harvester");
        }
        if (request.exists(ARG_CANCEL)) {
            return new Result(request.url(URL_HARVESTERS_LIST));
        }

        if (request.exists(ARG_DELETE_CONFIRM)) {
            harvesterMap.remove(harvester.getId());
            harvesters.remove(harvester);
            getDatabaseManager().delete(Tables.HARVESTERS.NAME,
                                        Clause.eq(Tables.HARVESTERS.COL_ID,
                                            harvester.getId()));

            return new Result(request.url(URL_HARVESTERS_LIST));
        }

        if (request.exists(ARG_DELETE)) {
            StringBuffer sb = new StringBuffer();
            makeFormHeader(request, harvester, sb);
            sb.append(
                getRepository().showDialogQuestion(
                    msg("Are you sure you want to delete the harvester"),
                    RepositoryUtil.buttons(
                        HtmlUtils.submit(msg("Yes"), ARG_DELETE_CONFIRM),
                        HtmlUtils.submit(msg("Cancel"), ARG_CANCEL_DELETE))));
            sb.append(HtmlUtils.formClose());

            return getAdmin().makeResult(request, msg("Edit Harvester"), sb);
        }
        if (request.exists(ARG_CHANGE)) {
            harvester.applyEditForm(request);
            getDatabaseManager().delete(Tables.HARVESTERS.NAME,
                                        Clause.eq(Tables.HARVESTERS.COL_ID,
                                            harvester.getId()));
            getDatabaseManager().executeInsert(Tables.HARVESTERS.INSERT,
                    new Object[] { harvester.getId(),
                                   harvester.getClass().getName(),
                                   harvester.getContent() });

        }

        return new Result(request.url(URL_HARVESTERS_FORM, ARG_HARVESTER_ID,
                                      harvester.getId()));
    }


    /**
     * _more_
     *
     * @param request _more_
     * @param harvester _more_
     * @param sb _more_
     */
    private void makeFormHeader(Request request, Harvester harvester,
                                StringBuffer sb) {
        sb.append(header(msgLabel("Harvester") + harvester.getName()));
        request.formPostWithAuthToken(sb, URL_HARVESTERS_CHANGE);
        sb.append(HtmlUtils.hidden(ARG_HARVESTER_ID, harvester.getId()));
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
    public Result processList(Request request) throws Exception {
        StringBuffer sb = new StringBuffer();
        if (request.defined(ARG_ACTION)) {
            String    action    = request.getString(ARG_ACTION);
            Harvester harvester =
                findHarvester(request.getString(ARG_HARVESTER_ID));
            if (action.equals(ACTION_STOP)) {
                harvester.setActive(false);
            } else if (action.equals(ACTION_REMOVE)) {
                harvester.setActive(false);
                harvesters.remove(harvester);
            } else if (action.equals(ACTION_START)) {
                if ( !harvester.getActive()) {
                    getEntryManager().clearSeenResources();
                    harvester.clearCache();
                    harvester.setActive(true);
                    Misc.run(harvester, "run");
                }
            }
            if (request.get(ARG_HARVESTER_REDIRECTTOEDIT, false)) {
                return new Result(request.url(URL_HARVESTERS_FORM,
                        ARG_HARVESTER_ID, harvester.getId()));
            }

            return new Result(request.url(URL_HARVESTERS_LIST));
        }

        //        sb.append(msgHeader("Harvesters"));
        request.formPostWithAuthToken(sb, URL_HARVESTERS_NEW);
        sb.append(HtmlUtils.submit(msg("New Harvester")));
        sb.append(HtmlUtils.formClose());

        if (request.exists(ARG_MESSAGE)) {
            sb.append(
                getRepository().showDialogNote(
                    request.getString(ARG_MESSAGE, "")));
        }
        makeHarvestersList(request, harvesters, sb);

        return getAdmin().makeResult(request, msg("Harvesters"), sb);
    }


    /**
     * _more_
     *
     * @param request _more_
     * @param harvesters _more_
     * @param sb _more_
     *
     * @throws Exception _more_
     */
    private void makeHarvestersList(Request request,
                                    List<Harvester> harvesters,
                                    StringBuffer sb)
            throws Exception {
        sb.append(HtmlUtils.p());
        //        sb.append(HtmlUtils.formTable());
        sb.append("<table cellpadding=4 cellspacing=0>");
        sb.append(HtmlUtils.row(HtmlUtils.cols("",
                HtmlUtils.bold(msg("Name")), HtmlUtils.bold(msg("State")),
                HtmlUtils.bold(msg("Action")), "", "")));

        int cnt = 0;
        for (Harvester harvester : harvesters) {
            String removeLink =
                HtmlUtils.href(request.url(URL_HARVESTERS_LIST, ARG_ACTION,
                                           ACTION_REMOVE, ARG_HARVESTER_ID,
                                           harvester.getId()), msg("Remove"));
            if (harvester.getIsEditable()) {
                removeLink = "";
            }

            String edit = "&nbsp;";
            if (harvester.getIsEditable()) {
                edit = HtmlUtils
                    .href(request
                        .url(URL_HARVESTERS_FORM, ARG_HARVESTER_ID,
                             harvester.getId()), HtmlUtils
                                 .img(getRepository().iconUrl(ICON_EDIT),
                                      msg("Edit")));
            }
            cnt++;
            String rowAttributes = HtmlUtils.attr(HtmlUtils.ATTR_VALIGN,
                                       HtmlUtils.VALUE_TOP);

            if (harvester.getActive()) {
                rowAttributes +=
                    HtmlUtils.cssClass(CSS_CLASS_HARVESTER_ACTIVE);
            }

            StringBuffer info  = new StringBuffer();
            StringBuffer error = harvester.getError();
            if ((error != null) && (error.length() > 0)) {
                info.append(HtmlUtils.b(msg("Errors")));
                info.append("<div class=\"error-list\"><pre>" + error
                            + "</div></pre>");
            }
            info.append(harvester.getExtraInfo());
            sb.append(HtmlUtils.tag(HtmlUtils.TAG_TR, rowAttributes,
                                    HtmlUtils.cols(edit, harvester.getName(),
                                        (harvester.getActive()
                                         ? HtmlUtils.bold(msg("Active"))
                                         : msg("Stopped")) + HtmlUtils.space(
                                             2), harvester.getRunLink(
                                             request, false), removeLink,
                                                 info.toString())));
        }
        sb.append(HtmlUtils.formTableClose());


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
    public Result processFile(Request request) throws Exception {
        List<Harvester> harvesters  = getHarvesters();
        TypeHandler     typeHandler = getRepository().getTypeHandler(request);
        String          filepath    = request.getUnsafeString(ARG_FILE, BLANK);
        //Check to  make sure we can access this file
        if ( !getStorageManager().isInDownloadArea(new File(filepath))) {
            return new Result(BLANK,
                              new StringBuffer("Cannot load file:"
                                  + filepath), "text/plain");
        }
        for (Harvester harvester : harvesters) {
            Entry entry = harvester.processFile(typeHandler, filepath);
            if (entry != null) {
                getEntryManager().addNewEntry(entry);

                return new Result(BLANK, new StringBuffer("OK"),
                                  "text/plain");
            }
        }

        return new Result(BLANK, new StringBuffer("Could not create entry"),
                          "text/plain");
    }






}
