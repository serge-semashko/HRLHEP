package cern.kpi.viewdb;

//import dubna.walt.util.DBUtil;
import java.util.StringTokenizer;

public class AccRightsInst extends dubna.walt.service.Service
{

public void beforeStart() throws Exception
{
  super.beforeStart();
  String groupsList = (cfgTuner.getParameter("GROUPS_LIST")).substring(1);
  StringTokenizer st = new StringTokenizer(groupsList, ",");
  String instId = cfgTuner.getParameter("instid");
  while (st.hasMoreTokens())
  { String groupId =  st.nextToken();
    String ar = cfgTuner.getParameter("gr_" + groupId).trim();

    if ( ar.length() > 0 && !ar.equals("none") )
    { String sql = "insert into INST_ACC_RIGHTS (INSTID, GROUPID, ACC_RIGHTS) values ('" 
            + instId + "'," + groupId + ",'"+ ar + "')";
//      System.out.println(i + ":" + sql);
      dbUtil.update(sql);
    }
  }
}

}