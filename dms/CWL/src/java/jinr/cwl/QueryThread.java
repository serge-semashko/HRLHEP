package jinr.cwl;

import dubna.walt.util.*;

import java.sql.*;

public class QueryThread extends dubna.walt.SimpleQueryThread {

	/**
	 *
	 *
	 */
	@Override
	public void start() {
		try {
			startTm = System.currentTimeMillis();
			parseRequest(request);
			getAddresses();
			makeTuner();
			setContentType();
			dbUtil = makeDBUtil();

			if (!cfgTuner.enabledOption("KeepLog=true")
					|| cfgTuner.enabledOption("ClearLog=true")) {
				IOUtil.clearLogFile(rm);
			}

			validateUser();
			rm.println("\n"
					+ rm.getString("queryLabel")
					+ ": " + cfgTuner.getParameter("c")
					+ ": " + cfgTuner.getParameter("login")
					+ ":" + cfgTuner.getParameter("ClientIP")
					+ ";[" + Fmt.shortDateStr(new java.util.Date()) + "]\n "
			);

			startService();

		} catch (Exception e) {
			logException(e);
			logQuery(e);
		} finally {
			if (outWriter != null) {
				outWriter.flush();
				outWriter.close();
				try {
					outStream.close();
				} catch (Exception e) { /* don't warry! */ }
			}
			finish();
		}
	}
	/**/

	@Override
	public boolean validateUser() throws Exception {
		jinr.cwl.UserValidator uv = (jinr.cwl.UserValidator) rm.getObject("UserValidator", false);
		boolean validated = uv.validate(rm);
//	System.out.println("Validated: " + validated + "; " + uv);
		return validated;
	}

	/**
	 * Коннект к БД в начале выполнения запроса Коннект не делается для
	 * cfg-файлов, содержащих "_noDB" в имени файла или в пути к нему
	 *
	 * @return объект DBUtil, который далее будет использоваться для обращения к
	 * БД.
	 * @throws Exception
	 */
	@Override
	public synchronized DBUtil makeDBUtil() throws Exception {
		if (cfgFileName.indexOf("_noDB") > 0) {
			return null;
		}

		Class.forName(rm.getString("dbDriver"));        // init the JDBC driver
		try {
			/* Establish connection to the database and make DBUtil */
//			if (cfgTuner.enabledOption("debug=on")) 
			{ 
				System.out.println("  connect:" + cfgTuner.getParameter("connString")
						+ " //|| " + cfgTuner.getParameter("usr") + "/*** " 
					+ cfgTuner.getParameter("pw")
				);
			}
			Connection conn = DriverManager.getConnection(cfgTuner.getParameter("connString")
					, cfgTuner.getParameter("usr"), cfgTuner.getParameter("pw"));
			conn.setAutoCommit(true);
			dbUtil = new DBUtil(conn, queryLabel);
			dbUtil.db = DBUtil.DB_MySQL;
			dbUtil.allocate();

			rm.setObject("DBUtil", dbUtil, false);
		} catch (Exception e) {
			System.out.println("Connection to " + cfgTuner.getParameter("connString") + " FAILED!...");

			cfgTuner.addParameter("NotConnected", "Y");
			if (outWriter != null) {
				cfgTuner.addParameter("ERR_MSG", e.getMessage());
				cfgTuner.addParameter("ERR_MSG_DES",
						"Could not connect to the Database " + cfgTuner.getParameter("connString"));
			}
				 if (outWriter != null)
			 { outWriter.println("<small>" +  e.getMessage() + "</small>");
			 outWriter.println("<center><table border=1 bgcolor=#FFEEBB cellpadding=8><tr><th>"
			 + "Could not connect to the Database '" + cfgTuner.getParameter("connString") + "!</th></tr></table></center><p>");
			 outWriter.flush();
			 }
			 /**/
			e.printStackTrace(System.out);
			return null;
		}
		if (cfgTuner.enabledOption("debug=on")) {
			System.out.println(" Connect OK!");
		}
		return dbUtil;
	}

}
