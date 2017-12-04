package jinr.arch;

import java.sql.*;


public class ServiceUploadFile_OLD_BLOB extends dubna.walt.service.Service
{

	public void beforeStart() throws Exception
	{ super.beforeStart();
		try
		{ if (cfgTuner.enabledExpression("FILE_1"))
			{
			    //            Class.forName ("oracle.jdbc.driver.OracleDriver");
			    Connection con = dbUtil.getConnection();

			    oracle.sql.BLOB bl = oracle.sql.BLOB.createTemporary( con, false, oracle.sql.BLOB.DURATION_SESSION );
			    //     bl.setBytes(posted.b);
			    dubna.walt.util.FileContent fc = (dubna.walt.util.FileContent) rm.getObject("FILE_1_CONTENT");
			    bl.putBytes( 1,fc.getBytes() );
			    //          bl = new SerialBlob(posted.b);
					
					
			    PreparedStatement stmt = con.prepareStatement
			      ( "INSERT INTO DOC_DATA (DOC_ID, PAGE_NR, PAGE_DATA, FILE_NAME, EXT, CONTENT_TYPE, UPLOADED) VALUES(?, ?, ?, ?, ?, ?, SYSDATE)" );
			    stmt.setInt( 1, cfgTuner.getIntParameter("ID") );
			    stmt.setInt( 2, cfgTuner.getIntParameter("NEXT_PAGE_NR") );
			    stmt.setBlob( 3, bl );
			    stmt.setString( 4, cfgTuner.getParameter("FILE_1"));
			    stmt.setString( 5, cfgTuner.getParameter("FILE_1_TYPE")); 
					stmt.setString( 6, cfgTuner.getParameter("FILE_1_CONTENT_TYPE")); 
//					stmt.setString( 7, "SYSDATE"); 
			    stmt.executeQuery();
			    stmt.close();
			    System.out.println("Store OK");
			}
		}
		catch (Exception e)
		{ cfgTuner.addParameter("UPLOAD_ERROR",e.toString());
			e.printStackTrace(System.out);
		}
		
	}
}