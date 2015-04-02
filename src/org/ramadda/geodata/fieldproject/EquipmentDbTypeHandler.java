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

package org.ramadda.geodata.fieldproject;


import org.ramadda.plugins.db.*;



import org.ramadda.repository.*;


import org.w3c.dom.*;



/**
 *
 */

public class EquipmentDbTypeHandler extends DbTypeHandler {

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
    public EquipmentDbTypeHandler(DbAdminHandler dbAdmin,
                                  Repository repository, String tableName,
                                  Element tableNode, String desc)
            throws Exception {
        super(dbAdmin, repository, tableName, tableNode, desc);
    }

    /**
     * _more_
     *
     * @param view _more_
     *
     * @return _more_
     */
    public boolean showInHeader(String view) {
        if (view.equals(VIEW_CHART) || view.equals(VIEW_STICKYNOTES)
                || view.equals(VIEW_RSS)) {
            return false;
        }

        return super.showInHeader(view);
    }

}
