/*
 * Copyright 1997-2010 Unidata Program Center/University Corporation for
 * Atmospheric Research, P.O. Box 3000, Boulder, CO 80307,
 * support@unidata.ucar.edu.
 * 
 * This library is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation; either version 2.1 of the License, or (at
 * your option) any later version.
 * 
 * This library is distributed in the hope that it will be useful, but
 * WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU Lesser
 * General Public License for more details.
 * 
 * You should have received a copy of the GNU Lesser General Public License
 * along with this library; if not, write to the Free Software Foundation,
 * Inc., 59 Temple Place, Suite 330, Boston, MA 02111-1307 USA
 */

package ucar.unidata.repository.output;


import org.w3c.dom.*;

import ucar.unidata.repository.*;
import ucar.unidata.repository.auth.*;

import ucar.unidata.sql.SqlUtil;
import ucar.unidata.ui.ImageUtils;
import ucar.unidata.util.DateUtil;
import ucar.unidata.util.HtmlUtil;
import ucar.unidata.util.IOUtil;
import ucar.unidata.util.Misc;


import ucar.unidata.util.StringUtil;
import ucar.unidata.xml.XmlUtil;


import java.io.*;

import java.io.File;



import java.net.*;



import java.util.ArrayList;
import java.util.Date;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.List;
import java.util.Properties;



import java.util.regex.*;

import java.util.zip.*;


/**
 *
 *
 * @author IDV Development Team
 * @version $Revision: 1.3 $
 */
public class TextOutputHandler extends OutputHandler {



    /** _more_ */
    public static final OutputType OUTPUT_TEXT =
        new OutputType("Annotated Text", "text", OutputType.TYPE_HTML, "",
                       ICON_TEXT);

    /** _more_ */
    public static final OutputType OUTPUT_WORDCLOUD =
        new OutputType("Word Cloud", "wordcloud", OutputType.TYPE_HTML, "",
                       ICON_CLOUD);

    /** _more_          */
    public static final OutputType OUTPUT_PRETTY =
        new OutputType("Pretty Print", "pretty", OutputType.TYPE_HTML, "",
                       ICON_TEXT);


    /**
     * _more_
     *
     *
     * @param repository _more_
     * @param element _more_
     * @throws Exception _more_
     */
    public TextOutputHandler(Repository repository, Element element)
            throws Exception {
        super(repository, element);
        addType(OUTPUT_TEXT);
        addType(OUTPUT_WORDCLOUD);
        addType(OUTPUT_PRETTY);
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

        if (state.entry == null) {
            return;
        }

        if ( !state.entry.isFile()) {
            return;
        }
        if ( !getRepository().getAccessManager().canAccessFile(request,
                state.entry)) {
            return;
        }

        String   path     = state.entry.getResource().getPath();
        String[] suffixes = new String[] {
            ".csv", ".txt", ".java", ".c", ".h", ".cc", ".f90", ".cpp", ".hh",
            ".cdl", ".sh", "m4"
        };

        String[] codesuffixes = new String[] {
            ".bsh", ".c", ".cc", ".cpp", ".cs", ".csh", ".cyc", ".cv", ".htm",
            ".html", ".java", ".js", ".m", ".mxml", ".perl", ".pl", ".pm",
            ".py", ".rb", ".sh", ".xhtml", ".xml", ".xsl"
        };

        for (int i = 0; i < codesuffixes.length; i++) {
            if (path.endsWith(codesuffixes[i])) {
                links.add(makeLink(request, state.entry, OUTPUT_PRETTY));
                return;
            }
        }


        for (int i = 0; i < suffixes.length; i++) {
            if (path.endsWith(suffixes[i])) {
                links.add(makeLink(request, state.entry, OUTPUT_TEXT));
                links.add(makeLink(request, state.entry, OUTPUT_WORDCLOUD));
                return;
            }
        }





        String suffix = IOUtil.getFileExtension(path);
        String type   = getRepository().getMimeTypeFromSuffix(suffix);
        if ((type != null) && type.startsWith("text/")) {

            links.add(makeLink(request, state.entry, OUTPUT_TEXT));
            links.add(makeLink(request, state.entry, OUTPUT_WORDCLOUD));
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
    public Result outputEntry(Request request, OutputType outputType,
                              Entry entry)
            throws Exception {
        if ( !getRepository().getAccessManager().canAccessFile(request,
                entry)) {
            throw new AccessException("Cannot access data", request);
        }

        if (outputType.equals(OUTPUT_WORDCLOUD)) {
            return outputWordCloud(request, entry);
        }


        if (outputType.equals(OUTPUT_PRETTY)) {
            return outputPretty(request, entry);
        }


        String contents =
            getStorageManager().readSystemResource(entry.getFile());
        StringBuffer sb = new StringBuffer();

        sb.append("<pre>\n");
        int cnt = 0;
        for (String line :
                (List<String>) StringUtil.split(contents, "\n", false,
                false)) {
            cnt++;
            line = line.replace("\r", "");
            line = HtmlUtil.entityEncode(line);
            sb.append("<a " + HtmlUtil.attr("name", "line" + cnt)
                      + "></a><a href=#line" + cnt + ">" + cnt + "</a> "
                      + HtmlUtil.space(1) + line + "<br>");
        }
        sb.append("</pre>");
        return makeLinksResult(request, msg("Text"), sb, new State(entry));
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
    public Result outputWordCloud(Request request, Entry entry)
            throws Exception {
        String contents =
            getStorageManager().readSystemResource(entry.getFile());
        StringBuffer sb   = new StringBuffer();

        StringBuffer head = new StringBuffer();
        head.append("\n");
        head.append(
            "<link rel=\"stylesheet\" type=\"text/css\" href=\"http://visapi-gadgets.googlecode.com/svn/trunk/wordcloud/wc.css\">\n");
        head.append(
            HtmlUtil.importJS(
                "http://visapi-gadgets.googlecode.com/svn/trunk/wordcloud/wc.js"));
        head.append("\n");
        head.append(HtmlUtil.importJS("http://www.google.com/jsapi"));
        head.append("\n");

        sb.append("<div id=\"wcdiv\"></div>");
        sb.append("\n");
        StringBuffer js = new StringBuffer();
        js.append("google.load(\"visualization\", \"1\");\n");
        js.append("google.setOnLoadCallback(draw);\n");
        js.append("function draw() {\n");
        js.append("var data = new google.visualization.DataTable();\n");
        js.append("data.addColumn('string', 'Text1');\n");
        List<String> lines = (List<String>) StringUtil.split(contents, "\n",
                                 false, false);

        js.append("data.addRows(" + lines.size() + ");\n");
        int cnt = 0;
        for (String line : lines) {
            line = line.replace("\r", "");
            line = line.replace("'", "\\'");
            js.append("data.setCell(" + cnt + ", 0, '" + line + "');\n");
            cnt++;
        }
        js.append("var outputDiv = document.getElementById('wcdiv');\n");
        js.append("var wc = new WordCloud(outputDiv);\n");
        js.append("wc.draw(data, null);\n");
        js.append("      }");
        sb.append(HtmlUtil.script(js.toString()));
        Result result = makeLinksResult(request, msg("Word Cloud"), sb,
                                        new State(entry));
        result.putProperty(PROP_HTML_HEAD, head.toString());
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
    public Result outputPretty(Request request, Entry entry)
            throws Exception {
        String contents =
            getStorageManager().readSystemResource(entry.getFile());
        StringBuffer sb   = new StringBuffer();
        StringBuffer head = new StringBuffer();
        head.append("\n");
        head.append(
            "<link rel=\"stylesheet\" type=\"text/css\" href=\"http://visapi-gadgets.googlecode.com/svn/trunk/wordcloud/wc.css\">\n");
        head.append(
            HtmlUtil.importJS(getRepository().fileUrl("/prettify.js")));
        head.append(
            HtmlUtil.cssLink(getRepository().fileUrl("/prettify.css")));


        sb.append(head);
        sb.append("<pre class=\"prettyprint\">\n");

        int cnt = 0;
        for (String line :
                (List<String>) StringUtil.split(contents, "\n", false,
                false)) {
            cnt++;
            line = line.replace("\r", "");
            line = HtmlUtil.entityEncode(line);
            sb.append("<span class=nocode><a "
                      + HtmlUtil.attr("name", "line" + cnt)
                      + "></a><a href=#line" + cnt + ">" + cnt
                      + "</a></span>" + HtmlUtil.space(1) + line + "<br>");
        }
        sb.append("</pre>\n");
        sb.append(HtmlUtil.script("prettyPrint();"));
        Result result = makeLinksResult(request, msg("Pretty Print"), sb,
                                        new State(entry));
        return result;
    }



}
