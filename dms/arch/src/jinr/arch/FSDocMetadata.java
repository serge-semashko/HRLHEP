/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package jinr.arch;

import java.io.File;
import java.io.PrintWriter;
import java.sql.Connection;
import java.util.HashMap;
import jinr.arch.dbtools.CommonTools;
import jinr.arch.dbtools.DBSelect;

/**
 *
 * @author Pavel
 */
public class FSDocMetadata extends dubna.walt.service.Service{
    @Override
    public void beforeStart() throws Exception
{
    
    Connection con = dbUtil.getConnection();
    String filepathschema =cfgTuner.getParameter("filepathschema");
    String fileextension =cfgTuner.getParameter("fileextension");
    String[] sp = filepathschema.split("/");
    String[] aSQL = cfgTuner.getCustomSection("GET METADATA");
    String sSQL = "";
    for(int i = 0 ; i < aSQL.length ; i ++){
        sSQL +="\n"+ aSQL[i];
    }
    HashMap hm = DBSelect.getRow(sSQL, null, con);    
    if(hm.isEmpty()) return;
    
    String path = "";
        for (String sp1 : sp) {
            path += "/" + hm.get(sp1);
        }
    if(!path.isEmpty()) path = path.substring(1);

    String fileStoragePath = cfgTuner.getParameter("file_storage_path");
    if( fileStoragePath != null && ! fileStoragePath.isEmpty() ){
        String filename = fileStoragePath+path+fileextension;
        mkDirs(filename);
        PrintWriter pw = new PrintWriter(filename,"UTF-8");
        pw.print(hm);
        pw.close();
        cfgTuner.addParameter("METADATA_PATH", path+fileextension);
    }
    

}
    private void mkDirs(String filepath){
     String dirs = filepath.substring(0,filepath.lastIndexOf("/"));
     File f = new File(dirs);
     if(!f.exists()){
        f.mkdirs();
     }
    }
    @Override
        public void afterStart() throws Exception{
            Connection con = dbUtil.getConnection();
            String[] aSQL = cfgTuner.getCustomSection("SET METADATA PATH");
            String sSQL = "";
            for(int i = 0 ; i < aSQL.length ; i ++){
                sSQL +="\n"+ aSQL[i];
            }
            DBSelect.executeStatement(sSQL, null, con);
            super.afterStart();
        }
    
}
