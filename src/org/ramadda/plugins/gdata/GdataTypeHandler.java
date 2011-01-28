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

package org.ramadda.plugins.gdata;


import com.google.gdata.client.GoogleService;

import com.google.gdata.data.BaseEntry;
import com.google.gdata.data.Category;

import com.google.gdata.data.Person;


import org.w3c.dom.*;


import ucar.unidata.repository.*;
import ucar.unidata.repository.metadata.Metadata;
import ucar.unidata.repository.type.*;

import java.io.File;

import java.net.URL;

import java.util.Hashtable;
import java.util.List;






import java.util.Set;


/**
 * Class TypeHandler _more_
 *
 *
 * @author IDV Development Team
 * @version $Revision: 1.3 $
 */
public class GdataTypeHandler extends GdataBaseTypeHandler {

    /** _more_          */
    private Hashtable<String, GoogleService> serviceMap =
        new Hashtable<String, GoogleService>();

    /**
     * _more_
     *
     * @param repository _more_
     * @param entryNode _more_
     *
     * @throws Exception _more_
     */
    public GdataTypeHandler(Repository repository, Element entryNode)
            throws Exception {
        super(repository, entryNode);
    }


    /**
     * _more_
     *
     * @param entry _more_
     *
     * @return _more_
     *
     * @throws Exception _more_
     */
    protected GoogleService getService(Entry entry) throws Exception {
        String userId   = getUserId(entry);
        String password = getPassword(entry);
        if ((userId == null) || (password == null)) {
            return null;
        }
        GoogleService service = serviceMap.get(userId);
        if (service != null) {
            return service;
        }
        service = doMakeService(userId, password);
        if (service == null) {
            return null;
        }
        serviceMap.put(userId, service);
        return service;
    }


    /**
     * _more_
     *
     * @param userId _more_
     * @param password _more_
     *
     * @return _more_
     *
     * @throws Exception _more_
     */
    protected GoogleService doMakeService(String userId, String password)
            throws Exception {
        return null;
    }

    /**
     * _more_
     *
     * @param entry _more_
     *
     * @return _more_
     */
    public String getUserId(Entry entry) {
        return entry.getValue(0, (String) null);
    }

    /**
     * _more_
     *
     * @param entry _more_
     *
     * @return _more_
     */
    public String getPassword(Entry entry) {
        return entry.getValue(1, (String) null);
    }

    /**
     * _more_
     *
     * @param parentEntry _more_
     * @param type _more_
     * @param subId _more_
     *
     * @return _more_
     */
    public String getSynthId(Entry parentEntry, String type, String subId) {
        return Repository.ID_PREFIX_SYNTH + parentEntry.getId() + ":" + type
               + ":" + subId;
    }

    /**
     * _more_
     *
     * @param parentEntry _more_
     * @param subId _more_
     *
     * @return _more_
     */
    public String getSynthId(Entry parentEntry, String subId) {
        return Repository.ID_PREFIX_SYNTH + parentEntry.getId() + ":" + subId;
    }



}

