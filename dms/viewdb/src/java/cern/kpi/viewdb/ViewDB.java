package cern.kpi.viewdb;

import dubna.walt.util.ResourceManager;
import dubna.walt.util.DBUtil;

public class ViewDB extends dubna.walt.BasicServlet
{

public ResourceManager obtainResourceManager() throws Exception
{ String p = "pwd";
  ResourceManager rm = new ResourceManager("viewdb");
  if (rm.getString("usr", false).length() < 2)
    rm.setParam("usr",rm.getString("ApplicationName").toUpperCase(), true);
  Class.forName(rm.getString("dbDriver"));        // init the JDBC driver
  DBUtil dbUtil = QueryThread.connectToRepository(rm);
  if (rm.getString(p, false).length() < 2)
    rm.setParam(p,QueryThread.getP(), true);
  return rm;
}

}