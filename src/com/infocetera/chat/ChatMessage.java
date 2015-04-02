/**
* Copyright (c) 2008-2015 Geode Systems LLC
* This Software is licensed under the Geode Systems RAMADDA License available in the source distribution in the file 
* ramadda_license.txt. The above copyright notice shall be included in all copies or substantial portions of the Software.
*/

package com.infocetera.chat;


import com.infocetera.glyph.*;

import com.infocetera.util.*;

import java.awt.*;



import java.util.Vector;


/**
 * Class ChatMessage _more_
 *
 *
 * @author IDV Development Team
 * @version $Revision: 1.3 $
 */
public class ChatMessage extends HtmlGlyph {

    /** _more_ */
    private ChatUser from;

    /**
     * _more_
     *
     * @param canvas _more_
     * @param from _more_
     * @param message _more_
     */
    public ChatMessage(DisplayCanvas canvas, ChatUser from, String message) {
        super(canvas, message);
        this.from  = from;
        fixedWidth = true;
    }

    /**
     * _more_
     *
     * @return _more_
     */
    public boolean isFromChat() {
        return from.getName().equals(ChatApplet.USER_CHAT);
    }

    /**
     * _more_
     *
     * @return _more_
     */
    public String getUserName() {
        return from.getName();
    }

    /**
     * _more_
     *
     * @return _more_
     */
    public ChatUser getFrom() {
        return from;
    }

    /**
     * _more_
     *
     * @return _more_
     */
    public String getFormattedName() {
        return "(" + from.getName() + ")";

    }


}
