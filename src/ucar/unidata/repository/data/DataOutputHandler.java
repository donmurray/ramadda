/*
 * Copyright 1997-2010 Unidata Program Center/University Corporation for
 * Atmospheric Research, P.O. Box 3000, Boulder, CO 80307,
 * support@unidata.ucar.edu.
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
 */

package ucar.unidata.repository.data;


import opendap.dap.DAP2Exception;



import opendap.dap.parser.ParseException;

import opendap.servlet.GuardedDataset;
import opendap.servlet.ReqState;

//import ucar.nc2.dt.PointObsDataset;
//import ucar.nc2.dt.PointObsDatatype;

import org.apache.commons.httpclient.*;
import org.apache.commons.httpclient.auth.*;

import org.w3c.dom.*;



import thredds.server.ncSubset.GridPointWriter;
import thredds.server.ncSubset.QueryParams;

import thredds.server.opendap.GuardedDatasetImpl;



import ucar.ma2.Array;
import ucar.ma2.DataType;
import ucar.ma2.StructureData;
import ucar.ma2.StructureMembers;

import ucar.nc2.Attribute;

import ucar.nc2.NetcdfFile;
import ucar.nc2.Variable;
import ucar.nc2.VariableSimpleIF;

import ucar.nc2.constants.AxisType;
import ucar.nc2.constants.FeatureType;
import ucar.nc2.dataset.CoordinateAxis;
import ucar.nc2.dataset.CoordinateAxis1DTime;
import ucar.nc2.dataset.CoordinateSystem;
import ucar.nc2.dataset.NetcdfDataset;
import ucar.nc2.dataset.VariableDS;
import ucar.nc2.dataset.VariableEnhanced;



import ucar.nc2.dt.GridCoordSystem;
import ucar.nc2.dt.GridDatatype;
import ucar.nc2.dt.TrajectoryObsDataset;
import ucar.nc2.dt.TrajectoryObsDatatype;
import ucar.nc2.dt.TypedDatasetFactory;

import ucar.nc2.dt.grid.GridDataset;
import ucar.nc2.dt.grid.NetcdfCFWriter;
import ucar.nc2.dt.trajectory.TrajectoryObsDatasetFactory;

import ucar.nc2.ft.FeatureCollection;

import ucar.nc2.ft.FeatureDatasetFactoryManager;
import ucar.nc2.ft.FeatureDatasetPoint;
import ucar.nc2.ft.FeatureDatasetPoint;
import ucar.nc2.ft.NestedPointFeatureCollection;
import ucar.nc2.ft.PointFeature;
import ucar.nc2.ft.PointFeatureCollection;
import ucar.nc2.ft.PointFeatureIterator;
import ucar.nc2.ft.point.*;
import ucar.nc2.units.DateFormatter;
import ucar.nc2.units.DateType;
import ucar.nc2.util.DiskCache2;

import ucar.unidata.data.gis.KmlUtil;
import ucar.unidata.geoloc.LatLonPointImpl;

import ucar.unidata.geoloc.LatLonRect;

import ucar.unidata.repository.*;
import ucar.unidata.repository.auth.*;
import ucar.unidata.repository.metadata.*;
import ucar.unidata.repository.output.*;




import ucar.unidata.util.Cache;
import ucar.unidata.util.Counter;

import ucar.unidata.util.HtmlUtil;
import ucar.unidata.util.HtmlUtil;
import ucar.unidata.util.IOUtil;
import ucar.unidata.util.Misc;
import ucar.unidata.util.Pool;
import ucar.unidata.util.StringUtil;
import ucar.unidata.util.TemporaryDir;
import ucar.unidata.util.WrapperException;
import ucar.unidata.xml.XmlUtil;

import java.io.*;

import java.net.*;




import java.util.ArrayList;

import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.Enumeration;
import java.util.Formatter;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.List;
import java.util.Properties;

import javax.servlet.*;

import javax.servlet.http.*;


/**
 *
 *
 * @author IDV Development Team
 * @version $Revision: 1.3 $
 */
public class DataOutputHandler extends OutputHandler {

    public static final String VAR_PREFIX =  ARG_VARIABLE + ".";

    /** _more_ */
    public static final String ARG_ADDLATLON = "addlatlon";

    /** _more_ */
    public static final String ARG_ADDTOREPOSITORY = "addtorepository";

    /** _more_ */
    public static final String ARG_SUBSETAREA = "subsetarea";

    /** _more_ */
    public static final String ARG_SUBSETLOCATION = "subsetlocation";

    /** _more_ */
    public static final String ARG_SUBSETTIME = "subsettime";

    /** _more_ */
    public static final String ARG_HSTRIDE = "hstride";

    /** _more_ */
    public static final OutputType OUTPUT_OPENDAP =
        new OutputType("OpenDAP", "data.opendap", OutputType.TYPE_NONHTML,
                       OutputType.SUFFIX_NONE, ICON_OPENDAP);

    /** _more_ */
    public static final OutputType OUTPUT_CDL = new OutputType("CDL",
                                                    "data.cdl",
                                                    OutputType.TYPE_HTML,
                                                    OutputType.SUFFIX_NONE,
                                                    ICON_DATA);

    /** _more_ */
    public static final OutputType OUTPUT_WCS = new OutputType("WCS",
                                                    "data.wcs",
                                                    OutputType.TYPE_NONHTML);

    /** _more_ */
    public static final OutputType OUTPUT_POINT_MAP =
        new OutputType("Point as Map", "data.point.map",
                       OutputType.TYPE_HTML, OutputType.SUFFIX_NONE,
                       ICON_MAP);

    /** _more_ */
    public static final OutputType OUTPUT_POINT_CSV =
        new OutputType("Point as CSV", "data.point.csv",
                       OutputType.TYPE_NONHTML, OutputType.SUFFIX_NONE,
                       ICON_CSV);

    /** _more_ */
    public static final OutputType OUTPUT_POINT_KML =
        new OutputType("Point as KML", "data.point.kml",
                       OutputType.TYPE_NONHTML, "", ICON_KML);

    /** _more_ */
    public static final OutputType OUTPUT_TRAJECTORY_MAP =
        new OutputType("Trajectory as Map", "data.trajectory.map",
                       OutputType.TYPE_HTML, OutputType.SUFFIX_NONE,
                       ICON_MAP);

    /** _more_ */
    public static final OutputType OUTPUT_GRIDSUBSET_FORM =
        new OutputType("Grid Subset", "data.gridsubset.form",
                       OutputType.TYPE_HTML, OutputType.SUFFIX_NONE,
                       ICON_SUBSET);

    /** _more_ */
    public static final OutputType OUTPUT_GRIDSUBSET =
        new OutputType("data.gridsubset", OutputType.TYPE_NONHTML);

    /** _more_ */
    public static final OutputType OUTPUT_GRIDASPOINT_FORM =
        new OutputType("Grid As Point Data", "data.gridaspoint.form",
                       OutputType.TYPE_HTML, OutputType.SUFFIX_NONE,
                       ICON_SUBSET);

    /** _more_ */
    public static final OutputType OUTPUT_GRIDASPOINT =
        new OutputType("data.gridaspoint", OutputType.TYPE_NONHTML);

    /** _more_ */
    private Cache<String, Boolean> cdmEntries = new Cache<String,
                                                    Boolean>(5000);

    /** _more_ */
    private Cache<String, Boolean> gridEntries = new Cache<String,
                                                     Boolean>(5000);


    /** _more_ */
    private Cache<String, Boolean> pointEntries = new Cache<String,
                                                      Boolean>(5000);

    /** _more_ */
    private Cache<String, Boolean> trajectoryEntries = new Cache<String,
                                                           Boolean>(5000);


    /** _more_ */
    private TemporaryDir nj22Dir;

    /** _more_ */
    private TemporaryDir dataCacheDir;


    //TODO: When we close a ncfile some thread might be using it
    //Do we have to actually close it??

    /** _more_ */
    Counter ncCounter = new Counter();

    /** _more_ */
    Counter ncCreateCounter = new Counter();

    /** _more_ */
    Counter ncRemoveCounter = new Counter();

    /** _more_ */
    Counter ncGetCounter = new Counter();

    /** _more_ */
    Counter ncPutCounter = new Counter();

    /** _more_ */
    Counter extCounter = new Counter();

    /** _more_ */
    Counter opendapCounter = new Counter();


    /** _more_ */
    Counter gridOpenCounter = new Counter();

    /** _more_ */
    Counter gridCloseCounter = new Counter();


    /** _more_ */
    Counter pointOpenCounter = new Counter();

    /** _more_ */
    Counter pointCloseCounter = new Counter();

    /** _more_ */
    private Pool<String, NetcdfDataset> ncDatasetPool =
        new Pool<String, NetcdfDataset>(10) {
        protected void removeValue(String key, NetcdfDataset dataset) {
            try {
                super.removeValue(key, dataset);
                ncRemoveCounter.incr();
                dataset.close();
            } catch (Exception exc) {
                System.err.println("Error closing:" + key);
                exc.printStackTrace();
            }
        }

        /*
          public synchronized void put(String key, NetcdfDataset file) {
            ncPutCounter.incr();
            super.put(key, file);
            }*/

        protected NetcdfDataset getFromPool(List<NetcdfDataset> list) {
            NetcdfDataset dataset = super.getFromPool(list);
            ncGetCounter.incr();
            try {
                dataset.sync();
                return dataset;
            } catch (Exception exc) {
                throw new RuntimeException(exc);
            }
        }


        protected NetcdfDataset createValue(String path) {
            try {
                getStorageManager().dirTouched(nj22Dir, null);
                NetcdfDataset dataset = NetcdfDataset.openDataset(path);
                //                NetcdfDataset dataset = NetcdfDataset.openFile(path);
                ncCreateCounter.incr();
                return dataset;
            } catch (Exception exc) {
                throw new RuntimeException(exc);
            }
        }
    };




    /** _more_ */
    private Pool<String, NetcdfFile> ncFilePool = new Pool<String,
                                                      NetcdfFile>(10) {
        protected void removeValue(String key, NetcdfFile ncFile) {
            try {
                super.removeValue(key, ncFile);
                ncRemoveCounter.incr();
                ncFile.close();
            } catch (Exception exc) {
                System.err.println("Error closing:" + key);
                exc.printStackTrace();
            }
        }

        /*
        public synchronized void put(String key, NetcdfFile ncFile) {
            ncPutCounter.incr();
            super.put(key, ncFile);
            }*/

        protected NetcdfFile getFromPool(List<NetcdfFile> list) {
            NetcdfFile ncFile = super.getFromPool(list);
            ncGetCounter.incr();
            try {
                ncFile.sync();
                return ncFile;
            } catch (Exception exc) {
                throw new RuntimeException(exc);
            }
        }


        protected NetcdfFile createValue(String path) {
            try {
                getStorageManager().dirTouched(nj22Dir, null);
                //                NetcdfDataset dataset = NetcdfDataset.openDataset(path);
                NetcdfFile ncFile = NetcdfDataset.openFile(path, null);
                ncCreateCounter.incr();
                return ncFile;
            } catch (Exception exc) {
                throw new RuntimeException(exc);
            }
        }
    };


    /** _more_ */
    private Pool<String, GridDataset> gridPool = new Pool<String,
                                                     GridDataset>(10) {
        protected void removeValue(String key, GridDataset dataset) {
            try {
                super.removeValue(key, dataset);
                gridCloseCounter.incr();
                dataset.close();
            } catch (Exception exc) {}
        }

        protected GridDataset getFromPool(List<GridDataset> list) {
            GridDataset dataset = super.getFromPool(list);
            try {
                dataset.sync();
                return dataset;
            } catch (Exception exc) {
                throw new RuntimeException(exc);
            }

        }


        protected GridDataset createValue(String path) {
            try {
                getStorageManager().dirTouched(nj22Dir, null);
                gridOpenCounter.incr();

                GridDataset gds = GridDataset.open(path);
                if (gds.getGrids().iterator().hasNext()) {
                    return gds;
                } else {
                    gridCloseCounter.incr();
                    gds.close();
                    return null;
                }
            } catch (Exception exc) {
                throw new RuntimeException(exc);
            }
        }

    };


    /** _more_ */
    private Pool<String, FeatureDatasetPoint> pointPool =
        new Pool<String, FeatureDatasetPoint>(10) {
        protected void removeValue(String key, FeatureDatasetPoint dataset) {
            try {
                super.removeValue(key, dataset);
                dataset.close();
            } catch (Exception exc) {}
        }

        /*
        protected  FeatureDatasetPoint getFromPool(List<FeatureDatasetPoint> list) {
            FeatureDatasetPoint dataset = super.getFromPool(list);
            dataset.sync();
            return dataset;
            }*/

        protected FeatureDatasetPoint createValue(String path) {
            try {
                Formatter buf = new Formatter();
                getStorageManager().dirTouched(nj22Dir, null);

                FeatureDatasetPoint pods =
                    (FeatureDatasetPoint) FeatureDatasetFactoryManager.open(
                        ucar.nc2.constants.FeatureType.POINT, path, null,
                        buf);
                if (pods == null) {  // try as ANY_POINT
                    pods = (FeatureDatasetPoint) FeatureDatasetFactoryManager
                        .open(ucar.nc2.constants.FeatureType.ANY_POINT, path,
                              null, buf);
                }
                return pods;
            } catch (Exception exc) {
                throw new RuntimeException(exc);
            }
        }


    };


    /** _more_ */
    private Pool<String, TrajectoryObsDataset> trajectoryPool =
        new Pool<String, TrajectoryObsDataset>(10) {
        protected void removeValue(String key, TrajectoryObsDataset dataset) {
            try {
                super.removeValue(key, dataset);
                dataset.close();
            } catch (Exception exc) {}
        }

        /*
        protected  TrajectoryObsDataset getFromPool(List<TrajectoryObsDataset> list) {
            TrajectoryObsDataset dataset = super.getFromPool(list);
            dataset.sync();
            return dataset;
            }*/

        protected TrajectoryObsDataset createValue(String path) {
            try {
                getStorageManager().dirTouched(nj22Dir, null);

                //                System.err.println("track:" + path);
                TrajectoryObsDataset dataset =
                    (TrajectoryObsDataset) TypedDatasetFactory.open(
                        FeatureType.TRAJECTORY, path, null,
                        new StringBuilder());

                //                System.err.println("Create trajectoryPool: " + path);
                //                System.err.println("got it? " + (dataset!=null));
                return dataset;
            } catch (Exception exc) {
                //                System.err.println("oops");
                throw new RuntimeException(exc);
            }
        }


    };




    /**
     *     _more_
     *
     *     @param repository _more_
     *     @param element _more_
     *     @throws Exception On badness
     */
    public DataOutputHandler(Repository repository, Element element)
            throws Exception {
        super(repository, element);

        //TODO: what other global configuration should be done?
        nj22Dir =
            getRepository().getStorageManager().makeTemporaryDir("nj22");
        nj22Dir.setMaxFiles(500);

        // Apply settings for the NetcdfDataset
        ucar.nc2.dataset.NetcdfDataset.setHttpClient(
            getRepository().getHttpClient());


        // Apply settings for the opendap.dap
        opendap.dap.DConnect2.setHttpClient(getRepository().getHttpClient());



        //Set the temp file and the cache policy
        ucar.nc2.util.DiskCache.setRootDirectory(nj22Dir.getDir().toString());
        ucar.nc2.util.DiskCache.setCachePolicy(true);
        //        ucar.nc2.iosp.grib.GribServiceProvider.setIndexAlwaysInCache(true);
        ucar.nc2.iosp.grid.GridServiceProvider.setIndexAlwaysInCache(true);

        dataCacheDir = getRepository().getStorageManager().makeTemporaryDir(
            "visaddatacache");
        dataCacheDir.setMaxFiles(2000);

        NetcdfDataset.disableNetcdfFileCache();


        visad.SampledSet.setCacheSizeThreshold(10000);
        visad.util.ThreadManager.setGlobalMaxThreads(4);
        visad.data.DataCacheManager.getCacheManager().setCacheDir(
            dataCacheDir.getDir());
        visad.data.DataCacheManager.getCacheManager().setMemoryPercent(0.1);

        addType(OUTPUT_OPENDAP);
        addType(OUTPUT_CDL);
        addType(OUTPUT_WCS);
        addType(OUTPUT_TRAJECTORY_MAP);
        addType(OUTPUT_POINT_MAP);
        addType(OUTPUT_POINT_CSV);
        addType(OUTPUT_POINT_KML);
        addType(OUTPUT_GRIDSUBSET);
        addType(OUTPUT_GRIDSUBSET_FORM);
        addType(OUTPUT_GRIDASPOINT);
        addType(OUTPUT_GRIDASPOINT_FORM);
    }


    /**
     * _more_
     *
     * @param sb _more_
     */
    public void getSystemStats(StringBuffer sb) {
        super.getSystemStats(sb);
        StringBuffer poolStats = new StringBuffer("<pre>");
        ncFilePool.getStats(poolStats);
        ncDatasetPool.getStats(poolStats);
        poolStats.append("</pre>");
        sb.append(
            HtmlUtil.formEntryTop(
                msgLabel("Data Cache Size"),
                msgLabel("NC File Pool") + ncFilePool.getSize()
                + " have ncfile cache:"
                + (NetcdfDataset.getNetcdfFileCache() != null) + " "
                + " Count:  Create:" + ncCreateCounter.getCount()
                + " Remove:" + ncRemoveCounter.getCount() + "<br>" + " Get:"
                + ncGetCounter.getCount() + " Put:" + ncPutCounter.getCount()
                + "<br>" + " Ext Count:" + extCounter.getCount()
                + " Dap Count:" + opendapCounter.getCount() + poolStats
                + HtmlUtil.br() + msgLabel("Grid Pool") + gridPool.getSize()
                + HtmlUtil.br() + msgLabel("Point Pool")
                + pointPool.getSize() + HtmlUtil.br()
                + msgLabel("Trajectory Pool") + trajectoryPool.getSize()
                + HtmlUtil.br()));

    }


    /**
     * _more_
     */
    public void clearCache() {
        super.clearCache();
        ncFilePool.clear();
        ncDatasetPool.clear();
        gridPool.clear();
        pointPool.clear();
        trajectoryPool.clear();

        cdmEntries.clear();
        gridEntries.clear();
        pointEntries.clear();
        trajectoryEntries.clear();
    }



    /**
     * _more_
     *
     * @param request _more_
     * @param entry _more_
     * @param node _more_
     *
     * @throws Exception _more_
     */
    public void addToEntryNode(Request request, Entry entry, Element node)
            throws Exception {
        super.addToEntryNode(request, entry, node);
        if ( !getRepository().getAccessManager().canAccessFile(request,
                entry)) {
            return;
        }

        if ( !canLoadAsCdm(entry)) {
            return;
        }
        String  url         = getFullTdsUrl(entry);
        Element serviceNode = XmlUtil.create(TAG_SERVICE, node);
        XmlUtil.setAttributes(serviceNode, new String[] { ATTR_TYPE,
                SERVICE_OPENDAP, ATTR_URL, url });

    }


    /**
     * _more_
     *
     * @param request _more_
     * @param state _more_
     * @param links _more_
     *
     * @throws Exception _more_
     */
    public void getEntryLinks(Request request, State state, List<Link> links)
            throws Exception {


        Entry entry = state.entry;
        if (entry == null) {
            return;
        }

        if ( !getRepository().getAccessManager().canAccessFile(request,
                entry)) {
            return;
        }

        long t1 = System.currentTimeMillis();
        if ( !canLoadAsCdm(entry)) {
            long t2 = System.currentTimeMillis();
            if ((t2 - t1) > 1) {
                //                System.err.println("DataOutputHandler (cdm) getEntryLinks  "
                //                                   + entry.getName() + " time:" + (t2 - t1));
            }
            return;
        }

        if (canLoadAsGrid(entry)) {
            addOutputLink(request, entry, links, OUTPUT_GRIDSUBSET_FORM);
            addOutputLink(request, entry, links, OUTPUT_GRIDASPOINT_FORM);
        } else if (canLoadAsTrajectory(entry)) {
            addOutputLink(request, entry, links, OUTPUT_TRAJECTORY_MAP);
        } else if (canLoadAsPoint(entry)) {
            addOutputLink(request, entry, links, OUTPUT_POINT_MAP);
            links.add(makeLink(request, entry, OUTPUT_POINT_CSV,
                               "/" + IOUtil.stripExtension(entry.getName())
                               + ".csv"));

            links.add(makeLink(request, entry, OUTPUT_POINT_KML,
                               "/" + IOUtil.stripExtension(entry.getName())
                               + ".kml"));
        }

        Object oldOutput = request.getOutput();
        request.put(ARG_OUTPUT, OUTPUT_OPENDAP);
        String opendapUrl = getRepository().URL_ENTRY_SHOW + "/"
                            + request.getPathEmbeddedArgs() + "/"
                            + getStorageManager().getFileTail(entry)
                            + "/dodsC/entry.das";
        links.add(new Link(opendapUrl, getRepository().iconUrl(ICON_OPENDAP),
                           "OpenDAP", OUTPUT_OPENDAP));
        request.put(ARG_OUTPUT, oldOutput);


        Link cdlLink = makeLink(request, state.entry, OUTPUT_CDL);
        //        cdlLink.setLinkType(OutputType.TYPE_ACTION);
        links.add(cdlLink);
        long t2 = System.currentTimeMillis();
        if ((t2 - t1) > 1) {
            //            System.err.println("DataOutputHandler  getEntryLinks  "
            //                               + entry.getName() + " time:" + (t2 - t1));
        }
    }




    /**
     * _more_
     *
     * @param entry _more_
     *
     * @return _more_
     */
    public String getOpendapUrl(Entry entry) {
        return "/" + ARG_OUTPUT + ":"
               + Request.encodeEmbedded(OUTPUT_OPENDAP) + "/" + ARG_ENTRYID
               + ":" + Request.encodeEmbedded(entry.getId()) + "/"
               + getStorageManager().getFileTail(entry) + "/dodsC/entry.das";
    }


    /**
     * _more_
     *
     * @param entry _more_
     *
     * @return _more_
     */
    public String getFullTdsUrl(Entry entry) {
        return getRepository().URL_ENTRY_SHOW.getFullUrl() + "/" + ARG_OUTPUT
               + ":" + Request.encodeEmbedded(OUTPUT_OPENDAP) + "/"
               + ARG_ENTRYID + ":" + Request.encodeEmbedded(entry.getId())
               + "/" + getStorageManager().getFileTail(entry)
               + "/dodsC/entry.das";
    }


    /**
     * _more_
     *
     * @param entry _more_
     *
     * @return _more_
     */
    private boolean canLoadEntry(Entry entry) {
        String url = entry.getResource().getPath();
        if (url == null) {
            return false;
        }
        if (url.endsWith("~")) {
            return false;
        }
        if (url.endsWith("#")) {
            return false;
        }
        if (entry.isGroup()) {
            return false;
        }

        if (entry.getResource().isRemoteFile()) {
            return true;
        }

        if (entry.getResource().isFileType()) {
            return entry.getFile().exists();
        }
        if ( !entry.getResource().isUrl()) {
            return false;
        }
        if (url.indexOf("dods") >= 0) {
            return true;
        }
        return true;
    }


    /**
     * Can the given entry be served by the tds
     *
     *
     * @param entry The entry
     *
     * @return Can the given entry be served by the tds
     */
    public boolean canLoadAsCdm(Entry entry) {
        if (entry.getType().equals(OpendapLinkTypeHandler.TYPE_OPENDAPLINK)) {
            return true;
        }


        if ( !entry.getType().equals(
                OpendapLinkTypeHandler.TYPE_OPENDAPLINK)) {
            if ( !entry.isFile()) {
                return false;
            }
            if (cannotLoad(entry, TYPE_CDM)) {
                return false;
            }
        }

        String[] types = { TYPE_CDM, TYPE_GRID, TYPE_TRAJECTORY, TYPE_POINT };
        for (int i = 0; i < types.length; i++) {
            if (canLoad(entry, types[i])) {
                return true;
            }
        }


        if (entry.getResource().isRemoteFile()) {
            String path = entry.getResource().getPath();
            if (path.endsWith(".nc")) {
                return true;
            }
        }

        Boolean b = (Boolean) cdmEntries.get(entry.getId());
        if (b == null) {
            boolean ok = false;
            if (canLoadEntry(entry)) {
                try {
                    String path = entry.getFile().toString();
                    //Exclude zip files becase canOpen tries to unzip them (?)
                    if ( !(path.endsWith(".zip"))) {
                        ok = NetcdfDataset.canOpen(path);
                    }
                } catch (Exception ignoreThis) {
                    //                    System.err.println("   error:" + ignoreThis);
                    //                    System.err.println("error:" + ignoreThis);
                }
            }
            b = new Boolean(ok);
            cdmEntries.put(entry.getId(), b);
        }
        return b.booleanValue();
    }


    /**
     * _more_
     *
     * @param entry _more_
     *
     * @return _more_
     */
    public boolean canLoadAsPoint(Entry entry) {
        if (cannotLoad(entry, TYPE_POINT)) {
            return false;
        }
        if (canLoad(entry, TYPE_POINT)) {
            return true;
        }

        Boolean b = (Boolean) pointEntries.get(entry.getId());
        if (b == null) {
            boolean ok = false;
            if ( !canLoadEntry(entry)) {
                ok = false;
            } else {
                try {
                    ok = pointPool.containsOrCreate(getPath(entry));
                } catch (Exception ignore) {}
            }
            pointEntries.put(entry.getId(), b = new Boolean(ok));
        }
        return b.booleanValue();
    }


    /**
     * _more_
     *
     * @param entry _more_
     *
     * @return _more_
     */
    public boolean canLoadAsTrajectory(Entry entry) {
        if (cannotLoad(entry, TYPE_TRAJECTORY)) {
            return false;
        }
        if (canLoad(entry, TYPE_TRAJECTORY)) {
            return true;
        }

        if ( !canLoadAsCdm(entry)) {
            return false;
        }

        Boolean b = (Boolean) trajectoryEntries.get(entry.getId());
        if (b == null) {
            boolean ok = false;
            if (canLoadEntry(entry)) {
                try {
                    ok = trajectoryPool.containsOrCreate(getPath(entry));
                } catch (Exception ignoreThis) {}
            }
            trajectoryEntries.put(entry.getId(), b = new Boolean(ok));
        }
        return b.booleanValue();
    }


    /** _more_ */
    public static final String TYPE_CDM = "cdm";

    /** _more_ */
    public static final String TYPE_GRID = "grid";

    /** _more_ */
    public static final String TYPE_TRAJECTORY = "trajectory";

    /** _more_ */
    public static final String TYPE_POINT = "point";

    /** _more_ */
    private Hashtable prefixMap;

    /**
     * _more_
     *
     * @param entry _more_
     * @param type _more_
     *
     * @return _more_
     */
    private boolean cannotLoad(Entry entry, String type) {
        String[] types = { TYPE_CDM, TYPE_GRID, TYPE_TRAJECTORY, TYPE_POINT };
        //If this entry can be loaded by another type then we cannot
        //load it for this type
        /*        if(!type.equals(TYPE_CDM)) {
            for(int i=0;i<types.length;i++) {
                if(!types[i].equals(TYPE_CDM)) {
                    continue;
                }
                if(type.equals(types[i])) continue;
                if(canLoad(entry,types[i])) return true;
            }
            }*/

        return hasPrefixForType(entry, type, true);
    }

    /**
     * _more_
     *
     * @param entry _more_
     * @param type _more_
     *
     * @return _more_
     */
    private boolean canLoad(Entry entry, String type) {
        //        System.err.println ("can load:" + type+ " " +hasPrefixForType(entry, type, false));        
        return hasPrefixForType(entry, type, false);
    }

    /**
     * _more_
     *
     * @param entry _more_
     * @param type _more_
     * @param forNot _more_
     *
     * @return _more_
     */
    private boolean hasPrefixForType(Entry entry, String type,
                                     boolean forNot) {
        if (prefixMap == null) {
            Hashtable tmp = new Hashtable();
            String[] types = { TYPE_CDM, TYPE_GRID, TYPE_TRAJECTORY,
                               TYPE_POINT };
            for (int i = 0; i < types.length; i++) {
                List toks = StringUtil.split(
                                getRepository().getProperty(
                                    "ramadda.data." + types[i] + ".prefixes",
                                    ""), ",", true, true);
                for (String tok : (List<String>) toks) {
                    if ((tok.length() == 0) || tok.equals("!")) {
                        continue;
                    }
                    String key = types[i] + "." + tok;
                    tmp.put(key, "");
                }
            }
            prefixMap = tmp;
        }
        String url = entry.getResource().getPath();
        if (url == null) {
            return false;
        }

        String ext    = IOUtil.getFileExtension(url).toLowerCase();
        String key    = type + "." + ext;
        String notKey = type + ".!" + ext;
        if (forNot) {
            return prefixMap.get(notKey) != null;
        } else {
            return prefixMap.get(key) != null;
        }


    }

    /**
     * _more_
     *
     * @param entry _more_
     *
     * @return _more_
     */
    public boolean canLoadAsGrid(Entry entry) {
        if (cannotLoad(entry, TYPE_GRID)) {
            return false;
        }
        if (canLoad(entry, TYPE_GRID)) {
            return true;
        }
        if ( !canLoadAsCdm(entry)) {
            return false;
        }


        Boolean b = (Boolean) gridEntries.get(entry.getId());
        if (b == null) {
            boolean ok = false;
            if ( !canLoadEntry(entry)) {
                ok = false;
            } else {
                try {
                    ok = gridPool.containsOrCreate(getPath(entry));
                } catch (Exception ignoreThis) {}
            }
            b = new Boolean(ok);
            gridEntries.put(entry.getId(), b);
        }
        return b.booleanValue();
    }


    /**
     * _more_
     *
     * @param request _more_
     * @param entry _more_
     *
     * @return _more_
     *
     * @throws Exception _more_
     */
    public Result outputCdl(final Request request, Entry entry)
            throws Exception {
        StringBuffer sb = new StringBuffer();
        if (request.get(ARG_METADATA_ADD, false)) {
            if (getRepository().getAccessManager().canDoAction(request,
                    entry, Permission.ACTION_EDIT)) {
                sb.append(HtmlUtil.p());
                List<Entry> entries = (List<Entry>) Misc.newList(entry);
                getEntryManager().addInitialMetadata(request, entries, false,
                        request.get(ARG_SHORT, false));
                getEntryManager().insertEntries(entries, false);
                sb.append(getRepository().showDialogNote("Properties added"));
                sb.append(
                    getRepository().getHtmlOutputHandler().getInformationTabs(
                        request, entry, false, false));

            } else {
                sb.append("You cannot add properties");
            }
            return makeLinksResult(request, "CDL", sb, new State(entry));
        }




        if (getRepository().getAccessManager().canDoAction(request, entry,
                Permission.ACTION_EDIT)) {
            request.put(ARG_METADATA_ADD, HtmlUtil.VALUE_TRUE);
            sb.append(
                HtmlUtil.href(
                    request.getUrl() + "&"
                    + HtmlUtil.arg(ARG_SHORT, HtmlUtil.VALUE_TRUE), msg(
                        "Add time/spatial properties")));
            sb.append(HtmlUtil.span("&nbsp;|&nbsp;",
                                    HtmlUtil.cssClass("separator")));

            sb.append(HtmlUtil.href(request.getUrl(),
                                    msg("Add full properties")));
        }
        String        path    = getPath(entry);
        NetcdfDataset dataset = ncDatasetPool.get(path);
        if (dataset == null) {
            sb.append("Could not open dataset");
        } else {
            ByteArrayOutputStream bos = new ByteArrayOutputStream();
            ucar.nc2.NCdump.print(dataset, "", bos, null);
            sb.append("<pre>" + bos.toString() + "</pre>");
            ncDatasetPool.put(path, dataset);
        }
        return makeLinksResult(request, "CDL", sb, new State(entry));
    }

    /**
     * _more_
     *
     * @param entry _more_
     * @param path _more_
     *
     * @return _more_
     */
    public NetcdfDataset getNetcdfDataset(Entry entry, String path) {
        if ( !canLoadAsCdm(entry)) {
            return null;
        }
        extCounter.incr();
        return ncDatasetPool.get(path);
    }

    /**
     * _more_
     *
     * @param path _more_
     * @param ncd _more_
     */
    public void returnNetcdfDataset(String path, NetcdfDataset ncd) {
        extCounter.decr();
        ncDatasetPool.put(path, ncd);
    }


    /**
     * _more_
     *
     * @param entry _more_
     * @param path _more_
     *
     * @return _more_
     */
    public GridDataset getGridDataset(Entry entry, String path) {
        if ( !canLoadAsGrid(entry)) {
            return null;
        }
        return gridPool.get(path);
    }

    /**
     * _more_
     *
     * @param path _more_
     * @param ncd _more_
     */
    public void returnGridDataset(String path, GridDataset ncd) {
        gridPool.put(path, ncd);
    }



    /**
     * _more_
     *
     * @param entry _more_
     * @param path _more_
     *
     * @return _more_
     */
    public FeatureDatasetPoint getPointDataset(Entry entry, String path) {
        if ( !canLoadAsPoint(entry)) {
            return null;
        }
        return pointPool.get(path);
    }

    /**
     * _more_
     *
     * @param path _more_
     * @param ncd _more_
     */
    public void returnPointDataset(String path, FeatureDatasetPoint ncd) {
        pointPool.put(path, ncd);
    }

    /**
     * _more_
     *
     * @param request _more_
     * @param entry _more_
     *
     * @return _more_
     */
    public Result outputWcs(Request request, Entry entry) {
        return new Result("", new StringBuffer("TBD"));
    }


    public Result outputGridAsPointProcess(Request request, Entry entry, GridDataset gds, StringBuffer sb)
        throws Exception {

        boolean canAdd =
            getRepository().getAccessManager().canDoAction(request,
                                                           entry.getParentEntry(), Permission.ACTION_NEW);

        List      varNames = new ArrayList<String>();
        Hashtable args     = request.getArgs();
        for (Enumeration keys = args.keys(); keys.hasMoreElements(); ) {
            String arg = (String) keys.nextElement();
            if (arg.startsWith(VAR_PREFIX) && request.get(arg, false)) {
                varNames.add(arg.substring(VAR_PREFIX.length()));
            }
        }
        //            System.err.println(varNames);
        LatLonPointImpl llp = null;
        if (request.get(ARG_SUBSETLOCATION, true)) {
            llp = new LatLonPointImpl(request.get(ARG_LOCATION_LATITUDE,
                                                  40.0), request.get(ARG_LOCATION_LONGITUDE, -105.0));
        }
        int    timeStride = 1;
        Date[] dates = new Date[] { request.get(ARG_SUBSETTIME, false)
                                    ? request.getDate(ARG_FROMDATE, null)
                                    : null, request.get(ARG_SUBSETTIME,
                                                        false)
                                    ? request.getDate(ARG_TODATE, null)
                                    : null };
        if ((dates[0] != null) && (dates[1] != null)
            && (dates[0].getTime() > dates[1].getTime())) {
            sb.append(
                      getRepository().showDialogWarning(
                                                        "From date is after to date"));
        } else if (varNames.size() == 0) {
            sb.append(
                      getRepository().showDialogWarning(
                                                        "No variables selected"));
        } else {

            //                System.err.println ("varNames:" + varNames);

            GridPointWriter writer =
                new GridPointWriter(gds,
                                    new DiskCache2(getRepository()
                                                   .getStorageManager().getTmpDir()
                                                   .toString(), false, 0, 0));
            QueryParams qp = new QueryParams();
            qp.acceptType     = QueryParams.NETCDF;

            qp.vars           = varNames;

            qp.hasLatlonPoint = true;
            qp.lat            = llp.getLatitude();
            qp.lon            = llp.getLongitude();

            if (dates[0] != null) {
                qp.time_start = new DateType(false, dates[0]);
                if (dates[1] != null) {
                    qp.time_end     = new DateType(false, dates[1]);
                    qp.hasDateRange = true;
                } else {
                    qp.hasTimePoint = true;
                    qp.hasDateRange = false;
                    qp.time         = qp.time_start;
                }
            }

            PrintWriter pw = new PrintWriter(System.out);

            File        f  = writer.write(qp, pw);


            if (request.get(ARG_ADDTOREPOSITORY, false)) {
                if ( !canAdd) {
                    sb.append("Cannot add to repository");
                } else {
                    Entry newEntry = (Entry) entry.clone();
                    File newFile =
                        getRepository().getStorageManager().moveToStorage(
                                                                          request, f);
                    newEntry.setResource(new Resource(newFile,
                                                      Resource.TYPE_STOREDFILE));
                    newEntry.setId(getRepository().getGUID());
                    newEntry.setName("subset_" + newEntry.getName());
                    newEntry.clearMetadata();
                    newEntry.setUser(request.getUser());
                    newEntry.addAssociation(
                                            new Association(
                                                            getRepository().getGUID(), "", "subset from",
                                                            entry.getId(), newEntry.getId()));
                    if (request.get(ARG_METADATA_ADD, false)) {
                        newEntry.clearArea();
                        List<Entry> entries =
                            (List<Entry>) Misc.newList(newEntry);
                        getEntryManager().addInitialMetadata(request,
                                                             entries, false,
                                                             request.get(ARG_SHORT, false));
                    }
                    getEntryManager().insertEntries(
                                                    Misc.newList(newEntry), true);
                    return new Result(
                                      request.entryUrl(
                                                       getRepository().URL_ENTRY_FORM, newEntry));
                }
            } else {
                return new Result(
                                  entry.getName() + ".nc",
                                  getStorageManager().getFileInputStream(f),
                                  "application/x-netcdf");
            }
        }

        return null;
    }


    public Result outputGridAsPointForm(Request request, Entry entry, GridDataset dataset, StringBuffer sb)
        throws Exception {

        boolean canAdd =
            getRepository().getAccessManager().canDoAction(request,
                                                           entry.getParentEntry(), Permission.ACTION_NEW);

        String formUrl  = request.url(getRepository().URL_ENTRY_SHOW);
        String fileName = IOUtil.stripExtension(entry.getName())
                          + "_point.nc";

        sb.append(HtmlUtil.form(formUrl + "/" + fileName));
        sb.append(HtmlUtil.br());

        String submitExtra = "";
        if (canAdd) {
            submitExtra = HtmlUtil.space(1)
                          + HtmlUtil.checkbox(
                              ARG_ADDTOREPOSITORY, HtmlUtil.VALUE_TRUE,
                              request.get(ARG_ADDTOREPOSITORY, false)) + msg(
                                  "Add to Repository") + HtmlUtil.checkbox(
                                  ARG_METADATA_ADD, HtmlUtil.VALUE_TRUE,
                                  request.get(ARG_METADATA_ADD, false)) + msg(
                                      "Add properties");

        }


        sb.append(HtmlUtil.submit("Get Point", ARG_SUBMIT));
        sb.append(submitExtra);
        sb.append(HtmlUtil.br());
        sb.append(HtmlUtil.hidden(ARG_OUTPUT, OUTPUT_GRIDASPOINT));
        sb.append(HtmlUtil.hidden(ARG_ENTRYID, entry.getId()));
        sb.append(HtmlUtil.formTable());

        /*
        sb.append(HtmlUtil.formEntry(msgLabel("Horizontal Stride"),
                                     HtmlUtil.input(ARG_HSTRIDE,
                                         request.getString(ARG_HSTRIDE, "1"),
                                         HtmlUtil.SIZE_3)));
                                         */


        Date[]             dateRange = null;
        List<Date>         dates     = null;


        List<GridDatatype> grids     = dataset.getGrids();

        StringBuffer       varSB     = new StringBuffer();
        HashSet<Date>      dateHash  = new HashSet<Date>();
        List<CoordinateAxis1DTime> timeAxes =
            new ArrayList<CoordinateAxis1DTime>();

        for (GridDatatype grid : grids) {
            GridCoordSystem      gcs      = grid.getCoordinateSystem();
            CoordinateAxis1DTime timeAxis = gcs.getTimeAxis1D();
            if ((timeAxis != null) && !timeAxes.contains(timeAxis)) {
                timeAxes.add(timeAxis);

                Date[] timeDates = timeAxis.getTimeDates();
                for (Date timeDate : timeDates) {
                    dateHash.add(timeDate);
                }
            }
        }
        dates = Arrays.asList(dateHash.toArray(new Date[dateHash.size()]));
        Collections.sort(dates);
        /*
        for (VariableSimpleIF var : dataset.getDataVariables()) {
            //            System.err.println("var:" + var.getName() + " type:"
            //                               + var.getClass().getName());
            if (var instanceof CoordinateAxis) {
                CoordinateAxis ca       = (CoordinateAxis) var;
                AxisType       axisType = ca.getAxisType();
                if (axisType == null) {
                    continue;
                }
                if (axisType.equals(AxisType.Time)) {
                    dates = (List<Date>) Misc.sort(
                        ThreddsMetadataHandler.getDates(var, ca));
                }
                continue;
            }
        }
        */
        int varCnt = 0;

        for (GridDatatype grid : sortGrids(dataset)) {
            String cbxId = "varcbx_" + (varCnt++);
            String call = HtmlUtil.attr(
                              HtmlUtil.ATTR_ONCLICK,
                              HtmlUtil.call(
                                  "checkboxClicked",
                                  HtmlUtil.comma(
                                      "event", HtmlUtil.squote(ARG_VARIABLE),
                                      HtmlUtil.squote(cbxId))));
            VariableEnhanced var = grid.getVariable();
            varSB.append(
                HtmlUtil.row(
                    HtmlUtil.cols(
                        HtmlUtil.checkbox(
                            ARG_VARIABLE + "." + var.getShortName(),
                            HtmlUtil.VALUE_TRUE, false,
                            HtmlUtil.id(cbxId) + call) + HtmlUtil.space(1)
                                + var.getName() + HtmlUtil.space(1)
                                + ((var.getUnitsString() != null)
                                   ? "(" + var.getUnitsString() + ")"
                                   : ""), "<i>" + var.getDescription()
                                          + "</i>")));

        }

        LatLonRect llr = dataset.getBoundingBox();
        String     lat = "";
        String     lon = "";
        if (llr != null) {
            lat = Misc.format(llr.getLatMin() + llr.getHeight() / 2);
            lon = Misc.format(llr.getCenterLon());
        }
        //String llb =
        //  getRepository().getMapManager().makeMapSelector(ARG_LOCATION, true, new String[]{ "", ""});

        String llb = " Latitude: "
                     + HtmlUtil.input(
                         ARG_LOCATION_LATITUDE, lat,
                         HtmlUtil.SIZE_5 + " "
                         + HtmlUtil.id(
                             ARG_LOCATION_LATITUDE)) + " Longitude: "
                                 + HtmlUtil.input(
                                     ARG_LOCATION_LONGITUDE, lon,
                                     HtmlUtil.SIZE_5 + " "
                                     + HtmlUtil.id(ARG_LOCATION_LONGITUDE));

        llb = getRepository().getMapManager().makeMapSelector(ARG_LOCATION, true, "", "", new String[]{"",""}, null);
        sb.append(
            HtmlUtil.formEntryTop(
                msgLabel("Choose Point"),
                llb));


        if ((dates != null) && (dates.size() > 0)) {
            List formattedDates = new ArrayList();
            for (Date date : dates) {
                formattedDates.add(getRepository().formatDate(request, date));
            }
            String fromDate = request.getUnsafeString(ARG_FROMDATE,
                                  getRepository().formatDate(request,
                                      dates.get(0)));
            String toDate = request.getUnsafeString(ARG_TODATE,
                                getRepository().formatDate(request,
                                    dates.get(dates.size() - 1)));
            sb.append(
                HtmlUtil.formEntry(
                    msgLabel("Time Range"),
                    HtmlUtil.checkbox(
                        ARG_SUBSETTIME, HtmlUtil.VALUE_TRUE,
                        request.get(ARG_SUBSETTIME, false)) + HtmlUtil.space(
                            1) + HtmlUtil.select(
                            ARG_FROMDATE, formattedDates,
                            fromDate) + HtmlUtil.img(iconUrl(ICON_ARROW))
                                      + HtmlUtil.select(
                                          ARG_TODATE, formattedDates,
                                          toDate)));
        }

        sb.append("</table>");
        sb.append("<hr>");
        sb.append(msgLabel("Select Variables"));
        sb.append(HtmlUtil.insetDiv(HtmlUtil.table(varSB.toString(),
                                                   HtmlUtil.attrs(HtmlUtil.ATTR_CELLPADDING, "5", HtmlUtil.ATTR_CELLSPACING, "0")),0,30,0,0));

        sb.append(HtmlUtil.submit("Get Point"));
        //sb.append(submitExtra);
        sb.append(HtmlUtil.formClose());

        return makeLinksResult(request, msg("Grid As Point"), sb,
                               new State(entry));
    }


    /**
     * _more_
     *
     * @param request _more_
     * @param entry _more_
     *
     * @return _more_
     *
     * @throws Exception _more_
     */
    public Result outputGridAsPoint(Request request, Entry entry)
        throws Exception {
        StringBuffer sb     = new StringBuffer();
        String       path   = getPath(entry);
        GridDataset gds = gridPool.get(path);
        OutputType   output = request.getOutput();
        try {
            if (output.equals(OUTPUT_GRIDASPOINT)) {
                Result result = outputGridAsPointProcess(request, entry, gds, sb);
                if(result!=null) return result;
            }
            return  outputGridAsPointForm(request, entry, gds, sb);
        } finally {
            gridPool.put(path, gds);
        }
    }


    /**
     * _more_
     *
     * @param request _more_
     * @param entry _more_
     *
     * @return _more_
     *
     * @throws Exception _more_
     */
    public Result outputGridSubset(Request request, Entry entry)
            throws Exception {

        boolean canAdd =
            getRepository().getAccessManager().canDoAction(request,
                entry.getParentEntry(), Permission.ACTION_NEW);




        String       path   = getPath(entry);
        StringBuffer sb     = new StringBuffer();

        OutputType   output = request.getOutput();
        if (output.equals(OUTPUT_GRIDSUBSET)) {
            List      varNames = new ArrayList();
            Hashtable args     = request.getArgs();
            for (Enumeration keys = args.keys(); keys.hasMoreElements(); ) {
                String arg = (String) keys.nextElement();
                if (arg.startsWith(VAR_PREFIX) && request.get(arg, false)) {
                    varNames.add(arg.substring(VAR_PREFIX.length()));
                }
            }
            //            System.err.println(varNames);
            LatLonRect llr = null;
            if (request.get(ARG_SUBSETAREA, false)) {
                llr = new LatLonRect(
                    new LatLonPointImpl(
                        request.get(ARG_AREA_NORTH, 90.0), request.get(
                            ARG_AREA_WEST, -180.0)), new LatLonPointImpl(
                                request.get(ARG_AREA_SOUTH, 0.0), request.get(
                                    ARG_AREA_EAST, 180.0)));
                //                System.err.println("llr:" + llr);
            }
            int     hStride       = request.get(ARG_HSTRIDE, 1);
            int     zStride       = 1;
            boolean includeLatLon = request.get(ARG_ADDLATLON, false);
            int     timeStride    = 1;
            Date[]  dates = new Date[] { request.get(ARG_SUBSETTIME, false)
                                         ? request.getDate(ARG_FROMDATE, null)
                                         : null, request.get(ARG_SUBSETTIME,
                                             false)
                    ? request.getDate(ARG_TODATE, null)
                    : null };
            if ((dates[0] != null) && (dates[1] != null)
                    && (dates[0].getTime() > dates[1].getTime())) {
                sb.append(
                    getRepository().showDialogWarning(
                        "From date is after to date"));
            } else if (varNames.size() == 0) {
                sb.append(
                    getRepository().showDialogWarning(
                        "No variables selected"));
            } else {
                NetcdfCFWriter writer = new NetcdfCFWriter();
                File f =
                    getRepository().getStorageManager().getTmpFile(request,
                        "subset.nc");

                //                System.err.println ("varNames:" + varNames);

                GridDataset gds = gridPool.get(path);
                writer.makeFile(f.toString(), gds, varNames, llr,
                                ((dates[0] == null)
                                 ? null
                                 : new ucar.nc2.units.DateRange(dates[0],
                                 dates[1])), includeLatLon, hStride, zStride,
                                             timeStride);
                gridPool.put(path, gds);

                if (request.get(ARG_ADDTOREPOSITORY, false)) {
                    if ( !canAdd) {
                        sb.append("Cannot add to repository");
                    } else {
                        Entry newEntry = (Entry) entry.clone();
                        File newFile =
                            getRepository().getStorageManager().moveToStorage(
                                request, f);
                        newEntry.setResource(new Resource(newFile,
                                Resource.TYPE_STOREDFILE));
                        newEntry.setId(getRepository().getGUID());
                        newEntry.setName("subset_" + newEntry.getName());
                        newEntry.clearMetadata();
                        newEntry.setUser(request.getUser());
                        newEntry.addAssociation(
                            new Association(
                                getRepository().getGUID(), "", "subset from",
                                entry.getId(), newEntry.getId()));
                        if (request.get(ARG_METADATA_ADD, false)) {
                            newEntry.clearArea();
                            List<Entry> entries =
                                (List<Entry>) Misc.newList(newEntry);
                            getEntryManager().addInitialMetadata(request,
                                    entries, false,
                                    request.get(ARG_SHORT, false));
                        }
                        getEntryManager().insertEntries(
                            Misc.newList(newEntry), true);
                        return new Result(
                            request.entryUrl(
                                getRepository().URL_ENTRY_FORM, newEntry));
                    }
                } else {
                    return new Result(
                        entry.getName() + ".nc",
                        getStorageManager().getFileInputStream(f),
                        "application/x-netcdf");
                }
            }
        }

        String formUrl = request.url(getRepository().URL_ENTRY_SHOW);
        String fileName = IOUtil.stripExtension(entry.getName())
                          + "_subset.nc";

        sb.append(HtmlUtil.form(formUrl + "/" + fileName));
        sb.append(HtmlUtil.br());

        String submitExtra = "";
        if (canAdd) {
            submitExtra = HtmlUtil.space(1)
                          + HtmlUtil.checkbox(
                              ARG_ADDTOREPOSITORY, HtmlUtil.VALUE_TRUE,
                              request.get(ARG_ADDTOREPOSITORY, false)) + msg(
                                                                             "Add to Repository") + HtmlUtil.checkbox(
                                  ARG_METADATA_ADD, HtmlUtil.VALUE_TRUE,
                                  request.get(ARG_METADATA_ADD, false)) + msg(
                                      "Add properties");
        }



        sb.append(HtmlUtil.submit("Subset Grid", ARG_SUBMIT));
        sb.append(submitExtra);
        sb.append(HtmlUtil.br());
        sb.append(HtmlUtil.hidden(ARG_OUTPUT, OUTPUT_GRIDSUBSET));
        sb.append(HtmlUtil.hidden(ARG_ENTRYID, entry.getId()));
        sb.append(HtmlUtil.formTable());

        sb.append(HtmlUtil.formEntry(msgLabel("Horizontal Stride"),
                                     HtmlUtil.input(ARG_HSTRIDE,
                                         request.getString(ARG_HSTRIDE, "1"),
                                         HtmlUtil.SIZE_3)));


        Date[]       dateRange = null;
        List<Date>   dates     = null;

        GridDataset  dataset   = gridPool.get(path);
        StringBuffer varSB     = new StringBuffer();
        for (VariableSimpleIF var : dataset.getDataVariables()) {
            //            System.err.println("var:" + var.getName() + " type:"
            //                               + var.getClass().getName());
            if (var instanceof CoordinateAxis) {
                CoordinateAxis ca       = (CoordinateAxis) var;
                AxisType       axisType = ca.getAxisType();
                if (axisType == null) {
                    continue;
                }
                if (axisType.equals(AxisType.Time)) {
                    dates = (List<Date>) Misc.sort(
                        ThreddsMetadataHandler.getDates(var, ca));
                }
                continue;
            }
        }
        int varCnt = 0;

        for (GridDatatype grid : sortGrids(dataset)) {
            String cbxId = "varcbx_" + (varCnt++);
            String call = HtmlUtil.attr(
                              HtmlUtil.ATTR_ONCLICK,
                              HtmlUtil.call(
                                  "checkboxClicked",
                                  HtmlUtil.comma(
                                      "event", HtmlUtil.squote(ARG_VARIABLE),
                                      HtmlUtil.squote(cbxId))));


            VariableEnhanced var = grid.getVariable();
            varSB.append(
                HtmlUtil.row(
                    HtmlUtil.cols(
                        HtmlUtil.checkbox(
                            ARG_VARIABLE + "." + var.getShortName(),
                            HtmlUtil.VALUE_TRUE, false,
                            HtmlUtil.id(cbxId) + call) + HtmlUtil.space(1)
                                + var.getName() + HtmlUtil.space(1)
                                + ((var.getUnitsString() != null)
                                   ? "(" + var.getUnitsString() + ")"
                                   : ""), "<i>" + var.getDescription()
                                          + "</i>")));

        }

        if ((dates != null) && (dates.size() > 0)) {
            List formattedDates = new ArrayList();
            for (Date date : dates) {
                formattedDates.add(getRepository().formatDate(request, date));
            }
            String fromDate = request.getUnsafeString(ARG_FROMDATE,
                                  getRepository().formatDate(request,
                                      dates.get(0)));
            String toDate = request.getUnsafeString(ARG_TODATE,
                                getRepository().formatDate(request,
                                    dates.get(dates.size() - 1)));
            sb.append(
                HtmlUtil.formEntry(
                    msgLabel("Time Range"),
                    HtmlUtil.checkbox(
                        ARG_SUBSETTIME, HtmlUtil.VALUE_TRUE,
                        request.get(ARG_SUBSETTIME, true)) + HtmlUtil.space(
                            1) + HtmlUtil.select(
                            ARG_FROMDATE, formattedDates,
                            fromDate) + HtmlUtil.img(iconUrl(ICON_ARROW))
                                      + HtmlUtil.select(
                                          ARG_TODATE, formattedDates,
                                          toDate)));
        }


        /*
          for (CoordinateSystem coordSys : (List<CoordinateSystem>)dataset
          .getCoordinateSystems()) {
          ProjectionImpl proj = coordSys.getProjection();
          if (proj == null) {
          continue;
          }
          break;
          }
        */

        LatLonRect llr = dataset.getBoundingBox();
        if (llr != null) {
            String llb =
                getRepository().getMapManager().makeMapSelector(ARG_AREA,
                    true, new String[] { "" + llr.getLatMin(),
                                         "" + llr.getLatMax(),
                                         "" + llr.getLonMax(),
                                         "" + llr.getLonMin() });

            sb.append(
                HtmlUtil.formEntryTop(
                    msgLabel("Subset Spatially"),
                    "<table cellpadding=0 cellspacing=0><tr valign=top><td>"
                    + HtmlUtil.checkbox(
                        ARG_SUBSETAREA, HtmlUtil.VALUE_TRUE,
                        request.get(ARG_SUBSETAREA, false)) + "</td><td>"
                            + llb + "</table>"));
        }


        sb.append(HtmlUtil.formEntry(msgLabel("Add Lat/Lon Variables"),
                                     HtmlUtil.checkbox(ARG_ADDLATLON,
                                         HtmlUtil.VALUE_TRUE,
                                         request.get(ARG_ADDLATLON, true))));


        sb.append("</table>");
        sb.append("<hr>");
        sb.append("Select Variables:<ul>");
        sb.append("<table>");
        sb.append(varSB);
        sb.append("</table>");
        sb.append("</ul>");
        sb.append(HtmlUtil.br());
        sb.append(HtmlUtil.submit("Subset Grid"));
        sb.append(HtmlUtil.formClose());
        gridPool.put(path, dataset);
        return makeLinksResult(request, msg("Grid Subset"), sb,
                               new State(entry));
    }


    /**
     * _more_
     *
     * @param dataset _more_
     *
     * @return _more_
     */
    public List<GridDatatype> sortGrids(GridDataset dataset) {
        List tuples = new ArrayList();
        for (GridDatatype grid : dataset.getGrids()) {
            VariableEnhanced var = grid.getVariable();
            tuples.add(new Object[] { var.getShortName().toLowerCase(),
                                      grid });
        }
        tuples = Misc.sortTuples(tuples, true);
        List<GridDatatype> result = new ArrayList<GridDatatype>();
        for (Object[] tuple : (List<Object[]>) tuples) {
            result.add((GridDatatype) tuple[1]);
        }
        return result;
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
            collection = npfc.flatten(null, null);
        } else {
            throw new IllegalArgumentException(
                "Can't handle collection of type " + fc.getClass().getName());
        }
        return collection.getPointFeatureIterator(16384);
    }




    /**
     * _more_
     *
     * @param request _more_
     * @param entry _more_
     *
     * @return _more_
     *
     * @throws Exception _more_
     */
    public Result outputPointMap(Request request, Entry entry)
            throws Exception {

        String              mapVarName = "map" + HtmlUtil.blockCnt++;
        String              path       = getPath(entry);
        FeatureDatasetPoint pod        = pointPool.get(path);

        StringBuffer        sb         = new StringBuffer();
        List                vars       = pod.getDataVariables();
        int                 skip       = request.get(ARG_SKIP, 0);
        int                 max        = request.get(ARG_MAX, 200);

        StringBuffer        js         = new StringBuffer();
        js.append("var marker;\n");
        int                  cnt            = 0;
        int                  total          = 0;
        String               icon           = iconUrl("/icons/pointdata.gif");

        PointFeatureIterator dataIterator   = getPointIterator(pod);
        List                 columnDataList = new ArrayList();
        while (dataIterator.hasNext()) {
            PointFeature po = (PointFeature) dataIterator.next();
            //                ucar.unidata.geoloc.EarthLocation el = po.getLocation();
            ucar.unidata.geoloc.EarthLocation el = po.getLocation();
            if (el == null) {
                continue;
            }
            double lat = el.getLatitude();
            double lon = el.getLongitude();
            if ((lat != lat) || (lon != lon)) {
                continue;
            }
            if ((lat < -90) || (lat > 90) || (lon < -180) || (lon > 180)) {
                continue;
            }
            total++;
            if (total <= skip) {
                continue;
            }
            if (total > (max + skip)) {
                continue;
            }
            cnt++;
            List          columnData = new ArrayList();
            StructureData structure  = po.getData();
            js.append("marker = new Marker("
                      + llp(el.getLatitude(), el.getLongitude()) + ");\n");

            js.append("marker.setIcon(" + HtmlUtil.quote(icon) + ");\n");
            StringBuffer info = new StringBuffer("");
            info.append("<b>Date:</b> " + po.getNominalTimeAsDate() + "<br>");
            for (VariableSimpleIF var : (List<VariableSimpleIF>) vars) {
                //{name:\"Ashley\",breed:\"German Shepherd\",age:12}
                StructureMembers.Member member =
                    structure.findMember(var.getShortName());
                if ((var.getDataType() == DataType.STRING)
                        || (var.getDataType() == DataType.CHAR)) {
                    String value = structure.getScalarString(member);
                    columnData.add(var.getShortName() + ":"
                                   + HtmlUtil.quote(value));
                    info.append("<b>" + var.getName() + ": </b>" + value
                                + "</br>");

                } else {
                    float value = structure.convertScalarFloat(member);
                    info.append("<b>" + var.getName() + ": </b>" + value
                                + "</br>");

                    columnData.add(var.getShortName() + ":" + value);
                }
            }
            columnDataList.add("{" + StringUtil.join(",", columnData)
                               + "}\n");
            js.append("marker.setInfoBubble(\"" + info.toString() + "\");\n");
            js.append("initMarker(marker," + HtmlUtil.quote("" + cnt) + ","
                      + mapVarName + ");\n");
        }

        js.append(mapVarName + ".autoCenterAndZoom();\n");
        //        js.append(mapVarName+".resizeTo(" + width + "," + height + ");\n");

        StringBuffer yui         = new StringBuffer();
        List         columnDefs  = new ArrayList();
        List         columnNames = new ArrayList();
        for (VariableSimpleIF var : (List<VariableSimpleIF>) vars) {
            columnNames.add(HtmlUtil.quote(var.getShortName()));
            String label = var.getDescription();
            //            if(label.trim().length()==0)
            label = var.getName();
            columnDefs.add("{key:" + HtmlUtil.quote(var.getShortName()) + ","
                           + "sortable:true," + "label:"
                           + HtmlUtil.quote(label) + "}");
        }


        if (total > max) {
            sb.append((skip + 1) + "-" + (skip + cnt) + " of " + total + " ");
        } else {
            sb.append((skip + 1) + "-" + (skip + cnt));
        }
        if (total > max) {
            boolean didone = false;
            if (skip > 0) {
                sb.append(HtmlUtil.space(2));
                sb.append(
                    HtmlUtil.href(
                        HtmlUtil.url(request.getRequestPath(), new String[] {
                    ARG_OUTPUT, request.getOutput().toString(), ARG_ENTRYID,
                    entry.getId(), ARG_SKIP, "" + (skip - max), ARG_MAX,
                    "" + max
                }), msg("Previous")));
                didone = true;
            }
            if (total > (skip + cnt)) {
                sb.append(HtmlUtil.space(2));
                sb.append(
                    HtmlUtil.href(
                        HtmlUtil.url(request.getRequestPath(), new String[] {
                    ARG_OUTPUT, request.getOutput().toString(), ARG_ENTRYID,
                    entry.getId(), ARG_SKIP, "" + (skip + max), ARG_MAX,
                    "" + max
                }), msg("Next")));
                didone = true;
            }
            //Just come up with some max number
            if (didone && (total < 2000)) {
                sb.append(HtmlUtil.space(2));
                sb.append(
                    HtmlUtil.href(
                        HtmlUtil.url(request.getRequestPath(), new String[] {
                    ARG_OUTPUT, request.getOutput().toString(), ARG_ENTRYID,
                    entry.getId(), ARG_SKIP, "" + 0, ARG_MAX, "" + total
                }), msg("All")));

            }
        }
        //        sb.append("<table width=\"100%\"><tr valign=top><td>\n");
        getRepository().getMapManager().initMap(request, mapVarName, sb,
                request.get(ARG_WIDTH, 800), request.get(ARG_HEIGHT, 500),
                true);
        /*        sb.append("</td><td>");
                  sb.append(HtmlUtil.div("",HtmlUtil.id("datatable")+HtmlUtil.cssClass(" yui-skin-sam")));
                  sb.append("</td></tr></table>");
                  sb.append("\n<link rel=\"stylesheet\" type=\"text/css\" href=\"http://yui.yahooapis.com/2.5.2/build/fonts/fonts-min.css\" />\n<link rel=\"stylesheet\" type=\"text/css\" href=\"http://yui.yahooapis.com/2.5.2/build/datatable/assets/skins/sam/datatable.css\" />\n<script type=\"text/javascript\" src=\"http://yui.yahooapis.com/2.5.2/build/yahoo-dom-event/yahoo-dom-event.js\"></script>\n<script type=\"text/javascript\" src=\"http://yui.yahooapis.com/2.5.2/build/dragdrop/dragdrop-min.js\"></script>\n<script type=\"text/javascript\" src=\"http://yui.yahooapis.com/2.5.2/build/element/element-beta-min.js\"></script>\n<script type=\"text/javascript\" src=\"http://yui.yahooapis.com/2.5.2/build/datasource/datasource-beta-min.js\"></script>\n<script type=\"text/javascript\" src=\"http://yui.yahooapis.com/2.5.2/build/datatable/datatable-beta-min.js\"></script>\n");

                  sb.append(HtmlUtil.script(yui.toString()));
        */

        sb.append(HtmlUtil.script(js.toString()));
        pointPool.put(path, pod);

        return new Result(msg("Point Data Map"), sb);
    }


    /** Fixed var name for lat */
    public static final String VAR_LATITUDE = "Latitude";

    /** Fixed var name for lon */
    public static final String VAR_LONGITUDE = "Longitude";

    /** Fixed var name for alt */
    public static final String VAR_ALTITUDE = "Altitude";

    /** Fixed var name for time */
    public static final String VAR_TIME = "Time";



    /**
     * Get the 1D values for an array as floats.
     *
     * @param arr   Array of values
     * @return  float representation
     */
    public static float[] toFloatArray(Array arr) {
        Object dst       = arr.get1DJavaArray(float.class);
        Class  fromClass = dst.getClass().getComponentType();
        if (fromClass.equals(float.class)) {
            //It should always be a float
            return (float[]) dst;
        } else {
            float[] values = new float[(int) arr.getSize()];
            if (fromClass.equals(byte.class)) {
                byte[] fromArray = (byte[]) dst;
                for (int i = 0; i < fromArray.length; ++i) {
                    values[i] = fromArray[i];
                }
            } else if (fromClass.equals(short.class)) {
                short[] fromArray = (short[]) dst;
                for (int i = 0; i < fromArray.length; ++i) {
                    values[i] = fromArray[i];
                }
            } else if (fromClass.equals(int.class)) {
                int[] fromArray = (int[]) dst;
                for (int i = 0; i < fromArray.length; ++i) {
                    values[i] = fromArray[i];
                }
            } else if (fromClass.equals(double.class)) {
                double[] fromArray = (double[]) dst;
                for (int i = 0; i < fromArray.length; ++i) {
                    values[i] = (float) fromArray[i];
                }
            } else {
                throw new IllegalArgumentException("Unknown array type:"
                        + fromClass.getName());
            }
            return values;
        }

    }


    /**
     * _more_
     *
     * @param request _more_
     * @param entry _more_
     *
     * @return _more_
     *
     * @throws Exception _more_
     */
    public Result outputTrajectoryMap(Request request, Entry entry)
            throws Exception {
        String               path         = getPath(entry);
        TrajectoryObsDataset tod          = trajectoryPool.get(path);
        StringBuffer         sb           = new StringBuffer();
        String               mapVarName   = "map" + HtmlUtil.blockCnt++;
        StringBuffer         js           = new StringBuffer();
        List                 trajectories = tod.getTrajectories();
        //TODO: Use new openlayers map
        for (int i = 0; i < trajectories.size(); i++) {
            List allVariables = tod.getDataVariables();
            TrajectoryObsDatatype todt =
                (TrajectoryObsDatatype) trajectories.get(i);
            float[]      lats     = toFloatArray(todt.getLatitude(null));
            float[]      lons     = toFloatArray(todt.getLongitude(null));
            StringBuffer markerSB = new StringBuffer();
            js.append("line = new Polyline([");
            for (int ptIdx = 0; ptIdx < lats.length; ptIdx++) {
                if (ptIdx > 0) {
                    js.append(",");
                    if (ptIdx == lats.length - 1) {
                        markerSB.append("var endMarker = new Marker("
                                        + MapOutputHandler.llp(lats[ptIdx],
                                            lons[ptIdx]) + ");\n");
                        markerSB.append("endMarker.setInfoBubble(\"End time:"
                                        + todt.getEndDate() + "\");\n");
                        markerSB.append("initMarker(endMarker,\"endMarker\","
                                        + mapVarName + ");\n");
                    }
                } else {
                    markerSB.append("var startMarker = new Marker("
                                    + MapOutputHandler.llp(lats[ptIdx],
                                        lons[ptIdx]) + ");\n");
                    markerSB.append("startMarker.setInfoBubble(\"Start time:"
                                    + todt.getStartDate() + "\");\n");
                    markerSB.append("initMarker(startMarker,\"startMarker\","
                                    + mapVarName + ");\n");
                }
                js.append(MapOutputHandler.llp(lats[ptIdx], lons[ptIdx]));
            }
            js.append("]);\n");
            js.append("line.setWidth(2);\n");
            js.append("line.setColor(\"#FF0000\");\n");
            js.append(mapVarName + ".addPolyline(line);\n");
            js.append(markerSB);
            StructureData    structure = todt.getData(0);
            VariableSimpleIF theVar    = null;
            for (int varIdx = 0; varIdx < allVariables.size(); varIdx++) {
                VariableSimpleIF var =
                    (VariableSimpleIF) allVariables.get(varIdx);
                if (var.getRank() != 0) {
                    continue;
                }
                theVar = var;
                break;
            }
            if (theVar == null) {
                continue;
            }
        }


        js.append(mapVarName + ".autoCenterAndZoom();\n");
        getRepository().getMapManager().initMap(request, mapVarName, sb, 800,
                500, true);
        sb.append(HtmlUtil.script(js.toString()));
        trajectoryPool.put(path, tod);
        return new Result(msg("Trajectory Map"), sb);


    }


    /*
    public Result outputTrajectoryChart(Request request, Entry entry)
            throws Exception {
        TrajectoryObsDataset tod = trajectoryPool.get(entry.getResource().getPath());
        StringBuffer sb         = new StringBuffer();
        StringBuffer head = new StringBuffer();
        head.append("<script type='text/javascript' src='http://www.google.com/jsapi'></script>\n");
        head.append("<script type='text/javascript'>\n");
        head.append("google.load('visualization', '1', {'packages':['annotatedtimeline']});\n");
        head.append("google.setOnLoadCallback(drawChart);\n");
        head.append("function drawChart() {");
        head.append("var data = new google.visualization.DataTable();\n");
        head.append("data.addColumn('date', 'Date');\n");
        head.append("data.addColumn('number', 'Value');\n");
        head.append("var chart = new google.visualization.AnnotatedTimeLine(document.getElementById('chart_div'));\n");
        sb.append("<div id='chart_div' style='width: 700px; height: 240px;'></div>\n");

        StringBuffer values  = new StringBuffer();
        int numRows = 0;
        synchronized (tod) {
            List         trajectories = tod.getTrajectories();
            List allVariables = tod.getDataVariables();
            TrajectoryObsDatatype todt =
                (TrajectoryObsDatatype) trajectories.get(0);
            float[]      lats     = toFloatArray(todt.getLatitude(null));
            numRows = lats.length;
            for (int ptIdx = 0; ptIdx < lats.length; ptIdx++) {
                StructureData    structure = todt.getData(0);
                VariableSimpleIF theVar    = null;
                for (int varIdx = 0; varIdx < allVariables.size(); varIdx++) {
                    VariableSimpleIF var =
                        (VariableSimpleIF) allVariables.get(varIdx);
                    if (var.getRank() != 0) {
                        continue;
                    }
                    theVar = var;
                    break;
               }
                if (theVar == null) {
                    continue;
                }
                values.append("data.setValue(0, 0, new Date(2008, 1 ,1));\n");
                values.append("data.setValue(0, 1, 30000);\n");
            }
            }

        head.append(values);
        head.append("data.addRows(" + numRows+");\n");
        head.append("chart.draw(data, {displayAnnotations: true});\n");
        head.append("}\n</script>\n");

        Result result =  new Result(msg("Trajectory Time Series"), sb);
        result.putProperty(PROP_HTML_HEAD,head.toString());
        return result;
    }
    */





    /**
     * _more_
     *
     * @param lat _more_
     * @param lon _more_
     *
     * @return _more_
     */
    public static String llp(double lat, double lon) {
        return "new LatLonPoint(" + lat + "," + lon + ")";
    }


    /**
     * _more_
     *
     * @param request _more_
     * @param entry _more_
     *
     * @return _more_
     *
     * @throws Exception _more_
     */
    public Result outputPointCsv(Request request, Entry entry)
            throws Exception {

        String               path         = getPath(entry);
        FeatureDatasetPoint  pod          = pointPool.get(path);
        StringBuffer         sb           = new StringBuffer();
        List                 vars         = pod.getDataVariables();
        PointFeatureIterator dataIterator = getPointIterator(pod);
        int                  cnt          = 0;
        while (dataIterator.hasNext()) {
            PointFeature po = (PointFeature) dataIterator.next();
            ucar.unidata.geoloc.EarthLocation el = po.getLocation();
            if (el == null) {
                continue;
            }
            cnt++;

            double        lat       = el.getLatitude();
            double        lon       = el.getLongitude();
            StructureData structure = po.getData();

            if (cnt == 1) {
                sb.append(HtmlUtil.quote("Time"));
                sb.append(",");
                sb.append(HtmlUtil.quote("Latitude"));
                sb.append(",");
                sb.append(HtmlUtil.quote("Longitude"));
                for (VariableSimpleIF var : (List<VariableSimpleIF>) vars) {
                    sb.append(",");
                    String unit = var.getUnitsString();
                    if (unit != null) {
                        sb.append(HtmlUtil.quote(var.getShortName() + " ("
                                + unit + ")"));
                    } else {
                        sb.append(HtmlUtil.quote(var.getShortName()));
                    }
                }
                sb.append("\n");
            }

            sb.append(HtmlUtil.quote("" + po.getNominalTimeAsDate()));
            sb.append(",");
            sb.append(el.getLatitude());
            sb.append(",");
            sb.append(el.getLongitude());

            for (VariableSimpleIF var : (List<VariableSimpleIF>) vars) {
                StructureMembers.Member member =
                    structure.findMember(var.getShortName());
                sb.append(",");
                if ((var.getDataType() == DataType.STRING)
                        || (var.getDataType() == DataType.CHAR)) {
                    sb.append(
                        HtmlUtil.quote(structure.getScalarString(member)));
                } else {
                    sb.append(structure.convertScalarFloat(member));
                }
            }
            sb.append("\n");
        }
        pointPool.put(path, pod);
        return new Result(msg("Point Data"), sb,
                          getRepository().getMimeTypeFromSuffix(".csv"));
    }



    /**
     * _more_
     *
     * @param request _more_
     * @param entry _more_
     *
     * @return _more_
     *
     * @throws Exception _more_
     */
    public Result outputPointKml(Request request, Entry entry)
            throws Exception {
        String               path         = getPath(entry);
        FeatureDatasetPoint  pod          = pointPool.get(path);
        Element              root         = KmlUtil.kml(entry.getName());
        Element              docNode = KmlUtil.document(root,
                                           entry.getName());
        List                 vars         = pod.getDataVariables();
        PointFeatureIterator dataIterator = getPointIterator(pod);

        while (dataIterator.hasNext()) {
            PointFeature po = (PointFeature) dataIterator.next();
            ucar.unidata.geoloc.EarthLocation el = po.getLocation();
            if (el == null) {
                continue;
            }
            double lat = el.getLatitude();
            double lon = el.getLongitude();
            double alt = 0;
            if ((lat != lat) || (lon != lon)) {
                continue;
            }

            StructureData structure = po.getData();
            StringBuffer  info      = new StringBuffer("");
            info.append("<b>Date:</b> " + po.getNominalTimeAsDate() + "<br>");
            for (VariableSimpleIF var : (List<VariableSimpleIF>) vars) {
                StructureMembers.Member member =
                    structure.findMember(var.getShortName());
                if ((var.getDataType() == DataType.STRING)
                        || (var.getDataType() == DataType.CHAR)) {
                    info.append("<b>" + var.getName() + ": </b>"
                                + structure.getScalarString(member) + "<br>");
                } else {
                    info.append("<b>" + var.getName() + ": </b>"
                                + structure.convertScalarFloat(member)
                                + "<br>");

                }
            }
            KmlUtil.placemark(docNode, "" + po.getNominalTimeAsDate(),
                              info.toString(), lat, lon, alt, null);
        }
        StringBuffer sb = new StringBuffer(XmlUtil.toString(root));
        pointPool.put(path, pod);
        return new Result(msg("Point Data"), sb,
                          getRepository().getMimeTypeFromSuffix(".kml"));

    }


    /**
     * _more_
     *
     * @param request _more_
     *
     * @return _more_
     */
    public AuthorizationMethod getAuthorizationMethod(Request request) {
        OutputType output = request.getOutput();
        if (output.equals(OUTPUT_WCS) || output.equals(OUTPUT_OPENDAP)) {
            return AuthorizationMethod.AUTH_HTTP;
        }
        return super.getAuthorizationMethod(request);
    }




    /**
     * Serve up the entry
     *
     * @param request _more_
     * @param outputType _more_
     * @param entry _more_
     *
     * @return _more_
     *
     * @throws Exception On badness
     */
    public Result outputEntry(Request request, OutputType outputType,
                              Entry entry)
            throws Exception {

        if ( !getRepository().getAccessManager().canDoAction(request, entry,
                Permission.ACTION_FILE)) {
            throw new AccessException("Cannot access data", request);
        }

        if ( !getRepository().getAccessManager().canAccessFile(request,
                entry)) {
            throw new AccessException("Cannot access data", request);
        }




        if (outputType.equals(OUTPUT_CDL)) {
            return outputCdl(request, entry);
        }
        if (outputType.equals(OUTPUT_WCS)) {
            return outputWcs(request, entry);
        }
        if (outputType.equals(OUTPUT_GRIDSUBSET)
                || outputType.equals(OUTPUT_GRIDSUBSET_FORM)) {
            return outputGridSubset(request, entry);
        }

        if (outputType.equals(OUTPUT_GRIDASPOINT)
                || outputType.equals(OUTPUT_GRIDASPOINT_FORM)) {
            return outputGridAsPoint(request, entry);
        }


        if (outputType.equals(OUTPUT_TRAJECTORY_MAP)) {
            return outputTrajectoryMap(request, entry);
        }


        if (outputType.equals(OUTPUT_POINT_MAP)) {
            return outputPointMap(request, entry);
        }
        if (outputType.equals(OUTPUT_POINT_CSV)) {
            return outputPointCsv(request, entry);
        }

        if (outputType.equals(OUTPUT_POINT_KML)) {
            return outputPointKml(request, entry);
        }


        if (outputType.equals(OUTPUT_OPENDAP)) {
            //If its a head request then just return the content description
            if (request.isHeadRequest()) {
                Result result = new Result("", new StringBuffer());
                result.addHttpHeader(HtmlUtil.HTTP_CONTENT_DESCRIPTION,
                                     "dods-dds");
                return result;
            }
            Result result = outputOpendap(request, entry);
            return result;
        }

        throw new IllegalArgumentException("Unknown output type:"
                                           + outputType);
    }


    /**
     * _more_
     *
     * @param entry _more_
     *
     * @return _more_
     *
     * @throws Exception _more_
     */
    public String getPath(Entry entry) throws Exception {
        String location;
        if (entry.getType().equals(OpendapLinkTypeHandler.TYPE_OPENDAPLINK)) {
            Resource resource = entry.getResource();
            location = resource.getPath();
            String ext = IOUtil.getFileExtension(location).toLowerCase();
            if (ext.equals(".html") || ext.equals(".das")
                    || ext.equals(".dds")) {
                location = IOUtil.stripExtension(location);
            }
        } else {
            File file = getEntryManager().getFileForEntry(entry);
            location = file.toString();
        }
        getStorageManager().checkPath(location);

        List<Metadata> metadataList =
            getMetadataManager().findMetadata(entry,
                ContentMetadataHandler.TYPE_ATTACHMENT, true);
        //        System.err.println("getPath");
        if (metadataList == null) {
            return location;
        }
        //        System.err.println("nd:" + metadataList);
        for (Metadata metadata : metadataList) {
            if (metadata.getAttr1().endsWith(".ncml")) {
                File templateNcmlFile =
                    new File(
                        IOUtil.joinDir(
                            getRepository().getStorageManager().getEntryDir(
                                metadata.getEntryId(),
                                false), metadata.getAttr1()));
                String ncml =
                    getStorageManager().readSystemResource(templateNcmlFile);
                ncml = ncml.replace("${location}", location);
                //                System.err.println("ncml:" + ncml);
                //Use the last modified time of the ncml file so we pick up any updated file
                String dttm = templateNcmlFile.lastModified() + "";
                String fileName = dttm + "_" + entry.getId() + "_"
                                  + metadata.getId() + ".ncml";
                File ncmlFile = getStorageManager().getScratchFile(fileName);
                IOUtil.writeBytes(ncmlFile, ncml.getBytes());
                location = ncmlFile.toString();
                break;
            }
        }
        return location;
    }



    /**
     * _more_
     *
     * @param request _more_
     * @param entry _more_
     *
     * @return _more_
     *
     * @throws Exception _more_
     */

    public Result outputOpendap(final Request request, final Entry entry)
            throws Exception {
        //jeffmc: this used to be synchronized and I just don't know why
        //whether there was a critical section here or what
        //But it has the potential to lock all access to this object
        //if opening a file hangs. So, lets remove the synchronized
        //    public synchronized Result outputOpendap(final Request request,

        String     location = getPath(entry);
        NetcdfFile ncFile   = ncFilePool.get(location);
        opendapCounter.incr();
        //        try {
        //Bridge the ramadda servlet to the opendap servlet
        NcDODSServlet servlet = new NcDODSServlet(request, entry, ncFile) {
            public ServletConfig getServletConfig() {
                return request.getHttpServlet().getServletConfig();
            }
            public ServletContext getServletContext() {
                return request.getHttpServlet().getServletContext();
            }
            public String getServletInfo() {
                return request.getHttpServlet().getServletInfo();
            }
            public Enumeration getInitParameterNames() {
                return request.getHttpServlet().getInitParameterNames();
            }
        };

        if ((request.getHttpServlet() != null)
                && (request.getHttpServlet().getServletConfig() != null)) {
            servlet.init(request.getHttpServlet().getServletConfig());
        }


        servlet.doGet(request.getHttpServletRequest(),
                      request.getHttpServletResponse());
        //We have to pass back a result though we set needtowrite to false because the opendap servlet handles the writing
        Result result = new Result("");
        result.setNeedToWrite(false);
        opendapCounter.decr();
        ncFilePool.put(location, ncFile);
        return result;
        //        } finally {
        //        }

    }


    /**
     * Class NcDODSServlet _more_
     *
     *
     * @author IDV Development Team
     * @version $Revision: 1.3 $
     */
    public class NcDODSServlet extends opendap.servlet.AbstractServlet {


        /** _more_ */
        Request repositoryRequest;

        /** _more_ */
        NetcdfFile ncFile;

        /** _more_ */
        Entry entry;

        /**
         * _more_
         *
         * @param request _more_
         * @param entry _more_
         * @param ncFile _more_
         */
        public NcDODSServlet(Request request, Entry entry,
                             NetcdfFile ncFile) {
            this.repositoryRequest = request;
            this.entry             = entry;
            this.ncFile            = ncFile;
        }

        /**
         * _more_
         *
         * @param preq _more_
         *
         * @return _more_
         *
         * @throws DAP2Exception On badness
         * @throws IOException On badness
         * @throws ParseException On badness
         */
        protected GuardedDataset getDataset(ReqState preq)
                throws DAP2Exception, IOException, ParseException {
            HttpServletRequest request = preq.getRequest();
            String             reqPath = entry.getName();

            try {
                GuardedDatasetImpl guardedDataset =
                    new GuardedDatasetImpl(reqPath, ncFile, true);
                return guardedDataset;
            } catch (Exception exc) {
                throw new WrapperException(exc);
            }
        }

        /**
         * _more_
         *
         * @return _more_
         */
        public String getServerVersion() {
            return "opendap/3.7";
        }
    }


}
