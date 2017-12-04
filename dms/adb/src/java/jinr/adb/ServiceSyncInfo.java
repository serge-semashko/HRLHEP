package jinr.adb;

import dubna.walt.util.*;

public class ServiceSyncInfo extends dubna.walt.service.Service
{

public void beforeStart() throws Exception
{ 
  dbUtil = ServiceDocSend.makeDBUtilLink(rm);
  super.beforeStart();
  dbUtil.release();
  dbUtil = (DBUtil) rm.getObject("DBUtil", true);

}

public void XXXstart() throws Exception
{ 
  super.start();

}



}