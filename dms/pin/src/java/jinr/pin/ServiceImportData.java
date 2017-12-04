package jinr.pin;

import java.sql.ResultSet;import dubna.walt.util.DBUtil;
import dubna.walt.util.IOUtil;
import java.sql.Connection;
import java.sql.ResultSet;

public class ServiceImportData extends dubna.walt.service.Service
{

public void start() throws Exception
{
  cfgTuner.outCustomSection("report header",out);
	
  try
    { doCopy(getSQL("SQL"));
    }
    catch (Exception e)
    { IOUtil.writeLogLn("XXXXXXXX Exception: " + e.toString(), rm);
      cfgTuner.addParameter("ImportDataError", e.toString());
      cfgTuner.addParameter("ERROR", e.toString());
//      out.print(e.toString()); 
//      out.flush();  
    }
  cfgTuner.outCustomSection("[report footer]",out);
}

	public void doCopy(String sql) throws Exception
	{ if (sql == null || cfgTuner.enabledOption("NotConnected"))
			return;
	  DBUtil srcDBUtil = dbUtil;
	  System.out.println("doCopy: dbUtil=" + srcDBUtil); 
	  System.out.println("+++ go +++"); 
		String val = "";
	  int numBatch=cfgTuner.getIntParameter("numBatch");
	  if (numBatch <= 0)numBatch=100;

		if (sql.toUpperCase().indexOf("SELECT_SP") == 0)
			sql = sql.substring(9);
		ResultSet r = srcDBUtil.getResults(sql);
	  cfgTuner.outCustomSection("start loop", out);
	  Connection conn = dbUtil.getConnection();
	  conn.setAutoCommit(false);
		if (r != null)
		{ String[] headers = DBUtil.getColNames(r);
			int nr = 0;
//			while (r.next() && !cfgTuner.enabledOption("ERROR"))
		  while (r.next())
			{ for (int i = 0; i < headers.length; i++)
				{ val = r.getString(i+1);
				  if (val != null && val.length() > 0) val = val.trim();
					if (val != null && val.length() > 0 && !val.equalsIgnoreCase("NULL"))
					  cfgTuner.addParameter(headers[i], val);
					else
					  cfgTuner.addParameter(headers[i], "");
				}
				nr++;
			  cfgTuner.addParameter("REC_NR", Integer.toString(nr));
			  cfgTuner.addParameter("REC_10", Integer.toString(nr % 10));
			  cfgTuner.addParameter("REC_100", Integer.toString(nr % numBatch));
			  cfgTuner.outCustomSection("record", out);
				
			  if ((nr % numBatch) == 1) 
			    System.out.print("\n\r " + nr);
			  if ((nr % 2) == 1) 
				  System.out.print(".");
//				System.out.print(" " + nr % numBatch);
			  if (nr  % 1000 == 0) 
			  {
					dbUtil.commit(); dbUtil.close();
					System.out.println(" "); System.out.println(nr + ": SLEEP 10 sec." );
					out.flush();
					System.gc();
					Thread.sleep(1000);
					dbUtil.connect();
			  }
			}
			srcDBUtil.closeResultSet(r);
		  srcDBUtil.close();
		  dbUtil.commit();
		}
	}

}