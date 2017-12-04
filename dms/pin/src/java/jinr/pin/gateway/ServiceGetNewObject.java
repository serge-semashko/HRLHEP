package jinr.pin.gateway;


import dubna.walt.util.IOUtil;
import dubna.walt.util.gateway.Utils;

/**
 *
 * @author serg
 */
public class ServiceGetNewObject extends dubna.walt.service.Service{
    
private long timer;
    /**
     *
     * @throws Exception
     */
    @Override
    public void start() throws Exception {
        timer = System.currentTimeMillis();
        try { 

// Готовим параметры запроса
            String encodedData = Utils.getEncodedSection("request", rm);

// Посылаем запрос и читаем ответ             
            String responce = Utils.postRequest(cfgTuner.getParameter("GatewayHost"), encodedData, rm);
            IOUtil.writeLogLn(3, "***** TIMER (post data): " + Long.toString(System.currentTimeMillis() - timer), rm);
            IOUtil.writeLogLn(3, "RESPONCE:" + responce, rm);
            cfgTuner.addParameter("responce", responce);

            boolean ok = Utils.parseJson(responce, rm);
//            if (ok && cfgTuner.enabledOption("Object")) {
//                String object = cfgTuner.getParameter("Object").replace('[', ' ').replace(']', ' ') ;
//                ok = Utils.parseJson(object, rm);
//            }
            if(!ok) 
                cfgTuner.addParameter("ERROR", "Ошибка распознавания JSON");

        } catch (Exception e) {
            e.printStackTrace(System.out);
            IOUtil.writeLogLn("XXXXXXXX Exception: " + e.toString(), rm);
            cfgTuner.addParameter("ERROR", e.toString());
            cfgTuner.addParameter("Result", e.toString());
        } finally {
// выводим завершение формы 
            cfgTuner.outCustomSection("report", out);
            out.flush();
        }
    }


}
