package cern.kpi.viewdb;

import dubna.walt.util.*;
import java.util.*;

public class UserConnListService extends dubna.walt.service.Service
{


public void beforeStart() throws Exception
{
  String loginName = cfgTuner.getParameter("loginName");

  if (cfgTuner.enabledOption("cop=cls"))
    logoutUser(loginName);

  String conns = rm.getString(loginName + "_conns", false);
  DBUtil dbu = null;
  String s = "";
  StringBuffer sb = new StringBuffer(100);
  if (conns.length() > 0)
  { StringTokenizer st = new StringTokenizer( conns, ";");
    while (st.hasMoreTokens())
    { s = st.nextToken();
      sb.append ("<li> " + s);
      dbu = (DBUtil) rm.getObject(loginName + s + "db", false);
      if (dbu != null && dbu.isAlive())
        sb.append (" (alive)" );
      else
        sb.append (" <b>(DEAD!)</b>" );
    }
    cfgTuner.addParameter("user_conns", sb.toString());  
  }
}

private void logoutUser(String username)
{
  String conns = rm.getString(username + "_conns", false);
  DBUtil dbu = null;
  String s = "";
  if (conns.length() > 0)
  { StringTokenizer st = new StringTokenizer( conns, ";");
    while (st.hasMoreTokens())
    { s = st.nextToken();
      dbu = (DBUtil) rm.getObject(username + s + "db", false);
      if (dbu != null && dbu.isAlive())
      { dbu.close();
        rm.removeKey(username + s + "db");
      }
      rm.removeParam(username + s);
    }
    rm.removeParam(username + "_conns");  
  }
}

}