package jinr.gateway;

/**
 *
 * @author serg
 */
public class ServiceParseDoc extends dubna.walt.service.Service {
    
    long timer;

    /**
     *
     * @throws Exception
     */
    @Override
    public void start() throws Exception {
        timer = System.currentTimeMillis();
        super.start();

        String object = cfgTuner.getParameter("object");
        if (object.length() > 2) {
            dubna.walt.util.gateway.Utils.parseJson(object, rm);
        }

        cfgTuner.getCustomSection("process object");

    }

}
