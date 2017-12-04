package jinr.arch;

import dubna.walt.util.DBUtil;
import dubna.walt.util.Fmt;
import java.sql.Connection;
import java.sql.PreparedStatement;
//import java.sql.ResultSet;

//import dubna.walt.DBQueryThread;

public class QueryThread extends dubna.walt.DBQueryThread
{

private static final String excludeFromLog = ",check,doc/event_cnt,css/tree,adm/acc_story,adm/viewRequest,adm/showLog_noDB,empty,doc/setParam,";
	public void getInitParams()
	{
//		try 
//		{ 
//			long startId = Long.parseLong(rm.getString("startId",false,"0"));
//			if ( startId < 2000)
//			{ 
//			  System.out.println("----------- GET startId...");
//				ResultSet rs = dbUtil.getResults("select max(id) from (select max(id)+1 as id from arch_doc union select max(id)+1 as id from arch_doc_old)");
//				if (rs.next())
//				{ startId = rs.getLong(1);
//					startId = startId / 1000 + 1;
//					rm.setParam("startId", Long.toString(startId*1000), true);
//					System.out.println("----------- startId=" + startId);
//				}
//				rs.close();
//			}
//		}
//		catch (Exception e)
//		{ System.out.println("!!!!!!!!!!!!!! Could not get initial parameters! " + e.toString());
//	 //   e.printStackTrace(System.out);
//		}	
	}

 /**
	* Print query informaition into Tomcat's log
	*/
 public void logQuery() 
 {
	 String c = cfgTuner.getParameter("c");
	 if(excludeFromLog.indexOf("," + c + ",") < 0) 
//	 if(!cfgTuner.enabledOption("c=empty"))
		 System.out.println(rm.getString("queryLabel") 
                             + "; [" + Fmt.fullDateStr( new java.util.Date() ) + "] "
                             + cfgTuner.getParameter("ClientIP")
                             + ": " + cfgTuner.getParameter("uname")
                             + ": " + cfgTuner.getParameter("c")
                            );  
 }
 
 /**
	 * Store query information in the database
	 * @param e
	 */
	  
	  public void logQuery(Exception e) 
	  {
	  //    System.out.println("+++ logQuery +++ e=" + e);
            String err = trimString(cfgTuner.getParameter("ERROR"), 2047);
           if(excludeFromLog.contains("," + c + ",") && err.length() < 2) 
               return;
	   dbUtil = (DBUtil) rm.getObject("DBUtil");
	   if (dbUtil == null || !dbUtil.isAlive()) 
	     try {     
	       dbUtil = makeDBUtil();
	     }
	     catch (Exception ex) 
	     {
	       System.out.println("+++ logQuery: ERROR! " + ex.toString());
	       ex.printStackTrace(System.out);       
	     }
	   if (dbUtil == null )
	     System.out.println("+++ logQuery: ERROR! dbUtil is null!");
	   else if (cfgTuner == null )
	       System.out.println("+++ logQuery: ERROR! cfgTuner is null!");
	   else 
	    try {
	      String c = cfgTuner.getParameter("c");
				if(excludeFromLog.indexOf("," + c + ",") < 0 || err.length() > 1) 
				{					
					Connection conn = dbUtil.getConnection();
					conn.setAutoCommit(true);
					String s = "insert into ACC_STORY (USER_ID, C, QUERY, COOKIES, ERR, DAT, IP, USER_AGENT, REF, SESS_ID, SESS, TIME) values (?, ?, ?, ?, ?, SYSDATE, ?, ?, ?, ?, ?, ?)";
					PreparedStatement stmt = conn.prepareStatement ( s );
					stmt.setInt(1, cfgTuner.getIntParameter(null, "USER_ID", 0));
					stmt.setString(2, c);
					stmt.setString(3, trimString(cfgTuner.getParameter("queryString"), 1023));
					stmt.setString(4, trimString(cfgTuner.getParameter("h_cookie"), 1047));
					if (e == null) 
						stmt.setString(5, err );
					else 
						stmt.setString(5, e.toString() + " / " + err);
					stmt.setString(6, cfgTuner.getParameter("ClientIP"));
					stmt.setString(7, cfgTuner.getParameter("h_user-agent"));
					stmt.setString(8, cfgTuner.getParameter("h_referer"));
					stmt.setInt(9, cfgTuner.getIntParameter(null, "SESS_ID", 0));
					stmt.setString(10, cfgTuner.getParameter("q_JSESSIONID"));
					long l = System.currentTimeMillis() - startTm;
					stmt.setInt(11, (int) l);
					stmt.executeUpdate();
	      }
	    }
	    catch (Exception ex) 
	    {
	      System.out.println("+++ logQuery: STORE ERROR! " + ex.toString());
	      ex.printStackTrace(System.out);       
	    }
	  }
	  
	private String trimString(String s, int maxLen) 
	{
		return s.substring(0, Math.min(s.length(), maxLen));
	}
}