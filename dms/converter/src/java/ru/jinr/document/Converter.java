package ru.jinr.document;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.nio.file.Files;

import javax.net.ssl.HttpsURLConnection;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import java.util.concurrent.ThreadLocalRandom;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;

import ru.jinr.document.ext.PDF2Raster;

public class Converter extends HttpServlet{
	public final static String TITLE = "title";
	public final static String DOC_URL = "url";
	public final static String URL_UPLOAD = "urlOut";
	public final static String DOC_ID = "id";
	public final static String WIDTH = "width";
	public final static String DOC_SIZE = "size";
	public final static String USE_PDF = "usePDF";
//	public final static String OUTPUT_FILE = "output_file";
//	public final static String OUTPUT_DIR = "output_dir";
	public final static String TYPE = "type";
	public final static String FIRST_FILE_NAME = "f";
	
	public final static int FI_GIF=1503;
	public final static int FI_JPEG2000=1646;
	public final static int FI_JPEGFIF=1535;
	public final static int FI_PNG=1574;
	public final static int FI_BMP=1500;
	public final static int FI_TIFF=1501;
	
	static final int FI_PDF=1557;
	static final int FI_PDFIMAGE=1609;
    static final int FI_PDFA=1655;  /* Used exclusively by PDF Export as an output type */
    static final int FI_PDFA_2=1656;  /* Used exclusively by PDF Export as an output type */

	
	public static final int MSOFFICE_DOC = 0;
	public static final int MSOFFICE_XLS = 2;
	public static final int MSOFFICE_PPT = 1;

//	private JNIBase jni = null;
	private String dllPath = null;
	private String dllPDFPath = null;
	private String dllPDF2RasterPath = null;
	private String dllLogPath = null;
	private String defaultOutDir = null;
	private int logLevel = 0;
	private int maxTimeout = 30;//minute 
	private int defaultType = FI_TIFF;
//	private String getDocumentUrlBase = null;
	
	static final String servletName = "Converter";
	static final String dllName = "Dll";
	
	private PDF2Raster converter = null;

	
	public void doPost(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {
			doGet(request, response);
	}
	

//C:\Users\evgeny\Documents\2\AtlasGeomDB.doc
//C:\Users\evgeny\Documents\tmp4replace\1
	//http://127.0.0.1:8080/converter/convert?input_file=C:\Users\evgeny\Documents\2\AtlasGeomDB.doc&output_file=C:\Users\evgeny\Documents\tmp4replace\1
	//http://127.0.0.1:8080/converter/convert?id=678    679
	//http://127.0.0.1:8080/converter/convert?id=678&url=http%3A%2F%2Flt-a3.jinr.ru%2Fsed%2Fdubna%3Fc%3Ddocs%2Fdoc_files_dnld%26id%3D678
	//http://lt-a3.jinr.ru/sed/dubna?c=docs/doc_files_dnld&id=678
	public void doGet(HttpServletRequest request_old, HttpServletResponse response)
		    throws ServletException, IOException {
		PrintWriter out = response.getWriter();
		//String urlTmp = "http://lt-a3.jinr.ru/sed/dubna?c=docs/doc_files_dnld&id=678";
		//String urlEnc = URLEncoder.encode(urlTmp, "utf-8");
		String inputTitle = request_old.getParameter(TITLE);
		String urlS = request_old.getParameter(DOC_URL);
		String urlU = request_old.getParameter(URL_UPLOAD);
		String id = request_old.getParameter(DOC_ID);
//		String outputS = request_old.getParameter(OUTPUT_FILE);
		String outputDir= null;//request_old.getParameter(OUTPUT_DIR);
		String typeS = request_old.getParameter(TYPE);
		String sWidth = request_old.getParameter(WIDTH);
		String sdocSize = request_old.getParameter(DOC_SIZE);
		String usePDF = request_old.getParameter(USE_PDF);
		String ext = null;
		if(inputTitle!=null && inputTitle.length()>0)
			ext=Utils.extractFileExt(inputTitle);
		//String tmp_id=generateDocId();
    
    System.out.println("");System.out.println("_________________________");
    System.out.println("[" + Fmt.shortDateStr(new java.util.Date()) + "] " + request_old.getRemoteAddr());
		Utils.printMessage("Request: "+request_old.getQueryString(),id,servletName,1);
		int width=0;
		int type = defaultType;
		if(urlS ==null || urlS.length()==0){
			Utils.printMessage("Error in parameter url: "+urlS,id,servletName,0);
			out.println("Error in parameter url: "+urlS);
			return;
		}
		else{
			if(!urlS.contains("id=")){
				if(id==null || id.length()==0){
					Utils.printMessage("Id not found for url parameter: "+urlS,id,servletName,0);
					out.println("Id not found for url parameter: "+urlS);
					return;					
				}
				urlS+="&id="+id;
			}
		}
		if(urlU ==null || urlU.length()==0){
			Utils.printMessage("Error in parameter url: "+urlS,id,servletName,0);
			out.println("Error in parameter urlOut: "+urlU);
			urlU=null;
			//return;
		}
		if(id==null || id.length()==0){
			id = new Integer(ThreadLocalRandom.current().nextInt(0, Integer.MAX_VALUE)).toString();
		}
		
		if(outputDir ==null || outputDir.length()==0){
			outputDir = defaultOutDir;
		}
		if(inputTitle ==null || inputTitle.length()==0){
			inputTitle="test.doc";
		}
		
//		String urlS=getDocumentUrlBase+"&id="+id;
		long fSize = 0;
		if(sdocSize !=null && sdocSize.length()>0){
			try{
				fSize = Long.parseLong(sdocSize);
			}catch(Exception ex){
				Utils.printMessage("Parameter of size file is wrong",id,servletName,0);
				Utils.printMessage(ex.getMessage(),id,servletName,0);
				out.println("Parameter of size file is wrong");
			}						
		}		
		URL url;
		String inpF = outputDir+File.separator+id;
		String outF = outputDir+File.separator+id;
		try{
			url= new URL(urlS);
			if(!new File(inpF).exists())
				new File(inpF).mkdirs();
			inpF+=File.separator+inputTitle;
			FileUtils.copyURLToFile(url, new File(inpF));
			Utils.printMessage("SRC file downloaded to "+inpF,id,servletName,2);
                        long gotSize = Files.size(new File(inpF).toPath());
			if(gotSize!=fSize){
				Utils.printMessage("Wrong file size: expected " + fSize + " got " + gotSize ,id,servletName,0);
				out.println("Wrong file size: expected " + fSize + " got " + gotSize);
				return;				
			}
		}
		catch(Exception ex){
			Utils.printMessage(ex.getMessage(),id,servletName,0);
			out.println(ex.getMessage());
			return;
		}
		if(sWidth !=null && sWidth.length()>0){
			try{
				width = Integer.parseInt(sWidth);
			}catch(Exception ex){
				Utils.printMessage(ex.getMessage(),id,servletName,0);
			}						
		}

		
//		if(outputS ==null || outputS.length()==0){
//			outputS = Utils.extractFileNameWithoutExt(inputTitle);
//		}
		if(typeS !=null && typeS.length()>0){
			try{
				type = Integer.parseInt(typeS);
			}catch(Exception ex){
				Utils.printMessage(ex.getMessage(),id,servletName,0);
			}			
		}
//		if(!new File(outF).exists())
//			new File(outF).mkdirs();
		//out.println("File converte started");
		//out.flush();
		//out.close();
		//convertDocument(inpF,outF+".pdf",1655,0);//pdfa
		boolean resConv = false;
		String pdfOK = "not created";
		if("pdf".equalsIgnoreCase(ext)){
			Utils.printMessage("File already PDF",id,servletName,2);
		}
		else{
			if("doc".equalsIgnoreCase(ext)||"docx".equalsIgnoreCase(ext)){
				resConv=converter.convertDocument2PDF(inpF,outF+File.separator+FIRST_FILE_NAME+".pdf",MSOFFICE_DOC,id);
				if(resConv){
					new File(inpF).delete();
					Utils.printMessage("Converted to pdf using MSOConverter",id,servletName,2);
					inpF=outF+File.separator+FIRST_FILE_NAME+".pdf";
                                        pdfOK="OK (MSO)";
				}
			}
			if(!resConv){
				resConv = convertDocumentPdf(inpF,outF+File.separator+FIRST_FILE_NAME+getFileExt(FI_PDFA_2),FI_PDFA_2,0,id);
				if(resConv && new File(outF+File.separator+FIRST_FILE_NAME+getFileExt(FI_PDFA_2)).exists()){
					new File(inpF).delete();
					Utils.printMessage("Converted to pdf using DLLs",id,servletName,2);
					inpF=outF+File.separator+FIRST_FILE_NAME+getFileExt(FI_PDFA_2);
                                        pdfOK="OK (DLLs)";
				}else{
					Utils.printMessage("File NOT converted to pdf! resConv=" + resConv,id,servletName,0);
					//out.println("File do not converted to pdf");
					//Utils.deleteDirectory(new File(outF));
					//return;
				}
			}
		}	
		if(converter!=null)
			resConv = converter.convertDocument(inpF,outF+File.separator+FIRST_FILE_NAME+getFileExt(type),type,width,id);
		else
			resConv = false;
		//resConv = convertDocument(inpF,outF+File.separator+FIRST_FILE_NAME+getFileExt(type),type,width,id);
		new File(inpF).delete();
		if(resConv)
			Utils.printMessage("File converted",id,servletName,2);
		else{
			Utils.printMessage("File NOT converted",id,servletName,0);
			out.println("File NOT converted! pdf: " + pdfOK);
			Utils.deleteDirectory(new File(outF));
			return;
		}
		//new File(outF+".pdf").delete();
		int count = getDocumentCount(outF);
		if(count<=0){
			Utils.printMessage("No converted files in dir:"+outF,id,servletName,0);
			out.println("No converted files in dir:"+outF);
			return;
		}
		Utils.printMessage("Number of pages="+count,id,servletName,1);
		String urlCommon = "http://"+request_old.getServerName()+":"+request_old.getServerPort()+"/converter/get?id="+id;
		if(urlU!=null)
			urlU+="&id="+id+"&count="+count;
		boolean res = true;
		for(int i=0;i<count;i++){
			String urlOutF = getFileUrl(outF,urlCommon,i,type);
			HttpURLConnection connection = null;
			if(urlOutF!=null){
				if(urlU!=null){
					//?id=678&page=1&count=8&url=myUrl
					try{
						String stTemp = urlU+"&page="+(i+1)+"&url="+URLEncoder.encode(urlOutF, "utf-8");
						stTemp+="&"+DOC_SIZE+"="+getFileSize(outF, i, type);
						Utils.printMessage("TakePage " + (i+1) + " Request: \""+stTemp+"\"",id,servletName,2);
						url= new URL(stTemp);
						//connection = (HttpURLConnection) url.openConnection();
						if (stTemp.contains("https://"))
							connection = ( HttpsURLConnection )	url.openConnection();
						else
							connection = ( HttpURLConnection )	url.openConnection();
						connection.setRequestProperty( "Connection",	"keep-alive");
						connection.connect();
						
//						System.out.println("ResponceCode=" + connection.getResponseCode());
//						System.out.println("ResponceCode=" + connection.getResponseMessage();
//
//						BufferedReader in = new BufferedReader( new InputStreamReader( connection.getInputStream() ) );
//
//						String inputLine;
//						while( ( inputLine = in.readLine() ) != null
//		)
//						{
//							System.out.println( inputLine );
//							out.println( inputLine + "<br>" );
//						}
//						in.close();
//						
						InputStream is = connection.getInputStream();
						int status = connection.getResponseCode();
						Utils.printMessage("TakePage " + (i+1) + " ResponceCode: \"" + status +"\"",id,servletName,2);
						String theString = IOUtils.toString(is, "UTF-8");
						is.close();
						connection.disconnect();
						connection = null;
						if(status>=400 && status<200){
							Utils.printMessage("Error URL:"+stTemp+" return status:"+status,id,servletName,0);
							out.println("Error URL:"+stTemp+" return status:"+status);
							res=false;
							break;
						}
						else if(theString==null ||!theString.contains("OK")){
							Utils.printMessage("Error URL:"+stTemp+" return string:"+theString,id,servletName,0);
							out.println("Error URL:"+stTemp+" return string:"+theString);
							res=false;
							break;							
						}
						Utils.printMessage("TakePage "+(i+1)+" Request finished.",id,servletName,2);
					}
					catch(Exception ex){
						Utils.printMessage(ex.getMessage(),id,servletName,0);
						out.println(urlOutF);
						res=false;
						if(connection!=null)
							connection.disconnect();
						break;
					}
				}
				else
				//out.println(URLEncoder.encode(urlOutF, "utf-8"));
					out.println(urlOutF);
			}
			else{
				Utils.printMessage("Page:"+(i+1)+" not found",id,servletName,0);
				out.println("Page:"+(i+1)+" not found");
				res=false;
				break;
			}
		}
		Utils.deleteDirectory(new File(outF));
		Utils.printMessage("Dir. "+outF+" deleted. Request finished. Result " +res,id,servletName,1);
		Utils.printMessage("===" +res,id,servletName,1);
		if(res)
			out.println("Ok");
			
//		if(jni==null){
//			jni = new JNIBase("C:\\Program Files (x86)\\Apache Software Foundation\\Tomcat 7.0\\webapps\\converter\\dll\\");
//			jni.setLogHome("C:\\Program Files (x86)\\Apache Software Foundation\\Tomcat 7.0\\webapps\\converter\\dll\\", 0, 2);
//		}
		
	}
	public static String getFileExt(int type){
		switch(type){
		case FI_GIF: 
			return ".gif";
		case FI_JPEG2000: 
			return ".jp2";
		case FI_JPEGFIF: 
			return ".jpg";
		case FI_PNG: 
			return ".png";
		case FI_BMP: 
			return ".bmp";
		case FI_TIFF: 
			return ".tif";
		case FI_PDF: 
		case FI_PDFA: 
		case FI_PDFA_2: 
		case FI_PDFIMAGE: 
			return ".pdf";
		}
		return "";
	}
	private int getDocumentCount(String path){
		File f = new File(path);
		if(!f.exists() || !f.isDirectory())
			return -1;
		File[] files = f.listFiles();
		return files.length;
	}
	private String getFileUrl(String dirPath,String url,int page,int type){
		if(page==0){
			File f = new File(dirPath+File.separator+FIRST_FILE_NAME+getFileExt(type));
			if(f.exists())
				return url+"&name="+FIRST_FILE_NAME+getFileExt(type);
		}
		String addExt = "";
		if(page<10)
			addExt="000"+page;
		else {
			String hex = "000"+Integer.toHexString(page);
			int len = hex.length();
			addExt = hex.substring(len-4);
		}
		File f = new File(dirPath+File.separator+FIRST_FILE_NAME+addExt+getFileExt(type));
		if(f.exists())
			return url+"&name="+FIRST_FILE_NAME+addExt+getFileExt(type);
		return null;
	}
	private long getFileSize(String dirPath,int page,int type) throws IOException{
			if(page==0){
				File f = new File(dirPath+File.separator+FIRST_FILE_NAME+getFileExt(type));
				if(f.exists())
						return Files.size(f.toPath());
			}
			String addExt = "";
			if(page<10)
				addExt="000"+page;
			else {
				String hex = "000"+Integer.toHexString(page);
				int len = hex.length();
				addExt = hex.substring(len-4);
			}
			File f = new File(dirPath+File.separator+FIRST_FILE_NAME+addExt+getFileExt(type));
			if(f.exists())
				return Files.size(f.toPath());
		return 0;
	}
	public void init() throws ServletException {
		super.init();
		dllPath = getInitParameter("dllPath");
		dllPDFPath = getInitParameter("dllPdfPath");
		dllPDF2RasterPath = getInitParameter("dllPdf2RasterPath");
		dllLogPath = getInitParameter("dllLogPath");
		defaultOutDir = getInitParameter("defaultOutDir");
//		getDocumentUrlBase = getInitParameter("urlInputServer");
		try{
			logLevel = Integer.parseInt(getInitParameter("logLevel"));
			maxTimeout = Integer.parseInt(getInitParameter("maxTimeout"));
		}catch(Exception ex){
			System.out.println("Init problem "+ex.getMessage());
		}
		Utils.LOG_LEVEL=logLevel;
		defaultType = Integer.parseInt(getInitParameter("defaultType"));
		converter = new PDF2Raster(dllPDF2RasterPath);
		converter.setLogHome(dllLogPath, 2, 1000);
//		jni = new JNIBase(dllPath);
//		jni.setLogHome(dllLogPath, logLevel, logSize);
	}
	
	private boolean convertDocument(String _path,String _outPath,int _type,int width,String id){
		MyProcessBuilder myProc = new MyProcessBuilder();
		String cmd = "\""+dllPath + "\" \""+_path+"\" \""+_outPath+"\" "+_type+" "+width+" "+logLevel;
		boolean res = myProc.startProcess(cmd, new File(dllPath).getParentFile(), true, maxTimeout);
		String err = myProc.getError();
		String info = myProc.getInfo();
		if(res){
			if(err.length()>0){
				Utils.printMessage(err,id,servletName,0);
				res = false;
			}
			else if(!info.contains("Ok")){
				Utils.printMessage("Result value of convert is incorrect",id,servletName,0);
				res = false;
			}
			else{
				String logDllFile = new File(_outPath).getParent()+File.separator+"edocDLL.log";
				if(new File(logDllFile).exists()){
					try(BufferedReader br = new BufferedReader(new FileReader(logDllFile))) {
					    for(String line; (line = br.readLine()) != null; ) {
					    	Utils.printMessage(line,id,dllName,0);
					    }
					    // line is not visible here.
					} catch (Exception e) {
						e.printStackTrace();
					} 
					new File(logDllFile).delete();
				}
			}
		}
		return res;
		
	}
	private boolean convertDocumentPdf(String _path,String _outPath,int _type,int width,String id){
		MyProcessBuilder myProc = new MyProcessBuilder();
		String cmd = "\""+dllPDFPath + "\" \""+_path+"\" \""+_outPath+"\" "+_type+" "+width+" "+logLevel;
		boolean res = myProc.startProcess(cmd, new File(dllPDFPath).getParentFile(), true, maxTimeout);
		String err = myProc.getError();
		String info = myProc.getInfo();
		if(res){
			if(err.length()>0){
				Utils.printMessage(err,id,servletName,0);
				res = false;
			}
			else if(!info.contains("Ok")){
				Utils.printMessage("Result value of convert to PDF is incorrect: " + info,id,servletName,0);
				res = false;
			}
			else{
				String logDllFile = new File(_outPath).getParent()+File.separator+"edocDLL.log";
				if(new File(logDllFile).exists()){
					try(BufferedReader br = new BufferedReader(new FileReader(logDllFile))) {
					    for(String line; (line = br.readLine()) != null; ) {
					    	Utils.printMessage(line,id,dllName,0);
					    }
					    // line is not visible here.
					} catch (Exception e) {
						e.printStackTrace();
					} 
					new File(logDllFile).delete();
				}
			}
		}
		return res;
		
	}
	
}
