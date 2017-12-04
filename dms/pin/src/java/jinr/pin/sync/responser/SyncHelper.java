package jinr.pin.sync.responser;

import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.sql.Connection;
import java.sql.Statement;

import jinr.pin.sync.model.SerializableResultSet;

public class SyncHelper {
	private ObjectInputStream in;
	private ObjectOutputStream out;
	
	private String requestId;
	private String requestTitle;
	private Connection con;
	
	public SyncHelper(ObjectInputStream in, ObjectOutputStream out, Connection con) throws Exception {
		this.in = in;
		this.out = out;
		this.con = con;
		
		doWork();
	}

	private void doWork() throws Exception {
		requestId = (String)in.readObject();
		requestTitle = (String)in.readObject();
		
		out.writeObject(requestId);
		out.writeObject(requestTitle);
		int commandsCount = in.readInt();
		out.writeInt(commandsCount);
		
		while(--commandsCount >= 0) {
			String commandKey = (String)in.readObject();
			String sql = (String)in.readObject();
			
			sql = sql.trim();
			boolean update = !
				(sql.toLowerCase().startsWith("select") || sql.toLowerCase().startsWith("show"));
			
			out.writeObject(commandKey);
			
			Statement st = con.createStatement();

			if(update) {
				out.writeObject(st.executeUpdate(sql)); // Integer
			} else {
				out.writeObject(new SerializableResultSet(st.executeQuery(sql)));
			}
			
			st.close();
		}
	}
}
