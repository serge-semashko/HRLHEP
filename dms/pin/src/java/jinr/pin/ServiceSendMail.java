package jinr.pin;

//import java.sql.*;
//import javax.sql.rowset.serial.*;
//import javax.servlet.*;
//import javax.servlet.http.*;
//import java.io.*;
//import dubna.walt.util.MD5;

import java.util.*;

import javax.mail.*;
import javax.mail.internet.*;
import javax.activation.*;
import javax.mail.util.*;
import javax.mail.Authenticator;
import javax.mail.PasswordAuthentication;

public class ServiceSendMail extends dubna.walt.service.Service
{
	boolean debugMode = false;
	boolean sendFiles = true;
	Vector file_names = null;

	public void start () throws Exception
	{	debugMode = cfgTuner.getParameter("mailDebug").equals("true");
	  sendFiles = cfgTuner.getParameter("sendFiles").equals("true");
	  if (debugMode) System.out.println(">>> =================================");
		if (debugMode) System.out.println("===> ServiceSendMail:");
	  sendMails();
		cfgTuner.outCustomSection("report", out);
	}


	public void sendMails()
	{
		try
		{ 
		  Properties props = new Properties();
		  props.put( "mail.smtp.host", cfgTuner.getParameter("mailServer") );
	  if (debugMode) System.out.println("===> mailServer:" + cfgTuner.getParameter("mailServer"));
		if (debugMode) System.out.println("===> mail.smtp.host: " + props.get( "mail.smtp.host" ));
		  props.put( "mail.smtp.user", cfgTuner.getParameter("mailUser") );
	  if (debugMode) System.out.println("===> mailUser:" + cfgTuner.getParameter("mailUser" ));
			props.put("mail.smtp.auth", "true");
			Authenticator auth = new SMTPAuthenticator();
		  Session session = Session.getDefaultInstance(props, auth);
//		  Session session = Session.getDefaultInstance( props, null );
		  session.setDebug( cfgTuner.getParameter("mailDebug").equals("true") );
			MimeMessage msg = new MimeMessage( session );
		  msg.setSentDate( new java.util.Date() );
		  String charset = cfgTuner.getParameter("mailCharset");
		  if (charset.length() < 2) charset = "windows-1251";
//		  if (charset.length() < 2) charset = "koi8-r";
		  msg.setHeader("charset",charset);
			
		  /*============== ADDRS =================*/
   if (debugMode) System.out.println("===> mailFrom:" + cfgTuner.getParameter("mailFrom" ));
		  if (cfgTuner.enabledOption("mailFrom"))
			  msg.setFrom( new InternetAddress( cfgTuner.getParameter("mailFrom") ) );
	
		  msg.setRecipients( Message.RecipientType.TO, parseAddreses(cfgTuner.getParameter("mailTo" )) );
			
			/*============== Subject & BODY =================*/
			 msg.setSubject( cfgTuner.getParameter("subject"), charset );
	  if (debugMode) System.out.println("===> subject:" + cfgTuner.getParameter("subject" ));

			 String[] body = cfgTuner.getCustomSection("msgBody");
			 String m_body = "";
			 for (int i=0; i<body.length; i++)
			 { m_body += body[i] + "\n";
			 }
			MimeBodyPart mbp1 = new MimeBodyPart();
			if (cfgTuner.enabledExpression("mailHTML"))
			  mbp1.setDataHandler( new DataHandler(new ByteArrayDataSource(m_body, "text/html; charset=" + charset)));
			else
				mbp1.setText( m_body , charset);

		  Multipart mp = new MimeMultipart();
		  mp.addBodyPart( mbp1 );

		  msg.setContent( mp );
			Transport.send( msg );
		}
		catch( Exception mex )
		{	mex.printStackTrace(System.out);
		  if (mex instanceof javax.mail.MessagingException)
			{	Exception ex = ((MessagingException) mex).getNextException();
				if( ex != null )
					ex.printStackTrace(System.out);
			}
		  
		cfgTuner.addParameter("MAIL_SEND_ERROR",mex.toString());
			if (mex.toString().contains("mailbox full")){
				String s= new String();
				s="ПОЧТОВЫЙ ЯЩИК "+mex.toString().substring(mex.toString().indexOf("<")+1, mex.toString().indexOf(">")).toUpperCase()+" ПЕРЕПОЛНЕН!\\n\\rСООБЩЕНИЯ НЕ РАЗОСЛАНЫ!";
				cfgTuner.addParameter("ERROR",s);		
				}
			else{
				cfgTuner.addParameter("ERROR",mex.toString().replace("\n", " "));
			}
		}

	}

	protected InternetAddress[] parseAddreses(String addrs) throws AddressException
	{ 
	if (debugMode) System.out.println("===> addrs:" + addrs);
		if (addrs == null || addrs.length() < 3) return null;
		StringTokenizer to = new StringTokenizer(addrs, ";");
		int numAddrs = to.countTokens();
		InternetAddress[] address = new InternetAddress[numAddrs];
		for (int i=0; i<numAddrs; i++)
		{ address[i] = new InternetAddress( to.nextToken() );
		}
		return address;
	}


private class SMTPAuthenticator extends javax.mail.Authenticator {
        public PasswordAuthentication getPasswordAuthentication() {
           return new PasswordAuthentication("pin", "8XUm1W5h");
        }
    }
		
}		