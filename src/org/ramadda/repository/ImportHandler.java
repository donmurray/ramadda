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

package org.ramadda.repository;


import org.w3c.dom.*;

import ucar.unidata.util.TwoFacedObject;


import java.io.InputStream;

import java.util.List;


/**
 */
public abstract class ImportHandler extends RepositoryManager {

    /**
     * _more_
     */
    public ImportHandler() {
        super(null);
    }

    /**
     * _more_
     *
     * @param repository _more_
     */
    public ImportHandler(Repository repository) {
        super(repository);
    }

    /**
     * _more_
     *
     * @param importTypes _more_
     * @param formBuffer _more_
     */
    public void addImportTypes(List<TwoFacedObject> importTypes,
                               Appendable formBuffer) {}

    /**
     * _more_
     *
     * @param request _more_
     * @param repository _more_
     * @param uploadedFile _more_
     * @param parentEntry _more_
     *
     * @return _more_
     *
     * @throws Exception _more_
     */
    public Result handleRequest(Request request, Repository repository,
                                String uploadedFile, Entry parentEntry)
            throws Exception {
        return null;
    }

    /**
     * _more_
     *
     *
     * @param request _more_
     * @param fileName _more_
     * @param stream _more_
     *
     * @return _more_
     *
     * @throws Exception _more_
     */
    public InputStream getStream(Request request, Entry parent, String fileName,
                                 InputStream stream)
            throws Exception {
        return null;
    }

    /**
     * _more_
     *
     *
     * @param request _more_
     * @param root _more_
     *
     * @return _more_
     *
     * @throws Exception _more_
     */
    public Element getDOM(Request request, Element root) throws Exception {
        return null;
    }

}
