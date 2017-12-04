package jinr.arch.upload;

import dubna.walt.util.ResourceManager;
import java.io.*;
import javax.servlet.*;
import javax.servlet.http.*;
import dubna.walt.util.ResourceManager;

public class UploadServlet extends HttpServlet 
{     
  public Listener listener = null;
	private ResourceManager rm = null;

public void init(ServletConfig config) throws ServletException 
{
	super.init(config);
	System.out.print("*** UploadServlet - INIT ...");
	try
	{	rm = new ResourceManager("arch");
		rm.setObject("Servlet",this);
	}
	catch (Exception e)
	{ e.printStackTrace(System.out);
	}
}    

	public void destroy() 
	{
		listener.stop();
		listener = null;
	}
	
	protected void doGet(HttpServletRequest request, HttpServletResponse response)
	throws ServletException, IOException {  

			System.out.println("Upload Servlet is here!");
			response.setContentType("text/html");                      
			PrintWriter out = response.getWriter();  
			out.println("Upload Servlet<br>");        
			try{
					if(listener == null)
					{   listener = new Listener(rm);
							System.out.println("Listener created");
							Thread thread = new Thread( listener );
							thread.start();
							out.println("Listener started");        
					}
			}catch(Exception e)
			{ System.out.println("============= Upload Servlet ==============");
				e.printStackTrace(System.out);
				e.printStackTrace(out);
			}
			out.flush();
			out.close();
	}    
 }

