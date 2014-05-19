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
import ucar.unidata.util.StringUtil;
import ucar.unidata.util.Misc;
import ucar.unidata.xml.XmlUtil;



import java.io.FileOutputStream;
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

    private String output = "default.csv";

    private String fields = "name,url";

    private boolean download = false;

    private boolean overwrite = false;

    private boolean showMetadata = false;

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

    private void doSearch(List<String> args) throws Exception {
        RequestUrl URL_ENTRY_SEARCH = new RequestUrl(this, "/search/do",
                                          "Search");

        List<String> argList = new ArrayList<String>();
        for(int i=0;i<args.size();i++) {
            String arg = args.get(i);
            if(arg.equals("-text")) {
                argList.add(ARG_TEXT);
                argList.add(args.get(++i));
            } else if(arg.equals("-type")) {
                argList.add(ARG_TYPE);
                argList.add(args.get(++i));
            } else if(arg.equals("-daterange")) {
                argList.add("datadate.from");
                argList.add(args.get(++i));
                argList.add("datadate.to");
                argList.add(args.get(++i));
            } else if(arg.equals("-mindate")) {
                argList.add("datadate.from");
                argList.add(args.get(++i));
            } else if(arg.equals("-maxdate")) {
                argList.add("datadate.to");
                argList.add(args.get(++i));
            } else if(arg.equals("-bounds")) {
                argList.add(ARG_AREA_NORTH);
                argList.add(args.get(++i));
                argList.add(ARG_AREA_WEST);
                argList.add(args.get(++i));
                argList.add(ARG_AREA_SOUTH);
                argList.add(args.get(++i));
                argList.add(ARG_AREA_EAST);
                argList.add(args.get(++i));
            } else if(arg.equals("-tag")) {
                argList.add("metadata.attr1.enum_tag");
                argList.add(args.get(++i));
            } else if(arg.equals("-keyword")) {
                argList.add("metadata.attr1.content.keyword");
                argList.add(args.get(++i));
            } else if(arg.equals("-variable")) {
                argList.add("metadata.attr1.thredds.variable");
                argList.add(args.get(++i));
            } else if(arg.equals("-fields")) {
                fields = args.get(++i);
                output = "default.csv";
            } else if(arg.equals("-overwrite")) {
                overwrite = true;
            } else if(arg.equals("-download")) {
                download = true;
                fields = "name,url";
                output = "default.csv";
            } else if(arg.equals("-help")) {
                usage("");
            } else if(arg.equals("-output")) {
                output = args.get(++i);
                if(output.equals("wget")) {
                    output = "wget.wget";
                } else if(output.equals("csv")) {
                    output = "default.csv";
                }  else if(output.equals("metadata")) {
                    output = "xml.xml";
                    showMetadata = true;
                }  else if(output.equals("name")) {
                    output = "default.csv";
                    fields = "name";
                }
            } else {
                usage("Unknown arg:" + arg);
            }
        }

        argList.add("fields");
        argList.add(fields);

        //        argList.add(args[0]);
        //argList.add(args[1]);
        checkSession();
        argList.add(ARG_OUTPUT);
        argList.add(output);
        argList.add(ARG_SESSIONID);
        argList.add(getSessionId());
        String url = HtmlUtils.url(URL_ENTRY_SEARCH.getFullUrl(), argList);
        String xml = IOUtil.readContents(url, getClass());

        if(download) {
            handleDownload(xml);
            return;
        }

        if(showMetadata) {
            handleMetadata(xml);
            return;
        }

        System.out.println(xml);
    }


    private void handleMetadata(String xml) throws Exception {
        Element root = XmlUtil.getRoot(xml);
        System.err.println (XmlUtil.toString(root));
        List children = XmlUtil.findChildren(root, "entry");
        if(children.size() == 0) {
            System.err.println("no results found:\n" + XmlUtil.toString(root));
        }
        for (int i = 0; i < children.size(); i++) {
            Element entry = (Element) children.get(i);
            System.out.println("name: " + XmlUtil.getAttribute(entry,"name",""));
            System.out.println("path: " + XmlUtil.getAttribute(entry,"path",""));
            System.out.println("\tcreate date: " + XmlUtil.getAttribute(entry,"createdate",""));
            System.out.println("\tfrom date: " + XmlUtil.getAttribute(entry,"fromdate",""));
            System.out.println("\tto date: " + XmlUtil.getAttribute(entry,"todate",""));
            System.out.println("\tfile size: " + XmlUtil.getAttribute(entry,"filesize","0"));
            List mdts = XmlUtil.findChildren(entry, "metadata");
            for (int j = 0; j < mdts.size(); j++) {
                Element mdt = (Element) mdts.get(j);
                String type = XmlUtil.getAttribute(mdt,"type","");
                System.out.println("\tmetadata: " + type);
                List attrs = XmlUtil.findChildren(mdt, "attr");
                for (int k = 0; k < attrs.size(); k++) {
                    Element attr = (Element) attrs.get(k);
                    String childText = new String(RepositoryUtil.decodeBase64(XmlUtil.getChildText(attr)));
                    System.out.println("\t\tattr[" + k +"]="  + childText);
                }

            }            
        }
        
    }


    private void handleDownload(String csv) throws Exception {
        List<String> lines = StringUtil.split(csv,"\n", true,true);
        for(int i=0;i<lines.size();i++) {
            
            if(i == 0) continue;
            String line = lines.get(i);
            //            System.err.println("line:" + line);
            List<String> toks = StringUtil.splitUpTo(line,",",2);
            if(toks.size() != 2) {
                continue;
            }
            String name   = toks.get(0);
            File f = new File(name);
            if(f.exists()) {
                if(!overwrite) {
                    System.err.println ("Not overwriting:" + f);
                    continue;
                }
                System.err.println ("Overwriting:" + f);
            }
            String url   = toks.get(1);
            InputStream inputStream = IOUtil.getInputStream(url, getClass());
            FileOutputStream fos = new FileOutputStream(f);
            IOUtil.writeTo(inputStream, fos);
            IOUtil.close(fos);
            IOUtil.close(inputStream);
            System.err.println ("downloaded:" + name);
        }
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
                password = args[++i];
            } else {
                argList.add(args[i]);
            }
        }


        try {
            RepositorySearch client = new RepositorySearch(new URL(repository),
                                                           user,password);

            client.doSearch(argList);
            String[] msg = { "" };
            /*            if ( !client.isValidSession(true, msg)) {
                System.err.println("Error: invalid session:" + msg[0]);
                return;
                }*/
        } catch (Exception exc) {
            System.err.println("Error:" + exc);
            exc.printStackTrace();
            System.exit(1);
        }
        System.exit(0);

    }


    /**
     * _more_
     *
     * @param msg _more_
     */
    public static void usage(String msg) {
        System.err.println(msg);
        System.err.println(
            "Usage: RepositorySearch -repository <server url> -user <user id> <password> -output <csv|wget|name|metadata|...>  \n-fields <comma separated list of fields to output - e.g. name, url, size, id, lat, lon, ....>\n-download -overwrite \n-text <search text>  \n-type <entry type> \n-variable <var name> \n-tag <tag> \n-keyword <keyword> \n-bounds <north> <west> <south> <east> \n-daterange <startdate yyyy-MM-dd> <todate yyyy-MM-dd> \n-mindate <startdate yyyy-MM-dd> \n-maxdate <startdate yyyy-MM-dd> ");
        System.exit(1);
    }



}
