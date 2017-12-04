package dubna.walt;

import java.io.IOException;

import javax.servlet.*;
import javax.servlet.http.*;
import dubna.walt.util.*;

/**
 *
 * @author serg
 */
public class BasicServlet extends HttpServlet {

    /**
     *
     */
    protected String appName = "Basic Servlet";

    /**
     *
     */
    protected int totalQueryNr = 0;

    /**
     *
     */
    protected static ResourceManager rm_Global = null;


    /**
     *  модули, которые не нужно отображать в логе запросов
     */
    
    public static String ignoreModules = ",empty,css/tree,doc/event_cnt,doc/setParam,adm/showLog_noDB"   //ARCH
                + ",doc/getBcInfo"  //ADB
                + ",free/checkSession_noDB,"; //SED

    /*
public void init(ServletConfig config) throws ServletException
{ super.init(config);
  initServlet();
}
public void initServlet() throws ServletException
{
     */
    /**
     *
     * @param config
     * @throws ServletException
     */
    @Override
    public void init(ServletConfig config) throws ServletException {
        super.init(config);
        totalQueryNr = 0;
        try {
            rm_Global = obtainResourceManager();
            rm_Global.servlet = this;
//   System.out.println("--------------------------- INIT: " + this);
//		rm_Global.println("--------------------------- INIT: " + this);
            rm_Global.setObject("ServletConfig", config);
            ServletContext co = config.getServletContext();
            rm_Global.setObject("ServletContext", co);
            rm_Global.setObject("ServletContextName", co.getServletContextName());
            setPaths();
            
            Class.forName(rm_Global.getString("dbDriver"));        // init the JDBC driver

            Logger logger = new Logger(rm_Global);
            rm_Global.setObject("logger", logger);
//		rm_Global.println("--------------------------- INIT - OK.");
        } catch (Exception e) {
            log("!!!!!" + appName + " Init - could not get ResourceManager!", e);
            throw new ServletException("Could not get ResourceManager.");
        }

        appName = rm_Global.getString("ApplicationName");
        if (appName.length() == 0) {
            appName = rm_Global.rFile.toUpperCase();
        }

        /* --- make ParamValidator object (if specified) --- */
        String className = rm_Global.getString("ParamValidatorClassName", false);
        if (className.length() > 0) {
            try {
                Class cl = Class.forName(className);
                ParamValidator pv = (ParamValidator) (cl.newInstance());
                pv.init(rm_Global);
                rm_Global.setObject("ParamValidator", pv);
            } catch (Exception e) {
                rm_Global.logException("!!!!!" + appName + " Init - could not create ParamValidator " + className + "! NULL will be passed.", e);
            }
        }

        /* --- make UserValidator object (if specified) --- */
        className = rm_Global.getString("UserValidatorClassName", false);
        if (className.length() > 0) {
            try {
                Class cl = Class.forName(className);
                rm_Global.setObject("UserValidator", (UserValidator) (cl.newInstance()));
            } catch (Exception e) {
                rm_Global.logException("!!!!!" + appName + " Init - could not create UserValidator " + className + "! NULL will be passed.", e);
            }
        }

        customInit();
        rm_Global.println("--------------------------- INIT " + appName + " - OK! ");
    }

    /**
     * Установка в rm_Global глобальных параметров: TomcatRoot, AppRoot,
     * CfgRootPath, logPath,
     */
    public void setPaths() {
        String myPath = getServletConfig().getServletContext().getRealPath("/");
        myPath = StrUtil.replaceInString(myPath, "\\", "/");
        //  rm_Global.println("... " +  myPath );

// Path to the application root in the server's file system
        rm_Global.setParam("AppRoot", myPath, true);
        if (rm_Global.getString("logPath", false, "").length() == 0) {
            rm_Global.setParam("logPath", myPath + "WEB-INF/", true);
        }

// Path to the Tomcat root in the server's file system
        String t = myPath.substring(0, myPath.length() - 2);
        int i = t.lastIndexOf("/");
        if (i > 0) {
            t = t.substring(0, i + 1);
        }
        rm_Global.setParam("TomcatRoot", t, true);

// Path to the configs root folder
        if (rm_Global.getString("CfgRootPath", false, "").length() == 0) {
            rm_Global.setParam("CfgRootPath", myPath + "WEB-INF/classes/configs/", true);
        }
        rm_Global.println("CfgRootPath:" + rm_Global.getString("CfgRootPath"));

    }

    /**
     *
     */
    public void customInit() {
    }

    /**
     *
     */
    @Override
    public void destroy() {
        rm_Global.println("--------------------------- DESTROY: " + this);
        try {
            String[] keys = rm_Global.getKeys("A");
            for (String key : keys) {
                if (key.contains("dbUtil")) {
                    try {
                        ((DBUtil) rm_Global.getObject(key)).close();
                    } catch (Exception ex) {;
                        /* We don't care */
                    }
                }
            }
        } catch (Exception e) {
            /* We don't care */
        }

        super.destroy();
    }

    /**
     *
     * @return @throws Exception
     */
    public ResourceManager obtainResourceManager() throws Exception {
        return new ResourceManager("def");
    }

    /**
     *
     * @param req
     * @param res
     * @throws ServletException
     * @throws IOException
     */
    @Override
    public void doPost(HttpServletRequest req, HttpServletResponse res)
            throws ServletException, IOException {
//  rm_Global.println("=== Do Post works... - call DoGet");
//  rm_Global.println("=== req:" + req);
        doGet(req, res);
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
       
        if ("XMLHttpRequest".equals(req.getHeader("X-Requested-With"))) {
            req.setCharacterEncoding("utf8"); //ajax ВСЕГДА пересылается в UTF-8.
            res.setCharacterEncoding(rm_Global.getString("clientEncoding", false, "Cp1251"));
        } else {
            req.setCharacterEncoding(rm_Global.getString("clientEncoding", false, "Cp1251")); //ADDED 09.09.2014
            res.setCharacterEncoding(rm_Global.getString("clientEncoding", false, "Cp1251"));
        }
        
        String queryLabel=newQueryLabel();
        logRequest(req, queryLabel);
        
        String reset = req.getParameter("reset");
        if (reset != null && reset.equals("yes")) {
            destroy();
//    initServlet();
            init((ServletConfig) rm_Global.getObject("ServletConfig"));

        }
        new Query(req, res, rm_Global, queryLabel, this);
    }

        
        public void logRequest(HttpServletRequest req, String queryLabel) {
        //System.out.println(""); getRemoteHost getRemoteAddr
        String c = req.getParameter("c");
        if (BasicServlet.ignoreModules.contains("," + c + ",")) {
            return;
        }

        String ip = req.getRemoteHost();
        if(ip.indexOf("159.93.") == 0)
            ip=ip.replace("159.93.", "~");
        else
            ip="EXT " + ip;
        String s = "\n*** " + queryLabel + " [" + Fmt.fullDateStr(new java.util.Date()) + "] " + ip + "; ";
        String mem = "; total=" + Long.toString(Runtime.getRuntime().totalMemory() / (1024 * 1024))
                + "MB,free=" + Long.toString(Runtime.getRuntime().freeMemory() / (1024 * 1024)) + "MB";
        if (req.getMethod().equals("GET")) {
            System.out.println(s + "GET: c=" + c + "; query=" + req.getQueryString() + mem);
        } else {
            System.out.println(s + req.getMethod() + " length=" + req.getContentLength() + " c=" + c + mem);
        }

//HttpSession sess = req.getSession(false);
//if(sess != null) System.out.println( " sess:" + sess.getId()  + "; ");
    }

    /**
     *
     * @return
     */
    public synchronized String newQueryLabel() {
        return appName + "_" + (++totalQueryNr);
    }

}
