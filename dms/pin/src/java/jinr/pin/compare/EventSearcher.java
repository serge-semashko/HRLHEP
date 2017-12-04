package jinr.pin.compare;

import dubna.walt.util.ResourceManager;

import java.io.*;
import dubna.walt.util.*;
import java.util.Hashtable;

public class EventSearcher
{
	private Hashtable ht_Collections;
	
	public EventSearcher()
	{
	  ht_Collections = new Hashtable(10);
	}

	public EventInfo[] getMatchingEvents(int type_id, String patt_name, String patt_authors, String patt_host
			, int maxPattLength, int minMatch, int exclude_id, DBUtil dbUtil)
	{
	  EventCollection ec = (EventCollection) ht_Collections.get(type_id);
		if (ec == null)
			ec = refreshEvents(type_id, dbUtil);
		return ec.getMatchingEvents(patt_name, patt_authors, patt_host, maxPattLength, minMatch, exclude_id);
	}
	
	public int[] getMatchingEventIDs(int type_id, String patt_name, String patt_authors, String patt_host
			, int maxPattLength, int minMatch, int exclude_id, DBUtil dbUtil)
	{
//	System.out.println("--- search for: " + patt_name + ";" + patt_authors + ";" + patt_host);
		EventCollection ec = (EventCollection) ht_Collections.get(type_id);
		if (ec == null)
			ec = refreshEvents(type_id, dbUtil);
		return ec.getMatchingEventIDs(patt_name, patt_authors, patt_host, maxPattLength, minMatch, exclude_id);
	}
	
	public EventCollection refreshEvents(int type_id, DBUtil dbUtil)
	{ EventCollection ec = new EventCollection(type_id, dbUtil);
		ht_Collections.put(type_id, ec);	
		return ec;
	}

}
