/*
 * Copyright (c) 2008-2015 Geode Systems LLC
 * This Software is licensed under the Geode Systems RAMADDA License available in the source distribution in the file 
 * ramadda_license.txt. The above copyright notice shall be included in all copies or substantial portions of the Software.
 */

package org.ramadda.service;


import org.ramadda.repository.*;
import org.ramadda.repository.metadata.*;
import org.ramadda.repository.output.HtmlOutputHandler;
import org.ramadda.repository.output.ServiceOutputHandler;
import org.ramadda.repository.type.*;

import org.ramadda.util.FormInfo;
import org.ramadda.util.HtmlUtils;
import org.ramadda.util.Utils;


import org.w3c.dom.*;

import ucar.unidata.util.IOUtil;

import ucar.unidata.util.Misc;
import ucar.unidata.util.StringUtil;
import ucar.unidata.util.TwoFacedObject;


import ucar.unidata.xml.XmlUtil;

import java.util.ArrayList;

import java.util.Date;
import java.util.Hashtable;
import java.util.List;


/**
 *
 *
 */
public class ServiceLinkTypeHandler extends ServiceTypeHandler {

    /** _more_ */
    public static final int IDX_LINK_ID = IDX_LAST + 1;

    /**
     * _more_
     *
     * @param repository _more_
     * @param entryNode _more_
     *
     * @throws Exception _more_
     */
    public ServiceLinkTypeHandler(Repository repository, Element entryNode)
            throws Exception {
        super(repository, entryNode);
    }





    /**
     * _more_
     *
     * @param request _more_
     * @param column _more_
     * @param formBuffer _more_
     * @param entry _more_
     * @param values _more_
     * @param state _more_
     * @param formInfo _more_
     *
     * @throws Exception _more_
     */
    @Override
    public void addColumnToEntryForm(Request request, Column column,
                                     Appendable formBuffer, Entry entry,
                                     Object[] values, Hashtable state,
                                     FormInfo formInfo)
            throws Exception {
        if ( !column.getName().equals("service_id")) {
            super.addColumnToEntryForm(request, column, formBuffer, entry,
                                       values, state, formInfo);

            return;
        }
        List<Service> services =
            getRepository().getJobManager().getServices();
        List<HtmlUtils.Selector> items = new ArrayList<HtmlUtils.Selector>();
        for (Service service : services) {
            items.add(new HtmlUtils.Selector(service.getLabel(),
                                             service.getId(),
                                             iconUrl(service.getIcon())));
        }

        formBuffer.append(HtmlUtils.formEntry(msgLabel("Service"),
                HtmlUtils.select(column.getEditArg(), items,
                                 column.getString(values))));
    }

    /**
     * _more_
     *
     *
     * @param request _more_
     * @param entry _more_
     *
     * @throws Exception _more_
     */
    @Override
    public void initializeNewEntry(Request request, Entry entry)
            throws Exception {
        super.initializeNewEntry(request, entry);

        if ( !Utils.stringDefined(entry.getName())) {
            Service service = getService(getRepository().getTmpRequest(),
                                         entry);
            if (service != null) {
                if (service.getLink() != null) {
                    entry.setName(service.getLink().getLabel());
                } else {
                    entry.setName(service.getLabel());
                }
            }
        }
    }



    /**
     * _more_
     *
     * @param request _more_
     * @param entry _more_
     * @param column _more_
     * @param tmpSb _more_
     * @param values _more_
     *
     * @throws Exception _more_
     */
    public void formatColumnHtmlValue(Request request, Entry entry,
                                      Column column, Appendable tmpSb,
                                      Object[] values)
            throws Exception {
        if ( !column.getName().equals("service_id")) {
            super.formatColumnHtmlValue(request, entry, column, tmpSb,
                                        values);

            return;
        }
        Service service = getService(request, entry);
        if (service == null) {
            return;
        }
        tmpSb.append(service.getLabel());
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
    @Override
    public Service getService(Request request, Entry entry) throws Exception {
        Service service = new Service(getRepository(), entry.getId(),
                                      entry.getName());
        service.setLinkId(entry.getValue(IDX_LINK_ID, ""));

        if (Utils.stringDefined(entry.getLabel())) {
            service.setLabel(entry.getLabel());
        }

        return service;
    }


}
