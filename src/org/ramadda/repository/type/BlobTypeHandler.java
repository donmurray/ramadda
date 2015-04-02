/**
* Copyright (c) 2008-2015 Geode Systems LLC
* This Software is licensed under the Geode Systems RAMADDA License available in the source distribution in the file 
* ramadda_license.txt. The above copyright notice shall be included in all copies or substantial portions of the Software.
*/

/**
 * Copyright (c) 2008-2015 Geode Systems LLC
 * This Software is licensed under the Geode Systems RAMADDA License available in the source distribution in the file
 * ramadda_license.txt. The above copyright notice shall be included in all copies or substantial portions of the Software.
 */

package org.ramadda.repository.type;


import org.ramadda.repository.*;




import org.w3c.dom.*;

import ucar.unidata.xml.XmlUtil;


import java.util.Hashtable;





/**
 *
 *
 * @author RAMADDA Development Team
 * @version $Revision: 1.3 $
 */
public class BlobTypeHandler extends GenericTypeHandler {



    /**
     * _more_
     *
     * @param repository _more_
     * @param entryNode _more_
     *
     * @throws Exception _more_
     */
    public BlobTypeHandler(Repository repository, Element entryNode)
            throws Exception {
        super(repository, entryNode);
    }


    /**
     * _more_
     *
     * @param repository _more_
     * @param type _more_
     * @param description _more_
     */
    public BlobTypeHandler(Repository repository, String type,
                           String description) {
        super(repository, type, description);
    }



    /**
     * _more_
     *
     * @param entry _more_
     * @param key _more_
     * @param value _more_
     *
     * @throws Exception _more_
     */
    public void putEntryProperty(Entry entry, String key, Object value)
            throws Exception {
        Hashtable props = getProperties(entry);
        props.put(key, value);
        setProperties(entry, props);
    }


    /**
     * _more_
     *
     * @param entry _more_
     *
     * @return _more_
     *
     * @throws Exception _more_
     */
    protected Hashtable getProperties(Entry entry) throws Exception {
        if (entry == null) {
            return new Hashtable();
        }
        Hashtable properties = null;
        if (properties == null) {
            Object[] values = entry.getValues();
            if ((values != null) && (values.length > 0)
                    && (values[0] != null)) {
                properties =
                    (Hashtable) Repository.decodeObject((String) values[0]);
            }
            if (properties == null) {
                properties = new Hashtable();
            }
        }

        return properties;
    }

    /**
     * _more_
     *
     * @param entry _more_
     * @param properties _more_
     *
     * @throws Exception _more_
     */
    protected void setProperties(Entry entry, Hashtable properties)
            throws Exception {
        entry.setValues(new Object[] { Repository.encodeObject(properties) });
    }



}
