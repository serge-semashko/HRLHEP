package cern.kpi.viewdb;

import java.io.PrintWriter;
import java.io.FileOutputStream;

import java.util.Properties;
import javax.mail.*;
import javax.mail.internet.*;
import java.sql.*;

import dubna.walt.util.*;

public class KillSessService extends dubna.walt.service.Service 
{

public void start() throws Exception
{
  try
  { if (cfgTuner.enabledOption("kills"))
    { if (cfgTuner.enabledOption("getPreDataError"))
      { /* Remove the detailed error message for all exept DBAs */
        if (!cfgTuner.enabledOption("admin=ACC_RIGHTS"))
        { String s = cfgTuner.getParameter("getPreDataError");
          int i = s.indexOf("SQL:");
          s = s.substring(0, i+1);
          cfgTuner.addParameter("getPreDataError", s);
        }
      } 
      else
      { String[] msg = cfgTuner.getCustomSection("mail body");
        String body = "";
        for (int i=0; i<msg.length; i++)
          body = body + msg[i] + "\r";
        String subject = cfgTuner.getParameter("subject");
        
        String addrs = getDBAAdresses(rm);
        if (addrs.length() > 0)
          KillSessService.sendMail(rm.getString("mailserver")
              , rm.getString("mail_from"), addrs, subject, body);
              
        logAction(rm, body);
        cfgTuner.addParameter("kills","done");
      }
    }
  } catch (Exception e)
  { System.out.println("!!!!!!! KillSessService :" + e.toString());
    e.printStackTrace(System.out);
    cfgTuner.addParameter("kills","error");
    cfgTuner.addParameter("ERROR", e.toString());
  }
  cfgTuner.outCustomSection("report", out);
}

public static void logAction(ResourceManager rm, String body)
{  try 
   { String logFileName = new String(rm.getString("killsLogFileName"));
     if (logFileName.length() > 0)
     { FileOutputStream lf = new FileOutputStream(logFileName,true);
       if (lf == null) return;
       PrintWriter pw = new PrintWriter(lf);
       String s = StrUtil.replaceInString(body,"\r\r","\r");
       s = StrUtil.replaceInString(s,"\r","\r\n");
       s = StrUtil.replaceInString(s,"\n\n","\n");
       pw.print(s + "==================================================\r\n");
       pw.flush(); pw=null; lf.close();
     }
   }
   catch (Exception ex) 
   { System.out.println(ex.toString());
   } 
}

public static String getDBAAdresses(ResourceManager rm) throws Exception
{ String addrs = rm.getString("killSessMailTo", false);
  if (addrs.length() < 1)
  { DBUtil dbRepos = QueryThread.connectToRepository(rm);
    if (dbRepos != null)
    { ResultSet r = dbRepos.getResults(
        "select ','||EMAIL from users where USERID in (select USERID from USER_TO_GROUP	where GROUPID=1)");
      addrs = "";
      while (r.next())
        addrs += r.getString(1);
      dbRepos.closeResultSet(r);
      if (addrs.length() > 2) 
        addrs=addrs.substring(1).replace( ',', ';');
    }
  }
  return addrs;
}

public static void sendMail(String mailserver, String from, String to, String sbj, String msg_txt)
{
/*
   Properties props = new Properties();
//   props.put("mail.smtp.host", "smtp.cern.ch");
   props.put("mail.smtp.host", mailserver);
System.out.println("++++ mailserver: " +  mailserver + "; from:" + from + ";");
System.out.println("++++ to:" + to);
System.out.println("++++ sbj: " +  sbj);
System.out.println("++++ msg_txt: " +  msg_txt);
/**/
/*
   Session session = Session.getDefaultInstance(props, null);
 
   try
   { MimeMessage msg = new MimeMessage(session);
     msg.setFrom(new InternetAddress(from));
//     msg.setFrom(new InternetAddress("SpotDB <serguei.kouniaev@cern.ch>"));
//     InternetAddress[] address = {new InternetAddress(to)};
//     InternetAddress[] address = {new InternetAddress("serguei.kouniaev@cern.ch"), new InternetAddress("john.ferguson@cern.ch"), new InternetAddress("jurgen.de.jonghe@cern.ch")};
     String[] addrs = StrUtil.splitStr(to,';');
     int numAddrs = 0;
     for (int i = 0; i< addrs.length; i++)
     {  //System.out.println(i + ": '" + addrs[i]+"'");
        try
        {  InternetAddress dummy = new InternetAddress(addrs[i]);
           numAddrs++;
        }
        catch (Exception e)
        { System.out.println("Invalid e-mail address: '" + addrs[i]+"'");
          addrs[i] = "";
        }
     }
     InternetAddress[] address = new InternetAddress[numAddrs];
     int j = 0;
     for (int i = 0; i< addrs.length; i++)
     {  // System.out.println(i + ": '" + addrs[i]+"'");
        if (addrs[i].length() > 0)
          address[j++] = new InternetAddress(addrs[i]);
     }
     msg.setRecipients(Message.RecipientType.TO, address);
     msg.setSubject(sbj);
 
     // create and fill the first message part
     MimeBodyPart mbp1 = new MimeBodyPart();
 
     mbp1.setText(msg_txt);
 
     // create the second message part
//     MimeBodyPart mbp2 = new MimeBodyPart();
 
  // attach the file to the message
  //   FileDataSource fds = new FileDataSource(fileName);
  //   mbp2.setDataHandler(new DataHandler(fds));
  //   mbp2.setFileName(fds.getName());
 
     // create the Multipart and its parts to it
     Multipart mp = new MimeMultipart();
     mp.addBodyPart(mbp1);
//     mp.addBodyPart(mbp2);
 
     // add the Multipart to the message
     msg.setContent(mp);
 
     // set the Date: header
     msg.setSentDate(new java.util.Date());
 
     // send the message
     Transport.send(msg);
   }
    catch (Exception mex)
    {
     mex.printStackTrace(System.out);
   }
 */
  }
}