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


import org.ramadda.repository.*;
import org.ramadda.data.process.Service;
import org.ramadda.data.process.ServiceInfo;
import org.ramadda.data.process.ServiceInput;
import org.ramadda.data.process.ServiceOutput;
import org.ramadda.data.process.OutputDefinition;
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

    /** _more_ */
    public static final String ATTR_ICON = "icon";

    /** _more_ */
    public static final String ATTR_LABEL = "label";


    /** _more_ */
    private OutputType outputType;

    /** _more_ */
    private Service command;

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
     * @param element _more_
     *
     * @throws Exception _more_
     */
    private void init(Element element) throws Exception {
        String commandId = XmlUtil.getAttribute(element, "commandId",
                               (String) null);
        if (commandId != null) {
            command = getRepository().getJobManager().getService(commandId);
            if (command == null) {
                throw new IllegalStateException(
                    "ServiceOutputHandler: could not find command:"
                    + commandId);
            }
        }


        if (command == null) {
            NodeList children = XmlUtil.getElements(element,
                                    Service.TAG_SERVICE);
            Element commandNode = element;
            if (children.getLength() > 0) {
                commandNode = (Element) children.item(0);
            }
            command = new Service(getRepository(), commandNode);
            getRepository().getJobManager().addService(command);
        }



        outputType = new OutputType(
            XmlUtil.getAttribute(element, ATTR_LABEL, command.getLabel()),
            XmlUtil.getAttribute(element, ATTR_ID, command.getId()),
            OutputType.TYPE_OTHER | OutputType.TYPE_IMPORTANT, "",
            XmlUtil.getAttribute(element, ATTR_ICON, command.getIcon()));
        addType(outputType);


    }

    /**
     * _more_
     *
     * @return _more_
     */
    public boolean isEnabled() {
        return command.isEnabled();
    }

    /**
     * _more_
     *
     * @return _more_
     */
    public Service getService() {
        return command;
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
            if (command.isApplicable(state.getEntry())) {
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

        StringBuffer sb = new StringBuffer();
        if ( !request.defined(ARG_EXECUTE)
                && !request.defined(ARG_SHOWCOMMAND)) {
            makeForm(request, entry, sb);

            return new Result(outputType.getLabel(), sb);
        }

        //        Object       uniqueId = getRepository().getGUID();
        //        File         workDir  = getWorkDir(uniqueId);
        File              workDir     =
            getStorageManager().createProcessDir();

        boolean           forDisplay  = request.exists(ARG_SHOWCOMMAND);

        final ServiceInput serviceInput  = new ServiceInput(entry);
        final ServiceInfo serviceInfo = new ServiceInfo(workDir, forDisplay);
        serviceInfo.setPublish(doingPublish(request));

        StringBuffer xml = new StringBuffer();
        xml.append(XmlUtil.tag("entry",
                               XmlUtil.attrs("type", "group", "name",
                                             "Processing Results")));
        IOUtil.writeFile(new File(IOUtil.joinDir(workDir,
                ".this.ramadda.xml")), xml.toString());
        boolean asynchronous = request.get(ARG_ASYNCH, false);

        if (asynchronous) {
            ActionManager.Action action = new ActionManager.Action() {
                public void run(Object actionId) throws Exception {
                    ServiceOutput output = null;
                    try {
                        //TODO:
                        //    public ServiceOutput evaluate(Request request, ServiceInfo info, ServiceInput input)
                        //                        output = command.evaluate(request, entry,
                        output = command.evaluate(request, serviceInfo, serviceInput);
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
                    String url =
                        getStorageManager().getProcessDirEntryUrl(request,
                            serviceInfo.getWorkDir());
                    if (serviceInfo.getPublish()
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
        ServiceOutput output = command.evaluate(request, serviceInfo, serviceInput);
        //command.evaluate(request, entry, serviceInfo);

        if ( !output.isOk()) {
            sb.append(
                getPageHandler().showDialogError(
                    "An error has occurred:<pre>" + output.getResults()
                    + "</pre>"));
            makeForm(request, entry, sb);

            return new Result(outputType.getLabel(), sb);
        }

        //        System.err.println ("params:" + serviceInfo.getParams());
        //        System.err.println ("entries:" + serviceInfo.getEntries());
        if (serviceInfo.getPublish() && (output.getEntries().size() > 0)) {
            return new Result(
                request.entryUrl(
                    getRepository().URL_ENTRY_SHOW,
                    output.getEntries().get(0)));
        }


        if (forDisplay || output.getResultsShownAsText()) {
            sb.append(HtmlUtils.b(msg("Results")));
            sb.append("<div class=command-output>");
            sb.append("<pre>");
            sb.append(output.getResults());
            sb.append("</pre>");
            sb.append("</div>");
            makeForm(request, entry, sb);

            return new Result(outputType.getLabel(), sb);
        }


        //Redirect to the products entry 
        if (request.get(ARG_GOTOPRODUCTS, false)) {
            return new Result(
                getStorageManager().getProcessDirEntryUrl(
                    request, serviceInfo.getWorkDir()));
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
        makeForm(request, entry, sb);

        return new Result(outputType.getLabel(), sb);

    }

    /**
     * _more_
     *
     * @param s _more_
     *
     * @return _more_
     */
    private String macro(String s) {
        return "${" + s + "}";
    }

    /**
     * _more_
     *
     * @param entry _more_
     * @param workDir _more_
     * @param value _more_
     * @param forDisplay _more_
     *
     * @return _more_
     */
    private String applyMacros(Entry entry, File workDir, String value,
                               boolean forDisplay) {
        return command.applyMacros(entry, workDir, value, forDisplay);
    }


    /**
     * _more_
     *
     * @param request _more_
     * @param entry _more_
     * @param sb _more_
     *
     * @throws Exception _more_
     */
    public void makeForm(Request request, Entry entry, StringBuffer sb)
            throws Exception {


        String formId = HtmlUtils.getUniqueId("form_");
        request.uploadFormWithAuthToken(sb, getRepository().URL_ENTRY_SHOW,
                                        HtmlUtils.id(formId));
        sb.append(HtmlUtils.hidden(ARG_OUTPUT, outputType.getId()));
        sb.append(HtmlUtils.hidden(ARG_ENTRYID, entry.getId()));



        StringBuffer extraSubmit = new StringBuffer();
        extraSubmit.append(HtmlUtils.space(2));
        extraSubmit.append(HtmlUtils.labeledCheckbox(ARG_GOTOPRODUCTS,
                "true", false, "Go to products page"));

        boolean                haveAnyOutputs = false;
        List<OutputDefinition> outputs = new ArrayList<OutputDefinition>();

        command.getAllOutputs(outputs);
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


        command.addToForm(request, new ServiceInput(entry), sb);

        sb.append(HtmlUtils.p());
        sb.append(HtmlUtils.submit(command.getLabel(), ARG_EXECUTE,
                                   makeButtonSubmitDialog(sb,
                                       "Processing request...")));
        StringBuffer etc = new StringBuffer();

        etc.append(HtmlUtils.br());
        etc.append(extraSubmit);
        etc.append(HtmlUtils.p());
        etc.append(HtmlUtils.formTable());


        if (haveAnyOutputs) {
            addPublishWidget(
                request, entry, etc,
                msg("Optionally, select a folder to publish to"), true,
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
