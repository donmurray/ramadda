/*
 * Copyright 2008-2011 Jeff McWhirter/ramadda.org
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

package org.ramadda.geodata.data;


import opendap.dap.DAP2Exception;



//import opendap.dap.parser.ParseException;

import opendap.servlet.GuardedDataset;
import opendap.servlet.ReqState;

//import ucar.nc2.dt.PointObsDataset;
//import ucar.nc2.dt.PointObsDatatype;

import org.apache.commons.httpclient.*;
import org.apache.commons.httpclient.auth.*;

import org.jfree.chart.*;
import org.jfree.chart.annotations.*;
import org.jfree.chart.axis.*;
import org.jfree.chart.entity.*;
import org.jfree.chart.event.*;
import org.jfree.chart.labels.*;
import org.jfree.chart.plot.*;
import org.jfree.chart.renderer.xy.*;
import org.jfree.data.*;
import org.jfree.data.general.*;
import org.jfree.data.time.*;
import org.jfree.data.xy.*;
import org.jfree.ui.*;


import org.ramadda.repository.*;
import org.ramadda.repository.auth.*;
import org.ramadda.repository.map.*;
import org.ramadda.repository.metadata.*;
import org.ramadda.repository.output.*;

import org.ramadda.repository.type.TypeHandler;



import org.ramadda.util.TempDir;

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

import ucar.nc2.ncml.NcMLWriter;
import ucar.nc2.units.DateFormatter;
import ucar.nc2.units.DateType;
import ucar.nc2.util.DiskCache2;

import ucar.unidata.data.gis.KmlUtil;
import ucar.unidata.geoloc.LatLonPointImpl;

import ucar.unidata.geoloc.LatLonRect;
import ucar.unidata.ui.ImageUtils;

import ucar.unidata.util.Cache;
import ucar.unidata.util.Counter;

import ucar.unidata.util.DateUtil;
import ucar.unidata.util.GuiUtils;
import ucar.unidata.util.HtmlUtil;
import ucar.unidata.util.IOUtil;
import ucar.unidata.util.Misc;
import ucar.unidata.util.Pool;
import ucar.unidata.util.StringUtil;
import ucar.unidata.util.TwoFacedObject;
import ucar.unidata.util.WrapperException;
import ucar.unidata.xml.XmlUtil;

import ucar.visad.Util;

import java.awt.Color;
import java.awt.Image;
import java.awt.image.BufferedImage;

import java.io.*;

import java.net.*;

import java.text.SimpleDateFormat;

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
import java.util.regex.*;

import javax.servlet.*;

import javax.servlet.http.*;


/**
 *
 *
 * @author IDV Development Team
 * @version $Revision: 1.3 $
 */
public class DataOutputHandler extends OutputHandler {

    /** _more_ */
    public static final String FORMAT_CSV = "csv";

    /** _more_ */
    public static final String FORMAT_KML = "kml";

    /** _more_ */
    public static final String SUFFIX_NCML = ".ncml";

    /** _more_ */
    public static final String SUFFIX_CTL = ".ctl";

    /** _more_ */
    public static final String ARG_POINT_BBOX = "bbox";

    /** _more_ */
    public static final String FORMAT_NCML = "ncml";

    /** Variable prefix */
    public static final String VAR_PREFIX = ARG_VARIABLE + ".";

    /** add lat lon argument */
    public static final String ARG_ADDLATLON = "addlatlon";

    /** subset area argument */
    public static final String ARG_SUBSETAREA = "subsetarea";

    /** subset time argument */
    public static final String ARG_SUBSETTIME = "subsettime";

    /** horizontal stride */
    public static final String ARG_HSTRIDE = "hstride";

    /** level */
    public static final String ARG_LEVEL = "level";

    /** format */
    public static final String ARG_FORMAT = "format";

    /** format */
    public static final String ARG_IMAGE_WIDTH = "image_width";

    /** format */
    public static final String ARG_IMAGE_HEIGHT = "image_height";

    /** chart format */
    private static final String FORMAT_TIMESERIES = "timeseries";

    /** chart format */
    private static final String FORMAT_TIMESERIES_CHART = "timeserieschart";

    /** chart format */
    private static final String FORMAT_TIMESERIES_CHART_DATA =
        "timeserieschartdata";

    /** chart image format */
    private static final String FORMAT_TIMESERIES_IMAGE = "timeseriesimage";

    /** _more_ */
    public static final String GROUP_DATA = "Data";


    /** OPeNDAP Output Type */
    public static final OutputType OUTPUT_OPENDAP =
        new OutputType("OPeNDAP", "data.opendap", OutputType.TYPE_OTHER,
                       OutputType.SUFFIX_NONE, ICON_OPENDAP, GROUP_DATA);

    /** CDL Output Type */
    public static final OutputType OUTPUT_CDL = new OutputType("File Metadata",
                                                    "data.cdl",
                                                    OutputType.TYPE_OTHER,
                                                    OutputType.SUFFIX_NONE,
                                                    ICON_DATA, GROUP_DATA);

    /** WCS Output Type */
    public static final OutputType OUTPUT_WCS = new OutputType("WCS",
                                                    "data.wcs",
                                                    OutputType.TYPE_FEEDS);

    /** Point map Output Type */
    public static final OutputType OUTPUT_POINT_MAP =
        new OutputType("Plot Points on a Map", "data.point.map",
                       OutputType.TYPE_OTHER, OutputType.SUFFIX_NONE,
                       ICON_MAP, GROUP_DATA);

    /** CSV Output Type */
    public static final OutputType OUTPUT_POINT_SUBSET =
        new OutputType("CSV, KML Output", "data.point.subset",
                       OutputType.TYPE_OTHER, OutputType.SUFFIX_NONE,
                       ICON_CSV, GROUP_DATA);


    /** Trajectory map Output Type */
    public static final OutputType OUTPUT_TRAJECTORY_MAP =
        new OutputType("Trajectory as Map", "data.trajectory.map",
                       OutputType.TYPE_OTHER, OutputType.SUFFIX_NONE,
                       ICON_MAP, GROUP_DATA);

    /** Grid subset form Output Type */
    public static final OutputType OUTPUT_GRIDSUBSET_FORM =
        new OutputType("Subset Spatially", "data.gridsubset.form",
                       OutputType.TYPE_OTHER, OutputType.SUFFIX_NONE,
                       ICON_SUBSET, GROUP_DATA);

    /** Grid subset Output Type */
    public static final OutputType OUTPUT_GRIDSUBSET =
        new OutputType("data.gridsubset", OutputType.TYPE_FEEDS);

    /** Grid as point form Output Type */
    public static final OutputType OUTPUT_GRIDASPOINT_FORM =
        new OutputType("Extract Time Series", "data.gridaspoint.form",
                       OutputType.TYPE_OTHER, OutputType.SUFFIX_NONE,
                       ICON_SUBSET, GROUP_DATA);

    /** Grid as point Output Type */
    public static final OutputType OUTPUT_GRIDASPOINT =
        new OutputType("data.gridaspoint", OutputType.TYPE_FEEDS);

    /** cdm cache */
    private Cache<String, Boolean> cdmEntries = new Cache<String,
                                                    Boolean>(5000);

    /** grid entries cache */
    private Cache<String, Boolean> gridEntries = new Cache<String,
                                                     Boolean>(5000);


    /** point entries cache */
    private Cache<String, Boolean> pointEntries = new Cache<String,
                                                      Boolean>(5000);

    /** trajectory entries cache */
    private Cache<String, Boolean> trajectoryEntries = new Cache<String,
                                                           Boolean>(5000);


    /** nj cache directory */
    private TempDir nj22Dir;

    /** data cache directory */
    private TempDir dataCacheDir;


    //TODO: When we close a ncfile some thread might be using it
    //Do we have to actually close it??

    /** nc counter */
    Counter ncCounter = new Counter();

    /** nc create counter */
    Counter ncCreateCounter = new Counter();

    /** nc remove counter */
    Counter ncRemoveCounter = new Counter();

    /** nc get counter */
    Counter ncGetCounter = new Counter();

    /** nc put counter */
    Counter ncPutCounter = new Counter();

    /** ext counter */
    Counter extCounter = new Counter();

    /** opendap counter */
    Counter opendapCounter = new Counter();


    /** grid open counter */
    Counter gridOpenCounter = new Counter();

    /** grid close counter */
    Counter gridCloseCounter = new Counter();


    /** point open counter */
    Counter pointOpenCounter = new Counter();

    /** point close counter */
    Counter pointCloseCounter = new Counter();

    /** nc dataset pool */
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




    /** nc file pool */
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
    private boolean doGridPool = true;

    /** grid pool */
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


    /**
     * _more_
     *
     * @param path _more_
     *
     * @return _more_
     */
    private GridDataset createGrid(String path) {
        try {
            getStorageManager().dirTouched(nj22Dir, null);
            //            gridOpenCounter.incr();

            GridDataset gds = GridDataset.open(path);
            if (gds.getGrids().iterator().hasNext()) {
                return gds;
            } else {
                //                gridCloseCounter.incr();
                gds.close();
                return null;
            }
        } catch (Exception exc) {
            throw new RuntimeException(exc);
        }
    }



    /** point pool */
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


    /** trajectory pool */
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
     * _more_
     *
     * @param repository _more_
     * @param name _more_
     *
     * @throws Exception _more_
     */
    public DataOutputHandler(Repository repository, String name)
            throws Exception {
        super(repository, name);
    }

    /**
     *     Create a DataOutputHandler
     *
     *     @param repository  the repository
     *     @param element     the element
     *     @throws Exception On badness
     */
    public DataOutputHandler(Repository repository, Element element)
            throws Exception {
        super(repository, element);

        //TODO: what other global configuration should be done?
        nj22Dir = getRepository().getStorageManager().makeTempDir("nj22");
        nj22Dir.setMaxFiles(500);

        // Apply settings for the NetcdfDataset
        //        ucar.nc2.dataset.NetcdfDataset.setHttpClient(getRepository().getHttpClient());


        // Apply settings for the opendap.dap
        //        opendap.dap.DConnect2.setHttpClient(getRepository().getHttpClient());



        //Set the temp file and the cache policy
        ucar.nc2.util.DiskCache.setRootDirectory(nj22Dir.getDir().toString());
        ucar.nc2.util.DiskCache.setCachePolicy(true);
        //        ucar.nc2.iosp.grib.GribServiceProvider.setIndexAlwaysInCache(true);
        ucar.nc2.iosp.grid.GridServiceProvider.setIndexAlwaysInCache(true);

        dataCacheDir =
            getRepository().getStorageManager().makeTempDir("visaddatacache");
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
        addType(OUTPUT_POINT_SUBSET);
        addType(OUTPUT_GRIDSUBSET);
        addType(OUTPUT_GRIDSUBSET_FORM);
        addType(OUTPUT_GRIDASPOINT);
        addType(OUTPUT_GRIDASPOINT_FORM);
    }


    /**
     * Get the system stats
     *
     * @param sb  the stats
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
     * clear the cache
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
     * Add to an entry
     *
     * @param request the request
     * @param entry  the entry
     * @param node   the node
     *
     * @throws Exception  on badness
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
     * Get the Entry links
     *
     * @param request  the request
     * @param state    the state
     * @param links    the links
     *
     * @throws Exception on badness
     */
    public void getEntryLinks(Request request, State state, List<Link> links)
            throws Exception {

        Entry entry = state.entry;

        if ((state.group != null)
                && state.group.getType().equals(
                    GridAggregationTypeHandler.TYPE_GRIDAGGREGATION)) {
            entry = state.group;
        }


        if (entry == null) {
            return;
        }

        if ( !getRepository().getAccessManager().canAccessFile(request,
                entry)) {
            return;
        }

        long    t1           = System.currentTimeMillis();
        boolean canLoadAsCdm = canLoadAsCdm(entry);

        if ( !canLoadAsCdm) {
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
            addOutputLink(request, entry, links, OUTPUT_POINT_SUBSET);
        }

        Object oldOutput = request.getOutput();
        request.put(ARG_OUTPUT, OUTPUT_OPENDAP);
        String opendapUrl = getRepository().URL_ENTRY_SHOW + "/"
                            + request.getPathEmbeddedArgs() + "/"
                            + getStorageManager().getFileTail(entry)
                            + "/dodsC/entry.das";
        links.add(new Link(opendapUrl, getRepository().iconUrl(ICON_OPENDAP),
                           "OPeNDAP", OUTPUT_OPENDAP));
        request.put(ARG_OUTPUT, oldOutput);


        Link cdlLink = makeLink(request, entry, OUTPUT_CDL);
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

        if (entry.getType().equals(
                GridAggregationTypeHandler.TYPE_GRIDAGGREGATION)) {
            return true;
        }
        if ( !entry.getType().equals(
                OpendapLinkTypeHandler.TYPE_OPENDAPLINK)) {
            if ( !entry.isFile()) {
                return false;
            }

            if (excludedByPattern(entry, TYPE_CDM)) {
                return false;
            }
        }

        String[] types = { TYPE_CDM, TYPE_GRID, TYPE_TRAJECTORY, TYPE_POINT };
        for (int i = 0; i < types.length; i++) {
            if (includedByPattern(entry, types[i])) {
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
        if (excludedByPattern(entry, TYPE_POINT)) {
            return false;
        }
        if (includedByPattern(entry, TYPE_POINT)) {
            return true;
        }
        if ( !canLoadAsCdm(entry)) {
            return false;
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
        if (excludedByPattern(entry, TYPE_TRAJECTORY)) {
            return false;
        }
        if (includedByPattern(entry, TYPE_TRAJECTORY)) {
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
    private HashSet<String> suffixSet;

    /** _more_ */
    private Hashtable<String, List<Pattern>> patterns;

    /** _more_ */
    private Hashtable<String, List<Pattern>> notPatterns;


    /**
     * _more_
     *
     * @param entry _more_
     * @param type _more_
     *
     * @return _more_
     */
    private boolean excludedByPattern(Entry entry, String type) {
        return hasSuffixForType(entry, type, true);
    }

    /**
     * _more_
     *
     * @param entry _more_
     * @param type _more_
     *
     * @return _more_
     */
    private boolean includedByPattern(Entry entry, String type) {
        return hasSuffixForType(entry, type, false);
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
    private boolean hasSuffixForType(Entry entry, String type,
                                     boolean forNot) {
        String url = entry.getResource().getPath();
        if (url == null) {
            return false;
        }
        return hasSuffixForType(url, type, forNot);
    }

    /**
     * _more_
     *
     * @param url _more_
     * @param type _more_
     * @param forNot _more_
     *
     * @return _more_
     */
    private boolean hasSuffixForType(String url, String type,
                                     boolean forNot) {
        if (suffixSet == null) {
            HashSet<String> tmpSuffixSet = new HashSet<String>();

            Hashtable<String, List<Pattern>> tmpPatterns =
                new Hashtable<String, List<Pattern>>();
            Hashtable<String, List<Pattern>> tmpNotPatterns =
                new Hashtable<String, List<Pattern>>();




            String[] types = { TYPE_CDM, TYPE_GRID, TYPE_TRAJECTORY,
                               TYPE_POINT };
            for (int i = 0; i < types.length; i++) {
                List toks = StringUtil.split(
                                getRepository().getProperty(
                                    "ramadda.data." + types[i] + ".suffixes",
                                    ""), ",", true, true);
                for (String tok : (List<String>) toks) {
                    if ((tok.length() == 0) || tok.equals("!")) {
                        continue;
                    }
                    String key = types[i] + "." + tok;
                    tmpSuffixSet.add(key);
                }
            }



            for (int i = 0; i < types.length; i++) {
                tmpPatterns.put(types[i], new ArrayList<Pattern>());
                tmpNotPatterns.put(types[i], new ArrayList<Pattern>());
                List patterns = StringUtil.split(
                                    getRepository().getProperty(
                                        "ramadda.data." + types[i]
                                        + ".patterns", ""), ",", true, true);
                for (String pattern : (List<String>) patterns) {
                    if ((pattern.length() == 0) || pattern.equals("!")) {
                        continue;
                    }
                    Hashtable<String, List<Pattern>> tmp;
                    if (pattern.startsWith("!")) {
                        tmp     = tmpNotPatterns;
                        pattern = pattern.substring(1);
                    } else {
                        tmp = tmpPatterns;
                    }
                    tmp.get(types[i]).add(Pattern.compile(pattern));
                }
            }

            patterns    = tmpPatterns;
            notPatterns = tmpNotPatterns;
            suffixSet   = tmpSuffixSet;

        }

        url = url.toLowerCase();



        //First check the patterns
        List<Pattern> patternList = (forNot
                                     ? notPatterns.get(type)
                                     : patterns.get(type));
        for (Pattern pattern : patternList) {
            if (pattern.matcher(url).find()) {
                return true;
            }
        }


        String ext    = IOUtil.getFileExtension(url);
        String key    = type + "." + ext;
        String notKey = type + ".!" + ext;
        if (forNot) {
            if (suffixSet.contains(notKey)) {
                return true;
            }
        } else {
            if (suffixSet.contains(key)) {
                return true;
            }
        }


        return false;
    }

    /**
     * _more_
     *
     * @param entry _more_
     *
     * @return _more_
     */
    public boolean canLoadAsGrid(Entry entry) {
        if (entry.getType().equals(
                GridAggregationTypeHandler.TYPE_GRIDAGGREGATION)) {
            return true;
        }
        if (excludedByPattern(entry, TYPE_GRID)) {
            return false;
        }
        if (includedByPattern(entry, TYPE_GRID)) {
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
                    if (doGridPool) {
                        ok = gridPool.containsOrCreate(getPath(entry));
                    } else {
                        ok = (createGrid(getPath(entry)) != null);
                    }
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
        String path = getPath(request, entry);
        if (request.getString(ARG_FORMAT, "").equals(FORMAT_NCML)) {

            /**
             *  This gets hung up calling back into the repository
             *  so for now don't do it and just use the file
             */
            String opendapUrl = getFullTdsUrl(entry);
            path = opendapUrl;
            NetcdfFile ncFile = NetcdfDataset.openFile(path, null);
            NcMLWriter writer = new NcMLWriter();
            String     xml    = writer.writeXML(ncFile);
            Result result = new Result("", new StringBuffer(xml), "text/xml");
            ncFile.close();
            return result;
        }


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
                                    HtmlUtil.cssClass(CSS_CLASS_SEPARATOR)));

            sb.append(HtmlUtil.href(request.getUrl(),
                                    msg("Add full properties")));
            sb.append(HtmlUtil.span("&nbsp;|&nbsp;",
                                    HtmlUtil.cssClass(CSS_CLASS_SEPARATOR)));
        }
        String tail =
            IOUtil.stripExtension(getStorageManager().getFileTail(entry));

        sb.append(HtmlUtil.href(HtmlUtil.url(getRepository().URL_ENTRY_SHOW
                                             + "/" + tail
                                             + SUFFIX_NCML, new String[] {
            ARG_ENTRYID, entry.getId(), ARG_OUTPUT, OUTPUT_CDL.getId(),
            ARG_FORMAT, FORMAT_NCML
        }), "NCML"));


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
     *
     * @throws Exception _more_
     */
    public GridDataset getGridDataset(Entry entry, String path)
            throws Exception {
        if ( !canLoadAsGrid(entry)) {
            return null;
        }
        //Don't cache the aggregations
        if (entry.getType().equals(
                GridAggregationTypeHandler.TYPE_GRIDAGGREGATION)) {
            return GridDataset.open(path);
        }
        if (doGridPool) {
            return gridPool.get(path);
        } else {
            return createGrid(path);
        }
    }

    /**
     * _more_
     *
     * @param path _more_
     * @param ncd _more_
     */
    public void returnGridDataset(String path, GridDataset ncd) {
        if (doGridPool) {
            gridPool.put(path, ncd);
        }
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


    /**
     * Process a grid as point request
     *
     * @param request  the request
     * @param entry    the entry
     * @param gds      the corresponding grid dataset
     * @param sb       the StringBuffer
     *
     * @return a Result
     *
     * @throws Exception problem doing what was asked
     */
    public Result outputGridAsPointProcess(Request request, Entry entry,
                                           GridDataset gds, StringBuffer sb)
            throws Exception {

        List      varNames = new ArrayList<String>();
        Hashtable args     = request.getArgs();
        for (Enumeration keys = args.keys(); keys.hasMoreElements(); ) {
            String arg = (String) keys.nextElement();
            if (arg.startsWith(VAR_PREFIX) && request.get(arg, false)) {
                varNames.add(arg.substring(VAR_PREFIX.length()));
            }
        }
        //            System.err.println(varNames);
        LatLonRect llr    = gds.getBoundingBox();
        double     deflat = 0;
        double     deflon = 0;
        if (llr != null) {
            deflat = llr.getLatMin() + llr.getHeight() / 2;
            deflon = llr.getCenterLon();
        }
        LatLonPointImpl llp = null;

        if (request.get(ARG_LOCATION, true)) {
            llp = new LatLonPointImpl(
                request.getLatOrLonValue(ARG_LOCATION + ".latitude", deflat),
                request.getLatOrLonValue(
                    ARG_LOCATION + ".longitude", deflon));
        }
        double levelVal   = request.get(ARG_LEVEL, Double.NaN);

        int    timeStride = 1;
        Date[] dates      = new Date[] { request.get(ARG_SUBSETTIME, false)
                                         ? request.getDate(ARG_FROMDATE, null)
                                         : null,
                                         request.get(ARG_SUBSETTIME, false)
                                         ? request.getDate(ARG_TODATE, null)
                                         : null };
        if ((dates[0] != null) && (dates[1] != null)
                && (dates[0].getTime() > dates[1].getTime())) {
            sb.append(
                getRepository().showDialogWarning(
                    "From date is after to date"));
        } else if (varNames.size() == 0) {
            sb.append(
                getRepository().showDialogWarning("No variables selected"));
        } else {
            //                System.err.println ("varNames:" + varNames);

            QueryParams qp = new QueryParams();
            String format  = request.getString(ARG_FORMAT,
                                 QueryParams.NETCDF);
            qp.acceptType = (format.equals(FORMAT_TIMESERIES_CHART)
                             || format.equals(FORMAT_TIMESERIES_IMAGE))
                            ? QueryParams.CSV
                            : format;

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
            if (levelVal == levelVal) {
                qp.hasVerticalCoord = true;
                qp.vertCoord        = levelVal;
            }
            String suffix = ".nc";
            if (qp.acceptType.equals(QueryParams.CSV)
                    || format.equals(FORMAT_TIMESERIES_CHART_DATA)
                    || format.equals(FORMAT_TIMESERIES_IMAGE)) {
                suffix = ".csv";
            } else if (qp.acceptType.equals(QueryParams.XML)) {
                suffix = ".xml";
            }

            String baseName = IOUtil.stripExtension(entry.getName());
            if (format.equals(FORMAT_TIMESERIES_CHART)) {
                StringBuffer buf = new StringBuffer();
                String chartTemplate =
                    getRepository().getResource(
                        "/org/ramadda/repository/resources/chart/dycharts.html");
                chartTemplate = chartTemplate.replaceAll("\\$\\{urlroot\\}",
                        getRepository().getUrlBase());
                //String title = request.getString(ARG_POINT_TIMESERIES_TITLE,
                //                   entry.getName());
                String title = "Data at: " + llp.toString();
                if (title.equals("")) {
                    title = entry.getName();
                }
                chartTemplate = chartTemplate.replace("${title}", title);
                StringBuffer vizsb =
                    new StringBuffer("visibility: [false, false");
                if (qp.hasVerticalCoord) {
                    vizsb.append(", false");
                }
                for (int var = 0; var < varNames.size(); var++) {
                    vizsb.append(", true");
                }
                vizsb.append("],");
                chartTemplate = chartTemplate.replace("${options}",
                        vizsb.toString());

                String html = chartTemplate;
                request.put(ARG_FORMAT, QueryParams.CSV);
                String dataUrl = request.getRequestPath() + "/" + baseName
                                 + suffix + "?" + request.getUrlArgs();
                html = html.replace("${dataurl}", dataUrl);

                buf.append(html);
                return new Result("Point As Grid Time Series", buf);
            }

            File tmpFile = getStorageManager().getTmpFile(request,
                               "pointsubset" + suffix);

            GridPointWriter writer =
                new GridPointWriter(gds,
                                    new DiskCache2(getRepository()
                                        .getStorageManager().getTmpDir()
                                        .toString(), false, 0, 0));
            OutputStream outStream =
                (qp.acceptType.equals(QueryParams.NETCDF))
                ? System.out
                : getStorageManager().getFileOutputStream(tmpFile);

            PrintWriter pw = new PrintWriter(outStream);
            File        f  = writer.write(qp, pw);
            if (f == null) {
                outStream.close();
                f = tmpFile;
            }
            if (doingPublish(request)) {
                return getEntryManager().processEntryPublish(request, f,
                        (Entry) entry.clone(), entry, "point series of");
            }
            Result result = null;
            if (format.equals(FORMAT_TIMESERIES_IMAGE)) {
                result = outputTimeSeriesImage(request, entry, f);
            } else {
                result =
                    new Result(getStorageManager().getFileInputStream(f),
                               qp.acceptType);
                //Set return filename sets the Content-Disposition http header so the browser saves the file
                //with the correct name and suffix
                result.setReturnFilename(baseName + suffix);
            }
            return result;
        }

        return new Result("", sb);
    }


    /**
     * Output the grid as a point form
     *
     * @param request   the request
     * @param entry     the entry
     * @param dataset   the corresponding dataset
     * @param sb        the string buffer
     *
     * @return the result
     *
     * @throws Exception problem creating form
     */
    public Result outputGridAsPointForm(Request request, Entry entry,
                                        GridDataset dataset, StringBuffer sb)
            throws Exception {

        boolean canAdd =
            getRepository().getAccessManager().canDoAction(request,
                entry.getParentEntry(), Permission.ACTION_NEW);

        String formUrl  = request.url(getRepository().URL_ENTRY_SHOW);
        String fileName = IOUtil.stripExtension(entry.getName()) + "_point";

        sb.append(HtmlUtil.form(formUrl + "/" + fileName));
        sb.append(HtmlUtil.br());



        sb.append(HtmlUtil.submit("Get Point", ARG_SUBMIT));
        sb.append(HtmlUtil.br());
        sb.append(HtmlUtil.hidden(ARG_OUTPUT, OUTPUT_GRIDASPOINT));
        sb.append(HtmlUtil.hidden(ARG_ENTRYID, entry.getId()));
        sb.append(HtmlUtil.formTable());



        Date[]       dateRange = null;
        List<Date>   dates     = getGridDates(dataset);

        StringBuffer varSB     = getVariableForm(dataset, true);

        LatLonRect   llr       = dataset.getBoundingBox();
        String       lat       = "";
        String       lon       = "";
        if (llr != null) {
            lat = Misc.format(llr.getLatMin() + llr.getHeight() / 2);
            lon = Misc.format(llr.getCenterLon());
        }
        MapInfo map = getRepository().getMapManager().createMap(request,
                          true);
        map.addBox("", llr, new MapProperties("blue", false, true));
        String llb = map.makeSelector(ARG_LOCATION, true, new String[] { lat,
                lon });
        sb.append(HtmlUtil.formEntryTop(msgLabel("Location"), llb));

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
        List formats = Misc.toList(new Object[] {
                           new TwoFacedObject("NetCDF", QueryParams.NETCDF),
                           new TwoFacedObject("Xml", QueryParams.XML),
                           new TwoFacedObject("Time Series Image",
                               FORMAT_TIMESERIES),
                           new TwoFacedObject("Interactive Time Series",
                               FORMAT_TIMESERIES_CHART),
                           new TwoFacedObject("Comma Separated Values (CSV)",
                               QueryParams.CSV) });

        String format = request.getString(ARG_FORMAT, QueryParams.NETCDF);

        sb.append(HtmlUtil.formEntry(msgLabel("Format"),
                                     HtmlUtil.select(ARG_FORMAT, formats,
                                         format)));


        addPublishWidget(request, entry, sb,
                         msg("Select a folder to publish the point data to"));
        sb.append(HtmlUtil.formTableClose());
        sb.append("<hr>");
        sb.append(msgLabel("Select Variables"));
        sb.append(HtmlUtil.insetDiv(HtmlUtil.table(varSB.toString(),
                HtmlUtil.attrs(HtmlUtil.ATTR_CELLPADDING, "5",
                               HtmlUtil.ATTR_CELLSPACING, "0")), 0, 30, 0,
                                   0));

        sb.append(HtmlUtil.submit("Get Point"));
        //sb.append(submitExtra);
        sb.append(HtmlUtil.formClose());

        return makeLinksResult(request, msg("Grid As Point"), sb,
                               new State(entry));
    }

    /**
     * Get the grid dates
     *
     * @param dataset  the dataset
     *
     * @return  the dates or null
     */
    private List<Date> getGridDates(GridDataset dataset) {
        List<Date>         gridDates = null;
        List<GridDatatype> grids     = dataset.getGrids();
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
        if ( !dateHash.isEmpty()) {
            gridDates =
                Arrays.asList(dateHash.toArray(new Date[dateHash.size()]));
            Collections.sort(gridDates);
        }
        return gridDates;
    }

    /**
     * Get the variable selector form
     *
     * @param dataset  the dataset
     * @param withLevelSelector  if true, include a level selector widget
     *
     * @return  the form
     */
    protected StringBuffer getVariableForm(GridDataset dataset,
                                           boolean withLevelSelector) {
        int          varCnt  = 0;
        StringBuffer varSB   = new StringBuffer();
        StringBuffer varSB2D = new StringBuffer();
        StringBuffer varSB3D = new StringBuffer();

        for (GridDatatype grid : sortGrids(dataset)) {
            String cbxId = "varcbx_" + (varCnt++);
            String call = HtmlUtil.attr(
                              HtmlUtil.ATTR_ONCLICK,
                              HtmlUtil.call(
                                  "checkboxClicked",
                                  HtmlUtil.comma(
                                      "event", HtmlUtil.squote(ARG_VARIABLE),
                                      HtmlUtil.squote(cbxId))));
            VariableEnhanced var     = grid.getVariable();
            StringBuffer     sbToUse = (grid.getZDimension() == null)
                                       ? varSB2D
                                       : varSB3D;

            sbToUse.append(
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
        if (varSB2D.length() > 0) {
            if (varSB3D.length() > 0) {
                varSB.append(HtmlUtil.row(HtmlUtil.headerCols(new Object[] {
                    "2D Grids" })));
            }
            varSB.append(varSB2D);
        }
        if (varSB3D.length() > 0) {
            if ((varSB2D.length() > 0) || withLevelSelector) {
                String header = " 3D Grids";
                if (withLevelSelector) {
                    header += HtmlUtil.space(3) + "Level:"
                              + HtmlUtil.space(1)
                              + HtmlUtil.input(ARG_LEVEL, "");
                }
                varSB.append(HtmlUtil.row(HtmlUtil.headerCols(new Object[] {
                    header })));
            }
            varSB.append(varSB3D);
        }
        return varSB;
    }

    /**
     * Handle a grid as point request
     *
     * @param request  the request
     * @param entry    the entry
     *
     * @return  the result
     *
     * @throws Exception problems
     */
    public Result outputGridAsPoint(Request request, Entry entry)
            throws Exception {
        String format   = request.getString(ARG_FORMAT, QueryParams.NETCDF);
        String baseName = IOUtil.stripExtension(entry.getName());
        if (format.equals(FORMAT_TIMESERIES)) {
            request.put(ARG_FORMAT, FORMAT_TIMESERIES_IMAGE);
            String redirectUrl = request.getRequestPath() + "/" + baseName
                                 + ".png" + "?" + request.getUrlArgs();
            return new Result("Point As Grid Time Series Image",
                              new StringBuffer(HtmlUtil.img(redirectUrl,
                                  "Image is being processed...")));
        }
        StringBuffer sb     = new StringBuffer();
        String       path   = getPath(request, entry);

        GridDataset  gds    = getGridDataset(entry, path);
        OutputType   output = request.getOutput();
        try {
            if (output.equals(OUTPUT_GRIDASPOINT)) {
                Result result = outputGridAsPointProcess(request, entry, gds,
                                    sb);
                if (result != null) {
                    return result;
                }
            }
            return outputGridAsPointForm(request, entry, gds, sb);
        } finally {
            returnGridDataset(path, gds);
            //gridPool.put(path, gds);
        }
    }


    /**
     * Handle a grid subset request
     *
     * @param request the request
     * @param entry   the entry
     *
     * @return  a Result
     *
     * @throws Exception  problem handling the request
     */
    public Result outputGridSubset(Request request, Entry entry)
            throws Exception {

        boolean canAdd =
            getRepository().getAccessManager().canDoAction(request,
                entry.getParentEntry(), Permission.ACTION_NEW);




        String       path   = getPath(request, entry);
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
                                         : null,
                                         request.get(ARG_SUBSETTIME, false)
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
                GridDataset gds = getGridDataset(entry, path);
                writer.makeFile(f.toString(), gds, varNames, llr,
                                ((dates[0] == null)
                                 ? null
                                 : new ucar.nc2.units.DateRange(dates[0],
                                 dates[1])), includeLatLon, hStride, zStride,
                                             timeStride);
                returnGridDataset(path, gds);
                //                gridPool.put(path, gds);

                if (doingPublish(request)) {
                    TypeHandler typeHandler =
                        getRepository().getTypeHandler(TypeHandler.TYPE_FILE);
                    Entry newEntry =
                        typeHandler.createEntry(getRepository().getGUID());
                    return getEntryManager().processEntryPublish(request, f,
                            newEntry, entry, "subset of");
                }

                return new Result(entry.getName() + ".nc",
                                  getStorageManager().getFileInputStream(f),
                                  "application/x-netcdf");
            }
        }

        String formUrl = request.url(getRepository().URL_ENTRY_SHOW);
        String fileName = IOUtil.stripExtension(entry.getName())
                          + "_subset.nc";

        sb.append(HtmlUtil.form(formUrl + "/" + fileName));
        sb.append(HtmlUtil.br());

        sb.append(HtmlUtil.submit("Subset Grid", ARG_SUBMIT));
        sb.append(HtmlUtil.br());
        sb.append(HtmlUtil.hidden(ARG_OUTPUT, OUTPUT_GRIDSUBSET));
        sb.append(HtmlUtil.hidden(ARG_ENTRYID, entry.getId()));
        sb.append(HtmlUtil.formTable());



        sb.append(HtmlUtil.formEntry(msgLabel("Horizontal Stride"),
                                     HtmlUtil.input(ARG_HSTRIDE,
                                         request.getString(ARG_HSTRIDE, "1"),
                                         HtmlUtil.SIZE_3)));


        GridDataset  dataset   = getGridDataset(entry, path);
        Date[]       dateRange = null;
        List<Date>   dates     = getGridDates(dataset);
        StringBuffer varSB     = getVariableForm(dataset, false);
        LatLonRect   llr       = dataset.getBoundingBox();
        if (llr != null) {
            MapInfo map = getRepository().getMapManager().createMap(request,
                              true);
            map.addBox("", llr, new MapProperties("blue", false, true));
            String llb = map.makeSelector(ARG_AREA, true,
                                          new String[] { "" + llr.getLatMax(),
                    "" + llr.getLonMin(), "" + llr.getLatMin(),
                    "" + llr.getLonMax(), });
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

        addPublishWidget(request, entry, sb,
                         msg("Select a folder to publish the subset to"));
        sb.append(HtmlUtil.formTableClose());
        sb.append("<hr>");
        sb.append(msgLabel("Select Variables"));
        sb.append("<ul>");
        sb.append("<table>");
        sb.append(varSB);
        sb.append("</table>");
        sb.append("</ul>");
        sb.append(HtmlUtil.br());
        sb.append(HtmlUtil.submit(msg("Subset Grid")));
        sb.append(HtmlUtil.formClose());
        returnGridDataset(path, dataset);
        //        gridPool.put(path, dataset);
        return makeLinksResult(request, msg("Grid Subset"), sb,
                               new State(entry));
    }


    /**
     * Sort the grids
     *
     * @param dataset  the grid dataset
     *
     * @return  the grids
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
     * Get the PointFeatureIterator
     *
     * @param input  the dataset
     *
     * @return  the iterator
     *
     * @throws Exception  problem getting the iterator
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
     * Output a point map
     *
     * @param request  the request
     * @param entry  the entry
     *
     * @return  the Result
     *
     * @throws Exception  on badness
     */
    public Result outputPointMap(Request request, Entry entry)
            throws Exception {


        MapInfo map = getRepository().getMapManager().createMap(request,
                          false);
        String               path           = getPath(request, entry);
        FeatureDatasetPoint  pod            = pointPool.get(path);

        StringBuffer         sb             = new StringBuffer();
        List                 vars           = pod.getDataVariables();
        int                  skip           = request.get(ARG_SKIP, 0);
        int                  max            = request.get(ARG_MAX, 200);

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
            StringBuffer  info       = new StringBuffer("");
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
            map.addMarker("",
                          new LatLonPointImpl(el.getLatitude(),
                              el.getLongitude()), icon, info.toString());
        }



        List columnDefs  = new ArrayList();
        List columnNames = new ArrayList();
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
        map.center();
        sb.append(map.getHtml());
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
     * Output a trajectory map
     *
     * @param request  the request
     * @param entry    the entry
     *
     * @return  the result
     *
     * @throws Exception  on badness
     */
    public Result outputTrajectoryMap(Request request, Entry entry)
            throws Exception {
        String               path = getPath(request, entry);
        TrajectoryObsDataset tod  = trajectoryPool.get(path);
        StringBuffer         sb   = new StringBuffer();

        MapInfo map = getRepository().getMapManager().createMap(request, 800,
                          600, false);
        List trajectories = tod.getTrajectories();
        //TODO: Use new openlayers map
        for (int i = 0; i < trajectories.size(); i++) {
            List allVariables = tod.getDataVariables();
            TrajectoryObsDatatype todt =
                (TrajectoryObsDatatype) trajectories.get(i);
            float[] lats = toFloatArray(todt.getLatitude(null));
            float[] lons = toFloatArray(todt.getLongitude(null));
            for (int ptIdx = 0; ptIdx < lats.length; ptIdx++) {
                if (ptIdx > 0) {
                    if (ptIdx == lats.length - 1) {
                        map.addMarker("", lats[ptIdx], lons[ptIdx], null,
                                      "End time:" + todt.getEndDate());
                    }
                    //#FF0000
                    map.addLine("", lats[ptIdx - 1], lons[ptIdx - 1],
                                lats[ptIdx], lons[ptIdx]);
                } else {
                    map.addMarker("", lats[ptIdx], lons[ptIdx], null,
                                  "Start time:" + todt.getEndDate());
                }

            }
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

        map.center();
        sb.append(map.getHtml());
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
     * Create a LatLonPoint string
     *
     * @param lat  the latitude
     * @param lon  the longitude
     *
     * @return  the string
     */
    public static String llp(double lat, double lon) {
        return "new LatLonPoint(" + lat + "," + lon + ")";
    }

    /**
     * _more_
     *
     * @param request _more_
     * @param entry _more_
     * @param suffix _more_
     *
     * @return _more_
     */
    private Result makePointSubsetForm(Request request, Entry entry,
                                       String suffix) {
        StringBuffer sb      = new StringBuffer();
        String       formUrl = request.url(getRepository().URL_ENTRY_SHOW);
        sb.append(HtmlUtil.form(formUrl + suffix));
        sb.append(HtmlUtil.submit("Subset Point Data", ARG_SUBMIT));
        sb.append(HtmlUtil.hidden(ARG_OUTPUT,
                                  request.getString(ARG_OUTPUT, "")));
        sb.append(HtmlUtil.hidden(ARG_ENTRYID, entry.getId()));
        sb.append(HtmlUtil.formTable());
        List<TwoFacedObject> formats = new ArrayList<TwoFacedObject>();
        formats.add(new TwoFacedObject("CSV", FORMAT_CSV));
        formats.add(new TwoFacedObject("KML", FORMAT_KML));
        String format = request.getString(ARG_FORMAT, FORMAT_CSV);
        sb.append(HtmlUtil.formEntry(msgLabel("Format"),
                                     HtmlUtil.select(ARG_FORMAT, formats,
                                         format)));

        MapInfo map = getRepository().getMapManager().createMap(request,
                          true);
        map.addBox(entry, new MapProperties("blue", false, true));
        map.centerOn(entry);
        String llb = map.makeSelector(ARG_POINT_BBOX, true, null);
        sb.append(HtmlUtil.formEntryTop(msgLabel("Location"), llb));


        sb.append(HtmlUtil.formTableClose());
        sb.append(HtmlUtil.submit("Subset Point Data", ARG_SUBMIT));
        sb.append(HtmlUtil.formClose());
        return new Result("", sb);
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
    public Result outputPointSubset(Request request, Entry entry)
            throws Exception {
        if ( !request.defined(ARG_FORMAT)) {
            return makePointSubsetForm(request, entry, "");
        }
        String format = request.getString(ARG_FORMAT, FORMAT_CSV);

        if (format.equals(FORMAT_CSV)) {
            request.getHttpServletResponse().setContentType("text/csv");
            request.setReturnFilename(IOUtil.stripExtension(entry.getName())
                                      + ".csv");
        } else {
            request.getHttpServletResponse().setContentType(
                "application/vnd.google-earth.kml+xml");
            request.setReturnFilename(IOUtil.stripExtension(entry.getName())
                                      + ".kml");
        }


        OutputStream os = request.getHttpServletResponse().getOutputStream();
        PrintWriter  pw = new PrintWriter(os);

        if (format.equals(FORMAT_CSV)) {
            outputPointCsv(request, entry, pw);
        } else {
            outputPointKml(request, entry, pw);
        }

        pw.close();
        Result result = new Result();
        result.setNeedToWrite(false);
        return result;
    }


    /**
     * _more_
     *
     * @param request _more_
     * @param entry _more_
     * @param pw _more_
     *
     *
     * @throws Exception _more_
     */
    private void outputPointCsv(Request request, Entry entry, PrintWriter pw)
            throws Exception {
        String               path         = getPath(request, entry);
        FeatureDatasetPoint  pod          = pointPool.get(path);
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
                pw.print(HtmlUtil.quote("Time"));
                pw.print(",");
                pw.print(HtmlUtil.quote("Latitude"));
                pw.print(",");
                pw.print(HtmlUtil.quote("Longitude"));
                for (VariableSimpleIF var : (List<VariableSimpleIF>) vars) {
                    pw.print(",");
                    String unit = var.getUnitsString();
                    if (unit != null) {
                        pw.print(HtmlUtil.quote(var.getShortName() + " ("
                                + unit + ")"));
                    } else {
                        pw.print(HtmlUtil.quote(var.getShortName()));
                    }
                }
                pw.print("\n");
            }

            pw.print(HtmlUtil.quote("" + po.getNominalTimeAsDate()));
            pw.print(",");
            pw.print(el.getLatitude());
            pw.print(",");
            pw.print(el.getLongitude());

            for (VariableSimpleIF var : (List<VariableSimpleIF>) vars) {
                StructureMembers.Member member =
                    structure.findMember(var.getShortName());
                pw.print(",");
                if ((var.getDataType() == DataType.STRING)
                        || (var.getDataType() == DataType.CHAR)) {
                    pw.print(
                        HtmlUtil.quote(structure.getScalarString(member)));
                } else {
                    pw.print(structure.convertScalarFloat(member));
                }
            }
            pw.print("\n");
        }
        pointPool.put(path, pod);

    }


    /**
     * _more_
     *
     * @param request _more_
     * @param entry _more_
     * @param pw _more_
     *
     *
     * @throws Exception _more_
     */
    private void outputPointKml(Request request, Entry entry, PrintWriter pw)
            throws Exception {
        String               path         = getPath(request, entry);
        FeatureDatasetPoint  pod          = pointPool.get(path);
        List                 vars         = pod.getDataVariables();
        PointFeatureIterator dataIterator = getPointIterator(pod);

        Element              root         = KmlUtil.kml(entry.getName());
        Element              docNode = KmlUtil.document(root,
                                           entry.getName());

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
        pw.print(XmlUtil.toString(root));
        pointPool.put(path, pod);
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
     * _more_
     *
     * @param request _more_
     * @param outputType _more_
     * @param group _more_
     * @param subGroups _more_
     * @param entries _more_
     *
     * @return _more_
     *
     * @throws Exception _more_
     */
    public Result outputGroup(Request request, OutputType outputType,
                              Entry group, List<Entry> subGroups,
                              List<Entry> entries)
            throws Exception {
        if (group.getType().equals(
                GridAggregationTypeHandler.TYPE_GRIDAGGREGATION)) {
            return outputEntry(request, outputType, group);
        }
        //        System.err.println("group:" + group + " " + group.getType());
        return super.outputGroup(request, outputType, group, subGroups,
                                 entries);
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

        if (outputType.equals(OUTPUT_POINT_SUBSET)) {
            return outputPointSubset(request, entry);
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
        return getPath(null, entry);
    }


    /**
     * Get the path for the Entry
     *
     *
     * @param request _more_
     * @param entry  the Entry
     *
     * @return   the path
     *
     * @throws Exception problem getting the path
     */
    public String getPath(Request request, Entry entry) throws Exception {
        String location;
        if (entry.getType().equals(OpendapLinkTypeHandler.TYPE_OPENDAPLINK)) {
            Resource resource = entry.getResource();
            location = resource.getPath();
            String ext = IOUtil.getFileExtension(location).toLowerCase();
            if (ext.equals(".html") || ext.equals(".das")
                    || ext.equals(".dds")) {
                location = IOUtil.stripExtension(location);
            }
        } else if (entry.getType().equals(
                GridAggregationTypeHandler.TYPE_GRIDAGGREGATION)) {
            GridAggregationTypeHandler gridAggregation =
                (GridAggregationTypeHandler) entry.getTypeHandler();
            location = gridAggregation.getNcmlFile(request, entry).toString();
            // Something must be fixed to check if its empty
        } else {
            location = getStorageManager().getFastResourcePath(entry);
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
            String fileAttachment = metadata.getAttr1();
            if (fileAttachment.endsWith(SUFFIX_NCML)
                    || fileAttachment.endsWith(SUFFIX_CTL)) {
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
                                  + metadata.getId() + SUFFIX_NCML;
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


        String     location = getPath(request, entry);
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
         * Make the dataset
         *
         * @param preq preq
         *
         * @return The dataset
         *
         * @throws DAP2Exception On badness
         * @throws IOException On badness
         */
        protected GuardedDataset getDataset(ReqState preq)
                throws DAP2Exception, IOException /*, ParseException*/ {
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


    /**
     * _more_
     *
     * @param args _more_
     *
     * @throws Exception _more_
     */
    public static void main(String[] args) throws Exception {
        Repository repository = new Repository(new String[] {}, 8080);
        repository.initProperties(null);

        DataOutputHandler dop = new DataOutputHandler(repository, "test");
        String[] types = { TYPE_CDM, TYPE_GRID, TYPE_TRAJECTORY, TYPE_POINT };
        for (String f : args) {
            System.err.println("file:" + f);
            for (String type : types) {
                boolean ok      = dop.hasSuffixForType(f, type, false);
                boolean exclude = dop.hasSuffixForType(f, type, true);
                if ( !ok && !exclude) {
                    System.err.println("\t" + type + ": " + "unknown");
                } else {
                    System.err.println("\t" + type + ": " + "ok? " + ok
                                       + " exclude:" + exclude);
                }
            }
        }
    }



    /**
     * Output the timeseries image
     *
     * @param request the request
     * @param entry  the entry
     * @param f  the file
     *
     * @return  the image
     *
     * @throws Exception  problem creating image
     */
    private Result outputTimeSeriesImage(Request request, Entry entry, File f)
            throws Exception {

        StringBuffer sb = new StringBuffer();
        //sb.append(getHeader(request, entry));
        sb.append(header(msg("Chart")));

        TimeSeriesCollection dummy  = new TimeSeriesCollection();
        JFreeChart chart = createChart(request, entry, dummy);
        XYPlot               xyPlot = (XYPlot) chart.getPlot();

        Hashtable<String, MyTimeSeries> seriesMap = new Hashtable<String,
                                                        MyTimeSeries>();
        List<MyTimeSeries> allSeries = new ArrayList<MyTimeSeries>();
        int     paramCount = 0;
        int     colorCount = 0;
        boolean axisLeft   = true;
        Hashtable<String, List<ValueAxis>> axisMap = new Hashtable<String,
                                                         List<ValueAxis>>();
        Hashtable<String, double[]> rangeMap = new Hashtable<String,
                                                   double[]>();
        List<String> units      = new ArrayList<String>();
        List<String> paramUnits = new ArrayList<String>();
        List<String> paramNames = new ArrayList<String>();

        long         t1         = System.currentTimeMillis();
        String contents =
            IOUtil.readContents(getStorageManager().getFileInputStream(f));
        List<String> lines      = StringUtil.split(contents, "\n", true,
                                      true);
        String       header     = lines.get(0);
        String[]     headerToks = header.split(",");
        for (int i = 0; i < headerToks.length; i++) {
            paramNames.add(getParamName(headerToks[i]));
            paramUnits.add(getUnitFromName(headerToks[i]));
        }
        boolean hasLevel   = paramNames.get(3).equals("vertCoord");

        boolean readHeader = false;
        for (String line : lines) {
            if ( !readHeader) {
                readHeader = true;
                continue;
            }
            String[] lineTokes = line.split(",");
            Date     date      = DateUtil.parse(lineTokes[0]);
            int      startIdx  = hasLevel
                                 ? 4
                                 : 3;
            for (int i = startIdx; i < lineTokes.length; i++) {
                double value = Double.parseDouble(lineTokes[i]);
                if (value != value) {
                    continue;
                }
                List<ValueAxis> axises     = null;
                double[]        range      = null;
                String          u          = paramUnits.get(i);
                String          paramName  = paramNames.get(i);
                String          formatName = paramName.replaceAll("_", " ");
                String formatUnit = ((u == null) || (u.length() == 0))
                                    ? ""
                                    : "[" + u + "]";
                if (u != null) {
                    axises = axisMap.get(u);
                    range  = rangeMap.get(u);
                    if (axises == null) {
                        axises = new ArrayList<ValueAxis>();
                        range  = new double[] { value, value };
                        rangeMap.put(u, range);
                        axisMap.put(u, axises);
                        units.add(u);
                    }
                    range[0] = Math.min(range[0], value);
                    range[1] = Math.max(range[1], value);
                }
                MyTimeSeries series = seriesMap.get(paramName);
                if (series == null) {
                    paramCount++;
                    TimeSeriesCollection dataset = new TimeSeriesCollection();
                    series = new MyTimeSeries(formatName,
                            FixedMillisecond.class);
                    allSeries.add(series);
                    ValueAxis rangeAxis = new NumberAxis(formatName + " "
                                              + formatUnit);
                    if (axises != null) {
                        axises.add(rangeAxis);
                    }
                    XYItemRenderer renderer =
                        new XYAreaRenderer(XYAreaRenderer.LINES);
                    if (colorCount >= GuiUtils.COLORS.length) {
                        colorCount = 0;
                    }
                    renderer.setSeriesPaint(0, GuiUtils.COLORS[colorCount]);
                    colorCount++;
                    xyPlot.setRenderer(paramCount, renderer);
                    xyPlot.setRangeAxis(paramCount, rangeAxis, false);
                    AxisLocation side = (axisLeft
                                         ? AxisLocation.TOP_OR_LEFT
                                         : AxisLocation.BOTTOM_OR_RIGHT);
                    axisLeft = !axisLeft;
                    xyPlot.setRangeAxisLocation(paramCount, side);

                    dataset.setDomainIsPointsInTime(true);
                    dataset.addSeries(series);
                    seriesMap.put(paramNames.get(i), series);
                    xyPlot.setDataset(paramCount, dataset);
                    xyPlot.mapDatasetToRangeAxis(paramCount, paramCount);
                }
                //series.addOrUpdate(new FixedMillisecond(pointData.date),value);
                TimeSeriesDataItem item =
                    new TimeSeriesDataItem(new FixedMillisecond(date), value);
                series.addItem(item);
            }
        }



        for (MyTimeSeries timeSeries : allSeries) {
            timeSeries.finish();
        }

        for (String unit : units) {
            List<ValueAxis> axises = axisMap.get(unit);
            double[]        range  = rangeMap.get(unit);
            for (ValueAxis rangeAxis : axises) {
                rangeAxis.setRange(new org.jfree.data.Range(range[0],
                        range[1]));
            }
        }


        long t2 = System.currentTimeMillis();

        BufferedImage newImage =
            chart.createBufferedImage(request.get(ARG_IMAGE_WIDTH, 1000),
                                      request.get(ARG_IMAGE_HEIGHT, 400));
        long t3 = System.currentTimeMillis();
        //System.err.println("timeseries image time:" + (t2 - t1) + " "
        //                   + (t3 - t2));

        File file = getStorageManager().getTmpFile(request, "point.png");
        ImageUtils.writeImageToFile(newImage, file);
        InputStream is     = getStorageManager().getFileInputStream(file);
        Result      result = new Result("", is, "image/png");
        return result;

    }

    /**
     * get the parameter name from the raw name
     *
     * @param rawname the raw name
     *
     * @return  the parameter name
     */
    public String getParamName(String rawname) {
        String name  = rawname;
        int    index = rawname.indexOf("[unit=");
        if (index >= 0) {
            name = rawname.substring(0, index);
        }
        return name;
    }

    /**
     * Get the parameter unit from the raw name
     *
     * @param rawname  the raw name
     *
     * @return  the unit or null
     */
    private String getUnitFromName(String rawname) {
        String unit  = null;
        int    index = rawname.indexOf("[unit=");
        if (index >= 0) {
            unit = rawname.substring(index + 6, rawname.indexOf("]"));
            unit = unit.replaceAll("\"", "");
        }
        return unit;
    }


    /**
     * A wrapper for TimeSeries
     *
     * @author RAMADDA Development Team
     */
    private static class MyTimeSeries extends TimeSeries {

        /** _more_ */
        List<TimeSeriesDataItem> items = new ArrayList<TimeSeriesDataItem>();

        /** _more_ */
        HashSet<TimeSeriesDataItem> seen = new HashSet<TimeSeriesDataItem>();

        /**
         * _more_
         *
         * @param name _more_
         * @param c _more_
         */
        public MyTimeSeries(String name, Class c) {
            super(name, c);
        }

        /**
         * _more_
         *
         * @param item _more_
         */
        public void addItem(TimeSeriesDataItem item) {
            if (seen.contains(item)) {
                return;
            }
            seen.add(item);
            items.add(item);
        }

        /**
         * _more_
         */
        public void finish() {
            items = new ArrayList<TimeSeriesDataItem>(Misc.sort(items));

            for (TimeSeriesDataItem item : items) {
                this.data.add(item);
            }
            fireSeriesChanged();
        }


    }




    /**
     * Create the chart
     *
     *
     * @param request  the request
     * @param entry    the entry
     * @param dataset  the dataset
     *
     * @return _more_
     */
    private static JFreeChart createChart(Request request, Entry entry,
                                          XYDataset dataset) {
        LatLonPointImpl llp =
            new LatLonPointImpl(request.getLatOrLonValue(ARG_LOCATION
                + ".latitude", 0), request.getLatOrLonValue(ARG_LOCATION
                               + ".longitude", 0));
        String     title = entry.getName() + " at " + llp.toString();

        JFreeChart chart = ChartFactory.createTimeSeriesChart(
        //entry.getName(),  // title
        title,    // title
        "Date",   // x-axis label
        "",       // y-axis label
        dataset,  // data
        true,     // create legend?
        true,     // generate tooltips?
        false     // generate URLs?
            );

        chart.setBackgroundPaint(Color.white);
        ValueAxis rangeAxis = new NumberAxis("");
        rangeAxis.setVisible(false);
        XYPlot plot = (XYPlot) chart.getPlot();
        if (request.get("gray", false)) {
            plot.setBackgroundPaint(Color.lightGray);
            plot.setDomainGridlinePaint(Color.white);
            plot.setRangeGridlinePaint(Color.white);
        } else {
            plot.setBackgroundPaint(Color.white);
            plot.setDomainGridlinePaint(Color.lightGray);
            plot.setRangeGridlinePaint(Color.lightGray);
        }
        plot.setAxisOffset(new RectangleInsets(5.0, 5.0, 5.0, 5.0));
        plot.setDomainCrosshairVisible(true);
        plot.setRangeCrosshairVisible(true);
        plot.setRangeAxis(0, rangeAxis, false);


        XYItemRenderer r    = plot.getRenderer();
        DateAxis       axis = (DateAxis) plot.getDomainAxis();
        //axis.setDateFormatOverride(new SimpleDateFormat("MMM-yyyy"));

        return chart;

    }


}
