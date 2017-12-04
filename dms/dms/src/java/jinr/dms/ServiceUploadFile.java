/*
 * Сервис загрузки файла в ФХ с привязкой к документу
 * Автор: Устенко
 */

package jinr.dms;
import java.io.File;
import java.io.FileOutputStream;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.sql.*;
import java.util.HashMap;
import jinr.dms.tools.DBTools;

public class ServiceUploadFile extends dubna.walt.service.Service {

    public static String md5hashhex(byte[] bytes) throws NoSuchAlgorithmException {
        String md5hash = null;
        MessageDigest md = MessageDigest.getInstance("MD5");
        md.update(bytes);
        byte[] mdbytes = md.digest();
        StringBuffer sb = new StringBuffer();
        for (int i = 0; i < mdbytes.length; i++) {
            sb.append(Integer.toString((mdbytes[i] & 0xff) + 0x100, 16).substring(1));
        }
        md5hash = sb.toString();
        return md5hash;
    }

    public void beforeStart() throws Exception {
        super.beforeStart();
        try {
            if (cfgTuner.enabledExpression("new_file")) {
                Connection con = dbUtil.getConnection();
                int docId = cfgTuner.getIntParameter("doc_id");
                int filesize = 0;
                String fileStoragePath = cfgTuner.getParameter("file_storage_path");
                dubna.walt.util.FileContent fc = (dubna.walt.util.FileContent) rm.getObject("new_file_CONTENT");

                String FSFileName = "";
                String filename = "";
                String fileext = "." + cfgTuner.getParameter("new_file_TYPE");
                if (fileStoragePath != null && !fileStoragePath.isEmpty()) {
                    filename = md5hashhex(fc.getBytes());
                    filesize = fc.getBytes().length;
                    if (!filename.isEmpty() && !cfgTuner.getParameter("doc_id").isEmpty()) {
                        String dirname = docId + "/";
                        File dir = new File(fileStoragePath + dirname);
                        if (!dir.exists()) {
                            dir.mkdirs();
                        }
                        File file = new File(fileStoragePath + dirname + filename + fileext);
                        if (!file.exists()) {
                            file.createNewFile();
                            FSFileName = dirname + filename;
                        } else {
                            int cnt = 0;
                            while (file.exists()) {
                                cnt++;
                                file = new File(fileStoragePath + dirname + filename + "(" + cnt + ")" + fileext);
                            }
                            file.createNewFile();
                            FSFileName = dirname + filename + "(" + cnt + ")";
                        }
                        FileOutputStream fos = new FileOutputStream(file);
                        fos.write(fc.getBytes());
                        fos.close();
                    }

                }

				/*
                int norder = cfgTuner.getIntParameter("norder");
                HashMap docData = new HashMap();
                docData.put("doc_id", docId);
                docData.put("norder", norder);
                docData.put("file_name", cfgTuner.getParameter("new_file"));
                docData.put("file_ext", cfgTuner.getParameter("new_file_TYPE"));
                docData.put("file_content_type", cfgTuner.getParameter("new_file_CONTENT_TYPE"));
                docData.put("fs_file_name", FSFileName + fileext);
                docData.put("file_size", filesize);
                DBTools.insertRow("doc_files", docData, con);
						/**/
				cfgTuner.addParameter("fs_file_name", FSFileName + fileext);
				cfgTuner.addParameter("file_size", Integer.toString(filesize));
				getData("register file");
                System.out.println("Store OK!");
                cfgTuner.outCustomSection("ok", out);
            }
        } catch (Exception e) {
            cfgTuner.addParameter("UPLOAD_ERROR", e.toString());
            e.printStackTrace(System.out);
            cfgTuner.outCustomSection("no ok", out);
        }

    }
}
