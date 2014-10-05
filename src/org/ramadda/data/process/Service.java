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

import java.util.Enumeration;


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
    public static boolean debug = false;

    /** _more_ */
    public static final String ARG_SERVICEFORM = "serviceform";

    /** _more_ */
    private static ServiceUtil dummyToForceCompile;

    /** _more_ */
    private static WorkflowTypeHandler dummy2ToForceCompile;


    /** _more_ */
    public static final String TAG_ARG = "arg";

    /** _more_ */
    public static final String TAG_PARAMS = "params";

    /** _more_ */
    public static final String TAG_PARAM = "param";

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
    public static final String TAG_SERVICES = "services";


    /** _more_ */
    public static final String ATTR_CATEGORY = "category";

    /** _more_ */
    public static final String ATTR_VALUES = "values";

    /** _more_ */
    public static final String ATTR_LABEL = "label";

    /** _more_ */
    public static final String ATTR_HELP = "help";

    /** _more_ */
    public static final String ATTR_DESCRIPTION = "description";

    /** _more_ */
    public static final String ATTR_SERIAL = "serial";

    /** _more_ */
    public static final String ATTR_GROUP = "group";

    /** _more_ */
    public static final String ATTR_FILE = "file";

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
    private Boolean requiresMultipleEntries;

    /** _more_ */
    private boolean outputToStderr = false;
    private boolean immediate = false;

    /** _more_ */
    private boolean ignoreStderr = false;

    /** _more_ */
    private boolean cleanup = false;

    /** _more_ */
    private String command;

    /** _more_ */
    private Object commandObject;

    /** _more_ */
    private Method commandMethod;

    /** _more_ */
    private String description;

    /** _more_ */
    private String category;

    /** _more_ */
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


    /** _more_ */
    private Hashtable paramValues = new Hashtable();

    /** _more_ */
    private Element element;

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
     * @return _more_
     */
    public String toString() {
        return getLabel() + " " + getId();
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



    /** _more_ */
    public static final String ARG_DELIMITER = ".";


    /**
     * _more_
     *
     * @return _more_
     */
    public String getUrlArg() {
        if (parent != null) {
            return parent.getUrlArg() + ARG_DELIMITER + id;
        }

        return id;
    }

    /**
     * _more_
     *
     * @param name _more_
     *
     * @return _more_
     */
    public String getUrlArg(String name) {
        return getUrlArg() + ARG_DELIMITER + name;
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

        this.element = element;
        this.parent  = parent;
        id           = XmlUtil.getAttribute(element, ATTR_ID, dfltId);
        if (id == null) {
            id = "dummy";
        }


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

        immediate = XmlUtil.getAttributeFromTree(element,
                "immediate", false);

        ignoreStderr = XmlUtil.getAttributeFromTree(element, "ignoreStderr",
                ignoreStderr);

        cleanup = XmlUtil.getAttributeFromTree(element, ATTR_CLEANUP, true);
        category = XmlUtil.getAttributeFromTree(element, "category",
                (String) null);
        linkId = XmlUtil.getAttribute(element, ATTR_LINK, (String) null);
        description = XmlUtil.getGrandChildText(element, ATTR_DESCRIPTION,
                XmlUtil.getGrandChildText(element, ATTR_HELP, ""));

        processDesc = XmlUtil.getGrandChildText(element,
                "process_description", "");
        label  = XmlUtil.getAttribute(element, ATTR_LABEL, (String) null);
        serial = XmlUtil.getAttribute(element, ATTR_SERIAL, true);




        NodeList nodes;

        Element  params = XmlUtil.findChild(element, TAG_PARAMS);

        if (params != null) {
            nodes = XmlUtil.getElements(params, TAG_PARAM);
            for (int i = 0; i < nodes.getLength(); i++) {
                Element node  = (Element) nodes.item(i);
                String  name  = XmlUtil.getAttribute(node, "name");
                String  value = XmlUtil.getChildText(node);
                Object  v     = paramValues.get(name);
                if (v == null) {
                    paramValues.put(name, value);
                } else if (v instanceof List) {
                    ((List) v).add(value);
                } else {
                    List newList = new ArrayList();
                    newList.add(v);
                    newList.add(value);
                    paramValues.put(name, newList);
                }
            }
        }



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
                System.err.println("Service: no command defined:"
                                   + XmlUtil.toString(element) + " path:"
                                   + pathProperty);

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
     * @param request _more_
     *
     * @return _more_
     */
    public Request makeRequest(Request request) {
        if (paramValues.size() == 0) {
            return request;
        }
        request = request.cloneMe();
        for (Enumeration keys =
                paramValues.keys(); keys.hasMoreElements(); ) {
            String id    = (String) keys.nextElement();
            Object value = paramValues.get(id);
            if ( !request.defined(id)) {
                request.put(id, value);
            }
        }

        return request;
    }


    /**
     * _more_
     *
     * @param request _more_
     * @param input _more_
     * @param argName _more_
     * @param dflt _more_
     *
     * @return _more_
     */
    public boolean getRequestValue(Request request, ServiceInput input,
                                   String argName, boolean dflt) {
        String v = getRequestValue(request, input, argName, (String) null);
        if (v == null) {
            return dflt;
        }

        return v.equals("true");
    }


    /**
     * _more_
     *
     * @param request _more_
     * @param argName _more_
     * @param dflt _more_
     *
     * @return _more_
     */
    public boolean getRequestValue(Request request, String argName,
                                   boolean dflt) {
        return getRequestValue(request, null, argName, dflt);
    }

    /**
     * _more_
     *
     * @param request _more_
     * @param argName _more_
     * @param dflt _more_
     *
     * @return _more_
     */
    public String getRequestValue(Request request, String argName,
                                  String dflt) {
        return getRequestValue(request, null, argName, dflt);
    }


    /**
     * _more_
     *
     * @param request _more_
     * @param input _more_
     * @param argName _more_
     * @param dflt _more_
     *
     * @return _more_
     */
    public String getRequestValue(Request request, ServiceInput input,
                                  String argName, String dflt) {
        String fullArg = getUrlArg(argName);

        if (request.defined(fullArg)) {
            String value = request.getString(fullArg, dflt);
            if (input != null) {
                input.addParam(fullArg, value);
            }
            debug("getRequestValue: full arg: " + argName + "=" + value);

            return value;
        }
        if (request.defined(argName)) {
            String value = request.getString(argName, dflt);
            if (input != null) {
                input.addParam(argName, value);
            }
            debug("getRequestValue: part arg: " + argName + "=" + value);

            return value;
        }
        debug("getRequestValue: no value: " + argName);

        return dflt;
    }


    /**
     * _more_
     *
     * @param request _more_
     * @param input _more_
     * @param argName _more_
     * @param dflt _more_
     *
     * @return _more_
     */
    public List<String> getRequestValue(Request request, ServiceInput input,
                                        String argName, List<String> dflt) {
        String       fullArg = getUrlArg(argName);
        List<String> results = null;
        if (request.defined(fullArg)) {
            results = request.get(fullArg, results);
            if (input != null) {
                for (String value : results) {
                    input.addParam(fullArg, value);
                }
            }

            return results;
        }

        if (request.defined(argName)) {
            results = request.get(argName, results);
            if (input != null) {
                for (String value : results) {
                    input.addParam(argName, value);
                }
            }

            return results;
        }
        debug("getRequestValue: no value: " + argName);

        return dflt;
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
            try {
                link = getRepository().getJobManager().getService(linkId);
            } catch (Exception exc) {
                throw new RuntimeException(exc);
            }
        }
    }

    /**
     * _more_
     *
     * @return _more_
     */
    public boolean haveLink() {
        initService();

        return link != null;

    }

    public boolean getImmediate() {
        return immediate;
    }

    /**
     * _more_
     *
     * @param request _more_
     * @param input _more_
     * @param commands _more_
     * @param filesToDelete _more_
     *
     *
     * @return _more_
     * @throws Exception _more_
     */
    public HashSet<String> addArgs(Request request, ServiceInput input,
                                   List<String> commands,
                                   List<File> filesToDelete,        
                                   List<Entry> allEntries)


            throws Exception {

        if (haveLink()) {
            return link.addArgs(request, input, commands, filesToDelete,allEntries);
        }

        List<Entry> inputEntries = input.getEntries();

        File        workDir      = input.getProcessDir();
        HashSet<String> seenGroup       = new HashSet<String>();
        HashSet<String> definedArgs     = new HashSet<String>();


        Hashtable<String,List<Entry>> entryMap = new Hashtable<String,List<Entry>>();

        boolean         haveSeenAnEntry = false;

        for (Service.Arg arg : getArgs()) {
            if (!arg.isEntry()) {
                continue;
            }
            String      argName = arg.getName() + "_hidden";
            List<Entry> entries = new ArrayList<Entry>();
            List<String> entryIds = getRequestValue(request, input,
                                                    argName, new ArrayList<String>());
            for (String entryId : entryIds) {
                Entry entry = getEntryManager().getEntry(request,
                                                         entryId);
                if (entry == null) {
                    System.err.println("Bad entry:" + entryId);

                    throw new IllegalArgumentException(
                                                       "Could not find entry for arg:" + arg.getLabel()
                                                       + " entry id:" + entryId);
                }
                entries.add(entry);
            }
            if (entries.size() == 0) {
                if (!haveSeenAnEntry) {
                    for (Entry entry : input.getEntries()) {
                        if (arg.isApplicable(entry, false)) {
                            entries.add(entry);
                        }
                    }
                }
                for (Entry entry : entries) {
                    if ( !getEntryManager().isSynthEntry(entry.getId())) {
                        input.addParam(getUrlArg(argName), entry.getId());
                    }
                }
            }
            allEntries.addAll(entries);
            entryMap.put(arg.getName(), entries);
            haveSeenAnEntry = true;
        }



        if(inputEntries.size()==0) {
            inputEntries = allEntries;
            input.setEntries(allEntries);
        }

        Entry currentEntry = (Entry) Utils.safeGet(inputEntries, 0);

        String cmd = applyMacros(currentEntry, entryMap, workDir, getCommand(),
                                 input.getForDisplay());
        commands.add(cmd);

        addExtraArgs(request, input, commands, true);



        for (Service.Arg arg : getArgs()) {
            if (arg.depends != null) {
                if ( !definedArgs.contains(arg.depends)) {
                    //                    System.err.println("Dependency:" + arg.getName() + " " + arg.depends);
                    continue;
                }
            }

            if (arg.getCategory() != null) {
                continue;
            }
            String       argValue = null;
            List<String> values   = null;
            if (arg.isValueArg()) {
                argValue = arg.getValue();
            } else if (arg.isFlag()) {
                if (arg.getGroup() != null) {
                    if ( !seenGroup.contains(arg.getGroup())) {
                        argValue = getRequestValue(request, input,
                                arg.getGroup(), (String) null);
                        if (Utils.stringDefined(argValue)) {
                            seenGroup.add(arg.getGroup());
                        } else {
                            argValue = null;
                        }
                    }
                } else if (getRequestValue(request, input, arg.getName(),
                                           false)) {
                    argValue = arg.getValue();
                }
            } else if (arg.isFile()) {
                //TODO:
                //                String filename = applyMacros(currentEntry, entryMap, workDir,
                //arg.getFileName(), input.getForDisplay());
                //argValue = IOUtil.joinDir(workDir, filename);
            } else if (arg.isEntry()) {
                List<Entry> entries =  entryMap.get(arg.getName());
                if ( !arg.isMultiple() && (entries.size() > 1)) {
                    throw new IllegalArgumentException(
                        "Too many entries specified for arg:"
                        + arg.getLabel() + " entries:" + entries);
                } else if ( !arg.isRequired() && (entries.size() == 0)) {
                    throw new IllegalArgumentException(
                        "No entry specified for arg:" + arg.getLabel());
                }


                values = new ArrayList<String>();
                for (Entry entry : entries) {
                    currentEntry = entry;
                    String filePath = currentEntry.getResource().getPath();
                    if (arg.copy) {
                        File newFile =
                            new File(
                                IOUtil.joinDir(
                                    input.getProcessDir(),
                                    getStorageManager().getFileTail(
                                        currentEntry)));
                        if ( !newFile.exists()) {
                            IOUtil.copyFile(currentEntry.getFile(), newFile);
                            filesToDelete.add(newFile);
                        }
                        filePath = newFile.toString();
                    }

                    argValue = arg.getValue();
                    argValue = argValue.replace("${entry.file}", filePath);
                    values.add(argValue);
                }
            } else {
                argValue = getRequestValue(request, input, arg.getName(),
                                           (String) null);
                if (argValue != null) {}
            }


            if (arg.isMultiple() && (values != null)) {
                if (arg.multipleJoin != null) {
                    argValue = StringUtil.join(arg.multipleJoin, values);
                    values   = null;
                }
            }


            if ((values == null) && (argValue != null)) {
                values = new ArrayList<String>();
                values.add(argValue);
            }


            int argCnt = 0;
            if (values != null) {
                for (String originalValue : values) {
                    String value = originalValue;
                    if (arg.getIfDefined() && !Utils.stringDefined(value)) {
                        continue;
                    }
                    argCnt++;
                    if ( !arg.isEntry() && Utils.stringDefined(arg.value)) {
                        value = arg.value.replace("${value}", value);
                    }

                    if (Utils.stringDefined(value) || arg.isRequired()) {
                        if (Utils.stringDefined(arg.prefix)) {
                            commands.add(arg.prefix);
                        }

                        if (arg.file != null) {
                            String fileName = applyMacros(currentEntry,
                                                          entryMap, 
                                                          workDir, arg.file,
                                                          input.getForDisplay());


                            File destFile = new File(IOUtil.joinDir(workDir,
                                                fileName));
                            int cnt = 0;
                            while (destFile.exists()) {
                                cnt++;
                                destFile = new File(IOUtil.joinDir(workDir,
                                        cnt + "_" + fileName));
                            }

                            value = arg.value.replace("${value}", value);

                            value = value.replace("${file}",
                                    destFile.getName());
                            value = value.replace("${value}", originalValue);
                        }
                        value = applyMacros(currentEntry, entryMap, workDir, value,
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

        addExtraArgs(request, input, commands, false);

        return definedArgs;
    }

    /**
     * _more_
     *
     * @param request _more_
     * @param input _more_
     * @param args _more_
     * @param start _more_
     *
     * @throws Exception _more_
     */
    public void addExtraArgs(Request request, ServiceInput input,
                             List<String> args, boolean start)
            throws Exception {}


    /**
     * _more_
     *
     * @param input _more_
     *
     * @return _more_
     */
    public String getLinkXml(ServiceInput input) {
        if (haveLink()) {
            return link.getLinkXml(input);
        }


        StringBuffer sb = new StringBuffer();
        sb.append(XmlUtil.openTag(TAG_SERVICES));
        sb.append("\n");
        sb.append(XmlUtil.openTag(TAG_SERVICE,
                                  XmlUtil.attrs(ATTR_LINK, getId(), ATTR_ID,
                                      "service")));
        sb.append("\n");
        sb.append(XmlUtil.openTag(TAG_PARAMS));
        sb.append("\n");

        for (String[] param : input.getParams()) {
            sb.append(XmlUtil.tag(TAG_PARAM,
                                  XmlUtil.attrs(ATTR_NAME, param[0]),
                                  XmlUtil.getCdata(param[1])));
        }

        sb.append(XmlUtil.closeTag(TAG_PARAMS));
        sb.append(XmlUtil.closeTag(TAG_SERVICE));
        sb.append(XmlUtil.closeTag(TAG_SERVICES));

        return sb.toString();
    }


    /**
     * _more_
     *
     * @return _more_
     */
    public String getCategory() {
        if (category != null) {
            return category;
        }
        if (haveLink()) {
            return link.getCategory();
        }

        return "Services";
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

        return "/icons/cog.png";
    }

    /**
     * _more_
     *
     * @return _more_
     */
    public String getId() {
        /*
        if (haveLink()) {
            return link.getId();
            }*/

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
     *
     * @throws Exception _more_
     */
    public void addToForm(Request request, ServiceInput input, Appendable sb)
            throws Exception {
        boolean comingFromForm = request.get(ARG_SERVICEFORM, false);
        if ( !comingFromForm) {
            request = makeRequest(request);

        }


        if (haveLink()) {
            link.addToForm(request, input, sb);

            return;
        }

        if (haveChildren()) {
            Service sourceService = input.getSourceService();

            for (Service child : children) {
                StringBuilder tmpSB = new StringBuilder();
                child.addToForm(request, input, tmpSB);
                if (tmpSB.length() > 0) {
                    sb.append(HtmlUtils.p());
                    sb.append(tmpSB);
                }
                input.setSourceService(child);
            }

            input.setSourceService(sourceService);

            return;
        }

        addToFormInner(request, input, sb);
    }

    /**
     * _more_
     *
     * @param request _more_
     * @param input _more_
     * @param sb _more_
     *
     *
     * @throws Exception _more_
     */
    private void addToFormInner(Request request, ServiceInput input,
                                Appendable sb)
            throws Exception {


        StringBuilder formSB      = new StringBuilder();
        int           blockCnt    = 0;
        CatBuff       catBuff     = null;
        Service.Arg   catArg      = null;
        boolean       anyRequired = false;
        for (int argType = 0; argType <= 1; argType++) {
            for (Service.Arg arg : getArgs()) {
                if (argType == 0) {
                    if ( !arg.isEntry() && !arg.first) {
                        continue;
                    }
                } else {
                    if (arg.isEntry() || arg.first) {
                        continue;
                    }
                }

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
        }

        if ((catBuff != null) && (catBuff.length() > 0)) {
            processCatBuff(request, formSB, catArg, catBuff, ++blockCnt);
        }
        if (anyRequired) {
            formSB.append(
                "<span class=ramadda-required-label>* required</span>");
        }


        sb.append(HtmlUtils.open(HtmlUtils.TAG_DIV,
                                 HtmlUtils.cssClass("service-form")));

        sb.append(HtmlUtils.div(HtmlUtils.img(iconUrl(getIcon())) +" " + getLabel(),
                                HtmlUtils.cssClass("service-form-header")));
        if (Utils.stringDefined(getDescription())) {
            sb.append(
                HtmlUtils.div(
                    getDescription(),
                    HtmlUtils.cssClass("service-form-description")));
        }
        List<Entry> entries = input.getEntries();
        if (false && (entries.size() > 1)) {
            StringBuffer entriesSB = new StringBuffer();
            for (Entry entry : entries) {
                if ( !isApplicable(entry)) {
                    continue;
                }
                entriesSB.append(
                    HtmlUtils.href(
                        getEntryManager().getEntryURL(request, entry),
                        entry.getName(), " target=\"_help\" "));
                entriesSB.append(HtmlUtils.br());
            }
            sb.append(
                HtmlUtils.div(
                    entriesSB.toString(),
                    HtmlUtils.cssClass("service-form-entries")));
        }


        if (formSB.length() > 0) {
            sb.append(
                HtmlUtils.div(
                    formSB.toString(),
                    HtmlUtils.cssClass("service-form-contents")));
        }
        sb.append(HtmlUtils.close(HtmlUtils.TAG_DIV));

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

            List selected = request.get(arg.getUrlArg(),
                                        Misc.newList(arg.dflt));
            inputHtml.append(HtmlUtils.select(arg.getUrlArg(), values,
                    selected, extra, 100));
        } else if (arg.isFlag()) {
            if (arg.getGroup() != null) {
                boolean selected = getRequestValue(request, arg.getGroup(),
                                       arg.dflt).equals(arg.getValue());
                inputHtml.append(HtmlUtils.radio(getUrlArg(arg.getGroup()),
                        arg.getValue(), selected));
            } else {
                inputHtml.append(HtmlUtils.checkbox(arg.getUrlArg(), "true",
                        getRequestValue(request, arg.getName(),
                                        arg.dflt.equals("true"))));
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

            if ((input.getSourceService() != null)
                    && (input.getSourceService().getOutputs().size() > 0)) {
                return;
            }
            if (primaryEntry != null && arg.isPrimaryEntry()) {
                return;
            } 

            if (arg.getEntryType() != null) {
                request.put(ARG_ENTRYTYPE, arg.getEntryType());
            }
            String elementId = HtmlUtils.getUniqueId("select_");
            inputHtml.append(OutputHandler.getSelect(request, elementId,
                                                     msg("Select"), true, null));
            String argName    = arg.getName() + "_hidden";

            String entryId    = getRequestValue(request, argName, "");

            String entryLabel = "";

            if (Utils.stringDefined(entryId)) {
                Entry entryArg = getEntryManager().getEntry(request,
                                                            entryId);
                if (entryArg != null) {
                    entryLabel = entryArg.getName();
                }
            }

            inputHtml.append(HtmlUtils.hidden(getUrlArg(argName),
                                              entryId, HtmlUtils.id(elementId + "_hidden")));
            inputHtml.append(HtmlUtils.space(1));
            inputHtml.append(HtmlUtils.disabledInput(arg.getUrlArg(),
                                                     entryLabel,
                                                     HtmlUtils.SIZE_60 + HtmlUtils.id(elementId)));
            //                inputHtml.append(HtmlUtils.disabledInput(arg.getUrlArg(),
            //                                                         getRequestValue(request, arg.getName(), ""),
            //                                                         HtmlUtils.SIZE_60 + HtmlUtils.id(elementId)));
            request.remove(ARG_ENTRYTYPE);

        } else {
            String extra = HtmlUtils.attr(HtmlUtils.ATTR_SIZE,
                                          "" + arg.getSize());
            if (arg.placeHolder != null) {
                extra += HtmlUtils.attr("placeholder", arg.placeHolder);
            }
            inputHtml.append(HtmlUtils.input(arg.getUrlArg(),
                                             getRequestValue(request,
                                                 arg.getName(),
                                                     arg.dflt), extra));
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
        if (help != null) {
            help = HtmlUtils.div(help,
                                 HtmlUtils.cssClass("service-form-help"));
            sb.append(HtmlUtils.formEntryTop(Utils.stringDefined(label)
                                             ? msgLabel(label)
                                             : "", col1, help));

        } else {
            sb.append(HtmlUtils.formEntryTop(Utils.stringDefined(label)
                                             ? msgLabel(label)
                                             : "", col1, 2));
        }

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
            /*
            String desc = catArg.getValue();
            if (Utils.stringDefined(desc)) {
                html += desc;
                html += HtmlUtils.br();
            }
            */
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
        if (haveLink()) {
            return link.isEnabled();
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
     * @return _more_
     */
    public boolean requiresMultipleEntries() {
        if (requiresMultipleEntries != null) {
            return requiresMultipleEntries;
        }
        if (haveLink()) {
            requiresMultipleEntries =
                new Boolean(link.requiresMultipleEntries());
        } else if (haveChildren()) {
            if (serial) {
                requiresMultipleEntries =
                    new Boolean(children.get(0).requiresMultipleEntries());
            } else {
                for (Service child : children) {
                    if ( !child.isEnabled()) {
                        requiresMultipleEntries = new Boolean(false);

                        break;
                    }
                }
            }
        } else {
            for (Service.Arg arg : getArgs()) {
                if (arg.isEntry()) {
                    requiresMultipleEntries = new Boolean(arg.isMultiple());

                    break;
                }
            }
        }
        if (requiresMultipleEntries == null) {
            requiresMultipleEntries = new Boolean(false);
        }

        return requiresMultipleEntries;
    }



    /**
     * _more_
     *
     * @param args _more_
     */
    public void collectArgs(List<Arg> args) {
        if (haveLink()) {
            link.collectArgs(args);

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
        if (haveLink()) {
            return link.getOutputToStderr();
        }

        return outputToStderr;
    }

    /**
     * _more_
     *
     * @return _more_
     */
    public boolean getIgnoreStderr() {
        if (haveLink()) {
            return link.getIgnoreStderr();
        }

        return ignoreStderr;
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
        if (haveLink()) {
            return link.getOutputs();
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
     * @param entries _more_
     *
     * @return _more_
     */
    public boolean isApplicable(List<Entry> entries) {
        if ( !requiresMultipleEntries()) {
            return false;
        }
        if (entries == null) {
            return false;
        }
        int cnt = 0;
        for (Entry entry : entries) {
            if (isApplicableInner(entry)) {
                cnt++;
                if (cnt > 1) {
                    return true;
                }
            }
        }

        return false;
    }


    /**
     * _more_
     *
     * @param entry _more_
     *
     * @return _more_
     */
    public boolean isApplicable(Entry entry) {
        if (requiresMultipleEntries()) {
            return false;
        }

        return isApplicableInner(entry);
    }


    /**
     * _more_
     *
     * @param entry _more_
     *
     * @return _more_
     */
    private boolean isApplicableInner(Entry entry) {
        if (haveLink()) {
            return link.isApplicable(entry);
        }

        if (haveChildren()) {
            return children.get(0).isApplicable(entry);
        }

        //        System.err.println("isApplicable:" + getLabel() +" " + command);

        for (Arg input : inputs) {
            boolean debug = false;
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
        if (haveLink()) {
            return link.getCommand();
        }

        return command;
    }

    /**
     * _more_
     *
     * @return _more_
     *
     */
    public String getDescription() {
        if (haveLink()) {
            link.getDescription();
        }

        return description;
    }

    /**
     * _more_
     *
     * @return _more_
     */
    public String getProcessDescription() {
        if (Utils.stringDefined(processDesc)) {
            return processDesc;
        }
        if (haveLink()) {
            return link.getProcessDescription();
        }

        return null;
    }

    /**
     * _more_
     *
     * @return _more_
     */
    public String getLabel() {
        if (Utils.stringDefined(label)) {
            return label;
        }
        if (haveLink()) {
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

        boolean comingFromForm = request.get(ARG_SERVICEFORM, false);

        if ( !comingFromForm) {
            request = makeRequest(request);
        }

        if (haveLink()) {
            return link.evaluate(request, input);
        }


        ServiceOutput myOutput      = new ServiceOutput();


        List<File>    filesToDelete = new ArrayList<File>();

        HashSet<File> existingFiles = new HashSet<File>();
        for (File f : input.getProcessDir().listFiles()) {
            existingFiles.add(f);
        }
        List<Entry>   entries  = input.getEntries();

        HashSet<File> newFiles = new HashSet<File>();
        if (haveChildren()) {
            ServiceOutput childOutput = null;
            ServiceInput  childInput  = input;

            for (Service child : children) {
                //                System.err.println("Input:" + childInput.getEntries());
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
                    System.err.println("Service.evaluate: deleting:" + f);
                    f.delete();
                }
            }
            if (input.getForDisplay()) {
                return myOutput;
            }

            return myOutput;
        }


        List<String>    commands     = new ArrayList<String>();
        List<Entry> allEntries = new ArrayList<Entry>();
        HashSet<String> definedArgs  =   this.addArgs(request, input, commands,
                                                      filesToDelete, allEntries);

        if(entries.size()==0) {
            entries = allEntries;
        }

        Entry           currentEntry = (Entry) Utils.safeGet(entries, 0);

        System.err.println("Command:" + commands);

        if (input.getForDisplay()) {
            commands.set(0, IOUtil.getFileTail(commands.get(0)));
            myOutput.append(StringUtil.join(" ", commands));
            myOutput.append("\n");

            return myOutput;
        }

        String errMsg = "";
        String outMsg = "";
        File stdoutFile = new File(IOUtil.joinDir(input.getProcessDir(),
                              "." + getId() + ".stdout"));
        File stderrFile = new File(IOUtil.joinDir(input.getProcessDir(),
                              "." + getId() + ".stderr"));


        //        System.out.println(getLinkXml(input));
        if (commandObject != null) {
            commandMethod.invoke(commandObject, new Object[] { request, this,
                    input, commands });
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
            } else if ( !getIgnoreStderr()) {
                //If there is an error then
                myOutput.setOk(false);
                myOutput.append(errMsg);

                return myOutput;
            }
        }

        boolean       setResultsFromStdout = true;

        HashSet<File> seen                 = new HashSet<File>();


        for (File f : filesToDelete) {
            System.err.println("delete:" + f);
            f.delete();
        }




        for (OutputDefinition output : getOutputs()) {
            String depends = output.getDepends();
            if (depends != null) {
                if (depends.startsWith("!")) {
                    if (definedArgs.contains(depends.substring(1))) {
                        continue;
                    }
                } else {
                    if ( !definedArgs.contains(depends)) {
                        continue;
                    }
                }
            }

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
                String filename = applyMacros(currentEntry,
                                              null, 
                                              input.getProcessDir(),
                                              output.getFilename(),
                                              input.getForDisplay());
                File destFile =
                    new File(IOUtil.joinDir(input.getProcessDir(), filename));
                IOUtil.moveFile(stdoutFile, destFile);
                files = new File[] { destFile };
            }
            final String thePattern = applyMacros(currentEntry,
                                                  null, 
                                                  input.getProcessDir(),
                                                  output.getPattern(),
                                                  input.getForDisplay());

            if (files == null) {
                files = input.getProcessDir().listFiles(new FileFilter() {
                    public boolean accept(File f) {
                        String name = f.getName();
                        if (name.startsWith(".")) {
                            return false;
                        }
                        if (thePattern == null) {
                            return true;
                        }
                        if (name.toLowerCase().matches(thePattern)) {
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
                            newEntry, currentEntry, "derived from");
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
     * @param request _more_
     * @param input _more_
     * @param output _more_
     * @param sb _more_
     *
     * @throws Exception _more_
     */
    public void addOutput(Request request, ServiceInput input,
                          ServiceOutput output, Appendable sb)
            throws Exception {
        if (haveLink()) {
            link.addOutput(request, input, output, sb);
            return;
        }

        int cnt = 0;
        for (Entry entry : input.getEntries()) {
            if (cnt++ == 0) {
                sb.append(
                    HtmlUtils.open(
                        HtmlUtils.TAG_DIV,
                        HtmlUtils.cssClass("service-output-header")));
            } else {
                sb.append(HtmlUtils.br());
            }
            sb.append(HtmlUtils.href(getEntryManager().getEntryURL(request,
                    entry), entry.getName()));
        }
        if (cnt > 0) {
            sb.append(HtmlUtils.close(HtmlUtils.TAG_DIV));
        }
        sb.append("<div class=service-output>");
        sb.append("<pre>");
        sb.append(output.getResults());
        sb.append("</pre>");
        sb.append("</div>");
    }


    /**
     * _more_
     */
    public void ensureSafeServices() {
        if (command != null) {
            throw new IllegalArgumentException(
                "Service cannot have a command:" + command);
        }
        if (haveChildren()) {
            for (Service child : children) {
                child.ensureSafeServices();
            }
        }
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
    public String applyMacros(Entry entry, 
                              Hashtable<String,List<Entry>> entryMap, File workDir, String value,
                              boolean forDisplay) {

        if (value == null) {
            return null;
        }

        value = value.replace("${workdir}", forDisplay
                                            ? "&lt;working directory&gt;"
                                            : workDir.toString());
        if(entryMap!=null) {
            for (Enumeration keys = entryMap.keys(); keys.hasMoreElements(); ) {
                String id = (String) keys.nextElement();
                for(Entry otherEntry:entryMap.get(id)) {
                    value =  applyMacros(otherEntry, id, value, forDisplay);
                }
            }
        }

        //        System.err.println("Apply macros:" +entry);
        if (entry != null) {
            value =  applyMacros(entry, "entry", value, forDisplay);
        }

        return value;
    }


    private String applyMacros(Entry entry, String id, String value, boolean forDisplay) {
        List<Column> columns = entry.getTypeHandler().getColumns();
        if (columns != null) {
            for (Column column : columns) {
                Object columnValue =
                    entry.getTypeHandler().getEntryValue(entry,
                                                         column.getName());
                if (columnValue != null) {
                    value = value.replace("${" + id +".attr."
                                          + column.getName() + "}", "" + columnValue);
                } else {
                    value = value.replace("${" + id +".attr."
                                          + column.getName() + "}", "");
                }
            }
        }


        String fileTail = getStorageManager().getFileTail(entry);
        value = value.replace("${" + id +".id}", entry.getId());
        value = value.replace("${" + id +".file}", forDisplay
                              ? getStorageManager().getFileTail(entry)
                              : entry.getResource().getPath());
        //? not sure what the macros should be
        //            value = value.replace(macro("entry.file.base"),
        //                                  IOUtil.stripExtension(entry.getName()));
        value = value.replace("${" + id +".file.base}",
                              IOUtil.stripExtension(fileTail));
        value =
            value.replace("${" + id +".file.suffix}",
                          IOUtil.getFileExtension(fileTail).replace(".",
                                                                    ""));
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
        private Service service;

        /** _more_ */
        private String name;


        /** _more_ */
        private String value;

        /** _more_ */
        private String dflt;

        /** _more_ */
        private String prefix;

        /** _more_ */
        private String group;

        /** _more_ */
        private String file;

        /** _more_ */
        private boolean nameDefined = false;


        /** _more_ */
        private boolean ifDefined = true;


        /** _more_ */
        private boolean multiple = false;

        /** _more_ */
        private boolean first = false;

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

        /** _more_ */
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
         * @param service _more_
         * @param node _more_
         * @param idx _more_
         *
         * @throws Exception _more_
         */
        public Arg(Service service, Element node, int idx) throws Exception {
            this.service = service;

            type = XmlUtil.getAttribute(node, ATTR_TYPE, (String) null);
            depends = XmlUtil.getAttribute(node, "depends", (String) null);
            addAll       = XmlUtil.getAttribute(node, "addAll", false);
            addNone      = XmlUtil.getAttribute(node, "addNone", false);


            entryType = XmlUtil.getAttributeFromTree(node, ATTR_ENTRY_TYPE,
                    (String) null);

            entryPattern = XmlUtil.getAttributeFromTree(node,
                    ATTR_ENTRY_PATTERN, (String) null);

            placeHolder = XmlUtil.getAttribute(node, "placeHolder",
                    (String) null);
            isPrimaryEntry = XmlUtil.getAttribute(node, ATTR_PRIMARY,
                    isPrimaryEntry);

            prefix = XmlUtil.getAttribute(node, "prefix", (String) null);
            value  = Utils.getAttributeOrTag(node, "value", "");
            dflt   = Utils.getAttributeOrTag(node, "default", "");
            name   = XmlUtil.getAttribute(node, ATTR_NAME, (String) null);

            if ((name == null) && isEntry() && isPrimaryEntry) {
                name = "input_file";
            }

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
            file = XmlUtil.getAttribute(node, ATTR_FILE, (String) null);
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
            first     = XmlUtil.getAttribute(node, "first", false);
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
            return service.getUrlArg() + ARG_DELIMITER + name;
        }


        /**
         * _more_
         *
         * @param entry _more_
         * @param debug _more_
         *
         * @return _more_
         */
        public boolean isApplicable(Entry entry, boolean debug) {
            boolean defaultReturn = true;
            //            debug  = true;

            if (debug) {
                System.err.println("Service.Arg.isApplicable:" + getName()
                                   + " entry type:" + entryType + " pattern:"
                                   + entryPattern);
            }
            if (entryType != null) {
                if ( !entry.getTypeHandler().isType(entryType)) {
                    if (debug) {
                        System.err.println("\tentry is not type");
                    }

                    return false;
                }
                if (entryPattern == null) {
                    if (debug) {
                        System.err.println("\thas entry type:" + entryType);
                    }

                    return true;
                }
            }
            if (entryPattern != null) {
                if (entry.getResource().getPath().toLowerCase().matches(
                        entryPattern)) {
                    return true;
                }

                return false;
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
            return group;
        }


        /**
         * _more_
         *
         * @return _more_
         */
        public String getGroupUrlArg() {
            if (group == null) {
                return null;
            }

            return service.getUrlArg() + ARG_DELIMITER + group;
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
     * _more_
     *
     * @return _more_
     */
    public Element getElement() {
        return element;
    }
}
