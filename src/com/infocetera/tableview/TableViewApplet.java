/**
* Copyright (c) 2008-2015 Geode Systems LLC
* This Software is licensed under the Geode Systems RAMADDA License available in the source distribution in the file 
* ramadda_license.txt. The above copyright notice shall be included in all copies or substantial portions of the Software.
*/

package com.infocetera.tableview;


import com.infocetera.util.*;

import com.infocetera.util.GuiUtils;

import java.applet.*;

import java.awt.*;

import java.io.*;

import java.net.*;




/**
 * Class TableViewApplet _more_
 *
 *
 * @author IDV Development Team
 * @version $Revision: 1.3 $
 */
public class TableViewApplet extends IfcApplet {

    /** _more_ */
    TableView tableView;

    /**
     * _more_
     */
    public TableViewApplet() {
        System.err.println("IN THERE");
    }

    /**
     * _more_
     *
     * @return _more_
     */
    public Component doMakeContents() {
        try {
            tableView = new TableView(this);

            return tableView.doMakeContents();
        } catch (Exception e) {
            e.printStackTrace();

            return new Label("An error has occurred (234):" + e);
        }
    }



}
