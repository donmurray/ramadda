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

package org.ramadda.plugins.media;


import org.ramadda.data.process.*;


import org.ramadda.repository.*;

import org.w3c.dom.*;

import java.util.List;



/**
 *
 * @author Jeff McWhirter/geodesystems.com
 */
public class TabularService extends Service {


    /**
     * ctor
     *
     * @param repository _more_
     * @throws Exception _more_
     */
    public TabularService(Repository repository) throws Exception {
        super(repository, (Element) null);
    }


    /**
     * _more_
     *
     * @return _more_
     *
     * @throws Exception _more_
     */
    public TabularOutputHandler getTabularOutputHandler() throws Exception {
        return (TabularOutputHandler) getRepository().getOutputHandler(
            TabularOutputHandler.class);
    }

    /**
     * _more_
     *
     * @param request _more_
     * @param service _more_
     * @param input _more_
     * @param args _more_
     *
     *
     * @return _more_
     * @throws Exception _more_
     */
    public boolean extractSheet(Request request, Service service,
                                ServiceInput input, List args)
            throws Exception {
        return getTabularOutputHandler().extractSheet(request, service,
                input, args);
    }


    public boolean csv(Request request, Service service,
                       ServiceInput input, List args)
            throws Exception {
        return getTabularOutputHandler().csv(request, service,
                input, args);
    }


}
