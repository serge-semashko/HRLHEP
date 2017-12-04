package jinr.arch;

import dubna.walt.util.*;
import java.sql.*;


public class ServiceImportData extends dubna.walt.service.Service
{

	public void start() throws Exception
	{
		cfgTuner.outCustomSection("report header",out);
		
		try
			{ doCopy(getSQL("SQL"));
			}
			catch (Exception e)
			{ IOUtil.writeLogLn("XXXXXXXX Exception: " + e.toString(), rm);
				cfgTuner.addParameter("ImportDataError", e.toString());
				cfgTuner.addParameter("ERROR", e.toString());
	//      out.print(e.toString()); 
	//      out.flush();  
			}
		cfgTuner.outCustomSection("[report footer]",out);
	}

		public void doCopy(String sql) throws Exception
		{ if (sql == null || cfgTuner.enabledOption("NotConnected"))
				return;
			DBUtil srcDBUtil = dbUtil;
			System.out.println("doCopy: dbUtil=" + srcDBUtil);    
			String val = "";
			
			if (sql.toUpperCase().indexOf("SELECT_SP") == 0)
				sql = sql.substring(9);
			ResultSet r = srcDBUtil.getResults(sql);
			cfgTuner.outCustomSection("start loop", out);
			Connection conn = dbUtil.getConnection();
			conn.setAutoCommit(false);
			if (r != null)
			{ String[] headers = DBUtil.getColNames(r);
				int nr = 0;
				while (r.next())
				{ for (int i = 0; i < headers.length; i++)
					{ val = r.getString(i+1);
						if (val != null && val.length() > 0 && !val.equalsIgnoreCase("NULL"))
							cfgTuner.addParameter(headers[i], val.replace('\'','"'));
						else
							cfgTuner.addParameter(headers[i], "");
					}
					nr++;
					cfgTuner.addParameter("REC_NR", Integer.toString(nr));
					cfgTuner.addParameter("REC_100", Integer.toString(nr % 100));
					cfgTuner.outCustomSection("record", out);
				  loadFile();
				}
	//      r.close();
				srcDBUtil.closeResultSet(r);
				srcDBUtil.close();
				dbUtil.commit();
			}
		}


	public void loadFile() throws Exception
	{ String fileName = "";
		try
		{ if (cfgTuner.enabledExpression("file"))
			{	fileName = cfgTuner.getParameter("file");
			  String filePath = cfgTuner.getParameter("uploadPath") + fileName;
			    //            Class.forName ("oracle.jdbc.driver.OracleDriver");
				long doc_id=cfgTuner.getIntParameter("NEW_ID_INT");
				if (doc_id < 1)
				  throw new Exception("COULD NOT GET DOC_ID:" + doc_id + ", file:" + fileName);        
				int pageNr=cfgTuner.getIntParameter("NEXT_PAGE_NR");
				if (pageNr < 1) pageNr=1;
				Connection con = dbUtil.getConnection();
				oracle.sql.BLOB bl = oracle.sql.BLOB.createTemporary( con, false, oracle.sql.BLOB.DURATION_SESSION );
				//     bl.setBytes(posted.b);
				FileContent fc = new FileContent(filePath, "");
				bl.putBytes( 1,fc.getBytes() );
				//          bl = new SerialBlob(posted.b);
				PreparedStatement stmt = con.prepareStatement
					( "INSERT INTO DOC_DATA (DOC_ID, PAGE_NR, PAGE_DATA, FILE_NAME, EXT, CONTENT_TYPE, UPLOADED) VALUES(?, ?, ?, ?, ?, ?, SYSDATE)" );
				stmt.setLong( 1, doc_id );
				stmt.setInt( 2, pageNr);
				stmt.setBlob( 3, bl );
				stmt.setString( 4, fileName);
				stmt.setString( 5, cfgTuner.getParameter("FILE_1_TYPE")); 
				stmt.setString( 6, cfgTuner.getParameter("FILE_1_CONTENT_TYPE")); 
//					stmt.setString( 7, "SYSDATE"); 
				stmt.executeQuery();
				stmt.close();
				System.out.println("Store OK");
				cfgTuner.addParameter("UPLOAD_ERROR","");
			}
		}
		catch (Exception e)
		{ cfgTuner.addParameter("UPLOAD_ERROR",e.toString());
			e.printStackTrace(System.out);
		  cfgTuner.addParameter("UPLOAD_ERRORS", cfgTuner.getParameter("UPLOAD_ERRORS") + "<br>" + fileName);
		}
		
	}
}