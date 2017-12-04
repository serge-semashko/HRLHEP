package jinr.adb;

import dubna.walt.util.*;
import java.util.StringTokenizer;
import java.io.*;

public class ServiceDocSend extends dubna.walt.service.Service
{
StringTokenizer st = null;
DBUtil dbUtilLink = null;

public void beforeStart() throws Exception
{ 
  execCmd(cfgTuner.getParameter("CmdMapR"), cfgTuner.enabledOption("debug=on"));
  
  dbUtilLink = makeDBUtilLink(rm);
  dbUtil = dbUtilLink; 
  super.beforeStart();
  dbUtil = (DBUtil) rm.getObject("DBUtil", true);
}

public void afterStart() throws Exception
{ super.afterStart();
  try 
  { dbUtilLink.release();
  }
  catch (Exception e) {}
  execCmd(cfgTuner.getParameter("CmdUnMapR"), cfgTuner.enabledOption("debug=on"));
}



public void execCmd(String cmd, boolean trace) throws Exception
{ if (cmd == null || cmd.length() < 2) return;
  IOUtil.writeLogLn("<p><b>Executing command: '" + cmd +"'</b>", rm);
  if (trace)
  { System.out.println("+++ Starting command: '" + cmd +"'");
  }
  Runtime rt = Runtime.getRuntime();
  Process p = rt.exec(cmd);
  InputStream from = p.getInputStream();
  if (trace) System.out.println(" waiting...");
  IOUtil.writeLogLn("<pre>", rm);
  copyAll(from);
  IOUtil.writeLogLn("</pre>", rm);
//      p.waitFor();
  if (trace) 
  { System.out.println(" === Finished!");
    System.out.println("     Exit code=" + Integer.toString(exitValue(p))); 
  }
}

public int exitValue(Process p)
{
  int val = 0;
  try
  {
    val = p.exitValue();
    return val;
  }
  catch (IllegalThreadStateException e)
  {
    System.out.println ("CommandExecutor-Warning: IllegalThreadStateException.");
    return -1;
  }
}


public void copyAll(InputStream from)
{
  int ch =0; 
  StringBuffer sb = new StringBuffer(1000);
  try
  { while(ch >= 0 )
    { ch = from.read();
      sb.append((char)ch);
    }
    IOUtil.writeLog(sb.toString(), rm);
  }
  catch (Exception e)
  {
    System.out.println(e.toString());
  }
}

/**/
public static synchronized DBUtil makeDBUtilLink(ResourceManager rm) throws Exception
{ DBUtil dbUtil = null;
  PrintWriter out = (PrintWriter) rm.getObject("outWriter");
  try
  { 
//    try
//    {  // check the DBUtil in the ResourceManager 
//      dbUtil = (DBUtil) rm.getObject("DBUtilLink", false);
//      if (dbUtil != null)
//        return dbUtil.cloneDBUtil(rm.getString("queryLabel"));      // the rest we don't need to execute
//    }
//    catch (Exception ex) {System.out.println("***** makeDBUtil - should not happen: " + ex.toString());}

    Class.forName(rm.getString("dbDriverLink"));        // init the JDBC driver
    // Establish connection to the database and make DBUtil 
    dbUtil = new DBUtil(rm.getString("connStringLink"), 
        "", "",  rm.getString("queryLabel"), 1);
    dbUtil.nrConnsToKeep = 1;
    dbUtil.allocate();
    rm.setObject("DBUtilLink", dbUtil, true);
  }
  catch (Exception e)
  { System.out.println("Connection to " + rm.getString("connStringLink") + " FAILED!...");
    e.printStackTrace(System.out);
    if (out != null)
    { out.println("<small>" +  e.getMessage() + "</small>");
      out.println("<center><table border=1 bgcolor=#FFEEBB cellpadding=8><tr><th>"
          + "Could not connect to the Database '" + rm.getString("connString") + "!</th></tr></table></center><p>");
      out.flush();
    }
    return null;
  }
  return dbUtil;
}
/**/

}