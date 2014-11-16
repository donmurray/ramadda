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


import org.apache.poi.hssf.usermodel.HSSFCell;
import org.apache.poi.hssf.usermodel.HSSFDateUtil;
import org.apache.poi.hssf.usermodel.HSSFRow;
import org.apache.poi.hssf.usermodel.HSSFSheet;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;

import org.apache.poi.xssf.usermodel.XSSFCell;
import org.apache.poi.xssf.usermodel.XSSFRow;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;


import org.ramadda.repository.*;
import org.ramadda.repository.auth.*;
import org.ramadda.repository.output.*;
import org.ramadda.util.GoogleChart;
import org.ramadda.util.HtmlUtils;
import org.ramadda.util.Json;
import org.ramadda.util.Utils;

import org.ramadda.util.XlsUtil;


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
import java.util.Properties;
import java.util.TimeZone;


import java.util.regex.*;



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
        Entry   entry = state.getEntry();
        if(entry == null) return;
        boolean isXls = entry.getTypeHandler().isType("type_document_tabular");
        if ( !isXls) {
            if ( !entry.isFile()) {
                return;
            }
            String path = entry.getResource().getPath();
            if (path.endsWith(".xls") || path.endsWith(".xlsx") || path.endsWith(".csv")) {
                isXls = true;
            }
        }

        if (isXls) {
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
        StringBuilder    sb           = new StringBuilder();
        List<String>     sheets       = new ArrayList<String>();

        String           file         = entry.getFile().toString();
        InputStream      myxls = getStorageManager().getFileInputStream(file);



        HashSet<Integer> sheetsToShow = null;
        if (entry.getTypeHandler().isType("type_document_tabular")) {
            List<String> sheetsStr =
                StringUtil.split(entry.getValue(XlsTypeHandler.IDX_SHEETS,
                    ""), ",", true, true);
            if (sheetsStr.size() > 0) {
                sheetsToShow = new HashSet<Integer>();
                for (String s : sheetsStr) {
                    sheetsToShow.add(Integer.parseInt(s));
                }
            }
        }

        int skip = request.get("table.skip",0);

        String suffix = IOUtil.getFileExtension(file).toLowerCase();
        if (suffix.equals(".xlsx")) {
            readXlsx(myxls, sheets, sheetsToShow, skip);
        } else if (suffix.endsWith(".xls")) {
            readXls(myxls, sheets, sheetsToShow, skip);
        } else if (suffix.endsWith(".csv")) {
            readCsv(entry,myxls, sheets, skip);
        } else {
            throw new IllegalStateException("Unknown file type:" + suffix);
        }


        sb.append(Json.list(sheets));
        request.setReturnFilename(entry.getName() + ".json");
        Result result = new Result("", sb);
        result.setShouldDecorate(false);
        result.setMimeType("application/json");

        return result;
    }


    /**
     * _more_
     *
     * @param myxls _more_
     * @param sheets _more_
     * @param sheetsToShow _more_
     *
     * @throws Exception _more_
     */
    private void readXls(InputStream myxls, List<String> sheets,
                         HashSet<Integer> sheetsToShow, int skip)
            throws Exception {
        HSSFWorkbook wb = new HSSFWorkbook(myxls);
        for (int sheetIdx = 0; sheetIdx < wb.getNumberOfSheets();
                sheetIdx++) {
            if ((sheetsToShow != null) && !sheetsToShow.contains(sheetIdx)) {
                continue;
            }
            HSSFSheet    sheet = wb.getSheetAt(sheetIdx);
            List<String> rows  = new ArrayList<String>();
            int sheetSkip=skip;
            int rowCnt = 0;
            for (int rowIdx = sheet.getFirstRowNum();
                    (rowCnt < MAX_ROWS) && (rowIdx <= sheet.getLastRowNum());
                    rowIdx++) {
                if(sheetSkip-->0) continue;

                HSSFRow row = sheet.getRow(rowIdx);
                if (row == null) {
                    continue;
                }
                rowCnt++;

                List<String> cols     = new ArrayList<String>();
                short        firstCol = row.getFirstCellNum();
                for (short col = firstCol;
                        (col < MAX_COLS) && (col < row.getLastCellNum());
                        col++) {
                    HSSFCell cell = row.getCell(col);
                    if (cell == null) {
                        break;
                    }
                    String value = cell.toString();
                    value = value.replaceAll("\"", "&quot;");
                    cols.add(Json.quote(value));
                }
                rows.add(Json.list(cols));
            }
            sheets.add(Json.map("name", Json.quote(sheet.getSheetName()),
                                "rows", Json.list(rows)));
        }
    }


    /**
     * _more_
     *
     * @param myxls _more_
     * @param sheets _more_
     * @param sheetsToShow _more_
     *
     * @throws Exception _more_
     */

    private void readXlsx(InputStream myxls, List<String> sheets,
                          HashSet<Integer> sheetsToShow, int skip)
            throws Exception {
        XSSFWorkbook wb = new XSSFWorkbook(myxls);
        for (int sheetIdx = 0; sheetIdx < wb.getNumberOfSheets();
                sheetIdx++) {
            XSSFSheet    sheet = wb.getSheetAt(sheetIdx);
            List<String> rows  = new ArrayList<String>();
            int sheetSkip = skip;
            int rowCnt = 0;
            for (int rowIdx = sheet.getFirstRowNum();
                 (rowCnt < MAX_ROWS) && (rowIdx <= sheet.getLastRowNum());
                 rowIdx++) {
                if(sheetSkip-->0) continue;

                XSSFRow row = sheet.getRow(rowIdx);
                if (row == null) {
                    continue;
                }
                rowCnt++;
                List<String> cols     = new ArrayList<String>();
                short        firstCol = row.getFirstCellNum();
                for (short col = firstCol;
                        (col < MAX_COLS) && (col < row.getLastCellNum());
                        col++) {
                    XSSFCell cell = row.getCell(col);
                    if (cell == null) {
                        break;
                    }
                    String value = cell.toString();
                    cols.add(XlsUtil.clean(Json.quote(value)));
                }
                rows.add(Json.list(cols));
            }
            sheets.add(Json.map("name", Json.quote(sheet.getSheetName()),
                                "rows", Json.list(rows)));
        }
    }

    private void readCsv(Entry entry, InputStream myxls, List<String> sheets, int skip)
        throws Exception {
        BufferedReader br =  new BufferedReader(new InputStreamReader(myxls));
        String line;
        int rowIdx =0;
        List<String> rows  = new ArrayList<String>();
        while((line=br.readLine())!=null) {
            if(line.startsWith("#")) {
                continue;
            }
            if(skip-->0) continue;
            rowIdx++;
            if(rowIdx > MAX_ROWS) {
                break;
            }
            List<String> cols     = Utils.tokenizeColumns(line, ",");
            rows.add(Json.list(cols, true));
        }
        sheets.add(Json.map("name", Json.quote(entry.getName()),
                            "rows", Json.list(rows)));
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

        boolean showTable  = entry.getValue(XlsTypeHandler.IDX_SHOWTABLE, true);
        boolean showChart  = entry.getValue(XlsTypeHandler.IDX_SHOWCHART, true);


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

        List<String> charts  = new ArrayList<String>();
        for(String line: 
                StringUtil.split(entry.getValue(XlsTypeHandler.IDX_CHARTS,""),"\n", true, true)) {

            List<String> chart  = new ArrayList<String>();
            Hashtable<String,String> map = new Hashtable<String,String>();
            for(String tok:StringUtil.split(line,",")) {
                List<String>subtoks = StringUtil.splitUpTo(tok,"=",2);
                String key = subtoks.get(0);
                if(subtoks.size()<2) {
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
        if(charts.size()>0) {
            propsList.add("defaultCharts");
            propsList.add(Json.list(charts));
        }

        propsList.add("url");
        propsList.add(Json.quote(jsonUrl));

        String        props = Json.map(propsList);

        StringBuilder sb    = new StringBuilder();

        getRepository().getWikiManager().addDisplayImports(request, sb);

        sb.append(
            HtmlUtils.importJS(
                getRepository().getUrlBase()
                + "/media/jquery.handsontable.full.min.js"));
        sb.append(
            HtmlUtils.cssLink(
                getRepository().getUrlBase()
                + "/media/jquery.handsontable.full.min.css"));

        sb.append(HtmlUtils.cssLink(getRepository().getUrlBase()
                                    + "/media/xls.css"));
        sb.append(HtmlUtils.importJS(getRepository().getUrlBase()
                                     + "/media/xls.js"));

        sb.append("\n");

        sb.append(header(entry.getName()));
        sb.append(entry.getDescription());
        String divId = HtmlUtils.getUniqueId("div_");
        sb.append(HtmlUtils.div("", HtmlUtils.id(divId)));
        StringBuilder js = new StringBuilder();
        js.append("var displayManager = getOrCreateDisplayManager(\"" + divId
                  + "\",");
        js.append(Json.map("showMap", "false", "showMenu", "false",
                           "showTitle", "false", "layoutType",
                           Json.quote("table"), "layoutColumns", "1"));
        js.append(");\n");
        js.append("displayManager.createDisplay('xls'," + props + ");\n");
        sb.append(HtmlUtils.script(js.toString()));

        return sb.toString();

    }




}
