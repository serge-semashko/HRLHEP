/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package myiss;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;

/**
 *
 * @author Pavel
 */
public class JinrUser {

    private String LOGIN = null;
    private String FIO = null;
    private String TEL = null;
    private String MAIL = null;
    private String LAB = null;
    private String ID = null;
    private String DATE = null;
    private String SERVICE = null;
    private String TOKEN = null;
    private Date EXPIRE = null;

    private String getUrlencodedParams(HashMap mp) throws UnsupportedEncodingException {
        Iterator it = mp.entrySet().iterator();
        StringBuilder sb = new StringBuilder();
        int c = 0;
        while (it.hasNext()) {
            Map.Entry pair = (Map.Entry) it.next();
            if (c > 0) {
                sb.append("&");
            }
            c++;
            sb.append(pair.getKey());
            sb.append("=");
            sb.append(URLEncoder.encode((String) pair.getValue(), "UTF-8"));
            it.remove();
        }
        return sb.toString();
    }

    private void authUser(HttpServletRequest request, HttpServletResponse response) throws Exception {
        String code = request.getParameter("code");
        HashMap params = new HashMap();
        params.put("code", code);
        params.put("redirect_uri", "http://omega.jinr.ru:8084/myiss/iss");
        params.put("pass", "is172");
        params.put("client_id", "9");
        String url = "http://login.jinr.ru/cgi-bin/token";
        URL obj = new URL(url);
        HttpURLConnection con = (HttpURLConnection) obj.openConnection();
        con.setRequestMethod("POST");
        con.setRequestProperty("Content-type", "application/x-www-form-urlencoded");
        String urlParameters = getUrlencodedParams(params);
        con.setDoOutput(true);
        DataOutputStream wr = new DataOutputStream(con.getOutputStream());
        wr.writeBytes(urlParameters);
        wr.flush();
        wr.close();
        int responseCode = con.getResponseCode();
        System.out.println("\nSending 'POST' request to URL : " + url);
        System.out.println("Post parameters : " + urlParameters);
        System.out.println("Response Code : " + responseCode);
        if (responseCode == 200) {
            BufferedReader in = new BufferedReader(new InputStreamReader(con.getInputStream()));
            String inputLine;
            StringBuilder res = new StringBuilder();
            while ((inputLine = in.readLine()) != null) {
                res.append(inputLine);
            }
            in.close();
            String resp = response.toString();
            if (!resp.isEmpty() && resp.startsWith("{") && resp.contains("access_token")) {
                JSONParser jp = new JSONParser();
                System.out.println(response.toString());
                JSONObject o = (JSONObject) jp.parse(response.toString());
                String accessToken = (String) o.get("access_token");
                String expiresIn = (String) o.get("expires_in");
                Date tokenExpiresIn = (new SimpleDateFormat("yyyy-MM-dd kk:mm:ss")).parse(expiresIn);
                TOKEN = accessToken;
                EXPIRE = tokenExpiresIn;
            } else {
                throw new Exception("couldn't get token! Response:" + response.toString());
            }
        } else {
            throw new Exception(url + ": got response code:" + responseCode);
        }

    }

    public JinrUser(HttpServletRequest request, HttpServletResponse response) throws Exception {
        if (this.TOKEN == null) {
            authUser(request, response);
        } else {
            refreshUser(request, response);
        }

    }

    private void refreshUser(HttpServletRequest request, HttpServletResponse response) throws Exception {

        if (TOKEN != null && EXPIRE != null) {
            String url = "http://login.jinr.ru/cgi-bin/infojson?access_token=" + TOKEN;
            URL obj = new URL(url);
            HttpURLConnection con = (HttpURLConnection) obj.openConnection();
            con.setRequestMethod("GET");
            StringBuilder result = new StringBuilder();
            BufferedReader rd = new BufferedReader(new InputStreamReader(con.getInputStream()));
            String line;
            while ((line = rd.readLine()) != null) {
                result.append(line);
            }
            rd.close();
            JSONParser jp = new JSONParser();
            JSONObject userinfo = (JSONObject) jp.parse(result.toString());
            LOGIN = (String) userinfo.get("login");
            FIO = (String) userinfo.get("fio");
            TEL = (String) userinfo.get("tel");
            MAIL = (String) userinfo.get("mail");
            LAB = (String) userinfo.get("lab");
            ID = (String) userinfo.get("id");
            DATE = (String) userinfo.get("date");
            SERVICE = (String) userinfo.get("service");
        }

    }

    public String getLogin() {
        return this.LOGIN;
    }

    public String getFio() {
        return this.FIO;
    }

    public String getTel() {
        return this.TEL;
    }

    public String getMail() {
        return this.MAIL;
    }

    public String getLab() {
        return this.LAB;
    }

    public String getId() {
        return this.ID;
    }

    public String getDate() {
        return this.DATE;
    }

    public int getService() {
        return Tools.parseInt(this.SERVICE, -1);
    }

}
