package cern.kpi.viewdb;

import java.sql.Connection;
import java.sql.DriverManager;
import dubna.walt.util.DBUtil;
//import dubna.walt.service.Service;

public class Setup extends dubna.walt.service.Service
{

public void beforeStart() throws Exception
{ 
  
  if ( !cfgTuner.enabledOption("doIt"))
  { super.beforeStart();
    String conn_str_name = cfgTuner.getParameter("conn_str_name");
    cfgTuner.addParameter("conn_str", rm.getString(conn_str_name));
    return;
  }
    
  try      
  { Connection conn = DriverManager.getConnection(
      rm.getString("driverType", true) + cfgTuner.getParameter("conn_str")
    , cfgTuner.getParameter("u"), cfgTuner.getParameter("p"));

    conn.setAutoCommit(true);
    dbUtil = (new DBUtil(conn, 
        rm.getString("queryLabel",false) + "_" + cfgTuner.getParameter("db")));
    rm.setObject("DBUtil", dbUtil, true);
    System.out.println("+++++++ Connected: dbUtil="+dbUtil);
    super.beforeStart();
    cfgTuner.addParameter("done","yes");
  }
  catch (Exception e)
  { out.println("SETUP: " + e.toString());
    e.printStackTrace(System.out);
  }
}

public void afterStart()
{
  if (cfgTuner.enabledOption("done"))
    dbUtil.close();
}


}