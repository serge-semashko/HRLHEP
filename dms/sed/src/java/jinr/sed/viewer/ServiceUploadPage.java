package jinr.sed.viewer;

import dubna.walt.util.IOUtil;
import java.io.File;
import java.net.URL;
import org.apache.commons.io.FileUtils;

public class ServiceUploadPage extends dubna.walt.service.Service {

    @Override
    public void start() throws Exception {

        cfgTuner.getCustomSection("report header");
        if (!cfgTuner.enabledExpression("ERROR")) {
            try {
                String outF = cfgTuner.getParameter("FILE_PATH");
                String u = cfgTuner.getParameter("url");
                URL url= new URL(u);
                IOUtil.writeLogLn(1, "try to get page from url:" + u + " => " + outF, rm);
                FileUtils.copyURLToFile(url, new File(outF));
                IOUtil.writeLogLn(1, " FileUtils - DONE!" , rm);
                File f = new File(outF);
                long l = f.length();
                IOUtil.writeLogLn(1, "ServiceUploadPage: Got file of " + l + " bytes.", rm);
                cfgTuner.addParameter("file_size", Long.toString(l));
            } catch (Exception e) {
                e.printStackTrace(System.out);
                cfgTuner.addParameter("ERROR", e.toString());
            }
        }
        cfgTuner.outCustomSection("report footer", out);
        out.close();
    }
}
