package cern.kpi.viewdb;

import java.io.*;
//import java.util.*;
import java.sql.*;
import javax.servlet.http.*;

import dubna.walt.util.*;

public class TesterQueryThread
{

protected ResourceManager rm;

public TesterQueryThread( HttpServletRequest req,
                  HttpServletResponse res,
                  ResourceManager rm) throws Exception
{
  this.rm = rm;
  String db = req.getParameter("db");
//    System.out.println("........... TesterQueryThread - db:'" + db + "'");
  OutputStream out=null;
  try { out = res.getOutputStream();  }
  catch (Exception e) { return; }

  try
  { DriverManager.setLoginTimeout(2);
    Connection conn = DriverManager.getConnection(
          rm.getString("driverType", true) + db
        , rm.getString("usr")
        , rm.getString("pwd"));
    outImage("ok.gif", out);
    conn.close();
  } 
  catch (Exception e)
  { //    System.out.println(e.getMessage());
    if (e.getMessage().indexOf("invalid username/password")>=0)
      outImage("alert.gif", out);
    else
      outImage("stop.gif", out);
  }
}

public void outImage(String fn, OutputStream out) throws Exception
{ if (out == null) return;  
  DataBuffer dbuf = (DataBuffer) rm.getObject("dbuf_" + fn, false);
  if (dbuf == null)
    dbuf = new DataBuffer();
  else
  { dbuf.outData(out);
    out.close();
    return;
  }
  
  InputStream inp = IOUtil.getResourceAsInputStream(fn, "viewdb_images.jar");
  String imgPath = rm.getString("imgPath");
  if (inp == null) inp = IOUtil.getResourceAsInputStream(imgPath + fn, "viewdb_images.jar");
  if (inp == null) inp = IOUtil.getResourceAsInputStream(fn, ".jar");
  if (inp == null) inp = IOUtil.getResourceAsInputStream(imgPath + fn, ".jar");
  if (inp == null)
  { System.out.println("++++++ could not find imgFile '" + fn +"'");
    out.close();
    return;
  }
  byte[] buf = new byte[1024];
//  byte[] buf = new byte[64];
  int l = -1;
  while ( (l = inp.read(buf)) > 0)
  { out.write(buf, 0, l);
    dbuf.addData(buf,l);
  }
  out.close();
  rm.setObject("dbuf_" + fn, dbuf, true);
}

}

