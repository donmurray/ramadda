/*
 * Copyright (c) 2008-2015 Geode Systems LLC
 * This Software is licensed under the Geode Systems RAMADDA License available in the source distribution in the file 
 * ramadda_license.txt. The above copyright notice shall be included in all copies or substantial portions of the Software.
 */

package org.ramadda.util;


import org.apache.commons.httpclient.*;
import org.apache.commons.httpclient.methods.PostMethod;
import org.apache.commons.httpclient.methods.multipart.*;
import org.apache.commons.httpclient.params.HttpClientParams;

import org.apache.http.HttpEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.mime.MultipartEntityBuilder;
import org.apache.http.entity.mime.content.FileBody;
import org.apache.http.entity.mime.content.StringBody;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;

import ucar.unidata.util.*;



import ucar.unidata.util.IOUtil;
import ucar.unidata.util.LogUtil;
import ucar.unidata.util.Misc;
import ucar.unidata.util.WrapperException;

import java.awt.*;
import java.awt.event.*;


import java.io.*;

import java.net.URL;

import java.util.ArrayList;
import java.util.List;

import javax.swing.*;
import javax.swing.event.*;
import javax.swing.text.*;
import javax.swing.tree.*;


/**
 * Class HttpFormEntry Represents a http form post input field.
 * Originally from the ucar.unidata.ui package
 *
 */
public class HttpFormField {

    /** Hidden input field type */
    public static final int TYPE_HIDDEN = 0;

    /** Normal input field type */
    public static final int TYPE_INPUT = 1;

    /** Multi-line input field type */
    public static final int TYPE_AREA = 2;

    /** Just a label in the gui */
    public static final int TYPE_LABEL = 3;

    /** Just a label in the gui */
    public static final int TYPE_FILE = 4;


    /** The type of input field */
    private int type;

    /** The http post name */
    private String name;

    /** THe label in the gui */
    private String label;

    /** The initial value */
    private String value;

    /** How many rows if this is a text area */
    private int rows;

    /** How many columns if this is a text area */
    private int cols;

    /** The component. (e.g., JTextField, JTextArea) */
    private JComponent component;

    /** Is this field required */
    private boolean required = true;

    /** file part source */
    private PartSource filePartSource;

    /** filename */
    private String fileName;


    /**
     * Create an entry that already holds the byte contents of a file.
     * Having an entry like this will result in a multi-part post
     *
     * @param name The name of the file
     * @param fileName filename - this is the name that is posted
     * @param bytes the bytes
     */
    public HttpFormField(String name, final String fileName,
                         final byte[] bytes) {
        this.name           = name;
        type                = TYPE_FILE;
        this.filePartSource = new PartSource() {
            public InputStream createInputStream() {
                return new ByteArrayInputStream(bytes);
            }
            public String getFileName() {
                return fileName;
            }
            public long getLength() {
                return bytes.length;
            }
        };
    }


    /**
     * _more_
     *
     * @param name _more_
     * @param label _more_
     * @param value _more_
     */
    public HttpFormField(String name, String label, String value) {
        this(TYPE_INPUT, name, label, value);
    }


    /**
     * _more_
     *
     * @param name _more_
     * @param value _more_
     *
     * @return _more_
     */
    public static HttpFormField hidden(String name, String value) {
        return new HttpFormField(TYPE_HIDDEN, name, "", value);
    }

    /**
     * Create the entry
     *
     * @param name The name
     * @param label The label
     */
    public HttpFormField(String name, String label) {
        this(TYPE_INPUT, name, label);
    }

    /**
     * Create the entry
     *
     *
     * @param type The type of this entry
     * @param name The name
     * @param label The label
     */
    public HttpFormField(int type, String name, String label) {
        this(type, name, label, "", 1, 30);
    }

    /**
     * Create the entry
     *
     *
     * @param type The type of this entry
     * @param name The name
     * @param label The label
     * @param value Initial value
     */
    public HttpFormField(int type, String name, String label, String value) {
        this(type, name, label, value, 1, 30, true);
    }

    /**
     * Create the entry
     *
     *
     * @param type The type of this entry
     * @param name The name
     * @param label The label
     * @param value Initial value
     * @param required Is this entry required
     */
    public HttpFormField(int type, String name, String label, String value,
                         boolean required) {
        this(type, name, label, value, 1, 30, required);
    }

    /**
     * Create the entry
     *
     *
     * @param type The type of this entry
     * @param name The name
     * @param label The label
     * @param rows How may rows in the text area
     * @param cols How many columns in the text area
     */
    public HttpFormField(int type, String name, String label, int rows,
                         int cols) {
        this(type, name, label, "", rows, cols);
    }

    /**
     * Create the entry
     *
     *
     * @param type The type of this entry
     * @param name The name
     * @param label The label
     * @param value Initial value
     * @param rows How many rows
     * @param cols How many cols
     */
    public HttpFormField(int type, String name, String label, String value,
                         int rows, int cols) {
        this(type, name, label, value, rows, cols, true);
    }

    /**
     * Create the entry
     *
     *
     * @param type The type of this entry
     * @param name The name
     * @param label The label
     * @param value Initial value
     * @param rows How many rows
     * @param cols How many cols
     * @param required Is this entry required
     */
    public HttpFormField(int type, String name, String label, String value,
                         int rows, int cols, boolean required) {
        this.type     = type;
        this.name     = name;
        this.label    = label;
        this.value    = value;
        this.rows     = rows;
        this.cols     = cols;
        this.required = required;
    }


    /**
     * Is this entry ok. That is, has their been input if it is required
     *
     * @return Is ok
     */
    public boolean ok() {
        if (type == TYPE_LABEL) {
            return true;
        }

        return !required || (getValue().trim().length() > 0);
    }





    /**
     * Get the value the user entered.
     *
     * @return The input value
     */
    public String getValue() {
        if (type == TYPE_HIDDEN) {
            return value;
        }
        if (type == TYPE_LABEL) {
            return null;
        }
        if (component != null) {
            return ((JTextComponent) component).getText();
        }

        return value;
    }

    /**
     * Set the text value to the given newValue
     *
     * @param newValue The new text
     */
    public void setValue(String newValue) {
        if (type == TYPE_HIDDEN) {
            this.value = newValue;
        }
        if (type == TYPE_LABEL) {}
        else if (component != null) {
            ((JTextComponent) component).setText(newValue);
        }
    }

    /**
     * Get the name
     *
     * @return The name
     */
    public String getName() {
        return name;
    }

    /**
     * Get the label
     *
     * @return The label
     */
    public String getLabel() {
        return label;
    }




    /**
     * Check the entries to make sure they have been filled in
     *
     * @param entries list of entries
     *
     * @return false if some are not filled in.
     */
    public static boolean checkEntries(List entries) {
        for (int i = 0; i < entries.size(); i++) {
            HttpFormField formEntry = (HttpFormField) entries.get(i);
            if ( !formEntry.ok()) {
                LogUtil.userMessage("The entry: \"" + formEntry.getLabel()
                                    + "\" is required");

                return false;
            }
        }

        return true;
    }




    /**
     * Get the file part
     *
     * @return the file part
     */
    private FilePart getFilePart() {
        if (filePartSource == null) {
            final String file = getValue();

            return new FilePart(getName(), new PartSource() {
                public InputStream createInputStream() {
                    try {
                        return IOUtil.getInputStream(file);
                    } catch (Exception exc) {
                        throw new WrapperException("Reading file:" + file,
                                exc);
                    }
                }
                public String getFileName() {
                    return new File(file).getName();
                }
                public long getLength() {
                    return new File(file).length();
                }
            });
        }

        return new FilePart(getName(), filePartSource);
    }

    /**
     * Post the file
     *
     * @return true if posted
     */
    public boolean okToPost() {
        if (type == TYPE_LABEL) {
            return false;
        }

        if (type == TYPE_FILE) {
            if (filePartSource == null) {
                return new File(getValue()).exists();
            }
        }

        return true;
    }


    /**
     * Post the given entries tot he given url
     *
     * @param entries The entries
     * @param urlPath The url to post to
     *
     * @return 2 element array. First element is non-null if there was an error.
     * Second element is non-null if no error. This is the returned html.
     */
    public static String[] doPost(List entries, String urlPath) {
        return doPost(entries, urlPath, true);
    }

    /**
     * _more_
     *
     * @param entries _more_
     * @param urlPath _more_
     * @param followRedirect _more_
     *
     * @return _more_
     */
    public static String[] doPost(List entries, String urlPath,
                                  boolean followRedirect) {

        return doPost(entries, urlPath, null, followRedirect);
    }

    /**
     * _more_
     *
     * @param entries _more_
     * @param urlPath _more_
     * @param params _more_
     * @param followRedirect _more_
     *
     * @return _more_
     */
    public static String[] doPost(List entries, String urlPath,
                                  HttpClientParams params,
                                  boolean followRedirect) {
        try {
            PostMethod postMethod = null;
            int        numTries   = 0;
            while (numTries++ < 5) {
                postMethod = getMethod(entries, urlPath);
                HttpClient client = new HttpClient();
                if (params != null) {
                    client.setParams(params);
                }
                client.executeMethod(postMethod);
                if ((postMethod.getStatusCode() >= 300)
                        && (postMethod.getStatusCode() <= 399)) {
                    Header locationHeader =
                        postMethod.getResponseHeader("location");
                    if (locationHeader == null) {
                        return new String[] {
                            "Error: No 'location' given on the redirect",
                            null };
                    }
                    if ( !followRedirect) {
                        return new String[] { locationHeader.getValue(),
                                null };
                    }

                    //Keep trying with the new location
                    urlPath = locationHeader.getValue();
                    if (postMethod.getStatusCode() == 301) {
                        System.err.println(
                            "Warning: form post has been permanently moved to:"
                            + urlPath);
                    }

                    continue;
                }

                //Done
                break;
            }
            String result =
                IOUtil.readContents(postMethod.getResponseBodyAsStream());
            System.err.println("Status:" + postMethod.getStatusCode());
            if (postMethod.getStatusCode() >= 300) {
                return new String[] { result, null };
            } else {
                return new String[] { null, result };
            }
        } catch (Exception exc) {
            System.err.println("EXC:" + exc);

            throw new WrapperException("doing post", exc);
        }

    }


    /**
     * _more_
     *
     * @param entries _more_
     * @param urlPath _more_
     *
     * @return _more_
     */
    private static PostMethod getMethod(List entries, String urlPath) {
        PostMethod postMethod  = new PostMethod(urlPath);
        boolean    anyFiles    = false;
        int        count       = 0;
        List       goodEntries = new ArrayList();
        for (int i = 0; i < entries.size(); i++) {
            HttpFormField formEntry = (HttpFormField) entries.get(i);
            if ( !formEntry.okToPost()) {
                continue;
            }
            goodEntries.add(entries.get(i));
            if (formEntry.type == TYPE_FILE) {
                anyFiles = true;
            }
        }


        if (anyFiles) {
            Part[] parts = new Part[goodEntries.size()];
            for (int i = 0; i < goodEntries.size(); i++) {
                HttpFormField formEntry = (HttpFormField) goodEntries.get(i);
                if (formEntry.type == TYPE_FILE) {
                    parts[i] = formEntry.getFilePart();
                } else {
                    //Not sure why but we have seen a couple of times
                    //the byte value '0' gets into one of these strings
                    //This causes an error in the StringPart.
                    //                    System.err.println("
                    String value = formEntry.getValue();
                    char   with  = new String(" ").charAt(0);
                    while (value.indexOf(0) >= 0) {
                        value = value.replace((char) 0, with);
                    }
                    parts[i] = new StringPart(formEntry.getName(), value);
                }
            }
            postMethod.setRequestEntity(new MultipartRequestEntity(parts,
                    postMethod.getParams()));
        } else {
            for (int i = 0; i < goodEntries.size(); i++) {
                HttpFormField formEntry = (HttpFormField) goodEntries.get(i);
                postMethod.addParameter(
                    new NameValuePair(
                        formEntry.getName(), formEntry.getValue()));
            }
        }

        return postMethod;
    }


}
