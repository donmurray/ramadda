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

package org.ramadda.gdata;


import org.w3c.dom.*;


import ucar.unidata.repository.*;
import ucar.unidata.repository.output.CalendarOutputHandler;
import ucar.unidata.repository.metadata.*;
import ucar.unidata.repository.type.*;

import ucar.unidata.util.StringUtil;
import ucar.unidata.util.IOUtil;
import ucar.unidata.util.HtmlUtil;







import java.util.ArrayList;
import java.util.List;
import java.util.Date;
import java.io.File;
import java.net.URL;

import com.google.gdata.client.*;
import  com.google.gdata.data.TextContent;

import com.google.gdata.client.calendar.*;
import com.google.gdata.data.acl.*;
import com.google.gdata.data.calendar.*;
import com.google.gdata.data.extensions.*;
import com.google.gdata.util.*;
import com.google.gdata.util.*;

import com.google.gdata.client.docs.*;
import com.google.gdata.data.MediaContent;
import com.google.gdata.data.acl.*;
import com.google.gdata.data.docs.*;
import com.google.gdata.data.extensions.*;
import com.google.gdata.util.*;


/**
 * Class TypeHandler _more_
 *
 *
 * @author IDV Development Team
 * @version $Revision: 1.3 $
 */
public class CalendarTypeHandler extends GdataTypeHandler {

    public static final String TYPE_CALENDAR = "calendar";
    public static final String TYPE_EVENT = "event";

    /** _more_ */
    private CalendarOutputHandler calendarOutputHandler;

    /**
     * _more_
     *
     * @param repository _more_
     * @param entryNode _more_
     *
     * @throws Exception _more_
     */
    public CalendarTypeHandler(Repository repository, Element entryNode)
            throws Exception {
        super(repository, entryNode);
    }





    private CalendarService getService(Entry entry) throws Exception {
        String userId = getUserId(entry);
        String password = getPassword(entry);
        if (userId == null || password == null) {
            return null;
        }
        CalendarService myService = new CalendarService("exampleCo-exampleApp-1");
        myService.setUserCredentials(userId, password);
        return myService;
    }


    public List<String> getCalendarIds(Request request, Entry entry)
            throws Exception {
        List<String> ids    = entry.getChildIds();
        if(ids!=null) return ids;
        ids = new ArrayList<String>();
        for(Entry calendar: getCalendarEntries(request, entry)) {
            ids.add(calendar.getId());
        }
        entry.setChildIds(ids);
        return ids;
    }


    public List<Entry> getCalendarEntries(Request request, Entry entry)
        throws Exception {
        List<Entry> entries    = new ArrayList<Entry>();
        String userId = getUserId(entry);
        if(userId ==null) return entries;
        URL feedUrl = new URL("https://www.google.com/calendar/feeds/default/allcalendars/full");
        CalendarFeed resultFeed = getService(entry).getFeed(feedUrl, CalendarFeed.class);
        for (int i = 0; i < resultFeed.getEntries().size(); i++) {
            CalendarEntry calendar = resultFeed.getEntries().get(i);
            String entryId = getSynthId(entry, TYPE_CALENDAR, IOUtil.getFileTail(calendar.getId()));
            String title = calendar.getTitle().getPlainText();
            Entry newEntry =  new Entry(entryId, this, true);
            entries.add(newEntry);
            Resource resource = new Resource();
            Date now = new Date();
            newEntry.initEntry(title, "", entry, entry.getUser(),
                            resource, "", now.getTime(),now.getTime(),now.getTime(),now.getTime(),
                            null);
            getEntryManager().cacheEntry(newEntry);
        }
        return entries;
    }


    public List<String> getEventIds(Request request, Entry mainEntry, Entry entry, String calendarId)
            throws Exception {
        List<String> ids    = entry.getChildIds();
        if(ids!=null) return ids;
        ids = new ArrayList<String>();
        for(Entry event: getEventEntries(request, mainEntry, entry, calendarId)) {
            ids.add(event.getId());
        }
        entry.setChildIds(ids);
        return ids;
    }


    public List<Entry> getEventEntries(Request request, Entry mainEntry,Entry entry, String calendarId)
        throws Exception {
        List<Entry> entries    = new ArrayList<Entry>();
        String userId = getUserId(mainEntry);
        if(userId ==null) return entries;
        URL feedUrl = new URL("https://www.google.com/calendar/feeds/" + calendarId +"/private/full");
        //        System.err.println("Feed:" + feedUrl);
        CalendarEventFeed resultFeed =  getService(mainEntry).getFeed(feedUrl, CalendarEventFeed.class); 
        for (int i = 0; i < resultFeed.getEntries().size(); i++) {
            CalendarEventEntry  event= resultFeed.getEntries().get(i);
            String entryId = getSynthId(mainEntry, TYPE_EVENT, calendarId +":" + IOUtil.getFileTail(event.getId()));
            String title = event.getTitle().getPlainText();
            Entry newEntry =  new Entry(entryId, this, false);
            entries.add(newEntry);
            Date from = new Date();
            Date to = new Date();
            Date now = new Date();
            if(event.getTimes().size()>0) {
                com.google.gdata.data.DateTime startTime=event.getTimes().get(0).getStartTime(); 
                com.google.gdata.data.DateTime endTime=event.getTimes().get(0).getEndTime(); 
                from = new Date(startTime.getValue());
                to = new Date(endTime.getValue());
            }
            StringBuffer desc = new StringBuffer();
            if(event.getContent() instanceof TextContent) {
                TextContent content = (TextContent) event.getContent();
                desc.append(content.getContent().getPlainText()); 
                desc.append(HtmlUtil.p());
            }

            boolean didone = false;
            for(EventWho who: event.getParticipants()) {
                if(!didone) {
                    desc.append("Participants:<ul>");
                    didone = true;
                }
                desc.append("<li>");
                desc.append(who.getValueString());
            }
            if(didone) {
                desc.append("</ul>");
            }
            didone = false;
            for(Where where: event.getLocations()) {
                String s = where.getValueString();
                if(s==null || s.length()==0) continue;
                if(!didone) {
                    desc.append("Locations:<ul>");
                    didone = true;
                }
                desc.append("<li>");
                desc.append(s);
            }
            if(didone) {
                desc.append("</ul>");
            }


            Resource resource = new Resource(event.getHtmlLink().getHref());
            newEntry.initEntry(title, desc.toString(), entry, entry.getUser(),
                            resource, "", now.getTime(),now.getTime(),from.getTime(),to.getTime(),
                            null);
            getEntryManager().cacheEntry(newEntry);
        }
        return entries;
    }



    public List<String> getSynthIds(Request request, Entry mainEntry,
                                    Entry parentEntry, String synthId)
            throws Exception {
        if(synthId==null) {
            return getCalendarIds(request, mainEntry);
        }

        List<String> ids    = parentEntry.getChildIds();
        if(ids!=null) return ids;
        ids = new ArrayList<String>();
        List<String> toks = StringUtil.split(synthId,":");
        String type = toks.get(0);
        String calendarId = toks.get(1);
        return getEventIds(request, mainEntry, parentEntry,  calendarId);
    }


    public Entry makeSynthEntry(Request request, Entry mainEntry, String id)
        throws Exception {
        List<String> toks = StringUtil.split(id,":");
        String type = toks.get(0);
        if(type.equals(TYPE_CALENDAR)) {
            for(Entry entry: getCalendarEntries(request, mainEntry)) {
                if(entry.getId().endsWith(id)) {
                    return entry;
                }
            }
            return null;
        }

        String calendarId =  toks.get(1);
        String calendarEntryId = getSynthId(mainEntry, TYPE_CALENDAR, calendarId);
        Entry calendarEntry = getEntryManager().getEntry(request, calendarEntryId);
        String eventId =  getSynthId(mainEntry, TYPE_EVENT, calendarId+":" + toks.get(2));

        //        System.err.println(eventId);
        for(Entry entry: getEventEntries(request, mainEntry, calendarEntry, calendarId)) {
            //            System.err.println("\t" + entry.getId());
            if(entry.getId().equals(eventId)) {
                return entry;
            }
        }
        return null;
    }

    public String getIconUrl(Request request, Entry entry) throws Exception {
        if(entry.getId().indexOf(TYPE_EVENT)>=0) 
            return iconUrl("/icons/calendar_view_day.png");
        return super.getIconUrl(request, entry);
    }

    public Result getHtmlDisplay(Request request, Entry group,
                                 List<Entry> subGroups, List<Entry> entries)
            throws Exception {

        if(request.defined(ARG_OUTPUT)) {
            return null;
        }
        if(!getEntryManager().isSynthEntry(group.getId())) {
            return null;
        }
        if(group.getId().indexOf(TYPE_CALENDAR)<0) {
            return null;
        }

        if (calendarOutputHandler == null) {
            calendarOutputHandler =
                (CalendarOutputHandler) getRepository().getOutputHandler(
                    CalendarOutputHandler.OUTPUT_CALENDAR);
        }
        return calendarOutputHandler.outputGroup(request,
                request.getOutput(), group, subGroups, entries);
    }



    public static void main(String[]args) throws Exception {
        String userId = "jeff.mcwhirter@gmail.com";
        CalendarService myService = new CalendarService("exampleCo-exampleApp-1");
        myService.setUserCredentials(userId, args[0]);


        // Send the request and print the response
        URL feedUrl = new URL("https://www.google.com/calendar/feeds/default/allcalendars/full");
        CalendarFeed resultFeed = myService.getFeed(feedUrl, CalendarFeed.class);
        System.out.println("Your calendars:");
        for (int i = 0; i < resultFeed.getEntries().size(); i++) {
            CalendarEntry entry = resultFeed.getEntries().get(i);
            String id = IOUtil.getFileTail(entry.getId());
            System.out.println("\t" + entry.getTitle().getPlainText() +" " + id);
            System.out.println("\tEvents:");
            URL eventUrl = new URL("https://www.google.com/calendar/feeds/" + id +"/private/full");
            CalendarEventFeed eventFeed =  myService.getFeed(eventUrl, CalendarEventFeed.class); 
            for (int eventIdx = 0; eventIdx < eventFeed.getEntries().size(); eventIdx++) {
                CalendarEventEntry  calendar= eventFeed.getEntries().get(eventIdx);
                System.err.println("\t\t" +IOUtil.getFileTail(calendar.getId())+" " + calendar.getTitle().getPlainText());
            }
        }

    }




}