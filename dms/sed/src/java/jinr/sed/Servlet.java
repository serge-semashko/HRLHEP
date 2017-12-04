package jinr.sed;

import dubna.walt.Query;
import dubna.walt.util.ResourceManager;
import java.io.IOException;
import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

public class Servlet extends dubna.walt.BasicServlet {

    static Monitor monitor = null;

    /**
     *
     * @return @throws Exception
     */
    @Override
    public ResourceManager obtainResourceManager() throws Exception {
        System.out.println(".\n\r.\n\r.\n\r*** SED - INIT ...");
//  Load static resources for the app
        ResourceManager rm = new ResourceManager("dms");
//  System.out.println("  --> " + rm.getString("dbDriver"));
//  Class.forName(rm.getString("dbDriver"));        // init the JDBC driver

// Path to the application root in the server's file system
/*
  String myPath=getServletConfig().getServletContext().getRealPath("/");
  myPath = StrUtil.replaceInString(myPath,"\\","/");
  System.out.println("... myPath=" +  myPath );
  rm.setParam("AppRoot", myPath, true);
  rm.setParam("logPath", myPath, true);
  
// Path to the Tomcat root in the server's file system
  String t = myPath.substring(0,myPath.length()-2);
  int i = t.lastIndexOf("/");
  if (i > 0) t = t.substring(0,i+1);
  rm.setParam("TomcatRoot", t, true);
	
	System.out.println("*** EDO: rm=" + rm);
/**/
 /*  Servlet.readConfigFile(myPath + "WEB-INF/" + rm.getString("cfgFileName"), rm);
         */
        return rm;
    }

    /**
     *
     */
    @Override
    public void customInit() {
        try {
            Class.forName(rm_Global.getString("dbDriver"));        // init the JDBC driver
            Class.forName(rm_Global.getString("dbDriverADB"));        // init the JDBC driver

            if(monitor != null )
                monitor.stop = true;
            monitor = null;
            //  Load static resource for Russian GUI language            
            System.out.println("*** SED - loading russian.dat ...");
            ResourceManager srm = new ResourceManager(rm_Global.getString("CfgRootPath", true) + "dat/russian.dat", rm_Global.getString("serverEncoding", false, "Cp1251"));
            rm_Global.setObject("srm_russian", srm, false);

            //  Load static resource for English GUI language
            System.out.println("*** SED - loading english.dat ...");
            srm = new ResourceManager(rm_Global.getString("CfgRootPath", true) + "dat/english.dat", rm_Global.getString("serverEncoding", false, "Cp1251"));
            rm_Global.setObject("srm_english", srm, false);
        } catch (Exception e) {
            e.printStackTrace(System.out);
        }
    }

    public void setResourceManager(ResourceManager rm) {
        Servlet.rm_Global = rm;
        System.out.println("*** SED: rm_Global=" + rm_Global);
    }

    protected static void startMonitor() {
        System.out.println("*** SED - startMonitor: rm_Global=" + rm_Global);
        try {
            if (monitor != null) {
                monitor.stop = true; //НА ВСЯКИЙ СЛУЧАЙ остановим
            }
            monitor = new Monitor(rm_Global);
//        System.out.println("*** GATEWAY - monitor=" + monitor);
            new Thread(monitor).start();
        } catch (Exception e) {
            e.printStackTrace(System.out);
        }
    }

    /**
     * This method is called when the servlet's URL is accessed.<P>
     * @param req
     * @param res
     * @throws javax.servlet.ServletException
     * @throws java.io.IOException
     */
// http://www.getinfo.ru/article296.html - КОДИРОВКИ в JAVA
    @Override
    public void doGet(HttpServletRequest req, HttpServletResponse res)
            throws ServletException, IOException {
        try {
            super.doGet(req, res);
            if(monitor == null || !monitor.isRunning())
                startMonitor();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    
    /**
     *
     */
    @Override
    public void destroy() {
        rm_Global.println("--------------------------- DESTROY: " + this);
        try {
//            rm_Global.putString("DESTROY", "true");
            if(monitor != null)
                monitor.stop = true;
            monitor = null;
        } catch (Exception e) {
            e.printStackTrace();
        }
        super.destroy();
    }
}
