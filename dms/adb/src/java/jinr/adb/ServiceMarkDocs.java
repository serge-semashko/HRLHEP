package jinr.adb;
// =================== ÏÎÊÀ ÍÅ ÈÑÏÎËÜÇÓÅÒÑß ===================
public class ServiceMarkDocs extends dubna.walt.service.TableServiceSpecial
{
	protected void processRecord_ZZZ() throws Exception
	{
	  super.processRecord();
		if (currentRow >= srn-2 && currentRow < srn+rpp)
		{ for (int colNr = 0; colNr < numSqlColumns; colNr++)
			{ if (record[colNr]== null || record[colNr].length() <1 ) record[colNr]=" ";

				if (cfgTuner.getParameter("of").equals("xlh"))
					record[colNr] = record[colNr].replaceAll("<br>","\n");

				cfgTuner.addParameter(colNames[colNr], record[colNr]);
				if (record[colNr].indexOf("$CALL_SERVICE") != 0)
				{ cfgTuner.deleteParameter("subservice");
					// ========= äîáàâëåíî 20.09.2005, íî íå ðàáîòàåò
					try
					{ //if (currentRow >= srn-2 && currentRow < srn+rpp-2)
							if (totalRow != null && makeTotalsForCols.indexOf("," + colNames[colNr] + ",") >=0 )
							{ 
								cell.setValue(record[colNr]);        
								colTotals[colNr] += cell.getDValue();
								if (makeSubtotals)
									colSubtotals[colNr] += cell.getDValue();
							}
					} catch (Exception ex) { /* we don't care, if the value is not numeric */ }
					// ========================
				}
				else
				{ cfgTuner.addParameter("subservice", record[colNr].substring(14));
					record[colNr] = "";
				}
			}
			if (terminated) return;
			cfgTuner.addParameter("oddRow", Integer.toString(currentRow % 2));
			cfgTuner.addParameter("currentRow", Integer.toString(currentRow+1));
			cfgTuner.outCustomSection("item", out);
		}
	}

}