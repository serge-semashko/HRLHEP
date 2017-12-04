package jinr.pin.sync;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

/**
 * Простейший класс-помошник с jdbc
 */
public class ConnectionHelper {
	private String dbUrl, dbUser, dbPass;

	public ConnectionHelper(String dbUrl, String dbUser, String dbPass) {
		this.dbUrl = dbUrl;
		this.dbUser = dbUser;
		this.dbPass = dbPass;
	}

	public Connection getConnection() throws SQLException {
		return DriverManager.getConnection(dbUrl, dbUser, dbPass);
	}
	
	public void closeSql(ResultSet res, Statement st, Connection con) {
		if(null==st&&null!=res)  try {st = res.getStatement();}catch(SQLException e) {}
		if(null!=res) try {res.close();}catch(SQLException e) {}
		if(null!=st)  try {st.close();}catch(SQLException e) {}
		if(null!=con) try {con.close();}catch(SQLException e) {}
	}
}
