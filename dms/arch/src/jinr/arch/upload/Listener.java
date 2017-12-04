package jinr.arch.upload;
import java.io.*;
import java.net.*;
import java.sql.*;
import dubna.walt.util.ResourceManager;

class Listener implements Runnable
{
	private ServerSocket server;
	protected boolean running = true;
	private ResourceManager rm = null;
	private UploadServlet servlet=null;

public Listener(ResourceManager rm)
	{ this.rm = rm;
	  System.out.println("INIT: RM:" + rm);
	  servlet = (UploadServlet) rm.getObject("Servlet");
	}
	
public void stop()
{	running = false;
	try 
	{ server.close();
		System.out.println("server socket closed");
	} catch(Exception e)
	{ e.printStackTrace(System.out);
	}
}
public void run()
{ 	try
		{	System.out.println("Open serversocket:" + Integer.parseInt(rm.getString("UploadServletPort")));
			server = new ServerSocket(Integer.parseInt(rm.getString("UploadServletPort")),100);   
			System.out.println("serversocket created");
		} catch(Exception e)
		{ e.printStackTrace(System.out);
		  servlet.listener = null;
			stop();
			return;
		}
		SendObj posted = null;
		Socket socket = null;
		ObjectInputStream input = null;
		ObjectOutputStream output = null;
		int numFiles=0;
		while(running)
		{ try
			{	socket = server.accept();  		
				input = new ObjectInputStream(socket.getInputStream());                
			  numFiles = input.readInt();
				for(int i = 0; i < numFiles; i++)
				{	posted = (SendObj)input.readObject(); 
					if (posted != null)
					{	// System.out.println("Received:" + posted.name);
						StoreData(posted);
					}
				}
//				 System.out.println("Open output stream...");
				 output = new ObjectOutputStream(socket.getOutputStream());
//			   System.out.println("Write int..." + count_objects);
				 output.writeInt(numFiles);
			   output.flush();
				 input.close(); output.close(); socket.close();
				 input=null; output=null; socket=null; posted = null;
//			   System.out.println("DONE!!!");
			 } catch(Exception e)
			 { e.printStackTrace(System.out);
			   servlet.listener = null;
				 stop();
			   return;
			 }
			 finally
			 {
				 try { input.close(); } catch (Exception e) {}
			   try { output.close(); } catch (Exception e) {}
			   try { socket.close();  } catch (Exception e) {}
			   input=null; output=null; socket=null; posted = null;
			 }
		}            
}


public void StoreData ( SendObj posted )
{
	//     SerialBlob bl = null;
	//     Blob bl = null;
//	oracle.sql.BLOB bl = null;
	try
	{
		//            Class.forName ("oracle.jdbc.driver.OracleDriver");
		DriverManager.registerDriver( new oracle.jdbc.driver.OracleDriver() );
		Connection con = DriverManager.getConnection
		( 	rm.getString( "connString" )
			, rm.getString( "usr" )
			, rm.getString( "pw" )
		);
	  String sql = "select nvl(max(PAGE_NR), 0) + 1 from doc_data where doc_id=" + posted.id;
//		System.out.println(sql);
	  Statement st = con.createStatement();
	  ResultSet r=st.executeQuery(sql);
	  int pageNr = 1;
		if (r.next())
			pageNr = r.getInt(1);
		r.close();

		oracle.sql.BLOB bl = oracle.sql.BLOB.createTemporary( con, false, oracle.sql.BLOB.DURATION_SESSION );
		//     bl.setBytes(posted.b);
		bl.putBytes( 1, posted.b );
		//          bl = new SerialBlob(posted.b);
		String fn = posted.name;
	  fn = fn.substring(fn.lastIndexOf("\\") + 1);
		String ext = fn.substring(fn.lastIndexOf(".") + 1).toLowerCase();
		System.out.println("Store file: doc ID:" + posted.id + "; Page:" + pageNr 
			+ "; file:" + posted.name + "; size:" + posted.b.length + "; ext.:" + ext);
		PreparedStatement stmt = con.prepareStatement
			( "INSERT INTO DOC_DATA (DOC_ID, PAGE_NR, PAGE_DATA, FILE_NAME, EXT) VALUES(?, ?, ?, ?, ?)" );
		stmt.setInt( 1, posted.id );
		stmt.setInt( 2, pageNr );
		stmt.setBlob( 3, bl );
		stmt.setString( 4, fn);
	  stmt.setString( 5, ext);
		stmt.executeQuery();
		stmt.close();
		con.close();
	  System.out.println("Store OK");
	}
	catch( Exception ee )
	{
		ee.printStackTrace( System.out );
	}
}


}
