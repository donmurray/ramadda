/*
 * Copyright 1997-2010 Unidata Program Center/University Corporation for
 * Atmospheric Research, P.O. Box 3000, Boulder, CO 80307,
 * support@unidata.ucar.edu.
 * 
 * This library is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation; either version 2.1 of the License, or (at
 * your option) any later version.
 * 
 * This library is distributed in the hope that it will be useful, but
 * WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU Lesser
 * General Public License for more details.
 * 
 * You should have received a copy of the GNU Lesser General Public License
 * along with this library; if not, write to the Free Software Foundation,
 * Inc., 59 Temple Place, Suite 330, Boston, MA 02111-1307 USA
 * 
 */

package ucar.unidata.repository.auth;


import ucar.unidata.repository.*;


/**
 *
 *
 * @author RAMADDA Development Team
 * @version $Revision: 1.3 $
 */
public class AuthorizationMethod {

    /** _more_ */
    private static final String TYPE_HTTPAUTH = "httpauth";

    /** _more_ */
    private static final String TYPE_HTML = "html";

    /** _more_ */
    private String type = TYPE_HTML;

    /** _more_ */
    public static final AuthorizationMethod AUTH_HTTP =
        new AuthorizationMethod(TYPE_HTTPAUTH);

    /** _more_ */
    public static final AuthorizationMethod AUTH_HTML =
        new AuthorizationMethod(TYPE_HTML);

    /**
     * _more_
     *
     *
     * @param type _more_
     */
    private AuthorizationMethod(String type) {
        this.type = type;
    }


    /**
     * _more_
     *
     * @param method _more_
     *
     * @return _more_
     */
    public static AuthorizationMethod getMethod(String method) {
        if (method.equals(TYPE_HTTPAUTH)) {
            return AUTH_HTTP;
        }
        return AUTH_HTML;
    }

    /**
     * _more_
     *
     * @param o _more_
     *
     * @return _more_
     */
    public boolean equals(Object o) {
        if ( !(o instanceof AuthorizationMethod)) {
            return false;
        }
        AuthorizationMethod that = (AuthorizationMethod) o;
        return this.type.equals(that.type);
    }

    /**
     * _more_
     *
     * @return _more_
     */
    public String toString() {
        return type;
    }

}
