/**
* Copyright (c) 2008-2015 Geode Systems LLC
* This Software is licensed under the Geode Systems RAMADDA License available in the source distribution in the file 
* ramadda_license.txt. The above copyright notice shall be included in all copies or substantial portions of the Software.
*/

package org.ramadda.repository.database;


/**
 *
 *
 * @author RAMADDA Development Team
 * @version $Revision: 1.3 $
 */
public class IndexInfo {

    /** _more_ */
    private String name;

    /** _more_ */
    private String columnName;


    /**
     * _more_
     */
    public IndexInfo() {}

    /**
     * _more_
     *
     * @param name _more_
     * @param columnName _more_
     */
    public IndexInfo(String name, String columnName) {
        this.name       = name;
        this.columnName = columnName;

    }

    /**
     *  Set the Name property.
     *
     *  @param value The new value for Name
     */
    public void setName(String value) {
        this.name = value;
    }

    /**
     *  Get the Name property.
     *
     *  @return The Name
     */
    public String getName() {
        return this.name;
    }

    /**
     * Set the ColumnName property.
     *
     * @param value The new value for ColumnName
     */
    public void setColumnName(String value) {
        this.columnName = value;
    }

    /**
     * Get the ColumnName property.
     *
     * @return The ColumnName
     */
    public String getColumnName() {
        return this.columnName;
    }


}
