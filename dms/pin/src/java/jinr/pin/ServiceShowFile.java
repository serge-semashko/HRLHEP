package jinr.pin;

import java.sql.*;
import javax.sql.rowset.serial.*;
//import javax.servlet.*;
//import javax.servlet.http.*;
//import java.io.*;
import dubna.walt.util.StrUtil;
import javax.servlet.ServletContext;
import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServlet;

public class ServiceShowFile extends dubna.walt.service.Service
{

	public void start() throws Exception
	{
		getDocData();
	//  cfgTuner.addParameter("MORE_LINKS", after_tree);
	}


	protected void getDocData() throws Exception
	{ 
		String sql = getSQL ("SQL");
		String file_name = "";
//  SerialBlob sb = null;
//		oracle.sql.BLOB bl = null;
	  java.sql.Blob bl = null;
//		System.out.println("+++++++++++++++++++ Sytart reading file data +++++++++++++++++++");
    long tm = System.currentTimeMillis();
		try
		{
			ServletOutputStream outStream = (ServletOutputStream)rm.getObject("outStream");
			ResultSet r = dbUtil.getResults(sql);
			if (r.next())
			{ file_name = r.getString(1);
	      bl = new SerialBlob( r.getBlob(2));
//				bl = (oracle.sql.BLOB) r.getBlob(2);
				dbUtil.closeResultSet(r); 
			  long t1 = System.currentTimeMillis() - tm;
//				System.out.println("+++ file_name:" + file_name);
//			  System.out.println("+++ got blob " + Long.toString(bl.length()/1024) + "KB - " + t1 + "ms.");
				HttpServlet serv = (HttpServlet) rm.getObject("Servlet");
				ServletContext sc = serv.getServletContext();
				String content_type = sc.getMimeType(file_name.toLowerCase());
//					System.out.println("getMimeType:" + content_type);
				response.setContentType(content_type);
				response.setHeader ("Content-Disposition", "attachment; filename=" + file_name);
				response.setHeader ("Content-length", Long.toString(bl.length()));
			  t1 = System.currentTimeMillis() - tm;
//			  System.out.println("+++ FLUSHED " + t1);
			  outStream.flush();
			//  out.flush();
			
//			 System.out.println("BLOB=" + bl + "; LENGTH=" + bl.length());
			 byte[] dat = bl.getBytes(1, (int)bl.length());
	//      byte[] dat = sb.getBytes(1, (int)sb.length());
			//  out.println("file_name:" + file_name + "<p>");
					outStream.write(dat);
			}
			outStream.flush();
			outStream.close();
		}
		catch (Exception e) 
		{
			e.printStackTrace(out);
		}
	}

}