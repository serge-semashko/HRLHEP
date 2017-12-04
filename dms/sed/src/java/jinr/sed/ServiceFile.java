/**
 * Выгрузка файла из ФС клиенту
 */
package jinr.sed;

import dubna.walt.util.FileContent;
import java.net.URLEncoder;
import javax.servlet.ServletContext;
import javax.servlet.ServletOutputStream;
import jinr.sed.tools.Transliterator;

public class ServiceFile extends dubna.walt.service.ServiceFileData {

    @Override
    public void start() throws Exception {

        cfgTuner.getCustomSection("report header");
        if (!cfgTuner.enabledExpression("ERROR")) {
            try {
                if (cfgTuner.enabledExpression("pf")) {
                    cfgTuner.addParameter("FILE_PATH",
                            cfgTuner.getParameter("FILE_PATH").replace(".",  cfgTuner.getParameter("pf") + ".")
                    );
                }
                
//            String fileName = URLEncoder.encode(cfgTuner.getParameter("FILE_NAME").trim(), "UTF-8");
//                super.start();
                start2();
            } catch (Exception e) {
                e.printStackTrace(System.out);
                cfgTuner.addParameter("ERROR", e.toString());
            }
        }
        if (!cfgTuner.enabledExpression("ERROR")) {
            cfgTuner.getCustomSection("report footer");
        }
        else {
            cfgTuner.outCustomSection("ERR_MSG", out);
        }
    } 

    
        public void start2() throws Exception {
        String file_path = cfgTuner.getParameter("FILE_PATH");
        String content_type = cfgTuner.getParameter("CONTENT_TYPE");
//        String fileName = cfgTuner.getParameter("NAME");
        String fileName = cfgTuner.getParameter("FILE_NAME").trim().replaceAll(" ", "_");
        System.out.println("*** ServiceFile: file_path=" + file_path);

        if (content_type.length() < 2) {
            ServletContext sc = (ServletContext) rm.getObject("ServletContext");
            content_type = sc.getMimeType(file_path);
        }
        if (fileName.length() < 1) {
            int i = file_path.lastIndexOf("/");
            fileName = file_path.substring(i + 1);
        }
        long file_size = FileContent.getFileSize(file_path);

        String userAgent = request.getHeader("USER-AGENT").toLowerCase();
//            System.out.println("userAgent = " + userAgent);
        System.out.println("; File Name=" + fileName + "; size=" + Long.toString(file_size) + "; ContentType:" + content_type);

        if (file_size > 0) {
            ServletOutputStream outStream = (ServletOutputStream) rm.getObject("outStream");
//   response.setHeader("Content-Description", "File Transfer"); --что делает это и далее?
// header('Content-Type: application/octet-stream');
// header('Content-Transfer-Encoding: binary');
// header('Expires: 0');
// header('Cache-Control: must-revalidate');
// header('Pragma: public');
            response.setContentType(content_type);
            response.setHeader("Content-length", Long.toString(file_size));

            fileName = URLEncoder.encode(fileName, "UTF8");
            if (userAgent != null
                    && (userAgent.contains("chrome")
                    || userAgent.contains("msie")) ) {
//        System.out.println("*** chrome, msie");
                response.setHeader("Content-Disposition",
                        "attachment; filename=\"" + fileName + "\"");
            } else if (userAgent.contains("firefox")){
                response.setHeader(
                        "Content-Disposition",
                        "attachment; filename*=\"utf-8'" + fileName + "\"");
//        System.out.println("*** firefox");
            } else {
              fileName = cfgTuner.getParameter("FILE_NAME").trim().replaceAll(" ", "_");
              fileName = Transliterator.transliterate(fileName);
//        System.out.println("*** OTHER: filename=" + fileName);
                response.setHeader(
                        "Content-Disposition",
                        "attachment; filename=" + fileName );
            }

            outStream.flush();

            FileContent.copyFileData(file_path, outStream);
            outStream.flush();
            outStream.close();
        } else {
            cfgTuner.addParameter("ERROR", "Файл не найден! fs=" +  Long.toString(file_size));
        }

    }
}
