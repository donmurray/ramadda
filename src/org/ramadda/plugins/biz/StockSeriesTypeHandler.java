/*
 * Copyright (c) 2008-2015 Geode Systems LLC
 * This Software is licensed under the Geode Systems RAMADDA License available in the source distribution in the file 
 * ramadda_license.txt. The above copyright notice shall be included in all copies or substantial portions of the Software.
 */

package org.ramadda.plugins.biz;


import org.ramadda.data.services.PointTypeHandler;
import org.ramadda.repository.*;
import org.ramadda.repository.type.*;
import org.ramadda.util.Utils;

import org.w3c.dom.*;


/**
 * Reads yahoo stock ticker time series CSV
 */
public class StockSeriesTypeHandler extends PointTypeHandler {

    /** _more_ */
    public static final int IDX_SYMBOL = IDX_PROPERTIES + 1;

    /** _more_ */
    public static final String URL = "http://ichart.yahoo.com/table.csv?s=";

    /**
     * ctor
     *
     * @param repository _more_
     * @param entryNode _more_
     *
     * @throws Exception on badness
     */
    public StockSeriesTypeHandler(Repository repository, Element entryNode)
            throws Exception {
        super(repository, entryNode);
    }


    /**
     * Return the URL to the CSV file
     *
     * @param entry the entry
     *
     * @return URL
     *
     * @throws Exception on badness
     */
    @Override
    public String getPathForEntry(Entry entry) throws Exception {
        String symbol = entry.getValue(IDX_SYMBOL, (String) null);
        if ( !Utils.stringDefined(symbol)) {
            return null;
        }

        return URL + symbol;
    }



}
