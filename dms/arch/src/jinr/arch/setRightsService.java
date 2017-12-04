package jinr.arch;

import dubna.walt.service.Service;
import java.util.Vector;
import java.sql.*;


public class setRightsService extends Service {
    private int curTreeID = -1;

    public void start() throws Exception {
        if (cfgTuner.getParameter("activeNode").equalsIgnoreCase("")) {
            cfgTuner.outCustomSection("noTreeID",out);
            return;
        }
        try {
            curTreeID = Integer.parseInt(cfgTuner.getParameter("activeNode"));
        }
        catch (NumberFormatException e) {
            System.out.println("-------- class setRightsService = Can't parseInt cfgTuner.getParameter(\"activeNode\")");
            cfgTuner.outCustomSection("noTreeID",out);
            return;
        }  

        updateDbRights();
        cfgTuner.outCustomSection("report",out);
    }
    
    private int addRightsToDB(String gID, String rID, String r) throws Exception {
        int result = -1;
        ResultSet res = dbUtil.getResults("SELECT Rt_ID FROM Rights WHERE G_ID="+gID+" AND R_ID="+rID+" AND Rights="+r);
        if (res.next()) {
            result = res.getInt("Rt_ID");
        }
        else {
            dbUtil.update("INSERT INTO Rights (G_ID, R_ID, Rights) VALUES ("+gID+","+rID+","+r+")");
            ResultSet rtID = dbUtil.getResults("SELECT Rt_ID FROM Rights WHERE G_ID="+gID+" AND R_ID="+rID+" AND Rights="+r);
            if (rtID.next())
                result = rtID.getInt("Rt_ID");            
            else
                result = -1;
        }
        dbUtil.closeResultSet(res);
        return result;
    }
    
    private boolean addRightsForTree(String treeID, String rtID, String gID, String rID) throws Exception {
        if (rtID.equalsIgnoreCase("-1")) return false;
        dbUtil.update("DELETE FROM TreeRights WHERE Rt_ID IN ("+
                          " SELECT r.Rt_ID FROM Rights r, TreeRights rt"+
                          " WHERE r.Rt_ID = rt.Rt_ID AND (r.R_ID="+rID+") AND (r.G_ID="+gID+") AND (rt.Tree_ID="+treeID+")"+
                          ")");
        dbUtil.update("INSERT INTO TreeRights (Tree_ID, Rt_ID) VALUES ("+treeID+", "+rtID+")");
        return true;
    }
    
    private void updateDbRights() throws Exception {
        Vector treeRights = getTreeRights(curTreeID);
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
                              Connection conn = dbUtil.getConnection();
                              CallableStatement cs = conn.prepareCall("begin addRightsForTree(:1,:2,:3,:4); end;");
                              cs.setInt(1,curTreeID);
                              cs.setInt(2,Integer.parseInt(curS[0]));
                              cs.setInt(3,Integer.parseInt(curS[1]));
                              cs.setInt(4,Integer.parseInt(curS[2]));
                              cs.execute();
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
                       Connection conn = dbUtil.getConnection();
                       CallableStatement cs = conn.prepareCall("begin addRightsForTree(:1,:2,:3,:4); end;");
                       cs.setInt(1,curTreeID);
                       cs.setInt(2,Integer.parseInt(curS[0]));
                       cs.setInt(3,Integer.parseInt(curS[1]));
                       cs.setInt(4,Integer.parseInt(curS[2]));
                       cs.execute();
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
            String whereCondition = "";
            for (int i = 0; i < treeRights.size(); i++) {
                String[] treeS = (String[])treeRights.elementAt(i);
                whereCondition+=treeS[3]+",";
            }
            whereCondition = whereCondition.substring(0,whereCondition.length()-1);
            dbUtil.update("DELETE FROM TreeRights WHERE Tree_ID = "+String.valueOf(curTreeID)+" AND Rt_ID IN ("+whereCondition+")");
        }
    }
    
    /**
    * ѕолучает права на ID узла дерева из базы данных
    * @param treeID ID узла дерева
    * @return {@link Vector} массивов строк из 4 элементов <br>
    *         String[0] - G_ID, <br>
    *         String[1] - R_ID, <br>
    *         String[2] - Rights, <br>
    *         String[3] - Rt_ID<br>
    */
    private Vector getTreeRights(int treeID) throws Exception { 
        Vector v = new Vector();
        String[] s;
        ResultSet res = dbUtil.getResults(" SELECT r.Rt_ID, r.G_ID, r.R_ID, r.Rights, tr.Tree_ID FROM TreeRights tr, Rights r "+ 
                                          " WHERE tr.Rt_ID = r.Rt_ID AND tr.Tree_ID="+String.valueOf(treeID)); 
        while (res.next()) {
            s = new String[4];
            s[0] = res.getString("G_ID");
            s[1] = res.getString("R_ID");
            s[2] = res.getString("Rights");
            s[3] = res.getString("Rt_ID");            
            v.add(s);
        }
        return v;
    }    

    /**
     * @param r массив строк полученых от getArrayRights
     * @return {@link Vector} массивов строк из 3 элементов String[0] - G_ID, String[1] - R_ID, String[2] - Rights
     */
    public Vector parseRights(String[] r) {
        if (r==null) return null;    
        String[] s;
        Vector v = new Vector();
        boolean isAdd = true;
        for (int i=0; i < r.length; i++) {
            s = new String[3];
            s[0] = r[i].substring(7,r[i].indexOf("]"));
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
     * ѕолучает из cfgTuner параметры вида rright[G][R]=V или wright[G][R]=V, <br>
     * где <ul><li><b>G</b> - ID группы <br>
     *     <li><b>R</b> - ID роли <br>
     *     <li><b>V</b> - «начение права (при rright - 1, при wright - 2)</ul>
     * @return массив строк вида rright[G][R]=V и wright[G][R]=V
     */
    public String[] getArrayRights() {
        String[] arr=cfgTuner.getParameters();
        Vector v = new Vector();
        for (int i=0; i < arr.length; i++) {
            if ((arr[i].toLowerCase().indexOf("rright",0)==0) || (arr[i].toLowerCase().indexOf("wright",0)==0)) {
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
