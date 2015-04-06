/*
 * Copyright (c) 2008-2015 Geode Systems LLC
 * This Software is licensed under the Geode Systems RAMADDA License available in the source distribution in the file 
 * ramadda_license.txt. The above copyright notice shall be included in all copies or substantial portions of the Software.
 */

package org.ramadda.plugins.media;


import org.ramadda.util.text.Row;
import org.ramadda.util.text.Visitor;




import java.util.List;


/**
 */
public interface TabularVisitor {

    /**
     * _more_
     *
     *
     * @param info _more_
     * @param sheetName _more_
     * @param rows _more_
     *
     * @return _more_
     */
    public boolean visit(Visitor info, String sheetName,
                         List<List<Object>> rows);

}
