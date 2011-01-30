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

package org.ramadda.repository.monitor;


import org.ramadda.repository.*;
import org.ramadda.repository.auth.*;

import ucar.unidata.util.IOUtil;
import ucar.unidata.util.Misc;
import ucar.unidata.util.StringUtil;

import java.io.File;

import java.util.ArrayList;
import java.util.Enumeration;
import java.util.Hashtable;

import java.util.List;


/**
 * Class FileInfo _more_
 *
 *
 * @author IDV Development Team
 * @version $Revision: 1.30 $
 */
public class SynchronousEntryMonitor extends EntryMonitor {

    /** _more_ */
    private Entry entry;


    /**
     * _more_
     */
    public SynchronousEntryMonitor() {}


    /**
     * _more_
     *
     * @param repository _more_
     * @param request _more_
     */
    public SynchronousEntryMonitor(Repository repository, Request request) {
        super(repository, request.getUser(), "Synchronous Search", false);
        Hashtable properties = request.getDefinedProperties();
        for (Enumeration keys = properties.keys(); keys.hasMoreElements(); ) {
            String arg   = (String) keys.nextElement();
            String value = (String) properties.get(arg);
            addFilter(new Filter(arg, value));
        }
    }


    /**
     * _more_
     *
     * @return _more_
     */
    public String getActionName() {
        return "Synchronous Action";
    }

    /**
     * _more_
     *
     * @param entry _more_
     */
    protected void entryMatched(Entry entry) {
        this.entry = entry;
        synchronized (this) {
            this.notify();
        }
    }


    /**
     * _more_
     *
     * @return _more_
     */
    public Entry getEntry() {
        return entry;
    }

}
