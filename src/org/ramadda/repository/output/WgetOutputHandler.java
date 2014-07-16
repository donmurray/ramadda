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


import org.ramadda.repository.Entry;
import org.ramadda.repository.Link;
import org.ramadda.repository.Repository;
import org.ramadda.repository.Request;
import org.ramadda.repository.Result;
import org.ramadda.repository.auth.AuthorizationMethod;

import org.ramadda.util.HtmlUtils;

import org.w3c.dom.Element;

import ucar.unidata.util.IOUtil;
import ucar.unidata.util.Misc;


import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;



/**
 * Produces a wget script to download files
 *
 *
 * @author RAMADDA Development Team
 * @version $Revision: 1.3 $
 */
public class WgetOutputHandler extends OutputHandler {


    /** The WGET output type */
    public static final OutputType OUTPUT_CURL =
        new OutputType("Bulk Download Script", "bulk.curl",
                       OutputType.TYPE_FILE, "", ICON_FETCH);

    /** _more_          */
    public static final OutputType OUTPUT_WGET =
        new OutputType("Bulk Download Script", "bulk.wget",
                       OutputType.TYPE_FILE, "", ICON_FETCH);


    /** _more_ */
    public static final String ARG_RECURSE = "recurse";

    /** _more_ */
    public static final String ARG_COMMAND = "command";

    /** _more_ */
    public static final String COMMAND_WGET = "wget";

    /** _more_ */
    public static final String COMMAND_CURL = "curl";

    /**
     * Create a wget output handler
     *
     * @param repository  the repository
     * @param element     the XML definition
     * @throws Exception  problem creating the handler
     */
    public WgetOutputHandler(Repository repository, Element element)
            throws Exception {
        super(repository, element);
        addType(OUTPUT_CURL);
    }




    /**
     * Get the authorization method
     *
     * @param request  the request
     *
     * @return  the authorization method
     */
    public AuthorizationMethod getAuthorizationMethod(Request request) {
        return AuthorizationMethod.AUTH_HTTP;
    }

    /**
     * Get the entry links
     *
     * @param request  the Request
     * @param state    the State
     * @param links    the list of links to add to
     *
     * @throws Exception  problem generating links
     */
    public void getEntryLinks(Request request, State state, List<Link> links)
            throws Exception {
        if (state.entry != null) {
            if (state.entry.getResource().isUrl()
                    || getAccessManager().canDownload(request, state.entry)) {
                links.add(
                    makeLink(
                        request, state.entry, OUTPUT_CURL,
                        "/" + IOUtil.stripExtension(state.entry.getName())
                        + "_download.sh"));
            }
        } else {
            boolean ok = false;
            for (Entry child : state.getAllEntries()) {
                //For now add the bulk download link to any folder entry, even if it doesn't have file children
                ok = true;
                if (ok) {
                    break;
                }
                /*
                if (child.getResource().isUrl()
                        || getAccessManager().canDownload(request, child)) {
                    ok = true;

                    break;
                }
                    */
            }



            if (ok) {
                //Maybe don't put this for the top level entries. 
                //Somebody will invariably come along and try to fetch everything
                if (state.group != null) {
                    if ( !state.group.isTopEntry()) {
                        links.add(
                            makeLink(
                                request, state.group, OUTPUT_CURL,
                                "/"
                                + IOUtil.stripExtension(
                                    state.group.getName()) + "_download.sh"));
                    }
                } else {
                    links.add(makeLink(request, state.group, OUTPUT_CURL));
                }
            }
        }
    }




    /**
     * Output the entry
     *
     * @param request   the request
     * @param outputType  the output type
     * @param entry     the entry
     *
     * @return the Result
     *
     * @throws Exception on badness
     */
    public Result outputEntry(Request request, OutputType outputType,
                              Entry entry)
            throws Exception {
        request.setReturnFilename(IOUtil.stripExtension(entry.getName())
                                  + "_download.sh");

        return outputGroup(request, outputType, null, new ArrayList<Entry>(),
                           (List<Entry>) Misc.newList(entry));
    }


    /**
     * Output a group of entries
     *
     * @param request    the Request
     * @param outputType the output type
     * @param group      the group (may be null)
     * @param subGroups  the list of subgroups (may be null)
     * @param entries    the list of entries
     *
     * @return  the result
     *
     * @throws Exception  problem creating the script
     */
    public Result outputGroup(Request request, OutputType outputType,
                              Entry group, List<Entry> subGroups,
                              List<Entry> entries)
            throws Exception {

        if ((group != null) && group.isDummy()) {
            request.setReturnFilename("Search_Results_download.sh");
        }

        StringBuffer sb = new StringBuffer();
        System.err.println("RAMADDA: wget server name: "
                           + request.getHttpServletRequest().getServerName()
                           + " getServerPort:"
                           + request.getHttpServletRequest().getServerPort()
                           + " getLocalPort:"
                           + request.getHttpServletRequest().getLocalPort());
        boolean recurse = request.get(ARG_RECURSE, true);
        subGroups.addAll(entries);
        process(request, sb, group, subGroups, recurse);

        return new Result("", sb, getMimeType(OUTPUT_CURL));
    }

    /**
     * _more_
     *
     * @param request _more_
     * @param sb _more_
     * @param group _more_
     * @param entries _more_
     * @param recurse _more_
     *
     * @throws Exception _more_
     */
    public void process(Request request, StringBuffer sb, Entry group,
                        List<Entry> entries, boolean recurse)
            throws Exception {
        String command   = request.getString(ARG_COMMAND, COMMAND_CURL);
        String outputArg = command.equals(COMMAND_WGET)
                           ? "-O"
                           : command.equals(COMMAND_CURL)
                             ? "-o"
                             : "";
        for (Entry entry : entries) {
            if (entry.isGroup()) {
                if ( !recurse) {
                    continue;
                }
                List<Entry> subEntries =
                    getEntryManager().getChildrenAll(request, entry);
                String dirName = IOUtil.cleanFileName(entry.getName());
                if (dirName.length() == 0) {
                    dirName = entry.getId();
                }
                System.err.println("dirname:" + dirName);
                if (subEntries.size() > 0) {
                    sb.append("if ! test -e " + qt(dirName) + " ; then \n");
                    sb.append(cmd("mkdir " + qt(dirName)));
                    sb.append("fi\n");
                    sb.append(cmd("cd " + qt(dirName)));
                    process(request, sb, entry, subEntries, recurse);
                    sb.append(cmd("cd .."));
                }
            }

            if (entry.getResource().isUrl()) {
                sb.append(cmd(command + " "
                              + qt(entry.getResource().getPath())));

                continue;
            } else if ( !getAccessManager().canDownload(request, entry)) {
                continue;
            }
            String tail = getStorageManager().getFileTail(entry);
            String path = request.getAbsoluteUrl(
                              getEntryManager().getEntryResourceUrl(
                                  request, entry));

            path = HtmlUtils.urlEncodeSpace(path);
            String tmpFile = tail + ".tmp";
            sb.append("if ! test -e " + qt(tail) + " ; then \n");
            sb.append(cmd(command + " " + outputArg + " " + qt(tmpFile) + " "
                          + qt(path)));
            sb.append("if [[ $? != 0 ]] ; then\n");
            sb.append(cmd("echo" + " "
                          + qt("file download failed for " + tail)));
            sb.append("exit $?\n");
            sb.append("fi\n");
            sb.append(cmd("mv " + qt(tmpFile) + " " + qt(tail)));
            sb.append("else\n");
            sb.append(cmd("echo " + qt("File " + tail + " already exists")));
            sb.append("fi\n");
        }
    }

    /**
     * _more_
     *
     * @param s _more_
     *
     * @return _more_
     */
    private String cmd(String s) {
        return s + ";\n";
    }

    /**
     * _more_
     *
     * @param s _more_
     *
     * @return _more_
     */
    private String qt(String s) {
        return "\"" + s + "\"";
    }

    /**
     * Get the MIME type for this output handler
     *
     * @param output  the output type
     *
     * @return  the MIME type
     */
    public String getMimeType(OutputType output) {
        return "application/x-sh";
    }



}
