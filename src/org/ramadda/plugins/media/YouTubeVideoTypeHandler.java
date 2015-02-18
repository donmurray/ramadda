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

package org.ramadda.plugins.media;


import org.ramadda.repository.*;
import org.ramadda.repository.metadata.*;
import org.ramadda.repository.type.*;

import org.ramadda.util.HtmlUtils;


import org.w3c.dom.*;

import ucar.unidata.util.IOUtil;

import ucar.unidata.util.Misc;
import ucar.unidata.util.StringUtil;


import ucar.unidata.xml.XmlUtil;

import java.io.*;

import java.util.Date;
import java.util.Hashtable;
import java.util.List;


/**
 *
 *
 */
public class YouTubeVideoTypeHandler extends GenericTypeHandler {

    /** _more_ */
    private static int IDX = 0;


    /** _more_ */
    public static final int IDX_ID = IDX++;


    /** _more_ */
    public static final int IDX_WIDTH = IDX++;

    /** _more_ */
    public static final int IDX_HEIGHT = IDX++;

    /** _more_ */
    public static final int IDX_START = IDX++;

    /** _more_ */
    public static final int IDX_END = IDX++;

    /** _more_ */
    public static final int IDX_DISPLAY = IDX++;

    /** _more_ */
    private int idCnt = 0;

    /**
     * _more_
     *
     * @param repository _more_
     * @param entryNode _more_
     *
     * @throws Exception _more_
     */
    public YouTubeVideoTypeHandler(Repository repository, Element entryNode)
            throws Exception {
        super(repository, entryNode);
    }

    /**
     * _more_
     *
     * @param request _more_
     * @param props _more_
     * @param entry _more_
     *
     * @return _more_
     *
     * @throws Exception _more_
     */
    @Override
    public String getSimpleDisplay(Request request, Hashtable props,
                                   Entry entry)
            throws Exception {

        String  sdisplay = entry.getValue(IDX_DISPLAY, "true");
        boolean display  = (sdisplay.length() == 0)
                           ? true
                           : Misc.equals("true", sdisplay);
        if ( !display) {
            return null;
        }

        StringBuffer sb = new StringBuffer();
        sb.append(getWikiManager().wikifyEntry(request, entry,
                DFLT_WIKI_HEADER));

        String url = entry.getResource().getPath();
        String id  = entry.getValue(IDX_ID, (String) null);
        //For legacy entries
        if (id == null) {
            id = StringUtil.findPattern(url, "v=([^&]+)&");
        }
        if (id == null) {
            id = StringUtil.findPattern(url, "v=([^&]+)");
        }
        if (id == null) {
            id = StringUtil.findPattern(url, "youtu.be/([^&]+)");
        }

        if (id == null) {
            return getPageHandler().showDialogError(
                "Could not find ID in YouTube URL");
        }





        String width  = entry.getValue(IDX_WIDTH, "640");
        String height = entry.getValue(IDX_HEIGHT, "390");
        double start  = entry.getValue(IDX_START, 0.0);
        double end    = entry.getValue(IDX_END, -1);
        sb.append("\n");
        sb.append(
            "<iframe id=\"ytplayer\" type=\"text/html\" frameborder=\"0\" ");
        sb.append(XmlUtil.attr("width", width));
        sb.append(XmlUtil.attr("height", height));
        String playerId = "video_" + (idCnt++);
        String embedUrl = "http://www.youtube.com/embed/" + id;
        embedUrl += "?enablejsapi=1";
        embedUrl += "&autoplay=0";
        embedUrl += "&playerapiid=" + playerId;
        if (start > 0) {
            embedUrl += "&start=" + ((int) (start * 60));
        }
        if (end > 0) {
            embedUrl += "&end=" + ((int) (end * 60));
        }

        sb.append("\n");
        sb.append("src=\"" + embedUrl + "\"");
        sb.append(">\n");
        sb.append("</iframe>\n");

        sb.append(HtmlUtils.br());
        sb.append(HtmlUtils.href(url, url));

        List<Metadata> metadataList =
            getMetadataManager().findMetadata(request, entry, "video_cue",
                false);
        if ((metadataList != null) && (metadataList.size() > 0)) {
            StringBuffer links = new StringBuffer();
            for (Metadata metadata : metadataList) {
                String name   = metadata.getAttr1();
                String offset = metadata.getAttr2().trim();
                if (offset.length() == 0) {
                    continue;
                }
                links.append(HtmlUtils.href("javascript:cueVideo("
                                            + HtmlUtils.squote(playerId)
                                            + "," + offset + ");", name
                                                + " -- " + offset
                                                    + " minutes"));
                links.append(HtmlUtils.br());
            }
            StringBuffer embed = sb;
            sb = new StringBuffer();

            sb.append(
                HtmlUtils.importJS("http://www.youtube.com/player_api"));
            StringBuffer js = new StringBuffer("\n");
            js.append(
                "function onYouTubePlayerAPIReady(id) {/*alert(id);*/}\n");
            js.append("function cueVideo (id,minutes) {\n");
            js.append("player = document.getElementById('ytplayer');");
            js.append("alert(player.playVideo);");
            js.append("player.playVideo();");
            js.append("}\n");
            sb.append("\n");
            sb.append(HtmlUtils.script(js.toString()));
            sb.append("<table cellspacing=5><tr valign=top><td>");
            sb.append(embed);
            sb.append("</td><td>");
            sb.append(links);
            sb.append("</td></tr></table>");
        }

        return sb.toString();

    }



    /**
     * _more_
     *
     * @param request _more_
     * @param entry _more_
     *
     * @throws Exception _more_
     */
    @Override
    public void initializeNewEntry(Request request, Entry entry)
            throws Exception {
        super.initializeNewEntry(request, entry);

        String url = entry.getResource().getPath();
        String id  = StringUtil.findPattern(url, "v=([^&]+)&");
        if (id == null) {
            id = StringUtil.findPattern(url, "v=([^&]+)");
        }
        if (id == null) {
            id = StringUtil.findPattern(url, "youtu.be/([^&]+)");
        }
        if (id != null) {
            entry.setValue(IDX_ID, id);
        }

        String html  = IOUtil.readContents(url, "");
        String title = StringUtil.findPattern(html, "<title>(.*)</title>");
        if (title == null) {
            title = StringUtil.findPattern(html, "<TITLE>(.*)</TITLE>");
        }
        if (title != null) {
            title = title.replace("- YouTube", "");
            entry.setName(title);
        }


        if (id != null) {
            String thumbUrl = "https://i.ytimg.com/vi/" + id + "/default.jpg";
            System.err.println(thumbUrl);
            try {
                File f = getStorageManager().getTmpFile(request,
                             "youtubethumb.jpg");
                InputStream  is =
                    getStorageManager().getInputStream(thumbUrl);
                OutputStream fos = getStorageManager().getFileOutputStream(f);
                try {
                    IOUtil.writeTo(is, fos);
                    f = getStorageManager().moveToEntryDir(entry, f);
                    entry.addMetadata(new Metadata(getRepository().getGUID(),
                            entry.getId(),
                            ContentMetadataHandler.TYPE_THUMBNAIL, false,
                            f.getName(), null, null, null, null));

                } finally {
                    IOUtil.close(fos);
                    IOUtil.close(is);
                }
            } catch (Exception exc) {
                System.err.println("Error fetching youtube thumbnail:"
                                   + thumbUrl);
            }
        }
    }




    /**
     * _more_
     *
     * @param request _more_
     * @param entry _more_
     * @param wikiTemplate _more_
     *
     * @return _more_
     *
     * @throws Exception _more_
     */
    @Override
    public String getInnerWikiContent(Request request, Entry entry,
                                      String wikiTemplate)
            throws Exception {
        return getSimpleDisplay(request, null, entry);
    }


    /**
     * _more_
     *
     * @param args _more_
     */
    public static void main(String[] args) {
        String pattern = "^http://www.youtube.com/watch\\?v=.*";
        String url =
            "http://www.youtube.com/watch?v=sOU2WXaDEs0&feature=g-vrec";
        System.err.println(url.matches(pattern));
    }

}
