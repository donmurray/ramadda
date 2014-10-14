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
import org.ramadda.repository.type.*;

import org.w3c.dom.*;

import ucar.unidata.util.IOUtil;
import ucar.unidata.xml.XmlUtil;


/**
 *
 */
public class ServiceFileTypeHandler extends ServiceTypeHandler {



    /**
     * _more_
     *
     * @param repository _more_
     * @param entryNode _more_
     *
     * @throws Exception _more_
     */
    public ServiceFileTypeHandler(Repository repository, Element entryNode)
            throws Exception {
        super(repository, entryNode);
    }



    /**
     * _more_
     *
     * @param request _more_
     * @param entry _more_
     * @param appendable _more_
     *
     * @throws Exception _more_
     */
    public void getServiceXml(Request request, Entry entry,
                              Appendable appendable)
            throws Exception {
        appendable.append(
            IOUtil.readContents(
                getStorageManager().getFileInputStream(
                    entry.getFile().toString())));
    }


    /**
     * _more_
     *
     * @param entry _more_
     *
     * @throws Exception _more_
     */
    @Override
    public void initializeNewEntry(Entry entry) throws Exception {
        super.initializeNewEntry(entry);
        Element root = XmlUtil.getRoot(
                           IOUtil.readContents(
                               getStorageManager().getFileInputStream(
                                   entry.getFile().toString())));
        Element service;
        if (root.getTagName().equals(Service.TAG_SERVICE)) {
            service = root;
        } else {
            service = XmlUtil.findChild(root, Service.TAG_SERVICE);
        }
        /*
<params>
<param  name="17ae4559-9ae0-499d-93b0-1295d00b4b60.imagemagick.convert.type.input_file_hidden" ><![CDATA[58607c20-77e3-4c70-8f96-c9307f6ec835]]></param>
        */

        String  paramsXml = "";
        Element params    = XmlUtil.findChild(service, Service.TAG_PARAMS);
        if (params != null) {
            paramsXml = XmlUtil.toString(params);
        }
        entry.getTypeHandler().getEntryValues(entry)[IDX_PARAMETERS] =
            paramsXml;
    }
}
