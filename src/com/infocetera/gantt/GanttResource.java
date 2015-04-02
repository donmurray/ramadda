/**
* Copyright (c) 2008-2015 Geode Systems LLC
* This Software is licensed under the Geode Systems RAMADDA License available in the source distribution in the file 
* ramadda_license.txt. The above copyright notice shall be included in all copies or substantial portions of the Software.
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

    /** _more_ */
    public static final int ROWHEIGHT = 40;
    //  private Color color = new Color (204, 255, 255);

    /** _more_ */
    private Color color = new Color(0, 128, 192);

    /** _more_ */
    private GanttView view;

    /** _more_ */
    Vector tasks = new Vector();

    /** _more_ */
    String name;

    /** _more_ */
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
