/*
 * Copyright (c) 2008-2015 Geode Systems LLC
 * This Software is licensed under the Geode Systems RAMADDA License available in the source distribution in the file 
 * ramadda_license.txt. The above copyright notice shall be included in all copies or substantial portions of the Software.
 */

package org.ramadda.plugins.box;


import org.json.*;

import org.ramadda.repository.*;
import org.ramadda.repository.search.*;
import org.ramadda.repository.type.*;

import org.ramadda.util.HtmlUtils;
import org.ramadda.util.Utils;

import ucar.unidata.util.IOUtil;

import java.io.*;

import java.net.URL;
import java.net.URLConnection;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;


/**
 * Proxy that searches google
 *
 */
public class BoxSearchProvider extends SearchProvider {


    /** _more_ */
    public static final String URL =
        "https://api.duckduckgo.com/?format=json";

    /** _more_ */
    public static final String SEARCH_ID = "duckduckgo";

    /**
     * _more_
     *
     * @param repository _more_
     */
    public BoxSearchProvider(Repository repository) {
        super(repository, SEARCH_ID, "Box Search Provider");
    }

    /**
     * _more_
     *
     * @param repository _more_
     * @param args _more_
     */
    public BoxSearchProvider(Repository repository,
                                    List<String> args) {
        super(repository, SEARCH_ID, "Box");
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

        List<Entry> entries = new ArrayList<Entry>();
        return entries;
    }

}
