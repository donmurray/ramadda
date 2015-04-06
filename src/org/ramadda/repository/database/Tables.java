/*
 * Copyright (c) 2008-2015 Geode Systems LLC
 * This Software is licensed under the Geode Systems RAMADDA License available in the source distribution in the file 
 * ramadda_license.txt. The above copyright notice shall be included in all copies or substantial portions of the Software.
 */

/** Generated by running: java org.unavco.projects.gsac.repository.UnavcoGsacDatabaseManager */

package org.ramadda.repository.database;


import org.ramadda.sql.SqlUtil;


//J-
public abstract class Tables {
    public abstract String getName();
    public abstract String getColumns();


    public static class ANCESTORS extends Tables {
        public static final String NAME = "ancestors";

        public String getName() {return NAME;}
        public String getColumns() {return COLUMNS;}
        public static final String COL_ID =  NAME + ".id";
        public static final String COL_NODOT_ID =   "id";
        public static final String COL_ANCESTOR_ID =  NAME + ".ancestor_id";
        public static final String COL_NODOT_ANCESTOR_ID =   "ancestor_id";

        public static final String[] ARRAY = new String[] {
            COL_ID,COL_ANCESTOR_ID
        };
        public static final String COLUMNS = SqlUtil.comma(ARRAY);
        public static final String NODOT_COLUMNS = SqlUtil.commaNoDot(ARRAY);
        public static final String INSERT =SqlUtil.makeInsert(NAME, NODOT_COLUMNS,SqlUtil.getQuestionMarks(ARRAY.length));
    public static final ANCESTORS table  = new  ANCESTORS();
    }



    public static class ASSOCIATIONS extends Tables {
        public static final String NAME = "associations";

        public String getName() {return NAME;}
        public String getColumns() {return COLUMNS;}
        public static final String COL_ID =  NAME + ".id";
        public static final String COL_NODOT_ID =   "id";
        public static final String COL_NAME =  NAME + ".name";
        public static final String COL_NODOT_NAME =   "name";
        public static final String COL_TYPE =  NAME + ".type";
        public static final String COL_NODOT_TYPE =   "type";
        public static final String COL_FROM_ENTRY_ID =  NAME + ".from_entry_id";
        public static final String COL_NODOT_FROM_ENTRY_ID =   "from_entry_id";
        public static final String COL_TO_ENTRY_ID =  NAME + ".to_entry_id";
        public static final String COL_NODOT_TO_ENTRY_ID =   "to_entry_id";

        public static final String[] ARRAY = new String[] {
            COL_ID,COL_NAME,COL_TYPE,COL_FROM_ENTRY_ID,COL_TO_ENTRY_ID
        };
        public static final String COLUMNS = SqlUtil.comma(ARRAY);
        public static final String NODOT_COLUMNS = SqlUtil.commaNoDot(ARRAY);
        public static final String INSERT =SqlUtil.makeInsert(NAME, NODOT_COLUMNS,SqlUtil.getQuestionMarks(ARRAY.length));
    public static final ASSOCIATIONS table  = new  ASSOCIATIONS();
    }



    public static class COMMENTS extends Tables {
        public static final String NAME = "comments";

        public String getName() {return NAME;}
        public String getColumns() {return COLUMNS;}
        public static final String COL_ID =  NAME + ".id";
        public static final String COL_NODOT_ID =   "id";
        public static final String COL_ENTRY_ID =  NAME + ".entry_id";
        public static final String COL_NODOT_ENTRY_ID =   "entry_id";
        public static final String COL_USER_ID =  NAME + ".user_id";
        public static final String COL_NODOT_USER_ID =   "user_id";
        public static final String COL_DATE =  NAME + ".date";
        public static final String COL_NODOT_DATE =   "date";
        public static final String COL_SUBJECT =  NAME + ".subject";
        public static final String COL_NODOT_SUBJECT =   "subject";
        public static final String COL_COMMENT =  NAME + ".comment";
        public static final String COL_NODOT_COMMENT =   "comment";

        public static final String[] ARRAY = new String[] {
            COL_ID,COL_ENTRY_ID,COL_USER_ID,COL_DATE,COL_SUBJECT,COL_COMMENT
        };
        public static final String COLUMNS = SqlUtil.comma(ARRAY);
        public static final String NODOT_COLUMNS = SqlUtil.commaNoDot(ARRAY);
        public static final String INSERT =SqlUtil.makeInsert(NAME, NODOT_COLUMNS,SqlUtil.getQuestionMarks(ARRAY.length));
    public static final COMMENTS table  = new  COMMENTS();
    }



    public static class DUMMY extends Tables {
        public static final String NAME = "dummy";

        public String getName() {return NAME;}
        public String getColumns() {return COLUMNS;}
        public static final String COL_NAME =  NAME + ".name";
        public static final String COL_NODOT_NAME =   "name";

        public static final String[] ARRAY = new String[] {
            COL_NAME
        };
        public static final String COLUMNS = SqlUtil.comma(ARRAY);
        public static final String NODOT_COLUMNS = SqlUtil.commaNoDot(ARRAY);
        public static final String INSERT =SqlUtil.makeInsert(NAME, NODOT_COLUMNS,SqlUtil.getQuestionMarks(ARRAY.length));
    public static final DUMMY table  = new  DUMMY();
    }



    public static class ENTRIES extends Tables {
        public static final String NAME = "entries";

        public String getName() {return NAME;}
        public String getColumns() {return COLUMNS;}
        public static final String COL_ID =  NAME + ".id";
        public static final String COL_NODOT_ID =   "id";
        public static final String COL_TYPE =  NAME + ".type";
        public static final String COL_NODOT_TYPE =   "type";
        public static final String COL_NAME =  NAME + ".name";
        public static final String COL_NODOT_NAME =   "name";
        public static final String COL_DESCRIPTION =  NAME + ".description";
        public static final String COL_NODOT_DESCRIPTION =   "description";
        public static final String COL_PARENT_GROUP_ID =  NAME + ".parent_group_id";
        public static final String COL_NODOT_PARENT_GROUP_ID =   "parent_group_id";
        public static final String COL_USER_ID =  NAME + ".user_id";
        public static final String COL_NODOT_USER_ID =   "user_id";
        public static final String COL_RESOURCE =  NAME + ".resource";
        public static final String COL_NODOT_RESOURCE =   "resource";
        public static final String COL_RESOURCE_TYPE =  NAME + ".resource_type";
        public static final String COL_NODOT_RESOURCE_TYPE =   "resource_type";
        public static final String COL_MD5 =  NAME + ".md5";
        public static final String COL_NODOT_MD5 =   "md5";
        public static final String COL_FILESIZE =  NAME + ".filesize";
        public static final String COL_NODOT_FILESIZE =   "filesize";
        public static final String COL_DATATYPE =  NAME + ".datatype";
        public static final String COL_NODOT_DATATYPE =   "datatype";
        public static final String COL_CREATEDATE =  NAME + ".createdate";
        public static final String COL_NODOT_CREATEDATE =   "createdate";
        public static final String COL_CHANGEDATE =  NAME + ".changedate";
        public static final String COL_NODOT_CHANGEDATE =   "changedate";
        public static final String COL_FROMDATE =  NAME + ".fromdate";
        public static final String COL_NODOT_FROMDATE =   "fromdate";
        public static final String COL_TODATE =  NAME + ".todate";
        public static final String COL_NODOT_TODATE =   "todate";
        public static final String COL_SOUTH =  NAME + ".south";
        public static final String COL_NODOT_SOUTH =   "south";
        public static final String COL_NORTH =  NAME + ".north";
        public static final String COL_NODOT_NORTH =   "north";
        public static final String COL_EAST =  NAME + ".east";
        public static final String COL_NODOT_EAST =   "east";
        public static final String COL_WEST =  NAME + ".west";
        public static final String COL_NODOT_WEST =   "west";
        public static final String COL_ALTITUDETOP =  NAME + ".altitudetop";
        public static final String COL_NODOT_ALTITUDETOP =   "altitudetop";
        public static final String COL_ALTITUDEBOTTOM =  NAME + ".altitudebottom";
        public static final String COL_NODOT_ALTITUDEBOTTOM =   "altitudebottom";

        public static final String[] ARRAY = new String[] {
            COL_ID,COL_TYPE,COL_NAME,COL_DESCRIPTION,COL_PARENT_GROUP_ID,COL_USER_ID,COL_RESOURCE,COL_RESOURCE_TYPE,COL_MD5,COL_FILESIZE,COL_DATATYPE,COL_CREATEDATE,COL_CHANGEDATE,COL_FROMDATE,COL_TODATE,COL_SOUTH,COL_NORTH,COL_EAST,COL_WEST,COL_ALTITUDETOP,COL_ALTITUDEBOTTOM
        };
        public static final String COLUMNS = SqlUtil.comma(ARRAY);
        public static final String NODOT_COLUMNS = SqlUtil.commaNoDot(ARRAY);
        public static final String INSERT =SqlUtil.makeInsert(NAME, NODOT_COLUMNS,SqlUtil.getQuestionMarks(ARRAY.length));
    public static final ENTRIES table  = new  ENTRIES();
    }



    public static class FAVORITES extends Tables {
        public static final String NAME = "favorites";

        public String getName() {return NAME;}
        public String getColumns() {return COLUMNS;}
        public static final String COL_ID =  NAME + ".id";
        public static final String COL_NODOT_ID =   "id";
        public static final String COL_USER_ID =  NAME + ".user_id";
        public static final String COL_NODOT_USER_ID =   "user_id";
        public static final String COL_ENTRY_ID =  NAME + ".entry_id";
        public static final String COL_NODOT_ENTRY_ID =   "entry_id";
        public static final String COL_NAME =  NAME + ".name";
        public static final String COL_NODOT_NAME =   "name";
        public static final String COL_CATEGORY =  NAME + ".category";
        public static final String COL_NODOT_CATEGORY =   "category";

        public static final String[] ARRAY = new String[] {
            COL_ID,COL_USER_ID,COL_ENTRY_ID,COL_NAME,COL_CATEGORY
        };
        public static final String COLUMNS = SqlUtil.comma(ARRAY);
        public static final String NODOT_COLUMNS = SqlUtil.commaNoDot(ARRAY);
        public static final String INSERT =SqlUtil.makeInsert(NAME, NODOT_COLUMNS,SqlUtil.getQuestionMarks(ARRAY.length));
    public static final FAVORITES table  = new  FAVORITES();
    }



    public static class FTP extends Tables {
        public static final String NAME = "ftp";

        public String getName() {return NAME;}
        public String getColumns() {return COLUMNS;}
        public static final String COL_ID =  NAME + ".id";
        public static final String COL_NODOT_ID =   "id";
        public static final String COL_SERVER =  NAME + ".server";
        public static final String COL_NODOT_SERVER =   "server";
        public static final String COL_BASEDIR =  NAME + ".basedir";
        public static final String COL_NODOT_BASEDIR =   "basedir";
        public static final String COL_FTPUSER =  NAME + ".ftpuser";
        public static final String COL_NODOT_FTPUSER =   "ftpuser";
        public static final String COL_FTPPASSWORD =  NAME + ".ftppassword";
        public static final String COL_NODOT_FTPPASSWORD =   "ftppassword";
        public static final String COL_MAXSIZE =  NAME + ".maxsize";
        public static final String COL_NODOT_MAXSIZE =   "maxsize";
        public static final String COL_PATTERN =  NAME + ".pattern";
        public static final String COL_NODOT_PATTERN =   "pattern";

        public static final String[] ARRAY = new String[] {
            COL_ID,COL_SERVER,COL_BASEDIR,COL_FTPUSER,COL_FTPPASSWORD,COL_MAXSIZE,COL_PATTERN
        };
        public static final String COLUMNS = SqlUtil.comma(ARRAY);
        public static final String NODOT_COLUMNS = SqlUtil.commaNoDot(ARRAY);
        public static final String INSERT =SqlUtil.makeInsert(NAME, NODOT_COLUMNS,SqlUtil.getQuestionMarks(ARRAY.length));
    public static final FTP table  = new  FTP();
    }



    public static class GLOBALS extends Tables {
        public static final String NAME = "globals";

        public String getName() {return NAME;}
        public String getColumns() {return COLUMNS;}
        public static final String COL_NAME =  NAME + ".name";
        public static final String COL_NODOT_NAME =   "name";
        public static final String COL_VALUE =  NAME + ".value";
        public static final String COL_NODOT_VALUE =   "value";

        public static final String[] ARRAY = new String[] {
            COL_NAME,COL_VALUE
        };
        public static final String COLUMNS = SqlUtil.comma(ARRAY);
        public static final String NODOT_COLUMNS = SqlUtil.commaNoDot(ARRAY);
        public static final String INSERT =SqlUtil.makeInsert(NAME, NODOT_COLUMNS,SqlUtil.getQuestionMarks(ARRAY.length));
    public static final GLOBALS table  = new  GLOBALS();
    }



    public static class HARVESTERS extends Tables {
        public static final String NAME = "harvesters";

        public String getName() {return NAME;}
        public String getColumns() {return COLUMNS;}
        public static final String COL_ID =  NAME + ".id";
        public static final String COL_NODOT_ID =   "id";
        public static final String COL_CLASS =  NAME + ".class";
        public static final String COL_NODOT_CLASS =   "class";
        public static final String COL_CONTENT =  NAME + ".content";
        public static final String COL_NODOT_CONTENT =   "content";

        public static final String[] ARRAY = new String[] {
            COL_ID,COL_CLASS,COL_CONTENT
        };
        public static final String COLUMNS = SqlUtil.comma(ARRAY);
        public static final String NODOT_COLUMNS = SqlUtil.commaNoDot(ARRAY);
        public static final String INSERT =SqlUtil.makeInsert(NAME, NODOT_COLUMNS,SqlUtil.getQuestionMarks(ARRAY.length));
    public static final HARVESTERS table  = new  HARVESTERS();
    }



    public static class JOBINFOS extends Tables {
        public static final String NAME = "jobinfos";

        public String getName() {return NAME;}
        public String getColumns() {return COLUMNS;}
        public static final String COL_ID =  NAME + ".id";
        public static final String COL_NODOT_ID =   "id";
        public static final String COL_ENTRY_ID =  NAME + ".entry_id";
        public static final String COL_NODOT_ENTRY_ID =   "entry_id";
        public static final String COL_USER_ID =  NAME + ".user_id";
        public static final String COL_NODOT_USER_ID =   "user_id";
        public static final String COL_DATE =  NAME + ".date";
        public static final String COL_NODOT_DATE =   "date";
        public static final String COL_TYPE =  NAME + ".type";
        public static final String COL_NODOT_TYPE =   "type";
        public static final String COL_JOB_INFO_BLOB =  NAME + ".job_info_blob";
        public static final String COL_NODOT_JOB_INFO_BLOB =   "job_info_blob";

        public static final String[] ARRAY = new String[] {
            COL_ID,COL_ENTRY_ID,COL_USER_ID,COL_DATE,COL_TYPE,COL_JOB_INFO_BLOB
        };
        public static final String COLUMNS = SqlUtil.comma(ARRAY);
        public static final String NODOT_COLUMNS = SqlUtil.commaNoDot(ARRAY);
        public static final String INSERT =SqlUtil.makeInsert(NAME, NODOT_COLUMNS,SqlUtil.getQuestionMarks(ARRAY.length));
    public static final JOBINFOS table  = new  JOBINFOS();
    }



    public static class LOCALFILES extends Tables {
        public static final String NAME = "localfiles";

        public String getName() {return NAME;}
        public String getColumns() {return COLUMNS;}
        public static final String COL_ID =  NAME + ".id";
        public static final String COL_NODOT_ID =   "id";
        public static final String COL_LOCALFILEPATH =  NAME + ".localfilepath";
        public static final String COL_NODOT_LOCALFILEPATH =   "localfilepath";
        public static final String COL_TIMEDELAY =  NAME + ".timedelay";
        public static final String COL_NODOT_TIMEDELAY =   "timedelay";
        public static final String COL_INCLUDEPATTERN =  NAME + ".includepattern";
        public static final String COL_NODOT_INCLUDEPATTERN =   "includepattern";
        public static final String COL_EXCLUDEPATTERN =  NAME + ".excludepattern";
        public static final String COL_NODOT_EXCLUDEPATTERN =   "excludepattern";
        public static final String COL_NAMING =  NAME + ".naming";
        public static final String COL_NODOT_NAMING =   "naming";

        public static final String[] ARRAY = new String[] {
            COL_ID,COL_LOCALFILEPATH,COL_TIMEDELAY,COL_INCLUDEPATTERN,COL_EXCLUDEPATTERN,COL_NAMING
        };
        public static final String COLUMNS = SqlUtil.comma(ARRAY);
        public static final String NODOT_COLUMNS = SqlUtil.commaNoDot(ARRAY);
        public static final String INSERT =SqlUtil.makeInsert(NAME, NODOT_COLUMNS,SqlUtil.getQuestionMarks(ARRAY.length));
    public static final LOCALFILES table  = new  LOCALFILES();
    }



    public static class LOCALREPOSITORIES extends Tables {
        public static final String NAME = "localrepositories";

        public String getName() {return NAME;}
        public String getColumns() {return COLUMNS;}
        public static final String COL_ID =  NAME + ".id";
        public static final String COL_NODOT_ID =   "id";
        public static final String COL_EMAIL =  NAME + ".email";
        public static final String COL_NODOT_EMAIL =   "email";
        public static final String COL_STATUS =  NAME + ".status";
        public static final String COL_NODOT_STATUS =   "status";

        public static final String[] ARRAY = new String[] {
            COL_ID,COL_EMAIL,COL_STATUS
        };
        public static final String COLUMNS = SqlUtil.comma(ARRAY);
        public static final String NODOT_COLUMNS = SqlUtil.commaNoDot(ARRAY);
        public static final String INSERT =SqlUtil.makeInsert(NAME, NODOT_COLUMNS,SqlUtil.getQuestionMarks(ARRAY.length));
    public static final LOCALREPOSITORIES table  = new  LOCALREPOSITORIES();
    }



    public static class METADATA extends Tables {
        public static final String NAME = "metadata";

        public String getName() {return NAME;}
        public String getColumns() {return COLUMNS;}
        public static final String COL_ID =  NAME + ".id";
        public static final String COL_NODOT_ID =   "id";
        public static final String COL_ENTRY_ID =  NAME + ".entry_id";
        public static final String COL_NODOT_ENTRY_ID =   "entry_id";
        public static final String COL_TYPE =  NAME + ".type";
        public static final String COL_NODOT_TYPE =   "type";
        public static final String COL_INHERITED =  NAME + ".inherited";
        public static final String COL_NODOT_INHERITED =   "inherited";
        public static final String COL_ATTR1 =  NAME + ".attr1";
        public static final String COL_NODOT_ATTR1 =   "attr1";
        public static final String COL_ATTR2 =  NAME + ".attr2";
        public static final String COL_NODOT_ATTR2 =   "attr2";
        public static final String COL_ATTR3 =  NAME + ".attr3";
        public static final String COL_NODOT_ATTR3 =   "attr3";
        public static final String COL_ATTR4 =  NAME + ".attr4";
        public static final String COL_NODOT_ATTR4 =   "attr4";
        public static final String COL_EXTRA =  NAME + ".extra";
        public static final String COL_NODOT_EXTRA =   "extra";

        public static final String[] ARRAY = new String[] {
            COL_ID,COL_ENTRY_ID,COL_TYPE,COL_INHERITED,COL_ATTR1,COL_ATTR2,COL_ATTR3,COL_ATTR4,COL_EXTRA
        };
        public static final String COLUMNS = SqlUtil.comma(ARRAY);
        public static final String NODOT_COLUMNS = SqlUtil.commaNoDot(ARRAY);
        public static final String INSERT =SqlUtil.makeInsert(NAME, NODOT_COLUMNS,SqlUtil.getQuestionMarks(ARRAY.length));
    public static final METADATA table  = new  METADATA();
    }



    public static class METADATA_TEST1 extends Tables {
        public static final String NAME = "metadata_test1";

        public String getName() {return NAME;}
        public String getColumns() {return COLUMNS;}
        public static final String COL_ID =  NAME + ".id";
        public static final String COL_NODOT_ID =   "id";
        public static final String COL_ENTRY_ID =  NAME + ".entry_id";
        public static final String COL_NODOT_ENTRY_ID =   "entry_id";
        public static final String COL_TYPE =  NAME + ".type";
        public static final String COL_NODOT_TYPE =   "type";
        public static final String COL_INHERITED =  NAME + ".inherited";
        public static final String COL_NODOT_INHERITED =   "inherited";
        public static final String COL_ATTR1 =  NAME + ".attr1";
        public static final String COL_NODOT_ATTR1 =   "attr1";
        public static final String COL_ATTR2 =  NAME + ".attr2";
        public static final String COL_NODOT_ATTR2 =   "attr2";
        public static final String COL_ATTR3 =  NAME + ".attr3";
        public static final String COL_NODOT_ATTR3 =   "attr3";
        public static final String COL_ATTR4 =  NAME + ".attr4";
        public static final String COL_NODOT_ATTR4 =   "attr4";
        public static final String COL_EXTRA =  NAME + ".extra";
        public static final String COL_NODOT_EXTRA =   "extra";

        public static final String[] ARRAY = new String[] {
            COL_ID,COL_ENTRY_ID,COL_TYPE,COL_INHERITED,COL_ATTR1,COL_ATTR2,COL_ATTR3,COL_ATTR4,COL_EXTRA
        };
        public static final String COLUMNS = SqlUtil.comma(ARRAY);
        public static final String NODOT_COLUMNS = SqlUtil.commaNoDot(ARRAY);
        public static final String INSERT =SqlUtil.makeInsert(NAME, NODOT_COLUMNS,SqlUtil.getQuestionMarks(ARRAY.length));
    public static final METADATA_TEST1 table  = new  METADATA_TEST1();
    }



    public static class MONITORS extends Tables {
        public static final String NAME = "monitors";

        public String getName() {return NAME;}
        public String getColumns() {return COLUMNS;}
        public static final String COL_MONITOR_ID =  NAME + ".monitor_id";
        public static final String COL_NODOT_MONITOR_ID =   "monitor_id";
        public static final String COL_NAME =  NAME + ".name";
        public static final String COL_NODOT_NAME =   "name";
        public static final String COL_USER_ID =  NAME + ".user_id";
        public static final String COL_NODOT_USER_ID =   "user_id";
        public static final String COL_FROM_DATE =  NAME + ".from_date";
        public static final String COL_NODOT_FROM_DATE =   "from_date";
        public static final String COL_TO_DATE =  NAME + ".to_date";
        public static final String COL_NODOT_TO_DATE =   "to_date";
        public static final String COL_ENCODED_OBJECT =  NAME + ".encoded_object";
        public static final String COL_NODOT_ENCODED_OBJECT =   "encoded_object";

        public static final String[] ARRAY = new String[] {
            COL_MONITOR_ID,COL_NAME,COL_USER_ID,COL_FROM_DATE,COL_TO_DATE,COL_ENCODED_OBJECT
        };
        public static final String COLUMNS = SqlUtil.comma(ARRAY);
        public static final String NODOT_COLUMNS = SqlUtil.commaNoDot(ARRAY);
        public static final String INSERT =SqlUtil.makeInsert(NAME, NODOT_COLUMNS,SqlUtil.getQuestionMarks(ARRAY.length));
    public static final MONITORS table  = new  MONITORS();
    }



    public static class PERMISSIONS extends Tables {
        public static final String NAME = "permissions";

        public String getName() {return NAME;}
        public String getColumns() {return COLUMNS;}
        public static final String COL_ENTRY_ID =  NAME + ".entry_id";
        public static final String COL_NODOT_ENTRY_ID =   "entry_id";
        public static final String COL_ACTION =  NAME + ".action";
        public static final String COL_NODOT_ACTION =   "action";
        public static final String COL_ROLE =  NAME + ".role";
        public static final String COL_NODOT_ROLE =   "role";

        public static final String[] ARRAY = new String[] {
            COL_ENTRY_ID,COL_ACTION,COL_ROLE
        };
        public static final String COLUMNS = SqlUtil.comma(ARRAY);
        public static final String NODOT_COLUMNS = SqlUtil.commaNoDot(ARRAY);
        public static final String INSERT =SqlUtil.makeInsert(NAME, NODOT_COLUMNS,SqlUtil.getQuestionMarks(ARRAY.length));
    public static final PERMISSIONS table  = new  PERMISSIONS();
    }



    public static class POINTDATAMETADATA extends Tables {
        public static final String NAME = "pointdatametadata";

        public String getName() {return NAME;}
        public String getColumns() {return COLUMNS;}
        public static final String COL_TABLENAME =  NAME + ".tablename";
        public static final String COL_NODOT_TABLENAME =   "tablename";
        public static final String COL_COLUMNNAME =  NAME + ".columnname";
        public static final String COL_NODOT_COLUMNNAME =   "columnname";
        public static final String COL_COLUMNNUMBER =  NAME + ".columnnumber";
        public static final String COL_NODOT_COLUMNNUMBER =   "columnnumber";
        public static final String COL_SHORTNAME =  NAME + ".shortname";
        public static final String COL_NODOT_SHORTNAME =   "shortname";
        public static final String COL_LONGNAME =  NAME + ".longname";
        public static final String COL_NODOT_LONGNAME =   "longname";
        public static final String COL_UNIT =  NAME + ".unit";
        public static final String COL_NODOT_UNIT =   "unit";
        public static final String COL_VARTYPE =  NAME + ".vartype";
        public static final String COL_NODOT_VARTYPE =   "vartype";

        public static final String[] ARRAY = new String[] {
            COL_TABLENAME,COL_COLUMNNAME,COL_COLUMNNUMBER,COL_SHORTNAME,COL_LONGNAME,COL_UNIT,COL_VARTYPE
        };
        public static final String COLUMNS = SqlUtil.comma(ARRAY);
        public static final String NODOT_COLUMNS = SqlUtil.commaNoDot(ARRAY);
        public static final String INSERT =SqlUtil.makeInsert(NAME, NODOT_COLUMNS,SqlUtil.getQuestionMarks(ARRAY.length));
    public static final POINTDATAMETADATA table  = new  POINTDATAMETADATA();
    }



    public static class REMOTESERVERS extends Tables {
        public static final String NAME = "remoteservers";

        public String getName() {return NAME;}
        public String getColumns() {return COLUMNS;}
        public static final String COL_URL =  NAME + ".url";
        public static final String COL_NODOT_URL =   "url";
        public static final String COL_TITLE =  NAME + ".title";
        public static final String COL_NODOT_TITLE =   "title";
        public static final String COL_DESCRIPTION =  NAME + ".description";
        public static final String COL_NODOT_DESCRIPTION =   "description";
        public static final String COL_EMAIL =  NAME + ".email";
        public static final String COL_NODOT_EMAIL =   "email";
        public static final String COL_ISREGISTRY =  NAME + ".isregistry";
        public static final String COL_NODOT_ISREGISTRY =   "isregistry";
        public static final String COL_SELECTED =  NAME + ".selected";
        public static final String COL_NODOT_SELECTED =   "selected";

        public static final String[] ARRAY = new String[] {
            COL_URL,COL_TITLE,COL_DESCRIPTION,COL_EMAIL,COL_ISREGISTRY,COL_SELECTED
        };
        public static final String COLUMNS = SqlUtil.comma(ARRAY);
        public static final String NODOT_COLUMNS = SqlUtil.commaNoDot(ARRAY);
        public static final String INSERT =SqlUtil.makeInsert(NAME, NODOT_COLUMNS,SqlUtil.getQuestionMarks(ARRAY.length));
    public static final REMOTESERVERS table  = new  REMOTESERVERS();
    }



    public static class SERVERREGISTRY extends Tables {
        public static final String NAME = "serverregistry";

        public String getName() {return NAME;}
        public String getColumns() {return COLUMNS;}
        public static final String COL_URL =  NAME + ".url";
        public static final String COL_NODOT_URL =   "url";
        public static final String COL_TITLE =  NAME + ".title";
        public static final String COL_NODOT_TITLE =   "title";
        public static final String COL_DESCRIPTION =  NAME + ".description";
        public static final String COL_NODOT_DESCRIPTION =   "description";
        public static final String COL_EMAIL =  NAME + ".email";
        public static final String COL_NODOT_EMAIL =   "email";
        public static final String COL_ISREGISTRY =  NAME + ".isregistry";
        public static final String COL_NODOT_ISREGISTRY =   "isregistry";

        public static final String[] ARRAY = new String[] {
            COL_URL,COL_TITLE,COL_DESCRIPTION,COL_EMAIL,COL_ISREGISTRY
        };
        public static final String COLUMNS = SqlUtil.comma(ARRAY);
        public static final String NODOT_COLUMNS = SqlUtil.commaNoDot(ARRAY);
        public static final String INSERT =SqlUtil.makeInsert(NAME, NODOT_COLUMNS,SqlUtil.getQuestionMarks(ARRAY.length));
    public static final SERVERREGISTRY table  = new  SERVERREGISTRY();
    }



    public static class SESSIONS extends Tables {
        public static final String NAME = "sessions";

        public String getName() {return NAME;}
        public String getColumns() {return COLUMNS;}
        public static final String COL_SESSION_ID =  NAME + ".session_id";
        public static final String COL_NODOT_SESSION_ID =   "session_id";
        public static final String COL_USER_ID =  NAME + ".user_id";
        public static final String COL_NODOT_USER_ID =   "user_id";
        public static final String COL_CREATE_DATE =  NAME + ".create_date";
        public static final String COL_NODOT_CREATE_DATE =   "create_date";
        public static final String COL_LAST_ACTIVE_DATE =  NAME + ".last_active_date";
        public static final String COL_NODOT_LAST_ACTIVE_DATE =   "last_active_date";
        public static final String COL_EXTRA =  NAME + ".extra";
        public static final String COL_NODOT_EXTRA =   "extra";

        public static final String[] ARRAY = new String[] {
            COL_SESSION_ID,COL_USER_ID,COL_CREATE_DATE,COL_LAST_ACTIVE_DATE,COL_EXTRA
        };
        public static final String COLUMNS = SqlUtil.comma(ARRAY);
        public static final String NODOT_COLUMNS = SqlUtil.commaNoDot(ARRAY);
        public static final String INSERT =SqlUtil.makeInsert(NAME, NODOT_COLUMNS,SqlUtil.getQuestionMarks(ARRAY.length));
    public static final SESSIONS table  = new  SESSIONS();
    }



    public static class TYPE_COLUMN extends Tables {
        public static final String NAME = "type_column";

        public String getName() {return NAME;}
        public String getColumns() {return COLUMNS;}
        public static final String COL_ID =  NAME + ".id";
        public static final String COL_NODOT_ID =   "id";
        public static final String COL_COLUMN_INDEX =  NAME + ".column_index";
        public static final String COL_NODOT_COLUMN_INDEX =   "column_index";
        public static final String COL_COLUMN_NAME =  NAME + ".column_name";
        public static final String COL_NODOT_COLUMN_NAME =   "column_name";
        public static final String COL_DATATYPE =  NAME + ".datatype";
        public static final String COL_NODOT_DATATYPE =   "datatype";
        public static final String COL_ENUMERATION_VALUES =  NAME + ".enumeration_values";
        public static final String COL_NODOT_ENUMERATION_VALUES =   "enumeration_values";
        public static final String COL_PROPERTIES =  NAME + ".properties";
        public static final String COL_NODOT_PROPERTIES =   "properties";

        public static final String[] ARRAY = new String[] {
            COL_ID,COL_COLUMN_INDEX,COL_COLUMN_NAME,COL_DATATYPE,COL_ENUMERATION_VALUES,COL_PROPERTIES
        };
        public static final String COLUMNS = SqlUtil.comma(ARRAY);
        public static final String NODOT_COLUMNS = SqlUtil.commaNoDot(ARRAY);
        public static final String INSERT =SqlUtil.makeInsert(NAME, NODOT_COLUMNS,SqlUtil.getQuestionMarks(ARRAY.length));
    public static final TYPE_COLUMN table  = new  TYPE_COLUMN();
    }



    public static class USERROLES extends Tables {
        public static final String NAME = "userroles";

        public String getName() {return NAME;}
        public String getColumns() {return COLUMNS;}
        public static final String COL_USER_ID =  NAME + ".user_id";
        public static final String COL_NODOT_USER_ID =   "user_id";
        public static final String COL_ROLE =  NAME + ".role";
        public static final String COL_NODOT_ROLE =   "role";

        public static final String[] ARRAY = new String[] {
            COL_USER_ID,COL_ROLE
        };
        public static final String COLUMNS = SqlUtil.comma(ARRAY);
        public static final String NODOT_COLUMNS = SqlUtil.commaNoDot(ARRAY);
        public static final String INSERT =SqlUtil.makeInsert(NAME, NODOT_COLUMNS,SqlUtil.getQuestionMarks(ARRAY.length));
    public static final USERROLES table  = new  USERROLES();
    }



    public static class USERS extends Tables {
        public static final String NAME = "users";

        public String getName() {return NAME;}
        public String getColumns() {return COLUMNS;}
        public static final String COL_ID =  NAME + ".id";
        public static final String COL_NODOT_ID =   "id";
        public static final String COL_NAME =  NAME + ".name";
        public static final String COL_NODOT_NAME =   "name";
        public static final String COL_EMAIL =  NAME + ".email";
        public static final String COL_NODOT_EMAIL =   "email";
        public static final String COL_QUESTION =  NAME + ".question";
        public static final String COL_NODOT_QUESTION =   "question";
        public static final String COL_ANSWER =  NAME + ".answer";
        public static final String COL_NODOT_ANSWER =   "answer";
        public static final String COL_PASSWORD =  NAME + ".password";
        public static final String COL_NODOT_PASSWORD =   "password";
        public static final String COL_DESCRIPTION =  NAME + ".description";
        public static final String COL_NODOT_DESCRIPTION =   "description";
        public static final String COL_ADMIN =  NAME + ".admin";
        public static final String COL_NODOT_ADMIN =   "admin";
        public static final String COL_LANGUAGE =  NAME + ".language";
        public static final String COL_NODOT_LANGUAGE =   "language";
        public static final String COL_TEMPLATE =  NAME + ".template";
        public static final String COL_NODOT_TEMPLATE =   "template";
        public static final String COL_ISGUEST =  NAME + ".isguest";
        public static final String COL_NODOT_ISGUEST =   "isguest";
        public static final String COL_PROPERTIES =  NAME + ".properties";
        public static final String COL_NODOT_PROPERTIES =   "properties";

        public static final String[] ARRAY = new String[] {
            COL_ID,COL_NAME,COL_EMAIL,COL_QUESTION,COL_ANSWER,COL_PASSWORD,COL_DESCRIPTION, COL_ADMIN,COL_LANGUAGE,COL_TEMPLATE,COL_ISGUEST,COL_PROPERTIES
        };
        public static final String COLUMNS = SqlUtil.comma(ARRAY);
        public static final String NODOT_COLUMNS = SqlUtil.commaNoDot(ARRAY);
        public static final String INSERT =SqlUtil.makeInsert(NAME, NODOT_COLUMNS,SqlUtil.getQuestionMarks(ARRAY.length));
    public static final USERS table  = new  USERS();
    }



    public static class USER_ACTIVITY extends Tables {
        public static final String NAME = "user_activity";

        public String getName() {return NAME;}
        public String getColumns() {return COLUMNS;}
        public static final String COL_USER_ID =  NAME + ".user_id";
        public static final String COL_NODOT_USER_ID =   "user_id";
        public static final String COL_DATE =  NAME + ".date";
        public static final String COL_NODOT_DATE =   "date";
        public static final String COL_WHAT =  NAME + ".what";
        public static final String COL_NODOT_WHAT =   "what";
        public static final String COL_EXTRA =  NAME + ".extra";
        public static final String COL_NODOT_EXTRA =   "extra";
        public static final String COL_IPADDRESS =  NAME + ".ipaddress";
        public static final String COL_NODOT_IPADDRESS =   "ipaddress";

        public static final String[] ARRAY = new String[] {
            COL_USER_ID,COL_DATE,COL_WHAT,COL_EXTRA,COL_IPADDRESS
        };
        public static final String COLUMNS = SqlUtil.comma(ARRAY);
        public static final String NODOT_COLUMNS = SqlUtil.commaNoDot(ARRAY);
        public static final String INSERT =SqlUtil.makeInsert(NAME, NODOT_COLUMNS,SqlUtil.getQuestionMarks(ARRAY.length));
    public static final USER_ACTIVITY table  = new  USER_ACTIVITY();
    }



}














