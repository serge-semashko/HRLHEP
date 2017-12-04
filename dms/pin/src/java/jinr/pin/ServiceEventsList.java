package jinr.pin;

import dubna.walt.util.DBUtil;
import java.sql.*;
 import java.util.*;
//import dubna.walt.util.StrUtil;

public class ServiceEventsList extends dubna.walt.service.Service
{

	public void start() throws Exception
	{ 
		String ids = cfgTuner.getParameter("EVENT_IDS");
	  if (ids.length() < 2) return;
		
	  cfgTuner.outCustomSection("report header", out);
		
	  StringTokenizer st = new  StringTokenizer(cfgTuner.getParameter("FIELDS_LIST"), "/");
		int n=1;
		String field_names = "";
		while (st.hasMoreTokens())
		{ String field = st.nextToken();
		  field_names += ", " + field + " as \"FIELD_"+ Integer.toString(n++) + "\"";
//			field_names += ",replace(" + field + ",'<','&lt;') as \"FIELD_"+ Integer.toString(n++) + "\"";
		}
	  cfgTuner.addParameter("field_names" , field_names);
		
	  int i=0;
		st = new  StringTokenizer(ids, ",");
	  while (st.hasMoreTokens())
		{ cfgTuner.addParameter("event_id", st.nextToken());
		  cfgTuner.addParameter("event_num", Integer.toString(++i));
			cfgTuner.outCustomSection("item", out);
		}
	  cfgTuner.outCustomSection("report footer", out);

//	  super.start();
	}
}
