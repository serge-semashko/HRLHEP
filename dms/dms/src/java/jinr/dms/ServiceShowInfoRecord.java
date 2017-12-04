package jinr.dms;

import java.sql.ResultSet;
import dubna.walt.util.DBUtil;
import dubna.walt.util.IOUtil;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.util.Vector;

/**
 * Сервис используется для вывода справочника в pop-up окно для выбора записи
 *
 */
public class ServiceShowInfoRecord extends ServiceShowInfoData
{


	/**
	 * Формирование, выполнение запроса в базу
	 * и вывод HTML-таблицы с одной записью справочника
	 *
	 * @throws Exception
	 */
	protected void showInfoRecords () throws Exception
	{
		// конструируем SQL-запрос в базу
		String fields = cfgTuner.getParameter( "FIELDS" );
		fields = fields.substring( 0, fields.length() - 1 );


				//	запрос на выборку записи
				String s = "select " + cfgTuner.getParameter( "FIELDS" ) + cfgTuner.getParameter( "SYS_FIELDS" ) 
					+ " from " + cfgTuner.getParameter( "TABLE_NAME" )
					+ " " + cfgTuner.getParameter( "CRITERIA" );
				IOUtil.writeLogLn( "+++ GET RECORD SQL: '" + s + "'", rm );
				ResultSet r = dbUtil.getResults( s ); // выполняем запрос
				ResultSetMetaData metaData = r.getMetaData();
				numCols = metaData.getColumnCount();

				if( r.next() )
				{
					String record = "";
					String val = "";
					String b1 = "<b>";
				  String b2 = "</b>";
					for( int i = 1; i <= numFields; i++ ) // цикл по полям записи
					{
						if( fields_types[i - 1].equals( "int" ) )
						{
							val = Integer.toString( r.getInt( i ) );
							record += "<tr><td class='right'>" + val + "</td></tr>";
						}
						else if( fields_types[i - 1].equals( "varchar" ) )
						{
							val = r.getString( i );
							record += "<tr><td>" + b1 + val + b2 + "</td></tr>";
						}
						else
							System.out.println( " !!!!! UNKNOWN TYPE: " + fields_types[i - 1] );
						if( i == 1 )
							cfgTuner.addParameter( "returnValue", val );
						b1 = ""; b2 = "";
					}
					cfgTuner.addParameter( "returnId", Integer.toString( r.getInt( numFields + 1 ) ) );
					cfgTuner.addParameter( "record", record ); // заносим в Tuner полученную строку
				IOUtil.writeLogLn( "+++ RESULT: <xmp>'" + record + "'</xmp>", rm );
				for( int i = numFields + 1; i <= numCols; i++ ) // выбираем служебные (не отображаемые) поля
						cfgTuner.addParameter( metaData.getColumnLabel( i ), r.getString( i ) ); // и пихаем их в Tuner
					cfgTuner.outCustomSection( "item", out ); // выводим строку
				}
				r.close();
	}

}
