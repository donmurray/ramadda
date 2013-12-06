/*
* Copyright 2008-2013 Geode Systems LLC
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

package org.ramadda.repository.auth;


import org.ramadda.repository.*;
import org.ramadda.repository.database.*;
import org.ramadda.repository.output.*;
import org.ramadda.repository.type.*;

import org.ramadda.sql.Clause;
import org.ramadda.sql.SqlUtil;

import org.ramadda.util.HtmlTemplate;
import org.ramadda.util.HtmlUtils;
import org.ramadda.util.Utils;

import ucar.unidata.util.Misc;
import ucar.unidata.util.StringUtil;
import ucar.unidata.util.TwoFacedObject;
import ucar.unidata.xml.XmlUtil;

import java.io.UnsupportedEncodingException;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;


import java.security.SignatureException;

import java.sql.ResultSet;
import java.sql.Statement;


import java.util.ArrayList;
import java.util.Date;
import java.util.Hashtable;
import java.util.List;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;


/**
 * Handles user stuff
 *
 * @author Jeff McWhirter
 */
public class UserManager extends RepositoryManager {

    /** output type */
    public static final OutputType OUTPUT_CART_ADD =
        new OutputType("Add to Cart", "user.cart.add",
                       OutputType.TYPE_TOOLBAR, "", ICON_CART_ADD);

    /** output type */
    public static final OutputType OUTPUT_CART_REMOVE =
        new OutputType("Remove from Cart", "user.cart.remove",
                       OutputType.TYPE_ACTION, "", ICON_CART_DELETE);

    /** output type */
    public static final OutputType OUTPUT_FAVORITE =
        new OutputType("Add as Favorite", "user.addfavorite",
                       OutputType.TYPE_TOOLBAR, "", ICON_FAVORITE);

    /** role */
    public static final String ROLE_ANY = "any";

    /** role */
    public static final String ROLE_NONE = "none";

    /** _more_ */
    public static final String ROLE_USER = "user";

    /** _more_ */
    public static final String ROLE_ANONYMOUS = "anonymous";

    /** _more_ */
    public static final String ROLE_GUEST = "guest";

    /** _more_ */
    public static final String ROLE_INHERIT = "inherit";


    /** _more_ */
    public static final String PROP_LOGIN_ALLOWEDIPS =
        "ramadda.login.allowedips";

    /** _more_ */
    public static final String PROP_PASSWORD_DIGEST =
        "ramadda.password.hash.digest";


    /** _more_ */
    public static final String PROP_PASSWORD_ITERATIONS =
        "ramadda.password.hash.iterations";

    /** _more_ */
    public static final String PROP_PASSWORD_SALT =
        "ramadda.password.hash.salt";


    /** _more_ */
    public static final String PROP_PASSWORD_SALT1 =
        "ramadda.password.hash.salt1";

    /** _more_ */
    public static final String PROP_PASSWORD_SALT2 =
        "ramadda.password.hash.salt2";

    /** activity type for logging */
    public static final String ACTIVITY_LOGIN = "login";

    /** activity type for logging */
    public static final String ACTIVITY_LOGOUT = "logout";

    /** activity type for logging */
    public static final String ACTIVITY_PASSWORD_CHANGE = "password.change";

    /** _more_ */
    private static final String USER_DEFAULT = "default";

    /** _more_ */
    public static final String USER_ANONYMOUS = "anonymous";

    /** _more_ */
    public static final String USER_LOCALFILE = "localuser";

    /** _more_ */
    public final RequestUrl URL_USER_NEW_FORM = new RequestUrl(this,
                                                    "/user/new/form");

    /** _more_ */
    public final RequestUrl URL_USER_NEW_DO = new RequestUrl(this,
                                                  "/user/new/do");



    /** _more_ */
    public static final String PROP_SHOW_HELP = "ramadda.html.show.help";

    /** _more_ */
    public static final String PROP_SHOW_CART = "ramadda.html.show.cart";



    /** urls to use when the user is logged in */
    protected List<RequestUrl> userUrls =
        RequestUrl.toList(new RequestUrl[] {
            getRepositoryBase().URL_USER_HOME,
            getRepositoryBase().URL_USER_FORM,
            getRepositoryBase().URL_USER_CART,
            getRepositoryBase().URL_USER_MONITORS });

    /** _more_ */
    protected List<RequestUrl> remoteUserUrls =
        RequestUrl.toList(new RequestUrl[] {
            getRepositoryBase().URL_USER_HOME,
            getRepositoryBase().URL_USER_CART,
            getRepositoryBase().URL_USER_MONITORS });


    /** urls to use with no user */
    protected List<RequestUrl> anonUserUrls =
        RequestUrl.toList(new RequestUrl[] {
            getRepositoryBase().URL_USER_CART });


    /** List of ip addresses (or prefixes) that control where users can login from */
    private List<String> allowedIpsForLogin;


    /** _more_ */
    private Hashtable<String, User> userMap = new Hashtable<String, User>();

    /** Holds the users data cart. This of course is ephemeral */
    private Hashtable<String, List<Entry>> userCart = new Hashtable<String,
                                                          List<Entry>>();



    /** any external user authenticators from plugins */
    private List<UserAuthenticator> userAuthenticators =
        new ArrayList<UserAuthenticator>();

    /** holds password reset information */
    private Hashtable<String, PasswordReset> passwordResets =
        new Hashtable<String, PasswordReset>();


    /** _more_          */
    private boolean debug = false;

    /** _more_ */
    private String salt;

    /** _more_ */
    private String salt1;

    /** _more_ */
    private String salt2;


    /**
     * ctor
     *
     * @param repository the repository
     */
    public UserManager(Repository repository) {
        super(repository);
    }


    /**
     * _more_
     *
     * @param msg _more_
     */
    public void debugLogin(String msg) {
        if (debug) {
            //System.err.println(getRepository().debugPrefix() + ":" + msg);
            System.err.println(msg);
        }
    }

    /**
     * add the user authenticator
     *
     * @param userAuthenticator user authenticator
     */
    public void addUserAuthenticator(UserAuthenticator userAuthenticator) {
        userAuthenticators.add(userAuthenticator);
        if (userAuthenticator instanceof UserAuthenticatorImpl) {
            ((UserAuthenticatorImpl) userAuthenticator).setRepository(
                getRepository());
        }
    }



    /**
     * Is login allowed for the given request. This checks the allowed ip addresses
     *
     * @param request the request
     *
     * @return can do login
     */
    public boolean canDoLogin(Request request) {
        if (getRepository().isReadOnly()) {
            return false;
        }

        if (allowedIpsForLogin.size() > 0) {
            String requestIp = request.getIp();
            if (requestIp == null) {
                return false;
            }
            for (String ip : allowedIpsForLogin) {
                if (requestIp.startsWith(ip)) {
                    return true;
                }
            }

            //If there were any ips and none matched then return false
            return false;
        }

        return true;
    }


    /**
     * _more_
     *
     * @param request the request
     * @param result _more_
     *
     * @return The result
     */
    private Result addHeader(Request request, Result result) {
        try {
            return addHeaderToAncillaryPage(request, result);
        } catch (Exception exc) {
            throw new RuntimeException(exc);
        }
    }


    /**
     * initial the list of users from the command line
     *
     *
     * @param cmdLineUsers users to initialize
     * @throws Exception On badness
     */
    public void initUsers(List<User> cmdLineUsers) throws Exception {
        debug = getProperty("ramadda.debug.login", false);
        salt  = getProperty(PROP_PASSWORD_SALT, "");
        salt1 = getProperty(PROP_PASSWORD_SALT1, "");
        salt2 = getProperty(PROP_PASSWORD_SALT2, "");
        allowedIpsForLogin =
            StringUtil.split(getProperty(PROP_LOGIN_ALLOWEDIPS, ""), ",",
                             true, true);



        makeUserIfNeeded(new User(USER_DEFAULT, "Default User"));
        makeUserIfNeeded(new User(USER_ANONYMOUS, "Anonymous"));
        makeUserIfNeeded(new User(USER_LOCALFILE, "Local Files"));

        for (User user : cmdLineUsers) {
            //If it was from the cmd line then the password is not hashed
            user.setPassword(hashPassword(user.getPassword()));
            makeOrUpdateUser(user, true);
        }


        //If we have an admin property then it is of the form userid:password           
        //and is used to set the password of the admin                                  
        //Use localProperties so plugins can't slide in an admin password
        String adminFromProperties =
            getRepository().getLocalProperty(PROP_ADMIN, null);
        if (adminFromProperties != null) {
            List<String> toks = StringUtil.split(adminFromProperties, ":");
            if (toks.size() != 2) {
                getLogManager().logError("Error: The " + PROP_ADMIN
                                         + " property is incorrect");

                return;
            }
            User   user        = new User(toks.get(0), "", false);
            String rawPassword = toks.get(1).trim();
            user.setPassword(hashPassword(rawPassword));
            if ( !userExistsInDatabase(user)) {
                makeOrUpdateUser(user, true);
            } else {
                changePassword(user);
            }
            logInfo("Password for:" + user.getId() + " has been updated");
        }

        for (UserAuthenticator userAuthenticator : userAuthenticators) {
            userAuthenticator.initUsers();
        }
    }







    /**
     * _more_
     *
     * @param password _more_
     *
     * @return _more_
     *
     * @throws Exception _more_
     */
    private String getPasswordToUse(String password) throws Exception {
        //If we have a salt then use a generated hmac as the password to hash
        if (salt.length() != 0) {
            debugLogin("Has SALT:" + salt);

            return calculateRFC2104HMAC(password, salt);
        }

        return password;
    }

    //From: http://docs.aws.amazon.com/AWSSimpleQueueService/latest/SQSDeveloperGuide/AuthJavaSampleHMACSignature.html

    /**
     * _more_
     *
     * @param data _more_
     * @param key _more_
     *
     * @return _more_
     *
     * @throws java.security.SignatureException _more_
     */
    public static String calculateRFC2104HMAC(String data, String key)
            throws java.security.SignatureException {
        try {
            String HMAC_SHA1_ALGORITHM = "HmacSHA1";
            // get an hmac_sha1 key from the raw key bytes
            SecretKeySpec signingKey = new SecretKeySpec(key.getBytes(),
                                           HMAC_SHA1_ALGORITHM);


            // get an hmac_sha1 Mac instance and initialize with the signing key
            Mac mac = Mac.getInstance(HMAC_SHA1_ALGORITHM);
            mac.init(signingKey);

            // compute the hmac on input data bytes
            byte[] rawHmac = mac.doFinal(data.getBytes());

            // base64-encode the hmac
            return RepositoryUtil.encodeBase64(rawHmac);
        } catch (Exception e) {
            throw new SignatureException("Failed to generate HMAC : "
                                         + e.getMessage());
        }
    }



    /**
     * hash the given raw text password for storage into the database
     *
     * @param password raw text password
     *
     * @return hashed password
     */
    public String hashPassword(String password) {
        try {
            return PasswordHash.createHash(getPasswordToUse(password));
        } catch (Exception exc) {
            throw new RuntimeException(exc);
        }
    }






    /**
     * _more_
     *
     * @param request the request
     * @param user The user
     *
     * @return _more_
     *
     * @throws Exception On badness
     */
    public List<FavoriteEntry> getFavorites(Request request, User user)
            throws Exception {
        if (user.getAnonymous()) {
            return new ArrayList<FavoriteEntry>();
        }
        List<FavoriteEntry> favorites = user.getFavorites();
        if (favorites == null) {
            favorites = new ArrayList<FavoriteEntry>();
            Statement statement = getDatabaseManager().select(
                                      Tables.FAVORITES.COLUMNS,
                                      Tables.FAVORITES.NAME,
                                      Clause.eq(
                                          Tables.FAVORITES.COL_USER_ID,
                                          user.getId()));
            SqlUtil.Iterator iter =
                getDatabaseManager().getIterator(statement);
            ResultSet results;
            //COL_ID,COL_USER_ID,COL_ENTRY_ID,COL_NAME,COL_CATEGORY
            while ((results = iter.getNext()) != null) {
                int    col    = 1;
                String id     = results.getString(col++);
                String userId = results.getString(col++);
                Entry entry = getEntryManager().getEntry(request,
                                  results.getString(col++));
                String name     = results.getString(col++);
                String category = results.getString(col++);
                if (entry == null) {
                    getDatabaseManager().delete(
                        Tables.FAVORITES.NAME,
                        Clause.and(
                            Clause.eq(
                                Tables.FAVORITES.COL_USER_ID,
                                user.getId()), Clause.eq(
                                    Tables.FAVORITES.COL_ID, id)));

                    continue;
                }
                favorites.add(new FavoriteEntry(id, entry, name, category));
            }
            user.setFavorites(favorites);
        }

        return favorites;
    }



    /**
     * _more_
     *
     * @param user The user
     *
     * @return _more_
     */
    public User getCurrentUser(User user) {
        if (user == null) {
            return null;
        }
        User currentUser = userMap.get(user.getId());
        if (currentUser != null) {
            return currentUser;
        }

        return user;
    }

    /**
     * _more_
     *
     * @param request the request
     *
     * @return _more_
     */
    public boolean isRequestOk(Request request) {
        User user = request.getUser();
        if (getProperty(PROP_ACCESS_ADMINONLY, false) && !user.getAdmin()) {
            getRepository().debugSession("isRequestOK: Admin only");
            if ( !request.getRequestPath().startsWith(
                    getRepository().getUrlBase() + "/user/")) {
                return false;
            }
        }

        if (getProperty(PROP_ACCESS_REQUIRELOGIN, false)
                && user.getAnonymous()) {
            if ( !request.getRequestPath().startsWith(
                    getRepository().getUrlBase() + "/user/")) {
                getRepository().debugSession(
                    "isRequestOk: login is required ");

                return false;
            }
        }

        return true;
    }


    /**
     * _more_
     *
     * @param request the request
     *
     * @return _more_
     */
    public String makeLoginForm(Request request) {
        return makeLoginForm(request, "");
    }

    /**
     * _more_
     *
     * @param request the request
     * @param extra _more_
     *
     * @return _more_
     */
    public String makeLoginForm(Request request, String extra) {
        StringBuffer sb = new StringBuffer();
        request.appendMessage(sb);
        if ( !canDoLogin(request)) {
            sb.append(
                getPageHandler().showDialogWarning(
                    msg("Login is not allowed")));

            return sb.toString();
        }


        sb.append(header(msg("Please login")));
        String id = request.getString(ARG_USER_ID, "");
        //        sb.append(HtmlUtils.formPost(getRepository().getUrlPath(request,
        //                getRepositoryBase().URL_USER_LOGIN)));

        sb.append(HtmlUtils.formPost(getRepository().getUrlPath(request,
                getRepositoryBase().URL_USER_LOGIN)));

        if (request.defined(ARG_REDIRECT)) {
            String redirect = request.getUnsafeString(ARG_REDIRECT, "");
            //Um, a bit of a hack
            if (redirect.indexOf("logout") < 0) {
                sb.append(HtmlUtils.hidden(ARG_REDIRECT, redirect));
            }
        }
        sb.append(HtmlUtils.formTable());
        sb.append(
            formEntry(
                request, msgLabel("User"),
                HtmlUtils.input(
                    ARG_USER_ID, id,
                    HtmlUtils.cssClass(CSS_CLASS_USER_FIELD))));
        sb.append(formEntry(request, msgLabel("Password"),
                            HtmlUtils.password(ARG_USER_PASSWORD)));
        sb.append(extra);

        sb.append(formEntry(request, "", HtmlUtils.submit(msg("Login"))));
        sb.append(HtmlUtils.formClose());
        sb.append(HtmlUtils.formTableClose());

        if (getAdmin().isEmailCapable()) {
            sb.append(HtmlUtils.p());
            sb.append(
                HtmlUtils.href(
                    request.url(getRepositoryBase().URL_USER_FINDUSERID),
                    msg("Forget your user ID?")));
            sb.append(HtmlUtils.p());
            sb.append(
                HtmlUtils.href(
                    request.url(getRepositoryBase().URL_USER_RESETPASSWORD),
                    msg("Forget your password?")));
        }

        return sb.toString();
    }


    /**
     * _more_
     *
     * @return _more_
     *
     * @throws Exception On badness
     */
    public User getDefaultUser() throws Exception {
        return findUser(USER_DEFAULT);
    }

    /**
     * _more_
     *
     * @return _more_
     *
     * @throws Exception On badness
     */
    public User getAnonymousUser() throws Exception {
        return findUser(USER_ANONYMOUS);
    }

    /**
     * _more_
     *
     * @return _more_
     *
     * @throws Exception on badness
     */
    public User getLocalFileUser() throws Exception {
        return findUser(USER_LOCALFILE);
    }


    /**
     * _more_
     *
     * @param id _more_
     *
     * @return _more_
     *
     * @throws Exception On badness
     */
    public User findUser(String id) throws Exception {
        return findUser(id, false);
    }



    /**
     * _more_
     *
     * @param id _more_
     * @param userDefaultIfNotFound _more_
     *
     * @return _more_
     *
     * @throws Exception On badness
     */
    public User findUser(String id, boolean userDefaultIfNotFound)
            throws Exception {
        //        debugLogin("RAMADDA.findUser: " + id);
        if (id == null) {
            return null;
        }
        User user = userMap.get(id);
        if (user != null) {
            //System.err.println ("got from user map:" + id +" " + user);
            return user;
        }


        Statement statement =
            getDatabaseManager().select(Tables.USERS.COLUMNS,
                                        Tables.USERS.NAME,
                                        Clause.eq(Tables.USERS.COL_ID, id));
        ResultSet results = statement.getResultSet();
        if (results.next()) {
            user = getUser(results);
            debugLogin("RAMADDA.findUser: from database:" + user);
        } else {
            for (UserAuthenticator userAuthenticator : userAuthenticators) {
                debugLogin("RAMADDA.findUser: calling authenticator:"
                           + userAuthenticator);
                user = userAuthenticator.findUser(getRepository(), id);
                debugLogin("RAMADDA.findUser: from authenticator:" + user);
                if (user != null) {
                    user.setIsLocal(false);

                    break;
                }
            }
        }

        getDatabaseManager().closeAndReleaseConnection(statement);

        if (user == null) {
            if (userDefaultIfNotFound) {
                return getDefaultUser();
            }

            return null;
        }


        userMap.put(user.getId(), user);

        return user;
    }


    /**
     * _more_
     *
     * @param email _more_
     *
     * @return _more_
     *
     * @throws Exception On badness
     */
    public User findUserFromEmail(String email) throws Exception {
        Statement statement =
            getDatabaseManager().select(Tables.USERS.COLUMNS,
                                        Tables.USERS.NAME,
                                        Clause.eq(Tables.USERS.COL_EMAIL,
                                            email));
        ResultSet results = statement.getResultSet();
        if ( !results.next()) {
            return null;
        }

        return getUser(results);
    }




    /**
     * _more_
     *
     * @param user _more_
     *
     * @return _more_
     *
     * @throws Exception on badness
     */
    public boolean userExistsInDatabase(User user) throws Exception {
        return getDatabaseManager().tableContains(user.getId(),
                Tables.USERS.NAME, Tables.USERS.COL_ID);
    }





    /**
     * _more_
     *
     * @param user The user
     *
     *
     * @return _more_
     * @throws Exception On badness
     */
    public User makeUserIfNeeded(User user) throws Exception {
        if ( !userExistsInDatabase(user)) {
            makeOrUpdateUser(user, false);
        }

        return user;
    }


    /**
     * _more_
     *
     * @param user _more_
     *
     * @throws Exception on badness
     */
    public void changePassword(User user) throws Exception {
        getDatabaseManager().update(
            Tables.USERS.NAME, Tables.USERS.COL_ID, user.getId(),
            new String[] { Tables.USERS.COL_PASSWORD },
            new Object[] { user.getHashedPassword() });
    }


    /**
     * _more_
     *
     * @param user The user
     * @param updateIfNeeded _more_
     *
     * @throws Exception On badness
     */
    public void makeOrUpdateUser(User user, boolean updateIfNeeded)
            throws Exception {
        if ( !userExistsInDatabase(user)) {
            getDatabaseManager().executeInsert(Tables.USERS.INSERT,
                    new Object[] {
                user.getId(), user.getName(), user.getEmail(),
                user.getQuestion(), user.getAnswer(),
                user.getHashedPassword(), user.getDescription(),
                new Boolean(user.getAdmin()), user.getLanguage(),
                user.getTemplate(), new Boolean(user.getIsGuest()),
                user.getPropertiesBlob()
            });
            userMap.put(user.getId(), user);

            return;
        }

        if ( !updateIfNeeded) {
            throw new IllegalArgumentException(
                "Database already contains user:" + user.getId());
        }

        getDatabaseManager().update(Tables.USERS.NAME, Tables.USERS.COL_ID,
                                    user.getId(), new String[] {
            Tables.USERS.COL_NAME, Tables.USERS.COL_PASSWORD,
            Tables.USERS.COL_EMAIL, Tables.USERS.COL_QUESTION,
            Tables.USERS.COL_ANSWER, Tables.USERS.COL_ADMIN,
            Tables.USERS.COL_LANGUAGE, Tables.USERS.COL_TEMPLATE,
            Tables.USERS.COL_ISGUEST, Tables.USERS.COL_PROPERTIES
        }, new Object[] {
            user.getName(), user.getHashedPassword(), user.getEmail(),
            user.getQuestion(), user.getAnswer(), user.getAdmin()
                    ? new Integer(1)
                    : new Integer(0), user.getLanguage(), user.getTemplate(),
            new Boolean(user.getIsGuest()), user.getPropertiesBlob()
        });
        userMap.remove(user.getId());


    }




    /**
     * _more_
     *
     * @param user The user
     *
     * @throws Exception On badness
     */
    public void deleteUser(User user) throws Exception {
        userMap.remove(user.getId());
        deleteRoles(user);
        getDatabaseManager().delete(Tables.USERS.NAME,
                                    Clause.eq(Tables.USERS.COL_ID,
                                        user.getId()));
    }

    /**
     * _more_
     *
     * @param user The user
     *
     * @throws Exception On badness
     */
    public void deleteRoles(User user) throws Exception {
        getDatabaseManager().delete(Tables.USERROLES.NAME,
                                    Clause.eq(Tables.USERROLES.COL_USER_ID,
                                        user.getId()));
    }


    /**
     * This checks the PASSWORD1 and PASSWORD2 URL arguments for equality.
     * If they are defined and are equal then the hashed password is set for the user
     * and this returns true.
     *
     * If the passwords are not equal then false
     *
     * @param request the request
     * @param user The user
     *
     * @return Are the passwords equal and did the user's password get set
     */
    private boolean checkAndSetNewPassword(Request request, User user) {
        String password1 = request.getString(ARG_USER_PASSWORD1, "").trim();
        String password2 = request.getString(ARG_USER_PASSWORD2, "").trim();
        if (Utils.stringDefined(password1)
                || Utils.stringDefined(password2)) {
            if (password1.equals(password2)) {
                user.setPassword(hashPassword(password1));

                return true;
            }

            return false;
        }

        return true;
    }


    /**
     * set the user state from the request
     *
     * @param request the request
     * @param user The user
     * @param doAdmin _more_
     *
     * @throws Exception On badness
     */
    private void applyUserProperties(Request request, User user,
                                     boolean doAdmin)
            throws Exception {
        user.setName(request.getString(ARG_USER_NAME, user.getName()));
        user.setDescription(request.getString(ARG_USER_DESCRIPTION,
                user.getDescription()));
        user.setEmail(request.getString(ARG_USER_EMAIL, user.getEmail()));
        user.setTemplate(request.getString(ARG_TEMPLATE, user.getTemplate()));
        user.setLanguage(request.getString(ARG_USER_LANGUAGE,
                                           user.getLanguage()));
        user.setQuestion(request.getString(ARG_USER_QUESTION,
                                           user.getQuestion()));
        user.setAnswer(request.getString(ARG_USER_ANSWER, user.getAnswer()));
        if (doAdmin) {
            applyAdminState(request, user);
        }
        makeOrUpdateUser(user, true);
    }


    /**
     * _more_
     *
     * @param request the request
     * @param user _more_
     *
     * @throws Exception on badness
     */
    private void applyAdminState(Request request, User user)
            throws Exception {
        if ( !request.getUser().getAdmin()) {
            throw new IllegalArgumentException("Need to be admin");
        }
        if ( !request.defined(ARG_USER_ADMIN)) {
            user.setAdmin(false);
        } else {
            user.setAdmin(request.get(ARG_USER_ADMIN, user.getAdmin()));
        }
        user.setIsGuest(request.get(ARG_USER_ISGUEST, false));

        List<String> roles =
            StringUtil.split(request.getString(ARG_USER_ROLES, ""), "\n",
                             true, true);

        user.setRoles(roles);
        setRoles(request, user);
    }


    /**
     * _more_
     *
     * @param request the request
     * @param user The user
     *
     * @throws Exception On badness
     */
    private void setRoles(Request request, User user) throws Exception {
        deleteRoles(user);
        if (user.getRoles() == null) {
            return;
        }
        for (String role : user.getRoles()) {
            getDatabaseManager().executeInsert(Tables.USERROLES.INSERT,
                    new Object[] { user.getId(),
                                   role });
        }
    }

    /**
     * _more_
     *
     * @param request the request
     *
     * @return The result
     *
     * @throws Exception On badness
     */
    public Result adminUserEdit(Request request) throws Exception {
        String userId = request.getString(ARG_USER_ID, "");
        User   user   = findUser(userId);
        if (user == null) {
            throw new IllegalArgumentException(
                msgLabel("Could not find user") + userId);
        }

        if (request.defined(ARG_USER_DELETE_CONFIRM)) {
            request.ensureAuthToken();
            deleteUser(user);

            return new Result(request.url(getRepositoryBase().URL_USER_LIST));
        }


        StringBuffer sb = new StringBuffer();
        if (request.defined(ARG_USER_CHANGE)) {
            request.ensureAuthToken();
            if ( !checkAndSetNewPassword(request, user)) {
                sb.append(
                    getPageHandler().showDialogWarning(
                        "Incorrect passwords"));
            } else {
                if (request.defined(ARG_USER_PASSWORD1)) {
                    sb.append(
                        getPageHandler().showDialogNote(
                            msg("Password changed")));
                }
                applyUserProperties(request, user, true);
            }
        }


        sb.append(RepositoryUtil.header(msgLabel("User") + HtmlUtils.space(1)
                                        + user.getId()));
        sb.append(HtmlUtils.p());
        request.formPostWithAuthToken(sb, getRepositoryBase().URL_USER_EDIT);
        sb.append(HtmlUtils.hidden(ARG_USER_ID, user.getId()));
        if (request.defined(ARG_USER_DELETE)) {
            sb.append(
                getPageHandler().showDialogQuestion(
                    msg("Are you sure you want to delete the user?"),
                    HtmlUtils.buttons(
                        HtmlUtils.submit(
                            msg("Yes"),
                            ARG_USER_DELETE_CONFIRM), HtmlUtils.submit(
                                msg("Cancel"), ARG_USER_CANCEL))));
        } else {
            String buttons =
                HtmlUtils.submit(msg("Change User"), ARG_USER_CHANGE)
                + HtmlUtils.space(2)
                + HtmlUtils.submit(msg("Delete User"), ARG_USER_DELETE)
                + HtmlUtils.space(2)
                + HtmlUtils.submit(msg("Cancel"), ARG_CANCEL);
            sb.append(buttons);
            makeUserForm(request, user, sb, true);
            if (user.canChangePassword()) {
                sb.append(HtmlUtils.p());
                sb.append(RepositoryUtil.header(msgLabel("Password")));
                makePasswordForm(request, user, sb);
            }
            sb.append(HtmlUtils.p());
            sb.append(buttons);
        }
        sb.append(HtmlUtils.formClose());

        return getAdmin().makeResult(request,
                                     msgLabel("User") + user.getLabel(), sb);
    }


    /**
     * _more_
     *
     * @param request the request
     * @param user The user
     * @param sb _more_
     * @param includeAdmin _more_
     *
     * @throws Exception On badness
     */
    private void makeUserForm(Request request, User user, StringBuffer sb,
                              boolean includeAdmin)
            throws Exception {
        sb.append(HtmlUtils.formTable());
        if (user.canChangeNameAndEmail()) {
            sb.append(formEntry(request, msgLabel("Name"),
                                HtmlUtils.input(ARG_USER_NAME,
                                    user.getName(), HtmlUtils.SIZE_40)));
            sb.append(formEntry(request, msgLabel("Desription"),
                                HtmlUtils.textArea(ARG_USER_DESCRIPTION,
                                    user.getDescription(), 5, 20)));
        }
        if (includeAdmin) {
            if ( !request.getUser().getAdmin()) {
                throw new IllegalArgumentException("Need to be admin");
            }
            sb.append(formEntry(request, msgLabel("Admin"),
                                HtmlUtils.checkbox(ARG_USER_ADMIN, "true",
                                    user.getAdmin())));
            sb.append(formEntry(request, msgLabel("Guest"),
                                HtmlUtils.checkbox(ARG_USER_ISGUEST, "true",
                                    user.getIsGuest())));
            String       userRoles = user.getRolesAsString("\n");
            StringBuffer allRoles  = new StringBuffer();
            List         roles     = getRoles();
            allRoles.append(
                "<table border=0 cellspacing=0 cellpadding=0><tr valign=\"top\"><td><b>e.g.:</b></td><td>&nbsp;&nbsp;</td><td>");
            int cnt = 0;
            allRoles.append("</td><td>&nbsp;&nbsp;</td><td>");
            for (int i = 0; i < roles.size(); i++) {
                if (cnt++ > 4) {
                    allRoles.append("</td><td>&nbsp;&nbsp;</td><td>");
                    cnt = 0;
                }
                allRoles.append("<i>");
                allRoles.append(roles.get(i));
                allRoles.append("</i><br>");
            }
            allRoles.append("</table>\n");

            String roleEntry =
                HtmlUtils.hbox(HtmlUtils.textArea(ARG_USER_ROLES, userRoles,
                    5, 20), allRoles.toString());
            sb.append(formEntryTop(request, msgLabel("Roles"), roleEntry));
        }

        if (user.canChangeNameAndEmail()) {
            sb.append(formEntry(request, msgLabel("Email"),
                                HtmlUtils.input(ARG_USER_EMAIL,
                                    user.getEmail(), HtmlUtils.SIZE_40)));
        }

        List<TwoFacedObject> templates =
            getPageHandler().getTemplateSelectList();
        sb.append(formEntry(request, msgLabel("Page Style"),
                            HtmlUtils.select(ARG_TEMPLATE, templates,
                                             user.getTemplate())));

        List languages = new ArrayList(getPageHandler().getLanguages());
        languages.add(0, new TwoFacedObject("-default-", ""));
        sb.append(formEntry(request, msgLabel("Language"),
                            HtmlUtils.select(ARG_USER_LANGUAGE, languages,
                                             user.getLanguage())));
        sb.append(HtmlUtils.formTableClose());
    }



    /**
     * _more_
     *
     * @param request the request
     * @param user The user
     * @param sb _more_
     *
     * @throws Exception On badness
     */
    private void makePasswordForm(Request request, User user, StringBuffer sb)
            throws Exception {
        sb.append(HtmlUtils.formTable());
        sb.append(formEntry(request, msgLabel("Password"),
                            HtmlUtils.password(ARG_USER_PASSWORD1)));

        sb.append(formEntry(request, msgLabel("Password Again"),
                            HtmlUtils.password(ARG_USER_PASSWORD2)));

        sb.append(HtmlUtils.formTableClose());
    }


    /**
     * _more_
     *
     * @param request the request
     *
     * @return The result
     *
     * @throws Exception On badness
     */
    public Result adminUserNewForm(Request request) throws Exception {
        StringBuffer sb = new StringBuffer();
        makeNewUserForm(request, sb);

        return getAdmin().makeResult(request, msg("New User"), sb);
    }


    /**
     * _more_
     *
     * @param request the request
     *
     * @return The result
     *
     * @throws Exception on badness
     */
    public Result adminUserNewDo(Request request) throws Exception {

        request.ensureAuthToken();
        StringBuffer sb          = new StringBuffer();
        StringBuffer errorBuffer = new StringBuffer();
        List<User>   users       = new ArrayList<User>();
        boolean      ok          = true;
        for (String line :
                (List<String>) StringUtil.split(
                    request.getString(ARG_USER_BULK, ""), "\n", true, true)) {
            if (line.startsWith("#")) {
                continue;
            }
            List<String> toks = (List<String>) StringUtil.split(line, ",",
                                    true, true);
            if (toks.size() == 0) {
                continue;
            }
            if (toks.size() < 2) {
                ok = false;
                sb.append(getPageHandler().showDialogError("Bad line:"
                        + line));

                break;
            }
            String id        = toks.get(0);
            String password1 = toks.get(1);
            String name      = ((toks.size() >= 3)
                                ? toks.get(2)
                                : id);
            String email     = ((toks.size() >= 4)
                                ? toks.get(3)
                                : "");
            if (findUser(id) != null) {
                ok = false;
                sb.append(
                    getPageHandler().showDialogError(
                        getRepository().translate(
                            request, "User already exists") + " " + id));

                break;
            }
            User user = new User(id, name, email, "", "",
                                 hashPassword(password1), "", false, "", "",
                                 false, null);
            users.add(user);
        }


        if ( !ok) {
            makeNewUserForm(request, sb);

            return getAdmin().makeResult(request, msg("New User"), sb);
        }

        String  id        = "";
        String  name      = "";
        String  desc      = "";
        String  email     = "";
        String  password1 = "";
        String  password2 = "";
        boolean admin     = false;

        if (request.defined(ARG_USER_ID)) {
            id        = request.getString(ARG_USER_ID, "").trim();
            name      = request.getString(ARG_USER_NAME, name).trim();
            desc      = request.getString(ARG_USER_DESCRIPTION, name).trim();
            email     = request.getString(ARG_USER_EMAIL, "").trim();
            password1 = request.getString(ARG_USER_PASSWORD1, "").trim();
            password2 = request.getString(ARG_USER_PASSWORD2, "").trim();
            admin     = request.get(ARG_USER_ADMIN, false);

            boolean okToAdd = true;
            if (id.length() == 0) {
                okToAdd = false;
                errorBuffer.append(msg("Please enter an ID"));
                errorBuffer.append(HtmlUtils.br());
            }

            if ((password1.length() == 0) && (password2.length() == 0)) {
                password1 = password2 = getRepository().getGUID() + "."
                                        + Math.random();
            }


            if ((password1.length() == 0) || !password1.equals(password2)) {
                okToAdd = false;
                errorBuffer.append(msg("Invalid password"));
                errorBuffer.append(HtmlUtils.br());
            }

            if (findUser(id) != null) {
                okToAdd = false;
                errorBuffer.append(msg("User with given id already exists"));
                errorBuffer.append(HtmlUtils.br());
            }

            if (okToAdd) {
                User newUser = new User(id, name, email, "", "",
                                        hashPassword(password1), desc, admin,
                                        "", "", false, null);
                users.add(newUser);
            }
        }


        List<String> newUserRoles =
            StringUtil.split(request.getString(ARG_USER_ROLES, ""), "\n",
                             true, true);

        String homeGroupId = request.getString(ARG_USER_HOME + "_hidden", "");

        sb.append("<ul>");
        for (User newUser : users) {
            newUser.setRoles(newUserRoles);
            makeOrUpdateUser(newUser, false);
            setRoles(request, newUser);

            sb.append("<li> ");
            sb.append(msgLabel("Created user"));
            sb.append(HtmlUtils.space(1));
            sb.append(
                HtmlUtils.href(
                    request.url(
                        getRepositoryBase().URL_USER_EDIT, ARG_USER_ID,
                        newUser.getId()), newUser.getId()));


            StringBuffer msg =
                new StringBuffer(request.getString(ARG_USER_MESSAGE, ""));
            msg.append("<p>User id: " + newUser.getId() + "<p>");
            msg.append(
                "Click on this link to send a password reset link to your registered email address:<br>");
            String resetUrl =
                HtmlUtils.url(
                    getRepositoryBase().URL_USER_RESETPASSWORD.toString(),
                    ARG_USER_NAME, newUser);

            if ( !resetUrl.startsWith("http")) {
                resetUrl = request.getAbsoluteUrl(resetUrl);
            }
            msg.append(HtmlUtils.href(resetUrl,
                                      "Send Password Reset Message"));
            msg.append("<p>");

            if (homeGroupId.length() > 0) {
                Entry parent = getEntryManager().findGroup(request,
                                   homeGroupId);
                Entry home = getEntryManager().makeNewGroup(parent,
                                 newUser.getName(), newUser, null,
                                 TypeHandler.TYPE_HOMEPAGE);
                msg.append("A home folder has been created for you: ");
                String homeUrl =
                    HtmlUtils.url(
                        getRepositoryBase().URL_ENTRY_SHOW.toString(),
                        ARG_ENTRYID, home.getId());
                msg.append(HtmlUtils.href(request.getAbsoluteUrl(homeUrl),
                                          home.getFullName()));
                addFavorites(request, newUser,
                             (List<Entry>) Misc.newList(home));
            }

            if ((newUser.getEmail().length() > 0)
                    && request.get(ARG_USER_SENDMAIL, false)
                    && getAdmin().isEmailCapable()) {
                getRepository().getMailManager().sendEmail(
                    newUser.getEmail(), "RAMADDA User Account",
                    msg.toString(), true);

                sb.append(" sent mail to:" + newUser.getEmail());
            }
        }

        sb.append("</ul>");

        if (users.size() > 0) {
            return addHeader(request, new Result("", sb));
        }

        if (errorBuffer.toString().length() > 0) {
            sb.append(
                getPageHandler().showDialogWarning(errorBuffer.toString()));
        }
        makeNewUserForm(request, sb);

        return getAdmin().makeResult(request, msg("New User"), sb);


    }

    /**
     * _more_
     *
     * @param request the request
     * @param sb _more_
     *
     * @throws Exception on badness
     */
    private void makeNewUserForm(Request request, StringBuffer sb)
            throws Exception {

        request.formPostWithAuthToken(sb, URL_USER_NEW_DO);
        StringBuffer formSB = new StringBuffer();
        String       id     = request.getString(ARG_USER_ID, "").trim();
        String       name   = request.getString(ARG_USER_NAME, "").trim();
        String       desc = request.getString(ARG_USER_DESCRIPTION,
                                "").trim();
        String       email  = request.getString(ARG_USER_EMAIL, "").trim();
        boolean      admin  = request.get(ARG_USER_ADMIN, false);

        formSB.append(msgHeader("Create a single user"));
        formSB.append(HtmlUtils.formTable());
        formSB.append(formEntry(request, msgLabel("ID"),
                                HtmlUtils.input(ARG_USER_ID, id,
                                    HtmlUtils.SIZE_40)));
        formSB.append(formEntry(request, msgLabel("Name"),
                                HtmlUtils.input(ARG_USER_NAME, name,
                                    HtmlUtils.SIZE_40)));

        formSB.append(formEntry(request, msgLabel("Desription"),
                                HtmlUtils.textArea(ARG_USER_DESCRIPTION,
                                    desc, 5, 20)));

        formSB.append(formEntry(request, msgLabel("Admin"),
                                HtmlUtils.checkbox(ARG_USER_ADMIN, "true",
                                    admin)));

        formSB.append(formEntry(request, msgLabel("Email"),
                                HtmlUtils.input(ARG_USER_EMAIL, email,
                                    HtmlUtils.SIZE_40)));

        formSB.append(formEntry(request, msgLabel("Password"),
                                HtmlUtils.password(ARG_USER_PASSWORD1)));

        formSB.append(formEntry(request, msgLabel("Password Again"),
                                HtmlUtils.password(ARG_USER_PASSWORD2)));

        formSB.append(
            HtmlUtils.formEntryTop(
                msgLabel("Roles"),
                HtmlUtils.textArea(
                    ARG_USER_ROLES, request.getString(ARG_USER_ROLES, ""), 3,
                    25)));



        String select =
            getRepository().getHtmlOutputHandler().getSelect(request,
                ARG_USER_HOME, HtmlUtils.space(1) + msg("Select"), false, "");
        formSB.append(HtmlUtils.hidden(ARG_USER_HOME + "_hidden", "",
                                       HtmlUtils.id(ARG_USER_HOME
                                           + "_hidden")));

        String groupMsg =
            "Create a folder using the user's name under this folder";
        formSB.append(
            HtmlUtils.formEntry(
                msgLabel("Home folder"),
                groupMsg + "<br>"
                + HtmlUtils.disabledInput(
                    ARG_USER_HOME, "",
                    HtmlUtils.SIZE_40
                    + HtmlUtils.id(ARG_USER_HOME)) + HtmlUtils.space(1)
                        + select));







        StringBuffer msgSB = new StringBuffer();
        String       msg   =
            "A new RAMADDA account has been created for you.";
        msgSB.append(HtmlUtils.checkbox(ARG_USER_SENDMAIL, "true", false));
        msgSB.append(HtmlUtils.space(1));
        msgSB.append(msgLabel("Send an email to the new user with message"));
        msgSB.append(HtmlUtils.br());
        msgSB.append(HtmlUtils.textArea(ARG_USER_MESSAGE, msg, 5, 50));

        if (getAdmin().isEmailCapable()) {
            formSB.append(HtmlUtils.formEntryTop(msgLabel("Notification"),
                    msgSB.toString()));
        }

        formSB.append(HtmlUtils.formTableClose());
        StringBuffer bulkSB = new StringBuffer();
        bulkSB.append(msgHeader("Or create a number of users"));
        bulkSB.append(
            "one user per line<br><i>user id, password, name, email</i><br>");
        bulkSB.append(HtmlUtils.textArea(ARG_USER_BULK,
                                         request.getString(ARG_USER_BULK,
                                             ""), 10, 80));



        StringBuffer top = new StringBuffer();
        top.append(
            HtmlUtils.table(
                HtmlUtils.rowTop(
                    HtmlUtils.cols(formSB.toString(), bulkSB.toString()))));


        sb.append(HtmlUtils.p());
        sb.append(top);
        sb.append(HtmlUtils.p());
        sb.append(HtmlUtils.submit(msg("Create User"), ARG_USER_NEW));
        sb.append(HtmlUtils.formClose());




    }


    /**
     * _more_
     *
     * @param request the request
     * @param sb _more_
     * @param init _more_
     */
    private void makeBulkForm(Request request, StringBuffer sb, String init) {
        if (init == null) {
            init = "#one per line\n#user id, password, name, email";
        }
        sb.append(msgHeader("Bulk User Create"));
        request.formPostWithAuthToken(sb, URL_USER_NEW_DO);
        sb.append(HtmlUtils.textArea(ARG_USER_BULK, init, 10, 80));
        sb.append(HtmlUtils.br());
        sb.append(HtmlUtils.submit("Submit"));
        sb.append("\n</form>\n");

    }







    /**
     * _more_
     *
     * @param request the request
     *
     * @return The result
     *
     * @throws Exception On badness
     */
    public Result adminUserList(Request request) throws Exception {

        if (request.exists(ARG_REMOVESESSIONID)) {
            getSessionManager().debugSession(
                "RAMADDA.adminUserList: removing session:"
                + request.getString(ARG_REMOVESESSIONID));
            getSessionManager().removeSession(
                request.getString(ARG_REMOVESESSIONID));

            return new Result(request.url(getRepositoryBase().URL_USER_LIST,
                                          ARG_SHOWTAB, "2"));
        }


        Hashtable<String, StringBuffer> rolesMap = new Hashtable<String,
                                                       StringBuffer>();
        List<String> rolesList = new ArrayList<String>();
        StringBuffer usersHtml = new StringBuffer();
        StringBuffer rolesHtml = new StringBuffer();

        usersHtml.append(request.form(URL_USER_NEW_FORM));
        usersHtml.append(HtmlUtils.submit(msg("New User")));
        usersHtml.append(HtmlUtils.formClose());

        Statement statement =
            getDatabaseManager().select(Tables.USERS.COLUMNS,
                                        Tables.USERS.NAME, new Clause(),
                                        " order by " + Tables.USERS.COL_ID);

        SqlUtil.Iterator iter  = getDatabaseManager().getIterator(statement);


        List<User>       users = new ArrayList();
        ResultSet        results;
        while ((results = iter.getNext()) != null) {
            users.add(getUser(results));
        }


        usersHtml.append("<table>");
        usersHtml.append(HtmlUtils.row(HtmlUtils.cols("",
                HtmlUtils.bold(msg("ID")) + HtmlUtils.space(2),
                HtmlUtils.bold(msg("Name")) + HtmlUtils.space(2),
        //                    HtmlUtils.bold(msg("Roles")) + HtmlUtils.space(2),
        HtmlUtils.bold(msg("Email")) + HtmlUtils.space(2), HtmlUtils.bold(
            msg("Admin")) + HtmlUtils.space(2), HtmlUtils.bold(
            msg("Guest")))));

        for (User user : users) {
            String userEditLink = HtmlUtils.href(
                                      request.url(
                                          getRepositoryBase().URL_USER_EDIT,
                                          ARG_USER_ID,
                                          user.getId()), HtmlUtils.img(
                                              iconUrl(ICON_EDIT),
                                              msg("Edit user")));

            String userProfileLink =
                HtmlUtils.href(
                    HtmlUtils.url(
                        request.url(getRepository().URL_USER_PROFILE),
                        ARG_USER_ID, user.getId()), user.getId(),
                            "title=\"View user profile\"");

            String userLogLink =
                HtmlUtils.href(
                    request.url(
                        getRepositoryBase().URL_USER_ACTIVITY, ARG_USER_ID,
                        user.getId()), HtmlUtils.img(
                            getRepository().iconUrl(ICON_LOG),
                            msg("View user log")));


            String row = (user.getAdmin()
                          ? "<tr valign=\"top\" style=\"background-color:#cccccc;\">"
                          : "<tr valign=\"top\" >") + HtmlUtils.cols(
                              userLogLink + userEditLink, userProfileLink,
                              user.getName(),
            /*user.getRolesAsString("<br>"),*/
            user.getEmail(), "" + user.getAdmin(),
                             "" + user.getIsGuest()) + "</tr>";
            usersHtml.append(row);

            List<String> roles = user.getRoles();
            if (roles != null) {
                for (String role : roles) {
                    StringBuffer rolesSB = rolesMap.get(role);
                    if (rolesSB == null) {
                        rolesSB = new StringBuffer("");
                        rolesList.add(role);
                        rolesMap.put(role, rolesSB);
                    }
                    rolesSB.append(HtmlUtils.row(HtmlUtils.cols("<li>",
                            userEditLink, user.getId(), user.getName(),
                            user.getEmail())));
                }
            }
        }
        usersHtml.append("</table>");

        for (String role : rolesList) {
            StringBuffer rolesSB = rolesMap.get(role);
            rolesHtml.append(HtmlUtils.makeShowHideBlock(role,
                    "<table>" + rolesSB.toString() + "</table>", false));
        }
        if (rolesList.size() == 0) {
            rolesHtml.append(msg("No roles"));
        }


        StringBuffer sb         = new StringBuffer();
        List         tabTitles  = new ArrayList();
        List         tabContent = new ArrayList();

        int          showTab    = request.get(ARG_SHOWTAB, 0);
        tabTitles.add(msg("User List"));
        tabContent.add(usersHtml.toString());

        tabTitles.add(msg("Roles"));
        tabContent.add(rolesHtml.toString());


        tabTitles.add(msg("Current Sessions"));
        tabContent.add(
            getSessionManager().getSessionList(request).toString());


        tabTitles.add(msg("Recent User Activity"));
        tabContent.add(getUserActivities(request, null));


        tabTitles.set(showTab, tabTitles.get(showTab));
        sb.append(HtmlUtils.p());
        sb.append(OutputHandler.makeTabs(tabTitles, tabContent, true));

        return getAdmin().makeResult(request, msg("Users"), sb);
    }


    /**
     * _more_
     *
     * @param results _more_
     *
     * @return _more_
     *
     * @throws Exception On badness
     */
    public User getUser(ResultSet results) throws Exception {
        int col = 1;
        //id, name, email, question, answer, hashedPassword, description
        //admin, language, template, isGuest, propertiesBlob

        User user = new User(results.getString(col++),
                             results.getString(col++),
                             results.getString(col++),
                             results.getString(col++),
                             results.getString(col++),
                             results.getString(col++),
                             results.getString(col++),
                             results.getBoolean(col++),
                             results.getString(col++),
                             results.getString(col++),
                             results.getBoolean(col++),
                             results.getString(col++));

        Statement statement = getDatabaseManager().select(
                                  Tables.USERROLES.COL_ROLE,
                                  Tables.USERROLES.NAME,
                                  Clause.eq(
                                      Tables.USERROLES.COL_USER_ID,
                                      user.getId()));

        String[] array =
            SqlUtil.readString(getDatabaseManager().getIterator(statement),
                               1);
        List<String> roles = new ArrayList<String>(Misc.toList(array));
        user.setRoles(roles);

        return user;
    }



    /**
     * _more_
     *
     * @param request the request
     *
     * @return _more_
     */
    public List<Entry> getCart(Request request) {
        String sessionId = request.getSessionId();

        if (sessionId == null) {
            return new ArrayList<Entry>();
        }
        List<Entry> cart = userCart.get(sessionId);
        if (cart == null) {
            cart = new ArrayList<Entry>();
            userCart.put(sessionId, cart);
        }

        return cart;
    }

    /**
     * _more_
     *
     * @param request the request
     * @param entries _more_
     *
     * @throws Exception On badness
     */
    private void addToCart(Request request, List<Entry> entries)
            throws Exception {
        List<Entry> cart = getCart(request);
        for (Entry entry : entries) {
            if ( !cart.contains(entry)) {
                cart.add(entry);
            }
        }
    }


    /**
     * _more_
     *
     * @param request the request
     * @param entries _more_
     *
     * @throws Exception on badness
     */
    private void removeFromCart(Request request, List<Entry> entries)
            throws Exception {
        List<Entry> cart = getCart(request);
        for (Entry entry : entries) {
            cart.remove(entry);
        }
    }





    /**
     * _more_
     *
     * @param request the request
     *
     * @return The result
     *
     * @throws Exception On badness
     */
    public Result processCart(Request request) throws Exception {
        String       action = request.getString(ARG_ACTION, "");
        StringBuffer sb     = new StringBuffer();
        if (action.equals(ACTION_CLEAR)) {
            getCart(request).clear();
        } else if (action.equals(ACTION_ADD)) {
            Entry entry = getEntryManager().getEntry(request,
                              request.getId(""));
            if (entry == null) {
                throw new IllegalArgumentException(
                    msgLabel("Could not find entry with id")
                    + request.getId(""));
            }
            if ( !getCart(request).contains(entry)) {
                getCart(request).add(entry);
            }
        }

        return showCart(request);
    }



    /**
     * _more_
     *
     * @param request the request
     *
     * @return The result
     *
     * @throws Exception On badness
     */
    public Result showCart(Request request) throws Exception {

        StringBuffer sb = new StringBuffer();
        request.appendMessage(sb);
        List<Entry> entries = getCart(request);
        if (entries.size() == 0) {
            entries = new ArrayList<Entry>();
            sb.append(
                getPageHandler().showDialogNote(msg("No entries in cart")));
            //            entries.add(getEntryManager().getTopGroup());
        }

        sb.append(HtmlUtils.p());
        if (entries.size() == 0) {
            return makeResult(request, "User Cart", sb);
        }

        boolean splitScreen = request.getString(ARG_ACTION,
                                  "").equals(ACTION_SPLIT);
        boolean      haveFrom = request.defined(ARG_FROM);
        StringBuffer header   = new StringBuffer();

        if ( !haveFrom) {
            if (splitScreen) {
                header.append(
                    HtmlUtils.href(
                        request.url(getRepositoryBase().URL_USER_CART),
                        msg("Unsplit Screen")));
            } else {
                header.append(
                    HtmlUtils.href(
                        request.url(
                            getRepositoryBase().URL_USER_CART, ARG_ACTION,
                            ACTION_SPLIT), msg("Split Screen")));
            }
            header.append(
                HtmlUtils.span(
                    "&nbsp;|&nbsp;",
                    HtmlUtils.cssClass(CSS_CLASS_SEPARATOR)));
        }

        header.append(
            HtmlUtils.href(
                request.url(
                    getRepositoryBase().URL_USER_CART, ARG_ACTION,
                    ACTION_CLEAR), msg("Clear Cart")));

        sb.append(HtmlUtils.center(header.toString()));

        sb.append(HtmlUtils.p());
        if (haveFrom) {
            Entry fromEntry = getEntryManager().getEntry(request,
                                  request.getString(ARG_FROM, ""));
            sb.append(HtmlUtils.br());
            sb.append(msgLabel("Pick an entry to associate with")
                      + HtmlUtils.space(1) + fromEntry.getName());
        }



        if ( !haveFrom && !splitScreen) {
            String[] formTuple =
                getRepository().getHtmlOutputHandler().getEntryFormStart(
                    request, entries, false, "Cart");

            sb.append(formTuple[2]);
        }
        OutputHandler outputHandler =
            getRepository().getOutputHandler(request);

        int cnt = 1;
        if (splitScreen) {
            cnt = 2;
        }
        entries = getEntryUtil().doGroupAndNameSort(entries, false);
        List<StringBuffer> columns = new ArrayList<StringBuffer>();
        StringBuffer       jsSB    = null;
        for (int column = 0; column < cnt; column++) {
            StringBuffer colSB = new StringBuffer();
            columns.add(colSB);
            for (Entry entry : entries) {
                if (haveFrom) {
                    colSB.append(
                        HtmlUtils.img(
                            getPageHandler().getIconUrl(request, entry)));
                    colSB.append(
                        HtmlUtils.href(
                            request.url(
                                getRepository().URL_ASSOCIATION_ADD,
                                ARG_FROM, request.getString(ARG_FROM, ""),
                                ARG_TO, entry.getId()), HtmlUtils.img(
                                    getRepository().iconUrl(
                                        ICON_ASSOCIATION), msg(
                                        "Create an association")) + HtmlUtils.space(
                                            1) + entry.getLabel()));
                } else if (splitScreen) {
                    request.put(ARG_SHOWLINK, "false");
                    colSB.append(getEntryManager().getAjaxLink(request,
                            entry, entry.getLabel(), null));

                    request.remove(ARG_SHOWLINK);
                } else {
                    String cbxId = "checkbox_" + HtmlUtils.blockCnt++;
                    String links =
                        HtmlUtils.checkbox(
                            "entry_" + entry.getId(), "true", false,
                            HtmlUtils.attrs(
                                HtmlUtils.ATTR_ID, cbxId,
                                HtmlUtils.ATTR_ONCLICK,
                                HtmlUtils.call(
                                    "checkboxClicked",
                                    HtmlUtils.comma(
                                        "event", HtmlUtils.squote("entry_"),
                                        HtmlUtils.squote(cbxId)))));


                    colSB.append(HtmlUtils.hidden("all_" + entry.getId(),
                            "1"));
                    colSB.append(links);
                    colSB.append(
                        HtmlUtils.img(
                            getPageHandler().getIconUrl(request, entry)));
                    colSB.append(
                        HtmlUtils.href(
                            request.url(
                                getRepositoryBase().URL_USER_CART, ARG_FROM,
                                entry.getId()), HtmlUtils.img(
                                    getRepository().iconUrl(
                                        ICON_ASSOCIATION), msg(
                                        "Create an association"))));

                }
                if ( !splitScreen) {
                    colSB.append(HtmlUtils.space(1));
                    if (haveFrom) {}
                    else {
                        colSB.append(getPageHandler().getBreadCrumbs(request,
                                entry));
                    }
                    colSB.append(HtmlUtils.br());
                }
            }
        }
        sb.append(HtmlUtils.open(HtmlUtils.TAG_TABLE,
                                 HtmlUtils.attr(HtmlUtils.ATTR_WIDTH,
                                     "100%")));

        sb.append(HtmlUtils.open(HtmlUtils.TAG_TR,
                                 HtmlUtils.attr(HtmlUtils.ATTR_VALIGN,
                                     "top")));
        int colCnt = 0;
        for (StringBuffer colSB : columns) {
            colCnt++;
            String extra = "";
            if (colCnt == 1) {
                extra = HtmlUtils.style("border-right : 1px black solid;");
            }
            sb.append(HtmlUtils.open(HtmlUtils.TAG_TD,
                                     HtmlUtils.attr(HtmlUtils.ATTR_WIDTH,
                                         "50%") + extra));
            if (splitScreen) {
                sb.append(
                    HtmlUtils.open(
                        HtmlUtils.TAG_DIV,
                        HtmlUtils.style(
                            "max-height: 600px; overflow-y: auto;")));
            }
            sb.append(colSB);
            if (splitScreen) {
                sb.append(HtmlUtils.close(HtmlUtils.TAG_DIV));
            }
            sb.append(HtmlUtils.close(HtmlUtils.TAG_TD));
        }
        sb.append(HtmlUtils.close(HtmlUtils.TAG_TR));
        sb.append(HtmlUtils.close(HtmlUtils.TAG_TABLE));

        if ( !haveFrom && !splitScreen) {
            sb.append("</form>");
        }


        return makeResult(request, "User Cart", sb);

    }

    /**
     * _more_
     *
     * @param request the request
     * @param title _more_
     * @param sb _more_
     *
     * @return The result
     */
    public Result makeResult(Request request, String title, StringBuffer sb) {
        StringBuffer headerSB = new StringBuffer();
        addUserHeader(request, headerSB);
        headerSB.append(sb);
        sb = headerSB;
        /*
        List<RequestUrl> links  =  !request.getUser().canEditSettings()
            ? anonUserUrls
            : userUrls;
        if(true) {
            return getRepository().makeResult(request, title, sb, links);
        }

        User user = request.getUser();
        List list = getRepository().getSubNavLinks(request, links);
        sb.append(StringUtil.join("",list));
        */
        //      Result result = getRepository().makeResult(request, title, sb,
        Result result = new Result(title, sb);

        //        if(true) return result;
        return addHeader(request, result);
    }


    /**
     * _more_
     *
     * @param request the request
     * @param sb _more_
     */
    public void addUserHeader(Request request, StringBuffer sb) {
        User             user    = request.getUser();
        boolean          useAnon = user.getAnonymous() || user.getIsGuest();
        List<RequestUrl> links   = userUrls;
        if (user.getAnonymous() || user.getIsGuest()) {
            links = anonUserUrls;
        } else if ( !user.getIsLocal()) {
            links = remoteUserUrls;
        }
        sb.append(getPageHandler().makeHeader(request, links, ""));
    }


    /**
     * _more_
     *
     * @return _more_
     */
    public boolean isCartEnabled() {
        return getProperty(PROP_SHOW_CART, true);
    }


    /**
     * Get the login/settings/help links
     *
     * @param request the request
     * @param template template to make the links
     * @param separator separator between links
     *
     * @return user links
     */
    public String getUserLinks(Request request, String template,
                               String separator) {
        User user   = request.getUser();

        List extras = new ArrayList();
        List urls   = new ArrayList();
        List labels = new ArrayList();
        List tips   = new ArrayList();

        if (user.getAnonymous()) {
            if (canDoLogin(request)) {
                request.remove(ARG_MESSAGE);
                request.remove(ARG_REDIRECT);
                String redirect =
                    RepositoryUtil.encodeBase64(request.getUrl().getBytes());
                extras.add("");
                urls.add(request.url(getRepositoryBase().URL_USER_LOGIN,
                                     ARG_REDIRECT, redirect));
                labels.add(msg("Login"));
                tips.add(msg("Login"));
            }

            if (isCartEnabled()) {
                extras.add("");
                urls.add(request.url(getRepositoryBase().URL_USER_CART));
                //        labels.add(HtmlUtils.img(getRepository().iconUrl(ICON_CART),
                //                                msg("Data Cart")));
                labels.add(msg("Data Cart"));
                tips.add(msg("View data cart"));
            }
        } else {
            extras.add("");
            urls.add(request.url(getRepositoryBase().URL_USER_LOGOUT));
            labels.add(msg("Logout"));
            tips.add(msg("Logout"));

            extras.add("");
            urls.add(request.url(getRepositoryBase().URL_USER_HOME));
            String label = user.getLabel().replace(" ", "&nbsp;");
            labels.add(label);
            tips.add(msg("Go to user settings"));
        }

        if (getProperty(PROP_SHOW_HELP, true)
                && (getRepository().getPluginManager().getDocUrls().size()
                    > 0)) {
            urls.add(request.url(getRepositoryBase().URL_HELP));
            extras.add("");
            labels.add(msg("Help"));
            tips.add(msg("View Help"));
        }


        List links = new ArrayList();
        for (int i = 0; i < urls.size(); i++) {
            String link = template.replace("${label}",
                                           labels.get(i).toString());
            link = link.replace("${url}", urls.get(i).toString());
            link = link.replace("${tooltip}", tips.get(i).toString());
            link = link.replace("${extra}", extras.get(i).toString());
            links.add(link);
        }


        return StringUtil.join(separator, links);
    }




    /**
     * _more_
     *
     * @param request the request
     *
     * @return The result
     *
     * @throws Exception On badness
     */
    public Result processFavorite(Request request) throws Exception {
        String message = "";
        User   user    = request.getUser();

        if ( !request.getUser().canEditFavorites()) {
            return addHeader(
                request,
                new Result(
                    msg("Favorites"),
                    new StringBuffer(
                        getPageHandler().showDialogError(
                            "Favorites not allowed"))));
        }
        String entryId = request.getString(ARG_ENTRYID, BLANK);

        if (request.get(ARG_FAVORITE_ADD, false)) {
            Entry entry = getEntryManager().getEntry(request, entryId);
            if (entry == null) {
                return addHeader(
                    request,
                    new Result(
                        msg("Favorites"),
                        new StringBuffer(
                            getPageHandler().showDialogError(
                                getRepository().translate(
                                    request,
                                    "Cannot find or access entry")))));
            }

            addFavorites(request, user, (List<Entry>) Misc.newList(entry));
            message = "Favorite added";
        } else if (request.get(ARG_FAVORITE_DELETE, false)) {
            getDatabaseManager().delete(
                Tables.FAVORITES.NAME,
                Clause.and(
                    Clause.eq(
                        Tables.FAVORITES.COL_ID,
                        request.getString(ARG_FAVORITE_ID, "")), Clause.eq(
                            Tables.FAVORITES.COL_USER_ID, user.getId())));
            message = "Favorite deleted";
            user.setFavorites(null);
        } else {
            message = "Unknown favorite command";
        }

        String redirect = getRepositoryBase().URL_USER_HOME.toString();

        return new Result(HtmlUtils.url(redirect, ARG_MESSAGE,
                                        getRepository().translate(request,
                                            message)));

    }


    /**
     * _more_
     *
     * @param request the request
     * @param user The user
     * @param entries _more_
     *
     * @throws Exception On badness
     */
    private void addFavorites(Request request, User user, List<Entry> entries)
            throws Exception {
        List<Entry> favorites =
            FavoriteEntry.getEntries(getFavorites(request, user));
        if (user.getAnonymous()) {
            throw new IllegalArgumentException(
                "Need to be logged in to add favorites");
        }
        if ( !request.getUser().canEditFavorites()) {
            throw new IllegalArgumentException("Cannot add favorites");
        }

        for (Entry entry : entries) {
            if (favorites.contains(entry)) {
                continue;
            }
            //COL_ID,COL_USER_ID,COL_ENTRY_ID,COL_NAME
            String name     = "";
            String category = "";
            getDatabaseManager().executeInsert(Tables.FAVORITES.INSERT,
                    new Object[] { getRepository().getGUID(),
                                   user.getId(), entry.getId(), name,
                                   category });
        }
        user.setFavorites(null);
    }


    /**
     * _more_
     *
     * @param request the request
     *
     * @return The result
     *
     * @throws Exception On badness
     */
    public Result processHome(Request request) throws Exception {
        boolean responseAsXml = request.getString(ARG_RESPONSE,
                                    "").equals(RESPONSE_XML);
        StringBuffer sb   = new StringBuffer();
        User         user = request.getUser();
        if (user.getAnonymous()) {
            if (responseAsXml) {
                return new Result(XmlUtil.tag(TAG_RESPONSE,
                        XmlUtil.attr(ATTR_CODE, CODE_ERROR),
                        "No user defined"), MIME_XML);
            }
            String msg = msg("You are not logged in");
            if (request.exists(ARG_FROMLOGIN)) {
                msg = msg + HtmlUtils.p()
                      + msg("If you had logged in perhaps you have cookies turned off?");
            }
            sb.append(getPageHandler().showDialogWarning(msg));
            sb.append(makeLoginForm(request));

            return addHeader(request, new Result(msg("User Home"), sb));
            //        } else  if (user.getIsGuest()) {
        } else {
            request.appendMessage(sb);
        }

        if (responseAsXml) {
            return new Result(XmlUtil.tag(TAG_RESPONSE,
                                          XmlUtil.attr(ATTR_CODE, "ok"),
                                          user.getId()), MIME_XML);
        }


        sb.append(HtmlUtils.p());
        int cnt = 0;
        for (FavoriteEntry favorite : getFavorites(request, user)) {
            if (cnt == 0) {
                sb.append(msgHeader("Favorites"));
            }
            cnt++;
            //TODO: Use the categories
            String removeLink =
                HtmlUtils.href(
                    request.url(
                        getRepositoryBase().URL_USER_FAVORITE,
                        ARG_FAVORITE_ID, favorite.getId(),
                        ARG_FAVORITE_DELETE, "true"), HtmlUtils.img(
                            getRepository().iconUrl(ICON_DELETE),
                            msg("Delete this favorite")));
            sb.append(removeLink);
            sb.append(HtmlUtils.space(1));
            sb.append(getPageHandler().getBreadCrumbs(request,
                    favorite.getEntry()));
            sb.append(HtmlUtils.br());
        }


        if (request.getUser().canEditSettings() && (cnt == 0)) {
            sb.append(
                getPageHandler().showDialogNote(
                    "You have no favorite entries defined.<br>When you see an  entry or folder just click on the "
                    + HtmlUtils.img(iconUrl(ICON_FAVORITE))
                    + " icon to add it to your list of favorites"));
        }

        //        xxxx
        return makeResult(request, "User Home", sb);
    }



    /**
     * _more_
     *
     * @param request the request
     *
     * @return The result
     *
     * @throws Exception On badness
     */
    public Result processProfile(Request request) throws Exception {
        StringBuffer sb   = new StringBuffer();
        User         user = findUser(request.getString(ARG_USER_ID, ""));
        if (user == null) {
            sb.append(msgLabel("Unknown user"));
            sb.append(request.getString(ARG_USER_ID, ""));

            return new Result(msg("User Profile"), sb);
        }

        sb.append(msgHeader("User Profile"));
        String searchLink =
            HtmlUtils
                .href(HtmlUtils
                    .url(request
                        .url(getRepository().getSearchManager()
                            .URL_ENTRY_SEARCH), ARG_USER_ID,
                                user.getId()), HtmlUtils
                                    .img(getRepository()
                                        .iconUrl(ICON_SEARCH), msg(
                                            "Search for entries created by this user")));

        sb.append(HtmlUtils.formTable());
        sb.append(formEntry(request, msgLabel("ID"),
                            user.getId() + HtmlUtils.space(2) + searchLink));
        sb.append(formEntry(request, msgLabel("Name"), user.getLabel()));
        String email = user.getEmail();
        if (email.length() > 0) {
            email = email.replace("@", " _AT_ ");
            sb.append(formEntry(request, msgLabel("Email"), email));
        }
        sb.append(HtmlUtils.formTableClose());

        return new Result(msg("User Profile"), sb);
    }




    /**
     * Class PasswordReset _more_
     *
     *
     * @author RAMADDA Development Team
     * @version $Revision: 1.3 $
     */
    private static class PasswordReset {

        /** _more_ */
        String user;

        /** _more_ */
        Date dttm;

        /**
         * _more_
         *
         * @param user The user
         * @param dttm _more_
         */
        public PasswordReset(String user, Date dttm) {
            this.user = user;
            this.dttm = dttm;
        }
    }

    /**
     * _more_
     *
     * @param request the request
     *
     * @return The result
     *
     * @throws Exception On badness
     */
    public Result processFindUserId(Request request) throws Exception {
        StringBuffer sb = new StringBuffer();
        if ( !getAdmin().isEmailCapable()) {
            return makeResult(
                request, "User Information",
                new StringBuffer(
                    getPageHandler().showDialogWarning(
                        msg(
                        "This RAMADDA server has not been configured to send email"))));
        }

        String email = request.getString(ARG_USER_EMAIL, "").trim();
        if (email.length() > 0) {
            User user = findUserFromEmail(email);
            if (user != null) {
                String userIdMailTemplate =
                    getProperty(PROP_USER_RESET_ID_TEMPLATE, "${userid}");
                String contents = userIdMailTemplate.replace("${userid}",
                                      user.getId());
                contents = contents.replace(
                    "${url}",
                    request.getAbsoluteUrl(getRepository().URL_USER_LOGIN));
                String subject = getProperty(PROP_USER_RESET_ID_SUBJECT,
                                             "Your RAMADDA ID");
                getRepository().getMailManager().sendEmail(user.getEmail(),
                        subject, contents.toString(), true);
                String message =
                    "You user id has been sent to your registered email address";

                return new Result(
                    request.url(
                        getRepositoryBase().URL_USER_LOGIN, ARG_MESSAGE,
                        getRepository().translate(request, message)));
            }
            sb.append(
                getPageHandler().showDialogError(
                    getRepository().translate(
                        request,
                        "No user is registered with the given email address")));
        }

        sb.append(msgHeader("Please enter your registered email address"));
        sb.append(HtmlUtils.p());
        sb.append(request.form(getRepositoryBase().URL_USER_FINDUSERID));
        sb.append(msgLabel("Your Email"));
        sb.append(HtmlUtils.space(1));
        sb.append(HtmlUtils.input(ARG_USER_EMAIL, email, HtmlUtils.SIZE_30));
        sb.append(HtmlUtils.space(1));
        sb.append(HtmlUtils.submit("Submit"));
        sb.append(HtmlUtils.formClose());

        sb.append(HtmlUtils.p());
        sb.append(HtmlUtils.img(getRepository().getUrlBase()
                                + "/images/dilbert.gif"));

        return new Result(msg("Password Reset"), sb);
    }




    /**
     * _more_
     *
     * @param request the request
     *
     * @return The result
     *
     * @throws Exception On badness
     */
    public Result processResetPassword(Request request) throws Exception {

        if ( !canDoLogin(request)) {
            return new Result(
                msg("Password Reset"),
                new StringBuffer(
                    getPageHandler().showDialogWarning(
                        msg("Login is not allowed"))));
        }


        String key = request.getString(ARG_USER_PASSWORDKEY, (String) null);
        PasswordReset resetInfo = null;
        StringBuffer  sb        = new StringBuffer();
        if (key != null) {
            resetInfo = passwordResets.get(key);
            if (resetInfo != null) {
                if (new Date().getTime() > resetInfo.dttm.getTime()) {
                    sb.append(
                        getPageHandler().showDialogError(
                            getRepository().translate(
                                request,
                                "Password reset has timed out") + "<br>"
                                    + getRepository().translate(
                                        request, "Please try again")));
                    resetInfo = null;
                    passwordResets.remove(key);
                }
            } else {
                sb.append(
                    getPageHandler().showDialogError(
                        getRepository().translate(
                            request, "Password reset has timed out") + "<br>"
                                + getRepository().translate(
                                    request, "Please try again")));
            }
        }

        User user = ((resetInfo != null)
                     ? findUser(resetInfo.user, false)
                     : null);
        if (user != null) {
            if (request.exists(ARG_USER_PASSWORD1)) {
                if (checkAndSetNewPassword(request, user)) {
                    applyUserProperties(request, user, false);
                    sb.append(
                        getPageHandler().showDialogNote(
                            msg("Your password has been reset")));
                    sb.append(makeLoginForm(request));
                    addActivity(request, request.getUser(),
                                ACTIVITY_PASSWORD_CHANGE, "");

                    return new Result(msg("Password Reset"), sb);
                }
                sb.append(
                    getPageHandler().showDialogWarning(
                        "Incorrect passwords"));
            }
            sb.append(msgHeader("Please reset your password"));
            sb.append(HtmlUtils.p());

            request.formPostWithAuthToken(
                sb, getRepositoryBase().URL_USER_RESETPASSWORD);
            sb.append(HtmlUtils.hidden(ARG_USER_PASSWORDKEY, key));
            sb.append(HtmlUtils.formTable());
            sb.append(formEntry(request, msgLabel("User"), user.getId()));
            sb.append(formEntry(request, msgLabel("Password"),
                                HtmlUtils.password(ARG_USER_PASSWORD1)));
            sb.append(formEntry(request, msgLabel("Password Again"),
                                HtmlUtils.password(ARG_USER_PASSWORD2)));
            sb.append(formEntry(request, "", HtmlUtils.submit("Submit")));

            sb.append(HtmlUtils.formTableClose());
            sb.append(HtmlUtils.formClose());

            return new Result(msg("Password Reset"), sb);
        }

        if ( !getAdmin().isEmailCapable()) {
            return new Result(
                msg("Password Reset"),
                new StringBuffer(
                    getPageHandler().showDialogWarning(
                        msg(
                        "This RAMADDA server has not been configured to send email"))));
        }


        if (user == null) {
            user = findUser(request.getString(ARG_USER_NAME, ""), false);
        }
        if (user == null) {
            if (request.exists(ARG_USER_NAME)) {
                sb.append(
                    getPageHandler().showDialogError(
                        getRepository().translate(
                            request, "Not a registered user")));
                sb.append(HtmlUtils.p());
            }
            addPasswordResetForm(request, sb,
                                 request.getString(ARG_USER_NAME, ""));

            return new Result(msg("Password Reset"), sb);
        }



        if ( !request.getUser().canEditSettings()
                && !request.getUser().getAnonymous()) {
            return new Result(msg("Password Reset"),
                              new StringBuffer(msg("Cannot reset password")));
        }

        key = getRepository().getGUID() + "_" + Math.random();
        //Time out is 1 hour
        resetInfo = new PasswordReset(user.getId(),
                                      new Date(new Date().getTime()
                                          + 1000 * 60 * 60));
        passwordResets.put(key, resetInfo);
        String toUser = user.getEmail();
        String url =
            request.getAbsoluteUrl(getRepository().URL_USER_RESETPASSWORD)
            + "?" + ARG_USER_PASSWORDKEY + "=" + key;
        String template = getProperty(PROP_USER_RESET_PASSWORD_TEMPLATE, "");
        template = template.replace("${url}", url);
        template = template.replace("${userid}", user.getId());
        String subject = getProperty(PROP_USER_RESET_PASSWORD_SUBJECT,
                                     "Your RAMADDA Password");
        getRepository().getMailManager().sendEmail(toUser, subject, template,
                true);
        StringBuffer message = new StringBuffer();
        message.append(
            getPageHandler().showDialogNote(
                "Instructions on how to reset your password have been sent to your registered email address."));

        return new Result("Password Reset", message);
    }


    /**
     * _more_
     *
     * @param request the request
     * @param sb _more_
     * @param name _more_
     */
    private void addPasswordResetForm(Request request, StringBuffer sb,
                                      String name) {
        sb.append(msgHeader("Please enter your user ID"));
        sb.append(HtmlUtils.p());
        request.formPostWithAuthToken(
            sb, getRepositoryBase().URL_USER_RESETPASSWORD);
        sb.append(msgLabel("User ID"));
        sb.append(HtmlUtils.space(1));
        sb.append(HtmlUtils.input(ARG_USER_NAME, name, HtmlUtils.SIZE_20));
        sb.append(HtmlUtils.space(1));
        sb.append(HtmlUtils.submit(msg("Reset your password")));
        sb.append(HtmlUtils.formClose());
        sb.append(HtmlUtils.p());
        sb.append(HtmlUtils.img(getRepository().getUrlBase()
                                + "/images/dilbert.gif"));
    }




    /**
     * _more_
     *
     * @param user _more_
     * @param rawPassword raw password
     *
     * @return _more_
     *
     * @throws Exception on badness
     */
    public boolean isPasswordValid(User user, String rawPassword)
            throws Exception {
        return isPasswordValid(user.getId(), rawPassword);
    }


    /**
     * _more_
     *
     * @param userId the user id
     * @param rawPassword raw (unhashed) password
     *
     * @return _more_
     *
     * @throws Exception On badness
     */
    public boolean isPasswordValid(String userId, String rawPassword)
            throws Exception {
        User user = authenticateUser(null, userId, rawPassword,
                                     new StringBuffer());
        if (user == null) {
            return false;
        }

        return true;
    }



    /**
     * _more_
     *
     * @param request the request
     *
     * @return The result
     *
     * @throws Exception On badness
     */
    public Result processLogin(Request request) throws Exception {

        debugLogin("RAMADDA.processLogin");

        if ( !canDoLogin(request)) {
            return new Result(
                msg("Login"),
                new StringBuffer(
                    getPageHandler().showDialogWarning(
                        msg("Login is not allowed"))));
        }

        boolean responseAsXml = request.getString(ARG_RESPONSE,
                                    "").equals(RESPONSE_XML);
        StringBuffer sb             = new StringBuffer();
        User         user           = null;
        String       output         = request.getString(ARG_OUTPUT, "");
        StringBuffer loginFormExtra = new StringBuffer();
        if (request.exists(ARG_USER_ID)) {
            String name = request.getString(ARG_USER_ID, "").trim();
            if (name.equals(USER_DEFAULT) || name.equals(USER_ANONYMOUS)) {
                name = "";
            }
            String password = request.getString(ARG_USER_PASSWORD, "").trim();

            if ((name.length() > 0) && (password.length() > 0)) {
                user = authenticateUser(request, name, password,
                                        loginFormExtra);
            }



            if (user != null) {
                addActivity(request, user, ACTIVITY_LOGIN, "");
                debugLogin("RAMADDA.processLogin: login OK. user=" + user);
                getSessionManager().createSession(request, user);
                debugLogin("RAMADDA.processLogin: after create session:"
                           + request.getUser());

                if (responseAsXml) {
                    System.err.println("RAMADDA: setting user session "
                                       + request.getSessionId());
                }

                if (responseAsXml) {
                    return new Result(XmlUtil.tag(TAG_RESPONSE,
                            XmlUtil.attr(ATTR_CODE, CODE_OK),
                            request.getSessionId()), MIME_XML);
                }
                String       destUrl;
                String       destMsg;
                StringBuffer response = new StringBuffer();
                response.append(
                    getPageHandler().showDialogNote(
                        msg("You are logged in")));
                if (request.exists(ARG_REDIRECT)) {
                    destUrl = new String(
                        RepositoryUtil.decodeBase64(
                            request.getUnsafeString(ARG_REDIRECT, "")));
                    //Gack  - make sure we don't redirect to the logout page
                    if (destUrl.indexOf("logout") < 0) {
                        return new Result(destUrl);
                    }
                    response.append(
                        HtmlUtils.href(
                            getRepositoryBase().URL_ENTRY_SHOW.toString(),
                            msg("Continue")));
                } else if ( !user.canEditSettings()) {
                    response.append(
                        HtmlUtils.href(
                            getRepository().getUrlBase(), msg("Continue")));
                } else {
                    //Redirect to the top-level entry
                    if (true) {
                        return new Result(
                            getRepositoryBase().URL_ENTRY_SHOW.toString());
                    }
                    response.append(
                        HtmlUtils.href(
                            getRepositoryBase().URL_ENTRY_SHOW.toString(),
                            msg(
                            "Continue to the top level of the repository")));
                    response.append("<p>");
                    response.append(
                        HtmlUtils.href(
                            getRepositoryBase().URL_USER_HOME.toString(),
                            msg("Continue to user home")));
                }

                return addHeader(request, new Result("Login", response));
            } else {
                Misc.sleepSeconds(1);
                if (responseAsXml) {
                    return new Result(XmlUtil.tag(TAG_RESPONSE,
                            XmlUtil.attr(ATTR_CODE, CODE_ERROR),
                            "Incorrect user name or password"), MIME_XML);
                }

                if (name.length() > 0) {
                    //Check if they have a blank password
                    Statement statement = getDatabaseManager().select(
                                              Tables.USERS.COL_PASSWORD,
                                              Tables.USERS.NAME,
                                              Clause.eq(
                                                  Tables.USERS.COL_ID, name));
                    ResultSet results = statement.getResultSet();
                    if (results.next()) {
                        password = results.getString(1);
                        if ((password == null) || (password.length() == 0)) {
                            sb.append(
                                getPageHandler().showDialogNote(
                                    "Sorry, we were doing some cleanup and have reset your password"));

                            addPasswordResetForm(request, sb, name);
                            getDatabaseManager().closeAndReleaseConnection(
                                statement);

                            return addHeader(request,
                                             new Result(msg("Login"), sb));
                        }
                    }
                    getDatabaseManager().closeAndReleaseConnection(statement);
                }

                //TODO: what to do when we have ssl here?
                sb.append(
                    getPageHandler().showDialogWarning(
                        msg("Incorrect user name or password")));


            }
        }


        if (user == null) {
            sb.append(makeLoginForm(request, loginFormExtra.toString()));
        }

        return addHeader(request, new Result(msg("Login"), sb));


    }


    /**
     * _more_
     *
     * @param request the request
     * @param name _more_
     * @param password _more_
     * @param loginFormExtra _more_
     *
     * @return _more_
     *
     * @throws Exception on badness
     */
    private User authenticateUser(Request request, String name,
                                  String password,
                                  StringBuffer loginFormExtra)
            throws Exception {

        debugLogin("RAMADDA.authenticateUser:" + name);

        User user = authenticateUserFromDatabase(request, name, password);
        if (user != null) {
            debugLogin(
                "RAMADDA.authenticateUser: authenticated from database:"
                + user);

            return user;
        }

        //Try the authenticators
        for (UserAuthenticator userAuthenticator : userAuthenticators) {
            debugLogin("RAMADDA.authenticateUser: trying:"
                       + userAuthenticator);
            user = userAuthenticator.authenticateUser(getRepository(),
                    request, loginFormExtra, name, password);
            if (user != null) {
                user.setIsLocal(false);
                debugLogin(
                    "RAMADDA.authenticateUser: authenticated from external authenticator: "
                    + user + " " + userAuthenticator);

                return user;
            }
        }


        //
        //!!IMPORTANT!!
        //Chain up to the parent
        //This allows anyone in a parent repository to have a login in the child repository
        //If that user is an admin then they have admin rights here
        //
        if (getRepository().getParentRepository() != null) {
            if (name.startsWith("parent:")) {
                name = name.replace("parent:", "");
            }
            debugLogin("RAMADDA. authenticating user with parent repository");
            user = getRepository().getParentRepository().getUserManager()
                .authenticateUser(request, name, password, loginFormExtra);
            if (user != null) {
                debugLogin("RAMADDA. got user from parent repository");
                String userName = user.getName();
                if (userName.length() == 0) {
                    userName = user.getId();
                }
                //Change the name to denote this user comes from above
                user.setName(getRepository().getParentRepository()
                    .getUrlBase().substring(1) + ":" + userName);
            }
        }

        return user;
    }


    /**
     * _more_
     *
     * @param request _more_
     * @param name _more_
     * @param password _more_
     *
     * @return _more_
     *
     * @throws Exception _more_
     */
    private User authenticateUserFromDatabase(Request request, String name,
            String password)
            throws Exception {

        Statement statement =
            getDatabaseManager().select(Tables.USERS.COLUMNS,
                                        Tables.USERS.NAME,
                                        Clause.eq(Tables.USERS.COL_ID, name));

        ResultSet results = statement.getResultSet();

        try {
            //User is not in the database
            if ( !results.next()) {
                debugLogin("RAMADDA: No user in database:" + name);

                return null;
            }

            String storedHash =
                results.getString(Tables.USERS.COL_NODOT_PASSWORD);
            if ( !Utils.stringDefined(storedHash)) {
                debugLogin("RAMADDA: No stored hash");

                return null;
            }

            String passwordToUse = getPasswordToUse(password);
            //Call getPasswordToUse to add the system salt
            boolean userOK = PasswordHash.validatePassword(passwordToUse,
                                 storedHash);

            //Check for old formats of hashes
            if ( !userOK) {
                userOK = storedHash.equals(hashPassword_oldway(password));
                //            System.err.println ("trying the old way:" + userOK);
            }

            if ( !userOK) {
                userOK = storedHash.equals(hashPassword_oldoldway(password));
                //            System.err.println ("trying the old old way:" + userOK);
            }


            if (userOK) {
                return getUser(results);
            }

            return null;
        } finally {
            getDatabaseManager().closeAndReleaseConnection(statement);
        }
    }

    /**
     * _more_
     *
     * @param request the request
     *
     * @return The result
     *
     * @throws Exception On badness
     */
    public Result processLogout(Request request) throws Exception {
        StringBuffer sb = new StringBuffer();
        addActivity(request, request.getUser(), ACTIVITY_LOGOUT, "");
        getSessionManager().debugSession("RAMADDA.processLogout: "
                                         + request.getSessionId());
        getSessionManager().removeUserSession(request);
        request.setSessionId(getSessionManager().createSessionId());
        sb.append(getPageHandler().showDialogNote(msg("You are logged out")));
        sb.append(makeLoginForm(request));
        Result result = new Result(msg("Logout"), sb);

        return addHeader(request, result);
    }



    /**
     * _more_
     *
     * @throws Exception On badness
     */
    public void initOutputHandlers() throws Exception {
        OutputHandler outputHandler = new OutputHandler(getRepository(),
                                          "Cart") {
            public void getEntryLinks(Request request, State state,
                                      List<Link> links)
                    throws Exception {
                if (state.getEntry() != null) {
                    Link link;
                    if (isCartEnabled()) {
                        List<Entry> cart = getCart(request);
                        boolean entryIsInCart =
                            cart.contains(state.getEntry());
                        if ( !entryIsInCart) {
                            link = makeLink(request, state.getEntry(),
                                            OUTPUT_CART_ADD);
                            link.setLinkType(OutputType.TYPE_FILE
                                             | OutputType.TYPE_TOOLBAR);
                            links.add(link);
                        } else {
                            link = makeLink(request, state.getEntry(),
                                            OUTPUT_CART_REMOVE);
                            link.setLinkType(OutputType.TYPE_FILE
                                             | OutputType.TYPE_ACTION);
                            links.add(link);
                        }
                    }


                    if ( !request.getUser().getAnonymous()) {
                        link = makeLink(request, state.getEntry(),
                                        OUTPUT_FAVORITE);
                        link.setLinkType(OutputType.TYPE_TOOLBAR);
                        links.add(link);
                    }
                }
            }

            public boolean canHandleOutput(OutputType output) {
                if (output.equals(OUTPUT_CART_ADD)
                        || output.equals(OUTPUT_CART_REMOVE)) {
                    return isCartEnabled();
                }

                return output.equals(OUTPUT_FAVORITE);
            }

            public Result outputGroup(Request request, OutputType outputType,
                                      Entry group, List<Entry> subGroups,
                                      List<Entry> entries)
                    throws Exception {
                OutputType output = request.getOutput();
                if (output.equals(OUTPUT_CART_ADD)) {
                    if (group.isDummy()) {
                        addToCart(request, entries);
                        addToCart(request,
                                  (List<Entry>) new ArrayList(subGroups));
                    } else {
                        addToCart(request, (List<Entry>) Misc.newList(group));
                    }

                    return showCart(request);
                } else if (output.equals(OUTPUT_CART_REMOVE)) {
                    if (group.isDummy()) {
                        removeFromCart(request, entries);
                        removeFromCart(
                            request, (List<Entry>) new ArrayList(subGroups));
                    } else {
                        removeFromCart(request,
                                       (List<Entry>) Misc.newList(group));
                    }

                    return showCart(request);
                } else {
                    User user = request.getUser();
                    if (group.isDummy()) {
                        addFavorites(request, user, entries);
                        addFavorites(request, user,
                                     (List<Entry>) new ArrayList(subGroups));
                    } else {
                        addFavorites(request, user,
                                     (List<Entry>) Misc.newList(group));
                    }
                    String redirect =
                        getRepositoryBase().URL_USER_HOME.toString();

                    return new Result(HtmlUtils.url(redirect, ARG_MESSAGE,
                            getRepository().translate(request,
                                "Favorites Added")));

                }

            }
        };

        getRepository().addOutputHandler(outputHandler);
    }


    /**
     * _more_
     *
     * @return _more_
     *
     * @throws Exception On badness
     */
    public List<String> getRoles() throws Exception {
        String[] roleArray =
            SqlUtil.readString(
                getDatabaseManager().getIterator(
                    getDatabaseManager().select(
                        SqlUtil.distinct(Tables.USERROLES.COL_ROLE),
                        Tables.USERROLES.NAME, new Clause())), 1);
        List<String> roles = new ArrayList<String>(Misc.toList(roleArray));

        for (UserAuthenticator userAuthenticator : userAuthenticators) {
            List<String> authenticatorRoles = userAuthenticator.getAllRoles();
            if (authenticatorRoles != null) {
                roles.addAll(authenticatorRoles);
            }
        }
        roles.add(0, ROLE_GUEST);
        roles.add(0, ROLE_ANONYMOUS);
        roles.add(0, ROLE_NONE);
        roles.add(0, ROLE_ANY);
        roles.add(0, ROLE_USER);

        return roles;
    }




    /**
     * _more_
     *
     * @param request the request
     * @param user The user
     * @param what _more_
     * @param extra _more_
     *
     * @throws Exception On badness
     */
    private void addActivity(Request request, User user, String what,
                             String extra)
            throws Exception {
        getDatabaseManager().executeInsert(Tables.USER_ACTIVITY.INSERT,
                                           new Object[] { user.getId(),
                new Date(), what, extra, request.getIp() });
    }





    /**
     * _more_
     *
     * @param request the request
     *
     * @return The result
     *
     * @throws Exception On badness
     */
    public Result processActivityLog(Request request) throws Exception {
        StringBuffer sb   = new StringBuffer();

        User         user = findUser(request.getString(ARG_USER_ID, ""));

        if (user == null) {
            sb.append(
                getPageHandler().showDialogError(
                    getRepository().translate(
                        request, "Could not find user")));
        } else {
            sb.append(getUserActivities(request, user));
        }

        return getAdmin().makeResult(request, msg("User Log"), sb);
    }



    /**
     * _more_
     *
     * @param request the request
     * @param theUser The user
     *
     * @return _more_
     *
     * @throws Exception On badness
     */
    private String getUserActivities(Request request, User theUser)
            throws Exception {
        StringBuffer sb          = new StringBuffer();
        Clause       clause      = null;
        String       limitString = "";
        if (theUser != null) {
            clause = Clause.eq(Tables.USER_ACTIVITY.COL_USER_ID,
                               theUser.getId());
        } else {
            limitString = getDatabaseManager().getLimitString(0,
                    request.get(ARG_LIMIT, 100));
        }


        Statement statement =
            getDatabaseManager().select(Tables.USER_ACTIVITY.COLUMNS,
                                        Tables.USER_ACTIVITY.NAME, clause,
                                        " order by "
                                        + Tables.USER_ACTIVITY.COL_DATE
                                        + " desc " + limitString);

        SqlUtil.Iterator iter = getDatabaseManager().getIterator(statement);
        ResultSet        results;
        if (theUser != null) {
            sb.append(msgLabel("Activity for User"));
            sb.append(HtmlUtils.space(1));
            sb.append(theUser.getLabel());
        }
        sb.append(HtmlUtils.p());
        sb.append(HtmlUtils.open(HtmlUtils.TAG_TABLE));
        sb.append(HtmlUtils.row(HtmlUtils.cols(((theUser == null)
                ? HtmlUtils.b(msg("User"))
                : ""), HtmlUtils.b(msg("Activity")),
                       HtmlUtils.b(msg("Date")),
                       HtmlUtils.b(msg("IP Address")))));

        int cnt = 0;
        while ((results = iter.getNext()) != null) {
            int    col      = 1;
            String userId   = results.getString(col++);
            String firstCol = "";
            if (theUser == null) {
                User user = findUser(userId);
                if (user == null) {
                    firstCol = "No user:" + userId;
                } else {
                    firstCol =
                        HtmlUtils.href(
                            request.url(
                                getRepositoryBase().URL_USER_ACTIVITY,
                                ARG_USER_ID, user.getId()), HtmlUtils.img(
                                    getRepository().iconUrl(ICON_LOG),
                                    msg("View user log")) + " "
                                        + user.getLabel());
                }

            }
            Date   dttm  = getDatabaseManager().getDate(results, col++);
            String what  = results.getString(col++);
            String extra = results.getString(col++);
            String ip    = results.getString(col++);
            sb.append(HtmlUtils.row(HtmlUtils.cols(firstCol, what,
                    getPageHandler().formatDate(dttm), ip)));

            cnt++;
        }
        sb.append(HtmlUtils.close(HtmlUtils.TAG_TABLE));
        if (cnt == 0) {
            sb.append(msg("No activity"));
        }

        return sb.toString();
    }




    /**
     * _more_
     *
     * @param request the request
     *
     * @return The result
     *
     * @throws Exception On badness
     */
    public Result processForm(Request request) throws Exception {
        Result result = checkIfUserCanChangeSettings(request);
        if (result != null) {
            return result;
        }
        StringBuffer sb   = new StringBuffer();
        User         user = request.getUser();
        request.appendMessage(sb);
        sb.append(HtmlUtils.p());
        sb.append(msgHeader("User Settings"));
        request.formPostWithAuthToken(sb,
                                      getRepositoryBase().URL_USER_CHANGE);
        makeUserForm(request, user, sb, false);
        sb.append(HtmlUtils.submit(msg("Change Settings"), ARG_USER_CHANGE));
        sb.append(HtmlUtils.formClose());

        if (user.canChangePassword()) {
            sb.append(HtmlUtils.p());
            sb.append(msgHeader("Password"));
            request.formPostWithAuthToken(
                sb, getRepositoryBase().URL_USER_CHANGE);
            makePasswordForm(request, user, sb);
            sb.append(HtmlUtils.submit(msg("Change Password"),
                                       ARG_USER_CHANGE));
            sb.append(HtmlUtils.formClose());
        }


        sb.append(HtmlUtils.p());

        String roles = user.getRolesAsString("<br>").trim();
        if (roles.length() == 0) {
            roles = "--none--";
        } else {
            sb.append(msgHeader("Your Roles"));
        }

        sb.append(HtmlUtils.formTable());
        sb.append(formEntryTop(request, msgLabel("Roles"), roles));
        sb.append(HtmlUtils.formTableClose());

        return makeResult(request, msg("User Settings"), sb);
    }




    /**
     * _more_
     *
     * @param request the request
     *
     * @return The result
     *
     * @throws Exception on badness
     */
    public Result checkIfUserCanChangeSettings(Request request)
            throws Exception {
        User user = request.getUser();
        if (user.getAnonymous()) {
            StringBuffer sb = new StringBuffer();
            sb.append(
                getPageHandler().showDialogWarning(
                    msg("You need to be logged in to change user settings")));
            sb.append(makeLoginForm(request));

            return addHeader(request, new Result(msg("User Settings"), sb));
        }

        if ( !user.canEditSettings()) {
            StringBuffer sb = new StringBuffer();
            sb.append(
                getPageHandler().showDialogWarning(
                    msg("You cannot edit your settings")));

            return addHeader(request, new Result(msg("User Settings"), sb));
        }

        return null;
    }



    /**
     * _more_
     *
     * @param request the request
     *
     * @return The result
     *
     * @throws Exception on badness
     */
    public Result processChange(Request request) throws Exception {
        Result result = checkIfUserCanChangeSettings(request);
        if (result != null) {
            return result;
        }
        if ( !request.exists(ARG_USER_CHANGE)) {
            return new Result(getRepositoryBase().URL_USER_FORM.toString());
        }
        User         user = request.getUser();
        StringBuffer sb   = new StringBuffer();
        request.ensureAuthToken();
        boolean settingsOk = true;
        String  message;
        if (request.exists(ARG_USER_PASSWORD1)) {
            settingsOk = checkAndSetNewPassword(request, user);
            if ( !settingsOk) {
                sb.append(
                    getPageHandler().showDialogWarning(
                        msg("Incorrect passwords")));
                message = "Incorrect passwords";
            } else {
                //msg("Your password has been changed");
                message = "Your password has been changed";
                addActivity(request, request.getUser(),
                            ACTIVITY_PASSWORD_CHANGE, "");
            }
        } else {
            message = "Your settings have been changed";
            //msg("Your settings have been changed");
        }
        String formUrl = getRepository().getUrlPath(request,
                             getRepositoryBase().URL_USER_FORM);
        if (settingsOk) {
            applyUserProperties(request, user, false);
            String redirect = formUrl;
            //If we are under ssl then redirect to non-ssl
            if (getRepository().isSSLEnabled(request)) {
                //redirect = request.getAbsoluteUrl(getRepository().URL_USER_FORM));
            } else {
                //redirect = request.getAbsoluteUrl(getRepository().URL_USER_FORM));
            }

            return new Result(
                HtmlUtils.url(
                    redirect, ARG_MESSAGE,
                    getRepository().translate(request, message)));
        }

        return new Result(HtmlUtils.url(formUrl, ARG_MESSAGE,
                                        getRepository().translate(request,
                                            message)));

    }



    /**
     * hash the given raw text password for storage into the database
     *
     * @param password raw text password
     *
     * @return hashed password
     */
    public String hashPassword_oldoldway(String password) {
        if (getProperty(PROP_PASSWORD_OLDMD5, false)) {
            return RepositoryUtil.hashPasswordForOldMD5(password);
        } else {
            return RepositoryUtil.hashPassword(password);
        }
    }



    /**
     * hash the given raw text password for storage into the database
     *
     * @param password raw text password
     *
     * @return hashed password
     */
    private String hashPassword_oldway(String password) {
        //See, e.g. http://www.jasypt.org/howtoencryptuserpasswords.html
        try {
            //having a single salt repository wide isn't a great way to do this
            //It really should be a per user/password salt that gets stored in the db as well
            if (salt1.length() > 0) {
                password = salt1 + password;
            }
            int hashIterations =
                getRepository().getProperty(PROP_PASSWORD_ITERATIONS, 1);
            byte[] bytes = password.getBytes("UTF-8");
            for (int i = 0; i < hashIterations; i++) {
                bytes = doHashPassword(bytes);
            }
            if (salt2.length() > 0) {
                byte[] prefix   = salt2.getBytes("UTF-8");
                byte[] newBytes = new byte[prefix.length + bytes.length];
                for (int i = 0; i < prefix.length; i++) {
                    newBytes[i] = prefix[i];
                }
                for (int i = 0; i < bytes.length; i++) {
                    newBytes[prefix.length + i] = bytes[i];
                }
                bytes = newBytes;
            }
            String result = RepositoryUtil.encodeBase64(bytes);

            return result.trim();
        } catch (Exception exc) {
            throw new RuntimeException(exc);
        }
    }


    /**
     * _more_
     *
     * @param bytes _more_
     *
     * @return _more_
     */
    private byte[] doHashPassword(byte[] bytes) {
        try {
            String digest    = getProperty(PROP_PASSWORD_DIGEST, "SHA-512");
            MessageDigest md = MessageDigest.getInstance(digest);
            md.update(bytes);

            return md.digest();
        } catch (Exception exc) {
            throw new RuntimeException(exc);
        }
    }

}
