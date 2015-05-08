
package org.ramadda.util.text;
import java.text.SimpleDateFormat;
import org.ramadda.util.text.*;
import org.ramadda.util.Utils;
import ucar.unidata.util.IOUtil;
import ucar.unidata.util.StringUtil;
import java.io.File;
import java.util.List;
import java.util.Date;
import ucar.unidata.util.DateUtil;

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

        for (int i = 1;i<rows.size();i++) {
            Row row = rows.get(i);
            String  s= template;
            List values =  row.getValues();
            for(int colIdx=0;colIdx<values.size();colIdx++) {
                Object v = values.get(colIdx);
                s = s.replace("${" + colIdx +"}" , v.toString());
            }
            StringBuilder metadata = new StringBuilder();
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


            String doi = values.get(8).toString();
            if(Utils.stringDefined(doi)) {
                metadata.append("<metadata  type=\"doi_identifier\"><attr encoded=\"false\" index=\"2\"><![CDATA[" + doi +"]]></attr></metadata>\n");
            }

        //<metadata inherited="false" type="assessment_practice">      <attr index="1"><![CDATA[UmVmbGVjdGl2ZSBpbnRlcnZpZXdz]]></attr>    </metadata>
            for(String author: StringUtil.split(values.get(3).toString(),";",true,true)) {
                metadata.append("<metadata  type=\"metadata_author\"><attr encoded=\"false\" index=\"1\"><![CDATA[" + author +"]]></attr></metadata>\n");
            }




            s = s.replace("${metadata}" , metadata.toString());
            s = s.replace("${attrs}" , attrs.toString());

            sb.append(s);
        }
        sb.append("</entries>\n");
        System.out.println(sb);
    }


}