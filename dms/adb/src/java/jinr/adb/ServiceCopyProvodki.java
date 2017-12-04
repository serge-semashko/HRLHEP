package jinr.adb;

import java.sql.ResultSet;
import dubna.walt.util.DBUtil;
import dubna.walt.util.IOUtil;
import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.SQLException;

public class ServiceCopyProvodki extends jinr.adb.ServiceCopyRecordset
{
String[][] plan = {
	{"20.","lab","sbj","sst"},
	{"23.","lab","sbj","sst"},
	{"25.","lab","sst","dev"},
	{"26.01","lab","sst",""}
};

String[] p = new String[8];
String accD = "";
String accK = "";
private int fieldNr=0;

protected void parseSubconto(boolean isKredit, String val, int nr)
{
	String acc = accD;
	int offset = 0;
	if (isKredit) 
	{ acc = accK;
	  offset = 4;
	}
	for (int na = 0; na < plan.length; na++)
	{
		try {
		if (acc.startsWith(plan[na][0]))
		{
		  if(plan[na][nr].equals("lab"))
		    if (val.length() > 2)
					p[offset] = val.substring(0,3);
		  else if(plan[na][nr].equals("sbj"))
			{
			  if (val.length() > 6)
			  { p[1+offset] = val.substring(4,7);
			    p[2+offset] = val.substring(0,4);
			  }
			}
		  else if(plan[na][nr].equals("dev"))
		  {
		    if (val.length() > 2)
		    { p[1+offset] = val.substring(0,3);
		      p[2+offset] = "Уст.";
		    }
		  }
		  else if(plan[na][nr].equals("sst"))
		  {
		    if (val.length() > 2)
		    { p[3+offset] = val.substring(0,3);
		    }
		  }
//			return;
		}
		}
		catch (Exception e)
		{
			out.println("<br>============================================<br>");
		  out.println("=== parseSubconto - EXCEPTION:" + e.toString());
		  String k =(isKredit)? "кредит": "дебет";
		  out.println("<br>=== " + k + "; счет=" + acc + "; Субконто " + nr + " (" + headers[fieldNr] + ") ='" + val + "'");
		  out.println("<br>============================================");
			e.printStackTrace(System.out);
		}
	}
	
//	if (accD.equals("25.03") && accK.equals("10.02"))
//	{
//		System.out.print( isKredit + "/" + acc + "/ " + val + " / " + nr);
//		for (int i = 0; i<8; i++)
//		{
//			System.out.print(" " + p[i]);
//		} 
//		System.out.println(" ");
//	}     

}

	protected void processRecord(ResultSet r) throws SQLException
	{
	  String val = "";
	  int year = 0;
	  int mm = 0;
		for (int i = 0; i<8; i++)
			p[i]="";


	  for (int i = 1; i <= headers.length; i++)
	  {
      stmt.setObject(i, r.getObject(i));
	    val = r.getString(i);
//	    if (val.length() > 2)
			fieldNr = i;
				switch (i) 
				{
					case 1: {
						year = r.getDate(1).getYear() + 1900;
						mm = r.getDate(1).getMonth()+1;
					  break;
					}
					case 3: accD = val; break;
					case 4: parseSubconto(false, val,1); break;
				  case 5: parseSubconto(false, val,2); break;
				  case 6: parseSubconto(false, val,3); break;
					
					case 9: accK = val; break;
				  case 10: parseSubconto(true, val,1); break;
				  case 11: parseSubconto(true, val,2); break;
				  case 12: parseSubconto(true, val,3); break;
				}
	  }

		stmt.setInt(20, year);
	  stmt.setInt(21, mm);
	  stmt.setInt(22, (mm-1)/3 + 1);
	  for (int i = 0; i<8; i++)
	    stmt.setString(23+i, p[i]);
	  stmt.executeUpdate();
		
/*		if (accD.equals("25.03") && accK.equals("10.02"))
		{
			System.out.print(" " + accD + "/" + accK);
			for (int i = 0; i<8; i++)
			{
				System.out.print(" " + p[i]);
			}	
			System.out.println(" ");
		}
*/		
//	  for (int i = 0; i<8; i++)
//	    p[i]="";

	}


}