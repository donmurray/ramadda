/*
* Copyright 2008-2013 Geode Systems LLC
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

package org.ramadda.geodata.gps;



import org.ramadda.data.process.*;
import org.ramadda.util.Utils;
import org.ramadda.util.HtmlUtils;
import org.ramadda.repository.Entry;
import org.ramadda.repository.Repository;
import org.ramadda.repository.Request;
import org.w3c.dom.*;

import java.util.List;

/**
 */
public class GpsService extends Service {

    public GpsService(Repository repository, Element element)
            throws Exception {
        super(repository, element);
    }


    @Override
    public void addExtraArgs(Request request, ServiceInput input, List<String> args, boolean start) 
        throws Exception {
        if(!start) return;
        List<Entry> entries = input.getEntries();
        if(entries.size()==0) {
            return;
        }
        Entry entry = entries.get(0);
        String antenna = entry.getValue(GpsOutputHandler.IDX_ANTENNA_TYPE,
                                 (String) null);
        double height = entry.getValue(GpsOutputHandler.IDX_ANTENNA_HEIGHT, 0.0);

        System.err.println("ant:" + antenna + " h:" + height);
        if (height != 0) {
            args.add("-O.pe");
            args.add("" + height);
            args.add("0");
            args.add("0");
        }
        if ((antenna != null) && (antenna.length() > 0)
            && !antenna.equalsIgnoreCase(Antenna.NONE)) {
            args.add("-O.at");
            args.add(antenna);
        }
    }



}
