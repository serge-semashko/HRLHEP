/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package jinr.dms;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;

/**
 *
 * @author Pavel
 */
public class CfgFileInfo {
    public String filename="";
    public String filepath="";
    public String comments="";
    public String content="";
    public String key = "";
    public String descr = "";
    public String testUrl = "";
    public String input = "";
    public String output = "";
    public String author = "";
    public List<String> parents = new ArrayList<String>();
    public List<String> childs = new ArrayList<String>();
    public String isNull(String in){
        return in==null?"":in;
    }

    public CfgFileInfo(String fpath, String charset){
        
        InputStream fis = null;
        InputStreamReader isr = null;
        BufferedReader br = null;
        try
        {
            fis = new FileInputStream(fpath);
            isr = new InputStreamReader(fis, charset);
            br = new BufferedReader(isr);
            String line = "";
            while ((line = br.readLine()) != null) {
                content+=line+"\r\n";
            }
            filename = fpath.substring(fpath.lastIndexOf("/")+1, fpath.length());
            //filename = fpath.substring(fpath.lastIndexOf("/")+1, fpath.length()-4);
            
            filepath = fpath.substring(0,fpath.lastIndexOf("/")+1);
            if(filepath.startsWith(ServiceCfgDoc.cfgRootPath)){
                filepath = filepath.substring(ServiceCfgDoc.cfgRootPath.length());
               
            }
            key = filepath + filename;
            comments = getNamedSectionContent("comments");
            descr = getCommentParameter("descr",false,true);
            testUrl = getCommentParameter("testURL",true);
            input = getCommentParameter("input",true);
            output = getCommentParameter("output",true);
            author = getCommentParameter("author",true);
            if(testUrl==null||testUrl.length()==0)testUrl=getCommentParameter("test_URL",true);
            
            if(getCommentParameter("childs",false)!=null){
                String[] sChilds = getCommentParameter("childs",true).split(",");
                for(int i = 0 ; i < sChilds.length; i++ ){
                    if(sChilds[i].length()!=0)
                    childs.add(sChilds[i]);
                }
            }
            if(getCommentParameter("parents",false)!=null){
                String[] sParents = getCommentParameter("parents",true).split(",");
                for(int i = 0 ; i < sParents.length; i++ ){
                    if(sParents[i].length()!=0){
                        parents.add(sParents[i]);
                    }
                }
                
            }
        }
        catch(Exception e){
            e.printStackTrace();
        }finally{
            try{
            fis.close();
            isr.close();
            br.close();
            }catch(Exception e){
            }
        }
        
    }
    public boolean hasCommentsSection(){
        return !(comments.length()==0);
    }
    public String getCommentParameter(String name, boolean delFromComments){
        return getCommentParameter( name, delFromComments,  delFromComments);
    }
    public String getCommentParameter(String name, boolean delParameterValue, boolean delParameterName){
            Pattern regex = Pattern.compile("^" + name + "=(.*)", Pattern.MULTILINE);
            Matcher regexMatcher = regex.matcher(comments);
            if(regexMatcher.find()){
                String replaceRX = "(?m)(^" + name + "=)(.*)(\r\n)";
              if(delParameterValue&delParameterName){
                  comments = comments.replaceAll(replaceRX, "");
              }else if(delParameterValue){
                  comments = comments.replaceAll(replaceRX, "$1$3");
              }else if(delParameterName){
                  comments = comments.replaceAll(replaceRX, "$2$3");
              }
              return  regexMatcher.group(1);   
            }  
            return "";
    }
    public String getNamedSectionContent(String sectionName){
        String res = "";
        
        try {
	Pattern regex = Pattern.compile("^\\["+sectionName+"\\]\r\n(.+?)\\[end\\]", Pattern.DOTALL | Pattern.MULTILINE);
	Matcher regexMatcher = regex.matcher(this.content);
        	if (regexMatcher.find()) {
                	res = regexMatcher.group(1);
                } 
        } catch (PatternSyntaxException ex) {
            ex.printStackTrace();
        }

        
        
        return res;
    }



}
