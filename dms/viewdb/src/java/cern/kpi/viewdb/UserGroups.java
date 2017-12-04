package cern.kpi.viewdb;

import java.util.StringTokenizer;
import java.sql.*;
//import dubna.walt.util.DBUtil;

public class UserGroups extends dubna.walt.service.Service
{

public void beforeStart() throws Exception
{ 
  // get the connection and prepared statement
  Connection conn = dbUtil.getConnection();
  PreparedStatement pstmt = conn.prepareStatement
    ("insert into USER_TO_GROUP (USERID, GROUPID) values (?, ?)");

  // Adding a user to several groups  (cfg. admin_user_edit)
  if ( cfgTuner.enabledOption("group_ids"))
  { StringTokenizer st = new StringTokenizer(cfgTuner.getParameter("group_ids"),",");
    String userid = cfgTuner.getParameter("userid");
    dbUtil.update("delete from USER_TO_GROUP where USERID='" + userid + "'");

    pstmt.setString(1, userid);
    while (st.hasMoreTokens())
    { pstmt.setInt(2, Integer.parseInt(st.nextToken()));
      pstmt.executeUpdate();
    }
    pstmt.close();
  }

  super.beforeStart();

  // Adding several users to a group (cfg. admin_group_edit)
  if ( cfgTuner.enabledOption("user_ids"))
  { StringTokenizer st = new StringTokenizer(cfgTuner.getParameter("user_ids"),",");
    int groupid = 0;
    if (cfgTuner.enabledOption("cop=ADD"))
      groupid = cfgTuner.getIntParameter("NEW_ID");
    else
      groupid = cfgTuner.getIntParameter("groupid");

    pstmt.setInt(2, groupid);
    while (st.hasMoreTokens())
    { pstmt.setString(1, st.nextToken());
      pstmt.executeUpdate();
    }
    pstmt.close();
  }
}

}