/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package jinr.dms;

import dubna.walt.service.CfgCompiler;
import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

/**
 *
 * @author Pavel
 */
public class ServiceCfgDoc extends dubna.walt.service.Service{
    public static String cfgRootPath="";
    
    private List<CfgFileInfo> cfgFiles = null;
    //private static List<CfgDirInfo> cfgDirs = null;
    public String escapehtml(String in){
        return in.replaceAll("&", "&amp;").replaceAll("<", "&lt;").replaceAll(">", "&gt;").replaceAll("\"", "&quot;").replaceAll("'", "&apos;").replaceAll("/", "&#47;").replaceAll("\r\n", "<br/>").replaceAll("#","##");
    }
    public void start () throws Exception
	{
            cfgRootPath = cfgTuner.getParameter("CfgRootPath").replaceAll("\\\\", "/");;
            String dir = cfgTuner.getParameter("dir");
            if(dir==null) dir = "";
            if(cfgTuner.getParameter("reload")!=null||cfgFiles == null){
                cfgFiles = (List<CfgFileInfo>) new ArrayList<CfgFileInfo>();
                String charset = rm.getString("serverEncoding", false, "Cp1251");
                walkDir(cfgRootPath, charset);
            }
            Collections.sort(cfgFiles,cfgFileInfoComparator);
            setDir(dir);
            cfgTuner.outCustomSection("header", out);
            
            cfgTuner.addParameter("dir_list", getDirList(dir));
            cfgTuner.addParameter("file_list", getCfgFileList(dir, cfgTuner.getParameter("name")));
            cfgTuner.outCustomSection("list", out);
            if(cfgTuner.getParameter("name").length()>0){
                outCfgComments(dir,cfgTuner.getParameter("name"));
            }
            cfgTuner.outCustomSection("footer", out);
        }
    private void setDir(String dir){
        //TODO: проверить валидность
        String[] aDir = dir.split("/");
        String upperDir = "";
        if(aDir.length>1){
            for(int i=0 ; i<aDir.length-1; i++){
                upperDir+=aDir[i]+"/";
            }
        }
        cfgTuner.addParameter("upper_dir_path", upperDir);
        cfgTuner.addParameter("this_dir_name", dir);
        String c = cfgTuner.getParameter("c");
        String thisDirUrl  = "<a href='?c="+c+"'>cfg root</a>&nbsp;/&nbsp;";
        for(int i=0 ; i<aDir.length; i++){
            if(aDir[i].length()!=0) thisDirUrl = thisDirUrl+"<a href='?c="+c+"&dir="+aDir[i]+"/'>"+aDir[i]+"</a>&nbsp;/&nbsp;";
        }
        cfgTuner.addParameter("this_dir_url", thisDirUrl);

    }
   private String getDirList(String parentdir) throws Exception{
       String oldDir=null;
       String s = "";
       for(CfgFileInfo cfi : cfgFiles){
           if(!cfi.filepath.equals(oldDir)&&cfi.filepath.startsWith(parentdir)){
               if(dirDepth(parentdir)!=dirDepth(cfi.filepath)-1){
                   continue;
               }
               cfgTuner.setFlashParameter("dir_name", cfi.filepath.replace(parentdir, ""));
               cfgTuner.setFlashParameter("dir_path", cfi.filepath);
               s+=getCustomSectionStr("cfg_dir_item");
               oldDir = cfi.filepath;
           }
       }
       return s;
   }
   int dirDepth (String dirPath){
       if(dirPath.length()==0) return 0;
       String[] levels = dirPath.split("/");
       return levels.length;
   
   }
   /*
		* 
		*/
   public void walkDir( String path, String charset ) {
       
        File root = new File( path );
        File[] list = root.listFiles();
        if (list == null) return;
        for ( File f : list ) {
            if ( f.isDirectory() ) {
                String fpath = f.getAbsolutePath().replaceAll("\\\\", "/");
                walkDir( fpath, charset );
            }
            else {
                String fpath = f.getAbsolutePath().replaceAll("\\\\", "/");
                if(fpath.toLowerCase().endsWith(".cfg")||fpath.toLowerCase().endsWith(".dat")){
                    cfgFiles.add(new CfgFileInfo( fpath, charset ));
                }
            }
        }
    }
    public static final Comparator cfgFileInfoComparator = new Comparator<CfgFileInfo>(){
        @Override
        public int compare(CfgFileInfo o1, CfgFileInfo o2) {
            return o1.filepath.compareTo(o2.filepath);
        }
    
    };

    private String getCfgFileList(String dir, String filename) throws Exception {
        String s = "";
        for(CfgFileInfo cfi : cfgFiles){
                if(cfi.filepath.equals(dir)){
                    if(cfi.filename.equals(filename)) {
                        cfgTuner.setFlashParameter("is_active", "y") ;
                    }else{
                        cfgTuner.setFlashParameter("is_active", "");
                    }
                    
                cfgTuner.setFlashParameter("cfg_name", cfi.filename);
                cfgTuner.setFlashParameter("cfg_descr", cfi.descr);
                cfgTuner.setFlashParameter("has_comments", cfi.hasCommentsSection()+"");
                s+=getCustomSectionStr("cfg_item");
                }
            }
        return s;
    }

    private void outCfgComments(String dir, String name) throws Exception {
        CfgFileInfo found = null;
        for(CfgFileInfo cfi : cfgFiles){
            if(cfi.key.startsWith(dir+name)){
                found=cfi;
                break;
            }
        }
        if(found!=null){
            
            cfgTuner.addParameter("this_cfg_name", found.filename);
            if(found.comments.length()!=0){
                cfgTuner.addParameter("this_cfg_comments", found.comments.replaceAll("\r\n", "<br/>"));
            }else{
                cfgTuner.addParameter("this_cfg_comments", "Секция comments отсутствует");
                cfgTuner.addParameter("no_comments_section", "y");
            }
            
            if(found.input!=null && found.input.length()!=0){
                cfgTuner.addParameter("cfg_input", found.input);
            }
            if(found.output!=null && found.output.length()!=0){
                cfgTuner.addParameter("cfg_output", found.output);
            }     
            
            if(found.testUrl!=null && found.testUrl.length()!=0){
                cfgTuner.addParameter("test_url", found.testUrl);
            }
            if(found.author!=null && found.author.length()!=0){
                cfgTuner.addParameter("cfg_author", found.author);
            }
            
            if(found.parents.size()>0){
                String parents_list = "";
                int cnt = 0;
                for(String parent:found.parents){
                    String filename = "", filePath = "";
                    if(parent.contains("/")){
                        filename = parent.substring(parent.lastIndexOf("/")+1,parent.length());
                        filePath = parent.substring(0,parent.lastIndexOf("/")+1);
                    }else{
                        filename = parent;
                    }
                    
                    if(cnt==0) cfgTuner.setFlashParameter("is_first", "y");
                    if(cnt==found.parents.size()-1) cfgTuner.setFlashParameter("is_last", "y");
                    cfgTuner.setFlashParameter("parent_cnt", cnt+"");
                    cfgTuner.setFlashParameter("parent_dir", filePath);
                    cfgTuner.setFlashParameter("parent_name", filename);
                    cfgTuner.setFlashParameter("is_last", "");
                    cfgTuner.setFlashParameter("is_first", "");
                    parents_list+=getCustomSectionStr("parent");
                    cnt++;
                }
                cfgTuner.addParameter("parents_list", parents_list);
                
            }
            if(found.childs.size()>0){
                int cnt = 0 ;
                String children_list = "";
                for(String child:found.childs){
                    String filename = "", filePath = "", url = "";
                    if(child.contains("/")){
                        filename = child.substring(child.lastIndexOf("/")+1,child.length());
                        filePath = child.substring(0,child.lastIndexOf("/")+1);
                    }else{
                        filename = child;
                    }
                    if(cnt==0) cfgTuner.setFlashParameter("is_first", "y");
                    if(cnt==found.childs.size()-1) cfgTuner.setFlashParameter("is_last", "y");
                    cfgTuner.setFlashParameter("child_cnt", cnt+"");
                    cfgTuner.setFlashParameter("child_dir", filePath);
                    cfgTuner.setFlashParameter("child_name", filename);
                    //cfgTuner.outCustomSection("child", out);
                    
                    cfgTuner.setFlashParameter("is_last", "");
                    cfgTuner.setFlashParameter("is_first", "");
                    children_list+=getCustomSectionStr("child");
                    cnt++;
                }
                cfgTuner.addParameter("children_list", children_list);
            }            
            cfgTuner.addParameter("this_cfg_src",found.content);
            cfgTuner.addParameter("this_cfg_src_html", escapehtml(found.content));
            
            cfgTuner.outCustomSection("cfg_comments", out);
        }
    }
    public String getCustomSectionStr(String sectionName){
        StringBuilder sb = new StringBuilder();
        for (String line : cfgTuner.getCustomSection(sectionName)) {
            sb.append(line+"\r\n");
        }
        return sb.toString();
    }
}
