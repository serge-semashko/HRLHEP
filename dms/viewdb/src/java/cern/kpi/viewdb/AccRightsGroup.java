package cern.kpi.viewdb;

//import dubna.walt.util.DBUtil;
import java.util.StringTokenizer;

public class AccRightsGroup extends dubna.walt.service.Service
{

public void beforeStart() throws Exception
{
  super.beforeStart();
  String instList = (cfgTuner.getParameter("INST_LIST")).substring(1); 
  String groupId = cfgTuner.getParameter("groupid");
  StringTokenizer st = new StringTokenizer(instList, ",");
  while (st.hasMoreTokens())
  { String instId =  st.nextToken();
    String ar = cfgTuner.getParameter("gr_" + instId).trim();

    if ( ar.length() > 0 && !ar.equals("none") )
    { String sql = "insert into INST_ACC_RIGHTS (INSTID, GROUPID, ACC_RIGHTS) values ('" 
            + instId + "'," + groupId + ",'"+ ar + "')";
//      System.out.println(i + ":" + sql);
      dbUtil.update(sql);
    }
  }
}

}