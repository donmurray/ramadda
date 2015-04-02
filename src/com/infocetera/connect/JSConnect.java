/**
* Copyright (c) 2008-2015 Geode Systems LLC
* This Software is licensed under the Geode Systems RAMADDA License available in the source distribution in the file 
* ramadda_license.txt. The above copyright notice shall be included in all copies or substantial portions of the Software.
*/

package com.infocetera.connect;


import com.infocetera.util.*;

import netscape.javascript.JSObject;


import java.applet.*;

import java.awt.*;

import java.util.StringTokenizer;


/**
 * Class JSConnect _more_
 *
 *
 * @author IDV Development Team
 * @version $Revision: 1.3 $
 */
public class JSConnect extends SocketApplet {

    /** _more_ */
    String directory = "";

    /** _more_ */
    String user = "NO_USER";

    /** _more_ */
    String markupid = null;

    /** _more_ */
    String sessionId = "BADID";


    /** _more_ */
    JSObject js;

    /**
     * _more_
     */
    public JSConnect() {}


    /**
     * _more_
     */
    public void init() {
        super.init();
        shouldTokenizeMsg = false;
        js                = JSObject.getWindow(this);
        directory         = "directory";
        try {
            directory = getParameter("directory");
            sessionId = getParameter("sessionid");
            myid      = getParameter("id");
            user      = getParameter("user");
            markupid  = getParameter("markupid");
        } catch (Exception exc) {}
    }

    /**
     * _more_
     *
     * @param msg _more_
     */
    public void handleMessage(String msg) {
        processMessage(msg);
    }


    /**
     * _more_
     *
     * @param msg _more_
     */
    public void write2(String msg) {
        System.err.println("OK");
    }


    /**
     * _more_
     *
     * @param msg _more_
     */
    public void processMessage(String msg) {
        System.err.println("JSConnect:(" + myid + ") " + msg);
        if (msg.startsWith("JS:")) {
            StringTokenizer st = new StringTokenizer(msg, "\n");
            while (st.hasMoreTokens()) {
                String sub = st.nextToken();
                if (sub.startsWith("JS:")) {
                    sub = sub.substring(3).trim();
                }
                evalJS(sub);
            }
        } else if (msg.startsWith("URL")) {
            String urlString = msg.substring(3);
            urlString = urlString.replace('\n', ';');
            try {
                loadUrl(urlString);
                //      evalJS("loadUrl('"+urlString+"');");
            } catch (Exception exc) {
                System.err.println("Error: " + exc);
            }
        }
    }




    /**
     * _more_
     */
    public void initConnection() {
        super.initConnection();
        System.err.println("ID: " + sessionId);

        write("START <" + directory + "><" + user + "><" + sessionId + ">");
        write("ID " + myid);
        if (markupid != null) {
            System.err.println("Writing get ");
            write("GET " + markupid);
        }
    }

    /**
     * _more_
     *
     * @param s _more_
     */
    public void evalJS(String s) {
        try {
            System.err.println("Evaluating javascript: " + s + "\n");
            js.eval(s);
        } catch (Exception exc) {
            System.err.println("** Error evaluating javascript: " + s + "\n"
                               + exc);
        }
    }


    /**
     * _more_
     *
     * @param urlString _more_
     */
    public void loadUrl(String urlString) {
        urlString = urlString.trim();
        String[] args = { urlString };
        try {
            //      showUrl(urlString,null);
            //      js.eval("location="+urlString);
            js.call("loadUrl", args);
        } catch (Exception exc) {
            System.err.println("** Error id= " + myid + " : " + urlString
                               + "\n" + exc);
        }
    }








}
