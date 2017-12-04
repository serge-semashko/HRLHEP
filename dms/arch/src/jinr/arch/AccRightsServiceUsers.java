package jinr.arch;



public class AccRightsServiceUsers extends dubna.walt.service.TableServiceSpecial
{


	protected void processRecord () throws Exception
	{
		boolean isItem = false;
		boolean isItem_end = false;
		boolean isItem_begin = false;
		//if (cfgTuner.getParameter("USER_ID").equalsIgnoreCase("")) return;

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
				if( i >= 1 )
					cfgTuner.addParameter( "R", "CHECKED" );
				else
					cfgTuner.addParameter( "R", "" );
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
			//if (!isItem_end) {
			cfgTuner.addParameter( "p_" + colNames[colNr], cfgTuner.getParameter( colNames[colNr] ) );
			//}
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

}
