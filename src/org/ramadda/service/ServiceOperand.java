/*
 * Copyright (c) 2008-2015 Geode Systems LLC
 * This Software is licensed under the Geode Systems RAMADDA License available in the source distribution in the file 
 * ramadda_license.txt. The above copyright notice shall be included in all copies or substantial portions of the Software.
 */

package org.ramadda.service;


import org.ramadda.repository.Entry;

import ucar.unidata.util.Misc;

import java.util.ArrayList;
import java.util.List;


/**
 * Class to hold a set of entries for a ServiceInput or Output
 */
public class ServiceOperand {

    /** The list of entries for this operand */
    private List<Entry> entries;

    /** the description */
    private String description;

    /**
     * Create an operand from the entry
     * @param entry the entry
     */
    public ServiceOperand(Entry entry) {
        this(Misc.newList(entry));
    }

    /**
     * _more_
     *
     * @param description _more_
     * @param entry _more_
     */
    public ServiceOperand(String description, Entry entry) {
        this(description, Misc.newList(entry));
    }

    /**
     * Create an operand with a description and list of entries
     *
     * @param entries      the entries
     */
    public ServiceOperand(List<Entry> entries) {
        this("", entries);
    }

    /**
     * Create an operand with a description and list of entries
     *
     * @param description  the description
     * @param entries      the entries
     */
    public ServiceOperand(String description, List<Entry> entries) {
        this.description = description;
        this.entries     = entries;
    }

    /**
     * _more_
     *
     * @param entries _more_
     *
     * @return _more_
     */
    public static List<ServiceOperand> makeOperands(List<Entry> entries) {
        List<ServiceOperand> operands = new ArrayList<ServiceOperand>();
        if (entries == null) {
            return operands;
        }
        for (Entry entry : entries) {
            operands.add(new ServiceOperand(entry));
        }

        return operands;
    }


    /**
     * Get the entries
     *
     * @return  the entries
     */
    public List<Entry> getEntries() {
        return entries;
    }

    /**
     * Set the entries
     *
     * @param newEntries  the new entries
     */
    public void setEntries(List<Entry> newEntries) {
        entries = newEntries;
    }

    /**
     * Get the description of this operand
     *
     * @return  the description
     */
    public String getDescription() {
        return description;
    }

    /**
     * Set the description of the operand
     *
     * @param description  the description
     */
    public void setDescription(String description) {
        this.description = description;
    }

}
