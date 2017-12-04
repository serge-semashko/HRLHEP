package cern.kpi.viewdb;

import dubna.walt.util.StrUtil;

public class SQLStructuringService extends dubna.walt.service.Service
{


public void beforeStart() throws Exception
{
  try
  {
    super.beforeStart();
    String sql = cfgTuner.getFinalParameter("SQL_TEXT");
    cfgTuner.addParameter("SQL_TEXT", "==" + sql);
    if (sql != null && sql.length() > 0)
    { 
      cfgTuner.addParameter("SQL", SQLStructuringService.SQLTextRendering(StrUtil.replaceInString(sql, "#", "##")));
//      cfgTuner.addParameter("SQL", sql);
    }

  }catch (Exception e)
  {
    if (e.toString().indexOf("Only Keyset-driven cursors") != -1){
      cfgTuner.addParameter("ERROR", "Only Keyset-driven cursors are updateable.");
    }
    else if (e.toString().indexOf("ORA-00903") != -1){
      cfgTuner.addParameter("ERROR", "Invalid table name.");
    }
    else if (e.toString().indexOf("ORA-01039") != -1){
      cfgTuner.addParameter("ERROR", "Insufficient privileges on underlying objects of the view.");
    }
    else if (e.toString().indexOf("ORA-01031") != -1){
      cfgTuner.addParameter("ERROR", "Insufficient privileges.");
    }
    else {
      cfgTuner.addParameter("ERROR", e.toString());
    }
//    System.out.println("!!!!!!!" + e.toString());
  }
}

public static String SQLTextRendering(String sql_t)
{
  String sql = new String (sql_t);
  String space = new String("&nbsp;");
  String buf = new String();
  String substr = new String();
  int pref = 0;  // ammount of spaces
  int indx = 0;
  int fromIndx = 0;  //pointer on current <br> tag in the string
  int begIndx = 0;
  if (sql.length() < 3)
    return sql;
  
  sql = StrUtil.replaceInString(sql, "<", "&lt;");
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