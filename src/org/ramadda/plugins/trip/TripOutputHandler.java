/*
 * Copyright 2008-2011 Jeff McWhirter/ramadda.org
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

package org.ramadda.plugins.trip;


import org.w3c.dom.*;

import org.ramadda.repository.*;
import org.ramadda.repository.auth.*;
import org.ramadda.repository.output.*;

import ucar.unidata.sql.SqlUtil;
import ucar.unidata.ui.ImageUtils;
import ucar.unidata.util.DateUtil;
import ucar.unidata.util.HtmlUtil;
import ucar.unidata.util.IOUtil;
import ucar.unidata.util.Misc;


import ucar.unidata.util.StringUtil;
import ucar.unidata.util.WmsUtil;
import ucar.unidata.xml.XmlUtil;

import java.io.*;
import java.util.TimeZone;




import java.io.File;


import java.net.*;


import java.text.SimpleDateFormat;

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
public class TripOutputHandler extends OutputHandler {






    /** _more_ */
    public static final OutputType OUTPUT_ITINERARY = new OutputType("Trip Itinerary",
                                                     "trip_itinerary",
                                                     OutputType.TYPE_VIEW,
                                                     "", "/trip/itinerary.png");


    /**
     * _more_
     *
     *
     * @param repository _more_
     * @param element _more_
     * @throws Exception _more_
     */
    public TripOutputHandler(Repository repository, Element element)
            throws Exception {
        super(repository, element);
        addType(OUTPUT_ITINERARY);
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
        if (state.group == null) {
            return;
        }
        if(state.group.getTypeHandler().getType().equals("trip_trip")) {
            links.add(makeLink(request, state.getEntry(), OUTPUT_ITINERARY));
        }
    }


    /**
     * _more_
     *
     * @param request _more_
     * @param outputType _more_
     * @param group _more_
     * @param subGroups _more_
     * @param entries _more_
     *
     * @return _more_
     *
     * @throws Exception _more_
     */
    public Result outputGroup(Request request, OutputType outputType,
                              Entry group, List<Entry> subGroups,
                              List<Entry> entries)
            throws Exception {
        TimeZone  utcTimeZone = TimeZone.getTimeZone("UTC");
        TimeZone  timeZone = TimeZone.getTimeZone(group.getValue(0,"UTC"));
        SimpleDateFormat sdf = new SimpleDateFormat("EEEE MMMMM d, yyyy");
        sdf.setTimeZone(timeZone);
        SimpleDateFormat timeSdf = new SimpleDateFormat("HH:mm Z");
        timeSdf.setTimeZone(timeZone);
        SimpleDateFormat utcTimeSdf = new SimpleDateFormat("HH:mm Z");
        utcTimeSdf.setTimeZone(utcTimeZone);
        boolean differentTZ = !timeZone.equals(utcTimeZone);

        boolean forPrint = request.get("forprint",false);
        TripTypeHandler handler = (TripTypeHandler) group.getTypeHandler();
        StringBuffer sb = new StringBuffer();
        if(forPrint) {
            request.setHtmlTemplateId("empty");
        } else {
            request.put("forprint","true");
            sb.append(HtmlUtil.href(request.getUrl(),HtmlUtil.img(getRepository().iconUrl("/icons/printer.png"))));
            sb.append(" ");
            handler.appendHeader(request, group, sb);
        }
        subGroups.addAll(entries);
        
        sb.append(HtmlUtil.cssLink(getRepository().getUrlBase()
                                   + "/trip/trip.css"));
        subGroups = getEntryManager().sortEntriesOnDate(subGroups, false);
        String currentDate = "";
        for(Entry entry: subGroups) {
            String entryDate = sdf.format(new Date(entry.getStartDate()));
            if(!Misc.equals(currentDate, entryDate)) {
                if(currentDate.length()>0) {
                    sb.append("</div>");
                }
                currentDate = entryDate;
                sb.append(HtmlUtil.div(entryDate,HtmlUtil.cssClass("itinerary-header")));
                sb.append("<div class=itinerary-day>");
                
            }
            String url = getEntryManager().getAjaxLink(request,
                                                       entry, entry.getLabel(), null, true,null,false).toString();

            sb.append(url);
            StringBuffer desc = new StringBuffer(entry.getDescription());
            String type = entry.getTypeHandler().getType();
            desc.append(HtmlUtil.formTable());
            String entryTimezone = entry.getValue(0,null);
            if(type.equals(TripTypeHandler.TYPE_FLIGHT) || type.equals(TripTypeHandler.TYPE_TRAIN)) {
                SimpleDateFormat sdfToUse = timeSdf;
                boolean addUtc = differentTZ;
                if(entryTimezone!=null) {
                    sdfToUse = new SimpleDateFormat("HH:mm Z");
                    sdfToUse.setTimeZone(TimeZone.getTimeZone(entryTimezone));
                    addUtc = true;
                }
                String timeHtml = sdfToUse.format(new Date(entry.getStartDate()));
                if(addUtc) {
                    timeHtml = timeHtml +" (" +utcTimeSdf.format(new Date(entry.getStartDate()))+")";
                }
                desc.append(HtmlUtil.formEntry(msgLabel("Time"), timeHtml));
            } 

            if(type.equals(TripTypeHandler.TYPE_HOTEL)) {
                String address = entry.getValue(1,"");
                String phone = entry.getValue(2,null);
                String email = entry.getValue(3,null);
                String confirmation = entry.getValue(4,null);
              
                String mapUrl = "http://maps.google.com/maps?f=q&source=s_q&hl=en&geocode=&q=" + address.replaceAll("\n"," ");
                address = address.replaceAll("\n","<br>");
                desc.append(HtmlUtil.formEntryTop(msgLabel("Address"), HtmlUtil.italics(address) +" " +
                                                  HtmlUtil.href(mapUrl,"(map)")));
                if(phone!=null) {
                    desc.append(HtmlUtil.formEntry(msgLabel("Phone"), phone));
                }
                if(email!=null) {
                    desc.append(HtmlUtil.formEntry(msgLabel("Email"), email));
                }
                if(confirmation!=null) {
                    desc.append(HtmlUtil.formEntry(msgLabel("Confirmation"), confirmation));
                }
            } 

            desc.append(HtmlUtil.formTableClose());

            if(desc.length()>0) {
                sb.append(HtmlUtil.div(desc.toString(),""));
            }
        }
        if(subGroups.size()>0) {
            sb.append("</div>");
        }

        return new Result("", sb);
    }



}
