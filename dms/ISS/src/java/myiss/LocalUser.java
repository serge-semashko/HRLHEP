/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package myiss;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import org.inet.dbtools.DBSelect;

/**
 *
 * @author Pavel
 */
public class LocalUser {
    private static final String sSQL = "SELECT ID, USER_ID, PASSWORD, IP FROM USERS  WHERE NAME = ? AND PASSWORD = ?";
    private static final String USER_ID = "USER_ID";
    private static final String PASSWORD = "PASSWORD";
    private static final String IP = "IP";
    private static final String ID = "ID";
    private static final String LOGIN = "LOGIN";
    
    private Map userData;
    
    private int id;
    
    private String userId;
    private String userPassword; 
    private String userIp;
    private String userLogin;
    
    private boolean init = false;
    public void LocalUser(String login, String password, Connection conn ) throws SQLException{
        ArrayList sqlParams = new ArrayList();
        sqlParams.add(login);
        sqlParams.add(password);
        userData = DBSelect.getRow(sSQL, sqlParams, conn);
        id = Tools.parseInt(userData.get(ID), -1);
        userId = Tools.getStringValue(userData.get(USER_ID), "");
        userIp = Tools.getStringValue(userData.get(IP), "");
        userLogin = Tools.getStringValue(userData.get(LOGIN), "");
        userPassword = Tools.getStringValue(userData.get(PASSWORD), "");
        init = true;
    }
    public boolean isInit(){
        return init;
    }
    
    public int getUid(){
        return id;
    }
    
    public String getUserId(){
        return userId;
    }
    public String getUserIp(){
        return userIp;
    }
    
    public String getUserLogin(){
        return userLogin;
    }
    
    public String getUserPassword(){
        return userPassword;
    }
    
    public HashMap getUserData(){
        return (HashMap) userData;
    }
}
