/**
* Copyright (c) 2008-2015 Geode Systems LLC
* This Software is licensed under the Geode Systems RAMADDA License available in the source distribution in the file 
* ramadda_license.txt. The above copyright notice shall be included in all copies or substantial portions of the Software.
*/

package org.ramadda.plugins.incident;


import org.ramadda.repository.*;
import org.ramadda.repository.type.*;


import org.ramadda.util.HtmlUtils;


import org.w3c.dom.*;

import ucar.unidata.util.StringUtil;

import java.util.Date;
import java.util.List;


/**
 *
 *
 */
public class IncidentTypeHandler extends ExtensibleGroupTypeHandler {



    /**
     * _more_
     *
     * @param repository _more_
     * @param entryNode _more_
     *
     * @throws Exception _more_
     */
    public IncidentTypeHandler(Repository repository, Element entryNode)
            throws Exception {
        super(repository, entryNode);
    }


    /**
     * _more_
     *
     * @param request _more_
     * @param entry _more_
     *
     * @return _more_
     *
     * @throws Exception _more_
     */
    public String getIconUrl(Request request, Entry entry) throws Exception {
        String disaster = entry.getValue(0, (String) null);
        if (disaster == null) {
            return super.getIconUrl(request, entry);
        }
        String icon = getRepository().getProperty("icon." + disaster.trim(),
                          (String) null);
        if ((icon == null) || (icon.trim().length() == 0)) {
            return super.getIconUrl(request, entry);
        }

        return iconUrl(icon);
    }



}
