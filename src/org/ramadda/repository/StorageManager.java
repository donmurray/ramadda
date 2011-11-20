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

package org.ramadda.repository;



import org.python.core.*;
import org.python.util.*;

import org.ramadda.repository.auth.*;
import org.ramadda.util.TempDir;


import org.w3c.dom.*;



import ucar.unidata.sql.SqlUtil;

import ucar.unidata.util.DateUtil;
import ucar.unidata.util.HtmlUtil;
import ucar.unidata.util.IOUtil;
import ucar.unidata.util.LogUtil;
import ucar.unidata.util.Misc;

import ucar.unidata.util.StringUtil;

import ucar.unidata.xml.XmlUtil;



import java.io.*;

import java.io.File;
import java.io.InputStream;



import java.lang.reflect.*;

import java.net.*;



import java.util.ArrayList;
import java.util.Date;
import java.util.Enumeration;
import java.util.GregorianCalendar;
import java.util.Hashtable;
import java.util.List;
import java.util.Properties;
import java.util.TimeZone;

import java.util.zip.*;




/**
 *
 *
 *
 * @author RAMADDA Development Team
 * @version $Revision: 1.3 $
 */
public class StorageManager extends RepositoryManager {


    /** _more_ */
    public static final String FILE_SEPARATOR = "_file_";

    /** _more_ */
    public static final String FILE_FULLLOG = "fullrepository.log";

    /** _more_ */
    public static final String FILE_LOG = "repository.log";

    /** _more_ */
    public static final String DIR_REPOSITORY = "repository";

    /** _more_ */
    public static final String DIR_ENTRIES = "entries";

    /** _more_ */
    public static final String DIR_USERS = "users";

    /** _more_ */
    public static final String DIR_STORAGE = "storage";

    /** _more_ */
    public static final String DIR_PLUGINS = "plugins";


    /** _more_ */
    public static final String DIR_BACKUPS = "backups";

    /** _more_ */
    public static final String DIR_RESOURCES = "resources";

    /** _more_ */
    public static final String DIR_HTDOCS = "htdocs";

    /** _more_ */
    public static final String DIR_ANONYMOUSUPLOAD = "anonymousupload";

    /** _more_ */
    public static final String DIR_LOGS = "logs";

    /** _more_ */
    public static final String DIR_INDEX = "index";

    /** _more_ */
    public static final String DIR_CACHE = "cache";

    /** _more_ */
    public static final String DIR_TMP = "tmp";

    /** _more_ */
    public static final String DIR_ICONS = "icons";

    /** _more_ */
    public static final String DIR_UPLOADS = "uploads";

    /** _more_ */
    public static final String DIR_SCRATCH = "scratch";

    /** _more_ */
    public static final String DIR_THUMBNAILS = "thumbnails";


    /** _more_ */
    public static final String PROP_DIRDEPTH = "ramadda.storage.dirdepth";

    /** _more_ */
    public static final String PROP_FASTDIR = "ramadda.storage.fastdir";

    /** _more_ */
    public static final String PROP_FASTDIRSIZE =
        "ramadda.storage.fastdirsize";

    /** _more_ */
    public static final String PROP_DIRRANGE = "ramadda.storage.dirrange";


    /** _more_ */
    private int dirDepth = 2;

    /** _more_ */
    private int dirRange = 10;


    /** _more_ */
    private File repositoryDir;

    /** _more_ */
    private File tmpDir;


    /** _more_ */
    private String htdocsDir;

    /** _more_ */
    private String iconsDir;

    /** _more_ */
    private List<TempDir> tmpDirs = new ArrayList<TempDir>();

    /** _more_ */
    private TempDir scratchDir;


    /** _more_ */
    private String anonymousDir;

    /** _more_ */
    private TempDir cacheDir;

    /** _more_ */
    private String logDir;

    /** _more_ */
    private long cacheDirSize = -1;

    /** _more_ */
    private String uploadDir;

    /** _more_ */
    private String entriesDir;

    /** _more_ */
    private String usersDir;

    /** _more_ */
    private File storageDir;

    /** _more_ */
    private File indexDir;

    /** _more_ */
    private TempDir thumbDir;

    /** _more_ */
    private List<File> downloadDirs = new ArrayList<File>();



    /**
     * _more_
     *
     * @param repository _more_
     */
    public StorageManager(Repository repository) {
        super(repository);
    }

    /**
     * _more_
     *
     * @param resource _more_
     *
     * @return _more_
     */
    public String resourceFromDB(String resource) {
        if (resource != null) {
            resource = resource.replace("${ramadda.storagedir}",
                                        getStorageDir().toString());
        }
        return resource;
    }

    /**
     * _more_
     *
     * @param resource _more_
     *
     * @return _more_
     */
    public String resourceToDB(String resource) {
        if (resource != null) {
            resource = resource.replace(getStorageDir().toString(),
                                        "${ramadda.storagedir}");
        }
        return resource;
    }


    /** _more_ */
    private boolean haveInitializedPython = false;


    /**
     * _more_
     */
    public void initPython() {
        if (haveInitializedPython) {
            return;
        }
        haveInitializedPython = true;
        String     pythonCacheDir = getCacheDir().toString();
        Properties pythonProps    = new Properties();
        pythonProps.put("python.home", pythonCacheDir);
        PythonInterpreter.initialize(System.getProperties(), pythonProps,
                                     new String[] {});
    }


    /**
     * _more_
     */
    protected void doFinalInitialization() {
        Misc.run(new Runnable() {
            public void run() {
                getCacheDir();
                getScratchDir();
                getThumbDir();
                scourTmpDirs();
            }
        });
    }



    /**
     * _more_
     *
     */
    protected void init() {
        String repositoryDirProperty =
            getRepository().getProperty(PROP_REPOSITORY_HOME, (String) null);
        if (repositoryDirProperty == null) {
            //Use the old <home>/.unidata/repository if its there
            repositoryDirProperty =
                IOUtil.joinDir(Misc.getSystemProperty("user.home", "."),
                               IOUtil.joinDir(".unidata", "repository"));
            //Else use  <home>/.ramadda
            if ( !new File(repositoryDirProperty).exists()) {
                repositoryDirProperty =
                    IOUtil.joinDir(Misc.getSystemProperty("user.home", "."),
                                   ".ramadda");
            } else {}
        }
        repositoryDir = new File(repositoryDirProperty);

        System.err.println("RAMADDA: home directory: " + repositoryDir);
        if ( !repositoryDir.exists()) {
            IOUtil.makeDirRecursive(repositoryDir);
        }

        if ( !repositoryDir.exists()) {
            System.err.println(
                "RAMADDA: error: home directory does not exist");
            throw new IllegalStateException(
                "RAMADDA: error: home directory does not exist");
        }

        if ( !repositoryDir.canWrite()) {
            System.err.println(
                "RAMADDA: error: home directory is not writable");
            throw new IllegalStateException(
                "RAMADDA: error: home directory is not writable");
        }


        htdocsDir = IOUtil.joinDir(repositoryDir, DIR_HTDOCS);
        IOUtil.makeDir(htdocsDir);
        String resourcesDir = IOUtil.joinDir(repositoryDir, DIR_RESOURCES);
        IOUtil.makeDir(resourcesDir);

        dirDepth = getRepository().getProperty(PROP_DIRDEPTH, dirDepth);
        dirRange = getRepository().getProperty(PROP_DIRRANGE, dirRange);
        getUploadDir();




    }





    /**
     * _more_
     *
     * @param sb _more_
     */
    public void addInfo(StringBuffer sb) {
        sb.append(HtmlUtil.formEntry("Home Directory:",
                                     getRepositoryDir().toString()));
        sb.append(HtmlUtil.formEntry("Storage Directory:",
                                     getStorageDir().toString()));
    }


    /**
     * _more_
     *
     *
     * @param dir _more_
     */
    public void addDownloadDirectory(File dir) {
        if ( !downloadDirs.contains(dir)) {
            downloadDirs.add(dir);
        }
    }



    /**
     * _more_
     *
     * @param path _more_
     *
     * @return _more_
     */
    public String localizePath(String path) {
        String repositoryDir = getRepositoryDir().toString();
        path = path.replace("%repositorydir%", repositoryDir);
        path = path.replace("${repositorydir}", repositoryDir);
        path = path.replace("%resourcedir%",
                            "/org/ramadda/repository/resources");
        return path;
    }

    /**
     * _more_
     *
     * @return _more_
     */
    public String getSystemResourcePath() {
        return "/org/ramadda/repository/resources";
    }



    /**
     * _more_
     *
     * @return _more_
     */
    public String getUploadDir() {
        if (uploadDir == null) {
            uploadDir = IOUtil.joinDir(getStorageDir(), DIR_UPLOADS);
            IOUtil.makeDirRecursive(new File(uploadDir));
        }
        return uploadDir;
    }

    /**
     * _more_
     *
     * @return _more_
     */
    public File getRepositoryDir() {
        return repositoryDir;
    }


    /**
     * _more_
     *
     * @param storageDir _more_
     */
    public void addTempDir(final TempDir storageDir) {
        tmpDirs.add(storageDir);
        Misc.runInABit(10000, new Runnable() {
            public void run() {
                scourTmpDir(storageDir, true);
            }
        });
    }

    /**
     * _more_
     *
     * @param dir _more_
     *
     * @return _more_
     */
    public TempDir makeTempDir(String dir) {
        return makeTempDir(dir, true);
    }

    /**
     * Make a temporary directory
     *
     * @param dir dir name
     * @param shouldScour should we scour this directory of old files
     *
     * @return _more_
     */
    public TempDir makeTempDir(String dir, boolean shouldScour) {
        TempDir tmpDir = new TempDir(IOUtil.joinDir(getTmpDir(), dir));
        IOUtil.makeDirRecursive(tmpDir.getDir());
        if (shouldScour) {
            addTempDir(tmpDir);
        }
        return tmpDir;
    }


    /**
     * _more_
     *
     * @param tmpDir _more_
     * @param file _more_
     *
     * @return _more_
     */
    public File getTmpDirFile(TempDir tmpDir, String file) {
        File f = new File(IOUtil.joinDir(tmpDir.getDir(), file));
        dirTouched(tmpDir, f);
        return checkFile(f);
    }


    /**
     * _more_
     *
     * @param file _more_
     *
     * @return _more_
     */
    public File getIconsDirFile(String file) {
        return new File(IOUtil.joinDir(getIconsDir(), file));
    }


    /**
     * _more_
     *
     * @return _more_
     */
    private String getIconsDir() {
        if (iconsDir == null) {
            iconsDir = IOUtil.joinDir(htdocsDir, DIR_ICONS);
            IOUtil.makeDirRecursive(new File(iconsDir));
        }
        return iconsDir;
    }





    /**
     * _more_
     *
     * @return _more_
     */
    public File getTmpDir() {
        if (tmpDir == null) {
            tmpDir = new File(IOUtil.joinDir(getRepositoryDir(), DIR_TMP));
            IOUtil.makeDirRecursive(tmpDir);
        }
        return tmpDir;
    }

    /**
     * _more_
     *
     * @return _more_
     */
    private TempDir getScratchDir() {
        if (scratchDir == null) {
            scratchDir = makeTempDir(DIR_SCRATCH);
            scratchDir.setMaxAge(DateUtil.hoursToMillis(1));
        }
        return scratchDir;
    }

    /**
     * _more_
     *
     * @param file _more_
     *
     * @return _more_
     */
    public File getUniqueScratchFile(String file) {
        File f = getScratchFile(file);
        while (f.exists()) {
            f = getScratchFile(getRepository().getGUID() + "_" + file);
        }
        return f;
    }


    /**
     * _more_
     *
     * @param file _more_
     *
     * @return _more_
     */
    public File getScratchFile(String file) {
        return getTmpDirFile(getScratchDir(), file);
    }


    /**
     * _more_
     *
     * @return _more_
     */
    private TempDir getThumbDir() {
        if (thumbDir == null) {
            thumbDir = makeTempDir(DIR_THUMBNAILS);
            thumbDir.setMaxFiles(1000);
            thumbDir.setMaxSize(1000 * 1000 * 1000);
        }
        return thumbDir;
    }

    /**
     * _more_
     *
     * @param file _more_
     *
     * @return _more_
     */
    public File getThumbFile(String file) {
        return getTmpDirFile(getThumbDir(), file);
    }


    /**
     * _more_
     *
     * @param file _more_
     *
     * @return _more_
     */
    public File getThumbDir(String file) {
        File f = getTmpDirFile(getThumbDir(), file);
        IOUtil.makeDirRecursive(f);
        return f;
    }



    /**
     * _more_
     *
     * @param file _more_
     *
     * @return _more_
     */
    public File getIconFile(String file) {
        return getTmpDirFile(getThumbDir(), file);
    }


    /**
     * _more_
     *
     * @return _more_
     */
    private TempDir getCacheDir() {
        if (cacheDir == null) {
            cacheDir = makeTempDir(DIR_CACHE);
            cacheDir.setMaxSize(1000 * 1000 * 1000);
        }
        return cacheDir;
    }


    /**
     * _more_
     *
     * @param file _more_
     *
     * @return _more_
     */
    public File getCacheFile(String file) {
        return getTmpDirFile(getCacheDir(), file);
    }


    /**
     * _more_
     *
     * @return _more_
     */
    public String getLogDir() {
        if (logDir == null) {
            logDir = IOUtil.joinDir(getRepositoryDir(), DIR_LOGS);

            File f = new File(logDir);
            IOUtil.makeDirRecursive(f);

            if (getRepository().isReadOnly()) {
                System.err.println("RAMADDA: skipping log4j");
                return logDir;
            }

            File log4JFile = new File(f + "/" + "log4j.properties");
            //For now always write out the log from the jar
            //System.err.println("log4j file=" + log4JFile);

            if (true || !log4JFile.exists()) {
                try {
                    String c =
                        IOUtil.readContents(
                            "/org/ramadda/repository/resources/log4j.properties",
                            getClass());
                    c = c.replace("${ramadda.logdir}", f.toString());
                    IOUtil.writeFile(log4JFile, c);
                } catch (Exception exc) {
                    throw new RuntimeException(exc);
                }
            }
            try {
                System.err.println(
                    "RAMADDA: Configuring log4j. Note: this may print out a stack trace.");
                org.apache.log4j.PropertyConfigurator.configure(
                    log4JFile.toString());
            } catch (Exception exc) {
                System.err.println("RAMADDA: Error configuring log4j:" + exc);
                exc.printStackTrace();
            }
        }
        return logDir;
    }


    /**
     * _more_
     *
     * @param tmpDir _more_
     * @param f _more_
     */
    public void dirTouched(final TempDir tmpDir, File f) {
        if (f != null) {
            f.setLastModified(new Date().getTime());
            //if the file is already there then don't scour
            if (f.exists()) {
                return;
            }
        }
        //Run this in 10 seconds
        if (tmpDir.getTouched()) {
            return;
        }
        tmpDir.setTouched(true);
        Misc.runInABit(10000, new Runnable() {
            public void run() {
                scourTmpDir(tmpDir);
            }
        });
    }


    /**
     * _more_
     */
    private void scourTmpDirs() {
        //Scour once an hour
        while (true) {
            List<TempDir> tmpTmpDirs = new ArrayList<TempDir>(tmpDirs);
            for (TempDir tmpDir : tmpTmpDirs) {
                scourTmpDir(tmpDir);
            }
            Misc.sleepSeconds(60 * 60);
        }
    }


    /**
     * _more_
     *
     * @param tmpDir _more_
     */
    protected void scourTmpDir(final TempDir tmpDir) {
        scourTmpDir(tmpDir, false);
    }

    /**
     * _more_
     *
     * @param tmpDir _more_
     * @param force _more_
     */
    protected void scourTmpDir(final TempDir tmpDir, boolean force) {
        synchronized (tmpDir) {
            //            if ( !force && !tmpDir.haveChanged()) {
            //                return;
            //            }
            List<File> filesToScour = tmpDir.findFilesToScour();
            if (filesToScour.size() > 0) {
                logInfo("StorageManager: scouring " + filesToScour.size()
                        + " files from:" + tmpDir.getDir().getName());
                System.err.println("StorageManager: scouring "
                                   + filesToScour.size() + " files from:"
                                   + tmpDir);
            }
            List<File> notDeleted = IOUtil.deleteFiles(filesToScour);
            if (notDeleted.size() > 0) {
                logInfo("Unable to delete tmp files:" + notDeleted);
            }
            //Now check for empty top level dirs and get rid of the
            for (File remainingFile : tmpDir.listFiles()) {
                if ( !remainingFile.isDirectory()) {
                    continue;
                }
                if (remainingFile.listFiles().length == 0) {
                    remainingFile.delete();
                }
            }

        }
        tmpDir.setTouched(false);
    }



    /**
     * _more_
     *
     * @return _more_
     */
    public String getAnonymousDir() {
        if (anonymousDir == null) {
            anonymousDir = IOUtil.joinDir(getStorageDir(),
                                          DIR_ANONYMOUSUPLOAD);
            IOUtil.makeDirRecursive(new File(anonymousDir));
        }
        return anonymousDir;
    }



    /**
     * _more_
     *
     * @return _more_
     */
    public String getStorageDir() {
        if (storageDir == null) {
            storageDir = new File(IOUtil.joinDir(getRepositoryDir(),
                    DIR_STORAGE));
            IOUtil.makeDirRecursive(storageDir);
            addDownloadDirectory(storageDir);
        }
        return storageDir.toString();
    }

    /**
     * _more_
     *
     * @return _more_
     */
    public String getIndexDir() {
        if (indexDir == null) {
            indexDir = new File(IOUtil.joinDir(getRepositoryDir(),
                    DIR_INDEX));
            IOUtil.makeDirRecursive(indexDir);
        }
        return indexDir.toString();
    }

    /**
     * _more_
     *
     * @return _more_
     */
    public String getPluginsDir() {
        String dir = IOUtil.joinDir(getRepositoryDir(), DIR_PLUGINS);
        IOUtil.makeDirRecursive(new File(dir));
        return dir;
    }

    /**
     * _more_
     *
     * @return _more_
     */
    public String getBackupsDir() {
        String dir = IOUtil.joinDir(getRepositoryDir(), DIR_BACKUPS);
        IOUtil.makeDirRecursive(new File(dir));
        return dir;
    }

    /**
     * _more_
     *
     * @param name _more_
     *
     * @return _more_
     */
    public String getDir(String name) {
        String dir = IOUtil.joinDir(getRepositoryDir(), name);
        IOUtil.makeDirRecursive(new File(dir));
        return dir;
    }






    /**
     * _more_
     *
     * @param request _more_
     * @param name _more_
     *
     * @return _more_
     */
    public File getTmpFile(Request request, String name) {
        return getTmpDirFile(getScratchDir(),
                             getRepository().getGUID() + FILE_SEPARATOR
                             + name);
    }


    /**
     * _more_
     *
     * @param request _more_
     * @param original _more_
     *
     * @return _more_
     *
     * @throws Exception _more_
     */
    public File moveToStorage(Request request, File original)
            throws Exception {
        return moveToStorage(request, original, original.getName());
    }



    /**
     * _more_
     *
     * @param id _more_
     *
     * @return _more_
     */
    private String cleanEntryId(String id) {
        return IOUtil.cleanFileName(id);
    }

    /**
     * _more_
     *
     *
     * @param id _more_
     * @param createIfNeeded _more_
     * @return _more_
     */
    public File getEntryDir(String id, boolean createIfNeeded) {
        id = cleanEntryId(id);
        if (entriesDir == null) {
            entriesDir = IOUtil.joinDir(getRepositoryDir(), DIR_ENTRIES);
            IOUtil.makeDirRecursive(new File(entriesDir));
        }
        File entryDir = new File(IOUtil.joinDir(entriesDir, id));
        //The old way
        if (entryDir.exists()) {
            return entryDir;
        }

        String dir1 = "entry_" + ((id.length() >= 2)
                                  ? id.substring(0, 2)
                                  : "");
        String dir2 = "entry_" + ((id.length() >= 4)
                                  ? id.substring(2, 4)
                                  : "");
        entryDir = new File(IOUtil.joinDir(entriesDir,
                                           IOUtil.joinDir(dir1,
                                               IOUtil.joinDir(dir2, id))));
        //        System.err.println("entrydir:" + entryDir);
        if (createIfNeeded) {
            IOUtil.makeDirRecursive(entryDir);
        }
        return entryDir;
    }


    /**
     * _more_
     *
     * @param id _more_
     * @param createIfNeeded _more_
     *
     * @return _more_
     */
    public File getUserDir(String id, boolean createIfNeeded) {
        id = IOUtil.cleanFileName(id);
        if (usersDir == null) {
            usersDir = IOUtil.joinDir(getRepositoryDir(), DIR_USERS);
            IOUtil.makeDirRecursive(new File(usersDir));
        }

        String dir1 = "user_" + ((id.length() >= 2)
                                 ? id.substring(0, 2)
                                 : "");
        String dir2 = "user_" + ((id.length() >= 4)
                                 ? id.substring(2, 4)
                                 : "");
        File userDir = new File(IOUtil.joinDir(usersDir,
                           IOUtil.joinDir(dir1, IOUtil.joinDir(dir2, id))));
        if (createIfNeeded) {
            IOUtil.makeDirRecursive(userDir);
        }
        return userDir;
    }



    /**
     * _more_
     *
     * @param id _more_
     */
    public void deleteEntryDir(final String id) {
        Misc.run(new Runnable() {
            public void run() {
                File dir = getEntryDir(id, false);
                if (dir.exists()) {
                    IOUtil.deleteDirectory(dir);
                }
            }
        });
    }


    /**
     * _more_
     *
     * @param entry _more_
     * @param original _more_
     *
     * @return _more_
     *
     * @throws Exception _more_
     */
    public File moveToEntryDir(Entry entry, File original) throws Exception {
        File newFile = new File(IOUtil.joinDir(getEntryDir(entry.getId(),
                           true), original.getName()));
        moveFile(original, newFile);
        return newFile;
    }

    /**
     * _more_
     *
     * @param entry _more_
     * @param original _more_
     *
     * @return _more_
     *
     * @throws Exception _more_
     */
    public File copyToEntryDir(Entry entry, File original) throws Exception {
        File newFile = new File(IOUtil.joinDir(getEntryDir(entry.getId(),
                           true), original.getName()));
        copyFile(original, newFile);
        return newFile;
    }

    /**
     * _more_
     *
     * @param oldEntry _more_
     * @param newEntry _more_
     * @param filename _more_
     *
     * @return _more_
     *
     * @throws Exception _more_
     */
    public String copyToEntryDir(Entry oldEntry, Entry newEntry,
                                 String filename)
            throws Exception {
        File oldFile = new File(IOUtil.joinDir(getEntryDir(oldEntry.getId(),
                           true), filename));

        File newFile = new File(IOUtil.joinDir(getEntryDir(newEntry.getId(),
                           true), filename));

        //If it is a URL then copy it over
        if ( !oldFile.exists()) {
            URL         url        = new URL(filename);
            InputStream fromStream = getInputStream(filename);

            newFile = new File(IOUtil.joinDir(getEntryDir(newEntry.getId(),
                    true), IOUtil.getFileTail(url.getPath())));


            FileOutputStream toStream = getFileOutputStream(newFile);
            IOUtil.writeTo(fromStream, toStream);
            IOUtil.close(fromStream);
            IOUtil.close(toStream);
            return newFile.getName();
        }


        copyFile(oldFile, newFile);
        return newFile.getName();
    }

    /**
     * _more_
     *
     * @param oldFile _more_
     * @param newFile _more_
     *
     * @throws Exception _more_
     */
    private void copyFile(File oldFile, File newFile) throws Exception {
        checkLocalFile(oldFile);
        checkWriteFile(newFile);
        IOUtil.copyFile(oldFile, newFile);
    }



    /**
     * _more_
     *
     * @param request _more_
     * @param original _more_
     * @param targetName _more_
     *
     * @return _more_
     *
     * @throws Exception _more_
     */
    public File moveToStorage(Request request, File original,
                              String targetName)
            throws Exception {
        if (targetName == null) {
            targetName = original.getName();
        }
        String storageDir = getStorageDir();
        GregorianCalendar cal =
            new GregorianCalendar(RepositoryUtil.TIMEZONE_DEFAULT);
        cal.setTime(new Date());
        storageDir = IOUtil.joinDir(storageDir, "y" + cal.get(cal.YEAR));
        IOUtil.makeDir(storageDir);
        storageDir = IOUtil.joinDir(storageDir,
                                    "m" + (cal.get(cal.MONTH) + 1));
        IOUtil.makeDir(storageDir);
        storageDir = IOUtil.joinDir(storageDir,
                                    "d" + cal.get(cal.DAY_OF_MONTH));
        IOUtil.makeDir(storageDir);

        for (int depth = 0; depth < dirDepth; depth++) {
            int index = (int) (dirRange * Math.random());
            storageDir = IOUtil.joinDir(storageDir, "data" + index);
            IOUtil.makeDir(storageDir);
        }

        File newFile = new File(IOUtil.joinDir(storageDir, targetName));
        moveFile(original, newFile);
        return newFile;
    }



    /**
     * _more_
     *
     * @param request _more_
     * @param original _more_
     * @param prefix _more_
     *
     * @return _more_
     *
     * @throws Exception _more_
     */
    public File moveToAnonymousStorage(Request request, File original,
                                       String prefix)
            throws Exception {
        String storageDir = getAnonymousDir();
        File[] files      = new File(storageDir).listFiles();
        long   size       = 0;
        for (int i = 0; i < files.length; i++) {
            if (files[i].isHidden()) {
                continue;
            }
            size += files[i].length();
        }

        double sizeThresholdGB =
            getRepository().getProperty(PROP_UPLOAD_MAXSIZEGB, 10.0);
        if (size + original.length() > sizeThresholdGB * 1000 * 1000 * 1000) {
            throw new IllegalArgumentException(
                "Anonymous upload area exceeded capacity");
        }

        String fileName = original.getName();
        fileName = HtmlUtil.urlEncode(fileName);
        String targetName = prefix + fileName;
        File   newFile    = new File(IOUtil.joinDir(storageDir, targetName));
        moveFile(original, newFile);
        return newFile;
    }


    /**
     * _more_
     *
     * @param request _more_
     * @param original _more_
     * @param newName _more_
     *
     * @return _more_
     *
     * @throws Exception _more_
     */
    public File copyToStorage(Request request, File original, String newName)
            throws Exception {
        return copyToStorage(request, getFileInputStream(original), newName);
    }

    /**
     * _more_
     *
     * @param request _more_
     * @param original _more_
     * @param newName _more_
     *
     * @return _more_
     *
     * @throws Exception _more_
     */
    public File copyToStorage(Request request, InputStream original,
                              String newName)
            throws Exception {
        String targetName = newName;
        String storageDir = getStorageDir();

        GregorianCalendar cal =
            new GregorianCalendar(RepositoryUtil.TIMEZONE_DEFAULT);
        cal.setTime(new Date());

        storageDir = IOUtil.joinDir(storageDir, "y" + cal.get(cal.YEAR));
        IOUtil.makeDir(storageDir);
        storageDir = IOUtil.joinDir(storageDir,
                                    "m" + (cal.get(cal.MONTH) + 1));
        IOUtil.makeDir(storageDir);
        storageDir = IOUtil.joinDir(storageDir,
                                    "d" + cal.get(cal.DAY_OF_MONTH));
        IOUtil.makeDir(storageDir);


        for (int depth = 0; depth < dirDepth; depth++) {
            int index = (int) (dirRange * Math.random());
            storageDir = IOUtil.joinDir(storageDir, "data" + index);
            IOUtil.makeDir(storageDir);
        }

        File newFile = new File(IOUtil.joinDir(storageDir, targetName));
        IOUtil.copyFile(original, newFile);
        IOUtil.close(original);
        return newFile;
    }

    /**
     * _more_
     *
     * @param fileName _more_
     *
     * @return _more_
     */
    public File getUploadFilePath(String fileName) {
        return checkFile(new File(IOUtil.joinDir(getUploadDir(),
                getStorageFileName(fileName))));
    }


    /**
     * _more_
     *
     * @param fileName _more_
     *
     * @return _more_
     */
    public String getStorageFileName(String fileName) {
        return repository.getGUID() + FILE_SEPARATOR + fileName;
    }


    /**
     * _more_
     *
     * @param entry _more_
     *
     * @return _more_
     */
    public String getFileTail(Entry entry) {
        String tail;
        if (entry.getIsLocalFile()) {
            tail = IOUtil.getFileTail(entry.getResource().getPath());
        } else {
            tail = getFileTail(entry.getResource().getPath());
        }

        return tail;
    }

    /**
     * _more_
     *
     * @param fileName _more_
     *
     * @return _more_
     */
    public static String getFileTail(String fileName) {
        return RepositoryUtil.getFileTail(fileName);
    }

    /**
     * _more_
     *
     * @param request _more_
     * @param entry _more_
     *
     * @return _more_
     *
     * @throws Exception _more_
     */
    public boolean canDownload(Request request, Entry entry)
            throws Exception {
        Resource resource = entry.getResource();
        if ( !resource.isFile()) {
            return false;
        }
        File file = resource.getTheFile();

        //This is for the ftptypehandler where it caches the file
        if (resource.isRemoteFile()) {
            return true;
        }

        //Check if its in the storage dir or under of the harvester dirs
        if (isInDownloadArea(file)) {
            return true;
        }

        //Check if its under one of the local file dirs defined by the admin
        if (isLocalFileOk(file)) {
            return true;
        }


        return false;
    }


    /**
     * _more_
     *
     * @param entry _more_
     *
     * @return _more_
     */
    public String getFastResourcePath(Entry entry) {
        String fastDir = getRepository().getProperty(PROP_FASTDIR,
                             (String) null);
        if ((fastDir == null) || !entry.isFile()) {
            return entry.getResource().getPath();
        }

        File f = entry.getTypeHandler().getFileForEntry(entry);
        if ( !f.exists()) {
            return entry.getResource().getPath();
        }
        //TODO: do this


        return f.toString();
    }


    /**
     * _more_
     *
     * @param entry _more_
     */
    public void removeFile(Entry entry) {
        removeFile(entry.getResource());
    }

    /**
     * _more_
     *
     * @param resource _more_
     */
    public void removeFile(Resource resource) {
        if (resource.isStoredFile()) {
            deleteFile(resource.getTheFile());
        }
    }


    /**
     * _more_
     *
     * @param f _more_
     */
    public void deleteFile(File f) {
        f.delete();
    }



    /**
     * _more_
     *
     *
     * @param file _more_
     *
     * @return _more_
     *
     * @throws Exception _more_
     */
    public boolean isInDownloadArea(File file) throws Exception {
        //Force the creation of the storage dir
        getStorageDir();
        for (File dir : downloadDirs) {
            if (IOUtil.isADescendent(dir, file)) {
                return true;
            }
        }
        return false;
    }






    /**
     * _more_
     *
     * @param file _more_
     *
     * @throws Exception _more_
     */
    public void checkLocalFile(File file) throws Exception {
        if ( !isLocalFileOk(file)) {
            System.err.println(
                "StorageManager.checkLocalFile bad file location:" + file);
            throw new AccessException(
                "The specified file is not under one of the allowable file system directories<br>These need to be set by the site administrator",
                null);
        }
    }


    /**
     * _more_
     *
     * @param file _more_
     *
     * @return _more_
     */
    public boolean isLocalFileOk(File file) {
        boolean ok = false;
        //        System.err.println ("StorageManager: isLocalFileOk:" +file);
        for (File parent : getRepository().getLocalFilePaths()) {
            //            System.err.println ("      Checking:" +parent);
            if (IOUtil.isADescendent(parent, file)) {
                //                System.err.println ("      OK");
                return true;
            }
        }
        //        System.err.println ("      NOT OK");
        return false;
    }



    /**
     * _more_
     *
     * @param f _more_
     */
    private void throwBadFile(File f) {
        throw new IllegalArgumentException(
            "The file:" + f
            + " is not under one of the allowable file system directories", null);
    }

    /**
     * _more_
     *
     * @param file _more_
     *
     * @return _more_
     */
    public File checkWriteFile(File file) {
        if (getRepository().isReadOnly()) {
            throw new IllegalArgumentException("Unable to write to file");
        }
        getStorageDir();
        if (IOUtil.isADescendent(getRepositoryDir(), file)) {
            return file;
        }
        throwBadFile(file);
        return null;
    }


    /**
     * _more_
     *
     * @param file _more_
     *
     * @return _more_
     */
    public File checkFile(File file) {
        //check if its in an allowable area for access
        if (isLocalFileOk(file)) {
            return file;
        }
        getStorageDir();
        //Check if its in the storage dir
        if (IOUtil.isADescendent(getRepositoryDir(), file)) {
            return file;
        }

        throwBadFile(file);
        return null;
    }


    /**
     *
     * @param path _more_
     *
     * @throws Exception _more_
     */
    public void checkPath(String path) throws Exception {
        //Path can be a file, a URL, a file URL or a system resource
        File f = new File(path);
        if (f.exists()) {
            checkFile(f);
            return;
        }

        if (path.toLowerCase().trim().startsWith("file:")) {
            f = new File(path.substring("file:".length()));
            checkFile(f);
            return;
        }

        //Should be ok here. Its either a url or a system resource
    }



    /**
     * _more_
     *
     * @param path _more_
     *
     * @return _more_
     *
     * @throws Exception _more_
     */
    public String readUncheckedSystemResource(String path) throws Exception {
        //        checkPath(path);
        return IOUtil.readContents(path, getClass());
    }

    /**
     * _more_
     *
     * @param from _more_
     * @param to _more_
     *
     * @throws Exception _more_
     */
    public void moveFile(File from, File to) throws Exception {
        checkFile(from);
        checkWriteFile(to);
        try {
            IOUtil.moveFile(from, to);
        } catch (Exception exc) {
            //Handle the windows file move problem
            if (to.isDirectory()) {
                to = new File(
                    IOUtil.joinDir(to, IOUtil.getFileTail(from.toString())));
            }
            IOUtil.copyFile(from, to);
            if ( !from.delete()) {
                from.deleteOnExit();
            }
        }
    }


    /**
     * _more_
     *
     * @param path _more_
     * @param dflt _more_
     *
     * @return _more_
     *
     * @throws Exception _more_
     */
    public String readUncheckedSystemResource(String path, String dflt)
            throws Exception {
        //        checkPath(path);
        return IOUtil.readContents(path, getClass(), dflt);
    }


    /**
     * _more_
     *
     * @param url _more_
     *
     * @return _more_
     *
     * @throws Exception _more_
     */
    public String readSystemResource(URL url) throws Exception {
        checkPath(url.toString());
        return IOUtil.readContents(url.toString(), getClass());
    }


    /**
     * _more_
     *
     * @param file _more_
     *
     * @return _more_
     *
     * @throws Exception _more_
     */
    public String readSystemResource(File file) throws Exception {
        return IOUtil.readInputStream(getFileInputStream(file));
    }


    /**
     * _more_
     *
     * @param path _more_
     *
     * @return _more_
     *
     * @throws Exception _more_
     */
    public String readSystemResource(String path) throws Exception {
        InputStream stream = getInputStream(path);
        try {
            return IOUtil.readInputStream(stream);
        } finally {
            IOUtil.close(stream);
        }
    }


    /**
     * _more_
     *
     * @param path _more_
     *
     * @return _more_
     *
     * @throws Exception _more_
     */
    public InputStream getInputStream(String path) throws Exception {
        checkPath(path);
        return IOUtil.getInputStream(path, getClass());
    }

    /**
     * _more_
     *
     * @param path _more_
     *
     * @return _more_
     *
     * @throws Exception _more_
     */
    public FileInputStream getFileInputStream(String path) throws Exception {
        return getFileInputStream(new File(path));
    }

    /**
     * This creates a FileOutputStream. It does enforce that the file is under
     * the main ramadda directory and it DOES check for READONLY mode
     *
     * @param file The file to write to
     *
     * @return FileOutputStream
     *
     * @throws Exception If the file is not under the main ramadda dir
     */
    public FileInputStream getFileInputStream(File file) throws Exception {
        checkFile(file);
        return new FileInputStream(file);
    }

    /**
     * _more_
     *
     * @param file _more_
     *
     * @return _more_
     *
     * @throws Exception _more_
     */
    public FileOutputStream getFileOutputStream(File file) throws Exception {
        checkWriteFile(file);
        return getUncheckedFileOutputStream(file);
    }


    /**
     * This creates a FileOutputStream. It does enforce that the file is under
     * the main ramadda directory. It DOES NOT check for READONLY mode
     *
     * @param file The file to write to
     *
     * @return FileOutputStream
     *
     * @throws Exception If the file is not under the main ramadda dir
     */
    public FileOutputStream getUncheckedFileOutputStream(File file)
            throws Exception {
        if ( !IOUtil.isADescendent(getRepositoryDir(), file)) {
            throw new IllegalArgumentException("Cannot write to:" + file);
        }
        return new FileOutputStream(file);
    }



}
