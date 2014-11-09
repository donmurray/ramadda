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
import org.ramadda.repository.metadata.*;
import org.ramadda.repository.type.*;


import org.ramadda.util.HtmlUtils;
import org.ramadda.util.Utils;


import org.w3c.dom.*;

import ucar.unidata.util.IOUtil;

import ucar.unidata.util.Misc;
import ucar.unidata.util.StringUtil;


import ucar.unidata.xml.XmlUtil;

import java.io.File;
import java.io.InputStream;
import java.io.OutputStream;

import java.util.ArrayList;

import java.util.Date;
import java.util.List;



/**
 *
 *
 */
public class PdfTypeHandler extends GenericTypeHandler {


    /**
     * _more_
     *
     * @param repository _more_
     * @param entryNode _more_
     *
     * @throws Exception _more_
     */
    public PdfTypeHandler(Repository repository, Element entryNode)
            throws Exception {
        super(repository, entryNode);
    }

    /**
     * _more_
     *
     * @param entry _more_
     * @param service _more_
     * @param output _more_
     *
     * @throws Exception _more_
     */
    @Override
        public void handleServiceResults(Request request, Entry entry, Service service,
                                     ServiceOutput output)
            throws Exception {
        super.handleServiceResults(request, entry, service, output);
        if(request!=null && !request.get(ARG_METADATA_ADD,false)) {
            return;
        }
        List<Entry> entries = output.getEntries();
        if (entries.size() == 0) {
            return;
        }
        Entry serviceEntry = entries.get(0);
        if ( !serviceEntry.getResource().getPath().endsWith(".txt")) {
            return;
        }
        String results =
            IOUtil.readContents(serviceEntry.getFile().toString(),
                                getClass());
        if ( !Utils.stringDefined(results)) {
            return;
        }
        List<String> headerLines = new ArrayList<String>();
        String       firstLine   = null;
        for (String line : StringUtil.split(results, "\n", true, true)) {
            line = clean(line);
            if ((line.length() == 0) || line.startsWith("!")) {
                continue;
            }
            if (firstLine == null) {
                firstLine = line;

                continue;
            }
            headerLines.add(line);
            if (headerLines.size() >= 10) {
                break;
            }
        }

        if ((firstLine != null)
                && ( !Utils.stringDefined(entry.getName())
                     || entry.getResource().getPath().endsWith(
                         entry.getName()))) {
            firstLine = firstLine.replaceAll("(_\\s)+_","_");
            firstLine = firstLine.replaceAll("_+_","_");
            if (firstLine.length() > 100) {
                firstLine = firstLine.substring(0, 100);
            }
            entry.setName(firstLine);
        }
        if ((headerLines.size() > 0)
                && !Utils.stringDefined(entry.getDescription())) {
            String desc = "<pre class=\"ramadda-pre\">" +
                StringUtil.join("\n", headerLines);
            if (desc.length() > Entry.MAX_DESCRIPTION_LENGTH-10) {
                desc = desc.substring(0, Entry.MAX_DESCRIPTION_LENGTH - 10);
            }
            entry.setDescription(desc + "</pre>");
        }




    }


    /**
     * _more_
     *
     * @param s _more_
     *
     * @return _more_
     */
    private String clean(String s) {
        if (s == null) {
            return s;
        }
        s = Utils.removeNonAscii(s);
        s = s.trim();
        s = s.replaceAll("\n", " ");
        s = s.replaceAll("(_-)+", "");

        return s;
    }


}
