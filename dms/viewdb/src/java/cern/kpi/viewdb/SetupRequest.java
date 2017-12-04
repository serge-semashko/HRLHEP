package cern.kpi.viewdb;


public class SetupRequest extends dubna.walt.service.Service
{
 
public void beforeStart() throws Exception
{ 
  if (cfgTuner.enabledOption("send"))
  { cfgTuner.addParameter("done","no");
    try
    { String[] msg = cfgTuner.getCustomSection("mail body");
      String body = "";
      for (int i=0; i<msg.length; i++)
        body = body + msg[i] + "\r";
        
      String subject = cfgTuner.getParameter("subject");
      String addrs = cfgTuner.getParameter("setupMailTo");
      
      if (addrs.length() > 0)
      { KillSessService.sendMail(rm.getString("mailserver")
        , rm.getString("mail_from"), addrs, subject, body);
        cfgTuner.addParameter("done","yes");
      }
    }
    catch (Exception e)
    { cfgTuner.addParameter("ERROR", e.getMessage());
    }
  }
    
}

}