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

package org.ramadda.repository;


import org.ramadda.repository.util.ServerInfo;

import org.ramadda.repository.database.*;


import org.w3c.dom.*;

import ucar.unidata.sql.Clause;

import ucar.unidata.sql.SqlUtil;

import ucar.unidata.ui.ImageUtils;
import ucar.unidata.util.DateUtil;
import org.ramadda.util.HtmlUtils;
import ucar.unidata.util.IOUtil;
import ucar.unidata.util.LogUtil;
import ucar.unidata.util.Misc;


import ucar.unidata.util.StringUtil;
import ucar.unidata.util.TwoFacedObject;

import ucar.unidata.xml.XmlUtil;



import java.io.*;

import java.io.File;

import java.lang.reflect.*;



import java.net.*;



import java.sql.ResultSet;
import java.sql.Statement;

import java.text.SimpleDateFormat;

import java.util.ArrayList;
import java.util.Date;
import java.util.Enumeration;
import java.util.GregorianCalendar;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.List;
import java.util.Properties;
import java.util.TimeZone;

import java.util.regex.*;
import java.util.zip.*;




/**
 *
 *
 * @author RAMADDA Development Team
 * @version $Revision: 1.3 $
 */
public class RegistryManager extends RepositoryManager {


    /** _more_ */
    private Object REMOTE_MUTEX = new Object();

    /** _more_ */
    public static final String ARG_REGISTRY_RELOAD = "registry.reload";

    /** _more_ */
    public static final String ARG_REGISTRY_SERVER = "registry.server";

    /** _more_ */
    public static final String ARG_REGISTRY_SELECTED = "registry.selected";

    /** _more_ */
    public static final String ARG_REGISTRY_CLIENT = "registry.client";

    /** _more_ */
    public static final String ARG_REGISTRY_ADD = "registry.add";

    /** _more_ */
    public static final String ARG_REGISTRY_URL = "registry.url";


    /** _more_ */
    public RequestUrl URL_REGISTRY_REMOTESERVERS =
        new RequestUrl(this, "/admin/remoteservers", "Remote Servers");



    /** _more_ */
    private List<ServerInfo> registeredServers;

    /** _more_ */
    private List<ServerInfo> remoteServers;

    /** _more_ */
    private List<ServerInfo> selectedRemoteServers;


    /** _more_ */
    private Hashtable<String, ServerInfo> remoteServerMap;


    /**
     * _more_
     *
     *
     * @param repository _more_
     *
     */
    public RegistryManager(Repository repository) {
        super(repository);
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
    public Result processAdminRemoteServers(Request request)
            throws Exception {

        StringBuffer sb = new StringBuffer();
        if (request.exists(ARG_REGISTRY_ADD)) {
            if (request.defined(ARG_REGISTRY_URL)) {
                String url = request.getString(ARG_REGISTRY_URL, "");
                URL fullUrl =
                    new URL(
                        HtmlUtils.url(
                            url + getRepository().URL_INFO.getPath(),
                            new String[] { ARG_RESPONSE,
                                           RESPONSE_XML }));

                try {
                    String contents = getStorageManager().readSystemResource(
                                          fullUrl.toString());
                    Element root = XmlUtil.getRoot(contents);
                    if ( !responseOk(root)) {
                        sb.append(
                            getRepository().showDialogError(
                                "Failed to read information from:"
                                + fullUrl));
                    } else {
                        ServerInfo serverInfo = new ServerInfo(root);
                        serverInfo.setSelected(true);
                        addRemoteServer(serverInfo, true);
                        return new Result(
                            request.url(URL_REGISTRY_REMOTESERVERS));
                    }
                } catch (Exception exc) {
                    sb.append(
                        getRepository().showDialogError(
                            "Failed to read information from:" + fullUrl));
                }
            }

            sb.append(request.form(URL_REGISTRY_REMOTESERVERS, ""));
            sb.append(HtmlUtils.formTable());
            sb.append(HtmlUtils.p());

            sb.append(
                HtmlUtils.formEntry(
                    msgLabel("URL"),
                    HtmlUtils.input(
                        ARG_REGISTRY_URL,
                        request.getString(ARG_REGISTRY_URL, ""),
                        HtmlUtils.SIZE_60)));
            sb.append(HtmlUtils.formTableClose());
            sb.append(HtmlUtils.submit("Add New Server", ARG_REGISTRY_ADD));
            sb.append(HtmlUtils.submit("Cancel", ARG_CANCEL));
            sb.append(HtmlUtils.formClose());
            return getAdmin().makeResult(request, msg("Remote Servers"), sb);

        } else if (request.exists(ARG_REGISTRY_RELOAD)) {
            for (String server : getServersToRegisterWith()) {
                fetchRemoteServers(server);
            }
            checkApi();
        } else if (request.exists(ARG_SUBMIT)) {
            List allIds = request.get(ARG_REGISTRY_SERVER, new ArrayList());
            List selectedIds = request.get(ARG_REGISTRY_SELECTED,
                                           new ArrayList());
            Hashtable selected = new Hashtable();
            for (String id : (List<String>) selectedIds) {
                selected.put(id, id);
            }

            for (String id : (List<String>) allIds) {
                boolean isSelected = (selected.get(id) != null);
                getDatabaseManager().update(
                    Tables.REMOTESERVERS.NAME, Tables.REMOTESERVERS.COL_URL,
                    id, new String[] { Tables.REMOTESERVERS.COL_SELECTED },
                    new Object[] { new Boolean(isSelected) });

            }
            clearRemoteServers();
            checkApi();
        } else if (request.exists(ARG_DELETE)) {
            List selectedIds = request.get(ARG_REGISTRY_SELECTED,
                                           new ArrayList());
            for (String id : (List<String>) selectedIds) {
                getDatabaseManager().delete(
                    Tables.REMOTESERVERS.NAME,
                    Clause.eq(Tables.REMOTESERVERS.COL_URL, id));
            }
            clearRemoteServers();
            checkApi();
        }


        sb.append(HtmlUtils.p());
        sb.append(request.form(URL_REGISTRY_REMOTESERVERS, ""));
        sb.append(HtmlUtils.submit(msg("Change Selected"), ARG_SUBMIT));
        sb.append(HtmlUtils.submit(msg("Delete Selected"), ARG_DELETE));
        sb.append(HtmlUtils.submit(msg("Add New Server"), ARG_REGISTRY_ADD));
        sb.append(HtmlUtils.submit(msg("Reload from Registry Servers"),
                                  ARG_REGISTRY_RELOAD));
        sb.append(HtmlUtils.open(HtmlUtils.TAG_UL));
        List<ServerInfo> remoteServers = getRemoteServers();
        if (remoteServers.size() == 0) {
            sb.append(msg("No remote servers"));
        } else {
            sb.append(msg("Use selected in search"));
        }
        sb.append(HtmlUtils.br());

        sb.append(HtmlUtils.open(HtmlUtils.TAG_TABLE));
        int idCnt = 0;

        for (ServerInfo serverInfo : remoteServers) {
            sb.append(HtmlUtils.hidden(ARG_REGISTRY_SERVER,
                                      serverInfo.getId()));
            String cbxId = ARG_REGISTRY_SELECTED + "_" + (idCnt++);
            String call = HtmlUtils.attr(
                              HtmlUtils.ATTR_ONCLICK,
                              HtmlUtils.call(
                                  "checkboxClicked",
                                  HtmlUtils.comma(
                                      "event",
                                      HtmlUtils.squote(ARG_REGISTRY_SELECTED),
                                      HtmlUtils.squote(cbxId))));


            sb.append(
                HtmlUtils.row(
                    HtmlUtils.cols(
                        HtmlUtils.checkbox(
                            ARG_REGISTRY_SELECTED, serverInfo.getId(),
                            serverInfo.getSelected(),
                            HtmlUtils.id(cbxId)
                            + call) + serverInfo.getLabel(), HtmlUtils.space(
                                4), HtmlUtils.href(
                                serverInfo.getUrl(), serverInfo.getUrl()))));
        }

        sb.append(HtmlUtils.close(HtmlUtils.TAG_TABLE));
        sb.append(HtmlUtils.close(HtmlUtils.TAG_UL));
        sb.append(HtmlUtils.submit("Change Selected", ARG_SUBMIT));
        sb.append(HtmlUtils.submit("Delete Selected", ARG_DELETE));
        sb.append(HtmlUtils.submit("Add New Server", ARG_REGISTRY_ADD));
        sb.append(HtmlUtils.submit("Reload from Registry Servers",
                                  ARG_REGISTRY_RELOAD));
        sb.append(HtmlUtils.formClose());


        return getAdmin().makeResult(request, msg("Remote Servers"), sb);

    }


    /**
     * _more_
     *
     * @param request _more_
     * @param sb _more_
     *
     * @throws Exception _more_
     */
    public void addToInstallForm(Request request, StringBuffer sb)
            throws Exception {
        if (true) {
            return;
        }
        String msg = msgLabel("Servers this server registers with");
        msg = HtmlUtils.space(1)
              + HtmlUtils.href(getRepository().getUrlBase()
                              + "/userguide/remoteservers.html", msg("Help"),
                                  HtmlUtils.attr(HtmlUtils.ATTR_TARGET,
                                      "_help"));
        sb.append(
            HtmlUtils.formEntry(
                msgLabel("Registry Servers"),
                msg + HtmlUtils.br()
                + HtmlUtils.textArea(
                    PROP_REGISTRY_SERVERS,
                    getRepository().getProperty(
                        PROP_REGISTRY_SERVERS,
                        getRepository().getProperty(
                            PROP_REGISTRY_DEFAULTSERVER,
                            "http://motherlode.ucar.edu/repository")), 5,
                                60)));
    }

    /**
     * _more_
     *
     * @param request _more_
     *
     * @throws Exception _more_
     */
    public void applyInstallForm(Request request) throws Exception {
        List<String> newList =
            StringUtil.split(request.getUnsafeString(PROP_REGISTRY_SERVERS,
                ""), "\n", true, true);


        getRepository().writeGlobal(PROP_REGISTRY_SERVERS,
                                    StringUtil.join("\n", newList));
    }

    /**
     * _more_
     *
     * @throws Exception _more_
     */
    public void doFinalInitialization() throws Exception {
        //        Misc.printStack("doFinal");
        if (isEnabledAsServer()) {
            Misc.run(new Runnable() {
                public void run() {
                    while (true) {
                        //Every one hour clean up the server collection
                        cleanupServers();
                        Misc.sleep(DateUtil.hoursToMillis(1));
                        //                        Misc.sleep(10000);
                    }
                }
            });
        }


        Misc.runInABit(5000, new Runnable() {
            public void run() {
                doFinalInitializationInner();
            }
        });

    }


    /**
     * _more_
     */
    private void cleanupServers() {
        if ( !isEnabledAsServer()) {
            return;
        }
        try {
            List<ServerInfo> servers = getRegisteredServers();
            for (ServerInfo serverInfo : servers) {
                checkServer(serverInfo, true);
            }
        } catch (Exception exc) {
            logError("RegistryManager.cleanUpServers:", exc);
        }


    }


    /**
     * _more_
     */
    public void doFinalInitializationInner() {
        try {
            registerWithServers();
        } catch (Exception exc) {
            logError(
                "RegistryManager.doFinalInitialization: Registering with servers",
                exc);
        }
    }





    /**
     * _more_
     *
     * @throws Exception _more_
     */
    public void checkApi() throws Exception {
        ApiMethod apiMethod = getRepository().getApiMethod("/registry/list");
        if (apiMethod != null) {
            apiMethod.setIsTopLevel(
                (isEnabledAsServer() && (getRegisteredServers().size() > 0))
                || (getSelectedRemoteServers().size() > 0));
        }

    }


    /**
     * _more_
     *
     * @param request _more_
     * @param csb _more_
     */
    public void addAdminConfig(Request request, StringBuffer csb) {
        String helpLink =
            HtmlUtils.href(getRepository().getUrlBase()
                          + "/userguide/remoteservers.html", msg("Help"),
                              HtmlUtils.attr(HtmlUtils.ATTR_TARGET, "_help"));
        csb.append(
            HtmlUtils.row(HtmlUtils.colspan(msgHeader("Server Registry"), 2)));

        csb.append(
            HtmlUtils.formEntry(
                "",
                HtmlUtils.checkbox(
                    PROP_REGISTRY_ENABLED, "true", isEnabledAsServer()) + " "
                        + msg("Enable this server to be a registry for other servers")));

        csb.append(
            HtmlUtils.formEntry(
                "",
                msgLabel("Servers this server registers with")
                + HtmlUtils.space(2) + helpLink));
        csb.append(
            HtmlUtils.formEntry(
                "",
                HtmlUtils.textArea(
                    PROP_REGISTRY_SERVERS,
                    getRepository().getProperty(
                        PROP_REGISTRY_SERVERS,
                        "http://motherlode.ucar.edu/repository"), 5, 60)));

    }



    /**
     * _more_
     *
     * @param request _more_
     *
     * @throws Exception _more_
     */
    public void applyAdminConfig(Request request) throws Exception {
        List<String> oldList = getServersToRegisterWith();
        List<String> newList =
            StringUtil.split(request.getUnsafeString(PROP_REGISTRY_SERVERS,
                ""), "\n", true, true);


        getRepository().writeGlobal(PROP_REGISTRY_SERVERS,
                                    StringUtil.join("\n", newList));
        getRepository().writeGlobal(PROP_REGISTRY_ENABLED,
                                    request.get(PROP_REGISTRY_ENABLED, false)
                                    + "");
        checkApi();
        if ( !newList.equals(oldList)) {
            for (String url : oldList) {
                newList.remove(url);
                Misc.run(this, "registerWithServer", url);
            }
            for (String url : newList) {
                Misc.run(this, "registerWithServer", url);
            }
        }
    }


    /**
     * _more_
     *
     * @return _more_
     */
    public boolean isEnabledAsServer() {
        return getRepository().getProperty(PROP_REGISTRY_ENABLED, false);
    }

    /**
     * _more_
     *
     * @return _more_
     *
     * @throws Exception _more_
     */
    public List<ServerInfo> getRegisteredServers() throws Exception {
        List<ServerInfo> servers = registeredServers;
        if (servers != null) {
            return servers;
        }
        servers = new ArrayList<ServerInfo>();

        Statement stmt =
            getDatabaseManager().select(Tables.SERVERREGISTRY.COLUMNS,
                                        Tables.SERVERREGISTRY.NAME,
                                        (Clause) null,
                                        " order by "
                                        + Tables.SERVERREGISTRY.COL_URL);
        SqlUtil.Iterator iter     = getDatabaseManager().getIterator(stmt);
        List<Comment>    comments = new ArrayList();
        ResultSet        results;
        while ((results = iter.getNext()) != null) {
            URL     url        = new URL(results.getString(1));
            String  title      = results.getString(2);
            String  desc       = results.getString(3);
            String  email      = results.getString(4);
            boolean isRegistry = results.getInt(5) != 0;
            servers.add(new ServerInfo(url.getHost(), url.getPort(), -1,
                                       url.getPath(), title, desc, email,
                                       isRegistry, false));
        }
        registeredServers = servers;
        return servers;
    }



    /**
     * _more_
     */
    private void clearRegisteredServers() {
        registeredServers = null;
    }


    /**
     * _more_
     */
    private void clearRemoteServers() {
        selectedRemoteServers = null;
        remoteServers         = null;
        remoteServerMap       = null;

    }



    /**
     * _more_
     *
     * @param id _more_
     *
     * @return _more_
     *
     * @throws Exception _more_
     */
    public ServerInfo findRemoteServer(String id) throws Exception {
        if (id.equals(ServerInfo.ID_THIS)) {
            ServerInfo serverInfo = getRepository().getServerInfo();
            serverInfo.setSelected(true);
            return serverInfo;
        }
        return getRemoteServerMap().get(id);
    }


    /**
     * _more_
     *
     * @return _more_
     *
     * @throws Exception _more_
     */
    public List<ServerInfo> getSelectedRemoteServers() throws Exception {
        List<ServerInfo> selected = selectedRemoteServers;
        if (selected != null) {
            return selected;
        }
        selected = new ArrayList<ServerInfo>();
        for (ServerInfo serverInfo : getRemoteServers()) {
            if (serverInfo.getSelected()) {
                selected.add(serverInfo);
            }
        }
        selectedRemoteServers = selected;
        return selected;
    }


    /**
     * _more_
     *
     * @return _more_
     *
     * @throws Exception _more_
     */
    private Hashtable<String, ServerInfo> getRemoteServerMap()
            throws Exception {
        Hashtable<String, ServerInfo> map = remoteServerMap;
        while (map == null) {
            getRemoteServers();
            map = remoteServerMap;
        }
        return map;
    }



    /**
     * _more_
     *
     * @param results _more_
     *
     * @return _more_
     *
     * @throws Exception _more_
     */
    private ServerInfo makeRemoteServer(ResultSet results) throws Exception {
        URL     url        = new URL(results.getString(1));
        String  title      = results.getString(2);
        String  desc       = results.getString(3);
        String  email      = results.getString(4);
        boolean isRegistry = results.getInt(5) != 0;
        boolean isSelected = results.getInt(6) != 0;

        ServerInfo serverInfo = new ServerInfo(url.getHost(), url.getPort(),
                                    -1, url.getPath(), title, desc, email,
                                    isRegistry, isSelected);
        return serverInfo;
    }

    /**
     * _more_
     *
     * @return _more_
     *
     * @throws Exception _more_
     */
    public List<ServerInfo> getRemoteServers() throws Exception {
        List<ServerInfo> servers = remoteServers;
        if (servers != null) {
            return servers;
        }
        servers = new ArrayList<ServerInfo>();
        Hashtable<String, ServerInfo> map = new Hashtable<String,
                                                ServerInfo>();

        Statement stmt =
            getDatabaseManager().select(Tables.REMOTESERVERS.COLUMNS,
                                        Tables.REMOTESERVERS.NAME,
                                        (Clause) null,
                                        " order by "
                                        + Tables.REMOTESERVERS.COL_URL);
        SqlUtil.Iterator iter = getDatabaseManager().getIterator(stmt);
        ResultSet        results;
        while ((results = iter.getNext()) != null) {
            ServerInfo serverInfo = makeRemoteServer(results);
            map.put(serverInfo.getId(), serverInfo);
            servers.add(serverInfo);
        }
        clearRemoteServers();
        remoteServerMap = map;
        remoteServers   = servers;
        return servers;
    }


    /**
     * _more_
     *
     * @return _more_
     */
    public List<String> getServersToRegisterWith() {
        List<String> urls =
            StringUtil.split(
                getRepository().getProperty(
                    PROP_REGISTRY_SERVERS,
                    "http://motherlode.ucar.edu/repository"), "\n", true,
                        true);
        return urls;
    }


    /**
     * _more_
     *
     * @throws Exception _more_
     */
    public void registerWithServers() throws Exception {
        List<String> urls = getServersToRegisterWith();
        for (String url : urls) {
            Misc.run(this, "registerWithServer", url);
        }
    }


    /**
     * _more_
     *
     * @param url _more_
     *
     * @throws Exception _more_
     */
    public void registerWithServer(String url) throws Exception {
        ServerInfo serverInfo = getRepository().getServerInfo();
        url = url + getRepository().URL_REGISTRY_ADD.getPath();
        URL theUrl = new URL(HtmlUtils.url(url, ARG_REGISTRY_CLIENT,
                                          serverInfo.getUrl()));
        try {
            String  contents = getStorageManager().readSystemResource(theUrl);
            Element root     = XmlUtil.getRoot(contents);
            if ( !responseOk(root)) {
                logInfo(
                    "RegistryManager.registerWithServer: Failed to register with:"
                    + theUrl);
                //                logInfo(XmlUtil.getChildText(root).trim());
            } else {
                logInfo(
                    "RegistryManager.registerWithServer: Registered with:"
                    + theUrl);
            }

        } catch (Exception exc) {
            logError(
                "RegistryManager.registerWithServer: Error registering with:"
                + theUrl, exc);
        }
    }


    /**
     * _more_
     *
     * @param root _more_
     *
     * @return _more_
     */
    public boolean responseOk(Element root) {
        return XmlUtil.getAttribute(root, ATTR_CODE).equals(CODE_OK);
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
    public Result processRegistryAdd(Request request) throws Exception {
        StringBuffer sb = new StringBuffer();
        if ( !isEnabledAsServer()) {
            logInfo(
                "RegistryManager.processRegistryAdd: Was asked to register a server when this server is not configured as a registry server. URL = "
                + request);
            return new Result(
                XmlUtil.tag(
                    TAG_RESPONSE, XmlUtil.attr(ATTR_CODE, CODE_ERROR),
                    "Not enabled as a registry"), MIME_XML);
        }


        String     baseUrl    = request.getString(ARG_REGISTRY_CLIENT, "");

        ServerInfo serverInfo = new ServerInfo(new URL(baseUrl), "", "");

        logInfo(
            "RegistryManager.processRegistryAdd: calling checkServer url="
            + baseUrl);
        if (checkServer(serverInfo, true)) {
            return new Result(XmlUtil.tag(TAG_RESPONSE,
                                          XmlUtil.attr(ATTR_CODE, CODE_OK),
                                          "OK"), MIME_XML);
        }
        return new Result(XmlUtil.tag(TAG_RESPONSE,
                                      XmlUtil.attr(ATTR_CODE, CODE_ERROR),
                                      "failed"), MIME_XML);

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
    public Result processRegistryInfo(Request request) throws Exception {
        final String requestingServer =
            request.getString(ARG_REGISTRY_SERVER, "");
        URL          requestingServerUrl = new URL(requestingServer);
        List<String> servers             = getServersToRegisterWith();
        boolean      ok                  = false;
        StringBuffer msg                 = new StringBuffer();

        int          requestingPort      = requestingServerUrl.getPort();
        if (requestingPort == -1) {
            requestingPort = 80;
        }
        msg.append("requesting server:" + requestingServerUrl.getHost() + ":"
                   + requestingPort + "\n");


        for (String myServer : servers) {
            URL myServerUrl  = new URL(myServer);
            int myServerPort = myServerUrl.getPort();
            if (myServerPort == -1) {
                myServerPort = 80;
            }
            msg.append("    my server:" + myServerUrl.getHost() + ":"
                       + myServerPort + "\n");
            if (myServerUrl.getHost().toLowerCase()
                    .equals(requestingServerUrl.getHost()
                        .toLowerCase()) && (myServerPort == requestingPort)) {
                ok = true;
                break;
            }
        }

        if ( !ok) {
            logInfo(
                "RegistryManger.processRegistryInfo: Was asked to register with a server:"
                + requestingServer + " that is not in our list:" + servers);
            return new Result(
                XmlUtil.tag(
                    TAG_RESPONSE, XmlUtil.attr(ATTR_CODE, CODE_ERROR),
                    "Not registering with you. Output: " + msg), MIME_XML);
        }
        Misc.run(new Runnable() {
            public void run() {
                try {
                    fetchRemoteServers(requestingServer);
                } catch (Exception exc) {
                    logError(
                        "RegistryManager.processRegistryInfo: Loading servers from:"
                        + requestingServer, exc);
                }
            }
        });

        return getRepository().processInfo(request);
    }


    /**
     * _more_
     *
     * @param serverUrl _more_
     *
     * @throws Exception _more_
     */
    private void fetchRemoteServers(String serverUrl) throws Exception {
        serverUrl = serverUrl + getRepository().URL_REGISTRY_LIST.getPath();
        serverUrl = HtmlUtils.url(serverUrl, ARG_RESPONSE, RESPONSE_XML);
        String contents =
            getStorageManager().readSystemResource(new URL(serverUrl));
        Element root = XmlUtil.getRoot(contents);

        if ( !responseOk(root)) {
            logInfo("RegistryManager.fetchRemoteServers: Bad response from "
                    + serverUrl);
            return;
        }
        List<ServerInfo> servers  = new ArrayList<ServerInfo>();
        ServerInfo       me       = getRepository().getServerInfo();
        NodeList         children = XmlUtil.getElements(root);
        for (int i = 0; i < children.getLength(); i++) {
            Element node = (Element) children.item(i);
            servers.add(new ServerInfo(node));
        }


        Hashtable<String, ServerInfo> map = getRemoteServerMap();
        for (ServerInfo serverInfo : servers) {
            if (serverInfo.equals(me)) {
                continue;
            } else {}
            ServerInfo oldServer = map.get(serverInfo.getId());
            if (oldServer != null) {
                serverInfo.setSelected(oldServer.getSelected());
            }
            addRemoteServer(serverInfo, oldServer != null);
        }
    }


    /**
     * _more_
     *
     * @param serverInfo _more_
     * @param deleteOldServer _more_
     *
     * @throws Exception _more_
     */
    private void addRemoteServer(ServerInfo serverInfo,
                                 boolean deleteOldServer)
            throws Exception {
        if (deleteOldServer) {
            getDatabaseManager().delete(
                Tables.REMOTESERVERS.NAME,
                Clause.eq(Tables.REMOTESERVERS.COL_URL, serverInfo.getUrl()));
        }


        getDatabaseManager().executeInsert(Tables.REMOTESERVERS.INSERT,
                                           new Object[] {
            serverInfo.getUrl(), serverInfo.getTitle(),
            serverInfo.getDescription(), serverInfo.getEmail(),
            new Boolean(serverInfo.getIsRegistry()),
            new Boolean(serverInfo.getSelected())
        });
        clearRemoteServers();
    }


    /**
     * _more_
     *
     * @param serverInfo _more_
     * @param deleteOnFailure _more_
     *
     * @return _more_
     *
     * @throws Exception _more_
     */
    private boolean checkServer(ServerInfo serverInfo,
                                boolean deleteOnFailure)
            throws Exception {

        String serverUrl =
            HtmlUtils.url(
                serverInfo.getUrl()
                + getRepository().URL_REGISTRY_INFO.getPath(), new String[] {
                    ARG_RESPONSE,
                    RESPONSE_XML, ARG_REGISTRY_SERVER,
                    getRepository().getServerInfo().getUrl() });

        try {
            String contents =
                getStorageManager().readSystemResource(new URL(serverUrl));
            Element root = XmlUtil.getRoot(contents);
            if (responseOk(root)) {
                ServerInfo clientServer = new ServerInfo(root);
                if (clientServer.equals(serverInfo)) {
                    logInfo("RegistryManager.checkServer: adding server "
                            + serverUrl);
                    getDatabaseManager().delete(Tables.SERVERREGISTRY.NAME,
                            Clause.eq(Tables.SERVERREGISTRY.COL_URL,
                                      serverInfo.getUrl()));
                    getDatabaseManager().executeInsert(
                        Tables.SERVERREGISTRY.INSERT,
                        new Object[] { clientServer.getUrl(),
                                       clientServer.getTitle(),
                                       clientServer.getDescription(),
                                       clientServer.getEmail(),
                                       new Boolean(
                                           clientServer.getIsRegistry()) });
                    clearRegisteredServers();
                    return true;
                } else {
                    logInfo("RegistryManager.checkServer: not equals:"
                            + serverInfo.getId() + " "
                            + clientServer.getId());

                }
            } else {
                logInfo("RegistryManager.checkServer: response not ok from:"
                        + serverInfo + " with url: " + serverUrl
                        + "response:\n" + contents);
            }
        } catch (Exception exc) {
            logError(
                "RegistryManager.checkServer: Could not fetch server xml from:"
                + serverInfo + " with url:" + serverUrl, exc);
        }
        if (deleteOnFailure) {
            logInfo("RegistryManager.checkServer: Deleting server:"
                    + serverInfo.getUrl());
            getDatabaseManager().delete(
                Tables.SERVERREGISTRY.NAME,
                Clause.eq(
                    Tables.SERVERREGISTRY.COL_URL, serverInfo.getUrl()));
            clearRegisteredServers();
        }
        return false;
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
    public Result processRegistryList(Request request) throws Exception {
        boolean responseAsXml = request.getString(ARG_RESPONSE,
                                    "").equals(RESPONSE_XML);

        List<ServerInfo>    registeredServers = getRegisteredServers();
        List<ServerInfo>    remoteServers     = getSelectedRemoteServers();
        HashSet<ServerInfo> seen              = new HashSet<ServerInfo>();
        if (responseAsXml) {
            List<ServerInfo> servers = registeredServers;
            //Add myself to the list
            servers.add(0, getRepository().getServerInfo());
            Document resultDoc = XmlUtil.makeDocument();
            Element resultRoot = XmlUtil.create(resultDoc, TAG_RESPONSE,
                                     null, new String[] { ATTR_CODE,
                    CODE_OK });


            for (ServerInfo serverInfo : servers) {
                resultRoot.appendChild(serverInfo.toXml(getRepository(),
                        resultDoc));
            }
            return new Result(XmlUtil.toString(resultRoot, false), MIME_XML);
        }



        StringBuffer sb = new StringBuffer();
        for (int i = 0; i < 2; i++) {
            boolean          evenRow = false;
            boolean          didone  = false;
            List<ServerInfo> servers = ((i == 0)
                                        ? registeredServers
                                        : remoteServers);
            for (ServerInfo serverInfo : servers) {
                if (seen.contains(serverInfo)) {
                    continue;
                }
                if ( !didone) {
                    sb.append(HtmlUtils.p());
                    if (i == 0) {
                        sb.append(msgHeader("Registered Servers"));
                    } else {
                        sb.append(msgHeader("Remote Servers"));
                    }
                    sb.append("<table cellspacing=\"0\" cellpadding=\"4\">");
                    sb.append(HtmlUtils.row(HtmlUtils.headerCols(new String[] {
                        msg("Repository"),
                        msg("URL"), msg("Is Registry?") })));
                }
                didone = true;
                seen.add(serverInfo);
                sb.append(HtmlUtils.row(HtmlUtils.cols(new String[] {
                    serverInfo.getLabel(),
                    HtmlUtils.href(serverInfo.getUrl(), serverInfo.getUrl()),
                    (serverInfo.getIsRegistry()
                     ? msg("Yes")
                     : msg("No")) }), HtmlUtils.cssClass(evenRow
                        ? "listrow1"
                        : "listrow2")));
                String desc = serverInfo.getDescription();
                if ((desc != null) && (desc.trim().length() > 0)) {
                    desc = HtmlUtils.makeShowHideBlock(msg("Description"),
                            desc, false);
                    sb.append(HtmlUtils.row(HtmlUtils.colspan(desc, 3),
                                           HtmlUtils.cssClass(evenRow
                            ? "listrow1"
                            : "listrow2")));
                }
                evenRow = !evenRow;
            }
            if (didone) {
                sb.append("</table>");
            }
            if (isEnabledAsServer()) {
                if ((i == 0) && !didone) {
                    sb.append(msg("No servers are registered"));
                }
            }
        }
        Result result = new Result(msg("Registry List"), sb);
        return result;
    }


}
