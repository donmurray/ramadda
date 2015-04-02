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
     *
     * @param request _more_
     * @param entry _more_
     *
     * @throws Exception _more_
     */
    @Override
    public void initializeNewEntry(Request request, Entry entry)
            throws Exception {
        super.initializeNewEntry(request, entry);

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
