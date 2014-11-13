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

import java.text.SimpleDateFormat;

import java.util.ArrayList;
import java.util.Date;
import java.util.Enumeration;

import java.util.GregorianCalendar;
import java.util.Hashtable;
import java.util.List;
import java.util.Properties;
import java.util.TimeZone;


import java.util.regex.*;
import java.util.zip.*;


/**
 */
public class XlsOutputHandler extends OutputHandler {


    /** _more_ */
    public static final OutputType OUTPUT_XLS_JSON =
        new OutputType("XLS to JSON", "xls_json", OutputType.TYPE_FEEDS, "",
                       "/media/xls.png");


    /**
     * _more_
     */
    public XlsOutputHandler() {}

    /**
     * _more_
     *
     *
     * @param repository _more_
     * @param element _more_
     * @throws Exception _more_
     */
    public XlsOutputHandler(Repository repository, Element element)
            throws Exception {
        super(repository, element);
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
        if (state.getEntry().getTypeHandler().isType("type_document_xls")) {
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
        try {
            return outputEntryInner(request, outputType, entry);
        } catch (org.apache.poi.hssf.OldExcelFormatException exc) {
            StringBuilder sb = new StringBuilder();
            sb.append(Json.map("error",
                               Json.quote("Old Excel format not supported")));
            request.setReturnFilename(entry.getName() + ".json");
            Result result = new Result("", sb);
            result.setShouldDecorate(false);
            result.setMimeType("application/json");

            return result;
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
    private Result outputEntryInner(Request request, OutputType outputType,
                                    Entry entry)
            throws Exception {
        StringBuilder sb     = new StringBuilder();
        List<String>  sheets = new ArrayList<String>();
        String        file   = entry.getFile().toString();
        InputStream   myxls  = getStorageManager().getFileInputStream(file);

        if (file.endsWith(".xlsx")) {
            readXlsx(myxls, sheets);
        } else {
            readXls(myxls, sheets);
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
     *
     * @throws Exception _more_
     */
    private void readXls(InputStream myxls, List<String> sheets)
            throws Exception {
        HSSFWorkbook wb = new HSSFWorkbook(myxls);
        for (int sheetIdx = 0; sheetIdx < wb.getNumberOfSheets();
                sheetIdx++) {
            HSSFSheet    sheet = wb.getSheetAt(sheetIdx);
            List<String> rows  = new ArrayList<String>();
            for (int rowIdx = sheet.getFirstRowNum();
                    rowIdx <= sheet.getLastRowNum(); rowIdx++) {
                HSSFRow row = sheet.getRow(rowIdx);
                if (row == null) {
                    continue;
                }
                List<String> cols     = new ArrayList<String>();
                short        firstCol = row.getFirstCellNum();
                for (short col = firstCol; col < row.getLastCellNum();
                        col++) {
                    HSSFCell cell = row.getCell(col);
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


    /**
     * _more_
     *
     * @param myxls _more_
     * @param sheets _more_
     *
     * @throws Exception _more_
     */
    private void readXlsx(InputStream myxls, List<String> sheets)
            throws Exception {
        XSSFWorkbook wb = new XSSFWorkbook(myxls);
        for (int sheetIdx = 0; sheetIdx < wb.getNumberOfSheets();
                sheetIdx++) {
            XSSFSheet    sheet = wb.getSheetAt(sheetIdx);
            List<String> rows  = new ArrayList<String>();
            for (int rowIdx = sheet.getFirstRowNum();
                    rowIdx <= sheet.getLastRowNum(); rowIdx++) {
                XSSFRow row = sheet.getRow(rowIdx);
                if (row == null) {
                    continue;
                }
                List<String> cols     = new ArrayList<String>();
                short        firstCol = row.getFirstCellNum();
                for (short col = firstCol; col < row.getLastCellNum();
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


}
