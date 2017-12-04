package jinr.adb;

import dubna.walt.util.*;

public class UserValidatorH extends dubna.walt.util.UserValidator {

    private Tuner cfgTuner = null;

    /**
     *
     *
     */
    public synchronized boolean validate(ResourceManager rm) throws Exception {
        //  HttpServletRequest request = (HttpServletRequest) rm.getObject("request");
        //                        PrintWriter outWriter,

        cfgTuner = (Tuner) rm.getObject("cfgTuner");
        cfgTuner.deleteParameter("logged");
        cfgTuner.deleteParameter("uname");
        cfgTuner.deleteParameter("FIO");
        cfgTuner.deleteParameter("LOGINNAME");
        cfgTuner.deleteParameter("ADMIN");
        cfgTuner.addParameter("USER_ID", "-1");

        boolean logged = false;
        String user_id = "";
        String login = "";
        String sess_id = "100";

        String ip = cfgTuner.getParameter("ClientIP");

        if (ip.equals("159.93.40.211") || ip.equals("159.93.192.174") || ip.equals("127.0.0.1") ) {
            user_id = "1";
            cfgTuner.addParameter("FIO","Куняев С.В.");
        }
        else if (ip.equals("95.72.11.43") || ip.equals("185.48.37.163")) {
            user_id = "34";
            cfgTuner.addParameter("FIO","Потребеников Ю.К.");
        }

        if (user_id.length() > 0 && (Integer.parseInt(user_id) > 0 || Integer.parseInt(user_id) < -1)) {
            cfgTuner.addParameter("logged", "YES");
            cfgTuner.addParameter("USER_ID", user_id);
            cfgTuner.addParameter("LOGINNAME", login);
            cfgTuner.addParameter("uname", login);
            cfgTuner.addParameter("SESS_ID", sess_id);
            //          System.out.println( "----------- UserValidator.validate:" + login + ": logged" );
            if (user_id.equals("1")) {
                if (cfgTuner.enabledOption("q_VU")) {
                    cfgTuner.addParameter("ADMIN", "Y");
                    cfgTuner.addParameter("UID", "1");
                    cfgTuner.addParameter("USER_ID", cfgTuner.getParameter("q_VU"));
                }
            } else {
                cfgTuner.deleteParameter("debug");
            }
//						IOUtil.writeLogLn( "<b>+++ UserValidator - OK:</b> user_id=" + user_id + "; login=" + login, rm );
            return true;
        } else {
            //========== Логин не прошёл ================
            cfgTuner.addParameter("NoAccess", "Y");
            String c = cfgTuner.getParameter("c");
            //  сбрасываем dbUtilLogin для обновления коннекта

            //      t = System.currentTimeMillis() - t; // + queryNum
            //        System.out.println( ".....  validate " + ": " + cfgTuner.getParameter( "LOGINNAME" ) + "; spent: " + t + " ms." );
            return true;
        }

    }
}