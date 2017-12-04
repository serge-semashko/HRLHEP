package jinr.sed;

import dubna.walt.util.Fmt;

/**
 *
 * @author serg
 */
public class ServiceTest extends dubna.walt.service.Service {
    	public void start () throws Exception
	{	
            long t = System.currentTimeMillis();
            long sec = t / 1000;
            long min = sec / 60; 
            long hr = min / 60;
            cfgTuner.addParameter("hr", Long.toString(hr));
            cfgTuner.addParameter("min", Long.toString(min));
            cfgTuner.addParameter("sec", Long.toString(sec));
            
            cfgTuner.addParameter("DATE", Fmt.shortDateStr(new java.util.Date()));
            super.start();
	}
    
}
