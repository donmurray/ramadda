/*
 * Copyright 1997-2010 Unidata Program Center/University Corporation for Atmospheric Research
 * Copyright 2010- Jeff McWhirter
 * 
 * This library is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation; either version 2.1 of the License, or (at
 * your option) any later version.
 * 
 * This library is distributed in the hope that it will be useful, but
 * WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU Lesser
 * General Public License for more details.
 * 
 * You should have received a copy of the GNU Lesser General Public License
 * along with this library; if not, write to the Free Software Foundation,
 * Inc., 59 Temple Place, Suite 330, Boston, MA 02111-1307 USA
 * 
 */

package org.ramadda.repository.metadata;


import org.w3c.dom.*;

import org.ramadda.repository.*;


import ucar.unidata.ui.ImageUtils;
import ucar.unidata.util.HtmlUtil;
import ucar.unidata.util.IOUtil;
import ucar.unidata.util.Misc;
import ucar.unidata.util.StringUtil;
import ucar.unidata.util.TwoFacedObject;

import ucar.unidata.xml.XmlUtil;

import java.awt.Image;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.InputStream;

import java.net.URL;
import java.net.URLConnection;

import java.util.ArrayList;
import java.util.Hashtable;
import java.util.List;



/**
 *
 *
 * @author IDV Development Team
 * @version $Revision: 1.3 $
 */
public class MetadataTypeBase extends RepositoryManager {

    /** _more_ */
    public static final String TAG_TYPE = "type";

    /** _more_ */
    public static final String TAG_ELEMENT = "element";

    /** _more_ */
    public static final String TAG_TEMPLATE = "template";

    /** _more_ */
    public static final String ATTR_FILE = "file";

    /** _more_ */
    public static final String ATTR_TAG = "tag";

    /** _more_ */
    public static final String ATTR_TYPE = "type";


    /** _more_ */
    public static final String ATTR_NAME = "name";

    /** _more_ */
    public static final String ATTR_SEARCHABLE = "searchable";



    /** _more_ */
    public static final String ATTR_SHOWINHTML = "showinhtml";


    /** _more_ */
    public static final String TEMPLATETYPE_THREDDS = "thredds";

    /** _more_ */
    public static final String TEMPLATETYPE_DIF = "dif";


    /** _more_ */
    public static final String TEMPLATETYPE_ISO = "iso";


    /** _more_ */
    public static final String TEMPLATETYPE_OAIDC = "oai_dc";

    /** _more_ */
    public static final String TEMPLATETYPE_HTML = "html";

    /** _more_ */
    private String name;

    /** _more_ */
    private boolean showInHtml = true;


    /** _more_ */
    List<MetadataElement> children = new ArrayList<MetadataElement>();


    /** _more_ */
    private Hashtable<String, String> templates = new Hashtable<String,
                                                      String>();


    /** _more_ */
    MetadataHandler handler;

    /** _more_ */
    private boolean searchable = false;

    /** _more_ */
    private Hashtable<String, String> tags = new Hashtable<String, String>();


    /**
     * _more_
     *
     *
     * @param handler _more_
     */
    public MetadataTypeBase(MetadataHandler handler) {
        super(handler.getRepository());
        this.handler = handler;
    }


    /**
     *  Set the Handler property.
     *
     *  @param value The new value for Handler
     */
    public void setHandler(MetadataHandler value) {
        this.handler = value;
    }

    /**
     *  Get the Handler property.
     *
     *  @return The Handler
     */
    public MetadataHandler getHandler() {
        return this.handler;
    }

    /**
     * _more_
     *
     * @return _more_
     */
    public String toString() {
        return name;
    }


    /**
     * _more_
     *
     * @param templateType _more_
     * @param entry _more_
     * @param metadata _more_
     * @param parent _more_
     *
     * @throws Exception _more_
     */
    protected void checkFileXml(String templateType, Entry entry,
                                Metadata metadata, Element parent)
            throws Exception {
        for (MetadataElement element : getChildren()) {
            if ( !element.getDataType().equals(element.TYPE_FILE)) {
                continue;
            }
            File f = getFile(entry, metadata, element);
            if (f == null) {
                continue;
            }
            String tail = getStorageManager().getFileTail(f.toString());
            String path =
                handler.getRepository().getMetadataManager().URL_METADATA_VIEW
                    .getFullUrl("/" + tail);
            String url = HtmlUtil.url(path, ARG_ELEMENT,
                                      element.getIndex() + "", ARG_ENTRYID,
                                      metadata.getEntryId(), ARG_METADATA_ID,
                                      metadata.getId());
            //TODO:
            if (templateType.equals(TEMPLATETYPE_THREDDS)) {
                XmlUtil.create(parent.getOwnerDocument(), "property", parent,
                               new String[] { "name", (element.getThumbnail()
                        ? "thumbnail"
                        : "attachment"), "value", url });
            }

        }
    }


    /**
     * _more_
     *
     * @param templateType _more_
     * @param entry _more_
     * @param metadata _more_
     * @param parent _more_
     *
     * @return _more_
     *
     * @throws Exception _more_
     */
    public String applyTemplate(String templateType, Entry entry,
                                Metadata metadata, Element parent)
            throws Exception {
        checkFileXml(templateType, entry, metadata, parent);

        String template = getTemplate(templateType);
        if ((template == null) || (template.length() == 0)) {
            return null;
        }
        //Remove newlines??
        template = template.replaceAll("\n","");
        template = template.replace("${root}", getRepository().getUrlBase());

        for (MetadataElement element : getChildren()) {
            String value = element.getValueForXml(templateType, entry,
                               metadata,
                               metadata.getAttr(element.getIndex()), parent);

            template = applyMacros(template, element, value);

        }
        return template;
    }


    /**
     * _more_
     *
     * @param template _more_
     * @param element _more_
     * @param value _more_
     *
     * @return _more_
     */
    public String applyMacros(String template, MetadataElement element,
                              String value) {
        if (value == null) {
            value = "";
        }
        //        value = XmlUtil.encodeString(value);
        String   label = element.getLabel(value);
        String   name  = element.getName();
        String[] keys  = {
            "attr" + element.getIndex(), name, name.toLowerCase(),
            name.replace(" ", "_"), name.toLowerCase().replace(" ", "_"),
            element.getId()
        };
        Hashtable macros = new Hashtable();
        macros.put("name", name);
        //      template = template.replaceAll("\\${name}", name);
        for (String key : keys) {
            if (key == null) {
                continue;
            }
            //                System.err.println("key: " + key);
            //            template = template.replaceAll("\\${" + key + "}", value);
            //            template = template.replaceAll("\\${" + key + ".label}", label);
            //            template = template.replaceAll("\\${" + key + ".cdata}",
            //                                        "<![CDATA[" + value + "]]>");
            macros.put(key, value);
            macros.put(key + ".label", label);
            macros.put(key + ".cdata", "<![CDATA[" + value + "]]>");
        }
        template = StringUtil.applyMacros(template, macros, false);
        template = template.replaceAll("\r\n\r\n", "<p>");
        template = template.replace("\n\n", "<p>");
        return template;
    }

    /**
     * _more_
     *
     * @return _more_
     */
    public boolean hasElements() {
        return getChildren().size() > 0;
    }


    /**
     * _more_
     *
     * @param what _more_
     *
     * @return _more_
     */
    public String getTag(String what) {
        return tags.get(what + ".tag");
    }

    /**
     * _more_
     *
     * @param node _more_
     *
     * @throws Exception _more_
     */
    public void init(Element node) throws Exception {
        setName(XmlUtil.getAttribute(node, ATTR_NAME, ""));
        setShowInHtml(XmlUtil.getAttribute(node, ATTR_SHOWINHTML, true));
        setSearchable(XmlUtil.getAttributeFromTree(node, ATTR_SEARCHABLE,
                false));

        NamedNodeMap nnm = node.getAttributes();
        if (nnm != null) {
            for (int i = 0; i < nnm.getLength(); i++) {
                Attr   attr     = (Attr) nnm.item(i);
                String attrName = attr.getNodeName();
                if (attrName.endsWith(".tag")) {
                    tags.put(attrName, attr.getNodeValue());
                }
            }
        }

        NodeList children = XmlUtil.getElements(node);
        for (int i = 0; i < children.getLength(); i++) {
            Element childNode = (Element) children.item(i);
            if (childNode.getTagName().equals(TAG_TEMPLATE)) {
                String templateType = XmlUtil.getAttribute(childNode,
                                          ATTR_TYPE);
                if (XmlUtil.hasAttribute(childNode, ATTR_FILE)) {
                    templates.put(
                        templateType,
                        getStorageManager().readSystemResource(
                            XmlUtil.getAttribute(childNode, ATTR_FILE)));
                } else {
                    templates.put(templateType,
                                  XmlUtil.getChildText(childNode));
                }
            } else if (childNode.getTagName().equals(TAG_ELEMENT)) {}
            else {
                logError("Unknown metadata xml tag:"
                         + childNode.getTagName(), null);
            }
        }

        List childrenElements = XmlUtil.findChildren(node, TAG_ELEMENT);
        int  lastIndex        = 0;
        for (int j = 0; j < childrenElements.size(); j++) {
            Element elementNode = (Element) childrenElements.get(j);
            int     index       = lastIndex + 1;
            if (XmlUtil.hasAttribute(elementNode,
                                     MetadataElement.ATTR_INDEX)) {
                index = XmlUtil.getAttribute(elementNode,
                                             MetadataElement.ATTR_INDEX,
                                             index);
            }
            lastIndex = index;
            MetadataElement element = new MetadataElement(getHandler(), this,
                                          lastIndex, elementNode);
            addElement(element);
        }

    }


    /**
     * _more_
     *
     * @return _more_
     */
    public List<MetadataElement> getChildren() {
        return children;
    }

    /**
     * _more_
     *
     * @param element _more_
     */
    public void addElement(MetadataElement element) {
        getChildren().add(element);
    }


    /**
     * _more_
     *
     * @param request _more_
     * @param entry _more_
     * @param metadata _more_
     * @param matchFile _more_
     *
     * @return _more_
     *
     * @throws Exception _more_
     */
    public String getImageUrl(Request request, Entry entry,
                              Metadata metadata, String matchFile)
            throws Exception {
        for (MetadataElement element : getChildren()) {
            if ( !element.getDataType().equals(element.TYPE_FILE)) {
                continue;
            }

            File f = getFile(entry, metadata, element);
            if (f == null) {
                String value = metadata.getAttr(element.getIndex());
                if ((value != null) && value.startsWith("http")) {
                    return value;
                }
                return null;
            }

            String tail = getStorageManager().getFileTail(f.toString());
            if ((matchFile != null) && !Misc.equals(matchFile, tail)) {
                continue;
            }
            if (ImageUtils.isImage(f.toString())) {
                String path =
                    handler.getRepository().getMetadataManager()
                        .URL_METADATA_VIEW + "/" + tail;

                return HtmlUtil.url(path, ARG_ELEMENT,
                                    element.getIndex() + "", ARG_ENTRYID,
                                    metadata.getEntryId(), ARG_METADATA_ID,
                                    metadata.getId());
            }
        }
        return null;
    }

    /**
     * _more_
     *
     * @param request _more_
     * @param entry _more_
     * @param metadata _more_
     * @param element _more_
     * @param forLink _more_
     *
     * @return _more_
     */
    public String getFileHtml(Request request, Entry entry,
                              Metadata metadata, MetadataElement element,
                              boolean forLink) {
        File f = getFile(entry, metadata, element);
        if ((f == null) || !f.exists() || f.isDirectory()) {
            return null;
        }

        String extra = (forLink
                        ? " "
                        : "");
        String tail  = getStorageManager().getFileTail(f.toString());
        String path =
            handler.getRepository().getMetadataManager().URL_METADATA_VIEW
            + "/" + tail;


        if (ImageUtils.isImage(f.toString())) {
            String img = HtmlUtil.img(HtmlUtil.url(path, ARG_ELEMENT,
                             element.getIndex() + "", ARG_ENTRYID,
                             metadata.getEntryId(), ARG_METADATA_ID,
                             metadata.getId(), ARG_THUMBNAIL,
                             "" + forLink), (forLink
                                             ? msg("Click to enlarge")
                                             : ""), extra);

            if (forLink) {
                String bigimg = HtmlUtil.img(HtmlUtil.url(path, ARG_ELEMENT,
                                    element.getIndex() + "", ARG_ENTRYID,
                                    metadata.getEntryId(), ARG_METADATA_ID,
                                    metadata.getId()), "thumbnail", "");


                String imgUrl = HtmlUtil.url(path, ARG_ELEMENT,
                                             element.getIndex() + "",
                                             ARG_ENTRYID,
                                             metadata.getEntryId(),
                                             ARG_METADATA_ID,
                                             metadata.getId());

                img = handler.getRepository().makePopupLink(img, bigimg,
                        true, false);
            } else {
                img = img + "\n<br>\n<b>" + tail + "</b>\n";
            }
            return img;
        } else if (f.exists()) {
            String name = getStorageManager().getFileTail(f.getName());
            return HtmlUtil.href(HtmlUtil.url(path, ARG_ELEMENT,
                    element.getIndex() + "", ARG_ENTRYID,
                    metadata.getEntryId(), ARG_METADATA_ID,
                    metadata.getId()), name);
        }
        return "";
    }


    /**
     * _more_
     *
     * @param entry _more_
     * @param metadata _more_
     * @param attr _more_
     *
     * @return _more_
     */
    public File getFile(Entry entry, Metadata metadata, int attr) {
        String filename = metadata.getAttr(attr);
        if ((filename == null) || (filename.trim().length() == 0)) {
            return null;
        }
        return new File(
            IOUtil.joinDir(
                getStorageManager().getEntryDir(
                    metadata.getEntryId(), false), filename));
    }


    /**
     * _more_
     *
     * @param entry _more_
     * @param metadata _more_
     * @param element _more_
     *
     * @return _more_
     */
    public File getFile(Entry entry, Metadata metadata,
                        MetadataElement element) {
        File f;
        if ( !entry.getIsLocalFile()) {
            f = getFile(entry, metadata, element.getIndex());
        } else {
            f = new File(metadata.getAttr(element.getIndex()));
        }

        if ((f == null) || !f.exists()) {
            return null;
        }
        return f;
    }



    /**
     * Set the Name property.
     *
     * @param value The new value for Name
     */
    public void setName(String value) {
        name = value;
    }

    /**
     * Get the Name property.
     *
     * @return The Name
     */
    public String getName() {
        return name;
    }

    /**
     * _more_
     *
     * @return _more_
     */
    public String getLabel() {
        return name;
    }

    /**
     *  Set the ShowInHtml property.
     *
     *  @param value The new value for ShowInHtml
     */
    public void setShowInHtml(boolean value) {
        this.showInHtml = value;
    }

    /**
     *  Get the ShowInHtml property.
     *
     *  @return The ShowInHtml
     */
    public boolean getShowInHtml() {
        return this.showInHtml;
    }

    /**
     * Get the Template property.
     *
     *
     * @param type _more_
     * @return The Template
     */
    public String getTemplate(String type) {
        return templates.get(type);
    }

    /**
     * Set the Searchable property.
     *
     * @param value The new value for Searchable
     */
    public void setSearchable(boolean value) {
        this.searchable = value;
    }

    /**
     * Get the Searchable property.
     *
     * @return The Searchable
     */
    public boolean getSearchable() {
        return this.searchable;
    }


}
