import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.URL;
import java.net.URLConnection;
import java.util.Enumeration;
import java.util.ResourceBundle;

public class Daemon
{

	public static void main ( String[] args )
	{
//	  ResourceBundle rb = ResourceBundle.getBundle("daemon");
	  System.out.println("======================");
//	  System.out.println("..... ALL PARAMETERS: ");
//	  Enumeration en = rb.getKeys(); 
//	  while (en.hasMoreElements())
//		{ String name = (String) en.nextElement();
//	    System.out.println(name + "='" + rb.getString(name) + "'");
//		}
		String link = args[0];
	  URL url;
		try
		{
		  System.out.println(link);
			url = new URL(link);
		  URLConnection yc = url.openConnection();
			BufferedReader in = new BufferedReader(
		                                  new InputStreamReader(
		                                  yc.getInputStream()));
			String inputLine;
      while ((inputLine = in.readLine()) != null) 
					System.out.println(inputLine);
		      in.close();		}
		catch( Exception e )
		{
			e.printStackTrace(System.out);
		}
		System.out.println("======================");
	}

}
