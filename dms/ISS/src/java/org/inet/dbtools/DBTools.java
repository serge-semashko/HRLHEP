package org.inet.dbtools;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import java.sql.Blob;
import java.sql.Clob;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.sql.Types;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;

import javax.sql.DataSource;


/**
 * Класс предназначен для выполнения элементарных операций со строками БД:
 * insertRow - операция добавления строки
 * updareRow - операция обновления строи
 * deleteRow - операция удаления строки
 * selectRow - операция выборки отдельной строки
 * 
 * общий принцип таков: данные строки хранятся в HashMap в виде пар 
 * имя колонки таблицы (String) и значения (тип производный от java.lang.Object)
 * Неявно используется таблица соответствия между java типами и SQL типами,
 * при необходимости выполняется преобразование типов
 * 
 * настоящая реализация работает с MS SQL
 * Для ORACLE требуется дописать ряд методов
 * 
 * Многое не оптимально - надо улучшать
 */
public class DBTools 
{
  static HashMap openConnectionsMap = new HashMap();
/**
   * Метод возвращает массив имен столбцов составляющих Первичный ключ таблицы
   * @throws java.sql.SQLException
   * @return String[]
   * @param conn - SQL соединение
   * @param tableName - имя таблицы
   */
  static public String[] getPKFieldNameArray(String tableName, Connection conn) throws SQLException
  {
    ResultSet rs = null;
    ArrayList arr = new ArrayList();
    rs = conn.getMetaData().getPrimaryKeys(null,null,tableName);
    while (rs.next())
    {
      arr.add(rs.getString("COLUMN_NAME"));
    }
    try {if (rs != null) rs.close();} catch (SQLException e){throw e;}
    String res[] = new String[arr.size()];
    for (int i = 0; i < arr.size(); i++) res[i] = (String) arr.get(i);
    return res;
  }
  /**
   * Метод возвращает массив ВСЕХ имен столбцов втаблице
   * @throws java.sql.SQLException
   * @return String[]
   * @param conn - SQL соединение
   * @param tableName
   */
  static public String[] getFieldNameArray(String tableName, Connection conn) throws SQLException
  {
    ResultSet rs = null;
    ArrayList arr = new ArrayList();
    rs = conn.getMetaData().getColumns(null, null, tableName, null);
    while (rs.next())
    {
      arr.add(rs.getString("COLUMN_NAME"));
    }
    try {if (rs != null) rs.close();} catch (SQLException e){throw e;}
    String res[] = new String[arr.size()];
    for (int i = 0; i < arr.size(); i++) res[i] = (String) arr.get(i);
    return res;
  }
  /**
   * Метод возвращает Map содержащий данные в виде экземпляра класса ColumnData для
   * каждого столбца таблицы, ключами являются имена столбцов
   * @throws java.sql.SQLException
   * @return Map
   * @param conn - SQL соединение 
   * @param tableName - имя таблицы
   */
  static public java.util.Map getColumnDataMap(String tableName, Connection conn) throws SQLException
  {
    Hashtable ht = new Hashtable();
    ResultSet rs = null;
    ArrayList arr = new ArrayList();
    rs = conn.getMetaData().getColumns(null, null, tableName, null);
    while (rs.next())
    {
      ColumnData colData = new ColumnData(rs);
      ht.put(colData.columnName,colData);
    }
    try {if (rs != null) rs.close();} catch (SQLException e){throw e;}
    return ht;
  }
   /**
   * Метод возвращает массив ВСЕХ имен столбцов втаблице
   * @throws java.sql.SQLException
   * @return String[]
   * @param columnMap (java.util.Map)
   */
  static public String[] getFieldNameArray(java.util.Map columnMap)
  {
    Set keySet = columnMap.keySet();
    String  res[] = new String[keySet.size()];
    
    for (Iterator it = keySet.iterator(); it.hasNext();) 
    {
      String fieldName = (String) it.next();
      int columnPos = ((ColumnData) columnMap.get(fieldName)).ordinalPosition;
      res[columnPos - 1] = fieldName;
    }
    return res;
   
  }
  
    public static int executeStatement(String sSQL, Connection conn) throws SQLException{
        int res = 0;
        boolean doCommit = false;
        boolean connCommitState = conn.getAutoCommit();
        if (conn.getAutoCommit()) 
        {
          conn.setAutoCommit(false);
          doCommit = true;
        }
        PreparedStatement ps = conn.prepareStatement(sSQL);
        ResultSet rs = null;
        try{
            res = ps.executeUpdate();
        }catch (SQLException e) {
            if (doCommit) 
            {
              conn.rollback();
              conn.setAutoCommit(false);
            }
            if (rs != null) rs.close();
            if (ps != null) ps.close();
            throw e;
        }
        
        
        if(doCommit)
        {
          conn.commit();
          conn.setAutoCommit(connCommitState);
        }
        return res;

    }
  
  
  /**
   * Метод добавляет строку в таблицу.
   * Значения солбцов помещаются в HashMap, имена столбцов которых не обнаружены в
   * таблице игнорируются,что позволяет использовать один HashMap для добавления строк
   * в несколько таблиц. Анализируются PK, если PK имеет опцию identity то он не включается в 
   * insert, но после insert выполняется SELECT @@IDENTITY и значение PK вовращаеся процедурой
   * @throws java.sql.SQLException
   * @return int - значение PK, если он определился в методе (используя identity или sequence), 
   * впротивном случае 0 (использование ORACLE sequence дописать)
   * @param conn - SQL соединение
   * @param paramsMap - Map значений столбцов
   * @param tableName - имя таблицы
   */
  public static int insertRow(String tableName, HashMap paramsMap, Connection conn) throws SQLException
  {
    boolean doCommit = false;
    boolean connCommitState = conn.getAutoCommit();
    if (conn.getAutoCommit()) 
    {
      conn.setAutoCommit(false);
      doCommit = true;
    }
    String sSQL = "INSERT INTO " + tableName + " (";
    String sVals = " VALUES (";
    ArrayList paramValsArr = new ArrayList();
    ArrayList SQLTypesArr = new ArrayList();
    
    String columnNameArr[] = getFieldNameArray(tableName, conn);
    String PKFieldNameArr[] = getPKFieldNameArray(tableName, conn);
    Map columnDataMap = getColumnDataMap(tableName, conn);
    int venderId = getDBVender(conn);
    String PKFieldName = "";
    if (PKFieldNameArr.length == 1) PKFieldName = PKFieldNameArr[0];
    
    if (columnNameArr.length == 0) throw new SQLException("No columns");
    
    boolean useIdentity = false;
   // boolean useSequence = false;
    boolean useOraLOB = false;
    int newId = 0;
    
    if ((PKFieldNameArr.length == 1)
     && (((ColumnData) columnDataMap.get(PKFieldNameArr[0])).typeName.indexOf("identity") > -1))
    {
      useIdentity = true;
    }
    else if (PKFieldName.length() > 0 && venderId == 2 && !paramsMap.containsKey(PKFieldName))
    {
      newId = getSequenceNextVal(tableName + "_SEQ", conn);
      paramsMap.put(PKFieldName, new Integer(newId));
    }
    
    ArrayList LobFieldList = new ArrayList();
    
    for (int i = 0; i < columnNameArr.length; i++)
    {
      String columnName = columnNameArr[i];
      if (!paramsMap.containsKey(columnName)) continue;
      if (paramsMap.get(columnName) == null) continue;
      ColumnData columnData = (ColumnData) columnDataMap.get(columnName);
      int columnType = columnData.SQLTypeId;
      if (paramValsArr.size() > 0)
      {
        sSQL += ",";
        sVals += ",";
      }
      
      sSQL += columnName;
      sVals += "?";
      paramValsArr.add(paramsMap.get(columnName));
      SQLTypesArr.add(new Integer(columnData.SQLTypeId));
      if (columnType == Types.CLOB || columnType == Types.BLOB) LobFieldList.add(columnName);
    }
    sSQL += ") " + sVals + ")";
    if (useIdentity) sSQL +=  ";SELECT @@IDENTITY";
    PreparedStatement ps = conn.prepareStatement(sSQL);
    ResultSet rs = null;
    setVals(ps, paramValsArr, SQLTypesArr);
    try
    {
      if (!useIdentity) ps.execute();
      else
      {
        rs=ps.executeQuery();
       // rs = ps.getGeneratedKeys(); //Для MSJDBC3 DRIVER
        if (rs.next()) newId = rs.getInt(1);
        paramsMap.put(PKFieldName, new Integer(newId));
      }
      
      if (rs != null) rs.close();
      if (ps != null) ps.close();
      
      if (PKFieldName.length() > 0 && venderId == 2 && LobFieldList.size() > 0)
      {
         for (int i = 0; i < LobFieldList.size(); i++)
         {
           String LOBFieldName = (String) LobFieldList.get(i);
           sSQL = "SELECT " + LOBFieldName + " FROM " + tableName + " WHERE " + PKFieldName + "=?";
           ps = conn.prepareStatement(sSQL);
           ps.setObject(1,paramsMap.get(PKFieldName));
           rs = ps.executeQuery();
           rs.next();
           ColumnData columnData = (ColumnData) columnDataMap.get(LOBFieldName);
           int columnType = columnData.SQLTypeId;
           if (columnType == Types.CLOB)
              writeToClob(rs.getClob(1), String.valueOf(paramsMap.get(LOBFieldName)));
           else if (columnType == Types.BLOB)
           {
             Object oData = paramsMap.get(LOBFieldName);
             byte bData[] = {};
             if  (oData instanceof byte[]) bData = (byte[]) oData;
             else bData = String.valueOf(bData).getBytes();
             writeToBlob(rs.getBlob(1), bData);
           }
           rs.close();
           ps.close(); 
         }
      }
      
    }
    catch (SQLException e)
    { 
      try 
      {
        if (doCommit) 
        {
          conn.rollback();
          conn.setAutoCommit(false);
        }
        if (rs != null) rs.close();
        if (ps != null) ps.close();
      }
      catch (SQLException ee){}
      throw e;
    }
    if(doCommit)
    {
      conn.commit();
      conn.setAutoCommit(connCommitState);
    }
    //jdenis: стераем из карты ключ к PK полю
    if(paramsMap.containsKey(PKFieldName)) paramsMap.remove(PKFieldName);
    
    //if (useIdentity) return newId;
    return newId;
  }
 /**
   * Метод выполняет обновление строки таблицы, Map содержит значения столбцов.
   * Если Map содержит имя столбца, но значение NULL, то значение устанавливается в NULL
   * Если Map не содержит имени столбца, то значение не обновляется.
   * Значения всех столбцов входящих в PK должны быть определены - обновить можно только
   * одну строку
   * @throws java.sql.SQLException
   * @param conn - SQL соединение
   * @param paramsMap - Map содержащий значения столбцов 
   * @param tableName - Имя таблицы
   */
  public static void updateRow(String tableName, HashMap paramsMap, Connection conn) throws SQLException
  {
    boolean doCommit = false;
    if (conn.getAutoCommit()) 
    {
      conn.setAutoCommit(false);
      doCommit = true;
    }
    String sSQL = "UPDATE " + tableName + " SET ";
    String sWhere = " WHERE ";
    ArrayList setValsArr = new ArrayList();
    ArrayList setSQLTypesArr = new ArrayList();
    ArrayList whereValsArr = new ArrayList();
    ArrayList whereSQLTypesArr = new ArrayList();
    
    String columnNameArr[] = getFieldNameArray(tableName, conn);
    String PKFieldNameArr[] = getPKFieldNameArray(tableName, conn);
    Map columnDataMap = getColumnDataMap(tableName, conn);
    int venderId = getDBVender(conn);
  
    if (columnNameArr.length == 0) throw new SQLException("No columns");
    if (PKFieldNameArr.length == 0) throw new SQLException("No PK columns");
  
    Hashtable PKKeysTbl = new Hashtable();
    ArrayList<String> setFieldsList = new ArrayList<String>();
    for (int i = 0; i < PKFieldNameArr.length; i++)
    {
      if (!paramsMap.containsKey(PKFieldNameArr[i]) || paramsMap.get(PKFieldNameArr[i]) == null)
          throw new SQLException("PK Value not SET"); 
      String columnName = PKFieldNameArr[i];
      if (setFieldsList.contains(columnName))continue;
      setFieldsList.add(columnName);
      ColumnData columnData = (ColumnData) columnDataMap.get(columnName);
      PKKeysTbl.put(columnName, "");
      
      if (whereValsArr.size() > 0) sWhere =sWhere + " AND ";
      sWhere =sWhere +  columnName + "=?";
    
      whereValsArr.add(paramsMap.get(columnName));
      whereSQLTypesArr.add(new Integer(columnData.SQLTypeId));
    }
  
    int cnt = 0;
    ArrayList LobFieldList = new ArrayList();
    
    for (int i = 0; i < columnNameArr.length; i++)
    {
      String columnName = columnNameArr[i];
      ColumnData columnData = (ColumnData) columnDataMap.get(columnName);
      int columnType = columnData.SQLTypeId;
      if (setFieldsList.contains(columnName))continue;
      if (!paramsMap.containsKey(columnName)) continue;
      if (PKKeysTbl.containsKey(columnName)) continue;
      Object data = paramsMap.get(columnName);
      if (data == null)
      {
        if (cnt > 0) sSQL += ",";
        sSQL += columnName + "=NULL";
      }
      else
      {
        if (cnt > 0) sSQL += ",";
        sSQL += columnName + "=?";
        setValsArr.add(data);
        setSQLTypesArr.add(new Integer(columnData.SQLTypeId));
        if (columnType == Types.CLOB || columnType == Types.BLOB) LobFieldList.add(columnName);
      }
      setFieldsList.add(columnName);
      cnt++;
    }
    int nUpdatedFields = cnt;
    setValsArr.addAll(whereValsArr);
    setSQLTypesArr.addAll(whereSQLTypesArr);
    sSQL += sWhere;
    //System.out.println(sSQL);
    PreparedStatement ps = conn.prepareStatement(sSQL);
    ResultSet rs = null;
    setVals(ps,setValsArr, setSQLTypesArr);
    if (nUpdatedFields > 0)
    {
      try 
      {
        ps.execute();
        if (ps != null) { ps.close(); ps = null;}
        if (venderId == 2 && LobFieldList.size() > 0)
        {
           for (int i = 0; i < LobFieldList.size(); i++)
           {
             String LOBFieldName = (String) LobFieldList.get(i);
             sSQL = "SELECT " + LOBFieldName + " FROM " + tableName + sWhere;
             ps = conn.prepareStatement(sSQL);
             setVals(ps,whereValsArr, whereSQLTypesArr);
             rs = ps.executeQuery();
             rs.next();
             ColumnData columnData = (ColumnData) columnDataMap.get(LOBFieldName);
             int columnType = columnData.SQLTypeId;
             if (columnType == Types.CLOB)
                writeToClob(rs.getClob(1), String.valueOf(paramsMap.get(LOBFieldName)));
             else if (columnType == Types.BLOB)
             {
               Object oData = paramsMap.get(LOBFieldName);
               byte bData[] = {};
               if  (oData instanceof byte[]) bData = (byte[]) oData;
               else bData = String.valueOf(bData).getBytes();
               writeToBlob( rs.getBlob(1), bData);
             }
             if (rs != null) { rs.close(); rs = null;}
             if (ps != null) { ps.close(); ps = null;}
           }
        }
      } 
      catch (SQLException e)
      {
        try 
        {
          if (doCommit) 
          {
            conn.rollback();
            conn.setAutoCommit(true);
          }
          if (ps != null) ps.close();
          if (rs != null) rs.close();
        }
        catch (SQLException ee){}
        throw e;
      }
    }
    if(doCommit)
    {
      conn.commit();
      conn.setAutoCommit(true);
    }
  }
 
 /**
   * Метод выполняет удаление строки таблицы. Значения всех столбцов составляющих 
   * PK должны быть включенны в HashMap
   * @throws java.sql.SQLException
   * @param conn - SQL соединение
   * @param paramsMap - Map содержащий значения столбцов составляющих PK
   * @param tableName - имя таблицы
   */
 
  public static void deleteRow(String tableName, HashMap paramsMap, Connection conn) throws SQLException
  {
    String sSQL = "DELETE FROM " + tableName;
    String sWhere = " WHERE ";
   
    ArrayList whereValsArr = new ArrayList();
    ArrayList whereSQLTypesArr = new ArrayList();
    
    String columnNameArr[] = getFieldNameArray(tableName, conn);
    String PKFieldNameArr[] = getPKFieldNameArray(tableName, conn);
    Map columnDataMap = getColumnDataMap(tableName, conn);
  
    if (columnNameArr.length == 0) throw new SQLException("No columns");
    if (PKFieldNameArr.length == 0) throw new SQLException("No PK columns");
  
    
    for (int i = 0; i < PKFieldNameArr.length; i++)
    {
      if (!paramsMap.containsKey(PKFieldNameArr[i]) || paramsMap.get(PKFieldNameArr[i]) == null)
          throw new SQLException("PK Value not SET"); 
      String columnName = PKFieldNameArr[i];
      ColumnData columnData = (ColumnData) columnDataMap.get(columnName);
           
      if (whereValsArr.size() > 0) sWhere += " AND ";
      sWhere += columnName + "=?";
    
      whereValsArr.add(paramsMap.get(columnName));
      whereSQLTypesArr.add(new Integer(columnData.SQLTypeId));
    }
  
    sSQL += sWhere;
    //System.out.println(sSQL);
    PreparedStatement ps = conn.prepareStatement(sSQL);
    setVals(ps,whereValsArr, whereSQLTypesArr);
    ps.execute();
    try {if (ps != null) ps.close();}catch (SQLException e){}
  }
  /**
   * Метод возвращает в виде HashMap значения всех полей строки таблицы в виде объектов 
   * сооветсвующего типа или null, ключами в Map служат имена столбцов
   * Строка задается значениями столбцов составляющих PK, все столбцы составляющих PK
   * должны иметь определенное значение передаваемое через paramsMap
   * @throws java.sql.SQLException
   * @return HashMap - содержит значения полей сторки
   * @param conn - SQL соединение
   * @param paramsMap - Map значений полей составляющих PK
   * @param tableName - имя таблицы
   */
  public static HashMap selectRow(String tableName, HashMap paramsMap, Connection conn) throws SQLException
  {
    String sSQL = "SELECT ";
    String sFrom = " FROM " + tableName;
    String sWhere = " WHERE ";
   
    ArrayList whereValsArr = new ArrayList();
    ArrayList whereSQLTypesArr = new ArrayList();
    
    String columnNameArr[] = getFieldNameArray(tableName, conn);
    String PKFieldNameArr[] = getPKFieldNameArray(tableName, conn);
    Map columnDataMap = getColumnDataMap(tableName, conn);
  
    if (columnNameArr.length == 0) throw new SQLException("No columns");
    if (PKFieldNameArr.length == 0) throw new SQLException("No PK columns");
    
    for (int i = 0; i < columnNameArr.length; i++)
    {
      if (i > 0) sSQL += ",";
      sSQL += columnNameArr[i];
    }
    
    for (int i = 0; i < PKFieldNameArr.length; i++)
    {
      if (!paramsMap.containsKey(PKFieldNameArr[i]) || paramsMap.get(PKFieldNameArr[i]) == null)
          throw new SQLException("PK Value not SET"); 
      String columnName = PKFieldNameArr[i];
      ColumnData columnData = (ColumnData) columnDataMap.get(columnName);
           
      if (whereValsArr.size() > 0) sWhere += " AND ";
      sWhere += columnName + "=?";
    
      whereValsArr.add(paramsMap.get(columnName));
      whereSQLTypesArr.add(new Integer(columnData.SQLTypeId));
    }
  
    sSQL += (sFrom + sWhere);
    //System.out.println(sSQL);
    PreparedStatement ps = conn.prepareStatement(sSQL);
    
    setVals(ps,whereValsArr, whereSQLTypesArr);
    ResultSet rs = ps.executeQuery();
    HashMap res = new HashMap();
    if (rs.next())
    {
      for (int i = 0; i < columnNameArr.length; i++)
      {
        ColumnData columnData = (ColumnData) columnDataMap.get(columnNameArr[i]);
        int columnType = columnData.SQLTypeId;
        //String aa = ColumnData.getSqlTypeName(columnType);
        //System.out.println(aa);
        switch (columnType)
        {
          case Types.CLOB:
            res.put(columnNameArr[i],readClob( rs.getClob(columnNameArr[i])));
          break;
          case Types.BLOB:
            res.put(columnNameArr[i],readBlob( rs.getBlob(columnNameArr[i])));
          break;
          case Types.DATE:
          java.sql.Date sqlDate = rs.getDate(columnNameArr[i]);
          if (sqlDate != null)  res.put(columnNameArr[i],new java.util.Date(sqlDate.getTime()));
          else  res.put(columnNameArr[i],null);
          break;
          case Types.TIMESTAMP:
          Timestamp sqlTimestamp = rs.getTimestamp(columnNameArr[i]);
          if (sqlTimestamp != null) res.put(columnNameArr[i],new java.util.Date(sqlTimestamp.getTime()));
          else  res.put(columnNameArr[i],null);
          break;
          case Types.BIGINT:
          case Types.NUMERIC:
          case Types.SMALLINT:
          case Types.DECIMAL:
          int iVal = rs.getInt(columnNameArr[i]);
          res.put(columnNameArr[i], new Integer(iVal));
          break; 
          default:
          res.put(columnNameArr[i],rs.getObject(columnNameArr[i]));
        }
      }
    }
    try 
    {
      if (rs != null) rs.close();
      if (ps != null) ps.close();
    }catch (SQLException e){}
    
    return res;
  }
  /**
   * Устанавливает значения переменных в PreparedStatement в соответствии со значениями 
   * и типами объектов включенных в список valArray, с учетом SQL типами полей включенных
   * в список typeArray
   * @throws java.sql.SQLException
   * @param typeArray - список SQL типов
   * @param valArray - список значений
   * @param ps - PreparedStatement
   */
  private static void setVals(PreparedStatement ps, ArrayList valArray, ArrayList typeArray) throws SQLException
  {
    if (valArray == null || typeArray == null || valArray.size() != typeArray.size()) throw new SQLException("Invalid params or SQL types array");
    Connection conn = ps.getConnection();
    for (int i = 0; i < valArray.size(); i++)
    {
      int typeId = ((Integer) typeArray.get(i)).intValue();
      Object data = valArray.get(i);
      if (data == null)
      {
        ps.setNull(i + 1, typeId);
        continue;
      }
      switch (typeId)
      {
        case Types.BINARY:
        case Types.VARBINARY:
        case Types.LONGVARBINARY: 
        if (data instanceof byte[]) ps.setBytes(i + 1, (byte[]) data);
        else ps.setBytes(i + 1, data.toString().getBytes());
        break;
        
        case Types.DATE:
        case Types.TIMESTAMP:
        if (data instanceof String) ps.setString(i + 1, (String) data);
        else if (data instanceof java.util.Date) 
          ps.setTimestamp(i + 1, new java.sql.Timestamp(((java.util.Date) data).getTime() ));
     /*   else if (data instanceof java.sql.Date) 
          ps.setTimestamp(1 + 1, (java.sql.Date) data); */
        break;
        case Types.CLOB:
        //Clob clob = CLOB.empty_lob();
        ps.setString(i+1, String.valueOf(data));
        break;
        case Types.BLOB:
//        Blob blob = null;

        ps.setBinaryStream(i+1,new ByteArrayInputStream((byte[])data),((byte[])data).length);
        //ps.setBlob(i + 1, blob);
        break;
        default:
        if (data instanceof String) ps.setString(i + 1, (String) data);
        if (data instanceof Integer) ps.setInt(i + 1, ((Integer) data).intValue());
        if (data instanceof Long) ps.setLong(i + 1, ((Long) data).longValue());
        if (data instanceof Float) ps.setFloat(i + 1, ((Float) data).floatValue());
        if (data instanceof Double) ps.setDouble(i + 1, ((Double) data).doubleValue());
      }
    }
  }
  
  /**
   * Возвращает Id разработчика БД  0 - неизвестный, 1 - MS, 2 -ORACLE
   * @return 
   */
  public static int getDBVender(Connection conn) throws SQLException
  {
    String ProductName = conn.getMetaData().getDatabaseProductName().toUpperCase();
    int DBVenderId = 0;
    if (ProductName.indexOf("MICROSOFT") >= 0) DBVenderId = 1;
    else if (ProductName.indexOf("ORACLE") >= 0) DBVenderId = 2;
    else DBVenderId = 0;
    return DBVenderId;
  }
  
  public static int getSequenceNextVal(String SeqName, Connection conn) throws SQLException
  {
    int res = -1;
    String sSQL = "SELECT " + SeqName + ".nextval FROM dual";
    PreparedStatement ps = conn.prepareStatement(sSQL);
    ResultSet rs = ps.executeQuery();
    try
    {
      rs.next();
      res = rs.getInt(1);
      rs.close();
      ps.close();
    }
    catch (SQLException e)
    {
      if (rs != null) rs.close();
      if (ps != null) ps.close();
      throw e;
    }
    return res;
  }
  
  static void writeToClob(Clob clob, String text) throws SQLException
  {
    clob.setString(1,text);
  }
  
  static String readClob(Clob clob) throws SQLException
  {
    if (clob == null) return "";
    int len = (int) clob.length();
    return clob.getSubString(1, len);
  }
  
  static void writeToBlob(Blob blob, byte[] bData) throws SQLException
  {
    int buffLen = 1024;
    byte buff[] = new byte[buffLen];
    ByteArrayInputStream inp = new ByteArrayInputStream(bData);
    OutputStream out = blob.setBinaryStream(1);
    int cnt = 0;
    try
    {
      while ((cnt = inp.read(buff,0,buffLen)) >= 0) out.write(buff,0,cnt);
      out.flush();
      out.close();
    }
    catch(IOException ioe){throw new SQLException(ioe.getMessage());}
    //inp.
  }
  
  static byte[] readBlob(Blob blob) throws SQLException 
  {
    if (blob == null) return new byte[0];
    int buffLen = 1024;
    byte buff[] = new byte[buffLen];
    ByteArrayOutputStream out = new ByteArrayOutputStream();
    InputStream inp = blob.getBinaryStream();
    int cnt = 0;
    try
    {
      while ((cnt = inp.read(buff,0,buffLen)) >= 0) out.write(buff,0,cnt);
    }
    catch(IOException ioe){throw new SQLException(ioe.getMessage());}
    return out.toByteArray();
  }
   /**
   * ТОЛЬКО ДЛЯ ТЕСТИРОВАНИЯ!!!!!!
   * @param args
   */
  public static void main(String[] args)
  {
    String driverName = "com.microsoft.jdbc.sqlserver.SQLServerDriver";
    //String connectionString = "jdbc:microsoft:sqlserver://mobile:1433;DatabaseName=testdb;User=sa;Password=avk1258;SelectMethod=cursor;";
    String connectionString = "jdbc:microsoft:sqlserver://192.168.0.109:1433;DatabaseName=Infograd05;User=LITMS;Password=call7708;SelectMethod=cursor;";
    String oraDriverName ="oracle.jdbc.driver.OracleDriver";
    String oraConnectionString = "jdbc:oracle:thin:@192.168.0.105:1521:DNET";
    String oraConnectionString_home = "jdbc:oracle:thin:@localhost:1521:ORCL";
    
    //String oraConnectionString = "jdbc:oracle:thin:@192.168.0.105:1521:dnet";
    Connection conn = null;
   // Conn
    try
    {
      
  // conn = openSQLConnection(driverName, connectionString);
      conn = openSQLConnection(oraDriverName, oraConnectionString,"MGP_DATA", "mgpuser");
      //conn = openSQLConnection(oraDriverName, oraConnectionString,"SCOTT", "TIGER");
      System.out.println(conn.getMetaData().getDatabaseProductName());
      System.out.println(getDBVender(conn));
      PORTALDBConfig.initDBConfig(conn);
      System.out.println(PORTALDBConfig.sequenceExist("OBJECT_METADATA_SEQ"));
/*     
      ObjectMetadata objMd = new ObjectMetadata();
      objMd.setTypeId(4);
      objMd.setName("Третья Тестовая новость");
      objMd.setDescription("Текст третьей тестовой новости");
      HashMap dataHm  = objMd.getDataMap();
      CommonTransaction trans = new CommonTransaction(null);
      trans.addObject("News", dataHm, conn);
*/
      int idArr[] = PORTALDBConfig.getParentObjectTypeArr(4);
//      ObjectMetadata objMd = new ObjectMetadata(3,conn);
//      BndObject2Rubric bndArr[] = objMd.getObjectRubricBnd(conn);
//      int cnt = bndArr.length;

      //CommonTransaction trans = new CommonTransaction(null);
      //trans.addObjectToCollection(3,31,1,2,conn);
      //trans. changeBndOrderInCollection(3,21,2,conn);
      //trans.bindObjectToRubricator(3,4,3,-1,conn);

      conn.close();
      System.out.println("OK");
    }
    catch(Exception e)
    {
      System.out.println(e.getMessage());
    }
    
  }
  
  
  public static Connection openSQLConnection(String driverName, String connectionString) throws Exception
  {
    Class.forName(driverName); 
    return DriverManager.getConnection(connectionString);
  }
 
  public static Connection openSQLConnection(String driverName, String connectionString, String user, String password) throws Exception
  {
    Class.forName(driverName); 
    return DriverManager.getConnection(connectionString,user,password);
  }
  
  public static Connection openSQLConnectionByName(String Name) throws SQLException
  {
     clearConnectionMap();
     try { ///
      Context initCtx = new InitialContext();
      DataSource ds = (DataSource) initCtx.lookup(Name);
      Connection conn = ds.getConnection();
      registerConnection(conn);
      return conn;
    }
    catch(NamingException ne)
    {
      throw new SQLException(ne.getMessage());
    }
   
  }
  
  public static Connection openSQLConnectionByName(String Name, Context initCtx) throws SQLException
  {
     clearConnectionMap();
     if (initCtx == null) throw new SQLException("openSQLConnectionByName: Context is null");
     try { ///
      DataSource ds = (DataSource) initCtx.lookup(Name);
      Connection conn = ds.getConnection();
      registerConnection(conn);
      return conn;
    }
    catch(NamingException ne)
    {
      throw new SQLException(ne.getMessage());
    }
   
  }
  public static void closeSQLConnection(Connection conn)
  {
    try{if (conn !=null) conn.close();}catch(Exception ee){}
  }
  
  private static void registerConnection(Connection conn)
  {
      Thread thisThread = Thread.currentThread();
      Map stackTraceMap = Thread.getAllStackTraces();
      StackTraceElement stackTraceElement[] = (StackTraceElement[]) stackTraceMap.get(thisThread);
      StackTraceElement callerElement = null;
      if ( stackTraceElement != null )
      {
        boolean flag = false;
        for (int i = 0; i < stackTraceElement.length; i++ )
        {
          if (stackTraceElement[i].getClassName().equals(DBTools.class.getName()))
          {
            flag = true;
          }
          else if (flag)
          {
            callerElement = stackTraceElement[i];
            break;
          }
        }
      }
      String callerData = "";
      if (callerElement != null) 
      {
        callerData = callerElement.getClassName() +" : " + callerElement.getMethodName() + "[" + callerElement.getLineNumber() + "]";
        //System.out.println(callerData);
        openConnectionsMap.put(conn,callerData);
      }
  }
    
 
  public static java.util.List getOpenConnData()
  {
    clearConnectionMap();
    ArrayList dataArr = new ArrayList();
    Set keySet = openConnectionsMap.keySet();
    for (Iterator itr = keySet.iterator(); itr.hasNext();)  dataArr.add(openConnectionsMap.get(itr.next()));
    return dataArr;
  }
  
  private static void clearConnectionMap()
  {
    ArrayList removeArr = new ArrayList();
    Set keySet = openConnectionsMap.keySet();
    for (Iterator itr = keySet.iterator(); itr.hasNext();)
    {
      Connection conn = (Connection) itr.next();
      try
      {
        if (conn.isClosed()) removeArr.add(conn);
      }
      catch(Exception e)
      {
        System.out.println(e.getMessage());
        removeArr.add(conn);
      }
    }
    for (Iterator itr = removeArr.iterator(); itr.hasNext();) openConnectionsMap.remove(itr.next());
  }
}