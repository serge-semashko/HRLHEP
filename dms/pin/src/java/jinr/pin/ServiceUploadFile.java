package jinr.pin;

import dubna.walt.util.IOUtil;
import java.sql.*;
import javax.sql.rowset.serial.*;
import dubna.walt.util.StrUtil;

public class ServiceUploadFile extends dubna.walt.service.Service
{

	public void start() throws Exception
	{ try
		{ if (cfgTuner.enabledExpression("FILE_1"))
			{
		    Connection con = dbUtil.getConnection();
			  String sql = StrUtil.strFromArray(cfgTuner.getCustomSection("StoreFile"));
				IOUtil.writeLogLn("<b>Upload file '" + cfgTuner.getParameter("FILE_1") + "'</b><br>" + sql, rm);
			  //        System.out.println("Store SQL:" + sql);
			  PreparedStatement stmt = con.prepareStatement (sql);

//			  oracle.sql.BLOB bl = oracle.sql.BLOB.createTemporary( con, false, oracle.sql.BLOB.DURATION_SESSION );
 //          bl.putBytes( 1,fc.getBytes() );
			    dubna.walt.util.FileContent fc = (dubna.walt.util.FileContent) rm.getObject("FILE_1_CONTENT");
					java.sql.Blob bl = new SerialBlob(fc.getBytes());
					
					stmt.setBlob( 1, bl );
//			    stmt.executeQuery();
					int numRec = stmt.executeUpdate();
			    stmt.close();
					con.commit();
					if (numRec == 1)
					{ IOUtil.writeLogLn("Upload file - OK", rm);
					}
					else
					{ cfgTuner.addParameter("UPLOAD_ERROR","Number inserted records:" + numRec);
					  cfgTuner.addParameter("ERROR","Number inserted records:" + numRec);
					  IOUtil.writeLogLn("***** Upload file: number inserted records:" + numRec, rm);
					}
			}
		}
		catch (Exception e)
		{ cfgTuner.addParameter("UPLOAD_ERROR", e.toString());
			e.printStackTrace(System.out);
		  cfgTuner.addParameter("ERROR",e.toString());
		}
	  super.start();
	}
}