package jinr.arch;
import java.sql.ResultSet;
public class AccRightsService extends dubna.walt.service.TableServiceSpecial
{

	public void start () throws Exception
	{
		super.start();
	}

	protected int outTableBody ( ResultSet resultSet ) throws Exception
	{
		if( terminated )
			return 0;
		int numItems = super.outTableBody( resultSet );
		if( terminated )
			return 0;
		setRowLinks( numItems );
		return numItems;
	}

	protected void processRecord () throws Exception
	{
		boolean isItem = false;
		boolean isItem_end = false;
		boolean isItem_begin = false;
		if( cfgTuner.getParameter( "activeNode" ).equalsIgnoreCase( "" ) )
			return;

		for( int colNr = 0; colNr < numSqlColumns; colNr++ )
		{
			if( colNames[colNr].equalsIgnoreCase( cfgTuner.getParameter( "item_title" ) ) )
			{
				int i = 0;
				try
				{
					i = Integer.parseInt( record[colNr] );
				}
				catch( Exception e )
				{
				}
				if( i == 1 || i == 3 )
					cfgTuner.addParameter( "R", "CHECKED" );
				else
					cfgTuner.addParameter( "R", "" );
				if( i >= 2 )
					cfgTuner.addParameter( "W", "CHECKED" );
				else
					cfgTuner.addParameter( "W", "" );
				isItem = true;
			}
			else if( colNames[colNr].equalsIgnoreCase( cfgTuner.getParameter( "item_begin_title" ) ) )
			{
				if( cfgTuner.getParameter( "PrevRoles" ).equalsIgnoreCase( record[colNr] ) )
				{
					isItem = true;
				}
				else
				{
					if( cfgTuner.getParameter( "PrevRoles" ) != "" )
						isItem_end = true;
					cfgTuner.addParameter( "PrevRoles", record[colNr] );
					isItem_begin = true;
				}
			}
			else
			{
				isItem = true;
			}
			cfgTuner.addParameter( colNames[colNr], record[colNr] );
			record[colNr] = "";
		}
		if( terminated )
			return;

		if( isItem_end )
			cfgTuner.outCustomSection( "item end", out );
		if( isItem_begin )
			cfgTuner.outCustomSection( "item begin", out );
		if( isItem )
			cfgTuner.outCustomSection( "item", out );
	}

	protected void outTableHeader ( ResultSet resultSet )
	{
		try
		{
			cfgTuner.outCustomSection( "table header", out );
		}
		catch( Exception e )
		{
		}
		headerRow = null;
	}

}
