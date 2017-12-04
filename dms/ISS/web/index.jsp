<!DOCTYPE HTML PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN"
"http://www.w3.org/TR/html4/loose.dtd">
<%@ page contentType="text/html;charset=utf-8"%>
<%
boolean isProduction = (request.getServerName().indexOf("iss.jinr.ru")>-1);
String uri = (isProduction
                    ? "https://"
                    : request.getScheme() + "://") +   // "http" + "://
             request.getServerName() +       // "iss.jinr.ru"
             (isProduction
                    ?  ""
                    : ":" + request.getServerPort()) +       // ":8080"
             ((isProduction)
                    ? request.getRequestURI().replaceAll("/iss/","")
                    : request.getRequestURI())                                  // "/iss/index.jsp"
             ;
             uri = uri.replaceAll("index.jsp","");
            //if(request.getServerName().indexOf("iss.jinr.ru")>-1) uri = uri.replaceAll("/iss","");

%>
<html>
  <head>
    <meta http-equiv="Content-Type" content="text/html; charset=utf-8"/>
    <title>Информационно-справочная система</title>
  </head>
  <!--<%=request.getServerName()%>-->

  <frameset rows="*" cols="*" name="topFrame" scrolling="no">
  <frame src="<%=uri%>/iss">
  </frameset>

</html>