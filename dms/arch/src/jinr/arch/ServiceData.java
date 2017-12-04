package jinr.arch;

import java.sql.*;
import javax.servlet.*;
import javax.servlet.http.*;
import java.net.URLEncoder;
public class ServiceData extends dubna.walt.service.Service
{

public void start() throws Exception
{
	cfgTuner.getCustomSection("report");
	getDocData();
//  cfgTuner.addParameter("MORE_LINKS", after_tree);
}


protected void getDocData() throws Exception
{ // String sql = "select file_name, content_type, page_data from doc_data where doc_id=1 and page_nr=1";
	String sql = getSQL ("SQL");
	String file_name = "";
//	SerialBlob sb = null;
  oracle.sql.BLOB bl = null;
//	System.out.println("++++++++++++++++");
	try
	{
	  ServletOutputStream outStream = (ServletOutputStream)rm.getObject("outStream");
		ResultSet r = dbUtil.getResults(sql);
		if (r.next())
		{ file_name = URLEncoder.encode(r.getString(1).trim(),"UTF-8");
//			sb = new SerialBlob( r.getBlob(2));
		  bl = (oracle.sql.BLOB) r.getBlob(2);
		  String mime_type = r.getString(3);
		  String ct = r.getString(4);
		  String content_type = "";
			dbUtil.closeResultSet(r); 
		
			HttpServlet serv = (HttpServlet) rm.getObject("Servlet");
			if (mime_type== null || mime_type.length() < 2)
			{	ServletContext sc = serv.getServletContext();
				content_type = sc.getMimeType(file_name.toLowerCase());
			  if (content_type== null || content_type.length() < 2)
			    content_type = ct;
			}
			else
			{ content_type = mime_type;
			}
//		  System.out.println("file_name:" + file_name + "; MimeType:" + content_type);
			response.setContentType(content_type);
			if (!cfgTuner.enabledOption("inline=true"))
				response.setHeader ("Content-Disposition", "attachment; filename=" + file_name);
		  response.setHeader ("Content-length", Long.toString(bl.length()));
			outStream.flush();
		//	out.flush();
		
//		 System.out.println("BLOB=" + bl);
//		  System.out.println("LENGTH=" + bl.length());
		 byte[] dat = bl.getBytes(1, (int)bl.length());
//			byte[] dat = sb.getBytes(1, (int)sb.length());
		//	out.println("file_name:" + file_name + "<p>");
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