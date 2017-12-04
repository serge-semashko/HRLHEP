package cern.kpi.viewdb;

public class UserConnectionService extends dubna.walt.service.Service
{

public void beforeStart() throws Exception
{ dbUtil.release();

  dbUtil = LoginCustom.getCustomDBUtil(rm, cfgTuner.getParameter("db")
     , cfgTuner.getParameter("orauser")
     , cfgTuner.getParameter("loginName"));

//  System.out.println( "dbUser=" + dbUtil + "; CONNECTED=" + cfgTuner.getFinalParameter("CONNECTED"));

  if ( dbUtil != null)
    super.beforeStart();
}

}