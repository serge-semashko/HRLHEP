package jinr.cwl;

import dubna.walt.util.DBUtil;
import dubna.walt.util.IOUtil;
import dubna.walt.util.ResourceManager;
import dubna.walt.util.StrUtil;
import dubna.walt.util.Tuner;
import javax.servlet.http.*;
import java.util.StringTokenizer;
import java.sql.*;

public class UserValidator extends dubna.walt.util.UserValidator {

	/**
	 * @param rm
	 * @return
	 * @throws java.lang.Exception
	 */
	@Override
	public synchronized boolean validate(ResourceManager rm) throws Exception {
		Tuner cfgTuner = (Tuner) rm.getObject("cfgTuner");
		cfgTuner.deleteParameter("logged");
		String q_name = rm.getString("loginCookieName", false);
		if (q_name.length() < 2) {
			q_name = "cwl";
			rm.setParam("loginCookieName", q_name, true);
		}
		String q = cfgTuner.getParameter("q");
		if (q.length() < 3) {
			q = getCookieValue((HttpServletRequest) rm.getObject("request"), q_name);
		}
		IOUtil.writeLogLn(" ============= q=" + q, rm);
		q = StrUtil.unescape(q);
		IOUtil.writeLogLn(" =========== UserValidator.validate: " + q_name + "=" + q, rm);

//	System.out.println("validate1: dbUtilLogin:" + dbUtilLogin);
		makeLoginDBUtil(rm);
IOUtil.writeLogLn("validate2: dbUtilLogin:" + dbUtilLogin, rm);
		if (q.length() > 10) {
			try {
				StringTokenizer st = new StringTokenizer(q, ":");
				String fp0 = st.nextToken();
				String sess_id = st.nextToken();
				String user_id = st.nextToken();
				String login = st.nextToken();
				cfgTuner.addParameter("login", login);
				IOUtil.writeLogLn("----------- UserValidator sess_id=" + sess_id + "; user_id=" + user_id,rm );

				String checked_user_id = checkLogin(q, rm);
				IOUtil.writeLogLn("----------- UserValidator.validate: user_id=" + user_id, rm);

				if (checked_user_id.equals(user_id)) {
					cfgTuner.addParameter("logged", "YES");
					cfgTuner.addParameter("SESS_ID", sess_id);
					cfgTuner.addParameter("USER_ID", user_id);
//					cfgTuner.addParameter("FIO", usersTuner.getParameter(user_id, "FIO"));
//    System.out.println("----------- UserValidator.validate:" + user_name + ": logged");
					cfgTuner.deleteParameter("loginURL");
//				if (dbUtilLogin != null) //УБРАНО 01.06.2015 - сохранить dbUtilLogin для EDO UserValidator
//				{ dbUtilLogin.close();
//					dbUtilLogin = null;
//				}
					return true;
				} else {
					cfgTuner.deleteParameter("logged");
					cfgTuner.deleteParameter("SESS_ID");
					cfgTuner.deleteParameter("USER_ID");
					cfgTuner.deleteParameter("FIO");
//    System.out.println(user_name + ": NOT LOGGED");
				}
			} catch (Exception e) {
				System.out.println(" =========== UserValidator Exception:");
				e.printStackTrace(System.out);
			}
		}
		String c = cfgTuner.getParameter("c");
		boolean log = (c.indexOf("login") >= 0);
//  System.out.println(" =========== UserValidator log:" + log);
//	если логин не прошел - сбрасываем dbUtilLogin для обновления коннекта
		if (dbUtilLogin != null) {
			try {
				dbUtilLogin.close();
			} catch (Exception e) {;
			}
			dbUtilLogin = null;
		}
		return (log);
//  return true;
	}

	@Override
	public void makeUsersTuner(ResourceManager rm) //throws Exception
{ try {
	usersTuner = new Tuner(null, null, null, rm);
} catch (Exception e) {;}
}
	/*
	 private void makeUsersTuner(ResourceManager rm) //throws Exception
	 {
	 try {
	 System.out.println("..... WL: makeUsersTuner...");
	 Vector params = new Vector(200, 100);
	 //		String sql="select id, LOGINNAME, FIO from " + rm.getString("usr", true) + ".wu";
	 String sql = "select id, LOGINNAME, FIO from wl.wu";
	 //      + " where last > CDate('11.01.2002')";
	 IOUtil.writeLogLn("===== getUsers (SQL):" + sql, rm);

	 ResultSet r = dbUtilLogin.getResults(sql);
	 String[] columns = DBUtil.getColNames(r);
	 while (r.next()) {
	 params.addElement("[" + r.getString(1) + "]");
	 for (int i = 2; i <= columns.length; i++) {
	 String s = r.getString(i);
	 if (s != null && !s.equals("null")) {
	 params.addElement(columns[i - 1] + "=" + s);
	 }
	 //          params.addElement(columns[i-1] + "=" + StrUtil.unicode(s));
	 }
	 params.addElement("[end]");
	 }
	 dbUtilLogin.closeResultSet(r);
	 String[] param = new String[params.size()];
	 params.copyInto(param);
	 IOUtil.writeLog("===== getUsers (USERS):", param, rm);

	 usersTuner = new Tuner(null, null, null, rm);
	 usersTuner.addSection(params);

	 } catch (Exception e) {
	 e.printStackTrace(System.out);
	 }
	 }
	 /**/
}
