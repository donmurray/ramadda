/*
 * Copyright (c) 2008-2015 Geode Systems LLC
 * This Software is licensed under the Geode Systems RAMADDA License available in the source distribution in the file 
 * ramadda_license.txt. The above copyright notice shall be included in all copies or substantial portions of the Software.
 */

package org.ramadda.geodata.model;


import org.ramadda.geodata.cdmdata.CdmDataOutputHandler;
import org.ramadda.repository.ApiMethod;
import org.ramadda.repository.Association;
import org.ramadda.repository.Entry;
import org.ramadda.repository.Repository;
import org.ramadda.repository.Request;
import org.ramadda.repository.RequestHandler;
import org.ramadda.repository.Resource;
import org.ramadda.repository.type.GranuleTypeHandler;
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
import ucar.unidata.util.IOUtil;
import ucar.unidata.util.StringUtil;
import ucar.unidata.util.TwoFacedObject;

import ucar.visad.data.CalendarDateTime;

import visad.util.ThreadManager;


import java.io.File;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.SortedSet;
import java.util.TreeSet;


/**
 * Service for area statistics using CDO
 */
public class CDOArealStatisticsProcess extends CDODataProcess {

    /** the type of request */
    String type = null;

    /**
     * Area statistics DataProcess
     *
     * @param repository  the Repository
     *
     * @throws Exception  problems
     */
    public CDOArealStatisticsProcess(Repository repository) throws Exception {
        super(repository, "CDO_AREA_STATS", "Area Statistics");
    }

    /**
     * Initialize the form javascript
     *
     * @param request  the request
     * @param js       the javascript
     * @param formVar  the form variable
     *
     * @throws Exception problems
     */
    public void initFormJS(Request request, Appendable js, String formVar)
            throws Exception {
        js.append(formVar
                  + ".addService(new CDOArealStatisticsService());\n");
    }


    /**
     * Add to form
     *
     * @param request  the Request
     * @param input    the ServiceInput
     * @param sb       the form
     * @param argPrefix  the argument prefix
     * @param label      the label
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
     * @param argPrefix  the argument prefix
     *
     * @throws Exception  problem making stuff
     */
    private void makeInputForm(Request request, ServiceInput input,
                               Appendable sb, String argPrefix)
            throws Exception {

        List<NamedTimePeriod> periods = null;
        ApiMethod             api     = request.getApiMethod();
        if (api != null) {
            RequestHandler handler = api.getRequestHandler();
            if ((handler != null)
                    && (handler instanceof ClimateModelApiHandler)) {
                String group = null;
                if (request.defined(ClimateModelApiHandler.ARG_EVENT_GROUP)) {
                    group = request.getString(
                        ClimateModelApiHandler.ARG_EVENT_GROUP, null);
                    periods =
                        ((ClimateModelApiHandler) handler)
                            .getNamedTimePeriods(group);
                }
            }
        }

        Entry first = input.getEntries().get(0);

        CdmDataOutputHandler dataOutputHandler =
            getOutputHandler().getDataOutputHandler();
        GridDataset dataset =
            dataOutputHandler.getCdmManager().getGridDataset(first,
                first.getResource().getPath());

        if (dataset != null) {
            getOutputHandler().addVarLevelWidget(request, sb, dataset,
                    CdmDataOutputHandler.ARG_LEVEL);
        }

        addStatsWidget(request, sb);

        addTimeWidget(request, sb, input, periods);

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
     * Process the request
     *
     * @param request  The request
     * @param input  the  data process input
     * @param argPrefix the argument prefix
     *
     * @return  the processed data
     *
     * @throws Exception  problem processing
     */
    @Override
    public ServiceOutput evaluate(Request request, ServiceInput input,
                                  String argPrefix)
            throws Exception {

        final Request      myRequest = request;
        final ServiceInput myInput   = input;
        final String       argP      = argPrefix;

        if ( !canHandle(input)) {
            throw new Exception("Illegal data type");
        }
        String type =
            input.getProperty(
                "type", ClimateModelApiHandler.ARG_ACTION_COMPARE).toString();


        final List<ServiceOperand> outputEntries =
            new ArrayList<ServiceOperand>();
        int     opNum      = 0;
        int     numThreads = Math.min(input.getOperands().size(), 6);
        boolean useThreads = false || (numThreads > 2);
        System.err.println("Using threads: " + useThreads);
        ThreadManager threadManager =
            new ThreadManager("CDOArealStatistics.evaluate");
        for (final ServiceOperand op : input.getOperands()) {
            Entry oneOfThem = op.getEntries().get(0);
            Entry collection = GranuleTypeHandler.getCollectionEntry(request,
                                   oneOfThem);
            String frequency = "Monthly";
            if (collection != null) {
                //frequency = collection.getValues()[0].toString();
                frequency = collection.getValue(0).toString();
            }
            if (frequency.toLowerCase().indexOf("mon") >= 0) {
                if ( !useThreads) {
                    outputEntries.add(processMonthlyRequest(request, input,
                            op, opNum));
                } else {
                    final int myOp = opNum;
                    System.out.println("making thread " + opNum);
                    threadManager.addRunnable(new ThreadManager.MyRunnable() {
                        public void run() throws Exception {
                            try {
                                ServiceOperand so =
                                    processMonthlyRequest(myRequest, myInput,
                                        op, myOp);
                                if (so != null) {
                                    synchronized (outputEntries) {
                                        outputEntries.add(so);
                                    }
                                }
                            } catch (Exception ve) {
                                ve.printStackTrace();
                            }
                        }
                    });
                }

            }
            if (input.getOperands().size() <= 2) {
                opNum++;
            }
        }
        if (useThreads) {
            try {
                System.out.println("Running in " + numThreads + " threads");
                threadManager.runInParallel(numThreads);
            } catch (Exception ve) {
                ve.printStackTrace();
            }
        }
        if (type.equals(ClimateModelApiHandler.ARG_ACTION_MULTI_COMPARE)
                || type.equals(
                    ClimateModelApiHandler.ARG_ACTION_ENS_COMPARE)) {
            addEnsembleMean(request, input, outputEntries, type);
        }

        return new ServiceOutput(outputEntries);
    }

    /**
     * Add the ensemble mean to list of output entries
     *
     * @param request the request
     * @param dpi     the service input
     * @param ops     the service operands
     * @param type    the type of request
     *
     * @throws Exception problems
     */
    public void addEnsembleMean(Request request, ServiceInput dpi,
                                List<ServiceOperand> ops, String type)
            throws Exception {
        Entry         oneOfThem = ops.get(0).getEntries().get(0);
        Object[]      values    = oneOfThem.getValues(true);
        StringBuilder fileName  = new StringBuilder();
        fileName.append(oneOfThem.getValue(4));
        fileName.append("_MultiModel_");
        fileName.append(oneOfThem.getValue(2));
        fileName.append("_mean_");
        String id      = getRepository().getGUID();
        String newName = fileName + id + ".nc";
        /*
        String tail =
            getOutputHandler().getStorageManager().getFileTail(oneOfThem);
        String       id        = getRepository().getGUID();
        String       newName = IOUtil.stripExtension(tail) + "_MMM_" + id + ".nc";
        */
        File outFile = new File(IOUtil.joinDir(dpi.getProcessDir(), newName));
        List<String> commands = initCDOService();
        commands.add("-ensmean");
        for (ServiceOperand op : ops) {
            List<Entry> entries = op.getEntries();
            for (Entry e : entries) {
                commands.add(e.getResource().getPath());
            }
        }
        commands.add(outFile.toString());
        System.out.println(commands);
        StringBuilder outputName = new StringBuilder();
        outputName.append("Multi-Model Ensemble Mean");
        runProcess(commands, dpi.getProcessDir(), outFile);
        Resource resource = new Resource(outFile, Resource.TYPE_LOCAL_FILE);
        TypeHandler myHandler = getRepository().getTypeHandler("cdm_grid",
                                    false, true);
        Entry outputEntry = new Entry(myHandler, true, outputName.toString());
        outputEntry.setResource(resource);
        Object[] newValues = values;
        newValues[1] = "MultiModel";
        newValues[3] = "mean";
        outputEntry.setValues(newValues);
        // Add in lineage and associations
        outputEntry.addAssociation(new Association(getRepository().getGUID(),
                "generated product", "product generated from",
                oneOfThem.getId(), outputEntry.getId()));
        getOutputHandler().getEntryManager().writeEntryXmlFile(request,
                outputEntry);
        ops.add(new ServiceOperand(outputName.toString(), outputEntry));
    }

    /**
     * Process the daily data request
     *
     * @param request  the request
     * @param dpi      the ServiceInput
     *
     * @return  some output
     *
     * @throws Exception problem processing the daily data
     */
    private Entry processDailyRequest(Request request, ServiceInput dpi)
            throws Exception {
        throw new Exception("can't handle daily data yet");
    }

    /**
     * Process the monthly request
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
    private ServiceOperand processMonthlyRequest(Request request,
            ServiceInput dpi, ServiceOperand op, int opNum)
            throws Exception {

        Entry oneOfThem = op.getEntries().get(0);
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
        int firstDataYear  = firstDataYearMM / 100;
        int firstDataMonth = firstDataYearMM % 100;
        int lastDataYearMM = Integer.parseInt(
                                 new CalendarDateTime(
                                     dateRange.getEnd()).formattedString(
                                     "yyyyMM",
                                     CalendarDateTime.DEFAULT_TIMEZONE));
        int lastDataYear  = lastDataYearMM / 100;
        int lastDataMonth = lastDataYearMM % 100;
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

        String       stat = request.getString(CDOOutputHandler.ARG_CDO_STAT);
        Entry        climEntry = null;
        if (stat.equals(CDOOutputHandler.STAT_ANOM)) {
            System.err.println("Looking for climo");
            List<Entry> climo = findClimatology(request, oneOfThem);
            if ((climo == null) || climo.isEmpty()) {
                System.err.println("Couldn't find one - making it");
                climEntry = makeClimatology(request, oneOfThem, dpi, tail);
                //throw new Exception("Unable to find climatology for "
                //                    + oneOfThem.getName());
            } else if (climo.size() > 1) {
                System.err.println("found too many");
            } else {
                climEntry = climo.get(0);
                System.err.println("found climo: " + climEntry);
            }
        }

        // Select order (left to right) - operations go right to left:
        //   - stats
        //   - region
        //   - variable
        //   - month range
        //   - year or time range
        //   - level   (putting this first speeds things up)
        getOutputHandler().addStatServices(request, oneOfThem, commands);
        getOutputHandler().addAreaSelectServices(request, oneOfThem,
                commands);
        commands.add("-remapbil,r360x180");
        //getOutputHandler().addLevelSelectServices(request, oneOfThem,
        //        commands, CdmDataOutputHandler.ARG_LEVEL);
        commands.add("-selname," + varname);
        String       opStr    = (opNum == 0)
                                ? ""
                                : "" + (opNum + 1);
        Request timeRequest = handleNamedTimePeriod(request, opStr);
        // Handle the case where the months span the year end (e.g. DJF)
        // Break it up into two requests
        if (doMonthsSpanYearEnd(timeRequest, oneOfThem)) {
            System.out.println("months span the year end");
            List<String> tmpFiles = new ArrayList<String>();
            boolean      haveYears;
            // find the start & end month of the request
            int requestStartMonth =
                timeRequest.get(CDOOutputHandler.ARG_CDO_STARTMONTH, 1);
            int requestEndMonth =
                timeRequest.get(CDOOutputHandler.ARG_CDO_ENDMONTH, 1);
            //if (opNum == 0) {
            //    haveYears = timeRequest.defined(CDOOutputHandler.ARG_CDO_YEARS);
            //} else {
            haveYears = timeRequest.defined(CDOOutputHandler.ARG_CDO_YEARS
                                            + opStr);
                /*
                        (request.defined(CDOOutputHandler.ARG_CDO_YEARS) &&
                         !(request.defined(CDOOutputHandler.ARG_CDO_STARTYEAR+opStr) &&
                           request.defined(CDOOutputHandler.ARG_CDO_ENDYEAR+opStr)));
                           */
            //}
            if (haveYears) {
                List<Integer> years = new ArrayList<Integer>();
                String yearString = timeRequest.getString(
                                        CDOOutputHandler.ARG_CDO_YEARS
                                        + opStr, timeRequest.getString(
                                            CDOOutputHandler.ARG_CDO_YEARS,
                                            null));
                if (yearString != null) {
                    yearString = CDOOutputHandler.verifyYearsList(yearString);
                }
                List<String> yearList = StringUtil.split(yearString, ",",
                                            true, true);
                for (String year : yearList) {
                    int iyear = Integer.parseInt(year);
                    if ((iyear <= firstDataYear) || (iyear > lastDataYear)
                            || ((iyear == lastDataYear)
                                && (requestEndMonth > lastDataMonth))) {
                        continue;
                    }
                    years.add(Integer.parseInt(year));
                }
                for (int i = 0; i < 2; i++) {
                    List<String> savedServices = new ArrayList(commands);
                    Request      newRequest    = timeRequest.cloneMe();
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
                int startYear = timeRequest.get(
                                    CDOOutputHandler.ARG_CDO_STARTYEAR
                                    + opStr, timeRequest.get(
                                        CDOOutputHandler.ARG_CDO_STARTYEAR,
                                        1979));
                int endYear =
                    timeRequest.get(CDOOutputHandler.ARG_CDO_ENDYEAR + opStr,
                                timeRequest.get(CDOOutputHandler.ARG_CDO_ENDYEAR,
                                            1979));
                // can't go back before the beginning of data or past the last data
                if (startYear <= firstDataYear) {
                    startYear = firstDataYear + 1;
                }
                if (endYear > lastDataYear) {
                    endYear = lastDataYear;
                }
                if ((endYear == lastDataYear)
                        && (requestEndMonth > lastDataMonth)) {
                    endYear = lastDataYear - 1;
                }
                for (int i = 0; i < 2; i++) {
                    List<String> savedServices = new ArrayList(commands);
                    Request      newRequest    = timeRequest.cloneMe();
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
            commands.add("-timmean");
            commands.add(tmpFile.toString());
            commands.add(outFile.toString());
            runProcess(commands, dpi.getProcessDir(), outFile);

        } else {
            getOutputHandler().addDateSelectServices(timeRequest, oneOfThem,
                    commands, opNum);
            getOutputHandler().addLevelSelectServices(timeRequest, oneOfThem,
                    commands, CdmDataOutputHandler.ARG_LEVEL);

            System.err.println("cmds:" + commands);

            commands.add(oneOfThem.getResource().getPath());
            commands.add(outFile.toString());
            runProcess(commands, dpi.getProcessDir(), outFile);
        }

        if (climEntry != null) {
            String climName = IOUtil.stripExtension(tail) + "_" + id
                              + "_clim.nc";
            File climFile = new File(IOUtil.joinDir(dpi.getProcessDir(),
                                climName));
            commands = initCDOService();

            // Select order (left to right) - operations go right to left:
            //   - region
            //   - level
            //   - month range
            getOutputHandler().addStatServices(timeRequest, climEntry, commands);
            getOutputHandler().addAreaSelectServices(timeRequest, climEntry,
                    commands);
            commands.add("-remapbil,r360x180");
            commands.add("-selname," + varname);
            getOutputHandler().addMonthSelectServices(timeRequest, climEntry,
                    commands);
            getOutputHandler().addLevelSelectServices(timeRequest, climEntry,
                    commands, CdmDataOutputHandler.ARG_LEVEL);

            //System.err.println("clim cmds:" + commands);

            commands.add(climEntry.getResource().getPath());
            commands.add(climFile.toString());
            runProcess(commands, dpi.getProcessDir(), climFile);

            // now subtract them
            String anomName = IOUtil.stripExtension(tail) + "_" + id
                              + "_anom.nc";
            File anomFile = new File(IOUtil.joinDir(dpi.getProcessDir(),
                                anomName));
            commands = initCDOService();
            // We use sub instead of ymonsub because there is only one value in each file and
            // CDO sets the time of the merged files to be the last time. 
            //commands.add("-ymonsub");
            commands.add("-sub");
            commands.add(outFile.toString());
            commands.add(climFile.toString());
            commands.add(anomFile.toString());
            runProcess(commands, dpi.getProcessDir(), anomFile);
            outFile = anomFile;
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
        if (timeRequest.getString(
                CDOOutputHandler.ARG_CDO_MONTHS).equalsIgnoreCase("all")) {
            startMonth = 1;
            endMonth   = 12;
        } else {
            startMonth = timeRequest.defined(CDOOutputHandler.ARG_CDO_STARTMONTH)
                         ? timeRequest.get(CDOOutputHandler.ARG_CDO_STARTMONTH, 1)
                         : 1;
            endMonth = timeRequest.defined(CDOOutputHandler.ARG_CDO_ENDMONTH)
                       ? timeRequest.get(CDOOutputHandler.ARG_CDO_ENDMONTH,
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
        if (timeRequest.defined(CDOOutputHandler.ARG_CDO_YEARS + yearNum)) {
            outputName.append(
                timeRequest.getString(CDOOutputHandler.ARG_CDO_YEARS + yearNum));
        } else if (timeRequest.defined(CDOOutputHandler.ARG_CDO_YEARS)
                   && !(timeRequest.defined(
                       CDOOutputHandler.ARG_CDO_STARTYEAR
                       + yearNum) || timeRequest.defined(
                           CDOOutputHandler.ARG_CDO_ENDYEAR + yearNum))) {
            outputName.append(
                timeRequest.getString(CDOOutputHandler.ARG_CDO_YEARS));
        } else {
            String startYear =
                timeRequest.defined(CDOOutputHandler.ARG_CDO_STARTYEAR + yearNum)
                ? timeRequest.getString(CDOOutputHandler.ARG_CDO_STARTYEAR
                                    + yearNum)
                : timeRequest.defined(CDOOutputHandler.ARG_CDO_STARTYEAR)
                  ? timeRequest.getString(CDOOutputHandler.ARG_CDO_STARTYEAR, "")
                  : "";
            String endYear = timeRequest.defined(CDOOutputHandler.ARG_CDO_ENDYEAR
                                             + yearNum)
                             ? timeRequest.getString(
                                 CDOOutputHandler.ARG_CDO_ENDYEAR + yearNum)
                             : timeRequest.defined(
                                 CDOOutputHandler.ARG_CDO_ENDYEAR)
                               ? timeRequest.getString(
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
        if (climEntry != null) {
            outputEntry.addAssociation(
                new Association(
                    getRepository().getGUID(), "generated product",
                    "product generated from", climEntry.getId(),
                    outputEntry.getId()));
        }
        getOutputHandler().getEntryManager().writeEntryXmlFile(timeRequest,
                outputEntry);

        //return new ServiceOperand(outputEntry.getName(), outputEntry);
        return new ServiceOperand(outputName.toString(), outputEntry);
    }


    private Request handleNamedTimePeriod(Request request, String opStr) {
        if (!request.defined(ClimateModelApiHandler.ARG_EVENT)) {
            return request;
        }
        Request newRequest = request.cloneMe();
        String eventString = newRequest.getString(ClimateModelApiHandler.ARG_EVENT);
        if (eventString == null) {
            return request;
        }
        List<String> toks = StringUtil.split(eventString, ";");
        if (toks.size() != 4) {
            System.err.println("Bad named time period: " + eventString);
            return request;
        }
        newRequest.remove(ClimateModelApiHandler.ARG_EVENT);
        newRequest.put(CDOOutputHandler.ARG_CDO_STARTMONTH, toks.get(1));
        newRequest.put(CDOOutputHandler.ARG_CDO_ENDMONTH, toks.get(2));
        String years = toks.get(3);
        if (years.indexOf("/")>0) {
            List<String> ytoks = StringUtil.split(years, "/");
            newRequest.put(CDOOutputHandler.ARG_CDO_STARTYEAR+opStr, ytoks.get(0));
            newRequest.put(CDOOutputHandler.ARG_CDO_ENDYEAR+opStr, ytoks.get(1));
        } else {
            newRequest.put(CDOOutputHandler.ARG_CDO_YEARS+opStr, toks.get(3));
        }
        return newRequest;
    }

    /**
     * Check for valid entries
     * @param entries  list of entries
     * @return
     */
    protected boolean checkForValidEntries(List<Entry> entries) {
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
     * Add the statitics widget
     *
     * @param request  the Request
     * @param sb       the HTML
     *
     * @throws Exception problems
     */
    public void addStatsWidget(Request request, Appendable sb)
            throws Exception {

        sb.append(
            HtmlUtils.hidden(
                CDOOutputHandler.ARG_CDO_PERIOD,
                request.getString(
                    CDOOutputHandler.ARG_CDO_PERIOD,
                    CDOOutputHandler.PERIOD_TIM)));
        super.addStatsWidget(request, sb);
        /*
        sb.append(
            HtmlUtils.formEntry(
                Repository.msgLabel("Statistic"),
                HtmlUtils.select(
                    CDOOutputHandler.ARG_CDO_STAT,
                    CDOOutputHandler.STAT_TYPES,
                    request.getString(CDOOutputHandler.ARG_CDO_STAT, null))));
                    */
    }

    /**
     * Add a time widget
     *
     * @param request  the Request
     * @param sb       the HTML page
     * @param input    the input
     * @param periods  the time periods
     *
     * @throws Exception  problem making datasets
     */
    public void addTimeWidget(Request request, Appendable sb,
                              ServiceInput input,
                              List<NamedTimePeriod> periods)
            throws Exception {

        String type =
            input.getProperty(
                "type", ClimateModelApiHandler.ARG_ACTION_COMPARE).toString();
        List<GridDataset> grids = new ArrayList<GridDataset>();
        for (ServiceOperand op : input.getOperands()) {
            Entry first = op.getEntries().get(0);
            CdmDataOutputHandler dataOutputHandler =
                getOutputHandler().getDataOutputHandler();
            GridDataset dataset =
                dataOutputHandler.getCdmManager().getGridDataset(first,
                    first.getResource().getPath());
            if (dataset != null) {
                grids.add(dataset);
            }

        }
        if ((periods == null) || (periods.isEmpty())) {
            CDOOutputHandler.makeMonthsWidget(request, sb, null);
            makeYearsWidget(request, sb, grids, type);
        } else {
            makeEventsWidget(request, sb, periods, type);
        }
    }

    /**
     * Make a widget for named time periods
     *
     * @param request  the request
     * @param sb       the form
     * @param periods  the periods
     * @param type     the type of request
     *
     * @throws Exception problems
     */
    private void makeEventsWidget(Request request, Appendable sb,
                                  List<NamedTimePeriod> periods, String type)
            throws Exception {
        String group =
            request.getString(ClimateModelApiHandler.ARG_EVENT_GROUP, null);
        List<TwoFacedObject> values        = new ArrayList<TwoFacedObject>();
        NamedTimePeriod      selectedEvent = periods.get(0);
        String               event         = null;
        if (request.defined(ClimateModelApiHandler.ARG_EVENT)) {
            event = request.getString(ClimateModelApiHandler.ARG_EVENT);
        }
        for (NamedTimePeriod period : periods) {
            String value = period.getId() + ";" + period.getStartMonth()
                           + ";" + period.getEndMonth() + ";"
                           + period.getYears();
            TwoFacedObject item = new TwoFacedObject(period.getName(), value);
            values.add(item);
        }
        /*
        sb.append(HtmlUtils.hidden(CDOOutputHandler.ARG_CDO_STARTMONTH,
                                   selectedEvent.getStartMonth()));
        sb.append(HtmlUtils.hidden(CDOOutputHandler.ARG_CDO_ENDMONTH,
                                   selectedEvent.getEndMonth()));
        sb.append(HtmlUtils.hidden(CDOOutputHandler.ARG_CDO_YEARS,
                                   selectedEvent.getYears()));
        */
        if ((group == null) || group.equals("all")) {
            group = "Events";
        }
        sb.append(
            HtmlUtils.formEntry(
                Repository.msgLabel(group),
                HtmlUtils.select(
                    ClimateModelApiHandler.ARG_EVENT, values, event)));

    }

    /**
     * Add the year selection widget
     *
     * @param request  the Request
     * @param sb       the StringBuilder to add to
     * @param grids    list of grids to use
     * @param type     the type of request
     *
     * @throws Exception problems
     */
    private void makeYearsWidget(Request request, Appendable sb,
                                 List<GridDataset> grids, String type)
            throws Exception {

        int grid     = 0;
        int numGrids = grids.size();
        boolean handleMultiple =
            type.equals(ClimateModelApiHandler.ARG_ACTION_MULTI_COMPARE)
            || type.equals(ClimateModelApiHandler.ARG_ACTION_ENS_COMPARE);
        /* If we are doing a compare, we make widgets for each grid.  If we are doing
         * a multi compare, we make one from the intersection of all grids
         */
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
            if (handleMultiple) {
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
            }

            String yearNum = ((grid == 0) || handleMultiple)
                             ? ""
                             : String.valueOf(grid + 1);
            String yrLabel = ((grids.size() == 1) || handleMultiple)
                             ? "Start"
                             : (grid == 0)
                               ? "First Dataset:<br>Start"
                               : "Second Dataset:<br>Start";
            yrLabel = Repository.msgLabel(yrLabel);
            if ((grid > 0)
                    && type.equals(
                        ClimateModelApiHandler.ARG_ACTION_COMPARE)) {
                years.add(0, "");
            }
            int endIndex = 0;
            //int endIndex = (grid == 0)
            //               ? years.size() - 1
            //               : 0;

            sb.append(HtmlUtils
                .formEntry(Repository.msgLabel("Years"), yrLabel
                    + HtmlUtils
                        .select(CDOOutputHandler.ARG_CDO_STARTYEAR
                            + yearNum, years, request
                                .getString(CDOOutputHandler.ARG_CDO_STARTYEAR
                                    + yearNum, request
                                        .getString(CDOOutputHandler
                                            .ARG_CDO_STARTYEAR, years
                                                .get(0))), HtmlUtils
                                                    .title("Select the starting year")) + HtmlUtils
                                                        .space(3) + Repository
                                                            .msgLabel("End") + HtmlUtils
                                                                .select(CDOOutputHandler
                                                                    .ARG_CDO_ENDYEAR + yearNum, years, request
                                                                        .getString(CDOOutputHandler
                                                                            .ARG_CDO_ENDYEAR + yearNum, request
                                                                                .getString(CDOOutputHandler
                                                                                    .ARG_CDO_ENDYEAR, years
                                                                                        .get(endIndex))), HtmlUtils
                                                                                            .title("Select the ending year")) + HtmlUtils
                                                                                                .p() + Repository
                                                                                                    .msgLabel("or List") + HtmlUtils
                                                                                                        .input(CDOOutputHandler
                                                                                                            .ARG_CDO_YEARS + yearNum, request
                                                                                                                .getString(CDOOutputHandler
                                                                                                                    .ARG_CDO_YEARS + yearNum, ""), 20, HtmlUtils
                                                                                                                        .title("Input a set of years separated by commas (e.g. 1980,1983,2012)")) + HtmlUtils
                                                                                                                            .space(2) + Repository
                                                                                                                                .msg("(comma separated)")));
            grid++;
        }

    }

}
