/**
* Copyright (c) 2008-2015 Geode Systems LLC
* This Software is licensed under the Geode Systems RAMADDA License available in the source distribution in the file 
* ramadda_license.txt. The above copyright notice shall be included in all copies or substantial portions of the Software.
*/

package org.ramadda.plugins.phone;


import org.ramadda.repository.*;
import org.ramadda.repository.auth.*;
import org.ramadda.repository.harvester.*;
import org.ramadda.repository.metadata.*;
import org.ramadda.repository.type.*;

import org.ramadda.util.HtmlUtils;
import org.ramadda.util.TTLCache;


import org.w3c.dom.*;

import ucar.unidata.util.IOUtil;

import ucar.unidata.util.Misc;
import ucar.unidata.util.StringUtil;

import ucar.unidata.xml.XmlUtil;

import java.io.*;



import java.net.*;



import java.util.ArrayList;
import java.util.Date;
import java.util.Hashtable;
import java.util.List;



/**
 */
public class LocationPhoneHarvester extends PhoneHarvester {

    /**
     * _more_
     *
     * @param repository _more_
     * @param id _more_
     *
     * @throws Exception _more_
     */
    public LocationPhoneHarvester(Repository repository, String id)
            throws Exception {
        super(repository, id);
    }


    /**
     * _more_
     *
     * @param repository _more_
     * @param node _more_
     *
     * @throws Exception _more_
     */
    public LocationPhoneHarvester(Repository repository, Element node)
            throws Exception {
        super(repository, node);
    }




    /**
     * _more_
     *
     * @param request _more_
     * @param info _more_
     * @param msg _more_
     *
     * @return _more_
     *
     * @throws Exception _more_
     */
    public boolean handleMessageInner(Request request, PhoneInfo info,
                                      StringBuffer msg)
            throws Exception {

        System.err.println("PhoneHarvester: handleMessage: from:"
                           + info.getFromPhone() + " to:" + info.getToPhone()
                           + " " + info.getMessage());

        Entry        parent  = getBaseGroup();
        String       message = info.getMessage().trim();
        List<String> lines   = StringUtil.split(message, "\n", true, true);

        if (message.equals("help") || message.equals("?")
                || (lines.size() == 0)) {

            msg.append(
                "usage:\n<address>\ninches of water (0 for none)\nhow many days\nany other tags, e.g.\nsewage, etc");

            return true;
        }


        String      type        = "notes_note";


        TypeHandler typeHandler = getRepository().getTypeHandler(type);
        Entry       entry =
            typeHandler.createEntry(getRepository().getGUID());
        Date        date        = new Date();
        Object[]    values      =
            typeHandler.makeEntryValues(new Hashtable());
        String      desc        = "";
        String      name        = "";
        values[0] = info.getFromPhone();
        values[1] = info.getToPhone();
        entry.initEntry(name, desc, parent, getUser(), new Resource(), "",
                        date.getTime(), date.getTime(), date.getTime(),
                        date.getTime(), values);


        double[] location = org.ramadda.util.GeoUtils.getLocationFromAddress(
                                info.getFromZip());
        if (location != null) {
            entry.setLocation(location[0], location[1], 0);
        }

        List<Entry> entries = (List<Entry>) Misc.newList(entry);
        getEntryManager().addNewEntries(getRequest(), entries);
        msg.append("New entry:\n" + entry);

        return true;
    }



}
