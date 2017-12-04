/*
 * Сервис выгрузки файла из ФХ по id
 * Автор: Устенко
 */
package jinr.dms;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.sql.*;
import javax.servlet.*;
import javax.servlet.http.*;
import java.net.URLEncoder;

public class ServiceDownloadFile extends dubna.walt.service.Service {

    public void start() throws Exception {
        cfgTuner.getCustomSection("report");
        getDocData();
    }

    public static byte[] file2bytearray(File file) throws IOException {

        ByteArrayOutputStream ous = null;
        InputStream ios = null;
        try {
            byte[] buffer = new byte[4096];
            ous = new ByteArrayOutputStream();
            ios = new FileInputStream(file);
            int read = 0;
            while ((read = ios.read(buffer)) != -1) {
                ous.write(buffer, 0, read);
            }
        } finally {
            try {
                if (ous != null) {
                    ous.close();
                }
            } catch (IOException e) {
            }

            try {
                if (ios != null) {
                    ios.close();
                }
            } catch (IOException e) {
            }
        }
        return ous.toByteArray();
    }

    protected void getDocData() throws Exception {
        String FILE_NAME_FIELD = cfgTuner.getParameter("NAME_FIELD").length() > 0 ? cfgTuner.getParameter("NAME_FIELD") : "file_name";
        String CT_FIELD = cfgTuner.getParameter("CT_FIELD").length() > 0 ? cfgTuner.getParameter("CT_FIELD") : "file_content_type";
        String PATH_FIELD = cfgTuner.getParameter("PATH_FIELD").length() > 0 ? cfgTuner.getParameter("PATH_FIELD") : "fs_file_name";
        String sql = getSQL("SQL");
        String file_name = "";
        String fsPath = cfgTuner.getParameter("file_storage_path");
        String fileId = cfgTuner.getParameter("id");
        try {
            ServletOutputStream outStream = (ServletOutputStream) rm.getObject("outStream");
            ResultSet r = dbUtil.getResults(sql);
            if (r.next()) {
                file_name = URLEncoder.encode(r.getString(FILE_NAME_FIELD).trim(), "UTF-8");
                String ct = r.getString(CT_FIELD);
                String fsFileName = r.getString(PATH_FIELD);
                HttpServlet serv = (HttpServlet) rm.getObject("Servlet");
                if (ct == null || ct.length() < 2) {
                    ServletContext sc = serv.getServletContext();
                    ct = sc.getMimeType(file_name.toLowerCase());
                }
                byte[] dat = null;
                String fileLength = null;
                if (fsPath != null && !fsPath.isEmpty()
                        && fsFileName != null && !fsFileName.isEmpty()
                        && fileId != null && !fileId.isEmpty()) {
                    File file = new File(fsPath + fsFileName);
                    fileLength = Long.toString(file.length());
                    if (file.exists()) {
                        response.setContentType(ct);
                        response.setHeader("Content-Disposition", "attachment; filename=" + file_name + ";filename*=utf-8''" + file_name.replaceAll("\\+", "%20"));
                        dat = file2bytearray(file);
                        response.setHeader("Content-length", fileLength);
                        outStream.flush();
                        if (dat != null) {
                            outStream.write(dat);
                        }
                    } else {
                        // почему-то нет файла в ФХ
                        cfgTuner.outCustomSection("file not found", out);
                    }
                } else {
                    // ФХ не верно сконфигурировано, в таблице пусто fs_filename
                    cfgTuner.outCustomSection("file not found", out);
                }

            } else {
                //нет файла с таким id
                cfgTuner.outCustomSection("file not found", out);
            }
            dbUtil.closeResultSet(r);
            outStream.flush();
            outStream.close();
        } catch (Exception e) {
            e.printStackTrace(out);
        }
    }

}
