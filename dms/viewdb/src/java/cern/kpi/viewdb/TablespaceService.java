package cern.kpi.viewdb;

import java.sql.ResultSet;

//import dubna.walt.util.*;

public class TablespaceService extends dubna.walt.service.TableServiceSimple
{


protected int outTableBody(ResultSet resultSet) throws Exception
{
  int colNr;
  currentRow = -1;
  int numRows = 0;
  

  while (resultSet.next())
  {
    numRows++;
    row.setValue("");
    getRecord(resultSet);


    for (colNr = 0; colNr < numSqlColumns; colNr++)
    {
      cell.setValue(record[colNr]);
      cell.setFormatParams(numDigitsForCols[colNr], thsnDelimiter);
      row.addValue(cell);
    }
    currentRow++;
    row.setAttr(tableTuner.getParameter("totalBgColor"));
    out.println(row.toHTML());

//    if (currentRow % 4 == 1)
//      row.setAttr(tableTuner.getParameter(""));
//    else if (currentRow % 4 == 3)
//      row.setAttr(tableTuner.getParameter(""));
  }
  resultSet.close();
  return numRows;

}

public void beforeStart() throws Exception
{
  { super.beforeStart();
  }
}

}