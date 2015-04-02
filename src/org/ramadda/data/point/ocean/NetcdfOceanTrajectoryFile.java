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

package org.ramadda.data.point.ocean;


import org.ramadda.data.point.*;
import org.ramadda.data.point.netcdf.*;



import org.ramadda.data.record.*;
import org.ramadda.util.Utils;

import ucar.unidata.util.Misc;

import java.io.*;

import java.util.ArrayList;
import java.util.List;



/**
 * Class description
 *
 *
 * @version        $version$, Wed, Nov 13, '13
 * @author         Enter your name here...
 */
public class NetcdfOceanTrajectoryFile extends NetcdfTrajectoryFile {

    /**
     * ctor
     */
    public NetcdfOceanTrajectoryFile() {}




    /**
     * ctor
     *
     *
     *
     *
     *
     * @param filename _more_
     * @throws IOException On badness
     */
    public NetcdfOceanTrajectoryFile(String filename) throws IOException {
        super(filename);
    }


    /**
     * _more_
     *
     * @param visitInfo _more_
     *
     * @return _more_
     *
     * @throws Exception _more_
     */
    public VisitInfo prepareToVisit(VisitInfo visitInfo) throws Exception {
        super.prepareToVisit(visitInfo);
        //Need to find the platform or instrument property
        String platform = Misc.getProperty(getFileProperties(), "platform",
                                           "");

        //LOOK: this needs to be in the same order as the oceantypes.xml defines in the point plugin
        setFileMetadata(new Object[] { platform });

        return visitInfo;
    }


    /**
     * _more_
     *
     * @param args _more_
     *
     * @throws Exception _more_
     */
    public static void main(String[] args) throws Exception {
        PointFile.test(args, NetcdfOceanTrajectoryFile.class);
    }





}
