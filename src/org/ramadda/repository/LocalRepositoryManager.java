/*
* Copyright 2008-2013 Geode Systems LLC
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

package org.ramadda.repository;


import org.ramadda.repository.admin.Admin;
import org.ramadda.repository.auth.SessionManager;
import org.ramadda.repository.auth.User;
import org.ramadda.repository.auth.UserManager;
import org.ramadda.repository.database.DatabaseManager;
import org.ramadda.repository.database.Tables;
import org.ramadda.sql.Clause;
import org.ramadda.sql.SqlUtil;
import org.ramadda.util.HtmlUtils;


import org.ramadda.util.MyTrace;


import org.ramadda.util.Utils;

import ucar.unidata.util.IOUtil;

import java.io.File;
import java.io.FileOutputStream;


import java.sql.ResultSet;
import java.sql.Statement;

import java.text.SimpleDateFormat;

import java.util.ArrayList;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Random;
import java.util.TimeZone;




/**
 */
public class LocalRepositoryManager extends RepositoryManager {


    /** _more_ */
    public static final String PROP_MASTER_ENABLED = "ramadda.master.enabled";

    /** _more_ */
    public static final String ARG_LOCAL_NEW = "local.new";

    /** _more_ */
    public static final String ARG_LOCAL_SURE = "local.sure";

    /** _more_ */
    public static final String ARG_LOCAL_NAME = "local.name";

    /** _more_ */
    public static final String ARG_LOCAL_CONTACT = "local.contact";

    /** _more_ */
    public static final String ARG_LOCAL_ID = "local.id";

    /** _more_ */
    public static final String ARG_LOCAL_ADMIN = "local.admin";

    /** _more_ */
    public static final String ARG_LOCAL_CHANGE = "local.change";

    /** _more_ */
    public static final String ARG_LOCAL_STATUS = "local.status";

    //    public static final String ARG_LOCAL_ = "local.id";

    /** _more_ */
    public static final String STATUS_ACTIVE = "active";

    /** _more_ */
    public static final String STATUS_STOPPED = "stopped";

    /** _more_ */
    public static final String STATUS_DELETED = "deleted";


    /*
     * Holds the currently running repositories
     */

    /** _more_ */
    private Hashtable<String, Repository> children = new Hashtable<String,
                                                         Repository>();

    /** _more_ */
    private List<String> childrenIds = new ArrayList<String>();


    /**
     * _more_
     *
     * @param repository _more_
     */
    public LocalRepositoryManager(Repository repository) {
        super(repository);
    }

    /**
     * _more_
     *
     * @throws Exception _more_
     */
    @Override
    public void shutdown() throws Exception {
        if (children == null) {
            return;
        }
        super.shutdown();
        for (String childId : childrenIds) {
            Repository childRepository = children.get(childId);
            childRepository.shutdown();
        }
        children = null;
    }



    /**
     * _more_
     *
     * @throws Exception _more_
     */
    public void initializeLocalRepositories() throws Exception {
        if ( !getRepository().isMaster()) {
            return;
        }
        for (Local local : readLocals()) {
            if (local.status.equals(STATUS_ACTIVE)) {
                Repository childRepository = startLocalRepository(local.id,
                                                 new Properties());
            }
        }
    }

    /**
     * _more_
     *
     * @return _more_
     *
     * @throws Exception _more_
     */
    private List<Local> readLocals() throws Exception {
        List<Local> locals = new ArrayList<Local>();
        Statement stmt =
            getDatabaseManager().select(Tables.LOCALREPOSITORIES.COLUMNS,
                                        Tables.LOCALREPOSITORIES.NAME,
                                        (Clause) null);
        SqlUtil.Iterator iter = getDatabaseManager().getIterator(stmt);
        ResultSet        results;
        while ((results = iter.getNext()) != null) {
            locals.add(new Local(results
                .getString(Tables.LOCALREPOSITORIES.COL_NODOT_ID), results
                .getString(Tables.LOCALREPOSITORIES.COL_NODOT_EMAIL), results
                .getString(Tables.LOCALREPOSITORIES.COL_NODOT_STATUS)));

        }

        return locals;
    }

    /**
     * _more_
     *
     * @param otherServer _more_
     *
     * @return _more_
     *
     * @throws Exception _more_
     */
    public boolean hasServer(String otherServer) throws Exception {
        return getDatabaseManager().tableContains(otherServer,
                Tables.LOCALREPOSITORIES.NAME,
                Tables.LOCALREPOSITORIES.COL_ID);
    }


    /**
     * _more_
     *
     * @param request _more_
     * @param sb _more_
     * @param repositoryId _more_
     *
     * @throws Exception _more_
     */
    public void addChildRepository(Request request, StringBuffer sb,
                                   String repositoryId)
            throws Exception {
        repositoryId = repositoryId.trim();

        //clean up the name
        repositoryId = repositoryId.replaceAll("[^a-zA-Z_0-9]+", "");
        if (repositoryId.length() == 0) {
            throw new IllegalArgumentException("Bad id:" + repositoryId);
        }
        if (hasServer(repositoryId)) {
            throw new IllegalArgumentException(
                "Already have a repository with id:" + repositoryId);
        }

        String password = request.getString(UserManager.ARG_USER_PASSWORD1,
                                            "").trim();
        if (password.length() == 0) {
            sb.append(
                getPageHandler().showDialogError("Password is required"));

            return;
        }


        String     name       = request.getString(ARG_LOCAL_NAME, "");
        String     contact = request.getString(ARG_LOCAL_CONTACT, "").trim();
        Properties properties = new Properties();
        properties.put(PROP_REPOSITORY_NAME, name);
        properties.put(PROP_MASTER_ENABLED, "" + true);
        File ramaddaHomeDir = getHomeDir(repositoryId);
        boolean existedBefore = new File(IOUtil.joinDir(ramaddaHomeDir,
                                    "storage")).exists();

        Repository childRepository = startLocalRepository(repositoryId,
                                         properties);
        childRepository.writeGlobal(Admin.ARG_ADMIN_INSTALLCOMPLETE, "true");
        childRepository.writeGlobal(PROP_HOSTNAME,
                                    getProperty(PROP_HOSTNAME, ""));
        childRepository.writeGlobal(PROP_PORT,
                                    "" + getRepository().getPort());
        childRepository.writeGlobal(PROP_ADMIN_SMTP,
                                    getProperty(PROP_ADMIN_SMTP, ""));
        childRepository.writeGlobal(PROP_ADMIN_EMAIL,
                                    getProperty(PROP_ADMIN_EMAIL, ""));


        StringBuffer msg            = new StringBuffer();
        String       childUrlPrefix = getChildUrlBase(repositoryId);

        msg.append("Created repository: "
                   + HtmlUtils.href(childUrlPrefix, childUrlPrefix));
        msg.append("<br>");

        String adminId = request.getString(ARG_LOCAL_ADMIN, "").trim();
        if (adminId.length() == 0) {
            adminId = repositoryId + "_admin";
        }

        User user = childRepository.getUserManager().findUser(adminId);
        if (user == null) {
            user = new User(
                adminId, "Administrator", "", "", "",
                childRepository.getUserManager().hashPassword(password),
                true, "", "", false, null);
            childRepository.getUserManager().makeOrUpdateUser(user, false);
            msg.append("Created admin: " + user.getId());
            msg.append(HtmlUtils.p());
            if ( !existedBefore) {
                childRepository.getAdmin().addInitEntries(user);
            }
        }

        if (msg.length() > 0) {
            sb.append(getPageHandler().showDialogNote(msg.toString()));
        }


        getDatabaseManager().executeInsert(Tables.LOCALREPOSITORIES.INSERT,
                                           new Object[] { repositoryId,
                contact, STATUS_ACTIVE });
        getRepository().addChildRepository(childRepository);
    }

    /**
     * _more_
     *
     * @param childId _more_
     *
     * @return _more_
     */
    private String getChildUrlBase(String childId) {
        return getRepository().getUrlBase() + "/repos/" + childId;
    }

    /**
     * _more_
     *
     * @param repositoryId _more_
     *
     * @return _more_
     *
     * @throws Exception _more_
     */
    private File getHomeDir(String repositoryId) throws Exception {
        String repositoriesDir = getProperty("ramadda.master.dir",
                                             "%repositorydir%/repositories");
        repositoriesDir = repositoriesDir.replace("%repositorydir%",
                getStorageManager().getRepositoryDir().toString());

        return new File(IOUtil.joinDir(repositoriesDir,
                                       "repository_" + repositoryId));
    }

    /**
     * _more_
     *
     * @param repositoryId _more_
     * @param properties _more_
     *
     * @return _more_
     *
     * @throws Exception _more_
     */
    private Repository startLocalRepository(String repositoryId,
                                            Properties properties)
            throws Exception {
        System.err.println("RAMADDA: starting local repository:"
                           + repositoryId);
        File ramaddaHomeDir = getHomeDir(repositoryId);
        ramaddaHomeDir.mkdirs();
        File otherPluginDir = new File(IOUtil.joinDir(ramaddaHomeDir,
                                  "plugins"));
        //TODO: Do we always copy the plugins on start up or just the first time
        //        if(!otherPluginDir.exists()) {
        otherPluginDir.mkdirs();
        for (File currentPluginFile : otherPluginDir.listFiles()) {
            currentPluginFile.delete();
        }

        for (File myPluginFile :
                getStorageManager().getPluginsDir().listFiles()) {
            if ( !myPluginFile.isFile()) {
                continue;
            }
            IOUtil.copyFile(myPluginFile, otherPluginDir);
        }
        //}

        //Write out a random seed and iteration if we haven't done so already
        File passwordPropertiesFile = new File(IOUtil.joinDir(ramaddaHomeDir,
                                          "password.properties"));
        if ( !passwordPropertiesFile.exists()) {
            StringBuffer propsSB = new StringBuffer();
            String alpha =
                "abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789";
            Random random = new Random();
            String seed1  = "";
            String seed2  = "";
            for (int i = 0; i < 20; i++) {
                seed1 += alpha.charAt(random.nextInt(alpha.length()));
                seed2 += alpha.charAt(random.nextInt(alpha.length()));
            }
            propsSB.append(
                "#generated password salts\n#do not change these or your passwords will be invalidated\n\n");
            propsSB.append(UserManager.PROP_PASSWORD_SALT1 + "=" + seed1
                           + "\n");
            propsSB.append(UserManager.PROP_PASSWORD_SALT2 + "=" + seed2
                           + "\n");
            propsSB.append(UserManager.PROP_PASSWORD_ITERATIONS + "="
                           + (500 + random.nextInt(200)));
            IOUtil.writeFile(passwordPropertiesFile, propsSB.toString());
        }

        //Copy the keystore and the ssl.properties??
        properties.put(PROP_HTML_URLBASE, getChildUrlBase(repositoryId));
        properties.put(PROP_REPOSITORY_HOME, ramaddaHomeDir.toString());

        //We let the children be masters for now
        properties.put(PROP_MASTER_ENABLED, "true");

        File propertiesFile = new File(IOUtil.joinDir(ramaddaHomeDir,
                                  "repository.properties"));
        properties.store(new FileOutputStream(propertiesFile),
                         "Generated by RAMADDA and company");
        Repository childRepository = new Repository(getRepository(),
                                         new String[] {},
                                         getRepository().getPort());
        childRepository.init(properties);
        getRepository().addChildRepository(childRepository);
        children.put(repositoryId, childRepository);
        childrenIds.add(repositoryId);

        return childRepository;
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
    public Result handleRepos(Request request) throws Exception {
        if ( !getRepository().isMaster()) {
            throw new IllegalArgumentException("Not a master repo");
        }
        String path = request.getRequestPath();
        //        child:/repository/repos/testitout5/:
        //        child:/repository/repos/test6/:

        Repository theRepository = null;
        //        System.err.println("path:" + path+":");
        for (Repository childRepository :
                getRepository().getChildRepositories()) {
            String childUrl = childRepository.getUrlBase() + "/";
            //            System.err.println ("child:" + childUrl+":");
            if (path.startsWith(childUrl)
                    || childRepository.getUrlBase().equals(path)) {
                Request originalRequest = request;
                request = request.cloneMe(childRepository);
                //                request.setRequestPath(suffix);
                request.setUser(null);
                request.setSessionId(null);
                //                System.err.println ("Dispatch:" + originalRequest + " " + originalRequest.getSecure() + " new:" +
                //                                    request + " "  + request.getSecure());
                //                System.err.println (getRepository().getUrlBase() + ": Local dispatching:" + request);
                Result result = childRepository.handleRequest(request);
                //                System.err.println (getRepository().getUrlBase() + ": done dispatching:" + request.getSessionId());

                result.setShouldDecorate(false);

                return result;
            }
        }


        StringBuffer sb = new StringBuffer();
        sb.append(
            getPageHandler().showDialogWarning(
                "Could not find requested repository.<br>Perhaps it hasn't started up yet"));

        return new Result("", sb);
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
    public Result adminLocal(Request request) throws Exception {

        if ( !getRepository().isMaster()) {
            throw new IllegalArgumentException("Not a master repo");
        }

        StringBuffer sb = new StringBuffer();

        if (request.defined(ARG_LOCAL_NEW)) {
            request.ensureAuthToken();
            if (request.get(ARG_LOCAL_SURE, false)) {
                processLocalNew(request, sb);
            } else {
                sb.append(
                    getPageHandler().showDialogWarning(
                        "You didn't select \"I'm sure\""));
            }
        }


        if (request.defined(ARG_LOCAL_CHANGE)) {
            request.ensureAuthToken();
            processLocalChange(request, sb);

        }

        sb.append(HtmlUtils.formTable(""));
        boolean didone = false;
        for (Local local : readLocals()) {
            if ( !didone) {
                sb.append(
                    HtmlUtils.row(
                        HtmlUtils.cols(
                            "<b>Repository</b>" /*,"<b>Contact</b>"*/,
                            "<b>Status</b>", "<b>Action</b>")));
            }
            didone = true;
            StringBuffer statusSB  = new StringBuffer();
            StringBuffer statusSB2 = new StringBuffer();

            request.formPostWithAuthToken(statusSB,
                                          getAdmin().URL_ADMIN_LOCAL);
            statusSB.append(HtmlUtils.hidden(ARG_LOCAL_ID, local.id));
            if (local.status.equals(STATUS_ACTIVE)) {
                statusSB.append(HtmlUtils.submit(msg("Stop Repository"),
                        ARG_LOCAL_CHANGE));
                statusSB.append(HtmlUtils.hidden(ARG_LOCAL_STATUS,
                        STATUS_STOPPED));
            } else if (local.status.equals(STATUS_STOPPED)) {
                statusSB.append(HtmlUtils.submit(msg("Start Repository"),
                        ARG_LOCAL_CHANGE));
                statusSB.append(HtmlUtils.hidden(ARG_LOCAL_STATUS,
                        STATUS_ACTIVE));
            }
            statusSB.append(HtmlUtils.formClose());

            if (local.status.equals(STATUS_STOPPED)) {
                request.formPostWithAuthToken(statusSB2,
                        getAdmin().URL_ADMIN_LOCAL);
                statusSB2.append(HtmlUtils.space(1));
                statusSB2.append(HtmlUtils.hidden(ARG_LOCAL_ID, local.id));
                statusSB2.append(HtmlUtils.hidden(ARG_LOCAL_STATUS,
                        STATUS_DELETED));
                statusSB2.append(HtmlUtils.submit(msg("Remove Repository"),
                        ARG_LOCAL_CHANGE));
                statusSB2.append(HtmlUtils.checkbox(ARG_LOCAL_SURE, "true",
                        false) + " " + msg("Yes, remove this repository"));
                statusSB2.append(HtmlUtils.formClose());
            }
            sb.append(
                HtmlUtils.rowTop(
                    HtmlUtils.cols(
                        HtmlUtils.href(
                            getChildUrlBase(local.id),
                            local.id) /*,contact*/, local.status,
                                statusSB.toString(), statusSB2.toString())));
        }
        sb.append(HtmlUtils.formTableClose());
        sb.append(HtmlUtils.p());

        request.formPostWithAuthToken(sb, getAdmin().URL_ADMIN_LOCAL);
        sb.append(formHeader(msg("New repository")));
        String required =
            " "
            + HtmlUtils.span("* required",
                             HtmlUtils.cssClass(CSS_CLASS_REQUIRED));
        sb.append(HtmlUtils.formTable());
        sb.append(HtmlUtils.formEntry(msgLabel("ID"),
                                      HtmlUtils.input(ARG_LOCAL_ID,
                                          request.getString(ARG_LOCAL_ID,
                                              "")) + required));
        sb.append(HtmlUtils.formEntry(msgLabel("Repository Name"),
                                      HtmlUtils.input(ARG_LOCAL_NAME,
                                          request.getString(ARG_LOCAL_NAME,
                                              ""))));
        sb.append(
            HtmlUtils.formEntry(
                msgLabel("Contact"),
                HtmlUtils.input(
                    ARG_LOCAL_CONTACT,
                    request.getString(ARG_LOCAL_CONTACT, ""))));

        sb.append(
            HtmlUtils.formEntry(
                msgLabel("Admin ID"),
                HtmlUtils.input(ARG_LOCAL_ADMIN)
                + " Default is &lt;repository id&gt;_admin"));
        sb.append(
            HtmlUtils.formEntry(
                msgLabel("Admin Password"),
                HtmlUtils.input(UserManager.ARG_USER_PASSWORD1) + required));

        sb.append(HtmlUtils.formEntry("",
                                      HtmlUtils.checkbox(ARG_LOCAL_SURE,
                                          "true", false) + " "
                                              + msg("I'm sure")));


        sb.append(
            HtmlUtils.formEntry(
                "",
                HtmlUtils.submit(
                    msg("Create new repository"), ARG_LOCAL_NEW)));

        sb.append(HtmlUtils.formTableClose());

        sb.append(HtmlUtils.formClose());


        return getAdmin().makeResult(request, "Administration", sb);

    }

    /**
     * _more_
     *
     * @param request _more_
     * @param sb _more_
     *
     * @throws Exception _more_
     */
    private void processLocalChange(Request request, StringBuffer sb)
            throws Exception {
        String status = request.getString(ARG_LOCAL_STATUS, "");
        String id     = request.getString(ARG_LOCAL_ID, "");
        if (status.equals(STATUS_ACTIVE)) {
            Repository child = startLocalRepository(id, new Properties());
        } else if (status.equals(STATUS_STOPPED)) {
            Repository child = children.get(id);
            if (child == null) {
                sb.append(
                    getPageHandler().showDialogError(
                        "Could not find running server with id: " + id));

                return;
            }
            child.close();
            getRepository().removeChildRepository(child);
            children.remove(id);
            childrenIds.remove(id);
        } else if (status.equals(STATUS_DELETED)) {
            if ( !request.get(ARG_LOCAL_SURE, false)) {
                sb.append(
                    getPageHandler().showDialogError(
                        "Check on the 'Yes, remove this repository' box"));

                return;
            }
            //TODO: make sure status is stopped
            getDatabaseManager().delete(
                Tables.LOCALREPOSITORIES.NAME,
                Clause.eq(Tables.LOCALREPOSITORIES.COL_ID, id));

            return;
        } else {
            sb.append(getPageHandler().showDialogError("Unknown status:"
                    + status));

            return;
        }
        getDatabaseManager().update(
            Tables.LOCALREPOSITORIES.NAME, Tables.LOCALREPOSITORIES.COL_ID,
            id, new String[] { Tables.LOCALREPOSITORIES.COL_STATUS },
            new String[] { status });
    }

    /**
     * _more_
     *
     * @param request _more_
     * @param sb _more_
     *
     * @throws Exception _more_
     */
    private void processLocalNew(Request request, StringBuffer sb)
            throws Exception {
        String id = request.getString(ARG_LOCAL_ID, "");
        if ( !Utils.stringDefined(id)) {
            sb.append(getPageHandler().showDialogError("No ID given"));

            return;
        }

        if (hasServer(id)) {
            sb.append(
                getPageHandler().showDialogError(
                    "Server with id already exists"));

            return;
        }
        try {
            addChildRepository(request, sb, id);
        } catch (Exception exc) {
            sb.append(getPageHandler().showDialogError("Error:" + exc));
            logError("adding repository:" + id, exc);
        }

    }


    /**
     * Class description
     *
     *
     * @version        $version$, Fri, Aug 23, '13
     * @author         Enter your name here...
     */
    private static class Local {

        /** _more_ */
        String id;

        /** _more_ */
        String contact;

        /** _more_ */
        String status;

        /**
         * _more_
         *
         * @param id _more_
         * @param contact _more_
         * @param status _more_
         */
        public Local(String id, String contact, String status) {
            this.id      = id;
            this.contact = contact;
            this.status  = status;
        }
    }


}
