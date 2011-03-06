/*
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
 * 
 */

package org.ramadda.repository.search;
import org.ramadda.repository.*;


import org.w3c.dom.*;

import org.ramadda.repository.*;
import org.ramadda.repository.metadata.*;
import org.ramadda.repository.type.*;
import org.ramadda.repository.map.*;
import org.ramadda.repository.output.*;

import java.awt.geom.Rectangle2D;
import java.util.ArrayList;
import java.util.Hashtable;
import java.util.List;
import ucar.unidata.util.HtmlUtil;
import ucar.unidata.util.StringUtil;


/**
 *
 *
 * @author jeff mcwhirter 
 * @version $Revision: 1.3 $
 */
public class SpecialSearch extends RepositoryManager implements RequestHandler  {

    private  RequestUrl URL_SEARCH;

    public static final String ARG_SEARCH_SUBMIT = "search.submit";

    private String theType;

    private String searchUrl;

    private String label;

    private List<String> metadataTypes = new ArrayList<String>();



    /**
     * _more_
     *
     * @param repository _more_
     * @param node _more_
     *
     * @throws Exception _more_
     */
    public SpecialSearch(Repository repository, Element node, Hashtable props)
            throws Exception {
        super(repository);
        String types = (String)props.get("metadatatypes");
        if(types!=null) {
            for(String type: StringUtil.split(types, ",",true, true)) {
                metadataTypes.add(type);
            }
        }
        searchUrl = (String)props.get("searchurl");
        label = (String)props.get("label");
        theType = (String)props.get("type");
    }





    /**
     * _more_
     *
     * @param request _more_
     *
     * @return _more_
     *
     * @throws Exception _more_
     */
    public Result processCapabilitiesRequest(Request request) throws Exception {
        request.put(ARG_OUTPUT, "atom");
        request.put(ARG_TYPE, theType);
        Result result =  getRepository().getSearchManager().processEntrySearch(request);
        return result;
    }

    public void makeHeader(Request request, StringBuffer sb) throws Exception {
    }


    public Result processSearchRequest(Request request) throws Exception {
        int tabWidth = 700;
        int tabHeight = 350;

        request.put(ARG_TYPE, theType);
        List[] groupAndEntries = getRepository().getEntryManager().getEntries(request);
        List<Entry> entries = (List<Entry>) groupAndEntries[0];

        if(request.exists("timelinexml")) {
            Entry group  = getRepository().getEntryManager().getDummyGroup();
            return getRepository().getHtmlOutputHandler().outputTimelineXml(request, group, entries);
        }

        StringBuffer sb = new StringBuffer();
        StringBuffer js = new StringBuffer();
        if(URL_SEARCH==null) {
            URL_SEARCH = new RequestUrl(this, searchUrl);
        }

        makeHeader(request, sb);
        sb.append(request.form(URL_SEARCH,  HtmlUtil.attr(HtmlUtil.ATTR_NAME,"apisearchform")));

        String[]nwse = new String[] {
            request.getString(ARG_AREA_NORTH, ""),
            request.getString(ARG_AREA_WEST, ""),
            request.getString(ARG_AREA_SOUTH, ""),
            request.getString(ARG_AREA_EAST, ""),
        };


        MapInfo map = getRepository().getMapManager().createMap(request,  tabWidth,tabHeight, true); 
        MapOutputHandler mapOutputHandler = (MapOutputHandler) getRepository().getOutputHandler(MapOutputHandler.OUTPUT_MAP);
        if(mapOutputHandler!=null) {
            mapOutputHandler.addToMap(request, map, entries, null,true);
        }
        Rectangle2D.Double bounds = getEntryManager().getBounds(entries);

        if(bounds.getWidth()>180 && false) {
            double cx = bounds.getX()+bounds.getWidth()/2;
            double cy = bounds.getY()+bounds.getHeight()/2;
            int f = 120;
            bounds = new Rectangle2D.Double(cx-f, cy-f/2, f*2,f);
        }
        map.centerOn(bounds);
        map.addJS(map.getVariableName()+".initMap(true);\n");
        if(request.defined(ARG_AREA_NORTH) &&
           request.defined(ARG_AREA_WEST) &&
           request.defined(ARG_AREA_SOUTH) &&
           request.defined(ARG_AREA_EAST)) {
            map.addJS(HtmlUtil.call(map.getVariableName()+".setSelectionBox",
                                    request.get(ARG_AREA_NORTH, 0.0) +"," +
                                    request.get(ARG_AREA_WEST, 0.0) +"," +
                                    request.get(ARG_AREA_SOUTH, 0.0) +"," +
                                    request.get(ARG_AREA_EAST, 0.0)));

        }



        String initParams = HtmlUtil.squote(ARG_AREA) + "," + true +"," + "0";
        map.addJS(map.getVariableName()+".setSelection(" + initParams+ ");\n");
        map.centerOn(bounds);






        StringBuffer formSB  = new StringBuffer();
        formSB.append(HtmlUtil.br());
        formSB.append(HtmlUtil.formTable());
        formSB.append(HtmlUtil.formEntryTop(msgLabel("Text"),
                                            HtmlUtil.input(ARG_TEXT, request.getString(ARG_TEXT,""),
                                                       HtmlUtil.SIZE_20
                                                       + " autofocus ")));

        String clearLink =   map.getSelectorClearLink(repository.msg("Clear"));
        String searchType = TypeHandler.getSpatialSearchTypeWidget(request);
        String widget = map.getSelectorWidget(ARG_AREA, nwse);
        formSB.append(HtmlUtil.formEntry(msgLabel("Location"), HtmlUtil.table(new Object[]{widget,clearLink})));
        formSB.append(HtmlUtil.formEntry("", searchType));

        for(String type: metadataTypes) {
            MetadataType metadataType = getRepository().getMetadataManager().findType(type);
            if(metadataType!=null) {
                metadataType.getHandler().addToSearchForm(request, formSB, metadataType);
            }
        }

        formSB.append(HtmlUtil.formEntry("", HtmlUtil.submit(msg("Search"), ARG_SEARCH_SUBMIT)));
        formSB.append(HtmlUtil.formTableClose());
        sb.append("<table border=0 cellpadding=0 cellspacing=0><tr valign=top>");

        List<String> tabContents = new ArrayList<String>();
        List<String> tabTitles = new ArrayList<String>();
        StringBuffer timelineSB = new StringBuffer();
        String head = getRepository().getHtmlOutputHandler().makeTimeline(request, entries, timelineSB,"width:" + tabWidth+"px; height: " + tabHeight+"px;");
       

        StringBuffer mapSB =new StringBuffer(msg("Shift-drag to select region"));
        mapSB.append(map.getHtml());
        tabContents.add(mapSB.toString());
        tabTitles.add(msg("Map"));

        tabContents.add(timelineSB.toString());
        tabTitles.add(msg("Timeline"));
        String tabs =  OutputHandler.makeTabs(tabTitles, tabContents, true, "tab_content");
        sb.append(HtmlUtil.col(tabs));
        sb.append(HtmlUtil.col(formSB.toString()));
        sb.append("</table>");
        sb.append(HtmlUtil.formClose());
        sb.append(HtmlUtil.script(js.toString()));
        if(entries.size()==0) {
            sb.append(getRepository().showDialogNote("No entries found"));
        }

        Result tmpResult =
            getRepository().getHtmlOutputHandler().outputGroup(request,
                                                               HtmlOutputHandler.OUTPUT_HTML,
                                                               getRepository().getEntryManager().getDummyGroup(),
                                                               entries, new ArrayList<Entry>());

        sb.append(new String(tmpResult.getContent()));
        for(Entry entry: entries) {
            
        }

        Result result =  new Result("Search", sb);
        result.putProperty(PROP_HTML_HEAD, head);

        return getRepository().getEntryManager().addEntryHeader(request, getRepository().getEntryManager().getTopGroup(), result);
    }


}
