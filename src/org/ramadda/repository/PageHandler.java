/*
 * Copyright (c) 2008-2015 Geode Systems LLC
 * This Software is licensed under the Geode Systems RAMADDA License available in the source distribution in the file 
 * ramadda_license.txt. The above copyright notice shall be included in all copies or substantial portions of the Software.
 */

package org.ramadda.repository;


import org.ramadda.repository.auth.Permission;
import org.ramadda.repository.auth.User;

import org.ramadda.repository.map.MapInfo;
import org.ramadda.repository.map.MapLayer;
import org.ramadda.repository.metadata.ContentMetadataHandler;
import org.ramadda.repository.metadata.Metadata;
import org.ramadda.repository.output.CalendarOutputHandler;
import org.ramadda.repository.output.HtmlOutputHandler;
import org.ramadda.repository.output.OutputHandler;
import org.ramadda.repository.output.OutputType;
import org.ramadda.repository.output.PageStyle;
import org.ramadda.repository.type.TypeHandler;
import org.ramadda.util.CategoryBuffer;
import org.ramadda.util.HtmlTemplate;
import org.ramadda.util.HtmlUtils;
import org.ramadda.util.JQuery;
import org.ramadda.util.MapRegion;

import org.ramadda.util.Utils;

import ucar.unidata.ui.ImageUtils;
import ucar.unidata.util.DateUtil;
import ucar.unidata.util.IOUtil;
import ucar.unidata.util.Misc;
import ucar.unidata.util.StringUtil;
import ucar.unidata.util.TwoFacedObject;


import java.awt.Graphics;
import java.awt.Image;
import java.awt.image.BufferedImage;

import java.io.File;
import java.io.FileOutputStream;
import java.io.PrintWriter;

import java.net.URL;

import java.text.SimpleDateFormat;

import java.util.ArrayList;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.List;
import java.util.Properties;
import java.util.TimeZone;


/**
 * The main class.
 *
 */
public class PageHandler extends RepositoryManager {


    /** _more_ */
    public static final String DEFAULT_TEMPLATE = "aodnStyle";



    /** _more_ */
    public static final String DEFAULT_TIME_FORMAT = "yyyy-MM-dd HH:mm:ss z";

    /** _more_ */
    public static final String DEFAULT_TIME_SHORTFORMAT =
    //        "yyyy/MM/dd HH:mm z";
    "yyyy/MM/dd";

    /** _more_ */
    public static final String DEFAULT_TIME_THISYEARFORMAT =
        "yyyy/MM/dd HH:mm z";


    /** _more_ */
    public static final TimeZone TIMEZONE_UTC = TimeZone.getTimeZone("UTC");

    /** _more_ */
    public static final TimeZone TIMEZONE_GMT = TimeZone.getTimeZone("GMT");



    /** _more_ */
    public static final GregorianCalendar calendar =
        new GregorianCalendar(TIMEZONE_UTC);



    /** _more_ */
    public static final String REGISTER_MESSAGE =
        "<div class=\"ramadda-register\">Thank you for trying the <a href=\"http://geodesystems.com\">Geode Systems</a> RAMADDA repository. This is the shareware version of the software. Please register to support continued development. &nbsp; <a class=\"btn btn-primary\" role=\"button\" target=register href=\"https://geodesystems.com/repository/alias/ramadda_registration\">Register here</a></div>";


    /** _more_ */
    protected SimpleDateFormat sdf;

    /** _more_ */
    protected SimpleDateFormat displaySdf;

    /** _more_ */
    protected SimpleDateFormat thisYearSdf;


    /** _more_ */
    protected SimpleDateFormat dateSdf =
        RepositoryUtil.makeDateFormat("yyyy-MM-dd");

    /** _more_ */
    protected SimpleDateFormat timeSdf =
        RepositoryUtil.makeDateFormat("HH:mm:ss z");


    /** _more_ */
    private List<MapLayer> mapLayers = null;


    /** _more_ */
    private List<MapRegion> mapRegions = new ArrayList<MapRegion>();


    /** _more_ */
    private static final org.ramadda.util.HttpFormField dummyFieldToForceCompile =
        null;

    /** _more_ */
    public static final String PROP_LANGUAGE_DEFAULT =
        "ramadda.language.default";

    /** _more_ */
    public static final String PROP_ENTRY_TABLE_SHOW_CREATEDATE =
        "ramadda.entry.table.show.createdate";


    /** _more_ */
    public static final String MSG_PREFIX = "<msg ";

    /** _more_ */
    public static final String MSG_SUFFIX = " msg>";


    /** html template macro */
    public static final String MACRO_LINKS = "links";

    /** html template macro */
    public static final String MACRO_LOGO_URL = "logo.url";

    /** html template macro */
    public static final String MACRO_LOGO_IMAGE = "logo.image";

    /** html template macro */
    public static final String MACRO_SEARCH_URL = "search.url";

    /** html template macro */
    public static final String MACRO_ENTRY_HEADER = "entry.header";

    /** html template macro */
    public static final String MACRO_HEADER = "header";

    /** html template macro */
    public static final String MACRO_ENTRY_FOOTER = "entry.footer";

    /** html template macro */
    public static final String MACRO_ENTRY_BREADCRUMBS = "entry.breadcrumbs";

    /** html template macro */
    public static final String MACRO_HEADER_IMAGE = "header.image";

    /** html template macro */
    public static final String MACRO_HEADER_TITLE = "header.title";

    /** html template macro */
    public static final String MACRO_USERLINK = "userlinks";

    /** _more_ */
    public static final String MACRO_ALLLINKS = "alllinks";

    /** html template macro */
    public static final String MACRO_FAVORITES = "favorites";


    /** html template macro */
    public static final String MACRO_REPOSITORY_NAME = "repository_name";

    /** html template macro */
    public static final String MACRO_FOOTER = "footer";

    /** html template macro */
    public static final String MACRO_TITLE = "title";

    /** html template macro */
    public static final String MACRO_ROOT = "root";

    /** html template macro */
    public static final String MACRO_HEADFINAL = "headfinal";

    /** html template macro */
    public static final String MACRO_BOTTOM = "bottom";

    /** html template macro */
    public static final String MACRO_CONTENT = "content";


    /** _more_ */
    private List<HtmlTemplate> htmlTemplates;

    /** _more_ */
    private HtmlTemplate mobileTemplate;

    /** _more_ */
    private HtmlTemplate defaultTemplate;


    /** _more_ */
    private Properties phraseMap;


    /** _more_ */
    private Hashtable<String, Properties> languageMap = new Hashtable<String,
                                                            Properties>();

    /** _more_ */
    private List<TwoFacedObject> languages = new ArrayList<TwoFacedObject>();





    /** _more_ */
    private HashSet<String> seenMsg = new HashSet<String>();

    /**
     * Set this to true to print to a file the missing messages and this also
     *   adds a "NA:" to the missing labels.
     */
    private boolean debugMsg = false;

    /** _more_ */
    private PrintWriter allMsgOutput;

    /** _more_ */
    private PrintWriter missingMsgOutput;

    /**
     * _more_
     *
     * @param repository _more_
     */
    public PageHandler(Repository repository) {
        super(repository);
    }


    /**
     * _more_
     */
    public void initDateStuff() {
        sdf = RepositoryUtil.makeDateFormat(getProperty(PROP_DATEFORMAT,
                DEFAULT_TIME_FORMAT));
        defaultTimeZone = TimeZone.getDefault();
        TimeZone.setDefault(RepositoryUtil.TIMEZONE_DEFAULT);
    }

    /**
     * _more_
     */
    public void adminSettingsChanged() {
        super.adminSettingsChanged();
        phraseMap = null;
    }

    /**
     * _more_
     *
     *
     * @param request The request
     * @param result _more_
     *
     * @throws Exception _more_
     */
    public void decorateResult(Request request, Result result)
            throws Exception {

        if ( !request.get(ARG_DECORATE, true)) {
            return;
        }
        Repository   repository   = getRepository();
        Entry        currentEntry = getSessionManager().getLastEntry(request);
        String       template     = null;
        HtmlTemplate htmlTemplate;
        if (request.isMobile() && !request.defined(ARG_TEMPLATE)) {
            htmlTemplate = getMobileTemplate();
        } else {
            htmlTemplate = getTemplate(request, currentEntry);
        }
        template = htmlTemplate.getTemplate();

        String systemMessage = getRepository().getSystemMessage(request);


        String jsContent     = getTemplateJavascriptContent();

        List   allLinks      = new ArrayList();
        List   links         = (List) result.getProperty(PROP_NAVLINKS);
        String linksHtml     = HtmlUtils.space(1);

        if (links != null) {
            linksHtml = StringUtil.join(
                htmlTemplate.getTemplateProperty(
                    "ramadda.template.link.separator", ""), links);
            linksHtml =
                HtmlUtils.div(linksHtml,
                              HtmlUtils.cssClass("ramadda-header-links"));
            allLinks.addAll(links);
        }
        String entryHeader = (String) result.getProperty(PROP_ENTRY_HEADER);
        if (entryHeader == null) {
            entryHeader = "";
        }
        String entryFooter = (String) result.getProperty(PROP_ENTRY_FOOTER);
        if (entryFooter == null) {
            entryFooter = "";
        }


        String entryBreadcrumbs =
            (String) result.getProperty(PROP_ENTRY_BREADCRUMBS);
        if (entryBreadcrumbs == null) {
            entryBreadcrumbs = "";
        }

        String header = "";
        if (entryHeader.length() > 0) {
            header = entryHeader;
        }

        String favoritesWrapper = htmlTemplate.getTemplateProperty(
                                      "ramadda.template.favorites.wrapper",
                                      "${link}");
        String favoritesTemplate =
            htmlTemplate.getTemplateProperty(
                "ramadda.template.favorites",
                "<span class=\"linkslabel\">Favorites:</span>${entries}");
        String favoritesSeparator =
            htmlTemplate.getTemplateProperty(
                "ramadda.template.favorites.separator", "");

        List<FavoriteEntry> favoritesList =
            getUserManager().getFavorites(request, request.getUser());
        StringBuilder favorites = new StringBuilder();
        if (favoritesList.size() > 0) {
            List favoriteLinks = new ArrayList();
            int  favoriteCnt   = 0;
            for (FavoriteEntry favorite : favoritesList) {
                if (favoriteCnt++ > 100) {
                    break;
                }
                Entry entry = favorite.getEntry();
                EntryLink entryLink = getEntryManager().getAjaxLink(request,
                                          entry, entry.getLabel(), null,
                                          false, null, false);
                String link = favoritesWrapper.replace("${link}",
                                  entryLink.toString());
                favoriteLinks.add("<nobr>" + link + "</nobr>");
            }
            favorites.append(favoritesTemplate.replace("${entries}",
                    StringUtil.join(favoritesSeparator, favoriteLinks)));
        }

        List<Entry> cartEntries = getUserManager().getCart(request);
        if (cartEntries.size() > 0) {
            String cartTemplate =
                htmlTemplate.getTemplateProperty("ramadda.template.cart",
                    "<b>Cart:<b><br>${entries}");
            List cartLinks = new ArrayList();
            for (Entry entry : cartEntries) {
                EntryLink entryLink = getEntryManager().getAjaxLink(request,
                                          entry, entry.getLabel(), null,
                                          false);
                String link = favoritesWrapper.replace("${link}",
                                  entryLink.toString());
                cartLinks.add("<nobr>" + link + "<nobr>");
            }
            favorites.append(HtmlUtils.br());
            favorites.append(cartTemplate.replace("${entries}",
                    StringUtil.join(favoritesSeparator, cartLinks)));
        }

        String content = new String(result.getContent());
        if ((systemMessage != null) && (systemMessage.length() > 0)) {
            content = HtmlUtils.div(
                systemMessage,
                HtmlUtils.cssClass("ramadda-system-message")) + content;
        }

        if ( !getRepository().isRegistered()
                && getAdmin().getInstallationComplete()) {
            content = REGISTER_MESSAGE + content;
        }



        String head = (String) result.getProperty(PROP_HTML_HEAD);
        if (head == null) {
            head = (String) request.getExtraProperty(PROP_HTML_HEAD);
        }


        if (head == null) {
            head = "";
        }
        String logoImage = repository.getLogoImage(result);
        String logoUrl   = (String) result.getProperty(PROP_LOGO_URL);
        if ((logoUrl == null) || (logoUrl.trim().length() == 0)) {
            logoUrl = getProperty(PROP_LOGO_URL, "");
        }
        String pageTitle = (String) result.getProperty(PROP_REPOSITORY_NAME);
        if (pageTitle == null) {
            pageTitle = repository.getProperty(PROP_REPOSITORY_NAME,
                    "Repository");
        }

        for (PageDecorator pageDecorator :
                repository.getPluginManager().getPageDecorators()) {
            String tmpTemplate = pageDecorator.decoratePage(repository,
                                     request, template, currentEntry);
            if (tmpTemplate != null) {
                template = tmpTemplate;
            }
        }
        String html = template;

        boolean makePopup = htmlTemplate.getTemplateProperty(
                                "ramadda.template.userlink.popup", false);
        String userLinkTemplate;
        String separator;

        if (makePopup) {
            userLinkTemplate =
                "<div onClick=\"document.location=\'${url}\'\"  class=\"ramadda-user-link\">${label}</div>";
            separator = "";
        } else {
            userLinkTemplate = htmlTemplate.getTemplateProperty(
                "ramadda.template.link.wrapper", "");
            userLinkTemplate = htmlTemplate.getTemplateProperty(
                "ramadda.template.userlink.wrapper", userLinkTemplate);
            separator = htmlTemplate.getTemplateProperty(
                "ramadda.template.link.separator", "");
            separator = htmlTemplate.getTemplateProperty(
                "ramadda.template.userlink.separator", separator);
        }




        StringBuilder extra = new StringBuilder();
        String userLinks = getUserManager().getUserLinks(request,
                               userLinkTemplate, separator, extra, makePopup);

        if (makePopup
                && ((userLinks != null) && !userLinks.trim().isEmpty())) {
            String userImage =
                HtmlUtils.img(iconUrl(ICON_USERLINKS),
                              msg("Login, user settings, help"),
                              HtmlUtils.cssClass("ramadda-user-menu-image"));
            //        userLinks = makePopupLink(userImage, userLinks, "", true, true, bottom);
            userLinks =
                HtmlUtils.div(userLinks,
                              HtmlUtils.cssClass("ramadda-user-menu"));
            userLinks = extra
                        + makePopupLink(userImage, userLinks, false, true);

            allLinks.add(userLinks);
        }

        String allLinksHtml = StringUtil.join(
                                  htmlTemplate.getTemplateProperty(
                                      "ramadda.template.link.separator",
                                      ""), allLinks);
        allLinksHtml =
            HtmlUtils.div(allLinksHtml,
                          HtmlUtils.cssClass("ramadda-header-links"));


        StringBuilder bottom = new StringBuilder(result.getBottomHtml());

        String[]      macros = new String[] {
            MACRO_LOGO_URL, logoUrl, MACRO_LOGO_IMAGE, logoImage,
            MACRO_HEADER_IMAGE, iconUrl(ICON_HEADER), MACRO_HEADER_TITLE,
            pageTitle, MACRO_USERLINK, userLinks, MACRO_LINKS, linksHtml,
            "alllinks", allLinksHtml, MACRO_REPOSITORY_NAME,
            repository.getProperty(PROP_REPOSITORY_NAME, "Repository"),
            MACRO_FOOTER, repository.getProperty(PROP_HTML_FOOTER, BLANK),
            MACRO_TITLE, result.getTitle(), MACRO_BOTTOM, bottom.toString(),
            MACRO_SEARCH_URL, getSearchManager().getSearchUrl(request),
            MACRO_CONTENT, content + jsContent, MACRO_FAVORITES,
            favorites.toString(), MACRO_ENTRY_HEADER, entryHeader,
            MACRO_HEADER, header, MACRO_ENTRY_FOOTER, entryFooter,
            MACRO_ENTRY_BREADCRUMBS, entryBreadcrumbs, MACRO_HEADFINAL, head,
            MACRO_ROOT, repository.getUrlBase(),
        };


        long t1 = System.currentTimeMillis();
        //TODO: This is really inefficient. The template needs to be tokenized to find the macros
        //Not this way
        for (int i = 0; i < macros.length; i += 2) {
            html = html.replace("${" + macros[i] + "}", macros[i + 1]);
        }

        for (String property : htmlTemplate.getPropertyIds()) {
            html = html.replace("${" + property + "}",
                                getRepository().getProperty(property, ""));
        }

        //cleanup old macro
        html = StringUtil.replace(html, "${sublinks}", BLANK);

        long t2 = System.currentTimeMillis();
        html = translate(request, html);
        long t3 = System.currentTimeMillis();
        //        System.err.println ("time:" + (t2-t1) +" t2:" + (t3-t2));
        result.setContent(html.getBytes());


    }




    /** _more_ */
    private String templateJavascriptContent;

    /**
     * _more_
     *
     * @return _more_
     */
    public String getTemplateJavascriptContent() {
        if (templateJavascriptContent == null) {
            //TODO: add a property to not buttonize
            StringBuilder js = new StringBuilder();
            js.append(JQuery.buttonize(":submit"));
            js.append("\n");
            /*
            String btArgs = "{contentSelector: \"$(this).attr('tooltip')\",fill: '#efefef',cssStyles: {color: 'white', fontWeight: 'bold'},            trigger: ['mouseover', 'mouseclick'], xshrinkToFit: true,padding: 10, cornerRadius: 10,xspikeLength: 15,xspikeGirth: 5,positions: ['left', 'right', 'bottom']}";


            js.append("$('[tooltip]').bt(" + btArgs +");\n");
            js.append("\n");
            */

            String buttonizeJS = HtmlUtils.script(js.toString());
            //j-
            StringBuilder sb = new StringBuilder();
            sb.append(HtmlUtils.div("",
                                    HtmlUtils.id("ramadda-tooltipdiv")
                                    + HtmlUtils.cssClass("tooltip-outer")));
            sb.append(HtmlUtils.div("",
                                    HtmlUtils.id("ramadda-dialog")
                                    + HtmlUtils.cssClass("ramadda-dialog")));
            sb.append(
                HtmlUtils.div(
                    "",
                    HtmlUtils.id("ramadda-selectdiv")
                    + HtmlUtils.cssClass("ramadda-selectdiv")));
            sb.append(
                HtmlUtils.div(
                    "",
                    HtmlUtils.id("ramadda-floatdiv")
                    + HtmlUtils.cssClass("ramadda-floatdiv")));
            sb.append(buttonizeJS);
            //j+
            templateJavascriptContent = sb.toString();
        }

        return templateJavascriptContent;
    }




    /**
     * _more_
     *
     * @param template _more_
     * @param ignoreErrors _more_
     *
     * @return _more_
     */
    public String processTemplate(String template, boolean ignoreErrors) {
        List<String>  toks   = StringUtil.splitMacros(template);
        StringBuilder result = new StringBuilder();
        if (toks.size() > 0) {
            result.append(toks.get(0));
            for (int i = 1; i < toks.size(); i++) {
                if (2 * (i / 2) == i) {
                    result.append(toks.get(i));
                } else {
                    String prop = getRepository().getProperty(toks.get(i),
                                      (String) null);
                    if (prop == null) {
                        if (ignoreErrors) {
                            prop = "${" + toks.get(i) + "}";
                        } else {
                            throw new IllegalArgumentException(
                                "Could not find property:" + toks.get(i)
                                + ":");
                        }
                    }
                    if (prop.startsWith("bsf:")) {
                        prop = new String(
                            RepositoryUtil.decodeBase64(prop.substring(4)));
                    }
                    result.append(prop);
                }
            }
        }

        return result.toString();
    }




    /**
     * _more_
     *
     * @param request The request
     * @param s _more_
     *
     * @return _more_
     */
    public String translate(Request request, String s) {
        String     language = request.getLanguage();
        Properties tmpMap;
        Properties map =
            (Properties) languageMap.get(getProperty(PROP_LANGUAGE_DEFAULT,
                "default"));
        if (map == null) {
            map = new Properties();
        }
        tmpMap = (Properties) languageMap.get(getProperty(PROP_LANGUAGE,
                BLANK));
        if (tmpMap != null) {
            map.putAll(tmpMap);
        }
        tmpMap = (Properties) languageMap.get(language);

        if (tmpMap != null) {
            map.putAll(tmpMap);
        }

        Properties tmpPhraseMap = phraseMap;
        if (tmpPhraseMap == null) {
            String phrases = getProperty(PROP_ADMIN_PHRASES, (String) null);
            if (phrases != null) {
                Object[] result = parsePhrases("", phrases);
                tmpPhraseMap = (Properties) result[2];
                phraseMap    = tmpPhraseMap;
            }
        }

        if (tmpPhraseMap != null) {
            map.putAll(tmpPhraseMap);
        }


        StringBuilder stripped     = new StringBuilder();
        int           prefixLength = MSG_PREFIX.length();
        int           suffixLength = MSG_PREFIX.length();
        //        System.out.println(s);
        while (s.length() > 0) {
            String tmp  = s;
            int    idx1 = s.indexOf(MSG_PREFIX);
            if (idx1 < 0) {
                stripped.append(s);

                break;
            }
            String text = s.substring(0, idx1);
            if (text.length() > 0) {
                stripped.append(text);
            }
            s = s.substring(idx1 + 1);

            int idx2 = s.indexOf(MSG_SUFFIX);
            if (idx2 < 0) {
                //Should never happen
                throw new IllegalArgumentException(
                    "No closing message suffix:" + s);
            }
            String key   = s.substring(prefixLength - 1, idx2);
            String value = null;
            if (map != null) {
                value = (String) map.get(key);
            }
            if (debugMsg) {
                try {
                    if (allMsgOutput == null) {
                        allMsgOutput = new PrintWriter(
                            new FileOutputStream("allmessages.pack"));
                        missingMsgOutput = new PrintWriter(
                            new FileOutputStream("missingmessages.pack"));
                    }
                    if ( !seenMsg.contains(key)) {
                        allMsgOutput.println(key + "=");
                        allMsgOutput.flush();
                        System.err.println(key);
                        if (value == null) {
                            missingMsgOutput.println(key + "=");
                            missingMsgOutput.flush();
                        }
                        seenMsg.add(key);
                    }
                } catch (Exception exc) {
                    throw new RuntimeException(exc);
                }
            }


            if (value == null) {
                value = key;
                if (debugMsg) {
                    value = "NA:" + key;
                }
            }
            stripped.append(value);
            s = s.substring(idx2 + suffixLength);
        }

        return stripped.toString();
    }

    /**
     * _more_
     *
     *
     * @param file _more_
     * @param content _more_
     *
     * @return _more_
     */
    private Object[] parsePhrases(String file, String content) {
        List<String> lines   = StringUtil.split(content, "\n", true, true);
        Properties   phrases = new Properties();
        String       type    =
            IOUtil.stripExtension(IOUtil.getFileTail(file));
        String       name    = type;
        for (String line : lines) {
            if (line.startsWith("#")) {
                continue;
            }
            List<String> toks = StringUtil.split(line, "=", true, true);
            if (toks.size() == 0) {
                continue;
            }
            String key = toks.get(0).trim();
            String value;
            if (toks.size() == 1) {
                if ( !debugMsg) {
                    continue;
                }
                value = "UNDEF:" + key;
            } else {
                value = toks.get(1).trim();
            }
            if (key.equals("language.id")) {
                type = value;
            } else if (key.equals("language.name")) {
                name = value;
            } else {
                if (value.length() == 0) {
                    if (debugMsg) {
                        value = "UNDEF:" + value;
                    } else {
                        continue;
                    }
                }


                phrases.put(key, value);
            }
        }

        return new Object[] { type, name, phrases };
    }


    /**
     * _more_
     *
     * @return _more_
     */
    public List<TwoFacedObject> getLanguages() {
        return languages;
    }




    /**
     * _more_
     *
     * @param request The request
     *
     * @return _more_
     */
    public HtmlTemplate getHtmlTemplate(Request request) {
        return null;
    }

    /**
     * _more_
     *
     * @return _more_
     */
    public HtmlTemplate getMobileTemplate() {
        if(!getRepository().cacheResources()) {
            mobileTemplate = null;
        }
        if (mobileTemplate == null) {
            for (HtmlTemplate htmlTemplate : getTemplates()) {
                if (htmlTemplate.getId().equals("mobile")) {
                    mobileTemplate = htmlTemplate;

                    break;
                }
            }
        }

        return mobileTemplate;
    }


    /**
     * _more_
     *
     * @return _more_
     */
    public List<HtmlTemplate> getTemplates() {
        List<HtmlTemplate> theTemplates = htmlTemplates;
        if (theTemplates == null) {
            //            System.err.println ("Loading templates");
            String imports = "";
            try {
                imports = getStorageManager().readSystemResource(
                    "/org/ramadda/repository/resources/web/imports.html");
            } catch (Exception exc) {
                throw new RuntimeException(exc);
            }

            imports = imports.replace("${root}",
                                      getRepository().getUrlBase());
            imports = imports.replace("${htdocs_version}",
                                      RepositoryUtil.HTDOCS_VERSION);

            theTemplates = new ArrayList<HtmlTemplate>();

            String defaultId = getProperty(PROP_HTML_TEMPLATE_DEFAULT,
                                           DEFAULT_TEMPLATE);

            List<String> templatePaths =
                new ArrayList<String>(
                    getRepository().getPluginManager().getTemplateFiles());
            for (String path :
                    StringUtil.split(getProperty(PROP_HTML_TEMPLATES,
                        "%resourcedir%/template.html"), ";", true, true)) {
                path = getStorageManager().localizePath(path);
                templatePaths.add(path);
            }
            for (String path : templatePaths) {
                try {
                    //Skip resources called template.html that might be for other things
                    if (IOUtil.getFileTail(path).equals("template.html")) {
                        continue;
                    }
                    String resource =
                        getStorageManager().readSystemResource(path);
                    try {
                        resource = processTemplate(resource);
                    } catch (Exception exc) {
                        getLogManager().logError(
                            "failed to process template:" + path, exc);

                        continue;
                    }
                    String[] changes = { "userlink", MACRO_USERLINK,
                                         "html.imports", "imports", };
                    for (int i = 0; i < changes.length; i += 2) {
                        resource = resource.replace("${" + changes[i] + "}",
                                "${" + changes[i + 1] + "}");
                    }

                    resource = resource.replace("${imports}", imports);
                    HtmlTemplate template = new HtmlTemplate(getRepository(),
                                                path, resource);
                    //Check if we got some other ...template.html file from a plugin
                    if (template.getId() == null) {
                        System.err.println("template: no id in " + path);

                        continue;
                    }
                    theTemplates.add(template);

                    if (defaultTemplate == null) {
                        if (defaultId == null) {
                            defaultTemplate = template;
                        } else {
                            if (Misc.equals(defaultId, template.getId())) {
                                defaultTemplate = template;
                            }
                        }
                    }
                } catch (Exception exc) {
                    getLogManager().logError("loading template" + path, exc);
                    //noop
                }
            }
            if (getRepository().cacheResources()) {
                htmlTemplates = theTemplates;
            }
        }

        return theTemplates;
    }


    /**
     * _more_
     *
     * @param html _more_
     *
     * @return _more_
     *
     * @throws Exception _more_
     */
    public String processTemplate(String html) throws Exception {
        StringBuilder template = new StringBuilder();
        while (true) {
            int idx1 = html.indexOf("<include");
            if (idx1 < 0) {
                template.append(html);

                break;
            }
            template.append(html.substring(0, idx1));
            html = html.substring(idx1);
            idx1 = html.indexOf(">") + 1;
            String include = html.substring(0, idx1);
            include = include.substring("<include".length());
            include = include.replace(">", "");
            Hashtable props = StringUtil.parseHtmlProperties(include);
            String    url   = (String) props.get("href");
            if (url != null) {
                String includedContent =
                    getStorageManager().readSystemResource(new URL(url));
                //                String includedContent =  IOUtil.readContents(url, Repository.class);
                template.append(includedContent);
            }
            html = html.substring(idx1);
        }
        html = template.toString();
        if (html.indexOf("${imports}") < 0) {
            html = html.replace("<head>", "<head>\n${imports}");
        }
        if (html.indexOf("${headfinal}") < 0) {
            html = html.replace("</head>", "${headfinal}\n</head>");
        }

        return html;
    }



    /** _more_ */
    private HashSet<String> seenPack = new HashSet<String>();

    /**
     * _more_
     *
     * @throws Exception _more_
     */
    protected void loadLanguagePacks() throws Exception {
        //        getLogManager().logInfoAndPrint("RAMADDA: loadLanguagePacks");
        List sourcePaths =
            Misc.newList(
                getStorageManager().getSystemResourcePath() + "/languages",
                getStorageManager().getPluginsDir().toString());
        for (int i = 0; i < sourcePaths.size(); i++) {
            String       dir     = (String) sourcePaths.get(i);
            List<String> listing = getRepository().getListing(dir,
                                       getClass());
            for (String path : listing) {
                if ( !path.endsWith(".pack")) {
                    if (i == 0) {
                        getLogManager().logInfoAndPrint(
                            "RAMADDA: not ends with .pack:" + path);
                    }

                    continue;
                }
                if (seenPack.contains(path)) {
                    continue;
                }
                seenPack.add(path);
                String content =
                    getStorageManager().readUncheckedSystemResource(path,
                        (String) null);
                if (content == null) {
                    getLogManager().logInfoAndPrint(
                        "RAMADDA: could not read:" + path);

                    continue;
                }
                Object[]   result     = parsePhrases(path, content);
                String     type       = (String) result[0];
                String     name       = (String) result[1];
                Properties properties = (Properties) result[2];
                if (type != null) {
                    if (name == null) {
                        name = type;
                    }
                    languages.add(new TwoFacedObject(name, type));
                    languageMap.put(type, properties);
                } else {
                    getLogManager().logError("No _type_ found in: " + path);
                }
            }
        }
    }

    /**
     * _more_
     *
     * @return _more_
     */
    public List<MapRegion> getMapRegions() {
        return getMapRegions(null);
    }

    /**
     * _more_
     *
     * @param group _more_
     *
     * @return _more_
     */
    public List<MapRegion> getMapRegions(String group) {
        if (group == null) {
            return mapRegions;
        }
        List<MapRegion> regions = new ArrayList<MapRegion>();
        for (MapRegion region : mapRegions) {
            if (region.isGroup(group)) {
                regions.add(region);
            }
        }

        return regions;
    }

    /**
     * _more_
     *
     * @param request _more_
     * @param inputArgBase _more_
     *
     * @return _more_
     */
    public String getMapRegionSelector(Request request, String inputArgBase) {
        StringBuilder sb = new StringBuilder();

        return sb.toString();
    }


    /**
     * _more_
     *
     * @throws Exception _more_
     */
    protected void loadMapRegions() throws Exception {
        List<String> mapRegionFiles = new ArrayList<String>();
        List<String> allFiles       = getPluginManager().getAllFiles();
        for (String f : allFiles) {
            if (f.endsWith("regions.csv")) {
                mapRegionFiles.add(f);
            }
        }

        String dir = getStorageManager().getSystemResourcePath() + "/geo";
        List<String> listing = getRepository().getListing(dir, getClass());
        for (String f : listing) {
            if (f.endsWith("regions.csv")) {
                mapRegionFiles.add(f);
            }
        }

        for (String path : mapRegionFiles) {
            String contents =
                getStorageManager().readUncheckedSystemResource(path,
                    (String) null);
            if (contents == null) {
                getLogManager().logInfoAndPrint("RAMADDA: could not read:"
                        + path);

                continue;
            }
            //Name,ID,Group,North,West,South,East
            //Group
            List<String> lines = StringUtil.split(contents, "\n", true, true);
            lines.remove(0);
            String group = lines.get(0);
            lines.remove(0);
            for (String line : lines) {
                List<String> toks = StringUtil.split(line, ",");
                if (toks.size() != 6) {
                    throw new IllegalArgumentException("Bad map region line:"
                            + line + "\nFile:" + path);
                }


                mapRegions.add(
                    new MapRegion(
                        toks.get(1), toks.get(0), group,
                        Utils.decodeLatLon(toks.get(2)),
                        Utils.decodeLatLon(toks.get(3)),
                        Utils.decodeLatLon(toks.get(4)),
                        Utils.decodeLatLon(toks.get(5))));
            }

        }
    }



    /**
     * _more_
     *
     * @param request The request
     * @param name _more_
     * @param dflt _more_
     *
     * @return _more_
     */
    public String getTemplateProperty(Request request, String name,
                                      String dflt) {
        return getTemplate(request).getTemplateProperty(name, dflt);
    }


    /**
     * _more_
     *
     * @return _more_
     */
    public List<TwoFacedObject> getTemplateSelectList() {
        List<TwoFacedObject> tfos = new ArrayList<TwoFacedObject>();
        tfos.add(new TwoFacedObject("-default-", ""));
        for (HtmlTemplate template : getTemplates()) {
            tfos.add(new TwoFacedObject(template.getName(),
                                        template.getId()));
        }

        return tfos;

    }



    /**
     * _more_
     *
     * @param request _more_
     *
     * @return _more_
     */
    public HtmlTemplate getTemplate(Request request) {
        Entry currentEntry = null;
        if (request != null) {
            try {
                currentEntry = getSessionManager().getLastEntry(request);
            } catch (Exception exc) {
                throw new RuntimeException(exc);
            }
        }

        return getTemplate(request, currentEntry);
    }


    /**
     * Find the html template for the given request
     *
     * @param request The request
     * @param entry _more_
     *
     * @return _more_
     */
    //    static int tcnt = 0;

    public HtmlTemplate getTemplate(Request request, Entry entry) {
        if (request.isMobile()) {
            return getMobileTemplate();
        }


        List<HtmlTemplate> theTemplates = getTemplates();

        /*
          code to run through all of the templates
        if(request.template == null) {
            request.template =  theTemplates.get(tcnt++);
            return request.template;
        }
        if(true)return request.template;
        */


        if ((request == null) && (defaultTemplate != null)) {
            return defaultTemplate;
        }

        String templateId = request.getHtmlTemplateId();
        if (((templateId == null) || (templateId.length() == 0))
                && (entry != null)) {
            try {
                List<Metadata> metadataList =
                    getMetadataManager().findMetadata(request, entry,
                        ContentMetadataHandler.TYPE_TEMPLATE, true);
                if (metadataList != null) {
                    for (Metadata metadata : metadataList) {
                        templateId = metadata.getAttr1();
                        request.put(ARG_TEMPLATE, templateId);

                        break;
                    }
                }
            } catch (Exception exc) {
                throw new RuntimeException(exc);
            }
        }


        User user = request.getUser();

        if ((templateId == null) && user.getAnonymous()) {
            templateId = user.getTemplate();
        }

        if (templateId != null) {
            for (HtmlTemplate template : theTemplates) {
                if (Misc.equals(template.getId(), templateId)) {
                    return template;
                }
            }
        }

        if (user.getAnonymous()) {
            if (defaultTemplate != null) {
                return defaultTemplate;
            }

            return theTemplates.get(0);
        }

        for (HtmlTemplate template : theTemplates) {
            if (request == null) {
                return template;
            }
            if (isTemplateFor(request, template)) {
                return template;
            }
        }
        if (defaultTemplate != null) {
            return defaultTemplate;
        }

        return theTemplates.get(0);
    }



    /**
     * _more_
     *
     * @param request _more_
     * @param template _more_
     *
     * @return _more_
     */
    public boolean isTemplateFor(Request request, HtmlTemplate template) {
        if (request.getUser() == null) {
            return false;
        }
        String templateId = request.getUser().getTemplate();
        if (templateId == null) {
            return false;
        }
        if (Misc.equals(template.getId(), templateId)) {
            return true;
        }

        return false;
    }




    /**
     * _more_
     *
     * @param msg _more_
     *
     * @return _more_
     */
    public static String msg(String msg) {
        if (msg == null) {
            return null;
        }
        if (msg.indexOf(MSG_PREFIX) >= 0) {
            //            System.err.println("bad msg:" + msg+"\n" + LogUtil.getStackTrace());
            //            throw new IllegalArgumentException("bad msg:" + msg);
            return msg;

        }

        return MSG_PREFIX + msg + MSG_SUFFIX;
    }

    /**
     * _more_
     *
     * @param msg _more_
     *
     * @return _more_
     */
    public static String msgLabel(String msg) {
        if (msg == null) {
            return null;
        }
        if (msg.length() == 0) {
            return msg;
        }
        msg = msg(msg);

        return msg(msg) + ":" + HtmlUtils.space(1);
    }

    /**
     * _more_
     *
     * @param h _more_
     *
     * @return _more_
     */
    public static String msgHeader(String h) {
        return HtmlUtils.div(msg(h), HtmlUtils.cssClass(CSS_CLASS_HEADING_1));
    }




    /**
     * _more_
     *
     * @param sb _more_
     * @param date _more_
     * @param url _more_
     * @param dayLinks _more_
     *
     * @throws Exception _more_
     */
    public void createMonthNav(Appendable sb, Date date, String url,
                               Hashtable dayLinks)
            throws Exception {

        GregorianCalendar cal =
            new GregorianCalendar(RepositoryUtil.TIMEZONE_DEFAULT);
        cal.setTime(date);
        int[] theDate  = CalendarOutputHandler.getDayMonthYear(cal);
        int   theDay   = cal.get(cal.DAY_OF_MONTH);
        int   theMonth = cal.get(cal.MONTH);
        int   theYear  = cal.get(cal.YEAR);
        while (cal.get(cal.DAY_OF_MONTH) > 1) {
            cal.add(cal.DAY_OF_MONTH, -1);
        }
        GregorianCalendar prev =
            new GregorianCalendar(RepositoryUtil.TIMEZONE_DEFAULT);
        prev.setTime(date);
        prev.add(cal.MONTH, -1);
        GregorianCalendar next =
            new GregorianCalendar(RepositoryUtil.TIMEZONE_DEFAULT);
        next.setTime(date);
        next.add(cal.MONTH, 1);

        sb.append(HtmlUtils.open(HtmlUtils.TAG_TABLE,
                                 HtmlUtils.attrs(HtmlUtils.ATTR_BORDER, "1",
                                     HtmlUtils.ATTR_CELLSPACING, "0",
                                     HtmlUtils.ATTR_CELLPADDING, "0")));
        String[] dayNames = {
            "Su", "Mo", "Tu", "We", "Th", "Fr", "Sa"
        };
        String prevUrl = HtmlUtils.space(1)
                         + HtmlUtils.href(
                             url + "&"
                             + CalendarOutputHandler.getUrlArgs(
                                 prev), "&lt;");
        String nextUrl =
            HtmlUtils.href(
                url + "&" + CalendarOutputHandler.getUrlArgs(next),
                HtmlUtils.ENTITY_GT) + HtmlUtils.space(1);
        sb.append(HtmlUtils.open(HtmlUtils.TAG_TR,
                                 HtmlUtils.attr(HtmlUtils.ATTR_VALIGN,
                                     HtmlUtils.VALUE_TOP)));
        sb.append(HtmlUtils.open(HtmlUtils.TAG_TD,
                                 HtmlUtils.attrs(HtmlUtils.ATTR_COLSPAN, "7",
                                     HtmlUtils.ATTR_ALIGN,
                                     HtmlUtils.VALUE_CENTER,
                                     HtmlUtils.ATTR_CLASS,
                                     "calnavmonthheader")));


        sb.append(
            HtmlUtils.open(
                HtmlUtils.TAG_TABLE,
                HtmlUtils.cssClass("calnavtable")
                + HtmlUtils.attrs(
                    HtmlUtils.ATTR_CELLSPACING, "0",
                    HtmlUtils.ATTR_CELLPADDING, "0", HtmlUtils.ATTR_WIDTH,
                    "100%")));
        sb.append(HtmlUtils.open(HtmlUtils.TAG_TR));
        sb.append(HtmlUtils.col(prevUrl,
                                HtmlUtils.attrs(HtmlUtils.ATTR_WIDTH, "1",
                                    HtmlUtils.ATTR_CLASS,
                                    "calnavmonthheader")));
        sb.append(
            HtmlUtils.col(
                DateUtil.MONTH_NAMES[cal.get(cal.MONTH)] + HtmlUtils.space(1)
                + theYear, HtmlUtils.attr(
                    HtmlUtils.ATTR_CLASS, "calnavmonthheader")));



        sb.append(HtmlUtils.col(nextUrl,
                                HtmlUtils.attrs(HtmlUtils.ATTR_WIDTH, "1",
                                    HtmlUtils.ATTR_CLASS,
                                    "calnavmonthheader")));
        sb.append(HtmlUtils.close(HtmlUtils.TAG_TABLE));
        sb.append(HtmlUtils.close(HtmlUtils.TAG_TR));
        sb.append(HtmlUtils.open(HtmlUtils.TAG_TR));
        for (int colIdx = 0; colIdx < 7; colIdx++) {
            sb.append(HtmlUtils.col(dayNames[colIdx],
                                    HtmlUtils.attrs(HtmlUtils.ATTR_WIDTH,
                                        "14%", HtmlUtils.ATTR_CLASS,
                                        "calnavdayheader")));
        }
        sb.append(HtmlUtils.close(HtmlUtils.TAG_TR));
        int startDow = cal.get(cal.DAY_OF_WEEK);
        while (startDow > 1) {
            cal.add(cal.DAY_OF_MONTH, -1);
            startDow--;
        }
        for (int rowIdx = 0; rowIdx < 6; rowIdx++) {
            sb.append(HtmlUtils.open(HtmlUtils.TAG_TR,
                                     HtmlUtils.attrs(HtmlUtils.ATTR_VALIGN,
                                         HtmlUtils.VALUE_TOP)));
            for (int colIdx = 0; colIdx < 7; colIdx++) {
                int     thisDay    = cal.get(cal.DAY_OF_MONTH);
                int     thisMonth  = cal.get(cal.MONTH);
                int     thisYear   = cal.get(cal.YEAR);
                boolean currentDay = false;
                String  dayClass   = "calnavday";
                if (thisMonth != theMonth) {
                    dayClass = "calnavoffday";
                } else if ((theMonth == thisMonth) && (theYear == thisYear)
                           && (theDay == thisDay)) {
                    dayClass   = "calnavtheday";
                    currentDay = true;
                }
                String content;
                if (dayLinks != null) {
                    String key = thisYear + "/" + thisMonth + "/" + thisDay;
                    if (dayLinks.get(key) != null) {
                        content = HtmlUtils.href(url + "&"
                                + CalendarOutputHandler.getUrlArgs(cal), ""
                                    + thisDay);
                        if ( !currentDay) {
                            dayClass = "calnavoffday";
                        }
                    } else {
                        content  = "" + thisDay;
                        dayClass = "calnavday";
                    }
                } else {
                    content = HtmlUtils.href(
                        url + "&" + CalendarOutputHandler.getUrlArgs(cal),
                        "" + thisDay);
                }

                sb.append(HtmlUtils.col(content,
                                        HtmlUtils.cssClass(dayClass)));
                sb.append("\n");
                cal.add(cal.DAY_OF_MONTH, 1);
            }
            if (cal.get(cal.MONTH) > theMonth) {
                break;
            }
            if (cal.get(cal.YEAR) > theYear) {
                break;
            }
        }
        sb.append(HtmlUtils.close(HtmlUtils.TAG_TABLE));

    }



    /**
     * _more_
     *
     * @param request The request
     * @param name _more_
     * @param formName _more_
     * @param date _more_
     *
     * @return _more_
     */
    public String makeDateInput(Request request, String name,
                                String formName, Date date) {
        return makeDateInput(request, name, formName, date, null);
    }

    /**
     * _more_
     *
     * @param request The request
     * @param name _more_
     * @param formName _more_
     * @param date _more_
     * @param timezone _more_
     *
     * @return _more_
     */
    public String makeDateInput(Request request, String name,
                                String formName, Date date, String timezone) {
        return makeDateInput(request, name, formName, date, timezone, true);
    }

    /**
     * Make the HTML for a date input widget
     *
     * @param request The request
     * @param name    the name
     * @param formName  the form name
     * @param date      the default date
     * @param timezone  the timezone
     * @param includeTime  true to include a time box
     *
     * @return  the widget html
     */
    public String makeDateInput(Request request, String name,
                                String formName, Date date, String timezone,
                                boolean includeTime) {
        return makeDateInput(request, name, formName, date, timezone,
                             includeTime, null);
    }

    /**
     * Make the HTML for a date input widget
     *
     * @param request The request
     * @param name    the name
     * @param formName  the form name
     * @param date      the default date
     * @param timezone  the timezone
     * @param includeTime  true to include a time box
     * @param dates _more_
     *
     * @return  the widget html
     */
    public String makeDateInput(Request request, String name,
                                String formName, Date date, String timezone,
                                boolean includeTime, List dates) {
        String dateHelp = "e.g., yyyy-mm-dd,  now, -1 week, +3 days, etc.";
        String           timeHelp   = "hh:mm:ss Z, e.g. 20:15:00 MST";

        String           dateArg    = request.getString(name, "");
        String           timeArg    = request.getString(name + ".time", "");
        String           dateString = ((date == null)
                                       ? dateArg
                                       : dateSdf.format(date));
        SimpleDateFormat timeFormat = ((timezone == null)
                                       ? timeSdf
                                       : getSDF("HH:mm:ss z", timezone));
        String           timeString = ((date == null)
                                       ? timeArg
                                       : timeFormat.format(date));

        String           inputId    = "dateinput" + (HtmlUtils.blockCnt++);
        String           minDate    = null;
        String           maxDate    = null;
        if ((dates != null) && !dates.isEmpty()) {
            minDate = dateSdf.format((Date) dates.get(0));
            maxDate = dateSdf.format((Date) dates.get(dates.size() - 1));
        }

        StringBuilder jsBuf =
            new StringBuilder("<script>jQuery(function() {$( ");
        jsBuf.append(HtmlUtils.squote("#" + inputId));
        jsBuf.append(
            " ).datepicker({ dateFormat: 'yy-mm-dd',changeMonth: true, changeYear: true,constrainInput:false, yearRange: '1900:2100' ");
        if ((minDate != null) && (maxDate != null)) {
            jsBuf.append(", minDate: '" + minDate + "', maxDate: '" + maxDate
                         + "'");
        }
        jsBuf.append(" });});</script>");
        String extra = "";
        if (includeTime) {
            extra = " T:"
                    + HtmlUtils.input(name + ".time", timeString,
                                      HtmlUtils.sizeAttr(6)
                                      + HtmlUtils.attr(HtmlUtils.ATTR_TITLE,
                                          timeHelp));
        }

        return "\n" + jsBuf.toString() + "\n"
               + HtmlUtils.input(name, dateString,
                                 HtmlUtils.SIZE_10 + HtmlUtils.id(inputId)
                                 + HtmlUtils.title(dateHelp)) + extra;
    }



    /**
     * _more_
     *
     * @param link _more_
     * @param menuContents _more_
     *
     * @return _more_
     */
    public String makePopupLink(String link, String menuContents) {
        return makePopupLink(link, menuContents, false, false);
    }


    /**
     * _more_
     *
     * @param link _more_
     * @param menuContents _more_
     * @param makeClose _more_
     * @param alignLeft _more_
     *
     * @return _more_
     */
    public String makePopupLink(String link, String menuContents,
                                boolean makeClose, boolean alignLeft) {
        return makePopupLink(link, menuContents, "", makeClose, alignLeft);
    }


    /**
     * _more_
     *
     * @param link _more_
     * @param menuContents _more_
     * @param linkAttributes _more_
     *
     * @return _more_
     */
    public String makePopupLink(String link, String menuContents,
                                String linkAttributes) {
        return makePopupLink(link, menuContents, linkAttributes, false,
                             false);
    }

    /**
     * _more_
     *
     * @param link _more_
     * @param menuContents _more_
     * @param linkAttributes _more_
     * @param makeClose _more_
     * @param alignLeft _more_
     *
     * @return _more_
     */
    public String makePopupLink(String link, String menuContents,
                                String linkAttributes, boolean makeClose,
                                boolean alignLeft) {
        StringBuilder sb = new StringBuilder();
        link = makePopupLink(link, menuContents, linkAttributes, makeClose,
                             alignLeft, sb);

        return link + sb;
    }

    /**
     * _more_
     *
     * @param link _more_
     * @param menuContents _more_
     * @param linkAttributes _more_
     * @param makeClose _more_
     * @param alignLeft _more_
     * @param popup _more_
     *
     * @return _more_
     */
    public String makePopupLink(String link, String menuContents,
                                String linkAttributes, boolean makeClose,
                                boolean alignLeft, Appendable popup) {
        try {

            String compId = "menu_" + HtmlUtils.blockCnt++;
            String linkId = "menulink_" + HtmlUtils.blockCnt++;
            popup.append(makePopupDiv(menuContents, compId, makeClose));
            String onClick =
                HtmlUtils.onMouseClick(HtmlUtils.call("showPopup",
                    HtmlUtils.comma(new String[] { "event",
                    HtmlUtils.squote(linkId), HtmlUtils.squote(compId),
                    (alignLeft
                     ? "1"
                     : "0") })));
            String href = HtmlUtils.href("javascript:noop();", link,
                                         onClick + HtmlUtils.id(linkId)
                                         + linkAttributes);

            return href;
        } catch (java.io.IOException ioe) {
            throw new RuntimeException(ioe);
        }


    }



    /**
     * _more_
     *
     * @param link _more_
     * @param innerContents _more_
     * @param initCall _more_
     *
     * @return _more_
     */
    public String makeStickyPopup(String link, String innerContents,
                                  String initCall) {
        boolean alignLeft = true;
        String  compId    = "menu_" + HtmlUtils.blockCnt++;
        String  linkId    = "menulink_" + HtmlUtils.blockCnt++;
        String  contents  = makeStickyPopupDiv(innerContents, compId);
        String onClick =
            HtmlUtils.onMouseClick(HtmlUtils.call("showStickyPopup",
                HtmlUtils.comma(new String[] { "event",
                HtmlUtils.squote(linkId), HtmlUtils.squote(compId), (alignLeft
                ? "1"
                : "0") })) + initCall);
        String href = HtmlUtils.href("javascript:noop();", link,
                                     onClick + HtmlUtils.id(linkId));

        return href + contents;
    }



    /**
     * _more_
     *
     * @param contents _more_
     * @param compId _more_
     *
     * @return _more_
     */
    public String makeStickyPopupDiv(String contents, String compId) {
        StringBuilder menu = new StringBuilder();
        String cLink = HtmlUtils.jsLink(
                           HtmlUtils.onMouseClick(
                               HtmlUtils.call(
                                   "hideElementById",
                                   HtmlUtils.squote(compId))), HtmlUtils.img(
                                       iconUrl(ICON_CLOSE)), "");
        contents = cLink + HtmlUtils.br() + contents;

        menu.append(HtmlUtils.div(contents,
                                  HtmlUtils.id(compId)
                                  + HtmlUtils.cssClass(CSS_CLASS_POPUP)));

        return menu.toString();
    }




    /**
     * _more_
     *
     * @param contents _more_
     * @param compId _more_
     * @param makeClose _more_
     *
     * @return _more_
     */
    public String makePopupDiv(String contents, String compId,
                               boolean makeClose) {
        StringBuilder menu = new StringBuilder();
        if (makeClose) {
            String cLink = HtmlUtils.jsLink(
                               HtmlUtils.onMouseClick("hidePopupObject();"),
                               HtmlUtils.img(iconUrl(ICON_CLOSE)), "");
            contents = cLink + HtmlUtils.br() + contents;
        }

        menu.append(HtmlUtils.div(contents,
                                  HtmlUtils.id(compId)
                                  + HtmlUtils.cssClass(CSS_CLASS_POPUP)));

        return menu.toString();
    }



    /**
     * _more_
     *
     * @param request The request
     * @param url _more_
     * @param okArg _more_
     * @param extra _more_
     *
     * @return _more_
     */
    public static String makeOkCancelForm(Request request, RequestUrl url,
                                          String okArg, String extra) {
        StringBuilder fb = new StringBuilder();
        fb.append(request.form(url));
        fb.append(extra);
        String okButton     = HtmlUtils.submit("OK", okArg);
        String cancelButton = HtmlUtils.submit("Cancel",
                                  Constants.ARG_CANCEL);
        String buttons = HtmlUtils.buttons(okButton, cancelButton);
        fb.append(buttons);
        fb.append(HtmlUtils.formClose());

        return fb.toString();
    }


    /** _more_ */
    private Hashtable<String, SimpleDateFormat> dateFormats =
        new Hashtable<String, SimpleDateFormat>();

    /** _more_ */
    TimeZone defaultTimeZone;


    /** _more_ */
    protected List<SimpleDateFormat> formats;





    /**
     * _more_
     *
     * @param format _more_
     * @param timezone _more_
     *
     * @return _more_
     */
    protected SimpleDateFormat getSDF(String format, String timezone) {
        String key;
        if (timezone != null) {
            key = format + "-" + timezone;
        } else {
            key = format;
        }
        SimpleDateFormat sdf = dateFormats.get(key);
        if (sdf == null) {
            sdf = new SimpleDateFormat();
            sdf.applyPattern(format);
            if (timezone == null) {
                sdf.setTimeZone(TIMEZONE_UTC);
            } else {
                if ((defaultTimeZone != null)
                        && (timezone.equals("")
                            || timezone.equals("default"))) {
                    sdf.setTimeZone(defaultTimeZone);
                } else {
                    sdf.setTimeZone(TimeZone.getTimeZone(timezone));
                }
            }
            dateFormats.put(key, sdf);
        }

        return sdf;
    }


    /**
     * _more_
     *
     * @param format _more_
     *
     * @return _more_
     */
    public SimpleDateFormat makeSDF(String format) {
        return getSDF(format, null);
    }

    /**
     * _more_
     *
     * @param d _more_
     *
     * @return _more_
     */
    public String formatDate(Date d) {
        return formatDate(d, null);
    }

    /**
     * _more_
     *
     * @param d _more_
     * @param timezone _more_
     *
     * @return _more_
     */
    public String formatDate(Date d, String timezone) {
        if (sdf == null) {
            sdf = makeSDF(getProperty(PROP_DATE_FORMAT, DEFAULT_TIME_FORMAT));
        }
        SimpleDateFormat dateFormat = ((timezone == null)
                                       ? sdf
                                       : getSDF(getProperty(PROP_DATE_FORMAT,
                                           DEFAULT_TIME_FORMAT), timezone));
        if (d == null) {
            return BLANK;
        }
        synchronized (dateFormat) {
            return dateFormat.format(d);
        }
    }


    /**
     * _more_
     *
     * @param request The request
     * @param ms _more_
     *
     * @return _more_
     */
    public String formatDate(Request request, long ms) {
        return formatDate(new Date(ms));
    }


    /**
     * _more_
     *
     * @param request The request
     * @param ms _more_
     * @param timezone _more_
     *
     * @return _more_
     */
    public String formatDate(Request request, long ms, String timezone) {
        return formatDate(new Date(ms), timezone);
    }

    /**
     * _more_
     *
     * @param request The request
     * @param d _more_
     *
     * @return _more_
     */
    public String formatDate(Request request, Date d) {
        return formatDate(d);
    }


    /**
     * _more_
     *
     * @param request The request
     * @param d _more_
     * @param timezone _more_
     *
     * @return _more_
     */
    public String formatDate(Request request, Date d, String timezone) {
        return formatDate(d, timezone);
    }




    /**
     * _more_
     *
     * @param request The request
     * @param d _more_
     * @param timezone _more_
     *
     * @return _more_
     */
    public String formatDateShort(Request request, Date d, String timezone) {
        return formatDateShort(request, d, timezone, "");
    }

    /**
     * _more_
     *
     * @param request The request
     * @param d _more_
     * @param timezone _more_
     * @param extraAlt _more_
     *
     * @return _more_
     */
    public String formatDateShort(Request request, Date d, String timezone,
                                  String extraAlt) {
        if (d == null) {
            return BLANK;
        }

        String fmt = getProperty(PROP_DATE_SHORTFORMAT,
                                 DEFAULT_TIME_SHORTFORMAT);
        SimpleDateFormat sdf      = getSDF(fmt, timezone);



        Date             now      = new Date();
        long             diff     = now.getTime() - d.getTime();
        double           minutes  = DateUtil.millisToMinutes(diff);
        String           fullDate = formatDate(d, timezone);
        String           result;
        if ((minutes > 0) && (minutes < 65) && (minutes > 55)) {
            result = "about an hour ago";
        } else if ((diff > 0) && (diff < DateUtil.minutesToMillis(1))) {
            result = (int) (diff / (1000)) + " seconds ago";
        } else if ((diff > 0) && (diff < DateUtil.hoursToMillis(1))) {
            int value = (int) DateUtil.millisToMinutes(diff);
            result = value + " minute" + ((value > 1)
                                          ? "s"
                                          : "") + " ago";
        } else if ((diff > 0) && (diff < DateUtil.hoursToMillis(24))) {
            int value = (int) (diff / (1000 * 60 * 60));
            result = value + " hour" + ((value > 1)
                                        ? "s"
                                        : "") + " ago";
        } else if ((diff > 0) && (diff < DateUtil.daysToMillis(6))) {
            int value = (int) (diff / (1000 * 60 * 60 * 24));
            result = value + " day" + ((value > 1)
                                       ? "s"
                                       : "") + " ago";
        } else {
            result = sdf.format(d);
        }

        return HtmlUtils.span(result,
                              HtmlUtils.cssClass(CSS_CLASS_DATETIME)
                              + HtmlUtils.attr(HtmlUtils.ATTR_TITLE,
                                  fullDate + extraAlt));
    }





    /**
     * _more_
     *
     * @param dttm _more_
     *
     * @return _more_
     *
     * @throws java.text.ParseException _more_
     */
    public Date parseDate(String dttm) throws java.text.ParseException {
        if ( !Utils.stringDefined(dttm)) {
            return null;
        }
        if (formats == null) {
            formats = new ArrayList<SimpleDateFormat>();
            formats.add(makeSDF("yyyy-MM-dd HH:mm:ss z"));
            formats.add(makeSDF("yyyy-MM-dd HH:mm:ss"));
            formats.add(makeSDF("yyyy-MM-dd HH:mm"));
            formats.add(makeSDF("yyyy-MM-dd"));
        }


        for (SimpleDateFormat fmt : formats) {
            try {
                synchronized (fmt) {
                    return fmt.parse(dttm);
                }
            } catch (Exception noop) {}
        }

        throw new IllegalArgumentException("Unable to parse date:" + dttm);
    }

    /**
     * _more_
     *
     *
     * @param request The request
     * @return _more_
     */
    public List getNavLinks(Request request) {
        List    links   = new ArrayList();
        boolean isAdmin = false;
        if (request != null) {
            User user = request.getUser();
            isAdmin = user.getAdmin();
        }

        String template = getPageHandler().getTemplateProperty(request,
                              "ramadda.template.link.wrapper", "");

        ApiMethod homeApi = getRepository().getApiManager().getHomeApi();
        for (ApiMethod apiMethod :
                getRepository().getApiManager().getTopLevelMethods()) {
            if (apiMethod.getMustBeAdmin() && !isAdmin) {
                continue;
            }
            if ( !apiMethod.getIsTopLevel()) {
                continue;
            }
            String url;
            if (apiMethod == homeApi) {
                url = fileUrl(apiMethod.getRequest());
            } else {
                url = request.url(apiMethod.getUrl());
            }


            String html = template.replace("${url}", url);
            html = html.replace("${label}", msg(apiMethod.getName()));
            html = html.replace("${topgroup}",
                                getEntryManager().getTopGroup().getName());
            links.add(html);
        }

        return links;
    }


    /**
     * _more_
     *
     * @param request The request
     * @param sb _more_
     * @param urls _more_
     * @param arg _more_
     *
     *
     * @throws Exception _more_
     */
    public void makeLinksHeader(Request request, Appendable sb,
                                List<RequestUrl> urls, String arg)
            throws Exception {
        List<String> links   = new ArrayList();
        String       type    = request.getRequestPath();
        String       onLabel = null;
        for (RequestUrl requestUrl : urls) {
            String label = requestUrl.getLabel();
            label = msg(label);
            if (label == null) {
                label = requestUrl.toString();
            }
            String url = request.url(requestUrl) + arg;
            if (type.endsWith(requestUrl.getPath())) {
                //links.add(HtmlUtils.span(label,
                // HtmlUtils.cssClass("subheader-on")));
                onLabel = label;
                //            } else {
            }
            links.add(HtmlUtils.span(HtmlUtils.href(url, label),
                                     HtmlUtils.cssClass("subheader-off")));
            //            }
        }
        StringBuilder header =
            new StringBuilder(
                StringUtil.join(
                    "<span class=\"subheader-sep\">|</span>", links));
        header.append("\n");
        if (Utils.stringDefined(onLabel)) {
            header.append(HtmlUtils.div(HtmlUtils.h2(msg(onLabel)), 
                    HtmlUtils.style("margin-top: .5em")));
        }
        sb.append(HtmlUtils.tag(HtmlUtils.TAG_DIV,
                                HtmlUtils.cssClass("subheader-container"),
                                header.toString()));
    }



    /**
     * _more_
     *
     * @param h _more_
     *
     * @return _more_
     */
    public String showDialogNote(String h) {
        return getMessage(h, Constants.ICON_INFORMATION, true);
    }

    /**
     * _more_
     *
     * @param h _more_
     *
     * @return _more_
     */
    public String progress(String h) {
        return getMessage(h, Constants.ICON_PROGRESS, false);
    }


    /**
     * _more_
     *
     * @param h _more_
     *
     * @return _more_
     */
    public String showDialogWarning(String h) {
        return getMessage(h, Constants.ICON_WARNING, true);
    }


    /**
     * _more_
     *
     * @param h _more_
     * @param buttons _more_
     *
     * @return _more_
     */
    public String showDialogQuestion(String h, String buttons) {
        return getMessage(h + "<p><hr>" + buttons, Constants.ICON_QUESTION,
                          false);
    }

    /**
     * _more_
     *
     * @param h _more_
     *
     * @return _more_
     */
    public String showDialogError(String h) {
        h = getDialogString(h);

        return getMessage(h, Constants.ICON_ERROR, true);
    }


    /**
     * _more_
     *
     * @param s _more_
     *
     * @return _more_
     */
    public static String getDialogString(String s) {
        s = s.replaceAll("<pre>", "PREOPEN");
        s = s.replaceAll("</pre>", "PRECLOSE");
        s = HtmlUtils.entityEncode(s);
        s = s.replace("&#60;msg&#32;", "<msg ");
        s = s.replace("&#32;msg&#62;", " msg>");
        s = s.replace("&#32;", " ");
        s = s.replace("&#60;p&#62;", "<p>");
        s = s.replace("&#60;br&#62;", "<br>");
        s = s.replace("&#38;nbsp&#59;", "&nbsp;");
        s = s.replaceAll("PREOPEN", "<pre>");
        s = s.replaceAll("PRECLOSE", "</pre>");

        return s;
    }


    /**
     * _more_
     *
     * @param h _more_
     * @param icon _more_
     * @param showClose _more_
     *
     * @return _more_
     */
    public String getMessage(String h, String icon, boolean showClose) {
        String html =
            HtmlUtils.jsLink(HtmlUtils.onMouseClick("hide('messageblock')"),
                             HtmlUtils.img(iconUrl(Constants.ICON_CLOSE)));
        if ( !showClose) {
            html = "&nbsp;";
        }
        h = "<div class=\"innernote\"><table cellspacing=\"0\" cellpadding=\"0\" border=\"0\"><tr><td valign=\"top\">"
            + HtmlUtils.img(iconUrl(icon)) + HtmlUtils.space(2)
            + "</td><td valign=\"bottom\"><span class=\"notetext\">" + h
            + "</span></td></tr></table></div>";

        return "\n<table border=\"0\" id=\"messageblock\"><tr><td><div class=\"note\"><table><tr valign=top><td>"
               + h + "</td><td>" + html + "</td></tr></table>"
               + "</div></td></tr></table>\n";
    }


    /**
     * _more_
     *
     * @param request _more_
     * @param entry _more_
     *
     * @return _more_
     */
    public PageStyle doMakePageStyle(Request request, Entry entry) {
        try {
            PageStyle pageStyle = new PageStyle();
            if (request.exists(PROP_NOSTYLE)
                    || getProperty(PROP_NOSTYLE, false)) {
                return pageStyle;
            }
            List<Metadata> metadataList =
                getMetadataManager().findMetadata(request, entry,
                    ContentMetadataHandler.TYPE_PAGESTYLE, true);
            if ((metadataList == null) || (metadataList.size() == 0)) {
                return pageStyle;
            }

            //menus -1, showbreadcrumbs-2, toolbar-3, entry header-4, layout toolbar-5, type-6,  apply to this-7, wiki-8
            Metadata theMetadata = null;
            for (Metadata metadata : metadataList) {
                if (Misc.equals(metadata.getAttr(7), "false")) {
                    if (metadata.getEntryId().equals(entry.getId())) {
                        continue;
                    }
                }
                String types = metadata.getAttr(6);
                if ((types == null) || (types.trim().length() == 0)) {
                    theMetadata = metadata;

                    break;
                }
                for (String type : StringUtil.split(types, ",", true, true)) {
                    if (type.equals("file") && !entry.isGroup()) {
                        theMetadata = metadata;

                        break;
                    }
                    if (type.equals("folder") && entry.isGroup()) {
                        theMetadata = metadata;

                        break;
                    }
                    if (entry.getTypeHandler().isType(type)) {
                        theMetadata = metadata;

                        break;
                    }
                }
            }

            if (theMetadata == null) {
                return pageStyle;
            }

            pageStyle.setShowBreadcrumbs(Misc.equals(theMetadata.getAttr2(),
                    "true"));
            pageStyle.setShowToolbar(Misc.equals(theMetadata.getAttr3(),
                    "true"));
            pageStyle.setShowEntryHeader(Misc.equals(theMetadata.getAttr4(),
                    "true"));
            pageStyle.setShowLayoutToolbar(
                Misc.equals(theMetadata.getAttr(5), "true"));

            boolean canEdit = getAccessManager().canDoAction(request, entry,
                                  Permission.ACTION_EDIT);
            if ( !canEdit) {
                String menus = theMetadata.getAttr1();
                if ((menus != null) && (menus.trim().length() > 0)) {
                    if (menus.equals("none")) {
                        pageStyle.setShowMenubar(false);
                    } else {
                        for (String menu :
                                StringUtil.split(menus, ",", true, true)) {
                            pageStyle.setMenu(menu);
                        }
                    }
                }
            }
            if ((theMetadata.getAttr(8) != null)
                    && (theMetadata.getAttr(8).trim().length() > 0)) {
                pageStyle.setWikiTemplate(theMetadata.getAttr(8));
            }

            return pageStyle;
        } catch (Exception exc) {
            throw new RuntimeException(exc);
        }
    }


    /**
     * _more_
     *
     * @param request _more_
     * @param entry _more_
     *
     * @return _more_
     *
     * @throws Exception _more_
     */
    public String getConfirmBreadCrumbs(Request request, Entry entry)
            throws Exception {
        return HtmlUtils.img(getIconUrl(request, entry)) + " "
               + getBreadCrumbs(request, entry);
    }




    /**
     * _more_
     *
     * @param request _more_
     * @param entry _more_
     *
     *
     * @return _more_
     * @throws Exception _more_
     */
    public String getBreadCrumbs(Request request, Entry entry)
            throws Exception {
        return getBreadCrumbs(request, entry, null, null, 80);
    }


    /**
     * _more_
     *
     * @param request _more_
     * @param entry _more_
     * @param stopAt _more_
     *
     * @return _more_
     *
     * @throws Exception _more_
     */
    public String getBreadCrumbs(Request request, Entry entry, Entry stopAt)
            throws Exception {
        return getBreadCrumbs(request, entry, stopAt, null, 80);
    }


    /**
     * _more_
     *
     * @param request _more_
     * @param entry _more_
     * @param stopAt _more_
     * @param requestUrl _more_
     * @param lengthLimit _more_
     *
     * @return _more_
     *
     * @throws Exception _more_
     */
    public String getBreadCrumbs(Request request, Entry entry, Entry stopAt,
                                 RequestUrl requestUrl, int lengthLimit)
            throws Exception {
        if (entry == null) {
            return BLANK;
        }

        List breadcrumbs = new ArrayList();
        Entry parent = getEntryManager().findGroup(request,
                           entry.getParentEntryId());
        int         length          = 0;
        List<Entry> parents         = new ArrayList<Entry>();
        int         totalNameLength = 0;
        while (parent != null) {
            parents.add(parent);
            String name = parent.getName();
            totalNameLength += name.length();
            if (stopAt != null) {
                if (stopAt.getId().equals(parent.getId())) {
                    break;
                }
            }

            parent = getEntryManager().findGroup(request,
                    parent.getParentEntryId());
        }


        boolean needToClip = totalNameLength > lengthLimit;
        String  target     = (request.defined(ARG_TARGET)
                              ? request.getString(ARG_TARGET, "")
                              : null);
        String  targetAttr = ((target != null)
                              ? HtmlUtils.attr(HtmlUtils.ATTR_TARGET, target)
                              : "");

        for (Entry ancestor : parents) {
            if (length > lengthLimit) {
                breadcrumbs.add(0, "...");

                break;
            }
            String name = ancestor.getName();
            if (needToClip && (name.length() > 20)) {
                name = name.substring(0, 19) + "...";
            }
            length += name.length();
            //xxx
            //            String linkLabel =  HtmlUtils.img(getIconUrl(request, ancestor))+" " + name;
            String linkLabel = name;
            String link      = null;
            if (target != null) {
                link = HtmlUtils.href(
                    request.entryUrl(
                        getRepository().URL_ENTRY_SHOW, ancestor), linkLabel,
                            targetAttr);
            } else {
                link = ((requestUrl == null)
                        ? getEntryManager().getTooltipLink(request, ancestor,
                        name, null)
                        : HtmlUtils.href(request.entryUrl(requestUrl,
                        ancestor), linkLabel, targetAttr));
            }
            breadcrumbs.add(0, link);
        }
        if (target != null) {
            breadcrumbs.add(
                HtmlUtils.href(
                    request.entryUrl(getRepository().URL_ENTRY_SHOW, entry),
                    entry.getLabel(), targetAttr));

        } else {
            if (requestUrl == null) {
                breadcrumbs.add(getEntryManager().getTooltipLink(request,
                        entry, entry.getLabel(), null));
            } else {
                breadcrumbs.add(HtmlUtils.href(request.entryUrl(requestUrl,
                        entry), entry.getLabel()));
            }
        }
        //        breadcrumbs.add(HtmlUtils.href(request.entryUrl(getRepository().URL_ENTRY_SHOW,
        //                entry), entry.getLabel()));
        //        breadcrumbs.add(HtmlUtils.href(request.entryUrl(getRepository().URL_ENTRY_SHOW,
        //                entry), entry.getLabel()));

        //        System.err.println("BC:" + breadcrumbs);

        String separator = getPageHandler().getTemplateProperty(request,
                               "ramadda.template.breadcrumbs.separator",
                               BREADCRUMB_SEPARATOR);

        return StringUtil.join(HtmlUtils.pad(BREADCRUMB_SEPARATOR),
                               breadcrumbs);
    }



    /**
     * _more_
     *
     * @param request _more_
     * @param entry _more_
     * @param title _more_
     *
     * @return _more_
     *
     * @throws Exception _more_
     */
    public String getEntryHeader(Request request, Entry entry,
                                 Appendable title)
            throws Exception {
        if (entry == null) {
            return BLANK;
        }
        if (request == null) {
            request = getRepository().getTmpRequest(entry);
        }


        PageStyle pageStyle = request.getPageStyle(entry);


        Entry parent = getEntryManager().findGroup(request,
                           entry.getParentEntryId());
        OutputType  output  = OutputHandler.OUTPUT_HTML;
        int         length  = 0;

        List<Entry> parents = new ArrayList<Entry>();
        parents.add(entry);

        while (parent != null) {
            parents.add(parent);
            parent = getEntryManager().findGroup(request,
                    parent.getParentEntryId());
        }

        HtmlTemplate htmlTemplate = getPageHandler().getTemplate(request);
        List<Link> linkList = getEntryManager().getEntryLinks(request, entry);

        String links =
            getEntryManager().getEntryActionsTable(request, entry,
                OutputType.TYPE_FILE | OutputType.TYPE_EDIT
                | OutputType.TYPE_VIEW | OutputType.TYPE_OTHER, linkList,
                    false,
                    msgLabel("Links for") + " " + getEntryDisplayName(entry));


        StringBuilder popup = new StringBuilder();
        String menuLinkImg =
            HtmlUtils.img(getRepository().iconUrl("/icons/menu_arrow.gif"),
                          msg("Click to show menu"),
                          HtmlUtils.cssClass("ramadda-breadcrumbs-menu-img"));
        String menuLink = getPageHandler().makePopupLink(menuLinkImg, links,
                              "", true, false, popup);

        List<String> titleList = new ArrayList();
        List<String> breadcrumbs = makeBreadcrumbList(request, parents,
                                       titleList);

        boolean showBreadcrumbs = pageStyle.getShowBreadcrumbs(entry);
        boolean showMenu        = pageStyle.getShowMenubar(entry);
        String  toolbar         = pageStyle.getShowToolbar(entry)
                                  ? getEntryToolbar(request, entry)
                                  : "";

        String  header          = "";
        if (showBreadcrumbs) {
            header = makeBreadcrumbs(request, breadcrumbs);
            StringBuilder sb =
                new StringBuilder(
                    "<div class=ramadda-breadcrumbs><table border=0 width=100% cellspacing=0 cellpadding=0><tr valign=center>");
            if (showMenu) {
                sb.append(
                    "<td valign=center width=1%><div class=ramadda-breadcrumbs-menu>");
                sb.append(menuLink);
                sb.append("</div></td>");
            }

            sb.append("<td>");
            sb.append(header);
            sb.append("</td>");
            sb.append("<td align=right width=100>");
            sb.append(toolbar);
            sb.append("</td>");
            sb.append("</tr></table></div>");
            sb.append(popup);
            header = sb.toString();
        } else {
            if ( !request.isAnonymous()) {
                header = menuLink + popup;
            }
        }
        title.append(
            StringUtil.join(
                HtmlUtils.pad(Repository.BREADCRUMB_SEPARATOR), titleList));

        return header;

    }

    /**
     * _more_
     *
     * @param request _more_
     * @param entry _more_
     *
     * @return _more_
     *
     * @throws Exception _more_
     */
    public String getEntryToolbar(Request request, Entry entry)
            throws Exception {
        List<Link>    links  = getEntryManager().getEntryLinks(request,
                                   entry);
        StringBuilder sb     = new StringBuilder();

        OutputType    output = HtmlOutputHandler.OUTPUT_INFO;
        String treeLink = HtmlUtils.href(
                              request.entryUrl(
                                  getRepository().URL_ENTRY_SHOW, entry,
                                  ARG_OUTPUT,
                                  output.toString()), HtmlUtils.img(
                                      iconUrl(output.getIcon()),
                                      output.getLabel()));


        sb.append(treeLink);
        for (Link link : links) {
            if (link.isType(OutputType.TYPE_TOOLBAR)) {
                String href = HtmlUtils.href(link.getUrl(),
                                             HtmlUtils.img(link.getIcon(),
                                                 link.getLabel()));
                sb.append(HtmlUtils.inset(href, 0, 3, 0, 0));
            }
        }

        return sb.toString();
    }



    /**
     * _more_
     *
     * @param request _more_
     * @param entry _more_
     *
     * @return _more_
     *
     * @throws Exception _more_
     */
    public String getEntryMenubar(Request request, Entry entry)
            throws Exception {


        List<Link> links = getEntryManager().getEntryLinks(request, entry);

        String entryMenu = getEntryManager().getEntryActionsTable(request,
                               entry, OutputType.TYPE_FILE, links, true,
                               null);
        String editMenu = getEntryManager().getEntryActionsTable(request,
                              entry, OutputType.TYPE_EDIT, links, true, null);
        String exportMenu = getEntryManager().getEntryActionsTable(request,
                                entry, OutputType.TYPE_FEEDS, links, true,
                                null);
        String viewMenu = getEntryManager().getEntryActionsTable(request,
                              entry, OutputType.TYPE_VIEW, links, true, null);

        String       categoryMenu = null;
        List<String> menuItems    = new ArrayList<String>();
        String sep =
            HtmlUtils.div("",
                          HtmlUtils.cssClass(CSS_CLASS_MENUBUTTON_SEPARATOR));


        String menuClass = HtmlUtils.cssClass(CSS_CLASS_MENUBUTTON);
        for (Link link : links) {
            if (link.isType(OutputType.TYPE_OTHER)) {
                categoryMenu =
                    getEntryManager().getEntryActionsTable(request, entry,
                        OutputType.TYPE_OTHER, links);
                String categoryName = link.getOutputType().getCategory();
                //HtmlUtils.span(msg(categoryName), menuClass),
                categoryMenu =
                    getPageHandler().makePopupLink(msg(categoryName),
                        categoryMenu.toString(), menuClass, false, true);

                break;
            }
        }



        PageStyle pageStyle = request.getPageStyle(entry);

        /*
          puts these here so we can extract the file names for the .pack files
          msg("File")
          msg("Edit")
          msg("View")
          msg("Connect")
          msg("Data")
         */

        if (pageStyle.okToShowMenu(entry, PageStyle.MENU_FILE)
                && (entryMenu != null)) {
            if (menuItems.size() > 0) {
                menuItems.add(sep);
            }
            String menuName = "File";
            //Do we really want to change the name of the menu based on the entry type?
            if (entry.isGroup()) {
                //                menuName="Folder";
            }
            //HtmlUtils.span(msg(menuName), menuClass), 
            menuItems.add(getPageHandler().makePopupLink(msg(menuName),
                    entryMenu, menuClass, false, true));

        }

        if (pageStyle.okToShowMenu(entry, PageStyle.MENU_EDIT)
                && (editMenu != null)) {
            if (menuItems.size() > 0) {
                menuItems.add(sep);
            }
            menuItems.add(getPageHandler().makePopupLink(msg("Edit"),
                    editMenu, menuClass, false, true));
        }

        if (pageStyle.okToShowMenu(entry, PageStyle.MENU_FEEDS)
                && (exportMenu != null)) {
            if (menuItems.size() > 0) {
                menuItems.add(sep);
            }
            menuItems.add(getPageHandler().makePopupLink(msg("Links"),
                    exportMenu, menuClass, false, true));
        }

        if (pageStyle.okToShowMenu(entry, PageStyle.MENU_VIEW)
                && (viewMenu != null)) {
            if (menuItems.size() > 0) {
                menuItems.add(sep);
            }
            menuItems.add(getPageHandler().makePopupLink(msg("View"),
                    viewMenu, menuClass, false, true));
        }

        if (pageStyle.okToShowMenu(entry, PageStyle.MENU_OTHER)
                && (categoryMenu != null)) {
            if (menuItems.size() > 0) {
                menuItems.add(sep);
            }
            menuItems.add(categoryMenu);
        }

        String leftTable;
        leftTable = HtmlUtils.table(
            HtmlUtils.row(
                HtmlUtils.cols(Misc.listToStringArray(menuItems)),
                " cellpadding=0 cellspacing=0 border=0 "));

        return leftTable;
    }


    /**
     * _more_
     *
     * @param request _more_
     * @param parents _more_
     * @param titleList _more_
     *
     * @return _more_
     *
     * @throws Exception _more_
     */
    public List<String> makeBreadcrumbList(Request request,
                                           List<Entry> parents,
                                           List<String> titleList)
            throws Exception {
        List<String> breadcrumbs = new ArrayList<String>();
        for (Entry ancestor : parents) {
            String name = getEntryDisplayName(ancestor);
            String linkLabel;
            if (breadcrumbs.size() == 0) {
                linkLabel = HtmlUtils.img(getIconUrl(request, ancestor))
                            + " " + name;
            } else {
                linkLabel = HtmlUtils.img(getIconUrl(request, ancestor))
                            + " " + name;
                //                linkLabel =   name;
            }
            if (titleList != null) {
                titleList.add(0, name);
            }

            String url = request.entryUrl(getRepository().URL_ENTRY_SHOW,
                                          ancestor);
            String link = HtmlUtils.href(url, linkLabel);
            breadcrumbs.add(0, link);
        }

        return breadcrumbs;

    }



    /**
     * _more_
     *
     * @param request _more_
     * @param breadcrumbs _more_
     *
     * @return _more_
     */
    public String makeBreadcrumbs(Request request, List<String> breadcrumbs) {
        StringBuilder sb =
            new StringBuilder(
                "<div class=\"breadCrumbHolder module\"><div id=\"breadCrumb0\" class=\"breadCrumb module\"><ul>");

        for (Object crumb : breadcrumbs) {
            sb.append("<li>\n");
            sb.append(crumb);
            sb.append("</li>\n");
        }
        sb.append("</ul></div></div>");
        sb.append(
            HtmlUtils.script(
                JQuery.ready(
                    "jQuery(\"#breadCrumb0\").jBreadCrumb({previewWidth: 5, easing:'swing',endElementsToLeaveOpen: 1});")));

        return sb.toString();
    }

    /** _more_ */
    private Image remoteImage;


    /**
     * _more_
     *
     * @throws Exception _more_
     */
    public void loadResources() throws Exception {
        loadLanguagePacks();
        loadMapRegions();
    }

    /**
     * _more_
     *
     * @param request _more_
     * @param entry _more_
     *
     * @return _more_
     *
     * @throws Exception _more_
     */
    private String getIconUrlInner(Request request, Entry entry)
            throws Exception {
        if (entry.getIcon() != null) {
            return iconUrl(entry.getIcon());
        }
        if (getEntryManager().isAnonymousUpload(entry)) {
            return iconUrl(ICON_ENTRY_UPLOAD);
        }
        if (request.defined(ARG_ICON)) {
            return iconUrl(request.getString(ARG_ICON, ""));
        }

        return entry.getTypeHandler().getIconUrl(request, entry);
    }



    /**
     * _more_
     *
     *
     * @param request _more_
     * @param entry _more_
     *
     * @return _more_
     *
     * @throws Exception _more_
     */
    public String getIconUrl(Request request, Entry entry) throws Exception {
        String iconPath = getIconUrlInner(request, entry);

        if (iconPath == null) {
            return null;
        }
        //Assume absolute urls to remote icons
        if (false && entry.getIsRemoteEntry()) {
            String iconFile = iconPath.replaceAll("/", "_");
            String ext      = IOUtil.getFileExtension(iconFile);
            String newIcon = IOUtil.stripExtension(iconFile) + "_remote"
                             + ext;
            File newIconFile = getStorageManager().getIconsDirFile(newIcon);
            try {
                if ( !newIconFile.exists()) {
                    if (remoteImage == null) {
                        remoteImage = ImageUtils.readImage(
                            "/org/ramadda/repository/htdocs/icons/arrow.png");
                    }
                    //                    System.err.println("    icon path:" + iconFile);
                    //TODO: 
                    System.err.println("Image:" + iconPath);
                    Image originalImage = ImageUtils.readImage(
                    //"/org/ramadda/repository/htdocs/icons/" + iconFile
                    iconPath);
                    if (originalImage != null) {
                        int w = originalImage.getWidth(null);
                        int h = originalImage.getHeight(null);
                        BufferedImage newImage =
                            new BufferedImage(w, h,
                                BufferedImage.TYPE_INT_ARGB);
                        Graphics newG = newImage.getGraphics();
                        newG.drawImage(originalImage, 0, 0, null);
                        newG.drawImage(remoteImage,
                                       w - remoteImage.getWidth(null) - 0,
                                       h - remoteImage.getHeight(null) - 0,
                                       null);
                        ImageUtils.writeImageToFile(newImage,
                                newIconFile.toString());
                        //                        System.err.println("    /repository/icons/" + newIconFile.getName());
                    }
                }

                return "/repository/icons/" + newIconFile.getName();
            } catch (Exception exc) {
                logError("Error reading icon:" + iconPath, exc);

                return iconPath;
            }
        }

        return iconPath;
    }

    /**
     * Function to get share button, ratings and also Numbers of Comments and comments icon getComments(request, entry);
     * This will only be painted if there is a menubar.
     *
     * @param request _more_
     * @param entry _more_
     *
     * @return String with the HTML
     *
     * @throws Exception _more_
     */

    public String entryFooter(Request request, Entry entry) throws Exception {

        if (entry == null) {
            entry = getEntryManager().getTopGroup();
        }
        StringBuilder sb = new StringBuilder();

        String entryUrl =
            HtmlUtils.url(
                request.getAbsoluteUrl(getRepository().URL_ENTRY_SHOW),
                ARG_ENTRYID, entry.getId());



        //Table to enclose this toolbar
        sb.append("<table width=\"100%\"><tr><td>");

        // Comments
        List<Comment> comments = entry.getComments();
        if (comments != null) {
            Link link = new Link(
                            request.entryUrl(
                                getRepository().URL_COMMENTS_SHOW,
                                entry), getRepository().iconUrl(
                                    ICON_COMMENTS), "Add/View Comments",
                                        OutputType.TYPE_TOOLBAR);

            String href = HtmlUtils.href(link.getUrl(),
                                         "Comments:(" + comments.size() + ")"
                                         + HtmlUtils.img(link.getIcon(),
                                             link.getLabel(),
                                             link.getLabel()));


            sb.append(href);

            sb.append("</td><td>");
        }

        /*
          Don't include the sharing from addthis.com for now since I think theyre doing tracking
        String title = getEntryManager().getEntryDisplayName(entry);
        String share =
            "<script type=\"text/javascript\">"
            + "var addthis_disable_flash=\"true\"; addthis_pub=\"jeffmc\";</script>"
            + "<a href=\"http://www.addthis.com/bookmark.php?v=20\" "
            + "onclick=\"return addthis_open(this, '', '" + entryUrl + "', '"
            + title
            + "')\"><img src=\"http://s7.addthis.com/static/btn/lg-share-en.gif\" width=\"125\" height=\"16\" alt=\"Bookmark and Share\" style=\"border:0\"/></a><script type=\"text/javascript\" src=\"http://s7.addthis.com/js/200/addthis_widget.js\"></script>";


        sb.append(share);
        */

        sb.append("</td><td>");


        // Ratings 
        boolean doRatings = getRepository().getProperty(PROP_RATINGS_ENABLE,
                                true);
        if (doRatings) {
            String link = request.url(getRepository().URL_COMMENTS_SHOW,
                                      ARG_ENTRYID, entry.getId());
            String ratings = HtmlUtils.div(
                                 "",
                                 HtmlUtils.cssClass("js-kit-rating")
                                 + HtmlUtils.attr(
                                     HtmlUtils.ATTR_TITLE,
                                     entry.getFullName()) + HtmlUtils.attr(
                                         "permalink",
                                         link)) + HtmlUtils.importJS(
                                             "http://js-kit.com/ratings.js");

            sb.append(
                HtmlUtils.table(
                    HtmlUtils.row(
                        HtmlUtils.col(
                            ratings,
                            HtmlUtils.attr(
                                HtmlUtils.ATTR_ALIGN,
                                HtmlUtils.VALUE_RIGHT)), HtmlUtils.attr(
                                    HtmlUtils.ATTR_VALIGN,
                                    HtmlUtils.VALUE_TOP)), HtmlUtils.attr(
                                        HtmlUtils.ATTR_WIDTH, "100%")));
        } else {
            sb.append(HtmlUtils.p());
        }


        sb.append("</td></tr></table>");

        return sb.toString();
    }




    /**
     * _more_
     *
     * @param request _more_
     * @param entry _more_
     *
     * @return _more_
     *
     * @throws Exception _more_
     */
    public String getCommentHtml(Request request, Entry entry)
            throws Exception {
        boolean canEdit = getAccessManager().canDoAction(request, entry,
                              Permission.ACTION_EDIT);
        boolean canComment = getAccessManager().canDoAction(request, entry,
                                 Permission.ACTION_COMMENT);

        StringBuilder sb = new StringBuilder();
        List<Comment> comments =
            getRepository().getCommentManager().getComments(request, entry);

        if (canComment) {
            sb.append(
                HtmlUtils.href(
                    request.entryUrl(
                        getRepository().URL_COMMENTS_ADD,
                        entry), "Add Comment"));
        }


        if (comments.size() == 0) {
            sb.append("<br>");
            sb.append(msg("No comments"));
        }
        //        sb.append("<table>");
        int rowNum = 1;
        for (Comment comment : comments) {
            //            sb.append(HtmlUtils.formEntry(BLANK, HtmlUtils.hr()));
            //TODO: Check for access
            String deleteLink = ( !canEdit
                                  ? ""
                                  : HtmlUtils.href(request.url(getRepository().URL_COMMENTS_EDIT,
                                      ARG_DELETE, "true", ARG_ENTRYID,
                                      entry.getId(), ARG_AUTHTOKEN,
                                      getRepository().getAuthToken(request.getSessionId()),
                                      ARG_COMMENT_ID,
                                      comment.getId()), HtmlUtils.img(iconUrl(ICON_DELETE),
                                          msg("Delete comment"))));
            if (canEdit) {
                //                sb.append(HtmlUtils.formEntry(BLANK, deleteLink));
            }
            //            sb.append(HtmlUtils.formEntry("Subject:", comment.getSubject()));


            String theClass = HtmlUtils.cssClass("listrow" + rowNum);
            theClass = HtmlUtils.cssClass(CSS_CLASS_COMMENT_BLOCK);
            rowNum++;
            if (rowNum > 2) {
                rowNum = 1;
            }
            StringBuilder content = new StringBuilder();
            String byLine = HtmlUtils.span(
                                "Posted by " + comment.getUser().getLabel(),
                                HtmlUtils.cssClass(
                                    CSS_CLASS_COMMENT_COMMENTER)) + " @ "
                                        + HtmlUtils.span(
                                            getPageHandler().formatDate(
                                                request,
                                                comment.getDate()), HtmlUtils.cssClass(
                                                    CSS_CLASS_COMMENT_DATE)) + HtmlUtils.space(
                                                        1) + deleteLink;
            content.append(
                HtmlUtils.open(
                    HtmlUtils.TAG_DIV,
                    HtmlUtils.cssClass(CSS_CLASS_COMMENT_INNER)));
            content.append(comment.getComment());
            content.append(HtmlUtils.br());
            content.append(byLine);
            content.append(HtmlUtils.close(HtmlUtils.TAG_DIV));
            sb.append(HtmlUtils
                .div(HtmlUtils
                    .makeShowHideBlock(HtmlUtils
                        .span(comment.getSubject(), HtmlUtils
                            .cssClass(CSS_CLASS_COMMENT_SUBJECT)), content
                                .toString(), true, ""), theClass));
        }

        return sb.toString();
    }


    /** _more_ */
    private Hashtable<String, String> typeToWikiTemplate =
        new Hashtable<String, String>();

    /** _more_ */
    public static final String TEMPLATE_DEFAULT = "default";

    /** _more_ */
    public static final String TEMPLATE_CONTENT = "content";



    /**
     * _more_
     */
    @Override
    public void clearCache() {
        super.clearCache();
        templateJavascriptContent = null;
        htmlTemplates             = null;
        defaultTemplate           = null;
        typeToWikiTemplate        = new Hashtable<String, String>();
    }


    /**
     * _more_
     *
     * @param request _more_
     * @param entry _more_
     * @param templateType _more_
     *
     * @return _more_
     *
     * @throws Exception _more_
     */
    public String getWikiTemplate(Request request, Entry entry,
                                  String templateType)
            throws Exception {
        if (entry.isDummy()) {
            return null;
        }
        String entryType = entry.getTypeHandler().getType();
        String key       = entryType + "." + templateType;
        String wiki      = typeToWikiTemplate.get(key);
        if (wiki != null) {
            return wiki;
        }

        String propertyPrefix = "ramadda.wikitemplate." + templateType + ".";
        String property       = getProperty(propertyPrefix + entryType, null);
        if (property != null) {
            wiki = getRepository().getResource(property);
        }
        if (wiki == null) {
            String tmp = propertyPrefix + (entry.isGroup()
                                           ? "folder"
                                           : "file");
            wiki = getRepository().getResource(getProperty(tmp, ""));
        }
        if (wiki != null) {
            typeToWikiTemplate.put(key, wiki);
        }

        return wiki;
    }


    /**
     * _more_
     *
     * @param request _more_
     * @param sb _more_
     * @param cb _more_
     */
    public void doTableLayout(Request request, Appendable sb,
                              CategoryBuffer cb) {

        try {

            sb.append("<table width=100%><tr valign=top>");

            int colCnt = 0;
            for (String cat : cb.getCategories()) {
                String content = cb.get(cat).toString();
                if (content.length() == 0) {
                    continue;
                }
                colCnt++;
                if (colCnt > 4) {
                    sb.append("</tr><tr valign=top>");
                    sb.append("<td colspan=4><hr></td>");
                    sb.append("</tr><tr valign=top>");
                    colCnt = 1;
                }

                sb.append("<td>");
                sb.append(HtmlUtils.b(msg(cat)));
                sb.append(
                    "<div style=\"solid black; max-height: 150px; overflow-y: auto\";>");
                sb.append("<ul>");
                sb.append(content);
                sb.append("</ul>");
                sb.append("</div>");
                sb.append("</td>");

            }
            sb.append("</table>");
        } catch (java.io.IOException ioe) {
            throw new RuntimeException(ioe);
        }
    }

    /**
     * _more_
     *
     * @return _more_
     */
    public boolean showEntryTableCreateDate() {
        return getProperty(PROP_ENTRY_TABLE_SHOW_CREATEDATE, false);
    }


    /**
     * _more_
     *
     * @return _more_
     */
    private List<MapLayer> getMapLayers() {
        if (mapLayers == null) {
            List<MapLayer> tmp = new ArrayList<MapLayer>();
            for (String base :
                    StringUtil.split(
                        getRepository().getProperty(
                            "ramadda.map.extras", ""), ",", true, true)) {
                tmp.addAll(MapLayer.makeLayers(getRepository(), base));
            }
            mapLayers = tmp;
        }

        return mapLayers;
    }



    /**
     * _more_
     *
     * @param request _more_
     * @param mapInfo _more_
     */
    public void addToMap(Request request, MapInfo mapInfo) {
        //        if(mapInfo.forSelection()) return;
        //http://wms.alaskamapped.org/extras?

        List<String> titles = new ArrayList<String>();
        List<String> tabs   = new ArrayList<String>();
        for (MapLayer mapLayer : getMapLayers()) {
            if (Utils.stringDefined(mapLayer.getLegendLabel())) {
                titles.add(mapLayer.getLegendLabel());
                StringBuffer sb = new StringBuffer(mapLayer.getLegendText());
                if (Utils.stringDefined(mapLayer.getLegendImage())) {
                    sb.append(HtmlUtils.img(mapLayer.getLegendImage()));
                }
                tabs.add(HtmlUtils.div(sb.toString(),
                                       HtmlUtils.cssClass("map-legend-div")));
            }
            mapLayer.addToMap(request, mapInfo);
        }

        StringBuffer rightSide = new StringBuffer();
        rightSide.append("<b>Legends</b><br>");
        rightSide.append(OutputHandler.makeTabs(titles, tabs, true));

        mapInfo.addRightSide(
            getPageHandler().makeStickyPopup(
                HtmlUtils.img(getRepository().fileUrl("/icons/map_go.png")),
                rightSide.toString(), null));
        //        mapInfo.addRightSide(HtmlUtils.makeShowHideBlock("", rightSide.toString(),false));
        //        mapInfo.addRightSide(HtmlUtils.makeShowHideBlock("", rightSide.toString(),false));

    }


    /**
     * _more_
     *
     * @param request _more_
     * @param typeHandler _more_
     *
     * @return _more_
     *
     * @throws Exception _more_
     */
    public String makeFileTypeSelector(Request request,
                                       TypeHandler typeHandler)
            throws Exception {
        List<HtmlUtils.Selector> items =
            getEntryManager().getTypeHandlerSelectors(request, true, false,
                null);
        items.add(0, new HtmlUtils.Selector(msg("Find match"),
                                            TypeHandler.TYPE_FINDMATCH,
                                            null));

        return repository.makeTypeSelect(items, request, false,
                                         (typeHandler != null)
                                         ? typeHandler.getType()
                                         : "", false, null);
    }


    /**
     * _more_
     *
     * @param request _more_
     * @param entry _more_
     * @param sb _more_
     * @param title _more_
     *
     * @throws Exception _more_
     */
    public void entrySectionOpen(Request request, Entry entry, Appendable sb,
                                 String title)
            throws Exception {
        entrySectionOpen(request, entry, sb, title, false);
    }

    /**
     * _more_
     *
     * @param request _more_
     * @param entry _more_
     * @param sb _more_
     * @param title _more_
     * @param showLine _more_
     *
     * @throws Exception _more_
     */
    public void entrySectionOpen(Request request, Entry entry, Appendable sb,
                                 String title, boolean showLine)
            throws Exception {
        sb.append(HtmlUtils.sectionOpen(null, showLine));
        if (entry != null) {
            sb.append(HtmlUtils.h2(entry.getName() + ": " + msg(title)));
        } else {
            sb.append(HtmlUtils.h2(msg(title)));
        }

    }

    /**
     * _more_
     *
     * @param request _more_
     * @param entry _more_
     * @param sb _more_
     *
     * @throws Exception _more_
     */
    public void entrySectionClose(Request request, Entry entry, Appendable sb)
            throws Exception {
        sb.append(HtmlUtils.sectionClose());
    }


    /**
     *
     *
     * @param request _more_
     * @param sb _more_
     *
     * @throws Exception _more_
     */
    public void addGoogleJSImport(Request request, Appendable sb)
            throws Exception {
        if (request.getExtraProperty("googlejsapi") == null) {
            request.putExtraProperty("googlejsapi", "added");
            sb.append(
                HtmlUtils.importJS(
                    getRepository().getProperty("ramadda.google.js", "")));
        }
    }




}
