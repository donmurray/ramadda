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

package org.ramadda.repository.client;


/**
 *
 * @author xuqing
 */
public class EntryFile {

    /** _more_ */
    public String entryName;

    /** _more_ */
    public String entryDescription;

    /** _more_ */
    public String parent;

    /** _more_ */
    public String filePath;

    /** _more_ */
    public String north = "";

    /** _more_ */
    public String south = "";

    /** _more_ */
    public String west = "";

    /** _more_ */
    public String east = "";

    /**
     * _more_
     *
     * @param entryName _more_
     * @param entryDescription _more_
     * @param parent _more_
     * @param filePath _more_
     */
    public EntryFile(String entryName, String entryDescription,
                     String parent, String filePath) {
        this.entryName        = entryName;
        this.entryDescription = entryDescription;
        this.parent           = parent;
        this.filePath         = filePath;
    }

    /**
     * _more_
     *
     * @param entryName _more_
     * @param entryDescription _more_
     * @param parent _more_
     * @param filePath _more_
     * @param north _more_
     * @param south _more_
     * @param west _more_
     * @param east _more_
     */
    public EntryFile(String entryName, String entryDescription,
                     String parent, String filePath, String north,
                     String south, String west, String east) {
        this.entryName        = entryName;
        this.entryDescription = entryDescription;
        this.parent           = parent;
        this.filePath         = filePath;
        this.east             = east;
        this.west             = west;
        this.north            = north;
        this.south            = south;
    }

    /**
     * _more_
     *
     * @param north _more_
     * @param south _more_
     * @param west _more_
     * @param east _more_
     */
    public void setRange(String north, String south, String west,
                         String east) {
        this.east  = east;
        this.west  = west;
        this.north = north;
        this.south = south;
    }
}
