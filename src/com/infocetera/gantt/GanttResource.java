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


import com.infocetera.util.GuiUtils;

import java.awt.*;


import java.util.Vector;


/**
 * Class GanttResource _more_
 *
 *
 * @author IDV Development Team
 * @version $Revision: 1.3 $
 */
public class GanttResource {

    /** _more_          */
    public static final int ROWHEIGHT = 40;
    //  private Color color = new Color (204, 255, 255);

    /** _more_          */
    private Color color = new Color(0, 128, 192);

    /** _more_          */
    private GanttView view;

    /** _more_          */
    Vector tasks = new Vector();

    /** _more_          */
    String name;

    /** _more_          */
    String id;


    /**
     * _more_
     *
     * @param view _more_
     * @param id _more_
     * @param name _more_
     * @param color _more_
     */
    public GanttResource(GanttView view, String id, String name,
                         Color color) {
        this.view = view;
        this.id   = id;
        this.name = name;
        if (color != null) {
            this.color = color;
        }
    }

    /**
     * _more_
     *
     * @return _more_
     */
    public Color getColor() {
        return color;
    }

    /**
     * _more_
     *
     * @return _more_
     */
    public GanttView getView() {
        return view;
    }

    /**
     * _more_
     *
     * @param task _more_
     */
    public void addTask(GanttTask task) {
        tasks.addElement(task);
    }


    /**
     * _more_
     *
     * @return _more_
     */
    public String getName() {
        return name;
    }

    /**
     * _more_
     *
     * @return _more_
     */
    public String getId() {
        return id;
    }



}

