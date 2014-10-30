/*
/** Copyright 2008-2014 Geode Systems LLC
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

package org.ramadda.plugins.edgar;


import org.ramadda.repository.*;
import org.ramadda.repository.metadata.*;
import org.ramadda.repository.type.*;
import org.ramadda.util.HtmlUtils;

import org.w3c.dom.*;

import ucar.unidata.util.IOUtil;
import ucar.unidata.util.StringUtil;


import ucar.unidata.xml.XmlUtil;

import java.io.*;

import java.util.Date;
import java.util.List;

import javax.mail.*;
import javax.mail.internet.*;


/**
 *
 *
 */
public class EdgarFilingTypeHandler extends GenericTypeHandler {



    /**
     * _more_
     *
     * @param repository _more_
     * @param entryNode _more_
     *
     * @throws Exception _more_
     */
    public EdgarFilingTypeHandler(Repository repository, Element entryNode)
            throws Exception {
        super(repository, entryNode);
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

        //If the file for the entry does not exist then return
        if ( !entry.isFile()) {
            return;
        }
        InputStream fis = getStorageManager().getFileInputStream(
                              entry.getFile().toString());
        StringBuilder  headerSB = new StringBuilder();
        BufferedReader br = new BufferedReader(new InputStreamReader(fis));
        int            cnt      = 0;
        while (true) {
            cnt++;
            String line = br.readLine();
            if ((cnt > 1000) || (line == null)
                    || (line.indexOf("</SEC-HEADER>") >= 0)) {
                break;
            }
            headerSB.append(line);
            headerSB.append("\n");
        }
        IOUtil.close(fis);
        String header = headerSB.toString();

        String dateString = StringUtil.findPattern(header,
                                "CONFORMED PERIOD OF REPORT:([^\\n]+)\\n");
        if (dateString != null) {
            dateString = dateString.trim();
            Date date =
                RepositoryUtil.makeDateFormat("yyyyMMdd").parse(dateString);
            entry.setStartDate(date.getTime());
            entry.setEndDate(date.getTime());
        }

        Object[] values   = getEntryValues(entry);
        String[] patterns = new String[] {
            "FORM TYPE:([^\\n]+)\\n", "ACCESSION NUMBER:([^\\n]+)\\n",
            "COMPANY CONFORMED NAME:([^\\n]+)\\n",
            //CIK number
            "ACCESSION NUMBER:([^-]+)-", "CENTRAL INDEX KEY:([^\\n]+)\\n",
            "STANDARD INDUSTRIAL CLASSIFICATION:([^\\n]+)\\n",
            "IRS NUMBER:([^\\n]+)\\n", "STATE OF INCORPORATION:([^\\n]+)\\n"
        };
        for (int i = 0; i < patterns.length; i++) {
            String value = StringUtil.findPattern(header, patterns[i]);
            if (value != null) {
                value = value.trim();
            }
            values[i] = value;
        }

        String companyName =
            (String) entry.getTypeHandler().getEntryValue(entry,
                "company_name");
        if (companyName != null) {
            entry.setName(companyName + "-" + entry.getName());
        }


    }




}
