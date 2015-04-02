/**
* Copyright (c) 2008-2015 Geode Systems LLC
* This Software is licensed under the Geode Systems RAMADDA License available in the source distribution in the file 
* ramadda_license.txt. The above copyright notice shall be included in all copies or substantial portions of the Software.
*/

package org.ramadda.plugins.metameta;


import org.ramadda.repository.*;
import org.ramadda.repository.type.*;
import org.ramadda.util.CategoryBuffer;
import org.ramadda.util.HtmlUtils;
import org.ramadda.util.Utils;


import org.w3c.dom.Element;

import ucar.unidata.util.StringUtil;
import ucar.unidata.util.TwoFacedObject;

import ucar.unidata.xml.XmlUtil;

import java.util.Hashtable;
import java.util.List;


/**
 * Class description
 *
 *
 * @version        $version$, Sat, Dec 28, '13
 * @author         Enter your name here...
 */
public class MetametaApiHandler extends RepositoryManager implements RequestHandler {


    /**
     * _more_
     *
     * @param repository _more_
     * @param node _more_
     * @param props _more_
     *
     * @throws Exception _more_
     */
    public MetametaApiHandler(Repository repository, Element node,
                              Hashtable props)
            throws Exception {
        super(repository);
    }


    /**
     * _more_
     *
     * @param request _more_
     *
     * @return _more_
     *
     * @throws Exception _more_
     */
    public Result processTypeList(Request request) throws Exception {
        CategoryBuffer cb = new CategoryBuffer();
        for (TypeHandler typeHandler : getRepository().getTypeHandlers()) {
            if ( !typeHandler.getForUser()) {
                continue;
            }
            StringBuffer buff = new StringBuffer();
            String       icon = typeHandler.getProperty("icon",
                                    (String) null);
            if (icon != null) {
                buff.append(HtmlUtils.img(getRepository().iconUrl(icon)));
                buff.append(HtmlUtils.space(1));
            }
            buff.append(
                HtmlUtils.href(
                    getRepository().getUrlBase() + "/metameta/type/"
                    + typeHandler.getType(), typeHandler.getLabel()));
            buff.append("<br>\n");
            cb.append(typeHandler.getCategory(), buff);
        }

        StringBuffer sb = new StringBuffer();
        sb.append(
            "Below are RAMADDA entry import files that allow you to view and modify the entry metadata<p>");

        getPageHandler().doTableLayout(request, sb, cb);

        return new Result("Metameta", sb);
    }


    /**
     *  This will be the entry point for querying on the types
     *
     * @param request _more_
     *
     * @return _more_
     *
     * @throws Exception _more_
     */
    public Result processTypeRequest(Request request) throws Exception {

        List<String> toks = StringUtil.split(request.getRequestPath(), "/",
                                             true, true);
        String       type        = toks.get(toks.size() - 1);
        TypeHandler  typeHandler = getRepository().getTypeHandler(type);
        StringBuffer xml = new StringBuffer(XmlUtil.openTag(TAG_ENTRIES));
        xml.append("\n");
        StringBuffer inner = new StringBuffer();
        inner.append(
            XmlUtil.tag(
                MetametaDictionaryTypeHandler.FIELD_PROPERTIES, "",
                XmlUtil.getCdata(
                    Utils.makeProperties(typeHandler.getProperties()))));


        inner.append(
            XmlUtil.tag(
                MetametaDictionaryTypeHandler.FIELD_SHORT_NAME, "",
                typeHandler.getType()));

        inner.append(
            XmlUtil.tag(
                MetametaDictionaryTypeHandler.FIELD_DICTIONARY_TYPE, "",
                "entry"));

        if (typeHandler.getParent() != null) {
            inner.append(
                XmlUtil.tag(
                    MetametaDictionaryTypeHandler.FIELD_SUPER_TYPE, "",
                    typeHandler.getParent().getType()));
        }
        if (typeHandler.isGroup()) {
            inner.append(
                XmlUtil.tag(
                    MetametaDictionaryTypeHandler.FIELD_ISGROUP, "", "true"));
        }
        inner.append(
            XmlUtil.tag(
                MetametaDictionaryTypeHandler.FIELD_HANDLER_CLASS, "",
                typeHandler.getClass().getName()));


        xml.append(
            XmlUtil.tag(
                TAG_ENTRY,
                XmlUtil.attrs(
                    ATTR_NAME, "Data Dictionary: " + typeHandler.getLabel(),
                    ATTR_TYPE, MetametaDictionaryTypeHandler.TYPE, ATTR_ID,
                    "dictionary"), inner.toString()));

        List<Column> columns = typeHandler.getColumns();
        if (columns != null) {
            int index = 0;
            for (Column column : columns) {
                index++;
                inner = new StringBuffer();
                StringBuffer              attrs = new StringBuffer();

                Hashtable<String, String> enums = column.getEnumTable();
                if ((enums != null) && (enums.size() > 0)) {
                    inner.append(XmlUtil
                        .tag(MetametaFieldTypeHandler
                            .FIELD_ENUMERATION_VALUES, "",
                                XmlUtil.getCdata(Utils
                                    .makeProperties(enums))));
                }

                inner.append(
                    XmlUtil.tag(
                        MetametaFieldTypeHandler.FIELD_FIELD_INDEX, "",
                        "" + index));
                inner.append(
                    XmlUtil.tag(
                        MetametaFieldTypeHandler.FIELD_FIELD_ID, "",
                        column.getName()));
                inner.append(
                    XmlUtil.tag(
                        MetametaFieldTypeHandler.FIELD_DATATYPE, "",
                        column.getType()));

                StringBuffer props = new StringBuffer();
                String       dflt  = column.getDflt();
                if (Utils.stringDefined(dflt)) {
                    props.append(Column.ATTR_DEFAULT);
                    props.append("=");
                    props.append(dflt);
                    props.append("\n");
                }


                props.append(Utils.makeProperties(column.getProperties()));

                inner.append(
                    XmlUtil.tag(
                        MetametaFieldTypeHandler.FIELD_PROPERTIES, "",
                        XmlUtil.getCdata(props.toString())));

                inner.append(
                    XmlUtil.tag(
                        MetametaFieldTypeHandler.FIELD_DATABASE_COLUMN_SIZE,
                        "", "" + column.getSize()));


                attrs.append(XmlUtil.attrs(ATTR_NAME, column.getLabel(),
                                           ATTR_TYPE,
                                           MetametaFieldTypeHandler.TYPE,
                                           ATTR_PARENT, "dictionary"));

                xml.append(XmlUtil.tag(TAG_ENTRY, attrs.toString(),
                                       inner.toString()));

            }
        }

        xml.append(XmlUtil.closeTag(TAG_ENTRIES));

        return new Result("Type", xml, "text/xml");

    }

}
