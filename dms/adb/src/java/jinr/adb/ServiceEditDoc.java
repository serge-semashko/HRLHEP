package jinr.adb;

import dubna.walt.util.*;
import java.util.StringTokenizer;

public class ServiceEditDoc extends dubna.walt.service.Service
{
StringTokenizer st = null;

public void beforeStart() throws Exception
{ String fieldsToCorrect = cfgTuner.getParameter("fieldsToCorrect");
  st = new StringTokenizer(fieldsToCorrect,", ");
  if (cfgTuner.enabledOption("cop=u"))
  {  
    while (st.hasMoreTokens())
    { String field = st.nextToken();
      cfgTuner.addParameter(field, 
//        StrUtil.replaceInString(
        StrUtil.replaceInString(
          cfgTuner.getParameter(field),"\n","<br>")
//          ," ","&nbsp;")
          );
    }
  }
    super.beforeStart();
}

public void start() throws Exception
{ 
  String[] sa = cfgTuner.getCustomSection("Doc Data");
  while (st.hasMoreTokens())
  { String field = st.nextToken();
    cfgTuner.addParameter(field, 
//      StrUtil.replaceInString(
      StrUtil.replaceInString(
         cfgTuner.getParameter(field),"<br>","\n")
//         ,"&nbsp;"," ")
         );
  }   
  cfgTuner.outCustomSection(reportSectionName,out);
}

}