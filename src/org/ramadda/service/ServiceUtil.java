/*
 * Copyright (c) 2008-2015 Geode Systems LLC
 * This Software is licensed under the Geode Systems RAMADDA License available in the source distribution in the file 
 * ramadda_license.txt. The above copyright notice shall be included in all copies or substantial portions of the Software.
 */

package org.ramadda.service;


import org.ramadda.repository.*;

import ucar.unidata.util.IOUtil;

import java.io.File;

import java.util.ArrayList;
import java.util.List;


/**
 *
 * @author RAMADDA Development Team
 */
public class ServiceUtil {

    /** _more_ */
    public static final String COMMAND_CP = "cp";


    /** _more_ */
    public static final String COMMAND_MV = "mv";

    /**
     * _more_
     */
    public ServiceUtil() {}


    /**
     * _more_
     *
     * @param request _more_
     * @param command _more_
     * @param input _more_
     * @param commands _more_
     *
     * @return _more_
     *
     * @throws Exception _more_
     */
    public boolean evaluate(Request request, Service command,
                            ServiceInput input, List<String> commands)
            throws Exception {
        if (commands.size() <= 1) {
            return true;
        }

        String task = commands.get(1);
        if (task.equals(COMMAND_MV)) {
            File entryFile = new File(commands.get(2));
            if ( !IOUtil.isADescendent(input.getProcessDir(), entryFile)) {
                throw new IllegalArgumentException(
                    "Cannot move the entry file. Can only move temp files");
            }
            String newName = commands.get(3);
            IOUtil.moveFile(entryFile,
                            new File(IOUtil.joinDir(input.getProcessDir(),
                                newName)));
        } else if (task.equals(COMMAND_CP)) {
            File   entryFile = new File(commands.get(2));
            String newName   = commands.get(3);
            IOUtil.copyFile(entryFile,
                            new File(IOUtil.joinDir(input.getProcessDir(),
                                newName)));
        }

        return false;
    }

}
