/**
* Copyright (c) 2008-2015 Geode Systems LLC
* This Software is licensed under the Geode Systems RAMADDA License available in the source distribution in the file 
* ramadda_license.txt. The above copyright notice shall be included in all copies or substantial portions of the Software.
*/

package org.ramadda.repository.metadata;


import org.ramadda.repository.*;


import org.ramadda.sql.SqlUtil;
import org.ramadda.util.HtmlTemplate;
import org.ramadda.util.HtmlUtils;


import org.w3c.dom.*;

import ucar.unidata.ui.ImageUtils;


import ucar.unidata.util.DateUtil;
import ucar.unidata.util.IOUtil;
import ucar.unidata.util.Misc;


import ucar.unidata.util.StringUtil;
import ucar.unidata.xml.XmlUtil;

import java.awt.Image;


import java.io.*;
import java.io.File;
import java.io.FileInputStream;

import java.net.URL;
import java.net.URLConnection;


import java.sql.Statement;

import java.util.ArrayList;
import java.util.Hashtable;
import java.util.List;


/**
 *
 *
 * @author RAMADDA Development Team
 * @version $Revision: 1.3 $
 */
public class ContentMetadataHandler extends MetadataHandler {


    /** _more_ */
    public static final String TYPE_THUMBNAIL = "content.thumbnail";

    /** _more_ */
    public static final String TYPE_ICON = "content.icon";

    /** _more_ */
    public static final String TYPE_ATTACHMENT = "content.attachment";

    /** _more_ */
    public static final String TYPE_PAGESTYLE = "content.pagestyle";

    /** _more_ */
    public static final String TYPE_KEYWORD = "content.keyword";

    /** _more_ */
    public static final String TYPE_URL = "content.url";

    /** _more_ */
    public static final String TYPE_EMAIL = "content.email";

    /** _more_ */
    public static final String TYPE_AUTHOR = "content.author";

    /** _more_ */
    public static final String TYPE_LOGO = "content.logo";

    /** _more_ */
    public static final String TYPE_JYTHON = "content.jython";

    /** _more_ */
    public static final String TYPE_CONTACT = "content.contact";

    /** _more_ */
    public static final String TYPE_SORT = "content.sort";

    /** _more_ */
    public static final String TYPE_TIMEZONE = "content.timezone";

    /** _more_ */
    public static final String TYPE_ALIAS = "content.alias";

    /** _more_ */
    public static final String TYPE_TEMPLATE = "content.pagetemplate";

    /**
     * _more_
     *
     * @param repository _more_
     *
     * @throws Exception _more_
     */
    public ContentMetadataHandler(Repository repository) throws Exception {
        super(repository);
    }


    /**
     * _more_
     *
     * @param element _more_
     *
     * @return _more_
     */
    public String getEnumerationValues(MetadataElement element) {
        if (element.getName().equals("template")) {
            StringBuffer sb = new StringBuffer();
            for (HtmlTemplate htmlTemplate :
                    getRepository().getPageHandler().getTemplates()) {
                sb.append(htmlTemplate.getId());
                sb.append(":");
                sb.append(htmlTemplate.getName());
                sb.append(",");
            }

            return sb.toString();
        }

        return "";
    }

}
