/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package jinr.sed;

import dubna.walt.util.ResourceManager;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 *
 * @author serg
 */
public class ServletRedir extends dubna.walt.BasicServlet {
    

    /**
     *
     * @return @throws Exception
     */
    @Override
    public ResourceManager obtainResourceManager() throws Exception {
        System.out.println(".\n\r.\n\r.\n\r*** SED ServletRedir - INIT ...");
//  Load static resources for the app
        ResourceManager rm = new ResourceManager("dms");
        return rm;
    }

    /**
     *
     */
    @Override
    public void customInit() {
        try {
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
//            super.doGet(req, res);
            String clientEncoding = rm_Global.getString("clientEncoding", false, "utf8");
            req.setCharacterEncoding(clientEncoding); //ADDED 09.09.2014
            res.setCharacterEncoding(clientEncoding);
            res.setContentType("text/html; charset=" + clientEncoding);

            System.out.println("******* REDIRECT ==> ");
                PrintWriter outWriter = new PrintWriter(new OutputStreamWriter(res.getOutputStream(), clientEncoding));
                outWriter.println("<html><body><p><b><center><br><br><br>СЭД перенесен на другой сервер. Перенаправление запроса...</center></b> <script  type=\"text/javascript\">window.location.replace(\"https://lt-a4.jinr.ru/sed/dubna\"); </script></body></html>");
                outWriter.flush();
                outWriter.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    
}
