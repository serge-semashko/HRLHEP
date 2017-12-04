package jinr.pin;

import dubna.walt.util.IOUtil;

import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.geom.AffineTransform;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.InputStream;
import java.io.OutputStream;
import java.sql.*;

import javax.imageio.ImageIO;
import javax.sql.rowset.serial.*;
//import javax.servlet.*;
//import javax.servlet.http.*;
//import java.io.*;
import dubna.walt.util.StrUtil;
import javax.servlet.ServletContext;
import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServlet;

public class ServiceMakeThumbnail extends dubna.walt.service.Service
{

	public void start() throws Exception
	{
		byte[] b = getImageBytes();
		
/* resizing comes here !!! */
		int destWidth=cfgTuner.getIntParameter("maxWidth");
		int destHeight=cfgTuner.getIntParameter("maxHeight");
		int lp=cfgTuner.getIntParameter("limitPercentage");
//		System.out.println("max w:"+destWidth);
//		System.out.println("max h:"+destHeight);
		BufferedImage src = ImageIO.read(new ByteArrayInputStream(b));
		int srcHeight=src.getHeight(); 
		int srcWidth=src.getWidth();
		System.out.println("src w:"+srcWidth);
		System.out.println("src h:"+srcHeight);
		float koeff;
		float kh=(float)srcHeight/destHeight;
		float kw=(float)srcWidth/srcWidth;
		if((float)srcHeight/destHeight>(float)srcWidth/destWidth){
			koeff=(float)srcHeight/destHeight;
			
		}
		else{
			koeff=(float)srcWidth/destWidth;
		}
		System.out.println("koeff:"+koeff);	

if ((koeff>1)&&(koeff>(float)(lp+100)/100)){	
		
    destWidth=(int)(srcWidth/koeff);
    destHeight=(int)(srcHeight/koeff);
//	System.out.println("dest h:"+destHeight);
//	System.out.println("dest w:"+destWidth);
	BufferedImage dest = new BufferedImage(destWidth,destHeight,BufferedImage.TYPE_INT_RGB);
	Graphics2D g = dest.createGraphics();
	AffineTransform at = AffineTransform.getScaleInstance((double)destWidth/srcWidth,(double)destHeight/srcHeight);
	RenderingHints renderingHint =new RenderingHints(RenderingHints.KEY_RENDERING , RenderingHints.VALUE_RENDER_QUALITY);
	g.addRenderingHints(renderingHint);
	g.drawRenderedImage(src,at);
	ByteArrayOutputStream output=new ByteArrayOutputStream();
	ImageIO.write(dest, "JPG",output);
	storeImage(output.toByteArray());
}
else{ 
	System.out.println("resize is not needed!");		
	storeImage(b);
}
/* resizing ends here !!! */

		super.start();
	//  cfgTuner.addParameter("MORE_LINKS", after_tree);
	}


	protected byte[] getImageBytes() throws Exception
	{ 
		String sql = getSQL ("getImageSQL");
		String file_name = "";
//		oracle.sql.BLOB bl = null;
	  java.sql.Blob bl = null;
		System.out.println("+++++++++++++++++++ Start reading file data +++++++++++++++++++");
		try
		{
			ResultSet r = dbUtil.getResults(sql);
			if (r.next())
			{ file_name = r.getString(1);
	      bl = new SerialBlob( r.getBlob(2));
//				bl = (oracle.sql.BLOB) r.getBlob(2);
				dbUtil.closeResultSet(r); 
				System.out.println("+++ file_name:" + file_name);
			  System.out.println("+++ got blob " + Long.toString(bl.length()/1024) + "KB" );
			
//			 System.out.println("BLOB=" + bl + "; LENGTH=" + bl.length());
				byte[] dat = bl.getBytes(1, (int)bl.length());
			  //out.println("file_name:" + file_name + "<p>");
				return dat;
			}
		}
		catch (Exception e) 
		{
			e.printStackTrace(out);
		}
		return null;
	}

	public void storeImage(byte[] b) throws Exception
	{ try
		{ Connection con = dbUtil.getConnection();
			String sql = StrUtil.strFromArray(cfgTuner.getCustomSection("storeImageSQL"));
			IOUtil.writeLogLn("<b>Store Image: </b><br>" + sql, rm);
	 System.out.println("Store SQL:" + sql);
			PreparedStatement stmt = con.prepareStatement (sql);

//        oracle.sql.BLOB bl = oracle.sql.BLOB.createTemporary( con, false, oracle.sql.BLOB.DURATION_SESSION );
//          bl.putBytes( 1,fc.getBytes() );
				java.sql.Blob bl = new SerialBlob(b);
				
				stmt.setBlob( 1, bl );
//          stmt.executeQuery();
				int numRec = stmt.executeUpdate();
				stmt.close();
				con.commit();
				if (numRec == 1)
				{ IOUtil.writeLogLn("Store image - OK", rm);
				}
				else
				{ cfgTuner.addParameter("UPLOAD_ERROR","Number inserted records:" + numRec);
					cfgTuner.addParameter("ERROR","Number inserted records:" + numRec);
					IOUtil.writeLogLn("***** Upload file: number inserted records:" + numRec, rm);
				}
		}
		catch (Exception e)
		{ cfgTuner.addParameter("UPLOAD_ERROR", e.toString());
			e.printStackTrace(System.out);
			cfgTuner.addParameter("ERROR",e.toString());
		}
	}
}