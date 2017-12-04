package com.lgv.ext;


import java.io.File;
import java.io.InputStream;
import java.util.Date;
import java.util.List;
import java.util.Vector;

import javax.swing.JOptionPane;



//import com.lgv.bundle.LGVBundle;
//import com.lgv.common.CommonConstants;
import ru.jinr.document.MyProcessBuilder;
import ru.jinr.document.Utils;
//import com.lgv.prt.RichText;
import ru.jinr.document.ext.CommonConstants;


public class JNIBase {
  	public boolean isLoaded = false;
  	private boolean canCheckMSOConverter = false;
  	String runID = null; //id of instance of dlls
  	boolean remote = false; //my be will be remote in future
  	private String libPath = null; //path of dll to be loaded
    static final String prep2010 = "prepare";
    boolean isShowConvertDialog = false;
    
    private boolean isntallMSOExport = false;
    private boolean useMSOExport = true;
    
    public JNIBase(String libPath) {
      this(libPath, false, "001");
    }

    public JNIBase(String libPath, boolean remote) {
      this(libPath, remote, "001");
    }
      	
  	public JNIBase(String _libPath, boolean _remote, String id) {
  		libPath = _libPath;
  		remote = _remote;
  		runID = id;
  		try{
  			String dll = _libPath+File.separator + "prepare.dll";
//  			if(new File(dll).exists())
//  				System.load(dll);
  			dll=_libPath+File.separator+"MSVCRT"+File.separator+"msvcr100.dll";
  			System.load(dll);
  			dll=_libPath+File.separator+"MSVCRT"+File.separator+"msvcp100.dll";
  			System.load(dll);
  			dll=_libPath+File.separator+"MSVCRT"+File.separator+"mfc100.dll";
  			System.load(dll);
//  			dll=_libPath+File.separator+"fsdk_win32.dll";
//  			if(new File(dll).exists())
//  				System.load(dll);
//  			dll = _libPath+File.separator+"pxcview.dll";
//  			if(new File(dll).exists())
//  				System.load(dll);
//  			dll=_libPath+File.separator + "cpdf.dll";
//  			System.load(dll);
  			dll=_libPath+File.separator+"Inso"+File.separator+"wvcore.dll";
  			System.load(dll);
  			dll=_libPath+File.separator+"Inso"+File.separator+"sccut.dll";
  			System.load(dll);
  			dll=_libPath+File.separator+"Inso"+File.separator+"sccch.dll";
  			System.load(dll);
  			dll=_libPath+File.separator+"Inso"+File.separator+"sccfa.dll";
  			System.load(dll);
  			dll=_libPath+File.separator+"Inso"+File.separator+"sccfut.dll";
  			System.load(dll);
  			dll=_libPath+File.separator+"Inso"+File.separator+"sccfi.dll";
  			System.load(dll);
  			dll=_libPath+File.separator+"Inso"+File.separator+"sccda.dll";
  			System.load(dll);
  			dll=_libPath+File.separator+"Inso"+File.separator+"sccfnt.dll";
  			System.load(dll);
  			dll=_libPath+File.separator+"Inso"+File.separator+"sccex.dll";
  			System.load(dll);
  			dll=_libPath+File.separator+"Inso"+File.separator+"sccfs.dll";
  			System.load(dll);
  			dll=_libPath+File.separator+"Inso"+File.separator+"sccfmt.dll";
  			System.load(dll);
  			dll=_libPath+File.separator+"Inso"+File.separator+"sccdu.dll";
  			System.load(dll);
  			dll=_libPath+File.separator+"Inso"+File.separator+"dewp.dll";
  			System.load(dll);
  			//AE 19.07.2016
  			if(new File(_libPath+File.separator+"Inso"+File.separator+"adinit.dat").exists())
  				System.out.println("Inso is full");
  			
  			dll=_libPath+File.separator + "lgv" + CommonConstants.DLL_VERSION+".dll";
  			System.load(dll);
  	  		System.out.println("DLLS version " + CommonConstants.DLL_VERSION + " were loaded from directory " + _libPath); //AI 200315
  	  		dll=_libPath+File.separator + "DetectNet.dll";
  	  		System.load(dll);
  	  		int res = detectNet("");
                        Utils.printMessage("MSOConverter res="+res,id,"XXXXXXXXXXXX",2);
  	  		//System.out.println(".Net version: "+res);
  	  		if(res>0)
  	  			canCheckMSOConverter = true;
  		}
  		catch(Throwable ex){
  			System.err.println("DLL can not loaded! "+ex.getMessage());
  			System.err.println(_libPath + " was used for loading"); //AI 200315
  		}
  		if(!canCheckMSOConverter){
//	  		try{
//	  			String dll="";	  			
//	  			dll =_libPath+File.separator+"msconv"+File.separator+"MSOConv.dll";
//	  			if(new File(dll).exists())
//	  				System.load(dll);
//	  			//setDir4MS(_libPath+File.separator+"msconv");
//	  			
//	  		}
//	  		catch(Throwable ex){
//	  			System.out.println("MSOConverter can not loaded!");
//	  		}
//  		else{
  			System.out.println(".Net not found. Use inso for open doc format.");
  		}
 		//Console.println("MiniForReview: loaded successfully dispatcher" + ".dll"+ "  Time== "+ (new java.text.SimpleDateFormat("hh:mm:ss a").format(new Date())));

  	}
  	
  	
  	public boolean canCheckMSOConverter(){
  		return canCheckMSOConverter;
  	}
  	public String getLibDir(){
  		return libPath;
  	}
  	
  	public void setShowConvertDialog( boolean flag){
  		isShowConvertDialog=flag;
  	}
  	public native int detectNet(String pathConfig);
  	//public native int setDir4MS(String dirPath);
  	public native int closeDoc(String docId);
  	public native int setLogHome(String dirPath,int logLevel,int logSize);
  	public native synchronized int createDocument(String title, String id, int type);//0 - pdf; 1 - inso
  	public native synchronized int createStreamDocument(String DocID, byte[] jByteArray, long ArrSize, int type);//0 - pdf; 1 - inso
  	public native synchronized long drawDocument(String id, byte[] outByteArray, long arraySize, PageInfo4Draw drawInfo);
  	public native synchronized long drawDocumentFast(String id, byte[] outByteArray, long arraySize, int page, int w, int h);
    //private native synchronized long getDibInfoLength(String id, PageInfo4Draw drawInfo);
//  	public native int createSNBThumbnail(String fileInput, String fileOutput, int nStartPage, int nCountPage, double width,
//  			double height, boolean bPage2File);
//  	private synchronized native long getDIBLength(int is4Print);
//  	
  	public native synchronized int getDocumentInfo(String id);  	
  	private native synchronized Object getPageInfo(String id, int npage);
  	public native synchronized int convertDocumentToPDF(String inPath,String outPath,long outId);  	//AE new INSO
  	public native synchronized int convertDocumentToPDFAndOpen(String inPath,String outPath,String id,int type,long outId);  	//AE new INSO


//  	public native synchronized int convertStreamDocumentToPDF(byte[] outByteArray, long arraySize,String outPath,long outId); //AE 21.02.2017 commented
  	public native synchronized int convertStreamDocumentToPDF2(InputStream in,String outPath,long outId);
  	public native synchronized int convertStreamDocumentToPDFAndOpen(InputStream in,String outPath,String id,int type,long outId);//AE 05.07.2016
  	
  	public native synchronized int createStreamDllDocument(String DocID,InputStream in,int type);
	
  	//AE 08.08.2016
  	public native synchronized int setKoefPDF(String DocID,double kPDF);

	public PageInfo getPageInfo_(String id, int npage) {
		Vector vecPI = (Vector)getPageInfo(id, npage);
		return (PageInfo)vecPI.get(0);
	}
	
  	public int convert(String srcPath,String dstPath, int type){
  		if(!useMSOExport)
  			return -1;
  		MyProcessBuilder pr = new MyProcessBuilder();
  		String startString = "MSOConvTest.exe "+type+" \""+srcPath+"\" \""+dstPath+"\"";
  		if(!(new File(libPath+File.separator+"msconv").exists()))
  			System.err.println("Convert dir not found");
  		//System.out.println("Convert dir:"+new File(libPath+File.separator+"msconv").getAbsolutePath());
  		//System.out.println("Convert command:"+startString);

		List<String> params = java.util.Arrays.asList(libPath+File.separator+"msconv"+File.separator+"MSOConvTest.exe", ""+type, srcPath,dstPath);
//  		if(!pr.startProcessWithoutCmd(params, new File(libPath+File.separator+"msconv"),true))
//  			pr.startProcess(startString, new File(libPath+File.separator+"msconv"));
  		pr.startProcess(startString, new File(libPath+File.separator+"msconv"));
                Utils.printMessage("MSOConverter command="+startString,"","XXXXXXXXXXXX",2);
 		String err = pr.getError();
                Utils.printMessage("MSOConverter err="+err,"","XXXXXXXXXXXX",2);
  		//System.out.println("Convert output string:"+err);
  		if(err.startsWith("-2")){
  			if(isntallMSOExport){
  		    	useMSOExport=false;
  		    	System.out.println("Convert error. Status:"+err);
  		        return -1; 				
  			}
//  			if(isShowConvertDialog){
//	  	  		System.out.println("Convert will ask install SaveAsPDFandXPS.exe");
//	  			Object[] options = { "Yes", "No" };
//	  		    int n = JOptionPane.showOptionDialog(null,
//	  		    		LGVBundle.getString("InstallDoc2DPFExport"), LGVBundle.getString("InstallDoc2DPFExportTitle"),
//	  		            JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE, null,
//	  		            options, options[1]);
//	  	  		System.out.println("Result of dialog:"+n);
//	 		    if(n == JOptionPane.OK_OPTION){ // Afirmative
//	  		  		MyProcessBuilder pr2 = new MyProcessBuilder();
//	  		  		String startString2 = "SaveAsPDFandXPS.exe ";
//	  				params = java.util.Arrays.asList(libPath+File.separator+"msconv"+File.separator+"SaveAsPDFandXPS.exe");
//	  		  		if(!pr2.startProcessWithoutCmd(params, new File(libPath+File.separator+"msconv"),true))
//	  		  			pr2.startProcess(startString2, new File(libPath+File.separator+"msconv"));
//			    	isntallMSOExport = true;
//	  		    	convert(srcPath,dstPath, type);
//	  		    }
//	  		    if(n == JOptionPane.NO_OPTION){ // negative
//	  		    	useMSOExport=false;
//	  		        return -1;
//	  		    }
//	  		    if(n == JOptionPane.CLOSED_OPTION){ // closed the dialog
//	  		    	useMSOExport=false;
//	  		        return -1;
//	  		    }
//  			}
//  			else{
//  				System.out.println("Plugin Doc2DPFExport installer is disable.");
//  				useMSOExport=false;
//  				return -1;
//  			}
//			JOptionPane.showOptionDialog(
//					null,
//					LGVBundle.getString("NoOpenFile"),
//					LGVBundle.getString("NoOpenFileTitle"),
//					JOptionPane.OK_CANCEL_OPTION);

  		}
  		
  		return 0;//convert(srcPath,dstPath,type,libPath+File.separator+"msconv");
  	}

/*	public int getDocumentInfo(String id) {
		Vector vecPI = (Vector)getDocumentInfo_(id);
		return (DocInfo)vecPI.get(0);
	}*/

//  	private native boolean isBgChangeble();
//  	public native boolean isExistDocId(String id);
//  	private native int getBackground();
//  	private native void setRemote2(boolean rem,String id);
}