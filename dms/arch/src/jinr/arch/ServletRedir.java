package jinr.arch;

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
public class ServletRedir extends Arch {
    
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
            String clientEncoding = rm_Global.getString("clientEncoding", false, "windows-1251");
            req.setCharacterEncoding(clientEncoding); //ADDED 09.09.2014
            res.setCharacterEncoding(clientEncoding);
            res.setContentType("text/html; charset=" + clientEncoding);

            System.out.println("******* ARCH REDIRECT ==> A5");
                PrintWriter outWriter = new PrintWriter(new OutputStreamWriter(res.getOutputStream(), clientEncoding));
                outWriter.println("<html><body><p><b><center><br><br><br>База документов перенесена на другой сервер. Перенаправление запроса...</center></b> <script  type=\"text/javascript\">window.location.replace(\"https://lt-a5.jinr.ru/arch/arch\"); </script></body></html>");
                outWriter.flush();
                outWriter.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    
}
