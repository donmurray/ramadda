/**
* Copyright (c) 2008-2015 Geode Systems LLC
* This Software is licensed under the Geode Systems RAMADDA License available in the source distribution in the file 
* ramadda_license.txt. The above copyright notice shall be included in all copies or substantial portions of the Software.
*/

package org.ramadda.util;


/**
 *
 *
 * @author RAMADDA Development Team
 * @version $Revision: 1.30 $
 */
public abstract class ActionContext {

    /** _more_ */
    private String actionID;

    /** _more_ */
    private String status;

    /**
     * _more_
     */
    public ActionContext() {}


    /**
     * _more_
     *
     * @param actionID _more_
     */
    public ActionContext(String actionID) {
        this.actionID = actionID;
    }

    /**
     * _more_
     *
     * @param message _more_
     */
    public void setStatus(String message) {
        this.status = message;
    }


    /**
     * _more_
     *
     * @return _more_
     */
    public String getStatus() {
        return status;
    }


    /**
     *
     * @return _more_
     */
    public String toString() {
        return getStatus();
    }


}
