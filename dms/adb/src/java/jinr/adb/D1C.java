package jinr.adb;

import dubna.walt.util.ResourceManager;

public class D1C extends dubna.walt.BasicServlet
{

public ResourceManager obtainResourceManager() throws Exception
{
//System.out.print("*** Locale:" + java.util.Locale.getDefault());
//  java.util.Locale.setDefault(new java.util.Locale("ru","RU"));
//  java.util.Locale.setDefault(new java.util.Locale("ru","RU","WIN"));
//System.out.println("  --> " + java.util.Locale.getDefault());
  return new ResourceManager("d1c");
}

}