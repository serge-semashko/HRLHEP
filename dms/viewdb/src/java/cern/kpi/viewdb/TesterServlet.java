package cern.kpi.viewdb;

import java.io.IOException;

import javax.servlet.*;
import javax.servlet.http.*;
import dubna.walt.util.*;

public class TesterServlet extends javax.servlet.http.HttpServlet
{
public ResourceManager rm = null;
protected long timer;

public void init(ServletConfig config) throws ServletException
{
  super.init(config);
  try
  {
    rm = new ResourceManager("viewdb");
    Class.forName(rm.getString("dbDriver"));        // init the JDBC driver
  }
  catch (Exception e)
  {
    System.out.println("!!!!! TesterServlet Init - could not get ResourceManager!");
    e.printStackTrace(System.out);
    throw new ServletException("Could not get ResourceManager.");
  }

  System.out.println("======== TesterServlet Init - OK! ");
}


/**
 * This method is called when the servlet's URL is accessed.<P>
 */

public void doGet(HttpServletRequest req, HttpServletResponse res)
    throws ServletException, IOException
{
  timer = System.currentTimeMillis();
  TesterQueryThread t=null;
  
  try
  {
    t = new TesterQueryThread(req, res, rm);
  }
  catch (Exception e)
  {
    if (! (e instanceof java.net.SocketException))
    {
      System.out.println("xxxxx TesterServlet error - " + e.getMessage() + "");
      e.printStackTrace(System.out);
      throw (new ServletException(e.toString()));
    }
  }

//  System.out.println("+++ TesterServlet finished in " + (System.currentTimeMillis() - timer) / 1000. + " sec.");
  System.gc();
}


}