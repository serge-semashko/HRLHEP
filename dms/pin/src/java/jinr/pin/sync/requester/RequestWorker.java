package jinr.pin.sync.requester;

import java.io.ObjectInputStream;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import jinr.pin.sync.ConnectionHelper;
import jinr.pin.sync.KeysHelper;
import jinr.pin.sync.model.CommonRequest;
import jinr.pin.sync.model.CommonResponse;

public class RequestWorker implements Runnable {
	private static final String QUEUE_TABLE_NAME = "sync_request_queue";
	private ConnectionHelper ch;
	ScheduledExecutorService executorService;
	private KeysHelper keysHelper;

	public RequestWorker(ConnectionHelper ch) throws SQLException {
		this.ch = ch;
		keysHelper = new KeysHelper(ch);
		
		checkDatabase();
		
		initExecutorService();
	}

	private void initExecutorService() {
		executorService = Executors.newScheduledThreadPool(1);
		executorService.scheduleAtFixedRate(this, 0, 60, TimeUnit.SECONDS);
	}

	private void checkDatabase() throws SQLException {
		Connection con = null;
		try {
			con = ch.getConnection();
			
			ResultSet tables = con.getMetaData().getTables(null, null, QUEUE_TABLE_NAME, null);
			if(!tables.next()) createQueueTable(con);
			ch.closeSql(tables, null, null);
			
		} catch (Throwable e) {
			throw new RuntimeException(e);
		} finally {
			ch.closeSql(null, null, con);
		}
	}
	
	private void createQueueTable(Connection con) throws SQLException {
		PreparedStatement st = con.prepareStatement(
			"CREATE TABLE " + QUEUE_TABLE_NAME + "(" +
				"id VARCHAR(100) NOT NULL, " +
				"time_to_update TIMESTAMP NOT NULL DEFAULT NOW(), " +
				"request BLOB NOT NULL, " +
				"response BLOB, " +
				"tries_count INT NOT NULL DEFAULT 0, " +
				"PRIMARY KEY (id)" +
			")");
		
		st.executeUpdate();
		ch.closeSql(null, st, null);
		
		st = con.prepareStatement("CREATE INDEX " + QUEUE_TABLE_NAME + "_time_idx ON " + QUEUE_TABLE_NAME + " (time_to_update)");
		st.executeUpdate();
		ch.closeSql(null, st, null);
		
	}

	public void close() {
		executorService.shutdown();
	}

	public void addRequest(String url, CommonRequest<?> request) {
		request.url = url;
		
		Connection con = null;
		try {
			con = ch.getConnection();
			
			PreparedStatement st = con.prepareStatement(
				"INSERT INTO " + QUEUE_TABLE_NAME +
				" (id, request) VALUES(?, ?)"
			);
			
			st.setString(1, request.id);
			st.setObject(2, request);

			st.executeUpdate();
			
			ch.closeSql(null, st, null);
			
			executorService.execute(this);
			
		} catch (Throwable e) {
			throw new RuntimeException(e);
		} finally {
			ch.closeSql(null, null, con);
		}
	}

	public void run() {
		doUpdate();
	}

	private void doUpdate() {
		Connection con = null;
		try {
			con = ch.getConnection();
			
			
			PreparedStatement st = con.prepareStatement(
				"SELECT request FROM " + QUEUE_TABLE_NAME +
				" WHERE time_to_update<=NOW() AND tries_count<3"
			);
			
			ResultSet res = st.executeQuery();
			
			while(res.next()) {
				CommonRequest<?> request = (CommonRequest<?>)new ObjectInputStream(res.getBinaryStream(1)).readObject();
				Object response;
				try {
					response = makeRequest(request);
					try {
						request.onResponse((CommonResponse) response);
					}catch(Throwable t) {
						t.printStackTrace();
					}
				}catch(Throwable e) {
					response = e;
					try {
						request.onResponseError(e);
					}catch(Throwable t) {
						t.printStackTrace();
					}
				}
				
				PreparedStatement upst = con.prepareStatement(
					"UPDATE " + QUEUE_TABLE_NAME +
					" SET tries_count=tries_count+1" +
					"     , response=? " +
					"     , time_to_update=? " +
					" WHERE id=?"
				);
				
				upst.setObject(1, response);
				upst.setTimestamp(2, new Timestamp(System.currentTimeMillis() + 60*60*1000));
				upst.setString(3, request.id);
				
				upst.executeUpdate();

			}
			
			ch.closeSql(res, st, null);
			
		} catch (Throwable e) {
			e.printStackTrace();
		} finally {
			ch.closeSql(null, null, con);
		}
	}

	private CommonResponse makeRequest(CommonRequest<?> request) throws Exception {
		return new RequestHelper(keysHelper).makeRequest(request);
	}
}
