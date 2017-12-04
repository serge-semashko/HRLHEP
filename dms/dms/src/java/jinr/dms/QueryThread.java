package jinr.dms;

import dubna.walt.util.*;

import java.sql.*;

public class QueryThread extends dubna.walt.SimpleQueryThread {
// public DBUtil dbUtil = null;

	public boolean logIt = true;
	private static final String excludeFromLog = ",svs/showInfoTooltip,sys/request_log,sys/viewRequest,sys/setErrFixed,";

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

			//    rm.setObject("cfgTuner", cfgTuner);
			if (!cfgTuner.enabledOption("KeepLog=true")
					|| cfgTuner.enabledOption("ClearLog=true")) {
				IOUtil.clearLogFile(rm);
			}

//       writeHttpHeaders();
			if (validateUser()) {
				logQuery();
			} else {
				rm.println("\n"
						+ rm.getString("queryLabel")
						+ ": " + cfgTuner.getParameter("c")
						+ " NOT LOGGED, " + cfgTuner.getParameter("ClientIP")
						+ "; [" + Fmt.shortDateStr(new java.util.Date()) + "] "
				);
				cfgTuner.outCustomSection("not identified", outWriter);
			}
			validateParameters();
			startService();
			logQuery(null);
		} catch (Exception e) {
			logException(e);
			logQuery(e);
		} finally {
			if (outWriter != null) {
				outWriter.flush();
				outWriter.close();
				try {
					outStream.close();
					//         inpStream.close();
				} catch (Exception e) { /* don't warry! */ }
			}
			finish();
		}
	}
	/**/

	@Override
	public boolean validateUser() throws Exception {
		if (!logIt) {
			return true;
		}
		return super.validateUser();
	}

	/**
	 * Вывод в консоль Томката информации о запросе
	 */
	@Override
	public void logQuery() {
		if (logIt) {
			System.out.println(
					rm.getString("queryLabel")
					+ ": " + cfgTuner.getParameter("c")
					+ ": " + cfgTuner.getParameter("USER_ID")
					//							+ " (" + cfgTuner.getParameter("USER_ROLE") 
					+ ") " + cfgTuner.getParameter("ClientIP")
					+ "; [" + Fmt.shortDateStr(new java.util.Date()) + "] "
			);
		}
	}

	/**
	 * Запись в лог в БД основных параметров http-запроса
	 *
	 * @param e зафиксированная exception при выполнения запроса Если exception
	 * не возник, то регистрируется параметр ERROR из Tuner
	 */
	@Override
	public void logQuery(Exception e) {
//	    System.out.println("+++ logQuery +++ c=" + cfgFileName + "; e=" + e);
		if (!logIt) {
			return;
		}
		dbUtil = (DBUtil) rm.getObject("DBUtil", false);
		if (dbUtil == null || !dbUtil.isAlive()) {
			try {
				dbUtil = makeDBUtil();
			} catch (Exception ex) {
				System.out.println("+++ logQuery: ERROR! " + ex.toString());
				ex.printStackTrace(System.out);
			}
		}
		if (dbUtil == null) {
			System.out.println("+++ logQuery: ERROR! dbUtil is null!");
		} else if (cfgTuner == null) {
			System.out.println("+++ logQuery: ERROR! cfgTuner is null!");
		} else {
			try {
				String c = cfgTuner.getParameter("c");
				String err = trimString(cfgTuner.getParameter("ERROR"), 1900);
//	    System.out.println("+++ logQuery +++ err=" + err + "; ");
				String request_name = trimString(cfgTuner.getParameter("request_name"), 64);
				if (!excludeFromLog.contains("," + c + ",")) {
					Connection conn = dbUtil.getConnection();
					conn.setAutoCommit(true);
					String s = "insert into a_req_log (USER_ID, C, REQUEST_NAME, QUERY, COOKIES, ERR, DAT, IP, USER_AGENT, REF, SESS_ID, SESS, TIM) values (?, ?, ?, ?, ?, ?, NOW(), ?, ?, ?, ?, ?, ?)";
					PreparedStatement stmt = conn.prepareStatement(s);
					stmt.setInt(1, cfgTuner.getIntParameter(null, "USER_ID", 0));
					stmt.setString(2, c);
					stmt.setString(3, request_name);
					stmt.setString(4, trimString(cfgTuner.getParameter("queryString"), 2047));
					stmt.setString(5, trimString(cfgTuner.getParameter("h_cookie"), 2047));
					if (e == null) {
						stmt.setString(6, err);
					} else {
						stmt.setString(6, e.toString() + " / " + err);
					}
					stmt.setString(7, cfgTuner.getParameter("ClientIP"));
					stmt.setString(8, cfgTuner.getParameter("h_user-agent"));
					stmt.setString(9, cfgTuner.getParameter("h_referer"));
					stmt.setInt(10, cfgTuner.getIntParameter(null, "SESS_ID", 0));
					stmt.setString(11, cfgTuner.getParameter("q_JSESSIONID"));
					long l = System.currentTimeMillis() - startTm;
					stmt.setInt(12, (int) l);
					stmt.executeUpdate();
				}
			} catch (Exception ex) {
				System.out.println("+++ logQuery: STORE ERROR! " + ex.toString());
				ex.printStackTrace(System.out);
			}
		}
	}

	private String trimString(String s, int maxLen) {
		return s.substring(0, Math.min(s.length(), maxLen));
	}

	/**
	 * Коннект к БД в начале выполнения запроса Коннект не делается для
	 * cfg-файлов, содержащих "_noDB" в имени файла или в пути к нему
	 *
	 * @return объект DBUtil, который далее будет использоваться для обращения к
	 * БД.
	 * @throws Exception
	 */
	public synchronized DBUtil makeDBUtil() throws Exception {
		if (cfgFileName.contains("_noDB")) {
			logIt = false;
			return null;
		}
		if ( cfgFileName.contains("free/")
			|| cfgFileName.contains("svs/"))
			logIt = false;

		if (!cfgTuner.enabledOption("connString")) {
			return null;
		}
		Class.forName(rm.getString("dbDriver"));        // init the JDBC driver
		try {
			/* Establish connection to the database and make DBUtil */
			if (cfgTuner.enabledOption("debug=on")) {
				System.out.println("  connect:" + cfgTuner.getParameter("connString")
						+ cfgTuner.getParameter("database") + cfgTuner.getParameter("connParam")
						+ " //|| " + cfgTuner.getParameter("usr") + "/*** "
				//	+ cfgTuner.getParameter("pw")
				);
			}
			Connection conn = DriverManager.getConnection(cfgTuner.getParameter("connString")
					+ cfgTuner.getParameter("database")
					+ cfgTuner.getParameter("connParam"), cfgTuner.getParameter("usr"), cfgTuner.getParameter("pw"));
			conn.setAutoCommit(false);
			dbUtil = new DBUtil(conn, queryLabel);
			dbUtil.db = DBUtil.DB_MySQL;
			dbUtil.allocate();

//=============== SET DB Connection properties =====================
//	   Statement stmt = conn.createStatement();
//	   stmt.executeUpdate("set max_sp_recursion_depth=30");
//	   stmt.executeUpdate("SET NAMES cp1251");
//	   stmt.executeUpdate("set max_allowed_packet=5000000");
//	   conn.commit();
//	   stmt.close();
			rm.setObject("DBUtil", dbUtil, false);
		} catch (Exception e) {
			System.out.println("Connection to " + cfgTuner.getParameter("connString") + " FAILED!...");
//		 e.printStackTrace(System.out);
			cfgTuner.addParameter("NotConnected", "Y");
			if (outWriter != null) {
				cfgTuner.addParameter("ERR_MSG", e.getMessage());
				cfgTuner.addParameter("ERR_MSG_DES",
						"Could not connect to the Database " + cfgTuner.getParameter("connString"));
			}
			/*		 if (outWriter != null)
			 { outWriter.println("<small>" +  e.getMessage() + "</small>");
			 outWriter.println("<center><table border=1 bgcolor=#FFEEBB cellpadding=8><tr><th>"
			 + "Could not connect to the Database '" + cfgTuner.getParameter("connString") + "!</th></tr></table></center><p>");
			 outWriter.flush();
			 }
			 */ e.printStackTrace(System.out);
			return null;
		}
		if (cfgTuner.enabledOption("debug=on")) {
			System.out.println(" Connect OK!");
		}
		return dbUtil;
	}

	/**
	 * Finalizer. Закрытие коннектов к базе, запись в консоль Томката в режиме
	 * "debug=on"
	 */
	protected void finish() {
		closeDBUtils();
		if (cfgTuner != null) {
			try {
				if (cfgTuner.enabledOption("debug=on")) {
					System.out.println("\n" + "[" + Fmt.lsDateStr(new java.util.Date()) + "] "
							+ rm.getString("queryLabel") + ": --- finish()");
				}
			} catch (Exception e) {;
			}
		}
	}

	/**
	 * Закрытие коннектов к базе, удаление DBUtils из ResourceManager rm Если
	 * включены транзакции и были ошибки, то откат, иначе - коммит
	 */
	protected void closeDBUtils() {
		if (dbUtil != null) {
			try {
				if (cfgTuner.enabledOption("ERROR")) {
					dbUtil.update("ROLLBACK");
				} else {
					dbUtil.update("COMMIT");
				}
			} catch (Exception e) {
			}
			dbUtil.release();
			dbUtil.close();
		}

		dbUtil = (DBUtil) rm.getObject("DBUtil", false);
		if (dbUtil != null) {
			dbUtil.release();
			dbUtil.close();
			rm.setObject("DBUtil", null);
		}
		dbUtil = null;
//  System.out.println("---------------- closeDBUtils() " + queryLabel);
	}

}

/**
 * Reads and parses the multipart form data from the ServletInputStream
 */
/*	 
 public String getMultiPartData(HttpServletRequest request, Hashtable ht)
 throws Exception
 {
 if (ServletFileUpload.isMultipartContent(request))
 { String allParams = MULTIPART_SEPARATOR;

 ServletFileUpload upload = new ServletFileUpload();
 ServletRequestContext rc = new ServletRequestContext(request);
 int contLength=rc.getContentLength();
 registerParameter("ContentLength", Long.toString(contLength), ht);
 //			System.out.println("ContentLength: " + contLength + "; CharacterEncoding: " + rc.getCharacterEncoding());
		 
 BufferedInputStream fileStream = null;
 //	   InputStream fileStream = null;
 int bufLen = 64000;
 byte[] buf = new byte[bufLen];
			
 String name = "";
 String val = "";
 String filePath = "";
 String fileName = "";
 String contentType = "";
					   
 FileItemIterator iter = upload.getItemIterator(request);
 while (fileStream == null && iter.hasNext()) 
 {   FileItemStream item = iter.next();
 name = item.getFieldName();
 InputStream stream = item.openStream();
 if (item.isFormField()) 
 { val = getAsString(stream, buf, bufLen);
 if (val.length() > 0)
 { registerParameter(name, val, ht);
 //  System.out.println("Form field: '" + name + "'='"  + val  + "'");						              
 allParams = allParams + name + "=" + val + MULTIPART_SEPARATOR;
 }
 name = "";
 val = "";
 //                + StreamUtil.asString(stream) + " detected.");
 } else 
 { filePath = item.getName();
 if (filePath.length() > 1)
 {	
 fileName = FileContent.parseFilePath(filePath);;
 //System.out.println("File field " + name + " file name: '" + filePath + "' ContentType:" + item.getContentType() );
														 
 registerParameter(name + "_TYPE", FileContent.getFileType(fileName), ht);
 contentType = item.getContentType();
 registerParameter(name + "_CONTENT_TYPE", contentType, ht);
 registerParameter(name, fileName, ht);
 registerParameter(name + "_SRC", StrUtil.replaceInString(filePath,"\\","/"), ht);
 allParams = allParams + name + "=" + fileName + MULTIPART_SEPARATOR
 + name + "_TYPE=" + FileContent.getFileType(fileName) + MULTIPART_SEPARATOR; 
								
 if (name.indexOf("BFILE_1") == 0)
 { 
 //								fileStream=stream;
 fileStream=new BufferedInputStream(stream);
 rm.setObject(name + "_STREAM",fileStream);
 }
 else
 {
 FileContent fc = new FileContent(stream, fileName, contentType, contLength);
 rm.setObject(name + "_CONTENT",fc);
 registerParameter(name+ "_SIZE", Integer.toString(fc.getFileSize()), ht);
 //            fc.storeToDisk(rm.getString("AppRoot") + rm.getString("uploadPath"), filename);
 //            fc.storeToDisk(cfgTuner.getParameter("AppRoot") + cfgTuner.getParameter("uploadPath"), filename);
								
 }
 }
 }
 }
	
 //		 String s = new String(buf, 0, n, clientEncoding); 

 //System.out.println("... bytes read. Total:" + numBytes + ". Content length:" + contLength + ". Rest:" + rest);
 params.addElement("multipart=yes");
 return allParams;
 }
 else
 return "";
 }
 */

/*
 public void logException(Exception e)
 { if (! (e instanceof java.net.SocketException))
 { 	if (cfgTuner != null) cfgTuner.addParameter("ERROR", e.toString());
 if (srv == null || !srv.terminated)
 { if (outWriter == null)
 { try
 { outWriter = new PrintWriter(response.getOutputStream());
 } catch (Exception ex) {; }
 }
			
 if (outWriter != null 
 && (	cfgTuner == null 
 || cfgTuner.enabledOption("debug=on")
 || cfgTuner.getParameter("USER_GROUP").equalsIgnoreCase("SA")))
 { outWriter.println("<xmp>");
 e.printStackTrace(outWriter);
 outWriter.println("</xmp>");
 logAll();
 }
 outWriter.println(e.toString());
 }  
 }
 }
 */
/**/

/*
 public String getAsString(InputStream in, byte[] buf, int bufLen) throws Exception
 { String s = "";
 int numBytes = 1;
 int bufPos = 0;
 while (numBytes > 0)
 { while (bufPos < bufLen && numBytes > 0)
 { numBytes = in.read(buf, bufPos, bufLen-bufPos);
 if (numBytes > 0)
 bufPos += numBytes;
 }
 s += new String(buf, 0, bufPos, clientEncoding);
 }
 return s; 
 }
 /**/

/* Непонятно, зачем оно было в ПИНе и нужно ли нам сейчас
 public void makeTuner() throws Exception
 { String cfg = cfgFileName;
 GregorianCalendar c = new GregorianCalendar();
 String now = Fmt.fmt(c.get(Calendar.YEAR), 4, Fmt.ZF) + "-" 
 + Fmt.fmt(c.get(Calendar.MONTH)+1, 2, Fmt.ZF) + "-"
 + Fmt.fmt(c.get(Calendar.DAY_OF_MONTH), 2, Fmt.ZF) + " "
 + Fmt.fmt(c.get(Calendar.HOUR_OF_DAY), 2, Fmt.ZF) + ":"
 + Fmt.fmt(c.get(Calendar.MINUTE), 2, Fmt.ZF) + ":"
 + Fmt.fmt(c.get(Calendar.SECOND), 2, Fmt.ZF);
 params.addElement("now=" + now);
 super.makeTuner();
 if (cfgTuner.enabledOption("reloadCfg=y"))
 { jinr.edo.Servlet sr = (jinr.edo.Servlet) rm.getObject("Servlet");
 sr.setResourceManager(sr.obtainResourceManager());
 System.out.println ("+++++++ RM Reloaded; CFG:" + cfg);
 cfgFileName = cfg;
 cfgTuner.addParameter("c", cfg);
 }
 //  System.out.println("......  cfgFileName:" + cfgFileName);
 }
 /**/

	/* Мы пока не дошли до проверок IP. Не ясно, дойдем ли...
	 public void startService() throws Exception
	 { if (checkClientIP())
	 super.startService();
	 }
 
	 public boolean checkClientIP()
	 {	StringTokenizer st = null;
	 String blocked_ip = cfgTuner.getParameter("blocked_ip");
	 String ip = cfgTuner.getParameter("ClientIP");
	 String ip_mask = cfgTuner.getParameter("ip_mask");
	 if (ip_mask.length() > 0)
	 { st = new StringTokenizer(ip_mask, ",");
	 while (st.hasMoreTokens())
	 { String s = st.nextToken();
	 if (ip.indexOf(s) == 0) 
	 return true;      
	 }
	 System.out.println("XXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX BLOCKED IP: " + ip);
	 return false;
	 }

	 if (blocked_ip.length() > 0)
	 { st = new StringTokenizer(blocked_ip, ",");
	 while (st.hasMoreTokens())
	 { String s = st.nextToken();
	 if (ip.indexOf(s) == 0) 
	 {
	 System.out.println("ZZZZZZZZZZZZZZZZXXXXXXXXXXXXXXXXXX BLOCKED IP: " + ip);
	 return false;     
	 }
	 }
	 }

	 return true;
	 }	

	 /**/
