package jinr.pin;

import java.sql.*;
import dubna.walt.util.StrUtil;

public class TableServiceSortable extends dubna.walt.service.TableServiceSimple
{
  public void beforeStart() throws Exception
  { 
    super.beforeStart();
    srn = cfgTuner.getIntParameter("srn");
    rpp = cfgTuner.getIntParameter("rpp");
    if (srn < 0 || rpp < 0) 
    { rpp = 99999;
      srn = 1;
    } 
    int totNumRec = cfgTuner.getIntParameter("TotNumRecords");
    int totNumPages = ((totNumRec - 1) / rpp) + 1;
// System.out.println("*** srn=" + srn + "; rpp=" + rpp + "; TotNumRecords=" + totNumRec + "; totNumPages=" + totNumPages);
    int currPageNr = ((srn - 1) / rpp) +1;
    cfgTuner.addParameter ("totNumPages", Integer.toString(totNumPages));
    cfgTuner.addParameter ("currPageNr", Integer.toString(currPageNr));
    int pageSize = totNumRec - (currPageNr - 1)*rpp;
    if (pageSize > rpp) pageSize = rpp;
    cfgTuner.addParameter ("currPageSize", Integer.toString(pageSize));
    String pList = "";
    for (int i=1; i <= totNumPages; i++)
    { cfgTuner.addParameter ("pageNr", Integer.toString(i));
      if (i == currPageNr)
        cfgTuner.addParameter ("currPage", "Y");
      else
        cfgTuner.addParameter ("currPage", "");
      pList += cfgTuner.getParameter("pList");
    }
    cfgTuner.addParameter ("pageList", pList);
  //  System.out.println("*** pageList=" + pList);

  }

  protected void outTableHeader(ResultSet resultSet)
  { // outTag("table_beg");
    headerRow.setAttr(tableTuner.getParameter("headerBgColor"));
    String sortCol = "";
    String sortCols = "," + cfgTuner.getParameter("sortCols") + ",";

    for (int i = 0; i < numSqlColumns- numSpecialCols; i++)
    { String tag = cfgTuner.getParameter(colTagsSectionName, colNames[i]);
  //    System.out.println("i=" + i + "; tag=" + tag + "; colName=" + colNames[i]);
      tag = (tag.equals(""))? colNames[i] : tag;
      colLabels[i] = tag;

      if (tag.indexOf("$CALL_SUBSERVICE") >= 0)
      { int j = tag.indexOf(" tag=");
        if (j > 0)
          tag = tag.substring(j+5);
        else
          tag = null;
      }
      
      if (tag.indexOf("$ATTR:") > 0)
      { int j = tag.indexOf("$ATTR:");
        cell_h.setAttr(tag.substring(j+6));
        tag = tag.substring(0, j-1);
      }
      else
        cell_h.setAttr("");

      sortCol = cfgTuner.getParameter("SortBy", colNames[i]);
      if (colLabels[i].indexOf("$CALL_SUBSERVICE") < 0
        && (sortCol.length() > 0
         || sortCols.equalsIgnoreCase("ALL") 
         || sortCols.indexOf(","+colNames[i]+",") >=0))
      { if (sortCol.length() == 0) 
        { if (makeSubtotals)
            sortCol = Integer.toString(i + 2);
          else
            sortCol = Integer.toString(i + 1);
        }
        if (cfgTuner.enabledOption("srt=" + sortCol))
          tag +=tableTuner.getParameter("sortArrow");
        else
          tag +=tableTuner.getParameter("spacer");
          
        tableTuner.addParameter("h_id", "h_" + sortCol);
        tableTuner.addParameter("sortCol", sortCol);
        cell_h.addAttr(tableTuner.getParameter("thSortAttr"));
      }
  //    colLabels[i] = tag;
      cell_h.setValue(tag);
      headerRow.addValue(cell_h);
    }
  //  if (!cfgTuner.enabledOption("hide_headers"))
      out.println(headerRow.toHTML());
    if (makeSubtotals)
    {
      headerRow = null;
  //    super.outTableHeader(resultSet);
    }
  }

  }