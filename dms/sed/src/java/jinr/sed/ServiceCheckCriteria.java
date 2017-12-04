package jinr.sed;

import dubna.walt.util.IOUtil;

public class ServiceCheckCriteria extends dubna.walt.service.Service{

    @Override
    public void start () throws Exception
	{
            String criteria = cfgTuner.getParameter("custom_criteria");
            IOUtil.writeLog(3, "ServiceCheckCriteria: criteria=" + criteria + "; res=" + cfgTuner.enabledExpression(criteria), rm);
            if (cfgTuner.enabledExpression(criteria))
                cfgTuner.addParameter("CRITERIA_OK", "Y");
            else
                cfgTuner.addParameter("CRITERIA_OK", "");
            super.start();
        }
    }
