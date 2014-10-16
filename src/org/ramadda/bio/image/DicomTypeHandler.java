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

package org.ramadda.bio.image;



import org.ramadda.data.process.Service;
import org.ramadda.data.process.ServiceOutput;
import org.ramadda.repository.*;
import org.ramadda.repository.metadata.Metadata;
import org.ramadda.repository.type.*;
import org.ramadda.util.Utils;



import org.w3c.dom.*;

import ucar.nc2.units.DateUnit;

import ucar.unidata.util.IOUtil;
import ucar.unidata.util.Misc;
import ucar.unidata.util.StringUtil;
import ucar.unidata.xml.XmlUtil;

import java.io.File;


import java.util.Date;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.List;


/**
 *
 *
 * @author Jeff McWhirter
 */
public class DicomTypeHandler extends GenericTypeHandler {

    /** _more_ */
    private Hashtable<String, Tag> tags = new Hashtable<String, Tag>();

    /** _more_ */
    private HashSet<String> metadataTags = new HashSet<String>();




    /**
     * _more_
     *
     * @param repository _more_
     * @param node _more_
     * @throws Exception On badness
     */
    public DicomTypeHandler(Repository repository, Element node)
            throws Exception {
        super(repository, node);
        init(node);
    }


    /** _more_ */
    public static final String TAG_ATTR = "attr";

    /** _more_ */
    public static final String ATTR_TAG = "tag";

    /** _more_ */
    public static final String ATTR_VR = "vr";

    /** _more_ */
    public static final String ATTR_LEN = "len";

    /** _more_ */
    private static final String DEFAULT_TAGS = "00100010,00100020";

    /**
     * _more_
     *
     * @param node _more_
     *
     * @throws Exception _more_
     */
    private void init(Element node) throws Exception {
        List<String> lines =
            StringUtil.split(
                getRepository().getResource(
                    "/org/ramadda/bio/image/dicomtags.txt"), "\n", true,
                        true);
        for (int i = 0; i < lines.size(); i += 2) {
            Tag tag = new Tag(lines.get(i), lines.get(i + 1));
            tags.put(tag.id, tag);
            tags.put(tag.name, tag);
        }

        String metadataString =
            getProperty("dicom.metadata",
                        getRepository().getProperty("dicom.metadata", ""));
        for (String tok : StringUtil.split(metadataString, ",", true, true)) {
            Tag tag = getTag(tok);
            metadataTags.add(tag.id);
        }
    }




    /**
     * _more_
     *
     * @param entry _more_
     * @param service _more_
     * @param output _more_
     *
     * @throws Exception _more_
     */
    @Override
    public void handleServiceResults(Entry entry, Service service,
                                     ServiceOutput output)
            throws Exception {
        super.handleServiceResults(entry, service, output);

        List<Entry> entries = output.getEntries();
        if (entries.size() == 0) {
            return;
        }
        String filename = entries.get(0).getFile().toString();
        if ( !filename.endsWith(".xml")) {
            return;
        }
        String xml = IOUtil.readContents(filename, getClass(), "");
        Element                   root       = root = XmlUtil.getRoot(xml);
        NodeList children = XmlUtil.getElements(root, TAG_ATTR);
        Hashtable<String, String> tagToValue = new Hashtable<String,
                                                   String>();




        //<attr tag="00020000" vr="UL" len="4">194</attr>
        for (int childIdx = 0; childIdx < children.getLength(); childIdx++) {
            Element item = (Element) children.item(childIdx);
            String tagId = XmlUtil.getAttribute(item, ATTR_TAG,
                               (String) null);
            Tag tag = getTag(tagId);
            if (tag == null) {
                continue;
            }
            String value = XmlUtil.getChildText(item);
            tagToValue.put(tag.id, value);
            tagToValue.put(tag.name, value);
            if (metadataTags.contains(tag.id)
                    || metadataTags.contains(tag.name)) {
                Metadata metadata = new Metadata(getRepository().getGUID(),
                                        entry.getId(), "bio_dicom_attr",
                                        false, tag.name, value, null, null,
                                        null);

                entry.addMetadata(metadata);
            }
        }

        Object[]     values  = getEntryValues(entry);
        List<Column> columns = getColumns();
        for (Column column : columns) {
            String value = tagToValue.get(column.getName());
            if ((value == null) && (column.getAlias() != null)) {
                value = tagToValue.get(column.getAlias());
            }
            if (value == null) {
                continue;
            }
            column.setValue(entry, values, value);
        }

    }


    /**
     * _more_
     *
     * @param s _more_
     *
     * @return _more_
     */
    public Tag getTag(String s) {
        if (s == null) {
            return null;
        }

        return tags.get(s);
    }



    /**
     * Class description
     *
     *
     * @version        $version$, Thu, Oct 2, '14
     * @author         Enter your name here...
     */
    public static class Tag {

        /** _more_ */
        String id;

        /** _more_ */
        String name;

        /**
         * _more_
         *
         * @param id _more_
         * @param name _more_
         */
        public Tag(String id, String name) {
            this.id   = id;
            this.name = name;
        }

        /**
         * _more_
         *
         * @return _more_
         */
        public String toString() {
            return id + ":" + name;
        }

    }


}
