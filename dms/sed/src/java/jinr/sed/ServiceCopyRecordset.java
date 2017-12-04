package jinr.sed;

import java.sql.ResultSet;
import dubna.walt.util.DBUtil;
import dubna.walt.util.IOUtil;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

public class ServiceCopyRecordset extends dubna.walt.service.Service {

    protected boolean debugPrint = false;
    protected String[] dFields = null;
    protected String[] headers = null;
    protected PreparedStatement stmt = null;
    private int nr;

    @Override
    public void start() throws Exception {
        debugPrint = cfgTuner.enabledOption("debugPrint");
        try {
            String[] f;
            String srcFields = "";
            String destFields = "";
            String qList = "";
            String[] fields = cfgTuner.getCustomSection("fields");
            dFields = new String[fields.length];
            for (int i = 0; i < fields.length; i++) {
                f = fields[i].split("=");
                srcFields += ", " + f[0];
                destFields += ", " + f[1];
                dFields[i] = f[1];
                qList += ", ?";
            }
            cfgTuner.addParameter("srcFields", srcFields.substring(2));
            cfgTuner.addParameter("destFields", destFields.substring(2));
            cfgTuner.addParameter("qList", qList.substring(2));
            cfgTuner.addParameter("srcSQL", getSQL("srcSQL"));
            cfgTuner.outCustomSection("report header", out);
            out.flush();
            cfgTuner.outCustomSection("prepare data", out);
            doCopy(getSQL("srcSQL"));
        } catch (Exception e) {
            e.printStackTrace(System.out);
            IOUtil.writeLogLn("XXXXXXXX Exception: " + e.toString(), rm);
            cfgTuner.addParameter("ImportDataError", e.toString());
            cfgTuner.addParameter("ERROR", e.toString());
//      out.print(e.toString()); 
//      out.flush();  
        }
        cfgTuner.outCustomSection("report footer", out);
    }

    protected void doCopy(String sql) throws Exception {
        if (sql == null || cfgTuner.enabledOption("NotConnected")) {
            return;
        }
        DBUtil srcDBUtil = dbUtil;
        System.out.println("doCopy: srcDBUtil=" + srcDBUtil);

        ResultSet r = srcDBUtil.getResults(sql);

        if (r != null) {
            cfgTuner.outCustomSection("start copy", out); // SET DBUtil to default
            int numBatch = cfgTuner.getIntParameter("numBatch");
            if (numBatch < 1) {
                numBatch = 100;
            }
            Connection conn = dbUtil.getConnection();
            conn.setAutoCommit(false);
            stmt = conn.prepareStatement(cfgTuner.getParameter("InsertSQL"));
            System.out.println(cfgTuner.getParameter("InsertSQL"));

            headers = DBUtil.getColNames(r);
            nr = 0;
            while (r.next()) {
                processRecord(r);
                if ((nr % numBatch) == 0) {
                    dbUtil.commit();
                    out.print(nr + " ");
                    out.flush();
                    Thread.sleep(100);
                }
                nr++;
            }

            srcDBUtil.closeResultSet(r);
            srcDBUtil.close();
            dbUtil.commit();
            out.print("<br>" + nr + " records copied. ");
            out.flush();
        } else {
            throw (new Exception("COULD NOT READ SOURCE TABLE"));
        }
    }

    protected void processRecord(ResultSet r) throws SQLException {
        if (cfgTuner.enabledOption("SHOW_DET")) {
            out.print("<hr>" + nr + ":");
        }
        for (int i = 1; i <= headers.length; i++) {
            stmt.setObject(i, r.getObject(i));
            if (cfgTuner.enabledOption("SHOW_DET")) {
                out.print("<br>" + i + ":" + headers[i - 1] + ": " + r.getObject(i) + ";");
            }
        }
        //          copyValue(i+1, r, stmt, types[i]);
        stmt.executeUpdate();
    }

}