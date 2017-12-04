package cern.kpi.viewdb;

public class PwGen extends dubna.walt.service.Service
{

public void beforeStart() throws Exception
{ 
//  super.beforeStart();    
  String tm = Long.toString(System.currentTimeMillis());
  tm = tm.substring(tm.length()-3);  
//  System.out.println(tm);
  tm += tm;
  tm = dubna.walt.util.Coder.encode(tm, "ZXBYXaR5").replace(' ','_');
  cfgTuner.addParameter("newPW", tm);
}

public static String encPw(String pw)
{
  return pw;
}
}