package jinr.pin.compare;

import dubna.walt.util.*;


public class EventInfo implements Comparable
{
	public int id;
	public String name;
	public String authors;
	public String host;
	public int match;
	
	
	public EventInfo(int id, String name, String authors, String host)
	{ this.id=id;
		this.name=name;
		this.authors=authors;
		this.host=host;		
	  match = 0;
	}


public EventInfo match(String patt_name, String patt_authors, String patt_host, int maxPattLength, int minMatch)
{
	int n = 0;
	int numTests = 0;
	if (patt_name.length() > 3)
	{	n += 5*StrUtil.fuzzyMatch(patt_name, this.name, maxPattLength);
	  numTests +=5;
	}

	if (patt_authors.length() > 3 && patt_authors.length() <50)
	{ n += StrUtil.fuzzyMatch(patt_authors, this.authors, maxPattLength);
		numTests++;
	}
	if (patt_host.length() > 3)
	{ n += 2*StrUtil.fuzzyMatch(patt_host, this.host, maxPattLength);
		numTests +=2;
	}
	
	if (numTests == 0) return null;
	n = n / numTests;
//	if (n > 1) System.out.println(n + ":" + this.name + ":" + this.authors + ":" + this.host);
	if (n > minMatch)
	{ EventInfo e = new EventInfo(this.id, this.name, this.authors, this.host);
		e.match = n;
		return e;
	}
	return null;
}

	public int compareTo ( Object o )
	{ EventInfo e = (EventInfo) o;
	  if (e.match == this.match) return 0;
		if (e.match > this.match) return 1;
		return -1;
	}

}
