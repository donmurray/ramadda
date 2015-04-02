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

package org.ramadda.data.tools;


import org.ramadda.data.point.*;

import org.ramadda.data.record.*;
import org.ramadda.data.tools.*;

import java.io.*;

import java.util.ArrayList;
import java.util.List;


/**
 * Class description
 *
 *
 * @version        Enter version here..., Fri, May 21, '10
 * @author         Enter your name here...
 */
public class PointChecker extends RecordTool {

    /**
     * _more_
     *
     * @param args _more_
     *
     * @param argArray _more_
     *
     * @throws Exception _more_
     */
    public PointChecker(String[] argArray) throws Exception {
        super(null);
        List<String> args = processArgs(argArray);
        for (String arg : args) {
            System.err.println("Checking:" + arg);
            PointFile    pointFile = (PointFile) doMakeRecordFile(arg);
            StringBuffer sb        = new StringBuffer();
            pointFile.runCheck(arg, sb);
            System.err.println(sb);
        }
    }

    /**
     * _more_
     *
     * @param args _more_
     *
     * @throws Exception _more_
     */
    public static void main(String[] args) throws Exception {
        new PointChecker(args);
    }
}
