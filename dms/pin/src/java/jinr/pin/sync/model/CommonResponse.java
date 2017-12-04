package jinr.pin.sync.model;

import java.io.ObjectInputStream;
import java.io.Serializable;
import java.util.Map;
import java.util.TreeMap;

public class CommonResponse implements Serializable {
	private static final long serialVersionUID = -6111489703317984827L;

	final public String requestId;
	public String requestTitle;
	
	public Map<String, Serializable> results = new TreeMap<String, Serializable>();
	
	public CommonResponse(ObjectInputStream in) throws Exception {
		requestId = (String)in.readObject();
		requestTitle = (String)in.readObject();
		int commandsCount = in.readInt();
		
		while(--commandsCount >= 0) {
			String commandKey = (String)in.readObject();
			Serializable data = (Serializable)in.readObject();
						
			results.put(commandKey, data);
		}
	}
}
