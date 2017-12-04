package cern.kpi.viewdb;

//import java.sql.*;
import dubna.walt.util.*;

public class LoginCustom extends dubna.walt.service.Service
{
DBUtil dbUser = null;

public static DBUtil getCustomDBUtil(ResourceManager rm, String db, String orauser,String username)
{ DBUtil dbUser = null;
  Tuner cfgTuner = (Tuner) rm.getObject("cfgTuner");
    /* try to get the user dbUtil from the ResouceManager */
  try
  { String s = db + " / " + orauser;
    DBUtil dbu = (DBUtil) rm.getObject(username + s + "db", false);
//    System.out.println("LoginCustom..." + username + s + "db: " + dbu);
    if (dbu != null)
    { dbUser = dbu.cloneDBUtil("");
      String dbu_queryLabel = dbu.queryLabel;
      if (dbUser.isAlive())
      { cfgTuner.addParameter("CONNECTED", "Y");
//    System.out.println("LoginCustom - CONNECTED!");
        dbUser.allocate();
        if (dbUser == dbu)
          dbUser.queryLabel = dbu_queryLabel;
        return dbUser;
      }
    }
  }
  catch (Exception e)
  { }
  return null; 
}

public void beforeStart() throws Exception
{ String db = cfgTuner.getParameter("db");
  String orauser = cfgTuner.getParameter("orauser");
  String loginName = cfgTuner.getParameter("loginName");
  String s = db + " / " + orauser;

  if (getCustomDBUtil(rm, db, orauser, loginName) != null) 
    return;

  /* get the password for the new user connection */
  String pw = cfgTuner.getParameter("pwcustom");
  if (cfgTuner.enabledOption("DirectOraUser")) 
    orauser=cfgTuner.getParameter("DirectOraUser");
  if (pw.length() > 0)
    rm.setParam(loginName + s, pw, true);
  else
    pw = rm.getString(loginName + s, false);
    
  /* make the user's connection and dbUtil */
  if (pw.length() > 0)
  { try
    { DBUtil dbUser = new DBUtil(rm.getString("driverType") + db, 
              orauser, pw, "", 1);
      dbUser.getConnection().setAutoCommit(false);
      dbUser.nrConnsToKeep = 1;
      rm.setObject(loginName + s + "db", dbUser, true);
      String conns = rm.getString(loginName + "_conns", false);    
      rm.setParam(loginName + "_conns", conns + s + ";", true);
//      dbUser.allocate();
      cfgTuner.addParameter("CONNECTED", "Y");
    }
    catch (Exception e)
    { cfgTuner.addParameter("LOGIN_ERROR", e.getMessage());
      System.out.println("Connection to " + db + " FAILED!...");
      System.out.println(rm.getString("driverType", true) 
          + "/" +  rm.getString("conn_str_" +db, false)
          + "/" +  orauser + "/" + pw);
      e.printStackTrace(System.out);
    }
  }
}

}