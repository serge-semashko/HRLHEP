import java.util.Enumeration;
import java.util.ResourceBundle;

public class Test
{

	public static void main ( String[] args )
	{
	  ResourceBundle rb = ResourceBundle.getBundle("test1");
	  System.out.println("======================");
		System.out.println("param1=" + rb.getString("param1"));
	  System.out.println("..... ALL PARAMETERS: ");
	  Enumeration e = rb.getKeys(); 
	  while (e.hasMoreElements())
		{ String name = (String) e.nextElement();
	    System.out.println(name + "='" + rb.getString(name) + "'");
		}
	  System.out.println("======================");
	}

}
