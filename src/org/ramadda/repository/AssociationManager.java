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

package org.ramadda.repository;


import org.ramadda.repository.auth.*;

import org.ramadda.repository.database.*;
import org.ramadda.repository.output.*;
import org.ramadda.repository.type.*;



import org.w3c.dom.*;

import ucar.unidata.sql.Clause;

import ucar.unidata.sql.SqlUtil;




import ucar.unidata.util.HtmlUtil;
import ucar.unidata.util.Misc;
import ucar.unidata.util.StringUtil;
import ucar.unidata.util.TwoFacedObject;
import ucar.unidata.xml.XmlUtil;



import java.io.*;

import java.net.*;

import java.sql.ResultSet;
import java.sql.Statement;


import java.util.ArrayList;
import java.util.Date;

import java.util.Hashtable;
import java.util.List;
import java.util.Map;






/**
 *
 *
 * @author RAMADDA Development Team
 * @version $Revision: 1.3 $
 */
public class AssociationManager extends RepositoryManager {

    /** _more_ */
    private List<String> types = null;

    /**
     * _more_
     *
     * @param repository _more_
     */
    public AssociationManager(Repository repository) {
        super(repository);
    }

    /**
     * _more_
     *
     * @param request The request
     *
     * @return _more_
     *
     * @throws Exception On badness
     */
    public Result processAssociationAdd(Request request) throws Exception {
        Entry fromEntry = getEntryManager().getEntry(request,
                              request.getString(ARG_FROM, BLANK));
        Entry toEntry = getEntryManager().getEntry(request,
                            request.getString(ARG_TO, BLANK));
        if (fromEntry == null) {
            throw new RepositoryUtil.MissingEntryException(
                "Could not find entry:" + request.getString(ARG_FROM, BLANK));
        }
        if (toEntry == null) {
            throw new RepositoryUtil.MissingEntryException(
                "Could not find entry:" + request.getString(ARG_TO, BLANK));
        }




        String name = request.getString(ARG_NAME, (String) null);
        if (name != null) {
            String type = request.getString(ARG_TYPE_FREEFORM, "").trim();
            if (type.length() == 0) {
                type = request.getString(ARG_TYPE, "").trim();
            }
            request.ensureAuthToken();
            addAssociation(request, fromEntry, toEntry, name, type);
            //            return new Result(request.entryUrl(getRepository().URL_ENTRY_SHOW, fromEntry));
            return new Result(
                request.entryUrl(
                    getRepositoryBase().URL_ENTRY_SHOW, fromEntry,
                    ARG_MESSAGE,
                    getRepository().translate(
                        request, MSG_ASSOCIATION_ADDED)));
        }


        StringBuffer sb = new StringBuffer();
        sb.append(msgHeader("Add Association"));
        sb.append("Add association between " + fromEntry.getLabel());
        sb.append(" and  " + toEntry.getLabel());
        request.formPostWithAuthToken(sb, getRepository().URL_ASSOCIATION_ADD, BLANK);
        sb.append(HtmlUtil.br());
        sb.append(HtmlUtil.formTable());

        sb.append(HtmlUtil.formEntry(msgLabel("Association Name"),
                                     HtmlUtil.input(ARG_NAME)));

        List types = getAssociationManager().getTypes();
        types.add(0, new TwoFacedObject("None", ""));
        String select = ((types.size() == 1)
                         ? ""
                         : HtmlUtil.select(ARG_TYPE, types)
                           + HtmlUtil.space(1) + "Or:" + HtmlUtil.space(1));
        sb.append(HtmlUtil.formEntry(msgLabel("Type"),
                                     select
                                     + HtmlUtil.input(ARG_TYPE_FREEFORM, "",
                                         HtmlUtil.SIZE_20)));

        sb.append(HtmlUtil.formTableClose());

        sb.append(HtmlUtil.hidden(ARG_FROM, fromEntry.getId()));
        sb.append(HtmlUtil.hidden(ARG_TO, toEntry.getId()));
        sb.append(HtmlUtil.space(1));
        sb.append(HtmlUtil.submit(msg("Add Association")));
        sb.append(HtmlUtil.formClose());

        return getEntryManager().addEntryHeader(request, fromEntry,
                new Result("Add Association", sb));


    }


    /**
     * _more_
     *
     * @param request The request
     *
     * @return _more_
     *
     * @throws Exception On badness
     */
    public Result processAssociationDelete(Request request) throws Exception {
        String associationId = request.getString(ARG_ASSOCIATION, "");
        Clause clause = Clause.eq(Tables.ASSOCIATIONS.COL_ID, associationId);
        List<Association> associations = getAssociations(request, clause);
        if (associations.size() == 0) {
            return new Result(
                msg("Delete Associations"),
                new StringBuffer(
                    getRepository().showDialogError(
                        "Could not find assocation")));
        }

        Entry fromEntry = getEntryManager().getEntry(request,
                              associations.get(0).getFromId());
        Entry toEntry = getEntryManager().getEntry(request,
                            associations.get(0).getToId());

        if (request.exists(ARG_CANCEL)) {
            return new Result(
                request.entryUrl(getRepository().URL_ENTRY_SHOW, fromEntry));
        }


        if (request.exists(ARG_DELETE_CONFIRM)) {
            request.ensureAuthToken();
            getDatabaseManager().delete(Tables.ASSOCIATIONS.NAME, clause);
            fromEntry.setAssociations(null);
            toEntry.setAssociations(null);
            return new Result(
                request.entryUrl(getRepository().URL_ENTRY_SHOW, fromEntry));
        }
        StringBuffer sb     = new StringBuffer();
        StringBuffer hidden = new StringBuffer();
        getRepository().addAuthToken(request, hidden);
        hidden.append(HtmlUtil.hidden(ARG_ASSOCIATION, associationId));
        String form = Repository.makeOkCancelForm(request,
                          getRepository().URL_ASSOCIATION_DELETE,
                          ARG_DELETE_CONFIRM, hidden.toString());
        sb.append(
            getRepository().showDialogQuestion(
                msg("Are you sure you want to delete the assocation?"),
                form));

        sb.append(associations.get(0).getName());
        sb.append(HtmlUtil.br());
        sb.append(fromEntry.getLabel());
        sb.append(HtmlUtil.pad(HtmlUtil.img(iconUrl(ICON_ARROW))));
        sb.append(toEntry.getLabel());
        return new Result(msg("Delete Associations"), sb);
    }




    /**
     * _more_
     *
     * @param request The request
     * @param node _more_
     * @param entries _more_
     * @param files _more_
     *
     * @return _more_
     *
     * @throws Exception On badness
     */
    public String processAssociationXml(Request request, Element node,
                                        Hashtable entries, Hashtable files)
            throws Exception {

        String fromId    = XmlUtil.getAttribute(node, ATTR_FROM);
        String toId      = XmlUtil.getAttribute(node, ATTR_TO);
        Entry  fromEntry = (Entry) entries.get(fromId);
        Entry  toEntry   = (Entry) entries.get(toId);
        if (fromEntry == null) {
            fromEntry = getEntryManager().getEntry(request, fromId);
            if (fromEntry == null) {
                throw new RepositoryUtil.MissingEntryException(
                    "Could not find from entry:" + fromId);
            }
        }
        if (toEntry == null) {
            toEntry = getEntryManager().getEntry(request, toId);
            if (toEntry == null) {
                throw new RepositoryUtil.MissingEntryException(
                    "Could not find to entry:" + toId);
            }
        }
        return addAssociation(request, fromEntry, toEntry,
                              XmlUtil.getAttribute(node, ATTR_NAME, ""),
                              XmlUtil.getAttribute(node, ATTR_TYPE, ""));
    }



    /**
     * Add an association between the two entries
     *
     * @param request request
     * @param fromEntry _more_
     * @param toEntry _more_
     * @param name _more_
     * @param type _more_
     *
     * @return _more_
     *
     * @throws Exception On badness
     */
    public String addAssociation(Request request, Entry fromEntry,
                                 Entry toEntry, String name, String type)
            throws Exception {
        if ( !getAccessManager().canDoAction(request, fromEntry,
                                             Permission.ACTION_NEW)) {
            throw new IllegalArgumentException("Cannot add association to "
                    + fromEntry);
        }
        if ( !getAccessManager().canDoAction(request, toEntry,
                                             Permission.ACTION_NEW)) {
            throw new IllegalArgumentException("Cannot add association to "
                    + toEntry);
        }
        //Clear the cached associations
        String result =
            addAssociation(request,
                           new Association(getRepository().getGUID(), name,
                                           type, fromEntry.getId(),
                                           toEntry.getId()));
        fromEntry.clearAssociations();
        toEntry.clearAssociations();
        return result;
    }


    /**
     * _more_
     *
     * @param request The request
     * @param association _more_
     *
     * @return _more_
     *
     * @throws Exception On badness
     */
    public String addAssociation(Request request, Association association)
            throws Exception {
        request.ensureAuthToken();
        String id = getRepository().getGUID();
        getDatabaseManager().executeInsert(Tables.ASSOCIATIONS.INSERT,
                                           new Object[] { association.getId(),
                association.getName(), association.getType(),
                association.getFromId(),
                association.getToId() });
        return id;
    }



    /**
     * _more_
     *
     * @return _more_
     *
     * @throws Exception On badness
     */
    public List<String> getTypes() throws Exception {
        if (types == null) {
            Statement stmt =
                getDatabaseManager().select(
                    SqlUtil.distinct(Tables.ASSOCIATIONS.COL_TYPE),
                    Tables.ASSOCIATIONS.NAME, (Clause) null);
            String[] values =
                SqlUtil.readString(getDatabaseManager().getIterator(stmt), 1);
            types = (List<String>) Misc.toList(values);
            types.remove("");
        }
        return new ArrayList<String>(types);
    }




    /**
     * _more_
     *
     * @param request The request
     * @param association _more_
     *
     * @throws Exception On badness
     */
    public void associationChanged(Request request, Association association)
            throws Exception {
        types = null;
        Entry fromEntry = getEntryManager().getEntry(request,
                              association.getFromId());
        if (fromEntry != null) {
            fromEntry.setAssociations(null);
        }
        Entry toEntry = getEntryManager().getEntry(request,
                            association.getToId());
        if (toEntry != null) {
            toEntry.setAssociations(null);
        }

    }


    /**
     * _more_
     *
     * @param request The request
     * @param association _more_
     *
     * @throws Exception On badness
     */
    public void deleteAssociation(Request request, Association association)
            throws Exception {
        request.ensureAuthToken();
        getDatabaseManager().delete(Tables.ASSOCIATIONS.NAME,
                                    Clause.eq(Tables.ASSOCIATIONS.COL_ID,
                                        association.getId()));
        types = null;
    }

    /**
     * _more_
     *
     * @param request The request
     * @param association _more_
     *
     * @return _more_
     *
     * @throws Exception On badness
     */
    public String getAssociationLinks(Request request, String association)
            throws Exception {
        if (true) {
            return BLANK;
        }
        String search =
            HtmlUtil.href(
                request.url(
                    getRepository().getSearchManager().URL_SEARCH_FORM,
                    ARG_ASSOCIATION,
                    HtmlUtil.urlEncode(association)), HtmlUtil.img(
                        iconUrl(ICON_SEARCH), msg("Search in association")));

        return search;
    }



    /**
     * _more_
     *
     * @param request The request
     * @param entryId _more_
     *
     * @return _more_
     *
     * @throws Exception On badness
     */
    public List<Association> getAssociations(Request request, String entryId)
            throws Exception {
        Entry entry = getEntryManager().getEntry(request, entryId);
        if (entry == null) {
            throw new IllegalArgumentException(
                "getAssociations Entry is null:" + entryId);
        }
        return getAssociations(request, entry);
    }


    /**
     * _more_
     *
     * @param request The request
     * @param entry _more_
     *
     * @return _more_
     *
     * @throws Exception On badness
     */
    public List<Association> getAssociations(Request request, Entry entry)
            throws Exception {
        if (entry.getAssociations() != null) {
            return entry.getAssociations();
        }
        if (entry.isDummy()) {
            return new ArrayList<Association>();
        }

        List<Association> associations =
            getAssociations(
                request,
                Clause.or(
                    Clause.eq(
                        Tables.ASSOCIATIONS.COL_FROM_ENTRY_ID,
                        entry.getId()), Clause.eq(
                            Tables.ASSOCIATIONS.COL_TO_ENTRY_ID,
                            entry.getId())));
        entry.setAssociations(associations);
        return associations;
    }


    /**
     * _more_
     *
     * @param request The request
     * @param clause _more_
     *
     * @return _more_
     *
     * @throws Exception On badness
     */
    public List<Association> getAssociations(Request request, Clause clause)
            throws Exception {
        int max = request.get(ARG_MAX, DB_MAX_ROWS);
        String orderBy = " ORDER BY " + Tables.ASSOCIATIONS.COL_TYPE
                         + " ASC ," + Tables.ASSOCIATIONS.COL_NAME + " ASC ";
        Statement stmt = getDatabaseManager().select(
                             Tables.ASSOCIATIONS.COLUMNS,
                             Tables.ASSOCIATIONS.NAME, clause,
                             orderBy + " "
                             + getDatabaseManager().getLimitString(
                                 request.get(ARG_SKIP, 0), max));
        List<Association> associations = new ArrayList();
        SqlUtil.Iterator  iter = getDatabaseManager().getIterator(stmt);
        ResultSet         results;
        while ((results = iter.getNext()) != null) {
            Association association = new Association(results.getString(1),
                                          results.getString(2),
                                          results.getString(3),
                                          results.getString(4),
                                          results.getString(5));

            Entry fromEntry = getEntryManager().getEntry(request,
                                  association.getFromId());
            Entry toEntry = getEntryManager().getEntry(request,
                                association.getToId());
            if ((fromEntry != null) && (toEntry != null)) {
                associations.add(association);
            }
        }
        return associations;
    }





    /**
     * _more_
     *
     * @param request The request
     *
     * @return _more_
     *
     * @throws Exception On badness
     */
    public String[] getAssociations(Request request) throws Exception {
        TypeHandler  typeHandler = getRepository().getTypeHandler(request);
        List<Clause> where       = typeHandler.assembleWhereClause(request);
        if (where.size() > 0) {
            where.add(0, Clause.eq(Tables.ASSOCIATIONS.COL_FROM_ENTRY_ID,
                                   Tables.ENTRIES.COL_ID));
            where.add(0, Clause.eq(Tables.ASSOCIATIONS.COL_TO_ENTRY_ID,
                                   Tables.ENTRIES.COL_ID));
        }

        return SqlUtil.readString(
            getDatabaseManager().getIterator(
                typeHandler.select(
                    request, SqlUtil.distinct(Tables.ASSOCIATIONS.COL_NAME),
                    where, "")), 1);
    }



    /**
     * _more_
     *
     * @param request The request
     * @param entry _more_
     * @param text _more_
     *
     * @return _more_
     */
    public String processText(Request request, Entry entry, String text) {
        int idx = text.indexOf("<more>");
        if (idx >= 0) {
            String first  = text.substring(0, idx);
            String base   = "" + (HtmlUtil.blockCnt++);
            String divId  = "morediv_" + base;
            String linkId = "morelink_" + base;
            String second = text.substring(idx + "<more>".length());
            String moreLink = "javascript:showMore(" + HtmlUtil.squote(base)
                              + ")";
            String lessLink = "javascript:hideMore(" + HtmlUtil.squote(base)
                              + ")";
            text = first + "<br><a " + HtmlUtil.id(linkId) + " href="
                   + HtmlUtil.quote(moreLink)
                   + ">More...</a><div style=\"\" class=\"moreblock\" "
                   + HtmlUtil.id(divId) + ">" + second + "<br>" + "<a href="
                   + HtmlUtil.quote(lessLink) + ">...Less</a>" + "</div>";
        }
        return text;
    }



    /**
     * _more_
     *
     * @param request The request
     * @param entry _more_
     *
     *
     * @return _more_
     * @throws Exception On badness
     */
    public StringBuffer getAssociationBlock(Request request, Entry entry)
            throws Exception {

        boolean canEdit = getAccessManager().canDoAction(request, entry,
                              Permission.ACTION_EDIT);
        List<Association> associations =
            getAssociationManager().getAssociations(request, entry);
        if (associations.size() == 0) {
            StringBuffer sb = new StringBuffer();
            return sb;
        }
        return getAssociationList(request, associations, entry, canEdit);
    }

    /**
     * _more_
     *
     * @param request The request
     * @param associations _more_
     * @param entry _more_
     * @param canEdit _more_
     *
     * @return _more_
     *
     * @throws Exception On badness
     */
    public StringBuffer getAssociationList(Request request,
                                           List<Association> associations,
                                           Entry entry, boolean canEdit)
            throws Exception {

        List cols1 = new ArrayList();
        List cols2 = new ArrayList();

        Hashtable<String, StringBuffer> rowMap = new Hashtable<String,
                                                     StringBuffer>();
        List<String> rows = new ArrayList<String>();
        for (Association association : associations) {
            Entry fromEntry = null;
            Entry toEntry   = null;
            List  cols;
            if ((entry != null)
                    && association.getFromId().equals(entry.getId())) {
                cols      = cols1;
                fromEntry = entry;
            } else {
                fromEntry = getEntryManager().getEntry(request,
                        association.getFromId());
                cols = cols2;
            }
            if ((entry != null)
                    && association.getToId().equals(entry.getId())) {
                toEntry = entry;
            } else {
                toEntry = getEntryManager().getEntry(request,
                        association.getToId());
            }
            if ((fromEntry == null) || (toEntry == null)) {
                continue;
            }
            if (canEdit) {
                cols.add(
                    HtmlUtil.pad(
                        HtmlUtil.href(
                            request.url(
                                getRepository().URL_ASSOCIATION_DELETE,
                                ARG_ASSOCIATION,
                                association.getId()), HtmlUtil.img(
                                    getRepository().iconUrl(ICON_DELETE),
                                    msg("Delete association")))));
            } else {
                cols.add("");
            }
            List args = Misc.newList(ARG_SHOW_ASSOCIATIONS, "true");
            cols.add(HtmlUtil.img(getEntryManager().getIconUrl(request,
                    fromEntry)) + HtmlUtil.pad((Misc.equals(fromEntry, entry)
                    ? fromEntry.getLabel()
                    : getEntryManager().getEntryLink(request, fromEntry,
                    args))));

            cols.add(association.getType());
            //            cols.add(association.getLabel());
            cols.add(HtmlUtil.img(getRepository().iconUrl(ICON_ARROW)));
            cols.add(HtmlUtil.img(getEntryManager().getIconUrl(request,
                    toEntry)) + HtmlUtil.pad((Misc.equals(toEntry, entry)
                    ? toEntry.getLabel()
                    : getEntryManager().getEntryLink(request, toEntry,
                    args))));
        }

        List cols = Misc.toList(new Object[] { "&nbsp;",
                HtmlUtil.bold(msg("From")), HtmlUtil.bold(msg("Type")),
        /*HtmlUtil.bold(msg("Name")),*/
        "&nbsp;", HtmlUtil.bold(msg("To")) });

        cols.addAll(cols1);
        cols.addAll(cols2);

        return HtmlUtil.table(cols, 5,
                              HtmlUtil.attr(HtmlUtil.ATTR_CELLSPACING, "3"));
    }


    /**
     * _more_
     *
     * @param request The request
     *
     * @return _more_
     *
     * @throws Exception On badness
     */
    public Result processSearchAssociations(Request request)
            throws Exception {
        StringBuffer sb      = new StringBuffer();
        String       type    = request.getString(ARG_TYPE, "").trim();
        String       name    = request.getString(ARG_NAME, "").trim();
        List<Clause> clauses = new ArrayList<Clause>();
        if (type.length() > 0) {
            clauses.add(Clause.eq(Tables.ASSOCIATIONS.COL_TYPE, type));
        }

        if (name.length() > 0) {
            if (request.get(ARG_EXACT, false)) {
                clauses.add(Clause.eq(Tables.ASSOCIATIONS.COL_NAME, name));
            } else {
                clauses.add(Clause.like(Tables.ASSOCIATIONS.COL_NAME,
                                        "%" + name + "%"));
            }
        }
        List<Association> associations =
            getAssociationManager().getAssociations(request,
                Clause.and(clauses));
        int     max = request.get(ARG_MAX, DB_MAX_ROWS);
        int     cnt = associations.size();
        boolean showingAll;
        if ((cnt > 0) && ((cnt == max) || request.defined(ARG_SKIP))) {
            showingAll = false;
        } else {
            showingAll = true;
        }

        if (associations.size() == 0) {
            sb.append(
                getRepository().showDialogNote(msg("No associations found")));
            getAssociationsSearchForm(request, sb);
        } else {
            getAssociationsSearchForm(request, sb);
            getRepository().getHtmlOutputHandler().showNext(request, cnt, sb);
            sb.append(getAssociationManager().getAssociationList(request,
                    associations, null, false));
        }

        return getSearchManager().makeResult(request,
                                             msg("Search Associations"), sb);
    }


    /**
     * _more_
     *
     * @param request The request
     *
     * @return _more_
     *
     * @throws Exception On badness
     */
    public Result processSearchAssociationsForm(Request request)
            throws Exception {

        StringBuffer sb = new StringBuffer();
        getAssociationsSearchForm(request, sb);
        return getSearchManager().makeResult(request,
                                             msg("Search Associations"), sb);
    }



    /**
     * _more_
     *
     * @param request The request
     * @param sb buffer to append to
     *
     * @throws Exception On badness
     */
    private void getAssociationsSearchForm(Request request, StringBuffer sb)
            throws Exception {
        sb.append(HtmlUtil.form(request
                .url(getRepository().getSearchManager()
                    .URL_SEARCH_ASSOCIATIONS, ARG_NAME,
                        WHAT_ENTRIES), " name=\"searchform\" "));

        sb.append(HtmlUtil.formTable());

        String searchExact = " "
                             + HtmlUtil.checkbox(ARG_EXACT, "true",
                                 request.get(ARG_EXACT, false)) + " "
                                     + msg("Match exactly");
        sb.append(HtmlUtil.formEntry(msgLabel("Name"),
                                     HtmlUtil.input(ARG_NAME,
                                         request.getString(ARG_NAME, ""),
                                         HtmlUtil.SIZE_40) + searchExact));


        List types = getAssociationManager().getTypes();
        types.add(0, new TwoFacedObject(msg("None"), ""));
        if (types.size() > 1) {
            sb.append(HtmlUtil.formEntry(msgLabel("Type"),
                                         HtmlUtil.select(ARG_TYPE, types,
                                             request.getString(ARG_TYPE,
                                                 ""))));
        }


        sb.append(HtmlUtil.formTableClose());

        OutputType output  = request.getOutput(BLANK);
        String     buttons = HtmlUtil.submit(msg("Search"), "submit");
        sb.append(HtmlUtil.p());
        sb.append(buttons);
        sb.append(HtmlUtil.p());
        sb.append(HtmlUtil.formClose());

    }





}
