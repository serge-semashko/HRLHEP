package cern.kpi.viewdb;

import dubna.walt.util.*;
import java.sql.*;

public class LoginSys extends dubna.walt.service.Service
{

public void beforeStart() throws Exception
{ String dbs = cfgTuner.getParameter("db~s");
  if (!cfgTuner.enabledOption("connected_to=" + dbs))
  { dbUtil = QueryThread.makeDBUtil(dbs, cfgTuner, rm);
  }
  if (cfgTuner.enabledOption("connected_to=" + dbs))
  { try      
    { String sql = "select INITIAL_RSRC_CONSUMER_GROUP as ACC_GROUP from dba_users where USERNAME=upper('" 
          + cfgTuner.getParameter("current_user") + "')";
      getPreData(sql);

      if (cfgTuner.enabledExpression("ACC_GROUP&!ACC_GROUP=SYS_GROUP"))
      { cfgTuner.addParameter("CONNECT_ERROR", "You must specify a DBA account!");
        dbUtil.close();
        rm.removeKey("DBUtil_" + dbs);
        return;
      }

      try
      { if (dbs.indexOf("~s") > 0)
        { String db = StrUtil.replaceInString(dbs, "~s", "");
          DBUtil dbu = (DBUtil) rm.getObject("DBUtil_" + db);
          try { dbu.close(); } catch (Exception xx) {}
          rm.removeKey("DBUtil_" + db);
          killSessions(dbUtil, rm);
        }
      }
      catch (Exception ex) {
        ex.printStackTrace(System.out);
      }
    }
    catch (Exception e)
    { cfgTuner.addParameter("CONNECT_ERROR", e.toString());
      //out.println("SETUP: " + e.toString());
      e.printStackTrace(System.out);
    }
    super.beforeStart();
  }
}

private static void killSessions(DBUtil dbUtil, ResourceManager rm) throws Exception
{
  String usr = rm.getString("usr");
  String sql = "SELECT 'ALTER SYSTEM KILL SESSION '''||sid||','||serial#||'''' FROM gv$session WHERE username=upper('" + usr + "')";
  
  ResultSet r = dbUtil.getResults(sql);
  while (r.next())
  { sql = r.getString(1);
    if (sql != null && sql.length() > 0 && !sql.equalsIgnoreCase("NULL"))
    { //System.out.println(sql);
      try 
      {  int n = dbUtil.update(sql);
//        System.out.println(n);
      } catch (Exception e) {}
    }
  }
  dbUtil.commit();
  dbUtil.closeResultSet(r);
}

}