<%@ page contentType="text/html;charset=utf-8"%>
<%@page import="java.util.*, myiss.*, java.net.*"%>
<jsp:useBean id="JspData" class="java.util.HashMap" scope="request"/>
<%
String issVersion = (String)JspData.get("iss-version");
int uId = Tools.parseInt(JspData.get("UID"),-1);
String userName = Tools.getStringValue(JspData.get("USER_NAME"),"");
//String baseUrl = Tools.getStringValue(JspData.get("SERVLET_BASE_URL"),"");
boolean isProduction = (request.getServerName().indexOf("iss.jinr.ru")>-1);
        String  baseUrl = (isProduction
                    ? "https://"
                    : request.getScheme() + "://") +   // "http" + "://
             request.getServerName() +       // "iss.jinr.ru"
             (isProduction
                    ?  ""
                    : ":" + request.getServerPort()) +       // ":8080"
             ((isProduction)
                    ? request.getRequestURI()  //.replaceAll("/iss/","")
                    : request.getRequestURI())                                  // "/iss/index.jsp"
             ;

               baseUrl = baseUrl.replaceAll("mainpage.jsp","");
%>
<div id = "logo">
<H1><a href = "<%=baseUrl%>" id = "homelink">Информационно-справочная система</a> 
<a href="<%=baseUrl%>/1.html" class="iss-version" id = "iss-version">
<sup>v<%=issVersion%></sup>
</a>
</H1>
<%--
<sup><a href="http://adm2.jinr.ru/query/" target="_blank">Старая версия ИСС</a></sup>
--%>
</div>
<div id="auth_form">
<%

if(uId<0){
%>

<form name = "auth_form" action="iss" method="POST">
    Имя пользователя:&nbsp;<input type="text" name = "userlogin"/>&nbsp;&nbsp;Пароль:&nbsp;<input name = "userpass" type="password"/>&nbsp;&nbsp;<input type="submit" name ="postlogin" value = "Войти"/>
</form>

<%
}else{
%>

<form name = "auth_form" action="<%=baseUrl%>logout" method="post">
    <input type="hidden" name = "postlogout" value = "1"/>
     Пользователь:&nbsp;<%=userName%>&nbsp;&nbsp;<input type="submit" name ="postlogout1" value = "Выйти"/>
</form><%

}
%>
<%--
<a href ="http://login.jinr.ru/cgi-bin/authorize?client_id=9&backurl=http%3A%2F%2Fomega.jinr.ru%3A8084%2Fmyiss%2Foauth">Войти с помощью JINR SSO</a>
--%>
</div>
<div class="clear"></div>
<%
if(Tools.parseInt(request.getParameter("wrong_auth"),-1)==1){
%>
<div id="wrong_auth">
Неверные имя пользователя или пароль!
</div>
<%
}
%>
