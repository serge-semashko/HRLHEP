package jinr.adb;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.regex.* ;

public class ServiceCopyMNTS extends ServiceCopyRecordset
{
	protected int adb2ID=0;
	protected Pattern p = Pattern.compile("¹\\d{5,7}");
	protected Matcher m = null;

protected void processRecord(ResultSet r) throws SQLException
	{
		adb2ID=0;
		for (int i = 1; i < headers.length; i++)
		{
			Object o = r.getObject(i);
			if (o != null && o.getClass().equals(String.class))
			{
			  stmt.setString(i, o.toString().trim());
//				System.out.print(" '" + o.toString().trim() + "' ");
			}
			else
				stmt.setObject(i, o);
			if (dFields[i-1].equalsIgnoreCase("AccK2"))
			{ String val = (String) r.getObject(i);
				if (val != null && val.length() > 5)
				{
					m = p.matcher(val);
					if (m.find())
						try{
							adb2ID = Integer.parseInt(val.substring(m.start()+1, m.end()));
						}
						catch (Exception e) {;}
				}
			}
		}
		stmt.setInt(headers.length, adb2ID);
		stmt.executeUpdate();

}

}