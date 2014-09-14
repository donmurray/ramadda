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
import org.ramadda.repository.job.Command;
import org.ramadda.repository.job.JobManager;
import org.ramadda.repository.type.*;

import org.ramadda.util.HtmlUtils;
import org.ramadda.util.Utils;

import org.w3c.dom.*;

import ucar.unidata.util.StringUtil;
import ucar.unidata.util.IOUtil;
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
public class ExecutableOutputHandler extends OutputHandler {

    public static final String ARG_SHOWCOMMAND = "showcommand";


    /** _more_ */
    public static final String ATTR_ICON = "icon";

    /** _more_ */
    public static final String ATTR_LABEL = "label";


    /** _more_ */
    private OutputType outputType;

    /** _more_ */
    private Command command;

    /**
     * _more_
     *
     * @param repository _more_
     * @param element _more_
     * @throws Exception _more_
     */
    public ExecutableOutputHandler(Repository repository, Element element)
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
            command = getRepository().getJobManager().getCommand(commandId);
        }

        if (command == null) {
            NodeList children = XmlUtil.getElements(element,
                                    Command.TAG_COMMAND);
            Element commandNode = element;
            if (children.getLength() > 0) {
                commandNode = (Element) children.item(0);
            }
            command = new Command(getRepository(), commandNode);
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
    public Result outputEntry(Request request, OutputType outputType,
                              Entry entry)
            throws Exception {

        StringBuffer sb = new StringBuffer();
        if ( !request.defined(ARG_EXECUTE) && !request.defined(ARG_SHOWCOMMAND)) {
            makeForm(request, entry, sb);

            return new Result(outputType.getLabel(), sb);
        }

        //        Object       uniqueId = getRepository().getGUID();
        //        File         workDir  = getWorkDir(uniqueId);
        File         workDir = getStorageManager().createProcessDir();

        boolean forDisplay = request.exists(ARG_SHOWCOMMAND);
        List<String> commands = new ArrayList<String>();
        command.addArgs(request, entry, workDir, commands, forDisplay);
        System.err.println("Commands:" + commands);

        if(forDisplay) {
            sb.append(HtmlUtils.p());
            sb.append("Command line:");
            commands.set(0, IOUtil.getFileTail(commands.get(0)));
            sb.append(HtmlUtils.pre(StringUtil.join(" ", commands)));
            makeForm(request, entry, sb);
            return new Result(outputType.getLabel(), sb);
        }
        String   errMsg = "";
        String   outMsg = "";
        File     stdoutFile = new File(IOUtil.joinDir(workDir, ".stdout"));
        File     stderrFile = new File(IOUtil.joinDir(workDir, ".stderr"));
        JobManager.CommandResults results = getRepository().getJobManager().executeCommand(commands,
                null, workDir, -1, new PrintWriter(stdoutFile),
                new PrintWriter(stderrFile));
        if (stderrFile.exists()) {
            errMsg = IOUtil.readContents(stderrFile);
        }

        StringBuffer resultsSB = new StringBuffer();
        System.err.println(" to stderr:" + command.getOutputToStderr());
        System.err.println("err:" + errMsg);

        if (Utils.stringDefined(errMsg)) {
            if (command.getOutputToStderr()) {
                resultsSB.append(errMsg);
                resultsSB.append("\n");
                errMsg = null;
            }
        }


        if (Utils.stringDefined(errMsg)) {
            sb.append(
                getPageHandler().showDialogError(
                    "An error has occurred:<pre>" + errMsg + "</pre>"));
            makeForm(request, entry, sb);

            return new Result(outputType.getLabel(), sb);
        }

        List<Entry>   newEntries  = new ArrayList<Entry>();
        List<File>    newFiles    = new ArrayList<File>();
        HashSet<File> seen        = new HashSet<File>();
        boolean       showResults = false;
        for (Command.Output output : command.getOutputs()) {
            if (output.getShowResults()) {
                showResults = true;
                if (output.getUseStdout()) {
                    resultsSB.append(IOUtil.readContents(stdoutFile));
                } else {}

                continue;
            }


            File[] files = null;
            if (output.getUseStdout()) {
                String filename = applyMacros(entry, workDir,
                                              output.getFilename(), forDisplay);
                File destFile = new File(IOUtil.joinDir(workDir, filename));
                IOUtil.moveFile(stdoutFile, destFile);
                files = new File[] { destFile };
            }
            final String thePattern = output.getPattern();
            if (files == null) {
                files = workDir.listFiles(new FileFilter() {
                    public boolean accept(File f) {
                        if (thePattern == null) {
                            return true;
                        }
                        String name = f.getName();
                        if (name.startsWith(".")) {
                            return false;
                        }
                        if (name.matches(thePattern)) {
                            return true;
                        }

                        return false;
                    }
                });
            }

            for (File file : files) {
                if (seen.contains(file)) {
                    continue;
                }
                seen.add(file);
                newFiles.add(file);
                //                System.err.println("FILE:" + file +" " + file.exists());
                if (doingPublish(request)) {
                    TypeHandler typeHandler =
                        getRepository().getTypeHandler(output.getEntryType());
                    Entry newEntry =
                        typeHandler.createEntry(getRepository().getGUID());
                    newEntry.setDate(new Date().getTime());
                    getEntryManager().processEntryPublish(request, files[0],
                            newEntry, entry, "derived from");
                    newEntries.add(newEntry);
                }
            }
        }


        if (newEntries.size() > 0) {
            return new Result(
                request.entryUrl(
                    getRepository().URL_ENTRY_SHOW, newEntries.get(0)));
        }

        if (showResults) {
            makeForm(request, entry, sb);
            sb.append(header(msg("Results")));
            sb.append("<pre>");
            sb.append(resultsSB);
            sb.append("</pre>");

            return new Result(outputType.getLabel(), sb);
        }

        if (newFiles.size() == 0) {
            for (File file : workDir.listFiles()) {
                if (file.getName().startsWith(".")) {
                    continue;
                }
                //                System.err.println("file:" + file +" " + file.exists());
                newFiles.add(file);
            }
        }
        if (newFiles.size() >= 1) {
            //TODO: handle multiple files by zipping them
            File file = newFiles.get(0);
            request.setReturnFilename(file.getName());

            return new Result(getStorageManager().getFileInputStream(file),
                              "");
        }

        sb.append("<pre>");
        sb.append(resultsSB);
        sb.append("</pre>");

        return new Result(outputType.getLabel(),
                          new StringBuffer("Error: no output files<br>"
                                           + sb));
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
     *
     * @return _more_
     */
    private String applyMacros(Entry entry, File workDir, String value, boolean forDisplay) {
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
        sb.append(HtmlUtils.p());
        sb.append(command.getHelp());
        sb.append(HtmlUtils.p());

        String formId = HtmlUtils.getUniqueId("form_");
        request.uploadFormWithAuthToken(sb, getRepository().URL_ENTRY_SHOW, HtmlUtils.id(formId));
        sb.append(HtmlUtils.hidden(ARG_OUTPUT, outputType.getId()));
        sb.append(HtmlUtils.hidden(ARG_ENTRYID, entry.getId()));

        StringBuffer extraSubmit = new StringBuffer(HtmlUtils.space(2));
        extraSubmit.append(HtmlUtils.submit(msg("Show Command"), ARG_SHOWCOMMAND,""));

        StringBuffer formSB  = new StringBuffer();
        int blockCnt = command.makeForm(request, entry, formSB);
        if(blockCnt>0) {
            sb.append("<div class=inputform>");
            sb.append(formSB);
            sb.append("</div>");
        }
        /*
        sb.append(HtmlUtils.submit(command.getLabel(), ARG_EXECUTE,
                                   makeButtonSubmitDialog(sb,
                                       "Processing request...")));
        */


        sb.append(HtmlUtils.p());
        sb.append(HtmlUtils.submit(command.getLabel(), ARG_EXECUTE,
                                   makeButtonSubmitDialog(sb,
                                                          "Processing request...")));
        sb.append(extraSubmit);
        sb.append(HtmlUtils.p());
        sb.append(HtmlUtils.formTable());


        boolean haveAnyOutputs = false;
        for (Command.Output output : command.getOutputs()) {
            if ( !output.getShowResults()) {
                haveAnyOutputs = true;

                break;
            }
        }

        if (haveAnyOutputs) {
            addPublishWidget(
                request, entry, sb,
                msg("Optionally, select a folder to publish to"));
        }

        sb.append(HtmlUtils.formTableClose());
        addUrlShowingForm(sb,  formId,null);




    }



}
