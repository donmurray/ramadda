/*
 * Copyright (c) 2008-2015 Geode Systems LLC
 * This Software is licensed under the Geode Systems RAMADDA License available in the source distribution in the file 
 * ramadda_license.txt. The above copyright notice shall be included in all copies or substantial portions of the Software.
 */

package org.ramadda.data.point.text;


import org.ramadda.data.point.*;
import org.ramadda.data.point.text.*;

import org.ramadda.data.record.*;

import ucar.unidata.util.Misc;
import ucar.unidata.util.StringUtil;

import java.io.*;

import java.util.ArrayList;
import java.util.Date;
import java.util.Hashtable;
import java.util.List;



/**
 */
public abstract class SingleSiteTextFile extends CsvFile {

    /**
     * _more_
     */
    public SingleSiteTextFile() {}

    /**
     * ctor
     *
     *
     * @param filename _more_
     *
     * @throws IOException _more_
     */
    public SingleSiteTextFile(String filename) throws IOException {
        super(filename);
    }

    /**
     * _more_
     *
     * @param filename _more_
     * @param properties _more_
     *
     * @throws IOException _more_
     */
    public SingleSiteTextFile(String filename, Hashtable properties)
            throws IOException {
        super(filename, properties);
    }


    /**
     *  Since these are single station files don't do bounds, etc
     *
     * @param action _more_
     *
     * @return _more_
     */
    public boolean isCapable(String action) {
        if (action.equals(ACTION_BOUNDINGPOLYGON)) {
            return false;
        }
        if (action.equals(ACTION_GRID)) {
            return false;
        }

        return super.isCapable(action);
    }


}
