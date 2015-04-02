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


import org.ramadda.repository.*;
import org.ramadda.repository.output.*;
import org.ramadda.repository.type.*;

import org.w3c.dom.*;

import java.io.File;

import java.util.List;


/**
 *
 *
 * @author Jeff McWhirter
 */
public class ResearchFacilityTypeHandler extends ExtensibleGroupTypeHandler {

    /**
     * _more_
     *
     * @param repository _more_
     * @param node _more_
     * @throws Exception On badness
     */
    public ResearchFacilityTypeHandler(Repository repository, Element node)
            throws Exception {
        super(repository, node);
    }



    /*
    public void getEntryLinks(Request request, Entry entry, List<Link> links)
        throws Exception {
        super.getEntryLinks(request, entry, links);
        links.add(
                  new Link(
                           request.entryUrl(
                                            getRepository().URL_ENTRY_ACCESS, entry, "type",
                                            "kml"), getRepository().iconUrl(ICON_KML),
                           "Convert GPX to KML", OutputType.TYPE_FILE));
    }



    public Result processEntryAccess(Request request, Entry entry)
        throws Exception {
        File imageFile = getStorageManager().getTmpFile(request,
                                                        "icon.png");
        return new Result("",
                          getStorageManager().getFileInputStream(imageFile),
                          getRepository().getMimeTypeFromSuffix("png"));
    }
    */


}
