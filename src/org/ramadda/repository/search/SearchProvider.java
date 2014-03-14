/*
* Copyright 2008-2014 Geode Systems LLC
*
* Permission is hereby granted, free of charge, to any person obtaining a copy of this 
* software and associated documentation files (the "Software"), to deal in the Software 
* without restriction, including without limitation the rights to use, copy, modify, 
* merge, publish, distribute, sublicense, and/or sell copies of the Software, and to 
* permit persons to whom the Software is furnished to do so, subject to the following conditions:
* 
* The above copyright notice and this permission notice shall be included in all copies 
* or substantial portions of the Software.
* 
* THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, 
* INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR 
* PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE 
* FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR 
* OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER 
* DEALINGS IN THE SOFTWARE.
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
    public abstract List<Entry>[] getEntries(Request request,
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
        public List<Entry>[] getEntries(Request request,
                                        Appendable searchCriteriaSB)
                throws Exception {
            return getEntryManager().getEntries(request, searchCriteriaSB);
        }

    }

}
