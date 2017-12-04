package cern.kpi.viewdb;

import java.sql.*;
import dubna.walt.util.*;

public class SQLExecService extends dubna.walt.service.TableServiceSimple
{
DBUtil dbViewDB = null;
DBUtil dbUser = null;

public void beforeStart() throws Exception
{ dbViewDB = dbUtil;

  dbUser = LoginCustom.getCustomDBUtil(rm, cfgTuner.getParameter("db")
     , cfgTuner.getParameter("orauser")
     , cfgTuner.getParameter("loginName"));

  if ( dbUser != null && dbUser.queryLabel == "")
  { getData("markSession");   // "mark" the user's connection - execute unique SQL
    dbUtil = dbViewDB;
    getData("getSessID");     // get the user's session ID
    dbUser.queryLabel = cfgTuner.getParameter("SESS_LABEL");  // put it into dbUser
    dbUtil = dbUser;
    dbViewDB.release();
  }
}


public void afterStart() throws Exception
{ if (dbUser != null)
     dbUser.release();
  dbUser = null;
}

public void finalize()
{ try
  { afterStart();
  }
  catch (Exception e) {}
}

public void makeTable() throws Exception
{ try
  { if ( dbUser != null)
     super.makeTable();
  }
  catch (Exception e)
  {  cfgTuner.addParameter("ERROR",e.toString());
    return;
  }
  /* get the user's session ID from the dbUser.queryLabel 
   *  and put it into cfgTuner */
  try 
  {  if ( dbUser != null)
    { dbUtil = dbViewDB;
      String sid = dbUser.queryLabel;
      int i = sid.indexOf(":");
      String sn = sid.substring(i+1);
      sid = sid.substring(0, i);
      cfgTuner.addParameter("SID", sid);
      cfgTuner.addParameter("SERIAL", sn);
      getData("getSqlID");
      dbUtil = dbUser;
    }
  }
  catch (Exception e)
  { if (rm.getBoolean("debug"))
      e.printStackTrace(System.out);
  }
}

protected ResultSet runSQL(String sqlSectionName) throws Exception
{ 
  ResultSet resultSet = null;
//  Terminator qk = new QueryKiller(rm, dbUtil, this);
//  Thread.sleep(1000);
  try
  {  resultSet = super.runSQL(sqlSectionName);
     try
     { resultSet.getMetaData().getColumnCount();
     }
     catch (Exception ex)
     { cfgTuner.addParameter("no_results", "y");
     }
     cfgTuner.addParameter("timer", dbUtil.timeSpent+" sec.");
     cfgTuner.outCustomSection("finished", out);
//      dbUtil.cancelAllStatements();
  }
  catch (Exception e)
  { cfgTuner.addParameter("timer", dbUtil.timeSpent+" sec.");
    e.printStackTrace(System.out);
    String s = e.toString();
    int i = s.indexOf("SQL: ");
    if (i > 0) s = s.substring(0, i-1);
    i = s.indexOf(" ORA-");
    if (i > 0) s = s.substring(i+1);
    cfgTuner.addParameter("ERROR", s);
    cfgTuner.outCustomSection("err msg", out);
    throw e;
  }
  finally
  { 
//  qk.finished=true;
//    qk.interrupt();
//    qk=null;
  }
  return resultSet;
}

/**
 * Termination of the query processing.
 */
public void setTerminated()
{ if (dbUser != null) 
  { dbUser.terminate();
    dbUser.terminated = false;
  }
  super.setTerminated();
}


}