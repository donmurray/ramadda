package org.ramadda.repository.type;

import ucar.unidata.sql.Clause;
import ucar.unidata.sql.SqlUtil;
import java.sql.*;


import java.util.Date;
import java.util.Hashtable;
import java.util.Properties;
import java.util.regex.Pattern;
import java.util.regex.Matcher;


import org.ramadda.repository.*;
import org.ramadda.repository.output.JsonOutputHandler;
import org.ramadda.repository.database.*;
import org.ramadda.repository.type.*;
import org.ramadda.util.HtmlUtils;
import org.ramadda.util.Utils;
import org.ramadda.util.TTLCache;
import org.ramadda.util.JQuery;
import org.ramadda.repository.type.Column;
import org.ramadda.repository.type.GenericTypeHandler;
import org.ramadda.repository.type.ExtensibleGroupTypeHandler;

import ucar.unidata.util.Misc;
import ucar.unidata.util.StringUtil;
import ucar.unidata.util.TwoFacedObject;

import org.w3c.dom.Element;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import ucar.unidata.util.IOUtil;

public class CollectionTypeHandler extends ExtensibleGroupTypeHandler {
    
    public static final JQuery JQ = null;

    public static final String ARG_SEARCH =  "search";

    public static final String PROP_GRANULE_TYPE  = "granule_type";

    private JsonOutputHandler jsonOutputHandler;

    private String dbTableName;

    private String dbColumnCollectionId;

    private List<Column> columns;

    private String selectArg = "select";

    private TypeHandler granuleTypeHandler;

    private TTLCache<String, Object> cache = new TTLCache<String, Object>(60*60*1000);

    public CollectionTypeHandler(Repository repository, Element entryNode)
        throws Exception {
        super(repository, entryNode);
    }


    public JsonOutputHandler getJsonOutputHandler () {
        if(jsonOutputHandler == null) {
            jsonOutputHandler  =(JsonOutputHandler)getRepository().getOutputHandler(org.ramadda.repository.output.JsonOutputHandler.class);
        }
        return         jsonOutputHandler;
    }

    public TypeHandler getGranuleTypeHandler() throws Exception {
        if(granuleTypeHandler ==null) {
            dbTableName = getProperty(PROP_GRANULE_TYPE,"");
            granuleTypeHandler = getRepository().getTypeHandler(getProperty(PROP_GRANULE_TYPE,""));
            columns = new ArrayList<Column>(granuleTypeHandler.getColumns());
            dbColumnCollectionId = columns.get(0).getName();
            columns.remove(0);
        }
        return granuleTypeHandler;
    }


    public Result getMetadataJson(Request request, Entry entry,
                                 List<Entry> subGroups, List<Entry> entries)
        throws Exception {
        List<String> values = new ArrayList<String>();
        List<Clause> clauses = getClauses(request, entry, values);
        String valueKey = "json::" + entry.getId() + "::" + StringUtil.join("::", values);
        StringBuffer json = (StringBuffer) cache.get(valueKey);
        if(json == null) {
            Column nextColumn = columns.get(0);
            String nextColumnName = nextColumn.getLabel();
            for(int selectIdx=0;selectIdx<columns.size();selectIdx++) {
                if(!request.defined(selectArg+selectIdx)) break;
                if(selectIdx<columns.size()-1) {
                    nextColumn = columns.get(selectIdx+1);
                    nextColumnName = nextColumn.getLabel();
                } else {
                    nextColumn = null;
                }
            }

            List<String> uniqueValues= getUniqueValues(entry, nextColumn, clauses);
            nextColumnName = nextColumnName.toLowerCase();
            String selectLabel = ":-- Select "  + Utils.getArticle(nextColumnName) +" " + nextColumnName + " --";
            uniqueValues.add(0,selectLabel);
            json = new StringBuffer();
            json.append(HtmlUtils.jsonMap(new String[]{
                        "values", HtmlUtils.jsonList(uniqueValues)},false));
            System.err.println(json);
            cache.put(valueKey, json);
        }
        return new Result(BLANK, json,
                          getRepository().getMimeTypeFromSuffix(".json"));
    }


    public void makeMetadataTree(Request request, Entry entry, StringBuffer sb, int colIdx, List<Clause>clauses)     throws Exception {

    }


    public void makeMetadataTree(Request request, Entry entry)     throws Exception {
        StringBuffer tree = new StringBuffer();
        tree.append("<ul>");
        for(Column column: columns) {
            List<Clause> clauses = new ArrayList<Clause>();
            List<String> uniqueValues= getUniqueValues(entry, column, clauses);
            for(String v: uniqueValues) {
                tree.append("<li> " + v);
            }
        }
        tree.append("</ul>");
    }




    private List<String> getUniqueValues(Entry entry, Column column, List<Clause> clauses) throws Exception {
        clauses = new ArrayList<Clause>(clauses);
        List<String> uniqueValues= new ArrayList<String>();
        if(column!=null) {
            clauses.add(Clause.eq(dbColumnCollectionId, entry.getId()));
            Statement stmt = getDatabaseManager().select(
                                                         SqlUtil.distinct(dbTableName+"."+column.getName()),
                                                         dbTableName, Clause.and(clauses));
            List<String> dbValues = (List<String>)Misc.toList(SqlUtil.readString(getRepository().getDatabaseManager().getIterator(stmt), 1));
            for(TwoFacedObject tfo: getValueList(entry, dbValues, column)) {
                uniqueValues.add(tfo.getId()+":" + tfo.getLabel());
            }
        }
        return uniqueValues;
    }


    public List<Clause> getClauses(Request request, Entry entry, List<String> values) throws Exception {
        List<Clause> clauses = new ArrayList<Clause>();
        Column nextColumn = columns.get(0);
        String nextColumnName = nextColumn.getLabel();
        for(int selectIdx=0;selectIdx<columns.size();selectIdx++) {
            if(!request.defined(selectArg+selectIdx)) break;
            if(selectIdx<columns.size()-1) {
                nextColumn = columns.get(selectIdx+1);
                nextColumnName = nextColumn.getLabel();
            } else {
                nextColumn = null;
            }
            String column=  columns.get(selectIdx).getName();
            String v = request.getString(selectArg+selectIdx,"");
            clauses.add(Clause.eq(column, v));
            if(values!=null) values.add(v);
        }
        return clauses;
    }


    private Hashtable<String,Properties> labelCache = new Hashtable<String,Properties>();

    private List<TwoFacedObject> getValueList(Entry collectionEntry, List values, Column column) throws Exception {
        Hashtable map  = column.getEnumTable();
        String key  = column.getName()+".values";
        String vocabFile = getProperty(key,(String) null);
        if(vocabFile!=null) {
            Properties properties = labelCache.get(vocabFile);
            if(properties == null) {
                properties = new Properties();
                getRepository().loadProperties(properties, vocabFile);
                labelCache.put(vocabFile, properties);
            }
            map  = new Hashtable();
            map.putAll(properties);
        }
        List<TwoFacedObject> tfos = new ArrayList<TwoFacedObject>();
        for(String value: (List<String>)values) {
            String label = (String) map.get(value);
            if(label == null) label = value;
            tfos.add(new TwoFacedObject(label, value));
        }
        return tfos;
    }


    public void addSelectorsToForm(Request request, Entry entry,
                                   List<Entry> subGroups, List<Entry> entries, StringBuffer sb, String formId, StringBuffer js)
        throws Exception {

        for(int selectIdx=0;selectIdx<columns.size();selectIdx++) {
            Column column = columns.get(selectIdx);
            String key = "values::" + entry.getId()+"::" +column.getName();
            List values = (List)cache.get(key);
            if(values == null) {
                Statement stmt = getRepository().getDatabaseManager().select(
                                                                             SqlUtil.distinct(dbTableName+"."+column.getName()),
                                                                             dbTableName, Clause.eq(dbColumnCollectionId, entry.getId()));
                values = getValueList(entry, Misc.toList(SqlUtil.readString(getRepository().getDatabaseManager().getIterator(stmt), 1)), column);
                values.add(0, new TwoFacedObject("-- Select " + Utils.getArticle(column.getLabel()) +" " +column.getLabel() + " --",""));
                cache.put(key, values);
            }
            String selectId = formId +"_"  + selectArg + selectIdx;
            String selectedValue = request.getString(selectArg+selectIdx,"");
            String selectBox = HtmlUtils.select(selectArg + selectIdx ,values,selectedValue,
                                                " style=\"min-width:250px;\" " +
                                                HtmlUtils.attr("id",selectId));
            sb.append(HtmlUtils.formEntry(msgLabel(column.getLabel()), selectBox));
            js.append(JQ.change(JQ.id(selectId), "return " + HtmlUtils.call(formId +".select" ,HtmlUtils.squote("" + selectIdx))));
        }
    }


    public void addJsonSelectorsToForm(Request request, Entry entry,
                                       List<Entry> subGroups, List<Entry> entries, StringBuffer sb, String formId,StringBuffer js)
        throws Exception {


        List firstValues =  (List)cache.get("firstValues::" + entry.getId());
        if(firstValues == null) {
            Statement stmt = getRepository().getDatabaseManager().select(
                                                                         SqlUtil.distinct(dbTableName+"."+columns.get(0).getName()),
                                                                         dbTableName, Clause.eq(dbColumnCollectionId, entry.getId()));
            firstValues = getValueList(entry, Misc.toList(SqlUtil.readString(getRepository().getDatabaseManager().getIterator(stmt), 1)), columns.get(0));
            cache.put("firstValues::" + entry.getId(), firstValues);
        }


        for(int selectIdx=0;selectIdx<columns.size();selectIdx++) {
            String column=  columns.get(selectIdx).getName();
            String label=  columns.get(selectIdx).getLabel();
            List values = new ArrayList();
            if(selectIdx==0) {
                values.add(new TwoFacedObject("-- Select a " + label + " --",""));
                values.addAll(firstValues);
            }  else {
                values.add(new TwoFacedObject("--",""));
            }
            String selectId = formId +"_"  + selectArg + selectIdx;
            String selectedValue = request.getString(selectArg+selectIdx,"");

            String selectBox = HtmlUtils.select(selectArg + selectIdx ,values,selectedValue,
                                                " style=\"min-width:250px;\" " +
                                                HtmlUtils.attr("id",selectId));
            sb.append(HtmlUtils.formEntry(msgLabel(label), selectBox));
            js.append(JQ.change(JQ.id(selectId), "return " + HtmlUtils.call(formId +".select" ,HtmlUtils.squote("" + selectIdx))));
        }
    }



    /**
     * Get the HTML display for this type
     *
     * @param request  the Request
     * @param group    the group
     * @param subGroups    the subgroups
     * @param entries      the Entries
     *
     * @return  the Result
     *
     * @throws Exception  problem getting the HTML
     */
@Override
    public Result getHtmlDisplay(Request request, Entry entry,
                                 List<Entry> subGroups, List<Entry> entries)
        throws Exception {
        //Always call this to initialize things
        getGranuleTypeHandler();

        //Check if the user clicked on tree view, etc.
        if ( !isDefaultHtmlOutput(request)) {
            return null;
        }

        StringBuffer sb     = new StringBuffer();
        if(request.exists(ARG_SEARCH)) {
            entries =  processSearch(request, entry);
            getJsonOutputHandler().makeJson(request, entries,sb);
            return new Result("", sb, "application/json");
        }

        if(request.get("metadata", false)) {
            return getMetadataJson(request, entry, subGroups, entries);
        }

        sb.append(entry.getDescription());
        String formId = "selectform" + HtmlUtils.blockCnt++;

        sb.append(HtmlUtils.form(request.entryUrl(getRepository().URL_ENTRY_SHOW, entry),
                                 HtmlUtils.attr("id", formId)));
        sb.append(HtmlUtils.formTable());

        StringBuffer js = new StringBuffer();
        js.append("var " + formId + " = new SelectForm(" + HtmlUtils.squote(formId)+"," + HtmlUtils.squote(entry.getId()) +");\n");
        addJsonSelectorsToForm(request, entry, subGroups, entries, sb, formId, js);
        sb.append(js);

        sb.append(HtmlUtils.formTableClose());
        sb.append(HtmlUtils.p());
        sb.append(HtmlUtils.submit(ARG_SEARCH,"Submit", HtmlUtils.id(formId+".search")));
        js.append(JQ.submit(JQ.id(formId+".search"), "return " + HtmlUtils.call(formId +".search","")));
        sb.append(HtmlUtils.script(js.toString()));
        sb.append(HtmlUtils.formClose());


        return new Result(msg(getLabel()), sb);


    }


    public  void addClauses(Request request, Entry group, List<Clause> clauses) throws Exception {
        clauses.add(Clause.eq(dbTableName +"." + dbColumnCollectionId, group.getId()));
        clauses.add(Clause.join(Tables.ENTRIES.COL_ID,
                                dbTableName + ".id"));
        for(int i=0;i<columns.size();i++) {
            String column=  columns.get(i).getName();
            String urlArg = selectArg + i;
            if(request.defined(urlArg)) {
                clauses.add(Clause.makeOrSplit(dbTableName +"." + column, request.getString(urlArg)));
            }
        }
    }


    public  List<Entry> processSearch(Request request, Entry group) throws Exception {
        List<Clause> clauses = new ArrayList<Clause>();
        addClauses(request, group, clauses);
        List[] pair = getEntryManager().getEntries(request, clauses, getGranuleTypeHandler());
        //        pair[0] is the folder entries. shouldn't have any here
        return (List<Entry>) pair[1];
    }


}
