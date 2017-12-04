package jinr.adb;

import dubna.walt.util.Coder;

public class ServiceLogin extends dubna.walt.service.Service
{

public void start() throws Exception
{
  if (cfgTuner.enabledOption("FIO"))
  {
	  String sessID=cfgTuner.getParameter("NEW_SESS_ID");
	  String name=cfgTuner.getParameter("u");
	  String u_id=cfgTuner.getParameter("U_ID");
    long t = System.currentTimeMillis();
//	  cfgTuner.addParameter("cookie", Long.toString(t) + "-" + sessID + "-"+ u_id);
	  cfgTuner.addParameter("cookie",Coder.encode(Long.toString(t) + "-" + sessID + "-"+ u_id
      ,rm.getString("ApplicationName")));
//    System.out.println("+++ cookie:" + cfgTuner.getParameter("cookie"));
  }
  super.start();
}

}