/*
 * Copyright 2008-2011 Jeff McWhirter/ramadda.org
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
 * 
 */

package org.ramadda.repository;


import org.ramadda.repository.auth.*;


import ucar.unidata.util.HtmlUtil;

import java.io.InputStream;

import java.util.ArrayList;
import java.util.Date;


import java.util.Hashtable;
import java.util.List;


/**
 */

public class Result {

    /** _more_ */
    public static final int RESPONSE_OK = 200;

    /** _more_ */
    public static final int RESPONSE_NOTFOUND = 404;

    /** _more_ */
    public static final int RESPONSE_UNAUTHORIZED = 401;

    /** _more_ */
    public static final int RESPONSE_INTERNALERROR = 500;


    /** _more_ */
    public static String TYPE_HTML = "text/html";

    /** _more_ */
    private String redirectUrl;

    /** _more_ */
    public static String TYPE_XML = "text/xml";

    /** _more_ */
    public static String TYPE_CSV = "text/csv";


    /** _more_ */
    private byte[] content;

    /** _more_ */
    private String title = "";

    /** _more_ */
    private String mimeType = "text/html";

    /** _more_ */
    private boolean shouldDecorate = true;

    /** _more_ */
    private Hashtable properties = new Hashtable();

    /** _more_ */
    private InputStream inputStream;

    /** _more_ */
    private boolean cacheOk = false;

    /** _more_ */
    private Date lastModified;


    /** _more_ */
    private int responseCode = RESPONSE_OK;

    /** _more_ */
    private List<String> httpHeaderArgs;

    /** _more_ */
    private boolean needToWrite = true;


    /** _more_ */
    public String bottomHtml = "";

    /** _more_ */
    private AuthorizationMethod authorizationMethod;

    /**
     * _more_
     */
    public Result() {}


    /**
     * _more_
     *
     * @param authorizationMethod _more_
     */
    public Result(AuthorizationMethod authorizationMethod) {
        this.authorizationMethod = authorizationMethod;
    }


    /**
     * _more_
     *
     * @param redirectUrl _more_
     */
    public Result(RequestUrl redirectUrl) {
        this(redirectUrl.toString());
    }


    /**
     * _more_
     *
     * @param redirectUrl _more_
     */
    public Result(String redirectUrl) {
        this.redirectUrl    = redirectUrl;
        this.shouldDecorate = false;
    }


    /**
     * _more_
     *
     * @param title _more_
     * @param content _more_
     * @param foo _more_
     */
    public Result(String title, byte[] content, boolean foo) {
        this(title, content, TYPE_HTML);
    }

    /**
     * _more_
     *
     * @param content _more_
     */
    public Result(StringBuffer content) {
        this("", content);
    }

    /**
     * _more_
     *
     * @param title _more_
     * @param content _more_
     */
    public Result(String title, StringBuffer content) {
        this(title, content.toString().getBytes(), TYPE_HTML);
    }


    /**
     * _more_
     *
     * @param content _more_
     * @param mime _more_
     */
    public Result(String content, String mime) {
        this("", content.getBytes(), mime);
    }

    /**
     * _more_
     *
     * @param title _more_
     * @param content _more_
     * @param mimeType _more_
     */
    public Result(String title, StringBuffer content, String mimeType) {
        this(title, content.toString().getBytes(), mimeType);
    }

    /**
     * _more_
     *
     * @param title _more_
     * @param content _more_
     * @param mimeType _more_
     */
    public Result(String title, byte[] content, String mimeType) {
        this(title, content, mimeType, true);
    }

    /**
     * _more_
     *
     * @param title _more_
     * @param inputStream _more_
     * @param mimeType _more_
     */
    public Result(InputStream inputStream, String mimeType) {
        this("", inputStream, mimeType);
    }

    /**
     * _more_
     *
     * @param title _more_
     * @param inputStream _more_
     * @param mimeType _more_
     */
    public Result(String title, InputStream inputStream, String mimeType) {
        this.title          = title;
        this.inputStream    = inputStream;
        this.mimeType       = mimeType;
        this.shouldDecorate = false;
    }


    /**
     *
     * _more_
     *
     * @param title _more_
     * @param content _more_
     * @param mimeType _more_
     * @param shouldDecorate _more_
     */
    public Result(String title, byte[] content, String mimeType,
                  boolean shouldDecorate) {
        this.content        = content;
        this.title          = title;
        this.mimeType       = mimeType;
        this.shouldDecorate = shouldDecorate;
    }


    /**
     * _more_
     *
     * @return _more_
     */
    public static Result makeNoOpResult() {
        Result result = new Result();
        result.setNeedToWrite(false);
        return result;
    }


    /**
     * Set the NeedToWrite property.
     *
     * @param value The new value for NeedToWrite
     */
    public void setNeedToWrite(boolean value) {
        needToWrite = value;
    }

    /**
     * Get the NeedToWrite property.
     *
     * @return The NeedToWrite
     */
    public boolean getNeedToWrite() {
        return needToWrite;
    }


    /**
     * _more_
     *
     * @param name _more_
     * @param value _more_
     */
    public void putProperty(String name, Object value) {
        properties.put(name, value);
    }

    /**
     * _more_
     *
     * @param name _more_
     *
     * @return _more_
     */
    public Object getProperty(String name) {
        return properties.get(name);
    }

    /**
     * _more_
     *
     * @return _more_
     */
    public boolean isHtml() {
        return (mimeType != null) && mimeType.equals(TYPE_HTML);
    }

    /**
     * _more_
     *
     * @return _more_
     */
    public boolean isXml() {
        return mimeType.equals(TYPE_XML);
    }


    /**
     * _more_
     *
     * @return _more_
     */
    public boolean isCsv() {
        return mimeType.equals(TYPE_CSV);
    }


    /**
     * Set the Content property.
     *
     * @param value The new value for Content
     */
    public void setContent(byte[] value) {
        content = value;
    }

    /**
     * Get the Content property.
     *
     * @return The Content
     */
    public byte[] getContent() {
        return content;
    }



    /**
     * Set the Title property.
     *
     * @param value The new value for Title
     */
    public void setTitle(String value) {
        title = value;
    }

    /**
     * Get the Title property.
     *
     * @return The Title
     */
    public String getTitle() {
        return title;
    }

    /**
     * Set the MimeType property.
     *
     * @param value The new value for MimeType
     */
    public void setMimeType(String value) {
        mimeType = value;
    }

    /**
     * Get the MimeType property.
     *
     * @return The MimeType
     */
    public String getMimeType() {
        return mimeType;
    }



    /**
     * Set the ShouldDecorate property.
     *
     * @param value The new value for ShouldDecorate
     */
    public void setShouldDecorate(boolean value) {
        shouldDecorate = value;
    }

    /**
     * Get the ShouldDecorate property.
     *
     * @return The ShouldDecorate
     */
    public boolean getShouldDecorate() {
        if (shouldDecorate && (mimeType != null) && (mimeType.length() > 0)
                && (mimeType.indexOf("html") < 0)) {
            return false;
        }
        return shouldDecorate;
    }

    /**
     * Set the InputStream property.
     *
     * @param value The new value for InputStream
     */
    public void setInputStream(InputStream value) {
        inputStream = value;
    }

    /**
     * Get the InputStream property.
     *
     * @return The InputStream
     */
    public InputStream getInputStream() {
        return inputStream;
    }

    /**
     *  Set the RedirectUrl property.
     *
     *  @param value The new value for RedirectUrl
     */
    public void setRedirectUrl(String value) {
        redirectUrl = value;
    }

    /**
     *  Get the RedirectUrl property.
     *
     *  @return The RedirectUrl
     */
    public String getRedirectUrl() {
        return redirectUrl;
    }

    /**
     * Set the CacheOk property.
     *
     * @param value The new value for CacheOk
     */
    public void setCacheOk(boolean value) {
        cacheOk = value;
    }

    /**
     * Get the CacheOk property.
     *
     * @return The CacheOk
     */
    public boolean getCacheOk() {
        return cacheOk;
    }

    /**
     * Set the ResponseCode property.
     *
     * @param value The new value for ResponseCode
     */
    public void setResponseCode(int value) {
        responseCode = value;
    }

    /**
     * Get the ResponseCode property.
     *
     * @return The ResponseCode
     */
    public int getResponseCode() {
        return responseCode;
    }


    /**
     * _more_
     *
     * @param filename _more_
     */
    public void setReturnFilename(String filename) {
        filename = filename.replaceAll(" ","_");
        addHttpHeader("Content-disposition",
                      "attachment; filename=" + filename);
    }



    /**
     * _more_
     *
     * @param name _more_
     * @param value _more_
     */
    public void addHttpHeader(String name, String value) {
        if (httpHeaderArgs == null) {
            httpHeaderArgs = new ArrayList<String>();
        }
        httpHeaderArgs.add(name);
        httpHeaderArgs.add(value);
    }


    /**
     * Set the HttpHeaderArgs property.
     *
     * @param value The new value for HttpHeaderArgs
     */
    public void setHttpHeaderArgs(List<String> value) {
        httpHeaderArgs = value;
    }

    /**
     * Get the HttpHeaderArgs property.
     *
     * @return The HttpHeaderArgs
     */
    public List<String> getHttpHeaderArgs() {
        return httpHeaderArgs;
    }


    /**
     * _more_
     *
     * @param name _more_
     * @param value _more_
     */
    public void addCookie(String name, String value) {
        addHttpHeader(HtmlUtil.HTTP_SET_COOKIE, name + "=" + value);
    }

    /**
     * Set the BottomHtml property.
     *
     * @param value The new value for BottomHtml
     */
    public void setBottomHtml(String value) {
        bottomHtml = value;
    }

    /**
     * Get the BottomHtml property.
     *
     * @return The BottomHtml
     */
    public String getBottomHtml() {
        return bottomHtml;
    }

    /**
     * Set the AuthorizationMethod property.
     *
     * @param value The new value for AuthorizationMethod
     */
    public void setAuthorizationMethod(AuthorizationMethod value) {
        this.authorizationMethod = value;
    }

    /**
     * Get the AuthorizationMethod property.
     *
     * @return The AuthorizationMethod
     */
    public AuthorizationMethod getAuthorizationMethod() {
        return this.authorizationMethod;
    }

    /**
     *  Set the LastModified property.
     *
     *  @param value The new value for LastModified
     */
    public void setLastModified(Date value) {
        this.lastModified = value;
    }

    /**
     *  Get the LastModified property.
     *
     *  @return The LastModified
     */
    public Date getLastModified() {
        return this.lastModified;
    }



}
