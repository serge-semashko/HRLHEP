package jinr.pin;

import dubna.walt.util.ResourceManager;

import java.io.*;
import dubna.walt.util.*;

public class Servlet extends dubna.walt.BasicServlet
{

public ResourceManager obtainResourceManager() throws Exception
{
  System.out.println(".\n\r.\n\r.\n\r*** PIN v1.0.0 - INIT ...");
  ResourceManager rm = new ResourceManager("pin");
//  System.out.println("  --> " + rm.getString("dbDriver"));
//  Class.forName(rm.getString("dbDriver"));        // init the JDBC driver

  String myPath=getServletConfig().getServletContext().getRealPath("/");
  myPath = StrUtil.replaceInString(myPath,"\\","/");
  System.out.println("... myPath=" +  myPath );
  rm.setParam("AppRoot", myPath, true);
  rm.setParam("logPath", myPath, true);
  
  String t = myPath.substring(0,myPath.length()-2);
  int i = t.lastIndexOf("/");
  if (i > 0) t = t.substring(0,i+1);
  rm.setParam("TomcatRoot", t, true);
  
/*  Servlet.readConfigFile(myPath + "WEB-INF/" + rm.getString("cfgFileName"), rm);
*/	
  return rm;
}

public void setResourceManager(ResourceManager rm)
{
  this.rm_Global = rm;
}
/* public static void readConfigFile(String filePath, ResourceManager rm)
{
  try
  { String charset = rm.getString("serverEncoding", false, "ISO-8859-1");
    System.out.println("... opening " + filePath + "; charset: " + charset);
    BufferedReader br = 
      new BufferedReader(
        new InputStreamReader(
          new FileInputStream(filePath), charset)
      ,2048);
      
  String str=null;
  while ((str = br.readLine()) != null)
  { // System.out.println(str);
    int i = str.indexOf("=");
    if (i > 0 && str.indexOf("#") != 0)
      rm.setParam(str.substring(0,i), str.substring(i+1), true);
  }
  br.close();
  }
  catch (Exception e)
  {
     e.printStackTrace(System.out);
  }
}
*/

}