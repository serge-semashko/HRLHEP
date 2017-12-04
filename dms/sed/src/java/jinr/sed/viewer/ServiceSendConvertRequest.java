package jinr.sed.viewer;

import dubna.walt.util.IOUtil;
import dubna.walt.util.ResourceManager;
import dubna.walt.util.Tuner;
import dubna.walt.util.gateway.Utils;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.URL;
import java.net.URLConnection;
//import org.apache.commons.io.FileUtils;

/**
 *
 * @author serg
 */
public class ServiceSendConvertRequest extends dubna.walt.service.Service {

  @Override
  public void start() throws Exception {

    long t = System.currentTimeMillis();
    cfgTuner.outCustomSection("report header", out);
    if (!cfgTuner.enabledExpression("ERROR")) {
      try {
        String serviceUrl = cfgTuner.getParameter("serviceUrl");
        String url = cfgTuner.getParameter("url");
        String urlOut = cfgTuner.getParameter("urlOut");
        String s;
        if (serviceUrl.contains("?")) {
          s = "&";
        } else {
          s = "?";
        }
//        s = serviceUrl + s + "url=" + Utils.encodeString(url, rm) + "&urlOut=" + Utils.encodeString(urlOut, rm);
        sendRequest(serviceUrl + s + "url=" + Utils.encodeString(url, rm) + "&urlOut=" + Utils.encodeString(urlOut, rm), rm);
      } catch (Throwable e) {
        e.printStackTrace(System.out);
        cfgTuner.addParameter("ERROR", e.toString());
        cfgTuner.addParameter("RESPONCE", e.toString());        
      }
    }
    else
        cfgTuner.addParameter("RESPONCE", cfgTuner.getParameter("Не выполнено"));        
//        cfgTuner.addParameter("RESPONCE", cfgTuner.getParameter("ERROR"));        
    t = System.currentTimeMillis() - t;
    cfgTuner.addParameter("TIME", Long.toString(t));
    cfgTuner.outCustomSection("report footer", out);
  }

  /**
   * Посылает GET-запрос
   *
   * @param url - URL хоста, куда слать запрос
   * @param rm
   */
  public static void sendRequest(String url, ResourceManager rm) {
    Tuner cfgTuner = (Tuner) rm.getObject("cfgTuner");
    IOUtil.writeLogLn(0, "<b>Send Convert Request: </b>url=" + url + "; ", rm);
    try {
      URL u = new URL(url);
      URLConnection conn = u.openConnection();
      conn.setConnectTimeout(2000);
      conn.setReadTimeout(10*60*1000);
      BufferedReader in = new BufferedReader(new InputStreamReader(conn.getInputStream()));
      String inputLine;
      int ln = 1;
      String responce = "";
      IOUtil.writeLogLn(5, "<b>RESPONCE:</b>", rm);
      while ((inputLine = in.readLine()) != null) {
        IOUtil.writeLogLn(5, ln++ + ": '" + inputLine + "';", rm);
        responce += inputLine + "\n\r";
      }
      in.close();
      cfgTuner.addParameter("RESPONCE", responce);
    } catch (Throwable e) {
      IOUtil.writeLogLn(0, "<font color=red> get URL " + url + "; ERROR: " + e.toString() + "</font>", rm);
      cfgTuner.addParameter("RESPONCE", e.toString());
    }
  }

}


/*
        try {
            IOUtil.writeLogLn("POST to:" + host, rm);
            HttpURLConnection connection = (HttpURLConnection) new URL(host).openConnection();
            connection.setConnectTimeout(2000);
            connection.setRequestMethod("GET");
            connection.setDoOutput(true);

            final OutputStream outputStream = connection.getOutputStream();
            outputStream.write(encodedData.getBytes());
            outputStream.close();

            IOUtil.writeLogLn("Reading responce...", rm);
            int respCode = connection.getResponseCode();
            String respMsg = connection.getResponseMessage();
            IOUtil.writeLogLn("+++++ respCode = " + respCode + ": " + respMsg, rm);
            cfgTuner.addParameter("responceMsg", respCode + ": " + respMsg);
            String enc = cfgTuner.getParameter("encoding");
            if (enc.length() < 2) {
                enc = "utf-8";
            }
//            final BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream(), "utf-8"));
            final BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream(), enc));

            for (String line = reader.readLine(); line != null; line = reader.readLine()) {
//                IOUtil.writeLogLn(": " + line, rm);
//                IOUtil.writeLogLn(line);
                responce += line + "\n";
            }

            reader.close();
        } catch (Throwable e) {
            cfgTuner.addParameter("ERROR", e.toString());
            cfgTuner.addParameter("ResultCode", "3");
            cfgTuner.addParameter("Result", e.toString());

            e.printStackTrace();
            return e.toString();
        }
 */
