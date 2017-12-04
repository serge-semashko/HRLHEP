/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package jinr.sed;

import java.text.MessageFormat;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.Map;
import java.util.UUID;
import java.util.Vector;
import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.file.FileSystem;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.nio.file.StandardOpenOption;

/**
 * @author Eugeny Alexandrov
 * Замена значение в шаблонах типа *.docx
 * Подстановка значений хэш-таблицы
 */


public class DocxReplace {
	private int[] indexes = new int[2];
    private boolean isChange = false;
    private boolean isOneString = false;
    static String tmpDir = null;
//    private int fontSize = 0;
    
    static final String PROPERTY_FONT_SIZE = "font_size"; // Integer value
    static final String PROPERTY_BOLD = "bold"; // Boolean value
//	private class WordElement{
//		public String value = null;
//		public int startIndex;
//	}
	public DocxReplace(){
		if(tmpDir==null){
			try{
				tmpDir = System.getProperty("java.io.tmpdir");
			}
			catch(Exception ex){
				ex.printStackTrace();
			}
			if(tmpDir==null)
				tmpDir="/tmp";
		}
	}
//	public DocxReplace(int fontSize){
//		this();
//		this.fontSize = fontSize;
//	}
	final static String magicKey = "#";
	final static String startContentTag = "<w:t";
	final static String endContentTag = "</w:t>";
//	public void setFontSize(int fontSize){
//		this.fontSize = fontSize;
//	}
	private int getIndexOfEndStartContentTag(String content,int ind){
		int i = content.indexOf(">", ind+startContentTag.length());
		if(i<0)
			i=ind+startContentTag.length();		
		return i+1;
	}
	private int searchWord(String content,int startIndex){
		indexes[0] = content.indexOf(magicKey,startIndex);
		indexes[1] = 0;
		if(indexes[0]>0){
			indexes[1] = content.indexOf(magicKey,indexes[0]+1);
		}
		if(indexes[1]<=0)
			return -1;
		return indexes[0];
	}
	private String getFullWord(String content){
		String res = "";
		isOneString = false;
		int ind = content.indexOf(startContentTag,indexes[0]);
		if(ind>indexes[1]){// all text in middle
			res+=content.substring(indexes[0]+1, indexes[1]);
			isOneString = true;
			//indexes[0] = indexes[1];
			return res;
		}
		int indE = content.indexOf(endContentTag,ind);
		if(indE>indexes[1])
			indE=indexes[1];
		while(ind>0 && ind<indexes[1] && indE>0 && indE<=indexes[1]){
			res+=content.substring(getIndexOfEndStartContentTag(content,ind), indE);
			ind = content.indexOf(startContentTag,indE);
			if(ind>0){
				indE = content.indexOf(endContentTag,ind);
				if(indE>indexes[1])
					indE=indexes[1];
			}
		}
		return res;
	}
	
	private String replaceWord(String content,String word){
		String res = "";
		if(isOneString){
			res+=content.substring(0, indexes[0])+word;
			res+=content.substring(indexes[1]+1,content.length());
			return res;
		}
		res+=content.substring(0, indexes[0])+word;
		int ind = indexes[0]+1;//content.indexOf(startContentTag,indexes[0]-startContentTag.length());
		int indE = content.indexOf(endContentTag,ind);
		if(!(ind>0 && ind<indexes[1] && indE>0 && indE<indexes[1]))
			return content;
		//res+=content.substring(0, ind+startContentTag.length())+word;
		int lastIndex = indE;
		boolean isFirst = true;
		while(ind>0 && getIndexOfEndStartContentTag(content,ind)<indexes[1] && indE>0){
			if(isFirst){
				isFirst = false;
			}
			else
				res += content.substring(lastIndex, getIndexOfEndStartContentTag(content,ind));
			lastIndex = indE;
			ind = content.indexOf(startContentTag,getIndexOfEndStartContentTag(content,ind));
			if(ind>0)
				indE = content.indexOf(endContentTag,ind);
			if(indE>indexes[1]+1){
				lastIndex=indexes[1]+1;
				break;
			}
//				indE=indexes[1]+1;
		}
		res += content.substring(lastIndex, content.length());
		return res;
	}
	private String addVector(Vector<String> word,Hashtable props){
//		if(word.size()==0)
//			return "";
//		if(word.size()==1)
//			return word.elementAt(0);<w:rPr><w:color w:val="000000"/><w:sz w:val="24"/><w:szCs w:val="24"/></w:rPr>
		String res ="";
		for(int i=0;i<word.size();i++){
			if(i!=0)
				res+="</w:t></w:r></w:p><w:p><w:r><w:t>";
//			if(i!=0){
				//res+="</w:t></w:r></w:p><w:p><w:r><w:t>";
			if(props!=null){
				res+="<w:rPr>";
				try{
					if(props.containsKey(DocxReplace.PROPERTY_FONT_SIZE)){
						int fontSize=((Integer)props.get(DocxReplace.PROPERTY_FONT_SIZE)).intValue();
						res+="<w:sz w:val=\""+2*fontSize+"\"/><w:szCs w:val=\""+2*fontSize+"\"/>";
					}
				}catch(Exception ex){
					ex.printStackTrace();
				}
				try{
					if(props.containsKey(DocxReplace.PROPERTY_BOLD) && ((Boolean)props.get(DocxReplace.PROPERTY_BOLD)).booleanValue())
						res+="<w:b/>";
				}catch(Exception ex){
					ex.printStackTrace();
				}
				res+="</w:rPr>";
						//if(fontSize!=0)
						//res+="<w:rPr><w:sz w:val=\""+2*fontSize+"\"/><w:szCs w:val=\""+2*fontSize+"\"/></w:rPr>";
//				}
			}
			res += word.elementAt(i);
		}
			
		return res;
	}
	private String replaceWordMap(String content,Vector<String> word,Hashtable props){
		String res = "";
		if(isOneString){
			res+=content.substring(0, indexes[0])+addVector(word,props);
			res+=content.substring(indexes[1]+1,content.length());
			return res;
		}
		res+=content.substring(0, indexes[0])+addVector(word,props);
		int ind = indexes[0]+1;//content.indexOf(startContentTag,indexes[0]-startContentTag.length());
		int indE = content.indexOf(endContentTag,ind);
		if(!(ind>0 && ind<indexes[1] && indE>0 && indE<indexes[1]))
			return content;
		//res+=content.substring(0, ind+startContentTag.length())+word;
		int lastIndex = indE;
		boolean isFirst = true;
		while(ind>0 && getIndexOfEndStartContentTag(content,ind)<indexes[1] && indE>0){
			if(isFirst){
				isFirst = false;
			}
			else
				res += content.substring(lastIndex, getIndexOfEndStartContentTag(content,ind));
			lastIndex = indE;
			ind = content.indexOf(startContentTag,getIndexOfEndStartContentTag(content,ind));
			if(ind>0)
				indE = content.indexOf(endContentTag,ind);
			if(indE>indexes[1]+1){
				lastIndex=indexes[1]+1;
				break;
			}
//				indE=indexes[1]+1;
		}
		res += content.substring(lastIndex, content.length());
		return res;
	}
	public static Path getUniqueFileName(String directory, String extension) {
	    String fileName = MessageFormat.format("{0}.{1}", UUID.randomUUID(), extension.trim());
	    return Paths.get(directory, fileName);
	}
	
	private String replaceContentTag(String cont){
		String wrongContentTag = "<w:t xml:space=\"preserve\">";
		return cont.replaceAll(wrongContentTag, startContentTag);
	}
	public boolean replaceMap(String in,String out,Hashtable<String, Vector<String>> map,Hashtable<String, Hashtable> mapPr){
		Path scrP = Paths.get(in);
		Path dstFinalP = Paths.get(out);
		Path dstP = null;
		try {
			dstP = DocxReplace.getUniqueFileName(tmpDir, "docx");
		} catch (Exception e2) {			
			e2.printStackTrace();
			boolean res = copyFile(scrP, dstFinalP);
			return res;
		}
		boolean res = copyFile(scrP, dstP);
		if(map.size()==0 || !res){
			return res;
		}
//		Path path = Paths.get("test.txt");
		Charset charset = StandardCharsets.UTF_8;
	      /* Define ZIP File System Properies in HashMap */    
        Map<String, String> zip_properties = new HashMap<>(); 
        /* We want to read an existing ZIP File, so we set this to False */
        zip_properties.put("create", "false");
        /* Specify the encoding as UTF -8 */
        zip_properties.put("encoding", "UTF-8"); 
//        File f = new File(in);
//        URI zip_disk = f.toURI();
        try {
			//zip_disk = new URI(in);
	        try (FileSystem zipfs = FileSystems.newFileSystem(dstP, null)) {
	        	Path externalTxtFile = zipfs.getPath("/word/document.xml");
	        	String content = new String(Files.readAllBytes(externalTxtFile), charset);
	        	int index=0;
	        	while((index=searchWord(content,index))>0){
	        		String word = getFullWord(content);
	        		if(isOneString)
	        			index = indexes[1];
	        		if(word.length()==0){
	        			index++;
	        			continue;
	        		}
		            Enumeration<String> keys=map.keys();
		            while(keys.hasMoreElements()){
		            	String key = keys.nextElement();
		            	if(word.equals(key)){
		            		content = replaceWordMap(content,map.get(key),mapPr.get(key));
		            		//content = content.replaceAll("direktor", map.get(key));
		            		isChange = true;
		            	}
		            }
		            index++;
	        	}
	            if(isChange){
	            	Files.deleteIfExists(externalTxtFile);
	            	Files.write(externalTxtFile, content.getBytes(charset),StandardOpenOption.CREATE);
	            }
	        } catch (Exception e) {
				e.printStackTrace();
				return true;
			}
		} catch (Exception e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}//zip_disk.getPath()
        res = copyFile(dstP, dstFinalP);
        try{
        	dstP.toFile().delete();
        }catch(Exception ex){
        	ex.printStackTrace();
        }

		return true;
		}
	public boolean replace(String in,String out,Hashtable<String, String> map){
		Path scrP = Paths.get(in);
		Path dstFinalP = Paths.get(out);
		Path dstP = null;
		try {
			dstP = DocxReplace.getUniqueFileName(tmpDir, "docx");
		} catch (Exception e2) {			
			e2.printStackTrace();
			boolean res = copyFile(scrP, dstFinalP);
			return res;
		}
		boolean res = copyFile(scrP, dstP);
		if(map.size()==0 || !res){
			return res;
		}
//		Path path = Paths.get("test.txt");
		Charset charset = StandardCharsets.UTF_8;
	      /* Define ZIP File System Properies in HashMap */    
        Map<String, String> zip_properties = new HashMap<>(); 
        /* We want to read an existing ZIP File, so we set this to False */
        zip_properties.put("create", "false");
        /* Specify the encoding as UTF -8 */
        zip_properties.put("encoding", "UTF-8"); 
//        File f = new File(in);
//        URI zip_disk = f.toURI();
        try {
			//zip_disk = new URI(in);
	        try (FileSystem zipfs = FileSystems.newFileSystem(dstP, null)) {
	        	Path externalTxtFile = zipfs.getPath("/word/document.xml");
	        	String content = new String(Files.readAllBytes(externalTxtFile), charset);
	        	int index=0;
	        	while((index=searchWord(content,index))>0){
	        		String word = getFullWord(content);
	        		if(isOneString)
	        			index = indexes[1];
	        		if(word.length()==0){
	        			index++;
	        			continue;
	        		}
		            Enumeration<String> keys=map.keys();
		            while(keys.hasMoreElements()){
		            	String key = keys.nextElement();
		            	if(word.equals(key)){
		            		content = replaceWord(content,map.get(key));
		            		//content = content.replaceAll("direktor", map.get(key));
		            		isChange = true;
		            	}
		            }
		            index++;
	        	}
	            if(isChange){
	            	Files.deleteIfExists(externalTxtFile);
	            	Files.write(externalTxtFile, content.getBytes(charset),StandardOpenOption.CREATE);
	            }
	        } catch (Exception e) {
				e.printStackTrace();
				return true;
			}
		} catch (Exception e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}//zip_disk.getPath()
        res = copyFile(dstP, dstFinalP);
        try{
        	dstP.toFile().delete();
        }catch(Exception ex){
        	ex.printStackTrace();
        }

//		String content = new String(Files.readAllBytes(path), charset);
//		content = content.replaceAll("foo", "bar");
//		Files.write(path, content.getBytes(charset));	
		return true;
	}
	
	boolean copyFile(Path scrP, Path dstP){
		try {
			Files.copy(scrP, dstP, StandardCopyOption.REPLACE_EXISTING);
			return true;
		} catch (Exception e) {
			e.printStackTrace();
		}
		return false;
		
	}
	
}
