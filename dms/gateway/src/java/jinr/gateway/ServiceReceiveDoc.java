package jinr.gateway;

import dubna.walt.util.IOUtil;
import dubna.walt.util.gateway.Utils;

/**
 *
 * @author serg
 */
public class ServiceReceiveDoc extends dubna.walt.service.Service {

    /**
     * Принимает объект от источника и рассылает его приемникам
     *
     * @throws Exception
     */
    @Override
    public void start() throws Exception {
        String o = cfgTuner.getParameter("Object");
        IOUtil.writeLogLn(9, "==== ServiceReceiveDoc: 0: OBJECT=<xmp>" + o + "</xmp>", rm);
        if (o.length() > 3) {
            o=Utils.parseJson(o, rm);
            if (!cfgTuner.enabledOption("ERROR")) {
//            IOUtil.writeLogLn("==== ServiceReceiveDoc: 1: OBJECT=<xmp>" + o + "</xmp>", rm);
                o = o.replaceAll("\\\\\"", "``");
//                IOUtil.writeLogLn("==== ServiceReceiveDoc: 2: OBJECT=<xmp>" + o + "</xmp>", rm);
                cfgTuner.addParameter("Object", o);
            }
        }
        if (!cfgTuner.enabledOption("ERROR")) {
            cfgTuner.getCustomSection(null, "process object", null);
        }

//        if (!cfgTuner.enabledOption("ERROR")) {
//            cfgTuner.getCustomSection(null, "queue object", null);
//        }

        super.start();
//        out.flush(); out.close();
    }

}
