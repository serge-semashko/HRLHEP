package myiss;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.StringReader;

import java.security.MessageDigest;

import java.sql.Connection;
import java.sql.SQLException;

import java.text.NumberFormat;
import java.text.SimpleDateFormat;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.StringTokenizer;

import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;

import javax.sql.DataSource;

import sun.misc.BASE64Encoder;


public class Tools
{
  String ErrMsg = "";
  public Tools()
  {
  }
  public static String Html2Text(String inStr){
      try {
        // the HTML to convert
        StringReader in = new StringReader(inStr);
        Html2Text parser = new Html2Text();
        parser.parse(in);
        in.close();
        return (parser.getText());
      }
      catch (Exception e) {
        e.printStackTrace();
      }
      return inStr;

  }
  public static String humanBytes(int length)
  {
    String[] prefixes = {"Байт","КБайт","МБайт","ГБайт"};
    float num = length;
    final int step = 1024; 
    for (String val: prefixes)
    {
      if (num<step) return (((num>=2)?Tools.getFormatedFloat(num,1):Tools.getFormatedFloat(num,2)) + " " + val);
      num = num/step;
    }
    return (length + " " + prefixes[0]);
  }
  
  public static String encrypt(String source)
  {
    if (source == null) return null;
    try
    {
      MessageDigest md = MessageDigest.getInstance("SHA-1");
      md.update(source.getBytes());
      BASE64Encoder encoder = new BASE64Encoder();
      return encoder.encodeBuffer(md.digest());
    }
    catch (Exception e)
    {
      return null;
    }
  }
  /**
   * 
   * @param ipAddress
   * @return
   */
  public static long getUnsingedIntFromIPString(String ipAddress)
  {
    long ipIntValue = 0;
    long [] adminIntIP  = new long [4];
    String ipPart = "";
    StringTokenizer st = new StringTokenizer(ipAddress,".");
    int i = 0;
    while(st.hasMoreTokens())
    {
      ipPart = st.nextToken();
      adminIntIP[i++] = Tools.parseInt(ipPart,-1);
    }
        
    return (adminIntIP[0] << 24) + (adminIntIP[1] << 16) + (adminIntIP[2] << 8) + (adminIntIP[3]);
  }
  
  public static String getFormatedFloat(float val, int fraction)
  {
    Locale loc = new Locale("en", "US");
    NumberFormat nf = NumberFormat.getNumberInstance(loc);
    nf.setMaximumFractionDigits(fraction);
    nf.setMinimumFractionDigits(fraction);
    String resStr = nf.format(val);
    return resStr;
  }

  
  /**
   * 
   * @param ipAddress
   * @return
   */
  public static String getStringFromIntIP(long ipAddress)
  {
    long [] ipIntAddr = new long[4];
    for(int i=0;i<4;i++)
    {
      ipIntAddr[3-i] = ipAddress % 256;
      ipAddress = ipAddress / 256;
    }
    
    return ipIntAddr[0] + "." + ipIntAddr[1] + "." + ipIntAddr[2] + "." + ipIntAddr[3];
  }

  /**
   * Метод возвращает стринговое значение переменной из JNDI среды приложения
   * по ее имени
   * @param name - имя переменной
   * @return value String
   */
  public static String LookUpInEnv(String name)
  {
    Context initCtx, env;
    try 
      { ///
        initCtx = new InitialContext();
        env = (Context) initCtx.lookup("java:comp/env");
        String result = (java.lang.String) env.lookup(name);
        return result;
      }
      catch (Exception e)
      {
        return null;
      }
  }
  
  /**
  * Метод возвращает сообщение связанное с последней ошибкой возникшей
  * при вызове методов класса
  * @return Error message - String
  */
  public String getLastError() { return ErrMsg;}

  /**
   * Метод возвращает SQL соединение созданное контейнером по
   * имени DataSource зарегистрированного в JNDI
   * @param name - имя источника данных
   * @return SQL соединение
   */
  public Connection OpenDBConnection(String Name)
  {
    ErrMsg = "";
    try { ///
      Context initCtx = new InitialContext();
      DataSource ds = (DataSource) initCtx.lookup(Name);
      Connection conn = ds.getConnection();
      return conn;
    }
    catch(NamingException ne)
    {
      ErrMsg = ne.getMessage();
      return null;
    }
    catch(SQLException e) 
    {
      ErrMsg = e.getMessage();
      return null;
    }
  }
  /**
   * Исправление Unicode us (старший байт 0) кодировки стринга на ru (старший байт 4) кодировку
   * @param  стринг 
   * @return исправленный стринг
   */
  public static String repru( String s )
  {
    if (s == null) return null;
    final String rusc="АБВГДЕЖЗИЙКЛМНОПРСТУФХЦЧШЩЪЫЬЭЮЯабвгдежзийклмнопрстуфхцчшщъыьэюяЁё";
    int n=s.length(), i,j;  char c;
    StringBuffer q=new StringBuffer( n );
    for(i=0;i<n;i++)
    {
      c=s.charAt( i );
      if( 0xC0<=c && c<=0xFF ) c = rusc.charAt( c-0xC0 );
      else if( c==0xA8 )       c = 'Ё';
      else if( c==0xB8 )       c = 'ё';
      else if( c==0xB9 )       c = '№';
      else if( c==0x96 )       c = '\u2013';
      else if( c==0x97 )       c = '\u2014';
      q.append( c );
    }
    return q.toString();
  }
  /**
  * Метод выполняет замену всех подстрок в  строеке на другую подстроку
  * @param полная строка
  * @param искомая подстрока
  * @param подстрока замены
  * @return нвая срока
  */
  public static String StringReplace(String org, String search, String sub)
  {
    String result = "";
    int j = sub.indexOf(search);
    if(!org.equals(search) && j==-1) 
    {
      int i;
        do{
        i = org.indexOf(search);
          if(i != -1){
        
          result = org.substring(0,i);
          result = result + sub;
          result = result + org.substring(i + search.length());
          org = result;
          }
      
        }while (i != -1);
    }
       
   return org;
  }  
  /**
  * Метод выполняет вставку  подстрок после искомой подстроки
  * @param полная строка
  * @param искомая подстрока
  * @param подстрока вставки
  * @return нвая срока
  */
  public static String StringInsertPart(String org, String search, String partins)
  {
    String suborg = org;
    String result = "";
    int i;
    
        do{
       
        i = suborg.indexOf(search);
          if(i != -1){
          result = result + suborg.substring(0,i) + search + partins;
          suborg = suborg.substring(i + search.length());
          }
          else {result = result + suborg;}
     
        }while (i != -1);
  
  return result;
  }

  /**
  * Метод разделяющий строку @org на слова и
  * вставляющий между словами ними заданную подстроку @partins
  * выходная подстрока @word
  * обычно использую для запроса поиска в БД
  * @param строка
  * @param вставляемая подстрока
  * @return результирующую строку
  */
  public static String StringInsertPartInBlank(String org, String partins)
  {
    String suborg = org.trim();
    String search = " ";
    String word = "";
    String strstr = "";
    ArrayList result = new ArrayList(); //массив слов
    int i, k=0;
    
  //===разбиваем входную фразу на массив слов
        do{
       
        i = suborg.indexOf(search);
          if(i != -1){
          strstr = suborg.substring(0,i).trim();
            if(strstr.length()>0){result.add(suborg.substring(0,i).trim());}
          suborg = suborg.substring(i + search.length());
          }
          else {result.add(suborg.trim());}
        k++;     
        }while (i != -1);

  //===формируем выходную строку из массива слов и входной подстроки @partins
        word = (String)result.get(0);
        if(k != 1){
        
          for(i=1; i<result.size(); i++){word = word + partins + result.get(i);}
        }
        
    return word;
  }

  /**
  * Метод заменяющий у входного параметра @str все
  * опасные теги и символы, которые может внести user
  * при заполнении форм, на пробел
  * @param входная строка
  * @return преобразованная строка
  */
  public static String StringReplaceTegiInNull(String str)
  {
      String Mess;
      Mess = str.trim();
      if(Mess != null && Mess != "")
      {
       Mess = Tools.StringReplace(Mess, "javascript", " ");
       Mess = Tools.StringReplace(Mess, "JavaScript", " ");
       Mess = Tools.StringReplace(Mess, "JAVASCRIPT", " ");
       Mess = Tools.StringReplace(Mess, "Jscript", " ");
       Mess = Tools.StringReplace(Mess, "</script>", " ");
       Mess = Tools.StringReplace(Mess, "<SCRIPT LANGUAGE=", " ");
       Mess = Tools.StringReplace(Mess, "<script language=", " ");
       Mess = Tools.StringReplace(Mess, "img ", " ");
       Mess = Tools.StringReplace(Mess, "src=", " ");
       Mess = Tools.StringReplace(Mess, "SRC=", " ");
       Mess = Tools.StringReplace(Mess, "a href=", " ");
       Mess = Tools.StringReplace(Mess, ".exe", " ");
       Mess = Tools.StringReplace(Mess, ".js", " ");
      }
      
  return Mess;  
  }
  /**
  *  Метод заменяет кавычки \" тегом &qout;
  * @param входная строка
  * @return преобразованная строка
  */
  public static String StringReplaceKavychka(String str)
  {
      if (str == null) return null;
      String Mess;
      Mess = str.trim();
      char bukv = '"';
      char teg = '&';
      String strr = String.valueOf(teg) + "quot";
      teg = ';';
      strr = strr + String.valueOf(teg);
      if(Mess != null && Mess != "")
      {
       Mess = Tools.StringReplace(Mess, String.valueOf(bukv), strr);
       
      }
      
  return Mess;  
  }

  /**
  * Метод выполняет проверку последней даты в месяце
  * @param day
  * @param mounth
  * @param year
  * @return последняя дата в формате yyyyMMdd
  */
  public static String LastDataForSQL(String day, String mounth, String year)
  {
    String Data = "";
    Calendar q = Calendar.getInstance();
    
      try
     {
      q.clear();
      int mm = Integer.parseInt(mounth);
      q.set(Integer.parseInt(year),mm-1,Integer.parseInt(day));
      int den = q.get(Calendar.DATE);
      int mes = (q.get(Calendar.MONTH)==0)?1:q.get(Calendar.MONTH);
      int god = q.get(Calendar.YEAR);

      mes = (mm==1)?1:mes + 1;
      Data = String.valueOf(god);
      Data = (mes<10)?Data+"0"+mes:Data+mes;
      Data = (den<10)?Data+"0"+den:Data+den;
      
      }
      catch(Exception e){}
    
    
    return Data;
  }

  public static int  parseInt(Object o, int defaultVal)
  {
    try
    {
      return Integer.parseInt(o.toString());
    }
    catch (Exception e)
    {
      return defaultVal;
    }
  }
  public static Float parseFloat(Object o, Float defValue)
  {
    if (o == null) return defValue;
    if (o instanceof Float) return (Float) o;
    try
    {
      return Float.parseFloat(String.valueOf(o));
    }
    catch (Exception e) {return defValue;}
  }
  
  public static Double parseDouble(Object o, Double defValue)
  {
    if (o == null) return defValue;
    if (o instanceof Double || o instanceof Float) return (Double) o;
    try
    {
      return Double.parseDouble(String.valueOf(o));
    }
    catch (Exception e) {return defValue;}
  }
  
  public static Boolean parseBoolean(Object o, Boolean defValue)
  {
    if (o == null) return defValue;
    if (o instanceof Number) return (((Number) o).intValue()!= 0 );
    try
    {
      return Boolean.parseBoolean(String.valueOf(o));
    }
    catch (Exception e) {return defValue;}
  }
  
  /**
   * Чтобы замещать null одной строчкой в html
   * 
   * @param o
   * @param defObj
   * @return
   */
  public static Object isNull(Object o, Object defObj)
  {
    return (o == null)?  defObj: o;
  }
  
  public static String getFormatedDate(java.util.Date date, String pattern)
  {
    //String pattern = "HH:mm E','dd MMMM yyyy";
    String sDate = "";
    if (date == null) return "";
    try
    {
      Locale loc = new Locale("ru","RU");
      SimpleDateFormat dateFormat = new SimpleDateFormat(pattern,loc);
      sDate = dateFormat.format(date);
    }
    catch(Exception e)
    {
      return "Invalid date format";
    }
    return sDate;
  }
  public static java.util.Date parseDate(String sDateTime)
  {
    if (sDateTime == null) return null;
    StringTokenizer st = new StringTokenizer(sDateTime, " ");
    String sDate = "", sTime = "";
    if (st.hasMoreTokens()) sDate = st.nextToken();
    if (st.hasMoreTokens()) sTime = st.nextToken();
    if (sDate.length() == 0)return null;
    
    st = new StringTokenizer(sDate, ".-/");
    if (st.countTokens() != 3) return null;
    String sDay = st.nextToken();
    String sMonth = st.nextToken();
    String sYear = st.nextToken();
    if (sDay.length() == 1) sDay = "0" + sDay;
    if (sMonth.length() == 1) sMonth = "0" + sMonth;
    if (sYear.length() == 2) sYear = "20" + sYear;
    
    sDate = sDay + "." + sMonth + "." +sYear;
    
    String sTimeT = sTime;
    sTime = "00:00";
    st = new StringTokenizer(sTimeT,":");
    if (st.countTokens() >= 2)
    {
       String sHour = st.nextToken();
       String sMin = st.nextToken();
       if (sHour.length() == 1) sHour = "0" + sHour;
       if (sMin.length() == 1) sMin = "0" + sMin;
       sTime = sHour + ":" + sMin;
    }
    
    sDateTime = sDate + " " + sTime;
    String pattern = "dd.MM.yyyy HH:mm";
    try
    {
      Locale loc = new Locale("ru","RU");
      SimpleDateFormat dateFormat = new SimpleDateFormat(pattern,loc);
      return dateFormat.parse(sDateTime);
    }
    catch(Exception e)
    {
      return null;
    }
    
  }
  
  public static String getStringValue(Object o, String defValue, String dateFormat){
    if(dateFormat.length()==0) dateFormat="dd.MM.yyyy";
    if (o instanceof String) return getStringValue(o, defValue);
    if (o instanceof Integer) return parseInt(o,-1)+"";
    if (o instanceof Date) return getFormatedDate((Date)o,dateFormat);
    if (o instanceof Float) return String.format("%10.2f",parseFloat(o,-1f))+"";
    if (o instanceof Double) return String.format("%10.2f",parseDouble(o,-1d))+"";
    if (o instanceof Long) return o.toString();
    if (o instanceof java.lang.String[] && ((String[])o).length>0) {
    return ((String[])o)[0];
    };
    return defValue;
  }
  public static String getStringValue(Object o, String defValue)
  {
    if (o == null) return defValue;
    else return String.valueOf(o);
  }
  
  public static String getStringFromFile(String fileName) 
  {
    try
    {
      final File inf = new File(fileName);
      final BufferedReader in = new BufferedReader(new FileReader(inf));
  
      try 
      {
        final StringBuffer sb = new StringBuffer();
        for (String s = in.readLine(); s != null; s = in.readLine()) 
        {
          sb.append(s).append(" ");
        }
        return sb.toString();
      } 
      finally
      {
        try 
        {
          in.close();
        } 
        catch (IOException e){e.printStackTrace();}
      }
    }
    catch(IOException e)
    { 
      System.err.println(e.toString());
      return "";
    }    
  }
  /**
   * Возвращает индекс str в массиве стриегов arr 
   * если не найден -1
   * Для опроеделения action по суффиксу Url
   * @param str
   * @param arr
   * @return
   */
  public static int getStringIndex(String str, String[] arr)
  {
    if (str == null || arr == null || arr.length == 0) return -1;
    for (int i = 0; i < arr.length; i++)
      if (str.equals(arr[i])) return i;
    return - 1;  
  
  }
  public static Object getLHMValue(List list, int index, String colName)
  {
    return ((HashMap) list.get(index)).get(colName);
  }
  public static List<HashMap> getItemsForPage(int itemsOnPage, int pageNo, List<HashMap> items){
      List<HashMap> res = new ArrayList<HashMap>();
      int cnt = 0;
      for (HashMap hm: items){
        if(cnt < (pageNo*itemsOnPage) && cnt >=(pageNo-1)*itemsOnPage)
        res.add(hm);
        cnt++;
      }
  return res;
  }
}
