/*
* Copyright 2008-2011 Jeff McWhirter/ramadda.org
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

package org.ramadda.repository.database;


import ucar.unidata.sql.SqlUtil;


/**
 *
 *
 *
 * @author RAMADDA Development Team
 * @version $Revision: 1.3 $
 */
public class Tables {

    /**
     * Class ENTRIES _more_
     *
     *
     * @author RAMADDA Development Team
     */
    public static class ENTRIES {

        /** _more_ */
        public static final String NAME = "entries";

        /** _more_ */
        public static final String COL_ID = NAME + ".id";

        /** _more_ */
        public static final String COL_TYPE = NAME + ".type";

        /** _more_ */
        public static final String COL_NAME = NAME + ".name";

        /** _more_ */
        public static final String COL_DESCRIPTION = NAME + ".description";

        /** _more_ */
        public static final String COL_PARENT_GROUP_ID = NAME
                                                         + ".parent_group_id";

        /** _more_ */
        public static final String COL_USER_ID = NAME + ".user_id";

        /** _more_ */
        public static final String COL_RESOURCE = NAME + ".resource";

        /** _more_ */
        public static final String COL_RESOURCE_TYPE = NAME
                                                       + ".resource_type";

        /** _more_ */
        public static final String COL_MD5 = NAME + ".md5";

        /** _more_          */
        public static final String COL_FILESIZE = NAME + ".filesize";

        /** _more_ */
        public static final String COL_DATATYPE = NAME + ".datatype";

        /** _more_ */
        public static final String COL_CREATEDATE = NAME + ".createdate";

        /** _more_          */
        public static final String COL_CHANGEDATE = NAME + ".changedate";

        /** _more_ */
        public static final String COL_FROMDATE = NAME + ".fromdate";

        /** _more_ */
        public static final String COL_TODATE = NAME + ".todate";

        /** _more_ */
        public static final String COL_SOUTH = NAME + ".south";

        /** _more_ */
        public static final String COL_NORTH = NAME + ".north";

        /** _more_ */
        public static final String COL_EAST = NAME + ".east";

        /** _more_ */
        public static final String COL_WEST = NAME + ".west";

        /** _more_          */
        public static final String COL_ALTITUDETOP = NAME + ".altitudetop";

        /** _more_          */
        public static final String COL_ALTITUDEBOTTOM = NAME
                                                        + ".altitudebottom";

        /** _more_ */
        public static final String[] ARRAY = new String[] {
            COL_ID, COL_TYPE, COL_NAME, COL_DESCRIPTION, COL_PARENT_GROUP_ID,
            COL_USER_ID, COL_RESOURCE, COL_RESOURCE_TYPE, COL_MD5,
            COL_FILESIZE, COL_DATATYPE, COL_CREATEDATE, COL_CHANGEDATE,
            COL_FROMDATE, COL_TODATE, COL_SOUTH, COL_NORTH, COL_EAST,
            COL_WEST, COL_ALTITUDETOP, COL_ALTITUDEBOTTOM
        };

        /** _more_ */
        public static final String UPDATE = SqlUtil.makeUpdate(NAME, COL_ID,
                                                ARRAY);

        /** _more_ */
        public static final String COLUMNS = SqlUtil.comma(ARRAY);

        /** _more_ */
        public static final String NODOT_COLUMNS = SqlUtil.commaNoDot(ARRAY);

        /** _more_ */
        public static final String INSERT =
            SqlUtil.makeInsert(NAME, NODOT_COLUMNS,
                               SqlUtil.getQuestionMarks(ARRAY.length));

    }

    ;

    /**
     * Class ANCESTORS _more_
     *
     *
     * @author RAMADDA Development Team
     */
    public static class ANCESTORS {

        /** _more_ */
        public static final String NAME = "ancestors";

        /** _more_ */
        public static final String COL_ID = NAME + ".id";

        /** _more_ */
        public static final String COL_ANCESTOR_ID = NAME + ".ancestor_id";

        /** _more_ */
        public static final String[] ARRAY = new String[] { COL_ID,
                COL_ANCESTOR_ID };


        /** _more_ */
        public static final String COLUMNS = SqlUtil.comma(ARRAY);

        /** _more_ */
        public static final String NODOT_COLUMNS = SqlUtil.commaNoDot(ARRAY);

        /** _more_ */
        public static final String INSERT =
            SqlUtil.makeInsert(NAME, NODOT_COLUMNS,
                               SqlUtil.getQuestionMarks(ARRAY.length));

    }

    ;

    /**
     * Class METADATA _more_
     *
     *
     * @author RAMADDA Development Team
     */
    public static class METADATA {

        /** _more_ */
        public static final String NAME = "metadata";

        /** _more_ */
        public static final String COL_ID = NAME + ".id";

        /** _more_ */
        public static final String COL_ENTRY_ID = NAME + ".entry_id";

        /** _more_ */
        public static final String COL_TYPE = NAME + ".type";

        /** _more_ */
        public static final String COL_INHERITED = NAME + ".inherited";

        /** _more_ */
        public static final String COL_ATTR1 = NAME + ".attr1";

        /** _more_ */
        public static final String COL_ATTR2 = NAME + ".attr2";

        /** _more_ */
        public static final String COL_ATTR3 = NAME + ".attr3";

        /** _more_ */
        public static final String COL_ATTR4 = NAME + ".attr4";

        /** _more_ */
        public static final String COL_EXTRA = NAME + ".extra";

        /** _more_ */
        public static final String[] ARRAY = new String[] {
            COL_ID, COL_ENTRY_ID, COL_TYPE, COL_INHERITED, COL_ATTR1,
            COL_ATTR2, COL_ATTR3, COL_ATTR4, COL_EXTRA
        };


        /** _more_ */
        public static final String COLUMNS = SqlUtil.comma(ARRAY);

        /** _more_ */
        public static final String NODOT_COLUMNS = SqlUtil.commaNoDot(ARRAY);

        /** _more_ */
        public static final String INSERT =
            SqlUtil.makeInsert(NAME, NODOT_COLUMNS,
                               SqlUtil.getQuestionMarks(ARRAY.length));

    }

    ;

    /**
     * Class COMMENTS _more_
     *
     *
     * @author RAMADDA Development Team
     */
    public static class COMMENTS {

        /** _more_ */
        public static final String NAME = "comments";

        /** _more_ */
        public static final String COL_ID = NAME + ".id";

        /** _more_ */
        public static final String COL_ENTRY_ID = NAME + ".entry_id";

        /** _more_ */
        public static final String COL_USER_ID = NAME + ".user_id";

        /** _more_ */
        public static final String COL_DATE = NAME + ".date";

        /** _more_ */
        public static final String COL_SUBJECT = NAME + ".subject";

        /** _more_ */
        public static final String COL_COMMENT = NAME + ".comment";

        /** _more_ */
        public static final String[] ARRAY = new String[] {
            COL_ID, COL_ENTRY_ID, COL_USER_ID, COL_DATE, COL_SUBJECT,
            COL_COMMENT
        };


        /** _more_ */
        public static final String COLUMNS = SqlUtil.comma(ARRAY);

        /** _more_ */
        public static final String NODOT_COLUMNS = SqlUtil.commaNoDot(ARRAY);

        /** _more_ */
        public static final String INSERT =
            SqlUtil.makeInsert(NAME, NODOT_COLUMNS,
                               SqlUtil.getQuestionMarks(ARRAY.length));

    }

    ;

    /**
     * Class ASSOCIATIONS _more_
     *
     *
     * @author RAMADDA Development Team
     */
    public static class ASSOCIATIONS {

        /** _more_ */
        public static final String NAME = "associations";

        /** _more_ */
        public static final String COL_ID = NAME + ".id";

        /** _more_ */
        public static final String COL_NAME = NAME + ".name";

        /** _more_ */
        public static final String COL_TYPE = NAME + ".type";

        /** _more_ */
        public static final String COL_FROM_ENTRY_ID = NAME
                                                       + ".from_entry_id";

        /** _more_ */
        public static final String COL_TO_ENTRY_ID = NAME + ".to_entry_id";

        /** _more_ */
        public static final String[] ARRAY = new String[] { COL_ID, COL_NAME,
                COL_TYPE, COL_FROM_ENTRY_ID, COL_TO_ENTRY_ID };


        /** _more_ */
        public static final String COLUMNS = SqlUtil.comma(ARRAY);

        /** _more_ */
        public static final String NODOT_COLUMNS = SqlUtil.commaNoDot(ARRAY);

        /** _more_ */
        public static final String INSERT =
            SqlUtil.makeInsert(NAME, NODOT_COLUMNS,
                               SqlUtil.getQuestionMarks(ARRAY.length));

    }

    ;

    /**
     * Class USERS _more_
     *
     *
     * @author RAMADDA Development Team
     */
    public static class USERS {

        /** _more_ */
        public static final String NAME = "users";

        /** _more_ */
        public static final String COL_ID = NAME + ".id";

        /** _more_ */
        public static final String COL_NAME = NAME + ".name";

        /** _more_ */
        public static final String COL_EMAIL = NAME + ".email";

        /** _more_ */
        public static final String COL_QUESTION = NAME + ".question";

        /** _more_ */
        public static final String COL_ANSWER = NAME + ".answer";

        /** _more_ */
        public static final String COL_PASSWORD = NAME + ".password";

        /** _more_ */
        public static final String COL_ADMIN = NAME + ".admin";

        /** _more_ */
        public static final String COL_LANGUAGE = NAME + ".language";

        /** _more_ */
        public static final String COL_TEMPLATE = NAME + ".template";

        /** _more_ */
        public static final String COL_ISGUEST = NAME + ".isguest";

        /** _more_          */
        public static final String COL_PROPERTIES = NAME + ".properties";

        /** _more_ */
        public static final String[] ARRAY = new String[] {
            COL_ID, COL_NAME, COL_EMAIL, COL_QUESTION, COL_ANSWER,
            COL_PASSWORD, COL_ADMIN, COL_LANGUAGE, COL_TEMPLATE, COL_ISGUEST,
            COL_PROPERTIES
        };


        /** _more_ */
        public static final String COLUMNS = SqlUtil.comma(ARRAY);

        /** _more_ */
        public static final String NODOT_COLUMNS = SqlUtil.commaNoDot(ARRAY);

        /** _more_ */
        public static final String INSERT =
            SqlUtil.makeInsert(NAME, NODOT_COLUMNS,
                               SqlUtil.getQuestionMarks(ARRAY.length));

    }

    ;

    /**
     * Class USERROLES _more_
     *
     *
     * @author RAMADDA Development Team
     */
    public static class USERROLES {

        /** _more_ */
        public static final String NAME = "userroles";

        /** _more_ */
        public static final String COL_USER_ID = NAME + ".user_id";

        /** _more_ */
        public static final String COL_ROLE = NAME + ".role";

        /** _more_ */
        public static final String[] ARRAY = new String[] { COL_USER_ID,
                COL_ROLE };


        /** _more_ */
        public static final String COLUMNS = SqlUtil.comma(ARRAY);

        /** _more_ */
        public static final String NODOT_COLUMNS = SqlUtil.commaNoDot(ARRAY);

        /** _more_ */
        public static final String INSERT =
            SqlUtil.makeInsert(NAME, NODOT_COLUMNS,
                               SqlUtil.getQuestionMarks(ARRAY.length));

    }

    ;

    /**
     * Class FAVORITES _more_
     *
     *
     * @author RAMADDA Development Team
     */
    public static class FAVORITES {

        /** _more_ */
        public static final String NAME = "favorites";

        /** _more_ */
        public static final String COL_ID = NAME + ".id";

        /** _more_ */
        public static final String COL_USER_ID = NAME + ".user_id";

        /** _more_ */
        public static final String COL_ENTRY_ID = NAME + ".entry_id";

        /** _more_ */
        public static final String COL_NAME = NAME + ".name";

        /** _more_ */
        public static final String COL_CATEGORY = NAME + ".category";

        /** _more_ */
        public static final String[] ARRAY = new String[] { COL_ID,
                COL_USER_ID, COL_ENTRY_ID, COL_NAME, COL_CATEGORY };


        /** _more_ */
        public static final String COLUMNS = SqlUtil.comma(ARRAY);

        /** _more_ */
        public static final String NODOT_COLUMNS = SqlUtil.commaNoDot(ARRAY);

        /** _more_ */
        public static final String INSERT =
            SqlUtil.makeInsert(NAME, NODOT_COLUMNS,
                               SqlUtil.getQuestionMarks(ARRAY.length));

    }

    ;

    /**
     * Class USER_ACTIVITY _more_
     *
     *
     * @author RAMADDA Development Team
     */
    public static class USER_ACTIVITY {

        /** _more_ */
        public static final String NAME = "user_activity";

        /** _more_ */
        public static final String COL_USER_ID = NAME + ".user_id";

        /** _more_ */
        public static final String COL_DATE = NAME + ".date";

        /** _more_ */
        public static final String COL_WHAT = NAME + ".what";

        /** _more_ */
        public static final String COL_EXTRA = NAME + ".extra";

        /** _more_ */
        public static final String COL_IPADDRESS = NAME + ".ipaddress";

        /** _more_ */
        public static final String[] ARRAY = new String[] { COL_USER_ID,
                COL_DATE, COL_WHAT, COL_EXTRA, COL_IPADDRESS };


        /** _more_ */
        public static final String COLUMNS = SqlUtil.comma(ARRAY);

        /** _more_ */
        public static final String NODOT_COLUMNS = SqlUtil.commaNoDot(ARRAY);

        /** _more_ */
        public static final String INSERT =
            SqlUtil.makeInsert(NAME, NODOT_COLUMNS,
                               SqlUtil.getQuestionMarks(ARRAY.length));

    }

    ;

    /**
     * Class SESSIONS _more_
     *
     *
     * @author RAMADDA Development Team
     */
    public static class SESSIONS {

        /** _more_ */
        public static final String NAME = "sessions";

        /** _more_ */
        public static final String COL_SESSION_ID = NAME + ".session_id";

        /** _more_ */
        public static final String COL_USER_ID = NAME + ".user_id";

        /** _more_ */
        public static final String COL_CREATE_DATE = NAME + ".create_date";

        /** _more_ */
        public static final String COL_LAST_ACTIVE_DATE =
            NAME + ".last_active_date";

        /** _more_ */
        public static final String COL_EXTRA = NAME + ".extra";

        /** _more_ */
        public static final String[] ARRAY = new String[] { COL_SESSION_ID,
                COL_USER_ID, COL_CREATE_DATE, COL_LAST_ACTIVE_DATE,
                COL_EXTRA };


        /** _more_ */
        public static final String COLUMNS = SqlUtil.comma(ARRAY);

        /** _more_ */
        public static final String NODOT_COLUMNS = SqlUtil.commaNoDot(ARRAY);

        /** _more_ */
        public static final String INSERT =
            SqlUtil.makeInsert(NAME, NODOT_COLUMNS,
                               SqlUtil.getQuestionMarks(ARRAY.length));

    }

    ;

    /**
     * Class MONITORS _more_
     *
     *
     * @author RAMADDA Development Team
     */
    public static class MONITORS {

        /** _more_ */
        public static final String NAME = "monitors";

        /** _more_ */
        public static final String COL_MONITOR_ID = NAME + ".monitor_id";

        /** _more_ */
        public static final String COL_NAME = NAME + ".name";

        /** _more_ */
        public static final String COL_USER_ID = NAME + ".user_id";

        /** _more_ */
        public static final String COL_FROM_DATE = NAME + ".from_date";

        /** _more_ */
        public static final String COL_TO_DATE = NAME + ".to_date";

        /** _more_ */
        public static final String COL_ENCODED_OBJECT = NAME
                                                        + ".encoded_object";

        /** _more_ */
        public static final String[] ARRAY = new String[] {
            COL_MONITOR_ID, COL_NAME, COL_USER_ID, COL_FROM_DATE, COL_TO_DATE,
            COL_ENCODED_OBJECT
        };


        /** _more_ */
        public static final String COLUMNS = SqlUtil.comma(ARRAY);

        /** _more_ */
        public static final String NODOT_COLUMNS = SqlUtil.commaNoDot(ARRAY);

        /** _more_ */
        public static final String INSERT =
            SqlUtil.makeInsert(NAME, NODOT_COLUMNS,
                               SqlUtil.getQuestionMarks(ARRAY.length));

    }

    ;

    /**
     * Class PERMISSIONS _more_
     *
     *
     * @author RAMADDA Development Team
     */
    public static class PERMISSIONS {

        /** _more_ */
        public static final String NAME = "permissions";

        /** _more_ */
        public static final String COL_ENTRY_ID = NAME + ".entry_id";

        /** _more_ */
        public static final String COL_ACTION = NAME + ".action";

        /** _more_ */
        public static final String COL_ROLE = NAME + ".role";

        /** _more_ */
        public static final String[] ARRAY = new String[] { COL_ENTRY_ID,
                COL_ACTION, COL_ROLE };


        /** _more_ */
        public static final String COLUMNS = SqlUtil.comma(ARRAY);

        /** _more_ */
        public static final String NODOT_COLUMNS = SqlUtil.commaNoDot(ARRAY);

        /** _more_ */
        public static final String INSERT =
            SqlUtil.makeInsert(NAME, NODOT_COLUMNS,
                               SqlUtil.getQuestionMarks(ARRAY.length));

    }

    ;

    /**
     * Class HARVESTERS _more_
     *
     *
     * @author RAMADDA Development Team
     */
    public static class HARVESTERS {

        /** _more_ */
        public static final String NAME = "harvesters";

        /** _more_ */
        public static final String COL_ID = NAME + ".id";

        /** _more_ */
        public static final String COL_CLASS = NAME + ".class";

        /** _more_ */
        public static final String COL_CONTENT = NAME + ".content";

        /** _more_ */
        public static final String[] ARRAY = new String[] { COL_ID, COL_CLASS,
                COL_CONTENT };


        /** _more_ */
        public static final String COLUMNS = SqlUtil.comma(ARRAY);

        /** _more_ */
        public static final String NODOT_COLUMNS = SqlUtil.commaNoDot(ARRAY);

        /** _more_ */
        public static final String INSERT =
            SqlUtil.makeInsert(NAME, NODOT_COLUMNS,
                               SqlUtil.getQuestionMarks(ARRAY.length));

    }

    ;

    /**
     * Class GLOBALS _more_
     *
     *
     * @author RAMADDA Development Team
     */
    public static class GLOBALS {

        /** _more_ */
        public static final String NAME = "globals";

        /** _more_ */
        public static final String COL_NAME = NAME + ".name";

        /** _more_ */
        public static final String COL_VALUE = NAME + ".value";

        /** _more_ */
        public static final String[] ARRAY = new String[] { COL_NAME,
                COL_VALUE };


        /** _more_ */
        public static final String COLUMNS = SqlUtil.comma(ARRAY);

        /** _more_ */
        public static final String NODOT_COLUMNS = SqlUtil.commaNoDot(ARRAY);

        /** _more_ */
        public static final String INSERT =
            SqlUtil.makeInsert(NAME, NODOT_COLUMNS,
                               SqlUtil.getQuestionMarks(ARRAY.length));

    }

    ;


    /**
     * Class SERVERREGISTRY _more_
     *
     *
     * @author RAMADDA Development Team
     */
    public static class SERVERREGISTRY {

        /** _more_ */
        public static final String NAME = "serverregistry";

        /** _more_ */
        public static final String COL_URL = NAME + ".url";

        /** _more_ */
        public static final String COL_TITLE = NAME + ".title";

        /** _more_ */
        public static final String COL_DESCRIPTION = NAME + ".description";

        /** _more_ */
        public static final String COL_EMAIL = NAME + ".email";

        /** _more_ */
        public static final String COL_ISREGISTRY = NAME + ".isregistry";

        /** _more_ */
        public static final String[] ARRAY = new String[] { COL_URL,
                COL_TITLE, COL_DESCRIPTION, COL_EMAIL, COL_ISREGISTRY };


        /** _more_ */
        public static final String COLUMNS = SqlUtil.comma(ARRAY);

        /** _more_ */
        public static final String NODOT_COLUMNS = SqlUtil.commaNoDot(ARRAY);

        /** _more_ */
        public static final String INSERT =
            SqlUtil.makeInsert(NAME, NODOT_COLUMNS,
                               SqlUtil.getQuestionMarks(ARRAY.length));

    }

    ;

    /**
     * Class REMOTESERVERS _more_
     *
     *
     * @author RAMADDA Development Team
     */
    public static class REMOTESERVERS {

        /** _more_ */
        public static final String NAME = "remoteservers";

        /** _more_ */
        public static final String COL_URL = NAME + ".url";

        /** _more_ */
        public static final String COL_TITLE = NAME + ".title";

        /** _more_ */
        public static final String COL_DESCRIPTION = NAME + ".description";

        /** _more_ */
        public static final String COL_EMAIL = NAME + ".email";

        /** _more_ */
        public static final String COL_ISREGISTRY = NAME + ".isregistry";

        /** _more_ */
        public static final String COL_SELECTED = NAME + ".selected";

        /** _more_ */
        public static final String[] ARRAY = new String[] {
            COL_URL, COL_TITLE, COL_DESCRIPTION, COL_EMAIL, COL_ISREGISTRY,
            COL_SELECTED
        };


        /** _more_ */
        public static final String COLUMNS = SqlUtil.comma(ARRAY);

        /** _more_ */
        public static final String NODOT_COLUMNS = SqlUtil.commaNoDot(ARRAY);

        /** _more_ */
        public static final String INSERT =
            SqlUtil.makeInsert(NAME, NODOT_COLUMNS,
                               SqlUtil.getQuestionMarks(ARRAY.length));

    }

    ;

    /**
     * Class DUMMY _more_
     *
     *
     * @author RAMADDA Development Team
     */
    public static class DUMMY {

        /** _more_ */
        public static final String NAME = "dummy";

        /** _more_ */
        public static final String COL_NAME = NAME + ".name";

        /** _more_ */
        public static final String[] ARRAY = new String[] { COL_NAME };


        /** _more_ */
        public static final String COLUMNS = SqlUtil.comma(ARRAY);

        /** _more_ */
        public static final String NODOT_COLUMNS = SqlUtil.commaNoDot(ARRAY);

        /** _more_ */
        public static final String INSERT =
            SqlUtil.makeInsert(NAME, NODOT_COLUMNS,
                               SqlUtil.getQuestionMarks(ARRAY.length));

    }

    ;

    /**
     * Class POINTDATAMETADATA _more_
     *
     *
     * @author RAMADDA Development Team
     */
    public static class POINTDATAMETADATA {

        /** _more_ */
        public static final String NAME = "pointdatametadata";

        /** _more_ */
        public static final String COL_TABLENAME = NAME + ".tablename";

        /** _more_ */
        public static final String COL_COLUMNNAME = NAME + ".columnname";

        /** _more_ */
        public static final String COL_COLUMNNUMBER = NAME + ".columnnumber";

        /** _more_ */
        public static final String COL_SHORTNAME = NAME + ".shortname";

        /** _more_ */
        public static final String COL_LONGNAME = NAME + ".longname";

        /** _more_ */
        public static final String COL_UNIT = NAME + ".unit";

        /** _more_ */
        public static final String COL_VARTYPE = NAME + ".vartype";

        /** _more_ */
        public static final String[] ARRAY = new String[] {
            COL_TABLENAME, COL_COLUMNNAME, COL_COLUMNNUMBER, COL_SHORTNAME,
            COL_LONGNAME, COL_UNIT, COL_VARTYPE
        };


        /** _more_ */
        public static final String COLUMNS = SqlUtil.comma(ARRAY);

        /** _more_ */
        public static final String NODOT_COLUMNS = SqlUtil.commaNoDot(ARRAY);

        /** _more_ */
        public static final String INSERT =
            SqlUtil.makeInsert(NAME, NODOT_COLUMNS,
                               SqlUtil.getQuestionMarks(ARRAY.length));

    }

    ;


}
