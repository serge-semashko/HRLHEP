package jinr.arch;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.sql.*;
import javax.servlet.*;
import javax.servlet.http.*;
import java.net.URLEncoder;
import java.util.HashMap;
public class ServiceFSData extends dubna.walt.service.Service
{
public static final boolean useBlob = false;
public void start() throws Exception
{
	cfgTuner.getCustomSection("report");
	getDocData();
//  cfgTuner.addParameter("MORE_LINKS", after_tree);
}

public static byte[] file2bytearray(File file) throws IOException {

    ByteArrayOutputStream ous = null;
    InputStream ios = null;
    try {
        byte[] buffer = new byte[4096];
        ous = new ByteArrayOutputStream();
        ios = new FileInputStream(file);
        int read = 0;
        while ( (read = ios.read(buffer)) != -1 ) {
            ous.write(buffer, 0, read);
        }
    } finally { 
        try {
             if ( ous != null ) 
                 ous.close();
        } catch ( IOException e) {
        }

        try {
             if ( ios != null ) 
                  ios.close();
        } catch ( IOException e) {
        }
    }
    return ous.toByteArray();
}

        
        
protected void getDocData() throws Exception
{ 
	String sql = getSQL ("SQL");
	String file_name = "";
  oracle.sql.BLOB bl = null;
	try
	{
	  ServletOutputStream outStream = (ServletOutputStream)rm.getObject("outStream");
		ResultSet r = dbUtil.getResults(sql);
                
		if (r.next())
		{ file_name = URLEncoder.encode(r.getString(1).trim(),"UTF-8");
		  
		  String mime_type = r.getString("MIME_TYPE");
		  String ct = r.getString("CONTENT_TYPE");
                  String fsFileName = r.getString("FS_FILE_NAME");
		  String content_type = "";
			
		
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
			response.setContentType(content_type);
			if (!cfgTuner.enabledOption("inline=true"))
				response.setHeader ("Content-Disposition", "attachment; filename=" + file_name);
                byte[] dat = null;   
                String fsPath = cfgTuner.getParameter("file_storage_path");
                String docId = cfgTuner.getParameter("doc_id");
                String fileLength = null;
                if(fsPath!=null && !fsPath.isEmpty()
                        && fsFileName!=null && !fsFileName.isEmpty()
                        && docId!=null && !docId.isEmpty()){
                    File file = new File(fsPath+fsFileName);
                    fileLength = Long.toString(file.length());
                    if(file.exists()){
                        dat = file2bytearray(file);
                    }
//                    else if(useBlob){
//                        //Запись о файле есть, но файла нет - берём блоб
//                        // потом удалить работу с BLOB, а так же удалить из запроса в getDocData.cfg
//                        bl = (oracle.sql.BLOB) r.getBlob(2);
//                        fileLength = Long.toString(bl.length());
//                        dat = bl.getBytes(1, (int)bl.length());
//                    }
                }
//                else if(useBlob){
//                    //Записи о файле нет - берём блоб
//                    // потом удалить работу с BLOB, а так же удалить из запроса в getDocData.cfg
//                    bl = (oracle.sql.BLOB) r.getBlob(2);
//                    fileLength = Long.toString(bl.length());
//                    dat = bl.getBytes(1, (int)bl.length());
//                }
                dbUtil.closeResultSet(r); 
                response.setHeader ("Content-length", fileLength);
        	outStream.flush();
                if(dat!=null) outStream.write(dat);
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