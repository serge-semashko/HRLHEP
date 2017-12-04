package jinr.arch;

import java.io.File;
import java.io.FileOutputStream;
import java.io.PrintWriter;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.sql.*;
import java.util.HashMap;
import jinr.arch.dbtools.DBSelect;
import jinr.arch.dbtools.DBTools;


public class ServiceUploadFile extends dubna.walt.service.Service
{
        public static String md5hashhex(byte[] bytes) throws NoSuchAlgorithmException{
            String md5hash = null;
            MessageDigest md = MessageDigest.getInstance("MD5");
                              md.update(bytes);
                              byte[] mdbytes = md.digest();
                              StringBuffer sb = new StringBuffer();
                              for (int i = 0; i < mdbytes.length; i++) {
                                sb.append(Integer.toString((mdbytes[i] & 0xff) + 0x100, 16).substring(1));
                              }
                              md5hash=sb.toString();
            return md5hash;
        }
	public void beforeStart() throws Exception
	{ super.beforeStart();
		try
		{ if (cfgTuner.enabledExpression("FILE_1"))
			{
			    //            Class.forName ("oracle.jdbc.driver.OracleDriver");
			    Connection con = dbUtil.getConnection();
                            int docId = cfgTuner.getIntParameter("ID") ;
                            HashMap dateHm = DBSelect.getRow("SELECT SYSDATE, DAT_CREATE, to_char(DAT_CREATE, 'MM') AS M, to_char(DAT_CREATE,'YYYY') AS Y FROM ARCH_DOC WHERE ID = "+docId,  null, con);
                            java.util.Date sysdate = (java.util.Date)dateHm.get("SYSDATE");
                            String m = (String)dateHm.get("M");
                            String y = (String)dateHm.get("Y");
                            int filesize = 0;
                            String fileStoragePath = cfgTuner.getParameter("file_storage_path");
			    
			    //     bl.setBytes(posted.b);
			    dubna.walt.util.FileContent fc = (dubna.walt.util.FileContent) rm.getObject("FILE_1_CONTENT");
			    String FSFileName="";
                            String filename = "";
                            String fileext = "."+ cfgTuner.getParameter("FILE_1_TYPE");
                            if( fileStoragePath != null && ! fileStoragePath.isEmpty() ){
                               filename = md5hashhex(fc.getBytes());
                               filesize = fc.getBytes().length;
                               if(!filename.isEmpty()&&!cfgTuner.getParameter("ID").isEmpty()){
                                   String dirname = y+"/"+m+"/"+cfgTuner.getParameter("ID")+"/";
                                   File dir = new File(fileStoragePath+dirname);
                                   if(!dir.exists()) dir.mkdirs();
                                   
                                  // filename =dirname+filename+fileext;
                                   File file = new File(fileStoragePath+dirname+filename+fileext);
                                   if(!file.exists()){
                                        file.createNewFile();
                                        FSFileName=dirname+filename;
                                   }else{
                                       int cnt = 0;
                                       while(file.exists()){
                                           cnt++;
                                           file = new File(fileStoragePath+dirname+filename+"("+cnt+")"+fileext);
                                           
                                       }
                                       file.createNewFile();
                                       FSFileName=dirname+filename+"("+cnt+")";
                                   }
                                   FileOutputStream fos = new FileOutputStream(file);
                                   fos.write(fc.getBytes());
                                   fos.close();
                               }
                                
                            }
                            
			    //          bl = new SerialBlob(posted.b);
					
                            
                            int pageNr = cfgTuner.getIntParameter("NEXT_PAGE_NR") ;
                            
                            HashMap docData = new HashMap();
                            docData.put("DOC_ID", docId);
                            docData.put("PAGE_NR", pageNr);
                            //docData.put("PAGE_DATA",fc.getBytes());
                            docData.put("FILE_NAME",cfgTuner.getParameter("FILE_1"));
                            docData.put("EXT", cfgTuner.getParameter("FILE_1_TYPE"));
                            docData.put("CONTENT_TYPE",cfgTuner.getParameter("FILE_1_CONTENT_TYPE"));
                            docData.put("UPLOADED", sysdate);
                            docData.put("FS_FILE_NAME",FSFileName+fileext);
                            docData.put("FILE_SIZE",filesize);
                            DBTools.insertRow("DOC_DATA", docData, con);
			    System.out.println("Store OK");
                            docData.remove("PAGE_DATA");
                            PrintWriter writer = new PrintWriter(fileStoragePath+FSFileName+fileext+".metadata", "UTF-8");
                            writer.print(docData);
                            writer.close();
			}
		}
		catch (Exception e)
		{ cfgTuner.addParameter("UPLOAD_ERROR",e.toString());
			e.printStackTrace(System.out);
		}
		
	}
}