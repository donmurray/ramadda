/*
 * Copyright 1997-2010 Unidata Program Center/University Corporation for
 * Atmospheric Research, P.O. Box 3000, Boulder, CO 80307,
 * support@unidata.ucar.edu.
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
 */

package org.ramadda.repository;



import ucar.unidata.util.HtmlUtil;

import java.text.SimpleDateFormat;

import java.util.ArrayList;
import java.util.Date;
import java.util.Enumeration;
import java.util.GregorianCalendar;
import java.util.Hashtable;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.TimeZone;
import java.util.UUID;


/**
 *
 *
 * @author IDV Development Team
 * @version $Revision: 1.3 $
 */
public class RepositoryBase implements Constants, RepositorySource {


    /** _more_ */
    public final RequestUrl URL_HELP = new RequestUrl(this, "/help/toc.html");

    /** _more_ */
    public final RequestUrl URL_PING = new RequestUrl(this, "/ping");


    /** _more_ */
    public final RequestUrl URL_SSLREDIRECT = new RequestUrl(this,
                                                  "/sslredirect");

    /** _more_ */
    public final RequestUrl URL_INFO = new RequestUrl(this, "/info");

    /** _more_ */
    public final RequestUrl URL_MESSAGE = new RequestUrl(this, "/message");

    /** _more_ */
    public final RequestUrl URL_DUMMY = new RequestUrl(this, "/dummy");

    /** _more_ */
    public final RequestUrl URL_INSTALL = new RequestUrl(this, "/install");


    /** _more_ */
    public final RequestUrl URL_REGISTRY_ADD = new RequestUrl(this,
                                                   "/registry/add");

    /** _more_ */
    public final RequestUrl URL_REGISTRY_LIST = new RequestUrl(this,
                                                    "/registry/list");

    /** _more_ */
    public final RequestUrl URL_REGISTRY_INFO = new RequestUrl(this,
                                                    "/registry/info");



    /** _more_ */
    public final RequestUrl URL_COMMENTS_SHOW = new RequestUrl(this,
                                                    "/entry/comments/show");

    /** _more_ */
    public final RequestUrl URL_COMMENTS_ADD = new RequestUrl(this,
                                                   "/entry/comments/add");

    /** _more_ */
    public final RequestUrl URL_COMMENTS_EDIT = new RequestUrl(this,
                                                    "/entry/comments/edit");


    /** _more_ */
    public final RequestUrl URL_ENTRY_XMLCREATE = new RequestUrl(this,
                                                      "/entry/xmlcreate");


    /** _more_          */
    public final RequestUrl URL_ENTRY_IMPORT = new RequestUrl(this,
                                                   "/entry/import");

    /** _more_          */
    public final RequestUrl URL_ENTRY_EXPORT = new RequestUrl(this,
                                                   "/entry/export");


    /** _more_ */
    public final RequestUrl URL_ASSOCIATION_ADD = new RequestUrl(this,
                                                      "/association/add");

    /** _more_ */
    public final RequestUrl URL_ASSOCIATION_DELETE =
        new RequestUrl(this, "/association/delete");

    /** _more_ */
    public final RequestUrl URL_LIST_HOME = new RequestUrl(this,
                                                "/list/home");

    /** _more_ */
    public final RequestUrl URL_LIST_SHOW = new RequestUrl(this,
                                                "/list/show");

    /** _more_ */
    public final RequestUrl URL_GRAPH_VIEW = new RequestUrl(this,
                                                 "/graph/view");

    /** _more_ */
    public final RequestUrl URL_GRAPH_GET = new RequestUrl(this,
                                                "/graph/get");

    /** _more_ */
    public final RequestUrl URL_ENTRY_SHOW = new RequestUrl(this,
                                                 "/entry/show", "View Entry");

    /** _more_ */
    public final RequestUrl URL_ENTRY_COPY = new RequestUrl(this,
                                                 "/entry/copy");


    /** _more_ */
    public final RequestUrl URL_ENTRY_DELETE = new RequestUrl(this,
                                                   "/entry/delete", "Delete");

    /** _more_ */
    public final RequestUrl URL_ENTRY_DELETELIST = new RequestUrl(this,
                                                       "/entry/deletelist");


    /** _more_ */
    public final RequestUrl URL_ACCESS_FORM = new RequestUrl(this,
                                                  "/access/form", "Access");


    /** _more_ */
    public final RequestUrl URL_ACCESS_CHANGE = new RequestUrl(this,
                                                    "/access/change");

    /** _more_ */
    public final RequestUrl URL_ENTRY_CHANGE = new RequestUrl(this,
                                                   "/entry/change");

    /** _more_ */
    public final RequestUrl URL_ENTRY_FORM = new RequestUrl(this,
                                                 "/entry/form", "Edit Entry");


    /** _more_ */
    public final RequestUrl URL_ENTRY_NEW = new RequestUrl(this,
                                                "/entry/new", "New Entry");

    /** _more_ */
    public final RequestUrl URL_ENTRY_UPLOAD = new RequestUrl(this,
                                                   "/entry/upload",
                                                   "Upload a file");


    /** _more_ */
    public final RequestUrl URL_ENTRY_GETENTRIES = new RequestUrl(this,
                                                       "/entry/getentries");

    /** _more_ */
    public final RequestUrl URL_ENTRY_GET = new RequestUrl(this,
                                                "/entry/get");


    /** _more_ */
    public final RequestUrl URL_USER_LOGIN = new RequestUrl(this,
                                                 "/user/login");


    /** _more_ */
    public final RequestUrl URL_USER_FAVORITE = new RequestUrl(this,
                                                    "/user/favorite");

    /** _more_ */
    public final RequestUrl URL_USER_ACTIVITY = new RequestUrl(this,
                                                    "/user/activity");

    /** _more_ */
    public final RequestUrl URL_USER_RESETPASSWORD =
        new RequestUrl(this, "/user/resetpassword");

    /** _more_ */
    public final RequestUrl URL_USER_FINDUSERID = new RequestUrl(this,
                                                      "/user/finduserid");


    /** _more_ */
    public final RequestUrl URL_USER_LOGOUT = new RequestUrl(this,
                                                  "/user/logout");


    /** _more_ */
    public final RequestUrl URL_USER_HOME = new RequestUrl(this,
                                                "/user/home", "User Home");

    /** _more_ */
    public final RequestUrl URL_USER_PROFILE = new RequestUrl(this,
                                                   "/user/profile",
                                                   "User Profile");

    /** _more_ */
    public final RequestUrl URL_USER_SETTINGS = new RequestUrl(this,
                                                    "/user/settings",
                                                    "Settings");

    /** _more_ */
    public final RequestUrl URL_USER_MONITORS = new RequestUrl(this,
                                                    "/user/monitors",
                                                    "Monitors");

    /** _more_ */
    public final RequestUrl URL_USER_CART = new RequestUrl(this,
                                                "/user/cart", "Data Cart");

    /** _more_ */
    public final RequestUrl URL_USER_LIST = new RequestUrl(this,
                                                "/user/list", "Users");

    /** _more_ */
    public final RequestUrl URL_USER_EDIT = new RequestUrl(this,
                                                "/user/edit", "Users");

    /** _more_ */
    public final RequestUrl URL_USER_NEW = new RequestUrl(this, "/user/new");

    /** _more_          */
    public static final TimeZone TIMEZONE_UTC = TimeZone.getTimeZone("UTC");
    


    /** _more_ */
    public static final GregorianCalendar calendar =
        new GregorianCalendar(TIMEZONE_UTC);


    /** _more_ */
    private static String urlBase = "/repository";


    /** _more_ */
    private String hostname = "";

    private String ipAddress = "";

    /** _more_ */
    private int httpPort = 80;

    /** _more_ */
    private int httpsPort = -1;

    /** _more_ */
    private boolean clientMode = false;



    /** _more_ */
    public static final String DEFAULT_TIME_FORMAT = "yyyy-MM-dd HH:mm:ss z";

    /** _more_ */
    public static final String DEFAULT_TIME_SHORTFORMAT =
        "yyyy/MM/dd HH:mm z";

    /** _more_ */
    public static final String DEFAULT_TIME_THISYEARFORMAT =
        "yyyy/MM/dd HH:mm z";


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
    protected List<SimpleDateFormat> formats;

    /**
     * _more_
     */
    public RepositoryBase() {}


    /**
     * _more_
     *
     * @param port _more_
     *
     * @throws Exception _more_
     */
    public RepositoryBase(int port) throws Exception {
        this.httpPort = port;
    }



    /**
     * _more_
     *
     * @param date _more_
     *
     * @return _more_
     */
    public String formatYYYYMMDD(Date date) {
        synchronized(dateSdf) {
            return dateSdf.format(date);
        }
    }

    /**
     *     _more_
     *
     *     @return _more_
     */
    public String getGUID() {
        return UUID.randomUUID().toString();
    }



    /**
     * _more_
     *
     * @return _more_
     */
    protected long currentTime() {
        return new Date().getTime();

    }


    /** _more_ */
    private Hashtable<String, SimpleDateFormat> dateFormats =
        new Hashtable<String, SimpleDateFormat>();

    /** _more_ */
    TimeZone defaultTimeZone;


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
        synchronized(dateFormat) {
            return dateFormat.format(d);
        }
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
        if (formats == null) {
            formats = new ArrayList<SimpleDateFormat>();
            formats.add(makeSDF("yyyy-MM-dd HH:mm:ss z"));
            formats.add(makeSDF("yyyy-MM-dd HH:mm:ss"));
            formats.add(makeSDF("yyyy-MM-dd HH:mm"));
            formats.add(makeSDF("yyyy-MM-dd"));
        }


        for (SimpleDateFormat fmt : formats) {
            try {
                synchronized(fmt) {
                    return fmt.parse(dttm);
                }
            } catch (Exception noop) {}
        }
        throw new IllegalArgumentException("Unable to parse date:" + dttm);
    }

    /**
     * _more_
     *
     * @param url _more_
     *
     * @return _more_
     */
    public String absoluteUrl(String url) {
        return "http://" + getHostname() + ":" + getPort() + url;
    }



    /**
     * _more_
     *
     * @param requestUrl _more_
     */
    public void initRequestUrl(RequestUrl requestUrl) {}

    /**
     * _more_
     *
     * @param requestUrl _more_
     *
     * @return _more_
     */
    public String getUrlPath(RequestUrl requestUrl) {
        return getUrlBase() + requestUrl.getPath();
    }

    /**
     * _more_
     *
     * @return _more_
     */
    protected int getHttpsPort() {
        return httpsPort;
    }


    /**
     * _more_
     *
     * @param port _more_
     */
    protected void setHttpsPort(int port) {
        httpsPort = port;
    }



    /**
     * _more_
     *
     * @param url _more_
     *
     * @return _more_
     */
    public String httpsUrl(String url) {
        int port = getHttpsPort();
        if (port < 0) {
            return "http://" + getHostname() + ":" + getPort() + url;
            //            return url;
            //            throw new IllegalStateException("Do not have ssl port defined");
        }
        if (port == 0) {
            return "https://" + getHostname() + url;
        } else {
            return "https://" + getHostname() + ":" + port + url;
        }
    }

    /**
     * _more_
     *
     * @param name _more_
     * @param dflt _more_
     *
     * @return _more_
     */
    public String getProperty(String name, String dflt) {
        return dflt;
    }




    /**
     * _more_
     *
     * @return _more_
     */
    public RepositoryBase getRepositoryBase() {
        return this;
    }

    /**
     * Set the Hostname property.
     *
     * @param value The new value for Hostname
     */
    public void setHostname(String value) {
        hostname = value;
    }

    public void setIpAddress(String ip) {
        ipAddress= ip;
    }

    public String getIpAddress() {
        return ipAddress;
    }

    /**
     * Get the Hostname property.
     *
     * @return The Hostname
     */
    public String getHostname() {
        return hostname;
    }

    /**
     * Set the Port property.
     *
     * @param value The new value for Port
     */
    public void setPort(int value) {
        httpPort = value;
    }

    /**
     * Get the Port property.
     *
     * @return The Port
     */
    public int getPort() {
        return httpPort;
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
        s = HtmlUtil.entityEncode(s);
        s = s.replace("&#60;msg&#32;", "<msg ");
        s = s.replace("&#32;msg&#62;", " msg>");
        s = s.replace("&#32;", " ");
        s = s.replace("&#60;p&#62;", "<p>");
        s = s.replace("&#60;br&#62;", "<br>");
        s = s.replace("&#38;nbsp&#59;", "&nbsp;");
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
            HtmlUtil.jsLink(HtmlUtil.onMouseClick("hide('messageblock')"),
                            HtmlUtil.img(iconUrl(Constants.ICON_CLOSE)));
        if ( !showClose) {
            html = "&nbsp;";
        }
        h = "<div class=\"innernote\"><table cellspacing=\"0\" cellpadding=\"0\" border=\"0\"><tr><td valign=\"top\">"
            + HtmlUtil.img(iconUrl(icon)) + HtmlUtil.space(2)
            + "</td><td valign=\"bottom\"><span class=\"notetext\">" + h
            + "</span></td></tr></table></div>";
        return "\n<table border=\"0\" id=\"messageblock\"><tr><td><div class=\"note\"><table><tr valign=top><td>"
               + h + "</td><td>" + html + "</td></tr></table>"
               + "</div></td></tr></table>\n";
    }



    /**
     * _more_
     *
     * @param f _more_
     *
     * @return _more_
     */
    public static String fileUrl(String f) {
        return urlBase + f;
    }

    /**
     * _more_
     *
     * @param f _more_
     *
     * @return _more_
     */
    public String iconUrl(String f) {
        if (f == null) {
            return null;
        }
        String path = getProperty(f, f);
        return urlBase + path;
    }


    /**
     * Set the UrlBase property.
     *
     * @param value The new value for UrlBase
     */
    public void setUrlBase(String value) {
        urlBase = value;
    }

    /**
     * Get the UrlBase property.
     *
     * @return The UrlBase
     */
    public static String getUrlBase() {
        return urlBase;
    }




}
