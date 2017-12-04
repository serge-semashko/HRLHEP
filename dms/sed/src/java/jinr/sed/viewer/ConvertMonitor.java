/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package jinr.sed.viewer;

import dubna.walt.service.Service;
import dubna.walt.util.DBUtil;
import dubna.walt.util.Fmt;
import dubna.walt.util.IOUtil;
import dubna.walt.util.ResourceManager;
import dubna.walt.util.Tuner;
import java.sql.Connection;
import java.sql.DriverManager;
import java.util.Date;

/**
 *
 * @author serg
 */
public class ConvertMonitor implements Runnable {

  private static ResourceManager rm_Global;
  protected boolean stop = false;
  protected boolean running = false;
  private static long sleepTime = 5000;
  private static boolean checkAgain = true;

  public ConvertMonitor(ResourceManager rm_Global) {
    ConvertMonitor.rm_Global = rm_Global;
    stop = false;
//        System.out.println("*** GATEWAY - Monitor started! rm_Global=" + rm_Global);
  }

  @Override
  public void run() {
    sleepTime = (long) rm_Global.getInt("MonitorInterval", 1000);
    System.out.println("+++ ConvertMonitor STARTED +++ sleepTime=" + sleepTime);
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
          if (isDataUpdated() || checkAgain) {
            callService("files/sys/checkConvertQueue.cfg", rm_Global);
          }
        } catch (Exception e) {
          e.printStackTrace(System.out);
        }
      }
    }
    running = false;
    System.out.println("+++ ConvertMonitor - STOP +++");
//        if (dbUtil != null && dbUtil.isAlive()) {
//            dbUtil.close();
//            dbUtil = null;
//            System.out.println("+++ ConvertMonitor - dbUtil.close() +++");
//        }
  }

  private boolean isDataUpdated() {
    synchronized (rm_Global) {
      String s = rm_Global.getString("dataUpdateTime", false, "");
      if (s.length() > 5) {
        IOUtil.writeLogLn(0, "<br>ConvertMonitor - GOT DATA: s=" + s + "; wait...", rm_Global);
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

  public static synchronized ResourceManager callService(String cfgFileName, ResourceManager rm_Global) {
    String[] queryParam = {"c=" + cfgFileName};
    return ConvertMonitor.callService(queryParam, rm_Global);
  }

  public static synchronized ResourceManager callService(String[] queryParam, ResourceManager rm_Global) {

//        System.out.println("+++ ConvertMonitor - DATA PROCESS +++ queueCfg=" + queueCfg);
    ResourceManager rms = rm_Global.cloneRM();
    rms.setObject("rm_Global", rm_Global);
    rms.setObject("queryLabel", "ConvertMonitor.callService()");
    rms.setParam("log", "true");
    Tuner cfgTuner;
    DBUtil db = null;
    String cfgFileName = "";
    try {
      for (int i = 0; i < queryParam.length; i++) {
        if (queryParam[i].startsWith("c=")) {
          cfgFileName = queryParam[i].substring(2);
        }
      }
      IOUtil.writeLogLn(1, "<b>===> ConvertMonitor.callService( " + cfgFileName + " )</b>", rms);
      cfgTuner = new Tuner(queryParam,
              cfgFileName, rms.getString("CfgRootPath", false), rms);
      rms.setObject("cfgTuner", cfgTuner);
      cfgTuner.addParameter("tm", Long.toString(System.currentTimeMillis()));
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

      Date dat = new java.util.Date();

      IOUtil.writeLogLn(0, "<hr><span style='border:solid 1px red; font-weight:bold; background-color:#FFFFA0;'>ConvertMonitor.callService( " + cfgFileName + " ) "
              + " [" + Fmt.shortDateStr(dat) + ":" + dat.getSeconds() + "]</span> " + "; log=" + rms.getBoolean("log") + "; SERVICE:"
              + srv, rms);
      srv.doIt(rms);          // START OF THE WALT SERVICE with CFG-file
      checkAgain = (cfgTuner.getParameter("checkAgain").equals("true"));

      IOUtil.writeLogLn(0, "<hr><span style='border:solid 1px red; font-weight:bold; background-color:#FFFFA0;'>BACK to ConvertMonitor.callService() : "
              + "</span> " + "; checkAgain=" + checkAgain + "; "
              + srv, rms);
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
   * @param rm
   * @return DBUtil, который будет использоваться для работы с БД.
   * @throws Exception
   */
  public static synchronized DBUtil makeDBUtil(ResourceManager rm) throws Exception {
//        if (dbUtil != null && dbUtil.isAlive()) {
//            return dbUtil;
//        }
    DBUtil dbUtil;
    long tm = System.currentTimeMillis();
    try {
      /* Establish connection to the database and make DBUtil */
      IOUtil.writeLogLn(1, " ConvertMonitor.makeDBUtil() :" + rm.getString("connString")
              + rm.getString("database") + rm.getString("connParam")
              + " // " + rm.getString("usr") + "/*** ", rm
      );
      Connection conn = DriverManager.getConnection(rm.getString("connString")
              + rm.getString("database")
              + rm.getString("connParam"), rm.getString("usr"), rm.getString("pw"));
      conn.setAutoCommit(true);
      dbUtil = new DBUtil(conn, "ConvertMonitor");
      dbUtil.db = DBUtil.DB_MySQL;
      dbUtil.allocate();
    } catch (Exception e) {
      System.out.println("ConvertMonitor: Connection to " + rm.getString("connString") + " FAILED!...");
      e.printStackTrace(System.out);
      IOUtil.writeLogLn(0, "ERROR: ConvertMonitor: Connection to " + rm.getString("connString") + " FAILED!...", rm);
      return null;
    }
//        System.out.println(" Connect OK!");
    tm = System.currentTimeMillis() - tm;
    IOUtil.writeLogLn(1, "ConvertMonitor: Connection to " + rm.getString("connString")
            + " OK (" + tm + "ms) ", rm);
    return dbUtil;
  }

}
