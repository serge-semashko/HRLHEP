package jinr.sed.gateway;

import dubna.walt.util.IOUtil;
import dubna.walt.util.ResourceManager;
import dubna.walt.util.Tuner;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.util.Iterator;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

/**
 *
 * @author serg
 */
public class zzz_Utils {

    /**
     * Посылает POST-запрос
     *
     * @param host - URL хоста, куда слать запрос
     * @param encodedData - содержимое запроса
     * @param rm
     * @return - полученный ответ или сообщение об ошибке
     */
    public static String postRequest(String host, String encodedData, ResourceManager rm) {
        Tuner cfgTuner = (Tuner) rm.getObject("cfgTuner");
        String responce = "";
        try {
            IOUtil.writeLogLn("POST to:" + host, rm);
            HttpURLConnection connection = (HttpURLConnection) new URL(host).openConnection();
            connection.setConnectTimeout(2000);
            connection.setRequestMethod("POST");
            connection.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
//               conn.setRequestProperty( "Content-Type", "application/json; charset=UTF-8");

            connection.setRequestProperty("Content-Length", String.valueOf(encodedData.length()));
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

        return responce;
    }

    /**
     * Парсит строку с JSON-объектом и раскладывает значения параметров в
     * cfgTuner запроса
     *
     * @param json
     * @param rm
     * @throws ParseException
     */
    public static boolean parseJson(String json, ResourceManager rm) throws ParseException {
        Tuner cfgTuner = (Tuner) rm.getObject("cfgTuner");
        IOUtil.writeLogLn("+++parseJson: json='" + json + "'", rm);

        if (json.length() < 2) {
            return true;
        }
        try {
            JSONParser parser = new JSONParser();
            JSONObject jsonObj = (JSONObject) parser.parse(json);
            String keys = "";
            String comma = "";
            String key = "";
            String val = "";
            Object item;

//        JSONParser aParser = new JSONParser();
            JSONObject aObj;
            Object aItem;
//        IOUtil.writeLogLn(jsonObj.get("paramsStr"), rm);

            for (Iterator iterator = jsonObj.keySet().iterator(); iterator.hasNext();) {
                key = (String) iterator.next();
                item = jsonObj.get(key);
                if (item instanceof String) {
                    val = (String) item;
                } else {
                    val = "";
                    JSONArray msg = (JSONArray) jsonObj.get(key);
                    Iterator<String> a_iterator = msg.iterator();

//                    System.out.println("ARRAY " + key + ":");
                    while (a_iterator.hasNext()) {
                        aItem = a_iterator.next();
                        if (aItem instanceof JSONObject) {
//                        aObj = (JSONObject) aParser.parse(aItem);

//                            System.out.println(aItem.toString());
                            val += aItem.toString();
                        } else {
                            cfgTuner.addParameter("ERROR", "JSON: Ошибка в табличной части " + key);
                            cfgTuner.addParameter("ERR_CODE", "1");
                        }
                    }
                }
                keys += comma + key;
                cfgTuner.addParameter(key, val);
                IOUtil.writeLogLn("* " + key + "=" + val, rm);
                comma = ", ";
            }
            cfgTuner.addParameter("JSON Keys", keys);
            IOUtil.writeLogLn("* JSON Keys=" + keys, rm);
        } catch (Exception e) {
            IOUtil.writeLogLn("<b>JSON ERROR: " + e.toString() + "</b>", rm);
            e.printStackTrace();
            cfgTuner.addParameter("ERROR_JSON", e.toString());
            cfgTuner.addParameter("ERROR", "JSON - ошибка в формате");
            cfgTuner.addParameter("ERR_CODE", "1");
            return false;
        }
        return true;
    }

    /**
     * Преобразовать содержимое секции из вида: "name=value" к строке запроса
     * name=encValue&name2=encValue2&...
     *
     * @param src
     * @param rm
     * @return
     * @throws UnsupportedEncodingException
     */
    public static String encodeString(String src, ResourceManager rm) throws UnsupportedEncodingException {
        Tuner cfgTuner = (Tuner) rm.getObject("cfgTuner");
        String enc = cfgTuner.getParameter("encoding");
        if (enc.length() < 2) 
            enc = "utf-8";

        return URLEncoder.encode(src, cfgTuner.getParameter("encoding"));
    }

    /**
     * Преобразовать содержимое секции из вида: "name=value" к строке запроса
     * name=encValue&name2=encValue2&...
     *
     * @param sectionName
     * @param rm
     * @return
     * @throws UnsupportedEncodingException
     */
    public static String getEncodedSection(String sectionName, ResourceManager rm) throws UnsupportedEncodingException {
        Tuner cfgTuner = (Tuner) rm.getObject("cfgTuner");
        String encodedData = "";

        String[] sa = cfgTuner.getCustomSection(sectionName);
        String[] pair;
        String amp = "";
        for (String sa1 : sa) {
            pair = sa1.split("=");
            String val = "";
            if (pair.length > 1) {
                IOUtil.writeLogLn(pair[0].trim() + "=" + pair[1].trim() + ";", rm);
                val = pair[1].trim();
                if (cfgTuner.enabledOption("encoding")) {
                    val = URLEncoder.encode(val, cfgTuner.getParameter("encoding"));
                }
                encodedData += amp + pair[0] + "=" + val;
                amp = "&";
            }
//            encodedData += amp + pair[0] + "=" + pair[1];
        }
        return encodedData;
    }

    /**
     * Преобразовать содержимое секции из вида: name=value к строке в JSON:
     * "name":"encValue", "name2":"encValue2",... Внешние скобки { и } не
     * добавляются
     *
     * @param sectionName
     * @param rm
     * @param encode
     * @return
     * @throws UnsupportedEncodingException
     */
    public static String getEncodedJSON(String sectionName, ResourceManager rm, boolean encode) throws UnsupportedEncodingException {
        Tuner cfgTuner = (Tuner) rm.getObject("cfgTuner");
        String encodedJson = "";

        String[] sa = cfgTuner.getCustomSection(sectionName);
        String[] pair;
        String comma = "";
        for (String sa1 : sa) {
            int k = sa1.indexOf("=");
            String val = "";
            if(k>0) {
//                val = sa1.substring(k+1).trim().replace("\"", "\\\"");
                val = sa1.substring(k+1).trim().replace("\"", "`");
        IOUtil.writeLogLn(sa1.substring(0, k) + "/=" + val + ";", rm);
                if (encode && cfgTuner.enabledOption("encoding")) {
                    val = URLEncoder.encode(val, cfgTuner.getParameter("encoding"));
                }
                encodedJson += comma + " \"" + sa1.substring(0, k) + "\":\"" + val + "\"";
            }
            else
                encodedJson += comma + " \"" + sa1 + "\":\"\"";
/*            pair = sa1.split("=");
            if (pair.length > 1) {
                val = pair[1].trim().replace("\"", "\\\"");
//        IOUtil.writeLogLn(pair[0].trim() + "=" + val + ";", rm);
                if (encode && cfgTuner.enabledOption("encoding")) {
                    val = URLEncoder.encode(val, cfgTuner.getParameter("encoding"));
                }
            }
            encodedJson += comma + " \"" + pair[0] + "\":\"" + val + "\"";
/**/            
            comma = ",";
//            encodedData += amp + pair[0] + "=" + pair[1];
        }
        IOUtil.writeLogLn("[" + sectionName + "]=>" + encodedJson + ";", rm);
        return encodedJson;
    }

}
