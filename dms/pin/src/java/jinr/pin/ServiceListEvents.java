package jinr.pin;

import dubna.walt.service.TableServiceSpecial;
import dubna.walt.util.IOUtil;
import java.sql.*;
import javax.sql.rowset.serial.*;
import dubna.walt.util.StrUtil;

public class ServiceListEvents extends dubna.walt.service.TableServiceSpecial
{

	public void start() throws Exception
	{ String[] item=null;
		
		cfgTuner.addSection("item",item);
	  super.start();
	}
}