package cern.kpi.viewdb;

import java.sql.*;
import java.util.StringTokenizer;
import java.io.PrintWriter;
import javax.servlet.http.HttpServletRequest;
import dubna.walt.util.*;

public class UserValidator extends dubna.walt.util.UserValidator
{

protected Tuner cfgTuner = null;
protected DBUtil dbRepos = null;
protected Connection conn = null;
protected static long SESS_TIMEOUT_MIN=600;

public boolean validate(ResourceManager rm) throws Exception
{ cfgTuner = (Tuner) rm.getObject("cfgTuner");
  conn = getRepositoryConnection(rm);
  if (conn == null) 
  { setDBA();
    return true;
  }
  HttpServletRequest request = (HttpServletRequest) rm.getObject("request");

  String loginName = "";
  String userHRID = "";
  cfgTuner.deleteParameter("DBA");

  //========= Validate user - get user's login name =========
  if (rm.getBoolean("UseCommonLogin"))  // via common login
  { userHRID = getCookieValue(request, "AI_HRID");
    loginName = getCookieValue(request, "AI_USERNAME").toUpperCase();    
    if (rm.getBoolean("local") && loginName.length() == 0)
    { userHRID="406151";
      loginName="KOUNIAEV";
//      loginName="IMICHTCH";
    }
    cfgTuner.addParameter("userHRID", userHRID);
    cfgTuner.addParameter("loginName", loginName);
  }
  
  else if (rm.getBoolean("UseViewdbLogin"))  // via ViewDB login
  { if (validateViewDBUser(rm))
      loginName = cfgTuner.getParameter("loginName");
  }
  
  else if (rm.getBoolean("UseStandardAuthorization"))   // via standard web server authorization
  { System.out.println("UseStandardAuthorization...");
    if (!standardAuthorization())
      return checkSetup();
    loginName = cfgTuner.getParameter("loginName");
  }

  //======= check direct Oracle user access =======
  if (rm.getBoolean("AllowDirectOraUserAccess"))
  { //System.out.println("AllowDirectOraUserAccess...");
    if (validateDirectUser(rm))
      return true;
  }

  System.out.println(rm.getString("queryLabel", false) 
      + " [" + Fmt.lsDateStr( new java.util.Date() ) + "] " 
      + loginName // + "(" + cfgTuner.getParameter("ClientIP") + ")"
      + " c=" + cfgTuner.getParameter("c")
      + " db="+ cfgTuner.getParameter("db"));

  if (loginName.length() <2 )  //user not validated
  { checkSetup();
    if (cfgTuner.enabledOption("DBA"))
      return true;
    if (rm.getBoolean("UseCommonLogin"))
      redirectToCommonLogin(rm);
    System.out.println("NOT logged..." + cfgTuner.enabledExpression("c=login&UseViewdbLogin=true"));
    if (!cfgTuner.enabledExpression("c=login&UseViewdbLogin=true"))
      cfgTuner.addParameter("AUTH_ERROR", "Not Authenticated");
     return true;
  }
  
  //======= get the ViewDB user's info and access rights =======
  //===== Get the user name, email, and test DBA privileges
//  long tm = System.currentTimeMillis();
  String sql = "SELECT u.name, u.email, ug.GROUPID FROM users u, user_to_group ug where u.LOGINNAME=? and u.userid=ug.userid";
  PreparedStatement pstmt = conn.prepareStatement(sql);
  pstmt.setString(1, loginName);
  ResultSet r = pstmt.executeQuery();
  while (r.next()) 
  { cfgTuner.addParameter("userName", r.getString(1));
    cfgTuner.addParameter("email", r.getString(2));
    if (r.getInt(3) == 1) // user is a member of DBA group
    { setDBA();
      getCurrentDB();
    }
//    System.out.println("..." +  r.getString(1) + " : " +  r.getString(2) + " : "+  r.getString(3) + ";" );
  }
   r.close();  pstmt.close();

  if (!cfgTuner.enabledOption("DBA=Y"))
  { //===== Get list of instances for the current user
    checkSetup();
    if (cfgTuner.enabledOption("DBA"))
      return true;
    cfgTuner.addParameter("dbList", "<option>=== NO ACCESS ===");
    sql = "SELECT UNIQUE to_char(type)||service, '<option value='''||CONN_STR||'''>'||service FROM accrights where LOGINNAME =? ORDER BY 1";
    pstmt = conn.prepareStatement(sql);
    pstmt.setString(1, loginName);
    r = pstmt.executeQuery();
    String dbList = "";
    while (r.next()) 
      dbList += r.getString(2);
    r.close(); pstmt.close();
    if (dbList.length() < 1) 
    { cfgTuner.addParameter("AUTH_ERROR", "NO ACCESS");
      return true;
    }
    cfgTuner.addParameter("dbList", dbList);

    //===== Get the user's privileges on the current db
    sql = "SELECT ACC_RIGHTS||',', GROUPNAME||',' FROM accrights where LOGINNAME=? and CONN_STR=?";
    pstmt = conn.prepareStatement(sql);
    pstmt.setString(1, loginName);
    pstmt.setString(2, getCurrentDB());
    r = pstmt.executeQuery();
    String accRights = "";
    if (cfgTuner.getParameter("c").indexOf("admin")==0)
      accRights = "user,";
    String groups="";
    while (r.next()) 
    { accRights += r.getString(1);
      groups += r.getString(2);
    }
    r.close();   pstmt.close();
    cfgTuner.addParameter("groups", groups);
    cfgTuner.addParameter("ACC_RIGHTS", accRights);

    if (accRights.length() < 1)   
    { cfgTuner.addParameter("AUTH_ERROR", "NO ACCESS TO INSTANCE");
      return true;
    }
    StringTokenizer st = new StringTokenizer(accRights, ",");
    while (st.hasMoreTokens())
      cfgTuner.addParameter(st.nextToken(), "ACC_RIGHTS");
  }

  //===== update the number of user's hits
  try
  { pstmt = conn.prepareStatement("update users set hits=nvl(hits,0)+1, lasthit=SYSDATE where LOGINNAME=?");
    pstmt.setString(1, loginName);
    pstmt.executeUpdate();
    pstmt.close();
  } catch (Exception e) { /*we don't care too much */ }

  cfgTuner.addParameter("logged", "YES"); 
//  System.out.println("+++++ time spent:" + (System.currentTimeMillis() - tm) + "ms");

  return true;
}

private boolean standardAuthorization()
{ String auth = cfgTuner.getParameter("h_AUTHORIZATION");
  cfgTuner.deleteParameter("AUTH_ERROR");
  if (auth.length() < 1) 
    cfgTuner.addParameter("AUTH_ERROR", "NOT LOGGED");
  else if (auth.indexOf("Basic ") != 0)
    cfgTuner.addParameter("AUTH_ERROR", "Unknown Authorisation method: " + auth);
  else
  { try
    { String loginName = new String(Base64.decode(auth.substring(6)));
      int i = loginName.indexOf(":");
      if (i <= 0)
        cfgTuner.addParameter("AUTH_ERROR", "Unknown Authorisation method: '" + auth + "'; decoded: '" +  loginName + "'");        
      else
        loginName = loginName.substring(0,i).toUpperCase();
      cfgTuner.addParameter("loginName", loginName);
    }
    catch (Exception e)
    { cfgTuner.addParameter("AUTH_ERROR", "Authorisation failed: '" + auth + "'");
    }
  }
  return (!cfgTuner.enabledOption("AUTH_ERROR"));
}

private boolean validateDirectUser(ResourceManager rm)
{ 
  HttpServletRequest request = (HttpServletRequest) rm.getObject("request");
  String sessId = getCookieValue(request, "ViewDB_Sess");
  if (sessId.length() < 10) return false;
  
  // ===== Check if session id is valid
  String directOraUser = rm.getString(sessId + "_user", false);
  cfgTuner.addParameter("DirectOraUser", directOraUser);
//  System.out.println("+++++ validateDirectUser: sessId = " + sessId + "; DirectOraUser = " + directOraUser );
  if (directOraUser.length() < 1)   
  { // cfgTuner.addParameter("AUTH_ERROR", "USER NOT LOGGED");
    return false;
  }
  //===== Check the session timout
  long tm = 0;
  try { tm = Long.parseLong(rm.getString(sessId + "_tm", false)); }
  catch (Exception e) { tm = 0; }
  long tm2 = System.currentTimeMillis();
//  System.out.println("+++++ Inactive:" + Long.toString((tm2 - tm)/1000) + " sec.");

  if (tm2 - tm > 1000*60*SESS_TIMEOUT_MIN)
  { cfgTuner.addParameter("SessionTimeout", "Y");
    rm.removeKey(sessId + "_user");
    rm.removeKey(sessId + "_pw");
    rm.removeKey(sessId + "_db");
    rm.removeKey(sessId + "_tm");
    cfgTuner.addParameter("dbList","<option>=== No Access ===");
    cfgTuner.addParameter("AUTH_ERROR", "SESSION TIMEOUT");
    return true;
  }
  //===== prolongate this session
  rm.setParam(sessId + "_tm", Long.toString(tm2), true);
  String loginName = cfgTuner.getParameter("loginName");
  
  //===== set session parameters
  String db = rm.getString(sessId + "_db", false);
  cfgTuner.addParameter("db", db);
  cfgTuner.addParameter("sessId", sessId);
  cfgTuner.addParameter("pwcustom", rm.getString(sessId + "_pw", false));
  cfgTuner.addParameter("logged", "YES");   
  cfgTuner.addParameter("userName",  "direct ORACLE user " + directOraUser);
  cfgTuner.addParameter("loginName",  loginName);
  cfgTuner.addParameter("ACC_RIGHTS", "direct ORACLE user");

  //===== protect hacking ======
  cfgTuner.addParameter("orauser", directOraUser);
  cfgTuner.addParameter("object_owner", directOraUser);

  //===== get SERVICE name
  PreparedStatement pstmt = null;
  try
  { pstmt = conn.prepareStatement("select SERVICE from INSTANCES where CONN_STR=?");
    pstmt.setString(1, db);
    ResultSet r = pstmt.executeQuery();
    if (r.next()) 
      cfgTuner.addParameter("SERVICE", r.getString(1));   
    r.close();   pstmt.close();
  }
  catch (Exception e) 
  { cfgTuner.addParameter("SERVICE", "UNKNOWN" + e.toString());
    e.printStackTrace(System.out);
  }

  //===== Register the hit
  int n = 0;
  try
  { pstmt = conn.prepareStatement("update ora_users set hits=hits+1, lasthit=SYSDATE  where sessid=?");
    pstmt.setString(1, sessId);
    n = pstmt.executeUpdate();
    pstmt.close();
  }
  catch (Exception e) 
  { e.printStackTrace(System.out);
  }

  if (n == 0)
  { try
    { dbRepos.update("insert into ora_users (SESSID, LOGINNAME, DB, ORA_USER, HITS, login_time, LASTHIT) values ('"
        + sessId + "','" + loginName + "','" + db + "','" + directOraUser + "', 1, SYSDATE, SYSDATE)" );
    }
    catch (Exception e) 
    { e.printStackTrace(System.out);
    }
  }

  System.out.println(rm.getString("queryLabel", false) 
      + " [" + Fmt.lsDateStr( new java.util.Date() ) + "] " 
      + directOraUser // + "(" + cfgTuner.getParameter("ClientIP") + ")"
      + " on "+ db + "; c=" + cfgTuner.getParameter("c"));
  
  return true;
}

private boolean validateViewDBUser(ResourceManager rm)
{ 
  HttpServletRequest request = (HttpServletRequest) rm.getObject("request");
  String sessId = getCookieValue(request, "VViewDB_Sess");
//  System.out.println("+++++ validateDirectUser: sessId = '" + sessId + "';");
  if (sessId.length() < 10) return false;
  
  // ===== Check if session id is valid
  String viewDBUser = rm.getString(sessId + "_user", false);
//  System.out.println("+++++ validateDirectUser: sessId = " + sessId + "; viewDBUser = " + viewDBUser );
  if (viewDBUser.length() < 1)   
  { cfgTuner.addParameter("AUTH_ERROR", "USER NOT LOGGED");
    return false;
  }
  //===== Check the session timeout
  long tm = 0;
  try { tm = Long.parseLong(rm.getString(sessId + "_tm", false)); }
  catch (Exception e) { tm = 0; }
  long tm2 = System.currentTimeMillis();
//  System.out.println("+++++ Inactive:" + Long.toString((tm2 - tm)/1000) + " sec.");

  if (tm2 - tm > 1000*60*SESS_TIMEOUT_MIN)
  { cfgTuner.addParameter("SessionTimeout", "Y");
    rm.removeKey(sessId + "_user");
    rm.removeKey(sessId + "_ip");
    rm.removeKey(sessId + "_tm");
    cfgTuner.addParameter("AUTH_ERROR", "SESSION TIMEOUT");
    return true;
  }
  //===== prolongate this session
  rm.setParam(sessId + "_tm", Long.toString(tm2), true);
  
  //===== set session parameters
  String db = rm.getString(sessId + "_db", false);
  cfgTuner.addParameter("vsessId", sessId);
  cfgTuner.addParameter("logged", "YES");   
  cfgTuner.addParameter("loginName",  viewDBUser);
  cfgTuner.addParameter("ViewDBUser", viewDBUser);

  return true;
}

private boolean checkSetup()
{ int numUsers = 0;
  try
  { PreparedStatement pstmt = conn.prepareStatement("SELECT count(*) FROM USER_TO_GROUP where GROUPID=1");
    ResultSet r = pstmt.executeQuery();
    if (r.next())
      numUsers = r.getInt(1);
    r.close();
    pstmt.close(); 
  }
  catch (Exception e)
  { System.out.println("UserValidator: Repository corrupted: " + e.toString());
    cfgTuner.addParameter("REPOS_CORRUPTED", "Y");
  }
  if (numUsers == 0)
  { setDBA();
    cfgTuner.addParameter("NODBA", "Y");
  }
  return true;
}

private void setDBA()
{ cfgTuner.addParameter("DBA", "Y");
  cfgTuner.addParameter("ACC_RIGHTS", "admin");
  cfgTuner.addParameter("admin", "ACC_RIGHTS");
  cfgTuner.deleteParameter("AUTH_ERROR");
  cfgTuner.addParameter("logged", "YES"); 
  getCurrentDB();
}

private void redirectToCommonLogin(ResourceManager rm)
{ PrintWriter outWriter = (PrintWriter) rm.getObject("outWriter");
  outWriter.println("ViewDB USER Not Autenticated!");
  if (rm.getBoolean("UseCommonLogin"))
    outWriter.println("<script>window.location.href='" + rm.getString("CommonLoginURL") + "'</script>");
}

private String getCurrentDB()
{ //===== Get the current db
    String db = cfgTuner.getParameter("db");
    String dbList = cfgTuner.getParameter("dbList");
//    System.out.println("....... dbList=" + dbList);
    if (db.length() < 2)
    { db = cfgTuner.getParameter("db~s");
      if (db.endsWith("~s")) db = db.substring(0, db.length() -2);
    }
//    System.out.println("....... db=" + db);
    if (db.length() < 2) 
    { db = cfgTuner.getParameter("q_ViewDB_db");
//      System.out.println("++++++ q_ViewDB_db=" + db);
      if (db.length() < 2) 
      { if (dbList.length() > 1)
        { int i = dbList.indexOf("'");
          db = dbList.substring (i+1, dbList.indexOf("'", i+1));
        }
        else
          db = cfgTuner.getParameter("repository");
      }      
      cfgTuner.addParameter("db", db);
    }
    if (dbList.length() > 1 && dbList.indexOf(db) < 0)
    { int i = dbList.indexOf("'");
      if (i > 0)
        db = dbList.substring (i+1, dbList.indexOf("'", i+1));
      cfgTuner.addParameter("db", db);
    }
//    System.out.println("******* db="+ db + "'");
    return db;
}

private Connection getRepositoryConnection(ResourceManager rm)
{ dbRepos = (DBUtil) rm.getObject("DBUtil_Repos");
  if (dbRepos == null || !dbRepos.isAlive()) 
  { System.out.println("UserValidator: Could not get repository connection!");
    return null;
  }
  return dbRepos.getConnection();
}

}