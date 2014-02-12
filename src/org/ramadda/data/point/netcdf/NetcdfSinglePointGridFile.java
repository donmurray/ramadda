package org.ramadda.data.point.netcdf;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Hashtable;
import java.util.List;

import org.ramadda.data.point.PointFile;
import org.ramadda.data.record.Record;
import org.ramadda.data.record.RecordField;
import org.ramadda.data.record.VisitInfo;
import org.ramadda.util.Utils;

import ucar.ma2.DataType;
import ucar.nc2.VariableSimpleIF;
import ucar.nc2.dt.GridCoordSystem;
import ucar.nc2.dt.GridDatatype;
import ucar.nc2.dt.grid.GridDataset;

public class NetcdfSinglePointGridFile extends PointFile {

    public NetcdfSinglePointGridFile() {
    }

    public NetcdfSinglePointGridFile(String filename) throws IOException {
        super(filename);
    }

    public NetcdfSinglePointGridFile(String filename, Hashtable properties)
            throws IOException {
        super(filename, properties);
    }

    @Override
    public Record doMakeRecord(VisitInfo visitInfo) {
        // TODO Auto-generated method stub
        return null;
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

            GridDataset gds  = getDataset(getFilename());
            List                vars = gds.getDataVariables();
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
     * @param path _more_
     *
     * @return _more_
     *
     * @throws Exception _more_
     */
    private GridDataset getDataset(String path) throws Exception {
        GridDataset gds = GridDataset.open(path);
        List<GridDatatype> grids = gds.getGrids();
        if (grids.size() == 0) {
            throw new Exception("No grids in file");
        }
        GridDatatype sample = grids.get(0);
        if (sample.getXDimension().getLength() != 1 &&
                sample.getYDimension().getLength() != 1) {
            throw new Exception("Not a single point grid");
        }
        return gds;
    }
    
    /**
     * @param args
     */
    public static void main(String[] args) {
        PointFile.test(args, NetcdfSinglePointGridFile.class);        
    }

}
