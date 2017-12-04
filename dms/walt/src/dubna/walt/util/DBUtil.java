package dubna.walt.util;

import java.sql.*;
import java.util.Hashtable;
import java.util.Enumeration;

/**
 *
 * @author serg
 */
public class DBUtil
{

//public static boolean busy=false;

public long timer = 0,

    /**
     *
     */

    /**
     *
     */
    t0;

    /**
     *
     */
    public int numQueries = 0;

    /**
     *
     */
    public String timeSQL = "";

    /**
     *
     */
    public String timeSpent = "";

    /**
     *
     */
    public String queryLabel = "";

    /**
     *
     */
    public String myName = "";

    /**
     *
     */
    public String connStr = "";

    /**
     *
     */
    public String usr = "";

    /**
     *
     */
    public String pw = "";

    /**
     *
     */
    public Connection m_conn=null;

    /**
     *
     */
    public Hashtable stmnts = null;
private Statement curr_stmt = null;

private DBUtil childDB = null;

    /**
     *
     */
    public boolean terminated = false;
private boolean inUse = false;
 
    /**
     *
     */
    protected int numberInChain = 1; 
 
    /**
     *
     */
    public int nrConnsToKeep = 2; 

    /**
     *
     */
    public static int DB_ORA=0;

    /**
     *
     */
    public static int DB_MSSQL=1;

    /**
     *
     */
    public static int DB_MySQL=2;

    /**
     *
     */
    public int db=DB_ORA;

/**
 *
     * @throws Exception
 */
public DBUtil() throws Exception
{
}

/**
 *
     * @param connStr
     * @param usr
     * @param pw
     * @param queryLabel
     * @param numberInChain
     * @throws java.lang.Exception
 */
public DBUtil(String connStr,
              String usr,
              String pw,
              String queryLabel,
              int numberInChain) throws Exception
{
  this.connStr = connStr;
  this.usr = usr;
  this.pw = pw;
  this.queryLabel=queryLabel;
  this.numberInChain = numberInChain;
  myName = this.toString().substring(15) + "(" + queryLabel + ")/" 
    + Integer.toString(numberInChain);
  connect();
}

    /**
     *
     * @throws Exception
     */
    public void reconnect() throws Exception
{ commit();
	close();
	System.gc();
	Thread.sleep(1000);
	connect();
}

    /**
     *
     * @throws Exception
     */
    public void connect() throws Exception
{
//  rm.println("Connecting: '" + connStr + "' (" + usr + "/*****)" ); 
//  rm.println(pw); 
  try
  { m_conn = DriverManager.getConnection(connStr, usr, pw);
  } catch (Exception e) 
  { //rm.println("*Not Connected: " + e.toString());
    throw e;
  }
//  System.out.println("*Connected: " + myName); // + "; m_conn=" + m_conn);

  stmnts = new Hashtable();  
  if (db == DB_MySQL)
  {  m_conn.setAutoCommit(false);  //***** 14.05.06    
  }
  if (db == DB_ORA)
  {  m_conn.setAutoCommit(true);  //***** 01.04.06
     try   //***** 01.04.06
     { curr_stmt = m_conn.createStatement();
       int res = curr_stmt.executeUpdate("ALTER SESSION SET NLS_NUMERIC_CHARACTERS='. ' ");
       curr_stmt.close();
     }
     catch (Exception e) /**/
     { /* we don't care - this is valid for ORACLE only */ }
  }
}

/**
 *
     * @param conn
     * @param queryLabel
     * @throws Exception
 */
public DBUtil (Connection conn, String queryLabel ) throws Exception
{
  m_conn = conn;
  this.queryLabel=queryLabel;
  stmnts = new Hashtable();
  myName = this.toString().substring(15) + "(" + queryLabel + ")";
}

/**
 *
     * @param conn
     * @throws java.lang.Exception
 */
public DBUtil (Connection conn) throws Exception
{
  m_conn=conn;
  stmnts = new Hashtable();
  myName = this.toString().substring(15);
}

/**
 *
     * @return 
 */
/* 
public synchronized DBUtil cloneDBUtil(String queryLabel) throws Exception
{
//  System.out.println(queryLabel + "/" + numberInChain + ": ~~~~~ cloneDBUtil...");
  if (!inUse)
  { if (isAlive()) 
    { this.queryLabel = queryLabel;
      allocate();
      return this;
    }
    else
    { try 
      { connect();
        if (isAlive()) 
        { this.queryLabel = queryLabel;
          allocate();
          return this;
        }
      }
      catch (Exception e) {   }
    }
  }
  
  if (childDB != null) 
    return childDB.cloneDBUtil(queryLabel);
  
  try
  { if (connStr.length() > 0)
    { childDB = new DBUtil (connStr, usr, pw, queryLabel, numberInChain + 1);
      childDB.nrConnsToKeep = this.nrConnsToKeep;
    }
    else
      childDB = new DBUtil (m_conn, queryLabel);
    childDB.allocate();
  }
  catch (Exception ex)
  { throw new Exception(ex.toString() + " (in " + myName + "/cloneDBUtil) ");
  }
  return childDB;
}
/**/

public boolean isInUse()
{ return inUse;
}

    /**
     *
     */
    public synchronized void allocate()
{ inUse = true;
  terminated = false;
  numQueries = 0;
}

    /**
     *
     */
    public void release()
{ inUse = false;
//    System.out.println( "***** release *****" + myName);
  terminated = false;
  numQueries = 0;
  if (childDB != null)
  { childDB.finish();
    if (numberInChain >= nrConnsToKeep)
      childDB = null;
  }
  if (numberInChain == -1)
    close();
}

    /**
     *
     */
    protected void finish()
{ if (childDB != null)
    childDB.finish();
  if (numberInChain > nrConnsToKeep)
  { if (!inUse)
      close();
    else
      numberInChain = -1;
  }
}

protected void finalize()
{ close();
}

    /**
     *
     * @return
     */
    public boolean isAlive()
{ 
// System.out.println( "***** isAlive() ..." + myName);
//  if (inUse)
//    System.out.println( "***** isAlive() - I'AM Busy!!!" + myName);
  try
  {
//  System.out.println( "***** isClosed():" + m_conn.isClosed() );
    if (m_conn.isClosed()) 
     throw (new Exception("Connection is closed")); 
    Statement stmt = m_conn.createStatement();
    stmt.close();
//    System.out.println("..... " + myName + "; Connection is alive.");
    return true;
  }
  catch (Exception e)
  {
//		System.out.println("xxxxx " + myName + "; Connection is dead: " + e.toString());
    closeAllStatements();
    try { m_conn.close();} catch (Exception ex) {}
    return false;
  }
}

/**
 *
     * @return 
 */
public Connection getConnection()
{  return m_conn;
}

    /**
     *
     * @param r
     */
    public void closeResultSet(ResultSet r)
{
  if (stmnts == null || r == null) return;
  try
  { // System.out.println(stmnts.size() + ": removing " + r);
    Statement stmt = (Statement) stmnts.get(r);
    stmnts.remove(r);
    if (stmt != null)
      stmt.close();
//    System.out.println(stmnts.size() + ": removed... ");
  }
  catch (Exception e)
  { 
		System.out.println("+++++++ " +  myName + "; stmnts =" + stmnts + "; r=" + r);
    e.printStackTrace(System.out); 
  }  
}

private void closeAllStatements()
{ if (stmnts == null || stmnts.isEmpty()) return;
  ResultSet r;
  Enumeration e = stmnts.keys();
  while (e.hasMoreElements())
  { try
    { r = (ResultSet) e.nextElement();
      closeResultSet(r);
//      System.out.println("******** " + myName + ":  closing statement - OK");
    }
    catch (Exception ex)
    { 
			System.out.println("++++++++ " + myName + ": closeAllStatements(): " + ex.toString());
    }
  }
  stmnts.clear();
}

/**
 *
     * @param sql
     * @return 
     * @throws Exception 
 */
//public synchronized ResultSet getResults(String sql) throws Exception
public ResultSet getResults(String sql) throws Exception
{ if (terminated) return null;
  if (m_conn == null) 
  { 
//		System.out.println("++++++++++ DBUtil closed! Could not execute 'getResults'" + this); 
    return null;
  }
  if (sql == null || sql.length() < 3) return null;
  
//  if (stmnts.size() > 4)
//    System.out.println("========== " + myName + ": total number of statements is " + stmnts.size() + ";");
  if (stmnts.size() > 8)
  { 
//		System.out.println("========== " + myName + ": total number of statements is " + stmnts.size() + "; CLOSING ALL OF THEM ...");
    closeAllStatements();
  }
  
  try
  { numQueries++;
//    System.out.println(queryLabel+ " - executing " + numQueries + "...");
    Statement stmt = m_conn.createStatement();
    curr_stmt = stmt;
    t0 = System.currentTimeMillis();
// System.out.println(queryLabel+ ": " + sql + ";");
    ResultSet r=stmt.executeQuery(sql);
    t0 = System.currentTimeMillis() - t0;
    timer += t0;
    timeSpent = StrUtil.formatDouble(t0,0, " ");
    timeSQL  += "<BR>  SQL " + numQueries + " processed in " +timeSpent+"ms.<br>";
    try 
    {
      stmnts.put(r, stmt);
//    System.out.println("========== " + myName + ": total number of statements is " + stmnts.size() + ";");
    }
    catch (Exception exx)
    { 
//			System.out.println("++++++++++++++++++++++++++++++++++++++++++++++++");
//      System.out.println("stmnts:" + stmnts + ";\n\r curr_stmt: "+ stmt + ";\n\r r:" + r);
//      System.out.println(exx.toString());
//      System.out.println("++++++++++++++++++++++++++++++++++++++++++++++++");
			throw exx;
    }
    
//    curr_stmt = null;
//    if (stmnts.size() > 5)
//      System.out.println("~~~~~~~ " + myName + ": - there are " + stmnts.size() + " opened statements!!!");
    return r;
  }
  catch (Exception ex)
  { if (terminated) return null;;
//    System.out.println("++++++++ " + myName + " - Exception:");
//    System.out.println("++++++++ " + ex.toString());
	  throw new java.sql.SQLException(ex.getMessage() + "; SQL: '" + sql + "'");
//    throw ex;
  }
}

/**
 *
 */
public void terminate()
{ terminated=true;
  inUse = false;
  try
  { 
//		System.out.println( myName + "/terminate()");
    if (curr_stmt != null)
    { curr_stmt.cancel();
      curr_stmt.close();
    }
  }
  catch (Exception ex)
  { 
//		System.out.println("++++++++ " + myName);
//    ex.printStackTrace(System.out);
  }
  finally
  { closeAllStatements();
    curr_stmt = null;
  }
}

/**
 *
 */
public void close() //throws Exception
{ 
/*
	if (m_conn != null) 
	{ try
		{ 
		System.out.println("***** Close " + myName+ "; m_conn:" + m_conn + "; closed="  + m_conn.isClosed());
	//  System.out.println("***** stmnts:" + stmnts);
		}
		catch (Exception ex)
		{ ex.printStackTrace(System.out);
		}
	}
/**/

  try
  { if (m_conn != null) 
    { closeAllStatements();
      m_conn.close();
//      System.out.println("***** closed=" + m_conn.isClosed());
    }
  }
  catch (Exception ex)
  { m_conn = null;
    System.out.println(ex.toString() + " (" + myName + "/close)");
//    ex.printStackTrace(System.out);
//    throw new Exception(ex.toString() + " (" + myName + "/close)");
  }
}


/**
 *
     * @param sql
     * @return 
     * @throws java.lang.Exception
 */
public int update(String sql) throws Exception
{
//  System.out.println("------- update: " + sql);
  try
  {
//    if (busy) System.out.println("Waiting...");
/**/
    int i=0;
/*    while (busy)
    {
//      System.out.println("Waiting..." + i);
      Thread.yield();  Thread.sleep(1000);
      if (++i == 5)
      {
        System.out.println(" ======== " + myName + " - problem to execute 'UPDATE'! Try anyway...");
        busy=false;
      }
    }
    busy=true;
/**/   //***** 01.04.06
//   if (db != DB_MySQL)
   {
    if (sql.equalsIgnoreCase("COMMIT"))
    { m_conn.commit();
      return 1;
    }
    if (sql.equalsIgnoreCase("ROLLBACK"))
    { m_conn.rollback();
      return -1;
    }
   }
/**/    
// System.out.println(" ======== " + myName + " - 'UPDATE' ... db=" + db);
    t0 = System.currentTimeMillis();
//    System.out.println(" === Executing...");
//          stmt = conn.createStatement();
    curr_stmt = m_conn.createStatement();
    int res = curr_stmt.executeUpdate(sql);
//    if (db == DB_MySQL)
//      m_conn.commit();
    curr_stmt.close();
    t0=System.currentTimeMillis() - t0;
//    System.out.println(" === Done in " + t0 + " ms.");
    timeSpent = StrUtil.formatDouble(t0, 0, " ");
    timeSQL  += "<br>  Update SQL " + numQueries + " processed in " +timeSpent+"ms.<br>";

//    busy=false;
    return res;
  }
  catch (Exception ex)
  { //busy=false;
//    System.out.println("+++++++ " + myName + ": " + ex.toString() + ". SQL:" + sql);
//    ex.printStackTrace(System.out);
    throw new Exception(ex.toString() + ". SQL:"+ sql + ";"); // (" + myName + "/update)");
  }
}

/**
 *
     * @param resultSet
     * @return 
     * @throws java.lang.Exception
 */
public static int getNumCols(ResultSet resultSet)throws Exception
{
  if (resultSet == null) return 0;
  try
  { ResultSetMetaData metaData = resultSet.getMetaData();
    return metaData.getColumnCount();
  }
  catch (Exception ex)
  { throw new Exception(ex.toString() + " (in DBUtil/getNumCols)");
  }
}

/**
 *
     * @param resultSet
     * @return 
     * @throws java.lang.Exception
 */
public static String[] getColNames(ResultSet resultSet) throws Exception
{
  if (resultSet == null) return null;
  try
  { ResultSetMetaData metaData = resultSet.getMetaData();
    int numCols=metaData.getColumnCount();
    String[] sqlColNames = new String[numCols];
    for (int i=0;i<numCols; i++)
		{
//      sqlColNames[i]=metaData.getColumnName(i+1);
		  sqlColNames[i]=metaData.getColumnLabel(i+1);	  
//			System.out.println("+ col." + i + "; ColumnName='" + sqlColNames[i] + "'");
		}
    metaData=null;
    return sqlColNames;
  }
  catch (Exception ex)
  { throw new Exception(ex.toString() + " (in DBUtil.getColNames)");
  }
}

/**
 *
     * @throws java.lang.Exception
 */
public synchronized void commit() throws Exception
{ try { m_conn.commit();	}
  catch (Exception ex)
  { throw new Exception(queryLabel+ ex.toString() + "/ " + myName + "/commit()");
  }
  Thread.sleep(100);  // delay for Access - to finish updating
}

}