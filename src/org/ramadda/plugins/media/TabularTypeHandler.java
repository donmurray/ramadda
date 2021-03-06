/*
 * Copyright (c) 2008-2015 Geode Systems LLC
 * This Software is licensed under the Geode Systems RAMADDA License available in the source distribution in the file 
 * ramadda_license.txt. The above copyright notice shall be included in all copies or substantial portions of the Software.
 */

package org.ramadda.plugins.media;


import org.ramadda.repository.*;
import org.ramadda.repository.metadata.*;
import org.ramadda.repository.type.*;
import org.ramadda.util.FileInfo;
import org.ramadda.util.HtmlUtils;

import org.ramadda.util.Json;
import org.ramadda.util.XlsUtil;

import org.ramadda.util.text.Visitor;


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
import java.util.HashSet;
import java.util.Hashtable;
import java.util.List;

import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;


/**
 *
 *
 */
public class TabularTypeHandler extends MsDocTypeHandler {


    /** _more_ */
    public static final String TYPE_TABULAR = "type_document_tabular";


    /** _more_ */
    private static int IDX = 0;

    /** _more_ */
    public static final int IDX_SHOWTABLE = IDX++;

    /** _more_ */
    public static final int IDX_SHOWCHART = IDX++;

    /** _more_ */
    public static final int IDX_SHEETS = IDX++;

    /** _more_ */
    public static final int IDX_SKIPROWS = IDX++;

    /** _more_ */
    public static final int IDX_SKIPCOLUMNS = IDX++;



    /** _more_ */
    public static final int IDX_USEFIRSTROW = IDX++;

    /** _more_ */
    public static final int IDX_COLHEADER = IDX++;

    /** _more_ */
    public static final int IDX_HEADER = IDX++;

    /** _more_ */
    public static final int IDX_ROWHEADER = IDX++;

    /** _more_ */
    public static final int IDX_WIDTHS = IDX++;

    /** _more_ */
    public static final int IDX_CHARTS = IDX++;

    /** _more_ */
    public static final int IDX_SEARCHINFO = IDX++;

    /** _more_ */
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
     * @param entry _more_
     *
     * @return _more_
     */
    public static boolean isTabular(Entry entry) {
        if (entry == null) {
            return false;
        }

        return entry.getTypeHandler().isType(TabularTypeHandler.TYPE_TABULAR);
    }


    /**
     * _more_
     *
     * @param request _more_
     * @param entry _more_
     * @param fromWhere _more_
     * @param args _more_
     * @param sb _more_
     * @param files _more_
     *
     * @throws Exception _more_
     */
    @Override
    public void addEncoding(Request request, Entry entry, String fromWhere,
                            List<String> args, Appendable sb,
                            List<FileInfo> files)
            throws Exception {
        super.addEncoding(request, entry, fromWhere, args, sb, files);

        getTabularOutputHandler().addEncoding(request, entry, fromWhere,
                args, sb, files);
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


    /**
     * _more_
     *
     * @param request _more_
     * @param entry _more_
     * @param myxls _more_
     * @param visitInfo _more_
     * @param visitor _more_
     *
     * @throws Exception _more_
     */
    public void visit(Request request, Entry entry, InputStream myxls,
                      Visitor visitInfo, TabularVisitor visitor)
            throws Exception {}


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

        boolean showTable = entry.getValue(IDX_SHOWTABLE, true);
        boolean showChart = entry.getValue(IDX_SHOWCHART, true);


        if ( !showTable && !showChart) {
            return null;
        }

        return getTabularOutputHandler().getHtmlDisplay(request,
                requestProps, entry);
    }


    /**
     * _more_
     *
     * @param request _more_
     * @param entry _more_
     *
     * @return _more_
     */
    public boolean okToShowTable(Request request, Entry entry) {
        return true;
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
