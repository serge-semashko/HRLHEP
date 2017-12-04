package jinr.arch;



public class TableServiceSpecial extends dubna.walt.service.TableServiceSpecial
{

	protected void outSubtotals() throws Exception
	{ /**/
		if (subtotalRow == null 
		|| colSubtotals == null) return;

		boolean flush = false;
		int colspan = 0;
		
		cell.setValue("");
		
		String subtotRowLabel = cfgTuner.getParameter("subtotRowLabel");
		if (subtotRowLabel.length() < 2) subtotRowLabel = "Итого:";

		cell.setAttr("");
		
		for (int colNr = 0; colNr < numSqlColumns-numSpecialCols; colNr++)
		{ if (makeTotalsForCols.indexOf("," + colNames[colNr] + ",") >=0 )
			// Get subtotal data 
			{ if (flush)
				// Put the previous cell into the row 
				{ if (colspan > 1)
						cell.addAttr("colspan=" + Integer.toString(colspan));
					subtotalRow.addValue(cell);
					cell.setValue("");
				}
				// Put the new subtotal value 
				cell.setAttr("");
				cell.setValue(Double.toString(colSubtotals[colNr]));
				colSubtotals[colNr] = 0.;
				subtotalRow.addValue(cell);
				cell.setValue("");
				colspan = 0;
				flush = false;      
			}
			else 
			{ if (colNr == 0)
				// Make the 1st cell in the subtotal row 
				{ cell.setValue("<b>" + subtotRowLabel + "</b>&nbsp;");
					cell.setAttr("align=right");
				}
				colspan++;
				flush = true;      
			}
	//    if (colspan > 1)
	 //     cell.addAttr("colspan=" + Integer.toString(colspan));
		}

		// Put the tailing cell
		if (flush)
		{ if (colspan > 1)
				cell.setAttr("colspan=" + Integer.toString(colspan));
			subtotalRow.addValue(cell);
		}

		// Output the subtotal row and reset it 
		subtotalRow.setAttr(tableTuner.getParameter("altBgColorAttr"));
		out.println(subtotalRow.toHTML());
		subtotalRow.setValue("");
	/**/
	}

}