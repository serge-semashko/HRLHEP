package jinr.sed;

import dubna.walt.util.*;

import java.sql.*;
import java.util.Hashtable;
import javax.servlet.http.HttpServletRequest;

public class QueryThread extends dubna.walt.SimpleQueryThread {
// public DBUtil dbUtil = null;

    public boolean logIt = true;
//    private static final String excludeFromLog = ",svs/showInfoTooltip,sys/request_log,sys/viewRequest,sys/setErrFixed,free/checkSession_noDB,";
    private InputValidator iv = null;
    private int objectTypeId = 0;

    /**
     *
     *
     */
    @Override
    public void start() {
//                super.start();

        try {
            startTm = System.currentTimeMillis();
            rm.setParam("startTm", Long.toString(startTm));
            parseRequest(request);
            getAddresses();
         
            makeTuner();
//            checkSession();
            setContentType();
            if(cfgTuner.enabledExpression("Host=NULL&!ClientIP=159.93.33.24"))
                ((Logger) rm.getObject("logger")).logRequest2DB(rm, "Host=NULL", null);
            dbUtil = makeDBUtil();
            //    rm.setObject("cfgTuner", cfgTuner);
            if (cfgTuner.enabledOption("ResetLog=true")) {
                IOUtil.clearLogFile(rm);
                rm.removeKey("InputValidator");
                iv = null;
            }
            getSysConst();
            excludeFromLog = rm.getString("excludeFromLog", false, "");
            if (cfgTuner.getParameter("c").contains("showLog")) {
                rm.setParam("log", "false");
            }
//       writeHttpHeaders();
            logQuery();
            if (validateUser()) {
//                logQuery();
            } else {
                rm.println("! " + rm.getString("queryLabel")
                        + " [" + Fmt.fullDateStr(new java.util.Date()) + "] "
                        + cfgTuner.getParameter("ClientIP")
                        + " NOT LOGGED, "
                );
                cfgTuner.outCustomSection("not identified", outWriter);
            }
            validateParameters();
            startService();
            logQuery(null);
        } catch (Exception e) {
            logException(e);
            logQuery(e);
        } finally {
            if (outWriter != null) {
                outWriter.flush();
                outWriter.close();
                try {
                    outStream.close();
                    //         inpStream.close();
                } catch (Exception e) {
                    /* don't warry! */ }
            }
            finish();
            logRequestFinished();
        }
//        startTm = System.currentTimeMillis() - startTm;
//        if (log && !cfgTuner.getParameter("c").contains("showLog")) {
//            rm.setParam("log", "true");
//            if (!excludeFromLog.contains(cfgTuner.getParameter("c") + ',')) {
//                IOUtil.writeLogLn("<hr><span style='border:solid 1px green; font-weight:bold; background-color:#FFFFA0;'>REQUEST "
//                        + rm.getString("queryLabel") + " (" + cfgTuner.getParameter("c") + ") DONE! (" + startTm + "ms)</span> ", rm);
//            }
//        }
    }

    //            Server server = ServerFactory.getServer();
        /*  Вытаскиваем из Томката адреса. Пока не используется. Может оказаться полезным */
/*        
import java.net.InetAddress;
import org.apache.catalina.Server;
import org.apache.catalina.Service;
import org.apache.catalina.connector.Connector;
import org.apache.coyote.ProtocolHandler;
import org.apache.coyote.http11.Http11AprProtocol;
import org.apache.coyote.http11.Http11NioProtocol;
import org.apache.coyote.http11.Http11Protocol;
import javax.management.*;

        MBeanServer mBeanServer = MBeanServerFactory.findMBeanServer(null).get(0);
        ObjectName name = new ObjectName("Catalina", "type", "Server");
        Server server = (Server) mBeanServer.getAttribute(name, "managedResource");            
        Service[] services = server.findServices();
        for (Service service : services) {
            for (Connector connector : service.findConnectors()) {
                ProtocolHandler protocolHandler = connector.getProtocolHandler();
                if (protocolHandler instanceof Http11Protocol
                    || protocolHandler instanceof Http11AprProtocol
                    || protocolHandler instanceof Http11NioProtocol) {
                    int serverPort = connector.getPort();
                    System.out.println("server.getAddress: " + server.getAddress()
                            + "; InetAddress.getHostAddress: " +InetAddress.getLocalHost().getHostAddress()
                            + "; getCanonicalHostName: " +InetAddress.getLocalHost().getCanonicalHostName()
                            + "; getAddress: " +InetAddress.getLocalHost().getAddress()
//                                    .getHostName()
                    );
                    System.out.println("HTTP Port: " + connector.getPort()
                    + "; getRedirectPort:" + connector.getRedirectPort()
                    + "; getLocalPort:" + connector.getLocalPort()
//                    + "; getProxyPort:" + connector.getProxyPort()
                    + "; getProtocol:" + connector.getProtocol()
                    + "; getScheme:" + connector.getScheme()
                    + "; getDomain:" + connector.getDomain()
                    + "; getAttribute:" + connector.getAttribute("host")
                    );
                    
                }
            }
        }
*/
    
    /**
     *
     * @param request
     * @throws Exception
     */
    @Override
    public void parseRequest(HttpServletRequest request) throws Exception {
        if(iv == null)
            iv = (InputValidator) rm.getObject("InputValidator", false);
        if(iv == null) {
            iv = new InputValidator(rm.getGlobalRM());
            rm.getGlobalRM().putObject("InputValidator", iv);
        }
//        object_type_id
        String[] sa = request.getParameterValues("object_type_id");        
        objectTypeId = 0;
        if(sa != null && sa.length == 1) {
            try {
            objectTypeId = Integer.parseInt(sa[0]);
            } catch (Exception e) {;}
        }
        super.parseRequest(request);
    }

    @Override
    public void registerParameter(String name, String val, Hashtable ht) {
        String v = iv.getValidatedValue(name, val, objectTypeId);
//        IOUtil.writeLogLn("+++++ param: " + name + "=" + val + "; =>" + v, rm);
        if(v == null) {
            String paramErr = iv.getErrMsg(name, val, objectTypeId);
            IOUtil.writeLogLn("+++++ ERROR: param: " + name + "=" + val + "; =>" + v + "; " + paramErr + "; objectTypeId=" + objectTypeId, rm);            
            super.registerParameter("INPUT_ERROR", paramErr + " ", ht);
            super.registerParameter("ERROR_" + name, paramErr, ht);
            super.registerParameter(name + "_orig", val, ht);
            System.out.println("ERROR_" + name + "='" + paramErr + "'; " + name + "_orig='" + val +"'");
        }
        else if(v.length() > 0)
            super.registerParameter(name, v, ht);
    }

    /**/
    private void getSysConst() throws Exception {
        if (dbUtil == null) {
            makeDBUtil();
        }
        if (dbUtil == null) {
            return;
        }
        if (!rm.getBoolean("const_inited")) {
            String sql = "select alias, value from sys_const";
            IOUtil.writeLogLn("===== get System Const SQL:" + sql, rm);
            ResultSet r = dbUtil.getResults(sql);
            while (r.next()) {
                rm.setParam(r.getString(1), r.getString(2), true);
                System.out.println("- " + r.getString(1) +":" + r.getString(2));
            }
            dbUtil.closeResultSet(r);
            rm.setParam("const_inited", "true", true);
        }
    }

    @Override
    public boolean validateUser() throws Exception {
        if (!logIt) {
            return true;
        }
        return super.validateUser();
    }

    /**
     * Вывод в консоль Томката информации о запросе
     */ 
    @Override
    public void logQuery() {
//        if (logIt) 
        {
            System.out.println(rm.getString("queryLabel")
//                    + " [" + Fmt.fullDateStr(new java.util.Date()) + "] "
//                    + cfgTuner.getParameter("ClientIP") 
                    + ": " + cfgTuner.getParameter("USER_ID")
//                    + ": " + cfgTuner.getParameter("c") 
                    + "; " + cfgTuner.getParameter("queryString")
            );
//            System.out.println(". " + rm.getString("queryLabel") + ": " + cfgTuner.getParameter("queryString"));
        }
    }

    /**
     * Запись в лог в БД основных параметров http-запроса
     *
     * @param e зафиксированная exception при выполнения запроса Если exception
     * не возник, то регистрируется параметр ERROR из Tuner
     */
    @Override
    public void logQuery(Exception e) {
        String c = cfgTuner.getParameter("c");
//        System.out.println("+++ logQuery +++ c=" + c + "; logIt=" + logIt + "; e=" + e);
        if (!logIt) {
            return;
        }
//        svs/info_show_dd,svs/showWfStepTooltip,free/checkSession_noDB,sys/showLog_noDB,
//        ,wf/show_wf_for_doc,wf/graph_show_wf_for_doc,sys/checkAdminRights,wf/show_wf_status,docs/out_files_list,docs/doc_files_list,docs/doc_field_file,sys/request_log,
        if (excludeFromLog.contains("," + c + ",")) 
            return;
        Logger logger = (Logger) rm.getObject("logger");
        logger.logRequest2DB(rm, cfgTuner.getParameter("ERROR"), e);
        
        
        /*
        dbUtil = (DBUtil) rm.getObject("DBUtil", false);
        if (dbUtil == null || !dbUtil.isAlive()) {
            try {
                dbUtil = makeDBUtil();
            } catch (Exception ex) {
                System.out.println("+++ logQuery: ERROR! " + ex.toString());
                ex.printStackTrace(System.out);
            }
        }
        if (dbUtil == null) {
            System.out.println("+++ logQuery: ERROR! dbUtil is null!");
        } else if (cfgTuner == null) {
            System.out.println("+++ logQuery: ERROR! cfgTuner is null!");
        } else {
            try {
                String err = trimString(cfgTuner.getParameter("ERROR"), 1900);
//	    System.out.println("+++ logQuery +++ err=" + err + "; ");
                String request_name = trimString(cfgTuner.getParameter("request_name"), 64);

                    Connection conn = dbUtil.getConnection();
                    conn.setAutoCommit(true);
                    String s = "insert into a_req_log (USER_ID, C, REQUEST_NAME, QUERY, DOC_ID, COOKIES, ERR, DAT, IP, USER_AGENT, REF, SESS_ID, SESS, DID, TIM, REAL_USER_ID) values (?, ?, ?, ?, ?, ?, ?, NOW(), ?, ?, ?, ?, ?, ?, ?, ?)";
                    PreparedStatement stmt = conn.prepareStatement(s);
                    stmt.setInt(1, cfgTuner.getIntParameter(null, "USER_ID", 0));
                    stmt.setString(2, c);
                    stmt.setString(3, request_name);
                    stmt.setString(4, trimString(cfgTuner.getParameter("queryString"), 2047));
                    stmt.setInt(5, cfgTuner.getIntParameter(null, "doc_id", 0));
                    stmt.setString(6, trimString(cfgTuner.getParameter("h_cookie"), 2047));
                    if (e == null) {
                        stmt.setString(7, err);
                    } else {
                        stmt.setString(7, e.toString() + " / " + err);
                    }
                    stmt.setString(8, cfgTuner.getParameter("ClientIP"));
                    stmt.setString(9, cfgTuner.getParameter("h_user-agent"));
                    stmt.setString(10, cfgTuner.getParameter("h_referer"));
                    stmt.setInt(11, cfgTuner.getIntParameter(null, "SESS_ID", 0));
                    stmt.setString(12, cfgTuner.getParameter("q_JSESSIONID"));
                    stmt.setString(13, cfgTuner.getParameter("q_cwldid"));
                    long l = System.currentTimeMillis() - startTm;
                    stmt.setInt(14, (int) l);
                     stmt.setInt(15, cfgTuner.getIntParameter(null, "VU", 0));
                    stmt.executeUpdate();

            } catch (Exception ex) {
                System.out.println("+++ logQuery: STORE ERROR! " + ex.toString());
                ex.printStackTrace(System.out);
            }
        }
        /**/
    }

       /**
     *
     */
    public void logAll() {
        super.logAll();
    
        if (outWriter != null) {
            if(iv == null)
                iv = (InputValidator) rm.getObject("InputValidator", false);
            if(iv != null) {
                iv.log(outWriter);
            }
//    outWriter.flush();
        }
    }
    
//   private String trimString(String s, int maxLen) {
//        return s.substring(0, Math.min(s.length(), maxLen));
//    }

    /**
     * Коннект к БД в начале выполнения запроса Коннект не делается для
     * cfg-файлов, содержащих "_noDB" в имени файла или в пути к нему
     *
     * @return объект DBUtil, который далее будет использоваться для обращения к
     * БД.
     * @throws Exception
     */
    @Override
    public synchronized DBUtil makeDBUtil() throws Exception {
        if (cfgFileName == null || cfgFileName.contains("_noDB")) {
            logIt = false;
            return null;
        }
        if (cfgFileName.contains("free/")
                || cfgFileName.contains("svs/")) {
            logIt = false;
        }

        if (!cfgTuner.enabledOption("connString")) {
            return null;
        }
        try {
            IOUtil.writeLog(5, "<br><i>connect: " + cfgTuner.getParameter("connString")
                        + cfgTuner.getParameter("database") + cfgTuner.getParameter("connParam") + "</i>...", rm);
            /* Establish connection to the database and make DBUtil */
//            if (cfgTuner.enabledOption("debug=on")) 
//            {
//                System.out.println("  connect:" + cfgTuner.getParameter("connString")
//                        + cfgTuner.getParameter("database") + cfgTuner.getParameter("connParam")
//                        + " //|| " + cfgTuner.getParameter("usr") + "/*** "
//                //	+ cfgTuner.getParameter("pw")
//                );
//            }
            Connection conn = DriverManager.getConnection(cfgTuner.getParameter("connString")
                    + cfgTuner.getParameter("database")
                    + cfgTuner.getParameter("connParam"), cfgTuner.getParameter("usr"), cfgTuner.getParameter("pw"));
            conn.setAutoCommit(false);
            dbUtil = new DBUtil(conn, queryLabel);
            dbUtil.db = DBUtil.DB_MySQL;
            dbUtil.allocate();

//=============== SET DB Connection properties =====================
//	   Statement stmt = conn.createStatement();
//	   stmt.executeUpdate("set max_sp_recursion_depth=30");
//	   stmt.executeUpdate("SET NAMES cp1251");
//	   stmt.executeUpdate("set max_allowed_packet=5000000");
//	   conn.commit();
//	   stmt.close();
            rm.setObject("DBUtil", dbUtil, false);
            IOUtil.writeLog(5, " Connect OK! " + Long.toString(System.currentTimeMillis() - startTm) + "ms.", rm);
        } catch (Exception e) {
            IOUtil.writeLogLn(cfgTuner.getParameter("connString") + cfgTuner.getParameter("database") + cfgTuner.getParameter("connParam")
                        + " <b>Connect ERROR: </b>" + e.toString(), rm);
            System.out.println("Connection to " + cfgTuner.getParameter("connString") + " FAILED!..." + e.toString());
//		 e.printStackTrace(System.out);
            cfgTuner.addParameter("NotConnected", "Y");
            if (outWriter != null) {
                cfgTuner.addParameter("ERR_MSG", e.getMessage());
                cfgTuner.addParameter("ERR_MSG_DES",
                        "Could not connect to the Database " + cfgTuner.getParameter("connString"));
            }
            /*		 if (outWriter != null)
			 { outWriter.println("<small>" +  e.getMessage() + "</small>");
			 outWriter.println("<center><table border=1 bgcolor=#FFEEBB cellpadding=8><tr><th>"
			 + "Could not connect to the Database '" + cfgTuner.getParameter("connString") + "!</th></tr></table></center><p>");
			 outWriter.flush();
			 }
             */ e.printStackTrace(System.out);
            return null;
        }
        if (cfgTuner.enabledOption("debug=on")) {
            System.out.println(" Connect OK!");
        }
        return dbUtil;
    }

    /**
     * Finalizer. Закрытие коннектов к базе, запись в консоль Томката в режиме
     * "debug=on"
     */
    @Override
    protected void finish() {
        closeDBUtils();
        if (cfgTuner != null) {
            try {
                if (cfgTuner.enabledOption("debug=on")) {
                    System.out.println("\n" + "[" + Fmt.lsDateStr(new java.util.Date()) + "] "
                            + rm.getString("queryLabel") + ": --- finish()");
                }
            } catch (Exception e) {;
            }
        }
    }

    /**
     * Закрытие коннектов к базе, удаление DBUtils из ResourceManager rm Если
     * включены транзакции и были ошибки, то откат, иначе - коммит
     */
    protected void closeDBUtils() {
        if (dbUtil != null) {
            try {
                if (cfgTuner.enabledOption("ERROR")) {
                    dbUtil.update("ROLLBACK");
                } else {
                    dbUtil.update("COMMIT");
                }
            } catch (Exception e) {
            }
            dbUtil.release();
            dbUtil.close();
        }

        dbUtil = (DBUtil) rm.getObject("DBUtil", false);
        if (dbUtil != null) {
            dbUtil.release();
            dbUtil.close();
            rm.setObject("DBUtil", null);
        }
        dbUtil = null;
//  System.out.println("---------------- closeDBUtils() " + queryLabel);
    }

}
