package jinr.adb.evm;

import dubna.walt.util.Base64;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.HttpURLConnection;
import java.net.URL;
import javax.net.ssl.HttpsURLConnection;

// http://stackoverflow.com/questions/1051004/how-to-send-put-delete-http-request-in-httpurlconnection - PUT DELETE

public class ServiceEvmJson extends dubna.walt.service.Service
{


	public void start () throws Exception
	{
	  super.start();
	  String u = cfgTuner.getParameter( "url" );
//	  String authString = "nica:nica";
	  //      System.out.println( "auth string: " + authString );
//	  String authStringEnc = Base64.encode( authString );
//	  out.println( authString + "=> '" + authStringEnc + "'<br>" );

		try
		{
		  cfgTuner.outCustomSection("body", out);
		  String[] body = cfgTuner.getCustomSection("body");
		  int length = 0;
			if ( body != null && body.length > 0) {
				for (int i = 0; i< body.length; i++)
					length += body[i].length();
				cfgTuner.outCustomSection( "report header", out );
			}
		  String method = cfgTuner.getParameter( "method" );
//		  System.out.println( "method='" + method + "'" );
			if(u.indexOf(cfgTuner.getParameter("baseUrl")) == 0) 
			{
				URL url = new URL( u );
				HttpURLConnection conn;
				if (cfgTuner.getParameter("baseUrl").indexOf("https://") >= 0)
					conn = ( HttpsURLConnection ) url.openConnection();
				else
					conn = ( HttpURLConnection ) url.openConnection();
//				conn.setRequestProperty( "Authorization", "Basic " + authStringEnc );
				conn.setRequestProperty( "Connection", "keep-alive");
				conn.setRequestProperty( "Accept", "text/html,application/xhtml+xml,application/xml;q=0.9,*/*;q=0.8");
				conn.setRequestProperty( "User-Agent", "Mozilla/5.0 (Windows NT 6.1; WOW64; rv:29.0) Gecko/20100101 Firefox/29.0");	   
	
				if (!method.equals("GET")) 
				{
					conn.setRequestProperty( "User", "ADB2");
					conn.setRequestMethod(method);
					conn.setRequestProperty( "Content-Length", Integer.toString(length));
					conn.setDoOutput(true);
					conn.setRequestProperty( "Content-Type", "application/json; charset=UTF-8");
					if(length > 0) {
						PrintWriter pw = new PrintWriter(conn.getOutputStream());
						for (int i = 0; i< body.length; i++)
							pw.write(body[i]);
						pw.close();
					}
				}
				conn.connect();
				
				cfgTuner.addParameter("RESP_CODE", Integer.toString(conn.getResponseCode()));
				cfgTuner.addParameter("RESP_MSG", conn.getResponseMessage());
				BufferedReader in = new BufferedReader( new InputStreamReader( conn.getInputStream() ) );
	
				String inputLine;
				while( ( inputLine = in.readLine() ) != null )
				{
					System.out.println( inputLine );
					out.println( inputLine + "<br>" );
				}
				in.close();
			}
			else
			  out.println( "ERROR: '" + u + "'<br>" );
		}
		catch( Exception e )
		{
			e.printStackTrace( System.out );
			out.println( "<pre>" );
			e.printStackTrace( out );
			out.println( "</pre>" );
		}

	  cfgTuner.outCustomSection( "report footer", out );
		out.flush();

	}


}
