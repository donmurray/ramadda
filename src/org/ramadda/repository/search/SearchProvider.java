/*
 * Copyright (c) 2008-2015 Geode Systems LLC
 * This Software is licensed under the Geode Systems RAMADDA License available in the source distribution in the file 
 * ramadda_license.txt. The above copyright notice shall be included in all copies or substantial portions of the Software.
 */

package org.ramadda.repository.search;



import org.ramadda.repository.*;
import org.ramadda.repository.type.*;
import org.ramadda.sql.Clause;

import java.io.*;

import java.net.URL;
import java.net.URLConnection;

import java.util.ArrayList;
import java.util.List;


/**
 */
public abstract class SearchProvider extends GenericTypeHandler {

    /** _more_ */
    private String id;

    /** _more_ */
    private String name;

    /** _more_ */
    private String apiKey;


    /**
     * _more_
     *
     * @param repository _more_
     */
    public SearchProvider(Repository repository) {
        super(repository, "", "Search Provider");
    }

    /**
     * _more_
     *
     * @param repository _more_
     * @param id _more_
     */
    public SearchProvider(Repository repository, String id) {
        super(repository, id, "Search Provider");
        this.id = id;
    }

    /**
     * _more_
     *
     * @param repository _more_
     * @param id _more_
     * @param name _more_
     */
    public SearchProvider(Repository repository, String id, String name) {
        super(repository, id, name);
        this.id   = id;
        this.name = name;
    }


    /**
     * _more_
     *
     * @return _more_
     */
    public boolean isEnabled() {
        return true;
    }

    /**
     * _more_
     *
     * @return _more_
     */
    public String getApiKey() {
        if (apiKey == null) {
            apiKey = getRepository().getProperty(getId() + ".api.key");
        }

        return apiKey;
    }



    /**
     * _more_
     *
     * @return _more_
     *
     * @throws Exception _more_
     */
    public TypeHandler getLinkTypeHandler() throws Exception {
        return getRepository().getTypeHandler("link");
    }

    /**
     * _more_
     *
     * @return _more_
     */
    public String getId() {
        return id;
    }

    /**
     * _more_
     *
     * @return _more_
     */
    public String getName() {
        if (name == null) {
            return getId();
        }

        return name;
    }

    /**
     * _more_
     *
     * @param name _more_
     */
    public void setName(String name) {
        this.name = name;
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
         * @param id _more_
         * @param name _more_
         */
        public RamaddaSearchProvider(Repository repository, String id,
                                     String name) {
            super(repository, id, name);
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

    /**
     * _more_
     *
     * @param url _more_
     *
     * @return _more_
     *
     * @throws Exception _more_
     */
    public URLConnection getConnection(String url) throws Exception {
        URLConnection connection = new URL(url).openConnection();
        connection.setRequestProperty("User-Agent", "ramadda");

        return connection;
    }

    /**
     * _more_
     *
     * @param url _more_
     *
     * @return _more_
     *
     * @throws Exception _more_
     */
    public InputStream getInputStream(String url) throws Exception {
        return getConnection(url).getInputStream();
    }


}
