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

package org.ramadda.plugins.biblio;


import org.ramadda.repository.*;
import org.ramadda.repository.auth.*;
import org.ramadda.repository.metadata.*;
import org.ramadda.repository.output.*;
import org.ramadda.repository.type.*;
import org.ramadda.util.HtmlUtils;


import org.w3c.dom.*;

import java.text.SimpleDateFormat;

import java.util.ArrayList;
import java.util.Date;
import java.util.Enumeration;
import java.util.GregorianCalendar;
import java.util.Hashtable;
import java.util.List;
import java.util.Properties;


/**
 *
 *
 */
public class BiblioTypeHandler extends GenericTypeHandler {

    /** _more_ */
    private SimpleDateFormat dateFormat;

    /**
     * _more_
     *
     * @param repository _more_
     * @param entryNode _more_
     *
     * @throws Exception _more_
     */
    public BiblioTypeHandler(Repository repository, Element entryNode)
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
     */
    public String getEntryListName(Request request, Entry entry) {
        return "NAME:" + entry.getName();
    }



    /**
     * _more_
     *
     * @param request _more_
     * @param entry _more_
     * @param date _more_
     * @param extra _more_
     *
     * @return _more_
     */
    public String formatDate(Request request, Entry entry, Date date,
                             String extra) {
        return formatDate(request, date);
    }

    /**
     * _more_
     *
     * @param request _more_
     * @param d _more_
     *
     * @return _more_
     */
    public String formatDate(Request request, Date d) {
        if (dateFormat == null) {
            dateFormat = new SimpleDateFormat("yyyy-MM");
            dateFormat.setTimeZone(RepositoryBase.TIMEZONE_UTC);
        }
        synchronized (dateFormat) {
            return dateFormat.format(d);
        }
    }

    /*
    <column name="type" type="enumerationplus"  label="Type" values="Generic,Journal Article,Report" />
    <column name="primary_author" type="string" size="500" changetype="true"  label="Primary Author"  cansearch="true"/>
    <column name="institution" type="string"  label="Institution"  cansearch="true"/>
    <column name="other_authors" type="list"  changetype="true" size="5000" label="Other Authors"  rows="5"/>
    <column name="publication" type="enumerationplus"  label="Publication"  />
    <column name="volume_number" type="string"  label="Volume"  />
    <column name="issue_number" type="string"  label="Issue"  />
    <column name="pages" type="string"  label="Pages"  />
    <column name="doi" type="string"  label="DOI"  />
    <column name="link" type="url"  label="Link"  />
   */


    /* (non-Javadoc)
     * @see org.ramadda.repository.type.TypeHandler#getHtmlDisplay(org.ramadda.repository.Request, org.ramadda.repository.Entry)
     */
    @Override
    public Result getHtmlDisplay(Request request, Entry entry) throws Exception {
        StringBuilder sb = new StringBuilder();
        GregorianCalendar cal =
                new GregorianCalendar(RepositoryUtil.TIMEZONE_DEFAULT);
        cal.setTime(new Date(entry.getStartDate()));
        Object[] values = entry.getTypeHandler().getEntryValues(entry);
        int      idx    = 0;
        String  author = values[idx++].toString();
        String  type   = values[idx++].toString();
        if (type.toString().equals("Journal Article")) {
            String title = entry.getName();
            Object institution = values[idx++];
            String others = values[idx++].toString();
            String authors = formatAuthors(author, others);
            String pub = values[idx++].toString();
            String volume = values[idx++].toString();
            String issue = values[idx++].toString();
            String pages = values[idx++].toString();
            String doi = values[idx++].toString();
            sb.append(HtmlUtils.open(HtmlUtils.TAG_DIV, "style=\"max-width:700px;\""));
            sb.append(authors);
            sb.append(", ");
            sb.append(cal.get(GregorianCalendar.YEAR));
            sb.append(": ");
            sb.append(title);
            if (!title.endsWith(".")) {
              sb.append(".");
            }
            sb.append(" ");
            sb.append(HtmlUtils.italics(pub));
            sb.append(", ");
            if (volume != null) {
                sb.append(HtmlUtils.bold(volume));
            }
            // TODO: deal with issues
            sb.append(", ");
            if (pages != null) {
                sb.append(pages);
                sb.append(".");
            }
            if (doi != null) {
                sb.append(" doi: ");
                if (!doi.startsWith("http")) {
                    doi = "http://dx.doi.org/"+doi;
                }
                sb.append(HtmlUtils.href(doi, doi));
            }
            sb.append(HtmlUtils.close(HtmlUtils.TAG_DIV));
            return new Result("", sb);
            
        } else {
            return super.getHtmlDisplay(request, entry);
        }
    }
    
    private String formatAuthors(String primary, String others) {
        return primary+", "+others;
    }

}
