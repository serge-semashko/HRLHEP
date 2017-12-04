package jinr.gateway;

import java.util.StringTokenizer;
import javax.servlet.http.*;
import dubna.walt.util.*;

public class UserValidator extends dubna.walt.util.UserValidator {

    protected Tuner accRightsTuner = null;
    protected int queryNum = 0;

    /**
     * @param rm
     * @return
     * @throws java.lang.Exception
     */
    @Override
    public synchronized boolean validate(ResourceManager rm) throws Exception {
        Tuner cfgTuner = (Tuner) rm.getObject("cfgTuner");
        cfgTuner.deleteParameter("logged");
        cfgTuner.deleteParameter("VU");
        cfgTuner.deleteParameter("playMode");
        String q_name = rm.getString("loginCookieName", false);
        if (q_name.length() < 2) {
            q_name = "cwl";
            rm.setParam("loginCookieName", q_name, true);
        }
        String q = cfgTuner.getParameter("q");
        if (q.length() < 3) {
            q = getCookieValue((HttpServletRequest) rm.getObject("request"), q_name);
        }
//        IOUtil.writeLogLn(" ============= q=" + q, rm);
        q = StrUtil.unescape(q);
//        IOUtil.writeLogLn(" =========== UserValidator.validate: " + q_name + "=" + q, rm);

//	System.out.println("validate1: dbUtilLogin:" + dbUtilLogin);
        makeLoginDBUtil(rm);
//        IOUtil.writeLogLn("validate2: dbUtilLogin:" + dbUtilLogin, rm);
        if (q.length() > 10) {
            try {
                StringTokenizer st = new StringTokenizer(q, ":");
                String fp0 = st.nextToken();
                String sess_id = st.nextToken();
                String user_id = st.nextToken();
                String login = st.nextToken();
                cfgTuner.addParameter("login", login);
//                IOUtil.writeLogLn("----------- UserValidator sess_id=" + sess_id + "; user_id=" + user_id, rm);

                String checked_user_id = checkLogin(q, rm);
//                IOUtil.writeLogLn("----------- UserValidator: checked_user_id=" + checked_user_id, rm);

                if (checked_user_id.equals(user_id)) {
                    cfgTuner.addParameter("logged", "YES");
                    cfgTuner.addParameter("SESS_ID", sess_id);
                    cfgTuner.setParameterSession("USER_ID", user_id);
//					cfgTuner.addParameter("FIO", usersTuner.getParameter(user_id, "FIO"));
//    System.out.println("----------- UserValidator.validate:" + user_name + ": logged");
                    cfgTuner.deleteParameter("loginURL");
//				if (dbUtilLogin != null) //УБРАНО 01.06.2015 - сохранить dbUtilLogin для EDO UserValidator
//				{ dbUtilLogin.close();
//					dbUtilLogin = null;
//				}
                    getVUser(user_id, cfgTuner, rm);
                    return true;
                } else {
                    cfgTuner.deleteParameter("logged");
                    cfgTuner.deleteParameter("SESS_ID");
                    cfgTuner.setParameterSession("USER_ID", "");
                    cfgTuner.deleteParameter("FIO");
    IOUtil.writeLogLn(0, "xxxxxxx UserValidator: NOT LOGGED", rm);
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
    {
        try {
            usersTuner = new Tuner(null, null, null, rm);
        } catch (Exception e) {;
        }
    }

    /**
     *
     * @param user_id
     * @param cfgTuner
     */
    public void getVUser(String user_id, Tuner cfgTuner, ResourceManager rm) {
        if (cfgTuner.enabledOption("q_vu_id")) {
            IOUtil.writeLog(1, "======= getVUser() ======= user_id=" + user_id + "=>" + cfgTuner.getParameter("q_vu_id"), rm);
            String a = ",2309,413,2584,8329,4790,4241,8389,3311,97,";
//	2309-Куняев 413-Белякова 2584-Печникова 8329-Чихалина 4790-Яковлев 4241-Устенко 8389-Пляшкевич 3311-Приходько 97-Александров

            if (a.contains("," + user_id + ",")) {
                cfgTuner.setParameterSession("VU", cfgTuner.getParameter("USER_ID"));
                cfgTuner.setParameterSession("USER_ID", cfgTuner.getParameter("q_vu_id"));
                IOUtil.writeLogLn(1, " - OK", rm);
            } else {
                cfgTuner.setParameterSession("VU", null);
                cfgTuner.deleteParameter("q_vu_id");
                IOUtil.writeLogLn(1, " - REJECTED!", rm);
            }
        }
    }

    protected void getAccRightsTuner(ResourceManager rm) {

    }
  
}
