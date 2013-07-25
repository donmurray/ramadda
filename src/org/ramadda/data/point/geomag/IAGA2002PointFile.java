package org.ramadda.data.point.geomag;

import org.ramadda.data.record.*;
import org.ramadda.data.point.*;
import org.ramadda.data.point.text.*;

import ucar.unidata.util.Misc;
import ucar.unidata.util.StringUtil;

import java.io.*;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;



/**
*/
public class IAGA2002PointFile extends CsvFile  {


    private SimpleDateFormat sdf = makeDateFormat("yyyy-MM-dd HH:mm:ss");
    
    public static final String FIELD_DATE  = "DATE";
    public static final String FIELD_TIME  = "TIME";
    public static final String FIELD_DOY  = "DOY";



    /**
     * The constructor
     *
     * @param filename file
     * @throws IOException On badness
     */
    public IAGA2002PointFile(String filename) throws IOException {
        super(filename);
    }

    public boolean isHeaderStandard() {
        return true;
    }

    public boolean isHeaderLine(String line) {
        return line.endsWith("|");
    }



    /**
     * This  gets called before the file is visited. 
     * It reads the header and defines the fields
     *
     * @param visitInfo visit info
     * @return possible new visitinfo
     * @throws IOException On badness
     */
    public VisitInfo prepareToVisit(VisitInfo visitInfo) throws IOException {
        //Set the delimiter and how many lines in the header to skip
        putProperty(PROP_DELIMITER, "");
        super.prepareToVisit(visitInfo);

        //Read the header and make sure things are cool
        List<String>headerLines = getHeaderLines();
        /*
Format                 IAGA-2002                                    |
 Source of Data         Sveriges geologiska undersokning             |
 Station Name           Abisko                                       |
 IAGA CODE              ABK                                          |
 Geodetic Latitude      68.400                                       |
 Geodetic Longitude     18.800                                       |
 Elevation                                                           |
 Reported               XYZF                                         |
 Sensor Orientation                                                  |
 Digital Sampling                                                    |
 Data Interval Type     1-minute                                     |
 Data Type              variation      
        */
        int idx =0;
        String format = getHeaderValue(headerLines.get(idx++));
        String source = getHeaderValue(headerLines.get(idx++));
        String station = getHeaderValue(headerLines.get(idx++));
        String iagaCode = getHeaderValue(headerLines.get(idx++));
        String latitude = getHeaderValue(headerLines.get(idx++));
        String longitude = getHeaderValue(headerLines.get(idx++));
        String elevation = getHeaderValue(headerLines.get(idx++));
        if(elevation.length()==0) elevation="0";

        String reported = getHeaderValue(headerLines.get(idx++));
        String orientation = getHeaderValue(headerLines.get(idx++));
        String sampling = getHeaderValue(headerLines.get(idx++));
        String interval = getHeaderValue(headerLines.get(idx++));
        String dataType = getHeaderValue(headerLines.get(idx++));

        StringBuffer sb = new StringBuffer();
        sb.append(FIELD_DATE);
        sb.append("[type=string]");
        sb.append(",");

        sb.append(FIELD_TIME);
        sb.append("[type=string]");
        sb.append(",");

        sb.append(FIELD_DOY);
        sb.append("[]");
        sb.append(",");

        sb.append(FIELD_LATITUDE);
        sb.append("[value=" + latitude+"]");
        sb.append(",");
        sb.append(FIELD_LONGITUDE);
        sb.append("[value=" + longitude+"]");
        sb.append(",");
        sb.append(FIELD_ELEVATION);
        sb.append("[value=" + elevation+"]");
        
        for(int i=0;i<reported.length();i++) {
            char c  = reported.charAt(i);
            sb.append(",");
            sb.append(c);
            sb.append("[chartable=true searchable=true missing=99999]");
        }



        putProperty(PROP_FIELDS, sb.toString());
        return visitInfo;
    }

    public String getHeaderValue(String line) {
        String s =  line.substring(23).trim();
        s = s.substring(0,s.length()-1);
        s = s.trim();
        return s;
    }


    public boolean isCapable(String action) {
        if(action.equals(ACTION_MAPINCHART)) return true;
        if(action.equals(ACTION_BOUNDINGPOLYGON)) return true;
        return super.isCapable(action);
    }

    /*
     * This gets called after a record has been read
     */
    public boolean processAfterReading(VisitInfo visitInfo, Record record) throws Exception {
        if(!super.processAfterReading(visitInfo, record)) return false;
        TextRecord textRecord = (TextRecord) record;
        String dateString = textRecord.getStringValue(1);
        String timeString = textRecord.getStringValue(2);
        StringBuffer dttm  =new StringBuffer();
        dttm.append(dateString);
        dttm.append(" ");
        dttm.append(timeString);
        Date date = sdf.parse(dttm.toString());
        record.setRecordTime(date.getTime());
        return true;
    }

    public static void main(String[]args) {
        PointFile.test(args, IAGA2002PointFile.class);
    }

}
