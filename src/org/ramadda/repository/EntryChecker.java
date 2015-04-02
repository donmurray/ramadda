/**
* Copyright (c) 2008-2015 Geode Systems LLC
* This Software is licensed under the Geode Systems RAMADDA License available in the source distribution in the file 
* ramadda_license.txt. The above copyright notice shall be included in all copies or substantial portions of the Software.
*/

package org.ramadda.repository;


import java.util.List;


/**
 * Interface description
 *
 *
 * @author         Enter your name here...
 */
public interface EntryChecker {

    /**
     * _more_
     *
     * @param entries _more_
     */
    public void entriesCreated(List<Entry> entries);

    /**
     * _more_
     *
     * @param entries _more_
     */
    public void entriesModified(List<Entry> entries);

    /**
     * _more_
     *
     * @param ids _more_
     */
    public void entriesDeleted(List<String> ids);
}
