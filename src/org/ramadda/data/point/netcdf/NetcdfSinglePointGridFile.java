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

package org.ramadda.data.point.netcdf;


import org.ramadda.data.point.PointFile;
import org.ramadda.data.record.Record;
import org.ramadda.data.record.RecordField;
import org.ramadda.data.record.RecordIO;
import org.ramadda.data.record.VisitInfo;
import org.ramadda.util.Utils;

import ucar.ma2.DataType;

import ucar.nc2.VariableSimpleIF;
import ucar.nc2.dataset.CoordinateAxis1D;
import ucar.nc2.dt.GridCoordSystem;
import ucar.nc2.dt.GridDatatype;
import ucar.nc2.dt.grid.GridDataset;
import ucar.nc2.ft.FeatureDatasetPoint;
import ucar.nc2.ft.PointFeatureIterator;

import java.io.IOException;

import java.util.ArrayList;
import java.util.Hashtable;
import java.util.List;


/**
 * Class description
 *
 *
 * @version        $version$, Thu, Feb 13, '14
 * @author         Enter your name here...
 */
public class NetcdfSinglePointGridFile extends PointFile {

    /**
     * Default constructor
     */
    public NetcdfSinglePointGridFile() {}

    /**
     * Create a NetcdfSinglePointGridFile from the file
     *
     * @param filename  the filename
     *
     * @throws IOException problem opening file
     */
    public NetcdfSinglePointGridFile(String filename) throws IOException {
        super(filename);
    }

    /**
     * Create a NetcdfSinglePointGridFile from the file and properties
     *
     * @param filename  the filename
     * @param properties  properties
     *
     * @throws IOException problem opening file
     */
    public NetcdfSinglePointGridFile(String filename, Hashtable properties)
            throws IOException {
        super(filename, properties);
    }

    /**
     * Make the record
     *
     * @param visitInfo  the visitInfo
     *
     * @return the Record
     */
    @Override
    public Record doMakeRecord(VisitInfo visitInfo) {
        try {
            GridDataset gds = getDataset(getFilename());
            NetcdfSinglePointGridRecord record =
                new NetcdfSinglePointGridRecord(this, getFields(), gds);

            return record;
        } catch (Exception exc) {
            throw new RuntimeException(exc);
        }
    }

    /**
     * Make the fields
     *
     * @return the list of fields
     */
    public List<RecordField> doMakeFields() {

        Hashtable<String, RecordField> dfltFields = new Hashtable<String,
                                                        RecordField>();
        String fieldsProperty = getProperty("fields", "NONE");
        boolean defaultChartable = getProperty("chartable",
                                       "true").equals("true");
        if (fieldsProperty != null) {
            List<RecordField> fields = doMakeFields(fieldsProperty);
            for (RecordField field : fields) {
                if (field.getChartable()) {
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

            GridDataset gds = getDataset(getFilename());
            for (GridDatatype var : gds.getGrids()) {
                String label = var.getDescription();
                if ( !Utils.stringDefined(label)) {
                    label = var.getShortName();
                }
                String      unit  = var.getUnitsString();
                RecordField field = dfltFields.get(var.getShortName());
                if (field == null) {
                    field = new RecordField(var.getShortName(), label, label,
                                            cnt++, unit);
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
     * Get the dataset for this
     *
     * @param path  the path to the data
     *
     * @return  the GridDataset
     *
     * @throws Exception  not a valid grid, or problem opening grid
     */
    private GridDataset getDataset(String path) throws Exception {
        GridDataset        gds   = GridDataset.open(path);
        List<GridDatatype> grids = gds.getGrids();
        if (grids.size() == 0) {
            throw new Exception("No grids in file");
        }
        GridDatatype sample = grids.get(0);
        if ((sample.getXDimension().getLength() != 1)
                && (sample.getYDimension().getLength() != 1)) {
            throw new Exception("Not a single point grid");
        }
        GridCoordSystem  gcs       = sample.getCoordinateSystem();
        CoordinateAxis1D lons      = (CoordinateAxis1D) gcs.getXHorizAxis();
        double lon = lons.getCoordValue(0);
        CoordinateAxis1D lats = (CoordinateAxis1D) gcs.getYHorizAxis();
        double lat          = lats.getCoordValue(0);
        setLocation(lat, lon, 0);

        return gds;
    }

    /**
     * @param args
     */
    public static void main(String[] args) {
        PointFile.test(args, NetcdfSinglePointGridFile.class);
    }

}
