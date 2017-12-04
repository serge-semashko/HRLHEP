package jinr.pin.sync.model;

import java.io.IOException;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.rmi.server.UID;
import java.util.Map;
import java.util.TreeMap;

public class CommonRequest<R extends Serializable> implements Serializable {
	private static final long serialVersionUID = -8971909021766410929L;

	final public String id = new UID().toString();
	public String title;
	public R reservedData;
	public String url;
	
	/**
	 * key => sql
	 */
	public Map<String, String> commands = new TreeMap<String, String>();
	
	public void writeTo(ObjectOutputStream out) throws IOException {
		out.writeObject(id);
		out.writeObject(title);
		
		out.writeInt(commands.size());
		
		for(Map.Entry<String, String>e : commands.entrySet()) {
			out.writeObject(e.getKey());
			out.writeObject(e.getValue());
		}
	}
	
	public void onResponse(CommonResponse response) throws Exception {
		
	}
	
	public void onResponseError(Throwable ex) throws Exception {
		
	}
}
