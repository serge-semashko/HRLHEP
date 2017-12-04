package jinr.arch;

import dubna.walt.service.Service;
import java.util.Vector;
import java.sql.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;


public class setRightsServiceOnTree extends Service {
    private int curTreeID = -1;

    public void start() throws Exception {
        if (cfgTuner.getParameter("activeNode").equalsIgnoreCase("")) {
            cfgTuner.outCustomSection("noTreeID",out);
            return;
        }
        curTreeID = cfgTuner.getIntParameter("activeNode");
        if (curTreeID < 0) {
            System.out.println("-------- class setRightsService = Can't parseInt cfgTuner.getParameter(\"activeNode\")");
            cfgTuner.outCustomSection("noTreeID",out);
            return;
        }

        updateDbRights();
        updateUsrLab();
        cfgTuner.outCustomSection("report",out);
    }
    
    private void updateUsrLab()throws Exception{
        String[] p = cfgTuner.getParameters();
        for (int i = 0 ; i<p.length; i++){
            try {
        String[] pp = p[i].split("=");
        //    \A__usr_lab(\d+)\z
	Pattern r = Pattern.compile("\\A^__usr_lab(\\d+)");
	Matcher rm = r.matcher(p[i]);
	if (rm.find()) {
            int userId = Integer.parseInt(rm.group(1));
            int labCode = Integer.parseInt(cfgTuner.getParameter(pp[0]));
                dbUtil.update("DELETE FROM user_lab WHERE User_ID = "+userId+"");
            if(labCode>0){
                String sLabCode=cfgTuner.getParameter(pp[0]);
                dbUtil.update("INSERT INTO user_lab (USER_ID, LAB_CODE) VALUES (" + userId + ",'" + sLabCode + "')");
                
            }
//            System.out.println("::::"+rm.group(1)+":"+cfgTuner.getParameter(pp[0]));
	} 
} catch (PatternSyntaxException ex) {
	
}
            
        
        }
    }
    
    
    
    private void updateDbRights() throws Exception {
        Vector treeRights = getUserRightsOnTree(curTreeID);
        Vector curRights = parseRights(getArrayRights());

        boolean isDone = false;
        if (curRights!=null) {
            for (int i = 0; i < curRights.size(); i++) {
                String[] curS = (String[])curRights.elementAt(i);
                isDone = false;
                for (int j = 0; j < treeRights.size(); j++) {
                    String[] treeS = (String[])treeRights.elementAt(j);
                    if ((treeS[0].equalsIgnoreCase(curS[0])) && (treeS[1].equalsIgnoreCase(curS[1]))) {
                        if (treeS[2].equalsIgnoreCase(curS[2])) {
                            treeRights.removeElementAt(j);
                            isDone = true;                        
                            break;
                        }
                        else {
                            try
                            { 
//															System.out.println("-----Begin PL1");
															cfgTuner.addParameter("U_ID",curS[0]);
															cfgTuner.addParameter("T_ID",curS[1]);
															cfgTuner.addParameter("RIGHT_ID",curS[2]);
															getData("addRights");
//															System.out.println("-----END PL1");															
                            }
                            catch (Exception e)
                            { 
                              cfgTuner.addParameter("PLSQL_ERROR", e.toString());   
                              e.printStackTrace(System.out);
                            } 
                            
                            //addRightsForTree(String.valueOf(curTreeID), String.valueOf(addRightsToDB(curS[0],curS[1],curS[2])),curS[0],curS[1]);
                            treeRights.removeElementAt(j);
                            isDone = true;                        
                            break;
                        }
                    }
                }
                if (!isDone) {
                    //System.out.println("addRightsForTree("+String.valueOf(curTreeID)+","+curS[0]+","+curS[1]+","+curS[2]+");");
                     try
                     { 
//											 System.out.println("-----Begin PL2");
                       Connection conn = dbUtil.getConnection();
                       CallableStatement cs = conn.prepareCall("begin addRightsForUserOnTree(:1,:2,:3); end;");
                       cs.setInt(1,Integer.parseInt(curS[0]));
                       cs.setInt(2,Integer.parseInt(curS[1]));
                       cs.setInt(3,Integer.parseInt(curS[2]));
                       cs.execute();
//											 System.out.println("-----End PL1");											 
                     }
                     catch (Exception e)
                     { 
                       cfgTuner.addParameter("PLSQL_ERROR", e.toString());   
                       e.printStackTrace(System.out);
                     } 
                    //addRightsForTree(String.valueOf(curTreeID), String.valueOf(addRightsToDB(curS[0],curS[1],curS[2])),curS[0],curS[1]);
                }
            }
        }
        if (!treeRights.isEmpty()) {
					//        System.out.println("-----Begin DELETE");
            String whereConditionU = "";
            String whereConditionT = "";
            for (int i = 0; i < treeRights.size(); i++) {
                String[] treeS = (String[])treeRights.elementAt(i);
                whereConditionU+=treeS[0]+",";
                whereConditionT+=treeS[1]+",";                
            }
            whereConditionU = whereConditionU.substring(0,whereConditionU.length()-1);
            whereConditionT = whereConditionT.substring(0,whereConditionT.length()-1);
//					System.out.println("DELETE FROM UserRightsOnTree WHERE User_ID IN ("+whereConditionU+") AND Tree_ID IN ("+whereConditionT+")");
            dbUtil.update("DELETE FROM UserRightsOnTree WHERE User_ID IN ("+whereConditionU+") AND Tree_ID IN ("+whereConditionT+")");
        }
//				System.out.println("-----END ALL");
    }
    
    /**
    * ѕолучает права на ID узла дерева из базы данных
    * @param treeID ID узла дерева
    * @return {@link Vector} массивов строк из 3 элементов <br>
    *         String[0] - USER_ID, <br>
    *         String[1] - TREE_ID, <br>
    *         String[2] - Rights, <br>
    */
    private Vector getUserRightsOnTree(int treeID) throws Exception { 
        Vector v = new Vector();
        String[] s;
        ResultSet res = dbUtil.getResults(" SELECT r.USER_ID, r.TREE_ID, r.Rights FROM UserRightsOnTree r "+ 
                                          " WHERE r.TREE_ID="+String.valueOf(treeID)); 
        while (res.next()) {
            s = new String[4];
            s[0] = res.getString("USER_ID");
            s[1] = res.getString("TREE_ID");
            s[2] = res.getString("Rights");
            v.add(s);
        }
        return v;
    }    

    /**
     * @param r массив строк полученых от getArrayRights
     * @return {@link Vector} массивов строк из 3 элементов String[0] - ID, String[1] - activeNode, String[2] - Rights
     */
    public Vector parseRights(String[] r) {
        if (r==null) return null;    
        String[] s;
        Vector v = new Vector();
        boolean isAdd = true;
        for (int i=0; i < r.length; i++) {
            s = new String[3];
            s[0] = r[i].substring(8,r[i].indexOf("]"));
            s[1] = r[i].substring(r[i].indexOf("]")+2,r[i].indexOf("]",r[i].indexOf("]")+2));
            s[2] = r[i].substring(r[i].indexOf("=")+1,r[i].length());
            for (int j=0; j < v.size(); j++) {
                if ((((String[])v.elementAt(j))[0].equalsIgnoreCase(s[0])) && (((String[])v.elementAt(j))[1].equalsIgnoreCase(s[1]))) {
                    if ((((String[])v.elementAt(j))[2].equalsIgnoreCase("2")) && (s[2].equalsIgnoreCase("1"))) {
                        ((String[])v.elementAt(j))[2] = "3";
                        isAdd = false;
                        break;
                    }
                    if ((((String[])v.elementAt(j))[2].equalsIgnoreCase("1")) && (s[2].equalsIgnoreCase("2"))) {
                        ((String[])v.elementAt(j))[2] = "3";
                        isAdd = false;
                        break;
                    }
                }
                isAdd = true;                
            }
            if (isAdd) v.addElement(s);
        }
        return v;
    }


    /**
     * ѕолучает из cfgTuner параметры вида rright[id][activeNode]=V или wright[id][activeNode]=V, <br>
     * где <ul><li><b>id</b> - ID пользовател€ <br>
     *     <li><b>activeNode</b> - ID дерева <br>
     *     <li><b>V</b> - «начение права (при rright - 1, при wright - 2)</ul>
     * @return массив строк вида rright[id][activeNode]=V и wright[id][activeNode]=V
     */
    public String[] getArrayRights() {
        String[] arr=cfgTuner.getParameters();
        Vector v = new Vector();
        for (int i=0; i < arr.length; i++) {
            if ((arr[i].toLowerCase().indexOf("ruright",0)==0) || (arr[i].toLowerCase().indexOf("wuright",0)==0)) {
                v.addElement(arr[i]);
            }
        }
        if ( v.size() == 0 )
            return null;
        String[] vArray = new String[v.size()];
        v.copyInto(vArray);
        return vArray;
    }

}
