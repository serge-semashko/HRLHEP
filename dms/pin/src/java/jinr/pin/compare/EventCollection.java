package jinr.pin.compare;

import dubna.walt.util.*;
import java.util.*;
import java.sql.*;

public class EventCollection
{
//	private DBUtil dbUtil;
	public int type_id;
	public java.util.Date lastAccess=null;
	private Hashtable ht_Events;
		
	public EventCollection(int type_id, DBUtil dbUtil)
	{ this.type_id=type_id;
		ht_Events = new Hashtable(100);
		try
		{	String sql = "select id, replace(upper(event_name),' ',''), replace(upper(authors),' ',''), replace(upper(event_host),' ','') from event_values where type_id=" + Integer.toString(type_id);
			ResultSet r = dbUtil.getResults(sql);
			while (r.next())
			{ int id = r.getInt(1);
				EventInfo e = new EventInfo(id,r.getString(2), r.getString(3),r.getString(4));
				ht_Events.put(id, e);
			}
		  lastAccess = new java.util.Date();
		}
		catch (Exception e)
		{
			e.printStackTrace(System.out);
		}
	}
	
	public EventInfo getEventInfo(int id)
	{
		return (EventInfo) ht_Events.get(id);
	}
	
	public int[] getMatchingEventIDs(String patt_name, String patt_authors, String patt_host, int maxPattLength, int minMatch, int exclude_id)
	{ lastAccess = new java.util.Date();
	  EventInfo[] es = getMatchingEvents(patt_name, patt_authors, patt_host, maxPattLength, minMatch, exclude_id);
		if (es == null) return null;
	  int[] ids = new int[es.length];
		for (int i=0; i< es.length; i++)
			ids[i] = es[i].id;
		return ids;
	}

	public EventInfo[] getMatchingEvents(String patt_name, String patt_authors, String patt_host, int maxPattLength, int minMatch, int exclude_id)
	{ lastAccess = new java.util.Date();
		Vector v = new Vector(20,20);
		Enumeration en = ht_Events.elements();
		Locale ru = new Locale("ru");
	  String name = patt_name.replaceAll(" ","").toUpperCase(ru);
	  String authors = patt_authors.replaceAll(" ","").toUpperCase(ru);
	  String host = patt_host.replaceAll(" ","").toUpperCase(ru);
//	  System.out.println(name + ":" + authors + ":" + host);
		while (en.hasMoreElements())
		{ EventInfo e = (EventInfo) en.nextElement();
		  if (e.id != exclude_id)
			{	EventInfo em = e.match(name, authors, host, maxPattLength, minMatch);
				if (em != null && em.id != exclude_id)
				{	v.add(em);
					System.out.println(em.id + ":" + em.match+ ":" + em.name);
				}
			}
		}
		if (v.size() == 0) return null;
		EventInfo[] res = new EventInfo[v.size()];
		Object[] res1 = v.toArray(res);
		System.out.println("***** Matching events:" + res.length + ":" + res);
//		System.out.println("***** Matching events1:" + res1.length + ":" + res1);
		Arrays.sort(res);
		return res;
	}
	
}
