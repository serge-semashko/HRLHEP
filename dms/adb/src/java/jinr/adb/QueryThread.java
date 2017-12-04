package jinr.adb;

import dubna.walt.util.DBUtil;
import dubna.walt.util.Fmt;
import dubna.walt.util.IOUtil;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.util.Hashtable;

public class QueryThread extends dubna.walt.DBQueryThread {

    /**
     *
     *
     */
//    @Override
    public void start() // throws Exception
    {
        boolean log = rm.getBoolean("log");
        try {
            startTm = System.currentTimeMillis();
            parseRequest(request);
            getAddresses();
            makeTuner();

//            logQuery();

            setContentType();
            dbUtil = makeDBUtil();

            if (!headOnly) {
                if (cfgTuner.enabledOption("ResetLog=true")) {
                    IOUtil.clearLogFile(rm);
                }
                if (cfgTuner.getParameter("c").contains("showLog")) {
                    rm.setParam("log", "false");
                }

                if (validateUser()) {
                    logQuery();
                    validateParameters();
                    startService();
                } else {
                    rm.println("\n"
                            + rm.getString("queryLabel")
                            + "; [" + Fmt.shortDateStr(new java.util.Date()) + "] "
                            + cfgTuner.getParameter("ClientIP")
                            + " NOT LOGGED, "
                            + cfgTuner.getParameter("c")
                    );
                    cfgTuner.outCustomSection("not identified", outWriter);
                }
            }
            logQuery(null);
        } catch (Exception e) {
            logException(e);
            logQuery(e);
        } finally {
            if (outWriter != null) {
                try {
                    outWriter.flush();
                    outWriter.close();
                } catch (Exception e) {
                    /* don't warry! */ }
            }
            if (outStream != null) {
                try {
                    outStream.close();
                    outStream = null;
                    //         inpStream.close();
                } catch (Exception e) {
                    /* don't warry! */ }
            }
            finish();
        }
        logRequestFinished();
    }

    /**
     *
     * @return @throws Exception
     */
    @Override
    public boolean validateUser() throws Exception {
        if (cfgFileName.contains("gateway")) {
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
            System.out.println(
                    rm.getString("queryLabel")
                    + "; [" + Fmt.shortDateStr(new java.util.Date()) + "] "
                    + cfgTuner.getParameter("ClientIP")
                    + ": " + cfgTuner.getParameter("USER_ID")
                    + ": " + cfgTuner.getParameter("c")
            );
        }
    }

    
    /**
     * 
     * @param name
     * @param val
     * @param ht 
     */
    @Override
    public void registerParameter(String name, String val, Hashtable ht) {
        if (val.length() > 0) {
            if (name.equals("c")) // The Configuration file name
            {
                cfgFileName = val + ".cfg";
            } else if (ht.containsKey(name)) {
                val = val + "," + (String) ht.get(name);
            }
            if (!name.equals("SQL_TEXT") && !name.startsWith("FIXED_")) {
                val = val.replace('\'', '`');
            }
            ht.put(name, val);
        }
    }

    public void logQuery(Exception e) {
//		System.out.println("+++ logQuery +++ e=" + e);
        dbUtil = (DBUtil) rm.getObject("DBUtil");
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
                String err = trimString(cfgTuner.getParameter("ERROR"), 2047);
//            System.out.println("+++ logQuery: cfgFileName=" + cfgFileName + "; excludeFromLog=" + excludeFromLog + "; i=" + excludeFromLog.indexOf("," + cfgFileName + ",") );
                
                if (excludeFromLog.indexOf("," + cfgFileName + ",") < 0 && err.length() < 3) {
                    Connection conn = dbUtil.getConnection();
                    conn.setAutoCommit(true);
                    //		  WHEN, SYSDATE,
                    String s = "insert into ACC_STORY (USER_ID, C, QUERY, Q_YR, ERR, DAT, IP, USER_AGENT, REF, SESS_ID, SESS, TIME) values (?, ?, ?, ?, ?, SYSDATE, ?, ?, ?, ?, ?, ?)";
                    //		  System.out.println(stmnt);      
                    PreparedStatement stmt = conn.prepareStatement(s);
                    stmt.setInt(1, cfgTuner.getIntParameter(null, "USER_ID", 0));
                    stmt.setString(2, cfgTuner.getParameter("c"));
                    //		  s = cfgTuner.getParameter("queryString");
                    //			if(s.length() > 1023)
                    //			  s = s.substring(0,1023);
                    stmt.setString(3, trimString(cfgTuner.getParameter("queryString"), 1023));
                    stmt.setString(4, cfgTuner.getParameter("q_yr"));
                    if (e == null) {
                        stmt.setString(5, err);
                    } else {
                        stmt.setString(5, e.toString() + " / " + err);
                    }
                    stmt.setString(6, cfgTuner.getParameter("ClientIP"));
                    stmt.setString(7, cfgTuner.getParameter("h_user-agent"));
                    stmt.setString(8, cfgTuner.getParameter("h_referer"));
                    stmt.setInt(9, cfgTuner.getIntParameter(null, "SESS_ID", 0));
                    stmt.setString(10, cfgTuner.getParameter("q_JSESSIONID"));
                    long l = System.currentTimeMillis() - startTm;
                    stmt.setInt(11, (int) l);
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

}
