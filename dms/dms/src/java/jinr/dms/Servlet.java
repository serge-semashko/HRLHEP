package jinr.dms;

import dubna.walt.util.ResourceManager;
import java.io.*;
import dubna.walt.util.*;

public class Servlet extends dubna.walt.BasicServlet
{

public ResourceManager obtainResourceManager() throws Exception
{
  System.out.println(".\n\r.\n\r.\n\r*** DMS - INIT ...");
//  Load static resources for the app
  ResourceManager rm = new ResourceManager("dms");
//  System.out.println("  --> " + rm.getString("dbDriver"));
//  Class.forName(rm.getString("dbDriver"));        // init the JDBC driver

// Path to the application root in the server's file system
/*
  String myPath=getServletConfig().getServletContext().getRealPath("/");
  myPath = StrUtil.replaceInString(myPath,"\\","/");
  System.out.println("... myPath=" +  myPath );
  rm.setParam("AppRoot", myPath, true);
  rm.setParam("logPath", myPath, true);
  
// Path to the Tomcat root in the server's file system
  String t = myPath.substring(0,myPath.length()-2);
  int i = t.lastIndexOf("/");
  if (i > 0) t = t.substring(0,i+1);
  rm.setParam("TomcatRoot", t, true);
	
	System.out.println("*** EDO: rm=" + rm);
/**/  
/*  Servlet.readConfigFile(myPath + "WEB-INF/" + rm.getString("cfgFileName"), rm);
*/	
  return rm;
}

public void customInit() { 
	try {
		//  Load static resource for Russian GUI language
		 System.out.println("*** DMS - loading russian.dat ...");
			ResourceManager srm = new ResourceManager(rm_Global.getString("CfgRootPath", true) +  "dat/russian.dat"
				,  rm_Global.getString("serverEncoding", false, "Cp1251"));
			rm_Global.setObject("srm_russian", srm, false );
	
			//  Load static resource for English GUI language
			System.out.println("*** DMS - loading english.dat ...");
			srm = new ResourceManager(rm_Global.getString("CfgRootPath", true) + "dat/english.dat"
				,  rm_Global.getString("serverEncoding", false, "Cp1251"));
			rm_Global.setObject("srm_english", srm, false );
	}
	catch (Exception e) 
	{
		e.printStackTrace(System.out);
	}
}
	
public void setResourceManager(ResourceManager rm)
{
  this.rm_Global = rm;
	System.out.println("*** DMS: rm_Global=" + rm_Global);
}
 
}