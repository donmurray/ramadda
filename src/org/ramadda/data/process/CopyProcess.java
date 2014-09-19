/*
* Copyright 2008-2013 Geode Systems LLC
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


import org.ramadda.repository.Entry;


import org.ramadda.repository.Repository;
import org.ramadda.repository.Request;
import org.ramadda.repository.Resource;
import org.ramadda.repository.Result;

import ucar.unidata.util.IOUtil;


import java.io.File;

import java.util.ArrayList;
import java.util.List;



/**
 * A class for holding process information
 */
public class CopyProcess extends DataProcess {

    /**
     * Default ctor
     */
    public CopyProcess() {
        this("foo", "bar");
    }

    /**
     * Create a DownloadProcess
     *
     * @param id       the unique id for this DataProcess
     * @param label    the label for this DataProcess
     */
    public CopyProcess(Repository repository, String id, String label) {
        super(repository, id, label);
    }

    /**
     * Add to form
     *
     * @param request  the Request
     * @param input    the Entry
     * @param sb       the form
     *
     * @throws Exception  problem adding to the form
     */
    public void addToForm(Request request, CommandInput input,
                          StringBuilder sb)
            throws Exception {}


    /**
     * Process the request
     *
     * @param request  The request
     * @param dpi  the input
     *
     * @return  the processed data
     *
     * @throws Exception  problem processing
     */
    public DataProcessOutput processRequest(Request request,
                                            CommandInput dpi)
            throws Exception {
        File        baseDir = dpi.getProcessDir();
        List<Entry> results = new ArrayList<Entry>();
        for (CommandOperand op : dpi.getOperands()) {
            for (Entry e : op.getEntries()) {
                File f       = e.getFile();
                File newFile = new File(IOUtil.joinDir(baseDir, f.getName()));
                IOUtil.copyFile(f, newFile);
                Entry entry = new Entry();
                entry.setResource(new Resource(newFile,
                        Resource.TYPE_LOCAL_FILE));
                results.add(entry);
            }
        }

        return new DataProcessOutput(new CommandOperand("Copied Entries",
                results));
    }

    /**
     * Can we handle this type of DataProcessInput?
     *
     *
     * @param dpi _more_
     * @return true if we can handle
     */
    public boolean canHandle(CommandInput dpi) {
        return (dpi != null) && !dpi.getOperands().isEmpty();
    }

}
