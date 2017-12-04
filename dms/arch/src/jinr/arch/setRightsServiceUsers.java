package jinr.arch;

import dubna.walt.service.Service;
import java.util.Vector;
import java.sql.*;


public class setRightsServiceUsers extends Service
{
	//private int curUSERID = -1;

	public void start () throws Exception
	{
		cfgTuner.outCustomSection( "report", out );
		updateDbRights();
		cfgTuner.outCustomSection( "Rights Changes", out );
	}

	private void updateDbRights () throws Exception
	{
		Vector userRights = getUserRights();
		Vector curRights = parseRights( getArrayRights() );

		boolean isDone = false;
		if( curRights != null )
		{
			for( int i = 0; i < curRights.size(); i++ )
			{
				String[] curS = ( String[] ) curRights.elementAt( i );
				isDone = false;
				if( curS[0].equalsIgnoreCase( "n" ) )
					curS[0] = String.valueOf( cfgTuner.getIntParameter( "GROUP_ID" ) );
				if( curS[1].equalsIgnoreCase( "n" ) )
					curS[1] = String.valueOf( cfgTuner.getIntParameter( "ROLE_ID" ) );

				for( int j = 0; j < userRights.size(); j++ )
				{
					String[] userS = ( String[] ) userRights.elementAt( j );
					if( ( userS[0].equalsIgnoreCase( curS[0] ) ) && ( userS[1].equalsIgnoreCase( curS[1] ) ) )
					{
						userRights.removeElementAt( j );
						isDone = true;
						break;
					}
				}
				if( !isDone )
				{
					try
					{ cfgTuner.addParameter("GR_ID", curS[0]);
					  cfgTuner.addParameter("ROL_ID", curS[1]);
						getData("add Rights SQL");
					}
					catch( Exception e )
					{
						cfgTuner.addParameter( "PLSQL_ERROR", e.toString() );
						e.printStackTrace( System.out );
					}
				}
			}
		}
		if( !userRights.isEmpty() )
		{
			String whereConditionR = "";
			String whereConditionG = "";
			for( int i = 0; i < userRights.size(); i++ )
			{
				String[] userS = ( String[] ) userRights.elementAt( i );
				whereConditionG += userS[0] + ",";
				whereConditionR += userS[1] + ",";
			}
			cfgTuner.addParameter("whereConditionG", whereConditionG.substring( 0, whereConditionG.length() - 1 ));
			cfgTuner.addParameter("whereConditionR", whereConditionR.substring( 0, whereConditionR.length() - 1 ));
		  getData("remove Rights SQL");
		}
	}

	/**
    * Получает права по ID пользователя из базы данных
    * @return {@link Vector} массивов строк из 2 элементов <br>
    *         String[0] - G_ID, <br>
    *         String[1] - R_ID<br>
    */
	private Vector getUserRights () throws Exception
	{
		Vector v = new Vector();
		String[] s = null;
		String sql = getSQL( "getUserRightsSQL" );
		ResultSet res = dbUtil.getResults( sql );
		while( res.next() )
		{
			s = new String[2];
			s[0] = res.getString( 1 ); //G_ID
			s[1] = res.getString( 2 ); //R_ID
			v.add( s );
		}
		res.close();
		return v;
	}

	/**
     * @param r массив строк полученых от getArrayRights
     * @return {@link Vector} массивов строк из 3 элементов String[0] - G_ID, String[1] - R_ID, String[2] - Right
     */
	public Vector parseRights ( String[] r )
	{
		if( r == null )
			return null;
		String[] s;
		Vector v = new Vector();
		boolean isAdd = true;
		for( int i = 0; i < r.length; i++ )
		{
			s = new String[3];
			if( ( r[i].toLowerCase().indexOf( "uright", 0 ) == 0 ) )
			{
				s[0] = r[i].substring( 7, r[i].indexOf( "]" ) );
				s[1] = r[i].substring( r[i].indexOf( "]" ) + 2, r[i].indexOf( "]", r[i].indexOf( "]" ) + 2 ) );
				s[2] = r[i].substring( r[i].indexOf( "=" ) + 1, r[i].length() );
			}
			else
			{ // if (r[i].toLowerCase().indexOf("newuright",0)==0)) {
				s[0] = r[i].substring( 10, r[i].indexOf( "]" ) );
				s[1] = r[i].substring( r[i].indexOf( "]" ) + 2, r[i].indexOf( "]", r[i].indexOf( "]" ) + 2 ) );
				s[2] = r[i].substring( r[i].indexOf( "=" ) + 1, r[i].length() );
			}

			if( isAdd )
				v.addElement( s );
		}
		return v;
	}


	/**
     * Получает из cfgTuner параметры вида right[G][R]=V, <br>
     * где <ul><li><b>G</b> - ID группы <br>
     *     <li><b>R</b> - ID роли <br>
     *     <li><b>V</b> - Значение права</ul>
     * @return массив строк вида right[G][R]=V
     */
	public String[] getArrayRights ()
	{
		String[] arr = cfgTuner.getParameters();
		Vector v = new Vector();
		for( int i = 0; i < arr.length; i++ )
		{
			if( ( arr[i].toLowerCase().indexOf( "uright", 0 ) == 0 ) || ( arr[i].toLowerCase().indexOf( "newuright", 0 ) == 0 ) )
			{
				v.addElement( arr[i] );
			}
		}
		if( v.size() == 0 )
			return null;
		String[] vArray = new String[v.size()];
		v.copyInto( vArray );
		return vArray;
	}

}
