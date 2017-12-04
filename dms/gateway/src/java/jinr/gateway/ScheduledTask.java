package jinr.gateway;

import dubna.walt.service.Service;
import dubna.walt.util.DBUtil;
import dubna.walt.util.Fmt;
import dubna.walt.util.IOUtil;
import dubna.walt.util.ResourceManager;
import dubna.walt.util.Tuner;
import java.sql.Connection;
import java.util.Date;

/**
 *
 * @author serg
 */
public class ScheduledTask {

    protected int taskId;
    protected String time;
    protected int hr;
    protected int min;
    protected String module;
    protected String comment;
    protected long lastCall;
    protected String lastResult;
    protected ResourceManager rm;

    public ScheduledTask(int taskId, String time, String module, long lastCall, String lastResult, ResourceManager rm_Global) {
        this.taskId = taskId;
        this.time = time;
        this.module = module;
//    this.comment=comment;
        this.lastCall = lastCall;
        this.lastResult = lastResult;
        String[] nextTm = time.split(":");
        hr = Integer.parseInt(nextTm[0]);
        min = Integer.parseInt(nextTm[1]);
        rm = rm_Global.cloneRM();
        rm.setObject("rm_Global", rm_Global);
        rm.setParam("log", "true");
        IOUtil.writeLogLn(5, "new ScheduledTask(" + taskId + ", " + time + ", " + module + ", " + lastCall + ", " + lastResult + ") / " + hr + ":" + min, rm);
    }

    public void checkStatus() {
        try {
//            System.out.println(" ScheduledTask.checkStatus(" + taskId + ", " + time + ", " + module + ", " + lastCall + ", " + lastResult + ")");
            Date datNext = new java.util.Date();
            datNext.setHours(hr);
            datNext.setMinutes(min);
            datNext.setSeconds(0);
            Date datLast = new java.util.Date(lastCall);
            Date dat = new java.util.Date();
//            System.out.println("datNext=" + datNext.toLocaleString() + "; datLast=" + datLast.toLocaleString() + "; comp=" + datLast.compareTo(datNext) + "/" + dat.compareTo(datNext));
            IOUtil.writeLogLn(7, "<hr> Check Task "+ taskId + ":" + module + "; Last Call:" + Fmt.shortDateStr(datLast) 
                        + " [" + Fmt.shortDateStr(dat) + ":" + dat.getSeconds() + "]; "
                        + "</span> " + "; ", rm);
            if (datLast.compareTo(datNext) < 0 && dat.compareTo(datNext) > 0) {
                IOUtil.writeLogLn(3, "<hr><span style='border:solid 1px red; font-weight:bold; background-color:#FFFFA0;'> ++++++++ START SCHEDULED TASK +++++++ "
                        + taskId + ":" + module + " [" + Fmt.shortDateStr(dat) + ":" + dat.getSeconds() + "]; "
                        + "</span> " + "; ", rm);
                System.out.println("++++++++ START SCHEDULED TASK " + taskId + ":" + module + " [" + Fmt.shortDateStr(dat) + ":" + dat.getSeconds() + "]; ");
                ResourceManager rms = Monitor.callService(module);
                lastCall = System.currentTimeMillis();
                Tuner taskTuner = (Tuner) rms.getObject("cfgTuner");

                DBUtil db = null;

                try {
                    db = Monitor.makeDBUtil(rm);
                    String sql = "update schedule set lastCall=" + lastCall + ", lastCallTm=now(), lastResult='" + taskTuner.getParameter("RESULT") + "' where id=" + taskId;
                    IOUtil.writeLogLn(3, " ***** Update SQL=" + sql, rm);
//                    System.out.println(" ***** Update schedule SQL=" + sql);
                    db.update(sql);
                    IOUtil.writeLogLn(3, "<span style='border:solid 1px red; font-weight:bold; background-color:#FFFFA0;'>BACK to MONITOR.callService() : "
                            + "</span> " + ";<hr>", rm);
                } catch (Exception e) {
                    e.printStackTrace();
                } finally {
                    if (db != null) {
                        db.close();
                    }
                }
            }

        } catch (Exception e) {
            e.printStackTrace();
            IOUtil.writeLogLn(0, " ScheduledTask.checkStatus() ERROR:" + e.toString(), rm);
        }
    }

}
