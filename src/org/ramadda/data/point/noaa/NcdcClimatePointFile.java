/*
 * Copyright (c) 2008-2015 Geode Systems LLC
 * This Software is licensed under the Geode Systems RAMADDA License available in the source distribution in the file 
 * ramadda_license.txt. The above copyright notice shall be included in all copies or substantial portions of the Software.
 */

package org.ramadda.data.point.noaa;


import org.ramadda.data.point.*;
import org.ramadda.data.point.text.*;
import org.ramadda.data.record.*;

import java.io.*;


/**
 */
public class NcdcClimatePointFile extends HeaderPointFile {


    /**
     * ctor
     *
     *
     * @param filename _more_
     *
     * @throws IOException On badness
     */
    public NcdcClimatePointFile(String filename) throws IOException {
        super(filename);
    }


    /**
     * _more_
     *
     * @param args _more_
     */
    public static void main(String[] args) {
        PointFile.test(args, NcdcClimatePointFile.class);
    }

}
