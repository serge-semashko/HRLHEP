package ru.jinr.document;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintWriter;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
// http://127.0.0.1:8080/converter/get?id=678&name=f.png
public class Uploader extends HttpServlet {
	public final static String DOC_ID = "id";
	public final static String DOC_NAME = "name";
	public final static String FIRST_FILE_NAME = "f";
	static final String servletName = "Uploader";
	private String defaultOutDir = null;
	public void doPost(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {
			doGet(request, response);
	}
	public void doGet(HttpServletRequest request_old, HttpServletResponse response)
		    throws ServletException, IOException {
		//PrintWriter out = response.getWriter();
		String id = request_old.getParameter(DOC_ID);
		String name = request_old.getParameter(DOC_NAME);
		String inpF = defaultOutDir+File.separator+id+File.separator+name;
		Utils.printMessage("Got TakePage request: "+request_old.getQueryString(),id,servletName,1);
		if(!(new File(inpF).exists())){
			Utils.printMessage("File not found.",id,servletName,0);
			return;
		}
		response.setContentType("image/gif");  //НЕ КОРРЕКТНО, но вроде пока работает (С.К.)
		response.setContentLength((int)(new File(inpF).length()));
		byte[] buffer = new byte[10240];
		InputStream input = null;
		OutputStream output = null;
		try {
			input = new FileInputStream(inpF);
			output = response.getOutputStream();
		    for (int length = 0; (length = input.read(buffer)) > 0;) {
		        output.write(buffer, 0, length);
		    }
		    input.close();
		    input = null;
		    output.flush();
		    Utils.printMessage("File " + inpF + " uploaded.",id,servletName,1);
		}
		catch(Exception ex){
			Utils.printMessage(ex.getMessage(),id,servletName,0);
			if(input!=null)
				input.close();
		}
	}
	public void init() throws ServletException {
		super.init();
		defaultOutDir = getInitParameter("defaultOutDir");
	}
}
