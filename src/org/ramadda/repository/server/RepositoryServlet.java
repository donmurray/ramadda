/*
* Copyright 2008-2014 Geode Systems LLC
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

package org.ramadda.repository.server;


import org.apache.commons.fileupload.FileItem;
import org.apache.commons.fileupload.FileUploadException;
import org.apache.commons.fileupload.disk.*;


import org.apache.commons.fileupload.servlet.ServletFileUpload;

import org.ramadda.repository.*;

import ucar.unidata.util.IOUtil;
import ucar.unidata.util.LogUtil;



import ucar.unidata.util.Misc;

import ucar.unidata.util.StringUtil;
import ucar.unidata.util.WrapperException;

import java.io.*;

import java.net.*;

import java.text.SimpleDateFormat;

import java.util.ArrayList;
import java.util.Date;
import java.util.Enumeration;

import java.util.Hashtable;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import java.util.Properties;

import java.util.StringTokenizer;

import javax.servlet.*;
import javax.servlet.http.*;


/**
 *
 *
 * @author RAMADDA Development Team
 * @version $Revision: 1.3 $
 */
public class RepositoryServlet extends HttpServlet implements Constants {


    /** _more_ */
    private SimpleDateFormat sdf =
        RepositoryUtil.makeDateFormat("E, d M yyyy HH:m Z");



    /** _more_ */
    private String[] args;

    /** Repository object that will be instantiated */
    private Repository repository;

    /** _more_ */
    private JettyServer jettyServer;


    /**
     * _more_
     */
    public RepositoryServlet() {
        System.err.println("RepositoryServlet:ctor");
    }

    /**
     * _more_
     *
     * @param jettyServer _more_
     * @param args _more_
     * @param port _more_
     * @param properties _more_
     *
     * @throws Exception _more_
     */
    public RepositoryServlet(JettyServer jettyServer, String[] args,
                             int port, Properties properties)
            throws Exception {
        this.jettyServer = jettyServer;
        this.args        = args;
        createRepository(port, properties, false);
    }

    /**
     * _more_
     *
     * @param args _more_
     * @param repository _more_
     *
     * @throws Exception _more_
     */
    public RepositoryServlet(String[] args, Repository repository)
            throws Exception {
        this.args       = args;
        this.repository = repository;
        initRepository(this.repository, false);
    }


    /**
     * _more_
     *
     * @return _more_
     */
    public Repository getRepository() {
        return repository;
    }

    /**
     * Create the repository
     *
     * @param request - an HttpServletRequest object that contains the request the client has made of the servlet
     *
     * @throws Exception - if an Exception occurs during the creation of the repository
     */
    private void createRepository(HttpServletRequest request)
            throws Exception {
        Properties     webAppProperties = new Properties();
        ServletContext context          = getServletContext();
        if (context != null) {
            String      propertyFile = "/WEB-INF/repository.properties";
            InputStream is = context.getResourceAsStream(propertyFile);
            if (is != null) {
                webAppProperties.load(is);
            }
        }
        createRepository(request.getServerPort(), webAppProperties, true);
    }

    /**
     * Create the repository.
     *
     * @param port _more_
     * @param webAppProperties _more_
     * @param checkSsl _more_
     *
     * @throws Exception _more_
     */
    private synchronized void createRepository(int port,
            Properties webAppProperties, boolean checkSsl)
            throws Exception {
        if (repository != null) {
            return;
        }
        String repositoryClassName =
            System.getProperty("repository.classname");
        if (repositoryClassName == null) {
            repositoryClassName = "org.ramadda.repository.Repository";
        }
        Class      repositoryClass = Misc.findClass(repositoryClassName);
        Repository tmpRepository = (Repository) repositoryClass.newInstance();
        tmpRepository.init(getInitParams(), port);
        //        Repository tmpRepository = new Repository(getInitParams(), port);
        tmpRepository.init(webAppProperties);
        initRepository(tmpRepository, checkSsl);
        repository = tmpRepository;
    }


    /**
     * _more_
     *
     * @param tmpRepository _more_
     * @param checkSsl _more_
     */
    private void initRepository(Repository tmpRepository, boolean checkSsl) {
        if (checkSsl) {
            int sslPort = -1;
            String ssls = tmpRepository.getPropertyValue(PROP_SSL_PORT,
                              (String) null, false);
            if ((ssls != null) && (ssls.trim().length() > 0)) {
                sslPort = new Integer(ssls.trim());
            }
            if (sslPort >= 0) {
                tmpRepository.getLogManager().logInfo("SSL: using port:"
                        + sslPort);
                tmpRepository.setHttpsPort(sslPort);
            }
        }
    }






    /**
     * Gets any initialization parameters the specified in the Web deployment descriptor (web.xml)
     * Populates the String[] args which will be passed to repository later.
     *
     * @return - an String[] containing the initialization parameters required for repository startup
     */
    private String[] getInitParams() {
        if (args != null) {
            return args;
        }
        List<String> tokens = new ArrayList<String>();
        for (Enumeration params =
                this.getServletContext().getInitParameterNames();
                params.hasMoreElements(); ) {
            String paramName = (String) params.nextElement();
            if ( !paramName.equals("args")) {
                continue;
            }
            String paramValue =
                getServletContext().getInitParameter(paramName);
            tokens = StringUtil.split(paramValue, ",", true, true);

            break;
        }
        String[] args = (String[]) tokens.toArray(new String[tokens.size()]);

        return args;
    }


    /**
     * _more_
     */
    public void destroy() {
        super.destroy();
        System.err.println("RAMADDA: RepositoryServlet.destroy");
        if (repository != null) {
            try {
                repository.close();
            } catch (Exception e) {
                logException(e);
            }
        }
        repository = null;
    }



    /**
     * Overriding doGet method in HttpServlet. Called by the server via the service method.
     *
     * @param request - an HttpServletRequest object that contains the request the client has made of the servlet
     * @param response - an HttpServletResponse object that contains the response the servlet sends to the client
     *
     * @throws IOException - if an input or output error is detected when the servlet handles the GET request
     * @throws ServletException - if the request for the GET could not be handled
     */
    public void doGet(HttpServletRequest request,
                      HttpServletResponse response)
            throws IOException, ServletException {

        // there can be only one
        if (repository == null) {
            try {
                createRepository(request);
            } catch (Exception e) {
                logException(e, request);
                response.sendError(response.SC_INTERNAL_SERVER_ERROR,
                                   "An error has occurred:" + e.getMessage());

                return;
            }
        }
        request.setCharacterEncoding("UTF-8");
        response.setCharacterEncoding("UTF-8");

        RequestHandler handler          = new RequestHandler(request);
        Result         repositoryResult = null;

        boolean        isHeadRequest    = request.getMethod().equals("HEAD");
        try {
            try {
                // create a org.ramadda.repository.Request object from the relevant info from the HttpServletRequest object
                Request repositoryRequest = new Request(repository,
                                                request.getRequestURI(),
                                                handler.formArgs, request,
                                                response, this);

                repositoryRequest.setIp(request.getRemoteAddr());
                repositoryRequest.setOutputStream(response.getOutputStream());
                repositoryRequest.setFileUploads(handler.fileUploads);
                repositoryRequest.setHttpHeaderArgs(handler.httpArgs);



                // create a org.ramadda.repository.Result object and transpose the relevant info into a HttpServletResponse object
                repositoryResult =
                    repository.handleRequest(repositoryRequest);
                if (jettyServer != null) {
                    //We are running stand-alone so nothing is doing logging

                }
            } catch (Throwable e) {
                e = LogUtil.getInnerException(e);
                logException(e, request);
                response.sendError(response.SC_INTERNAL_SERVER_ERROR,
                                   e.getMessage());
            }
            if (repositoryResult == null) {
                response.sendError(response.SC_INTERNAL_SERVER_ERROR,
                                   "Unknown request:"
                                   + request.getRequestURI());

                return;
            }

            if (repositoryResult.getNeedToWrite()) {
                List<String> args = repositoryResult.getHttpHeaderArgs();
                if (args != null) {
                    for (int i = 0; i < args.size(); i += 2) {
                        String name  = args.get(i);
                        String value = args.get(i + 1);
                        response.setHeader(name, value);
                    }
                }

                Date lastModified = repositoryResult.getLastModified();
                if (lastModified != null) {
                    response.addDateHeader("Last-Modified",
                                           lastModified.getTime());
                }

                if (repositoryResult.getCacheOk()) {
                    //                    response.setHeader("Cache-Control",
                    //                                       "public,max-age=259200");
                    response.setHeader("Expires",
                                       "Tue, 08 Jan 2019 07:41:19 GMT");
                    if (lastModified == null) {
                        //                        response.setHeader("Last-Modified",
                        //                                           "Tue, 20 Jan 2010 01:45:54 GMT");
                    }
                }

                if (isHeadRequest) {
                    response.setStatus(repositoryResult.getResponseCode());

                    return;
                }

                if (repositoryResult.getRedirectUrl() != null) {
                    try {
                        response.sendRedirect(
                            repositoryResult.getRedirectUrl());
                    } catch (Exception e) {
                        logException(e, request);
                    }
                } else if (repositoryResult.getInputStream() != null) {
                    try {
                        response.setStatus(
                            repositoryResult.getResponseCode());
                        response.setContentType(
                            repositoryResult.getMimeType());
                        OutputStream output = response.getOutputStream();
                        try {
                            IOUtil.writeTo(repositoryResult.getInputStream(),
                                           output);
                        } finally {
                            IOUtil.close(output);
                        }
                    } catch (IOException e) {
                        //We'll ignore any ioexception
                    } catch (Exception e) {
                        logException(e, request);
                    } finally {
                        IOUtil.close(repositoryResult.getInputStream());
                    }
                } else {
                    try {
                        response.setStatus(
                            repositoryResult.getResponseCode());
                        response.setContentType(
                            repositoryResult.getMimeType());
                        OutputStream output = response.getOutputStream();
                        try {
                            output.write(repositoryResult.getContent());
                        } catch (java.net.SocketException se) {
                            //ignore
                        } finally {
                            IOUtil.close(output);
                        }
                    } catch (Exception e) {
                        logException(e, request);
                    }
                }
            }
        } finally {
            if ((repositoryResult != null)
                    && (repositoryResult.getInputStream() != null)) {
                IOUtil.close(repositoryResult.getInputStream());
            }
        }

    }


    /**
     * Overriding doPost method in HttpServlet. Called by the server via the service method.
     * Hands off HttpServletRequest and HttpServletResponse to doGet method.
     *
     * @param request - an HttpServletRequest object that contains the request the client has made of the servlet
     * @param response - an HttpServletResponse object that contains the response the servlet sends to the client
     *
     * @throws IOException - if an input or output error is detected when the servlet handles the GET request
     * @throws ServletException - if the request for the POST could not be handled
     */
    public void doPost(HttpServletRequest request,
                       HttpServletResponse response)
            throws IOException, ServletException {
        doGet(request, response);
    }



    /**
     * Class RequestHandler _more_
     *
     *
     * @author RAMADDA Development Team
     * @version $Revision: 1.3 $
     */
    private class RequestHandler {

        /** _more_ */
        Hashtable formArgs = new Hashtable();

        /** _more_ */
        Hashtable httpArgs = new Hashtable();

        /** _more_ */
        Hashtable fileUploads = new Hashtable();


        /**
         * _more_
         *
         * @param request _more_
         *
         * @throws IOException _more_
         */
        public RequestHandler(HttpServletRequest request) throws IOException {
            getFormArgs(request);
            getRequestHeaders(request);
        }


        /**
         * Get parameters of this request including any uploaded files.
         *
         * @param request - an HttpServletRequest object that contains the request the client has made of the servlet
         *
         * @throws IOException _more_
         */
        public void getFormArgs(HttpServletRequest request)
                throws IOException {

            if (ServletFileUpload.isMultipartContent(request)) {
                ServletFileUpload upload =
                    new ServletFileUpload(new DiskFileItemFactory());
                try {
                    upload.setHeaderEncoding("UTF-8");
                    List     items = upload.parseRequest(request);
                    Iterator iter  = items.iterator();
                    while (iter.hasNext()) {
                        FileItem item = (FileItem) iter.next();
                        if (item.isFormField()) {
                            processFormField(item);
                        } else {
                            processUploadedFile(item, request);
                        }
                    }
                } catch (FileUploadException e) {
                    logException(e, request);
                }
            } else {
                // Map containing parameter names as keys and parameter values as map values. 
                // The keys in the parameter map are of type String. The values in the parameter map are of type String array. 
                Map      p  = request.getParameterMap();
                Iterator it = p.entrySet().iterator();
                // Convert Map values into type String. 

                while (it.hasNext()) {
                    Map.Entry pairs = (Map.Entry) it.next();
                    String    key   = (String) pairs.getKey();
                    String[]  vals  = (String[]) pairs.getValue();
                    if (vals.length == 1) {
                        formArgs.put(key, vals[0]);
                    } else if (vals.length > 1) {
                        List values = new ArrayList();
                        for (int i = 0; i < vals.length; i++) {
                            values.add(vals[i]);
                        }
                        formArgs.put(key, values);
                    }
                }
            }
        }



        /**
         * Process any form input.
         *
         * @param item - a form item that was received within a multipart/form-data POST request
         */
        public void processFormField(FileItem item) {
            String name  = item.getFieldName();
            byte[] bytes = item.get();
            //Don't do this for now since it screws up utf-8 character encodings
            //String value = item.getString();
            String value = new String(bytes);
            //            if(name.startsWith("desc")) {
            //                System.err.println ("value:" + value);
            //            }
            formArgs.put(name, value);
        }


        /**
         * Process any files uploaded with the form input.
         *
         * @param item - a file item that was received within a multipart/form-data POST request
         * @param request - an HttpServletRequest object that contains the request the client has made of the servlet
         *
         * @throws IOException _more_
         */
        public void processUploadedFile(FileItem item,
                                        HttpServletRequest request)
                throws IOException {
            String fieldName = item.getFieldName();
            String fileName  = item.getName();
            if ((fileName == null) || (fileName.trim().length() == 0)) {
                return;
            }

            //Look for full path names and get the tail
            int idx = fileName.lastIndexOf("\\");
            if (idx >= 0) {
                fileName = fileName.substring(idx + 1);
            }

            idx = fileName.lastIndexOf("/");
            if (idx >= 0) {
                fileName = fileName.substring(idx + 1);
            }


            //TODO: what should we do with the fileName to ensure against XSS
            //            fileName = HtmlUtils.encode(fileName);


            String contentType = item.getContentType();
            File uploadedFile =
                new File(
                    IOUtil.joinDir(
                        repository.getStorageManager().getUploadDir(),
                        repository.getGUID() + StorageManager.FILE_SEPARATOR
                        + fileName));

            try {
                InputStream inputStream = item.getInputStream();
                OutputStream outputStream =
                    repository.getStorageManager().getFileOutputStream(
                        uploadedFile);
                try {
                    IOUtil.writeTo(inputStream, outputStream);
                } finally {
                    IOUtil.close(outputStream);
                }
                //                item.write(uploadedFile);
            } catch (Exception e) {
                logException(e, request);

                return;
            }
            fileUploads.put(fieldName, uploadedFile.toString());
            formArgs.put(fieldName, fileName);
        }


        /**
         * Gets the HTTP request headers.  Populate httpArgs.
         *
         * @param request - an HttpServletRequest object that contains the request the client has made of the servlet
         */
        public void getRequestHeaders(HttpServletRequest request) {
            for (Enumeration headerNames = request.getHeaderNames();
                    headerNames.hasMoreElements(); ) {
                String name  = (String) headerNames.nextElement();
                String value = request.getHeader(name);
                httpArgs.put(name, value);
            }
        }

    }


    /**
     * _more_
     *
     * @param exc _more_
     */
    protected void logException(Throwable exc) {
        logException(exc, null);
    }

    /**
     * _more_
     *
     * @param exc _more_
     * @param request _more_
     */
    protected void logException(Throwable exc, HttpServletRequest request) {
        try {
            String address = "";
            if (request != null) {
                address = request.getRemoteAddr();
            }
            if (repository != null) {
                repository.getLogManager().logError(
                    "Error in RepositoryServlet address=" + address, exc);

                return;
            }
            System.err.println("Exception: " + exc);
            exc.printStackTrace();
        } catch (Exception ioe) {
            System.err.println("Exception in logging exception:" + ioe);
        }

    }




}
