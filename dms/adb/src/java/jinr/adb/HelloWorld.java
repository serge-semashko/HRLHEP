package jinr.adb;
import javax.servlet.*;
import javax.servlet.http.*;
import java.io.*;

public class HelloWorld  extends HttpServlet
{
  public void doGet(HttpServletRequest req, HttpServletResponse res)
    throws ServletException, IOException
{ 
  try 
  {
  OutputStream outStream = res.getOutputStream();
  PrintWriter  out = new PrintWriter( new OutputStreamWriter(outStream, "Cp1251") );
  out.println("Hello! World");
  out.close();
  } catch (Exception e) {}
}

}