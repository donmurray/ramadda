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

package org.ramadda.data.process;


import org.ramadda.repository.*;
import org.ramadda.repository.auth.*;
import org.ramadda.repository.job.JobManager;
import org.ramadda.repository.output.OutputHandler;
import org.ramadda.repository.type.*;
import org.ramadda.util.HtmlUtils;
import org.ramadda.util.Utils;




import org.w3c.dom.*;

import ucar.unidata.util.IOUtil;
import ucar.unidata.util.Misc;
import ucar.unidata.util.StringUtil;
import ucar.unidata.util.TwoFacedObject;
import ucar.unidata.xml.XmlUtil;

import java.io.*;

import java.lang.reflect.*;

import java.net.*;

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
public class Service extends RepositoryManager {

    /** _more_ */
    private static ServiceUtil dummyToForceCompile;

    /** _more_ */
    public static boolean debug = false;

    /** _more_ */
    public static final String TAG_ARG = "arg";

    /** _more_ */
    public static final String ATTR_COMMAND = "command";

    /** _more_ */
    public static final String ATTR_CLEANUP = "cleanup";

    /** _more_ */
    public static final String ATTR_LINK = "link";

    /** _more_ */
    public static final String ATTR_ID = "id";

    /** _more_ */
    public static final String ATTR_PRIMARY = "primary";

    /** _more_ */
    public static final String ATTR_ENTRY_TYPE = "entryType";

    /** _more_ */
    public static final String ATTR_ENTRY_PATTERN = "entryPattern";

    /** _more_ */
    public static final String ATTR_ICON = "icon";

    /** _more_ */
    public static final String TAG_OUTPUT = "output";

    /** _more_ */
    public static final String TAG_INPUT = "input";

    /** _more_ */
    public static final String TAG_SERVICE = "service";


    /** _more_ */
    public static final String ATTR_CATEGORY = "category";

    /** _more_ */
    public static final String ATTR_VALUES = "values";

    /** _more_ */
    public static final String ATTR_LABEL = "label";

    /** _more_ */
    public static final String ATTR_HELP = "help";

    /** _more_ */
    public static final String ATTR_SERIAL = "serial";

    /** _more_ */
    public static final String ATTR_GROUP = "group";

    /** _more_ */
    public static final String ATTR_SERVICE = "service";

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

    /** _more_ */
    private boolean cleanup = false;

    /** _more_ */
    private String command;

    /** _more_ */
    private Object commandObject;

    /** _more_ */
    private Method commandMethod;

    /** _more_ */
    private String help;

    /** _more_          */
    private String processDesc;

    /** _more_ */
    private String label;

    /** _more_ */
    private String pathProperty;

    /** _more_ */
    private Service parent;

    /** _more_ */
    private List<Service> children;

    /** _more_ */
    public boolean serial;

    /** _more_ */
    private String linkId;

    /** _more_ */
    private Service link;

    /** _more_ */
    private List<Arg> args = new ArrayList<Arg>();

    /** _more_ */
    private List<Arg> inputs = new ArrayList<Arg>();

    /** _more_ */
    private List<OutputDefinition> outputs =
        new ArrayList<OutputDefinition>();



    /**
     * _more_
     *
     * @param repository _more_
     * @param element _more_
     * @throws Exception _more_
     */
    public Service(Repository repository, Element element) throws Exception {
        super(repository);
        init(null, element, null);
    }

    /**
     * _more_
     *
     * @param repository _more_
     * @param id _more_
     * @param label _more_
     */
    public Service(Repository repository, String id, String label) {
        super(repository);
        this.id    = id;
        this.label = label;
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
    public Service(Repository repository, Service parent, Element element,
                   int index)
            throws Exception {
        super(repository);
        init(parent, element, "service_" + index);
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
    private void init(Service parent, Element element, String dfltId)
            throws Exception {

        this.parent = parent;
        id          = XmlUtil.getAttribute(element, ATTR_ID, dfltId);

        if (id == null) {
            throw new IllegalStateException("Service: no id defined in: "
                                            + XmlUtil.toString(element));
        }
        entryType = XmlUtil.getAttribute(element, ATTR_ENTRY_TYPE,
                                         (String) null);

        icon = XmlUtil.getAttributeFromTree(element, ATTR_ICON,
                                            (String) null);
        outputToStderr = XmlUtil.getAttributeFromTree(element,
                "outputToStderr", outputToStderr);

        cleanup = XmlUtil.getAttributeFromTree(element, ATTR_CLEANUP, true);
        linkId = XmlUtil.getAttribute(element, ATTR_LINK, (String) null);
        help   = XmlUtil.getGrandChildText(element, ATTR_HELP, "");
        processDesc = XmlUtil.getGrandChildText(element,
                "process_description", "");
        label  = XmlUtil.getAttribute(element, ATTR_LABEL, (String) null);
        serial = XmlUtil.getAttribute(element, ATTR_SERIAL, true);

        NodeList nodes;

        nodes = XmlUtil.getElements(element, TAG_SERVICE);
        for (int i = 0; i < nodes.getLength(); i++) {
            Element node = (Element) nodes.item(i);
            addChild(new Service(getRepository(), this, node, i));
        }

        if (linkId != null) {
            initService();
        } else if (children == null) {
            command = XmlUtil.getAttributeFromTree(element, ATTR_COMMAND,
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
                System.err.println("Service: no command defined:" + command
                                   + " path:" + pathProperty);

                return;
            }
        }

        //Look for:
        //java:<class>:<method>
        if ((command != null) && command.equals("util")) {
            command = "java:org.ramadda.data.process.ServiceUtil:evaluate";
        }

        if ((command != null) && command.startsWith("java:")) {
            List<String> toks      = StringUtil.split(command, ":");
            String       className = toks.get(1);
            if (className.trim().length() == 0) {
                className = "org.ramadda.repository.job.ServiceUtil";
            }
            commandObject = Misc.findClass(className).newInstance();
            Class[] paramTypes = new Class[] { Request.class, Entry.class,
                    Service.class, ServiceInput.class, List.class };
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
            Element          node   = (Element) nodes.item(i);
            OutputDefinition output = new OutputDefinition(node);
            outputs.add(output);
        }
        enabled = true;

    }

    /**
     * _more_
     *
     *
     * @param service _more_
     */
    private void addChild(Service service) {
        if (children == null) {
            children = new ArrayList<Service>();
        }
        children.add(service);
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
    private void initService() {
        if (link != null) {
            return;
        }
        if (linkId != null) {
            link = getRepository().getJobManager().getService(linkId);
        }
    }

    /**
     * _more_
     *
     * @return _more_
     */
    public Service getServiceToUse() {
        initService();
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
     * @param input _more_
     * @param commands _more_
     *
     * @throws Exception _more_
     */
    public void addArgs(Request request, Entry primaryEntry,
                        ServiceInput input, List<String> commands)
            throws Exception {
        getServiceToUse().addArgsInner(request, primaryEntry, input,
                                       commands);
    }


    /**
     * _more_
     *
     * @param request _more_
     * @param primaryEntry _more_
     * @param input _more_
     * @param commands _more_
     *
     * @throws Exception _more_
     */
    private void addArgsInner(Request request, Entry primaryEntry,
                              ServiceInput input, List<String> commands)
            throws Exception {

        File workDir = input.getProcessDir();
        String cmd = applyMacros(primaryEntry, workDir, getCommand(),
                                 input.getForDisplay());
        commands.add(cmd);
        HashSet<String> seenGroup   = new HashSet<String>();
        HashSet<String> definedArgs = new HashSet<String>();

        for (Service.Arg arg : getArgs()) {
            if (arg.depends != null) {
                if ( !definedArgs.contains(arg.depends)) {
                    //                    System.err.println("Dependency:" + arg.getName() + " " + arg.depends);
                    continue;
                }
            }

            Entry currentEntry = primaryEntry;
            if (arg.getCategory() != null) {
                continue;
            }
            String       argValue = null;
            String       argKey   = arg.getUrlArg();
            List<String> values   = null;
            if (arg.isValueArg()) {
                argValue = arg.getValue();
            } else if (arg.isFlag()) {
                if (arg.getGroup() != null) {
                    if ( !seenGroup.contains(arg.getGroup())) {
                        argValue = request.getString(arg.getGroup(), null);
                        if (Utils.stringDefined(argValue)) {
                            seenGroup.add(arg.getGroup());
                        } else {
                            argValue = null;
                        }
                        argKey = arg.getGroup();
                    }
                } else if (request.get(arg.getUrlArg(), false)) {
                    argValue = arg.getValue();
                }
            } else if (arg.isFile()) {
                //TODO:
                //                String filename = applyMacros(currentEntry, workDir,
                //arg.getFileName(), input.getForDisplay());
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
                    input.addParam(arg.getUrlArg() + "_hidden",
                                   entryArg.getId());
                }
                argValue = arg.getValue();
                if (argValue.equals("${entry.file}")) {
                    argValue = null;
                }
                if ( !Utils.stringDefined(argValue)) {
                    if (arg.copy) {
                        File newFile =
                            new File(
                                IOUtil.joinDir(
                                    input.getProcessDir(),
                                    getStorageManager().getFileTail(
                                        currentEntry)));
                        if ( !newFile.exists()) {
                            IOUtil.copyFile(currentEntry.getFile(), newFile);
                        }
                        argValue = newFile.toString();
                    } else {
                        argValue = currentEntry.getResource().getPath();
                    }
                }
            } else {
                if (arg.isMultiple()) {
                    values = request.get(arg.getUrlArg(),
                                         new ArrayList<String>());
                    if (arg.multipleJoin != null) {
                        argValue = StringUtil.join(arg.multipleJoin, values);
                        values   = null;
                    }
                } else {
                    argValue = request.getString(arg.getUrlArg(), "");
                }
            }
            if ((values == null) && (argValue != null)) {
                values = new ArrayList<String>();
                values.add(argValue);
            }


            int argCnt = 0;
            if (values != null) {
                for (String value : values) {
                    if (arg.getIfDefined() && !Utils.stringDefined(value)) {
                        continue;
                    }
                    argCnt++;
                    input.addParam(argKey, value);
                    if ( !arg.isEntry() && Utils.stringDefined(arg.value)) {
                        value = arg.value.replace("${value}", value);
                    }
                    if (Utils.stringDefined(value) || arg.isRequired()) {
                        if (Utils.stringDefined(arg.prefix)) {
                            commands.add(arg.prefix);
                        }

                        value = applyMacros(currentEntry, workDir, value,
                                            input.getForDisplay());
                        commands.add(value);
                    }
                }
            }

            if (argCnt != 0) {
                definedArgs.add(arg.getName());
            }
            if ((argCnt == 0) && arg.isRequired()) {
                throw new IllegalArgumentException("No entry  specified for:"
                        + arg.getLabel());
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
        initService();
        if (link != null) {
            return link.getId();
        }

        return id;
    }


    /**
     * _more_
     *
     * @param request _more_
     * @param js _more_
     * @param formVar _more_
     *
     * @throws Exception _more_
     */
    public void initFormJS(Request request, Appendable js, String formVar)
            throws Exception {}


    /**
     * _more_
     *
     * @param request _more_
     * @param input _more_
     * @param sb _more_
     *
     * @return _more_
     *
     * @throws Exception _more_
     */
    public int addToForm(Request request, ServiceInput input, Appendable sb)
            throws Exception {
        initService();
        if (link != null) {
            return link.addToForm(request, input, sb);
        }

        if (haveChildren()) {
            int cnt = 0;
            for (Service child : children) {
                StringBuilder tmpSB = new StringBuilder();
                int blockCnt        = child.addToForm(request, input, tmpSB);
                cnt += blockCnt;
                if (blockCnt > 0) {
                    sb.append(HtmlUtils.p());
                    sb.append(header(child.getLabel()));
                    sb.append(tmpSB);
                }

            }

            return cnt;
        }

        return addToFormInner(request, input, sb);
    }

    /**
     * _more_
     *
     * @param request _more_
     * @param input _more_
     * @param sb _more_
     *
     * @return _more_
     *
     * @throws Exception _more_
     */
    private int addToFormInner(Request request, ServiceInput input,
                               Appendable sb)
            throws Exception {

        StringBuilder formSB      = new StringBuilder();
        int           blockCnt    = 0;
        CatBuff       catBuff     = null;
        Service.Arg   catArg      = null;
        boolean       anyRequired = false;
        for (Service.Arg arg : getArgs()) {
            if (arg.isCategory()) {
                if ((catBuff != null) && (catBuff.length() > 0)) {
                    processCatBuff(request, formSB, catArg, catBuff,
                                   ++blockCnt);
                }
                catArg  = arg;
                catBuff = new CatBuff();

                continue;
            }

            if (arg.isValueArg()) {
                continue;
            }

            if (catBuff == null) {
                catBuff = new CatBuff();
                catArg  = null;
            }

            if (arg.isRequired()) {
                anyRequired = true;
            }
            addArgToForm(request, input, catBuff, arg);
        }

        if ((catBuff != null) && (catBuff.length() > 0)) {
            processCatBuff(request, formSB, catArg, catBuff, ++blockCnt);
        }
        if (anyRequired) {
            formSB.append(
                "<span class=ramadda-required-label>* required</span>");
        }

        sb.append(HtmlUtils.p());
        if (Utils.stringDefined(getHelp())) {
            sb.append(HtmlUtils.div(getHelp(),
                                    HtmlUtils.cssClass("service-help")));
        }
        if (blockCnt > 0) {
            sb.append(HtmlUtils.div(formSB.toString(),
                                    HtmlUtils.cssClass("service-form")));
        }


        return blockCnt;

    }


    /**
     * _more_
     *
     * @param request _more_
     * @param input _more_
     * @param catBuff _more_
     * @param arg _more_
     *
     * @throws Exception _more_
     */
    public void addArgToForm(Request request, ServiceInput input,
                             CatBuff catBuff, Arg arg)
            throws Exception {

        String        tooltip   = arg.getPrefix();
        StringBuilder inputHtml = new StringBuilder();
        if (arg.isEnumeration()) {
            List<TwoFacedObject> values = arg.getValues();
            if ((values.size() == 0) && (arg.valuesProperty != null)) {
                values = (List<TwoFacedObject>) input.getProperty(
                    arg.valuesProperty, values);
            }
            if (values.size() == 0) {
                values =
                    (List<TwoFacedObject>) input.getProperty(arg.getUrlArg()
                        + ".values", values);
            }

            if (arg.addAll) {
                values = new ArrayList<TwoFacedObject>(values);
                values.add(0, new TwoFacedObject("--all--", ""));
            } else if (arg.addNone) {
                values = new ArrayList<TwoFacedObject>(values);
                values.add(0, new TwoFacedObject("--none--", ""));
            }

            String extra = "";
            if (arg.isMultiple()) {
                extra = " MULTIPLE SIZE=" + ((arg.getSize() > 0)
                                             ? arg.getSize()
                                             : 4) + " ";
            }
            inputHtml.append(HtmlUtils.select(arg.getUrlArg(), values,
                    (List) null, extra, 100));
        } else if (arg.isFlag()) {
            if (arg.getGroup() != null) {
                boolean selected = request.getString(arg.getGroup(),
                                       "").equals(arg.getValue());
                inputHtml.append(HtmlUtils.radio(arg.getGroup(),
                        arg.getValue(), selected));
            } else {
                inputHtml.append(HtmlUtils.checkbox(arg.getUrlArg(), "true",
                        request.get(arg.getUrlArg(), false)));
            }

            inputHtml.append(HtmlUtils.space(2));
            inputHtml.append(arg.getHelp());
            if (arg.sameRow) {
                catBuff.appendToCurrentRow(inputHtml.toString());
            } else {
                catBuff.addRow("", inputHtml.toString(), null);
            }

            return;
        } else if (arg.isFile()) {
            //noop
        } else if (arg.isEntry()) {
            List<Entry> entries      = input.getEntries();
            Entry       primaryEntry = ((entries.size() == 0)
                                        ? null
                                        : entries.get(0));

            if ((primaryEntry != null) && arg.isPrimaryEntry()) {
                return;
            } else {
                if (arg.getEntryType() != null) {
                    request.put(ARG_ENTRYTYPE, arg.getEntryType());
                }
                inputHtml.append(OutputHandler.getSelect(request,
                        arg.getUrlArg(), msg("Select"), true, null));
                inputHtml.append(HtmlUtils.hidden(arg.getUrlArg()
                        + "_hidden", request.getString(arg.getUrlArg()
                            + "_hidden", ""), HtmlUtils.id(arg.getUrlArg()
                                         + "_hidden")));
                inputHtml.append(HtmlUtils.space(1));
                inputHtml.append(HtmlUtils.disabledInput(arg.getUrlArg(),
                        request.getString(arg.getUrlArg(), ""),
                        HtmlUtils.SIZE_60 + HtmlUtils.id(arg.getUrlArg())));
                request.remove(ARG_ENTRYTYPE);
            }

        } else {
            String extra = HtmlUtils.attr(HtmlUtils.ATTR_SIZE,
                                          "" + arg.getSize());
            if (arg.placeHolder != null) {
                extra += HtmlUtils.attr("placeholder", arg.placeHolder);
            }
            inputHtml.append(
                HtmlUtils.input(
                    arg.getUrlArg(), request.getString(arg.getUrlArg(), ""),
                    extra));
        }
        if (inputHtml.length() == 0) {
            return;
        }
        if (arg.isRequired()) {
            inputHtml.append(HtmlUtils.space(1));
            inputHtml.append("<span class=ramadda-required-label>*</span>");
        }

        if (arg.sameRow) {
            catBuff.appendToCurrentRow(inputHtml.toString());
        } else {
            catBuff.addRow(arg.getLabel(), inputHtml.toString(),
                           arg.getHelp());
        }
        //        makeFormEntry(catBuff, arg.getLabel(), inputHtml.toString(), arg.getHelp());


    }


    /**
     * _more_
     *
     * @param sb _more_
     * @param label _more_
     * @param col1 _more_
     * @param help _more_
     *
     * @throws Exception _more_
     */
    private void makeFormEntry(Appendable sb, String label, String col1,
                               String help)
            throws Exception {
        String extra = "";
        if (help != null) {
            extra = HtmlUtils.div(help,
                                  HtmlUtils.cssClass("service-form-help"));
        }
        sb.append(HtmlUtils.formEntryTop(Utils.stringDefined(label)
                                         ? msgLabel(label)
                                         : "", col1, extra));
    }


    /**
     * _more_
     *
     * @param request _more_
     * @param sb _more_
     * @param catArg _more_
     * @param catBuff _more_
     * @param blockCnt _more_
     *
     * @throws Exception _more_
     */
    private void processCatBuff(Request request, Appendable sb,
                                Service.Arg catArg, CatBuff catBuff,
                                int blockCnt)
            throws Exception {
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
        StringBuilder formSB = new StringBuilder(HtmlUtils.formTable());
        catBuff.addToForm(formSB);
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
            return getServiceToUse().isEnabled();
        }
        if (haveChildren()) {
            for (Service child : children) {
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
     * @param args _more_
     */
    public void collectArgs(List<Arg> args) {
        if (linkId != null) {
            getServiceToUse().collectArgs(args);

            return;
        }
        if (haveChildren()) {
            for (Service child : children) {
                child.collectArgs(args);
            }
        }

        args.addAll(this.args);
    }



    /**
     * _more_
     *
     * @return _more_
     */
    public boolean getOutputToStderr() {
        if (linkId != null) {
            return getServiceToUse().getOutputToStderr();
        }

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
    public List<OutputDefinition> getOutputs() {
        if (linkId != null) {
            return getServiceToUse().getOutputs();
        }

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
     * Can we handle this type of ServiceInput?
     *
     * @param dpi ServiceInput
     * @return true if we can handle
     */
    public boolean canHandle(ServiceInput dpi) {
        List<Entry> entries = dpi.getEntries();
        if (entries.size() == 0) {
            return false;
        }

        return isApplicable(entries.get(0));
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
            return getServiceToUse().isApplicable(entry);
        }
        if (haveChildren()) {
            return children.get(0).isApplicable(entry);
        }

        for (Arg input : inputs) {
            boolean debug =false;
            if (input.isApplicable(entry, debug)) {
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
            getServiceToUse().getCommand();
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
            getServiceToUse().getHelp();
        }

        return help;
    }

    /**
     * _more_
     *
     * @return _more_
     */
    public String getProcessDescription() {
        if (linkId != null) {
            getServiceToUse().getProcessDescription();
        }

        return processDesc;
    }

    /**
     * _more_
     *
     * @return _more_
     */
    public String getLabel() {
        initService();
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
     * @param input _more_
     *
     * @return _more_
     *
     * @throws Exception _more_
     */
    public ServiceOutput evaluate(Request request, ServiceInput input)
            throws Exception {

        ServiceOutput myOutput      = new ServiceOutput();

        HashSet<File> existingFiles = new HashSet<File>();
        for (File f : input.getProcessDir().listFiles()) {
            existingFiles.add(f);
        }
        Entry         entry    = input.getEntries().get(0);

        HashSet<File> newFiles = new HashSet<File>();
        if (haveChildren()) {
            ServiceOutput childOutput = null;
            ServiceInput  childInput  = input;
            for (Service child : children) {
                Entry primaryEntryForChild = entry;
                if (serial) {
                    if (childOutput != null) {
                        List<Entry> lastEntries = childOutput.getEntries();
                        if ((lastEntries != null)
                                && (lastEntries.size() > 0)) {
                            primaryEntryForChild = lastEntries.get(0);
                        }
                    }
                }

                childOutput = child.evaluate(request, childInput);
                if ( !childOutput.isOk()) {
                    return childOutput;
                }
                if ( !serial) {
                    if (childOutput.getResultsShownAsText()) {
                        myOutput.setResultsShownAsText(true);
                    }
                    myOutput.getEntries().addAll(childOutput.getEntries());
                    myOutput.append(childOutput.getResults());
                } else {
                    childInput = childInput.makeInput(childOutput);

                }
            }

            //If we are  serial then we only add the last command's entry (or add them all?)
            if (serial && (childOutput != null)) {
                myOutput.getEntries().addAll(childOutput.getEntries());
                if (childOutput.getResultsShownAsText()) {
                    myOutput.setResultsShownAsText(true);
                }
                myOutput.append(childOutput.getResults());
            }

            for (Entry newEntry : myOutput.getEntries()) {
                newFiles.add(newEntry.getFile());
            }
            if (cleanup) {
                for (File f : input.getProcessDir().listFiles()) {
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
            if (input.getForDisplay()) {
                return myOutput;
            }

            return myOutput;
        }

        List<String> commands = new ArrayList<String>();
        if (link != null) {
            link.addArgs(request, entry, input, commands);
        } else {
            this.addArgs(request, entry, input, commands);
        }
        System.err.println("Command:" + commands);

        if (input.getForDisplay()) {
            commands.set(0, IOUtil.getFileTail(commands.get(0)));
            myOutput.append(HtmlUtils.pre(StringUtil.join(" ", commands)));
            myOutput.append("\n");

            return myOutput;
        }

        String errMsg = "";
        String outMsg = "";
        File stdoutFile = new File(IOUtil.joinDir(input.getProcessDir(),
                              "." + getId() + ".stdout"));
        File stderrFile = new File(IOUtil.joinDir(input.getProcessDir(),
                              "." + getId() + ".stderr"));


        if (commandObject != null) {
            commandMethod.invoke(commandObject, new Object[] { request, entry,
                    this, input, commands });
        } else {
            JobManager.CommandResults results =
                getRepository().getJobManager().executeCommand(commands,
                    null, input.getProcessDir(), -1,
                    new PrintWriter(stdoutFile), new PrintWriter(stderrFile));
        }
        if (stderrFile.exists()) {
            errMsg = IOUtil.readContents(stderrFile);
        }
        if (Utils.stringDefined(errMsg)) {
            if (getOutputToStderr()) {
                myOutput.append(errMsg);
                myOutput.append("\n");
            } else {
                //If there is an error then
                myOutput.setOk(false);
                myOutput.append(errMsg);

                return myOutput;
            }
        }

        boolean       setResultsFromStdout = true;

        HashSet<File> seen                 = new HashSet<File>();


        for (OutputDefinition output : getOutputs()) {
            if (output.getShowResults()) {
                setResultsFromStdout = false;
                myOutput.setResultsShownAsText(true);
                if (output.getUseStdout()) {
                    myOutput.append(IOUtil.readContents(stdoutFile));
                } else {}

                continue;
            }

            File[] files = null;
            if (output.getUseStdout()) {
                setResultsFromStdout = false;
                String filename = applyMacros(entry, input.getProcessDir(),
                                      output.getFilename(),
                                      input.getForDisplay());
                File destFile =
                    new File(IOUtil.joinDir(input.getProcessDir(), filename));
                IOUtil.moveFile(stdoutFile, destFile);
                files = new File[] { destFile };
            }
            final String thePattern = output.getPattern();
            if (files == null) {
                files = input.getProcessDir().listFiles(new FileFilter() {
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
                StringBuilder entryXml = new StringBuilder();
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
                if (input.getPublish()) {
                    getEntryManager().processEntryPublish(request, files[0],
                            newEntry, entry, "derived from");
                } else {
                    newEntry
                        .setId(getEntryManager().getProcessFileTypeHandler()
                            .getSynthId(getEntryManager().getProcessEntry(),
                                        input.getProcessDir().toString(),
                                        file));
                }
                myOutput.addEntry(newEntry);
            }
        }

        if (setResultsFromStdout) {
            myOutput.append(IOUtil.readContents(stdoutFile));
        }

        return myOutput;
    }


    /**
     * _more_
     *
     * @param outputs _more_
     */
    public void getAllOutputs(List<OutputDefinition> outputs) {
        if (haveChildren()) {
            for (Service child : children) {
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
     * @version        $version$, Tue, Sep 23, '14
     * @author         Enter your name here...
     */
    private class CatBuff {

        /** _more_ */
        List<List<StringBuilder>> rows = new ArrayList<List<StringBuilder>>();

        /**
         * _more_
         *
         * @param label _more_
         * @param value _more_
         * @param value2 _more_
         *
         * @return _more_
         */
        public List<StringBuilder> addRow(String label, String value,
                                          String value2) {
            List<StringBuilder> row = new ArrayList<StringBuilder>();
            row.add(new StringBuilder(label));
            row.add(new StringBuilder(value));
            if (value2 != null) {
                row.add(new StringBuilder(value2));
            }
            rows.add(row);

            return row;
        }

        /**
         * _more_
         *
         * @return _more_
         */
        public int length() {
            return rows.size();
        }

        /**
         * _more_
         *
         * @param row _more_
         */
        public void addRow(List<StringBuilder> row) {
            rows.add(row);
        }

        /**
         * _more_
         *
         * @param v _more_
         */
        public void appendToCurrentRow(String v) {
            List<StringBuilder> row = rows.get(rows.size() - 1);
            row.get(1).append(v);
        }



        /**
         * _more_
         *
         * @param sb _more_
         *
         * @throws Exception _more_
         */
        public void addToForm(Appendable sb) throws Exception {
            for (List<StringBuilder> row : rows) {
                String label  = row.get(0).toString();
                String value  = row.get(1).toString();
                String value2 = ((row.size() > 2)
                                 ? row.get(2).toString()
                                 : null);
                makeFormEntry(sb, label, value, value2);
            }
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
        private Service command;

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
        private boolean multiple = false;

        /** _more_ */
        private String multipleJoin;

        /** _more_ */
        private boolean sameRow = false;


        /** _more_ */
        private String label;

        /** _more_ */
        private String help;

        /** _more_ */
        private String type;



        /** _more_ */
        private String depends;

        /** _more_ */
        private boolean addAll;

        /** _more_ */
        private boolean addNone;

        /** _more_ */
        private String placeHolder;

        /** _more_ */
        private String entryType;

        /** _more_ */
        private String entryPattern;

        /** _more_ */
        private boolean isPrimaryEntry = false;

        /** _more_ */
        private boolean required = false;

        /** _more_          */
        private boolean copy = false;

        /** _more_ */
        private String fileName;

        /** _more_ */
        private int size;


        /** _more_ */
        private List<TwoFacedObject> values = new ArrayList<TwoFacedObject>();

        /** _more_ */
        private String valuesProperty;


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
        public Arg(Service command, Element node, int idx) throws Exception {
            this.command = command;

            type = XmlUtil.getAttribute(node, ATTR_TYPE, (String) null);
            depends = XmlUtil.getAttribute(node, "depends", (String) null);
            addAll       = XmlUtil.getAttribute(node, "addAll", false);
            addNone      = XmlUtil.getAttribute(node, "addNone", false);


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

            group = XmlUtil.getAttribute(node, ATTR_GROUP, (String) null);
            required = XmlUtil.getAttribute(node, "required", required);
            copy     = XmlUtil.getAttribute(node, "copy", false);
            valuesProperty = XmlUtil.getAttribute(node, "valuesProperty",
                    (String) null);
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
            multiple  = XmlUtil.getAttribute(node, "multiple", multiple);
            multipleJoin = XmlUtil.getAttribute(node, "multipleJoin",
                    (String) null);
            sameRow = XmlUtil.getAttribute(node, "sameRow", sameRow);
            size    = XmlUtil.getAttribute(node, "size", size);
            for (String tok :
                    StringUtil.split(Utils.getAttributeOrTag(node,
                        ATTR_VALUES, ""), ",", true, true)) {
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
        public boolean isApplicable(Entry entry, boolean debug) {
            boolean defaultReturn = true;

            if (entryType != null) {
                if ( !entry.getTypeHandler().isType(entryType)) {
                    return false;
                }
                if (entryPattern== null) {
                    return true;
                }
            }
            if (entryPattern != null) {
                return entry.getResource().getPath().matches(entryPattern);
            }

            return defaultReturn;
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
        public boolean isMultiple() {
            return multiple;
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


}
