/*
 * Copyright (c) 2008-2015 Geode Systems LLC
 * This Software is licensed under the Geode Systems RAMADDA License available in the source distribution in the file 
 * ramadda_license.txt. The above copyright notice shall be included in all copies or substantial portions of the Software.
 */

package org.ramadda.geodata.model;


import org.ramadda.data.services.NoaaPsdMonthlyClimateIndexTypeHandler;
import org.ramadda.geodata.cdmdata.CdmDataOutputHandler;
import org.ramadda.geodata.cdmdata.GridTypeHandler;
import org.ramadda.repository.Constants;
import org.ramadda.repository.Entry;
import org.ramadda.repository.Repository;
import org.ramadda.repository.RepositoryManager;
import org.ramadda.repository.Request;
import org.ramadda.repository.Resource;
import org.ramadda.repository.job.JobManager;
import org.ramadda.repository.type.TypeHandler;

import org.ramadda.service.Service;
import org.ramadda.service.ServiceInput;
import org.ramadda.service.ServiceOperand;
import org.ramadda.service.ServiceOutput;
import org.ramadda.util.GeoUtils;
import org.ramadda.util.HtmlUtils;

import ucar.nc2.dt.GridDatatype;
import ucar.nc2.dt.grid.GridDataset;
import ucar.nc2.units.SimpleUnit;

import ucar.unidata.geoloc.LatLonRect;
import ucar.unidata.util.IOUtil;
import ucar.unidata.util.Misc;
import ucar.unidata.util.StringUtil;
import ucar.unidata.util.TwoFacedObject;


import java.io.File;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.List;
import java.util.Map;
import java.util.SortedSet;
import java.util.TreeSet;


/**
 * Map plotting process using NCL
 */
public class NCLModelPlotDataProcess extends Service {

    /** The nclOutputHandler */
    NCLOutputHandler nclOutputHandler;

    /** prefix for ncl commands */
    public final static String ARG_NCL_PREFIX = "ncl.";

    /** output type */
    public final static String ARG_NCL_OUTPUT = ARG_NCL_PREFIX + "output";

    /** mask type */
    public final static String ARG_NCL_MASKTYPE = ARG_NCL_PREFIX + "masktype";

    /** units arg */
    public final static String ARG_NCL_UNITS = ARG_NCL_PREFIX + "units";

    /** contour interval argument */
    private static final String ARG_NCL_CINT = ARG_NCL_PREFIX + "cint";

    /** contour minimum argument */
    private static final String ARG_NCL_CMIN = ARG_NCL_PREFIX + "cmin";

    /** contour maximum argument */
    private static final String ARG_NCL_CMAX = ARG_NCL_PREFIX + "cmax";

    /** colormap name */
    private static final String ARG_NCL_COLORMAP = ARG_NCL_PREFIX
                                                   + "colormap";

    /** contour lines */
    private static final String ARG_NCL_CLINES = ARG_NCL_PREFIX
                                                 + "contourlines";

    /** contour labels */
    private static final String ARG_NCL_CLABELS = ARG_NCL_PREFIX
                                                  + "contourlabels";

    /** colorfill */
    private static final String ARG_NCL_CFILL = ARG_NCL_PREFIX
                                                + "contourfill";

    /** list of colormaps          */
    private List colormaps = null;

    /**
     * Create a new map process
     *
     * @param repository  the repository
     *
     * @throws Exception  badness
     */
    public NCLModelPlotDataProcess(Repository repository) throws Exception {
        this(repository, "NCLPlot", "Plot Options");
    }

    /**
     * Create a new map process
     *
     * @param repository  the repository
     * @param id  an id for this process
     * @param label  a label for this process
     *
     * @throws Exception  problem creating process
     */
    public NCLModelPlotDataProcess(Repository repository, String id,
                                   String label)
            throws Exception {
        super(repository, id, label);
        nclOutputHandler = new NCLOutputHandler(repository);
    }

    /**
     * Init the javascript for the form
     *
     * @param request  the request
     * @param js  the JavaScript form
     * @param formVar the form id
     *
     * @throws Exception problems
     */
    public void initFormJS(Request request, Appendable js, String formVar)
            throws Exception {
        js.append(formVar + ".addService(new NCLModelPlotService());\n");
    }


    /**
     * Add this process to the form
     *
     * @param request  the Request
     * @param input    the process input
     * @param sb       the form
     * @param argPrefix arg prefix
     * @param label     label
     *
     *
     * @throws Exception  problem getting the information for the form
     */
    @Override
    public void addToForm(Request request, ServiceInput input, Appendable sb,
                          String argPrefix, String label)
            throws Exception {

        String type =
            input.getProperty(
                "type", ClimateModelApiHandler.ARG_ACTION_COMPARE).toString();
        boolean handleMultiple =
            type.equals(ClimateModelApiHandler.ARG_ACTION_MULTI_COMPARE)
            || type.equals(ClimateModelApiHandler.ARG_ACTION_ENS_COMPARE);
        boolean isCorrelation =
            type.equals(ClimateModelApiHandler.ARG_ACTION_CORRELATION);
        sb.append(HtmlUtils.formTable());
        //List<Entry> entries = input.getEntries();
        Entry first = getFirstGridEntry(input);
        /*
        for (Entry entry : entries) {
            TypeHandler mytype = entry.getTypeHandler();
            if (mytype instanceof ClimateModelFileTypeHandler ||
                mytype instanceof GridTypeHandler) {
                first = entry;
                break;
            }
        }
        */

        String units = "";

        CdmDataOutputHandler dataOutputHandler =
            nclOutputHandler.getDataOutputHandler();
        GridDataset dataset =
            dataOutputHandler.getCdmManager().getGridDataset(first,
                first.getResource().getPath());
        if (dataset != null) {
            List<GridDatatype> grids = dataset.getGrids();
            GridDatatype       grid  = grids.get(0);
            units = grid.getUnitsString();
        }

        String space1 = HtmlUtils.space(1);
        String space2 = HtmlUtils.space(1);

        if ((input.getOperands().size() > 1) && !handleMultiple
                && !isCorrelation) {
            StringBuilder buttons = new StringBuilder();
            buttons.append(
                HtmlUtils.radio(
                    ARG_NCL_OUTPUT, "diff",
                    RepositoryManager.getShouldButtonBeSelected(
                        request, ARG_NCL_OUTPUT, "diff", true)));
            buttons.append(space1);
            buttons.append(Repository.msg("Difference"));
            buttons.append(space2);
            buttons.append(
                HtmlUtils.radio(
                    ARG_NCL_OUTPUT, "comp",
                    RepositoryManager.getShouldButtonBeSelected(
                        request, ARG_NCL_OUTPUT, "comp", false)));
            buttons.append(space1);
            buttons.append(Repository.msg("Separate Plots"));

            sb.append(HtmlUtils.formEntry(Repository.msgLabel("Plot As"),
                                          buttons.toString()));
        } else if (isCorrelation) {
            StringBuilder buttons = new StringBuilder();
            buttons.append(
                HtmlUtils.radio(
                    ARG_NCL_OUTPUT, "correlation",
                    RepositoryManager.getShouldButtonBeSelected(
                        request, ARG_NCL_OUTPUT, "correlation", true)));
            buttons.append(space1);
            buttons.append(Repository.msg("Correlation"));
            buttons.append(space2);
            buttons.append(
                HtmlUtils.radio(
                    ARG_NCL_OUTPUT, "regression",
                    RepositoryManager.getShouldButtonBeSelected(
                        request, ARG_NCL_OUTPUT, "regression", false)));
            buttons.append(space1);
            buttons.append(Repository.msg("Regression"));

            sb.append(HtmlUtils.formEntry(Repository.msgLabel("Plot As"),
                                          buttons.toString()));
        } else {
            sb.append(HtmlUtils.hidden(ARG_NCL_OUTPUT, "comp"));
        }
        if ( !isCorrelation) {
            StringBuilder plotTypes = new StringBuilder();
            plotTypes.append(
                HtmlUtils.radio(
                    NCLOutputHandler.ARG_NCL_PLOTTYPE, "png",
                    RepositoryManager.getShouldButtonBeSelected(
                        request, NCLOutputHandler.ARG_NCL_PLOTTYPE, "png",
                        true)));
            plotTypes.append(space1);
            plotTypes.append(Repository.msg("Map (Image)"));
            plotTypes.append(space2);
            plotTypes.append(
                HtmlUtils.radio(
                    NCLOutputHandler.ARG_NCL_PLOTTYPE, "kmz",
                    RepositoryManager.getShouldButtonBeSelected(
                        request, NCLOutputHandler.ARG_NCL_PLOTTYPE, "kmz",
                        false)));
            plotTypes.append(space1);
            plotTypes.append(Repository.msg("Google Earth"));


            sb.append(HtmlUtils.formEntry(Repository.msgLabel("Plot Type"),
                                          plotTypes.toString()));

            // units
            if (SimpleUnit.isCompatible(units, "K")) {
                StringBuilder unitsSB = new StringBuilder();
                unitsSB.append(HtmlUtils.radio(ARG_NCL_UNITS, "K",
                        RepositoryManager.getShouldButtonBeSelected(request,
                            ARG_NCL_UNITS, "K", true)));
                unitsSB.append(space1);
                unitsSB.append(Repository.msg("Kelvin"));
                unitsSB.append(space2);
                unitsSB.append(HtmlUtils.radio(ARG_NCL_UNITS, "degC",
                        RepositoryManager.getShouldButtonBeSelected(request,
                            ARG_NCL_UNITS, "degC", false)));
                unitsSB.append(space1);
                unitsSB.append(Repository.msg("Celsius"));


                sb.append(
                    HtmlUtils.formEntry(
                        Repository.msgLabel("Plot Units"),
                        unitsSB.toString()));
            } else if (SimpleUnit.isCompatible(units, "kg m-2 s-1")
                       || SimpleUnit.isCompatible(units, "mm/day")) {
                sb.append(HtmlUtils.hidden(ARG_NCL_UNITS, "mm/day"));
            } else if (SimpleUnit.isCompatible(units, "kg m-1 s-2")
                       || SimpleUnit.isCompatible(units, "Pa")) {
                sb.append(HtmlUtils.hidden(ARG_NCL_UNITS, "hPa"));
            } else if (SimpleUnit.isCompatible(units, "kg m-2")) {
                sb.append(HtmlUtils.hidden(ARG_NCL_UNITS, "mm"));
            }
        }
        // Mask buttons
        StringBuilder mbuttons = new StringBuilder();
        mbuttons.append(
            HtmlUtils.radio(
                ARG_NCL_MASKTYPE, "none",
                RepositoryManager.getShouldButtonBeSelected(
                    request, ARG_NCL_MASKTYPE, "none", true)));
        mbuttons.append(space1);
        mbuttons.append(Repository.msg("None"));
        mbuttons.append(space2);
        mbuttons.append(
            HtmlUtils.radio(
                ARG_NCL_MASKTYPE, "ocean",
                RepositoryManager.getShouldButtonBeSelected(
                    request, ARG_NCL_MASKTYPE, "ocean", false)));
        mbuttons.append(space1);
        mbuttons.append(Repository.msg("Ocean"));
        mbuttons.append(space2);
        mbuttons.append(
            HtmlUtils.radio(
                ARG_NCL_MASKTYPE, "land",
                RepositoryManager.getShouldButtonBeSelected(
                    request, ARG_NCL_MASKTYPE, "land", false)));
        mbuttons.append(space1);
        mbuttons.append(Repository.msg("Land"));

        sb.append(HtmlUtils.formEntry(Repository.msgLabel("Data Mask"),
                                      mbuttons.toString()));

        // TODO:  For now, don't get value from request.  May not
        // be valid if variable changes.
        // Contour options
        StringBuilder contourOpts = new StringBuilder();
        contourOpts.append(HtmlUtils.labeledCheckbox(ARG_NCL_CFILL, "true",
                request.get(ARG_NCL_CFILL, true), "Color-fill"));
        contourOpts.append(HtmlUtils.space(3));
        contourOpts.append(HtmlUtils.labeledCheckbox(ARG_NCL_CLINES, "false",
                request.get(ARG_NCL_CLINES, false), "Lines"));
        contourOpts.append(HtmlUtils.space(3));
        contourOpts.append(HtmlUtils.labeledCheckbox(ARG_NCL_CLABELS, "true",
                request.get(ARG_NCL_CLABELS, false), "Labels"));
        sb.append(HtmlUtils.formEntry(Repository.msgLabel("Contours"),
                                      contourOpts.toString()));
        // Contour interval
        StringBuilder contourSB = new StringBuilder();
        contourSB.append(Repository.msg("Interval: "));
        contourSB.append(HtmlUtils.makeLatLonInput(ARG_NCL_CINT,
                ARG_NCL_CINT, ""));
        //request.getString(ARG_NCL_CINT, "")));
        contourSB.append("<br>");
        contourSB.append(Repository.msg("Range: Low"));
        contourSB.append(HtmlUtils.makeLatLonInput(ARG_NCL_CMIN,
                ARG_NCL_CMIN, ""));
        //request.getString(ARG_NCL_CMIN, "")));
        contourSB.append(Repository.msg("High"));
        contourSB.append(HtmlUtils.makeLatLonInput(ARG_NCL_CMAX,
                ARG_NCL_CMAX, ""));
        //request.getString(ARG_NCL_CMAX, "")));
        sb.append(
            HtmlUtils.formEntry(
                "<div style=\"width:9em\">"
                + Repository.msgLabel("Override Contour Defaults")
                + "</div>", contourSB.toString()));
        // colormaps
        List cmaps = getColorMaps();
        sb.append(
            HtmlUtils.formEntry(
                msgLabel("Colormap"),
                HtmlUtils.select(
                    ARG_NCL_COLORMAP, cmaps,
                    request.getString(ARG_NCL_COLORMAP, "default"),
                    HtmlUtils.cssClass("select_widget"))));

        sb.append(HtmlUtils.formTableClose());

    }

    /**
     * Get the list of color maps
     *
     * @return  list
     *
     * @throws Exception problems
     */
    public List getColorMaps() throws Exception {
        if (colormaps == null) {
            colormaps = new ArrayList<TwoFacedObject>();
            colormaps.add(new TwoFacedObject("Default", "default"));
            String list =
                getRepository().getResource(
                    "/org/ramadda/geodata/model/resources/ncl/colormaps.txt");
            List<String> cmaps = StringUtil.split(list, "\n", true, true);
            for (String cmap : cmaps) {
                List<String> toks = StringUtil.split(cmap);
                colormaps.add(new HtmlUtils.Selector(toks.get(1),
                        toks.get(0),
                        getRepository().getUrlBase() + "/model/images/"
                        + toks.get(2), 3, 130, false));
            }
        }

        return colormaps;
    }

    /**
     * Get the first grid entry
     *
     * @param input the operands
     *
     * @return  the first grid
     */
    private Entry getFirstGridEntry(ServiceInput input) {
        List<Entry> entries = input.getEntries();
        Entry       first   = null;
        for (Entry entry : entries) {
            if (isGridEntry(entry)) {
                first = entry;

                break;
            }
        }

        return first;
    }

    /**
     * Is this a grid entry
     *
     * @param entry  the entry
     *
     * @return  true if it's a grid
     */
    private boolean isGridEntry(Entry entry) {
        TypeHandler mytype = entry.getTypeHandler();

        return (mytype instanceof ClimateModelFileTypeHandler)
               || (mytype instanceof GridTypeHandler);
    }

    /**
     * Process the request
     *
     * @param request  the request
     * @param input    the ServiceInput
     * @param argPrefix arg prefix
     *
     * @return  the output
     *
     * @throws Exception  problems generating the output
     */
    @Override
    public ServiceOutput evaluate(Request request, ServiceInput input,
                                  String argPrefix)
            throws Exception {

        List<Entry>          outputEntries = new ArrayList<Entry>();
        List<ServiceOperand> ops           = input.getOperands();
        StringBuilder        fileList      = new StringBuilder();
        StringBuilder        nameList      = new StringBuilder();
        StringBuilder        modelList     = new StringBuilder();
        StringBuilder        ensList       = new StringBuilder();
        StringBuilder        expList       = new StringBuilder();
        Entry                inputEntry    = getFirstGridEntry(input);
        boolean              haveOne       = false;
        boolean              haveGrid      = false;
        for (ServiceOperand op : ops) {

            List<Entry> opEntries = op.getEntries();
            for (Entry entry : opEntries) {
                if (haveOne) {
                    fileList.append(",");
                    nameList.append(";");
                }
                if (haveGrid) {
                    modelList.append(";");
                    expList.append(";");
                    ensList.append(";");
                }
                //fileList.append("\"");
                fileList.append(entry.getResource().toString());
                nameList.append(op.getDescription());
                if (isGridEntry(entry)) {
                    modelList.append(entry.getValue(1));
                    expList.append(entry.getValue(2));
                    ensList.append(entry.getValue(3));
                    haveGrid = true;
                }
                //fileList.append("\"");
                haveOne = true;
            }
        }

        String wksName = getRepository().getGUID();
        String plotType =
            request.getString(NCLOutputHandler.ARG_NCL_PLOTTYPE, "png");
        if (plotType.equals("image")) {
            plotType = "png";
        }
        String suffix = plotType;
        if (plotType.equals("timeseries") || plotType.equals("pdf")) {
            suffix = "png";
        }
        String outputType = request.getString(ARG_NCL_OUTPUT, "comp");
        String maskType   = request.getString(ARG_NCL_MASKTYPE, "none");
        File outFile = new File(IOUtil.joinDir(input.getProcessDir(),
                           wksName) + "." + suffix);
        CdmDataOutputHandler dataOutputHandler =
            nclOutputHandler.getDataOutputHandler();
        GridDataset dataset = dataOutputHandler.getCdmManager().createGrid(
                                  inputEntry.getResource().toString());
        if (dataset == null) {
            throw new Exception("Not a grid");
        }

        StringBuilder commandString = new StringBuilder();
        List<String>  commands      = new ArrayList<String>();
        String        ncargRoot     = nclOutputHandler.getNcargRootDir();
        commands.add(IOUtil.joinDir(ncargRoot, "bin/ncl"));
        commands.add(
            IOUtil.joinDir(
                IOUtil.joinDir(
                    nclOutputHandler.getStorageManager().getResourceDir(),
                    "ncl"), nclOutputHandler.SCRIPT_MAPPLOT));
        Map<String, String> envMap = new HashMap<String, String>();
        nclOutputHandler.addGlobalEnvVars(envMap);
        envMap.put("wks_name", wksName);
        envMap.put("ncfiles", fileList.toString());
        envMap.put("titles", nameList.toString());
        envMap.put("models", modelList.toString());
        envMap.put("exps", expList.toString());
        envMap.put("ens", ensList.toString());
        envMap.put("productdir", input.getProcessDir().toString());
        envMap.put("plot_type", plotType);
        envMap.put("output", outputType);
        envMap.put("mask", maskType);

        Hashtable    args     = request.getArgs();
        List<String> varNames = new ArrayList<String>();
        for (Enumeration keys = args.keys(); keys.hasMoreElements(); ) {
            String arg = (String) keys.nextElement();
            if (arg.startsWith(CdmDataOutputHandler.VAR_PREFIX)
                    && request.get(arg, false)) {
                varNames.add(
                    arg.substring(CdmDataOutputHandler.VAR_PREFIX.length()));
            }
        }
        String varname = request.getString(nclOutputHandler.ARG_NCL_VARIABLE,
                                           null);
        if (varname == null) {
            List<GridDatatype> grids = dataset.getGrids();
            GridDatatype       var   = grids.get(0);
            varname = var.getName();
        }
        envMap.put("variable", varname);
        String level = request.getString(CdmDataOutputHandler.ARG_LEVEL,
                                         null);
        if ((level != null) && !level.isEmpty()) {
            envMap.put(CdmDataOutputHandler.ARG_LEVEL, level);
        }
        LatLonRect llb = dataset.getBoundingBox();
        // Normalize longitude bounds to the data
        double origLonMin = llb.getLonMin();
        double lonMin = Double.parseDouble(
                            request.getString(
                                NCLOutputHandler.ARG_NCL_AREA_WEST,
                                String.valueOf(llb.getLonMin())));
        double lonMax = Double.parseDouble(
                            request.getString(
                                NCLOutputHandler.ARG_NCL_AREA_EAST,
                                String.valueOf(llb.getLonMax())));
        if (origLonMin < 0) {  // -180 to 180
            lonMin = GeoUtils.normalizeLongitude(lonMin);
            lonMax = GeoUtils.normalizeLongitude(lonMax);
        } else {               // 0-360
            lonMin = GeoUtils.normalizeLongitude360(lonMin);
            lonMax = GeoUtils.normalizeLongitude360(lonMax);
        }
        envMap.put("maxLat",
                   request.getString(NCLOutputHandler.ARG_NCL_AREA_NORTH,
                                     String.valueOf(llb.getLatMax())));
        envMap.put("minLat",
                   request.getString(NCLOutputHandler.ARG_NCL_AREA_SOUTH,
                                     String.valueOf(llb.getLatMin())));
        envMap.put("minLon", String.valueOf(lonMin));
        envMap.put("maxLon", String.valueOf(lonMax));

        boolean haveOriginalBounds = true;
        for (String spatialArg : NCLOutputHandler.NCL_SPATIALARGS) {
            if ( !Misc.equals(request.getString(spatialArg, ""),
                              request.getString(spatialArg + ".original",
                                  ""))) {
                haveOriginalBounds = false;

                break;
            }
        }
        envMap.put("addCyclic", Boolean.toString(haveOriginalBounds));
        String outUnits = request.getString(ARG_NCL_UNITS, null);
        if ((outUnits != null) && !outUnits.isEmpty()) {
            envMap.put("units", outUnits);
        }

        // contours
        double   cint  = request.get(ARG_NCL_CINT, 0.);
        double   cmin  = request.get(ARG_NCL_CMIN, 0.);
        double   cmax  = request.get(ARG_NCL_CMAX, 0.);
        double[] cvals = verifyContourInfo(cint, cmin, cmax);
        if (cint != 0.) {
            envMap.put("cint", String.valueOf(cvals[0]));
            envMap.put("cmin", String.valueOf(cvals[1]));
            envMap.put("cmax", String.valueOf(cvals[2]));
        }

        String mapid =
            request.getString(NCLOutputHandler.ARG_NCL_AREA_REGIONID,
                              "").toLowerCase().trim();
        boolean usepolar = mapid.startsWith("nh") || mapid.startsWith("sh")
                           || mapid.startsWith("ant");
        envMap.put("usepolar", Boolean.toString(usepolar));
        if (usepolar) {
            String center = "0";
            if ((mapid.startsWith("nh") || mapid.startsWith("sh"))
                    && (mapid.length() > 2)) {
                center = mapid.substring(2);
            } else if (mapid.startsWith("ant") && (mapid.length() > 3)) {
                center = mapid.substring(3);
            }
            //System.out.println("Map: "+mapid+", center: "+center);
            envMap.put("meridian", center);
        }

        boolean haveAnom = fileList.toString().indexOf("anom") >= 0;
        boolean isCorrelation = outputType.equals("correlation")
                                || outputType.equals("regression");
        String colormap = request.getString(ARG_NCL_COLORMAP, "default");
        if (colormap.equals("default")) {
            colormap = "rainbow";
            if (outputType.equals("diff") || haveAnom || isCorrelation) {
                colormap = "testcmap";
            }
        }
        envMap.put("colormap", colormap);
        envMap.put("clines",
                   Boolean.toString(request.get(ARG_NCL_CLINES, false)));
        envMap.put("clabels",
                   Boolean.toString(request.get(ARG_NCL_CLABELS, false)));
        envMap.put("cfill",
                   Boolean.toString(request.get(ARG_NCL_CFILL, true)));
        envMap.put("anom", Boolean.toString(haveAnom || isCorrelation));
        envMap.put(
            "annotation",
            getRepository().getProperty(Constants.PROP_REPOSITORY_NAME, ""));
        String logo = getRepository().getProperty(Constants.PROP_LOGO_IMAGE,
                          "");
        if ( !logo.isEmpty()) {
            if ( !logo.startsWith("http")) {
                if ( !logo.startsWith("/")) {
                    logo = "/" + logo;
                }
                logo = request.getAbsoluteUrl(logo);
            }
            envMap.put("logo", logo);
        }


        //System.err.println("cmds:" + commands);
        System.err.println("env:" + envMap);

        //Use new repository method to execute. This gets back [stdout,stderr]
        JobManager.CommandResults results =
            getRepository().getJobManager().executeCommand(commands, envMap,
                input.getProcessDir(), 60);
        String errorMsg = results.getStderrMsg();
        String outMsg   = results.getStdoutMsg();
        // Check the version
        /*
        if (suffix.equals("png")) {
            Matcher m = NCLOutputHandler.versionPattern.matcher(outMsg);
            if (m.find()) {
                String version = m.group(1);
                if (version.compareTo("6.0.0") < 0) {
                    String oldPath = outFile.toString();
                    outFile = new File(oldPath.replace(".png",
                            ".000001.png"));
                }
            }
        }
        */

        if ( !outFile.exists()) {
            if (outMsg.length() > 0) {
                throw new IllegalArgumentException(outMsg);
            }
            if (errorMsg.length() > 0) {
                throw new IllegalArgumentException(errorMsg);
            }
            if ( !outFile.exists()) {
                throw new IllegalArgumentException(
                    "Humm, the NCL image generation failed for some reason");
            }
        }
        String outType = "type_image";
        if (plotType.equals("kmz")) {
            outType = "geo_kml";
        }
        Resource resource = new Resource(outFile, Resource.TYPE_LOCAL_FILE);
        TypeHandler myHandler = getRepository().getTypeHandler(outType,
                                    false, true);
        Entry outputEntry = new Entry(myHandler, true, outFile.toString());
        outputEntry.setResource(resource);
        nclOutputHandler.getEntryManager().writeEntryXmlFile(request,
                outputEntry);
        outputEntries.add(outputEntry);
        ServiceOutput dpo = new ServiceOutput(new ServiceOperand("Plot of "
                                + nameList, outputEntries));

        return dpo;

    }

    /**
     * Verify the contour values
     *
     * @param cint  the contour interval
     * @param cmin  the minimum value
     * @param cmax  the maximum value
     *
     * @return the adjusted values
     */
    private double[] verifyContourInfo(double cint, double cmin,
                                       double cmax) {

        if (cint == 0) {
            cint = 0;
            cmin = 0;
            cmax = 0;
        } else if (cint < 0) {
            System.err.println(
                "contour interval must be greater than zero - using default values");
            cint = 0;
            cmin = 0;
            cmax = 0;
        } else if (cmin >= cmax) {
            System.err.println(
                "min must be less than max - using default values");
            cint = 0;
            cmin = 0;
            cmax = 0;
        } else {
            double diff = (cmax - cmin) * 1.0;
            double var  = diff / cint;
            if (var > 300.) {
                System.err.println(
                    "too many contour lines - using default values");
                cint = 0;
                cmin = 0;
                cmax = 0;
            }
        }

        return new double[] { cint, cmin, cmax };

    }

    /**
     * Can we handle this type of ServiceInput?
     *
     * @param dpi  the ServiceInput
     * @return true if we can handle
     */
    public boolean canHandle(ServiceInput dpi) {
        if ( !nclOutputHandler.isEnabled()) {
            return false;
        }

        //if (dpi.getOperands().size() > 2) {
        //    return false;
        //}

        for (ServiceOperand op : dpi.getOperands()) {
            if (checkForValidEntries(op.getEntries())) {
                continue;
            } else {
                return false;
            }
        }

        return true;
    }

    /**
     * Check for valid entries
     * @param entries  list of entries
     * @return
     */
    private boolean checkForValidEntries(List<Entry> entries) {
        // TODO: change this when we can handle more than one entry (e.g. daily data)
        if (entries.isEmpty()) {
            //if (entries.isEmpty() || (entries.size() > 1)) {
            return false;
        }
        SortedSet<String> uniqueModels =
            Collections.synchronizedSortedSet(new TreeSet<String>());
        SortedSet<String> uniqueMembers =
            Collections.synchronizedSortedSet(new TreeSet<String>());
        for (Entry entry : entries) {
            if ( !(entry.getTypeHandler()
                    instanceof ClimateModelFileTypeHandler)) {
                if (entry.getTypeHandler()
                        instanceof NoaaPsdMonthlyClimateIndexTypeHandler) {
                    continue;
                }

                return false;
            }
            uniqueModels.add(entry.getValue(1).toString());
            uniqueMembers.add(entry.getValue(3).toString());
        }
        // one model, one member
        if ((uniqueModels.size() == 1) && (uniqueMembers.size() == 1)) {
            return true;
        }
        // multi-model multi-ensemble - don't want to think about this
        if ((uniqueModels.size() >= 1) && (uniqueMembers.size() > 1)) {
            return false;
        }
        // single model, multi-ensemble - can't handle yet
        if ((uniqueModels.size() > 1) && (uniqueMembers.size() > 1)) {
            return true;
        }

        return true;
    }

    /**
     * Is this enabled?
     *
     * @return true if it is
     */
    public boolean isEnabled() {
        return nclOutputHandler.isEnabled();
    }

}
