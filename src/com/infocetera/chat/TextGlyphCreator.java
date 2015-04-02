/**
* Copyright (c) 2008-2015 Geode Systems LLC
* This Software is licensed under the Geode Systems RAMADDA License available in the source distribution in the file 
* ramadda_license.txt. The above copyright notice shall be included in all copies or substantial portions of the Software.
*/

package com.infocetera.chat;


import com.infocetera.glyph.*;


import com.infocetera.util.*;

import java.awt.*;
import java.awt.event.*;

import java.util.Hashtable;
import java.util.StringTokenizer;
import java.util.Vector;




/**
 * Class TextGlyphCreator _more_
 *
 *
 * @author IDV Development Team
 * @version $Revision: 1.3 $
 */
public class TextGlyphCreator extends CanvasCommand {

    /** _more_ */
    TextGlyph theGlyph;

    /** _more_ */
    boolean alreadyCreated = false;

    /** _more_ */
    boolean gotKey = false;


    /**
     * _more_
     *
     * @param canvas _more_
     * @param firstEvent _more_
     * @param theGlyph _more_
     * @param x _more_
     * @param y _more_
     */
    public TextGlyphCreator(EditCanvas canvas, AWTEvent firstEvent,
                            TextGlyph theGlyph, int x, int y) {
        this(canvas, firstEvent, false, theGlyph, x, y);
    }

    /**
     * _more_
     *
     * @param canvas _more_
     * @param firstEvent _more_
     * @param already _more_
     * @param theGlyph _more_
     * @param x _more_
     * @param y _more_
     */
    public TextGlyphCreator(EditCanvas canvas, AWTEvent firstEvent,
                            boolean already, TextGlyph theGlyph, int x,
                            int y) {
        super(canvas, firstEvent, x, y);
        this.alreadyCreated = already;
        this.theGlyph       = theGlyph;
        theGlyph.setInserting(true);
    }

    /**
     * _more_
     *
     * @return _more_
     */
    public Cursor getCursor() {
        return DisplayCanvas.TEXT_CURSOR;
    }

    /**
     * _more_
     *
     * @return _more_
     */
    public String getDescription() {
        return "Create a TextGlyph";
    }


    /**
     * _more_
     *
     * @param e _more_
     *
     * @return _more_
     */
    public CanvasCommand doKeyPress(KeyEvent e) {
        char key     = e.getKeyChar();
        int  keyCode = e.getKeyCode();

        //On escape key we are done
        if (keyCode == KeyEvent.VK_ESCAPE) {
            doComplete();

            return null;
        }

        if ((keyCode == KeyEvent.VK_SHIFT)
                || (keyCode == KeyEvent.VK_CONTROL)) {
            return this;
        }
        if (e.isControlDown()) {
            return this;
        }

        gotKey = true;
        //    System.err.println ("Key:" + e);
        theGlyph.append(key, keyCode);

        canvas.notifyGlyphChanged(theGlyph, Glyph.ATTR_TEXT);
        //Just do a repaint here because the TextGlyph does not calculate
        //its bounds until it is painted.
        canvas.repaint();

        return this;
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
    public CanvasCommand doMouseReleased(MouseEvent e, int x, int y) {
        return this;
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
        return this;
    }

    /**
     * _more_
     */
    protected void doComplete() {
        debug("textglyph.doComplete");
        theGlyph.setInserting(false);

        //Do nothing if nothing was typed.
        if ( !gotKey) {
            if ( !alreadyCreated) {
                canvas.justRemoveGlyph(theGlyph);
            }
        } else {
            if (alreadyCreated) {
                canvas.notifyGlyphChangeDone(theGlyph, Glyph.ATTR_TEXT);
            } else {
                canvas.notifyGlyphCreateComplete(theGlyph, true);
            }
        }
        canvas.repaint(theGlyph);
    }

}
