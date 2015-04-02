/**
* Copyright (c) 2008-2015 Geode Systems LLC
* This Software is licensed under the Geode Systems RAMADDA License available in the source distribution in the file 
* ramadda_license.txt. The above copyright notice shall be included in all copies or substantial portions of the Software.
*/

package org.ramadda.geodata.cdmdata;


import org.ramadda.repository.Repository;

import org.w3c.dom.Element;


/**
 * Created with IntelliJ IDEA.
 * User: opendap
 * Date: 5/4/13
 * Time: 9:08 AM
 * To change this template use File | Settings | File Templates.
 */
public class UFRadarTypeHandler extends RadarTypeHandler {


    /**
     * _more_
     *
     * @param repository _more_
     * @param entryNode _more_
     *
     * @throws Exception _more_
     */
    public UFRadarTypeHandler(Repository repository, Element entryNode)
            throws Exception {
        super(repository, entryNode);
    }


}
