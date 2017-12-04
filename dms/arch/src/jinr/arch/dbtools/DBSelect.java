package jinr.arch.dbtools;
import java.sql.*;
import java.util.*;
import oracle.sql.*;
/**
 * Класс содержит статические методы для выборки данных из базы данных
 */
public class DBSelect 
{

  public static HashMap getRow(String sSQL, List argsList, Connection conn) throws SQLException
  {
          List<HashMap> resList = getTopRows(sSQL, 1, argsList, conn);
          if (resList.size() == 0) return new HashMap();
          else return resList.get(0);
  }
  
  public static Object getScalar(String sSQL, List argsList, Connection conn) throws SQLException
  {
    HashMap row = getRow(sSQL, argsList, conn);
          if (row.size() == 0)
                  return null;
          else
          {
            return row.values().iterator().next();
          }
  }
  
  public static int getCount(String sSQL, String cntField, List argsList, Connection conn) throws Exception
  {
    List<HashMap> resList = getRows(sSQL, argsList, conn);
    if (resList.size() == 0) return 0;
    if (resList.size() > 1) throw new Exception("Multiple count result");
    return (Integer) resList.get(0).get(cntField);
  }
  
  
  public static Object get1Object(String sSQL, String field, List argsList, Connection conn) throws Exception
  {
    List<HashMap> resList = getRows(sSQL, argsList, conn);
    if (resList.size() == 0) return null;
    return resList.get(0).get(field);
  }

  public static List<HashMap> getRows(String sSQL, List argsList, Connection conn) throws SQLException
  {
    return getRows(sSQL, argsList,  conn,false);
  }
  
  /** 
   * Возвращает первые rowCount строк !!!!
   * Метод использует Connection c базой данных, создает PreparedStatement, инициализирует
   * значения параметров, приведенных в списке argsList, возвращает список (интерфейс List)
   * объектов типа HashMap в которых содержатся данные строки, причем имя столбца используется
   * в качестве ключа, а зачения (объекты типа String, Integer, Float, Date) являются значениями
   * хэш таблицы.
   * @throws java.sql.SQLException
   * @return List объектов типа HashMap
   * @param conn  - соединение с БД
   * @param argsList - лист аргументов
   * @param sSQL - SQL команда выборки данных причем число ? в предикате должно совпадать
   * @param rowCount - число строк
   * с числом элементов в argsList
   */
   public static List<HashMap> getTopRows(String sSQL, int rowCount, List argsList, Connection conn, boolean forHtml) throws SQLException
   {
      if (rowCount < 0)
                         return getRows(sSQL, argsList,  conn, forHtml);

     int venderId = DBTools.getDBVender(conn);
     if (venderId == 1)
     {
       sSQL = "SELECT TOP " + rowCount + " " + sSQL.trim().substring("SELECT".length());
     }
     else if (venderId == 2)
     {
       sSQL = "SELECT rownum, inSQL.* FROM \n" +
       "(\n" +
       sSQL +
       ") inSQL \n" +
       "WHERE rownum <= " + rowCount;
     }
     return getRows(sSQL, argsList,  conn, forHtml);
   }
  
  
  public static List<HashMap> getTopRows(String sSQL, int rowCount, List argsList, Connection conn) throws SQLException
  {
    return getTopRows(sSQL, rowCount,argsList, conn, false);
  }
  /** 
   * Метод использует Connection c базой данных, создает PreparedStatement, инициализирует
   * значения параметров, приведенных в списке argsList, возвращает список (интерфейс List)
   * объектов типа HashMap в которых содержатся данные строки, причем имя столбца используется
   * в качестве ключа, а зачения (объекты типа String, Integer, Float, Date) являются значениями
   * хэш таблицы.
   * @throws java.sql.SQLException
   * @return List объектов типа HashMap
   * @param conn  - соединение с БД
   * @param argsList - лист аргументов
   * @param sSQL - SQL команда выборки данных причем число ? в предикате должно совпадать
   * с числом элементов в argsList
   */
   
   /** 
    * Возвращает первые rowCount строк !!!!
    * Метод использует Connection c базой данных, создает PreparedStatement, инициализирует
    * значения параметров, приведенных в списке argsList, возвращает список (интерфейс List)
    * объектов типа HashMap в которых содержатся данные строки, причем имя столбца используется
    * в качестве ключа, а зачения (объекты типа String, Integer, Float, Date) являются значениями
    * хэш таблицы.
    * @throws java.sql.SQLException
    * @return List объектов типа HashMap
    * @param conn  - соединение с БД
    * @param argsList - лист аргументов
    * @param sSQL - SQL команда выборки данных причем число ? в предикате должно совпадать
    * @param rowCount - число строк
    * с числом элементов в argsList
    */
   public static List<HashMap> getRowsForPage(String sSQL, int startIndex, int endIndex, List argsList, Connection conn) throws SQLException
   {
           if ((startIndex < 0) || (endIndex <= 0))
                   return getRows(sSQL, argsList,  conn);

           int venderId = DBTools.getDBVender(conn);
           if (venderId == 1)
           {
           }
           else if (venderId == 2)
           {
             sSQL = "SELECT * FROM \n" +
             "(\n" +
             "  SELECT rownum NNN, inSQL.* FROM \n" +
             "  (\n" +
                  sSQL +
             "  ) inSQL \n" +
             ")\n" +
             "WHERE NNN >= " + startIndex + " AND NNN <= " + endIndex;
           }
           return getRows(sSQL, argsList, conn);
   }

  public static List<HashMap> getRows(String sSQL, List argsList, Connection conn, boolean forHtml) throws SQLException
  {
    PreparedStatement ps = null;
    ResultSet rs = null;
    ArrayList res = new ArrayList();
    try
    {
      ps = conn.prepareStatement(sSQL);
      if (argsList != null)
      {
          for (int i = 0; i < argsList.size(); i++) ps.setObject(i + 1, argsList.get(i));
      }
      rs = ps.executeQuery();
      
      while (rs.next()) res.add(getResaltSetDataMap(rs, forHtml));
      
      rs.close(); rs = null;
      ps.close(); ps = null;
    }
    catch (SQLException e)
    {
      try 
      {
        if (rs != null) rs.close();
        if (ps != null) ps.close();
      }catch (SQLException ee){}
       throw e;
    }
    return res;
  }
   public static void executeStatement(String sSQL, List argsList, Connection conn) throws SQLException
   {
    PreparedStatement ps = null;
    try
    {
      ps = conn.prepareStatement(sSQL);
      if (argsList != null)
      {
          for (int i = 0; i < argsList.size(); i++) ps.setObject(i + 1, argsList.get(i));
      }
      ps.execute();
      
      ps.close(); ps = null;
    }
    catch (SQLException e)
    {
      try 
      {
        if (ps != null) ps.close();
      }catch (SQLException ee){}
       throw e;
    }
  }
  
  /**
   * Метод преобразовывает данные строки ResultSet в HashMap, где ключи имена
   * столбцов, а значения - значения столбцов в формате объектов 
   * @throws java.sql.SQLException
   * @return HashMap
   * @param rs - ResultSet
   */
  public static HashMap getResaltSetDataMap(ResultSet rs, boolean replaceHtmlCtrSymbols) throws SQLException
  {
    HashMap dataMap = new HashMap();
    ResultSetMetaData rsmd = rs.getMetaData();
    int colNum = rsmd.getColumnCount();
    for (int i = 1; i <= colNum; i++)
    {
      String colName = rsmd.getColumnName(i);
      if (dataMap.containsKey(colName)) continue;
       
      switch (rsmd.getColumnType(i))
      {
          case Types.VARCHAR:
            String sData = rs.getString(colName);
            if (replaceHtmlCtrSymbols && sData != null) dataMap.put(colName,prepareTextForHtml(sData));
            else  dataMap.put(colName,sData);
          break;
          case Types.CLOB:
            dataMap.put(colName, DBTools.readClob( (CLOB) rs.getClob(colName)));
          break;
          case Types.BLOB:
            dataMap.put(colName, DBTools.readBlob( (BLOB) rs.getBlob(colName)));
          break;
          case Types.DATE:
          int aaa = 1;
          /*
          java.sql.Date sqlDate = rs.getDate(colName);
          if (sqlDate != null) dataMap.put(colName,new java.util.Date(sqlDate.getTime()));
          else dataMap.put(colName,null);
          break;
          */
          
          case Types.TIMESTAMP:
          Timestamp sqlTimestamp = rs.getTimestamp(colName);
          if (sqlTimestamp != null) dataMap.put(colName,new java.util.Date(sqlTimestamp.getTime()));
          else  dataMap.put(colName,null);
          break;
          case Types.BIGINT:
            long lVal = rs.getLong(colName);
            dataMap.put(colName, new Long(lVal));
          break;
          case Types.NUMERIC:
          case Types.SMALLINT:
          case Types.DECIMAL:
            int scale = rsmd.getScale(i);
            if (scale > 0)
            {
              dataMap.put(colName, rs.getDouble(colName));
            }
            else if(rsmd.getPrecision(i) <= 10)
            {
              int iVal = rs.getInt(colName);
              dataMap.put(colName, new Integer(iVal));
            }
            else if(rsmd.getPrecision(i) > 10)
            {
              lVal = rs.getLong(colName);
              dataMap.put(colName, new Long(lVal));
            }
            
          break; 
          
          default:
          dataMap.put(colName,rs.getObject(colName));
      }
    }
    return dataMap;
  }
  
  public static String prepareTextForHtml(String input)
  {
    if (input == null) return null;
    String replTable[][] = {
                            {"\"", "&quot"} 
                          };
                          
    for (int i = 0; i <  replTable.length; i++)
    {
      String repLine[] = replTable[i];
      input = input.replaceAll(repLine[0], repLine[1]);
    }
    return input;
  }
  
  /**
   * Метод возвращает SQL предложение формирующих представление (View) для информационных
   * объектов типа определяемого значением objectTypeId
   * @return 
   * @param objectTypeId
   */
  public static String getObjectView(int objectTypeId)
  {
    return null;
  }
  
  public static HashMap getUserColumnComments(String tableName, Connection conn) throws SQLException
  {
    String sSQL = 
    "SELECT COL.COLUMN_NAME, COL.COMMENTS\n" + 
    "FROM USER_COL_COMMENTS COL\n" + 
    "WHERE COL.TABLE_NAME = ' " + tableName + "'";
    List<HashMap> resList = getRows(sSQL,null,conn);
    HashMap userColumnCommentsMap = new HashMap();
    if(resList != null && resList.size() > 0)
    {
      for(HashMap hm:resList)
      {
        String colName = (String)hm.get("COLUMN_NAME");
        String colComment = (String)hm.get("COMMENTS");
        userColumnCommentsMap.put(colName,colComment);
      }
    }
    return userColumnCommentsMap;
  }
}