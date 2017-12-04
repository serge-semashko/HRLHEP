package cern.kpi.viewdb;

import java.sql.*;
import java.util.StringTokenizer;
import java.io.PrintWriter;

import dubna.walt.util.*;
//import oracle.jdbc.pool.*;

/**
 *
 */
public class QueryThread extends dubna.walt.SimpleQueryThread
{

protected DBUtil dbUtil = null;

public void init(ResourceManager rm) throws Exception
{ super.init(rm);
//  Class.forName(rm.getString("dbDriver"));        // init the JDBC driver
  rm.setObject("QueryThread", this);
}


public void startService() throws Exception
{ 
  cfgTuner.addParameter("now", dubna.walt.util.Fmt.lsDateStr( new java.util.Date())); 

  String c = cfgTuner.getParameter("c");  
  if (c.indexOf("repos_") == 0 )
  { super.startService();
    return;
  }

  dbUtil = connectToRepository(rm);
  if (dbUtil != null)
    rm.setObject("DBUtil", dbUtil, false);
  
  if (c.indexOf("admin") == 0  
   || c.indexOf("about") == 0
   || c.indexOf("login") == 0)
  { super.startService();
    return;
  }
  
 
  /* database name from the query */
  String db = cfgTuner.getParameter("db");
  if (db.length() < 2) db = cfgTuner.getParameter("q_ViewDB_db");
  if (db.length() < 2) db = cfgTuner.getParameter("db~s");
  if (db.length() < 2) db = rm.getString("conn_str_repos");

  /* obtain DBUtil for the "db" */
  if( cfgTuner.enabledOption("repos_connected"))
    dbUtil = makeDBUtil(db, cfgTuner, rm);
    
    /* could not connect - take the repository connection */
  if (dbUtil == null)
  { dbUtil = connectToRepository(rm);
    if (dbUtil != null)
    { db = rm.getString("conn_str_repos");
      cfgTuner.addParameter("db", db);
      dbUtil = makeDBUtil(db, cfgTuner, rm);
      if (!cfgTuner.enabledOption("new_db"))
      { outWriter.println("<center><table border=1 bgcolor=white cellspacing=0 cellpadding=8 width=70%><tr><th>"
            + "Connection has been set to the repository database <b>'" + db
            + "'.</th></tr></table><p></center>");
        outWriter.flush();
      }
      if (cfgTuner.enabledOption("AUTH_ERROR"))
      { cfgTuner.getCustomSection("headers.dat", "Header_HTML", outWriter);
        System.out.println("*****" + cfgTuner.getParameter("AUTH_ERROR") + ":" + cfgTuner.enabledOption("AUTH_ERROR"));
      }
      else   
      { getOraVersion(db, dbUtil);
        super.startService();
      }
    }
    else  // could not connect anyway - redirect to custom repository login
    { redirectToCustomLogin(rm);
    }
    if (cfgTuner.enabledOption("debug=on")) 
      logAll();
    return;
  }
  
  /*  successfully connected  */
//  System.out.println("=====" + cfgTuner.getParameter("AUTH_ERROR") + ":" + cfgTuner.enabledOption("AUTH_ERROR"));
  if (cfgTuner.enabledExpression("AUTH_ERROR&!c=about"))
  { cfgTuner.getCustomSection("headers.dat", "Header_HTML", outWriter);
    if (cfgTuner.enabledOption("debug=on")) 
      logAll();
  }
  else   
  { getOraVersion(db, dbUtil);
    super.startService();
    if (cfgTuner.enabledOption("debug=on")) 
      logAll();
  }
}

public static void redirectToCustomLogin(ResourceManager rm)
{  Tuner cfgTuner = (Tuner) rm.getObject("cfgTuner");
   if (cfgTuner == null) return;
   if (cfgTuner.enabledOption("c=repos_login")) return;
   PrintWriter outWriter = (PrintWriter) rm.getObject("outWriter");
   System.out.println("***** Could not connect to Repository - redirect to custom login");
   String url = cfgTuner.getParameter("ServletRoot") 
     + cfgTuner.getParameter("ServletPath") + "?c=repos_login&tm=" + Long.toString(System.currentTimeMillis());
   outWriter.println("<script>window.location.href='" + url + "'</script>");
   outWriter.flush();
   outWriter.close();
}

public static DBUtil connectToRepository(ResourceManager rm)
{ 
  String use_repos = rm.getString("use_repos", false);
  System.out.println();
//  System.out.println(".................... connectToRepository ............" + use_repos);
  rm.removeKey("use_repos");
  
  DBUtil dbUtil = null;
  
  try
  { //=== direct connect as required by parameter "use_repos"
    if (use_repos.length() > 3)
    { dbUtil = connectToRepository(use_repos, rm);
    }

    if (dbUtil == null || !dbUtil.isAlive())
    { //=== try to get DBUtil from ResorceManager
      dbUtil = (DBUtil) rm.getObject("DBUtil_Repos", false);
      if (dbUtil != null && dbUtil.isAlive())
      { rm.setParam("repos_connected", "Y");
        return dbUtil;
      }
      rm.removeKey("DBUtil_Repos");
      //=== try to connect using DataSource
      if (getReposURLs(rm))
      { dbUtil = connectToRepositoryDataSource(rm.getString("main_repos"), rm);
        if (dbUtil == null )
          dbUtil = connectToRepositoryDataSource(rm.getString("secondary_repos"), rm);
      }
      else //=== try to connect using connection string from the .properties file
      { String conn_str_repos =  rm.getString("repository");
        if (conn_str_repos.length() > 1)
          dbUtil = connectToRepository(conn_str_repos, rm);
        if (dbUtil == null)
        { conn_str_repos =  rm.getString("repository_2");
          if (conn_str_repos.length() > 1)
            dbUtil = connectToRepository(conn_str_repos, rm);
        }
        if (dbUtil == null) //=== NO success - go to Custom Login to the repository
        { redirectToCustomLogin(rm);
          return null;
        }
      }
    }
    rm.setObject("DBUtil_Repos", dbUtil, true);
    rm.setParam("repos_connected", "Y");
    getDbList(dbUtil, rm);               
    return dbUtil;    
  }
  catch (Exception e)
  { System.out.println(e.toString());
    e.printStackTrace(System.out);
    redirectToCustomLogin(rm);
    return null;
  }
}

public static DBUtil connectToRepository(String conn_str, ResourceManager rm) throws Exception
{ DBUtil dbUtil = null;
//System.out.println("+++++++ try to connect:" + conn_str_repos);
  try
  { dbUtil = (new DBUtil(rm.getString("driverType", true) + conn_str
             , rm.getString("usr")
             , rm.getString("pwd")
             , rm.getString("queryLabel", false) + "_Repos"
             , 1));
    rm.setParam("db", conn_str); 
    rm.setParam("conn_str_repos", conn_str, true); 
    rm.setObject("DBUtil_" + conn_str, dbUtil, true);
    return dbUtil;
  }
  catch (Exception e)
  { e.printStackTrace(System.out);
    return null;
  }
//        System.out.println ("++++++++ " + conn_str_repos + " - connected" );
}

public static boolean getReposURLs(ResourceManager rm)
{ String dsName = rm.getString("main_repos", false);
  if (dsName.length() < 1) return false;
  try
  { javax.naming.InitialContext ic = new javax.naming.InitialContext();
    oracle.jdbc.pool.OracleDataSource dataSource = (oracle.jdbc.pool.OracleDataSource)ic.lookup(dsName);
    String conn_str = dataSource.getURL();
    conn_str = conn_str.substring (conn_str.indexOf("@") + 1);
    rm.setParam(dsName, conn_str, true);
    rm.setParam("repository", conn_str, true);

    dsName = rm.getString("secondary_repos");
    dataSource = (oracle.jdbc.pool.OracleDataSource)ic.lookup(dsName);
    conn_str = dataSource.getURL();
    conn_str = conn_str.substring (conn_str.indexOf("@") + 1);
    System.out.println ("++++++++ " + dsName + ":" + conn_str + "/" + dataSource.getUser());
    rm.setParam(dsName, conn_str, true);
    rm.setParam("repository_2", conn_str, true);
    return true;
  } catch (Exception e)
  { System.out.println("+++++++++++++++++ Could not get ReposURL: " + dsName);
    System.out.println(e.toString());
    return false;
  }
}

public static DBUtil connectToRepositoryDataSource(String dsName, ResourceManager rm)
{ DBUtil dbUtil = null;
  try
  { javax.naming.InitialContext ic = new javax.naming.InitialContext();
    oracle.jdbc.pool.OracleDataSource dataSource = (oracle.jdbc.pool.OracleDataSource)ic.lookup(dsName);
    String conn_str = dataSource.getURL();
    conn_str = conn_str.substring (conn_str.indexOf("@") + 1);
//    String conn_str = "missr:1524:ARS";

    java.sql.Connection conn = dataSource.getConnection(); 
    dbUtil = new DBUtil(conn, rm.getString("queryLabel",false) + "_" + conn_str);

    rm.setParam("db", conn_str); 
    rm.setParam("conn_str_repos", conn_str, true); 
    rm.setObject("DBUtil_" + conn_str, dbUtil, true);      
    System.out.println ("++++++++ " + dsName + ":" + conn_str + " - connected" );
    return dbUtil; 
  }
  catch (Exception e)
  { System.out.println("+++++++++++++++++ Could not connect to DataSource " + dsName);
    System.out.println(e.toString());
    return null;
  }
}


public static boolean getDbList(DBUtil dbu, ResourceManager rm)
{ 
  String cmd = rm.getString("getIntancesCmd", false);
  if (cmd.length() > 1)
  { try
    { String s = dubna.walt.service.Service.executeCommand(cmd);
      String[] dbs = StrUtil.splitStr(s,'\n');
//        IOUtil.writeLog("INSTANCES:", dbs, rm);
      if (dbs.length > 1)
      { String connStr = "";
        String db = "";
        String val = "";
        System.out.println(s);
        dbu.update("update instances set CONN_STR='.',	SERVICE='.' where TYPE=1");
        
        for (int i=0; i < dbs.length; i++)
        { String[] tokens = StrUtil.splitStr(dbs[i], ',');
          if (tokens.length >= 4)
          { if (tokens[2].length() == 0) tokens[2] = "1521";  // def. port number
            db =  tokens[3] + tokens[4];
            val = tokens[1] +  "/" + tokens[0] + " (" + tokens[3] + ")";
            connStr = tokens[0] +  ":" + tokens[2] + ":" + tokens[1];
            int num = dbu.update ("update instances set CONN_STR='" + connStr
                + "', SERVICE='" + val 
                + "', TYPE=1 where INSTID='" + tokens[3] + "'");
            if (num == 0)
              dbu.update ("insert into instances (INSTID, CONN_STR, SERVICE, TYPE, PUBLIC_ACCESS) values('"
                + tokens[3] + "','" + connStr + "','" + val + "', 1, 0)");
          }
        }
        dbu.update("delete from instances where CONN_STR='.' and	SERVICE='.'");

//        String rep = rm.getString(rm.getString("main_repos"), false) + "','"
//                   + rm.getString(rm.getString("secondary_repos"), false);
//        dbu.update ("update instances set TYPE='repository' where CONN_STR in ('" + rep + "')");
      }
    }
    catch (Exception e)
    { System.out.println(e.toString());
      e.printStackTrace(System.out);
      rm.setParam("DB_LIST_ERROR",e.toString(), true);
    }
  }
  
  try 
  { String dbList = "";
    ResultSet r = dbu.getResults("SELECT '<option value='''||CONN_STR||'''>'||service as DBLIST FROM INSTANCES ORDER BY type, service");
    while (r.next())
      dbList += r.getString(1);
    dbu.closeResultSet(r);
    rm.setParam("dbList", dbList, true);
  } catch (Exception ex)
  { ex.printStackTrace(System.out);
    return false;
  }
//      System.out.println("2:" + dbList);
  return true;
}

public static DBUtil makeDBUtil(String db, Tuner cfgTuner, ResourceManager rm)
{ if (db.length() < 2)
    return null;
  DBUtil dbUtil = null;
//  System.out.println(queryLabel + " - try to get DBUtil...");
  try
  { StringTokenizer st = new StringTokenizer(db, ":");
    cfgTuner.addParameter("host", st.nextToken());
    cfgTuner.addParameter("port", st.nextToken());
    cfgTuner.addParameter("instance", st.nextToken());
  } catch (Exception e) {}

  try
  { dbUtil = (DBUtil) rm.getObject("DBUtil_" + db, false);
    if (dbUtil != null)
    { dbUtil = dbUtil.cloneDBUtil(rm.getString("queryLabel"));
      rm.setObject("DBUtil", dbUtil, false);
//        System.out.println(queryLabel + " - Got " + dbUtil.myName);
      cfgTuner.addParameter("connected_to", db);
      cfgTuner.addParameter("current_user", dbUtil.usr);
      return dbUtil;
/*    if (dbUtil.isAlive())
      { rm.setObject("DBUtil", dbUtil, false);
        dbUtil.allocate();
      }
*/        
    }
  }  catch (Exception e) { /* Ignore and just try to connect again */ }

   /* try to establish the connection to the DB */
  try
  { String connStr = db;
    if (db.indexOf("~s") > 0) connStr = db.substring(0, db.indexOf("~s"));
//    System.out.println("**************** '" + db + "' >>> '" + connStr + "'");
//    System.out.println("***** Connecting '" + connStr + "'(" 
//      + cfgTuner.getParameter("usr") + "/" + cfgTuner.getParameter("pwd") + ")" );
    dbUtil = (new DBUtil(rm.getString("driverType", true) + connStr
                      , cfgTuner.getParameter("usr")
                      , cfgTuner.getParameter("pwd")
                      , rm.getString("queryLabel",false) + " " + db, 1));
    dbUtil.nrConnsToKeep = 1;
    dbUtil.allocate();
  }
  catch (Exception e)
  { if (e.getMessage().indexOf("ORA-01017") >= 0)
    { cfgTuner.addParameter("not configured", "YES");
    }
    cfgTuner.addParameter("LOGIN_ERROR", e.getMessage());
    System.out.println("Connection to " + db + " FAILED!..." + e.getMessage());
    if (!cfgTuner.enabledOption("new_db"))
    { PrintWriter outWriter = (PrintWriter) rm.getObject("outWriter");
      outWriter.println("<center><table border=1 bgcolor=white cellspacing=0 cellpadding=8 width=70%><tr><td>"
          + "<b><center>Could not connect to the Database '" + db
          + "' !</b><br>" 
          + rm.getString("conn_str_" + db , false)
          + "<br><small>" +  e.getMessage() + "</small>"
          + "</th></tr></table></center>");
      outWriter.flush();
      System.out.println(e.toString());
    }
    return null;   
  }
  rm.setObject("DBUtil", dbUtil, false);
  rm.setObject("DBUtil_" + db, dbUtil, true);
  cfgTuner.addParameter("connected_to", db);
  cfgTuner.addParameter("current_user", dbUtil.usr);
  return dbUtil;
}

protected static String getP()
{ return "version20";
  
}

private void getOraVersion(String db, DBUtil dbUtil)
{ if (cfgTuner.enabledOption("NotConnected")
      || dbUtil == null)
    return;
    
  String s = "Not Defined";
  ResultSet r = null;
  try
  { if (cfgTuner.enabledOption("OraVersion_" + db))
    {  s = cfgTuner.getParameter("OraVersion_" + db);
    }
    else
    { String sql="SELECT SUBSTR(banner,INSTR(LOWER(banner),'release')+8,9) FROM v$version WHERE LOWER(banner) LIKE '%oracle%'";
      r = dbUtil.getResults(sql);
      if (r.next())
        s = r.getString(1);
    }
  }
  catch (Exception e)
  { s = "Not Defined: " + e.toString();
  }
  finally
  { dbUtil.closeResultSet(r);
    rm.setParam("OraVersion_" + db, s, true);
    cfgTuner.addParameter("OraVersion", s);
    cfgTuner.addParameter("OraVer1", s.substring(0,1));
    cfgTuner.addParameter("OraVer2", s.substring(0,3));
    cfgTuner.addParameter("OraVer3", s.substring(0,5));
  }
}


protected void finish()
{ if (dbUtil != null)
    dbUtil.release();
  dbUtil = null;
//  System.out.println("---------------- finish() " + queryLabel);
}

protected void finalize()
{ finish();
}

}