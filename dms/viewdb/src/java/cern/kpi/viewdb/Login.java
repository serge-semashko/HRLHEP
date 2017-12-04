package cern.kpi.viewdb;

import dubna.walt.util.*;
//import java.sql.*;

public class Login extends dubna.walt.service.Service
{

public void beforeStart() throws Exception
{ 
  dbUtil = QueryThread.connectToRepository ( rm);
  super.beforeStart();
  cfgTuner.addParameter("dbList", rm.getString("dbList"));
  String sessId = "";
  System.out.println("------- LOGIN -------");
  if (cfgTuner.enabledExpression("c_usr&c_pwd&db"))
  { String connStr = cfgTuner.getParameter("db");
    String usr = cfgTuner.getParameter("c_usr");
    String pwd = cfgTuner.getParameter("c_pwd");
    try
    { DBUtil dbUtilUsr = (new DBUtil(rm.getString("driverType", true) + connStr
                , usr, pwd, rm.getString("queryLabel",false) + " " + connStr, 1));
      dbUtilUsr.close();
    }
    catch (Exception e)
    { String msg = e.toString();
      if (msg.indexOf("Exception:") > 0) 
        msg = msg.substring(msg.indexOf("Exception:") + 10);
      cfgTuner.deleteParameter("c_pwd");
      cfgTuner.addParameter("Login_Error", msg);
      return;
    }
    String tm = Long.toString(System.currentTimeMillis());
    sessId = dubna.walt.util.MD5.getHashString(tm + pwd + usr + connStr);
    cfgTuner.addParameter("sessId", sessId);
    cfgTuner.addParameter("loginOK", "Y");
    cfgTuner.addParameter("DirectOraUser", usr);
    cfgTuner.deleteParameter("AUTH_ERROR");
    rm.setParam(sessId + "_user", usr.toUpperCase(), true);
    rm.setParam(sessId + "_pw", pwd, true);
    rm.setParam(sessId + "_db", connStr, true);
//    rm.setParam(sessId + "_ip", cfgTuner.getParameter("ClientIP"), true);
    rm.setParam(sessId + "_tm", tm, true);        
  }
  else if (cfgTuner.enabledOption("ss"))
  { sessId = cfgTuner.getParameter("ss");
    cfgTuner.deleteParameter("c_usr");
    rm.removeKey(sessId + "_user");
    rm.removeKey(sessId + "_pw");
    rm.removeKey(sessId + "_db");
    rm.removeKey(sessId + "_tm");
    System.out.println("+++++++ LOGout -------");
  }
  else  
  {
    System.out.println("............. Continue ");
  }
}

}