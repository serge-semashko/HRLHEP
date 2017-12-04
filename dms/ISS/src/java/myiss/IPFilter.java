package myiss;

import java.io.IOException;
import java.util.StringTokenizer;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletResponse;

public class IPFilter implements Filter {

    private FilterConfig config;

    public final static String IP_RANGE = "159.93";

    public IPFilter() {
    }

    @Override
    public void init(FilterConfig filterConfig) throws ServletException {

        this.config = filterConfig;

    }

    @Override
    public void doFilter(ServletRequest request, ServletResponse rrr,
            FilterChain chain) throws IOException, ServletException {

        String ip = request.getRemoteAddr();

        HttpServletResponse httpResp = null;

        if (rrr instanceof HttpServletResponse) {
            httpResp = (HttpServletResponse) rrr;
        }

        StringTokenizer toke = new StringTokenizer(ip, ".");
        int dots = 0;
        String byte1 = "";
        String byte2 = "";
        String client = "";

        while (toke.hasMoreTokens()) {

            ++dots;

      //if we've reached the second dot, break and check out the indx
            // value
            if (dots == 1) {

                byte1 = toke.nextToken();

            } else {

                byte2 = toke.nextToken();
                break;
            }
        }//while

    //Piece together half of the client IP address so it can be compared
        // with
        //the forbidden range represented by IPFilter.IP_RANGE
        client = byte1 + "." + byte2;

        if (!IP_RANGE.equals(client)) {

            httpResp.sendError(HttpServletResponse.SC_FORBIDDEN,
                    "Site is accessible from JINR IPs only.<br/>Сайт доступен только из сети ОИЯИ.");

        } else {

            chain.doFilter(request, rrr);
        }

    }// doFilter

    @Override
    public void destroy() {
        /*
         * called before the Filter instance is removed from service by the web
         * container
         */
    }

}
