package jinr.pin;

import dubna.walt.Query;
import dubna.walt.util.ResourceManager;

import java.io.*;
import dubna.walt.util.*;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.fileupload.*;
import org.apache.commons.fileupload.servlet.*;
import org.apache.commons.fileupload.util.*;

public class UploadServlet extends dubna.walt.BasicServlet
{

public String clientEncoding = "Cp1251";

public ResourceManager obtainResourceManager() throws Exception
{
  System.out.println(".\n\r.\n\r.\n\r*** PIN Upload - INIT ...");
  ResourceManager rm = new ResourceManager("pin");
  return rm;
}

public void setResourceManager(ResourceManager rm)
{
  this.rm_Global = rm;
}


 public void doGet(HttpServletRequest request, HttpServletResponse res)
		 throws ServletException, IOException
 { 
 //  System.out.println("=== Do doGet works...");
 //  System.out.println("=== req:" + req);
	System.out.println("++++++++++++++ doGet - Start upload ++++++++++++++++++++++++++++++");
	boolean isMultipart = ServletFileUpload.isMultipartContent(request);
	ServletFileUpload upload = new ServletFileUpload();
	 ServletRequestContext rc = new ServletRequestContext(request);
	 System.out.println("ContentLength: " + rc.getContentLength());
	 System.out.println("CharacterEncoding: " + rc.getCharacterEncoding());

	 InputStream fileStream = null;
	int bufLen = 64000;
	byte[] buf = new byte[bufLen];
	try
	{ FileItemIterator iter = upload.getItemIterator(request);
		while (fileStream == null && iter.hasNext()) 
		{		FileItemStream item = iter.next();
				String name = item.getFieldName();
				InputStream stream = item.openStream();
				if (item.isFormField()) 
				{
						System.out.println("Form field " + name + " with value '" 
							+ getAsString(stream, buf, bufLen)  + "' detected.");
//								+ StreamUtil.asString(stream) + " detected.");
				} else 
				{
						System.out.println("File field " + name + " file name: '"
								+ item.getName() + "' ContentType:" + item.getContentType() );
				  fileStream=stream;
//				  copyFile(stream, "c:/tmp/1/", "Upload.dat");
						// Process the input stream
		//				...
				}
		}
		 copyFile(fileStream, "c:/tmp/1/", "Upload.dat");
	}
	catch (Exception e)
	{
		e.printStackTrace(System.out);
	}
//	 new Query(req, res, rm_Global, newQueryLabel(), this);
 }

public String getAsString(InputStream in, byte[] buf, int bufLen) throws Exception
{ String s = "";
	int numBytes = 1;
	int bufPos = 0;
	while (numBytes > 0)
	{ while (bufPos < bufLen && numBytes > 0)
		{ numBytes = in.read(buf, bufPos, bufLen-bufPos);
			if (numBytes > 0)
				bufPos += numBytes;
		}
		System.out.println("++++++++++++++++++++++++++ numBytes=" + bufPos + "; ");
		s += new String(buf, 0, bufPos, clientEncoding);
	}
	return s; 
}

 public static void copyFile(InputStream srcStream, String destPath, String destFileName) throws Exception
 { 
	System.out.println("+++ COPY FILE: "+ srcStream + " ==> "+ destPath + " " + destFileName);
	File f = null;
	FileOutputStream out = null;
	f = new File(sFilePath(destPath));
		 if (!f.exists())
			 if (!f.mkdirs()) throw new Exception("Could not create destination directory");
		 out = new FileOutputStream(destPath + destFileName, false);
		 if (out == null) throw new Exception("Could not write output file");
	BufferedInputStream lf = new BufferedInputStream(srcStream);
	int bufLen = 1000000;
	byte[] buf = new byte[bufLen];
	int numBytes = 1;
	int totNumBytes = 0;
	int bufPos = 0;
	while (numBytes > 0)
	{ 	while (bufPos < bufLen && numBytes > 0)
			{	numBytes = lf.read(buf, bufPos, bufLen-bufPos);
			  if (numBytes > 0)
				{	totNumBytes += numBytes;
					bufPos += numBytes;
				}
			}
		System.out.println("++++++++++++++++++++++++++ bufPos=" + bufPos + "; totNumBytes=" + totNumBytes);
		if (bufPos > 0)
		{ out.write(buf, 0, bufPos);
		  bufPos = 0;
		}
	}

	 out.flush();
		out.close();
	 lf.close();  
	 System.out.println("... DATA COPIED - OK!");

 //   System.out.println("... COPY FILE - OK! ");
 }
	 
	public static String sFilePath(String srcPath)
	{
		return (srcPath.replace('/', File.separatorChar)).replace('\\', File.separatorChar);
	}

	 
}