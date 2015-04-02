/**
* Copyright (c) 2008-2015 Geode Systems LLC
* This Software is licensed under the Geode Systems RAMADDA License available in the source distribution in the file 
* ramadda_license.txt. The above copyright notice shall be included in all copies or substantial portions of the Software.
*/

/**
 * (C) 1999-2002  WTS Systems, L.L.C.
 *   All rights reserved
 */


package com.infocetera.util;


import java.awt.*;
import java.awt.event.*;

import javax.swing.*;


/**
 * Class OkCancelDialog _more_
 *
 *
 * @author IDV Development Team
 * @version $Revision: 1.3 $
 */
public class OkCancelDialog extends JDialog implements ActionListener {

    /** _more_ */
    public String dialogText;

    /** _more_ */
    Component contents;

    /** _more_ */
    public boolean okPressed = false;

    /** _more_ */
    Button okBtn;

    /** _more_ */
    Button cancelBtn;

    /** _more_ */
    boolean doClose = false;

    /** _more_ */
    public boolean doCancel = true;

    /**
     * _more_
     *
     * @param f _more_
     * @param contents _more_
     */
    public OkCancelDialog(JFrame f, Component contents) {
        super(f, true);
        this.contents = contents;
    }

    /**
     * _more_
     *
     * @param f _more_
     * @param contents _more_
     * @param doClose _more_
     */
    public OkCancelDialog(JFrame f, Component contents, boolean doClose) {
        super(f, true);
        this.doClose  = doClose;
        this.contents = contents;
    }

    /**
     * _more_
     *
     * @param f _more_
     * @param contents _more_
     * @param doClose _more_
     * @param modal _more_
     */
    public OkCancelDialog(JFrame f, Component contents, boolean doClose,
                          boolean modal) {
        super(f, modal);
        this.doClose  = doClose;
        this.contents = contents;
    }


    /** _more_ */
    public Dimension size;

    /**
     * _more_
     */
    public void init() {
        Component bottom;
        if (doClose) {
            okBtn  = new Button("Close");
            bottom = okBtn;
        } else {
            okBtn     = new Button("Ok");
            cancelBtn = new Button("Cancel");
            cancelBtn.addActionListener(this);
            if (doCancel) {
                bottom = GuiUtils.doLayout(new Component[] { okBtn,
                        cancelBtn }, 2, GuiUtils.DS_YY, GuiUtils.DS_N);
            } else {
                bottom = GuiUtils.doLayout(new Component[] { okBtn }, 2,
                                           GuiUtils.DS_YY, GuiUtils.DS_N);
            }
        }
        okBtn.addActionListener(this);

        setLayout(new BorderLayout());
        if (contents != null) {
            add("Center", contents);
        }

        Panel wrapper = new Panel(new FlowLayout());
        wrapper.add(bottom);
        add("South", wrapper);
        pack();
        if (size != null) {
            setSize(size);
        }
        show();
    }



    /**
     * _more_
     *
     * @param event _more_
     */
    public void actionPerformed(ActionEvent event) {
        Object src = event.getSource();
        if (src == okBtn) {
            okPressed = true;
            dispose();
        } else if (src == cancelBtn) {
            dispose();
        }
    }

}
