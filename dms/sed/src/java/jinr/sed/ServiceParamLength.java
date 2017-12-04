package jinr.sed;

/**
 *
 * @author serg
 */
public class ServiceParamLength extends dubna.walt.service.Service {

    @Override
    public void start() throws Exception {
//               if(iv == null)
        cfgTuner.addParameter("PARAM_LENGTH", "");
        InputValidator iv = (InputValidator) rm.getObject("InputValidator", false);
        if (iv == null) {
            iv = new InputValidator(rm.getGlobalRM());
            rm.getGlobalRM().putObject("InputValidator", iv);
        }

        String param_name = cfgTuner.getParameter("INPUT_PARAM_NAME");
        int docType = cfgTuner.getIntParameter(null, "DOC_TYPE_ID", 0);
//        if(objectType == 0) objectType=cfgTuner.getIntParameter(null, "OBJECT_TYPE_ID", 0);
        InputParameter ip = null;
        if (docType > 0) {
            ip = iv.getParamInfo(param_name, docType);
        }
        if (ip == null) {
            getData("getParamInfo");
            iv.addParamInfo(param_name, Integer.toString(docType), cfgTuner.getParameter("name") + "(" + param_name + ")",
                     cfgTuner.getParameter("type"), cfgTuner.getIntParameter(null, "size", 0));
        }
//        else 
        if (ip != null && ip.fieldSize > 0) {
            cfgTuner.addParameter("PARAM_LENGTH", Integer.toString(ip.fieldSize));
            cfgTuner.addParameter("PARAM_TYPE", ip.fieldType);
            cfgTuner.addParameter("objectType", Integer.toString(docType));
        }
        super.start();
    }

}
