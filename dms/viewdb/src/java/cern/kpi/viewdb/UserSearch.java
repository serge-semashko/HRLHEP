package cern.kpi.viewdb;

//import java.sql.*;
import dubna.walt.util.*;

public class UserSearch extends dubna.walt.service.TableServiceSimple
{

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
{ String db = rm.getString("found_DB");
  String orauser = rm.getString("found_ORAUSER");
  String s = db + " / " + orauser;

  dbUtil.release();
  dbUtil = getCustomDBUtil(rm, db, orauser, "FOUNDATION");
  if (dbUtil != null) 
    return;

  /* get the password for the new user connection */
  String pw = rm.getString("found_PW");
    
  /* make the user's connection and dbUtil */
  if (pw.length() > 0)
  { try
    { dbUtil = new DBUtil(rm.getString("driverType") + db, orauser, pw, "FOUNDATION", 1);
      dbUtil.nrConnsToKeep = 1;
      rm.setObject("FOUNDATION" + s + "db", dbUtil, true);
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