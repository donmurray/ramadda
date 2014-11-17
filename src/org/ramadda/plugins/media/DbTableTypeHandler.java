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

package org.ramadda.plugins.media;


import org.ramadda.repository.*;
import org.ramadda.repository.metadata.*;
import org.ramadda.repository.type.*;

import org.ramadda.sql.*;
import org.ramadda.util.HtmlUtils;

import org.ramadda.util.Json;
import org.ramadda.util.Utils;
import org.ramadda.util.XlsUtil;


import org.w3c.dom.*;

import ucar.unidata.util.IOUtil;

import ucar.unidata.util.Misc;
import ucar.unidata.util.StringUtil;


import ucar.unidata.xml.XmlUtil;

import java.io.File;
import java.io.InputStream;
import java.io.OutputStream;

import java.sql.*;

import java.util.ArrayList;

import java.util.Date;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.List;

import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;


/**
 *
 *
 */
public class DbTableTypeHandler extends TabularTypeHandler {

    /** _more_          */
    private static int IDX = TabularTypeHandler.IDX_LAST;

    /** _more_          */
    public static final int IDX_JDBC = IDX++;

    /** _more_          */
    public static final int IDX_USER = IDX++;

    /** _more_          */
    public static final int IDX_PASSWORD = IDX++;

    /** _more_          */
    public static final int IDX_TABLE = IDX++;

    /** _more_          */
    public static final int IDX_COLUMNS = IDX++;



    /**
     * _more_
     *
     * @param repository _more_
     * @param entryNode _more_
     *
     * @throws Exception _more_
     */
    public DbTableTypeHandler(Repository repository, Element entryNode)
            throws Exception {
        super(repository, entryNode);
    }

    /**
     * _more_
     *
     * @return _more_
     */
    @Override
    public boolean adminOnly() {
        return true;
    }



    /**
     * _more_
     *
     * @param request _more_
     * @param entry _more_
     * @param myxls _more_
     * @param sheets _more_
     * @param sheetsToShow _more_
     * @param skip _more_
     *
     * @throws Exception _more_
     */
    @Override
    public void read(Request request, Entry entry, InputStream myxls,
                     List<String> sheets, HashSet<Integer> sheetsToShow,
                     int skip)
            throws Exception {

        //        Object[]values = entry.getValues();
        //        for(int i=0;i<values.length;i++) {
        //            System.err.println (i +" =  "  + values[i]);
        //        }

        String table = entry.getValue(IDX_TABLE, (String) null);
        System.err.println("table:" + table + " idx:" + IDX_TABLE);


        if ( !Utils.stringDefined(table)) {
            System.err.println("db-read- no table");

            return;
        }

        List<String> cols = StringUtil.split(entry.getValue(IDX_COLUMNS, ""),
                                             "\n", true, true);
        String what = "*";

        if (cols.size() > 0) {
            what = StringUtil.join(",", cols);
        }

        int max = TabularOutputHandler.MAX_ROWS;
        Statement stmt = getDatabaseManager().select(what,
                             Misc.newList(table), null, "", max);

        SqlUtil.Iterator  iter = getDatabaseManager().getIterator(stmt);
        ResultSet         results;




        List<String>      rows = new ArrayList<String>();
        ResultSetMetaData rsmd = null;

        while ((results = iter.getNext()) != null) {
            if (rsmd == null) {
                rsmd = results.getMetaData();
                int          columnCount = rsmd.getColumnCount();
                List<String> names       = new ArrayList<String>();
                for (int i = 1; i < columnCount + 1; i++) {
                    String name = rsmd.getColumnName(i);
                    names.add(Json.quote(name));
                }
                rows.add(Json.list(names));
            }
            List<String> row = new ArrayList<String>();
            for (int col = 0; col < rsmd.getColumnCount(); col++) {
                String s = results.getString(col + 1);
                if (s == null) {
                    s = "null";
                } else {
                    s = Json.quote(s);
                }
                row.add(s);
            }
            rows.add(Json.list(row));
        }

        sheets.add(Json.map("name", Json.quote(table), "rows",
                            Json.list(rows)));



    }
}
