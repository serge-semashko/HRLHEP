package jinr.pin.compare;

import dubna.walt.util.ResourceManager;

import java.io.*;
import dubna.walt.util.*;

public class TestComparet
{



public static void main(String[] args)
{ System.out.println("===========");
	long l = System.currentTimeMillis();
	String[] names = {"b"
/**/			,"Cellular Automata Study of High Burn-up Structures."
		,	"Cellular Study of High Burn-up Structures."
	  , "of High Burn-up Structures Cellular Automata Study."
		, "Cellular Automata Approach to Investigation of High Burn-up Structures in Nuclear Reactor Fuel."
		, "Cellular Automata Approach to Investigation of High Burn-up Structures in Nuclear"
/**/		
/*	,"bb"
	,"bbb"
	,"bbbb"
	,"bbcbb"
	, "bbbbbb"
	, "bbbbbbb" */
		};
	
	String patt = "Cellular Automata Study of High Burn-up Structures.";
//  String patt = "bbbb";
 System.out.println( "pattern: " +patt );
	
//   for (int i=0; i<1000; i++)
	{
		for (int j=0; j< names.length; j++)
		{ int n = StrUtil.fuzzyMatch(patt, names[j], 30);
			System.out.println( n + ":" + names[j] );
//		  n = fuzzyMatch("bbb", names[j], 50);  System.out.println("bbb:" + names[j] + ":" + n);
		}
	}
	System.out.println("TIME: " + Long.toString(System.currentTimeMillis() - l));
}

}
