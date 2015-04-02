/**
* Copyright (c) 2008-2015 Geode Systems LLC
* This Software is licensed under the Geode Systems RAMADDA License available in the source distribution in the file 
* ramadda_license.txt. The above copyright notice shall be included in all copies or substantial portions of the Software.
*/

package org.ramadda.repository;


import org.ramadda.util.HtmlUtils;


/**
 *
 *
 * @author RAMADDA Development Team
 * @version $Revision: 1.3 $
 */
public class EntryLink {

    /** _more_ */
    private String link;

    /** _more_ */
    private String folderBlock;

    /** _more_ */
    private String uid;

    /**
     * _more_
     *
     * @param link _more_
     * @param folderBlock _more_
     * @param uid _more_
     */
    public EntryLink(String link, String folderBlock, String uid) {
        this.link        = link;
        this.folderBlock = folderBlock;
        this.uid         = uid;
    }

    /**
     * _more_
     *
     * @return _more_
     */
    public String toString() {
        return link + HtmlUtils.br() + folderBlock;
    }

    /**
     *  Set the Link property.
     *
     *  @param value The new value for Link
     */
    public void setLink(String value) {
        link = value;
    }

    /**
     *  Get the Link property.
     *
     *  @return The Link
     */
    public String getLink() {
        return link;
    }

    /**
     *  Set the FolderBlock property.
     *
     *  @param value The new value for FolderBlock
     */
    public void setFolderBlock(String value) {
        folderBlock = value;
    }

    /**
     *  Get the FolderBlock property.
     *
     *  @return The FolderBlock
     */
    public String getFolderBlock() {
        return folderBlock;
    }

    /**
     *  Set the Uid property.
     *
     *  @param value The new value for Uid
     */
    public void setUid(String value) {
        uid = value;
    }

    /**
     *  Get the Uid property.
     *
     *  @return The Uid
     */
    public String getUid() {
        return uid;
    }



}
