/**
* Copyright (c) 2008-2015 Geode Systems LLC
* This Software is licensed under the Geode Systems RAMADDA License available in the source distribution in the file 
* ramadda_license.txt. The above copyright notice shall be included in all copies or substantial portions of the Software.
*/

package org.ramadda.service;


import java.util.List;



/**
 */
public interface ServiceProvider {

    /**
     * Get the DataProcesses that this supports
     *
     * @return the list of DataProcesses
     */
    public List<Service> getServices();
}
