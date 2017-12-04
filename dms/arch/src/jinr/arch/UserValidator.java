package jinr.arch;

import java.io.PrintWriter;
import java.util.Vector;
import java.util.StringTokenizer;
import java.sql.ResultSet;
import javax.servlet.http.*;

import dubna.walt.util.*;
import java.sql.CallableStatement;
import java.sql.Connection;
import java.sql.DriverManager;

public class UserValidator extends dubna.walt.util.UserValidator
{

	private Tuner cfgTuner = null;

	/**
  *
  *
  */
	public synchronized boolean validate ( ResourceManager rm ) throws Exception
	{
		//  HttpServletRequest request = (HttpServletRequest) rm.getObject("request");
		//                        PrintWriter outWriter,
		long t = System.currentTimeMillis();
		cfgTuner = ( Tuner ) rm.getObject( "cfgTuner" );
		cfgTuner.deleteParameter( "logged" );
		cfgTuner.deleteParameter( "uname" );
		cfgTuner.deleteParameter( "FIO" );
		cfgTuner.deleteParameter( "LOGINNAME" );
		cfgTuner.deleteParameter( "ADMIN" );
	  cfgTuner.deleteParameter( "USR_LAB_CODE" );
	  cfgTuner.addParameter( "USER_ID", "-1" );
	  String c = cfgTuner.getParameter( "c" );
		boolean cnt = c.equals("doc/event_cnt")|c.equals("check");
		boolean logged = false;

		String qn = rm.getString( "loginCookieName", false );
		if( qn.length() < 2 )
			rm.setParam( "loginCookieName", "cwl", true );
		String q0 = cfgTuner.getParameter( "q_" + qn );
		if(!cnt) {
//		  System.out.println( " *");
//			System.out.print( "ARCH.validate:" + cfgTuner.getParameter("ClientIP") + ";c=" + c + ";"); // DB:" + dbUtilLogin);
		}
		//	System.out.println(" ============= q=" + q);
		String q = StrUtil.unescape( q0 );
//		System.out.println( " =========== NEW UserValidator.validate: " + qn + "=" + q );
		if( q.length() > 10 )
		{
			try
			{
				StringTokenizer st = new StringTokenizer( q, ":" );
				String fp0 = st.nextToken();

				String sql = "select u.id, u.person_id, u.login, u.sess_id, u.ip from users u join up on (up.id=u.person_id and up.sh='" + q0 + "') ";
				IOUtil.writeLogLn(5, "<b>+++ UserValidator: Executing SQL: </b>" + sql + "; q=" + q, rm );
				String user_id = "", person_id = "", login = "", sess_id = "", ip = "";

				makeLoginDBUtil( rm );
				ResultSet r = dbUtilLogin.getResults( sql );
				if( r.next() )
				{
					user_id = r.getString( 1 );
					person_id = r.getString( 2 );
					login = r.getString( 3 );
					sess_id = r.getString( 4 );
					ip = r.getString( 5 );
					if( !ip.equals( cfgTuner.getParameter( "ClientIP" ) ) && (!cfgTuner.getParameter( "ClientIP" ).equals("127.0.0.1")))
					{
						IOUtil.writeLogLn(5, "<hr><b>HACK: IP " + ip + " => " + cfgTuner.getParameter( "ClientIP" ) + "</b><hr>", rm );
						System.out.println( "********************************************** " );
						System.out.println( "*************** HACK: IP " + ip + " => " + cfgTuner.getParameter( "ClientIP" ) );
						return false;
					}
				}

//				System.out.println( "----------- UserValidator.validate: user_id=" + user_id );

				//      if (loginURL.equals("TRUE"))
				if( user_id.length() > 0 && (Integer.parseInt( user_id ) > 0 || Integer.parseInt( user_id ) < -1 ))
				{
					cfgTuner.addParameter( "logged", "YES" );
					cfgTuner.addParameter( "USER_ID", user_id );
					cfgTuner.addParameter( "LOGINNAME", login );
				  cfgTuner.addParameter( "uname", login );
					cfgTuner.addParameter( "SESS_ID", sess_id );
//					System.out.println( "----------- UserValidator.validate:" + login + ": logged" );
				  if( user_id.equals( "1" ))
					{
						if (cfgTuner.enabledOption( "q_VU" ) )
						{
							cfgTuner.addParameter( "ADMIN", "Y" );
							cfgTuner.addParameter( "UID", "1" );
							cfgTuner.addParameter("USER_ID", cfgTuner.getParameter( "q_VU"));
						}
					}
					else 
					  cfgTuner.deleteParameter( "debug" );
                                    IOUtil.writeLogLn(5, "<b>+++ UserValidator - OK:</b> user_id=" + user_id + "; login=" + login, rm );
//                                    if(!cnt)
//                                        System.out.println( " +U=" + user_id + ";");
                                    return true;
				}
//			  if(!cnt)
//				  System.out.println( " -U=" + user_id + ";");
			}

			catch( Exception e )
			{
				System.out.println( " =========== UserValidator Exception:" );
				e.printStackTrace( System.out );
			}
		}
//	  if(!cnt)
//		  System.out.println( " ARCH: NOT Logged!");

//========== Логин не прошёл ================
		//  сбрасываем dbUtilLogin для обновления коннекта
		if( !cnt && dbUtilLogin != null )
		{
			try	{dbUtilLogin.close();}
			catch( Exception e ){;}
			dbUtilLogin = null;
		}

//			t = System.currentTimeMillis() - t; // + queryNum 
//				System.out.println( ".....  validate " + ": " + cfgTuner.getParameter( "LOGINNAME" ) + "; spent: " + t + " ms." );

		return true;
	}


	public void makeLoginDBUtil ( ResourceManager rm ) throws Exception
	{
		if( cfgTuner.enabledExpression( "resetAR" ) )
			if( dbUtilLogin != null )
			{
				dbUtilLogin.close();
				dbUtilLogin = null;
			}
			
//    System.out.println("==== makeLoginDBUtil: dbUtilLogin:" + dbUtilLogin);
			
		if( dbUtilLogin == null || !dbUtilLogin.isAlive() )
		{
			Class.forName( rm.getString( "loginDriver" ) );
			String usr = rm.getString( "usrLogin", false, rm.getString( "usr", true ) );
			String pw = rm.getString( "pwLogin", false, rm.getString( "pw", false ) );
			String connStr = rm.getString( "connStringLogin", false, rm.getString( "connString", true ) );
//			System.out.print( "UserValidator: connecting... " + connStr + "/" + usr );
			long tm = System.currentTimeMillis();
			try
			{
				dbUtilLogin = new DBUtil( connStr, usr, pw, "CheckLogin", 1 );
				tm = System.currentTimeMillis() - tm;
//				System.out.println( " - OK " + tm + "ms" );
				dbUtilLogin.nrConnsToKeep = 0;
			}
			catch( Exception e )
			{
				dbUtilLogin = null;
				System.out.println( "=======  [" + Fmt.shortDateStr( new java.util.Date() ) + "] UserValidator.makeLoginDBUtil() - ERROR: " + e.toString() );
			}
//		  System.out.println("==== ARCH.UserValidator.makeLoginDBUtil(): " + dbUtilLogin);
		}
	}


}
