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

package org.ramadda.geodata.mb;



import org.ramadda.data.process.Service;
import org.ramadda.data.process.ServiceOutput;
import org.ramadda.repository.*;
import org.ramadda.repository.metadata.Metadata;
import org.ramadda.repository.type.*;
import org.ramadda.util.Utils;



import org.w3c.dom.*;

import ucar.nc2.units.DateUnit;

import ucar.unidata.util.IOUtil;
import ucar.unidata.util.Misc;
import ucar.unidata.util.StringUtil;
import ucar.unidata.xml.XmlUtil;

import java.io.File;


import java.util.Date;
import java.util.List;


/**
 *
 *
 * @author Jeff McWhirter
 */
public class MbTypeHandler extends GenericTypeHandler {

    /**
     * _more_
     *
     * @param repository _more_
     * @param node _more_
     * @throws Exception On badness
     */
    public MbTypeHandler(Repository repository, Element node)
            throws Exception {
        super(repository, node);
    }


    /**
     * _more_
     *
     *
     * @param request _more_
     * @param entry _more_
     *
     * @throws Exception On badness
     */
    @Override
    public void initializeNewEntry(Request request, Entry entry)
            throws Exception {
        Object[] values = getEntryValues(entry);
        String suffix =
            IOUtil.getFileExtension(entry.getResource().getPath());
        suffix    = suffix.replace(".mb", "");
        values[0] = suffix;
        super.initializeNewEntry(request, entry);
    }



    /**
     * _more_
     *
     *
     * @param request _more_
     * @param entry _more_
     * @param service _more_
     * @param output _more_
     *
     * @throws Exception _more_
     */
    @Override
    public void handleServiceResults(Request request, Entry entry,
                                     Service service, ServiceOutput output)
            throws Exception {
        super.handleServiceResults(request, entry, service, output);

        List<Entry> entries = output.getEntries();
        if (entries.size() == 0) {
            return;
        }
        
        String file = entries.get(0).getFile().toString();
        if(!file.endsWith(".xml")) {
            return;
        }

        String xml = IOUtil.readContents(file,
                                         getClass(), "");
        Element root = null;

        //        System.err.println(xml);
        try {
            root = XmlUtil.getRoot(xml);
        } catch (Exception exc) {
            xml  = Utils.removeNonAscii(xml);
            root = XmlUtil.getRoot(xml);
        }
        Element limits   = XmlUtil.findChild(root, MbUtil.TAG_LIMITS);

        Element fileInfo = XmlUtil.findChild(root, MbUtil.TAG_FILE_INFO);
        String desc = XmlUtil.getGrandChildText(fileInfo,
                          MbUtil.TAG_INFORMAL_DESCRIPTION);

        desc = Utils.removeNonAscii(desc);
        entry.setDescription(desc);

        for (String attr :
                StringUtil.split(XmlUtil.getGrandChildText(fileInfo,
                    MbUtil.TAG_ATTRIBUTES), ",", true, true)) {
            entry.addMetadata(new Metadata(getRepository().getGUID(),
                                           entry.getId(), "enum_tag", false,
                                           attr, "", "", "", ""));

        }

        entry.setNorth(Double.parseDouble(XmlUtil.getGrandChildText(limits,
                MbUtil.TAG_MAXIMUM_LATITUDE)));
        entry.setSouth(Double.parseDouble(XmlUtil.getGrandChildText(limits,
                MbUtil.TAG_MINIMUM_LATITUDE)));
        entry.setEast(Double.parseDouble(XmlUtil.getGrandChildText(limits,
                MbUtil.TAG_MAXIMUM_LONGITUDE)));
        entry.setWest(Double.parseDouble(XmlUtil.getGrandChildText(limits,
                MbUtil.TAG_MINIMUM_LONGITUDE)));


        Element startOfData = XmlUtil.findChild(root,
                                  MbUtil.TAG_START_OF_DATA);
        Element endOfData = XmlUtil.findChild(root, MbUtil.TAG_END_OF_DATA);


        Date startDate =
            DateUnit.getStandardOrISO(XmlUtil.getGrandChildText(startOfData,
                MbUtil.TAG_TIME_ISO));
        Date endDate =
            DateUnit.getStandardOrISO(XmlUtil.getGrandChildText(endOfData,
                MbUtil.TAG_TIME_ISO));
        entry.setStartDate(startDate.getTime());
        entry.setEndDate(endDate.getTime());


        int numberOfRecords =
            Integer.parseInt(
                XmlUtil.getGrandChildText(
                    XmlUtil.findChild(root, MbUtil.TAG_DATA_TOTALS),
                    MbUtil.TAG_NUMBER_OF_RECORDS));

        Object[] values = getEntryValues(entry);
        values[1] = new Integer(numberOfRecords);

        Element navTotals = XmlUtil.findChild(root,
                                MbUtil.TAG_TNAVIGATION_TOTALS);
        values[2] = new Double(XmlUtil.getGrandChildText(navTotals,
                MbUtil.TAG_TOTAL_TRACK_LENGTH_KM));

    }





}
