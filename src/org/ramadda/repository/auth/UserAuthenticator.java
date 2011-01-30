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

package org.ramadda.repository.auth;


import org.ramadda.repository.*;


import java.util.List;


/**
 * UserAuthenticator _more_
 *
 *
 * @author RAMADDA Development Team
 */
public interface UserAuthenticator {

    /**
     * this gets called at startup
     *
     * @throws Exception _more_
     */
    public void initUsers() throws Exception;

    /**
     * this gets called when we want to just get a User object from the ID.
     * return null if user is unknown
     *
     * @param repository _more_
     * @param userId _more_
     *
     * @return _more_
     */
    public User findUser(Repository repository, String userId);


    /**
     * this gets called when we want to autheticate the given user/password
     * return null if user/password is unknown or incorrect
     *
     * @param repository _more_
     * @param request _more_
     * @param loginFormExtra _more_
     * @param userId _more_
     * @param password _more_
     *
     * @return _more_
     */
    public User authenticateUser(Repository repository, Request request,
                                 StringBuffer loginFormExtra, String userId,
                                 String password);



    /**
     * This is used to list out the roles for display in the access pages
     *
     * @return _more_
     */
    public List<String> getAllRoles();

    /**
     * this can be used to list out all of the users and display them
     * in RAMADDA
     * It is not used by RAMADDA right now
     *
     * @return _more_
     */
    public List<User> getAllUsers();



    /**
     * This will be used to allow this authenticator to add options
     * to the admin config form
     * Its not used right now
     *
     * @param repository _more_
     * @param sb _more_
     */
    public void addToConfigurationForm(Repository repository,
                                       StringBuffer sb);

    /**
     * This will be used to allow this authenticator to set the options from the config form
     * to the admin config form
     * Its not used right now
     *
     * @param repository _more_
     * @param request _more_
     */
    public void applyConfigurationForm(Repository repository,
                                       Request request);

}
