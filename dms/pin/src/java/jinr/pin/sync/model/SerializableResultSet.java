package jinr.pin.sync.model;

import java.io.IOException;
import java.io.InputStream;
import java.io.Reader;
import java.io.Serializable;
import java.math.BigDecimal;
import java.net.URL;
import java.sql.Array;
import java.sql.Blob;
import java.sql.Clob;
import java.sql.Date;
import java.sql.NClob;
import java.sql.Ref;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.RowId;
import java.sql.SQLException;
import java.sql.SQLWarning;
import java.sql.SQLXML;
import java.sql.Statement;
import java.sql.Time;
import java.sql.Timestamp;
import java.util.Calendar;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.Vector;

/*
 * Раскомментируйте импорты и методы в самом конце, чтобы компилировалось под Java6 
 */
/*/
import java.sql.NClob;
import java.sql.RowId;
import java.sql.SQLXML;
// */

/*
 * Раскомментируйте импорты и методы в самом конце, чтобы компилировалось под Java6 
 */
public class SerializableResultSet implements ResultSet, Serializable {
	private static final long serialVersionUID = -4056896672955795129L;

	Map<String, Integer> columns;
	List<Object[]> rows;
	transient int rowIndex = -1;
	String query;
	SerializableResultSetMetaData metaData;
	
	public SerializableResultSet() {
	}
	
	private void writeObject(java.io.ObjectOutputStream out) throws IOException {
		out.writeObject(columns);
		out.writeObject(rows);
		out.writeObject(query);
		out.writeObject(metaData);
	}

	private void readObject(java.io.ObjectInputStream in) throws IOException,
			ClassNotFoundException {
		columns  = (Map<String, Integer>) in.readObject();
	  rows     = (List) in.readObject();
//		rows     = (List<Object[]>) in.readObject(); //SK 01.04.09 - compile error
		query    = (String) in.readObject();
		metaData = (SerializableResultSetMetaData) in.readObject();
		rowIndex = -1;
	}
	
	public SerializableResultSet(ResultSet rs) throws SQLException {
		query = rs.getStatement().toString();
		
		metaData = new SerializableResultSetMetaData(rs.getMetaData());
		
		int cc = metaData.getColumnCount();
		
		columns = new TreeMap<String, Integer>();
		rows = new Vector<Object[]>();
		
		for(int i=1;i<=cc; i++) {
			columns.put(metaData.getColumnName(i), i);
		}
		
		while(rs.next()) {
			Object[] row = new Object[cc];
			for(int i=0; i<cc; i++) row[i] = rs.getObject(i+1);
			rows.add(row);
		}
	}
	
	public String getQuery() {
		return query;
	}
	
	public boolean absolute(int row) throws SQLException {
		if(row<0) row = rows.size()+row;
		if(row<0 || row>=rows.size()) throw new SQLException("Invalid row number: " + row);
		rowIndex = row;
		return true;
	}

	public void afterLast() throws SQLException {
		throw new SQLException("Not supported");
		
	}

	public void beforeFirst() throws SQLException {
		throw new SQLException("Not supported");
		
	}

	public void cancelRowUpdates() throws SQLException {
		throw new SQLException("Not supported");
		
	}

	public void clearWarnings() throws SQLException {
		throw new SQLException("Not supported");
		
	}

	public void close() throws SQLException {
	}

	public void deleteRow() throws SQLException {
		throw new SQLException("Not supported");
		
	}

	public int findColumn(String columnLabel) throws SQLException {
		Integer i = columns.get(columnLabel);
		if(null==i) throw new SQLException("No such column: " + columnLabel);
		return i;
	}

	public boolean first() throws SQLException {
		return absolute(0);
	}

	public Array getArray(int columnIndex) throws SQLException {
		throw new SQLException("Not supported");
	}

	public Array getArray(String columnLabel) throws SQLException {
		throw new SQLException("Not supported");
	}

	public InputStream getAsciiStream(int columnIndex) throws SQLException {
		throw new SQLException("Not supported");
	}

	public InputStream getAsciiStream(String columnLabel) throws SQLException {
		throw new SQLException("Not supported");
	}

	public BigDecimal getBigDecimal(int columnIndex) throws SQLException {
		return (BigDecimal)rows.get(rowIndex)[columnIndex-1];
	}

	public BigDecimal getBigDecimal(String columnLabel) throws SQLException {
		return (BigDecimal)rows.get(rowIndex)[findColumn(columnLabel)-1];
	}

	public BigDecimal getBigDecimal(int columnIndex, int scale) throws SQLException {
		return (BigDecimal)rows.get(rowIndex)[columnIndex-1];
	}

	public BigDecimal getBigDecimal(String columnLabel, int scale) throws SQLException {
		return (BigDecimal)rows.get(rowIndex)[findColumn(columnLabel)-1];
	}

	public InputStream getBinaryStream(int columnIndex) throws SQLException {
		throw new SQLException("Not supported");
	}

	public InputStream getBinaryStream(String columnLabel) throws SQLException {
		throw new SQLException("Not supported");
	}

	public Blob getBlob(int columnIndex) throws SQLException {
		throw new SQLException("Not supported");
	}

	public Blob getBlob(String columnLabel) throws SQLException {
		throw new SQLException("Not supported");
	}

	public boolean getBoolean(int columnIndex) throws SQLException {
		Object o = rows.get(rowIndex)[columnIndex-1];
		return o!=null && (o.toString().equals("1")||o.toString().equals("true"));
	}

	public boolean getBoolean(String columnLabel) throws SQLException {
		return getBoolean(findColumn(columnLabel));
	}

	public byte getByte(int columnIndex) throws SQLException {
		Number n = (Number)rows.get(rowIndex)[columnIndex-1];
		return null==n? (byte) 0 : n.byteValue();
	}

	public byte getByte(String columnLabel) throws SQLException {
		return getByte(findColumn(columnLabel));
	}

	public byte[] getBytes(int columnIndex) throws SQLException {
		return (byte[]) rows.get(rowIndex)[columnIndex-1];
	}

	public byte[] getBytes(String columnLabel) throws SQLException {
		return getBytes(findColumn(columnLabel));
	}

	public Reader getCharacterStream(int columnIndex) throws SQLException {
		throw new SQLException("Not supported");
	}

	public Reader getCharacterStream(String columnLabel) throws SQLException {
		throw new SQLException("Not supported");
	}

	public Clob getClob(int columnIndex) throws SQLException {
		throw new SQLException("Not supported");
	}

	public Clob getClob(String columnLabel) throws SQLException {
		throw new SQLException("Not supported");
	}

	public int getConcurrency() throws SQLException {
		throw new SQLException("Not supported");
	}

	public String getCursorName() throws SQLException {
		throw new SQLException("Not supported");
	}

	public Date getDate(int columnIndex) throws SQLException {
		return (Date) rows.get(rowIndex)[columnIndex-1];
	}

	public Date getDate(String columnLabel) throws SQLException {
		return getDate(findColumn(columnLabel));
	}

	public Date getDate(int columnIndex, Calendar cal) throws SQLException {
		return getDate(columnIndex);
	}

	public Date getDate(String columnLabel, Calendar cal) throws SQLException {
		return getDate(columnLabel);
	}

	public double getDouble(int columnIndex) throws SQLException {
		Number n = (Number)rows.get(rowIndex)[columnIndex-1];
		return null==n? 0 : n.doubleValue();
	}

	public double getDouble(String columnLabel) throws SQLException {
		return getDouble(findColumn(columnLabel));
	}

	public int getFetchDirection() throws SQLException {
		throw new SQLException("Not supported");
	}

	public int getFetchSize() throws SQLException {
		return rows.size();
	}

	public float getFloat(int columnIndex) throws SQLException {
		Number n = (Number)rows.get(rowIndex)[columnIndex-1];
		return null==n? 0 : n.floatValue();
	}

	public float getFloat(String columnLabel) throws SQLException {
		return getFloat(findColumn(columnLabel));
	}

	public int getHoldability() throws SQLException {
		throw new SQLException("Not supported");
		
	}

	public int getInt(int columnIndex) throws SQLException {
		Number n = (Number)rows.get(rowIndex)[columnIndex-1];
		return null==n? 0 : n.intValue();
	}

	public int getInt(String columnLabel) throws SQLException {
		return getInt(findColumn(columnLabel));
	}

	public long getLong(int columnIndex) throws SQLException {
		Number n = (Number)rows.get(rowIndex)[columnIndex-1];
		return null==n? 0 : n.longValue();
	}

	public long getLong(String columnLabel) throws SQLException {
		return getLong(findColumn(columnLabel));
	}

	public ResultSetMetaData getMetaData() throws SQLException {
		return metaData;
	}

	public Reader getNCharacterStream(int columnIndex) throws SQLException {
		throw new SQLException("Not supported");
		
	}

	public Reader getNCharacterStream(String columnLabel) throws SQLException {
		throw new SQLException("Not supported");
		
	}

	public String getNString(int columnIndex) throws SQLException {
		Object o = rows.get(rowIndex)[columnIndex-1];
		return null==o? null : o.toString();
	}

	public String getNString(String columnLabel) throws SQLException {
		return getNString(findColumn(columnLabel));
	}

	public Object getObject(int columnIndex) throws SQLException {
		return rows.get(rowIndex)[columnIndex-1];
	}

	public Object getObject(String columnLabel) throws SQLException {
		return getObject(findColumn(columnLabel));
	}

	public Object getObject(int arg0, Map<String, Class<?>> arg1) throws SQLException {
		throw new SQLException("Not supported");
	}

	public Object getObject(String arg0, Map<String, Class<?>> arg1) throws SQLException {
		throw new SQLException("Not supported");
	}

	public Ref getRef(int columnIndex) throws SQLException {
		throw new SQLException("Not supported");
	}

	public Ref getRef(String columnLabel) throws SQLException {
		throw new SQLException("Not supported");
	}

	public int getRow() throws SQLException {
		return rowIndex;
	}

	public short getShort(int columnIndex) throws SQLException {
		Number n = (Number)rows.get(rowIndex)[columnIndex-1];
		return null==n? (short) 0 : n.shortValue();
	}

	public short getShort(String columnLabel) throws SQLException {
		return getShort(findColumn(columnLabel));
	}

	public Statement getStatement() throws SQLException {
		throw new SQLException("Not supported");
	}

	public String getString(int columnIndex) throws SQLException {
		return getNString(columnIndex);
	}

	public String getString(String columnLabel) throws SQLException {
		return getString(findColumn(columnLabel));
	}

	public Time getTime(int columnIndex) throws SQLException {
		return (Time) rows.get(rowIndex)[columnIndex-1];
	}

	public Time getTime(String columnLabel) throws SQLException {
		return getTime(findColumn(columnLabel));
	}

	public Time getTime(int columnIndex, Calendar cal) throws SQLException {
		return getTime(columnIndex);
	}

	public Time getTime(String columnLabel, Calendar cal) throws SQLException {
		return getTime(findColumn(columnLabel));
	}

	public Timestamp getTimestamp(int columnIndex) throws SQLException {
		return (Timestamp) rows.get(rowIndex)[columnIndex-1];
	}

	public Timestamp getTimestamp(String columnLabel) throws SQLException {
		return getTimestamp(findColumn(columnLabel));
	}

	public Timestamp getTimestamp(int columnIndex, Calendar cal) throws SQLException {
		return getTimestamp(columnIndex);
	}

	public Timestamp getTimestamp(String columnLabel, Calendar cal) throws SQLException {
		return getTimestamp(findColumn(columnLabel));
	}

	public int getType() throws SQLException {
		return ResultSet.TYPE_SCROLL_INSENSITIVE;
	}

	public URL getURL(int columnIndex) throws SQLException {
		return (URL) rows.get(rowIndex)[columnIndex-1];
	}

	public URL getURL(String columnLabel) throws SQLException {
		return getURL(findColumn(columnLabel));
	}

	public InputStream getUnicodeStream(int columnIndex) throws SQLException {
		throw new SQLException("Not supported");
		
	}

	public InputStream getUnicodeStream(String columnLabel) throws SQLException {
		throw new SQLException("Not supported");
		
	}

	public SQLWarning getWarnings() throws SQLException {
		throw new SQLException("Not supported");
		
	}

	public void insertRow() throws SQLException {
		throw new SQLException("Not supported");
		
	}

	public boolean isAfterLast() throws SQLException {
		throw new SQLException("Not supported");
		
	}

	public boolean isBeforeFirst() throws SQLException {
		throw new SQLException("Not supported");
		
	}

	public boolean isClosed() throws SQLException {
		return false;
	}

	public boolean isFirst() throws SQLException {
		return rowIndex == 0;
	}

	public boolean isLast() throws SQLException {
		return rowIndex == rows.size()-1;
	}

	public boolean last() throws SQLException {
		rowIndex = rows.size() - 1;
		return rowIndex > 0;
	}

	public void moveToCurrentRow() throws SQLException {
		throw new SQLException("Not supported");
		
	}

	public void moveToInsertRow() throws SQLException {
		throw new SQLException("Not supported");
		
	}

	public boolean next() throws SQLException {
		return relative(1);
	}

	public boolean previous() throws SQLException {
		return relative(-1);
	}

	public void refreshRow() throws SQLException {
	}

	public boolean relative(int rws) throws SQLException {
		rowIndex += rws;
		return rowIndex>=0 && rowIndex < rows.size();
	}

	public boolean rowDeleted() throws SQLException {
		throw new SQLException("Not supported");
	}

	public boolean rowInserted() throws SQLException {
		throw new SQLException("Not supported");
		
	}

	public boolean rowUpdated() throws SQLException {
		throw new SQLException("Not supported");
		
	}

	public void setFetchDirection(int direction) throws SQLException {
		throw new SQLException("Not supported");
	}

	public void setFetchSize(int rows) throws SQLException {
		throw new SQLException("Not supported");
	}

	public void updateArray(int columnIndex, Array x) throws SQLException {
		throw new SQLException("Not supported");
	}

	public void updateArray(String columnLabel, Array x) throws SQLException {
		throw new SQLException("Not supported");
	}

	public void updateAsciiStream(int columnIndex, InputStream x) throws SQLException {
		throw new SQLException("Not supported");
		
	}

	public void updateAsciiStream(String columnLabel, InputStream x) throws SQLException {
		throw new SQLException("Not supported");
		
	}

	public void updateAsciiStream(int columnIndex, InputStream x, int length) throws SQLException {
		throw new SQLException("Not supported");
		
	}

	public void updateAsciiStream(String columnLabel, InputStream x, int length) throws SQLException {
		throw new SQLException("Not supported");
		
	}

	public void updateAsciiStream(int columnIndex, InputStream x, long length) throws SQLException {
		throw new SQLException("Not supported");
		
	}

	public void updateAsciiStream(String columnLabel, InputStream x, long length) throws SQLException {
		throw new SQLException("Not supported");
		
	}

	public void updateBigDecimal(int columnIndex, BigDecimal x) throws SQLException {
		throw new SQLException("Not supported");
		
	}

	public void updateBigDecimal(String columnLabel, BigDecimal x) throws SQLException {
		throw new SQLException("Not supported");
		
	}

	public void updateBinaryStream(int columnIndex, InputStream x) throws SQLException {
		throw new SQLException("Not supported");
		
	}

	public void updateBinaryStream(String columnLabel, InputStream x) throws SQLException {
		throw new SQLException("Not supported");
		
	}

	public void updateBinaryStream(int columnIndex, InputStream x, int length) throws SQLException {
		throw new SQLException("Not supported");
		
	}

	public void updateBinaryStream(String columnLabel, InputStream x, int length) throws SQLException {
		throw new SQLException("Not supported");
		
	}

	public void updateBinaryStream(int columnIndex, InputStream x, long length) throws SQLException {
		throw new SQLException("Not supported");
		
	}

	public void updateBinaryStream(String columnLabel, InputStream x, long length) throws SQLException {
		throw new SQLException("Not supported");
		
	}

	public void updateBlob(int columnIndex, Blob x) throws SQLException {
		throw new SQLException("Not supported");
		
	}

	public void updateBlob(String columnLabel, Blob x) throws SQLException {
		throw new SQLException("Not supported");
		
	}

	public void updateBlob(int columnIndex, InputStream inputStream) throws SQLException {
		throw new SQLException("Not supported");
		
	}

	public void updateBlob(String columnLabel, InputStream inputStream) throws SQLException {
		throw new SQLException("Not supported");
		
	}

	public void updateBlob(int columnIndex, InputStream inputStream, long length) throws SQLException {
		throw new SQLException("Not supported");
		
	}

	public void updateBlob(String columnLabel, InputStream inputStream, long length) throws SQLException {
		throw new SQLException("Not supported");
		
	}

	public void updateBoolean(int columnIndex, boolean x) throws SQLException {
		throw new SQLException("Not supported");
		
	}

	public void updateBoolean(String columnLabel, boolean x) throws SQLException {
		throw new SQLException("Not supported");
		
	}

	public void updateByte(int columnIndex, byte x) throws SQLException {
		throw new SQLException("Not supported");
		
	}

	public void updateByte(String columnLabel, byte x) throws SQLException {
		throw new SQLException("Not supported");
		
	}

	public void updateBytes(int columnIndex, byte[] x) throws SQLException {
		throw new SQLException("Not supported");
		
	}

	public void updateBytes(String columnLabel, byte[] x) throws SQLException {
		throw new SQLException("Not supported");
		
	}

	public void updateCharacterStream(int columnIndex, Reader x) throws SQLException {
		throw new SQLException("Not supported");
		
	}

	public void updateCharacterStream(String columnLabel, Reader reader) throws SQLException {
		throw new SQLException("Not supported");
		
	}

	public void updateCharacterStream(int columnIndex, Reader x, int length) throws SQLException {
		throw new SQLException("Not supported");
		
	}

	public void updateCharacterStream(String columnLabel, Reader reader, int length) throws SQLException {
		throw new SQLException("Not supported");
		
	}

	public void updateCharacterStream(int columnIndex, Reader x, long length) throws SQLException {
		throw new SQLException("Not supported");
		
	}

	public void updateCharacterStream(String columnLabel, Reader reader, long length) throws SQLException {
		throw new SQLException("Not supported");
		
	}

	public void updateClob(int columnIndex, Clob x) throws SQLException {
		throw new SQLException("Not supported");
		
	}

	public void updateClob(String columnLabel, Clob x) throws SQLException {
		throw new SQLException("Not supported");
		
	}

	public void updateClob(int columnIndex, Reader reader) throws SQLException {
		throw new SQLException("Not supported");
		
	}

	public void updateClob(String columnLabel, Reader reader) throws SQLException {
		throw new SQLException("Not supported");
		
	}

	public void updateClob(int columnIndex, Reader reader, long length) throws SQLException {
		throw new SQLException("Not supported");
		
	}

	public void updateClob(String columnLabel, Reader reader, long length) throws SQLException {
		throw new SQLException("Not supported");
		
	}

	public void updateDate(int columnIndex, Date x) throws SQLException {
		throw new SQLException("Not supported");
		
	}

	public void updateDate(String columnLabel, Date x) throws SQLException {
		throw new SQLException("Not supported");
		
	}

	public void updateDouble(int columnIndex, double x) throws SQLException {
		throw new SQLException("Not supported");
		
	}

	public void updateDouble(String columnLabel, double x) throws SQLException {
		throw new SQLException("Not supported");
		
	}

	public void updateFloat(int columnIndex, float x) throws SQLException {
		throw new SQLException("Not supported");
		
	}

	public void updateFloat(String columnLabel, float x) throws SQLException {
		throw new SQLException("Not supported");
		
	}

	public void updateInt(int columnIndex, int x) throws SQLException {
		throw new SQLException("Not supported");
		
	}

	public void updateInt(String columnLabel, int x) throws SQLException {
		throw new SQLException("Not supported");
		
	}

	public void updateLong(int columnIndex, long x) throws SQLException {
		throw new SQLException("Not supported");
		
	}

	public void updateLong(String columnLabel, long x) throws SQLException {
		throw new SQLException("Not supported");
		
	}

	public void updateNCharacterStream(int columnIndex, Reader x) throws SQLException {
		throw new SQLException("Not supported");
		
	}

	public void updateNCharacterStream(String columnLabel, Reader reader) throws SQLException {
		throw new SQLException("Not supported");
		
	}

	public void updateNCharacterStream(int columnIndex, Reader x, long length) throws SQLException {
		throw new SQLException("Not supported");
		
	}

	public void updateNCharacterStream(String columnLabel, Reader reader, long length) throws SQLException {
		throw new SQLException("Not supported");
		
	}

	public void updateNClob(int columnIndex, Reader reader) throws SQLException {
		throw new SQLException("Not supported");
		
	}

	public void updateNClob(String columnLabel, Reader reader) throws SQLException {
		throw new SQLException("Not supported");
		
	}

	public void updateNClob(int columnIndex, Reader reader, long length) throws SQLException {
		throw new SQLException("Not supported");
		
	}

	public void updateNClob(String columnLabel, Reader reader, long length) throws SQLException {
		throw new SQLException("Not supported");
		
	}

	public void updateNString(int columnIndex, String nString) throws SQLException {
		throw new SQLException("Not supported");
		
	}

	public void updateNString(String columnLabel, String nString) throws SQLException {
		throw new SQLException("Not supported");
		
	}

	public void updateNull(int columnIndex) throws SQLException {
		throw new SQLException("Not supported");
		
	}

	public void updateNull(String columnLabel) throws SQLException {
		throw new SQLException("Not supported");
		
	}

	public void updateObject(int columnIndex, Object x) throws SQLException {
		throw new SQLException("Not supported");
		
	}

	public void updateObject(String columnLabel, Object x) throws SQLException {
		throw new SQLException("Not supported");
		
	}

	public void updateObject(int columnIndex, Object x, int scaleOrLength) throws SQLException {
		throw new SQLException("Not supported");
		
	}

	public void updateObject(String columnLabel, Object x, int scaleOrLength) throws SQLException {
		throw new SQLException("Not supported");
		
	}

	public void updateRef(int columnIndex, Ref x) throws SQLException {
		throw new SQLException("Not supported");
		
	}

	public void updateRef(String columnLabel, Ref x) throws SQLException {
		throw new SQLException("Not supported");
		
	}

	public void updateRow() throws SQLException {
		throw new SQLException("Not supported");
		
	}

	public void updateShort(int columnIndex, short x) throws SQLException {
		throw new SQLException("Not supported");
		
	}

	public void updateShort(String columnLabel, short x) throws SQLException {
		throw new SQLException("Not supported");
		
	}

	public void updateString(int columnIndex, String x) throws SQLException {
		throw new SQLException("Not supported");
		
	}

	public void updateString(String columnLabel, String x) throws SQLException {
		throw new SQLException("Not supported");
		
	}

	public void updateTime(int columnIndex, Time x) throws SQLException {
		throw new SQLException("Not supported");
		
	}

	public void updateTime(String columnLabel, Time x) throws SQLException {
		throw new SQLException("Not supported");
		
	}

	public void updateTimestamp(int columnIndex, Timestamp x) throws SQLException {
		throw new SQLException("Not supported");
		
	}

	public void updateTimestamp(String columnLabel, Timestamp x) throws SQLException {
		throw new SQLException("Not supported");
		
	}

	public boolean wasNull() throws SQLException {
		throw new SQLException("Not supported");
		
	}

	public boolean isWrapperFor(Class<?> iface) throws SQLException {
		throw new SQLException("Not supported");
		
	}

	public <T> T unwrap(Class<T> iface) throws SQLException {
		throw new SQLException("Not supported");
	}
	
	
	/*
	 * Раскомментируйте импорты и методы в самом конце, чтобы компилировалось под Java6 
	 */
	
	/*/
	public NClob getNClob(int columnIndex) throws SQLException {
		throw new SQLException("Not supported");
		
	}

	public NClob getNClob(String columnLabel) throws SQLException {
		throw new SQLException("Not supported");
		
	}

	public RowId getRowId(int columnIndex) throws SQLException {
		throw new SQLException("Not supported");
	}

	public RowId getRowId(String columnLabel) throws SQLException {
		throw new SQLException("Not supported");
	}

	public SQLXML getSQLXML(int columnIndex) throws SQLException {
		throw new SQLException("Not supported");
	}

	public SQLXML getSQLXML(String columnLabel) throws SQLException {
		throw new SQLException("Not supported");
	}

	public void updateNClob(int columnIndex, NClob nClob) throws SQLException {
		throw new SQLException("Not supported");
		
	}

	public void updateNClob(String columnLabel, NClob nClob) throws SQLException {
		throw new SQLException("Not supported");
		
	}

	public void updateRowId(int columnIndex, RowId x) throws SQLException {
		throw new SQLException("Not supported");
		
	}

	public void updateRowId(String columnLabel, RowId x) throws SQLException {
		throw new SQLException("Not supported");
		
	}

	public void updateSQLXML(int columnIndex, SQLXML xmlObject) throws SQLException {
		throw new SQLException("Not supported");
		
	}

	public void updateSQLXML(String columnLabel, SQLXML xmlObject) throws SQLException {
		throw new SQLException("Not supported");
		
	}
	// */

    @Override
    public RowId getRowId(int columnIndex) throws SQLException {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public RowId getRowId(String columnLabel) throws SQLException {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public void updateRowId(int columnIndex, RowId x) throws SQLException {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public void updateRowId(String columnLabel, RowId x) throws SQLException {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public void updateNClob(int columnIndex, NClob nClob) throws SQLException {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public void updateNClob(String columnLabel, NClob nClob) throws SQLException {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public NClob getNClob(int columnIndex) throws SQLException {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public NClob getNClob(String columnLabel) throws SQLException {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public SQLXML getSQLXML(int columnIndex) throws SQLException {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public SQLXML getSQLXML(String columnLabel) throws SQLException {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public void updateSQLXML(int columnIndex, SQLXML xmlObject) throws SQLException {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public void updateSQLXML(String columnLabel, SQLXML xmlObject) throws SQLException {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public <T> T getObject(int columnIndex, Class<T> type) throws SQLException {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public <T> T getObject(String columnLabel, Class<T> type) throws SQLException {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }
}
