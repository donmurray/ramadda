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

    /** _more_ */
    public static final String TAG_ARG = "arg";

    /** _more_ */
    public static final String ATTR_ID = "id";

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

    /** _more_          */
    private boolean outputToStderr = false;


    /** _more_ */
    private String command;


    /** _more_ */
    private String help;

    /** _more_ */
    private String label;


    /** _more_ */
    private String pathProperty;

    /** _more_ */
    private List<Arg> args = new ArrayList<Arg>();

    /** _more_ */
    private List<Input> inputs = new ArrayList<Input>();

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
        id = XmlUtil.getAttribute(element, ATTR_ID);
        icon = XmlUtil.getAttribute(element, ATTR_ICON, (String) null);
        command = XmlUtil.getAttribute(element, TAG_COMMAND, (String) null);
        outputToStderr = XmlUtil.getAttribute(element, "outputToStderr",
                outputToStderr);
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

        help  = XmlUtil.getGrandChildText(element, "help", "");

        label = XmlUtil.getAttribute(element, ATTR_LABEL, "Command");

        NodeList children = XmlUtil.getElements(element, TAG_ARG);
        for (int i = 0; i < children.getLength(); i++) {
            Element node = (Element) children.item(i);
            Arg     arg  = new Arg(node, i);
            args.add(arg);
        }


        children = XmlUtil.getElements(element, TAG_INPUT);
        for (int i = 0; i < children.getLength(); i++) {
            Element node  = (Element) children.item(i);
            Input   input = new Input(node);
            inputs.add(input);
        }


        children = XmlUtil.getElements(element, TAG_OUTPUT);
        for (int i = 0; i < children.getLength(); i++) {
            Element node   = (Element) children.item(i);
            Output  output = new Output(node);
            outputs.add(output);
        }
        enabled = true;

        getRepository().getJobManager().addCommand(this);
    }

    /**
     * _more_
     *
     * @param request _more_
     * @param entry _more_
     * @param workDir _more_
     * @param commands _more_
     *
     * @throws Exception _more_
     */
    public void addArgs(Request request, Entry entry, File workDir,
                        List<String> commands)
            throws Exception {
        String cmd = applyMacros(entry, workDir, getCommand());
        commands.add(cmd);
        HashSet<String> seenGroup = new HashSet<String>();
        for (Command.Arg arg : getArgs()) {
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
                if (arg.getIfDefined() && !Utils.stringDefined(argValue)) {
                    continue;
                }
                if (Utils.stringDefined(arg.value)) {
                    argValue = arg.value.replace("${value}", argValue);
                }
            }

            if (Utils.stringDefined(argValue)) {
                if (Utils.stringDefined(arg.prefix)) {
                    commands.add(arg.prefix);
                }
                argValue = applyMacros(entry, workDir, argValue);
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
        return icon;
    }

    /**
     * _more_
     *
     * @return _more_
     */
    public String getId() {
        return id;
    }


    /**
     * _more_
     *
     * @param request _more_
     * @param entry _more_
     * @param sb _more_
     *
     * @return _more_
     *
     * @throws Exception _more_
     */
    public int makeForm(Request request, Entry entry, StringBuffer sb)
            throws Exception {

        int          blockCnt = 0;
        StringBuffer catBuff  = null;
        Command.Arg  catArg   = null;
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
                    String label = arg.getLabel();
                    if (Utils.stringDefined(arg.getHelp())) {
                        label += " -- " + arg.getHelp();
                    }
                    input = HtmlUtils.labeledCheckbox(arg.getName(), "true",
                            request.get(arg.getName(), false), label);
                }
                catBuff.append(HtmlUtils.formEntry("", input));

                continue;
            } else if (arg.isFile()) {
                //noop
            } else if (arg.isEntry()) {
                input = OutputHandler.getSelect(
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
            if (Utils.stringDefined(arg.getHelp())) {
                input = input + HtmlUtils.space(2) + arg.getHelp();
            }
            catBuff.append(HtmlUtils.formEntry(msgLabel(arg.getLabel()),
                    input));
        }

        if ((catBuff != null) && (catBuff.length() > 0)) {
            processCatBuff(request, sb, catArg, catBuff, ++blockCnt);
        }

        return blockCnt;
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
        if (blockCnt == 1) {
            sb.append(formSB);
        } else {
            sb.append(HtmlUtils.makeShowHideBlock("More...",
                    formSB.toString(), blockCnt == 1));
        }
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
    public List<Input> getInputs() {
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
        for (Input input : inputs) {
            if (input.isApplicable(entry)) {
                return true;
            }
        }
        //if(children.size()>0) {return children.get(0).isApplicable(entry);}

        return false;
    }


    /**
     * _more_
     *
     * @return _more_
     */
    public String getCommand() {
        return command;
    }

    /**
     * _more_
     *
     * @return _more_
     *
     */
    public String getHelp() {
        return help;
    }

    /**
     * _more_
     *
     * @return _more_
     */
    public String getLabel() {
        return label;
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
     *
     * @return _more_
     */
    public String applyMacros(Entry entry, File workDir, String value) {
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
     * Class description
     *
     *
     * @version        $version$, Thu, Sep 4, '14
     * @author         Enter your name here...
     */
    public static class Input {

        /** _more_ */
        private String entryType;

        /**
         * _more_
         *
         * @param node _more_
         */
        public Input(Element node) {
            entryType = XmlUtil.getAttribute(node, ATTR_TYPE,
                                             TypeHandler.TYPE_FILE);
        }



        /**
         * _more_
         *
         * @param entry _more_
         *
         * @return _more_
         */
        public boolean isApplicable(Entry entry) {
            return entry.isType(entryType);
        }


        /**
         *  Get the EntryType property.
         *
         *  @return The EntryType
         */
        public String getEntryType() {
            return entryType;
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

        /** _more_          */
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
         *
         * @throws Exception _more_
         */
        public Arg(Element node, int idx) throws Exception {
            name = XmlUtil.getAttribute(node, ATTR_NAME, (String) null);
            if (name == null) {
                name        = "arg" + idx;
                nameDefined = false;
            } else {
                nameDefined = true;
            }

            type      = XmlUtil.getAttribute(node, ATTR_TYPE, (String) null);
            group     = XmlUtil.getAttribute(node, ATTR_GROUP, (String) null);
            value     = Utils.getAttributeOrTag(node, "value", "");
            label     = XmlUtil.getAttribute(node, ATTR_LABEL, name);
            prefix    = XmlUtil.getAttribute(node, "prefix", (String) null);
            help      = Utils.getAttributeOrTag(node, "help", "");
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
                }
                values.add(new TwoFacedObject(label, value));
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
