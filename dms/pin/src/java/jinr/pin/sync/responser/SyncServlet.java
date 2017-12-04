package jinr.pin.sync.responser;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.security.PublicKey;
import java.security.Signature;
import java.security.SignatureException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.util.Properties;
import java.util.ResourceBundle;
import java.util.zip.GZIPOutputStream;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import jinr.pin.sync.ConnectionHelper;
import jinr.pin.sync.KeysHelper;
import jinr.pin.sync.SyncException;

import org.Base64;

/**
 * Сервлет синхронизации со СМЭН
 * @author Andrey
 */
public class SyncServlet extends HttpServlet {
	public static final String CMDS_PARAM_NAME = "cmds";
	public static final String SIGN_PARAM_NAME = "sign";
	private static final long serialVersionUID = 1961777044365621194L;
	private static final String PIN_PROPERTIES_FILE_NAME = "/pin"; // .properties
	private ConnectionHelper ch;
	private KeysHelper keysHelper;
	private String myUrl;

	@Override
	public void init() throws ServletException {
		super.init();
		
		ResourceBundle pinProperties = ResourceBundle.getBundle(PIN_PROPERTIES_FILE_NAME);
		try {
			Class.forName(pinProperties.getString("dbDriver"));
			
			myUrl = pinProperties.getString("Server");
			
			ch = new ConnectionHelper(
				pinProperties.getString("connString") + pinProperties.getString("database") + pinProperties.getString("connParam"),
				pinProperties.getString("usr"),
				pinProperties.getString("pw")
			);
			
			keysHelper = new KeysHelper(ch);
			
		} catch (Exception e) {
			e.printStackTrace();
			throw new ServletException(e);
		}
	}
	
	@Override
	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		System.out.println("GET");
		response.getWriter().print("<html>" +
			"<head><title>PIN manual synchronisation form</title></head>" +
			"<body><form method=POST>" +
				"Commands:<br><textarea name="+CMDS_PARAM_NAME+" cols=80 rows=20></textarea>" +
				"<p>Signature:<br><textarea name="+SIGN_PARAM_NAME+" cols=80 rows=10></textarea>" +
				"<br><input type=submit>" +
			"</form></body>" +
			"</html>");
	}
	
	@Override
	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		System.out.println(request.getHeaderNames());
		
		try {
			String cmds = request.getParameter(CMDS_PARAM_NAME);
			if(cmds==null || cmds.length()==0) throw new IOException(CMDS_PARAM_NAME + " parametr is empty");
			byte[] cmdsBytes = Base64.decode(cmds);
			
			String sign = request.getParameter(SIGN_PARAM_NAME);
			if(sign==null || sign.length()==0) throw new IOException(SIGN_PARAM_NAME + " parametr is empty");
			byte[] signBytes = Base64.decode(sign);
			
			checkSignature(cmdsBytes, signBytes);
			
			ObjectInputStream in = new ObjectInputStream(new ByteArrayInputStream(cmdsBytes));
			ByteArrayOutputStream outputBuf = new ByteArrayOutputStream();
			GZIPOutputStream gzipOut = new GZIPOutputStream(outputBuf);
			ObjectOutputStream out = new ObjectOutputStream(gzipOut);
			
			Connection con = null;
			
			try {
				new SyncHelper(in, out, con = ch.getConnection());
			} catch (Throwable e) {
				throw new SyncException(e);
			} finally {
				ch.closeSql(null, null, con);
			}
			out.flush();
			out.close();
			gzipOut.flush();
			gzipOut.close();
			
			response.setHeader("Content-Disposition", 
				"attachment; filename=\"response.dat\"");
			
			outputBuf.writeTo(response.getOutputStream());
			
		}catch(Throwable e) {
			e.printStackTrace();
			StringWriter wr = new StringWriter();
			wr.append("\n\n");
			e.printStackTrace(new PrintWriter(wr));
			response.sendError(515, wr.toString());
		}
		
		System.gc();
	}

	private void checkSignature(byte[] data, byte[] sign) {
		try {
			Signature dsa = Signature.getInstance("SHA1withDSA");			
			dsa.initVerify(keysHelper.getPublicKey(myUrl));
			dsa.update(data);
			if(!dsa.verify(sign)) throw new SignatureException();
		} catch(Exception e) {
			throw new RuntimeException(e);
		}
	}
}
