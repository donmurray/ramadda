/**
* Copyright (c) 2008-2015 Geode Systems LLC
* This Software is licensed under the Geode Systems RAMADDA License available in the source distribution in the file 
* ramadda_license.txt. The above copyright notice shall be included in all copies or substantial portions of the Software.
*/

/**
 * (C) 1999-2002  WTS Systems, L.L.C.
 *   All rights reserved
 */



package com.infocetera.chat;


import com.infocetera.util.*;



import java.applet.*;

import java.awt.*;

import java.util.StringTokenizer;


/**
 * Class DisplayApplet _more_
 *
 *
 * @author IDV Development Team
 * @version $Revision: 1.3 $
 */
public class DisplayApplet extends SocketApplet {

    /** _more_ */
    DisplayCanvas canvas;

    /**
     * _more_
     */
    public void init() {
        canvas = new DisplayCanvas(this);
        setLayout(new BorderLayout());
        setBackground(Color.gray);
        add("Center", GuiUtils.inset(canvas.doMakeContents(), 2, 2));
        //    canvas.processGfxList (getProperty ("GFXLIST"));
    }

    /** _more_ */
    public static String msg = "HELLO";

    /** _more_ */
    int cnt = 0;

    /**
     * _more_
     *
     * @return _more_
     */
    public String getMsg() {
        return "MSG: " + (cnt++);
    }


}
