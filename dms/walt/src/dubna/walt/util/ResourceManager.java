package dubna.walt.util;

import dubna.walt.BasicServlet;
import static dubna.walt.util.IOUtil.getLogWriter;
import java.util.*;
import java.io.*;

/**
 *
 * @author serg
 */
public class ResourceManager {

    /**
     *
     */
    public String rFile = "";
    ResourceBundle rb = null;

    /**
     *
     */
    protected Hashtable ht = null;

    /**
     *
     */
    public BasicServlet servlet = null;

    /**
     *
     */
    public ResourceManager() {
        this.ht = new Hashtable(100);
    }

    /**
     *
     * @param hashFileName
     * @param encoding
     * @throws Exception
     */
    public ResourceManager(String hashFileName, String encoding) throws Exception {
        ht = new Hashtable(200);
        String[] ss;
        if (hashFileName.length() < 1) {
            return;
        }
        //     rm.println("... opening " + fileName + "; charset: " + charset);
        BufferedReader br
                = new BufferedReader(
                        new InputStreamReader(
                                new FileInputStream(hashFileName), encoding), 8192);

        String str = null;
        while ((str = br.readLine()) != null) {
            ss = str.split("=");
            if (ss.length == 2) {
                ht.put(ss[0], ss[1]);
            }
        }
    }

    /**
     *
     * @param resourceFileName
     * @throws Exception
     */
    public ResourceManager(String resourceFileName) throws Exception {
        ht = new Hashtable(200);
        if (resourceFileName.length() < 1) {
            return;
        }
        rFile = resourceFileName;
        rb = ResourceBundle.getBundle(resourceFileName);
//	println("Loading '"+resourceFileName+".properties' - OK");
    }

    /**
     *
     */
    public void reset() {
        ht.clear();
    }

    /**
     *
     * @param key
     * @param value
     */
    public synchronized void putString(String key, String value) {
        setParam(key, value, false);
    }

    /**
     *
     * @param key
     * @param value
     */
    public synchronized void setParam(String key, String value) {
        setParam(key, value, false);
    }

    /**
     *
     * @param key
     * @param value
     * @param global
     */
    public synchronized void setParam(String key, String value, boolean global) {
        ht.remove(key);
        if (global) {
            ResourceManager rm_Global = getGlobalRM();
            if (rm_Global != null) {
//		  println("--- set GLOBAL param:" + key + "='" + value+ "'" + this.toString());
                rm_Global.setParam(key, value, false);
            } else {
                if (key.length() > 0 && value.length() > 0) {
                    ht.put(key, value);
//				println(" direct GLOBAL==>" + key + "='" + value + "'" + this.toString());
                }
            }
        } else {
            if (key.length() > 0 && value.length() > 0) {
                ht.put(key, value);
//			println("store -->" + key + "='" + value + "';" + this.toString());
            }
        }
    }

    /**
     *
     * @param key
     * @param obj
     */
    public synchronized void putObject(String key, Object obj) {
        setObject(key, obj, false);
    }

    /**
     *
     * @param key
     * @param obj
     */
    public synchronized void setObject(String key, Object obj) {
        setObject(key, obj, false);
    }

    /**
     *
     * @param key
     * @param obj
     * @param global
     */
    public synchronized void setObject(String key, Object obj, boolean global) {
//  println(this.toString().substring(16) + ": setObject: '" + key + "': " + obj);
        ht.remove(key);

        if (global) {
            ResourceManager rm_Global = getGlobalRM();
            if (rm_Global != null) {
                rm_Global.setObject(key, obj, false);
            } else if (obj != null) {
                ht.put(key, obj);
            }
        } else {
            if (obj != null) {
                ht.put(key, obj);
            }
        }
    }

    /**
     *
     * @param key
     */
    public synchronized void removeParam(String key) {
        ht.remove(key);
        ResourceManager rm_Global = getGlobalRM();
        if (rm_Global != null) {
            rm_Global.removeParam(key);
        }
    }

    /**
     *
     * @param name
     * @param showErrMsg
     * @param defValue
     * @return
     */
    public synchronized String getString(String name, boolean showErrMsg, String defValue) {
        /*if (getGlobalRM() == null)
		 print("..GLB..");
		 else
		 print("--(" + name + "," + showErrMsg+",'" + defValue + "'): ");
         */
        if (name.isEmpty()) {
            return "";
        }
        try {
            if (ht.containsKey(name)) { // println("HT:'" + (String) ht.get(name) + "'");
                return (String) ht.get(name);
            }
        } catch (Exception e) {/* don't warry yet - just not found a string */

        }

        ResourceManager rm_Global = getGlobalRM();
        if (rm_Global != null) {
            String res = rm_Global.getString(name, false, "");
//	  if (res != null && res.length() > 0) println( "GLOBAL:" + res);
            if (res != null && res.length() > 0) {
                return res;
            }
        }

        try { // println( "RB: '" + rb.getString(name) + "'");
            if (rb != null) {
                return rb.getString(name);
            }
        } catch (Exception e) {
            if (showErrMsg) {
                servlet.log("+++ ResourceManager.getString: '" + name + "' not found! '" + defValue + "' returned. showErrMsg=" + showErrMsg, e);
            }
        }
        return defValue;
    }

    /**
     *
     * @param name
     * @param showErrMsg
     * @return
     */
    public synchronized String getString(String name, boolean showErrMsg) {
        return getString(name, showErrMsg, "");
    }

    /**
     *
     * @param name
     * @return
     */
    public synchronized String getString(String name) {
        return getString(name, true, "");
    }

    /**
     *
     * @param name
     * @param defValue
     * @return
     */
    public synchronized int getInt(String name, int defValue) {
        String s = getString(name, false, Integer.toString(defValue)).trim();
//		if (s.length() == 0) {
//			return defValue;
//		}
        try {
            int val = Integer.parseInt(s);
            return val;
        } catch (NumberFormatException e) {
            return defValue;
        }
    }

    /**
     *
     * @param name
     * @return
     */
    public synchronized boolean getBoolean(String name) {
        String s = getString(name, false).trim();
        if (s.length() == 0) {
            return false;
        }
        return (s.equalsIgnoreCase("true"));
    }

    /**
     *
     * @param name
     * @return
     */
    public synchronized int getInt(String name) {
        String s = getString(name, true).trim();
        if (s.length() == 0) {
            return 0;
        }
        return (Integer.parseInt(s));
    }

    /**
     *
     * @param name
     * @param showErrMsg
     * @return
     */
    public synchronized Object getObject(String name, boolean showErrMsg) {
//  print("getObject name:" + name + " ...");
        if (ht.containsKey(name)) {
//    Object o = ht.get(name);
//    println(name + ": " + o.getClass().getName());
            return ht.get(name);
        }

        ResourceManager rm_Global = getGlobalRM();
        if (rm_Global != null) { // print("rm_Global:" + rm_Global.toString() + "; ");
            Object res = rm_Global.getObject(name, false);
            if (res != null) {
                return res;
            }
        }

        try {
            if (rb != null) {
                return rb.getObject(name);
            }
        } catch (MissingResourceException e) {
            if (showErrMsg) {
                println("ResourceManager: Object '" + name
                        + "' not found! " + this.toString());
            }
        }

        return null;
    }

    /**
     *
     * @return
     */
    public ResourceManager getGlobalRM() {
        if (ht.containsKey("rm_Global")) {
            return (ResourceManager) ht.get("rm_Global");
        } else {
            return null;
        }
    }

    /**
     *
     * @param name
     * @return
     */
    public synchronized Object getObject(String name) {
        return getObject(name, true);
    }

    /**
     * Gets the object associated with the specified key in the dictionary. The
     * key is assumed to be a String, which is matched against the
     * wildcard-pattern keys in the dictionary.
     *
     * @param key the string to match
     * @return
     * @returns the element for the key, or null if there's no match
     */
    public synchronized Object getMatchingObject(String key) {
//	String sKey = (String) key;
//HashTable
//	String sKey = key;	
        Enumeration e = ht.keys();
        Object defObject = null;
        while (e.hasMoreElements()) {
            String thisKey = (String) e.nextElement();
            if (thisKey.equals("*")) {
                defObject = ht.get(thisKey);
            } else {
//      println("=== " + key + ":" + thisKey);
                if (StrUtil.match(thisKey, key)) {
//        println("Yes:" + ht.get(thisKey));
                    return ht.get(thisKey);
                }
            }
        }
        ResourceManager rm_Global = getGlobalRM();
        if (rm_Global != null) {
            defObject = rm_Global.getMatchingObject(key);
        }

        return defObject;
    }

    /**
     *
     * @return
     */
    public synchronized ResourceManager cloneRM() { // ResourceManager rm = null;
        //try {
//	rm = new ResourceManager(rFile);
        ResourceManager rm = new ResourceManager();
        rm.rFile = this.rFile;
        rm.rb = this.rb;
        rm.ht = (Hashtable) this.ht.clone();
        rm.ht.remove("startId");
        rm.servlet = this.servlet;
//	}
//  catch (Exception e)	{ 	}
        return rm;
    }

    /**
     *
     * @param key
     */
    public synchronized void removeKey(String key) {
//  println("Removing " + key);
        ht.remove(key);
        ResourceManager rm_Global = getGlobalRM();
        if (rm_Global != null) {
            rm_Global.removeKey(key);
        }
    }

    /**
     * Gets the array of keys containing in the ResourceManager.
     *
     * @param kind
     * @return
     * @parameter kind defines type of the keys to be returned: - "R" - keys
     * from the resource file only - "D" - the dynamically addad keys only - "A"
     * - all keys.
     */
    public synchronized String[] getKeys(String kind) {
        Enumeration e;
        Vector v = new Vector(100, 100);

        if (!kind.equals("R")) {
            e = ht.keys();
            while (e.hasMoreElements()) {
                v.addElement(e.nextElement());
            }
        }

        if (rb != null) {
            if (kind.equals("R") || kind.equals("A")) {
                e = rb.getKeys();
                while (e.hasMoreElements()) {
                    v.addElement((String) e.nextElement());
                }
            }
        }

        String[] sa = new String[v.size()];
        v.copyInto(sa);
        return sa;
    }

    /**
     *
     * @return
     */
    public synchronized Object[] getAll() {
        Enumeration e = ht.keys();
        ArrayList v = new ArrayList(100);
        String s;

        v.add("!!!~~~~~~~ Dynamic Elements ~~~~~~~");
        System.out.println("=========================== RM.getAll() =========================");
        while (e.hasMoreElements()) {
            s = (String) e.nextElement();
            v.add(s + "=" + ht.get(s));
            System.out.println("... name=" + s + "; value=" + ht.get(s) + ";");
        }
        Collections.sort(v);
        v.add("\n\r~~~~~~~ Elements from the resource file ~~~~~~~");
        if (rb != null) {
            e = rb.getKeys();
            while (e.hasMoreElements()) {
                s = (String) e.nextElement();
                v.add(s + "=" + rb.getString(s));
            }
        }
//                String[] sa = (String[]) v.toArray();
//		String[] sa = new String[v.size()];
//		v.copyInto(sa);
        return v.toArray();
    }

    /**
     *
     * @return
     */
    /*
    public synchronized String[] getAll() {
		Enumeration e = ht.keys();
//		Vector v = new Vector(100, 100);
		ArrayList v = new ArrayList(100);
		String s;

		v.addElement("~~~~~~~ Dynamic Elements ~~~~~~~");
		while (e.hasMoreElements()) {
			s = (String) e.nextElement();
			v.addElement(s + "=" + ht.get(s));
//		if (getGlobalRM()==null)
//			println(s + "=" + ht.get(s));
		}
		v.addElement("\n\r~~~~~~~ Elements from the resource file ~~~~~~~");
		if (rb != null) {
			e = rb.getKeys();
			while (e.hasMoreElements()) {
				s = (String) e.nextElement();
				v.addElement(s + "=" + rb.getString(s));
			}
		}
		String[] sa = new String[v.size()];
		v.copyInto(sa);
		return sa;
	}
/*
    
     */
    /**
     *
     * @param text
     */
    public void print(String text) {
//		if (getBoolean("TomcatLog"))
        {
            System.out.print(text);
        }
//		else
//		{		
////		  servlet.log(text);
//		  servlet.log(text, null);
//		}
    }

    /**
     *
     * @param text
     */
    public void println(String text) {
//		if (getBoolean("TomcatLog")) {
        print(text + "\n");
//		} else {
//			print(text + "; ");
//		}
//		  print(text + "<br>\n");
    }

    /**
     *
     * @param msg
     * @param e
     */
    public void logException(String msg, Exception e) {
        if (servlet == null) {
            servlet = (BasicServlet) getObject("Servlet");
        }
        if (servlet != null) {
            servlet.log(msg, e);
        }
        IOUtil.writeLog("*** EXCEPTION:<br><xmp>" + e.toString() + "</xmp><hr>", this);
        try {
            println(msg);
//		  PrintWriter lpw = new PrintWriter(getLogFile());
//		  FileOutputStream fs = IOUtil.getLogFile(this);
//		  PrintWriter lpw = new PrintWriter(fs);
            PrintWriter lpw = getLogWriter(this, true);
            if (lpw != null) {
                e.printStackTrace(lpw);
                lpw.close();
            } else {
                e.printStackTrace(System.out);
            }
        } catch (Exception ex) {;
        }
    }

    /**
     *
     * @param e
     */
    public void logException(Exception e) {
        logException("***", e);
    }

    /**
     *
     * @param out
     */
    public synchronized void logAll(PrintWriter out) {
        if (out == null) {
            return;
        }
//		String[] rm_keys = getAll();
        Object[] rm_keys = getAll();
        out.println("<b>ResourceManager content: </b><xmp>");
        for (int i = 0; i < rm_keys.length; i++) {
            out.println((String) rm_keys[i]);
        }
        out.println("</xmp><hr>");
//  out.flush();

        ResourceManager rm_Global = getGlobalRM();
        if (rm_Global != null) {
            Enumeration e = rm_Global.ht.keys();
            out.println("<b>Global ResourceManager content: </b><br>~~~~~~~ Dynamic Elements ~~~~~~~<xmp>");
            String s = "";
            while (e.hasMoreElements()) {
                s = (String) e.nextElement();
                out.println(s + "=" + rm_Global.ht.get(s));
            }
            out.println("</xmp><hr>");
        }
//  out.flush();
    }

}
