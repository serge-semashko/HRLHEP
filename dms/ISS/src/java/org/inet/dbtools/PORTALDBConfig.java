package org.inet.dbtools;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import java.util.ArrayList;
import java.util.Hashtable;
import java.util.Iterator;

/**
 * Класс предназначен для получения данных по конфигурации системы хранения
 */
public class PORTALDBConfig 
{
  public static final int staticCollectionTypeId = 100;

  private static int DBVenderId = 0;
  private static String sSQLObjectTypes = "SELECT * FROM RUBRICATOR_OBJECT_TYPE";
  private static String sSQLBndObjectTypeList = "SELECT * FROM BND_OBJECTTYPE_OBJECTTYPE WHERE MASTER_OBJECT_TYPE_ID = ";
  private static Hashtable ObjectTypesMap = new Hashtable();
  private static String ownerName = "MGP_DATA";
  private static ArrayList SequenceList = new ArrayList();
  private static String sSQLSequenceList = "SELECT SEQUENCE_NAME FROM sys.all_sequences " +
                            " WHERE SEQUENCE_OWNER='" + ownerName +"'";
  private static ArrayList TableList = new ArrayList();
  private static String sSQLTableList = "SELECT TABLE_NAME FROM sys.all_tables " +
                            " WHERE OWNER='" + ownerName +"'";
  
  /**
   * Возвращает Id разработчика БД  0 - неизвестный, 1 - MS, 2 -ORACLE
   * @return 
   */
  public static int getDBVenderId()
  {
    return DBVenderId;
  }

  public static PORTALObjectType getObjectType(int objectTypeId)
  {
    return (PORTALObjectType) ObjectTypesMap.get(objectTypeId);
  }

  /**
   * Метод возвращает имя объекта по его Id
   * @return 
   * @param objectTypeId
   */
  public static String getObjectNameById(int objectTypeId)
  {
    PORTALObjectType objectType = (PORTALObjectType) ObjectTypesMap.get(new Integer(objectTypeId));
    if (objectType != null) return objectType.getTypeName();
    return null;
  }

  /**
   * Метод возвращает Id объекта по его имени
   * @return 
   * @param objectName
   */
  public static int getObjectIdByName(String objectName)
  {
    for (Iterator it = ObjectTypesMap.values().iterator(); it.hasNext();)
    {
      PORTALObjectType objectType = (PORTALObjectType) it.next();
      if (objectName.equals(objectType.getTypeName())) return objectType.getObjectTypeId();
    }
    return 0;
  }
/**
   * Метод возвращает имена таблиц, в которых содержатся атрибуты объекта
   * @return String[]
   * @param objectTypeId
   */
  public static String[] getObjectTableNames(int objectTypeId)
  {
    PORTALObjectType objectType = null;
    int currentId = objectTypeId;
    ArrayList arr = new ArrayList();
    while((objectType = (PORTALObjectType) ObjectTypesMap.get(new Integer(currentId))) != null)
    {
      String tableName = objectType.getTableName();
      if (tableName !=  null && tableName.length() != 0 ) arr.add(objectType.getTableName());
      currentId = objectType.getUpTypeId();
    }
    String res[] = new String[arr.size()];
    for(int i = arr.size() - 1; i > -1; i--) res[arr.size() - i - 1] = (String) arr.get(i);
    return res;
  }
  /**
   * Метод выполняет инициализацию данных (статических) класса
   * @throws java.sql.SQLException
   * @param conn
   */
  public static void initDBConfig(Connection conn) throws java.sql.SQLException
  {
  /*
   * Определяем диалект (разработчика) БД
   */
    String ProductName = conn.getMetaData().getDatabaseProductName().toUpperCase();
    if (ProductName.indexOf("MICROSOFT") >= 0) DBVenderId = 1;
    else if (ProductName.indexOf("ORACLE") >= 0) DBVenderId = 2;
    else DBVenderId = 0;
  /*
   * Загрузка типов информационных объектов
   */
    synchronized(ObjectTypesMap)
    {
      ObjectTypesMap.clear();
      PreparedStatement ps = null;
      ResultSet rs = null;
      try
      {
        ps = conn.prepareStatement(sSQLObjectTypes);
        rs = ps.executeQuery();
        while (rs.next())
        {
          PORTALObjectType objectType = new PORTALObjectType(rs);
          PreparedStatement ps1 = null;
          ResultSet rs1 = null;
          try
          {
            ps1 = conn.prepareStatement(sSQLBndObjectTypeList + objectType.getObjectTypeId());
            rs1 = ps1.executeQuery();
            objectType.setBndObjectTypeList(rs1);
          }
          catch(Exception e)
          {
            try
            {
              if (rs1 != null)rs1.close(); 
              if (ps1 != null)ps1.close(); 
            }
            catch (SQLException ee) {}
          }
          ObjectTypesMap.put(new Integer(objectType.getObjectTypeId()), objectType);
        }
        if (rs != null) rs.close();
        if (ps != null) ps.close();
        SequenceList.clear();
        TableList.clear();
        if (DBVenderId == 2) //Oracle
        {
          ps = conn.prepareStatement(sSQLSequenceList);
          rs = ps.executeQuery();
          while (rs.next()) SequenceList.add(rs.getString(1));
          if (rs != null) rs.close();
          if (ps != null) ps.close();
          
          ps = conn.prepareStatement(sSQLTableList);
          rs = ps.executeQuery();
          while (rs.next()) TableList.add(rs.getString(1));
          if (rs != null) rs.close();
          if (ps != null) ps.close();
        }
        
      }
      catch(SQLException e)
      {
        try
        {
          if (rs != null) rs.close();
          if (ps != null) ps.close();
        }
        catch (SQLException ee) {    }
        throw e;
      }
    }
  }//initDBConfig
  
  public static boolean ObjectTypeIsCollection(int objectTypeId) throws SQLException
  {
    PORTALObjectType objectType = (PORTALObjectType) ObjectTypesMap.get(new Integer(objectTypeId));
    if (objectType == null) throw new SQLException("тип объекта не найден");
    int objectTypeKind = objectType.getKindOfObjectType();
    return (objectTypeKind == PORTALObjectType.OBJECT_COLLECTION);
  }
  
  public static int[] getParentObjectTypeArr(int objectTypeId)
   {
    PORTALObjectType objectType = null;
    int currentId = objectTypeId;
    ArrayList arr = new ArrayList();
    while((objectType = (PORTALObjectType) ObjectTypesMap.get(new Integer(currentId))) != null)
    {
      arr.add(new Integer(currentId));
      currentId = objectType.getUpTypeId();
    }
    int res[] = new int[arr.size()];
    for(int i = arr.size() - 1; i > -1; i--) res[arr.size() - i - 1] = ((Integer) arr.get(i)).intValue();
    return res;
  }
  
  public static boolean sequenceExist(String seqName)
  {
    return SequenceList.contains(seqName);
  }
  
  public static boolean tableExist(String tableName)
  {
    return TableList.contains(tableName);
  }
  
}