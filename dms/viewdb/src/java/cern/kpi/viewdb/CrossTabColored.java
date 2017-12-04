package cern.kpi.viewdb;

import java.sql.ResultSet;
import dubna.walt.util.IOUtil;
import dubna.walt.util.DBUtil;


public class CrossTabColored extends dubna.walt.service.CrossTabService{

protected void makeTable() throws Exception
{
//  tableTuner = new Tuner(null, "common/table.cfg", rm.getString("CfgRootPath"), rm);

  getCrossValues();

  ResultSet resultSet = runSQL(sqlSectionName);
  colNames = DBUtil.getColNames(resultSet);
  numSqlColumns = colNames.length;
  crossColIndex = numSqlColumns - 2; // minus crossValue and value fields
//  numTableColumns = numSqlColumns + numCrossValues - 1;  *****++++By me
  numTableColumns = numSqlColumns + numCrossValues - 2;

  initArrays();
  colTotals = new double[numCrossValues + 1];

  IOUtil.writeLogLn("numSqlColumns: " + Integer.toString(numSqlColumns)
      + "; crossColIndex: " + Integer.toString(crossColIndex)
      + "; numCrossValues: " + Integer.toString(numCrossValues), rm);

  outTag("wrapperTable");
  outTableHeader(resultSet);
  outTableBody(resultSet);
  dbUtil.closeResultSet(resultSet);

//  outTableTotal("Всего");
  outTableFooter();
  outTag("wrapperTableEnd");

//  cfgTuner.outCustomSection(footerSectionName, out);
}

protected void outTableHeader(ResultSet resultSet)
{

  outTag("table_beg");  // The <TABLE> tag

  row.setAttr(tableTuner.getParameter("darkheaderBgColor"));

  /* The empty cell, spanned for crossColIndex columns */
  cell_h.addAttr("colspan=" + Integer.toString(crossColIndex));
  cell_h.addAttr("");
  row.addValue(cell_h);

  /* The name of the CrossValue field */
  cell_h.setValue(cfgTuner.getParameter(colTagsSectionName, colNames[crossColIndex]));
  cell_h.setAttr("colspan=" + Integer.toString(numCrossValues));
  row.addValue(cell_h);

  /* The "TOTAL" column header */
//  cell_h.setValue("Всего");
//  cell_h.setAttr("rowspan=2");
//  row.addValue(cell_h);

  cell_h.setAttr("");

  /* Output the 1st row */
  out.write(row.toHTML());
  row.setValue("");

  row.setAttr(tableTuner.getParameter("headerBgColor"));
  /* The names of the starting columns */
  for (int i = 0; i < crossColIndex; i++)
  {
    String tag = cfgTuner.getParameter(colTagsSectionName, colNames[i]);
    cell_h.setValue((tag.equals(""))? colNames[i] : tag);
    row.addValue(cell_h);
  }
  /* The Cross Values columns */
  for (int i = 0; i < numCrossValues; i++)
  {
//    cell_h.addValue("<small>");
    cell_h.setValue("<small>" + crossValues[i]);
    row.addValue(cell_h);
  }
  cell_h.reset(numDigits, thsnDelimiter);
  /* Output the 2nd row */
  out.write(row.toHTML());
}


protected void startRow()
{
  endRow();

  cell.reset(numDigits, thsnDelimiter);
  row.setAttr(tableTuner.getParameter("lmrowBgColor"));

  for (int colNr = 0; colNr < crossColIndex; colNr++)
  {
    cell.setValue(record[colNr]);
    cell.setAttr(tableTuner.getParameter("headerBgColor"));
    row.addValue(cell);
  }
  currentColumn = 0;
}


protected boolean putValue()
{
  double d = 0.;
  IOUtil.writeLogLn("=== currentColumn=" + currentColumn
      + "; numCrossValues: " + numCrossValues, rm);

  for (int i = currentColumn; i < numCrossValues; i++)
  {
    if (record[crossColIndex].equals(crossValues[i]))
    {
      cell.setValue(record[crossColIndex + 1]);
      if (record[crossColIndex + 1].indexOf("BLOCKING") != -1)
        cell.setAttr(tableTuner.getParameter("blockingCellBgColor"));
      else if (record[crossColIndex + 1].indexOf("Lock mode") != -1)
        cell.setAttr(tableTuner.getParameter("lmodeCellBgColor"));
      else if (record[crossColIndex + 1].indexOf("Waiting") != -1)
        cell.setAttr(tableTuner.getParameter("waitingCellBgColor"));
      if (cell.isNumeric())
      {
        d = cell.getDValue();
        collectColTotal (i, d);
        rowTotal = rowTotal + d;
        numRowValues++;
      }
      row.addValue(cell);
      cell.setAttr("");
      currentColumn = i + 1;
      return true;
    }
    else
      row.addValue("<td>&nbsp;</td>");
  }
  currentColumn = numCrossValues;
  return false;
}


}