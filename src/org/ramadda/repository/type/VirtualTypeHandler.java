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

package org.ramadda.repository.type;


import org.ramadda.repository.*;
import org.ramadda.repository.output.OutputHandler;

import org.ramadda.util.FormInfo;
import org.ramadda.util.HtmlUtils;


import org.w3c.dom.*;

import ucar.unidata.util.StringUtil;

import java.util.ArrayList;
import java.util.Date;
import java.util.Hashtable;
import java.util.List;



/**
 * Class TypeHandler _more_
 *
 *
 * @author RAMADDA Development Team
 * @version $Revision: 1.3 $
 */
public class VirtualTypeHandler extends GenericTypeHandler {




    /**
     * _more_
     *
     * @param repository _more_
     * @param entryNode _more_
     *
     * @throws Exception _more_
     */
    public VirtualTypeHandler(Repository repository, Element entryNode)
            throws Exception {
        super(repository, entryNode);
    }


    /**
     * _more_
     *
     * @param request _more_
     * @param column _more_
     * @param formBuffer _more_
     * @param entry _more_
     * @param values _more_
     * @param state _more_
     * @param formInfo _more_
     *
     * @throws Exception _more_
     */
    public void addColumnToEntryForm(Request request, Column column,
                                     StringBuffer formBuffer, Entry entry,
                                     Object[] values, Hashtable state,
                                     FormInfo formInfo)
            throws Exception {

        if (column.getOffset() == 0) {
            String value = "";
            if (values != null) {
                value = column.toString(values, column.getOffset());
            }
            String urlArg     = column.getEditArg();
            String textAreaId = HtmlUtils.getUniqueId("input_");
            String widget = HtmlUtils.textArea(urlArg, value, 10, 60,
                                HtmlUtils.id(textAreaId));
            formInfo.addSizeValidation(column.getLabel(), textAreaId, 1500);
            String suffix =
                "entry ids - one per row<br>Or use the  <a target=_help href=\"http://ramadda.org/repository/userguide/wikitext.html#collection\">entry shortcut and search</a> services";
            String buttons = OutputHandler.getSelect(request, textAreaId,
                                 "Add entry id", true, "entryid", entry,
                                 false);

            formBuffer.append(
                HtmlUtils.formEntryTop(
                    msgLabel(column.getLabel()),
                    buttons + "<table cellspacing=0 cellpadding=0 border=0>"
                    + HtmlUtils.row(HtmlUtils.cols(widget, suffix))
                    + "</table>"));
            formBuffer.append("\n");
        } else {
            super.addColumnToEntryForm(request, column, formBuffer, entry,
                                       values, state, formInfo);
        }

    }


    /**
     * _more_
     *
     * @return _more_
     */
    public boolean isSynthType() {
        return true;
    }

    /**
     * _more_
     *
     * @param request _more_
     * @param mainEntry _more_
     * @param parentEntry _more_
     * @param synthId _more_
     *
     * @return _more_
     *
     * @throws Exception _more_
     */
    @Override
    public List<String> getSynthIds(Request request, Entry mainEntry,
                                    Entry parentEntry, String synthId)
            throws Exception {
        List<String> ids      = new ArrayList<String>();
        String       idString = (String) mainEntry.getValue(0, "");
        idString = idString.replace(",", "_COMMA_");
        List<String> lines = StringUtil.split(idString, "\n", true, true);
        idString = StringUtil.join(",", lines);


        List<Entry> entries = getWikiManager().getEntries(request, mainEntry,
                                  mainEntry, idString, null, false, "");
        for (Entry entry : entries) {
            ids.add(entry.getId());
        }

        return ids;
    }


    /**
     * _more_
     *
     * @param request _more_
     * @param parentEntry _more_
     * @param id _more_
     *
     * @return _more_
     *
     * @throws Exception _more_
     */
    public Entry makeSynthEntry(Request request, Entry parentEntry, String id)
            throws Exception {
        return getEntryManager().getEntry(request, id);
    }





    /**
     * _more_
     *
     * @param id _more_
     *
     * @return _more_
     */
    public Entry createEntry(String id) {
        //Make the top level entry act like a group
        return new Entry(id, this, true);
    }



}
