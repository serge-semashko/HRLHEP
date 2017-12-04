package jinr.adb;

import java.sql.ResultSet;
import java.sql.SQLException;

public class ServiceCopyPlat extends ServiceCopyRecordset
{

	protected void processRecord(ResultSet r) throws SQLException
	{
	  for (int i = 1; i <= headers.length; i++)
	  {
	    if (i==5)
	    {
	      try {
	        Integer.parseInt(r.getString(i));
	        stmt.setObject(i, r.getObject(i));
	      }
	      catch (NumberFormatException eex)
	      {
	  //              System.out.println(nr + "/" + i + ": " + r.getString(i));
	        stmt.setObject(i, null);
	      }
	    }
	    else
	      stmt.setObject(i, r.getObject(i));
	  }
	  stmt.executeUpdate();
	}

}