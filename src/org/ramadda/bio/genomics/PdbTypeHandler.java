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

package org.ramadda.bio.genomics;


import org.ramadda.repository.*;
import org.ramadda.repository.metadata.*;
import org.ramadda.repository.type.*;
import org.ramadda.util.Utils;


import org.w3c.dom.*;

import ucar.unidata.util.IOUtil;

import ucar.unidata.util.StringUtil;


import ucar.unidata.xml.XmlUtil;

import java.io.*;

import java.util.ArrayList;

import java.util.Date;
import java.util.Hashtable;
import java.util.List;

import javax.mail.*;
import javax.mail.internet.*;


/**
 *
 *
 */
public class PdbTypeHandler extends GenericTypeHandler {



    /**
     * _more_
     *
     * @param repository _more_
     * @param entryNode _more_
     *
     * @throws Exception _more_
     */
    public PdbTypeHandler(Repository repository, Element entryNode)
            throws Exception {
        super(repository, entryNode);
    }


    /**
     * _more_
     *
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

        //If the file for the entry does not exist then return
        if ( !entry.isFile()) {
            return;
        }

        List<String>        titles    = new ArrayList<String>();
        List<String>        remarks   = new ArrayList<String>();
        List<String>        keywords  = new ArrayList<String>();
        List<StringBuilder> compounds = new ArrayList<StringBuilder>();
        List<StringBuilder> sources   = new ArrayList<StringBuilder>();
        List<String>        authors   = new ArrayList<String>();
        InputStream is =
            Utils.doMakeInputStream(entry.getResource().getPath(), true);
        BufferedReader br  = new BufferedReader(new InputStreamReader(is));
        int            cnt = 0;
        while (true) {
            cnt++;
            String line = br.readLine();
            if (line == null) {
                break;
            }
            if (line.startsWith("TITLE ")) {
                //                System.err.println (line);
                titles.add(
                    line.substring("TITLE ".length()).replaceAll(
                        "^\\s*[0-9]+", ""));
            } else if (line.startsWith("REMARK ")) {
                //                System.err.println (line);
                String remark = line.substring("REMARK ".length()).trim();
                int    idx    = remark.indexOf(" ");
                if (idx > 0) {
                    remark = remark.substring(idx + 1);
                } else {
                    continue;
                }
                remarks.add(remark);
            } else if (line.startsWith("EXPDATA ")) {
                entry.addMetadata(new Metadata(getRepository().getGUID(),
                        entry.getId(), "bio_method", true,
                        line.substring("EXPDATA ".length()), "", "", "", ""));

            } else if (line.startsWith("KEYWDS ")) {
                keywords.addAll(
                    StringUtil.split(
                        line.substring("KEYWDS ".length()).trim(), ",", true,
                        true));
            } else if (line.startsWith("COMPND ")) {
                String tmp = read(line, "COMPND ");
                if (tmp.startsWith("MOL_ID") || (compounds.size() == 0)) {
                    compounds.add(new StringBuilder());
                }
                compounds.get(compounds.size() - 1).append(tmp);
            } else if (line.startsWith("SOURCE ")) {
                String tmp = read(line, "SOURCE ");
                if (tmp.startsWith("MOL_ID") || (sources.size() == 0)) {
                    sources.add(new StringBuilder());
                }
                sources.get(sources.size() - 1).append(tmp);
            } else if (line.startsWith("AUTHOR ")) {
                authors.addAll(
                    StringUtil.split(
                        line.substring("AUTHOR ".length()).trim(), ",", true,
                        true));
            }

        }

        for (StringBuilder compound : compounds) {
            Hashtable<String, String> map = getMap(compound);
            entry.addMetadata(new Metadata(getRepository().getGUID(),
                                           entry.getId(), "bio_pdb_compound",
                                           true, map.get("MOL_ID"),
                                           map.get("MOLECULE"),
                                           map.get("CHAIN"),
                                           map.get("ENGINEERED"), ""));
        }

        for (StringBuilder source : sources) {
            Hashtable<String, String> map = getMap(source);
            entry.addMetadata(new Metadata(getRepository().getGUID(),
                                           entry.getId(), "bio_pdb_source",
                                           true, map.get("MOL_ID"),
                                           map.get("ORGANISM_SCIENTIFIC"),
                                           map.get("ORGANISM_COMMON"),
                                           map.get("ORGANISM_TAXID"), ""));
        }

        for (String word : authors) {
            entry.addMetadata(new Metadata(getRepository().getGUID(),
                                           entry.getId(), "bio_author", true,
                                           word, "", "", "", ""));
        }


        for (String word : keywords) {
            entry.addMetadata(new Metadata(getRepository().getGUID(),
                                           entry.getId(), "content.keyword",
                                           true, word, "", "", "", ""));
        }
        if (titles.size() > 0) {
            entry.setName(StringUtil.join(" ", titles));
        }
        if (remarks.size() > 0) {
            StringBuilder desc =
                new StringBuilder(
                    "<pre><div style=\"max-height: 300px; overflow-y:auto;\">");
            desc.append(StringUtil.join("\n", remarks));
            if (desc.length() > Entry.MAX_DESCRIPTION_LENGTH - 20) {
                desc = new StringBuilder(
                    desc.substring(Entry.MAX_DESCRIPTION_LENGTH - 20));
            }
            desc.append("</div></pre>");
            entry.setDescription(desc.toString());
        }
        /*
          entry.getFile().toString();
          Object[] values = getEntryValues(entry);
        */


    }


    /**
     * _more_
     *
     * @param sb _more_
     *
     * @return _more_
     */
    private Hashtable<String, String> getMap(StringBuilder sb) {
        Hashtable<String, String> map = new Hashtable<String, String>();
        for (String tok : StringUtil.split(sb.toString(), ";", true, true)) {
            List<String> toks = StringUtil.splitUpTo(tok, ":", 2);
            map.put(toks.get(0), (toks.size() < 2)
                                 ? ""
                                 : toks.get(1));
        }

        return map;
    }


    /**
     * _more_
     *
     * @param line _more_
     * @param prefix _more_
     *
     * @return _more_
     */
    private String read(String line, String prefix) {
        String tmp = line.substring(prefix.length()).trim();
        tmp = tmp.replaceAll("^[0-9]+", "");

        return tmp;
    }

}
