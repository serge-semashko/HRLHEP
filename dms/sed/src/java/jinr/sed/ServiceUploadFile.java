/*
 * Сервис загрузки файла в ФХ с привязкой к документу
 * Автор: Устенко
 */

package jinr.sed;
import java.io.File;
import java.io.FileOutputStream;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
//import java.sql.*;

public class ServiceUploadFile extends dubna.walt.service.Service {

    @Override
    public void start() throws Exception {
        try {
            cfgTuner.getCustomSection("before all");
            if(!cfgTuner.getParameter("UNAUTHORIZED_ACCESS").isEmpty()){
                cfgTuner.outCustomSection("NO_ACCESS", out);
                return;
            }

            if (cfgTuner.enabledExpression("new_file")) {
                String idDirParamName=cfgTuner.getParameter("idDirParamName");
                boolean keepOriginalFileName = cfgTuner.getParameter("keepOriginalFileName").equals("true");
                int filesize = 0;
                String fileStoragePath = cfgTuner.getParameter("file_storage_path");
                String fileStorageSubPath = cfgTuner.getParameter("file_storage_subpath");
                if(!fileStorageSubPath.isEmpty()&&!fileStorageSubPath.endsWith("/")) fileStorageSubPath+="/";
                
                dubna.walt.util.FileContent fc = (dubna.walt.util.FileContent) rm.getObject("new_file_CONTENT");

                String FSFileName = "";
                String filename = cfgTuner.getParameter("new_file");
                filename = filename.lastIndexOf(".")>0?filename.substring(0,filename.lastIndexOf(".")):filename;
                String fileext = "." + cfgTuner.getParameter("new_file_TYPE");
                if (fileStoragePath != null && !fileStoragePath.isEmpty()) {
                    if(!keepOriginalFileName){
                        if(cfgTuner.enabledOption("NEW_FILE_ID"))
                            filename = "f" + cfgTuner.getParameter("NEW_FILE_ID");
                        else
                            filename = md5hashhex(fc.getBytes());
                    }
                    filesize = fc.getBytes().length;
                    if (!filename.isEmpty()) {
                        String dirname ="";
                        if(!cfgTuner.getParameter(idDirParamName).isEmpty()){
                            dirname = cfgTuner.getIntParameter(idDirParamName) + "/";
                            File dir = new File(fileStoragePath + fileStorageSubPath + dirname);
                                if (!dir.exists()) {
                                    dir.mkdirs();
                                }
                        }
                        File file = new File(fileStoragePath + fileStorageSubPath + dirname + filename + fileext);
                        if (!file.exists()) {
                            file.createNewFile();
                            FSFileName = fileStorageSubPath+dirname + filename;
                        } else {
                            int cnt = 0;
                            while (file.exists()) {
                                cnt++;
                                file = new File(fileStoragePath + fileStorageSubPath + dirname + filename + "(" + cnt + ")" + fileext);
                            }
                            file.createNewFile();
                            FSFileName = dirname + filename + "(" + cnt + ")";
                        }
                        FileOutputStream fos = new FileOutputStream(file);
                        fos.write(fc.getBytes());
                        fos.close();
                    }

                }

                cfgTuner.addParameter("fs_file_name", FSFileName + fileext);
		cfgTuner.addParameter("file_size", Integer.toString(filesize));
		getData("register file");
                System.out.println("Store OK!");
                cfgTuner.outCustomSection("ok", out);

//                super.start();
                cfgTuner.outCustomSection(reportSectionName, out);


            }
        } catch (Exception e) {
            cfgTuner.addParameter("UPLOAD_ERROR", e.getLocalizedMessage());
            e.printStackTrace(System.out);
            cfgTuner.outCustomSection("UPLOAD_ERROR", out);
            
        }

    }
/*
    public void beforeStart() throws Exception {
        super.beforeStart();
        try {
            cfgTuner.getCustomSection("before all");
            if(!cfgTuner.getParameter("UNAUTHORIZED_ACCESS").isEmpty()){
                throw (new Exception("Нет прав для загрузки файла!"));
            }

            if (cfgTuner.enabledExpression("new_file")) {
                String idDirParamName=cfgTuner.getParameter("idDirParamName");
                int docId = cfgTuner.getIntParameter(idDirParamName);
                boolean keepOriginalFileName = cfgTuner.getParameter("keepOriginalFileName").equals("true");
                int filesize = 0;
                String fileStoragePath = cfgTuner.getParameter("file_storage_path");
                String fileStorageSubPath = cfgTuner.getParameter("file_storage_subpath");
                if(!fileStorageSubPath.isEmpty()&&!fileStorageSubPath.endsWith("/")) fileStorageSubPath+="/";
                
                dubna.walt.util.FileContent fc = (dubna.walt.util.FileContent) rm.getObject("new_file_CONTENT");

                String FSFileName = "";
                String filename = cfgTuner.getParameter("new_file");
                filename = filename.lastIndexOf(".")>0?filename.substring(0,filename.lastIndexOf(".")):filename;
                String fileext = "." + cfgTuner.getParameter("new_file_TYPE");
                if (fileStoragePath != null && !fileStoragePath.isEmpty()) {
                    if(!keepOriginalFileName){
                        if(cfgTuner.enabledOption("NEW_FILE_ID"))
                            filename = "f" + cfgTuner.getParameter("NEW_FILE_ID");
                        else
                            filename = md5hashhex(fc.getBytes());
                    }
                    filesize = fc.getBytes().length;
                    if (!filename.isEmpty()) {
                        String dirname ="";
                        if(!cfgTuner.getParameter(idDirParamName).isEmpty()){
                            dirname = docId + "/";
                            File dir = new File(fileStoragePath + fileStorageSubPath + dirname);
                                if (!dir.exists()) {
                                    dir.mkdirs();
                                }
                        }
                        File file = new File(fileStoragePath + fileStorageSubPath + dirname + filename + fileext);
                        if (!file.exists()) {
                            file.createNewFile();
                            FSFileName = fileStorageSubPath+dirname + filename;
                        } else {
                            int cnt = 0;
                            while (file.exists()) {
                                cnt++;
                                file = new File(fileStoragePath + fileStorageSubPath + dirname + filename + "(" + cnt + ")" + fileext);
                            }
                            file.createNewFile();
                            FSFileName = dirname + filename + "(" + cnt + ")";
                        }
                        FileOutputStream fos = new FileOutputStream(file);
                        fos.write(fc.getBytes());
                        fos.close();
                    }

                }

				cfgTuner.addParameter("fs_file_name", FSFileName + fileext);
				cfgTuner.addParameter("file_size", Integer.toString(filesize));
				getData("register file");
                System.out.println("Store OK!");
                cfgTuner.outCustomSection("ok", out);
            }
        } catch (Exception e) {
            cfgTuner.addParameter("UPLOAD_ERROR", e.getLocalizedMessage());
            e.printStackTrace(System.out);
            cfgTuner.outCustomSection("NO_ACCESS", out);
        }

    }
/**/
    
    public static String md5hashhex(byte[] bytes) throws NoSuchAlgorithmException {
        String md5hash = null;
        MessageDigest md = MessageDigest.getInstance("MD5");
        md.update(bytes);
        byte[] mdbytes = md.digest();
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < mdbytes.length; i++) {
            sb.append(Integer.toString((mdbytes[i] & 0xff) + 0x100, 16).substring(1));
        }
        md5hash = sb.toString();
        return md5hash;
    }
}
