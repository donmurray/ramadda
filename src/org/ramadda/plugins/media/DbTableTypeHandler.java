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
import org.ramadda.repository.database.*;
import org.ramadda.repository.type.*;

import org.ramadda.sql.*;
import org.ramadda.util.FormInfo;
import org.ramadda.util.HtmlUtils;
import org.ramadda.util.Json;
import org.ramadda.util.Utils;

import org.w3c.dom.*;

import ucar.unidata.util.IOUtil;
import ucar.unidata.util.Misc;
import ucar.unidata.util.StringUtil;

import java.io.File;
import java.io.InputStream;
import java.io.OutputStream;

import java.sql.*;

import java.util.ArrayList;

import java.util.Date;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.List;
import java.util.regex.*;



/**
 *
 *
 */
public class DbTableTypeHandler extends TabularTypeHandler {

    /** _more_ */
    private static int IDX = TabularTypeHandler.IDX_LAST;

    /** _more_ */
    public static final int IDX_DBID = IDX++;

    /** _more_ */
    public static final int IDX_TABLE = IDX++;

    /** _more_ */
    public static final int IDX_PROPERTIES = IDX++;


    /** _more_ */
    private Hashtable<String, List<TableInfo>> dbToTables =
        new Hashtable<String, List<TableInfo>>();

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
       column: Label, searchable
     **/


    /**
     * _more_
     *
     * @param request _more_
     * @param entry _more_
     * @param myxls _more_
     * @param visitInfo _more_
     * @param visitor _more_
     *
     * @throws Exception _more_
     */
    @Override
    public void visit(Request request, Entry entry, InputStream myxls,
                      TabularVisitInfo visitInfo, TabularVisitor visitor)
            throws Exception {


        TableInfo tableInfo = getTableInfo(entry);
        if(tableInfo==null) {
            System.err.println ("Error: Could not find TableInfo for entry:" + entry);
            return;
        }
        List columns = new ArrayList();
        for (ColumnInfo col : tableInfo.getColumns()) {
            columns.add(Json.map("name",
                                 Json.quote(col.getName()),
                                 "type",
                                 Json.quote(col.getTypeName())));
        }
        visitInfo.addTableProperty("columns", Json.list(columns));


        String dbid = entry.getValue(IDX_DBID, (String) null);
        if ( !Utils.stringDefined(dbid)) {
            System.err.println("DbTableTypeHandler.visit: no dbid defined");

            return;
        }

        Connection connection =
            getDatabaseManager().getExternalConnection("table.db." + dbid);
        if (connection == null) {
            System.err.println("DbTableTypeHandler.visit: no connection");

            return;
        }

        String table = entry.getValue(IDX_TABLE, (String) null);
        //        System.err.println("table:" + table + " idx:" + IDX_TABLE);

        if ( !Utils.stringDefined(table)) {
            System.err.println("DbTableTypeHadler.visit: no table defined");

            return;
        }

        


        String what = "*";



        /*
        List<String> cols = StringUtil.split(entry.getValue(IDX_COLUMNS, ""),
                                             "\n", true, true);

        if (cols.size() > 0) {
            what = StringUtil.join(",", cols);
            }*/

        int          max        = TabularOutputHandler.MAX_ROWS;

        List<Clause> andClauses = new ArrayList<Clause>();
        List<Clause> orClauses  = new ArrayList<Clause>();
        String       s          = visitInfo.getSearchText();
        String opp = "(<>|=|<|>|<=|>=)";

        if (Utils.stringDefined(s)) {
            if (tableInfo != null) {
                for (ColumnInfo col : tableInfo.getColumns()) {
                    //Don't loop forever
                    int cnt = 5;
                    while (cnt-- > 0) {
                        if (s.length() == 0) {
                            break;
                        }
                        String pattern = col.getName().toLowerCase() + "\\s*"
                                         + opp + "\\s*([^\\s]+)";
                        String[] toks = Utils.findPatterns(s, pattern);
                        if (toks == null) {
                            pattern = col.getName().toLowerCase() + "\\s*"
                                      + opp + "\\s*\"(.*)\"";
                            toks = Utils.findPatterns(s, pattern);
                        }
                        if (toks == null) {
                            break;
                        }
                        if (toks != null) {
                            String op = toks[0].trim();
                            String v  = toks[1];
                            s = s.replaceFirst(pattern, "").trim();
                            v = v.trim();
                            Clause clause = new Clause(col.getName(), op, v);
                            andClauses.add(clause);
                        }
                    }
                }


                if (Utils.stringDefined(s)) {
                    String sqlString = SqlUtil.wildCardBoth(s);
                    for (ColumnInfo col : tableInfo.getColumns()) {
                        if (col.getType() == col.TYPE_VARCHAR) {
                            orClauses.add(Clause.like(col.getName(),
                                    sqlString));
                        }
                    }
                }
            }
        }


        if (tableInfo != null) {
            for (ColumnInfo col : tableInfo.getColumns()) {
                String v = request.getString("table." + col.getName().toLowerCase(),(String)null);
                if(!Utils.stringDefined(v)) continue;
                List<String> toks = StringUtil.split(v," ",true,true);
                String lastOp = null;
                String pattern = "^" + opp +"(.+)";
                for(int i=0;i<toks.size();i++) {
                    String tok = toks.get(i);
                    String[] toks2 = Utils.findPatterns(tok, pattern);
                    if(toks2==null) {
                        String isOp = StringUtil.findPattern(tok, opp);
                        if(isOp!=null) {
                            lastOp = tok;
                            continue;
                        }
                        if(lastOp == null) lastOp = Clause.EXPR_EQUALS;
                        andClauses.add(new Clause(col.getName(), lastOp, tok));
                        lastOp = null;
                        continue;
                    }
                    Clause clause = new Clause(col.getName(), toks2[0].trim(), toks2[1].trim());
                    andClauses.add(clause);

                }
            }
        }



        if (orClauses.size() > 0) {
            andClauses.add(Clause.or(orClauses));
        }
        SqlUtil.debug = true;
        Statement stmt = SqlUtil.select(connection, what,
                                        Misc.newList(table),
                                        Clause.and(andClauses), "", max, 0);

        SqlUtil.debug = false;
        SqlUtil.Iterator   iter = new SqlUtil.Iterator(stmt);
        ResultSet          results;
        ResultSetMetaData  rsmd = null;

        List<List<Object>> rows = new ArrayList<List<Object>>();
        while ((results = iter.getNext()) != null) {
            if (rsmd == null) {
                rsmd = results.getMetaData();
                int          columnCount = rsmd.getColumnCount();
                List<Object> names       = new ArrayList<Object>();
                for (int i = 1; i < columnCount + 1; i++) {
                    String name     = rsmd.getColumnName(i);
                    String typeName = rsmd.getColumnTypeName(i);
                    //                    names.add(name + " - " + typeName);
                    names.add(name);
                }
                rows.add(names);
            }
            List<Object> row = new ArrayList<Object>();
            for (int col = 0; col < rsmd.getColumnCount(); col++) {
                int    colIdx = col + 1;
                int    type   = rsmd.getColumnType(colIdx);
                Object value  = null;
                //TODO: Handle more dates
                if (type == java.sql.Types.DOUBLE) {
                    value = new Double(results.getDouble(colIdx));
                } else if (type == java.sql.Types.FLOAT) {
                    value = new Float(results.getFloat(colIdx));
                } else if (type == java.sql.Types.INTEGER) {
                    value = new Integer(results.getInt(colIdx));
                } else {
                    value = results.getString(colIdx);
                }
                row.add(value);
            }
            if ((andClauses.size() == 0) && !visitInfo.rowOk(row)) {
                //                System.err.println ("skipping row:" + row);
                continue;
            }
            //            System.err.println ("adding row:" + row);
            rows.add(row);
        }
        visitor.visit(visitInfo, table, rows);
        connection.close();
    }


    /**
     * _more_
     *
     * @param request _more_
     * @param entry _more_
     *
     * @return _more_
     */
    @Override
    public boolean okToShowTable(Request request, Entry entry) {
        if ( !Utils.stringDefined(entry.getValue(IDX_DBID, (String) null))) {
            return false;
        }
        if ( !Utils.stringDefined(entry.getValue(IDX_TABLE, (String) null))) {
            return false;
        }

        return super.okToShowTable(request, entry);
    }

    /**
     * _more_
     *
     * @param request _more_
     * @param entry _more_
     * @param column _more_
     * @param formBuffer _more_
     * @param values _more_
     * @param state _more_
     * @param formInfo _more_
     *
     * @throws Exception _more_
     */
    @Override
    public void addColumnToEntryForm(Request request, Entry entry,
                                     Column column, Appendable formBuffer,
                                     Object[] values, Hashtable state,
                                     FormInfo formInfo)
            throws Exception {

        if (column.getName().equals("db_id")) {
            List<String> dbs = StringUtil.split(
                                   getRepository().getProperty(
                                       "table.db.databases", ""), ",", true,
                                           true);
            if (dbs.size() > 0) {
                String dbid = (entry==null?null:entry.getValue(IDX_DBID, (String) null));
                formBuffer.append(
                    formEntry(
                        request, column.getLabel() + ":",
                        HtmlUtils.select(column.getEditArg(), dbs, dbid)));

                return;
            }
        }

        if (column.getName().equals("table_name")) {
            List<String> tables = getTableNames(entry);
            if ((tables != null) && (tables.size() > 0)) {
                tables.add(0, "");
                String name = entry==null?null:entry.getValue(IDX_TABLE, (String) null);
                formBuffer.append(
                    formEntry(
                        request, column.getLabel() + ":",
                        HtmlUtils.select(column.getEditArg(), tables, name)));

                return;
            }
        }
        super.addColumnToEntryForm(request, entry, column, formBuffer,
                                   values, state, formInfo);
    }


    /**
     * _more_
     *
     * @param request _more_
     * @param entry _more_
     * @param column _more_
     * @param widget _more_
     *
     * @return _more_
     *
     * @throws Exception _more_
     */
    public String xxxgetFormWidget(Request request, Entry entry,
                                   Column column, String widget)
            throws Exception {
        return super.getFormWidget(request, entry, column, widget);
    }



    /**
     * _more_
     *
     * @param entry _more_
     *
     * @return _more_
     *
     * @throws Exception _more_
     */
    private List<String> getTableNames(Entry entry) throws Exception {
        if(entry  == null) return null;
        List<TableInfo> tableInfos = getTableInfos(entry);
        if (tableInfos == null) {
            return null;
        }

        return TableInfo.getTableNames(tableInfos);
    }

    /**
     * _more_
     *
     * @param entry _more_
     *
     * @return _more_
     *
     * @throws Exception _more_
     */
    public TableInfo getTableInfo(Entry entry) throws Exception {
        List<TableInfo> tableInfos = getTableInfos(entry);
        if (tableInfos == null) {
            return null;
        }
        String table = entry.getValue(IDX_TABLE, (String) null);
        if ( !Utils.stringDefined(table)) {
            return null;
        }
        table = table.toLowerCase();
        for (TableInfo tableInfo : tableInfos) {
            if (tableInfo.getName().toLowerCase().equals(table)) {
                return tableInfo;
            }
        }

        return null;
    }



    /**
     * _more_
     *
     * @param entry _more_
     *
     * @return _more_
     *
     * @throws Exception _more_
     */
    private List<TableInfo> getTableInfos(Entry entry) throws Exception {
        String dbid = entry.getValue(IDX_DBID, (String) null);
        if ( !Utils.stringDefined(dbid)) {
            return null;
        }

        List<TableInfo> tableInfos = dbToTables.get(dbid);
        if (tableInfos == null) {
            Connection connection = null;
            try {
                connection =
                    getDatabaseManager().getExternalConnection("table.db."
                        + dbid);
                if (connection == null) {
                    System.err.println(
                        "DbTableTypeHadler.visit: no connection");

                    return null;
                }
                tableInfos = getDatabaseManager().getTableInfos(connection,
                        true);
            } finally {
                if (connection != null) {
                    connection.close();
                }
            }
            dbToTables.put(dbid, tableInfos);
        }

        return tableInfos;
    }

    public static class TableProperty {
        private String type;
        private String otherTable;
        private String name;
        private String label;
        private boolean canSearch;

    }

}
