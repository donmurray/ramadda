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

package org.ramadda.plugins.phone;


import org.ramadda.repository.*;
import org.ramadda.repository.type.*;

import org.ramadda.util.HtmlUtils;

import org.w3c.dom.*;

import ucar.unidata.util.StringUtil;

import java.util.List;


/**
 *
 *
 */
public class VoiceMailTypeHandler extends GenericTypeHandler {



    /**
     * _more_
     *
     * @param repository _more_
     * @param entryNode _more_
     *
     * @throws Exception _more_
     */
    public VoiceMailTypeHandler(Repository repository, Element entryNode)
            throws Exception {
        super(repository, entryNode);
    }


    /**
     * _more_
     *
     * @param request _more_
     * @param group _more_
     * @param subGroups _more_
     * @param entries _more_
     * @param entry _more_
     *
     * @return _more_
     *
     * @throws Exception _more_
     */
    public Result getHtmlDisplay(Request request, Entry entry)
            throws Exception {
        StringBuffer sb = new StringBuffer();
        String html =
            "<table><tr><td><div class=\"audio-player\"><object>\n<param name=\"autostart\" value=\"false\">\n<param name=\"src\" value=\"${url}\">\n<param name=\"autoplay\" value=\"false\">\n<param name=\"controller\" value=\"true\">\n<embed src=\"${url}\" controller=\"true\" autoplay=\"false\" autostart=\"False\" type=\"audio/wav\" /\n></object></div></td></tr></table>\n";

        String fileUrl = entry.getTypeHandler().getEntryResourceUrl(request,
                             entry);
        html = html.replace("${url}", fileUrl);
        sb.append(html);
        sb.append(HtmlUtils.p());
        sb.append(entry.getDescription());

        return new Result(msg("Voice Mail"), sb);
    }




}
