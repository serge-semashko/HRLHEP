 package cern.kpi.viewdb;

//import java.sql.Connection;
//import java.sql.DriverManager;
import dubna.walt.util.DBUtil;

public class ReposLogin extends dubna.walt.service.Service
{

public void beforeStart() throws Exception
{ 
  try      
  { dbUtil = (DBUtil) rm.getObject("DBUtil_Repos", false);
    if (cfgTuner.enabledExpression("cop=DISCONNECT|cop=RECONNECT") && dbUtil != null)
    { dbUtil.close();
      rm.removeKey("DBUtil_Repos");
    }
    if (cfgTuner.enabledExpression("cop=CONNECT|cop=RECONNECT"))
    { if (dbUtil == null || !dbUtil.isAlive())
      { String conn_str_repos =  cfgTuner.getParameter("conn_str_repos");
        System.out.println("+++++++ try to connect:" + conn_str_repos);
        if (cfgTuner.enabledExpression("u&p"))
        { rm.setParam("usr", cfgTuner.getParameter("u"), true);
          rm.setParam("pwd", cfgTuner.getParameter("p"), true);
        }
        dbUtil = (new DBUtil(rm.getString("driverType", true) + conn_str_repos
                  , cfgTuner.getParameter("usr")
                  , cfgTuner.getParameter("pwd")
                  , rm.getString("queryLabel", false) + "_Repository"
                  , 1));

        cfgTuner.addParameter("db", conn_str_repos);
        rm.setParam("repository", conn_str_repos, true);
        rm.setParam("conn_str_repos", conn_str_repos, true);
        rm.setObject("DBUtil_Repos", dbUtil, true);
        rm.setObject("DBUtil_" + conn_str_repos, dbUtil, true);       
        cfgTuner.addParameter("repos_connected", "Y");
        
//        System.out.println("+++++++ Connected: dbUtil="+dbUtil);
        QueryThread qt = (QueryThread) rm.getObject("QueryThread");
 //       qt.getDbList(dbUtil);
      }
    }
    if (dbUtil != null && dbUtil.isAlive())
    { cfgTuner.addParameter("repos_connected","yes");
      cfgTuner.addParameter("connected_to", cfgTuner.getParameter("db"));
      cfgTuner.addParameter("current_user", dbUtil.usr);
    
      super.beforeStart();

      if (cfgTuner.enabledOption("CONNECT_ERROR"))
      { dbUtil.close(); dbUtil=null;
        rm.removeKey("DBUtil_Repos");
        cfgTuner.deleteParameter("repos_connected");
      }
    }
  }
  catch (Exception e)
  { 
    String s = e.toString();
    while (s.indexOf("Exception: ") > 0) s = s.substring (s.indexOf("Exception: ") + 10);
    cfgTuner.addParameter("CONNECT_ERROR", s);
    if (s.indexOf("ORA-01017") >= 0)
      cfgTuner.addParameter("not configured", "YES");
    //out.println("SETUP: " + e.toString());
    //e.printStackTrace(System.out);
  }
}


}