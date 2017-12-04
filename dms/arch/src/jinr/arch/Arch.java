package jinr.arch;

import dubna.walt.util.ResourceManager;
import java.util.*;

public class Arch extends dubna.walt.BasicServlet {

public ResourceManager obtainResourceManager() throws Exception {
//System.out.print("*** Locale:" + java.util.Locale.getDefault());
//  java.util.Locale.setDefault(new java.util.Locale("ru","RU"));
//  java.util.Locale.setDefault(new java.util.Locale("ru","RU","WIN"));
        System.out.print("*** ARCH - INIT ...");
        ResourceManager rm = new ResourceManager("arch");
        rm.setObject("Servlet", this);
//System.out.println("  --> " + rm.getString("dbDriver"));
//  Class.forName(rm.getString("dbDriver"));        // init the JDBC driver
        GregorianCalendar calendar = new GregorianCalendar();
        int currYr = calendar.get(Calendar.YEAR);
//	int minYr=rm.getInt("minYr");
//	if (minYr == 0) minYr = 1989;
// int yr_interval=rm.getInt("YR_interval");
        int minYr = Integer.parseInt(rm.getString("minYr", false, "1989"));
        int yr_interval = Integer.parseInt(rm.getString("YR_interval", false, "3"));

        int currMonth = calendar.get(Calendar.MONTH);
        System.out.print("*** ARCH - currYr=" + currYr + " currMonth=" + currMonth + "; minYr=" + minYr + "; YR_interval=" + yr_interval);

        String yrs_present = "", yrs_old = "", yrs_present_options = "", yrs_old_options = "";

        for (int i = currYr; i >= minYr; i--) {
            if (i > currYr - yr_interval) {
                yrs_present += i + ",";
                yrs_present_options += "<option value='" + i + "'>" + i + "</option>";
            } else {
                yrs_old += i + ",";
                yrs_old_options += "<option value='" + i + "'>" + i + "</option>";
            }

        }
        yrs_present += "9999";
        yrs_old += "-9999";
        rm.setParam("YRS_OLD", yrs_old, true);
        rm.setParam("YRS_PRESENT", yrs_present, true);
        rm.setParam("YRS_OLD_OPTIONS", yrs_old_options, true);
        rm.setParam("YRS_PRESENT_OPTIONS", yrs_present_options, true);
        rm.setParam("CURR_YR", Integer.toString(currYr), true);

        return rm;
    }

        }
