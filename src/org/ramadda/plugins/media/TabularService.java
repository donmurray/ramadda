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

import org.ramadda.data.process.*;

import org.ramadda.data.process.Service;

import org.ramadda.repository.*;
import org.ramadda.repository.job.JobManager;
import org.ramadda.repository.output.*;
import org.ramadda.util.HtmlUtils;

import org.ramadda.util.TempDir;

import org.w3c.dom.*;

import ucar.unidata.util.IOUtil;
import ucar.unidata.util.StringUtil;
import ucar.unidata.xml.XmlUtil;

import java.io.*;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashSet;
import java.util.List;



/**
 *
 * @author Jeff McWhirter/ramadda.org
 */
public class TabularService extends Service {


    /**
     * ctor
     *
     * @param repository _more_
     * @param element _more_
     * @throws Exception _more_
     */
    public TabularService(Repository repository, Element element)
            throws Exception {
        super(repository, element);
    }


    /**
     * _more_
     *
     * @return _more_
     *
     * @throws Exception _more_
     */
    public TabularOutputHandler getTabularOutputHandler() throws Exception {
        return (TabularOutputHandler) getRepository().getOutputHandler(
            TabularOutputHandler.class);
    }

    /**
     * _more_
     *
     * @param request _more_
     * @param service _more_
     * @param input _more_
     * @param args _more_
     *
     * @throws Exception _more_
     */
    public void extractSheet(Request request, Service service,
                             ServiceInput input, List args)
            throws Exception {
        Entry entry = null;
        for (Entry e : input.getEntries()) {
            if (e.getTypeHandler().isType("type_document_xls")) {
                entry = e;

                break;
            }
        }
        if (entry == null) {
            throw new IllegalArgumentException("No tabular entry found");
        }

        TabularOutputHandler toh = getTabularOutputHandler();
        HashSet<Integer> sheetsToShow =
            toh.getSheetsToShow((String) args.get(2));

        final XSSFWorkbook wb = new XSSFWorkbook();
        
        File newFile =
            new File(
                     IOUtil.joinDir(
                                    input.getProcessDir(),
                                    IOUtil.stripExtension(
                                                          getStorageManager().getFileTail(
                                                                                          entry)) +".xlsx"));

        final List<String> sheets  = new ArrayList<String>();
        TabularVisitor     visitor = new TabularVisitor() {
            public boolean visit(String sheetName, List<List<String>> rows) {
                XSSFSheet sheet = wb.createSheet(sheetName);
                short rowCnt = 0;
                for(List<String> cols: rows) {
                    XSSFRow row = sheet.createRow(rowCnt++);
                    for(int colIdx=0;colIdx<cols.size();colIdx++) {
                        XSSFCell cell = row.createCell(colIdx);
                        cell.setCellValue(cols.get(colIdx));
                    }
                }
                return true;
            }
        };
        toh.visit(request, entry, entry.getFile().toString(), sheetsToShow,
                  visitor);

        FileOutputStream fileOut = new FileOutputStream(newFile);
        wb.write(fileOut);
        fileOut.close();


    }

}
