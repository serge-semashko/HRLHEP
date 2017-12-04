package jinr.adb;

import dubna.walt.util.ResourceManager;

public class Adb extends dubna.walt.BasicServlet
{

public ResourceManager obtainResourceManager() throws Exception
{
//System.out.print("*** Locale:" + java.util.Locale.getDefault());
//  java.util.Locale.setDefault(new java.util.Locale("ru","RU"));
//  java.util.Locale.setDefault(new java.util.Locale("ru","RU","WIN"));
System.out.print("*** ADB - INIT ...");
  ResourceManager rm = new ResourceManager("adb");
System.out.println("  --> " + rm.getString("dbDriver"));
//  Class.forName(rm.getString("dbDriver"));        // init the JDBC driver
  return rm;
}

}