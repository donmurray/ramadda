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


package org.ramadda.plugins.metameta;



import org.ramadda.repository.*;
import org.ramadda.repository.output.*;
import org.ramadda.repository.type.*;

import org.ramadda.util.HtmlUtils;
import org.ramadda.util.Utils;

import org.w3c.dom.*;

import ucar.unidata.util.Misc;
import ucar.unidata.util.StringUtil;
import ucar.unidata.xml.XmlUtil;

import java.io.*;

import java.util.ArrayList;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.List;


/**
 * Generated TypeHandler code. Do not edit
 *
 *
 * @author RAMADDA Development Team
 */
public class MetadataFieldTypeHandlerBase extends  ExtensibleGroupTypeHandler {

    	private static int INDEX_BASE = 0;
	public static final int INDEX_FIELD_INDEX = INDEX_BASE + 0;
	public static final int INDEX_FIELD_ID = INDEX_BASE + 1;
	public static final int INDEX_DATATYPE = INDEX_BASE + 2;
	public static final int INDEX_ENUMERATION_VALUES = INDEX_BASE + 3;
	public static final int INDEX_PROPERTIES = INDEX_BASE + 4;
	public static final int INDEX_DATABASE_COLUMN_SIZE = INDEX_BASE + 5;
	public static final int INDEX_MISSING = INDEX_BASE + 6;
	public static final int INDEX_UNIT = INDEX_BASE + 7;




    /**
     * _more_
     *
     * @param repository _more_
     * @param entryNode _more_
     *
     * @throws Exception _more_
     */
    public MetadataFieldTypeHandlerBase(Repository repository,
                        Element entryNode)
            throws Exception {
        super(repository, entryNode);
    }


    


    /**
     * If this entry type is a group then this method gets called to create the default HTML display
     *
     * @param request request
     * @param parent the parent entry
     * @param subGroups child groups
     * @param entries child entries
     *
     * @return result
     *
     * @throws Exception on badness
     */
    @Override
    public Result getHtmlDisplay(Request request, Entry parent,
                                 List<Entry> subGroups, List<Entry> entries)
            throws Exception {
        return super.getHtmlDisplay(request, parent, subGroups, entries);
    }

    @Override
    public Result getHtmlDisplay(Request request, Entry entry)
            throws Exception {
        return super.getHtmlDisplay(request, entry);
    }

    /**
     * _more_
     *
     * @param request _more_
     * @param entry _more_
     *
     * @return _more_
     *
     * @throws Exception _more_
     */
    @Override
    public Result processEntryAccess(Request request, Entry entry)
            throws Exception {
        return super.processEntryAccess(request, entry);
    }




}
