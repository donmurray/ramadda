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
 * Class TaskType _more_
 *
 *
 * @author IDV Development Team
 * @version $Revision: 1.3 $
 */
public class TaskType {

    /** _more_ */
    public static final int ROWHEIGHT = 40;

    /** _more_ */
    private Color fillColor = null;

    /** _more_ */
    private GanttView view;

    /** _more_ */
    Vector tasks = new Vector();

    /** _more_ */
    String name;

    /** _more_ */
    String id;

    /** _more_ */
    boolean milestone;



    /**
     * _more_
     *
     * @param view _more_
     * @param id _more_
     * @param name _more_
     * @param fillColor _more_
     * @param milestone _more_
     */
    public TaskType(GanttView view, String id, String name, Color fillColor,
                    boolean milestone) {
        this.view      = view;
        this.id        = id;
        this.name      = name;
        this.milestone = milestone;
        if (fillColor != null) {
            this.fillColor = fillColor;
        }
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
     * @return _more_
     */
    public boolean isMilestone() {
        return milestone;
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
    public Color getColor() {
        return fillColor;
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
