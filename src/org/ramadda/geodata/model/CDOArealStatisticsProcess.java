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


import org.ramadda.data.process.DataProcessInput;
import org.ramadda.data.process.DataProcessOperand;
import org.ramadda.data.process.DataProcessOutput;
import org.ramadda.geodata.cdmdata.CdmDataOutputHandler;
import org.ramadda.repository.Association;
import org.ramadda.repository.Entry;
import org.ramadda.repository.Repository;
import org.ramadda.repository.Request;
import org.ramadda.repository.Resource;
import org.ramadda.repository.type.GranuleTypeHandler;
import org.ramadda.repository.type.TypeHandler;
import org.ramadda.util.HtmlUtils;

import ucar.nc2.dt.GridDatatype;
import ucar.nc2.dt.GridDatatype;

import ucar.nc2.dt.GridDatatype;
import ucar.nc2.dt.grid.GridDataset;
import ucar.nc2.time.Calendar;
import ucar.nc2.time.CalendarDate;

import ucar.unidata.geoloc.LatLonPointImpl;
import ucar.unidata.geoloc.LatLonRect;
import ucar.unidata.util.IOUtil;
import ucar.unidata.util.StringUtil;

import ucar.visad.data.CalendarDateTime;


import java.io.File;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.SortedSet;
import java.util.TreeSet;


/**
 * DataProcess for area statistics using CDO
 */
public class CDOArealStatisticsProcess extends CDODataProcess {


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
     * _more_
     *
     * @param request _more_
     * @param js _more_
     * @param formVar _more_
     *
     * @throws Exception _more_
     */
    public void initFormJS(Request request, StringBuilder js, String formVar)
            throws Exception {
        js.append(formVar
                  + ".addDataProcess(new CDOArealStatisticsProcess());\n");
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
        makeInputForm(request, input, sb);
        sb.append(HtmlUtils.formTableClose());
    }

    /**
     * Make the input form
     *
     * @param request  the Request
     * @param input    the DataProcessInput
     * @param sb       the StringBuilder
     *
     * @throws Exception  problem making stuff
     */
    private void makeInputForm(Request request, DataProcessInput input,
                               StringBuilder sb)
            throws Exception {
        Entry first = input.getOperands().get(0).getEntries().get(0);

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

        if ( !canHandle(input)) {
            throw new Exception("Illegal data type");
        }

        List<DataProcessOperand> outputEntries =
            new ArrayList<DataProcessOperand>();
        int opNum = 0;
        for (DataProcessOperand op : input.getOperands()) {
            Entry oneOfThem = op.getEntries().get(0);
            Entry collection = GranuleTypeHandler.getCollectionEntry(request,
                                   oneOfThem);
            String frequency = "Monthly";
            if (collection != null) {
                frequency = collection.getValues()[0].toString();
            }
            if (frequency.toLowerCase().indexOf("mon") >= 0) {
                outputEntries.add(processMonthlyRequest(request, input, op,
                        opNum));
            }
            opNum++;
        }

        return new DataProcessOutput(outputEntries);
    }

    /**
     * Process the daily data request
     *
     * @param request  the request
     * @param dpi      the DataProcessInput
     *
     * @return  some output
     *
     * @throws Exception problem processing the daily data
     */
    private Entry processDailyRequest(Request request, DataProcessInput dpi)
            throws Exception {
        throw new Exception("can't handle daily data yet");
    }

    /**
     * Process the monthly request
     *
     * @param request  the request
     * @param dpi      the DataProcessInput
     * @param op       the operand
     * @param opNum    the operand number
     *
     * @return  some output
     *
     * @throws Exception Problem processing the monthly request
     */
    private DataProcessOperand processMonthlyRequest(Request request,
            DataProcessInput dpi, DataProcessOperand op, int opNum)
            throws Exception {

        Entry oneOfThem = op.getEntries().get(0);
        CdmDataOutputHandler dataOutputHandler =
            getOutputHandler().getDataOutputHandler();
        GridDataset dataset =
            dataOutputHandler.getCdmManager().getGridDataset(oneOfThem,
                oneOfThem.getResource().getPath());
        if ((dataset == null) || dataset.getGrids().isEmpty()) {
            throw new Exception("No grids found");
        }
        String varname  =
            ((GridDatatype) dataset.getGrids().get(0)).getName();

        Object[] values = oneOfThem.getValues();
        String tail =
            getOutputHandler().getStorageManager().getFileTail(oneOfThem);
        String       id        = getRepository().getGUID();
        String       newName = IOUtil.stripExtension(tail) + "_" + id + ".nc";
        File outFile = new File(IOUtil.joinDir(dpi.getProcessDir(), newName));
        List<String> commands  = initCDOCommand();

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
        //   - level
        //   - month range
        //   - year or time range
        //   - variable
        getOutputHandler().addStatCommands(request, oneOfThem, commands);
        getOutputHandler().addAreaSelectCommands(request, oneOfThem,
                commands);
        commands.add("-remapbil,r360x180");
        getOutputHandler().addLevelSelectCommands(request, oneOfThem,
                commands, CdmDataOutputHandler.ARG_LEVEL);
        // Handle the case where the months span the year end (e.g. DJF)
        // Break it up into two requests
        if (doMonthsSpanYearEnd(request, oneOfThem)) {
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
                /*
                        (request.defined(CDOOutputHandler.ARG_CDO_YEARS) &&
                         !(request.defined(CDOOutputHandler.ARG_CDO_STARTYEAR+opStr) &&
                           request.defined(CDOOutputHandler.ARG_CDO_ENDYEAR+opStr)));
                           */
            }
            String        yearString = null;
            List<Integer> years      = new ArrayList<Integer>();
            if (haveYears) {
                yearString = request.getString(
                    CDOOutputHandler.ARG_CDO_YEARS + opStr,
                    request.getString(CDOOutputHandler.ARG_CDO_YEARS, null));
                if (yearString != null) {
                    yearString = CDOOutputHandler.verifyYearsList(yearString);
                }
                List<String> yearList = StringUtil.split(yearString, ",",
                                            true, true);
                for (String year : yearList) {
                    years.add(Integer.parseInt(year));
                }
            } else {
                int oldStartYear =
                    request.get(
                        CDOOutputHandler.ARG_CDO_STARTYEAR + opStr,
                        request.get(
                            CDOOutputHandler.ARG_CDO_STARTYEAR, 1979));
                int oldEndYear =
                    request.get(CDOOutputHandler.ARG_CDO_ENDYEAR + opStr,
                                request.get(CDOOutputHandler.ARG_CDO_ENDYEAR,
                                            1979));
                for (int i = oldStartYear; i < oldEndYear; i++) {
                    years.add(oldStartYear + 1);
                }
            }
            int yearNum = 0;
            for (Integer year : years) {
                for (int i = 0; i < 2; i++) {
                    List<String> savedCommands = new ArrayList(commands);
                    Request      newRequest    = request.cloneMe();
                    newRequest.remove(CDOOutputHandler.ARG_CDO_YEARS);
                    newRequest.remove(CDOOutputHandler.ARG_CDO_YEARS + opStr);
                    /*
                    int oldStartYear = request.get(
                                           CDOOutputHandler.ARG_CDO_STARTYEAR
                                           + opStr, request.get(
                                               CDOOutputHandler.ARG_CDO_STARTYEAR
                                               + opStr, 1979));
                    int oldEndYear =
                        request.get(CDOOutputHandler.ARG_CDO_ENDYEAR + opStr,
                                    request.get(CDOOutputHandler.ARG_CDO_ENDYEAR
                                                + opStr, 1979));
                    */
                    if (i == 0) {  // last half of previous year
                        newRequest.put(CDOOutputHandler.ARG_CDO_ENDMONTH, 12);
                        newRequest.put(CDOOutputHandler.ARG_CDO_STARTYEAR
                                       + opStr, year - 1);
                        newRequest.put(CDOOutputHandler.ARG_CDO_ENDYEAR
                                       + opStr, year - 1);
                    } else {  // first half of current year
                        newRequest.put(CDOOutputHandler.ARG_CDO_STARTMONTH,
                                       1);
                        newRequest.put(CDOOutputHandler.ARG_CDO_STARTYEAR
                                       + opStr, year);
                        newRequest.put(CDOOutputHandler.ARG_CDO_ENDYEAR
                                       + opStr, year);
                    }
                    File tmpFile = new File(outFile.toString() + "."
                                            + yearNum);
                    getOutputHandler().addDateSelectCommands(newRequest,
                            oneOfThem, savedCommands, opNum);
                    savedCommands.add("-selname," + varname);
                    System.err.println("cmds:" + savedCommands);
                    savedCommands.add(oneOfThem.getResource().getPath());
                    savedCommands.add(tmpFile.toString());
                    runProcess(savedCommands, dpi.getProcessDir(), tmpFile);
                    tmpFiles.add(tmpFile.toString());
                    yearNum++;
                }
            }
            // merge the files together
            File tmpFile = new File(outFile.toString() + ".tmp");
            commands = initCDOCommand();
            commands.add("-mergetime");
            for (String file : tmpFiles) {
                commands.add(file);
            }
            commands.add(tmpFile.toString());
            runProcess(commands, dpi.getProcessDir(), tmpFile);
            // now take the mean of the merged files
            commands = initCDOCommand();
            commands.add("-timmean");
            commands.add(tmpFile.toString());
            commands.add(outFile.toString());
            runProcess(commands, dpi.getProcessDir(), outFile);

        } else {
            getOutputHandler().addDateSelectCommands(request, oneOfThem,
                    commands, opNum);
            commands.add("-selname," + varname);

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
            commands = initCDOCommand();

            // Select order (left to right) - operations go right to left:
            //   - region
            //   - level
            //   - month range
            getOutputHandler().addStatCommands(request, climEntry, commands);
            getOutputHandler().addAreaSelectCommands(request, climEntry,
                    commands);
            commands.add("-remapbil,r360x180");
            getOutputHandler().addLevelSelectCommands(request, climEntry,
                    commands, CdmDataOutputHandler.ARG_LEVEL);
            getOutputHandler().addMonthSelectCommands(request, climEntry,
                    commands);
            commands.add("-selname," + varname);

            //System.err.println("clim cmds:" + commands);

            commands.add(climEntry.getResource().getPath());
            commands.add(climFile.toString());
            runProcess(commands, dpi.getProcessDir(), climFile);

            // now subtract them
            String anomName = IOUtil.stripExtension(tail) + "_" + id
                              + "_anom.nc";
            File anomFile = new File(IOUtil.joinDir(dpi.getProcessDir(),
                                anomName));
            commands = initCDOCommand();
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
        outputName.append(values[4]);
        outputName.append(" ");
        outputName.append(stat);
        outputName.append(" ");

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
        getOutputHandler().getEntryManager().writeEntryXmlFile(request,
                outputEntry);

        //return new DataProcessOperand(outputEntry.getName(), outputEntry);
        return new DataProcessOperand(outputName.toString(), outputEntry);
    }

    /**
     * _more_
     *
     * @param request _more_
     * @param oneOfThem _more_
     *
     * @return _more_
     *
     * @throws Exception _more_
     */
    private boolean doMonthsSpanYearEnd(Request request, Entry oneOfThem)
            throws Exception {
        if (request.defined(CDOOutputHandler.ARG_CDO_MONTHS)
                && request.getString(
                    CDOOutputHandler.ARG_CDO_MONTHS).equalsIgnoreCase(
                    "all")) {
            return false;
        }
        // Can't handle years requests yet.
        //if (request.defined(CDOOutputHandler.ARG_CDO_YEARS)
        //        || request.defined(CDOOutputHandler.ARG_CDO_YEARS + "1")) {
        //    return false;
        //}
        if (request.defined(CDOOutputHandler.ARG_CDO_STARTMONTH)
                || request.defined(CDOOutputHandler.ARG_CDO_ENDMONTH)) {
            int startMonth =
                request.defined(CDOOutputHandler.ARG_CDO_STARTMONTH)
                ? request.get(CDOOutputHandler.ARG_CDO_STARTMONTH, 1)
                : 1;
            int endMonth = request.defined(CDOOutputHandler.ARG_CDO_ENDMONTH)
                           ? request.get(CDOOutputHandler.ARG_CDO_ENDMONTH,
                                         startMonth)
                           : startMonth;
            // if they requested all months, no need to do a select on month
            if ((startMonth == 1) && (endMonth == 12)) {
                return false;
            }
            if (endMonth > startMonth) {
                return false;
            } else if (startMonth > endMonth) {
                return true;
            }
        }

        return false;
    }

    /**
     * Can we handle this input
     *
     * @param input  the input
     *
     * @return true if we can, otherwise false
     */
    public boolean canHandle(DataProcessInput input) {
        if ( !getOutputHandler().isEnabled()) {
            return false;
        }
        for (DataProcessOperand op : input.getOperands()) {
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
     * Add the statitics widget
     *
     * @param request  the Request
     * @param sb       the HTML
     */
    public void addStatsWidget(Request request, StringBuilder sb) {

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
     *
     * @throws Exception  problem making datasets
     */
    public void addTimeWidget(Request request, StringBuilder sb,
                              DataProcessInput input)
            throws Exception {

        List<GridDataset> grids = new ArrayList<GridDataset>();
        for (DataProcessOperand op : input.getOperands()) {
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
        CDOOutputHandler.makeMonthsWidget(request, sb, null);
        makeYearsWidget(request, sb, grids);
    }


    /**
     * Add the year selection widget
     *
     * @param request  the Request
     * @param sb       the StringBuilder to add to
     * @param grids    list of grids to use
     */
    private void makeYearsWidget(Request request, StringBuilder sb,
                                 List<GridDataset> grids) {
        int grid = 0;
        for (GridDataset dataset : grids) {
            List<CalendarDate> dates =
                CdmDataOutputHandler.getGridDates(dataset);
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
            String yearNum = (grid == 0)
                             ? ""
                             : String.valueOf(grid + 1);
            String yrLabel = (grids.size() == 1)
                             ? "Start"
                             : (grid == 0)
                               ? "First Dataset:<br>Start"
                               : "Second Dataset:<br>Start";
            yrLabel = Repository.msgLabel(yrLabel);
            if (grid > 0) {
                years.add(0, "");
            }
            int endIndex = (grid == 0)
                           ? years.size() - 1
                           : 0;

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
                                                                                                                        .title("Input a set of years separated by commas (e.g. 1980,1983,2012)"))));
            grid++;
        }
    }

}
