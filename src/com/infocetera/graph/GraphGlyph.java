/**
* Copyright (c) 2008-2015 Geode Systems LLC
* This Software is licensed under the Geode Systems RAMADDA License available in the source distribution in the file 
* ramadda_license.txt. The above copyright notice shall be included in all copies or substantial portions of the Software.
*/

package com.infocetera.graph;


import com.infocetera.util.GuiUtils;
import com.infocetera.util.XmlNode;

import java.awt.*;

import java.util.Hashtable;
import java.util.Vector;


/**
 * Class GraphGlyph _more_
 *
 *
 * @author IDV Development Team
 * @version $Revision: 1.3 $
 */
public class GraphGlyph {

    /** _more_ */
    static Hashtable fontMap = new Hashtable();

    /** _more_ */
    public static final String ATTR_ID = "id";

    /** _more_ */
    public String id;

    /** _more_ */
    public boolean dirty = true;

    /** _more_ */
    XmlNode xmlNode;

    /** _more_ */
    private XmlNode glyphType;

    /** _more_ */
    protected Vector types;

    /** _more_ */
    public GraphView graphView;


    /** _more_ */
    double scale = 1.0;

    /** _more_ */
    double levelScale = 1.0;

    /** _more_ */
    private String title;



    /**
     * _more_
     *
     * @param gv _more_
     * @param nodeNode _more_
     */
    public GraphGlyph(GraphView gv, XmlNode nodeNode) {
        graphView = gv;
        xmlNode   = nodeNode;
    }

    /**
     * _more_
     *
     * @return _more_
     */
    public String getLabel() {
        return graphView.getTitle(this);
    }

    /**
     * _more_
     *
     * @return _more_
     */
    public XmlNode getNodeXml() {
        return xmlNode;
    }

    /**
     * _more_
     *
     * @return _more_
     */
    public XmlNode getTypeXml() {
        return glyphType;
    }

    /**
     * _more_
     *
     * @param n _more_
     */
    public void setType(XmlNode n) {
        glyphType = n;
        types     = new Vector();
        Hashtable seen    = new Hashtable();
        XmlNode   tmpType = glyphType;
        while (tmpType != null) {
            if (seen.get(tmpType) != null) {
                break;
            }
            seen.put(tmpType, tmpType);
            types.addElement(tmpType);
            String parent = tmpType.getAttribute("parent", (String) null);
            if (parent == null) {
                break;
            }
            tmpType = graphView.getNodeType(parent, null);
        }
    }


    /**
     * _more_
     *
     * @param name _more_
     * @param dflt _more_
     *
     * @return _more_
     */
    public Color getAttr(String name, Color dflt) {
        String v = getAttr(name);
        if (v == null) {
            return dflt;
        }

        return GuiUtils.getColor(v);
    }


    /**
     * _more_
     *
     * @param name _more_
     * @param dflt _more_
     *
     * @return _more_
     */
    public int getAttr(String name, int dflt) {
        String v = getAttr(name);
        if (v == null) {
            return dflt;
        }

        return Integer.decode(v).intValue();
    }

    /**
     * _more_
     *
     * @param name _more_
     * @param dflt _more_
     *
     * @return _more_
     */
    public boolean getAttr(String name, boolean dflt) {
        String v = getAttr(name);
        if (v == null) {
            return dflt;
        }

        return new Boolean(v).booleanValue();
    }

    /**
     * _more_
     *
     * @param name _more_
     *
     * @return _more_
     */
    public String getAttr(String name) {
        return getAttr(name, (String) null);
    }


    /**
     * _more_
     *
     * @param name _more_
     * @param value _more_
     */
    public void setAttr(String name, String value) {
        xmlNode.setAttribute(name, value);
    }


    /**
     * _more_
     *
     * @param name _more_
     * @param dflt _more_
     *
     * @return _more_
     */
    public String getAttr(String name, String dflt) {
        String value = xmlNode.getAttribute(name, (String) null);
        if (value == null) {
            if (types != null) {
                for (int i = 0; (value == null) && (i < types.size()); i++) {
                    XmlNode tmpType = (XmlNode) types.elementAt(i);
                    value = (String) tmpType.getAttribute(name);
                }
            }
        }
        if (value == null) {
            value = graphView.getAttr(name);
        }

        return ((value == null)
                ? dflt
                : value);
    }

    /**
     * _more_
     *
     * @return _more_
     */
    public Vector getTypeList() {
        return types;
    }


    /**
     * _more_
     *
     * @param face _more_
     * @param style _more_
     * @param size _more_
     *
     * @return _more_
     */
    public static Font getFont(String face, int style, int size) {
        String key = face + "-" + style + "-" + size;
        Font   f   = (Font) fontMap.get(key);
        if (f == null) {
            f = new Font(face, style, size);
            fontMap.put(key, f);
        }

        return f;
    }




    /**
     * _more_
     *
     * @param s _more_
     */
    public void setScale(double s) {
        scale = s;
        dirty = true;
    }





}
