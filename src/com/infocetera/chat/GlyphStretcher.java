/**
* Copyright (c) 2008-2015 Geode Systems LLC
* This Software is licensed under the Geode Systems RAMADDA License available in the source distribution in the file 
* ramadda_license.txt. The above copyright notice shall be included in all copies or substantial portions of the Software.
*/

/**
 * (C) 1999-2002  WTS Systems, L.L.C.
 *   All rights reserved
 */



package com.infocetera.chat;


import com.infocetera.glyph.*;
import com.infocetera.util.*;

import java.awt.*;
import java.awt.event.*;

import java.util.Vector;



/**
 * Class GlyphStretcher _more_
 *
 *
 * @author IDV Development Team
 * @version $Revision: 1.3 $
 */
class GlyphStretcher extends CanvasCommand {

    /** _more_ */
    Glyph theGlyph;

    /** _more_ */
    Point lastPoint;

    /** _more_ */
    String ptKey = "";

    /** _more_ */
    Vector selection;

    /**
     * _more_
     *
     * @param canvas _more_
     * @param firstEvent _more_
     * @param theGlyph _more_
     * @param selection _more_
     * @param x _more_
     * @param y _more_
     */
    public GlyphStretcher(EditCanvas canvas, AWTEvent firstEvent,
                          Glyph theGlyph, Vector selection, int x, int y) {
        super(canvas, firstEvent, x, y);
        this.theGlyph  = theGlyph;
        this.selection = selection;
        if ((selection != null) && (selection.size() > 1)) {
            ptKey = Glyph.PT_CENTER;
        } else {
            ptKey = theGlyph.getStretchPoint(x, y);
        }
        lastPoint = new Point(x, y);
    }


    /**
     * _more_
     *
     * @return _more_
     */
    public Cursor getCursor() {
        return EditCanvas.MOVE_CURSOR;
    }

    /**
     * _more_
     *
     * @return _more_
     */
    public String getDescription() {
        return "Resize a glyph";
    }

    /**
     * _more_
     *
     * @param e _more_
     * @param x _more_
     * @param y _more_
     *
     * @return _more_
     */
    public CanvasCommand doMouseDragged(MouseEvent e, int x, int y) {
        debug("GlyphStretched.doMouseDragged");
        if (theGlyph == null) {
            debug("GlyphStretched.doMouseDragged - glyph null");

            return null;
        }
        canvas.repaint(theGlyph);
        int     destx    = x;
        int     desty    = y;
        boolean absolute = true;

        if (ptKey.equals(Glyph.PT_CENTER)) {
            destx    = x - lastPoint.x;
            desty    = y - lastPoint.y;
            absolute = false;
        }
        if (selection != null) {
            for (int i = 0; i < selection.size(); i++) {
                Glyph g = (Glyph) selection.elementAt(i);
                if (g != theGlyph) {
                    canvas.repaint(g);
                    if (g.canStretch()) {
                        g.stretchTo(destx, desty, ptKey, true);
                    } else {
                        //          if (absolute) g.moveTo(destx,desty);    else      g.moveBy(destx,desty);                  
                    }
                    canvas.repaint(g);
                }
            }
        }

        if (theGlyph.canStretch()) {
            ptKey = theGlyph.stretchTo(destx, desty, ptKey, true);
        } else {
            if (absolute) {
                theGlyph.moveTo(destx, desty);
            } else {
                theGlyph.moveBy(destx, desty);
            }
        }

        canvas.repaint(theGlyph);
        lastPoint.x = x;
        lastPoint.y = y;
        canvas.notifyGlyphMoved(theGlyph);
        debug("GlyphStretched.doMouseDragged - done");

        return this;
    }


    /**
     * _more_
     */
    protected void doComplete() {
        super.doComplete();
        canvas.notifyGlyphMoveComplete(theGlyph);
    }
}
