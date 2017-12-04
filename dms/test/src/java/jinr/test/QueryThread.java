package jinr.test;

import dubna.walt.util.*;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;

import java.sql.*;
import java.util.Vector;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

public class QueryThread extends dubna.walt.SimpleQueryThread {

    public boolean logIt = true;
//    private static final String excludeFromLog = ",svs/showInfoTooltip,sys/request_log,sys/viewRequest,sys/setErrFixed,";
    private static final String openedModules = ",receive.cfg,";

    /**
     * 
     * @param rm
     * @throws Exception 
     */
        @Override        
        public void init(ResourceManager rm) throws Exception {
        request = (HttpServletRequest) rm.getObject("request");
        response = (HttpServletResponse) rm.getObject("response");
//  rm_Global = (ResourceManager) rm.getObject("rm_Global");
        this.rm = rm;
        this.queryLabel = rm.getString("queryLabel");
        serverEncoding = rm.getString("serverEncoding", false, "Cp1251");
        clientEncoding = rm.getString("clientEncoding", false, "Cp1251");
        rm.putObject("QueryThread", this);

        if (outWriter == null) {
            inpStream = request.getInputStream();
            outStream = response.getOutputStream();
//    outWriter = new PrintWriter(outStream);
            try {
                outWriter = new PrintWriter(new OutputStreamWriter(outStream, clientEncoding));
            } catch (Exception e) {
                rm.println("******* EXCEPTION:" + e.toString());
                outWriter = new PrintWriter(new OutputStreamWriter(outStream, clientEncoding));
            }
        }

        rm.setObject("outWriter", outWriter);
        rm.setObject("outStream", outStream);

        params = new Vector(40, 20);
    }
        
    /**
     *
     *
     */
    @Override
    public void start() {
        boolean log =  rm.getBoolean("log");
        try {
            startTm = System.currentTimeMillis();

            parseRequest(request);
            getAddresses();
            makeTuner();
            setContentType();
            dbUtil = makeDBUtil();

            //    rm.setObject("cfgTuner", cfgTuner);
            if (cfgTuner.enabledOption("ResetLog=true")) {
                IOUtil.clearLogFile(rm);
            }
            getSysConst();
            if (cfgTuner.getParameter("c").contains("showLog")) {
                rm.setParam("log", "false");
            }
//       writeHttpHeaders();
            if (validateUser()) {
                logQuery();
            } else {
                rm.println("\n"
                        + rm.getString("queryLabel")
                        + ": " + cfgTuner.getParameter("c")
                        + " NOT LOGGED, " + cfgTuner.getParameter("ClientIP")
                        + "; [" + Fmt.shortDateStr(new java.util.Date()) + "] "
                );
                cfgTuner.outCustomSection("not identified", outWriter);
            }
            validateParameters();
            
            if(log & !excludeFromLog.contains(cfgFileName + ',')) {
                long ttt = System.currentTimeMillis() - startTm;
                IOUtil.writeLogLn("<span style='border:solid 1px blue;' > REQUEST " 
                    + rm.getString("queryLabel") + " start time=" + ttt + "ms</span> ", rm);
            }
            
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
        }
        startTm = System.currentTimeMillis() - startTm;
        if (log && !cfgTuner.getParameter("c").contains("showLog") ) {
            rm.setParam("log", "true");
            if(!excludeFromLog.contains(cfgFileName + ',')) 
                IOUtil.writeLogLn("<hr><span style='border:solid 1px green; font-weight:bold; background-color:#FFFFA0;'>REQUEST " 
                    + rm.getString("queryLabel") + " (" + cfgTuner.getParameter("c") + ") DONE! (" + startTm + "ms)</span> ", rm);
        }
//        try {
//            Thread.sleep(5000);
//        } catch (InterruptedException ex) {
//            Logger.getLogger(QueryThread.class.getName()).log(Level.SEVERE, null, ex);
//        }

    }

    @Override
        public void setContentType() {
        of = cfgTuner.getParameter("of");
        //  if (of == "") of = "h";
        //  rm.println("......... Format:" + of);
        if (of.equalsIgnoreCase("bin")) {
            return;
        }
        if (of.equalsIgnoreCase("xl") || of.equals("xlh")) {
            response.setContentType("application/vnd.ms-excel");
            if (!cfgTuner.enabledOption("inline=true")) {
                response.setHeader("Content-Disposition", "attachment; filename=" + cfgTuner.getParameter("file_name"));
            }
        } else {
            String contentType = cfgTuner.getParameter("contentType");
            if (contentType.length() > 0) {
                response.setContentType(contentType);
            } else {
                response.setContentType("text/html; charset=" + serverEncoding);
            }
        }

        //  response.setHeader( "Last-modified", "Sun, 23 May 2004 11:10:40 GMT" );
        //    response.setContentType("application/msword");
        // application/x-zip-compressed    
    }
    
    /**/

    private void getSysConst() throws Exception {
        if (!rm.getBoolean("const_inited") && dbUtil != null) {
            String sql = "select alias, value from sys_const";
            IOUtil.writeLogLn("===== get System Const SQL:" + sql, rm);
            ResultSet r = dbUtil.getResults(sql);
            while (r.next()) {
                rm.setParam(r.getString(1), r.getString(2), true);
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
        if (openedModules.contains("," + cfgFileName + ",")) {
            return true;
        }
        return super.validateUser();
    }

    /**
     * Вывод в консоль Томката информации о запросе
     */
    @Override
    public void logQuery() {
        if (logIt) {
            System.out.println(
                    rm.getString("queryLabel")
                    + ": " + cfgTuner.getParameter("c")
                    + ": " + cfgTuner.getParameter("USER_ID")
                    //							+ " (" + cfgTuner.getParameter("USER_ROLE") 
                    + ") " + cfgTuner.getParameter("ClientIP")
                    + "; [" + Fmt.shortDateStr(new java.util.Date()) + "] "
            );
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
//	    System.out.println("+++ logQuery +++ c=" + cfgFileName + "; e=" + e);
        if (!logIt) {
            return;
        }
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
                String c = cfgTuner.getParameter("c");
                String err = trimString(cfgTuner.getParameter("ERROR"), 1900);
//	    System.out.println("+++ logQuery +++ err=" + err + "; ");
                String request_name = trimString(cfgTuner.getParameter("request_name"), 64);
                if (!excludeFromLog.contains("," + cfgFileName + ",")) {
                    Connection conn = dbUtil.getConnection();
                    conn.setAutoCommit(true);
                    String s = "insert into a_req_log (USER_ID, C, REQUEST_NAME, QUERY, COOKIES, ERR, DAT, IP, USER_AGENT, REF, SESS_ID, SESS, TIM) values (?, ?, ?, ?, ?, ?, NOW(), ?, ?, ?, ?, ?, ?)";
                    PreparedStatement stmt = conn.prepareStatement(s);
                    stmt.setInt(1, cfgTuner.getIntParameter(null, "USER_ID", 0));
                    stmt.setString(2, c);
                    stmt.setString(3, request_name);
                    stmt.setString(4, trimString(cfgTuner.getParameter("queryString"), 2047));
                    stmt.setString(5, trimString(cfgTuner.getParameter("h_cookie"), 2047));
                    if (e == null) {
                        stmt.setString(6, err);
                    } else {
                        stmt.setString(6, e.toString() + " / " + err);
                    }
                    stmt.setString(7, cfgTuner.getParameter("ClientIP"));
                    stmt.setString(8, cfgTuner.getParameter("h_user-agent"));
                    stmt.setString(9, cfgTuner.getParameter("h_referer"));
                    stmt.setInt(10, cfgTuner.getIntParameter(null, "SESS_ID", 0));
                    stmt.setString(11, cfgTuner.getParameter("q_JSESSIONID"));
                    long l = System.currentTimeMillis() - startTm;
                    stmt.setInt(12, (int) l);
                    stmt.executeUpdate();
                }
            } catch (Exception ex) {
                System.out.println("+++ logQuery: STORE ERROR! " + ex.toString());
                ex.printStackTrace(System.out);
            }
        }
    }

    private String trimString(String s, int maxLen) {
        return s.substring(0, Math.min(s.length(), maxLen));
    }

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
        if (cfgFileName.contains("_noDB")) {
            logIt = false;
//            return null;
        }
        if (cfgFileName.contains("free/")
                || cfgFileName.contains("svs/")) {
            logIt = false;
        }
        
        long tm = System.currentTimeMillis();
//        Class.forName(rm.getString("dbDriver"));        // init the JDBC driver
        try {
            /* Establish connection to the database and make DBUtil */
//            if (cfgTuner.enabledOption("debug=on")) 
            {
                System.out.println("  connect:" + cfgTuner.getParameter("connString")
                        + cfgTuner.getParameter("database") + cfgTuner.getParameter("connParam")
                        + " //|| " + cfgTuner.getParameter("usr") + "/*** "
                //	+ cfgTuner.getParameter("pw")
                );
            }
            Connection conn = DriverManager.getConnection(cfgTuner.getParameter("connString")
                    + cfgTuner.getParameter("database")
                    + cfgTuner.getParameter("connParam"), cfgTuner.getParameter("usr"), cfgTuner.getParameter("pw"));
            conn.setAutoCommit(true);
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
        } catch (Exception e) {
            System.out.println("Connection to " + cfgTuner.getParameter("connString") + " FAILED!...");
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
        tm = System.currentTimeMillis() - tm;
        IOUtil.writeLogLn("Connection to " + cfgTuner.getParameter("connString") 
                    + rm.getString("queryLabel") + " (" + cfgTuner.getParameter("c") + ") OK (" + tm + "ms) ", rm);
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
