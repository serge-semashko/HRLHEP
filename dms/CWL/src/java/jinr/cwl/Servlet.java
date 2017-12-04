package jinr.cwl;

import dubna.walt.util.ResourceManager;

public class Servlet extends dubna.walt.BasicServlet
{

	
@Override
public ResourceManager obtainResourceManager() throws Exception
{
  System.out.println(".\n\r.\n\r.\n\r*** CWL - INIT ...");
//  Load static resources for the app
  ResourceManager rm = new ResourceManager("cwl");
//  System.out.println("  --> " + rm.getString("dbDriver"));
//  Class.forName(rm.getString("dbDriver"));        // init the JDBC driver

  return rm;
}
	
public void setResourceManager(ResourceManager rm)
{
  this.rm_Global = rm;
	System.out.println("*** CWL: rm_Global=" + rm_Global);
}
 
}