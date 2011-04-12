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


import ucar.unidata.sql.SqlUtil;
import ucar.unidata.util.DateUtil;

import ucar.unidata.util.HtmlUtil;
import ucar.unidata.util.IOUtil;


import ucar.unidata.util.JobManager;
import ucar.unidata.util.LogUtil;
import ucar.unidata.util.Misc;
import ucar.unidata.util.StringUtil;
import ucar.unidata.util.TwoFacedObject;
import ucar.unidata.xml.XmlUtil;

import java.io.File;


import java.util.ArrayList;
import java.util.Date;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.List;
import java.util.Properties;




/**
 * Class TypeHandler _more_
 *
 *
 * @author IDV Development Team
 * @version $Revision: 1.3 $
 */
public class ActionManager extends RepositoryManager {


    /** _more_ */
    public RequestUrl URL_STATUS = new RequestUrl(this, "/status");


    /** _more_ */
    private Hashtable<Object, ActionInfo> actions = new Hashtable<Object,
                                                        ActionInfo>();


    /**
     * _more_
     *
     * @param repository _more_
     */
    public ActionManager(Repository repository) {
        super(repository);
    }



    /**
     * _more_
     *
     * @param actionId _more_
     * @param html _more_
     */
    public void setContinueHtml(Object actionId, String html) {
        if (actionId == null) {
            return;
        }
        ActionInfo action = getAction(actionId);
        if (action != null) {
            action.setContinueHtml(html);
        }
    }


    /**
     * _more_
     *
     * @param request _more_
     *
     * @return _more_
     *
     * @throws Exception _more_
     */
    public Result processStatus(Request request) throws Exception {

        String       id     = request.getString(ARG_ACTION_ID, "");
        ActionInfo   action = getAction(id);
        StringBuffer sb     = new StringBuffer();
        if (action == null) {
            sb.append(msg("No action found"));
            return new Result(msg("Status"), sb);
        }

        sb.append(msgHeader("Action: " + action.getName()));
        if (request.exists(ARG_CANCEL)) {
            action.setRunning(false);
            actions.remove(id);
            sb.append(msg("Action canceled"));
            JobManager.getManager().stopLoad(id);
        } else {
            if (action.getError() != null) {
                sb.append(getRepository().showDialogError(msg("Error")
                        + "<p>" + action.getError()));
                actions.remove(id);
            } else if ( !action.getRunning()) {
                sb.append(getRepository().showDialogNote(msg("Completed")));
                sb.append(action.getContinueHtml());
                actions.remove(id);
            } else {
                sb.append("<meta http-equiv=\"refresh\" content=\"2\">");
                sb.append(getRepository().progress(msg("In progress")));
                sb.append(HtmlUtil.href(request.url(URL_STATUS,
                        ARG_ACTION_ID, id), msg("Reload")));
                sb.append("<p>");
                if(action.getExtraHtml()!=null) {
                    sb.append(action.getExtraHtml());
                }
                String msg = JobManager.getManager().getDialogLabel2(id);
                if (msg != null) {
                    sb.append(msg);
                } else {
                    sb.append(action.getMessage());
                }
                sb.append("<p>");
                sb.append(request.form(URL_STATUS));
                sb.append(HtmlUtil.submit(msg("Cancel Action"), ARG_CANCEL));
                sb.append(HtmlUtil.hidden(ARG_ACTION_ID, id));
                sb.append(HtmlUtil.formClose());
            }
        }
        Result result = new Result(msg("Status"), sb);
        if(action.entry!=null) {
            return getEntryManager().addEntryHeader(request, action.entry, result);
        }
        return result;
    }






    /**
     * _more_
     *
     * @param id _more_
     *
     * @return _more_
     */
    protected ActionInfo getAction(Object id) {
        if (id == null) {
            return null;
        }
        return actions.get(id);
    }


    /**
     * _more_
     *
     * @param id _more_
     *
     * @return _more_
     */
    public  boolean getActionOk(Object id) {
        ActionInfo action = getAction(id);
        if (action == null) {
            return false;
        }
        return action.getRunning();
    }


    /**
     * _more_
     *
     * @param id _more_
     * @param msg _more_
     */
    public void setActionMessage(Object id, String msg) {
        ActionInfo action = getAction(id);
        if (action == null) {
            return;
        }
        action.setMessage(msg);
    }


    /**
     * _more_
     *
     * @param id _more_
     */
    public void actionComplete(Object id) {
        ActionInfo action = getAction(id);
        if (action == null) {
            return;
        }
        action.setRunning(false);
    }

    /**
     * _more_
     *
     * @param actionId _more_
     * @param exc _more_
     */
    public void handleError(Object actionId, Exception exc) {
        ActionInfo action = getAction(actionId);
        if (action == null) {
            return;
        }
        action.setError("An error has occurred:" + exc);
    }

    /**
     * _more_
     *
     * @param msg _more_
     * @param continueHtml _more_
     *
     * @return _more_
     */
    private Object addAction(String msg, String continueHtml, Entry entry) {

        String id = getRepository().getGUID();
        actions.put(id, new ActionInfo(msg, continueHtml, entry));
        return id;
    }


    /**
     * _more_
     *
     * @param request _more_
     * @param runnable _more_
     * @param name _more_
     * @param continueHtml _more_
     *
     * @return _more_
     */
    public Result doAction(Request request, final Action runnable,
                              String name, String continueHtml) {
        return doAction(request, runnable, name, continueHtml, null);
    }

    public Result doAction(Request request, final Action runnable,
                           String name, String continueHtml, Entry entry) {
        Object actionId = runAction(runnable, name, continueHtml, entry);
        return new Result(request.url(URL_STATUS, ARG_ACTION_ID,
                                      "" + actionId));
    }


    /**
     * _more_
     *
     * @param runnable _more_
     * @param name _more_
     * @param continueHtml _more_
     *
     * @return _more_
     */
    protected Object runAction(final Action runnable, String name,
                               String continueHtml) {
        return runAction(runnable, name, continueHtml, null);
    }

    protected Object runAction(final Action runnable, String name,
                               String continueHtml, Entry entry) {
        final Object actionId = addAction(name, continueHtml, entry);
        Misc.run(new Runnable() {
            public void run() {
                try {
                    runnable.run(actionId);
                } catch (Exception exc) {
                    //TODO: handle the error better
                    exc.printStackTrace();
                    handleError(actionId, exc);
                    return;
                }
                actionComplete(actionId);
            }
        });
        return actionId;
    }


    /**
     * Action _more_
     *
     *
     * @author IDV Development Team
     * @version $Revision: 1.3 $
     */
    public static interface Action {

        /**
         * _more_
         *
         * @param actionId _more_
         *
         * @throws Exception _more_
         */
        public void run(Object actionId) throws Exception;
    }


    /**
     * Class ActionInfo _more_
     *
     *
     * @author IDV Development Team
     * @version $Revision: 1.3 $
     */
    public  class ActionInfo {

        private String id;

        /** _more_ */
        private String name;

        /** _more_ */
        private boolean running = true;

        /** _more_ */
        private String message = "";

        /** _more_ */
        private String continueHtml;

        /** _more_ */
        private String error = null;


        private String extraHtml;

        private Entry entry;

        /**
         * _more_
         *
         * @param name _more_
         * @param continueHtml _more_
         */
        public ActionInfo(String name, String continueHtml, Entry entry) {
            this.name         = name;
            this.continueHtml = continueHtml;
            this.id = getRepository().getGUID();
            this.entry = entry;
        }


        /**
           Set the Id property.

           @param value The new value for Id
        **/
        public void setId (String value) {
            id = value;
        }

        /**
           Get the Id property.

           @return The Id
        **/
        public String getId () {
            return id;
        }



        /**
         *  Set the Name property.
         *
         *  @param value The new value for Name
         */
        public void setName(String value) {
            name = value;
        }

        /**
         *  Get the Name property.
         *
         *  @return The Name
         */
        public String getName() {
            return name;
        }

        /**
         *  Set the Running property.
         *
         *  @param value The new value for Running
         */
        public void setRunning(boolean value) {
            running = value;
        }

        /**
         *  Get the Running property.
         *
         *  @return The Running
         */
        public boolean getRunning() {
            return running;
        }



        /**
         * Set the Message property.
         *
         * @param value The new value for Message
         */
        public void setMessage(String value) {
            message = value;
        }

        /**
         * Get the Message property.
         *
         * @return The Message
         */
        public String getMessage() {
            return message;
        }


        /**
           Set the ExtraHtml property.

           @param value The new value for ExtraHtml
        **/
        public void setExtraHtml (String value) {
            extraHtml = value;
        }

        /**
           Get the ExtraHtml property.

           @return The ExtraHtml
        **/
        public String getExtraHtml () {
            return extraHtml;
        }


        /**
         * Set the ContinueHtml property.
         *
         * @param value The new value for ContinueHtml
         */
        public void setContinueHtml(String value) {
            continueHtml = value;
        }

        /**
         * Get the ContinueHtml property.
         *
         * @return The ContinueHtml
         */
        public String getContinueHtml() {
            return continueHtml;
        }

        /**
         * Set the Error property.
         *
         * @param value The new value for Error
         */
        public void setError(String value) {
            error = value;
        }

        /**
         * Get the Error property.
         *
         * @return The Error
         */
        public String getError() {
            return error;
        }




    }



}
