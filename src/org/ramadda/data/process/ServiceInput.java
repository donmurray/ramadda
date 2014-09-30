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


import org.ramadda.repository.Entry;

import ucar.unidata.util.Misc;

import java.io.File;


import java.util.ArrayList;
import java.util.Hashtable;
import java.util.List;


/**
 * Class description
 *
 *
 */
public class ServiceInput {

    /** the process directory for this input */
    private File processDir;

    /** The operands for this input */
    private List<ServiceOperand> operands;

    /** _more_ */
    private Hashtable properties = new Hashtable();

    /** _more_ */
    private boolean publish = false;

    /** _more_ */
    private boolean forDisplay = false;

    /** _more_ */
    private List<String[]> params = new ArrayList<String[]>();

    /** _more_          */
    private Service sourceService;


    /**
     * _more_
     */
    public ServiceInput() {
        this.operands = new ArrayList<ServiceOperand>();
    }

    /**
     * _more_
     *
     * @param entry _more_
     */
    public ServiceInput(Entry entry) {
        this(new ServiceOperand(entry));
    }



    /**
     * Create a data process input
     *
     *
     * @param operand _more_
     */
    public ServiceInput(ServiceOperand operand) {
        this(null, Misc.newList(operand));
    }

    /**
     * _more_
     *
     * @param processDir _more_
     * @param entry _more_
     */
    public ServiceInput(File processDir, Entry entry) {
        this(processDir, Misc.newList(new ServiceOperand(entry)));
    }

    /**
     * _more_
     *
     * @param processDir _more_
     * @param entry _more_
     * @param entries _more_
     * @param dummy _more_
     */
    public ServiceInput(File processDir, List<Entry> entries, boolean dummy) {
        this(processDir, ServiceOperand.makeOperands(entries));
    }



    /**
     * Create a data process input
     *
     *
     * @param processDir _more_
     * @param operand _more_
     */
    public ServiceInput(File processDir, ServiceOperand operand) {
        this(processDir, Misc.newList(operand));
    }

    /**
     * Create a data process input
     *
     * @param operands  the operands for this process
     */
    public ServiceInput(List<ServiceOperand> operands) {
        // TODO: should we call this with a default directory?
        this(null, operands);
    }

    /**
     * Create a ServiceInput from a list of operands
     *
     *
     * @param dir process directory
     * @param operands the operands
     */
    public ServiceInput(File dir, List<ServiceOperand> operands) {
        this.processDir = dir;
        this.operands   = operands;
    }

    /**
     * _more_
     *
     * @param dir _more_
     */
    public ServiceInput(File dir) {
        this.processDir = dir;
        this.operands   = new ArrayList<ServiceOperand>();
    }

    /**
     * _more_
     *
     * @param key _more_
     * @param value _more_
     */
    public void putProperty(Object key, Object value) {
        properties.put(key, value);
    }

    /**
     * _more_
     *
     * @param key _more_
     *
     * @return _more_
     */
    public Object getProperty(Object key) {
        return getProperty(key, null);
    }

    /**
     * _more_
     *
     * @param key _more_
     * @param dflt _more_
     *
     * @return _more_
     */
    public Object getProperty(Object key, Object dflt) {
        Object value = properties.get(key);
        if (value == null) {
            return dflt;
        }

        return value;
    }

    /**
     * _more_
     *
     * @param key _more_
     * @param value _more_
     */
    public void addParam(String key, String value) {
        params.add(new String[] { key, value });
    }

    /**
     * _more_
     *
     * @return _more_
     */
    public List<String[]> getParams() {
        return params;
    }

    /**
     * _more_
     *
     * @return _more_
     */
    public List<Entry> getEntries() {
        List<Entry> entries = new ArrayList<Entry>();
        if (operands == null) {
            return entries;
        }
        for (ServiceOperand op : operands) {
            entries.addAll(op.getEntries());
        }

        return entries;
    }



    /**
     * Get the operands
     *
     * @return the list of operands
     */
    public List<ServiceOperand> getOperands() {
        return operands;
    }

    /**
     * Get the process directory
     *
     * @return  the process directory
     */
    public File getProcessDir() {
        return processDir;
    }


    /**
     *  Does this input have any operands?
     *  return true if it has operands
     *
     * @return _more_
     */
    public boolean hasOperands() {
        return (operands != null) && !operands.isEmpty();
    }

    /**
     * Create a ServiceInput from a ServiceOutput
     * @param output  the output
     * @return a new ServiceInput
     */
    public ServiceInput makeInput(ServiceOutput output) {
        ServiceInput input = new ServiceInput(
                                 this.getProcessDir(),
                                 new ArrayList<ServiceOperand>(
                                     output.getOperands()));
        input.setPublish(getPublish());
        input.setForDisplay(getForDisplay());
        input.params     = params;
        input.properties = properties;

        return input;
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
     * Set the ForDisplay property.
     *
     * @param value The new value for ForDisplay
     */
    public void setForDisplay(boolean value) {
        forDisplay = value;
    }

    /**
     * Get the ForDisplay property.
     *
     * @return The ForDisplay
     */
    public boolean getForDisplay() {
        return forDisplay;
    }

    /**
     *  Set the SourceService property.
     *
     *  @param value The new value for SourceService
     */
    public void setSourceService(Service value) {
        sourceService = value;
    }

    /**
     *  Get the SourceService property.
     *
     *  @return The SourceService
     */
    public Service getSourceService() {
        return sourceService;
    }





}
