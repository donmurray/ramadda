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
 * Class GlyphCreator _more_
 *
 *
 * @author IDV Development Team
 * @version $Revision: 1.3 $
 */
public class GlyphCreator extends GlyphStretcher {

    /**
     * _more_
     *
     * @param canvas _more_
     * @param firstEvent _more_
     * @param theGlyph _more_
     * @param x _more_
     * @param y _more_
     */
    public GlyphCreator(EditCanvas canvas, AWTEvent firstEvent,
                        Glyph theGlyph, int x, int y) {
        super(canvas, firstEvent, theGlyph, null, x, y);
    }

    /**
     * _more_
     */
    protected void doComplete() {
        canvas.notifyGlyphCreateComplete(theGlyph, true);
    }

}
