/*
 * Copyright (c) 2008-2015 Geode Systems LLC
 * This Software is licensed under the Geode Systems RAMADDA License available in the source distribution in the file 
 * ramadda_license.txt. The above copyright notice shall be included in all copies or substantial portions of the Software.
 */

package org.ramadda.projects.above;


import org.ramadda.plugins.db.*;


import org.ramadda.repository.*;

import org.ramadda.repository.output.*;


import org.ramadda.repository.type.*;
import org.ramadda.util.HtmlUtils;
import org.ramadda.util.Site;



import org.ramadda.util.Utils;


import org.w3c.dom.*;

import ucar.unidata.util.IOUtil;
import ucar.unidata.util.Misc;
import ucar.unidata.util.StringUtil;
import ucar.unidata.util.StringUtil;
import ucar.unidata.util.TwoFacedObject;



import java.io.File;

import java.text.DecimalFormat;
import java.text.SimpleDateFormat;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.Hashtable;
import java.util.List;



/**
 *
 */

public class AboveDbTypeHandler extends DbTypeHandler {

    /**
     * _more_
     *
     *
     * @param dbAdmin _more_
     * @param repository _more_
     * @param tableName _more_
     * @param tableNode _more_
     * @param desc _more_
     *
     * @throws Exception _more_
     */
    public AboveDbTypeHandler(DbAdminHandler dbAdmin, Repository repository,
                              String tableName, Element tableNode,
                              String desc)
            throws Exception {
        super(dbAdmin, repository, tableName, tableNode, desc);
    }


    /**
     * _more_
     *
     * @param entry _more_
     * @param entryProps _more_
     * @param values _more_
     *
     * @return _more_
     */
    @Override
    public String getIconFor(Entry entry, Hashtable entryProps,
                             Object[] values) {
        Column iconColumn = columnsToUse.get(columnsToUse.size() - 1);
        String value      = iconColumn.getString(values);
        if (value == null) {
            return super.getIconFor(entry, entryProps, values);
        }
        if (value.equals("Fluxtower")) {
            return fileUrl("/above/tower.png");
        } else {
            return fileUrl("/icons/add.png");
        }
    }


}
