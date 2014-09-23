/*
* Copyright 2008-2014 Geode Systems LLC
*
* Permission is hereby granted, free of charge, to any person obtaining a copy of this 
* software and associated documentation files (the "Software"), to deal in the Software 
* without restriction, including without limitation the rights to use, copy, modify, 
* merge, publish, distribute, sublicense, and/or sell copies of the Software, and to 
* permit persons to whom the Software is furnished to do so, subject to the following conditions:
* 
* The above copyright notice and this permission notice shall be included in all copies 
* or substantial portions of the Software.
* 
* THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, 
* INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR 
* PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE 
* FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR 
* OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER 
* DEALINGS IN THE SOFTWARE.
*/

package org.ramadda.data.process;


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
     * @param entry _more_
     * @param command _more_
     * @param info _more_
     * @param commands _more_
     *
     * @return _more_
     *
     * @throws Exception _more_
     */
    public boolean evaluate(Request request, Entry entry, Service command,
                            ServiceInfo info, List<String> commands)
            throws Exception {
        if (commands.size() <= 1) {
            return true;
        }

        String task = commands.get(1);
        if (task.equals(COMMAND_MV)) {
            File entryFile = new File(commands.get(2));
            if ( !IOUtil.isADescendent(info.getWorkDir(), entryFile)) {
                throw new IllegalArgumentException(
                    "Cannot move the entry file. Can only move temp files");
            }
            String newName = commands.get(3);
            IOUtil.moveFile(entryFile,
                            new File(IOUtil.joinDir(info.getWorkDir(),
                                newName)));
        } else if (task.equals(COMMAND_CP)) {
            File   entryFile = new File(commands.get(2));
            String newName   = commands.get(3);
            IOUtil.copyFile(entryFile,
                            new File(IOUtil.joinDir(info.getWorkDir(),
                                newName)));
        }

        return false;
    }

}
