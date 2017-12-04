package ru.jinr.document.ext;

import java.awt.image.BufferedImage;
import java.io.File;

import javax.imageio.ImageIO;

import com.lgv.ext.PageInfo;
import com.lgv.ext.PageInfo4Draw;

import ru.jinr.document.Converter;
import ru.jinr.document.Utils;




public class PDF2Raster {
	static final String servletName = "PDF2Raster";

	JNIInterface jni = null;
	public PDF2Raster(String dllPath){
		jni = LocalJNIImpl.getLocalJNI(dllPath);
	}
	private String getStringType(int _type){
		switch(_type){
			case Converter.FI_GIF:
				return "gif";
			case Converter.FI_JPEGFIF:
				return "jpg";
			case Converter.FI_PNG:
				return "png";
			default:
				return "";
		}
	}
	public void setLogHome(String dirPath,int logLevel,int logSize){
		if(jni!=null)
			jni.setLogHome(dirPath, logLevel, logSize);
	}
	public boolean convertDocument(String _path,String _outPath,int _type,int width,String id){
		if(jni==null){
			Utils.printMessage("JNIInterface do not created",id,servletName,0);
			return false;
		}
		int res = jni.createDocument(_path, id,3);
		if(res<=0){
			Utils.printMessage("Dll do not create document",id,servletName,0);
			return false;
		}
		String type = getStringType(_type);
		if(type.length()==0){
			Utils.printMessage("Unsupported raster type",id,servletName,0);
			return false;			
		}
		BufferedImage image = null;
		//jni.setKoefPDF(id, doc.getKPDF());
		//DocInfo docInfo = jni.getDocumentInfo(doc.getDocId());
		int p = jni.getDocumentInfo(id);
		jni.setKoefPDF(id, 1);
		String dirPath = new File(_outPath).getParent();
		for(int i=0;i<p;i++){
			try{
				PageInfo pageInfo = jni.getPageInfo(id, i);
				int widthPage = pageInfo.dWidth;
				int heightPage = pageInfo.dHeight;
				double sc = 1;
				if(width>0){
					sc = (width*1.0)/widthPage;
				}
				PageInfo4Draw drawInfo = new PageInfo4Draw(i, 0,0,(int)(widthPage*sc),(int)(heightPage*sc));
				drawInfo.zoom = sc;
				drawInfo.kPdf = 1;//96./72.;
				drawInfo.rotate=0;
				image = jni.getBufferedImage(id, image, drawInfo);
				String outputfile = _outPath;
				if(i!=0){
					String addExt = "";
					if(i<10)
						addExt="000"+i;
					else {
						String hex = "000"+Integer.toHexString(i);
						int len = hex.length();
						addExt = hex.substring(len-4);
					}
					outputfile = dirPath+File.separator+Converter.FIRST_FILE_NAME+addExt+"."+type;
				}
				ImageIO.write(image, type, new File(outputfile));
			}catch(Throwable ex){
				Utils.printMessage(ex.getMessage(),id,servletName,0);
				jni.closeDoc(id);
				return false;
			}
		}
		jni.closeDoc(id);
		return true;
	}
	public boolean convertDocument2PDF(String _path,String _outPath,int _type,String id){
		if(jni==null){
			Utils.printMessage("JNIInterface do not created",id,servletName,0);
			return false;
		}
		int res = jni.convertMSOffice2Pdf(_path, _outPath, _type);
		if(res==0  && new File(_outPath).exists()){
			return true;
		}
		return false;
	}

}
