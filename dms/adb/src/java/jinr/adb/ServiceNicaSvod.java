package jinr.adb;

public class ServiceNicaSvod extends dubna.walt.service.TableServiceSpecial
{
protected void outColTotals() throws Exception
{
//	super.outColTotals();
 if (totalRow == null) return;
 outSubtotals();
 if (cfgTuner.enabledOption("hide_totals"))
	 return;
 
 boolean flush = false;
 int colspan = 0;
 cell.setValue(""); 
 cell.setAttr("");
 for (int colNr = 0; colNr < numSqlColumns - numSpecialCols; colNr++)
 { if (makeTotalsForCols.indexOf("," + colNames[colNr] + ",") >=0 )
	 // Get total data 
	 { if (flush)
		 // Put the previous cell into the row 
		 { if (colspan > 1)
				 cell.addAttr("colspan=" + Integer.toString(colspan));
			 totalRow.addValue(cell);
		 }
		 // Put the new subtotal value 
		 cell.setAttr("class=total");
		 cell.setValue(Double.toString(colTotals[colNr]));
		 totalRow.addValue(cell);
		 cell.setValue("");
		 colspan = 0;
		 flush = false;      
	 }
	 else if (cfgTuner.enabledOption(colNames[colNr] + "_TOTAL"))
	 {
		 String[] t_section = cfgTuner.getCustomSection(cfgTuner.getParameter(colNames[colNr] + "_TOTAL"));
		 String s = "";
		 for (int i=0; i<t_section.length; i++)
			s += t_section[i];
	   cell.setAttr("class=total");
//	   cell.addAttr("colspan=3");
	   cell.setValue(s);
	   totalRow.addValue(cell);
	   cell.setValue("");
	   colspan = -10;
	   flush = true;      
	 }
	 else 
	 { if (colNr == 0)
		 // Make the 1st cell in the subtotal row 
		 { cell.setValue("<b><i>" + totRowLabel + ":</i></b>&nbsp;");
			 cell.setAttr("align=right");
		 }
			 
		 colspan++;
		 flush = true;      
	 }
 }

 if (flush)
 // Put the tailing cell
 { if (colspan > 1)
		 cell.setAttr("colspan=" + Integer.toString(colspan));
	 if (colspan > -1)
		totalRow.addValue(cell);
 }

 // Output the total row 
 totalRow.setAttr(tableTuner.getParameter("totalBgColor"));
 out.println(totalRow.toHTML());
 /**/
}

}