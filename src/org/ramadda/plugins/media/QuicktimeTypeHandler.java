/**
* Copyright (c) 2008-2015 Geode Systems LLC
* This Software is licensed under the Geode Systems RAMADDA License available in the source distribution in the file 
* ramadda_license.txt. The above copyright notice shall be included in all copies or substantial portions of the Software.
*/

package org.ramadda.plugins.media;


import org.ramadda.repository.*;
import org.ramadda.repository.metadata.*;
import org.ramadda.repository.type.*;


import org.ramadda.service.*;


import org.ramadda.service.Service;

import org.ramadda.util.HtmlUtils;


import org.w3c.dom.*;

import ucar.unidata.util.IOUtil;

import ucar.unidata.util.Misc;
import ucar.unidata.util.StringUtil;


import ucar.unidata.xml.XmlUtil;

import java.util.Date;
import java.util.Hashtable;
import java.util.List;


/**
 *
 *
 */
public class QuicktimeTypeHandler extends GenericTypeHandler {

    /** _more_ */
    public static final int IDX_WIDTH = 0;

    /** _more_ */
    public static final int IDX_HEIGHT = 1;


    /**
     * ctor
     *
     * @param repository _more_
     * @param entryNode _more_
     *
     * @throws Exception _more_
     */
    public QuicktimeTypeHandler(Repository repository, Element entryNode)
            throws Exception {
        super(repository, entryNode);
    }

    /**
     * _more_
     *
     * @param request _more_
     * @param props _more_
     * @param entry _more_
     *
     * @return _more_
     *
     * @throws Exception _more_
     */
    @Override
    public String getSimpleDisplay(Request request, Hashtable props,
                                   Entry entry)
            throws Exception {
        String width  = entry.getValue(IDX_WIDTH, "320");
        String height = entry.getValue(IDX_HEIGHT, "256");
        String header = getWikiManager().wikifyEntry(request, entry,
                            DFLT_WIKI_HEADER);
        StringBuffer sb = new StringBuffer(header);
        String url = entry.getTypeHandler().getEntryResourceUrl(request,
                         entry);
        sb.append("\n");
        sb.append(HtmlUtils.tag(HtmlUtils.TAG_EMBED,
                                HtmlUtils.attrs(new String[] {
            HtmlUtils.ATTR_SRC, url, HtmlUtils.ATTR_CLASS,
            "ramadda-video-embed", HtmlUtils.ATTR_WIDTH, width,
            HtmlUtils.ATTR_HEIGHT, height, "autoplay", "false"
        })));

        return sb.toString();

    }



    /**
     * _more_
     *
     * @param request _more_
     * @param entry _more_
     * @param wikiTemplate _more_
     *
     * @return _more_
     *
     * @throws Exception _more_
     */
    @Override
    public String getInnerWikiContent(Request request, Entry entry,
                                      String wikiTemplate)
            throws Exception {
        return getSimpleDisplay(request, null, entry);
    }


}
