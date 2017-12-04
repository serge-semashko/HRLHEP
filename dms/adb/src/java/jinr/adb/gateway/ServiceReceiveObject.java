package jinr.adb.gateway;

import dubna.walt.util.IOUtil;
import dubna.walt.util.gateway.Utils;

/**
 *
 * @author serg
 */
public class ServiceReceiveObject extends dubna.walt.service.Service {
        @Override
    public void start() throws Exception {

        cfgTuner.getCustomSection("LOG REQUEST");        
        String object = cfgTuner.getParameter("Object");
        IOUtil.writeLogLn(3, "<hr><b>ServiceReceiveObject.start(): ObjectType=</b>" + cfgTuner.getParameter("ObjectType"), rm);
        if(cfgTuner.enabledOption("ObjectType=0")) {
            IOUtil.writeLogLn(2, "<hr><b>ServiceReceiveObject: Processing command...</b>" , rm);

            Utils.parseJson(object, rm); 
            if(!cfgTuner.enabledOption("ERROR")) {
                cfgTuner.outCustomSection("command responce", out); 
                try { out.flush(); out.close(); } catch (Exception e) {;}
                cfgTuner.getCustomSection("process command");
            }
            else {
                cfgTuner.outCustomSection("ERROR", out); 
                try { out.flush(); out.close(); } catch (Exception e) {;}
            }
        }
        else {
            IOUtil.writeLogLn(2, "<hr><b>ServiceReceiveObject: Receiving object...</b>" , rm);
            if(!cfgTuner.enabledOption("ERROR"))
                Utils.parseJson(object, rm); 
//parseJson(object);
            if(!cfgTuner.enabledOption("ERROR"))
                cfgTuner.getCustomSection("process request");        

            super.start();
        }
        
//        cfgTuner.addParameter("RESPONCE", responce);
//        IOUtil.writeLogLn("RESPONCE:" + responce, rm);
        
        
        cfgTuner.getCustomSection("process post result");   
        cfgTuner.addParameter("CloseSession", "Y");
    }

}
