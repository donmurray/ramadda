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

package gov.noaa.esrl.psd.repository.data.model;



import org.ramadda.repository.Entry;
import org.ramadda.repository.Repository;
import org.ramadda.repository.harvester.FileInfo;
import org.ramadda.repository.harvester.PatternHarvester;
import org.ramadda.repository.type.TypeHandler;

import org.w3c.dom.Element;

import java.io.File;

import java.util.regex.Matcher;


/**
 * Class description
 *
 *
 */
public class NOAAClimateModelFileHarvester extends PatternHarvester {

    /**
     * Construct a new NOAAClimateModelFileHarvester
     *
     * @param repository  the Repository
     * @param id          the id
     *
     * @throws Exception problem creating harvester
     */
    public NOAAClimateModelFileHarvester(Repository repository, String id)
            throws Exception {
        super(repository, id);
    }

    /**
     * Create a new NOAAClimateModelFileHarvester from the XML
     *
     * @param repository  the Repository
     * @param element     the XML declaration
     *
     * @throws Exception  problem creating the harvester
     */
    public NOAAClimateModelFileHarvester(Repository repository,
                                         Element element)
            throws Exception {
        super(repository, element);
    }

    /**
     * Get the TypeHandler class
     *
     * @return  the class
     */
    public Class getTypeHandlerClass() {
        return NOAAClimateModelFileTypeHandler.class;
    }

    /**
     * Get the type handler
     *
     * @return  the TypeHandler or null
     *
     * @throws Exception  can't find type handler
     */
    public TypeHandler getTypeHandler() throws Exception {
        return getRepository()
            .getTypeHandler(NOAAClimateModelFileTypeHandler
                .TYPE_NOAA_FACTS_CLIMATE_MODELFILE);
    }


    /**
     * harvester description
     *
     * @return harvester description
     */
    public String getDescription() {
        return "NOAA Climate Model File";
    }

    /**
     * Should this harvester harvest the given file
     *
     * @param fileInfo file information
     * @param f the actual file
     * @param matcher pattern matcher
     * @param originalFile  the original file
     * @param entry   the Entry
     *
     * @return the new entry or null if nothing is harvested
     *
     * @throws Exception on badness
     * @Override
     */
    public Entry harvestFile(FileInfo fileInfo, File f, Matcher matcher)
            throws Exception {
        if ( !f.toString().endsWith(".nc")) {
            return null;
        }

        return super.harvestFile(fileInfo, f, matcher);
    }



    /**
     * Check for a .properties file that corresponds to the given data file.
     * If it exists than add it to the entry
     *
     * @param fileInfo File information
     * @param originalFile Data file
     * @param entry New entry
     *
     * @return The entry
     */
    @Override
    public Entry initializeNewEntry(FileInfo fileInfo, File originalFile,
                                    Entry entry) {
        try {
            //getRepository().getLogManager().logInfo(
            //    "ClimateModelFileHarvester:initializeNewEntry:"
            //    + entry.getResource());
            if (entry.getTypeHandler()
                    instanceof NOAAClimateModelFileTypeHandler) {
                //jeffmc: comment this out. initNewEntry should be called now
                //                ((NOAAClimateModelFileTypeHandler) entry.getTypeHandler())
                //                    .initializeEntry(entry);
            }

            //getRepository().getLogManager().logInfo(
            //    "ClimateModelFileHarvester:initializeNewEntry done");
            return entry;
        } catch (Exception exc) {
            throw new RuntimeException(exc);
        }
    }

}
