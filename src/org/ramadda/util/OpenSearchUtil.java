/*
* Copyright 2008-2015 Geode Systems LLC
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

package org.ramadda.util;


import org.w3c.dom.*;


import ucar.unidata.xml.XmlUtil;

import java.util.HashSet;
import java.util.List;


/**
 */
public class OpenSearchUtil {

    /** _more_ */
    public static final String XMLNS = "http://a9.com/-/spec/opensearch/1.1/";

    /** _more_ */
    public static final String XMLNS_GEO =
        "http://a9.com/-/opensearch/extensions/geo/1.0/";

    /** _more_ */
    public static final String XMLNS_TIME =
        "http://a9.com/-/opensearch/extensions/time/1.0/";


    /** _more_ */
    public static final String MIMETYPE =
        "application/opensearchdescription+xml";

    /** _more_ */
    public static final String TAG_OPENSEARCHDESCRIPTION =
        "OpenSearchDescription";

    /** _more_ */
    public static final String TAG_SHORTNAME = "ShortName";

    /** _more_ */
    public static final String TAG_DESCRIPTION = "Description";

    /** _more_ */
    public static final String TAG_TAGS = "Tags";

    /** _more_ */
    public static final String TAG_CONTACT = "Contact";

    /** _more_ */
    public static final String TAG_URL = "Url";

    /** _more_ */
    public static final String TAG_LONGNAME = "LongName";

    /** _more_ */
    public static final String TAG_IMAGE = "Image";

    /** _more_ */
    public static final String TAG_QUERY = "Query";

    /** _more_ */
    public static final String TAG_DEVELOPER = "Developer";

    /** _more_ */
    public static final String TAG_ATTRIBUTION = "Attribution";

    /** _more_ */
    public static final String TAG_SYNDICATIONRIGHT = "SyndicationRight";

    /** _more_ */
    public static final String TAG_ADULTCONTENT = "AdultContent";

    /** _more_ */
    public static final String TAG_LANGUAGE = "Language";

    /** _more_ */
    public static final String TAG_OUTPUTENCODING = "OutputEncoding";

    /** _more_ */
    public static final String TAG_INPUTENCODING = "InputEncoding";

    /** _more_ */
    public static final String ATTR_XMLNS = "xmlns";

    /** _more_ */
    public static final String ATTR_XMLNS_GEO = "xmlns:geo";

    /** _more_ */
    public static final String ATTR_XMLNS_TIME = "xmlns:time";

    /** _more_ */
    public static final String ATTR_BBOX = "bbox";

    /** _more_ */
    public static final String ATTR_TEMPLATE = "template";

    /** _more_ */
    public static final String ATTR_TYPE = "type";

    /** _more_ */
    public static final String ATTR_HEIGHT = "height";

    /** _more_ */
    public static final String ATTR_WIDTH = "width";

    /** _more_ */
    public static final String ATTR_ROLE = "role";

    /** _more_ */
    public static final String ATTR_SEARCHTERMS = "searchTerms";

    /** _more_ */
    public static final String MACRO_TEXT = "{searchTerms}";

    /** _more_ */
    public static final String MACRO_BBOX = "{geo:box?}";

    /** _more_ */
    public static final String MACRO_TIME_START = "{time:start?}";

    /** _more_ */
    public static final String MACRO_TIME_END = "{time:end?}";



    /**
     * _more_
     *
     * @return _more_
     *
     * @throws Exception _more_
     */


    public static Element getRoot() throws Exception {
        Document doc = XmlUtil.makeDocument();
        Element root = XmlUtil.create(doc, TAG_OPENSEARCHDESCRIPTION, null,
                                      new String[] {
            ATTR_XMLNS, XMLNS, ATTR_XMLNS_GEO, XMLNS_GEO, ATTR_XMLNS_TIME,
            XMLNS_TIME,
        });

        return root;
    }

    /*
<ShortName>Web Search</ShortName>
<Description>Use Example.com to search the Web.</Description>
<Tags>example web</Tags>
<Contact>admin@example.com</Contact>
<SyndicationRight>open</SyndicationRight>
<AdultContent>false</AdultContent>
<Language>en-us</Language>
<OutputEncoding>UTF-8</OutputEncoding>
<InputEncoding>UTF-8</InputEncoding>

    */

    /**
     * _more_
     *
     * @param root _more_
     * @param name _more_
     * @param description _more_
     * @param email _more_
     *
     * @throws Exception _more_
     */
    public static void addBasicTags(Element root, String name,
                                    String description, String email)
            throws Exception {
        if (name.length() > 16) {
            name = name.substring(0, 15);
        }
        String[] tags = new String[] {
            TAG_SHORTNAME, name, TAG_DESCRIPTION, description, TAG_CONTACT,
            email, TAG_SYNDICATIONRIGHT, "open", TAG_ADULTCONTENT, "false",
            TAG_LANGUAGE, "en-us", TAG_OUTPUTENCODING, "UTF-8",
            TAG_INPUTENCODING, "UTF-8"
        };
        for (int i = 0; i < tags.length; i += 2) {
            ((Element) XmlUtil.create(tags[i], root)).appendChild(
                XmlUtil.makeCDataNode(
                    root.getOwnerDocument(), tags[i + 1], false));
        }
    }





}
