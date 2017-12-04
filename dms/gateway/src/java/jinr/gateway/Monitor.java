package jinr.gateway;

import dubna.walt.service.Service;
import dubna.walt.util.DBUtil;
import dubna.walt.util.Fmt;
import dubna.walt.util.IOUtil;
import dubna.walt.util.ResourceManager;
import dubna.walt.util.Tuner;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.Date;

/**
 *
 * @author serg
 */
public class Monitor implements Runnable {

    private static ResourceManager rm_Global;
    protected boolean stop = false;
    protected boolean running = false;
    private static long sleepTime = 10000;
    private static boolean checkAgain = true;
//    private static ArrayList<ScheduledTask> tasks = null;
    protected static int run_id=0;

    public Monitor(ResourceManager rm_Global) {
        this.rm_Global = rm_Global;
        stop = false;
//        System.out.println("*** GATEWAY - Monitor started! rm_Global=" + rm_Global);
        run_id=0;

    }

    @Override
    public void run() {
        sleepTime = (long) rm_Global.getInt("MonitorInterval", 10000);
        System.out.println("+++ GATEWAY.MONITOR STARTED +++ sleepTime=" + sleepTime);
//        getScheduledTasks();
        while (!stop) {
            running = true;
            try {
                Thread.sleep(sleepTime);
            } catch (InterruptedException ex) {
                ex.printStackTrace(System.out);
            }
            if (!stop) {
                try {
                    run_id++;
                    checkScheduledTasks();
                    if (isDataUpdated() || checkAgain) {
                        callService("process_queue.cfg");
                    }
                } catch (Exception e) {
                    e.printStackTrace(System.out);
                }
            }
        }
        running = false;
//        tasks = null;
        System.out.println("+++ MONITOR - STOP +++");
//        if (dbUtil != null && dbUtil.isAlive()) {
//            dbUtil.close();
//            dbUtil = null;
//            System.out.println("+++ MONITOR - dbUtil.close() +++");
//        }
    }

    private ArrayList<ScheduledTask>  getScheduledTasks() {
        ArrayList<ScheduledTask> tasks = new ArrayList<ScheduledTask>();
        ResourceManager rms = rm_Global.cloneRM();
        rms.setObject("rm_Global", rm_Global);
        rms.setParam("log", "true");

        try {
//            IOUtil.writeLogLn(0, "<b>===> MONITOR.getScheduledTasks()</b>", rms);
            DBUtil db = makeDBUtil(rms);
            ResultSet rs = db.getResults("select id, time, module, comment, lastCall, lastResult from schedule where active=1");
            while (rs.next()) {
                ScheduledTask st = new ScheduledTask(rs.getInt("id"), rs.getString("time"), rs.getString("module"), rs.getLong("lastCall"), rs.getString("lastResult"), rm_Global);
                tasks.add(st);
            }
            db.close();
        } catch (Exception e) {
            e.printStackTrace();
            tasks = null;
        }
        return tasks;
    }

    private void checkScheduledTasks() {
//        if (tasks == null) {
//            getScheduledTasks();
//        }
        ArrayList<ScheduledTask> tasks = getScheduledTasks();
        try {
            for (ScheduledTask st : tasks) {
                st.checkStatus();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    private boolean isDataUpdated() {
        synchronized (rm_Global) {
            String s = rm_Global.getString("dataUpdateTime", false, "");
            if (s.length() > 5) {
                IOUtil.writeLogLn(5, "<br>MONITOR - GOT DATA: s=" + s + "; wait...", rm_Global);
                long dataUpdateTime = Long.parseLong(s);
                if (System.currentTimeMillis() - dataUpdateTime > sleepTime) {
                    rm_Global.putString("dataUpdateTime", "false");
                    return true;
                }
            } else {
                IOUtil.writeLog(0, "m ", rm_Global);
            }
        }
        return false;
    }

    public static ResourceManager callService(String cfgFileName) {
        String[] queryParam = {"c=" + cfgFileName};
//        System.out.println("+++ MONITOR - DATA PROCESS +++ queueCfg=" + queueCfg);
        ResourceManager rms = rm_Global.cloneRM();
        rms.setObject("rm_Global", rm_Global);
        rms.setObject("queryLabel", "MONITOR.processData()");
        rms.setParam("log", "true");
        Tuner cfgTuner = null;
        DBUtil db = null;

        try {
//            IOUtil.writeLogLn(1, "<b>===> MONITOR.callService( " + cfgFileName + " )</b>", rms);
            Date dat = new java.util.Date();
            long tm = System.currentTimeMillis();
            IOUtil.writeLogLn(5, "<br><span class='req_start pt' onClick='toggleDiv(\"MON_" + run_id + "\");'> MON_" + run_id 
                    + " [" + Fmt.shortDateStr(dat) + ":" + dat.getSeconds() + "] MONITOR.callService( " + cfgFileName 
                    + " ) </span><div id='MON_" + run_id + "' class='req'>"
                    , rms);
            
            cfgTuner = new Tuner(queryParam,
                    cfgFileName, rms.getString("CfgRootPath", false), rms);
            rms.setObject("cfgTuner", cfgTuner);
            cfgTuner.addParameter("tm", Long.toString(System.currentTimeMillis()) );
            db = makeDBUtil(rms);
            if (db == null) {
                cfgTuner.addParameter("NotConnected", "Y");
            }
            rms.setObject("DBUtil", db, false);
//            rms.setObject("DBUtil", makeDBUtil(cfgTuner), false);

            String className = cfgTuner.getParameter("parameters", "service");
            if (className == null || className.length() == 0) {
                className = "dubna.walt.service.Service";
            }
            Class cl = Class.forName(className);
            Service srv = (Service) (cl.newInstance());
            if (cfgTuner.enabledOption("LOG=OFF")) {
                rms.setParam("log", "false");
            } else if (cfgTuner.enabledOption("LOG=ON")) {
                rms.setParam("log", "true");
            }

//            IOUtil.writeLogLn(0, "<hr><span style='border:solid 1px red; font-weight:bold; background-color:#FFFFA0;'>MONITOR.callService( " + cfgFileName + " ) "
//                    + " [" + Fmt.shortDateStr(dat) + ":" + dat.getSeconds() + "]</span> " + "; log=" + rms.getBoolean("log") + "; SERVICE:"
//                    + srv, rms);
            srv.doIt(rms);          // START OF THE WALT SERVICE with CFG-file
            checkAgain = (cfgTuner.getParameter("checkAgain").equals("true"));

//            IOUtil.writeLogLn(0, "<hr><span style='border:solid 1px red; font-weight:bold; background-color:#FFFFA0;'>BACK to MONITOR.callService() : "
//                    + "</span> " + "; checkAgain=" + checkAgain + "; "
//                    + srv, rms);

            tm = System.currentTimeMillis() - tm;
            IOUtil.writeLogLn(5, "</div><span style='border:solid 1px red; font-weight:bold; background-color:#FFFFA0;'>BACK to MONITOR.callService() (" + tm + "ms)</span>"
                    + " checkAgain=" + checkAgain + "; "
                    , rms);

        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (db != null) {
                db.close();
            }
        }
        return rms;
    }

    public boolean isRunning() {
        return running;
    }

    /**
     * Коннект к БД в начале выполнения запроса
     *
     * @param cfgTuner
     * @return DBUtil, который будет использоваться для работы с БД.
     * @throws Exception
     */
    public static synchronized DBUtil makeDBUtil(ResourceManager rm) throws Exception {
//        if (dbUtil != null && dbUtil.isAlive()) {
//            return dbUtil;
//        }
        DBUtil dbUtil = null;
        long tm = System.currentTimeMillis();
        try {
            /* Establish connection to the database and make DBUtil */
            IOUtil.writeLogLn(5, " MONITOR.makeDBUtil() :" + rm.getString("connString")
                    + rm.getString("database") + rm.getString("connParam")
                    + " // " + rm.getString("usr") + "/*** ", rm_Global
            );
            Connection conn = DriverManager.getConnection(rm.getString("connString")
                    + rm.getString("database")
                    + rm.getString("connParam"), rm.getString("usr"), rm.getString("pw"));
            conn.setAutoCommit(true);
            dbUtil = new DBUtil(conn, "MONITOR");
            dbUtil.db = DBUtil.DB_MySQL;
            dbUtil.allocate();
        } catch (Exception e) {
            IOUtil.writeLogLn(0, "ERROR: MONITOR: Connection to " + rm.getString("connString") + " FAILED!...", rm_Global);
            System.out.println("MONITOR: Connection to " + rm.getString("connString") + " FAILED!...");
            e.printStackTrace(System.out);
            return null;
        }
//        System.out.println(" Connect OK!");
        tm = System.currentTimeMillis() - tm;
        IOUtil.writeLogLn(5, "MONITOR: Connection to " + rm.getString("connString")
                 + " OK (" + tm + "ms) ", rm_Global);
        return dbUtil;
    }


}
