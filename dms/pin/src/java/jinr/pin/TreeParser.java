package jinr.pin;

import dubna.walt.util.IOUtil;
import java.sql.ResultSet;
import java.util.Vector;

import org.json.JSONArray;
import org.json.JSONObject;

public class TreeParser extends dubna.walt.service.Service
{
	public void start() throws Exception
	{
		long timer = System.currentTimeMillis();
		ResultSet rs = dbUtil.getResults(getSQL("treeSQL"));
		long t1 = (System.currentTimeMillis() - timer) / 10;
		double t = t1 / 100.;
		IOUtil.writeLogLn("------ done in " + t + " sec.", rm);
		
		String frame = "detail";
		
		Vector names = new Vector();
		while (rs.next())
		{
			JSONObject jnode = new JSONObject();
			jnode.put("text", rs.getString("name"));
			jnode.put("qtip", rs.getString("description"));
			String id = rs.getString("Id");
			jnode.put("id", id);
			jnode.put("href", "javascript:openFolder('" + id + "');");
			if (rs.getInt("isParent") == 0)
				jnode.put("leaf", "true");
//			IOUtil.writeLogLn("" + jnode.toString(), rm);
			names.add(jnode);      	  
		}
		dbUtil.closeResultSet(rs);

		JSONArray jsonItems = null;
		jsonItems = new JSONArray(names);  
		out.print(jsonItems.toString());
		out.flush();
		IOUtil.writeLogLn("TREE:" + jsonItems.toString(), rm);
		t1 = (System.currentTimeMillis() - timer) / 10;
		t = t1 / 100.;
		IOUtil.writeLogLn("------ done in " + t + " sec.", rm);
	}

}
