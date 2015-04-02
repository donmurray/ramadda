/**
* Copyright (c) 2008-2015 Geode Systems LLC
* This Software is licensed under the Geode Systems RAMADDA License available in the source distribution in the file 
* ramadda_license.txt. The above copyright notice shall be included in all copies or substantial portions of the Software.
*/

package org.ramadda.plugins.trip;


import org.ramadda.repository.*;
import org.ramadda.repository.auth.*;
import org.ramadda.repository.metadata.*;
import org.ramadda.repository.output.*;
import org.ramadda.repository.type.*;


import org.ramadda.sql.Clause;


import org.ramadda.sql.SqlUtil;
import org.ramadda.sql.SqlUtil;

import org.ramadda.util.HtmlUtils;


import org.w3c.dom.*;

import ucar.unidata.util.DateUtil;
import ucar.unidata.util.HttpServer;
import ucar.unidata.util.IOUtil;
import ucar.unidata.util.LogUtil;
import ucar.unidata.util.Misc;
import ucar.unidata.util.StringUtil;
import ucar.unidata.util.WikiUtil;
import ucar.unidata.xml.XmlUtil;

import java.sql.PreparedStatement;

import java.sql.ResultSet;
import java.sql.Statement;


import java.util.ArrayList;
import java.util.Date;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.List;
import java.util.Properties;


/**
 *
 *
 */
public class TripTypeHandler extends ExtensibleGroupTypeHandler {

    /** _more_ */
    private CalendarOutputHandler calendarOutputHandler;

    /** _more_ */
    public static final String TYPE_HOTEL = "trip_hotel";

    /** _more_ */
    public static final String TYPE_FLIGHT = "trip_flight";

    /** _more_ */
    public static final String TYPE_CAR = "trip_car";

    /** _more_ */
    public static final String TYPE_TRAIN = "trip_train";

    /** _more_ */
    public static final String TYPE_EVENT = "trip_event";


    /** _more_ */
    private static String[] types = { TYPE_HOTEL, TYPE_CAR, TYPE_FLIGHT,
                                      TYPE_TRAIN, TYPE_EVENT, };

    /** _more_ */
    private static String[] names = { "New Lodging", "New Car Rental",
                                      "New Flight", "New Train",
                                      "New Event" };

    /** _more_ */
    private static String[] icons = { "/trip/hotel.png", "/trip/car.gif",
                                      "/trip/plane.png", "/trip/train.gif",
                                      "/trip/event.png" };


    /**
     * _more_
     *
     * @param repository _more_
     * @param entryNode _more_
     *
     * @throws Exception _more_
     */
    public TripTypeHandler(Repository repository, Element entryNode)
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
     *
     * @return _more_
     *
     * @throws Exception _more_
     */
    public Result getHtmlDisplay(Request request, Entry group,
                                 List<Entry> subGroups, List<Entry> entries)
            throws Exception {
        if ( !isDefaultHtmlOutput(request)) {
            return null;
        }
        if (calendarOutputHandler == null) {
            calendarOutputHandler =
                (CalendarOutputHandler) getRepository().getOutputHandler(
                    CalendarOutputHandler.OUTPUT_CALENDAR);
        }

        StringBuffer sb = new StringBuffer();
        appendHeader(request, group, sb);
        sb.append(group.getDescription());

        subGroups.addAll(entries);
        List<Entry> eventEntries = new ArrayList<Entry>();
        for (Entry entry : subGroups) {
            if (entry.getTypeHandler() instanceof TripItemHandler) {
                eventEntries.add(entry);
            }
        }

        return calendarOutputHandler.outputCalendar(request, group,
                eventEntries, sb);
    }


    /**
     * _more_
     *
     * @param request _more_
     * @param group _more_
     * @param sb _more_
     *
     * @throws Exception _more_
     */
    public void appendHeader(Request request, Entry group, StringBuffer sb)
            throws Exception {
        boolean canAdd = getAccessManager().canDoAction(request, group,
                             Permission.ACTION_NEW);

        if (canAdd) {
            sb.append("<b>New:</b> ");
            for (int i = 0; i < types.length; i++) {
                if (i > 0) {
                    sb.append("&nbsp;|&nbsp;");
                }
                sb.append(
                    HtmlUtils.href(
                        HtmlUtils.url(
                            request.entryUrl(
                                getRepository().URL_ENTRY_FORM, group,
                                ARG_GROUP), ARG_TYPE,
                                            types[i]), HtmlUtils.img(
                                                getRepository().iconUrl(
                                                    icons[i]), msg(
                                                        names[i]))));
            }

            sb.append("<p>");
        }
    }

}
