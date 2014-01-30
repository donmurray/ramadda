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

package org.ramadda.repository.output;


import org.ramadda.data.services.*;


import org.ramadda.repository.*;
import org.ramadda.repository.auth.*;
import org.ramadda.repository.map.*;


import org.ramadda.repository.metadata.*;
import org.ramadda.repository.search.*;
import org.ramadda.repository.type.*;
import org.ramadda.repository.util.DateArgument;
import org.ramadda.repository.util.ServerInfo;
import org.ramadda.util.BufferMapList;
import org.ramadda.util.HtmlUtils;
import org.ramadda.util.Json;
import org.ramadda.util.Utils;
import org.ramadda.util.WikiUtil;

import ucar.unidata.util.Misc;


import ucar.unidata.util.StringUtil;
import ucar.unidata.util.TwoFacedObject;

import java.io.*;

import java.net.*;

import java.text.SimpleDateFormat;

import java.util.ArrayList;
import java.util.Date;
import java.util.Enumeration;

import java.util.GregorianCalendar;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.List;
import java.util.Properties;

import java.util.regex.*;


/**
 * Provides wiki text processing services
 */
public class WikiManager extends RepositoryManager implements WikiUtil
    .WikiPageHandler {

    /** id counter */
    static int idCounter = 0;

    /** wiki page type */
    public static String TYPE_WIKIPAGE = "wikipage";

    /** attribute in import tag */
    public static final String ATTR_ENTRY = "entry";

    /** _more_ */
    public static final String ATTR_ALIGN = "align";

    /** _more_ */
    public static final String ATTR_ID = "id";


    /** _more_ */
    public static final String ATTR_BLOCK_SHOW = "block.show";

    /** _more_ */
    public static final String ATTR_BLOCK_OPEN = "block.open";

    /** _more_ */
    public static final String ATTR_BLOCK_TITLE = "block.title";

    /** _more_ */
    public static final String ATTR_BLOCK_POPUP = "block.popup";

    /** _more_ */
    public static final String ATTR_ROW_LABEL = "row.label";


    /** border attribute */
    public static final String ATTR_BORDER = "border";

    /** _more_ */
    public static final String ATTR_METADATA_TYPES = "metadata.types";

    /** _more_ */
    public static final String ATTR_PADDING = "padding";

    /** border color */
    public static final String ATTR_BORDERCOLOR = "bordercolor";

    public static final String ATTR_COLORS = "colors";

    /** show the details attribute */
    public static final String ATTR_DETAILS = "details";

    /** _more_ */
    public static final String ATTR_DECORATE = "decorate";

    /** maximum attribute */
    public static final String ATTR_MAX = "max";

    /** linkresource attribute */
    public static final String ATTR_LINKRESOURCE = "linkresource";

    /** listentries attribute */
    public static final String ATTR_LISTENTRIES = "listentries";

    /** _more_ */
    public static final String ATTR_LAYER = "layer";

    /** listwidth attribute */
    public static final String ATTR_LISTWIDTH = "listwidth";

    /** link attribute */
    public static final String ATTR_LINK = "link";

    /** attribute in the tabs tag */
    public static final String ATTR_USEDESCRIPTION = "usedescription";

    /** attribute in the tabs tag */
    public static final String ATTR_SHOWLINK = "showlink";

    /** _more_          */
    public static final String ATTR_SHOWTITLE = "showTitle";

    /** _more_          */
    public static final String ATTR_SHOWMAP = "showMap";

    /** _more_          */
    public static final String ATTR_SHOWMENU = "showMenu";

    /** src attribute */
    public static final String ATTR_SRC = "src";

    /** _more_ */
    public static final String ATTR_PREFIX = "prefix";

    /** _more_ */
    public static final String ATTR_SUFFIX = "suffix";


    /** _more_ */
    public static final String ATTR_IF = "if";


    /** include icon attribute */
    public static final String ATTR_INCLUDEICON = "includeicon";

    /** _more_ */
    public static final String ATTR_SHOWDESCRIPTION = "showdescription";


    /** _more_ */
    public static final String ATTR_ICON = "icon";

    /** attribute in the tabs tag */
    public static final String ATTR_LINKLABEL = "linklabel";

    /** attribute in import tag */
    public static final String ATTR_ENTRIES = "entries";

    /** exclude attribute */
    public static final String ATTR_EXCLUDE = "exclude";

    /** first attribute */
    public static final String ATTR_FIRST = "first";

    /** _more_ */
    public static final String ATTR_LAST = "last";

    /** sort attribute */
    public static final String ATTR_SORT = "sort";

    /** sort order attribute */
    public static final String ATTR_SORT_ORDER = "sortorder";

    /** sort date attribute */
    public static final String SORT_DATE = "date";

    /** change date attribute */
    public static final String SORT_CHANGEDATE = "changedate";

    /** sort name attribute */
    public static final String SORT_NAME = "name";

    /** the message attribute */
    public static final String ATTR_MESSAGE = "message";

    /** the associations attribute */
    public static final String ATTR_ASSOCIATIONS = "associations";

    /** attribute in import tag */
    public static final String ATTR_SHOWHIDE = "showhide";

    /** attribute in import tag */
    public static final String ATTR_OUTPUT = "output";

    /** attribute in import tag */
    public static final String ATTR_RANDOM = "random";

    /** attribute in import tag */
    public static final String ATTR_CHILDREN = "children";

    /** _more_ */
    public static final String ATTR_CONSTRAINSIZE = "constrainsize";

    /** attribute in import tag */
    public static final String ATTR_FORMAT = "format";

    /** attribute in import tag */
    public static final String ATTR_SHOWTOGGLE = "showtoggle";

    /** attribute in import tag */
    public static final String ATTR_OPEN = "open";

    /** attribute in import tag */
    public static final String ATTR_FILES = "files";

    /** attribute in import tag */
    public static final String ATTR_FOLDERS = "folders";

    /** images only attribute */
    public static final String ATTR_IMAGES = "images";

    /** attribute in import tag */
    public static final String ATTR_TYPE = "type";

    /** thumbnail attribute */
    public static final String ATTR_THUMBNAIL = "thumbnail";

    /** caption attribute */
    public static final String ATTR_CAPTION = "caption";

    /** attribute in import tag */
    public static final String ATTR_SEPARATOR = "separator";

    /** attribute in import tag */
    public static final String ATTR_CLASS = "class";

    /** attribute in import tag */
    public static final String ATTR_STYLE = "style";

    /** _more_ */
    public static final String ATTR_TAG = "tag";


    /** attribute in import tag */
    public static final String ATTR_TAGOPEN = "tagopen";

    /** attribute in import tag */
    public static final String ATTR_TAGCLOSE = "tagclose";

    /** attribute in import tag */
    public static final String ATTR_TITLE = "title";

    /** attribute in import tag */
    public static final String ATTR_LAYOUT = "layout";

    /** the columns attribute */
    public static final String ATTR_COLUMNS = "columns";

    /** the fieldname attribute */
    public static final String ATTR_FIELDNAME = "name";

    /** attribute in import tag */
    public static final String LAYOUT_HORIZONTAL = "hor";

    /** attribute in import tag */
    public static final String LAYOUT_VERTICAL = "vert";

    /** attribute in import tag */
    public static final String ATTR_MENUS = "menus";

    /** attribute in import tag */
    public static final String ATTR_REQUEST = "request";

    /** _more_ */
    public static final String ATTR_POPUP = "popup";

    /** attribute in import tag */
    public static final String ATTR_POPUPCAPTION = "popupcaption";

    /** attribute in import tag */
    public static final String ATTR_LEVEL = "level";

    /** attribute in import tag */
    public static final String ATTR_COUNT = "count";

    /** attribute to wikify the content */
    public static final String ATTR_WIKIFY = "wikify";

    /** max image height attribute */
    public static final String ATTR_MAXIMAGEHEIGHT = "maximageheight";

    /** _more_ */
    public static final String ATTR_MAXHEIGHT = "maxheight";

    /** _more_ */
    public static final String ATTR_MINHEIGHT = "minheight";

    /** attribute in import tag */
    public static final String ATTR_DAY = "day";

    /** attribute in import tag */
    public static final String ATTR_DAYS = "days";

    /** wiki group property */
    public static final String WIKI_PROP_GROUP = "wiki.group";

    /** _more_ */
    public static final String WIKI_PROP_DISPLAYGROUP = "displaygroup";

    /** _more_ */
    public static final String WIKI_PROP_DISPLAY = "display";

    /** wiki import */
    public static final String WIKI_PROP_IMPORT = "import";

    /** the field property */
    public static final String WIKI_PROP_FIELD = "field";

    /** _more_ */
    public static final String WIKI_PROP_ROOT = "root";

    /** the calendar property */
    public static final String WIKI_PROP_CALENDAR = "calendar";

    /** _more_ */
    public static final String WIKI_PROP_GRAPH = "graph";

    /** the timeline property */
    public static final String WIKI_PROP_TIMELINE = "timeline";

    /** wiki import */
    public static final String WIKI_PROP_DATE = "date";

    /** wiki import */
    public static final String WIKI_PROP_DATE_FROM = "fromdate";

    /** wiki import */
    public static final String WIKI_PROP_DATE_TO = "todate";

    /** wiki import */
    public static final String WIKI_PROP_MENU = "menu";


    /** _more_ */
    public static final String WIKI_PROP_SEARCH = "search";

    /** wiki import */
    public static final String WIKI_PROP_TREE = "tree";

    /** _more_ */
    public static final String WIKI_PROP_TREEVIEW = "treeview";

    /** the table property */
    public static final String WIKI_PROP_TABLE = "table";

    /** wiki import */
    public static final String WIKI_PROP_COMMENTS = "comments";

    /** _more_ */
    public static final String WIKI_PROP_TAGCLOUD = "tagcloud";

    /** wiki import */
    public static final String WIKI_PROP_RECENT = "recent";

    /** wiki import */
    public static final String WIKI_PROP_GALLERY = "gallery";

    /** the image player property */
    public static final String WIKI_PROP_PLAYER = "imageplayer";

    /** the old image player property */
    private static final String WIKI_PROP_PLAYER_OLD = "player";

    /** wiki import */
    public static final String WIKI_PROP_TABS = "tabs";

    /** _more_ */
    public static final String WIKI_PROP_APPLY = "apply";

    /** _more_ */
    public static final String APPLY_PREFIX = "apply.";

    /** _more_ */
    public static final String ATTR_APPLY_TAG = APPLY_PREFIX + "tag";

    /** accordian property */
    public static final String WIKI_PROP_ACCORDIAN = "accordian";

    /** the slideshow property */
    public static final String WIKI_PROP_SLIDESHOW = "slideshow";

    /** wiki import */
    public static final String WIKI_PROP_GRID = "grid";

    /** wiki import */
    public static final String WIKI_PROP_TOOLBAR = "toolbar";

    /** wiki import */
    public static final String WIKI_PROP_BREADCRUMBS = "breadcrumbs";

    /** wiki import */
    public static final String WIKI_PROP_INFORMATION = "information";

    /** _more_ */
    public static final String WIKI_PROP_DOWNLOAD = "download";

    /** wiki import */
    public static final String WIKI_PROP_IMAGE = "image";

    /** wiki import */
    public static final String WIKI_PROP_NAME = "name";

    /** wiki import */
    public static final String WIKI_PROP_MAP = "map";

    /** wiki import */
    public static final String WIKI_PROP_EARTH = "earth";

    /** wiki import */
    public static final String WIKI_PROP_HTML = "html";

    /** wiki import */
    public static final String WIKI_PROP_MAPENTRY = "mapentry";

    /** wiki import */
    public static final String WIKI_PROP_DESCRIPTION = "description";

    /** _more_ */
    public static final String WIKI_PROP_SIMPLE = "simple";

    /** wiki import */
    public static final String WIKI_PROP_PROPERTIES = "properties";

    /** wiki import */
    public static final String WIKI_PROP_LINKS = "links";

    /** link property */
    public static final String WIKI_PROP_LINK = "link";

    /** list property */
    public static final String WIKI_PROP_LIST = "list";

    /** wiki import */
    public static final String WIKI_PROP_ENTRYID = "entryid";

    /** wiki import */
    public static final String WIKI_PROP_LAYOUT = "layout";

    /** wiki import */
    public static final String WIKI_PROP_CHILDREN_GROUPS = "subgroups";

    /** wiki import */
    public static final String WIKI_PROP_CHILDREN_ENTRIES = "subentries";

    /** wiki import */
    public static final String WIKI_PROP_CHILDREN = "children";

    /** wiki import */
    public static final String WIKI_PROP_URL = "url";

    /** _more_ */

    public static final String WIKI_PROP_RESOURCE = "resource";

    /** _more_ */
    public static final String WIKI_PROP_TOOLS = "tools";

    /** Upload property */
    public static final String WIKI_PROP_UPLOAD = "upload";


    /** _more_ */
    public static final String FILTER_IMAGE = "image";

    /** _more_ */
    public static final String FILTER_FILE = "file";

    /** _more_ */
    public static final String FILTER_GEO = "geo";

    /** _more_ */
    public static final String FILTER_FOLDER = "folder";

    /** _more_ */
    public static final String FILTER_TYPE = "type:";

    /** _more_ */
    public static final String FILTER_SUFFIX = "suffix:";




    /**
     * Create an attribute with the name and value
     *
     * @param name  attribute name
     * @param value  value
     *
     * @return  the attribute string
     */
    private static String attr(String name, String value) {
        return " " + name + "=" + value + " ";
    }

    /**
     * Generate a list of attributes
     *
     * @param attrs  set of attrs
     *
     * @return  the string version
     */
    private static String attrs(String... attrs) {
        StringBuffer sb = new StringBuffer();
        String       qt = "&quote;";

        for (int i = 0; i < attrs.length; i += 2) {
            sb.append(attr(attrs[i], qt + attrs[i + 1] + qt));
        }

        return sb.toString();
    }

    /**
     * Create a property string
     *
     * @param prop  the property
     * @param args  the property arguments
     *
     * @return  the property string
     */
    private static String prop(String prop, String args) {
        return prop + PROP_DELIM + args;
    }



    /** property delimiter */
    public static final String PROP_DELIM = ":";

    /** layout attributes */
    public static final String ATTRS_LAYOUT = attrs(ATTR_TEXTPOSITION,
                                                  POS_LEFT);

    /** list of import items for the text editor menu */
    //j-
    public static final String[] WIKIPROPS = {
        WIKI_PROP_GROUP + "Information",
        prop(WIKI_PROP_INFORMATION, attrs(ATTR_DETAILS, "false")),
        WIKI_PROP_NAME, WIKI_PROP_DESCRIPTION, WIKI_PROP_RESOURCE,
        WIKI_PROP_DATE_FROM, WIKI_PROP_DATE_TO, WIKI_PROP_LINK,
        WIKI_PROP_HTML,
        prop(WIKI_PROP_SIMPLE, attrs(ATTR_TEXTPOSITION, POS_LEFT)),
        WIKI_PROP_IMPORT, 
        prop(WIKI_PROP_FIELD,attrs("name","")),
        WIKI_PROP_GROUP + "Layout",
        prop(WIKI_PROP_LINKS,
             attrs(ATTR_LINKRESOURCE, "true", ATTR_SEPARATOR, " | ",
                   ATTR_TAGOPEN, "", ATTR_TAGCLOSE, "")),
        WIKI_PROP_LIST,
        prop(WIKI_PROP_TABS,
             attrs(ATTR_TAG, WIKI_PROP_HTML, ATTR_SHOWLINK, "true",
                   ATTR_INCLUDEICON, "false") + ATTRS_LAYOUT),
        prop(WIKI_PROP_TREE, attrs(ATTR_DETAILS, "true")), WIKI_PROP_TREEVIEW,
        prop(WIKI_PROP_ACCORDIAN,
             attrs(ATTR_TAG, WIKI_PROP_HTML, ATTR_SHOWLINK, "true",
                   ATTR_INCLUDEICON, "false") + ATTRS_LAYOUT),
        WIKI_PROP_GRID, WIKI_PROP_TABLE,
        prop(WIKI_PROP_RECENT, attrs(ATTR_DAYS, "3")),
        prop(WIKI_PROP_APPLY,
             attrs(APPLY_PREFIX + "tag", WIKI_PROP_HTML,
                   APPLY_PREFIX + "layout", "table",
                   APPLY_PREFIX + "columns", "1", APPLY_PREFIX + "header",
                   "", APPLY_PREFIX + "footer", "", APPLY_PREFIX + "border",
                   "0", APPLY_PREFIX + "bordercolor", "#000")),
        WIKI_PROP_GROUP + "Earth",
        prop(WIKI_PROP_MAP,
             attrs(ATTR_WIDTH, "400", ATTR_HEIGHT, "400", ATTR_LISTENTRIES,
                   "false", ATTR_DETAILS, "false", ATTR_ICON,
                   "#/icons/dots/green.png", ARG_MAP_ICONSONLY, "false")),
        prop(WIKI_PROP_MAPENTRY,
             attrs(ATTR_WIDTH, "400", ATTR_HEIGHT, "400", ATTR_DETAILS,
                   "false")),
        prop(WIKI_PROP_EARTH,
             attrs(ATTR_WIDTH, "400", ATTR_HEIGHT, "400", ATTR_LISTENTRIES,
                   "false")),
        WIKI_PROP_GROUP + "Images",
        prop(WIKI_PROP_IMAGE,
             attrs(ATTR_SRC, "", ATTR_ALIGN, "left|center|right")),
        prop(WIKI_PROP_GALLERY,
             attrs(ATTR_WIDTH, "200", ATTR_COLUMNS, "3", ATTR_POPUP, "true",
                   ATTR_THUMBNAIL, "true", ATTR_CAPTION,
                   "Figure ${count}: ${name}", ATTR_POPUPCAPTION, "over")),
        prop(WIKI_PROP_SLIDESHOW,
             attrs(ATTR_TAG, WIKI_PROP_SIMPLE, ATTR_SHOWLINK, "true")
             + ATTRS_LAYOUT + attrs(ATTR_WIDTH, "400", ATTR_HEIGHT, "270")),
        WIKI_PROP_PLAYER, WIKI_PROP_GROUP + "Misc",
        prop(WIKI_PROP_CALENDAR, attrs(ATTR_DAY, "false")),
        prop(WIKI_PROP_TIMELINE, attrs(ATTR_HEIGHT, "150")),
        prop(WIKI_PROP_GRAPH, attrs(ATTR_WIDTH, "400", ATTR_HEIGHT, "400")),
        WIKI_PROP_COMMENTS,
        prop(WIKI_PROP_TAGCLOUD, attrs("type", "", "threshold", "0")),
        WIKI_PROP_PROPERTIES, WIKI_PROP_BREADCRUMBS, 
        WIKI_PROP_TOOLS, WIKI_PROP_TOOLBAR, WIKI_PROP_LAYOUT, WIKI_PROP_MENU,
        WIKI_PROP_ENTRYID,
        prop(WIKI_PROP_SEARCH,
             attrs(ATTR_TYPE, "", ARG_MAX, "10", ARG_SEARCH_SHOWFORM,
                   "false", SpecialSearch.ATTR_TABS, SpecialSearch.TAB_LIST)),
        prop(WIKI_PROP_UPLOAD,
             attrs(ATTR_TITLE, "Upload file", ATTR_INCLUDEICON, "false")),
        WIKI_PROP_ROOT,
        prop(WIKI_PROP_DISPLAYGROUP,
             attrs(ATTR_SHOWTITLE, "true", ATTR_SHOWMENU, "false",
                   "layoutType", "table", "layoutColumns", "1")),
        prop(WIKI_PROP_DISPLAY,
             attrs(ATTR_WIDTH, "800", ATTR_HEIGHT, "400", "fields", "",
                   "type", "linechart", "name", "", "eventSource", "",
                   "layoutFixed", "true", ATTR_SHOWMENU, "true",
                   ATTR_SHOWTITLE, "true", "row", "0", "column", "0",
                   ARG_FROMDATE, "", ARG_TODATE, "")),
    };
    //j+



    //j++

    /** the id for this */
    public static final String ID_THIS = "this";

    /** _more_ */
    public static final String ID_REMOTE = "remote:";

    /** _more_ */
    public static final String ID_ROOT = "root";

    /** _more_ */
    public static final String ID_CHILDREN = "children";

    /** _more_ */
    public static final String PREFIX_SEARCH = "search.";

    /** _more_ */
    public static final String ATTR_SEARCH_TYPE = PREFIX_SEARCH + "type";

    /** _more_ */
    public static final String ATTR_SEARCH_TEXT = PREFIX_SEARCH + "text";

    /** _more_ */
    public static final String ATTR_SEARCH_PARENT = PREFIX_SEARCH + "parent";

    /** _more_ */
    public static final String ATTR_SEARCH_NORTH = PREFIX_SEARCH + "north";

    /** _more_ */
    public static final String ATTR_SEARCH_URL = PREFIX_SEARCH + "url";

    //    public static final String ATTR_SEARCH_PARENT = PREFIX_SEARCH +"parent";

    /** _more_ */
    public static final String ID_SEARCH = "search";

    /** _more_ */
    public static final String ID_SIBLINGS = "siblings";

    /** the id for my parent */
    public static final String ID_PARENT = "parent";

    /** _more_ */
    public static final String ID_ANCESTORS = "ancestors";

    /** the id for my grandparent */
    public static final String ID_GRANDPARENT = "grandparent";

    /** _more_ */
    public static final String ID_GRANDCHILDREN = "grandchildren";

    /** _more_ */
    public static final String ID_GREATGRANDCHILDREN = "greatgrandchildren";

    /** _more_ */
    public static final String ID_LINKS = "links";

    /** default label */
    public static final String LABEL_LINKS = "Actions";

    /** output type */
    public static final OutputType OUTPUT_WIKI = new OutputType("Wiki",
                                                     "wiki.view",
                                                     OutputType.TYPE_VIEW,
                                                     "", ICON_WIKI);



    /**
     * ctor
     *
     * @param repository the repository
     */
    public WikiManager(Repository repository) {
        super(repository);
    }



    /**
     * Get a wiki property value
     *
     * @param wikiUtil The wiki util
     * @param property the property
     *
     * @return the value
     */
    public String getWikiPropertyValue(WikiUtil wikiUtil, String property) {

        try {
            Entry   entry   = (Entry) wikiUtil.getProperty(ATTR_ENTRY);
            Request request = (Request) wikiUtil.getProperty(ATTR_REQUEST);
            //Check for infinite loop
            property = property.trim();
            if (property.length() == 0) {
                return "";
            }


            property = property.replaceAll("\\n", " ");
            property = property.replaceAll("\r", "");

            List<String> toks  = StringUtil.splitUpTo(property, " ", 2);
            String       stoks = toks.toString();
            if (toks.size() == 0) {
                return "<b>Incorrect import specification:" + property
                       + "</b>";
            }
            String tag       = toks.get(0);
            String remainder = "";
            if (toks.size() > 1) {
                remainder = toks.get(1);
            }
            Entry theEntry = entry;
            if (tag.equals(WIKI_PROP_IMPORT)) {
                //Old style
                if (remainder.indexOf("=") < 0) {
                    toks = StringUtil.splitUpTo(remainder, " ", 3);
                    if (toks.size() < 2) {
                        return "<b>Incorrect import specification:"
                               + property + "</b>";
                    }
                    String id = toks.get(0).trim();
                    tag = toks.get(1).trim();
                    if (toks.size() == 3) {
                        remainder = toks.get(2);
                    } else {
                        remainder = "";
                    }
                    theEntry = findWikiEntry(request, wikiUtil, id, entry);
                    if (theEntry == null) {
                        return "<b>Could not find entry&lt;" + id
                               + "&gt;</b>";
                    }
                }
            }


            Hashtable props   = StringUtil.parseHtmlProperties(remainder);
            String    entryId = (String) props.get(ATTR_ENTRY);

            if (entryId != null) {
                theEntry = null;
                int barIndex = entryId.indexOf("|");

                if (barIndex >= 0) {
                    entryId = entryId.substring(0, barIndex);
                }
                if (entryId.equals(ID_THIS)) {
                    theEntry = entry;
                }
                if (entryId.equals(ID_ROOT)) {
                    theEntry = getEntryManager().getTopGroup();
                }
                if (theEntry == null) {
                    if (entryId.equals(ID_PARENT)) {
                        theEntry = getEntryManager().getEntry(request,
                                entry.getParentEntryId());
                    }
                }

                if (theEntry == null) {
                    if (entryId.equals(ID_GRANDPARENT)) {
                        theEntry = getEntryManager().getEntry(request,
                                entry.getParentEntryId());
                        if (theEntry != null) {
                            theEntry = getEntryManager().getEntry(request,
                                    theEntry.getParentEntryId());
                        }
                    }
                }
                if (theEntry == null) {
                    theEntry = getEntryManager().getEntry(request, entryId);
                }
                if (theEntry == null) {
                    theEntry = findWikiEntry(request, wikiUtil, entryId,
                                             entry);
                }

                //Ugghh - I really have to unify the EntryManager find entry methods
                //Look for file path based entry id
                if ((theEntry == null) && entryId.startsWith("/")) {
                    theEntry = getEntryManager().findEntryFromName(request,
                            entryId, request.getUser(), false);
                }

                //Look for relative to the current entry
                if (theEntry == null) {
                    theEntry = getEntryManager().findEntryFromPath(request,
                            entry, entryId);
                }

                if (theEntry == null) {
                    return getMessage(props, "Unknown entry:" + entryId);
                }
            }


            //TODO: figure out a way to look for infinte loops
            /*
            String propertyKey = theEntry.getId() + "_" + property;
            if (request.getExtraProperty(propertyKey) != null) {
                return "<b>Detected circular wiki import:" + property +
                    "<br>For entry:" +  theEntry.getId()
                    + "</b>";
            }
            request.putExtraProperty(propertyKey, property);
            */


            addWikiLink(wikiUtil, theEntry);
            String include = handleWikiImport(wikiUtil, request, entry,
                                 theEntry, tag, props);
            if (include != null) {
                return include;
            }

            return wikiUtil.getPropertyValue(property);
        } catch (Exception exc) {
            throw new RuntimeException(exc);
        }

    }



    /**
     * Get a wiki image link
     *
     *
     * @param wikiUtil The wiki util
     * @param request The request
     * @param url  the image url
     * @param entry  the entry
     * @param props  the properties
     *
     * @return the link
     *
     * @throws Exception problems
     */
    public String getWikiImage(WikiUtil wikiUtil, Request request,
                               String url, Entry entry, Hashtable props)
            throws Exception {
        String align = (String) props.get(ATTR_ALIGN);
        String width = (String) props.get(ATTR_WIDTH);
        String alt   = (String) props.get(HtmlUtils.ATTR_ALT);
        String extra = "";

        //imagewidth says to resize and cache the image on the server
        //If its defined then add it to the URL
        int imageWidth = Misc.getProperty(props, ATTR_IMAGEWIDTH, -1);
        if (imageWidth > 0) {
            url = url + "&" + ARG_IMAGEWIDTH + "=" + imageWidth;
        }
        if (width != null) {
            extra = extra + HtmlUtils.attr(HtmlUtils.ATTR_WIDTH, width);
        }

        if (alt == null) {
            String name = getEntryDisplayName(entry);
            if ((name != null) && !name.isEmpty()) {
                alt = name;
            }
        }

        if (alt != null) {
            extra = extra + HtmlUtils.attr(HtmlUtils.ATTR_ALT, alt);
        }

        if (wikiUtil != null) {
            String imageClass = (String) wikiUtil.getProperty("image.class");
            if (imageClass != null) {
                extra = extra + HtmlUtils.cssClass(imageClass);
            }
        }


        String style       = Misc.getProperty(props, ATTR_STYLE, "");
        int    border      = Misc.getProperty(props, ATTR_BORDER, -1);
        String bordercolor = Misc.getProperty(props, ATTR_BORDERCOLOR,
                                 "#000");

        if (border > 0) {
            style += " border: " + border + "px solid " + bordercolor + ";";
        }
        String left = (String) props.get("left");
        if (left != null) {
            style += " position:absolute; left: " + left + ";";
        }

        String top = (String) props.get("top");
        if (top != null) {
            style += " position:absolute;  top: " + top + ";";
        }

        if (style.length() > 0) {
            extra = extra + " style=\" " + style + "\" ";
        }


        String  img = HtmlUtils.img(url, getEntryDisplayName(entry), extra);
        boolean link = Misc.equals("true", props.get(ATTR_LINK));
        boolean linkResource = Misc.getProperty(props, ATTR_LINKRESOURCE,
                                   false);

        boolean popup = Misc.getProperty(props, ATTR_POPUP, false);
        if (link) {
            img = HtmlUtils.href(
                request.entryUrl(getRepository().URL_ENTRY_SHOW, entry), img);
        } else if (linkResource) {
            img = HtmlUtils.href(
                entry.getTypeHandler().getEntryResourceUrl(request, entry),
                img);
        } else if (popup) {
            //A hack to see if this image is an attachment (e.g. src="::*")
            String hrefUrl;
            if (url.indexOf("/metadata/view") >= 0) {
                hrefUrl = url;
            } else {
                hrefUrl = entry.getTypeHandler().getEntryResourceUrl(request,
                        entry);
            }
            StringBuffer buf = new StringBuffer();
            addImagePopupJS(request, buf, props);
            buf.append(HtmlUtils.href(hrefUrl, img,
                                      HtmlUtils.cssClass("popup_image")));

            img = buf.toString();
        }

        if (align != null) {
            img = HtmlUtils.div(img,
                                HtmlUtils.style("text-align:" + align + ";"));
        }

        return img;
    }


    /**
     * Get the wiki url
     *
     * @param wikiUtil The wiki util
     * @param request The request
     * @param entry  the entry
     * @param props  the properties
     *
     * @return  the url
     *
     * @throws Exception problems
     */
    public String getWikiUrl(WikiUtil wikiUtil, Request request, Entry entry,
                             Hashtable props)
            throws Exception {

        String src      = (String) props.get("src");
        Entry  srcEntry = null;
        if (src == null) {
            srcEntry = entry;
        } else {
            src = src.trim();
            if ((src.length() == 0) || entry.getName().equals(src)) {
                srcEntry = entry;
            } else if (entry instanceof Entry) {
                srcEntry = getEntryManager().findEntryWithName(request,
                        (Entry) entry, src);
            }
        }
        if (srcEntry == null) {
            srcEntry = getEntryManager().getEntry(request, src);
        }

        if (srcEntry == null) {
            return getMessage(props, msgLabel("Could not find src") + src);
        }

        return request.entryUrl(getRepository().URL_ENTRY_SHOW, srcEntry);

    }


    /**
     * Get the wiki image link
     *
     * @param wikiUtil The wiki util
     * @param request The request
     * @param entry  the entry
     * @param props  the properties
     *
     * @return  the link
     *
     * @throws Exception problems
     */
    public String getWikiImage(WikiUtil wikiUtil, Request request,
                               Entry entry, Hashtable props)
            throws Exception {

        String src = (String) props.get(ATTR_SRC);
        if ((src == null) || (src.length() == 0)) {
            if ( !entry.getResource().isImage()) {
                return getMessage(props, msg("Not an image"));
            }

            return getWikiImage(wikiUtil, request,
                                getHtmlOutputHandler().getImageUrl(request,
                                    entry), entry, props);
        }

        String attachment = null;
        int    idx        = src.indexOf("::");
        if (idx >= 0) {
            List<String> toks = StringUtil.splitUpTo(src, "::", 2);
            if (toks.size() == 2) {
                src        = toks.get(0);
                attachment = toks.get(1).substring(1);
            }
        }
        src = src.trim();
        Entry srcEntry = null;

        if ((src.length() == 0) || entry.getName().equals(src)) {
            srcEntry = entry;
        } else if (entry instanceof Entry) {
            srcEntry = getEntryManager().findEntryWithName(request,
                    (Entry) entry, src);
        }
        if (srcEntry == null) {
            return getMessage(props, msgLabel("Could not find src") + src);
        }
        if (attachment == null) {
            if ( !srcEntry.getResource().isImage()) {
                return getMessage(props, msg("Not an image"));
            }

            return getWikiImage(wikiUtil, request,
                                getHtmlOutputHandler().getImageUrl(request,
                                    srcEntry), srcEntry, props);
        }


        if ((attachment != null) && attachment.equals("*")) {
            attachment = null;
        }
        for (Metadata metadata : getMetadataManager().getMetadata(srcEntry)) {
            MetadataType metadataType =
                getMetadataManager().findType(metadata.getType());
            if (metadataType == null) {
                continue;
            }
            String url = metadataType.getDisplayImageUrl(request, srcEntry,
                             metadata, attachment);
            if (url != null) {
                return getWikiImage(wikiUtil, request, url, srcEntry, props);
            }
        }

        return getMessage(props,
                          msgLabel("Could not find image attachment")
                          + attachment);
    }



    /**
     * Get the message
     *
     * @param props the properties
     * @param message the default
     *
     * @return  the message or the default
     */
    public String getMessage(Hashtable props, String message) {
        return Misc.getProperty(props, ATTR_MESSAGE, message);
    }



    /**
     * Get the text for the include
     *
     * @param wikiUtil The wiki util
     * @param request The request
     * @param originalEntry _more_
     * @param entry the entry
     * @param tag  the tag
     * @param props    the properties
     *
     * @return  the include text
     *
     * @throws Exception  problems
     */
    private String getWikiInclude(WikiUtil wikiUtil, Request request,
                                  Entry originalEntry, Entry entry,
                                  String tag, Hashtable props)
            throws Exception {
        boolean doingApply = tag.equals(WIKI_PROP_APPLY);
        String  attrPrefix = "";
        if (doingApply) {
            attrPrefix = APPLY_PREFIX;
        }

        boolean blockPopup = Misc.getProperty(props,
                                 attrPrefix + ATTR_BLOCK_POPUP, false);
        boolean blockShow = Misc.getProperty(props,
                                             attrPrefix + ATTR_BLOCK_SHOW,
                                             false);
        String prefix = Misc.getProperty(props, attrPrefix + ATTR_PREFIX,
                                         (String) null);
        String suffix = Misc.getProperty(props, attrPrefix + ATTR_SUFFIX,
                                         (String) null);

        String result = getWikiIncludeInner(wikiUtil, request, originalEntry,
                                            entry, tag, props);
        if (result == null) {
            result = getMessage(props, "Could not find entry ");
        }
        if (result.trim().length() == 0) {
            return "";
        }

        StringBuffer sb = new StringBuffer();

        String rowLabel = Misc.getProperty(props,
                                           attrPrefix + ATTR_ROW_LABEL,
                                           (String) null);
        if (rowLabel != null) {
            return HtmlUtils.formEntry(rowLabel, result);
        }

        boolean      wrapInADiv = false;
        StringBuffer style      = new StringBuffer();
        int maxHeight = Misc.getProperty(props, "box." + ATTR_MAXHEIGHT, -1);
        style.append(Misc.getProperty(props, "box." + ATTR_STYLE, ""));
        String cssClass = Misc.getProperty(props, "box." + ATTR_CLASS,
                                           (String) null);
        if (cssClass != null) {
            wrapInADiv = true;
        }
        if (maxHeight > 0) {
            wrapInADiv = true;
            style.append(" max-height: " + maxHeight
                         + "px;  overflow-y: auto; ");
        }

        if (prefix != null) {
            sb.append(makeWikiUtil(request, false).wikify(prefix, null));
        }

        if (wrapInADiv) {
            sb.append(HtmlUtils.open("div", ((cssClass != null)
                                             ? HtmlUtils.cssClass(cssClass)
                                             : "") + HtmlUtils.style(
                                             style.toString())));
        }
        sb.append(result);
        if (wrapInADiv) {
            sb.append(HtmlUtils.close("div"));
        }

        if (suffix != null) {
            sb.append(makeWikiUtil(request, false).wikify(suffix, null));
        }




        String blockTitle = Misc.getProperty(props,
                                             attrPrefix + ATTR_BLOCK_TITLE,
                                             "");
        if (blockPopup) {
            return getPageHandler().makePopupLink(blockTitle, sb.toString());
        }

        if (blockShow) {
            boolean blockOpen = Misc.getProperty(props,
                                    attrPrefix + ATTR_BLOCK_OPEN, true);

            return HtmlUtils.makeShowHideBlock(blockTitle, sb.toString(),
                    blockOpen, HtmlUtils.cssClass("entry-toggleblock-label"),
                    "", iconUrl("ramadda.icon.togglearrowdown"),
                    iconUrl("ramadda.icon.togglearrowright"));

        }

        return sb.toString();

    }


    /**
     * _more_
     *
     * @param wikiUtil _more_
     * @param request _more_
     * @param originalEntry _more_
     * @param entry _more_
     * @param theTag _more_
     * @param props _more_
     *
     * @return _more_
     *
     * @throws Exception _more_
     */
    private String getWikiIncludeInner(WikiUtil wikiUtil, Request request,
                                       Entry originalEntry, Entry entry,
                                       String theTag, Hashtable props)
            throws Exception {

        boolean wikify   = Misc.getProperty(props, ATTR_WIKIFY, true);

        String  criteria = Misc.getProperty(props, ATTR_IF, (String) null);
        if (criteria != null) {}

        StringBuffer sb = new StringBuffer();
        if (theTag.equals(WIKI_PROP_INFORMATION)) {
            boolean details = Misc.getProperty(props, ATTR_DETAILS, false);
            if ( !details) {
                return entry.getTypeHandler().getEntryContent(request, entry,
                        false, true).toString();
            }

            return getRepository().getHtmlOutputHandler().getInformationTabs(
                request, entry, false);
        } else if (theTag.equals(WIKI_PROP_TAGCLOUD)) {
            StringBuffer tagCloud  = new StringBuffer();
            int          threshold = Misc.getProperty(props, "threshold", 0);
            getMetadataManager().doMakeTagCloudOrList(request,
                    Misc.getProperty(props, "type", ""), tagCloud, true,
                    threshold);

            return tagCloud.toString();
        } else if (theTag.equals(WIKI_PROP_COMMENTS)) {
            return getHtmlOutputHandler().getCommentBlock(request, entry,
                    false).toString();
        } else if (theTag.equals(WIKI_PROP_TOOLBAR)) {
            return getPageHandler().getEntryToolbar(request, entry);
        } else if (theTag.equals(WIKI_PROP_BREADCRUMBS)) {
            List<Entry> children = getEntries(request, originalEntry, entry,
                                       props);
            List<String> breadcrumbs =
                getPageHandler().makeBreadcrumbList(request, children, null);

            return getPageHandler().makeBreadcrumbs(request, breadcrumbs);
        } else if (theTag.equals(WIKI_PROP_LINK)) {
            boolean linkResource = Misc.getProperty(props, ATTR_LINKRESOURCE,
                                       false);
            String title = Misc.getProperty(props, ATTR_TITLE,
                                            getEntryDisplayName(entry));
            String url;
            if (linkResource
                    && (entry.getTypeHandler().isType("link")
                        || entry.isFile() || entry.getResource().isUrl())) {
                url = entry.getTypeHandler().getEntryResourceUrl(request,
                        entry);
            } else {
                url = request.entryUrl(getRepository().URL_ENTRY_SHOW, entry);
            }

            return HtmlUtils.href(url, title);

        } else if (theTag.equals(WIKI_PROP_RESOURCE)) {
            if ( !entry.getResource().isDefined()) {
                String message = Misc.getProperty(props, ATTR_MESSAGE,
                                     (String) null);
                if (message != null) {
                    return message;
                }

                return "";
            }

            String url;
            String label;

            if (entry.getResource().isFile()) {
                url = entry.getTypeHandler().getEntryResourceUrl(request,
                        entry);
                label = Misc.getProperty(props, ATTR_TITLE, "Download");
            } else {
                url   = entry.getResource().getPath();
                label = url;
            }
            boolean includeIcon = Misc.getProperty(props, ATTR_INCLUDEICON,
                                      false);
            if (includeIcon) {
                label = HtmlUtils.img(iconUrl("/icons/download.png"))
                        + HtmlUtils.space(2) + label;

                return HtmlUtils.div(
                    HtmlUtils.href(url, label),
                    HtmlUtils.cssClass("entry-download-box"));
            }

            return HtmlUtils.href(url, label);

        } else if (theTag.equals(WIKI_PROP_UPLOAD)) {
            Entry group = getEntryManager().findGroup(request);
            if ( !getEntryManager().canAddTo(request, group)) {
                return "";
            }
            // can't add to local file view
            if (group.getIsLocalFile()
                    || (group.getTypeHandler()
                        instanceof LocalFileTypeHandler)) {
                return "";
            }
            TypeHandler typeHandler =
                getRepository().getTypeHandler(Misc.getProperty(props,
                    ATTR_TYPE, TypeHandler.TYPE_FILE));
            if (typeHandler == null) {
                return "ERROR: unknown type";
            }
            if ( !typeHandler.getForUser()) {
                return "";
            }
            if (typeHandler.isAnyHandler()) {
                return "";
            }
            if ( !typeHandler.canBeCreatedBy(request)) {
                return "";
            }
            String img = "";
            if (Misc.getProperty(props, ATTR_INCLUDEICON, false)) {
                String icon = typeHandler.getProperty("icon", (String) null);
                if (icon == null) {
                    icon = ICON_BLANK;
                    img = HtmlUtils.img(typeHandler.iconUrl(icon), "",
                                        HtmlUtils.attr(HtmlUtils.ATTR_WIDTH,
                                            "16"));
                } else {
                    img = HtmlUtils.img(typeHandler.iconUrl(icon));
                }
            }

            String label = Misc.getProperty(props, ATTR_TITLE,
                                            typeHandler.getLabel());

            return HtmlUtils.href(request.url(getRepository().URL_ENTRY_FORM,
                    ARG_GROUP, group.getId(), EntryManager.ARG_TYPE,
                    typeHandler.getType()), img + " " + msg(label));

        } else if (theTag.equals(WIKI_PROP_DESCRIPTION)) {
            String desc = entry.getDescription();
            desc = desc.replaceAll("\r\n\r\n", "\n<p>\n");
            if (wikify) {
                desc = makeWikiUtil(request, false).wikify(desc, null);
            }

            return desc;
        } else if (theTag.equals(WIKI_PROP_LAYOUT)) {
            return getHtmlOutputHandler().makeHtmlHeader(request, entry,
                    Misc.getProperty(props, ATTR_TITLE, "Layout"));
        } else if (theTag.equals(WIKI_PROP_NAME)) {
            return getEntryDisplayName(entry);
        } else if (theTag.equals(WIKI_PROP_FIELD)) {
            String name = Misc.getProperty(props, ATTR_FIELDNAME,
                                           (String) null);
            if (name != null) {
                String fieldValue =
                    entry.getTypeHandler().getFieldHtml(request, entry, name);
                if (fieldValue != null) {
                    return fieldValue;
                }

                return "Could not find field: " + name;
            } else {
                return "No name=... specified in wiki tag";
            }
        } else if (theTag.equals(WIKI_PROP_DATE_FROM)
                   || theTag.equals(WIKI_PROP_DATE_TO)) {
            String format =
                Misc.getProperty(props, ATTR_FORMAT,
                                 RepositoryBase.DEFAULT_TIME_FORMAT);
            Date date = new Date(theTag.equals(WIKI_PROP_DATE_FROM)
                                 ? entry.getStartDate()
                                 : entry.getEndDate());
            SimpleDateFormat dateFormat = new SimpleDateFormat(format);
            dateFormat.setTimeZone(RepositoryUtil.TIMEZONE_DEFAULT);

            return dateFormat.format(date);
        } else if (theTag.equals(WIKI_PROP_ENTRYID)) {
            return entry.getId();
        } else if (theTag.equals(WIKI_PROP_PROPERTIES)) {
            return makeEntryTabs(request, entry, props);
        } else if (theTag.equals(WIKI_PROP_IMAGE)) {
            return getWikiImage(wikiUtil, request, entry, props);
        } else if (theTag.equals(WIKI_PROP_URL)) {
            return getWikiUrl(wikiUtil, request, entry, props);
        } else if (theTag.equals(WIKI_PROP_HTML)) {
            Request newRequest = makeRequest(request, props);
            if (Misc.getProperty(props, ATTR_CHILDREN, false)) {
                List<Entry> children = getEntries(request, originalEntry,
                                           entry, props);
                for (Entry child : children) {
                    Result result =
                        getHtmlOutputHandler().getHtmlResult(request,
                            OutputHandler.OUTPUT_HTML, child);
                    sb.append(getEntryManager().getEntryLink(request, child));
                    sb.append(HtmlUtils.br());
                    sb.append(new String(result.getContent()));
                    sb.append(HtmlUtils.p());
                }

                return sb.toString();
            }


            Request myRequest = request.cloneMe();
            myRequest.put(ARG_ENTRYID, entry.getId());
            myRequest.put(ARG_OUTPUT, OutputHandler.OUTPUT_HTML.getId());
            myRequest.put(ARG_EMBEDDED, "true");


            Result result = getEntryManager().processEntryShow(myRequest,
                                entry);
            //            Result result = getHtmlOutputHandler().getHtmlResult(newRequest,
            //                                OutputHandler.OUTPUT_HTML, entry);

            return new String(result.getContent());
        } else if (theTag.equals(WIKI_PROP_CALENDAR)) {
            List<Entry> children = getEntries(request, originalEntry, entry,
                                       props);
            boolean doDay = Misc.getProperty(props, ATTR_DAY, false);
            getCalendarOutputHandler().outputCalendar(request,
                    getCalendarOutputHandler().makeCalendarEntries(request,
                        children), sb, doDay);

            return sb.toString();
        } else if (theTag.equals(WIKI_PROP_DISPLAY)
                   || theTag.equals("chart")) {
            //TODO: don't hard code the class
            //TODO: handle grids
            PointTypeHandler pth =
                (PointTypeHandler) getRepository().getTypeHandler(
                    "type_point");
            PointOutputHandler poh =
                (PointOutputHandler) pth.getRecordOutputHandler();

            String jsonUrl = poh.getJsonUrl(request, entry);
            getEntryChart(request, entry.getName(), jsonUrl, sb, props);

            return sb.toString();
        } else if (theTag.equals(WIKI_PROP_DISPLAYGROUP)) {
            getEntryChart(request, entry.getName(), null, sb, props);

            return sb.toString();
        } else if (theTag.equals(WIKI_PROP_GRAPH)) {
            int width  = Misc.getProperty(props, ATTR_WIDTH, 400);
            int height = Misc.getProperty(props, ATTR_HEIGHT, 300);
            List<Entry> children = getEntries(request, originalEntry, entry,
                                       props);
            getGraphOutputHandler().getGraph(request, entry, children, sb,
                                             width, height);

            return sb.toString();
        } else if (theTag.equals(WIKI_PROP_TIMELINE)) {
            Entry mainEntry = entry;
            List<Entry> children = getEntries(request, originalEntry,
                                       mainEntry, props);
            int    height = Misc.getProperty(props, ATTR_HEIGHT, 150);
            String style  = "height: " + height + "px;";
            getCalendarOutputHandler().makeTimeline(request, mainEntry,
                    children, sb, style);

            return sb.toString();
        } else if (theTag.equals(WIKI_PROP_MAP)
                   || theTag.equals(WIKI_PROP_EARTH)
                   || theTag.equals(WIKI_PROP_MAPENTRY)) {
            int     width      = Misc.getProperty(props, ATTR_WIDTH, 400);
            int     height     = Misc.getProperty(props, ATTR_HEIGHT, 300);
            boolean justPoints = Misc.getProperty(props, "justpoints", false);
            boolean listEntries = Misc.getProperty(props, ATTR_LISTENTRIES,
                                      false);
            boolean googleEarth =
                theTag.equals(WIKI_PROP_EARTH)
                && getMapManager().isGoogleEarthEnabled(request);

            List<Entry> children;
            if (theTag.equals(WIKI_PROP_MAPENTRY)) {
                children = new ArrayList<Entry>();
                children.add(entry);
            } else {
                children = getEntries(request, originalEntry, entry, props,
                                      false, "");
                if (children.isEmpty()) {
                    children.add(entry);
                }
            }


            if (children.size() == 0) {
                String message = Misc.getProperty(props, ATTR_MESSAGE,
                                     (String) null);
                if (message != null) {
                    return message;
                }
            } else {
                boolean anyHaveLatLon = false;
                for (Entry child : children) {
                    if (child.hasLocationDefined()
                            || child.hasAreaDefined()) {
                        anyHaveLatLon = true;

                        break;
                    }
                }
                if ( !anyHaveLatLon) {
                    String message = Misc.getProperty(props, ATTR_MESSAGE,
                                         (String) null);
                    if (message != null) {
                        return message;
                    }
                }
            }

            Request newRequest = makeRequest(request, props);


            if (googleEarth) {
                getMapManager().getGoogleEarth(newRequest, children, sb,
                        Misc.getProperty(props, ATTR_WIDTH, -1),
                        Misc.getProperty(props, ATTR_HEIGHT, -1),
                        listEntries, justPoints);
            } else {
                MapOutputHandler mapOutputHandler =
                    (MapOutputHandler) getRepository().getOutputHandler(
                        MapOutputHandler.OUTPUT_MAP);
                if (mapOutputHandler == null) {
                    return "No maps";
                }
                boolean[] haveBearingLines = { false };
                //Request   newRequest       = request.cloneMe();
                //newRequest.putAll(props);
                boolean details = Misc.getProperty(props, ATTR_DETAILS,
                                      false);
                String icon = Misc.getProperty(props, ATTR_ICON,
                                  (String) null);

                String layer = Misc.getProperty(props, ATTR_LAYER,
                                   (String) null);

                if ((icon != null) && icon.startsWith("#")) {
                    icon = null;
                }
                if (icon != null) {
                    newRequest.put(ARG_ICON, icon);
                }
                if (Misc.equals("true",
                                Misc.getProperty(props, ARG_MAP_ICONSONLY,
                                    (String) null))) {
                    newRequest.put(ARG_MAP_ICONSONLY, "true");
                }
                MapInfo map = getMapManager().getMap(newRequest, children,
                                  sb, width, height, details,
                                  haveBearingLines, listEntries);
                if (icon != null) {
                    newRequest.remove(ARG_ICON);
                }
                newRequest.remove(ARG_MAP_ICONSONLY);
            }

            return sb.toString();
        } else if (theTag.equals(WIKI_PROP_TOOLS)) {
            StringBuffer links = new StringBuffer();
            int          cnt   = 0;
            for (Link link :
                    getEntryManager().getEntryLinks(request, entry)) {
                if ( !link.isType(OutputType.TYPE_IMPORTANT)) {
                    continue;
                }
                String label = HtmlUtils.img(link.getIcon())
                               + HtmlUtils.space(1) + link.getLabel();
                links.append(HtmlUtils.href(link.getUrl(), label));
                links.append(HtmlUtils.br());
                cnt++;
            }
            if (cnt == 0) {
                return "";
            }
            String title = Misc.getProperty(props, ATTR_TITLE, "Links");
            sb.append(HtmlUtils.div(title, HtmlUtils.cssClass("wiki-h4")));
            sb.append(HtmlUtils.div(links.toString(),
                                    HtmlUtils.cssClass("entry-tools-box")));

            return sb.toString();
        } else if (theTag.equals(WIKI_PROP_MENU)) {
            String menus = Misc.getProperty(props, ATTR_MENUS, "");
            int type = OutputType.getTypeMask(StringUtil.split(menus, ",",
                           true, true));

            return getEntryManager().getEntryActionsTable(request, entry,
                    type);
        } else if (theTag.equals(WIKI_PROP_SEARCH)) {
            String type = Misc.getProperty(props, ATTR_TYPE,
                                           Misc.getProperty(props, ATTR_ID,
                                               TypeHandler.TYPE_ANY));
            TypeHandler typeHandler = getRepository().getTypeHandler(type);

            if (typeHandler == null) {
                return "Could not find search type: " + type;
            }
            String  incomingMax = request.getString(ARG_MAX, (String) null);
            Request myRequest   = copyRequest(request, props);

            if ( !myRequest.defined(ARG_SEARCH_SHOWHEADER)) {
                myRequest.put(ARG_SEARCH_SHOWHEADER, "false");
            }
            if ( !myRequest.defined(ARG_SEARCH_SHOWFORM)) {
                myRequest.put(ARG_SEARCH_SHOWFORM, "false");
            }
            if ( !myRequest.defined(ARG_SHOWCATEGORIES)) {
                myRequest.put(ARG_SHOWCATEGORIES, "false");
            }

            addSearchTerms(myRequest, props, entry);

            if (incomingMax != null) {
                myRequest.put(ARG_MAX, incomingMax);
            } else {
                if ( !myRequest.exists(ARG_MAX)) {
                    myRequest.put(ARG_MAX, "10");
                }
            }
            SpecialSearch ss = typeHandler.getSpecialSearch();
            ss.processSearchRequest(myRequest, sb);

            return sb.toString();
        } else if (theTag.equals(WIKI_PROP_APPLY)) {
            StringBuffer style = new StringBuffer(Misc.getProperty(props,
                                     APPLY_PREFIX + ATTR_STYLE, ""));
            int padding = Misc.getProperty(props,
                                           APPLY_PREFIX + ATTR_PADDING, 5);
            int border = Misc.getProperty(props, APPLY_PREFIX + ATTR_BORDER,
                                          -1);
            String bordercolor = Misc.getProperty(props,
                                     APPLY_PREFIX + ATTR_BORDERCOLOR, "#000");

            if (border > 0) {
                style.append(" border: " + border + "px solid " + bordercolor
                             + "; ");
            }

            if (padding > 0) {
                style.append(" padding: " + padding + "px; ");
            }


            int maxHeight = Misc.getProperty(props,
                                             APPLY_PREFIX + "maxheight", -1);
            if (maxHeight > 0) {
                style.append(" max-height: " + maxHeight
                             + "px;  overflow-y: auto; ");
            }

            int minHeight = Misc.getProperty(props,
                                             APPLY_PREFIX + "minheight", -1);
            if (minHeight > 0) {
                style.append(" min-height: " + minHeight + "px; ");
            }


            Hashtable tmpProps = new Hashtable(props);
            tmpProps.remove(ATTR_ENTRY);
            Request newRequest = makeRequest(request, props);
            //            System.err.println("cloned:" + newRequest);
            //            {{apply tag="tree" apply.layout="grid" apply.columns="2"}}
            String tag = Misc.getProperty(props, ATTR_APPLY_TAG, "html");
            String prefixTemplate = Misc.getProperty(props,
                                        APPLY_PREFIX + "header", "");
            String suffixTemplate = Misc.getProperty(props,
                                        APPLY_PREFIX + "footer", "");

            List<Entry> children = getEntries(request, originalEntry, entry,
                                       props, false, APPLY_PREFIX);
            if (children.size() == 0) {
                return null;
            }
            String layout = Misc.getProperty(props, APPLY_PREFIX + "layout",
                                             "table");
            int columns = Misc.getProperty(props,
                                           APPLY_PREFIX + ATTR_COLUMNS, 1);
            if (columns > children.size()) {
                columns = children.size();
            }
            String colWidth = "";
            if (layout.equals("table")) {
                if (columns > 1) {
                    sb.append(
                        "<table border=0 cellspacing=5 cellpadding=5  width=100%>");
                    sb.append("<tr valign=top>");
                    colWidth = ((int) (100.0 / columns)) + "%";
                }
            }
            List<String> contents = new ArrayList<String>();
            List<String> titles   = new ArrayList<String>();


            boolean includeIcon = Misc.getProperty(props,
                                      APPLY_PREFIX + ATTR_INCLUDEICON, false);


            int colCnt = 0;
            for (Entry child : children) {
                String childsHtml = getWikiInclude(wikiUtil, newRequest,
                                        originalEntry, child, tag, tmpProps);
                childsHtml = HtmlUtils.div(childsHtml,
                                           HtmlUtils.style(style.toString()));
                String prefix = prefixTemplate;
                String suffix = suffixTemplate;
                String childUrl = HtmlUtils.href(
                                      request.entryUrl(
                                          getRepository().URL_ENTRY_SHOW,
                                          child), getEntryDisplayName(child));
                prefix = prefix.replace(
                    "${name}",
                    getEntryDisplayName(child).replace(
                        "${description}", child.getDescription()));
                suffix = suffix.replace(
                    "${name}",
                    getEntryDisplayName(child).replace(
                        "${description}", child.getDescription()));
                prefix = prefix.replace("${url}", childUrl);
                suffix = suffix.replace("${url}", childUrl);
                String icon =
                    HtmlUtils.img(getPageHandler().getIconUrl(request,
                        child));
                prefix = prefix.replace("${icon}", icon);
                suffix = suffix.replace("${icon}", icon);

                StringBuffer content = new StringBuffer();
                content.append(prefix);
                content.append(childsHtml);
                content.append(suffix);

                if (layout.equals("table")) {
                    if (columns > 1) {
                        if (colCnt >= columns) {
                            sb.append("</tr>");
                            sb.append("<tr valign=top>");
                            colCnt = 0;
                        }
                        sb.append("<td width=" + colWidth + ">");
                    }
                    colCnt++;
                    sb.append(content);
                    if (columns > 1) {
                        sb.append("</td>");
                    }
                } else {
                    contents.add(content.toString());
                    String title = getEntryDisplayName(child);
                    if (includeIcon) {
                        title = HtmlUtils.img(
                            getPageHandler().getIconUrl(
                                request, child)) + " " + title;
                    }
                    titles.add(title);
                }
            }

            if (layout.equals("table")) {
                if (columns > 1) {
                    sb.append("</table>");
                }
            } else if (layout.equals("tabs")) {
                sb.append(OutputHandler.makeTabs(titles, contents, true,
                        false));
            } else if (layout.equals("accordian")) {
                HtmlUtils.makeAccordian(sb, titles, contents);
            } else {
                throw new IllegalArgumentException("Unknown layout:"
                        + layout);
            }

            return sb.toString();
        } else if (theTag.equals(WIKI_PROP_SIMPLE)) {
            return makeSimpleDisplay(request, props, originalEntry, entry);
        } else if (theTag.equals(WIKI_PROP_TABS)
                   || theTag.equals(WIKI_PROP_ACCORDIAN)
                   || theTag.equals(WIKI_PROP_SLIDESHOW)) {
            List<Entry> children = getEntries(request, originalEntry, entry,
                                       props);
            boolean      doingSlideshow = theTag.equals(WIKI_PROP_SLIDESHOW);
            List<String> titles         = new ArrayList<String>();
            List<String> contents       = new ArrayList<String>();
            String       dfltTag        = WIKI_PROP_SIMPLE;

            if (props.get(ATTR_USEDESCRIPTION) != null) {
                boolean useDescription = Misc.getProperty(props,
                                             ATTR_USEDESCRIPTION, true);

                if (useDescription) {
                    dfltTag = WIKI_PROP_SIMPLE;
                } else {
                    dfltTag = WIKI_PROP_HTML;
                }
            }

            boolean showLink = Misc.getProperty(props, ATTR_SHOWLINK, true);
            boolean includeIcon = Misc.getProperty(props, ATTR_INCLUDEICON,
                                      false);
            boolean useCookies = Misc.getProperty(props, "cookie", false);
            String  linklabel  = Misc.getProperty(props, ATTR_LINKLABEL, "");

            int     width      = Misc.getProperty(props, ATTR_WIDTH, 400);
            int     height     = Misc.getProperty(props, ATTR_HEIGHT, 270);

            if (doingSlideshow) {
                props.put(ATTR_WIDTH, "" + width);
                props.put(ATTR_HEIGHT, "" + height);
                props.put(ATTR_CONSTRAINSIZE, "true");
            }

            if (theTag.equals(WIKI_PROP_TABS)) {
                dfltTag = WIKI_PROP_HTML;
            }


            boolean linkResource = Misc.getProperty(props, ATTR_LINKRESOURCE,
                                       false);


            String    tag        = Misc.getProperty(props, ATTR_TAG, dfltTag);
            Request   newRequest = makeRequest(request, props);
            Hashtable tmpProps   = new Hashtable(props);
            tmpProps.remove(ATTR_ENTRY);




            for (Entry child : children) {
                String title = getEntryDisplayName(child);
                if (includeIcon) {
                    title =
                        HtmlUtils.img(getPageHandler().getIconUrl(request,
                            child)) + " " + title;
                }
                titles.add(title);
                String content = getWikiInclude(wikiUtil, newRequest,
                                     originalEntry, child, tag, tmpProps);


                /*
                if ( !useDescription) {
                    Result      result      = null;
                    TypeHandler typeHandler = child.getTypeHandler();
                    if (typeHandler.isGroup()) {
                        List<Entry> entries   = new ArrayList<Entry>();
                        List<Entry> subGroups = new ArrayList<Entry>();
                        child.getTypeHandler().getChildrenEntries(request,
                                child, entries, subGroups, null);
                        result = typeHandler.getHtmlDisplay(request, child,
                                subGroups, entries);
                    } else {
                        result = typeHandler.getHtmlDisplay(request, child);
                    }
                    if (result == null) {
                        result =
                            getHtmlOutputHandler().getHtmlResult(request,
                                OutputHandler.OUTPUT_HTML, child);
                    }
                    content = new String(result.getContent());
                } else {
                    if (child.getTypeHandler().isType(TYPE_WIKIPAGE)) {
                        String wikitext = child.getValue(0,
                                              child.getDescription());
                        content = wikifyEntry(request, child, wikitext,
                                false, null, null);
                    } else {
                        content = child.getDescription();
                        if (wikify) {
                            content = wikifyEntry(request, child, content,
                                    false, null, null);

                        }
                    }
                    if (child.getResource().isImage()) {
                        Request newRequest = makeRequest(request, props);
                        content = getMapManager().makeInfoBubble(newRequest,
                                child);
                    }
                }
                */

                if (showLink) {
                    String url;
                    if (linkResource
                            && (child.isFile()
                                || child.getResource().isUrl())) {
                        url = child.getTypeHandler().getEntryResourceUrl(
                            request, child);
                    } else {
                        url = request.entryUrl(
                            getRepository().URL_ENTRY_SHOW, child);
                    }
                    String href = HtmlUtils.href(url, linklabel.isEmpty()
                            ? getEntryDisplayName(child)
                            : linklabel);

                    content = content + HtmlUtils.br()
                              + HtmlUtils.leftRight("", href);
                }
                contents.add(content);

            }


            if (theTag.equals(WIKI_PROP_ACCORDIAN)) {
                HtmlUtils.makeAccordian(sb, titles, contents);

                return sb.toString();
            } else if (doingSlideshow) {
                // for slideshow
                boolean shownav = Misc.getProperty(props, "shownav", false);
                boolean autoplay = Misc.getProperty(props, "autoplay", false);
                int     playSpeed   = Misc.getProperty(props, "speed", 5);

                String  arrowWidth  = "24";
                String  arrowHeight = "43";
                String  slideId     = "slides_" + (idCounter++);

                sb.append(HtmlUtils.open("style",
                                         HtmlUtils.attr("type", "text/css")));
                // need to set the height of the div to include the nav bar
                sb.append("#" + slideId + " { width: " + width
                          + "px; height: " + (height + 30) + "}\n");


                int border = Misc.getProperty(props, ATTR_BORDER, 1);
                String borderColor = Misc.getProperty(props,
                                         ATTR_BORDERCOLOR, "#aaa");
                sb.append(
                    "#" + slideId + " .slides_container {border: " + border
                    + "px solid " + borderColor + "; width:" + width
                    + "px;overflow:hidden;position:relative;display:none;}\n.slides_container div.slide {width:"
                    + width + "px;height:" + height + "px;display:block;}\n");
                sb.append("</style>\n\n");
                sb.append("<link rel=\"stylesheet\" href=\"");
                sb.append(
                    getRepository().htdocsUrl("/lib/slides/paginate.css"));
                sb.append("\" type=\"text/css\" media=\"screen\" />");
                sb.append("\n");


                // user speed is seconds, script uses milliseconds - 0 == no play
                int startSpeed = (autoplay)
                                 ? playSpeed * 1000
                                 : 0;
                String slideParams =
                    "preload: false, preloadImage: "
                    + HtmlUtils.squote(
                        getRepository().htdocsUrl(
                            "/lib/slides/img/loading.gif")) + ", play: "
                                + startSpeed
                                + ", pause: 2500, hoverPause: true"
                                + ", generatePagination: " + shownav + "\n";
                StringBuffer js = new StringBuffer();

                js.append(
                    "$(function(){\n$(" + HtmlUtils.squote("#" + slideId)
                    + ").slides({" + slideParams
                    + ",\nslidesLoaded: function() {$('.caption').animate({ bottom:0 },200); }\n});\n});\n");

                sb.append(HtmlUtils.open(HtmlUtils.TAG_DIV,
                                         HtmlUtils.id(slideId)));



                String prevImage =
                    HtmlUtils.href(
                        "#",
                        HtmlUtils.img(
                            getRepository().htdocsUrl(
                                "/lib/slides/img/arrow-prev.png"), "Prev",
                                    " width=18 "), HtmlUtils.cssClass(
                                        "prev"));

                String nextImage =
                    HtmlUtils.href(
                        "#",
                        HtmlUtils.img(
                            getRepository().htdocsUrl(
                                "/lib/slides/img/arrow-next.png"), "Next",
                                    " width=18 "), HtmlUtils.cssClass(
                                        "next"));


                sb.append(
                    "<table border=\"0\" cellpadding=\"0\" cellspacing=\"0\"><tr>\n");
                sb.append(HtmlUtils.col(prevImage, "width=1"));
                sb.append(HtmlUtils.open(HtmlUtils.TAG_TD,
                                         HtmlUtils.attr(HtmlUtils.ATTR_WIDTH,
                                             "" + width)));
                sb.append(
                    HtmlUtils.open(
                        HtmlUtils.TAG_DIV,
                        HtmlUtils.cssClass("slides_container")));
                for (int i = 0; i < titles.size(); i++) {
                    String title   = titles.get(i);
                    String content = contents.get(i);
                    sb.append("\n");
                    sb.append(HtmlUtils.open(HtmlUtils.TAG_DIV,
                                             HtmlUtils.cssClass("slide")));
                    sb.append(content);
                    //                    sb.append(HtmlUtils.br());
                    //                    sb.append(title);
                    sb.append(HtmlUtils.close(HtmlUtils.TAG_DIV));  // slide
                }
                sb.append(HtmlUtils.close(HtmlUtils.TAG_DIV));  // slides_container
                sb.append(HtmlUtils.close(HtmlUtils.TAG_TD));
                sb.append(HtmlUtils.col(nextImage, "width=1"));
                sb.append("</tr></table>");
                sb.append(HtmlUtils.close(HtmlUtils.TAG_DIV));  // slideId

                sb.append(
                    HtmlUtils.importJS(
                        getRepository().htdocsUrl(
                            "/lib/slides/slides.min.jquery.js")));

                sb.append(HtmlUtils.script(js.toString()));

                return sb.toString();
            } else {
                return OutputHandler.makeTabs(titles, contents, true,
                        useCookies);
            }
        } else if (theTag.equals(WIKI_PROP_GRID)) {
            getHtmlOutputHandler().makeGrid(request,
                                            getEntries(request,
                                                originalEntry, entry,
                                                    props), sb);

            return sb.toString();
        } else if (theTag.equals(WIKI_PROP_TABLE)) {
            List<Entry> entries = getEntries(request, originalEntry, entry,
                                             props);
            getHtmlOutputHandler().makeTable(request, entries, sb);

            return sb.toString();
        } else if (theTag.equals(WIKI_PROP_RECENT)) {
            List<Entry> children = getEntries(request, originalEntry, entry,
                                       props);
            int numDays = Misc.getProperty(props, ATTR_DAYS, 3);
            BufferMapList<Date> map = new BufferMapList<Date>();
            SimpleDateFormat dateFormat =
                new SimpleDateFormat("EEEEE MMMMM d");
            dateFormat.setTimeZone(RepositoryUtil.TIMEZONE_DEFAULT);
            Date firstDay = ((children.size() > 0)
                             ? new Date(children.get(0).getChangeDate())
                             : new Date());
            GregorianCalendar cal1 =
                new GregorianCalendar(RepositoryUtil.TIMEZONE_DEFAULT);
            cal1.setTime(new Date(firstDay.getTime()));
            cal1.set(cal1.MILLISECOND, 0);
            cal1.set(cal1.SECOND, 0);
            cal1.set(cal1.MINUTE, 0);
            cal1.set(cal1.HOUR, 0);
            GregorianCalendar cal2 =
                new GregorianCalendar(RepositoryUtil.TIMEZONE_DEFAULT);
            cal2.setTime(cal1.getTime());
            cal2.roll(cal2.DAY_OF_YEAR, 1);

            for (int i = 0; i < numDays; i++) {
                Date date1 = cal1.getTime();
                Date date2 = cal2.getTime();
                cal2.setTime(cal1.getTime());
                cal1.roll(cal1.DAY_OF_YEAR, -1);
                for (Entry e : children) {
                    Date changeDate = new Date(e.getChangeDate());
                    if ((changeDate.getTime() < date1.getTime())
                            || (changeDate.getTime() > date2.getTime())) {
                        continue;
                    }
                    StringBuffer buff = map.get(date1);
                    buff.append("<tr><td width=75%>&nbsp;&nbsp;&nbsp;");
                    buff.append(getEntryManager().getAjaxLink(request, e,
                            e.getLabel()));
                    buff.append("</td><td width=25% align=right><i>");
                    buff.append(formatDate(request, changeDate));
                    buff.append("</i></td></tr>");
                }
            }
            for (Date date : map.getKeys()) {
                StringBuffer tmp = new StringBuffer();
                String msg = msg("New on") + " " + dateFormat.format(date);
                tmp.append("<table width=100% border=0>");
                tmp.append(map.get(date));
                tmp.append("</table>");
                sb.append(HtmlUtils.makeShowHideBlock(msg, tmp.toString(),
                        true));
            }

            return sb.toString();

        } else if (theTag.equals(WIKI_PROP_PLAYER)
                   || theTag.equals(WIKI_PROP_PLAYER_OLD)) {
            List<Entry> children = getEntries(request, originalEntry, entry,
                                       props, true);
            if (children.size() == 0) {
                return null;
            }
            ImageOutputHandler imageOutputHandler =
                (ImageOutputHandler) getRepository().getOutputHandler(
                    ImageOutputHandler.OUTPUT_PLAYER);
            Request imageRequest = request.cloneMe();
            int     width        = Misc.getProperty(props, ATTR_WIDTH, 0);
            if (width > 0) {
                imageRequest.put(ARG_WIDTH, "" + width);
            }
            int height = Misc.getProperty(props, ATTR_HEIGHT, 0);
            if (height > 0) {
                imageRequest.put(ARG_HEIGHT, "" + height);
            }
            imageOutputHandler.makePlayer(imageRequest, children, sb, false);

            return sb.toString();
        } else if (theTag.equals(WIKI_PROP_GALLERY)) {
            List<Entry> children = getEntries(request, originalEntry, entry,
                                       props, true);
            if (children.size() == 0) {
                String message = Misc.getProperty(props, ATTR_MESSAGE,
                                     (String) null);
                if (message != null) {
                    return message;
                }
            }
            makeGallery(request, children, props, sb);

            return sb.toString();
        } else if (theTag.equals(WIKI_PROP_ROOT)) {
            return getRepository().getUrlBase();
        } else if (theTag.equals(WIKI_PROP_CHILDREN_GROUPS)
                   || theTag.equals(WIKI_PROP_CHILDREN_ENTRIES)
                   || theTag.equals(WIKI_PROP_CHILDREN)
                   || theTag.equals(WIKI_PROP_TREE)) {

            if (theTag.equals(WIKI_PROP_CHILDREN_GROUPS)) {
                props.put(ATTR_FOLDERS, "true");
            } else if (theTag.equals(WIKI_PROP_CHILDREN_ENTRIES)) {
                props.put(ATTR_FILES, "true");
            }

            List<Entry> children = getEntries(request, originalEntry, entry,
                                       props);
            if (children.size() == 0) {
                return null;
            }
            boolean showCategories = Misc.getProperty(props,
                                         ARG_SHOWCATEGORIES, false);
            if (showCategories) {
                request.put(ARG_SHOWCATEGORIES, "true");
            }
            boolean decorate    = Misc.getProperty(props, ATTR_DECORATE,
                                      true);
            boolean showDetails = Misc.getProperty(props, ATTR_DETAILS, true);

            if ( !showDetails) {
                request.put(ARG_DETAILS, "false");
            }
            if ( !decorate) {
                request.put(ARG_DECORATE, "false");
            }
            String link = getHtmlOutputHandler().getEntriesList(request, sb,
                              children, true, false, showDetails);
            if ( !decorate) {
                request.put(ARG_DECORATE, "false");
            }
            if ( !showDetails) {
                request.remove(ARG_DETAILS);
            }
            if (showCategories) {
                request.remove(ARG_SHOWCATEGORIES);
            }
            if (Misc.getProperty(props, "form", false)) {
                return link + HtmlUtils.br() + sb.toString();
            } else {
                return sb.toString();
            }

        } else if (theTag.equals(WIKI_PROP_TREEVIEW)) {
            List<Entry> children = getEntries(request, originalEntry, entry,
                                       props);
            if (children.size() == 0) {
                return null;
            }
            getHtmlOutputHandler().makeTreeView(request, children, sb);

            return sb.toString();
        } else if (theTag.equals(WIKI_PROP_LINKS)
                   || theTag.equals(WIKI_PROP_LIST)) {
            boolean isList = theTag.equals(WIKI_PROP_LIST);
            List<Entry> children = getEntries(request, originalEntry, entry,
                                       props);
            if (children.size() == 0) {
                return null;
            }
            boolean includeIcon = Misc.getProperty(props, ATTR_INCLUDEICON,
                                      false);
            boolean linkResource = Misc.getProperty(props, ATTR_LINKRESOURCE,
                                       false);
            String separator = (isList
                                ? ""
                                : Misc.getProperty(props, ATTR_SEPARATOR,
                                    ""));
            String cssClass = Misc.getProperty(props, ATTR_CLASS, "");
            String style    = Misc.getProperty(props, ATTR_STYLE, "style");
            String tagOpen  = (isList
                               ? "<li>"
                               : Misc.getProperty(props, ATTR_TAGOPEN,
                                   "<li>"));

            String tagClose = (isList
                               ? ""
                               : Misc.getProperty(props, ATTR_TAGCLOSE, ""));

            if (includeIcon) {
                tagOpen  = "";
                tagClose = "<br>";
            }

            List<String> links = new ArrayList<String>();
            for (Entry child : children) {
                String url;
                if (linkResource
                        && (child.getTypeHandler().isType("link")
                            || child.isFile()
                            || child.getResource().isUrl())) {
                    url = child.getTypeHandler().getEntryResourceUrl(request,
                            child);
                } else {
                    url = request.entryUrl(getRepository().URL_ENTRY_SHOW,
                                           child);
                }

                String linkLabel = getEntryDisplayName(child);
                if (includeIcon) {
                    linkLabel =
                        HtmlUtils.img(getPageHandler().getIconUrl(request,
                            child)) + " " + linkLabel;
                }
                String href = HtmlUtils.href(url, linkLabel,
                                             HtmlUtils.cssClass(cssClass)
                                             + HtmlUtils.style(style));
                StringBuffer link = new StringBuffer();
                link.append(tagOpen);
                link.append(href);
                link.append(tagClose);
                links.add(link.toString());
            }

            return StringUtil.join(separator, links);
        } else {
            String fromTypeHandler =
                entry.getTypeHandler().getWikiInclude(wikiUtil, request,
                    originalEntry, entry, theTag, props);
            if (fromTypeHandler != null) {
                return fromTypeHandler;
            }

            for (PageDecorator pageDecorator :
                    repository.getPluginManager().getPageDecorators()) {
                String fromPageDecorator =
                    pageDecorator.getWikiInclude(wikiUtil, request,
                        originalEntry, entry, theTag, props);
                if (fromPageDecorator != null) {
                    return fromPageDecorator;
                }

            }

            return null;
        }

    }

    /**
     * _more_
     *
     * @param request _more_
     * @param props _more_
     *
     * @return _more_
     *
     * @throws Exception _more_
     */
    private Request copyRequest(Request request, Hashtable props)
            throws Exception {
        Request clonedRequest = request.cloneMe();
        clonedRequest.putAll(props);

        return clonedRequest;
    }



    /**
     * Add the image popup javascript
     *
     * @param request  the Request
     * @param buf      the page StringBuffer
     * @param props    the properties
     */
    private void addImagePopupJS(Request request, StringBuffer buf,
                                 Hashtable props) {
        if (request.getExtraProperty("added fancybox") == null) {
            String captionpos = Misc.getProperty(props, ATTR_POPUPCAPTION,
                                    "none");
            StringBuffer options = new StringBuffer("{");
            if ( !captionpos.equals("none")) {
                options.append(HtmlUtils.squote("titlePosition"));
                options.append(" : ");
                if (captionpos.equals("inside")) {
                    options.append(HtmlUtils.squote("inside"));
                } else if (captionpos.equals("over")) {
                    options.append(HtmlUtils.squote("over"));
                } else {
                    options.append(HtmlUtils.squote("outside"));
                }
            } else {
                options.append(HtmlUtils.squote("titleShow"));
                options.append(" : ");
                options.append("false");
            }
            options.append("}");

            buf.append(
                HtmlUtils.importJS(
                    getRepository().htdocsUrl(
                        "/lib/fancybox/jquery.fancybox-1.3.4.pack.js")));
            buf.append("\n");
            buf.append("<link rel=\"stylesheet\" href=\"");
            buf.append(
                getRepository().htdocsUrl(
                    "/lib/fancybox/jquery.fancybox-1.3.4.css"));
            buf.append("\" type=\"text/css\" media=\"screen\" />");
            buf.append("\n");
            buf.append(
                HtmlUtils.script(
                    "$(document).ready(function() {\n $(\"a.popup_image\").fancybox("
                    + options.toString() + ");\n });\n"));

            request.putExtraProperty("added fancybox", "yes");
        }
    }

    /**
     * Make a WikiUtil class for the request
     *
     * @param request  the Request
     * @param makeHeadings  true to make headings
     *
     * @return  the WikiUtil
     */
    private WikiUtil makeWikiUtil(Request request, boolean makeHeadings) {
        WikiUtil wikiUtil = new WikiUtil();
        wikiUtil.setMakeHeadings(makeHeadings);
        wikiUtil.setMobile(request.isMobile());
        if ( !request.isAnonymous()) {
            wikiUtil.setUser(request.getUser().getId());
        }

        return wikiUtil;
    }


    /**
     * _more_
     *
     * @param original _more_
     * @param props _more_
     *
     * @return _more_
     */
    private Request makeRequest(Request original, Hashtable props) {
        Request newRequest = original.cloneMe();
        newRequest.putAll(props);
        newRequest.put(ARG_EMBEDDED, "true");

        return newRequest;
    }

    /**
     * Make entry tabs html
     *
     * @param request The request
     * @param entry  the entry
     * @param props _more_
     *
     * @return the entry tabs html
     *
     * @throws Exception  problems
     */
    private String makeEntryTabs(Request request, Entry entry,
                                 Hashtable props)
            throws Exception {

        List<String> onlyTheseTypes = null;
        List<String> notTheseTypes  = null;

        String metadataTypesAttr = Misc.getProperty(props,
                                       ATTR_METADATA_TYPES, (String) null);
        if (metadataTypesAttr != null) {
            onlyTheseTypes = new ArrayList<String>();
            notTheseTypes  = new ArrayList<String>();
            for (String type :
                    StringUtil.split(metadataTypesAttr, ",", true, true)) {
                if (type.startsWith("!")) {
                    notTheseTypes.add(type.substring(1));
                } else {
                    onlyTheseTypes.add(type);
                }
            }
        }
        List tabTitles   = new ArrayList<String>();
        List tabContents = new ArrayList<String>();
        for (TwoFacedObject tfo :
                getRepository().getHtmlOutputHandler().getMetadataHtml(
                    request, entry, onlyTheseTypes, notTheseTypes)) {
            tabTitles.add(tfo.toString());
            tabContents.add(tfo.getId());
        }
        if (tabTitles.size() == 0) {
            return getMessage(props, "No metadata found");
        }
        if (tabTitles.size() > 1) {
            return OutputHandler.makeTabs(tabTitles, tabContents, true);
        }

        return tabContents.get(0).toString();

    }


    /**
     * Get the entries that are images
     *
     * @param entries  the list of entries
     *
     * @return  the list of entries that are images
     */
    public List<Entry> getImageEntries(List<Entry> entries) {
        return getImageEntriesOrNot(entries, false);
    }

    /**
     * _more_
     *
     * @param entries _more_
     * @param entry _more_
     * @param flag _more_
     * @param orNot _more_
     */
    private void orNot(List<Entry> entries, Entry entry, boolean flag,
                       boolean orNot) {
        if (orNot) {
            if ( !flag) {
                entries.add(entry);
            }
        } else {
            if (flag) {
                entries.add(entry);
            }
        }
    }


    /**
     * _more_
     *
     * @param entries _more_
     * @param orNot _more_
     *
     * @return _more_
     */
    public List<Entry> getImageEntriesOrNot(List<Entry> entries,
                                            boolean orNot) {
        List<Entry> imageEntries = new ArrayList<Entry>();
        for (Entry entry : entries) {
            orNot(imageEntries, entry, entry.getResource().isImage(), orNot);
        }

        return imageEntries;
    }

    /**
     * Get the entries for the request
     *
     * @param request The request
     * @param originalEntry _more_
     * @param entry  the parent entry
     * @param props  properties
     *
     * @return the list of entries
     *
     * @throws Exception problems
     */
    public List<Entry> getEntries(Request request, Entry originalEntry,
                                  Entry entry, Hashtable props)
            throws Exception {

        return getEntries(request, originalEntry, entry, props, false, "");
    }



    /**
     * Get the entries for the request
     *
     * @param request  the request
     * @param originalEntry _more_
     * @param entry    the entry
     * @param props    properties
     * @param onlyImages  true to only show images
     *
     * @return the list of Entry's
     *
     * @throws Exception  problems making list
     */
    public List<Entry> getEntries(Request request, Entry originalEntry,
                                  Entry entry, Hashtable props,
                                  boolean onlyImages)
            throws Exception {
        return getEntries(request, originalEntry, entry, props, onlyImages,
                          "");
    }


    /**
     * Get the entries for the request
     *
     * @param request  the request
     * @param originalEntry _more_
     * @param entry    the entry
     * @param props    properties
     * @param onlyImages  true to only show images
     * @param attrPrefix _more_
     *
     * @return the list of Entry's
     *
     * @throws Exception  problems making list
     */
    public List<Entry> getEntries(Request request, Entry originalEntry,
                                  Entry entry, Hashtable props,
                                  boolean onlyImages, String attrPrefix)
            throws Exception {
        if (props == null) {
            props = new Hashtable();
        } else {
            Hashtable tmp = new Hashtable();
            tmp.putAll(props);
            props = tmp;
        }

        String userDefinedEntries = Misc.getProperty(props,
                                        attrPrefix + ATTR_ENTRIES,
                                        ID_CHILDREN);

        return getEntries(request, originalEntry, entry, userDefinedEntries,
                          props, onlyImages, attrPrefix);
    }



    /**
     * _more_
     *
     * @param request _more_
     * @param originalEntry _more_
     * @param entry _more_
     * @param userDefinedEntries _more_
     * @param props _more_
     * @param onlyImages _more_
     * @param attrPrefix _more_
     *
     * @return _more_
     *
     * @throws Exception _more_
     */
    public List<Entry> getEntries(Request request, Entry originalEntry,
                                  Entry entry, String userDefinedEntries,
                                  Hashtable props, boolean onlyImages,
                                  String attrPrefix)
            throws Exception {


        if (props == null) {
            props = new Hashtable();
        }

        //If there is a max property then clone the request and set the max
        int max = Misc.getProperty(props, attrPrefix + ATTR_MAX, -1);
        if (max > 0) {
            request = request.cloneMe();
            request.put(ARG_MAX, "" + max);
        }



        List<Entry> entries = getEntries(request, entry, userDefinedEntries,
                                         props);
        String filter = Misc.getProperty(props,
                                         attrPrefix + ATTR_ENTRIES
                                         + ".filter", (String) null);

        if (Misc.getProperty(props, attrPrefix + ATTR_FOLDERS, false)) {
            filter = FILTER_FOLDER;
        }

        if (Misc.getProperty(props, attrPrefix + ATTR_FILES, false)) {
            filter = FILTER_FILE;
        }


        //TODO - how do we combine filters? what kind of or/and logic?
        if (filter != null) {
            boolean doNot = false;
            if (filter.startsWith("!")) {
                doNot  = true;
                filter = filter.substring(1);
            }
            if (filter.equals(FILTER_IMAGE)) {
                entries = getImageEntriesOrNot(entries, doNot);
            } else if (filter.equals(FILTER_FILE)) {
                List<Entry> tmp = new ArrayList<Entry>();
                for (Entry child : entries) {
                    orNot(tmp, child, !child.isGroup(), doNot);
                }
                entries = tmp;
            } else if (filter.equals(FILTER_GEO)) {
                List<Entry> tmp = new ArrayList<Entry>();
                for (Entry child : entries) {
                    orNot(tmp, child, child.isGeoreferenced(), doNot);
                }
                entries = tmp;
            } else if (filter.equals(FILTER_FOLDER)) {
                List<Entry> tmp = new ArrayList<Entry>();
                for (Entry child : entries) {
                    orNot(tmp, child, child.isGroup(), doNot);
                }
                entries = tmp;
            } else if (filter.startsWith(FILTER_TYPE)) {
                String      type = filter.substring(FILTER_TYPE.length());
                List<Entry> tmp  = new ArrayList<Entry>();
                for (Entry child : entries) {
                    orNot(tmp, child, child.getTypeHandler().isType(type),
                          doNot);
                }
                entries = tmp;
            } else if (filter.startsWith(FILTER_SUFFIX)) {
                List<String> suffixes = StringUtil.split(
                                            filter.substring(
                                                FILTER_SUFFIX.length()), ",",
                                                    true, true);
                List<Entry> tmp = new ArrayList<Entry>();
                for (Entry child : entries) {
                    for (String suffix : suffixes) {
                        boolean matches =
                            child.getResource().getPath().endsWith(suffix);
                        orNot(tmp, child, matches, doNot);
                        if (matches) {
                            break;
                        }
                    }
                }
                entries = tmp;
            }
        }


        if (onlyImages
                || Misc.getProperty(props, attrPrefix + ATTR_IMAGES, false)) {
            entries = getImageEntries(entries);
        }


        String excludeEntries = Misc.getProperty(props,
                                    attrPrefix + ATTR_EXCLUDE, (String) null);

        if (excludeEntries != null) {
            HashSet seen = new HashSet();
            for (String id : StringUtil.split(excludeEntries, ",")) {
                if (id.equals(ID_THIS)) {
                    seen.add(originalEntry.getId());
                } else {
                    seen.add(id);
                }
            }
            List<Entry> okEntries = new ArrayList<Entry>();
            for (Entry e : entries) {
                if ( !seen.contains(e.getId())
                        && !seen.contains(e.getName())) {
                    okEntries.add(e);
                }
            }
            entries = okEntries;
        }


        String sort = Misc.getProperty(props, attrPrefix + ATTR_SORT,
                                       (String) null);
        if (sort != null) {
            boolean ascending = Misc.getProperty(props,
                                    attrPrefix + ATTR_SORT_ORDER,
                                    "up").equals("up");
            if (sort.equals(SORT_DATE)) {
                entries = getEntryUtil().sortEntriesOnDate(entries,
                        !ascending);
            } else if (sort.equals(SORT_CHANGEDATE)) {
                entries = getEntryUtil().sortEntriesOnChangeDate(entries,
                        !ascending);
            } else if (sort.equals(SORT_NAME)) {
                entries = getEntryUtil().sortEntriesOnName(entries,
                        !ascending);
            } else {
                throw new IllegalArgumentException("Unknown sort:" + sort);
            }
        }

        String firstEntries = Misc.getProperty(props,
                                  attrPrefix + ATTR_FIRST, (String) null);

        if (firstEntries != null) {
            Hashtable<String, Entry> map = new Hashtable<String, Entry>();
            for (Entry child : entries) {
                map.put(child.getId(), child);
            }
            List<String> ids = StringUtil.split(firstEntries, ",");
            for (int i = ids.size() - 1; i >= 0; i--) {
                Entry firstEntry = map.get(ids.get(i));
                if (firstEntry == null) {
                    continue;
                }
                entries.remove(firstEntry);
                entries.add(0, firstEntry);
            }
        }


        String lastEntries = Misc.getProperty(props, attrPrefix + ATTR_LAST,
                                 (String) null);

        if (lastEntries != null) {
            Hashtable<String, Entry> map = new Hashtable<String, Entry>();
            for (Entry child : entries) {
                map.put(child.getId(), child);
            }
            List<String> ids = StringUtil.split(lastEntries, ",");
            for (int i = ids.size() - 1; i >= 0; i--) {
                Entry lastEntry = map.get(ids.get(i));
                if (lastEntry == null) {
                    continue;
                }
                entries.remove(lastEntry);
                entries.add(lastEntry);
            }
        }

        String name = Misc.getProperty(props, attrPrefix + ATTR_NAME,
                                       (String) null);
        String pattern = (name == null)
                         ? null
                         : getPattern(name);
        if (name != null) {
            List<Entry> tmp = new ArrayList<Entry>();
            for (Entry child : entries) {
                if (entryMatches(child, pattern, name)) {
                    tmp.add(child);
                }
            }
            entries = tmp;
        }


        int count = Misc.getProperty(props, attrPrefix + ATTR_COUNT, -1);
        if (count > 0) {
            List<Entry> tmp = new ArrayList<Entry>();
            for (Entry child : entries) {
                tmp.add(child);
                if (tmp.size() >= count) {
                    break;
                }
            }
            entries = tmp;
        }

        return entries;

    }


    /**
     * Get the entries corresponding to the ids
     *
     * @param request the Request
     * @param baseEntry _more_
     * @param ids  list of comma separated ids
     * @param props _more_
     *
     * @return List of Entrys
     *
     * @throws Exception problem getting entries
     */
    public List<Entry> getEntries(Request request, Entry baseEntry,
                                  String ids, Hashtable props)
            throws Exception {

        if (props == null) {
            props = new Hashtable();
        }
        Hashtable   searchProps = null;
        List<Entry> entries     = new ArrayList<Entry>();
        Request myRequest = new Request(getRepository(), request.getUser());
        for (String entryid : StringUtil.split(ids, ",", true, true)) {
            if (entryid.startsWith("#")) {
                continue;
            }
            entryid = entryid.replace("_COMMA_", ",");

            if (entryid.equals(ID_ANCESTORS)) {
                List<Entry> tmp    = new ArrayList<Entry>();
                Entry       parent = baseEntry.getParentEntry();
                while (parent != null) {
                    tmp.add(0, parent);
                    parent = parent.getParentEntry();
                }
                entries.addAll(tmp);

                continue;
            }

            if (entryid.equals(ID_LINKS)) {
                List<Association> associations =
                    getRepository().getAssociationManager().getAssociations(
                        request, baseEntry.getId());
                for (Association association : associations) {
                    String id = null;
                    if ( !association.getFromId().equals(baseEntry.getId())) {
                        id = association.getFromId();
                    } else if ( !association.getToId().equals(
                            baseEntry.getId())) {
                        id = association.getToId();
                    } else {
                        continue;
                    }
                    entries.add(getEntryManager().getEntry(request, id));
                }

                continue;
            }

            if (entryid.equals(ID_ROOT)) {
                entries.add(getEntryManager().getTopGroup());

                continue;
            }


            if (entryid.startsWith(ID_REMOTE)) {
                //                http://ramadda.org/repository/entry/show/Home/RAMADDA+Examples?entryid=a96b9616-40b0-41f5-914a-fb1be157d97c
                List<String> toks = StringUtil.splitUpTo(entryid, ID_REMOTE,
                                        2);
                String url = toks.get(1);

                continue;
            }

            if (entryid.equals(ID_THIS)) {
                entries.add(baseEntry);

                continue;
            }


            if (entryid.startsWith(ATTR_ENTRIES + ".filter")) {
                List<String> toks = StringUtil.splitUpTo(entryid, "=", 2);
                if (toks.size() == 2) {
                    props.put(ATTR_ENTRIES + ".filter", toks.get(1));
                }

                continue;
            }


            boolean isRemote = entryid.startsWith(ATTR_SEARCH_URL);
            if ( !isRemote && entryid.startsWith(ID_SEARCH + ".")) {
                List<String> toks = StringUtil.splitUpTo(entryid, "=", 2);
                if (toks.size() == 2) {
                    if (searchProps == null) {
                        searchProps = new Hashtable();
                        searchProps.putAll(props);
                    }
                    searchProps.put(toks.get(0), toks.get(1));
                    myRequest.put(toks.get(0), toks.get(1));
                }

                continue;
            }

            if (isRemote || entryid.equals(ID_SEARCH)) {
                if (searchProps == null) {
                    searchProps = props;
                }
                myRequest.put(ARG_AREA_MODE,
                              Misc.getProperty(searchProps, ARG_AREA_MODE,
                                  VALUE_AREA_CONTAINS));
                myRequest.put(ARG_MAX,
                              Misc.getProperty(searchProps,
                                  PREFIX_SEARCH + ARG_MAX, "100"));

                addSearchTerms(myRequest, searchProps, baseEntry);

                if (isRemote) {
                    List<String> toks = (entryid.indexOf("=") >= 0)
                                        ? StringUtil.splitUpTo(entryid, "=",
                                            2)
                                        : StringUtil.splitUpTo(entryid, ":",
                                            2);
                    ServerInfo serverInfo =
                        new ServerInfo(new URL(toks.get(1)), "remote server",
                                       "");

                    List<ServerInfo> servers = new ArrayList<ServerInfo>();
                    servers.add(serverInfo);
                    List<Entry> remoteGroups  = new ArrayList<Entry>();
                    List<Entry> remoteEntries = new ArrayList<Entry>();

                    getSearchManager().doDistributedSearch(myRequest,
                            servers, baseEntry, remoteGroups, remoteEntries);
                    entries.addAll(remoteGroups);
                    entries.addAll(remoteEntries);

                    continue;
                }

                List<Entry>[] pair = getEntryManager().getEntries(myRequest);
                entries.addAll(pair[0]);
                entries.addAll(pair[1]);

                continue;
            }


            if (entryid.equals(ID_PARENT)) {
                entries.add(getEntryManager().getEntry(request,
                        baseEntry.getParentEntryId()));

                continue;
            }

            if (entryid.equals(ID_SIBLINGS)) {
                Entry parent = getEntryManager().getEntry(request,
                                   baseEntry.getParentEntryId());
                if (parent != null) {
                    for (Entry sibling :
                            getEntryManager().getChildren(request, parent)) {
                        if ( !sibling.getId().equals(baseEntry.getId())) {
                            entries.add(sibling);
                        }
                    }
                }

                continue;
            }


            if (entryid.equals(ID_GRANDPARENT)) {
                Entry parent = getEntryManager().getEntry(request,
                                   baseEntry.getParentEntryId());
                if (parent != null) {
                    Entry grandparent = getEntryManager().getEntry(request,
                                            parent.getParentEntryId());
                    if (grandparent != null) {
                        entries.add(grandparent);
                    }
                }

                continue;
            }

            if (entryid.equals(ID_CHILDREN)) {
                List<Entry> children = getEntryManager().getChildren(request,
                                           baseEntry);
                entries.addAll(children);

                continue;
            }


            if (entryid.equals(ID_GRANDCHILDREN)
                    || entryid.equals(ID_GREATGRANDCHILDREN)) {
                List<Entry> children = getEntryManager().getChildren(request,
                                           baseEntry);
                List<Entry> grandChildren = new ArrayList<Entry>();
                for (Entry child : children) {
                    //Include the children non folders
                    if ( !child.isGroup()) {
                        grandChildren.add(child);
                    } else {
                        grandChildren.addAll(
                            getEntryManager().getChildren(request, child));
                    }
                }


                if (entryid.equals(ID_GREATGRANDCHILDREN)) {
                    List<Entry> greatgrandChildren = new ArrayList<Entry>();
                    for (Entry child : grandChildren) {
                        if ( !child.isGroup()) {
                            greatgrandChildren.add(child);
                        } else {
                            greatgrandChildren.addAll(
                                getEntryManager().getChildren(
                                    request, child));

                        }
                    }
                    grandChildren = greatgrandChildren;
                }

                entries.addAll(
                    getEntryUtil().sortEntriesOnDate(grandChildren, true));

                continue;
            }

            boolean addChildren = false;
            if (entryid.startsWith("+")) {
                addChildren = true;
                entryid     = entryid.substring(1);
            }

            Entry entry = getEntryManager().getEntry(request, entryid);
            if (entry != null) {
                if (addChildren) {
                    List<Entry> children =
                        getEntryManager().getChildrenAll(request, entry);
                    entries.addAll(children);
                } else {
                    entries.add(entry);
                }
            }
        }




        return entries;

    }




    /**
     * _more_
     *
     * @param request _more_
     * @param props _more_
     * @param baseEntry _more_
     *
     * @throws Exception _more_
     */
    private void addSearchTerms(Request request, Hashtable props,
                                Entry baseEntry)
            throws Exception {
        String[] args = new String[] {
            ARG_TEXT, ARG_TYPE, ARG_GROUP, ARG_FILESUFFIX, ARG_BBOX,
            ARG_BBOX + ".north", ARG_BBOX + ".west", ARG_BBOX + ".south",
            ARG_BBOX + ".east", DateArgument.ARG_DATA.getFrom(),
            DateArgument.ARG_DATA.getTo(),
            DateArgument.ARG_DATA.getRelative(),
            DateArgument.ARG_CREATE.getFrom(),
            DateArgument.ARG_CREATE.getTo(),
            DateArgument.ARG_CREATE.getRelative(),
            DateArgument.ARG_CHANGE.getFrom(),
            DateArgument.ARG_CHANGE.getTo(),
            DateArgument.ARG_CHANGE.getRelative(),
        };
        for (String arg : args) {
            String text = (String) props.get(PREFIX_SEARCH + arg);
            if (text == null) {
                text = (String) props.get(arg);
            }

            if (text != null) {
                if (arg.equals(ARG_GROUP)) {
                    //TODO: Handle other identifiers
                    if (text.equals(ID_THIS)) {
                        text = baseEntry.getId();
                    }
                }
                request.put(arg, text);
            }
        }
    }


    /**
     * Make the gallery
     *
     * @param request   the request
     * @param imageEntries  the list of image entries
     * @param props         the tag properties
     * @param sb            the string buffer to add to
     *
     * @throws Exception  problem making the gallery
     */
    public void makeGallery(Request request, List<Entry> imageEntries,
                            Hashtable props, StringBuffer sb)
            throws Exception {

        int     width        = Misc.getProperty(props, ATTR_WIDTH, 200);
        int serverImageWidth = Misc.getProperty(props, ATTR_IMAGEWIDTH, -1);

        int     columns      = Misc.getProperty(props, ATTR_COLUMNS, 3);
        boolean random       = Misc.getProperty(props, ATTR_RANDOM, false);
        boolean popup        = Misc.getProperty(props, ATTR_POPUP, true);
        boolean thumbnail    = Misc.getProperty(props, ATTR_THUMBNAIL, true);
        String  caption = Misc.getProperty(props, ATTR_CAPTION, "${name}");
        String captionPos = Misc.getProperty(props, ATTR_POPUPCAPTION,
                                             "none");
        boolean showDesc = Misc.getProperty(props, ATTR_SHOWDESCRIPTION,
                                            false);
        if (popup) {
            addImagePopupJS(request, sb, props);
        }
        int size = imageEntries.size();
        if (random && (size > 1)) {
            int randomIdx = (int) (Math.random() * size);
            if (randomIdx >= size) {
                randomIdx = size;
            }
            Entry randomEntry = imageEntries.get(randomIdx);
            imageEntries = new ArrayList<Entry>();
            imageEntries.add(randomEntry);
        }


        StringBuffer[] colsSB = new StringBuffer[columns];
        for (int i = 0; i < columns; i++) {
            colsSB[i] = new StringBuffer();
        }
        int num    = 0;
        int colCnt = 0;

        for (Entry child : imageEntries) {
            num++;
            if (colCnt >= columns) {
                colCnt = 0;
            }
            StringBuffer buff = colsSB[colCnt];
            colCnt++;
            String url = null;

            if (thumbnail) {
                List<String> urls = new ArrayList<String>();
                getMetadataManager().getThumbnailUrls(request, child, urls);
                if (urls.size() > 0) {
                    url = urls.get(0);
                }
            }

            if (url == null) {
                url = HtmlUtils.url(
                    request.url(repository.URL_ENTRY_GET) + "/"
                    + getStorageManager().getFileTail(child), ARG_ENTRYID,
                        child.getId());
            }
            if (serverImageWidth > 0) {
                url = url + "&" + ARG_IMAGEWIDTH + "=" + serverImageWidth;
            }

            String extra = "";
            if (width > 0) {
                extra = extra
                        + HtmlUtils.attr(HtmlUtils.ATTR_WIDTH, "" + width);
            }
            String name = getEntryDisplayName(child);
            if ((name != null) && !name.isEmpty()) {
                extra = extra + HtmlUtils.attr(HtmlUtils.ATTR_ALT, name);
            }
            String img = HtmlUtils.img(url, "", extra);

            String entryUrl =
                request.entryUrl(getRepository().URL_ENTRY_SHOW, child);
            buff.append("<div class=\"image-outer\">");
            buff.append("<div class=\"image-inner\">");
            String theCaption = caption;
            theCaption = theCaption.replace("${count}", "" + num);
            theCaption =
                theCaption.replace("${date}",
                                   formatDate(request,
                                       new Date(child.getStartDate())));
            theCaption = theCaption.replace("${name}", child.getLabel());
            theCaption = theCaption.replace("${description}",
                                            child.getDescription());

            if (popup) {
                String popupExtras = HtmlUtils.cssClass("popup_image");
                if ( !captionPos.equals("none")) {
                    popupExtras += HtmlUtils.attr("title", theCaption);
                }
                buff.append(
                    HtmlUtils.href(
                        child.getTypeHandler().getEntryResourceUrl(
                            request, child), img, popupExtras));
            } else {
                buff.append(img);
            }
            buff.append("</div>");


            theCaption =
                HtmlUtils.href(entryUrl, theCaption,
                               HtmlUtils.style("color:#666;font-size:10pt;"));

            buff.append(HtmlUtils.div(theCaption,
                                      HtmlUtils.cssClass("image-caption")));
            if (showDesc) {
                if (Utils.stringDefined(child.getDescription())) {
                    buff.append("<div class=\"image-description\">");
                    buff.append(child.getDescription());
                    buff.append("</div>");
                }
            }

            buff.append("</div>");
        }
        sb.append("<table cellspacing=4 width='100%'>");
        sb.append("<tr valign=\"top\">");
        for (StringBuffer buff : colsSB) {
            sb.append("<td>");
            sb.append(buff);
            sb.append("</td>");
        }
        sb.append("</tr>");
        sb.append("</table>");

    }


    /**
     * utility to get the htmloutputhandler
     *
     * @return htmloutputhandler
     */
    public HtmlOutputHandler getHtmlOutputHandler() {
        return getRepository().getHtmlOutputHandler();
    }

    /**
     * Get the calendar output handler
     *
     * @return the calendar output handler
     */
    public CalendarOutputHandler getCalendarOutputHandler() {
        try {
            return (CalendarOutputHandler) getRepository().getOutputHandler(
                CalendarOutputHandler.OUTPUT_CALENDAR);
        } catch (Exception exc) {
            throw new RuntimeException(exc);
        }
    }

    /**
     * _more_
     *
     * @return _more_
     */
    public GraphOutputHandler getGraphOutputHandler() {
        try {
            return (GraphOutputHandler) getRepository().getOutputHandler(
                GraphOutputHandler.OUTPUT_GRAPH);
        } catch (Exception exc) {
            throw new RuntimeException(exc);
        }
    }



    /**
     * Handle the wiki import
     *
     * @param wikiUtil The wiki util
     * @param request The request
     * @param originalEntry _more_
     * @param importEntry  the import entry
     * @param tag   the tag
     * @param props properties
     *
     * @return the include output
     */
    private String handleWikiImport(WikiUtil wikiUtil, final Request request,
                                    Entry originalEntry, Entry importEntry,
                                    String tag, Hashtable props) {
        try {
            if ( !tag.equals(WIKI_PROP_IMPORT)) {
                String include = getWikiInclude(wikiUtil, request,
                                     originalEntry, importEntry, tag, props);
                if (include != null) {
                    return include;
                }
            } else {
                tag = Misc.getProperty(props, ATTR_OUTPUT,
                                       OutputHandler.OUTPUT_HTML.getId());
            }

            OutputHandler handler = getRepository().getOutputHandler(tag);
            if (handler == null) {
                return null;
            }

            Request myRequest =
                new Request(request,
                            getRepository().URL_ENTRY_SHOW.toString()) {
                public void putExtraProperty(Object key, Object value) {
                    request.putExtraProperty(key, value);
                }
                public Object getExtraProperty(Object key) {
                    return request.getExtraProperty(key);
                }
            };

            for (Enumeration keys = props.keys(); keys.hasMoreElements(); ) {
                Object key = keys.nextElement();
                myRequest.put(key, props.get(key));
            }

            OutputType outputType = handler.findOutputType(tag);
            myRequest.put(ARG_ENTRYID, importEntry.getId());
            myRequest.put(ARG_OUTPUT, outputType.getId());
            myRequest.put(ARG_EMBEDDED, "true");

            Result result = getEntryManager().processEntryShow(myRequest,
                                importEntry);
            String content = new String(result.getContent());
            String title = Misc.getProperty(props, ATTR_TITLE,
                                            result.getTitle());

            boolean inBlock = Misc.getProperty(props, ATTR_SHOWTOGGLE,
                                  Misc.getProperty(props, ATTR_SHOWHIDE,
                                      false));
            if (inBlock && (title != null)) {
                boolean open = Misc.getProperty(props, ATTR_OPEN, true);

                return HtmlUtils.makeShowHideBlock(title, content, open,
                        HtmlUtils.cssClass(CSS_CLASS_HEADING_2), "",
                        iconUrl("ramadda.icon.togglearrowdown"),
                        iconUrl("ramadda.icon.togglearrowright"));
            }

            return content;
        } catch (Exception exc) {
            throw new RuntimeException(exc);
        }
    }


    /**
     * Find the wiki entry for the request
     *
     * @param request The request
     * @param wikiUtil The wiki util
     * @param name     the name
     * @param parent   the parent
     *
     * @return the Entry
     *
     * @throws Exception problem retreiving Entry
     */
    public Entry findWikiEntry(Request request, WikiUtil wikiUtil,
                               String name, Entry parent)
            throws Exception {
        Entry theEntry = null;
        if ((parent != null  /* top group */
                ) && parent.isGroup()) {
            String pattern = getPattern(name);
            for (Entry child :
                    getEntryManager().getChildren(request, (Entry) parent)) {
                if (entryMatches(child, pattern, name)) {
                    return child;
                }
            }
        }
        theEntry = getEntryManager().getEntry(request, name);
        if (theEntry != null) {
            return theEntry;
        }

        return theEntry;
    }


    /**
     * Check to see if an entry matches the pattern or name
     *
     * @param child   the child
     * @param pattern the pattern
     * @param name    the name
     *
     * @return true if a match
     */
    private boolean entryMatches(Entry child, String pattern, String name) {
        String entryName = child.getName().trim().toLowerCase();
        if (pattern != null) {
            if (entryName.matches(pattern)) {
                return true;
            }
            String path = child.getResource().getPath();
            if (path != null) {
                path = path.toLowerCase();
                if (path.matches(pattern)) {
                    return true;
                }
            }
        } else if (name.startsWith("type:")) {
            if (child.getTypeHandler().isType(
                    name.substring("type:".length()))) {
                return true;
            }
        } else if (entryName.equalsIgnoreCase(name)) {
            return true;
        }

        return false;

    }

    /**
     * Make a pattern from the name
     *
     * @param name  the name
     *
     * @return the regex pattern
     */
    private String getPattern(String name) {
        return ((name.indexOf("*") >= 0)
                ? StringUtil.wildcardToRegexp(name)
                : null);
    }

    /**
     *
     * Make the wiki edit bar
     *
     * @param request The request
     * @param entry   the Entry
     * @param textAreaId  the textAreaId
     *
     * @return  the edit bar
     *
     * @throws Exception problems
     */
    public String makeWikiEditBar(Request request, Entry entry,
                                  String textAreaId)
            throws Exception {




        StringBuffer buttons = new StringBuffer();
        buttons.append(addWikiEditButton(textAreaId, "button_bold.png",
                                         "Bold text", "\\'\\'\\'",
                                         "\\'\\'\\'", "Bold text",
                                         "mw-editbutton-bold"));
        buttons.append(addWikiEditButton(textAreaId, "button_italic.png",
                                         "Italic text", "\\'\\'", "\\'\\'",
                                         "Italic text",
                                         "mw-editbutton-italic"));
        buttons.append(addWikiEditButton(textAreaId, "button_link.png",
                                         "Internal link", "[[", "]]",
                                         "Link title", "mw-editbutton-link"));
        buttons.append(
            addWikiEditButton(
                textAreaId, "button_extlink.png",
                "External link (remember http:// prefix)", "[", "]",
                "http://www.example.com link title",
                "mw-editbutton-extlink"));
        buttons.append(addWikiEditButton(textAreaId, "button_headline.png",
                                         "Level 2 headline", "\\n== ",
                                         " ==\\n", "Headline text",
                                         "mw-editbutton-headline"));
        buttons.append(addWikiEditButton(textAreaId, "button_linebreak.png",
                                         "Line break", "<br>", "", "",
                                         "mw-editbutton-headline"));
        buttons.append(addWikiEditButton(textAreaId, "button_strike.png",
                                         "Strike Through", "<s>", "</s>",
                                         "Strike-through text",
                                         "mw-editbutton-headline"));
        buttons.append(addWikiEditButton(textAreaId,
                                         "button_upper_letter.png",
                                         "Super Script", "<sup>", "</sup>",
                                         "Super script text",
                                         "mw-editbutton-headline"));
        buttons.append(addWikiEditButton(textAreaId,
                                         "button_lower_letter.png",
                                         "Sub Script", "<sub>", "</sub>",
                                         "Subscript script text",
                                         "mw-editbutton-headline"));
        buttons.append(addWikiEditButton(textAreaId, "button_small.png",
                                         "Small text", "<small>", "</small>",
                                         "Small text",
                                         "mw-editbutton-headline"));
        buttons.append(addWikiEditButton(textAreaId, "button_blockquote.png",
                                         "Insert block quote",
                                         "<blockquote>", "</blockquote>",
                                         "Quoted text",
                                         "mw-editbutton-headline"));
        //        buttons.append(addWikiEditButton(textAreaId,"button_image.png","Embedded file","[[File:","]]","Example.jpg","mw-editbutton-image"));
        //        buttons.append(addWikiEditButton(textAreaId,"button_media.png","File link","[[Media:","]]","Example.ogg","mw-editbutton-media"));
        //        buttons.append(addWikiEditButton(textAreaId,"button_nowiki.png","Ignore wiki formatting","\\x3cnowiki\\x3e","\\x3c/nowiki\\x3e","Insert non-formatted text here","mw-editbutton-nowiki"));
        //        buttons.append(addWikiEditButton(textAreaId,"button_sig.png","Your signature with timestamp","--~~~~","","","mw-editbutton-signature"));
        buttons.append(addWikiEditButton(textAreaId, "button_hr.png",
                                         "Horizontal line (use sparingly)",
                                         "\\n----\\n", "", "",
                                         "mw-editbutton-hr"));


        StringBuffer importMenu = new StringBuffer();
        String       inset      = "&nbsp;&nbsp;";
        int          cnt        = 0;
        importMenu.append("<table><tr valign=top><td valign=top>\n");
        for (int i = 0; i < WIKIPROPS.length; i++) {
            String prop = WIKIPROPS[i];

            if (prop.startsWith(WIKI_PROP_GROUP)) {
                cnt++;
                if (cnt > 1) {
                    importMenu.append(
                        "</td><td>&nbsp;</td><td valign=top>\n");
                    cnt = 1;
                }
                String group = prop.substring(WIKI_PROP_GROUP.length());
                importMenu.append(HtmlUtils.b(group));
                importMenu.append(HtmlUtils.br());
                importMenu.append("\n");

                continue;
            }
            String textToInsert = prop;
            int    colonIdx     = prop.indexOf(PROP_DELIM);
            if (colonIdx >= 0) {
                prop = prop.substring(0, colonIdx);
                textToInsert = prop + " "
                               + textToInsert.substring(colonIdx + 1);
            }


            String js2 = "javascript:insertTags("
                         + HtmlUtils.squote(textAreaId) + ","
                         + HtmlUtils.squote("{{" + textToInsert + " ") + ","
                         + HtmlUtils.squote("}}") + ","
                         + HtmlUtils.squote("") + ");";
            importMenu.append(inset);
            importMenu.append(HtmlUtils.href(js2, prop));
            importMenu.append(HtmlUtils.br());
            importMenu.append("\n");
        }
        importMenu.append("</td></tr></table>\n");
        List<Link> links = getRepository().getOutputLinks(request,
                               new OutputHandler.State(entry));



        for (Link link : links) {
            if (link.getOutputType() == null) {
                continue;
            }

            String prop = link.getOutputType().getId();
            String js = "javascript:insertTags("
                        + HtmlUtils.squote(textAreaId) + ","
                        + HtmlUtils.squote("{{") + ","
                        + HtmlUtils.squote("}}") + ","
                        + HtmlUtils.squote(prop) + ");";
        }


        StringBuffer importOutputMenu = new StringBuffer();
        /*
                List<OutputType> allTypes = getRepository().getOutputTypes();
                //        importMenu.append("<hr>");
                for(OutputType type: allTypes) {
                    String prop = type.getId();
                    String js = "javascript:insertTags(" + HtmlUtils.squote(textAreaId)+"," +
                        HtmlUtils.squote("{{import ") +","+
                        HtmlUtils.squote(" " + type.getId()+" }}") +","+
                        HtmlUtils.squote("entryid")+");";
                    importOutputMenu.append(HtmlUtils.href(js,type.getLabel()));
                    importOutputMenu.append(HtmlUtils.br());
                }
        */


        String importMenuLabel = msg("Add property");
        //            HtmlUtils.img(iconUrl("/icons/wiki/button_import.png"),
        //                         "Import Entry Property");
        String importButton = getPageHandler().makePopupLink(importMenuLabel,
                                  HtmlUtils.hbox(importMenu.toString(),
                                      importOutputMenu.toString()));
        String addEntry = OutputHandler.getSelect(request, textAreaId,
                              "Add entry id", true, "entryid", entry, false);

        String addLink = OutputHandler.getSelect(request, textAreaId,
                             "Add entry link", true, "wikilink", entry,
                             false);

        buttons.append(HtmlUtils.space(2));
        buttons.append(importButton);
        buttons.append(HtmlUtils.space(2));
        buttons.append(addEntry);
        buttons.append(HtmlUtils.space(2));
        buttons.append(addLink);

        return buttons.toString();
    }


    /**
     * Add a wiki edit button
     *
     *
     * @param textAreaId  the TextArea
     * @param icon        the icon
     * @param label       the label
     * @param prefix      the prefix
     * @param suffix      the suffix
     * @param example     example string
     * @param huh         huh?
     *
     * @return  the html for the button
     */
    private String addWikiEditButton(String textAreaId, String icon,
                                     String label, String prefix,
                                     String suffix, String example,
                                     String huh) {
        String prop = prefix + example + suffix;
        String js;
        if (suffix.length() == 0) {
            js = "javascript:insertText(" + HtmlUtils.squote(textAreaId)
                 + "," + HtmlUtils.squote(prop) + ");";
        } else {
            js = "javascript:insertTags(" + HtmlUtils.squote(textAreaId)
                 + "," + HtmlUtils.squote(prefix) + ","
                 + HtmlUtils.squote(suffix) + "," + HtmlUtils.squote(example)
                 + ");";
        }

        return HtmlUtils.href(js,
                              HtmlUtils.img(iconUrl("/icons/wiki/" + icon),
                                            label));

    }




    /**
     * Get a wiki link
     *
     * @param wikiUtil The wiki util
     * @param name     the name
     * @param label    the label
     *
     * @return  the wiki link
     */
    public String getWikiLink(WikiUtil wikiUtil, String name, String label) {
        try {
            Entry   entry   = (Entry) wikiUtil.getProperty(ATTR_ENTRY);
            Request request = (Request) wikiUtil.getProperty(ATTR_REQUEST);
            Entry   parent  = entry.getParentEntry();


            name = name.trim();
            if (name.startsWith("Category:")) {
                String category = name.substring("Category:".length());
                String url =
                    request.url(
                        getRepository().getSearchManager().URL_ENTRY_SEARCH,
                        ARG_METADATA_TYPE + ".wikicategory", "wikicategory",
                        ARG_METADATA_ATTR1 + ".wikicategory", category);
                wikiUtil.addCategoryLink(HtmlUtils.href(url, category));
                List categories =
                    (List) wikiUtil.getProperty("wikicategories");
                if (categories == null) {
                    wikiUtil.putProperty("wikicategories",
                                         categories = new ArrayList());
                }
                categories.add(category);

                return "";
            }

            Entry theEntry = null;
            theEntry = getEntryManager().findEntryWithName(request,
                    (Entry) entry, name);

            //If the entry is a group first check its children.
            if (theEntry == null) {
                if (entry.isGroup()) {
                    theEntry = findWikiEntry(request, wikiUtil, name,
                                             (Entry) entry);
                }
            }
            if (theEntry == null) {
                theEntry = findWikiEntry(request, wikiUtil, name, parent);
            }

            if (theEntry != null) {
                addWikiLink(wikiUtil, theEntry);
                if (label.trim().length() == 0) {
                    label = getEntryDisplayName(theEntry);
                }
                if (theEntry.getType().equals(TYPE_WIKIPAGE)) {
                    String url =
                        request.entryUrl(getRepository().URL_ENTRY_SHOW,
                                         theEntry, ARG_OUTPUT, OUTPUT_WIKI);

                    return getEntryManager().getTooltipLink(request,
                            theEntry, label, url);

                } else {
                    return getEntryManager().getTooltipLink(request,
                            theEntry, label, null);
                }
            }


            //If its an anonymous user then jusst show the label or the name
            if (request.isAnonymous()) {
                String extra = HtmlUtils.cssClass("wiki-link-noexist");
                if ((label != null) && (label.length() > 0)) {
                    return HtmlUtils.span(label, extra);
                }

                return HtmlUtils.span(name, extra);
            }

            String url = request.url(getRepository().URL_ENTRY_FORM,
                                     ARG_NAME, name, ARG_GROUP,
                                     (entry.isGroup()
                                      ? entry.getId()
                                      : parent.getId()), ARG_TYPE,
                                          TYPE_WIKIPAGE);

            return HtmlUtils.href(url, name,
                                  HtmlUtils.cssClass("wiki-link-noexist"));
        } catch (Exception exc) {
            throw new RuntimeException(exc);
        }
    }



    /**
     * Wikify the entry
     *
     * @param request The request
     * @param entry   the Entry
     * @param wikiContent  the content
     *
     * @return wikified content
     *
     * @throws Exception  problem wikifying
     */
    public String wikifyEntry(Request request, Entry entry,
                              String wikiContent)
            throws Exception {
        return wikifyEntry(request, entry, wikiContent, true, null, null);
    }


    /**
     * Wikify the entry
     *
     * @param request The request
     * @param entry   the Entry
     * @param wikiContent  the content to wikify
     * @param wrapInDiv    true to wrap in a div tag
     * @param subGroups    the list of subgroups to include
     * @param subEntries   the list of subentries to include
     *
     * @return the wikified Entry
     *
     * @throws Exception  problem wikifying
     */
    public String wikifyEntry(Request request, Entry entry,
                              String wikiContent, boolean wrapInDiv,
                              List<Entry> subGroups, List<Entry> subEntries)
            throws Exception {
        Request myRequest = request.cloneMe();
        WikiUtil wikiUtil = new WikiUtil(Misc.newHashtable(new Object[] {
                                ATTR_REQUEST,
                                myRequest, ATTR_ENTRY, entry }));

        wikiUtil.setMobile(request.isMobile());

        return wikifyEntry(request, entry, wikiUtil, wikiContent, wrapInDiv,
                           subGroups, subEntries);
    }


    /**
     * Wikify the entry
     *
     * @param request The request
     * @param entry   the Entry
     * @param wikiUtil The wiki util
     * @param wikiContent  the content to wikify
     * @param wrapInDiv    true to wrap in a div tag
     * @param subGroups    the list of subgroups to include
     * @param subEntries   the list of subentries to include
     *
     * @return the wikified Entry
     *
     * @throws Exception  problem wikifying
     */
    public String wikifyEntry(Request request, Entry entry,
                              WikiUtil wikiUtil, String wikiContent,
                              boolean wrapInDiv, List<Entry> subGroups,
                              List<Entry> subEntries)
            throws Exception {
        List children = new ArrayList();
        if (subGroups != null) {
            wikiUtil.putProperty(entry.getId() + "_subgroups", subGroups);
            children.addAll(subGroups);
        }

        if (subEntries != null) {
            wikiUtil.putProperty(entry.getId() + "_subentries", subEntries);
            children.addAll(subEntries);
        }

        //TODO: We need to keep track of what is getting called so we prevent
        //infinite loops
        String content = wikiUtil.wikify(wikiContent, this);
        if ( !wrapInDiv) {
            return content;
        }

        return HtmlUtils.div(content, HtmlUtils.cssClass("wikicontent"));
    }

    /**
     * Add a wiki link
     *
     * @param wikiUtil The wiki util
     * @param toEntry  the entry to add to
     */
    public void addWikiLink(WikiUtil wikiUtil, Entry toEntry) {
        if (toEntry == null) {
            return;
        }
        Hashtable links = (Hashtable) wikiUtil.getProperty("wikilinks");
        if (links == null) {
            wikiUtil.putProperty("wikilinks", links = new Hashtable());
        }
        links.put(toEntry, toEntry);
    }


    /**
     * Class for holding attributes
     */
    public static class Attr {

        /** Attribute name */
        String name;

        /** the default */
        String dflt;

        /** the label */
        String label;

        /**
         * Create an Attribute
         *
         * @param name  the name
         * @param dflt  the default
         * @param label the label
         */
        public Attr(String name, String dflt, String label) {
            this.name  = name;
            this.dflt  = dflt;
            this.label = label;
        }

        /**
         * Return a String version of this object
         *
         * @return a String version of this object
         */
        public String toString() {
            return name;
        }
    }

    /**
     * _more_
     *
     * @param request _more_
     * @param props _more_
     * @param originalEntry _more_
     * @param entry _more_
     *
     * @return _more_
     *
     * @throws Exception _more_
     */
    private String getDescription(Request request, Hashtable props,
                                  Entry originalEntry, Entry entry)
            throws Exception {
        String  content;
        boolean wikify = Misc.getProperty(props, ATTR_WIKIFY, false);
        if (entry.getTypeHandler().isType(TYPE_WIKIPAGE)) {
            content = entry.getValue(0, entry.getDescription());
            wikify  = true;
        } else {
            content = entry.getDescription();
        }

        if (wikify) {
            if ( !originalEntry.equals(entry)) {
                content = wikifyEntry(request, entry, content, false, null,
                                      null);
            } else {
                content = makeWikiUtil(request, false).wikify(content, null);
            }
        }

        return content;
    }



    /**
     * _more_
     *
     * @param request _more_
     * @param props _more_
     * @param originalEntry _more_
     * @param entry _more_
     *
     * @return _more_
     *
     * @throws Exception _more_
     */
    public String makeSimpleDisplay(Request request, Hashtable props,
                                    Entry originalEntry, Entry entry)
            throws Exception {

        String fromType = entry.getTypeHandler().getSimpleDisplay(request,
                              props, entry);
        if (fromType != null) {
            return fromType;
        }


        boolean sizeConstrained = Misc.getProperty(props, ATTR_CONSTRAINSIZE,
                                      false);;
        String  content = getDescription(request, props, originalEntry,
                                         entry);
        boolean haveText = Utils.stringDefined(content);
        String  imageUrl = null;

        if (entry.getResource().isImage()) {
            imageUrl =
                getRepository().getHtmlOutputHandler().getImageUrl(request,
                    entry);
        } else {
            List<String> urls = new ArrayList<String>();
            getMetadataManager().getThumbnailUrls(request, entry, urls);
            if (urls.size() > 0) {
                imageUrl = urls.get(0);
            }
        }


        if (imageUrl != null) {
            StringBuffer extra = new StringBuffer();
            String position = request.getString(ATTR_TEXTPOSITION, POS_LEFT);
            boolean layoutHorizontal = position.equals(POS_RIGHT)
                                       || position.equals(POS_LEFT);
            int imageWidth = -1;

            if (sizeConstrained) {
                imageWidth = Misc.getProperty(props, ATTR_WIDTH, 400);
                //Give some space to the text on the side
                if (haveText && layoutHorizontal) {
                    imageWidth -= 200;
                }
            }

            imageWidth = Misc.getProperty(props, ATTR_IMAGEWIDTH, imageWidth);

            if (imageWidth > 0) {
                extra.append(HtmlUtils.attr(HtmlUtils.ATTR_WIDTH,
                                            "" + imageWidth));
            }

            String alt = request.getString(ATTR_ALT,
                                           getEntryDisplayName(entry));
            String imageClass = request.getString("imageclass",
                                    (String) null);
            if (Utils.stringDefined(alt)) {
                extra.append(HtmlUtils.attr(ATTR_ALT, alt));
            }
            String image = HtmlUtils.img(imageUrl, "", extra.toString());
            if (request.get(WikiManager.ATTR_LINK, true)) {
                image = HtmlUtils.href(
                    request.entryUrl(getRepository().URL_ENTRY_SHOW, entry),
                    image);
                /*  Maybe add this later
                } else if (request.get(WikiManager.ATTR_LINKRESOURCE, false)) {
                    image =  HtmlUtils.href(
                        entry.getTypeHandler().getEntryResourceUrl(request, entry),
                        image);
                */
            }

            String extraDiv = "";
            if (haveText && sizeConstrained) {
                int height = Misc.getProperty(props, ATTR_HEIGHT, -1);
                if ((height > 0) && position.equals(POS_BOTTOM)) {
                    extraDiv =
                        HtmlUtils.style("overflow-y: hidden; max-height:"
                                        + (height - 100) + "px;");
                }
            }
            image = HtmlUtils.div(image,
                                  HtmlUtils.cssClass("entry-simple-image")
                                  + extraDiv);
            if ( !haveText) {
                return image;
            }

            String textClass = "entry-simple-text";
            if (position.equals(POS_NONE)) {
                content = image;
            } else if (position.equals(POS_BOTTOM)) {
                content = image
                          + HtmlUtils.div(content,
                                          HtmlUtils.cssClass(textClass));
            } else if (position.equals(POS_TOP)) {
                content =
                    HtmlUtils.div(content, HtmlUtils.cssClass(textClass))
                    + image;
            } else if (position.equals(POS_RIGHT)) {
                content =
                    HtmlUtils.table(
                        HtmlUtils.row(
                            HtmlUtils.col(image)
                            + HtmlUtils.col(
                                HtmlUtils.div(
                                    content,
                                    HtmlUtils.cssClass(
                                        textClass))), HtmlUtils.attr(
                                            HtmlUtils.ATTR_VALIGN,
                                            "top")), HtmlUtils.attr(
                                                HtmlUtils.ATTR_CELLPADDING,
                                                    "0"));
            } else if (position.equals(POS_LEFT)) {
                content =
                    HtmlUtils.table(
                        HtmlUtils.row(
                            HtmlUtils.col(
                                HtmlUtils.div(
                                    content,
                                    HtmlUtils.cssClass(
                                        textClass))) + HtmlUtils.col(
                                            image), HtmlUtils.attr(
                                            HtmlUtils.ATTR_VALIGN,
                                            "top")), HtmlUtils.attr(
                                                HtmlUtils.ATTR_CELLPADDING,
                                                    "0"));
            } else {
                content = "Unknown position:" + position;
            }
        }


        if (entry.getTypeHandler().isGroup()
                && entry.getTypeHandler().isType(TypeHandler.TYPE_GROUP)) {
            //Do we tack on the listing
            StringBuffer sb = new StringBuffer();
            List<Entry> children = getEntryManager().getChildren(request,
                                       entry);
            if (children.size() > 0) {
                String link = getHtmlOutputHandler().getEntriesList(request,
                                  sb, children, true, false, true);
                content = content + sb;
            }
        }
        content = HtmlUtils.div(content, HtmlUtils.cssClass("entry-simple"));

        return content;
    }

    /**
     * _more_
     *
     * @param request _more_
     * @param name _more_
     * @param url _more_
     * @param sb _more_
     * @param props _more_
     *
     * @throws Exception _more_
     */
    public void getEntryChart(Request request, String name, String url,
                              StringBuffer sb, Hashtable props)
            throws Exception {

        List<String> topProps     = new ArrayList<String>();

        String       displayDivId = HtmlUtils.getUniqueId("displaydiv");
        sb.append(HtmlUtils.div("", HtmlUtils.id(displayDivId)));

        if (request.getExtraProperty("initmap") == null) {
            sb.append(getMapManager().getHtmlImports());
            request.putExtraProperty("initmap", "");
        }

        if (request.getExtraProperty("initchart") == null) {
            request.putExtraProperty("initchart", "");
            sb.append(HtmlUtils.comment("google chart imports"));
            sb.append(HtmlUtils.importJS("https://www.google.com/jsapi"));
            sb.append(
                HtmlUtils.script(
                    "google.load(\"visualization\", \"1\", {packages:['corechart','table','annotatedtimeline']});\n"));
            sb.append(HtmlUtils.importJS(fileUrl("/pointdata.js")));
            sb.append(HtmlUtils.comment("chart imports"));
            sb.append(HtmlUtils.importJS(fileUrl("/displaymanager.js")));
            sb.append(HtmlUtils.importJS(fileUrl("/display.js")));
            sb.append(HtmlUtils.importJS(fileUrl("/displayext.js")));
        }

        sb.append(HtmlUtils.comment("start chart"));
        List<String> propList = new ArrayList<String>();


        StringBuffer js       = new StringBuffer();


        for (String showArg : new String[] { ATTR_SHOWMAP, ATTR_SHOWMENU }) {
            topProps.add(showArg);
            topProps.add("" + Misc.getProperty(props, showArg, false));
        }

        if (props.get(ATTR_SHOWMENU) != null) {
            propList.add(ATTR_SHOWMENU);
            propList.add(Misc.getProperty(props, ATTR_SHOWMENU, "true"));
            props.remove(ATTR_SHOWMENU);
        }

        String colors = (String)props.get(ATTR_COLORS);
        if(colors!=null) {
            propList.add(ATTR_COLORS);
            propList.add(Json.list(StringUtil.split(colors,","),true));
            props.remove(ATTR_COLORS);
        }

        if (props.get(ATTR_SHOWTITLE) != null) {
            propList.add(ATTR_SHOWTITLE);
            propList.add(Misc.getProperty(props, ATTR_SHOWTITLE, "true"));
            topProps.add(ATTR_SHOWTITLE);
            topProps.add(Misc.getProperty(props, ATTR_SHOWTITLE, "true"));
            props.remove(ATTR_SHOWTITLE);
        }


        String title = Misc.getProperty(props, ATTR_TITLE, (String) null);
        if (title != null) {
            propList.add(ATTR_TITLE);
            propList.add(Json.quote(title));
        }
        topProps.add("layoutType");
        topProps.add(Json.quote(Misc.getProperty(props, "layoutType",
                "table")));
        props.remove("layoutType");
        topProps.add("layoutColumns");
        topProps.add(Misc.getProperty(props, "layoutColumns", "1"));
        props.remove("layoutColumns");

        //Always add the default map layer to the displaymanager properties so any new maps pick it up
        String defaultLayer = Misc.getProperty(props, "defaultMapLayer",
                                  getProperty("ramadda.map.defaultlayer",
                                      "google.terrain"));

        topProps.add("defaultMapLayer");
        topProps.add(Json.quote(defaultLayer));


        //If no json url then just add the displaymanager
        if (url == null) {
            js.append("var displayManager = getOrCreateDisplayManager("
                      + HtmlUtils.quote(displayDivId) + ","
                      + Json.map(topProps, false) + ",true);\n");
            sb.append(HtmlUtils.script(js.toString()));

            return;
        }




        String fields = Misc.getProperty(props, "fields", (String) null);
        if (fields != null) {
            List<String> toks = StringUtil.split(fields, ",", true, true);
            if (toks.size() > 0) {
                propList.add("fields");
                propList.add(Json.list(toks, true));
            }
            props.remove("fields");
        }

        boolean fixedLayout = Misc.getProperty(props, "layoutFixed", true);

        if (fixedLayout) {
            propList.add("layoutFixed");
            propList.add("true");
            String anotherDiv = HtmlUtils.getUniqueId("displaydiv");
            sb.append(HtmlUtils.div("", HtmlUtils.id(anotherDiv)));
            propList.add("divid");
            propList.add(Json.quote(anotherDiv));
        }
        props.remove("layoutFixed");


        for (String arg : new String[] {
            "eventSource", "name", "displayFilter", "chartMin", ARG_WIDTH,
            ARG_HEIGHT, ARG_FROMDATE, ARG_TODATE, "column", "row"
        }) {
            String value = Misc.getProperty(props, arg, (String) null);
            if (value != null) {
                propList.add(arg);
                propList.add(Json.quote(value));
            }
            props.remove(arg);
        }


        //Only add the default layer to the display if its been specified
        defaultLayer = Misc.getProperty(props, "defaultLayer", (String) null);
        if (defaultLayer != null) {
            propList.add("defaultMapLayer");
            propList.add(Json.quote(defaultLayer));
            props.remove("defaultLayer");
        }

        String displayType = Misc.getProperty(props, "type", "linechart");
        props.remove("type");

        for (Enumeration keys = props.keys(); keys.hasMoreElements(); ) {
            Object key = keys.nextElement();
            Object value = props.get(key);
            System.err.println (key +" =  " + value);
            propList.add(key.toString());
            propList.add(Json.quote(value.toString()));
        }


        js.append("var displayManager = getOrCreateDisplayManager("
                  + HtmlUtils.quote(displayDivId) + ","
                  + Json.map(topProps, false) + ");\n");

        propList.add("data");
        propList.add("new  PointData(" + HtmlUtils.quote(name)
                     + ",  null,null," + HtmlUtils.quote(url) + ")");
        js.append("displayManager.createDisplay("
                  + HtmlUtils.quote(displayType) + ","
                  + Json.map(propList, false) + ");\n");

        sb.append(HtmlUtils.script(js.toString()));
    }



}
