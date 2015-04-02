/**
* Copyright (c) 2008-2015 Geode Systems LLC
* This Software is licensed under the Geode Systems RAMADDA License available in the source distribution in the file 
* ramadda_license.txt. The above copyright notice shall be included in all copies or substantial portions of the Software.
*/

package org.ramadda.repository.auth;


import org.ramadda.repository.*;


/**
 * Class AccessException _more_
 *
 *
 * @author RAMADDA Development Team
 * @version $Revision: 1.3 $
 */
public class AccessException extends RuntimeException {

    /** _more_ */
    Request request;


    /**
     * _more_
     *
     * @param message _more_
     * @param request _more_
     */
    public AccessException(String message, Request request) {
        super(message);
        this.request = request;
    }

    /**
     * Set the Request property.
     *
     * @param value The new value for Request
     */
    public void setRequest(Request value) {
        this.request = value;
    }

    /**
     * Get the Request property.
     *
     * @return The Request
     */
    public Request getRequest() {
        return this.request;
    }




}
