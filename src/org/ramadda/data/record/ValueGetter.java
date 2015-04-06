/*
 * Copyright (c) 2008-2015 Geode Systems LLC
 * This Software is licensed under the Geode Systems RAMADDA License available in the source distribution in the file 
 * ramadda_license.txt. The above copyright notice shall be included in all copies or substantial portions of the Software.
 */

package org.ramadda.data.record;


import java.io.*;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.List;


/**
 * Holds information about the record's parameters
 *
 *
 * @author Jeff McWhirter
 */
public interface ValueGetter {

    /**
     * _more_
     *
     * @param record _more_
     * @param field _more_
     * @param visitInfo _more_
     *
     * @return _more_
     */
    public double getValue(Record record, RecordField field,
                           VisitInfo visitInfo);

    /**
     * _more_
     *
     * @param record _more_
     * @param field _more_
     * @param visitInfo _more_
     *
     * @return _more_
     */
    public String getStringValue(Record record, RecordField field,
                                 VisitInfo visitInfo);
}
