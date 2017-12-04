package dubna.walt;

import java.io.IOException;
import java.io.PrintWriter;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpServletRequest;
import dubna.walt.util.*;

import javax.servlet.http.HttpServlet;

//public class Query implements Runnable

/**
 *
 * @author serg
 */
public class Query
{

private ResourceManager rm = null;
private HttpServletRequest req = null;
private HttpServletResponse res = null;
private String queryLabel = null;
private long timer;


/** Constructor.
     * @param req
     * @param res
     * @param rm_Global
     * @param queryLabel
     * @param servlet
     * @throws javax.servlet.ServletException
     * @throws java.io.IOException
 */
public Query ( HttpServletRequest req
             , HttpServletResponse res
             , ResourceManager rm_Global
             , String queryLabel
             , HttpServlet servlet)
                throws ServletException, IOException
{
  this.rm = rm_Global.cloneRM();
  this.queryLabel=queryLabel;

  rm.setObject("rm_Global", rm_Global);
  rm.setObject("request", req);
  rm.setObject("response", res);
  rm.setObject("queryLabel", queryLabel);
  rm.setObject("Servlet", servlet);
  
	try
  {
//    System.out.println(queryLabel + " - RUN() ");
    timer = System.currentTimeMillis() / 10;
    String s = req.getServletPath() + "?" + req.getQueryString();
/*    if (s.indexOf("&debug=") > 0)
      s = s.substring(0, s.indexOf("&debug=")) + ";...";
    if (s.indexOf("&tm=") > 0)
      s = s.substring(0, s.indexOf("&tm=")) + ";...";
    System.out.println(". " + queryLabel + " " + Fmt.lsDateStr((new java.util.Date()))
      + " " + StrUtil.unescape(s));
/**/      
//    Thread thisThread = Thread.currentThread();
//    thisThread.setPriority(Thread.MAX_PRIORITY-1);
//    thisThread.setName(queryLabel);

    rm.setParam("queryLabel", queryLabel);
//    if(1 == 1)
 //     throw (new Exception("AAA"));
//    System.out.println("\n\r Make query thread...");
    QueryThread t = makeQueryThread(rm);
    t.start();

//    String name = queryLabel;
//    String name = thisThread.getName();
//    if (name.length() > 30) name.substring(0, 20);
//    thisThread.setName("Finished! <small>(" + name + ")</small>");
//    System.out.println(queryLabel + ": " + (System.currentTimeMillis() / 10 - timer) / 100. + " sec.");
  }
  catch (Exception e)
  {
    if (! (e instanceof java.net.SocketException))
    {
      System.out.println("EXCEPTION: " + e.getMessage());
      e.printStackTrace(System.out);
      try
      {
        PrintWriter outWriter = new PrintWriter(res.getOutputStream());
        outWriter.println("</center><H3> Servlet error - </h3>" + e.getMessage() + "<pre>");
        e.printStackTrace(outWriter);
        outWriter.close();
      }
      catch (Exception ex)
      {
      System.out.println("-------");
        ex.printStackTrace(System.out);
      }
    }
  }
  finally
  {
this.rm = null;
this.req = null;
this.res = null;
this.queryLabel = null;

//  System.gc();
  }
}

    /**
     *
     * @param rm
     * @return
     * @throws Exception
     */
    public QueryThread makeQueryThread (ResourceManager rm) throws Exception
{
  /* --- make QueryThread object --- */
  String className = rm.getString("QueryThreadClass");
  if (className.length() == 0) // set the default QueryThread class
    className = "dubna.walt.SimpleQueryThread";
// System.out.println("----- makeQueryThread... '" + className + "'");
  Class cl = Class.forName(className);
  QueryThread t = (QueryThread)(cl.newInstance());
  t.init(rm);
  return t;
}

}