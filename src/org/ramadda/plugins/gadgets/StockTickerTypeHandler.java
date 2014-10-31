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

package org.ramadda.plugins.gadgets;


import org.ramadda.repository.*;
import org.ramadda.repository.type.*;


import org.ramadda.util.HtmlUtils;
import org.ramadda.util.Json;
import org.ramadda.util.Utils;


import org.w3c.dom.*;

import ucar.unidata.util.StringUtil;

import java.util.Date;
import java.util.List;


/**
 *
 *
 */
public class StockTickerTypeHandler extends GenericTypeHandler {



    /**
     * _more_
     *
     * @param repository _more_
     * @param entryNode _more_
     *
     * @throws Exception _more_
     */
    public StockTickerTypeHandler(Repository repository, Element entryNode)
            throws Exception {
        super(repository, entryNode);
    }


    /**
     * _more_
     *
     * @param request _more_
     * @param group _more_
     * @param subGroups _more_
     * @param entries _more_
     * @param entry _more_
     *
     * @return _more_
     *
     * @throws Exception _more_
     */
    public Result getHtmlDisplay(Request request, Entry entry)
            throws Exception {
        StringBuffer sb = new StringBuffer();
        sb.append(entry.getDescription());
        sb.append(HtmlUtils.p());
        StringBuffer js       = new StringBuffer();
        String       symbols  = entry.getValue(0, "");
        String       width    = entry.getValue(1, "400");
        String       height   = entry.getValue(2, "400");
        String       interval = entry.getValue(3, "60");
        if ( !Utils.stringDefined(width)) {
            width = "400";
        }
        if ( !Utils.stringDefined(height)) {
            width = "400";
        }
        if ( !Utils.stringDefined(interval)) {
            interval = "60";
        }

        sb.append(
            HtmlUtils.importJS(
                "https://d33t3vvu2t2yu5.cloudfront.net/tv.js"));

        for (String line : StringUtil.split(symbols, "\n", true, true)) {
            js.append("new TradingView.widget(");
            js.append(Json.mapAndQuote("symbol", line, "width", width,
                                       "height", height, "interval",
                                       interval, "timezone", "exchange",
                                       "theme", "White", "style", "2",
                                       "toolbar_bg", "#f1f3f6",
                                       "hide_top_toolbar", "true",
                                       "allow_symbol_change", "true",
                                       "hideideas", "true",
                                       "show_popup_button", "false",
                                       "popup_width", "1000", "popup_height",
                                       "650"));
            js.append(");\n");
        }
        sb.append(HtmlUtils.script(js.toString()));

        return new Result(msg("Stock ticker"), sb);
    }




}
