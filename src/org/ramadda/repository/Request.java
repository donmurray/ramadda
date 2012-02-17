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


import org.ramadda.repository.auth.*;
import org.ramadda.repository.output.*;

import ucar.unidata.sql.SqlUtil;
import ucar.unidata.util.DateUtil;
import ucar.unidata.util.HtmlUtil;
import ucar.unidata.util.IOUtil;
import ucar.unidata.util.LogUtil;
import ucar.unidata.util.Misc;


import ucar.unidata.util.StringUtil;
import ucar.unidata.util.WrapperException;
import ucar.unidata.xml.XmlUtil;

import java.io.File;
import java.io.InputStream;
import java.io.OutputStream;

import java.lang.reflect.*;



import java.net.*;




import java.util.ArrayList;
import java.util.Date;
import java.util.Enumeration;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.List;
import java.util.Properties;
import java.util.regex.*;



import java.util.regex.*;

import javax.servlet.*;
import javax.servlet.http.*;


/**
 *
 *
 * @author RAMADDA Development Team
 */
public class Request implements Constants {


    /** _more_ */
    private static int COUNTER = 0;

    /** _more_ */
    public int count = COUNTER++;


    /** _more_ */
    private Hashtable fileUploads;

    /** _more_ */
    private String type;

    /** _more_ */
    private Hashtable parameters;

    /** _more_ */
    private Hashtable originalParameters;

    /** _more_ */
    private Hashtable extraProperties = new Hashtable();

    /** _more_ */
    private Repository repository;

    /** _more_ */
    private Hashtable httpHeaderArgs;

    /** _more_ */
    private String sessionId;

    /** _more_ */
    private OutputStream outputStream;

    /** _more_ */
    private User user;

    /** _more_ */
    private String ip;

    /** _more_ */
    private String protocol;

    /** _more_ */
    private boolean secure = false;

    /** _more_ */
    //    private Entry collectionEntry;


    /** _more_ */
    private HttpServletRequest httpServletRequest;

    /** _more_ */
    private HttpServletResponse httpServletResponse;

    /** _more_ */
    private HttpServlet httpServlet;


    /** _more_ */
    private String leftMessage;

    /** _more_ */
    private boolean checkingAuthMethod = false;

    /** _more_ */
    private ApiMethod apiMethod;

    /** _more_ */
    private boolean isMobile = false;

    /** _more_ */
    private String htmlTemplateId;

    /** _more_ */
    private PageStyle pageStyle;


    /**
     * _more_
     *
     * @param repository _more_
     * @param user _more_
     */
    public Request(Repository repository, User user) {
        this(repository, user, "");
    }


    /**
     * _more_
     *
     * @param repository _more_
     * @param user _more_
     * @param path _more_
     */
    public Request(Repository repository, User user, String path) {
        this.repository         = repository;
        this.user               = user;
        this.type               = path;
        this.parameters         = new Hashtable();
        this.originalParameters = new Hashtable();
    }


    /**
     * _more_
     *
     *
     * @param repository _more_
     * @param type _more_
     * @param parameters _more_
     */
    public Request(Repository repository, String type, Hashtable parameters) {
        this.repository         = repository;
        this.type               = type;
        this.parameters         = parameters;
        this.originalParameters = new Hashtable();
        originalParameters.putAll(parameters);
    }



    /**
     * _more_
     *
     * @param repository _more_
     * @param type _more_
     * @param parameters _more_
     * @param httpServletRequest _more_
     * @param httpServletResponse _more_
     * @param httpServlet _more_
     */
    public Request(Repository repository, String type, Hashtable parameters,
                   HttpServletRequest httpServletRequest,
                   HttpServletResponse httpServletResponse,
                   HttpServlet httpServlet) {
        this(repository, type, parameters);
        this.httpServletRequest  = httpServletRequest;
        this.httpServletResponse = httpServletResponse;
        this.httpServlet         = httpServlet;
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
     * _more_
     *
     * @return _more_
     */
    public boolean isConnected() {
        try {
            OutputStream os = getHttpServletResponse().getOutputStream();
            InputStream  is = getHttpServletRequest().getInputStream();
            System.err.println(is.available());
            is.read();
            //System.err.println(is.available());
        } catch (Exception exc) {
            System.err.println("bad");
            return false;
        }
        return true;
    }


    /**
     * _more_
     *
     * @param theUrl _more_
     * @param entry _more_
     *
     * @return _more_
     */
    public String entryUrl(RequestUrl theUrl, Entry entry) {
        return entryUrl(theUrl, entry, ARG_ENTRYID);
    }

    /**
     * _more_
     *
     * @return _more_
     */
    public boolean useFullUrl() {
        return get(ARG_FULLURL, false);
    }


    /**
     * _more_
     *
     * @param url _more_
     *
     * @return _more_
     */
    public String checkUrl(String url) {
        if (useFullUrl() && !url.startsWith("http")) {
            return getAbsoluteUrl(url);
        }
        return url;
    }


    /**
     * _more_
     *
     * @return _more_
     */
    public boolean isPost() {
        if (httpServletRequest == null) {
            return false;
        }
        return httpServletRequest.getMethod().equals("POST");
    }

    /**
     * _more_
     *
     * @param filename _more_
     */
    public void setReturnFilename(String filename) {
        filename = filename.replaceAll(" ", "_");
        httpServletResponse.setHeader("Content-disposition",
                                      "attachment; filename=" + filename);
    }


    /**
     * _more_
     *
     * @param theUrl _more_
     * @param entry _more_
     *
     * @return _more_
     */
    public String getEntryUrl(String theUrl, Entry entry) {
        String url = theUrl.toString();
        if (theUrl.equals(repository.URL_ENTRY_SHOW.toString())) {
            String name = entry.getFullName(true);
            try {
                name = name.replace("/", "_FORWARDSLASH_");
                name = java.net.URLEncoder.encode(name, "UTF-8");
                name = name.replace("_FORWARDSLASH_", "/");
                name = name.replace("?", "_");
                //A hack because the browser thinks this a zipped page
                if (name.endsWith(".gz")) {
                    name = name.replace(".gz", "");
                } else if (name.endsWith(".tgz")) {
                    name = name.replace(".tgz", "");
                }
            } catch (Exception ignore) {}
            url = url + "/" + name;

        }
        return checkUrl(url);
    }



    /**
     * _more_
     *
     * @param theUrl _more_
     * @param entry _more_
     * @param arg _more_
     *
     * @return _more_
     */
    public String entryUrl(RequestUrl theUrl, Entry entry, String arg) {
        if (entry.getIsRemoteEntry()) {
            String id = repository.getEntryManager().getRemoteEntryInfo(
                            entry.getId())[1];
            if (id.length() == 0) {
                return entry.getRemoteServer() + theUrl.getPath();
            }
            return HtmlUtil.url(entry.getRemoteServer() + theUrl.getPath(),
                                arg, id);
        }



        String url = getEntryUrl(theUrl.toString(), entry);
        if (entry.isTopEntry()) {
            return checkUrl(HtmlUtil.url(url, arg, entry.getId()));
        }
        if (entry.getIsLocalFile()) {
            return checkUrl(HtmlUtil.url(url, arg, entry.getId()));
        }
        return checkUrl(HtmlUtil.url(url, arg, entry.getId()));
    }

    /**
     * _more_
     *
     * @param theUrl _more_
     * @param entry _more_
     * @param arg1 _more_
     * @param value1 _more_
     *
     * @return _more_
     */
    public String entryUrl(RequestUrl theUrl, Entry entry, String arg1,
                           Object value1) {
        return checkUrl(HtmlUtil.url(entryUrl(theUrl, entry), arg1, value1));
    }



    /**
     * _more_
     *
     * @param theUrl _more_
     * @param entry _more_
     * @param args _more_
     *
     * @return _more_
     */
    public String entryUrl(RequestUrl theUrl, Entry entry, List args) {
        return checkUrl(HtmlUtil.url(entryUrl(theUrl, entry), args));
    }


    /**
     * _more_
     *
     * @param theUrl _more_
     * @param entry _more_
     * @param args _more_
     *
     * @return _more_
     */
    public String entryUrl(RequestUrl theUrl, Entry entry, String[] args) {
        return checkUrl(HtmlUtil.url(entryUrl(theUrl, entry), args));
    }

    /**
     * _more_
     *
     * @param theUrl _more_
     * @param entry _more_
     * @param arg1 _more_
     * @param value1 _more_
     * @param arg2 _more_
     * @param value2 _more_
     *
     * @return _more_
     */
    public String entryUrl(RequestUrl theUrl, Entry entry, String arg1,
                           Object value1, String arg2, Object value2) {
        return checkUrl(HtmlUtil.url(entryUrl(theUrl, entry), arg1, value1,
                                     arg2, value2));
    }




    /**
     * _more_
     *
     * @param theUrl _more_
     *
     * @return _more_
     */
    public String url(RequestUrl theUrl) {
        /*
        if (collectionEntry != null) {
            String collectionPath =
                repository.getPathFromEntry(collectionEntry);
            return theUrl.getUrl(collectionPath);
            }*/
        return checkUrl(theUrl.toString());
    }

    /**
     * _more_
     *
     * @param theUrl _more_
     *
     * @return _more_
     */
    public String form(RequestUrl theUrl) {
        return HtmlUtil.form(url(theUrl));
    }


    /**
     * _more_
     *
     * @param theUrl _more_
     *
     * @return _more_
     */
    public String formPost(RequestUrl theUrl) {
        return HtmlUtil.formPost(url(theUrl));
    }


    /**
     * _more_
     *
     * @param theUrl _more_
     * @param extra _more_
     *
     * @return _more_
     */
    public String formPost(RequestUrl theUrl, String extra) {
        return HtmlUtil.formPost(url(theUrl), extra);
    }


    /**
     * _more_
     *
     * @param sb _more_
     * @param theUrl _more_
     */
    public void formPostWithAuthToken(StringBuffer sb, RequestUrl theUrl) {
        formPostWithAuthToken(sb, theUrl, null);
    }

    /**
     * _more_
     *
     * @param sb _more_
     * @param theUrl _more_
     * @param extra _more_
     */
    public void formPostWithAuthToken(StringBuffer sb, RequestUrl theUrl,
                                      String extra) {
        sb.append(formPost(theUrl, extra));
        repository.addAuthToken(this, sb);
    }


    /**
     * _more_
     *
     * @param sb _more_
     * @param theUrl _more_
     */
    public void uploadFormWithAuthToken(StringBuffer sb, RequestUrl theUrl) {
        uploadFormWithAuthToken(sb, theUrl, null);
    }

    /**
     * _more_
     *
     * @param sb _more_
     * @param theUrl _more_
     * @param extra _more_
     */
    public void uploadFormWithAuthToken(StringBuffer sb, RequestUrl theUrl,
                                        String extra) {
        sb.append(HtmlUtil.uploadForm(url(theUrl), extra));
        repository.addAuthToken(this, sb);
    }




    /**
     * _more_
     *
     * @param theUrl _more_
     * @param extra _more_
     *
     * @return _more_
     */
    public String form(RequestUrl theUrl, String extra) {
        return HtmlUtil.form(url(theUrl), extra);
    }

    /**
     * _more_
     *
     * @param theUrl _more_
     *
     * @return _more_
     */
    public String uploadForm(RequestUrl theUrl) {
        return uploadForm(theUrl, "");
    }


    /**
     * _more_
     *
     * @param theUrl _more_
     * @param extra _more_
     *
     * @return _more_
     */
    public String uploadForm(RequestUrl theUrl, String extra) {
        return HtmlUtil.uploadForm(url(theUrl), extra);
    }


    /**
     * _more_
     *
     * @param theUrl _more_
     * @param arg1 _more_
     * @param value1 _more_
     *
     * @return _more_
     */
    public String url(RequestUrl theUrl, String arg1, Object value1) {
        return checkUrl(HtmlUtil.url(url(theUrl), arg1, value1));
    }

    /**
     * _more_
     *
     * @param theUrl _more_
     * @param arg1 _more_
     * @param value1 _more_
     * @param arg2 _more_
     * @param value2 _more_
     *
     * @return _more_
     */
    public String url(RequestUrl theUrl, String arg1, Object value1,
                      String arg2, Object value2) {
        return checkUrl(HtmlUtil.url(url(theUrl), arg1, value1, arg2,
                                     value2));
    }

    /**
     * _more_
     *
     * @param theUrl _more_
     * @param arg1 _more_
     * @param value1 _more_
     * @param arg2 _more_
     * @param value2 _more_
     * @param arg3 _more_
     * @param value3 _more_
     *
     * @return _more_
     */
    public String url(RequestUrl theUrl, String arg1, Object value1,
                      String arg2, Object value2, String arg3,
                      Object value3) {
        return checkUrl(HtmlUtil.url(url(theUrl), arg1, value1, arg2, value2,
                                     arg3, value3));
    }

    /**
     * _more_
     *
     * @param theUrl _more_
     * @param arg1 _more_
     * @param value1 _more_
     * @param arg2 _more_
     * @param value2 _more_
     * @param arg3 _more_
     * @param value3 _more_
     * @param arg4 _more_
     * @param value4 _more_
     *
     * @return _more_
     */
    public String url(RequestUrl theUrl, String arg1, Object value1,
                      String arg2, Object value2, String arg3, Object value3,
                      String arg4, Object value4) {
        return checkUrl(HtmlUtil.url(url(theUrl), arg1, value1, arg2, value2,
                                     arg3, value3, arg4, value4));
    }



    /**
     *  Set the OutputStream property.
     *
     *  @param value The new value for OutputStream
     */
    public void setOutputStream(OutputStream value) {
        outputStream = value;
    }

    /**
     *  Get the OutputStream property.
     *
     *  @return The OutputStream
     */
    public OutputStream getOutputStream() {
        return outputStream;
    }



    /**
     * _more_
     *
     * @param uploads _more_
     */
    public void setFileUploads(Hashtable uploads) {
        fileUploads = uploads;
    }


    /**
     * _more_
     *
     * @param arg _more_
     *
     * @return _more_
     */
    public String getUploadedFile(String arg) {
        if (fileUploads == null) {
            return null;
        }
        return (String) fileUploads.get(arg);
    }

    /** _more_ */
    public StringBuffer tmp = new StringBuffer();

    /**
     * _more_
     *
     * @return _more_
     */
    public String getUrl() {
        return checkUrl(getRequestPath() + "?" + getUrlArgs());
    }


    /**
     * _more_
     *
     * @param exceptArgs _more_
     * @param exceptValues _more_
     *
     * @return _more_
     */
    public String getUrl(HashSet<String> exceptArgs,
                         HashSet<String> exceptValues) {
        return checkUrl(getRequestPath() + "?"
                        + getUrlArgs(exceptArgs, exceptValues));
    }

    /**
     * _more_
     *
     * @param request _more_
     *
     * @return _more_
     */
    public String getUrl(RequestUrl request) {
        return url(request) + "?" + getUrlArgs();
    }

    /**
     * _more_
     *
     * @return _more_
     */
    public String getFullUrl() {
        return getAbsoluteUrl(getUrl());
    }

    /**
     * _more_
     *
     * @param url _more_
     *
     * @return _more_
     */
    public String getAbsoluteUrl(String url) {
        int port = getServerPort();
        if (port == 80) {
            return repository.getHttpProtocol() + "://" + getServerName()
                   + url;
        } else {
            return repository.getHttpProtocol() + "://" + getServerName()
                   + ":" + port + url;
        }
    }



    /**
     * _more_
     *
     * @param except _more_
     *
     * @return _more_
     */
    public String getUrl(String except) {
        return getRequestPath() + "?" + getUrlArgs(except);
    }


    /**
     * _more_
     *
     * @return _more_
     */
    public String getUrlArgs() {
        return getUrlArgs((HashSet) null);
    }

    /**
     * _more_
     *
     * @param except _more_
     *
     * @return _more_
     */
    public String getUrlArgs(String except) {
        HashSet<String> tmp = new HashSet<String>();
        tmp.add(except);
        return getUrlArgs(tmp);
    }


    /**
     * _more_
     *
     *
     * @param exceptArgs _more_
     *
     * @return _more_
     */
    public String getUrlArgs(HashSet<String> exceptArgs) {
        return getUrlArgs(exceptArgs, null);
    }


    /**
     * _more_
     *
     * @param exceptArgs _more_
     * @param exceptValues _more_
     *
     * @return _more_
     */
    public String getUrlArgs(HashSet<String> exceptArgs,
                             HashSet<String> exceptValues) {
        return getUrlArgs(exceptArgs, exceptValues, null);
    }



    /**
     * _more_
     *
     * @param exceptArgs _more_
     * @param exceptValues _more_
     * @param exceptArgsPattern _more_
     *
     * @return _more_
     */
    public String getUrlArgs(HashSet<String> exceptArgs,
                             HashSet<String> exceptValues,
                             String exceptArgsPattern) {
        StringBuffer sb  = new StringBuffer();
        int          cnt = 0;
        for (Enumeration keys = parameters.keys(); keys.hasMoreElements(); ) {
            String arg = (String) keys.nextElement();
            if ((exceptArgs != null) && (exceptArgs.contains(arg))) {
                continue;
            }

            if ((exceptArgsPattern != null)
                    && arg.matches(exceptArgsPattern)) {
                continue;
            }
            //      System.out.println(arg+":" + exceptArgsPattern+":");

            Object value = parameters.get(arg);
            if ((exceptValues != null) && (exceptValues.contains(value))) {
                continue;
            }
            if (value instanceof List) {
                List l = (List) value;
                if (l.size() == 0) {
                    continue;
                }
                for (int i = 0; i < l.size(); i++) {
                    String svalue = (String) l.get(i);
                    if (svalue.length() == 0) {
                        continue;
                    }
                    if (cnt++ > 0) {
                        sb.append("&");
                    }
                    sb.append(arg + "=" + svalue);
                }
                continue;
            }
            String svalue = value.toString();
            if (svalue.length() == 0) {
                continue;
            }
            if (cnt++ > 0) {
                sb.append("&");
            }
            try {
                svalue = java.net.URLEncoder.encode(svalue, "UTF-8");
            } catch (Exception exc) {  /*noop*/
            }
            sb.append(arg + "=" + svalue);
        }
        return sb.toString();
    }


    /**
     * _more_
     *
     * @return _more_
     */
    public boolean isEmbedded() {
        return get(ARG_EMBEDDED, false);
    }


    /**
     * _more_
     *
     * @return _more_
     */
    public String getPathEmbeddedArgs() {
        try {
            StringBuffer sb  = new StringBuffer();
            int          cnt = 0;
            for (Enumeration keys =
                    parameters.keys(); keys.hasMoreElements(); ) {
                String arg   = (String) keys.nextElement();
                Object value = parameters.get(arg);
                if (value instanceof List) {
                    List l = (List) value;
                    if (l.size() == 0) {
                        continue;
                    }
                    for (int i = 0; i < l.size(); i++) {
                        String svalue = (String) l.get(i);
                        if (svalue.length() == 0) {
                            continue;
                        }
                        if (cnt++ > 0) {
                            sb.append("/");
                        }
                        sb.append(arg + ":" + encodeEmbedded(svalue));
                    }
                    continue;
                }
                String svalue = value.toString();
                if (svalue.length() == 0) {
                    continue;
                }
                if (cnt++ > 0) {
                    sb.append("/");
                }
                sb.append(arg + ":" + encodeEmbedded(svalue));
            }
            return sb.toString();
        } catch (Exception exc) {
            throw new WrapperException(exc);
        }
    }


    /**
     * _more_
     *
     *
     * @param o _more_
     *
     * @return _more_
     */
    public static String encodeEmbedded(Object o) {
        String s = o.toString();
        try {
            if (s.indexOf("/") >= 0) {
                s = "b64:" + RepositoryUtil.encodeBase64(s.getBytes()).trim();
            }
            //            s = java.net.URLEncoder.encode(s, "UTF-8");
            return s;
        } catch (Exception exc) {
            throw new WrapperException(exc);
        }
    }


    /**
     * _more_
     *
     * @param s _more_
     *
     * @return _more_
     */
    public static String decodeEmbedded(String s) {
        try {
            if (s.startsWith("b64:")) {
                s = s.substring(4);
                //s = java.net.URLDecoder.decode(s, "UTF-8");     
                s = new String(RepositoryUtil.decodeBase64(s));
            }
            return s;
        } catch (Exception exc) {
            throw new WrapperException(exc);
        }

    }


    /**
     * _more_
     *
     * @param key _more_
     *
     * @return _more_
     */
    public String getFromPath(String key) {
        String path = getRequestPath();
        //Look for .../id:<id>
        String prefix = key + ":";
        int    idx    = path.indexOf(prefix);
        if (idx >= 0) {
            String value = path.substring(idx + prefix.length());
            idx = value.indexOf("/");
            if (idx >= 0) {
                value = value.substring(0, idx);
            }
            try {
                value = decodeEmbedded(value);
            } catch (Exception exc) {
                throw new WrapperException(exc);
            }
            return value;
        }
        return null;
    }


    /**
     * _more_
     *
     * @return _more_
     */
    public Hashtable getDefinedProperties() {
        Hashtable props = new Hashtable();
        for (Enumeration keys = parameters.keys(); keys.hasMoreElements(); ) {
            String arg   = keys.nextElement().toString();
            Object value = parameters.get(arg);
            if (value instanceof List) {
                if (((List) value).size() == 0) {
                    continue;
                }
                props.put(arg, value);
                continue;
            }
            if (value.toString().length() == 0) {
                continue;
            }
            props.put(arg, value);
        }
        return props;
    }


    /**
     * _more_
     *
     * @return _more_
     */
    public Hashtable getArgs() {
        return parameters;
    }

    /**
     * _more_
     *
     * @param o _more_
     *
     * @return _more_
     */
    public boolean equals(Object o) {
        if ( !o.getClass().equals(getClass())) {
            return false;
        }
        Request that = (Request) o;
        return this.type.equals(that.type)
               && Misc.equals(this.user, that.user)
               && this.originalParameters.equals(that.originalParameters);
    }


    /**
     * _more_
     *
     * @return _more_
     */
    public int hashCode() {
        return type.hashCode() ^ Misc.hashcode(user)
               ^ originalParameters.hashCode();
    }

    /**
     * _more_
     *
     * @param key _more_
     *
     * @return _more_
     */
    public boolean hasParameter(String key) {
        return parameters.containsKey(key);
    }


    /**
     * _more_
     *
     * @param key _more_
     *
     * @return _more_
     */
    public Object remove(Object key) {
        Object v = parameters.get(key);
        parameters.remove(key);
        return v;
    }

    /**
     * _more_
     *
     * @param key _more_
     * @param value _more_
     */
    public void put(Object key, Object value) {
        parameters.put(key, value);
    }


    /**
     * _more_
     *
     * @param key _more_
     *
     * @return _more_
     */
    public boolean exists(Object key) {
        Object result = getValue(key, (Object) null);
        return result != null;
    }

    /**
     * _more_
     *
     * @param key _more_
     *
     * @return _more_
     */
    public boolean defined(String key) {
        if (key == null) {
            return false;
        }
        Object result = getValue(key, (Object) null);
        if (result == null) {
            return false;
        }
        if (result instanceof List) {
            return ((List) result).size() > 0;
        }
        String sresult = (String) result;
        if (sresult.trim().length() == 0) {
            return false;
        }
        //Check if its a macro that was not set
        if (sresult.equals("${" + key + "}")) {
            return false;
        }
        return true;
    }


    /**
     * _more_
     *
     * @param key _more_
     *
     * @return _more_
     */
    public boolean hasMultiples(String key) {
        Object result = getValue(key, (Object) null);
        if (result == null) {
            return false;
        }
        return (result instanceof List);
    }




    /**
     * _more_
     *
     * @param key _more_
     * @param dflt _more_
     *
     * @return _more_
     */
    public List get(String key, List dflt) {
        Object result = getValue(key, (Object) null);
        if (result == null) {
            return dflt;
        }
        if (result instanceof List) {
            return (List) result;
        }
        List tmp = new ArrayList();
        tmp.add(result);
        return tmp;
    }


    /**
     * _more_
     *
     * @param key _more_
     * @param dflt _more_
     *
     * @return _more_
     */
    public String getUnsafeString(String key, String dflt) {
        String result = (String) getValue(key, (String) null);
        if (result == null) {
            return dflt;
        }
        return result;
    }


    /** _more_ */
    private static Pattern checker;

    /**
     * _more_
     *
     * @param key _more_
     * @param dflt _more_
     * @param patternString _more_
     *
     * @return _more_
     */
    public String getCheckedString(String key, String dflt,
                                   String patternString) {
        return getCheckedString(key, dflt, Pattern.compile(patternString));
    }


    /**
     * _more_
     *
     * @param key _more_
     * @param dflt _more_
     *
     * @return _more_
     */
    public String getEncodedString(String key, String dflt) {
        String s = getString(key, dflt);
        if (s != null) {
            //            s = RepositoryUtil.encodeInput(s);
            s = HtmlUtil.entityEncode(s);
        }
        return s;
    }


    /**
     * _more_
     *
     * @param key _more_
     * @param dflt _more_
     *
     * @return _more_
     */
    public String getAnonymousEncodedString(String key, String dflt) {
        if ( !isAnonymous()) {
            return getString(key, dflt);
        }
        return getEncodedString(key, dflt);
    }




    /**
     * _more_
     *
     * @param sb _more_
     */
    public void appendMessage(StringBuffer sb) {
        if (defined(ARG_MESSAGE)) {
            String message = getUnsafeString(ARG_MESSAGE, "");
            //            message = HtmlUtil.entityEncode(getUnsafeString(ARG_MESSAGE, "");
            message = RepositoryBase.getDialogString(message);
            //Encode this to keep from a spoof attack
            message = HtmlUtil.entityEncode(message);
            sb.append(repository.showDialogNote(message));
            remove(ARG_MESSAGE);
        }
    }


    /**
     * _more_
     *
     * @param key _more_
     * @param dflt _more_
     * @param pattern _more_
     *
     * @return _more_
     */
    public String getCheckedString(String key, String dflt, Pattern pattern) {
        String v = (String) getValue(key, (String) null);
        if (v == null) {
            return dflt;
        }

        //If the user is anonymous then replace all "script" strings with "_script_"
        //encode < and >
        if (isAnonymous()) {
            v = v.replaceAll("([sS][cC][rR][iI][pP][tT])", "_$1_");
            v = v.replaceAll("<", "&lt;");
            v = v.replaceAll(">", "&gt;");
        }


        Matcher matcher = pattern.matcher(v);
        if ( !matcher.find()) {
            throw new BadInputException("Incorrect input for:" + key
                                        + " value:" + v + ":");
        }

        //        v = HtmlUtil.entityEncode(v);
        //TODO:Check the value
        return v;
        //        return repository.getDatabaseManager().escapeString(v);
    }


    /**
     * _more_
     *
     * @param key _more_
     *
     * @return _more_
     */
    public String getString(String key) {
        return getString(key, "");
    }

    /**
     * _more_
     */
    public void ensureAdmin() {
        if ( !getUser().getAdmin()) {
            throw new IllegalArgumentException("Need to be an administrator");
        }
    }


    //.../?sessionid=foobar

    /**
     * _more_
     */
    public void ensureAuthToken() {
        //java.awt.Toolkit.getDefaultToolkit().beep();

        String authToken = getString(ARG_AUTHTOKEN, (String) null);
        String sessionId = getSessionId();
        if (sessionId == null) {
            sessionId = getString(ARG_SESSIONID, (String) null);
        }
        //        System.err.println("session:" + sessionId);
        //        System.err.println("auth token:" + authToken);
        //        System.err.println("session hashed:" + repository.getAuthToken(sessionId));
        if ((authToken != null) && (sessionId != null)) {
            if (authToken.equals(repository.getAuthToken(sessionId))) {
                return;
            }
        }
        throw new IllegalArgumentException("Bad authentication token");
    }


    /**
     * _more_
     *
     * @param key _more_
     * @param dflt _more_
     *
     * @return _more_
     */
    public String getString(String key, String dflt) {
        if (checker == null) {
            checker =
                Pattern.compile(repository.getProperty(PROP_REQUEST_PATTERN));
        }
        return getCheckedString(key, dflt, checker);
    }

    /**
     * _more_
     *
     * @param key _more_
     * @param dflt _more_
     *
     * @return _more_
     */
    private Object getValue(Object key, Object dflt) {
        Object result = parameters.get(key);
        if (result == null) {
            result = getFromPath(key.toString());
        }
        if (result == null) {
            return dflt;
        }
        return result;
    }


    /**
     * _more_
     *
     * @param key _more_
     * @param dflt _more_
     *
     * @return _more_
     */
    private String getValue(Object key, String dflt) {
        Object result = parameters.get(key);
        if (result == null) {
            result = getFromPath(key.toString());
        }
        if (result == null) {
            return dflt;
        }
        if (result instanceof List) {
            List l = (List) result;
            if (l.size() == 0) {
                return dflt;
            }
            return (String) l.get(0);
        }
        String s = result.toString();
        if (s.startsWith("${")) {
            String extra =
                (String) repository.getSessionManager().getSessionExtra(s);
            if (extra != null) {
                s = extra;
            }
        }
        return s;
    }

    /**
     * _more_
     *
     * @return _more_
     */
    public OutputType getOutput() {
        return getOutput(OutputHandler.OUTPUT_HTML.getId());
    }


    /**
     * _more_
     *
     * @param dflt _more_
     *
     * @return _more_
     */
    public OutputType getOutput(String dflt) {
        String     typeId     = getString(ARG_OUTPUT, dflt);
        OutputType outputType = repository.findOutputType(typeId);
        if (outputType != null) {
            return outputType;
        }
        return new OutputType(typeId, OutputType.TYPE_FEEDS);
    }


    /**
     * _more_
     *
     * @param dflt _more_
     *
     * @return _more_
     */
    public String getId(String dflt) {
        return getString(ARG_ENTRYID, dflt);
    }


    /**
     * _more_
     *
     * @param dflt _more_
     *
     * @return _more_
     */
    public String getIds(String dflt) {
        return getString(ARG_ENTRYIDS, dflt);
    }

    /**
     * _more_
     *
     * @param name _more_
     * @param dflt _more_
     *
     * @return _more_
     */
    public String getDateSelect(String name, String dflt) {
        String v = getUnsafeString(name, (String) null);
        if (v == null) {
            return dflt;
        }
        if (defined(name + ".time")) {
            v = v + " " + getUnsafeString(name + ".time", "");
        }

        //TODO:Check value
        return v;
    }

    /**
     * Get the value for a latitude or longitude property
     *
     * @param from the the property
     * @param dflt  the default value
     *
     * @return  the decoded value or the default if not defined
     *
     */
    public double getLatOrLonValue(String from, double dflt) {
        if ( !defined(from)) {
            return dflt;
        }
        String llString = (String) getString(from, "").trim();
        if ((llString == null) || (llString.length() == 0)) {
            return dflt;
        }
        return Misc.decodeLatLon(llString);
    }


    /**
     * _more_
     *
     * @param dflt _more_
     *
     * @return _more_
     */
    public String getWhat(String dflt) {
        return getString(ARG_WHAT, dflt);
    }

    /*
     * _more_
     *
     * @param key _more_
     * @param dflt _more_
     *
     * @return _more_
     */

    /**
     * _more_
     *
     * @param key _more_
     * @param dflt _more_
     *
     * @return _more_
     */
    public int get(Object key, int dflt) {
        String result = (String) getValue(key, (String) null);
        if ((result == null) || (result.trim().length() == 0)) {
            return dflt;
        }
        return new Integer(result).intValue();
    }


    /**
     * _more_
     *
     * @param key _more_
     * @param dflt _more_
     *
     * @return _more_
     */
    public long get(Object key, long dflt) {
        String result = (String) getValue(key, (String) null);
        if ((result == null) || (result.trim().length() == 0)) {
            return dflt;
        }
        return new Long(result).longValue();
    }

    /**
     * _more_
     *
     * @param key _more_
     * @param dflt _more_
     *
     * @return _more_
     */
    public double get(Object key, double dflt) {
        String result = (String) getValue(key, (String) null);
        if ((result == null) || (result.trim().length() == 0)) {
            return dflt;
        }
        return new Double(result).doubleValue();
    }


    /**
     * _more_
     *
     * @param key _more_
     * @param dflt _more_
     *
     * @return _more_
     *
     * @throws java.text.ParseException _more_
     */
    public Date get(Object key, Date dflt) throws java.text.ParseException {
        String result = (String) getValue(key, (String) null);
        if ((result == null) || (result.trim().length() == 0)) {
            return dflt;
        }
        return DateUtil.parse(result);
    }

    /**
     * _more_
     *
     * @param from _more_
     * @param dflt _more_
     *
     * @return _more_
     *
     * @throws Exception _more_
     */
    public Date getDate(String from, Date dflt) throws Exception {
        if ( !defined(from)) {
            return dflt;
        }
        String dateString = (String) getDateSelect(from, "").trim();
        return repository.parseDate(dateString);
    }


    /**
     * _more_
     *
     * @param from _more_
     * @param to _more_
     * @param dflt _more_
     *
     * @return _more_
     *
     * @throws java.text.ParseException _more_
     */
    public Date[] getDateRange(String from, String to, Date dflt)
            throws java.text.ParseException {
        return getDateRange(from, to, ARG_RELATIVEDATE, dflt);
    }

    /**
     * _more_
     *
     * @param from _more_
     * @param to _more_
     * @param relativeArg _more_
     * @param dflt _more_
     *
     * @return _more_
     *
     * @throws java.text.ParseException _more_
     */
    public Date[] getDateRange(String from, String to, String relativeArg,
                               Date dflt)
            throws java.text.ParseException {
        String fromDate = "";
        String toDate   = "";
        if (defined(from) || defined(to)) {
            fromDate = (String) getDateSelect(from, "").trim();
            toDate   = (String) getDateSelect(to, "").trim();
        } else if (defined(relativeArg)) {
            fromDate = (String) getDateSelect(relativeArg, "").trim();
            if (fromDate.equals("none")) {
                return new Date[] { null, null };
            }
            toDate = "now";
        } else if (dflt == null) {
            return new Date[] { null, null };
        }

        //        System.err.println("from:" + fromDate);
        //        System.err.println("to:" + toDate);


        if (dflt == null) {
            dflt = new Date();
        }
        Date[] range = DateUtil.getDateRange(fromDate, toDate, dflt);
        //        System.err.println("dateRange:" + fromDate + " date:" + range[0]);
        return range;
    }


    /**
     * _more_
     *
     * @param key _more_
     * @param value _more_
     *
     * @return _more_
     */
    public boolean setContains(String key, Object value) {
        List list = get(key, (List) null);
        if (list == null) {
            Object singleValue = getValue(key, (Object) null);
            if (singleValue == null) {
                return false;
            }
            return singleValue.equals(value);
        }
        return list.contains(value);
    }


    /**
     * _more_
     *
     * @param key _more_
     * @param dflt _more_
     *
     * @return _more_
     */
    public boolean get(Object key, boolean dflt) {
        String result = (String) getValue(key, (String) null);
        if ((result == null) || (result.trim().length() == 0)) {
            return dflt;
        }
        return new Boolean(result).booleanValue();
    }


    /**
     * _more_
     *
     * @return _more_
     */
    public Enumeration keys() {
        return parameters.keys();
    }

    /**
     * Set the Type property.
     *
     * @param value The new value for Type
     */
    public void setType(String value) {
        type = value;
    }

    /**
     * Get the Type property.
     *
     * @return The Type
     */
    public String getRequestPath() {
        return type;
    }



    /**
     * Class BadInputException _more_
     *
     *
     * @author RAMADDA Development Team
     * @version $Revision: 1.3 $
     */
    public static class BadInputException extends RuntimeException {

        /**
         * _more_
         *
         * @param msg _more_
         */
        public BadInputException(String msg) {
            super(msg);
        }
    }

    /**
     * Set the HttpHeaderArgs property.
     *
     * @param value The new value for HttpHeaderArgs
     */
    public void setHttpHeaderArgs(Hashtable value) {
        httpHeaderArgs = value;
        //TODO: be smarter about this
        String ua = getUserAgent("").toLowerCase();
        isMobile = (ua.indexOf("iphone") >= 0)
                   || (ua.indexOf("android") >= 0)
                   || (ua.indexOf("blackberry") >= 0);
    }

    /**
     * Get the HttpHeaderArgs property.
     *
     * @return The HttpHeaderArgs
     */
    public Hashtable getHttpHeaderArgs() {
        return httpHeaderArgs;
    }

    /**
     * _more_
     *
     * @param name _more_
     *
     * @return _more_
     */
    public String getHeaderArg(String name) {
        if (httpHeaderArgs == null) {
            return null;
        }
        String arg = (String) httpHeaderArgs.get(name);
        if (arg == null) {
            arg = (String) httpHeaderArgs.get(name.toLowerCase());
        }
        return arg;
    }


    /**
     * _more_
     *
     * @return _more_
     */
    public String toString() {
        String args = getUrlArgs();
        if (args.trim().length() > 0) {
            return type + " url args:" + args;
        } else {
            return type;
        }
    }

    /** _more_ */
    private boolean sessionIdWasSet = false;

    /**
     * Set the SessionId property.
     *
     * @param value The new value for SessionId
     */
    public void setSessionId(String value) {
        sessionId       = value;
        sessionIdWasSet = true;
    }

    /**
     * _more_
     *
     * @return _more_
     */
    public boolean getSessionIdWasSet() {
        return sessionIdWasSet;
    }


    /**
     * Get the SessionId property.
     *
     * @return The SessionId
     */
    public String getSessionId() {
        return sessionId;
    }






    /**
     * Set the User property.
     *
     * @param value The new value for User
     */
    public void setUser(User value) {
        user = value;
    }

    /**
     * Get the User property.
     *
     * @return The User
     */
    public User getUser() {
        return user;
    }

    /**
     * _more_
     *
     * @return _more_
     */
    public boolean isAnonymous() {
        if ((user == null) || user.getAnonymous()) {
            return true;
        }
        return false;
    }

    /**
     * Set the Ip property.
     *
     * @param value The new value for Ip
     */
    public void setIp(String value) {
        ip = value;
    }

    /**
     * Get the Ip property.
     *
     * @return The Ip
     */
    public String getIp() {
        return ip;
    }

    /**
     * _more_
     *
     * @return _more_
     */
    public String getUserAgent() {
        return getUserAgent(null);
    }


    /**
     * _more_
     *
     * @return _more_
     */
    public boolean isMobile() {
        return isMobile;
    }

    /**
     * _more_
     *
     * @param dflt _more_
     *
     * @return _more_
     */
    public String getUserAgent(String dflt) {
        String value = getHeaderArg("User-Agent");
        if (value == null) {
            //            System.err.println("no user agent");
            return dflt;
        }
        return value;
    }

    /**
     * _more_
     *
     * @return _more_
     */
    public boolean isSpider() {
        String userAgent = getUserAgent();
        if (userAgent == null) {
            return false;
        }
        userAgent = userAgent.toLowerCase();
        return ((userAgent.indexOf("googlebot") >= 0)
                || (userAgent.indexOf("slurp") >= 0)
                || (userAgent.indexOf("spider") >= 0)
                || (userAgent.indexOf("bots") >= 0)
                || (userAgent.indexOf("msnbot") >= 0));
    }



    /**
     * _more_
     *
     * @return _more_
     */
    public boolean isHeadRequest() {
        if (httpServletRequest != null) {
            return httpServletRequest.getMethod().equals("HEAD");
        }
        return false;
    }


    /**
     * _more_
     *
     * @return _more_
     */
    public String getServerName() {
        String serverName = null;
        if (httpServletRequest != null) {
            serverName = httpServletRequest.getServerName();
        }
        if(serverName==null || serverName.trim().length()==0) {
            serverName = repository.getHostname();
        }
        return serverName;
    }

    /**
     * _more_
     *
     * @return _more_
     */
    public int getServerPort() {
        if (httpServletRequest != null) {
            httpServletRequest.getServerPort();
        }
        return repository.getPort();
    }


    /**
     *  Get the HttpServletRequest property.
     *
     *  @return The HttpServletRequest
     */
    public HttpServletRequest getHttpServletRequest() {
        return httpServletRequest;
    }

    /**
     * _more_
     *
     * @return _more_
     */
    public HttpServletResponse getHttpServletResponse() {
        return httpServletResponse;
    }



    /**
     * _more_
     *
     * @return _more_
     */
    public javax.servlet.http.HttpServlet getHttpServlet() {
        return httpServlet;
    }

    /**
     *  Set the LeftMessage property.
     *
     *  @param value The new value for LeftMessage
     */
    public void setLeftMessage(String value) {
        leftMessage = value;
    }

    /**
     *  Get the LeftMessage property.
     *
     *  @return The LeftMessage
     */
    public String getLeftMessage() {
        return leftMessage;
    }

    /**
     * _more_
     *
     * @param key _more_
     * @param value _more_
     */
    public void putExtraProperty(Object key, Object value) {
        extraProperties.put(key, value);
    }

    /**
     * _more_
     *
     * @param key _more_
     *
     * @return _more_
     */
    public Object getExtraProperty(Object key) {
        return extraProperties.get(key);
    }



    /**
     * Set the Protocol property.
     *
     * @param value The new value for Protocol
     */
    public void setProtocol(String value) {
        protocol = value;
    }

    /**
     * Get the Protocol property.
     *
     * @return The Protocol
     */
    public String getProtocol() {
        return protocol;
    }


    /**
     * Set the Secure property.
     *
     * @param value The new value for Secure
     */
    public void setSecure(boolean value) {
        secure = value;
    }

    /**
     * Get the Secure property.
     *
     * @return The Secure
     */
    public boolean getSecure() {
        return secure;
    }

    /**
     * Set the CheckingAuthMethod property.
     *
     * @param value The new value for CheckingAuthMethod
     */
    public void setCheckingAuthMethod(boolean value) {
        this.checkingAuthMethod = value;
    }

    /**
     * Get the CheckingAuthMethod property.
     *
     * @return The CheckingAuthMethod
     */
    public boolean getCheckingAuthMethod() {
        return this.checkingAuthMethod;
    }

    /**
     * Set the ApiMethod property.
     *
     * @param value The new value for ApiMethod
     */
    public void setApiMethod(ApiMethod value) {
        this.apiMethod = value;
    }

    /**
     * Get the ApiMethod property.
     *
     * @return The ApiMethod
     */
    public ApiMethod getApiMethod() {
        return this.apiMethod;
    }

    /**
     * _more_
     *
     * @return _more_
     */
    public boolean responseInXml() {
        return getString(ARG_RESPONSE, "").equals(RESPONSE_XML);
    }

    /**
     *  Set the HtmlTemplateId property.
     *
     *  @param value The new value for HtmlTemplateId
     */
    public void setHtmlTemplateId(String value) {
        htmlTemplateId = value;
    }

    /**
     *  Get the HtmlTemplateId property.
     *
     *  @return The HtmlTemplateId
     */
    public String getHtmlTemplateId() {
        if(htmlTemplateId!=null) return htmlTemplateId;
        return getString(ARG_TEMPLATE,"");
    }


    public String getLanguage() {
        if(exists(ARG_LANGUAGE)) {
            return getString(ARG_LANGUAGE,"");
        }
        User       user     = getUser();
        String     language = user.getLanguage();
        return language;
    }

    /**
     * Set the PageStyle property.
     *
     * @param value The new value for PageStyle
     */
    public void setPageStyle(PageStyle value) {
        pageStyle = value;
    }

    /**
     * Get the PageStyle property.
     *
     *
     * @param entry _more_
     * @return The PageStyle
     */
    public PageStyle getPageStyle(Entry entry) {
        if (pageStyle == null) {
            pageStyle = repository.doMakePageStyle(this, entry);
        }
        return pageStyle;
    }




}
