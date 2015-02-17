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
import org.ramadda.repository.type.*;

import org.ramadda.repository.util.ServerInfo;

import java.util.ArrayList;
import java.util.List;

import java.net.URL;

/**
 * Class description
 *
 *
 * @version        $version$, Sat, Nov 8, '14
 * @author         Enter your name here...
 */
public class TestSearchProvider extends SearchProvider {


    /** _more_          */
    private String externalUrl;

    private String name;

    /**
     * _more_
     *
     * @param repository _more_
     */
    public TestSearchProvider(Repository repository) {
        super(repository);
    }

    /**
     * _more_
     *
     * @param repository _more_
     * @param args _more_
     */
    public TestSearchProvider(Repository repository, List<String> args) {
        super(repository);
        if (args != null) {
            if (args.size() > 0) {
                externalUrl = args.get(0);
            }
            if (args.size() > 1) {
                name = args.get(1);
            } else {
                name  = externalUrl;
            }
        }
    }

    /**
     * _more_
     *
     * @return _more_
     */
    public String toString() {
        if (name != null) {
            return name;
        }

        return "TestSearchProvider";
    }

    /**
     * _more_
     *
     * @param request _more_
     * @param url _more_
     *
     * @return _more_
     */
    public String applyMacros(Request request, String url) {
        url = url.replace("${text}", request.getString(ARG_TEXT, ""));
        url = url.replace("${north}",
                          request.getString(TypeHandler.REQUESTARG_NORTH,
                                            ""));
        url = url.replace("${west}",
                          request.getString(TypeHandler.REQUESTARG_WEST, ""));
        url = url.replace("${south}",
                          request.getString(TypeHandler.REQUESTARG_SOUTH,
                                            ""));
        url = url.replace("${east}",
                          request.getString(TypeHandler.REQUESTARG_EAST, ""));

        //TODO: Add time and others
        return url;
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
    @Override
    public List<Entry> getEntries(Request request,
                                  Appendable searchCriteriaSB)
            throws Exception {
        List<Entry> results = new ArrayList<Entry>();
        if (externalUrl != null) {
            request = request.cloneMe();
            String url = applyMacros(request, externalUrl);
            Runnable runnable = getSearchManager().makeRunnable(request, 
                                                                new ServerInfo(new URL(url), this.name,""),
                                                                getEntryManager().getDummyGroup(),
                                                                results, null, null);
            runnable.run();
        } else {
            System.err.println("TestSearchProvider.getEntries");
            List[] fromRepos = getEntryManager().getEntries(request,
                                   searchCriteriaSB);
            results.addAll(((List<Entry>) fromRepos[0]));
            results.addAll(((List<Entry>) fromRepos[1]));

        }

        return results;
    }



}
