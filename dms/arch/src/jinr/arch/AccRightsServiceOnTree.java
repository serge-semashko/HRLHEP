package jinr.arch;


import java.sql.ResultSet;



public class AccRightsServiceOnTree extends dubna.walt.service.TableServiceSpecial
{

	protected int outTableBody ( ResultSet resultSet ) throws Exception
	{
		if( terminated )
			return 0;
		int numItems = super.outTableBody( resultSet );
		if( terminated )
			return 0;
//		setRowLinks( numItems );
		return numItems;
	}

	protected void processRecord () throws Exception
	{
		//      if (cfgTuner.getParameter("activeNode").equalsIgnoreCase("")) return;
                cfgTuner.deleteParameter("R");
                cfgTuner.deleteParameter("W");
		for( int colNr = 0; colNr < numSqlColumns; colNr++ )
		{
                    int iRight = 0;
                    if("RIGHTS".equals(colNames[colNr])){
                        iRight=Integer.parseInt(record[colNr]);
                        switch(iRight){
                            case 1:
                                cfgTuner.addParameter( "R", "CHECKED" );
                                cfgTuner.addParameter( "W", "" );
                            break;
                            case 2:
                                cfgTuner.addParameter( "R", "" );
                                cfgTuner.addParameter( "W", "CHECKED" );
                            break;
                            case 3:
                                cfgTuner.addParameter( "R", "CHECKED" );
                                cfgTuner.addParameter( "W", "CHECKED" );
                            break;
                            default:
                               cfgTuner.addParameter( "R", "" );
                               cfgTuner.addParameter( "W", "" );
                            break;
                        }
                    }
                    cfgTuner.addParameter( colNames[colNr], record[colNr] );
                    record[colNr] = "";
                }
		if( terminated ) return;
		cfgTuner.outCustomSection( "item", out );
//		for( int colNr = 0; colNr < numSqlColumns; colNr++ )
//		{                
//			int i = 0;
//			try
//			{
//				i = Integer.parseInt( record[colNr] );
//			}
//			catch( Exception e )
//			{
//			}
//			if( i == 1 || i == 3 )
//				cfgTuner.addParameter( "R", "CHECKED" );
//			else
//				cfgTuner.addParameter( "R", "" );
//			if( i >= 2 )
//				cfgTuner.addParameter( "W", "CHECKED" );
//			else
//				cfgTuner.addParameter( "W", "" );
//			cfgTuner.addParameter( colNames[colNr], record[colNr] );
//			record[colNr] = "";
//		}
//		if( terminated )
//			return;
//
//		cfgTuner.outCustomSection( "item", out );
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
