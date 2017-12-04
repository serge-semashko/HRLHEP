/*  NOT USED ??? */

package jinr.adb;

import java.sql.*;
//import java.util.Vector;
import dubna.walt.util.*;
import dubna.walt.service.Service;

public class ServiceEditPath extends Service
{


public void start() throws Exception
{

//  cfgTuner.outCustomSection("report header",out);
//  if (cfgTuner.enabledOption("node_id"))
//    showItems();
  cfgTuner.outCustomSection("report",out);
}

protected void showItems() throws Exception
{
  
  int numItems = cfgTuner.getIntParameter("NUM_ITEMS");
  String val="", cat="";
//  System.out.println("===== numItems="+numItems);
  if (numItems > 0)
  {
    cfgTuner.outCustomSection("list header", out);

    ResultSet r = dbUtil.getResults(getSQL("list SQL"));
    String[] headers = DBUtil.getColNames(r);
    int nr;
    String oldCat="", q_CODE="";
    String s="";

    String f_search = cfgTuner.getParameter("f_search");
    String mark_before = cfgTuner.getParameter("mark_before");
    String mark_after = cfgTuner.getParameter("mark_after");
    int srn = cfgTuner.getIntParameter("srn");
    int rpp = cfgTuner.getIntParameter("rpp");
    if (rpp < 0) 
    { rpp = 99999;
      srn = 1;
    }
    if (srn >= numItems) srn = 1;

    for (nr =1; nr<srn && r.next(); nr++)
    { // Skip the leading records
    }
    while (r.next() && nr < srn+rpp)
    {
      for (int i = 0; i < headers.length; i++)
      {
        val = r.getString(i+1);
        if (val != null && val.length() > 0 && !val.equalsIgnoreCase("NULL"))
          cfgTuner.addParameter(headers[i],val);
        else
          cfgTuner.addParameter(headers[i],"");
      }
      cfgTuner.addParameter("nr",Integer.toString(nr++));
      cat = cfgTuner.getParameter("CAT").trim();
      while (cat.startsWith("/"))
        cat = cat.substring(1).trim();
      cfgTuner.addParameter("CAT", cat);
  
      if (cat.equals(oldCat))
        cfgTuner.deleteParameter("newCat");
      else
        cfgTuner.addParameter("newCat","YES");
      oldCat = cat;
      
      if (f_search.length() > 0)
      { cfgTuner.addParameter("DESCR"
          ,StrUtil.markSubstr(cfgTuner.getParameter("DESCR")
            , f_search, mark_before, mark_after));
      }
      q_CODE = cfgTuner.getParameter("q_" + cfgTuner.getParameter("CODE"));
      cfgTuner.addParameter("q_CODE", q_CODE);
      cfgTuner.outCustomSection("item", out);

    }
    dbUtil.closeResultSet(r); 
    
    setRowLinks(srn, rpp, numItems);
    cfgTuner.outCustomSection("list footer", out);
  }
  else if (cfgTuner.enabledOption("f_search")|!cfgTuner.enabledOption("node_id"))
  {
    cfgTuner.addParameter("welcome", "YES");
    cfgTuner.outCustomSection("no items", out);
  }
}


protected void setRowLinks(int srn, int rpp, int numItems)
{
  String s = "";
  int ern_i;
  for (int srn_i=1; srn_i < numItems; srn_i += rpp)
  {
    ern_i = srn_i + rpp-1;
    if (ern_i > numItems) ern_i = numItems;
    if (srn_i == srn)
      cfgTuner.addParameter("currentPage", "YES");
    else
      cfgTuner.deleteParameter("currentPage");
        
    cfgTuner.addParameter("srn_i", Integer.toString(srn_i));
    cfgTuner.addParameter("ern_i", Integer.toString(ern_i));
    s = s + cfgTuner.getParameter("rowLink");
  }
  cfgTuner.addParameter("rowLinks", s);
}

}