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
import org.ramadda.repository.metadata.*;
import org.ramadda.repository.output.HtmlOutputHandler;
import org.ramadda.repository.output.ServiceOutputHandler;
import org.ramadda.repository.type.*;

import org.ramadda.util.HtmlUtils;


import org.w3c.dom.*;

import ucar.unidata.util.IOUtil;

import ucar.unidata.util.Misc;
import ucar.unidata.util.StringUtil;


import ucar.unidata.xml.XmlUtil;

import java.util.Date;
import java.util.Hashtable;
import java.util.List;


/**
 *
 *
 */
public class WorkflowTypeHandler extends GenericTypeHandler {



    /**
     * _more_
     *
     * @param repository _more_
     * @param entryNode _more_
     *
     * @throws Exception _more_
     */
    public WorkflowTypeHandler(Repository repository, Element entryNode)
            throws Exception {
        super(repository, entryNode);
    }


    /**
     * _more_
     *
     * @param request _more_
     * @param entry _more_
     *
     * @return _more_
     *
     * @throws Exception _more_
     */
    @Override
    public Result getHtmlDisplay(Request request, Entry entry)
            throws Exception {
        Element root = XmlUtil.getRoot(
                           getStorageManager().getFileInputStream(
                               entry.getFile().toString()));

        if (root.getTagName().equals(Service.TAG_SERVICES)) {
            root = XmlUtil.findChild(root, Service.TAG_SERVICE);
        }

        Service service = getRepository().makeService(root, false);

        //IMPORTANT! Do this because we don't allow a service xml entry file to have commands
        service.ensureSafeServices();

        ServiceOutputHandler soh = new ServiceOutputHandler(repository,
                                       service);
        StringBuilder sb = new StringBuilder();
        if ( !request.defined(soh.ARG_EXECUTE)
                && !request.defined(soh.ARG_SHOWCOMMAND)) {
            soh.makeForm(request, service, entry, null,
                         HtmlOutputHandler.OUTPUT_HTML, sb);

            return new Result("", sb);
        }

        return soh.evaluateService(request, HtmlOutputHandler.OUTPUT_HTML,
                                   entry, null, service);
    }

}
