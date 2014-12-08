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

package org.ramadda.util;


import org.w3c.dom.*;


import ucar.unidata.util.IOUtil;


import ucar.unidata.util.Misc;
import ucar.unidata.util.TwoFacedObject;
import ucar.unidata.xml.XmlUtil;

import java.text.StringCharacterIterator;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.List;


/**
 * JSON Utility class
 */
public class Json {

    /** JSON MIME type */
    public static final String MIMETYPE = "application/json";

    /** the null string identifier */
    public static final String NULL = "null";

    /** default quote value */
    public static final boolean DFLT_QUOTE = false;

    /** _more_ */
    public static final String FIELD_NAME = "name";

    /** _more_ */
    public static final String FIELD_FIELDS = "fields";

    /** _more_ */
    public static final String FIELD_DATA = "data";

    /** _more_ */
    public static final String FIELD_VALUES = "values";

    /** _more_ */
    public static final String FIELD_LATITUDE = "latitude";

    /** _more_ */
    public static final String FIELD_LONGITUDE = "longitude";

    /** _more_ */
    public static final String FIELD_ELEVATION = "elevation";

    /** _more_ */
    public static final String FIELD_DATE = "date";



    /**
     * _more_
     *
     * @param pw _more_
     * @param lat _more_
     * @param lon _more_
     * @param elevation _more_
     *
     * @throws Exception _more_
     */
    public static void addGeolocation(Appendable pw, double lat, double lon,
                                      double elevation)
            throws Exception {

        pw.append(attr(FIELD_LATITUDE, lat));
        pw.append(",\n");
        pw.append(attr(FIELD_LONGITUDE, lon));
        pw.append(",\n");
        pw.append(attr(FIELD_ELEVATION, elevation));
    }

    /**
     * Create a JSON map
     *
     * @param values  key/value pairs { key1,value1,key2,value2 }
     *
     * @return  the map object { key1:value1, key2:value2 }
     */
    public static String mapAndQuote(String... values) {
        return map(values, true);
    }


    /**
     * Create a JSON map
     *
     * @param values  key/value pairs { key1,value1,key2,value2 }
     *
     * @return  the map object { key1:value1, key2:value2 }
     */
    public static String map(String... values) {
        return map(values, DFLT_QUOTE);
    }

    /**
     * Create a JSON map
     *
     * @param values  key/value pairs [ key1,value1,key2,value2 ]
     *
     * @return  the map object { key1:value1, key2:value2 }
     */
    public static String map(List<String> values) {
        return map(values, DFLT_QUOTE);
    }

    /**
     * _more_
     *
     * @param values _more_
     *
     * @return _more_
     */
    public static String mapAndQuote(List<String> values) {
        return map(values, true);
    }

    /**
     * Create a JSON map
     *
     * @param values  key/value pairs { key1,value1,key2,value2 }
     * @param quoteValue  true to quote the values
     *
     * @return  the map object { key1:value1, key2:value2 }
     */
    public static String map(String[] values, boolean quoteValue) {
        return map((List<String>) Misc.toList(values), quoteValue);
    }

    /**
     * Create a JSON map
     *
     * @param values  key/value pairs [ key1,value1,key2,value2 ]
     * @param quoteValue  true to quote the values
     *
     * @return  the map object { key1:value1, key2:value2 }
     */
    public static String map(List<String> values, boolean quoteValue) {
        StringBuffer row = new StringBuffer();
        row.append(mapOpen());
        for (int i = 0; i < values.size(); i += 2) {
            if (i > 0) {
                row.append(",\n");
            }
            String name  = values.get(i);
            String value = values.get(i + 1);
            row.append(attr(name, value, quoteValue));
        }
        row.append(mapClose());

        return row.toString();
    }

    /**
     * Create a JSON list from the array of strings
     *
     * @param values  list of values { value1,value2,value3,value4 }
     *
     * @return  the values as a JSON array [ value1,value2,value3,value4 ]
     */
    public static String list(String[] values) {
        return list(Misc.toList(values));
    }

    /**
     * Create a JSON list from the array of strings
     *
     * @param values  list of values [ value1,value2,value3,value4 ]
     *
     * @return  the values as a JSON array [ value1,value2,value3,value4 ]
     */
    public static String list(List values) {
        return list(values, DFLT_QUOTE);
    }

    /**
     * _more_
     *
     * @param key _more_
     *
     * @return _more_
     */
    public static String mapKey(String key) {
        return "\"" + key + "\":";
    }

    /**
     * _more_
     *
     * @return _more_
     */
    public static String mapOpen() {
        return "{";
    }

    /**
     * _more_
     *
     * @return _more_
     */
    public static String mapClose() {
        return "}";
    }

    /**
     * _more_
     *
     * @return _more_
     */
    public static String listOpen() {
        return "[";
    }

    /**
     * _more_
     *
     * @return _more_
     */
    public static String listClose() {
        return "]";
    }

    /**
     * Create a JSON list from the array of strings
     *
     * @param values  list of values [ value1,value2,value3,value4 ]
     * @param quoteValue  true to quote the values
     *
     * @return  the values as a JSON array [ value1,value2,value3,value4 ]
     */
    public static String list(List values, boolean quoteValue) {
        StringBuffer row = new StringBuffer();
        row.append(listOpen());
        for (int i = 0; i < values.size(); i++) {
            if (i > 0) {
                row.append(",\n");
            }
            if (quoteValue) {
                row.append(quote(values.get(i).toString()));
            } else {
                row.append(values.get(i).toString());
            }
        }
        row.append(listClose());

        return row.toString();
    }



    /**
     * Create a list of JSON object from a list of TwoFacedObjects
     *
     * @param values  the values
     *
     * @return  the list [ {id:id1,label:label1},{id:id2,label:label2} ]
     */
    public static String tfoList(List<TwoFacedObject> values) {
        return tfoList(values, "id", "label");
    }

    /**
     * Create a list of JSON object from a list of TwoFacedObjects
     *
     * @param values  the values
     * @param idKey   the key for the TwoFacedObject ID
     * @param labelKey   the key for the TwoFacedObject label
     *
     * @return  the list [ {id:id1,label:label1},{id:id2,label:label2} ]
     */
    public static String tfoList(List<TwoFacedObject> values, String idKey,
                                 String labelKey) {
        List<String> arrayVals = new ArrayList<String>();
        for (TwoFacedObject tfo : values) {
            List<String> mapValues = new ArrayList<String>();
            String       id        = TwoFacedObject.getIdString(tfo);
            String       label     = tfo.toString();
            mapValues.add(idKey);
            mapValues.add((id == null)
                          ? label
                          : id);
            mapValues.add(labelKey);
            mapValues.add(label);
            arrayVals.add(map(mapValues, true));
        }

        return list(arrayVals);
    }

    /**
     * Get a string
     *
     * @param s  the string
     * @param quote  true to quote
     *
     * @return the string
     */
    public static String getString(String s, boolean quote) {
        if (s == null) {
            return NULL;
        }
        if (quote) {
            return quote(s);
        }

        return s;
    }



    /**
     * _more_
     *
     * @param name _more_
     * @param value _more_
     *
     * @return _more_
     */
    public static String attr(String name, double value) {
        return attr(name, formatNumber(value), false);
    }

    /**
     * _more_
     *
     * @param name _more_
     * @param value _more_
     *
     * @return _more_
     */
    public static String attr(String name, long value) {
        return attr(name, "" + value, false);
    }


    /**
     * Create a JSON object attribute
     *
     * @param name  the attribute name
     * @param value  the attribute value
     *
     * @return  the attribute as name:value
     */
    public static String attr(String name, String value) {
        return attr(name, value, DFLT_QUOTE);
    }

    /**
     * Create a JSON object attribute
     *
     * @param name  the attribute name
     * @param value  the attribute value
     * @param quoteValue true to quote the name and value
     *
     * @return  the attribute as name:value
     */
    public static String attr(String name, String value, boolean quoteValue) {
        return mapKey(name) + getString(value, quoteValue);
    }

    /**
     * quote the attribute value and add it to the list
     *
     * @param items the list of items
     * @param name  the attribute name
     * @param value the attribute value
     */
    public static void quoteAttr(List<String> items, String name,
                                 String value) {
        items.add(name);
        items.add(getString(value, true));
    }

    /**
     * Make an attribute and add it to the list
     *
     * @param items  the list of name/value pairs
     * @param name   the attribute name
     * @param value  the attribute value
     */
    public static void attr(List<String> items, String name, String value) {
        items.add(name);
        items.add(getString(value, false));
    }


    /**
     * _more_
     *
     * @param d _more_
     *
     * @return _more_
     */
    public static String formatNumber(double d) {
        if (Double.isNaN(d)) {
            return "null";
        }

        if ((d == Double.NEGATIVE_INFINITY)
                || (d == Double.POSITIVE_INFINITY)) {
            return "null";

        }

        return "" + d;
    }


    /**
     * Quote a string
     *
     * @param s the string
     *
     * @return  the quoted string
     */
    public static String quote(String s) {
        try {
            if (s == null) {
                return NULL;
            }
            s = cleanString(s);
            s = s.replaceAll("\"", "\\\\\"");
            if (s.equals("true") || s.equals("false")) {
                return s;
            }
            //This can mess up and match on what should be a string value, e.g.
            //00000000000
            //Not sure what to do here
            //            if(s.matches("^[0-9]+\\.?[0-9]*$")) return s;

            return "\"" + s + "\"";
        } catch (Exception exc) {
            throw new IllegalArgumentException("Could not quote string:" + s);
        }
    }


    /**
     * Clean a string of illegal JSON characters
     *
     * @param aText  the string
     *
     * @return  the cleaned string
     */
    public static String cleanString(String aText) {
        if ( !Utils.stringDefined(aText)) {
            return "";
        }
        final StringBuilder     result      = new StringBuilder();
        StringCharacterIterator iterator = new StringCharacterIterator(aText);
        char                    character   = iterator.current();
        char                    char_slash  = '\\';
        char                    char_dquote = '"';

        while (character != StringCharacterIterator.DONE) {
            if (character == char_dquote) {
                //For now don't escape double quotes
                result.append(character);
                //                result.append(char_slash);
                //                result.append(char_dquote);
            } else if (character == char_slash) {
                result.append(char_slash);
                result.append(char_slash);
            } else if (character == '\b') {
                result.append("\\b");
            } else if (character == '\f') {
                result.append("\\f");
            } else if (character == '\n') {
                result.append("\\n");
            } else if (character == '\r') {
                result.append("\\r");
            } else if (character == '\t') {
                result.append("\\t");
            } else {
                //the char is not a special one
                //add it to the result as is
                result.append(character);
            }
            character = iterator.next();
        }

        String s = result.toString();

        //Make into all ascii ??
        s = s.replaceAll("[^\n\\x20-\\x7E]+", " ");

        return s;
    }



    /**
     * _more_
     *
     * @param args _more_
     *
     * @throws Exception _more_
     */
    public static void main(String[] args) throws Exception {
        Element root = XmlUtil.getRoot(IOUtil.readContents(args[0], ""));
        System.out.println(xmlToJson(root));
    }

    /**
     * _more_
     *
     * @param node _more_
     *
     * @return _more_
     *
     * @throws Exception _more_
     */
    public static String xmlToJson(Element node) throws Exception {
        StringBuilder json = new StringBuilder();
        xmlToJson(node, json);
        return json.toString();
    }

    /*

     */

    /**
     * _more_
     *
     * @param node _more_
     * @param sb _more_
     *
     * @throws Exception _more_
     */
    private static void xmlToJson(Element node, Appendable sb)
            throws Exception {
        List<String> attrs = new ArrayList<String>();
        attrs.add("xml_tag");
        attrs.add(quote(node.getTagName()));
        NamedNodeMap nnm = node.getAttributes();
        if (nnm != null) {
            for (int i = 0; i < nnm.getLength(); i++) {
                Attr attr = (Attr) nnm.item(i);
                attrs.add(attr.getNodeName());
                attrs.add(quote(attr.getNodeValue()));
            }
        }
        String text = XmlUtil.getChildText(node);
        if (Utils.stringDefined(text)) {
            text = text.replaceAll("\"", "\\\"");
            attrs.add("xml_text");
            attrs.add(quote(text));
        }

        List<String> childJson = new ArrayList<String>();



        NodeList     children  = node.getChildNodes();



        for (int i = 0; i < children.getLength(); i++) {
            Node child = children.item(i);
            if ( !(child instanceof Element)) {
                continue;
            }
            String tag = ((Element)child).getTagName();

            StringBuilder csb = new StringBuilder();
            xmlToJson((Element) child, csb);
            childJson.add(csb.toString());
        }

        if (childJson.size() > 0) {
            attrs.add("children");
            attrs.add(list(childJson));
        }

        sb.append(map(attrs));

    }


}
