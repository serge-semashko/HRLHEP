package ru.jinr.document.ext;

import java.awt.image.BufferedImage;
import java.io.InputStream;
import java.util.Vector;

import com.lgv.ext.PageInfo;
import com.lgv.ext.PageInfo4Draw;


public interface JNIInterface {
	public static double K_PDF =96./72.;
	
	public int closeDoc(String docId);
 	public int createDocument(String title, String id,int type);  //0-pdf; 1-inso
  	public int createStreamDocument(String DocID, byte[] jByteArray, long ArrSize,int type);
  	public BufferedImage getBufferedImage(String id, BufferedImage buf, PageInfo4Draw drawInfo) throws Throwable;
  	public BufferedImage getThumbnail(String id, BufferedImage buf, int page, int w, int h);
  	//public int printDocument(String id,Vector<RichText> watermark,byte[] outByteArray, long arraySize);
//  	public int printDocument(LGVDocument doc,Vector<RichText> watermark,byte[] outByteArray, long arraySize,int orient, String pages);//AE 28.07.2016
//  	public int printFile(String path,int page, int type,Vector<RichText> watermark);
//  	public int printStream(byte[] outByteArray, long arraySize,int page, int type,Vector<RichText> watermark,int orient, String pages);
  	int setLogHome(String dirPath,int logLevel,int logSize);
  	public int convertMSOffice2Pdf(String srcPath,String dstPath, int type);
//  	public int haveMSOComponents();
//  	public int printStreamSelection(byte[] outByteArray, long arraySize, PageInfo4Draw drawInfo, int type,Vector<RichText> watermark);
//  	public int setQUALITY(double  dpi);
 // 	public int printDocumentToDefault(String id,Vector<RichText> watermark,byte[] outByteArray, long arraySize,PageInfo4Draw drawInfo,int fPage,int lPage,int copy,int orient,String margins);
//  	public int printDocumentToDefault(LGVDocument doc,Vector<RichText> watermark,byte[] outByteArray, long arraySize,PageInfo4Draw drawInfo,int fPage,int lPage,int copy,int orient,String margins);
//  	public int printStreamDocumentToDefault(byte[] outByteArray, long arraySize,int type,Vector<RichText> watermark,byte[] outAnnotByteArray, long arrayAnnotSize,PageInfo4Draw drawInfo,int fPage,int lPage,int copy,int orient,String margins);
//  	//AI 021216
//  	public int printDocumentToPrinter(String printerName,LGVDocument doc,Vector<RichText> watermark,byte[] outByteArray, long arraySize,PageInfo4Draw drawInfo,int fPage,int lPage,int copy,int orient,String margins, String pages); //AI 071216 pages added  	
//  	public int printStreamDocumentToPrinter(String printerName, byte[] outByteArray, long arraySize,int type,Vector<RichText> watermark,byte[] outAnnotByteArray, long arrayAnnotSize,PageInfo4Draw drawInfo,int fPage,int lPage,int copy,int orient,String margins, String pages); //AI 071216 pages added
  	
  	public void setShowConvertDialog( boolean flag);
  	public int convertDocumentToPDF(String inPath,String outPath,long outId);

//  	public int convertStreamDocumentToPDF(byte[] outByteArray, long arraySize,String outPath,long outId); //AE 05.05.2016
  	public int convertStreamDocumentToPDF2(InputStream in,String outPath,long outId); //AE 05.05.2016
  	public int convertDocumentToPDFAndOpen(String inPath,String outPath,String id,int type,long outId);
  	public int convertStreamDocumentToPDFAndOpen(InputStream in,String outPath,String id,int type,long outId); //AE 05.07.2016

  	public int createStreamDocument(String DocID,InputStream in,int type);//AE 18.05.2016

  	public int setKoefPDF(String DocID,double kPDF);//AE 08.08.2016


//  	private native synchronized long getDibInfoLength(String id, FrvDrawDocInfo drawInfo);
//  	public native int createSNBThumbnail(String fileInput, String fileOutput, int nStartPage, int nCountPage, double width,
//  			double height, boolean bPage2File);
//  	private synchronized native long getDIBLength(int is4Print);
//  	
  	int getDocumentInfo(String id);  
  	//DocInfo getDocumentInfo(String id);  	
  	PageInfo getPageInfo(String id, int npage);
  	public void destroy(); //AI
 // 	public String getLibDir();
//  	private native boolean isBgChangeble();
//  	public native boolean isExistDocId(String id);
//  	private native int getBackground();
//  	private native void setRemote2(boolean rem,String id);
  	}
