/**
* Copyright (c) 2008-2015 Geode Systems LLC
* This Software is licensed under the Geode Systems RAMADDA License available in the source distribution in the file 
* ramadda_license.txt. The above copyright notice shall be included in all copies or substantial portions of the Software.
*/

package com.infocetera.gantt;


import com.infocetera.util.*;

import java.applet.*;

import java.awt.*;

import java.io.*;

import java.net.*;

import java.util.*;




/**
 * Class GanttApplet _more_
 *
 *
 * @author IDV Development Team
 * @version $Revision: 1.3 $
 */
public class GanttApplet extends IfcApplet {

    /** _more_ */
    GanttView ganttView;

    /**
     * _more_
     */
    public GanttApplet() {}

    /**
     * _more_
     */
    public void initInner() {
        IfcApplet.debug = true;
        setLayout(new BorderLayout());
        ganttView = new GanttView(this);
        add("Center", ganttView.getContents());
    }

    /**
     * _more_
     */
    public void stop() {
        super.stop();
        if (ganttView != null) {
            ganttView.stop();
        }
    }


}
