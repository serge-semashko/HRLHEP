/*
 * Сервис загрузки файла в ФХ с привязкой к документу
 * Автор: Куняев
 */
package jinr.sed;

import dubna.walt.util.IOUtil;
import jinr.sed.viewer.ConvertMonitor;

public class ServiceUploadFile2 extends dubna.walt.service.Service {

    /**
     *
     * @throws Exception
     */
    @Override
    public void start() throws Exception {
        try {
            cfgTuner.getCustomSection("report header");
            if (!cfgTuner.enabledExpression("ERROR")) {
                dubna.walt.util.FileContent fc = (dubna.walt.util.FileContent) rm.getObject("new_file_CONTENT");
                fc.storeToDisk(cfgTuner.getParameter("FILE_PATH"), cfgTuner.getParameter("FILE_NAME"));
                cfgTuner.addParameter("file_size", Integer.toString(fc.getFileSize()));
            }

        } catch (Exception e) {
            cfgTuner.addParameter("ERROR", e.getLocalizedMessage());
            e.printStackTrace(System.out);

        }
        cfgTuner.outCustomSection("report footer", out);
        try {
            out.flush();
            out.close();
        } catch (Exception ex) {
            IOUtil.writeLogLn(0, "<font color=red> CLOSE OUT ERROR: " + ex.toString() + "</font>", rm);
        }

//======  Посылаем файл на растрирование
        if (!cfgTuner.enabledExpression("ERROR")) {
            /*            
            Thread converter = new Thread( new Runnable()
		{
                    public void run() //Этот метод будет выполняться в побочном потоке
                    {
                    }
                }
            );
/**/
            Thread converter = new Thread(new Converter());
            converter.start();
        }
    }

    private class Converter implements Runnable {
        @Override
        public void run() {
//          $CALL_SERVICE c=; file_id=#NEW_FILE_ID#;
          String[] queryParam = {"c=files/sys/sendConvertRequest"                  
                  , "file_id=" + cfgTuner.getIntParameter("NEW_FILE_ID")
                  , "ServerPath=" +  cfgTuner.getParameter("ServerPath")
                  , "ServletPath=" +  cfgTuner.getParameter("ServletPath")
          };
          ConvertMonitor.callService(queryParam, rm);
        }

    }

}

/*
            String url = "";
            try {
                cfgTuner.getCustomSection("convert file");
                String convertRequest = cfgTuner.getParameter("convertRequest");
                if (convertRequest.length() > 0) {
                    url = convertRequest
                            + "&url=" + URLEncoder.encode(cfgTuner.getParameter("getFileUrl"), "utf-8")
                            + "&urlOut=" + URLEncoder.encode(cfgTuner.getParameter("urlOut"), "utf-8");
                    IOUtil.writeLogLn(0, "<b> File Convert URL:</b>" + url, rm);

                    URL u = new URL(url);
                    URLConnection conn = u.openConnection();
                    BufferedReader in = new BufferedReader(new InputStreamReader(conn.getInputStream()));
                    String res = "";
                    String inputLine;
                    int ln = 1;
                    IOUtil.writeLogLn(3, "<b>File Convert URL RESPONCE:</b>", rm);
                    while ((inputLine = in.readLine()) != null) {
                        IOUtil.writeLogLn(3, ln++ + ": '" + inputLine + "';", rm);
                        res += inputLine;
                    }
                    in.close();
                    IOUtil.writeLogLn(0, "<b> +++++ ServiceUploadFile2: Convert URL Responce: " + res + "!</b>", rm);
                }
            } catch (Exception e) {
                IOUtil.writeLogLn(0, "<font color=red> File Convert URL: " + url + "; ERROR: " + e.toString() + "</font>", rm);
                cfgTuner.addParameter("URL_ERROR", e.toString());
            }
*/