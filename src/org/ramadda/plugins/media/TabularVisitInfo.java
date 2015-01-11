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

import ucar.unidata.util.StringUtil;

import java.util.ArrayList;

import java.util.HashSet;
import java.util.Hashtable;
import java.util.List;


/**
 */
public class TabularVisitInfo {

    /** _more_ */
    private int skipRows = 0;

    /** _more_ */
    private int maxRows = -1;

    /** _more_ */
    private List<TabularSearchField> searchFields;

    /** _more_ */
    private String searchText;

    /** _more_          */
    private String searchTextWithPattern;


    /** _more_ */
    private HashSet<Integer> sheetsToShow;

    private List props = new ArrayList();

    /**
     * _more_
     *
     * @param request _more_
     * @param entry _more_
     */
    public TabularVisitInfo(Request request, Entry entry) {
        searchText = request.getString("table.text", (String) null);
        if (searchText != null) {
            searchTextWithPattern = "(?i:.*" + searchText + ".*)";
        }


        if (TabularTypeHandler.isTabular(entry)) {
            searchFields = new ArrayList<TabularSearchField>();
            for (String line :
                    StringUtil.split(
                        entry.getValue(
                            TabularTypeHandler.IDX_SEARCHINFO, ""), "\n",
                                true, true)) {

                String label = null;
                List<String> toks = StringUtil.splitUpTo(line, " ", 2);
                if(toks.size()>1) {
                    line = toks.get(0);
                    Hashtable props = StringUtil.parseHtmlProperties(toks.get(1));
                    label = (String) props.get("label");
                }


                TabularSearchField sf = new TabularSearchField(line);
                if(label!=null) {
                    sf.setLabel(label);
                }
                sf.setValue(request.getString(sf.getUrlArg(), (String) null));
                searchFields.add(sf);
            }
            if (searchFields.size() == 0) {
                searchFields = null;
            }
        }
    }


    public void addTableProperty(String name, String value) {
        props.add(name);
        props.add(value);
    }

    public List getTableProperties() {
        return props;
    }

    /**
     * _more_
     *
     * @param cols _more_
     *
     * @return _more_
     */
    public boolean rowOk(List cols) {
        if (searchText != null) {
            for (Object o : cols) {
                if (o == null) {
                    continue;
                }
                if (o.toString().matches(searchTextWithPattern)) {
                    return true;
                }
            }

            return false;
        }

        return true;
    }



    /**
     * _more_
     *
     *
     * @param request _more_
     * @param entry _more_
     * @param skipRows _more_
     * @param maxRows _more_
     * @param sheetsToShow _more_
     * @param searchFields _more_
     */
    public TabularVisitInfo(Request request, Entry entry, int skipRows,
                            int maxRows, HashSet<Integer> sheetsToShow) {
        this(request, entry);
        this.skipRows     = skipRows;
        this.maxRows      = maxRows;
        this.sheetsToShow = sheetsToShow;
    }


    /**
     * _more_
     *
     * @param sheetIdx _more_
     *
     * @return _more_
     */
    public boolean okToShowSheet(int sheetIdx) {
        if ((sheetsToShow != null) && !sheetsToShow.contains(sheetIdx)) {
            return false;
        }

        return true;
    }


    /**
     * Set the SkipRows property.
     *
     * @param value The new value for SkipRows
     */
    public void setSkipRows(int value) {
        skipRows = value;
    }

    /**
     * Get the SkipRows property.
     *
     * @return The SkipRows
     */
    public int getSkipRows() {
        return skipRows;
    }

    /**
     * Set the MaxRows property.
     *
     * @param value The new value for MaxRows
     */
    public void setMaxRows(int value) {
        maxRows = value;
    }

    /**
     * Get the MaxRows property.
     *
     * @return The MaxRows
     */
    public int getMaxRows() {
        return maxRows;
    }

    /**
     * Set the SearchFields property.
     *
     * @param value The new value for SearchFields
     */
    public void setSearchFields(List<TabularSearchField> value) {
        searchFields = value;
    }

    /**
     * Get the SearchFields property.
     *
     * @return The SearchFields
     */
    public List<TabularSearchField> getSearchFields() {
        return searchFields;
    }

    /**
     *  Set the SheetsToShow property.
     *
     *  @param value The new value for SheetsToShow
     */
    public void setSheetsToShow(HashSet<Integer> value) {
        sheetsToShow = value;
    }

    /**
     *  Get the SheetsToShow property.
     *
     *  @return The SheetsToShow
     */
    public HashSet<Integer> getSheetsToShow() {
        return sheetsToShow;
    }

    /**
     * _more_
     *
     * @return _more_
     */
    public String getSearchText() {
        return searchText;
    }



}
