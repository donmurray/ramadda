/**
* Copyright (c) 2008-2015 Geode Systems LLC
* This Software is licensed under the Geode Systems RAMADDA License available in the source distribution in the file 
* ramadda_license.txt. The above copyright notice shall be included in all copies or substantial portions of the Software.
*/

package com.infocetera.tableview;


import java.awt.*;


/**
 * Class TableRow _more_
 *
 *
 * @author IDV Development Team
 * @version $Revision: 1.3 $
 */
public class TableRow extends Rectangle {

    /** _more_ */
    public boolean open = false;

    /** _more_ */
    public Object[] cols;

    /**
     * _more_
     *
     * @param col _more_
     *
     * @return _more_
     */
    public Object getColumnValue(int col) {
        return cols[col];
    }

    /**
     * _more_
     *
     * @param g _more_
     * @param hilite _more_
     * @param hiliteColumn _more_
     * @param columnWidth _more_
     */
    public void paint(Graphics g, TableRow hilite, int hiliteColumn,
                      int columnWidth) {
        boolean isHighlight = (this == hilite);
        int     myWidth     = columnWidth - 1;
        int     myHeight    = height - 1;
        int     crntX       = x;
        g.setColor(Color.black);
        g.drawLine(x, y + myHeight, x + width, y + myHeight);
        for (int c = 0; c < cols.length; c++) {
            g.setColor(Color.black);
            g.drawLine(crntX, y, crntX, y + height);
            if (isHighlight && (hiliteColumn == c)) {
                g.setColor(Color.yellow);
                g.drawRect(crntX + 1, y, myWidth - 1, myHeight - 1);
            }
            if (open) {
                g.setColor(Color.black);
                g.drawString(cols[c].toString(), crntX + 2, y + myHeight - 2);
            }
            crntX += columnWidth;


        }
    }



}
