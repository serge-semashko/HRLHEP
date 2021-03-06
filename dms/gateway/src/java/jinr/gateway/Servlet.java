package jinr.gateway;

import dubna.walt.Query;
import dubna.walt.util.ResourceManager;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

public class Servlet extends dubna.walt.BasicServlet {

    static Monitor monitor = null;

    public ResourceManager obtainResourceManager() throws Exception {
        System.out.println(".\n\r.\n\r.\n\r*** GATEWAY - INIT ...");
//  Load static resources for the app
        ResourceManager rm = new ResourceManager("gateway");
        try {
            Class.forName(rm.getString("dbDriver"));        // init the JDBC driver
            System.out.println("*** GATEWAY: dbDriver=" + rm.getString("dbDriver"));
        } catch (Exception ex) {
            Logger.getLogger(Servlet.class.getName()).log(Level.SEVERE, null, ex);
            ex.printStackTrace(System.out);
        }
        return rm;
    }

    @Override
    public void customInit() {
        System.out.println("*** GATEWAY - customInit: rm_Global=" + rm_Global);
        try {
        } catch (Exception e) {
        }
    }

    public void setResourceManager(ResourceManager rm) {
        this.rm_Global = rm;
        System.out.println("*** GATEWAY: rm_Global=" + rm_Global);
        try {
            Class.forName(rm.getString("dbDriver"));        // init the JDBC driver
            System.out.println("*** GATEWAY: dbDriver=" + rm.getString("dbDriver"));
        } catch (Exception ex) {
            Logger.getLogger(Servlet.class.getName()).log(Level.SEVERE, null, ex);
            ex.printStackTrace(System.out);
        }

    }

    protected static void startMonitor() {
//        System.out.println("*** GATEWAY - startMonitor: rm_Global=" + rm_Global);
        try {
            if(monitor != null) monitor.stop = true; //НА ВСЯКИЙ СЛУЧАЙ остановим
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
//  rm_Global.println("=== Do doGet works...");
//  rm_Global.println("=== req:" + req);
//String s = rm_Global.getString("clientEncoding",false,"Cp1251");
//System.out.println("");
//System.out.println("***** clientEncoding='" + s + "'");

// req.setCharacterEncoding("Cp1251");
        if ("XMLHttpRequest".equals(req.getHeader("X-Requested-With"))) {
            req.setCharacterEncoding("utf8"); //ajax ВСЕГДА пересылается в UTF-8.
            res.setCharacterEncoding(rm_Global.getString("clientEncoding", false, "Cp1251"));
        }
        else {
//	 res.setCharacterEncoding(s);
//	 res.setCharacterEncoding("UTF-8");
            req.setCharacterEncoding(rm_Global.getString("clientEncoding", false, "Cp1251")); //ADDED 09.09.2014
            res.setCharacterEncoding(rm_Global.getString("clientEncoding", false, "Cp1251"));
        }

        //req.setCharacterEncoding("Cp1251");
//        logRequest(req);

        String reset = req.getParameter("reset");
        if (reset != null && reset.equals("yes")) {
            destroy();
//    initServlet();
            init((ServletConfig) rm_Global.getObject("ServletConfig"));

        }
        new Query(req, res, rm_Global, newQueryLabel(), this);
        if(monitor == null || !monitor.isRunning())
            startMonitor();
    }

    @Override
    public void destroy() {
        rm_Global.println("--------------------------- DESTROY: " + this);
        try {
//            rm_Global.putString("DESTROY", "true");
            monitor.stop = true;
            monitor = null;
        } catch (Exception e) {
            /* We don't care */
        }

        super.destroy();
    }

}
