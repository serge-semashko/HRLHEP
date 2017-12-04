package jinr.pin.sync.model;

import java.io.Serializable;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;

public class SerializableResultSetMetaData implements ResultSetMetaData, Serializable {
	private static final long serialVersionUID = -3915549573842753652L;

	String[]  catalogNames;
	String[]  columnClassNames;
	String[]  columnLabels;
	String[]  columnNames;
	String[]  columnTypeNames;
	String[]  schemaNames;
	String[]  tableNames;
	int[]     columnDisplaySize;
	int[]     columnTypes;
	int[]     precisions;
	int[]     scales;
	int[]     nullables;
	boolean[] autoIncrements;
	boolean[] caseSensitives;
	boolean[] currencies;
	boolean[] searchables;
	boolean[] signeds;
	
	public SerializableResultSetMetaData() {
	}
	
	public SerializableResultSetMetaData(ResultSetMetaData d) throws SQLException {
		catalogNames = new String[d.getColumnCount()];
		columnClassNames = new String[d.getColumnCount()];
		columnLabels = new String[d.getColumnCount()];
		columnNames = new String[d.getColumnCount()];
		columnTypeNames = new String[d.getColumnCount()];
		schemaNames = new String[d.getColumnCount()];
		tableNames = new String[d.getColumnCount()];
		columnDisplaySize = new int[d.getColumnCount()];
		columnTypes = new int[d.getColumnCount()];
		precisions = new int[d.getColumnCount()];
		scales = new int[d.getColumnCount()];
		nullables = new int[d.getColumnCount()];
		autoIncrements = new boolean[d.getColumnCount()];
		caseSensitives = new boolean[d.getColumnCount()];
		currencies = new boolean[d.getColumnCount()];
		searchables = new boolean[d.getColumnCount()];
		signeds = new boolean[d.getColumnCount()];
		
		for(int column = 0; column<d.getColumnCount(); column++) {
			catalogNames[column] = d.getCatalogName(column+1);
			columnClassNames[column] = d.getColumnClassName(column+1);
			columnLabels[column] = d.getColumnLabel(column+1);
			columnNames[column] = d.getColumnName(column+1);
			columnTypeNames[column] = d.getColumnTypeName(column+1);
			schemaNames[column] = d.getSchemaName(column+1);
			tableNames[column] = d.getTableName(column+1);
			columnDisplaySize[column] = d.getColumnDisplaySize(column+1);
			columnTypes[column] = d.getColumnType(column+1);
			precisions[column] = d.getPrecision(column+1);
			scales[column] = d.getScale(column+1);
			nullables[column] = d.isNullable(column+1);
			autoIncrements[column] = d.isAutoIncrement(column+1);
			caseSensitives[column] = d.isCaseSensitive(column+1);
			currencies[column] = d.isCurrency(column+1);
			searchables[column] = d.isSearchable(column+1);
			signeds[column] = d.isSigned(column+1);
			
//			System.out.println(columnNames[column]);
		}
	}
	
	
	public String getCatalogName(int column) throws SQLException {
		return catalogNames[column-1];
	}

	public String getColumnClassName(int column) throws SQLException {
		return columnClassNames[column-1];
	}

	public int getColumnCount() throws SQLException {
		return columnClassNames.length;
	}

	public int getColumnDisplaySize(int column) throws SQLException {
		return columnDisplaySize[column-1];
	}

	public String getColumnLabel(int column) throws SQLException {
		return columnLabels[column-1];
	}

	public String getColumnName(int column) throws SQLException {
		return columnNames[column-1];
	}

	public int getColumnType(int column) throws SQLException {
		return columnTypes[column-1];
	}

	public String getColumnTypeName(int column) throws SQLException {
		return columnTypeNames[column-1];
	}

	public int getPrecision(int column) throws SQLException {
		return precisions[column-1];
	}

	public int getScale(int column) throws SQLException {
		return scales[column-1];
	}

	public String getSchemaName(int column) throws SQLException {
		return schemaNames[column-1];
	}

	public String getTableName(int column) throws SQLException {
		return tableNames[column-1];
	}

	public boolean isAutoIncrement(int column) throws SQLException {
		return autoIncrements[column-1];
	}

	public boolean isCaseSensitive(int column) throws SQLException {
		return caseSensitives[column-1];
	}

	public boolean isCurrency(int column) throws SQLException {
		return currencies[column-1];
	}

	public boolean isDefinitelyWritable(int column) throws SQLException {
		return false;
	}

	public int isNullable(int column) throws SQLException {
		return nullables[column-1];
	}

	public boolean isReadOnly(int column) throws SQLException {
		return true;
	}

	public boolean isSearchable(int column) throws SQLException {
		return searchables[column-1];
	}

	public boolean isSigned(int column) throws SQLException {
		return signeds[column-1];
	}

	public boolean isWritable(int column) throws SQLException {
		return false;
	}

	public boolean isWrapperFor(Class<?> iface) throws SQLException {
		return false;
	}

	public <T> T unwrap(Class<T> iface) throws SQLException {
		return null;
	}
}
