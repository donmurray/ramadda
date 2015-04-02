/**
* Copyright (c) 2008-2015 Geode Systems LLC
* This Software is licensed under the Geode Systems RAMADDA License available in the source distribution in the file 
* ramadda_license.txt. The above copyright notice shall be included in all copies or substantial portions of the Software.
*/

import netscape.javascript.JSObject;

import java.applet.*;

import java.awt.*;


/**
 * Class NS_JSConnect _more_
 *
 *
 * @author IDV Development Team
 * @version $Revision: 1.3 $
 */
public class NS_JSConnect extends JSConnect {

    /** _more_ */
    JSObject js;

    /**
     * _more_
     */
    public void init() {
        js = JSObject.getWindow(this);
        super.init();
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
