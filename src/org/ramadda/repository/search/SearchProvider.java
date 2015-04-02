/**
* Copyright (c) 2008-2015 Geode Systems LLC
* This Software is licensed under the Geode Systems RAMADDA License available in the source distribution in the file 
* ramadda_license.txt. The above copyright notice shall be included in all copies or substantial portions of the Software.
*/

/**
 * Copyright (c) 2008-2015 Geode Systems LLC
 * This Software is licensed under the Geode Systems RAMADDA License available in the source distribution in the file
 * ramadda_license.txt. The above copyright notice shall be included in all copies or substantial portions of the Software.
 */

package org.ramadda.repository.search;


import org.ramadda.repository.*;
import org.ramadda.sql.Clause;

import java.util.ArrayList;
import java.util.List;


/**
 */
public abstract class SearchProvider extends RepositoryManager {

    /**
     * _more_
     *
     * @param repository _more_
     */
    public SearchProvider(Repository repository) {
        super(repository);
    }



    /**
     * _more_
     *
     * @param request _more_
     * @param searchCriteriaSB _more_
     *
     * @return _more_
     *
     * @throws Exception _more_
     */
    public abstract List<Entry> getEntries(Request request,
                                           Appendable searchCriteriaSB)
     throws Exception;

    /**
     * Class description
     *
     *
     * @version        $version$, Fri, Mar 14, '14
     * @author         Enter your name here...
     */
    public static class RamaddaSearchProvider extends SearchProvider {


        /**
         * _more_
         *
         * @param repository _more_
         */
        public RamaddaSearchProvider(Repository repository) {
            super(repository);
        }

        /**
         * _more_
         *
         * @param request _more_
         * @param searchCriteriaSB _more_
         *
         * @return _more_
         *
         * @throws Exception _more_
         */
        public List<Entry> getEntries(Request request,
                                      Appendable searchCriteriaSB)
                throws Exception {
            List<Entry>[] repositoryResults =
                getEntryManager().getEntries(request, searchCriteriaSB);
            List<Entry> results = new ArrayList<Entry>();
            results.addAll(repositoryResults[0]);
            results.addAll(repositoryResults[1]);

            return results;
        }

    }

}
