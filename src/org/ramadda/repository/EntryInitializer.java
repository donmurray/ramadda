/**
* Copyright (c) 2008-2015 Geode Systems LLC
* This Software is licensed under the Geode Systems RAMADDA License available in the source distribution in the file 
* ramadda_license.txt. The above copyright notice shall be included in all copies or substantial portions of the Software.
*/

package org.ramadda.repository;


import java.io.File;


/**
 *
 *
 * @author RAMADDA Development Team
 * @version $Revision: 1.3 $
 */
public abstract class EntryInitializer {

    /**
     * _more_
     *
     * @param entry _more_
     */
    public void initEntry(Entry entry) {}

    /**
     * _more_
     *
     * @param entry _more_
     * @param fileArg _more_
     *
     * @return _more_
     */
    public File getMetadataFile(Entry entry, String fileArg) {
        return null;
    }


}
