package jinr.gateway;

import dubna.walt.util.IOUtil;
import java.net.URLEncoder;
import dubna.walt.util.gateway.Utils;

/**
 *
 * @author serg
 */
public class ServicePostDoc extends dubna.walt.service.Service {

    long timer;

    /**
     *
     * @throws Exception
     */
    @Override
    public void start() throws Exception {
        timer = System.currentTimeMillis();
        super.start();
        String responce = "";
        long t1 = System.currentTimeMillis();
        long t2 = t1;
try{
        String encodedData = "";
        if (cfgTuner.enabledOption("parameters")) {
            encodedData = cfgTuner.getParameter("parameters") + "&";
        }
        encodedData += Utils.getEncodedSection("post params", rm);

        String object = cfgTuner.getParameter("object");

//        IOUtil.writeLogLn(9, "object=" + object + ";;;", rm);
        if (object.length() > 2) {
            object = object.replaceAll("``", "\\\\\"");
            IOUtil.writeLogLn(9, "***** ServicePostDoc: object=<xmp>" + object + "</xmp>", rm);
            object = URLEncoder.encode(object, cfgTuner.getParameter("encoding"));
//        IOUtil.writeLogLn("encoded object=" + object + ";;;", rm);
            encodedData += "&Object=" + object;
        }

        cfgTuner.addParameter("encodedData", encodedData);  //for debug

//        rm.setParam("log", "true");
        t1 = System.currentTimeMillis();
        IOUtil.writeLogLn(2, "***** TIMER 1 (get data): " + Long.toString(t1 - timer), rm);

// Посылаем запрос и читаем ответ     
        responce = Utils.postRequest(cfgTuner.getParameter("URL"), encodedData, rm);
        t2 = System.currentTimeMillis();
        IOUtil.writeLogLn(2, "***** TIMER 2 (post data): " + Long.toString(t2 - t1), rm);

        cfgTuner.getCustomSection("reset request params");
        cfgTuner.addParameter("RESPONCE", responce);
        IOUtil.writeLogLn(1, "RESPONCE:<xmp>" + responce +"</xmp><hr>", rm);
        
        if(cfgTuner.enabledOption("responceCode=200"))
            cfgTuner.addParameter("ResultCode", "0");
        else
            cfgTuner.addParameter("ResultCode", "2");
        cfgTuner.addParameter("Result","?");
//        if(!cfgTuner.enabledOption("ERROR"))
        if(responce.length() > 10)
            try {
                Utils.parseJson(responce, rm);
            } catch (Exception e) {
                cfgTuner.addParameter("ResultCode", "3");
                cfgTuner.addParameter("Result",responce + ":" + e.toString());
            }
}
catch (Exception ex) {
        cfgTuner.addParameter("ResultCode", "3");
        cfgTuner.addParameter("Result", responce + ":" + ex.toString());
    
}
        cfgTuner.getCustomSection("process post result");

        long t3 = System.currentTimeMillis();
        IOUtil.writeLogLn(2, "***** TIMER 3 (process post result): " + Long.toString(t3 - t2), rm);
    }

}
