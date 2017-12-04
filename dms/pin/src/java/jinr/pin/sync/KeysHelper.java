package jinr.pin.sync;

import java.io.FileWriter;
import java.io.IOException;
import java.security.KeyFactory;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.SecureRandom;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Map;
import java.util.TreeMap;

import org.Base64;
import org.Base64.InputStream;

/**
 * Класс-помошник в генерации и хранении ключей подписи
 * Ключи хранятся в таблице KEYS_TABLE_NAME, если такой таблицы
 * нет, класс автоматически её создает
 */
public class KeysHelper {
	private static final String KEYS_TABLE_NAME = "sync_keys";
	private static final String SMAN_PUBLIC_FILENAME = "/sman.public";
	private ConnectionHelper ch;

	public KeysHelper(ConnectionHelper ch) throws SQLException {
		this.ch = ch;
		
		checkDatabase();
	}
		
	private void checkDatabase() {
		Connection con = null;
		try {
			con = ch.getConnection();
			
			ResultSet tables = con.getMetaData().getTables(null, null, KEYS_TABLE_NAME, null);
			if(!tables.next()) createKeysTable(con);
			ch.closeSql(tables, null, null);
			
		} catch (Throwable e) {
			throw new RuntimeException(e);
		} finally {
			ch.closeSql(null, null, con);
		}		
	}

	private void createKeysTable(Connection con) throws SQLException {
		PreparedStatement st = con.prepareStatement(
			"CREATE TABLE " + KEYS_TABLE_NAME + "(" +
				"url VARCHAR(100) NOT NULL, " +
				"public_key VARCHAR(1000) NOT NULL, " +
				"private_key VARCHAR(1000) NOT NULL, " +
				"PRIMARY KEY (url)" +
			")");
		
		st.executeUpdate();
		ch.closeSql(null, st, null);
	}

	private Map<String, PublicKey> publicKeys = new TreeMap<String, PublicKey>();
	private Map<String, PrivateKey> privateKeys = new TreeMap<String, PrivateKey>();

	public synchronized void initAndSave(String url) throws Exception {
		url = prepareUrl(url);
		
		KeyPairGenerator keyGen = KeyPairGenerator.getInstance("DSA");
    	SecureRandom random = SecureRandom.getInstance("SHA1PRNG");
    	keyGen.initialize(1024, random);
    	
    	KeyPair pair = keyGen.generateKeyPair();
    	
    	String encodedPublicKey = Base64.encodeBytes(pair.getPublic().getEncoded());
    	String encodedPrivateKey = Base64.encodeBytes(pair.getPrivate().getEncoded());
    	
		Connection con = null;
		try {
			con = ch.getConnection();
			
			PreparedStatement st = con.prepareStatement(
				"DELETE FROM " + KEYS_TABLE_NAME +
				" WHERE url=?");
			st.setString(1, url);
			st.executeUpdate();
			ch.closeSql(null, st, null);
			
			st = con.prepareStatement(
				"INSERT INTO " + KEYS_TABLE_NAME +
				" (url, public_key, private_key) VALUES(?, ?, ?)");
			st.setString(1, url);
			st.setString(2, encodedPublicKey);
			st.setString(3, encodedPrivateKey);
			st.executeUpdate();
			ch.closeSql(null, st, null);
			
		} catch (Throwable e) {
			throw new RuntimeException(e);
		} finally {
			ch.closeSql(null, null, con);
		}
		
		privateKeys.put(url, pair.getPrivate());
		publicKeys.put(url, pair.getPublic());
	}
	
	private String prepareUrl(String url) {
		return url.toLowerCase().trim().replaceFirst("/*$", "");
	}

	public synchronized PublicKey getPublicKey(String url) throws Exception {
		url = prepareUrl(url);
		
		PublicKey publicKey = publicKeys.get(url);
		if(publicKey!=null) return publicKey;
		
		Connection con = null;
		try {
			con = ch.getConnection();
			
			PreparedStatement st = con.prepareStatement(
				"SELECT public_key" +
				" FROM " + KEYS_TABLE_NAME + 
				" WHERE url=?");
			st.setString(1, url);
			ResultSet res = st.executeQuery();
			if(res.next()) {
				X509EncodedKeySpec pubKeySpec = new X509EncodedKeySpec(Base64.decode(res.getString(1)));
		    	KeyFactory keyFactory = KeyFactory.getInstance("DSA");

		        publicKey = keyFactory.generatePublic(pubKeySpec);
			}
			
			ch.closeSql(res, st, null);
			
		} catch (Throwable e) {
			throw new RuntimeException(e);
		} finally {
			ch.closeSql(null, null, con);
		}
		
		if(null==publicKey) publicKey = getPublicFromFile();
		if(null==publicKey) throw new RuntimeException("Cant find public key for url: " + url);
		publicKeys.put(url, publicKey);
		
		return publicKey;
	}

	private PublicKey getPublicFromFile() throws IOException, NoSuchAlgorithmException, InvalidKeySpecException {
		InputStream in = new Base64.InputStream(KeysHelper.class.getResourceAsStream(SMAN_PUBLIC_FILENAME));
		byte[] encKeyT = new byte[10000];
		int encKeyLen = in.read(encKeyT);
		in.close();
		byte[] encKey = new byte[encKeyLen];
		System.arraycopy(encKeyT, 0, encKey, 0, encKeyLen);
		
		X509EncodedKeySpec pubKeySpec = new X509EncodedKeySpec(encKey);
    	KeyFactory keyFactory = KeyFactory.getInstance("DSA");

        return keyFactory.generatePublic(pubKeySpec);
	}
	
//	public synchronized static PrivateKey readPrivateKey() throws Exception {
//		if(privateKey!=null) return privateKey;
//		
//		InputStream in = new Base64.InputStream(KeysHelper.class.getResourceAsStream(SMAN_PRIVATE_FILENAME));
//		byte[] encKeyT = new byte[10000];
//		int encKeyLen = in.read(encKeyT);
//		in.close();
//		byte[] encKey = new byte[encKeyLen];
//		System.arraycopy(encKeyT, 0, encKey, 0, encKeyLen);
//		
//		PKCS8EncodedKeySpec privKeySpec = new PKCS8EncodedKeySpec(encKey);
//    	KeyFactory keyFactory = KeyFactory.getInstance("DSA");
//
//        return privateKey = keyFactory.generatePrivate(privKeySpec);
//	}
	
	public synchronized PrivateKey getPrivateKey(String url) throws Exception {
		url = prepareUrl(url);
		
		PrivateKey privateKey = privateKeys.get(url);
		if(privateKey!=null) return privateKey;
		
		Connection con = null;
		try {
			con = ch.getConnection();
			
			PreparedStatement st = con.prepareStatement(
				"SELECT private_key" +
				" FROM " + KEYS_TABLE_NAME + 
				" WHERE url=?");
			st.setString(1, url);
			ResultSet res = st.executeQuery();
			if(res.next()) {
				PKCS8EncodedKeySpec privKeySpec = new PKCS8EncodedKeySpec(Base64.decode(res.getString(1)));
		    	KeyFactory keyFactory = KeyFactory.getInstance("DSA");
		
		        privateKey = keyFactory.generatePrivate(privKeySpec);
			}
			
			ch.closeSql(res, st, null);
			
		} catch (Throwable e) {
			throw new RuntimeException(e);
		} finally {
			ch.closeSql(null, null, con);
		}
		
		
		if(null==privateKey) throw new RuntimeException("Cant find private key for url: " + url);
		privateKeys.put(url, privateKey);
		
		return privateKey;
	}
}
