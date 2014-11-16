/*
* Copyright 2008-2014 Geode Systems LLC
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

import org.ramadda.util.Json;
import org.ramadda.util.XlsUtil;


import org.w3c.dom.*;

import ucar.unidata.util.IOUtil;

import ucar.unidata.util.Misc;
import ucar.unidata.util.StringUtil;


import ucar.unidata.xml.XmlUtil;

import java.io.File;
import java.io.InputStream;
import java.io.OutputStream;

import java.util.ArrayList;

import java.util.Date;
import java.util.Hashtable;
import java.util.HashSet;
import java.util.List;

import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;


/**
 *
 *
 */
public class TabularTypeHandler extends MsDocTypeHandler {


    /** _more_          */
    private static int IDX = 0;

    /** _more_          */
    public static final int IDX_SHOWTABLE = IDX++;

    /** _more_          */
    public static final int IDX_SHOWCHART = IDX++;

    /** _more_          */
    public static final int IDX_SHEETS = IDX++;

    /** _more_          */
    public static final int IDX_SKIPROWS = IDX++;

    /** _more_          */
    public static final int IDX_SKIPCOLUMNS = IDX++;



    /** _more_          */
    public static final int IDX_USEFIRSTROW = IDX++;

    /** _more_          */
    public static final int IDX_COLHEADER = IDX++;

    /** _more_          */
    public static final int IDX_HEADER = IDX++;

    /** _more_          */
    public static final int IDX_ROWHEADER = IDX++;

    /** _more_          */
    public static final int IDX_WIDTHS = IDX++;

    /** _more_          */
    public static final int IDX_CHARTS = IDX++;

    public static final int IDX_LAST = IDX;


    /** _more_ */
    private TabularOutputHandler tabularOutputHandler;

    /**
     * _more_
     *
     * @param repository _more_
     * @param entryNode _more_
     *
     * @throws Exception _more_
     */
    public TabularTypeHandler(Repository repository, Element entryNode)
            throws Exception {
        super(repository, entryNode);
    }


    /**
     * _more_
     *
     * @return _more_
     */
    private TabularOutputHandler getTabularOutputHandler() {
        if (tabularOutputHandler == null) {
            tabularOutputHandler =
                (TabularOutputHandler) getRepository().getOutputHandler(
                    TabularOutputHandler.class);
        }

        return tabularOutputHandler;
    }


    public void read(Request request, Entry entry, InputStream myxls, List<String> sheets, HashSet<Integer> sheetsToShow, int skip) throws Exception {

    }


    /**
     * _more_
     *
     * @param request _more_
     * @param requestProps _more_
     * @param entry _more_
     *
     * @return _more_
     *
     * @throws Exception _more_
     */
    @Override
    public String getSimpleDisplay(Request request, Hashtable requestProps,
                                   Entry entry)
            throws Exception {

        boolean showTable  = entry.getValue(IDX_SHOWTABLE, true);
        boolean showChart  = entry.getValue(IDX_SHOWCHART, true);


        if (!showTable && !showChart) {
            return null;
        }

        return getTabularOutputHandler().getHtmlDisplay(request, requestProps,
                entry);
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
