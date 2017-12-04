package jinr.pin;

import dubna.walt.service.CommandExecutor;
import dubna.walt.util.IOUtil;
import dubna.walt.util.StrUtil;
import java.io.InputStream;
import java.sql.Connection;
import java.sql.PreparedStatement;
import javax.sql.rowset.serial.SerialBlob;

public class ServiceConvertTex extends dubna.walt.service.CommandExecutor
{
	boolean silent = false;

public void start() throws Exception
{
//  super.start();
	start_();
	Connection con = dbUtil.getConnection();
	String sql = StrUtil.strFromArray(cfgTuner.getCustomSection("storeResult"));
	PreparedStatement stmt = con.prepareStatement (sql);
		stmt.setString( 1, cfgTuner.getParameter("contents1") );
//		stmt.setBlob( 2, bl );
		int numRec = stmt.executeUpdate();
		if (numRec == 1)
		{ IOUtil.writeLogLn("ServiceConvertTex - Update - OK", rm);
			System.out.println("ServiceConvertTex - Update - OK");
		}
		else
		{ cfgTuner.addParameter("UPDATE_ERROR","ServiceConvertTex: Number updated records:" + numRec);
			cfgTuner.addParameter("ERROR","ServiceConvertTex: Number updated records:" + numRec);
		}
	stmt.close();
	con.commit();
	
}

	public void start_() throws Exception
	{
		int numCommandsExecuted = 0;
		silent = cfgTuner.enabledOption("silent")
					&&!cfgTuner.enabledOption("debug=on");
					
		if (!silent)
			cfgTuner.outCustomSection("report header",out);

		if (validateInput())
		{
			Runtime rt = Runtime.getRuntime();
			Process p;
			String[] commands = cfgTuner.getCustomSection("commands");
			if (commands != null)
			{
				for (int i=0; i < commands.length; i++)
				{
					output("<p><b>" + (i + 1) + ": Command: '" + commands[i] +"'</b>");
					log (i + ": Starting command: '" + commands[i] +"'");
					p = rt.exec(commands[i]);
					InputStream from = p.getInputStream();
					log (" waiting...");
					output("<pre>");
					copyAll(from);
					output("</pre>");
			//      p.waitFor();
					log (" === Finished!");
					log ("     Exit code=" + Integer.toString(exitValue(p)));
					numCommandsExecuted++;
				}
			}
			else
				output("<p><b> Commands not executed - there are input mistakes!</b><p>");
		}

		if (numCommandsExecuted == 0)
			cfgTuner.addParameter("ERROR", "Nothing has been executed");

		cfgTuner.addParameter("NumComands", Integer.toString(numCommandsExecuted));

	/**/  if (!silent)
		{
			cfgTuner.outCustomSection("report footer",out);
			out.flush();
		}
	/**/  
	}

	public void copyAll(InputStream from)
	{
		int ch =0;
		try
		{
			while(ch >= 0 )
			{ System.out.print('.');
				ch = from.read();
			  System.out.print(ch);
				output(ch);
			}
		}
		catch (Exception e)
		{
			System.out.println(e.toString());
		}
	}

}