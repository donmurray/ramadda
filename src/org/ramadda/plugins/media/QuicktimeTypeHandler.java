/*
* Copyright 2008-2013 Geode Systems LLC
*
* Permission is hereby granted, free of charge, to any person obtaining a copy of this 
* software and associated documentation files (the "Software"), to deal in the Software 
* without restriction, including without limitation the rights to use, copy, modify, 
* merge, publish, distribute, sublicense, and/or sell copies of the Software, and to 
* permit persons to whom the Software is furnished to do so, subject to the following conditions:
* 
* The above copyright notice and this permission notice shall be included in all copies 
* or substantial portions of the Software.
* 
* THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, 
* INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR 
* PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE 
* FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR 
* OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER 
* DEALINGS IN THE SOFTWARE.
*/

package org.ramadda.plugins.media;


import org.ramadda.repository.*;
import org.ramadda.repository.metadata.*;
import org.ramadda.repository.type.*;

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
     * _more_
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
        String header =
            getWikiManager().wikifyEntry(
                request, entry,
                "{{name box.class=entry-page-name}}\n{{description box.class=\"entry-page-description\"}}");
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
