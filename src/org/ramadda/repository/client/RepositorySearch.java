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

package org.ramadda.repository.client;


import org.ramadda.repository.RepositoryBase;
import org.ramadda.repository.RepositoryUtil;
import org.ramadda.repository.RequestUrl;
import org.ramadda.repository.util.ServerInfo;
import org.ramadda.util.HtmlUtils;

import org.w3c.dom.Document;
import org.w3c.dom.Element;

import ucar.unidata.ui.HttpFormEntry;
import ucar.unidata.util.IOUtil;
import ucar.unidata.util.Misc;
import ucar.unidata.xml.XmlUtil;


import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;

import java.net.URL;

import java.util.ArrayList;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;



/**
 *
 *
 * @author RAMADDA Development Team
 */
public class RepositorySearch extends RepositoryClient  {

    /**
     * _more_
     */
    public RepositorySearch() {
    }

    /**
     * _more_
     *
     * @param serverUrl _more_
     * @param user _more_
     * @param password _more_
     *
     * @throws Exception _more_
     */
    public RepositorySearch(URL serverUrl, String user, String password)
            throws Exception {
        super(serverUrl, user, password);
    }




    /**
     * _more_
     *
     * @param hostname _more_
     * @param port _more_
     * @param base _more_
     *
     * @throws Exception _more_
     */
    public RepositorySearch(String hostname, int port, String base)
            throws Exception {
        this(hostname, port, base, "", "");
    }

    /**
     * _more_
     *
     * @param hostname _more_
     * @param port _more_
     * @param base _more_
     * @param user _more_
     * @param password _more_
     *
     * @throws Exception _more_
     */
    public RepositorySearch(String hostname, int port, String base,
                            String user, String password)
            throws Exception {
        super(hostname, port, base, user, password);
    }

    /**
     * _more_
     *
     * @param args _more_
     *
     * @throws Exception _more_
     */
    public static void main(String[] args) throws Exception {
        String repository = System.getenv(PROP_REPOSITORY);
        String user = System.getenv(PROP_USER);
        String password = System.getenv(PROP_PASSWORD);
        if(repository == null) {
            repository = "http://localhost/repository";
        }
        if(user == null) {
            user = "";
        }
        if(password == null) {
            password = "";
        }

        List<String> argList = new ArrayList<String>();
        for(int i=0;i<args.length;i++)  {
            if(args[i].startsWith("-repos")) {
                repository = args[++i];
            } else if(args[i].equals("-user")) {
                user = args[++i];
            } else if(args[i].equals("-password")) {
                password = args[++i];
            } else {
            }
        }


        try {
            RepositorySearch client = new RepositorySearch(new URL(args[0]),
                                                           args[1], args[2]);
            client.preProcessArgs(args);
            String[] msg = { "" };
            if ( !client.isValidSession(true, msg)) {
                System.err.println("Error: invalid session:" + msg[0]);

                return;
            }

            client.processCommandLine(args);
        } catch (Exception exc) {
            System.err.println("Error:" + exc);
            exc.printStackTrace();
        }

    }


    /**
     * _more_
     *
     * @param msg _more_
     */
    public static void usage(String msg) {
        System.err.println(msg);
        System.err.println(
            "Usage: RepositorySearch <server url> <user id> <password> <arguments>");
        System.err.println(
            "e.g,  RepositoryClient http://localhost:8080/repository <user id> <password> <arguments>");
        System.err.println("Where arguments are:\nFor fetching: \n"
                + argLine(CMD_PRINT, "<entry id> Create and print the given entry")
                + argLine(CMD_PRINTXML, " <entry id> Print out the xml for the given entry id")
                + argLine(CMD_FETCH, "<entry id> <destination file or directory>")
                + argLine(CMD_SEARCH, "<any number of search arguments pairs>")
                + "\n" + "For creating a new folder:\n"
                + argLine(CMD_FOLDER, "<folder name> <parent folder id (see below)>")
                + "\n" + "For uploading files:\n"
                + argLine(CMD_IMPORT, "entries.xml <parent entry id or path>")
                + "\n"
                + argLine(CMD_FILE, "<entry name> <file to upload> <parent folder id (see below)>")
                + "\n"
                + argLine(CMD_FILES, "<parent folder id (see below)> <one or more files to upload>")
                + "\n"
                + argLine(CMD_TIMEOUT, "<timeout in seconds for server info and login attempts>")
                + "\n"
                + "The following arguments get applied to the previously created folder or file:\n"
                + "\t-description <entry description>\n"
                + "\t-attach <file to attach>\n"
                + "\t-addmetadata (Add full metadata to entry)\n"
                + "\t-addshortmetadata (Add spatial/temporal metadata to entry)\n"
                + "\n" + "Miscellaneous:\n"
                + "\t-url (login to server and access url)\n"
                + "\t-debug (print out the generated xml)\n"
                + "\t-exit (exit without adding anything to the repository\n");


        System.err.println(
            "Note: the  <parent folder id> can be an identifier from a existing folder in the repository or it can be \"previous\" which will use the id of the previously specified folder\n"
            + "For example you could do:\n"
            + " ...  -folder \"Some new folder\" \"some id from the repository\" -file \"\" somefile1.nc -file somefile2.nc \"previous\" -folder \"some other folder\" \"previous\" -file \"\" someotherfile.nc \"previous\"\n" + "This results in the heirarchy:\n" + "Some new folder\n" + "\tsomefile1.nc\n" + "\tsomefile2.nc\n" + "\tsome other folder\n" + "\t\tsomeotherfile.nc\n");

        System.exit(1);
    }




}
