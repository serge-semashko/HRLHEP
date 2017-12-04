package jinr.pin.sync.requester;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.Signature;
import java.security.SignatureException;
import java.util.zip.GZIPInputStream;

import org.Base64;

import jinr.pin.sync.KeysHelper;
import jinr.pin.sync.model.CommonRequest;
import jinr.pin.sync.model.CommonResponse;
import jinr.pin.sync.responser.SyncServlet;

public class RequestHelper {
	KeysHelper keysHelper;
	
	public RequestHelper(KeysHelper keysHelper) {
		this.keysHelper = keysHelper;
	}

	public CommonResponse makeRequest(CommonRequest request) throws Exception {
		String postString = getPostString(request);
		
		String url = request.url.trim();
		if(url.endsWith("/")) url += "sync";
		else url += "/sync";
		
		HttpURLConnection con = (HttpURLConnection)new URL(url).openConnection();
		con.setRequestMethod("POST");
		con.setRequestProperty("Content-Length", "" + postString.length());
		con.setDoInput(true);
		con.setDoOutput(true);

		OutputStream out = con.getOutputStream();
		out.write(postString.getBytes());
		out.flush();
		out.close();
		
		if(con.getResponseCode() != con.HTTP_OK) {
			byte[] buf = new byte[1000];
			int len = con.getErrorStream().read(buf);
			con.disconnect();
			throw new RuntimeException(new String(buf, 0, len));
		}
		
		
//		int b;
//		while((b = con.getInputStream().read()) > 0) System.out.write(b);
		
		CommonResponse response = new CommonResponse(new ObjectInputStream(new GZIPInputStream(con.getInputStream())));
		
		con.disconnect();
		
		return response;
		
	}

	private String getPostString(CommonRequest request) throws IOException, NoSuchAlgorithmException, InvalidKeyException, Exception, SignatureException {
		byte[] requestBuf = getRequestBytes(request);
		
		byte[] sign = getRequestSign(requestBuf, keysHelper.getPrivateKey(request.url));
		
//		System.out.println(Base64.encodeBytes(requestBuf));
//		System.out.println();
//		System.out.println();
//		System.out.println(Base64.encodeBytes(sign));
		
		String postString = SyncServlet.CMDS_PARAM_NAME + "=" + URLEncoder.encode(Base64.encodeBytes(requestBuf))
			+ "&" + SyncServlet.SIGN_PARAM_NAME + "=" + URLEncoder.encode(Base64.encodeBytes(sign));
		
		return postString;
	}

	private byte[] getRequestSign(byte[] requestBuf, PrivateKey privateKey) throws NoSuchAlgorithmException, InvalidKeyException, Exception, SignatureException {
		Signature dsa = Signature.getInstance("SHA1withDSA");
		dsa.initSign(privateKey);
		dsa.update(requestBuf);
		byte[] sign = dsa.sign();
		return sign;
	}

	private byte[] getRequestBytes(CommonRequest request) throws IOException {
		ByteArrayOutputStream outputBuffer = new ByteArrayOutputStream();
		ObjectOutputStream out = new ObjectOutputStream(outputBuffer);
		
		request.writeTo(out);
		out.close();
		
		byte[] requestBuf = outputBuffer.toByteArray();
		return requestBuf;
	}
}
