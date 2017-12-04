package jinr.pin;

import dubna.walt.util.IOUtil;

public class ServiceExecuteSqlScript extends dubna.walt.service.Service
{

public void start() throws Exception
{
  cfgTuner.outCustomSection("report header",out);
  String s = cfgTuner.getParameter("contents1");
  String delim = cfgTuner.getParameter("delimiter");
  if (delim.length() == 0) delim = ";";
  String[] sa = s.split(delim);
  String sign=".";
  for (int i=0; i< sa.length; i++)
  { try
    { if (sa[i].length() > 10)
			{ sa[i] = cfgTuner.parseString(sa[i]);
				dubna.walt.service.Service.logSQL(" #" + Integer.toString(i), sa[i], rm);
				getPreData(sa[i]);
				out.print(sign); 
				sign = (sign.equals("."))? " " : ".";
				out.flush();
			}
    }
    catch (Exception e)
    { IOUtil.writeLogLn("XXXXXXXX Exception: " + e.toString(), rm);
      cfgTuner.addParameter("ExecuteSqlScriptError", e.toString());
      cfgTuner.addParameter("ERROR", e.toString());
//      out.print(e.toString()); 
//      out.flush();  
      break;
    }
  }
  cfgTuner.outCustomSection("[report footer]",out);
}

}