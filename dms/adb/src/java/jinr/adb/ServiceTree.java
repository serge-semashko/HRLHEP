package jinr.adb;

import java.sql.*;
import dubna.walt.util.StrUtil;

public class ServiceTree extends dubna.walt.service.Service
{
int numNodes =0;

public void start() throws Exception
{
  cfgTuner.outCustomSection("before tree",out);
   
  showTree();

  String after_tree = StrUtil.strFromArray(cfgTuner.getCustomSection("more_links"));
  after_tree = StrUtil.replaceInString(after_tree,"\"", "\\\"");
  cfgTuner.addParameter("MORE_LINKS", after_tree);
  cfgTuner.outCustomSection("after tree",out);
}


protected void showTree() throws Exception
{ numNodes = cfgTuner.getIntParameter("NUM_NODES");
  if (numNodes <= 0) return;
  String[][] nodes = new String[numNodes+1][5];
  int i=0;    
  try {
  ResultSet r = dbUtil.getResults(getSQL("tree SQL"));
//  String nodeId = "";
//  StringBuffer sb = new StringBuffer(1024);
  while (r.next() && i<=numNodes)
  { 
//    System.out.println("i=" + i + " / " + r.getString(1));
    nodes[i][0] = r.getString(1);
    nodes[i][1] = r.getString(2);
    nodes[i][2] = r.getString(3);
    nodes[i][3] = r.getString(4);
    nodes[i++][4] = r.getString(5);
  }
  dbUtil.closeResultSet(r); 
  }
  catch (Exception e) {
      e.printStackTrace();
      throw e;
  }
  for (i=0; i < numNodes; i++)
  { showNode(nodes,i);
  }
}

protected void showNode(String[][] nodes, int nr) throws Exception
{ String nodeId = nodes[nr][0];
  if (nodeId == null || nodeId.length() < 1) return;
  cfgTuner.addParameter("ID", nodeId);
  cfgTuner.addParameter("PID",  nodes[nr][1]);
  cfgTuner.addParameter("DES",  nodes[nr][2]);
  cfgTuner.addParameter("TYPE",  nodes[nr][3]);
  cfgTuner.addParameter("INACTIVE",  nodes[nr][4]);
  cfgTuner.outCustomSection("item", out);
  for (int i=0; i < numNodes; i++)
  { if (nodes[i][1] != null && nodes[i][1].equals(nodeId))
    showNode(nodes,i);
  }
  nodes[nr][0] = "";
  nodes[nr][1] = "";
}

}