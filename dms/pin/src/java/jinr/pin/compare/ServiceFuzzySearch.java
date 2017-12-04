package jinr.pin.compare;

import dubna.walt.util.DBUtil;
import java.sql.*;
 import java.util.*;
//import dubna.walt.util.StrUtil;

public class ServiceFuzzySearch extends dubna.walt.service.Service
{

	public void start() throws Exception
	{ 
	  int type_id = cfgTuner.getIntParameter("TYPE_ID");
	  int maxPattLength = cfgTuner.getIntParameter("maxPattLength");

		EventSearcher es = (EventSearcher) rm.getObject("EventSearcher", false);
		if (es == null)
		{	es = new EventSearcher();
			rm.setObject("EventSearcher", es, true);
		}
		else
		{	if (cfgTuner.enabledOption("RefreshEventsList"))
				es.refreshEvents(type_id, dbUtil);		
		}
	  if (cfgTuner.enabledOption("RefreshEventsList")) return;
		
/*	  String patt_name = cfgTuner.getParameter("event_name");
	  String patt_authors = cfgTuner.getParameter("authors");
	  String patt_host = cfgTuner.getParameter("event_host");
	  Locale ru = new Locale("ru");
	  String name = patt_name.replaceAll(" ","").toUpperCase(ru);
	  String authors = patt_authors.replaceAll(" ","").toUpperCase(ru);
	  String host = patt_host.replaceAll(" ","").toUpperCase(ru);
	  cfgTuner.addParameter("patt_name", name);
	  cfgTuner.addParameter("patt_authors", authors);
	  cfgTuner.addParameter("patt_host", host);
*/
		int[] ids = es.getMatchingEventIDs(	type_id
				,	cfgTuner.getParameter("event_name")
				,	cfgTuner.getParameter("authors")
				,	cfgTuner.getParameter("event_host")
				, maxPattLength
				, cfgTuner.getIntParameter("minMatch")
				, cfgTuner.getIntParameter("event_id")
				, dbUtil);
	  if (ids== null) return;
		
	  cfgTuner.addParameter("numEvents", Integer.toString(ids.length));
	  cfgTuner.outCustomSection("report header", out);
		
	  StringTokenizer st = new  StringTokenizer(cfgTuner.getParameter("FIELDS_LIST"), ",");
		int n=1;
		String field_names = "";
		while (st.hasMoreTokens())
		{ String field = st.nextToken();
			field_names += "," + field + " as \"FIELD_"+ Integer.toString(n++) + "\"";
		}
	  cfgTuner.addParameter("field_names" , field_names);
		
		for (int i=0; i<ids.length; i++)
		{ cfgTuner.addParameter("event_id", Integer.toString(ids[i]));
		  cfgTuner.addParameter("event_num", Integer.toString(i+1));
			cfgTuner.outCustomSection("item", out);
		}
	  cfgTuner.outCustomSection("report footer", out);

//	  super.start();
	}
}
