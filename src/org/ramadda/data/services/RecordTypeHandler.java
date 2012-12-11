
package org.ramadda.data.services;

import org.ramadda.data.record.filter.*;

import org.ramadda.data.point.PointFile;
import org.ramadda.data.point.PointMetadataHarvester;
import org.ramadda.data.record.RecordFile;
import org.ramadda.data.record.RecordFileFactory;
import org.ramadda.data.record.RecordVisitorGroup;

import org.ramadda.data.record.VisitInfo;

import org.ramadda.data.services.RecordEntry;
import org.ramadda.data.services.PointEntry;

import org.ramadda.repository.*;
import org.ramadda.repository.map.*;
import org.ramadda.repository.metadata.*;
import org.ramadda.repository.output.*;
import org.ramadda.repository.type.*;

import org.ramadda.util.HtmlUtils;
import org.ramadda.util.grid.LatLonGrid;



import org.w3c.dom.*;


import java.io.BufferedOutputStream;
import java.io.ByteArrayInputStream;
import java.io.DataOutputStream;


import java.io.File;
import java.io.FileOutputStream;

import ucar.unidata.util.Misc;
import java.util.Hashtable;
import java.util.Date;
import java.lang.reflect.*;


import java.util.ArrayList;
import java.util.Hashtable;
import java.util.List;
import java.util.Properties;


/**
 *
 *
 * @author Jeff McWhirter
 * @version $Revision: 1.3 $
 */
public abstract  class RecordTypeHandler extends GenericTypeHandler implements RecordConstants {

    /** _more_ */
    private  RecordFileFactory recordFileFactory;

    private RecordOutputHandler recordOutputHandler;


    /**
     * _more_
     *
     * @param repository ramadda
     * @param node _more_
     * @throws Exception On badness
     */
    public RecordTypeHandler(Repository repository, Element node)
            throws Exception {
        super(repository, node);
    }

    public  RecordOutputHandler getRecordOutputHandler() {
        if(recordOutputHandler==null) {
            try {
                recordOutputHandler = doMakeRecordOutputHandler();
            } catch(Exception exc) {
                throw new RuntimeException(exc);
            }
        }
        return recordOutputHandler;
    }


    public  RecordOutputHandler doMakeRecordOutputHandler() throws Exception {
        return new RecordOutputHandler(getRepository(), null);
    }


    public boolean includedInRequest(Request request, RecordEntry recordEntry) throws Exception {
        return true;
    }



    /**
     * _more_
     *
     * @param request _more_
     * @param entry _more_
     * @param tabTitles _more_
     * @param tabContents _more_
     */
    public void addToInformationTabs(Request request, Entry entry,
                                     List<String> tabTitles,
                                     List<String> tabContents) {
        //        super.addToInformationTabs(request, entry, tabTitles, tabContents);
        try {
            RecordOutputHandler outputHandler = getRecordOutputHandler();
            if(outputHandler!=null) {
                tabTitles.add(msg("File Format"));
                StringBuffer sb         = new StringBuffer();
                RecordEntry   recordEntry = outputHandler.doMakeEntry(request,
                                                                      entry);
                outputHandler.getFormHandler().getEntryMetadata(request,
                                                                recordEntry, sb);
                tabContents.add(sb.toString());
            }
        } catch (Exception exc) {
            throw new RuntimeException(exc);

        }
    }



    /**
     * _more_
     *
     * @param request _more_
     * @param entry _more_
     * @param parent _more_
     * @param newEntry _more_
     *
     * @throws Exception On badness
     */
    @Override
    public void initializeEntryFromForm(Request request, Entry entry,
                                        Entry parent, boolean newEntry)
            throws Exception {
        //Don't call the super because this calls the parent.init method so
        //we end up initializing twice
        //        super.initializeEntryFromForm(request, entry, parent, newEntry);
        if ( !newEntry) {
            return;
        }
        initializeNewEntry(entry);
    }


    /**
     * _more_
     *
     * @param entry _more_
     * @param originalFile _more_
     *
     * @throws Exception _more_
     */
    public void initializeEntry(Entry entry, File originalFile)
            throws Exception {
        Hashtable existingProperties = getRecordProperties(entry);
        //        System.err.println("initializeEntry:" + originalFile + " props:" + existingProperties);
        if ((existingProperties != null) && (existingProperties.size() > 0)) {
            return;
        }

        //Look around for properties files that define
        //the crs, fields for text formats, etc.
        Hashtable properties =
            RecordFile.getPropertiesForFile(originalFile.toString(),
                                            PointFile.DFLT_PROPERTIES_FILE);
        //        System.err.println("from file:" + existingProperties +" " + PointFile.DFLT_PROPERTIES_FILE);
        //Make the properties string

        String contents = makePropertiesString(properties);
        Object[] values   = entry.getValues();
        if (values == null) {
            values = new Object[2];
        }
        //Append the properties file contents
        if(values[1]!=null) {
            values[1] = "\n" + contents;
        } else {
            values[1] =  contents;
        }
    }

    public String makePropertiesString(Hashtable properties) {
        StringBuffer sb = new StringBuffer();
        for (java.util.Enumeration keys = properties.keys();
             keys.hasMoreElements(); ) {
            Object key = keys.nextElement();
            sb.append(key);
            sb.append("=");
            sb.append(properties.get(key));
            sb.append("\n");
        }
        return  sb.toString();
    }


    /**
     * _more_
     *
     * @param msg _more_
     */
    public void log(String msg) {
        getRepository().getLogManager().logInfo("RecordTypeHandler:" + msg);
    }



    /**
     * _more_
     *
     * @param entry _more_
     *
     * @throws Exception On badness
     */
    @Override
    public void initializeNewEntry(Entry entry) throws Exception {
    }

    /**
     * _more_
     *
     * @param pointEntry _more_
     *
     * @return _more_
     */
    public PointMetadataHarvester doMakeMetadataHarvester(
                                                          RecordEntry pointEntry) {
        return new PointMetadataHarvester();
    }

    /**
     * _more_
     *
     * @param pointEntry _more_
     * @param metadata _more_
     *
     * @throws Exception _more_
     */
    protected void handleHarvestedMetadata(RecordEntry recordEntry,
                                           PointMetadataHarvester metadata)
            throws Exception {

        PointEntry pointEntry = (PointEntry) recordEntry;
        Entry entry = pointEntry.getEntry();
        if (pointEntry.getRecordFile().isCapable(PointFile.ACTION_BOUNDINGPOLYGON)) {
            if ( !entry.hasMetadataOfType(
                    MetadataHandler.TYPE_SPATIAL_POLYGON)) {
                LatLonGrid llg = new LatLonGrid(80, 40,
                                     metadata.getMaxLatitude(),
                                     metadata.getMinLongitude(),
                                     metadata.getMinLatitude(),
                                     metadata.getMaxLongitude());


                PointMetadataHarvester metadata2 =
                    new PointMetadataHarvester(llg);
                pointEntry.getBinaryPointFile().visit(metadata2,
                        new VisitInfo(true), null);
                List<double[]> polygon = llg.getBoundingPolygon();
                StringBuffer[] sb = new StringBuffer[] { new StringBuffer(),
                        new StringBuffer(), new StringBuffer(),
                        new StringBuffer() };
                int idx = 0;
                for (double[] point : polygon) {
                    String toAdd = point[0] + "," + point[1] + ";";
                    if ((sb[idx].length() + toAdd.length())
                            >= (Metadata.MAX_LENGTH - 100)) {
                        idx++;
                        if (idx >= sb.length) {
                            break;
                        }
                    }
                    sb[idx].append(toAdd);
                }
                //                System.err.println ("sb length:" + sb[idx].length() +" " +Metadata.MAX_LENGTH);

                Metadata polygonMetadata =
                    new Metadata(getRepository().getGUID(), entry.getId(),
                                 MetadataHandler.TYPE_SPATIAL_POLYGON,
                                 DFLT_INHERITED, sb[0].toString(),
                                 sb[1].toString(), sb[2].toString(),
                                 sb[3].toString(), Metadata.DFLT_EXTRA);
                entry.addMetadata(polygonMetadata, false);
            }
        }

        Object[] values = entry.getValues();
        if (values == null) {
            values = new Object[2];
        }
        values[0] = new Integer(metadata.getCount());
        entry.setValues(values);
        entry.setNorth(metadata.getMaxLatitude());
        entry.setSouth(metadata.getMinLatitude());
        entry.setEast(metadata.getMaxLongitude());
        entry.setWest(metadata.getMinLongitude());
        if ( !Double.isNaN(metadata.getMinElevation())) {
            entry.setAltitudeBottom(metadata.getMinElevation());
        }
        if ( !Double.isNaN(metadata.getMaxElevation())) {
            entry.setAltitudeTop(metadata.getMaxElevation());
        }

        if (metadata.hasTimeRange()) {
            entry.setStartDate(metadata.getMinTime());
            entry.setEndDate(metadata.getMaxTime());
            System.err.println("has time:" + new Date(entry.getStartDate()) +"  --  " + new Date(entry.getEndDate()));
        } else {
            System.err.println("does not have time");
        }
        entry.setCategory(getEntryCategory(entry));
    }


    public String getEntryCategory(Entry entry) {
        return getProperty("entry.category", "");
    }

    /**
     * _more_
     *
     * @param entry _more_
     * @param originalEntry _more_
     *
     * @throws Exception On badness
     */
    public void initializeCopiedEntry(Entry entry, Entry originalEntry)
            throws Exception {
        super.initializeCopiedEntry(entry, originalEntry);
        initializeNewEntry(entry);
    }


    /**
     * _more_
     *
     * @param entry _more_
     *
     * @return _more_
     *
     * @throws Exception On badness
     */
    public Hashtable getRecordProperties(Entry entry) throws Exception {
        String   propertiesString = null;
        Object[] values           = entry.getValues();
        if ((values != null) && (values.length >= 2) && (values[1] != null)) {
            propertiesString = values[1].toString();
        }
        if (propertiesString != null) {
            Properties p = new Properties();
            p.load(new ByteArrayInputStream(propertiesString.getBytes()));

            return p;
        }

        return null;
    }

    /**
     * _more_
     *
     * @param entry _more_
     *
     * @return _more_
     *
     * @throws Exception On badness
     */
    public RecordFile doMakeRecordFile(Entry entry) throws Exception {
        String recordFileClass= getProperty("record.file.class",(String) null);
        if(recordFileClass!= null)  {
            return doMakeRecordFile(entry, recordFileClass);
        }
        String path = entry.getFile().toString();
        return (RecordFile) getRecordFileFactory().doMakeRecordFile(path,
                                                                    getRecordProperties(entry));
    }


    private RecordFile doMakeRecordFile(Entry entry, String className) throws Exception {
        Class c = Misc.findClass(className);
        Constructor ctor = Misc.findConstructor(c,
                                                new Class[] { String.class, Hashtable.class });
        if(ctor!=null) {
            return (RecordFile) ctor.newInstance(new Object[]{entry.getFile().toString(),
                                                              getRecordProperties(entry)});
        }
        ctor = Misc.findConstructor(c,
                                    new Class[] { String.class});
        if(ctor!=null) {
            return (RecordFile) ctor.newInstance(new Object[]{entry.getFile().toString()});
            
        }
        throw new IllegalArgumentException("Could not find constructor for " +className);
    }


    public void getFilters(Request request, Entry entry,
                           RecordFile recordFile, List<RecordFilter> filters) {
    }

    /**
     * _more_
     *
     * @param f _more_
     *
     * @param path _more_
     * @param filename _more_
     *
     * @return _more_
     */
    @Override
    public boolean canHandleResource(String path, String filename) {
        try {
            boolean ok = getRecordFileFactory().canLoad(path);
            return ok;
        } catch (Exception exc) {
            //If the point loading flaked out then just keep going
            //            logException("Harvesting file:" + f, exc);
            return false;
        }
    }



    /**
     * _more_
     *
     * @return _more_
     */
    public  RecordFileFactory getRecordFileFactory() {
        if(recordFileFactory==null) {
            recordFileFactory = doMakeRecordFileFactory();
        }
        return recordFileFactory;
    }


    public  RecordFileFactory doMakeRecordFileFactory() {
        return new RecordFileFactory();
    }


    /**
     * _more_
     *
     * @param path _more_
     *
     * @return _more_
     *
     * @throws Exception On badness
     */
    public boolean isRecordFile(String path) throws Exception {
        return getRecordFileFactory().canLoad(path);
    }



    /**
     * _more_
     *
     * @param s _more_
     *
     * @return _more_
     */
    public String macro(String s) {
        return "${" + s + "}";
    }

    /**
     * _more_
     *
     * @param request _more_
     * @param entry _more_
     * @param services _more_
     *
     * @return _more_
     */
    @Override
    public void getServices(Request request, Entry entry,
                            List<Service> services) {
        super.getServices(request, entry, services);
        getRecordOutputHandler().getServices(request, entry, services);
    }



    /**
     * _more_
     *
     *
     * @param request _more_
     * @param icon _more_
     *
     * @return _more_
     */
    public String getIconUrl(Request request, String icon) {
        return request.getAbsoluteUrl(getRepository().iconUrl(icon));
    }


}
