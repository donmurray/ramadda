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

    public static final String ATTR_ICON = "icon";

    /** _more_ */
    public static final String ATTR_VALUES = "values";

    /** _more_ */
    public static final String ATTR_ACTIONLABEL = "actionLabel";

    /** _more_ */
    public static final String ATTR_LABEL = "label";

    /** _more_ */
    public static final String ATTR_COMMAND = "command";

    /** _more_ */
    public static final String ATTR_PATHPROPERTY = "pathProperty";


    /** _more_ */
    private OutputType outputType;

    /** _more_ */
    private String entryType;

    /** _more_          */
    private String destType;

    /** _more_ */
    private String path;

    /** _more_ */
    private String command;

    /** _more_ */
    private String fullPath;

    /** _more_ */
    private String actionLabel;

    /** _more_          */
    private String message;


    /** _more_ */
    private List<Arg> args = new ArrayList<Arg>();

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
        //command is required
        command   = XmlUtil.getAttribute(element, ATTR_COMMAND);
        entryType = XmlUtil.getAttribute(element, ATTR_TYPE, (String) null);
        destType  = XmlUtil.getAttribute(element, "destType", (String) null);


        //path is required
        String pathProperty = XmlUtil.getAttribute(element,
                                  ATTR_PATHPROPERTY);

        path = getProperty(pathProperty, null);

        if (path == null) {
            System.err.println(
                "ExecutableOutputHandler: no path property defined:"
                + pathProperty);

            return;
        }

        fullPath = IOUtil.joinDir(path, command);
        if ( !new File(fullPath).exists()) {
            throw new IllegalArgumentException(
                "ExecutableOutputHandler: command does not exist:"
                + fullPath);
        }
        outputType = new OutputType(XmlUtil.getAttribute(element, ATTR_LABEL,
                "Executable"), XmlUtil.getAttribute(element, ATTR_ID),
                               OutputType.TYPE_OTHER, "",
                               XmlUtil.getAttribute(element, ATTR_ICON,
                                   (String) null));
        actionLabel = XmlUtil.getAttribute(element, ATTR_ACTIONLABEL,
                                           outputType.getLabel());


        message = XmlUtil.getGrandChildText(element, "message", "");
        addType(outputType);
        NodeList children = XmlUtil.getElements(element, TAG_ARG);
        for (int i = 0; i < children.getLength(); i++) {
            Element node = (Element) children.item(i);
            Arg     arg  = new Arg(node);
            args.add(arg);
        }



    }

    /**
     * _more_
     *
     * @return _more_
     */
    public boolean isEnabled() {
        return path != null;
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
     * @param output _more_
     *
     * @return _more_
     */
    public String getMimeType(OutputType output) {
        if (true) {
            return super.getMimeType(output);
        }
        if (output.equals(outputType)) {
            return repository.getMimeTypeFromSuffix(".rss");
        } else {
            return super.getMimeType(output);
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
            sb.append(message);
            makeForm(request, entry, sb);

            return new Result(outputType.getLabel(), sb);
        }



        Object       uniqueId = getRepository().getGUID();
        File         workDir  = getWorkDir(uniqueId);
        List<String> commands = new ArrayList<String>();
        commands.add(fullPath);

        List<File> files    = new ArrayList<File>();
        String     fileTail = getStorageManager().getFileTail(entry);
        for (Arg arg : args) {
            if (arg.isValueArg()) {
                String value = arg.getValue();
                value = value.replace("${workdir}", workDir.toString());
                value = value.replace("${entry.file}",
                                      entry.getResource().getPath());
                value = value.replace("${entry.filebase}",
                                      IOUtil.stripExtension(entry.getName()));
                value = value.replace("${entry.filebase}",
                                      IOUtil.stripExtension(fileTail));

                commands.add(value);
            } else if (arg.isFile()) {
                String filename = arg.getFileName();
                filename =
                    filename.replace("${entry.name}",
                                     IOUtil.stripExtension(entry.getName()));
                filename = filename.replace("${entry.file}",
                                            entry.getResource().getPath());
                filename = filename.replace("${entry.filebase}",
                                            IOUtil.stripExtension(fileTail));

                String filePath = IOUtil.joinDir(workDir, filename);
                files.add(new File(filePath));
                if ( !arg.predefined) {
                    commands.add(filePath);
                    System.err.println("filePath:" + filePath);
                }
            } else if (arg.isEntry()) {
                String entryId = request.getString(arg.getId() + "_hidden",
                                     (String) null);
                Entry entryArg = getEntryManager().getEntry(request, entryId);
                //TODO: Check for null, get the file
                if (entryArg == null) {
                    throw new IllegalArgumentException(
                        "No entry  specified for:" + arg.getLabel());
                }
                commands.add(entryArg.getResource().getPath());
            } else {
                String argValue = request.getString(arg.getId(),
                                      (String) null);
                if (argValue == null) {
                    throw new IllegalArgumentException(
                        "No argument specified for:" + arg.getLabel());
                } else {
                    commands.add(argValue);
                }
            }
        }

        System.err.println("Executing: " + StringUtil.join(" ", commands));

        String[] results  = getRepository().executeCommand(commands, workDir);
        String   errorMsg = results[1];
        String   outMsg   = results[0];

        sb.append("stdout:" + outMsg);
        if (Utils.stringDefined(errorMsg)) {
            sb.append("\n");
            sb.append(HtmlUtils.p());
            sb.append("stderr:" + errorMsg);
        }

        if (files.size() > 0) {
            File file = files.get(0);
            if ( !file.exists()) {
                System.err.println(sb);

                throw new IllegalArgumentException("No output file created");
            }

            if (doingPublish(request)) {
                TypeHandler typeHandler =
                    getRepository().getTypeHandler((destType != null)
                        ? destType
                        : TypeHandler.TYPE_FILE);
                Entry newEntry =
                    typeHandler.createEntry(getRepository().getGUID());

                return getEntryManager().processEntryPublish(request,
                        files.get(0), newEntry, entry, "derived from");
            }


            request.setReturnFilename(file.getName());

            return new Result(getStorageManager().getFileInputStream(file),
                              "");
        }


        return new Result(outputType.getLabel(), sb);
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
        sb.append(HtmlUtils.formTable());
        for (Arg arg : args) {
            if (arg.isValueArg()) {
                continue;
            }
            String input = null;
            if (arg.isEnumeration()) {
                input = HtmlUtils.select(arg.getId(), arg.getValues(),
                                         (List) null, "", 100);
            } else if (arg.isFile()) {
                //noop
            } else if (arg.isEntry()) {
                input = getSelect(
                    request, arg.getId(), msg("Select"), true,
                    null) + HtmlUtils.hidden(
                        arg.getId() + "_hidden", "",
                        HtmlUtils.id(
                            arg.getId() + "_hidden")) + HtmlUtils.space(1)
                                + HtmlUtils.disabledInput(
                                    arg.getId(), "",
                                    HtmlUtils.SIZE_60
                                    + HtmlUtils.id(arg.getId()));

            } else {
                input = HtmlUtils.input(arg.getId(), "", arg.getSize());
            }
            if (input == null) {
                continue;
            }
            sb.append(HtmlUtils.formEntry(arg.getLabel(), input));
        }
        addPublishWidget(request, entry, sb,
                         msg("Select a folder to publish to"));

        sb.append(HtmlUtils.formTableClose());
        sb.append(HtmlUtils.submit(actionLabel, ARG_EXECUTE));
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
        private static final String TYPE_FILE = "file";



        /** _more_ */
        private String value;

        /** _more_ */
        private String id;

        /** _more_ */
        private String label;

        /** _more_ */
        private String type;

        /** _more_ */
        private String fileName;

        /** _more_ */
        private int size = 24;

        /** _more_          */
        private boolean predefined = false;

        /** _more_ */
        private List<TwoFacedObject> values = new ArrayList<TwoFacedObject>();

        /**
         * _more_
         *
         * @param node _more_
         */
        public Arg(Element node) {
            id         = XmlUtil.getAttribute(node, ATTR_ID, (String) null);
            type       = XmlUtil.getAttribute(node, ATTR_TYPE, TYPE_STRING);
            value      = XmlUtil.getChildText(node);
            label      = XmlUtil.getAttribute(node, ATTR_LABEL, id);
            fileName   = XmlUtil.getAttribute(node, "filename", "${src}");
            predefined = XmlUtil.getAttribute(node, "predefined", false);
            size       = XmlUtil.getAttribute(node, ATTR_SIZE, size);
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
        public boolean isEnumeration() {
            return type.equals(TYPE_ENUMERATION);
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
        public boolean isValueArg() {
            return (id == null) && (value != null);
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
         * Set the Id property.
         *
         * @param value The new value for Id
         */
        public void setId(String value) {
            id = value;
        }

        /**
         * Get the Id property.
         *
         * @return The Id
         */
        public String getId() {
            return id;
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




    }


    /**
     * _more_
     *
     * @param args _more_
     *
     * @throws Exception _more_
     */
    public static void main(String[] args) throws Exception {
        List<String> commands = new ArrayList<String>();
        commands.add("/Users/jeffmc/.ramadda/bin/test.sh");



        ProcessBuilder pb = new ProcessBuilder(commands);
        pb.directory(new File("."));
        StringWriter outBuf   = new StringWriter();
        StringWriter errorBuf = new StringWriter();
        Process      process  = pb.start();

        // process the outputs in a thread
        StreamEater esg = new StreamEater(process.getErrorStream(),
                                          new PrintWriter(errorBuf));
        StreamEater isg = new StreamEater(process.getInputStream(),
                                          new PrintWriter(outBuf));
        esg.start();
        isg.start();
        int exitCode = process.waitFor();
        System.err.println("code:" + exitCode + " output:" + outBuf + " err:"
                           + errorBuf);


    }


}
