/**
* Copyright (c) 2008-2015 Geode Systems LLC
* This Software is licensed under the Geode Systems RAMADDA License available in the source distribution in the file 
* ramadda_license.txt. The above copyright notice shall be included in all copies or substantial portions of the Software.
*/

package com.infocetera.graph;


import com.infocetera.util.GuiUtils;



import com.infocetera.util.IfcApplet;

import java.applet.*;

import java.awt.*;

import java.util.Hashtable;
import java.util.Vector;

import javax.swing.*;


/**
 *  Just a simple wrapper around a GraphView
 */
public class GraphApplet extends IfcApplet {

    /** _more_ */
    public static final String PARAM_DATAURL = "dataurl";

    /** _more_ */
    private String dataUrl;

    /** _more_ */
    GraphView mainView;

    /** _more_ */
    Vector views = new Vector();

    /** _more_ */
    Vector graphs = new Vector();

    /** _more_ */
    Hashtable loadedNodes = new Hashtable();

    /**
     * _more_
     */
    public GraphApplet() {}



    /**
     *  Initialize the GraphView. If there are any erros then show a Label
     *  with the error message.
     */
    public void initInner() {
        super.initInner();
        dataUrl = getFullUrl(getParameter(PARAM_DATAURL));
        System.err.println("DATA URL:" + dataUrl);
        if (dataUrl == null) {
            System.err.println("No data url provided");
        }
        Component inner = null;
        setLayout(new BorderLayout());
        try {
            mainView = new GraphView(this, true);
            views.addElement(mainView);
            inner = mainView.getContents();
            reload();
            if (getParameter("shownodelist", true)) {
                mainView.handleFunction(mainView.CMD_SHOWNODELIST, "", null,
                                        null, null);
            }
        } catch (Exception e) {
            System.err.println("Failed to create GraphView\n"
                               + e.getMessage());
            e.printStackTrace();
            inner = new Label("An error has occurred:" + e.getMessage());
        }
        add("Center", inner);
    }



    /**
     * _more_
     *
     * @return _more_
     */
    public GraphView makeView() {
        GraphView graphView = new GraphView(this, false);
        views.addElement(graphView);

        return graphView;
    }


    /**
     * _more_
     */
    public void reload() {
        graphs      = new Vector();
        loadedNodes = new Hashtable();
        for (int i = 0; i < views.size(); i++) {
            GraphView view = (GraphView) views.elementAt(i);
            view.initializeState();
        }
        mainView.loadNodes(dataUrl);
    }

    /**
     * _more_
     *
     * @param id _more_
     *
     * @return _more_
     */
    public boolean isNodeLoaded(String id) {
        return loadedNodes.get(id) != null;
    }

    /**
     * _more_
     *
     * @param id _more_
     */
    public void setNodeLoaded(String id) {
        loadedNodes.put(id, id);
    }

    /**
     * _more_
     *
     * @param xml _more_
     * @param fromView _more_
     */
    public void addGraph(String xml, GraphView fromView) {
        graphs.addElement(xml);
        for (int i = 0; i < views.size(); i++) {
            GraphView view = (GraphView) views.elementAt(i);
            if (view != fromView) {
                view.processGraphXml(xml);
            }
        }
    }

    /**
     * _more_
     *
     * @param view _more_
     */
    public void removeView(GraphView view) {
        views.removeElement(view);
    }

    /**
     * _more_
     */
    public void stop() {
        for (int i = 0; i < views.size(); i++) {
            ((GraphView) views.elementAt(i)).stop();
        }
        super.stop();
    }



}
