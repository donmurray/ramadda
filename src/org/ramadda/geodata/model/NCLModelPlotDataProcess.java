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

package org.ramadda.geodata.model;


import org.ramadda.data.process.Service;
import org.ramadda.data.process.ServiceInput;
import org.ramadda.data.process.ServiceOperand;
import org.ramadda.data.process.ServiceOutput;
import org.ramadda.geodata.cdmdata.CdmDataOutputHandler;
import org.ramadda.repository.Constants;
import org.ramadda.repository.Entry;
import org.ramadda.repository.Repository;
import org.ramadda.repository.RepositoryManager;
import org.ramadda.repository.Request;
import org.ramadda.repository.Resource;
import org.ramadda.repository.job.JobManager;
import org.ramadda.repository.type.TypeHandler;
import org.ramadda.util.GeoUtils;
import org.ramadda.util.HtmlUtils;

import ucar.nc2.dt.GridDatatype;
import ucar.nc2.dt.grid.GridDataset;

import ucar.unidata.geoloc.LatLonRect;
import ucar.unidata.util.IOUtil;
import ucar.unidata.util.Misc;


import java.io.File;

import java.util.ArrayList;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;


/**
 * Map plotting process using NCL
 */
public class NCLModelPlotDataProcess extends Service {

    /** The nclOutputHandler */
    NCLOutputHandler nclOutputHandler;


    /** output type */
    public final static String ARG_NCL_OUTPUT = "ncl.output";

    /** output type */
    public final static String ARG_NCL_UNITS = "ncl.units";

    /** contour interval argument */
    private static final String ARG_NCL_CINT = "ncl.cint";

    /** contour minimum argument */
    private static final String ARG_NCL_CMIN = "ncl.cmin";

    /** contour maximum argument */
    private static final String ARG_NCL_CMAX = "ncl.cmax";

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
     * _more_
     *
     * @param request _more_
     * @param js _more_
     * @param formVar _more_
     *
     * @throws Exception _more_
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
     *
     *
     * @return _more_
     * @throws Exception  problem getting the information for the form
     */
    @Override
    public int addToForm(Request request, ServiceInput input, Appendable sb)
            throws Exception {

        sb.append(HtmlUtils.formTable());
        Entry first = input.getEntries().get(0);

        CdmDataOutputHandler dataOutputHandler =
            nclOutputHandler.getDataOutputHandler();
        GridDataset dataset =
            dataOutputHandler.getCdmManager().getGridDataset(first,
                first.getResource().getPath());
        List<GridDatatype> grids = dataset.getGrids();
        GridDatatype       grid  = grids.get(0);

        if (input.getOperands().size() > 1) {
            sb.append(
                HtmlUtils.formEntry(
                    Repository.msgLabel("Plot As"),
                    HtmlUtils.radio(
                        ARG_NCL_OUTPUT, "diff",
                        RepositoryManager.getShouldButtonBeSelected(
                            request, ARG_NCL_OUTPUT, "diff",
                            true)) + Repository.msg("Difference")
                                   + HtmlUtils.radio(
                                       ARG_NCL_OUTPUT, "comp",
                                       RepositoryManager.getShouldButtonBeSelected(
                                           request, ARG_NCL_OUTPUT, "comp",
                                           false)) + Repository.msg(
                                               "Separate Plots")));
        }
        sb.append(
            HtmlUtils.formEntry(
                Repository.msgLabel("Plot Type"),
                HtmlUtils.radio(
                    NCLOutputHandler.ARG_NCL_PLOTTYPE, "png",
                    RepositoryManager.getShouldButtonBeSelected(
                        request, NCLOutputHandler.ARG_NCL_PLOTTYPE, "png",
                        true)) + Repository.msg("Map (Image)")
                               + HtmlUtils.radio(
                                   NCLOutputHandler.ARG_NCL_PLOTTYPE, "kmz",
                                   RepositoryManager.getShouldButtonBeSelected(
                                       request,
                                       NCLOutputHandler.ARG_NCL_PLOTTYPE,
                                       "kmz", false)) + Repository.msg(
                                           "Google Earth")));
        // units
        String units = grid.getUnitsString();
        if (units.equalsIgnoreCase("K") || units.equalsIgnoreCase("degK")
                || units.equalsIgnoreCase("Kelvins")
                || units.equalsIgnoreCase("Kelvin")) {
            sb.append(
                HtmlUtils.formEntry(
                    Repository.msgLabel("Plot Units"),
                    HtmlUtils.radio(
                        ARG_NCL_UNITS, "K",
                        RepositoryManager.getShouldButtonBeSelected(
                            request, ARG_NCL_UNITS, "K",
                            true)) + Repository.msg("Kelvin")
                                   + HtmlUtils.radio(
                                       ARG_NCL_UNITS, "degC",
                                       RepositoryManager.getShouldButtonBeSelected(
                                           request, ARG_NCL_UNITS, "degC",
                                           false)) + Repository.msg(
                                               "Celsius")));
        } else if (units.equalsIgnoreCase("kg m-2 s-1")
                   || units.equalsIgnoreCase("kg/m^2/s")
                   || units.equalsIgnoreCase("m/day")
                   || units.equalsIgnoreCase("mm/s")) {
            sb.append(HtmlUtils.hidden(ARG_NCL_UNITS, "mm/day"));
            /*
            sb.append(
                HtmlUtils.formEntry(
                    Repository.msgLabel("Output Units"),
                    HtmlUtils.radio(
                        ARG_NCL_UNITS, "mm/s",
                        RepositoryManager.getShouldButtonBeSelected(
                            request, ARG_NCL_UNITS, "mm/s",
                            true)) + Repository.msg("mm/s")
                                   + HtmlUtils.radio(
                                       ARG_NCL_UNITS, "mm/day",
                                       RepositoryManager.getShouldButtonBeSelected(
                                           request, ARG_NCL_UNITS, "mm/day",
                                           true)) + Repository.msg(
                                               "mm/day")));
                                               */
        }
        // TODO:  For now, don't get value from request.  May not
        // be valid if variable changes.
        // Contour interval
        StringBuffer contourSB = new StringBuffer();
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
        sb.append(HtmlUtils.formTableClose());

        return 1;

    }

    /**
     * Process the request
     *
     * @param request  the request
     * @param info _more_
     * @param input    the ServiceInput
     *
     * @return  the output
     *
     * @throws Exception  problems generating the output
     */
    @Override
    public ServiceOutput evaluate(Request request, ServiceInput input)
            throws Exception {

        List<Entry>          outputEntries = new ArrayList<Entry>();
        List<ServiceOperand> ops           = input.getOperands();
        StringBuffer         fileList      = new StringBuffer();
        StringBuffer         nameList      = new StringBuffer();
        Entry                inputEntry    = null;
        boolean              haveOne       = false;
        for (ServiceOperand op : ops) {

            List<Entry> opEntries = op.getEntries();
            inputEntry = opEntries.get(0);
            for (Entry entry : opEntries) {
                if (haveOne) {
                    fileList.append(",");
                    nameList.append(";");
                }
                //fileList.append("\"");
                fileList.append(entry.getResource().toString());
                nameList.append(op.getDescription());
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
        File outFile = new File(IOUtil.joinDir(input.getProcessDir(),
                           wksName) + "." + suffix);
        CdmDataOutputHandler dataOutputHandler =
            nclOutputHandler.getDataOutputHandler();
        GridDataset dataset = dataOutputHandler.getCdmManager().createGrid(
                                  inputEntry.getResource().toString());
        if (dataset == null) {
            throw new Exception("Not a grid");
        }

        StringBuffer commandString = new StringBuffer();
        List<String> commands      = new ArrayList<String>();
        String       ncargRoot     = nclOutputHandler.getNcargRootDir();
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
        envMap.put("productdir", input.getProcessDir().toString());
        envMap.put("plot_type", plotType);
        envMap.put("output", outputType);

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
        String  colormap = "rainbow";
        if (outputType.equals("diff") || haveAnom) {
            colormap = "testcmap";
        }
        envMap.put("colormap", colormap);
        envMap.put("anom", Boolean.toString(haveAnom));
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

        if (dpi.getOperands().size() > 2) {
            return false;
        }

        for (ServiceOperand op : dpi.getOperands()) {
            List<Entry> entries = op.getEntries();
            // TODO: change this when we can handle more than one entry (e.g. daily data)
            if (entries.isEmpty() || (entries.size() > 1)) {
                return false;
            }
            Entry firstEntry = entries.get(0);
            if ( !(firstEntry.getTypeHandler()
                    instanceof ClimateModelFileTypeHandler)) {
                return false;
            }
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
