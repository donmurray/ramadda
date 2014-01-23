/*
* Copyright 2008-2013 Geode Systems LLC
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

package org.ramadda.data.point.netcdf;


import org.ramadda.data.point.*;


import org.ramadda.data.record.*;
import org.ramadda.util.Utils;

import ucar.ma2.DataType;

import ucar.nc2.*;
import ucar.nc2.dataset.NetcdfDataset;
import ucar.nc2.ft.*;
import ucar.nc2.jni.netcdf.Nc4Iosp;
import ucar.nc2.time.Calendar;
import ucar.nc2.time.CalendarDate;
import ucar.nc2.time.CalendarDateFormatter;
import ucar.nc2.time.CalendarDateRange;

import ucar.unidata.util.IOUtil;


import ucar.unidata.util.StringUtil;

import java.io.*;

import java.util.ArrayList;
import java.util.Hashtable;
import java.util.Formatter;
import java.util.List;



/**
 * Class description
 *
 *
 * @version        $version$, Fri, Aug 23, '13
 * @author         Enter your name here...
 */
public class NetcdfPointFile extends PointFile {

    /** _more_ */
    public static final String NETCDF_ATTR_SUMMARY = "summary";



    /**
     * ctor
     */
    public NetcdfPointFile() {}




    /**
     * ctor
     *
     * @param filename _more_
     * @throws IOException On badness
     */
    public NetcdfPointFile(String filename) throws IOException {
        super(filename);
    }


    /**
     * ctor
     *
     * @param filename _more_
     * @throws IOException On badness
     */
    public NetcdfPointFile(String filename, Hashtable properties) throws IOException {
        super(filename, properties);
    }



    /**
     * _more_
     *
     * @return _more_
     */
    public List<RecordField> doMakeFields() {
        
        Hashtable<String,RecordField> dfltFields = new Hashtable<String,RecordField>();
        String fieldsProperty = getProperty("fields","NONE");
        boolean defaultChartable = getProperty("chartable", "true").equals("true");
        if(fieldsProperty!=null) {
            List<RecordField> fields = doMakeFields(fieldsProperty);
            for(RecordField field: fields) {
                if(field.getChartable())  {
                    defaultChartable = false;
                }
                dfltFields.put(field.getName(), field);
            }
        }
        List<RecordField> fields = new ArrayList<RecordField>();
        try {
            int cnt = 1;
            fields.add(new RecordField("latitude", "Latitude", "Latitude",
                                       cnt++, "degrees"));
            fields.add(new RecordField("longitude", "Longitude", "Longitude",
                                       cnt++, "degrees"));

            RecordField dateField = new RecordField("date", "Date", "Date",
                                       cnt++, "");
            dateField.setType(dateField.TYPE_DATE);
            fields.add(dateField);

            FeatureDatasetPoint pod  = getDataset(getFilename());
            List                vars = pod.getDataVariables();
            for (VariableSimpleIF var : (List<VariableSimpleIF>) vars) {
                String label = var.getDescription();
                if ( !Utils.stringDefined(label)) {
                    label = var.getShortName();
                }
                String unit = var.getUnitsString();
                RecordField field = dfltFields.get(var.getShortName());
                if(field == null) {
                    field = new RecordField(var.getShortName(),
                                            label, label, cnt++, unit);
                    if ((var.getDataType() == DataType.STRING)
                        || (var.getDataType() == DataType.CHAR)) {
                        field.setType(field.TYPE_STRING);
                    } else {
                        field.setChartable(defaultChartable);
                        field.setSearchable(true);
                    }
                } else {
                    //                    System.err.println ("got default: " + field);
                }
                fields.add(field);
            }
            //            System.err.println ("fields: " + fields);
        } catch (Exception exc) {
            throw new RuntimeException(exc);
        }

        return fields;
    }

    /**
     * _more_
     *
     * @param input _more_
     *
     * @return _more_
     *
     * @throws Exception _more_
     */
    public static PointFeatureIterator getPointIterator(
            FeatureDatasetPoint input)
            throws Exception {
        List<FeatureCollection> collectionList =
            input.getPointFeatureCollectionList();
        if (collectionList.size() > 1) {
            throw new IllegalArgumentException(
                "Can't handle point data with multiple collections");
        }
        FeatureCollection      fc         = collectionList.get(0);
        PointFeatureCollection collection = null;
        if (fc instanceof PointFeatureCollection) {
            collection = (PointFeatureCollection) fc;
        } else if (fc instanceof NestedPointFeatureCollection) {
            NestedPointFeatureCollection npfc =
                (NestedPointFeatureCollection) fc;
            collection = npfc.flatten(null, (CalendarDateRange) null);
        } else {
            throw new IllegalArgumentException(
                "Can't handle collection of type " + fc.getClass().getName());
        }

        return collection.getPointFeatureIterator(16384);
    }



    /**
     * _more_
     *
     * @param action _more_
     *
     * @return _more_
     */
    @Override
    public boolean isCapable(String action) {
        if (action.equals(ACTION_TRACKS)) {
            return false;
        }
        if (action.equals(ACTION_MAPINCHART)) {
            return true;
        }

        //        if(action.equals(ACTION_BOUNDINGPOLYGON)) return false;
        return super.isCapable(action);
    }



    /**
     * _more_
     *
     * @param file _more_
     *
     * @return _more_
     */
    public boolean canLoad(String file) {
        if (true) {
            return false;
        }
        try {
            return file.endsWith(".nc");
        } catch (Exception exc) {
            return false;
        }
    }



    /**
     * This just passes through to FileType.doMakeRecord
     *
     *
     * @param visitInfo the visit info
     * @return the new record
     */
    public Record doMakeRecord(VisitInfo visitInfo) {
        try {
            FeatureDatasetPoint  pod          = getDataset(getFilename());
            PointFeatureIterator dataIterator = getPointIterator(pod);
            NetcdfRecord record = new NetcdfRecord(this, getFields(),
                                      dataIterator);

            return record;
        } catch (Exception exc) {
            throw new RuntimeException(exc);
        }
    }


    /**
     * _more_
     *
     * @param visitInfo _more_
     * @param record _more_
     * @param howMany _more_
     *
     * @return _more_
     *
     *
     * @throws Exception _more_
     */
    @Override
    public boolean skip(VisitInfo visitInfo, Record record, int howMany)
            throws Exception {
        visitInfo.addRecordIndex(howMany);
        while (howMany-- >= 0) {
            if (record.read(visitInfo.getRecordIO())
                    == Record.ReadStatus.EOF) {
                return false;
            }
        }

        return true;
    }




    /**
     * _more_
     *
     * @param visitInfo _more_
     *
     * @return _more_
     *
     *
     * @throws Exception _more_
     */
    public VisitInfo prepareToVisit(VisitInfo visitInfo) throws Exception {
        visitInfo.setRecordIO(readHeader(visitInfo.getRecordIO()));

        NetcdfDataset   dataset  = NetcdfDataset.openDataset(getFilename());
        String          platform = "";

        List<Attribute> attrs    = dataset.getGlobalAttributes();
        for (Attribute attr : attrs) {
            String name  = attr.getName();
            String value = attr.getStringValue();
            if (value == null) {
                continue;
            }
            if (name.equals(NETCDF_ATTR_SUMMARY)) {
                setDescriptionFromFile(value);
            } else {
                putFileProperty(name, value);
            }
        }
        dataset.close();

        return visitInfo;
    }


    /**
     * _more_
     *
     * @param recordIO _more_
     *
     * @return _more_
     *
     * @throws IOException _more_
     */
    @Override
    public RecordIO readHeader(RecordIO recordIO) throws IOException {
        //        recordIO.getDataInputStream().read(header);
        return recordIO;
    }


    /**
     * _more_
     *
     * @return _more_
     */
    public long getNumRecords() {
        if (super.getNumRecords() <= 0) {
            try {
                RecordCountVisitor visitor = new RecordCountVisitor();
                visit(visitor, new VisitInfo(true), null);
                setNumRecords(visitor.getCount());
            } catch (Exception exc) {
                throw new RuntimeException(exc);
            }
        }

        return super.getNumRecords();
    }




    /**
     * _more_
     *
     * @param args _more_
     *
     * @throws Exception _more_
     */
    public static void main(String[] args) throws Exception {
        PointFile.test(args, NetcdfPointFile.class);
    }



    /**
     * _more_
     *
     * @param path _more_
     *
     * @return _more_
     *
     * @throws Exception _more_
     */
    private FeatureDatasetPoint getDataset(String path) throws Exception {
        Formatter buf = new Formatter();
        FeatureDatasetPoint pods =
            (FeatureDatasetPoint) FeatureDatasetFactoryManager.open(
                ucar.nc2.constants.FeatureType.POINT, path, null, buf);
        if (pods == null) {  // try as ANY_POINT
            pods = (FeatureDatasetPoint) FeatureDatasetFactoryManager.open(
                ucar.nc2.constants.FeatureType.ANY_POINT, path, null, buf);
        }
        if (pods == null) {}

        return pods;
    }






}
