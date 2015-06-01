/*
 * Copyright (c) 2008-2015 Geode Systems LLC
 * This Software is licensed under the Geode Systems RAMADDA License available in the source distribution in the file 
 * ramadda_license.txt. The above copyright notice shall be included in all copies or substantial portions of the Software.
 */

package org.ramadda.plugins.socrata;


import org.json.*;

import org.ramadda.data.point.*;
import org.ramadda.data.point.text.*;
import org.ramadda.data.record.*;
import org.ramadda.repository.Entry;


import org.ramadda.repository.RepositoryUtil;
import org.ramadda.util.Json;
import org.ramadda.util.Utils;

import org.w3c.dom.Element;

import ucar.unidata.util.IOUtil;

import ucar.unidata.xml.XmlUtil;

import java.io.*;

import java.text.SimpleDateFormat;

import java.util.ArrayList;

import java.util.Date;
import java.util.List;

import java.util.TimeZone;


/**
 */
public class SocrataFile extends CsvFile {

    /** _more_ */
    private StringBuilder buffer;


    /**
     * ctor
     *
     * @param filename _more_
     *
     * @throws IOException _more_
     */
    public SocrataFile(String filename) throws IOException {
        super(filename);
    }


    /**
     * _more_
     *
     * @param buffered _more_
     *
     * @return _more_
     *
     * @throws IOException _more_
     */
    @Override
    public InputStream doMakeInputStream(boolean buffered)
            throws IOException {

        try {
            if (buffer == null) {
                //System.err.println("Reading SOCRATA time series");
                buffer = new StringBuilder();

                InputStream source = super.doMakeInputStream(buffered);
                int         MAX    = 2 * 1000000;
                String      json   = new String(Utils.readBytes(source, MAX));
                if (json.length() >= MAX) {
                    throw new IllegalArgumentException(
                        "Too big reading from Socrata");
                }
                //                System.out.println (json);
                JSONObject   obj    = new JSONObject(new JSONTokener(json));
                JSONObject   view   = Json.readObject(obj, "meta.view");
                JSONArray    cols   = view.getJSONArray("columns");
                JSONArray    data   = obj.getJSONArray("data");

                List<String> types  = new ArrayList<String>();
                List<String> fields = new ArrayList<String>();
                for (int i = 0; i < cols.length(); i++) {
                    JSONObject col  = cols.getJSONObject(i);
                    String     name = col.get("name").toString();
                    String     id   = col.get("fieldName").toString();
                    String     type = col.get("dataTypeName").toString();
                    types.add(type);
                    if (type.equals("meta_data")) {
                        continue;
                    }

                    StringBuilder sb = new StringBuilder();
                    if (type.equals("location")) {
                        sb.append(
                            "latitude[label=Latitude],longitude[label=Longitude]");
                    } else {
                        sb.append(id);
                        sb.append("[");
                        sb.append(attrLabel(name));
                        if (type.equals("text")) {
                            sb.append(attrType(TYPE_STRING));
                        } else if (type.equals("number")) {
                            sb.append(attrChartable());
                        } else if (type.equals("percent")) {
                            sb.append(attrChartable());
                            sb.append(attrUnit("%"));
                        } else if (type.equals("money")) {
                            sb.append(attrChartable());
                        } else if (type.equals("calendar_date")) {
                            sb.append(attrType(TYPE_DATE));
                            sb.append(attrFormat("yyyy-MM-dd'T'HH:mm:ss"));
                        } else {
                            sb.append(attrType(TYPE_STRING));
                        }
                        sb.append("]");
                    }
                    fields.add(sb.toString());
                }
                System.err.println(makeFields(fields));
                putProperty(PROP_FIELDS, makeFields(fields));
                putProperty("output.latlon", "false");
                //                putProperty("output.time","false");

                for (int i = 0; i < data.length(); i++) {
                    JSONArray row    = data.getJSONArray(i);
                    int       colCnt = 0;
                    for (int j = 0; j < row.length(); j++) {
                        String type = types.get(j);
                        if (type.equals("meta_data")) {
                            continue;
                        }
                        String v = null;
                        if (type.equals("location")) {
                            JSONArray tuple = row.getJSONArray(j);

                            v = tuple.get(1).toString() + ","
                                + tuple.get(2).toString();
                            if (colCnt > 0) {
                                buffer.append(",");
                            }
                            buffer.append(v);
                            System.err.println("location:" + v);
                            colCnt += 2;

                        } else {
                            v = row.get(j).toString();
                            if (v != null) {
                                v = v.replaceAll("\n", " ");
                                if (colCnt > 0) {
                                    buffer.append(",");
                                }
                                boolean wrap = v.indexOf(",") >= 0;
                                if (wrap) {
                                    buffer.append("\"");
                                }
                                buffer.append(v);
                                if (wrap) {
                                    buffer.append("\"");
                                }
                                colCnt++;
                            }
                        }
                    }
                    buffer.append("\n");
                }
            }

            ByteArrayInputStream bais =
                new ByteArrayInputStream(buffer.toString().getBytes());

            return bais;
        } catch (Exception exc) {
            throw new RuntimeException(exc);
        }

    }


    /**
     * _more_
     *
     * @param args _more_
     *
     * @throws Exception _more_
     */
    public static void main(String[] args) throws Exception {
        PointFile.test(args, SocrataFile.class);
    }

}
