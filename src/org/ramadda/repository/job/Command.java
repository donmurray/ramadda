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

package org.ramadda.repository.job;


import org.ramadda.repository.*;
import org.ramadda.repository.auth.*;
import org.ramadda.repository.output.OutputHandler;
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


import java.lang.reflect.Method;



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
public class Command extends RepositoryManager {

    /** _more_          */
    public static boolean debug = false;

    /** _more_ */
    public static final String TAG_ARG = "arg";

    /** _more_ */
    public static final String ATTR_ID = "id";

    /** _more_ */
    public static final String ATTR_PRIMARY = "primary";

    /** _more_ */
    public static final String ATTR_ENTRY_TYPE = "entryType";

    /** _more_          */
    public static final String ATTR_ENTRY_PATTERN = "entryPattern";

    /** _more_ */
    public static final String ATTR_ICON = "icon";

    /** _more_ */
    public static final String TAG_OUTPUT = "output";

    /** _more_ */
    public static final String TAG_INPUT = "input";

    /** _more_ */
    public static final String TAG_COMMAND = "command";


    /** _more_ */
    public static final String ATTR_CATEGORY = "category";

    /** _more_ */
    public static final String ATTR_VALUES = "values";

    /** _more_ */
    public static final String ATTR_LABEL = "label";

    /** _more_ */
    public static final String ATTR_HELP = "help";

    /** _more_ */
    public static final String ATTR_GROUP = "group";

    /** _more_ */
    public static final String ATTR_COMMAND = "command";

    /** _more_ */
    public static final String ATTR_PATHPROPERTY = "pathProperty";

    /** _more_ */
    private String id;

    /** _more_ */
    private String icon;

    /** _more_ */
    private String entryType;


    /** _more_ */
    private boolean enabled = false;

    /** _more_ */
    private boolean outputToStderr = false;

    /** _more_          */
    private boolean cleanup = false;


    /** _more_ */
    private String command;

    /** _more_          */
    private Object commandObject;

    /** _more_          */
    private Method commandMethod;

    /** _more_ */
    private String help;

    /** _more_ */
    private String label;

    /** _more_ */
    private String pathProperty;

    /** _more_ */
    private Command parent;

    /** _more_ */
    private List<Command> children;

    /** _more_          */
    public boolean serial;

    /** _more_ */
    private String linkId;

    /** _more_ */
    private Command link;

    /** _more_ */
    private List<Arg> args = new ArrayList<Arg>();

    /** _more_ */
    private List<Arg> inputs = new ArrayList<Arg>();

    /** _more_ */
    private List<Output> outputs = new ArrayList<Output>();



    /**
     * _more_
     *
     * @param repository _more_
     * @param element _more_
     * @throws Exception _more_
     */
    public Command(Repository repository, Element element) throws Exception {
        super(repository);
        init(null, element, null);
    }



    /**
     * _more_
     *
     * @param repository _more_
     * @param parent _more_
     * @param element _more_
     * @param index _more_
     *
     * @throws Exception _more_
     */
    public Command(Repository repository, Command parent, Element element,
                   int index)
            throws Exception {
        super(repository);
        init(parent, element, "command_" + index);
    }



    /**
     * _more_
     *
     * @return _more_
     */
    public String getUrlArg() {
        if (parent != null) {
            return parent.getUrlArg() + "_" + id;
        }

        return id;
    }

    /**
     * _more_
     *
     * @param msg _more_
     */
    private static void debug(String msg) {
        if (debug) {
            System.err.println(msg);
        }
    }

    /**
     * _more_
     *
     *
     * @param parent _more_
     * @param element _more_
     * @param dfltId _more_
     *
     * @throws Exception _more_
     */
    private void init(Command parent, Element element, String dfltId)
            throws Exception {

        this.parent = parent;
        id          = XmlUtil.getAttribute(element, ATTR_ID, dfltId);

        if (id == null) {
            throw new IllegalStateException("Command: no id defined in: "
                                            + XmlUtil.toString(element));
        }
        entryType = XmlUtil.getAttribute(element, ATTR_ENTRY_TYPE,
                                         (String) null);

        icon = XmlUtil.getAttributeFromTree(element, ATTR_ICON,
                                            (String) null);
        outputToStderr = XmlUtil.getAttributeFromTree(element,
                "outputToStderr", outputToStderr);

        cleanup = XmlUtil.getAttributeFromTree(element, "cleanup", true);

        linkId  = XmlUtil.getAttribute(element, "link", (String) null);
        help    = XmlUtil.getGrandChildText(element, "help", "");
        label   = XmlUtil.getAttribute(element, ATTR_LABEL, (String) null);
        serial  = XmlUtil.getAttribute(element, "serial", true);

        NodeList nodes;

        nodes = XmlUtil.getElements(element, TAG_COMMAND);
        for (int i = 0; i < nodes.getLength(); i++) {
            Element node = (Element) nodes.item(i);
            addChild(new Command(getRepository(), this, node, i));
        }

        if (linkId != null) {
            initCommand();
        } else if (children == null) {
            command = XmlUtil.getAttributeFromTree(element, TAG_COMMAND,
                    (String) null);
            pathProperty = XmlUtil.getAttribute(element, ATTR_PATHPROPERTY,
                    (String) null);

            //Extract it from the command
            if ((pathProperty == null) && (command != null)) {
                int index = command.indexOf("${");
                if (index >= 0) {
                    pathProperty = command.substring(index + 2,
                            command.indexOf("}"));
                }
            }
            if (pathProperty != null) {
                String pathPropertyValue =
                    getRepository().getPropertyFromTree(pathProperty, null);
                if (pathPropertyValue != null) {
                    if (command == null) {
                        command = pathPropertyValue;
                    } else {
                        command = command.replace(macro(pathProperty),
                                pathPropertyValue);
                    }
                }
            }
            if ((command == null) || (command.indexOf("${") >= 0)) {
                System.err.println("Command: no command defined:" + command
                                   + " path:" + pathProperty);

                return;
            }
        }

        //Look for:
        //java:<class>:<method>
        if ((command != null) && command.equals("util")) {
            command = "java:org.ramadda.repository.job.CommandUtil:evaluate";
        }

        if ((command != null) && command.startsWith("java:")) {
            List<String> toks      = StringUtil.split(command, ":");
            String       className = toks.get(1);
            if (className.trim().length() == 0) {
                className = "org.ramadda.repository.job.CommandUtil";
            }
            commandObject = Misc.findClass(className).newInstance();
            Class[] paramTypes = new Class[] { Request.class, Entry.class,
                    Command.class, CommandInfo.class, List.class };
            commandMethod = Misc.findMethod(commandObject.getClass(),
                                            toks.get(2), paramTypes);
        }

        nodes = XmlUtil.getElements(element, TAG_ARG);
        for (int i = 0; i < nodes.getLength(); i++) {
            Element node = (Element) nodes.item(i);
            Arg     arg  = new Arg(this, node, i);
            args.add(arg);
            if (arg.isEntry()) {
                inputs.add(arg);
            }
        }


        nodes = XmlUtil.getElements(element, TAG_OUTPUT);
        for (int i = 0; i < nodes.getLength(); i++) {
            Element node   = (Element) nodes.item(i);
            Output  output = new Output(node);
            outputs.add(output);
        }
        enabled = true;

    }

    /**
     * _more_
     *
     * @param command _more_
     */
    private void addChild(Command command) {
        if (children == null) {
            children = new ArrayList<Command>();
        }
        children.add(command);
    }

    /**
     * _more_
     *
     * @return _more_
     */
    public boolean haveChildren() {
        return (children != null) && (children.size() > 0);
    }

    /**
     * _more_
     */
    private void initCommand() {
        if (link != null) {
            return;
        }
        if (linkId != null) {
            link = getRepository().getJobManager().getCommand(linkId);
        }
    }

    /**
     * _more_
     *
     * @return _more_
     */
    public Command getCommandToUse() {
        initCommand();
        if (link != null) {
            return link;
        }

        return this;
    }

    /**
     * _more_
     *
     * @param request _more_
     * @param primaryEntry _more_
     * @param info _more_
     * @param commands _more_
     * @param forDisplay _more_
     *
     * @throws Exception _more_
     */
    public void addArgs(Request request, Entry primaryEntry,
                        CommandInfo info, List<String> commands,
                        boolean forDisplay)
            throws Exception {
        getCommandToUse().addArgsInner(request, primaryEntry, info, commands,
                                       forDisplay);
    }


    /**
     * _more_
     *
     * @param request _more_
     * @param primaryEntry _more_
     * @param info _more_
     * @param commands _more_
     * @param forDisplay _more_
     *
     * @throws Exception _more_
     */
    private void addArgsInner(Request request, Entry primaryEntry,
                              CommandInfo info, List<String> commands,
                              boolean forDisplay)
            throws Exception {

        File workDir = info.getWorkDir();
        String cmd = applyMacros(primaryEntry, workDir, getCommand(),
                                 forDisplay);
        commands.add(cmd);
        HashSet<String> seenGroup = new HashSet<String>();
        for (Command.Arg arg : getArgs()) {
            Entry currentEntry = primaryEntry;
            if (arg.getCategory() != null) {
                continue;
            }
            String argValue = null;
            String argKey   = null;
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
                        if (argValue != null) {
                            info.addParam(arg.getGroup(), argValue);
                        }
                    }
                } else if (request.get(arg.getUrlArg(), false)) {
                    argValue = arg.getValue();
                    if (argValue != null) {
                        info.addParam(arg.getUrlArg(), argValue);
                    }
                }
            } else if (arg.isFile()) {
                //TODO:
                //                String filename = applyMacros(currentEntry, workDir,
                //arg.getFileName(), forDisplay);
                //argValue = IOUtil.joinDir(workDir, filename);
            } else if (arg.isEntry()) {
                if (arg.isPrimaryEntry()) {
                    currentEntry = primaryEntry;
                } else {
                    currentEntry = null;
                }

                if (currentEntry == null) {
                    String entryId = request.getString(arg.getUrlArg()
                                         + "_hidden", (String) null);
                    Entry entryArg = getEntryManager().getEntry(request,
                                         entryId);
                    if (entryArg == null) {
                        if (arg.isRequired()) {
                            throw new IllegalArgumentException(
                                "No entry  specified for:" + arg.getLabel());
                        }

                        continue;
                    }
                    currentEntry = entryArg;
                    info.addParam(arg.getUrlArg() + "_hidden",
                                  entryArg.getId());
                }
                argValue = arg.getValue();
                if ( !Utils.stringDefined(argValue)) {
                    argValue = currentEntry.getResource().getPath();
                }
            } else {
                argValue = request.getString(arg.getUrlArg(), "");
                if (arg.getIfDefined() && !Utils.stringDefined(argValue)) {
                    continue;
                }
                info.addParam(arg.getUrlArg(), argValue);
                if (Utils.stringDefined(arg.value)) {
                    argValue = arg.value.replace("${value}", argValue);
                }

            }

            if ((argValue == null) && arg.isRequired()) {
                throw new IllegalArgumentException("No entry  specified for:"
                        + arg.getLabel());
            }

            if (Utils.stringDefined(argValue) || arg.isRequired()) {
                if (Utils.stringDefined(arg.prefix)) {
                    commands.add(arg.prefix);
                }
                argValue = applyMacros(currentEntry, workDir, argValue,
                                       forDisplay);


                commands.add(argValue);
            }
        }
    }

    /**
     * _more_
     *
     * @return _more_
     */
    public String getIcon() {
        if (icon != null) {
            return icon;
        }
        if (link != null) {
            link.getIcon();
        }
        if (haveChildren()) {
            return children.get(0).getIcon();
        }

        return null;
    }

    /**
     * _more_
     *
     * @return _more_
     */
    public String getId() {
        initCommand();
        if (link != null) {
            return link.getId();
        }

        return id;
    }


    /**
     * _more_
     *
     * @param request _more_
     * @param primaryEntry _more_
     * @param sb _more_
     *
     * @return _more_
     *
     * @throws Exception _more_
     */
    public int makeForm(Request request, Entry primaryEntry, StringBuffer sb)
            throws Exception {
        initCommand();
        if (link != null) {
            return link.makeForm(request, primaryEntry, sb);
        }

        if (haveChildren()) {
            int cnt = 0;
            for (Command child : children) {
                StringBuffer tmpSB = new StringBuffer();
                int blockCnt = child.makeForm(request, primaryEntry, tmpSB);
                cnt += blockCnt;
                if (blockCnt > 0) {
                    sb.append(header("Command: " + child.getLabel()));
                    sb.append(HtmlUtils.p());
                    sb.append(tmpSB);
                }

            }

            return cnt;
        }

        return makeFormInner(request, primaryEntry, sb);
    }

    /**
     * _more_
     *
     * @param request _more_
     * @param primaryEntry _more_
     * @param sb _more_
     *
     * @return _more_
     *
     * @throws Exception _more_
     */
    private int makeFormInner(Request request, Entry primaryEntry,
                              StringBuffer sb)
            throws Exception {

        int          blockCnt    = 0;
        StringBuffer catBuff     = null;
        Command.Arg  catArg      = null;
        boolean      anyRequired = false;
        for (Command.Arg arg : getArgs()) {
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

            String       tooltip = arg.getPrefix();
            StringBuffer input   = new StringBuffer();
            if (arg.isEnumeration()) {
                input.append(HtmlUtils.select(arg.getUrlArg(),
                        arg.getValues(), (List) null, "", 100));
            } else if (arg.isFlag()) {
                if (arg.getGroup() != null) {
                    boolean selected = request.getString(arg.getGroup(),
                                           "").equals(arg.getValue());
                    input.append(HtmlUtils.radio(arg.getGroup(),
                            arg.getValue(), selected));
                } else {
                    input.append(HtmlUtils.checkbox(arg.getUrlArg(), "true",
                            request.get(arg.getUrlArg(), false)));
                }

                input.append(HtmlUtils.space(2));
                input.append(arg.getHelp());
                catBuff.append(HtmlUtils.formEntry("", input.toString(), 2));

                continue;
            } else if (arg.isFile()) {
                //noop
            } else if (arg.isEntry()) {
                if ((primaryEntry != null) && arg.isPrimaryEntry()) {
                    continue;
                } else {
                    if (arg.getEntryType() != null) {
                        request.put(ARG_ENTRYTYPE, arg.getEntryType());
                    }
                    input.append(OutputHandler.getSelect(request,
                            arg.getUrlArg(), msg("Select"), true, null));
                    input.append(
                        HtmlUtils.hidden(
                            arg.getUrlArg() + "_hidden",
                            request.getString(
                                arg.getUrlArg() + "_hidden",
                                ""), HtmlUtils.id(
                                    arg.getUrlArg() + "_hidden")));
                    input.append(HtmlUtils.space(1));
                    input.append(HtmlUtils.disabledInput(arg.getUrlArg(),
                            request.getString(arg.getUrlArg(), ""),
                            HtmlUtils.SIZE_60
                            + HtmlUtils.id(arg.getUrlArg())));
                    request.remove(ARG_ENTRYTYPE);
                }

            } else {
                String extra = HtmlUtils.attr(HtmlUtils.ATTR_SIZE,
                                   "" + arg.getSize());
                if (arg.placeHolder != null) {
                    extra += HtmlUtils.attr("placeholder", arg.placeHolder);
                }
                input.append(
                    HtmlUtils.input(
                        arg.getUrlArg(),
                        request.getString(arg.getUrlArg(), ""), extra));
            }
            if (input.length() == 0) {
                continue;
            }
            if (arg.isRequired()) {
                anyRequired = true;
                input.append(HtmlUtils.space(1));
                input.append("<span class=ramadda-required-label>*</span>");
            }

            makeFormEntry(catBuff, arg.getLabel(), input.toString(),
                          arg.getHelp());
        }

        if ((catBuff != null) && (catBuff.length() > 0)) {
            processCatBuff(request, sb, catArg, catBuff, ++blockCnt);
        }
        if (anyRequired) {
            sb.append("<span class=ramadda-required-label>* required</span>");
        }

        return blockCnt;

    }


    /**
     * _more_
     *
     * @param sb _more_
     * @param label _more_
     * @param col1 _more_
     * @param help _more_
     */
    private void makeFormEntry(StringBuffer sb, String label, String col1,
                               String help) {
        sb.append(HtmlUtils.formEntryTop(Utils.stringDefined(label)
                                         ? msgLabel(label)
                                         : "", col1,
                                         HtmlUtils.div(
                                             help,
                                             HtmlUtils.cssClass(
                                                 "command-form-help"))));
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
    private void processCatBuff(Request request, StringBuffer sb,
                                Command.Arg catArg, StringBuffer catBuff,
                                int blockCnt) {
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
        if (true || (blockCnt == 1)) {
            sb.append(formSB);
        } else {
            sb.append(HtmlUtils.makeShowHideBlock("", formSB.toString(),
                    true || (blockCnt == 1)));
        }
    }



    /**
     * _more_
     *
     * @return _more_
     */
    public boolean isEnabled() {
        if (linkId != null) {
            return getCommandToUse().isEnabled();
        }
        if (haveChildren()) {
            for (Command child : children) {
                if ( !child.isEnabled()) {
                    return false;
                }
            }

            return true;
        }

        return enabled;
    }

    /**
     * _more_
     *
     * @return _more_
     */
    public boolean getOutputToStderr() {
        return outputToStderr;
    }

    /**
     * _more_
     *
     * @return _more_
     */
    public List<Arg> getArgs() {
        return args;
    }

    /**
     * _more_
     *
     * @return _more_
     */
    public List<Output> getOutputs() {
        return outputs;
    }


    /**
     * _more_
     *
     * @return _more_
     */
    public List<Arg> getInputs() {
        return inputs;
    }

    /**
     * _more_
     *
     * @param entry _more_
     *
     * @return _more_
     */
    public boolean isApplicable(Entry entry) {
        if (linkId != null) {
            return getCommandToUse().isApplicable(entry);
        }
        if (haveChildren()) {
            return children.get(0).isApplicable(entry);
        }
        for (Arg input : inputs) {
            if (input.isApplicable(entry)) {
                return true;
            }
        }
        if (inputs.size() > 0) {
            return false;
        }
        if (entryType != null) {
            return entry.getTypeHandler().isType(entryType);
        }

        return false;
    }


    /**
     * _more_
     *
     * @return _more_
     */
    public String getCommand() {
        if (linkId != null) {
            getCommandToUse().getCommand();
        }

        return command;
    }

    /**
     * _more_
     *
     * @return _more_
     *
     */
    public String getHelp() {
        if (linkId != null) {
            getCommandToUse().getHelp();
        }

        return help;
    }

    /**
     * _more_
     *
     * @return _more_
     */
    public String getLabel() {
        initCommand();
        if (label != null) {
            return label;
        }
        if (link != null) {
            return link.getLabel();
        }

        return id.replaceAll("_", " ");
    }


    /**
     * _more_
     *
     * @param request _more_
     * @param entry _more_
     * @param info _more_
     *
     * @return _more_
     *
     * @throws Exception _more_
     */
    public boolean evaluate(Request request, Entry entry, CommandInfo info)
            throws Exception {

        if (haveChildren()) {
            HashSet<File> existingFiles = new HashSet<File>();
            HashSet<File> newFiles      = new HashSet<File>();
            for (File f : info.getWorkDir().listFiles()) {
                existingFiles.add(f);
            }
            List<Entry> myEntries        = new ArrayList<Entry>();
            CommandInfo childCommandInfo = null;
            for (Command child : children) {
                Entry primaryEntryForChild = entry;
                if (serial) {
                    if (childCommandInfo != null) {
                        List<Entry> lastEntries =
                            childCommandInfo.getEntries();
                        if ((lastEntries != null)
                                && (lastEntries.size() > 0)) {
                            primaryEntryForChild = lastEntries.get(0);
                        }
                    }
                }

                childCommandInfo = new CommandInfo(info);
                childCommandInfo.setEntries(new ArrayList<Entry>());
                if ( !child.evaluate(request, primaryEntryForChild,
                                     childCommandInfo)) {
                    return false;
                }
                if ( !serial) {
                    myEntries.addAll(childCommandInfo.getEntries());
                }
            }

            //If we are  serial then we only add the last command's entry (or add them all?)
            if (serial && (childCommandInfo != null)) {
                myEntries.addAll(childCommandInfo.getEntries());
            }
            for (Entry newEntry : myEntries) {
                newFiles.add(newEntry.getFile());
                info.addEntry(newEntry);
            }
            if (cleanup) {
                for (File f : info.getWorkDir().listFiles()) {
                    if (f.getName().startsWith(".")) {
                        continue;
                    }
                    if (existingFiles.contains(f) || newFiles.contains(f)) {
                        continue;
                    }
                    System.err.println("deleting:" + f);
                    f.delete();
                }
            }


            if (info.getForDisplay()) {
                return true;
            }

            return true;
        }

        List<String> commands = new ArrayList<String>();
        if (link != null) {
            link.addArgs(request, entry, info, commands,
                         info.getForDisplay());
        } else {
            this.addArgs(request, entry, info, commands,
                         info.getForDisplay());
        }
        System.err.println("Commands:" + commands);


        if (info.getForDisplay()) {
            info.getResults().append(HtmlUtils.br());
            commands.set(0, IOUtil.getFileTail(commands.get(0)));
            info.getResults().append(HtmlUtils.pre(StringUtil.join(" ",
                    commands)));

            return true;
        }

        String errMsg = "";
        String outMsg = "";
        File stdoutFile = new File(IOUtil.joinDir(info.getWorkDir(),
                              "." + getId() + ".stdout"));
        File stderrFile = new File(IOUtil.joinDir(info.getWorkDir(),
                              "." + getId() + ".stderr"));


        if (commandObject != null) {
            commandMethod.invoke(commandObject, new Object[] { request, entry,
                    this, info, commands });
        } else {
            JobManager.CommandResults results =
                getRepository().getJobManager().executeCommand(commands,
                    null, info.getWorkDir(), -1, new PrintWriter(stdoutFile),
                    new PrintWriter(stderrFile));
        }
        if (stderrFile.exists()) {
            errMsg = IOUtil.readContents(stderrFile);
        }
        if (Utils.stringDefined(errMsg)) {
            if (getOutputToStderr()) {
                info.getResults().append(errMsg);
                info.getResults().append("\n");
            } else {
                info.getError().append(errMsg);
            }
        }



        if (info.getError().length() > 0) {
            return false;
        }

        HashSet<File> seen = new HashSet<File>();
        for (Command.Output output : getOutputs()) {
            if (output.getShowResults()) {
                info.setResultsShownAsText(true);
                if (output.getUseStdout()) {
                    info.getResults().append(IOUtil.readContents(stdoutFile));
                } else {}

                continue;
            }

            File[] files = null;
            if (output.getUseStdout()) {
                String filename = applyMacros(entry, info.getWorkDir(),
                                      output.getFilename(),
                                      info.getForDisplay());
                File destFile = new File(IOUtil.joinDir(info.getWorkDir(),
                                    filename));
                IOUtil.moveFile(stdoutFile, destFile);
                files = new File[] { destFile };
            }
            final String thePattern = output.getPattern();
            if (files == null) {
                files = info.getWorkDir().listFiles(new FileFilter() {
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
                StringBuffer entryXml = new StringBuffer();
                entryXml.append(XmlUtil.tag("entry",
                                            XmlUtil.attrs("name",
                                                file.getName(), "type",
                                                    (output.getEntryType()
                                                        != null)
                        ? output.getEntryType()
                        : TypeHandler.TYPE_FILE)));
                IOUtil.writeFile(getEntryManager().getEntryXmlFile(file),
                                 entryXml.toString());

                TypeHandler typeHandler =
                    getRepository().getTypeHandler(output.getEntryType());
                Entry newEntry =
                    typeHandler.createEntry(getRepository().getGUID());
                newEntry.setDate(new Date().getTime());
                newEntry.setName(file.getName());
                newEntry.setResource(new Resource(file, Resource.TYPE_FILE));
                if (info.getPublish()) {
                    getEntryManager().processEntryPublish(request, files[0],
                            newEntry, entry, "derived from");
                } else {
                    newEntry
                        .setId(getEntryManager().getProcessFileTypeHandler()
                            .getSynthId(getEntryManager().getProcessEntry(),
                                        info.getWorkDir().toString(), file));
                }
                info.addEntry(newEntry);
            }
        }

        return true;
    }


    /**
     * _more_
     *
     * @param outputs _more_
     */
    public void getAllOutputs(List<Command.Output> outputs) {
        if (haveChildren()) {
            for (Command child : children) {
                child.getAllOutputs(outputs);
            }
        }
        outputs.addAll(this.getOutputs());
    }



    /**
     * _more_
     *
     * @param s _more_
     *
     * @return _more_
     */
    public String macro(String s) {
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
    public String applyMacros(Entry entry, File workDir, String value,
                              boolean forDisplay) {

        value = value.replace(macro("workdir"), forDisplay
                ? "&lt;working directory&gt;"
                : workDir.toString());
        if (entry != null) {
            String fileTail = getStorageManager().getFileTail(entry);
            value = value.replace(macro("entry.id"), entry.getId());
            value = value.replace(macro("entry.file"), forDisplay
                    ? getStorageManager().getFileTail(entry)
                    : entry.getResource().getPath());
            //? not sure what the macros should be
            //            value = value.replace(macro("entry.filebase"),
            //                                  IOUtil.stripExtension(entry.getName()));
            value = value.replace(macro("entry.filebase"),
                                  IOUtil.stripExtension(fileTail));
        }

        return value;
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
         * _more_
         *
         * @return _more_
         */
        public String getFilename() {
            return filename;
        }


        /**
         *  Get the Pattern property.
         *
         *  @return The Pattern
         */
        public String getPattern() {
            return pattern;
        }

        /**
         * _more_
         *
         * @return _more_
         */
        public boolean getShowResults() {
            return showResults;
        }

        /**
         * _more_
         *
         * @return _more_
         */
        public boolean getUseStdout() {
            return useStdout;
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
        private static final String TYPE_INT = "int";

        /** _more_ */
        private static final String TYPE_FLOAT = "float";

        /** _more_ */
        private static final String TYPE_ENTRY = "entry";

        /** _more_ */
        private static final String TYPE_FLAG = "flag";

        /** _more_ */
        private static final String TYPE_FILE = "file";

        /** _more_ */
        private static final String TYPE_CATEGORY = "category";

        /** _more_ */
        private Command command;

        /** _more_ */
        private String name;

        /** _more_ */
        private String value;

        /** _more_ */
        private String prefix;

        /** _more_ */
        private String group;

        /** _more_ */
        private boolean nameDefined = false;

        /** _more_ */
        private boolean ifDefined = true;

        /** _more_ */
        private String label;

        /** _more_ */
        private String help;

        /** _more_ */
        private String type;

        /** _more_ */
        private String placeHolder;

        /** _more_ */
        private String entryType;

        /** _more_          */
        private String entryPattern;

        /** _more_ */
        private boolean isPrimaryEntry = false;

        /** _more_ */
        private boolean required = false;

        /** _more_ */
        private String fileName;

        /** _more_ */
        private int size;


        /** _more_ */
        private List<TwoFacedObject> values = new ArrayList<TwoFacedObject>();


        /**
         * _more_
         *
         *
         * @param command _more_
         * @param node _more_
         * @param idx _more_
         *
         * @throws Exception _more_
         */
        public Arg(Command command, Element node, int idx) throws Exception {
            this.command = command;

            type = XmlUtil.getAttribute(node, ATTR_TYPE, (String) null);
            entryType = XmlUtil.getAttribute(node, ATTR_ENTRY_TYPE,
                                             (String) null);

            entryPattern = XmlUtil.getAttribute(node, ATTR_ENTRY_PATTERN,
                    (String) null);

            placeHolder = XmlUtil.getAttribute(node, "placeHolder",
                    (String) null);
            isPrimaryEntry = XmlUtil.getAttribute(node, ATTR_PRIMARY,
                    isPrimaryEntry);

            prefix      = XmlUtil.getAttribute(node, "prefix", (String) null);
            value       = Utils.getAttributeOrTag(node, "value", "");
            name        = XmlUtil.getAttribute(node, ATTR_NAME,
                    (String) null);
            nameDefined = name != null;
            if ((name == null) && (prefix != null)) {
                name = prefix.replaceAll("-", "");
            }
            if ((name == null) && Utils.stringDefined(value)) {
                name = value.replaceAll("-", "");
            }
            if (name == null) {
                name = "arg" + idx;
            }


            group    = XmlUtil.getAttribute(node, ATTR_GROUP, (String) null);
            required = XmlUtil.getAttribute(node, "required", required);
            label    = XmlUtil.getAttribute(node, ATTR_LABEL, name);
            help     = Utils.getAttributeOrTag(node, "help", "");
            fileName = XmlUtil.getAttribute(node, "filename", "${src}");
            if (isInt()) {
                size = 5;
            } else if (isFloat()) {
                size = 10;
            } else {
                size = 24;
            }
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
                }
                values.add(new TwoFacedObject(label, value));
            }
        }

        /**
         * _more_
         *
         * @return _more_
         */
        public String getUrlArg() {
            return command.getUrlArg() + "_" + name;
        }


        /**
         * _more_
         *
         * @param entry _more_
         *
         * @return _more_
         */
        public boolean isApplicable(Entry entry) {
            if (entryType != null) {
                return entry.getTypeHandler().isType(entryType);
            }
            if (entryPattern != null) {
                return entry.getResource().getPath().matches(entryPattern);
            }

            return true;
        }


        /**
         * _more_
         *
         * @return _more_
         */
        public boolean isRequired() {
            return required;
        }


        /**
         * _more_
         *
         * @return _more_
         */
        public String toString() {
            return getName() + " " + getLabel();
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
        public boolean getIfDefined() {
            return ifDefined;
        }


        /**
         * _more_
         *
         * @return _more_
         */
        public boolean isEnumeration() {
            return (type != null) && type.equals(TYPE_ENUMERATION);
        }

        /**
         * _more_
         *
         * @return _more_
         */
        public boolean isFlag() {
            return (type != null) && type.equals(TYPE_FLAG);
        }



        /**
         * _more_
         *
         * @return _more_
         */
        public boolean isFile() {
            return (type != null) && type.equals(TYPE_FILE);
        }

        /**
         * _more_
         *
         * @return _more_
         */
        public boolean isEntry() {
            return (type != null) && type.equals(TYPE_ENTRY);
        }

        /**
         * _more_
         *
         * @return _more_
         */
        public boolean isInt() {
            return (type != null) && type.equals(TYPE_INT);
        }

        /**
         * _more_
         *
         * @return _more_
         */
        public boolean isFloat() {
            return (type != null) && type.equals(TYPE_FLOAT);
        }

        /**
         * _more_
         *
         * @return _more_
         */
        public boolean isPrimaryEntry() {
            return isPrimaryEntry;
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
            if (group == null) {
                return null;
            }

            return command.getUrlArg() + "_" + group;
        }

        /**
         * _more_
         *
         * @return _more_
         */
        public String getPrefix() {
            return prefix;
        }


        /**
         * _more_
         *
         * @return _more_
         */
        public String getEntryType() {
            return entryType;
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
        public String getHelp() {
            return help;
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
     * Class description
     *
     *
     * @version        $version$, Mon, Sep 15, '14
     * @author         Enter your name here...
     */
    public static class CommandInfo {

        /** _more_ */
        private File workDir;

        /** _more_ */
        private boolean forDisplay = false;

        /** _more_          */
        private boolean publish = false;

        /** _more_ */
        private StringBuffer results;

        /** _more_ */
        private StringBuffer error;

        /** _more_ */
        private List<Entry> entries = new ArrayList<Entry>();

        /** _more_          */
        private boolean resultsShownAsText = false;

        /** _more_          */
        private Hashtable<String, String> params = new Hashtable<String,
                                                       String>();

        /**
         * _more_
         *
         * @param commandInfo _more_
         */
        public CommandInfo(CommandInfo commandInfo) {
            this.workDir            = commandInfo.workDir;
            this.forDisplay         = commandInfo.forDisplay;
            this.publish            = commandInfo.publish;
            this.results            = commandInfo.results;
            this.error              = commandInfo.error;
            this.entries            = commandInfo.entries;
            this.resultsShownAsText = commandInfo.resultsShownAsText;
        }

        /**
         * _more_
         *
         * @param workDir _more_
         * @param forDisplay _more_
         */
        public CommandInfo(File workDir, boolean forDisplay) {
            this.workDir    = workDir;
            this.forDisplay = forDisplay;
            this.results    = new StringBuffer();
            this.error      = new StringBuffer();
        }


        /**
         * _more_
         *
         * @param key _more_
         * @param value _more_
         */
        public void addParam(String key, String value) {
            params.put(key, value);
        }

        /**
         * _more_
         *
         * @return _more_
         */
        public Hashtable<String, String> getParams() {
            return params;
        }

        /**
         * _more_
         *
         * @param entry _more_
         */
        public void addEntry(Entry entry) {
            entries.add(entry);
        }

        /**
         * _more_
         *
         * @return _more_
         */
        public List<Entry> getEntries() {
            return entries;
        }

        /**
         * _more_
         *
         * @param entries _more_
         */
        public void setEntries(List<Entry> entries) {
            this.entries = entries;
        }

        /**
         * _more_
         *
         * @return _more_
         */
        public StringBuffer getResults() {
            return results;
        }

        /**
         * _more_
         *
         * @return _more_
         */
        public StringBuffer getError() {
            return error;
        }

        /**
         * Set the Publish property.
         *
         * @param value The new value for Publish
         */
        public void setPublish(boolean value) {
            publish = value;
        }

        /**
         * Get the Publish property.
         *
         * @return The Publish
         */
        public boolean getPublish() {
            return publish;
        }


        /**
         * Set the ResultsShownAsText property.
         *
         * @param value The new value for ResultsShownAsText
         */
        public void setResultsShownAsText(boolean value) {
            resultsShownAsText = value;
        }

        /**
         * Get the ResultsShownAsText property.
         *
         * @return The ResultsShownAsText
         */
        public boolean getResultsShownAsText() {
            return resultsShownAsText;
        }



        /**
         * _more_
         *
         * @return _more_
         */
        public File getWorkDir() {
            return workDir;
        }

        /**
         * _more_
         *
         * @return _more_
         */
        public boolean getForDisplay() {
            return forDisplay;
        }

    }

}
