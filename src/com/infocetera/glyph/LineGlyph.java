/*
* Copyright 2008-2013 Geode Systems LLC
*
* Permission is hereby granted, free of charge, to any person obtaining a copy of this 
* software and associated documentation files (the "Software"), to deal in the Software 
* without restriction, including without limitation the rights to use, copy, modify, 
* merge, publish, distribute, sublicense, and/or sell copies of the Software, and to 
* permit persons to whom the Software is furnished to do so, subject to the following conditions:
* 
* The above copyright notice and this permission notice shall be included in all copies 
* or substantial portions of the Software.
* 
* THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, 
* INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR 
* PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE 
* FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR 
* OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER 
* DEALINGS IN THE SOFTWARE.
*/

/**
 * (C) 1999-2002  WTS Systems, L.L.C.
 *   All rights reserved
 */


package com.infocetera.glyph;


import com.infocetera.util.*;

import java.awt.Color;
import java.awt.Graphics;



import java.awt.Point;
import java.awt.Rectangle;


/**
 * Class LineGlyph _more_
 *
 *
 * @author IDV Development Team
 * @version $Revision: 1.3 $
 */
public class LineGlyph extends Glyph {

    //Have this width here for the htmlglyph

    /** _more_ */
    String width = null;

    /** _more_ */
    Point p1;

    /** _more_ */
    Point p2;

    /**
     * _more_
     *
     * @param x1 _more_
     * @param y1 _more_
     * @param x2 _more_
     * @param y2 _more_
     */
    public LineGlyph(int x1, int y1, int x2, int y2) {
        super("LINE");
        p1 = new Point(x1, y1);
        p2 = new Point(x2, y2);
    }

    /**
     * _more_
     *
     * @return _more_
     */
    public Rectangle getBounds() {
        Rectangle r = new Rectangle();
        if (p1.x < p2.x) {
            r.x     = p1.x;
            r.width = (p2.x - p1.x);
        } else {
            r.x     = p2.x;
            r.width = (p1.x - p2.x);
        }
        if (p1.y < p2.y) {
            r.y      = p1.y;
            r.height = (p2.y - p1.y);
        } else {
            r.y      = p2.y;
            r.height = (p1.y - p2.y);
        }

        return r;

    }

    /**
     * _more_
     *
     * @param p _more_
     * @param cnt _more_
     */
    public void setPoints(int[] p, int cnt) {
        if (p.length < 4) {
            return;
        }
        p1.x = p[0];
        p1.y = p[1];
        p2.x = p[2];
        p2.y = p[3];
    }

    /**
     * _more_
     *
     * @param g _more_
     * @param c _more_
     */
    public void paint(Graphics g, ScrollCanvas c) {
        paint(g, c, getColor());
    }

    /**
     * _more_
     *
     * @param g _more_
     * @param c _more_
     */
    public void paintHighlight(Graphics g, ScrollCanvas c) {
        paint(g, c, highlightColor);
    }

    /**
     * _more_
     *
     * @param g _more_
     * @param c _more_
     * @param color _more_
     */
    public void paint(Graphics g, ScrollCanvas c, Color color) {
        Point sp1 = c.scalePoint(p1);
        Point sp2 = c.scalePoint(p2);
        g.setColor(color);
        GuiUtils.drawLine(g, sp1.x, sp1.y, sp2.x, sp2.y,
                          c.scaleCoord(getWidth()));
        g.setColor(Color.black);
    }


    /**
     * _more_
     *
     * @param g _more_
     * @param c _more_
     */
    public void paintSelection(Graphics g, ScrollCanvas c) {
        Point sp1 = c.scalePoint(p1);
        Point sp2 = c.scalePoint(p2);
        g.setColor(Color.black);
        g.fillRect(sp1.x - H_SEL_WIDTH, sp1.y - H_SEL_WIDTH, SEL_WIDTH,
                   SEL_WIDTH);
        g.fillRect(sp2.x - H_SEL_WIDTH, sp2.y - H_SEL_WIDTH, SEL_WIDTH,
                   SEL_WIDTH);
    }


    /**
     * _more_
     *
     * @return _more_
     */
    public String getPositionAttr() {
        return makeAttr(ATTR_PTS,
                        p1.x + " " + p1.y + " " + p2.x + " " + p2.y);
    }


    /**
     * _more_
     *
     * @param x _more_
     * @param y _more_
     *
     * @return _more_
     */
    public double distance(int x, int y) {
        return ptSegDist((double) p1.x, (double) p1.y, (double) p2.x,
                         (double) p2.y, (double) x, (double) y);
    }

    /**
     * _more_
     *
     * @param x _more_
     * @param y _more_
     * @param pt _more_
     * @param correct _more_
     *
     * @return _more_
     */
    public String stretchTo(int x, int y, String pt, boolean correct) {
        if (PT_P1.equals(pt) || PT_UL.equals(pt)) {
            p1.x = x;
            p1.y = y;
        } else if (PT_P2.equals(pt) || PT_LR.equals(pt)) {
            p2.x = x;
            p2.y = y;
        } else {
            p1.x += x;
            p1.y += y;
            p2.x += x;
            p2.y += y;
        }


        return pt;
    }

    /**
     * _more_
     *
     * @param x _more_
     * @param y _more_
     *
     * @return _more_
     */
    public String getStretchPoint(int x, int y) {
        double d1  = distance(x, y, p1.x, p1.y);
        double d2  = distance(x, y, p2.x, p2.y);

        double min = ((d1 <= d2)
                      ? d1
                      : d2);
        if (min > MIN_DISTANCE_TO_STRETCH) {
            return PT_CENTER;
        }
        if (d1 <= d2) {
            return PT_P1;
        }

        return PT_P2;
    }


    /**
     * _more_
     *
     * @param x _more_
     * @param y _more_
     */
    public void moveBy(int x, int y) {
        p1.x += x;
        p1.y += y;
        p2.x += x;
        p2.y += y;
    }

    /**
     * _more_
     *
     * @param x _more_
     * @param y _more_
     */
    public void moveTo(int x, int y) {
        //TODO
    }


    //Taken from java1.2 java.awt.geom.Line2D

    /**
     * _more_
     *
     * @param X1 _more_
     * @param Y1 _more_
     * @param X2 _more_
     * @param Y2 _more_
     * @param PX _more_
     * @param PY _more_
     *
     * @return _more_
     */
    public static double ptSegDistSq(double X1, double Y1, double X2,
                                     double Y2, double PX, double PY) {
        // Adjust vectors relative to X1,Y1
        // X2,Y2 becomes relative vector from X1,Y1 to end of segment
        X2 -= X1;
        Y2 -= Y1;
        // PX,PY becomes relative vector from X1,Y1 to test point
        PX -= X1;
        PY -= Y1;
        double dotprod = PX * X2 + PY * Y2;
        double projlenSq;
        if (dotprod <= 0.0) {
            // PX,PY is on the side of X1,Y1 away from X2,Y2
            // distance to segment is length of PX,PY vector
            // "length of its (clipped) projection" is now 0.0
            projlenSq = 0.0;
        } else {
            // switch to backwards vectors relative to X2,Y2
            // X2,Y2 are already the negative of X1,Y1=>X2,Y2
            // to get PX,PY to be the negative of PX,PY=>X2,Y2
            // the dot product of two negated vectors is the same
            // as the dot product of the two normal vectors
            PX      = X2 - PX;
            PY      = Y2 - PY;
            dotprod = PX * X2 + PY * Y2;
            if (dotprod <= 0.0) {
                // PX,PY is on the side of X2,Y2 away from X1,Y1
                // distance to segment is length of (backwards) PX,PY vector
                // "length of its (clipped) projection" is now 0.0
                projlenSq = 0.0;
            } else {
                // PX,PY is between X1,Y1 and X2,Y2
                // dotprod is the length of the PX,PY vector
                // projected on the X2,Y2=>X1,Y1 vector times the
                // length of the X2,Y2=>X1,Y1 vector
                projlenSq = dotprod * dotprod / (X2 * X2 + Y2 * Y2);
            }
        }

        // Distance to line is now the length of the relative point
        // vector minus the length of its projection onto the line
        // (which is zero if the projection falls outside the range
        //  of the line segment).
        return PX * PX + PY * PY - projlenSq;
    }

    /**
     * _more_
     *
     * @param X1 _more_
     * @param Y1 _more_
     * @param X2 _more_
     * @param Y2 _more_
     * @param PX _more_
     * @param PY _more_
     *
     * @return _more_
     */
    public static double ptSegDist(double X1, double Y1, double X2,
                                   double Y2, double PX, double PY) {
        return Math.sqrt(ptSegDistSq(X1, Y1, X2, Y2, PX, PY));
    }


}
