package org.ramadda.util;

import org.w3c.dom.*;

import org.xml.sax.*;



import ucar.unidata.xml.*;

import ucar.unidata.util.IOUtil;
import ucar.unidata.util.Misc;
import ucar.unidata.util.StringUtil;



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
     */
    public static void appendCdataBytes(Appendable sb, byte[] bytes) throws Exception {
        sb.append("<![CDATA[");
        sb.append(XmlUtil.encodeBase64(bytes));
        sb.append("]]>");
    }

    /**
     * _more_
     *
     * @param sb _more_
     * @param s _more_
     */
    public static void appendCdata(Appendable sb, String s) throws Exception {
        sb.append("<![CDATA[");
        sb.append(s);
        sb.append("]]>");
    }


}