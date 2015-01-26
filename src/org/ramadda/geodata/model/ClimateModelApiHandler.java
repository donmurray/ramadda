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
import org.ramadda.repository.Entry;
import org.ramadda.repository.Repository;
import org.ramadda.repository.RepositoryManager;
import org.ramadda.repository.Request;
import org.ramadda.repository.RequestHandler;
import org.ramadda.repository.Result;
import org.ramadda.repository.database.Tables;
import org.ramadda.repository.type.CollectionTypeHandler;
import org.ramadda.repository.type.Column;
import org.ramadda.sql.Clause;
import org.ramadda.util.HtmlUtils;
import org.ramadda.util.JQuery;
import org.ramadda.util.Json;
import org.ramadda.util.TTLCache;
import org.ramadda.util.Utils;

import org.w3c.dom.Element;

import ucar.unidata.util.IOUtil;
import ucar.unidata.util.Misc;
import ucar.unidata.util.TwoFacedObject;


import java.io.File;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.List;


/**
 * An API for doing climate model comparisons
 *
 */
public class ClimateModelApiHandler extends RepositoryManager implements RequestHandler {

    /** search action */
    public static final String ARG_ACTION_SEARCH = "action.search";

    /** compare action */
    public static final String ARG_ACTION_COMPARE = "action.compare";

    /** multi compare action */
    public static final String ARG_ACTION_MULTI_COMPARE = "action.multicompare";

    /** timeseries action */
    public static final String ARG_ACTION_TIMESERIES = "action.timeseries";

    /** fixed collection id */
    public static final String ARG_COLLECTION = "collection";

    /** collection 1 id */
    public static final String ARG_COLLECTION1 = "collection1";

    /** collection 2 id */
    public static final String ARG_COLLECTION2 = "collection2";

    /** collection frequencey */
    public static final String ARG_FREQUENCY = "frequency";


    /** shortcut to JQuery class */
    private static final JQuery JQ = null;

    /** the collection type */
    private String collectionType;

    /** ttl cache */
    private TTLCache<Object, Object> cache = new TTLCache<Object,
                                                 Object>(60 * 60 * 1000);

    /**
     * ctor
     *
     * @param repository the repository
     * @param node xml from api.xml
     * @param props properties
     *
     * @throws Exception on badness
     */

    public ClimateModelApiHandler(Repository repository, Element node,
                                  Hashtable props)
            throws Exception {
        super(repository);
        collectionType = Misc.getProperty(props, "collectiontype",
                                          "climate_collection");
    }


    /**
     * Get the data processes for this request
     *
     * @param request  the Request
     * @param action _more_
     *
     * @return  the list of data processes
     *
     * @throws Exception problem generating list
     */
    private List<Service> getServices(Request request, String action)
            throws Exception {
        //return getTypeHandler().getServicesToRun(request);
        List<Service> processes = new ArrayList<Service>();
        if (action.equals(ARG_ACTION_COMPARE) ||
            action.equals(ARG_ACTION_MULTI_COMPARE)) {
            Service process = new CDOArealStatisticsProcess(repository);
            if (process.isEnabled()) {
                processes.add(process);
            }
            process = new NCLModelPlotDataProcess(repository);
            if (process.isEnabled()) {
                processes.add(process);
            }
        } else if (action.equals(ARG_ACTION_TIMESERIES)) {
            Service process = new CDOTimeSeriesProcess(repository);
            if (process.isEnabled()) {
                processes.add(process);
            }
        }

        return processes;
    }


    /**
     * Do the compare
     *
     * @param request  the Request
     * @param dpi   the input
     *
     * @return  a Result
     *
     * @throws Exception  problems processing the input
     */
    public Result doCompare(Request request, ServiceInput dpi)
            throws Exception {

        //This finds the selected processes
        List<Service> processesToRun = getServices(request,
                                           ARG_ACTION_COMPARE);

        //This is the dir under <home>/process
        File processDir = null;
        processDir = dpi.getProcessDir();
        if (processDir == null) {
            processDir = getStorageManager().createProcessDir();
        }

        List<ServiceOutput> outputs   = new ArrayList<ServiceOutput>();
        ServiceInput        nextInput = dpi;
        for (Service process : processesToRun) {
            System.err.println("MODEL: applying process: "
                               + process.getLabel());
            ServiceOutput output = process.evaluate(request, nextInput, null);
            outputs.add(output);

            //make a new input for the next process
            nextInput = dpi.makeInput(output);

            //Are we done? This should probably be a check to see if the output has a Result
            //if (output.hasOutput()) {
            //    break;
            //}
        }

        boolean    anyKMZ    = false;
        List<File> files     = new ArrayList<File>();
        File       lastFile  = null;
        Entry      lastEntry = null;
        for (ServiceOutput dpo : outputs) {
            for (Entry granule : dpo.getEntries()) {
                if (granule.isFile()) {
                    lastFile = granule.getFile();
                    if (IOUtil.hasSuffix(lastFile.toString(), "kmz")) {
                        anyKMZ = true;
                    }
                    files.add(lastFile);
                }
                lastEntry = granule;
            }
        }

        String template =
            getStorageManager().readSystemResource(
                "/org/ramadda/geodata/model/resources/plot_template.xml");
        template = template.replace("${name}",
                                    "Climate model comparison output");
        StringBuilder dpiDesc = new StringBuilder();
        if (dpi.getOperands().size() > 1) {
            dpiDesc.append("Comparison of ");
        } else {
            dpiDesc.append("Analysis of ");
        }
        int cntr = 0;
        for (ServiceOperand dpo : dpi.getOperands()) {
            if (cntr > 0) {
                dpiDesc.append(" and ");
            }
            dpiDesc.append(dpo.getDescription());
            cntr++;
        }
        template = template.replace("${description}", dpiDesc.toString());
        if (anyKMZ) {
            template = template.replace(
                "${output}",
                "{{earth width=\"500\" name=\"*.kmz\" listentries=\"true\" listwidth=\"450\"}}");
        } else {
            template = template.replace(
                "${output}",
                "{{gallery prefix=\"'''Images'''\" message=\"\" width=\"500\"}}");
        }
        IOUtil.writeFile(new File(IOUtil.joinDir(processDir,
                ".this.ramadda.xml")), template);



        //If no processing was done then return the raw files
        if (processesToRun.size() == 0) {
            ClimateCollectionTypeHandler typeHandler = getTypeHandler();

            return typeHandler.zipFiles(request, "results.zip", files);
        }

        //Now we get the process entry id
        String processId = processDir.getName();
        String processEntryId =
            getStorageManager().getProcessDirEntryId(processId);

        String entryUrl =
            HtmlUtils.url(
                request.getAbsoluteUrl(getRepository().URL_ENTRY_SHOW),
                ARG_ENTRYID,
        // Use this if you want to return the process directory
        processEntryId);
        //processEntryId + "/" + IOUtil.getFileTail(lastFile.toString()));

        if (request.get("returnimage", false)) {
            request.setReturnFilename("generated_image.png");

            return new Result(
                "",
                getStorageManager().getFileInputStream(lastFile.toString()),
                "image/png");
        } else if (request.get("returnjson", false)) {
            StringBuilder json            = new StringBuilder();
            Entry         processDirEntry =
            //new Entry(processEntryId, new ProcessFileTypeHandler(getRepository(), null));
            new Entry(processEntryId,
                      getEntryManager().getProcessFileTypeHandler());
            getRepository().getJsonOutputHandler().makeJson(request,
                    Misc.newList(processDirEntry), json);

            return new Result("", json, "application/json");

        } else {
            return new Result(entryUrl);
        }
    }

    /**
     * Make the time series
     *
     * @param request  the Request
     * @param dpi   the input
     *
     * @return  a Result
     *
     * @throws Exception  problems processing the input
     */
    public Result makeTimeSeries(Request request, ServiceInput dpi)
            throws Exception {

        //This finds the selected processes
        List<Service> processesToRun = getServices(request,
                                           ARG_ACTION_TIMESERIES);

        //This is the dir under <home>/process
        File processDir = null;
        processDir = dpi.getProcessDir();
        if (processDir == null) {
            processDir = getStorageManager().createProcessDir();
        }

        List<ServiceOutput> outputs   = new ArrayList<ServiceOutput>();
        ServiceInput        nextInput = dpi;
        for (Service process : processesToRun) {
            System.err.println("MODEL: applying process: "
                               + process.getLabel());
            ServiceOutput output = process.evaluate(request, nextInput, null);
            outputs.add(output);

            //make a new input for the next process
            nextInput = dpi.makeInput(output);

            //Are we done? This should probably be a check to see if the output has a Result
            //if (output.hasOutput()) {
            //    break;
            //}
        }

        boolean    anyKMZ    = false;
        List<File> files     = new ArrayList<File>();
        File       lastFile  = null;
        Entry      lastEntry = null;
        for (ServiceOutput dpo : outputs) {
            for (Entry granule : dpo.getEntries()) {
                if (granule.isFile()) {
                    lastFile = granule.getFile();
                    if (IOUtil.hasSuffix(lastFile.toString(), "kmz")) {
                        anyKMZ = true;
                    }
                    files.add(lastFile);
                }
                lastEntry = granule;
            }
        }

        String template =
            getStorageManager().readSystemResource(
                "/org/ramadda/geodata/model/resources/ts_template.xml");
        template = template.replace("${name}",
                                    "Climate model comparison output");
        StringBuilder dpiDesc = new StringBuilder();
        if (dpi.getOperands().size() > 1) {
            dpiDesc.append("Comparison of ");
        } else {
            dpiDesc.append("Analysis of ");
        }
        int cntr = 0;
        for (ServiceOperand dpo : dpi.getOperands()) {
            if (cntr > 0) {
                dpiDesc.append(" and ");
            }
            dpiDesc.append(dpo.getDescription());
            cntr++;
        }
        template = template.replace("${description}", dpiDesc.toString());
        if (anyKMZ) {
            template = template.replace(
                "${output}",
                "{{earth width=\"500\" name=\"*.kmz\" listentries=\"true\" listwidth=\"450\"}}");
        } else {
            template = template.replace(
                "${output}",
                "{{gallery prefix=\"'''Images'''\" message=\"\" width=\"500\"}}");
        }
        IOUtil.writeFile(new File(IOUtil.joinDir(processDir,
                ".this.ramadda.xml")), template);



        //If no processing was done then return the raw files
        if (processesToRun.size() == 0) {
            ClimateCollectionTypeHandler typeHandler = getTypeHandler();

            return typeHandler.zipFiles(request, "results.zip", files);
        }

        //Now we get the process entry id
        String processId = processDir.getName();
        String processEntryId =
            getStorageManager().getProcessDirEntryId(processId);

        String entryUrl =
            HtmlUtils.url(
                request.getAbsoluteUrl(getRepository().URL_ENTRY_SHOW),
                ARG_ENTRYID,
        // Use this if you want to return the process directory
        processEntryId);
        //processEntryId + "/" + IOUtil.getFileTail(lastFile.toString()));

        if (request.get("returnjson", false)) {
            StringBuilder json            = new StringBuilder();
            Entry         processDirEntry =
            //new Entry(processEntryId, new ProcessFileTypeHandler(getRepository(), null));
            new Entry(processEntryId,
                      getEntryManager().getProcessFileTypeHandler());
            getRepository().getJsonOutputHandler().makeJson(request,
                    Misc.newList(processDirEntry), json);

            return new Result("", json, "application/json");

        } else {
            return new Result(entryUrl);
        }
    }

    /**
     * handle the plot comparison request
     *
     * @param request request
     *
     * @return result
     *
     * @throws Exception on badness
     */
    public Result processCompareRequest(Request request) throws Exception {
        return handleRequest(request, ARG_ACTION_COMPARE);
    }

    /**
     * handle the multiple param comparison request
     *
     * @param request request
     *
     * @return result
     *
     * @throws Exception on badness
     */
    public Result processMultiCompareRequest(Request request) throws Exception {
        return handleRequest(request, ARG_ACTION_MULTI_COMPARE);
    }

    /**
     * handle the time series request
     *
     * @param request request
     *
     * @return result
     *
     * @throws Exception on badness
     */
    public Result processTimeSeriesRequest(Request request) throws Exception {
        return handleRequest(request, ARG_ACTION_TIMESERIES);
    }

    /**
     * handle the request
     *
     * @param request request
     * @param type _more_
     *
     * @return result
     *
     * @throws Exception on badness
     */
    public Result handleRequest(Request request, String type)
            throws Exception {

        if (getServices(request, type).isEmpty()) {
            throw new RuntimeException(
                "Data processes for model comparison are not configured.");
        }

        String fixedCollectionId = request.getString(ARG_COLLECTION,
                                       (String) null);
        Entry fixedCollection = null;

        if (fixedCollectionId != null) {
            //            System.err.println ("Have fixed collection:" + fixedCollectionId);
        }

        if (fixedCollectionId != null) {
            request.put(ARG_COLLECTION1, fixedCollectionId);
            request.put(ARG_COLLECTION2, fixedCollectionId);
        }


        String json = request.getString("json", (String) null);
        if (json != null) {
            return processJsonRequest(request, json);
        }
        boolean returnjson = request.get("returnjson", false);

        Hashtable<String, StringBuilder> extra = new Hashtable<String,
                                                     StringBuilder>();
        List<ServiceOperand> operands = new ArrayList<ServiceOperand>();

        File processDir               =
            getStorageManager().createProcessDir();

        //If we are searching or comparing then find the selected entries
        if (request.exists(ARG_ACTION_SEARCH) || request.exists(type)) {
            int collectionCnt = 0;
            for (String collection : new String[] { ARG_COLLECTION1,
                    ARG_COLLECTION2 }) {

                collectionCnt++;
                StringBuilder tmp = new StringBuilder();
                extra.put(collection, tmp);
                String selectArg = getCollectionSelectArg(collection);
                Entry collectionEntry = getEntryManager().getEntry(request,
                                            request.getString(selectArg, ""));
                //                System.err.println("collectionEntry:" + collectionEntry+" select: " +
                //                                    request.getString(selectArg,""));

                if (collectionEntry == null) {
                    tmp.append("No collection");

                    continue;
                }
                List<Entry> entries = findEntries(request, collection,
                                          collectionEntry, collectionCnt);
                if (entries.isEmpty()) {
                    if (operands.isEmpty()) {
                        tmp.append(
                            getPageHandler().showDialogError(
                                "You need to select all fields"));
                    }

                    continue;
                }
                operands.add(new ServiceOperand(entries.get(0).getName(),
                        entries));

                tmp.append(
                    "<div style=\" margin-bottom:2px;  margin-top:2px; max-height: 150px; overflow-y: auto\">");
                if ( !request.defined(ARG_COLLECTION)) {
                    tmp.append(getEntryManager().getEntryLink(request,
                            collectionEntry));
                }
                tmp.append("<ul>");
                for (Entry granule : entries) {
                    tmp.append("<li>");
                    tmp.append(getEntryManager().getEntryLink(request,
                            granule));
                }
                tmp.append("</ul>");
                tmp.append("</div>");
            }
        }


        //Check to see if we at least 1 operand 
        boolean hasOperands = false;
        if (operands.size() >= 1) {
            hasOperands = (operands.get(0).getEntries().size() > 0)
                          || (operands.get(1).getEntries().size() > 0);
        }

        if ((fixedCollectionId != null) && (fixedCollection == null)) {
            fixedCollection = getEntryManager().getEntry(request,
                    fixedCollectionId);
            //            System.err.println("got it:" + fixedCollection);
        }



        StringBuilder sb  = new StringBuilder();
        ServiceInput  dpi = new ServiceInput(processDir, operands);

        if (request.exists(type)) {
            if (hasOperands) {
                try {
                    if (type.equals(ARG_ACTION_COMPARE)) {
                        return doCompare(request, dpi);
                    } else {
                        return makeTimeSeries(request, dpi);
                    }
                } catch (Exception exc) {
                    if (returnjson) {
                        StringBuilder data = new StringBuilder();
                        data.append(Json.mapAndQuote("error",
                                exc.getMessage()));

                        return new Result("", data, Json.MIMETYPE);
                    } else {
                        sb.append(
                            getPageHandler().showDialogError(
                                "An error occurred:<br>" + exc.getMessage()));
                    }
                }
            } else {
                if (returnjson) {
                    StringBuilder data = new StringBuilder();
                    data.append(Json.map("error", "No fields selected."));

                    return new Result("", data, Json.MIMETYPE);
                } else {
                    sb.append(
                        getPageHandler().showDialogWarning(
                            "No fields selected"));
                }
            }
        }



        ClimateCollectionTypeHandler typeHandler = getTypeHandler();
        List<Entry>                  collections = getCollections(request);
        if (collections.size() == 0) {
            return new Result(
                "Climate Model Comparison",
                new StringBuilder(
                    getPageHandler().showDialogWarning(
                        msg("No climate collections found"))));
        }

        String formId = "selectform" + HtmlUtils.blockCnt++;
        sb.append(HtmlUtils.comment("collection form"));
        sb.append(HtmlUtils.importJS(fileUrl("/model/compare.js")));
        sb.append(HtmlUtils.cssLink(getRepository().getUrlBase()
                                    + "/model/model.css"));

        String formAttrs = HtmlUtils.attrs(ATTR_ID, formId);
        if (type.equals(ARG_ACTION_COMPARE)) {
            sb.append(HtmlUtils.form(getCompareUrlPath(request), formAttrs));
            getMapManager().addGoogleEarthImports(request, sb);
            sb.append(
                "<script type=\"text/JavaScript\">google.load(\"earth\", \"1\");</script>\n");
            //sb.append(HtmlUtils.script(
            //    "$(document).ready(function() {\n $(\"a.popup_image\").fancybox({\n 'titleShow' : false\n });\n });\n"));
        } else if (type.equals(ARG_ACTION_MULTI_COMPARE)) {
            sb.append(HtmlUtils.form(getMultiCompareUrlPath(request),
                                     formAttrs));
            getWikiManager().addDisplayImports(request, sb);
        } else {
            sb.append(HtmlUtils.form(getTimeSeriesUrlPath(request),
                                     formAttrs));
            getWikiManager().addDisplayImports(request, sb);
        }

        List<TwoFacedObject> tfos = new ArrayList<TwoFacedObject>();
        tfos.add(new TwoFacedObject("Select Climate Collection", ""));
        for (Entry collection : collections) {
            tfos.add(new TwoFacedObject(collection.getLabel(),
                                        collection.getId()));
        }

        List<Service> processes = getServices(request, type);


        String        formType  = "compare";
        String helpFile =
            "/org/ramadda/geodata/model/htdocs/model/compare.html";
        if (type.equals(ARG_ACTION_TIMESERIES)) {
            formType = "timeseries";
            helpFile =
                "/org/ramadda/geodata/model/htdocs/model/timeseries.html";
        }
        StringBuilder js =
            new StringBuilder("\n//collection form initialization\n");
        String frequency = getFrequencyArgs(request);
        js.append("var " + formId + " = new "
                  + HtmlUtils.call("CollectionForm",
                                   HtmlUtils.squote(formId),
                                   HtmlUtils.squote(formType),
                                   HtmlUtils.squote(frequency)));




        for (Service process : processes) {
            process.initFormJS(request, js, formId);
        }


        if (type.equals(ARG_ACTION_COMPARE)) {
            sb.append(HtmlUtils.h1("Climate Model Comparison"));
            sb.append(
                "Plot monthly maps from different climate model datasets as well as differences between datasets.");
        } else {
            sb.append(HtmlUtils.h1("Climate Model Time Series"));
            sb.append(
                "Plot monthly time series from different climate model datasets.");
        }
        sb.append(HtmlUtils.p());

        if (fixedCollection != null) {
            sb.append(HtmlUtils.h2(msg("Collection: "
                                       + fixedCollection.getName())));
            sb.append(HtmlUtils.hidden(ARG_COLLECTION,
                                       fixedCollection.getId()));
        }

        if (request.defined(ARG_FREQUENCY)) {
            sb.append(HtmlUtils.hidden(ARG_FREQUENCY,
                                       request.getString(ARG_FREQUENCY)));
        }

        sb.append("<table><tr valign=\"center\" align=\"left\">\n");
        sb.append(HtmlUtils.open(
                                 "td",
                                 "width=\"400px\""));
        sb.append(HtmlUtils.div(msg("Select Data To Plot"),
                                HtmlUtils.cssClass("model-header")));
        sb.append(HtmlUtils.close("td"));

        sb.append(HtmlUtils.open("td",
                                 "width=\"800px\" "));
        sb.append(HtmlUtils.open("div", HtmlUtils.cssClass("model-header")));
        if (hasOperands) {
            if (type.equals(ARG_ACTION_COMPARE)) {
                sb.append(
                    HtmlUtils.submit(
                        msg("Make Plot"), ARG_ACTION_COMPARE,
                        HtmlUtils.id(formId + "_submit")
                        + makeButtonSubmitDialog(
                            sb, msg("Making Plot, Please Wait") + "...")));
            } else {
                sb.append(
                    HtmlUtils.submit(
                        msg("Make Time Series"), ARG_ACTION_TIMESERIES,
                        HtmlUtils.id(formId + "_submit")
                        + makeButtonSubmitDialog(
                            sb,
                            msg("Making Time Series, Please Wait") + "...")));
            }
        }
        sb.append(HtmlUtils.close("div"));
        sb.append(HtmlUtils.close("td"));
        sb.append("</tr>\n");

        sb.append("<tr valign=\"top\">");
        sb.append(HtmlUtils.open("td", "width=\"400px\"") + "\n");

        // Field selection
        sb.append(HtmlUtils.open("div", HtmlUtils.cssClass("titled_border")));
        sb.append(HtmlUtils.div(msg("Field(s)"), HtmlUtils.id("title")));
        sb.append(HtmlUtils.open("div", HtmlUtils.id("content")));

        int          collectionNumber = 0;



        List<String> datasets         = new ArrayList<String>(2);
        List<String> datasetTitles    = new ArrayList<String>(2);
        for (String collection : new String[] { ARG_COLLECTION1,
                ARG_COLLECTION2 }) {
            StringBuilder dsb = new StringBuilder();


            //dsb.append(HtmlUtils.formTable());
            if (collectionNumber == 0) {
                datasetTitles.add("Dataset 1");
            } else {
                datasetTitles.add("Dataset 2 (Optional)");
            }
            collectionNumber++;
            String       arg       = getCollectionSelectArg(collection);
            String       id        = getCollectionSelectId(formId,
                                         collection);



            List<String> selectors = new ArrayList<String>();
            if (fixedCollection != null) {
                dsb.append("\n");
                dsb.append(HtmlUtils.hidden(arg, fixedCollection.getId(),
                                            HtmlUtils.id(id)));
            } else {
                String collectionWidget =
                    HtmlUtils.select(arg, tfos, request.getString(arg, ""),
                                     HtmlUtils.cssClass("select_widget")
                                     + HtmlUtils.id(id));
                String select = "<label class=\"selector\" for=\"" + id
                                + "\">" + msgLabel("Collection") + "</label>"
                                + collectionWidget;
                selectors.add(select);
            }

            Entry        entry   = collections.get(0);
            List<Column> columns = typeHandler.getGranuleColumns();
            for (int fieldIdx = 0; fieldIdx < columns.size(); fieldIdx++) {
                Column column = columns.get(fieldIdx);
                //String key = "values::" + entry.getId()+"::" +column.getName();
                List values = new ArrayList();
                values.add(new TwoFacedObject("--", ""));
                arg = getFieldSelectArg(collection, fieldIdx);
                String selectedValue = request.getString(arg, "");
                if (Utils.stringDefined(selectedValue)) {
                    values.add(selectedValue);
                }
                //dsb.append("\n");
                // don't show variable selector for subsequent collections when we have a fixed
                // collection
                if (request.defined(ARG_COLLECTION) && (collectionNumber > 1)
                        && column.getName().equals("variable")) {
                    //dsb.append(HtmlUtils.formEntry(msg(""),
                    String selector =
                        "<span class=\"select_widget\">&nbsp;</span>";
                    selectors.add("<label class=\"selector\">" + msg("")
                                  + "</label>" + selector);
                } else {

                    String extraSelect = "";
                    if (type.equals(ARG_ACTION_MULTI_COMPARE)) {
                        extraSelect = HtmlUtils.attr(HtmlUtils.ATTR_MULTIPLE, "true")+HtmlUtils.attr("size", "4");
                    }
                    String selectBox =
                        HtmlUtils.select(arg, values, selectedValue,
                                         HtmlUtils.cssClass("select_widget")
                                         + HtmlUtils.attr("id",
                                             getFieldSelectId(formId,
                                                 collection, fieldIdx))+extraSelect);
                    String select = "<label class=\"selector\" for=\""
                                    + getFieldSelectId(formId, collection,
                                        fieldIdx) + "\">"
                                            + msgLabel(column.getLabel())
                                            + "</label>" + selectBox;

                    selectors.add(select);

                }
            }
            // TODO: lay out a table with the selectors 
            //dsb.append(HtmlUtils.formTableClose());
            dsb.append(
                "<table cellspacing=\"3px\" cellpadding=\"2px\" align=\"center\">\n");
            for (int i = 0; i < selectors.size(); i++) {
                dsb.append("<tr valign=\"top\">\n");
                dsb.append("<td>");
                dsb.append(selectors.get(i));
                dsb.append("</td>");
            }
            dsb.append("</tr></table>\n");

            // List out the search results
            //StringBuilder results = extra.get(collection);
            //if (results != null) {
            //    dsb.append(results.toString());
            //}


            datasets.add(dsb.toString());
        }
        // table of two datasets
        //sb.append("<table align=\"center\"><tr>");
        sb.append("<table><tr>");
        sb.append(
            "<td style=\"border-right:1px solid #0000FF;border-left:none;border-top:none;border-bottom:none\">");
        sb.append(HtmlUtils.div(msg(datasetTitles.get(0)),
                                HtmlUtils.cssClass("model-dataset_title")));
        sb.append(HtmlUtils.div(datasets.get(0), HtmlUtils.cssClass("model-dataset")));
        sb.append("</td>");
        sb.append("<td valign=\"top\">");
        sb.append(HtmlUtils.div(msg(datasetTitles.get(1)),
                                HtmlUtils.cssClass("model-dataset_title")));
        sb.append(HtmlUtils.div(datasets.get(1), HtmlUtils.cssClass("model-dataset")));
        sb.append("</td>");
        sb.append("</tr>");

        if ( !hasOperands) {
            sb.append("<tr><td colspan=\"2\" align=\"center\">");
            sb.append(HtmlUtils.p());
            sb.append(HtmlUtils.submit("Select Data", ARG_ACTION_SEARCH,
                                       HtmlUtils.id(formId + "_submit")
                                       + makeButtonSubmitDialog(sb,
                                           msg("Searching for data")
                                           + "...")));
            sb.append("</td></tr></table>");
            sb.append(HtmlUtils.close("div"));  // titled_border_content
            // Right column - help
            sb.append("<td width=\"800px\">");
            sb.append("<div id=\"" + formId + "_output\">");
            try {
                String helpText =
                    getStorageManager().readSystemResource(helpFile);
                sb.append(helpText);
            } catch (Exception excp) {}
            sb.append("</div>");
            sb.append("</td>");
        } else {
            sb.append("<tr><td colspan=\"2\" align=\"center\">");
            sb.append(HtmlUtils.p());
            sb.append(HtmlUtils.submit("Update Data Selection",
                                       ARG_ACTION_SEARCH,
                                       HtmlUtils.id(formId + "_submit")
                                       + makeButtonSubmitDialog(sb,
                                           msg("Searching for new data")
                                           + "...")));
            sb.append("</td></tr></table>");
            sb.append(HtmlUtils.close("div"));  // titled_border_content
            sb.append(HtmlUtils.close("div"));  // titled_border


            List<String> processTabs   = new ArrayList<String>();
            List<String> processTitles = new ArrayList<String>();

            boolean      first         = true;

            for (Service process : processes) {
                StringBuilder tmpSB = new StringBuilder();
                if (processes.size() > 1) {
                    /*
                tmpSB.append(
                    HtmlUtils.radio(
                        ClimateCollectionTypeHandler.ARG_DATA_PROCESS_ID,
                        process.getId(), first));
                tmpSB.append(HtmlUtils.space(1));
                tmpSB.append(msg("Select"));
                tmpSB.append(HtmlUtils.br());
                */
                } else {
                    tmpSB.append(
                        HtmlUtils.hidden(
                            ClimateCollectionTypeHandler.ARG_DATA_PROCESS_ID,
                            process.getId()));
                }
                if (process.canHandle(dpi)) {
                    process.addToForm(request, dpi, tmpSB, null, null);
                    processTabs.add(tmpSB.toString());
                    processTitles.add(process.getLabel());
                    first = false;
                }
            }


            //sb.append(header(msg("Process Data")));
            for (int i = 0; i < processTitles.size(); i++) {
                sb.append(
                    HtmlUtils.open(
                        "div", HtmlUtils.cssClass("titled_border")));
                sb.append(HtmlUtils.div(msg(processTitles.get(i)),
                                        HtmlUtils.id("title")));
                sb.append("<div id=\"content\">\n");
                sb.append(processTabs.get(i));
                sb.append("</div>\n");
                sb.append("</div> <!-- titled_border -->");
            }
            sb.append("</td>");
            // Right column - no data
            sb.append("<td width=\"800px\">");
            sb.append("<div id=\"" + formId + "_output\">");
            try {
                String helpText =
                    getStorageManager().readSystemResource(helpFile);
                sb.append(helpText);
            } catch (Exception excp) {}
            sb.append("</div>");
            sb.append("</td>");
        }
        sb.append("\n</tr></table>");


        sb.append("\n");
        sb.append(HtmlUtils.script(js.toString()));
        sb.append("\n");

        sb.append(HtmlUtils.formClose());





        return new Result("Climate Model Comparison", sb);


    }



    /**
     * Get the frequency arguments from the request
     * @param request the Request
     * @return empty string if not defined
     */
    private String getFrequencyArgs(Request request) {
        // TODO Auto-generated method stub
        StringBuilder args = new StringBuilder();
        if (request.defined(ARG_FREQUENCY)) {
            args.append(ARG_FREQUENCY);
            args.append("=");
            args.append(request.getString(ARG_FREQUENCY));
        }

        return args.toString();
    }


    /**
     * Find the entries
     *
     * @param request   the Request
     * @param collection  the collection
     * @param entry  the entry
     * @param collectionCnt  the collection count
     *
     * @return the list of entries (may be empty)
     *
     * @throws Exception  problem with search
     */
    private List<Entry> findEntries(Request request, String collection,
                                    Entry entry, int collectionCnt)
            throws Exception {
        CollectionTypeHandler typeHandler =
            (CollectionTypeHandler) entry.getTypeHandler();
        List<Clause>    clauses       = new ArrayList<Clause>();
        List<Column>    columns       = typeHandler.getGranuleColumns();
        HashSet<String> seenTable     = new HashSet<String>();
        boolean         haveAllFields = true;
        for (int fieldIdx = 0; fieldIdx < columns.size(); fieldIdx++) {
            Column column      = columns.get(fieldIdx);
            String dbTableName = column.getTableName();
            if ( !seenTable.contains(dbTableName)) {
                clauses.add(Clause.eq(typeHandler.getCollectionIdColumn(),
                                      entry.getId()));
                clauses.add(Clause.join(Tables.ENTRIES.COL_ID,
                                        dbTableName + ".id"));
                seenTable.add(dbTableName);
            }
            String arg = "";
            if (request.defined(ARG_COLLECTION)
                    && collection.equals(ARG_COLLECTION2)
                    && column.getName().equals("variable")) {
                arg = getFieldSelectArg(ARG_COLLECTION1, fieldIdx);
            } else {
                arg = getFieldSelectArg(collection, fieldIdx);
            }
            List v = request.get(arg, new ArrayList());
            if (v.isEmpty() || (v.size() == 1 && v.get(0).toString().isEmpty())) {
                haveAllFields = false;
                break;
            }
            if (v.size() > 0) {
               List<Clause> ors = new ArrayList<Clause>(); 
               for (Object o : v) {
                   String s = o.toString();
                   if (s.length() > 0) {
                       ors.add(Clause.eq(column.getName(), s));
                   }
               }
               if (!ors.isEmpty()) {
                   clauses.add(Clause.or(ors));
               }
            } /*else {
               haveAllFields = false;
               break;
            } */
        }
        //System.out.println(clauses);
        //System.out.println(haveAllFields);
        //If we have a fixed collection then don't do the search if no fields were selected
        //if ( !haveAllFields && request.defined(ARG_COLLECTION)) {
        if ( !haveAllFields) {
            return new ArrayList<Entry>();
        }


        List[] pair = getEntryManager().getEntries(request, clauses,
                          typeHandler.getGranuleTypeHandler());

        return pair[1];
    }

    /**
     * Get the field select argument
     *
     * @param collection  the collection
     * @param fieldIdx  the field index
     *
     * @return  the argument
     */
    private String getFieldSelectArg(String collection, int fieldIdx) {
        return collection + "_field" + fieldIdx;

    }

    /**
     * Get the collection select argument
     *
     * @param collection  the collection
     *
     * @return  the collection
     */
    private String getCollectionSelectArg(String collection) {
        return collection;
    }


    /**
     * Get the field select id
     *
     * @param formId   the form id
     * @param collection  the collection
     * @param fieldIdx  the field index
     *
     * @return  the field id
     */
    private String getFieldSelectId(String formId, String collection,
                                    int fieldIdx) {
        return getCollectionSelectId(formId, collection) + "_field"
               + fieldIdx;
    }

    /**
     * Get the collection select id
     *
     * @param formId   the form id
     * @param collection  the collection
     *
     * @return  the collection select id
     */
    private String getCollectionSelectId(String formId, String collection) {
        return formId + "_" + collection;
    }



    /**
     * Process the JSON request
     *
     * @param request  the request
     * @param what     what to search for
     *
     * @return  the JSON string
     *
     * @throws Exception  problem with processing
     */
    private Result processJsonRequest(Request request, String what)
            throws Exception {


        //        System.err.println("Request:" + request);
        Entry entry = getEntryManager().getEntry(request,
                          request.getString("thecollection", ""));
        //        System.err.println("Entry:" + entry);
        CollectionTypeHandler typeHandler =
            (CollectionTypeHandler) entry.getTypeHandler();
        List<Clause> clauses = new ArrayList<Clause>();
        List<Column> columns = typeHandler.getGranuleColumns();
        for (int fieldIdx = 0; fieldIdx < columns.size(); fieldIdx++) {
            String arg = "field" + fieldIdx;
            String column = columns.get(fieldIdx).getName();
            List v = request.get(arg, new ArrayList());
            if (v.size() == 1 && v.get(0).toString().isEmpty()) {
                continue;
            }
            if (v.size() > 0) {
               List<Clause> ors = new ArrayList<Clause>(); 
               for (Object o : v) {
                   String s = o.toString();
                   if (s.length() > 0) {
                       ors.add(Clause.eq(column, s));
                   }
               }
               if (!ors.isEmpty()) {
                   clauses.add(Clause.or(ors));
               }
            } 
        }
        //System.err.println("x: " + clauses);

        int columnIdx = request.get("field", 1);
        if (columnIdx >= columns.size()) {
            return new Result("", new StringBuilder(), Json.MIMETYPE);
        }
        Column myColumn = columns.get(columnIdx);
        List<String> values =
            new ArrayList<String>(((CollectionTypeHandler) entry
                .getTypeHandler())
                    .getUniqueColumnValues(entry, columnIdx, clauses, false));
        StringBuilder sb = new StringBuilder();
        if (myColumn.isEnumeration()) {
            List<TwoFacedObject> tfos = typeHandler.getValueList(entry,
                                            values, myColumn);
            tfos.add(0, new TwoFacedObject(""));
            String json = Json.tfoList(tfos);
            sb.append(json);
        } else {
            values.add(0, "");
            String json = Json.list(values, true);
            sb.append(json);
        }
        //System.out.println("json:" + sb);

        return new Result("", sb, Json.MIMETYPE);
    }

    /**
     *  return the main entry point URL
     *
     *
     * @param request _more_
     * @return  the main entry point
     */
    private String getMultiCompareUrlPath(Request request) {
        //Use the collection type in the path. This is defined in the api.xml file
        StringBuilder base = new StringBuilder(getRepository().getUrlBase());
        base.append("/model/mcompare");
        if (request.defined(ARG_FREQUENCY)) {
            base.append("?");
            base.append(getFrequencyArgs(request));
        }

        return base.toString();
    }


    /**
     *  return the main entry point URL
     *
     *
     * @param request _more_
     * @return  the main entry point
     */
    private String getCompareUrlPath(Request request) {
        //Use the collection type in the path. This is defined in the api.xml file
        StringBuilder base = new StringBuilder(getRepository().getUrlBase());
        base.append("/model/compare");
        if (request.defined(ARG_FREQUENCY)) {
            base.append("?");
            base.append(getFrequencyArgs(request));
        }

        return base.toString();
    }

    /**
     *  return the main entry point URL
     *
     *
     * @param request _more_
     * @return  the main entry point
     */
    private String getTimeSeriesUrlPath(Request request) {
        //Use the collection type in the path. This is defined in the api.xml file
        String base = getRepository().getUrlBase() + "/model/timeseries";

        return base;
    }



    /**
     * Get the type handler
     *
     * @return  the climate collection type handler
     *
     * @throws Exception  problem finding one
     */
    private ClimateCollectionTypeHandler getTypeHandler() throws Exception {
        return (ClimateCollectionTypeHandler) getRepository().getTypeHandler(
            collectionType);
    }



    /**
     * Get the list of collections
     *
     * @param request  the request
     *
     * @return  the list of collections
     *
     * @throws Exception  problem generating list
     */
    private List<Entry> getCollections(Request request) throws Exception {
        Request tmpRequest = new Request(getRepository(), request.getUser());

        tmpRequest.put(ARG_TYPE, collectionType);
        List<Clause> fclause = new ArrayList<Clause>();
        if (request.defined(ARG_FREQUENCY)) {
            tmpRequest.put(Column.ARG_SEARCH_PREFIX + collectionType + "."
                           + ARG_FREQUENCY, request.getString(ARG_FREQUENCY));
        }

        List<Entry> collections =
            (List<Entry>) getEntryManager().getEntries(tmpRequest)[0];

        return collections;
    }

}
