/*
 * Copyright 1997-2010 Unidata Program Center/University Corporation for
 * Atmospheric Research, P.O. Box 3000, Boulder, CO 80307,
 * support@unidata.ucar.edu.
 * 
 * This library is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation; either version 2.1 of the License, or (at
 * your option) any later version.
 * 
 * This library is distributed in the hope that it will be useful, but
 * WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU Lesser
 * General Public License for more details.
 * 
 * You should have received a copy of the GNU Lesser General Public License
 * along with this library; if not, write to the Free Software Foundation,
 * Inc., 59 Temple Place, Suite 330, Boston, MA 02111-1307 USA
 */

package org.ramadda.repository.server;
import org.ramadda.repository.*;


import org.mortbay.jetty.*;
import org.mortbay.jetty.Connector;
import org.mortbay.jetty.Handler;
import org.mortbay.jetty.HttpConnection;
import org.mortbay.jetty.NCSARequestLog;
import org.mortbay.jetty.Request;
import org.mortbay.jetty.Server;


import org.mortbay.jetty.bio.SocketConnector;
import org.mortbay.jetty.handler.*;
import org.mortbay.jetty.handler.AbstractHandler;

import org.mortbay.jetty.security.SslSocketConnector;
import org.mortbay.jetty.servlet.Context;
import org.mortbay.jetty.servlet.ServletHolder;



import ucar.unidata.util.IOUtil;

import ucar.unidata.util.LogUtil;
import ucar.unidata.util.WrapperException;

import java.io.*;

import java.net.*;

import java.util.Date;
import java.util.Enumeration;


import java.util.Hashtable;
import java.util.List;

import java.util.Properties;

import javax.servlet.*;
import javax.servlet.http.*;




/**
 *
 *
 * @author RAMADDA Development Team
 * @version $Revision: 1.3 $
 */
public class JettyServer extends RepositoryServlet implements Constants {

    /**
     * _more_
     *
     * @param args _more_
     * @throws Throwable _more_
     */
    public JettyServer(String[] args) throws Throwable {
        int port = 8080;
        for (int i = 0; i < args.length; i++) {
            if (args[i].equals("-port")) {
                port = new Integer(args[i + 1]).intValue();
                break;
            }
        }


        RepositoryServlet repositoryServlet = new RepositoryServlet(this,
                                                                    args, port);

        Server                   server   = new Server(port);
        Repository repository             = repositoryServlet.getRepository();
        HandlerCollection        handlers = new HandlerCollection();
        ContextHandlerCollection contexts = new ContextHandlerCollection();

        Context context = new Context(contexts, "/", Context.SESSIONS);
        context.addServlet(new ServletHolder(repositoryServlet), "/*");


        RequestLogHandler requestLogHandler = new RequestLogHandler();

        if(!repository.isReadOnly()) {
            NCSARequestLog logger =
                new NCSARequestLog(repository.getStorageManager().getLogDir()
                                   + "/" + "requests.log");
            logger.setExtended(false);
            logger.setRetainDays(90);
            logger.setAppend(true);
            logger.setLogTimeZone("GMT");

            requestLogHandler.setRequestLog(logger);

            handlers.setHandlers(new Handler[] { contexts, new DefaultHandler(),
                                                 requestLogHandler });
        } else {
            handlers.setHandlers(new Handler[] { contexts, new DefaultHandler()});
        }
        server.setHandler(handlers);

        try {
            initSsl(server, repository);
        } catch (Throwable exc) {
            repository.getLogManager().logErrorAndPrint(
                "SSL: error opening ssl connection", exc);
        }
        server.start();
        server.join();
    }


    /**
     * _more_
     *
     * @param server _more_
     * @param repository _more_
     *
     * @throws Throwable _more_
     */
    protected void initSsl(Server server, Repository repository)
            throws Throwable {


        File keystore =
            new File(repository.getPropertyValue(PROP_SSL_KEYSTORE,
                repository.getStorageManager().getRepositoryDir()
                + "/keystore", false));
        if ( !keystore.exists()) {
            return;
        }

        if (repository.getProperty(PROP_SSL_IGNORE, false)) {
            repository.getLogManager().logInfo("SSL: ssl.ignore is set.");
            return;
        }
        repository.getLogManager().logInfo("SSL: using keystore: "
                                           + keystore);

        String password = repository.getPropertyValue(PROP_SSL_PASSWORD,
                              (String) null, false);
        String keyPassword = repository.getPropertyValue(PROP_SSL_PASSWORD,
                                 password, false);
        if (password == null) {
            repository.getLogManager().logInfo(
                "SSL: no password and keypassword property defined");
            repository.getLogManager().logInfoAndPrint(
                "SSL: define the properties:\n\t" + PROP_SSL_PASSWORD
                + "=<the ssl password>\n" + "\t" + PROP_SSL_KEYPASSWORD
                + "=<the key password>\n"
                + "in some .properties file (e.g., \"ssl.properties\") in the RAMADDA directory:"
                + repository.getStorageManager().getRepositoryDir()
                + "\nor as a System property on the java command line:"
                + "-D" + PROP_SSL_PASSWORD + "=<the ssl password>  " + "-D"
                + PROP_SSL_KEYPASSWORD + "=<the key password>");

            return;
        }


        int sslPort = -1;
        String ssls = repository.getPropertyValue(PROP_SSL_PORT,
                          (String) null, false);
        if ((ssls != null) && (ssls.trim().length() > 0)) {
            sslPort = new Integer(ssls.trim());

        }

        if (sslPort < 0) {
            repository.getLogManager().logInfoAndPrint(
                "SSL: no ssl port defined. not creating ssl connection");
            repository.getLogManager().logInfoAndPrint(
                "SSL: define the property:\n\t" + PROP_SSL_PORT
                + "=<the ssl port>\n"
                + "in some .properties file (e.g., \"ssl.properties\") in the RAMADDA directory:"
                + repository.getStorageManager().getRepositoryDir()
                + "\nor as a System property on the java command line:"
                + "-D" + PROP_SSL_PORT + "=<the ssl port>");

            return;
        }

        repository.getLogManager().logInfo(
            "SSL: creating ssl connection on port:" + sslPort);
        SslSocketConnector sslSocketConnector = new SslSocketConnector();
        sslSocketConnector.setKeystore(keystore.toString());
        sslSocketConnector.setPassword(password);
        sslSocketConnector.setKeyPassword(keyPassword);
        sslSocketConnector.setTrustPassword(password);
        sslSocketConnector.setPort(sslPort);
        server.addConnector(sslSocketConnector);
        repository.setHttpsPort(sslPort);
    }



    /**
     * _more_
     *
     * @param args _more_
     *
     * @throws Throwable _more_
     */
    public static void main(String[] args) throws Throwable {
        try {
            JettyServer mds = new JettyServer(args);
        } catch (Exception exc) {
            LogUtil.printExceptionNoGui(null, "Error in main",
                                        LogUtil.getInnerException(exc));
            System.exit(1);
        }
    }



}
