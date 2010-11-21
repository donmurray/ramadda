/*
 * 
 * 
 * 
 * 
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

    /** _more_          */
    GanttView ganttView;

    /**
     * _more_
     */
    public GanttApplet() {}

    /**
     * _more_
     */
    public void initInner() {
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

