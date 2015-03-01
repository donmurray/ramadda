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

import org.xml.sax.*;

import ucar.unidata.util.IOUtil;
import ucar.unidata.util.Misc;
import ucar.unidata.util.StringUtil;



import ucar.unidata.xml.*;



import java.io.ByteArrayInputStream;


import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;

import java.security.SignatureException;



import java.util.ArrayList;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.List;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;


import javax.xml.parsers.*;



/**
 * A collection of utilities for xml.
 *
 * @author IDV development team
 */

public abstract class XmlUtils {


    /**
     * _more_
     *
     * @param sb _more_
     * @param bytes _more_
     *
     * @throws Exception _more_
     */
    public static void appendCdataBytes(Appendable sb, byte[] bytes)
            throws Exception {
        sb.append("<![CDATA[");
        sb.append(XmlUtil.encodeBase64(bytes));
        sb.append("]]>");
    }

    /**
     * _more_
     *
     * @param sb _more_
     * @param s _more_
     *
     * @throws Exception _more_
     */
    public static void appendCdata(Appendable sb, String s) throws Exception {
        sb.append("<![CDATA[");
        sb.append(s);
        sb.append("]]>");
    }


}
