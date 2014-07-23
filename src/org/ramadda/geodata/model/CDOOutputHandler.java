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


import org.ramadda.data.process.DataProcess;
import org.ramadda.data.process.DataProcessInput;
import org.ramadda.data.process.DataProcessOutput;
import org.ramadda.data.process.DataProcessProvider;
import org.ramadda.geodata.cdmdata.CdmDataOutputHandler;


import org.ramadda.repository.Entry;
import org.ramadda.repository.Link;
import org.ramadda.repository.PageHandler;
import org.ramadda.repository.Repository;
import org.ramadda.repository.Request;
import org.ramadda.repository.Resource;
import org.ramadda.repository.Result;
import org.ramadda.repository.map.MapBoxProperties;
import org.ramadda.repository.map.MapInfo;
import org.ramadda.repository.output.OutputHandler;
import org.ramadda.repository.output.OutputType;
import org.ramadda.util.GeoUtils;
import org.ramadda.util.HtmlUtils;
import org.ramadda.util.TempDir;


import org.w3c.dom.Element;

import ucar.nc2.dataset.CoordinateAxis1D;
import ucar.nc2.dataset.CoordinateAxis1DTime;
import ucar.nc2.dt.GridCoordSystem;
import ucar.nc2.dt.GridDatatype;
import ucar.nc2.dt.grid.GridDataset;
import ucar.nc2.time.Calendar;
import ucar.nc2.time.CalendarDate;
import ucar.nc2.time.CalendarDateFormatter;
import ucar.nc2.units.SimpleUnit;

import ucar.unidata.geoloc.LatLonPointImpl;
import ucar.unidata.geoloc.LatLonRect;
import ucar.unidata.util.DateUtil;
import ucar.unidata.util.IOUtil;
import ucar.unidata.util.Misc;
import ucar.unidata.util.StringUtil;
import ucar.unidata.util.TwoFacedObject;

import ucar.visad.data.CalendarDateTime;

import visad.DateTime;


import java.io.File;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.List;
import java.util.SortedSet;
import java.util.TreeSet;


/**
 * Interface to the Climate Data Operators (CDO) package
 */
public class CDOOutputHandler extends OutputHandler implements DataProcessProvider {

    /** CDO program path */
    private static final String PROP_CDO_PATH = "cdo.path";

    /** prefix for cdo args */
    private static final String ARG_CDO_PREFIX = "cdo_";

    /** operation identifier */
    public static final String ARG_CDO_OPERATION = ARG_CDO_PREFIX
                                                   + "operation";

    /** start month identifier */
    public static final String ARG_CDO_STARTMONTH = ARG_CDO_PREFIX
                                                    + "startmonth";

    /** end month identifier */
    public static final String ARG_CDO_ENDMONTH = ARG_CDO_PREFIX + "endmonth";

    /** months identifier */
    public static final String ARG_CDO_MONTHS = ARG_CDO_PREFIX + "months";

    /** start year identifier */
    public static final String ARG_CDO_STARTYEAR = ARG_CDO_PREFIX
                                                   + "startyear";

    /** end year identifier */
    public static final String ARG_CDO_ENDYEAR = ARG_CDO_PREFIX + "endyear";

    /** years identifier */
    public static final String ARG_CDO_YEARS = ARG_CDO_PREFIX + "years";

    /** variable identifier */
    public static final String ARG_CDO_PARAM = ARG_CDO_PREFIX + "param";

    /** end month identifier */
    public static final String ARG_CDO_LEVEL = ARG_CDO_PREFIX + "level";

    /** statistic identifier */
    public static final String ARG_CDO_STAT = ARG_CDO_PREFIX + "stat";

    /** from date arg */
    public static final String ARG_CDO_FROMDATE = ARG_CDO_PREFIX + "fromdate";

    /** to date arg */
    public static final String ARG_CDO_TODATE = ARG_CDO_PREFIX + "todate";


    /** period identifier */
    public static final String ARG_CDO_PERIOD = ARG_CDO_PREFIX + "period";

    /** area argument */
    //public static final String ARG_CDO_AREA = ARG_CDO_PREFIX + "area";
    public static final String ARG_CDO_AREA = "area";

    /** area argument - north */
    private static final String ARG_CDO_AREA_NORTH = ARG_CDO_AREA + "_north";

    /** area argument - south */
    private static final String ARG_CDO_AREA_SOUTH = ARG_CDO_AREA + "_south";

    /** area argument - east */
    private static final String ARG_CDO_AREA_EAST = ARG_CDO_AREA + "_east";

    /** area argument - west */
    private static final String ARG_CDO_AREA_WEST = ARG_CDO_AREA + "_west";


    /** CDO Output Type */
    public static final OutputType OUTPUT_CDO =
        new OutputType("Data Analysis", "cdo", OutputType.TYPE_OTHER,
                       OutputType.SUFFIX_NONE, "/model/cdo.png",
                       CdmDataOutputHandler.GROUP_DATA);

    /** info operator */
    public static final String OP_INFO = "info";

    /** short info operator */
    public static final String OP_SINFO = "sinfo";

    /** number of years operator */
    public static final String OP_NYEAR = "nyear";

    /** select years operator */
    public static final String OP_SELYEAR = "-selyear";

    /** select months operator */
    public static final String OP_SELMON = "-selmon";

    /** select seasons operator */
    public static final String OP_SELSEAS = "-selseas";

    /** select date operator */
    public static final String OP_SELDATE = "-seldate";

    /** select llbox operator */
    public static final String OP_SELLLBOX = "-sellonlatbox";

    /** select level operator */
    public static final String OP_SELLEVEL = "-sellevel";

    /** statistic mean */
    public static final String STAT_MEAN = "mean";

    /** statistic standard deviation */
    public static final String STAT_STD = "std";

    /** statistic max */
    public static final String STAT_MAX = "max";

    /** statistic anomaly */
    public static final String STAT_ANOM = "anomaly";

    /** statistic min */
    public static final String STAT_MIN = "min";

    /** year period */
    public static final String PERIOD_TIM = "tim";

    /** year period */
    public static final String PERIOD_YEAR = "year";

    /** month of year period */
    public static final String PERIOD_YMON = "ymon";

    /** month period */
    public static final String PERIOD_MON = "mon";

    /** day of year period */
    public static final String PERIOD_YDAY = "yday";

    /** day period */
    public static final String PERIOD_DAY = "day";

    /** start year */
    int startYear = 1979;

    /** end year */
    private int endYear = 2011;

    /** info types */
    @SuppressWarnings("unchecked")
    private List<TwoFacedObject> INFO_TYPES = Misc.toList(new Object[] {
                                                  new TwoFacedObject("Info",
                                                      OP_INFO),
            new TwoFacedObject("Short Info", OP_SINFO),
            new TwoFacedObject("Number of Years", OP_NYEAR), });

    /** stat types */
    @SuppressWarnings("unchecked")
    public static final List<TwoFacedObject> STAT_TYPES =
        Misc.toList(new Object[] { new TwoFacedObject("Mean", STAT_MEAN),
                                   new TwoFacedObject(
                                       "Std Deviation",
                                       STAT_STD), new TwoFacedObject(
                                           "Maximum", STAT_MAX),
                                   new TwoFacedObject("Minimum", STAT_MIN),
                                   new TwoFacedObject("Anomaly",
                                       STAT_ANOM) });

    /** period types */
    @SuppressWarnings("unchecked")
    public static final List<TwoFacedObject> PERIOD_TYPES =
        Misc.toList(new Object[] {
            new TwoFacedObject("All Times", PERIOD_TIM),
            new TwoFacedObject("Annual", PERIOD_YEAR),
            new TwoFacedObject("Monthly", PERIOD_YMON) });

    /** month names */
    private static final String[] MONTH_NAMES = {
        "January", "February", "March", "April", "May", "June", "July",
        "August", "September", "October", "November", "December"
    };

    /** month numbers */
    private static final int[] MONTH_NUMBERS = {
        1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12
    };

    /** month list */
    public static final List<TwoFacedObject> MONTHS =
        TwoFacedObject.createList(MONTH_NUMBERS, MONTH_NAMES);

    /** spatial arguments */
    private static final String[] SPATIALARGS = new String[] {
                                                    ARG_CDO_AREA_NORTH,
            ARG_CDO_AREA_WEST, ARG_CDO_AREA_SOUTH, ARG_CDO_AREA_EAST, };


    /** the product directory */
    private TempDir productDir;

    /** the path to cdo program */
    private String cdoPath;

    /**
     * Create a CDOOutputHandler
     *
     * @param repository  the repository
     *
     * @throws Exception problem during creation
     */
    public CDOOutputHandler(Repository repository) throws Exception {
        super(repository, "CDO");
        cdoPath = getProperty(PROP_CDO_PATH, null);
    }

    /**
     * Constructor
     *
     * @param repository   the Repository
     * @param element      the Element
     * @throws Exception   problem creating handler
     */
    public CDOOutputHandler(Repository repository, Element element)
            throws Exception {
        super(repository, element);
        addType(OUTPUT_CDO);
        cdoPath = getProperty(PROP_CDO_PATH, null);
    }

    /**
     * Get the CDO path
     *
     * @return  the CDO path
     */
    public String getCDOPath() {
        return cdoPath;
    }

    /**
     *  The DataProcessProvider method. Just adds this
     *
     * @return List of DataProcesses
     */
    public List<DataProcess> getDataProcesses() {
        List<DataProcess> processes = new ArrayList<DataProcess>();
        //TODO: put this back
        if (isEnabled()) {
            //if (true) {
            processes.add(new CDOAreaStatistics());
        }

        return processes;
    }


    /**
     * Get the DataProcess id
     *
     * @return the ID
     */
    public String getDataProcessId() {
        return "CDO";
    }

    /**
     * Get the label for the DataProcess
     *
     * @return the label
     */
    public String getDataProcessLabel() {
        return "Climate Data Operator";
    }



    /**
     * Is this enabled
     *
     * @return  true if enabled
     */
    public boolean isEnabled() {
        return cdoPath != null;
    }



    /**
     * This method gets called to determine if the given entry or entries can be displays as las xml
     *
     * @param request  the Request
     * @param state    the State
     * @param links    the list of Links to add to
     *
     * @throws Exception Problem adding links
     */
    public void getEntryLinks(Request request, State state, List<Link> links)
            throws Exception {
        if ( !isEnabled()) {
            return;
        }
        Entry stateEntry = state.getEntry();
        if ((stateEntry != null) && stateEntry.isFile()
                && (stateEntry.getTypeHandler()
                    instanceof ClimateModelFileTypeHandler)) {
            links.add(makeLink(request, state.entry, OUTPUT_CDO));
        }
    }

    /**
     * Get the product directory
     *
     * @return  the directory
     *
     * @throws Exception  problem getting directory
     */
    public File getProductDir() throws Exception {
        if (productDir == null) {
            TempDir tempDir = getStorageManager().makeTempDir("cdoproducts");
            //keep things around for 1 hour
            tempDir.setMaxAge(1000 * 60 * 60 * 1);
            productDir = tempDir;
        }

        return productDir.getDir();
    }



    /**
     * Get the data output handler
     *
     * @return the handler
     *
     * @throws Exception Problem getting that
     */
    public CdmDataOutputHandler getDataOutputHandler() throws Exception {
        return (CdmDataOutputHandler) getRepository().getOutputHandler(
            CdmDataOutputHandler.OUTPUT_OPENDAP.toString());
    }




    /**
     * Create the entry display
     *
     * @param request   the Request
     * @param outputType  the output type
     * @param entry     the entry to output
     *
     * @return the entry or form
     *
     * @throws Exception problem making the form
     */
    public Result outputEntry(Request request, OutputType outputType,
                              Entry entry)
            throws Exception {


        if (request.defined(ARG_SUBMIT)) {
            return outputCDO(request, entry);
        }
        StringBuilder sb = new StringBuilder();
        addForm(request, entry, sb);

        return new Result("CDO Form", sb);
    }

    /**
     * Add the form
     *
     * @param request  the Request
     * @param entry    the Entry
     * @param sb       the HTML
     *
     * @throws Exception problems
     */
    private void addForm(Request request, Entry entry, StringBuilder sb)
            throws Exception {

        String formUrl = request.url(getRepository().URL_ENTRY_SHOW);
        sb.append(HtmlUtils.form(formUrl));
        /*
        sb.append(HtmlUtils.form(formUrl,
                                 makeFormSubmitDialog(sb,
                                     msg("Processing Data...."))));
        */

        sb.append(HtmlUtils.formTable());
        sb.append(HtmlUtils.hidden(ARG_OUTPUT, OUTPUT_CDO));
        sb.append(HtmlUtils.hidden(ARG_ENTRYID, entry.getId()));
        String buttons = HtmlUtils.submit("Extract Data", ARG_SUBMIT);
        //sb.append(buttons);
        sb.append(HtmlUtils.h2("Dataset Analysis"));
        sb.append(HtmlUtils.hr());
        addToForm(request, entry, sb);
        sb.append(HtmlUtils.formTableClose());
        sb.append(buttons);

        /*
        sb.append(
            HtmlUtils.href(
                "https://code.zmaw.de/projects/cdo/wiki/Cdo#Documentation",
                "CDO Documentation", " target=_external "));
                */

    }

    /**
     * Add this output handlers UI to the form
     *
     * @param request   the Request
     * @param entry     the Entry
     * @param sb        the form HTML
     *
     * @throws Exception  on badness
     */
    public void addToForm(Request request, Entry entry, StringBuilder sb)
            throws Exception {
        //sb.append(HtmlUtils.formTable());
        if (entry.getType().equals("noaa_climate_modelfile")) {
            //values[1] = var;
            //values[2] = model;
            //values[3] = experiment;
            //values[4] = member;
            //values[5] = frequency;
            Object[]     values = entry.getValues();
            StringBuilder header = new StringBuilder();
            header.append("Model: ");
            header.append(values[2]);
            header.append(" Experiment: ");
            header.append(values[3]);
            header.append(" Ensemble: ");
            header.append(values[4]);
            header.append(" Frequency: ");
            header.append(values[5]);
            //sb.append(HtmlUtils.h3(header.toString()));
        }

        //addInfoWidget(request, sb);
        CdmDataOutputHandler dataOutputHandler = getDataOutputHandler();
        GridDataset dataset =
            dataOutputHandler.getCdmManager().getGridDataset(entry,
                entry.getResource().getPath());

        if (dataset != null) {
            addVarLevelWidget(request, sb, dataset);
        }

        addStatsWidget(request, sb);

        //if(dataset != null)  {
        addTimeWidget(request, sb, dataset, true);
        //}

        LatLonRect llr = null;
        if (dataset != null) {
            llr = dataset.getBoundingBox();
        } else {
            llr = new LatLonRect(new LatLonPointImpl(90.0, -180.0),
                                 new LatLonPointImpl(-90.0, 180.0));
        }
        addMapWidget(request, sb, llr);
        addPublishWidget(
            request, entry, sb,
            msg("Select a folder to publish the generated NetCDF file to"));
        //sb.append(HtmlUtils.formTableClose());
    }

    /**
     * Add the statitics widget
     *
     * @param request  the Request
     * @param sb       the HTML
     */
    public void addStatsWidget(Request request, StringBuilder sb) {
        sb.append(
            HtmlUtils.formEntry(
                msgLabel("Statistic"),
                msgLabel("Period")
                + HtmlUtils.select(
                    ARG_CDO_PERIOD, PERIOD_TYPES,
                    request.getString(
                        ARG_CDO_PERIOD, null)) + HtmlUtils.space(3)
                            + msgLabel("Type")
                            + HtmlUtils.select(
                                ARG_CDO_STAT, STAT_TYPES,
                                request.getString(ARG_CDO_STAT, null))));
    }

    /**
     * Add the variable/level selector widget
     *
     * @param request  the Request
     * @param sb       the HTML
     * @param dataset  the dataset
     */
    public void addVarLevelWidget(Request request, StringBuilder sb,
                                  GridDataset dataset) throws Exception {
        addVarLevelWidget(request, sb, dataset, ARG_CDO_LEVEL);
    }

    /**
     * Add the variable/level selector widget
     *
     * @param request  the Request
     * @param sb       the HTML
     * @param dataset  the dataset
     * @param levelArg the level argument
     */
    public void addVarLevelWidget(Request request, StringBuilder sb,
                                  GridDataset dataset, String levelArg) throws Exception {
        List<GridDatatype> grids = dataset.getGrids();
        StringBuilder       varsb = new StringBuilder();
        //TODO: handle multiple variables
        //List<TwoFacedObject> varList = new ArrayList<TwoFacedObject>(grids.size());
        //for (GridDatatype grid : dataset.getGrids()) {
        //    varList.add(new TwoFacedObject(grid.getDescription(), grid.getName()));
        //}

        //varsb.append(HtmlUtils.select(ARG_CDO_PARAM, varList, request.getString(ARG_CDO_PARAM, null)));
        GridDatatype grid = grids.get(0);
        String longname = grid.getDescription();
        if (longname == null || longname.isEmpty()) {
            longname = grid.getName();
        }
        varsb.append(longname);
        if (grid.getZDimension() != null) {
            varsb.append(HtmlUtils.space(5));
            varsb.append(msgLabel("Level"));
            GridCoordSystem      gcs    = grid.getCoordinateSystem();
            CoordinateAxis1D     zAxis  = gcs.getVerticalAxis();
            int                  sizeZ  = (int) zAxis.getSize();
            String               unit   =
                zAxis.getUnitsString().toLowerCase();
            List<TwoFacedObject> levels =
                new ArrayList<TwoFacedObject>(sizeZ);
            // TODO: Gotta be a better way to do this.
            for (int i = 0; i < sizeZ; i++) {
                int    lev   = (int) zAxis.getCoordValue(i);
                String label = String.valueOf(unit.startsWith("pa")
                        ? lev / 100
                        : lev);
                levels.add(new TwoFacedObject(label, String.valueOf(lev)));
            }
            varsb.append(HtmlUtils.select(levelArg, levels,
                                          request.getString(levelArg, null)));
            varsb.append(HtmlUtils.space(2));
            varsb.append("hPa");
            varsb.append(HtmlUtils.hidden(levelArg+"_unit", "hPa"));
        }
        sb.append(HtmlUtils.formEntry(msgLabel("Variable"),
                                      varsb.toString()));
    }


    /**
     * Add the months widget
     *
     * @param request  the Request
     * @param sb       the StringBuilder to add to
     */
    private void addInfoWidget(Request request, StringBuilder sb) throws Exception {
        sb.append(
            HtmlUtils.formEntry(
                msgLabel("Months"),
                HtmlUtils.select(
                    ARG_CDO_OPERATION, INFO_TYPES,
                    request.getString(ARG_CDO_OPERATION, null))));
    }

    /**
     * Add a time widget
     *
     * @param request  the Request
     * @param sb       the HTML page
     * @param dataset  the GridDataset
     * @param useYYMM  true to provide month/year widgets, otherwise straight dates
     */
    public void addTimeWidget(Request request, StringBuilder sb,
                              GridDataset dataset, boolean useYYMM) throws Exception {
        List<CalendarDate> dates = CdmDataOutputHandler.getGridDates(dataset);
        if ( !dates.isEmpty()) {
            CalendarDate cd  = dates.get(0);
            Calendar     cal = cd.getCalendar();
            if (cal != null) {
                sb.append(
                    HtmlUtils.hidden(
                        CdmDataOutputHandler.ARG_CALENDAR,
                        request.getString(
                            CdmDataOutputHandler.ARG_CALENDAR,
                            cal.toString())));
            }
        }
        //if ((dates != null) && (!dates.size() > 0)) {
        if (useYYMM) {
            makeMonthsWidget(request, sb, dates);
            makeYearsWidget(request, sb, dates);
        } else {
            makeTimesWidget(request, sb, dates);
        }
        //}
    }

    /**
     * Format a date
     *
     * @param request  the request
     * @param date     the date object (CalendarDate or Date)
     *
     * @return the formatted date
     */
    public String formatDate(Request request, Object date) {
        if (date == null) {
            return BLANK;
        }
        if (date instanceof CalendarDate) {
            String dateFormat = getProperty(PROP_DATE_FORMAT,
                                            PageHandler.DEFAULT_TIME_FORMAT);

            return new CalendarDateFormatter(dateFormat).toString(
                (CalendarDate) date);
        } else if (date instanceof Date) {
            return getPageHandler().formatDate(request, (Date) date);
        } else {
            return date.toString();
        }
    }

    /**
     * Add at time widget
     *
     * @param request  the Request
     * @param sb       the HTML
     * @param dates    the list of Dates
     */
    private void makeTimesWidget(Request request, StringBuilder sb,
                                 List<CalendarDate> dates) throws Exception {
        List formattedDates = new ArrayList();
        formattedDates.add(new TwoFacedObject("---", ""));
        for (CalendarDate date : dates) {
            //formattedDates.add(getPageHandler().formatDate(request, date));
            formattedDates.add(formatDate(request, date));
        }
        /*
          for now default to "" for dates
        String fromDate = request.getUnsafeString(ARG_CDO_FROMDATE,
        getPageHandler().formatDate(request,
                                  dates.get(0)));
        String toDate = request.getUnsafeString(ARG_CDO_TODATE,
                            getPageHandler().formatDate(request,
                                dates.get(dates.size() - 1)));
        */
        String fromDate = request.getUnsafeString(ARG_CDO_FROMDATE, "");
        String toDate   = request.getUnsafeString(ARG_CDO_TODATE, "");
        sb.append(
            HtmlUtils.formEntry(
                msgLabel("Time Range"),
                HtmlUtils.select(ARG_CDO_FROMDATE, formattedDates, fromDate)
                + HtmlUtils.img(iconUrl(ICON_ARROW))
                + HtmlUtils.select(ARG_CDO_TODATE, formattedDates, toDate)));
    }

    /**
     * Add the month selection widget
     *
     * @param request  the Request
     * @param sb       the StringBuilder to add to
     * @param dates    the list of dates (just in case)
     */
    public static void makeMonthsWidget(Request request, StringBuilder sb,
                                        List<CalendarDate> dates) throws Exception {
        /*
        HtmlUtils.radio(ARG_CDO_MONTHS, "all", request.get(ARG_CDO_MONTHS, true))+msg("All")+
        HtmlUtils.space(2)+
        HtmlUtils.radio(ARG_CDO_MONTHS, "", request.get(ARG_CDO_MONTHS, false))+msg("Season")+
        HtmlUtils.space(2)+
        */
        sb.append(
            HtmlUtils.formEntry(
                msgLabel("Months"),
                msgLabel("Start")
                + HtmlUtils.select(
                    ARG_CDO_STARTMONTH, MONTHS,
                    request.getString(
                        ARG_CDO_STARTMONTH, null), HtmlUtils.title("Select the starting month")) 
                        + HtmlUtils.space(2)
                            + msgLabel("End")
                            + HtmlUtils.select(
                                ARG_CDO_ENDMONTH, MONTHS,
                                request.getString(
                                    ARG_CDO_ENDMONTH,
                                    MONTHS.get(
                                        MONTHS.size()
                                        - 1).getId().toString()), HtmlUtils.title("Select the ending month"))));
    }

    /**
     * Add the year selection widget
     *
     * @param request  the Request
     * @param sb       the StringBuilder to add to
     * @param dates    the list of dates
     */
    private void makeYearsWidget(Request request, StringBuilder sb,
                                 List<CalendarDate> dates) throws Exception {
        SortedSet<String> uniqueYears =
            Collections.synchronizedSortedSet(new TreeSet<String>());
        if ((dates != null) && !dates.isEmpty()) {
            for (CalendarDate d : dates) {
                try {  // shouldn't get an exception
                    String year = new CalendarDateTime(d).formattedString(
                                      "yyyy",
                                      CalendarDateTime.DEFAULT_TIMEZONE);
                    uniqueYears.add(year);
                } catch (Exception e) {}
            }
        }
        List<String> years = new ArrayList<String>(uniqueYears);
        // TODO:  make a better list of years
        if (years.isEmpty()) {
            for (int i = startYear; i <= endYear; i++) {
                years.add(String.valueOf(i));
            }
        }

        sb.append(
            HtmlUtils.formEntry(
                msgLabel("Years"),
                msgLabel("Start")
                + HtmlUtils.select(
                    ARG_CDO_STARTYEAR, years,
                    request.getString(
                        ARG_CDO_STARTYEAR, years.get(0)), HtmlUtils.title("Select the starting year")) + HtmlUtils.space(
                            3) + msgLabel("End")
                               + HtmlUtils.select(
                                   ARG_CDO_ENDYEAR, years,
                                   request.getString(
                                       ARG_CDO_ENDYEAR,
                                       years.get(years.size() - 1)), HtmlUtils.title("Select the ending year"))));
    }

    /**
     * Add the map widget
     *
     * @param request   The request
     * @param sb        the HTML
     * @param llr       the lat/lon rectangle
     */
    public void addMapWidget(Request request, StringBuilder sb,
                             LatLonRect llr) {
        addMapWidget(request, sb, llr, false);
    }

    /**
     * Add the map widget
     *
     * @param request   The request
     * @param sb        the HTML
     * @param llr       the lat/lon rectangle
     * @param usePopup  use a popup
     */
    public void addMapWidget(Request request, StringBuilder sb,
                             LatLonRect llr, boolean usePopup) {

        //TODO: This should be a parameter to the method.
        //If its null then all map regions are used 
        //If non-null then only map regions with the group
        //String mapRegionGroup = null;
        String mapRegionGroup = "model regions";

        MapInfo map;
        if ( !usePopup) {
            map = getRepository().getMapManager().createMap(request, 250,
                    150, true);
            String maplayers = getRepository().getProperty(PROP_MAP_LAYERS, null);
            if (maplayers != null) {
                map.addProperty("mapLayers", Misc.newList(maplayers));
            } else {
                map.addProperty("mapLayers", Misc.newList("google.terrain"));
            }
            // remove some of the widgets
            map.addProperty("showScaleLine", "false");
            map.addProperty("showLayerSwitcher", "false");
            map.addProperty("showZoomPanControl", "false");
            map.addProperty("showZoomOnlyControl", "true");
        } else {
            map = getRepository().getMapManager().createMap(request, true);
        }

        map.setMapRegions(getPageHandler().getMapRegions(mapRegionGroup));
        map.setDefaultMapRegion(request.getString("mapregion", null));

        //map.addBox("", llr, new MapBoxProperties("blue", false, true));
        String[] points = new String[] { "" + llr.getLatMax(),
                                         "" + llr.getLonMin(),
                                         "" + llr.getLatMin(),
                                         "" + llr.getLonMax(), };

        for (int i = 0; i < points.length; i++) {
            sb.append(HtmlUtils.hidden(SPATIALARGS[i] + ".original",
                                       points[i]));
        }
        //ARG_PREFIX +
        String llb = map.makeSelector(ARG_CDO_AREA, usePopup, points);
        sb.append(HtmlUtils.formEntry(msgLabel("Region"), llb, 4));
    }

    /**
     * Output the cdo request
     *
     * @param request  the request
     * @param entry    the entry
     *
     * @return  the output
     *
     * @throws Exception  problem executing the command
     */
    public Result outputCDO(Request request, Entry entry) throws Exception {
        try {
            File outFile = processRequest(request, entry);

            if (doingPublish(request)) {
                if ( !request.defined(ARG_PUBLISH_NAME)) {
                    request.put(ARG_PUBLISH_NAME, outFile.getName());
                }

                return getEntryManager().processEntryPublish(request,
                        outFile, null, entry, "generated from");
            }

            return request.returnFile(
                outFile, getStorageManager().getFileTail(outFile.toString()));
        } catch (RuntimeException rte) {
            return getErrorResult(request, "CDO-Error", rte.toString());
        }
    }

    /**
     * Process the request
     *
     * @param request  the request
     * @param entry    the Entry
     *
     * @return the output file
     *
     * @throws Exception  problems running the commands
     */
    public File processRequest(Request request, Entry entry)
            throws Exception {
        String tail    = getStorageManager().getFileTail(entry);
        String newName = IOUtil.stripExtension(tail) + "_product.nc";
        tail = getStorageManager().getStorageFileName(tail);
        File outFile = new File(IOUtil.joinDir(getProductDir(), newName));
        List<String> commands = new ArrayList<String>();
        commands.add(cdoPath);
        commands.add("-L");
        commands.add("-s");
        commands.add("-O");
        String operation = request.getString(ARG_CDO_OPERATION, OP_INFO);
        //commands.add(operation);

        // Select order (left to right) - operations go right to left:
        //   - stats
        //   - level
        //   - region
        //   - month range
        //   - year or time range

        addStatCommands(request, entry, commands);
        addLevelSelectCommands(request, entry, commands);
        addAreaSelectCommands(request, entry, commands);
        addDateSelectCommands(request, entry, commands);

        System.err.println("cmds:" + commands);

        commands.add(entry.getResource().getPath());
        commands.add(outFile.toString());
        String[] results = getRepository().executeCommand(commands, null,
                               getProductDir());
        String errorMsg = results[1];
        String outMsg   = results[0];
        if ( !outFile.exists()) {
            if (outMsg.length() > 0) {
                throw new IllegalArgumentException(outMsg);
            }
            if (errorMsg.length() > 0) {
                throw new IllegalArgumentException(errorMsg);
            }
            if ( !outFile.exists()) {
                throw new IllegalArgumentException(
                    "Humm, the CDO processing failed for some reason");
            }
        }

        //The jeff is here for when I have a fake cdo.sh
        boolean jeff = false;

        if (doingPublish(request)) {
            return outFile;
        }

        //Assuming this is some text - DOESN'T HAPPEN anymore
        if (operation.equals(OP_INFO) && false) {
            String info;

            if ( !jeff) {
                info = IOUtil.readInputStream(
                    getStorageManager().getFileInputStream(outFile));
            } else {
                info = outMsg;
            }

            StringBuilder sb = new StringBuilder();
            addForm(request, entry, sb);
            sb.append(header(msg("CDO Information")));
            sb.append(HtmlUtils.pre(info));

            //            return new Result("CDO", sb);
        }

        return outFile;
    }

    /**
     * Set the start year
     *
     * @param start  start year
     */
    public void setStartYear(int start) {
        startYear = start;
    }

    /**
     * Set the end year
     *
     * @param end end year
     */
    public void setEndYear(int end) {
        endYear = end;
    }

    /**
     * Create the CDO command to select an area
     *
     * @param request  the Request
     * @param entry    the Entry
     * @param commands commands to add to
     */
    public void addAreaSelectCommands(Request request, Entry entry,
                                      List<String> commands) {
        boolean anySpatialDifferent = false;
        boolean haveAllSpatialArgs  = true;
        for (String spatialArg : SPATIALARGS) {
            if ( !Misc.equals(request.getString(spatialArg, ""),
                              request.getString(spatialArg + ".original",
                                  ""))) {
                anySpatialDifferent = true;

                break;
            }
        }

        for (String spatialArg : SPATIALARGS) {
            if ( !request.defined(spatialArg)) {
                haveAllSpatialArgs = false;

                break;
            }
        }

        String llSelect = "";
        if (haveAllSpatialArgs && anySpatialDifferent) {
            // Normalize longitude bounds to the data
            double origLonMin =
                Double.parseDouble(request.getString(ARG_CDO_AREA_WEST
                    + ".original", "-180"));
            double origLonMax =
                Double.parseDouble(request.getString(ARG_CDO_AREA_EAST
                    + ".original", "180"));
            double lonMin =
                Double.parseDouble(request.getString(ARG_CDO_AREA_WEST,
                    "-180"));
            double lonMax =
                Double.parseDouble(request.getString(ARG_CDO_AREA_EAST,
                    "180"));
            // TODO: do we need to do this?  CDO seems to handle 
            // the -180 to 180 vs 0 to 360 subsetting okay.
//            if (origLonMin < 0) {  // -180 to 180
//                lonMin = GeoUtils.normalizeLongitude(lonMin);
//                lonMax = GeoUtils.normalizeLongitude(lonMax);
//            } else {               // 0-360
//                lonMin = GeoUtils.normalizeLongitude360(lonMin);
//                lonMax = GeoUtils.normalizeLongitude360(lonMax);
//            }
            llSelect = OP_SELLLBOX + "," + String.valueOf(lonMin) + ","
                       + String.valueOf(lonMax) + ","
            //+ request.getString(ARG_CDO_AREA_WEST, "-180") + ","
            //+ request.getString(ARG_CDO_AREA_EAST, "180") + ","
            + request.getString(ARG_CDO_AREA_SOUTH, "-90") + ","
                    + request.getString(ARG_CDO_AREA_NORTH, "90");
        }
        if ( !llSelect.isEmpty()) {
            commands.add(llSelect);
        }
    }

    /**
     * Create the region subset command
     * @param request  the Request
     * @param entry    the Entry
     * @param commands list of commands
     */
    public void addLevelSelectCommands(Request request, Entry entry,
                                       List<String> commands) {
        addLevelSelectCommands(request, entry, commands, ARG_CDO_LEVEL);
    }

    /**
     * Create the region subset command
     * @param request  the Request
     * @param entry    the Entry
     * @param commands the list of commands
     * @param levelArg  the level argument
     */
    public void addLevelSelectCommands(Request request, Entry entry,
                                       List<String> commands,
                                       String levelArg) {
        String levSelect = null;
        if (request.defined(levelArg)) {
            String level = request.getString(levelArg);
            if (level != null) {
                String levelUnit = request.getString(levelArg+"_unit","hPa");
                String dataUnit = "hPa";
                try {
                    CdmDataOutputHandler dataOutputHandler = getDataOutputHandler();
                    GridDataset dataset =
                        dataOutputHandler.getCdmManager().getGridDataset(entry,
                            entry.getResource().getPath());
                    GridDatatype grid = dataset.getGrids().get(0);
                    if (grid.getZDimension() != null) {
                        GridCoordSystem      gcs    = grid.getCoordinateSystem();
                        CoordinateAxis1D     zAxis  = gcs.getVerticalAxis();
                        dataUnit   = zAxis.getUnitsString().toLowerCase();
                    }
                    if (!Misc.equals(levelUnit, dataUnit) && 
                        SimpleUnit.isCompatible(levelUnit, dataUnit)) {
                        SimpleUnit have = SimpleUnit.factory(levelUnit);
                        SimpleUnit want = SimpleUnit.factory(dataUnit);
                        level = String.valueOf(have.convertTo(Misc.parseDouble(level), want));
                    }
                } catch (Exception e) {
                    System.err.println("can't convert level from " + levelUnit + " to " +dataUnit );
                }
                levSelect = OP_SELLEVEL + "," + level;
            }
        }
        if (levSelect != null) {
            commands.add(levSelect);
        }
    }

    /**
     * Create the list of date/time select commands
     * @param request the Request
     * @param entry   the associated Entry
     * @param commands  list of commands
     * @throws Exception  on badness
     */
    public void addMonthSelectCommands(Request request, Entry entry,
                                       List<String> commands)
            throws Exception {

        if (request.defined(ARG_CDO_MONTHS)
                && request.getString(ARG_CDO_MONTHS).equalsIgnoreCase(
                    "all")) {
            return;
        }
        if (request.defined(ARG_CDO_STARTMONTH)
                || request.defined(ARG_CDO_ENDMONTH)) {
            int startMonth = request.defined(ARG_CDO_STARTMONTH)
                             ? request.get(ARG_CDO_STARTMONTH, 1)
                             : 1;
            int endMonth   = request.defined(ARG_CDO_ENDMONTH)
                             ? request.get(ARG_CDO_ENDMONTH, startMonth)
                             : startMonth;
            // if they requested all months, no need to do a select on month
            if ((startMonth == 1) && (endMonth == 12)) {
                return;
            }
            StringBuilder buf = new StringBuilder(OP_SELMON + "," + startMonth);
            if (endMonth > startMonth) {
                buf.append("/");
                buf.append(endMonth);
            } else if (startMonth > endMonth) {
                int firstMonth = startMonth + 1;
                while (firstMonth <= 12) {
                    buf.append(",");
                    buf.append(firstMonth);
                    firstMonth++;
                }
                for (int i = 0; i < endMonth; i++) {
                    buf.append(",");
                    buf.append((i + 1));
                }
            }
            commands.add(buf.toString());
        } else {  // ONLY FOR TESTING
            commands.add(OP_SELMON + ",1");
        }
    }

    /**
     * Create the list of date/time select commands
     * @param request the Request
     * @param entry   the associated Entry
     * @param commands list of commands
     *
     * @throws Exception  on badness
     */
    public void addDateSelectCommands(Request request, Entry entry,
                                      List<String> commands)
            throws Exception {
        addDateSelectCommands(request, entry, commands, 0);
    }

    /**
     * Create the list of date/time select commands
     * @param request the Request
     * @param entry   the associated Entry
     * @param commands list of commands
     * @param dateCounter  the date counter
     *
     * @throws Exception  on badness
     */
    public void addDateSelectCommands(Request request, Entry entry,
                                      List<String> commands, int dateCounter)
            throws Exception {

        String dateCounterString = "";
        if (dateCounter > 0) {
            dateCounterString = String.valueOf(dateCounter + 1);
        }

        String dateSelect = null;
        if (request.defined(ARG_CDO_FROMDATE + dateCounterString)
                || request.defined(ARG_CDO_TODATE + dateCounterString)) {
            CalendarDate[] dates = new CalendarDate[2];
            String calString =
                request.getString(CdmDataOutputHandler.ARG_CALENDAR, null);
            if (request.defined(ARG_FROMDATE + dateCounterString)) {
                String fromDateString = request.getString(ARG_FROMDATE
                                            + dateCounterString, null);
                dates[0] = CalendarDate.parseISOformat(calString,
                        fromDateString);
            }
            if (request.defined(ARG_TODATE + dateCounterString)) {
                String toDateString = request.getString(ARG_TODATE
                                          + dateCounterString, null);
                dates[1] = CalendarDate.parseISOformat(calString,
                        toDateString);
            }

            //have to have both dates
            if ((dates[0] != null) && (dates[1] == null)) {
                dates[0] = null;
            }
            if ((dates[1] != null) && (dates[0] == null)) {
                dates[1] = null;
            }
            if ((dates[0] != null) && (dates[1] != null)) {
                if (dates[0].isAfter(dates[1])) {
                    getPageHandler().showDialogWarning(
                        "From date is after to date");
                } else {
                    dateSelect =
                        OP_SELDATE + ","
                        + CalendarDateFormatter.toDateTimeStringISO(dates[0])
                        + ","
                        + CalendarDateFormatter.toDateTimeStringISO(dates[1]);
                }
            }
        } else {                                // month and year
            addMonthSelectCommands(request, entry, commands);
            if (dateCounterString.isEmpty()) {  // first time through
                String yearString = null;
                if (request.defined(ARG_CDO_YEARS)) {
                    yearString = request.getString(ARG_CDO_YEARS);
                    yearString = verifyYearsList(yearString);
                } else {
                    String startYear =
                        request.getString(CDOOutputHandler.ARG_CDO_STARTYEAR,
                                          null);
                    String endYear =
                        request.getString(CDOOutputHandler.ARG_CDO_ENDYEAR,
                                          startYear);
                    verifyStartEndYears(startYear, endYear);
                    yearString = startYear + "/" + endYear;
                }
                dateSelect = CDOOutputHandler.OP_SELYEAR + "," + yearString;
            } else {
                String years = request.defined(ARG_CDO_YEARS
                                   + dateCounterString)
                               ? request.getString(ARG_CDO_YEARS
                                   + dateCounterString)
                               : null;
                if ((years == null) && request.defined(ARG_CDO_YEARS)
                        && !(request.defined(
                            ARG_CDO_STARTYEAR
                            + dateCounterString) || request.defined(
                                ARG_CDO_ENDYEAR + dateCounterString))) {
                    years = request.getString(ARG_CDO_YEARS);
                }
                if (years != null) {
                    years      = verifyYearsList(years);
                    dateSelect = CDOOutputHandler.OP_SELYEAR + "," + years;
                } else {
                    String startYear =
                        request.defined(CDOOutputHandler.ARG_CDO_STARTYEAR
                                        + dateCounterString)
                        ? request.getString(
                            CDOOutputHandler.ARG_CDO_STARTYEAR
                            + dateCounterString)
                        : request.defined(CDOOutputHandler.ARG_CDO_STARTYEAR)
                          ? request.getString(
                              CDOOutputHandler.ARG_CDO_STARTYEAR, null)
                          : null;
                    String endYear =
                        request.defined(CDOOutputHandler.ARG_CDO_ENDYEAR
                                        + dateCounterString)
                        ? request.getString(CDOOutputHandler.ARG_CDO_ENDYEAR
                                            + dateCounterString)
                        : request.defined(CDOOutputHandler.ARG_CDO_ENDYEAR)
                          ? request.getString(
                              CDOOutputHandler.ARG_CDO_ENDYEAR, startYear)
                          : startYear;
                    verifyStartEndYears(startYear, endYear);
                    dateSelect = CDOOutputHandler.OP_SELYEAR + ","
                                 + startYear + "/" + endYear;
                }
            }
        }
        if (dateSelect != null) {
            commands.add(dateSelect);
        }
    }

    /**
     * Verify the start/end years
     *
     * @param startYear startYear
     * @param endYear   endYear
     *
     * @throws Exception bad (or null) years
     */
    private void verifyStartEndYears(String startYear, String endYear)
            throws Exception {
        //have to have both dates
        if ((startYear != null) && (endYear == null)) {
            startYear = null;
        }
        if ((endYear != null) && (startYear == null)) {
            endYear = null;
        }
        if ((startYear != null) && (endYear != null)) {
            if (startYear.compareTo(endYear) > 0) {
                throw new IllegalArgumentException(
                    "Start year is after end year");
            }
        }
    }

    /**
     * Verify that the list of years if valid
     *
     * @param years  list of comma separated years
     *
     * @return  the list of valid years
     */
    public static String verifyYearsList(String years) {
        List<String>  yearList = StringUtil.split(years, ",", true, true);
        List<Integer> newYears = new ArrayList<Integer>();
        // TODO: verify list of years by the data
        for (String year : yearList) {
            try {
                int yearInt = Integer.parseInt(year);
                newYears.add(yearInt);
            } catch (NumberFormatException nfe) {
                System.out.println("Bad year: " + year + ", omitting");

                continue;
            }
        }
        Collections.sort(newYears);

        return StringUtil.join(",", newYears, true);
    }

    /**
     * Create the statistics command
     * @param request  the request
     * @param entry    the entry
     * @param commands list of commands
     */
    public void addStatCommands(Request request, Entry entry,
                                List<String> commands) {
        if (request.defined(ARG_CDO_PERIOD)
                && request.defined(ARG_CDO_STAT)) {
            String period = request.getString(ARG_CDO_PERIOD);
            String stat   = request.getString(ARG_CDO_STAT);
            if ((period == null) || (stat == null)) {
                return;
            }
            // TODO:  Handle anomaly
            if (stat.equals(STAT_ANOM)) {
                stat = STAT_MEAN;
            }
            if (period.equals(PERIOD_TIM)) {
                commands.add("-" + PERIOD_TIM + stat);
            } else if (period.equals(PERIOD_YEAR)) {
                commands.add("-" + PERIOD_YEAR + stat);
            } else if (period.equals(PERIOD_YMON)) {
                commands.add("-" + PERIOD_YMON + stat);
            }
        }  //else {  // ONLY FOR TESTING
        //    commands.add("-" + PERIOD_TIM + STAT_MEAN);
        //}

    }

    /**
     * Class description
     *
     *
     */
    protected class CDOAreaStatistics extends DataProcess {

        /**
         * Area statistics DataProcess
         */
        public CDOAreaStatistics() {
            super("CDO_AREA_STATS", "Area Statistics");
        }

        /**
         * Add to form
         *
         * @param request  the Request
         * @param input    the DataProcessInput
         * @param sb       the form
         *
         * @throws Exception  problem adding to the form
         */
        public void addToForm(Request request, DataProcessInput input,
                              StringBuilder sb)
                throws Exception {
            sb.append(HtmlUtils.formTable());
            Entry first = input.getOperands().get(0).getEntries().get(0);
            if (first.getType().equals("noaa_climate_modelfile")) {
                //values[1] = var;
                //values[2] = model;
                //values[3] = experiment;
                //values[4] = member;
                //values[5] = frequency;
                Object[]     values = first.getValues();
                StringBuilder header = new StringBuilder();
                header.append("Model: ");
                header.append(values[2]);
                header.append(" Experiment: ");
                header.append(values[3]);
                header.append(" Ensemble: ");
                header.append(values[4]);
                header.append(" Frequency: ");
                header.append(values[5]);
                //sb.append(HtmlUtils.h3(header.toString()));
            }

            //addInfoWidget(request, sb);
            CdmDataOutputHandler dataOutputHandler = getDataOutputHandler();
            GridDataset dataset =
                dataOutputHandler.getCdmManager().getGridDataset(first,
                    first.getResource().getPath());

            if (dataset != null) {
                addVarLevelWidget(request, sb, dataset);
            }

            addStatsWidget(request, sb);

            //if(dataset != null)  {
            addTimeWidget(request, sb, dataset, true);
            //}

            LatLonRect llr = null;
            if (dataset != null) {
                llr = dataset.getBoundingBox();
            } else {
                llr = new LatLonRect(new LatLonPointImpl(90.0, -180.0),
                                     new LatLonPointImpl(-90.0, 180.0));
            }
            addMapWidget(request, sb, llr);
            sb.append(HtmlUtils.formTableClose());
        }



        /**
         * Process the request
         *
         * @param request  The request
         * @param input  the  data process input
         *
         * @return  the processed data
         *
         * @throws Exception  problem processing
         */
        public DataProcessOutput processRequest(Request request,
                DataProcessInput input)
                throws Exception {

            Entry  oneOfThem = input.getOperands().get(0).getEntries().get(0);
            String tail      = getStorageManager().getFileTail(oneOfThem);
            String id        = getRepository().getGUID();
            String newName   = IOUtil.stripExtension(tail) + "_" + id + ".nc";
            tail = getStorageManager().getStorageFileName(tail);
            File outFile = new File(IOUtil.joinDir(input.getProcessDir(),
                               newName));
            List<String> commands = new ArrayList<String>();
            commands.add(cdoPath);
            commands.add("-L");
            commands.add("-s");
            commands.add("-O");
            String operation = request.getString(ARG_CDO_OPERATION, OP_INFO);
            //commands.add(operation);

            // Select order (left to right) - operations go right to left:
            //   - stats
            //   - level
            //   - region
            //   - month range
            //   - year or time range

            addStatCommands(request, oneOfThem, commands);
            addLevelSelectCommands(request, oneOfThem, commands);
            addAreaSelectCommands(request, oneOfThem, commands);
            addDateSelectCommands(request, oneOfThem, commands);

            System.err.println("cmds:" + commands);

            commands.add(oneOfThem.getResource().getPath());
            commands.add(outFile.toString());
            String[] results = getRepository().executeCommand(commands, null,
                                   getProductDir());
            String errorMsg = results[1];
            String outMsg   = results[0];
            if ( !outFile.exists()) {
                if (outMsg.length() > 0) {
                    throw new IllegalArgumentException(outMsg);
                }
                if (errorMsg.length() > 0) {
                    throw new IllegalArgumentException(errorMsg);
                }
                if ( !outFile.exists()) {
                    throw new IllegalArgumentException(
                        "Humm, the CDO processing failed for some reason");
                }
            }

            Resource r   = new Resource(outFile, Resource.TYPE_LOCAL_FILE);
            Entry    out = new Entry();
            out.setResource(r);

            if (doingPublish(request)) {
                return new DataProcessOutput(out);
            }

            return new DataProcessOutput(out);
        }

        /**
         * Can we handle this type of DataProcessInput?
         *
         * @param dpi  the DataProcessInput to check
         * @return true if we can handle
         */
        public boolean canHandle(DataProcessInput dpi) {
            return isEnabled();
        }
    }
}
