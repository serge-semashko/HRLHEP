package cern.kpi.viewdb;

import java.sql.ResultSet;

import dubna.walt.util.StrUtil;

public class SQLExpPlanService extends dubna.walt.service.TableServiceSimple
{

protected int outTableBody(ResultSet resultSet) throws Exception
{
  int colNr;
  currentRow = -1;
  int numRows = 0;

  while (resultSet.next())
  { numRows++;
    row.setValue("");
    getRecord(resultSet);

    for (colNr = 0; colNr < numSqlColumns; colNr++)
    { cell.setAttr("");
      if (colNr==3 || colNr==1)
      {
        if (record[colNr].indexOf("UNIQUE SCAN") != -1)
          cell.setAttr("bgcolor=#54FF9F");
        else if (record[colNr].indexOf("FAST FULL SCAN") != -1)
          cell.setAttr("bgcolor=#54FF9F");
        else if (record[colNr].indexOf("FULL") != -1)
          { if (record[colNr+1].indexOf("DUAL") == -1)
              cell.setAttr("bgcolor=red");
            else
              cell.setAttr("bgcolor=#7CFC00");
          }
        else if (record[colNr].indexOf("BY INDEX ROWID") != -1)
          cell.setAttr("bgcolor=#7CFC00");
        else if (record[colNr].indexOf("BY USER ROWID") != -1)
          cell.setAttr("bgcolor=#7CFC00");
        else if (record[colNr].indexOf("RANGE SCAN") != -1)
          cell.setAttr("bgcolor=#98FB98");
      }
      
      cell.addAttr("nowrap=true");
      cell.setValue(record[colNr]);
      cell.setFormatParams(numDigits, thsnDelimiter);
      row.addValue(cell);
    }
    currentRow++;
    row.setAttr(tableTuner.getParameter("totalBgColor"));
    out.println(row.toHTML());
  }
  return numRows;

}

public void beforeStart() throws Exception
{
  try
  {   String sql = cfgTuner.getFinalParameter("SQL_TEXT");
      if (sql != null && sql.length() > 0)
      { sql = StrUtil.replaceInString(sql, "#", "##");
        cfgTuner.addParameter("SQL_TEXT", sql);
      }
    super.beforeStart();
  }
  catch (Exception e)
  { if (e.toString().indexOf("Only Keyset-driven cursors") != -1){
      cfgTuner.addParameter("ERROR", "Only Keyset-driven cursors are updateable.");
    }
    else if (e.toString().indexOf("ORA-00903") != -1){
      cfgTuner.addParameter("ERROR", "Invalid table name.");
    }
    else if (e.toString().indexOf("ORA-00904") != -1){
      cfgTuner.addParameter("ERROR", "Invalid column name.");
    }
    else if (e.toString().indexOf("ORA-01039") != -1){
      cfgTuner.addParameter("ERROR", "Insufficient privileges on underlying objects of the view.");
    }
    else if (e.toString().indexOf("ORA-01031") != -1){
      cfgTuner.addParameter("ERROR", "Insufficient privileges.");
    }
    else if (e.toString().indexOf("ORA-02019") != -1){
      cfgTuner.addParameter("ERROR", "Connection description for remote database not found.");
    }
    else {
      String s = StrUtil.replaceInString(e.toString(), "#", "##");
      int i = s.indexOf(" SQL:");
      if ( i > 0) s = s.substring(0,i-1);
      cfgTuner.addParameter("ERROR", s);
    }
    if (cfgTuner.enabledOption("debug=on"))
      e.printStackTrace(System.out);
  }
  String sql = cfgTuner.getFinalParameter("SQL_TEXT");
  if (sql != null && sql.length() > 0)
  { sql = StrUtil.replaceInString(sql, "#", "##");
//    cfgTuner.addParameter("SQL_TEXT", sql);
    cfgTuner.addParameter("SQL", this.SQLTextRendering(sql));
  }
}

protected String SQLTextRendering(String sql_t){
  String sql = new String (sql_t);
  sql = StrUtil.replaceInString(sql, "<", "&lt;");
  String space = new String("&nbsp;");
  String buf = new String();
  String substr = new String();
  int pref = 0;  // ammount of spaces
  int indx = 0;
  int fromIndx = 0;  //pointer on current <br> tag in the string
  int begIndx = 0;
  if (sql.equals(""))
    return "";
  sql = StrUtil.replaceInString(sql, "\n", " ");
  sql = StrUtil.replaceInString(sql, "\r", " ");
  sql = StrUtil.replaceIgnoreCase(sql, "select ", "<br><b>SELECT </b>");
  sql = StrUtil.replaceIgnoreCase(sql, "delete ", "<b>DELETE </b>");
  sql = StrUtil.replaceIgnoreCase(sql, "update ", "<b>UPDATE </b>");
  sql = StrUtil.replaceIgnoreCase(sql, "insert ", "<b>INSERT </b>");
  sql = StrUtil.replaceIgnoreCase(sql, " from ", "<br><b>FROM </b>");
  sql = StrUtil.replaceIgnoreCase(sql, " where ", "<br><b>WHERE </b>");
  sql = StrUtil.replaceIgnoreCase(sql, " and ", "<br>&nbsp;<b> AND </b>");
  sql = StrUtil.replaceIgnoreCase(sql, " or ", "<br>&nbsp;<b> OR </b>");
  sql = StrUtil.replaceIgnoreCase(sql, " order by ", "<br><b>ORDER BY </b>");
  sql = StrUtil.replaceIgnoreCase(sql, " group by ", "<br><b>GROUP BY </b>");
  sql = StrUtil.replaceIgnoreCase(sql, " having ", "<br><b>HAVING </b>");
  sql = StrUtil.replaceIgnoreCase(sql, " union ", "<br><b>UNION </b>");
  sql = StrUtil.replaceIgnoreCase(sql, " set ", "<b> SET </b>");
  sql = StrUtil.replaceInString(sql, ",", ", ");
  if (sql.indexOf("<br>") == 0)
    sql = sql.substring(4);
  while ((indx = sql.indexOf("<br>", fromIndx)) != -1){
    fromIndx = indx + 4;
    substr = sql.substring(begIndx, indx);
    buf += sql.substring(begIndx, fromIndx);
    begIndx = fromIndx;
    for(int i=0; i < substr.length(); i++){
      if (substr.charAt(i) == '(')
        pref++;
      if (substr.charAt(i) == ')')
        pref--;
    }
    for(int i=0; i < pref*5; i++){
      buf += space;
    }
  }
  buf += sql.substring(fromIndx);
  return buf;
}
}