package cern.kpi.viewdb;

import java.sql.ResultSet;
import java.util.Vector;
import dubna.walt.util.DBUtil;

public class LockStructuringService extends dubna.walt.service.Service 
{

private int treeLevel=0;
private String parentSID;

public void start() throws Exception
{
    cfgTuner.outCustomSection(headerSectionName,out);

    makeTree();

    cfgTuner.outCustomSection(footerSectionName, out);
}


public void makeTree() throws Exception
{ 
  String[] parents = getParentsSIDs();
  if (parents.length == 0)
    cfgTuner.addParameter("NumTableRows","0");
  else 
    for (int i=0; i < parents.length; i++)
    { parentSID = parents[i];
      treeLevel = 0;
      makeSubTree(parents[i]);
    }
}

public void makeSubTree(String sid) throws Exception
{ String str = "";
  String sqlSectionName="";

  for (int i=1; i < treeLevel; i++)
    str = "&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;" + str;
    
  cfgTuner.addParameter("SPACES", str);
  cfgTuner.addParameter("SessSID", sid);
  
  if (!parentSID.equalsIgnoreCase(sid))
  { cfgTuner.addParameter("isChild", "yes");
    sqlSectionName = "getLocksSQL";
  }
  else
  { cfgTuner.addParameter("isChild", "");
    sqlSectionName = "getParLocksSQL";
  }

  ResultSet r = dbUtil.getResults(getSQL(sqlSectionName));
  String[] headers = DBUtil.getColNames(r);
  String val = "";

  while (r.next())
  { for (int i = 0; i < headers.length; i++)
    { val = r.getString(i+1);
       if (val != null && val.length() > 0 && !val.equalsIgnoreCase("NULL"))
          cfgTuner.addParameter(headers[i], val);
     }
     cfgTuner.outCustomSection("Locks Table", out);
   }
   dbUtil.closeResultSet(r);

  /* get children */
  cfgTuner.addParameter("PARSID", sid);
  String[] childs = getChildsSIDs();
  if (childs.length > 0)
  { for (int i=0; i < childs.length; i++)
    { treeLevel++;
      makeSubTree(childs[i]);
      treeLevel--;
    }
  }
}

public String[] getParentsSIDs() throws Exception
{
  ResultSet r = dbUtil.getResults(getSQL("getParentsSQL"));
  String[] headers = DBUtil.getColNames(r);

  if (!headers[0].equalsIgnoreCase("SID"))
    return null;

  Vector parents = new Vector(10);
  while (r.next())
    parents.addElement(r.getString(headers[0]));

  String[] str = new String[parents.size()];
  for (int i=0; i<parents.size(); i++)
  {
    String val = (String)parents.elementAt(i);
    if (val != null && val.length() > 0 && !val.equalsIgnoreCase("NULL"))
      str[i] = val;
  }
  dbUtil.closeResultSet(r);
  return str;
}

public String[] getChildsSIDs() throws Exception
{
  ResultSet r = dbUtil.getResults(getSQL("getChildsSQL"));
  String[] headers = DBUtil.getColNames(r);
  
  if (!headers[0].equalsIgnoreCase("SID"))
    return null;

  Vector childs = new Vector(10);
  while (r.next())
    childs.addElement(r.getString(headers[0]));

  String[] str = new String[childs.size()];
  for (int i=0; i<childs.size(); i++)
  { String val = (String)childs.elementAt(i);
    if (val != null && val.length() > 0 && !val.equalsIgnoreCase("NULL"))
      str[i] = val;
  }
  dbUtil.closeResultSet(r);
  return str;
}

}