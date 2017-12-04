package myiss;

import java.awt.image.BufferedImage;
import java.io.BufferedReader;
import java.io.DataOutputStream;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;

import java.net.URLEncoder;

import java.security.MessageDigest;

import java.security.NoSuchAlgorithmException;

import java.sql.Connection;
import java.sql.SQLException;
import java.text.SimpleDateFormat;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.TimeZone;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;

import javax.imageio.ImageIO;

import javax.servlet.RequestDispatcher;
import javax.servlet.ServletException;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.inet.dbtools.DBSelect;
import org.inet.dbtools.DBTools;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;

public class mainServlet extends HttpServlet {

    public static String driverName = "";
    public static String connectionString = "";
    private HttpSession userSession;
    private static final int DEF_ITEMS_ON_PAGE = 100;
    public static final String actionList[] = {"enter", "menu", "query"};
    public static boolean initOk = false;
    public static int debuggerUserId = -1;
    private static HashMap userList = new HashMap();

    @Override
    public void init() throws ServletException {
        super.init();
        FileInputStream fis;
        try {

            Properties p = new Properties();
            p.load(super.getServletContext().getResourceAsStream("/WEB-INF/myiss.properties"));
            if (p.containsKey("driverName")) {
                driverName = p.getProperty("driverName");
            }
            if (p.containsKey("connectionString")) {
                connectionString = p.getProperty("connectionString");
            }
            if (p.containsKey("debuggerUserId")) {
                debuggerUserId = Tools.parseInt(p.getProperty("debuggerUserId"), -1);
            }
            setUserList();
            initOk = true;
        } catch (FileNotFoundException e) {
            System.out.println("Props file not found");
            initOk = false;

        } catch (IOException e) {
            System.out.println("IO Exception during reading props");
            e.printStackTrace();
            initOk = false;
        } catch (Exception e) {
            e.printStackTrace();
            System.out.println("DB connection lost. User list cannot be read.");
        }

        //connectionString = "jdbc:jtds:sqlserver://admnt.jinr.ru:1433;DatabaseName=ASU;SelectMethod=cursor;User=wq;Password=wq99;SelectMethod=cursor;";
        //driverName = "net.sourceforge.jtds.jdbc.Driver";
        //initOk=true;
    }

    //private List<String> servletExceptions = new ArrayList<String>();
    private void addException(Exception e) {
        List<String> exList = (userSession.getAttribute("EXCEPTION_LIST") != null) ? (List<String>) userSession.getAttribute("EXCEPTION_LIST") : new ArrayList<String>();
        exList.add(e.getLocalizedMessage());
        userSession.setAttribute("EXCEPTION_LIST", exList);
    }

    private int getAction(HashMap rParams) {
        for (int i = 0; i < actionList.length; i++) {
            if (((String) rParams.get("action")).equals(actionList[i])) {
                return i;
            }
        }
        return -1;
    }

    private String getSubmenuIdByQueryTableName(int uid, String tname, Connection conn) {
        String sSQL = "SELECT  SM.SUB_NAME,SM.SUB_ID, M.NAME, M.MENU_ID  FROM USERS U  \n"
                + "INNER JOIN  USER_MENU UM ON U.MENU_GROUP=UM.GROUP_CODE  \n"
                + "INNER JOIN MENU M ON M.MENU_ID=UM.MENU_ID  \n"
                + "INNER JOIN USER_RIGHTS UR ON U.RIGHTS_GROUP=UR.GROUP_CODE  \n"
                + "INNER JOIN SUB_MENU SM  ON M.MENU_ID=SM.SUB_ID AND SM.Q_NAME = ?"
                + "WHERE U.ID  = ? \n";
        ArrayList param = new ArrayList();

        param.add(tname);
        param.add(uid);
        HashMap res = new HashMap();
        try {
            res = DBSelect.getRow(sSQL, param, conn);
        } catch (SQLException e) {
            addException(e);
            e.printStackTrace();
        }
        return Tools.getStringValue(res.get("SUB_ID"), "");
    }

    private HashMap getNavigationByQuery(int uid, String qName, Connection conn) {
        HashMap out = new HashMap();
        ArrayList param = new ArrayList();
        String sSQL = "SELECT  SM.SUB_NAME,SM.SUB_ID, M.NAME, M.MENU_ID   "
                + "FROM USERS U  "
                + "INNER JOIN  USER_MENU UM ON U.MENU_GROUP=UM.GROUP_CODE  "
                + "INNER JOIN MENU M ON M.MENU_ID=UM.MENU_ID  "
                + "INNER JOIN USER_RIGHTS UR ON U.RIGHTS_GROUP=UR.GROUP_CODE  "
                + "INNER JOIN SUB_MENU SM  ON M.MENU_ID=SM.SUB_ID AND SM.Q_NAME = ?  "
                + "WHERE U.ID  = ? ";
        param.add(qName);
        param.add(uid);
        try {
            out = DBSelect.getRow(sSQL, param, conn);
        } catch (SQLException e) {
            addException(e);
            e.printStackTrace();

        }
        return out;

    }

    private List<HashMap> getFilters4query(String tname, String qname, Connection conn) {
        //PARAM_xxx
        // NameQuery
        // NameParam
        // N 
        List<HashMap> res = new ArrayList<HashMap>();
        try {
            tname = tname.toUpperCase().replace("QUERY", "PARAM");
            ArrayList param = new ArrayList();
            String sSQL = "SELECT * FROM " + tname + " WHERE NAMEQUERY=?";
            System.out.println("FILTER:>>>>> " + sSQL.replace("?", "'" + qname + "'"));
            param.add(qname);
            res = DBSelect.getRows(sSQL, param, conn);
        } catch (Exception e) {
            addException(e);
            e.printStackTrace();

        } finally {
            return res;
        }
    }

    private List<String> getParamNamesFromSQL(String sSQL) {
        List<String> matchList = new UniqueArrayList();
        try {

            Pattern regex = Pattern.compile("\\[(.*?)\\]", Pattern.CASE_INSENSITIVE | Pattern.UNICODE_CASE | Pattern.MULTILINE);
            Matcher regexMatcher = regex.matcher(sSQL);
            while (regexMatcher.find()) {
                matchList.add(regexMatcher.group(1));
            }
        } catch (PatternSyntaxException ex) {
            throw ex;
        }
        return matchList;
    }

    private HashMap getGraphic(String t_name, String q_name, Connection conn) throws SQLException {
        ArrayList p = new ArrayList();
        p.add(t_name);
        p.add(q_name);
        String sSQL = "SELECT * FROM GRAPHICS WHERE T_NAME = ? AND Q_NAME = ?";
        return DBSelect.getRow(sSQL, p, conn);

    }

    private List<HashMap> getLinks(String tname, String qname, Connection conn) {
        //PARAM_xxx
        // NameQuery
        // NameParam
        // N 
        List<HashMap> res = new ArrayList<HashMap>();
        try {
            tname = tname.toUpperCase().replace("QUERY", "LINKS");
            ArrayList param = new ArrayList();
            String sSQL = "SELECT * FROM " + tname + " WHERE QUERY=?";
            param.add(qname);
            res = DBSelect.getRows(sSQL, param, conn);
        } catch (Exception e) {
            addException(e);
            e.printStackTrace();

        } finally {
            return res;
        }
    }

    private HashMap getQueryData(String tname, String qname, Connection conn) {

        HashMap qdata = new HashMap();
        String sSQL = "";
        if (tname.length() > 0 && qname.length() > 0) {
            ArrayList param = new ArrayList();
            sSQL = "SELECT * FROM " + tname + " WHERE NAME_QUERY = ?";
            param.add(qname);
            try {
                qdata = DBSelect.getRow(sSQL, param, conn);
            } catch (SQLException e) {
                addException(e);
                e.printStackTrace();
            }
        }
        return qdata;

    }

    private HashMap getPSQueryAndParams(String runSQL, HashMap params, HashMap userrights) {
        HashMap res = new HashMap();
        ArrayList sqlparams = new ArrayList();
        Pattern regex = Pattern.compile("\\[(.*?)\\]", Pattern.CASE_INSENSITIVE | Pattern.UNICODE_CASE | Pattern.MULTILINE);
        Matcher regexMatcher = regex.matcher(runSQL);
        while (regexMatcher.find()) {
            for (int i = 1; i <= regexMatcher.groupCount(); i++) {
                String match = regexMatcher.group(i);
                //  String stringToReplace = "\\["+match+"\\]";
                String stringToReplace = "[" + match + "]";
                String paramValue = "";

                if (params.get(match) instanceof String[]) {
                    String[] val = (String[]) params.get(match);
                    paramValue = Tools.getStringValue(val[0], "");

                }

                if (params.get(match) instanceof String) {
                    paramValue = Tools.getStringValue(params.get(match), "");
                }

                if (userrights.get(match) != null && Tools.getStringValue(userrights.get(match), "", "").length() > 0) {
                    paramValue = Tools.getStringValue(userrights.get(match), "", "");
                    //  System.out.println("Опа");
                }

                //TODO: если параметры не строковые, то как-то определять и приводить к соответствующему типу.
                //runSQL = runSQL.replaceFirst(stringToReplace,"?");
                runSQL = runSQL.replace(stringToReplace, "?");
                //замена звезды
                if ("*".equals(paramValue)) {
                    paramValue = "%";
                }
                paramValue = paramValue.replaceAll("\\*", "%");
                sqlparams.add(paramValue);
            }
        }
        res.put("SQL", runSQL);
        res.put("SQLPARAMS", sqlparams);
        return res;
    }

    private List<HashMap> runQuery(String runSQL, HashMap params, Connection conn, boolean desc, String sortby, HashMap userrights) {
        if (sortby != null && sortby.length() > 0) {

            int pos = -1;

            try {
                Pattern regex = Pattern.compile("\\s+ORDER\\s+BY\\s+", Pattern.CASE_INSENSITIVE | Pattern.UNICODE_CASE | Pattern.MULTILINE);
                Matcher regexMatcher = regex.matcher(runSQL);
                while (regexMatcher.find()) {
                    pos = regexMatcher.start();
                }
            } catch (PatternSyntaxException ex) {
                ex.printStackTrace();
            }

            if (pos != -1) {
                runSQL = runSQL.substring(0, pos) + "\n ORDER BY '" + sortby + "' ";
                if (desc) {
                    runSQL += " DESC";
                }
            } else {
                runSQL += "\n ORDER BY '" + sortby + "' ";
                if (desc) {
                    runSQL += " DESC";
                }
            }
        }

        List<HashMap> res = new ArrayList();
        runSQL = runSQL.replaceAll("(?im)\"", "'");

        HashMap SQL2RUN = getPSQueryAndParams(runSQL, params, userrights);

        ArrayList sqlparams = (SQL2RUN.get("SQLPARAMS") != null) ? (ArrayList) SQL2RUN.get("SQLPARAMS") : new ArrayList();
        runSQL = (SQL2RUN.get("SQL") != null) ? (String) SQL2RUN.get("SQL") : "";
        System.out.println("RUN_QUERY>>>>>> " + runSQL);
        try {
            res = DBSelect.getRows(runSQL, sqlparams, conn);
        } catch (SQLException e) {
            addException(e);
            e.printStackTrace();

        }
        return res;
    }

    private List<HashMap> getMenu(int uid, Connection conn) {
        List<HashMap> res = new ArrayList<HashMap>();
        ArrayList sqlparam = new ArrayList();
        String sSQL = "SELECT * , (CASE WHEN SUBSCOUNT=1 THEN 0 ELSE 1  END) AS HAS_SUB "
                + " FROM ( "
                + " SELECT "
                + " COUNT (M.ID) AS SUBSCOUNT "
                + " ,M.NAME AS NAME, M.MENU_ID AS MENU_ID , M.ID AS ID "
                + " FROM USERS U "
                + " INNER JOIN USER_MENU UM ON U.MENU_GROUP = UM.GROUP_CODE "
                + " INNER JOIN MENU M ON M.MENU_ID = UM.MENU_ID "
                + " INNER JOIN USER_RIGHTS UR ON U.RIGHTS_GROUP = UR.GROUP_CODE "
                + " INNER JOIN SUB_MENU SM ON SM.SUB_ID = M.MENU_ID "
                + " WHERE U.ID = ? "
                + " AND EXISTS ( SELECT SUB_ID FROM SUB_MENU SM1 WHERE  SM1.SUB_ID = M.MENU_ID ) "
                + " GROUP BY M.NAME, M.MENU_ID, M.ID "
                + " ) AS Z "
                + " ORDER BY Z.MENU_ID";

        sqlparam.add(uid);
        List<HashMap> menu = new ArrayList<HashMap>();
        try {
            menu = DBSelect.getRows(sSQL, sqlparam, conn);
        } catch (SQLException e) {
            addException(e);
            e.printStackTrace();
        }
        sSQL = "SELECT SOURCE, Q_NAME FROM SUB_MENU WHERE SUB_ID = ? ";
        for (HashMap hm : menu) {
            int hasSubs = Tools.parseInt(hm.get("HAS_SUB"), -1);
            String menuId = Tools.getStringValue(hm.get("MENU_ID"), "");
            if (hasSubs == 0) {
                ArrayList p = new ArrayList();
                p.add(menuId);
                HashMap sub = new HashMap();
                try {
                    sub = DBSelect.getRow(sSQL, p, conn);
                } catch (SQLException e) {
                    addException(e);
                    e.printStackTrace();
                }
                String source = Tools.getStringValue(sub.get("SOURCE"), "");
                String q_name = Tools.getStringValue(sub.get("Q_NAME"), "");
                hm.put("SOURCE", source);
                hm.put("Q_NAME", q_name);
            }
            res.add(hm);
        }
        return res;
    }

    private List<HashMap> getSubMenu(int uid, String id, Connection conn) {
        List<HashMap> res = new ArrayList<HashMap>();
        ArrayList sqlparam = new ArrayList();
        sqlparam.add(id);
        sqlparam.add(uid);
        String sSQL = "SELECT "
                + " SM.* "
                + " FROM USERS U "
                + " INNER JOIN  USER_MENU UM ON U.MENU_GROUP=UM.GROUP_CODE "
                + " INNER JOIN MENU M ON M.MENU_ID=UM.MENU_ID "
                + " INNER JOIN USER_RIGHTS UR ON U.RIGHTS_GROUP=UR.GROUP_CODE "
                + " INNER JOIN SUB_MENU SM  ON M.MENU_ID=SM.SUB_ID AND SM.SUB_ID = ? "
                + //" AND SM.Q_NAME LIKE '%_13'" + 
                " WHERE U.ID  = ? ORDER BY M.MENU_ID "
                + " ,SM.SUB_NAME ";
        try {
            res = DBSelect.getRows(sSQL, sqlparam, conn);
        } catch (SQLException e) {
            addException(e);
            e.printStackTrace();

        }
        return res;
    }

    private String getQueryName(int uid, String qName, Connection conn) {
        HashMap res = new HashMap();
        ArrayList sqlparam = new ArrayList();
        sqlparam.add(qName);
        sqlparam.add(uid);
        String out = "";
        String sSQL = "SELECT \n"
                + "SM.Q_NAME "
                + ""
                + "FROM USERS U \n"
                + "INNER JOIN  USER_MENU UM ON U.MENU_GROUP=UM.GROUP_CODE\n"
                + "INNER JOIN MENU M ON M.MENU_ID=UM.MENU_ID\n"
                + "INNER JOIN USER_RIGHTS UR ON U.RIGHTS_GROUP=UR.GROUP_CODE\n"
                + "INNER JOIN SUB_MENU SM  ON M.MENU_ID=SM.SUB_ID  AND SM.Q_NAME = ? \n"
                + "WHERE U.ID  = ? ORDER BY M.MENU_ID\n"
                + ",SM.SUB_NAME ";
        try {
            res = DBSelect.getRow(sSQL, sqlparam, conn);
        } catch (SQLException e) {
            addException(e);
            e.printStackTrace();

        }
        if (res != null) {
            return (String) (res.get("Q_NAME"));
        }
        return out;
    }

    private List<HashMap> getQueryList(String qName, Connection conn) {
        List<HashMap> res = new ArrayList<HashMap>();
        String sSQL = "SELECT * FROM " + qName + " WHERE LEN(QUERY)>0 ORDER BY QUERY";
        try {
            res = DBSelect.getRows(sSQL, null, conn);
        } catch (SQLException e) {
            addException(e);
            e.printStackTrace();

        }
        return res;
    }

    private HashMap List2HashMap(List<HashMap> a, String uniqkey) {
        HashMap res = new HashMap();
        for (int i = 0; i < a.size(); i++) {
            HashMap hm = a.get(i);
            String key = (String) hm.get(uniqkey);
            res.put(key, hm);
        }
        return res;
    }

    private int getPageCount(int itemsCount, int itemsOnPage) {
        System.out.println("---");
        int pages = itemsCount / itemsOnPage;
        if (pages * itemsOnPage < itemsCount) {
            pages = pages + 1;
        }
        System.out.println(itemsCount + " / " + itemsOnPage + " = " + pages);
        return pages;
    }

    private HashMap getRequestParams(HttpServletRequest request) throws UnsupportedEncodingException {
        //****
        System.out.println(request.getCharacterEncoding());
        request.setCharacterEncoding("utf-8");
        HashMap out = new HashMap();
        HashMap prms = new HashMap(request.getParameterMap());

        out.putAll(prms);
        String action = Tools.getStringValue(request.getParameter("Action"), "");
        String id = Tools.getStringValue(request.getParameter("Id"), "");
        String name = Tools.getStringValue(request.getParameter("name"), "");
        String tname = Tools.getStringValue(request.getParameter("tname"), "");
        String qname = Tools.getStringValue(request.getParameter("qname"), "");
        boolean desc = "DESC".equals(Tools.getStringValue(request.getParameter("table_sort"), ""));
        String sortby = Tools.getStringValue(request.getParameter("table_sortby"), "");
        String doxls = Tools.getStringValue(request.getParameter("downloadXLS"), "");
        int Page = Tools.parseInt(request.getParameter("Page"), 1);
        int itemsOnPage = Tools.parseInt(request.getParameter("itemsOnPage"), DEF_ITEMS_ON_PAGE);
        out.put("action", action);
        out.put("id", id);
        out.put("name", name);
        out.put("tname", tname);
        out.put("qname", qname);
        out.put("tables_sort_desc", desc);
        out.put("tables_sortby", sortby);
        out.put("page", Page);
        out.put("itemsonpage", itemsOnPage);
        out.put("downloadXLS", doxls);
        return out;
    }

    private List<HashMap> filtersAndRequestValues(List<HashMap> filters, HashMap rParams) {
        List<HashMap> outList = new ArrayList<HashMap>();
        for (HashMap hm : filters) {
            if (rParams.get((String) hm.get("NameParam")) instanceof String[]
                    && ((String[]) rParams.get((String) hm.get("NameParam"))).length == 1
                    && ((String[]) rParams.get((String) hm.get("NameParam")))[0].equals("*")) {
                hm.put("~", "Все");
            } else {
                hm.put("~", rParams.get((String) hm.get("NameParam")));
            }
            outList.add(hm);
        }
        return outList;
    }

    private boolean findIp(String findStr, String source) {
        String[] IPs = {};
        boolean result = false;
        if (source.length() > 0) {
            IPs = source.split(",");
            for (int i = 0; i < IPs.length - 1; i++) {
                if (IPs[i].indexOf(findStr) == 1) {
                    result = true;
                    break;
                }
            }
        } else {
            result = true;
        }
        return result;
    }

    private int auth(HttpServletRequest request) {
        Cookie[] cookies = request.getCookies();
        if (cookies != null) {
            for (Cookie c : cookies) {
                if ("userid".equals(c.getName())) {
                    if (userList.containsKey(c.getValue())) {
                        return Tools.parseInt(userList.get(c.getValue()), -1);
                        //вошел старый пользователь
                    } else {
                        try {
                            // вдруг это новый пользователь? Тогда надо обновить список и снова попробовать найти такого.
                            setUserList();
                            if (userList.containsKey(c.getValue())) {
                                return Tools.parseInt(userList.get(c.getValue()), -1);
                            }
                        } catch (Exception e) {
                            this.addException(e);
                        }
                    }
                }
            }

        }
        return -1;
    }

    private String getUserName(int uId, Connection conn) throws SQLException {
        String sSQL = "SELECT NAME FROM USERS WHERE ID = ?";
        ArrayList p = new ArrayList();
        p.add(uId);
        return Tools.getStringValue(DBSelect.getRow(sSQL, p, conn).get("NAME"), "");
    }

    private void listRequestParams(HttpServletRequest request) throws UnsupportedEncodingException {
        //****
        request.setCharacterEncoding("utf-8");
        for (Object key : request.getParameterMap().keySet().toArray()) {

            System.out.println(key.toString());
        }
    }

    @Override
    public void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException,
            IOException {
request.setCharacterEncoding("utf-8");
response.setCharacterEncoding("utf-8");
        int uId = -1;
        String userId = "";
        String baseUrl = request.getScheme() + "://" + request.getServerName() + ":" + request.getServerPort() + request.getContextPath() + request.getServletPath();
        HashMap pp = new HashMap();
        if (request.getParameter("update_data") != null && request.getParameter("name") != null) {
            String tname = Tools.getStringValue(request.getParameter("name"), "", "");
            Connection conn = null;
            try {
                System.out.println("UPDATE IS STARTED>>>>>>" + tname);
                conn = DBTools.openSQLConnection(driverName, connectionString);
                List<HashMap> updates = getUpdates(tname, conn);
                for (HashMap hm : updates) {
                    String uTname = Tools.getStringValue(hm.get("TName"), "", "");
                    String uSql = Tools.getStringValue(hm.get("Sql"), "", "");
                    int upd_rows = 0;
                    if (uTname.length() > 0 && uSql.length() > 0) {
                        String sSQL = "DELETE FROM " + uTname + "; "
                                + "INSERT INTO " + uTname + "\n"
                                + "SELECT T1.* FROM (" + uSql + ") T1;";
                        upd_rows = DBTools.executeStatement(sSQL, conn);
                    }

                    System.out.println(uTname + " -------- " + upd_rows + " rows updated");
                    pp.put("UPDATE_FINISHED", 1);
                    System.out.println("<<<<UPDATE IS FINISHED");
                }
            } catch (Exception e) {
                e.printStackTrace();
                // TODO
            }

        }
        if (request.getParameter("postlogin") != null) {

            Date date = Calendar.getInstance().getTime();
            String cur_date = Tools.getFormatedDate(date, "MM.dd.yy");
            String cur_time = Tools.getFormatedDate(date, "HH:mm:ss");
            String c_date_time = cur_date + cur_time;
            String ip = request.getRemoteAddr();
            String login = Tools.getStringValue(request.getParameter("userlogin"), "");
            String password = Tools.getStringValue(request.getParameter("userpass"), "");
            String sSQL = "SELECT ID, USER_ID, PASSWORD, IP FROM USERS  WHERE NAME = ? AND PASSWORD = ?";
            ArrayList sqlParams = new ArrayList();
            sqlParams.add(login);
            sqlParams.add(password);
            Connection conn = null;
            try {
                conn = DBTools.openSQLConnection(driverName, connectionString);
                HashMap user = DBSelect.getRow(sSQL, sqlParams, conn);
                uId = Tools.parseInt(user.get("ID"), -1);
                userId = Tools.getStringValue(user.get("USER_ID"), "");
                if (uId > 0) {
                    logUser(uId, request, conn);
                    System.out.println("user " + login + " is logged");
                    Cookie cookie = new Cookie("userid", md5(login + password, false));
                    cookie.setPath("/");
                    response.addCookie(cookie);

                } else {
                    Cookie cookie = new Cookie("userid", "");
                    cookie.setPath("/");
                    cookie.setMaxAge(0);
                    response.addCookie(cookie);

                }
                pp.put("UID", uId);
                pp.put("USER_ID", userId);

            } catch (Exception e) {
                // TODO
                e.printStackTrace();
            }

        }
        if (request.getParameter("postlogout") != null) {
            Cookie cookie = new Cookie("userid", "");
            System.out.println("user is logged out");

            cookie.setPath("/");
            cookie.setMaxAge(0);
            response.addCookie(cookie);
            if (request.getServerName().contains("iss.jinr.ru")) {
               // response.sendRedirect("/logout");
            } else {
                //28.10
            }
            //
            response.sendRedirect("iss");
            return;
        }
        
        int muid = auth(request);
        if(request.getParameter("USER_MESSAGE") != null && muid >0){
            try {
                Connection conn = DBTools.openSQLConnection(driverName, connectionString);
                HashMap hm = new HashMap();
                hm.put("USER_ID", muid);
                hm.put("MESSAGE", request.getParameter("USER_MESSAGE"));
                hm.put("DATE_INSERT", new Date());
                DBTools.insertRow("user_feedback", hm, conn);
                DBTools.closeSQLConnection(conn);
            } catch (Exception ex) {
                Logger.getLogger(mainServlet.class.getName()).log(Level.SEVERE, null, ex);
            }
         
        }
        process(request, response, pp);
    }

    private String md5(String in, boolean caseSensitive) throws Exception {
        if (!caseSensitive) {
            in = in.toLowerCase();
        }
        // return Base64Coder.encodeString(in);
        ///            BASE64Encoder enc = new BASE64Encoder();
        ///            return  enc.encode(in.getBytes());

        MessageDigest md = MessageDigest.getInstance("MD5");
        md.update(in.getBytes("UTF-8"));
        byte[] digest = md.digest();

        StringBuffer sb = new StringBuffer(32);
        for (int i = 0; i < digest.length; i++) {
            byte b = digest[i];
            sb.append(String.format("%02x", b));
        }
        String md5 = sb.toString();
        return md5;
    }

    private HashMap getUserRights(int uId, Connection conn) throws SQLException {
        HashMap res = new HashMap();
        ArrayList sp = new ArrayList();
        String sSQL = "SELECT * FROM USER_RIGHTS UR\n"
                + "INNER JOIN USERS U ON U.RIGHTS_GROUP = UR.GROUP_CODE\n"
                + "WHERE U.ID = ?";
        sp.add(uId);
        res = DBSelect.getRow(sSQL, sp, conn);
        return res;
    }

    private List<HashMap> getUpdates(String qtable, Connection conn) throws SQLException {
        ArrayList params = new ArrayList();
        params.add(qtable);
        String sSQL = "SELECT * FROM UPDATES WHERE UPPER(QTABLE) = UPPER(?) ORDER BY UPD_ID";
        return DBSelect.getRows(sSQL, params, conn);
    }
    private void oauthUserInfo(HttpServletRequest request, HttpServletResponse response) throws Exception{
                HttpSession sess = request.getSession();
                if(sess.getAttribute(OAUTH2_TOKEN_KEY)!=null && sess.getAttribute(OAUTH2_TOKEN_EXPIRE_KEY)!=null){
                    String token = (String)userSession.getAttribute(OAUTH2_TOKEN_KEY);
                    Date expire = (Date) userSession.getAttribute(OAUTH2_TOKEN_EXPIRE_KEY);
                    String url = "http://login.jinr.ru/cgi-bin/infojson?access_token="+token;
                    URL obj = new URL(url);
                    HttpURLConnection con = (HttpURLConnection) obj.openConnection();
                    con.setRequestMethod("GET");
                    StringBuilder result = new StringBuilder();
                    BufferedReader rd = new BufferedReader(new InputStreamReader(con.getInputStream()));
                    String line;
                    while ((line = rd.readLine()) != null) {
                       result.append(line);
                    }
                    rd.close();
                    JSONParser jp = new JSONParser();
                    JSONObject userinfo = (JSONObject) jp.parse(result.toString());
                    sess.setAttribute("USER_INFO", userinfo);
                 
                }
                
    }
    private void process(HttpServletRequest request, HttpServletResponse response, HashMap pp) throws ServletException,
            IOException {
        userSession = request.getSession();
        
        //OAUTH
            try {
                oauthUserInfo(request, response);
                if(userSession.getAttribute("USER_INFO")!=null && request.getServletPath().equals("/oauth")){
                    
                if (request.getServerName().indexOf("iss.jinr.ru") > -1) {
                   response.sendRedirect("/iss");
                } else {
                    response.sendRedirect("iss");
                }    
                    
                
                return;
                }
            } catch (Exception ex) {
               ex.printStackTrace();
            }
        
        userSession.setAttribute("EXCEPTION_LIST", new ArrayList<String>());
        HashMap jspData = new HashMap();
        int uId = Tools.parseInt(pp.get("UID"), -1);
        if (uId < 0) {
            uId = auth(request);
        }

        Connection conn = null;

        try {
            conn = DBTools.openSQLConnection(driverName, connectionString);

            if (uId < 0) {
                RequestDispatcher dispatcher = request.getRequestDispatcher("/mainpage.jsp" + (request.getParameter("postlogin") != null ? "?wrong_auth=1" : ""));
                dispatcher.forward(request, response);
                return;
            } else {
                HashMap userrights = new HashMap();
                userrights = getUserRights(uId, conn);

                jspData.put("USER_NAME", getUserName(uId, conn));

                String baseUrl = request.getRequestURL().toString();
                if (request.getServerName().indexOf("iss.jinr.ru") > -1) {
                    baseUrl = baseUrl.replaceAll("/iss", "");
                }

                String queryString = Tools.getStringValue(request.getQueryString(), "");

                jspData.putAll(pp);
                queryString = Tools.repru(queryString);
                jspData.put("SERVLET_BASE_URL", baseUrl);
                jspData.put("SERVLET_QUERY_STRING", queryString);
                HashMap rParams = getRequestParams(request);
                String name = "";
                HashMap naviPath = new HashMap();

                int actionId = getAction(rParams);

                switch (actionId) {
                    case CommonData.ACTION_ENTER: //enter
                        jspData.put("MENU", getMenu(uId, conn));
                        jspData.put("SUBMENU", getSubMenu(uId, (String) rParams.get("id"), conn));
                        break;
                    case CommonData.ACTION_MENU: //menu
                        name = (String) rParams.get("name");
                        name = getQueryName(uId, name, conn);

                        List<HashMap> updates = new ArrayList<HashMap>();

                        updates = getUpdates(name, conn);
                        jspData.put("UPDATES", updates);

                        naviPath = getNavigationByQuery(uId, name, conn);
                        jspData.put("NAVI_PATH", naviPath);
                        List<HashMap> querys = new ArrayList<HashMap>();
                        if (name != null && name.length() > 0) {
                            querys = getQueryList(name, conn);
                            if (querys != null && querys.size() > 0) {
                                jspData.put("QUERY_LIST", querys);
                            }
                            jspData.put("TNAME", name);
                        }

                        jspData.put("MENU", getMenu(uId, conn));
                        jspData.put("SUBMENU", getSubMenu(uId, Tools.getStringValue(naviPath.get("SUB_ID"), ""), conn));
                        break;
                    case CommonData.ACTION_QUERY: //query
                        boolean desc = ("DESC".equals(Tools.getStringValue(rParams.get("table_sort"), "", ""))) ? true : false;
                        boolean hasR0 = (rParams.get("r0") != null) ? true : false;
                        if (desc) {
                            jspData.put("TABLE_SORT", "DESC");
                        }

                        jspData.put("MENU", getMenu(uId, conn));
                        String tname = Tools.getStringValue(rParams.get("tname"), "");
                        jspData.put("TNAME", tname);
                        String qname = Tools.getStringValue(rParams.get("qname"), "");
                        HashMap graphic = getGraphic(tname, qname, conn);
                        if (graphic.size() > 0) {
                            jspData.put("HAS_GRAPHIC", 1);
                        }
                        name = getQueryName(uId, tname, conn);
                        naviPath = getNavigationByQuery(uId, name, conn);
                        jspData.put("NAVI_PATH", naviPath);

                        int page = Tools.parseInt(rParams.get("page"), 1);
                        int itemsonpage = Tools.parseInt(rParams.get("itemsonpage"), DEF_ITEMS_ON_PAGE);
                        List<HashMap> filters = getFilters4query(tname, qname, conn);

                        List<HashMap> filtersTemp = filters;
                        for (int kk = 0; kk < filters.size(); kk++) { //выпиливаем отсюда фильтры, которые задаются жестко в userrights
                            HashMap hm = filters.get(kk);
                            String fNameParam = Tools.getStringValue(hm.get("NameParam"), "");
                            if (userrights.get(fNameParam) != null && Tools.getStringValue(userrights.get(fNameParam), "", "").length() > 0) {
                                filtersTemp.remove(hm);
                            }
                        }
                        filters = filtersTemp;
                        HashMap fdata = new HashMap(); //если фильтр имеет связанную таблицу со значениями, то создаем HashMap с ними
                        for (int i = 0; i < filters.size(); i++) {
                            HashMap filter = filters.get(i);
                            String fNameParam = Tools.getStringValue(filter.get("NameParam"), "");
                            String fNameTable = Tools.getStringValue(filter.get("NameTable"), "");
                            String fCode = Tools.getStringValue(filter.get("Code"), "");
                            String fName = Tools.getStringValue(filter.get("Name"), "");
                            if (fNameTable.length() > 0 && fCode.length() > 0 && fName.length() > 0) {
                                List<HashMap> filterData = getFilterData(fNameTable, fCode, fName, conn);
                                fdata.put(fNameParam, filterData);
                            }
                        }

                        jspData.put("FILTERS", filters);
                        jspData.put("FILTER_DATA", fdata);
                        HashMap qdata = getQueryData(tname, qname, conn);
                        String sql = Tools.getStringValue(qdata.get("SQL_Text"), "");
                        String queryTitle = Tools.getStringValue(qdata.get("QUERY"), "");

                        naviPath.put("QUERY_TITLE", queryTitle); //для навигации
                        jspData.put("QUERY_TITLE", queryTitle); //для вывода в заголовке результатов
                        String[] sqls = sql.split("(?im)\\sGO\\s");
                        System.out.println("SQLS COUNT:" + sqls.length);
                        HashMap hm = getPSQueryAndParams(sqls[0], rParams, userrights);
                        userSession.setAttribute("MAIN_SQL", sqls[0]);
                        if (sqls.length > 1) {
                            userSession.setAttribute("TOTALSQL", sqls[1]);
                        }
                        List sqlparams = (List) hm.get("SQLPARAMS");
                        String mainSQL = Tools.getStringValue(hm.get("SQL"), "");
                        List<String> colnames4filters = new ArrayList<String>();
                        try {
                            colnames4filters = DBSelect.getRowNames4SQL(mainSQL, sqlparams, conn);
                            jspData.put("COLUMN_NAMES", colnames4filters);
                        } catch (SQLException e) {
                            // TODO
                            addException(e);
                            e.printStackTrace();
                        }
                        String sortby = Tools.getStringValue(rParams.get("tables_sortby"), "");

                        if (!colnames4filters.contains(sortby)) {
                            sortby = "";
                        }

                        jspData.put("TABLE_SORTBY", sortby);

                        List<HashMap> qres = runQuery(sqls[0], rParams, conn, desc, sortby, userrights);
                        List<HashMap> tres = null;
                        jspData.put("IS_QUERY", 1);
                        //TODO: List<String> colnames4filters = DBSelect.getRowNames4SQL(sqls[0],rParams,conn);
                        // 2ой sql, итого
                        if (sqls.length > 1) {
                            tres = runQuery(sqls[1], rParams, conn, false, null, userrights);
                            System.out.println("TOTALS:> " + sqls[1]);
                            if (tres != null && tres.size() > 0) {
                                jspData.put("TOTALS", tres);
                            }
                        }

                        if (Tools.getStringValue(rParams.get("downloadXLS"), "", "").length() > 0) {

                            try {
                                //EXCEL 
                                response.setContentType("application/vnd.ms-excel");
                                long mills = Calendar.getInstance().getTimeInMillis();
                                response.setHeader("Content-Disposition", "attachment; filename=ISSExport" + mills + ".xls");

                                ExcelBook eb = new ExcelBook(response.getOutputStream());

                                eb.createSheet("ISSExport" + mills);
                                eb.setCellValue(0, 0, "Дата: " + Tools.getFormatedDate(new Date(), "dd.MM.yyyy HH:mm"), true);
                                ArrayList<String> filterFields = new ArrayList<String>();
                                filterFields.add("NameParam");
                                filterFields.add("~");
                                ArrayList<String> colNames = new ArrayList<String>();
                                colNames.add("Фильтр");
                                colNames.add("Значение");
                                eb.drawTable(filtersAndRequestValues(filters, rParams), filterFields, colNames, true, 1, 0);

                                eb.setCellValue(1, filters.size() + 2, Tools.Html2Text(queryTitle), true);
                                eb.drawTable(qres, null, null, true, 1, filters.size() + 4);
                                if (tres != null && tres.size() > 0) {
                                    eb.drawTable(tres, null, null, false, 1, filters.size() + 4 + qres.size() + 1);
                                }

                                eb.writeBook();
                                //ExcelTools.doExcel(qres, response.getOutputStream());
                                return; 
                            } catch (Exception e) {
                                // TODO
                                e.printStackTrace();
                            }

                        }
                        if (Tools.getStringValue(rParams.get("downloadImg"), "", "").length() > 0 && graphic.size() > 0) {

                            try {
                                //PNG DIAGRAM
                                response.setContentType("image/png");
                                BufferedImage img = null;
                                String x_arr_name = Tools.getStringValue(graphic.get("x_arr"), "", "");
                                String y_arr_name = Tools.getStringValue(graphic.get("y_arr"), "", "");
                                String diagType = Tools.getStringValue(graphic.get("diag_type"), "", "");
                                List<HashMap> grData = new ArrayList();

                                for (HashMap rec : qres) {
                                    rec.put("x_arr", rec.get(x_arr_name));
                                    rec.put("y_arr", rec.get(y_arr_name));
                                    grData.add(rec);
                                }
                                if ("Hist".equals(diagType)) {
                                    int x = 10;
                                    int imgWidth = 1600;
                                    int imgHeight = 1200;
                                    System.out.println("image " + imgWidth + " x " + imgHeight);
                                    if (Tools.getStringValue(rParams.get("thumbnail"), "", "").length() > 0) {
                                        imgWidth = 150;
                                        imgHeight = 100;
                                    }
                                    img = HistogramChart.HistogramChart(grData, imgWidth, imgHeight, x_arr_name, y_arr_name, Tools.Html2Text(queryTitle));
                                    ImageIO.write(img, "png", response.getOutputStream());
                                }

                            } catch (Exception e) {
                                // TODO
                                e.printStackTrace();
                            }
                            return; 
                        }

                        int pageCount = getPageCount(qres.size(), itemsonpage);
                        qres = Tools.getItemsForPage(itemsonpage, page, qres);

                        jspData.put("Page", page);
                        jspData.put("itemsOnPage", itemsonpage);
                        jspData.put("PageCount", pageCount);
                        System.out.println("QUERY:> " + sqls[0]);

                        HashMap lnkparams = new HashMap();
                        List<HashMap> lres = getLinks(tname, qname, conn);
                        for (int i = 0; i < lres.size(); i++) {
                            HashMap link = lres.get(i);
                            String lnkField = Tools.getStringValue(link.get("Field"), "");
                            String lnkTname = Tools.getStringValue(link.get("TName"), "");
                            String lnkQname = Tools.getStringValue(link.get("QName"), "");
                            HashMap lnkQdata = getQueryData(lnkTname, lnkQname, conn);
                            String lnkSQL = Tools.getStringValue(lnkQdata.get("SQL_Text"), "");
                            List<String> lnkParams = getParamNamesFromSQL(lnkSQL);
                            lnkparams.put(lnkField, lnkParams);
                        }
                        jspData.put("LINK_PARAMS", lnkparams);

                        jspData.put("LINKS", List2HashMap(lres, "Field"));
                        if (qres.size() > 0) {
                            ArrayList<String> qheaders = new ArrayList<String>(qres.get(0).keySet());
                            jspData.put("TABLE_HEADERS", qheaders);
                        }

                        jspData.put("QUERY_RESULT", qres);
                        jspData.put("RPARAMS", rParams);

                        String subid = getSubmenuIdByQueryTableName(uId, tname, conn);
                        jspData.put("SUBMENU", getSubMenu(uId, subid, conn));

                        break;
                    default:
                        jspData.put("MENU", getMenu(uId, conn));
                        break;
                }
                jspData.put("ACTION_ID", actionId);
            }
        } catch (Exception e) {
            // TODO 
            e.printStackTrace();
            jspData.put("SevereException", e.getMessage());
        } finally {

            DBTools.closeSQLConnection(conn);
        }

        //jspData.put("SERVLET_EXCEPTION", this.servletExceptions);
        jspData.put("UID", uId);

        DBTools.closeSQLConnection(conn);

        request.setAttribute("JspData", jspData);
        HttpSession session = request.getSession();
        RequestDispatcher dispatcher = request.getRequestDispatcher("/mainpage.jsp");
        dispatcher.forward(request, response);

    }

    @Override
    public void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException,
            IOException {
        request.setCharacterEncoding("utf-8");
        response.setCharacterEncoding("utf-8");
        if (request.getServletPath().endsWith("refreshusers")) {
            try {
                setUserList();
            } catch (Exception e) {
                this.addException(e);
            }
        }
        if (request.getServletPath().endsWith("oauth")) {
            try {
                oAuth(request);
            } catch (Exception e) {
                this.addException(e);
            }
        }        
        listRequestParams(request);
        HashMap pp = new HashMap();
        process(request, response, pp);
    }

    private List<HashMap> getFilterData(String fNameTable, String fCode,
            String fName, Connection conn) {
        ArrayList sqlparams = new ArrayList();
        String sSQL = "SELECT DISTINCT " + fCode
                + " as CODE, " + fName
                + " as NAME from " + fNameTable
                + " ORDER BY NAME";
        System.out.println("FILTER_DATA:> " + sSQL);

        List<HashMap> res = new ArrayList<HashMap>();
        try {

            res = DBSelect.getRows(sSQL, sqlparams, conn);
        } catch (SQLException e) {

            addException(e);
            e.printStackTrace();

        } finally {
            return res;
        }

    }

    private void setUserList() throws SQLException, Exception {
        if (driverName != null && connectionString != null && driverName.length() > 0 && connectionString.length() > 0) {
            Connection conn = null;
            conn = DBTools.openSQLConnection(driverName, connectionString);
            List<HashMap> tempUserList = DBSelect.getRows("SELECT ID, USER_ID, NAME, PASSWORD FROM USERS ", null, conn);
            HashMap tempUserListHashMap = new HashMap();
            for (HashMap hm : tempUserList) {
                String key = md5(Tools.getStringValue(hm.get("NAME"), "") + Tools.getStringValue(hm.get("PASSWORD"), ""), false);
                int uId = Tools.parseInt(hm.get("ID"), -1);
                tempUserListHashMap.put(key, uId);
            }
            if (tempUserListHashMap.size() > 0) {
                userList = new HashMap();
                userList.putAll(tempUserListHashMap);
            }
            DBTools.closeSQLConnection(conn);
        }
    }

    private void logUser(int uId, HttpServletRequest request,
            Connection conn) {
        try {
            String ipAddress = request.getRemoteAddr();
            String userName = getUserName(uId, conn);
            TimeZone tz = TimeZone.getTimeZone("Europe/Moscow");
            Calendar cal = Calendar.getInstance();
            cal.setTimeZone(tz);
            Date dateTime = cal.getTime();
            HashMap params = new HashMap();
            params.put("Date_Time", dateTime);
            params.put("User_ID", uId);
            params.put("User_Name", userName);
            params.put("Host_IP", ipAddress);
            params.put("pr", 1);

            DBTools.insertRow("LOGGS", params, conn);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
private void oAuth(HttpServletRequest request)throws Exception {
    //private void oAuth(String code) 
        String code = request.getParameter("code");
        HashMap params = new HashMap();
        params.put("code", code);
        params.put("redirect_uri", "http://omega.jinr.ru:8084/myiss/iss");
        params.put("pass", "is172");
        params.put("client_id", "9");
        String url = "http://login.jinr.ru/cgi-bin/token";
        URL obj = new URL(url);
        HttpURLConnection con = (HttpURLConnection) obj.openConnection();
        con.setRequestMethod("POST");
        con.setRequestProperty("Content-type", "application/x-www-form-urlencoded");
        String urlParameters = getUrlencodedParams(params);
        con.setDoOutput(true);
        DataOutputStream wr = new DataOutputStream(con.getOutputStream());
        wr.writeBytes(urlParameters);
        wr.flush();
        wr.close();
        int responseCode = con.getResponseCode();
        System.out.println("\nSending 'POST' request to URL : " + url);
        System.out.println("Post parameters : " + urlParameters);
        System.out.println("Response Code : " + responseCode);
        if (responseCode == 200) {
            BufferedReader in = new BufferedReader(new InputStreamReader(con.getInputStream()));
            String inputLine;
            StringBuffer response = new StringBuffer();
            while ((inputLine = in.readLine()) != null) {
                response.append(inputLine);
            }
            in.close();
            String resp = response.toString();
            if (!resp.isEmpty() && resp.startsWith("{") && resp.contains("access_token") ) {
                JSONParser jp = new JSONParser();
                System.out.println(response.toString());
                JSONObject o = (JSONObject) jp.parse(response.toString());
                String accessToken = (String) o.get("access_token");
                String expiresIn = (String) o.get("expires_in");
                Date tokenExpiresIn = (new SimpleDateFormat("yyyy-MM-dd kk:mm:ss")).parse(expiresIn);
                TOKEN_EXPIRES_IN = tokenExpiresIn;
                ACCESS_TOKEN = accessToken;
                request.getSession().setAttribute(OAUTH2_TOKEN_KEY, accessToken);
                request.getSession().setAttribute(OAUTH2_TOKEN_EXPIRE_KEY, tokenExpiresIn);
                System.out.println(accessToken);
                System.out.println(expiresIn);
                
            } else {
                System.out.println(response.toString());
            }
        }
    }
    public static String OAUTH2_TOKEN_KEY = "___oauth2tockenkey";
    public static String OAUTH2_TOKEN_EXPIRE_KEY = "___oauth2tockenkeyexpire";
    public static Date TOKEN_EXPIRES_IN = null;
    public static String ACCESS_TOKEN = "";

    private String getUrlencodedParams(HashMap mp) throws UnsupportedEncodingException {
        Iterator it = mp.entrySet().iterator();
        StringBuilder sb = new StringBuilder();
        int c = 0;
        while (it.hasNext()) {
            Map.Entry pair = (Map.Entry) it.next();
            if (c > 0) {
                sb.append("&");
            }
            c++;
            sb.append(pair.getKey());
            sb.append("=");
            sb.append(URLEncoder.encode((String) pair.getValue(), "UTF-8"));
            it.remove();
        }
        return sb.toString();
    }

    
}


/*DecodeDate(Date,year,mon,day);
 DecodeTime(Time,hou,min,sec,msec);
 cur_date:=IntToStr(mon)+'.'+IntToStr(day)+'.'+(IntToStr(year))[3]+(IntToStr(year))[4];
 cur_time:=IntToStr(hou)+':'+IntToStr(min)+':'+IntToStr(sec);
 c_date_time:=cur_date+' '+cur_time;
 host_ip:=Request.RemoteAddr;

 user_name:=Request.ContentFields.Values['name'];
 password:=Request.ContentFields.Values['pass'];

 Query1.SQL.Clear;
 str:='select '+USERS_ID_FIELD+', '+USERS_PASSW_FIELD+', IP';
 str:=str+' from '+USERS_TABLE;
 str:=str+' where '+USERS_NAME_FIELD+'='+#39+user_name+#39;
 Query1.SQL.Add(str);
 Query1.Open;
 Query1.First;
 if not Query1.EOF then
 begin
 hosts_allow:=Query1.FieldByName('IP').asString;
 user_id:=Query1.FieldByName(USERS_ID_FIELD).asString;
 user_passw:=Query1.FieldByName(USERS_PASSW_FIELD).asString;
 StrPCopy(Pass1,password);
 StrPCopy(Pass2,user_passw);
 if FindIP(host_IP,hosts_allow)=True then
 if StrComp(Pass1,Pass2)= 0 then
 begin
 Query1.sql.Clear;
 Query1.Sql.Add( 'select CurrUsers.User_Key'+
 ' from CurrUsers, Loggs'+
 ' where CurrUsers.User_ID=Loggs.User_ID'+
 ' and CurrUsers.User_Key=Loggs.User_Key'+
 ' and Loggs.User_ID='+#39+user_id+#39+
 ' and Loggs.Host_IP='+#39+host_ip+#39);
 Query1.Open;
 if Query1.RecordCount>0 then
 begin
 key:=Query1.Fields[0].asString;
 Query1.Close;
 Cookie:=Response.Cookies.Add;
 Cookie.Name:='Key';
 Cookie.Value:=key;
 Cookie.Secure:=False;
 end
 else
 begin
 //       +++++++++++++++ Adding Cookies ++++++++++++++++
 key:=KeyGen(32);
 Cookie:=Response.Cookies.Add;
 Cookie.Name:='Key';
 Cookie.Value:=key;
 Cookie.Secure:=False;

 //       ++++++++++++++ Update Current Users++++++++++++
 Query1.Close;
 Query1.Sql.Clear;
 str:='';
 str:=str+' INSERT INTO '+CURRUSER_TABLE+' Values( ';
 str:=str+#39+user_id+#39+', '+#39+key+#39+')';
 Query1.Sql.Add(str);
 Query1.ExecSQL;
 end;
 //       ++++++++++++++ Write Log +++++++++++++++++++++

 Query1.Sql.Clear;
 str:='';
 str:=str+' INSERT INTO '+LOGS_TABLE
 +' ('+LOGS_DATE_TIME_FIELD+', '
 +LOGS_USER_ID_FIELD+', '
 +LOGS_USER_NAME_FIELD+', '
 +LOGS_HOST_IP_FIELD+', '
 +LOGS_KEY_FIELD+' )'
 +' Values( '
 +#39+c_date_time+#39+', '
 +#39+user_id+#39+', '
 +#39+user_name+#39+', '
 +#39+host_ip+#39+' ,'
 +#39+key+#39+' )';
 Query1.Sql.Add(str);
 Query1.ExecSQL;

 Resp:='<HTML>'
 +'<Script Language=JavaScript>'
 +'parent.location="../query/main.html";'
 +'</Script>'
 +'</HTML>';

 //              Resp:=str;
 end
 else   Response.CustomHeaders.Add('Location=../query/error.html')
 else Response.CustomHeaders.Add('Location=../query/error.html');
 end
 else
 Response.CustomHeaders.Add('Location=../query/error.html');
 Query1.Close;
 end;
 Response.Content:=resp;
 end;
 */
