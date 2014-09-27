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

package org.ramadda.repository.output;


import org.ramadda.data.process.OutputDefinition;
import org.ramadda.data.process.Service;
import org.ramadda.data.process.ServiceInput;
import org.ramadda.data.process.ServiceOutput;


import org.ramadda.repository.*;
import org.ramadda.repository.job.JobManager;
import org.ramadda.repository.type.*;

import org.ramadda.util.HtmlUtils;
import org.ramadda.util.Utils;

import org.w3c.dom.*;

import ucar.unidata.util.IOUtil;

import ucar.unidata.util.StringUtil;
import ucar.unidata.xml.XmlUtil;

import java.io.*;
import java.io.File;


import java.util.ArrayList;
import java.util.Date;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.List;


/**
 *
 * @author RAMADDA Development Team
 */
public class ServiceOutputHandler extends OutputHandler {

    /** _more_ */
    public static final String ARG_ASYNCH = "asynch";

    /** _more_ */
    public static final String ARG_SHOWCOMMAND = "showcommand";

    /** _more_ */
    public static final String ARG_GOTOPRODUCTS = "gotoproducts";



    /** _more_          */
    public static final String ARG_WRITEWORKFLOW = "writeworkflow";

    /** _more_ */
    public static final String ATTR_ICON = "icon";

    /** _more_ */
    public static final String ATTR_LABEL = "label";


    /** _more_ */
    private OutputType outputType;

    /** _more_ */
    private Service service;

    /**
     * _more_
     *
     * @param repository _more_
     * @param element _more_
     * @throws Exception _more_
     */
    public ServiceOutputHandler(Repository repository, Element element)
            throws Exception {
        super(repository, element);
        init(element);
    }


    /**
     * _more_
     *
     * @param repository _more_
     * @param service _more_
     *
     * @throws Exception _more_
     */
    public ServiceOutputHandler(Repository repository, Service service)
            throws Exception {
        super(repository, "");
        this.service = service;
    }


    /**
     * _more_
     *
     * @param element _more_
     *
     * @throws Exception _more_
     */
    private void init(Element element) throws Exception {
        String serviceId = XmlUtil.getAttribute(element, "serviceId",
                               (String) null);
        if (serviceId != null) {
            service = getRepository().getJobManager().getService(serviceId);
            if (service == null) {
                throw new IllegalStateException(
                    "ServiceOutputHandler: could not find service:"
                    + serviceId);
            }
        }


        if (service == null) {
            NodeList children = XmlUtil.getElements(element,
                                    Service.TAG_SERVICE);
            Element serviceNode = element;
            if (children.getLength() > 0) {
                serviceNode = (Element) children.item(0);
            }
            service = getRepository().makeService(serviceNode, true);
        }



        outputType = new OutputType(
            XmlUtil.getAttribute(element, ATTR_LABEL, service.getLabel()),
            XmlUtil.getAttribute(element, ATTR_ID, service.getId()),
            OutputType.TYPE_OTHER | OutputType.TYPE_IMPORTANT, "",
            XmlUtil.getAttribute(element, ATTR_ICON, service.getIcon()));
        addType(outputType);


    }

    /**
     * _more_
     *
     * @return _more_
     */
    public boolean isEnabled() {
        return service.isEnabled();
    }

    /**
     * _more_
     *
     * @return _more_
     */
    public Service getService() {
        return service;
    }

    /**
     * _more_
     *
     * @param request _more_
     * @param state _more_
     * @param links _more_
     *
     * @throws Exception _more_
     */
    public void getEntryLinks(Request request, State state, List<Link> links)
            throws Exception {
        if ( !isEnabled()) {
            return;
        }
        if (state.getEntry() != null) {
            if (service.isApplicable(state.getEntry())) {
                links.add(makeLink(request, state.getEntry(), outputType
                // ,    "/" + IOUtil.stripExtension(state.getEntry().getName())+ ".rss"
                ));
            }
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
        return null;
    }







    /**
     * _more_
     *
     * @param request _more_
     * @param outputType _more_
     * @param entry _more_
     *
     * @return _more_
     *
     * @throws Exception _more_
     */
    public Result outputEntry(final Request request, OutputType outputType,
                              final Entry entry)
            throws Exception {

        if ( !request.defined(ARG_EXECUTE)
                && !request.defined(ARG_SHOWCOMMAND)) {
            StringBuffer sb = new StringBuffer();
            makeForm(request, service, entry, entry, outputType, sb);

            return new Result(outputType.getLabel(), sb);
        }

        return evaluateService(request, outputType, entry, entry, service);
    }



    private void writeProcessEntryXml(Request request, Service service, File processDir, String desc)
        throws Exception {
        StringBuffer xml  = new StringBuffer();
        if (desc == null) {
            desc = "";
        }
        xml.append(
            XmlUtil.tag(
                "entry",
                XmlUtil.attrs("type", "group", "name", "Processing Results"),
                XmlUtil.tag("description", "", XmlUtil.getCdata(desc))));

        IOUtil.writeFile(new File(IOUtil.joinDir(processDir,
                ".this.ramadda.xml")), xml.toString());
    }


    /**
     * _more_
     *
     * @param request _more_
     * @param outputType _more_
     * @param baseEntry _more_
     * @param entry _more_
     * @param service _more_
     *
     * @return _more_
     *
     * @throws Exception _more_
     */
    public Result evaluateService(final Request request,
                                  OutputType outputType,
                                  final Entry baseEntry, final Entry entry,
                                  final Service service)
            throws Exception {

        StringBuffer       sb           = new StringBuffer();
        File               workDir = getStorageManager().createProcessDir();

        final ServiceInput serviceInput = (entry != null)
                                          ? new ServiceInput(workDir, entry)
                                          : new ServiceInput(workDir);
        serviceInput.setPublish(doingPublish(request));
        serviceInput.setForDisplay(request.exists(ARG_SHOWCOMMAND));


        boolean asynchronous = request.get(ARG_ASYNCH, false);

        if (asynchronous) {
            ActionManager.Action action = new ActionManager.Action() {
                public void run(Object actionId) throws Exception {
                    ServiceOutput output = null;
                    try {
                        //TODO:
                        //    public ServiceOutput evaluate(Request request,  ServiceInput input)
                        //                        output = service.evaluate(request, entry,
                        output = service.evaluate(request, serviceInput);
                        if ( !output.isOk()) {
                            getActionManager().setContinueHtml(
                                actionId,
                                getPageHandler().showDialogError(
                                    "An error has occurred:<pre>"
                                    + output.getResults() + "</pre>"));

                            return;

                        }
                    } catch (Exception exc) {
                        getActionManager().setContinueHtml(
                            actionId,
                            getPageHandler().showDialogError(
                                "An error has occurred:<pre>" + exc
                                + "</pre>"));

                        return;
                    }
                    writeProcessEntryXml(request,  service, serviceInput.getProcessDir(), service.getProcessDescription());

                    String url =
                        getStorageManager().getProcessDirEntryUrl(request,
                                                                  serviceInput.getProcessDir());
                    if (serviceInput.getPublish()
                            && (output.getEntries().size() > 0)) {
                        url = request.entryUrl(
                            getRepository().URL_ENTRY_SHOW,
                            output.getEntries().get(0));
                    }
                    getActionManager().setContinueHtml(actionId,
                            HtmlUtils.href(url, msg("Continue")));
                }
            };

            return getActionManager().doAction(request, action,
                    outputType.getLabel(), "");

        }
        ServiceOutput output = service.evaluate(request, serviceInput);

        if ( !output.isOk()) {
            sb.append(
                getPageHandler().showDialogError(
                    "An error has occurred:<pre>" + output.getResults()
                    + "</pre>"));
            makeForm(request, service, baseEntry, entry, outputType, sb);

            return new Result(outputType.getLabel(), sb);
        }

        if (request.get(ARG_WRITEWORKFLOW, false)) {
            String workflowXml = service.getLinkXml(serviceInput);
            File workflowFile = new File(IOUtil.joinDir(workDir,
                                    "serviceworkflow.xml"));
            IOUtil.writeFile(workflowFile, workflowXml.toString());
            workflowXml =
                "<entry type=\"type_service_workflow\" name=\"Service workflow\" />";
            IOUtil.writeFile(getEntryManager().getEntryXmlFile(workflowFile),
                             workflowXml.toString());

        }



        if (serviceInput.getPublish() && (output.getEntries().size() > 0)) {
            return new Result(
                request.entryUrl(
                    getRepository().URL_ENTRY_SHOW,
                    output.getEntries().get(0)));
        }




        boolean gotoProducts = request.get(ARG_GOTOPRODUCTS, false);


        //Redirect to the products entry 
        if (gotoProducts) {
            if (output.getResultsShownAsText()) {
                sb.append(HtmlUtils.b(msg("Results")));
                sb.append("<div class=service-output>");
                sb.append("<pre>"); 
                sb.append(output.getResults());
                sb.append("</pre>");
                sb.append("</div>");
                writeProcessEntryXml(request,  service, serviceInput.getProcessDir(), sb.toString());
            }

            return new Result(
                              getStorageManager().getProcessDirEntryUrl(
                                                                        request, serviceInput.getProcessDir()));
        }




        if (serviceInput.getForDisplay() || output.getResultsShownAsText()) {
            sb.append(HtmlUtils.b(msg("Results")));
            sb.append("<div class=service-output>");
            sb.append("<pre>"); 
            sb.append(output.getResults());
            sb.append("</pre>");
            sb.append("</div>");
            makeForm(request, service, baseEntry, entry, outputType, sb);

            return new Result(outputType.getLabel(), sb);
        }





        if (output.getEntries().size() > 1) {
            List<File> files = new ArrayList<File>();
            for (Entry newEntry : output.getEntries()) {
                files.add(newEntry.getFile());
            }

            return getRepository().zipFiles(request, "results.zip", files);
        }
        if (output.getEntries().size() == 1) {
            File file = output.getEntries().get(0).getFile();
            request.setReturnFilename(file.getName());

            return new Result(getStorageManager().getFileInputStream(file),
                              "");
        }

        sb.append("Error: no output files<br>");
        sb.append("<pre>");
        sb.append(output.getResults());
        sb.append("</pre>");
        sb.append(HtmlUtils.hr());
        makeForm(request, service, baseEntry, entry, outputType, sb);

        return new Result(outputType.getLabel(), sb);


    }



    /**
     * _more_
     *
     * @param request _more_
     * @param service _more_
     * @param baseEntry _more_
     * @param entry _more_
     * @param outputType _more_
     * @param sb _more_
     *
     * @throws Exception _more_
     */
    public void makeForm(Request request, Service service, Entry baseEntry,
                         Entry entry, OutputType outputType, Appendable sb)
            throws Exception {

        String formId = HtmlUtils.getUniqueId("form_");
        request.uploadFormWithAuthToken(sb, getRepository().URL_ENTRY_SHOW,
                                        HtmlUtils.id(formId));

        sb.append(HtmlUtils.hidden(ARG_OUTPUT, outputType.getId()));
        sb.append(HtmlUtils.hidden(ARG_ENTRYID, baseEntry.getId()));



        StringBuffer extraSubmit = new StringBuffer();
        extraSubmit.append(HtmlUtils.space(2));
        extraSubmit.append(HtmlUtils.labeledCheckbox(ARG_GOTOPRODUCTS,
                "true", request.get(ARG_GOTOPRODUCTS, true),
                "Go to products page"));
        extraSubmit.append(HtmlUtils.labeledCheckbox(ARG_WRITEWORKFLOW,
                "true", request.get(ARG_WRITEWORKFLOW, false),
                "Write workflow"));

        boolean                haveAnyOutputs = false;
        List<OutputDefinition> outputs = new ArrayList<OutputDefinition>();

        service.getAllOutputs(outputs);
        for (OutputDefinition output : outputs) {
            if ( !output.getShowResults()) {
                haveAnyOutputs = true;

                break;
            }
        }

        if (haveAnyOutputs) {
            extraSubmit.append(HtmlUtils.space(2));
            extraSubmit.append(HtmlUtils.formEntry("",
                    HtmlUtils.checkbox(ARG_ASYNCH, "true",
                                       request.get(ARG_ASYNCH, false)) + " "
                                           + msg("Asynchronous")));
        }



        service.addToForm(request, (entry != null)
                                   ? new ServiceInput(entry)
                                   : new ServiceInput(), sb);

        sb.append(HtmlUtils.hidden(Service.ARG_SERVICEFORM, "true"));
        sb.append(HtmlUtils.p());
        sb.append(HtmlUtils.submit(service.getLabel(), ARG_EXECUTE,
                                   makeButtonSubmitDialog(sb,
                                       "Processing request...")));
        StringBuffer etc = new StringBuffer();
        etc.append(extraSubmit);
        etc.append(HtmlUtils.p());
        etc.append(HtmlUtils.formTable());


        if (haveAnyOutputs) {
            addPublishWidget(request, entry, etc,
                             msg("Select a folder to publish to"), true,
                             false);
        }

        etc.append(HtmlUtils.formTableClose());

        etc.append(HtmlUtils.p());
        etc.append(HtmlUtils.submit(msg("Show Service"), ARG_SHOWCOMMAND,
                                    ""));
        addUrlShowingForm(etc, formId, null);
        etc.append(HtmlUtils.br());
        sb.append(HtmlUtils.makeShowHideBlock("Options...", etc.toString(),
                false));


        sb.append(HtmlUtils.formClose());



    }



}
