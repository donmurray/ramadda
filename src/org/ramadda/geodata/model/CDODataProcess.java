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

package org.ramadda.geodata.model;


import org.ramadda.data.process.DataProcess;
import org.ramadda.repository.Entry;
import org.ramadda.repository.Repository;
import org.ramadda.repository.RepositoryManager;
import org.ramadda.repository.Request;
import org.ramadda.repository.database.Tables;
import org.ramadda.repository.type.CollectionTypeHandler;
import org.ramadda.repository.type.Column;
import org.ramadda.repository.type.GranuleTypeHandler;
import org.ramadda.sql.Clause;
import org.ramadda.util.HtmlUtils;


import java.io.File;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;


/**
 * Class description
 *
 *
 * @version        $version$, Thu, Mar 20, '14
 * @author         Enter your name here...
 */
public abstract class CDODataProcess extends DataProcess {

    /** months */
    protected static final String[] MONTHS = {
        "Jan", "Feb", "Mar", "Apr", "May", "Jun", "Jul", "Aug", "Sep", "Oct",
        "Nov", "Dec"
    };

    /** the type handler associated with this */
    private CDOOutputHandler outputHandler;

    /** the associated repository */
    private Repository repository;

    /**
     * _more_
     *
     * @param repository _more_
     * @param id _more_
     * @param label _more_
     *
     * @throws Exception _more_
     */
    public CDODataProcess(Repository repository, String id, String label)
            throws Exception {
        super(id, label);
        this.repository = repository;
        outputHandler   = new CDOOutputHandler(repository);
    }




    /**
     * Is this enabled?
     *
     * @return true if it is
     */
    public boolean isEnabled() {
        return outputHandler.isEnabled();
    }

    /**
     * Find the associated climatology for the input
     *
     * @param request  the Request
     * @param granule  the entry
     *
     * @return the climatology entry or null
     *
     * @throws Exception  problems
     */
    protected List<Entry> findClimatology(Request request, Entry granule)
            throws Exception {
        if ( !(granule.getTypeHandler()
                instanceof ClimateModelFileTypeHandler)) {
            return null;
        }
        Entry collection = GranuleTypeHandler.getCollectionEntry(request,
                               granule);
        CollectionTypeHandler ctypeHandler =
            (CollectionTypeHandler) collection.getTypeHandler();
        List<Clause>    clauses   = new ArrayList<Clause>();
        List<Column>    columns   = ctypeHandler.getGranuleColumns();
        HashSet<String> seenTable = new HashSet<String>();
        Object[]        values    = granule.getValues();
        for (int colIdx = 0; colIdx < columns.size(); colIdx++) {
            Column column = columns.get(colIdx);
            // first column is the collection ID
            int    valIdx      = colIdx + 1;
            String dbTableName = column.getTableName();
            if ( !seenTable.contains(dbTableName)) {
                clauses.add(Clause.eq(ctypeHandler.getCollectionIdColumn(),
                                      collection.getId()));
                clauses.add(Clause.join(Tables.ENTRIES.COL_ID,
                                        dbTableName + ".id"));
                seenTable.add(dbTableName);
            }
            String v = values[valIdx].toString();
            if (column.getName().equals("ensemble")) {
                clauses.add(Clause.eq(column.getName(), "clim"));
            } else {
                if (v.length() > 0) {
                    clauses.add(Clause.eq(column.getName(), v));
                }
            }

        }
        List[] pair = outputHandler.getEntryManager().getEntries(request,
                          clauses, ctypeHandler.getGranuleTypeHandler());

        return pair[1];
    }

    /**
     * Get the repository
     *
     * @return the repository
     */
    protected Repository getRepository() {
        return repository;
    }

    /**
     * Get the output handler
     *
     * @return the output handler
     */
    protected CDOOutputHandler getOutputHandler() {
        return outputHandler;
    }

    /**
     * Initialize the CDO command list
     *
     * @return  the initial list of CDO commands
     */
    protected List<String> initCDOCommand() {
        List<String> newCommands = new ArrayList<String>();
        newCommands.add(getOutputHandler().getCDOPath());
        newCommands.add("-L");
        newCommands.add("-s");
        newCommands.add("-O");

        return newCommands;
    }

    /**
     * Run the process
     *
     * @param commands  the list of commands to run
     * @param processDir  the processing directory
     * @param outFile     the outfile
     *
     * @throws Exception problem running commands
     */
    protected void runProcess(List<String> commands, File processDir,
                              File outFile)
            throws Exception {

        String[] results = getRepository().executeCommand(commands, null,
                               processDir, 60);
        String errorMsg = results[1];
        String outMsg   = results[0];
        if ( !outFile.exists()) {
            if (outMsg.length() > 0) {
                throw new IllegalArgumentException(outMsg);
            }
            if (errorMsg.length() > 0) {
                throw new IllegalArgumentException(errorMsg);
            }
            if ( !outFile.exists()) {
                throw new IllegalArgumentException(
                    "Humm, the CDO processing failed for some reason");
            }
        }
    }

    /**
     * Add the statitics widget  - use instead of CDOOutputHandler
     *
     * @param request  the Request
     * @param sb       the HTML
     */
    public void addStatsWidget(Request request, StringBuilder sb) {
        sb.append(
            HtmlUtils.formEntry(
                Repository.msgLabel("Statistic"),
                HtmlUtils.radio(
                    CDOOutputHandler.ARG_CDO_STAT,
                    CDOOutputHandler.STAT_MEAN,
                    RepositoryManager.getShouldButtonBeSelected(
                        request, CDOOutputHandler.ARG_CDO_STAT,
                        CDOOutputHandler.STAT_MEAN, true)) + Repository.msg(
                            "Mean") + HtmlUtils.space(2)
                                    + HtmlUtils.radio(
                                        CDOOutputHandler.ARG_CDO_STAT,
                                        CDOOutputHandler.STAT_ANOM,
                                        RepositoryManager.getShouldButtonBeSelected(
                                            request,
                                            CDOOutputHandler.ARG_CDO_STAT,
                                            CDOOutputHandler.STAT_ANOM,
                                            false)) + Repository.msg(
                                                "Anomaly")));
    }

}
