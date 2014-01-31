package org.ramadda.geodata.model;

import java.io.File;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

import org.ramadda.data.process.DataProcess;
import org.ramadda.repository.Entry;
import org.ramadda.repository.Repository;
import org.ramadda.repository.Request;
import org.ramadda.repository.database.Tables;
import org.ramadda.repository.type.CollectionTypeHandler;
import org.ramadda.repository.type.Column;
import org.ramadda.repository.type.GranuleTypeHandler;
import org.ramadda.sql.Clause;

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

    public CDODataProcess(Repository repository, String id, String label) throws Exception {
        super(id, label);
        this.repository = repository;
        outputHandler     = new CDOOutputHandler(repository);
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
    protected void runProcess(List<String> commands, File processDir, File outFile)
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

}