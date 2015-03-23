package org.ramadda.geodata.model;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.List;
import java.util.SortedSet;
import java.util.TimeZone;
import java.util.TreeSet;

import org.ramadda.data.services.NoaaPsdMonthlyClimateIndexTypeHandler;
import org.ramadda.geodata.cdmdata.CdmDataOutputHandler;
import org.ramadda.repository.Association;
import org.ramadda.repository.Entry;
import org.ramadda.repository.Repository;
import org.ramadda.repository.Request;
import org.ramadda.repository.Resource;
import org.ramadda.repository.type.TypeHandler;
import org.ramadda.service.ServiceInput;
import org.ramadda.service.ServiceOperand;
import org.ramadda.service.ServiceOutput;
import org.ramadda.util.HtmlUtils;

import ucar.nc2.dt.GridDatatype;
import ucar.nc2.dt.grid.GridDataset;
import ucar.nc2.time.Calendar;
import ucar.nc2.time.CalendarDate;
import ucar.nc2.time.CalendarDateRange;
import ucar.unidata.geoloc.LatLonPointImpl;
import ucar.unidata.geoloc.LatLonRect;
import ucar.unidata.util.DateUtil;
import ucar.unidata.util.IOUtil;
import ucar.unidata.util.Misc;
import ucar.unidata.util.StringUtil;
import ucar.visad.data.CalendarDateTime;
import visad.DateTime;

public class TimeSeriesCorrelation extends CDODataProcess {

    /**
     * Area statistics DataProcess
     *
     * @param repository  the Repository
     *
     * @throws Exception  problems
     */
    public TimeSeriesCorrelation(Repository repository) throws Exception {
        super(repository, "TIMESERIES_CORRELATION", "Time Series Correlation");
    }

    /**
     * Add to form
     *
     * @param request  the Request
     * @param input    the ServiceInput
     * @param sb       the form
     * @param argPrefix argument prefixes
     * @param label     the label
     *
     * @throws Exception  problem adding to the form
     */
    @Override
    public void addToForm(Request request, ServiceInput input, Appendable sb,
                          String argPrefix, String label)
            throws Exception {
        sb.append(HtmlUtils.formTable());
        makeInputForm(request, input, sb, argPrefix);
        sb.append(HtmlUtils.formTableClose());
    }

    /**
     * Make the input form
     *
     * @param request  the Request
     * @param input    the ServiceInput
     * @param sb       the StringBuilder
     * @param argPrefix _more_
     *
     * @throws Exception  problem making stuff
     */
    private void makeInputForm(Request request, ServiceInput input,
                               Appendable sb, String argPrefix)
            throws Exception {

        List<Entry> entries = input.getEntries();        
        GridDataset dataset = null;
        Entry tsEntry = null;
        for (Entry e : entries) {
            if (e.getTypeHandler() instanceof ClimateModelFileTypeHandler) {

                if (dataset == null) {
                    CdmDataOutputHandler dataOutputHandler =
                        getOutputHandler().getDataOutputHandler();
                    dataset =
                        dataOutputHandler.getCdmManager().getGridDataset(e,
                            e.getResource().getPath());
            
                    if (dataset != null) {
                        getOutputHandler().addVarLevelWidget(request, sb, dataset,
                                CdmDataOutputHandler.ARG_LEVEL);
                    }
                }
            } else if (e.getTypeHandler() instanceof NoaaPsdMonthlyClimateIndexTypeHandler) {
                tsEntry = e;
            }
        }
        if (dataset == null) {
            throw new Exception("No grids found");
        }

        //addStatsWidget(request, sb);
        addTimeWidget(request, sb, input);

        LatLonRect llr = null;
        if (dataset != null) {
            llr = dataset.getBoundingBox();
        } else {
            llr = new LatLonRect(new LatLonPointImpl(90.0, -180.0),
                                 new LatLonPointImpl(-90.0, 180.0));
        }
        getOutputHandler().addMapWidget(request, sb, llr, false);
    }
    
    /**
     * Add a time widget
     *
     * @param request  the Request
     * @param sb       the HTML page
     * @param input    the input
     *
     * @throws Exception  problem making datasets
     */
    public void addTimeWidget(Request request, Appendable sb,
                              ServiceInput input)
            throws Exception {

        CDOOutputHandler.makeMonthsWidget(request, sb, null);
        //TODO: add in a lag widget
        makeYearsWidget(request, sb, input);
    }

    /**
     * Make the years widget
     * @param request the request
     * @param sb      the form
     * @param input   the data
     * @throws Exception problems
     */
    private void makeYearsWidget(Request request, Appendable sb,
            ServiceInput input) throws Exception {
        List<GridDataset> grids = new ArrayList<GridDataset>();
        Entry tsEntry = null;
        List<Entry> entries = input.getEntries();
        for (Entry e : entries) {
            if (e.getTypeHandler() instanceof ClimateModelFileTypeHandler) {
                CdmDataOutputHandler dataOutputHandler =
                    getOutputHandler().getDataOutputHandler();
                GridDataset dataset =
                    dataOutputHandler.getCdmManager().getGridDataset(e,
                    e.getResource().getPath());
                if (dataset != null) {
                    grids.add(dataset);
                }
            } else if (e.getTypeHandler() instanceof NoaaPsdMonthlyClimateIndexTypeHandler) {
                tsEntry = e;
            }
        }
        int grid = 0;
        int numGrids = grids.size();
        List<String> commonYears = new ArrayList<String>();
        for (GridDataset dataset : grids) {
            List<CalendarDate> dates =
                CdmDataOutputHandler.getGridDates(dataset);
            if ( !dates.isEmpty() && (grid == 0)) {
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
            SortedSet<String> uniqueYears =
                Collections.synchronizedSortedSet(new TreeSet<String>());
            if ((dates != null) && !dates.isEmpty()) {
                for (CalendarDate d : dates) {
                    try {  // shouldn't get an exception
                        String year =
                            new CalendarDateTime(d).formattedString("yyyy",
                                CalendarDateTime.DEFAULT_TIMEZONE);
                        uniqueYears.add(year);
                    } catch (Exception e) {}
                }
            }
            List<String> years = new ArrayList<String>(uniqueYears);
            // TODO:  make a better list of years
            if (years.isEmpty()) {
                for (int i = 1979; i <= 2012; i++) {
                    years.add(String.valueOf(i));
                }
            }
            if (grid == 0) {
                commonYears.addAll(years);
            } else {
                commonYears.retainAll(years);
            }
            if (grid < numGrids - 1) {
                grid++;
                continue;
            } else {
                years = commonYears;
            }
            grid++;
        }
        // Get the years from the time series
        List<String> tsYears = getTimeSeriesYears(request, tsEntry);
        
        // merge them into the common years
        commonYears.retainAll(tsYears);

        // Make the widget
        String yearNum = "";
        String yrLabel = Repository.msgLabel("Start");
        int endIndex = commonYears.size()-1;
        sb.append(HtmlUtils
            .formEntry(Repository.msgLabel("Years"), yrLabel
                + HtmlUtils
                    .select(CDOOutputHandler.ARG_CDO_STARTYEAR
                        + yearNum, commonYears, request
                            .getString(CDOOutputHandler.ARG_CDO_STARTYEAR
                                + yearNum, request
                                    .getString(CDOOutputHandler
                                        .ARG_CDO_STARTYEAR, commonYears
                                            .get(0))), HtmlUtils
                                                .title("Select the starting year")) + HtmlUtils
                                                    .space(3) + Repository
                                                        .msgLabel("End") + HtmlUtils
                                                            .select(CDOOutputHandler
                                                                .ARG_CDO_ENDYEAR + yearNum, commonYears, request
                                                                    .getString(CDOOutputHandler
                                                                        .ARG_CDO_ENDYEAR + yearNum, request
                                                                            .getString(CDOOutputHandler
                                                                                .ARG_CDO_ENDYEAR, commonYears
                                                                                    .get(endIndex))), HtmlUtils
                                                                                        .title("Select the ending year")))); 

    }
        
    /**
     * Get the list of years from the time series
     * @param tsEntry
     * @return
     */
    private List<String> getTimeSeriesYears(Request r, Entry tsEntry) throws Exception {
        TimeSeriesData tsd = getTimeSeriesData(r, tsEntry);
        List<String> tsYears = new ArrayList<String>();
        for (TimeSeriesRecord tsr : tsd.getRecords()) {
            if (Double.isNaN(tsr.getValue())) {
                continue;
            }
            String year = new DateTime(tsr.getDate().getTime()/1000).formattedString("yyyy",
                                 DateTime.DEFAULT_TIMEZONE);
            if (!tsYears.contains(year)) {
                tsYears.add(year);
            }
        }
        /*
        String start = new DateTime(tsEntry.getStartDate()/1000).formattedString("yyyy",
                                DateTime.DEFAULT_TIMEZONE);
        String end = new DateTime(tsEntry.getEndDate()/1000).formattedString("yyyy",
                                DateTime.DEFAULT_TIMEZONE);
        int startYear = Integer.parseInt(start);
        int endYear = Integer.parseInt(end);
        for (int i = 0; i < (endYear-startYear+1); i++) {
            tsYears.add(String.valueOf(startYear+i));
        }
        */
        return tsYears;
    }

    /**
     * Can we handle this input
     *
     * @param input  the input
     *
     * @return true if we can, otherwise false
     */
    public boolean canHandle(ServiceInput input) {
        if ( !getOutputHandler().isEnabled()) {
            return false;
        }

        List<ServiceOperand> ops = input.getOperands();
        // TODO: maybe call input.getEntries() and check on that
        // need one grid and one timeseries
        if (ops.size() < 2) return false;
        for (ServiceOperand op : ops) {
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
    protected boolean checkForValidEntries(List<Entry> entries) {
        // TODO: change this when we can handle more than one entry (e.g. daily data)
        if (entries.isEmpty()) {
            return false;
        }
        SortedSet<String> uniqueModels =
            Collections.synchronizedSortedSet(new TreeSet<String>());
        SortedSet<String> uniqueMembers =
            Collections.synchronizedSortedSet(new TreeSet<String>());
        boolean isTS = false;
        for (Entry entry : entries) {
            TypeHandler th = entry.getTypeHandler();
            if ( !(th instanceof ClimateModelFileTypeHandler ||
                    th instanceof NoaaPsdMonthlyClimateIndexTypeHandler)) {
                return false;
            }
            if (th instanceof ClimateModelFileTypeHandler) {
                uniqueModels.add(entry.getValue(1).toString());
                uniqueMembers.add(entry.getValue(3).toString());
            } else if (th instanceof NoaaPsdMonthlyClimateIndexTypeHandler) {
                isTS = true;
            }
        }
        if (!isTS) {
            // one model, one member
            if ((uniqueModels.size() == 1) && (uniqueMembers.size() == 1)) {
                return true;
            }
            return false; 
            /*  We don't handle these now.
            // multi-model multi-ensemble - don't want to think about this
            if ((uniqueModels.size() >= 1) && (uniqueMembers.size() > 1)) {
                return false;
            }
            // single model, multi-ensemble 
            if ((uniqueModels.size() > 1) && (uniqueMembers.size() > 1)) {
                return false;
            }
            */
        }
    
        return true;
    }

    /**
     * Process the request
     *
     * @param request  The request
     * @param info     the service info
     * @param input  the  data process input
     * @param argPrefix  the prefix for arguments
     *
     * @return  the processed data
     *
     * @throws Exception  problem processing
     */
    @Override
    public ServiceOutput evaluate(Request request, ServiceInput input,
                                  String argPrefix)
            throws Exception {

        if ( !canHandle(input)) {
            throw new Exception("Illegal data type");
        }

        List<ServiceOperand> outputEntries =
            new ArrayList<ServiceOperand>();
        List<Entry> gridEntries = new ArrayList<Entry>();
        List<Entry> tsEntries = new ArrayList<Entry>();
        for (Entry oneOfThem : input.getEntries()) {
            if (oneOfThem.getTypeHandler() instanceof ClimateModelFileTypeHandler) {
                gridEntries.add(oneOfThem);
            } else if (oneOfThem.getTypeHandler() instanceof NoaaPsdMonthlyClimateIndexTypeHandler) {
                tsEntries.add(oneOfThem);
            }
        }
        for (Entry tsEntry : tsEntries) {
            ServiceOperand so = processTimeSeriesData(request, input, tsEntry);
        }
        
        for (Entry gridEntry : gridEntries) {
            outputEntries.add(processModelData(request, input, gridEntry, 0));
        }

        return new ServiceOutput(outputEntries);
    }
    
    /**
     * Process the request
     *
     * @param request  the request
     * @param dpi      the ServiceInput
     * @param op       the operand
     * @param opNum    the operand number
     *
     * @return  some output
     *
     * @throws Exception Problem processing the monthly request
     */
    private ServiceOperand processModelData(Request request,
            ServiceInput dpi, Entry oneOfThem, int opNum)
            throws Exception {

        CdmDataOutputHandler dataOutputHandler =
            getOutputHandler().getDataOutputHandler();
        GridDataset dataset =
            dataOutputHandler.getCdmManager().getGridDataset(oneOfThem,
                oneOfThem.getResource().getPath());
        CalendarDateRange dateRange = dataset.getCalendarDateRange();
        int firstDataYearMM = Integer.parseInt(
                                new CalendarDateTime(
                                    dateRange.getStart()).formattedString(
                                    "yyyyMM",
                                    CalendarDateTime.DEFAULT_TIMEZONE));
        int firstDataYear = firstDataYearMM/100;
        int firstDataMonth = firstDataYearMM%100;
        int lastDataYearMM = Integer.parseInt(
                               new CalendarDateTime(
                                   dateRange.getEnd()).formattedString(
                                   "yyyyMM",
                                   CalendarDateTime.DEFAULT_TIMEZONE));
        int lastDataYear = lastDataYearMM/100;
        int lastDataMonth = lastDataYearMM%100;
        if ((dataset == null) || dataset.getGrids().isEmpty()) {
            throw new Exception("No grids found");
        }
        String varname  =
            ((GridDatatype) dataset.getGrids().get(0)).getName();

        Object[] values = oneOfThem.getValues(true);
        String tail =
            getOutputHandler().getStorageManager().getFileTail(oneOfThem);
        String       id        = getRepository().getGUID();
        String       newName = IOUtil.stripExtension(tail) + "_" + id + ".nc";
        File outFile = new File(IOUtil.joinDir(dpi.getProcessDir(), newName));
        List<String> commands  = initCDOService();

        // Select order (left to right) - operations go right to left:
        //   - stats
        //   - region
        //   - variable
        //   - month range
        //   - year or time range
        //   - level   (putting this first speeds things up)
        boolean spanYears = doMonthsSpanYearEnd(request, oneOfThem);
        if (!spanYears) {
            addStatServices(request, oneOfThem, commands);
        }
        getOutputHandler().addAreaSelectServices(request, oneOfThem,
                commands);
        commands.add("-remapbil,r360x180");
        //getOutputHandler().addLevelSelectServices(request, oneOfThem,
        //        commands, CdmDataOutputHandler.ARG_LEVEL);
        commands.add("-selname," + varname);
        // find the start & end month of the request
        int requestStartMonth = request.get(CDOOutputHandler.ARG_CDO_STARTMONTH, 1);
        int requestEndMonth = request.get(CDOOutputHandler.ARG_CDO_ENDMONTH, 1);
        // Handle the case where the months span the year end (e.g. DJF)
        // Break it up into two requests
        if (spanYears) {
            System.out.println("months span the year end");
            List<String> tmpFiles = new ArrayList<String>();
            String       opStr    = (opNum == 0)
                                    ? ""
                                    : "" + (opNum + 1);
            boolean      haveYears;
            if (opNum == 0) {
                haveYears = request.defined(CDOOutputHandler.ARG_CDO_YEARS);
            } else {
                haveYears = request.defined(CDOOutputHandler.ARG_CDO_YEARS
                                            + opStr);
            }
            if (haveYears) {
                List<Integer> years = new ArrayList<Integer>();
                String yearString = request.getString(
                                        CDOOutputHandler.ARG_CDO_YEARS
                                        + opStr, request.getString(
                                            CDOOutputHandler.ARG_CDO_YEARS,
                                            null));
                if (yearString != null) {
                    yearString = CDOOutputHandler.verifyYearsList(yearString);
                }
                List<String> yearList = StringUtil.split(yearString, ",",
                                            true, true);
                for (String year : yearList) {
                    int iyear = Integer.parseInt(year);
                    if (iyear <= firstDataYear || iyear > lastDataYear ||
                            (iyear == lastDataYear && requestEndMonth > lastDataMonth)) {
                        continue;
                    }
                    years.add(iyear);
                }
                for (int i = 0; i < 2; i++) {
                    List<String> savedServices = new ArrayList(commands);
                    Request      newRequest    = request.cloneMe();
                    newRequest.remove(CDOOutputHandler.ARG_CDO_STARTYEAR);
                    newRequest.remove(CDOOutputHandler.ARG_CDO_ENDYEAR);
                    newRequest.remove(CDOOutputHandler.ARG_CDO_STARTYEAR
                                      + opStr);
                    newRequest.remove(CDOOutputHandler.ARG_CDO_ENDYEAR
                                      + opStr);
                    if (i == 0) {  // last half of previous year
                        String yearsToUse = makeCDOYearsString(years, -1);
                        newRequest.put(CDOOutputHandler.ARG_CDO_ENDMONTH, 12);
                        newRequest.put(CDOOutputHandler.ARG_CDO_YEARS
                                       + opStr, yearsToUse);
                    } else {  // first half of current year
                        String yearsToUse = makeCDOYearsString(years, 0);
                        newRequest.put(CDOOutputHandler.ARG_CDO_STARTMONTH,
                                       1);
                        newRequest.put(CDOOutputHandler.ARG_CDO_YEARS
                                       + opStr, yearsToUse);
                    }
                    File tmpFile = new File(outFile.toString() + "." + i);
                    getOutputHandler().addDateSelectServices(newRequest,
                            oneOfThem, savedServices, opNum);
                    getOutputHandler().addLevelSelectServices(newRequest,
                            oneOfThem, savedServices,
                            CdmDataOutputHandler.ARG_LEVEL);
                    System.err.println("cmds:" + savedServices);
                    savedServices.add(oneOfThem.getResource().getPath());
                    savedServices.add(tmpFile.toString());
                    runProcess(savedServices, dpi.getProcessDir(), tmpFile);
                    tmpFiles.add(tmpFile.toString());
                }
            } else {
                int startYear = request.get(
                                    CDOOutputHandler.ARG_CDO_STARTYEAR
                                    + opStr, request.get(
                                        CDOOutputHandler.ARG_CDO_STARTYEAR,
                                        1979));
                int endYear =
                    request.get(CDOOutputHandler.ARG_CDO_ENDYEAR + opStr,
                                request.get(CDOOutputHandler.ARG_CDO_ENDYEAR,
                                            1979));
                // can't go back before the beginning of data or past the last data
                if (startYear <= firstDataYear) {
                    startYear = firstDataYear + 1;
                }
                if (endYear < lastDataYear) {
                    endYear = lastDataYear;
                }
                if (endYear == lastDataYear && requestEndMonth > lastDataMonth) {
                    endYear = lastDataYear - 1;
                }
                for (int i = 0; i < 2; i++) {
                    List<String> savedServices = new ArrayList(commands);
                    Request      newRequest    = request.cloneMe();
                    // just in case
                    newRequest.remove(CDOOutputHandler.ARG_CDO_YEARS);
                    newRequest.remove(CDOOutputHandler.ARG_CDO_YEARS + opStr);
                    if (i == 0) {  // last half of previous year
                        newRequest.put(CDOOutputHandler.ARG_CDO_ENDMONTH, 12);
                        newRequest.put(CDOOutputHandler.ARG_CDO_STARTYEAR
                                       + opStr, startYear - 1);
                        newRequest.put(CDOOutputHandler.ARG_CDO_ENDYEAR
                                       + opStr, endYear - 1);
                    } else {  // first half of current year
                        newRequest.put(CDOOutputHandler.ARG_CDO_STARTMONTH,
                                       1);
                        newRequest.put(CDOOutputHandler.ARG_CDO_STARTYEAR
                                       + opStr, startYear);
                        newRequest.put(CDOOutputHandler.ARG_CDO_ENDYEAR
                                       + opStr, endYear);
                    }
                    File tmpFile = new File(outFile.toString() + "." + i);
                    getOutputHandler().addDateSelectServices(newRequest,
                            oneOfThem, savedServices, opNum);
                    getOutputHandler().addLevelSelectServices(newRequest,
                            oneOfThem, savedServices,
                            CdmDataOutputHandler.ARG_LEVEL);
                    System.err.println("cmds:" + savedServices);
                    savedServices.add(oneOfThem.getResource().getPath());
                    savedServices.add(tmpFile.toString());
                    runProcess(savedServices, dpi.getProcessDir(), tmpFile);
                    tmpFiles.add(tmpFile.toString());
                }
            }
            // merge the files together
            File tmpFile = new File(outFile.toString() + ".tmp");
            commands = initCDOService();
            commands.add("-mergetime");
            for (String file : tmpFiles) {
                commands.add(file);
            }
            commands.add(tmpFile.toString());
            runProcess(commands, dpi.getProcessDir(), tmpFile);
            // now take the mean of the merged files
            commands = initCDOService();
            addStatServices(request, oneOfThem, commands);
            commands.add(tmpFile.toString());
            commands.add(outFile.toString());
            System.err.println(commands);
            runProcess(commands, dpi.getProcessDir(), outFile);

        } else {
            getOutputHandler().addDateSelectServices(request, oneOfThem,
                    commands, opNum);
            getOutputHandler().addLevelSelectServices(request, oneOfThem,
                    commands, CdmDataOutputHandler.ARG_LEVEL);

            System.err.println("cmds:" + commands);

            commands.add(oneOfThem.getResource().getPath());
            commands.add(outFile.toString());
            runProcess(commands, dpi.getProcessDir(), outFile);
        }


        StringBuilder outputName = new StringBuilder();
        // values = collection,model,experiment,ens,var
        // model
        outputName.append(values[1].toString().toUpperCase());
        outputName.append(" ");
        // experiment
        outputName.append(values[2]);
        outputName.append(" ");
        // ens
        String ens = values[3].toString();
        if (ens.equals("mean") || ens.equals("sprd") || ens.equals("clim")) {
            outputName.append("ens");
        }
        outputName.append(ens);
        outputName.append(" ");
        // var
        /*
        outputName.append(values[4]);
        outputName.append(" ");
        outputName.append(stat);
        outputName.append(" ");
        */

        String yearNum = (opNum == 0)
                         ? ""
                         : String.valueOf(opNum + 1);
        int    startMonth, endMonth;
        if (request.getString(
                CDOOutputHandler.ARG_CDO_MONTHS).equalsIgnoreCase("all")) {
            startMonth = 1;
            endMonth   = 12;
        } else {
            startMonth = request.defined(CDOOutputHandler.ARG_CDO_STARTMONTH)
                         ? request.get(CDOOutputHandler.ARG_CDO_STARTMONTH, 1)
                         : 1;
            endMonth = request.defined(CDOOutputHandler.ARG_CDO_ENDMONTH)
                       ? request.get(CDOOutputHandler.ARG_CDO_ENDMONTH,
                                     startMonth)
                       : startMonth;
        }
        if (startMonth == endMonth) {
            outputName.append(MONTHS[startMonth - 1]);
        } else {
            outputName.append(MONTHS[startMonth - 1]);
            outputName.append("-");
            outputName.append(MONTHS[endMonth - 1]);
        }
        outputName.append(" ");
        if (request.defined(CDOOutputHandler.ARG_CDO_YEARS + yearNum)) {
            outputName.append(
                request.getString(CDOOutputHandler.ARG_CDO_YEARS + yearNum));
        } else if (request.defined(CDOOutputHandler.ARG_CDO_YEARS)
                   && !(request.defined(
                       CDOOutputHandler.ARG_CDO_STARTYEAR
                       + yearNum) || request.defined(
                           CDOOutputHandler.ARG_CDO_ENDYEAR + yearNum))) {
            outputName.append(
                request.getString(CDOOutputHandler.ARG_CDO_YEARS));
        } else {
            String startYear =
                request.defined(CDOOutputHandler.ARG_CDO_STARTYEAR + yearNum)
                ? request.getString(CDOOutputHandler.ARG_CDO_STARTYEAR
                                    + yearNum)
                : request.defined(CDOOutputHandler.ARG_CDO_STARTYEAR)
                  ? request.getString(CDOOutputHandler.ARG_CDO_STARTYEAR, "")
                  : "";
            String endYear = request.defined(CDOOutputHandler.ARG_CDO_ENDYEAR
                                             + yearNum)
                             ? request.getString(
                                 CDOOutputHandler.ARG_CDO_ENDYEAR + yearNum)
                             : request.defined(
                                 CDOOutputHandler.ARG_CDO_ENDYEAR)
                               ? request.getString(
                                   CDOOutputHandler.ARG_CDO_ENDYEAR,
                                   startYear)
                               : startYear;
            if (startYear.equals(endYear)) {
                outputName.append(startYear);
            } else {
                outputName.append(startYear);
                outputName.append("-");
                outputName.append(endYear);
            }
        }
        //System.out.println("Name: " + outputName.toString());

        Resource resource = new Resource(outFile, Resource.TYPE_LOCAL_FILE);
        TypeHandler myHandler = getRepository().getTypeHandler("cdm_grid",
                                    false, true);
        Entry outputEntry = new Entry(myHandler, true, outputName.toString());
        outputEntry.setResource(resource);
        outputEntry.setValues(values);
        // Add in lineage and associations
        outputEntry.addAssociation(new Association(getRepository().getGUID(),
                "generated product", "product generated from",
                oneOfThem.getId(), outputEntry.getId()));
        getOutputHandler().getEntryManager().writeEntryXmlFile(request,
                outputEntry);

        return new ServiceOperand(outputName.toString(), outputEntry);
    }

    private void addStatServices(Request request, Entry oneOfThem,
                List<String> commands) throws Exception {
        // find the start & end month of the request
        int requestStartMonth = request.get(CDOOutputHandler.ARG_CDO_STARTMONTH, 1);
        int requestEndMonth = request.get(CDOOutputHandler.ARG_CDO_ENDMONTH, 1);
        int numMonths = 0;
        if (doMonthsSpanYearEnd(request, oneOfThem)) {
            int months1 = 12-requestStartMonth+1;
            int months2 = requestEndMonth;
            numMonths = months1+months2;
        } else {
            numMonths = requestEndMonth - requestStartMonth + 1;
        }
        String timeSelect = "-" + CDOOutputHandler.PERIOD_TIM + "selmean," + numMonths;
        commands.add(timeSelect);
    }
    
    private TimeSeriesData getTimeSeriesData(Request request, Entry tsEntry) throws Exception {
        //String url = "http://localhost/repository/entry/show?entryid=79e642ee-dffe-4848-8aae-241e614c0c95&getdata=Get%20Data&output=points.product&product=points.csv
        StringBuilder url = new StringBuilder();
        url.append(getRepository().getHttpProtocol());
        url.append("://");
        url.append(request.getServerName());
        url.append(":");
        url.append(request.getServerPort());
        url.append(getRepository().getUrlBase());
        url.append("/entry/show?entryid=");
        url.append(tsEntry.getId());
        url.append("&getdata=Get Data&output=points.product&product=points.csv");
        System.err.println(url.toString());
        String contents = IOUtil.readContents(url.toString());
        List<String> lines = StringUtil.split(contents, "\n", true, true);
        TimeSeriesData data = new TimeSeriesData(tsEntry.getName());
        for (String line : lines) {
            if (line.startsWith("#") || line.isEmpty()) {
                continue;
            }
            String[] toks = StringUtil.split(line, ",", 2);
            Date d = DateUtil.parse(toks[0]);
            double value = Misc.parseNumber(toks[1]);
            TimeSeriesRecord r = new TimeSeriesRecord(d,value);
            data.addRecord(r);
        }
        return data;
    }
    
    private Date[] getTimeSeriesDateRange(TimeSeriesData data) {
        Date minDate = new Date(Long.MAX_VALUE);
        Date maxDate = new Date(-Long.MAX_VALUE);
        for (TimeSeriesRecord r : data.getRecords()) {
            Date rd = r.getDate();
            if (Double.isNaN(r.getValue())) {
                continue;
            }
            if (rd.compareTo(minDate) < 0) {
                minDate = rd;
            }
            if (rd.compareTo(maxDate) > 0) {
                maxDate = rd;
            }
        }
        return new Date[] {minDate, maxDate};
    }
    
    private ServiceOperand processTimeSeriesData(Request request, ServiceInput input, Entry tsEntry) throws Exception {
        TimeSeriesData tsd = getTimeSeriesData(request, tsEntry);
        Date[] range = getTimeSeriesDateRange(tsd);
        // find the start & end month of the request
        int requestStartMonth = request.get(CDOOutputHandler.ARG_CDO_STARTMONTH, 1);
        int requestEndMonth = request.get(CDOOutputHandler.ARG_CDO_ENDMONTH, 1);
        int firstDataYearMM = Integer.parseInt(
                                new DateTime(range[0]).formattedString(
                                    "yyyyMM",
                                    CalendarDateTime.DEFAULT_TIMEZONE));
        int firstDataYear = firstDataYearMM/100;
        int firstDataMonth = firstDataYearMM%100;
        int lastDataYearMM = Integer.parseInt(
                               new DateTime(range[1]).formattedString(
                                   "yyyyMM",
                                   CalendarDateTime.DEFAULT_TIMEZONE));
        int lastDataYear = lastDataYearMM/100;
        int lastDataMonth = lastDataYearMM%100;
        int startYear = 0; 
        int endYear = 0;
        boolean spanYears = doMonthsSpanYearEnd(request, tsEntry);
        List<Integer> years = new ArrayList<Integer>();
        int numMonths;
        if (spanYears) {
            boolean      haveYears = request.defined(CDOOutputHandler.ARG_CDO_YEARS);
            if (haveYears) {
                String yearString = request.getString(
                                            CDOOutputHandler.ARG_CDO_YEARS,
                                            null);
                if (yearString != null) {
                    yearString = CDOOutputHandler.verifyYearsList(yearString);
                }
                List<String> yearList = StringUtil.split(yearString, ",",
                                            true, true);
                for (String year : yearList) {
                    int iyear = Integer.parseInt(year);
                    if (iyear <= firstDataYear || iyear > lastDataYear ||
                            (iyear == lastDataYear && requestEndMonth > lastDataMonth)) {
                        continue;
                    }
                    years.add(iyear);
                }
            } else {
                startYear = request.get(CDOOutputHandler.ARG_CDO_STARTYEAR, 1979);
                endYear =
                    request.get(CDOOutputHandler.ARG_CDO_ENDYEAR, startYear);
                // can't go back before the beginning of data or past the last data
                if (startYear <= firstDataYear) {
                    startYear = firstDataYear + 1;
                }
                if (endYear > lastDataYear) {
                    endYear = lastDataYear;
                }
                if (endYear == lastDataYear && requestEndMonth > lastDataMonth) {
                    endYear = lastDataYear - 1;
                }
                years = makeYears(startYear, endYear);
                
            }
            numMonths = requestEndMonth + (12-requestStartMonth+1);
        } else {
            numMonths = requestEndMonth-requestStartMonth+1;
            startYear = request.get(CDOOutputHandler.ARG_CDO_STARTYEAR, 1979);
            endYear = request.get(CDOOutputHandler.ARG_CDO_ENDYEAR, startYear);
            if (requestEndMonth > lastDataMonth) {
                endYear -= 1;
            }
            if (startYear < firstDataYear) {
                startYear = firstDataYear;
            }
            years = makeYears(startYear, endYear);
            
        }
        List<TimeSeriesRecord> subset = new ArrayList<TimeSeriesRecord>();
        GregorianCalendar cal = new GregorianCalendar(TimeZone.getTimeZone("GMT"));
        GregorianCalendar cal2 = new GregorianCalendar(TimeZone.getTimeZone("GMT"));
        cal.set(GregorianCalendar.DAY_OF_MONTH, 1);
        cal.set(GregorianCalendar.HOUR_OF_DAY, 0);
        cal.set(GregorianCalendar.MINUTE, 0);
        cal.set(GregorianCalendar.SECOND, 0);
        cal.set(GregorianCalendar.MILLISECOND, 0);
        cal.set(GregorianCalendar.YEAR, years.get(years.size()-1));
        cal.set(GregorianCalendar.MONTH, requestEndMonth-1);
        Date endDate = cal.getTime();
        cal.set(GregorianCalendar.YEAR, years.get(0));
        cal.set(GregorianCalendar.MONTH, requestStartMonth-1);
        Date startDate = cal.getTime();
        System.out.println("start: "+startDate);
        System.out.println("end: "+endDate);
        int mcntr = 0;
        int ycntr = 0;
        int maxyears = years.size()-1;
        for (TimeSeriesRecord tsr : tsd.getRecords()) {
            Date d = tsr.getDate();
            if (d.compareTo(startDate) < 0 ||
                d.compareTo(endDate) > 0) {
                continue;
            }
            cal2.setTime(d);
            if (cal2.get(GregorianCalendar.YEAR) < cal.get(GregorianCalendar.YEAR)) {
                continue;
            }
            if (cal2.get(GregorianCalendar.MONTH) < cal.get(GregorianCalendar.MONTH)) {
                continue;
            }
            System.err.println(d);
            subset.add(tsr);
            if (mcntr < numMonths-1) {
                cal.add(GregorianCalendar.MONTH, 1);
                mcntr++;
            } else if (ycntr < maxyears) {
                ycntr++;
                cal.set(GregorianCalendar.YEAR, years.get(ycntr));
                cal.set(GregorianCalendar.MONTH, requestStartMonth-1);
                mcntr = 0;
            }
        }
        return null; 
    }
    
    private List<Integer> makeYears(int start, int end) {
        List<Integer> years = new ArrayList<Integer>();
        for (int i = start; i <= end; i++) {
            years.add(i);
        }
        return years;
    }
    
    
    class TimeSeriesData {
        
        List<TimeSeriesRecord> records = new ArrayList<TimeSeriesRecord>();
        String name = null;
        double missingValue = Double.NaN;
        
        TimeSeriesData(String name) {
            this.name = name;
        }
        
        public List<TimeSeriesRecord> getRecords() {
            return records;
        }
        
        public String getName() {
            return name;
        }
        
        public void addRecord(TimeSeriesRecord record) {
            records.add(record);
        }
    }
    
    class TimeSeriesRecord {
        
        Date date;
        double value;
        
        TimeSeriesRecord(Date d, double v) {
            date = d;
            value = v;
        }
        
        public Date getDate() {
            return date;
        }
        
        public double getValue() {
            return value;
        }
        
    }
}
