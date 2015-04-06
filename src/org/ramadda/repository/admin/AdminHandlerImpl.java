/*
 * Copyright (c) 2008-2015 Geode Systems LLC
 * This Software is licensed under the Geode Systems RAMADDA License available in the source distribution in the file 
 * ramadda_license.txt. The above copyright notice shall be included in all copies or substantial portions of the Software.
 */

package org.ramadda.repository.admin;


import org.ramadda.repository.*;


import java.util.List;


/**
 *
 *
 * @author RAMADDA Development Team
 * @version $Revision: 1.3 $
 */
public abstract class AdminHandlerImpl extends RepositoryManager implements AdminHandler {


    /**
     * _more_
     *
     * @param repository _more_
     */
    public AdminHandlerImpl(Repository repository) {
        super(repository);
    }


    /**
     * _more_
     */
    public AdminHandlerImpl() {
        this(null);
    }


    /**
     * _more_
     *
     * @return _more_
     */
    public Repository getRepository() {
        return repository;
    }


    /**
     * _more_
     *
     * @param blockId _more_
     * @param sb _more_
     */
    public void addToAdminSettingsForm(String blockId, StringBuffer sb) {}

    /**
     * _more_
     *
     * @param request _more_
     *
     * @throws Exception _more_
     */
    public void applyAdminSettingsForm(Request request) throws Exception {}

    /**
     * _more_
     *
     * @return _more_
     */
    public List<RequestUrl> getAdminUrls() {
        return null;
    }


}
