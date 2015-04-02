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


import org.ramadda.geodata.model.ClimateModelFileTypeHandler;
import org.ramadda.repository.Repository;

import org.w3c.dom.Element;


/**
 * Class description
 *
 *
 */
public class NOAAClimateModelFileTypeHandler extends ClimateModelFileTypeHandler {

    /** type identifier */
    public final static String TYPE_NOAA_FACTS_CLIMATE_MODELFILE =
        "noaa_facts_climate_modelfile";

    /**
     * Create a new NOAAClimateModelFileTypeHandler
     *
     * @param repository   the Repository
     * @param entryNode    the XML definition
     *
     * @throws Exception  problem creating the handler
     */
    public NOAAClimateModelFileTypeHandler(Repository repository,
                                           Element entryNode)
            throws Exception {
        super(repository, entryNode);
    }

}
