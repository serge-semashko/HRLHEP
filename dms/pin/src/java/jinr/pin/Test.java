package jinr.pin;

import java.sql.*;
import dubna.walt.util.StrUtil;
import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.util.Enumeration;
import java.util.ResourceBundle;

public class Test
{

		public static String convertString4XML(String src)
		{
//		  String s = src.replaceAll("\u0021-\u007E","?");
// String s = src.replaceAll("[\u0041-\u005A][\u0410-\u044F]","_");
			String s = src.replaceAll("[^\u0021-\u00A0][^\u0410-\u044F]","_");
			return s;
		}

	public static void main ( String[] args )
	{

		System.out.println("======================");
		try
		{
			FileReader r = new FileReader("c:/2/1s.xml");
		  BufferedReader br = 
		    new BufferedReader(
		      new InputStreamReader(
		        new FileInputStream("c:/2/1s.xml"), "UTF-8")
		    ,8192);
			String str;
		  PrintWriter pw = new PrintWriter(
				new OutputStreamWriter(
				new FileOutputStream("c:/2/1s_.xml",false), "UTF-8")
				);
		  while ((str = br.readLine()) != null)
		  { // System.out.println(str);
		    str = convertString4XML(str);
				pw.println(str);
		  }
			br.close();
		  pw.close();
		}
		catch( Exception e )
		{
			e.printStackTrace(System.out);
		}
		System.out.println("======================");
	}

}