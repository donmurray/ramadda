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
import org.ramadda.data.process.DataProcessOperand;
import org.ramadda.data.process.DataProcessOutput;
import org.ramadda.geodata.cdmdata.CdmDataOutputHandler;
import org.ramadda.repository.Entry;
import org.ramadda.repository.Repository;
import org.ramadda.repository.Request;
import org.ramadda.repository.Resource;
import org.ramadda.repository.type.GranuleTypeHandler;
import org.ramadda.repository.type.TypeHandler;
import org.ramadda.util.HtmlUtils;

import ucar.nc2.dt.grid.GridDataset;
import ucar.nc2.time.Calendar;
import ucar.nc2.time.CalendarDate;

import ucar.unidata.geoloc.LatLonPointImpl;
import ucar.unidata.geoloc.LatLonRect;
import ucar.unidata.util.IOUtil;

import ucar.visad.data.CalendarDateTime;

import java.io.File;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.SortedSet;
import java.util.TreeSet;


/**
 * Class description
 *
 *
 * @version        $version$, Wed, Feb 12, '14
 * @author         Enter your name here...    
 */
public class CDOTimeSeriesProcess extends CDODataProcess {

    /**
     * _more_
     *
     * @param repository _more_
     *
     * @throws Exception _more_
     */
    public CDOTimeSeriesProcess(Repository repository) throws Exception {
        super(repository, "CDO_TIMESERIES", "Time Series");
    }

    /**
     * _more_
     *
     * @param request _more_
     * @param input _more_
     * @param sb _more_
     *
     * @throws Exception _more_
     */
    @Override
    public void addToForm(Request request, DataProcessInput input,
                          StringBuffer sb)
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
     * @param sb       the StringBuffer
     *
     * @throws Exception  problem making stuff
     */
    private void makeInputForm(Request request, DataProcessInput input,
                               StringBuffer sb)
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

        getOutputHandler().addTimeWidget(request, sb, dataset, true);

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
     * _more_
     *
     * @param request _more_
     * @param input _more_
     *
     * @return _more_
     *
     * @throws Exception _more_
     */
    @Override
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
     * _more_
     *
     * @param dpi _more_
     *
     * @return _more_
     */
    @Override
    public boolean canHandle(DataProcessInput dpi) {
        // TODO Auto-generated method stub
        if ( !getOutputHandler().isEnabled()) {
            return false;
        }
        for (DataProcessOperand op : dpi.getOperands()) {
            List<Entry> entries = op.getEntries();
            // TODO: change this when we can handle more than one entry (e.g. daily data)
            //if (entries.isEmpty() || (entries.size() > 1)) {
            //    return false;
            //}
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
    public void addStatsWidget(Request request, StringBuffer sb) {
        sb.append(
            HtmlUtils.formEntry(
                Repository.msgLabel("Statistic"),
                HtmlUtils.radio(
                    CDOOutputHandler.ARG_CDO_STAT,
                    CDOOutputHandler.STAT_MEAN, true) + Repository.msg(
                        "Mean") + HtmlUtils.space(2)
                                + HtmlUtils.radio(
                                    CDOOutputHandler.ARG_CDO_STAT,
                                    CDOOutputHandler.STAT_ANOM,
                                    false) + Repository.msg("Anomaly")));
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
            if (climo == null) {
                throw new Exception("Unable to find climatology for "
                                    + oneOfThem.getName());
            } else if (climo.size() > 1) {
                System.err.println("found too many");
            } else {
                climEntry = climo.get(0);
                System.err.println("found climo: " + climEntry);
            }
        }

        commands.add("-fldavg");
        if ( !request.defined(getOutputHandler().ARG_CDO_MONTHS)) {
            commands.add("-yearmean");
        }
        // Select order (left to right) - operations go right to left:
        //   - stats
        //   - level
        //   - region
        //   - month range
        //   - year or time range
        getOutputHandler().addStatCommands(request, oneOfThem, commands);
        getOutputHandler().addLevelSelectCommands(request, oneOfThem,
                commands, CdmDataOutputHandler.ARG_LEVEL);
        getOutputHandler().addAreaSelectCommands(request, oneOfThem,
                commands);
        getOutputHandler().addDateSelectCommands(request, oneOfThem,
                commands, opNum);

        //System.err.println("cmds:" + commands);

        commands.add(oneOfThem.getResource().getPath());
        commands.add(outFile.toString());
        runProcess(commands, dpi.getProcessDir(), outFile);

        if (climEntry != null) {
            String climName = IOUtil.stripExtension(tail) + "_" + id
                              + "_clim.nc";
            File climFile = new File(IOUtil.joinDir(dpi.getProcessDir(),
                                climName));
            commands = initCDOCommand();

            commands.add("-fldavg");
            // Select order (left to right) - operations go right to left:
            //   - level
            //   - region
            //   - month range
            getOutputHandler().addStatCommands(request, climEntry, commands);
            getOutputHandler().addLevelSelectCommands(request, climEntry,
                    commands, CdmDataOutputHandler.ARG_LEVEL);
            getOutputHandler().addAreaSelectCommands(request, climEntry,
                    commands);
            getOutputHandler().addMonthSelectCommands(request, climEntry,
                    commands);

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
            commands.add("-ymonsub");
            commands.add(outFile.toString());
            commands.add(climFile.toString());
            commands.add(anomFile.toString());
            runProcess(commands, dpi.getProcessDir(), anomFile);
            outFile = anomFile;
        }

        StringBuffer outputName = new StringBuffer();
        Object[]     values     = oneOfThem.getValues();
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

        int startMonth = request.defined(CDOOutputHandler.ARG_CDO_STARTMONTH)
                         ? request.get(CDOOutputHandler.ARG_CDO_STARTMONTH, 1)
                         : 1;
        int endMonth = request.defined(CDOOutputHandler.ARG_CDO_ENDMONTH)
                       ? request.get(CDOOutputHandler.ARG_CDO_ENDMONTH,
                                     startMonth)
                       : startMonth;
        if (startMonth == endMonth) {
            outputName.append(MONTHS[startMonth - 1]);
        } else {
            outputName.append(MONTHS[startMonth - 1]);
            outputName.append("-");
            outputName.append(MONTHS[endMonth - 1]);
        }
        outputName.append(" ");
        String startYear = request.defined(CDOOutputHandler.ARG_CDO_STARTYEAR
                                           + yearNum)
                           ? request.getString(
                               CDOOutputHandler.ARG_CDO_STARTYEAR + yearNum)
                           : request.defined(
                               CDOOutputHandler.ARG_CDO_STARTYEAR)
                             ? request.getString(
                                 CDOOutputHandler.ARG_CDO_STARTYEAR, "")
                             : "";
        String endYear = request.defined(CDOOutputHandler.ARG_CDO_ENDYEAR
                                         + yearNum)
                         ? request.getString(CDOOutputHandler.ARG_CDO_ENDYEAR
                                             + yearNum)
                         : request.defined(CDOOutputHandler.ARG_CDO_ENDYEAR)
                           ? request.getString(
                               CDOOutputHandler.ARG_CDO_ENDYEAR, startYear)
                           : startYear;
        if (startYear.equals(endYear)) {
            outputName.append(startYear);
        } else {
            outputName.append(startYear);
            outputName.append("-");
            outputName.append(endYear);
        }
        //System.out.println("Name: " + outputName.toString());

        Resource resource = new Resource(outFile, Resource.TYPE_LOCAL_FILE);
        Entry outputEntry = new Entry(new TypeHandler(getRepository()), true);
        outputEntry.setResource(resource);

        //return new DataProcessOperand(outputEntry.getName(), outputEntry);
        return new DataProcessOperand(outputName.toString(), outputEntry);
    }
}
