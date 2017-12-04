package jinr.pin;

import dubna.walt.service.Service;
import dubna.walt.util.ResourceManager;
import dubna.walt.util.Tuner;
import java.io.PrintStream;
import java.net.URLEncoder;
import javax.servlet.ServletContext;
import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletResponse;

public class ServiceData
  extends Service
{
  public void start()
    throws Exception
  {
    getDocData();
  }
  
  protected void getDocData()
    throws Exception
  {
    String sql = getSQL("SQL");
    String file_name = "";
    try
    {
      ServletOutputStream outStream = (ServletOutputStream)this.rm.getObject("outStream");
      
      file_name = URLEncoder.encode(this.cfgTuner.getParameter("fileName"), "UTF-8");
      String mime_type = this.cfgTuner.getParameter("mimeType");
      
      String ct = this.cfgTuner.getParameter("contentType");
      String content_type = "";
      
      HttpServlet serv = (HttpServlet)this.rm.getObject("Servlet");
      if ((mime_type == null) || (mime_type.length() < 2))
      {
        ServletContext sc = serv.getServletContext();
        content_type = sc.getMimeType(file_name.toLowerCase());
        if ((content_type == null) || (content_type.length() < 2)) {
          content_type = ct;
        }
      }
      else
      {
        content_type = mime_type;
      }
      System.out.println("file_name:" + file_name + "; MimeType:" + content_type);
      this.response.setContentType(content_type);
      if (!this.cfgTuner.enabledOption("inline=true")) {
        this.response.setHeader("Content-Disposition", "attachment; filename=" + file_name);
      }
      outStream.flush();
      
      String[] body = this.cfgTuner.getCustomSection("report");
      String outS = "";
      for (int i = 0; i < body.length; i++) {
          System.out.println(""+body[i]);
        outS = outS + body[i];
      }
      byte[] outB = outS.getBytes("cp1251");
      System.out.println("LENGTH=" + outB.length);
      this.response.setHeader("Content-length", Long.toString(outB.length));
      outStream.write(outB);
      
      outStream.flush();
      outStream.close();
    }
    catch (Exception e)
    {
      e.printStackTrace(this.out);
    }
  }
}
