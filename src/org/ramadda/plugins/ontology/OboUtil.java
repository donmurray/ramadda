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

package org.ramadda.plugins.ontology;


import ucar.unidata.util.IOUtil;

import ucar.unidata.util.StringUtil;
import ucar.unidata.xml.XmlUtil;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Hashtable;

import java.util.List;


/**
 * Class description
 *
 *
 * @version        $version$, Thu, Nov 25, '10
 * @author         Enter your name here...
 */
public class OboUtil {

    /** _more_ */
    public static final String TAG_FORMAT_VERSION = "format-version";

    /** _more_ */
    public static final String TAG_DATA_VERSION = "data-version";

    /** _more_ */
    public static final String TAG_DATE = "date";

    /** _more_ */
    public static final String TAG_SUBSETDEF = "subsetdef";

    /** _more_ */
    public static final String TAG_SYNONYMTYPEDEF = "synonymtypedef";

    /** _more_ */
    public static final String TAG_DEFAULT_NAMESPACE = "default-namespace";

    /** _more_ */
    public static final String TAG_REMARK = "remark";

    /** _more_ */
    public static final String TAG_ID = "id";

    /** _more_ */
    public static final String TAG_NAME = "name";

    /** _more_ */
    public static final String TAG_NAMESPACE = "namespace";

    /** _more_ */
    public static final String TAG_DEF = "def";

    /** _more_ */
    public static final String TAG_SYNONYM = "synonym";

    /** _more_ */
    public static final String TAG_IS_A = "is_a";

    /** _more_ */
    public static final String TAG_ALT_ID = "alt_id";

    /** _more_ */
    public static final String TAG_SUBSET = "subset";

    /** _more_ */
    public static final String TAG_XREF = "xref";

    /** _more_ */
    public static final String TAG_COMMENT = "comment";

    /** _more_ */
    public static final String TAG_IS_OBSOLETE = "is_obsolete";

    /** _more_ */
    public static final String TAG_CONSIDER = "consider";

    /** _more_ */
    public static final String TAG_RELATIONSHIP = "relationship";

    /** _more_ */
    public static final String TAG_PROPERTY_VALUE = "property_value";

    /** _more_ */
    public static final String TAG_REPLACED_BY = "replaced_by";

    /** _more_ */
    public static final String TAG_DISJOINT_FROM = "disjoint_from";

    /** _more_ */
    public static final String TAG_IS_TRANSITIVE = "is_transitive";

    /** _more_ */
    public static final String TAG_TRANSITIVE_OVER = "transitive_over";


}
