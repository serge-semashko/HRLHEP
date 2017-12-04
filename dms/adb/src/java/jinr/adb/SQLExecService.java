package jinr.adb;

import java.sql.*;

public class SQLExecService extends dubna.walt.service.TableServiceSimple
{

public void start() throws Exception
{ makeTableTuner();
  initFormatParams();
  makeTotalsForCols = ",,";
  makeSubtotals = false;
  
  cfgTuner.outCustomSection(headerSectionName,out);
  String sqlScript = cfgTuner.getParameter("SQL_TEXT");

  int i = 0;
  int n = 1;
  while ((i = sqlScript.indexOf(";\r")) > 0 || sqlScript.length() > 0)
  { if (i > 0)
    { sqlSectionName = sqlScript.substring(0,i).trim();
      sqlScript = sqlScript.substring(i+1).trim();
    }
    else
    { sqlSectionName = sqlScript;
      sqlScript = "";
    }
    cfgTuner.addParameter("SQL_TEXT", Integer.toString(n++) + ": " + sqlSectionName);
    /* actually make the table */
    cfgTuner.outCustomSection("begin_results",out);
    initTableTagsObjects();
    try
    { makeTable();
    }
    catch (Exception e)
    { out.println("<p><xmp>");
      e.printStackTrace(out);
      out.println("</xmp><p>");
      sqlScript = "";
    }
    cfgTuner.outCustomSection("end_results",out);
  }
  
  /* Output the report footer */
  cfgTuner.outCustomSection(footerSectionName, out);
}


protected ResultSet runSQL(String sqlSectionName) throws Exception
{ 
  ResultSet resultSet = null;
//  Terminator qk = new QueryKiller(rm, dbUtil, this);
//  Thread.sleep(1000);
  try
  { if (sqlSectionName.toUpperCase().indexOf("SELECT ") == 0)
    { resultSet = dbUtil.getResults(sqlSectionName);
      try
       { resultSet.getMetaData().getColumnCount();
       }
       catch (Exception ex)
       { cfgTuner.addParameter("no_results", "y");
       }
      }
    else
      dbUtil.update(sqlSectionName);

     cfgTuner.addParameter("timer", dbUtil.timeSpent+" sec.");
     cfgTuner.outCustomSection("finished", out);
//      dbUtil.cancelAllStatements();
  }
  catch (Exception e)
  { cfgTuner.addParameter("timer", dbUtil.timeSpent+" sec.");
    e.printStackTrace(System.out);
    String s = e.toString();
    int i = s.indexOf("SQL: ");
    if (i > 0) s = s.substring(0, i-1);
    i = s.indexOf(" ORA-");
    if (i > 0) s = s.substring(i+1);
    cfgTuner.addParameter("ERROR", s);
    cfgTuner.outCustomSection("err msg", out);
    Exception ex = new Exception (s);
//    ex.setStackTrace(e.getStackTrace());
    throw ex;
  }
  finally
  { 
//  qk.finished=true;
//    qk.interrupt();
//    qk=null;
  }
  return resultSet;
}

}