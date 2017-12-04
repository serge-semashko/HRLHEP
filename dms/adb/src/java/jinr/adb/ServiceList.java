package jinr.adb;

import java.sql.ResultSet;
//import dubna.walt.util.*;
//import java.nio.charset.Charset; import java.util.*; import java.io.*;

public class ServiceList extends dubna.walt.service.TableServiceSimple
{
String bgcolor="CCCCFF";

protected int outTableBody(ResultSet resultSet) throws Exception
{ if (cfgTuner.enabledOption("MSIE")) bgcolor="gray";
/*
  OutputStream os = (OutputStream) rm.getObject("outStream");
  OutputStreamWriter osw = new OutputStreamWriter(os);
  
  out.println("+++++ here опнаю<br>");
  out.println("+++++ Encoding: " + osw.getEncoding() + "<br>");
  out.flush();
  osw.write("12345 here опнаю<br>");
  osw.flush();
  
  out.println("--<br>Charsets:" + Charset.availableCharsets());
  Locale[] l = java.text.Collator.getAvailableLocales();
  for (int i = 0; i < l.length; i++)
  out.println("<br>Locales:" + l[i]);
/**/  
  int numItems = super.outTableBody(resultSet);
  setRowLinks(numItems);
  return numItems;
}

protected void processRecord() throws Exception
{ 
//    if (currentRow >= srn-2 && currentRow < srn+rpp)
//        out.println("<tr><td colspan=" + Integer.toString(numTableColumns) 
//        + " bgcolor=" + bgcolor + "></td></tr>");
//      + " bgcolor=gray></td></tr>");
//    out.println("<tr><td colspan=3 bgcolor=white></td></tr>");
//    out.println("<tr><td colspan=3><hr></td></tr>");
  super.processRecord();
  cfgTuner.outCustomSection("spec row", out);
}

protected void setRowLinks(int numItems)
{
  String s = "";
  int ern_i;
  for (int srn_i=1; srn_i <= numItems; srn_i += rpp)
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