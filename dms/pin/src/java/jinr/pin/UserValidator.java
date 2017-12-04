package jinr.pin;

import java.io.PrintWriter;
import java.util.Vector;
import java.util.StringTokenizer;
import java.sql.ResultSet;
import javax.servlet.http.*;

import dubna.walt.util.*;

public class UserValidator extends dubna.walt.util.UserValidator
{

protected Tuner accRightsTuner = null;
protected int queryNum = 0;
/**
  *
  *
  */
public synchronized boolean validate(ResourceManager rm) throws Exception
{
//  HttpServletRequest request = (HttpServletRequest) rm.getObject("request");
//                        PrintWriter outWriter,
  long t = System.currentTimeMillis();

  boolean logged = super.validate(rm);
//  System.out.println("======= validate(): " + logged);
  Tuner cfgTuner = (Tuner) rm.getObject("cfgTuner");
  if (!logged)
    cfgTuner.addParameter("loginURL", rm.getString("loginURL"));
    
  if (accRightsTuner == null) 
    getAccRightsTuner(rm);
  if (cfgTuner.enabledOption("logged"))
  { if (!getUser(rm))
    { getAccRightsTuner(rm);
      getUser(rm);
    }
  }
//  cfgTuner.deleteParameter("debug");
  if(!cfgTuner.enabledOption("USER_GROUP=sys"))
  { //cfgTuner.addParameter("log", "false");
    cfgTuner.deleteParameter("debug");
//    rm.setParam("log","false");
//    rm.setParam("debug","off");
  }
  if (++queryNum > 100)
  { dbUtilLogin.close();
    dbUtilLogin = null;
    makeLoginDBUtil(rm);
    queryNum = 0;
  }
  if (cfgTuner.enabledOption("debug=on"))
  { t = System.currentTimeMillis() - t;
    System.out.println(".....  validate " + queryNum 
     + ": " + cfgTuner.getParameter("LOGINNAME")
     + "; spent: " + t + " ms.");
  }
  
  if(cfgTuner.enabledExpression("c=aradb&cop"))
    accRightsTuner = null;
  return true;
}

public boolean	getUser(ResourceManager rm) //throws Exception
{     System.out.println("======= getUser() =======");
  HttpServletRequest request = (HttpServletRequest) rm.getObject("request");
  Tuner cfgTuner = (Tuner) rm.getObject("cfgTuner");
  String user_id = cfgTuner.getParameter("USER_ID");
  cfgTuner.deleteParameter("ADMIN");

  String ug = "";
  if (user_id.equals("1") && cfgTuner.enabledOption ("q_VU"))
  {
    cfgTuner.addParameter("ADMIN","Y");
    cfgTuner.addParameter("UID","1");
    user_id = cfgTuner.getParameter ("q_VU");
  }

  String user_group = accRightsTuner.getParameter(user_id,"USER_GROUP");
  if (user_group.length() > 0)
  { cfgTuner.addParameter("USER_GROUP", user_group);
    StringTokenizer st = new StringTokenizer(user_group, ",");
    while (st.hasMoreTokens())
    { ug = st.nextToken();
      cfgTuner.addParameter("g_" + ug, "USER_GROUP");
      if (("sys,adm").indexOf(ug) >=0)
        cfgTuner.addParameter("ADMIN", "Y");
    }
      
//  System.out.println("user_id:" + user_id + ", FIO: " + accRightsTuner.getParameter(user_id,"FIO"));
    cfgTuner.addParameter("FIO", accRightsTuner.getParameter(user_id,"FIO"));
    cfgTuner.addParameter("LOGINNAME", accRightsTuner.getParameter(user_id,"LOGINNAME"));
    String a_Labs = accRightsTuner.getParameter(user_id,"LABS").trim();
    cfgTuner.addParameter("A_LABS", a_Labs);
    if (a_Labs.length() == 0)
      cfgTuner.addParameter("ACCESS_ALL", "Y");
    else
      cfgTuner.deleteParameter("ACCESS_ALL");
    cfgTuner.addParameter("A_BCS", accRightsTuner.getParameter(user_id,"BCS"));
     
    return true;
  }

//  cfgTuner.deleteParameter("logged");
  cfgTuner.addParameter("NoAccess", "Y");
  return false;
}

protected void getAccRightsTuner(ResourceManager rm) //throws Exception
{ 
  try
  { System.out.println("..... getAccRightsTuner...");
    Vector params=new Vector(100,100);
    String sql="select id, FIO, username as LOGINNAME, USER_GROUP, LABS, BCS from adb2.users a order by id";
//      + " where last > CDate('11.01.2002')";
    IOUtil.writeLogLn("===== getUsers (SQL):" + sql, rm);

    ResultSet r	= dbUtilLogin.getResults(sql);
    String[] columns=DBUtil.getColNames(r);
    while (r.next())
    { params.addElement("[" + r.getString(1) + "]");
      for (int i=2; i <= columns.length; i++)
      { String s = r.getString(i);
        if (s != null && !s.equals("null"))
          params.addElement(columns[i-1] + "=" + s);
//          params.addElement(columns[i-1] + "=" + StrUtil.unicode(s));
      }
      params.addElement("[end]");
    }
    dbUtilLogin.closeResultSet(r);
    String[] param=new String[params.size()];
    params.copyInto(param);
    IOUtil.writeLog("===== getUsers (USERS):", param, rm);

    accRightsTuner=new Tuner(null, null, null, rm);
    accRightsTuner.addSection(params);

  }
  catch (Exception e)
  {
    e.printStackTrace((PrintWriter) rm.getObject("outWriter"));
    e.printStackTrace(System.out);
  }
}


}