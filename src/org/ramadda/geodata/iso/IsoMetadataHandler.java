/**
* Copyright (c) 2008-2015 Geode Systems LLC
* This Software is licensed under the Geode Systems RAMADDA License available in the source distribution in the file 
* ramadda_license.txt. The above copyright notice shall be included in all copies or substantial portions of the Software.
*/

package org.ramadda.geodata.iso;


import org.ramadda.repository.*;
import org.ramadda.repository.metadata.*;
import org.ramadda.util.HtmlUtils;

import org.w3c.dom.*;

import ucar.unidata.util.DateUtil;

import ucar.unidata.util.StringUtil;
import ucar.unidata.util.TwoFacedObject;
import ucar.unidata.xml.XmlUtil;

import java.io.File;


import java.sql.Statement;


import java.util.ArrayList;
import java.util.Date;
import java.util.Hashtable;
import java.util.List;


/**
 *
 *
 * @author IDV Development Team
 * @version $Revision: 1.3 $
 */
public class IsoMetadataHandler extends MetadataHandler {

    /**
     * _more_
     *
     * @param repository _more_
     *
     * @throws Exception _more_
     */
    public IsoMetadataHandler(Repository repository) throws Exception {
        super(repository);
    }


    /**
     * _more_
     *
     * @param repository _more_
     * @param node _more_
     * @throws Exception _more_
     */
    public IsoMetadataHandler(Repository repository, Element node)
            throws Exception {
        super(repository, node);
    }




}
