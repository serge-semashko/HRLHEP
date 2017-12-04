package jinr.gateway;

import dubna.walt.util.IOUtil;
import dubna.walt.util.gateway.Utils;

/**
 *
 * @author serg
 */
public class ServiceGetNewObject extends dubna.walt.service.Service {

    long timer;

    /**
     *
     * @throws Exception
     */
    @Override
    public void start() throws Exception {
        timer = System.currentTimeMillis();
        super.start();
        String responce = "{" + Utils.getEncodedJSON("responce", rm, true) ;
        String object = cfgTuner.getParameter("Object");
        if(object.length() > 3) {
            object = object.replaceAll("``", "\\\\\"");
            responce += ", \"Object\":[" + object + "]}";
        }
        else
            responce += "}";
        out.print(responce);
        out.flush();
        out.close();

//        cfgTuner.addParameter("encodedData", responce);  //for debug
        IOUtil.writeLogLn(1, "<hr><b>RESPONCE TO CLIENT:</b><xmp>" + responce + "</xmp><hr>", rm);
        String[] dummy=cfgTuner.getCustomSection("finalize");
//        rm.setParam("log", "true");
        long t1 = System.currentTimeMillis();
        IOUtil.writeLogLn(2, "***** TIMER 1 (send object): " + Long.toString(t1 - timer), rm);


    }

}
