package ru.jinr.document.ext;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.Vector;

import com.lgv.ext.JNIBase;
import com.lgv.ext.PageInfo;
import com.lgv.ext.PageInfo4Draw;


public class LocalJNIImpl implements JNIInterface{
	//String dllPath = null;
	JNIBase jniBase = null;
	
	byte[] outByteArray = new byte[800000];//AE 08.06.2016 was 2000000
	int rgbArray[] = new int[100000];
	private int index = 0;
	
	byte[] outByteArray_thumb = new byte[70000];//AE 08.06.2016 was 200000
	int rgbArray_thumb[] = new int[10000];
	private int index_thumb = 0;

	public LocalJNIImpl(String dllPath){
		//dllPath = _dllPath;
		jniBase = new JNIBase(dllPath);
	}
	//AI 180615
	public void destroy() {
		jniBase = null;
		outByteArray = null;
		rgbArray = null;
		outByteArray_thumb = null;
		rgbArray_thumb = null;
	}
	
//	public String getLibDir(){
//		return jniBase.getLibDir();
//	}
  	public static boolean isLibsExist(String libPath) {
  		if(libPath==null)
  			return false;
  		File test = new File(libPath+File.separator + "lgv" + CommonConstants.DLL_VERSION+".dll");
  		if(!test.exists()){
  			System.err.println("File:"+test.getAbsolutePath()+" do not exists");
  			return false;
  		}
  		//test = new File(libPath + File.separator +"dispatcher" + ".dll");	
  		return test.exists();
  	}
	public static LocalJNIImpl getLocalJNI(String dllPath){
		if(!isLibsExist(dllPath))
			return null;
		LocalJNIImpl impl = null;
		try{
			impl = new LocalJNIImpl(dllPath);
		}
		catch(Exception ex){
			ex.printStackTrace();
		}
		return impl;
	}

	@Override
	public int closeDoc(String docId) {		
		return jniBase.closeDoc(docId);
	}
	
	public int setLogHome(String dirPath,int logLevel,int logSize){
		return jniBase.setLogHome(dirPath, logLevel, logSize);
	}

	@Override
	public int createDocument(String title, String id, int type) {
		return jniBase.createDocument(title, id,type);
	}

	@Override
	public int createStreamDocument(String DocID, byte[] jByteArray, long ArrSize, int type) {
		return jniBase.createStreamDocument(DocID, jByteArray, ArrSize,type);
	}

	private int read()
			throws Exception
			{
				int res = outByteArray[index++];
				if(res==-1)
					res= 255;
				return res;
			}
	private int readInt()
			throws Exception
	{
		int i1 = read();
		int i2 = read();
		int i3 = read();
		int i4 = read();
		
	    int val = (i1 & 0xFF) | (i2 & 0xFF) << 8 |
                  (i3 & 0xFF) << 16 | (i4 & 0xFF) << 24;
		//if(i1 == -1 || i2 == -1 || i3 == -1 || i4 == -1)
		//	return -1;
			
		return val;//(i4 << 24) | (i3 << 16) | (i2 << 8) | i1;
	}
	private int readShort() throws Exception
	{
		int i1 = read();
		int i2 = read();
		if(i1 == -1 || i2 == -1)
			return -1;
					
		return (i2 << 8) + i1;
	}
	
	private int read_thumb()
			throws Exception
			{
				int res = outByteArray_thumb[index_thumb++];
				if(res==-1)
					res= 255;
				return res;
			}
	private int readInt_thumb()
			throws Exception
	{
		int i1 = read_thumb();
		int i2 = read_thumb();
		int i3 = read_thumb();
		int i4 = read_thumb();
		
	    int val = (i1 & 0xFF) | (i2 & 0xFF) << 8 |
                  (i3 & 0xFF) << 16 | (i4 & 0xFF) << 24;
		//if(i1 == -1 || i2 == -1 || i3 == -1 || i4 == -1)
		//	return -1;
			
		return val;//(i4 << 24) | (i3 << 16) | (i2 << 8) | i1;
	}
	private int readShort_thumb() throws Exception
	{
		int i1 = read_thumb();
		int i2 = read_thumb();
		if(i1 == -1 || i2 == -1)
			return -1;
					
		return (i2 << 8) + i1;
	}
	
	@Override
	public BufferedImage getBufferedImage(String id, BufferedImage buf, PageInfo4Draw drawInfo) throws Throwable {
		long res = jniBase.drawDocument(id, outByteArray, outByteArray.length, drawInfo);
		if(res==0)
			return null;
		if(res<0){
			outByteArray = null;
			try{
				outByteArray = new byte[(int) (-1*res)];
			} catch(Throwable ex){
				try{
					System.out.println("@@@@@@@@@@@   Free memory before gc =" + Runtime.getRuntime().freeMemory());
					Runtime.getRuntime().gc();
					System.out.println("@@@@@@@@@@@   Free memory after gc =" + Runtime.getRuntime().freeMemory());
					outByteArray = new byte[(int) (-1*res)];
				}
				catch(Throwable ex2){
					System.err.println("Can not allocate memory size="+(-1*res));					
					System.err.println("@@@@@@@@@@@   Free memory =" + Runtime.getRuntime().freeMemory());
					ex2.printStackTrace();
					return null;
				}
			}
			res = jniBase.drawDocument(id, outByteArray, outByteArray.length, drawInfo);
		}
		if(res<0)
			return null;
		try{
			index = 0;
			int biSize = readInt();
			if(biSize != 0x28)
				return null;
				
			int biWidth = readInt(); 
			int biHeight = readInt();
	
			if(biWidth < 0 || biWidth > 0x10000 ||
				biHeight < 0 || biHeight > 0x10000)
				return null;
			
			int biPlanes = readShort();
			if(biPlanes != 1)
				return null;
			
			int biBitCount = readShort();
			if(	biBitCount != 24)
				return null;			
			
			int biCompression = readInt();
			if(biCompression != 0)
				return null;
				
			int biSizeImage = readInt(); 
		 	int biXPelsPerMeter = readInt(); 
			int biYPelsPerMeter = readInt(); 
			int biClrUsed = readInt(); 
			int biClrImportant = readInt();
			
			if(buf == null || buf.getWidth() != biWidth || buf.getHeight()!=biHeight)
				buf = new BufferedImage(biWidth, biHeight, BufferedImage.TYPE_INT_RGB);			
			if(rgbArray.length<biWidth*biHeight){
				rgbArray = null;
				rgbArray = new int[biWidth*biHeight];
			}
			switch(biBitCount)
			{
				case 24:
				{
					int align = (((biWidth*3 + 3) >> 2) << 2) - biWidth*3;
					for(int y = biHeight-1; y >=0 ; y--)
					{
						for(int x = 0; x < biWidth; x++)
						{
							int r = read();
							int g = read();
							int b = read();
							if(r == -1 || g == -1 || b == -1)
								return null;

							int clr = (b << 16) + (g << 8) + r;
							rgbArray[biWidth*y + x] = clr;
						}
						for(int a = 0; a < align; a++)
							read();
					}
				}
				break;

				default:
					return null; //To Do: implement all cases
			}

			buf.setRGB(0, 0, biWidth, biHeight, rgbArray, 0, biWidth);
		}
		catch(Throwable ex){
			
			throw ex;
		}
		return buf;
	}

	@Override
	public PageInfo getPageInfo(String id, int npage) {
		return jniBase.getPageInfo_(id, npage);
	}

	@Override
	public int getDocumentInfo(String id) {
		return jniBase.getDocumentInfo(id);
	}

	@Override
	public BufferedImage getThumbnail(String id, BufferedImage buf, int page, int w, int h) {
		long res = jniBase.drawDocumentFast(id, outByteArray_thumb, outByteArray_thumb.length, page, w, h);
		if(res==0)
			return null;
		if(res<0){
			outByteArray_thumb = new byte[(int) (-1*res)];
			res = jniBase.drawDocumentFast(id, outByteArray_thumb, outByteArray_thumb.length, page, w, h);
		}
		if(res<0)
			return null;
		try{
			index_thumb = 0;
			int biSize = readInt_thumb();
			if(biSize != 0x28)
				return null;
				
			int biWidth = readInt_thumb(); 
			int biHeight = readInt_thumb();
	
			if(biWidth < 0 || biWidth > 0x10000 ||
				biHeight < 0 || biHeight > 0x10000)
				return null;
			
			int biPlanes = readShort_thumb();
			if(biPlanes != 1)
				return null;
			
			int biBitCount = readShort_thumb();
			if(	biBitCount != 24)
				return null;			
			
			int biCompression = readInt_thumb();
			if(biCompression != 0)
				return null;
				
			int biSizeImage = readInt_thumb(); 
		 	int biXPelsPerMeter = readInt_thumb(); 
			int biYPelsPerMeter = readInt_thumb(); 
			int biClrUsed = readInt_thumb(); 
			int biClrImportant = readInt_thumb();
			
			if(buf == null || buf.getWidth() != biWidth || buf.getHeight()!=biHeight)
				buf = new BufferedImage(biWidth, biHeight, BufferedImage.TYPE_INT_RGB);			

			if(rgbArray_thumb.length<biWidth*biHeight){
				rgbArray_thumb = null;
				rgbArray_thumb = new int[biWidth*biHeight];
			}
			switch(biBitCount)
			{
				case 24:
				{
					int align = (((biWidth*3 + 3) >> 2) << 2) - biWidth*3;
					for(int y = biHeight-1; y >=0 ; y--)
					{
						for(int x = 0; x < biWidth; x++)
						{
							int r = read_thumb();
							int g = read_thumb();
							int b = read_thumb();
							if(r == -1 || g == -1 || b == -1)
								return null;

							int clr = (b << 16) + (g << 8) + r;
							rgbArray_thumb[biWidth*y + x] = clr;
						}
						for(int a = 0; a < align; a++)
							read_thumb();
					}
				}
				break;

				default:
					return null; //To Do: implement all cases
			}

			buf.setRGB(0, 0, biWidth, biHeight, rgbArray_thumb, 0, biWidth);
		}
		catch(Exception ex){
			return null;
		}
		return buf;
	}

//	@Override
//	public int printDocument(LGVDocument doc,Vector<RichText> watermark,byte[] outByteArray, long arraySize,int orient, String pages) {
//		return jniBase.printDocument(doc.getDocId(),watermark,outByteArray,arraySize,orient, pages);
//	}

//	@Override
//	public int printFile(String path, int page,int type,Vector<RichText> watermark) {
//		return jniBase.printFile(path, page, type,watermark);
//	}

//	@Override
//	public int printStream(byte[] outByteArray, long arraySize,int page, int type,Vector<RichText> watermark,int orient, String pages) {
//		return jniBase.printStream(outByteArray, arraySize,page,type,watermark,orient, pages);
//	}
	
	public int convertMSOffice2Pdf(String srcPath,String dstPath, int type){
		return jniBase.convert(srcPath, dstPath, type);
	}

//	@Override
//	public int haveMSOComponents() {
//		if(jniBase.canCheckMSOConverter())
//			try{
//				return jniBase.detectMSOComponents();//HaveMSOComponents();
//			}
//			catch(Throwable ex){
//				System.err.println(ex.getMessage());
//			}
//		return -2;
//	}
//	//since version of dll 3
//	public int printStreamSelection(byte[] outByteArray, long arraySize, PageInfo4Draw drawInfo, int type,Vector<RichText> watermark){
//		return jniBase.printStreamSelection(outByteArray, arraySize, drawInfo, type,watermark);
//	}
	//since version of dll 3
//	public int setQUALITY(double  dpi){
//		return setQUALITY(dpi);//working only for inso
//	}
	 //AI 071216 pages added here and below
//  	public int printDocumentToPrinter(String printerName,LGVDocument doc,Vector<RichText> watermark,byte[] outByteArray, long arraySize,PageInfo4Draw drawInfo,int fPage,int lPage,int copy,int orient,String margins, String pages)  {
//  		//TODO
// 			return jniBase.printDocumentToPrinter(printerName,doc.getDocId(), watermark, outByteArray, arraySize, drawInfo, fPage, lPage, copy, orient, margins, pages);
//  	}
//  	public int printStreamDocumentToPrinter(String printerName,byte[] outByteArray, long arraySize,int type,Vector<RichText> watermark,byte[] outAnnotByteArray, long arrayAnnotSize,PageInfo4Draw drawInfo,int fPage,int lPage,int copy,int orient,String margins, String pages){
//  		//TODO
//  			return jniBase.printStreamDocumentToPrinter(printerName,outByteArray, arraySize, type, watermark, outAnnotByteArray, arrayAnnotSize, drawInfo, fPage, lPage, copy, orient, margins, pages);
// 	}
  	public void setShowConvertDialog(boolean flag){
  		jniBase.setShowConvertDialog(flag);
  	}
  	
  	public int convertDocumentToPDF(String inPath,String outPath,long outId){
  		return jniBase.convertDocumentToPDF(inPath, outPath,outId);
  	}
  	
  	//AE 30.06.2016
  	public int convertDocumentToPDFAndOpen(String inPath,String outPath,String id,int type,long outId){
  		return jniBase.convertDocumentToPDFAndOpen(inPath, outPath, id,type,outId);
  	}
    //AE 21.02.2017 commented
//  	public int convertStreamDocumentToPDF(byte[] outByteArray, long arraySize,String outPath,long outId){ 
//  		return jniBase.convertStreamDocumentToPDF(outByteArray, arraySize, outPath,outId);
//  	}
  	
  	public int convertStreamDocumentToPDF2(InputStream in,String outPath,long outId){ 
  		return jniBase.convertStreamDocumentToPDF2(in, outPath,outId);
  	}
  	//AE 18.05.2016
  	public int createStreamDocument(String DocID,InputStream in,int type){
  		return jniBase.createStreamDllDocument(DocID, in, type);
  	}
  	public int convertStreamDocumentToPDFAndOpen(InputStream in,String outPath,String id,int type,long outId){
  		return jniBase.convertStreamDocumentToPDFAndOpen(in,outPath,id, type,outId);
  	}
	@Override
	public int setKoefPDF(String DocID, double kPDF) {
		if(kPDF<=0)
			kPDF=K_PDF;
		return jniBase.setKoefPDF(DocID, kPDF);
	}

}
