/*
 * Copyright (c) 2008-2015 Geode Systems LLC
 * This Software is licensed under the Geode Systems RAMADDA License available in the source distribution in the file 
 * ramadda_license.txt. The above copyright notice shall be included in all copies or substantial portions of the Software.
 */

package org.ramadda.plugins.db;


import org.ramadda.repository.*;
import org.ramadda.repository.auth.*;
import org.ramadda.repository.metadata.*;
import org.ramadda.repository.output.OutputHandler;
import org.ramadda.repository.output.OutputType;
import org.ramadda.repository.type.*;

import org.ramadda.util.HtmlUtils;


import org.w3c.dom.*;

import ucar.unidata.xml.XmlUtil;



import java.util.ArrayList;
import java.util.List;


/**
 *
 */

public class DbColumn {

    /** _more_ */
    private String name;

    /** _more_ */
    private String label;


    /**
     * _more_
     *
     * @param name _more_
     * @param label _more_
     */
    public DbColumn(String name, String label) {
        this.name  = name;
        this.label = label;
    }



}
