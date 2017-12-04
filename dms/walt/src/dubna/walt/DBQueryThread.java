package dubna.walt;

import dubna.walt.util.*;

/**
 *
 * @author serg
 */
public class DBQueryThread extends SimpleQueryThread {

    public void init(ResourceManager rm) throws Exception {
        super.init(rm);
//  System.out.println("\n\r start " + queryLabel);
//  dbUtil = makeDBUtil();
//  System.out.println(queryLabel + ": got DBUtil: " + dbUtil.myName);
    }

    /*	public void XXXwriteHttpHeaders() throws Exception {
		super.writeHttpHeaders();
		dbUtil = makeDBUtil();
//  System.out.println(queryLabel + ": got DBUtil: " + dbUtil.myName); 
	}
     */
    /**
     *
     * @return @throws Exception
     */
    public synchronized DBUtil makeDBUtil() throws Exception {
        if (cfgFileName.contains("_noDB")) {
            return null;
        }
        try {
            /* try
			 {  // check the DBUtil in the ResourceManager
			 dbUtil = (DBUtil) rm.getObject("DBUtil", false);
			 if (dbUtil != null)
			 return dbUtil.cloneDBUtil(queryLabel);   // the rest we don't need to execute
			 }
			 catch (Exception ex) {System.out.println("***** makeDBUtil - should not happen: " + ex.toString());}
             */

            IOUtil.writeLog(3, "<br><i>connect: " + cfgTuner.getParameter("connString")
                        + cfgTuner.getParameter("database") + cfgTuner.getParameter("connParam") + "</i>...", rm);
//  System.out.println(rm.getString("pw", false)); 
            /* Establish connection to the database and make DBUtil */
            dbUtil = new DBUtil(rm.getString("connString"),
                    rm.getString("usr", false),
                    rm.getString("pw", false),
                    queryLabel, 1);
            dbUtil.nrConnsToKeep = 0;
            dbUtil.allocate();
            rm.setObject("DBUtil", dbUtil, false);
            String connectTime=Long.toString(System.currentTimeMillis() - startTm);
            IOUtil.writeLog(3, " Connect OK! " + connectTime + "ms.", rm);
            cfgTuner.addParameter("connectTime", connectTime);

//    rm.setObject("DBUtil", dbUtil, true);
        } catch (Exception e) {
            System.out.println("[" + Fmt.shortDateStr(new java.util.Date()) + "] Connection to " + rm.getString("connString") + " FAILED!..." + e.toString());
//    e.printStackTrace(System.out);
            if (outWriter != null) {
                outWriter.println("<small>" + e.getMessage() + "</small>");
                outWriter.println("<center><br><br><table border=1 bgcolor=#FFEEBB cellpadding=8><tr><th>"
                        + "Нет связи с базой данных!"
                        //					+ rm.getString("connString") 
                        + "</th></tr></table></center><p>");
                outWriter.flush();
            }
            return null;
        }
        getInitParams();
        return dbUtil;
    }

    /**
     *
     */
    public void getInitParams() {
    }

    /**
     *
     */
    protected void finish() {
        if (dbUtil != null) {
            try {
                dbUtil.commit();
            } catch (Exception e) {
            }
            dbUtil.release();
            dbUtil.close();
        }
        dbUtil = null;

        dbUtil = (DBUtil) rm.getObject("DBUtil", false);
        if (dbUtil != null) {
            try {
                dbUtil.commit();
            } catch (Exception e) {
            }
            dbUtil.release();
            dbUtil.close();
        }
        dbUtil = null;
        /**/       
        if(cfgTuner != null && cfgTuner.enabledOption("CloseSession=Y")) {
            try {
                cfgTuner.session.invalidate();
            }
            catch (Throwable e) {;}
        }
//  System.out.println("---------------- finish() " + queryLabel);
    }

    protected void finalize() {
        finish();
    }

}
