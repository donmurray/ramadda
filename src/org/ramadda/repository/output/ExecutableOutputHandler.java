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
import org.ramadda.repository.auth.*;
import org.ramadda.repository.type.*;

import org.ramadda.sql.SqlUtil;
import org.ramadda.util.HtmlUtils;

import org.ramadda.util.RssUtil;
import org.ramadda.util.StreamEater;
import org.ramadda.util.Utils;


import org.w3c.dom.*;

import ucar.unidata.ui.ImageUtils;
import ucar.unidata.util.DateUtil;
import ucar.unidata.util.IOUtil;
import ucar.unidata.util.Misc;


import ucar.unidata.util.StringBufferCollection;


import ucar.unidata.util.StringUtil;
import ucar.unidata.util.TwoFacedObject;
import ucar.unidata.xml.XmlUtil;


import java.io.*;

import java.io.File;



import java.net.*;



import java.text.SimpleDateFormat;

import java.util.ArrayList;
import java.util.Date;
import java.util.Enumeration;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.List;
import java.util.Properties;



import java.util.regex.*;

import java.util.zip.*;


/**
 *
 *
 *
 * @author RAMADDA Development Team
 * @version $Revision: 1.3 $
 */
public class ExecutableOutputHandler extends OutputHandler {

    /** _more_ */
    public static final String TAG_ARG = "arg";

    /** _more_ */
    public static final String TAG_OUTPUT = "output";

    /** _more_ */
    public static final String TAG_COMMAND = "command";

    /** _more_ */
    public static final String ATTR_ICON = "icon";

    /** _more_ */
    public static final String ATTR_CATEGORY = "category";

    /** _more_ */
    public static final String ATTR_VALUES = "values";

    /** _more_ */
    public static final String ATTR_ACTIONLABEL = "actionLabel";

    /** _more_ */
    public static final String ATTR_LABEL = "label";

    /** _more_ */
    public static final String ATTR_SUFFIX = "suffix";

    /** _more_ */
    public static final String ATTR_GROUP = "group";

    /** _more_ */
    public static final String ATTR_COMMAND = "command";

    /** _more_ */
    public static final String ATTR_PATHPROPERTY = "pathProperty";


    /** _more_ */
    private OutputType outputType;

    /** _more_ */
    private String entryType;


    /** _more_ */
    private boolean enabled = false;


    /** _more_ */
    private String command;

    /** _more_ */
    private boolean doSingleArgCommand = false;


    /** _more_ */
    private String actionLabel;




    /** _more_ */
    private String help;

    /** _more_ */
    private String pathProperty;

    /** _more_ */
    private List<Arg> args = new ArrayList<Arg>();

    /** _more_ */
    private List<Output> outputs = new ArrayList<Output>();

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
     */
    private void init(Element element) {
        command = XmlUtil.getAttribute(element, TAG_COMMAND, (String) null);
        if (command == null) {
            command = XmlUtil.getGrandChildText(element, TAG_COMMAND);
            doSingleArgCommand = false;
        } else {
            doSingleArgCommand = true;

        }
        entryType = XmlUtil.getAttribute(element, ATTR_TYPE, (String) null);


        pathProperty = XmlUtil.getAttribute(element, ATTR_PATHPROPERTY,
                                            (String) null);

        //Extract it from the command
        if ((pathProperty == null) && command.startsWith("${")) {
            pathProperty = command.substring(2, command.indexOf("}"));
        }

        if ((pathProperty == null)
                || (getProperty(pathProperty, null) == null)) {
            System.err.println(
                "ExecutableOutputHandler: no path property defined:"
                + pathProperty);

            return;
        }


        outputType = new OutputType(XmlUtil.getAttribute(element, ATTR_LABEL,
                "Executable"), XmlUtil.getAttribute(element, ATTR_ID),
                               OutputType.TYPE_OTHER
                               | OutputType.TYPE_IMPORTANT, "",
                                   XmlUtil.getAttribute(element, ATTR_ICON,
                                       (String) null));
        addType(outputType);

        actionLabel = XmlUtil.getAttribute(element, ATTR_ACTIONLABEL,
                                           outputType.getLabel());



        help = XmlUtil.getGrandChildText(element, "help", "");

        NodeList children = XmlUtil.getElements(element, TAG_ARG);
        for (int i = 0; i < children.getLength(); i++) {
            Element node = (Element) children.item(i);
            Arg     arg  = new Arg(node, i);
            args.add(arg);
        }


        children = XmlUtil.getElements(element, TAG_OUTPUT);
        for (int i = 0; i < children.getLength(); i++) {
            Element node   = (Element) children.item(i);
            Output  output = new Output(node);
            outputs.add(output);
        }
        enabled = true;

    }

    /**
     * _more_
     *
     * @return _more_
     */
    public boolean isEnabled() {
        return enabled;
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
            if ((entryType == null)
                    || state.getEntry().getTypeHandler().isType(entryType)) {
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
        if ( !request.defined(ARG_EXECUTE)) {
            sb.append(HtmlUtils.p());
            sb.append(help);
            sb.append(HtmlUtils.p());
            makeForm(request, entry, sb);

            return new Result(outputType.getLabel(), sb);
        }

        Object       uniqueId = getRepository().getGUID();
        File         workDir  = getWorkDir(uniqueId);
        String       cmd      = applyMacros(entry, workDir, command);

        List<String> commands = new ArrayList<String>();
        commands.add(cmd);

        String          fileTail  = getStorageManager().getFileTail(entry);
        HashSet<String> seenGroup = new HashSet<String>();
        for (Arg arg : args) {
            if (arg.getCategory() != null) {
                continue;
            }
            String argValue = null;
            if (arg.isValueArg()) {
                argValue = arg.getValue();
            } else if (arg.isFlag()) {
                if (arg.getGroup() != null) {
                    if ( !seenGroup.contains(arg.getGroup())) {
                        argValue = request.getString(arg.getGroup(), null);
                        if ((argValue != null) && (argValue.length() > 0)) {
                            seenGroup.add(arg.getGroup());
                        } else {
                            argValue = null;
                        }
                    }
                } else if (request.get(arg.getName(), false)) {
                    argValue = arg.getValue();
                }
            } else if (arg.isFile()) {
                String filename = applyMacros(entry, workDir,
                                      arg.getFileName());
                argValue = IOUtil.joinDir(workDir, filename);
            } else if (arg.isEntry()) {
                String entryId = request.getString(arg.getName() + "_hidden",
                                     (String) null);
                Entry entryArg = getEntryManager().getEntry(request, entryId);
                //TODO: Check for null, get the file
                if (entryArg == null) {
                    throw new IllegalArgumentException(
                        "No entry  specified for:" + arg.getLabel());
                }
                argValue = entryArg.getResource().getPath();
            } else {
                argValue = request.getString(arg.getName(), "");
                if (arg.ifDefined && !Utils.stringDefined(argValue)) {
                    continue;
                }
                //The value is the argument
                if (Utils.stringDefined(arg.getValue())) {
                    commands.add(arg.getValue());
                }
            }
            if (argValue != null) {
                argValue = applyMacros(entry, workDir, argValue);
                if (doSingleArgCommand) {
                    commands.add(argValue);
                } else {
                    cmd = cmd.replace(macro(arg.getName()), argValue);
                }
            }
        }

        String   errMsg = "";
        String   outMsg = "";
        String[] results;
        File     stdoutFile = new File(IOUtil.joinDir(workDir, ".stdout"));
        File     stderrFile = new File(IOUtil.joinDir(workDir, ".stderr"));
        if (doSingleArgCommand) {
            System.err.println("Executing: " + commands);
            //                               + StringUtil.join("-", commands));
            results =
                getRepository().getJobManager().executeCommand(commands,
                    null, workDir, -1, new PrintWriter(stdoutFile),
                    new PrintWriter(stderrFile));
            if (stderrFile.exists()) {
                errMsg = IOUtil.readContents(stderrFile);
            }
        } else {
            System.err.println("Executing: " + cmd);
            results = getRepository().getJobManager().executeCommand(command,
                    workDir, -1);
            outMsg = results[0];
            errMsg = results[1];
        }

        if (Utils.stringDefined(errMsg)) {
            sb.append(
                getPageHandler().showDialogError(
                    "An error has occurred:<br>" + errMsg));
            makeForm(request, entry, sb);

            return new Result(outputType.getLabel(), sb);
        }


        StringBuffer  resultsSB   = new StringBuffer();

        List<Entry>   newEntries  = new ArrayList<Entry>();
        List<File>    newFiles    = new ArrayList<File>();
        HashSet<File> seen        = new HashSet<File>();
        boolean       showResults = false;
        for (Output output : outputs) {
            if (output.showResults) {
                showResults = true;
                System.err.println("Showing results");
                if (output.useStdout) {
                    resultsSB.append(IOUtil.readContents(stdoutFile));
                    System.err.println("resultssb: " + resultsSB);
                } else {}

                continue;
            }


            File[] files = null;
            if (output.useStdout) {
                String filename = applyMacros(entry, workDir,
                                      output.filename);
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
    private String applyMacros(Entry entry, File workDir, String value) {
        value = value.replace(macro(pathProperty),
                              getProperty(pathProperty, ""));
        String fileTail = getStorageManager().getFileTail(entry);
        value = value.replace(macro("workdir"), workDir.toString());
        value = value.replace(macro("entry.file"),
                              entry.getResource().getPath());
        value = value.replace(macro("entry.filebase"),
                              IOUtil.stripExtension(entry.getName()));
        value = value.replace(macro("entry.filebase"),
                              IOUtil.stripExtension(fileTail));

        return value;
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

        sb.append(request.form(getRepository().URL_ENTRY_SHOW));
        sb.append(HtmlUtils.hidden(ARG_OUTPUT, outputType.getId()));
        sb.append(HtmlUtils.hidden(ARG_ENTRYID, entry.getId()));

        sb.append(HtmlUtils.submit(actionLabel, ARG_EXECUTE,
                                   makeButtonSubmitDialog(sb,
                                       "Processing request...")));

        int          blockCnt = 0;
        StringBuffer catBuff  = null;
        Arg          catArg   = null;
        for (Arg arg : args) {
            if (arg.isCategory()) {
                if ((catBuff != null) && (catBuff.length() > 0)) {
                    processCatBuff(request, sb, catArg, catBuff, ++blockCnt);
                }
                catArg  = arg;
                catBuff = new StringBuffer();

                continue;
            }

            if (arg.isValueArg()) {
                continue;
            }

            if (catBuff == null) {
                catBuff = new StringBuffer();
                catArg  = null;
            }

            String input = null;
            if (arg.isEnumeration()) {
                input = HtmlUtils.select(arg.getName(), arg.getValues(),
                                         (List) null, "", 100);
            } else if (arg.isFlag()) {
                if (arg.getGroup() != null) {
                    boolean selected = request.getString(arg.getGroup(),
                                           "").equals(arg.getValue());
                    input = HtmlUtils.radio(arg.getGroup(), arg.getValue(),
                                            selected) + HtmlUtils.space(2)
                                                + arg.getLabel();
                } else {
                    input = HtmlUtils.labeledCheckbox(arg.getName(), "true",
                            request.get(arg.getName(), false),
                            arg.getLabel());
                }
                catBuff.append(HtmlUtils.formEntry("", input));

                continue;
            } else if (arg.isFile()) {
                //noop
            } else if (arg.isEntry()) {
                input = getSelect(
                    request, arg.getName(), msg("Select"), true,
                    null) + HtmlUtils.hidden(
                        arg.getName() + "_hidden",
                        request.getString(arg.getName() + "_hidden", ""),
                        HtmlUtils.id(
                            arg.getName() + "_hidden")) + HtmlUtils.space(1)
                                + HtmlUtils.disabledInput(
                                    arg.getName(),
                                    request.getString(arg.getName(), ""),
                                    HtmlUtils.SIZE_60
                                    + HtmlUtils.id(arg.getName()));

            } else {
                input = HtmlUtils.input(arg.getName(),
                                        request.getString(arg.getName(), ""),
                                        arg.getSize());
            }
            if (input == null) {
                continue;
            }
            if (Utils.stringDefined(arg.getSuffix())) {
                input = input + HtmlUtils.space(2) + arg.getSuffix();
            }
            catBuff.append(HtmlUtils.formEntry(msgLabel(arg.getLabel()),
                    input));
        }

        if ((catBuff != null) && (catBuff.length() > 0)) {
            processCatBuff(request, sb, catArg, catBuff, ++blockCnt);
        }

        if (blockCnt > 1) {
            sb.append(HtmlUtils.p());
            sb.append(HtmlUtils.submit(actionLabel, ARG_EXECUTE,
                                       makeButtonSubmitDialog(sb,
                                           "Processing request...")));
        }
        sb.append(HtmlUtils.p());
        sb.append(HtmlUtils.formTable());

        boolean haveAnyOutputs = false;
        for (Output output : outputs) {
            if ( !output.showResults) {
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

    }

    /**
     * _more_
     *
     * @param request _more_
     * @param sb _more_
     * @param catArg _more_
     * @param catBuff _more_
     * @param blockCnt _more_
     */
    private void processCatBuff(Request request, StringBuffer sb, Arg catArg,
                                StringBuffer catBuff, int blockCnt) {
        if (catArg != null) {
            String html = header(catArg.getCategory());
            String desc = catArg.getValue();
            if (Utils.stringDefined(desc)) {
                if (Utils.stringDefined(desc.trim())) {
                    html += desc;
                    html += HtmlUtils.br();
                }
            }
            sb.append(html);
        }
        StringBuffer formSB = new StringBuffer(HtmlUtils.formTable());
        formSB.append(catBuff);
        formSB.append(HtmlUtils.formTableClose());
        if (blockCnt == 1) {
            sb.append(formSB);
        } else {
            sb.append(HtmlUtils.makeShowHideBlock("More...",
                    formSB.toString(), blockCnt == 1));
        }
    }




    /**
     * Class description
     *
     *
     * @version        $version$, Thu, Sep 4, '14
     * @author         Enter your name here...
     */
    public static class Output {

        /** _more_ */
        private String entryType;

        /** _more_ */
        private String pattern;

        /** _more_ */
        private boolean useStdout = false;

        /** _more_ */
        private String filename;

        /** _more_ */
        private boolean showResults = false;

        /**
         * _more_
         *
         * @param node _more_
         */
        public Output(Element node) {
            entryType = XmlUtil.getAttribute(node, ATTR_TYPE,
                                             TypeHandler.TYPE_FILE);
            pattern   = XmlUtil.getAttribute(node, "pattern", (String) null);
            useStdout = XmlUtil.getAttribute(node, "stdout", useStdout);
            filename = XmlUtil.getAttribute(node, "filename", (String) null);
            showResults = XmlUtil.getAttribute(node, "showResults",
                    showResults);
        }


        /**
         *  Set the EntryType property.
         *
         *  @param value The new value for EntryType
         */
        public void setEntryType(String value) {
            entryType = value;
        }

        /**
         *  Get the EntryType property.
         *
         *  @return The EntryType
         */
        public String getEntryType() {
            return entryType;
        }

        /**
         *  Set the Pattern property.
         *
         *  @param value The new value for Pattern
         */
        public void setPattern(String value) {
            pattern = value;
        }

        /**
         *  Get the Pattern property.
         *
         *  @return The Pattern
         */
        public String getPattern() {
            return pattern;
        }


    }


    /**
     * Class description
     *
     *
     * @version        $version$, Mon, Aug 25, '14
     * @author         Enter your name here...
     */
    public static class Arg {

        /** _more_ */
        private static final String TYPE_STRING = "string";

        /** _more_ */
        private static final String TYPE_ENUMERATION = "enumeration";

        /** _more_ */
        private static final String TYPE_ENTRY = "entry";

        /** _more_ */
        private static final String TYPE_FLAG = "flag";

        /** _more_ */
        private static final String TYPE_FILE = "file";

        /** _more_ */
        private static final String TYPE_CATEGORY = "category";


        /** _more_ */
        private String name;

        /** _more_ */
        private String value;

        /** _more_ */
        private String group;

        /** _more_ */
        private boolean nameDefined = false;

        /** _more_ */
        private boolean ifDefined = true;

        /** _more_ */
        private String label;

        /** _more_ */
        private String suffix;

        /** _more_ */
        private String type;

        /** _more_ */
        private String fileName;

        /** _more_ */
        private int size = 24;


        /** _more_ */
        private List<TwoFacedObject> values = new ArrayList<TwoFacedObject>();


        /**
         * _more_
         *
         * @param node _more_
         * @param idx _more_
         */
        public Arg(Element node, int idx) {
            name = XmlUtil.getAttribute(node, ATTR_NAME, (String) null);
            if (name == null) {
                name        = "arg" + idx;
                nameDefined = false;
            } else {
                nameDefined = true;
            }

            type      = XmlUtil.getAttribute(node, ATTR_TYPE, (String) null);
            group     = XmlUtil.getAttribute(node, ATTR_GROUP, (String) null);
            value     = XmlUtil.getChildText(node);
            label     = XmlUtil.getAttribute(node, ATTR_LABEL, name);
            suffix    = XmlUtil.getAttribute(node, ATTR_SUFFIX, "");
            fileName  = XmlUtil.getAttribute(node, "filename", "${src}");
            size      = XmlUtil.getAttribute(node, ATTR_SIZE, size);
            ifDefined = XmlUtil.getAttribute(node, "ifdefined", ifDefined);
            for (String tok :
                    StringUtil.split(XmlUtil.getAttribute(node, ATTR_VALUES,
                        ""), ",", true, true)) {
                List<String> toks  = StringUtil.splitUpTo(tok, ":", 2);

                String       value = toks.get(0);
                String       label;
                if (toks.size() > 1) {
                    label = toks.get(1);
                } else {
                    label = value;
                    values.add(new TwoFacedObject(label, value));
                }
            }

        }

        /**
         * _more_
         *
         * @return _more_
         */
        public boolean isValueArg() {
            return (type == null) && !isCategory() && !nameDefined
                   && Utils.stringDefined(value);
        }

        /**
         * _more_
         *
         * @return _more_
         */
        public boolean isEnumeration() {
            return type.equals(TYPE_ENUMERATION);
        }

        /**
         * _more_
         *
         * @return _more_
         */
        public boolean isFlag() {
            return type.equals(TYPE_FLAG);
        }



        /**
         * _more_
         *
         * @return _more_
         */
        public boolean isFile() {
            return type.equals(TYPE_FILE);
        }

        /**
         * _more_
         *
         * @return _more_
         */
        public boolean isEntry() {
            return type.equals(TYPE_ENTRY);
        }

        /**
         * _more_
         *
         * @return _more_
         */
        public boolean isCategory() {
            return (type != null) && type.equals(TYPE_CATEGORY);
        }


        /**
         * _more_
         *
         * @return _more_
         */
        public String getGroup() {
            return group;
        }


        /**
         * Set the Value property.
         *
         * @param value The new value for Value
         */
        public void setValue(String value) {
            value = value;
        }

        /**
         * Get the Value property.
         *
         * @return The Value
         */
        public String getValue() {
            return value;
        }


        /**
         * Get the Id property.
         *
         * @return The Id
         */
        public String getName() {
            return name;
        }

        /**
         * Set the Label property.
         *
         * @param value The new value for Label
         */
        public void setLabel(String value) {
            label = value;
        }

        /**
         * Get the Label property.
         *
         * @return The Label
         */
        public String getLabel() {
            return label;
        }


        /**
         * _more_
         *
         * @return _more_
         */
        public String getSuffix() {
            return suffix;
        }

        /**
         * Set the Size property.
         *
         * @param value The new value for Size
         */
        public void setSize(int value) {
            size = value;
        }

        /**
         * Get the Size property.
         *
         * @return The Size
         */
        public int getSize() {
            return size;
        }

        /**
         * Set the FileName property.
         *
         * @param value The new value for FileName
         */
        public void setFileName(String value) {
            fileName = value;
        }

        /**
         * Get the FileName property.
         *
         * @return The FileName
         */
        public String getFileName() {
            return fileName;
        }




        /**
         * Set the Type property.
         *
         * @param value The new value for Type
         */
        public void setType(String value) {
            type = value;
        }

        /**
         * Get the Type property.
         *
         * @return The Type
         */
        public String getType() {
            return type;
        }

        /**
         * Set the Values property.
         *
         * @param value The new value for Values
         */
        public void setValues(List<TwoFacedObject> value) {
            values = value;
        }

        /**
         * Get the Values property.
         *
         * @return The Values
         */
        public List<TwoFacedObject> getValues() {
            return values;
        }


        /**
         *  Get the Category property.
         *
         *  @return The Category
         */
        public String getCategory() {
            if ( !isCategory()) {
                return null;
            }

            return label;
        }





    }


    /**
     * _more_
     *
     * @param args _more_
     *
     * @throws Exception _more_
     */
    public static void main(String[] args) throws Exception {
        Process      process  = null;
        StringWriter outBuf   = new StringWriter();
        StringWriter errorBuf = new StringWriter();

        args = new String[] {
            "/Users/jeffmc/software/sratoolkit.2.3.5-2-mac64/bin/fastq-dump -O /Users/jeffmc/.ramadda/tmp/products/6d9a0c4a-9ca4-479f-8bfd-2e0e27deb142 /Users/jeffmc/data/genomics/sra/test\\ file.sra" };

        args = new String[] {
            "/Users/jeffmc/software/sratoolkit.2.3.5-2-mac64/bin/fastq-dump",
            "-O",
            "/Users/jeffmc/.ramadda/tmp/products/6d9a0c4a-9ca4-479f-8bfd-2e0e27deb142",
            "/Users/jeffmc/data/genomics/sra/test file.sra" };


        if (args.length == 1) {
            process = Runtime.getRuntime().exec(args[0]);
        } else {
            List<String> commands = new ArrayList<String>();
            for (String arg : args) {
                commands.add(arg);
            }
            System.err.println("Commands:" + commands);
            ProcessBuilder pb = new ProcessBuilder(commands);
            pb.directory(new File("."));
            process = pb.start();
        }

        StreamEater esg = new StreamEater(process.getErrorStream(),
                                          new PrintWriter(errorBuf));
        StreamEater isg = new StreamEater(process.getInputStream(),
                                          new PrintWriter(outBuf));
        esg.start();
        isg.start();
        int exitCode = process.waitFor();
        System.err.println("err:" + errorBuf);
        System.err.println("out:" + outBuf);

    }


}
