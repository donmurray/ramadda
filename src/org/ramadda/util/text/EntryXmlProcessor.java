
package org.ramadda.util.text;
import java.text.SimpleDateFormat;
import org.ramadda.util.text.*;
import org.ramadda.util.Utils;
import ucar.unidata.util.IOUtil;
import ucar.unidata.util.StringUtil;
import java.io.File;
import java.util.List;
import java.util.Date;
import java.util.HashSet;
import ucar.unidata.util.DateUtil;
import ucar.unidata.xml.XmlUtil;

public class EntryXmlProcessor extends Processor.RowCollector {

    SimpleDateFormat fsdf  = new SimpleDateFormat("yyyy-MM-dd");
    SimpleDateFormat sdf1  = new SimpleDateFormat("MM/yyyy");
    SimpleDateFormat sdf2  = new SimpleDateFormat("yyyy");
    public EntryXmlProcessor () {
        
    }

    public void finish(Visitor info) throws Exception {
        String template = null;
        if(new File("template.xml").exists()) {
            template = IOUtil.readContents("template.xml");
        } else {
            template = IOUtil.readContents("/org/ramadda/util/text/template.xml", EntryXmlProcessor.class);
        }
        List<Row> rows = getRows();
        StringBuilder sb  = new StringBuilder("<entries>\n");


        HashSet<String> seen = new HashSet<String>();

        for (int i = 1;i<rows.size();i++) {
            StringBuilder extra  = new StringBuilder("");
            String id = "assessment" + i;
            Row row = rows.get(i);
            String  s= template;
            List values =  row.getValues();
            for(int colIdx=0;colIdx<values.size();colIdx++) {
                Object v = values.get(colIdx);
                String sv = v.toString();
                sv = Utils.removeNonAscii(sv);
                s = s.replace("${" + colIdx +"}" , sv);
            }
            StringBuilder content = new StringBuilder();
            String date = values.get(11).toString();
            Date dttm  = null;
            if(Utils.stringDefined(date)) {
                try {
                    dttm  = sdf1.parse(date);
                } catch(Exception exc) {
                    dttm  = sdf2.parse(date);
                }
            }
            StringBuilder attrs = new StringBuilder();
            if(dttm!=null) {
                attrs.append(" fromdate=\"" + fsdf.format(dttm)+"\" ");
            }               

            String file = IOUtil.getFileTail(values.get(37).toString());


            if(seen.contains(file)) {
                //                System.err.println ("DUP:" + file);
                file = "2_" + file;
            }
            seen.add(file);
            if(!new File("files/" +file).exists()) {
                System.err.println ("no file:" + file);
            } else {
                extra.append(XmlUtil.tag("entry", XmlUtil.attrs(new String[]{"name",file, "parent", id ,"file", file})));
                extra.append("\n");
            }

            String doi = values.get(8).toString();
            if(Utils.stringDefined(doi)) {
                content.append("<metadata  type=\"doi_identifier\"><attr encoded=\"false\" index=\"2\"><![CDATA[" + doi +"]]></attr></metadata>\n");
            }

            for(String author: StringUtil.split(values.get(3).toString(),";",true,true)) {
                author = Utils.removeNonAscii(author);
                content.append("<metadata  type=\"metadata_author\"><attr encoded=\"false\" index=\"1\"><![CDATA[" + author +"]]></attr></metadata>\n");
            }




            s = s.replace("${id}" , id);
            s = s.replace("${content}" , content.toString());
            s = s.replace("${attrs}" , attrs.toString());

            sb.append(s);
            sb.append(extra);
        }
        sb.append("</entries>\n");
        System.out.println(sb);
    }


}