package jinr.adb;

import java.util.*;
//import dubna.walt.DBQueryThread;

public class D1CQueryThread extends dubna.walt.DBQueryThread
{

public void startService() throws Exception
{ 
  System.out.println(cfgTuner.getParameter("uname") + ": " + cfgFileName);
  super.startService();
}


protected void finish()
{ 
  if (dbUtil != null)
  { dbUtil.close();
    dbUtil.release();
    dbUtil = null;
  }
  rm.removeKey("DBUtil");
  super.finish();
}



}