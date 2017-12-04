package jinr.adb;

import java.sql.ResultSet;
import dubna.walt.util.StrUtil;
//import java.nio.charset.Charset; import java.util.*; import java.io.*;

public class ServiceListFirst extends dubna.walt.service.TableServiceSimple
{

protected void outSubtotals() throws Exception
{ /**/
  if (subtotalRow == null 
  || colSubtotals == null) return;

  boolean flush = false;
  int colspan = 0;
  double plan = 0.;
  double payed = 0.;
  
  cell.setValue("");
  
  String subtotRowLabel = cfgTuner.getParameter("subtotRowLabel");
  if (subtotRowLabel.length() < 2) subtotRowLabel = "Итого";
  subtotRowLabel += oldKeyValue;

  cell.setAttr("");
  
  for (int colNr = 0; colNr < numSqlColumns; colNr++)
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
      if (colNames[colNr].equals("LIMIT"))
        plan = colSubtotals[colNr];
      else if (colNames[colNr].equals("PAYED"))
        payed = colSubtotals[colNr];
      if (colNames[colNr].equals("PS"))
      {
        colSubtotals[colNr] = (payed / plan ) *100.;
        cell.setValue("<center><b>" + StrUtil.formatDouble(colSubtotals[colNr], 1, "&nbsp;") + "%" );
      }
      else       
        cell.setValue("<b>--" + Double.toString(colSubtotals[colNr]) + "--</b>");
      
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
  out.println("<tr bgcolor=white><td colspan=" + Integer.toString(numSqlColumns) 
        + ">&nbsp;</td></tr>");
/**/
}

protected void outColTotals() throws Exception
{ /**/
  if (totalRow == null) return;
  outSubtotals();
  
  double plan = 0.;
  double payed = 0.;
  boolean flush = false;
  int colspan = 0;
  cell.setValue("");
  String totRowLabel = cfgTuner.getParameter("totRowLabel");
//  if (totRowLabel.length() < 2) totRowLabel = "Total";
  if (totRowLabel.length() < 2) totRowLabel = "Всего";
  
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
      if (colNames[colNr].equals("LIMIT"))
        plan = colTotals[colNr];
      else if (colNames[colNr].equals("PAYED"))
        payed = colTotals[colNr];
      if (colNames[colNr].equals("PS"))
      {
        colTotals[colNr] = (payed / plan ) *100.;
        cell.setValue("<center><b>" + StrUtil.formatDouble(colTotals[colNr], 1, "&nbsp;") + "%" );
      }
      else       
        cell.setValue("<b>--" + Double.toString(colTotals[colNr]) + "--</b>");
      
//      cell.setValue(Double.toString(colTotals[colNr]));

      totalRow.addValue(cell);
      cell.setValue("");
      colspan = 0;
      flush = false;      
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
    totalRow.addValue(cell);
  }

  // Output the total row 
  totalRow.setAttr(tableTuner.getParameter("totalBgColor"));
  out.println(totalRow.toHTML());
  /**/
}



}