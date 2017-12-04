/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package dubna.walt.util;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

/**
 *
 * @author serg
 */
public class Logger {

    private ResourceManager rm_global;
    private DBUtil dbUtil = null;

    public Logger(ResourceManager rm_global) {
        this.rm_global = rm_global;
        makeDBUtil();
    }

    public void logRequest2DB(ResourceManager rm, String msg, Exception ex) {
        makeDBUtil();
        try {
            Tuner cfgTuner = (Tuner) rm.getObject("cfgTuner");
//            System.out.println("-------");
//            System.out.println("  Logger.logRequest2DB(): msg=" + msg + "; cfgTuner=" + cfgTuner);
            String s = rm.getString("startTm");
            String sql;
            long l = 0;
            if (s.length() > 0) {
                l = System.currentTimeMillis() - Long.parseLong(s);
            }

            if (dbUtil != null && dbUtil.isAlive()) {
                if (cfgTuner != null) {
                    sql = "insert into a_req_log (USER_ID, REAL_USER_ID, REQUEST_TYPE, queryLabel"
                            + ", C, REQUEST_NAME, QUERY, DOC_ID, COOKIES, ERR"
                            + ", DAT, IP, USER_AGENT, REF, SESS_ID, SESS, DID, TIM)"
                            //                            
                            + " values (" + cfgTuner.getIntParameter(null, "USER_ID", 0)
                            + ", " + cfgTuner.getIntParameter(null, "VU", 0)
                            + ", '" + rm.getString("requestType", false, "?")
                            + "', '" + rm.getString("queryLabel", false, "?")
                            //                            
                            + "', '" + cfgTuner.getParameter("cfgFile")
                            + "', '" + cfgTuner.getParameter("request_name")
                            + "', '" + trimString(cfgTuner.getParameter("queryString"), 2047)
                            + "', " + cfgTuner.getIntParameter(null, "doc_id", 0)
                            + ", '" + trimString(cfgTuner.getParameter("h_cookie"), 2047)
                            + "', '" + ((ex == null) ? msg : ex.toString() + " / " + msg)
                            + "', NOW()"
                            + ", '" + cfgTuner.getParameter("ClientIP")
                            + "', '" + cfgTuner.getParameter("h_user-agent")
                            + "', '" + cfgTuner.getParameter("h_referer")
                            + "', " + cfgTuner.getIntParameter(null, "SESS_ID", 0)
                            + ", '" + cfgTuner.getParameter("q_JSESSIONID")
                            + "', '" + cfgTuner.getParameter("q_cwldid")
                            + "', " + Long.toString(l)
                            + ")";
                } else {
                    HttpServletRequest request = (HttpServletRequest) rm.getObject("request");
                    String user_id = "0";
                    String sess_id = "";
                    Object o;
                    HttpSession session = request.getSession();
                    System.out.print("***** ERROR: Logger- NO cfgTuner: session=" + session);
                    if (session != null) {
                        o = session.getAttribute("USER_ID");
                    System.out.print(" USER_ID=" + o);
                        if (o != null) {
                            user_id = o.toString();
                        }
                        o = session.getAttribute("JSESSIONID");
                    System.out.println(" JSESSIONID=" + o);
                        if (o != null) {
                            sess_id = o.toString();
                        }
                    }
                    user_id = (user_id.isEmpty()) ? "0" : user_id;
                    Cookie[] cookies = request.getCookies();
                    String n = "";
                    String v = "";
                    String q = "";
                    String vu = "null";
                    String dev_id = "";
                    if (cookies != null) {
                        for (int i = 0; i < cookies.length; i++) {
                            n = cookies[i].getName().trim();
                            v = StrUtil.unescape(cookies[i].getValue());
                            if (n.equals("VU")) {
                                vu = v;
                            } else if (n.equals("cwldid")) {
                                dev_id = v;
                            }
                            q += n + "=" + v + "; ";
                        }
                    }
                    String referer = "*";
                    if (request.getHeader("referer") != null) {
                        referer = StrUtil.replaceInString(
                                StrUtil.replaceInString(
                                        StrUtil.replaceInString(
                                                request.getHeader("referer"), "?", "%3F"), "&", "%26"), "=", "%3D");
                    }

                    sql = "insert into a_req_log (USER_ID, REAL_USER_ID, REQUEST_TYPE"
                            + ", C, QUERY, COOKIES, ERR"
                            + ", DAT, IP, USER_AGENT, REF, SESS, DID, TIM)"
                            //                            SESS_ID,
                            + " values (" + user_id
                            + ", " + vu
                            + ", '" + rm.getString("requestType")
                            //                            
                            + "', '" + rm.getString("cfgFileName")
                            + "', '" + trimString(request.toString(), 2047)
                            + "', '" + trimString(q, 2047)
                            + "', '" + ((ex == null) ? msg : ex.toString() + " / " + msg)
                            + "', NOW()"
                            + ", '" + rm.getString("clientIP")
                            + "', '*"
                            + "', '" + referer
                            //                            + "', " + cfgTuner.getIntParameter(null, "SESS_ID", 0) 
                            + "', '" + sess_id
                            + "', '" + dev_id
                            + "', " + Long.toString(l)
                            + ")";
                }
//                System.out.println("***** Logger.logRequest2DB() SQL:" + sql);
                dbUtil.update(sql);
            } else {
                System.out.println("***** ERROR: Logger.logRequest2DB() - NOT CONNECTED! ");
            }
        } catch (Exception e) {
            System.out.println("+++++++++++++++");
            System.out.println("***** ERROR: Logger.logRequest2DB()" + e.toString());
            System.out.println("+++++++++++++++");
            e.printStackTrace();
        }
    }
//insert into a_req_log (USER_ID, C, REQUEST_NAME, QUERY, DOC_ID, COOKIES, ERR, DAT, IP, USER_AGENT, REF, SESS_ID, SESS, DID, TIM, REAL_USER_ID) 
//    values (2309, JINR/reports/zajavka4dogovor.cfg, U:Список заявок, c=JINR/reports/zajavka4dogovor&ajax=Y, 0, JSESSIONID=11FAEC1F6495D9479576FA99B832F47F; doc_year=2016; smallScreen=; helperWindow=true; nomsg=; yrc=; curr_budget_table=; cwl4=; cwl=efdaee66e7f58f17f23ca5c970ff41b3%3A94968%3A2309%3Aserg%3A127.0.0.1%3A1499380124322; cwldid=1501240590.465; ; yr=17; VU=; cwlp=c53376befdeb225c45902415f1d6ea47%3A115149%3A2309%3Aserg%3A159.93.40.211%3A1503310157668; cwl8=5b8f0a6d97725b7ac3d60520080643a6%3A94987%3A2309%3Aserg%3A127.0.0.1%3A1503349080070; vu_id=, LOGGER, NOW(), 127.0.0.1, Mozilla/5.0 (X11; Linux x86_64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/57.0.2987.133 Safari/537.36, http://ak0211.jinr.ru:8084/sed/dubna?tm=1503400203445, 94987, 11FAEC1F6495D9479576FA99B832F47F, 1501240590.465, 146, 2309

    /**
     * Коннект к БД
     *
     * @return объект DBUtil, который далее будет использоваться для обращения к
     * БД.
     * @throws Exception
     */
    private synchronized void makeDBUtil() {
        if (dbUtil != null && dbUtil.isAlive()) {
            return;
        }
        try {
            /* Establish connection to the database and make DBUtil */
            System.out.println("  ..... Logger.makeDBUtil():" + rm_global.getString("connString", true)
                    + rm_global.getString("database", false, "") + rm_global.getString("connParam", false, "")
                    + " //|| " + rm_global.getString("usr") + "/*** "
                    + rm_global.getString("pw")
            );
            Connection conn = DriverManager.getConnection(rm_global.getString("connString")
                    + rm_global.getString("database", false, "")
                    + rm_global.getString("connParam", false, ""), rm_global.getString("usr"), rm_global.getString("pw"));
            conn.setAutoCommit(true);
            dbUtil = new DBUtil(conn, "Logger_" + rm_global.getString(""));
//            dbUtil.db = DBUtil.DB_MySQL;
            dbUtil.allocate();
        } catch (Exception e) {
            System.out.println("Logger: Connection FAILED!...");
            e.printStackTrace(System.out);
            dbUtil = null;
        }
        System.out.println(" Connect OK!");
    }

    /**
     * Finalizer. Закрытие коннектов к базе, запись в консоль Томката в режиме
     * "debug=on"
     */
    protected void finalize() {
        try {
            if (dbUtil != null) {
                dbUtil.release();
                dbUtil.close();
            }
            System.runFinalization();
            System.gc();
            super.finalize();
        } catch (Throwable tr) {
            tr.printStackTrace(System.out);
        }
    }

    private String trimString(String s, int maxLen) {
        return s.substring(0, Math.min(s.length(), maxLen));
    }
}
