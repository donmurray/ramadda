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


import org.apache.poi.hssf.usermodel.HSSFWorkbook;

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;

import org.apache.poi.xssf.streaming.SXSSFWorkbook;
import org.apache.poi.xssf.usermodel.*;

import org.ramadda.data.process.*;


import org.ramadda.repository.*;
import org.ramadda.repository.auth.*;
import org.ramadda.repository.output.*;
import org.ramadda.util.GoogleChart;
import org.ramadda.util.HtmlUtils;
import org.ramadda.util.Json;
import org.ramadda.util.Utils;

import org.ramadda.util.XlsUtil;
import org.ramadda.util.text.CsvUtil;
import org.ramadda.util.text.Filter;
import org.ramadda.util.text.ProcessInfo;
import org.ramadda.util.text.Processor;
import org.ramadda.util.text.SearchField;


import org.w3c.dom.*;

import ucar.unidata.util.DateUtil;
import ucar.unidata.util.IOUtil;
import ucar.unidata.util.Misc;
import ucar.unidata.util.StringUtil;

import java.io.*;
import java.io.File;

import java.net.*;


import java.util.ArrayList;
import java.util.Date;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.List;



/**
 */
public class TabularOutputHandler extends OutputHandler {


    /** _more_ */
    public static final int MAX_ROWS = 500;

    /** _more_ */
    public static final int MAX_COLS = 100;

    /** _more_ */
    public static final OutputType OUTPUT_XLS_JSON =
        new OutputType("XLS to JSON", "xls_json", OutputType.TYPE_FEEDS, "",
                       "/media/xls.png");

    /** _more_ */
    public static final OutputType OUTPUT_XLS_HTML =
        new OutputType("Show Spreadsheet", "xls_html", OutputType.TYPE_VIEW,
                       "", "/media/xls.png");


    /**
     * _more_
     */
    public TabularOutputHandler() {}

    /**
     * _more_
     *
     *
     * @param repository _more_
     * @param element _more_
     * @throws Exception _more_
     */
    public TabularOutputHandler(Repository repository, Element element)
            throws Exception {
        super(repository, element);
        addType(OUTPUT_XLS_HTML);
        addType(OUTPUT_XLS_JSON);
    }





    /**
     * _more_
     *
     * @param request _more_
     * @param state _more_
     * @param links _more_
     *
     *
     * @throws Exception _more_
     */
    public void getEntryLinks(Request request, State state, List<Link> links)
            throws Exception {
        Entry entry = state.getEntry();
        if (entry == null) {
            return;
        }
        boolean isTabular = isTabular(entry);
        if ( !isTabular) {
            if ( !entry.isFile()) {
                return;
            }
            String path = entry.getResource().getPath();
            if (path.endsWith(".xls") || path.endsWith(".xlsx")
                    || path.endsWith(".csv")) {
                isTabular = true;
            }
        }

        if (isTabular) {
            links.add(makeLink(request, state.getEntry(), OUTPUT_XLS_HTML));
            links.add(makeLink(request, state.getEntry(), OUTPUT_XLS_JSON));
        }
    }


    /**
     * _more_
     *
     * @param request _more_
     * @param outputType _more_
     * @param entry _more_
     *
     * @return _more_
     *
     * @throws Exception _more_
     */
    @Override
    public Result outputEntry(Request request, OutputType outputType,
                              Entry entry)
            throws Exception {
        if (outputType.equals(OUTPUT_XLS_JSON)) {
            try {
                return outputEntryJson(request, outputType, entry);
            } catch (org.apache.poi.hssf.OldExcelFormatException exc) {
                StringBuilder sb = new StringBuilder();
                sb.append(
                    Json.map(
                        "error",
                        Json.quote("Old Excel format not supported")));
                request.setReturnFilename(entry.getName() + ".json");
                Result result = new Result("", sb);
                result.setShouldDecorate(false);
                result.setMimeType("application/json");

                return result;
            }
        }

        return new Result("",
                          new StringBuffer(getHtmlDisplay(request,
                              new Hashtable(), entry)));
    }




    /**
     * _more_
     *
     * @param s _more_
     *
     * @return _more_
     */

    public HashSet<Integer> getSheetsToShow(String s) {
        HashSet<Integer> sheetsToShow = null;
        if (Utils.stringDefined(s)) {
            List<String> sheetsStr = StringUtil.split(s, ",", true, true);
            if (sheetsStr.size() > 0) {
                sheetsToShow = new HashSet<Integer>();
                for (String tok : sheetsStr) {
                    sheetsToShow.add(Integer.parseInt(tok));
                }
            }
        }

        return sheetsToShow;
    }


    /**
     * _more_
     *
     * @param request _more_
     * @param outputType _more_
     * @param entry _more_
     *
     * @return _more_
     *
     * @throws Exception _more_
     */
    private Result outputEntryJson(Request request, OutputType outputType,
                                   Entry entry)
            throws Exception {
        StringBuilder sb   = new StringBuilder();

        String        file = null;
        if (entry.isFile()) {
            file = entry.getFile().toString();
        }


        HashSet<Integer> sheetsToShow = null;
        if (isTabular(entry)) {
            sheetsToShow =
                getSheetsToShow(entry.getValue(XlsTypeHandler.IDX_SHEETS,
                    ""));
        }

        final List<String> sheets  = new ArrayList<String>();
        TabularVisitor     visitor = new TabularVisitor() {
            @Override
            public boolean visit(TabularVisitInfo info, String sheet,
                                 List<List<Object>> rows) {
                List<String> jrows = new ArrayList<String>();
                for (List<Object> cols : rows) {
                    List<String> quoted = new ArrayList<String>();
                    for (Object col : cols) {
                        if (col == null) {
                            col = "null";
                        }
                        String s = col.toString();
                        s = s.replaceAll("\"", "&quot;");
                        quoted.add(Json.quote(s));
                    }
                    jrows.add(Json.list(quoted));
                }
                sheets.add(Json.map("name", Json.quote(sheet), "rows",
                                    Json.list(jrows)));

                return true;
            }
        };

        List props = new ArrayList();

        ProcessInfo info =  new ProcessInfo();
        info.setSkip(getSkipRows(request, entry));
        info.setMaxRows(getRowCount(request, entry,MAX_ROWS));
        //        TabularVisitInfo visitInfo = new TabularVisitInfo(request, entry, sheetsToShow);
        visit(request, entry, info, visitor);
        props.addAll(info.getTableProperties());
        props.add("sheets");
        props.add(Json.list(sheets));
        sb.append(Json.map(props));

        request.setReturnFilename(entry.getName() + ".json");
        Result result = new Result("", sb);
        result.setShouldDecorate(false);
        result.setMimeType("application/json");

        return result;
    }


    /**
     * _more_
     *
     * @param request _more_
     * @param entry _more_
     *
     * @return _more_
     *
     * @throws Exception _more_
     */
    private int getSkipRows(Request request, Entry entry) throws Exception {
        int dflt = 0;
        if (isTabular(entry)) {
            dflt = (int) entry.getValue(XlsTypeHandler.IDX_SKIPROWS, dflt);
        }

        return (int) request.get("table.skiprows", dflt);
    }

    /**
     * _more_
     *
     * @param request _more_
     * @param entry _more_
     * @param dflt _more_
     *
     * @return _more_
     *
     * @throws Exception _more_
     */
    private int getRowCount(Request request, Entry entry, int dflt)
            throws Exception {
        int v = (int) request.get("table.rows", dflt);

        return v;
    }

    /**
     * _more_
     *
     * @param request _more_
     * @param entry _more_
     * @param file _more_
     * @param visitInfo _more_
     * @param visitor _more_
     *
     * @throws Exception _more_
     */
    public void visit(Request request, Entry entry,
                      ProcessInfo visitInfo, TabularVisitor visitor)
            throws Exception {

        File file = entry.getFile();
        //        System.err.println("visit:" + visitInfo);
        InputStream inputStream = null;
        String      suffix      = "";

        if ((file != null) && file.exists()) {
            inputStream = new BufferedInputStream(
                getStorageManager().getFileInputStream(file));
            suffix = IOUtil.getFileExtension(file.toString()).toLowerCase();
            if (suffix.equals(".xlsx") || suffix.equals(".xls")) {
                if (file.length() > 10 * 1000000) {
                    throw new IllegalArgumentException("File too big");
                }
            }

        }

        if (suffix.equals(".xlsx") || suffix.equals(".xls")) {
            //            visitXls(request, entry, suffix, inputStream, visitInfo, visitor);
        } else if (suffix.endsWith(".csv")) {
            visitCsv(request, entry, inputStream, visitInfo, visitor);
        } else {
            if (isTabular(entry)) {
                TabularTypeHandler tth =
                    (TabularTypeHandler) entry.getTypeHandler();
                //                tth.visit(request, entry, inputStream, visitInfo, visitor);
            } else {
                throw new IllegalStateException("Unknown file type:"
                        + suffix);
            }
        }
        IOUtil.close(inputStream);
    }



    /**
     * _more_
     *
     * @param request _more_
     * @param entry _more_
     * @param inputStream _more_
     * @param visitInfo _more_
     * @param visitor _more_
     *
     * @throws Exception _more_
     */
    public void visitCsv(Request request, Entry entry,
                         InputStream inputStream,
                         final ProcessInfo  info,
                         TabularVisitor visitor)
            throws Exception {
        BufferedReader br =
            new BufferedReader(new InputStreamReader(inputStream));
        final List<List<Object>> rows   = new ArrayList<List<Object>>();

        ByteArrayOutputStream    bos    = new ByteArrayOutputStream();
        
        info.setInput(new BufferedInputStream(inputStream));
        info.setOutput(bos);
        info.getProcessor().addProcessor(new Processor() {
                @Override
                    public boolean processRow(ProcessInfo info, org.ramadda.util.text.Row row, String line) {
                    List obj = new ArrayList();
                    obj.addAll(row.getValues());
                    rows.add((List<Object>) obj);
                    return true;
                }
            });

        if (info.getSearchFields() != null) {
            for (SearchField searchField :  info.getSearchFields()) {
                String id = "table." + searchField.getName();
                if (request.defined(id)) {
                    //Columns are 1 based to the user
                    if (searchField.getName().startsWith("column")) {
                        int column = Integer.parseInt(
                                         searchField.getName().substring(
                                             "column".length()).trim()) - 1;
                        String s = request.getString(id, "");
                        s = s.trim();
                        System.err.println("column:" + column + " s:" + s);
                        String operator = StringUtil.findPattern(s,
                                              "^([<>=]+).*");
                        if (operator != null) {
                            System.err.println("operator:" + operator);
                            s = s.replace(operator, "").trim();
                            double value = Double.parseDouble(s);
                            int op = Filter.ValueFilter.getOperator(operator);
                            info.getFilter().addFilter(
                                new Filter.ValueFilter(column, op, value));

                            continue;
                        }
                        //                        if(s.

                        info.getFilter().addFilter(
                            new Filter.PatternFilter(
                                column, request.getString(id, "")));

                    }
                }

            }
        }

        String searchText = request.getString("table.text", (String) null);
        if (Utils.stringDefined(searchText)) {
            //match all
            info.getFilter().addFilter(new Filter.PatternFilter(-1, "(?i:.*" + searchText + ".*)"));
        }


        System.err.println ("Process");
        CsvUtil.process(info);
        //        visitor.visit(visitInfo, entry.getName(), rows);
    }



    /**
     * _more_
     *
     * @param suffix _more_
     * @param inputStream _more_
     *
     * @return _more_
     *
     * @throws Exception _more_
     */
    private Workbook makeWorkbook(String suffix, InputStream inputStream)
            throws Exception {
        return (suffix.equals(".xls")
                ? new HSSFWorkbook(inputStream)
                : new XSSFWorkbook(inputStream));

    }



    /**
     * _more_
     *
     * @param request _more_
     * @param entry _more_
     * @param suffix _more_
     * @param inputStream _more_
     * @param visitInfo _more_
     * @param visitor _more_
     *
     * @throws Exception _more_
     */
    private void visitXls(Request request, Entry entry, String suffix,
                          InputStream inputStream,
                          TabularVisitInfo visitInfo, TabularVisitor visitor)
            throws Exception {
        //        System.err.println("visitXls: making workbook");
        Workbook wb = makeWorkbook(suffix, inputStream);
        //        System.err.println("visitXls:" + skip + " max rows:" + maxRows + " #sheets:" + wb.getNumberOfSheets());
        int maxRows = visitInfo.getMaxRows();
        for (int sheetIdx = 0; sheetIdx < wb.getNumberOfSheets();
                sheetIdx++) {
            if ( !visitInfo.okToShowSheet(sheetIdx)) {
                continue;
            }
            Sheet sheet = wb.getSheetAt(sheetIdx);
            //            System.err.println("\tsheet:" + sheet.getSheetName() + " #rows:" + sheet.getLastRowNum());
            List<List<Object>> rows      = new ArrayList<List<Object>>();
            int                sheetSkip = visitInfo.getSkipRows();
            for (int rowIdx = sheet.getFirstRowNum();
                    (rows.size() < maxRows)
                    && (rowIdx <= sheet.getLastRowNum());
                    rowIdx++) {
                if (sheetSkip-- > 0) {
                    continue;
                }

                Row row = sheet.getRow(rowIdx);
                if (row == null) {
                    continue;
                }
                List<Object> cols     = new ArrayList<Object>();
                short        firstCol = row.getFirstCellNum();
                for (short col = firstCol;
                        (col < MAX_COLS) && (col < row.getLastCellNum());
                        col++) {
                    Cell cell = row.getCell(col);
                    if (cell == null) {
                        break;
                    }
                    Object value = null;
                    int    type  = cell.getCellType();
                    if (type == cell.CELL_TYPE_NUMERIC) {
                        value = new Double(cell.getNumericCellValue());
                    } else if (type == cell.CELL_TYPE_BOOLEAN) {
                        value = new Boolean(cell.getBooleanCellValue());
                    } else if (type == cell.CELL_TYPE_ERROR) {
                        value = "" + cell.getErrorCellValue();
                    } else if (type == cell.CELL_TYPE_BLANK) {
                        value = "";
                    } else if (type == cell.CELL_TYPE_FORMULA) {
                        value = cell.getCellFormula();
                    } else {
                        value = cell.getStringCellValue();
                    }
                    cols.add(value);
                }

                /**** TODO
                org.ramadda.util.text.Row row = new Row(cols);

                if ( !visitInfo.rowOk(row)) {
                    if (rows.size() == 0) {
                        //todo: check for the header line
                    } else {
                        continue;
                    }
                }
                *****/
                //                rows.add(cols);
                //                if(xxx>15) break;
            }
            //            System.err.println("Rows:" + rows);
            //            if ( !visitor.visit(visitInfo, sheet.getSheetName(), rows)) {
            //                break;
            //            }
        }
    }



    /**
     * _more_
     *
     * @param request _more_
     * @param requestProps _more_
     * @param entry _more_
     *
     * @return _more_
     *
     * @throws Exception _more_
     */
    public String getHtmlDisplay(Request request, Hashtable requestProps,
                                 Entry entry)
            throws Exception {

        if (isTabular(entry)) {
            TabularTypeHandler handler =
                (TabularTypeHandler) entry.getTypeHandler();
            if ( !handler.okToShowTable(request, entry)) {
                return null;
            }
        }


        boolean showTable = entry.getValue(XlsTypeHandler.IDX_SHOWTABLE,
                                           true);
        boolean showChart = entry.getValue(XlsTypeHandler.IDX_SHOWCHART,
                                           true);


        boolean useFirstRowAsHeader =
            Misc.equals("true",
                        entry.getValue(XlsTypeHandler.IDX_USEFIRSTROW,
                                       "true"));


        boolean colHeader =
            Misc.equals("true",
                        entry.getValue(XlsTypeHandler.IDX_COLHEADER,
                                       "false"));
        boolean rowHeader =
            Misc.equals("true",
                        entry.getValue(XlsTypeHandler.IDX_ROWHEADER,
                                       "false"));
        List<String> widths =
            StringUtil.split(entry.getValue(XlsTypeHandler.IDX_WIDTHS, ""),
                             ",", true, true);



        List<String> sheetsStr =
            StringUtil.split(entry.getValue(XlsTypeHandler.IDX_SHEETS, ""),
                             ",", true, true);


        List propsList = new ArrayList();


        propsList.add("useFirstRowAsHeader");
        propsList.add("" + useFirstRowAsHeader);
        propsList.add("showTable");
        propsList.add("" + showTable);
        propsList.add("showChart");
        propsList.add("" + showChart);


        propsList.add("rowHeaders");
        propsList.add("" + rowHeader);

        propsList.add("skipRows");
        propsList.add(entry.getValue(XlsTypeHandler.IDX_SKIPROWS, "0"));
        propsList.add("skipColumns");
        propsList.add(entry.getValue(XlsTypeHandler.IDX_SKIPCOLUMNS, "0"));

        List<String> header =
            StringUtil.split(entry.getValue(XlsTypeHandler.IDX_HEADER, ""),
                             ",", true, true);
        if (header.size() > 0) {
            propsList.add("colHeaders");
            propsList.add(Json.list(header, true));
        } else {
            propsList.add("colHeaders");
            propsList.add("" + colHeader);
        }


        if (widths.size() > 0) {
            propsList.add("colWidths");
            propsList.add(Json.list(widths));
        }

        String jsonUrl =
            request.entryUrl(getRepository().URL_ENTRY_SHOW, entry,
                             ARG_OUTPUT,
                             TabularOutputHandler.OUTPUT_XLS_JSON.getId());

        List<String> charts = new ArrayList<String>();
        for (String line :
                StringUtil.split(entry.getValue(XlsTypeHandler.IDX_CHARTS,
                    ""), "\n", true, true)) {

            List<String>              chart = new ArrayList<String>();
            Hashtable<String, String> map   = new Hashtable<String, String>();
            for (String tok : StringUtil.split(line, ",")) {
                List<String> subtoks = StringUtil.splitUpTo(tok, "=", 2);
                String       key     = subtoks.get(0);
                if (subtoks.size() < 2) {
                    chart.add("type");
                    chart.add(Json.quote(key));

                    continue;
                }
                String value = subtoks.get(1);
                chart.add(key);
                chart.add(Json.quote(value));
            }
            charts.add(Json.map(chart));
        }
        if (charts.size() > 0) {
            propsList.add("defaultCharts");
            propsList.add(Json.list(charts));
        }

        propsList.add("url");
        propsList.add(Json.quote(jsonUrl));

        /***
        TabularVisitInfo visitInfo = new TabularVisitInfo(request, entry);
        if (visitInfo.getSearchFields() != null) {
            propsList.add("searchFields");
            List<String> names = new ArrayList<String>();
            for (SearchField searchField :
                    visitInfo.getSearchFields()) {

                List<String> props = new ArrayList<String>();
                props.add("name");
                props.add(Json.quote(searchField.getName()));
                props.add("label");
                props.add(Json.quote(searchField.getLabel()));
                names.add(Json.map(props));
            }
            propsList.add(Json.list(names));
        }
        */

        String props = Json.map(propsList);
        System.err.println(props);

        StringBuilder sb = new StringBuilder();
        getRepository().getWikiManager().addDisplayImports(request, sb);
        sb.append(header(entry.getName()));
        if ( !request.get(ARG_EMBEDDED, false)) {
            sb.append(entry.getDescription());
        }
        String divId = HtmlUtils.getUniqueId("div_");
        sb.append(HtmlUtils.div("", HtmlUtils.id(divId)));
        StringBuilder js = new StringBuilder();
        js.append("var displayManager = getOrCreateDisplayManager(\"" + divId
                  + "\",");
        js.append(Json.map("showMap", "false", "showMenu", "false",
                           "showTitle", "false", "layoutType",
                           Json.quote("table"), "layoutColumns", "1"));
        js.append(",true);\n");
        js.append("displayManager.createDisplay('xls'," + props + ");\n");
        sb.append(HtmlUtils.script(js.toString()));

        return sb.toString();

    }


    /**
     * _more_
     *
     * @param entry _more_
     *
     * @return _more_
     */
    public static boolean isTabular(Entry entry) {
        if (entry == null) {
            return false;
        }

        return entry.getTypeHandler().isType(TabularTypeHandler.TYPE_TABULAR);
    }

    /**
     * _more_
     *
     * @param request _more_
     * @param service _more_
     * @param input _more_
     * @param args _more_
     *
     *
     * @return _more_
     * @throws Exception _more_
     */
    public boolean extractSheet(Request request, Service service,
                                ServiceInput input, List args)
            throws Exception {
        Entry entry = null;
        for (Entry e : input.getEntries()) {
            if (isTabular(e)) {
                entry = e;

                break;
            }
        }
        if (entry == null) {
            throw new IllegalArgumentException("No tabular entry found");
        }

        HashSet<Integer> sheetsToShow = getSheetsToShow((String) args.get(0));

        final SXSSFWorkbook wb        = new SXSSFWorkbook(100);
        //        final Workbook   wb           = new XSSFWorkbook();

        String name = getStorageManager().getFileTail(entry);
        if ( !Utils.stringDefined(name)) {
            name = entry.getName();
        }
        name = IOUtil.stripExtension(name);

        File newFile = new File(IOUtil.joinDir(input.getProcessDir(),
                           name + ".xlsx"));

        TabularVisitor visitor = new TabularVisitor() {
            public boolean visit(TabularVisitInfo info, String sheetName,
                                 List<List<Object>> rows) {
                sheetName = sheetName.replaceAll("[/]+", "-");
                Sheet sheet  = wb.createSheet(sheetName);
                int   rowCnt = 0;
                for (List<Object> cols : rows) {
                    Row row = sheet.createRow(rowCnt++);
                    for (int colIdx = 0; colIdx < cols.size(); colIdx++) {
                        Object col  = cols.get(colIdx);
                        Cell   cell = row.createCell(colIdx);
                        if (col instanceof Double) {
                            cell.setCellValue(((Double) col).doubleValue());
                        } else if (col instanceof Date) {
                            cell.setCellValue((Date) col);
                        } else if (col instanceof Boolean) {
                            cell.setCellValue(((Boolean) col).booleanValue());
                        } else {
                            cell.setCellValue(col.toString());
                        }
                    }
                }

                return true;
            }
        };

        TabularVisitInfo visitInfo =
            new TabularVisitInfo(
                request, entry, getSkipRows(request, entry),
                getRowCount(request, entry, Integer.MAX_VALUE), sheetsToShow);

        ProcessInfo info =  new ProcessInfo();
        info.setSkip(getSkipRows(request, entry));
        info.setMaxRows(getRowCount(request, entry,MAX_ROWS));
        //        http:://localhost:8080/repository/entry/show?entryid=740ae258-805d-4a1f-935d-289d0a6e5519&output=media_tabular_extractsheet&serviceform=true&execute=Execute

        visit(request, entry, info, visitor);

        FileOutputStream fileOut = new FileOutputStream(newFile);
        wb.write(fileOut);
        fileOut.close();
        wb.dispose();

        return true;

    }



    public boolean csv(Request request, Service service,
                       ServiceInput input, List args)
            throws Exception {
        return true;
    }

    /*
        Entry entry = null;
        for (Entry e : input.getEntries()) {
            if (isTabular(e)) {
                entry = e;

                break;
            }
        }
        if (entry == null) {
            throw new IllegalArgumentException("No tabular entry found");
        }

        HashSet<Integer> sheetsToShow = getSheetsToShow((String) args.get(0));
        String name = getStorageManager().getFileTail(entry);
        if ( !Utils.stringDefined(name)) {
            name = entry.getName();
        }
        name = IOUtil.stripExtension(name);

        File newFile = new File(IOUtil.joinDir(input.getProcessDir(),
                           name + ".csv"));

        String file = "";
        InputStream inputStream = new BufferedInputStream(
                                                          getStorageManager().getFileInputStream(file));
        final ProcessInfo info =
            new ProcessInfo(new BufferedInputStream(inputStream), new FileOutputStream(newFile));


        TabularVisitor visitor = new TabularVisitor() {
            public boolean visit(TabularVisitInfo info, String sheetName,
                                 List<List<Object>> rows) {
                return true;
            }
        };

        TabularVisitInfo visitInfo =
            new TabularVisitInfo(
                request, entry, getSkipRows(request, entry),
                getRowCount(request, entry, Integer.MAX_VALUE), sheetsToShow);


        visit(request, entry, visitInfo, visitor);


        return true;

    }
    */


}
