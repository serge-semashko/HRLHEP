package jinr.pin;

import java.io.*;
import dubna.walt.util.IOUtil;
import java.sql.*;
import javax.sql.rowset.serial.*;
import dubna.walt.util.*;

/** 
 * 1) outputs [report header] section
 * 2) checks for "FILE_1" parameter. If exists - not implemented yet
 * 3) checks for "BFILE_1" parameter. If exists:
 * takes BFILE_1_STREAM from rm
 * checks if StoreFilesToDB=Y
 * IF YES -
 *  - takes parameter PieceLength
 *  - takes SQL [StoreBFileSQL] ( insert (?,?,...)
 *    1 - pieceNr, 2 - blob
 *  - reads BFILE_1_STREAM and inserts set of records size <= PieceLength
 * IF NO - 
 *  - takes cfgTuner parameters: uploadPath, filePath, BFILE_1 
 *  - copies BFILE_1_STREAM into #uploadPath##filePath##fileName#
 * 
 */
public class ServiceUploadBFile extends dubna.walt.service.Service
{
		public void start() throws Exception
		{
			cfgTuner.outCustomSection("report header",out);
			try
			{ if (cfgTuner.enabledExpression("BFILE_1"))
				{ // InputStream inp = (InputStream) rm.getObject("BFILE_1_STREAM");
				  BufferedInputStream inp = (BufferedInputStream) rm.getObject("BFILE_1_STREAM");
					if (cfgTuner.enabledOption("StoreFilesToDB=Y"))
						storeToDB(inp);
					else
						FileContent.storeToDisk(inp
							, cfgTuner.getParameter("uploadPath")
							+ cfgTuner.getParameter("filePath")
							, cfgTuner.getParameter("BFILE_1"));
				}
			  else if (cfgTuner.enabledExpression("FILE_1"))
				{ dubna.walt.util.FileContent fc = (dubna.walt.util.FileContent) rm.getObject("FILE_1_CONTENT");
				  cfgTuner.addParameter("FILE_SIZE", Integer.toString(fc.getFileSize()));
				  if (cfgTuner.enabledOption("StoreFilesToDB=Y"))
				    storeToDB(fc);
				  else
				    fc.storeToDisk(
				  //        cfgTuner.getParameter("AppRoot") +
				     cfgTuner.getParameter("uploadPath")
						+ cfgTuner.getParameter("filePath")
						, cfgTuner.getParameter("FILE_1"));

				}
			}
			catch (Exception e)
			{ cfgTuner.addParameter("UPLOAD_ERROR", e.toString());
				cfgTuner.addParameter("ERROR", cfgTuner.getParameter("ERROR") + " / " + e.toString());
				e.printStackTrace(System.out);
			}
			cfgTuner.outCustomSection("report footer",out);
		}

	public void storeToDB(FileContent fc) throws Exception
	{ int pieceLength = cfgTuner.getIntParameter("PieceLength");
		int numPieces = fc.getNumPieces(pieceLength);
		byte[] piece;
		Connection con = dbUtil.getConnection();
		String sql = StrUtil.strFromArray(cfgTuner.getCustomSection("StoreFileSQL"));
		IOUtil.writeLogLn("<b>Upload file '" + cfgTuner.getParameter("FILE_1") + "'</b><br>" + sql, rm);
		//        System.out.println("Store SQL:" + sql);
		PreparedStatement stmt = con.prepareStatement (sql);
		for (int i=0; i< numPieces; i++)
		{ piece = fc.getPiece(i,pieceLength);
			java.sql.Blob bl = new SerialBlob(piece);  
			stmt.setInt( 1, i );
			stmt.setBlob( 2, bl );
			int numRec = stmt.executeUpdate();
			if (numRec == 1)
			{ IOUtil.writeLogLn("Upload piece " + Integer.toString(i) + "; " + piece.length + "b. - OK", rm);
				System.out.println("Upload piece " + Integer.toString(i) + "; " + piece.length + "b. - OK");
			}
			else
			{ cfgTuner.addParameter("UPLOAD_ERROR","Number inserted records:" + numRec);
				cfgTuner.addParameter("ERROR"," Piece:" + Integer.toString(i) + "Number inserted records:" + numRec);
				IOUtil.writeLogLn("***** Upload file: Piece:" + Integer.toString(i) + "; number inserted records:" + numRec, rm);
			}
		}
		stmt.close();
		con.commit();
	}

//	public void storeToDB(InputStream inp) throws Exception
	public void storeToDB(BufferedInputStream inp) throws Exception
	{ 
		int pieceLength = cfgTuner.getIntParameter("PieceLength");
		byte[] piece = new byte[pieceLength];
	  int bufPos = 0;
		Connection con = dbUtil.getConnection();
		String sql = StrUtil.strFromArray(cfgTuner.getCustomSection("StoreBFileSQL"));
		IOUtil.writeLogLn("<b>Upload file '" + cfgTuner.getParameter("BFILE_1") + "'</b><br>" + sql, rm);
	 System.out.println("Upload file '" + cfgTuner.getParameter("BFILE_1") + "'");
// System.out.println("Store SQL:" + sql);
		PreparedStatement stmt = con.prepareStatement (sql);
		int numBytes = 1;
		int pieceNr = 0;
		long fileSize = 0;
		
	  while (numBytes > 0)
	  { while (bufPos < pieceLength && numBytes > 0)
	    { numBytes = inp.read(piece, bufPos, pieceLength-bufPos);
	      if (numBytes > 0)
	        bufPos += numBytes;
	      System.out.print(numBytes +".");
	    }
	    System.out.println("pieceNr:" + pieceNr +"; bufPos=" + bufPos);
//	    java.sql.Blob bl = new SerialBlob(piece);
			java.sql.Blob bl = null;
	    if (bufPos > 0)
			{	if (bufPos < pieceLength)
				{	byte[] b = new byte[bufPos];
					for (int i=0; i<bufPos; i++)
						b[i] = piece[i];
					bl = new SerialBlob(b);
//					bl.setBytes(1, piece, 0, bufPos-1);
				}
				else
					bl = new SerialBlob(piece);
			  fileSize += bufPos;
			  stmt.setInt( 1, pieceNr++ );
			  stmt.setBlob( 2, bl );
			  int numRec = stmt.executeUpdate();
			  con.commit();
			  stmt.clearParameters();
			  bufPos = 0;
			  if (numRec == 1)
			  { IOUtil.writeLogLn("Upload piece " + Integer.toString(pieceNr) + "; " + bl.length() + "b. - OK", rm);
			  }
			  else
			  { cfgTuner.addParameter("UPLOAD_ERROR","Number inserted records:" + numRec);
			    cfgTuner.addParameter("ERROR"," Piece:" + Integer.toString(pieceNr) + "Number inserted records:" + numRec);
			    IOUtil.writeLogLn("***** Upload file: Piece:" + Integer.toString(pieceNr) + "; number inserted records:" + numRec, rm);
			  }
			}
	  }

		stmt.close();
		con.commit();
		inp.close();
		cfgTuner.addParameter("FILE_SIZE", Long.toString(fileSize));
}
	
/*	
  FOR ORACLE:
 Connection con = dbUtil.getConnection();
			  PreparedStatement stmt = con.prepareStatement (sql);
			  oracle.sql.BLOB bl = oracle.sql.BLOB.createTemporary( con, false, oracle.sql.BLOB.DURATION_SESSION );
        bl.putBytes( 1,fc.getBytes() );				
				stmt.setBlob( 1, bl );
//			    stmt.executeQuery();
					int numRec = stmt.executeUpdate();
	/**/
}