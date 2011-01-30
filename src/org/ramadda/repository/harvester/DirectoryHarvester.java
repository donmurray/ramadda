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

package org.ramadda.repository.harvester;


import org.w3c.dom.*;

import org.ramadda.repository.*;
import org.ramadda.repository.type.*;

import ucar.unidata.sql.SqlUtil;
import ucar.unidata.util.DateUtil;
import ucar.unidata.util.HtmlUtil;
import ucar.unidata.util.HttpServer;
import ucar.unidata.util.IOUtil;
import ucar.unidata.util.LogUtil;
import ucar.unidata.util.Misc;


import ucar.unidata.util.StringUtil;
import ucar.unidata.util.TwoFacedObject;
import ucar.unidata.xml.XmlUtil;

import java.io.File;

import java.lang.reflect.*;



import java.net.*;


import java.text.SimpleDateFormat;

import java.util.ArrayList;
import java.util.Date;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.List;
import java.util.Properties;



import java.util.regex.*;


/**
 * Class SqlUtil _more_
 *
 *
 * @author IDV Development Team
 * @version $Revision: 1.3 $
 */
public class DirectoryHarvester extends Harvester {


    /**
     * _more_
     *
     * @param repository _more_
     */
    public DirectoryHarvester(Repository repository) {
        super(repository);
    }

    /**
     * _more_
     *
     * @param repository _more_
     * @param id _more_
     *
     * @throws Exception _more_
     */
    public DirectoryHarvester(Repository repository, String id)
            throws Exception {
        super(repository, id);
    }


    /**
     * _more_
     *
     * @param repository _more_
     * @param element _more_
     *
     * @throws Exception _more_
     */
    public DirectoryHarvester(Repository repository, Element element)
            throws Exception {
        super(repository, element);
    }


    public String getDescription() {
        return "Make folders from directory tree";
    }

    /**
     * _more_
     *
     * @param request _more_
     * @param sb _more_
     *
     * @throws Exception _more_
     */
    public void createEditForm(Request request, StringBuffer sb)
            throws Exception {
        sb.append(HtmlUtil.formEntry(msgLabel("Harvester name"),
                                     HtmlUtil.input(ARG_NAME, getName(),
                                         HtmlUtil.SIZE_40)));
        sb.append(
            HtmlUtil
                .formEntry(
                    msgLabel("Run"),
                    HtmlUtil
                        .checkbox(
                            ATTR_ACTIVEONSTART, "true",
                            getActiveOnStart()) + HtmlUtil.space(1)
                                + msg("Active on startup")
                                + HtmlUtil.space(3)
                                + HtmlUtil
                                    .checkbox(
                                        ATTR_MONITOR, "true",
                                        getMonitor()) + HtmlUtil.space(1)
                                            + msg("Monitor")
                                            + HtmlUtil.space(3)
                                            + msgLabel("Sleep")
                                            + HtmlUtil.space(1)
                                            + HtmlUtil
                                                .input(ATTR_SLEEP,
                                                    "" + getSleepMinutes(),
                                                        HtmlUtil
                                                            .SIZE_5) + HtmlUtil
                                                                .space(1) + msg(
                                                                    "(minutes)")));

        String root = (rootDir != null)
                      ? rootDir.toString()
                      : "";
        root = root.replace("\\", "/");
        String extraLabel = "";
        if ((rootDir != null) && !rootDir.exists()) {
            extraLabel = HtmlUtil.space(2)
                         + HtmlUtil.bold("Directory does not exist");
        }
        sb.append(
            RepositoryManager.tableSubHeader("Walk the directory tree"));
        sb.append(HtmlUtil.formEntry(msgLabel("Under directory"),
                                     HtmlUtil.input(ATTR_ROOTDIR, root,
                                         HtmlUtil.SIZE_60) + extraLabel));

        sb.append(
            RepositoryManager.tableSubHeader("Create new folders under"));

        addBaseGroupSelect(ATTR_BASEGROUP, sb);



    }


    /**
     * _more_
     *
     *
     * @param timestamp _more_
     * @throws Exception _more_
     */
    protected void runInner(int timestamp) throws Exception {
        if ( !canContinueRunning(timestamp)) {
            return;
        }
        Entry baseGroup = getBaseGroup();
        if (baseGroup == null) {
            baseGroup = getEntryManager().getTopGroup();
        }
        walkTree(rootDir, baseGroup);
    }


    /**
     * _more_
     *
     * @param dir _more_
     * @param parentGroup _more_
     *
     * @throws Exception _more_
     */
    protected void walkTree(File dir, Entry parentGroup) throws Exception {
        String name = dir.getName();
        File xmlFile = new File(IOUtil.joinDir(dir.getParentFile(),
                           "." + name + ".ramadda"));
        Entry fileInfoEntry = getEntryManager().getTemplateEntry(dir);
        Entry group =
            getEntryManager().findGroupFromName(parentGroup.getFullName()
                + "/" + name, getUser(), false);
        if (group == null) {
            group = getEntryManager().makeNewGroup(parentGroup, name,
                    getUser(), fileInfoEntry);
        }
        File[] files = dir.listFiles();
        for (int i = 0; i < files.length; i++) {
            if (files[i].isDirectory()) {
                walkTree(files[i], group);
            }
        }
    }

}
