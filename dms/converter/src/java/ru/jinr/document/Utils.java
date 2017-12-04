package ru.jinr.document;

import java.awt.Dimension;
import java.awt.Frame;
import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.lang.reflect.InvocationTargetException;
import java.nio.ByteBuffer;
import java.nio.CharBuffer;
import java.nio.charset.Charset;
import java.nio.charset.CharsetEncoder;
import java.util.Random;

import javax.swing.JDialog;
import javax.swing.JOptionPane;
import javax.swing.SwingUtilities;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.w3c.dom.Document;
import org.w3c.dom.Element; //AI 120216


public final class Utils {
	private static final String PROP_VM = "eclipse.vm"; //$NON-NLS-1$

	private static final String PROP_VMARGS = "eclipse.vmargs"; //$NON-NLS-1$

	private static final String PROP_COMMANDS = "eclipse.commands"; //$NON-NLS-1$

	public static final String PROP_EXIT_CODE = "eclipse.exitcode"; //$NON-NLS-1$

	public static final String PROP_EXIT_DATA = "eclipse.exitdata"; //$NON-NLS-1$

	private static final String CMD_DATA = "-data"; //$NON-NLS-1$

	private static final String CMD_VMARGS = "-vmargs"; //$NON-NLS-1$

	private static final String NEW_LINE = "\n"; //$NON-NLS-1$

	static int numSeq = 0;  //for test sysrpints
	
	final static String tmpDir = System.getProperty("java.io.tmpdir"); //270215
	
	static int LOG_LEVEL = 2;
	
	
	public static String getPrintableString(String sourceString) {
		if (sourceString == null)
			return null;
		StringBuffer sb = new StringBuffer(removeDoubleSlash(sourceString));

		try {
			if (File.separatorChar == '\\') {
				int index = 0;
				while (index < sb.length()) {
					if (sb.charAt(index) == '\\' || sb.charAt(index) == '\"') {
						sb.insert(index, '\\');
						index++;
					}
					index++;
				}
			}
		}
		catch (Exception e) {
		}
		return sb.toString();
	}
	
	public static String removeDoubleSlash(String sourceString) {
		if (sourceString == null)
			return null;
		StringBuffer sb = new StringBuffer(sourceString);
		try {
			if (File.separatorChar == '\\') {
				int index = sb.length()-2;
				while (index > 0) {
					if (sb.charAt(index) == '\\' && sb.charAt(index+1) == '\\') {
						sb.deleteCharAt(index);
					}
					index--;
				}
			}
			if (File.separatorChar == '/') {
				int index = sb.length()-2;
				while (index > 0) {
					if (sb.charAt(index) == '/' && sb.charAt(index+1) == '/') {
						sb.deleteCharAt(index);
					}
					index--;
				}
			}
		}
		catch (Exception e) {
		}
		return sb.toString();
	}
	
	public static String extractFileDrive(String path) {
		if (path == null || path.equals(""))
			return path;

		int ind = path.indexOf(File.separator);

		if (ind < 0)
			return path;
		else
			return path.substring(0, ind + File.separator.length());
	}
	
	public static String extractFileExt(String path) {
		if(path == null) return null;
		
		int iIndex = path.lastIndexOf(".");
		String ext = "";
		if (iIndex >= 0)
			ext = path.substring(iIndex+1);
		return ext;
	}
	
	public static String extractFileName(String path) {
		if(path == null) return File.separator;
			
		String fileName = path;

		fileName = fileName.replace('/', java.io.File.separatorChar); //for windows
		fileName = fileName.replace('\\', java.io.File.separatorChar); //for unix

		fileName = fileName.substring(fileName.lastIndexOf(java.io.File.separator) + 1);

		return fileName;
	}
	
	public static String extractFileNameWithoutExt(String path) {
		if(path == null) return null;
		String fileName = path;

		fileName = fileName.replace('/', java.io.File.separatorChar); //for windows
		fileName = fileName.replace('\\', java.io.File.separatorChar); //for unix
		try {
			fileName = fileName.substring(fileName.lastIndexOf(java.io.File.separator) + 1);
			fileName = fileName.substring(0, fileName.lastIndexOf("."));
		}
		catch (Exception ex) {}

		return fileName;
	}
	
	public static String extractFilePath(String filenamewithpath) {
		if(filenamewithpath == null) return null;
		String filepath = "";
		int iIndex = filenamewithpath.lastIndexOf("/");
		if (iIndex == -1) {
			iIndex = filenamewithpath.lastIndexOf("\\");
		}
		if (iIndex != -1)
			filepath = filenamewithpath.substring(0, iIndex);
		return filepath;
	}
	public static String getTmpDir() {
		return tmpDir;
	}
	public static String getTmpPath() {
		Random randomGenerator = new Random();
		int randomInt = randomGenerator.nextInt(100000);
		return tmpDir + File.separator+randomInt;
	}

	public static int createConfirmDialog(Frame parent, Dimension screenSize, String message, String szTitle, int option) {
	    JOptionPane pane = new JOptionPane(message, JOptionPane.INFORMATION_MESSAGE, option);
	    JDialog dialog = pane.createDialog(parent, szTitle);
	    dialog.setLocation((int) (screenSize.getWidth() / 2.5),(int) (screenSize.getHeight() / 2.5) ); // Configure
	    dialog.setVisible(true);
	    Object selectedValue = pane.getValue();
	    if(selectedValue == null)
	    	return JOptionPane.CLOSED_OPTION;
	    //If there is not an array of option buttons:
	    if(selectedValue instanceof Integer)
	    	return ((Integer)selectedValue).intValue();
	      
	    return JOptionPane.CLOSED_OPTION;
	}

	static public byte[] convertText2UTF8(InputStream in,String encInp){
		Charset charset = Charset.forName("UTF-8");
		CharsetEncoder encoder = charset.newEncoder();
		
		try {
	        BufferedReader inR = null;
	        if(encInp!=null)
	        	inR = new BufferedReader(new InputStreamReader(in, encInp));
	        else
	        	inR = new BufferedReader(new InputStreamReader(in));
			String html="";
			String str="";			      
			while ((str = inR.readLine()) != null) {
			     html+=str+'\n';
			 }		
			ByteBuffer bbuf = encoder.encode(CharBuffer.wrap(html));
			return bbuf.array();
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}
	//AI 201115
	public static void saveAsXML(Document xmlDocSrc,String outPath) {
		FileOutputStream outF = null;
		try{
			outF = new FileOutputStream(outPath);
			saveAsXML(xmlDocSrc,outF);
			outF.close();
		}
		catch(Exception ex) {
			
		}
	}
	public static void saveAsXML(Document xmlDocSrc, OutputStream out) {
		try{
			Transformer transformer = TransformerFactory.newInstance().newTransformer();
			DOMSource domSource = new DOMSource(xmlDocSrc);
			OutputStreamWriter osw = new OutputStreamWriter(out);
			ByteArrayOutputStream bos = new ByteArrayOutputStream();
			StreamResult xmlOuput = new StreamResult(bos);
	
			transformer.transform(domSource, xmlOuput);
			//osw.write(URLEncoder.encode(bos.toString()));
			osw.write(bos.toString());
			osw.flush();
		} catch (Throwable e) {
			e.printStackTrace();
		}
	}
	//AI 120216
	public static boolean readXMLBooleanAttr(String attrName, Element el) {
		if(attrName == null || el == null)
			return false;
		String tmp = el.getAttribute(attrName);
		if(tmp != null && "true".equalsIgnoreCase(tmp))
			return true;
		else
			return false;
	}
	
	//AE 18.03.2016 do not use now because has problem with relaunch
	public static String buildCommandLine(String workspace) {
		String property = System.getProperty(PROP_VM);
		if (property == null) {
			return null;
		}

		StringBuffer result = new StringBuffer(512);
		result.append(property);
		result.append(NEW_LINE);
//		//AE added
//		result.append("-Duser.dir="+workspace);
//		result.append(NEW_LINE);

		// append the vmargs and commands. Assume that these already end in \n
		String vmargs = System.getProperty(PROP_VMARGS);
		if (vmargs != null) {
			result.append(vmargs);
		}

		// append the rest of the args, replacing or adding -data as required
		property = System.getProperty(PROP_COMMANDS);
		if (property == null) {
			result.append(CMD_DATA);
			result.append(NEW_LINE);
			result.append(workspace);
			result.append(NEW_LINE);
		} else {
			// find the index of the arg to add/replace its value
			int cmd_data_pos = property.lastIndexOf(CMD_DATA);
			if (cmd_data_pos != -1) {
				cmd_data_pos += CMD_DATA.length() + 1;
				result.append(property.substring(0, cmd_data_pos));
				result.append(workspace);
				// append from the next arg
				int nextArg = property.indexOf("\n-", cmd_data_pos - 1); //$NON-NLS-1$
				if (nextArg != -1) {
					result.append(property.substring(nextArg));
				}
			} else {
				result.append(CMD_DATA);
				result.append(NEW_LINE);
				result.append(workspace);
				result.append(NEW_LINE);
				result.append(property);
			}
		}

		// put the vmargs back at the very end (the eclipse.commands property
		// already contains the -vm arg)
		if (vmargs != null) {
			if (result.charAt(result.length() - 1) != '\n') {
				result.append('\n');
			}
			result.append(CMD_VMARGS);
			result.append(NEW_LINE);
			result.append(vmargs);
		}

		return result.toString();
	}
	
	//AI
	public static Object getClassInstance(Object owner, String className) {
		Object retv = null;
		try {
			retv = owner.getClass().getClassLoader().loadClass(className).newInstance();
//			Class cls = owner.getClass().getClassLoader().loadClass(className);
//			if(cls != null)
//				retv = cls.getConstructor(new Class[]{String.class}).newInstance(); //newInstance(new String[] {});
		}
		catch(Exception e1) {
			e1.printStackTrace();
		}
		return retv;
	}
	//AE 12.08.2016
	public static Object getClassInstance(Object owner, String className,Class cl, Object obj) {
		Object retv = null;
		try {
			Class cls = owner.getClass().getClassLoader().loadClass(className);
			if(cls != null)
				retv = cls.getConstructor(new Class[]{cl}).newInstance(new Object[] {obj}); //newInstance(new String[] {});
		}
		catch(Exception e1) {
			e1.printStackTrace();
		}
		return retv;
	}

	 public static String createTempFile(byte[] byteArray,String name,String ext) throws IOException {
		 return createTempFile(new ByteArrayInputStream(byteArray),name,ext);
	 }	
	 public static String createTempFile(InputStream is,String name,String ext) throws IOException {
			Random randomGenerator = new Random();
			int randomInt = randomGenerator.nextInt(100000);
			if(name==null)
				name="noname";
			String outPath = System.getProperty("java.io.tmpdir");
			if(!outPath.endsWith(File.separator))
				outPath+=File.separator;
			outPath+=randomInt+name.charAt(0)+name.charAt(name.length()-1)+ext;
	    	FileOutputStream out = new FileOutputStream(outPath);

	    	byte[] buff = new byte[512];
	    	int len = is.read(buff);
	    	while (len != -1) {
	    		out.write(buff, 0, len);
	    		len = is.read(buff);
	    	}
	    	is.close();
	    	out.close();
	    	
	    	return outPath;
	    }
	 
		static public void startInNewThread(Runnable doBeanWork, boolean _wait) throws InterruptedException, InvocationTargetException {
			Thread awt_thread = new Thread(doBeanWork);
			awt_thread.start();
			if(_wait) {
				awt_thread.join();
			}
		}
		public static boolean deleteDirectory(File directory) {
		    if(directory.exists()){
		        File[] files = directory.listFiles();
		        if(null!=files){
		            for(int i=0; i<files.length; i++) {
		                if(files[i].isDirectory()) {
		                    deleteDirectory(files[i]);
		                }
		                else {
		                    files[i].delete();
		                }
		            }
		        }
		    }
		    return(directory.delete());
		}
		public static void printMessage(String mes,String id,String servletName,int log_level){
			if(LOG_LEVEL>=log_level)
				System.out.println(servletName+":"+id+"  "+ mes);
		}

}


