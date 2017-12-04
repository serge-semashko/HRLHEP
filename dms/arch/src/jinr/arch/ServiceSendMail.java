package jinr.arch;

import java.sql.*;
//import javax.sql.rowset.serial.*;
//import javax.servlet.*;
//import javax.servlet.http.*;
import java.io.*;
import dubna.walt.util.MD5;

import java.util.*;

import javax.mail.*;
import javax.mail.internet.*;
import javax.activation.*;
import javax.mail.util.*;

public class ServiceSendMail extends dubna.walt.service.Service
{
	boolean debugMode = false;
	boolean sendFiles = false;
	Vector file_names = null;

	public void start () throws Exception
	{
          
          debugMode = cfgTuner.getParameter("mailDebug").equals("true");
	  sendFiles = cfgTuner.getParameter("sendFiles").equals("true");
	  if (debugMode) System.out.println(">>> =================================");
		if (debugMode) System.out.println("===> ServiceSendMail:");
		if (sendFiles){
                    throw new Exception("files from BLOB are deprecated!");
		  //storeFiles();
                }
	  sendMails();
		cfgTuner.outCustomSection("report", out);
	}


	public void sendMails()
	{
		try
		{ 
		  Properties props = new Properties();
		  props.put( "mail.smtp.host", cfgTuner.getParameter("mailServer") );
		  props.put("mail.smtps.auth", "true");
	  if (debugMode) System.out.println("===> mailServer:" + cfgTuner.getParameter("mailServer"));
		if (debugMode) System.out.println("===> mail.smtp.host: " + props.get( "mail.smtp.host" ));
		  props.put( "mail.smtp.user", cfgTuner.getParameter("mailUser") );
	  if (debugMode) System.out.println("===> mailUser:" + cfgTuner.getParameter("mailUser" ));
		  Session session = Session.getDefaultInstance( props, null );
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
	
		  StringTokenizer to = new StringTokenizer(cfgTuner.getParameter("mailTo" ), ";");
/*			int numAddrs = to.countTokens();
		  InternetAddress[] address = new InternetAddress[numAddrs];
			for (int i=0; i<numAddrs; i++)
			{ address[i] = new InternetAddress( to.nextToken() );
			}
*/			
		  msg.setRecipients( Message.RecipientType.TO, parseAddreses(cfgTuner.getParameter("mailTo" )) );
//			msg.setRecipients( Message.RecipientType.TO, address );
			
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

		  if (sendFiles)
			{
				MimeBodyPart mbp2 = new MimeBodyPart();
				mbp2.setText( "Ñèñòåìà àâòîìàòè÷åñêîé ðàññûëêè äîêóìåíòîâ", charset );			
				mp.addBodyPart( mbp2 );
				/*============== Attachments =================*/
				if (file_names != null)
				{
					for (int i=0; i< file_names.size(); i++)
					{ String file_name = (String) file_names.elementAt(i);
						MimeBodyPart mbp3 = new MimeBodyPart();
						FileDataSource fds = new FileDataSource(file_name);
						mbp3.setDataHandler(new DataHandler(fds));
						mbp3.setFileName(fds.getName());
						mp.addBodyPart( mbp3 );
					}
				}
		  }
//			else

		  msg.setContent( mp );
//			Transport.send( msg );
 Transport transport = session.getTransport("smtps");
										 transport.connect(cfgTuner.getParameter("mailServer")
											, cfgTuner.getParameter("mailUser" ), cfgTuner.getParameter("mailUserPw" ));
										 transport.sendMessage(msg, msg.getAllRecipients());
										 transport.close();
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
				s="ÏÎ×ÒÎÂÛÉ ßÙÈÊ "+mex.toString().substring(mex.toString().indexOf("<")+1, mex.toString().indexOf(">")).toUpperCase()+" ÏÅÐÅÏÎËÍÅÍ!\\n\\rÑÎÎÁÙÅÍÈß ÍÅ ÐÀÇÎÑËÀÍÛ!";
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

	protected void storeFiles() throws Exception
	{ String sql = getSQL( "SQL" );
		oracle.sql.BLOB bl = null;
	  String file_name = "";
	  file_names = new Vector(5,5);
		try
		{
			ResultSet r = dbUtil.getResults( sql );
			while ( r.next() )
			{
				String destPath= cfgTuner.getParameter("tmpPath").replace('/','\\');
				file_name = destPath + r.getString( 1 );
			  file_names.add(file_name);
			  System.out.println( "file_name:" + file_name );
			  File f = null;
			  FileOutputStream lf = null;
			  f = new File(destPath);
			     if (!f.exists())
			       if (!f.mkdirs()) throw new Exception("Could not create destination directory");
			     lf = new FileOutputStream(file_name, false);
			     if (lf == null) throw new Exception("Could not write output file");
				//			sb = new SerialBlob( r.getBlob(2));
				bl = ( oracle.sql.BLOB ) r.getBlob( 2 );

				System.out.println( "BLOB=" + bl );
				System.out.println( "LENGTH=" + bl.length() );
				byte[] dat = bl.getBytes( 1, ( int ) bl.length() );
				//			byte[] dat = sb.getBytes(1, (int)sb.length());
				//	out.println("file_name:" + file_name + "<p>");
				lf.write( dat );
			  lf.flush();
			  lf.close();
			}
		  dbUtil.closeResultSet( r );
		}
		catch( Exception e )
		{
			e.printStackTrace( out );
		}
	}


}
