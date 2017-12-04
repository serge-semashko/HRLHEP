package jinr.sed;


public class ServiceProcessList extends dubna.walt.service.Service
{
 
public void start() throws Exception
{
  cfgTuner.outCustomSection(headerSectionName,out);
	if(cfgTuner.enabledOption("indexes")) {
		String[] indexes = cfgTuner.getParameter("indexes").split(",");
		String[] params = cfgTuner.getParameter("params").split(",");
//		System.out.println("indexes.length =" + indexes.length + "; " + cfgTuner.getParameter("indexes"));
	
		for (int i=0; i<indexes.length; i++) 
		{
			for (int j=0; j<params.length; j++) 
			{
//				System.out.println(i + "(" + indexes[i] + "):" + j + ": " + params[j]+"=" + cfgTuner.getParameter(params[j] + indexes[i]));
				cfgTuner.addParameter(params[j], cfgTuner.getParameter(params[j] + indexes[i]));
			}
			cfgTuner.addParameter("index", indexes[i] );
			cfgTuner.outCustomSection( cfgTuner.getParameter("itemSection"),out);
		}
	}
  cfgTuner.outCustomSection(footerSectionName, out);
}

}

