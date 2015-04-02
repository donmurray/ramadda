/**
* Copyright (c) 2008-2015 Geode Systems LLC
* This Software is licensed under the Geode Systems RAMADDA License available in the source distribution in the file 
* ramadda_license.txt. The above copyright notice shall be included in all copies or substantial portions of the Software.
*/

package org.ramadda.geodata.model;


import org.ramadda.repository.Association;
import org.ramadda.repository.Entry;
import org.ramadda.repository.Repository;
import org.ramadda.repository.RepositoryManager;
import org.ramadda.repository.Request;
import org.ramadda.repository.Resource;
import org.ramadda.repository.database.Tables;
import org.ramadda.repository.job.JobManager;
import org.ramadda.repository.type.CollectionTypeHandler;
import org.ramadda.repository.type.Column;
import org.ramadda.repository.type.GranuleTypeHandler;
import org.ramadda.repository.type.TypeHandler;


import org.ramadda.service.Service;
import org.ramadda.service.ServiceInput;
import org.ramadda.service.ServiceOperand;
import org.ramadda.sql.Clause;
import org.ramadda.util.HtmlUtils;

import ucar.unidata.util.IOUtil;


import java.io.File;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;


/**
 * Base class for CDO data processes
 */
public abstract class CDODataProcess extends Service {

    /** months */
    protected static final String[] MONTHS = {
        "Jan", "Feb", "Mar", "Apr", "May", "Jun", "Jul", "Aug", "Sep", "Oct",
        "Nov", "Dec"
    };

    /** the type handler associated with this */
    private CDOOutputHandler outputHandler;


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
        super(repository, id, label);
        outputHandler = new CDOOutputHandler(repository);
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
     * Make a climatology for the given entry
     * @param request the request
     * @param entry the entry
     * @param dpi _more_
     * @param tail _more_
     *
     * @return _more_
     *
     * @throws Exception _more_
     */
    protected Entry makeClimatology(Request request, Entry entry,
                                    ServiceInput dpi, String tail)
            throws Exception {
        String climName = IOUtil.stripExtension(tail) + "_clim.nc";
        File climFile = new File(IOUtil.joinDir(dpi.getProcessDir(),
                            climName));
        List<String> commands = initCDOService();
        commands.add("ymonmean");
        commands.add(entry.getResource().getPath());
        commands.add(climFile.toString());
        runProcess(commands, dpi.getProcessDir(), climFile);
        Resource resource = new Resource(climFile, Resource.TYPE_LOCAL_FILE);
        TypeHandler myHandler = getRepository().getTypeHandler("file", false,
                                    true);
        Entry climEntry = new Entry(myHandler, true, climFile.toString());
        climEntry.setResource(resource);
        climEntry.addAssociation(new Association(getRepository().getGUID(),
                "generated product", "product generated from", entry.getId(),
                climEntry.getId()));

        return climEntry;

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
    protected List<String> initCDOService() {
        List<String> newServices = new ArrayList<String>();
        newServices.add(getOutputHandler().getCDOPath());
        newServices.add("-L");
        newServices.add("-s");
        newServices.add("-O");

        return newServices;
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

        //System.out.println(commands);
        long millis = System.currentTimeMillis();
        JobManager.CommandResults results =
            getRepository().getJobManager().executeCommand(commands, null,
                processDir, 60);
        //System.out.println("processing took: " + (System.currentTimeMillis()-millis));
        String errorMsg = results.getStderrMsg();
        String outMsg   = results.getStdoutMsg();
        if ( !outFile.exists()) {
            if (outMsg.length() > 0) {
                throw new IllegalArgumentException(outMsg);
            }
            if (errorMsg.length() > 0) {
                throw new IllegalArgumentException(errorMsg);
            }
            if ( !outFile.exists()) {
                throw new IllegalArgumentException("Error processing data.");
            }
        }
    }

    /**
     * Add the statitics widget  - use instead of CDOOutputHandler
     *
     * @param request  the Request
     * @param sb       the HTML
     *
     * @throws Exception _more_
     */
    public void addStatsWidget(Request request, Appendable sb)
            throws Exception {
        sb.append(
            HtmlUtils.formEntry(
                Repository.msgLabel("Statistic"),
                HtmlUtils.radio(
                    CDOOutputHandler.ARG_CDO_STAT,
                    CDOOutputHandler.STAT_MEAN,
                    RepositoryManager.getShouldButtonBeSelected(
                        request, CDOOutputHandler.ARG_CDO_STAT,
                        CDOOutputHandler.STAT_MEAN, true)) + HtmlUtils.space(
                            1) + Repository.msg("Average")
                               + HtmlUtils.space(2)
                               + HtmlUtils.radio(
                                   CDOOutputHandler.ARG_CDO_STAT,
                                   CDOOutputHandler.STAT_ANOM,
                                   RepositoryManager.getShouldButtonBeSelected(
                                       request,
                                       CDOOutputHandler.ARG_CDO_STAT,
                                       CDOOutputHandler.STAT_ANOM,
                                       false)) + HtmlUtils.space(1)
                                           + Repository.msg("Anomaly")));
    }

    /**
     * Can we handle this input
     *
     * @param input  the input
     *
     * @return true if we can, otherwise false
     */
    public boolean canHandle(ServiceInput input) {
        if ( !getOutputHandler().isEnabled()) {
            return false;
        }

        for (ServiceOperand op : input.getOperands()) {
            if (checkForValidEntries(op.getEntries())) {
                continue;
            } else {
                return false;
            }
        }

        return true;
    }

    /**
     * Check for valid entries.  Subclasses need override as necessary
     * @param entries  list of entries
     * @return
     */
    protected abstract boolean checkForValidEntries(List<Entry> entries);

    /**
     * _more_
     *
     * @param years _more_
     * @param offset _more_
     *
     * @return _more_
     */
    protected String makeCDOYearsString(List<Integer> years, int offset) {
        StringBuilder buf = new StringBuilder();
        for (int year = 0; year < years.size(); year++) {
            buf.append(years.get(year) + offset);
            if (year < years.size() - 1) {
                buf.append(",");
            }
        }

        return buf.toString();
    }

    /**
     * _more_
     *
     * @param request _more_
     * @param oneOfThem _more_
     *
     * @return _more_
     *
     * @throws Exception _more_
     */
    protected boolean doMonthsSpanYearEnd(Request request, Entry oneOfThem)
            throws Exception {
        if (request.defined(CDOOutputHandler.ARG_CDO_MONTHS)
                && request.getString(
                    CDOOutputHandler.ARG_CDO_MONTHS).equalsIgnoreCase(
                    "all")) {
            return false;
        }
        // Can't handle years requests yet.
        //if (request.defined(CDOOutputHandler.ARG_CDO_YEARS)
        //        || request.defined(CDOOutputHandler.ARG_CDO_YEARS + "1")) {
        //    return false;
        //}
        if (request.defined(CDOOutputHandler.ARG_CDO_STARTMONTH)
                || request.defined(CDOOutputHandler.ARG_CDO_ENDMONTH)) {
            int startMonth =
                request.defined(CDOOutputHandler.ARG_CDO_STARTMONTH)
                ? request.get(CDOOutputHandler.ARG_CDO_STARTMONTH, 1)
                : 1;
            int endMonth = request.defined(CDOOutputHandler.ARG_CDO_ENDMONTH)
                           ? request.get(CDOOutputHandler.ARG_CDO_ENDMONTH,
                                         startMonth)
                           : startMonth;
            // if they requested all months, no need to do a select on month
            if ((startMonth == 1) && (endMonth == 12)) {
                return false;
            }
            if (endMonth > startMonth) {
                return false;
            } else if (startMonth > endMonth) {
                return true;
            }
        }

        return false;
    }
}
