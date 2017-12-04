/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package jsonsrv;

import static jsonsrv.Jsonsrv.paramReqTry;
import com.sun.net.httpserver.HttpContext;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpServer;
import java.io.*;
import java.net.*;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import java.util.ArrayList;
import java.util.List;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import static jsonsrv.Jsonsrv.paramPORT;
import static jsonsrv.Jsonsrv.paramReceiveDelay;
import static jsonsrv.Jsonsrv.paramURL;

public class ProcServer extends Thread {

    JSONObject Config;
    /**
     * Порт, на котором служба ожидает подключения (по умлочанию)
     */
    static volatile long FirstReq = -1;
    static volatile long LastReq = -1;
    static volatile long currStepTime = -1;

    static volatile int ReqReceived = -1;
    String PORTstr = "";
    JSONParser jParser;
    static public ProcessJSONRequest JSONProcessing;
    private boolean Running;
    private int ReqCount;
    private long ObjectID = 1;
    private byte[] inbuf;

    public ProcServer() {
        inbuf = new byte[1000000];
//        System.out.println("Start thread srv: ");
        Config = new JSONObject();
        jParser = new JSONParser();
//        try {
//            JSONProcessing = new ProcessJSONRequest();
//            Object obj = jParser.parse(new FileReader(Jsonsrv.ConfigFile));
//            Config = (JSONObject) obj;
//        } catch (Exception e) {
//            e.printStackTrace();
//            System.out.println("Procserver: Error config file");
//        }
        start();
    }

    /**
     * returns the url parameters in a map
     *
     * @param query
     * @return map
     */
    String excutePost(String targetURL, String urlParameters) {
        URL url;
        HttpURLConnection connection = null;
        try {
            //Create connection
            url = new URL(targetURL);
//            System.out.println("ProcPost URL:" + targetURL);

//       System.out.println("Port Post : "+ url.getPort());
            connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("POST");
            connection.setRequestProperty("Content-Type",
                    "application/x-www-form-urlencoded");

            connection.setRequestProperty("Content-Length", ""
                    + Integer.toString(urlParameters.getBytes().length));
            connection.setRequestProperty("Content-Language", "en-US");

            connection.setUseCaches(false);
            connection.setDoInput(true);
            connection.setDoOutput(true);

            //Send request
            DataOutputStream wr = new DataOutputStream(
                    connection.getOutputStream());
            wr.writeBytes(urlParameters);
            wr.flush();
            wr.close();

            //Get Response	
            InputStream is = connection.getInputStream();
            BufferedReader rd = new BufferedReader(new InputStreamReader(is));
            String line;
            StringBuffer response = new StringBuffer();
            while ((line = rd.readLine()) != null) {
                response.append(line);
                response.append(' ');
            }
            rd.close();
            return response.toString();

        } catch (Exception e) {

            e.printStackTrace();
            return null;

        } finally {

            if (connection != null) {
                connection.disconnect();
            }
        }
    }

    void ProcStat() {
        try {
            SimpleDateFormat dateFormat;
            SimpleDateFormat IDdateFormat;
            dateFormat = new SimpleDateFormat(" dd.MM.yyyy HH:mm:ss");
            IDdateFormat = new SimpleDateFormat(" dd_HH_mm_ss");
            String urlParameters = "Ver=" + URLEncoder.encode("1", "UTF-8")
                    + "&ClientID=" + URLEncoder.encode(Jsonsrv.paramClientId, "UTF-8")
                    + "&Command=" + URLEncoder.encode("PutObject", "UTF-8")
                    + "&ObjectType=" + URLEncoder.encode(Jsonsrv.paramObjectType, "UTF-8")
                    //                    + "&ClientObjectID=" + URLEncoder.encode(Long.toString(ObjectID++), "UTF-8")
                    + "&ClientObjectID=" + URLEncoder.encode(IDdateFormat.format(new Date()) + "/ " + Long.toString(ObjectID), "UTF-8")
                    + "&Time=" + URLEncoder.encode(dateFormat.format(new Date()), "UTF-8")
                    + "&Object=" + URLEncoder.encode(Jsonsrv.testObject.toString(), "UTF-8");
//      String postres = excutePost(Jsonsrv.paramURL,urlParameters);
            Long tm = System.currentTimeMillis();
            String postres = excutePost(Jsonsrv.paramURL, urlParameters);
            tm = System.currentTimeMillis() - tm;
            System.out.println("Post:" + ObjectID + " Result:" + postres + tm + "ms.");
            ObjectID++;
//      String postres = excutePost(" http://62.84.109.196:81/",urlParameters);
        } catch (Exception e) {
            System.out.println("ProcStat : Error encoding");
            e.printStackTrace();
        }

    }

    public static Map<String, String> queryToMap(String query) {
        Map<String, String> result = new HashMap<String, String>();
        for (String param : query.split("&")) {
            String pair[] = param.split("=");
            if (pair.length > 1) {
                result.put(pair[0], pair[1]);
            } else {
                result.put(pair[0], "");
            }
        }
        return result;
    }

    public static void writeResponse(HttpExchange httpExchange, String response) throws IOException {

        httpExchange.sendResponseHeaders(200, response.length());
        OutputStream os = httpExchange.getResponseBody();
        os.write(response.getBytes());
        os.close();

    }

    public static void parseQuery(String query, Map<String, Object> parameters) throws UnsupportedEncodingException {
        if (query != null) {
            String pairs[] = query.split("[&]");
            for (String pair : pairs) {
                String param[] = pair.split("[=]");
                String key = null;
                String value = null;
                if (param.length > 0) {
                    key = URLDecoder.decode(param[0],
                            System.getProperty("file.encoding"));
                }

                if (param.length > 1) {
                    value = URLDecoder.decode(param[1],
                            System.getProperty("file.encoding"));
                }

                if (parameters.containsKey(key)) {
                    Object obj = parameters.get(key);
                    if (obj instanceof List<?>) {
                        List<String> values = (List<String>) obj;
                        values.add(value);

                    } else if (obj instanceof String) {
                        List<String> values = new ArrayList<String>();
                        values.add((String) obj);
                        values.add(value);
                        parameters.put(key, values);
                    }
                } else {
                    parameters.put(key, value);
                }
            }
        }
    }

    public class PostHandler implements HttpHandler {

        @Override
        public void handle(HttpExchange he) throws IOException {
//            StringBuilder builder = new StringBuilder();
//            Headers headers = he.getRequestHeaders();
//            for (String header : headers.keySet()) {
//                String hdr = header;
//                String hdrval = headers.getFirst(header);
//                builder.append("<p>").append(hdr).append("=")
//                        .append(headers.getFirst(hdrval)).append("</p>");
//            }
//            System.out.println("req head: " + builder.toString());
            // parse request
            ReqReceived++;
            Map<String, Object> parameters = new HashMap<String, Object>();
            InputStreamReader isr = new InputStreamReader(he.getRequestBody(), "utf-8");
            BufferedReader br = new BufferedReader(isr);
            String query = br.readLine();
//            System.out.println("Nreq:"+ReqReceived+" Body:"+ query);
            parseQuery(query, parameters);

            // send response
            String response = "";
            for (String key : parameters.keySet()) {
                response += key + " = " + parameters.get(key) + "\n";
//                System.out.println("key: " + key + " value: " + parameters.get(key));
            }
            JSONObject jAnsw = new JSONObject();
            JSONObject jObject = new JSONObject();
            String ObjectStr = parameters.get("Object").toString();
            Object obj;

            try {
                obj = jParser.parse(ObjectStr);
                jObject = (JSONObject) obj;
                jAnsw.put("ResultCode", "0");
                jAnsw.put("Result", "testing");
                jAnsw.put("ClientObjectId", parameters.get("GateObjectID"));
            } catch (Exception ex) {
                Logger.getLogger(ProcServer.class.getName()).log(Level.SEVERE, null, "Error pasre object JSON:" + ex);
                jAnsw.put("ResultCode", "1");
                jAnsw.put("Result", "Bad json for object");
            }

            response = jAnsw.toString();
            he.sendResponseHeaders(200, response.length());
            OutputStream os = he.getResponseBody();
            os.write(response.getBytes());
            os.close();
            LastReq = System.currentTimeMillis();
            System.out.println("RECEIVED:" + parameters.get("ClientObjectID") + " (" + (LastReq - currStepTime) + "ms)");
            currStepTime = LastReq;
            if (paramReceiveDelay > 0) {
                try {
//                Thread.currentThread().sleep(1000);
                    sleep(paramReceiveDelay);
                } catch (InterruptedException ex) {
                    Logger.getLogger(ProcServer.class.getName()).log(Level.SEVERE, null, ex);
                }
            }

        }
    }

    public void run() {
        try {
            System.out.println("thread srv run ");
            HttpServer server = HttpServer.create();
            if (paramPORT > 0) {
                System.out.println("Server Started on PORT " + String.valueOf(paramPORT));
                server.bind(new InetSocketAddress(paramPORT), 0);

                HttpContext context = server.createContext("/", new PostHandler());

                server.setExecutor(null);
                server.start();
            } else {
                System.out.println("HTTP server port not defined. HTTP Server disabled");

            }

            if (paramURL.equals("-")) {
                System.out.println("URL not defined. Send requests disabled");
                Jsonsrv.paramReqTry = 0;

            } else {
                System.out.println("Send requests on URL " + (paramURL));
            }
            if (paramURL.equals("-") && (paramPORT == 0)) {
                System.out.println("Send requests disabled, receive disabled too/ What do You want? ");
                return;
            }
            Running = true;
            ReqCount = 0;
            ReqReceived = 0;
            try {
                Thread.currentThread().sleep(100);
            } catch (Exception e) {
            }
            try {
                FirstReq = System.currentTimeMillis();
                currStepTime = FirstReq;
                while (Running) {
                    try {
                        Thread.currentThread().sleep(1);

                    } catch (InterruptedException ex) {
                        Logger.getLogger(ProcServer.class.getName()).log(Level.SEVERE, null, ex);
                    }
                    if ((ReqCount < paramReqTry) && !paramURL.equals("-")) {
                        ReqCount++;
//                        System.out.println("try post =" + ReqCount);
                        ProcStat();
                    };
                    if ((ReqCount > paramReqTry) && !paramURL.equals("-")) {
                        long curTime = System.currentTimeMillis();;
                        if (LastReq > 0) {
                            if ((curTime - LastReq) / 1000 > 3) {
//                                System.out.println("receive delay = " + (curTime - LastReq) / 1000);
                                server.stop(1);
                                try {
                                    Thread.currentThread().sleep(1000);
                                } catch (InterruptedException ex) {
                                    Logger.getLogger(ProcServer.class.getName()).log(Level.SEVERE, null, ex);
                                }
                                Jsonsrv.DeviceIsTesting = false;
                                Running = false;
                            }
                        }
//                        System.out.print(" ReqReceived " + ReqReceived);
                    }
                    if (ReqCount == paramReqTry) {
                        long Fin = System.currentTimeMillis() - FirstReq;
                        System.out.println("Send.    Count=" + ReqCount + " Object size=" + Jsonsrv.paramSize
                                + " Post/sec: " + paramReqTry / (Fin / 1000.0));
                        ReqCount++;
                        if (paramPORT == 0) {
                            Jsonsrv.DeviceIsTesting = false;
                            Running = false;
                            return;
                        }
                    }
                    if (!paramURL.equals("-") && (paramPORT != 0)) {
                        if (ReqReceived >= paramReqTry) {
                            long dTime = LastReq - FirstReq;
                            System.out.println("Receive. Count=" + ReqReceived + " Object size=" + Jsonsrv.paramSize
                                    + " Request/sec: " + ReqReceived / (dTime / 1000.0));
                            ReqReceived = -10000;
                        }
                    }
                };

            } catch (Exception e) {
                System.err.println("Main cycle: Exception");
                e.printStackTrace();
            }
        } catch (Exception e) {
            System.err.println("Thread run(): Exception");
            e.printStackTrace();
        }

    }
}
