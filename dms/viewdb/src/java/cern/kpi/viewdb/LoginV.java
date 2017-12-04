package cern.kpi.viewdb;

//import dubna.walt.util.*;
//import java.sql.*;

public class LoginV extends dubna.walt.service.Service
{

public void beforeStart() throws Exception
{ 
  dbUtil = QueryThread.connectToRepository ( rm);
  super.beforeStart();    
 
  String sessId = "";
  String usr = cfgTuner.getParameter("c_v_usr").toUpperCase();
  
  if (cfgTuner.enabledOption("c_v_pwd"))
  { System.out.println("------- LOGIN_V -------");
    String pwd = cfgTuner.getParameter("c_v_pwd");
    if (cfgTuner.getParameter("USERID").equals(usr.toUpperCase()))
    { String tm = Long.toString(System.currentTimeMillis());
      sessId = dubna.walt.util.MD5.getHashString(tm + pwd + usr + cfgTuner.getParameter("h_user-agent"));
      cfgTuner.addParameter("vsessId", sessId);
      cfgTuner.addParameter("vloginOK", "Y");
      cfgTuner.addParameter("ViewDBUser", usr);
      cfgTuner.deleteParameter("AUTH_ERROR");
      rm.setParam(sessId + "_user", usr, true);
      rm.setParam(sessId + "_ip", cfgTuner.getParameter("ClientIP"), true);
      rm.setParam(sessId + "_tm", tm, true);  
    }
    else
    { cfgTuner.addParameter("V_Login_Error", "Invalid username / pasword"); 
      cfgTuner.deleteParameter("c_v_pwd");
    }
  }
  else if (cfgTuner.enabledOption("ss"))
  { sessId = cfgTuner.getParameter("ss");
    cfgTuner.deleteParameter("c_v_usr");
    rm.removeKey(sessId + "_user");
    rm.removeKey(sessId + "_ip");
    rm.removeKey(sessId + "_tm");
    System.out.println("+++++++ LOGoutV -------");
  }
}

}