package org.inet.dbtools;

import java.sql.ResultSet;
import java.sql.SQLException;

import java.util.ArrayList;


public class PORTALObjectType 
{

  private static final String[] FIELDS_NAMES = 
  {
    "OBJECT_TYPE_ID",
    "UP_OBJECT_TYPE_ID",
    "OBJECT_TYPE_ORDER",
    "OBJECT_TYPE_NAME_RU",
    "OBJECT_TYPE_NAME_ENG",
    "OBJECT_TYPE_DESCRIPTION_RU",
    "OBJECT_TYPE_DESCRIPTION_ENG",
    "OBJECT_TABLE_NAME",       
    "KIND_OBJECT_TYPE",        
    "CONTENT_MANAGER_ID",   
    "COLLECTION_CONTENT_MANAGER_ID",
    "OBJECT_TYPE_STATUS",
    "OBJECT_ALLOW_COMMENT",
    "SLAVE_OBJECT_TYPE_ID",
    "OBJECT_IS_INDEPENDENT"
  };
  
  public static final int INFORMATION_OBJECT = 1;
  public static final int OBJECT_COLLECTION  = 2;
  public static final int ADMIN_SERVICE      = 3;
  public static final int RUBICATOR          = 5;
  
  private int    typeId = 0;
  private String typeName = "";
  private int    kindOfObjectType = INFORMATION_OBJECT;
  private int    upTypeId = 0;
  private String typeDescription = "";
  private String tableName = "";
  private int    contentManagerId = -1;
  private int    collectionManagerId = -1;
  private int    objectIsIndependent = -1;
  private ArrayList bndObjectTypeList = new ArrayList();
  
  public PORTALObjectType() {}
  
  public PORTALObjectType(ResultSet rs) throws SQLException
  {
      typeId = rs.getInt(FIELDS_NAMES[0]);
      typeName = rs.getString(FIELDS_NAMES[3]);
      typeDescription = rs.getString(FIELDS_NAMES[5]);
      upTypeId =  rs.getInt(FIELDS_NAMES[1]);
      tableName = rs.getString(FIELDS_NAMES[7]);
      kindOfObjectType = rs.getInt(FIELDS_NAMES[8]);
      contentManagerId = rs.getInt(FIELDS_NAMES[9]);
      collectionManagerId = rs.getInt(FIELDS_NAMES[10]);
      objectIsIndependent = rs.getInt(FIELDS_NAMES[14]);
  }
  
  public void setTypeId(int typeId) {this.typeId = typeId;}
  public int getObjectTypeId() {return typeId;}
  
  public void setObjectIsIndependent(int objectIsIndependent) {this.objectIsIndependent = objectIsIndependent;}
  public int getObjectIsIndependent() {return objectIsIndependent;}
  
  public void setTypeName(String typeName){this.typeName = typeName;}
  public String getTypeName() {return typeName;}
  
  public void setKindOfObjectType(int kindOfObjectType) {this.kindOfObjectType = kindOfObjectType;}
  public int getKindOfObjectType() {return kindOfObjectType;}
  
  public void setUpTypeId(int upTypeId){this.upTypeId = upTypeId;}
  public int getUpTypeId(){return upTypeId;}
  
  public void setTypeDescription(String typeDescription) {this.typeDescription = typeDescription;}
  public String getTypeDescription() {return typeDescription;}
  
  public void setTableName(String tableName) {this.tableName = tableName;}
  public String getTableName() {return tableName;}
  
  public void setContentManagerId(int contentManagerId) {this.contentManagerId = contentManagerId;}
  public int getConentManagerId() {return contentManagerId;}
  
  public void setCollectionContentManagerId(int collectionManagerId)
  {
    this.collectionManagerId = collectionManagerId;
  }
  
  public int getCollectionContentManagerId()
  {
    return this.collectionManagerId;
  }
  
  public ArrayList getBndObjectTypeList(){return bndObjectTypeList;}
  public void setBndObjectTypeList(ResultSet rs)throws Exception
  {
    while (rs.next())
    {
      int slaveObjectTypeId = rs.getInt(FIELDS_NAMES[13]);
      bndObjectTypeList.add(slaveObjectTypeId);
    }
  }
}