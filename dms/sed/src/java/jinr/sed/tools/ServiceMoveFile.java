
package jinr.sed.tools;

import dubna.walt.util.FileContent;

/**
 *
 * @author Куняев
 */



public class ServiceMoveFile extends dubna.walt.service.Service {

    public void start() throws Exception {

        cfgTuner.getCustomSection("report header");
        if (!cfgTuner.enabledExpression("ERROR")) {
            try {
                FileContent.moveFile(cfgTuner.getParameter("OLD_DOC_PATH"), cfgTuner.getParameter("NEW_DOC_PATH"));
                
            } catch (Exception e) {
                e.printStackTrace(System.out);
                cfgTuner.addParameter("ERROR", e.toString());
            }
        }
        if (!cfgTuner.enabledExpression("ERROR")) {
            cfgTuner.getCustomSection("report footer");
        }
        else {
            cfgTuner.outCustomSection("ERR_MSG", out);
        }

    }
}

