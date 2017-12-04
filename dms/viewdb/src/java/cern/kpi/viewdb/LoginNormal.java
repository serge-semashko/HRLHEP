package cern.kpi.viewdb;

//import dubna.walt.util.*;
//import java.sql.*;

public class LoginNormal extends dubna.walt.service.Service
{

public void beforeStart() throws Exception
{ String db = cfgTuner.getParameter("db");
  if (dbUtil != null)
  { dbUtil.close();
    dbUtil = null;
  }
  rm.removeKey("DBUtil_" + db);
  rm.removeKey("DBUtil");
  cfgTuner.addParameter("usr", rm.getString("usr"));
  cfgTuner.addParameter("pwd", rm.getString("pwd"));
  System.out.println("\n\r--- LoginNormal:" + cfgTuner.getParameter("usr") + "/" + cfgTuner.getParameter("pwd"));
  try      
  { dbUtil = QueryThread.makeDBUtil(db, cfgTuner, rm);
    if (cfgTuner.enabledOption("connected_to=" + db))
    { rm.setObject("DBUtil_" + db, dbUtil, true);
    }
    super.beforeStart();
    QueryThread.getDbList(dbUtil, rm);
  }
  catch (Exception e)
  { cfgTuner.addParameter("CONNECT_ERROR", e.toString());
      //out.println("SETUP: " + e.toString());
    e.printStackTrace(System.out);
  }
}



}